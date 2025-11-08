# Noyau partagé pour les microservices des assureurs partenaires

Infrastructure de domaine partagée utilisée par les microservices des assureurs partenaires de Bamboo Assur. La bibliothèque regroupe des blocs de construction réutilisables pour les applications pilotées par le domaine, une gestion cohérente des erreurs et une sérialisation homogène.

## Vue d'ensemble

Le noyau partagé expose des modules cohérents à réutiliser dans vos services :
- **Classes de base du domaine** : `AggregateRoot`, `Model` et `DomainEvent` garantissent la cohérence des agrégats et facilitent les modèles orientés événements.
- **Objets-valeur** : `DomainEntityId`, `Email`, `Phone`, `Address`, `Url` et autres primitives standardisent les identifiants et la validation.
- **Contrats d'application** : `Command`, `Query`, `CommandHandler` et `QueryHandler` simplifient l'implémentation de la CQRS.
- **Résultats fonctionnels** : `Result` fournit des issues `Success`/`Failure` sans recourir aux exceptions.
- **Contrats d'infrastructure** : `IEventPublisher` décrit comment diffuser les événements de domaine.
- **Sérialisation** : `SerializationConfig` enregistre automatiquement les sérialiseurs Kotlinx pour les primitives du domaine.
- **Utilitaires de présentation** : `ApiResponse`, `ApiResponseBodyAdvice` et `GlobalExceptionHandler` unifient les enveloppes JSON HTTP et les réponses d'erreur.

## Prérequis

- **Java** : 21 (chaîne d'outils configurée dans `build.gradle.kts`)
- **Kotlin** : 2.2.20 avec `kotlin("plugin.spring")`
- **Spring Boot** : 4.0.0-RC1 ou version ultérieure (la bibliothèque s'appuie sur l'auto-configuration de Boot)
- **Sérialisation** : Kotlinx Serialization 1.9.0

## Installation

1. Ajoutez le dépôt GitHub Packages (ou utilisez `mavenLocal()` pour le développement local).
2. Déclarez la dépendance avec les coordonnées `com.bamboo.assur.partner-insurers:shared-kernel:<version>` (version actuelle : `0.1.0`).

### Gradle (Kotlin DSL)

```kotlin
repositories {
    mavenLocal()
    maven {
        url = uri("https://maven.pkg.github.com/billionaire-devs/partner-insurers-shared-kernel")
        credentials {
            username = System.getenv("GITHUB_ACTOR")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation("com.bamboo.assur.partner-insurers:shared-kernel:0.1.0")
}
```

### Gradle (Groovy DSL)

```groovy
repositories {
    mavenLocal()
    maven {
        url = uri('https://maven.pkg.github.com/billionaire-devs/partner-insurers-shared-kernel')
        credentials {
            username = System.getenv('GITHUB_ACTOR')
            password = System.getenv('GITHUB_TOKEN')
        }
    }
}

dependencies {
    implementation 'com.bamboo.assur.partner-insurers:shared-kernel:0.1.0'
}
```

### Maven

```xml
<repositories>
  <repository>
    <id>github-bamboo-assur-shared-kernel</id>
    <url>https://maven.pkg.github.com/billionaire-devs/partner-insurers-shared-kernel</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>com.bamboo.assur.partner-insurers</groupId>
    <artifactId>shared-kernel</artifactId>
    <version>0.1.0</version>
  </dependency>
</dependencies>
```

> **Authentification** : pour télécharger depuis GitHub Packages, fournissez un jeton personnel avec le scope `read:packages` via `GITHUB_TOKEN` (associé à `GITHUB_ACTOR`).

## Auto-configuration Spring Boot

L'import de la dépendance active automatiquement `SerializationConfig` grâce à l'entrée déclarée dans `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`. Aucune configuration supplémentaire n'est nécessaire pour exposer le bean `Json` configuré.

Si vous souhaitez surcharger les composants auto-configurés, déclarez des beans du même type dans votre contexte d'application.

## Blocs de construction du domaine

- **Agrégats** : étendez `AggregateRoot` pour encapsuler les invariants du domaine et enregistrer les événements.
- **Entités/Modèles** : étendez `Model` pour bénéficier de la gestion des horodatages et de l'égalité basée sur l'identifiant.
- **Événements de domaine** : spécialisez `DomainEvent` pour décrire les transitions d'état et publiez-les via `IEventPublisher`.

Exemple d'agrégat et d'événement :

```kotlin
@Serializable
class PartnerRegisteredEvent(
    aggregateId: DomainEntityId,
    val partnerName: String,
) : DomainEvent(
    aggregateId = aggregateId,
    aggregateType = "Partner",
    eventType = DomainEvent.getEventTypeNameOrDefault<PartnerRegisteredEvent>("PartnerRegistered"),
)

class PartnerAggregate(
    id: DomainEntityId,
    val name: String,
) : AggregateRoot(id) {

    fun registerPartner() {
        addDomainEvent(
            PartnerRegisteredEvent(
                aggregateId = id,
                partnerName = name,
            ),
        )
    }
}
```

Utilisez les objets-valeur pour renforcer le typage :

```kotlin
val partnerId = DomainEntityId.random()
val website = Url.from("https://assur.example.com")
```

## Contrats de couche application

- **Commandes** : implémentez l'interface marqueur `Command` pour les opérations d'écriture.
- **Requêtes** : implémentez l'interface marqueur `Query<R>` pour représenter les lectures (voir `Query.kt`).
- **Handlers** : implémentez `CommandHandler<C, R>` ou `QueryHandler<Q, R>` pour encapsuler les cas d'usage.

Exemple de handler :

```kotlin
data class RegisterPartnerCommand(val name: String) : Command

class RegisterPartnerHandler(
    private val repository: PartnerRepository,
    private val publisher: IEventPublisher,
) : CommandHandler<RegisterPartnerCommand, Result<DomainEntityId>> {

    override suspend fun invoke(command: RegisterPartnerCommand): Result<DomainEntityId> = Result.of {
        val aggregate = PartnerAggregate(DomainEntityId.random(), command.name)
        aggregate.registerPartner()

        repository.save(aggregate)
        publisher.publish(aggregate.getDomainEvents())
        aggregate.clearDomainEvents()

        aggregate.id
    }
}
```

## Publication des événements de domaine

Implémentez `IEventPublisher` dans votre service pour relayer les événements vers Kafka, une table outbox ou tout autre bus. Le noyau partagé fournit le contrat et `EventPublishingException` pour harmoniser la gestion des échecs.

```kotlin
class OutboxEventPublisher(
    private val outboxRepository: OutboxRepository,
    private val json: Json,
) : IEventPublisher {

    override suspend fun publish(events: List<DomainEvent>) {
        events.forEach { publish(it) }
    }

    override suspend fun publish(event: DomainEvent) {
        runCatching {
            outboxRepository.save(json.encodeToString(event))
        }.onFailure { cause ->
            throw EventPublishingException("Failed to persist event ${'$'}{event.eventType}", cause)
        }
    }
}
```

## Utilitaires de présentation

En intégrant le noyau partagé dans un service Spring Boot :
- **`ApiResponseBodyAdvice`** encapsule chaque réponse HTTP dans une enveloppe `ApiResponse` et injecte les métadonnées requête/réponse.
- **`GlobalExceptionHandler`** traduit les exceptions (dont `DomainException`, les erreurs de validation et les problèmes de persistance) en charges d'erreur cohérentes.

Il n'est pas nécessaire de déclarer ces beans manuellement. Pour exclure un contrôleur spécifique, retournez directement un `ApiResponse` ou désactivez le `ResponseBodyAdvice` via la configuration Spring.

Exemple de contrôleur :

```kotlin
@RestController
@RequestMapping("/partners")
class PartnerController(
    private val handler: RegisterPartnerHandler,
) {

    @PostMapping
    suspend fun register(@RequestBody payload: RegisterPartnerRequest): Result<DomainEntityId> {
        return handler(RegisterPartnerCommand(payload.name))
    }
}
```

Lorsque le handler renvoie `Result.Success`, l'advice encapsule le corps dans une enveloppe de succès. Les cas d'exception sont pris en charge par `GlobalExceptionHandler`.

## Sérialisation

`SerializationConfig` fournit un bean `Json` configuré pour :
- **Ignorer les clés inconnues** lors de la désérialisation pour conserver la compatibilité ascendante.
- **Encoder les valeurs par défaut** afin que les champs optionnels soient présents dans le JSON.
- **Omettre les nulls explicites** pour des charges utiles plus propres.
- **Enregistrer des sérialiseurs contextuels** pour `DomainEntityId`, `Url` et `Instant` (`DomainEntityIdSerializer`, `UrlSerializer`, `InstantSerializer`).

Injectez le bean partout où nécessaire :

```kotlin
@Component
class JsonExample(private val json: Json) {
    fun serialize(event: DomainEvent): String = json.encodeToString(event)
}
```

Pour personnaliser le comportement, définissez votre propre bean `Json`. Spring privilégiera votre définition grâce à l'annotation `@ConditionalOnMissingBean` placée sur la configuration de la bibliothèque.

## Motif Result

Le type scellé `Result` (défini dans `src/main/kotlin/com/bamboo/assur/partnerinsurers/sharedkernel/domain/Result.kt`) permet d'exprimer explicitement le succès ou l'échec d'une opération métier sans recourir systématiquement aux exceptions.

- **`Result.Success<T>`** encapsule la valeur attendue lorsque l'opération se déroule correctement.
- **`Result.Failure`** transporte un message d'erreur (et optionnellement une cause) destiné à l'appelant.
- Des helpers comme `Result.of { ... }`, `map`, `flatMap`, `getOrNull`, `getOrElse` facilitent la composition fonctionnelle des traitements.

### Exemple : validation métier puis persistance

```kotlin
fun handle(command: RegisterPartnerCommand): Result<DomainEntityId> = Result.of {
    require(command.name.isNotBlank()) { "Le nom du partenaire est obligatoire" }

    repository.findByName(command.name)?.let {
        throw IllegalArgumentException("Partner already exists")
    }

    createPartner(command)
}
    .map { partner ->
        repository.save(partner)
        partner.id
    }
    .onFailure { error -> logger.warn("Échec d'enregistrement", error) }
```

### Exemple : agrégation de deux opérations dépendantes

```kotlin
suspend fun buildDashboard(partnerId: DomainEntityId): Result<PartnerDashboard> =
    partnerQuery(partnerId)
        .flatMap { partner ->
            statisticsQuery(partner)
                .map { stats -> PartnerDashboard(partner, stats) }
        }
```

### Propager le résultat vers la couche web

En contrôleur, vous pouvez transformer `Result` en réponse HTTP cohérente avec les utilitaires d'exception du noyau partagé :

```kotlin
@PostMapping
suspend fun register(@RequestBody payload: RegisterPartnerRequest): ResponseEntity<ApiResponse<DomainEntityId>> {
    return when (val result = handler(RegisterPartnerCommand(payload.name))) {
        is Result.Success -> ResponseEntity.ok(ApiResponse(true, meta = buildMeta(), data = result.value))
        is Result.Failure -> throw BusinessRuleViolationException(result.message ?: "Erreur inconnue")
    }
}
```

L'enveloppe `ApiResponse` et `GlobalExceptionHandler` se chargent alors de rendre la réponse JSON homogène pour les consommateurs.

## Développement local et publication

- **Build** : `./gradlew build`
- **Publication dans Maven Local** : `./gradlew publishToMavenLocal`
- **Publication sur GitHub Packages** : `./gradlew publish`

Des tâches d'assistance sont définies dans `publish.gradle.kts` (`buildAndPublish`, `publishToGitHubPackages`).

## Versioning et mises à niveau

- Suivez la version publiée dans vos services consommateurs pour bénéficier des correctifs et améliorations.
- Les changements majeurs feront évoluer la version mineure tant que l'API n'est pas stabilisée (pré-1.0.0).
- Consultez les notes de version ou l'historique des commits avant toute montée de version.

## Support et contributions

Ouvrez des issues ou des pull requests dans le dépôt pour faire évoluer le noyau partagé de manière collaborative. Documentez toute nouvelle primitive ou handler afin de garder les équipes consommatrices alignées.