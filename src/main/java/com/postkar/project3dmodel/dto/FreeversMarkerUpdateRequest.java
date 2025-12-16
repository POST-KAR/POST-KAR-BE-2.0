package com.postkar.project3dmodel.dto;

import lombok.Data;

@Data
public class FreeversMarkerUpdateRequest {
    private String name;
    private String description;
    private String activeVideoId;
    private Boolean isActive;
    private String markerImageUrl;
    private String categoryId;
}
