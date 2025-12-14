package ua.edu.viti.military.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.edu.viti.military.dto.request.PersonnelCreateDTO;
import ua.edu.viti.military.dto.request.PersonnelUpdateDTO;
import ua.edu.viti.military.dto.response.PersonnelResponseDTO;
import ua.edu.viti.military.entity.MilitaryUnit;
import ua.edu.viti.military.entity.Personnel;
import ua.edu.viti.military.entity.Rank;
import ua.edu.viti.military.entity.SecurityClearance;
import ua.edu.viti.military.event.PersonnelAssignedEvent;
import ua.edu.viti.military.event.PersonnelTransferredEvent;
import ua.edu.viti.military.exception.BusinessLogicException;
import ua.edu.viti.military.exception.DuplicateResourceException;
import ua.edu.viti.military.exception.ResourceNotFoundException;
import ua.edu.viti.military.mapper.PersonnelMapper;
import ua.edu.viti.military.repository.MilitaryUnitRepository;
import ua.edu.viti.military.repository.PersonnelRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PersonnelService {

    private final PersonnelRepository personnelRepository;
    private final MilitaryUnitRepository militaryUnitRepository;
    private final PersonnelMapper personnelMapper;
    private final ApplicationEventPublisher eventPublisher;  // ← Inject EventPublisher
    private final MetricsService metricsService;  // ← Inject MetricsService

    @Transactional
    @SuppressWarnings("null")
    @CacheEvict(value = "personnel", allEntries = true)
    public PersonnelResponseDTO create(PersonnelCreateDTO dto) {
        // Додати контекст в MDC для structured logging
        MDC.put("operation", "create_personnel");
        MDC.put("militaryId", dto.getViyskovyiId());
        
        try {
            log.info("Creating new personnel with military ID: {}", dto.getViyskovyiId());

            
            if (personnelRepository.existsByMilitaryId(dto.getViyskovyiId())) {
            throw new DuplicateResourceException(
                "Військовослужбовець з ID " + dto.getViyskovyiId() + " вже існує"
            );
        }

   
        validateContractDates(dto.getDataPochatku(), dto.getDataZakinchennya());

    
        MilitaryUnit unit = null;
        if (dto.getPidrozdilId() != null) {
            unit = militaryUnitRepository.findById(dto.getPidrozdilId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Підрозділ з ID " + dto.getPidrozdilId() + " не знайдено"
                ));
        }


        Personnel personnel = new Personnel();
        personnel.setMilitaryId(dto.getViyskovyiId());
        personnel.setFirstName(dto.getImya());
        personnel.setLastName(dto.getPrizvyshche());
        personnel.setMiddleName(dto.getPobatkovi());
        personnel.setRank(dto.getZvannya());
        personnel.setSpecialization(dto.getSpetsializatsiya());
        personnel.setUnit(unit);
        personnel.setContractStartDate(dto.getDataPochatku());
        personnel.setContractEndDate(dto.getDataZakinchennya());
        personnel.setSecurityClearance(dto.getDopusk());
        personnel.setMedicalCategory(dto.getMedkategoriya());
        personnel.setPhoneNumber(dto.getTelefon());
        personnel.setEmail(dto.getEmail());

  
        Personnel saved = personnelRepository.save(personnel);
        log.info("Personnel created with ID: {}", saved.getId());

        // ✅ Опублікувати event про призначення до підрозділу
        if (unit != null) {
            // ✅ Зафіксувати metric
            metricsService.recordPersonnelAssigned(
                saved.getFirstName() + " " + saved.getLastName(),
                unit.getName()
            );
            
            eventPublisher.publishEvent(
                new PersonnelAssignedEvent(
                    this,
                    saved,
                    unit.getName(),
                    unit.getCode(),
                    "System"  // В реальній системі - getCurrentUser()
                )
            );
            log.info("PersonnelAssignedEvent published for personnel ID: {}", saved.getId());
        }

        return personnelMapper.toResponseDTO(saved);
        
        } finally {
            // Очистити MDC після виконання
            MDC.remove("operation");
            MDC.remove("militaryId");
            MDC.remove("personnelId");
        }
    }

    /**
     * Кешування - результат зберігається в Redis
     * Ключ: personnel::1 (де 1 - це id)
     */
    @Cacheable(value = "personnel", key = "#id")
    public PersonnelResponseDTO getById(@NonNull Long id) {
        MDC.put("operation", "get_personnel_by_id");
        MDC.put("personnelId", id.toString());
        
        try {
            log.info("Fetching personnel from database: id={}", id);

        Personnel personnel = personnelRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Військовослужбовець з ID " + id + " не знайдено"
            ));

        return personnelMapper.toResponseDTO(personnel);
        
        } finally {
            MDC.remove("operation");
            MDC.remove("personnelId");
        }
    }

    /**
     * Кешування списку
     * Ключ: personnel::all
     */
    @Cacheable(value = "personnel", key = "'all'")
    public List<PersonnelResponseDTO> getAll() {
        log.info("Fetching all personnel from database");

        return personnelMapper.toResponseDTOList(personnelRepository.findAll());
    }

    /**
     * Кешування списку по підрозділу
     * Ключ: personnel::unit::1
     */
    @Cacheable(value = "personnel", key = "'unit::' + #unitId")
    public List<PersonnelResponseDTO> getByUnitId(Long unitId) {
        log.info("Fetching personnel by unit from database: unitId={}", unitId);

        return personnelMapper.toResponseDTOList(
            personnelRepository.findByUnitId(unitId));
    }

    public List<PersonnelResponseDTO> getByRank(Rank rank) {
        log.debug("Fetching personnel by rank: {}", rank);

        return personnelMapper.toResponseDTOList(
            personnelRepository.findByRank(rank));
    }

    public List<PersonnelResponseDTO> getBySecurityClearance(SecurityClearance clearance) {
        log.debug("Fetching personnel by security clearance: {}", clearance);

        return personnelMapper.toResponseDTOList(
            personnelRepository.findBySecurityClearance(clearance));
    }

    public List<PersonnelResponseDTO> getBySpecialization(String keyword) {
        log.debug("Fetching personnel by specialization keyword: {}", keyword);

        return personnelMapper.toResponseDTOList(
            personnelRepository.findBySpecializationContaining(keyword));
    }

    public List<PersonnelResponseDTO> getActivePersonnel() {
        log.debug("Fetching active personnel");

        return personnelMapper.toResponseDTOList(
            personnelRepository.findActivePersonnel());
    }

    public List<PersonnelResponseDTO> getContractsExpiringSoon(int daysThreshold) {
        log.debug("Fetching contracts expiring within {} days", daysThreshold);

        LocalDate thresholdDate = LocalDate.now().plusDays(daysThreshold);

        List<Personnel> expiring = personnelRepository.findByContractEndDateBefore(thresholdDate)
            .stream()
            .filter(p -> p.getContractEndDate().isAfter(LocalDate.now()))
            .toList();
        
        return personnelMapper.toResponseDTOList(expiring);
    }

    /**
     * При оновленні - invalidate кеш
     */
    @Transactional
    @SuppressWarnings("null")
    @CacheEvict(value = "personnel", allEntries = true)
    public PersonnelResponseDTO update(@NonNull Long id, PersonnelUpdateDTO dto) {
        log.info("Updating personnel with ID: {}", id);

        Personnel personnel = personnelRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Військовослужбовець з ID " + id + " не знайдено"
            ));

       
        validateContractActive(personnel);

        // Зберігаємо старий підрозділ для event
        MilitaryUnit oldUnit = personnel.getUnit();
        MilitaryUnit newUnit = null;
        
        
        if (dto.getImya() != null) {
            personnel.setFirstName(dto.getImya());
        }
        if (dto.getPrizvyshche() != null) {
            personnel.setLastName(dto.getPrizvyshche());
        }
        if (dto.getPobatkovi() != null) {
            personnel.setMiddleName(dto.getPobatkovi());
        }
        if (dto.getZvannya() != null) {
            personnel.setRank(dto.getZvannya());
        }
        if (dto.getSpetsializatsiya() != null) {
            personnel.setSpecialization(dto.getSpetsializatsiya());
        }
        if (dto.getPidrozdilId() != null) {
            newUnit = militaryUnitRepository.findById(dto.getPidrozdilId())
                .orElseThrow(() -> new ResourceNotFoundException("Підрозділ не знайдено"));
            personnel.setUnit(newUnit);
        }
        if (dto.getDataZakinchennya() != null) {
            validateContractDates(personnel.getContractStartDate(), dto.getDataZakinchennya());
            personnel.setContractEndDate(dto.getDataZakinchennya());
        }
        if (dto.getDopusk() != null) {
            personnel.setSecurityClearance(dto.getDopusk());
        }
        if (dto.getMedkategoriya() != null) {
            personnel.setMedicalCategory(dto.getMedkategoriya());
        }
        if (dto.getTelefon() != null) {
            personnel.setPhoneNumber(dto.getTelefon());
        }
        if (dto.getEmail() != null) {
            personnel.setEmail(dto.getEmail());
        }

        Personnel updated = personnelRepository.save(personnel);
        log.info("Personnel with ID {} updated successfully", id);

        // ✅ Опублікувати event про переведення між підрозділами
        if (newUnit != null && oldUnit != null && !oldUnit.getId().equals(newUnit.getId())) {
            // ✅ Зафіксувати metric
            metricsService.recordPersonnelTransferred(
                updated.getFirstName() + " " + updated.getLastName(),
                oldUnit.getName(),
                newUnit.getName()
            );
            
            eventPublisher.publishEvent(
                new PersonnelTransferredEvent(
                    this,
                    updated.getId(),
                    updated.getFirstName() + " " + updated.getLastName(),
                    updated.getMilitaryId(),
                    updated.getRank(),
                    oldUnit.getId(),
                    oldUnit.getName(),
                    newUnit.getId(),
                    newUnit.getName(),
                    "System"  // В реальній системі - getCurrentUser()
                )
            );
            log.info("PersonnelTransferredEvent published for personnel ID: {}", updated.getId());
        }

        return personnelMapper.toResponseDTO(updated);
    }

    /**
     * При видаленні - invalidate кеш
     */
    @Transactional
    @CacheEvict(value = "personnel", allEntries = true)
    public void delete(@NonNull Long id) {
        log.info("Deleting personnel with ID: {}", id);

        if (!personnelRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                "Військовослужбовець з ID " + id + " не знайдено"
            );
        }

        personnelRepository.deleteById(id);
        log.info("Personnel with ID {} deleted successfully", id);
    }

    //               Бізнес-логіка 

    private void validateContractDates(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new BusinessLogicException(
                "Дата закінчення контракту не може бути раніше дати початку"
            );
        }
        if (endDate.isBefore(LocalDate.now())) {
            throw new BusinessLogicException(
                "Дата закінчення контракту не може бути в минулому"
            );
        }
    }

    private void validateContractActive(Personnel personnel) {
        if (personnel.getContractEndDate().isBefore(LocalDate.now())) {
            throw new BusinessLogicException(
                "Контракт цього військовослужбовця закінчився " + 
                personnel.getContractEndDate()
            );
        }
    }
}
