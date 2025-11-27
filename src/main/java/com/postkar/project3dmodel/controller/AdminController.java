package com.postkar.project3dmodel.controller;

import com.postkar.project3dmodel.dto.MarkerCreateRequest;
import com.postkar.project3dmodel.dto.MarkerUpdateRequest;
import com.postkar.project3dmodel.entity.Marker;
import com.postkar.project3dmodel.entity.Video;
import com.postkar.project3dmodel.response.UploadResponse;
import com.postkar.project3dmodel.service.CategoryService;
import com.postkar.project3dmodel.service.FileUploadService;
import com.postkar.project3dmodel.service.MarkerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Administrative APIs for marker and database management")
@Validated
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private MarkerService markerService;

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private CategoryService categoryService;

    @Operation(summary = "Upload Files", description = "Upload marker image, thumbnail, and video files to Cloudflare R2.\n\n"
            +
            "Returns actual R2 URLs that can be used in marker creation requests.\n" +
            "All files are uploaded to cloud storage with proper validation.\n" +
            "Files are organized by category: {categoryName}/{fileType}/{filename}")
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFiles(
            @Parameter(description = "Category ID for organizing files") @RequestParam(required = false) String categoryId,

            @Parameter(description = "Marker image file (PNG/JPG, max 5MB)") @RequestParam(required = false) MultipartFile markerImage,

            @Parameter(description = "Thumbnail image file (PNG/JPG, max 5MB)") @RequestParam(required = false) MultipartFile thumbnail,

            @Parameter(description = "Video file (MP4, max 500MB)") @RequestParam(required = false) MultipartFile video) {
        try {
            if (markerImage == null && thumbnail == null && video == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "At least one file must be provided"));
            }

            if (!fileUploadService.isConfigured()) {
                logger.error("FileUploadService is not properly configured");
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(Map.of("error", "File upload service is not configured"));
            }

            String categoryName = "uncategorized";
            if (categoryId != null && !categoryId.trim().isEmpty()) {
                var categoryOpt = categoryService.getCategoryById(categoryId.trim());
                if (categoryOpt.isPresent()) {
                    categoryName = categoryOpt.get().getName();
                } else {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Category not found: " + categoryId));
                }
            }

            UploadResponse response = new UploadResponse();
            Map<String, String> errors = new HashMap<>();

            if (markerImage != null) {
                try {
                    String url = fileUploadService.uploadFileByCategory(markerImage, categoryName, "markers");
                    response.setMarkerImageUrl(url);
                    logger.info("Marker image uploaded to category '{}': {}", categoryName, url);
                } catch (Exception e) {
                    logger.error("Failed to upload marker image", e);
                    errors.put("markerImage", e.getMessage());
                }
            }

            if (thumbnail != null) {
                try {
                    String url = fileUploadService.uploadFileByCategory(thumbnail, categoryName, "thumbnails");
                    response.setThumbnailUrl(url);
                    logger.info("Thumbnail uploaded to category '{}': {}", categoryName, url);
                } catch (Exception e) {
                    logger.error("Failed to upload thumbnail", e);
                    errors.put("thumbnail", e.getMessage());
                }
            }

            if (video != null) {
                try {
                    String url = fileUploadService.uploadFileByCategory(video, categoryName, "videos");
                    response.setVideoUrl(url);
                    logger.info("Video uploaded to category '{}': {}", categoryName, url);
                } catch (Exception e) {
                    logger.error("Failed to upload video", e);
                    errors.put("video", e.getMessage());
                }
            }

            if (!errors.isEmpty()) {
                Map<String, Object> responseWithErrors = new HashMap<>();
                responseWithErrors.put("uploads", response);
                responseWithErrors.put("errors", errors);
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(responseWithErrors);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Unexpected error during file upload", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Unexpected error during upload: " + e.getMessage()));
        }
    }

    @Operation(summary = "Upload and Create Marker", description = "Upload files and create a new AR marker in a single request.\n\n"
            +
            "This endpoint handles the complete marker creation process:\n" +
            "1. Upload marker image, video, and optional thumbnail to Cloudflare R2\n" +
            "2. Automatically create marker record in database with generated URLs\n" +
            "3. CategoryId is optional - leave empty if you don't want to categorize yet\n\n" +
            "All files and metadata are processed in one API call.")
    @PostMapping(value = "/markers/upload", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadAndCreateMarker(
            @Parameter(description = "Marker ID (required)", example = "MARKER_001") @RequestParam @NotBlank String markerId,

            @Parameter(description = "Marker name (required)", example = "Lion Marker") @RequestParam @NotBlank String name,

            @Parameter(description = "Description (optional)") @RequestParam(required = false) String description,

            @Parameter(description = "Physical width in meters (optional, default: 0.1)") @RequestParam(required = false) Double physicalWidthMeters,

            @Parameter(description = "Category ID (optional - can be left empty)") @RequestParam(required = false) String categoryId,

            @Parameter(description = "Video name (optional)") @RequestParam(required = false) String videoName,

            @Parameter(description = "Marker image file (PNG/JPG, required)") @RequestParam MultipartFile markerImage,

            @Parameter(description = "Video file (MP4, required)") @RequestParam MultipartFile video,

            @Parameter(description = "Thumbnail image file (PNG/JPG, optional)") @RequestParam(required = false) MultipartFile thumbnail) {
        try {
            // Validate marker doesn't already exist
            if (markerService.markerExists(markerId)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Marker with ID '" + markerId + "' already exists"));
            }

            // Check if FileUploadService is configured
            if (!fileUploadService.isConfigured()) {
                logger.error("FileUploadService is not properly configured");
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(Map.of("error", "File upload service is not configured"));
            }

            // Determine category name for file organization
            String categoryName = "markers"; // Default folder name when no category
            if (categoryId != null && !categoryId.trim().isEmpty()) {
                var categoryOpt = categoryService.getCategoryById(categoryId.trim());
                if (categoryOpt.isPresent()) {
                    categoryName = categoryOpt.get().getName();
                } else {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Category not found: " + categoryId));
                }
            }

            // Upload files to Cloudflare R2
            String markerImageUrl;
            String videoUrl;
            String thumbnailUrl = null;

            try {
                markerImageUrl = fileUploadService.uploadFileByCategory(markerImage, categoryName, "markers");
                logger.info("Uploaded marker image for '{}': {}", markerId, markerImageUrl);
            } catch (Exception e) {
                logger.error("Failed to upload marker image for '{}'", markerId, e);
                return ResponseEntity.internalServerError()
                        .body(Map.of("error", "Failed to upload marker image: " + e.getMessage()));
            }

            try {
                videoUrl = fileUploadService.uploadFileByCategory(video, categoryName, "videos");
                logger.info("Uploaded video for '{}': {}", markerId, videoUrl);
            } catch (Exception e) {
                logger.error("Failed to upload video for '{}'", markerId, e);
                return ResponseEntity.internalServerError()
                        .body(Map.of("error", "Failed to upload video: " + e.getMessage()));
            }

            if (thumbnail != null) {
                try {
                    thumbnailUrl = fileUploadService.uploadFileByCategory(thumbnail, categoryName, "thumbnails");
                    logger.info("Uploaded thumbnail for '{}': {}", markerId, thumbnailUrl);
                } catch (Exception e) {
                    logger.warn("Failed to upload thumbnail for '{}', using marker image as fallback", markerId, e);
                    thumbnailUrl = markerImageUrl; // Use marker image as fallback
                }
            } else {
                thumbnailUrl = markerImageUrl; // Use marker image if no thumbnail provided
            }

            // Create marker entity
            Marker marker = new Marker();
            marker.setMarkerId(markerId.trim());
            marker.setName(name.trim());
            marker.setDescription(description != null ? description.trim() : "");
            marker.setPhysicalWidthMeters(physicalWidthMeters != null ? physicalWidthMeters : 0.1);
            marker.setMarkerImageUrl(markerImageUrl);
            marker.setThumbnailUrl(thumbnailUrl);
            marker.setCategoryId(categoryId != null && !categoryId.trim().isEmpty() ? categoryId.trim() : null);
            marker.setActive(true);

            // Create video entity
            Video videoEntity = new Video();
            videoEntity.setId(markerId + "-v1");
            videoEntity.setName(videoName != null ? videoName.trim() : "Default Video");
            videoEntity.setVideoUrl(videoUrl);
            videoEntity.setFormat("mp4");
            videoEntity.setDefault(true);
            videoEntity.setVariants(Collections.singletonList("1080p"));

            marker.setVideos(Collections.singletonList(videoEntity));
            marker.setActiveVideoId(videoEntity.getId());

            // Save marker to MongoDB
            Marker savedMarker = markerService.saveMarker(marker);
            logger.info("Successfully created marker '{}' with uploaded files", savedMarker.getMarkerId());

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Marker created successfully",
                    "marker", savedMarker));

        } catch (Exception e) {
            logger.error("Failed to upload and create marker: {}", markerId, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to create marker: " + e.getMessage()));
        }
    }

    @Operation(summary = "Create New Marker", description = "Create a new AR marker with associated video.\n\n" +
            "This endpoint handles the complete marker creation process:\n" +
            "1. Validate marker data\n" +
            "2. Create marker record in database\n\n" +
            "All URLs should be obtained from the /upload endpoint first.")
    @PostMapping("/markers")
    public ResponseEntity<?> createMarker(
            @Parameter(description = "Marker creation request") @Valid @RequestBody MarkerCreateRequest request) {
        try {
            if (request.getMarkerId() == null || request.getMarkerId().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Marker ID is required"));
            }

            if (markerService.markerExists(request.getMarkerId())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Marker with ID '" + request.getMarkerId() + "' already exists"));
            }

            if (request.getMarkerImageUrl() == null || request.getMarkerImageUrl().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Marker image URL is required"));
            }

            if (request.getVideoUrl() == null || request.getVideoUrl().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Video URL is required"));
            }

            Marker marker = new Marker();
            marker.setMarkerId(request.getMarkerId().trim());
            marker.setName(request.getName() != null ? request.getName().trim() : request.getMarkerId());
            marker.setDescription(request.getDescription() != null ? request.getDescription().trim() : "");
            marker.setPhysicalWidthMeters(
                    request.getPhysicalWidthMeters() != null ? request.getPhysicalWidthMeters() : 0.1);
            marker.setMarkerImageUrl(request.getMarkerImageUrl().trim());
            marker.setThumbnailUrl(request.getThumbnailUrl() != null ? request.getThumbnailUrl().trim()
                    : request.getMarkerImageUrl().trim());
            marker.setCategoryId(request.getCategoryId());
            marker.setActive(true);

            Video video = new Video();
            video.setId(request.getMarkerId() + "-v1");
            video.setName(request.getVideoName() != null ? request.getVideoName().trim() : "Default Video");
            video.setVideoUrl(request.getVideoUrl().trim());
            video.setFormat("mp4");
            video.setDefault(true);
            video.setVariants(Collections.singletonList("1080p"));

            marker.setVideos(Collections.singletonList(video));
            marker.setActiveVideoId(video.getId());

            Marker savedMarker = markerService.saveMarker(marker);
            logger.info("Created new marker: {}", savedMarker.getMarkerId());

            return ResponseEntity.status(HttpStatus.CREATED).body(savedMarker);

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request for marker creation: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to create marker: {}", request.getMarkerId(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to create marker: " + e.getMessage()));
        }
    }

    @Operation(summary = "Update Marker", description = "Update an existing marker's properties.\n\n" +
            "Can update name, description, active video, or add new videos.\n" +
            "Triggers AR database rebuild if marker image changes.")
    @PutMapping("/markers/{markerId}")
    public ResponseEntity<?> updateMarker(
            @Parameter(description = "Marker business ID") @PathVariable @NotBlank String markerId,

            @Parameter(description = "Update request") @Valid @RequestBody MarkerUpdateRequest request) {
        try {
            Optional<Marker> markerOpt = markerService.getMarkerWithSignedUrls(markerId);

            if (markerOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Marker marker = markerOpt.get();
            boolean hasChanges = false;

            if (request.getName() != null && !request.getName().trim().isEmpty()) {
                marker.setName(request.getName().trim());
                hasChanges = true;
            }
            if (request.getDescription() != null) {
                marker.setDescription(request.getDescription().trim());
                hasChanges = true;
            }
            if (request.getActiveVideoId() != null && !request.getActiveVideoId().trim().isEmpty()) {
                boolean videoExists = marker.getVideos() != null &&
                        marker.getVideos().stream()
                                .anyMatch(v -> v.getId().equals(request.getActiveVideoId()));

                if (videoExists) {
                    marker.setActiveVideoId(request.getActiveVideoId());
                    hasChanges = true;
                } else {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Video ID not found: " + request.getActiveVideoId()));
                }
            }

            if (!hasChanges) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "No valid updates provided"));
            }

            Marker updatedMarker = markerService.saveMarker(marker);
            logger.info("Updated marker: {}", markerId);

            return ResponseEntity.ok(updatedMarker);

        } catch (Exception e) {
            logger.error("Failed to update marker: {}", markerId, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to update marker: " + e.getMessage()));
        }
    }

    @Operation(summary = "Delete Marker", description = "Delete a marker from the database.")
    @DeleteMapping("/markers/{markerId}")
    public ResponseEntity<?> deleteMarker(
            @Parameter(description = "Marker business ID") @PathVariable @NotBlank String markerId) {
        try {
            boolean deleted = markerService.deleteMarker(markerId);

            if (deleted) {
                logger.info("Deleted marker: {}", markerId);
                return ResponseEntity.ok()
                        .body(Map.of("message", "Marker deleted successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Marker not found: " + markerId));
            }

        } catch (Exception e) {
            logger.error("Failed to delete marker: {}", markerId, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to delete marker: " + e.getMessage()));
        }
    }

}