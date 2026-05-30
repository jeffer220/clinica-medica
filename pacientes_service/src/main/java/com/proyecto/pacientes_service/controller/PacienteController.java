package com.proyecto.pacientes_service.controller;

import com.proyecto.pacientes_service.model.Paciente;
import com.proyecto.pacientes_service.service.PacienteService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pacientes")
public class PacienteController {

    private final PacienteService service;

    public PacienteController(PacienteService service) {
        this.service = service;
    }

    // Se deja público porque lo usa /registro-usuario antes de iniciar sesión.
    @PostMapping
    public Paciente guardar(@RequestBody Paciente paciente) {
        return service.guardar(paciente);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    @GetMapping
    public List<Paciente> listar() {
        return service.listar();
    }

    // Se agrega MEDICO para que pueda consultar el nombre del paciente asignado a su cita.
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA', 'PACIENTE', 'MEDICO')")
    @GetMapping("/{id}")
    public Paciente buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
    @PutMapping("/{id}")
    public Paciente actualizar(
            @PathVariable Long id,
            @RequestBody Paciente paciente
    ) {
        return service.actualizar(id, paciente);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public String eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return "Paciente eliminado correctamente";
    }
}


/* 
import com.proyecto.pacientes_service.model.Paciente;
import com.proyecto.pacientes_service.service.PacienteService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pacientes")
public class PacienteController {

    private final PacienteService service;

    public PacienteController(PacienteService service) {
        this.service = service;
    }

    @PostMapping
    public Paciente guardar(@RequestBody Paciente paciente) {
        return service.guardar(paciente);
    }

    @GetMapping
    public List<Paciente> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Paciente buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public Paciente actualizar(
            @PathVariable Long id,
            @RequestBody Paciente paciente
    ) {
        return service.actualizar(id, paciente);
    }

    @DeleteMapping("/{id}")
    public String eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return "Paciente eliminado correctamente";
    }
}*/