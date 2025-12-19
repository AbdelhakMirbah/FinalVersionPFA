"""
Fraud Detection Model Training - Customized for Your Dataset
Dataset: /Users/abdelhakmirbah/Desktop/MonProjetPFA/Fraud.csv
"""

import pandas as pd
import numpy as np
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
from sklearn.metrics import classification_report, confusion_matrix, roc_auc_score
from skl2onnx import convert_sklearn
from skl2onnx.common.data_types import FloatTensorType
import onnxruntime as ort

print("=" * 80)
print("FRAUD DETECTION MODEL TRAINING")
print("=" * 80)

# ============================================================================
# STEP 1: LOAD DATASET
# ============================================================================
print("\n📊 STEP 1: Loading Dataset")
print("-" * 80)

dataset_path = '/Users/abdelhakmirbah/Desktop/MonProjetPFA/Fraud.csv'
print(f"Loading: {dataset_path}")

df = pd.read_csv(dataset_path)
print(f"✅ Loaded {len(df):,} transactions")
print(f"   Fraud cases: {df['isFraud'].sum():,} ({df['isFraud'].mean()*100:.2f}%)")

# ============================================================================
# STEP 2: ENCODE TRANSACTION TYPE
# ============================================================================
print("\n🔧 STEP 2: Encoding Transaction Types")
print("-" * 80)

# Map transaction types to numbers (as expected by Java application)
type_mapping = {
    'PAYMENT': 0,
    'TRANSFER': 1,
    'CASH_OUT': 2,
    'DEBIT': 3,
    'CASH_IN': 4
}

df['type_encoded'] = df['type'].map(type_mapping)
print("Transaction type encoding:")
for ttype, code in type_mapping.items():
    count = (df['type'] == ttype).sum()
    print(f"   {ttype} → {code} ({count:,} transactions)")

# ============================================================================
# STEP 3: PREPARE FEATURES (EXACT ORDER REQUIRED BY JAVA APP)
# ============================================================================
print("\n🎯 STEP 3: Preparing Features")
print("-" * 80)

# CRITICAL: This order MUST match the Java application!
feature_columns = [
    'type_encoded',      # Feature 0: type (0-4)
    'amount',            # Feature 1: amount
    'oldbalanceOrg',     # Feature 2: oldbalanceOrg
    'newbalanceOrig',    # Feature 3: newbalanceOrig
    'oldbalanceDest',    # Feature 4: oldbalanceDest
    'newbalanceDest'     # Feature 5: newbalanceDest
]

X = df[feature_columns].values.astype(np.float32)
y = df['isFraud'].values

print(f"✅ Features prepared:")
print(f"   Shape: {X.shape}")
print(f"   Data type: {X.dtype}")
print(f"   Feature order: {feature_columns}")

# ============================================================================
# STEP 4: HANDLE CLASS IMBALANCE
# ============================================================================
print("\n⚖️  STEP 4: Handling Class Imbalance")
print("-" * 80)

# The dataset is highly imbalanced (99.87% legitimate, 0.13% fraud)
# We'll use stratified sampling to keep the ratio in train/test

print(f"Original class distribution:")
print(f"   Legitimate: {(y == 0).sum():,} ({(y == 0).mean()*100:.2f}%)")
print(f"   Fraud: {(y == 1).sum():,} ({(y == 1).mean()*100:.2f}%)")

# Option 1: Use all data (recommended for large datasets)
# Option 2: Downsample majority class (if training is too slow)
# Option 3: Use class_weight='balanced' in RandomForest (we'll use this)

# For faster training, let's sample the data (optional)
# Uncomment the next 3 lines if you want to use a subset for faster training:
# sample_size = 100000
# df_sample = df.sample(n=min(sample_size, len(df)), random_state=42)
# X, y = df_sample[feature_columns].values.astype(np.float32), df_sample['isFraud'].values

# ============================================================================
# STEP 5: SPLIT DATA
# ============================================================================
print("\n✂️  STEP 5: Splitting Train/Test Data")
print("-" * 80)

X_train, X_test, y_train, y_test = train_test_split(
    X, y, 
    test_size=0.2, 
    random_state=42, 
    stratify=y  # Keep same fraud ratio in train and test
)

print(f"✅ Data split:")
print(f"   Training samples: {len(X_train):,}")
print(f"   Test samples: {len(X_test):,}")
print(f"   Training fraud cases: {y_train.sum():,}")
print(f"   Test fraud cases: {y_test.sum():,}")

# ============================================================================
# STEP 6: TRAIN MODEL
# ============================================================================
print("\n🤖 STEP 6: Training RandomForest Model")
print("-" * 80)

model = RandomForestClassifier(
    n_estimators=100,           # Number of trees
    max_depth=15,               # Maximum depth (increased for complex patterns)
    min_samples_split=20,       # Minimum samples to split
    min_samples_leaf=10,        # Minimum samples in leaf
    class_weight='balanced',    # Handle imbalanced classes
    random_state=42,
    n_jobs=-1,                  # Use all CPU cores
    verbose=1                   # Show progress
)

print("Training model (this may take a few minutes)...")
model.fit(X_train, y_train)

# ============================================================================
# STEP 7: EVALUATE MODEL
# ============================================================================
print("\n📈 STEP 7: Evaluating Model")
print("-" * 80)

# Predictions
y_pred = model.predict(X_test)
y_pred_proba = model.predict_proba(X_test)[:, 1]

# Metrics
train_score = model.score(X_train, y_train)
test_score = model.score(X_test, y_test)
roc_auc = roc_auc_score(y_test, y_pred_proba)

print(f"✅ Model Performance:")
print(f"   Training accuracy: {train_score:.4f}")
print(f"   Test accuracy: {test_score:.4f}")
print(f"   ROC-AUC score: {roc_auc:.4f}")

print("\nConfusion Matrix:")
cm = confusion_matrix(y_test, y_pred)
print(cm)
print(f"   True Negatives: {cm[0,0]:,}")
print(f"   False Positives: {cm[0,1]:,}")
print(f"   False Negatives: {cm[1,0]:,}")
print(f"   True Positives: {cm[1,1]:,}")

print("\nClassification Report:")
print(classification_report(y_test, y_pred, target_names=['Legitimate', 'Fraud']))

# Feature importance
print("\n🔍 Feature Importance:")
for feat, importance in zip(feature_columns, model.feature_importances_):
    print(f"   {feat}: {importance:.4f}")

# ============================================================================
# STEP 8: CONVERT TO ONNX
# ============================================================================
print("\n🔄 STEP 8: Converting to ONNX Format")
print("-" * 80)

initial_type = [('float_input', FloatTensorType([None, 6]))]

print("Converting to ONNX...")
onnx_model = convert_sklearn(
    model,
    initial_types=initial_type,
    target_opset=12
)

print("✅ ONNX conversion successful!")

# ============================================================================
# STEP 9: SAVE ONNX MODEL
# ============================================================================
print("\n💾 STEP 9: Saving ONNX Model")
print("-" * 80)

output_path = 'fraud_model_trained.onnx'
with open(output_path, 'wb') as f:
    f.write(onnx_model.SerializeToString())

import os
file_size = os.path.getsize(output_path) / 1024

print(f"✅ Model saved:")
print(f"   File: {output_path}")
print(f"   Size: {file_size:.1f} KB")

# ============================================================================
# STEP 10: VERIFY ONNX MODEL
# ============================================================================
print("\n✅ STEP 10: Verifying ONNX Model")
print("-" * 80)

session = ort.InferenceSession(output_path)

# Test with real fraud cases from test set
fraud_indices = np.where(y_test == 1)[0][:3]  # Get 3 fraud cases
legit_indices = np.where(y_test == 0)[0][:3]  # Get 3 legitimate cases

print("\n🧪 Testing with Real Data:")
print("\nFraud Cases:")
for idx in fraud_indices:
    test_input = X_test[idx:idx+1]
    outputs = session.run(None, {'float_input': test_input})
    fraud_prob = outputs[1][0][1]
    print(f"   Input: {test_input[0]}")
    print(f"   Fraud probability: {fraud_prob:.6f} (Actual: FRAUD)")

print("\nLegitimate Cases:")
for idx in legit_indices:
    test_input = X_test[idx:idx+1]
    outputs = session.run(None, {'float_input': test_input})
    fraud_prob = outputs[1][0][1]
    print(f"   Input: {test_input[0]}")
    print(f"   Fraud probability: {fraud_prob:.6f} (Actual: LEGITIMATE)")

# ============================================================================
# SUMMARY
# ============================================================================
print("\n" + "=" * 80)
print("✅ SUCCESS! Your trained ONNX model is ready!")
print("=" * 80)
print("\n📋 NEXT STEPS:")
print("\n1. Copy the trained model to your project:")
print("   cp fraud_model_trained.onnx src/main/resources/fraud_model.onnx")
print("\n2. Rebuild Docker container:")
print("   cd /Users/abdelhakmirbah/Desktop/EMSI/s9/PFAProject/MonProjetPFA")
print("   docker-compose down")
print("   docker-compose up --build -d")
print("\n3. Test the API:")
print("   curl -X POST -H 'Content-Type: application/json' \\")
print("     -d '{\"amount\": 181.0, \"oldBalance\": 181.0, \"newBalance\": 0.0, \"ip\": \"1.2.3.4\", \"email\": \"test@test.com\"}' \\")
print("     http://localhost:8081/api/v1/fraud/check")
print("\n4. Monitor logs:")
print("   docker-compose logs -f fraud-service")
print("=" * 80)
