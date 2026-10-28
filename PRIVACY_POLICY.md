# Política de Privacidad de AhorraGAS

**Última actualización:** 26 de octubre de 2026  
**Aplicación:** AhorraGAS (identificador: `com.aitorgc.ahorragas`)  
**Desarrollador:** Aitor Santana  
**Contacto de privacidad:** AitoR.GC89@gmail.com  

---

## 1. Introducción y Compromiso de Privacidad

La presente Política de Privacidad describe cómo **AhorraGAS** recopila, utiliza y protege la información del usuario al utilizar nuestra aplicación móvil para dispositivos Android. 

En **AhorraGAS**, nos comprometemos con la privacidad desde el diseño (*Privacy by Design*). Nuestra prioridad es que puedas ahorrar en el repostaje de tu vehículo sin comprometer tus datos personales ni tu privacidad.

---

## 2. Datos y Permisos Recopilados

### 2.1. Ubicación Geográfica del Dispositivo (`ACCESS_FINE_LOCATION` y `ACCESS_COARSE_LOCATION`)
- **Finalidad:** La aplicación solicita acceso a tu ubicación precisa y aproximada **exclusivamente en primer plano** y cuando el usuario lo solicita explícitamente (mediante el botón de detección GPS o al ordenar las gasolineras por cercanía).
- **Procesamiento On-Device:** Las coordenadas obtenidas se utilizan en el propio dispositivo para calcular la distancia en kilómetros a las estaciones de servicio y determinar la provincia/isla correspondiente.
- **Sin rastreo ni almacenamiento:** La aplicación **NO** rastrea tu ubicación en segundo plano, **NO** guarda tu historial de movimientos o rutas, y **NO** envía tus coordenadas geográficas a ningún servidor externo ni a terceros.

### 2.2. Conexión a Internet (`INTERNET` y `ACCESS_NETWORK_STATE`)
- **Finalidad:** Se requiere para consultar en tiempo real los precios actualizados de los carburantes en las estaciones de servicio de España a través de los servicios de datos abiertos del **Ministerio para la Transición Ecológica y el Reto Demográfico (MITECO)** del Gobierno de España.
- **Consultas anónimas:** Las peticiones a la API oficial de datos abiertos se realizan filtrando por provincia o zona territorial, sin enviar ningún identificador de usuario, publicidad ni dato personal.

### 2.3. Notificaciones (`POST_NOTIFICATIONS`)
- **Finalidad:** En dispositivos con Android 13 (API 33) o superior, la app puede solicitar permiso para enviarte alertas locales cuando una gasolinera marcada como favorita baje de precio.
- **Gestión:** Puedes desactivar o activar este permiso en cualquier momento desde los ajustes del sistema operativo Android.

---

## 3. Almacenamiento Local de Datos

Toda la información introducida por el usuario dentro de AhorraGAS se almacena de forma **estrictamente local** en la memoria del dispositivo (mediante base de datos SQLite cifrada por el sistema operativo Android):
- Lista de gasolineras favoritas.
- Registro de vehículos (marca, modelo, combustible).
- Historial de gastos y repostajes.
- Preferencias de la aplicación (combustible habitual, zona geográfica y tema visual).

**Ninguno de estos datos se envía a servidores en la nube ni se sincroniza con bases de datos externas.**

---

## 4. Ausencia de Rastreadores y Publicidad de Terceros

- **Sin publicidad:** AhorraGAS no contiene anuncios ni integra redes de publicidad (como Google AdMob, Unity Ads, etc.).
- **Sin SDKs de seguimiento:** No utilizamos herramientas de telemetría invasiva, analítica de comportamiento ni perfiles de usuario de terceros (como Facebook SDK, AppsFlyer o similares).

---

## 5. Derechos del Usuario (RGPD / GDPR)

Conforme al Reglamento General de Protección de Datos (RGPD) de la Unión Europea y normativas aplicables:
- **Control total:** Dado que los datos residen únicamente en tu dispositivo, puedes rectificar o eliminar cualquier registro directamente desde la interfaz de la aplicación.
- **Borrado completo:** Puedes restablecer y eliminar por completo todos los datos almacenados desinstalando la aplicación o borrando el almacenamiento local desde:  
  *Ajustes del Sistema > Aplicaciones > AhorraGAS > Almacenamiento > Borrar datos*.

---

## 6. Seguridad de la Información

Implementamos medidas técnicas de seguridad estándar en la plataforma Android para proteger la integridad de los datos locales y asegurar que las conexiones de consulta de precios a la API del Ministerio se efectúen mediante protocolos cifrados y seguros (`HTTPS / TLS`).

---

## 7. Cambios en esta Política

Nos reservamos el derecho de actualizar esta política para reflejar mejoras en la aplicación o cambios legislativos. Cualquier cambio sustancial será publicado en el repositorio de GitHub y reflejado en el visor de privacidad dentro de la aplicación.

---

## 8. Contacto

Si tienes cualquier duda, sugerencia o consulta sobre la presente Política de Privacidad o el tratamiento de datos en AhorraGAS, puedes contactar directamente con el desarrollador:

- **Responsable:** Aitor Santana
- **Correo electrónico:** [AitoR.GC89@gmail.com](mailto:AitoR.GC89@gmail.com)
