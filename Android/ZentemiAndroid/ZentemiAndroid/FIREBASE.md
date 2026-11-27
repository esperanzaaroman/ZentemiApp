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
