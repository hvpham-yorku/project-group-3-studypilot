package com.studypilot.studypilot.GUILayer;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
/**
 * LandingController component.
 */
public class LandingController {

    @GetMapping("/")
    public String showLandingPage() {
        return "landing-page"; 
    }
}
