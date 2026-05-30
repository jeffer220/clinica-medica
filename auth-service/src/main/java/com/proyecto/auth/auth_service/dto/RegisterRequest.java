package com.proyecto.auth.auth_service.dto;

import lombok.Data;
/* 
@Data
public class RegisterRequest {

    private String username;
    private String password;
    private String rol;
}*/

@Data
public class RegisterRequest {
    private String username;
    private String email;
    private String password;
    private String rol;
    private Long pacienteId;
    private Long medicoId;
}
