package com.studypilot.studypilot;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

@Controller
public class SettingsController {

    @GetMapping("/settings")
    public String showSettings(HttpSession session, Model model) {
       
        Object role = session.getAttribute("role");
        if (role == null) {
            return "redirect:/login";
        }

        
        model.addAttribute("fullName", session.getAttribute("fullName"));
        model.addAttribute("email", session.getAttribute("email"));

        return "settings";
    }
}
