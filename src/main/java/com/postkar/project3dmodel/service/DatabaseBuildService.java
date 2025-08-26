package com.postkar.project3dmodel.service;

import com.postkar.project3dmodel.entity.ArDatabase;
import com.postkar.project3dmodel.entity.Marker;
import com.postkar.project3dmodel.repository.ArDatabaseRepository;
import com.postkar.project3dmodel.repository.MarkerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class DatabaseBuildService {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseBuildService.class);

    // ARCore image requirements
    private static final int MIN_IMAGE_SIZE = 480;
    private static final long MAX_IMAGE_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final int ARCOREIMG_TIMEOUT_MINUTES = 5;

    @Autowired
    private ArDatabaseRepository arDatabaseRepository;

    @Autowired
    private MarkerRepository markerRepository;

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private ArDatabaseService arDatabaseService;

    @Value("${ar.build.temp.directory:/tmp/ar-build}")
    private String tempBuildDirectory;

    @Value("${ar.arcoreimg.path:arcoreimg}")
    private String arCoreImgPath;

    @Value("${ar.build.enabled:true}")
    private boolean buildEnabled;

    @Async
    public void buildDatabaseAsync(String databaseId) {
        logger.info("Starting async database build for ID: {}", databaseId);

        try {
            buildDatabase(databaseId);
        } catch (Exception e) {
            logger.error("Database build failed for ID: {}", databaseId, e);
            markBuildFailed(databaseId, e.getMessage());
        }
    }

    public void buildDatabase(String databaseId) throws Exception {
        if (!buildEnabled) {
            logger.info("Database build disabled, skipping build for ID: {}", databaseId);
            return;
        }

        Optional<ArDatabase> dbOpt = arDatabaseRepository.findById(databaseId);
        if (dbOpt.isEmpty()) {
            throw new RuntimeException("Database not found: " + databaseId);
        }

        ArDatabase database = dbOpt.get();
        StringBuilder buildLog = new StringBuilder();
        buildLog.append("Build started at: ").append(LocalDateTime.now()).append("\n");

        try {
            // 1. Verify ARCore tool availability
            verifyArCoreToolAvailability(buildLog);

            // 2. Create temp build directory
            Path buildDir = Paths.get(tempBuildDirectory, "build-" + databaseId);
            Files.createDirectories(buildDir);
            buildLog.append("Created build directory: ").append(buildDir).append("\n");

            // 3. Get all active markers
            List<Marker> activeMarkers = markerRepository.findByIsActiveTrue();
            if (activeMarkers.isEmpty()) {
                throw new RuntimeException("No active markers found");
            }
            buildLog.append("Found ").append(activeMarkers.size()).append(" active markers\n");

            // 4. Download and validate marker images
            Path inputListFile = buildDir.resolve("images.txt");
            int validMarkers = downloadAndValidateMarkers(activeMarkers, buildDir, inputListFile, buildLog);

            if (validMarkers == 0) {
                throw new RuntimeException("No valid marker images after validation. Check build log for details.");
            }

            // 5. Build .imgdb using arcoreimg with proper error handling
            Path imgdbFile = buildDir.resolve("markers_" + database.getVersion() + ".imgdb");
            runArCoreImgBuild(inputListFile, imgdbFile, buildLog);

            // 6. Verify output file was created
            if (!Files.exists(imgdbFile) || Files.size(imgdbFile) == 0) {
                throw new RuntimeException("ARCore build completed but output file is missing or empty");
            }

            // 7. Calculate checksum
            String checksum = calculateSHA256(imgdbFile);
            buildLog.append("Generated checksum: ").append(checksum).append("\n");

            // 8. Upload .imgdb to cloud storage
            String cloudUrl = fileUploadService.uploadFile(imgdbFile.toFile(),
                    "imgdb/markers_" + database.getVersion() + ".imgdb");
            buildLog.append("Uploaded to: ").append(cloudUrl).append("\n");

            // 9. Update database record
            database.setImgdbUrl(cloudUrl);
            database.setImgdbChecksum(checksum);
            database.setBuildStatus("ready");
            database.setBuildLog(buildLog.toString());
            arDatabaseRepository.save(database);

            // 10. Auto-publish if this is the first database or if auto-publish is enabled
            arDatabaseService.publishDatabase(databaseId);

            // 11. Cleanup temp files
            deleteDirectory(buildDir.toFile());

            logger.info("Database build completed successfully for ID: {}", databaseId);

        } catch (Exception e) {
            buildLog.append("Build failed: ").append(e.getMessage()).append("\n");
            database.setBuildStatus("failed");
            database.setBuildLog(buildLog.toString());
            arDatabaseRepository.save(database);
            throw e;
        }
    }

    private void verifyArCoreToolAvailability(StringBuilder buildLog) throws Exception {
        try {
            ProcessBuilder pb = new ProcessBuilder(arCoreImgPath, "--help");
            pb.redirectErrorStream(true);

            Process process = pb.start();
            boolean completed = process.waitFor(10, TimeUnit.SECONDS);

            if (!completed) {
                process.destroyForcibly();
                throw new RuntimeException("ARCore tool verification timed out");
            }

            if (process.exitValue() != 0 && process.exitValue() != 1) { // --help often returns 1
                throw new RuntimeException("ARCore tool not properly installed or accessible");
            }

            buildLog.append("ARCore tool verification passed\n");

        } catch (IOException e) {
            throw new RuntimeException("ARCore tool not found: " + arCoreImgPath +
                    ". Please ensure ARCore SDK is installed and PATH is configured correctly.", e);
        }
    }

    private int downloadAndValidateMarkers(List<Marker> markers, Path buildDir,
                                           Path inputListFile, StringBuilder buildLog) throws Exception {

        int validMarkerCount = 0;

        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(inputListFile, StandardCharsets.UTF_8))) {
            for (Marker marker : markers) {
                try {
                    String imageUrl = marker.getMarkerImageUrl();
                    if (imageUrl == null || imageUrl.isEmpty()) {
                        buildLog.append("Warning: No image URL for marker ").append(marker.getMarkerId()).append("\n");
                        continue;
                    }

                    buildLog.append("Processing marker: ").append(marker.getMarkerId())
                            .append(" from URL: ").append(imageUrl).append("\n");

                    // Download file with original extension first
                    String originalExtension = getFileExtension(imageUrl);
                    Path originalFile = buildDir.resolve("marker_" + marker.getMarkerId() + "_original." + originalExtension);

                    try {
                        fileUploadService.downloadFile(imageUrl, originalFile.toFile());
                        buildLog.append("Downloaded image: ").append(originalFile).append(" (")
                                .append(Files.size(originalFile)).append(" bytes)\n");
                    } catch (Exception e) {
                        buildLog.append("Error downloading image for marker ").append(marker.getMarkerId())
                                .append(": ").append(e.getMessage()).append("\n");
                        continue;
                    }

                    // Validate and preprocess image
                    Path processedFile = buildDir.resolve("marker_" + marker.getMarkerId() + ".png");
                    if (validateAndPreprocessImage(originalFile, processedFile, buildLog, marker.getMarkerId())) {
                        // Add to input list (format: name|path|widthInMeters)
                        String pathForArCore = processedFile.toAbsolutePath().toString().replace("\\", "/");
                        String markerName = marker.getMarkerId().trim();
                        double width = 0.1;

                        // Write properly formatted line - ARCore expects pipe-separated format
                        // Format: <marker_name>|<image_path>|<width_in_meters>
                        writer.println(markerName + "|" + pathForArCore + "|" + width);

                        validMarkerCount++;

                        buildLog.append("Successfully processed marker image: ").append(marker.getMarkerId())
                                .append(" (").append(width).append("m)\n");
                    } else {
                        buildLog.append("Warning: Skipped invalid marker image: ").append(marker.getMarkerId()).append("\n");
                    }

                    // Clean up original file
                    Files.deleteIfExists(originalFile);

                } catch (Exception e) {
                    buildLog.append("Error processing marker ").append(marker.getMarkerId())
                            .append(": ").append(e.getMessage()).append("\n");
                    logger.warn("Failed to process marker {}: {}", marker.getMarkerId(), e.getMessage(), e);
                }
            }
        }

        // Debug: Log the contents of the input list file
        try {
            List<String> lines = Files.readAllLines(inputListFile, StandardCharsets.UTF_8);
            buildLog.append("Input list file contents:\n");
            for (int i = 0; i < lines.size(); i++) {
                buildLog.append("Line ").append(i + 1).append(": [").append(lines.get(i)).append("]\n");
            }
        } catch (Exception e) {
            buildLog.append("Warning: Could not read input list file for debugging: ").append(e.getMessage()).append("\n");
        }

        buildLog.append("Created input list file with ").append(validMarkerCount).append(" valid markers\n");
        return validMarkerCount;
    }

    private boolean validateAndPreprocessImage(Path inputFile, Path outputFile, StringBuilder buildLog, String markerId) {
        try {
            buildLog.append("Validating image for marker ").append(markerId).append(": ").append(inputFile).append("\n");

            // Check if file exists
            if (!Files.exists(inputFile)) {
                buildLog.append("Error: Input file does not exist\n");
                return false;
            }

            // Check file size
            long fileSize = Files.size(inputFile);
            buildLog.append("File size: ").append(fileSize).append(" bytes\n");

            if (fileSize > MAX_IMAGE_FILE_SIZE) {
                buildLog.append("Error: Image too large: ").append(fileSize).append(" bytes (max: ").append(MAX_IMAGE_FILE_SIZE).append(")\n");
                return false;
            }

            if (fileSize == 0) {
                buildLog.append("Error: Image file is empty\n");
                return false;
            }

            // Read and validate image
            BufferedImage image;
            try {
                image = ImageIO.read(inputFile.toFile());
            } catch (Exception e) {
                buildLog.append("Error: Cannot read image file - ").append(e.getMessage()).append("\n");
                return false;
            }

            if (image == null) {
                buildLog.append("Error: ImageIO returned null - file may be corrupted or unsupported format\n");
                return false;
            }

            // Check dimensions
            buildLog.append("Image dimensions: ").append(image.getWidth()).append("x").append(image.getHeight()).append("\n");

            if (image.getWidth() < MIN_IMAGE_SIZE || image.getHeight() < MIN_IMAGE_SIZE) {
                buildLog.append("Error: Image too small: ").append(image.getWidth()).append("x").append(image.getHeight())
                        .append(" (minimum: ").append(MIN_IMAGE_SIZE).append("x").append(MIN_IMAGE_SIZE).append(")\n");
                return false;
            }

            // Convert to RGB if necessary and ensure high quality
            BufferedImage processedImage = new BufferedImage(
                    image.getWidth(),
                    image.getHeight(),
                    BufferedImage.TYPE_INT_RGB
            );

            Graphics2D g2d = processedImage.createGraphics();
            try {
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Fill with white background first (in case of transparency)
                g2d.setColor(Color.WHITE);
                g2d.fillRect(0, 0, image.getWidth(), image.getHeight());

                // Draw the image
                g2d.drawImage(image, 0, 0, null);
            } finally {
                g2d.dispose();
            }

            // Save as high-quality PNG
            boolean saved = ImageIO.write(processedImage, "PNG", outputFile.toFile());
            if (!saved) {
                buildLog.append("Error: Failed to save processed image\n");
                return false;
            }

            // Verify the output file was created and has content
            if (!Files.exists(outputFile) || Files.size(outputFile) == 0) {
                buildLog.append("Error: Output file was not created or is empty\n");
                return false;
            }

            buildLog.append("Successfully processed and saved image: ").append(outputFile)
                    .append(" (").append(Files.size(outputFile)).append(" bytes)\n");

            return true;

        } catch (Exception e) {
            buildLog.append("Image processing error for marker ").append(markerId).append(": ")
                    .append(e.getMessage()).append("\n");
            logger.error("Failed to process image {} for marker {}: {}", inputFile, markerId, e.getMessage(), e);
            return false;
        }
    }

    private String getFileExtension(String url) {
        if (url == null || !url.contains(".")) return "jpg";

        String[] parts = url.split("\\.");
        String extension = parts[parts.length - 1].toLowerCase();

        // Handle query parameters
        if (extension.contains("?")) {
            extension = extension.split("\\?")[0];
        }

        // Handle common URL parameters and ensure valid extension
        switch (extension) {
            case "jpeg":
                return "jpg";
            case "png":
            case "jpg":
            case "bmp":
            case "gif":
                return extension;
            default:
                return "jpg"; // Default fallback
        }
    }

    private void runArCoreImgBuild(Path inputListFile, Path outputFile, StringBuilder buildLog) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
                arCoreImgPath,
                "build-db",
                "--input_image_list_path=" + inputListFile.toAbsolutePath(),
                "--output_db_path=" + outputFile.toAbsolutePath()
        );

        // Set working directory and environment
        pb.directory(outputFile.getParent().toFile());
        pb.redirectErrorStream(false); // Keep stdout and stderr separate

        buildLog.append("Running ARCore command: ").append(String.join(" ", pb.command())).append("\n");
        buildLog.append("Working directory: ").append(pb.directory()).append("\n");

        Process process = pb.start();

        // Capture output and errors separately with timeout
        StringBuilder stdout = new StringBuilder();
        StringBuilder stderr = new StringBuilder();

        Thread outputReader = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    stdout.append(line).append("\n");
                    logger.info("ARCore stdout: {}", line);
                }
            } catch (IOException e) {
                logger.error("Error reading ARCore stdout", e);
            }
        });

        Thread errorReader = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    stderr.append(line).append("\n");
                    logger.warn("ARCore stderr: {}", line);
                }
            } catch (IOException e) {
                logger.error("Error reading ARCore stderr", e);
            }
        });

        outputReader.start();
        errorReader.start();

        // Wait for process with timeout
        boolean completed = process.waitFor(ARCOREIMG_TIMEOUT_MINUTES, TimeUnit.MINUTES);

        if (!completed) {
            process.destroyForcibly();
            throw new RuntimeException("ARCore build process timed out after " + ARCOREIMG_TIMEOUT_MINUTES + " minutes");
        }

        // Wait for output readers to finish
        outputReader.join(5000);
        errorReader.join(5000);

        int exitCode = process.exitValue();

        buildLog.append("ARCore build exit code: ").append(exitCode).append("\n");
        buildLog.append("ARCore stdout:\n").append(stdout.toString()).append("\n");

        if (stderr.length() > 0) {
            buildLog.append("ARCore stderr:\n").append(stderr.toString()).append("\n");
        }

        if (exitCode != 0) {
            String errorMsg = String.format("arcoreimg build failed with exit code: %d\nStderr: %s\nStdout: %s",
                    exitCode, stderr.toString(), stdout.toString());
            throw new RuntimeException(errorMsg);
        }

        buildLog.append("ARCore build completed successfully\n");
    }

    private String calculateSHA256(Path file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        try (FileInputStream fis = new FileInputStream(file.toFile());
             DigestInputStream dis = new DigestInputStream(fis, digest)) {

            byte[] buffer = new byte[8192];
            while (dis.read(buffer) != -1) {
                // Reading file to calculate digest
            }
        }

        byte[] hash = digest.digest();
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return "sha256:" + hexString.toString();
    }

    private void markBuildFailed(String databaseId, String error) {
        arDatabaseRepository.findById(databaseId).ifPresent(db -> {
            db.setBuildStatus("failed");
            db.setBuildLog(db.getBuildLog() + "\nFinal error: " + error);
            arDatabaseRepository.save(db);
        });
    }

    private void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        if (!file.delete()) {
                            logger.warn("Failed to delete file: {}", file.getAbsolutePath());
                        }
                    }
                }
            }
            if (!directory.delete()) {
                logger.warn("Failed to delete directory: {}", directory.getAbsolutePath());
            }
        }
    }
}