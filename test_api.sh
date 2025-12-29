#!/bin/bash

echo "---------------------------------------------------"
echo "TEST 1: Standard Payment (Low Amount)"
echo "Scenario: User pays 50.0. Balance goes 500 -> 450."
echo "Expected: LOW RISK (Type 0 is Payment)"
curl -s -X POST http://localhost:8081/api/v1/fraud/check \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 50.0,
    "oldBalance": 500.0,
    "newBalance": 450.0,
    "type": 0,
    "oldBalanceDest": 0.0,
    "newBalanceDest": 0.0,
    "ip": "127.0.0.1",
    "email": "user1@test.com"
  }'
echo -e "\n---------------------------------------------------\n"

echo "TEST 2: Massive Transfer (Account Emptying)"
echo "Scenario: User transfers 1,000,000. Balance goes 1,000,000 -> 0."
echo "Expected: HIGH RISK (Type 1 is Transfer)"
curl -s -X POST http://localhost:8081/api/v1/fraud/check \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 1000000.0,
    "oldBalance": 1000000.0,
    "newBalance": 0.0,
    "type": 1,
    "oldBalanceDest": 0.0,
    "newBalanceDest": 0.0,
    "ip": "1.2.3.4",
    "email": "hacker@test.com"
  }'
echo -e "\n---------------------------------------------------\n"

echo "TEST 3: Cash Out (Account Emptying)"
echo "Scenario: User cashes out 500,000. Balance goes 500,000 -> 0."
echo "Expected: HIGH RISK (Type 2 is Cash Out)"
curl -s -X POST http://localhost:8081/api/v1/fraud/check \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 500000.0,
    "oldBalance": 500000.0,
    "newBalance": 0.0,
    "type": 2,
    "oldBalanceDest": 0.0,
    "newBalanceDest": 500000.0,
    "ip": "5.6.7.8",
    "email": "thief@test.com"
  }'
echo -e "\n---------------------------------------------------\n"

echo "TEST 4: Large Payment (Rich User)"
echo "Scenario: User pays 500,000. Balance goes 1,000,000 -> 500,000."
echo "Expected: LOW RISK (Payments are usually safe in this dataset)"
curl -s -X POST http://localhost:8081/api/v1/fraud/check \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 500000.0,
    "oldBalance": 1000000.0,
    "newBalance": 500000.0,
    "type": 0,
    "oldBalanceDest": 0.0,
    "newBalanceDest": 0.0,
    "ip": "127.0.0.1",
    "email": "richuser@test.com"
  }'
echo -e "\n---------------------------------------------------\n"

echo "TEST 5: Suspicious Behavior (Stealing money but balance hides it)"
echo "Scenario: Transfer 200,000 but 'newBalance' claims to be unchanged (Anomalous)."
echo "Expected: Unpredictable/Suspicious"
curl -s -X POST http://localhost:8081/api/v1/fraud/check \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 200000.0,
    "oldBalance": 200000.0,
    "newBalance": 200000.0,
    "type": 1,
    "oldBalanceDest": 0.0,
    "newBalanceDest": 0.0,
    "ip": "1.2.3.4",
    "email": "bot@test.com"
  }'
echo -e "\n---------------------------------------------------"
