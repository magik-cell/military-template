package ua.edu.viti.military.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnitTypeResponseDTO {
    private Long id;
    private String name;
    private String code;
    private String description;
    private Integer hierarchy;
    private Integer typicalSize;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
