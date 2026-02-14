package com.studypilot.studypilot;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LandingControllerTests {

    @Test
    void testShowLandingPage() {
        LandingController controller = new LandingController();
        String viewName = controller.showLandingPage();
        assertEquals("landing-page", viewName, "LandingController should return 'landing-page' view");
    }
}
