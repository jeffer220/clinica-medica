package com.proyecto.auth.auth_service.controller;

import com.proyecto.auth.auth_service.dto.AuthResponse;
import com.proyecto.auth.auth_service.dto.LoginRequest;
import com.proyecto.auth.auth_service.dto.RegisterRequest;
import com.proyecto.auth.auth_service.dto.UsuarioDto;
import com.proyecto.auth.auth_service.model.Usuario;
import com.proyecto.auth.auth_service.service.AuthService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public Usuario registrar(@RequestBody RegisterRequest request) {
        return service.registrar(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return service.login(request);
    }

  @PreAuthorize("hasRole('ADMIN')")
@GetMapping("/usuarios")
public List<UsuarioDto> listarUsuarios() {
    return service.listarUsuarios();
}
}