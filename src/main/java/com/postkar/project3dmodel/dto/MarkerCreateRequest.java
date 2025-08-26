package com.postkar.project3dmodel.dto;

import com.mongodb.lang.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class MarkerCreateRequest {
    @NotBlank(message = "Marker ID is required")
    private String markerId;

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @Nullable
    private Double physicalWidthMeters;

    @NotBlank(message = "Marker image URL is required")
    private String markerImageUrl;

    private String thumbnailUrl;

    private String categoryId;

    @NotBlank(message = "Video URL is required")
    private String videoUrl;

    private String videoName;
}
