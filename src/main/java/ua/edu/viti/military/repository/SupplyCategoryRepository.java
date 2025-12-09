package ua.edu.viti.military.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.edu.viti.military.entity.SupplyCategory;

public interface SupplyCategoryRepository extends JpaRepository<SupplyCategory, Long> {
    boolean existsByCode(String code);
}
