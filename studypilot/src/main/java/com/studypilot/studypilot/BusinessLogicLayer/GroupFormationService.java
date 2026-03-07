package com.studypilot.studypilot.BusinessLogicLayer;

import com.studypilot.studypilot.DataAccessLayer.CourseRepo;
import com.studypilot.studypilot.DataAccessLayer.GroupFormationActivityRepo;
import com.studypilot.studypilot.DataAccessLayer.GroupFormationSkillOptionRepo;
import com.studypilot.studypilot.DataAccessLayer.GroupFormationTopicOptionRepo;
import com.studypilot.studypilot.DomainModel.Course;
import com.studypilot.studypilot.DomainModel.GroupFormationActivity;
import com.studypilot.studypilot.DomainModel.GroupFormationSkillOption;
import com.studypilot.studypilot.DomainModel.GroupFormationTopicOption;
import com.studypilot.studypilot.GUILayer.CreateGroupFormationForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class GroupFormationService {

    private final CourseRepo courseRepo;
    private final GroupFormationActivityRepo activityRepo;
    private final GroupFormationTopicOptionRepo topicRepo;
    private final GroupFormationSkillOptionRepo skillRepo;

    public GroupFormationService(CourseRepo courseRepo,
                                 GroupFormationActivityRepo activityRepo,
                                 GroupFormationTopicOptionRepo topicRepo,
                                 GroupFormationSkillOptionRepo skillRepo) {
        this.courseRepo = courseRepo;
        this.activityRepo = activityRepo;
        this.topicRepo = topicRepo;
        this.skillRepo = skillRepo;
    }

    @Transactional
    public GroupFormationActivity createActivity(Long professorId, String courseId, CreateGroupFormationForm form) {
        Course course = validateProfessorOwnsCourse(professorId, courseId);

        String activityName = clean(form.getActivityName());
        int preferred = requiredPositive(form.getPreferredGroupSize(), "Preferred group size is required.");
        int min = requiredPositive(form.getMinTeamSize(), "Minimum team size is required.");
        int max = requiredPositive(form.getMaxTeamSize(), "Maximum team size is required.");

        validateSizes(preferred, min, max);

        List<String> topics = normalizeFiveOptions(
                form.getTopic1(), form.getTopic2(), form.getTopic3(), form.getTopic4(), form.getTopic5(),
                "You must enter exactly 5 non-empty topic options."
        );

        List<String> skills = normalizeFiveOptions(
                form.getSkill1(), form.getSkill2(), form.getSkill3(), form.getSkill4(), form.getSkill5(),
                "You must enter exactly 5 non-empty skill options."
        );

        GroupFormationActivity activity = new GroupFormationActivity(
                course.getId(),
                professorId,
                activityName,
                preferred,
                min,
                max,
                form.isGroupTopicsSimilarly(),
                form.isGroupSkillsSimilarly()
        );

        GroupFormationActivity saved = activityRepo.save(activity);

        saveTopics(saved.getId(), topics);
        saveSkills(saved.getId(), skills);

        return saved;
    }

    public List<GroupFormationActivity> getActivitiesForCourse(String courseId) {
        return activityRepo.findByCourseIdOrderByCreatedAtDesc(courseId);
    }

    public CreateGroupFormationForm getEditForm(String courseId, Long activityId, Long professorId) {
        validateProfessorOwnsCourse(professorId, courseId);

        GroupFormationActivity activity = activityRepo.findByIdAndCourseId(activityId, courseId)
                .orElseThrow(() -> new IllegalArgumentException("Activity not found."));

        if (!activity.getProfessorId().equals(professorId)) {
            throw new IllegalArgumentException("You cannot edit this activity.");
        }

        List<GroupFormationTopicOption> topics = topicRepo.findByActivityIdOrderByOptionOrderAsc(activityId);
        List<GroupFormationSkillOption> skills = skillRepo.findByActivityIdOrderByOptionOrderAsc(activityId);

        if (topics.size() != 5 || skills.size() != 5) {
            throw new IllegalArgumentException("Saved activity data is incomplete.");
        }

        CreateGroupFormationForm form = new CreateGroupFormationForm();
        form.setActivityName(activity.getActivityName());
        form.setPreferredGroupSize(activity.getPreferredGroupSize());
        form.setMinTeamSize(activity.getMinTeamSize());
        form.setMaxTeamSize(activity.getMaxTeamSize());

        form.setTopic1(topics.get(0).getTopicText());
        form.setTopic2(topics.get(1).getTopicText());
        form.setTopic3(topics.get(2).getTopicText());
        form.setTopic4(topics.get(3).getTopicText());
        form.setTopic5(topics.get(4).getTopicText());

        form.setSkill1(skills.get(0).getSkillText());
        form.setSkill2(skills.get(1).getSkillText());
        form.setSkill3(skills.get(2).getSkillText());
        form.setSkill4(skills.get(3).getSkillText());
        form.setSkill5(skills.get(4).getSkillText());

        form.setGroupTopicsSimilarly(activity.isGroupTopicsSimilarly());
        form.setGroupSkillsSimilarly(activity.isGroupSkillsSimilarly());

        return form;
    }

    @Transactional
    public void updateActivity(Long professorId, String courseId, Long activityId, CreateGroupFormationForm form) {
        validateProfessorOwnsCourse(professorId, courseId);

        GroupFormationActivity activity = activityRepo.findByIdAndCourseId(activityId, courseId)
                .orElseThrow(() -> new IllegalArgumentException("Activity not found."));

        if (!activity.getProfessorId().equals(professorId)) {
            throw new IllegalArgumentException("You cannot edit this activity.");
        }

        String activityName = clean(form.getActivityName());
        int preferred = requiredPositive(form.getPreferredGroupSize(), "Preferred group size is required.");
        int min = requiredPositive(form.getMinTeamSize(), "Minimum team size is required.");
        int max = requiredPositive(form.getMaxTeamSize(), "Maximum team size is required.");

        validateSizes(preferred, min, max);

        List<String> topics = normalizeFiveOptions(
                form.getTopic1(), form.getTopic2(), form.getTopic3(), form.getTopic4(), form.getTopic5(),
                "You must enter exactly 5 non-empty topic options."
        );

        List<String> skills = normalizeFiveOptions(
                form.getSkill1(), form.getSkill2(), form.getSkill3(), form.getSkill4(), form.getSkill5(),
                "You must enter exactly 5 non-empty skill options."
        );

        activity.setActivityName(activityName);
        activity.setPreferredGroupSize(preferred);
        activity.setMinTeamSize(min);
        activity.setMaxTeamSize(max);
        activity.setGroupTopicsSimilarly(form.isGroupTopicsSimilarly());
        activity.setGroupSkillsSimilarly(form.isGroupSkillsSimilarly());

        activityRepo.save(activity);
        activityRepo.flush();

        topicRepo.deleteByActivityId(activityId);
        topicRepo.flush();
        saveTopics(activityId, topics);
        topicRepo.flush();

        skillRepo.deleteByActivityId(activityId);
        skillRepo.flush();
        saveSkills(activityId, skills);
        skillRepo.flush();
    }

    @Transactional
    public void deleteActivity(Long professorId, String courseId, Long activityId) {
        validateProfessorOwnsCourse(professorId, courseId);

        GroupFormationActivity activity = activityRepo.findByIdAndCourseId(activityId, courseId)
                .orElseThrow(() -> new IllegalArgumentException("Activity not found."));

        if (!activity.getProfessorId().equals(professorId)) {
            throw new IllegalArgumentException("You cannot delete this activity.");
        }

        activityRepo.delete(activity);
    }

    private Course validateProfessorOwnsCourse(Long professorId, String courseId) {
        if (professorId == null) {
            throw new IllegalArgumentException("Professor must be logged in.");
        }

        Course course = courseRepo.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found."));

        if (!course.getProfessorId().equals(professorId)) {
            throw new IllegalArgumentException("You cannot access this course.");
        }

        return course;
    }

    private void validateSizes(int preferred, int min, int max) {
        if (cleanInt(preferred) < 2) {
            throw new IllegalArgumentException("Preferred group size must be at least 2.");
        }
        if (cleanInt(min) < 2) {
            throw new IllegalArgumentException("Minimum team size must be at least 2.");
        }
        if (min > preferred) {
            throw new IllegalArgumentException("Minimum team size cannot be greater than preferred group size.");
        }
        if (preferred > max) {
            throw new IllegalArgumentException("Preferred group size cannot be greater than maximum team size.");
        }
    }

    private void saveTopics(Long activityId, List<String> topics) {
        List<GroupFormationTopicOption> rows = new ArrayList<>();
        for (int i = 0; i < topics.size(); i++) {
            rows.add(new GroupFormationTopicOption(activityId, i + 1, topics.get(i)));
        }
        topicRepo.saveAll(rows);
    }

    private void saveSkills(Long activityId, List<String> skills) {
        List<GroupFormationSkillOption> rows = new ArrayList<>();
        for (int i = 0; i < skills.size(); i++) {
            rows.add(new GroupFormationSkillOption(activityId, i + 1, skills.get(i)));
        }
        skillRepo.saveAll(rows);
    }

    private List<String> normalizeFiveOptions(String a, String b, String c, String d, String e, String errorMessage) {
        List<String> raw = List.of(clean(a), clean(b), clean(c), clean(d), clean(e));

        if (raw.stream().anyMatch(String::isBlank)) {
            throw new IllegalArgumentException(errorMessage);
        }

        Set<String> unique = new LinkedHashSet<>();
        for (String value : raw) {
            unique.add(value.toLowerCase());
        }

        if (unique.size() != 5) {
            throw new IllegalArgumentException("All 5 options must be different.");
        }

        return raw;
    }

    private int requiredPositive(Integer value, String message) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private int cleanInt(int value) {
        return value;
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}