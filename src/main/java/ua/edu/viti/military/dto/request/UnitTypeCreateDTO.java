package ua.edu.viti.military.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnitTypeCreateDTO {

    @NotBlank(message = "Назва типу підрозділу обов'язкова")
    @Size(max = 100, message = "Назва не може бути довшою за 100 символів")
    private String nazva;

    @NotBlank(message = "Код типу підрозділу обов'язковий")
    @Size(max = 50, message = "Код не може бути довшим за 50 символів")
    private String kod;

    @Size(max = 500, message = "Опис не може бути довшим за 500 символів")
    private String opys;

    @NotNull(message = "Рівень ієрархії обов'язковий")
    @Positive(message = "Рівень ієрархії має бути позитивним")
    private Integer ierarhiya;

    @NotNull(message = "Типова чисельність обов'язкова")
    @Positive(message = "Типова чисельність має бути позитивною")
    private Integer typovyiRozmir;
}
