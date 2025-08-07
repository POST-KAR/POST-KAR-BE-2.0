package com.postkar.project3dmodel.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private ObjectId id;

    private String name;
    private String email;
    private String password;
    private String phoneNumber;
    private LocalDate dateOfBirth;

    private boolean emailVerified;

    private String otp;
    private LocalDateTime otpGeneratedAt;

    private String provider; // LOCAL, GOOGLE, FACEBOOK
    private String providerId;

    private String jwtToken;
    private String refreshToken;

    private LocalDateTime refreshTokenExpiry;
}