package com.postkar.project3dmodel.controller;

import com.postkar.project3dmodel.dto.explorer.*;
import com.postkar.project3dmodel.service.ExplorerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/explorer")
@Tag(name = "Explorer", description = "APIs for the Explorer screen - AR experience discovery")
public class ExplorerController {

    @Autowired
    private ExplorerService explorerService;

    @Operation(
            summary = "Get All Categories",
            description = "Get list of all categories for the Explorer main screen.\n\n" +
                    "This is the main endpoint for the Explorer grid layout.\n" +
                    "Each category includes:\n" +
                    "- id: Category identifier\n" +
                    "- name: Category display name (e.g., 'Paisa Bolta Hai')\n" +
                    "- description: Category description\n" +
                    "- thumbnailUrl: Signed URL for category image\n" +
                    "- itemCount: Number of AR experiences in this category\n" +
                    "- isFeatured: Whether this category should appear in featured carousel\n" +
                    "- status: 'available' or 'coming_soon'"
    )
    @GetMapping("/categories")
    public ResponseEntity<List<ExplorerCategoryResponse>> getCategories() {
        List<ExplorerCategoryResponse> categories = explorerService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @Operation(
            summary = "Get Subcategories by Category",
            description = "Get list of subcategories (AR experiences) under a specific category.\n\n" +
                    "This is the endpoint for the subcategory list screen after tapping a category.\n" +
                    "Each subcategory includes:\n" +
                    "- id: Subcategory identifier\n" +
                    "- markerId: Business identifier for AR scanning\n" +
                    "- name: Display name (e.g., '₹10 Note')\n" +
                    "- description: Brief description\n" +
                    "- thumbnailUrl: Signed URL for preview image\n" +
                    "- categoryId: Parent category ID\n" +
                    "- categoryName: Parent category name"
    )
    @GetMapping("/categories/{categoryId}/subcategories")
    public ResponseEntity<List<ExplorerSubcategoryResponse>> getSubcategories(
            @Parameter(description = "Category ID", example = "64f1b2c3d4e5f6789abcdef0")
            @PathVariable String categoryId
    ) {
        List<ExplorerSubcategoryResponse> subcategories = explorerService.getSubcategoriesByCategory(categoryId);
        return ResponseEntity.ok(subcategories);
    }

    @Operation(
            summary = "Get Subcategory Detail",
            description = "Get detailed information for a specific subcategory.\n\n" +
                    "This is the endpoint for the subcategory detail screen with the large image and CTA button.\n" +
                    "Response includes:\n" +
                    "- id: Subcategory identifier\n" +
                    "- markerId: Business identifier for AR scanning\n" +
                    "- title: Full title for display\n" +
                    "- description: Complete description text\n" +
                    "- thumbnailUrl: Signed URL for the main display image\n" +
                    "- mediaPreviewUrl: Signed URL for video preview\n" +
                    "- arAssetUrl: Signed URL for AR content\n" +
                    "- triggerMarkerUrl: Signed URL for the marker image\n" +
                    "- categoryId & categoryName: Parent category information"
    )
    @GetMapping("/subcategories/{subcategoryId}")
    public ResponseEntity<ExplorerSubcategoryDetailResponse> getSubcategoryDetail(
            @Parameter(description = "Subcategory ID (markerId)", example = "10-rupee-note")
            @PathVariable String subcategoryId
    ) {
        return explorerService.getSubcategoryDetail(subcategoryId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Get AR Preview Information",
            description = "Get AR preview information for a specific subcategory.\n\n" +
                    "This endpoint provides all the information needed to set up AR scanning:\n" +
                    "- markerId: Business identifier for tracking\n" +
                    "- arAssetUrl: Signed URL for AR content (video/3D model)\n" +
                    "- instructions: Text instructions for the user\n" +
                    "- fallbackPreviewUrl: Fallback content if AR fails\n" +
                    "- markerImageUrl: Signed URL for iOS ARReferenceImage creation\n" +
                    "- physicalWidthMeters: Real-world size for AR tracking"
    )
    @GetMapping("/subcategories/{subcategoryId}/ar-preview")
    public ResponseEntity<ExplorerArPreviewResponse> getArPreview(
            @Parameter(description = "Subcategory ID (markerId)", example = "10-rupee-note")
            @PathVariable String subcategoryId
    ) {
        return explorerService.getArPreview(subcategoryId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Search Categories and Subcategories",
            description = "Search across all categories and subcategories using a query string.\n\n" +
                    "This endpoint powers the search functionality in the Explorer screen.\n" +
                    "Searches through:\n" +
                    "- Category names and descriptions\n" +
                    "- Subcategory names and descriptions\n\n" +
                    "Returns separate lists of matching categories and subcategories with total count."
    )
    @GetMapping("/search")
    public ResponseEntity<ExplorerSearchResponse> search(
            @Parameter(description = "Search query", example = "currency")
            @RequestParam String q
    ) {
        ExplorerSearchResponse results = explorerService.search(q);
        return ResponseEntity.ok(results);
    }

    @Operation(
            summary = "Get Featured Content",
            description = "Get curated/featured Explorer items for the featured carousel.\n\n" +
                    "Returns a list of featured categories that should be displayed prominently\n" +
                    "in the Explorer screen carousel (limited to 3-4 items as per design).\n\n" +
                    "Categories are marked as featured through the admin interface."
    )
    @GetMapping("/featured")
    public ResponseEntity<List<ExplorerCategoryResponse>> getFeaturedContent() {
        List<ExplorerCategoryResponse> featured = explorerService.getFeaturedContent();
        return ResponseEntity.ok(featured);
    }

    @Operation(
            summary = "Track Analytics Interaction",
            description = "Track user interactions within the Explorer screen for analytics.\n\n" +
                    "Use this endpoint to track:\n" +
                    "- Category views ('category_view')\n" +
                    "- Subcategory views ('subcategory_view')\n" +
                    "- AR preview launches ('ar_preview')\n" +
                    "- Search queries ('search')\n\n" +
                    "This data helps understand user behavior and optimize content."
    )
    @PostMapping("/analytics/interaction")
    public ResponseEntity<String> trackInteraction(
            @Valid @RequestBody ExplorerAnalyticsRequest request
    ) {
        explorerService.trackAnalytics(request);
        return ResponseEntity.ok("Interaction tracked successfully");
    }
}
