# Hire-Flow

Application de suivi du recrutement d'une PME, en microservices Spring Boot.
Un candidat dépose une candidature sur une offre, elle passe en entretien, puis elle est retenue ou refusée.
Une candidature retenue clôture l'offre et déclenche l'onboarding du candidat.

Les services communiquent en **synchrone** avec **OpenFeign** (+ Resilience4j) quand ils ont besoin d'une réponse immédiate, et en **asynchrone** avec **RabbitMQ** pour prévenir les autres services qu'un événement a eu lieu (sans attendre de réponse).

L'organisation du dépôt reprend celle du projet de référence du cours, [tp-ms-ecom](https://github.com/salimk/tp-ms-ecom) (M. KHALIL Salim).

## Stack

- Java 17+, Maven (wrapper fourni : `./mvnw`)
- Spring Boot 3.5, Spring Cloud 2025.0 (Config Server, Eureka, Gateway, OpenFeign, Resilience4j)
- Spring Data JPA + **H2 en mémoire** (une base par service, données de démo dans `data.sql`)
- **RabbitMQ** (Spring AMQP) pour les événements
- MapStruct + Lombok
- Docker Compose pour RabbitMQ uniquement
- Angular (bonus, semaine 4)

## Structure du dépôt

```
Hire-Flow/
├── pom.xml                  POM agrégateur (chaque module est un projet Spring Boot autonome)
├── config-repo/             Configuration de chaque service, servie par le Config Server
│   ├── gateway.properties
│   ├── service-offres.properties
│   ├── service-candidatures.properties
│   └── service-talents.properties
├── config-server/           Spring Cloud Config Server (port 8888)          membre 1
├── service-registry/        Annuaire Eureka (port 8761)                     membre 2
├── gateway/                 API Gateway (port 8080)                         membre 3
├── service-offres/          JobOffer, RequiredSkill (port 8081, H2 offresdb)       membre 1
├── service-talents/         Candidate, ScreeningTest (port 8083, H2 talentsdb)     membre 2
├── service-candidatures/    Service principal (port 8082, H2 candidaturesdb)
│   ├── .../application/     Application + cycle de vie                      membre 3
│   └── .../interview/       Interview + notation                            membre 4
└── docker-compose.yml       RabbitMQ + console d'administration             membre 4
```

Chaque service suit l'organisation de packages du TP :
`entite` (entités JPA), `enums`, `repository`, `dto`, `config` (RabbitConfig), puis à créer par chacun : `mapper` (MapStruct), `service`, `controller`, `exception`, `feignClient` (clients Feign + `ResilientRequests`), `messaging` (publisher / consumer RabbitMQ).

Chaque service n'a dans `src/main/resources/application.properties` que son nom et l'adresse du Config Server ; tout le reste (port, H2, RabbitMQ, Eureka, Resilience4j, routes) est dans `config-repo/`.

## Ce qui est déjà fait dans le squelette

- Les 6 entités du MCD avec leurs repositories, les enums `OfferStatus` (OUVERTE, POURVUE) et `ApplicationStatus` (DEPOSEE, EN_ENTRETIEN, RETENUE, REFUSEE).
- Une base H2 par service, recréée à chaque démarrage et remplie par `src/main/resources/data.sql` (3 offres, 3 candidats, 3 candidatures, 1 entretien). Console H2 : `http://localhost:<port>/h2-console`, JDBC URL `jdbc:h2:mem:offresdb` (ou `talentsdb`, `candidaturesdb`), utilisateur `sa`, sans mot de passe.
- Les 3 événements en `record` (`ApplicationSubmittedEvent`, `InterviewCompletedEvent`, `ApplicationAcceptedEvent`) : dans `service-candidatures` (producteur) et recopiés dans le package `dto` de chaque consommateur, comme `NotificationEmailDto` dans le TP.
- Un `RabbitConfig` par service : exchange, files, liaisons et convertisseur JSON.
- Config Server, Eureka, Gateway (routage automatique par nom de service + routes `/offers`, `/applications`, `/interviews`, `/candidates`).
- La configuration OpenFeign + Resilience4j (retry, circuit breaker) de `service-candidatures` vers `service-offres` et `service-talents`.

Reste à faire par chaque membre : mappers, services métier, contrôleurs REST, clients Feign et leur fallback, publishers et consumers RabbitMQ, tests. Voir le dossier projet pour la répartition.

## OpenFeign (synchrone) et RabbitMQ (asynchrone)

- **OpenFeign est synchrone** : `service-candidatures` appelle un autre service et attend sa réponse (ex. vérifier qu'une offre est ouverte avant d'accepter une candidature). On l'entoure de Resilience4j pour ne pas planter quand le service appelé est arrêté.
- **RabbitMQ est asynchrone** : `service-candidatures` publie un événement et continue sans attendre ; les services intéressés le traitent quand ils le reçoivent.

Fonctionnement RabbitMQ : `service-candidatures` publie sur l'exchange `hireflow.events` (type *topic*) avec une routing key. Chaque consommateur a **sa propre file** liée à cet exchange, donc un même événement est copié à tous les services qui l'écoutent (même rôle qu'un topic Kafka avec plusieurs groupes de consommateurs).

```java
// Producteur (service-candidatures, package messaging)
rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.RK_INTERVIEW_COMPLETED, event);

// Consommateur (service-offres ou service-talents, package messaging)
@RabbitListener(queues = RabbitConfig.QUEUE_INTERVIEW_COMPLETED)
public void recevoir(InterviewCompletedEvent event) { ... }
```

## Démarrer en local

1. Lancer RabbitMQ (seule infrastructure nécessaire, les bases H2 sont embarquées) :
   ```bash
   docker compose up -d
   ```
   RabbitMQ sur `localhost:5672`, console d'administration sur http://localhost:15672 (utilisateur `hireflow` / mot de passe `hireflow`).
2. Compiler tout le projet depuis la racine :
   ```bash
   ./mvnw clean install
   ```
3. Lancer les applications **dans cet ordre** (le Config Server doit être démarré en premier, sinon les services refusent de démarrer) :
   1. `config-server`
   2. `service-registry`
   3. `service-offres`, `service-talents`, `service-candidatures`
   4. `gateway`

   Par exemple avec le bouton Run d'IntelliJ, ou `./mvnw -pl config-server spring-boot:run`.
   Le Config Server lit le dossier `config-repo/` directement sur le disque (mode `native`) : après une modification, redémarrer le service concerné. La variante `git` du TP (lecture sur GitHub) est en commentaire dans `config-server/src/main/resources/application.properties`.
4. Vérifier dans Eureka (http://localhost:8761) que les services sont enregistrés, puis passer par la Gateway : http://localhost:8080/offers, `/applications`, `/candidates` (ou `http://localhost:8080/service-offres/...`).

## Contrats partagés (à ne pas changer sans prévenir l'équipe)

| Type | Nom | Émetteur → consommateur | Contenu |
|------|-----|-------------------------|---------|
| Feign ① | `GET /offers/{id}/eligibility?email=…` | candidatures → offres | statut de l'offre, compétences couvertes |
| Feign ② | `GET /candidates/by-email/{email}/profile` | candidatures → talents | experienceAnnees, statut, dernier score |
| RabbitMQ | `application.submitted` → file `talents.application-submitted` | candidatures → talents | applicationId, offerId, candidatEmail, cvUrl, date |
| RabbitMQ | `interview.completed` → files `offres.interview-completed`, `talents.interview-completed` | candidatures → offres, talents | applicationId, offerId, candidatEmail, note, avis |
| RabbitMQ | `application.accepted` → files `offres.application-accepted`, `talents.application-accepted` | candidatures → offres, talents | applicationId, offerId, candidatEmail, date |

Tous les événements passent par l'exchange `hireflow.events`. Les noms sont des constantes dans le `RabbitConfig` de chaque service.

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
   Exemples : `feature/offres-eligibility`, `feature/talents-rabbitmq-consumer`, `feature/candidatures-application-feign`, `feature/candidatures-interview`, `feature/infra-gateway`.
3. Committer souvent avec des messages clairs (`offres: ajoute le CRUD des compétences`), puis pousser la branche :
   ```bash
   git push -u origin feature/offres-crud
   ```
4. Ouvrir une **pull request vers `main`** sur GitHub, remplir le modèle de PR, et demander la relecture d'**un autre membre**.
5. Avant la fusion : `./mvnw clean verify` passe et la branche est à jour avec `main` (`git pull origin main` puis résoudre les conflits).
6. Après la fusion, supprimer la branche et repartir de `main` à jour.

Les membres 3 et 4 travaillent dans le même module `service-candidatures` : chacun reste dans son package (`application` ou `interview`) pour éviter les conflits.

Une tâche est terminée quand elle a des tests, tourne avec RabbitMQ (docker compose) et passe par la Gateway.

## Répartition

| | Membre 1 : offres | Membre 2 : talents | Membre 3 : candidatures · Application | Membre 4 : candidatures · Interview |
|---|---|---|---|---|
| Infra | Config Server | Eureka (service-registry) | Gateway | docker-compose (RabbitMQ) |
| Feign | expose l'éligibilité | expose le profil | client ① + fallback | client ② + fallback |
| RabbitMQ | consomme interview.completed, application.accepted | consomme les 3 événements | publie application.submitted, application.accepted | publie interview.completed |
