package ua.edu.viti.military.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import ua.edu.viti.military.entity.Personnel;
import ua.edu.viti.military.entity.Rank;

import java.time.LocalDateTime;

/**
 * Event який публікується при призначенні військовослужбовця до підрозділу
 */
@Getter
public class PersonnelAssignedEvent extends ApplicationEvent {
    
    private final Long personnelId;
    private final String personnelName;
    private final String militaryId;
    private final Rank rank;
    private final Long unitId;
    private final String unitName;
    private final String unitCode;
    private final String performedBy;
    private final LocalDateTime eventTimestamp;
    
    public PersonnelAssignedEvent(Object source, Personnel personnel, 
                                  String unitName, String unitCode, 
                                  String performedBy) {
        super(source);
        this.personnelId = personnel.getId();
        this.personnelName = personnel.getFirstName() + " " + personnel.getLastName();
        this.militaryId = personnel.getMilitaryId();
        this.rank = personnel.getRank();
        this.unitId = personnel.getUnit() != null ? personnel.getUnit().getId() : null;
        this.unitName = unitName;
        this.unitCode = unitCode;
        this.performedBy = performedBy;
        this.eventTimestamp = LocalDateTime.now();
    }
}
