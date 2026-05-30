package com.proyecto.medicos_service.service;

import com.proyecto.medicos_service.model.HorarioMedico;
import com.proyecto.medicos_service.repository.HorarioMedicoRepository;
import com.proyecto.medicos_service.repository.MedicoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HorarioMedicoService {

    private final HorarioMedicoRepository repo;
    private final MedicoRepository medicoRepository;

    public HorarioMedicoService(
            HorarioMedicoRepository repo,
            MedicoRepository medicoRepository
    ) {
        this.repo = repo;
        this.medicoRepository = medicoRepository;
    }

    public HorarioMedico guardar(HorarioMedico horario) {

        validarHorario(horario);

        horario.setDiaSemana(horario.getDiaSemana().toUpperCase());

        validarCruceHorario(horario, null);

        horario.setDisponible(true);

        return repo.save(horario);
    }

    public List<HorarioMedico> listar() {
        return repo.findAll();
    }

    public HorarioMedico buscarPorId(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Horario no encontrado"));
    }

    public List<HorarioMedico> listarPorMedico(Long medicoId) {
        return repo.findByMedico_Id(medicoId);
    }

    public List<HorarioMedico> listarDisponiblesPorMedico(Long medicoId) {
        return repo.findByMedico_IdAndDisponibleTrue(medicoId);
    }

    public List<HorarioMedico> listarDisponiblesPorMedicoYDia(
            Long medicoId,
            String diaSemana
    ) {
        return repo.findByMedico_IdAndDiaSemanaAndDisponibleTrue(
                medicoId,
                diaSemana.toUpperCase()
        );
    }

    public HorarioMedico actualizar(Long id, HorarioMedico horario) {

        HorarioMedico existente = buscarPorId(id);

        validarHorario(horario);

        horario.setDiaSemana(horario.getDiaSemana().toUpperCase());

        validarCruceHorario(horario, id);

        existente.setMedico(horario.getMedico());
        existente.setDiaSemana(horario.getDiaSemana());
        existente.setHoraInicio(horario.getHoraInicio());
        existente.setHoraFin(horario.getHoraFin());
        existente.setDisponible(horario.getDisponible());

        return repo.save(existente);
    }

    public HorarioMedico desactivar(Long id) {
        HorarioMedico horario = buscarPorId(id);
        horario.setDisponible(false);
        return repo.save(horario);
    }

    public HorarioMedico activar(Long id) {
        HorarioMedico horario = buscarPorId(id);
        horario.setDisponible(true);
        return repo.save(horario);
    }

    public void eliminar(Long id) {
        HorarioMedico horario = buscarPorId(id);
        repo.delete(horario);
    }

    public boolean validarDisponibilidad(
            Long medicoId,
            String diaSemana,
            java.time.LocalTime hora
    ) {

        List<HorarioMedico> horarios =
                repo.findByMedico_IdAndDiaSemanaAndDisponibleTrue(
                        medicoId,
                        diaSemana.toUpperCase()
                );

        return horarios.stream().anyMatch(h ->
                !hora.isBefore(h.getHoraInicio()) &&
                hora.isBefore(h.getHoraFin())
        );
    }

    private void validarHorario(HorarioMedico horario) {

        if (horario.getMedico() == null || horario.getMedico().getId() == null) {
            throw new RuntimeException("Debe seleccionar un médico");
        }

        if (!medicoRepository.existsById(horario.getMedico().getId())) {
            throw new RuntimeException("El médico no existe");
        }

        if (horario.getDiaSemana() == null || horario.getDiaSemana().isBlank()) {
            throw new RuntimeException("El día de la semana es obligatorio");
        }

        if (horario.getHoraInicio() == null) {
            throw new RuntimeException("La hora de inicio es obligatoria");
        }

        if (horario.getHoraFin() == null) {
            throw new RuntimeException("La hora de fin es obligatoria");
        }

        if (!horario.getHoraInicio().isBefore(horario.getHoraFin())) {
            throw new RuntimeException("La hora de inicio debe ser menor que la hora de fin");
        }
    }

    private void validarCruceHorario(HorarioMedico horario, Long idActual) {

        List<HorarioMedico> horariosExistentes =
                repo.findByMedico_IdAndDiaSemana(
                        horario.getMedico().getId(),
                        horario.getDiaSemana().toUpperCase()
                );

        boolean existeCruce = horariosExistentes.stream().anyMatch(h -> {

            if (idActual != null && h.getId().equals(idActual)) {
                return false;
            }

            return horario.getHoraInicio().isBefore(h.getHoraFin())
                    && horario.getHoraFin().isAfter(h.getHoraInicio());
        });

        if (existeCruce) {
            throw new RuntimeException("Ya existe un horario registrado para este médico en ese día y rango de hora");
        }
    }
}