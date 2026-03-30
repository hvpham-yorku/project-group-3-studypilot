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
---

## MEETINGS

**

## TIME SUMMARY

| Member     | Estimated | Actual |
|------------|-----------|--------|
| Tessa      |           |        |
| Gabriella  |           |        |
| Monabbir   |           |        |
| Ashvin     |     14    |   16   |

---


