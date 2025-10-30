package com.postkar.project3dmodel.repository;

import com.postkar.project3dmodel.entity.Waitlist;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WaitlistRepository extends MongoRepository<Waitlist, ObjectId> {
    
    Optional<Waitlist> findByEmail(String email);
    
    Optional<Waitlist> findByPhone(String phone);
    
    boolean existsByEmail(String email);
    
    boolean existsByPhone(String phone);
}
