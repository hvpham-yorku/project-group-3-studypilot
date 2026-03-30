# Deliverables
[Link to Jira](https://monabbir.atlassian.net/jira/software/projects/G3S/boards/34/backlog?atlOrigin=eyJpIjoiMWZmMzNkZjJkNGY3NGQ0OTk3ZDMwNGJmYjE4ZWNiN2QiLCJwIjoiaiJ9)

### ITR0
[Planning Document](docs/Planning_Document.pdf) is in the `docs` folder.

Customer Meeting video: https://drive.google.com/file/d/12tvFfuYCM1BgyzKO5wU000lIFVXpxwiI/view?usp=sharing  

### ITR1
[log.md](docs/log.md) is in the `docs` folder.

No changes in the Planning Document.

### Delivery 1
[Peer evaluation from](docs/EECS2311Z_Delivery1_PeerReview_Group3.pdf) is in the `docs` folder

# studypilot
StudyPilot is an AI-assisted academic workflow tool designed for the EECS2311 Software Development Project. It introduces a smart student grouping system that forms balanced teams using survey data, taking into account students’ skills, interests, and preferences. By automating this process, StudyPilot helps professors save time while creating more effective and compatible student groups.

## How to run
To run the Spring Boot application locally:

### Delivery 2

Second Customer Meeting Video: 

[Peer evaluation from](docs/EECS2311Z_Delivery2_PeerReview_Group3.pdf) is in the `docs` folder

```bash
cd studypilot
./gradlew bootRun
(If you are on Windows, use gradlew.bat bootRun)
```

Then, follow any additional configuration directions in [studypilot/README.md](studypilot/README.md) 

## How to test
To run the automated test suite using Gradle:


```bash
./gradlew test
```

## Features

* **Professor Dashboard - A centralized hub to manage student groups, regrouping, view students' surveys, and view group submissions.**

*  **Automated Grouping - Uses student survey responses to intelligently create balanced project groups, optimizing collaboration and compatibility.**

* **Group and individual skill options - view student skills, interests, preferences, and progress.**

## Tech Stack
* **Languages: Java**
* **Backend Framework: Spring Boot**
* **Build Tool: Gradle**
* **Database: PosgreSQL (Neon Tech)**
* **AI Integration: (Work under Progress)**

## Repository Structure
A more detailed repository structure is in the project's GitHub Wiki.

```text
studypilot-group-repo/
├── studypilot/                 # Main application (Java/Spring Boot/Gradle)
├── docs/                       # Submission documents for the iterations
└── README.md                   # Project overview
```

# studypilot Java Tests

This project contains:

- `UserTests.java` → JUnit tests for User Java objects.
- `UserDatabaseTest.java` → Template test cases for Neon database CRUD operations.

You can run Neon database tests once you have a working Neon connection.




