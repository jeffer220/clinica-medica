package com.proyecto.historial_medico_service.controller;


import com.proyecto.historial_medico_service.dto.HistorialResponse;
import com.proyecto.historial_medico_service.dto.RegistroClinicoRequest;
import com.proyecto.historial_medico_service.dto.RegistroClinicoResponse;
import com.proyecto.historial_medico_service.service.HistorialMedicoService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/historial-medico")
public class HistorialMedicoController {

    private final HistorialMedicoService service;

    public HistorialMedicoController(HistorialMedicoService service) {
        this.service = service;
    }

    @PreAuthorize("hasAnyRole('PACIENTE', 'MEDICO', 'ADMIN', 'RECEPCIONISTA')")
    @GetMapping("/pacientes/{pacienteId}/registros")
    public HistorialResponse consultarHistorial(
            @PathVariable Long pacienteId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "desc") String sort,
            HttpServletRequest request
    ) {
        return service.consultarHistorial(
                pacienteId,
                page,
                limit,
                sort,
                obtenerToken(request)
        );
    }

    private String obtenerToken(HttpServletRequest request) {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            throw new RuntimeException("UNAUTHORIZED: Sesión inválida o expirada.");
        }

        return header.substring(7);
    }



    @PreAuthorize("hasAnyRole('MEDICO', 'ADMIN')")
@PostMapping("/registros")
public RegistroClinicoResponse registrarAtencion(
        @RequestBody RegistroClinicoRequest request,
        HttpServletRequest httpRequest
) {
    return service.registrarAtencion(
            request,
            obtenerToken(httpRequest)
    );
}
}