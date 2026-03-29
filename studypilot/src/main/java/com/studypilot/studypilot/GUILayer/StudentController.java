package com.studypilot.studypilot.GUILayer;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.studypilot.studypilot.BusinessLogicLayer.CourseTimeSlotService;

@Controller
/**
 * StudentController component.
 */
public class StudentController {

    private final CourseTimeSlotService slotService;

    public StudentController(CourseTimeSlotService slotService) {
        this.slotService = slotService;
    }

    @GetMapping("/student/availability/{courseId}")
    public String showAvailability(@PathVariable String courseId, Model model) {
        List<String> slots = slotService.getSlotsForCourse(courseId);
        model.addAttribute("slots", slots);
        model.addAttribute("courseId", courseId);
        return "student_availability"; // your Thymeleaf template
    }
}
