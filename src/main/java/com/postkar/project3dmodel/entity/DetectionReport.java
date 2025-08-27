package com.postkar.project3dmodel.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "detection_reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetectionReport {
    @Id
    private String id;
    
    private String markerId;
    private String deviceId;
    private LocalDateTime detectionTime;
    private String appVersion;
    private String platform;
    private Double detectionDistance;
    private Integer detectionDurationMs;
    private String videoId;
    private Boolean playbackSuccess;
    private LocalDateTime createdAt;
}
