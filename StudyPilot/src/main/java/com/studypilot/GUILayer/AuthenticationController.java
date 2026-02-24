package com.studypilot.studypilot;

import jakarta.servlet.http.HttpSession;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthenticationController {

    private final Authentication authService;

    public AuthenticationController(Authentication authService) {
        this.authService = authService;
    }

    @GetMapping("/register")
    public String showRegister(Model model) {
        model.addAttribute("form", new RegisterForm());
        return "register";
    }

    @PostMapping("/register")
    public String doRegister(@ModelAttribute("form") RegisterForm form, Model model, HttpSession session) {
        try {
            User user = authService.register(form.getFullName(), form.getEmail(), form.getPassword(), form.getRole());

            session.setAttribute("userId", user.getId());
            session.setAttribute("role", user.getRole());
            session.setAttribute("fullName", user.getFullName());

            return "STUDENT".equals(user.getRole()) ? "redirect:/student/home" : "redirect:/prof/home";

        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return "register";
        } catch (DataIntegrityViolationException ex) {
            model.addAttribute("error", "Database rejected the save. Check logs + table constraints.");
            return "register";
        } catch (Exception ex) {
            model.addAttribute("error", "Unexpected error: " + ex.getClass().getSimpleName());
            return "register";
        }
    }

    @GetMapping("/login")
    public String showLogin(Model model) {
        model.addAttribute("form", new LoginForm());
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@ModelAttribute("form") LoginForm form, Model model, HttpSession session) {
        try {
            User user = authService.login(form.getEmail(), form.getPassword());

            session.setAttribute("userId", user.getId());
            session.setAttribute("role", user.getRole());
            session.setAttribute("fullName", user.getFullName());

            return "STUDENT".equals(user.getRole()) ? "redirect:/student/home" : "redirect:/prof/home";

        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return "login";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
