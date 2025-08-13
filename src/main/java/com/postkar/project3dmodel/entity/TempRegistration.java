package com.postkar.project3dmodel.entity;

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
@Document(collection = "temp_registrations")
public class TempRegistration {

    @Id
    private ObjectId id;

    private String email;
    private String otp;
    private LocalDateTime otpGeneratedAt;
    private boolean emailVerified;

    private String username;
    private String password;

    private String name;
    private String dob;
    private String phoneNumber;

    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    public enum Status {
        EMAIL_SENT,
        EMAIL_VERIFIED,
        CREDENTIALS_SET,
        READY_FOR_COMPLETION
    }

    private Status status;
}