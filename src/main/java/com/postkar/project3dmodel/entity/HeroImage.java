package com.postkar.project3dmodel.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "hero_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HeroImage {
    @Id
    private String id;

    private String imageUrl;
    private String fileKey; // R2 object key
    private String title;
    private String description;
    private Integer displayOrder;

    private Long fileSize; // Size in bytes
    private String contentType; // MIME type
    private String uploadedBy; // Admin user who uploaded

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isActive = true;
}
