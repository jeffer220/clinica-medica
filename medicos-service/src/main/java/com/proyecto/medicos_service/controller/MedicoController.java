package com.proyecto.medicos_service.controller;

import com.proyecto.medicos_service.model.Medico;
import com.proyecto.medicos_service.service.MedicoService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/medicos")
public class MedicoController {

    private final MedicoService service;

    public MedicoController(MedicoService service) {
        this.service = service;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public Medico guardar(@RequestBody Medico medico) {
        return service.guardar(medico);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    @GetMapping
    public List<Medico> listar() {
        return service.listar();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA', 'PACIENTE', 'MEDICO')")
    @GetMapping("/activos")
    public List<Medico> listarActivos() {
        return service.listarActivos();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA', 'PACIENTE', 'MEDICO')")
    @GetMapping("/{id}")
    public Medico buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA', 'PACIENTE', 'MEDICO')")
    @GetMapping("/especialidad/{especialidadId}")
    public List<Medico> buscarPorEspecialidad(@PathVariable Long especialidadId) {
        return service.buscarPorEspecialidad(especialidadId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public Medico actualizar(
            @PathVariable Long id,
            @RequestBody Medico medico
    ) {
        return service.actualizar(id, medico);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/desactivar")
    public Medico desactivar(@PathVariable Long id) {
        return service.desactivar(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/activar")
    public Medico activar(@PathVariable Long id) {
        return service.activar(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public String eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return "Médico eliminado correctamente";
    }
}

/* 
import com.proyecto.medicos_service.model.Medico;
import com.proyecto.medicos_service.service.MedicoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/medicos")
public class MedicoController {

    private final MedicoService service;

    public MedicoController(MedicoService service) {
        this.service = service;
    }

    @PostMapping
    public Medico guardar(@RequestBody Medico medico) {
        return service.guardar(medico);
    }

    @GetMapping
    public List<Medico> listar() {
        return service.listar();
    }

    @GetMapping("/activos")
    public List<Medico> listarActivos() {
        return service.listarActivos();
    }

    @GetMapping("/{id}")
    public Medico buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @GetMapping("/especialidad/{especialidadId}")
    public List<Medico> buscarPorEspecialidad(@PathVariable Long especialidadId) {
        return service.buscarPorEspecialidad(especialidadId);
    }

    @PutMapping("/{id}")
    public Medico actualizar(
            @PathVariable Long id,
            @RequestBody Medico medico
    ) {
        return service.actualizar(id, medico);
    }

    @PutMapping("/{id}/desactivar")
    public Medico desactivar(@PathVariable Long id) {
        return service.desactivar(id);
    }

    @PutMapping("/{id}/activar")
    public Medico activar(@PathVariable Long id) {
        return service.activar(id);
    }

    @DeleteMapping("/{id}")
    public String eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return "Médico eliminado correctamente";
    }
}*/