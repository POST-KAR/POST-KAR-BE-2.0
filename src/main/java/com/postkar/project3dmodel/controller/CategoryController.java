package com.postkar.project3dmodel.controller;

import com.postkar.project3dmodel.response.CategoryResponse;
import com.postkar.project3dmodel.response.CategoryWithMarkersResponse;
import com.postkar.project3dmodel.entity.Category;
import com.postkar.project3dmodel.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Categories", description = "APIs for category management and marker organization")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Operation(
            summary = "Get All Categories",
            description = "Get list of all categories with marker count.\n\n" +
                    "This is the main endpoint for the category selection screen.\n" +
                    "Each category includes:\n" +
                    "- id: Category identifier\n" +
                    "- name: Category display name\n" +
                    "- description: Category description\n" +
                    "- markerCount: Number of active markers in this category"
    )
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @Operation(
            summary = "Get Categories (Paginated)",
            description = "Get paginated list of categories.\n\n" +
                    "Use this for large datasets with pagination support.\n" +
                    "Pagination example: ?page=0&size=10&sort=name,asc"
    )
    @GetMapping("/paginated")
    public ResponseEntity<Page<Category>> getAllCategoriesPaginated(
            @Parameter(description = "Pagination parameters")
            Pageable pageable
    ) {
        return ResponseEntity.ok(categoryService.getAllCategories(pageable));
    }

    @Operation(
            summary = "Get Category with Markers",
            description = "Get a specific category with all its active markers.\n\n" +
                    "This is the main endpoint for the marker selection screen after choosing a category.\n" +
                    "Returns category details plus list of markers with:\n" +
                    "- markerId: Business identifier for scanning\n" +
                    "- name: Marker display name\n" +
                    "- description: Marker description\n" +
                    "- thumbnailUrl: Signed URL for marker preview (valid 24 hours)"
    )
    @GetMapping("/{categoryId}/markers")
    public ResponseEntity<CategoryWithMarkersResponse> getCategoryWithMarkers(
            @Parameter(description = "Category ID", example = "64f1b2c3d4e5f6789abcdef0")
            @PathVariable String categoryId
    ) {
        return categoryService.getCategoryWithMarkers(categoryId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Get Category Details",
            description = "Get detailed information about a specific category.\n\n" +
                    "Returns complete category information including:\n" +
                    "- id: Category unique identifier\n" +
                    "- name: Category display name\n" +
                    "- description: Category description\n" +
                    "- createdAt: Category creation timestamp\n" +
                    "- updatedAt: Last modification timestamp\n\n" +
                    "Use this endpoint to get category metadata before displaying category details."
    )
    @GetMapping("/{categoryId}")
    public ResponseEntity<Category> getCategoryById(
            @Parameter(description = "Category ID", example = "64f1b2c3d4e5f6789abcdef0")
            @PathVariable String categoryId
    ) {
        return categoryService.getCategoryById(categoryId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Create Category",
            description = "Create a new category for organizing markers (Admin only).\n\n" +
                    "Creates a new category that can be used to organize markers.\n" +
                    "Categories help organize markers by theme, location, or purpose.\n\n" +
                    "Request body should include:\n" +
                    "- name: Category display name (required)\n" +
                    "- description: Category description (optional)\n\n" +
                    "Returns the created category with generated ID."
    )
    @PostMapping
    public ResponseEntity<Category> createCategory(
            @Parameter(description = "Category details to create")
            @RequestBody Category category
    ) {
        Category createdCategory = categoryService.createCategory(category);
        return ResponseEntity.ok(createdCategory);
    }

    @Operation(
            summary = "Update Category",
            description = "Update an existing category's properties (Admin only).\n\n" +
                    "Updates category information such as name and description.\n" +
                    "Only provided fields will be updated (partial update supported).\n\n" +
                    "Updatable fields:\n" +
                    "- name: Category display name\n" +
                    "- description: Category description\n\n" +
                    "Returns the updated category or 404 if not found."
    )
    @PutMapping("/{categoryId}")
    public ResponseEntity<Category> updateCategory(
            @Parameter(description = "Category ID", example = "64f1b2c3d4e5f6789abcdef0")
            @PathVariable String categoryId,
            @Parameter(description = "Category fields to update")
            @RequestBody Category categoryUpdate
    ) {
        return categoryService.updateCategory(categoryId, categoryUpdate)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Delete Category",
            description = "Delete a category and handle associated markers (Admin only).\n\n" +
                    "Deletes the specified category from the system.\n" +
                    "Warning: This operation affects all markers in this category.\n\n" +
                    "Behavior:\n" +
                    "- Category is permanently removed\n" +
                    "- Associated markers may be moved to 'uncategorized' or handled per business logic\n" +
                    "- Returns 200 if deleted successfully\n" +
                    "- Returns 404 if category not found\n\n" +
                    "Use with caution as this operation cannot be undone."
    )
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "Category ID to delete", example = "64f1b2c3d4e5f6789abcdef0")
            @PathVariable String categoryId
    ) {
        boolean deleted = categoryService.deleteCategory(categoryId);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}