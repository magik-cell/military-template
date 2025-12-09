package ua.edu.viti.military.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.edu.viti.military.dto.request.PersonnelCreateDTO;
import ua.edu.viti.military.dto.request.PersonnelUpdateDTO;
import ua.edu.viti.military.dto.response.MilitaryUnitResponseDTO;
import ua.edu.viti.military.dto.response.PersonnelResponseDTO;
import ua.edu.viti.military.entity.MilitaryUnit;
import ua.edu.viti.military.entity.Personnel;
import ua.edu.viti.military.entity.Rank;
import ua.edu.viti.military.entity.SecurityClearance;
import ua.edu.viti.military.exception.BusinessLogicException;
import ua.edu.viti.military.exception.DuplicateResourceException;
import ua.edu.viti.military.exception.ResourceNotFoundException;
import ua.edu.viti.military.repository.MilitaryUnitRepository;
import ua.edu.viti.military.repository.PersonnelRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PersonnelService {

    private final PersonnelRepository personnelRepository;
    private final MilitaryUnitRepository militaryUnitRepository;

    @Transactional
    public PersonnelResponseDTO create(PersonnelCreateDTO dto) {
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

        return toResponseDTO(saved);
    }

    public PersonnelResponseDTO getById(Long id) {
        log.debug("Fetching personnel with ID: {}", id);

        Personnel personnel = personnelRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Військовослужбовець з ID " + id + " не знайдено"
            ));

        return toResponseDTO(personnel);
    }

    public List<PersonnelResponseDTO> getAll() {
        log.debug("Fetching all personnel");

        return personnelRepository.findAll()
            .stream()
            .map(this::toResponseDTO)
            .collect(Collectors.toList());
    }

    public List<PersonnelResponseDTO> getByUnitId(Long unitId) {
        log.debug("Fetching personnel by unit ID: {}", unitId);

        return personnelRepository.findByUnitId(unitId)
            .stream()
            .map(this::toResponseDTO)
            .collect(Collectors.toList());
    }

    public List<PersonnelResponseDTO> getByRank(Rank rank) {
        log.debug("Fetching personnel by rank: {}", rank);

        return personnelRepository.findByRank(rank)
            .stream()
            .map(this::toResponseDTO)
            .collect(Collectors.toList());
    }

    public List<PersonnelResponseDTO> getBySecurityClearance(SecurityClearance clearance) {
        log.debug("Fetching personnel by security clearance: {}", clearance);

        return personnelRepository.findBySecurityClearance(clearance)
            .stream()
            .map(this::toResponseDTO)
            .collect(Collectors.toList());
    }

    public List<PersonnelResponseDTO> getBySpecialization(String keyword) {
        log.debug("Fetching personnel by specialization keyword: {}", keyword);

        return personnelRepository.findBySpecializationContaining(keyword)
            .stream()
            .map(this::toResponseDTO)
            .collect(Collectors.toList());
    }

    public List<PersonnelResponseDTO> getActivePersonnel() {
        log.debug("Fetching active personnel");

        return personnelRepository.findActivePersonnel()
            .stream()
            .map(this::toResponseDTO)
            .collect(Collectors.toList());
    }

    public List<PersonnelResponseDTO> getContractsExpiringSoon(int daysThreshold) {
        log.debug("Fetching contracts expiring within {} days", daysThreshold);

        LocalDate thresholdDate = LocalDate.now().plusDays(daysThreshold);

        return personnelRepository.findByContractEndDateBefore(thresholdDate)
            .stream()
            .filter(p -> p.getContractEndDate().isAfter(LocalDate.now()))
            .map(this::toResponseDTO)
            .collect(Collectors.toList());
    }

    @Transactional
    public PersonnelResponseDTO update(Long id, PersonnelUpdateDTO dto) {
        log.info("Updating personnel with ID: {}", id);

        Personnel personnel = personnelRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Військовослужбовець з ID " + id + " не знайдено"
            ));

       
        validateContractActive(personnel);

        
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
            MilitaryUnit unit = militaryUnitRepository.findById(dto.getPidrozdilId())
                .orElseThrow(() -> new ResourceNotFoundException("Підрозділ не знайдено"));
            personnel.setUnit(unit);
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

        return toResponseDTO(updated);
    }

    @Transactional
    public void delete(Long id) {
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



    private PersonnelResponseDTO toResponseDTO(Personnel entity) {
        PersonnelResponseDTO dto = new PersonnelResponseDTO();
        dto.setId(entity.getId());
        dto.setMilitaryId(entity.getMilitaryId());
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setMiddleName(entity.getMiddleName());
        dto.setRank(entity.getRank());
        dto.setSpecialization(entity.getSpecialization());
        
        if (entity.getUnit() != null) {
            dto.setUnit(toMilitaryUnitResponseDTO(entity.getUnit()));
        }
        
        dto.setContractStartDate(entity.getContractStartDate());
        dto.setContractEndDate(entity.getContractEndDate());
        dto.setSecurityClearance(entity.getSecurityClearance());
        dto.setMedicalCategory(entity.getMedicalCategory());
        dto.setPhoneNumber(entity.getPhoneNumber());
        dto.setEmail(entity.getEmail());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        
        return dto;
    }

    private MilitaryUnitResponseDTO toMilitaryUnitResponseDTO(MilitaryUnit entity) {
        MilitaryUnitResponseDTO dto = new MilitaryUnitResponseDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setCode(entity.getCode());
        dto.setLocation(entity.getLocation());
        dto.setFormationDate(entity.getFormationDate());
        dto.setStrength(entity.getStrength());
        dto.setCurrentStrength(entity.getCurrentStrength());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}
