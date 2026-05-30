package com.proyecto.auth.auth_service.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthData {
    private String access_token;
    private String token_type;
    private long expires_in;
    private UsuarioDto usuario;
}
