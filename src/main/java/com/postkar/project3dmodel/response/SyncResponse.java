package com.postkar.project3dmodel.response;

import com.postkar.project3dmodel.entity.Marker;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SyncResponse {
    private boolean updateRequired;
    private String latestVersion;
    private String imgdbUrl;
    private String imgdbChecksum;
    private List<Marker> updatedMarkers;
    private List<String> deletedMarkerIds;
    private LocalDateTime syncTime;
}