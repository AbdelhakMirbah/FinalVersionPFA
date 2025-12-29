package ma.emsi.fraud.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.emsi.fraud.model.FraudCheck;
import ma.emsi.fraud.model.FraudRequest;
import ma.emsi.fraud.model.FraudResponse;
import ma.emsi.fraud.service.EnrichmentService;
import ma.emsi.fraud.service.MlService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/fraud")
@RequiredArgsConstructor
@Slf4j
public class FraudController {

    private final EnrichmentService enrichmentService;
    private final MlService mlService;
    private final KafkaTemplate<String, FraudCheck> kafkaTemplate;

    @Value("${fraud.risk.threshold:0.0001}")
    private double riskThreshold;

    private static final String KAFKA_TOPIC = "fraud-checks";

    /**
     * Endpoint principal de détection de fraude
     * Flow: Enrichissement (Async) -> ML (Sync) -> Kafka (Fire-and-Forget) ->
     * Réponse
     */
    @PostMapping("/check")
    public Mono<FraudResponse> checkFraud(@RequestBody FraudRequest request) {
        log.info("Received fraud check request: {}", request);

        // 1. Appel EnrichmentService (Async avec WebFlux)
        return enrichmentService.enrich(request.ip(), request.email())
                .map(enrichment -> {
                    log.debug("Enrichment result: {}", enrichment);

                    // 2. Appel MlService (Sync) avec 6 features
                    // Defaults: type=0 (PAYMENT), dest balances = 0.0 if null
                    int type = request.type() != null ? request.type() : 0;
                    double oldBalanceDest = request.oldBalanceDest() != null ? request.oldBalanceDest() : 0.0;
                    double newBalanceDest = request.newBalanceDest() != null ? request.newBalanceDest() : 0.0;

                    float score = mlService.predict(
                            type,
                            request.amount(),
                            request.oldBalance(),
                            request.newBalance(),
                            oldBalanceDest,
                            newBalanceDest);

                    // Déterminer le niveau de risque (seuil configurable)
                    String risk = score > riskThreshold ? "HIGH" : "LOW";

                    log.info("Fraud score: {}, Risk: {}", score, risk);

                    // 3. Envoi à Kafka (Fire-and-Forget) -> Wrapped in try-catch to ensure API
                    // response even if Kafka fails
                    try {
                        FraudCheck fraudCheck = new FraudCheck();
                        fraudCheck.setAmount(request.amount());
                        fraudCheck.setScore(score);
                        fraudCheck.setRisk(risk);
                        fraudCheck.setTransactionType(type);
                        fraudCheck.setOldBalance(request.oldBalance());
                        fraudCheck.setNewBalance(request.newBalance());
                        fraudCheck.setOldBalanceDest(oldBalanceDest);
                        fraudCheck.setNewBalanceDest(newBalanceDest);
                        fraudCheck.setIpAddress(request.ip());
                        fraudCheck.setEmail(request.email());
                        fraudCheck.setCreatedAt(java.time.LocalDateTime.now()); // Set date immediately

                        kafkaTemplate.send(KAFKA_TOPIC, fraudCheck);
                        log.debug("Sent to Kafka topic: {}", KAFKA_TOPIC);
                    } catch (Exception kafkaError) {
                        log.error("Failed to send to Kafka (Non-blocking): {}", kafkaError.getMessage(), kafkaError);
                    }

                    // 4. Retour immédiat au client
                    return new FraudResponse(score, risk);
                })
                .doOnError(error -> log.error("Fraud check failed", error))
                .onErrorResume(e -> {
                    log.warn("Returning fallback response due to error: {}", e.getMessage());
                    return Mono.just(new FraudResponse(-1.0f, "ERROR"));
                });
    }
}
