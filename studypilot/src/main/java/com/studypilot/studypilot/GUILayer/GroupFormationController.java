package com.studypilot.studypilot.GUILayer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.studypilot.studypilot.BusinessLogicLayer.CourseService;
import com.studypilot.studypilot.BusinessLogicLayer.GroupFormationService;
import com.studypilot.studypilot.DomainModel.Course;
import com.studypilot.studypilot.DomainModel.GroupFormationActivity;
import com.studypilot.studypilot.DomainModel.SurveyQuestion;

import jakarta.servlet.http.HttpSession;

@Controller
public class GroupFormationController {

    private final CourseService courseService;
    private final GroupFormationService groupFormationService;

    public GroupFormationController(CourseService courseService,
            GroupFormationService groupFormationService) {
        this.courseService = courseService;
        this.groupFormationService = groupFormationService;
    }

    @GetMapping("/prof/{courseId}/{courseSlug}/group-formation")
    public String showGroupFormationPage(@PathVariable("courseId") String courseId,
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

        groupFormationService.checkAndCloseExpiredActivities(courseId);

        List<GroupFormationActivity> activities
                = groupFormationService.getActivitiesForCourse(courseId);

        Map<Long, GroupFormationService.ActivityResponseStatus> responseStatusMap = new HashMap<>();
        Map<Long, List<GroupFormationService.FormedGroupView>> formedGroupsMap = new HashMap<>();
        Map<Long, List<SurveyQuestion>> questionsMap = new HashMap<>();

        for (GroupFormationActivity activity : activities) {
            responseStatusMap.put(activity.getId(),
                    groupFormationService.getResponseStatus(activity.getId(), courseId));

            if ("SORTED".equals(activity.getStatus())) {
                formedGroupsMap.put(activity.getId(),
                        groupFormationService.getFormedGroups(activity.getId()));
            }

            questionsMap.put(activity.getId(),
                    groupFormationService.getQuestionsForActivity(activity.getId()));
        }

        model.addAttribute("course", course);
        model.addAttribute("courseSlug", courseSlug);
        model.addAttribute("form", new CreateGroupFormationForm());
        model.addAttribute("activities", activities);
        model.addAttribute("responseStatusMap", responseStatusMap);
        model.addAttribute("formedGroupsMap", formedGroupsMap);
        model.addAttribute("questionsMap", questionsMap);
        model.addAttribute("fullName", session.getAttribute("fullName"));

        return "group_formation_page";
    }

    @PostMapping("/prof/{courseId}/{courseSlug}/group-formation")
    public String createGroupFormation(@PathVariable("courseId") String courseId,
            @PathVariable("courseSlug") String courseSlug,
            @ModelAttribute("form") CreateGroupFormationForm form,
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
            groupFormationService.createActivity(professorId, courseId, form);
            return "redirect:/prof/" + courseId + "/" + courseSlug + "/group-formation";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("course", course);
            model.addAttribute("courseSlug", courseSlug);
            model.addAttribute("form", form);
            model.addAttribute("activities", groupFormationService.getActivitiesForCourse(courseId));
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("fullName", session.getAttribute("fullName"));
            return "group_formation_page";
        }
    }

    @GetMapping("/prof/{courseId}/{courseSlug}/group-formation/{activityId}/edit")
    public String showEditPage(@PathVariable("courseId") String courseId,
            @PathVariable("courseSlug") String courseSlug,
            @PathVariable("activityId") Long activityId,
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

        CreateGroupFormationForm form
                = groupFormationService.getEditForm(courseId, activityId, professorId);

        model.addAttribute("course", course);
        model.addAttribute("courseSlug", courseSlug);
        model.addAttribute("form", form);
        model.addAttribute("activityId", activityId);
        model.addAttribute("fullName", session.getAttribute("fullName"));

        return "group_formation_edit_page";
    }

    @PostMapping("/prof/{courseId}/{courseSlug}/group-formation/{activityId}/edit")
    public String updateGroupFormation(@PathVariable("courseId") String courseId,
            @PathVariable("courseSlug") String courseSlug,
            @PathVariable("activityId") Long activityId,
            @ModelAttribute("form") CreateGroupFormationForm form,
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
            groupFormationService.updateActivity(professorId, courseId, activityId, form);
            return "redirect:/prof/" + courseId + "/" + courseSlug + "/group-formation";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("course", course);
            model.addAttribute("courseSlug", courseSlug);
            model.addAttribute("form", form);
            model.addAttribute("activityId", activityId);
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("fullName", session.getAttribute("fullName"));
            return "group_formation_edit_page";
        }
    }

    @PostMapping("/prof/{courseId}/{courseSlug}/group-formation/{activityId}/delete")
    public String deleteGroupFormation(@PathVariable("courseId") String courseId,
            @PathVariable("courseSlug") String courseSlug,
            @PathVariable("activityId") Long activityId,
            HttpSession session) {
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

        groupFormationService.deleteActivity(professorId, courseId, activityId);

        return "redirect:/prof/" + courseId + "/" + courseSlug + "/group-formation";
    }

    @PostMapping("/prof/{courseId}/{courseSlug}/group-formation/{activityId}/close")
    public String closeActivity(@PathVariable("courseId") String courseId,
            @PathVariable("courseSlug") String courseSlug,
            @PathVariable("activityId") Long activityId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        if (!isProfessor(session)) {
            return "redirect:/login";
        }

        Long professorId = (Long) session.getAttribute("userId");
        try {
            groupFormationService.closeActivity(professorId, courseId, activityId);
            redirectAttributes.addFlashAttribute("success", "Activity closed. You can now auto-sort students into groups.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/prof/" + courseId + "/" + courseSlug + "/group-formation";
    }

    @PostMapping("/prof/{courseId}/{courseSlug}/group-formation/{activityId}/reopen")
    public String reopenActivity(@PathVariable("courseId") String courseId,
            @PathVariable("courseSlug") String courseSlug,
            @PathVariable("activityId") Long activityId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        if (!isProfessor(session)) {
            return "redirect:/login";
        }

        Long professorId = (Long) session.getAttribute("userId");
        try {
            groupFormationService.reopenActivity(professorId, courseId, activityId);
            redirectAttributes.addFlashAttribute("success", "Activity reopened for student responses.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/prof/" + courseId + "/" + courseSlug + "/group-formation";
    }

    @PostMapping("/prof/{courseId}/{courseSlug}/group-formation/{activityId}/auto-sort")
    public String autoSortActivity(@PathVariable("courseId") String courseId,
            @PathVariable("courseSlug") String courseSlug,
            @PathVariable("activityId") Long activityId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        if (!isProfessor(session)) {
            return "redirect:/login";
        }

        Long professorId = (Long) session.getAttribute("userId");
        try {
            groupFormationService.autoSortActivity(professorId, courseId, activityId);
            redirectAttributes.addFlashAttribute("success", "Students have been sorted into groups using AI!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Auto-sort failed: " + ex.getMessage());
        }

        return "redirect:/prof/" + courseId + "/" + courseSlug + "/group-formation";
    }

    @GetMapping("/prof/{courseId}/{courseSlug}/group-formation/{activityId}/groups")
    public String viewFormedGroups(@PathVariable("courseId") String courseId,
            @PathVariable("courseSlug") String courseSlug,
            @PathVariable("activityId") Long activityId,
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

        List<GroupFormationActivity> activities = groupFormationService.getActivitiesForCourse(courseId);
        GroupFormationActivity activity = activities.stream()
                .filter(a -> a.getId().equals(activityId))
                .findFirst()
                .orElse(null);

        if (activity == null) {
            return "redirect:/prof/" + courseId + "/" + courseSlug + "/group-formation";
        }

        List<GroupFormationService.FormedGroupView> groups = groupFormationService.getFormedGroups(activityId);
        GroupFormationService.ActivityResponseStatus responseStatus =
                groupFormationService.getResponseStatus(activityId, courseId);

        model.addAttribute("course", course);
        model.addAttribute("courseSlug", courseSlug);
        model.addAttribute("activity", activity);
        model.addAttribute("groups", groups);
        model.addAttribute("responseStatus", responseStatus);
        model.addAttribute("fullName", session.getAttribute("fullName"));

        return "professor_groups_page";
    }

    @PostMapping("/prof/{courseId}/{courseSlug}/group-formation/{activityId}/move-student")
    public ResponseEntity<Map<String, Object>> moveStudent(@PathVariable("courseId") String courseId,
            @PathVariable("courseSlug") String courseSlug,
            @PathVariable("activityId") Long activityId,
            @RequestParam("studentId") Long studentId,
            @RequestParam("fromGroupId") Long fromGroupId,
            @RequestParam("toGroupId") Long toGroupId,
            HttpSession session) {

        Map<String, Object> result = new HashMap<>();

        if (!isProfessor(session)) {
            result.put("success", false);
            result.put("error", "Not authorized.");
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .body(result);
        }

        Long professorId = (Long) session.getAttribute("userId");
        try {
            groupFormationService.moveStudentBetweenGroups(professorId, courseId, activityId, studentId, fromGroupId, toGroupId);
            result.put("success", true);
        } catch (Exception ex) {
            result.put("success", false);
            result.put("error", ex.getMessage());
        }

        return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .body(result);
    }

    private boolean isProfessor(HttpSession session) {
        return "PROFESSOR".equals(session.getAttribute("role"));
    }
}
