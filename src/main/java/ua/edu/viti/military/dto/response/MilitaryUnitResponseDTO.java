package ua.edu.viti.military.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MilitaryUnitResponseDTO {
    private Long id;
    private String name;
    private String code;
    private UnitTypeResponseDTO unitType;
    private MilitaryUnitResponseDTO parentUnit;
    private PersonnelShortResponseDTO commander;
    private String location;
    private LocalDate formationDate;
    private Integer strength;
    private Integer currentStrength;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
