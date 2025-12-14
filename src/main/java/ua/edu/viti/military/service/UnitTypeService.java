package ua.edu.viti.military.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.edu.viti.military.dto.request.UnitTypeCreateDTO;
import ua.edu.viti.military.dto.request.UnitTypeUpdateDTO;
import ua.edu.viti.military.dto.response.UnitTypeResponseDTO;
import ua.edu.viti.military.entity.UnitType;
import ua.edu.viti.military.exception.DuplicateResourceException;
import ua.edu.viti.military.exception.ResourceNotFoundException;
import ua.edu.viti.military.mapper.UnitTypeMapper;
import ua.edu.viti.military.repository.UnitTypeRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UnitTypeService {

    private final UnitTypeRepository unitTypeRepository;
    private final UnitTypeMapper unitTypeMapper;

    @SuppressWarnings("null")
	@Transactional
    @CacheEvict(value = "unitTypes", key = "'all'")
    public UnitTypeResponseDTO create(UnitTypeCreateDTO dto) {
        log.info("Creating new unit type with code: {}", dto.getKod());

       
        if (unitTypeRepository.existsByCode(dto.getKod())) {
            throw new DuplicateResourceException(
                "Тип підрозділу з кодом " + dto.getKod() + " вже існує"
            );
        }

        // MapStruct маппінг DTO → Entity
        UnitType unitType = unitTypeMapper.toEntity(dto);
        
        UnitType saved = unitTypeRepository.save(unitType);
        log.info("Unit type created with ID: {}", saved.getId());

        // MapStruct маппінг Entity → ResponseDTO
        return unitTypeMapper.toResponseDTO(saved);
    }

    /**
     * Кешування - результат зберігається в Redis
     * Ключ: unitTypes::1 (де 1 - це id)
     */
    @Cacheable(value = "unitTypes", key = "#id")
    public UnitTypeResponseDTO getById(@NonNull Long id) {
        log.info("Fetching unit type from database: id={}", id);

        UnitType unitType = unitTypeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Тип підрозділу з ID " + id + " не знайдено"
            ));

        return unitTypeMapper.toResponseDTO(unitType);
    }

    /**
     * Кешування списку
     * Ключ: unitTypes::all
     */
    @Cacheable(value = "unitTypes", key = "'all'")
    public List<UnitTypeResponseDTO> getAll() {
        log.info("Fetching all unit types from database");

        return unitTypeMapper.toResponseDTOList(unitTypeRepository.findAll());
    }

    /**
     * При оновленні - invalidate конкретний запис та список
     */
    @Transactional
    @SuppressWarnings("null")
    @CacheEvict(value = "unitTypes", allEntries = true)
    public UnitTypeResponseDTO update(@NonNull Long id, UnitTypeUpdateDTO dto) {
        log.info("Updating unit type with ID: {}", id);

        UnitType unitType = unitTypeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Тип підрозділу з ID " + id + " не знайдено"
            ));

        // MapStruct оновлення Entity з DTO (тільки non-null поля)
        unitTypeMapper.updateEntityFromUpdateDTO(dto, unitType);

        UnitType updated = unitTypeRepository.save(unitType);
        log.info("Unit type with ID {} updated successfully", id);

        return unitTypeMapper.toResponseDTO(updated);
    }

    /**
     * При видаленні - invalidate кеш
     */
    @Transactional
    @CacheEvict(value = "unitTypes", allEntries = true)
    public void delete(@NonNull Long id) {
        log.info("Deleting unit type with ID: {}", id);

        if (!unitTypeRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                "Тип підрозділу з ID " + id + " не знайдено"
            );
        }

        unitTypeRepository.deleteById(id);
        log.info("Unit type with ID {} deleted successfully", id);
    }
}
