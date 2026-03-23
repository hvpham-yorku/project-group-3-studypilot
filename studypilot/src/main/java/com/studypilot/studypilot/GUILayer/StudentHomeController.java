package com.studypilot.studypilot.GUILayer;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.studypilot.studypilot.BusinessLogicLayer.QuizService;
import com.studypilot.studypilot.BusinessLogicLayer.StudentPortalService;
import com.studypilot.studypilot.BusinessLogicLayer.TeamHealthService;
import com.studypilot.studypilot.DomainModel.Course;
import com.studypilot.studypilot.DomainModel.GroupFormationActivity;
import com.studypilot.studypilot.DomainModel.QuizTest;
import com.studypilot.studypilot.DomainModel.TeamHealthCheckin;

import jakarta.servlet.http.HttpSession;

@Controller
public class StudentHomeController {

    private static final Pattern NON_ALNUM = Pattern.compile("[^a-z0-9]+");

    private final StudentPortalService studentPortalService;
    private final QuizService quizService;
    private final TeamHealthService teamHealthService;

    public StudentHomeController(StudentPortalService studentPortalService,
            QuizService quizService,
            TeamHealthService teamHealthService) {
        this.studentPortalService = studentPortalService;
        this.quizService = quizService;
        this.teamHealthService = teamHealthService;
    }

    @GetMapping("/student/home")
    public String studentHome(HttpSession session, Model model) {
        if (!isStudent(session)) {
            return "redirect:/login";
        }

        Long studentId = (Long) session.getAttribute("userId");
        model.addAttribute("fullName", session.getAttribute("fullName"));
        model.addAttribute("joinForm", new StudentJoinCourseForm());

        List<Course> enrolledCourses = studentPortalService.getStudentCourses(studentId);
        model.addAttribute("courses", toStudentCourseCards(enrolledCourses));

        List<Course> availableCourses = studentPortalService.getCoursesAvailableToJoin(studentId);
        model.addAttribute("availableCourses", availableCourses);

        return "student_home";
    }

    @GetMapping("/student/surveys")
    public String studentSurveys(HttpSession session, Model model) {
        if (!isStudent(session)) {
            return "redirect:/login";
        }

        Long studentId = (Long) session.getAttribute("userId");
        LocalDate weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        List<Course> studentCourses = studentPortalService.getStudentCourses(studentId);
        List<String> courseIds = studentCourses.stream().map(Course::getId).toList();
        Map<String, TeamHealthCheckin> checkinsByCourseId = new HashMap<>();

        for (TeamHealthCheckin checkin : teamHealthService.getStudentCourseCheckinsForWeek(studentId, courseIds, weekStart)) {
            checkinsByCourseId.put(checkin.getCourseId(), checkin);
        }

        model.addAttribute("fullName", session.getAttribute("fullName"));
        model.addAttribute("weekStart", weekStart);
        model.addAttribute("courses", toStudentCourseCards(studentCourses));
        model.addAttribute("healthStatus", teamHealthService.getStudentWeeklyStatus(studentId, weekStart));
        model.addAttribute("checkinsByCourseId", checkinsByCourseId);
        return "student_surveys";
    }

    @PostMapping("/student/surveys/checkin")
    public String submitHealthSurvey(@RequestParam("courseId") String courseId,
            @RequestParam("healthScore") Integer healthScore,
            @RequestParam("workloadScore") Integer workloadScore,
            @RequestParam("collaborationScore") Integer collaborationScore,
            @RequestParam(name = "statusText", required = false) String statusText,
            HttpSession session,
            Model model) {
        if (!isStudent(session)) {
            return "redirect:/login";
        }

        Long studentId = (Long) session.getAttribute("userId");
        try {
            teamHealthService.saveWeeklyCheckin(
                    studentId,
                    courseId,
                    healthScore,
                    workloadScore,
                    collaborationScore,
                    statusText,
                    LocalDate.now());
            model.addAttribute("success", "Weekly health survey saved.");
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
        }

        return studentSurveys(session, model);
    }

    @PostMapping("/student/course/join")
    public String joinCourse(@ModelAttribute("joinForm") StudentJoinCourseForm form,
            HttpSession session,
            Model model) {
        if (!isStudent(session)) {
            return "redirect:/login";
        }

        Long studentId = (Long) session.getAttribute("userId");
        try {
            studentPortalService.enrollStudentInCourseByJoinCode(studentId, form.getJoinCode());
            return "redirect:/student/home";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return studentHome(session, model);
        }
    }

    @GetMapping("/student/{courseId}/{courseSlug}")
    public String studentCoursePage(@PathVariable("courseId") String courseId,
            @PathVariable("courseSlug") String courseSlug,
            HttpSession session,
            Model model) {
        if (!isStudent(session)) {
            return "redirect:/login";
        }

        Long studentId = (Long) session.getAttribute("userId");
        try {
            Course course = studentPortalService.requireStudentEnrollment(studentId, courseId);
            model.addAttribute("fullName", session.getAttribute("fullName"));
            model.addAttribute("course", course);
            model.addAttribute("courseSlug", toSlug(course.getCourseName()));
            model.addAttribute("hasQuiz", quizService.getLatestQuizForCourse(courseId).isPresent());
            model.addAttribute("hasGroupActivity", studentPortalService.getLatestGroupActivityForCourse(courseId).isPresent());
            return "student_course_page";
        } catch (IllegalArgumentException ex) {
            return "redirect:/student/home";
        }
    }

    @GetMapping("/student/{courseId}/{courseSlug}/group-formation")
    public String groupFormationPage(@PathVariable("courseId") String courseId,
            @PathVariable("courseSlug") String courseSlug,
            HttpSession session,
            Model model) {
        if (!isStudent(session)) {
            return "redirect:/login";
        }

        Long studentId = (Long) session.getAttribute("userId");

        try {
            Course course = studentPortalService.requireStudentEnrollment(studentId, courseId);
            Optional<GroupFormationActivity> maybeActivity = studentPortalService.getLatestGroupActivityForCourse(courseId);

            model.addAttribute("fullName", session.getAttribute("fullName"));
            model.addAttribute("course", course);
            model.addAttribute("courseSlug", toSlug(course.getCourseName()));

            if (maybeActivity.isEmpty()) {
                model.addAttribute("activity", null);
                return "student_group_formation_page";
            }

            GroupFormationActivity activity = maybeActivity.get();
            model.addAttribute("activity", activity);
            model.addAttribute("topicOptions", studentPortalService.getTopicOptions(activity.getId()));
            model.addAttribute("skillOptions", studentPortalService.getSkillOptions(activity.getId()));

            StudentGroupPreferenceForm form = new StudentGroupPreferenceForm();
            studentPortalService.getStudentPreference(activity.getId(), studentId).ifPresent(pref -> {
                form.setTopicChoice(pref.getTopicChoice());
                form.setSkillChoice(pref.getSkillChoice());
                form.setNotes(pref.getNotes());
                model.addAttribute("savedPreference", true);
            });

            model.addAttribute("form", form);
            return "student_group_formation_page";
        } catch (IllegalArgumentException ex) {
            return "redirect:/student/home";
        }
    }

    @PostMapping("/student/{courseId}/{courseSlug}/group-formation")
    public String submitGroupFormation(@PathVariable("courseId") String courseId,
            @PathVariable("courseSlug") String courseSlug,
            @RequestParam("activityId") Long activityId,
            @ModelAttribute("form") StudentGroupPreferenceForm form,
            HttpSession session,
            Model model) {
        if (!isStudent(session)) {
            return "redirect:/login";
        }

        Long studentId = (Long) session.getAttribute("userId");

        try {
            studentPortalService.saveGroupPreference(
                    studentId,
                    courseId,
                    activityId,
                    form.getTopicChoice(),
                    form.getSkillChoice(),
                    form.getNotes()
            );
            model.addAttribute("success", "Your group preferences have been saved.");
            return groupFormationPage(courseId, courseSlug, session, model);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return groupFormationPage(courseId, courseSlug, session, model);
        }
    }

    @GetMapping("/student/{courseId}/{courseSlug}/quiz")
    public String takeQuizPage(@PathVariable("courseId") String courseId,
            @PathVariable("courseSlug") String courseSlug,
            HttpSession session,
            Model model) {
        if (!isStudent(session)) {
            return "redirect:/login";
        }

        Long studentId = (Long) session.getAttribute("userId");
        try {
            Course course = studentPortalService.requireStudentEnrollment(studentId, courseId);
            model.addAttribute("fullName", session.getAttribute("fullName"));
            model.addAttribute("course", course);
            model.addAttribute("courseSlug", toSlug(course.getCourseName()));

            Optional<QuizTest> maybeQuiz = quizService.getLatestQuizForCourse(courseId);
            if (maybeQuiz.isEmpty()) {
                model.addAttribute("quiz", null);
                return "student_quiz_page";
            }

            QuizTest quiz = maybeQuiz.get();
            model.addAttribute("quiz", quiz);
            model.addAttribute("questions", quizService.getQuestionsForQuiz(quiz.getId()));
            quizService.getLatestSubmission(quiz.getId(), studentId).ifPresent(previous -> {
                model.addAttribute("latestSubmission", previous);
            });
            return "student_quiz_page";
        } catch (IllegalArgumentException ex) {
            return "redirect:/student/home";
        }
    }

    @PostMapping("/student/{courseId}/{courseSlug}/quiz/submit")
    public String submitQuiz(@PathVariable("courseId") String courseId,
            @PathVariable("courseSlug") String courseSlug,
            @RequestParam("quizId") Long quizId,
            @RequestParam Map<String, String> payload,
            HttpSession session,
            Model model) {
        if (!isStudent(session)) {
            return "redirect:/login";
        }

        Long studentId = (Long) session.getAttribute("userId");

        try {
            Course course = studentPortalService.requireStudentEnrollment(studentId, courseId);

            Map<Long, String> selectedByQuestionId = new HashMap<>();
            for (Map.Entry<String, String> entry : payload.entrySet()) {
                String key = entry.getKey();
                if (!key.startsWith("q_")) {
                    continue;
                }
                String idPart = key.substring(2);
                try {
                    Long questionId = Long.parseLong(idPart);
                    selectedByQuestionId.put(questionId, entry.getValue());
                } catch (NumberFormatException ignored) {
                    // Skip malformed question keys.
                }
            }

            QuizService.QuizResult result = quizService.submitQuiz(quizId, courseId, studentId, selectedByQuestionId);

            model.addAttribute("fullName", session.getAttribute("fullName"));
            model.addAttribute("course", course);
            model.addAttribute("courseSlug", toSlug(course.getCourseName()));
            model.addAttribute("quiz", quizService.getLatestQuizForCourse(courseId).orElse(null));
            model.addAttribute("submission", result.submission());
            model.addAttribute("reviews", quizService.buildAnswerReview(result.submission()));
            return "student_quiz_result_page";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return takeQuizPage(courseId, courseSlug, session, model);
        }
    }

    private boolean isStudent(HttpSession session) {
        return "STUDENT".equals(session.getAttribute("role"));
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

    private List<StudentCourseCardView> toStudentCourseCards(List<Course> courses) {
        return courses.stream()
                .map(course -> new StudentCourseCardView(
                course.getId(),
                course.getCourseCode(),
                course.getCourseName(),
                toSlug(course.getCourseName())))
                .toList();
    }

    public record StudentCourseCardView(String id, String courseCode, String courseName, String courseSlug) {

    }
}
