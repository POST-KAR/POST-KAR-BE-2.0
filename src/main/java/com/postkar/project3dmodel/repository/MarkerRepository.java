package com.postkar.project3dmodel.repository;

import com.postkar.project3dmodel.entity.Marker;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MarkerRepository extends MongoRepository<Marker, String> {
    // Find by markerId (business ID like "A", "B", "C")
    Optional<Marker> findByMarkerId(String markerId);

    // Find all active markers
    Page<Marker> findByIsActiveTrue(Pageable pageable);
    List<Marker> findByIsActiveTrue();

    // Find markers updated after a certain time (for sync)
    List<Marker> findByLastUpdatedAfter(LocalDateTime since);

    // Check if markerId already exists
    boolean existsByMarkerId(String markerId);

    // Find by multiple marker IDs
    List<Marker> findByMarkerIdIn(List<String> markerIds);

    // Custom query to find markers that need database rebuild
    @Query("{ 'lastUpdated': { $gte: ?0 }, 'isActive': true }")
    List<Marker> findMarkersModifiedSince(LocalDateTime since);

    // Find markers by category ID
    List<Marker> findByCategoryId(String categoryId);
    
    // Find active markers by category ID
    List<Marker> findByCategoryIdAndIsActiveTrue(String categoryId);
    
    // Find active markers by category ID with pagination
    Page<Marker> findByCategoryIdAndIsActiveTrue(String categoryId, Pageable pageable);
    
    // Find markers by multiple category IDs
    List<Marker> findByCategoryIdIn(List<String> categoryIds);
    
    // Find active markers by multiple category IDs
    List<Marker> findByCategoryIdInAndIsActiveTrue(List<String> categoryIds);
}
