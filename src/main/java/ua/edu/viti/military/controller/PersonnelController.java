package ua.edu.viti.military.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ua.edu.viti.military.dto.request.PersonnelCreateDTO;
import ua.edu.viti.military.dto.request.PersonnelUpdateDTO;
import ua.edu.viti.military.dto.response.PersonnelResponseDTO;
import ua.edu.viti.military.service.PersonnelService;
import ua.edu.viti.military.entity.Rank;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/personnel")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Personnel", description = "API для керування персоналом")
@Validated
public class PersonnelController {

    private final PersonnelService personnelService;

    @PostMapping
    @Operation(summary = "Створити військовослужбовця")
    public ResponseEntity<PersonnelResponseDTO> create(@Valid @RequestBody PersonnelCreateDTO createDTO) {
        log.info("REST request to create Personnel: {}", createDTO);
        PersonnelResponseDTO created = personnelService.create(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Отримати військовослужбовця за ID")
    public ResponseEntity<PersonnelResponseDTO> getById(@PathVariable Long id) {
        log.info("REST request to get Personnel by id: {}", id);
        return ResponseEntity.ok(personnelService.getById(id));
    }

    @GetMapping
    @Operation(summary = "Отримати всіх військовослужбовців")
    public ResponseEntity<List<PersonnelResponseDTO>> getAll() {
        log.info("REST request to get all Personnel");
        return ResponseEntity.ok(personnelService.getAll());
    }

    @GetMapping("/by-unit")
    @Operation(summary = "Отримати персонал по підрозділу")
    public ResponseEntity<List<PersonnelResponseDTO>> getByUnit(@RequestParam Long unitId) {
        log.info("REST request to get Personnel by unitId: {}", unitId);
        return ResponseEntity.ok(personnelService.getByUnitId(unitId));
    }

    @GetMapping("/by-rank")
    @Operation(summary = "Отримати персонал по званню")
    public ResponseEntity<List<PersonnelResponseDTO>> getByRank(@RequestParam("rank") String rankStr) {
        log.info("REST request to get Personnel by rank: {}", rankStr);
        try {
            Rank rank = Rank.valueOf(rankStr);
            return ResponseEntity.ok(personnelService.getByRank(rank));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/contracts-expiring")
    @Operation(summary = "Персонал з контрактами що закінчуються")
    public ResponseEntity<List<PersonnelResponseDTO>> getContractsExpiring(@RequestParam(defaultValue = "60") int days) {
        log.info("REST request to get personnel with contracts expiring in {} days", days);
        return ResponseEntity.ok(personnelService.getContractsExpiringSoon(days));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Оновити військовослужбовця")
    public ResponseEntity<PersonnelResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody PersonnelUpdateDTO updateDTO) {
        log.info("REST request to update Personnel {}: {}", id, updateDTO);
        return ResponseEntity.ok(personnelService.update(id, updateDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Видалити військовослужбовця")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("REST request to delete Personnel: {}", id);
        personnelService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
