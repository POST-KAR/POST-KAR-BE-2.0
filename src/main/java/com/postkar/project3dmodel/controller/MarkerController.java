package com.postkar.project3dmodel.controller;

import com.postkar.project3dmodel.entity.Marker;
import com.postkar.project3dmodel.entity.MarkerVersion;
import com.postkar.project3dmodel.entity.Video;
import com.postkar.project3dmodel.response.MarkerVersionResponse;
import com.postkar.project3dmodel.service.ArDatabaseService;
import com.postkar.project3dmodel.service.MarkerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/markers")
@Tag(name = "AR Markers", description = "APIs for AR marker management and detection")
public class MarkerController {

    @Autowired
    private MarkerService markerService;

    @Autowired
    private ArDatabaseService arDatabaseService;

    @Operation(
            summary = "Get Current Version",
            description = "Get the current marker database version info. App uses this to check for updates.\n\n" +
                    "Response includes:\n" +
                    "- version: Current version string (e.g., 'v2')\n" +
                    "- imgdbUrl: Android .imgdb file URL\n" +
                    "- lastUpdated: When this version was published\n" +
                    "- totalMarkers: Number of markers in this version"
    )
    @GetMapping("/version")
    public ResponseEntity<MarkerVersionResponse> getCurrentVersion() {
        Optional<MarkerVersion> versionOpt = arDatabaseService.getCurrentVersion();

        if (versionOpt.isPresent()) {
            MarkerVersion version = versionOpt.get();

            // Get database info
            return arDatabaseService.getCurrentDatabase()
                    .map(db -> {
                        MarkerVersionResponse response = new MarkerVersionResponse();
                        response.setVersion(version.getVersion());
                        response.setImgdbUrl(db.getImgdbUrl());
                        response.setImgdbChecksum(db.getImgdbChecksum());
                        response.setLastUpdated(version.getLastUpdated());
                        response.setTotalMarkers(version.getTotalMarkers());
                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.notFound().build());
        }

        return ResponseEntity.notFound().build();
    }

    @Operation(
            summary = "Get All Active Markers",
            description = "Get paginated list of all active markers with metadata.\n\n" +
                    "Each marker includes:\n" +
                    "- markerId: Business identifier (A, B, C, etc.)\n" +
                    "- name, description: Human readable info\n" +
                    "- markerImageUrl: Signed URL for iOS ARReferenceImage creation\n" +
                    "- thumbnailUrl: Signed URL for app UI preview\n" +
                    "- videos: List of associated videos\n" +
                    "- activeVideoId: Currently active video\n\n" +
                    "Pagination example: ?page=0&size=10&sort=name,asc"
    )
    @GetMapping
    public ResponseEntity<Page<Marker>> getAllActiveMarkers(
            @Parameter(description = "Pagination parameters")
            Pageable pageable
    ) {
        return ResponseEntity.ok(markerService.getAllActiveMarkers(pageable));
    }

    @Operation(
            summary = "Get Markers Updated Since",
            description = "Get list of markers updated after a specific timestamp.\n\n" +
                    "Used for incremental sync - app can request only markers that changed\n" +
                    "since its last update to minimize data transfer.\n\n" +
                    "Timestamp format: yyyy-MM-dd'T'HH:mm:ss (e.g., 2025-08-19T10:30:00)"
    )
    @GetMapping("/updated-since")
    public ResponseEntity<List<Marker>> getMarkersUpdatedSince(
            @Parameter(description = "Only return markers updated after this timestamp", example = "2025-08-19T10:30:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime since
    ) {
        return ResponseEntity.ok(markerService.getMarkersUpdatedSince(since));
    }

    @Operation(
            summary = "Get Single Marker",
            description = "Get detailed information for a specific marker by its markerId.\n\n" +
                    "Returns marker with signed URLs for all assets (valid for 24 hours).\n" +
                    "Includes all associated videos and metadata."
    )
    @GetMapping("/{markerId}")
    public ResponseEntity<Marker> getMarker(
            @Parameter(description = "Marker business ID", example = "A")
            @PathVariable String markerId
    ) {
        return markerService.getMarkerWithSignedUrls(markerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Get Active Video",
            description = "Get the currently active video for a specific marker.\n\n" +
                    "This is the main endpoint apps call when a marker is detected.\n" +
                    "Returns signed video URL (valid for 24 hours) and video metadata.\n\n" +
                    "Response includes:\n" +
                    "- videoUrl: Signed URL for video playback\n" +
                    "- variants: Available quality levels\n" +
                    "- format: Video format (mp4, m3u8)\n" +
                    "- duration: Video length in seconds"
    )
    @GetMapping("/{markerId}/activeVideo")
    public ResponseEntity<Video> getActiveVideo(
            @Parameter(description = "Marker business ID", example = "A")
            @PathVariable String markerId
    ) {
        Optional<Video> videoOpt = markerService.getActiveVideo(markerId);

        return videoOpt.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Check Marker Exists",
            description = "Quick check if a marker ID exists and is active.\n\n" +
                    "Returns simple boolean response. Useful for validation."
    )
    @GetMapping("/{markerId}/exists")
    public ResponseEntity<Boolean> checkMarkerExists(
            @Parameter(description = "Marker business ID", example = "A")
            @PathVariable String markerId
    ) {
        return ResponseEntity.ok(markerService.markerExists(markerId));
    }

    @Operation(
            summary = "Get Markers by Category",
            description = "Get all active markers for a specific category.\n\n" +
                    "This is the main endpoint for fetching markers within a category.\n" +
                    "Returns list of markers with signed URLs for all assets (valid for 24 hours).\n" +
                    "Each marker includes:\n" +
                    "- markerId: Business identifier for scanning\n" +
                    "- name, description: Human readable info\n" +
                    "- markerImageUrl: Signed URL for iOS ARReferenceImage creation\n" +
                    "- thumbnailUrl: Signed URL for app UI preview\n" +
                    "- videos: List of associated videos with signed URLs"
    )
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Marker>> getMarkersByCategory(
            @Parameter(description = "Category ID", example = "64f1b2c3d4e5f6789abcdef0")
            @PathVariable String categoryId
    ) {
        List<Marker> markers = markerService.getMarkersByCategory(categoryId);
        return ResponseEntity.ok(markers);
    }

}
