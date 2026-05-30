package com.proyecto.medicos_service.repository;

import com.proyecto.medicos_service.model.HorarioMedico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HorarioMedicoRepository extends JpaRepository<HorarioMedico, Long> {

    List<HorarioMedico> findByMedico_Id(Long medicoId);

    List<HorarioMedico> findByMedico_IdAndDisponibleTrue(Long medicoId);

    List<HorarioMedico> findByMedico_IdAndDiaSemana(
            Long medicoId,
            String diaSemana
    );

    List<HorarioMedico> findByMedico_IdAndDiaSemanaAndDisponibleTrue(
            Long medicoId,
            String diaSemana
    );
}