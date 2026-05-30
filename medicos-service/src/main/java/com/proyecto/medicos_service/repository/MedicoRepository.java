package com.proyecto.medicos_service.repository;

import com.proyecto.medicos_service.model.Medico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicoRepository extends JpaRepository<Medico, Long> {

    List<Medico> findByEspecialidad_Id(Long especialidadId);

    List<Medico> findByActivoTrue();

    boolean existsByColegiado(String colegiado);

    boolean existsByCorreo(String correo);
}