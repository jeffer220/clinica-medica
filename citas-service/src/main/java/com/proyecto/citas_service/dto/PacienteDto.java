package com.proyecto.citas_service.dto;

import lombok.Data;

@Data
public class PacienteDto {

    private Long id;

    private String nombre;

    private String apellido;

    private String telefono;

    private String email;

    private String fechaNacimiento;

    private String direccion;

    private String seguroMedico;
}