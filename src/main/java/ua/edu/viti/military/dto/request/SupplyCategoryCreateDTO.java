package ua.edu.viti.military.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SupplyCategoryCreateDTO {

    @NotBlank
    @Size(max = 200)
    private String nazva;

    @NotBlank
    @Size(max = 100)
    private String kod;

    @Size(max = 2000)
    private String opys;
}
