package com.postkar.project3dmodel.repository;

import com.postkar.project3dmodel.entity.Model;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModelRepository extends MongoRepository<Model, String> {
    Page<Model> findByCategoryId(String categoryId, Pageable pageable);
}