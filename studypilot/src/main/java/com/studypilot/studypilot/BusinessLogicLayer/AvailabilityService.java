package com.studypilot.studypilot.BusinessLogicLayer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.studypilot.studypilot.DataAccessLayer.AvailabilityRepo;
import com.studypilot.studypilot.DataAccessLayer.CourseTimeSlotRepo;
import com.studypilot.studypilot.DataAccessLayer.TeamMemberRepo;
import com.studypilot.studypilot.DomainModel.Availability;
import com.studypilot.studypilot.DomainModel.CourseTimeSlot;
import com.studypilot.studypilot.DomainModel.TeamMember;

@Service
public class AvailabilityService {

    private final AvailabilityRepo availabilityRepo;
    private final CourseTimeSlotRepo courseTimeSlotRepo;
    private final TeamMemberRepo teamMemberRepo;

    public AvailabilityService(
            AvailabilityRepo availabilityRepo,
            CourseTimeSlotRepo courseTimeSlotRepo,
            TeamMemberRepo teamMemberRepo) {
        this.availabilityRepo = availabilityRepo;
        this.courseTimeSlotRepo= courseTimeSlotRepo;
        this.teamMemberRepo = teamMemberRepo;
    }

    @Transactional
    public void saveAvailability(Long studentId, String courseId, List<String> timeSlots) {
        availabilityRepo.deleteByStudentIdAndCourseId(studentId, courseId);

        if (timeSlots == null) {
            return;
        }

        for (String slot : timeSlots) {
            if (slot != null && !slot.trim().isBlank()) {
                availabilityRepo.save(new Availability(studentId, courseId, slot.trim()));
            }
        }
    }

    public Set<String> getStudentAvailabilitySet(Long studentId, String courseId) {
        List<Availability> rows = availabilityRepo.findByStudentIdAndCourseId(studentId, courseId);
        Set<String> result = new HashSet<>();

        for (Availability row : rows) {
            result.add(row.getTimeSlot());
        }

        return result;
    }

    public TeamAvailabilitySummary getTeamAvailabilitySummary(Long teamId, String courseId) {
        List<TeamMember> members = teamMemberRepo.findByTeamId(teamId);
        int teamSize = members.size();

        List<Long> studentIds = members.stream()
                .map(TeamMember::getStudentId)
                .collect(Collectors.toList());

        List<CourseTimeSlot> courseSlots = courseTimeSlotRepo.findByCourseId(courseId);

        Map<String, Integer> orderedCounts = new LinkedHashMap<>();
        for (CourseTimeSlot slot : courseSlots) {
            orderedCounts.put(slot.getSlotLabel(), 0);
        }

        if (!studentIds.isEmpty()) {
            List<Availability> savedAvailability =
                    availabilityRepo.findByStudentIdInAndCourseId(studentIds, courseId);

            Map<String, Integer> slotCounts = new HashMap<>();
            for (Availability availability : savedAvailability) {
                String slot = availability.getTimeSlot();
                slotCounts.put(slot, slotCounts.getOrDefault(slot, 0) + 1);
            }

            for (String slotLabel : orderedCounts.keySet()) {
                orderedCounts.put(slotLabel, slotCounts.getOrDefault(slotLabel, 0));
            }
        }

        int bestCount = 0;
        for (Integer count : orderedCounts.values()) {
            if (count > bestCount) {
                bestCount = count;
            }
        }

        return new TeamAvailabilitySummary(orderedCounts, bestCount, teamSize);
    }

    public record TeamAvailabilitySummary(
            Map<String, Integer> slotCounts,
            int bestCount,
            int teamSize) {
    }
}