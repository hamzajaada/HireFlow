# Hire-Flow

Application de suivi du recrutement d'une PME, en microservices Spring Boot.
Un candidat dépose une candidature sur une offre, elle passe en entretien, puis elle est retenue ou refusée.
Une candidature retenue clôture l'offre et déclenche l'onboarding du candidat.

Les services communiquent en **synchrone** (OpenFeign) quand ils ont besoin d'une réponse, et en **asynchrone** (Kafka) pour prévenir les autres qu'un événement a eu lieu.

## Stack

- Java 17+, Maven (wrapper fourni : `./mvnw`)
- Spring Boot 3.5, Spring Cloud 2025.0 (Config Server, Eureka, Gateway, OpenFeign, Resilience4j)
- Spring Data JPA + PostgreSQL (une base par service)
- Apache Kafka (mode KRaft, sans Zookeeper)
- Docker Compose pour l'infrastructure
- Angular (bonus, semaine 4)

## Structure du dépôt

```
hire-flow/
├── pom.xml                  POM parent (versions, liste des modules)
├── common-events/           DTO des événements Kafka partagés + noms des topics
├── config-server/           Spring Cloud Config Server (port 8888)          membre 1
├── config-repo/             Fichiers de configuration servis par le Config Server
├── discovery-server/        Annuaire Eureka (port 8761)                     membre 2
├── api-gateway/             API Gateway (port 8080)                         membre 3
├── service-offres/          JobOffer, RequiredSkill (port 8081, db_offres)  membre 1
├── service-talents/         Candidate, ScreeningTest (port 8083, db_talents) membre 2
├── service-candidatures/    Service principal (port 8082, db_candidatures)
│   ├── .../application/     Application + cycle de vie                      membre 3
│   └── .../interview/       Interview + notation                            membre 4
├── docker-compose.yml       PostgreSQL + Kafka + création des topics + Kafka UI   membre 4
└── docker/postgres-init.sql Création des 3 bases
```

Chaque service suit la même organisation de packages :
`entity` (entités JPA), `repository`, puis à créer par chacun : `dto`, `service`, `controller`, `client` (Feign), `kafka` (producteurs / consommateurs).

## Ce qui est déjà fait dans le squelette

- Les 6 entités du MCD avec leurs repositories, les enums `OfferStatus` (OUVERTE, POURVUE) et `ApplicationStatus` (DEPOSEE, EN_ENTRETIEN, RETENUE, REFUSEE).
- Les 3 événements Kafka (`ApplicationSubmittedEvent`, `InterviewCompletedEvent`, `ApplicationAcceptedEvent`) et les noms de topics dans `common-events`.
- Config Server en mode `native` qui lit `config-repo/`, Eureka, Gateway avec les routes vers les 3 services.
- La configuration JPA, PostgreSQL, Kafka (sérialisation JSON) et Eureka de chaque service dans `config-repo/`.

Reste à faire par chaque membre : les services métier, les contrôleurs REST, les clients Feign, les producteurs et consommateurs Kafka, les tests. Voir le dossier projet pour la répartition.

## Démarrer en local

1. Lancer l'infrastructure :
   ```bash
   docker compose up -d
   ```
   PostgreSQL sur `localhost:5432` (user `hireflow` / mot de passe `hireflow`), Kafka sur `localhost:9092`, Kafka UI sur http://localhost:8090.
2. Compiler tout le projet depuis la racine :
   ```bash
   ./mvnw clean install
   ```
3. Lancer les applications **dans cet ordre**, depuis la racine du dépôt (le Config Server lit `./config-repo`) :
   1. `config-server`
   2. `discovery-server`
   3. `service-offres`, `service-talents`, `service-candidatures`
   4. `api-gateway`

   Par exemple : `./mvnw -pl config-server spring-boot:run`, ou le bouton Run de l'IDE (répertoire de travail = racine du projet).
4. Vérifier dans Eureka (http://localhost:8761) que les services sont enregistrés, puis passer par la Gateway : http://localhost:8080/offers, `/applications`, `/candidates`.

## Contrats partagés (à ne pas changer sans prévenir l'équipe)

| Type | Nom | Émetteur → consommateur | Contenu |
|------|-----|-------------------------|---------|
| Feign ① | `GET /offers/{id}/eligibility?email=…` | candidatures → offres | statut de l'offre, compétences couvertes |
| Feign ② | `GET /candidates/by-email/{email}/profile` | candidatures → talents | experienceAnnees, statut, dernier score |
| Kafka | `application-submitted` | candidatures → talents | applicationId, offerId, candidatEmail, cvUrl, date |
| Kafka | `interview-completed` | candidatures → offres, talents | applicationId, offerId, candidatEmail, note, avis |
| Kafka | `application-accepted` | candidatures → offres, talents | applicationId, offerId, candidatEmail, date |

## Workflow Git

La branche `main` contient toujours un projet qui compile et démarre. **Personne ne pousse directement sur `main`.**

1. Récupérer la dernière version de `main` :
   ```bash
   git checkout main
   git pull origin main
   ```
2. Créer une branche par fonctionnalité, préfixée par le périmètre :
   ```bash
   git checkout -b feature/offres-crud
   ```
   Exemples : `feature/offres-eligibility`, `feature/talents-kafka-consumer`, `feature/candidatures-application-feign`, `feature/candidatures-interview`, `feature/infra-gateway`.
3. Committer souvent avec des messages clairs (`offres: ajoute le CRUD des compétences`), puis pousser la branche :
   ```bash
   git push -u origin feature/offres-crud
   ```
4. Ouvrir une **pull request vers `main`** sur GitHub, remplir le modèle de PR, et demander la relecture d'**un autre membre**.
5. Avant la fusion : `./mvnw clean verify` passe et la branche est à jour avec `main` (`git pull origin main` puis résoudre les conflits).
6. Après la fusion, supprimer la branche et repartir de `main` à jour.

Les membres 3 et 4 travaillent dans le même module `service-candidatures` : chacun reste dans son package (`application` ou `interview`) pour éviter les conflits.

Une tâche est terminée quand elle a des tests, tourne avec docker-compose et passe par la Gateway.

## Répartition

| | Membre 1 : offres | Membre 2 : talents | Membre 3 : candidatures · Application | Membre 4 : candidatures · Interview |
|---|---|---|---|---|
| Infra | Config Server | Eureka | API Gateway | docker-compose |
| Feign | expose l'éligibilité | expose le profil | client ① + fallback | client ② + fallback |
| Kafka | consomme interview-completed, application-accepted | consomme les 3 topics | produit application-submitted, application-accepted | produit interview-completed |
