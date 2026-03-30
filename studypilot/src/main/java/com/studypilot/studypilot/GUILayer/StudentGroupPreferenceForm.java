package com.studypilot.studypilot.GUILayer;

import java.util.HashMap;
import java.util.Map;

public class StudentGroupPreferenceForm {

    private String notes;
    private String availabilitySlots;
    private Map<String, String> responses = new HashMap<>();

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getAvailabilitySlots() {
        return availabilitySlots;
    }

    public void setAvailabilitySlots(String availabilitySlots) {
        this.availabilitySlots = availabilitySlots;
    }

    public Map<String, String> getResponses() {
        return responses;
    }

    public void setResponses(Map<String, String> responses) {
        this.responses = responses;
    }
}
