package ua.edu.viti.military.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ua.edu.viti.military.entity.MovementType;
import ua.edu.viti.military.entity.PersonnelMovement;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PersonnelMovementRepository extends JpaRepository<PersonnelMovement, Long> {
    
    /**
     * Отримати історію всіх операцій для конкретного військовослужбовця
     * Відсортовано від новіших до старіших
     */
    List<PersonnelMovement> findByPersonnelIdOrderByPerformedAtDesc(Long personnelId);
    
    /**
     * Знайти всі операції певного типу
     */
    List<PersonnelMovement> findByTypeOrderByPerformedAtDesc(MovementType type);
    
    /**
     * Знайти операції за період
     */
    List<PersonnelMovement> findByPerformedAtBetween(
        LocalDateTime start, 
        LocalDateTime end
    );
    
    /**
     * Знайти операції які набудуть чинності в майбутньому
     */
    @Query("SELECT m FROM PersonnelMovement m " +
           "WHERE m.effectiveDate > CURRENT_TIMESTAMP " +
           "ORDER BY m.effectiveDate ASC")
    List<PersonnelMovement> findPendingMovements();
    
    /**
     * Статистика: кількість операцій певного типу за період
     */
    @Query("SELECT COUNT(m) FROM PersonnelMovement m " +
           "WHERE m.type = :type AND m.performedAt BETWEEN :start AND :end")
    Long countByTypeAndPeriod(
        @Param("type") MovementType type,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );
    
    /**
     * Знайти всі переведення за період
     */
    @Query("SELECT m FROM PersonnelMovement m " +
           "WHERE m.type = 'TRANSFER' " +
           "AND m.performedAt BETWEEN :start AND :end " +
           "ORDER BY m.performedAt DESC")
    List<PersonnelMovement> findTransfersBetweenUnits(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );
    
    /**
     * Отримати останню операцію для військовослужбовця
     */
    @Query("SELECT m FROM PersonnelMovement m " +
           "WHERE m.personnel.id = :personnelId " +
           "ORDER BY m.performedAt DESC " +
           "LIMIT 1")
    PersonnelMovement findLatestMovement(@Param("personnelId") Long personnelId);
}
