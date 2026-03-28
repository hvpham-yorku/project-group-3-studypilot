package com.studypilot.studypilot.BusinessLogicLayer;

import com.studypilot.studypilot.DataAccessLayer.CourseEnrollmentRepo;
import com.studypilot.studypilot.DataAccessLayer.CourseRepo;
import com.studypilot.studypilot.DataAccessLayer.FormedGroupMemberRepo;
import com.studypilot.studypilot.DataAccessLayer.FormedGroupRepo;
import com.studypilot.studypilot.DataAccessLayer.GroupFormationActivityRepo;
import com.studypilot.studypilot.DataAccessLayer.GroupFormationSkillOptionRepo;
import com.studypilot.studypilot.DataAccessLayer.GroupFormationTopicOptionRepo;
import com.studypilot.studypilot.DataAccessLayer.StudentGroupPreferenceRepo;
import com.studypilot.studypilot.DataAccessLayer.UserRepo;
import com.studypilot.studypilot.DomainModel.Course;
import com.studypilot.studypilot.DomainModel.CourseEnrollment;
import com.studypilot.studypilot.DomainModel.FormedGroup;
import com.studypilot.studypilot.DomainModel.FormedGroupMember;
import com.studypilot.studypilot.DomainModel.GroupFormationActivity;
import com.studypilot.studypilot.DomainModel.GroupFormationSkillOption;
import com.studypilot.studypilot.DomainModel.GroupFormationTopicOption;
import com.studypilot.studypilot.DomainModel.StudentGroupPreference;
import com.studypilot.studypilot.DomainModel.User;
import com.studypilot.studypilot.GUILayer.CreateGroupFormationForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GroupFormationService {

    private final CourseRepo courseRepo;
    private final GroupFormationActivityRepo activityRepo;
    private final GroupFormationTopicOptionRepo topicRepo;
    private final GroupFormationSkillOptionRepo skillRepo;
    private final StudentGroupPreferenceRepo preferenceRepo;
    private final CourseEnrollmentRepo enrollmentRepo;
    private final UserRepo userRepo;
    private final FormedGroupRepo formedGroupRepo;
    private final FormedGroupMemberRepo formedGroupMemberRepo;
    private final OpenAiGroupSortingService openAiSortingService;

    public GroupFormationService(CourseRepo courseRepo,
                                 GroupFormationActivityRepo activityRepo,
                                 GroupFormationTopicOptionRepo topicRepo,
                                 GroupFormationSkillOptionRepo skillRepo,
                                 StudentGroupPreferenceRepo preferenceRepo,
                                 CourseEnrollmentRepo enrollmentRepo,
                                 UserRepo userRepo,
                                 FormedGroupRepo formedGroupRepo,
                                 FormedGroupMemberRepo formedGroupMemberRepo,
                                 OpenAiGroupSortingService openAiSortingService) {
        this.courseRepo = courseRepo;
        this.activityRepo = activityRepo;
        this.topicRepo = topicRepo;
        this.skillRepo = skillRepo;
        this.preferenceRepo = preferenceRepo;
        this.enrollmentRepo = enrollmentRepo;
        this.userRepo = userRepo;
        this.formedGroupRepo = formedGroupRepo;
        this.formedGroupMemberRepo = formedGroupMemberRepo;
        this.openAiSortingService = openAiSortingService;
    }

    @Transactional
    public GroupFormationActivity createActivity(Long professorId, String courseId, CreateGroupFormationForm form) {
        Course course = validateProfessorOwnsCourse(professorId, courseId);

        String activityName = clean(form.getActivityName());
        int preferred = requiredPositive(form.getPreferredGroupSize(), "Preferred group size is required.");
        int min = requiredPositive(form.getMinTeamSize(), "Minimum team size is required.");
        int max = requiredPositive(form.getMaxTeamSize(), "Maximum team size is required.");

        validateSizes(preferred, min, max);

        List<String> topics = normalizeOptions(form.getTopics(), "You must enter at least 2 non-empty topic options.");
        List<String> skills = normalizeOptions(form.getSkills(), "You must enter at least 2 non-empty skill options.");

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

        if (form.getDeadline() != null && !form.getDeadline().isBlank()) {
            activity.setDeadline(parseDeadline(form.getDeadline()));
        }

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

        CreateGroupFormationForm form = new CreateGroupFormationForm();
        form.setActivityName(activity.getActivityName());
        form.setPreferredGroupSize(activity.getPreferredGroupSize());
        form.setMinTeamSize(activity.getMinTeamSize());
        form.setMaxTeamSize(activity.getMaxTeamSize());

        List<String> topicTexts = new ArrayList<>();
        for (GroupFormationTopicOption t : topics) {
            topicTexts.add(t.getTopicText());
        }
        form.setTopics(topicTexts);

        List<String> skillTexts = new ArrayList<>();
        for (GroupFormationSkillOption s : skills) {
            skillTexts.add(s.getSkillText());
        }
        form.setSkills(skillTexts);

        form.setGroupTopicsSimilarly(activity.isGroupTopicsSimilarly());
        form.setGroupSkillsSimilarly(activity.isGroupSkillsSimilarly());

        if (activity.getDeadline() != null) {
            form.setDeadline(activity.getDeadline().toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }

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

        List<String> topics = normalizeOptions(form.getTopics(), "You must enter at least 2 non-empty topic options.");
        List<String> skills = normalizeOptions(form.getSkills(), "You must enter at least 2 non-empty skill options.");

        activity.setActivityName(activityName);
        activity.setPreferredGroupSize(preferred);
        activity.setMinTeamSize(min);
        activity.setMaxTeamSize(max);
        activity.setGroupTopicsSimilarly(form.isGroupTopicsSimilarly());
        activity.setGroupSkillsSimilarly(form.isGroupSkillsSimilarly());

        if (form.getDeadline() != null && !form.getDeadline().isBlank()) {
            activity.setDeadline(parseDeadline(form.getDeadline()));
        } else {
            activity.setDeadline(null);
        }

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

    @Transactional
    public void closeActivity(Long professorId, String courseId, Long activityId) {
        validateProfessorOwnsCourse(professorId, courseId);

        GroupFormationActivity activity = activityRepo.findByIdAndCourseId(activityId, courseId)
                .orElseThrow(() -> new IllegalArgumentException("Activity not found."));

        if (!activity.getProfessorId().equals(professorId)) {
            throw new IllegalArgumentException("You cannot modify this activity.");
        }

        if ("SORTED".equals(activity.getStatus())) {
            throw new IllegalArgumentException("Activity has already been sorted.");
        }

        activity.setStatus("CLOSED");
        activityRepo.save(activity);
    }

    @Transactional
    public void reopenActivity(Long professorId, String courseId, Long activityId) {
        validateProfessorOwnsCourse(professorId, courseId);

        GroupFormationActivity activity = activityRepo.findByIdAndCourseId(activityId, courseId)
                .orElseThrow(() -> new IllegalArgumentException("Activity not found."));

        if (!activity.getProfessorId().equals(professorId)) {
            throw new IllegalArgumentException("You cannot modify this activity.");
        }

        if ("SORTED".equals(activity.getStatus())) {
            throw new IllegalArgumentException("Cannot reopen a sorted activity.");
        }

        activity.setStatus("OPEN");
        activityRepo.save(activity);
    }

    @Transactional
    public void autoSortActivity(Long professorId, String courseId, Long activityId) {
        validateProfessorOwnsCourse(professorId, courseId);

        GroupFormationActivity activity = activityRepo.findByIdAndCourseId(activityId, courseId)
                .orElseThrow(() -> new IllegalArgumentException("Activity not found."));

        if (!activity.getProfessorId().equals(professorId)) {
            throw new IllegalArgumentException("You cannot sort this activity.");
        }

        if (!"CLOSED".equals(activity.getStatus())) {
            throw new IllegalArgumentException("Activity must be closed before sorting.");
        }

        List<CourseEnrollment> enrollments = enrollmentRepo.findByCourseId(courseId);
        if (enrollments.isEmpty()) {
            throw new IllegalArgumentException("No students enrolled in this course.");
        }

        List<User> enrolledStudents = new ArrayList<>();
        for (CourseEnrollment enrollment : enrollments) {
            userRepo.findById(enrollment.getStudentId()).ifPresent(enrolledStudents::add);
        }

        if (enrolledStudents.size() < activity.getMinTeamSize()) {
            throw new IllegalArgumentException("Not enough students to form groups (need at least " + activity.getMinTeamSize() + ").");
        }

        List<StudentGroupPreference> preferences = preferenceRepo.findByActivityId(activityId);
        List<GroupFormationTopicOption> topics = topicRepo.findByActivityIdOrderByOptionOrderAsc(activityId);
        List<GroupFormationSkillOption> skills = skillRepo.findByActivityIdOrderByOptionOrderAsc(activityId);

        List<OpenAiGroupSortingService.GroupAssignment> assignments =
                openAiSortingService.sortStudents(activity, topics, skills, preferences, enrolledStudents);

        // Clear any previous groups for this activity
        List<FormedGroup> existingGroups = formedGroupRepo.findByActivityIdOrderByGroupNumberAsc(activityId);
        if (!existingGroups.isEmpty()) {
            List<Long> groupIds = existingGroups.stream().map(FormedGroup::getId).toList();
            formedGroupMemberRepo.deleteByFormedGroupIdIn(groupIds);
            formedGroupRepo.deleteByActivityId(activityId);
            formedGroupRepo.flush();
        }

        // Save new groups
        for (OpenAiGroupSortingService.GroupAssignment assignment : assignments) {
            FormedGroup group = new FormedGroup(
                    activityId,
                    courseId,
                    assignment.groupNumber(),
                    "Group " + assignment.groupNumber()
            );
            FormedGroup savedGroup = formedGroupRepo.save(group);

            for (Long studentId : assignment.studentIds()) {
                formedGroupMemberRepo.save(new FormedGroupMember(savedGroup.getId(), studentId));
            }
        }

        activity.setStatus("SORTED");
        activityRepo.save(activity);
    }

    public List<FormedGroupView> getFormedGroups(Long activityId) {
        List<FormedGroup> groups = formedGroupRepo.findByActivityIdOrderByGroupNumberAsc(activityId);
        if (groups.isEmpty()) {
            return List.of();
        }

        List<Long> groupIds = groups.stream().map(FormedGroup::getId).toList();
        List<FormedGroupMember> allMembers = formedGroupMemberRepo.findByFormedGroupIdIn(groupIds);

        Map<Long, List<FormedGroupMember>> membersByGroupId = allMembers.stream()
                .collect(Collectors.groupingBy(FormedGroupMember::getFormedGroupId));

        Map<Long, User> userCache = new HashMap<>();

        List<FormedGroupView> views = new ArrayList<>();
        for (FormedGroup group : groups) {
            List<FormedGroupMember> members = membersByGroupId.getOrDefault(group.getId(), List.of());
            List<GroupMemberView> memberViews = new ArrayList<>();

            for (FormedGroupMember member : members) {
                User user = userCache.computeIfAbsent(member.getStudentId(),
                        id -> userRepo.findById(id).orElse(null));

                if (user != null) {
                    memberViews.add(new GroupMemberView(user.getId(), user.getFullName(), user.getEmail()));
                }
            }

            views.add(new FormedGroupView(group.getId(), group.getGroupNumber(), group.getGroupName(), memberViews));
        }

        return views;
    }

    public ActivityResponseStatus getResponseStatus(Long activityId, String courseId) {
        List<CourseEnrollment> enrollments = enrollmentRepo.findByCourseId(courseId);
        int totalEnrolled = enrollments.size();

        List<StudentGroupPreference> preferences = preferenceRepo.findByActivityId(activityId);
        Set<Long> respondedIds = preferences.stream()
                .map(StudentGroupPreference::getStudentId)
                .collect(Collectors.toSet());

        Map<Long, User> userCache = new HashMap<>();
        List<GroupMemberView> responded = new ArrayList<>();
        List<GroupMemberView> notResponded = new ArrayList<>();

        for (CourseEnrollment enrollment : enrollments) {
            User user = userCache.computeIfAbsent(enrollment.getStudentId(),
                    id -> userRepo.findById(id).orElse(null));

            if (user == null) {
                continue;
            }

            GroupMemberView view = new GroupMemberView(user.getId(), user.getFullName(), user.getEmail());
            if (respondedIds.contains(user.getId())) {
                responded.add(view);
            } else {
                notResponded.add(view);
            }
        }

        return new ActivityResponseStatus(totalEnrolled, responded.size(), responded, notResponded);
    }

    @Transactional
    public void moveStudentBetweenGroups(Long professorId, String courseId, Long activityId,
                                          Long studentId, Long fromGroupId, Long toGroupId) {
        validateProfessorOwnsCourse(professorId, courseId);

        GroupFormationActivity activity = activityRepo.findByIdAndCourseId(activityId, courseId)
                .orElseThrow(() -> new IllegalArgumentException("Activity not found."));

        if (!activity.getProfessorId().equals(professorId)) {
            throw new IllegalArgumentException("You cannot modify this activity.");
        }

        FormedGroup fromGroup = formedGroupRepo.findById(fromGroupId)
                .orElseThrow(() -> new IllegalArgumentException("Source group not found."));
        FormedGroup toGroup = formedGroupRepo.findById(toGroupId)
                .orElseThrow(() -> new IllegalArgumentException("Target group not found."));

        if (!fromGroup.getActivityId().equals(activityId) || !toGroup.getActivityId().equals(activityId)) {
            throw new IllegalArgumentException("Groups do not belong to this activity.");
        }

        formedGroupMemberRepo.deleteByFormedGroupIdAndStudentId(fromGroupId, studentId);
        formedGroupMemberRepo.save(new FormedGroupMember(toGroupId, studentId));
    }

    public void checkAndCloseExpiredActivities(String courseId) {
        List<GroupFormationActivity> activities = activityRepo.findByCourseIdOrderByCreatedAtDesc(courseId);
        OffsetDateTime now = OffsetDateTime.now();

        for (GroupFormationActivity activity : activities) {
            if ("OPEN".equals(activity.getStatus()) && activity.getDeadline() != null
                    && now.isAfter(activity.getDeadline())) {
                activity.setStatus("CLOSED");
                activityRepo.save(activity);
            }
        }
    }

    private OffsetDateTime parseDeadline(String deadlineStr) {
        try {
            LocalDateTime ldt = LocalDateTime.parse(deadlineStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return ldt.atOffset(ZoneOffset.UTC);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid deadline format. Use the date/time picker.");
        }
    }

    public record FormedGroupView(
            Long groupId,
            int groupNumber,
            String groupName,
            List<GroupMemberView> members) {
    }

    public record GroupMemberView(
            Long studentId,
            String fullName,
            String email) {
    }

    public record ActivityResponseStatus(
            int totalEnrolled,
            int respondedCount,
            List<GroupMemberView> responded,
            List<GroupMemberView> notResponded) {
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

    private List<String> normalizeOptions(List<String> options, String errorMessage) {
        if (options == null || options.isEmpty()) {
            throw new IllegalArgumentException(errorMessage);
        }

        List<String> cleaned = new ArrayList<>();
        for (String option : options) {
            String trimmed = clean(option);
            if (!trimmed.isBlank()) {
                cleaned.add(trimmed);
            }
        }

        if (cleaned.size() < 2) {
            throw new IllegalArgumentException(errorMessage);
        }

        Set<String> unique = new LinkedHashSet<>();
        for (String value : cleaned) {
            unique.add(value.toLowerCase());
        }

        if (unique.size() != cleaned.size()) {
            throw new IllegalArgumentException("All options must be different.");
        }

        return cleaned;
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