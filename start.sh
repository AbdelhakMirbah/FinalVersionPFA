#!/bin/bash

# Définition des couleurs
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== Démarrage du Système de Détection de Fraude ===${NC}"

# 1. Vérification et démarrage des conteneurs Docker
echo -e "\n${BLUE}[1/3] Vérification de l'infrastructure Docker...${NC}"
echo -e "${BLUE}🚀 Vérification/Démarrage de Docker Compose...${NC}"
docker-compose up -d postgres kafka zookeeper adminer kafka-ui

# Attendre que Kafka soit healthy (critique pour éviter les erreurs de connexion)
echo -e "${BLUE}⏳ Attente de la disponibilité de Kafka...${NC}"
for i in {1..30}; do
    KAFKA_STATUS=$(docker inspect kafka --format='{{.State.Health.Status}}' 2>/dev/null)
    if [ "$KAFKA_STATUS" = "healthy" ]; then
        echo -e "${GREEN}✅ Kafka est prêt!${NC}"
        break
    fi
    if [ $i -eq 30 ]; then
        echo -e "${RED}⚠️  Timeout: Kafka n'est pas devenu healthy après 60s${NC}"
        exit 1
    fi
    sleep 2
done

# Attendre que PostgreSQL soit healthy
echo -e "${BLUE}⏳ Vérification de PostgreSQL...${NC}"
for i in {1..15}; do
    POSTGRES_STATUS=$(docker inspect postgres --format='{{.State.Health.Status}}' 2>/dev/null)
    if [ "$POSTGRES_STATUS" = "healthy" ]; then
        echo -e "${GREEN}✅ PostgreSQL est prêt!${NC}"
        break
    fi
    sleep 1
done

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
npm start -- --port 4201 > ../frontend.log 2>&1 &
FRONTEND_PID=$!
echo -e "${GREEN}✅ Frontend lancé en arrière-plan (PID: $FRONTEND_PID). Logs: frontend.log${NC}"

echo -e "\n${GREEN}=== SYSTÈME DÉMARRÉ ===${NC}"
echo -e "Backend API : http://localhost:8088"
echo -e "Frontend UI : http://localhost:4201"
echo -e "Admin DB    : http://localhost:8082"
echo -e "Kafka UI    : http://localhost:8090"
echo -e "\n${BLUE}Pour arrêter le système, utilisez : ./stop.sh${NC}"

# Sauvegarder les PIDs pour le script d'arrêt
echo "$BACKEND_PID" > ../backend.pid
echo "$FRONTEND_PID" > ../frontend.pid
