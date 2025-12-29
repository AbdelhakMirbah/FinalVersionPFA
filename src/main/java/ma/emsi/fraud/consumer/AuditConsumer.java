package ma.emsi.fraud.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.emsi.fraud.model.FraudCheck;
import ma.emsi.fraud.repository.FraudCheckRepository;
import ma.emsi.fraud.service.FraudStreamService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditConsumer {

    private final FraudCheckRepository repository;
    private final FraudStreamService streamService;

    /**
     * Listener Kafka qui consomme les messages du topic "fraud-checks"
     * et les sauvegarde dans PostgreSQL
     */
    @KafkaListener(topics = "fraud-checks", groupId = "fraud-consumer-group")
    public void consume(@org.springframework.lang.NonNull FraudCheck fraudCheck) {
        log.info("Received fraud check from Kafka: {}", fraudCheck);

        try {
            // Sauvegarder dans la base de données
            FraudCheck saved = repository.save(fraudCheck);

            // Broadcast to SSE clients - LIVE UPDATE!
            streamService.pushEvent(saved);

            log.info("✅ Fraud check saved to database with ID: {}", saved.getId());
        } catch (Exception e) {
            log.error("❌ Failed to save fraud check to database", e);
        }
    }
}
