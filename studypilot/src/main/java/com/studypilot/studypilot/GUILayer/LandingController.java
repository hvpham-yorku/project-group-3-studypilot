package com.studypilot.studypilot.GUILayer;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LandingController {

    @GetMapping("/")
    public String showLandingPage() {
        return "landing-page"; // This should correspond to the Thymeleaf template file "landing.html"
    }
}
