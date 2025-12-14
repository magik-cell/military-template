package ua.edu.viti.military.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ua.edu.viti.military.dto.request.MovementRequestDTO;
import ua.edu.viti.military.dto.response.MovementResponseDTO;
import ua.edu.viti.military.entity.MedicalCategory;
import ua.edu.viti.military.entity.MilitaryUnit;
import ua.edu.viti.military.entity.MovementType;
import ua.edu.viti.military.entity.Personnel;
import ua.edu.viti.military.entity.PersonnelMovement;
import ua.edu.viti.military.entity.Rank;
import ua.edu.viti.military.exception.BusinessLogicException;
import ua.edu.viti.military.exception.ResourceNotFoundException;
import ua.edu.viti.military.repository.PersonnelMovementRepository;
import ua.edu.viti.military.repository.PersonnelRepository;

/**
 * Сервіс для управління рухом персоналу.
 * 
 * Забезпечує транзакційну цілісність при операціях з персоналом:
 * - Призначення на посаду
 * - Переведення між підрозділами
 * - Підвищення звання
 * - Звільнення
 * - Відправка у відпустку
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PersonnelMovementService {

    private final PersonnelRepository personnelRepository;
    private final PersonnelMovementRepository movementRepository;

    /**
     * Призначення на посаду (переведення до підрозділу).
     * 
     * Транзакція з REPEATABLE_READ для запобігання phantom reads
     */
    @Transactional(
        isolation = Isolation.REPEATABLE_READ,
        rollbackFor = Exception.class
    )
    public MovementResponseDTO assignPersonnel(MovementRequestDTO dto) {
        // Перевірка обов'язкових полів
        Long personnelId = java.util.Objects.requireNonNull(dto.getPersonnelId(), "Personnel ID is required");
        
        // Додати контекст в MDC
        MDC.put("operation", "assign_personnel");
        MDC.put("personnelId", personnelId.toString());
        MDC.put("toLocation", dto.getToLocation());
        
        try {
            log.info("Assigning personnel: personnelId={}, toLocation={}", 
                     personnelId, dto.getToLocation());
        
        // 1. Знайти військовослужбовця
        Personnel personnel = personnelRepository.findById(personnelId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Військовослужбовця з ID " + personnelId + " не знайдено"));
        
        // 2. Перевірити медичну категорію
        if (personnel.getMedicalCategory() == MedicalCategory.D) {
            throw new BusinessLogicException(
                "Неможливо призначити військовослужбовця з медичною категорією D (непридатний)");
        }
        
        // 3. Зберегти попередню інформацію
        MilitaryUnit previousUnit = personnel.getUnit();
        String previousLocation = previousUnit != null ? previousUnit.getName() : "Не призначено";
        
        // 4. Створити запис в журналі
        PersonnelMovement movement = createMovement(personnel, dto);
        movement.setFromLocation(previousLocation);
        movement.setToLocation(dto.getToLocation());
        
        PersonnelMovement saved = movementRepository.save(movement);
        
        log.info("Personnel assigned successfully. Movement ID: {}", saved.getId());
        
        return toResponseDTO(saved);
        
        } finally {
            MDC.remove("operation");
            MDC.remove("personnelId");
            MDC.remove("toLocation");
        }
    }

    /**
     * Переведення між підрозділами.
     */
    @Transactional(
        isolation = Isolation.REPEATABLE_READ,
        rollbackFor = Exception.class
    )
    public MovementResponseDTO transferPersonnel(MovementRequestDTO dto) {
        Long personnelId = java.util.Objects.requireNonNull(dto.getPersonnelId(), "Personnel ID is required");
        
        MDC.put("operation", "transfer_personnel");
        MDC.put("personnelId", personnelId.toString());
        
        try {
            log.info("Transferring personnel: personnelId={}, from={}, to={}", 
                     personnelId, dto.getFromLocation(), dto.getToLocation());
        
        Personnel personnel = personnelRepository.findById(personnelId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Військовослужбовця з ID " + dto.getPersonnelId() + " не знайдено"));
        
        // Зберегти попередню інформацію
        MilitaryUnit previousUnit = personnel.getUnit();
        String previousLocation = previousUnit != null ? previousUnit.getName() : "Не призначено";
        
        // Створити запис в журналі
        PersonnelMovement movement = createMovement(personnel, dto);
        movement.setFromLocation(dto.getFromLocation() != null ? dto.getFromLocation() : previousLocation);
        movement.setToLocation(dto.getToLocation());
        
        PersonnelMovement saved = movementRepository.save(movement);
        
        log.info("Personnel transferred successfully. Movement ID: {}", saved.getId());
        
        return toResponseDTO(saved);
        
        } finally {
            MDC.remove("operation");
            MDC.remove("personnelId");
        }
    }

    /**
     * Підвищення звання.
     */
    @Transactional(
        isolation = Isolation.REPEATABLE_READ,
        rollbackFor = Exception.class
    )
    public MovementResponseDTO promotePersonnel(MovementRequestDTO dto) {
        Long personnelId = java.util.Objects.requireNonNull(dto.getPersonnelId(), "Personnel ID is required");
        
        MDC.put("operation", "promote_personnel");
        MDC.put("personnelId", personnelId.toString());
        
        try {
            log.info("Promoting personnel: personnelId={}, newRank={}", 
                     personnelId, dto.getNewRank());
        
        Personnel personnel = personnelRepository.findById(personnelId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Військовослужбовця з ID " + dto.getPersonnelId() + " не знайдено"));
        
        // Перевірити що нове звання вище попереднього
        Rank currentRank = personnel.getRank();
        if (dto.getNewRank().ordinal() <= currentRank.ordinal()) {
            throw new BusinessLogicException(
                "Нове звання має бути вищим за поточне. Поточне: " + currentRank + ", запитане: " + dto.getNewRank());
        }
        
        // Оновити звання
        personnel.setRank(dto.getNewRank());
        personnelRepository.save(personnel);
        
        // Створити запис в журналі
        PersonnelMovement movement = createMovement(personnel, dto);
        movement.setPreviousRank(currentRank);
        movement.setNewRank(dto.getNewRank());
        
        PersonnelMovement saved = movementRepository.save(movement);
        
        log.info("Personnel promoted successfully. Movement ID: {}", saved.getId());
        
        return toResponseDTO(saved);
        
        } finally {
            MDC.remove("operation");
            MDC.remove("personnelId");
        }
    }

    /**
     * Звільнення (створюємо тільки запис в історії).
     */
    @Transactional(
        isolation = Isolation.REPEATABLE_READ,
        rollbackFor = Exception.class
    )
    public MovementResponseDTO dismissPersonnel(MovementRequestDTO dto) {
        Long personnelId = java.util.Objects.requireNonNull(dto.getPersonnelId(), "Personnel ID is required");
        
        log.info("Dismissing personnel: personnelId={}, reason={}", 
                 personnelId, dto.getReason());
        
        Personnel personnel = personnelRepository.findById(personnelId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Військовослужбовця з ID " + dto.getPersonnelId() + " не знайдено"));
        
        // Зберегти попередню інформацію
        MilitaryUnit previousUnit = personnel.getUnit();
        String previousLocation = previousUnit != null ? previousUnit.getName() : "Не призначено";
        
        // Створити запис в журналі
        PersonnelMovement movement = createMovement(personnel, dto);
        movement.setFromLocation(previousLocation);
        movement.setToLocation("Звільнений");
        
        PersonnelMovement saved = movementRepository.save(movement);
        
        log.info("Personnel dismissed successfully. Movement ID: {}", saved.getId());
        
        return toResponseDTO(saved);
    }

    /**
     * Відправка у відпустку (створюємо тільки запис в історії).
     */
    @Transactional(rollbackFor = Exception.class)
    public MovementResponseDTO sendOnLeave(MovementRequestDTO dto) {
        Long personnelId = java.util.Objects.requireNonNull(dto.getPersonnelId(), "Personnel ID is required");
        
        log.info("Sending personnel on leave: personnelId={}", personnelId);
        
        Personnel personnel = personnelRepository.findById(personnelId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Військовослужбовця з ID " + dto.getPersonnelId() + " не знайдено"));
        
        // Створити запис в журналі
        PersonnelMovement movement = createMovement(personnel, dto);
        
        @SuppressWarnings("null")
		PersonnelMovement saved = movementRepository.save(movement);
        
        log.info("Personnel sent on leave. Movement ID: {}", saved.getId());
        
        return toResponseDTO(saved);
    }

    /**
     * Отримати історію переміщень військовослужбовця.
     */
    @Transactional(readOnly = true)
    public List<MovementResponseDTO> getPersonnelHistory(Long personnelId) {
        Long id = java.util.Objects.requireNonNull(personnelId, "Personnel ID is required");
        
        log.info("Getting personnel history: personnelId={}", id);
        
        // Перевірити що військовослужбовець існує
        if (!personnelRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                "Військовослужбовця з ID " + id + " не знайдено");
        }
        
        return movementRepository.findByPersonnelIdOrderByPerformedAtDesc(id)
            .stream()
            .map(this::toResponseDTO)
            .toList();
    }

    /**
     * Отримати запланованні (pending) переміщення.
     */
    @Transactional(readOnly = true)
    public List<MovementResponseDTO> getPendingMovements() {
        log.info("Getting pending movements");
        
        return movementRepository.findPendingMovements()
            .stream()
            .map(this::toResponseDTO)
            .toList();
    }

    /**
     * Підрахувати кількість переміщень за типом та періодом.
     */
    @Transactional(readOnly = true)
    public Long countMovementsByType(MovementType type, LocalDate startDate, LocalDate endDate) {
        log.info("Counting movements: type={}, period={} to {}", 
                 type, startDate, endDate);
        
        return movementRepository.countByTypeAndPeriod(
            type, 
            startDate.atStartOfDay(), 
            endDate.atTime(23, 59, 59)
        );
    }

    /**
     * Отримати переведення між підрозділами за період.
     */
    @Transactional(readOnly = true)
    public List<MovementResponseDTO> getTransfersBetweenUnits(LocalDate startDate, LocalDate endDate) {
        log.info("Getting transfers between units: period={} to {}", startDate, endDate);
        
        return movementRepository.findTransfersBetweenUnits(
            startDate.atStartOfDay(), 
            endDate.atTime(23, 59, 59)
        )
            .stream()
            .map(this::toResponseDTO)
            .toList();
    }

    // ==================== Helper Methods ====================

    /**
     * Створити запис руху персоналу.
     */
    private PersonnelMovement createMovement(Personnel personnel, MovementRequestDTO dto) {
        PersonnelMovement movement = new PersonnelMovement();
        movement.setPersonnel(personnel);
        movement.setType(dto.getType());
        movement.setReason(dto.getReason());
        movement.setNotes(dto.getNotes());
        movement.setPerformedBy(getCurrentUser());
        movement.setEffectiveDate(dto.getEffectiveDate());
        
        return movement;
    }

    /**
     * Отримати поточного користувача з Spring Security Context.
     */
    private String getCurrentUser() {
        org.springframework.security.core.Authentication authentication = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated() 
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            return authentication.getName();  // username
        }
        
        return "SYSTEM";
    }

    /**
     * Перетворити entity в DTO.
     */
    private MovementResponseDTO toResponseDTO(PersonnelMovement movement) {
        PersonnelMovement mov = java.util.Objects.requireNonNull(movement, "Movement cannot be null");
        Personnel personnel = mov.getPersonnel();
        
        MovementResponseDTO dto = new MovementResponseDTO();
        dto.setId(mov.getId());
        dto.setPersonnelId(personnel.getId());
        dto.setPersonnelName(String.format("%s %s %s",
            personnel.getLastName(),
            personnel.getFirstName(),
            personnel.getMiddleName() != null ? personnel.getMiddleName() : ""));
        dto.setMilitaryId(personnel.getMilitaryId());
        dto.setType(mov.getType());
        dto.setFromLocation(mov.getFromLocation() != null ? mov.getFromLocation() : "");
        dto.setToLocation(mov.getToLocation() != null ? mov.getToLocation() : "");
        dto.setPreviousRank(mov.getPreviousRank());
        dto.setNewRank(mov.getNewRank());
        dto.setReason(mov.getReason());
        dto.setNotes(mov.getNotes());
        dto.setPerformedBy(mov.getPerformedBy());
        dto.setPerformedAt(mov.getPerformedAt());
        dto.setEffectiveDate(mov.getEffectiveDate());
        
        return dto;
    }
}
