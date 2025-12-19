# ONNX Model Requirements - Quick Reference

## What You Need to Create

### 1. Input Requirements ✅
```
- Input Name: "float_input" (or any name)
- Input Shape: [None, 6] or [1, 6]
  - None/1 = batch size
  - 6 = number of features (FIXED)
- Input Type: float32
```

### 2. The 6 Features (IN THIS EXACT ORDER!)
```
Feature 0: type           (transaction type: 0-4)
Feature 1: amount         (transaction amount)
Feature 2: oldbalanceOrg  (sender old balance)
Feature 3: newbalanceOrig (sender new balance)
Feature 4: oldbalanceDest (receiver old balance)
Feature 5: newbalanceDest (receiver new balance)
```

### 3. Output Requirements ✅
```
Your ONNX model should output 2 tensors:
- Output 0: label (int64) - predicted class (0 or 1)
- Output 1: probabilities (float32) - [prob_0, prob_1]

The Java code uses: probabilities[1] (fraud probability)
```

### 4. Model Type
```
✅ RandomForestClassifier (tested and working)
✅ Any scikit-learn classifier that outputs probabilities
⚠️  Deep learning models (TensorFlow/PyTorch) need different conversion
```

---

## Installation (One-time)

```bash
pip install scikit-learn skl2onnx onnxruntime pandas numpy
```

---

## Quick Start (3 Steps)

### Step 1: Prepare Your Data
You need a CSV with these columns:
```
type, amount, oldbalanceOrg, newbalanceOrig, oldbalanceDest, newbalanceDest, isFraud
```

Example datasets:
- Kaggle: "Paysim Synthetic Financial Dataset"
- Kaggle: "Credit Card Fraud Detection"

### Step 2: Run Training Script
```bash
python train_model.py
```

This will:
- Train a RandomForest model
- Convert it to ONNX format
- Save as `fraud_model.onnx`
- Test the model

### Step 3: Deploy to Your App
```bash
# Copy the ONNX file
cp fraud_model.onnx src/main/resources/fraud_model.onnx

# Rebuild and restart
docker-compose down
docker-compose up --build -d
```

---

## What the ONNX File Contains

The `.onnx` file is a binary file that contains:
1. **Model architecture** (tree structure for RandomForest)
2. **Trained weights** (decision thresholds, splits)
3. **Input/output metadata** (shapes, types, names)
4. **Computation graph** (how to process data)

You DON'T need to understand the internal format - just create it using the script!

---

## Testing Your ONNX Model

After creating `fraud_model.onnx`, test it with Python:

```python
import onnxruntime as ort
import numpy as np

# Load model
session = ort.InferenceSession('fraud_model.onnx')

# Test input (6 features)
test_input = np.array([[
    0,        # type: PAYMENT
    9000.0,   # amount
    10000.0,  # oldbalanceOrg
    1000.0,   # newbalanceOrig
    0.0,      # oldbalanceDest
    0.0       # newbalanceDest
]], dtype=np.float32)

# Run prediction
outputs = session.run(None, {'float_input': test_input})

print(f"Label: {outputs[0]}")           # [0] or [1]
print(f"Probabilities: {outputs[1]}")   # [[prob_0, prob_1]]
print(f"Fraud score: {outputs[1][0][1]}")  # This is what Java uses
```

---

## Common Issues & Solutions

### ❌ "Input shape mismatch"
**Problem**: Your model expects different number of features
**Solution**: Ensure your training data has exactly 6 features in the correct order

### ❌ "ONNX conversion failed"
**Problem**: Model type not supported
**Solution**: Use RandomForestClassifier or another scikit-learn classifier

### ❌ "Model not loading in Java"
**Problem**: ONNX opset version incompatible
**Solution**: Use `target_opset=12` or higher in conversion

### ❌ "Predictions are all the same"
**Problem**: Model not trained properly or data imbalanced
**Solution**: Check your training data, balance classes, tune hyperparameters

---

## Summary

**You need to create:**
1. A trained machine learning model (RandomForest recommended)
2. Convert it to ONNX format using `skl2onnx`
3. The ONNX file must accept 6 float32 inputs
4. The ONNX file must output probabilities

**You DON'T need to:**
- Understand ONNX internal format
- Write ONNX manually
- Modify Java code (unless changing features)

**Just run:** `python train_model.py` and you're done! 🚀
