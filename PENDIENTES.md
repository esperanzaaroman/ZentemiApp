# Pendientes de Zentemi

Notas para revisar con Sambe. Actualizado: 27 de noviembre de 2026.

## 1. Igualar iOS con Android en "Jugar con Maicito"

En Android ya quedó **un solo catálogo** con 5 juegos: Memorama (el de Leo, por
niveles), Laberinto, Sopa de letras, Maicito pide y El mercado de Ixtenco.
Maicito pide y El mercado se desbloquean al terminar «Primeras frases».

En iOS sigue la versión anterior, con **dos memoramas**:

- [ ] `GamesHubView.swift`: hoy lista Maicito pide, Memorama y Mercado, más un
      botón "Laberinto, Memorama y Sopa de letras". Hay que dejar una sola
      pantalla como en Android (la de `MaicitoGamesView.swift` con los 5 juegos).
- [ ] Borrar `MemoramaGameView.swift` (el memorama de audio). Se queda el de
      Leo (`MemoryLevelsView` / `MemoryGameView`).
- [ ] Agregar a `MaicitoGamesView.swift` las tarjetas de Maicito pide y El
      mercado de Ixtenco (bloqueadas hasta terminar «Primeras frases»).
- [ ] Memorama por niveles: en Android, al encontrar una pareja se escucha la
      palabra en Yuhmu. En iOS no suena. Las 11 palabras con grabación son agua,
      casa, flor, luna, abeja, caballo, asno, borrego, calabaza, atole y llave
      (audios en `Android/.../res/raw/voc_*.m4a`).

Referencia en Android: `ui/games/levels/LevelGameKit.kt` (`MaicitoGamesScreen`)
y `navigation/Navigation.kt` (ruta `GAMES`).

## 2. Imágenes para el catálogo de juegos

Faltan ilustraciones de Maicito pide y El mercado en el estilo de las de Leo
(`memorama_foto.png`, `laberinto_foto.png`, `sopa_letras_foto.png`). Por ahora
se usan los íconos `ic_maicito_pide` y `ic_mercado`.

- [ ] Guardarlas como `maicito_pide_foto.png` y `mercado_foto.png` (512×512) en
      `Android/.../res/drawable-nodpi/` y en los Assets de iOS.
- [ ] Cambiar `R.drawable.ic_maicito_pide` / `R.drawable.ic_mercado` por las
      nuevas en `MaicitoGamesScreen`.

Prompts (en `prompts-imagenes-juegos.md`).

## 3. Vocabulario del socio formador (86 palabras)

En Android están las 86 palabras con audio y escritura (`YuhmuVocabulary.themes`
y `res/raw/voc_*.m4a`), en 9 temas. Se usan en **Maicito pide**: cada partida
es de un tema distinto (Animales, Comida, Números… o las Primeras frases), las
opciones son del mismo tema y al acertar se muestra cómo se escribe en Yuhmu.

- [ ] iOS todavía no tiene estas palabras: copiar los audios y los temas a
      `MaicitoPideGameView.swift`.
- [ ] Idea: también agregarlas como niveles nuevos del memorama de Leo cuando
      haya ilustraciones de cada palabra.

## 4. Preguntas para el socio formador

- [ ] Escritura en Yuhmu de «¿Cómo te llamas?», «¿Cuántos años tienes?» y
      «Ayúdame por favor». Tenemos el audio, pero el texto no viene en el
      material (ni en el Excel ni en los PDF).
- [ ] Algunas palabras se escriben distinto en el Excel y en el Manual (el
      memorama de Leo usa el Manual). ¿Cuál es la correcta?
      Casa *Ngǘ* / *Ngǚ*, Caballo *Tóge* / *Tó̱ge*, Borrego *Déti* / *Dé̱ti*,
      Flor *Dòni* / *Dò̱ni*, Luna *Zànä* / *Zä̀nä*, Atole *T’ěi* / *T’èi*,
      Asno *’Áxnu* / *’Ǎxnu*.
- [ ] «Hermano» y «Hermana» cambian según quién habla. ¿Qué forma dicen los
      audios de la carpeta "Famila"?
- [ ] «Corazón»: en el Excel viene como `Mu̱ ̀ i` (con un espacio raro).

## 5. Base de datos (Firestore): progreso que iOS no sube

**Android ya lo sube** (27 nov): lecciones 2 a 6 (terminadas, mejor puntaje y
tiempo), niveles de Memorama, Sopa de letras y Laberinto (terminados, mejores
tiempos, monedas cobradas), puntos de juego y logros (comidas, mejor racha,
monedas ganadas). Los campos están en la tabla de
`Android/ZentemiAndroid/ZentemiAndroid/FIREBASE.md`.

En iOS todo eso sigue **solo en el teléfono**:

- [ ] Agregar los mismos campos a `ChildProfile` en `FirestoreModels.swift`
      (opcionales, mismos nombres) y llenarlos en `buildChildProfile` /
      `applyChildProfile` de `AppState.swift`. Si falta un campo en el
      documento, dejar lo que hay en el teléfono.
- [ ] iOS no guarda en `progress/` las lecciones 2 a 6 (solo «Primeras
      frases»), y para abrir la siguiente lección revisa lo guardado en el
      teléfono, no el camino de la nube. Lo que se termine en Android podría
      verse bloqueado en iOS.
- Las reglas de `firestore.rules` no necesitan cambios.

## 6. Otros

- [ ] La música del lobby sigue sonando dentro del memorama por niveles; revisar
      que se escuche bien el audio de las palabras encima de la música.
- [ ] `google-services.json` no está en git: hay que copiarlo a
      `Android/ZentemiAndroid/ZentemiAndroid/app/` para compilar (está en la rama
      `firebase`).
