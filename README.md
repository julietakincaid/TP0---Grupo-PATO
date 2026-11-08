# Agencia de turismo "Rumbo Sur"

**TRABAJO PRÁCTICO OBLIGATORIO — GRUPO "PATO"**

Materia: Paradigma Orientado a Objetos · Facultad de Ingeniería y Ciencias Exactas
Turno: Viernes Mañana · 2.º cuatrimestre 2026 · Profesora: Maria Angela Leon

## Integrantes

| Integrante | Legajo |
|---|---|
| Callejón, Julieta Rocío | LU1208583 |
| Farias Hiriart, Celina | LU1207439 |
| Kincaid, Julieta | LU1216032 |

---

## 1. Descripción del sistema

"Rumbo Sur" es una agencia de turismo que organiza tours con salidas programadas. Este sistema reemplaza las planillas con las que la agencia manejaba reservas, cupos y pagos. Es una **aplicación de consola en Java**, con menús numerados, que guarda toda su información en un archivo.

El sistema cubre cuatro áreas:

- **Catálogo:** tours (cultural, de naturaleza o de aventura), itinerario por días, tarifas por temporada, servicios adicionales, proveedores, vehículos y personal.
- **Operaciones:** salidas de un tour en fechas concretas, con guía, vehículo y lista de pasajeros. Cada salida pasa por los estados *programada, confirmada, completa, en curso, finalizada* y *cancelada*.
- **Ventas:** clientes, reservas con uno o más pasajeros, promociones, pagos (seña y saldo), cancelaciones con devolución y notas de ajuste.
- **Administración:** liquidación de honorarios de los guías y reportes de ocupación, ventas, ingresos y cancelaciones.

### Roles

El sistema tiene cuatro roles. Cada uno ingresa con su legajo y ve solo su menú.

| Rol | Qué hace | Casos de uso |
|---|---|---|
| **Administrador** | Gestiona el catálogo y el personal, liquida honorarios y emite reportes | CU01 a CU09 |
| **Coordinador de operaciones** | Programa salidas, asigna guía y vehículo, emite la lista de pasajeros, inicia y finaliza salidas | CU10 a CU13 |
| **Vendedor** | Registra clientes, crea y modifica reservas, cobra pagos, cancela y ajusta reservas | CU14 a CU19 |
| **Guía** | Consulta sus salidas y la lista de pasajeros | CU20 |

### Reglas de negocio principales

- **Precios por pasajero:** adulto 100 % de la tarifa; menor de 12 años 60 %; menor de 3 años no paga; de 12 a 17 años tarifa completa; jubilado 10 % de descuento. La edad se calcula a la fecha de inicio de la salida.
- **Promociones:** 15 % por reservar con más de 60 días de anticipación y 10 % para grupos de seis o más pasajeros. Se aplica únicamente la más favorable.
- **Reservas:** se aceptan hasta 10 días antes de la salida. Se confirman al pagar una seña del 30 %. El saldo se paga hasta 10 días antes de la salida; si no, la reserva se anula y se liberan los lugares.
- **Pagos:** pueden hacerse en varios pagos, con distintos medios (efectivo, transferencia o tarjeta). Pagar con tarjeta en cuotas tiene un recargo del 7 %, que se cobra aparte del saldo.
- **Cancelaciones:** si cancela el cliente se devuelve el 90 % con 30 días o más de anticipación, el 50 % con 15 a 29 días, el 20 % con 7 a 14 días y nada con menos de 7 días. Si cancela la agencia se devuelve todo. La devolución se hace por el mismo medio de cada pago.
- **Salidas:** se confirman al alcanzar el cupo mínimo y quedan completas al alcanzar el máximo. Si siete días antes del inicio no alcanzan el mínimo, se cancelan. Para iniciar una salida debe estar confirmada, haber llegado su fecha de inicio, tener guía, vehículo y la lista de pasajeros emitida.
- **Tours de aventura:** tienen edad mínima y nivel de dificultad, y el seguro de viaje es obligatorio.
- **Honorarios del guía:** un monto fijo por día más el 5 % de lo vendido en servicios adicionales de la salida.

---

## 2. Requisitos

| Requisito | Detalle |
|---|---|
| **Java** | JDK 11 o superior. Se desarrolló con JDK 26 y no usa nada posterior a Java 11 |
| **IDE** (opcional) | IntelliJ IDEA, recomendado para ejecutar y para correr las pruebas |
| **Pruebas** | JUnit 5 (IntelliJ la agrega con un clic, ver sección 8) |
| **Terminal** | Con soporte UTF-8 y colores ANSI (la consola de IntelliJ y Windows Terminal lo tienen) |

Para ejecutar el programa no hace falta base de datos, conexión a internet ni bibliotecas externas.

---

## 3. Estructura del repositorio

```
/test       Pruebas unitarias (JUnit 5)
/docs       Informe, diagramas UML y plan de pruebas
README.md
datos/      Archivo de datos (se crea solo al ejecutar; no se versiona)
```

El código sigue el patrón **Modelo-Vista-Controlador** y separa la persistencia en un repositorio:

| Paquete | Responsabilidad |
|---|---|
| `modelo` | Las clases del negocio y sus reglas (`Reserva`, `Salida`, `Tour`, `Pasajero`, `Pago`, etc.) |
| `servicio` | `Agencia` (coordina el modelo) y los servicios de cálculo (precios, asignaciones, liquidación y reportes) |
| `controlador` | Un controlador por área. Controla el rol, delega en el modelo y atrapa los errores para mostrarlos |
| `vista` | La interfaz de consola. Solo lee lo que escribe el usuario y muestra resultados |
| `repositorio` | Guarda y carga el estado de la agencia en un archivo |
| `excepcion` | Las excepciones propias del negocio |

Además, `Principal.java` (punto de entrada) y `DatosIniciales.java` (datos de prueba) están en la raíz de `/src`.

---

## 4. Instalación y ejecución

### Opción A: IntelliJ IDEA (recomendada)

1. Clonar el repositorio y abrir la carpeta con **File → Open**.
2. Comprobar que `src` esté marcada como *Sources Root* y `test` como *Test Sources Root* (clic derecho sobre la carpeta → **Mark Directory as**).
3. Abrir `src/Principal.java` y ejecutarlo con el botón verde de **Run**.

### Opción B: desde la terminal

Ubicarse en la **raíz del repositorio** (así el archivo de datos se crea ahí) y ejecutar:

**Linux / macOS**

```
mkdir out
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -Dfile.encoding=UTF-8 -cp out Principal
```

**Windows (PowerShell)**

```
New-Item -ItemType Directory -Force out
javac -encoding UTF-8 -d out (Get-ChildItem -Path src -Recurse -Filter *.java).FullName
java -Dfile.encoding=UTF-8 -cp out Principal
```

**Windows (cmd)**

```
mkdir out
dir /s /b src\*.java > fuentes.txt
javac -encoding UTF-8 -d out @fuentes.txt
java -Dfile.encoding=UTF-8 -cp out Principal
```

Si en la terminal se ven mal los acentos, ejecutar antes `chcp 65001`.

### Opciones del programa

| Opción | Efecto |
|---|---|
| `--sin-color` | Muestra todo sin colores (útil si la terminal no soporta ANSI). Se agrega al final del comando, por ejemplo `java -cp out Principal --sin-color` |

### Primera ejecución y archivo de datos

- La primera vez que se ejecuta, el programa **carga los datos de prueba** (sección 6) y avisa con un cartel amarillo. Los datos se guardan en `datos/agencia.dat`.
- Después de cada operación el programa guarda los datos, así que al volver a abrirlo todo sigue como estaba.
- Para **volver a empezar** con los datos de prueba, cerrar el programa y borrar el archivo `datos/agencia.dat`.
- Si se modifica alguna clase del modelo después de haber guardado datos, el archivo viejo puede dejar de ser compatible. En ese caso el programa lo avisa y se cierra sin pisar nada: borrar `datos/agencia.dat` lo soluciona.
- Las fechas de los datos de prueba se calculan a partir del día de la **primera ejecución**.

---

## 5. Cómo se usa

1. Al abrir el programa se elige **Ingresar con mi legajo** y se escribe el legajo (ver la tabla de la sección 6). El ingreso es por legajo, sin contraseña.
2. Cada rol ve su menú. Se elige una opción escribiendo su número; con **0** se vuelve o se cierra la sesión.
3. Las fechas se escriben como `dd/mm/aaaa` (con barras) y las horas como `hh:mm`.
4. Si algo no se puede hacer (por ejemplo, un pago mayor al saldo), el programa lo avisa en rojo con el motivo y no pierde nada.

### Menús y casos de uso

| Caso de uso | Rol | Dónde está |
|---|---|---|
| CU01 Gestionar tours | Administrador | Tours (ver, registrar nuevo, modificar, dar de baja) |
| CU02 Gestionar itinerario | Administrador | Itinerario de un tour |
| CU03 Gestionar proveedores | Administrador | Proveedores |
| CU04 Gestionar tarifas | Administrador | Tarifas de un tour |
| CU05 Gestionar servicios adicionales | Administrador | Servicios adicionales |
| CU06 Gestionar vehículos | Administrador | Vehículos |
| CU07 Gestionar personal | Administrador | Personal (ver, registrar nuevo, gestionar guías, modificar datos) |
| CU08 Liquidar honorarios del guía | Administrador | Liquidar los honorarios de un guía |
| CU09 Emitir reportes | Administrador | Reportes (ocupación, ventas, ingresos, cancelaciones) |
| CU10 Programar salida | Coordinador | Programar una salida |
| CU11 Asignar guía y vehículo | Coordinador | Asignar un guía / Asignar un vehículo a una salida |
| CU12 Iniciar salida | Coordinador | Iniciar una salida (emite la lista de pasajeros si falta) |
| CU13 Finalizar salida | Coordinador | Finalizar una salida |
| CU14 Registrar cliente | Vendedor | Clientes → Registrar un cliente |
| CU15 Crear reserva | Vendedor | Reservas → Crear una reserva |
| CU16 Registrar pago | Vendedor | Pagos → Registrar un pago |
| CU17 Modificar reserva | Vendedor | Reservas → Modificar una reserva |
| CU18 Cancelar reserva | Vendedor | Cancelar una reserva |
| CU19 Registrar nota de ajuste | Vendedor | Registrar una nota de ajuste |
| CU20 Consultar lista de pasajeros | Guía | Ver la lista de pasajeros de una salida |

---

## 6. Datos de prueba

Al ejecutar por primera vez se carga un conjunto de datos que incluye de todo un poco, con reservas y salidas en distintos estados.

### Personal (se ingresa con el legajo)

| Legajo | Rol | Nombre | Detalle |
|---|---|---|---|
| **1** | Administrador | Camila Torres | |
| **2** | Coordinador de operaciones | Santiago Mena | |
| **3** | Vendedor | Pablo Herrera | Comisión 5 % |
| **5** | Vendedor | Valeria Suárez | Comisión 8 % |
| **4** | Guía | Julia Navarro | Tours culturales · español · $1.000 por día |
| **6** | Guía | Martín Ortiz | Aventura y naturaleza · español e inglés · $1.500 por día |
| **7** | Guía | Lucía Romero | Naturaleza · español y portugués · $1.200 por día |

### Catálogo

| Código | Tour | Categoría | Días | Cupos | Tarifa base (baja / media / alta) | Detalle |
|---|---|---|---|---|---|---|
| CUL-01 | Buenos Aires cultural | Cultural | 3 | 2 a 10 | $100 / $150 / $200 | Seis actividades, algunas con proveedor |
| NAT-01 | Sierras y lagos de Córdoba | Naturaleza | 4 | 4 a 20 | $180 / $220 / $280 | |
| NAT-02 | Cataratas del Iguazú | Naturaleza | 3 | 3 a 15 | $250 / $300 / $350 | **Sin salidas**: sirve para probar cambios de duración, cupos y baja, y para programar una salida nueva |
| AVE-01 | Rafting en el Río Bravo | Aventura | 2 | 2 a 8 | $300 / $350 / $400 | Edad mínima 14 · nivel Alta · seguro obligatorio |

- **Proveedores:** Hotel Sur (CUIT 30-71234567-8), Bodega Los Andes (30-68765432-1) y Aventura Río Bravo (30-70123456-5).
- **Servicios adicionales:** Seguro de viaje ($50), Excursión a la bodega ($30) y Traslado al aeropuerto ($20).
- **Vehículos:** combi `AB123CD` (12 lugares), micro `AC456EF` (30) y camioneta `AD789GH` (6).
- **Clientes** (se identifican por documento): Ana Pérez `30111222`, Beto Gómez `31222333`, Carla Ruiz `32333444`, Diego Fernández `33444555`, Elena Martínez `34555666` y Facundo López `35666777`.

### Salidas

| N.º | Tour | Empieza | Estado | Guía y vehículo | Para qué sirve |
|---|---|---|---|---|---|
| 1 | CUL-01 | En 65 días | Confirmada (9 de 10 lugares) | Julia Navarro y combi | Promoción por anticipación, un grupo de 6, saldo pendiente y una reserva pendiente |
| 2 | NAT-01 | En 90 días | Programada (2 de 20) | Sin asignar | No alcanzó el cupo mínimo; el coordinador puede asignarle guía y vehículo |
| 3 | AVE-01 | En 45 días | Confirmada (2 de 8) | Martín Ortiz y micro | Seguro obligatorio incluido |
| 4 | CUL-01 | Hace 25 días | **Finalizada** | Julia Navarro y combi | Pendiente de liquidar; tiene una reserva anulada |
| 5 | CUL-01 | En 40 días | Confirmada (6 de 10) | Sin asignar | Promoción por grupo |
| 6 | CUL-01 | **Hoy** | Confirmada (2 de 10) | Julia Navarro y combi | Con la lista sin emitir: se puede emitir, iniciar y finalizar para ver el ciclo completo |

### Reservas

| N.º | Cliente | Salida | Pasajeros | Estado y situación |
|---|---|---|---|---|
| 1 | Beto Gómez | 1 | 6 | Confirmada, paga completa (parte con tarjeta en 3 cuotas, con recargo) |
| 2 | Ana Pérez | 1 | 2 | Confirmada, con una excursión, con saldo pendiente |
| 3 | Carla Ruiz | 1 | 1 | **Pendiente** (10 % pagado) |
| 4 | Diego Fernández | 2 | 2 | Confirmada (35 % pagado) |
| 5 | Elena Martínez | 3 | 2 | Confirmada, paga completa, con seguro obligatorio |
| 6 | Facundo López | 4 | 2 | Finalizada |
| 7 | Ana Pérez | 4 | 1 | **Anulada** por el cliente, con una devolución de $50 |
| 8 | Facundo López | 5 | 6 | Confirmada, con promoción por grupo (10 %) |
| 9 | Diego Fernández | 6 | 2 | Confirmada, paga completa |

### Recorrido sugerido

1. **Administrador (legajo 1):** Tours → Ver los tours (la columna *Salidas vigentes* indica cuáles se pueden modificar). Liquidar los honorarios de la salida 4 y mirar los reportes.
2. **Coordinador (legajo 2):** asignar guía y vehículo a la salida 5; en la salida 6, iniciarla y finalizarla. Probar iniciar la salida 1: el sistema la rechaza porque todavía no llegó su fecha de inicio.
3. **Vendedor (legajo 3):** Pagos → Registrar un pago (se ofrecen solo las reservas con saldo); Reservas → Crear una reserva con dos pasajeros para la cliente `30111222` en la salida 2.
4. **Guía (legajo 4):** ver sus salidas y la lista de pasajeros de la salida 4.

---

## 7. Supuestos y decisiones principales

Todo dato necesario que no figuraba en el enunciado se completó con supuestos. Los principales:

- El ingreso al sistema es por **legajo, sin contraseña**; no forma parte de los 20 casos de uso y se agregó como apoyo.
- Se considera **jubilado** a partir de los 60 años. Se considera **menor de edad** a quien tiene menos de 18.
- La ocupación de una salida cuenta los pasajeros de las reservas **pendientes, confirmadas y finalizadas**; una reserva anulada libera sus lugares.
- Una salida **no puede iniciarse antes de su fecha de inicio**; sí el mismo día o después.
- No se pueden quitar pasajeros ni servicios de una reserva que ya tiene pagos: para corregirla se usa una **nota de ajuste**.
- Las promociones se calculan sobre el total (tarifas más servicios). El recargo de la tarjeta se cobra aparte y también se devuelve al cancelar.
- La persistencia se hace por **serialización de objetos** en un único archivo (`datos/agencia.dat`), que se escribe primero a un archivo temporal para no perder datos si algo falla.

La lista completa de supuestos está en el informe (`/docs`).

---

## 8. Pruebas

### Pruebas unitarias

Están en la carpeta `/test`, escritas con **JUnit 5**: 98 pruebas sobre las clases con lógica de negocio (reservas, salidas, tours, políticas de cancelación, asignaciones, liquidación, reportes, agencia y controladores).

Para ejecutarlas desde IntelliJ:

1. Comprobar que `test` esté marcada como *Test Sources Root*.
2. Si `@Test` aparece en rojo, parar el cursor encima, apretar `Alt+Enter` y elegir **Add 'JUnit5.x' to classpath** (se hace una sola vez).
3. Clic derecho sobre la carpeta `test` → **Run 'All Tests'**.

### Plan de pruebas funcionales

Los casos de prueba funcionales (dato de entrada, resultado esperado y resultado obtenido) están en `/docs`.
