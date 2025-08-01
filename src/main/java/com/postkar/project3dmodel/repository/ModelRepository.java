package com.postkar.project3dmodel.repository;


import com.postkar.project3dmodel.entity.Model;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModelRepository extends MongoRepository<Model, String> {
    List<Model> findByCategoryId(String categoryId);
}

