package com.proyecto.historial_medico_service.service;

import com.proyecto.historial_medico_service.dto.*;
import com.proyecto.historial_medico_service.model.RegistroClinico;
import com.proyecto.historial_medico_service.repository.RegistroClinicoRepository;
import com.proyecto.historial_medico_service.security.JwtUtil;

import org.springframework.data.domain.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class HistorialMedicoService {

    private final RegistroClinicoRepository repo;
    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate;

    public HistorialMedicoService(
            RegistroClinicoRepository repo,
            JwtUtil jwtUtil,
            RestTemplate restTemplate
    ) {
        this.repo = repo;
        this.jwtUtil = jwtUtil;
        this.restTemplate = restTemplate;
    }

    public HistorialResponse consultarHistorial(
            Long pacienteId,
            int page,
            int limit,
            String sort,
            String token
    ) {
        validarPaginacion(page, limit);
        validarPermiso(pacienteId, token);

        Sort.Direction direction =
                "asc".equalsIgnoreCase(sort)
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(
                page - 1,
                limit,
                Sort.by(direction, "fechaConsulta")
        );

        Page<RegistroClinico> pagina =
                repo.findByPacienteId(pacienteId, pageable);

        List<RegistroClinicoResponse> registros =
                pagina.getContent()
                        .stream()
                        .map(this::mapearRegistro)
                        .toList();

        PaginacionDto paginacion = new PaginacionDto(
                pagina.getTotalElements(),
                page,
                pagina.getTotalPages()
        );

        HistorialResponse.DataHistorial data =
                new HistorialResponse.DataHistorial(
                        pacienteId,
                        paginacion,
                        registros
                );

        return new HistorialResponse(
                "success",
                data
        );
    }

    private void validarPaginacion(int page, int limit) {

        if (page <= 0 || limit <= 0) {
            throw new RuntimeException("BAD_REQUEST: Parámetros de paginación inválidos.");
        }

        if (limit > 100) {
            throw new RuntimeException("BAD_REQUEST: El límite máximo permitido es 100.");
        }
    }

    private void validarPermiso(Long pacienteId, String token) {

        String rol = jwtUtil.extractRol(token);

        if ("ADMIN".equalsIgnoreCase(rol) || "RECEPCIONISTA".equalsIgnoreCase(rol)) {
            return;
        }

        if ("PACIENTE".equalsIgnoreCase(rol)) {

            Long pacienteIdToken = jwtUtil.extractPacienteId(token);

            if (pacienteIdToken != null && pacienteIdToken.equals(pacienteId)) {
                return;
            }

            throw new RuntimeException("FORBIDDEN: Acceso denegado a los registros médicos de este paciente.");
        }

        if ("MEDICO".equalsIgnoreCase(rol)) {

            Long medicoIdToken = jwtUtil.extractMedicoId(token);

            if (medicoIdToken == null) {
                throw new RuntimeException("FORBIDDEN: Médico no asociado al usuario.");
            }

            boolean tieneRelacion = medicoTieneCitaConPaciente(
                    medicoIdToken,
                    pacienteId,
                    token
            );

            if (tieneRelacion) {
                return;
            }

            throw new RuntimeException("FORBIDDEN: El médico no tiene citas registradas con este paciente.");
        }

        throw new RuntimeException("FORBIDDEN: No tiene permisos para consultar historial médico.");
    }

    private boolean medicoTieneCitaConPaciente(
            Long medicoId,
            Long pacienteId,
            String token
    ) {
        try {
            String url = "http://localhost:8085/citas/validar-relacion"
                    + "?medicoId=" + medicoId
                    + "&pacienteId=" + pacienteId;

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            Boolean respuesta = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Boolean.class
            ).getBody();

            return Boolean.TRUE.equals(respuesta);

        } catch (Exception e) {
            throw new RuntimeException("SERVICE_UNAVAILABLE: No se pudo validar la relación médico-paciente con citas-service.");
        }
    }

    private RegistroClinicoResponse mapearRegistro(RegistroClinico r) {

        MedicoHistorialDto medico = new MedicoHistorialDto(
                r.getMedicoId(),
                "Médico ID: " + r.getMedicoId(),
                "Especialidad ID: " + r.getEspecialidadId()
        );

        return new RegistroClinicoResponse(
                r.getId(),
                String.valueOf(r.getFechaConsulta()),
                medico,
                r.getDiagnosticoPrincipal(),
                r.getRecetaMedica(),
                r.getObservaciones(),
                r.getResultadoLaboratorio()
        );
    }


    public RegistroClinicoResponse registrarAtencion(
        RegistroClinicoRequest request,
        String token
) {
    String rol = jwtUtil.extractRol(token);

    if (!"MEDICO".equalsIgnoreCase(rol) && !"ADMIN".equalsIgnoreCase(rol)) {
        throw new RuntimeException("FORBIDDEN: Solo un médico o administrador puede registrar atención médica.");
    }

    if ("MEDICO".equalsIgnoreCase(rol)) {
        Long medicoIdToken = jwtUtil.extractMedicoId(token);

        if (medicoIdToken == null) {
            throw new RuntimeException("FORBIDDEN: Médico no asociado al usuario.");
        }

        if (!medicoIdToken.equals(request.getMedicoId())) {
            throw new RuntimeException("FORBIDDEN: No puede registrar atención de otro médico.");
        }
    }

    if (request.getPacienteId() == null) {
        throw new RuntimeException("BAD_REQUEST: El paciente es obligatorio.");
    }

    if (request.getMedicoId() == null) {
        throw new RuntimeException("BAD_REQUEST: El médico es obligatorio.");
    }

    if (request.getEspecialidadId() == null) {
        throw new RuntimeException("BAD_REQUEST: La especialidad es obligatoria.");
    }

    if (request.getCitaId() == null) {
        throw new RuntimeException("BAD_REQUEST: La cita es obligatoria.");
    }

    if (request.getDiagnosticoPrincipal() == null || request.getDiagnosticoPrincipal().isBlank()) {
        throw new RuntimeException("BAD_REQUEST: El diagnóstico principal es obligatorio.");
    }

    if (repo.existsByCitaId(request.getCitaId())) {
        throw new RuntimeException("BAD_REQUEST: Esta cita ya tiene un registro clínico asociado.");
    }

    RegistroClinico registro = new RegistroClinico();

    registro.setPacienteId(request.getPacienteId());
    registro.setMedicoId(request.getMedicoId());
    registro.setEspecialidadId(request.getEspecialidadId());
    registro.setCitaId(request.getCitaId());

    if (request.getFechaConsulta() != null) {
        registro.setFechaConsulta(request.getFechaConsulta());
    } else {
        registro.setFechaConsulta(java.time.LocalDateTime.now());
    }

    registro.setDiagnosticoPrincipal(request.getDiagnosticoPrincipal());
    registro.setRecetaMedica(request.getRecetaMedica());
    registro.setObservaciones(request.getObservaciones());
    registro.setResultadoLaboratorio(request.getResultadoLaboratorio());

    RegistroClinico guardado = repo.save(registro);

    return mapearRegistro(guardado);
}
}