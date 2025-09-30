package com.postkar.project3dmodel.entity;

import com.mongodb.lang.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    @Id
    private String id;

    private String name;
    private String description;
    
    @Nullable
    private String thumbnailUrl;
    
    private boolean isFeatured = false;
    private String status = "available"; // "available" or "coming_soon"
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}

