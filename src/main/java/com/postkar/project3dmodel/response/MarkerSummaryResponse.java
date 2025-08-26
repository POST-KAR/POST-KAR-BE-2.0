package com.postkar.project3dmodel.response;

import lombok.Data;

@Data
public class MarkerSummaryResponse {
    private String id;
    private String markerId;
    private String name;
    private String description;
    private String thumbnailUrl;
    private boolean isActive;
}
