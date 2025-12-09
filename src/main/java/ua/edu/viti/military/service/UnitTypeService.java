package ua.edu.viti.military.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.edu.viti.military.dto.request.UnitTypeCreateDTO;
import ua.edu.viti.military.dto.request.UnitTypeUpdateDTO;
import ua.edu.viti.military.dto.response.UnitTypeResponseDTO;
import ua.edu.viti.military.entity.UnitType;
import ua.edu.viti.military.exception.DuplicateResourceException;
import ua.edu.viti.military.exception.ResourceNotFoundException;
import ua.edu.viti.military.repository.UnitTypeRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UnitTypeService {

    private final UnitTypeRepository unitTypeRepository;

    @Transactional
    public UnitTypeResponseDTO create(UnitTypeCreateDTO dto) {
        log.info("Creating new unit type with code: {}", dto.getKod());

       
        if (unitTypeRepository.existsByCode(dto.getKod())) {
            throw new DuplicateResourceException(
                "Тип підрозділу з кодом " + dto.getKod() + " вже існує"
            );
        }

       
        UnitType unitType = new UnitType();
        unitType.setName(dto.getNazva());
        unitType.setCode(dto.getKod());
        unitType.setDescription(dto.getOpys());
        unitType.setHierarchy(dto.getIerarhiya());
        unitType.setTypicalSize(dto.getTypovyiRozmir());   
        UnitType saved = unitTypeRepository.save(unitType);
        log.info("Unit type created with ID: {}", saved.getId());

        return toResponseDTO(saved);
    }

    public UnitTypeResponseDTO getById(Long id) {
        log.debug("Fetching unit type with ID: {}", id);

        UnitType unitType = unitTypeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Тип підрозділу з ID " + id + " не знайдено"
            ));

        return toResponseDTO(unitType);
    }

    public List<UnitTypeResponseDTO> getAll() {
        log.debug("Fetching all unit types");

        return unitTypeRepository.findAll()
            .stream()
            .map(this::toResponseDTO)
            .collect(Collectors.toList());
    }

    @Transactional
    public UnitTypeResponseDTO update(Long id, UnitTypeUpdateDTO dto) {
        log.info("Updating unit type with ID: {}", id);

        UnitType unitType = unitTypeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Тип підрозділу з ID " + id + " не знайдено"
            ));

       
        if (dto.getNazva() != null) {
            unitType.setName(dto.getNazva());
        }
        if (dto.getOpys() != null) {
            unitType.setDescription(dto.getOpys());
        }
        if (dto.getTypovyiRozmir() != null) {
            unitType.setTypicalSize(dto.getTypovyiRozmir());
        }

        UnitType updated = unitTypeRepository.save(unitType);
        log.info("Unit type with ID {} updated successfully", id);

        return toResponseDTO(updated);
    }

    @Transactional
    public void delete(Long id) {
        log.info("Deleting unit type with ID: {}", id);

        if (!unitTypeRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                "Тип підрозділу з ID " + id + " не знайдено"
            );
        }

        unitTypeRepository.deleteById(id);
        log.info("Unit type with ID {} deleted successfully", id);
    }


    private UnitTypeResponseDTO toResponseDTO(UnitType entity) {
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
}
