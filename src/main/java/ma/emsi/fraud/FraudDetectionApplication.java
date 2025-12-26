package ma.emsi.fraud;

import ma.emsi.fraud.service.MlService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class FraudDetectionApplication {

    public static void main(String[] args) {
        SpringApplication.run(FraudDetectionApplication.class, args);
    }

    @Bean
    CommandLineRunner testModel(MlService mlService) {
        return args -> {
            System.out.println("\n========== TEST DU MODÈLE ONNX ==========");
            try {
                // Test 1: Cas Normal (PAYMENT, 9000, score attendu ~0)
                float scoreNormal = mlService.predict(0, 9000.0, 10000.0, 1000.0, 0.0, 0.0);
                System.out.println("✅ Score Normal (Payment) : " + scoreNormal);

                // Test 2: Cas Fraude typique (TRANSFER, compte vidé, gros montant)
                // Type 1 = TRANSFER
                float scoreFraud = mlService.predict(1, 1000000.0, 1000000.0, 0.0, 0.0, 0.0);
                System.out.println("🚨 Score Fraude (Transfer) : " + scoreFraud);
            } catch (Exception e) {
                System.out.println("❌ Erreur lors du test : " + e.getMessage());
                e.printStackTrace();
            }
            System.out.println("=========================================\n");
        };
    }
}
