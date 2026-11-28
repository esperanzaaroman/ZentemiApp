# Firebase en Android

La app Android usa el mismo proyecto de Firebase que iOS (`zentemi-equipo4`),
con la misma base de datos de Firestore y las mismas reglas (`firestore.rules`).

## Configuración (una sola vez)

1. Firebase Console → proyecto **zentemi-equipo4** → ⚙️ Configuración del proyecto
   → *Tus apps* → **Agregar app** → Android.
2. Nombre del paquete: `com.zentemi.app`
3. Descarga `google-services.json` y colócalo en `app/google-services.json`
   (junto a `app/build.gradle.kts`).
4. Sync de Gradle y ejecutar.

Sin ese archivo la compilación falla en la tarea `processDebugGoogleServices`.

## Estructura compartida con iOS

| Ruta en Firestore | Android | iOS |
|---|---|---|
| `users/{uid}` | `ParentProfile` | `ParentProfile` |
| `users/{uid}/children/{childId}` | `ChildProfile` | `ChildProfile` |
| `.../children/{childId}/progress/{lessonId}` | `LessonProgressRecord` | `LessonProgressRecord` |
| `.../children/{childId}/inventory/{itemId}` | `InventoryItemRecord` | `InventoryItemRecord` |
| `users/{uid}/audit_logs/{logId}` | `AuditLogRecord` | `AuditLogRecord` |

Código: `app/src/main/java/com/zentemi/app/data/` (`AuthService`, `FirestoreService`,
`FirestoreModels`) y la sincronización en `state/AppState.kt`.

## Campos del perfil del menor que agrega Android

Todos son opcionales: si el documento no los trae (perfil viejo o creado desde
iOS), Android se queda con lo que ya hay en el teléfono. Las claves son las
mismas que se guardan en el teléfono (y en UserDefaults de iOS).

| Campo | Tipo | Contenido |
|---|---|---|
| `pathLessonsCompleted`, `unlockedPathLesson` | número | Avance del camino |
| `greetingsRewardClaimed` | booleano | «Primeras frases» terminada |
| `ownedItems`, `equippedItems` | lista de ids | Tienda |
| `claimedLessonRewards` | lista | Lecciones 2 a 6 terminadas (`family`, `numbers`…) |
| `lessonBestCorrectAnswers`, `lessonBestSeconds` | mapa clave → número | Mejor puntaje y tiempo por lección |
| `completedGameLevels` | lista | `memoryLevelOne`, `wordSearchLevelTwo`, `mazeLevelThree`… |
| `gameBestSeconds` | mapa clave → número | Mejor tiempo por nivel de juego |
| `mazeClaimedCoinMasks` | mapa clave → número | Monedas cobradas por laberinto (un bit por moneda) |
| `gamePoints` | número | Puntos de juego (meta 10,000) |
| `totalFeedings`, `bestAchievementStreak`, `lifetimeEarnedCoins` | número | Logros |
