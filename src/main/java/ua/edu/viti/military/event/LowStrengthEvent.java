package ua.edu.viti.military.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * Event який публікується при критично низькій чисельності підрозділу
 * (коточна чисельність < 70% від штатної)
 */
@Getter
public class LowStrengthEvent extends ApplicationEvent {
    
    private final Long unitId;
    private final String unitName;
    private final String unitCode;
    private final Integer currentStrength;
    private final Integer requiredStrength;
    private final Integer deficit;
    private final Double percentageFilled;
    private final LocalDateTime eventTimestamp;
    
    public LowStrengthEvent(Object source,
                           Long unitId,
                           String unitName,
                           String unitCode,
                           Integer currentStrength,
                           Integer requiredStrength) {
        super(source);
        this.unitId = unitId;
        this.unitName = unitName;
        this.unitCode = unitCode;
        this.currentStrength = currentStrength;
        this.requiredStrength = requiredStrength;
        this.deficit = requiredStrength - currentStrength;
        this.percentageFilled = (currentStrength * 100.0) / requiredStrength;
        this.eventTimestamp = LocalDateTime.now();
    }
}
