package com.postkar.project3dmodel.repository;

import com.postkar.project3dmodel.entity.ArDatabase;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArDatabaseRepository extends MongoRepository<ArDatabase, String> {
    Optional<ArDatabase> findByIsActiveTrue();

    Optional<ArDatabase> findByVersion(String version);

    List<ArDatabase> findByBuildStatusOrderByCreatedAtDesc(String buildStatus);

    List<ArDatabase> findTop5ByOrderByCreatedAtDesc();
}
