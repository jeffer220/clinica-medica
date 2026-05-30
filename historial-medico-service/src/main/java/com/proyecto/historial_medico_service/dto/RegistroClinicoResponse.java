package com.proyecto.historial_medico_service.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegistroClinicoResponse {

    private Long idRegistro;
    private String fechaConsulta;
    private MedicoHistorialDto medico;
    private String diagnosticoPrincipal;
    private String recetaMedica;
    private String observaciones;
    private String resultadoLaboratorio;
}