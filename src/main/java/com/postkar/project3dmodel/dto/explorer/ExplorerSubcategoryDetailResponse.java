package com.postkar.project3dmodel.dto.explorer;

import lombok.Data;

@Data
public class ExplorerSubcategoryDetailResponse {
    private String id;
    private String markerId;
    private String title;
    private String description;
    private String thumbnailUrl;
    private String mediaPreviewUrl;
    private String arAssetUrl;
    private String triggerMarkerUrl;
    private String categoryId;
    private String categoryName;
    private boolean isActive;
}
