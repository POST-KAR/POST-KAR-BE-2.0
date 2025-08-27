package com.postkar.project3dmodel.response;

import lombok.Data;

@Data
public class CategoryResponse {
    private String id;
    private String name;
    private String description;
    private int markerCount;
}
