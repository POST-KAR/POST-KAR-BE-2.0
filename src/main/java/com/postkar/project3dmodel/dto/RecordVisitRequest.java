package com.postkar.project3dmodel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecordVisitRequest {
    private String sessionId;
    private String timestamp;
}
