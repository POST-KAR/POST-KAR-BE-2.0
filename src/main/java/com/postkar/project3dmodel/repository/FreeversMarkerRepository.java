package com.postkar.project3dmodel.repository;

import com.postkar.project3dmodel.entity.FreeversMarker;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FreeversMarkerRepository extends MongoRepository<FreeversMarker, String> {

    Page<FreeversMarker> findByIsActiveTrue(Pageable pageable);

    List<FreeversMarker> findByIsActiveTrue();

    List<FreeversMarker> findByLastUpdatedAfter(LocalDateTime since);

    Optional<FreeversMarker> findByMarkerId(String markerId);

    boolean existsByMarkerId(String markerId);

    List<FreeversMarker> findByCategoryIdAndIsActiveTrue(String categoryId);
}
