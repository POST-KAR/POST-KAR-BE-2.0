package com.postkar.project3dmodel.controller;

import com.postkar.project3dmodel.dto.HeroPosterUploadRequest;
import com.postkar.project3dmodel.entity.HeroImage;
import com.postkar.project3dmodel.response.HeroPosterUploadResponse;
import com.postkar.project3dmodel.service.HeroImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/hero")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Hero Section", description = "Hero section APIs for managing hero posters")
@Validated
public class HeroController {

    private final HeroImageService heroImageService;

    @Operation(summary = "Get Random Hero Images", description = "Get 10 random active hero images for display (public endpoint)")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getHeroImages() {
        log.info("Getting random hero images");

        try {
            List<HeroImage> images = heroImageService.getRandomHeroImages();

            List<Map<String, Object>> heroData = images.stream()
                    .map(img -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", img.getId());
                        map.put("imageUrl", img.getImageUrl());
                        map.put("title", img.getTitle() != null ? img.getTitle() : "");
                        map.put("description", img.getDescription() != null ? img.getDescription() : "");
                        return map;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", heroData);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting hero images", e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get hero images");

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @Operation(summary = "Upload Hero Poster", description = "Upload a new hero poster image to Cloudflare R2 and save metadata to MongoDB (admin only).\n\n"
            +
            "**Only the file is required.** All metadata will be auto-generated if not provided:\n" +
            "- title: Generated from filename (e.g., 'sunset-beach.jpg' -> 'Sunset Beach')\n" +
            "- description: Empty string (can be updated later)\n" +
            "- displayOrder: Auto-incremented from highest existing order\n" +
            "- isActive: Defaults to true\n" +
            "- uploadedBy: Defaults to 'system'", responses = {
                    @ApiResponse(responseCode = "201", description = "Hero poster uploaded successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid input or file"),
                    @ApiResponse(responseCode = "500", description = "Server error")
            })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadHeroPoster(
            @Parameter(description = "Hero poster image file", required = true) @RequestParam("file") MultipartFile file,

            @Parameter(description = "Title of the hero image (optional, auto-generated from filename if not provided)") @RequestParam(value = "title", required = false) String title,

            @Parameter(description = "Description of the hero image (optional)") @RequestParam(value = "description", required = false) String description,

            @Parameter(description = "Display order (optional, auto-incremented if not provided)") @RequestParam(value = "displayOrder", required = false) Integer displayOrder,

            @Parameter(description = "Whether the image is active (optional, defaults to true)") @RequestParam(value = "isActive", required = false) Boolean isActive,

            @Parameter(description = "Admin username (optional, defaults to 'system')") @RequestParam(value = "uploadedBy", required = false) String uploadedBy) {
        log.info("Hero poster upload request received: {}", file.getOriginalFilename());

        try {
            // Validate file
            if (file.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "File cannot be empty");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Create request object (all fields are optional now)
            HeroPosterUploadRequest request = new HeroPosterUploadRequest();
            request.setTitle(title);
            request.setDescription(description);
            request.setDisplayOrder(displayOrder);
            request.setIsActive(isActive);
            request.setUploadedBy(uploadedBy);

            // Upload poster (metadata will be auto-generated as needed)
            HeroImage heroImage = heroImageService.uploadHeroPoster(file, request);

            // Build response
            HeroPosterUploadResponse uploadResponse = HeroPosterUploadResponse.builder()
                    .id(heroImage.getId())
                    .imageUrl(heroImage.getImageUrl())
                    .title(heroImage.getTitle())
                    .description(heroImage.getDescription())
                    .displayOrder(heroImage.getDisplayOrder())
                    .fileSize(heroImage.getFileSize())
                    .contentType(heroImage.getContentType())
                    .createdAt(heroImage.getCreatedAt())
                    .isActive(heroImage.isActive())
                    .uploadedBy(heroImage.getUploadedBy())
                    .build();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Hero poster uploaded successfully");
            response.put("data", uploadResponse);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            log.error("Invalid input for hero poster upload", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            log.error("Error uploading hero poster", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to upload hero poster: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @Operation(summary = "Get All Hero Images", description = "Get all hero images for admin management (admin only)")
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllHeroImages() {
        log.info("Getting all hero images for admin");

        try {
            List<HeroImage> images = heroImageService.getAllHeroImages();

            List<HeroPosterUploadResponse> heroData = images.stream()
                    .map(img -> HeroPosterUploadResponse.builder()
                            .id(img.getId())
                            .imageUrl(img.getImageUrl())
                            .title(img.getTitle())
                            .description(img.getDescription())
                            .displayOrder(img.getDisplayOrder())
                            .fileSize(img.getFileSize())
                            .contentType(img.getContentType())
                            .createdAt(img.getCreatedAt())
                            .isActive(img.isActive())
                            .uploadedBy(img.getUploadedBy())
                            .build())
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", heroData.size());
            response.put("data", heroData);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting all hero images", e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get hero images");

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @Operation(summary = "Update Hero Poster Metadata", description = "Update metadata of an existing hero poster (admin only)")
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateHeroPoster(
            @Parameter(description = "Hero image ID") @PathVariable String id,
            @Parameter(description = "Title") @RequestParam(required = false) String title,
            @Parameter(description = "Description") @RequestParam(required = false) String description,
            @Parameter(description = "Display order") @RequestParam(required = false) Integer displayOrder,
            @Parameter(description = "Active status") @RequestParam(required = false) Boolean isActive) {
        log.info("Updating hero poster: {}", id);

        try {
            HeroPosterUploadRequest request = new HeroPosterUploadRequest();
            request.setTitle(title);
            request.setDescription(description);
            request.setDisplayOrder(displayOrder);
            request.setIsActive(isActive);

            HeroImage updatedImage = heroImageService.updateHeroPoster(id, request);

            HeroPosterUploadResponse uploadResponse = HeroPosterUploadResponse.builder()
                    .id(updatedImage.getId())
                    .imageUrl(updatedImage.getImageUrl())
                    .title(updatedImage.getTitle())
                    .description(updatedImage.getDescription())
                    .displayOrder(updatedImage.getDisplayOrder())
                    .fileSize(updatedImage.getFileSize())
                    .contentType(updatedImage.getContentType())
                    .createdAt(updatedImage.getCreatedAt())
                    .isActive(updatedImage.isActive())
                    .uploadedBy(updatedImage.getUploadedBy())
                    .build();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Hero poster updated successfully");
            response.put("data", uploadResponse);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Hero poster not found: {}", id, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error("Error updating hero poster: {}", id, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to update hero poster");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @Operation(summary = "Delete Hero Poster", description = "Delete a hero poster from both R2 and MongoDB (admin only)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteHeroPoster(
            @Parameter(description = "Hero image ID") @PathVariable String id) {
        log.info("Deleting hero poster: {}", id);

        try {
            heroImageService.deleteHeroPoster(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Hero poster deleted successfully");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Hero poster not found: {}", id, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error("Error deleting hero poster: {}", id, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to delete hero poster");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @Operation(summary = "Toggle Hero Poster Active Status", description = "Toggle the active status of a hero poster (admin only)")
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Map<String, Object>> toggleActiveStatus(
            @Parameter(description = "Hero image ID") @PathVariable String id) {
        log.info("Toggling active status for hero poster: {}", id);

        try {
            HeroImage updatedImage = heroImageService.toggleActiveStatus(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Hero poster active status toggled successfully");
            response.put("data", Map.of(
                    "id", updatedImage.getId(),
                    "isActive", updatedImage.isActive()));

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Hero poster not found: {}", id, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error("Error toggling hero poster status: {}", id, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to toggle hero poster status");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
