package com.proyecto.pacientes_service.repository;

import com.proyecto.pacientes_service.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {
}