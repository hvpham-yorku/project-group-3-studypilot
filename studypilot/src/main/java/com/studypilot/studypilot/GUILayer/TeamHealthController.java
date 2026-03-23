package com.studypilot.studypilot.GUILayer;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.studypilot.studypilot.BusinessLogicLayer.TeamHealthService;
import com.studypilot.studypilot.DomainModel.TeamHealthCheckin;

import jakarta.servlet.http.HttpSession;

@RestController
public class TeamHealthController {

    private final TeamHealthService teamHealthService;

    public TeamHealthController(TeamHealthService teamHealthService) {
        this.teamHealthService = teamHealthService;
    }

    @PostMapping("/student/health/checkin")
    public ResponseEntity<?> submitStudentHealthCheckin(@RequestBody SubmitHealthCheckinRequest request,
            HttpSession session) {
        if (!"STUDENT".equals(session.getAttribute("role"))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Only students can submit health check-ins."));
        }

        Long studentId = (Long) session.getAttribute("userId");

        try {
            TeamHealthCheckin saved = teamHealthService.saveWeeklyCheckin(
                    studentId,
                    request.courseId(),
                    request.healthScore(),
                    request.workloadScore(),
                    request.collaborationScore(),
                    request.statusText(),
                    request.weekDate());

            return ResponseEntity.ok(new CheckinSavedResponse(
                    saved.getId(),
                    saved.getCourseId(),
                    saved.getWeekStart(),
                    saved.getUpdatedAt()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage()));
        }
    }

    @GetMapping("/student/health/status")
    public ResponseEntity<?> getStudentWeeklyHealthStatus(
            @RequestParam(name = "weekDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekDate,
            HttpSession session) {
        if (!"STUDENT".equals(session.getAttribute("role"))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Only students can access student health status."));
        }

        Long studentId = (Long) session.getAttribute("userId");

        try {
            return ResponseEntity.ok(teamHealthService.getStudentWeeklyStatus(studentId, weekDate));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage()));
        }
    }

    @GetMapping("/prof/health/summary")
    public ResponseEntity<?> getProfessorWeeklyHealthSummary(
            @RequestParam(name = "weekDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekDate,
            HttpSession session) {
        if (!"PROFESSOR".equals(session.getAttribute("role"))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Only professors can access health summary."));
        }

        Long professorId = (Long) session.getAttribute("userId");

        try {
            return ResponseEntity.ok(teamHealthService.getProfessorWeeklySummary(professorId, weekDate));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage()));
        }
    }

    public record SubmitHealthCheckinRequest(String courseId,
            Integer healthScore,
            Integer workloadScore,
            Integer collaborationScore,
            String statusText,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekDate) {

    }

    public record CheckinSavedResponse(Long checkinId,
            String courseId,
            LocalDate weekStart,
            java.time.OffsetDateTime updatedAt) {

    }

    public record ErrorResponse(String error) {

    }
}
