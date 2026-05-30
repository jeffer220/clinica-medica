package com.proyecto.auth.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/*@Data
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String username;
    private String rol;
}*/

@Data
@AllArgsConstructor
public class AuthResponse {
    private String status;
    private String message;
    private AuthData data;
}