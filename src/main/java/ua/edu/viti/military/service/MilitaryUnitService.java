package ua.edu.viti.military.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.edu.viti.military.dto.request.MilitaryUnitCreateDTO;
import ua.edu.viti.military.dto.request.MilitaryUnitUpdateDTO;
import ua.edu.viti.military.dto.response.MilitaryUnitResponseDTO;
import ua.edu.viti.military.dto.response.PersonnelShortResponseDTO;
import ua.edu.viti.military.dto.response.UnitTypeResponseDTO;
import ua.edu.viti.military.entity.MilitaryUnit;
import ua.edu.viti.military.entity.Personnel;
import ua.edu.viti.military.entity.UnitType;
import ua.edu.viti.military.exception.BusinessLogicException;
import ua.edu.viti.military.exception.DuplicateResourceException;
import ua.edu.viti.military.exception.ResourceNotFoundException;
import ua.edu.viti.military.repository.MilitaryUnitRepository;
import ua.edu.viti.military.repository.PersonnelRepository;
import ua.edu.viti.military.repository.UnitTypeRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MilitaryUnitService {

    private final MilitaryUnitRepository militaryUnitRepository;
    private final UnitTypeRepository unitTypeRepository;
    private final PersonnelRepository personnelRepository;

    @Transactional
    public MilitaryUnitResponseDTO create(MilitaryUnitCreateDTO dto) {
        log.info("Creating new military unit with code: {}", dto.getKod());


        if (militaryUnitRepository.existsByCode(dto.getKod())) {
            throw new DuplicateResourceException(
                "Підрозділ з кодом " + dto.getKod() + " вже існує"
            );
        }

  
        UnitType unitType = unitTypeRepository.findById(dto.getTypPidrozdilu())
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

        return toResponseDTO(saved);
    }

    public MilitaryUnitResponseDTO getById(Long id) {
        log.debug("Fetching military unit with ID: {}", id);

        MilitaryUnit militaryUnit = militaryUnitRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Підрозділ з ID " + id + " не знайдено"
            ));

        return toResponseDTO(militaryUnit);
    }

    public List<MilitaryUnitResponseDTO> getAll() {
        log.debug("Fetching all military units");

        return militaryUnitRepository.findAll()
            .stream()
            .map(this::toResponseDTO)
            .collect(Collectors.toList());
    }

    public List<MilitaryUnitResponseDTO> getByUnitType(Long unitTypeId) {
        log.debug("Fetching military units by unit type ID: {}", unitTypeId);

        return militaryUnitRepository.findByUnitTypeId(unitTypeId)
            .stream()
            .map(this::toResponseDTO)
            .collect(Collectors.toList());
    }

    public List<MilitaryUnitResponseDTO> getTopLevelUnits() {
        log.debug("Fetching top-level military units");

        return militaryUnitRepository.findTopLevelUnits()
            .stream()
            .map(this::toResponseDTO)
            .collect(Collectors.toList());
    }

    public List<MilitaryUnitResponseDTO> getSubunits(Long parentUnitId) {
        log.debug("Fetching subunits for parent ID: {}", parentUnitId);

        return militaryUnitRepository.findByParentUnitId(parentUnitId)
            .stream()
            .map(this::toResponseDTO)
            .collect(Collectors.toList());
    }

    @Transactional
    public MilitaryUnitResponseDTO update(Long id, MilitaryUnitUpdateDTO dto) {
        log.info("Updating military unit with ID: {}", id);

        MilitaryUnit militaryUnit = militaryUnitRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Підрозділ з ID " + id + " не знайдено"
            ));

       
        if (dto.getNazva() != null) {
            militaryUnit.setName(dto.getNazva());
        }
        if (dto.getBatkivskyyPidrozdil() != null) {
            MilitaryUnit parentUnit = militaryUnitRepository.findById(dto.getBatkivskyyPidrozdil())
                .orElseThrow(() -> new ResourceNotFoundException("Батьківський підрозділ не знайдено"));
            militaryUnit.setParentUnit(parentUnit);
        }
        if (dto.getKomandyr() != null) {
            Personnel commander = personnelRepository.findById(dto.getKomandyr())
                .orElseThrow(() -> new ResourceNotFoundException("Командира не знайдено"));
            validateCommanderContract(commander);
            militaryUnit.setCommander(commander);
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

        return toResponseDTO(updated);
    }

    @Transactional
    public void delete(Long id) {
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

    private MilitaryUnitResponseDTO toResponseDTO(MilitaryUnit entity) {
        MilitaryUnitResponseDTO dto = new MilitaryUnitResponseDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setCode(entity.getCode());
        dto.setUnitType(toUnitTypeDTO(entity.getUnitType()));
        
        if (entity.getParentUnit() != null) {
            dto.setParentUnit(toResponseDTO(entity.getParentUnit()));
        }
        
        if (entity.getCommander() != null) {
            dto.setCommander(toPersonnelShortDTO(entity.getCommander()));
        }
        
        dto.setLocation(entity.getLocation());
        dto.setFormationDate(entity.getFormationDate());
        dto.setStrength(entity.getStrength());
        dto.setCurrentStrength(entity.getCurrentStrength());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        
        return dto;
    }

    private UnitTypeResponseDTO toUnitTypeDTO(UnitType entity) {
        UnitTypeResponseDTO dto = new UnitTypeResponseDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setCode(entity.getCode());
        dto.setDescription(entity.getDescription());
        dto.setHierarchy(entity.getHierarchy());
        dto.setTypicalSize(entity.getTypicalSize());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    private PersonnelShortResponseDTO toPersonnelShortDTO(Personnel entity) {
        PersonnelShortResponseDTO dto = new PersonnelShortResponseDTO();
        dto.setId(entity.getId());
        dto.setMilitaryId(entity.getMilitaryId());
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setMiddleName(entity.getMiddleName());
        dto.setRank(entity.getRank());
        dto.setContractEndDate(entity.getContractEndDate());
        return dto;
    }
}
