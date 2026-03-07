StudyPilot – Iteration 2 Log
=================================

TEAM MEMBERS
==================================================
* **Tessa Cloutier**
* **Gabriella Crook**
* **Monabbir Bhuiyan**
* **Ashvin Kawleswaran**
* **Sandeepon Saha**

## ITERATION 2 ADDITION 

## SURVEY-BASED GROUP FORMATION FEATURE
**Mar 2–5, 2026 — Ashvin**

**Implemented:**
- Extended the course space flow to support a survey-based group formation feature for professors
- Added navigation from the professor course space into the group formation section
- Implemented professor-side creation of group formation activities within a course
- Added form handling for:
  - Activity name
  - Preferred group size
  - Minimum team size
  - Maximum team size
  - 5 topic options
  - 5 skill options
  - Topic grouping preference
  - Skill grouping preference
- Added support to view previously created group formation activities inside the course space
- Implemented editing of existing activities
- Implemented deletion of existing activities

**Validation and Constraints Handled:**
- Enforced exactly 5 topic options and exactly 5 skill options
- Enforced non empty option values
- Enforced uniqueness of topic and skill options within an activity
- Enforced valid team size bounds between minimum, preferred, and maximum values

**Design and Implementation Work:**
- Added domain models for group formation activities and option storage
- Added repository interfaces for activity, topic option, and skill option persistence
- Added business logic to create, retrieve, update, and delete survey based group formation activities
- Added GUI/controller flow for professor interaction with the feature
- Added edit page logic so previously saved values load correctly into the form
- Resolved update/save issues related to replacing topic and skill options during activity edits

**Database Work:**
- Designed and prepared persistent PostgreSQL schema for survey based group formation
- Added tables for:
  - Group formation activities
  - Topic options
  - Skill options
  - Future student survey submissions

**Testing / Verification:**
- Verified creation flow for new survey/group formation activities
- Verified persistence of saved activities under the correct course
- Verified edit and delete flows after backend and repository fixes
- Confirmed feature behavior within the professor-side course space workflow

| Estimated Time | Actual Time |
|---------------|-------------|
| 5 hours       | 5 hours     |

---

## DATABASE SCHEMA EXTENSION FOR GROUP FORMATION
**Mar 4, 2026 — Ashvin**

**Completed:**
- Designed relational schema for the survey based team formation feature
- Added support for professor created activities and fixed topic/skill options
- Added enrollment and future ready student submission structures
- Added generated team/run structures for later algorithm integration
- Added constraints and exactly five option enforcement for the intended options of preferred topics and skills for students to choose from

**Design Rationale:**  
The schema was structured so the current professor side setup could work immediately while also supporting later expansion to student survey submissions and group generation without requiring major redesign.

| Estimated Time | Actual Time |
|---------------|-------------|
| 1 hours       | 1 hours     |

---

## PROFESSOR GUI EXPANSION FOR COURSE SPACE
**Mar 3–5, 2026 — Ashvin**

**Implemented:**
- Expanded the professor course space to include access to survey based group formation
- Built the professor facing form pages for activity creation and editing
- Added saved activity display so instructors can manage previously created setups
- Improved workflow so the feature functions as part of the existing course space experience rather than as a standalone placeholder page

**Notes:**  
Student survey submission and actual grouping algorithm execution were intentionally left for later integration.

| Estimated Time | Actual Time |
|---------------|-------------|
| 3 hours       | 4 hours     |

---

## UNIT TESTING
**Feb 13, 2026 — Tessa, Gabriella**

**Framework:** JUnit 5

**Current Coverage:**
- 

**Notes:**
Full unit testing deferred; focus was on delivering a functional system.

| Estimated Time | Actual Time |
|---------------|-------------|
| 2 hour        | 2 hours     |

---


## DOCUMENTATION
**Feb 13, 2026 — Ashvin, Gabriella, Monabbir, Tessa**

**Completed:**
- `log.md`
- GitHub Wiki
- Group Sorting Enforcement Constraints

| Estimated Time | Actual Time |
|---------------|-------------|
| 1 hour        | 2 hour      |

---

## USER STORY DEVELOPMENT SUMMARY

### User Story 1 —  

### User Story 2 — 

---

## SYSTEM ARCHITECTURE REORGANIZATION
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

## PLAN CHANGES FROM ITR1

**Original Plan:**
- 

**Revised Plan:**
- 

**Reason:**  


---

## MEETINGS

**

## TIME SUMMARY

| Member     | Estimated | Actual |
|------------|-----------|--------|
| Tessa      |     7     |   9    |
| Gabriella  |     8     |   9    |
| Monabbir   |     7     |   9    |
| Ashvin     |     7     |   9    |

---

## CURRENT STATUS



## NEXT STEPS for Delivery

