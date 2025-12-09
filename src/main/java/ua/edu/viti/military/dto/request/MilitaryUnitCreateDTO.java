package ua.edu.viti.military.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MilitaryUnitCreateDTO {

    @NotBlank(message = "Назва підрозділу обов'язкова")
    @Size(max = 150, message = "Назва не може бути довшою за 150 символів")
    private String nazva;

    @NotBlank(message = "Код підрозділу обов'язковий")
    @Size(max = 50, message = "Код не може бути довшим за 50 символів")
    private String kod;

    @NotNull(message = "Тип підрозділу обов'язковий")
    @Positive(message = "ID типу підрозділу має бути позитивним")
    private Long typPidrozdilu;

    @Positive(message = "ID батьківського підрозділу має бути позитивним")
    private Long batkivskyyPidrozdil;

    @Positive(message = "ID командира має бути позитивним")
    private Long komandyr;

    @Size(max = 200, message = "Місцезнаходження не може бути довшим за 200 символів")
    private String lokatsiya;

    private LocalDate dataFormuvannya;

    @Positive(message = "Штатна чисельність має бути позитивною")
    private Integer shtatnaChuselna;

    @Positive(message = "Поточна чисельність має бути позитивною")
    private Integer potochnaChuselna;
}
