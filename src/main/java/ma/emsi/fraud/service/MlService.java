package ma.emsi.fraud.service;

import ai.djl.Model;
import ai.djl.inference.Predictor;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.translate.Batchifier;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Slf4j
public class MlService {

    @Value("${fraud.model.path}")
    private String modelPath;

    private Predictor<float[], Float> predictor;
    private Model model;

    /**
     * Charge le modèle ONNX au démarrage de l'application
     */
    @PostConstruct
    public void loadModel() throws Exception {
        log.info("Loading ONNX model from: {}", modelPath);

        Path path = Paths.get(modelPath);

        // Définition du Translator pour convertir les données
        Translator<float[], Float> translator = new Translator<>() {
            @Override
            public NDList processInput(TranslatorContext ctx, float[] input) {
                NDManager manager = ctx.getNDManager();
                // Créer directement un tableau 2D [1, 6] pour 6 features
                float[][] input2D = new float[1][6];
                System.arraycopy(input, 0, input2D[0], 0, 6);
                NDArray array = manager.create(input2D);
                return new NDList(array);
            }

            @Override
            public Float processOutput(TranslatorContext ctx, NDList list) {
                // Le modèle RandomForest retourne 2 sorties :
                // - Sortie 0 : labels (int64) - classe prédite (0 ou 1)
                // - Sortie 1 : probabilities (float) - probabilités [proba_0, proba_1]

                try {
                    // Essayer d'abord d'obtenir les probabilités (sortie 1)
                    if (list.size() > 1) {
                        float[] probabilities = list.get(1).toFloatArray();
                        return probabilities.length > 1 ? probabilities[1] : probabilities[0];
                    }

                    // Sinon, utiliser la classe prédite (0 ou 1) comme score
                    long[] labels = list.get(0).toLongArray();
                    return labels.length > 0 ? (float) labels[0] : 0.0f;
                } catch (Exception e) {
                    // Fallback : retourner 0.5 (incertain)
                    return 0.5f;
                }
            }

            @Override
            public Batchifier getBatchifier() {
                // ONNX Runtime ne supporte pas STACK, on utilise NONE
                return null; // null = pas de batching
            }
        };

        // Chargement du modèle
        model = Model.newInstance("fraud-detection");
        model.load(path);

        predictor = model.newPredictor(translator);

        log.info("✅ ONNX model loaded successfully!");
    }

    /**
     * Prédit le score de fraude
     * 
     * @param type           Type de transaction (0=PAYMENT, 1=TRANSFER, 2=CASH_OUT,
     *                       3=DEBIT, 4=CASH_IN)
     * @param amount         Montant de la transaction
     * @param oldBalanceOrg  Ancien solde origine
     * @param newBalanceOrig Nouveau solde origine
     * @param oldBalanceDest Ancien solde destination
     * @param newBalanceDest Nouveau solde destination
     * @return Score de fraude (0.0 à 1.0)
     */
    public float predict(int type, double amount, double oldBalanceOrg, double newBalanceOrig,
            double oldBalanceDest, double newBalanceDest) {
        try {
            // Préparer le vecteur de 6 features (comme dans le script Python)
            float[] features = new float[6];
            features[0] = (float) type; // type encodé
            features[1] = (float) amount; // amount
            features[2] = (float) oldBalanceOrg; // oldbalanceOrg
            features[3] = (float) newBalanceOrig; // newbalanceOrig
            features[4] = (float) oldBalanceDest; // oldbalanceDest
            features[5] = (float) newBalanceDest; // newbalanceDest

            log.info("🔍 Predicting with features: [{}, {}, {}, {}, {}, {}]",
                    features[0], features[1], features[2], features[3], features[4], features[5]);

            // Faire la prédiction
            float score = predictor.predict(features);

            log.info("✅ Prediction score: {}", score);

            return score;
        } catch (Exception e) {
            log.error("❌ Prediction failed with error: {}", e.getMessage(), e);
            return -1.0f; // Valeur d'erreur
        }
    }

    @PreDestroy
    public void cleanup() {
        if (predictor != null) {
            predictor.close();
        }
        if (model != null) {
            model.close();
        }
        log.info("ML Service cleaned up");
    }
}
