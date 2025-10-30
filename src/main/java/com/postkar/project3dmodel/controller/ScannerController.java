package com.postkar.project3dmodel.controller;

import com.postkar.project3dmodel.entity.Marker;
import com.postkar.project3dmodel.entity.Video;
import com.postkar.project3dmodel.service.MarkerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/scanner-api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Scanner", description = "AR scanner APIs for web integration")
public class ScannerController {
    
    private final MarkerService markerService;
    
    @Operation(
            summary = "Detect AR Content by Marker ID",
            description = "Get AR content for a specific marker ID. This simulates image detection for web AR integration."
    )
    @PostMapping("/detect")
    public ResponseEntity<Map<String, Object>> detectByMarkerId(
            @RequestBody Map<String, String> request
    ) {
        String markerId = request.get("markerId");
        log.info("AR detection request for marker: {}", markerId);
        
        if (markerId == null || markerId.trim().isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Marker ID is required");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        try {
            Optional<Marker> markerOpt = markerService.getMarkerWithSignedUrls(markerId);
            
            if (markerOpt.isPresent()) {
                Marker marker = markerOpt.get();
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "AR content found");
                
                Map<String, Object> arData = new HashMap<>();
                arData.put("markerId", marker.getMarkerId());
                arData.put("name", marker.getName());
                arData.put("description", marker.getDescription());
                arData.put("thumbnailUrl", marker.getThumbnailUrl());
                arData.put("markerImageUrl", marker.getMarkerImageUrl());
                arData.put("physicalWidthMeters", marker.getPhysicalWidthMeters());
                
                // Get active video if available
                if (marker.getActiveVideoId() != null && marker.getVideos() != null) {
                    Video activeVideo = marker.getVideos().stream()
                            .filter(v -> v.getId().equals(marker.getActiveVideoId()))
                            .findFirst()
                            .orElse(null);
                    
                    if (activeVideo != null) {
                        arData.put("videoUrl", activeVideo.getVideoUrl());
                        arData.put("videoDuration", activeVideo.getDurationSeconds());
                        arData.put("videoFormat", activeVideo.getFormat());
                    }
                }
                
                response.put("data", arData);
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "No AR content found for this marker");
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            log.error("Error detecting AR content", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to detect AR content");
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    @Operation(
            summary = "Get AR Media by Marker ID",
            description = "Get AR media content (video/3D model) for a specific marker."
    )
    @GetMapping("/media/{markerId}")
    public ResponseEntity<Map<String, Object>> getArMedia(
            @Parameter(description = "Marker ID", example = "A01")
            @PathVariable String markerId
    ) {
        log.info("Getting AR media for marker: {}", markerId);
        
        try {
            Optional<Video> videoOpt = markerService.getActiveVideo(markerId);
            
            if (videoOpt.isPresent()) {
                Video video = videoOpt.get();
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                
                Map<String, Object> mediaData = new HashMap<>();
                mediaData.put("id", video.getId());
                mediaData.put("name", video.getName());
                mediaData.put("videoUrl", video.getVideoUrl());
                mediaData.put("format", video.getFormat());
                mediaData.put("durationSeconds", video.getDurationSeconds());
                mediaData.put("fileSizeBytes", video.getFileSizeBytes());
                mediaData.put("width", video.getWidth());
                mediaData.put("height", video.getHeight());
                
                response.put("data", mediaData);
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "AR media not found for marker: " + markerId);
                
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error getting AR media", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to get AR media");
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    @Operation(
            summary = "Get All Available Markers for Scanning",
            description = "Get list of all active markers available for AR scanning."
    )
    @GetMapping("/markers")
    public ResponseEntity<Map<String, Object>> getAvailableMarkers() {
        log.info("Getting all available markers for scanning");
        
        try {
            // This could be enhanced to return a simplified list for scanning purposes
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Use /api/markers endpoint for full marker list");
            response.put("endpoint", "/api/markers");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting available markers", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to get available markers");
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
