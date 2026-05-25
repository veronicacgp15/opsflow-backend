<div align="center">

# 🗂️ OpsFlow Backend v2
### Plataforma SaaS de gestión documental empresarial

![Java](https://img.shields.io/badge/Java_21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_3.4-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Cloud](https://img.shields.io/badge/Spring_Cloud-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL_15-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-FF6600?style=for-the-badge&logo=rabbitmq&logoColor=white)
![Redis](https://img.shields.io/badge/Redis_7-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)

</div>

---

## 🎯 ¿Qué problema resuelve?

OpsFlow es el backend de una plataforma SaaS B2B diseñada para empresas que necesitan controlar documentos críticos con vencimiento — contratos, licencias, certificaciones y renovaciones operativas.

**El negocio que resuelve:**
- Empresas pierden contratos o sufren sanciones por documentos vencidos no detectados
- Los equipos operativos no tienen trazabilidad de quién modificó qué y cuándo
- Sin alertas automatizadas, la renovación depende de procesos manuales propensos a error

**Lo que OpsFlow entrega:**
- Control de ciclo de vida documental con alertas automáticas de vencimiento vía RabbitMQ
- Trazabilidad completa (auditoría) de cada operación sobre documentos
- Multi-tenant con aislamiento por organización — cada empresa ve solo sus datos
- Versionado de documentos con descarga por versión específica

---

## 🏗️ Arquitectura

OpsFlow sigue una arquitectura de **microservicios con API Gateway**, donde cada servicio tiene su propia base de datos y se comunica de forma asíncrona a través de RabbitMQ.

```
Cliente (Postman / Frontend)
        │
        ▼
┌─────────────────┐
│  gateway_service │  :8080  ← Punto único de entrada, validación JWT
└────────┬────────┘
         │ enruta a:
    ┌────┴──────────────────────────┐
    │           │                  │
    ▼           ▼                  ▼
┌──────────┐ ┌──────────┐ ┌───────────────┐
│auth_svc  │ │ org_svc  │ │document_svc   │
│  :8081   │ │  :8082   │ │    :8083      │
│          │ │          │ │               │
│JWT · RBAC│ │Orgs/Sedes│ │Docs · Alertas │
└──────────┘ └──────────┘ └───────────────┘
    │              │              │
    └──────────────┴──────────────┘
                   │
    ┌──────────────┼──────────────┐
    ▼              ▼              ▼
┌────────┐  ┌──────────┐  ┌────────────┐
│Postgres│  │  Redis   │  │  RabbitMQ  │
│  :5432 │  │  :6379   │  │   :5672    │
│ 3 DBs  │  │  Cache   │  │  Alertas   │
└────────┘  └──────────┘  └────────────┘
                   │
            ┌──────────────┐
            │eureka_server │  :8761  ← Service discovery
            └──────────────┘
```

### Microservicios

| Módulo | Puerto | Responsabilidad |
|--------|--------|-----------------|
| `gateway_service` | `8080` | Punto único de entrada — enruta y valida JWT |
| `auth_service` | `8081` | Autenticación, JWT, usuarios, roles y permisos |
| `org_service` | `8082` | Organizaciones multi-tenant y sedes |
| `document_service` | `8083` | Ciclo de vida documental, versiones, alertas |
| `eureka_server` | `8761` | Registro y descubrimiento de servicios |
| `common` | — | Librería compartida (DTOs, utilidades) |

---

## 🔐 Modelo de seguridad

Seguridad en dos capas: **JWT stateless** en el gateway + **RBAC granular** dentro de cada microservicio.

```
Request → Gateway → valida JWT (firma + expiración)
                         │
                         ▼
               Microservicio destino
                         │
              @PreAuthorize + SecurityConfig
                         │
                ┌────────┴────────┐
                │                 │
           Por rol           Por propiedad
        ROLE_ADMIN          "¿Es tu org?"
        ROLE_MANAGER        "¿Es tu doc?"
        ROLE_USER
```

| Rol | Alcance |
|-----|---------|
| `ROLE_ADMIN` | Acceso global. Administra todo el sistema |
| `ROLE_MANAGER` | Alcance acotado a su organización |
| `ROLE_USER` | Operativo — solo sus documentos y su organización |

**Tokens:** Access token (corta duración) + Refresh token almacenado en Redis. Revocación inmediata de sesión vía `POST /users/{id}/revoke-session`.

---

## ⚡ Inicio rápido

### 1. Infraestructura base

```bash
docker-compose up -d postgres-db redis-cache rabbitmq-broker
```

### 2. Orden de ejecución de microservicios

```bash
# Levanta en este orden exacto
1. eureka_server     → :8761
2. auth_service      → :8081
3. org_service       → :8082
4. document_service  → :8083
5. gateway_service   → :8080
```

### 3. Verificar registro en Eureka

```
http://localhost:8761/
```
Deben aparecer: `AUTH-SERVICE`, `ORG-SERVICE`, `DOCUMENT-SERVICE`, `GATEWAY-SERVICE`

### 4. Troubleshooting de puertos (Windows)

```bash
netstat -ano | findstr :8080
taskkill /F /PID [PID]
```

---

## 📋 Endpoints principales

### Auth Service — `/auth/**`, `/users/**`

| Endpoint | Método | Acceso |
|----------|--------|--------|
| `/auth/login` | `POST` | Público |
| `/auth/signup` | `POST` | Público |
| `/auth/refresh` | `POST` | Refresh token válido |
| `/auth/logout` | `POST` | Autenticado |
| `/auth/forgot-password` | `POST` | Público |
| `/auth/me/permissions` | `GET` | Autenticado |
| `/users` | `GET` | `ROLE_ADMIN` |
| `/users/my-organization` | `GET` | `ROLE_ADMIN`, `ROLE_MANAGER` |
| `/users/{id}/revoke-session` | `POST` | `ROLE_ADMIN` |
| `/auth/roles/**` | `*` | `ROLE_ADMIN` |

### Org Service — `/org/**`

| Endpoint | Método | Acceso |
|----------|--------|--------|
| `/org/create` | `POST` | `ROLE_ADMIN` |
| `/org/mine` | `GET` | Autenticado |
| `/org/{id}` | `PUT` | `ROLE_ADMIN`, `ROLE_MANAGER` de esa org |
| `/org/{id}/activate` | `PATCH` | `ROLE_ADMIN` o authority `ORG_ACTIVATE` |
| `/org/locations/**` | `*` | Por rol y pertenencia a organización |

### Document Service — `/documents/**`

| Endpoint | Método | Acceso |
|----------|--------|--------|
| `/documents/create` | `POST` | Todos los roles |
| `/documents/{id}` | `GET` | Autenticado + pertenencia a org |
| `/documents/{id}` | `PUT` | Admin, Manager de la org, o dueño |
| `/documents/{id}` | `DELETE` | `ROLE_ADMIN`, `ROLE_MANAGER` |
| `/documents/add-version/{id}` | `POST` | Admin, Manager, o dueño |
| `/documents/{id}/download` | `GET` | Autenticado + pertenencia a org |
| `/documents/{id}/versions/{vId}/download` | `GET` | Autenticado + pertenencia a org |
| `/documents/{id}/force-state` | `PATCH` | `ROLE_ADMIN` |

> **Regla de aislamiento multi-tenant:** si el usuario no es `ROLE_ADMIN`, el `organizationId` se fuerza desde el JWT — es imposible acceder a documentos de otra organización.

---

## 📊 Calidad de código

### Cobertura con JaCoCo

```bash
mvn clean install
# Reporte en: target/site/jacoco/index.html
```

### Análisis estático con SonarQube

```bash
docker-compose up -d sonarqube
# Accede a http://localhost:9000 (admin/admin)

mvn -DskipTests=true clean compile
mvn sonar:sonar -Dsonar.host.url=http://localhost:9000
```

---

## 🧪 Probar con Postman

El repo incluye colección y environment listos para usar:

- `OpsFlow.postman_collection.json`
- `OpsFlow.postman_environment.json`

**Flujo recomendado:**
1. `POST /auth/login` → copia el access token
2. Configura `Authorization: Bearer <token>` en la colección
3. Prueba endpoints según el rol del usuario

**Swagger UI por servicio:**
- Auth: `http://localhost:8081/swagger-ui.html`
- Org: `http://localhost:8082/swagger-ui.html`
- Documents: `http://localhost:8083/swagger-ui.html`

---

## 🧹 Limpieza

```bash
docker-compose down -v
```

---

## 💡 Decisiones técnicas destacadas

| Decisión | Alternativa descartada | Razón |
|----------|----------------------|-------|
| RabbitMQ para alertas de vencimiento | Cron job en la app | Desacopla el procesamiento; la app no bloquea esperando timers |
| Redis para refresh tokens | DB relacional | O(1) lookup + TTL nativo — no requiere jobs de limpieza |
| Eureka para service discovery | Hardcoded URLs | Permite escalar instancias sin cambiar configuración |
| JWT stateless en gateway | Sesiones en DB | El gateway puede validar sin llamar a auth_service en cada request |
| Multi-tenant por organización en JWT | Por base de datos | Simplicidad operativa manteniendo aislamiento en queries |
