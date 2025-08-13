package com.postkar.project3dmodel.repository;

import com.postkar.project3dmodel.entity.TempRegistration;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TempRegistrationRepository extends MongoRepository<TempRegistration, String> {

    Optional<TempRegistration> findByEmail(String email);

    void deleteByEmail(String email);

    @Query("{'expiresAt': {$lt: ?0}}")
    void deleteByExpiresAtBefore(LocalDateTime dateTime);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
}
