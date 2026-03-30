package com.studypilot.studypilot.GUILayer;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.studypilot.studypilot.BusinessLogicLayer.StudentPortalService;
import com.studypilot.studypilot.BusinessLogicLayer.TeamHealthService;
import com.studypilot.studypilot.DataAccessLayer.UserRepo;
import com.studypilot.studypilot.DomainModel.Course;
import com.studypilot.studypilot.DomainModel.GroupFormationActivity;
import com.studypilot.studypilot.DomainModel.SurveyQuestion;
import com.studypilot.studypilot.DomainModel.SurveyQuestionOption;
import com.studypilot.studypilot.DomainModel.SurveyResponse;
import com.studypilot.studypilot.DomainModel.TeamHealthCheckin;
import com.studypilot.studypilot.DomainModel.User;
import com.studypilot.studypilot.DomainModel.WeeklySurvey;

import jakarta.servlet.http.HttpSession;

@Controller
public class StudentHomeController {

    private static final Pattern NON_ALNUM = Pattern.compile("[^a-z0-9]+");

    private final StudentPortalService studentPortalService;
    private final TeamHealthService teamHealthService;
    private final UserRepo userRepo;

    public StudentHomeController(StudentPortalService studentPortalService,
            TeamHealthService teamHealthService,
            UserRepo userRepo) {
        this.studentPortalService = studentPortalService;
        this.teamHealthService = teamHealthService;
        this.userRepo = userRepo;
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

    @GetMapping("/student/courses")
    public String studentCourses(HttpSession session, Model model) {
        if (!isStudent(session)) {
            return "redirect:/login";
        }

        Long studentId = (Long) session.getAttribute("userId");
        model.addAttribute("fullName", session.getAttribute("fullName"));
        model.addAttribute("courses", toStudentCourseCards(studentPortalService.getStudentCourses(studentId)));
        return "student_my_courses";
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
        Map<String, WeeklySurvey> surveysByCourseId = teamHealthService.getWeeklySurveysByCourseIdForWeek(courseIds, weekStart);
        List<StudentCourseCardView> activeCourses = studentCourses.stream()
                .filter(course -> surveysByCourseId.containsKey(course.getId()))
                .map(course -> new StudentCourseCardView(
                course.getId(),
                course.getCourseCode(),
                course.getCourseName(),
                toSlug(course.getCourseName())))
                .toList();

        Map<String, TeamHealthCheckin> checkinsByCourseId = new HashMap<>();
        List<String> activeCourseIds = activeCourses.stream().map(StudentCourseCardView::id).toList();

        for (TeamHealthCheckin checkin : teamHealthService.getStudentCourseCheckinsForWeek(studentId, activeCourseIds, weekStart)) {
            checkinsByCourseId.put(checkin.getCourseId(), checkin);
        }

        model.addAttribute("fullName", session.getAttribute("fullName"));
        model.addAttribute("weekStart", weekStart);
        model.addAttribute("courses", activeCourses);
        model.addAttribute("healthStatus", teamHealthService.getStudentWeeklyStatus(studentId, weekStart));
        model.addAttribute("checkinsByCourseId", checkinsByCourseId);
        model.addAttribute("surveysByCourseId", surveysByCourseId);
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
            LocalDate weekStart = teamHealthService.getWeekStart(LocalDate.now());
            WeeklySurvey weeklySurvey = teamHealthService
                    .getWeeklySurveysByCourseIdForWeek(List.of(courseId), weekStart)
                    .get(courseId);
            TeamHealthCheckin existingCheckin = teamHealthService
                    .getStudentCourseCheckinsForWeek(studentId, List.of(courseId), weekStart)
                    .stream()
                    .findFirst()
                    .orElse(null);

            StudentPortalService.StudentTeamSnapshot teamSnapshot = studentPortalService.getStudentTeamForCourse(studentId,
                    courseId);
            List<TeamMemberView> teamMembers = new ArrayList<>();
            if (teamSnapshot != null) {
                for (Long memberId : teamSnapshot.memberIds()) {
                    User member = userRepo.findById(memberId).orElse(null);
                    if (member != null) {
                        teamMembers.add(new TeamMemberView(member.getFullName(), member.getEmail()));
                    }
                }
            }

            model.addAttribute("fullName", session.getAttribute("fullName"));
            model.addAttribute("course", course);
            model.addAttribute("courseSlug", toSlug(course.getCourseName()));

            model.addAttribute("hasGroupActivity", studentPortalService.getLatestGroupActivityForCourse(courseId).isPresent());
            model.addAttribute("weekStart", weekStart);
            model.addAttribute("weeklySurvey", weeklySurvey);
            model.addAttribute("courseCheckin", existingCheckin);
            model.addAttribute("teamSnapshot", teamSnapshot);
            model.addAttribute("teamMembers", teamMembers);
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
            model.addAttribute("activityStatus", activity.getStatus());

            if ("SORTED".equals(activity.getStatus())) {
                studentPortalService.getStudentGroupForCourse(studentId, courseId).ifPresent(groupInfo -> {
                    model.addAttribute("studentGroup", groupInfo);
                });
            }

            // Load survey questions and options
            List<SurveyQuestion> questions = studentPortalService.getSurveyQuestions(activity.getId());
            model.addAttribute("surveyQuestions", questions);

            List<Long> questionIds = questions.stream().map(SurveyQuestion::getId).toList();
            Map<Long, List<SurveyQuestionOption>> optionsByQuestion =
                    studentPortalService.getOptionsGroupedByQuestion(questionIds);
            model.addAttribute("optionsByQuestion", optionsByQuestion);

            // Load existing responses
            List<SurveyResponse> existingResponses = studentPortalService.getStudentResponses(activity.getId(), studentId);
            Map<Long, String> savedResponses = existingResponses.stream()
                    .collect(Collectors.toMap(SurveyResponse::getQuestionId, SurveyResponse::getResponseValue));
            model.addAttribute("savedResponses", savedResponses);

            // Load preference for availability/notes
            StudentGroupPreferenceForm form = new StudentGroupPreferenceForm();
            studentPortalService.getStudentPreference(activity.getId(), studentId).ifPresent(pref -> {
                form.setNotes(pref.getNotes());
                form.setAvailabilitySlots(pref.getAvailabilitySlots());
                model.addAttribute("savedPreference", true);
                model.addAttribute("savedAvailability", pref.getAvailabilitySlots() != null ? pref.getAvailabilitySlots() : "");
            });

            // Check if student has any survey responses (counts as having responded)
            if (!existingResponses.isEmpty()) {
                model.addAttribute("savedPreference", true);
            }

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
            studentPortalService.saveSurveyResponses(
                    studentId,
                    courseId,
                    activityId,
                    form.getResponses(),
                    form.getNotes(),
                    form.getAvailabilitySlots()
            );
            model.addAttribute("success", "Your survey responses have been saved.");
            return groupFormationPage(courseId, courseSlug, session, model);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return groupFormationPage(courseId, courseSlug, session, model);
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

    public record TeamMemberView(String fullName, String email) {

    }
}
