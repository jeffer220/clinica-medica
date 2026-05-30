package com.proyecto.medicos_service.repository;

import com.proyecto.medicos_service.model.Especialidad;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EspecialidadRepository extends JpaRepository<Especialidad, Long> {
}