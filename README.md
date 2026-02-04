<p align="center">
  <img src="logo.png" alt="Drape Logo" width="120"/>
</p>

# Drape

**Drape** è un'app Android per la gestione del guardaroba digitale. Organizza i tuoi vestiti, pianifica outfit e tieni traccia del tuo stile personale.

## Tecnologie

- **Jetpack Compose** - UI dichiarativa moderna
- **Hilt** - Dependency Injection
- **Firebase** - Backend (Auth, Firestore, Storage)
- **ML Kit** - Segmentazione immagini
- **Kotlin Coroutines & Flow** - Programmazione asincrona

## Requisiti

- Android Studio Ladybug o superiore
- JDK 11+
- Android SDK 24+ (minSdk)

## Setup

1. Clona il repository
2. Configura Firebase e aggiungi `google-services.json` in `app/`
3. Sincronizza Gradle
4. Esegui l'app

## Build & Test

```bash
# Build debug APK
./gradlew assembleDebug

# Run lint checks
./gradlew lint

# Run unit tests
./gradlew test

# Run instrumented tests (richiede emulatore/device)
./gradlew connectedAndroidTest

# Clean build
./gradlew clean assembleDebug
```

## Architettura

```
ui/               → Compose screens + ViewModels
data/
  ├── model/      → Data classes (Firestore)
  ├── repository/ → Business logic
  └── datasource/ → Firebase operations
navigation/       → Navigazione type-safe
di/               → Moduli Hilt
```

## Licenza

Vedi [LICENSE](LICENSE) per i dettagli.
