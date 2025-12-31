#!/bin/bash

# Définition des couleurs
RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m' # No Color

echo -e "${RED}=== Arrêt du Système de Détection de Fraude ===${NC}"

# 1. Arrêt du Frontend
if [ -f frontend.pid ]; then
    PID=$(cat frontend.pid)
    echo -e "Arrêt du Frontend (PID: $PID)..."
    kill $PID 2>/dev/null
    rm frontend.pid
    echo -e "${GREEN}✅ Frontend arrêté.${NC}"
else
    echo "Frontend PID non trouvé (peut-être déjà arrêté)."
fi

# Tuer tout processus sur le port 4200 au cas où
lsof -ti:4201 | xargs kill -9 2>/dev/null

# 2. Arrêt du Backend
if [ -f backend.pid ]; then
    PID=$(cat backend.pid)
    echo -e "Arrêt du Backend (PID: $PID)..."
    kill $PID 2>/dev/null
    rm backend.pid
    echo -e "${GREEN}✅ Backend arrêté.${NC}"
else
    echo "Backend PID non trouvé (peut-être déjà arrêté)."
fi

# Tuer tout processus sur le port 8081 au cas où
lsof -ti:8088 | xargs kill -9 2>/dev/null

# 3. Arrêt des Conteneurs Docker
echo -e "\nArrêt de l'infrastructure Docker..."
docker-compose down
echo -e "${GREEN}✅ Infrastructure arrêtée.${NC}"

echo -e "\n${GREEN}=== SYSTÈME ARRÊTÉ AVEC SUCCÈS ===${NC}"
