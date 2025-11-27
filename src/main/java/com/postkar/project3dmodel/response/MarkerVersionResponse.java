package com.postkar.project3dmodel.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MarkerVersionResponse {
    private String version;
    private LocalDateTime lastUpdated;
    private Integer totalMarkers;
}
