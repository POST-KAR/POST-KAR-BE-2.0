package com.postkar.project3dmodel.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.postkar.project3dmodel.entity.User;
import com.postkar.project3dmodel.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User createNewUser(User user) {

        return user;
    }
}

