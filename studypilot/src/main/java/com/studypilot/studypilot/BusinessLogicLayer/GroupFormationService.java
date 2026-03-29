package com.studypilot.studypilot.BusinessLogicLayer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.studypilot.studypilot.DataAccessLayer.CourseRepo;
import com.studypilot.studypilot.DataAccessLayer.GroupFormationActivityRepo;
import com.studypilot.studypilot.DataAccessLayer.GroupFormationSkillOptionRepo;
import com.studypilot.studypilot.DataAccessLayer.GroupFormationTopicOptionRepo;
import com.studypilot.studypilot.DataAccessLayer.TeamMemberRepo;
import com.studypilot.studypilot.DataAccessLayer.TeamRepo;
import com.studypilot.studypilot.DomainModel.Course;
import com.studypilot.studypilot.DomainModel.GroupFormationActivity;
import com.studypilot.studypilot.DomainModel.GroupFormationSkillOption;
import com.studypilot.studypilot.DomainModel.GroupFormationTopicOption;
import com.studypilot.studypilot.DomainModel.Team;
import com.studypilot.studypilot.DomainModel.TeamMember;
import com.studypilot.studypilot.GUILayer.CreateGroupFormationForm;

@Service
/**
 * Domain service for managing group formation activities and generated teams.
 *
 * Includes CRUD for activities/options and team-generation persistence helpers.
 */
public class GroupFormationService {

    private final CourseRepo courseRepo;
    private final GroupFormationActivityRepo activityRepo;
    private final GroupFormationTopicOptionRepo topicRepo;
    private final GroupFormationSkillOptionRepo skillRepo;
    private final TeamRepo teamRepo;
    private final TeamMemberRepo teamMemberRepo;
    private final AvailabilityService availabilityService;
    private final GroupFormationAlgorithmService algorithmService;

    public GroupFormationService(
            CourseRepo courseRepo,
            GroupFormationActivityRepo activityRepo,
            GroupFormationTopicOptionRepo topicRepo,
            GroupFormationSkillOptionRepo skillRepo,
            TeamRepo teamRepo,
            TeamMemberRepo teamMemberRepo,
            AvailabilityService availabilityService,
            GroupFormationAlgorithmService algorithmService) {

        this.courseRepo = courseRepo;
        this.activityRepo = activityRepo;
        this.topicRepo = topicRepo;
        this.skillRepo = skillRepo;
        this.teamRepo = teamRepo;
        this.teamMemberRepo = teamMemberRepo;
        this.availabilityService = availabilityService;
        this.algorithmService = algorithmService;
    }

    @Transactional
    /**
     * Creates a group formation activity with validated topic/skill options.
     */
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

    /**
     * Returns activities for one course, newest first.
     */
    public List<GroupFormationActivity> getActivitiesForCourse(String courseId) {
        return activityRepo.findByCourseIdOrderByCreatedAtDesc(courseId);
    }

    /**
     * Returns generated teams for one course.
     */
    public List<Team> getTeamsForCourse(String courseId) {
        return teamRepo.findByCourseIdOrderByIdAsc(courseId);
    }

    /**
     * Loads an existing activity into an editable form object.
     */
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
    /**
     * Updates an existing activity and replaces its topic/skill option sets.
     */
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
    /**
     * Deletes an activity owned by the requesting professor.
     */
    public void deleteActivity(Long professorId, String courseId, Long activityId) {
        validateProfessorOwnsCourse(professorId, courseId);

        GroupFormationActivity activity = activityRepo.findByIdAndCourseId(activityId, courseId)
                .orElseThrow(() -> new IllegalArgumentException("Activity not found."));

        if (!activity.getProfessorId().equals(professorId)) {
            throw new IllegalArgumentException("You cannot delete this activity.");
        }

        activityRepo.delete(activity);
    }

    @Transactional
    /**
     * Generates teams from survey profiles and saves them to persistent team
     * tables.
     */
    public void saveSurveyGroups(Long activityId, String courseId, List<Long> studentIds) {

        List<GroupFormationAlgorithmService.StudentSurveyProfile> students = studentIds.stream()
                .map(id -> new GroupFormationAlgorithmService.StudentSurveyProfile(
                id,
                true,
                availabilityService.getStudentAvailabilitySet(id, courseId),
                new HashSet<>(),
                new HashSet<>(),
                2
        ))
                .collect(Collectors.toList());

        GroupFormationAlgorithmService.GroupingRequest request
                = new GroupFormationAlgorithmService.GroupingRequest(
                        3,
                        2,
                        5,
                        true,
                        true,
                        new HashSet<>(studentIds),
                        students
                );

        GroupFormationAlgorithmService.GroupingResult result
                = algorithmService.generateGroups(request, courseId);

        List<List<Long>> groups = result.teams().stream()
                .map(GroupFormationAlgorithmService.GroupTeam::memberIds)
                .collect(Collectors.toList());

        saveSurveyGroups(activityId, courseId, groups, null);
    }

    @Transactional
    /**
     * Persists already computed groups with optional custom team names.
     */
    public void saveSurveyGroups(Long activityId, String courseId, List<List<Long>> groups, List<String> groupNames) {

        for (int i = 0; i < groups.size(); i++) {
            List<Long> studentIds = groups.get(i);

            String teamName = (groupNames != null && i < groupNames.size() && groupNames.get(i) != null
                    && !groupNames.get(i).trim().isBlank())
                    ? groupNames.get(i).trim()
                    : "Team " + (i + 1);

            Team team = new Team();
            team.setActivityId(activityId);
            team.setCourseId(courseId);
            team.setTeamName(teamName);

            team = teamRepo.save(team);
            final Long teamId = team.getId();

            List<TeamMember> members = studentIds.stream()
                    .map(studentId -> new TeamMember(teamId, studentId))
                    .collect(Collectors.toList());

            teamMemberRepo.saveAll(members);
        }
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
        if (preferred < 2) {
            throw new IllegalArgumentException("Preferred group size must be at least 2.");
        }
        if (min < 2) {
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

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
