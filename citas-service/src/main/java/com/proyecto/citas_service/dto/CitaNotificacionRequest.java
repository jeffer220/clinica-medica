package com.proyecto.citas_service.dto;

import lombok.Data;

@Data
public class CitaNotificacionRequest {

    private String emailDestino;

    private String nombrePaciente;

    private String codigoCita;

    private String nombreMedico;

    private String especialidad;

    private String fecha;

    private String hora;

    private String estado;

    private String observaciones;
}
