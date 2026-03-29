package com.studypilot.studypilot;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.studypilot.studypilot.BusinessLogicLayer.AvailabilityService;
import com.studypilot.studypilot.DataAccessLayer.AvailabilityRepo;
import com.studypilot.studypilot.DataAccessLayer.CourseTimeSlotRepo;
import com.studypilot.studypilot.DataAccessLayer.TeamMemberRepo;
import com.studypilot.studypilot.DataAccessLayer.UserRepo;
import com.studypilot.studypilot.DomainModel.Availability;
import com.studypilot.studypilot.DomainModel.CourseTimeSlot;
import com.studypilot.studypilot.DomainModel.TeamMember;

/**
 * AvailabilityServiceTest component.
 */
class AvailabilityServiceTest {

    private AvailabilityRepo availabilityRepo;
    private CourseTimeSlotRepo courseTimeSlotRepo;
    private TeamMemberRepo teamMemberRepo;
    private UserRepo userRepo;
    private AvailabilityService availabilityService;

    @BeforeEach
    void setUp() {
        availabilityRepo = mock(AvailabilityRepo.class);
        courseTimeSlotRepo = mock(CourseTimeSlotRepo.class);
        teamMemberRepo = mock(TeamMemberRepo.class);
        userRepo = mock(UserRepo.class);
        availabilityService = new AvailabilityService(availabilityRepo, courseTimeSlotRepo, teamMemberRepo, userRepo);
    }

    @Test
    void saveAvailability_replacesOldSlotsAndSavesTrimmedValues() {
        availabilityService.saveAvailability(7L, "EECS2311", List.of("MON_2PM", "  TUE_4PM  "));

        verify(availabilityRepo).deleteByStudentIdAndCourseId(7L, "EECS2311");
        verify(availabilityRepo, times(2)).save(any(Availability.class));
    }

    @Test
    void saveAvailability_withNullSlotsOnlyDeletesExistingRows() {
        availabilityService.saveAvailability(7L, "EECS2311", null);

        verify(availabilityRepo).deleteByStudentIdAndCourseId(7L, "EECS2311");
    }

    @Test
    void getStudentAvailabilitySet_returnsDistinctSlots() {
        when(availabilityRepo.findByStudentIdAndCourseId(3L, "EECS2311")).thenReturn(List.of(
                new Availability(3L, "EECS2311", "MON_2PM"),
                new Availability(3L, "EECS2311", "MON_2PM"),
                new Availability(3L, "EECS2311", "WED_10AM")));

        Set<String> result = availabilityService.getStudentAvailabilitySet(3L, "EECS2311");

        assertEquals(2, result.size());
        assertTrue(result.contains("MON_2PM"));
        assertTrue(result.contains("WED_10AM"));
    }

    @Test
    void getTeamAvailabilitySummary_countsPerSlotInDefinedOrder() {
        when(teamMemberRepo.findByTeamId(20L)).thenReturn(List.of(
                new TeamMember(20L, 1L),
                new TeamMember(20L, 2L),
                new TeamMember(20L, 3L)));

        when(courseTimeSlotRepo.findByCourseId("EECS2311")).thenReturn(List.of(
                new CourseTimeSlot("EECS2311", "MON_2PM"),
                new CourseTimeSlot("EECS2311", "TUE_4PM"),
                new CourseTimeSlot("EECS2311", "WED_10AM")));

        when(availabilityRepo.findByStudentIdInAndCourseId(List.of(1L, 2L, 3L), "EECS2311")).thenReturn(List.of(
                new Availability(1L, "EECS2311", "MON_2PM"),
                new Availability(2L, "EECS2311", "MON_2PM"),
                new Availability(3L, "EECS2311", "WED_10AM")));

        AvailabilityService.TeamAvailabilitySummary summary
                = availabilityService.getTeamAvailabilitySummary(20L, "EECS2311");

        assertEquals(3, summary.teamSize());
        assertEquals(2, summary.bestCount());
        assertEquals(Map.of("MON_2PM", 2, "TUE_4PM", 0, "WED_10AM", 1), summary.slotCounts());
    }

    @Test
    void getTeamAvailabilitySummary_withNoMembers_returnsZeroedCounts() {
        when(teamMemberRepo.findByTeamId(99L)).thenReturn(List.of());
        when(courseTimeSlotRepo.findByCourseId("EECS2311")).thenReturn(List.of(
                new CourseTimeSlot("EECS2311", "MON_2PM")));

        AvailabilityService.TeamAvailabilitySummary summary
                = availabilityService.getTeamAvailabilitySummary(99L, "EECS2311");

        assertEquals(0, summary.teamSize());
        assertEquals(0, summary.bestCount());
        assertEquals(Map.of("MON_2PM", 0), summary.slotCounts());
        verifyNoInteractions(availabilityRepo);
    }
}
