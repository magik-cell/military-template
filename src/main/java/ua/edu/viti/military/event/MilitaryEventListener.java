package ua.edu.viti.military.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import ua.edu.viti.military.repository.MilitaryUnitRepository;
import ua.edu.viti.military.repository.PersonnelRepository;

/**
 * Обробник подій для військових підрозділів та персоналу
 * 
 * @TransactionalEventListener - виконується ПІСЛЯ commit транзакції
 * @Async - виконується асинхронно (не блокує основний потік)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MilitaryEventListener {
    
  
        private final ua.edu.viti.military.service.MetricsService metricsService;  // ← Inject MetricsService
    
    /**
     * Обробник призначення військовослужбовця до підрозділу
     * Асинхронний - відправка нотифікацій не блокує основний потік
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handlePersonnelAssigned(PersonnelAssignedEvent event) {
        log.info("Processing PersonnelAssignedEvent: {} ({}) assigned to unit '{}' ({})",
            event.getPersonnelName(), 
            event.getRank(), 
            event.getUnitName(),
            event.getUnitCode());
        
        try {
            // 1. Надіслати email нотифікацію командиру підрозділу (імітація)
            sendEmailToCommander(event);
            
            // 2. Оновити статистику підрозділу
            updateUnitStatistics(event.getUnitId());
            
            // 3. Зареєструвати в журналі змін
            logPersonnelChange(event);
            
            log.info("PersonnelAssignedEvent processed successfully for personnel ID: {}", 
                event.getPersonnelId());
            
        } catch (Exception e) {
            log.error("Error processing PersonnelAssignedEvent for personnel ID: {}", 
                event.getPersonnelId(), e);
            // В реальній системі - повторна спроба або збереження в dead letter queue
        }
    }
    
    /**
     * Обробник переведення військовослужбовця між підрозділами
     * Асинхронний - обробка в background
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handlePersonnelTransferred(PersonnelTransferredEvent event) {
        log.info("Processing PersonnelTransferredEvent: {} ({}) transferred from '{}' to '{}'",
            event.getPersonnelName(),
            event.getRank(),
            event.getFromUnitName(),
            event.getToUnitName());
        
        try {
            // 1. Надіслати нотифікації обом командирам
            sendTransferNotifications(event);
            
            // 2. Оновити статистику обох підрозділів
            updateUnitStatistics(event.getFromUnitId());
            updateUnitStatistics(event.getToUnitId());
            
            // 3. Створити запис в журналі переміщень
            logPersonnelTransfer(event);
            
            log.info("PersonnelTransferredEvent processed successfully for personnel ID: {}", 
                event.getPersonnelId());
            
        } catch (Exception e) {
            log.error("Error processing PersonnelTransferredEvent for personnel ID: {}", 
                event.getPersonnelId(), e);
        }
    }
    
    /**
     * Обробник зміни командира підрозділу
     * Асинхронний
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleCommanderChanged(CommanderChangedEvent event) {
        log.info("Processing CommanderChangedEvent: unit '{}' ({}) - new commander: {} ({})",
            event.getUnitName(),
            event.getUnitCode(),
            event.getNewCommanderName(),
            event.getNewCommanderRank());
        
        try {
            // 1. Надіслати нотифікації вищому керівництву
            sendCommanderChangeNotification(event);
            
            // 2. Оновити права доступу нового командира
            updateCommanderPermissions(event);
            
            // 3. Створити наказ про призначення (імітація)
            generateAppointmentOrder(event);
            
            log.info("CommanderChangedEvent processed successfully for unit ID: {}", 
                event.getUnitId());
            
        } catch (Exception e) {
            log.error("Error processing CommanderChangedEvent for unit ID: {}", 
                event.getUnitId(), e);
        }
    }
    
    /**
     * Обробник створення нового підрозділу
     * Асинхронний
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleUnitCreated(UnitCreatedEvent event) {
        log.info("Processing UnitCreatedEvent: new unit '{}' ({}) created at {}",
            event.getUnitName(),
            event.getUnitCode(),
            event.getLocation());
        
        try {
            // 1. Надіслати нотифікації відповідальним особам
            sendUnitCreationNotification(event);
            
            // 2. Ініціалізувати необхідні ресурси для підрозділу
            initializeUnitResources(event);
            
            // 3. Додати до загальної статистики
            updateGlobalStatistics(event);
            
            log.info("UnitCreatedEvent processed successfully for unit ID: {}", 
                event.getUnitId());
            
        } catch (Exception e) {
            log.error("Error processing UnitCreatedEvent for unit ID: {}", 
                event.getUnitId(), e);
        }
    }
    
    /**
     * Обробник критично низької чисельності підрозділу
     * Синхронний - критична нотифікація виконується одразу
     */
    @EventListener
    public void handleLowStrength(LowStrengthEvent event) {
        log.warn("LOW STRENGTH ALERT: unit '{}' ({}) - current: {}, required: {} ({}% filled, deficit: {})",
            event.getUnitName(),
            event.getUnitCode(),
            event.getCurrentStrength(),
            event.getRequiredStrength(),
            String.format("%.1f", event.getPercentageFilled()),
            event.getDeficit());
        
        // ✅ Зафіксувати metric
        metricsService.recordLowStrengthAlert(
            event.getUnitName(),
            event.getCurrentStrength(),
            event.getRequiredStrength()
        );
        
        // Відправити критичну нотифікацію
        sendCriticalStrengthAlert(event);
        
        // Створити автоматичний запит на поповнення
        createReinforcementRequest(event);
    }
    
    // ==================== Helper методи ====================
    
    private void sendEmailToCommander(PersonnelAssignedEvent event) {
        log.info("Sending email notification to commander of unit '{}'", event.getUnitName());
        
        try {
            Thread.sleep(1500);  // Імітація затримки відправки email
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        log.info("Email sent successfully to unit '{}' commander", event.getUnitName());
    }
    
    private void sendTransferNotifications(PersonnelTransferredEvent event) {
        log.info("Sending transfer notifications to commanders of '{}' and '{}'",
            event.getFromUnitName(), event.getToUnitName());
        
        try {
            Thread.sleep(2000);  // Імітація затримки
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        log.info("Transfer notifications sent successfully");
    }
    
    private void sendCommanderChangeNotification(CommanderChangedEvent event) {
        log.info("Sending commander change notification for unit '{}'", event.getUnitName());
        
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        log.info("Commander change notification sent");
    }
    
    private void sendUnitCreationNotification(UnitCreatedEvent event) {
        log.info("Sending unit creation notification for '{}'", event.getUnitName());
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private void updateUnitStatistics(Long unitId) {
        if (unitId != null) {
            log.info("Updating statistics for unit ID: {}", unitId);
            // В реальній системі - збереження в окрему таблицю статистики
        }
    }
    
    private void updateCommanderPermissions(CommanderChangedEvent event) {
        log.info("Updating permissions for new commander ID: {}", event.getNewCommanderId());
        // В реальній системі - оновлення прав доступу в системі безпеки
    }
    
    private void generateAppointmentOrder(CommanderChangedEvent event) {
        log.info("Generating appointment order for commander of unit '{}'", event.getUnitName());
        // В реальній системі - генерація PDF наказу
    }
    
    private void initializeUnitResources(UnitCreatedEvent event) {
        log.info("Initializing resources for new unit '{}'", event.getUnitName());
        // В реальній системі - створення записів в інших системах (фінанси, логістика)
    }
    
    private void updateGlobalStatistics(UnitCreatedEvent event) {
        log.info("Updating global statistics with new unit '{}'", event.getUnitName());
        // В реальній системі - оновлення загальної статистики
    }
    
    private void logPersonnelChange(PersonnelAssignedEvent event) {
        log.info("Logging personnel assignment: {} to unit '{}'",
            event.getMilitaryId(), event.getUnitName());
        // В реальній системі - запис в audit log таблицю
    }
    
    private void logPersonnelTransfer(PersonnelTransferredEvent event) {
        log.info("Logging personnel transfer: {} from '{}' to '{}'",
            event.getMilitaryId(), event.getFromUnitName(), event.getToUnitName());
        // В реальній системі - запис в movement log таблицю
    }
    
    private void sendCriticalStrengthAlert(LowStrengthEvent event) {
        log.error("CRITICAL: Sending SMS/Push notification about low strength for unit '{}'",
            event.getUnitName());
        log.error("CRITICAL ALERT: Unit requires {} additional personnel immediately",
            event.getDeficit());
        // В реальній системі - відправка SMS/Push через сервіс нотифікацій
    }
    
    private void createReinforcementRequest(LowStrengthEvent event) {
        log.warn("Creating automatic reinforcement request for unit '{}' - {} personnel needed",
            event.getUnitName(), event.getDeficit());
        // В реальній системі - створення запиту в систему управління персоналом
    }
}
