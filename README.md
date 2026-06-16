# SportTracker — Aplikacja Android

Projekt zaliczeniowy z przedmiotu **Aplikacje Mobilne** (6. semestr, WAT).  
Aplikacja do śledzenia aktywności fizycznej z architekturą 3-warstwową: Android + Spring Boot + PostgreSQL.

## Funkcjonalności

- **Rejestracja i logowanie** z autoryzacją JWT
- **Śledzenie treningu w czasie rzeczywistym**
  - GPS — trasa na mapie Google Maps
  - Akcelerometr — licznik kroków (algorytm peak detection)
  - Foreground Service — działa w tle z powiadomieniem
- **Historia treningów** z kartami Material Design
- **Profil użytkownika** — edycja nazwy, statystyki
- **Ustawienia** — zmiana motywu (jasny/ciemny/systemowy) i języka (PL/EN) bez restartu aplikacji
- **Push notyfikacje** przez Firebase Cloud Messaging
- **HTTPS** z certyfikatem SSL i zaufaniem do certyfikatu serwera

## Architektura

```
MVVM + Clean Architecture
├── presentation/   — Fragment + ViewModel (StateFlow)
├── domain/         — modele domenowe, interfejsy repozytoriów
├── data/           — implementacje repozytoriów, API, baza lokalna
├── service/        — Foreground Service (GPS + akcelerometr), FcmService
├── di/             — moduły Hilt (DI)
└── util/           — stałe, rozszerzenia Kotlin
```

**Nawigacja:** Single Activity + Navigation Component (dwa grafy: auth / main)

## Technologie

| Kategoria | Biblioteka |
|---|---|
| Architektura | MVVM, Hilt, Navigation Component |
| Sieć | Retrofit 2, OkHttp 4, Gson |
| Baza lokalna | Room (offline-first) |
| Mapa | Google Maps SDK |
| Lokalizacja | FusedLocationProviderClient |
| Sensory | SensorManager (akcelerometr) |
| Bezpieczeństwo | EncryptedSharedPreferences, Android Keystore |
| Push | Firebase Cloud Messaging |
| UI | Material Design 3, ViewBinding, RecyclerView + DiffUtil |
| Async | Kotlin Coroutines + Flow |
| DI | Hilt |

## Wymagania

- Android 7.0+ (API 24)
- Emulator lub urządzenie z Google Play Services
- Uruchomiony backend SportTracker

## Konfiguracja

### 1. Google Maps API Key

Utwórz plik `local.properties` w głównym katalogu projektu:

```properties
MAPS_API_KEY=twój_klucz_api
```

### 2. Firebase

Pobierz `google-services.json` z Firebase Console i umieść w katalogu `app/`.

### 3. Backend

Ustaw adres backendu w `util/Constants.kt`:

```kotlin
const val BASE_URL = "https://10.0.2.2:8443/"  // emulator → localhost
```

Skopiuj certyfikat SSL backendu do `app/src/main/res/raw/dev_cert.crt`.

### 4. Budowanie

```bash
./gradlew assembleDebug
```

## Struktura projektu

```
app/src/main/java/wat/edu/pl/projektam/
├── data/
│   ├── local/db/          — Room: encje, DAO, baza danych
│   ├── local/preferences/ — TokenManager, AppPreferences
│   ├── remote/api/        — Retrofit API services
│   ├── remote/dto/        — klasy request/response
│   └── repository/        — implementacje repozytoriów
├── di/                    — NetworkModule, DatabaseModule, RepositoryModule
├── presentation/
│   ├── auth/              — LoginFragment, RegisterFragment
│   ├── home/              — HomeFragment
│   ├── workout/           — WorkoutFragment (mapa + sensory)
│   ├── history/           — HistoryFragment, WorkoutAdapter
│   ├── profile/           — ProfileFragment
│   └── settings/          — SettingsFragment
├── service/
│   ├── WorkoutTrackingService.kt
│   └── FcmService.kt
└── util/                  — Constants, Extensions, Resource
```

## Autorzy

Krzysztof Dobrowolski, Przemysław Defut
