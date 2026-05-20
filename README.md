# OpsFlow Backend - Plataforma SaaS

## Stack tecnológico

- **Lenguaje:** `Java 21`
- **Framework:** `Spring Boot 3.4.x`
- **Seguridad:** `Spring Security 6` + `JWT`
- **Persistencia:** `PostgreSQL 15`
- **Mensajería:** `RabbitMQ 3.12`
- **Caché y refresh tokens:** `Redis 7`
- **Service discovery:** `Netflix Eureka Server`
- **API Gateway:** `Spring Cloud Gateway`

---

## Arquitectura del backend

OpsFlow está compuesto por los siguientes módulos:

| Módulo | Puerto | Responsabilidad | Rutas principales |
| :--- | :---: | :--- | :--- |
| `gateway_service` | `8080` | Punto único de entrada | `/auth/**`, `/auth-legacy/**`, `/users/**`, `/org/**`, `/documents/**` |
| `auth_service` | `8081` | Autenticación, JWT, usuarios, roles y permisos | `/auth/**`, `/users/**`, `/auth-legacy/**` |
| `org_service` | `8082` | Organizaciones y sedes | `/org/**` |
| `document_service` | `8083` | Gestión documental, versiones y descargas | `/documents/**` |
| `eureka_server` | `8761` | Registro y descubrimiento de servicios | Dashboard Eureka |
| `common` | N/A | Librería compartida | Sin endpoints REST |

### Rutas publicadas por el gateway

El acceso recomendado desde clientes externos es a través del gateway en `http://localhost:8080`.

- `/auth/**` -> `auth_service`
- `/auth-legacy/**` -> `auth_service`
- `/users/**` -> `auth_service`
- `/org/**` -> `org_service`
- `/documents/**` -> `document_service`

Todo el tráfico externo debe entrar por el gateway (`http://localhost:8080`); no expongas los puertos internos (`8081`, `8082`, etc.) a clientes.

### Rate limiting (endpoints públicos de auth)

El gateway aplica `RequestRateLimiter` (Spring Cloud Gateway + Redis) en rutas sensibles antes del enrutado general:

- `/auth/login`, `/auth/signup`, `/auth/verify`, `/auth/forgot-password`, `/auth/reset-password`, `/auth/refresh`
- `/auth-legacy/login`, `/auth-legacy/signup`, `/auth-legacy/refresh`

Límite por IP: **2 peticiones/s** con ráfaga de **5** (`429 Too Many Requests` si se excede). Redis debe estar activo (`redis-cache` en Docker, puerto `6379`).

### Seguridad JWT en el gateway

El `gateway_service` valida el JWT en todas las rutas excepto las públicas de autenticación y documentación OpenAPI. Si el token es válido, reenvía la petición al microservicio destino con cabeceras internas:

- `X-User-Id`
- `X-Org-Id`
- `X-User-Role`

Los microservicios siguen validando el `Authorization: Bearer <token>` en `auth_service` (y sus equivalentes en org/document). El secreto JWT (`app.jwt.secret`) debe coincidir entre **gateway** y **auth_service** (módulo `common` / `JwtUtils`).

Rutas públicas en el gateway (sin JWT): login, signup, verify, refresh, forgot/reset password, Swagger/OpenAPI y rutas análogas en `/auth-legacy/**`.

---

## Guía de inicio rápido

Sigue estos pasos en orden para levantar el ecosistema completo correctamente.

### 1. Levantar la infraestructura base

Es fundamental que la base de datos `db_opsflow` y los servicios auxiliares estén operativos antes de iniciar los microservicios.

```bash
docker compose up -d postgres-db redis-cache rabbitmq-broker
```

*(También válido: `docker-compose up -d ...` en instalaciones antiguas de Docker Compose.)*

### 2. Orden de ejecución de microservicios

1. `eureka_server` en `8761`
2. `auth_service` en `8081`
3. `org_service` en `8082`
4. `document_service` en `8083`
5. `gateway_service` en `8080`

### 3. Usuarios semilla (desarrollo)

Tras el primer arranque de `auth_service`, `DataInitializer` crea usuarios de prueba (contraseña **`123456`**):

| Usuario | Rol | Email |
| :--- | :--- | :--- |
| `admin` | `ROLE_ADMIN` | `admin@opsflow.com` |
| `manager` | `ROLE_MANAGER` | `manager@opsflow.com` |
| `user` | `ROLE_USER` | `user@opsflow.com` |

Login vía gateway: `POST http://localhost:8080/auth/login` con `{"username":"admin","password":"123456"}`.

### 4. Frontend (prueba end-to-end)

El cliente Angular (`opsflow-frontend`) usa el proxy `/api` → `http://localhost:8080`. Arranca con `npm run dev` en el puerto **3000** después de tener el backend operativo.

### 5. Troubleshooting rápido de puertos en Windows

```bash
# Ver el proceso que ocupa un puerto
netstat -ano | findstr :9000

# Finalizar el proceso
taskkill /F /PID [NUMERO_DE_PID]
```

---

## Monitoreo y estado de los servicios

Para verificar que los servicios se registraron correctamente en Eureka:

- **Dashboard:** [http://localhost:8761/](http://localhost:8761/)
- **Instancias esperadas:**
  - `AUTH-SERVICE`
  - `ORG-SERVICE`
  - `DOCUMENT-SERVICE`
  - `GATEWAY-SERVICE`

Si un servicio no aparece, revisa primero su conectividad hacia la base de datos, Redis o RabbitMQ.

---

## Roles y modelo de autorización

### Roles base del sistema

| Rol | Alcance funcional | Endpoints representativos |
| :--- | :--- | :--- |
| `ROLE_ADMIN` | Acceso global al ecosistema. Administra usuarios, roles, permisos, organizaciones, sedes y operaciones administrativas de documentos. | `/auth/roles/**`, `/auth/permissions`, `/users/**`, `/org/**`, `/documents/{id}/force-state`, `/documents/storage/list` |
| `ROLE_MANAGER` | Administración acotada a su organización. Puede invitar usuarios `ROLE_USER`, consultar usuarios de su organización, actualizar su organización, gestionar sedes propias y operar documentos de su organización. | `/users/my-organization`, `/users/by-organization/{orgId}`, `POST /users`, `PUT /org/{id}`, `/org/locations/**`, `/documents/**` |
| `ROLE_USER` | Rol operativo. Puede autenticarse, cambiar su contraseña, consultar su organización y sus sedes, crear y operar documentos de su organización dentro de las reglas de propiedad. | `/auth/logout`, `/auth/me/permissions`, `/users/change-password`, `/org/{id}`, `/org/locations/by-org/{orgId}`, `/documents/create`, `/documents/{id}` |

### Notas importantes sobre autorización

- Los roles semilla reales del sistema son `ROLE_ADMIN`, `ROLE_MANAGER` y `ROLE_USER`.
- Se pueden crear roles personalizados desde `POST /auth/roles/create`, pero la mayor parte de las reglas de acceso actuales sigue validándose por roles fijos o por lógica de negocio.
- `auth_service` combina reglas de `SecurityConfig` con `@PreAuthorize`.
- `org_service` utiliza reglas dinámicas por organización y, además, permite configurar algunos roles desde `org_service/src/main/resources/application.yml`.
- `document_service` aplica permisos por rol y también valida pertenencia a la organización o propiedad del documento.
- Los endpoints `PATCH /org/{id}/activate` y `PATCH /org/{id}/deactivate` quedan para `ROLE_ADMIN` por defecto, pero también pueden habilitarse mediante las authorities `ORG_ACTIVATE` y `ORG_DEACTIVATE`.

---

## Inventario de endpoints por microservicio

La siguiente matriz refleja los endpoints definidos en el código backend y el rol o condición de acceso efectiva.

### 1. Auth Service

Servicio responsable de autenticación, sesiones, usuarios, roles y permisos.

#### 1.1 Autenticación y sesión

| Acción | Método | Endpoint | Acceso |
| :--- | :---: | :--- | :--- |
| Login principal | `POST` | `/auth/login` | Público |
| Registro de usuario | `POST` | `/auth/signup` | Público |
| Verificación de correo | `GET` | `/auth/verify?token=...` | Público |
| Solicitud de recuperación de contraseña | `POST` | `/auth/forgot-password` | Público |
| Restablecer contraseña | `POST` | `/auth/reset-password` | Público |
| Refrescar access token | `POST` | `/auth/refresh` | Público con refresh token válido |
| Cerrar sesión | `POST` | `/auth/logout` | Usuario autenticado |
| Ver permisos efectivos del usuario actual | `GET` | `/auth/me/permissions` | Usuario autenticado |
| Login legado | `POST` | `/auth-legacy/login` | Público |
| Registro legado | `POST` | `/auth-legacy/signup` | Público |
| Refresh legado | `POST` | `/auth-legacy/refresh` | Público con refresh token válido |
| Logout legado | `POST` | `/auth-legacy/logout` | Usuario autenticado |

#### 1.2 Roles y permisos

| Acción | Método | Endpoint | Acceso |
| :--- | :---: | :--- | :--- |
| Listar catálogo de permisos | `GET` | `/auth/permissions` | `ROLE_ADMIN` |
| Listar roles | `GET` | `/auth/roles` | `ROLE_ADMIN` |
| Obtener rol por ID | `GET` | `/auth/roles/{id}` | `ROLE_ADMIN` |
| Crear rol | `POST` | `/auth/roles/create` | `ROLE_ADMIN` |
| Actualizar rol | `PUT` | `/auth/roles/{id}` | `ROLE_ADMIN` |
| Eliminar rol | `DELETE` | `/auth/roles/{id}` | `ROLE_ADMIN` |
| Ver permisos asignados a un rol | `GET` | `/auth/roles/{id}/permissions` | `ROLE_ADMIN` |
| Reemplazar permisos de un rol | `PUT` | `/auth/roles/{id}/permissions` | `ROLE_ADMIN` |
| Reemplazar todos los roles de un usuario por uno solo | `PUT` | `/auth/roles/users/{userId}/change-role` | `ROLE_ADMIN` |
| Reemplazar todos los roles de un usuario por una lista | `PUT` | `/auth/roles/users/{userId}/roles` | `ROLE_ADMIN` |

#### 1.3 Usuarios

| Acción | Método | Endpoint | Acceso |
| :--- | :---: | :--- | :--- |
| Resolver perfiles públicos por lote | `POST` | `/users/profiles/batch` | Usuario autenticado |
| Listar todos los usuarios | `GET` | `/users` | `ROLE_ADMIN` |
| Listar usuarios de mi organización | `GET` | `/users/my-organization` | `ROLE_ADMIN` o `ROLE_MANAGER` |
| Listar usuarios por organización | `GET` | `/users/by-organization/{orgId}` | `ROLE_ADMIN` o `ROLE_MANAGER` de esa misma organización |
| Obtener usuario por ID | `GET` | `/users/{id}` | `ROLE_ADMIN` |
| Crear o invitar usuario | `POST` | `/users` | `ROLE_ADMIN` o `ROLE_MANAGER` |
| Actualizar usuario | `PUT` | `/users/{id}` | `ROLE_ADMIN` |
| Desasociar usuario de una organización | `PUT` | `/users/{id}` con body `{"clearOrganization": true}` | `ROLE_ADMIN` |
| Desasociar usuario (alternativa) | `PUT` / `PATCH` | `/users/{id}/detach-organization` | `ROLE_ADMIN` |
| Reemplazar roles del usuario | `PATCH` | `/users/{id}/roles` | `ROLE_ADMIN` |
| Asignar o cambiar manager de una organización | `PUT` | `/users/organizations/{orgId}/manager/{userId}` | `ROLE_ADMIN` |
| Asignar o cambiar manager de una organización | `PATCH` | `/users/organizations/{orgId}/manager/{userId}` | `ROLE_ADMIN` |
| Cambiar mi contraseña | `PATCH` | `/users/change-password` | Usuario autenticado |
| Desactivar usuario | `PATCH` | `/users/{id}/deactivate` | `ROLE_ADMIN` |
| Activar usuario | `PATCH` | `/users/{id}/activate` | `ROLE_ADMIN` |
| Revocar sesión de usuario | `POST` | `/users/{id}/revoke-session` | `ROLE_ADMIN` |
| Generar hash BCrypt | `GET` | `/users/tools/password-hash?password=...` | `ROLE_ADMIN` |

**Reglas adicionales en creación de usuarios (`POST /users`):**

- `ROLE_ADMIN` puede crear usuarios para cualquier organización y con cualquier rol.
- `ROLE_MANAGER` solo puede crear usuarios `ROLE_USER` y únicamente dentro de su propia organización.

**Desasociar usuario de organización:**

- Solo `ROLE_ADMIN`. Pone `organizationId` en `null`.
- Si el usuario es el **único manager activo** de la organización, la API responde **400** con mensaje de negocio: debe designarse otro manager antes.

### 2. Org Service

Servicio responsable de organizaciones y sedes.

#### 2.1 Organizaciones

| Acción | Método | Endpoint | Acceso |
| :--- | :---: | :--- | :--- |
| Crear organización | `POST` | `/org/create` | `ROLE_ADMIN` por configuración actual |
| Ver organizaciones del contexto actual | `GET` | `/org/mine` | Usuario autenticado |
| Listar todas las organizaciones | `GET` | `/org` | `ROLE_ADMIN` por configuración actual |
| Obtener organización por ID | `GET` | `/org/{id}` | `ROLE_ADMIN` o usuario miembro de esa organización |
| Actualizar organización | `PUT` | `/org/{id}` | `ROLE_ADMIN` o `ROLE_MANAGER` de esa misma organización |
| Eliminar organización | `DELETE` | `/org/{id}` | `ROLE_ADMIN` por configuración actual |
| Activar organización | `PATCH` | `/org/{id}/activate` | `ROLE_ADMIN` por defecto o authority `ORG_ACTIVATE` |
| Desactivar organización | `PATCH` | `/org/{id}/deactivate` | `ROLE_ADMIN` por defecto o authority `ORG_DEACTIVATE` |

**Comportamiento de `GET /org/mine`:**

- `ROLE_ADMIN` obtiene las organizaciones creadas por su `userId`.
- `ROLE_MANAGER` y `ROLE_USER` obtienen su organización asociada en el JWT.

#### 2.2 Sedes (`locations`)

| Acción | Método | Endpoint | Acceso |
| :--- | :---: | :--- | :--- |
| Crear sede | `POST` | `/org/locations/create` | `ROLE_ADMIN` o `ROLE_MANAGER` de la organización objetivo |
| Listar todas las sedes | `GET` | `/org/locations` | `ROLE_ADMIN` por configuración actual |
| Listar sedes por organización | `GET` | `/org/locations/by-org/{orgId}` | `ROLE_ADMIN` o usuario miembro de esa organización |
| Obtener sede por ID | `GET` | `/org/locations/{id}` | `ROLE_ADMIN` o usuario miembro de la organización propietaria |
| Actualizar sede | `PUT` | `/org/locations/{id}` | `ROLE_ADMIN` o `ROLE_MANAGER` de la organización propietaria |
| Eliminar sede | `DELETE` | `/org/locations/{id}` | `ROLE_ADMIN` o `ROLE_MANAGER` de la organización propietaria |

### 3. Document Service

Servicio responsable del ciclo de vida documental, versiones y descargas.

| Acción | Método | Endpoint | Acceso |
| :--- | :---: | :--- | :--- |
| Crear documento | `POST` | `/documents/create` | `ROLE_ADMIN`, `ROLE_MANAGER` o `ROLE_USER` |
| Obtener documento por ID | `GET` | `/documents/{id}` | Usuario autenticado con acceso a la organización del documento o `ROLE_ADMIN` |
| Listar documentos | `GET` | `/documents` | Usuario autenticado |
| Actualizar metadatos | `PUT` | `/documents/{id}` | `ROLE_ADMIN`, `ROLE_MANAGER` de la misma organización o dueño del documento |
| Eliminar documento | `DELETE` | `/documents/{id}` | `ROLE_ADMIN` o `ROLE_MANAGER` de la misma organización |
| Subir nueva versión | `POST` | `/documents/add-version/{id}` | `ROLE_ADMIN`, `ROLE_MANAGER` de la misma organización o dueño del documento |
| Eliminar una versión | `DELETE` | `/documents/{id}/versions/{versionId}` | `ROLE_ADMIN`, `ROLE_MANAGER` de la misma organización o dueño del documento |
| Forzar cambio de estado | `PATCH` | `/documents/{id}/force-state?state=...` | `ROLE_ADMIN` |
| Descargar última versión | `GET` | `/documents/{id}/download` | Usuario autenticado con acceso a la organización del documento o `ROLE_ADMIN` |
| Descargar versión específica | `GET` | `/documents/{id}/versions/{versionId}/download` | Usuario autenticado con acceso a la organización del documento o `ROLE_ADMIN` |
| Listar archivos del storage | `GET` | `/documents/storage/list?prefix=...` | `ROLE_ADMIN` |
| Listar tipos de documento | `GET` | `/documents/types` | Usuario autenticado |

**Reglas adicionales en documentos:**

- Si el usuario no es `ROLE_ADMIN`, el `organizationId` del documento se fuerza a la organización contenida en el JWT.
- `ROLE_USER` puede actualizar documentos y eliminar versiones solo si es el dueño del documento.
- `ROLE_USER` no puede eliminar el documento completo.
- En `GET /documents`, `ROLE_ADMIN` ve todo; el resto solo los documentos de su organización.

### 4. Módulos sin endpoints de negocio

| Módulo | Descripción |
| :--- | :--- |
| `gateway_service` | Validación JWT, rate limiting, Swagger unificado y enrutado hacia microservicios. |
| `eureka_server` | Servicio de descubrimiento y registro. |
| `common` | Librería compartida (`JwtUtils`, DTOs compartidos). |

---

## Swagger / OpenAPI

Accede a la documentación **solo por el gateway** (`http://localhost:8080`).

**Requisitos:** `eureka_server`, `auth_service`, `org_service`, `document_service` y `gateway_service` en ejecución.

### Panel unificado (recomendado)

- [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) — selector con Auth, Org y Document.

> No uses `/swagger-ui/index.html` en el gateway (ruta incorrecta). Si lo abres por error, redirige a `/swagger-ui.html`.

### Por microservicio (vía gateway)

| Servicio | Swagger UI | OpenAPI JSON |
|----------|------------|--------------|
| Auth (+ `/users`) | [http://localhost:8080/auth/swagger-ui.html](http://localhost:8080/auth/swagger-ui.html) | [http://localhost:8080/auth/v3/api-docs](http://localhost:8080/auth/v3/api-docs) |
| Org | [http://localhost:8080/org/swagger-ui.html](http://localhost:8080/org/swagger-ui.html) | [http://localhost:8080/org/v3/api-docs](http://localhost:8080/org/v3/api-docs) |
| Document | [http://localhost:8080/documents/swagger-ui.html](http://localhost:8080/documents/swagger-ui.html) | [http://localhost:8080/documents/v3/api-docs](http://localhost:8080/documents/v3/api-docs) |

Las rutas OpenAPI del gateway reescriben el prefijo (`/auth/...` → servicio), incluyen `webjars` y **no requieren JWT**.

### Comprobación rápida

1. Abre [http://localhost:8080/auth/v3/api-docs](http://localhost:8080/auth/v3/api-docs) — debe devolver JSON.
2. Luego [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) — debe cargar el selector de APIs.

---

## Pruebas con Postman

Archivos en la raíz de `opsflow-backend`:

| Archivo | Acción |
| :--- | :--- |
| `OpsFlow.postman_collection.json` | **Conservar** — importar en Postman |
| `OpsFlow.postman_environment.json` | **Conservar** — seleccionar entorno *OpsFlow Local* |
| `init-db.sql` | **Conservar** — lo monta Docker al crear Postgres (esquemas `msc_*`) |
| `init.sql` | **No usar** — duplicado incompleto; puede eliminarse |

Recomendación de flujo:

1. Importa colección + entorno; activa *OpsFlow Local*.
2. Ejecuta **Login** (admin / `123456`); el script guarda `token`, `refresh_token` y `userId`.
3. Prueba endpoints según rol (también hay **Login (Manager)** y **Login (User)**).
4. Para desasociar un usuario de su org (solo ADMIN): **Detach User From Organization** en la carpeta *User Management*.

---

## Cobertura y análisis

### JaCoCo

```bash
mvn clean install
```

Luego abre `target/site/jacoco/index.html`.

### SonarQube

1. Levanta el contenedor:

```bash
docker-compose up -d sonarqube
```

2. Accede a [http://localhost:9000](http://localhost:9000) con `admin/admin`.

3. Ejecuta el análisis:

```bash
mvn -DskipTests=true clean compile
mvn sonar:sonar -Dsonar.host.url=http://localhost:9000
```

---

## Limpieza de Docker

```bash
docker compose down -v
```
