package ua.edu.viti.military.dto.response;

import lombok.Data;
import ua.edu.viti.military.entity.MovementType;
import ua.edu.viti.military.entity.Rank;

import java.time.LocalDateTime;

@Data
public class MovementResponseDTO {
    
    private Long id;
    private Long personnelId;
    private String personnelName;  // ПІБ для зручності
    private String militaryId;     // Військовий квиток
    
    private MovementType type;
    
    private String fromLocation;
    private String toLocation;
    
    private Rank previousRank;
    private Rank newRank;
    
    private String reason;
    private String notes;
    
    private String performedBy;
    private LocalDateTime performedAt;
    private LocalDateTime effectiveDate;
}
