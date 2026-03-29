package com.studypilot.studypilot.GUILayer;

import java.util.ArrayList;
import java.util.List;

/**
 * AvailabilityForm component.
 */
public class AvailabilityForm {

    private List<String> selectedSlots = new ArrayList<>();

    public List<String> getSelectedSlots() {
        return selectedSlots;
    }

    public void setSelectedSlots(List<String> selectedSlots) {
        this.selectedSlots = selectedSlots;
    }
}
