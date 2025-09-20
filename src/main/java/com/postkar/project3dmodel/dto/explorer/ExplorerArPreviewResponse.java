package com.postkar.project3dmodel.dto.explorer;

import lombok.Data;

@Data
public class ExplorerArPreviewResponse {
    private String markerId;
    private String arAssetUrl;
    private String instructions;
    private String fallbackPreviewUrl;
    private String markerImageUrl;
    private Double physicalWidthMeters;
}
