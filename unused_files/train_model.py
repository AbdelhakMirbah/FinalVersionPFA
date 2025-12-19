"""
Complete ONNX Model Training Script for Fraud Detection
This script shows EXACTLY what you need to create a compatible ONNX model.
"""

import numpy as np
import pandas as pd
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
from skl2onnx import convert_sklearn
from skl2onnx.common.data_types import FloatTensorType
import onnxruntime as ort

print("=" * 60)
print("FRAUD DETECTION MODEL TRAINING")
print("=" * 60)

# ============================================================================
# STEP 1: PREPARE YOUR DATA
# ============================================================================
print("\n📊 STEP 1: Prepare Your Dataset")
print("-" * 60)

# You need a CSV file with these EXACT columns:
# - type: Transaction type (0-4)
# - amount: Transaction amount
# - oldbalanceOrg: Sender's balance before transaction
# - newbalanceOrig: Sender's balance after transaction
# - oldbalanceDest: Receiver's balance before transaction
# - newbalanceDest: Receiver's balance after transaction
# - isFraud: Label (0 = legitimate, 1 = fraud)

# Example: Load your dataset
# df = pd.read_csv('your_fraud_dataset.csv')

# For demonstration, let's create synthetic data:
print("Creating synthetic training data...")
np.random.seed(42)
n_samples = 10000

# Generate synthetic fraud data
data = {
    'type': np.random.randint(0, 5, n_samples),  # 0-4
    'amount': np.random.uniform(100, 100000, n_samples),
    'oldbalanceOrg': np.random.uniform(0, 200000, n_samples),
    'newbalanceOrig': np.random.uniform(0, 200000, n_samples),
    'oldbalanceDest': np.random.uniform(0, 200000, n_samples),
    'newbalanceDest': np.random.uniform(0, 200000, n_samples),
    'isFraud': np.random.randint(0, 2, n_samples)  # 0 or 1
}

df = pd.DataFrame(data)
print(f"✅ Dataset created: {len(df)} samples")
print(f"   Features: {list(df.columns)}")
print(f"   Fraud cases: {df['isFraud'].sum()} ({df['isFraud'].mean()*100:.1f}%)")

# ============================================================================
# STEP 2: PREPARE FEATURES (CRITICAL - MUST BE IN THIS ORDER!)
# ============================================================================
print("\n🔧 STEP 2: Prepare Features")
print("-" * 60)

# THIS ORDER IS MANDATORY - DO NOT CHANGE!
feature_columns = [
    'type',           # Feature 0
    'amount',         # Feature 1
    'oldbalanceOrg',  # Feature 2
    'newbalanceOrig', # Feature 3
    'oldbalanceDest', # Feature 4
    'newbalanceDest'  # Feature 5
]

X = df[feature_columns].values.astype(np.float32)  # MUST be float32
y = df['isFraud'].values

print(f"✅ Features prepared:")
print(f"   Shape: {X.shape}")
print(f"   Data type: {X.dtype}")
print(f"   Feature order: {feature_columns}")

# ============================================================================
# STEP 3: SPLIT DATA
# ============================================================================
print("\n✂️  STEP 3: Split Train/Test")
print("-" * 60)

X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.2, random_state=42, stratify=y
)

print(f"✅ Data split:")
print(f"   Training samples: {len(X_train)}")
print(f"   Test samples: {len(X_test)}")

# ============================================================================
# STEP 4: TRAIN MODEL
# ============================================================================
print("\n🤖 STEP 4: Train RandomForest Model")
print("-" * 60)

model = RandomForestClassifier(
    n_estimators=100,      # Number of trees
    max_depth=10,          # Maximum depth
    min_samples_split=10,  # Minimum samples to split
    min_samples_leaf=5,    # Minimum samples in leaf
    random_state=42,
    n_jobs=-1              # Use all CPU cores
)

print("Training model...")
model.fit(X_train, y_train)

# Evaluate
train_score = model.score(X_train, y_train)
test_score = model.score(X_test, y_test)

print(f"✅ Model trained:")
print(f"   Training accuracy: {train_score:.4f}")
print(f"   Test accuracy: {test_score:.4f}")

# ============================================================================
# STEP 5: CONVERT TO ONNX (THIS IS WHAT YOU NEED!)
# ============================================================================
print("\n🔄 STEP 5: Convert to ONNX Format")
print("-" * 60)

# Define input type - MUST be FloatTensorType with shape [None, 6]
initial_type = [('float_input', FloatTensorType([None, 6]))]

print("Converting to ONNX...")
onnx_model = convert_sklearn(
    model,
    initial_types=initial_type,
    target_opset=12  # ONNX opset version (12+ recommended)
)

print("✅ ONNX conversion successful!")

# ============================================================================
# STEP 6: SAVE ONNX FILE
# ============================================================================
print("\n💾 STEP 6: Save ONNX Model")
print("-" * 60)

output_path = 'fraud_model.onnx'
with open(output_path, 'wb') as f:
    f.write(onnx_model.SerializeToString())

import os
file_size = os.path.getsize(output_path) / 1024  # KB

print(f"✅ Model saved:")
print(f"   File: {output_path}")
print(f"   Size: {file_size:.1f} KB")

# ============================================================================
# STEP 7: VERIFY ONNX MODEL
# ============================================================================
print("\n✅ STEP 7: Verify ONNX Model")
print("-" * 60)

# Load ONNX model
session = ort.InferenceSession(output_path)

# Check input/output info
print("Model Input:")
for input_meta in session.get_inputs():
    print(f"   Name: {input_meta.name}")
    print(f"   Shape: {input_meta.shape}")
    print(f"   Type: {input_meta.type}")

print("\nModel Output:")
for output_meta in session.get_outputs():
    print(f"   Name: {output_meta.name}")
    print(f"   Shape: {output_meta.shape}")
    print(f"   Type: {output_meta.type}")

# Test prediction
print("\n🧪 Testing ONNX Model:")
test_cases = [
    {
        'name': 'Low-risk transaction',
        'input': np.array([[0, 1000.0, 5000.0, 4000.0, 0.0, 0.0]], dtype=np.float32)
    },
    {
        'name': 'High-risk transaction',
        'input': np.array([[0, 50000.0, 100000.0, 50000.0, 0.0, 0.0]], dtype=np.float32)
    }
]

for test in test_cases:
    outputs = session.run(None, {'float_input': test['input']})
    label = outputs[0][0]
    probabilities = outputs[1][0]
    fraud_prob = probabilities[1]
    
    print(f"\n   {test['name']}:")
    print(f"   Input: {test['input'][0]}")
    print(f"   Predicted label: {label}")
    print(f"   Probabilities: {probabilities}")
    print(f"   Fraud probability: {fraud_prob:.6f}")

# ============================================================================
# SUMMARY
# ============================================================================
print("\n" + "=" * 60)
print("✅ SUCCESS! Your ONNX model is ready!")
print("=" * 60)
print("\nNEXT STEPS:")
print("1. Copy 'fraud_model.onnx' to:")
print("   src/main/resources/fraud_model.onnx")
print("\n2. Rebuild Docker container:")
print("   docker-compose down")
print("   docker-compose up --build -d")
print("\n3. Test the API:")
print("   curl -X POST -H 'Content-Type: application/json' \\")
print("     -d '{\"amount\": 9000, \"oldBalance\": 10000, \"newBalance\": 1000, \"ip\": \"1.2.3.4\", \"email\": \"test@test.com\"}' \\")
print("     http://localhost:8081/api/v1/fraud/check")
print("=" * 60)
