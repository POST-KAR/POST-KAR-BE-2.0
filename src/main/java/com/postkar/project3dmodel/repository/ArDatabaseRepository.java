package com.postkar.project3dmodel.repository;

import com.postkar.project3dmodel.entity.ArDatabase;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArDatabaseRepository extends MongoRepository<ArDatabase, String> {
    // Find the currently active database
    Optional<ArDatabase> findByIsActiveTrue();

    // Find by version
    Optional<ArDatabase> findByVersion(String version);

    // Find databases ready for use
    List<ArDatabase> findByBuildStatusOrderByCreatedAtDesc(String buildStatus);

    // Find latest databases
    List<ArDatabase> findTop5ByOrderByCreatedAtDesc();
}
