package ua.edu.viti.military.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Журнал операцій з персоналом (audit trail)
 * Зберігає історію всіх змін: призначення, переведення, звільнення тощо
 */
@Entity
@Table(name = "personnel_movements")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonnelMovement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personnel_id", nullable = false)
    private Personnel personnel;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MovementType type;
    
    // Звідки (попередній підрозділ або посада)
    @Column(length = 200)
    private String fromLocation;
    
    // Куди (новий підрозділ або посада)
    @Column(length = 200)
    private String toLocation;
    
    // Попереднє звання (для PROMOTION)
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private Rank previousRank;
    
    // Нове звання (для PROMOTION)
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private Rank newRank;
    
    // Причина операції
    @Column(length = 500)
    private String reason;
    
    // Примітки
    @Column(length = 500)
    private String notes;
    
    // Хто виконав операцію (буде з Security Context)
    @Column(length = 100)
    private String performedBy;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime performedAt;
    
    // Дата набуття чинності (може бути в майбутньому)
    @Column
    private LocalDateTime effectiveDate;
    
    // ВАЖЛИВО: Optimistic Locking для запобігання конфліктів
    @Version
    private Long version;
}
