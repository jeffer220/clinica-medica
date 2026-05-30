package com.proyecto.pacientes_service.service;

import com.proyecto.pacientes_service.model.Paciente;
import com.proyecto.pacientes_service.repository.PacienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PacienteService {

    private final PacienteRepository repo;

    public PacienteService(PacienteRepository repo) {
        this.repo = repo;
    }

    public Paciente guardar(Paciente paciente) {

        validarPaciente(paciente);

        return repo.save(paciente);
    }

    public List<Paciente> listar() {
        return repo.findAll();
    }

    public Paciente buscarPorId(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
    }

    public Paciente actualizar(Long id, Paciente paciente) {

        Paciente existente = buscarPorId(id);

        validarPaciente(paciente);

        existente.setNombre(paciente.getNombre());
        existente.setApellido(paciente.getApellido());
        existente.setTelefono(paciente.getTelefono());
        existente.setEmail(paciente.getEmail());
        existente.setFechaNacimiento(paciente.getFechaNacimiento());
        existente.setDireccion(paciente.getDireccion());
        existente.setSeguroMedico(paciente.getSeguroMedico());

        return repo.save(existente);
    }

    public void eliminar(Long id) {
        Paciente paciente = buscarPorId(id);
        repo.delete(paciente);
    }

    private void validarPaciente(Paciente paciente) {

        if (paciente.getNombre() == null || paciente.getNombre().isBlank()) {
            throw new RuntimeException("El nombre del paciente es obligatorio");
        }

        if (paciente.getApellido() == null || paciente.getApellido().isBlank()) {
            throw new RuntimeException("El apellido del paciente es obligatorio");
        }

        if (paciente.getTelefono() == null || paciente.getTelefono().isBlank()) {
            throw new RuntimeException("El teléfono del paciente es obligatorio");
        }

        if (paciente.getEmail() == null || paciente.getEmail().isBlank()) {
            throw new RuntimeException("El email del paciente es obligatorio");
        }

        if (!paciente.getEmail().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            throw new RuntimeException("El email del paciente no tiene un formato válido");
        }

        if (paciente.getFechaNacimiento() == null) {
            throw new RuntimeException("La fecha de nacimiento es obligatoria");
        }

        if (paciente.getDireccion() == null || paciente.getDireccion().isBlank()) {
            throw new RuntimeException("La dirección del paciente es obligatoria");
        }
    }
}