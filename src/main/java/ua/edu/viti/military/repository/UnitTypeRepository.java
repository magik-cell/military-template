package ua.edu.viti.military.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.edu.viti.military.entity.UnitType;

import java.util.Optional;

@Repository
public interface UnitTypeRepository extends JpaRepository<UnitType, Long> {

 
    Optional<UnitType> findByName(String name);

    boolean existsByName(String name);

    Optional<UnitType> findByCode(String code);

    
    boolean existsByCode(String code);
}
