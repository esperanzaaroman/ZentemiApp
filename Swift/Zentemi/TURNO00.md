# TURNO00 — abierto

## Plan acordado de juegos

- Plan inicial: cinco niveles por juego. Actualización del usuario: **Memorama tendrá 3 niveles**; Laberinto conserva sus 5 niveles.
- El último nivel de cada juego tendrá mayor dificultad.
- Laberinto: niveles 1 a 5 implementados, con dificultad prevista creciente de 1/5 a 5/5. Nivel final y celebración pendientes de prueba del usuario.

## Pantalla de juegos de Maicito

Archivos creados:
- `Zentemi/Views/MaicitoGamesView.swift`: menú de cuatro tarjetas (Memorama, Completa la palabra, Laberinto, Sopa de letras), animación de pulsación, regreso, barra de puntos con objetivo 10,000, fondo como Home. Cada tarjeta admite un nombre de imagen de Assets. Música provisional audiolobby; nombre preparado para sustituirse.
- `TURNO00.md`: este registro de trabajo.

Archivos modificados:
- `Zentemi/Views/HomeView.swift`: el botón Jugar con la mascota abre MaicitoGamesView a pantalla completa.
- `Zentemi/Models/AppState.swift`: puntos de juegos independientes (`gamePoints`), guardados en UserDefaults con clave `zentemi.gamePoints`; carga inicial, objetivo de 10,000 y reinicio junto a los datos de desarrollo.

Sin archivos movidos o eliminados en TURNO00.

Estado inicial del menú: las tarjetas solo producían feedback visual. Actualización: Laberinto abre sus cinco niveles y Memorama su nivel 1 (ver registro abajo). Siguen pendientes los niveles 2 y 3 de Memorama, los otros juegos y la música definitiva.

No se ejecutó, compiló ni probó la app o el simulador. Pruebas a cargo del usuario por petición expresa.

## Imágenes de juegos y cambio de nombre

- Modificado `Zentemi/Views/MaicitoGamesView.swift`: tarjetas conectadas a `memoramaFoto`, `encuentraFoto`, `laberintoFoto` y `sopaLetrasFoto` (respetando mayúsculas de Assets). El segundo juego se llama «Completa la palabra»; sustituida la única referencia del nombre anterior en el código.
- Imágenes agregadas por el usuario en `Zentemi/Assets.xcassets`: `memoramaFoto.imageset`, `encuentraFoto.imageset`, `laberintoFoto.imageset`, `sopaLetrasFoto.imageset`. No se modificaron las imágenes.
- Actualizado este registro. No se crearon nuevos archivos en este cambio ni se ejecutó la aplicación.

## Primeras frases: escucha, repaso y resultados

Archivos creados:
- `Zentemi/PhraseSceneAudioPlayer.swift`: reproductor compartido por las cuatro cinemáticas. Habilita Siguiente únicamente al recibir la finalización correcta del audio; detenerlo o un error no desbloquean el paso. Permite reintentar y muestra errores de reproducción.
- `Zentemi/LessonResultSounds.swift`: tonos provisionales generados en memoria para estrellas, tarjetas y llenado de experiencia; respetan el volumen de efectos. No se añadieron archivos de audio.

Archivos modificados:
- `Zentemi/Views/FirstPhraseSceneView.swift`: bloqueo de Siguiente hasta terminar el audio.
- `Zentemi/Views/SecondPhraseSceneView.swift`: mismo bloqueo; se quitó «Hola», se simplificó la pregunta del nombre y se cambió la respuesta a «Claro, te enseño. Se escucha así…».
- `Zentemi/Views/ThirdPhraseSceneView.swift`: mismo bloqueo y pregunta simplificada sobre cuántos años tienes.
- `Zentemi/Views/FourthPhraseSceneView.swift`: mismo bloqueo.
- `Zentemi/Views/FourthPhraseExerciseView.swift`: después de verificar, la respuesta queda fija aunque sea incorrecta; Siguiente avanza y solo suma acierto si fue correcta. Conserva sonidos de acierto/error y muestra la traducción correcta.
- `Zentemi/Views/FifthPhraseExerciseView.swift`: aviso propio del juego con Maicito, instrucciones y OK; bloquea movimiento e interacción mientras está visible. Guarda la confirmación en `zentemi.hasSeenPhraseGameInstructions` y no vuelve a mostrarlo. También omite el aviso si la lección ya estaba completada antes de este cambio.
- `Zentemi/Views/LessonCompleteView.swift`: sonidos sincronizados con estrellas ganadas y aparición de tarjetas; tono ascendente durante la animación de XP, cuando hay experiencia nueva. Cancela sonidos y callbacks pendientes al salir.
- `TURNO00.md`: actualizado este registro.

Audios existentes utilizados, sin moverlos ni modificarlos: `buenosDias.m4a`, `comoTeLlamas.m4a`, `cuantosAniosTienes.m4a` y `ayudamePorfavor.m4a`, en `Zentemi/Audios`.

Revisión de código únicamente. No se compiló, ejecutó ni probó la aplicación o el simulador. Validación funcional pendiente del usuario.

## Laberinto — Nivel 1

Implementado:
- Flujo Jugar con Maicito → Laberinto → Nivel 1 → juego → resultados → niveles.
- Una entrada y tres salidas con textos tomados del ejercicio 1: «Nzengwa», «Té xki hats’i» (correcta para «Buenos días») y «Ra hyadi».
- Arbustos dibujados en código sobre caminos de tierra; un solo mapa de celdas sirve para dibujo y colisiones. Movimiento con el joystick existente y la mascota personalizada, colisión circular contra muros, pasos pequeños para no atravesarlos.
- Aviso animado y sonido en las salidas falsas; permite seguir buscando desde el interior de esa salida. Al acertar se bloquea el juego, suena el acierto y aparece Siguiente.
- Primera victoria: +300 monedas y +500 puntos en AppState, persistidos globalmente. Las repeticiones entregan 0 y pueden mejorar el récord. El premio se guarda al ganar, no al abrir resultados.
- Resultados: tiempo, mejor tiempo, monedas ganadas y totales, puntos ganados y progreso hacia 10,000; animaciones y sonidos existentes. Sin estrellas ni experiencia.
- Tiempo y movimiento pausados cuando la app no está activa; tareas canceladas al salir. La música provisional del Home se conserva.

Archivos creados:
- `Zentemi/Models/MazeGameSession.swift`: mapa, traducciones, colisiones, movimiento y cronómetro.
- `Zentemi/Views/MazeLevelsView.swift`: selector con Nivel 1, recompensa, récord y repetición; fondo y estilo de botón compartidos del laberinto.
- `Zentemi/Views/MazeBoardView.swift`: tablero de arbustos, caminos, entrada, letreros y mascota.
- `Zentemi/Views/MazeGameView.swift`: juego, control, avisos y conexión de recompensas.
- `Zentemi/Views/MazeResultsView.swift`: resultados animados con sonidos.
- `Zentemi/Assets.xcassets/mazeGrassBackground.imageset/mazeGrassBackground.png`: fondo de pasto generado.
- `Zentemi/Assets.xcassets/mazeGrassBackground.imageset/Contents.json`: registro universal del asset.

Archivos modificados:
- `Zentemi/Models/AppState.swift`: premio único, estado completado y mejor tiempo; carga, guardado y reinicio de datos. Claves nuevas: `zentemi.mazeLevelOneCompleted` y `zentemi.mazeLevelOneBestSeconds`.
- `Zentemi/Views/MaicitoGamesView.swift`: acción de la tarjeta Laberinto para abrir niveles; las otras tarjetas conservan su comportamiento.
- `TURNO00.md`: actualizado este registro.

Recursos reutilizados sin modificarlos: `PetView.swift`, `MaicitoJoystick.swift`, `LessonResultSounds.swift`, `SoundManager.swift`, `audioBien.mp3`, `audioMalaSeleccion.mp3` y `laberintoFoto`.

Imagen creada con la herramienta integrada imagegen, sin CLI/API adicional. Copiada a Assets e inspeccionada como imagen, sin abrir la aplicación.
Prompt exacto:
> Use case: stylized-concept. Asset type: portrait background texture for a children's top-down maze game on iPhone. Primary request: a charming soft green grassy meadow viewed exactly overhead, small scattered tufts of grass, a few tiny daisies and clover concentrated near the edges. Friendly polished hand-painted cartoon game art, gentle daylight, soft mint and spring green palette, quiet low contrast center so a maze will be drawn on top in code. Fill entire image with grass. No maze, no paths, no hedges, no character, no text, no border, no sky, no horizon, no watermark. Portrait orientation.

Revisión estática del código únicamente: no se compiló, abrió, ejecutó ni probó la aplicación o el simulador. El usuario realizará las pruebas de movimiento, colisiones, resultados, persistencia y premios.

## Laberinto — Nivel 2

Implementado:
- «Necesito ayuda», con la respuesta correcta provisional literal `*yuhmu*`, solicitada por el usuario (no es una traducción confirmada).
- Mapa de 15 × 17 celdas, caminos más pequeños, más bifurcaciones y recorridos; cuatro salidas: dos superiores y dos laterales. Muros de piedra dibujados en código y fondo nuevo de tierra/grava.
- Nivel 2 bloqueado hasta completar el Nivel 1. Pantalla de niveles con estado de bloqueo, completado, récord y monedas del mapa pendientes.
- Premio único de primera victoria: 450 monedas y 1,400 puntos globales.
- Cinco monedas identificadas individualmente, de +10 cada una: giro 3D, flotación, sombra, sonido y aviso animado «+10 monedas». Respetan Reducir movimiento y el volumen de efectos.
- Las monedas recogidas en una partida se suman al completar el nivel. Cada moneda se paga una sola vez; las ya cobradas no reaparecen. Las que falten se pueden obtener en repeticiones. Abandonar antes de ganar no cobra ni consume las monedas de esa partida.
- Resultados compartidos: tiempo y récord por nivel, monedas del nivel + recogidas + total global, puntos y sus animaciones/sonidos. Primera victoria con las cinco monedas: 500 monedas y 1,400 puntos.
- Se conserva el mapa, las traducciones, la apariencia y los premios del Nivel 1. La definición de niveles se separó del estado de la partida para compartir movimiento y resultados.

Archivos creados:
- `Zentemi/Models/MazeLevel.swift`: definiciones de ambos mapas, salidas superiores/laterales, monedas y recompensa.
- `Zentemi/Views/MazeCoinView.swift`: moneda giratoria/flotante y aviso de recogida.
- `Zentemi/Assets.xcassets/mazeStoneBackground.imageset/mazeStoneBackground.png`: fondo generado de tierra y grava.
- `Zentemi/Assets.xcassets/mazeStoneBackground.imageset/Contents.json`: configuración del asset.

Archivos modificados:
- `Zentemi/Models/MazeGameSession.swift`: acepta un nivel, detecta salidas laterales y recoge monedas pendientes sin duplicarlas.
- `Zentemi/Models/AppState.swift`: desbloqueo, premio único, récord y monedas cobradas del Nivel 2; persistencia y reinicio. Registra cinco niveles previstos por juego.
- `Zentemi/Views/MazeBoardView.swift`: tablero adaptable, muros de piedra, letreros laterales y monedas/avisos.
- `Zentemi/Views/MazeGameView.swift`: selección del nivel, contador de recogida, sonido y aplicación de recompensas.
- `Zentemi/Views/MazeLevelsView.swift`: tarjetas de ambos niveles y bloqueo del segundo.
- `Zentemi/Views/MazeResultsView.swift`: récord correspondiente y desglose de las monedas recolectadas.
- `Zentemi/LessonResultSounds.swift`: tono breve para recoger monedas, generado en memoria; sin archivos de sonido nuevos.
- `TURNO00.md`: este registro.

Claves nuevas de guardado: `zentemi.mazeLevelTwoCompleted`, `zentemi.mazeLevelTwoBestSeconds`, `zentemi.mazeLevelTwoClaimedCoinMask`.

Fondo creado con imagegen integrado, copiado a Assets e inspeccionado como imagen. Prompt exacto:
> Use case: stylized-concept. Asset type: portrait background for a children's overhead stone maze game. A gentle warm sandy earth and gravel clearing viewed exactly top down, scattered tiny pebbles and very sparse hardy grass tufts around the edges, soft gray and warm ochre palette, polished friendly hand-painted cartoon mobile game illustration. Quiet low contrast open center for paths and stone walls to be drawn on top in code. No walls, no maze, no paths, no characters, no coins, no lettering, no sky, no horizon, no watermark. Portrait composition.

Revisión estática únicamente. No se compiló, ejecutó, abrió ni probó la app o el simulador; pruebas a cargo del usuario.

## Laberinto — Nivel 3

- Mapa vertical de 11 columnas × 33 filas, una entrada y cuatro salidas (dos superiores y dos laterales).
- Vocabulario provisional: `palabraX`, `salidaMal1`, `salidaMal2`, `salidaMal3` y `salidaBuena`.
- Muros de ladrillo dibujados en código sobre las mismas celdas de colisión. Se reutiliza el fondo de tierra/grava existente.
- Cámara vertical automática, limitada a los bordes del mapa. El tamaño de las celdas depende del ancho disponible, no de la altura del mapa: Maicito permanece más grande que en el nivel 2. Encabezado y joystick permanecen fijos.
- Diez monedas de +10, con las animaciones, sonido y aviso de recogida existentes. Cobro por moneda una sola vez al completar; se pueden recuperar las pendientes en repeticiones.
- Premio único: 600 monedas y 2,000 puntos; hasta 700 monedas con las diez del mapa. Persistencia global, récord propio y misma pantalla de resultados.
- Nivel bloqueado hasta completar el nivel 2. Los mapas y premios anteriores se conservan.

Archivos modificados:
- `Zentemi/Models/MazeLevel.swift`: definición del tercer nivel y opciones de ladrillos/cámara vertical.
- `Zentemi/Models/AppState.swift`: premio, récord, monedas cobradas, carga y reinicio del tercer nivel; consulta del registro de monedas por nivel.
- `Zentemi/Views/MazeBoardView.swift`: cámara vertical, recorte al área del tablero y dibujo de ladrillos.
- `Zentemi/Views/MazeGameView.swift`: indicación de exploración vertical.
- `Zentemi/Views/MazeLevelsView.swift`: tarjeta del nivel 3, bloqueo por nivel anterior y selección de monedas pendientes.
- `Zentemi/Views/MazeResultsView.swift`: desglose de monedas también para el tercer nivel.
- `TURNO00.md`: actualizado el registro.

No se crearon, movieron ni eliminaron archivos en este cambio. No se añadieron imágenes ni audios.
Claves nuevas: `zentemi.mazeLevelThreeCompleted`, `zentemi.mazeLevelThreeBestSeconds`, `zentemi.mazeLevelThreeClaimedCoinMask`.

Revisión de código únicamente. No se compiló, abrió, ejecutó ni probó la aplicación o el simulador. Queda pendiente la prueba del usuario.

## Laberinto — Nivel 4 de hielo

- Mapa nuevo de 11 × 41 celdas, dificultad prevista 4/5, inicio central en una bifurcación con tres direcciones.
- Cinco salidas: dos arriba, una a la izquierda, una a la derecha y una abajo. El recorrido correcto hacia la salida inferior exige primero subir y luego descender. Incluye cruces, bucles y callejones; no repite los módulos del nivel 3.
- Vocabulario provisional: `palabraHielo`, `salidaMal1` a `salidaMal4` y `salidaBuena`.
- Muros de hielo con grietas, reflejos y nieve, caminos claros y nuevo fondo nevado. Cámara vertical y marcador del punto de inicio central. Se conserva el control de movimiento existente.
- Se desbloquea al completar el nivel 3; los niveles anteriores mantienen sus mapas y premios.
- Recompensa de primera victoria: 800 monedas y 2,800 puntos. Quince monedas adicionales de +10: hasta 950 monedas en total.
- Cada moneda se paga solo una vez al completar; las pendientes siguen disponibles en repeticiones. Se mantienen animaciones, sonidos, tiempo, récord y pantalla de resultados compartida.

Archivos creados:
- `Zentemi/Assets.xcassets/mazeIceBackground.imageset/mazeIceBackground.png`.
- `Zentemi/Assets.xcassets/mazeIceBackground.imageset/Contents.json`.

Archivos modificados:
- `Zentemi/Models/MazeLevel.swift`: mapa del nivel 4, hielo, inicio interior y salidas inferiores.
- `Zentemi/Models/MazeGameSession.swift`: detección de salida inferior.
- `Zentemi/Models/AppState.swift`: recompensa, récord y monedas cobradas del nivel 4, persistencia y reinicio.
- `Zentemi/Views/MazeBoardView.swift`: hielo, marcador central y letrero inferior con margen para que la cámara lo muestre completo.
- `Zentemi/Views/MazeLevelsView.swift`: tarjeta del cuarto nivel y bloqueo hasta completar el tercero.
- `Zentemi/Views/MazeGameView.swift`: instrucciones para explorar arriba y abajo desde el centro.
- `TURNO00.md`: registro actualizado.

Claves nuevas: `zentemi.mazeLevelFourCompleted`, `zentemi.mazeLevelFourBestSeconds` y `zentemi.mazeLevelFourClaimedCoinMask`.
No se añadieron audios ni se movieron o eliminaron archivos.

Fondo creado con imagegen integrado y copiado a Assets. Prompt exacto:
> Use case: stylized-concept. Asset type: portrait background for a friendly children's top-down ice maze game. Exactly overhead frozen snowy ground, pale icy blue and white, subtle frost crystals and small snowdrifts at the edges, gentle soft daylight, polished hand-painted cartoon mobile game illustration. Quiet low contrast open center for maze paths drawn on top in code. No walls, no maze, no paths, no characters, no coins, no lettering, no sky, no horizon, no watermark. Portrait composition.

Revisión estática del código y de los datos del mapa únicamente. No se compiló, ejecutó, abrió ni probó la aplicación o el simulador. Pruebas de juego a cargo del usuario.

## Laberinto — Nivel 5 de lava y final del juego

- Mapa vertical nuevo de 11 × 61 celdas, dificultad prevista 5/5, inicio central y seis salidas: dos superiores, dos inferiores y una a cada lado. Una sola correcta; recorrido con ramificaciones y callejones. Cámara vertical mantiene el tamaño de Maicito. Los mapas 1 a 4 no cambian.
- Vocabulario provisional: `palabraLava`, `salidaMal1` a `salidaMal5`, `salidaBuena`.
- Cubos de lava con corteza oscura y grietas naranja/amarillo dibujados dentro de las celdas sólidas; caminos diferenciados y fondo volcánico nuevo.
- Se desbloquea después del nivel 4. Primera victoria: 1,000 monedas + 3,500 puntos. Veinte monedas del mapa de +10, giratorias/flotantes, con sonido y aviso existentes: hasta 1,200 monedas en total. Cada moneda se cobra una vez al completar; las pendientes pueden recuperarse en repeticiones.
- Persistencia de victoria, récord y monedas cobradas. Se mantiene la pantalla compartida de resultados con tiempo, monedas, puntos, sonidos y animaciones. Los puntos reales se siguen acumulando aunque superen el objetivo de 10,000.
- Primera victoria del nivel 5: después de «Siguiente», celebración con la portada original `laberintoFoto`, cuatro pulsos de suspenso, transición a verde, «¡Completado!», sonidos y respuesta háptica. Después permite ver resultados. No repite premios ni celebración en victorias posteriores.
- Tarjeta de Laberinto del menú con fondo/borde verde y distintivo de completado persistente; sigue permitiendo entrar y repetir niveles.
- La celebración respeta Reducir movimiento, detiene sonidos fuera de pantalla y continúa su secuencia al regresar del segundo plano. La vibración depende del dispositivo.

Archivos creados:
- `Zentemi/Views/MazeCompletionCelebrationView.swift`.
- `Zentemi/Assets.xcassets/mazeLavaBackground.imageset/mazeLavaBackground.png`.
- `Zentemi/Assets.xcassets/mazeLavaBackground.imageset/Contents.json`.

Archivos modificados:
- `Zentemi/Models/MazeLevel.swift`: definición del quinto mapa y tema de lava.
- `Zentemi/Models/AppState.swift`: recompensas, bloqueo, récord, persistencia y reinicio del nivel 5.
- `Zentemi/Views/MazeBoardView.swift`: cubos de lava y color de caminos.
- `Zentemi/Views/MazeLevelsView.swift`: tarjeta y selección del nivel 5.
- `Zentemi/Views/MazeGameView.swift`: celebración de primera victoria y número dinámico de salidas en instrucciones.
- `Zentemi/Views/MaicitoGamesView.swift`: apariencia verde y distintivo persistente de Laberinto completado.
- `Zentemi/LessonResultSounds.swift`: tonos de suspenso y acorde final generados en memoria.
- `TURNO00.md`: registro actualizado.

Claves nuevas: `zentemi.mazeLevelFiveCompleted`, `zentemi.mazeLevelFiveBestSeconds`, `zentemi.mazeLevelFiveClaimedCoinMask` (20 bits).
No se añadieron archivos de audio ni se movieron o eliminaron archivos existentes.

Fondo generado con imagegen integrado, inspeccionado y copiado a `Zentemi/Assets.xcassets/mazeLavaBackground.imageset/mazeLavaBackground.png`. Prompt exacto:
> Use case: stylized-concept. Asset type: portrait background for a friendly children's mobile maze game. Top-down volcanic stone ground, soft rounded charcoal basalt stones and sparse warm orange lava glow near the outer edges. Quiet dark warm gray center to place a maze drawn separately in code. Polished colorful cartoon game illustration, inviting adventure, soft lighting. No maze, no paths, no characters, no text, no UI, no watermark. Portrait composition.

Revisión estática de código y datos del mapa únicamente. No se compiló, ejecutó, abrió ni probó la aplicación o el simulador. Las pruebas de juego, dificultad, animaciones, sonidos y vibración quedan a cargo del usuario.

## Memorama — Nivel 1

- Menú propio de tres niveles: primero jugable; segundo y tercero muestran Próximamente y requisito del anterior, sin iniciar juegos aún no implementados.
- Cinco pares, diez tarjetas mezcladas por partida. Una imagen y una tarjeta con Yuhmu destacado y español debajo por pareja.
- Vocabulario cotejado visualmente con el Manual para la escritura de los sonidos del yuhmu (Yonca Gaspar y Alarcón Montero, 2024), pp. 74 y 80: agua (Déhe), casa (Ngǚ), flor (Dò̱ni), luna (Zä̀nä), árbol (Zä̌). Casa/flor sin artículo Rà. Se conserva la grafía del manual, que presenta signos omitidos en la extracción de Zentemi4.
- Sonido al revelar tarjetas, acierto y error suave. Los errores quedan visibles 1.5 segundos; durante ese intervalo no se permite seleccionar otras tarjetas. Los pares correctos quedan descubiertos con marca verde.
- Sin límite de tiempo ni penalización. El reloj y el intervalo de errores se pausan cuando la escena no está activa. Finaliza al encontrar los cinco pares.
- Primera victoria: 300 monedas globales y 500 puntos de juego. Repeticiones sin duplicar premios, con mejor tiempo guardado.
- Resultados propios con mascota, tiempo/récord, monedas ganadas y totales, puntos y barra hacia 10,000; animaciones y sonidos existentes. No concede XP ni marca lecciones como completadas.
- Se mantiene la música del Home.

Archivos creados:
- `Zentemi/Models/MemoryGameSession.swift`: vocabulario, tarjetas, mezcla, selección, parejas y tiempo.
- `Zentemi/Views/MemoryLevelsView.swift`: selección de niveles.
- `Zentemi/Views/MemoryGameView.swift`: tablero, tarjetas, sonidos y finalización.
- `Zentemi/Views/MemoryResultsView.swift`: resultados y animaciones.
- `Zentemi/Assets.xcassets/memoryWater.imageset/memoryWater.png` y `Zentemi/Assets.xcassets/memoryWater.imageset/Contents.json`.
- `Zentemi/Assets.xcassets/memoryHouse.imageset/memoryHouse.png` y `Zentemi/Assets.xcassets/memoryHouse.imageset/Contents.json`.
- `Zentemi/Assets.xcassets/memoryFlower.imageset/memoryFlower.png` y `Zentemi/Assets.xcassets/memoryFlower.imageset/Contents.json`.
- `Zentemi/Assets.xcassets/memoryMoon.imageset/memoryMoon.png` y `Zentemi/Assets.xcassets/memoryMoon.imageset/Contents.json`.
- `Zentemi/Assets.xcassets/memoryTree.imageset/memoryTree.png` y `Zentemi/Assets.xcassets/memoryTree.imageset/Contents.json`.

Archivos modificados:
- `Zentemi/Models/AppState.swift`: tres niveles previstos de Memorama, premio único, persistencia de completado/récord y reinicio.
- `Zentemi/Views/MaicitoGamesView.swift`: la tarjeta Memorama abre sus niveles.
- `TURNO00.md`: este registro.

Claves nuevas: `zentemi.memoryLevelOneCompleted`, `zentemi.memoryLevelOneBestSeconds`.
No se movieron ni eliminaron archivos existentes; no se añadieron archivos de audio.
Imágenes creadas con imagegen integrado, inspeccionadas y copiadas a las rutas Assets anteriores. Prompts exactos:

**memoryWater**
> Use case: stylized-concept. Asset type: square vocabulary illustration for a children's matching card game. Subject: a large clear blue water droplet above a small water ripple. Friendly polished cartoon illustration, rounded shapes, clean silhouette, soft dimensional shading, vivid colors. Single centered subject filling 75 percent of frame with generous margin, plain white background. No words, no letters, no numbers, no border, no faces, no watermark.

**memoryHouse**
> Use case: stylized-concept. Asset type: square vocabulary illustration for a children's matching card game. Subject: a simple cozy small house with terracotta roof, cream walls, one door and two windows. Friendly polished cartoon illustration, rounded shapes, clean silhouette, soft dimensional shading, vivid colors. Single centered subject filling 75 percent of frame with generous margin, plain white background. No words, no letters, no numbers, no border, no faces, no watermark.

**memoryFlower**
> Use case: stylized-concept. Asset type: square vocabulary illustration for a children's matching card game. Subject: one bright pink flower with yellow center, green stem and two leaves. Friendly polished cartoon illustration, rounded shapes, clean silhouette, soft dimensional shading, vivid colors. Single centered subject filling 75 percent of frame with generous margin, plain white background. No words, no letters, no numbers, no border, no faces, no watermark.

**memoryMoon**
> Use case: stylized-concept. Asset type: square vocabulary illustration for a children's matching card game. Subject: one golden crescent moon with subtle surface craters. Friendly polished cartoon illustration, rounded shapes, clean silhouette, soft dimensional shading, vivid colors. Single centered subject filling 75 percent of frame with generous margin, plain white background. No words, no letters, no numbers, no border, no faces, no watermark.

**memoryTree**
> Use case: stylized-concept. Asset type: square vocabulary illustration for a children's matching card game. Subject: one leafy green tree with rounded canopy and brown trunk. Friendly polished cartoon illustration, rounded shapes, clean silhouette, soft dimensional shading, vivid colors. Single centered subject filling 75 percent of frame with generous margin, plain white background. No words, no letters, no numbers, no border, no faces, no watermark.

Revisión estática únicamente. No se compiló, ejecutó, abrió ni probó la app o el simulador. Pendiente la prueba del usuario.

## Memorama — Tarjetas con relieve

- Tarjetas de imágenes y palabras con esquinas más redondeadas, degradado suave, borde iluminado, base inferior visible y sombra para dar volumen.
- Efecto de botón: la cara desciende hacia la base al presionar. Pares encontrados mantienen el borde/base verde.
- Modificados: `Zentemi/Views/MemoryGameView.swift` y `TURNO00.md`.
- No se crearon archivos ni se cambiaron reglas, vocabulario o recompensas.
- Revisión estática; no se compiló ni ejecutó la aplicación o el simulador.

## Memorama — Niveles 2 y 3 y celebración final

- Nivel 2: 7 pares de animales; 450 monedas y 1,400 puntos.
- Nivel 3: 10 pares de vocabulario cotidiano; 700 monedas y 2,500 puntos.
- Vocabulario consultado en el Manual para la escritura de los sonidos del yuhmu, páginas 74, 75 y 80. Imágenes nuevas para cada palabra; tarjetas con relieve conservadas.
- Desbloqueo secuencial, premios solo en la primera victoria y mejor tiempo guardado por nivel. Resultados con sonidos.
- Primera victoria del nivel 3: celebración con portada, suspenso, transición a verde, sonidos y vibración. Tarjeta Memorama verde permanentemente y niveles repetibles.

### Archivos creados

- `Zentemi/Models/MemoryLevel.swift`
- `Zentemi/Views/MemoryCompletionCelebrationView.swift`
- `Zentemi/Assets.xcassets/memoryBee.imageset/memoryBee.png` y su `Contents.json`.
- `Zentemi/Assets.xcassets/memorySpider.imageset/memorySpider.png` y su `Contents.json`.
- `Zentemi/Assets.xcassets/memoryEagle.imageset/memoryEagle.png` y su `Contents.json`.
- `Zentemi/Assets.xcassets/memoryHare.imageset/memoryHare.png` y su `Contents.json`.
- `Zentemi/Assets.xcassets/memoryHorse.imageset/memoryHorse.png` y su `Contents.json`.
- `Zentemi/Assets.xcassets/memoryDonkey.imageset/memoryDonkey.png` y su `Contents.json`.
- `Zentemi/Assets.xcassets/memorySheep.imageset/memorySheep.png` y su `Contents.json`.
- `Zentemi/Assets.xcassets/memoryPillow.imageset/memoryPillow.png` y su `Contents.json`.
- `Zentemi/Assets.xcassets/memoryRing.imageset/memoryRing.png` y su `Contents.json`.
- `Zentemi/Assets.xcassets/memoryEarring.imageset/memoryEarring.png` y su `Contents.json`.
- `Zentemi/Assets.xcassets/memoryPumpkin.imageset/memoryPumpkin.png` y su `Contents.json`.
- `Zentemi/Assets.xcassets/memorySweetPotato.imageset/memorySweetPotato.png` y su `Contents.json`.
- `Zentemi/Assets.xcassets/memoryAtole.imageset/memoryAtole.png` y su `Contents.json`.
- `Zentemi/Assets.xcassets/memoryStar.imageset/memoryStar.png` y su `Contents.json`.
- `Zentemi/Assets.xcassets/memoryBroom.imageset/memoryBroom.png` y su `Contents.json`.
- `Zentemi/Assets.xcassets/memoryKey.imageset/memoryKey.png` y su `Contents.json`.
- `Zentemi/Assets.xcassets/memoryRainbow.imageset/memoryRainbow.png` y su `Contents.json`.

### Archivos modificados

- `Zentemi/Models/AppState.swift`
- `Zentemi/Models/MemoryGameSession.swift`
- `Zentemi/Views/MemoryLevelsView.swift`
- `Zentemi/Views/MemoryGameView.swift`
- `Zentemi/Views/MemoryResultsView.swift`
- `Zentemi/Views/MaicitoGamesView.swift`
- `TURNO00.md`

Nuevas claves persistentes: `zentemi.memoryLevelTwoCompleted`, `zentemi.memoryLevelTwoBestSeconds`, `zentemi.memoryLevelThreeCompleted` y `zentemi.memoryLevelThreeBestSeconds`. Se conservan las del nivel 1.

### Prompts de las ilustraciones

**memoryBee**
> Use case: stylized-concept. Asset type: square vocabulary illustration for a children's matching card game. Subject: one friendly honey bee with yellow and black stripes and translucent wings. Friendly polished cartoon illustration, rounded shapes, clean silhouette, soft dimensional shading, vivid colors. Single centered subject filling 75 percent of frame with generous margin, plain white background. No words, no letters, no numbers, no border, no watermark. No extra objects or scenery.

Destino: `Zentemi/Assets.xcassets/memoryBee.imageset/memoryBee.png`.

**memorySpider**
> Use case: stylized-concept. Asset type: square vocabulary illustration for a children's matching card game. Subject: one small friendly spider with eight clearly visible legs. Friendly polished cartoon illustration, rounded shapes, clean silhouette, soft dimensional shading, vivid colors. Single centered subject filling 75 percent of frame with generous margin, plain white background. No words, no letters, no numbers, no border, no watermark. No extra objects or scenery.

Destino: `Zentemi/Assets.xcassets/memorySpider.imageset/memorySpider.png`.

**memoryEagle**
> Use case: stylized-concept. Asset type: square vocabulary illustration for a children's matching card game. Subject: one brown eagle with hooked beak and broad folded wings, perched standing. Friendly polished cartoon illustration, rounded shapes, clean silhouette, soft dimensional shading, vivid colors. Single centered subject filling 75 percent of frame with generous margin, plain white background. No words, no letters, no numbers, no border, no watermark. No extra objects or scenery.

Destino: `Zentemi/Assets.xcassets/memoryEagle.imageset/memoryEagle.png`.

**memoryHare**
> Use case: stylized-concept. Asset type: square vocabulary illustration for a children's matching card game. Subject: one tan hare with long ears and long hind legs, sitting. Friendly polished cartoon illustration, rounded shapes, clean silhouette, soft dimensional shading, vivid colors. Single centered subject filling 75 percent of frame with generous margin, plain white background. No words, no letters, no numbers, no border, no watermark. No extra objects or scenery.

Destino: `Zentemi/Assets.xcassets/memoryHare.imageset/memoryHare.png`.

**memoryHorse**
> Use case: stylized-concept. Asset type: square vocabulary illustration for a children's matching card game. Subject: one chestnut horse standing in side three-quarter view. Friendly polished cartoon illustration, rounded shapes, clean silhouette, soft dimensional shading, vivid colors. Single centered subject filling 75 percent of frame with generous margin, plain white background. No words, no letters, no numbers, no border, no watermark. No extra objects or scenery.

Destino: `Zentemi/Assets.xcassets/memoryHorse.imageset/memoryHorse.png`.

**memoryDonkey**
> Use case: stylized-concept. Asset type: square vocabulary illustration for a children's matching card game. Subject: one gray donkey with long ears, short mane and tufted tail. Friendly polished cartoon illustration, rounded shapes, clean silhouette, soft dimensional shading, vivid colors. Single centered subject filling 75 percent of frame with generous margin, plain white background. No words, no letters, no numbers, no border, no watermark. No extra objects or scenery.

Destino: `Zentemi/Assets.xcassets/memoryDonkey.imageset/memoryDonkey.png`.

**memorySheep**
> Use case: stylized-concept. Asset type: square vocabulary illustration for a children's matching card game. Subject: one fluffy white sheep with woolly body and dark hooves. Friendly polished cartoon illustration, rounded shapes, clean silhouette, soft dimensional shading, vivid colors. Single centered subject filling 75 percent of frame with generous margin, plain white background. No words, no letters, no numbers, no border, no watermark. No extra objects or scenery.

Destino: `Zentemi/Assets.xcassets/memorySheep.imageset/memorySheep.png`.

**memoryPillow**
> Use case: stylized-concept. Asset type: square vocabulary illustration for a children's matching card game. Subject: one soft plump light blue bed pillow. Friendly polished cartoon illustration, rounded shapes, clean silhouette, soft dimensional shading, vivid colors. Single centered subject filling 75 percent of frame with generous margin, plain white background. No words, no letters, no numbers, no border, no watermark. No extra objects or scenery.

Destino: `Zentemi/Assets.xcassets/memoryPillow.imageset/memoryPillow.png`.

**memoryRing**
> Use case: stylized-concept. Asset type: square vocabulary illustration for a children's matching card game. Subject: one gold finger ring with a small blue gemstone. Friendly polished cartoon illustration, rounded shapes, clean silhouette, soft dimensional shading, vivid colors. Single centered subject filling 75 percent of frame with generous margin, plain white background. No words, no letters, no numbers, no border, no watermark. No extra objects or scenery.

Destino: `Zentemi/Assets.xcassets/memoryRing.imageset/memoryRing.png`.

**memoryEarring**
> Use case: stylized-concept. Asset type: square vocabulary illustration for a children's matching card game. Subject: one gold dangling earring with a pink bead and visible ear hook. Friendly polished cartoon illustration, rounded shapes, clean silhouette, soft dimensional shading, vivid colors. Single centered subject filling 75 percent of frame with generous margin, plain white background. No words, no letters, no numbers, no border, no watermark. No extra objects or scenery.

Destino: `Zentemi/Assets.xcassets/memoryEarring.imageset/memoryEarring.png`.

**memoryPumpkin**
> Use case: stylized-concept. Asset type: square vocabulary illustration for a children's matching card game. Subject: one round orange pumpkin with green stem. Friendly polished cartoon illustration, rounded shapes, clean silhouette, soft dimensional shading, vivid colors. Single centered subject filling 75 percent of frame with generous margin, plain white background. No words, no letters, no numbers, no border, no watermark. No extra objects or scenery.

Destino: `Zentemi/Assets.xcassets/memoryPumpkin.imageset/memoryPumpkin.png`.

**memorySweetPotato**
> Use case: stylized-concept. Asset type: square vocabulary illustration for a children's matching card game. Subject: one purple-skinned sweet potato with a cut half showing orange flesh. Friendly polished cartoon illustration, rounded shapes, clean silhouette, soft dimensional shading, vivid colors. Single centered subject filling 75 percent of frame with generous margin, plain white background. No words, no letters, no numbers, no border, no watermark. No extra objects or scenery.

Destino: `Zentemi/Assets.xcassets/memorySweetPotato.imageset/memorySweetPotato.png`.

**memoryAtole**
> Use case: stylized-concept. Asset type: square vocabulary illustration for a children's matching card game. Subject: one Mexican clay mug filled with warm creamy beige atole, a soft wisp of steam. Friendly polished cartoon illustration, rounded shapes, clean silhouette, soft dimensional shading, vivid colors. Single centered subject filling 75 percent of frame with generous margin, plain white background. No words, no letters, no numbers, no border, no watermark. No extra objects or scenery.

Destino: `Zentemi/Assets.xcassets/memoryAtole.imageset/memoryAtole.png`.

**memoryStar**
> Use case: stylized-concept. Asset type: square vocabulary illustration for a children's matching card game. Subject: one golden five-pointed star with soft glow. Friendly polished cartoon illustration, rounded shapes, clean silhouette, soft dimensional shading, vivid colors. Single centered subject filling 75 percent of frame with generous margin, plain white background. No words, no letters, no numbers, no border, no watermark. No extra objects or scenery.

Destino: `Zentemi/Assets.xcassets/memoryStar.imageset/memoryStar.png`.

**memoryBroom**
> Use case: stylized-concept. Asset type: square vocabulary illustration for a children's matching card game. Subject: one traditional straw broom with wooden handle. Friendly polished cartoon illustration, rounded shapes, clean silhouette, soft dimensional shading, vivid colors. Single centered subject filling 75 percent of frame with generous margin, plain white background. No words, no letters, no numbers, no border, no watermark. No extra objects or scenery.

Destino: `Zentemi/Assets.xcassets/memoryBroom.imageset/memoryBroom.png`.

**memoryKey**
> Use case: stylized-concept. Asset type: square vocabulary illustration for a children's matching card game. Subject: one simple golden door key with round bow and distinct teeth. Friendly polished cartoon illustration, rounded shapes, clean silhouette, soft dimensional shading, vivid colors. Single centered subject filling 75 percent of frame with generous margin, plain white background. No words, no letters, no numbers, no border, no watermark. No extra objects or scenery.

Destino: `Zentemi/Assets.xcassets/memoryKey.imageset/memoryKey.png`.

**memoryRainbow**
> Use case: stylized-concept. Asset type: square vocabulary illustration for a children's matching card game. Subject: one bright rainbow arch with two small white clouds at its ends. Friendly polished cartoon illustration, rounded shapes, clean silhouette, soft dimensional shading, vivid colors. Single centered subject filling 75 percent of frame with generous margin, plain white background. No words, no letters, no numbers, no border, no watermark. No extra objects or scenery.

Destino: `Zentemi/Assets.xcassets/memoryRainbow.imageset/memoryRainbow.png`.

Revisión estática únicamente. No se compiló, ejecutó, abrió ni probó la app o el simulador. Pendiente la prueba del usuario.

## Sopa de letras — Nivel 1

- Acceso desde Jugar con Maicito, pantalla de nivel 1 y tablero de 6 × 6.
- Tres palabras del vocabulario ya revisado: Déhe (agua), Dò̱ni (flor) y Zä̀nä (luna), con español debajo. Se conservan las marcas de cada vocal como una sola casilla.
- Arrastre horizontal o vertical, selección resaltada, validación al soltar, resaltado permanente de aciertos y contador de 0 a 3. Selección alternativa mediante acciones de accesibilidad de primera y última letra.
- Sonidos de selección, acierto y error. Los intentos incorrectos permiten continuar y las palabras repetidas no aumentan el contador.
- Primera victoria: 300 monedas globales y 500 puntos de juego. Repeticiones sin premio adicional, con registro del mejor tiempo.
- Resultados con mascota, tiempo, monedas, puntos, animaciones y sonidos. Reloj pausado al salir de la app.
- Persistencia: claves `zentemi.wordSearchLevelOneCompleted` y `zentemi.wordSearchLevelOneBestSeconds`, incluidas en carga y reinicio de desarrollo.

Archivos creados:
- `Zentemi/Models/WordSearchSession.swift`
- `Zentemi/Views/WordSearchLevelsView.swift`
- `Zentemi/Views/WordSearchGameView.swift`
- `Zentemi/Views/WordSearchResultsView.swift`

Archivos modificados:
- `Zentemi/Models/AppState.swift`
- `Zentemi/Views/MaicitoGamesView.swift`
- `TURNO00.md`

Se reutilizan la portada sopaLetrasFoto, la mascota y los sonidos existentes. No se agregaron imágenes ni audios.
Revisión de código y formato únicamente; no se compiló, abrió, ejecutó ni probó la app o el simulador. Pruebas funcionales pendientes del usuario.

## Sopa de letras — Niveles 2 y 3 (25 de noviembre)

- Nivel 2: 5 palabras, tablero 8 × 8 horizontal/vertical, 450 monedas y 1,400 puntos.
- Nivel 3: 7 palabras, tablero 10 × 10 con diagonales y palabras al revés, 700 monedas y 2,500 puntos.
- Vocabulario diferente por nivel, reutilizando las grafías revisadas del manual en MemoryLevel: nivel 2 abeja, araña, águila, caballo y borrego; nivel 3 almohada, anillo, llave, atole, calabaza, asno y arcoíris.
- Desbloqueo secuencial; premios únicos por nivel, mejores tiempos persistentes y resultados con animaciones/sonidos.
- Primera victoria del nivel 3: celebración con portada, suspenso, transición a verde, sonidos y vibración. La tarjeta Sopa de letras queda verde y permite repetir los niveles.
- Conservados el progreso y las claves del nivel 1. Nuevas claves zentemi.wordSearchLevelTwoCompleted, wordSearchLevelTwoBestSeconds, wordSearchLevelThreeCompleted y wordSearchLevelThreeBestSeconds (todas con prefijo zentemi.).

Creado:
- `Zentemi/Models/WordSearchLevel.swift`
- `Zentemi/Views/WordSearchCompletionCelebrationView.swift`

Modificado:
- `Zentemi/Models/WordSearchSession.swift`
- `Zentemi/Models/AppState.swift`
- `Zentemi/Views/WordSearchLevelsView.swift`
- `Zentemi/Views/WordSearchGameView.swift`
- `Zentemi/Views/WordSearchResultsView.swift`
- `Zentemi/Views/MaicitoGamesView.swift`
- `TURNO00.md`

Sin imágenes ni audios nuevos. Revisión estática del código y formato; no se compiló ni ejecutó o probó la app/simulador. Pruebas funcionales pendientes del usuario.

### Corrección: Llave en sopa de letras, nivel 3

Registro posterior del menú: se retiró la tarjeta «Completa la palabra» de Jugar con Maicito. Memorama ocupa la fila superior; Laberinto y Sopa de letras comparten la inferior. Se mantienen sus niveles, progreso y accesos. Archivos modificados: `Zentemi/Views/MaicitoGamesView.swift` y `TURNO00.md`. Sin archivos nuevos. No se compiló ni ejecutó la app/simulador.

- Ntshóki tiene siete caracteres, pero su recorrido tenía seis casillas; faltaba la I final.
- Se amplió el recorrido una casilla hacia arriba (columna derecha, filas 2 a 8), sin invadir la palabra Arcoíris.
- Modificados: `Zentemi/Models/WordSearchLevel.swift` y `TURNO00.md`. Sin archivos nuevos.
- No se compiló ni ejecutó o probó la app/simulador.

## Disminución de felicidad por tiempo

- Un punto cada dos horas, máximo diez por período fijo de 24 horas contado desde la activación de esta función; mínimo cero.
- Se calcula el tiempo con la app cerrada al iniciar o regresar. Mientras está activa, revisión cada 30 segundos. Se liquida el tiempo pendiente antes de alimentar.
- Primera activación sin descuento retroactivo. Conserva las fracciones de tiempo entre aperturas; no acumula deuda cuando la felicidad llega a cero. Si el reloj retrocede, espera a alcanzar la última fecha registrada.
- Persistencia en `zentemi.happinessDecayOrigin` y `zentemi.happinessDecayLastUpdate`; el reinicio de desarrollo elimina ambas junto con las demás claves Zentemi.
- Modificados: `Zentemi/Models/AppState.swift`, `Zentemi/ContentView.swift`, `Zentemi/Views/FeedMaicitoView.swift` y `TURNO00.md`. Sin archivos nuevos.
- Revisión estática; no se compiló, ejecutó ni probó la app o el simulador.

## Familia — Segunda lección del Camino

- Cinco ejercicios, cada uno precedido por una tarjeta breve de aprendizaje con Maicito: observa y elige, une parejas, completa Zíkü, recuerda quién habla y coloca etiquetas en escenas (arrastre o selección por toques).
- Tres hermanos ilustrados: Ana, Leo y Luz. Vocabulario cotejado visualmente en página 79 del Manual para la escritura de los sonidos del yuhmu: Dó̱t’e (hermano), Zíkü (hermana, dicho por mujer) y Nkhǘ (hermana, dicho por hombre). Se conservan diacríticos. Se enseñan entradas de vocabulario, no se inventan formas posesivas.
- Cada ejercicio se verifica una sola vez; después se bloquea la respuesta y se explica la solución. Los ejercicios de parejas cuentan como un acierto cuando todas sus parejas son correctas. Total: cinco aciertos posibles.
- Tiempo activo de la lección incluyendo aprendizaje; pausa en segundo plano. Salir antes de terminar no concede premios.
- Reutiliza LessonCompleteView con estrellas, aciertos, tiempo, monedas, experiencia y sus sonidos/animaciones. Familia entrega 900 monedas y 180 XP una sola vez; el premio se guarda al finalizar y resultados solo lo representa.
- Familia requiere haber completado Primeras frases. Terminarla actualiza el progreso a 2/7 y la siguiente lección se indica como Próximamente, sin abrir contenido aún inexistente.
- Guarda familyRewardClaimed, familyBestCorrectAnswers y familyBestSeconds con prefijo zentemi.; carga y reinicio de desarrollo incluidos.

Archivos creados:
- `Zentemi/Models/FamilyLessonSession.swift`
- `Zentemi/Views/FamilyLessonView.swift`
- `Zentemi/Assets.xcassets/familyAna.imageset/familyAna.png` y su `Contents.json`.
- `Zentemi/Assets.xcassets/familyLeo.imageset/familyLeo.png` y su `Contents.json`.
- `Zentemi/Assets.xcassets/familyLuz.imageset/familyLuz.png` y su `Contents.json`.

Archivos modificados:
- `Zentemi/Models/AppState.swift`
- `Zentemi/Views/LessonIntroView.swift`
- `Zentemi/Views/LessonCompleteView.swift`
- `Zentemi/Views/LearningPathView.swift`
- `TURNO00.md`

### Ilustraciones generadas
Herramienta incorporada image_gen (skill imagegen), sin CLI. Destinos finales en Assets arriba.

**familyAna**
> Use case: illustration-story. Asset type: character portrait for a Yuhmu language learning app for children and teens. Subject: a cheerful girl aged about 12, warm medium brown skin, dark hair in a single long braid, purple T-shirt and jeans, smiling and waving one hand. Polished friendly rounded cartoon, soft dimensional shading, clean outlines, vibrant pastel colors matching a purple and turquoise educational interface. Waist-up centered composition with generous margin, plain pale lavender background. One character only. No words, no letters, no logos, no watermark.

**familyLeo**
> Use case: illustration-story. Asset type: character portrait for a Yuhmu language learning app for children and teens. Subject: a cheerful boy aged about 11, warm medium brown skin, short dark hair, teal T-shirt and jeans, smiling and waving one hand. Polished friendly rounded cartoon, soft dimensional shading, clean outlines, vibrant pastel colors matching a purple and turquoise educational interface. Waist-up centered composition with generous margin, plain pale lavender background. One character only. No words, no letters, no logos, no watermark.

**familyLuz**
> Use case: illustration-story. Asset type: character portrait for a Yuhmu language learning app for children and teens. Subject: a cheerful girl aged about 9, warm medium brown skin, dark bob haircut, yellow T-shirt and jeans, smiling and waving one hand. Polished friendly rounded cartoon, soft dimensional shading, clean outlines, vibrant pastel colors matching a purple and turquoise educational interface. Waist-up centered composition with generous margin, plain pale lavender background. One character only. No words, no letters, no logos, no watermark.

Revisión estática de fuentes, referencias y formato. No se compiló, abrió, ejecutó ni probó la app o el simulador. Las pruebas funcionales corresponden al usuario.

### Revisión de Familia: variedad y minijuego (25 de noviembre de 2026)

Esta revisión sustituye la distribución anterior centrada en hermanos.
- Ejercicio 1: hermanos; elegir la palabra para hermano.
- Ejercicio 2: abuelos; unir dos palabras con los retratos de abuela y abuelo.
- Ejercicio 3: tío; completar la vocal con su marca.
- Ejercicio 4: madrina; ordenar las letras.
- Ejercicio 5: mover a Maicito con la palanca, recoger cuatro palabras de los temas anteriores y entregarlas a sus traducciones. Sonidos al recoger, acertar y equivocarse; cuenta como acierto si completa las cuatro entregas sin errores.
- Preparación con tarjetas que revelan las palabras al tocarlas, retratos distintos por tema y animaciones que respetan Reducir movimiento.
- Se conserva la integración existente de estrellas, tiempo, aciertos, monedas, experiencia y desbloqueos.
- Vocabulario comprobado en el Manual para la escritura de los sonidos del yuhmu, páginas 74, 79, 81 y 84. No se añadieron traducciones de mamá/papá sin confirmar.

Archivos modificados en esta revisión:
- Zentemi/Models/FamilyLessonSession.swift
- Zentemi/Views/FamilyLessonView.swift

Archivos nuevos en esta revisión:
- Zentemi/Models/FamilyReviewGame.swift
- Zentemi/Views/FamilyReviewGameView.swift
- Zentemi/Assets.xcassets/familyGrandmother.imageset/ (PNG y Contents.json)
- Zentemi/Assets.xcassets/familyGrandfather.imageset/ (PNG y Contents.json)
- Zentemi/Assets.xcassets/familyUncle.imageset/ (PNG y Contents.json)
- Zentemi/Assets.xcassets/familyGodmother.imageset/ (PNG y Contents.json)

Registro actualizado: TURNO00.md. No se añadieron audios.
Ilustraciones generadas mediante imagegen y revisadas visualmente; originales en:
 /Users/usuarioastra/Library/Developer/Xcode/CodingAssistant/codex/generated_images/01a1754c-0b48-7740-81cf-555d47d77e90/

Prompt familyGrandmother:
Use case: illustration-story. Asset type: unique character portrait for a children's Yuhmu vocabulary lesson. Subject: a joyful grandmother aged 65 with medium brown skin, silver braid, round glasses and a coral cardigan, hands open in greeting. Friendly polished rounded cartoon, soft dimensional shading, expressive eyes, clean outlines, vibrant pastel colors. Waist-up centered character with generous margin, plain pale lavender background. One character only, clearly distinct age and appearance, no text, no letters, no logos, no watermark.

Prompt familyGrandfather:
Use case: illustration-story. Asset type: unique character portrait for a children's Yuhmu vocabulary lesson. Subject: a joyful grandfather aged 68 with medium brown skin, short silver hair and mustache, indigo shirt, waving one hand. Friendly polished rounded cartoon, soft dimensional shading, expressive eyes, clean outlines, vibrant pastel colors. Waist-up centered character with generous margin, plain pale lavender background. One character only, clearly distinct age and appearance, no text, no letters, no logos, no watermark.

Prompt familyUncle:
Use case: illustration-story. Asset type: unique character portrait for a children's Yuhmu vocabulary lesson. Subject: a friendly adult uncle aged 35 with medium brown skin, short curly dark hair, a small beard and green shirt, smiling with a playful welcoming gesture. Friendly polished rounded cartoon, soft dimensional shading, expressive eyes, clean outlines, vibrant pastel colors. Waist-up centered character with generous margin, plain pale lavender background. One character only, clearly distinct age and appearance, no text, no letters, no logos, no watermark.

Prompt familyGodmother:
Use case: illustration-story. Asset type: unique character portrait for a children's Yuhmu vocabulary lesson. Subject: a friendly adult godmother aged 40 with medium brown skin, shoulder-length wavy dark hair, turquoise blouse and orange scarf, smiling and waving. Friendly polished rounded cartoon, soft dimensional shading, expressive eyes, clean outlines, vibrant pastel colors. Waist-up centered character with generous margin, plain pale lavender background. One character only, clearly distinct age and appearance, no text, no letters, no logos, no watermark.

Validación: revisión estática de referencias y diferencias. No se compiló, ejecutó ni probó la aplicación o el simulador, conforme a la instrucción del usuario.

### Lección Números (25 de noviembre de 2026)

- Fuente: tabla del 1 al 10 de Zentemi4.pdf compartida por el usuario. Se utilizan N’á, Yóho, Hyǘ, Gǒho y Kú̱t’a (1–5).
- Presentación interactiva de cantidades y palabras antes de practicar.
- Cinco ejercicios: tocar y contar cinco hojas; elegir la palabra para tres flores; llenar una canasta con dos objetos; completar la secuencia con cuatro; conectar las cinco palabras con cantidades mediante arrastre o dos toques.
- Conexiones correctas conservan sus líneas; errores permiten reintentar y afectan el acierto del quinto ejercicio. Los demás ejercicios se evalúan una sola vez.
- Indicador 1/5–5/5 y pantalla existente de resultados con estrellas, aciertos, tiempo, monedas y experiencia, incluidas sus animaciones y sonidos.
- Recompensa única: 1,000 monedas y 200 XP. Las repeticiones registran mejores resultados sin duplicar premios.
- Se desbloquea al completar Familia. Al terminar, la siguiente lección muestra Próximamente. Guardado y reinicio de progreso integrados.

Creado: Zentemi/Views/NumbersLessonView.swift.
Modificados: Zentemi/Models/AppState.swift, Zentemi/Views/LessonIntroView.swift, Zentemi/Views/LearningPathView.swift y TURNO00.md.
No se añadieron assets ni audios: las cantidades usan símbolos dibujados por SwiftUI y los sonidos existentes. Se reutiliza el recibo FamilyLessonReward para la pantalla compartida de resultados.
Validación estática de referencias y git diff --check. No se compiló, ejecutó ni probó la app ni el simulador.

### Lección Colores (25 de noviembre de 2026)

- Vocabulario comprobado visualmente en el Manual para la escritura de los sonidos del yuhmu: amarillo K’áxt’i (p. 74), blanco T’àxi (p. 75), negro Mbòi (p. 81), verde Nk’àngi (p. 85). No se inventaron traducciones de otros colores.
- Cinco actividades: descubrir cuatro manchas; reconocer el objeto verde; pintar una nube siguiendo el pedido; completar un patrón amarillo/negro; taller de Maicito con cuatro dibujos y pedidos en Yuhmu.
- Taller sin tiempo límite: elegir pintura, tocar el dibujo y entregar. Los errores permiten corregir con una pista y afectan el acierto del quinto ejercicio.
- Mantiene indicador 1/5–5/5 y pantalla compartida de estrellas, aciertos, tiempo, monedas y experiencia, con sus sonidos y animaciones.
- Primera finalización: 1,100 monedas y 220 XP. Persistencia de recompensa única y mejores resultados; reinicio integrado.
- Colores requiere Números terminado. Al completar Colores, Animales muestra Próximamente.

Creado: Zentemi/Views/ColorsLessonView.swift.
Modificados: Zentemi/Models/AppState.swift, Zentemi/Views/LearningPathView.swift, Zentemi/Views/LessonIntroView.swift y TURNO00.md.
Se usan dibujos y pinceles con símbolos del sistema, sin nuevos assets ni audios; recibo y pantalla de resultados existentes reutilizados.
Validación: revisión estática y git diff --check. No se compiló, abrió, ejecutó ni probó la app o el simulador. El script temporal /tmp/render-colors.swift solo renderizó dos páginas del PDF de vocabulario para su lectura.

### Diagnósticos de Colores: actualización del editor

- Se reprodujeron los seis diagnósticos del editor en ColorsLessonView.swift sobre numbersRewardClaimed.
- La propiedad ya existía correctamente en AppState.swift; no fue necesario cambiar código.
- Se actualizaron los diagnósticos estáticos de AppState.swift y después de ColorsLessonView.swift mediante XcodeRefreshCodeIssuesInFile. Ambos devolvieron cero incidencias. Esto indica diagnósticos desactualizados del editor.
- Único archivo modificado: TURNO00.md. No se ejecutó la aplicación, simulador, compilación completa ni pruebas.

### Lección Animales (26 de noviembre de 2026)

- Vocabulario comprobado visualmente en el Manual para la escritura de los sonidos del yuhmu: Gáne (abeja, p. 74), Khwá (conejo, p. 76), Bàdu (pato, p. 82), ’Yó (perro, p. 82).
- Cinco ejercicios: álbum interactivo; reconocer la silueta del conejo; completar Bàdu; seleccionar perro y abeja por sus nombres en Yuhmu; sendero con cuatro rondas para arrastrar a Maicito hasta el animal indicado o tocar su retrato como alternativa.
- El minijuego permite corregir errores, con pistas y sonidos; cuenta como acierto si se terminan las cuatro rondas sin errores. Sin límite de tiempo.
- Indicador y resultados compartidos: estrellas, aciertos, tiempo, monedas y XP. Recompensa única de 1,200 monedas y 240 XP; mejores resultados persistidos y reinicio integrado.
- Requiere Colores completado. Al terminar Animales, Alimentos muestra Próximamente.

Creado:
- Zentemi/Views/AnimalsLessonView.swift
- Zentemi/Assets.xcassets/animalLessonDog.imageset/ (animalLessonDog.png y Contents.json)
- Zentemi/Assets.xcassets/animalLessonDuck.imageset/ (animalLessonDuck.png y Contents.json)
- Zentemi/Assets.xcassets/animalLessonRabbit.imageset/ (animalLessonRabbit.png y Contents.json)
- Zentemi/Assets.xcassets/animalLessonBee.imageset/ (animalLessonBee.png y Contents.json)

Modificados: Zentemi/Models/AppState.swift, Zentemi/Views/LessonIntroView.swift, Zentemi/Views/LearningPathView.swift y TURNO00.md.
No se añadieron audios.

Imágenes generadas con la herramienta integrada image_gen, inspeccionadas visualmente y copiadas a Assets. Prompts finales:

animalLessonDog:
Use case: illustration-story. Asset type: animal portrait for a children's Yuhmu learning app. Subject: a friendly golden brown dog sitting, floppy ears and wagging tail. One animal, full body centered, generous margins, square composition. Polished rounded cartoon illustration with soft dimensional shading, expressive eyes and clean outlines, colorful and welcoming for primary and secondary students. Plain pale mint background. No text, letters, logo, frame or watermark. Match a cohesive educational animal card collection.

animalLessonDuck:
Use case: illustration-story. Asset type: animal portrait for a children's Yuhmu learning app. Subject: a friendly white duck with orange bill and webbed feet. One animal, full body centered, generous margins, square composition. Polished rounded cartoon illustration with soft dimensional shading, expressive eyes and clean outlines, colorful and welcoming for primary and secondary students. Plain pale mint background. No text, letters, logo, frame or watermark. Match a cohesive educational animal card collection.

animalLessonRabbit:
Use case: illustration-story. Asset type: animal portrait for a children's Yuhmu learning app. Subject: a friendly gray rabbit with long upright ears and small fluffy tail. One animal, full body centered, generous margins, square composition. Polished rounded cartoon illustration with soft dimensional shading, expressive eyes and clean outlines, colorful and welcoming for primary and secondary students. Plain pale mint background. No text, letters, logo, frame or watermark. Match a cohesive educational animal card collection.

animalLessonBee:
Use case: illustration-story. Asset type: animal portrait for a children's Yuhmu learning app. Subject: a friendly honeybee with yellow and dark brown stripes, six legs and translucent wings. One animal, full body centered, generous margins, square composition. Polished rounded cartoon illustration with soft dimensional shading, expressive eyes and clean outlines, colorful and welcoming for primary and secondary students. Plain pale mint background. No text, letters, logo, frame or watermark. Match a cohesive educational animal card collection.

Originales conservados en /Users/usuarioastra/Library/Developer/Xcode/CodingAssistant/codex/generated_images/01a1754c-0b48-7740-81cf-555d47d77e90/:
- Perro: exec-3d88bff4-e4b7-4eaa-9e9b-474ad3baf77c.png
- Pato: exec-a7135f33-c95a-45db-b2ea-e4d5526c6061.png
- Conejo: exec-d1067e96-00fb-44b0-a6c2-ffe3b17c2c20.png
- Abeja: exec-cd893703-bfa8-45dd-bf77-3fb3ada9dfd6.png

Validación: diagnósticos estáticos del editor de AppState, AnimalsLessonView, LearningPathView y LessonIntroView sin incidencias al terminar. No se compiló, ejecutó ni probó la app ni el simulador.

### Lección Alimentos (26 de noviembre de 2026)

- Vocabulario confirmado visualmente en el Manual para la escritura de los sonidos del yuhmu: Déhe (agua, p. 74), Ts’óni (aguacate, p. 74), K’ápuxü (papa, p. 82), Hmé (tortilla, p. 85).
- Cinco ejercicios: descubrir el puesto del mercado; etiquetar la papa; servir el aguacate indicado en Yuhmu; seleccionar tortilla y agua; preparar tres canastas con listas en Yuhmu y cantidades.
- Minijuego: agregar mediante toque o arrastre, quitar unidades, entregar y corregir. Cada toque representa una unidad; el texto lo aclara para la ilustración de varias tortillas. Sin tiempo límite. Entregas erróneas afectan el acierto del ejercicio final.
- Mantiene progreso 1/5–5/5, resultados con estrellas, aciertos, tiempo, monedas y XP, sonidos y animaciones.
- Requiere Animales terminado. Primera finalización: 1,300 monedas y 260 XP, sin premios duplicados al repetir. Mejores resultados, carga y reinicio integrados. Frases básicas queda Próximamente.

Creado: Zentemi/Views/FoodLessonView.swift.
Nuevos assets (cada carpeta contiene PNG homónimo y Contents.json):
- Zentemi/Assets.xcassets/foodLessonWater.imageset/
- Zentemi/Assets.xcassets/foodLessonAvocado.imageset/
- Zentemi/Assets.xcassets/foodLessonPotato.imageset/
- Zentemi/Assets.xcassets/foodLessonTortilla.imageset/
Modificados: Zentemi/Models/AppState.swift, Zentemi/Views/LessonIntroView.swift, Zentemi/Views/LearningPathView.swift y TURNO00.md. No se añadieron audios.

Imágenes generadas con la herramienta integrada image_gen, revisadas visualmente e incorporadas a Assets. Prompts finales:

foodLessonWater:
Use case: illustration-story. Asset type: food vocabulary card for a children's Yuhmu learning app. Subject: a clear drinking glass filled with fresh water, a few blue highlights. Polished rounded cartoon illustration, soft dimensional shading, clean outlines, appetizing natural colors. Centered full object, generous margins, square composition, plain pale peach background. Match a cohesive educational food card collection. No face, hands, text, letters, logo, frame or watermark.

foodLessonAvocado:
Use case: illustration-story. Asset type: food vocabulary card for a children's Yuhmu learning app. Subject: one ripe avocado cut in half, green flesh and large brown pit. Polished rounded cartoon illustration, soft dimensional shading, clean outlines, appetizing natural colors. Centered full object, generous margins, square composition, plain pale peach background. Match a cohesive educational food card collection. No face, hands, text, letters, logo, frame or watermark.

foodLessonPotato:
Use case: illustration-story. Asset type: food vocabulary card for a children's Yuhmu learning app. Subject: one whole golden brown potato with small natural eyes. Polished rounded cartoon illustration, soft dimensional shading, clean outlines, appetizing natural colors. Centered full object, generous margins, square composition, plain pale peach background. Match a cohesive educational food card collection. No face, hands, text, letters, logo, frame or watermark.

foodLessonTortilla:
Use case: illustration-story. Asset type: food vocabulary card for a children's Yuhmu learning app. Subject: a small stack of three round Mexican corn tortillas with lightly toasted spots. Polished rounded cartoon illustration, soft dimensional shading, clean outlines, appetizing natural colors. Centered full object, generous margins, square composition, plain pale peach background. Match a cohesive educational food card collection. No face, hands, text, letters, logo, frame or watermark.

Originales conservados en /Users/usuarioastra/Library/Developer/Xcode/CodingAssistant/codex/generated_images/01a1754c-0b48-7740-81cf-555d47d77e90/:
- Agua: exec-0c66e5f7-df3a-406f-a469-e58de6df0eba.png
- Aguacate: exec-5e094051-0ef4-447f-b9e4-93c071b98f69.png
- Papa: exec-33aa7b33-3afb-4f9b-9277-88a8dcbd3a50.png
- Tortilla: exec-415f143e-7f98-4910-8486-0fd1030ff63d.png

Validación: diagnósticos del editor sin incidencias en AppState, FoodLessonView, LearningPathView y LessonIntroView; git diff --check sin incidencias en los archivos de código tocados. No se compiló, ejecutó ni probó la app o el simulador.

### Renovación de Primeras frases (continuación tras interrupción por créditos)

- Nueva entrada FirstPhrasesLessonView: sustituye el recorrido por escenas y el ejercicio con micrófono. Los archivos antiguos se conservan, pero LessonIntroView ya no entra por FirstPhraseSceneView.
- Cinco ejercicios: descubrir y escuchar cuatro tarjetas; identificar Buenos días; ordenar ¿Cuántos años tienes?; elegir Ayúdame por favor para una situación; minijuego Un día con Maicito con cuatro paradas y opciones de audio.
- Se conservan los cuatro audios originales, sin inventar transcripciones en Yuhmu. Escucha confirmada exclusivamente por finalización correcta del reproductor. Cambiar de frase, salir o poner la app en segundo plano detiene el audio. Volumen ajustable dentro del panel.
- El minijuego permite reintentos con pistas; su acierto requiere cuatro situaciones sin errores. Indicador, sonidos y pantalla de resultados compartida conservados.
- Reutiliza completeGreetingsLesson y el indicador de premio existente: 700 monedas y 140 XP solo la primera vez; desbloqueo de Familia y progreso anterior conservados. El recibo de resultados muestra cero premios al repetir.
- PhraseSceneAudioPlayer también busca la carpeta audios en minúsculas.

Creado:
- Zentemi/Views/FirstPhrasesLessonView.swift
- Zentemi/Assets.xcassets/phraseMorning.imageset/ (phraseMorning.png, Contents.json)
- Zentemi/Assets.xcassets/phraseName.imageset/ (phraseName.png, Contents.json)
- Zentemi/Assets.xcassets/phraseAge.imageset/ (phraseAge.png, Contents.json)
- Zentemi/Assets.xcassets/phraseHelp.imageset/ (phraseHelp.png, Contents.json)

Modificados: Zentemi/Views/LessonIntroView.swift, Zentemi/PhraseSceneAudioPlayer.swift y TURNO00.md.
No se añadieron audios ni se borraron archivos o progreso.

Ilustraciones generadas con image_gen integrado, inspeccionadas visualmente y copiadas a Assets. Prompts finales:

phraseMorning:
Use case: illustration-story. Asset type: situation card for a children's Yuhmu learning app. Subject: two schoolchildren waving hello to each other outside school in gentle morning sunlight. Friendly Mexican children, age appropriate school clothing. Polished rounded cartoon, expressive faces, soft dimensional shading, vivid pastel palette, pale lavender and mint background details. Square composition, clear action readable at small sizes, generous margins. No writing, letters, numbers, speech bubbles, logos or watermark.

phraseName:
Use case: illustration-story. Asset type: situation card for a children's Yuhmu learning app. Subject: two schoolchildren meeting for the first time, one pointing gently to their own chest while introducing themselves. Friendly Mexican children, age appropriate school clothing. Polished rounded cartoon, expressive faces, soft dimensional shading, vivid pastel palette, pale lavender and mint background details. Square composition, clear action readable at small sizes, generous margins. No writing, letters, numbers, speech bubbles, logos or watermark.

phraseAge:
Use case: illustration-story. Asset type: situation card for a children's Yuhmu learning app. Subject: a schoolchild celebrating a birthday with a small cake and candles, another child asking about their age. Friendly Mexican children, age appropriate school clothing. Polished rounded cartoon, expressive faces, soft dimensional shading, vivid pastel palette, pale lavender and mint background details. Square composition, clear action readable at small sizes, generous margins. No writing, letters, numbers, speech bubbles, logos or watermark.

phraseHelp:
Use case: illustration-story. Asset type: situation card for a children's Yuhmu learning app. Subject: a schoolchild dropping a few books and asking another child who kindly helps pick them up. Friendly Mexican children, age appropriate school clothing. Polished rounded cartoon, expressive faces, soft dimensional shading, vivid pastel palette, pale lavender and mint background details. Square composition, clear action readable at small sizes, generous margins. No writing, letters, numbers, speech bubbles, logos or watermark.

Originales conservados en /Users/usuarioastra/Library/Developer/Xcode/CodingAssistant/codex/generated_images/01a1754c-0b48-7740-81cf-555d47d77e90/:
- phraseMorning: exec-e82f718e-4ffe-4b18-b405-99c38b2c6dfb.png
- phraseName: exec-84efdb8a-b819-4e35-925d-3690d92e1e08.png
- phraseAge: exec-dfff42f9-1233-40ea-8bfb-7bc50d8c522f.png
- phraseHelp: exec-88acfbcf-2759-4ea9-8b8b-7968a9c3f482.png

Validación: revisión estática y diagnósticos del editor. No se compiló ni ejecutó la aplicación, simulador o pruebas.


### Perfil: once logros en tres columnas (26 de noviembre de 2026)

- Sustituida la fila horizontal decorativa por 11 tarjetas con imagen, progreso, estado y detalles al tocar; tres columnas en el desplazamiento vertical existente. Conserva cabecera, estadísticas y estilo del perfil.
- Metas: completar los 3 juegos y las 6 lecciones disponibles, 10/50/100 comidas, 1,000/5,000/10,000 monedas ganadas, mejor racha de 5/10/20 días.
- Persistencia: zentemi.totalFeedings, zentemi.lifetimeEarnedCoins, zentemi.bestAchievementStreak. Gasto no resta progreso; comidas fallidas no cuentan; perder racha no retira logros. Reinicio de desarrollo integrado.
- Migración: recupera recompensas conocidas de lecciones, juegos y máscaras de monedas; excluye las 1,500 monedas iniciales. Comidas antiguas: solo se pueden recuperar las fechas retenidas; rachas antiguas: solo la actual disponible.
- Creados: Zentemi/Models/ProfileAchievement.swift, Zentemi/Views/ProfileAchievementsSection.swift. Modificados: Zentemi/Views/ProfileView.swift, Zentemi/Models/AppState.swift, Zentemi/Views/FeedMaicitoView.swift y este registro.
- Once imágenes generadas con image_gen integrado y revisadas visualmente, copiadas sin editar ni borrar originales. Cada imageset incluye PNG y Contents.json.

Prompts exactos y originales:

achievementGames — Zentemi/Assets.xcassets/achievementGames.imageset/

Use case: stylized-concept. Asset type: individual achievement badge illustration for Zentemi, a children's learning app. Subject: a golden trophy holding a purple game controller, turquoise laurel leaves. Cohesive polished rounded cartoon style, soft dimensional shading, clean thick outlines, purple and turquoise accents, warm cheerful colors, simple silhouette readable at 64 pixels. One centered badge, square canvas, generous padding, plain pale lavender background. No text, no numbers, no letters, no watermark.

Original: /Users/usuarioastra/Library/Developer/Xcode/CodingAssistant/codex/generated_images/01a21a16-f513-7451-b8bd-b3b593e15904/exec-8a3eeb41-54c8-4752-bf2e-ab448a008b15.png

achievementLessons — Zentemi/Assets.xcassets/achievementLessons.imageset/

Use case: stylized-concept. Asset type: individual achievement badge illustration for Zentemi, a children's learning app. Subject: an open lavender book with a golden graduation cap and turquoise star. Cohesive polished rounded cartoon style, soft dimensional shading, clean thick outlines, purple and turquoise accents, warm cheerful colors, simple silhouette readable at 64 pixels. One centered badge, square canvas, generous padding, plain pale lavender background. No text, no numbers, no letters, no watermark.

Original: /Users/usuarioastra/Library/Developer/Xcode/CodingAssistant/codex/generated_images/01a21a16-f513-7451-b8bd-b3b593e15904/exec-b58a82b9-22cd-4872-aa61-cd4f2f014f9d.png

achievementFeed10 — Zentemi/Assets.xcassets/achievementFeed10.imageset/

Use case: stylized-concept. Asset type: individual achievement badge illustration for Zentemi, a children's learning app. Subject: a bronze heart medal with a red apple and small green leaf. Cohesive polished rounded cartoon style, soft dimensional shading, clean thick outlines, purple and turquoise accents, warm cheerful colors, simple silhouette readable at 64 pixels. One centered badge, square canvas, generous padding, plain pale lavender background. No text, no numbers, no letters, no watermark.

Original: /Users/usuarioastra/Library/Developer/Xcode/CodingAssistant/codex/generated_images/01a21a16-f513-7451-b8bd-b3b593e15904/exec-0dca64e0-cb14-497a-9ece-15116c3dd261.png

achievementFeed50 — Zentemi/Assets.xcassets/achievementFeed50.imageset/

Use case: stylized-concept. Asset type: individual achievement badge illustration for Zentemi, a children's learning app. Subject: a silver heart medal with a small bowl of colorful fruit. Cohesive polished rounded cartoon style, soft dimensional shading, clean thick outlines, purple and turquoise accents, warm cheerful colors, simple silhouette readable at 64 pixels. One centered badge, square canvas, generous padding, plain pale lavender background. No text, no numbers, no letters, no watermark.

Original: /Users/usuarioastra/Library/Developer/Xcode/CodingAssistant/codex/generated_images/01a21a16-f513-7451-b8bd-b3b593e15904/exec-1ea5dc21-0c27-4a83-acd0-56497e6d7676.png

achievementFeed100 — Zentemi/Assets.xcassets/achievementFeed100.imageset/

Use case: stylized-concept. Asset type: individual achievement badge illustration for Zentemi, a children's learning app. Subject: a golden heart medal with a generous fruit basket and tiny crown. Cohesive polished rounded cartoon style, soft dimensional shading, clean thick outlines, purple and turquoise accents, warm cheerful colors, simple silhouette readable at 64 pixels. One centered badge, square canvas, generous padding, plain pale lavender background. No text, no numbers, no letters, no watermark.

Original: /Users/usuarioastra/Library/Developer/Xcode/CodingAssistant/codex/generated_images/01a21a16-f513-7451-b8bd-b3b593e15904/exec-17031f96-acbf-46d3-a00e-d7af4e2ec091.png

achievementCoins1000 — Zentemi/Assets.xcassets/achievementCoins1000.imageset/

Use case: stylized-concept. Asset type: individual achievement badge illustration for Zentemi, a children's learning app. Subject: a bronze medal framing a small stack of shiny gold coins. Cohesive polished rounded cartoon style, soft dimensional shading, clean thick outlines, purple and turquoise accents, warm cheerful colors, simple silhouette readable at 64 pixels. One centered badge, square canvas, generous padding, plain pale lavender background. No text, no numbers, no letters, no watermark.

Original: /Users/usuarioastra/Library/Developer/Xcode/CodingAssistant/codex/generated_images/01a21a16-f513-7451-b8bd-b3b593e15904/exec-8e96d7de-4e8d-4200-af32-73440b117eec.png

achievementCoins5000 — Zentemi/Assets.xcassets/achievementCoins5000.imageset/

Use case: stylized-concept. Asset type: individual achievement badge illustration for Zentemi, a children's learning app. Subject: a silver medal framing a purple pouch spilling gold coins. Cohesive polished rounded cartoon style, soft dimensional shading, clean thick outlines, purple and turquoise accents, warm cheerful colors, simple silhouette readable at 64 pixels. One centered badge, square canvas, generous padding, plain pale lavender background. No text, no numbers, no letters, no watermark.

Original: /Users/usuarioastra/Library/Developer/Xcode/CodingAssistant/codex/generated_images/01a21a16-f513-7451-b8bd-b3b593e15904/exec-66826adb-bbee-4238-a46e-3070aa9c41a3.png

achievementCoins10000 — Zentemi/Assets.xcassets/achievementCoins10000.imageset/

Use case: stylized-concept. Asset type: individual achievement badge illustration for Zentemi, a children's learning app. Subject: a golden medal framing a treasure chest full of gold coins. Cohesive polished rounded cartoon style, soft dimensional shading, clean thick outlines, purple and turquoise accents, warm cheerful colors, simple silhouette readable at 64 pixels. One centered badge, square canvas, generous padding, plain pale lavender background. No text, no numbers, no letters, no watermark.

Original: /Users/usuarioastra/Library/Developer/Xcode/CodingAssistant/codex/generated_images/01a21a16-f513-7451-b8bd-b3b593e15904/exec-6b12a69c-a658-4a48-ac77-11a319663ce2.png

achievementStreak5 — Zentemi/Assets.xcassets/achievementStreak5.imageset/

Use case: stylized-concept. Asset type: individual achievement badge illustration for Zentemi, a children's learning app. Subject: a bronze circular medal framing a cheerful orange flame. Cohesive polished rounded cartoon style, soft dimensional shading, clean thick outlines, purple and turquoise accents, warm cheerful colors, simple silhouette readable at 64 pixels. One centered badge, square canvas, generous padding, plain pale lavender background. No text, no numbers, no letters, no watermark.

Original: /Users/usuarioastra/Library/Developer/Xcode/CodingAssistant/codex/generated_images/01a21a16-f513-7451-b8bd-b3b593e15904/exec-c67e68f7-5df6-4067-ab69-21bbdbe063fd.png

achievementStreak10 — Zentemi/Assets.xcassets/achievementStreak10.imageset/

Use case: stylized-concept. Asset type: individual achievement badge illustration for Zentemi, a children's learning app. Subject: a silver circular medal framing two bright orange flames and a star. Cohesive polished rounded cartoon style, soft dimensional shading, clean thick outlines, purple and turquoise accents, warm cheerful colors, simple silhouette readable at 64 pixels. One centered badge, square canvas, generous padding, plain pale lavender background. No text, no numbers, no letters, no watermark.

Original: /Users/usuarioastra/Library/Developer/Xcode/CodingAssistant/codex/generated_images/01a21a16-f513-7451-b8bd-b3b593e15904/exec-7e29ab52-1114-48b1-a15a-6f73d26462bc.png

achievementStreak20 — Zentemi/Assets.xcassets/achievementStreak20.imageset/

Use case: stylized-concept. Asset type: individual achievement badge illustration for Zentemi, a children's learning app. Subject: a golden circular medal framing a bright orange flame wearing a tiny purple crown. Cohesive polished rounded cartoon style, soft dimensional shading, clean thick outlines, purple and turquoise accents, warm cheerful colors, simple silhouette readable at 64 pixels. One centered badge, square canvas, generous padding, plain pale lavender background. No text, no numbers, no letters, no watermark.

Original: /Users/usuarioastra/Library/Developer/Xcode/CodingAssistant/codex/generated_images/01a21a16-f513-7451-b8bd-b3b593e15904/exec-9a12c65c-6d43-4e3f-8b83-dade7f3fb64c.png

Validación: los cinco archivos Swift revisados presentan cero incidencias en los diagnósticos del editor. Referencias de los once assets verificadas y git diff --check sin incidencias. No se compiló, ejecutó ni probó la aplicación o el simulador; validación funcional pendiente del usuario.


### Tienda: playerita roja por 100 monedas (26 de noviembre de 2026)

- Misma tarjeta, confirmación, estado Comprado, sonidos y animación hacia Mochila. Compra única con saldo suficiente; precio centralizado en AppState.redShirtPrice.
- Mochila permite equipar y quitar, actualiza sus contadores y conserva los sonidos existentes. Playerita y traje elegante se excluyen mutuamente; los demás accesorios permanecen.
- PetView comparte la playerita en todas sus apariciones, con escala proporcional al tamaño de Maicito y registro transparente similar al traje.
- Persistencia: zentemi.redShirtPurchased y zentemi.redShirtEquipped; carga validada por propiedad y reinicio de desarrollo integrado. Gastar monedas no resta el progreso del logro de monedas acumuladas.
- Modificados: Zentemi/Models/AppState.swift, Zentemi/Views/StoreView.swift, Zentemi/Views/BackpackView.swift, Zentemi/Components/PetView.swift y TURNO00.md.
- Creado: Zentemi/Assets.xcassets/playeritaRoja.imageset/playeritaRoja.png y Contents.json. Imagen generada mediante image_gen integrado, revisada visualmente, canal alfa verificado y copiada sin modificar el original.
- Original: /Users/usuarioastra/Library/Developer/Xcode/CodingAssistant/codex/generated_images/01a21a16-f513-7451-b8bd-b3b593e15904/exec-861d089f-2b12-48ea-9ee2-7434f5a096c4.png

Prompt exacto:
Use case: precise-object-edit. Asset type: transparent clothing overlay for the Zentemi corn mascot. Edit the supplied elegant suit accessory into a simple bright red short-sleeved T-shirt. Preserve the source image's square canvas registration: the upper half stays completely transparent, shoulder tops at about 56% of canvas height, neckline centered at 60%, sleeves extending from 14% to 84% canvas width, rounded wide shirt hem at 83% height. The shirt covers the same torso region as the jacket, with a short round shallow collar (not a deep V), dark warm brown cartoon outlines, gentle red shading, small stitched hems. Remove the trousers, bowtie, jacket, gold trim and brown hands completely. Only the red T-shirt is visible; all other pixels including below the hem and inside the neck opening must be genuinely transparent. Front facing, symmetrical, wide short torso and short sleeves angled down, matching the original friendly hand-drawn game accessory style. No character, no mannequin, no hanger, no text, no background, no additional clothing. Do not center or enlarge the garment to fill the canvas: keep its position in the lower half for overlay alignment.

Validación: cero incidencias en los diagnósticos del editor de los cuatro archivos Swift modificados; git diff --check sin incidencias. No se compiló, ejecutó ni probó la app o el simulador. Ajuste visual y flujo de compra/equipamiento pendientes de prueba del usuario.


### Fondos comprables para Home: playa y valle (26 de noviembre de 2026)

- Fondo de playa: 300 monedas; fondo de valle: 500 monedas. Catálogo tipado HomeBackground con identificadores estables.
- Reutiliza tarjetas, confirmación, sonido de compra, estado comprado y animación hacia Mochila. Compra única con saldo suficiente.
- En Mochila: aparecen solo los fondos comprados, con Equipar fondo/Quitar fondo, estado y sonidos existentes; contadores incluyen fondos. Solo un fondo equipado; quitarlo restaura el original.
- Home muestra el paisaje seleccionado con escalado para cubrir el área disponible, sin cambiar las otras pantallas.
- Guardado local: zentemi.purchasedHomeBackgrounds y zentemi.equippedHomeBackground. Carga ignora identificadores desconocidos y selecciones no compradas. Reinicio de desarrollo integrado.
- Catálogo HomeBackground definido en AppState.swift. Modificados: Zentemi/Models/AppState.swift, Zentemi/Views/StoreView.swift, Zentemi/Views/BackpackView.swift, Zentemi/Views/HomeView.swift y TURNO00.md.
- Dos fondos generados con image_gen integrado, inspeccionados visualmente y copiados a Assets sin modificar originales. Cada imageset incluye PNG y Contents.json.

Asset: Zentemi/Assets.xcassets/homeBeachBackground.imageset/
Original: /Users/usuarioastra/Library/Developer/Xcode/CodingAssistant/codex/generated_images/01a21a16-f513-7451-b8bd-b3b593e15904/exec-716bb1d7-efba-4a8d-9261-bd95f2b2b375.png
Prompt exacto:
Use case: illustration-story. Asset type: portrait full-screen Home background for Zentemi, a cheerful children's Yuhmu learning game with a cartoon corn mascot. Scene: a peaceful tropical beach, turquoise ocean and gentle waves on the upper middle horizon, warm pale sand across the large open center and foreground, palm trees framing the far edges, small seashells near corners, soft blue sky. Polished friendly hand-painted cartoon game art, rounded shapes, soft dimensional shading, warm daylight, pastel colors with turquoise and lavender accents. Portrait 9:16 composition, fill all edges. Keep the central area calm and lightly colored for a character added by the app; keep lower third low-detail for interface cards. Landscape only: no characters, no corn mascot, no people, no UI, no text, no letters, no logos, no watermark.

Asset: Zentemi/Assets.xcassets/homeValleyBackground.imageset/
Original: /Users/usuarioastra/Library/Developer/Xcode/CodingAssistant/codex/generated_images/01a21a16-f513-7451-b8bd-b3b593e15904/exec-8046fd67-44ce-488e-ab48-5f2dc9ad74b0.png
Prompt exacto:
Use case: illustration-story. Asset type: portrait full-screen Home background for Zentemi, a cheerful children's Yuhmu learning game with a cartoon corn mascot. Scene: a peaceful lush green valley, rounded distant mountains and a winding small stream in the background, a large open light-green grassy clearing across the center and foreground, wildflowers and leafy trees framing the far edges, soft blue sky. Polished friendly hand-painted cartoon game art, rounded shapes, soft dimensional shading, warm daylight, pastel colors with turquoise and lavender accents. Portrait 9:16 composition, fill all edges. Keep the central area calm and lightly colored for a character added by the app; keep lower third low-detail for interface cards. Landscape only: no characters, no corn mascot, no people, no UI, no text, no letters, no logos, no watermark.

Validación: cero incidencias en los diagnósticos del editor de AppState, StoreView, BackpackView y HomeView. Revisión de compra única, saldo, selección exclusiva, restauración y reinicio en código. No se compiló, ejecutó ni probó la app o el simulador; prueba funcional y visual a cargo del usuario.


### Categorías de Tienda y Mochila (26 de noviembre de 2026)

- Barra compartida de filtros Todo, Ropa, Accesorios, Cabellos y Fondos, con iconos y selección morada. Se desplaza horizontalmente para acomodar los botones.
- Ropa: traje y playerita. Accesorios: lentes, lentes de sol, bufanda y espada. Cabellos: normal y rosa. Fondos: playa y valle.
- Tienda conserva tarjetas, precios, compras y orden de artículos comprados al final; cambiar categoría cierra la confirmación pendiente.
- Mochila conserva la vista fija de Maicito, equipamiento y contadores globales. Mensaje vacío para categorías sin objetos adquiridos. Los objetos iniciales también se filtran.
- Modificados: Zentemi/Views/StoreView.swift, Zentemi/Views/BackpackView.swift y TURNO00.md. ItemCategory e ItemCategoryBar se comparten desde StoreView.swift. No se añadieron assets ni se alteró el guardado.
- Validación: diagnósticos del editor sin incidencias en ambos archivos. No se compiló, ejecutó ni probó la app o el simulador.

### Resolución de conflictos de integración con v1.1 (26 de noviembre de 2026)

- Diagnóstico: merge pendiente entre HEAD 71861f4 y d5bfa14, con ocho archivos Swift sin fusionar. Se guardó una copia de los archivos conflictivos antes de editarlos en /var/folders/hb/1vff417n2msf6yp92dl4941c0000gn/T/zentemi-merge-hteffi1t.
- Resueltos PetView, ContentView, AppState, FeedMaicitoView, HomeView, MainTabView, ProfileView y StoreView, conservando Firebase Auth/Firestore, modo invitado y juegos nuevos.
- Catálogo compartido con 57 identificadores únicos: incorpora playerita roja (100), fondo playa (300) y valle (500) con sus assets y claves locales originales. Compras de estos artículos incluidas en exportación/importación del inventario cloud y persistencia posterior a recibirlo. Comprar no equipa automáticamente; equipamiento por zona excluyente mediante toggleEquip.
- Mochila restaurada como cuarta pestaña junto a Tienda, Inicio y Perfil; inventario y contadores derivados del catálogo completo, incluidos nuevos accesorios y fondos. Categorías compartidas: Todo, Ropa, Accesorios, Cabellos y Fondos.
- PetView usa capas del catálogo, incluida la playerita. Home conserva fondos completos de playa/valle y admite los fondos nuevos; el paisaje por defecto no los oculta. Tienda admite vista previa de ambos tipos de fondo.
- Perfil conserva la cuadrícula de 11 logros ilustrados y sus contadores persistidos. Alimentación registra el logro y aumenta felicidad; recompensas de juegos nuevos cuentan en monedas ganadas. GamesHub ofrece acceso a Laberinto, Memorama y Sopa de letras además de los nuevos juegos.
- Validación: diagnósticos del editor sin incidencias en 22 archivos Swift revisados; ausencia de marcadores de conflicto; catálogo sin IDs duplicados; 47 referencias de imágenes con archivos presentes; git diff --check limpio.
- No se compiló ni ejecutó la app o el simulador. El navegador de incidencias conserva 30 mensajes de la compilación anterior (origin: build); la validación de compilación completa, visual, compras y sincronización real queda pendiente de prueba del usuario. No se hizo commit ni push.

### Resolución de conflictos de integración con v1.1 (26 de noviembre de 2026)

- Diagnóstico: merge pendiente entre HEAD 71861f4 y d5bfa14, con ocho archivos Swift sin fusionar. Se guardó una copia de los archivos conflictivos antes de editarlos en /var/folders/hb/1vff417n2msf6yp92dl4941c0000gn/T/zentemi-merge-hteffi1t.
- Resueltos PetView, ContentView, AppState, FeedMaicitoView, HomeView, MainTabView, ProfileView y StoreView, conservando Firebase Auth/Firestore, modo invitado y juegos nuevos.
- Catálogo compartido con 57 identificadores únicos: incorpora playerita roja (100), fondo playa (300) y valle (500) con sus assets y claves locales originales. Compras de estos artículos incluidas en exportación/importación del inventario cloud y persistencia posterior a recibirlo. Comprar no equipa automáticamente; equipamiento por zona excluyente mediante toggleEquip.
- Mochila restaurada como cuarta pestaña junto a Tienda, Inicio y Perfil; inventario y contadores derivados del catálogo completo, incluidos nuevos accesorios y fondos. Categorías compartidas: Todo, Ropa, Accesorios, Cabellos y Fondos.
- PetView usa capas del catálogo, incluida la playerita. Home conserva fondos completos de playa/valle y admite los fondos nuevos; el paisaje por defecto no los oculta. Tienda admite vista previa de ambos tipos de fondo.
- Perfil conserva la cuadrícula de 11 logros ilustrados y sus contadores persistidos. Alimentación registra el logro y aumenta felicidad; recompensas de juegos nuevos cuentan en monedas ganadas. GamesHub ofrece acceso a Laberinto, Memorama y Sopa de letras además de los nuevos juegos.
- Validación: diagnósticos del editor sin incidencias en 22 archivos Swift revisados; ausencia de marcadores de conflicto; catálogo sin IDs duplicados; 47 referencias de imágenes con archivos presentes; git diff --check limpio.
- No se compiló ni ejecutó la app o el simulador. El navegador de incidencias conserva 30 mensajes de la compilación anterior (origin: build); la validación de compilación completa, visual, compras y sincronización real queda pendiente de prueba del usuario. No se hizo commit ni push.
- Git: los ocho archivos quedaron marcados como resueltos; sin entradas unmerged. Se limpiaron únicamente espacios finales en doce líneas entrantes de SettingsView para dejar también git diff --cached --check limpio. Merge preparado para su commit, todavía pendiente.
