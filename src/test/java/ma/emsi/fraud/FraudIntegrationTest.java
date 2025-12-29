package ma.emsi.fraud;

import ma.emsi.fraud.model.FraudCheck;
import ma.emsi.fraud.model.FraudRequest;
import ma.emsi.fraud.repository.FraudCheckRepository;
import ma.emsi.fraud.service.EnrichmentService;
import ma.emsi.fraud.service.MlService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(partitions = 1, topics = { "fraud-checks" })
@DirtiesContext
class FraudIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private MlService mlService;

    @MockBean
    private EnrichmentService enrichmentService;

    @MockBean
    private FraudCheckRepository repository;

    @Test
    void testFullFraudCheckFlow() {
        // Arrange
        FraudRequest request = new FraudRequest(
                500.0, // amount
                1000.0, // oldBalance
                500.0, // newBalance
                1, // type (TRANSFER)
                0.0, // oldBalanceDest
                500.0, // newBalanceDest
                "192.168.1.1",
                "test@integration.com");

        // Mock Enrichment Service
        when(enrichmentService.enrich(any(String.class), any(String.class)))
                .thenReturn(reactor.core.publisher.Mono
                        .just(new ma.emsi.fraud.service.EnrichmentService.EnrichmentResult(false, false)));

        // Mock ML Service to return High Risk score
        when(mlService.predict(anyInt(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(0.85f);

        // Mock Repository to simulate save
        when(repository.save(any(FraudCheck.class))).thenAnswer(i -> {
            FraudCheck fc = (FraudCheck) i.getArguments()[0];
            fc.setId(1L);
            return fc;
        });

        // Act: Call API
        webTestClient.post().uri("/api/v1/fraud/check")
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.score").isEqualTo(0.85)
                .jsonPath("$.risk").isEqualTo("HIGH");

        // Assert: Verify Async processing (Kafka -> Consumer -> DB)
        // This confirms the message went through Kafka and reached the consumer
        verify(repository, timeout(10000).times(1)).save(any(FraudCheck.class));
    }
}
