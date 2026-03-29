package com.studypilot.studypilot.BusinessLogicLayer;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
/**
 * GroupFormationAlgorithmService component.
 */
public class GroupFormationAlgorithmService {

    private final AvailabilityService availabilityService;

    public GroupFormationAlgorithmService(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    public GroupingResult generateGroups(GroupingRequest request, String courseId) {
        validateRequest(request);

        List<StudentSurveyProfile> participants = request.students().stream()
                .filter(StudentSurveyProfile::participant)
                .map(student -> new StudentSurveyProfile(
                        student.studentId(),
                        student.participant(),
                        availabilityService.getStudentAvailabilitySet(student.studentId(), courseId),
                        student.topicChoices(),
                        student.skillChoices(),
                        student.leadershipConfidence()
                ))
                .collect(Collectors.toList());

        List<StudentSurveyProfile> nonParticipants = request.students().stream()
                .filter(s -> !s.participant())
                .map(student -> new StudentSurveyProfile(
                        student.studentId(),
                        student.participant(),
                        availabilityService.getStudentAvailabilitySet(student.studentId(), courseId),
                        student.topicChoices(),
                        student.skillChoices(),
                        student.leadershipConfidence()
                ))
                .collect(Collectors.toList());

        List<GroupTeam> resultTeams = new ArrayList<>();
        Set<Long> alreadyAssigned = new HashSet<>();

        resultTeams.addAll(groupCohort(participants, request, alreadyAssigned));
        resultTeams.addAll(groupCohort(nonParticipants, request, alreadyAssigned));

        return new GroupingResult(resultTeams);
    }

    private List<GroupTeam> groupCohort(
            List<StudentSurveyProfile> cohort,
            GroupingRequest request,
            Set<Long> alreadyAssigned) {

        List<GroupTeam> teams = new ArrayList<>();
        List<StudentSurveyProfile> remaining = new ArrayList<>(cohort);

        while (!remaining.isEmpty()) {

            int targetSize = chooseNextTargetSize(
                    remaining.size(),
                    request.preferredGroupSize(),
                    request.minTeamSize(),
                    request.maxTeamSize()
            );

            List<StudentSurveyProfile> chosen = pickBestGroup(remaining, targetSize);

            GroupTeam team = new GroupTeam(
                    teams.size() + 1,
                    chosen.stream()
                            .map(StudentSurveyProfile::studentId)
                            .collect(Collectors.toList()),
                    chosen.size()
            );

            teams.add(team);

            Set<Long> chosenIds = chosen.stream()
                    .map(StudentSurveyProfile::studentId)
                    .collect(Collectors.toSet());

            alreadyAssigned.addAll(chosenIds);
            remaining.removeIf(student -> chosenIds.contains(student.studentId()));
        }

        return teams;
    }

   
    private List<StudentSurveyProfile> pickBestGroup(
            List<StudentSurveyProfile> students,
            int size) {

   
        students.sort((a, b) ->
                Integer.compare(b.availabilitySlots().size(), a.availabilitySlots().size()));

        return new ArrayList<>(students.subList(0, Math.min(size, students.size())));
    }

    
    private int chooseNextTargetSize(int remaining, int preferred, int min, int max) {
        if (remaining <= max) return Math.max(min, remaining);
        return preferred;
    }

    private void validateRequest(GroupingRequest request) {
        if (request == null)
            throw new IllegalArgumentException("Grouping request is required.");

        if (request.preferredGroupSize() < 2)
            throw new IllegalArgumentException("Preferred group size must be at least 2.");

        if (request.minTeamSize() < 2)
            throw new IllegalArgumentException("Minimum team size must be at least 2.");

        if (request.minTeamSize() > request.preferredGroupSize())
            throw new IllegalArgumentException("Minimum team size cannot exceed preferred.");

        if (request.preferredGroupSize() > request.maxTeamSize())
            throw new IllegalArgumentException("Preferred cannot exceed max.");
    }

    

    public record GroupingRequest(
            int preferredGroupSize,
            int minTeamSize,
            int maxTeamSize,
            boolean groupTopicsSimilarly,
            boolean groupSkillsSimilarly,
            Set<Long> enrolledStudentIds,
            List<StudentSurveyProfile> students) {}

    public record StudentSurveyProfile(
            Long studentId,
            boolean participant,
            Set<String> availabilitySlots,
            Set<String> topicChoices,
            Set<String> skillChoices,
            int leadershipConfidence) {}

    public record GroupTeam(
            int teamNumber,
            List<Long> memberIds,
            int size) {}

    public record GroupingResult(
            List<GroupTeam> teams) {}
}
