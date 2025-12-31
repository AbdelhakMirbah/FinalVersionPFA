# Rapport de Projet Complet : Système Intelligent de Détection de Fraude Bancaire en Temps Réel

**Auteur :** Abdelhak Mirbah  
**Date :** 31 Décembre 2025  
**Version :** 1.0.0  

---

## 📑 Table des Matières
1. [Introduction & Résumé Exécutif](#1-introduction--résumé-exécutif)
2. [Analyse des Besoins & Objectifs](#2-analyse-des-besoins--objectifs)
3. [Architecture Technique Globale](#3-architecture-technique-globale)
4. [Choix Technologiques (Stack)](#4-choix-technologiques-stack)
5. [Implémentation Backend (Core)](#5-implémentation-backend-core)
6. [Implémentation Frontend (UI/UX)](#6-implémentation-frontend-uiux)
7. [Circuit de la Donnée (Workflow)](#7-circuit-de-la-donnée-workflow)
8. [Stratégie de Tests & Validation](#8-stratégie-de-tests--validation)
9. [Conclusion & Perspectives](#9-conclusion--perspectives)

---

## 1. Introduction & Résumé Exécutif
La fraude bancaire représente un défi majeur pour les institutions financières, nécessitant des réponses immédiates (quasi temps réel) pour bloquer les transactions suspectes avant qu'elles ne soient finalisées. 

Ce projet propose une solution **Full-Stack** robuste et scalable capable d'analyser, de détecter et de visualiser les tentatives de fraude en temps réel. En combinant la puissance de l'Intelligence Artificielle (**Random Forest via ONNX**) avec une architecture événementielle (**Apache Kafka**) et une interface réactive moderne (**Angular 17**), nous avons créé un système capable de traiter des milliers de transactions par seconde avec une latence minimale.

---

## 2. Analyse des Besoins & Objectifs

### 🎯 Objectifs Principaux
*   **Rapidité :** Analyser chaque transaction en moins de 100ms.
*   **Scalabilité :** Gérer des pics de charge sans bloquer le système (Non-blocking I/O).
*   **Visibilité :** Offrir aux analystes une vue instantanée de l'état du système.
*   **Indépendance :** Le système doit fonctionner même si la base de données ou le bus de messages subit des ralentissements temporaires.

### 🛡️ Périmètre Fonctionnel
*   Réception de transactions via API REST sécurisée.
*   Enrichissement des données (Simulation d'appel API externe pour IP/Email).
*   Scoring de fraude via modèle Machine Learning pré-entraîné.
*   Diffusion d'alertes en temps réel aux tableaux de bord connectés.
*   Archivage des transactions pour audit et ré-entraînement futur des modèles.

---

## 3. Architecture Technique Globale

L'architecture suit le modèle **Microservices Reactive** (Architecture Hexagonale simplifiée) :

```
[ Client / TPE ]  -->  [ API Gateway / Controller ]  -->  [ Services (Async) ]
                                                                 |
            [ Dashboard Admin ] <== (SSE Stream) <== [ Kafka Consumer ] <== [ Apache Kafka ]
                  ^                                              |
                  |                                      [ PostgreSQL DB ]
            [ Frontend Angular ]
```

### Points Clés de l'Architecture
1.  **Event-Driven :** Le cœur du système est asynchrone. L'API ne fait pas attendre le client pendant l'écriture en base de données.
2.  **Reactive Programming :** Utilisation de **Project Reactor (Flux/Mono)** pour ne jamais bloquer les threads du serveur.
3.  **Broadcasting :** Le pattern **Server-Sent Events (SSE)** est utilisé pour "pousser" la donnée vers le frontend, évitant le polling coûteux.
4.  **Intelligence Embarquée :** Le modèle ML tourne *in-process* avec l'API Java, évitant la latence réseau d'un appel API externe vers Python.

---

## 4. Choix Technologiques (Stack)

### 🛠️ Backend
*   **Langage :** Java 17+ (LTS)
*   **Framework :** Spring Boot 3 & Spring WebFlux
*   **Messaging :** Apache Kafka (Zookeeper-less ou standard)
*   **Database :** PostgreSQL 15
*   **AI Engine :** DJL (Deep Java Library) & ONNX Runtime
*   **Build Tool :** Maven
*   **Port :** 8088 (Configurable)

### 💻 Frontend
*   **Framework :** Angular 17 (Standalone Components)
*   **Styling :** TailwindCSS (Utility-first CSS)
*   **Charts :** Ngx-Charts (D3.js wrapper for Angular)
*   **Communication :** RxJS & EventSource (Native SSE)
*   **Port :** 4201 (Configurable)

### 🐳 Infrastructure
*   **Containerization :** Docker & Docker Compose
*   **Monitoring :** Kafka UI & Adminer (DB SQL Client)

---

## 5. Implémentation Backend (Core)

### Le Cœur de la Détection (MlService)
Le service `MlService` charge un modèle ONNX (format portable pour l'IA). À chaque transaction, il convertit les 6 caractéristiques clés (Montant, Type, Soldes Origine/Destinataire) en un tenseur, l'envoie au modèle, et récupère une probabilité de fraude comprise entre 0 et 1.

### La Gestion des Flux (FraudStreamService)
Pour éviter de surcharger le réseau, nous utilisons un `Sinks.Many<FraudCheck>` de Reactor. C'est un canal "Hot" qui diffuse les messages entrants à tous les abonnés Web connectés simultanément. Si personne n'écoute, les messages sont ignorés (Backpressure).

### L'Intégration Kafka
Le `FraudController` agit comme un *Producer* : il dépose un message "Fire-and-Forget" dans le topic `fraud-checks` dès que l'analyse est terminée.
L'`AuditConsumer` agit comme un *Consumer* : il lit ce topic, sauvegarde en base, et notifie le `FraudStreamService`. Cela garantit que le Dashboard montre ce qui a été *réellement* traité et persisté.

---

## 6. Implémentation Frontend (UI/UX)

L'interface a été entièrement refondue pour offrir une expérience professionnelle de type "Admin Dashboard".

### 📊 Dashboard (Live Monitor)
*   **Stat Cards :** Affichage immédiat des KPIs (Total scanné, Risques détectés, Statut Serveur).
*   **Donut Chart Temps Réel :** Répartition visuelle des risques, mise à jour à chaque événement entrant.
*   **Feed de Transactions :** Liste déroulante animée montrant les dernières transactions avec codes couleurs (Rouge/Vert) et icônes contextuelles.
*   **Simulateur :** Formulaire intégré permettant de tester le système sans outils externes.
*   **Détails Transaction :** Modal interactif affichant l'intégralité des données (IP, Email, Balances, etc.) via le bouton "View Details".

### 📜 Historique & Reporting
*   **Tableau de Données :** Vue tabulaire complète des transactions passées.
*   **Recherche Dynamique :** Filtrage instantané par ID ou Montant.
*   **Export CSV :** Fonctionnalité native permettant aux auditeurs de télécharger les données pour analyse dans Excel.

### 🎨 Design System
Utilisation avancée de **TailwindCSS** avec un thème sombre (Dark Mode) pour réduire la fatigue visuelle des opérateurs travaillant en centre de surveillance.

---

## 7. Circuit de la Donnée (Workflow)

1.  **Entrée :** Une requête `POST /api/v1/fraud/check` arrive avec les détails de la transaction.
2.  **Enrichissement :** L'API enrichit les données (IP Score, Email Reputation) via `EnrichmentService`.
3.  **Inférence :** `MlService` calcule le score de fraude (ex: 0.94).
4.  **Décision :** Si Score > Seuil (ex: 0.8), Statut = HIGH RISK.
5.  **Broadcast Kafka :** L'événement est publié dans Kafka. L'API répond immédiatement au client HTTP (200 OK) avec le score.
6.  **Persistance & Notification :** 
    *   Le Consumer Kafka lit le message.
    *   Il sauvegarde dans PostgreSQL.
    *   Il "pousse" l'événement dans le canal SSE.
7.  **Visualisation :** Le navigateur reçoit l'événement SSE et met à jour le graphique et la liste en temps réel.

---

## 8. Stratégie de Tests & Validation

### ✅ Tests Unitaires & Intégration
*   Utilisation de **EmbeddedKafka** pour tester la messagerie sans lancer de Docker.
*   Utilisation de **H2 Database** (in-memory) pour valider la couche JPA.
*   Validation complète du contrôleur WebFlux avec `WebTestClient`.

### 🧪 Validation Manuelle
*   Script `test_api.sh` pour générer du trafic de masse.
*   Vérification des formats de date (Fix : `dd/MM/yyyy HH:mm:ss`) et de la cohérence des données affichées.

---

## 9. Conclusion & Perspectives

Ce projet démontre avec succès comment moderniser un système critique bancaire. L'architecture découplée garantit robustesse et évolutivité.

### Perspectives d'Évolution 🚀
*   **Sécurité :** Ajouter une couche d'authentification OAuth2 / JWT pour sécuriser l'accès au Dashboard.
*   **Big Data :** Connecter un cluster Hadoop/Spark au topic Kafka pour l'analyse de tendances sur le long terme.
*   **IA Avancée :** Mettre en place un pipeline de ré-entraînement automatique du modèle ONNX basé sur les faux positifs signalés dans le Dashboard.

