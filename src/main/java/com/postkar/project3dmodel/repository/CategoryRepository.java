package com.postkar.project3dmodel.repository;

import com.postkar.project3dmodel.entity.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends MongoRepository<Category, String> {
    
    // Find category by name
    Category findByName(String name);
    
    // Check if category exists by name
    boolean existsByName(String name);
}
