package com.studypilot.studypilot.GUILayer;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.studypilot.studypilot.BusinessLogicLayer.CourseService;
import com.studypilot.studypilot.BusinessLogicLayer.TeamHealthService;
import com.studypilot.studypilot.DataAccessLayer.CourseEnrollmentRepo;
import com.studypilot.studypilot.DomainModel.Course;
import com.studypilot.studypilot.DomainModel.CourseEnrollment;
import com.studypilot.studypilot.DomainModel.TeamHealthCheckin;

import jakarta.servlet.http.HttpSession;

@Controller
public class SidebarPagesController {

    private static final Pattern NON_ALNUM = Pattern.compile("[^a-z0-9]+");
    private static final DateTimeFormatter WEEK_LABEL = DateTimeFormatter.ofPattern("MMM d");
    private final CourseService courseService;
    private final CourseEnrollmentRepo courseEnrollmentRepo;
    private final TeamHealthService teamHealthService;

    public SidebarPagesController(CourseService courseService,
            CourseEnrollmentRepo courseEnrollmentRepo,
            TeamHealthService teamHealthService) {
        this.courseService = courseService;
        this.courseEnrollmentRepo = courseEnrollmentRepo;
        this.teamHealthService = teamHealthService;
    }

    @GetMapping("/courses")
    public String courses(HttpSession session, Model model) {
        if (!isAuthenticated(session)) {
            return "redirect:/login";
        }

        Object role = session.getAttribute("role");
        if (!"PROFESSOR".equals(role)) {
            return "redirect:/student/home";
        }

        Long professorId = (Long) session.getAttribute("userId");
        List<CourseDemo> courseCards = toCourseDemoCards(courseService.getCoursesForProfessor(professorId));

        model.addAttribute("fullName", session.getAttribute("fullName"));
        model.addAttribute("courses", courseCards);
        model.addAttribute("totalCourses", courseCards.size());
        model.addAttribute("totalEnrolledStudents", courseCards.stream().mapToInt(CourseDemo::students).sum());
        model.addAttribute("coursesRequiringReview", courseCards.stream()
                .filter(course -> "Needs Review".equals(course.status()))
                .count());
        return "my_courses";
    }

    @GetMapping("/faq-library")
    public String faqLibrary(HttpSession session) {
        if (!isAuthenticated(session)) {
            return "redirect:/login";
        }
        // FAQ page removed; redirect students to the new weekly surveys page
        Object role = session.getAttribute("role");
        if ("STUDENT".equals(role)) {
            return "redirect:/student/surveys";
        }
        return "redirect:/courses";
    }

    @GetMapping("/analytics")
    public String analytics(HttpSession session, Model model) {
        if (!isAuthenticated(session)) {
            return "redirect:/login";
        }

        model.addAttribute("fullName", session.getAttribute("fullName"));
        Object role = session.getAttribute("role");
        if ("PROFESSOR".equals(role)) {
            populateProfessorAnalytics(session, model);
        } else if ("STUDENT".equals(role)) {
            populateStudentAnalytics(session, model);
        } else {
            return "redirect:/login";
        }
        return "analytics";
    }

    private void populateProfessorAnalytics(HttpSession session, Model model) {
        Long professorId = (Long) session.getAttribute("userId");
        LocalDate weekStart = teamHealthService.getWeekStart(LocalDate.now());

        TeamHealthService.ProfessorHealthSummary summary = teamHealthService.getProfessorWeeklySummary(professorId, weekStart);
        TeamHealthService.ProfessorHealthTrend trend = teamHealthService.getProfessorHealthTrend(professorId, weekStart, 5);

        model.addAttribute("metric1Label", "Weekly Survey Submissions");
        model.addAttribute("metric1Value", summary.totalSubmissions());
        model.addAttribute("metric2Label", "Average Team Health");
        model.addAttribute("metric2Value", summary.avgHealthPercent() + "%");
        model.addAttribute("metric3Label", "At-Risk Responses");
        model.addAttribute("metric3Value", summary.atRiskResponses());

        List<TrendDemo> weeklyTrends = toTrendBars(trend.points());
        boolean hasTrendData = hasTrendData(weeklyTrends);
        model.addAttribute("weeklyTrends", weeklyTrends);
        model.addAttribute("hasTrendData", hasTrendData);

        if (hasTrendData) {
            model.addAttribute("insights", List.of(
                    trend.summaryText(),
                    "Missing surveys this week: " + summary.missingSurveys(),
                    "Data shown for week of " + weekStart.format(WEEK_LABEL) + "."));
            model.addAttribute("insightEmptyMessage", "");
        } else {
            model.addAttribute("insights", List.of());
            model.addAttribute("insightEmptyMessage", "No insights yet. Ask students to submit weekly surveys to unlock trend and risk insights.");
        }
    }

    private void populateStudentAnalytics(HttpSession session, Model model) {
        Long studentId = (Long) session.getAttribute("userId");
        LocalDate weekStart = teamHealthService.getWeekStart(LocalDate.now());
        TeamHealthService.StudentHealthStatus status = teamHealthService.getStudentWeeklyStatus(studentId, weekStart);

        List<String> courseIds = courseEnrollmentRepo.findByStudentIdOrderByCreatedAtDesc(studentId).stream()
                .map(CourseEnrollment::getCourseId)
                .toList();

        List<TeamHealthCheckin> currentWeekRows = teamHealthService
                .getStudentCourseCheckinsForWeek(studentId, courseIds, weekStart);
        int avgHealthPercent = averageHealthPercent(currentWeekRows);

        model.addAttribute("metric1Label", "Enrolled Courses");
        model.addAttribute("metric1Value", status.enrolledCourses());
        model.addAttribute("metric2Label", "Surveys Submitted");
        model.addAttribute("metric2Value", status.submittedSurveys());
        model.addAttribute("metric3Label", "Current Health Avg");
        model.addAttribute("metric3Value", avgHealthPercent + "%");

        List<TrendDemo> weeklyTrends = buildStudentTrend(studentId, courseIds, weekStart, 5);
        boolean hasTrendData = hasTrendData(weeklyTrends);
        model.addAttribute("weeklyTrends", weeklyTrends);
        model.addAttribute("hasTrendData", hasTrendData);

        if (hasTrendData) {
            model.addAttribute("insights", List.of(
                    "You have " + status.missingSurveys() + " pending survey(s) this week.",
                    "Your current average health score is " + avgHealthPercent + "%.",
                    "Data shown for week of " + weekStart.format(WEEK_LABEL) + "."));
            model.addAttribute("insightEmptyMessage", "");
        } else {
            model.addAttribute("insights", List.of());
            model.addAttribute("insightEmptyMessage", "No insights yet. Complete your weekly surveys to generate personalized analytics.");
        }
    }

    private List<TrendDemo> toTrendBars(List<TeamHealthService.WeeklyHealthPoint> points) {
        return points.stream()
                .map(point -> new TrendDemo(point.weekStart().format(WEEK_LABEL), point.avgHealthPercent()))
                .toList();
    }

    private List<TrendDemo> buildStudentTrend(Long studentId, List<String> courseIds, LocalDate endWeek, int weeks) {
        List<TrendDemo> trend = new ArrayList<>();
        int normalizedWeeks = weeks <= 0 ? 5 : weeks;
        LocalDate startWeek = endWeek.minusWeeks(normalizedWeeks - 1L);

        for (int i = 0; i < normalizedWeeks; i++) {
            LocalDate week = startWeek.plusWeeks(i);
            int avgHealthPercent = averageHealthPercent(
                    teamHealthService.getStudentCourseCheckinsForWeek(studentId, courseIds, week));
            trend.add(new TrendDemo(week.format(WEEK_LABEL), avgHealthPercent));
        }

        return trend;
    }

    private int averageHealthPercent(List<TeamHealthCheckin> checkins) {
        if (checkins == null || checkins.isEmpty()) {
            return 0;
        }

        double averageScore = checkins.stream().mapToInt(TeamHealthCheckin::getHealthScore).average().orElse(0.0);
        return (int) Math.round((averageScore / 5.0) * 100.0);
    }

    private boolean hasTrendData(List<TrendDemo> weeklyTrends) {
        return weeklyTrends != null && weeklyTrends.stream().anyMatch(trend -> trend.value() > 0);
    }

    private boolean isAuthenticated(HttpSession session) {
        return session.getAttribute("role") != null;
    }

    private List<CourseDemo> toCourseDemoCards(List<Course> courses) {
        return courses.stream()
                .map(course -> {
                    int studentCount = getStudentCount(course.getId());
                    String status = studentCount == 0 ? "Needs Review" : "Active";
                    return new CourseDemo(
                            course.getCourseCode(),
                            course.getCourseName(),
                            "See course space",
                            studentCount,
                            status,
                            "/prof/" + course.getId() + "/" + toSlug(course.getCourseName()));
                })
                .toList();
    }

    private int getStudentCount(String courseId) {
        List<CourseEnrollment> enrollments = courseEnrollmentRepo.findByCourseId(courseId);
        return enrollments.size();
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

    public record CourseDemo(String code, String name, String schedule, int students, String status, String href) {

    }

    public record TrendDemo(String day, int value) {

    }
}
