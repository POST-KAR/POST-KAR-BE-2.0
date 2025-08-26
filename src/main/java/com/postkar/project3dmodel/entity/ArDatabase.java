package com.postkar.project3dmodel.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "ar_databases")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArDatabase {
    @Id
    private String id;

    private String version;
    private String imgdbUrl;
    private String imgdbChecksum;

    private LocalDateTime createdAt;
    private LocalDateTime publishedAt;

    private boolean isActive;

    private String buildStatus;
    private String buildLog;
}
