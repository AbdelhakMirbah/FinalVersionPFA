#!/bin/bash

# URLs
API_URL="http://localhost:8088/api/v1/fraud/check"

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
YELLOW='\033[0;33m'
NC='\033[0m'

echo -e "${BLUE}=== Lancement de la Simulation de Transactions ===${NC}"

# Function to send request
send_request() {
    local amount=$1
    local type=$2
    local desc=$3
    
    echo -e "\n${YELLOW}Test: $desc${NC}"
    echo "Envoi : Montant=$amount | Type=$type "
    
    curl -s -X POST "$API_URL" \
         -H "Content-Type: application/json" \
         -d '{
               "amount": '"$amount"',
               "type": '"$type"',
               "oldBalance": 10000.0,
               "newBalance": 0.0,
               "oldBalanceDest": 0.0,
               "newBalanceDest": 0.0,
               "ip": "192.168.1.1",
               "email": "simulation@test.com"
             }' | jq .
}

# 1. Paiement Normal (Low Risk)
send_request 50 0 "Paiement Standard (Faible Montant)"

# 2. Gros Transfert (High Risk)
send_request 1000000 1 "Transfert Massif (Vidage de Compte)"

# 3. Cash Out Suspect
send_request 500000 2 "Retrait Cash Important"

# 4. Paiement Moyenne Gamme
send_request 5000 0 "Paiement Moyen"

# 5. Transfert Rapide
send_request 20000 1 "Transfert Rapide vers Inconnu"

echo -e "\n${GREEN}=== Simulation Terminée ===${NC}"
echo "Vérifiez le Dashboard : http://localhost:4201"
