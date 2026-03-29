package com.studypilot.studypilot.GUILayer;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.studypilot.studypilot.BusinessLogicLayer.CourseService;
import com.studypilot.studypilot.BusinessLogicLayer.TeamHealthService;
import com.studypilot.studypilot.DataAccessLayer.CourseEnrollmentRepo;
import com.studypilot.studypilot.DataAccessLayer.UserRepo;
import com.studypilot.studypilot.DomainModel.Course;
import com.studypilot.studypilot.DomainModel.CourseEnrollment;
import com.studypilot.studypilot.DomainModel.TeamHealthCheckin;
import com.studypilot.studypilot.DomainModel.User;
import com.studypilot.studypilot.DomainModel.WeeklySurvey;

import jakarta.servlet.http.HttpSession;

@Controller
/**
 * Professor dashboard and course-space controller.
 *
 * Responsibilities: 1) render professor home analytics, 2) create courses, 3)
 * render course pages, 4) publish and review weekly surveys.
 */
public class ProfessorHomeController {

    private static final Pattern NON_ALNUM = Pattern.compile("[^a-z0-9]+");

    private final CourseService courseService;

    private final TeamHealthService teamHealthService;
    private final CourseEnrollmentRepo courseEnrollmentRepo;
    private final UserRepo userRepo;

    public ProfessorHomeController(CourseService courseService,
            TeamHealthService teamHealthService,
            CourseEnrollmentRepo courseEnrollmentRepo, UserRepo userRepo) {
        this.courseService = courseService;

        this.teamHealthService = teamHealthService;
        this.courseEnrollmentRepo = courseEnrollmentRepo;
        this.userRepo = userRepo;
    }

    @GetMapping("/prof/home")
    /**
     * Renders the professor home page with health metrics and active courses.
     */
    public String profHome(HttpSession session, Model model) {
        Object role = session.getAttribute("role");
        if (!"PROFESSOR".equals(role)) {
            return "redirect:/login";
        }

        Long professorId = (Long) session.getAttribute("userId");
        List<Course> professorCourses = courseService.getCoursesForProfessor(professorId);
        TeamHealthService.ProfessorHealthSummary healthSummary = teamHealthService
                .getProfessorWeeklySummary(professorId, LocalDate.now());
        TeamHealthService.ProfessorHealthTrend healthTrend = teamHealthService
                .getProfessorHealthTrend(professorId, LocalDate.now(), 6);
        LocalDate weekStart = teamHealthService.getWeekStart(LocalDate.now());
        List<AtRiskRow> atRiskRows = buildAtRiskRows(professorCourses, weekStart);
        List<ParticipationRow> participationRows = buildParticipationRows(professorCourses, weekStart);

        model.addAttribute("fullName", session.getAttribute("fullName"));
        model.addAttribute("form", new CreateCourseForm());
        model.addAttribute("courses", toCourseCards(professorCourses));
        model.addAttribute("healthSummary", healthSummary);
        model.addAttribute("healthTrend", healthTrend);
        model.addAttribute("weekStart", weekStart);
        model.addAttribute("atRiskRows", atRiskRows);
        model.addAttribute("participationRows", participationRows);
        return "professor_home";
    }

    @PostMapping("/prof/course/create")
    /**
     * Creates a new course owned by the logged-in professor.
     */
    public String createCourse(@ModelAttribute("form") CreateCourseForm form, HttpSession session, Model model) {
        Object role = session.getAttribute("role");
        if (!"PROFESSOR".equals(role)) {
            return "redirect:/login";
        }

        try {
            Long professorId = (Long) session.getAttribute("userId");
            Course course = courseService.createCourse(professorId, form.getCourseCode(), form.getCourseName());
            return "redirect:/prof/" + course.getId() + "/" + toSlug(course.getCourseName());
        } catch (IllegalArgumentException ex) {
            Long professorId = (Long) session.getAttribute("userId");
            model.addAttribute("fullName", session.getAttribute("fullName"));
            model.addAttribute("form", form);
            model.addAttribute("courses", toCourseCards(courseService.getCoursesForProfessor(professorId)));
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("openCreateCourseModal", true);
            return "professor_home";
        }
    }

    @GetMapping("/prof/{courseId}/{courseSlug}")
    /**
     * Renders one professor course page including enrolled student list.
     */
    public String coursePage(@PathVariable("courseId") String courseId,
            @PathVariable("courseSlug") String courseSlug,
            HttpSession session,
            Model model) {
        if (!isProfessor(session)) {
            return "redirect:/login";
        }

        Course course = courseService.getCourseById(courseId);
        if (course == null) {
            return "redirect:/prof/home";
        }

        Long professorId = (Long) session.getAttribute("userId");
        if (!course.getProfessorId().equals(professorId)) {
            return "redirect:/prof/home";
        }

        List<CourseEnrollment> enrollments = courseEnrollmentRepo.findByCourseId(course.getId());
        List<MemberView> enrolledStudents = enrollments.stream()
                .map(e -> userRepo.findById(e.getStudentId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(u -> new MemberView(u.getFullName(), u.getEmail()))
                .toList();

        model.addAttribute("fullName", session.getAttribute("fullName"));
        model.addAttribute("courseId", course.getId());
        model.addAttribute("courseJoinCode", course.getJoinCode());
        model.addAttribute("courseCode", course.getCourseCode());
        model.addAttribute("courseName", course.getCourseName());
        model.addAttribute("courseSlug", toSlug(course.getCourseName()));
        model.addAttribute("enrolledStudents", enrolledStudents);
        return "professor_course_page";
    }

    @GetMapping("/prof/{courseId}/{courseSlug}/surveys")
    /**
     * Shows weekly survey management for a specific course.
     */
    public String weeklySurveyPage(@PathVariable("courseId") String courseId,
            @PathVariable("courseSlug") String courseSlug,
            HttpSession session,
            Model model) {
        if (!isProfessor(session)) {
            return "redirect:/login";
        }

        Course course = courseService.getCourseById(courseId);
        if (course == null) {
            return "redirect:/prof/home";
        }

        Long professorId = (Long) session.getAttribute("userId");
        if (!course.getProfessorId().equals(professorId)) {
            return "redirect:/prof/home";
        }

        populateCourseSurveyModel(course, professorId, session, model);
        return "professor_surveys";
    }

    @PostMapping("/prof/{courseId}/{courseSlug}/surveys")
    /**
     * Publishes or updates the current week's survey for a course.
     */
    public String publishWeeklySurvey(@PathVariable("courseId") String courseId,
            @PathVariable("courseSlug") String courseSlug,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            HttpSession session,
            Model model) {
        if (!isProfessor(session)) {
            return "redirect:/login";
        }

        Course course = courseService.getCourseById(courseId);
        if (course == null) {
            return "redirect:/prof/home";
        }

        Long professorId = (Long) session.getAttribute("userId");
        if (!course.getProfessorId().equals(professorId)) {
            return "redirect:/prof/home";
        }

        try {
            teamHealthService.publishWeeklySurvey(professorId, courseId, title, description, LocalDate.now());
            model.addAttribute("success", "Weekly survey published for this course.");
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
        }

        populateCourseSurveyModel(course, professorId, session, model);
        return "professor_surveys";
    }

    private boolean isProfessor(HttpSession session) {
        return "PROFESSOR".equals(session.getAttribute("role"));
    }

    private void populateCourseSurveyModel(Course course, Long professorId, HttpSession session, Model model) {
        // Computes submission and missing-student data used by the survey management UI.
        LocalDate weekStart = teamHealthService.getWeekStart(LocalDate.now());
        WeeklySurvey survey = teamHealthService.getWeeklySurveyForCourseAndWeek(professorId, course.getId(), weekStart);

        List<CourseEnrollment> enrollments = courseEnrollmentRepo.findByCourseId(course.getId());
        int enrolledCount = enrollments.size();

        Set<Long> submittedStudentIds = new HashSet<>();
        for (TeamHealthCheckin checkin : teamHealthService.getCourseCheckinsForWeek(List.of(course.getId()), weekStart)) {
            submittedStudentIds.add(checkin.getStudentId());
        }

        int submissionsCount = submittedStudentIds.size();

        Map<Long, User> userCache = new HashMap<>();
        List<String> missingStudentNames = enrollments.stream()
                .filter(e -> !submittedStudentIds.contains(e.getStudentId()))
                .map(e -> {
                    User u = findUser(userCache, e.getStudentId());
                    return u == null ? "Unknown Student" : u.getFullName();
                })
                .sorted()
                .toList();

        model.addAttribute("fullName", session.getAttribute("fullName"));
        model.addAttribute("courseId", course.getId());
        model.addAttribute("courseCode", course.getCourseCode());
        model.addAttribute("courseName", course.getCourseName());
        model.addAttribute("courseSlug", toSlug(course.getCourseName()));
        model.addAttribute("weekStart", weekStart);
        model.addAttribute("weeklySurvey", survey);
        model.addAttribute("submissionsCount", submissionsCount);
        model.addAttribute("enrolledCount", enrolledCount);
        model.addAttribute("missingStudentNames", missingStudentNames);
    }

    private List<CourseCardView> toCourseCards(List<Course> courses) {
        return courses.stream()
                .map(course -> new CourseCardView(
                course.getId(),
                course.getCourseCode(),
                course.getCourseName(),
                toSlug(course.getCourseName())))
                .toList();
    }

    private String toSlug(String input) {
        if (input == null) {
            return "course";
        }
        String lower = input.trim().toLowerCase(Locale.ROOT);
        String normalized = NON_ALNUM.matcher(lower).replaceAll("-");
        String slug = normalized.replaceAll("^-+|-+$", "");
        return slug.isBlank() ? "course" : slug;
    }

    private List<AtRiskRow> buildAtRiskRows(List<Course> courses, LocalDate weekStart) {
        if (courses.isEmpty()) {
            return List.of();
        }

        Map<String, Course> courseById = new HashMap<>();
        List<String> courseIds = new ArrayList<>();
        for (Course course : courses) {
            courseById.put(course.getId(), course);
            courseIds.add(course.getId());
        }

        Map<Long, User> userCache = new HashMap<>();

        return teamHealthService.getCourseCheckinsForWeek(courseIds, weekStart).stream()
                .filter(this::isAtRisk)
                .sorted(Comparator
                        .comparingInt(this::riskSeverity)
                        .thenComparing(TeamHealthCheckin::getUpdatedAt).reversed())
                .limit(3)
                .map(checkin -> {
                    Course course = courseById.get(checkin.getCourseId());
                    User student = findUser(userCache, checkin.getStudentId());
                    String studentName = student == null ? "Unknown Student" : student.getFullName();
                    return new AtRiskRow(studentName, course == null ? "" : course.getCourseCode(), riskLabel(checkin));
                })
                .toList();
    }

    private List<ParticipationRow> buildParticipationRows(List<Course> courses, LocalDate weekStart) {
        if (courses.isEmpty()) {
            return List.of();
        }

        List<String> courseIds = courses.stream().map(Course::getId).toList();
        Map<String, Course> courseById = new HashMap<>();
        for (Course course : courses) {
            courseById.put(course.getId(), course);
        }

        Set<String> submitted = new HashSet<>();
        for (TeamHealthCheckin checkin : teamHealthService.getCourseCheckinsForWeek(courseIds, weekStart)) {
            submitted.add(participationKey(checkin.getCourseId(), checkin.getStudentId()));
        }

        Map<Long, User> userCache = new HashMap<>();

        return courseEnrollmentRepo.findByCourseIdIn(courseIds).stream()
                .filter(enrollment -> !submitted.contains(participationKey(enrollment.getCourseId(), enrollment.getStudentId())))
                .limit(3)
                .map(enrollment -> {
                    User student = findUser(userCache, enrollment.getStudentId());
                    Course course = courseById.get(enrollment.getCourseId());
                    return new ParticipationRow(
                            student == null ? "Unknown Student" : student.getFullName(),
                            course == null ? "Unknown Course" : course.getCourseCode(),
                            false);
                })
                .toList();
    }

    private User findUser(Map<Long, User> userCache, Long userId) {
        if (userId == null) {
            return null;
        }
        if (userCache.containsKey(userId)) {
            return userCache.get(userId);
        }

        User user = userRepo.findById(userId).orElse(null);
        userCache.put(userId, user);
        return user;
    }

    private boolean isAtRisk(TeamHealthCheckin checkin) {
        return checkin.getHealthScore() <= 2
                || checkin.getWorkloadScore() <= 2
                || checkin.getCollaborationScore() <= 2;
    }

    private int riskSeverity(TeamHealthCheckin checkin) {
        return Math.min(checkin.getHealthScore(), Math.min(checkin.getWorkloadScore(), checkin.getCollaborationScore()));
    }

    private String riskLabel(TeamHealthCheckin checkin) {
        if (checkin.getCollaborationScore() <= 2) {
            return "Collaboration: Low";
        }
        if (checkin.getHealthScore() <= 2) {
            return "Health: " + ((int) Math.round((checkin.getHealthScore() / 5.0) * 100.0)) + "%";
        }
        if (checkin.getWorkloadScore() <= 2) {
            return "Workload: High";
        }
        return "Needs Attention";
    }

    private String participationKey(String courseId, Long studentId) {
        return courseId + "::" + studentId;
    }

    public record MemberView(String fullName, String email) {

    }

    public record AtRiskRow(String studentName, String courseCode, String reason) {

    }

    public record ParticipationRow(String studentName, String courseCode, boolean submitted) {

    }

    public static class CourseCardView {

        private final String id;
        private final String courseCode;
        private final String courseName;
        private final String courseSlug;

        public CourseCardView(String id, String courseCode, String courseName, String courseSlug) {
            this.id = id;
            this.courseCode = courseCode;
            this.courseName = courseName;
            this.courseSlug = courseSlug;
        }

        public String getId() {
            return id;
        }

        public String getCourseCode() {
            return courseCode;
        }

        public String getCourseName() {
            return courseName;
        }

        public String getCourseSlug() {
            return courseSlug;
        }
    }
}
