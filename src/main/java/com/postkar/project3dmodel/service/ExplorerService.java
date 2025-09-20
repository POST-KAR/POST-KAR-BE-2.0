package com.postkar.project3dmodel.service;

import com.postkar.project3dmodel.dto.explorer.*;
import com.postkar.project3dmodel.entity.Category;
import com.postkar.project3dmodel.entity.Marker;
import com.postkar.project3dmodel.entity.Video;
import com.postkar.project3dmodel.repository.CategoryRepository;
import com.postkar.project3dmodel.repository.MarkerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ExplorerService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private MarkerRepository markerRepository;

    @Autowired
    private SignedUrlService signedUrlService;

    @Autowired
    private ReportingService reportingService;

    /**
     * Get all categories for Explorer screen
     */
    public List<ExplorerCategoryResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        
        return categories.stream()
                .map(this::convertToExplorerCategoryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get subcategories (markers) for a specific category
     */
    public List<ExplorerSubcategoryResponse> getSubcategoriesByCategory(String categoryId) {
        Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
        if (categoryOpt.isEmpty()) {
            return List.of();
        }

        Category category = categoryOpt.get();
        List<Marker> markers = markerRepository.findByCategoryIdAndIsActiveTrue(categoryId);

        return markers.stream()
                .map(marker -> convertToExplorerSubcategoryResponse(marker, category))
                .collect(Collectors.toList());
    }

    /**
     * Get detailed information for a specific subcategory
     */
    public Optional<ExplorerSubcategoryDetailResponse> getSubcategoryDetail(String subcategoryId) {
        Optional<Marker> markerOpt = markerRepository.findByMarkerId(subcategoryId);
        if (markerOpt.isEmpty()) {
            return Optional.empty();
        }

        Marker marker = markerOpt.get();
        Optional<Category> categoryOpt = categoryRepository.findById(marker.getCategoryId());

        ExplorerSubcategoryDetailResponse response = new ExplorerSubcategoryDetailResponse();
        response.setId(marker.getId());
        response.setMarkerId(marker.getMarkerId());
        response.setTitle(marker.getName());
        response.setDescription(marker.getDescription());
        response.setThumbnailUrl(signedUrlService.generateSignedUrl(marker.getThumbnailUrl()));
        response.setTriggerMarkerUrl(signedUrlService.generateSignedUrl(marker.getMarkerImageUrl()));
        response.setCategoryId(marker.getCategoryId());
        response.setActive(marker.isActive());

        // Set media preview URL from active video
        if (marker.getActiveVideoId() != null && marker.getVideos() != null) {
            Video activeVideo = marker.getVideos().stream()
                    .filter(v -> v.getId().equals(marker.getActiveVideoId()))
                    .findFirst()
                    .orElse(null);
            
            if (activeVideo != null) {
                response.setMediaPreviewUrl(signedUrlService.generateSignedUrl(activeVideo.getVideoUrl()));
                response.setArAssetUrl(signedUrlService.generateSignedUrl(activeVideo.getVideoUrl()));
            }
        }

        // Set category name
        categoryOpt.ifPresent(category -> response.setCategoryName(category.getName()));

        return Optional.of(response);
    }

    /**
     * Get AR preview information for a subcategory
     */
    public Optional<ExplorerArPreviewResponse> getArPreview(String subcategoryId) {
        Optional<Marker> markerOpt = markerRepository.findByMarkerId(subcategoryId);
        if (markerOpt.isEmpty()) {
            return Optional.empty();
        }

        Marker marker = markerOpt.get();
        ExplorerArPreviewResponse response = new ExplorerArPreviewResponse();
        response.setMarkerId(marker.getMarkerId());
        response.setMarkerImageUrl(signedUrlService.generateSignedUrl(marker.getMarkerImageUrl()));
        response.setPhysicalWidthMeters(marker.getPhysicalWidthMeters());
        response.setInstructions("Point your camera at the " + marker.getName() + " to reveal AR content");

        // Set AR asset URL from active video
        if (marker.getActiveVideoId() != null && marker.getVideos() != null) {
            Video activeVideo = marker.getVideos().stream()
                    .filter(v -> v.getId().equals(marker.getActiveVideoId()))
                    .findFirst()
                    .orElse(null);
            
            if (activeVideo != null) {
                response.setArAssetUrl(signedUrlService.generateSignedUrl(activeVideo.getVideoUrl()));
                response.setFallbackPreviewUrl(signedUrlService.generateSignedUrl(activeVideo.getVideoUrl()));
            }
        }

        return Optional.of(response);
    }

    /**
     * Search across categories and subcategories
     */
    public ExplorerSearchResponse search(String query) {
        ExplorerSearchResponse response = new ExplorerSearchResponse();
        response.setQuery(query);

        // Search categories
        List<Category> matchingCategories = categoryRepository.findAll().stream()
                .filter(category -> 
                    category.getName().toLowerCase().contains(query.toLowerCase()) ||
                    category.getDescription().toLowerCase().contains(query.toLowerCase())
                )
                .collect(Collectors.toList());

        List<ExplorerCategoryResponse> categoryResponses = matchingCategories.stream()
                .map(this::convertToExplorerCategoryResponse)
                .collect(Collectors.toList());

        // Search markers (subcategories)
        List<Marker> matchingMarkers = markerRepository.findByIsActiveTrue().stream()
                .filter(marker ->
                    marker.getName().toLowerCase().contains(query.toLowerCase()) ||
                    marker.getDescription().toLowerCase().contains(query.toLowerCase())
                )
                .collect(Collectors.toList());

        List<ExplorerSubcategoryResponse> subcategoryResponses = matchingMarkers.stream()
                .map(marker -> {
                    Optional<Category> categoryOpt = categoryRepository.findById(marker.getCategoryId());
                    return convertToExplorerSubcategoryResponse(marker, categoryOpt.orElse(null));
                })
                .collect(Collectors.toList());

        response.setCategories(categoryResponses);
        response.setSubcategories(subcategoryResponses);
        response.setTotalResults(categoryResponses.size() + subcategoryResponses.size());

        return response;
    }

    /**
     * Get featured categories and subcategories
     */
    public List<ExplorerCategoryResponse> getFeaturedContent() {
        List<Category> featuredCategories = categoryRepository.findAll().stream()
                .filter(Category::isFeatured)
                .limit(4) // Limit to 3-4 as mentioned in the design
                .collect(Collectors.toList());

        return featuredCategories.stream()
                .map(this::convertToExplorerCategoryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Track analytics for Explorer interactions
     */
    public void trackAnalytics(ExplorerAnalyticsRequest request) {
        // For now, we can log the analytics or store in a separate collection
        // This can be enhanced later to integrate with existing ReportingService
        System.out.println("Explorer Analytics: " + request.getEventType() + 
                          " - Category: " + request.getCategoryId() + 
                          " - Subcategory: " + request.getSubcategoryId());
    }

    // Helper methods
    private ExplorerCategoryResponse convertToExplorerCategoryResponse(Category category) {
        ExplorerCategoryResponse response = new ExplorerCategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setFeatured(category.isFeatured());
        response.setStatus(category.getStatus());

        // Generate signed URL for thumbnail if exists
        if (category.getThumbnailUrl() != null) {
            response.setThumbnailUrl(signedUrlService.generateSignedUrl(category.getThumbnailUrl()));
        }

        // Count active markers in this category
        List<Marker> markers = markerRepository.findByCategoryIdAndIsActiveTrue(category.getId());
        response.setItemCount(markers.size());

        return response;
    }

    private ExplorerSubcategoryResponse convertToExplorerSubcategoryResponse(Marker marker, Category category) {
        ExplorerSubcategoryResponse response = new ExplorerSubcategoryResponse();
        response.setId(marker.getId());
        response.setMarkerId(marker.getMarkerId());
        response.setName(marker.getName());
        response.setDescription(marker.getDescription());
        response.setThumbnailUrl(signedUrlService.generateSignedUrl(marker.getThumbnailUrl()));
        response.setCategoryId(marker.getCategoryId());
        
        if (category != null) {
            response.setCategoryName(category.getName());
        }

        return response;
    }
}
