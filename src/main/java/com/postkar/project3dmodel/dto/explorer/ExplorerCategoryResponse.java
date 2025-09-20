package com.postkar.project3dmodel.dto.explorer;

import lombok.Data;

@Data
public class ExplorerCategoryResponse {
    private String id;
    private String name;
    private String description;
    private String thumbnailUrl;
    private int itemCount;
    private boolean isFeatured;
    private String status; // "available" or "coming_soon"
}
