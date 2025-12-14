package ua.edu.viti.military.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Сервіс для збору metrics (Micrometer)
 * Відстежує операції з personnel та military units
 */
@Service
@Slf4j
public class MetricsService {
    
    private final MeterRegistry meterRegistry;
    
    // Counters - лічильники подій
    private final Counter personnelAssignedCounter;
    private final Counter personnelTransferredCounter;
    private final Counter commanderChangedCounter;
    private final Counter unitsCreatedCounter;
    private final Counter lowStrengthAlertsCounter;
    
    // Timers - вимірювання тривалості операцій
    private final Timer personnelOperationTimer;
    private final Timer unitOperationTimer;
    
    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // Ініціалізація counters
        this.personnelAssignedCounter = Counter.builder("military.personnel.assigned")
            .description("Total number of personnel assigned to units")
            .tag("type", "personnel")
            .register(meterRegistry);
        
        this.personnelTransferredCounter = Counter.builder("military.personnel.transferred")
            .description("Total number of personnel transfers")
            .tag("type", "personnel")
            .register(meterRegistry);
        
        this.commanderChangedCounter = Counter.builder("military.commander.changed")
            .description("Total number of commander changes")
            .tag("type", "unit")
            .register(meterRegistry);
        
        this.unitsCreatedCounter = Counter.builder("military.units.created")
            .description("Total number of military units created")
            .tag("type", "unit")
            .register(meterRegistry);
        
        this.lowStrengthAlertsCounter = Counter.builder("military.alerts.low_strength")
            .description("Number of low strength alerts triggered")
            .tag("severity", "warning")
            .register(meterRegistry);
        
        // Ініціалізація timers
        this.personnelOperationTimer = Timer.builder("military.operations.personnel.duration")
            .description("Time taken for personnel operations")
            .tag("operation", "personnel")
            .register(meterRegistry);
        
        this.unitOperationTimer = Timer.builder("military.operations.unit.duration")
            .description("Time taken for unit operations")
            .tag("operation", "unit")
            .register(meterRegistry);
    }
    
    /**
     * Зафіксувати призначення військовослужбовця
     */
    public void recordPersonnelAssigned(String personnelName, String unitName) {
        personnelAssignedCounter.increment();
        log.debug("Metric recorded: personnel assigned - {}, unit - {}", personnelName, unitName);
    }
    
    /**
     * Зафіксувати переведення військовослужбовця
     */
    public void recordPersonnelTransferred(String personnelName, String fromUnit, String toUnit) {
        personnelTransferredCounter.increment();
        log.debug("Metric recorded: personnel transferred - {}, from {} to {}", 
            personnelName, fromUnit, toUnit);
    }
    
    /**
     * Зафіксувати зміну командира
     */
    public void recordCommanderChanged(String unitName) {
        commanderChangedCounter.increment();
        log.debug("Metric recorded: commander changed for unit - {}", unitName);
    }
    
    /**
     * Зафіксувати створення підрозділу
     */
    public void recordUnitCreated(String unitName) {
        unitsCreatedCounter.increment();
        log.debug("Metric recorded: unit created - {}", unitName);
    }
    
    /**
     * Зафіксувати попередження про низьку чисельність
     */
    public void recordLowStrengthAlert(String unitName, Integer currentStrength, Integer requiredStrength) {
        lowStrengthAlertsCounter.increment();
        
        // Додатковий gauge для відсотка укомплектованості
        Double percentage = (currentStrength.doubleValue() / requiredStrength.doubleValue()) * 100;
        meterRegistry.gauge("military.unit.strength.percentage", percentage);
        
        log.warn("Low strength alert metric incremented for: {} ({}%)", unitName, percentage);
    }
    
    /**
     * Виміряти тривалість операції з personnel
     */
    public <T> T measurePersonnelOperation(java.util.function.Supplier<T> operation) {
        return personnelOperationTimer.record(operation);
    }
    
    /**
     * Виміряти тривалість операції з підрозділом
     */
    public <T> T measureUnitOperation(java.util.function.Supplier<T> operation) {
        return unitOperationTimer.record(operation);
    }
    
    /**
     * Створити явний таймер
     */
    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }
    
    /**
     * Зупинити таймер
     */
    public void stopTimer(Timer.Sample sample, String timerName) {
        sample.stop(Timer.builder(timerName)
            .register(meterRegistry));
    }
}
