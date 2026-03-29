package com.studypilot.studypilot.GUILayer;

import java.util.ArrayList;
import java.util.List;

/**
 * CourseTimeSlotForm component.
 */
public class CourseTimeSlotForm {

    // Bound to checkbox selections on the professor time slot publishing page.
    private List<String> selectedSlots = new ArrayList<>();

    public List<String> getSelectedSlots() {
        return selectedSlots;
    }

    public void setSelectedSlots(List<String> selectedSlots) {
        this.selectedSlots = selectedSlots;
    }
}
