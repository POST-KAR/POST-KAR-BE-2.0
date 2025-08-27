package com.postkar.project3dmodel.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SyncRequest {
    private String currentVersion;
    private LocalDateTime lastSyncTime;
    private String platform;
    private String appVersion;
    private String deviceId;
}
