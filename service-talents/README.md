# service-talents

Membre 2 · également responsable d'Eureka (`service-registry`).

service-talents tient la base des candidats connus et de leurs tests de compétences. Dans le parcours de recrutement, il :

- **donne le profil** d'un candidat à service-candidatures avant un entretien (Feign ②) ;
- **réagit aux 3 événements RabbitMQ** de service-candidatures : il crée le profil au dépôt et prévient le RH, enregistre l'évaluation de l'entretien, puis lance l'onboarding quand le candidat est retenu.

| | |
|---|---|
| Port | `8083` |
| Base | H2 en mémoire `talentsdb`, recréée à chaque démarrage depuis `data.sql` |
| Configuration | `config-repo/service-talents.properties` (servie par le Config Server) |
| Entités | `Candidate`, `ScreeningTest` (un candidat passe 0..n tests) |

## Démarrer et vérifier

Prérequis : RabbitMQ (`docker compose up -d`), puis `config-server` et `service-registry` démarrés.

```bash
./mvnw -pl service-talents spring-boot:run
```

| Quoi | URL |
|---|---|
| Swagger | http://localhost:8083/swagger-ui.html |
| Liste des candidats | http://localhost:8083/candidates (ou via la Gateway : http://localhost:8080/candidates) |
| Console H2 | http://localhost:8083/h2-console · JDBC `jdbc:h2:mem:talentsdb` · utilisateur `sa`, sans mot de passe |
| Files RabbitMQ | http://localhost:15672 (`hireflow` / `hireflow`) → Queues → `talents.*` |

**Données de démo** (mêmes emails que dans les autres services) :

| Candidat | Email | Expérience | Statut | Test |
|---|---|---|---|---|
| Amine Jamali | amine@email.com | 3 ans | EN_PROCESSUS | Java · 78 |
| Sara Bennani | sara@email.com | 5 ans | EN_PROCESSUS | Angular · 85 |
| Youssef Alaoui | youssef@email.com | 1 an | EN_PROCESSUS | Java · 55 |

## API REST

Toutes les routes commencent par `/candidates`. Les erreurs renvoient un `ApiError` : `timestamp`, `status`, `error`, `message`, `path`.

| Méthode | Route | Rôle | Codes |
|---|---|---|---|
| POST | `/candidates` | Créer un candidat (statut NOUVEAU) | 201 · 400 · 409 email déjà utilisé |
| GET | `/candidates?statut=` | Lister, filtre optionnel par statut | 200 |
| GET | `/candidates/{id}` | Fiche avec l'historique des tests | 200 · 404 |
| GET | `/candidates/by-email/{email}` | Fiche par email | 200 · 404 |
| GET | `/candidates/by-email/{email}/profile` | Profil résumé (Feign ②) | 200 · 404 |
| PUT | `/candidates/{id}` | Modifier nom, téléphone, expérience | 200 · 400 · 404 |
| DELETE | `/candidates/{id}` | Supprimer (ses tests avec lui) | 204 · 404 |
| POST | `/candidates/{id}/tests` | Ajouter un test, score 0 à 100 | 201 · 400 · 404 |
| GET | `/candidates/{id}/tests` | Tests du plus récent au plus ancien | 200 · 404 |

## Feign ② : le profil du candidat

Appelé par service-candidatures (`TalentClient`) avant de planifier un entretien. Contrat partagé : ne pas renommer les champs sans prévenir l'équipe.

```
GET /candidates/by-email/amine@email.com/profile
```
```json
{
  "candidateId": 1,
  "nom": "Amine Jamali",
  "email": "amine@email.com",
  "experienceAnnees": 3,
  "statut": "EN_PROCESSUS",
  "dernierTest": "Java",
  "dernierScore": 78,
  "nombreTests": 1
}
```

Candidat inconnu : 404. Côté candidatures, le fallback affiche alors un résumé vide et l'entretien est quand même planifié.

## RabbitMQ : les 3 événements consommés

Les 3 files sont liées à l'exchange `hireflow.events` (type topic) publié par service-candidatures. Chaque service consommateur a sa propre file : un même événement est copié à offres et à talents.

| Routing key | File | Ce que fait talents | Statut du candidat |
|---|---|---|---|
| `application.submitted` | `talents.application-submitted` | Crée le candidat s'il est inconnu (nom provisoire = partie avant le `@`), log `[RH] Nouvelle candidature` | → EN_PROCESSUS |
| `interview.completed` | `talents.interview-completed` | Ajoute un test « Entretien · candidature N », score = note × 5 (sur 100) | → EVALUE |
| `application.accepted` | `talents.application-accepted` | Log `[ONBOARDING] Email de bienvenue envoyé à …` (email simulé) | → EMBAUCHE |

**Le statut ne recule jamais** : NOUVEAU → EN_PROCESSUS → EVALUE → EMBAUCHE. Un candidat déjà EMBAUCHE qui postule ailleurs reste EMBAUCHE.

**Un message reçu deux fois ne change rien** (idempotence). RabbitMQ peut livrer un message plus d'une fois : un entretien déjà enregistré pour cette candidature est ignoré, et l'email d'onboarding n'est envoyé qu'une fois.

**Message en erreur : 3 essais, puis la DLQ**

- Si un listener échoue, Spring réessaie 2 fois (après 1 s, puis 2 s) : `spring.rabbitmq.listener.simple.retry.*` dans `config-repo`.
- Après le 3ᵉ échec, le message n'est **pas** remis dans sa file, ce qui évite une boucle infinie. Il est republié par un `RepublishMessageRecoverer` dans l'exchange `talents.dlx` (fanout), puis la file `talents.dlq`.
- Le message y est conservé intact, avec la cause de l'erreur en en-têtes : `x-exception-message`, `x-exception-stacktrace`, `x-original-routingKey`.
- Pour le lire : console RabbitMQ → Queues → `talents.dlq` → *Get messages*. Pour le rejouer après correction : le republier sur `hireflow.events` avec sa routing key d'origine.
- Exemple de cause : un email mal formé est refusé avant tout accès à la base, avec le message « Email invalide reçu par RabbitMQ : … ».

## Règles métier

| Règle | Description |
|---|---|
| RG1 | Nom et email obligatoires à la création |
| RG2 | Email unique, sans tenir compte des majuscules (sinon 409) |
| RG3 | Score d'un test entre 0 et 100 |
| RG4 | Date du test : celle du jour si absente, jamais dans le futur |
| RG5 | `application.submitted` : candidat inconnu créé avec un nom provisoire, puis EN_PROCESSUS |
| RG6 | `interview.completed` : l'entretien devient un test, une seule fois par candidature, puis EVALUE |
| RG7 | `application.accepted` : EMBAUCHE et email de bienvenue, une seule fois |
| RG8 | Le statut ne fait qu'avancer |

## Tests

```bash
./mvnw -pl service-talents verify
```

25 tests, sans RabbitMQ ni Config Server (configuration de test dans `src/test/resources`).

| Classe | Type | Ce qu'elle vérifie |
|---|---|---|
| `CandidateServiceTest` | unitaire (Mockito) | RG2, RG5, RG8, emails invalides reçus par RabbitMQ, profil (dernier test, sans test, inconnu) |
| `MessagingTraitementsTest` | unitaire (Mockito) | RG6, RG7 et l'idempotence des traitements |
| `CandidateControllerTest` | contrôleur (`@WebMvcTest`) | codes 201, 400, 404 et champs JSON du profil Feign ② |
| `ListenersIntegrationTest` | intégration (`TestRabbitTemplate`, sans serveur RabbitMQ) | parcours complet dépôt → entretien → embauche avec des messages construits comme ceux de candidatures (en-tête `__TypeId__` étranger) : conversion JSON, RG5 à RG8, doublons |
| `RabbitConfigDeadLetterTest` | unitaire | la DLQ est liée à `talents.dlx`, un message en échec y est republié avec sa cause |

### Simuler les événements à la main

Avec talents et RabbitMQ démarrés, dans **Git Bash** (en PowerShell, `curl` et la syntaxe des fonctions sont différents) :

```bash
pub() {
  curl -s -u hireflow:hireflow -X POST "http://localhost:15672/api/exchanges/%2F/hireflow.events/publish" \
    -H "content-type: application/json" \
    -d "{\"routing_key\":\"$1\",\"properties\":{\"content_type\":\"application/json\"},\"payload_encoding\":\"string\",\"payload\":\"$(printf '%s' "$2" | sed 's/"/\\"/g')\"}"
  echo
}

pub application.submitted '{"applicationId":101,"offerId":1,"candidatEmail":"karim@email.com","cvUrl":null,"date":"2026-11-20"}'
pub interview.completed   '{"applicationId":101,"offerId":1,"candidatEmail":"karim@email.com","note":16,"avis":"Tres bon profil"}'
pub application.accepted  '{"applicationId":101,"offerId":1,"candidatEmail":"karim@email.com","date":"2026-11-20"}'

curl http://localhost:8083/candidates/by-email/karim@email.com
```

- Chaque `pub` doit répondre `{"routed":true}`. Sinon, le message n'est pas parti : `not_json` vient souvent d'un accent mal encodé par le terminal.
- Résultat attendu : karim est EMBAUCHE, avec un test « Entretien · candidature 101 » à 80, et le log `[ONBOARDING]`.
- Un email invalide, comme `"karim@"`, finit dans `talents.dlq` après environ 3 secondes.

## Points ouverts

- **Note d'entretien** : enregistrée sur 100 (note × 5) pour respecter RG3. Le document des livrables indique score = note. À valider en équipe.
- **Messages de test dans `offres.*`** : chaque `pub` de `interview.completed` ou `application.accepted` est aussi copié dans les files de service-offres. Les purger avant de tester les listeners d'offres, sinon ils seront rejoués.
