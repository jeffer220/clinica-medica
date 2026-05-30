package com.proyecto.auth.auth_service.service;

import com.proyecto.auth.auth_service.dto.AuthData;
import com.proyecto.auth.auth_service.dto.AuthResponse;
import com.proyecto.auth.auth_service.dto.LoginRequest;
import com.proyecto.auth.auth_service.dto.RegisterRequest;
import com.proyecto.auth.auth_service.dto.UsuarioDto;
import com.proyecto.auth.auth_service.model.Rol;
import com.proyecto.auth.auth_service.model.Usuario;
import com.proyecto.auth.auth_service.repository.RolRepository;
import com.proyecto.auth.auth_service.repository.UsuarioRepository;
import com.proyecto.auth.auth_service.security.JwtUtil;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil
    ) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public Usuario registrar(RegisterRequest request) {

        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new RuntimeException("El username es obligatorio");
        }

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new RuntimeException("El email es obligatorio");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new RuntimeException("La contraseña es obligatoria");
        }

        if (request.getRol() == null || request.getRol().isBlank()) {
            throw new RuntimeException("El rol es obligatorio");
        }

        if (usuarioRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Ya existe un usuario con ese username");
        }

        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Ya existe un usuario con ese email");
        }

        Rol rol = rolRepository.findByNombre(request.getRol().toUpperCase())
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        Usuario usuario = new Usuario();
        usuario.setUsername(request.getUsername());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setRol(rol);
        usuario.setActivo(true);
        usuario.setPacienteId(request.getPacienteId());
        usuario.setMedicoId(request.getMedicoId());

        return usuarioRepository.save(usuario);
    }

    public AuthResponse login(LoginRequest request) {

        if (request.getEmail() == null || request.getEmail().isBlank()
                || request.getPassword() == null || request.getPassword().isBlank()) {
            throw new RuntimeException("BAD_REQUEST: Los campos email y password son obligatorios.");
        }

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("UNAUTHORIZED: Correo o contraseña incorrectos."));

        if (usuario.getActivo() != null && !usuario.getActivo()) {
            throw new RuntimeException("FORBIDDEN: La cuenta se encuentra inactiva. Contacte a soporte.");
        }

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new RuntimeException("UNAUTHORIZED: Correo o contraseña incorrectos.");
        }

        String token = jwtUtil.generarToken(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getRol().getNombre(),
                usuario.getPacienteId(),
                usuario.getMedicoId()
        );

        UsuarioDto usuarioDto = new UsuarioDto(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getRol().getNombre(),
                usuario.getPacienteId(),
                usuario.getMedicoId()
        );

        AuthData data = new AuthData(
                token,
                "Bearer",
                3600,
                usuarioDto
        );

        return new AuthResponse(
                "success",
                "Autenticación exitosa",
                data
        );
    }

    public List<UsuarioDto> listarUsuarios() {

        return usuarioRepository.findAll()
                .stream()
                .map(usuario -> new UsuarioDto(
                        usuario.getId(),
                        usuario.getEmail(),
                        usuario.getRol().getNombre(),
                        usuario.getPacienteId(),
                        usuario.getMedicoId()
                ))
                .toList();
    }
}