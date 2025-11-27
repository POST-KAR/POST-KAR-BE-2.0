package com.postkar.project3dmodel.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private ObjectId id;

    private String email; // Primary identifier - email address
    private boolean emailVerified;
    private boolean isActive;

    private String otp;
    private LocalDateTime otpGeneratedAt;

    private String provider; // LOCAL, GOOGLE, FACEBOOK
    private String providerId;

    private String jwtToken;
    private String refreshToken;
    private LocalDateTime refreshTokenExpiry;

    // Registration status enum
    public enum RegistrationStatus {
        EMAIL_PENDING,
        EMAIL_VERIFIED,
        COMPLETED
    }

    private RegistrationStatus registrationStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}