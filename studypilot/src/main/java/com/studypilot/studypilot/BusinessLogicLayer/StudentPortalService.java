package com.studypilot.studypilot.BusinessLogicLayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.studypilot.studypilot.DataAccessLayer.CourseEnrollmentRepo;
import com.studypilot.studypilot.DataAccessLayer.CourseRepo;
import com.studypilot.studypilot.DataAccessLayer.GroupFormationActivityRepo;
import com.studypilot.studypilot.DataAccessLayer.GroupFormationSkillOptionRepo;
import com.studypilot.studypilot.DataAccessLayer.GroupFormationTopicOptionRepo;
import com.studypilot.studypilot.DataAccessLayer.StudentGroupPreferenceRepo;
import com.studypilot.studypilot.DomainModel.Course;
import com.studypilot.studypilot.DomainModel.CourseEnrollment;
import com.studypilot.studypilot.DomainModel.GroupFormationActivity;
import com.studypilot.studypilot.DomainModel.GroupFormationSkillOption;
import com.studypilot.studypilot.DomainModel.GroupFormationTopicOption;
import com.studypilot.studypilot.DomainModel.StudentGroupPreference;

@Service
/**
 * StudentPortalService component.
 */
public class StudentPortalService {

    private final CourseRepo courseRepo;
    private final CourseEnrollmentRepo courseEnrollmentRepo;
    private final GroupFormationActivityRepo groupFormationActivityRepo;
    private final GroupFormationTopicOptionRepo groupFormationTopicOptionRepo;
    private final GroupFormationSkillOptionRepo groupFormationSkillOptionRepo;
    private final StudentGroupPreferenceRepo studentGroupPreferenceRepo;
    private final GroupFormationAlgorithmService groupFormationAlgorithmService;

    public StudentPortalService(CourseRepo courseRepo,
            CourseEnrollmentRepo courseEnrollmentRepo,
            GroupFormationActivityRepo groupFormationActivityRepo,
            GroupFormationTopicOptionRepo groupFormationTopicOptionRepo,
            GroupFormationSkillOptionRepo groupFormationSkillOptionRepo,
            StudentGroupPreferenceRepo studentGroupPreferenceRepo,
            GroupFormationAlgorithmService groupFormationAlgorithmService) {
        this.courseRepo = courseRepo;
        this.courseEnrollmentRepo = courseEnrollmentRepo;
        this.groupFormationActivityRepo = groupFormationActivityRepo;
        this.groupFormationTopicOptionRepo = groupFormationTopicOptionRepo;
        this.groupFormationSkillOptionRepo = groupFormationSkillOptionRepo;
        this.studentGroupPreferenceRepo = studentGroupPreferenceRepo;
        this.groupFormationAlgorithmService = groupFormationAlgorithmService;
    }

    public List<Course> getStudentCourses(Long studentId) {
        requireStudent(studentId);

        List<CourseEnrollment> enrollments = courseEnrollmentRepo.findByStudentIdOrderByCreatedAtDesc(studentId);
        List<Course> courses = new ArrayList<>();

        for (CourseEnrollment enrollment : enrollments) {
            courseRepo.findById(enrollment.getCourseId()).ifPresent(courses::add);
        }
        return courses;
    }

    public List<Course> getCoursesAvailableToJoin(Long studentId) {
        requireStudent(studentId);

        List<Course> allCourses = courseRepo.findAllByOrderByCreatedAtDesc();
        Set<String> enrolledCourseIds = new HashSet<>();
        for (CourseEnrollment enrollment : courseEnrollmentRepo.findByStudentIdOrderByCreatedAtDesc(studentId)) {
            enrolledCourseIds.add(enrollment.getCourseId());
        }

        List<Course> available = new ArrayList<>();
        for (Course course : allCourses) {
            if (!enrolledCourseIds.contains(course.getId())) {
                available.add(course);
            }
        }
        return available;
    }

    @Transactional
    public void enrollStudentInCourseByJoinCode(Long studentId, String joinCode) {
        requireStudent(studentId);

        String normalizedJoinCode = clean(joinCode).toUpperCase();
        if (normalizedJoinCode.length() != 8) {
            throw new IllegalArgumentException("Course ID must be exactly 8 characters.");
        }

        Course course = courseRepo.findByJoinCode(normalizedJoinCode)
                .orElseThrow(() -> new IllegalArgumentException("Course not found for that Course ID."));

        if (courseEnrollmentRepo.existsByCourseIdAndStudentId(course.getId(), studentId)) {
            return;
        }

        courseEnrollmentRepo.save(new CourseEnrollment(course.getId(), studentId));
    }

    @Transactional
    public void enrollStudentInCourse(Long studentId, String courseId) {
        requireStudent(studentId);

        Course course = courseRepo.findById(clean(courseId))
                .orElseThrow(() -> new IllegalArgumentException("Course not found."));

        if (courseEnrollmentRepo.existsByCourseIdAndStudentId(course.getId(), studentId)) {
            return;
        }

        courseEnrollmentRepo.save(new CourseEnrollment(course.getId(), studentId));
    }

    public Course requireStudentEnrollment(Long studentId, String courseId) {
        requireStudent(studentId);
        String cleanCourseId = clean(courseId);

        Course course = courseRepo.findById(cleanCourseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found."));

        if (!courseEnrollmentRepo.existsByCourseIdAndStudentId(cleanCourseId, studentId)) {
            throw new IllegalArgumentException("You are not enrolled in this course.");
        }
        return course;
    }

    public Optional<GroupFormationActivity> getLatestGroupActivityForCourse(String courseId) {
        List<GroupFormationActivity> activities = groupFormationActivityRepo.findByCourseIdOrderByCreatedAtDesc(courseId);
        if (activities.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(activities.get(0));
    }

    public List<GroupFormationTopicOption> getTopicOptions(Long activityId) {
        return groupFormationTopicOptionRepo.findByActivityIdOrderByOptionOrderAsc(activityId);
    }

    public List<GroupFormationSkillOption> getSkillOptions(Long activityId) {
        return groupFormationSkillOptionRepo.findByActivityIdOrderByOptionOrderAsc(activityId);
    }

    public Optional<StudentGroupPreference> getStudentPreference(Long activityId, Long studentId) {
        if (activityId == null || studentId == null) {
            return Optional.empty();
        }
        return studentGroupPreferenceRepo.findByActivityIdAndStudentId(activityId, studentId);
    }

    public StudentTeamSnapshot getStudentTeamForCourse(Long studentId, String courseId) {
        requireStudent(studentId);
        Course course = requireStudentEnrollment(studentId, courseId);

        Optional<GroupFormationActivity> maybeActivity = getLatestGroupActivityForCourse(course.getId());
        if (maybeActivity.isEmpty()) {
            return null;
        }

        GroupFormationActivity activity = maybeActivity.get();
        List<CourseEnrollment> enrollments = courseEnrollmentRepo.findByCourseId(course.getId());
        if (enrollments.isEmpty()) {
            return null;
        }

        Set<Long> enrolledStudentIds = enrollments.stream()
                .map(CourseEnrollment::getStudentId)
                .collect(HashSet::new, Set::add, Set::addAll);

        Map<Long, StudentGroupPreference> preferenceByStudentId = new HashMap<>();
        for (StudentGroupPreference preference : studentGroupPreferenceRepo.findByActivityId(activity.getId())) {
            preferenceByStudentId.put(preference.getStudentId(), preference);
        }

        List<GroupFormationAlgorithmService.StudentSurveyProfile> profiles = new ArrayList<>();
        for (Long enrolledStudentId : enrolledStudentIds) {
            StudentGroupPreference preference = preferenceByStudentId.get(enrolledStudentId);
            if (preference == null) {
                profiles.add(new GroupFormationAlgorithmService.StudentSurveyProfile(
                        enrolledStudentId,
                        false,
                        Set.of("unspecified"),
                        Set.of(),
                        Set.of(),
                        3));
                continue;
            }

            profiles.add(new GroupFormationAlgorithmService.StudentSurveyProfile(
                    enrolledStudentId,
                    true,
                    Set.of("unspecified"),
                    Set.of(preference.getTopicChoice()),
                    Set.of(preference.getSkillChoice()),
                    3));
        }

        GroupFormationAlgorithmService.GroupingRequest request = new GroupFormationAlgorithmService.GroupingRequest(
                activity.getPreferredGroupSize(),
                activity.getMinTeamSize(),
                activity.getMaxTeamSize(),
                activity.isGroupTopicsSimilarly(),
                activity.isGroupSkillsSimilarly(),
                enrolledStudentIds,
                profiles);

        GroupFormationAlgorithmService.GroupingResult result = groupFormationAlgorithmService.generateGroups(request,
                courseId);
        for (GroupFormationAlgorithmService.GroupTeam team : result.teams()) {
            if (team.memberIds().contains(studentId)) {
                return new StudentTeamSnapshot(team.teamNumber(), team.memberIds(), activity.getActivityName());
            }
        }

        return null;
    }

    @Transactional
    public StudentGroupPreference saveGroupPreference(Long studentId,
            String courseId,
            Long activityId,
            String topicChoice,
            String skillChoice,
            String notes) {
        requireStudent(studentId);
        Course course = requireStudentEnrollment(studentId, courseId);

        GroupFormationActivity activity = groupFormationActivityRepo.findByIdAndCourseId(activityId, course.getId())
                .orElseThrow(() -> new IllegalArgumentException("Group formation activity not found."));

        List<GroupFormationTopicOption> topics = getTopicOptions(activity.getId());
        List<GroupFormationSkillOption> skills = getSkillOptions(activity.getId());

        String cleanTopic = clean(topicChoice);
        String cleanSkill = clean(skillChoice);
        String cleanNotes = notes == null ? "" : notes.trim();

        boolean topicExists = topics.stream().anyMatch(t -> t.getTopicText().equals(cleanTopic));
        boolean skillExists = skills.stream().anyMatch(s -> s.getSkillText().equals(cleanSkill));

        if (!topicExists) {
            throw new IllegalArgumentException("Please select a valid topic preference.");
        }
        if (!skillExists) {
            throw new IllegalArgumentException("Please select a valid skill preference.");
        }

        Optional<StudentGroupPreference> existing = studentGroupPreferenceRepo.findByActivityIdAndStudentId(activityId, studentId);
        if (existing.isPresent()) {
            StudentGroupPreference preference = existing.get();
            preference.setTopicChoice(cleanTopic);
            preference.setSkillChoice(cleanSkill);
            preference.setNotes(cleanNotes);
            return studentGroupPreferenceRepo.save(preference);
        }

        StudentGroupPreference preference = new StudentGroupPreference(
                activityId,
                course.getId(),
                studentId,
                cleanTopic,
                cleanSkill,
                cleanNotes
        );
        return studentGroupPreferenceRepo.save(preference);
    }

    private void requireStudent(Long studentId) {
        if (studentId == null) {
            throw new IllegalArgumentException("Student must be logged in.");
        }
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    public record StudentTeamSnapshot(int teamNumber, List<Long> memberIds, String activityName) {

    }
}
