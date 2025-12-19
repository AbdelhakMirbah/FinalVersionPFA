import requests
import psycopg2
import time
import json

# Configuration
API_URL = "http://localhost:8081/api/v1/fraud/check"
DB_CONFIG = {
    "dbname": "fraud_db",
    "user": "postgres",
    "password": "password",
    "host": "127.0.0.1",
    "port": "5433"  # Mapped port in docker-compose
}

def check_api():
    print("1. Sending Fraud Check Request via API...")
    payload = {
        "amount": 9000.0,
        "oldBalance": 10000.0,
        "newBalance": 1000.0,
        "ip": "192.168.1.1",
        "email": "test@example.com"
    }
    
    try:
        start_time = time.time()
        response = requests.post(API_URL, json=payload)
        duration = time.time() - start_time
        
        if response.status_code == 200:
            data = response.json()
            print(f"   ✅ API Success ({duration:.2f}s): {json.dumps(data)}")
            return True, data.get("score")
        else:
            print(f"   ❌ API Failed: {response.status_code} - {response.text}")
            return False, None
    except Exception as e:
        print(f"   ❌ API Connection Error: {e}")
        return False, None

def check_db(expected_amount):
    print("\n2. Verifying persistence in Database (waiting for Kafka processing)...")
    
    # Wait a bit for Kafka -> Consumer -> DB
    retries = 5
    conn = None
    
    try:
        conn = psycopg2.connect(**DB_CONFIG)
        cursor = conn.cursor()
        
        for i in range(retries):
            # Sort by ID desc to get latest
            cursor.execute("SELECT * FROM fraud_checks ORDER BY id DESC LIMIT 1")
            record = cursor.fetchone()
            
            if record:
                # Assuming schema: id, amount, score, risk, created_at
                # Check DB column order from entity if needed, but usually:
                # 1: id, 2: amount, 3: score, ...
                # Let's verify amount match
                db_amount = record[1] # amount is usually 2nd column if Id is first
                
                if abs(db_amount - expected_amount) < 0.01:
                    print(f"   ✅ Database Record Found: {record}")
                    return True
            
            print(f"   ⏳ Waiting for DB record... ({i+1}/{retries})")
            time.sleep(2)
            
        print("   ❌ Timeout: Record not found in DB after waiting.")
        return False
        
    except Exception as e:
        print(f"   ❌ Database Connection Error: {e}")
        return False
    finally:
        if conn:
            conn.close()

if __name__ == "__main__":
    print("=== System Verification ===")
    
    api_success, score = check_api()
    
    if api_success:
        db_success = check_db(9000.0)
        
        if db_success:
            print("\n✅✅ SYSTEM FUNCTIONAL: End-to-End Success! ✅✅")
        else:
            print("\n❌⚠️ SYSTEM PARTIAL: API worked but DB verification failed (Kafka/Consumer issue?)")
    else:
        print("\n❌ SYSTEM FAILED: API verification failed.")
