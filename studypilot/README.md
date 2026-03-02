# Main Application

## How to run

**For TAs:**
All project configurations, including database settings and AI integrations, are pre-configured in the `src/main/resources/application.properties` file. No external `.env` setup is required to start the application.

### Step 1: Prerequisites

Ensure you have **Java 21** (or higher) installed on your system. You can verify this by running `java -version` in your terminal.

### Step 2: Navigate to the Project

Open your terminal or command prompt and navigate to the root directory of the project:

cd `studypilot`

### Step 3: Start the local server

Run the following command to build and start the Spring Boot application using the Gradle wrapper:

**On macOS/Linux:**
``` bash
./gradlew bootRun`
```

**On Windows:**
```bash
gradlew.bat bootRun
```

The application will be accessible at `http://localhost:8080` once the startup sequence is complete.

---

## Testing

The project includes a comprehensive suite of unit and integration tests. To execute the tests and view the results:

```bash
./gradlew test
```

**Test Reports:**
After the tests complete, a detailed HTML report is generated. You can view it by opening the following file in any web browser:
`studypilot/build/reports/tests/test/index.html`

---

## App Structure

The project follows a standard N-Tier architecture to separate concerns across the StudyPilot system:

- `src/main/java/.../GUILayer/` – Contains the Spring MVC Controllers that manage web routes and handle user interactions.
- `src/main/java/.../BusinessLogicLayer/` – Contains service classes, such as `Authentication.java` and `CourseService.java`, which handle the core logic of the application.
- `src/main/java/.../DataAccessLayer/` – Contains repository interfaces (e.g., `UserRepo.java`, `CourseRepo.java`) for database communication.
- `src/main/java/.../DomainModel/` – Defines the data entities like `User.java` and `Course.java`.
- `src/main/resources/templates/` – Contains the Thymeleaf HTML templates for the frontend, organized into folders like `fragments` for reusable components.
- `src/main/resources/application.properties` – The central configuration file for the application environment.
- `src/test/java/.../` – Contains the test suite, including `AuthenticationTests.java`, `ProfessorHomeControllerTests.java`, and `UserTests.java`, used to verify the functionality of all application layers.

A more detailed repository structure and architectural overview can be found in the project's **GitHub Wiki**.
