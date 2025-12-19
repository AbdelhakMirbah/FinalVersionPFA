# ONNX Model Training Guide for Fraud Detection

## Current Model Status ✅
- **Location**: `src/main/resources/fraud_model.onnx`
- **Size**: 392 KB
- **Status**: Working correctly
- **Input Features**: 6 features (type, amount, oldBalanceOrg, newBalanceOrig, oldBalanceDest, newBalanceDest)
- **Output**: Fraud probability score (0.0 to 1.0)

## Verified Functionality
The current model successfully:
- Loads on application startup
- Makes predictions with 6-feature input
- Returns probability scores
- Integrates with the full pipeline (API → ML → Kafka → DB)

---

## Training Your New Model

### Required Input Features (MUST MATCH)
Your model MUST accept exactly **6 features** in this order:

1. **type** (int): Transaction type
   - 0 = PAYMENT
   - 1 = TRANSFER
   - 2 = CASH_OUT
   - 3 = DEBIT
   - 4 = CASH_IN

2. **amount** (float): Transaction amount
3. **oldBalanceOrg** (float): Original balance before transaction
4. **newBalanceOrig** (float): New balance after transaction
5. **oldBalanceDest** (float): Destination account old balance
6. **newBalanceDest** (float): Destination account new balance

### Model Requirements

#### Input Shape
```python
input_shape = (1, 6)  # Batch size 1, 6 features
input_dtype = np.float32
```

#### Output Format
Your model should output **2 tensors** (RandomForest ONNX format):
- **Output 0**: Predicted class label (int64) - 0 or 1
- **Output 1**: Probabilities (float32) - [prob_class_0, prob_class_1]

The Java code extracts `probabilities[1]` (fraud probability).

---

## Python Training Script Template

```python
import numpy as np
import pandas as pd
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
from skl2onnx import convert_sklearn
from skl2onnx.common.data_types import FloatTensorType
import onnxruntime as ort

# 1. Load your dataset
# Example: df = pd.read_csv('fraud_data.csv')
# Required columns: type, amount, oldbalanceOrg, newbalanceOrig, oldbalanceDest, newbalanceDest, isFraud

# 2. Prepare features (MUST BE IN THIS ORDER)
feature_columns = [
    'type',           # 0
    'amount',         # 1
    'oldbalanceOrg',  # 2
    'newbalanceOrig', # 3
    'oldbalanceDest', # 4
    'newbalanceDest'  # 5
]

X = df[feature_columns].values.astype(np.float32)
y = df['isFraud'].values  # 0 or 1

# 3. Train model
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

model = RandomForestClassifier(
    n_estimators=100,
    max_depth=10,
    random_state=42,
    n_jobs=-1
)
model.fit(X_train, y_train)

# 4. Evaluate
accuracy = model.score(X_test, y_test)
print(f"Model Accuracy: {accuracy:.4f}")

# 5. Convert to ONNX
initial_type = [('float_input', FloatTensorType([None, 6]))]
onnx_model = convert_sklearn(
    model,
    initial_types=initial_type,
    target_opset=12
)

# 6. Save ONNX model
with open('fraud_model.onnx', 'wb') as f:
    f.write(onnx_model.SerializeToString())

print("✅ Model saved as fraud_model.onnx")

# 7. Test ONNX model
session = ort.InferenceSession('fraud_model.onnx')
test_input = np.array([[0, 9000.0, 10000.0, 1000.0, 0.0, 0.0]], dtype=np.float32)
outputs = session.run(None, {'float_input': test_input})
print(f"Test prediction - Label: {outputs[0]}, Probabilities: {outputs[1]}")
```

---

## Deployment Steps

### 1. Train Your Model
```bash
pip install scikit-learn skl2onnx onnxruntime pandas numpy
python train_fraud_model.py
```

### 2. Replace the ONNX File
```bash
# Backup current model
cp src/main/resources/fraud_model.onnx src/main/resources/fraud_model.onnx.backup

# Copy your new model
cp fraud_model.onnx src/main/resources/fraud_model.onnx
```

### 3. Rebuild Docker Image
```bash
docker-compose down
docker-compose up --build -d
```

### 4. Verify New Model
```bash
# Check logs for successful loading
docker-compose logs fraud-service | grep "ONNX model loaded"

# Test prediction
curl -X POST -H "Content-Type: application/json" \
  -d '{"amount": 9000.0, "oldBalance": 10000.0, "newBalance": 1000.0, "ip": "1.2.3.4", "email": "test@test.com"}' \
  http://localhost:8081/api/v1/fraud/check
```

---

## Important Notes

### ⚠️ Critical Requirements
1. **Feature Order**: MUST match the 6-feature order shown above
2. **Data Types**: Use `float32` for inputs
3. **ONNX Opset**: Use opset 12 or higher (compatible with DJL)
4. **Model Type**: RandomForestClassifier works best (tested)

### 🔧 If You Change Features
If you need different features, you MUST update:
1. `FraudRequest.java` - Add new fields
2. `FraudController.java` - Pass new features to `mlService.predict()`
3. `MlService.java` - Update feature array size and mapping

### 📊 Recommended Datasets
- **Kaggle**: "Credit Card Fraud Detection" or "Paysim Synthetic Financial Dataset"
- Ensure your dataset has the 6 required features or can be engineered to match

---

## Testing Checklist

After deploying a new model:
- [ ] Application starts without errors
- [ ] Model loads successfully (check logs)
- [ ] API returns predictions
- [ ] Scores are between 0.0 and 1.0
- [ ] High-risk transactions get score > 0.0001
- [ ] Kafka messages are sent
- [ ] Database records are created
- [ ] Run `python3 verify_system.py`

---

## Current Model Performance

Based on recent tests:
- Low amount (9000): Score = 0.0 → Risk: LOW ✅
- High amount (50000): Score = 0.00018 → Risk: HIGH ✅

The threshold is currently set to `0.0001` in `FraudController.java` (line 53).
You may want to adjust this based on your model's output distribution.
