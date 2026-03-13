package com.studypilot.studypilot.GUILayer;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.studypilot.studypilot.BusinessLogicLayer.CourseService;
import com.studypilot.studypilot.DataAccessLayer.CourseEnrollmentRepo;
import com.studypilot.studypilot.DomainModel.Course;
import com.studypilot.studypilot.DomainModel.CourseEnrollment;

import jakarta.servlet.http.HttpSession;

@Controller
public class SidebarPagesController {

    private static final Pattern NON_ALNUM = Pattern.compile("[^a-z0-9]+");
    private final CourseService courseService;
    private final CourseEnrollmentRepo courseEnrollmentRepo;

    public SidebarPagesController(CourseService courseService, CourseEnrollmentRepo courseEnrollmentRepo) {
        this.courseService = courseService;
        this.courseEnrollmentRepo = courseEnrollmentRepo;
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
        model.addAttribute("weeklyTrends", List.of(
                new TrendDemo("Mon", 72),
                new TrendDemo("Tue", 84),
                new TrendDemo("Wed", 91),
                new TrendDemo("Thu", 77),
                new TrendDemo("Fri", 88)));
        model.addAttribute("insights", List.of(
                "AI response coverage increased by 14% this week.",
                "Most frequent topic: midterm preparation.",
                "Average response satisfaction score: 4.6/5."));
        return "analytics";
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
