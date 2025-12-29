# Système de Détection de Fraude en Temps Réel 🛡️

Ce projet est une solution complète (Full-Stack) pour analyser, détecter et visualiser les tentatives de fraude bancaire en temps réel.
Il utilise **Spring Boot 3 (Reactive/WebFlux)** pour le backend, **Angular 17** pour le frontend, **Apache Kafka** pour la messagerie asynchrone, et **ONNX/DJL** pour l'intelligence artificielle embarquée.

---

## 🚀 Démarrage Rapide (1-Click)

Le projet contient des scripts automatisés pour faciliter le démarrage.

### Prérequis
- Java 17+
- Node.js 18+ & NPM
- Docker & Docker Compose

### 1. Démarrer tout le système
```bash
./start.sh
```
Ce script va :
1. Lancer les conteneurs Docker (PostgreSQL, Kafka, Zookeeper, Adminer, Kafka UI).
2. Démarrer le Backend API (Port 8081).
3. Démarrer le Frontend Angular (Port 4200).

### 2. Arrêter le système
```bash
./stop.sh
```
Cela arrête proprement les application Java/Node et éteint les conteneurs Docker.

---

## 📊 Accès aux Interfaces

| Service | URL | Description |
|---------|-----|-------------|
| **Frontend** | [http://localhost:4200](http://localhost:4200) | Tableau de bord Admin (Live Feed, Graphiques, Historique) |
| **Backend API** | [http://localhost:8081](http://localhost:8081) | API REST Reactive |
| **Adminer** | [http://localhost:8082](http://localhost:8082) | Interface Web pour PostgreSQL |
| **Kafka UI** | [http://localhost:8090](http://localhost:8090) | Interface Web pour le cluster Kafka |

---

## 🧪 Tests & Simulation

Pour générer du trafic et voir le Dashboard s'animer :

```bash
./test.sh
```

Ce script simule plusieurs scénarios (Paiements normaux, Gros transferts, Cash-out suspect).

---

## 📑 Documentation

Pour une analyse détaillée de l'architecture, de la stack technique et des choix d'implémentation, consultez le [Rapport Complet (report.md)](report.md).

---

**Auteur :** Abdelhak Mirbah
**Date :** Décembre 2025
