package com.proyecto.citas_service.controller;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.proyecto.citas_service.model.Cita;
import com.proyecto.citas_service.service.CitaService;

@RestController
@RequestMapping("/citas")
public class CitaController {

    @Autowired
    private CitaService service;

    @PreAuthorize("hasAnyRole('PACIENTE', 'ADMIN', 'RECEPCIONISTA')")
    @PostMapping
    public Cita guardar(
            @RequestBody Cita cita,
            HttpServletRequest request
    ) {
        return service.guardar(cita, obtenerToken(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    @GetMapping
    public List<Cita> listar() {
        return service.listar();
    }

    @PreAuthorize("hasRole('PACIENTE')")
    @GetMapping("/mis-citas")
    public List<Cita> listarMisCitas(HttpServletRequest request) {
        return service.listarMisCitas(obtenerToken(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    @GetMapping("/paciente/{pacienteId}")
    public List<Cita> listarPorPaciente(@PathVariable Long pacienteId) {
        return service.listarPorPaciente(pacienteId);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA', 'MEDICO')")
    @GetMapping("/medico/{medicoId}")
    public List<Cita> listarPorMedico(@PathVariable Long medicoId) {
        return service.listarPorMedico(medicoId);
    }

    // Este endpoint lo usará historial-medico-service
    // para verificar si el médico tuvo cita con ese paciente.
    @PreAuthorize("hasAnyRole('MEDICO', 'ADMIN', 'RECEPCIONISTA')")
    @GetMapping("/validar-relacion")
    public boolean validarRelacionMedicoPaciente(
            @RequestParam Long medicoId,
            @RequestParam Long pacienteId,
            HttpServletRequest request
    ) {
        return service.validarRelacionMedicoPaciente(
                medicoId,
                pacienteId,
                obtenerToken(request)
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA', 'MEDICO')")
    @GetMapping("/{id}")
    public Cita buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PreAuthorize("hasAnyRole('PACIENTE', 'ADMIN', 'RECEPCIONISTA')")
    @PutMapping("/{id}/cancelar")
    public Cita cancelar(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        return service.cancelar(id, obtenerToken(request));
    }

    @PreAuthorize("hasAnyRole('MEDICO', 'ADMIN')")
    @PutMapping("/{id}/atender")
    public Cita marcarComoAtendida(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        return service.marcarComoAtendida(id, obtenerToken(request));
    }

    @PreAuthorize("hasAnyRole('PACIENTE', 'ADMIN', 'RECEPCIONISTA')")
    @PutMapping("/{id}/reprogramar")
    public Cita reprogramar(
            @PathVariable Long id,
            @RequestBody Cita nuevaCita,
            HttpServletRequest request
    ) {
        return service.reprogramar(id, nuevaCita, obtenerToken(request));
    }

    private String obtenerToken(HttpServletRequest request) {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            throw new RuntimeException("Token no enviado");
        }

        return header.substring(7);
    }
}