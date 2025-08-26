package com.postkar.project3dmodel.service;

import com.postkar.project3dmodel.entity.Marker;
import com.postkar.project3dmodel.entity.Video;
import com.postkar.project3dmodel.repository.MarkerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MarkerService {

    @Autowired
    private MarkerRepository markerRepository;

    @Autowired
    private SignedUrlService signedUrlService;

    @Autowired
    private ArDatabaseService arDatabaseService;

    // Get all active markers (for app sync)
    public Page<Marker> getAllActiveMarkers(Pageable pageable) {
        return markerRepository.findByIsActiveTrue(pageable);
    }

    // Get markers updated since a timestamp (for incremental sync)
    public List<Marker> getMarkersUpdatedSince(LocalDateTime since) {
        return markerRepository.findByLastUpdatedAfter(since);
    }

    // Get single marker by markerId with signed URLs
    public Optional<Marker> getMarkerWithSignedUrls(String markerId) {
        Optional<Marker> markerOpt = markerRepository.findByMarkerId(markerId);

        if (markerOpt.isPresent()) {
            Marker marker = markerOpt.get();

            // Generate signed URLs for assets
            marker.setMarkerImageUrl(signedUrlService.generateSignedUrl(marker.getMarkerImageUrl()));
            marker.setThumbnailUrl(signedUrlService.generateSignedUrl(marker.getThumbnailUrl()));

            // Generate signed URLs for all videos
            if (marker.getVideos() != null) {
                marker.getVideos().forEach(video ->
                        video.setVideoUrl(signedUrlService.generateSignedUrl(video.getVideoUrl()))
                );
            }

            return Optional.of(marker);
        }

        return Optional.empty();
    }

    // Get active video for a marker
    public Optional<Video> getActiveVideo(String markerId) {
        Optional<Marker> markerOpt = markerRepository.findByMarkerId(markerId);

        if (markerOpt.isPresent()) {
            Marker marker = markerOpt.get();
            String activeVideoId = marker.getActiveVideoId();

            if (activeVideoId != null && marker.getVideos() != null) {
                Video video = marker.getVideos().stream()
                        .filter(v -> v.getId().equals(activeVideoId))
                        .findFirst()
                        .orElse(null);

                if (video != null) {
                    // Generate signed URL for video
                    video.setVideoUrl(signedUrlService.generateSignedUrl(video.getVideoUrl()));
                    return Optional.of(video);
                }
            }
        }

        return Optional.empty();
    }

    // Create or update marker (admin function)
    public Marker saveMarker(Marker marker) {
        marker.setLastUpdated(LocalDateTime.now());

        if (marker.getCreatedAt() == null) {
            marker.setCreatedAt(LocalDateTime.now());
        }

        Marker savedMarker = markerRepository.save(marker);

        // Trigger AR database rebuild
        arDatabaseService.triggerDatabaseRebuild();

        return savedMarker;
    }

    // Delete marker
    public boolean deleteMarker(String markerId) {
        Optional<Marker> markerOpt = markerRepository.findByMarkerId(markerId);

        if (markerOpt.isPresent()) {
            markerRepository.delete(markerOpt.get());
            // Trigger AR database rebuild
            arDatabaseService.triggerDatabaseRebuild();
            return true;
        }

        return false;
    }

    // Check if markerId exists
    public boolean markerExists(String markerId) {
        return markerRepository.existsByMarkerId(markerId);
    }

    // Get markers by category ID
    public List<Marker> getMarkersByCategory(String categoryId) {
        List<Marker> markers = markerRepository.findByCategoryIdAndIsActiveTrue(categoryId);
        
        // Generate signed URLs for all markers
        markers.forEach(marker -> {
            marker.setMarkerImageUrl(signedUrlService.generateSignedUrl(marker.getMarkerImageUrl()));
            marker.setThumbnailUrl(signedUrlService.generateSignedUrl(marker.getThumbnailUrl()));
            
            // Generate signed URLs for all videos
            if (marker.getVideos() != null) {
                marker.getVideos().forEach(video ->
                        video.setVideoUrl(signedUrlService.generateSignedUrl(video.getVideoUrl()))
                );
            }
        });
        
        return markers;
    }

}

