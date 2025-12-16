package com.postkar.project3dmodel.service;

import com.postkar.project3dmodel.entity.FreeversMarker;
import com.postkar.project3dmodel.entity.Video;
import com.postkar.project3dmodel.repository.FreeversMarkerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FreeversMarkerService {

    @Autowired
    private FreeversMarkerRepository freeversMarkerRepository;

    @Autowired
    private SignedUrlService signedUrlService;

    public Page<FreeversMarker> getAllActiveFreeversMarkers(Pageable pageable) {
        return freeversMarkerRepository.findByIsActiveTrue(pageable);
    }

    public List<FreeversMarker> getFreeversMarkersUpdatedSince(LocalDateTime since) {
        return freeversMarkerRepository.findByLastUpdatedAfter(since);
    }

    public Optional<FreeversMarker> getFreeversMarkerWithSignedUrls(String markerId) {
        Optional<FreeversMarker> markerOpt = freeversMarkerRepository.findByMarkerId(markerId);

        if (markerOpt.isPresent()) {
            FreeversMarker marker = markerOpt.get();

            marker.setMarkerImageUrl(signedUrlService.generateSignedUrl(marker.getMarkerImageUrl()));
            marker.setThumbnailUrl(signedUrlService.generateSignedUrl(marker.getThumbnailUrl()));

            if (marker.getVideos() != null) {
                marker.getVideos()
                        .forEach(video -> video.setVideoUrl(signedUrlService.generateSignedUrl(video.getVideoUrl())));
            }

            return Optional.of(marker);
        }

        return Optional.empty();
    }

    public Optional<Video> getActiveFreeversVideo(String markerId) {
        Optional<FreeversMarker> markerOpt = freeversMarkerRepository.findByMarkerId(markerId);

        if (markerOpt.isPresent()) {
            FreeversMarker marker = markerOpt.get();
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

    public FreeversMarker saveFreeversMarker(FreeversMarker marker) {
        marker.setLastUpdated(LocalDateTime.now());

        if (marker.getCreatedAt() == null) {
            marker.setCreatedAt(LocalDateTime.now());
        }

        FreeversMarker savedMarker = freeversMarkerRepository.save(marker);
        return savedMarker;
    }

    public boolean deleteFreeversMarker(String markerId) {
        Optional<FreeversMarker> markerOpt = freeversMarkerRepository.findByMarkerId(markerId);

        if (markerOpt.isPresent()) {
            freeversMarkerRepository.delete(markerOpt.get());
            return true;
        }

        return false;
    }

    public boolean freeversMarkerExists(String markerId) {
        return freeversMarkerRepository.existsByMarkerId(markerId);
    }

    public List<FreeversMarker> getFreeversMarkersByCategory(String categoryId) {
        List<FreeversMarker> markers = freeversMarkerRepository.findByCategoryIdAndIsActiveTrue(categoryId);

        markers.forEach(marker -> {
            marker.setMarkerImageUrl(signedUrlService.generateSignedUrl(marker.getMarkerImageUrl()));
            marker.setThumbnailUrl(signedUrlService.generateSignedUrl(marker.getThumbnailUrl()));

            if (marker.getVideos() != null) {
                marker.getVideos()
                        .forEach(video -> video.setVideoUrl(signedUrlService.generateSignedUrl(video.getVideoUrl())));
            }
        });

        return markers;
    }

    public List<FreeversMarker> searchFreeversMarkers(String query) {
        List<FreeversMarker> markers = freeversMarkerRepository.findByIsActiveTrue().stream()
                .filter(marker -> marker.getName().toLowerCase().contains(query.toLowerCase()) ||
                        marker.getDescription().toLowerCase().contains(query.toLowerCase()))
                .toList();

        // Apply signed URLs to search results
        markers.forEach(marker -> {
            marker.setMarkerImageUrl(signedUrlService.generateSignedUrl(marker.getMarkerImageUrl()));
            marker.setThumbnailUrl(signedUrlService.generateSignedUrl(marker.getThumbnailUrl()));

            if (marker.getVideos() != null) {
                marker.getVideos()
                        .forEach(video -> video.setVideoUrl(signedUrlService.generateSignedUrl(video.getVideoUrl())));
            }
        });

        return markers;
    }

}
