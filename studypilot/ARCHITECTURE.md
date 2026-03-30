# StudyPilot Application Architecture

## 1. System Overview

StudyPilot is a server-rendered Spring Boot web application for two user roles:

- Professors create and manage courses, publish weekly surveys, define group-formation activities, review submissions, auto-sort students into groups, and inspect team availability.
- Students register or log in, join courses by join code, answer weekly health surveys, submit group-formation responses, view assigned groups, and submit availability.

The application uses a classic layered architecture:

1. Presentation layer: Spring MVC controllers + Thymeleaf templates
2. Business layer: service classes containing validation, workflow orchestration, and domain rules
3. Data access layer: Spring Data JPA repositories
4. Persistence layer: PostgreSQL tables generated and evolved by Hibernate/JPA

The app is primarily HTML-first, with a small JSON API surface for health-checkin endpoints.

## 2. Technology Stack

- Language: Java 21
- Framework: Spring Boot 4
- Web: Spring MVC
- View engine: Thymeleaf
- Persistence: Spring Data JPA + Hibernate
- Database: PostgreSQL
- Password hashing: BCrypt
- Build tool: Gradle
- Testing: JUnit 5, Mockito, Spring Boot Test, H2 for test runtime
- External integration: OpenAI Chat Completions API for auto-grouping

## 3. High-Level Component View

```text
Browser
  |
  v
Spring MVC Controllers
  |
  v
Business Services
  |
  v
Spring Data Repositories
  |
  v
PostgreSQL

Thymeleaf templates are used by MVC controllers to render HTML views.
HttpSession stores logged-in user context.
OpenAiGroupSortingService is called by GroupFormationService during auto-sorting.
```

## 4. Runtime Structure

### 4.1 Entry Point

- `StudypilotApplication` boots the Spring container and auto-configures the application.

### 4.2 Configuration

- `SecurityConfig` provides a `BCryptPasswordEncoder` bean.
- Authentication and authorization are not implemented through Spring Security filters.
- Instead, controllers manually check `HttpSession` values such as `userId`, `role`, `fullName`, and `email`.

### 4.3 Session Model

After login or registration, the app writes the following session attributes:

- `userId`
- `role`
- `fullName`
- `email`

Controllers use the `role` session attribute to gate professor-only and student-only routes.

## 5. Layer Breakdown

### 5.1 Presentation Layer

Location:

- `src/main/java/com/studypilot/studypilot/GUILayer`
- `src/main/resources/templates`

Responsibilities:

- Accept HTTP requests
- Validate session/role access at route level
- Bind form objects
- Call services
- Populate Thymeleaf view models
- Return HTML views or small JSON responses

Main controllers:

- `LandingController`: public landing page
- `AuthenticationController`: register, login, logout
- `ProfessorHomeController`: professor dashboard, course creation, course page, weekly survey management
- `StudentHomeController`: student dashboard, joined courses, weekly surveys, course join, student course page, group-formation submission flow
- `GroupFormationController`: professor group-formation lifecycle, formed groups, team availability overview, manual moves, auto-sort entry points
- `StudentAvailabilityController`: student availability page and submission
- `TeamHealthController`: JSON and history endpoints for weekly health check-ins and summaries
- `SidebarPagesController`: shared analytics and sidebar-oriented pages
- `SettingsController`: authenticated settings page
- `StudentController`: older simplified availability route still present in the codebase

### 5.2 Business Logic Layer

Location:

- `src/main/java/com/studypilot/studypilot/BusinessLogicLayer`

Responsibilities:

- Input normalization and validation
- Authorization checks based on current user and ownership
- Multi-step workflows spanning multiple repositories
- Construction of professor and student view models
- Transaction boundaries for write operations
- AI-assisted group assignment

Main services:

- `Authentication`: registration and login, email normalization, password hashing and verification
- `CourseService`: course creation, generated course IDs, unique join code generation, course retrieval
- `StudentPortalService`: student enrollment, course access checks, survey response persistence, group lookup, student team snapshot construction
- `GroupFormationService`: create/edit/delete/close/reopen/sort group-formation activities, question management, response status, formed-group generation, cleanup of old group data
- `OpenAiGroupSortingService`: builds AI prompt from activity data and survey responses, calls OpenAI, parses returned group assignments
- `TeamHealthService`: weekly survey publication, student check-ins, student history, professor summaries, trends, risk detection
- `AvailabilityService`: save student availability and compute team overlap summaries
- `CourseTimeSlotService`: manage professor-published meeting slot options

### 5.3 Data Access Layer

Location:

- `src/main/java/com/studypilot/studypilot/DataAccessLayer`

Responsibilities:

- Encapsulate persistence through Spring Data repository interfaces
- Provide query methods derived from method names
- Support lookups, ordering, existence checks, bulk deletes, and grouped retrieval patterns

Repository groups:

- User and auth: `UserRepo`
- Course management: `CourseRepo`, `CourseEnrollmentRepo`, `CourseTimeSlotRepo`
- Group formation: `GroupFormationActivityRepo`, `GroupFormationTopicOptionRepo`, `GroupFormationSkillOptionRepo`, `StudentGroupPreferenceRepo`, `SurveyQuestionRepo`, `SurveyQuestionOptionRepo`, `SurveyResponseRepo`, `FormedGroupRepo`, `FormedGroupMemberRepo`
- Team health and availability: `WeeklySurveyRepo`, `TeamHealthCheckinRepo`, `AvailabilityRepo`, `TeamRepo`, `TeamMemberRepo`

### 5.4 Domain Model Layer

Location:

- `src/main/java/com/studypilot/studypilot/DomainModel`

Responsibilities:

- Represent persisted entities
- Define table names, column names, lengths, and uniqueness constraints
- Capture timestamps and lifecycle hooks through JPA annotations

Important design note:

- Most entities store foreign keys as plain scalar fields like `courseId`, `studentId`, `activityId`, and `professorId` instead of object references with `@ManyToOne` or `@OneToMany`.
- This keeps entities simple, but relationship traversal happens in services through explicit repository calls.

## 6. Feature-Oriented Architecture

### 6.1 Authentication and Access

Flow:

1. User visits landing, login, or register page.
2. `AuthenticationController` submits data to `Authentication` service.
3. `Authentication` hashes passwords on registration and verifies hashes on login.
4. Session attributes are stored.
5. User is redirected to either professor or student home.

Characteristics:

- Session-based auth
- Role-based route branching inside controllers
- No separate auth middleware or centralized authorization policy layer

### 6.2 Course Management

Professor side:

- Professors create courses from the dashboard.
- `CourseService` generates both a course primary key and an 8-character join code.
- Professor dashboards use course and enrollment data to populate summaries.

Student side:

- Students join via join code.
- `StudentPortalService` ensures enrollment does not already exist.
- Student dashboards show enrolled and available-to-join courses.

### 6.3 Weekly Team Health Surveys

Professor workflow:

1. Publish or update the weekly survey for a course.
2. Review submission counts and missing students.
3. View summary metrics and trend analytics.

Student workflow:

1. See active surveys for enrolled courses.
2. Submit health, workload, collaboration, and optional status text.
3. View current week status and check-in history.

Core service:

- `TeamHealthService`

Persistence entities involved:

- `WeeklySurvey`
- `TeamHealthCheckin`
- `Course`
- `CourseEnrollment`

### 6.4 Group Formation

Professor workflow:

1. Create a group-formation activity for a course.
2. Define team-size bounds and survey questions.
3. Students submit responses.
4. Professor closes the activity.
5. Professor triggers auto-sort.
6. App stores resulting formed groups and members.
7. Professor can review groups and manually move students.

Student workflow:

1. Open course group-formation page.
2. Load latest activity for the course.
3. Submit survey answers, notes, and availability string.
4. After sorting, view assigned group and members.

Core services:

- `GroupFormationService`
- `StudentPortalService`
- `OpenAiGroupSortingService`

Persistence entities involved:

- `GroupFormationActivity`
- `SurveyQuestion`
- `SurveyQuestionOption`
- `SurveyResponse`
- `StudentGroupPreference`
- `FormedGroup`
- `FormedGroupMember`

### 6.5 Team Availability

Professor workflow:

1. Publish a set of allowed meeting slots for a course.
2. Inspect team overlap and per-student submissions.

Student workflow:

1. Open the course availability page.
2. Select available meeting slots.
3. Save selections.

Core services:

- `CourseTimeSlotService`
- `AvailabilityService`

Persistence entities involved:

- `CourseTimeSlot`
- `Availability`
- `TeamMember`

### 6.6 Analytics and Dashboard Views

Professor analytics combine:

- course counts
- survey participation
- average team health
- at-risk student indicators
- recent trend lines

Student analytics combine:

- enrolled course counts
- survey submission status
- current average health
- recent history trends

This logic is mainly assembled in `ProfessorHomeController`, `SidebarPagesController`, and `TeamHealthService`.

## 7. Request Flow Patterns

### 7.1 Standard MVC HTML Flow

```text
Browser request
  -> Controller method
  -> Session and role checks
  -> Service call(s)
  -> Repository query/update(s)
  -> Model population
  -> Thymeleaf template render
  -> HTML response
```

Used by most routes such as:

- login and register pages
- student and professor home pages
- course pages
- group-formation pages
- availability pages
- analytics pages

### 7.2 JSON API Flow

```text
Browser or script
  -> RestController endpoint
  -> Session and role checks
  -> Service call
  -> JSON response entity
```

Used mainly by `TeamHealthController` for health-checkin submission and summary/status endpoints.

### 7.3 AI Sorting Flow

```text
Professor clicks auto-sort
  -> GroupFormationController
  -> GroupFormationService.autoSortActivity
  -> gather activity, questions, options, responses, preferences, enrolled students
  -> OpenAiGroupSortingService
  -> OpenAI API
  -> parse JSON assignments
  -> persist formed groups and members
  -> mark activity as SORTED
```

## 8. Template/View Architecture

Location:

- `src/main/resources/templates`

Main templates map closely to user journeys:

- authentication: `landing-page.html`, `login.html`, `register.html`
- professor area: `professor_home.html`, `professor_course_page.html`, `professor_surveys.html`, `group_formation_page.html`, `group_formation_edit_page.html`, `team_availability_overview_page.html`, `team_availability_page.html`
- student area: `student_home.html`, `student_my_courses.html`, `student_course_page.html`, `student_group_formation_page.html`, `student_availability_page.html`, `student_surveys.html`, `student_checkin_history.html`, `student_group_space_page.html`
- shared/supporting pages: `analytics.html`, `settings.html`, `faq_library.html`, `my_courses.html`
- reusable fragments: `templates/fragments/*`

The frontend is not a separate SPA. Server-rendered HTML is the primary UI delivery mechanism.

## 9. Data Architecture

Main entity clusters:

- Identity: `User`
- Courses: `Course`, `CourseEnrollment`, `CourseTimeSlot`
- Availability: `Availability`
- Group formation: `GroupFormationActivity`, `GroupFormationTopicOption`, `GroupFormationSkillOption`, `StudentGroupPreference`, `SurveyQuestion`, `SurveyQuestionOption`, `SurveyResponse`, `FormedGroup`, `FormedGroupMember`
- Team health: `WeeklySurvey`, `TeamHealthCheckin`, `Team`, `TeamMember`

Important persistence characteristics:

- PostgreSQL is the target runtime database.
- Hibernate is configured with `ddl-auto=update`, so schema evolution is driven from entities at startup.
- Several tables use unique constraints for logical integrity, for example survey responses per question and weekly survey per course/week.
- Referential links are mostly enforced by application logic instead of explicit JPA object graphs.

## 10. Cross-Cutting Concerns

### 10.1 Validation

Validation is primarily implemented inside services rather than in controller annotations.

Examples:

- null and blank checks
- role and ownership checks
- duplicate enrollment prevention
- team size validation
- weekly survey existence checks before check-ins

### 10.2 Transactions

Write-heavy multi-step methods use `@Transactional`, especially around:

- course enrollment
- saving survey responses
- activity creation and updates
- activity sorting
- availability replacement
- weekly survey publication

### 10.3 Error Handling

There is no global exception-handling layer.

Current pattern:

- services throw `IllegalArgumentException` for business-rule violations
- controllers catch exceptions and map them to either model error messages or JSON error bodies

### 10.4 Security Posture

Current security is minimal and application-managed:

- password hashing is present
- session-based role checks are present
- route protection is manual and repeated per controller
- no formal Spring Security authorization chain is visible in the current codebase

## 11. Architectural Strengths

- Clear separation between controller, service, repository, and entity layers
- Good fit for server-rendered university workflow app
- Services encapsulate most important business rules
- Feature areas are easy to trace from controller to service to repository
- AI-assisted grouping is isolated to a dedicated service rather than spread through the codebase

## 12. Architectural Risks and Tradeoffs

- Authorization logic is duplicated across controllers instead of centralized.
- Entity relationships are mostly scalar IDs, so integrity depends more on service logic than ORM mapping.
- `ddl-auto=update` is convenient for development but weaker than explicit migrations for controlled schema evolution.
- OpenAI integration uses direct HTTP and manual JSON parsing, which is brittle compared with structured clients.
- Some routes overlap conceptually, such as the older `StudentController` availability page versus `StudentAvailabilityController`.
- There is no clear API boundary separate from the MVC layer, so view logic and workflow assembly sometimes sit close together.

## 13. One-Screen Summary

```text
StudyPilot is a monolithic Spring Boot MVC application.

Users:
- Professors
- Students

Primary capabilities:
- Authentication and session-based access
- Course creation and enrollment by join code
- Weekly team-health surveys and analytics
- Group-formation surveys and AI-assisted team creation
- Student availability collection and team overlap reporting

Main architectural style:
- Layered monolith
- Server-rendered HTML with Thymeleaf
- PostgreSQL persistence via Spring Data JPA
- Small supplemental JSON endpoints

Core flow:
Controller -> Service -> Repository -> PostgreSQL

External dependency:
GroupFormationService -> OpenAiGroupSortingService -> OpenAI API
```
