package com.postkar.project3dmodel.entity;

import com.mongodb.lang.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "markers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Marker {
    @Id
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

    private LocalDateTime createdAt;
    private LocalDateTime lastUpdated;
    private boolean isActive;

    @Nullable
    private String notes;
}