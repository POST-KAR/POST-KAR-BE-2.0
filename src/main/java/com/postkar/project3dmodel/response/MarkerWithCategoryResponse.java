package com.postkar.project3dmodel.response;

import com.postkar.project3dmodel.entity.Video;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MarkerWithCategoryResponse {
    private String id;
    private String markerId;
    private String name;
    private String description;
    private Double physicalWidthMeters;
    private String markerImageUrl;
    private String thumbnailUrl;
    private String markerChecksum;
    private List<Video> videos;
    private String activeVideoId;
    private String categoryId;
    private String categoryName;
    private String categoryDescription;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdated;
    private boolean isActive;
    private String notes;
}
