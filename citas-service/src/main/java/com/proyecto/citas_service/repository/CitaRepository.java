package com.proyecto.citas_service.repository;

import com.proyecto.citas_service.model.Cita;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    boolean existsByMedicoIdAndFechaAndHora(
            Long medicoId,
            LocalDate fecha,
            LocalTime hora
    );

    boolean existsByMedicoIdAndFechaAndHoraAndEstadoNot(
            Long medicoId,
            LocalDate fecha,
            LocalTime hora,
            String estado
    );

    // NUEVO: valida si existe una relación entre médico y paciente
    // Se usa desde historial-medico-service
    boolean existsByMedicoIdAndPacienteIdAndEstadoNot(
            Long medicoId,
            Long pacienteId,
            String estado
    );

    List<Cita> findByPacienteId(Long pacienteId);

    List<Cita> findByMedicoId(Long medicoId);

    List<Cita> findByPacienteIdAndEstado(
            Long pacienteId,
            String estado
    );

    List<Cita> findByMedicoIdAndEstado(
            Long medicoId,
            String estado
    );
}