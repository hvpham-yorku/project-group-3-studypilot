package com.studypilot.studypilot.GUILayer;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.studypilot.studypilot.BusinessLogicLayer.CourseService;
import com.studypilot.studypilot.DomainModel.Course;

import jakarta.servlet.http.HttpSession;

@Controller
public class ProfessorHomeController {

    private static final Pattern NON_ALNUM = Pattern.compile("[^a-z0-9]+");

    private final CourseService courseService;

    public ProfessorHomeController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping("/prof/home")
    public String profHome(HttpSession session, Model model) {
        Object role = session.getAttribute("role");
        if (!"PROFESSOR".equals(role)) {
            return "redirect:/login";
        }

        Long professorId = (Long) session.getAttribute("userId");

        model.addAttribute("fullName", session.getAttribute("fullName"));
        model.addAttribute("form", new CreateCourseForm());
        model.addAttribute("courses", toCourseCards(courseService.getCoursesForProfessor(professorId)));
        return "professor_home";
    }

    @PostMapping("/prof/course/create")
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
    public String coursePage(@PathVariable("courseId") String courseId,
            @PathVariable("courseSlug") String courseSlug, HttpSession session,
            Model model) {
        Object role = session.getAttribute("role");
        if (!"PROFESSOR".equals(role)) {
            return "redirect:/login";
        }

        Course course = courseService.getCourseById(courseId);
        if (course == null) {
            return "redirect:/prof/home";
        }

        model.addAttribute("fullName", session.getAttribute("fullName"));
        model.addAttribute("courseId", courseId);
        model.addAttribute("courseName", course.getCourseName());
        return "professor_course_page";
    }

    private List<CourseCardView> toCourseCards(List<Course> courses) {
        return courses.stream()
                .map(course -> new CourseCardView(course.getId(), course.getCourseCode(), course.getCourseName(),
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
