package com.postkar.project3dmodel.response;

import lombok.Data;
import java.util.List;

@Data
public class CategoryWithMarkersResponse {
    private String id;
    private String name;
    private String description;
    private List<MarkerSummaryResponse> markers;
}
