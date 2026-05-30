package com.proyecto.notificaciones_service.service;

import com.proyecto.notificaciones_service.dto.CitaNotificacionRequest;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarConfirmacionCita(CitaNotificacionRequest request) {

        validarRequest(request);
        
        SimpleMailMessage mensaje = new SimpleMailMessage();

        mensaje.setTo(request.getEmailDestino());
        mensaje.setSubject("Confirmación de cita médica - Hospital La Bendición");

        mensaje.setText(
                "Hospital La Bendición\n\n" +
                "Estimado/a " + valorSeguro(request.getNombrePaciente()) + ",\n\n" +
                "Su cita médica ha sido registrada correctamente.\n\n" +
                "Datos de la cita:\n" +
                "Código de cita: " + valorSeguro(request.getCodigoCita()) + "\n" +
                "Médico: " + valorSeguro(request.getNombreMedico()) + "\n" +
                "Especialidad: " + valorSeguro(request.getEspecialidad()) + "\n" +
                "Fecha: " + valorSeguro(request.getFecha()) + "\n" +
                "Hora: " + valorSeguro(request.getHora()) + "\n" +
                "Estado: " + valorSeguro(request.getEstado()) + "\n" +
                "Observaciones: " + valorSeguro(request.getObservaciones()) + "\n\n" +
                "Por favor, preséntese unos minutos antes de la hora programada.\n\n" +
                "Gracias por utilizar el sistema del Hospital La Bendición."
        );

        mailSender.send(mensaje);
    }

    public void enviarCancelacionCita(CitaNotificacionRequest request) {

        validarRequest(request);

        SimpleMailMessage mensaje = new SimpleMailMessage();

        mensaje.setTo(request.getEmailDestino());
        mensaje.setSubject("Cancelación de cita médica - Hospital La Bendición");

        mensaje.setText(
                "Hospital La Bendición\n\n" +
                "Estimado/a " + valorSeguro(request.getNombrePaciente()) + ",\n\n" +
                "Le informamos que su cita médica ha sido cancelada.\n\n" +
                "Datos de la cita:\n" +
                "Código de cita: " + valorSeguro(request.getCodigoCita()) + "\n" +
                "Médico: " + valorSeguro(request.getNombreMedico()) + "\n" +
                "Especialidad: " + valorSeguro(request.getEspecialidad()) + "\n" +
                "Fecha: " + valorSeguro(request.getFecha()) + "\n" +
                "Hora: " + valorSeguro(request.getHora()) + "\n" +
                "Estado: " + valorSeguro(request.getEstado()) + "\n\n" +
                "Si necesita una nueva cita, puede solicitarla nuevamente."
        );

        mailSender.send(mensaje);
    }

    public void enviarReprogramacionCita(CitaNotificacionRequest request) {

        validarRequest(request);

        SimpleMailMessage mensaje = new SimpleMailMessage();

        mensaje.setTo(request.getEmailDestino());
        mensaje.setSubject("Reprogramación de cita médica - Hospital La Bendición");

        mensaje.setText(
                "Hospital La Bendición\n\n" +
                "Estimado/a " + valorSeguro(request.getNombrePaciente()) + ",\n\n" +
                "Su cita médica ha sido reprogramada.\n\n" +
                "Nuevos datos de la cita:\n" +
                "Código de cita: " + valorSeguro(request.getCodigoCita()) + "\n" +
                "Médico: " + valorSeguro(request.getNombreMedico()) + "\n" +
                "Especialidad: " + valorSeguro(request.getEspecialidad()) + "\n" +
                "Fecha: " + valorSeguro(request.getFecha()) + "\n" +
                "Hora: " + valorSeguro(request.getHora()) + "\n" +
                "Estado: " + valorSeguro(request.getEstado()) + "\n" +
                "Observaciones: " + valorSeguro(request.getObservaciones()) + "\n\n" +
                "Por favor, tome nota de la nueva fecha y hora asignadas."
        );

        mailSender.send(mensaje);
    }

    private void validarRequest(CitaNotificacionRequest request) {

        if (request.getEmailDestino() == null || request.getEmailDestino().isBlank()) {
            throw new RuntimeException("El email destino es obligatorio");
        }

        if (!request.getEmailDestino().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            throw new RuntimeException("El email destino no tiene un formato válido");
        }
    }

    private String valorSeguro(String valor) {
        return valor == null || valor.isBlank()
                ? "No especificado"
                : valor;
    }
}