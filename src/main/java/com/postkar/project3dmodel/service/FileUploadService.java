package com.postkar.project3dmodel.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.*;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.Arrays;
import java.util.List;

@Service
public class FileUploadService {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadService.class);

    @Value("${cloudflare.r2.bucket}")
    private String bucketName;

    @Value("${cloudflare.r2.account-id}")
    private String accountId;

    @Value("${cloudflare.r2.access-key-id}")
    private String accessKeyId;

    @Value("${cloudflare.r2.secret-access-key}")
    private String secretAccessKey;

    @Value("${cloudflare.r2.endpoint:}")
    private String customEndpoint;

    @Value("${cloudflare.r2.public-url:}")
    private String publicUrl; // Optional: custom R2 public domain

    private S3Client s3Client;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );
    private static final List<String> ALLOWED_VIDEO_TYPES = Arrays.asList(
            "video/mp4", "video/mpeg", "video/quicktime", "video/x-msvideo"
    );

    private static final int DOWNLOAD_TIMEOUT_SECONDS = 30;
    private static final int MAX_DOWNLOAD_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;

    @PostConstruct
    public void initializeS3Client() {
        try {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);

            // Cloudflare R2 endpoint format: https://<account-id>.r2.cloudflarestorage.com
            String endpoint = customEndpoint != null && !customEndpoint.isEmpty() 
                ? customEndpoint 
                : String.format("https://%s.r2.cloudflarestorage.com", accountId);

            this.s3Client = S3Client.builder()
                    .endpointOverride(java.net.URI.create(endpoint))
                    .region(Region.of("auto")) // R2 uses "auto" region
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .serviceConfiguration(S3Configuration.builder()
                            .pathStyleAccessEnabled(false) // R2 uses virtual-hosted-style
                            .build())
                    .build();

            logger.info("Cloudflare R2 client initialized successfully for account: {}", accountId);
        } catch (Exception e) {
            logger.error("Failed to initialize R2 client", e);
            throw new RuntimeException("R2 configuration error", e);
        }
    }


    public String uploadFile(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        validateFileType(file, folder);

        String key = generateFileKey(file.getOriginalFilename(), folder);

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .metadata(java.util.Map.of(
                            "original-filename", file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown",
                            "upload-timestamp", LocalDateTime.now().toString(),
                            "folder", folder
                    ))
                    .build();

            PutObjectResponse response = s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            String fileUrl = getFileUrl(key);
            logger.info("File uploaded successfully to R2: {} -> {}", file.getOriginalFilename(), fileUrl);
            return fileUrl;

        } catch (S3Exception e) {
            logger.error("Failed to upload file to R2: {}", e.awsErrorDetails().errorMessage(), e);
            throw new RuntimeException("Failed to upload file to R2: " + e.awsErrorDetails().errorMessage(), e);
        }
    }


    public String uploadFileByCategory(MultipartFile file, String categoryName, String fileType) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }


        validateFileTypeByCategory(file, fileType);


        String key = generateCategoryBasedFileKey(file.getOriginalFilename(), categoryName, fileType);

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .metadata(java.util.Map.of(
                            "original-filename", file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown",
                            "upload-timestamp", LocalDateTime.now().toString(),
                            "category", categoryName,
                            "file-type", fileType
                    ))
                    .build();

            PutObjectResponse response = s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            String fileUrl = getFileUrl(key);
            logger.info("File uploaded successfully to R2 with category structure: {} -> {}", file.getOriginalFilename(), fileUrl);
            return fileUrl;

        } catch (S3Exception e) {
            logger.error("Failed to upload file to R2: {}", e.awsErrorDetails().errorMessage(), e);
            throw new RuntimeException("Failed to upload file to R2: " + e.awsErrorDetails().errorMessage(), e);
        }
    }


    public String uploadFile(File file, String s3Key) throws IOException {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("File does not exist: " + file);
        }

        try {
            String contentType = Files.probeContentType(file.toPath());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(contentType)
                    .contentLength(file.length())
                    .build();

            PutObjectResponse response = s3Client.putObject(putObjectRequest,
                    RequestBody.fromFile(file));

            String fileUrl = getFileUrl(s3Key);
            logger.info("File uploaded successfully to R2: {} -> {}", file.getName(), fileUrl);
            return fileUrl;

        } catch (S3Exception e) {
            logger.error("Failed to upload file to R2: {}", e.awsErrorDetails().errorMessage(), e);
            throw new RuntimeException("Failed to upload file to R2: " + e.awsErrorDetails().errorMessage(), e);
        }
    }

    public void downloadFile(String fileUrl, File destinationFile) throws IOException {
        String key = extractKeyFromUrl(fileUrl);
        if (key == null) {
            throw new IllegalArgumentException("Cannot extract S3 key from URL: " + fileUrl);
        }

        IOException lastException = null;

        for (int attempt = 1; attempt <= MAX_DOWNLOAD_RETRIES; attempt++) {
            try {
                logger.info("Downloading file (attempt {}): {} -> {}", attempt, fileUrl, destinationFile.getAbsolutePath());

                File parentDir = destinationFile.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    boolean created = parentDir.mkdirs();
                    if (!created && !parentDir.exists()) {
                        throw new IOException("Failed to create parent directories: " + parentDir.getAbsolutePath());
                    }
                }

                GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build();

                ResponseInputStream<GetObjectResponse> response = s3Client.getObject(getObjectRequest);

                long expectedLength = response.response().contentLength();
                long downloadedBytes = 0;

                try (FileOutputStream fos = new FileOutputStream(destinationFile);
                     BufferedOutputStream bos = new BufferedOutputStream(fos, 32768)) { // Larger buffer

                    byte[] buffer = new byte[32768]; // 32KB buffer
                    int bytesRead;

                    while ((bytesRead = response.read(buffer)) != -1) {
                        bos.write(buffer, 0, bytesRead);
                        downloadedBytes += bytesRead;
                    }

                    bos.flush();
                }

                if (!destinationFile.exists()) {
                    throw new IOException("Downloaded file does not exist after download completed");
                }

                long actualSize = destinationFile.length();
                if (expectedLength > 0 && actualSize != expectedLength) {
                    throw new IOException(String.format("Downloaded file size mismatch. Expected: %d bytes, Actual: %d bytes",
                            expectedLength, actualSize));
                }

                if (actualSize == 0) {
                    throw new IOException("Downloaded file is empty");
                }

                logger.info("File downloaded successfully: {} -> {} ({} bytes)",
                        fileUrl, destinationFile.getAbsolutePath(), actualSize);
                return;

            } catch (S3Exception e) {
                lastException = new IOException("R2 error during download: " + e.awsErrorDetails().errorMessage(), e);
                logger.warn("R2 error on attempt {} for {}: {}", attempt, fileUrl, e.awsErrorDetails().errorMessage());

                if (e.statusCode() == 404 || e.statusCode() == 403) {
                    throw lastException;
                }

            } catch (IOException e) {
                lastException = e;
                logger.warn("IO error on attempt {} for {}: {}", attempt, fileUrl, e.getMessage());
            }

            if (destinationFile.exists()) {
                try {
                    Files.delete(destinationFile.toPath());
                } catch (Exception cleanupEx) {
                    logger.warn("Failed to cleanup partial download: {}", cleanupEx.getMessage());
                }
            }

            if (attempt < MAX_DOWNLOAD_RETRIES) {
                try {
                    Thread.sleep(RETRY_DELAY_MS * attempt); // Exponential backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Download interrupted", ie);
                }
            }
        }

        String errorMsg = String.format("Failed to download file after %d attempts: %s",
                MAX_DOWNLOAD_RETRIES, fileUrl);
        throw new IOException(errorMsg, lastException);
    }

    public String generateFileKey(String originalFilename, String folder) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        String extension = getFileExtension(originalFilename);
        String sanitizedName = sanitizeFilename(originalFilename);

        return String.format("%s/%s/%s_%s.%s", folder, timestamp, uniqueId, sanitizedName, extension);
    }


    public String generateCategoryBasedFileKey(String originalFilename, String categoryName, String fileType) {
        String extension = getFileExtension(originalFilename);
        String sanitizedName = sanitizeFilename(originalFilename);
        String sanitizedCategory = sanitizeFilename(categoryName);
        
        return String.format("%s/%s/%s.%s", sanitizedCategory, fileType, sanitizedName, extension);
    }

    private String getFileUrl(String key) {
        // If custom public URL is configured (e.g., custom domain), use it
        if (publicUrl != null && !publicUrl.isEmpty()) {
            return publicUrl.endsWith("/") ? publicUrl + key : publicUrl + "/" + key;
        } else {
            // Default R2 public URL format: https://<bucket>.<account-id>.r2.cloudflarestorage.com/<key>
            // Note: This requires the bucket to have public access enabled in R2 dashboard
            return String.format("https://%s.%s.r2.cloudflarestorage.com/%s", bucketName, accountId, key);
        }
    }

    private void validateFileType(MultipartFile file, String folder) {
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();

        if (contentType == null || filename == null) {
            throw new IllegalArgumentException("Invalid file: missing content type or filename");
        }

        switch (folder.toLowerCase()) {
            case "markers":
            case "thumbnails":
                if (!ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
                    throw new IllegalArgumentException("Only image files allowed for " + folder +
                            ". Got: " + contentType);
                }
                break;
            case "videos":
                if (!ALLOWED_VIDEO_TYPES.contains(contentType.toLowerCase())) {
                    throw new IllegalArgumentException("Only video files allowed for videos. Got: " + contentType);
                }
                break;
            case "imgdb":
                break;
            default:
                logger.warn("Unknown folder type: {}, allowing any file type", folder);
                break;
        }

        long maxSize = getMaxFileSize(folder);
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException(
                    String.format("File size %d bytes exceeds limit of %d MB",
                            file.getSize(), maxSize / 1024 / 1024));
        }
    }

    private void validateFileTypeByCategory(MultipartFile file, String fileType) {
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();

        if (contentType == null || filename == null) {
            throw new IllegalArgumentException("Invalid file: missing content type or filename");
        }

        switch (fileType.toLowerCase()) {
            case "markers":
            case "thumbnails":
                if (!ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
                    throw new IllegalArgumentException("Only image files allowed for " + fileType +
                            ". Got: " + contentType);
                }
                break;
            case "videos":
                if (!ALLOWED_VIDEO_TYPES.contains(contentType.toLowerCase())) {
                    throw new IllegalArgumentException("Only video files allowed for videos. Got: " + contentType);
                }
                break;
            default:
                logger.warn("Unknown file type: {}, allowing any file type", fileType);
                break;
        }

        long maxSize = getMaxFileSize(fileType);
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException(
                    String.format("File size %d bytes exceeds limit of %d MB",
                            file.getSize(), maxSize / 1024 / 1024));
        }
    }

    private long getMaxFileSize(String folder) {
        switch (folder.toLowerCase()) {
            case "markers":
            case "thumbnails":
                return 5 * 1024 * 1024;
            case "videos":
                return 500 * 1024 * 1024;
            case "imgdb":
                return 100 * 1024 * 1024;
            default:
                return 10 * 1024 * 1024;
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "bin";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private String sanitizeFilename(String filename) {
        if (filename == null) return "file";

        String name = filename.contains(".") ?
                filename.substring(0, filename.lastIndexOf(".")) : filename;

        name = name.replaceAll("[\\\\/]", "");

        String sanitized = name.replaceAll("[^a-zA-Z0-9._-]", "_").toLowerCase();

        if (sanitized.length() > 50) {
            sanitized = sanitized.substring(0, 50);
        }

        return sanitized.isEmpty() ? "file" : sanitized;
    }

    public boolean deleteFile(String fileUrl) {
        try {
            String key = extractKeyFromUrl(fileUrl);
            if (key != null) {
                DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build();

                s3Client.deleteObject(deleteRequest);
                logger.info("File deleted successfully: {}", fileUrl);
                return true;
            } else {
                logger.warn("Cannot extract S3 key from URL: {}", fileUrl);
            }
        } catch (S3Exception e) {
            logger.error("Failed to delete file: {}", e.awsErrorDetails().errorMessage(), e);
        }
        return false;
    }

    private String extractKeyFromUrl(String url) {
        if (url == null || url.trim().isEmpty()) return null;

        try {
            // Handle custom public URL
            if (publicUrl != null && !publicUrl.isEmpty() && url.startsWith(publicUrl)) {
                String baseUrlClean = publicUrl.endsWith("/") ? publicUrl.substring(0, publicUrl.length() - 1) : publicUrl;
                return url.substring(baseUrlClean.length() + 1);
            } 
            // Handle R2 cloudflarestorage.com URLs
            else if (url.contains(".r2.cloudflarestorage.com/")) {
                return url.substring(url.indexOf(".r2.cloudflarestorage.com/") + 26);
            } 
            // Legacy S3 URL support (for migration)
            else if (url.contains(".amazonaws.com/")) {
                return url.substring(url.indexOf(".amazonaws.com/") + 15);
            } 
            // Handle s3:// or r2:// protocol
            else if (url.startsWith("s3://") || url.startsWith("r2://")) {
                String withoutProtocol = url.substring(5);
                if (withoutProtocol.contains("/")) {
                    return withoutProtocol.substring(withoutProtocol.indexOf("/") + 1);
                }
            }
        } catch (Exception e) {
            logger.error("Error extracting key from URL: {}", url, e);
        }

        return null;
    }

    public boolean isConfigured() {
        return s3Client != null && bucketName != null && !bucketName.trim().isEmpty();
    }
}