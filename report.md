# Rapport de Projet: Système de Détection de Fraude en Temps Réel

## 1. Introduction
Ce projet vise à concevoir une architecture microservices réactive pour la détection de fraude bancaire en temps réel. Le système combine l'analyse de données asynchrone (Kafka), l'intelligence artificielle (ONNX/DJL), et une interface utilisateur moderne (Angular 17) pour offrir un tableau de bord de surveillance en direct.

---

## 2. Architecture Technique

### 2.1 Backend (Spring Boot 3 & WebFlux)
L'architecture backend repose sur une approche non-bloquante (Reactive Stack) pour garantir une haute performance et une faible latence.

- **API Reactive (Spring WebFlux):** Les endpoints REST sont entièrement non-bloquants, permettant de gérer un grand nombre de requêtes simultanées.
- **Messaging (Apache Kafka):** Toutes les transactions analysées sont publiées dans un topic Kafka (`fraud-checks`), découplant ainsi le traitement de l'analyse (Machine Learning) de la persistance (Base de données).
- **Service d'Enrichissement:** Simule l'appel à des APIs externes (Géolocalisation IP, Fuite d'Email) de manière asynchrone (`Mono.zip`) pour enrichir les données de transaction avant l'analyse.
- **Machine Learning (DJL & ONNX):** Intégration d'un modèle de Forêt Aléatoire (Random Forest) pré-entraîné exporté au format ONNX. Le moteur d'inférence DJL exécute les prédictions (Score de Fraude) directement dans la JVM.
- **Server-Sent Events (SSE):** Pour permettre la mise à jour en temps réel du Dashboard sans rechargement, un endpoint de streaming (`/api/v1/records/stream`) a été mis en place. Il diffuse instantanément chaque événement Kafka consommé vers le Frontend.

### 2.2 Frontend (Angular 17 & TailwindCSS)
La couche de présentation a été conçue pour maximiser l'expérience utilisateur et l'effet visuel ("Wow Effect").

- **Framework:** Angular 17 avec le nouveau système de "Standalone Components".
- **Design:** Utilisation de **TailwindCSS** pour une interface moderne, responsive, et en mode sombre (Dark Mode) professionnel.
- **Visualisation:** Intégration de **Ngx-Charts** pour afficher la distribution des risques (High/Low) via des graphiques dynamiques.
- **Réactivité:** Le service `FraudService` se connecte au flux SSE du backend. Dès qu'une transaction est traitée, elle apparaît instantanément sur l'interface, accompagnée d'animations CSS (pulse effects) pour signaler l'activité.
- **Simulateur Intégré:** Un panneau de simulation permet à l'utilisateur de tester le système en envoyant des transactions fictives directement depuis l'interface.
- **Tableau de Bord Administrateur:** Une nouvelle structure avec Sidebar de navigation, incluant :
    - **Page Dashboard:** Vue temps réel avec cartes de statistiques (KPIs) et flux en direct.
    - **Page Historique:** Tableau de données complet avec recherche, filtrage et **export CSV** pour les rapports d'audit.

---

## 3. Fonctionnalités Clés Implémentées

1.  **Détection Instantanée:** Analyse de fraude en < 50ms grâce au modèle ONNX embarqué.
2.  **Streaming Temps Réel:** Les analystes voient les alertes de fraude apparaître en direct (Live Feed) grâce à la technologie SSE.
3.  **Résilience:** En cas de panne du cluster Kafka, l'API REST continue de répondre aux clients (mécanisme de Fallback), garantissant la continuité de service.
4.  **Audit Complet:** Toutes les transactions sont persistées de manière asynchrone dans une base de données PostgreSQL pour analyse post-mortem.

---

## 4. Tests et Validation
- **Tests d'Intégration:** Des tests complets (End-to-End) ont été implémentés avec `EmbeddedKafka` et `H2 Database` pour valider le flux complet (API -> ML -> Kafka -> DB) sans infrastructure externe.
- **Monitoring:** L'interface affiche en temps réel le nombre de transactions scannées et la répartition des risques, offrant une vue d'ensemble immédiate de l'état du système.
