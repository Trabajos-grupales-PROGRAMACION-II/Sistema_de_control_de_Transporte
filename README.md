# Sistema de Gestión y Monitoreo de Transporte

Proyecto de la asignatura **Programación II (ISWZ1103)** · Ingeniería de Software · Universidad de las Américas.

**Grupo 11:** Anthony Salazar (líder), Juan Rojas, Kevin Coello, Hillary Quevedo.

Aplicación en Java, por ahora de consola, para una operadora de buses. Permite administrar usuarios, rutas y unidades, seguir la ubicación de cada unidad durante su recorrido, estimar cuánto tardará en llegar a cada parada y detectar automáticamente desvíos y detenciones inusuales. La ubicación GPS se simula (un envío cada 5 segundos) y los datos se mantienen en memoria mientras el programa está abierto.

## Módulos

| Módulo | Responsable | Requerimientos |
| --- | --- | --- |
| 1. Usuarios y conductores | Anthony Salazar | RF1 Inicio de sesión y control de acceso · RF2 Registro y estado de usuarios · RF3 Control de jornada de conductores |
| 2. Rutas y unidades | Juan Rojas | RF4 Registro de rutas y paradas · RF5 Registro y asignación de unidades |
| 3. Monitoreo y llegada | Kevin Coello | RF6 Recorrido con envío de ubicación · RF7 Estado de la flota · RF8 Tiempo estimado de llegada |
| 4. Anomalías y alertas | Hillary Quevedo | RF9 Detección de desvío de ruta · RF10 Detección de detención inusual · RF11 Reporte de cumplimiento |

## Estructura

```
SistemaTransporte/src
├── modelo/     Clases del dominio: Usuario (abstracta), Administrador, Conductor, Pasajero,
│               Ruta, Parada, Unidad, Recorrido, Ubicacion, Anomalia y los enums
│               EstadoUnidad y TipoAnomalia
├── negocio/    Gestores con las reglas de negocio, cálculos geográficos (Haversine),
│               estimación de llegada, reportes, reloj simulado y ValidacionException
└── interfaz/   Main y los menús de consola por módulo y por rol
docs/
├── Informe_Avance2_Grupo11.pdf
└── diagrama_uml.png
```

## Cómo ejecutar

Requiere Java 8 o superior.

```bash
cd SistemaTransporte
javac -d out $(find src -name "*.java")
java -cp out interfaz.Main
```

También se puede abrir la carpeta `SistemaTransporte` en NetBeans, IntelliJ o Eclipse y ejecutar `interfaz.Main`.

## Usuarios de prueba

| Rol | Correo | Contraseña | Observación |
| --- | --- | --- | --- |
| Administrador | admin@transporte.ec | Admin2026 | Acceso a los 4 módulos |
| Conductor | carlos@transporte.ec | Conductor1 | Unidad PBA-1234, ruta R1 |
| Conductor | maria@transporte.ec | Conductor2 | Unidad PBB-5678, ruta R1 |
| Conductor | luis@transporte.ec | Conductor3 | Licencia vencida (para probar la validación) |
| Pasajero | sofia@correo.ec | Pasajero1 | Consulta de unidades y tiempos de llegada |

Ruta de prueba **R1 "Ruta Norte - UDLA"**: Terminal Norte → Parada intermedia → UDLA Park. La unidad PBC-9012 está libre para asignarla.

## Reglas de negocio principales

- La cuenta se bloquea 15 minutos tras 5 intentos fallidos; las contraseñas se guardan cifradas con SHA-256.
- Un conductor no puede superar 8 horas de conducción al día (límite configurable).
- La placa tiene el formato ABC-1234 y no se repite; no se asigna una unidad a un conductor con licencia vencida.
- Cada unidad envía su ubicación cada 5 segundos; con más de 30 segundos sin ubicación queda "Sin señal".
- Tiempo estimado de llegada = distancia restante sobre la ruta ÷ velocidad promedio de los últimos 2 minutos.
- Alerta de desvío si la unidad se aleja más de 100 m del trazado; alerta de detención si permanece 5 minutos o más fuera de una parada.

## Otros archivos

`Index.html`, `App.js` y `Styles.css` corresponden al prototipo web del primer avance. Siguiendo las observaciones de la docente sobre el alcance, la solución se delimitó a una aplicación en Java.
