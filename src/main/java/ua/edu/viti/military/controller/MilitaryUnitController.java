package ua.edu.viti.military.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ua.edu.viti.military.dto.request.MilitaryUnitCreateDTO;
import ua.edu.viti.military.dto.request.MilitaryUnitUpdateDTO;
import ua.edu.viti.military.dto.response.MilitaryUnitResponseDTO;
import ua.edu.viti.military.service.MilitaryUnitService;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/military-units")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Military Units", description = "API для керування підрозділами")
@Validated
public class MilitaryUnitController {

    private final MilitaryUnitService militaryUnitService;

    @PostMapping
    @Operation(summary = "Створити підрозділ")
    public ResponseEntity<MilitaryUnitResponseDTO> create(
            @Valid @RequestBody MilitaryUnitCreateDTO createDTO) {
        log.info("REST request to create MilitaryUnit: {}", createDTO);
        MilitaryUnitResponseDTO created = militaryUnitService.create(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Отримати підрозділ за ID")
    public ResponseEntity<MilitaryUnitResponseDTO> getById(@PathVariable Long id) {
        log.info("REST request to get MilitaryUnit by id: {}", id);
        return ResponseEntity.ok(militaryUnitService.getById(id));
    }

    @GetMapping
    @Operation(summary = "Отримати всі підрозділи")
    public ResponseEntity<List<MilitaryUnitResponseDTO>> getAll() {
        log.info("REST request to get all MilitaryUnits");
        return ResponseEntity.ok(militaryUnitService.getAll());
    }

    @GetMapping("/by-unit-type")
    @Operation(summary = "Отримати підрозділи по типу підрозділу")
    public ResponseEntity<List<MilitaryUnitResponseDTO>> getByUnitType(@RequestParam Long unitTypeId) {
        log.info("REST request to get MilitaryUnits by unitTypeId: {}", unitTypeId);
        return ResponseEntity.ok(militaryUnitService.getByUnitType(unitTypeId));
    }

    @GetMapping("/top-level")
    @Operation(summary = "Отримати верхньорівневі підрозділи")
    public ResponseEntity<List<MilitaryUnitResponseDTO>> getTopLevelUnits() {
        log.info("REST request to get top level MilitaryUnits");
        return ResponseEntity.ok(militaryUnitService.getTopLevelUnits());
    }

    @GetMapping("/{id}/subunits")
    @Operation(summary = "Отримати дочірні підрозділи для батьківського ID")
    public ResponseEntity<List<MilitaryUnitResponseDTO>> getSubunits(@PathVariable Long id) {
        log.info("REST request to get subunits for parent id: {}", id);
        return ResponseEntity.ok(militaryUnitService.getSubunits(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Оновити підрозділ")
    public ResponseEntity<MilitaryUnitResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody MilitaryUnitUpdateDTO updateDTO) {
        log.info("REST request to update MilitaryUnit {}: {}", id, updateDTO);
        return ResponseEntity.ok(militaryUnitService.update(id, updateDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Видалити підрозділ")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("REST request to delete MilitaryUnit: {}", id);
        militaryUnitService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
