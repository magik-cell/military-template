package ua.edu.viti.military.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.edu.viti.military.dto.request.SupplyCategoryCreateDTO;
import ua.edu.viti.military.dto.response.SupplyCategoryResponseDTO;
import ua.edu.viti.military.entity.SupplyCategory;
import ua.edu.viti.military.exception.DuplicateResourceException;
import ua.edu.viti.military.exception.ResourceNotFoundException;
import ua.edu.viti.military.repository.SupplyCategoryRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SupplyCategoryService {

    private final SupplyCategoryRepository repository;

    @Transactional
    public SupplyCategoryResponseDTO create(SupplyCategoryCreateDTO dto) {
        log.info("Creating supply category with code {}", dto.getKod());

        if (repository.existsByCode(dto.getKod())) {
            throw new DuplicateResourceException("Категорія з кодом " + dto.getKod() + " вже існує");
        }

        SupplyCategory entity = new SupplyCategory();
        entity.setName(dto.getNazva());
        entity.setCode(dto.getKod());
        entity.setDescription(dto.getOpys());

        SupplyCategory saved = repository.save(entity);
        log.info("Supply category created with id {}", saved.getId());

        return toDto(saved);
    }

    public SupplyCategoryResponseDTO getById(@NonNull Long id) {
        SupplyCategory entity = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Категорію з id " + id + " не знайдено"));
        return toDto(entity);
    }

    public List<SupplyCategoryResponseDTO> getAll() {
        return repository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public void delete(@NonNull Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Категорію з id " + id + " не знайдено");
        }
        repository.deleteById(id);
    }

    private SupplyCategoryResponseDTO toDto(SupplyCategory e) {
        SupplyCategoryResponseDTO d = new SupplyCategoryResponseDTO();
        d.setId(e.getId());
        d.setName(e.getName());
        d.setCode(e.getCode());
        d.setDescription(e.getDescription());
        d.setCreatedAt(e.getCreatedAt());
        d.setUpdatedAt(e.getUpdatedAt());
        return d;
    }
}
