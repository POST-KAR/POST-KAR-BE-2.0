package com.postkar.project3dmodel.repository;

import com.postkar.project3dmodel.entity.VisitSession;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface VisitSessionRepository extends MongoRepository<VisitSession, ObjectId> {
    
    Optional<VisitSession> findBySessionId(String sessionId);
    
    long countByActiveTrue();
    
    long countByLastActivityAfter(LocalDateTime cutoffTime);
}
