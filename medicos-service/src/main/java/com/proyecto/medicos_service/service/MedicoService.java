package com.proyecto.medicos_service.service;

import com.proyecto.medicos_service.model.Medico;
import com.proyecto.medicos_service.repository.MedicoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MedicoService {

    private final MedicoRepository repo;

    public MedicoService(MedicoRepository repo) {
        this.repo = repo;
    }

    public Medico guardar(Medico medico) {

        if (medico.getNombre() == null || medico.getNombre().isBlank()) {
            throw new RuntimeException("El nombre del médico es obligatorio");
        }

        if (medico.getApellido() == null || medico.getApellido().isBlank()) {
            throw new RuntimeException("El apellido del médico es obligatorio");
        }

        if (medico.getEspecialidad() == null || medico.getEspecialidad().getId() == null) {
            throw new RuntimeException("La especialidad del médico es obligatoria");
        }

        if (medico.getColegiado() == null || medico.getColegiado().isBlank()) {
            throw new RuntimeException("El número de colegiado es obligatorio");
        }

        if (repo.existsByColegiado(medico.getColegiado())) {
            throw new RuntimeException("Ya existe un médico con ese número de colegiado");
        }

        if (medico.getCorreo() != null && !medico.getCorreo().isBlank()) {
            if (repo.existsByCorreo(medico.getCorreo())) {
                throw new RuntimeException("Ya existe un médico con ese correo");
            }
        }

        medico.setActivo(true);

        return repo.save(medico);
    }

    public List<Medico> listar() {
        return repo.findAll();
    }

    public List<Medico> listarActivos() {
        return repo.findByActivoTrue();
    }

    public Medico buscarPorId(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Médico no encontrado"));
    }

    public List<Medico> buscarPorEspecialidad(Long especialidadId) {
        return repo.findByEspecialidad_Id(especialidadId);
    }

    public Medico actualizar(Long id, Medico medico) {

        Medico existente = buscarPorId(id);

        existente.setNombre(medico.getNombre());
        existente.setApellido(medico.getApellido());
        existente.setTelefono(medico.getTelefono());
        existente.setCorreo(medico.getCorreo());
        existente.setEspecialidad(medico.getEspecialidad());
        existente.setColegiado(medico.getColegiado());
        existente.setDireccion(medico.getDireccion());
        existente.setCentroHospitalario(medico.getCentroHospitalario());
        existente.setEdad(medico.getEdad());
        existente.setObservacion(medico.getObservacion());

        return repo.save(existente);
    }

    public Medico desactivar(Long id) {
        Medico medico = buscarPorId(id);
        medico.setActivo(false);
        return repo.save(medico);
    }

    public Medico activar(Long id) {
        Medico medico = buscarPorId(id);
        medico.setActivo(true);
        return repo.save(medico);
    }

    public void eliminar(Long id) {
        Medico medico = buscarPorId(id);
        repo.delete(medico);
    }
}