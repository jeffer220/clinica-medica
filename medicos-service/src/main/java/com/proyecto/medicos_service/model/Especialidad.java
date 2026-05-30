package com.proyecto.medicos_service.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Especialidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
}