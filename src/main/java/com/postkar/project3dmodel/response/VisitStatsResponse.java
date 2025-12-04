package com.postkar.project3dmodel.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VisitStatsResponse {
    private Long totalVisits;
    private Long activeNow;
    private String lastUpdated;
}
