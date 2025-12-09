package ua.edu.viti.military.dto.response;

import lombok.Data;

import java.time.Instant;

@Data
public class SupplyCategoryResponseDTO {
    private Long id;
    private String name;
    private String code;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
}
