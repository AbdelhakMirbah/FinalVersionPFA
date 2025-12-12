package ma.emsi.fraud.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.emsi.fraud.model.FraudCheck;
import ma.emsi.fraud.model.FraudRequest;
import ma.emsi.fraud.model.FraudResponse;
import ma.emsi.fraud.service.EnrichmentService;
import ma.emsi.fraud.service.MlService;
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
                    // type=0 (PAYMENT par défaut), amount, oldBalance, newBalance,
                    // oldBalanceDest=0, newBalanceDest=0 (valeurs par défaut)
                    float score = mlService.predict(
                            0, // type: PAYMENT
                            request.amount(),
                            request.oldBalance(),
                            request.newBalance(),
                            0.0, // oldBalanceDest (pas disponible dans FraudRequest)
                            0.0 // newBalanceDest (pas disponible dans FraudRequest)
                    );

                    // Déterminer le niveau de risque
                    String risk = score > 0.5 ? "HIGH" : "LOW";

                    log.info("Fraud score: {}, Risk: {}", score, risk);

                    // 3. Envoi à Kafka (Fire-and-Forget)
                    FraudCheck fraudCheck = new FraudCheck();
                    fraudCheck.setAmount(request.amount());
                    fraudCheck.setScore(score);
                    fraudCheck.setRisk(risk);

                    kafkaTemplate.send(KAFKA_TOPIC, fraudCheck);
                    log.debug("Sent to Kafka topic: {}", KAFKA_TOPIC);

                    // 4. Retour immédiat au client
                    return new FraudResponse(score, risk);
                })
                .doOnError(error -> log.error("Fraud check failed", error));
    }
}
