package com.proyecto.auth.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UsuarioDto {
    private Long id;
    private String email;
    private String rol;
    private Long pacienteId;
    private Long medicoId;
}