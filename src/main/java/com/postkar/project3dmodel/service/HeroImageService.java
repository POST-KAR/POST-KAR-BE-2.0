package com.postkar.project3dmodel.service;

import com.postkar.project3dmodel.dto.HeroPosterUploadRequest;
import com.postkar.project3dmodel.entity.HeroImage;
import com.postkar.project3dmodel.repository.HeroImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class HeroImageService {

    private final HeroImageRepository heroImageRepository;
    private final FileUploadService fileUploadService;
    private static final int HERO_IMAGES_TO_RETURN = 10;
    private static final String HERO_CATEGORY = "hero"; // Simplified from "hero-posters"
    private static final String FILE_TYPE = "hero"; // Changed from "image" to "hero" for clarity

    public List<HeroImage> getRandomHeroImages() {
        log.info("Fetching random {} hero images", HERO_IMAGES_TO_RETURN);

        try {
            List<HeroImage> allImages = heroImageRepository.findAllActive();

            if (allImages.isEmpty()) {
                log.warn("No active hero images found");
                return Collections.emptyList();
            }

            // Shuffle and take first 10
            Collections.shuffle(allImages);
            return allImages.stream()
                    .limit(HERO_IMAGES_TO_RETURN)
                    .toList();
        } catch (Exception e) {
            log.error("Error fetching hero images", e);
            throw new RuntimeException("Failed to fetch hero images", e);
        }
    }

    public HeroImage uploadHeroPoster(MultipartFile file, HeroPosterUploadRequest request) throws IOException {
        log.info("Uploading hero poster: {}", file.getOriginalFilename());

        // Validate file
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        // Upload to Cloudflare R2
        String imageUrl = fileUploadService.uploadFileByCategory(file, HERO_CATEGORY, FILE_TYPE);

        // Extract file key from URL
        String fileKey = fileUploadService.extractKeyFromUrl(imageUrl);

        // Auto-generate metadata if not provided
        String title = (request.getTitle() != null && !request.getTitle().trim().isEmpty())
                ? request.getTitle()
                : generateTitleFromFilename(file.getOriginalFilename());

        String description = (request.getDescription() != null)
                ? request.getDescription()
                : "";

        Integer displayOrder = (request.getDisplayOrder() != null)
                ? request.getDisplayOrder()
                : getNextDisplayOrder();

        Boolean isActive = (request.getIsActive() != null)
                ? request.getIsActive()
                : true;

        String uploadedBy = (request.getUploadedBy() != null && !request.getUploadedBy().trim().isEmpty())
                ? request.getUploadedBy()
                : "system";

        // Create HeroImage entity
        HeroImage heroImage = new HeroImage();
        heroImage.setImageUrl(imageUrl);
        heroImage.setFileKey(fileKey);
        heroImage.setTitle(title);
        heroImage.setDescription(description);
        heroImage.setDisplayOrder(displayOrder);
        heroImage.setFileSize(file.getSize());
        heroImage.setContentType(file.getContentType());
        heroImage.setUploadedBy(uploadedBy);
        heroImage.setCreatedAt(LocalDateTime.now());
        heroImage.setUpdatedAt(LocalDateTime.now());
        heroImage.setActive(isActive);

        // Save to MongoDB
        HeroImage savedImage = heroImageRepository.save(heroImage);
        log.info(
                "Hero poster uploaded successfully with ID: {} (auto-generated: title={}, displayOrder={}, uploadedBy={})",
                savedImage.getId(), title, displayOrder, uploadedBy);

        return savedImage;
    }

    /**
     * Generate a readable title from filename
     * Example: "sunset-beach.jpg" -> "Sunset Beach"
     */
    private String generateTitleFromFilename(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return "Untitled Hero Image";
        }

        // Remove extension
        String nameWithoutExt = filename.contains(".")
                ? filename.substring(0, filename.lastIndexOf("."))
                : filename;

        // Replace underscores and hyphens with spaces
        String cleaned = nameWithoutExt.replaceAll("[_-]+", " ");

        // Capitalize each word
        String[] words = cleaned.split("\\s+");
        StringBuilder titleBuilder = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                if (titleBuilder.length() > 0) {
                    titleBuilder.append(" ");
                }
                titleBuilder.append(word.substring(0, 1).toUpperCase())
                        .append(word.substring(1).toLowerCase());
            }
        }

        String title = titleBuilder.toString().trim();
        return title.isEmpty() ? "Untitled Hero Image" : title;
    }

    /**
     * Get the next display order by finding the maximum existing order and adding 1
     * Returns 0 if no images exist
     */
    private Integer getNextDisplayOrder() {
        try {
            List<HeroImage> allImages = heroImageRepository.findAll();

            if (allImages.isEmpty()) {
                return 0;
            }

            Integer maxOrder = allImages.stream()
                    .map(HeroImage::getDisplayOrder)
                    .filter(order -> order != null)
                    .max(Integer::compareTo)
                    .orElse(-1);

            return maxOrder + 1;
        } catch (Exception e) {
            log.warn("Error calculating next display order, defaulting to 0", e);
            return 0;
        }
    }

    public HeroImage updateHeroPoster(String id, HeroPosterUploadRequest request) {
        log.info("Updating hero poster: {}", id);

        HeroImage heroImage = heroImageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hero image not found with id: " + id));

        // Update only the metadata fields
        if (request.getTitle() != null) {
            heroImage.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            heroImage.setDescription(request.getDescription());
        }
        if (request.getDisplayOrder() != null) {
            heroImage.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getIsActive() != null) {
            heroImage.setActive(request.getIsActive());
        }

        heroImage.setUpdatedAt(LocalDateTime.now());

        HeroImage updatedImage = heroImageRepository.save(heroImage);
        log.info("Hero poster updated successfully: {}", id);

        return updatedImage;
    }

    public void deleteHeroPoster(String id) {
        log.info("Deleting hero poster: {}", id);

        HeroImage heroImage = heroImageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hero image not found with id: " + id));

        // Delete from R2
        try {
            if (heroImage.getImageUrl() != null && !heroImage.getImageUrl().isEmpty()) {
                fileUploadService.deleteFile(heroImage.getImageUrl());
                log.info("Deleted hero poster from R2: {}", heroImage.getFileKey());
            }
        } catch (Exception e) {
            log.warn("Failed to delete file from R2, continuing with DB deletion", e);
        }

        // Delete from MongoDB
        heroImageRepository.deleteById(id);
        log.info("Hero poster deleted successfully: {}", id);
    }

    public HeroImage toggleActiveStatus(String id) {
        log.info("Toggling active status for hero poster: {}", id);

        HeroImage heroImage = heroImageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hero image not found with id: " + id));

        heroImage.setActive(!heroImage.isActive());
        heroImage.setUpdatedAt(LocalDateTime.now());

        HeroImage updatedImage = heroImageRepository.save(heroImage);
        log.info("Hero poster active status toggled to {}: {}", updatedImage.isActive(), id);

        return updatedImage;
    }

    public List<HeroImage> getAllHeroImages() {
        log.info("Fetching all hero images for admin");
        return heroImageRepository.findAll();
    }

    public Optional<HeroImage> getHeroImageById(String id) {
        return heroImageRepository.findById(id);
    }

    // Legacy methods for backward compatibility
    public HeroImage addHeroImage(HeroImage heroImage) {
        log.info("Adding new hero image");
        heroImage.setCreatedAt(LocalDateTime.now());
        heroImage.setUpdatedAt(LocalDateTime.now());
        return heroImageRepository.save(heroImage);
    }

    public void deleteHeroImage(String id) {
        log.info("Deleting hero image (legacy): {}", id);
        heroImageRepository.deleteById(id);
    }
}
