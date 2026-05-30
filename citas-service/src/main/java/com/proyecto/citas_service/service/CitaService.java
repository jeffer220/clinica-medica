package com.proyecto.citas_service.service;

import com.proyecto.citas_service.dto.PacienteDto;
import com.proyecto.citas_service.dto.CitaNotificacionRequest;
import com.proyecto.citas_service.dto.MedicoDto;
import com.proyecto.citas_service.dto.EspecialidadDto;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.proyecto.citas_service.model.Cita;
import com.proyecto.citas_service.repository.CitaRepository;
import com.proyecto.citas_service.security.JwtUtil;

@Service
public class CitaService {

    private final CitaRepository repo;
    private final RestTemplate restTemplate;
    private final JwtUtil jwtUtil;

    public CitaService(
            CitaRepository repo,
            RestTemplate restTemplate,
            JwtUtil jwtUtil
    ) {
        this.repo = repo;
        this.restTemplate = restTemplate;
        this.jwtUtil = jwtUtil;
    }

    public Cita guardar(Cita cita, String token) {

        aplicarPacienteDesdeTokenSiCorresponde(cita, token);

        validarCamposObligatorios(cita);
        validarExistencias(cita, token);

        if (cita.getFecha().isBefore(LocalDate.now())) {
            throw new RuntimeException("No se puede agendar una cita en una fecha pasada");
        }

        boolean ocupada = repo.existsByMedicoIdAndFechaAndHoraAndEstadoNot(
                cita.getMedicoId(),
                cita.getFecha(),
                cita.getHora(),
                "CANCELADA"
        );

        if (ocupada) {
            throw new RuntimeException("Horario no disponible");
        }

        validarHorarioMedico(
                cita.getMedicoId(),
                cita.getFecha(),
                cita.getHora(),
                token
        );

        cita.setEstado("PROGRAMADA");

        Cita citaGuardada = repo.save(cita);

        enviarNotificacionConfirmacion(citaGuardada, token);

        return citaGuardada;
    }

    public List<Cita> listar() {
        return repo.findAll();
    }

    public List<Cita> listarMisCitas(String token) {

        String rol = jwtUtil.extractRol(token);
        Long pacienteId = jwtUtil.extractPacienteId(token);

        if (!"PACIENTE".equalsIgnoreCase(rol)) {
            throw new RuntimeException("Solo los pacientes pueden consultar sus propias citas");
        }

        if (pacienteId == null) {
            throw new RuntimeException("El token no tiene paciente asociado");
        }

        return repo.findByPacienteId(pacienteId);
    }

    public Cita buscarPorId(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
    }

    public List<Cita> listarPorPaciente(Long pacienteId) {
        return repo.findByPacienteId(pacienteId);
    }

    public List<Cita> listarPorMedico(Long medicoId) {
        return repo.findByMedicoId(medicoId);
    }

    public boolean validarRelacionMedicoPaciente(
            Long medicoId,
            Long pacienteId,
            String token
    ) {
        String rol = jwtUtil.extractRol(token);

        if (!"MEDICO".equalsIgnoreCase(rol)
                && !"ADMIN".equalsIgnoreCase(rol)
                && !"RECEPCIONISTA".equalsIgnoreCase(rol)) {
            throw new RuntimeException("No tiene permisos para validar relación médico-paciente");
        }

        if ("MEDICO".equalsIgnoreCase(rol)) {

            Long medicoIdToken = jwtUtil.extractMedicoId(token);

            if (medicoIdToken == null) {
                throw new RuntimeException("El token no tiene médico asociado");
            }

            if (!medicoIdToken.equals(medicoId)) {
                throw new RuntimeException("No puede validar relación de otro médico");
            }
        }

        return repo.existsByMedicoIdAndPacienteIdAndEstadoNot(
                medicoId,
                pacienteId,
                "CANCELADA"
        );
    }

    public Cita cancelar(Long id, String token) {

        Cita cita = buscarPorId(id);

        validarPermisoSobreCita(cita, token, true);

        if (!"PROGRAMADA".equalsIgnoreCase(cita.getEstado())) {
            throw new RuntimeException("Solo se pueden cancelar citas en estado PROGRAMADA");
        }

        validarCambioCon24Horas(cita);

        cita.setEstado("CANCELADA");

        Cita citaCancelada = repo.save(cita);

        enviarNotificacionCancelacion(citaCancelada, token);

        return citaCancelada;
    }

    public Cita marcarComoAtendida(Long id, String token) {

        Cita cita = buscarPorId(id);

        validarPermisoSobreCita(cita, token, false);

        if ("CANCELADA".equalsIgnoreCase(cita.getEstado())) {
            throw new RuntimeException("No se puede atender una cita cancelada");
        }

        cita.setEstado("ATENDIDA");

        return repo.save(cita);
    }

    public Cita reprogramar(Long id, Cita nuevaCita, String token) {

        Cita cita = buscarPorId(id);

        validarPermisoSobreCita(cita, token, true);

        if (!"PROGRAMADA".equalsIgnoreCase(cita.getEstado())) {
            throw new RuntimeException("Solo se pueden reprogramar citas en estado PROGRAMADA");
        }

        validarCambioCon24Horas(cita);

        if (nuevaCita.getFecha() == null || nuevaCita.getHora() == null) {
            throw new RuntimeException("Debe enviar nueva fecha y hora");
        }

        if (nuevaCita.getFecha().isBefore(LocalDate.now())) {
            throw new RuntimeException("No se puede reprogramar a una fecha pasada");
        }

        validarHorarioMedico(
                cita.getMedicoId(),
                nuevaCita.getFecha(),
                nuevaCita.getHora(),
                token
        );

        boolean ocupada = repo.existsByMedicoIdAndFechaAndHoraAndEstadoNot(
                cita.getMedicoId(),
                nuevaCita.getFecha(),
                nuevaCita.getHora(),
                "CANCELADA"
        );

        if (ocupada) {
            throw new RuntimeException("El nuevo horario no está disponible");
        }

        cita.setFecha(nuevaCita.getFecha());
        cita.setHora(nuevaCita.getHora());

        if (nuevaCita.getObservaciones() != null) {
            cita.setObservaciones(nuevaCita.getObservaciones());
        }

        Cita citaReprogramada = repo.save(cita);

        enviarNotificacionReprogramacion(citaReprogramada, token);

        return citaReprogramada;
    }

    private void validarCambioCon24Horas(Cita cita) {

        LocalDateTime fechaHoraCita =
                LocalDateTime.of(
                        cita.getFecha(),
                        cita.getHora()
                );

        LocalDateTime ahora =
                LocalDateTime.now();

        long horasRestantes =
                Duration.between(
                        ahora,
                        fechaHoraCita
                ).toHours();

        if (horasRestantes < 24) {
            throw new RuntimeException("Las citas con menos de 24 horas no admiten cambios");
        }
    }

    private void enviarNotificacionConfirmacion(Cita cita, String token) {
        enviarNotificacionCita(
                cita,
                token,
                "http://localhost:8087/notificaciones/cita-confirmada",
                "confirmación"
        );
    }

    private void enviarNotificacionCancelacion(Cita cita, String token) {
        enviarNotificacionCita(
                cita,
                token,
                "http://localhost:8087/notificaciones/cita-cancelada",
                "cancelación"
        );
    }

    private void enviarNotificacionReprogramacion(Cita cita, String token) {
        enviarNotificacionCita(
                cita,
                token,
                "http://localhost:8087/notificaciones/cita-reprogramada",
                "reprogramación"
        );
    }

    private void enviarNotificacionCita(
            Cita cita,
            String token,
            String urlNotificacion,
            String tipo
    ) {
        try {
            PacienteDto paciente = obtenerPacienteDto(cita.getPacienteId(), token);

            if (paciente == null) {
                System.out.println("No se encontró información del paciente. No se enviará correo de " + tipo + ".");
                return;
            }

            if (paciente.getEmail() == null || paciente.getEmail().isBlank()) {
                System.out.println("El paciente no tiene email registrado. No se enviará correo de " + tipo + ".");
                return;
            }

            MedicoDto medico = obtenerMedicoDto(cita.getMedicoId(), token);
            EspecialidadDto especialidad = obtenerEspecialidadDto(cita.getEspecialidadId(), token);

            String nombrePaciente = paciente.getNombre() + " " + paciente.getApellido();

            String nombreMedico = "No especificado";
            if (medico != null) {
                nombreMedico = medico.getNombre() + " " + medico.getApellido();
            }

            String nombreEspecialidad = "No especificada";
            if (especialidad != null) {
                nombreEspecialidad = especialidad.getNombre();
            }

            CitaNotificacionRequest request = new CitaNotificacionRequest();

            request.setEmailDestino(paciente.getEmail());
            request.setNombrePaciente(nombrePaciente);
            request.setCodigoCita(cita.getCodigoCita());
            request.setNombreMedico(nombreMedico);
            request.setEspecialidad(nombreEspecialidad);
            request.setFecha(String.valueOf(cita.getFecha()));
            request.setHora(String.valueOf(cita.getHora()));
            request.setEstado(cita.getEstado());
            request.setObservaciones(cita.getObservaciones());

            restTemplate.postForObject(
                    urlNotificacion,
                    request,
                    String.class
            );

            System.out.println("Correo de " + tipo + " enviado a: " + paciente.getEmail());

        } catch (Exception e) {
            System.out.println("No se pudo enviar el correo de " + tipo + ": " + e.getMessage());
        }
    }

    private PacienteDto obtenerPacienteDto(Long pacienteId, String token) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        return restTemplate.exchange(
                "http://localhost:8084/pacientes/" + pacienteId,
                HttpMethod.GET,
                entity,
                PacienteDto.class
        ).getBody();
    }

    private MedicoDto obtenerMedicoDto(Long medicoId, String token) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        return restTemplate.exchange(
                "http://localhost:8086/medicos/" + medicoId,
                HttpMethod.GET,
                entity,
                MedicoDto.class
        ).getBody();
    }

    private EspecialidadDto obtenerEspecialidadDto(Long especialidadId, String token) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        return restTemplate.exchange(
                "http://localhost:8086/especialidades/" + especialidadId,
                HttpMethod.GET,
                entity,
                EspecialidadDto.class
        ).getBody();
    }

    private void aplicarPacienteDesdeTokenSiCorresponde(Cita cita, String token) {

        String rol = jwtUtil.extractRol(token);

        if ("PACIENTE".equalsIgnoreCase(rol)) {

            Long pacienteId = jwtUtil.extractPacienteId(token);

            if (pacienteId == null) {
                throw new RuntimeException("El token no tiene paciente asociado");
            }

            cita.setPacienteId(pacienteId);
        }
    }

    private void validarPermisoSobreCita(
            Cita cita,
            String token,
            boolean permitirPaciente
    ) {

        String rol = jwtUtil.extractRol(token);

        if ("ADMIN".equalsIgnoreCase(rol) || "RECEPCIONISTA".equalsIgnoreCase(rol)) {
            return;
        }

        if ("MEDICO".equalsIgnoreCase(rol)) {
            Long medicoId = jwtUtil.extractMedicoId(token);

            if (medicoId != null && medicoId.equals(cita.getMedicoId())) {
                return;
            }
        }

        if (permitirPaciente && "PACIENTE".equalsIgnoreCase(rol)) {
            Long pacienteId = jwtUtil.extractPacienteId(token);

            if (pacienteId != null && pacienteId.equals(cita.getPacienteId())) {
                return;
            }
        }

        throw new RuntimeException("No tiene permisos para realizar esta acción sobre la cita");
    }

    private void validarCamposObligatorios(Cita cita) {

        List<String> errores = new ArrayList<>();

        if (cita.getPacienteId() == null) {
            errores.add("El paciente es obligatorio");
        }

        if (cita.getMedicoId() == null) {
            errores.add("El médico es obligatorio");
        }

        if (cita.getEspecialidadId() == null) {
            errores.add("La especialidad es obligatoria");
        }

        if (cita.getFecha() == null) {
            errores.add("La fecha es obligatoria");
        }

        if (cita.getHora() == null) {
            errores.add("La hora es obligatoria");
        }

        if (!errores.isEmpty()) {
            throw new RuntimeException(String.join(", ", errores));
        }
    }

    private void validarExistencias(Cita cita, String token) {

        List<String> errores = new ArrayList<>();

        if (!existePaciente(cita.getPacienteId(), token)) {
            errores.add("El paciente no existe");
        }

        if (!existeMedico(cita.getMedicoId(), token)) {
            errores.add("El médico no existe");
        }

        if (!existeEspecialidad(cita.getEspecialidadId(), token)) {
            errores.add("La especialidad no existe");
        }

        if (!errores.isEmpty()) {
            throw new RuntimeException(String.join(", ", errores));
        }
    }

    private boolean existePaciente(Long pacienteId, String token) {
        try {
            getConToken("http://localhost:8084/pacientes/" + pacienteId, Object.class, token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean existeMedico(Long medicoId, String token) {
        try {
            getConToken("http://localhost:8086/medicos/" + medicoId, Object.class, token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean existeEspecialidad(Long especialidadId, String token) {
        try {
            getConToken("http://localhost:8086/especialidades/" + especialidadId, Object.class, token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void validarHorarioMedico(
            Long medicoId,
            LocalDate fecha,
            java.time.LocalTime hora,
            String token
    ) {

        String diaSemana = obtenerDiaSemana(fecha);

        String url = "http://localhost:8086/horarios/validar"
                + "?medicoId=" + medicoId
                + "&diaSemana=" + diaSemana
                + "&hora=" + hora;

        Boolean disponible = getConToken(url, Boolean.class, token);

        if (disponible == null || !disponible) {
            throw new RuntimeException("El médico no atiende en ese día u horario");
        }
    }

    private <T> T getConToken(
            String url,
            Class<T> responseType,
            String token
    ) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        return restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                responseType
        ).getBody();
    }

    private String obtenerDiaSemana(LocalDate fecha) {

        DayOfWeek dia = fecha.getDayOfWeek();

        return switch (dia) {
            case MONDAY -> "LUNES";
            case TUESDAY -> "MARTES";
            case WEDNESDAY -> "MIERCOLES";
            case THURSDAY -> "JUEVES";
            case FRIDAY -> "VIERNES";
            case SATURDAY -> "SABADO";
            case SUNDAY -> "DOMINGO";
        };
    }
}