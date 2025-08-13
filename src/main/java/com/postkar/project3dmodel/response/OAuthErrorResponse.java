package com.postkar.project3dmodel.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Component
public class OAuthErrorResponse {

    private boolean success;
    private String error;
    private String message;
}
