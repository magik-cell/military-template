package ua.edu.viti.military.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import ua.edu.viti.military.entity.Rank;

import java.time.LocalDateTime;

/**
 * Event який публікується при зміні командира підрозділу
 */
@Getter
public class CommanderChangedEvent extends ApplicationEvent {
    
    private final Long unitId;
    private final String unitName;
    private final String unitCode;
    private final Long previousCommanderId;
    private final String previousCommanderName;
    private final Long newCommanderId;
    private final String newCommanderName;
    private final Rank newCommanderRank;
    private final String performedBy;
    private final LocalDateTime eventTimestamp;
    
    public CommanderChangedEvent(Object source,
                                Long unitId,
                                String unitName,
                                String unitCode,
                                Long previousCommanderId,
                                String previousCommanderName,
                                Long newCommanderId,
                                String newCommanderName,
                                Rank newCommanderRank,
                                String performedBy) {
        super(source);
        this.unitId = unitId;
        this.unitName = unitName;
        this.unitCode = unitCode;
        this.previousCommanderId = previousCommanderId;
        this.previousCommanderName = previousCommanderName;
        this.newCommanderId = newCommanderId;
        this.newCommanderName = newCommanderName;
        this.newCommanderRank = newCommanderRank;
        this.performedBy = performedBy;
        this.eventTimestamp = LocalDateTime.now();
    }
}
