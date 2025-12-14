package ua.edu.viti.military.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ua.edu.viti.military.dto.request.MovementRequestDTO;
import ua.edu.viti.military.dto.response.MovementResponseDTO;
import ua.edu.viti.military.entity.MovementType;
import ua.edu.viti.military.service.PersonnelMovementService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/personnel-movements")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Personnel Movements", description = "API для операцій з персоналом")
public class PersonnelMovementController {
    
    private final PersonnelMovementService movementService;
    
    @PostMapping("/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Призначити на посаду", 
               description = "Призначення військовослужбовця на посаду")
    public ResponseEntity<MovementResponseDTO> assignPersonnel(
            @Valid @RequestBody MovementRequestDTO dto) {
        
        log.info("REST request to assign personnel: {}", dto);
        MovementResponseDTO movement = movementService.assignPersonnel(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(movement);
    }
    
    @PostMapping("/transfer")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Перевести", 
               description = "Переведення між підрозділами або посадами")
    public ResponseEntity<MovementResponseDTO> transferPersonnel(
            @Valid @RequestBody MovementRequestDTO dto) {
        
        log.info("REST request to transfer personnel: {}", dto);
        MovementResponseDTO movement = movementService.transferPersonnel(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(movement);
    }
    
    @PostMapping("/promote")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Підвищити звання", 
               description = "Підвищення звання військовослужбовця")
    public ResponseEntity<MovementResponseDTO> promotePersonnel(
            @Valid @RequestBody MovementRequestDTO dto) {
        
        log.info("REST request to promote personnel: {}", dto);
        MovementResponseDTO movement = movementService.promotePersonnel(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(movement);
    }
    
    @PostMapping("/dismiss")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Звільнити", 
               description = "Звільнення військовослужбовця")
    public ResponseEntity<MovementResponseDTO> dismissPersonnel(
            @Valid @RequestBody MovementRequestDTO dto) {
        
        log.info("REST request to dismiss personnel: {}", dto);
        MovementResponseDTO movement = movementService.dismissPersonnel(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(movement);
    }
    
    @PostMapping("/leave")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Відправити у відпустку", 
               description = "Відправити військовослужбовця у відпустку")
    public ResponseEntity<MovementResponseDTO> sendOnLeave(
            @Valid @RequestBody MovementRequestDTO dto) {
        
        log.info("REST request to send personnel on leave: {}", dto);
        MovementResponseDTO movement = movementService.sendOnLeave(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(movement);
    }
    
    @GetMapping("/personnel/{personnelId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Історія операцій", 
               description = "Отримати всю історію операцій для конкретного військовослужбовця")
    public ResponseEntity<List<MovementResponseDTO>> getPersonnelHistory(
            @PathVariable Long personnelId) {
        
        log.info("REST request to get movement history for personnel ID: {}", personnelId);
        List<MovementResponseDTO> history = movementService.getPersonnelHistory(personnelId);
        return ResponseEntity.ok(history);
    }
    
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Майбутні операції", 
               description = "Отримати всі операції що набудуть чинності в майбутньому")
    public ResponseEntity<List<MovementResponseDTO>> getPendingMovements() {
        
        log.info("REST request to get pending movements");
        List<MovementResponseDTO> pending = movementService.getPendingMovements();
        return ResponseEntity.ok(pending);
    }
    
    @GetMapping("/statistics/count")
    @Operation(summary = "Статистика операцій за період", 
               description = "Кількість операцій певного типу за вказаний період")
    public ResponseEntity<Long> getMovementsCount(
            @RequestParam MovementType type,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
            LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate end) {
        
        log.info("REST request to count movements: type={}, start={}, end={}", type, start, end);
        Long count = movementService.countMovementsByType(type, start, end);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/transfers")
    @Operation(summary = "Переведення між підрозділами", 
               description = "Знайти всі переведення за період")
    public ResponseEntity<List<MovementResponseDTO>> getTransfersBetweenUnits(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        
        log.info("REST request to get transfers: period={} to {}", start, end);
        List<MovementResponseDTO> transfers = movementService.getTransfersBetweenUnits(start, end);
        return ResponseEntity.ok(transfers);
    }
}
