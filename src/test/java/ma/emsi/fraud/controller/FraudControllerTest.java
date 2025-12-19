package ma.emsi.fraud.controller;

import ma.emsi.fraud.model.FraudCheck;
import ma.emsi.fraud.model.FraudRequest;
import ma.emsi.fraud.service.EnrichmentService;
import ma.emsi.fraud.service.MlService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@WebFluxTest(FraudController.class)
class FraudControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private EnrichmentService enrichmentService;

    @MockBean
    private MlService mlService;

    @MockBean
    private KafkaTemplate<String, FraudCheck> kafkaTemplate;

    @Test
    void checkFraud_ShouldReturnLowRisk_WhenScoreIsLow() {
        // Arrange
        FraudRequest request = new FraudRequest(100.0, 1000.0, 900.0, "127.0.0.1", "test@example.com");

        // Mock EnrichmentService to return a clean result
        // Note: We need to use the record defined in EnrichmentService if it was
        // public,
        // but since we deleted the duplicate model, we need to ensure we import the
        // right one.
        // Wait, the record in EnrichmentService is public? Yes, line 48 in
        // EnrichmentService.java.
        // But java imports for inner static classes (records are static) require the
        // outer class.
        // So import should be ma.emsi.fraud.service.EnrichmentService.EnrichmentResult;

        when(enrichmentService.enrich(anyString(), anyString()))
                .thenReturn(Mono.just(new EnrichmentService.EnrichmentResult(false, false)));

        // Mock MlService to return a low score
        when(mlService.predict(anyInt(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(0.00001f);

        // Mock Kafka (returns future)
        when(kafkaTemplate.send(anyString(), any(FraudCheck.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/fraud/check")
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.score").isEqualTo(0.00001)
                .jsonPath("$.risk").isEqualTo("LOW");
    }

    @Test
    void checkFraud_ShouldReturnHighRisk_WhenScoreIsHigh() {
        // Arrange
        FraudRequest request = new FraudRequest(10000.0, 1000.0, 900.0, "127.0.0.1", "fraud@example.com");

        when(enrichmentService.enrich(anyString(), anyString()))
                .thenReturn(Mono.just(new EnrichmentService.EnrichmentResult(true, true)));

        when(mlService.predict(anyInt(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(0.8f);

        when(kafkaTemplate.send(anyString(), any(FraudCheck.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/fraud/check")
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.score").isEqualTo(0.8)
                .jsonPath("$.risk").isEqualTo("HIGH");
    }
}
