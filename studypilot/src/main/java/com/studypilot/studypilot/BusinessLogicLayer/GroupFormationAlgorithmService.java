package com.studypilot.studypilot.BusinessLogicLayer;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class GroupFormationAlgorithmService {

    public GroupingResult generateGroups(GroupingRequest request) {
        validateRequest(request);

        List<StudentSurveyProfile> participants = request.students().stream()
                .filter(StudentSurveyProfile::participant)
                .toList();

        List<StudentSurveyProfile> nonParticipants = request.students().stream()
                .filter(s -> !s.participant())
                .toList();

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
            int targetSize = chooseNextTargetSize(remaining.size(), request.preferredGroupSize(),
                    request.minTeamSize(), request.maxTeamSize());

            PairScoreMatrix matrix = buildPairScoreMatrix(remaining, request);

            List<StudentSurveyProfile> chosen = findConsistentWindowGroup(
                    remaining, matrix, targetSize, request, alreadyAssigned);

            if (chosen == null || chosen.isEmpty()) {
                chosen = fallbackBestValidGroup(remaining, matrix, targetSize, request, alreadyAssigned);
            }

            GroupTeam team = new GroupTeam(
                    teams.size() + 1,
                    chosen.stream().map(StudentSurveyProfile::studentId).toList(),
                    chosen.size());

            teams.add(team);

            Set<Long> chosenIds = chosen.stream()
                    .map(StudentSurveyProfile::studentId)
                    .collect(Collectors.toSet());

            alreadyAssigned.addAll(chosenIds);
            remaining.removeIf(student -> chosenIds.contains(student.studentId()));
        }

        return rebalanceTeams(teams, request);
    }

    private PairScoreMatrix buildPairScoreMatrix(List<StudentSurveyProfile> students, GroupingRequest request) {
        Map<Long, Map<Long, Double>> scores = new HashMap<>();

        for (StudentSurveyProfile a : students) {
            scores.putIfAbsent(a.studentId(), new HashMap<>());
            for (StudentSurveyProfile b : students) {
                if (Objects.equals(a.studentId(), b.studentId())) {
                    continue;
                }
                double score = pairScore(a, b, request);
                scores.get(a.studentId()).put(b.studentId(), score);
            }
        }

        return new PairScoreMatrix(scores);
    }

    private double pairScore(StudentSurveyProfile a, StudentSurveyProfile b, GroupingRequest request) {
        double availabilityScore = normalizedSetSimilarity(a.availabilitySlots(), b.availabilitySlots());

        double topicScore = request.groupTopicsSimilarly()
                ? normalizedSetSimilarity(a.topicChoices(), b.topicChoices())
                : normalizedSetDissimilarity(a.topicChoices(), b.topicChoices());

        double skillScore = request.groupSkillsSimilarly()
                ? normalizedSetSimilarity(a.skillChoices(), b.skillChoices())
                : normalizedSetDissimilarity(a.skillChoices(), b.skillChoices());

        double leadershipScore = leadershipCompatibility(a.leadershipConfidence(), b.leadershipConfidence());

        return availabilityScore
                + topicScore
                + skillScore
                + leadershipScore;
    }

    private double normalizedSetSimilarity(Set<String> a, Set<String> b) {
        if (a.isEmpty() && b.isEmpty()) {
            return 1.0;
        }

        int common = 0;
        for (String value : a) {
            if (b.contains(value)) {
                common++;
            }
        }

        int total = a.size() + b.size();
        if (total == 0) {
            return 1.0;
        }

        return (2.0 * common) / total;
    }

    private double normalizedSetDissimilarity(Set<String> a, Set<String> b) {
        if (a.isEmpty() && b.isEmpty()) {
            return 0.0;
        }

        Set<String> union = new HashSet<>(a);
        union.addAll(b);

        int common = 0;
        for (String value : a) {
            if (b.contains(value)) {
                common++;
            }
        }

        int allAnswersIncludingRepeats = a.size() + b.size();
        if (allAnswersIncludingRepeats == 0) {
            return 0.0;
        }

        return (double) (union.size() - common) / allAnswersIncludingRepeats;
    }

    private double leadershipCompatibility(int a, int b) {
        int diff = Math.abs(a - b);
        return 1.0 - (diff / 4.0);
    }

    private List<StudentSurveyProfile> findConsistentWindowGroup(
            List<StudentSurveyProfile> remaining,
            PairScoreMatrix matrix,
            int preferredWindowSize,
            GroupingRequest request,
            Set<Long> alreadyAssigned) {

        int maxWindow = remaining.size();

        for (StudentSurveyProfile anchor : remaining) {
            for (int windowSize = preferredWindowSize; windowSize <= maxWindow; windowSize++) {
                List<Long> anchorWindow = matrix.topMatches(anchor.studentId(), windowSize - 1);
                if (anchorWindow.size() < request.minTeamSize() - 1) {
                    continue;
                }

                LinkedHashSet<Long> candidateIds = new LinkedHashSet<>();
                candidateIds.add(anchor.studentId());
                candidateIds.addAll(anchorWindow);

                List<Long> trimmed = new ArrayList<>(candidateIds);

                if (trimmed.size() > request.maxTeamSize()) {
                    trimmed = trimmed.subList(0, request.maxTeamSize());
                }

                List<Long> consistent = findMutualCore(trimmed, matrix, windowSize, request);
                if (consistent.size() >= request.minTeamSize() && consistent.size() <= request.maxTeamSize()) {
                    List<StudentSurveyProfile> group = mapIdsToStudents(consistent, remaining);
                    if (isValidGroup(group, request, alreadyAssigned)) {
                        return group;
                    }
                }
            }
        }

        return null;
    }

    private List<Long> findMutualCore(
            List<Long> candidateIds,
            PairScoreMatrix matrix,
            int windowSize,
            GroupingRequest request) {

        List<Long> mutual = new ArrayList<>();

        for (Long candidate : candidateIds) {
            boolean presentInAll = true;

            for (Long other : candidateIds) {
                if (Objects.equals(candidate, other)) {
                    continue;
                }

                List<Long> otherWindow = matrix.topMatches(other, windowSize - 1);
                if (!otherWindow.contains(candidate)) {
                    presentInAll = false;
                    break;
                }
            }

            if (presentInAll) {
                mutual.add(candidate);
            }
        }

        if (mutual.size() > request.maxTeamSize()) {
            return mutual.subList(0, request.maxTeamSize());
        }
        return mutual;
    }

    private List<StudentSurveyProfile> fallbackBestValidGroup(
            List<StudentSurveyProfile> remaining,
            PairScoreMatrix matrix,
            int targetSize,
            GroupingRequest request,
            Set<Long> alreadyAssigned) {

        StudentSurveyProfile anchor = remaining.get(0);
        List<Long> ranked = matrix.topMatches(anchor.studentId(), remaining.size() - 1);

        List<Long> selectedIds = new ArrayList<>();
        selectedIds.add(anchor.studentId());

        for (Long id : ranked) {
            if (selectedIds.size() >= targetSize) {
                break;
            }

            selectedIds.add(id);
            List<StudentSurveyProfile> currentGroup = mapIdsToStudents(selectedIds, remaining);

            if (!isValidPartialGroup(currentGroup, request, alreadyAssigned)) {
                selectedIds.remove(id);
            }
        }

        while (selectedIds.size() < request.minTeamSize() && selectedIds.size() < remaining.size()) {
            for (StudentSurveyProfile student : remaining) {
                if (!selectedIds.contains(student.studentId())) {
                    selectedIds.add(student.studentId());
                    List<StudentSurveyProfile> currentGroup = mapIdsToStudents(selectedIds, remaining);
                    if (isValidPartialGroup(currentGroup, request, alreadyAssigned)) {
                        break;
                    }
                    selectedIds.remove(student.studentId());
                }
            }
        }

        return mapIdsToStudents(selectedIds, remaining);
    }

    private boolean isValidGroup(
            List<StudentSurveyProfile> group,
            GroupingRequest request,
            Set<Long> alreadyAssigned) {

        if (group.size() < request.minTeamSize() || group.size() > request.maxTeamSize()) {
            return false;
        }

        return isValidPartialGroup(group, request, alreadyAssigned);
    }

    private boolean isValidPartialGroup(
            List<StudentSurveyProfile> group,
            GroupingRequest request,
            Set<Long> alreadyAssigned) {

        Set<Long> seen = new HashSet<>();
        for (StudentSurveyProfile student : group) {
            if (!request.enrolledStudentIds().contains(student.studentId())) {
                return false;
            }
            if (alreadyAssigned.contains(student.studentId())) {
                return false;
            }
            if (!seen.add(student.studentId())) {
                return false;
            }
        }

        return true;
    }

    private List<GroupTeam> rebalanceTeams(List<GroupTeam> teams, GroupingRequest request) {
        if (teams.size() <= 1) {
            return teams;
        }

        List<GroupTeam> mutable = new ArrayList<>(teams);

        for (int i = 0; i < mutable.size(); i++) {
            GroupTeam team = mutable.get(i);

            if (team.memberIds().size() < request.minTeamSize()) {
                for (int j = 0; j < mutable.size(); j++) {
                    if (i == j) {
                        continue;
                    }
                    GroupTeam donor = mutable.get(j);
                    if (donor.memberIds().size() > request.preferredGroupSize()) {
                        List<Long> donorMembers = new ArrayList<>(donor.memberIds());
                        Long moved = donorMembers.remove(donorMembers.size() - 1);

                        List<Long> receivingMembers = new ArrayList<>(team.memberIds());
                        receivingMembers.add(moved);

                        mutable.set(j, new GroupTeam(donor.teamNumber(), donorMembers, donorMembers.size()));
                        mutable.set(i, new GroupTeam(team.teamNumber(), receivingMembers, receivingMembers.size()));
                        break;
                    }
                }
            }
        }

        return mutable;
    }

    private int chooseNextTargetSize(int remainingCount, int preferred, int min, int max) {
        if (remainingCount <= max && remainingCount >= min) {
            return remainingCount;
        }

        for (int size = preferred; size <= max; size++) {
            int leftover = remainingCount - size;
            if (leftover == 0 || leftover >= min) {
                return size;
            }
        }

        for (int size = preferred - 1; size >= min; size--) {
            int leftover = remainingCount - size;
            if (leftover == 0 || leftover >= min) {
                return size;
            }
        }

        return Math.min(max, Math.max(min, preferred));
    }

    private List<StudentSurveyProfile> mapIdsToStudents(List<Long> ids, List<StudentSurveyProfile> students) {
        Map<Long, StudentSurveyProfile> byId = students.stream()
                .collect(Collectors.toMap(StudentSurveyProfile::studentId, s -> s));

        List<StudentSurveyProfile> result = new ArrayList<>();
        for (Long id : ids) {
            StudentSurveyProfile profile = byId.get(id);
            if (profile != null) {
                result.add(profile);
            }
        }
        return result;
    }

    private void validateRequest(GroupingRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Grouping request is required.");
        }
        if (request.preferredGroupSize() < 2) {
            throw new IllegalArgumentException("Preferred group size must be at least 2.");
        }
        if (request.minTeamSize() < 2) {
            throw new IllegalArgumentException("Minimum team size must be at least 2.");
        }
        if (request.minTeamSize() > request.preferredGroupSize()) {
            throw new IllegalArgumentException("Minimum team size cannot be greater than preferred group size.");
        }
        if (request.preferredGroupSize() > request.maxTeamSize()) {
            throw new IllegalArgumentException("Preferred group size cannot be greater than maximum team size.");
        }
    }

    private record PairScoreMatrix(Map<Long, Map<Long, Double>> scores) {

        List<Long> topMatches(Long studentId, int count) {
            Map<Long, Double> row = scores.getOrDefault(studentId, Map.of());

            return row.entrySet().stream()
                    .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                    .limit(count)
                    .map(Map.Entry::getKey)
                    .toList();
        }
    }

    public record GroupingRequest(
            int preferredGroupSize,
            int minTeamSize,
            int maxTeamSize,
            boolean groupTopicsSimilarly,
            boolean groupSkillsSimilarly,
            Set<Long> enrolledStudentIds,
            List<StudentSurveyProfile> students) {
    }

    public record StudentSurveyProfile(
            Long studentId,
            boolean participant,
            Set<String> availabilitySlots,
            Set<String> topicChoices,
            Set<String> skillChoices,
            int leadershipConfidence) {
    }

    public record GroupTeam(
            int teamNumber,
            List<Long> memberIds,
            int size) {
    }

    public record GroupingResult(
            List<GroupTeam> teams) {
    }
}