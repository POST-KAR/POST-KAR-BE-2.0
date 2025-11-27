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
    Optional<Marker> findByMarkerId(String markerId);

    Page<Marker> findByIsActiveTrue(Pageable pageable);

    List<Marker> findByIsActiveTrue();

    List<Marker> findByLastUpdatedAfter(LocalDateTime since);

    boolean existsByMarkerId(String markerId);

    List<Marker> findByMarkerIdIn(List<String> markerIds);

    @Query("{ 'lastUpdated': { $gte: ?0 }, 'isActive': true }")
    List<Marker> findMarkersModifiedSince(LocalDateTime since);

    List<Marker> findByCategoryId(String categoryId);

    List<Marker> findByCategoryIdAndIsActiveTrue(String categoryId);

    Page<Marker> findByCategoryIdAndIsActiveTrue(String categoryId, Pageable pageable);

    List<Marker> findByCategoryIdIn(List<String> categoryIds);

    List<Marker> findByCategoryIdInAndIsActiveTrue(List<String> categoryIds);
}
