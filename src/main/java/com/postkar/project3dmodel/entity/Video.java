package com.postkar.project3dmodel.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Video {
    private String id;
    private String name;
    private String description;

    private String videoUrl;
    private List<String> variants;
    private String format;
    private Long fileSizeBytes;
    private Integer durationSeconds;
    private Integer width;
    private Integer height;

    private String schedule;
    private boolean isDefault;
}
