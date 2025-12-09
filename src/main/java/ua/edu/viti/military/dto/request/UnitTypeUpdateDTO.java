package ua.edu.viti.military.dto.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnitTypeUpdateDTO {

    @Size(max = 100, message = "Назва не може бути довшою за 100 символів")
    private String nazva;

    @Size(max = 500, message = "Опис не може бути довшим за 500 символів")
    private String opys;

    @Positive(message = "Типова чисельність має бути позитивною")
    private Integer typovyiRozmir;
}
