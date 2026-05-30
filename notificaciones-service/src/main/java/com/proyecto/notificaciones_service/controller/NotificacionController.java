package com.proyecto.notificaciones_service.controller;

import com.proyecto.notificaciones_service.dto.CitaNotificacionRequest;
import com.proyecto.notificaciones_service.service.EmailService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notificaciones")
public class NotificacionController {

    private final EmailService emailService;

    public NotificacionController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/cita-confirmada")
    public ResponseEntity<String> enviarConfirmacionCita(
            @RequestBody CitaNotificacionRequest request
    ) {
        emailService.enviarConfirmacionCita(request);
        return ResponseEntity.ok("Correo de confirmación enviado correctamente");
    }

    @PostMapping("/cita-cancelada")
    public ResponseEntity<String> enviarCancelacionCita(
            @RequestBody CitaNotificacionRequest request
    ) {
        emailService.enviarCancelacionCita(request);
        return ResponseEntity.ok("Correo de cancelación enviado correctamente");
    }

    @PostMapping("/cita-reprogramada")
    public ResponseEntity<String> enviarReprogramacionCita(
            @RequestBody CitaNotificacionRequest request
    ) {
        emailService.enviarReprogramacionCita(request);
        return ResponseEntity.ok("Correo de reprogramación enviado correctamente");
    }
}
