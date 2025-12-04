package com.postkar.project3dmodel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for uploading hero poster")
public class HeroPosterUploadRequest {

    @Schema(description = "Title of the hero image (optional, auto-generated from filename if not provided)", example = "Summer Collection 2024")
    private String title;

    @Schema(description = "Description of the hero image", example = "Explore our latest summer collection")
    private String description;

    @Schema(description = "Display order (lower numbers appear first)", example = "1", minimum = "0", maximum = "999")
    @Min(value = 0, message = "Display order must be at least 0")
    @Max(value = 999, message = "Display order must be at most 999")
    private Integer displayOrder;

    @Schema(description = "Whether the image is active", example = "true")
    private Boolean isActive = true;

    @Schema(description = "Admin username who is uploading", example = "admin")
    private String uploadedBy;
}
