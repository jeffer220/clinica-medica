package com.proyecto.medicos_service.controller;

import com.proyecto.medicos_service.model.HorarioMedico;
import com.proyecto.medicos_service.service.HorarioMedicoService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/horarios")
public class HorarioMedicoController {

    private final HorarioMedicoService service;

    public HorarioMedicoController(HorarioMedicoService service) {
        this.service = service;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    @PostMapping
    public HorarioMedico guardar(@RequestBody HorarioMedico horario) {
        return service.guardar(horario);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    @GetMapping
    public List<HorarioMedico> listar() {
        return service.listar();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'RECEPCIONISTA', 'PACIENTE')")
    @GetMapping("/{id}")
    public HorarioMedico buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'RECEPCIONISTA', 'PACIENTE')")
    @GetMapping("/medico/{medicoId}")
    public List<HorarioMedico> listarPorMedico(@PathVariable Long medicoId) {
        return service.listarPorMedico(medicoId);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'RECEPCIONISTA', 'PACIENTE')")
    @GetMapping("/medico/{medicoId}/disponibles")
    public List<HorarioMedico> listarDisponiblesPorMedico(@PathVariable Long medicoId) {
        return service.listarDisponiblesPorMedico(medicoId);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'RECEPCIONISTA', 'PACIENTE')")
    @GetMapping("/medico/{medicoId}/dia/{diaSemana}")
    public List<HorarioMedico> listarDisponiblesPorMedicoYDia(
            @PathVariable Long medicoId,
            @PathVariable String diaSemana
    ) {
        return service.listarDisponiblesPorMedicoYDia(medicoId, diaSemana);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    @PutMapping("/{id}")
    public HorarioMedico actualizar(
            @PathVariable Long id,
            @RequestBody HorarioMedico horario
    ) {
        return service.actualizar(id, horario);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    @PutMapping("/{id}/desactivar")
    public HorarioMedico desactivar(@PathVariable Long id) {
        return service.desactivar(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    @PutMapping("/{id}/activar")
    public HorarioMedico activar(@PathVariable Long id) {
        return service.activar(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public String eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return "Horario eliminado correctamente";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO', 'RECEPCIONISTA', 'PACIENTE')")
    @GetMapping("/validar")
    public boolean validarDisponibilidad(
            @RequestParam Long medicoId,
            @RequestParam String diaSemana,
            @RequestParam String hora
    ) {
        return service.validarDisponibilidad(
                medicoId,
                diaSemana,
                java.time.LocalTime.parse(hora)
        );
    }
}

/* 
import com.proyecto.medicos_service.model.HorarioMedico;
import com.proyecto.medicos_service.service.HorarioMedicoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/horarios")
public class HorarioMedicoController {

    private final HorarioMedicoService service;

    public HorarioMedicoController(HorarioMedicoService service) {
        this.service = service;
    }

    @PostMapping
    public HorarioMedico guardar(@RequestBody HorarioMedico horario) {
        return service.guardar(horario);
    }

    @GetMapping
    public List<HorarioMedico> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public HorarioMedico buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @GetMapping("/medico/{medicoId}")
    public List<HorarioMedico> listarPorMedico(@PathVariable Long medicoId) {
        return service.listarPorMedico(medicoId);
    }

    @GetMapping("/medico/{medicoId}/disponibles")
    public List<HorarioMedico> listarDisponiblesPorMedico(@PathVariable Long medicoId) {
        return service.listarDisponiblesPorMedico(medicoId);
    }

    @GetMapping("/medico/{medicoId}/dia/{diaSemana}")
    public List<HorarioMedico> listarDisponiblesPorMedicoYDia(
            @PathVariable Long medicoId,
            @PathVariable String diaSemana
    ) {
        return service.listarDisponiblesPorMedicoYDia(medicoId, diaSemana);
    }

    @PutMapping("/{id}")
    public HorarioMedico actualizar(
            @PathVariable Long id,
            @RequestBody HorarioMedico horario
    ) {
        return service.actualizar(id, horario);
    }

    @PutMapping("/{id}/desactivar")
    public HorarioMedico desactivar(@PathVariable Long id) {
        return service.desactivar(id);
    }

    @PutMapping("/{id}/activar")
    public HorarioMedico activar(@PathVariable Long id) {
        return service.activar(id);
    }

    @DeleteMapping("/{id}")
    public String eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return "Horario eliminado correctamente";
    }


    @GetMapping("/validar")
public boolean validarDisponibilidad(
        @RequestParam Long medicoId,
        @RequestParam String diaSemana,
        @RequestParam String hora
) {
    return service.validarDisponibilidad(
            medicoId,
            diaSemana,
            java.time.LocalTime.parse(hora)
    );
}
}*/