# Noyau partagé des microservices des services des Assureurs Partenaires

Bibliotheque de développement de systemes de gestion de domaines partagés pour l 'écosysteme des microservices des services des Assureurs Partenaires de Bamboo Assur.

## Vue d'ensemble

Cette bibliotheque fournit des blocs de construction de conception de design orienté  objet réutilisables :
- Classes de base : `AggregateRoot`, `DomainEvent`, `Model`
- Objets-valeur : `DomainEntityId`, `EmailAddress`, `Address`, etc.
- Mod les d'applications : `Command`, `Query`, `CommandHandler`, `QueryHandler`
- Types de résultats pour la gestion fonctionnelle des erreurs

## Installation

### Gradle Kotlin DSL
```kotlin
repositories {
    mavenLocal() // For local development
    // Or for production:
    maven {
        url = uri("[https://maven.pkg.github.com/billionaire-devs/bamboo-assur-shared-kernel](https://maven.pkg.github.com/billionaire-devs/bamboo-assur-shared-kernel)")
        credentials {
            username = System.getenv("GITHUB_ACTOR")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation("com.bamboo.assur:shared-kernel:1.0.0")
}