package com.postkar.project3dmodel.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object for hero poster upload")
public class HeroPosterUploadResponse {

    @Schema(description = "Unique identifier of the hero image")
    private String id;

    @Schema(description = "Public URL of the uploaded image")
    private String imageUrl;

    @Schema(description = "Title of the hero image")
    private String title;

    @Schema(description = "Description of the hero image")
    private String description;

    @Schema(description = "Display order")
    private Integer displayOrder;

    @Schema(description = "File size in bytes")
    private Long fileSize;

    @Schema(description = "Content type/MIME type")
    private String contentType;

    @Schema(description = "Upload timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Whether the image is active")
    private boolean isActive;

    @Schema(description = "Admin user who uploaded")
    private String uploadedBy;
}
