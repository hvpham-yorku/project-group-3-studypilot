package com.studypilot.studypilot.GUILayer;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.studypilot.studypilot.BusinessLogicLayer.CourseService;
import com.studypilot.studypilot.BusinessLogicLayer.GroupFormationService;
import com.studypilot.studypilot.DomainModel.Course;
import com.studypilot.studypilot.DomainModel.GroupFormationActivity;

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

        List<GroupFormationActivity> activities
                = groupFormationService.getActivitiesForCourse(courseId);

        model.addAttribute("course", course);
        model.addAttribute("courseSlug", courseSlug);
        model.addAttribute("form", new CreateGroupFormationForm());
        model.addAttribute("activities", activities);
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

    private boolean isProfessor(HttpSession session) {
        return "PROFESSOR".equals(session.getAttribute("role"));
    }
}
