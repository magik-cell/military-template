package ua.edu.viti.military.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ua.edu.viti.military.entity.Personnel;
import ua.edu.viti.military.entity.Rank;
import ua.edu.viti.military.entity.SecurityClearance;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PersonnelRepository extends JpaRepository<Personnel, Long> {

  
    Optional<Personnel> findByMilitaryId(String militaryId);

   
    boolean existsByMilitaryId(String militaryId);

    List<Personnel> findByUnitId(Long unitId);

  
    List<Personnel> findByRank(Rank rank);

   
    List<Personnel> findByContractEndDateBefore(LocalDate date);

    List<Personnel> findBySecurityClearance(SecurityClearance clearance);

    
    List<Personnel> findBySpecializationContaining(String keyword);

   
    @Query("SELECT p FROM Personnel p WHERE p.contractEndDate >= CURRENT_DATE")
    List<Personnel> findActivePersonnel();

    List<Personnel> findByFirstNameContainingAndLastNameContaining(String firstName, String lastName);

    
    long countByRank(Rank rank);

   
    long countByUnitId(Long unitId);
}
