package com.proyecto.historial_medico_service.repository;

import com.proyecto.historial_medico_service.model.RegistroClinico;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistroClinicoRepository extends JpaRepository<RegistroClinico, Long> {

    Page<RegistroClinico> findByPacienteId(Long pacienteId, Pageable pageable);

    boolean existsByPacienteId(Long pacienteId);

    boolean existsByCitaId(Long citaId);
}