package com.proyecto.citas_service.model;

import lombok.Data;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Data
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String codigoCita;

    private Long pacienteId;

    private Long medicoId;

    private Long especialidadId;

    private LocalDate fecha;

    private LocalTime hora;

    private String observaciones;

    private String estado;

    private LocalDateTime fechaRegistro;

    @PrePersist
    public void prePersist() {

        this.estado = "PROGRAMADA";

        this.fechaRegistro = LocalDateTime.now();

        this.codigoCita =
                "CIT-" + System.currentTimeMillis();
    }
}