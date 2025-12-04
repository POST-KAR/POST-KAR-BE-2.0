package com.postkar.project3dmodel.repository;

import com.postkar.project3dmodel.entity.HeroImage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HeroImageRepository extends MongoRepository<HeroImage, String> {
    
    @Query("{ 'isActive': true }")
    List<HeroImage> findAllActive();
}
