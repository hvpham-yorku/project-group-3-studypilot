# Database Structure

Current schema source:

- Database engine: PostgreSQL
- Schema management: JPA/Hibernate with `spring.jpa.hibernate.ddl-auto=update`
- Source of truth: entity classes in `src/main/java/com/studypilot/studypilot/DomainModel`

Notes:

- This project mostly models relationships as scalar ID columns such as `course_id` and `student_id` rather than JPA associations.
- That means the relationships below are logical/application-level links unless a unique constraint is explicitly listed.
- If a `String` column has no explicit `length`, Hibernate default sizing applies.

## Core Tables

### `users`

- `id`: BIGINT, primary key, auto-increment
- `email`: VARCHAR(320), not null, unique
- `password_hash`: string, not null
- `role`: string, not null
- `full_name`: string, not null
- `created_at`: TIMESTAMP WITH TIME ZONE, not null

### `courses`

- `id`: VARCHAR(32), primary key, not null
- `course_code`: string, not null
- `course_name`: string, not null
- `join_code`: VARCHAR(8), unique, nullable
- `professor_id`: BIGINT, not null, logical reference to `users.id`
- `created_at`: TIMESTAMP WITH TIME ZONE, not null

### `course_enrollments`

- `id`: BIGINT, primary key, auto-increment
- `course_id`: VARCHAR(32), not null, logical reference to `courses.id`
- `student_id`: BIGINT, not null, logical reference to `users.id`
- `created_at`: TIMESTAMP WITH TIME ZONE, not null

### `course_time_slots`

- `id`: BIGINT, primary key, auto-increment
- `course_id`: VARCHAR(32), not null, logical reference to `courses.id`
- `slot_label`: VARCHAR(50), not null

### `student_availability`

- `id`: BIGINT, primary key, auto-increment
- `student_id`: BIGINT, not null, logical reference to `users.id`
- `course_id`: VARCHAR(32), not null, logical reference to `courses.id`
- `time_slot`: VARCHAR(50), not null
- `created_at`: TIMESTAMP WITH TIME ZONE, not null

## Group Formation Tables

### `group_formation_activities`

- `id`: BIGINT, primary key, auto-increment
- `course_id`: VARCHAR(32), not null, logical reference to `courses.id`
- `professor_id`: BIGINT, not null, logical reference to `users.id`
- `activity_name`: VARCHAR(150), not null
- `preferred_group_size`: INTEGER, not null
- `min_team_size`: INTEGER, not null
- `max_team_size`: INTEGER, not null
- `group_topics_similarly`: BOOLEAN, default `true`
- `group_skills_similarly`: BOOLEAN, default `false`
- `status`: VARCHAR(20), default `OPEN`
- `deadline`: TIMESTAMP WITH TIME ZONE, nullable
- `created_at`: TIMESTAMP WITH TIME ZONE, not null

### `group_formation_topic_options`

- `id`: BIGINT, primary key, auto-increment
- `activity_id`: BIGINT, not null, logical reference to `group_formation_activities.id`
- `option_order`: INTEGER, not null
- `topic_text`: VARCHAR(120), not null

### `group_formation_skill_options`

- `id`: BIGINT, primary key, auto-increment
- `activity_id`: BIGINT, not null, logical reference to `group_formation_activities.id`
- `option_order`: INTEGER, not null
- `skill_text`: VARCHAR(120), not null

### `student_group_preferences`

- `id`: BIGINT, primary key, auto-increment
- `activity_id`: BIGINT, not null, logical reference to `group_formation_activities.id`
- `course_id`: VARCHAR(32), not null, logical reference to `courses.id`
- `student_id`: BIGINT, not null, logical reference to `users.id`
- `topic_choice`: VARCHAR(120), default empty string
- `skill_choice`: VARCHAR(120), default empty string
- `notes`: VARCHAR(600), nullable
- `question_responses`: VARCHAR(8000), nullable
- `availability_slots`: VARCHAR(1000), nullable
- `updated_at`: TIMESTAMP WITH TIME ZONE, not null
- Unique constraint: (`activity_id`, `student_id`)

### `formed_groups`

- `id`: BIGINT, primary key, auto-increment
- `activity_id`: BIGINT, not null, logical reference to `group_formation_activities.id`
- `course_id`: VARCHAR(32), not null, logical reference to `courses.id`
- `group_number`: INTEGER, not null
- `group_name`: VARCHAR(150), not null
- `created_at`: TIMESTAMP WITH TIME ZONE, not null

### `formed_group_members`

- `id`: BIGINT, primary key, auto-increment
- `formed_group_id`: BIGINT, not null, logical reference to `formed_groups.id`
- `student_id`: BIGINT, not null, logical reference to `users.id`
- Unique constraint: (`formed_group_id`, `student_id`)

## Survey Tables

### `survey_questions`

- `id`: BIGINT, primary key, auto-increment
- `activity_id`: BIGINT, not null, logical reference to `group_formation_activities.id`
- `question_order`: INTEGER, not null
- `question_title`: VARCHAR(200), not null
- `question_type`: VARCHAR(20), not null
- `grouping_strategy`: VARCHAR(20), not null

### `survey_question_options`

- `id`: BIGINT, primary key, auto-increment
- `question_id`: BIGINT, not null, logical reference to `survey_questions.id`
- `option_order`: INTEGER, not null
- `option_text`: VARCHAR(150), not null

### `survey_responses`

- `id`: BIGINT, primary key, auto-increment
- `activity_id`: BIGINT, not null, logical reference to `group_formation_activities.id`
- `student_id`: BIGINT, not null, logical reference to `users.id`
- `question_id`: BIGINT, not null, logical reference to `survey_questions.id`
- `response_value`: VARCHAR(2000), not null
- `updated_at`: TIMESTAMP WITH TIME ZONE, not null
- Unique constraint: (`activity_id`, `student_id`, `question_id`)

### `weekly_surveys`

- `id`: BIGINT, primary key, auto-increment
- `course_id`: VARCHAR(32), not null, logical reference to `courses.id`
- `professor_id`: BIGINT, not null, logical reference to `users.id`
- `week_start`: DATE, not null
- `title`: VARCHAR(120), not null
- `description`: VARCHAR(1000), not null
- `created_at`: TIMESTAMP WITH TIME ZONE, not null
- `updated_at`: TIMESTAMP WITH TIME ZONE, not null
- Unique constraint: (`course_id`, `week_start`)

## Team Tables

### `teams`

- `id`: BIGINT, primary key, auto-increment
- `activity_id`: BIGINT, not null, logical reference to `group_formation_activities.id`
- `course_id`: string, not null, logical reference to `courses.id`
- `team_name`: string, not null
- `created_at`: TIMESTAMP, not null

### `team_members`

- `id`: BIGINT, primary key, auto-increment
- `team_id`: BIGINT, not null, logical reference to `teams.id`
- `student_id`: BIGINT, not null, logical reference to `users.id`
- `created_at`: TIMESTAMP, not null

### `team_health_checkins`

- `id`: BIGINT, primary key, auto-increment
- `course_id`: VARCHAR(32), not null, logical reference to `courses.id`
- `student_id`: BIGINT, not null, logical reference to `users.id`
- `week_start`: DATE, not null
- `health_score`: INTEGER, not null
- `workload_score`: INTEGER, not null
- `collaboration_score`: INTEGER, not null
- `status_text`: VARCHAR(600), nullable
- `updated_at`: TIMESTAMP WITH TIME ZONE, not null
- Unique constraint: (`course_id`, `student_id`, `week_start`)

## Relationship Summary

- A `course` belongs to one professor through `courses.professor_id`.
- A `course_enrollment` links one student to one course.
- A `group_formation_activity` belongs to one course and one professor.
- Topic options, skill options, survey questions, and formed groups belong to one group formation activity.
- Survey question options belong to one survey question.
- Student group preferences and survey responses belong to one student for one activity.
- Formed group members belong to one formed group and one student.
- Teams belong to one activity and one course; team members belong to one team and one student.
- Weekly surveys and team health check-ins are course-scoped records.
