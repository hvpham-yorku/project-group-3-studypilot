DATABASE

- PostgreSQL
- Schema managed by JPA/Hibernate (ddl-auto=update)

TABLE users

- id: BIGINT, PK, auto-increment, NOT NULL
- email: VARCHAR(320), NOT NULL, UNIQUE
- password_hash: VARCHAR(255), NOT NULL
- role: VARCHAR(255), NOT NULL
- full_name: VARCHAR(255), NOT NULL
- created_at: TIMESTAMP WITH TIME ZONE, NOT NULL

TABLE courses

- id: VARCHAR(32), PK, NOT NULL
- course_code: VARCHAR(255), NOT NULL
- course_name: VARCHAR(255), NOT NULL
- join_code: VARCHAR(8), UNIQUE, NULL
- professor_id: BIGINT, NOT NULL
- created_at: TIMESTAMP WITH TIME ZONE, NOT NULL

TABLE course_enrollments

- id: BIGINT, PK, auto-increment, NOT NULL
- course_id: VARCHAR(32), NOT NULL
- student_id: BIGINT, NOT NULL
- created_at: TIMESTAMP WITH TIME ZONE, NOT NULL

TABLE group_formation_activities

- id: BIGINT, PK, auto-increment, NOT NULL
- course_id: VARCHAR(32), NOT NULL
- professor_id: BIGINT, NOT NULL
- activity_name: VARCHAR(150), NOT NULL
- preferred_group_size: INTEGER, NOT NULL
- min_team_size: INTEGER, NOT NULL
- max_team_size: INTEGER, NOT NULL
- group_topics_similarly: BOOLEAN, NOT NULL
- group_skills_similarly: BOOLEAN, NOT NULL
- created_at: TIMESTAMP WITH TIME ZONE, NOT NULL

TABLE group_formation_topic_options

- id: BIGINT, PK, auto-increment, NOT NULL
- activity_id: BIGINT, NOT NULL
- option_order: INTEGER, NOT NULL
- topic_text: VARCHAR(120), NOT NULL

TABLE group_formation_skill_options

- id: BIGINT, PK, auto-increment, NOT NULL
- activity_id: BIGINT, NOT NULL
- option_order: INTEGER, NOT NULL
- skill_text: VARCHAR(120), NOT NULL

TABLE student_group_preferences

- id: BIGINT, PK, auto-increment, NOT NULL
- activity_id: BIGINT, NOT NULL
- course_id: VARCHAR(32), NOT NULL
- student_id: BIGINT, NOT NULL
- topic_choice: VARCHAR(120), NOT NULL
- skill_choice: VARCHAR(120), NOT NULL
- notes: VARCHAR(600), NULL
- updated_at: TIMESTAMP WITH TIME ZONE, NOT NULL
- UNIQUE(activity_id, student_id)

TABLE team_health_checkins

- id: BIGINT, PK, auto-increment, NOT NULL
- course_id: VARCHAR(32), NOT NULL
- student_id: BIGINT, NOT NULL
- week_start: DATE, NOT NULL
- health_score: INTEGER, NOT NULL
- workload_score: INTEGER, NOT NULL
- collaboration_score: INTEGER, NOT NULL
- status_text: VARCHAR(600), NULL
- updated_at: TIMESTAMP WITH TIME ZONE, NOT NULL
- UNIQUE(course_id, student_id, week_start)

TABLE quiz_tests

- id: BIGINT, PK, auto-increment, NOT NULL
- course_id: VARCHAR(32), NOT NULL
- professor_id: BIGINT, NOT NULL
- title: VARCHAR(150), NOT NULL
- source_file_name: VARCHAR(255), NOT NULL
- created_at: TIMESTAMP WITH TIME ZONE, NOT NULL

TABLE quiz_questions

- id: BIGINT, PK, auto-increment, NOT NULL
- quiz_test_id: BIGINT, NOT NULL
- question_order: INTEGER, NOT NULL
- question_text: VARCHAR(500), NOT NULL
- option_a: VARCHAR(255), NOT NULL
- option_b: VARCHAR(255), NOT NULL
- option_c: VARCHAR(255), NOT NULL
- option_d: VARCHAR(255), NOT NULL
- correct_option: VARCHAR(1), NOT NULL

TABLE quiz_submissions

- id: BIGINT, PK, auto-increment, NOT NULL
- quiz_test_id: BIGINT, NOT NULL
- course_id: VARCHAR(32), NOT NULL
- student_id: BIGINT, NOT NULL
- score: INTEGER, NOT NULL
- total_questions: INTEGER, NOT NULL
- submitted_at: TIMESTAMP WITH TIME ZONE, NOT NULL

TABLE quiz_submission_answers

- id: BIGINT, PK, auto-increment, NOT NULL
- submission_id: BIGINT, NOT NULL
- question_id: BIGINT, NOT NULL
- selected_option: VARCHAR(1), NOT NULL
- is_correct: BOOLEAN, NOT NULL
