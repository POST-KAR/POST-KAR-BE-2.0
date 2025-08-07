package com.postkar.project3dmodel.repository;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.postkar.project3dmodel.entity.User;

@Repository
public interface UserRepository extends MongoRepository<User, ObjectId> {
    Optional<User> findByEmail(String email);
}
