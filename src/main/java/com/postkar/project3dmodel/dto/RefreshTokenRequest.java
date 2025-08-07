package com.postkar.project3dmodel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor

public class RefreshTokenRequest {
    private String email;
    private String refreshToken;
}

