package com.postkar.project3dmodel.service;

import com.postkar.project3dmodel.response.CategoryResponse;
import com.postkar.project3dmodel.response.CategoryWithMarkersResponse;
import com.postkar.project3dmodel.response.MarkerSummaryResponse;
import com.postkar.project3dmodel.entity.Category;
import com.postkar.project3dmodel.entity.Marker;
import com.postkar.project3dmodel.repository.CategoryRepository;
import com.postkar.project3dmodel.repository.MarkerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private MarkerRepository markerRepository;
    
    @Autowired
    private SignedUrlService signedUrlService;

    // Get all categories with marker count
    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        
        return categories.stream()
                .map(this::convertToCategoryResponse)
                .collect(Collectors.toList());
    }

    public Page<Category> getAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }

    public Optional<Category> getCategoryById(String categoryId) {
        return categoryRepository.findById(categoryId);
    }

    public Optional<CategoryWithMarkersResponse> getCategoryWithMarkers(String categoryId) {
        Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
        
        if (categoryOpt.isPresent()) {
            Category category = categoryOpt.get();
            List<Marker> markers = markerRepository.findByCategoryIdAndIsActiveTrue(categoryId);
            
            CategoryWithMarkersResponse response = new CategoryWithMarkersResponse();
            response.setId(category.getId());
            response.setName(category.getName());
            response.setDescription(category.getDescription());
            
            List<MarkerSummaryResponse> markerSummaries = markers.stream()
                    .map(this::convertToMarkerSummary)
                    .collect(Collectors.toList());
            
            response.setMarkers(markerSummaries);
            return Optional.of(response);
        }
        
        return Optional.empty();
    }

    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }

    public Optional<Category> updateCategory(String categoryId, Category categoryUpdate) {
        return categoryRepository.findById(categoryId)
                .map(existingCategory -> {
                    existingCategory.setName(categoryUpdate.getName());
                    existingCategory.setDescription(categoryUpdate.getDescription());
                    return categoryRepository.save(existingCategory);
                });
    }

    public boolean deleteCategory(String categoryId) {
        if (categoryRepository.existsById(categoryId)) {
            categoryRepository.deleteById(categoryId);
            return true;
        }
        return false;
    }

    private CategoryResponse convertToCategoryResponse(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        
        // Count active markers in this category
        List<Marker> markers = markerRepository.findByCategoryIdAndIsActiveTrue(category.getId());
        response.setMarkerCount(markers.size());
        
        return response;
    }

    private MarkerSummaryResponse convertToMarkerSummary(Marker marker) {
        MarkerSummaryResponse response = new MarkerSummaryResponse();
        response.setId(marker.getId());
        response.setMarkerId(marker.getMarkerId());
        response.setName(marker.getName());
        response.setDescription(marker.getDescription());
        response.setThumbnailUrl(signedUrlService.generateSignedUrl(marker.getThumbnailUrl()));
        response.setActive(marker.isActive());
        
        return response;
    }
}