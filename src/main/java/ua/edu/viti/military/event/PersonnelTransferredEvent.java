package ua.edu.viti.military.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import ua.edu.viti.military.entity.Rank;

import java.time.LocalDateTime;

/**
 * Event який публікується при переведенні військовослужбовця між підрозділами
 */
@Getter
public class PersonnelTransferredEvent extends ApplicationEvent {
    
    private final Long personnelId;
    private final String personnelName;
    private final String militaryId;
    private final Rank rank;
    private final Long fromUnitId;
    private final String fromUnitName;
    private final Long toUnitId;
    private final String toUnitName;
    private final String performedBy;
    private final LocalDateTime eventTimestamp;
    
    public PersonnelTransferredEvent(Object source, 
                                    Long personnelId,
                                    String personnelName,
                                    String militaryId,
                                    Rank rank,
                                    Long fromUnitId,
                                    String fromUnitName,
                                    Long toUnitId,
                                    String toUnitName,
                                    String performedBy) {
        super(source);
        this.personnelId = personnelId;
        this.personnelName = personnelName;
        this.militaryId = militaryId;
        this.rank = rank;
        this.fromUnitId = fromUnitId;
        this.fromUnitName = fromUnitName;
        this.toUnitId = toUnitId;
        this.toUnitName = toUnitName;
        this.performedBy = performedBy;
        this.eventTimestamp = LocalDateTime.now();
    }
}
