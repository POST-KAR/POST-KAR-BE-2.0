package com.postkar.project3dmodel.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "models")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Model{
    @Id
    private String id;

    private String name;
    private String description;
    private String categoryId;

    private String modelUrl;
    private String thumbnailUrl;
}

