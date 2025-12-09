package ua.edu.viti.military.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ua.edu.viti.military.dto.request.UnitTypeCreateDTO;
import ua.edu.viti.military.dto.request.UnitTypeUpdateDTO;
import ua.edu.viti.military.dto.response.UnitTypeResponseDTO;
import ua.edu.viti.military.service.UnitTypeService;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/unit-types")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Unit Types", description = "API для керування типами підрозділів")
@Validated
public class UnitTypeController {

    private final UnitTypeService unitTypeService;

    @PostMapping
    @Operation(summary = "Створити тип підрозділу")
    public ResponseEntity<UnitTypeResponseDTO> create(
            @Valid @RequestBody UnitTypeCreateDTO createDTO) {
        log.info("REST request to create UnitType: {}", createDTO);
        UnitTypeResponseDTO created = unitTypeService.create(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Отримати тип підрозділу за ID")
    public ResponseEntity<UnitTypeResponseDTO> getById(@PathVariable Long id) {
        log.info("REST request to get UnitType by id: {}", id);
        return ResponseEntity.ok(unitTypeService.getById(id));
    }

    @GetMapping
    @Operation(summary = "Отримати всі типи підрозділів")
    public ResponseEntity<List<UnitTypeResponseDTO>> getAll() {
        log.info("REST request to get all UnitTypes");
        return ResponseEntity.ok(unitTypeService.getAll());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Оновити тип підрозділу")
    public ResponseEntity<UnitTypeResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody UnitTypeUpdateDTO updateDTO) {
        log.info("REST request to update UnitType {}: {}", id, updateDTO);
        return ResponseEntity.ok(unitTypeService.update(id, updateDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Видалити тип підрозділу")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("REST request to delete UnitType: {}", id);
        unitTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
