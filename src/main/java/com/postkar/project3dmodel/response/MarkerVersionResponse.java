package com.postkar.project3dmodel.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MarkerVersionResponse {
    private String version;
    private String imgdbUrl;
    private String imgdbChecksum;
    private LocalDateTime lastUpdated;
    private Integer totalMarkers;
}
