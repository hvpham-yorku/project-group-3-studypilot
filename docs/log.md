StudyPilot – Iteration 1 Log
=================================

Repository Structure
---------------------------------
```text
📦 project-group-3-studypilot
├── 📂 docs
├── 📜 README.md
├── 📜 log.md
├── 📜 Planning_Document.pdf
└── 📂 studypilot
    ├── 📂 gradle/wrapper
    │   ├── 📜 gradle-wrapper.jar
    │   └── 📜 gradle-wrapper.properties
    ┣ 📂src
     ┃ ┣ 📂main
     ┃ ┃ ┣ 📂java
     ┃ ┃ ┃ ┗ 📂com
     ┃ ┃ ┃ ┃ ┗ 📂studypilot
     ┃ ┃ ┃ ┃ ┃ ┗ 📂studypilot
     ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂BusinessLogicLayer
     ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜Authentication.java
     ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜CourseService.java
     ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂Configuration
     ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜SecurityConfig.java
     ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂DataAccessLayer
     ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜CourseRepo.java
     ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜UserRepo.java
     ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂DomainModel
     ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜Course.java
     ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜User.java
     ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂GUILayer
     ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜AuthenticationController.java
     ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜CreateCourseForm.java
     ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜LandingController.java
     ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜LoginForm.java
     ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ProfessorHomeController.java
     ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜RegisterForm.java
     ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜StudentHomeController.java
     ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜SettingsController.java
     ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜StudypilotApplication.java
     ┃ ┃ ┗ 📂resources
     ┃ ┃ ┃ ┣ 📂graphql
     ┃ ┃ ┃ ┣ 📂static
     ┃ ┃ ┃ ┣ 📂templates
     ┃ ┃ ┃ ┃ ┣ 📂fragments
     ┃ ┃ ┃ ┃ ┃ ┗ 📜sidebar.html
     ┃ ┃ ┃ ┃ ┣ 📜landing-page.html
     ┃ ┃ ┃ ┃ ┣ 📜login.html
     ┃ ┃ ┃ ┃ ┣ 📜professor_course.html
     ┃ ┃ ┃ ┃ ┣ 📜professor_course_page.html
     ┃ ┃ ┃ ┃ ┣ 📜professor_home.html
     ┃ ┃ ┃ ┃ ┣ 📜register.html
     ┃ ┃ ┃ ┃ ┣ 📜settings.html
     ┃ ┃ ┃ ┃ ┗ 📜student_home.html
     ┃ ┃ ┃ ┗ 📜application.properties
     ┃ ┗ 📂test
     ┃ ┃ ┗ 📂java
     ┃ ┃ ┃ ┗ 📂com
     ┃ ┃ ┃ ┃ ┗ 📂studypilot
     ┃ ┃ ┃ ┃ ┃ ┗ 📂studypilot
     ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜AuthenticationControllerTests.java
     ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜AuthenticationTests.java
     ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜LandingControllerTests.java
     ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜LoginFormTests.java
     ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ProfessorHomeControllerTests.java
     ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜RegisterFormTests.java
     ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜StudentHomeControllerTests.java
     ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜StudypilotApplicationTests.java
     ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜UserTests.java
     ┗ 📂test
     ┃ ┗ 📂java
     ┃ ┃ ┗ 📂com
     ┃ ┃ ┃ ┗ 📂studypilot
     ┃ ┃ ┃ ┃ ┗ 📂studypilot
     ┃ ┃ ┃ ┃ ┃ ┣ 📜AuthenticationControllerTests.java
     ┃ ┃ ┃ ┃ ┃ ┣ 📜AuthenticationTests.java
     ┃ ┃ ┃ ┃ ┃ ┣ 📜LandingControllerTests.java
     ┃ ┃ ┃ ┃ ┃ ┣ 📜LoginFormTests.java
     ┃ ┃ ┃ ┃ ┃ ┣ 📜ProfessorHomeControllerTests.java
     ┃ ┃ ┃ ┃ ┃ ┣ 📜RegisterFormTests.java
     ┃ ┃ ┃ ┃ ┃ ┣ 📜StudentHomeControllerTests.java
     ┃ ┃ ┃ ┃ ┃ ┣ 📜StudypilotApplicationTests.java
     ┃ ┃ ┃ ┃ ┃ ┗ 📜UserTests.java
    ├── 📜 build.gradle
    ├── 📜 gradlew
    ├── 📜 settings.gradle
    └── 📜 tailwind.config.js
``` 

TEAM MEMBERS
==================================================
* **Tessa Cloutier**
* **Gabriella Crook**
* **Monabbir Bhuiyan**
* **Ashvin Kawleswaran**
* **Sandeepon Saha**

## ITERATION 0 REFERENCE
**Jan 28, 2026 — Gabriella (Revised by Team)**  
- Completed ITR0 planning document  
- Defined initial user stories  
- Drafted preliminary architecture  
- Selected major story for ITR1  

**Time Spent:** 2 hours

---

## CUSTOMER MEETING
**Feb 4, 2026 — Gabriella/Tessa (Revised by Team)**  
- Met with customer  
- Clarified scope of Iteration 1  
- Confirmed ITR1 requirements:
  - Domain objects
  - Stub database
  - Unit tests
  - GUI for one major story  

**Improvements Requested:**
- Improve presentation and documentation template  
- Add group selection feature  
- Reduce focus on email functionality  

**Time Spent:** 1.5 hours

---

## GROUP MEETING
**Feb 10, 2026 — Team**  
- Divided user stories / ITR1 tasks  

**Time Spent:** 15 minutes

---

## REPOSITORY SETUP
**Feb 10, 2026**  
- Created repository using GitHub Classroom  
- Cloned repository locally  
- Verified project builds on fresh clone  

**Time Spent:** 10 minutes

---

## PROJECT STRUCTURE SETUP
**Feb 12–13, 2026 — Monabbir (Logged and Uploaded by Gabriella)**

**Tasks Completed:**
- Created initial project folder structure  
- Added `README.md`  
- Added `log.md`  
- Uploaded planning documents  
- Configured `.gitignore`  

**Repository Cleanup:**
- Removed unused directories  
- Deleted accidental system files (`.DS_Store`)  
- Reorganized files for consistent structure  

**Established Git Workflow:**
- Pull before starting work  
- Smaller and more frequent commits  
- Descriptive commit messages  

**Design Rationale:**  
Standardizing repository structure improved collaboration, reduced version conflicts, and ensured consistent development across environments.

| Estimated Time | Actual Time |
|---------------|-------------|
| 2 hours       | 2 hours     |

---

## DOMAIN LAYER IMPLEMENTATION
**Feb 12–13, 2026 - Ashvin**

**Implemented:**
- User domain model  
- Authentication-related classes  
- Input validation for login and registration  

**Ensured:**
- Proper constructors  
- Encapsulation  
- Clean naming conventions  

| Estimated Time | Actual Time |
|---------------|-------------|
| 2 hours       | 2.5 hours   |

---

## DATABASE LAYER 
**Feb 13, 2026 - Ashvin**

**Implemented:**
- User repository interface  
- H2 in-memory database configuration  
- Non-persistent data storage  

**Characteristics:**
- In-memory storage only  
- Prepared system for future database replacement  
- Supports authentication testing  

**Design Rationale:**  
Using an in-memory database allows rapid testing and supports future integration of persistent storage without changing business logic.

| Estimated Time | Actual Time |
|---------------|-------------|
| 2 hours       | 2.5 hours   |

---

## BUSINESS LOGIC LAYER
**Feb 13, 2026 - Ashvin**

**Implemented:**
- User registration functionality  
- Login authentication logic  
- Role-based user handling (student/professor views)

**Responsibilities:**
- Validating credentials  
- Managing authentication flow  
- Connecting controllers with repository  

| Estimated Time | Actual Time |
|---------------|-------------|
| 2 hours       | 2 hours     |

---

## UNIT TESTING
**Feb 13, 2026 — Tessa**

**Framework:** JUnit 5

**Current Coverage:**
- Basic application tests configured  
- Edge case validation  
- All tests passing  

**Notes:**
Full unit testing deferred; focus was on delivering a functional system.

| Estimated Time | Actual Time |
|---------------|-------------|
| 1 hour        | 2 hours     |

---

## GUI IMPLEMENTATION (FOR USER STORIES)
**Feb 13, 2026 - Monabbir, Tessa, Gabriella**

**Implemented:**
- Landing page interface  
- Register / Login workflow  
- Home page navigation  

**Styling:**
- Tailwind CSS integration  
- Improved layout and navigation  
- Added features and FAQ sections  

**Notes:**  
GUI intentionally simple for ITR1. Enhancements planned for ITR2.

| Estimated Time | Actual Time |
|---------------|-------------|
| 3 hours       | 3 hours     |

---

## DOCUMENTATION
**Feb 13, 2026 — Ashvin, Gabriella, Monabbir**

**Completed:**
- `log.md`
- GitHub Wiki

| Estimated Time | Actual Time |
|---------------|-------------|
| 1 hour        | 2 hour      |

---

## USER STORY DEVELOPMENT SUMMARY

### User Story 1 — Create and Store Student Questions
- Created question data model  
- Implemented in-memory storage  
- Connected GUI input to backend  

### User Story 2 — Instructor Replies to Questions
- Added instructor data placeholder  

### User Story 3 — Instructor View Questions (GUI)
- Built instructor interface  
- Connected GUI to repository data  

### User Story 4 — Enter and Save Responses (GUI)
- Created response input form  
- Connected form to backend logic  
- Implemented response saving  

### User Story 5 — Data Storage System
- Implemented repository interface  
- Prepared for future database integration  

---

## SYSTEM ARCHITECTURE ORGANIZATION
**Feb 13, 2026 — Gabriella**

The system follows a layered architecture separating:
- **Domain Model** — User and authentication classes  
- **Data Access Layer** — Repository and database configuration  
- **Business Logic Layer** — Authentication processing  
- **GUI Layer** — Controllers and templates  

| Estimated Time | Actual Time |
|---------------|-------------|
| 20 minutes    | 20 minutes  |

---

## PLAN CHANGES FROM ITR0

**Original Plan:**
- Full academic domain model  
- Advanced GUI  
- Extensive unit testing  

**Revised Plan:**
- Authentication-focused functionality  
- Basic GUI implementation  
- Stub database support  

**Reason:**  
Scope adjusted to ensure a working system within the iteration timeframe.

---

## MEETINGS

**Feb 10 — Architecture Discussion**
- Finalized layered architecture  
- Assigned development tasks  

**Duration:** 1 hour

**Feb 12 — Code Review Session**
- Reviewed project structure  
- Cleaned repository organization  
- Verified successful build  

**Duration:** 1 hour

---

## GIT PROCESS
- Frequent incremental commits across Feb 12–13  
- Repository cleanup documented through commits  
- No unresolved conflicts  
- Project builds successfully on fresh clone  

This supports future database replacement and system scalability.

---

## TIME SUMMARY

| Member     | Estimated | Actual |
|------------|-----------|--------|
| Tessa      |           |        |
| Gabriella  |           |        |
| Monabbir   |           |        |
| Ashvin     |           |        |

---

## CURRENT STATUS
Repository structured
Authentication implemented
Stub database implemented
Business logic implemented
GUI for major story implemented
Planning documents included
Log maintained
Repository builds without errors


## NEXT STEPS for Delivery
Replace stub database with persistent database
Expand domain model (students, courses, registrations)
Increase unit test coverage
Improve validation and error handling
Enhance GUI features
