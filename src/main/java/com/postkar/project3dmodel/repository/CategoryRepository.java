package com.postkar.project3dmodel.repository;

import com.postkar.project3dmodel.entity.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends MongoRepository<Category, String> {

    Category findByName(String name);

    boolean existsByName(String name);
}
