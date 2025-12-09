package ua.edu.viti.military.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.edu.viti.military.entity.MedicalCategory;
import ua.edu.viti.military.entity.Rank;
import ua.edu.viti.military.entity.SecurityClearance;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonnelCreateDTO {

    @NotBlank(message = "Військовий ID обов'язковий")
    @Size(max = 50, message = "Військовий ID не може бути довшим за 50 символів")
    private String viyskovyiId;

    @NotBlank(message = "Ім'я обов'язкове")
    @Size(max = 100, message = "Ім'я не може бути довшим за 100 символів")
    private String imya;

    @NotBlank(message = "Прізвище обов'язкове")
    @Size(max = 100, message = "Прізвище не може бути довшим за 100 символів")
    private String prizvyshche;

    @Size(max = 100, message = "По батькові не може бути довшим за 100 символів")
    private String pobatkovi;

    @NotNull(message = "Звання обов'язкове")
    private Rank zvannya;

    @Size(max = 100, message = "Спеціалізація не може бути довшою за 100 символів")
    private String spetsializatsiya;

    @Positive(message = "ID підрозділу має бути позитивним")
    private Long pidrozdilId;

    @NotNull(message = "Дата початку контракту обов'язкова")
    @PastOrPresent(message = "Дата початку контракту не може бути в майбутньому")
    private LocalDate dataPochatku;

    @NotNull(message = "Дата закінчення контракту обов'язкова")
    @Future(message = "Дата закінчення контракту має бути в майбутньому")
    private LocalDate dataZakinchennya;

    private SecurityClearance dopusk;

    private MedicalCategory medkategoriya;

    @Size(max = 20, message = "Номер телефону не може бути довшим за 20 символів")
    @Pattern(regexp = "^[+]?[0-9]{10,}$", message = "Невірний формат номера телефону")
    private String telefon;

    @Email(message = "Невірна адреса електронної пошти")
    @Size(max = 100, message = "Email не може бути довшим за 100 символів")
    private String email;
}
