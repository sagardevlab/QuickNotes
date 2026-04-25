# Quick Notes

A personal rich-text note-taking web application built with Spring Boot and Quill.js. Create, edit, and manage notes with a clean WYSIWYG editor — with auto-save and PDF export built in.

## Features

- **Rich text editor** powered by Quill.js — supports headings, bold/italic/underline, lists, alignment, inline images, and links
- **Auto-save** every 30 seconds while editing
- **PDF export** — download any note as a paginated PDF
- **Image embedding** — upload images directly into notes (stored as base64)
- **Full CRUD** — create, view, edit, and delete notes
- **Responsive design** — works on mobile and desktop
- **REST API** — JSON endpoints alongside the server-rendered UI

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0.6 |
| ORM | Spring Data JPA + Hibernate |
| Templating | Thymeleaf |
| Dev DB | H2 (in-memory) |
| Prod DB | PostgreSQL |
| Build | Maven (wrapper included) |
| Container | Docker (Alpine + JRE 21) |
| Editor | Quill.js 1.3.7 |
| PDF export | jsPDF 2.5.1 + html2canvas 1.4.1 |

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.x (or use the included `./mvnw` wrapper)

### Run in development

```bash
./mvnw spring-boot:run
```

The app starts on `http://localhost:8080` using an H2 in-memory database.  
The H2 console is available at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:notedb`).

### Build a JAR

```bash
./mvnw clean package -DskipTests
java -jar target/quick-notes-0.0.1-SNAPSHOT.jar
```

## Production Deployment

### Environment Variables

| Variable | Description | Example |
|---|---|---|
| `DB_URL` | JDBC connection URL | `jdbc:postgresql://localhost:5432/notedb` |
| `DB_USER` | Database username | `postgres` |
| `DB_PASSWORD` | Database password | `secret` |
| `DB_DRIVER` | JDBC driver class | `org.postgresql.Driver` |
| `PORT` | Server port (optional) | `8080` |

### Run with prod profile

```bash
export DB_URL=jdbc:postgresql://localhost:5432/notedb
export DB_USER=postgres
export DB_PASSWORD=secret
export DB_DRIVER=org.postgresql.Driver

java -Dspring.profiles.active=prod -jar target/quick-notes-0.0.1-SNAPSHOT.jar
```

### Docker

```bash
# Build image
docker build -t quick-notes .

# Run container
docker run -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://host:5432/notedb \
  -e DB_USER=postgres \
  -e DB_PASSWORD=secret \
  -e DB_DRIVER=org.postgresql.Driver \
  quick-notes
```

## API Reference

### Web routes

| Method | Path | Description |
|---|---|---|
| `GET` | `/` | New note editor |
| `GET` | `/notes` | All notes list |
| `GET` | `/edit/{id}` | Edit an existing note |

### REST endpoints

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/notes` | List all notes (JSON) |
| `GET` | `/api/notes/{id}` | Get a single note |
| `POST` | `/api/notes` | Create a note |
| `PUT` | `/api/notes/{id}` | Update a note |
| `DELETE` | `/api/notes/{id}` | Delete a note |

**Note payload:**
```json
{
  "id": 1,
  "title": "My Note",
  "content": "<p>Rich HTML content...</p>",
  "createdAt": "2026-04-26T10:00:00",
  "updatedAt": "2026-04-26T10:30:00"
}
```

## Project Structure

```
src/main/java/com/sagardevlab/quicknotes/quick_notes/
├── QuickNotesApplication.java      # Entry point
├── controller/NoteController.java  # Web + REST handlers
├── model/Note.java                 # JPA entity
├── repository/NoteRepository.java  # Data access
└── service/NoteService.java        # Business logic

src/main/resources/
├── application.properties          # Dev config (H2)
├── application-prod.properties     # Prod config (PostgreSQL)
├── templates/
│   ├── index.html                  # Editor page
│   └── notes.html                  # Notes list page
└── static/
    ├── css/style.css
    └── js/editor.js                # Auto-save, PDF, image upload
```

## Running Tests

```bash
./mvnw test
```

## License

This project is open source. See [LICENSE](LICENSE) for details.
