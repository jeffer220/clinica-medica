package com.proyecto.medicos_service.controller;

import com.proyecto.medicos_service.model.Especialidad;
import com.proyecto.medicos_service.service.EspecialidadService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/especialidades")
public class EspecialidadController {

    private final EspecialidadService service;

    public EspecialidadController(EspecialidadService service) {
        this.service = service;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public Especialidad guardar(@RequestBody Especialidad especialidad) {
        return service.guardar(especialidad);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA', 'PACIENTE', 'MEDICO')")
    @GetMapping
    public List<Especialidad> listar() {
        return service.listar();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA', 'PACIENTE', 'MEDICO')")
    @GetMapping("/{id}")
    public Especialidad buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }
}

/* 
import com.proyecto.medicos_service.model.Especialidad;
import com.proyecto.medicos_service.service.EspecialidadService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/especialidades")
public class EspecialidadController {

    private final EspecialidadService service;

    public EspecialidadController(EspecialidadService service) {
        this.service = service;
    }

    @PostMapping
    public Especialidad guardar(@RequestBody Especialidad especialidad) {
        return service.guardar(especialidad);
    }

    @GetMapping
    public List<Especialidad> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Especialidad buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }
}*/