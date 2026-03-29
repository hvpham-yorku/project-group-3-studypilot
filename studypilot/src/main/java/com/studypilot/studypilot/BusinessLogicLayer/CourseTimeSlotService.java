package com.studypilot.studypilot.BusinessLogicLayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.studypilot.studypilot.DataAccessLayer.CourseTimeSlotRepo;
import com.studypilot.studypilot.DomainModel.CourseTimeSlot;

@Service
/**
 * Manages course-level meeting slot options configured by professors.
 */
public class CourseTimeSlotService {

    private final CourseTimeSlotRepo repository;

    public CourseTimeSlotService(CourseTimeSlotRepo repository) {
        this.repository = repository;
    }

    /**
     * Returns saved slot labels for a course in database order.
     */
    public List<String> getSlotsForCourse(String courseId) {
        return repository.findByCourseId(courseId)
                .stream()
                .map(CourseTimeSlot::getSlotLabel)
                .collect(Collectors.toList());
    }

    /**
     * Canonical list of selectable meeting slots shown in the professor UI.
     */
    public List<String> getPresetSlotOptions() {
        return List.of(
                "Mon 09:00-10:00", "Mon 10:00-11:00", "Mon 14:00-15:00", "Mon 16:00-17:00",
                "Tue 09:00-10:00", "Tue 11:00-12:00", "Tue 14:00-15:00", "Tue 17:00-18:00",
                "Wed 09:00-10:00", "Wed 12:00-13:00", "Wed 15:00-16:00", "Wed 18:00-19:00",
                "Thu 09:00-10:00", "Thu 11:00-12:00", "Thu 14:00-15:00", "Thu 16:00-17:00",
                "Fri 09:00-10:00", "Fri 11:00-12:00", "Fri 13:00-14:00", "Fri 15:00-16:00");
    }

    @Transactional
    /**
     * Replaces all published slots for a course with the provided valid
     * selections.
     */
    public void replaceSlotsForCourse(String courseId, List<String> selectedSlots) {
        repository.deleteByCourseId(courseId);

        if (selectedSlots == null || selectedSlots.isEmpty()) {
            return;
        }

        Set<String> allowedSlots = Set.copyOf(getPresetSlotOptions());
        List<CourseTimeSlot> rows = new ArrayList<>();

        Stream<String> cleaned = selectedSlots.stream()
                .filter(slot -> slot != null && !slot.trim().isBlank())
                .map(String::trim)
                .distinct();

        cleaned.forEach(slot -> {
            if (allowedSlots.contains(slot)) {
                rows.add(new CourseTimeSlot(courseId, slot));
            }
        });

        if (!rows.isEmpty()) {
            repository.saveAll(rows);
        }
    }
}
