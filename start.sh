#!/bin/bash

# Définition des couleurs
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== Démarrage du Système de Détection de Fraude ===${NC}"

# 1. Vérification des conteneurs Docker
echo -e "\n${BLUE}[1/3] Vérification de l'infrastructure Docker...${NC}"
if [ "$(docker ps -q -f name=postgres)" ]; then
    echo -e "${GREEN}✅ PostgreSQL est en ligne.${NC}"
else
    echo -e "${BLUE}🚀 Démarrage de Docker Compose...${NC}"
    docker-compose up -d
    echo -e "${BLUE}⏳ Attente de l'initialisation des services (10s)...${NC}"
    sleep 10
fi

# 2. Démarrage du Backend
echo -e "\n${BLUE}[2/3] Démarrage du Backend (Spring Boot)...${NC}"
# Ouvrir dans un nouvel onglet ou en arrière-plan selon l'OS (ici background)
./mvnw clean spring-boot:run > backend.log 2>&1 &
BACKEND_PID=$!
echo -e "${GREEN}✅ Backend lancé en arrière-plan (PID: $BACKEND_PID). Logs: backend.log${NC}"

# 3. Démarrage du Frontend
echo -e "\n${BLUE}[3/3] Démarrage du Frontend (Angular)...${NC}"
cd frontend
npm install > /dev/null 2>&1 # Installation silencieuse des dépendances si nécessaire
npm start > ../frontend.log 2>&1 &
FRONTEND_PID=$!
echo -e "${GREEN}✅ Frontend lancé en arrière-plan (PID: $FRONTEND_PID). Logs: frontend.log${NC}"

echo -e "\n${GREEN}=== SYSTÈME DÉMARRÉ ===${NC}"
echo -e "Backend API : http://localhost:8081"
echo -e "Frontend UI : http://localhost:4200"
echo -e "Admin DB    : http://localhost:8082"
echo -e "Kafka UI    : http://localhost:8090"
echo -e "\n${BLUE}Pour arrêter le système, utilisez : ./stop.sh${NC}"

# Sauvegarder les PIDs pour le script d'arrêt
echo "$BACKEND_PID" > ../backend.pid
echo "$FRONTEND_PID" > ../frontend.pid
