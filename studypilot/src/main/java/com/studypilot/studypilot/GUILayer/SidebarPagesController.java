package com.studypilot.studypilot.GUILayer;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

@Controller
public class SidebarPagesController {

    @GetMapping("/courses")
    public String courses(HttpSession session, Model model) {
        if (!isAuthenticated(session)) {
            return "redirect:/login";
        }

        model.addAttribute("fullName", session.getAttribute("fullName"));
        model.addAttribute("courses", List.of(
                new CourseDemo("EECS2311", "Software Design", "Mondays 2:30 PM", 312, "Active"),
                new CourseDemo("EECS2030", "Advanced Object-Oriented Programming", "Wednesdays 11:30 AM", 268,
                        "Active"),
                new CourseDemo("EECS2001", "Data Structures", "Fridays 9:30 AM", 289, "Needs Review")));
        return "my_courses";
    }

    @GetMapping("/faq-library")
    public String faqLibrary(HttpSession session, Model model) {
        if (!isAuthenticated(session)) {
            return "redirect:/login";
        }

        model.addAttribute("fullName", session.getAttribute("fullName"));
        model.addAttribute("faqItems", List.of(
                new FaqDemo("Exam Policy", "Is a calculator allowed in the final exam?", "Yes, non-programmable only.",
                        124),
                new FaqDemo("Assignments", "Can I submit Assignment 2 late?", "Late submissions are accepted for 48 hours with 10% penalty.",
                        98),
                new FaqDemo("Office Hours", "Where are office hours held?", "Bergeron Centre, Room 3120 and Zoom on Fridays.",
                        73),
                new FaqDemo("Labs", "Do I need to attend all labs?", "At least 9 of 11 labs are required for full participation marks.",
                        65)));
        return "faq_library";
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

    public record CourseDemo(String code, String name, String schedule, int students, String status) {

    }

    public record FaqDemo(String category, String question, String answer, int timesUsed) {

    }

    public record TrendDemo(String day, int value) {

    }
}
