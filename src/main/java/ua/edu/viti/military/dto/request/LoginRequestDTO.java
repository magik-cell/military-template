package ua.edu.viti.military.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDTO {
    
    @NotBlank(message = "Username обов'язковий")
    private String username;
    
    @NotBlank(message = "Пароль обов'язковий")
    private String password;
}
