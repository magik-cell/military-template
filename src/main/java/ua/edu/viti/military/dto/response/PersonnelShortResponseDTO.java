package ua.edu.viti.military.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.edu.viti.military.entity.Rank;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonnelShortResponseDTO {
    private Long id;
    private String militaryId;
    private String firstName;
    private String lastName;
    private String middleName;
    private Rank rank;
    private LocalDate contractEndDate;
}
