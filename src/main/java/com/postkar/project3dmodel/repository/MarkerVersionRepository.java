package com.postkar.project3dmodel.repository;

import com.postkar.project3dmodel.entity.MarkerVersion;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MarkerVersionRepository extends MongoRepository<MarkerVersion, String> {

    // Get the current active version
    Optional<MarkerVersion> findByIsActiveTrue();

    // Find by version string
    Optional<MarkerVersion> findByVersion(String version);

    // Get latest version
    Optional<MarkerVersion> findTopByOrderByLastUpdatedDesc();
}
