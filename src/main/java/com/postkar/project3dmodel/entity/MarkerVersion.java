package com.postkar.project3dmodel.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "marker_versions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarkerVersion {
    @Id
    private String id;

    private String version;              // "v1", "v2", "v3"
    private String arDatabaseId;         // Reference to ArDatabase
    private LocalDateTime lastUpdated;
    private boolean isActive;

    // Quick access info for app version checks
    private Integer totalMarkers;
    private String changeLog;
}