StudyPilot – Iteration 2 Log
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
  - **SELECT** — students choose one or more options from a list
  - **RATING** — students rate each option on a scale of 1–5
- Added a per-question grouping strategy toggle professors set when building the survey:
  - **SIMILAR** — group students who gave similar answers to this question
  - **DIVERSE** — group students who gave varied answers to this question
- Added support for professors to define any number of questions with any number of custom options during activity creation and editing
- Implemented student-facing survey form that dynamically renders all professor-defined questions at the correct type (checkboxes for SELECT, rating buttons for RATING)
- Added availability calendar to the student survey form so students can indicate what times they are free to meet
- Implemented saving and pre-population of previous student responses when revisiting a survey
- Integrated all survey responses, availability, and per-question grouping strategies into the AI sorting prompt

**AI Integration:**
- Wired OpenAI GPT 5.4 nano as the sorting backend using Java's `java.net.http.HttpClient` (no SDK)
- Built the AI prompt to include each question's title, type, grouping strategy, and every student's responses so the model can form groups intelligently
- Used `max_completion_tokens` parameter required by GPT 5.4 nano (replaces `max_tokens` which the model rejects)
- Parsed the AI's structured JSON response into actual formed groups stored in the database

**Drag-and-Drop Group Editing Fix:**
- Fixed the professor groups page drag-and-drop so moving a student between groups actually updates the database, not just the UI
- Fixed a unique constraint violation that occurred during group membership reassignment by flushing pending deletes before inserting
- Fixed the fetch response so the browser correctly reads the server's JSON reply, eliminating the silent "Network error" that appeared even when the DB update succeeded

**Design and Implementation Work:**
- Added three new domain model entities: `SurveyQuestion`, `SurveyQuestionOption`, `SurveyResponse`
- Added repository interfaces for each new entity with the query methods needed for creation, retrieval, and cleanup
- Removed two unapproved entity files (`GroupFormationQuestion`, `GroupFormationQuestionOption`) that had been introduced by an unreviewed branch and replaced their role with the new entities
- Rewrote `CreateGroupFormationForm` to use Spring MVC indexed binding for dynamic question lists instead of JSON serialization
- Rewrote `StudentGroupPreferenceForm` to use a `Map<String, String>` keyed by question ID for flexible response collection
- Rewrote `GroupFormationService` to save, load, update, and delete survey questions and options alongside each activity
- Rewrote `StudentPortalService` to load survey questions, group options by question, retrieve saved responses, and upsert responses on submission
- Rewrote `OpenAiGroupSortingService` to build and send a structured prompt containing all question and response data
- Updated `StudentHomeController` to pass survey questions, grouped options, and saved responses into the model for the student survey page
- Updated `GroupFormationController` to return explicit `ResponseEntity<Map>` with correct `Content-Type: application/json` header for the drag-and-drop endpoint
- Removed the `com.google.code.gson:gson` dependency from `build.gradle` since all serialization was replaced with native string building

**Database Work:**
- Added `survey_questions` table to store professor-defined questions per activity with type and grouping strategy
- Added `survey_question_options` table to store the answer options for each question in order
- Added `survey_responses` table with a unique constraint on `(activity_id, student_id, question_id)` to store each student's answer per question
- Added `availability_slots` and `question_responses` columns to `student_group_preferences` with `columnDefinition` defaults for safe Hibernate `ddl-auto=update` migration against existing rows
- Stored SELECT responses as comma-separated option texts and RATING responses as `Option:Rating,Option:Rating` format in a single `response_value` column

**Testing / Verification:**
- Verified professor can create a survey with multiple SELECT and RATING questions and custom options
- Verified student survey page renders all questions correctly with the right input type per question
- Verified student responses are saved and pre-populated correctly on revisit
- Verified auto-sort sends all question/response data to the AI and produces non-sequential group assignments
- Verified drag-and-drop group editing persists to the database and resolves without unique constraint errors
- Confirmed all activity lifecycle states (OPEN, CLOSED, SORTED) render the correct view on the student page

| Estimated Time | Actual Time |
|----------------|-------------|
| 12 hours       | 14 hours    |

---

## DATABASE SCHEMA EXTENSION FOR FLEXIBLE SURVEY SYSTEM
**Mar 2026 — Ashvin**

**Completed:**
- Designed relational schema to replace fixed topic/skill option tables with a general-purpose survey question and response structure
- Added `survey_questions` table with question type and per-question grouping strategy fields
- Added `survey_question_options` table linked to questions with ordering support
- Added `survey_responses` table with a unique constraint ensuring one response per student per question per activity
- Extended `student_group_preferences` with `availability_slots` and `question_responses` columns using Hibernate-compatible `columnDefinition` defaults to avoid null violations on existing rows

**Design Rationale:**  
The schema was structured so that questions, options, and responses are fully decoupled from the activity's fixed structure, allowing professors to define any survey shape without schema changes. Storing responses in a single `response_value` column with a type-specific format (comma-separated for SELECT, colon-pair format for RATING) keeps the table simple while still allowing the AI to distinguish response types from the question record.

| Estimated Time | Actual Time |
|----------------|-------------|
| 2 hours        | 2 hours     |

---

## STUDENT SURVEY PAGE AND PROFESSOR GROUPS PAGE OVERHAUL
**Mar 2026 — Ashvin**

**Implemented:**
- Rebuilt the student group formation page to dynamically render professor-defined survey questions instead of hardcoded topic/skill dropdowns
- Added SELECT question rendering with checkboxes, hidden input updated via JavaScript, and pre-population of saved selections on page load
- Added RATING question rendering with styled 1–5 radio buttons per option, visual highlighting of the selected rating, and pre-population of saved ratings on page load
- Added availability calendar section with toggleable time slots and hidden input tracking
- Rebuilt the professor activity creation and edit pages to support dynamic question building with add/remove question and add/remove option controls
- Updated the professor groups page fetch error handling to display the actual server error message rather than a generic network error

**Notes:**  
All JavaScript for the survey form (SELECT checkbox aggregation, RATING string building, availability slot tracking, and saved-state restoration on DOMContentLoaded) is written inline without any external dependencies.

| Estimated Time | Actual Time |
|----------------|-------------|
| 4 hours        | 5 hours     |

