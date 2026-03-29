package com.studypilot.studypilot.BusinessLogicLayer;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.studypilot.studypilot.DataAccessLayer.CourseEnrollmentRepo;
import com.studypilot.studypilot.DataAccessLayer.CourseRepo;
import com.studypilot.studypilot.DataAccessLayer.TeamHealthCheckinRepo;
import com.studypilot.studypilot.DataAccessLayer.WeeklySurveyRepo;
import com.studypilot.studypilot.DomainModel.Course;
import com.studypilot.studypilot.DomainModel.CourseEnrollment;
import com.studypilot.studypilot.DomainModel.TeamHealthCheckin;
import com.studypilot.studypilot.DomainModel.WeeklySurvey;

@Service
public class TeamHealthService {

    private final CourseRepo courseRepo;
    private final CourseEnrollmentRepo courseEnrollmentRepo;
    private final TeamHealthCheckinRepo teamHealthCheckinRepo;
    private final WeeklySurveyRepo weeklySurveyRepo;

    public TeamHealthService(CourseRepo courseRepo,
            CourseEnrollmentRepo courseEnrollmentRepo,
            TeamHealthCheckinRepo teamHealthCheckinRepo,
            WeeklySurveyRepo weeklySurveyRepo) {
        this.courseRepo = courseRepo;
        this.courseEnrollmentRepo = courseEnrollmentRepo;
        this.teamHealthCheckinRepo = teamHealthCheckinRepo;
        this.weeklySurveyRepo = weeklySurveyRepo;
    }

    @Transactional
    public TeamHealthCheckin saveWeeklyCheckin(Long studentId,
            String courseId,
            Integer healthScore,
            Integer workloadScore,
            Integer collaborationScore,
            String statusText,
            LocalDate weekDate) {
        requireStudent(studentId);
        String cleanCourseId = clean(courseId);
        Course course = courseRepo.findById(cleanCourseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found."));

        if (!courseEnrollmentRepo.existsByCourseIdAndStudentId(course.getId(), studentId)) {
            throw new IllegalArgumentException("You are not enrolled in this course.");
        }

        int normalizedHealth = validateScore(healthScore, "Health score must be between 1 and 5.");
        int normalizedWorkload = validateScore(workloadScore, "Workload score must be between 1 and 5.");
        int normalizedCollaboration = validateScore(collaborationScore, "Collaboration score must be between 1 and 5.");

        LocalDate weekStart = normalizeWeekStart(weekDate);
        if (!weeklySurveyRepo.existsByCourseIdAndWeekStart(course.getId(), weekStart)) {
            throw new IllegalArgumentException("No weekly survey has been published for this course yet.");
        }
        String normalizedStatus = normalizeStatus(statusText);

        Optional<TeamHealthCheckin> existing = teamHealthCheckinRepo
                .findByCourseIdAndStudentIdAndWeekStart(course.getId(), studentId, weekStart);

        if (existing.isPresent()) {
            TeamHealthCheckin checkin = existing.get();
            checkin.setHealthScore(normalizedHealth);
            checkin.setWorkloadScore(normalizedWorkload);
            checkin.setCollaborationScore(normalizedCollaboration);
            checkin.setStatusText(normalizedStatus);
            return teamHealthCheckinRepo.save(checkin);
        }

        TeamHealthCheckin checkin = new TeamHealthCheckin(
                course.getId(),
                studentId,
                weekStart,
                normalizedHealth,
                normalizedWorkload,
                normalizedCollaboration,
                normalizedStatus
        );
        return teamHealthCheckinRepo.save(checkin);
    }

    public ProfessorHealthSummary getProfessorWeeklySummary(Long professorId, LocalDate weekDate) {
        if (professorId == null) {
            throw new IllegalArgumentException("Professor must be logged in.");
        }

        LocalDate weekStart = normalizeWeekStart(weekDate);
        List<Course> courses = courseRepo.findByProfessorIdOrderByCreatedAtDesc(professorId);

        if (courses.isEmpty()) {
            return new ProfessorHealthSummary(weekStart, 0, 0, 0, 0);
        }

        List<String> courseIds = courses.stream()
                .map(Course::getId)
                .toList();

        List<CourseEnrollment> enrollments = courseEnrollmentRepo.findByCourseIdIn(courseIds);
        Set<String> coursesWithSurvey = getCourseIdsWithSurveyForWeek(courseIds, weekStart);
        List<String> scorableCourseIds = enrollments.stream()
                .map(CourseEnrollment::getCourseId)
                .filter(coursesWithSurvey::contains)
                .distinct()
                .toList();

        if (scorableCourseIds.isEmpty()) {
            return new ProfessorHealthSummary(weekStart, 0, 0, 0, 0);
        }

        List<TeamHealthCheckin> weekCheckins = teamHealthCheckinRepo.findByCourseIdInAndWeekStart(scorableCourseIds, weekStart);
        int enrollmentCount = (int) enrollments.stream()
                .filter(enrollment -> coursesWithSurvey.contains(enrollment.getCourseId()))
                .count();

        int totalSubmissions = weekCheckins.size();
        int avgHealthPercent = averageHealthPercent(weekCheckins);
        int atRiskResponses = (int) weekCheckins.stream().filter(this::isAtRisk).count();
        int missingSurveys = Math.max(enrollmentCount - totalSubmissions, 0);

        return new ProfessorHealthSummary(weekStart, totalSubmissions, avgHealthPercent, atRiskResponses, missingSurveys);
    }

    public ProfessorHealthTrend getProfessorHealthTrend(Long professorId, LocalDate weekDate, int weeks) {
        if (professorId == null) {
            throw new IllegalArgumentException("Professor must be logged in.");
        }

        int normalizedWeeks = weeks <= 0 ? 6 : weeks;
        LocalDate endWeek = normalizeWeekStart(weekDate);
        LocalDate startWeek = endWeek.minusWeeks(normalizedWeeks - 1L);

        List<Course> courses = courseRepo.findByProfessorIdOrderByCreatedAtDesc(professorId);
        List<WeeklyHealthPoint> points = new ArrayList<>();

        if (courses.isEmpty()) {
            for (int i = 0; i < normalizedWeeks; i++) {
                points.add(new WeeklyHealthPoint(startWeek.plusWeeks(i), 0, 0));
            }
            return new ProfessorHealthTrend(points, toSvgPoints(points), "No survey submissions yet for the last " + normalizedWeeks + " weeks.");
        }

        List<String> courseIds = courses.stream().map(Course::getId).toList();
        List<TeamHealthCheckin> rows = teamHealthCheckinRepo
                .findByCourseIdInAndWeekStartBetween(courseIds, startWeek, endWeek);

        Map<LocalDate, List<TeamHealthCheckin>> byWeek = new HashMap<>();
        for (TeamHealthCheckin row : rows) {
            byWeek.computeIfAbsent(row.getWeekStart(), ignored -> new ArrayList<>()).add(row);
        }

        for (int i = 0; i < normalizedWeeks; i++) {
            LocalDate week = startWeek.plusWeeks(i);
            List<TeamHealthCheckin> weekRows = byWeek.getOrDefault(week, List.of());
            points.add(new WeeklyHealthPoint(week, averageHealthPercent(weekRows), weekRows.size()));
        }

        return new ProfessorHealthTrend(points, toSvgPoints(points), buildTrendSummary(points));
    }

    public StudentHealthStatus getStudentWeeklyStatus(Long studentId, LocalDate weekDate) {
        requireStudent(studentId);
        LocalDate weekStart = normalizeWeekStart(weekDate);

        List<CourseEnrollment> enrollments = courseEnrollmentRepo.findByStudentIdOrderByCreatedAtDesc(studentId);
        Set<String> coursesWithSurvey = getCourseIdsWithSurveyForWeek(
                enrollments.stream().map(CourseEnrollment::getCourseId).distinct().toList(),
                weekStart);
        Set<String> submittedCourseIds = new HashSet<>();
        for (TeamHealthCheckin checkin : teamHealthCheckinRepo.findByStudentIdAndWeekStart(studentId, weekStart)) {
            if (coursesWithSurvey.contains(checkin.getCourseId())) {
                submittedCourseIds.add(checkin.getCourseId());
            }
        }

        int enrolledCourses = (int) enrollments.stream()
                .map(CourseEnrollment::getCourseId)
                .filter(coursesWithSurvey::contains)
                .distinct()
                .count();
        int submitted = submittedCourseIds.size();
        int missing = Math.max(enrolledCourses - submitted, 0);

        return new StudentHealthStatus(weekStart, enrolledCourses, submitted, missing);
    }

    public List<TeamHealthCheckin> getStudentCourseCheckinsForWeek(Long studentId, List<String> courseIds, LocalDate weekDate) {
        requireStudent(studentId);
        if (courseIds == null || courseIds.isEmpty()) {
            return List.of();
        }

        LocalDate weekStart = normalizeWeekStart(weekDate);
        return teamHealthCheckinRepo.findByStudentIdAndCourseIdInAndWeekStart(studentId, courseIds, weekStart);
    }

    public List<TeamHealthCheckin> getCourseCheckinsForWeek(List<String> courseIds, LocalDate weekDate) {
        if (courseIds == null || courseIds.isEmpty()) {
            return List.of();
        }

        LocalDate weekStart = normalizeWeekStart(weekDate);
        return teamHealthCheckinRepo.findByCourseIdInAndWeekStart(courseIds, weekStart);
    }

    public LocalDate getWeekStart(LocalDate weekDate) {
        return normalizeWeekStart(weekDate);
    }

    @Transactional
    public WeeklySurvey publishWeeklySurvey(Long professorId,
            String courseId,
            String title,
            String description,
            LocalDate weekDate) {
        if (professorId == null) {
            throw new IllegalArgumentException("Professor must be logged in.");
        }

        String cleanCourseId = clean(courseId);
        Course course = courseRepo.findById(cleanCourseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found."));

        if (!professorId.equals(course.getProfessorId())) {
            throw new IllegalArgumentException("You can only publish surveys for your own course.");
        }

        String normalizedTitle = normalizeRequiredText(title, 120, "Survey title is required.");
        String normalizedDescription = normalizeRequiredText(description, 1000, "Survey description is required.");
        LocalDate weekStart = normalizeWeekStart(weekDate);

        Optional<WeeklySurvey> existing = weeklySurveyRepo.findByCourseIdAndWeekStart(course.getId(), weekStart);
        if (existing.isPresent()) {
            WeeklySurvey survey = existing.get();
            survey.setTitle(normalizedTitle);
            survey.setDescription(normalizedDescription);
            return weeklySurveyRepo.save(survey);
        }

        WeeklySurvey survey = new WeeklySurvey(course.getId(), professorId, weekStart, normalizedTitle, normalizedDescription);
        return weeklySurveyRepo.save(survey);
    }

    public WeeklySurvey getWeeklySurveyForCourseAndWeek(Long professorId, String courseId, LocalDate weekDate) {
        if (professorId == null) {
            throw new IllegalArgumentException("Professor must be logged in.");
        }

        String cleanCourseId = clean(courseId);
        Course course = courseRepo.findById(cleanCourseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found."));

        if (!professorId.equals(course.getProfessorId())) {
            throw new IllegalArgumentException("You can only view surveys for your own course.");
        }

        LocalDate weekStart = normalizeWeekStart(weekDate);
        return weeklySurveyRepo.findByCourseIdAndWeekStart(course.getId(), weekStart).orElse(null);
    }

    public Map<String, WeeklySurvey> getWeeklySurveysByCourseIdForWeek(List<String> courseIds, LocalDate weekDate) {
        if (courseIds == null || courseIds.isEmpty()) {
            return Map.of();
        }

        LocalDate weekStart = normalizeWeekStart(weekDate);
        Map<String, WeeklySurvey> surveysByCourse = new HashMap<>();
        for (WeeklySurvey survey : weeklySurveyRepo.findByCourseIdInAndWeekStart(courseIds, weekStart)) {
            surveysByCourse.put(survey.getCourseId(), survey);
        }
        return surveysByCourse;
    }

    private boolean isAtRisk(TeamHealthCheckin checkin) {
        return checkin.getHealthScore() <= 2
                || checkin.getCollaborationScore() <= 2
                || checkin.getWorkloadScore() <= 2;
    }

    private int averageHealthPercent(List<TeamHealthCheckin> checkins) {
        if (checkins.isEmpty()) {
            return 0;
        }
        double averageScore = checkins.stream()
                .mapToInt(TeamHealthCheckin::getHealthScore)
                .average()
                .orElse(0.0);

        return (int) Math.round((averageScore / 5.0) * 100.0);
    }

    private String toSvgPoints(List<WeeklyHealthPoint> points) {
        if (points.isEmpty()) {
            return "";
        }

        if (points.size() == 1) {
            int y = toGraphY(points.get(0).avgHealthPercent());
            return "130," + y;
        }

        double width = 240.0;
        double startX = 10.0;
        double step = width / (points.size() - 1.0);

        StringJoiner joiner = new StringJoiner(" ");
        for (int i = 0; i < points.size(); i++) {
            int x = (int) Math.round(startX + (i * step));
            int y = toGraphY(points.get(i).avgHealthPercent());
            joiner.add(x + "," + y);
        }
        return joiner.toString();
    }

    private int toGraphY(int avgPercent) {
        int bounded = Math.max(0, Math.min(avgPercent, 100));
        return (int) Math.round(70.0 - ((bounded / 100.0) * 46.0));
    }

    private String buildTrendSummary(List<WeeklyHealthPoint> points) {
        if (points.isEmpty()) {
            return "No trend data available.";
        }

        int totalSubmissions = points.stream().mapToInt(WeeklyHealthPoint::submissions).sum();
        int weeks = points.size();
        if (totalSubmissions == 0) {
            return "No survey submissions yet for the last " + weeks + " weeks.";
        }

        int first = points.get(0).avgHealthPercent();
        int last = points.get(weeks - 1).avgHealthPercent();

        if (last > first + 2) {
            return "Health trending upward over the last " + weeks + " weeks.";
        }
        if (last < first - 2) {
            return "Health trending downward over the last " + weeks + " weeks.";
        }
        return "Health trend is stable over the last " + weeks + " weeks.";
    }

    private int validateScore(Integer value, String errorMessage) {
        if (value == null || value < 1 || value > 5) {
            throw new IllegalArgumentException(errorMessage);
        }
        return value;
    }

    private LocalDate normalizeWeekStart(LocalDate date) {
        LocalDate base = date == null ? LocalDate.now() : date;
        return base.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private String normalizeStatus(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.length() <= 600) {
            return trimmed;
        }
        return trimmed.substring(0, 600);
    }

    private void requireStudent(Long studentId) {
        if (studentId == null) {
            throw new IllegalArgumentException("Student must be logged in.");
        }
    }

    private Set<String> getCourseIdsWithSurveyForWeek(List<String> courseIds, LocalDate weekStart) {
        if (courseIds == null || courseIds.isEmpty()) {
            return Set.of();
        }

        Set<String> results = new HashSet<>();
        for (WeeklySurvey survey : weeklySurveyRepo.findByCourseIdInAndWeekStart(courseIds, weekStart)) {
            results.add(survey.getCourseId());
        }
        return results;
    }

    private String normalizeRequiredText(String value, int maxLen, String requiredMessage) {
        if (value == null) {
            throw new IllegalArgumentException(requiredMessage);
        }

        String trimmed = value.trim();
        if (trimmed.isBlank()) {
            throw new IllegalArgumentException(requiredMessage);
        }
        if (trimmed.length() > maxLen) {
            return trimmed.substring(0, maxLen);
        }
        return trimmed;
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    public record ProfessorHealthSummary(LocalDate weekStart,
            int totalSubmissions,
            int avgHealthPercent,
            int atRiskResponses,
            int missingSurveys) {

    }

    public record StudentHealthStatus(LocalDate weekStart,
            int enrolledCourses,
            int submittedSurveys,
            int missingSurveys) {

    }

    public record WeeklyHealthPoint(LocalDate weekStart,
            int avgHealthPercent,
            int submissions) {

    }

    public record ProfessorHealthTrend(List<WeeklyHealthPoint> points,
            String svgPoints,
            String summaryText) {

    }
}
