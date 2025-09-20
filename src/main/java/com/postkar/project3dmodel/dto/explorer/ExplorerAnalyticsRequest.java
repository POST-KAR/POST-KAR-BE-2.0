package com.postkar.project3dmodel.dto.explorer;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class ExplorerAnalyticsRequest {
    @NotBlank(message = "Event type is required")
    private String eventType; // "category_view", "subcategory_view", "ar_preview", "search"
    
    private String categoryId;
    private String subcategoryId;
    private String searchQuery;
    private String userId;
    
    @NotNull(message = "Timestamp is required")
    private Long timestamp;
    
    private String platform; // "android", "ios"
    private String appVersion;
}
