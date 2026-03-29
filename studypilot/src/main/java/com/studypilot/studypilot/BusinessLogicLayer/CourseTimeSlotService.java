package com.studypilot.studypilot.BusinessLogicLayer;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.studypilot.studypilot.DataAccessLayer.CourseTimeSlotRepo;
import com.studypilot.studypilot.DomainModel.CourseTimeSlot;

@Service
public class CourseTimeSlotService {

    private final CourseTimeSlotRepo repository;

    public CourseTimeSlotService(CourseTimeSlotRepo repository) {
        this.repository = repository;
    }

    public List<String> getSlotsForCourse(String courseId) {
        return repository.findByCourseId(courseId)
                         .stream()
                         .map(CourseTimeSlot::getSlotLabel)
                         .collect(Collectors.toList());
    }
}