# Preparación para Google Play Console y GitHub - AhorraGAS

Plan integral de preparación y empaquetado para la publicación de **AhorraGAS** en Google Play Store y sincronización segura en GitHub. Configura el identificador de aplicación definitivo `com.aitorgc.ahorragas`, genera una clave de firma de producción (Keystore), compila el paquete `Android App Bundle` (`.aab`) firmado y optimizado, elabora la Política de Privacidad en `PRIVACY_POLICY.md` con visor accesible dentro de Ajustes, y audita los archivos sensibles del repositorio.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> **Decisiones confirmadas por el usuario:**
> 1. **Identificador de Paquete (Application ID):** Se actualizará a `com.aitorgc.ahorragas` en `app/build.gradle.kts`.
> 2. **Firma de Producción:** Se generará una nueva clave de producción (`my-upload-key.jks` / `release.keystore`) mediante `keytool` con alias `upload` y cifrado RSA de 2048 bits para firmar el paquete de lanzamiento.
> 3. **Política de Privacidad:** Se creará el archivo `PRIVACY_POLICY.md` en la raíz del proyecto para GitHub y se integrará en la pantalla de Ajustes con un visor modal interactivo y un botón de enlace directo al repositorio.
> 4. **Visibilidad temporal en Git:** Siguiendo las instrucciones explícitas del usuario, la clave de firma y el paquete `.aab` permanecerán temporalmente accesibles para su descarga directa, y en un prompt posterior se añadirán al `.gitignore`.

---

## 1. Overview & Core Concept

- **Objetivo:** Cumplir al 100% las normativas y requisitos técnicos de **Google Play Developer Program** (Target SDK 34+, App Bundle optimizado, políticas de ubicación en primer plano y privacidad), permitiendo al desarrollador descargar el archivo `.aab` firmado para subirlo de inmediato a Google Play Console.
- **Transparencia y Privacidad:** Explicar con precisión el uso de la ubicación del dispositivo (acceso exclusivo en primer plano para listar estaciones cercanas y calcular distancias sin almacenamiento de historial de rutas ni rastreo en segundo plano) y el consumo de la API pública MITECO.
- **Seguridad en GitHub:** Garantizar que no se filtren contraseñas de servicios sensibles, configurando plantillas de entorno y documentación clara.

---

## 2. User Experience & Visual Design

- **Acceso a Política de Privacidad en la App:**
  - En la pantalla de **Ajustes** (`SettingsScreen`), dentro de la sección *Fuente de Datos & Legal*, se incorporará una tarjeta destacada con icono de seguridad (`Icons.Default.Security` / `Policy`).
  - Al pulsar, abrirá un diálogo modal (`PrivacyPolicyDialog`) que renderiza de forma limpia y legible el texto de la política de privacidad, con detalles sobre los permisos de ubicación GPS, almacenamiento local y derechos ARCO / GDPR.
  - Se incluirá un botón para "Abrir en navegador (GitHub)" mediante un `Intent` directo a la URL pública del repositorio.
- **Banner de Estado de Producción y Verificación:**
  - Notificación amigable en Ajustes informando que la compilación cumple con las directrices de Google Play.

---

## 3. Key Product Decisions & Trade-Offs

- **Decisión 1: Configuración de `applicationId` a `com.aitorgc.ahorragas`**
  - *Enfoque:* Mantener el `namespace = "com.example"` para respetar la estructura de paquetes y recursos R sin roturas de importación, modificando únicamente `defaultConfig { applicationId = "com.aitorgc.ahorragas" }`.
  - *Por qué:* Es la práctica recomendada oficial de Android y evita refactorizaciones masivas innecesarias que pudieran desestabilizar la aplicación.
- **Decisión 2: Generación de Keystore de Subida compatible con Gradle**
  - *Enfoque:* Generar `my-upload-key.jks` en la raíz del proyecto con alias `upload` y parámetros de validez extendida (25 años), enlazada con la configuración de firma `signingConfigs.release` existente en `app/build.gradle.kts`.
  - *Por qué:* Permite ejecutar la tarea oficial de Gradle `bundleRelease` firmando el `.aab` de forma reproducible.
- **Decisión 3: Generación de `PRIVACY_POLICY.md` conforme a RGPD y Google Play**
  - *Enfoque:* Redactar una política exhaustiva en español y estándar internacional que detalla la titularidad (Aitor Santana), la finalidad del permiso `ACCESS_FINE_LOCATION` y `ACCESS_COARSE_LOCATION`, la ausencia de SDKs publicitarios o rastreadores de terceros, y los datos no personales procesados.

---

## 4. Technical Architecture & Data Strategy

```
┌─────────────────────────────────────────────────────────────┐
│                    Google Play Console                      │
│                  Upload: app-release.aab                    │
│                 Package: com.aitorgc.ahorragas              │
│                Signed by: my-upload-key.jks                 │
└──────────────────────────────▲──────────────────────────────┘
                               │
               gradle :app:bundleRelease
                               │
┌──────────────────────────────┴──────────────────────────────┐
│                     AhorraGAS App                           │
│                                                             │
│  ┌─────────────────────────┐   ┌─────────────────────────┐  │
│  │   app/build.gradle.kts  │   │     PRIVACY_POLICY.md   │  │
│  │ - applicationId         │   │  (Raíz del repositorio) │  │
│  │ - targetSdk 36          │   └────────────▲────────────┘  │
│  │ - signingConfigs.release│                │               │
│  └─────────────────────────┘                │ Lee contenido │
│                                             │ o enlaza      │
│  ┌─────────────────────────┐   ┌────────────┴────────────┐  │
│  │     SettingsScreen      ├───►   PrivacyPolicyDialog   │  │
│  │  (Tarjeta Legal M3)     │   │ (Visor dentro de la app)│  │
│  └─────────────────────────┘   └─────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### Plan de Tareas de Ejecución

1. **Configuración de Gradle (`app/build.gradle.kts`)**:
   - Actualizar `applicationId` a `"com.aitorgc.ahorragas"`.
   - Verificar y asegurar que `signingConfigs.release` use la clave generada con alias `upload` y credenciales seguras.
2. **Generación de la Keystore de Producción**:
   - Crear `my-upload-key.jks` con `keytool` (RSA 2048 bits, validez de 10000 días, CN="Aitor Santana", OU="AhorraGAS", O="Aitor Santana", L="Las Palmas", ST="Canarias", C="ES").
   - Exportar certificado público SHA-1 y SHA-256 para verificación en Google Play Console.
3. **Redacción de `PRIVACY_POLICY.md`**:
   - Redactar el documento en la raíz del proyecto para GitHub con información del desarrollador (Aitor Santana, AitoR.GC89@gmail.com), permisos justificados, tratamiento de datos locales y fuentes oficiales.
4. **Visor de Política de Privacidad en Ajustes (`SettingsScreen`)**:
   - Crear `PrivacyPolicyDialog.kt` y agregar el botón correspondiente en la sección de información legal de `SettingsScreen.kt`.
5. **Auditoría de Seguridad y `.gitignore`**:
   - Verificar que no existan credenciales privadas o tokens expuestos en el código fuente.
   - Ajustar `.gitignore` para no bloquear temporalmente el `.aab` ni la keystore según la instrucción del usuario.
6. **Compilación y Generación del Paquete AAB**:
   - Ejecutar `gradle :app:bundleRelease` para generar el artefacto final `app/build/outputs/bundle/release/app-release.aab`.
   - Verificar firma e integridad del `.aab`.
