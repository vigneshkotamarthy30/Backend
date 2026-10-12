package com.hotel.controller;

import com.hotel.dto.AuthResponse;
import com.hotel.dto.LoginRequest;
import com.hotel.dto.RegisterRequest;
import com.hotel.dto.UserDto;
import com.hotel.entity.User;
import com.hotel.exception.ValidationException;
import com.hotel.security.JwtService;
import com.hotel.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, UserService userService, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.registerUser(
            request.getFirstName(),
            request.getLastName(),
            request.getEmail(),
            request.getPassword(),
            request.getPhone());
        String token = jwtService.generateToken(user.getEmail());
        return ResponseEntity.ok(new AuthResponse(token, userService.toDto(user)));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = userService.findByEmail(request.getEmail())
            .orElseThrow(() -> new ValidationException("Invalid credentials."));
        String token = jwtService.generateToken(user.getEmail());
        return ResponseEntity.ok(new AuthResponse(token, userService.toDto(user)));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> me() {
        User user = userService.getCurrentUser();
        return ResponseEntity.ok(userService.toDto(user));
    }
}
