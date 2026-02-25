package com.studypilot.studypilot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import com.studypilot.studypilot.GUILayer.LandingController;

class LandingControllerTests {

    @Test
    void testShowLandingPage() {
        LandingController controller = new LandingController();
        String viewName = controller.showLandingPage();
        assertEquals("landing-page", viewName, "LandingController should return 'landing-page' view");
    }
}
