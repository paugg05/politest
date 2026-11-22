# figure-service (microservicio de personajes históricos)

Proyecto Spring Boot INDEPENDIENTE de la web (`politest`) y de `party-service`. Tiene su propia base de datos
(`figure-db`, puerto 3308) y expone una API REST en el puerto 8091.

## Cómo arrancarlo
1. Importa esta carpeta en Eclipse (*File → Import → Maven → Existing Maven Projects*).
2. Base de datos propia: en esta carpeta, `docker compose up -d`
   (phpMyAdmin de este servicio: http://localhost:8084, usuario `figures`, contraseña `figures`).
3. Ejecuta `FigureServiceApplication`. Flyway crea las tablas y carga 20 personajes con textos en es, ca y en.
4. Prueba:
   - http://localhost:8091/api/v1/figures?lang=es
   - `POST http://localhost:8091/api/v1/affinity?lang=ca` con
     `{"scores":{"ECON":78,"WELFARE":71,"LIBERTY":22,"CULTURE":48,"NATION":35,"ECOLOGY":10,"DEMOCRACY":55,"CHANGE":60}}`
   - http://localhost:8091/actuator/health

## Puertos de todo el proyecto
Web 8080 · party-service 8090 · figure-service 8091 · phpMyAdmin: web 8081, party 8083, figure 8084 ·
MySQL: web 3306, party 3307, figure 3308.

## Cómo añadir o corregir personajes
Crea `V3__...sql` con `INSERT` en `figure`, `figure_text` (una fila por idioma) y `figure_position`
(un eje sin fila = no era un tema de su época; el cálculo lo ignora). Nunca edites V1 ni V2 una vez aplicadas.

## Importante
Los datos son PROVISIONALES: estimaciones que simplifican figuras históricas complejas. Incluyen dictaduras a propósito,
y la web avisa de que la afinidad se mide solo en los 8 ejes y no implica apoyo a sus actos.
