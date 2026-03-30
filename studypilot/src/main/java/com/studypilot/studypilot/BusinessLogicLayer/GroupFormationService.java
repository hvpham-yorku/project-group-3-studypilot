package com.studypilot.studypilot.BusinessLogicLayer;

import com.studypilot.studypilot.DataAccessLayer.CourseEnrollmentRepo;
import com.studypilot.studypilot.DataAccessLayer.CourseRepo;
import com.studypilot.studypilot.DataAccessLayer.FormedGroupMemberRepo;
import com.studypilot.studypilot.DataAccessLayer.FormedGroupRepo;
import com.studypilot.studypilot.DataAccessLayer.GroupFormationActivityRepo;
import com.studypilot.studypilot.DataAccessLayer.StudentGroupPreferenceRepo;
import com.studypilot.studypilot.DataAccessLayer.SurveyQuestionOptionRepo;
import com.studypilot.studypilot.DataAccessLayer.SurveyQuestionRepo;
import com.studypilot.studypilot.DataAccessLayer.SurveyResponseRepo;
import com.studypilot.studypilot.DataAccessLayer.UserRepo;
import com.studypilot.studypilot.DomainModel.Course;
import com.studypilot.studypilot.DomainModel.CourseEnrollment;
import com.studypilot.studypilot.DomainModel.FormedGroup;
import com.studypilot.studypilot.DomainModel.FormedGroupMember;
import com.studypilot.studypilot.DomainModel.GroupFormationActivity;
import com.studypilot.studypilot.DomainModel.StudentGroupPreference;
import com.studypilot.studypilot.DomainModel.SurveyQuestion;
import com.studypilot.studypilot.DomainModel.SurveyQuestionOption;
import com.studypilot.studypilot.DomainModel.SurveyResponse;
import com.studypilot.studypilot.DomainModel.User;
import com.studypilot.studypilot.GUILayer.CreateGroupFormationForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GroupFormationService {

    private final CourseRepo courseRepo;
    private final GroupFormationActivityRepo activityRepo;
    private final SurveyQuestionRepo questionRepo;
    private final SurveyQuestionOptionRepo questionOptionRepo;
    private final SurveyResponseRepo surveyResponseRepo;
    private final StudentGroupPreferenceRepo preferenceRepo;
    private final CourseEnrollmentRepo enrollmentRepo;
    private final UserRepo userRepo;
    private final FormedGroupRepo formedGroupRepo;
    private final FormedGroupMemberRepo formedGroupMemberRepo;
    private final OpenAiGroupSortingService openAiSortingService;

    public GroupFormationService(CourseRepo courseRepo,
                                 GroupFormationActivityRepo activityRepo,
                                 SurveyQuestionRepo questionRepo,
                                 SurveyQuestionOptionRepo questionOptionRepo,
                                 SurveyResponseRepo surveyResponseRepo,
                                 StudentGroupPreferenceRepo preferenceRepo,
                                 CourseEnrollmentRepo enrollmentRepo,
                                 UserRepo userRepo,
                                 FormedGroupRepo formedGroupRepo,
                                 FormedGroupMemberRepo formedGroupMemberRepo,
                                 OpenAiGroupSortingService openAiSortingService) {
        this.courseRepo = courseRepo;
        this.activityRepo = activityRepo;
        this.questionRepo = questionRepo;
        this.questionOptionRepo = questionOptionRepo;
        this.surveyResponseRepo = surveyResponseRepo;
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
        validateQuestions(form.getQuestions());

        GroupFormationActivity activity = new GroupFormationActivity(
                course.getId(),
                professorId,
                activityName,
                preferred,
                min,
                max,
                true,
                false
        );

        if (form.getDeadline() != null && !form.getDeadline().isBlank()) {
            activity.setDeadline(parseDeadline(form.getDeadline()));
        }
        GroupFormationActivity saved = activityRepo.save(activity);

        saveQuestions(saved.getId(), form.getQuestions());

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

        List<SurveyQuestion> questions = questionRepo.findByActivityIdOrderByQuestionOrderAsc(activityId);

        CreateGroupFormationForm form = new CreateGroupFormationForm();
        form.setActivityName(activity.getActivityName());
        form.setPreferredGroupSize(activity.getPreferredGroupSize());
        form.setMinTeamSize(activity.getMinTeamSize());
        form.setMaxTeamSize(activity.getMaxTeamSize());

        List<CreateGroupFormationForm.QuestionForm> questionForms = new ArrayList<>();
        for (SurveyQuestion q : questions) {
            CreateGroupFormationForm.QuestionForm qf = new CreateGroupFormationForm.QuestionForm();
            qf.setTitle(q.getQuestionTitle());
            qf.setType(q.getQuestionType());
            qf.setStrategy(q.getGroupingStrategy());

            List<SurveyQuestionOption> opts = questionOptionRepo.findByQuestionIdOrderByOptionOrderAsc(q.getId());
            List<String> optTexts = new ArrayList<>();
            for (SurveyQuestionOption opt : opts) {
                optTexts.add(opt.getOptionText());
            }
            qf.setOptions(optTexts);
            questionForms.add(qf);
        }
        form.setQuestions(questionForms);

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
        validateQuestions(form.getQuestions());

        activity.setActivityName(activityName);
        activity.setPreferredGroupSize(preferred);
        activity.setMinTeamSize(min);
        activity.setMaxTeamSize(max);

        if (form.getDeadline() != null && !form.getDeadline().isBlank()) {
            activity.setDeadline(parseDeadline(form.getDeadline()));
        } else {
            activity.setDeadline(null);
        }

        activityRepo.save(activity);

        // Delete old questions and their options
        List<SurveyQuestion> oldQuestions = questionRepo.findByActivityIdOrderByQuestionOrderAsc(activityId);
        if (!oldQuestions.isEmpty()) {
            List<Long> oldQuestionIds = oldQuestions.stream().map(SurveyQuestion::getId).toList();
            questionOptionRepo.deleteByQuestionIdIn(oldQuestionIds);
            questionOptionRepo.flush();
            questionRepo.deleteByActivityId(activityId);
            questionRepo.flush();
        }

        saveQuestions(activityId, form.getQuestions());
    }

    @Transactional
    public void deleteActivity(Long professorId, String courseId, Long activityId) {
        validateProfessorOwnsCourse(professorId, courseId);
        GroupFormationActivity activity = activityRepo.findByIdAndCourseId(activityId, courseId)
                .orElseThrow(() -> new IllegalArgumentException("Activity not found."));
        if (!activity.getProfessorId().equals(professorId)) {
            throw new IllegalArgumentException("You cannot delete this activity.");
        }

        // Clean up survey questions and options
        List<SurveyQuestion> questions = questionRepo.findByActivityIdOrderByQuestionOrderAsc(activityId);
        if (!questions.isEmpty()) {
            List<Long> questionIds = questions.stream().map(SurveyQuestion::getId).toList();
            questionOptionRepo.deleteByQuestionIdIn(questionIds);
            questionRepo.deleteByActivityId(activityId);
        }

        surveyResponseRepo.deleteByActivityId(activityId);

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

        List<User> enrolledStudents = enrollmentRepo.findByCourseId(courseId).stream()
                .map(CourseEnrollment::getStudentId)
                .map(id -> userRepo.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .toList();
        if (enrolledStudents.size() < activity.getMinTeamSize()) {
            throw new IllegalArgumentException("Not enough students to form groups (need at least " + activity.getMinTeamSize() + ").");
        }

        List<SurveyQuestion> questions = questionRepo.findByActivityIdOrderByQuestionOrderAsc(activityId);
        List<Long> questionIds = questions.stream().map(SurveyQuestion::getId).toList();
        List<SurveyQuestionOption> allOptions = questionOptionRepo.findByQuestionIdIn(questionIds);
        List<SurveyResponse> responses = surveyResponseRepo.findByActivityId(activityId);
        List<StudentGroupPreference> preferences = preferenceRepo.findByActivityId(activityId);

        List<OpenAiGroupSortingService.GroupAssignment> assignments =
                openAiSortingService.sortStudents(activity, questions, allOptions, responses, preferences, enrolledStudents);

        List<FormedGroup> existingGroups = formedGroupRepo.findByActivityIdOrderByGroupNumberAsc(activityId);
        if (!existingGroups.isEmpty()) {
            formedGroupMemberRepo.deleteByFormedGroupIdIn(existingGroups.stream().map(FormedGroup::getId).toList());
            formedGroupRepo.deleteByActivityId(activityId);
            formedGroupRepo.flush();
        }

        for (OpenAiGroupSortingService.GroupAssignment assignment : assignments) {
            FormedGroup savedGroup = formedGroupRepo.save(
                    new FormedGroup(activityId, courseId, assignment.groupNumber(), "Group " + assignment.groupNumber()));
            for (Long studentId : assignment.studentIds()) {
                formedGroupMemberRepo.save(new FormedGroupMember(savedGroup.getId(), studentId));
            }
        }

        activity.setStatus("SORTED");
        activityRepo.save(activity);
    }

    public List<FormedGroupView> getFormedGroups(Long activityId) {
        List<FormedGroup> groups = formedGroupRepo.findByActivityIdOrderByGroupNumberAsc(activityId);
        if (groups.isEmpty()) return List.of();

        Map<Long, List<FormedGroupMember>> membersByGroupId = formedGroupMemberRepo
                .findByFormedGroupIdIn(groups.stream().map(FormedGroup::getId).toList())
                .stream().collect(Collectors.groupingBy(FormedGroupMember::getFormedGroupId));

        Map<Long, User> userCache = new HashMap<>();
        List<FormedGroupView> views = new ArrayList<>();
        for (FormedGroup group : groups) {
            List<GroupMemberView> memberViews = new ArrayList<>();
            for (FormedGroupMember member : membersByGroupId.getOrDefault(group.getId(), List.of())) {
                User user = userCache.computeIfAbsent(member.getStudentId(), id -> userRepo.findById(id).orElse(null));
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

        // Check who responded via survey_responses (new system)
        List<Long> respondedStudentIds = surveyResponseRepo.findDistinctStudentIdsByActivityId(activityId);
        Set<Long> respondedIds = new HashSet<>(respondedStudentIds);

        // Also check old-style preferences
        List<StudentGroupPreference> preferences = preferenceRepo.findByActivityId(activityId);
        for (StudentGroupPreference pref : preferences) {
            respondedIds.add(pref.getStudentId());
        }

        List<GroupMemberView> responded = new ArrayList<>();
        List<GroupMemberView> notResponded = new ArrayList<>();
        for (CourseEnrollment enrollment : enrollments) {
            User user = userRepo.findById(enrollment.getStudentId()).orElse(null);
            if (user == null) continue;
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

        List<FormedGroupMember> fromMembers = formedGroupMemberRepo.findByFormedGroupId(fromGroupId);
        boolean inSource = fromMembers.stream().anyMatch(m -> m.getStudentId().equals(studentId));
        if (!inSource) {
            throw new IllegalArgumentException("Student is not in the source group.");
        }

        List<FormedGroupMember> targetMembers = formedGroupMemberRepo.findByFormedGroupId(toGroupId);
        boolean alreadyInTarget = targetMembers.stream().anyMatch(m -> m.getStudentId().equals(studentId));
        if (alreadyInTarget) return;

        formedGroupMemberRepo.deleteByFormedGroupIdAndStudentId(fromGroupId, studentId);
        formedGroupMemberRepo.flush();
        formedGroupMemberRepo.save(new FormedGroupMember(toGroupId, studentId));
    }

    public void checkAndCloseExpiredActivities(String courseId) {
        OffsetDateTime now = OffsetDateTime.now();
        for (GroupFormationActivity activity : activityRepo.findByCourseIdOrderByCreatedAtDesc(courseId)) {
            if ("OPEN".equals(activity.getStatus()) && activity.getDeadline() != null && now.isAfter(activity.getDeadline())) {
                activity.setStatus("CLOSED");
                activityRepo.save(activity);
            }
        }
    }

    public List<SurveyQuestion> getQuestionsForActivity(Long activityId) {
        return questionRepo.findByActivityIdOrderByQuestionOrderAsc(activityId);
    }

    public List<SurveyQuestionOption> getOptionsForQuestions(List<Long> questionIds) {
        if (questionIds.isEmpty()) return List.of();
        return questionOptionRepo.findByQuestionIdIn(questionIds);
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
        if (professorId == null) throw new IllegalArgumentException("Professor must be logged in.");
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

    private void validateQuestions(List<CreateGroupFormationForm.QuestionForm> questions) {
        if (questions == null || questions.isEmpty()) {
            throw new IllegalArgumentException("You must add at least one survey question.");
        }

        for (int i = 0; i < questions.size(); i++) {
            CreateGroupFormationForm.QuestionForm q = questions.get(i);
            String title = q.getTitle() == null ? "" : q.getTitle().trim();
            if (title.isBlank()) {
                throw new IllegalArgumentException("Question " + (i + 1) + " must have a title.");
            }

            String type = q.getType();
            if (!"SELECT".equals(type) && !"RATING".equals(type)) {
                throw new IllegalArgumentException("Question " + (i + 1) + " must be SELECT or RATING type.");
            }

            String strategy = q.getStrategy();
            if (!"SIMILAR".equals(strategy) && !"DIVERSE".equals(strategy)) {
                throw new IllegalArgumentException("Question " + (i + 1) + " must specify SIMILAR or DIVERSE grouping.");
            }

            List<String> options = normalizeOptions(q.getOptions(),
                    "Question '" + title + "' must have at least 2 non-empty options.");

            q.setOptions(options);
        }
    }

    private void saveQuestions(Long activityId, List<CreateGroupFormationForm.QuestionForm> questions) {
        for (int i = 0; i < questions.size(); i++) {
            CreateGroupFormationForm.QuestionForm qf = questions.get(i);
            SurveyQuestion question = new SurveyQuestion(
                    activityId,
                    i + 1,
                    qf.getTitle().trim(),
                    qf.getType(),
                    qf.getStrategy()
            );
            SurveyQuestion savedQ = questionRepo.save(question);

            List<String> opts = qf.getOptions();
            for (int j = 0; j < opts.size(); j++) {
                questionOptionRepo.save(new SurveyQuestionOption(savedQ.getId(), j + 1, opts.get(j)));
            }
        }
    }

    private List<String> normalizeOptions(List<String> options, String errorMessage) {
        if (options == null || options.isEmpty()) {
            throw new IllegalArgumentException(errorMessage);
        }

        List<String> cleaned = new ArrayList<>();
        for (String option : options) {
            String trimmed = option == null ? "" : option.trim();
            if (!trimmed.isBlank()) {
                cleaned.add(trimmed);
            }
        }

        if (cleaned.size() < 2) {
            throw new IllegalArgumentException(errorMessage);
        }

        Set<String> unique = new LinkedHashSet<>();
        for (String value : cleaned) {
            unique.add(value.toLowerCase(Locale.ROOT));
        }

        if (unique.size() != cleaned.size()) {
            throw new IllegalArgumentException("All options must be different.");
        }

        return cleaned;
    }

    private int requiredPositive(Integer value, String message) {
        if (value == null || value <= 0) throw new IllegalArgumentException(message);
        return value;
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
