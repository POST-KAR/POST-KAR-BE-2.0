package com.postkar.project3dmodel.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SyncRequest {
    private String currentVersion;
    private LocalDateTime lastSyncTime;
    private String platform; // "android" or "ios"
    private String appVersion;
    private String deviceId;
}
