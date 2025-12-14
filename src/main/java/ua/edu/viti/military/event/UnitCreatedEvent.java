package ua.edu.viti.military.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import ua.edu.viti.military.entity.MilitaryUnit;

import java.time.LocalDateTime;

/**
 * Event який публікується при створенні нового військового підрозділу
 */
@Getter
public class UnitCreatedEvent extends ApplicationEvent {
    
    private final Long unitId;
    private final String unitName;
    private final String unitCode;
    private final String unitTypeName;
    private final Long parentUnitId;
    private final String parentUnitName;
    private final String location;
    private final Integer strength;
    private final String performedBy;
    private final LocalDateTime eventTimestamp;
    
    public UnitCreatedEvent(Object source, 
                           MilitaryUnit unit, 
                           String performedBy) {
        super(source);
        this.unitId = unit.getId();
        this.unitName = unit.getName();
        this.unitCode = unit.getCode();
        this.unitTypeName = unit.getUnitType().getName();
        this.parentUnitId = unit.getParentUnit() != null ? unit.getParentUnit().getId() : null;
        this.parentUnitName = unit.getParentUnit() != null ? unit.getParentUnit().getName() : null;
        this.location = unit.getLocation();
        this.strength = unit.getStrength();
        this.performedBy = performedBy;
        this.eventTimestamp = LocalDateTime.now();
    }
}
