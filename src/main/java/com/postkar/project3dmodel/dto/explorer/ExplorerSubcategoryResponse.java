package com.postkar.project3dmodel.dto.explorer;

import lombok.Data;

@Data
public class ExplorerSubcategoryResponse {
    private String id;
    private String markerId;
    private String name;
    private String description;
    private String thumbnailUrl;
    private String categoryId;
    private String categoryName;
}
