package com.studypilot.studypilot;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfessorHomeController {

    @GetMapping("/prof/home")
    public String profHome(HttpSession session, Model model) {
        Object role = session.getAttribute("role");
        if (!"PROFESSOR".equals(role)) return "redirect:/login";

        model.addAttribute("fullName", session.getAttribute("fullName"));
        return "professor_home";
    }
}
