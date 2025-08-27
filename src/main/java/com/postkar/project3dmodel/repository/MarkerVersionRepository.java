package com.postkar.project3dmodel.repository;

import com.postkar.project3dmodel.entity.MarkerVersion;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MarkerVersionRepository extends MongoRepository<MarkerVersion, String> {

    Optional<MarkerVersion> findByIsActiveTrue();

    Optional<MarkerVersion> findByVersion(String version);

    Optional<MarkerVersion> findTopByOrderByLastUpdatedDesc();
}
