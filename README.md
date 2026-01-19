# LeihSy Backend

Dieses Repository enthält das Backend der Anwendung **LeihSy**, einem Online-Portal zum Verleihen von Gegenständen der Hochschule Esslingen.

## Tech Stack
- **Backend & Laufzeit**
    - Java 21
    - Spring Boot 3.5.x
- **Web & API**
    - Spring Web (Spring MVC)
    - Spring Validation (Jakarta Validation)
- **Security & Authentifizierung**
    - Spring Security
    - OAuth2 Resource Server über Keycloak (OpenID Connect)
- **Datenhaltung**
    - Spring Data JPA (Hibernate)
    - PostgreSQL (Production)
    - H2 Database (Development)
    - hibernate-types
- **DTO-Mapping & Biolerplate-Reduktion**
    - MapStruct
    - Lombok
- **Dokumentation**
    - SpringDoc OpenAPI (SwaggerUI)
- **Zusatzfunktionen**
    - Spring Boot Mail
    - OpenPDF
- **Testing & Qualitätssicherung**
    - Spring Boot Test
    - JaCoCo
- **Build & Tooling**
    - Maven
    - Maven Compiler Plugin
- **Deployment & Betrieb**
    - Docker
    - Docker Compose
    - Spring Boot Actuator

## Voraussetzungen

Folgende Software muss installiert sein:

- Docker
- Docker Compose (Plugin)
- PostgreSQL Datenbank (Vorzugsweise auf gleichem System)

## Herunterladen und Konfiguration

Sourcecode herunterladen
```bash
git clone https://github.com/LeihSy/LeihSy_Backend
```

In den Ordner LeihSy_Backend navigieren
```bash
cd LeihSy_Backend
```

Verbindungsdetails für die **PostgreSQL Datenbank** und die **Allowed Origins** ändern
```bash
nano docker-compose.yml
```

Wenn Docker Compose nicht verwendet wird können die **Verbindungsinformationen** für die PostgreSQL Datenbank und die Allowed Origins auch als **Umgebungsvariablen** gesetzt werden.
```bash
POSTGRES_URL: Datenbank URL (z.B. jdbc:postgresql://localhost:5432/default_database)
POSTGRES:USER: Username für die Datenbank
POSTGRES_PASSWORD: Passwort für die Datenbank
ALLOWED_ORIGINS: URL des Frontens (z.B. https://leihsy.hs-esslingen.com)
```
**Mehrere Origins** können durch ein Komma ohne Leerzeichen getrennt werden
```bash
ALLOWED_ORIGINS: z.B. https://leihsy.hs-esslingen.com,http://localhost:4200
```

## Bauen und Deployment

Docker Image bauen
```bash
docker build -t leihsy-backend .
```

Docker Container starten mit der **mitgelieferten docker-compose.yml**
```bash
docker compose up -d
```

## Entwicklung

Die Anwendung kann zu Developmentzwecken testweise lokal ausgeführt werden.

Ein installiertes **Maven** wird vorausgesetzt.

Unter */src/main/resources/application.properties* kann in der Zeile
```bash
spring.profiles.active=prod
```
das Profil auf **dev** geändert werden. Damit wird an Stelle der PostgreSQL Datenbank eine **H2 In-Memory Datenbank** verwendet.
Mit dem Profil **prod** wird eine **PostgreSQL Datenbank** verwendet.

Die Verbindungsdetails der **PostgreSQL Datenbank** können als Umgebungsvariablen gesetzt werden:
```bash
POSTGRES_URL: Datenbank URL (z.B. jdbc:postgresql://localhost:5432/default_database)
POSTGRES:USER: Username für die Datenbank
POSTGRES_PASSWORD: Passwort für die Datenbank
```

Die Allowed Origins für das Applikationsprofil application-prod.properties können als Umgebungsvariable gesetzt werden 
```bash
ALLOWED_ORIGINS: URL des Frontens (z.B. https://leihsy.hs-esslingen.com)
```
Mehrere Origins können durch ein Komma ohne Leerzeichen getrennt werden
```bash
ALLOWED_ORIGINS: z.B. https://leihsy.hs-esslingen.com,http://localhost:4200
```

Die Abhängigkeiten mit Maven installieren
```bash
mvn clean install
```

Die Anwendung **lokal ausführen**
```bash
mvn spring-boot:run
```

## API Dokumentation

Die **Swagger API Dokumentation** findet sich im Browser unter
```bash
http://localhost:8080/swagger-ui/index.html#/
```
