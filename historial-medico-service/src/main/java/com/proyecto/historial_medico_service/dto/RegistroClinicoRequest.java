package com.proyecto.historial_medico_service.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RegistroClinicoRequest {

    private Long pacienteId;
    private Long medicoId;
    private Long especialidadId;
    private Long citaId;

    private LocalDateTime fechaConsulta;

    private String diagnosticoPrincipal;
    private String recetaMedica;
    private String observaciones;
    private String resultadoLaboratorio;
}