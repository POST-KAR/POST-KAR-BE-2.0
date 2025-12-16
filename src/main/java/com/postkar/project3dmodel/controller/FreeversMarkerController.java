package com.postkar.project3dmodel.controller;

import com.postkar.project3dmodel.entity.FreeversMarker;
import com.postkar.project3dmodel.entity.Video;
import com.postkar.project3dmodel.service.FreeversMarkerService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/freeverse-markers")
@Tag(name = "Freeverse Markers", description = "Public APIs for freeverse AR markers (no authentication required)")
public class FreeversMarkerController {

    @Autowired
    private FreeversMarkerService freeversMarkerService;

    @Operation(summary = "Get Current Version", description = "Get the current freeverse marker version info.\\n\\n" +
            "Response includes:\\n" +
            "- version: Current version string\\n" +
            "- lastUpdated: When markers were last updated\\n" +
            "- totalMarkers: Number of active freeverse markers")
    @GetMapping("/version")
    public ResponseEntity<Map<String, Object>> getCurrentVersion() {
        List<FreeversMarker> markers = freeversMarkerService.getAllActiveFreeversMarkers(Pageable.unpaged())
                .getContent();

        Map<String, Object> response = new HashMap<>();
        response.put("version", "v1");
        response.put("lastUpdated", LocalDateTime.now());
        response.put("totalMarkers", markers.size());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get All Active Freeverse Markers", description = "Get paginated list of all active freeverse markers.\\n\\n"
            +
            "Each marker includes:\\n" +
            "- markerId: Business identifier\\n" +
            "- name, description: Human readable info\\n" +
            "- markerImageUrl: Signed URL for AR image\\n" +
            "- thumbnailUrl: Signed URL for preview\\n" +
            "- videos: List of associated videos\\n" +
            "- activeVideoId: Currently active video\\n\\n" +
            "Pagination example: ?page=0&size=10&sort=name,asc")
    @GetMapping
    public ResponseEntity<Page<FreeversMarker>> getAllActiveFreeversMarkers(
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        return ResponseEntity.ok(freeversMarkerService.getAllActiveFreeversMarkers(pageable));
    }

    @Operation(summary = "Get Freeverse Markers Updated Since", description = "Get list of freeverse markers updated after a specific timestamp.\\n\\n"
            +
            "Used for incremental sync - request only markers that changed\\n" +
            "since last update to minimize data transfer.\\n\\n" +
            "Timestamp format: yyyy-MM-dd'T'HH:mm:ss (e.g., 2025-08-19T10:30:00)")
    @GetMapping("/updated-since")
    public ResponseEntity<List<FreeversMarker>> getFreeversMarkersUpdatedSince(
            @Parameter(description = "Only return markers updated after this timestamp", example = "2025-08-19T10:30:00") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {
        return ResponseEntity.ok(freeversMarkerService.getFreeversMarkersUpdatedSince(since));
    }

    @Operation(summary = "Get Single Freeverse Marker", description = "Get detailed information for a specific freeverse marker.\\n\\n"
            +
            "Returns marker with signed URLs for all assets (valid for 24 hours).\\n" +
            "Includes all associated videos and metadata.")
    @GetMapping("/{markerId}")
    public ResponseEntity<FreeversMarker> getFreeversMarker(
            @Parameter(description = "Marker business ID", example = "FREEVERSE_001") @PathVariable String markerId) {
        return freeversMarkerService.getFreeversMarkerWithSignedUrls(markerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get Active Video", description = "Get the currently active video for a specific freeverse marker.\\n\\n"
            +
            "This is the main endpoint apps call when a freeverse marker is detected.\\n" +
            "Returns signed video URL (valid for 24 hours) and video metadata.\\n\\n" +
            "Response includes:\\n" +
            "- videoUrl: Signed URL for video playback\\n" +
            "- variants: Available quality levels\\n" +
            "- format: Video format (mp4, m3u8)\\n" +
            "- duration: Video length in seconds")
    @GetMapping("/{markerId}/activeVideo")
    public ResponseEntity<Video> getActiveFreeversVideo(
            @Parameter(description = "Marker business ID", example = "FREEVERSE_001") @PathVariable String markerId) {
        Optional<Video> videoOpt = freeversMarkerService.getActiveFreeversVideo(markerId);

        return videoOpt.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Check Freeverse Marker Exists", description = "Quick check if a freeverse marker ID exists and is active.\\n\\n"
            +
            "Returns simple boolean response. Useful for validation.")
    @GetMapping("/{markerId}/exists")
    public ResponseEntity<Boolean> checkFreeversMarkerExists(
            @Parameter(description = "Marker business ID", example = "FREEVERSE_001") @PathVariable String markerId) {
        return ResponseEntity.ok(freeversMarkerService.freeversMarkerExists(markerId));
    }

    @Operation(summary = "Get Freeverse Markers by Category", description = "Get all active freeverse markers for a specific category.\\n\\n"
            +
            "Returns list of markers with signed URLs for all assets (valid for 24 hours).\\n" +
            "Each marker includes:\\n" +
            "- markerId: Business identifier\\n" +
            "- name, description: Human readable info\\n" +
            "- markerImageUrl: Signed URL for AR image\\n" +
            "- thumbnailUrl: Signed URL for preview\\n" +
            "- videos: List of associated videos with signed URLs")
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<FreeversMarker>> getFreeversMarkersByCategory(
            @Parameter(description = "Category ID", example = "64f1b2c3d4e5f6789abcdef0") @PathVariable String categoryId) {
        List<FreeversMarker> markers = freeversMarkerService.getFreeversMarkersByCategory(categoryId);
        return ResponseEntity.ok(markers);
    }

    @Operation(summary = "Search Freeverse Markers", description = "Search freeverse markers by name or description.\\n\\n"
            +
            "Returns list of matching active markers with signed URLs.")
    @GetMapping("/search")
    public ResponseEntity<List<FreeversMarker>> searchFreeversMarkers(
            @Parameter(description = "Search query", example = "nature") @RequestParam String q) {
        List<FreeversMarker> markers = freeversMarkerService.searchFreeversMarkers(q);
        return ResponseEntity.ok(markers);
    }

}
