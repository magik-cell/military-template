package ua.edu.viti.military.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.edu.viti.military.entity.MedicalCategory;
import ua.edu.viti.military.entity.Rank;
import ua.edu.viti.military.entity.SecurityClearance;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonnelResponseDTO {
    private Long id;
    private String militaryId;
    private String firstName;
    private String lastName;
    private String middleName;
    private Rank rank;
    private String specialization;
    private MilitaryUnitResponseDTO unit;
    private LocalDate contractStartDate;
    private LocalDate contractEndDate;
    private SecurityClearance securityClearance;
    private MedicalCategory medicalCategory;
    private String phoneNumber;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
