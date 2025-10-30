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

    public Page<Marker> getAllActiveMarkers(Pageable pageable) {
        return markerRepository.findByIsActiveTrue(pageable);
    }

    public List<Marker> getMarkersUpdatedSince(LocalDateTime since) {
        return markerRepository.findByLastUpdatedAfter(since);
    }

    public Optional<Marker> getMarkerWithSignedUrls(String markerId) {
        Optional<Marker> markerOpt = markerRepository.findByMarkerId(markerId);

        if (markerOpt.isPresent()) {
            Marker marker = markerOpt.get();

            marker.setMarkerImageUrl(signedUrlService.generateSignedUrl(marker.getMarkerImageUrl()));
            marker.setThumbnailUrl(signedUrlService.generateSignedUrl(marker.getThumbnailUrl()));

            if (marker.getVideos() != null) {
                marker.getVideos().forEach(video ->
                        video.setVideoUrl(signedUrlService.generateSignedUrl(video.getVideoUrl()))
                );
            }

            return Optional.of(marker);
        }

        return Optional.empty();
    }

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
                    video.setVideoUrl(signedUrlService.generateSignedUrl(video.getVideoUrl()));
                    return Optional.of(video);
                }
            }
        }

        return Optional.empty();
    }

    public Marker saveMarker(Marker marker) {
        marker.setLastUpdated(LocalDateTime.now());

        if (marker.getCreatedAt() == null) {
            marker.setCreatedAt(LocalDateTime.now());
        }

        Marker savedMarker = markerRepository.save(marker);


        arDatabaseService.triggerDatabaseRebuild();

        return savedMarker;
    }

    public boolean deleteMarker(String markerId) {
        Optional<Marker> markerOpt = markerRepository.findByMarkerId(markerId);

        if (markerOpt.isPresent()) {
            markerRepository.delete(markerOpt.get());
            arDatabaseService.triggerDatabaseRebuild();
            return true;
        }

        return false;
    }

    public boolean markerExists(String markerId) {
        return markerRepository.existsByMarkerId(markerId);
    }

    public List<Marker> getMarkersByCategory(String categoryId) {
        List<Marker> markers = markerRepository.findByCategoryIdAndIsActiveTrue(categoryId);

        markers.forEach(marker -> {
            marker.setMarkerImageUrl(signedUrlService.generateSignedUrl(marker.getMarkerImageUrl()));
            marker.setThumbnailUrl(signedUrlService.generateSignedUrl(marker.getThumbnailUrl()));

            if (marker.getVideos() != null) {
                marker.getVideos().forEach(video ->
                        video.setVideoUrl(signedUrlService.generateSignedUrl(video.getVideoUrl()))
                );
            }
        });
        
        return markers;
    }

    public List<Marker> searchMarkers(String query) {
        List<Marker> markers = markerRepository.findByIsActiveTrue().stream()
                .filter(marker ->
                    marker.getName().toLowerCase().contains(query.toLowerCase()) ||
                    marker.getDescription().toLowerCase().contains(query.toLowerCase())
                )
                .toList();

        // Apply signed URLs to search results
        markers.forEach(marker -> {
            marker.setMarkerImageUrl(signedUrlService.generateSignedUrl(marker.getMarkerImageUrl()));
            marker.setThumbnailUrl(signedUrlService.generateSignedUrl(marker.getThumbnailUrl()));

            if (marker.getVideos() != null) {
                marker.getVideos().forEach(video ->
                        video.setVideoUrl(signedUrlService.generateSignedUrl(video.getVideoUrl()))
                );
            }
        });
        
        return markers;
    }

}

