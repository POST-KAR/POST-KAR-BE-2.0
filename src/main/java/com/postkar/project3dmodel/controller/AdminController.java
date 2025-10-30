package com.postkar.project3dmodel.controller;

import com.postkar.project3dmodel.dto.MarkerCreateRequest;
import com.postkar.project3dmodel.dto.MarkerUpdateRequest;
import com.postkar.project3dmodel.entity.ArDatabase;
import com.postkar.project3dmodel.entity.Marker;
import com.postkar.project3dmodel.entity.Video;
import com.postkar.project3dmodel.response.UploadResponse;
import com.postkar.project3dmodel.service.ArDatabaseService;
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
    private ArDatabaseService arDatabaseService;

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private CategoryService categoryService;

    @Operation(
            summary = "Upload Files",
            description = "Upload marker image, thumbnail, and video files to Cloudflare R2.\n\n" +
                    "Returns actual R2 URLs that can be used in marker creation requests.\n" +
                    "All files are uploaded to cloud storage with proper validation.\n" +
                    "Files are organized by category: {categoryName}/{fileType}/{filename}"
    )
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFiles(
            @Parameter(description = "Category ID for organizing files")
            @RequestParam(required = false) String categoryId,

            @Parameter(description = "Marker image file (PNG/JPG, max 5MB)")
            @RequestParam(required = false) MultipartFile markerImage,

            @Parameter(description = "Thumbnail image file (PNG/JPG, max 5MB)")
            @RequestParam(required = false) MultipartFile thumbnail,

            @Parameter(description = "Video file (MP4, max 500MB)")
            @RequestParam(required = false) MultipartFile video
    ) {
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

    @Operation(
            summary = "Create New Marker",
            description = "Create a new AR marker with associated video.\n\n" +
                    "This endpoint handles the complete marker creation process:\n" +
                    "1. Validate marker data\n" +
                    "2. Create marker record in database\n" +
                    "3. Trigger AR database rebuild automatically\n\n" +
                    "All URLs should be obtained from the /upload endpoint first."
    )
    @PostMapping("/markers")
    public ResponseEntity<?> createMarker(
            @Parameter(description = "Marker creation request")
            @Valid @RequestBody MarkerCreateRequest request
    ) {
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
            marker.setPhysicalWidthMeters(request.getPhysicalWidthMeters() != null ?
                    request.getPhysicalWidthMeters() : 0.1);
            marker.setMarkerImageUrl(request.getMarkerImageUrl().trim());
            marker.setThumbnailUrl(request.getThumbnailUrl() != null ?
                    request.getThumbnailUrl().trim() : request.getMarkerImageUrl().trim());
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

    @Operation(
            summary = "Update Marker",
            description = "Update an existing marker's properties.\n\n" +
                    "Can update name, description, active video, or add new videos.\n" +
                    "Triggers AR database rebuild if marker image changes."
    )
    @PutMapping("/markers/{markerId}")
    public ResponseEntity<?> updateMarker(
            @Parameter(description = "Marker business ID")
            @PathVariable @NotBlank String markerId,

            @Parameter(description = "Update request")
            @Valid @RequestBody MarkerUpdateRequest request
    ) {
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

    @Operation(
            summary = "Delete Marker",
            description = "Delete a marker and trigger database rebuild.\n\n" +
                    "This will remove the marker from AR detection and rebuild\n" +
                    "the .imgdb file without this marker."
    )
    @DeleteMapping("/markers/{markerId}")
    public ResponseEntity<?> deleteMarker(
            @Parameter(description = "Marker business ID")
            @PathVariable @NotBlank String markerId
    ) {
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

    @Operation(
            summary = "Trigger Database Rebuild",
            description = "Manually trigger AR database (.imgdb) rebuild.\n\n" +
                    "Normally happens automatically when markers are added/updated,\n" +
                    "but this allows manual triggering for maintenance."
    )
    @PostMapping("/rebuild-database")
    public ResponseEntity<?> triggerDatabaseRebuild() {
        try {
            arDatabaseService.triggerDatabaseRebuild();
            logger.info("Database rebuild triggered manually");

            return ResponseEntity.ok()
                    .body(Map.of("message", "Database rebuild triggered successfully"));

        } catch (Exception e) {
            logger.error("Failed to trigger database rebuild", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to trigger rebuild: " + e.getMessage()));
        }
    }

    @Operation(
            summary = "Get Database Build Status",
            description = "Check the status of the latest database build.\n\n" +
                    "Returns build status: 'building', 'ready', or 'failed'\n" +
                    "and build log for debugging."
    )
    @GetMapping("/database-status")
    public ResponseEntity<?> getDatabaseStatus() {
        try {
            Optional<ArDatabase> dbOpt = arDatabaseService.getCurrentDatabase();

            if (dbOpt.isPresent()) {
                return ResponseEntity.ok(dbOpt.get());
            } else {
                return ResponseEntity.ok()
                        .body(Map.of("message", "No database build found"));
            }

        } catch (Exception e) {
            logger.error("Failed to get database status", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to get database status: " + e.getMessage()));
        }
    }

    @Operation(
            summary = "Get All Database Builds",
            description = "Get list of all database builds with their status"
    )
    @GetMapping("/database-builds")
    public ResponseEntity<?> getAllDatabaseBuilds() {
        try {
            return ResponseEntity.ok()
                    .body(Map.of("message", "Feature not implemented yet"));

        } catch (Exception e) {
            logger.error("Failed to get database builds", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to get database builds: " + e.getMessage()));
        }
    }

    @Operation(
            summary = "Publish Database",
            description = "Publish a specific database build to make it active"
    )
    @PostMapping("/database/{databaseId}/publish")
    public ResponseEntity<?> publishDatabase(
            @Parameter(description = "Database ID to publish")
            @PathVariable @NotBlank String databaseId
    ) {
        try {
            arDatabaseService.publishDatabase(databaseId);
            logger.info("Published database: {}", databaseId);

            return ResponseEntity.ok()
                    .body(Map.of("message", "Database published successfully"));

        } catch (Exception e) {
            logger.error("Failed to publish database: {}", databaseId, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to publish database: " + e.getMessage()));
        }
    }
}