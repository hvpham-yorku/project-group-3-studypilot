package com.studypilot.studypilot;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

@Controller
public class SettingsController {

    @GetMapping("/settings")
    public String showSettings(HttpSession session, Model model) {
        // Security Check
        Object role = session.getAttribute("role");
        if (role == null) {
            return "redirect:/login";        }

        // Pass the user's name to the page
        model.addAttribute("fullName", session.getAttribute("fullName"));
        
        return "settings"; 
    }
}
