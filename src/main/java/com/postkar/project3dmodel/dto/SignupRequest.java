package com.postkar.project3dmodel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class SignupRequest {
    private String name;
    private String email;
    private String password;
    private String phoneNumber;
    private String dob;
}
