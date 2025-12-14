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
import ua.edu.viti.military.dto.request.MilitaryUnitCreateDTO;
import ua.edu.viti.military.dto.request.MilitaryUnitUpdateDTO;
import ua.edu.viti.military.dto.response.MilitaryUnitResponseDTO;
import ua.edu.viti.military.entity.MilitaryUnit;
import ua.edu.viti.military.entity.Personnel;
import ua.edu.viti.military.entity.UnitType;
import ua.edu.viti.military.event.CommanderChangedEvent;
import ua.edu.viti.military.event.LowStrengthEvent;
import ua.edu.viti.military.event.UnitCreatedEvent;
import ua.edu.viti.military.exception.BusinessLogicException;
import ua.edu.viti.military.exception.DuplicateResourceException;
import ua.edu.viti.military.exception.ResourceNotFoundException;
import ua.edu.viti.military.mapper.MilitaryUnitMapper;
import ua.edu.viti.military.repository.MilitaryUnitRepository;
import ua.edu.viti.military.repository.PersonnelRepository;
import ua.edu.viti.military.repository.UnitTypeRepository;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MilitaryUnitService {

    private final MilitaryUnitRepository militaryUnitRepository;
    private final UnitTypeRepository unitTypeRepository;
    private final PersonnelRepository personnelRepository;
    private final MilitaryUnitMapper militaryUnitMapper;
    private final ApplicationEventPublisher eventPublisher;  // ← Inject EventPublisher
    private final MetricsService metricsService;  // ← Inject MetricsService

    @Transactional
    @SuppressWarnings("null") // Nullable fields checked explicitly
    @CacheEvict(value = "militaryUnits", allEntries = true)
    public MilitaryUnitResponseDTO create(MilitaryUnitCreateDTO dto) {
        MDC.put("operation", "create_military_unit");
        MDC.put("unitCode", dto.getKod());
        
        try {
            log.info("Creating new military unit with code: {}", dto.getKod());


        if (militaryUnitRepository.existsByCode(dto.getKod())) {
            throw new DuplicateResourceException(
                "Підрозділ з кодом " + dto.getKod() + " вже існує"
            );
        }

  
        UnitType unitType = unitTypeRepository.findById(
                Objects.requireNonNull(dto.getTypPidrozdilu(), "Тип підрозділу обов'язковий")
            )
            .orElseThrow(() -> new ResourceNotFoundException(
                "Тип підрозділу з ID " + dto.getTypPidrozdilu() + " не знайдено"
            ));

        MilitaryUnit parentUnit = null;
        if (dto.getBatkivskyyPidrozdil() != null) {
            parentUnit = militaryUnitRepository.findById(dto.getBatkivskyyPidrozdil())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Батьківський підрозділ з ID " + dto.getBatkivskyyPidrozdil() + " не знайдено"
                ));
        }

      
        Personnel commander = null;
        if (dto.getKomandyr() != null) {
            commander = personnelRepository.findById(dto.getKomandyr())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Командира з ID " + dto.getKomandyr() + " не знайдено"
                ));

      
            validateCommanderContract(commander);
        }

        MilitaryUnit militaryUnit = new MilitaryUnit();
        militaryUnit.setName(dto.getNazva());
        militaryUnit.setCode(dto.getKod());
        militaryUnit.setUnitType(unitType);
        militaryUnit.setParentUnit(parentUnit);
        militaryUnit.setCommander(commander);
        militaryUnit.setLocation(dto.getLokatsiya());
        militaryUnit.setFormationDate(dto.getDataFormuvannya());
        militaryUnit.setStrength(dto.getShtatnaChuselna());
        militaryUnit.setCurrentStrength(dto.getPotochnaChuselna());

   
        MilitaryUnit saved = militaryUnitRepository.save(militaryUnit);
        log.info("Military unit created with ID: {}", saved.getId());

        // ✅ Зафіксувати metric
        metricsService.recordUnitCreated(saved.getName());

        // ✅ Опублікувати event про створення підрозділу
        eventPublisher.publishEvent(
            new UnitCreatedEvent(
                this,
                saved,
                "System"  // В реальній системі - getCurrentUser()
            )
        );
        log.info("UnitCreatedEvent published for unit ID: {}", saved.getId());

        // Перевірити низьку чисельність
        checkLowStrength(saved);

        return militaryUnitMapper.toResponseDTO(saved);
        
        } finally {
            MDC.remove("operation");
            MDC.remove("unitCode");
            MDC.remove("unitId");
        }
    }

    /**
     * Кешування - результат зберігається в Redis
     * Ключ: militaryUnits::1 (де 1 - це id)
     */
    @Cacheable(value = "militaryUnits", key = "#id")
    public MilitaryUnitResponseDTO getById(@NonNull Long id) {
        log.info("Fetching military unit from database: id={}", id);

        MilitaryUnit militaryUnit = militaryUnitRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Підрозділ з ID " + id + " не знайдено"
            ));

        return militaryUnitMapper.toResponseDTO(militaryUnit);
    }

    /**
     * Кешування списку
     * Ключ: militaryUnits::all
     */
    @Cacheable(value = "militaryUnits", key = "'all'")
    public List<MilitaryUnitResponseDTO> getAll() {
        log.info("Fetching all military units from database");

        return militaryUnitMapper.toResponseDTOList(militaryUnitRepository.findAll());
    }

    /**
     * Кешування списку по типу підрозділу
     * Ключ: militaryUnits::unitType::1
     */
    @Cacheable(value = "militaryUnits", key = "'unitType::' + #unitTypeId")
    public List<MilitaryUnitResponseDTO> getByUnitType(Long unitTypeId) {
        log.info("Fetching military units by unit type from database: unitTypeId={}", unitTypeId);

        return militaryUnitMapper.toResponseDTOList(
            militaryUnitRepository.findByUnitTypeId(unitTypeId));
    }

    public List<MilitaryUnitResponseDTO> getTopLevelUnits() {
        log.debug("Fetching top-level military units");

        return militaryUnitMapper.toResponseDTOList(
            militaryUnitRepository.findTopLevelUnits());
    }

    public List<MilitaryUnitResponseDTO> getSubunits(Long parentUnitId) {
        log.debug("Fetching subunits for parent ID: {}", parentUnitId);

        return militaryUnitMapper.toResponseDTOList(
            militaryUnitRepository.findByParentUnitId(parentUnitId));
    }

    /**
     * При оновленні - invalidate кеш
     */
    @Transactional
    @SuppressWarnings("null") // Nullable fields checked explicitly
    @CacheEvict(value = "militaryUnits", allEntries = true)
    public MilitaryUnitResponseDTO update(@NonNull Long id, MilitaryUnitUpdateDTO dto) {
        log.info("Updating military unit with ID: {}", id);

        MilitaryUnit militaryUnit = militaryUnitRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Підрозділ з ID " + id + " не знайдено"
            ));

        // Зберігаємо старого командира для event
        Personnel oldCommander = militaryUnit.getCommander();
        Personnel newCommander = null;
       
        if (dto.getNazva() != null) {
            militaryUnit.setName(dto.getNazva());
        }
        if (dto.getBatkivskyyPidrozdil() != null) {
            MilitaryUnit parentUnit = militaryUnitRepository.findById(dto.getBatkivskyyPidrozdil())
                .orElseThrow(() -> new ResourceNotFoundException("Батьківський підрозділ не знайдено"));
            militaryUnit.setParentUnit(parentUnit);
        }
        if (dto.getKomandyr() != null) {
            newCommander = personnelRepository.findById(dto.getKomandyr())
                .orElseThrow(() -> new ResourceNotFoundException("Командира не знайдено"));
            validateCommanderContract(newCommander);
            militaryUnit.setCommander(newCommander);
        }
        if (dto.getLokatsiya() != null) {
            militaryUnit.setLocation(dto.getLokatsiya());
        }
        if (dto.getShtatnaChuselna() != null) {
            militaryUnit.setStrength(dto.getShtatnaChuselna());
        }
        if (dto.getPotochnaChuselna() != null) {
            militaryUnit.setCurrentStrength(dto.getPotochnaChuselna());
        }

        MilitaryUnit updated = militaryUnitRepository.save(militaryUnit);
        log.info("Military unit with ID {} updated successfully", id);

        // ✅ Опублікувати event про зміну командира
        if (newCommander != null && 
            (oldCommander == null || !oldCommander.getId().equals(newCommander.getId()))) {
            // ✅ Зафіксувати metric
            metricsService.recordCommanderChanged(updated.getName());
            
            eventPublisher.publishEvent(
                new CommanderChangedEvent(
                    this,
                    updated.getId(),
                    updated.getName(),
                    updated.getCode(),
                    oldCommander != null ? oldCommander.getId() : null,
                    oldCommander != null ? oldCommander.getFirstName() + " " + oldCommander.getLastName() : null,
                    newCommander.getId(),
                    newCommander.getFirstName() + " " + newCommander.getLastName(),
                    newCommander.getRank(),
                    "System"  // В реальній системі - getCurrentUser()
                )
            );
            log.info("CommanderChangedEvent published for unit ID: {}", updated.getId());
        }

        // Перевірити низьку чисельність
        checkLowStrength(updated);

        return militaryUnitMapper.toResponseDTO(updated);
    }

    /**
     * При видаленні - invalidate кеш
     */
    @Transactional
    @CacheEvict(value = "militaryUnits", allEntries = true)
    public void delete(@NonNull Long id) {
        log.info("Deleting military unit with ID: {}", id);

        if (!militaryUnitRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                "Підрозділ з ID " + id + " не знайдено"
            );
        }

        militaryUnitRepository.deleteById(id);
        log.info("Military unit with ID {} deleted successfully", id);
    }

    //           Бізнес-логіка 

    private void validateCommanderContract(Personnel personnel) {
        if (personnel.getContractEndDate().isBefore(java.time.LocalDate.now())) {
            throw new BusinessLogicException(
                "Контракт командира закінчився " + personnel.getContractEndDate()
            );
        }
    }

    /**
     * Перевірка низької чисельності підрозділу
     * Якщо поточна чисельність < 70% від штатної - опублікувати event
     */
    private void checkLowStrength(MilitaryUnit unit) {
        if (unit.getStrength() != null && unit.getCurrentStrength() != null) {
            double fillPercentage = (unit.getCurrentStrength() * 100.0) / unit.getStrength();
            
            if (fillPercentage < 70.0) {
                eventPublisher.publishEvent(
                    new LowStrengthEvent(
                        this,
                        unit.getId(),
                        unit.getName(),
                        unit.getCode(),
                        unit.getCurrentStrength(),
                        unit.getStrength()
                    )
                );
                log.warn("LowStrengthEvent published for unit '{}' ({}% filled)", 
                    unit.getName(), String.format("%.1f", fillPercentage));
            }
        }
    }
}
