package com.proyecto.historial_medico_service.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "registro_clinico")
@Data
public class RegistroClinico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "paciente_id", nullable = false)
    private Long pacienteId;

    @Column(name = "medico_id", nullable = false)
    private Long medicoId;

    @Column(name = "especialidad_id", nullable = false)
    private Long especialidadId;

    @Column(name = "cita_id")
    private Long citaId;

    @Column(name = "fecha_consulta", nullable = false)
    private LocalDateTime fechaConsulta;

    @Column(name = "diagnostico_principal", columnDefinition = "TEXT", nullable = false)
    private String diagnosticoPrincipal;

    @Column(name = "receta_medica", columnDefinition = "TEXT")
    private String recetaMedica;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "resultado_laboratorio", columnDefinition = "TEXT")
    private String resultadoLaboratorio;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @PrePersist
    public void prePersist() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }
    }
}
