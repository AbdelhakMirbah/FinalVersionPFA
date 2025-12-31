import urllib.request
import json
import random
import time

API_URL = "http://localhost:8088/api/v1/fraud/check"

def send_transaction(data):
    req = urllib.request.Request(API_URL)
    req.add_header('Content-Type', 'application/json')
    jsondata = json.dumps(data).encode('utf-8')
    try:
        response = urllib.request.urlopen(req, jsondata)
        # print(".", end="", flush=True) # Minimal output
    except Exception as e:
        print(f"Error: {e}")

def generate_low_risk():
    amount = round(random.uniform(10.0, 500.0), 2)
    old_balance = round(amount + random.uniform(100.0, 2000.0), 2)
    new_balance = round(old_balance - amount, 2)
    
    return {
        "amount": amount,
        "type": 0, # PAYMENT
        "oldBalance": old_balance,
        "newBalance": new_balance,
        "oldBalanceDest": 0.0,
        "newBalanceDest": 0.0,
        "ip": f"192.168.1.{random.randint(2, 254)}",
        "email": f"user{random.randint(1000, 9999)}@gmail.com"
    }

def generate_high_risk():
    amount = round(random.uniform(200000.0, 1000000.0), 2)
    # High risk pattern: Emptying the account via Transfer
    return {
        "amount": amount,
        "type": 1, # TRANSFER
        "oldBalance": amount,
        "newBalance": 0.0,
        "oldBalanceDest": 0.0,
        "newBalanceDest": amount,
        "ip": f"10.0.0.{random.randint(2, 254)}",
        "email": f"hacker{random.randint(100, 999)}@darkweb.net"
    }

print("=== Starting Load Test (100 Transactions) ===")
print("Generating 80 Low Risk and 20 High Risk transactions...")

transactions = []

# Add 80 Low Risk
for _ in range(80):
    transactions.append(generate_low_risk())

# Add 20 High Risk
for _ in range(20):
    transactions.append(generate_high_risk())

# Shuffle appropriately to simulate real traffic 
random.shuffle(transactions)

count = 0
for t in transactions:
    send_transaction(t)
    count += 1
    if count % 10 == 0:
        print(f"Sent {count}/100 transactions...")
    time.sleep(0.05) # Small delay to be nice

print("\n=== Test Completed ===")
print("Check your dashboard at http://localhost:4201")
