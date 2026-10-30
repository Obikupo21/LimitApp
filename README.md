# LimitApp

App Android para limitar el uso de otras apps: eliges qué apps quieres limitar y, cada vez que intentas abrir una, tienes que resolver un acertijo matemático al azar para poder entrar.

## Funcionalidades

- Elige qué apps del teléfono quieres limitar (con buscador).
- Acertijos aleatorios en tres niveles de dificultad:
  - **Fácil**: sumas, restas y multiplicaciones sencillas.
  - **Medio**: operaciones con dos cifras, divisiones, ecuaciones (`Si 3x + 5 = 20, ¿cuánto vale x?`) y series.
  - **Difícil**: operaciones combinadas, potencias, ecuaciones y series cuadráticas.
- Si fallas, aparece un acertijo nuevo.
- Tras acertar, la app queda desbloqueada durante 1, 5, 15 o 30 minutos (configurable).

## Instalación

1. Descarga `LimitApp.apk` desde la sección [Releases](../../releases) e instálalo (Android te pedirá permitir apps de origen desconocido).
2. Abre LimitApp y pulsa **Abrir ajustes de accesibilidad**. Activa el servicio **LimitApp**.
3. En Android 13 o superior, si aparece **"Ajuste restringido"**: ve a Ajustes › Aplicaciones › LimitApp, pulsa ⋮ y elige **"Permitir ajustes restringidos"**. Después repite el paso 2.
4. Marca las apps que quieras limitar.

Requiere Android 8.0 o superior.

## Cómo funciona

LimitApp usa un servicio de accesibilidad que solo escucha qué app pasa a primer plano. No lee ni guarda el contenido de la pantalla. Si la app abierta está limitada y no está desbloqueada, muestra encima la pantalla del acertijo.

## Compilar

Requiere JDK 17+ y el Android SDK.

```
./gradlew assembleRelease
```

El APK se genera en `app/build/outputs/apk/release/`. Si no existe `firma/keystore.properties`, el release se firma con la clave de depuración.

Hecho con Kotlin y Jetpack Compose.
