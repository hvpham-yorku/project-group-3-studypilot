package com.studypilot.studypilot;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StudentHomeController {

    @GetMapping("/student/home")
    public String studentHome(HttpSession session, Model model) {
        Object role = session.getAttribute("role");
        if (!"STUDENT".equals(role)) return "redirect:/login";

        model.addAttribute("fullName", session.getAttribute("fullName"));
        return "student_home";
    }
}
