StudyPilot – Iteration 3 Log
=================================

Repository Structure
---------------------------------

TEAM MEMBERS
==================================================
* **Tessa Cloutier**
* **Gabriella Crook**
* **Monabbir Bhuiyan**
* **Ashvin Kawleswaran**


## FLEXIBLE SURVEY-BASED GROUP FORMATION OVERHAUL
**Mar 2026 — Ashvin**

**Implemented:**
- Replaced the fixed 5-topic / 5-skill survey model with a fully flexible, professor-defined question system
- Added support for two question types professors can choose per question:
  - **SELECT** : students choose one or more options from a list
  - **RATING** : students rate each option on a scale of 1–5
- Added a per question grouping strategy toggle professors set when building the survey:
  - **SIMILAR** : group students who gave similar answers to this question
  - **DIVERSE** : group students who gave varied answers to this question
- Added support for professors to define any number of questions with any number of custom options during activity creation and editing
- Implemented student-facing survey form that dynamically renders all professor-defined questions at the correct type (checkboxes for SELECT, rating buttons for RATING)
- Added availability calendar to the student survey form so students can indicate what times they are free to meet
- Implemented saving and pre-population of previous student responses when revisiting a survey
- Integrated all survey responses, availability, and per-question grouping strategies into the AI sorting prompt

**AI Integration:**
- Wired OpenAI GPT 5.4 nano as the sorting backend using Java
- Built the AI prompt to include each question's title, type, grouping strategy, and every student's responses so the model can form groups intelligently
- Parsed the AI's structured JSON response into actual formed groups stored in the database

**Drag-and-Drop Group Editing Fix:**
- Fixed the professor groups page drag-and-drop so moving a student between groups actually updates the database, not just the UI
- Fixed a unique violation that occurred during group membership reassignment by flushing pending deletes before inserting

**Design and Implementation Work:**
- Added three new domain model entities: `SurveyQuestion`, `SurveyQuestionOption`, `SurveyResponse`
- Added repository interfaces for each new entity with the query methods needed for creation, retrieval, and cleanup
branch and replaced their role with the new entities
- Rewrote `CreateGroupFormationForm` to use Spring MVC indexed binding for dynamic question lists instead of JSON serialization
- Rewrote `StudentGroupPreferenceForm` to use a `Map<String, String>` keyed by question ID for flexible response collection
- Rewrote `GroupFormationService` to save, load, update, and delete survey questions and options alongside each activity
- Rewrote `StudentPortalService` to load survey questions, group options by question, retrieve saved responses, and upsert responses on submission
- Rewrote `OpenAiGroupSortingService` to build and send a structured prompt containing all question and response data
- Updated `StudentHomeController` to pass survey questions, grouped options, and saved responses into the model for the student survey page
- Updated `GroupFormationController` to return explicit `ResponseEntity<Map>` with correct `Content-Type: application/json` header for the drag-and-drop endpoint


**Database Work:**
- Designed relational schema to replace fixed topic/skill option tables with a general purpose survey question and response structure
- Added `survey_questions` table with question type and per question grouping strategy fields
- Added `survey_question_options` table linked to questions with ordering support
- Added `survey_responses` table with a unique constraint ensuring one response per student per question per activity
- Extended `student_group_preferences` with `availability_slots` and `question_responses` columns using Hibernate compatible `columnDefinition` defaults to avoid null violations on existing rows
- Stored SELECT responses as comma separated option texts and RATING responses as `Option:Rating,Option:Rating` format in a single cell

**Testing / Verification:**
- Verified professor can create a survey with multiple SELECT and RATING questions and custom options
- Verified student survey page renders all questions correctly with the right input type per question
- Verified student responses are saved and pre populated correctly on revisit
- Verified auto sort sends all question/response data to the AI and produces non sequential group assignments
- Verified drag and drop group editing persists to the database and resolves without unique constraint errors
- Confirmed all activity lifecycle states (OPEN, CLOSED, SORTED) render the correct view on the student page

| Estimated Time | Actual Time |
|----------------|-------------|
| 14 hours       | 16 hours    |

---
### 📊 Weekly Team Health Check-ins
**Assignee:** @Monabbir | **Dates:** Mar 18–24, 2026

**Implemented:**
- [x] Implemented backend logic for weekly health check-ins.
- [x] Added capabilities for professors to publish a weekly survey for their courses.
- [x] Enabled students to submit weekly health, workload, and collaboration scores along with a status text.
- [x] Built JSON API endpoints in `TeamHealthController` for health-checkin submission and summary retrieval.

| Estimated Time | Actual Time |
|:--------------:|:-----------:|
| 4 hours        | 5 hours     |

---

### 🎨 Frontend GUI Expansion & Integration
**Assignee:** @Monabbir | **Dates:** Mar 20–28, 2026

**Implemented:**
- [x] Built out the frontend GUI for the complex group formation interfaces.
- [x] Implemented the dynamic form builder allowing professors to dynamically add/remove survey questions on the creation page.
- [x] Designed and integrated the student view for answering surveys (`student_group_formation_page.html`).
- [x] Integrated Thymeleaf models to render the "Active Group Formations" list and the final "Formed Groups" output.

| Estimated Time | Actual Time |
|:--------------:|:-----------:|
| 6 hours        | 7 hours     |

---

### 🧪 QA, Testing, and Refactoring (Lab 5)
**Assignees:** @Tessa, @Monabbir, @Ashvin, @Gabriella | **Dates:** Mar 25–29, 2026

**Framework:** Manual End-to-End Testing & Code Review

**Coverage & Fixes:**
- [x] **Tessa:** Tested User account registration, Professor course creation, Student course joining.
- [x] **Monabbir:** Tested Professor group formation creation, Student preference submission, Professor roster viewing.
- [x] **Ashvin:** Tested User login, Professor publishing weekly surveys, Student health check-in submissions.
- [x] **Code Smells Fixed:** Addressed Duplicated Code (Form Validation), Magic Numbers (Algorithm Service), and Dead Code (Obsolete Repositories).

| Estimated Time | Actual Time |
|:--------------:|:-----------:|
| 6 hours        | 8 hours     |

---

### 📝 Documentation & Course Management Features
**Assignees:** @Tessa, @Gabriella | **Dates:** Mar 15–29, 2026

**Completed:**
- [x] Wrote and refined the comprehensive System Architecture overview.
- [x] Designed the relational Database Schema and generated the Mermaid.js UML Entity-Relationship diagram.
- [x] Compiled the Iteration 3 Log, Team Dynamics feedback, and Lab 5 QA reporting.
- [x] Assisted with course management documentation and requirement tracking.

| Estimated Time | Actual Time |
|:--------------:|:-----------:|
| 4 hours        | 5 hours     |

---

## 📖 User Story Development Summary

### User Story 1 — Professor can create group formations
Professors can now create highly customizable group formation activities, define precise minimum/maximum team sizes, set deadlines, and build a dynamic list of survey questions (Select/Rating) to evaluate students.

### User Story 2 — Student can set group preferences
Students access pending surveys for their enrolled courses and submit answers to the professor's dynamic questions, alongside their availability slots, driving the data required for the sorting algorithm.

### User Story 3 — Professor can view enrolled students / Manage Health Check-ins
Professors have full visibility of the course roster, can publish weekly surveys, and monitor team dynamics through student-submitted health, workload, and collaboration scores.

---

## 🏗 System Architecture Reorganization
**Date:** Mar 22, 2026

The system architecture was refined to handle the external AI dependency. We introduced a dedicated `OpenAiGroupSortingService` to act as an intermediary, isolating the HTTP/JSON parsing logic away from the core `GroupFormationService`. We also shifted away from hardcoded entity relationships for topics/skills, moving toward a generic survey-response schema that relies on logical application-level links.

---

## 🔄 Plan Changes from Iteration 2

**Original Plan:**
Use a hardcoded, static list of exactly 5 topics and 5 skills for students to choose from during group formation.

**Revised Plan:**
Implemented a fully dynamic survey builder utilizing `survey_questions` and `survey_question_options` tables.

**Reason:** The original plan was too rigid for different types of courses. A dynamic survey allows professors to tailor questions specifically to their syllabus (e.g., asking about programming languages in a CS course, or research interests in a Humanities course). It also provides much better contextual data for the OpenAI sorting algorithm to form balanced groups.

---

## 📅 Meetings
- **Mar 18:** Ad-hoc meeting to address technical ambiguities regarding linking group preference models to the frontend displays.
- **Mar 23:** Structured brainstorming session to determine the most efficient technical path for integrating the OpenAI sorting feature.
- **Mar 27:** QA sync to distribute end-to-end testing responsibilities (ensuring cross-testing) and to redistribute workload due to a non-contributing team member.

---

## ⏱ Time Summary

| Member     | Estimated (hrs) | Actual (hrs) |
|------------|:---------------:|:------------:|
| Tessa      | 8               | 8            |
| Gabriella  | 8               | 7            |
| Monabbir   | 10              | 13           |
| Ashvin     | 14              | 16           |
| Sandeepon  | 4               | 0            |

---

## 📌 Current Status
StudyPilot is currently a highly functional, monolithic Spring Boot MVC application. The core workflows for both Professors and Students are complete. Authentication, course management, dynamic group formation (with auto-sorting), and weekly health check-ins are fully integrated between the PostgreSQL database and the Thymeleaf-rendered frontend. 

## 🚀 Next Steps for Delivery
- [ ] Final polish of the UI/UX across all Thymeleaf templates.
- [ ] Complete final deployment to a live hosting environment (e.g., Heroku, AWS, or Render) if required.
- [ ] Preparation of the final demonstration/presentation highlighting the AI auto-sort and the professor analytics dashboard.


