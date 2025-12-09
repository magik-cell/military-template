package ua.edu.viti.military.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ua.edu.viti.military.entity.MilitaryUnit;

import java.util.List;
import java.util.Optional;

@Repository
public interface MilitaryUnitRepository extends JpaRepository<MilitaryUnit, Long> {

   
    Optional<MilitaryUnit> findByCode(String code);

    
    boolean existsByCode(String code);

    List<MilitaryUnit> findByUnitTypeId(Long unitTypeId);

   
    List<MilitaryUnit> findByParentUnitId(Long parentUnitId);


    @Query("SELECT u FROM MilitaryUnit u WHERE u.parentUnit IS NULL")
    List<MilitaryUnit> findTopLevelUnits();

    List<MilitaryUnit> findByCommanderId(Long commanderId);
}
