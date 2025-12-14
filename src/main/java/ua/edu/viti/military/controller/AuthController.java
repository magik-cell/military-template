package ua.edu.viti.military.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.edu.viti.military.dto.request.LoginRequestDTO;
import ua.edu.viti.military.dto.request.RegisterRequestDTO;
import ua.edu.viti.military.dto.response.JwtResponseDTO;
import ua.edu.viti.military.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "API для автентифікації та реєстрації")
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/login")
    @Operation(summary = "Вхід в систему", description = "Автентифікація користувача та отримання JWT token")
    public ResponseEntity<JwtResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        JwtResponseDTO response = authService.login(dto);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/register")
    @Operation(summary = "Реєстрація", description = "Створення нового користувача")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequestDTO dto) {
        String message = authService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }
}
