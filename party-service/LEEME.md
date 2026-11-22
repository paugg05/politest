# party-service (microservicio de afinidad con partidos)

Proyecto Spring Boot INDEPENDIENTE de la web (carpeta `politest`). Tiene su propia base de datos (`party-db`, puerto 3307)
y expone una API REST en el puerto 8090.

## Cómo arrancarlo
1. Crea un proyecto Maven nuevo en Eclipse con el mismo Java 25 (o importa esta carpeta como "Existing Maven Project").
   Si falta el wrapper, copia `mvnw`, `mvnw.cmd` y `.mvn/` desde tu proyecto `politest`.
2. Base de datos propia: en esta carpeta, `docker compose up -d`
   (phpMyAdmin de este servicio: http://localhost:8083, usuario `parties`, contraseña `parties`).
3. Ejecuta `PartyServiceApplication`. Flyway crea las tablas y carga los 5 partidos.
4. Prueba:
   - http://localhost:8090/api/v1/parties
   - `POST http://localhost:8090/api/v1/affinity` con `{"scores":{"ECON":78,"WELFARE":71,"LIBERTY":22,"CULTURE":48,"NATION":35,"ECOLOGY":10,"DEMOCRACY":55,"CHANGE":60}}`
   - http://localhost:8090/actuator/health

## Cómo añadir un partido más adelante
Crea `V3__add_<partido>.sql` con un `INSERT` en `party` y 8 filas en `party_position` (nunca edites V1 ni V2 una vez aplicadas).
Para corregir una posición: otra migración con `UPDATE` y, si cambia la versión de los datos, `UPDATE dataset_info`.

## Importante
Los datos son PROVISIONALES: estimaciones con fuente y nivel de confianza (1 baja, 2 media, 3 alta). Los programas oficiales
del 29-N-2026 no estaban publicados al crearlos.
