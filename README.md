# Hire-Flow

Application de suivi du recrutement d'une PME, en microservices Spring Boot.
Un candidat dépose une candidature sur une offre, elle passe en entretien, puis elle est retenue ou refusée.
Une candidature retenue clôture l'offre, déclenche l'onboarding du candidat et lui envoie un email.

Les services communiquent en **synchrone** avec **OpenFeign** (+ Resilience4j) quand ils ont besoin d'une réponse immédiate, et en **asynchrone** avec **RabbitMQ** pour prévenir les autres services qu'un événement a eu lieu (sans attendre de réponse).

L'organisation du dépôt reprend celle du projet de référence du cours, [tp-ms-ecom](https://github.com/salimk/tp-ms-ecom) (M. KHALIL Salim).

## Équipe

| Membre | Service métier | Infrastructure | Communication |
|---|---|---|---|
| Hamza | `service-offres` (offres, compétences, éligibilité, avancement) | Config Server | expose Feign ①, consomme `interview.completed` et `application.accepted` |
| Douae | `service-talents` (candidats, tests, onboarding) | Eureka | expose Feign ②, consomme les 3 événements |
| Yasmine | `service-candidatures` · Application (dépôt, décision, pipeline) + `service-notification` | Gateway | client Feign ① + fallback, publie `application.submitted` et `application.accepted` |
| Abderahman | `service-candidatures` · Interview (entretiens, notation, temps réel SSE) | Docker (compose + Dockerfile) | client Feign ② + fallback, publie `interview.completed` |

## Exigences du module

| Version | Exigence | Où dans le projet |
|---|---|---|
| V0 | Conception (diagramme de classes) | `docs/` |
| V1 | Microservices + CRUD | `service-offres`, `service-talents`, `service-candidatures`, `service-notification` (H2 + JPA) |
| V2 | Eureka + Gateway | `service-registry` (8761), `gateway` (8080, routes `lb://`) |
| V3 | Config Server | `config-server` (8888) qui sert `config-repo/` |
| V4 | OpenFeign | `service-candidatures` → `service-offres` (éligibilité) et → `service-talents` (profil) |
| V5 | Service de notification découplé + RabbitMQ | `service-notification` : écoute les 3 événements et envoie les emails via **Mailpit** |
| V6 | Résilience | Resilience4j sur les 2 clients Feign : retry, circuit breaker, fallback (`config-repo/service-candidatures.properties`) |

## Stack

- Java 17, Maven (wrapper fourni : `./mvnw`)
- Spring Boot 3.5, Spring Cloud 2025.0 (Config Server, Eureka, Gateway, OpenFeign, Resilience4j)
- Spring Data JPA + **H2 en mémoire** (une base par service, données de démo dans `data.sql`)
- **RabbitMQ** (Spring AMQP) pour les événements
- **Spring Mail + Mailpit** pour les emails (SMTP de test, aucun email ne part vraiment sur Internet)
- Lombok, springdoc-openapi (Swagger)
- Docker Compose : l'infrastructure (RabbitMQ, Mailpit) et, en option, les 7 applications

## Structure du dépôt

```
Hire-Flow/
├── pom.xml                  POM agrégateur (chaque module est un projet Spring Boot autonome)
├── config-repo/             Configuration de chaque service, servie par le Config Server
├── config-server/           Spring Cloud Config Server            port 8888
├── service-registry/        Annuaire Eureka                       port 8761
├── gateway/                 API Gateway                           port 8080
├── service-offres/          JobOffer, RequiredSkill               port 8081
├── service-candidatures/    Application, Interview (+ SSE)        port 8082
├── service-talents/         Candidate, ScreeningTest              port 8083
├── service-notification/    Notification (emails via Mailpit)     port 8084
├── postman/                 Collection Postman de toute l'équipe
├── Dockerfile               Une image pour toutes les applications (argument MODULE)
└── docker-compose.yml       RabbitMQ + Mailpit (+ les 7 applications avec --profile apps)
```

Détail du service talents : [service-talents/README.md](service-talents/README.md).

Chaque service n'a dans `src/main/resources/application.properties` que son nom et l'adresse du Config Server ; tout le reste (port, H2, RabbitMQ, Eureka, Resilience4j, routes, SMTP) est dans `config-repo/`.

## Les endpoints (tous via la Gateway, http://localhost:8080)

| Service | Endpoints |
|---|---|
| offres | `GET/POST /offers`, `GET/PUT/DELETE /offers/{id}`, `POST /offers/{id}/skills`, `DELETE /offers/{id}/skills/{skillId}`, `POST /offers/{id}/eligibility`, `GET /offers/{id}/progress` |
| talents | `GET/POST /candidates`, `GET/PUT/DELETE /candidates/{id}`, `GET /candidates/by-email/{email}`, `GET /candidates/by-email/{email}/profile`, `GET/POST /candidates/{id}/tests` |
| candidatures · Application | `GET/POST /applications`, `GET /applications/{id}`, `PATCH /applications/{id}/status`, `GET /applications/pipeline?offerId=`, `GET /applications/stream` (SSE) |
| candidatures · Interview | `GET/POST /applications/{id}/interviews`, `GET/DELETE /interviews/{id}`, `PUT /interviews/{id}/evaluation` |
| notification | `GET /notifications?destinataire=`, `POST /notifications/email` |

Une candidature n'a ni `PUT` ni `DELETE` : c'est un choix métier. Une candidature déposée ne se modifie pas et ne s'efface pas, elle **change de statut** (DEPOSEE → EN_ENTRETIEN → RETENUE ou REFUSEE) et garde ainsi l'historique du recrutement.

Swagger de chaque service : `http://localhost:<port>/swagger-ui.html`.

## OpenFeign (synchrone) et RabbitMQ (asynchrone)

- **OpenFeign est synchrone** : `service-candidatures` appelle un autre service et attend sa réponse (vérifier qu'une offre accepte la candidature, récupérer le profil d'un candidat avant l'entretien). Resilience4j l'entoure pour ne pas planter quand le service appelé est arrêté : 3 tentatives, puis circuit breaker, puis fallback (réponse 503 claire).
- **RabbitMQ est asynchrone** : `service-candidatures` publie un événement et continue sans attendre ; les services intéressés le traitent quand ils le reçoivent. L'événement n'est publié **qu'après le commit** de la transaction, pour ne jamais annoncer une candidature qui n'existe pas en base.

`service-candidatures` publie sur l'exchange `hireflow.events` (type *topic*) avec une routing key. Chaque consommateur a **sa propre file** liée à cet exchange, donc un même événement est copié à tous les services qui l'écoutent.

```
                                   ┌─> offres.*        (service-offres)
candidatures ──> hireflow.events ──┼─> talents.*       (service-talents)
                                   └─> notification.*  (service-notification) ──SMTP──> Mailpit
```

## Contrats partagés (à ne pas changer sans prévenir l'équipe)

| Type | Nom | Émetteur → consommateur | Contenu |
|------|-----|-------------------------|---------|
| Feign ① | `POST /offers/{id}/eligibility` | candidatures → offres | compétences déclarées → offre ouverte ? compétences couvertes ? |
| Feign ② | `GET /candidates/by-email/{email}/profile` | candidatures → talents | experienceAnnees, statut, dernier score |
| RabbitMQ | `application.submitted` | candidatures → talents, notification | applicationId, offerId, candidatEmail, cvUrl, date |
| RabbitMQ | `interview.completed` | candidatures → offres, talents, notification | applicationId, offerId, candidatEmail, note, avis |
| RabbitMQ | `application.accepted` | candidatures → offres, talents, notification | applicationId, offerId, candidatEmail, date |

Emails envoyés par `service-notification` :

| Événement | Email |
|---|---|
| `application.submitted` | accusé de réception au candidat + alerte aux RH (`rh@hireflow.local`) |
| `interview.completed` | note et avis de l'entretien aux RH |
| `application.accepted` | félicitations au candidat |

Si Mailpit est arrêté, le message est retenté 3 fois, puis abandonné sans bloquer les autres services.

## Démarrer en local (depuis l'IDE)

1. Lancer l'infrastructure :
   ```bash
   docker compose up -d
   ```
   RabbitMQ sur `localhost:5672` (console http://localhost:15672, `hireflow` / `hireflow`) et Mailpit (boîte mail http://localhost:8025).
2. Compiler et tester tout le projet depuis la racine :
   ```bash
   ./mvnw clean install
   ```
3. Lancer les applications **dans cet ordre** : `config-server`, `service-registry`, `gateway`, puis `service-offres`, `service-talents`, `service-candidatures`, `service-notification`.
   Le Config Server lit le dossier `config-repo/` sur le disque (mode `native`) : après une modification, redémarrer le service concerné.
4. Vérifier dans Eureka (http://localhost:8761) que les 5 services sont enregistrés, puis passer par la Gateway : http://localhost:8080/offers.

Pour développer `service-candidatures` sans les autres services : profil `fake` (réponses Feign lues dans `src/main/resources/fake/`).

## Démarrer tout avec Docker (démo)

```bash
./mvnw clean package -DskipTests
docker compose --profile apps up -d --build
```

Les 7 applications, RabbitMQ et Mailpit démarrent dans des conteneurs. Compter 2 à 3 minutes avant que tout soit enregistré dans Eureka. Pour tout arrêter :

```bash
docker compose --profile apps down
```

## Tests

- Tests unitaires et de contrôleur dans chaque service : `./mvnw clean install`.
- Tests de bout en bout : importer `postman/HireFlow.postman_collection.json` dans Postman et lancer **Run collection** (les dossiers s'exécutent dans l'ordre), ou en ligne de commande :
  ```bash
  npx newman run postman/HireFlow.postman_collection.json
  ```
  Le dossier 5 rejoue le scénario complet (offre → candidature → entretien → embauche) et le dossier 6 vérifie les notifications et les emails reçus par Mailpit.

## Workflow Git

La branche `main` contient toujours un projet qui compile et démarre. **Personne ne pousse directement sur `main`.**

1. `git checkout main` puis `git pull origin main`.
2. Créer une branche par fonctionnalité : `git checkout -b feature/<périmètre>-<sujet>`.
3. Committer avec des messages clairs, puis `git push -u origin feature/...`.
4. Ouvrir une **pull request vers `main`**, remplir le modèle de PR, et demander la relecture d'**un autre membre**.
5. Avant la fusion : `./mvnw clean verify` passe et la branche est à jour avec `main`.
6. Après la fusion, supprimer la branche et repartir de `main` à jour.

Yasmine et Abderahman travaillent dans le même module `service-candidatures` : chacun reste dans son package (`application` ou `interview`).
