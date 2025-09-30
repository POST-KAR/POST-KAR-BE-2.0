package com.postkar.project3dmodel.dto.explorer;

import lombok.Data;
import java.util.List;

@Data
public class ExplorerSearchResponse {
    private String query;
    private int totalResults;
    private List<ExplorerCategoryResponse> categories;
    private List<ExplorerSubcategoryResponse> subcategories;
}
