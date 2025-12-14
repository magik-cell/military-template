package ua.edu.viti.military.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import ua.edu.viti.military.entity.MovementType;
import ua.edu.viti.military.entity.Rank;

import java.time.LocalDateTime;

@Data
public class MovementRequestDTO {
    
    @NotNull(message = "ID військовослужбовця обов'язковий")
    @Positive
    private Long personnelId;
    
    @NotNull(message = "Тип операції обов'язковий")
    private MovementType type;
    
    // Для TRANSFER
    @Size(max = 200, message = "Попереднє місце служби не може перевищувати 200 символів")
    private String fromLocation;
    
    @Size(max = 200, message = "Нове місце служби не може перевищувати 200 символів")
    private String toLocation;
    
    // Для PROMOTION
    private Rank previousRank;
    
    private Rank newRank;
    
    @Size(max = 500, message = "Причина не може перевищувати 500 символів")
    private String reason;
    
    @Size(max = 500, message = "Примітки не можуть перевищувати 500 символів")
    private String notes;
    
    // Коли набуде чинності (опційно, за замовчуванням зараз)
    private LocalDateTime effectiveDate;
}
