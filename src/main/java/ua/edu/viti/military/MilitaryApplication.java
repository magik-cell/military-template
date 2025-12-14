package ua.edu.viti.military;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import java.util.TimeZone;

@SpringBootApplication
@EnableJpaAuditing
@EnableAsync  // ← Увімкнення асинхронної обробки Events
public class MilitaryApplication {

    public static void main(String[] args) {
               TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(MilitaryApplication.class, args);
    }
}