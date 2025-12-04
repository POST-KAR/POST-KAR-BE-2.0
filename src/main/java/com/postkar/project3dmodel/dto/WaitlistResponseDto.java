package com.postkar.project3dmodel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WaitlistResponseDto {

    private String id;
    private String name;
    private String email;
    private String phone;
    private LocalDateTime createdAt;
}
