package com.postkar.project3dmodel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DetectionReportRequest {
    @NotBlank(message = "Marker ID is required")
    private String markerId;

    @NotBlank(message = "Device ID is required")
    private String deviceId;

    @NotNull(message = "Detection time is required")
    private LocalDateTime detectionTime;

    private String appVersion;
    private String platform; // "android" or "ios"
    private Double detectionDistance;
    private Integer detectionDurationMs;
    private String videoId;
    private Boolean playbackSuccess;
}