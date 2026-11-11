package com.folium.backend.web.controllers;

import com.folium.backend.dto.request.RegisterRequest;
import com.folium.backend.dto.request.LoginRequest;
import com.folium.backend.dto.response.LoginResponse;
import com.folium.backend.dto.response.RegisterResponse;
import com.folium.backend.model.domain.User;
import com.folium.backend.repository.UserRepository;
import com.folium.backend.service.application.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match");
        }

        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }

        User user = new User();
        user.setName(request.name().trim());
        user.setSurname(request.surname().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));

        User savedUser = userRepository.save(user);
        return new RegisterResponse(savedUser.getId(), savedUser.getName(), savedUser.getSurname(), savedUser.getEmail());
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        User user = userRepository.findByEmail(email)
                .filter(existingUser -> existingUser.getPassword() != null
                        && passwordEncoder.matches(request.password(), existingUser.getPassword()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        return new LoginResponse(
                jwtService.createToken(user),
                "Bearer",
                user.getId(),
                user.getName(),
                user.getSurname(),
                user.getEmail());
    }
}
