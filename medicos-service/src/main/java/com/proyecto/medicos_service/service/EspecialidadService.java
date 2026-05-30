package com.proyecto.medicos_service.service;

import com.proyecto.medicos_service.model.Especialidad;
import com.proyecto.medicos_service.repository.EspecialidadRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EspecialidadService {

    private final EspecialidadRepository repo;

    public EspecialidadService(EspecialidadRepository repo) {
        this.repo = repo;
    }

    public Especialidad guardar(Especialidad especialidad) {

        if (especialidad.getNombre() == null || especialidad.getNombre().isBlank()) {
            throw new RuntimeException("El nombre de la especialidad es obligatorio");
        }

        return repo.save(especialidad);
    }

    public List<Especialidad> listar() {
        return repo.findAll();
    }

    public Especialidad buscarPorId(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Especialidad no encontrada"));
    }
}