package ua.edu.viti.military.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ua.edu.viti.military.dto.request.SupplyCategoryCreateDTO;
import ua.edu.viti.military.dto.response.SupplyCategoryResponseDTO;
import ua.edu.viti.military.service.SupplyCategoryService;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/supply-categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Supply Categories", description = "API для керування категоріями матеріальних засобів")
@Validated
public class SupplyCategoryController {

    private final SupplyCategoryService service;

    @PostMapping
    @Operation(summary = "Створити категорію матеріальних засобів")
    public ResponseEntity<SupplyCategoryResponseDTO> create(@Valid @RequestBody SupplyCategoryCreateDTO dto) {
        log.info("REST create supply category: {}", dto);
        SupplyCategoryResponseDTO created = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Отримати категорію за id")
    public ResponseEntity<SupplyCategoryResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    @Operation(summary = "Отримати всі категорії")
    public ResponseEntity<List<SupplyCategoryResponseDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Видалити категорію")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
