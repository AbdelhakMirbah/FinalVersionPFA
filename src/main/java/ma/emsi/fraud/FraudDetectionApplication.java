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
                // Test avec des valeurs simples
                float score = mlService.predict(
                        0, // type: PAYMENT
                        9000.0, // amount
                        10000.0, // oldBalanceOrg
                        1000.0, // newBalanceOrig
                        0.0, // oldBalanceDest
                        0.0 // newBalanceDest
                );
                System.out.println("✅ Score obtenu : " + score);
            } catch (Exception e) {
                System.out.println("❌ Erreur lors du test : " + e.getMessage());
                e.printStackTrace();
            }
            System.out.println("=========================================\n");
        };
    }
}
