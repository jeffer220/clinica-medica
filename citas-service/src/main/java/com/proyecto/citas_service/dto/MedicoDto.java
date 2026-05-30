package com.proyecto.citas_service.dto;

import lombok.Data;

@Data
public class MedicoDto {

    private Long id;

    private String nombre;

    private String apellido;

    private String colegiado;

    private Boolean activo;

    private EspecialidadDto especialidad;
}