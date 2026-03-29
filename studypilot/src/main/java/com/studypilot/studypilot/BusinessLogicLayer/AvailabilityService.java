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
import com.studypilot.studypilot.DataAccessLayer.UserRepo;
import com.studypilot.studypilot.DomainModel.Availability;
import com.studypilot.studypilot.DomainModel.CourseTimeSlot;
import com.studypilot.studypilot.DomainModel.TeamMember;
import com.studypilot.studypilot.DomainModel.User;

@Service
/**
 * Encapsulates availability persistence and aggregation logic.
 *
 * Service responsibilities: 1) store a student's selected slots for a course,
 * 2) build team-level overlap summaries, 3) build per-student submission
 * details for professor review.
 */
public class AvailabilityService {

    private final AvailabilityRepo availabilityRepo;
    private final CourseTimeSlotRepo courseTimeSlotRepo;
    private final TeamMemberRepo teamMemberRepo;
    private final UserRepo userRepo;

    public AvailabilityService(
            AvailabilityRepo availabilityRepo,
            CourseTimeSlotRepo courseTimeSlotRepo,
            TeamMemberRepo teamMemberRepo,
            UserRepo userRepo) {
        this.availabilityRepo = availabilityRepo;
        this.courseTimeSlotRepo = courseTimeSlotRepo;
        this.teamMemberRepo = teamMemberRepo;
        this.userRepo = userRepo;
    }

    @Transactional
    /**
     * Replaces a student's availability rows for the specified course.
     */
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

    /**
     * Returns the student's availability as a set for quick membership checks.
     */
    public Set<String> getStudentAvailabilitySet(Long studentId, String courseId) {
        List<Availability> rows = availabilityRepo.findByStudentIdAndCourseId(studentId, courseId);
        Set<String> result = new HashSet<>();

        for (Availability row : rows) {
            result.add(row.getTimeSlot());
        }

        return result;
    }

    /**
     * Aggregates team overlap counts per published course slot.
     */
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
            List<Availability> savedAvailability
                    = availabilityRepo.findByStudentIdInAndCourseId(studentIds, courseId);

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

    /**
     * Returns one row per team member including submission status and selected
     * slots.
     */
    public List<TeamStudentAvailabilityRow> getTeamStudentAvailability(Long teamId, String courseId) {
        List<TeamMember> members = teamMemberRepo.findByTeamId(teamId);
        List<Long> studentIds = members.stream()
                .map(TeamMember::getStudentId)
                .collect(Collectors.toList());

        Map<Long, Set<String>> slotSetByStudentId = new HashMap<>();
        for (Long studentId : studentIds) {
            slotSetByStudentId.put(studentId, new HashSet<>());
        }

        if (!studentIds.isEmpty()) {
            List<Availability> availabilityRows = availabilityRepo.findByStudentIdInAndCourseId(studentIds, courseId);
            for (Availability availability : availabilityRows) {
                Set<String> slots = slotSetByStudentId.get(availability.getStudentId());
                if (slots != null) {
                    slots.add(availability.getTimeSlot());
                }
            }
        }

        List<CourseTimeSlot> courseSlots = courseTimeSlotRepo.findByCourseId(courseId);
        List<String> slotOrder = courseSlots.stream().map(CourseTimeSlot::getSlotLabel).toList();

        return members.stream().map(member -> {
            Long studentId = member.getStudentId();
            Set<String> slotSet = slotSetByStudentId.getOrDefault(studentId, Set.of());

            List<String> orderedSlots = slotOrder.stream()
                    .filter(slotSet::contains)
                    .collect(Collectors.toList());

            User user = userRepo.findById(studentId).orElse(null);
            String fullName = user == null ? "Unknown Student" : user.getFullName();
            String email = user == null ? "" : user.getEmail();

            return new TeamStudentAvailabilityRow(
                    studentId,
                    fullName,
                    email,
                    orderedSlots,
                    !orderedSlots.isEmpty());
        }).toList();
    }

    public record TeamAvailabilitySummary(
            Map<String, Integer> slotCounts,
            int bestCount,
            int teamSize) {

    }

    /**
     * View model row for professor-facing student submission details.
     */
    public record TeamStudentAvailabilityRow(
            Long studentId,
            String fullName,
            String email,
            List<String> selectedSlots,
            boolean submitted) {

    }
}
