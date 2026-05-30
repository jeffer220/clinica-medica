package com.proyecto.historial_medico_service.dto;



import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MedicoHistorialDto {

    private Long idMedico;
    private String nombre;
    private String especialidad;
}