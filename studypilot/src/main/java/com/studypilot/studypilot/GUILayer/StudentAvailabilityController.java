package com.studypilot.studypilot.GUILayer;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.studypilot.studypilot.BusinessLogicLayer.AvailabilityService;
import com.studypilot.studypilot.BusinessLogicLayer.StudentPortalService;
import com.studypilot.studypilot.DataAccessLayer.CourseTimeSlotRepo;
import com.studypilot.studypilot.DomainModel.Course;
import com.studypilot.studypilot.DomainModel.CourseTimeSlot;

import jakarta.servlet.http.HttpSession;

@Controller
/**
 * Student-facing availability endpoints.
 *
 * Students view professor-published course slots and submit selections that are
 * later summarized on professor team availability pages.
 */
public class StudentAvailabilityController {

    private static final Pattern NON_ALNUM = Pattern.compile("[^a-z0-9]+");

    private final StudentPortalService studentPortalService;
    private final AvailabilityService availabilityService;
    private final CourseTimeSlotRepo courseTimeSlotRepository;

    public StudentAvailabilityController(
            StudentPortalService studentPortalService,
            AvailabilityService availabilityService,
            CourseTimeSlotRepo courseTimeSlotRepository) {
        this.studentPortalService = studentPortalService;
        this.availabilityService = availabilityService;
        this.courseTimeSlotRepository = courseTimeSlotRepository;
    }

    @GetMapping("/student/{courseId}/availability")
    /**
     * Renders the availability page with all published course slots.
     */
    public String showAvailabilityPage(@PathVariable("courseId") String courseId,
            HttpSession session,
            Model model) {

        if (!isStudent(session)) {
            return "redirect:/login";
        }

        Long studentId = (Long) session.getAttribute("userId");
        Course course = studentPortalService.requireStudentEnrollment(studentId, courseId);

        List<CourseTimeSlot> courseSlots = courseTimeSlotRepository.findByCourseId(courseId);
        Set<String> selectedSlots = availabilityService.getStudentAvailabilitySet(studentId, courseId);

        AvailabilityForm form = new AvailabilityForm();
        form.setSelectedSlots(selectedSlots.stream().collect(Collectors.toList()));

        model.addAttribute("course", course);
        model.addAttribute("courseSlug", toSlug(course.getCourseName()));
        model.addAttribute("form", form);
        model.addAttribute("courseSlots", courseSlots);
        model.addAttribute("selectedSlots", selectedSlots);
        model.addAttribute("fullName", session.getAttribute("fullName"));

        return "student_availability_page";
    }

    @PostMapping("/student/{courseId}/availability")
    /**
     * Saves student selections and returns the same page with confirmation.
     */
    public String saveAvailability(@PathVariable("courseId") String courseId,
            @ModelAttribute("form") AvailabilityForm form,
            HttpSession session,
            Model model) {

        if (!isStudent(session)) {
            return "redirect:/login";
        }

        Long studentId = (Long) session.getAttribute("userId");

        try {
            Course course = studentPortalService.requireStudentEnrollment(studentId, courseId);

            availabilityService.saveAvailability(studentId, courseId, form.getSelectedSlots());

            model.addAttribute("course", course);
            model.addAttribute("courseSlug", toSlug(course.getCourseName()));
            model.addAttribute("form", form);
            model.addAttribute("courseSlots", courseTimeSlotRepository.findByCourseId(courseId));
            model.addAttribute("selectedSlots", availabilityService.getStudentAvailabilitySet(studentId, courseId));
            model.addAttribute("success", "Availability saved successfully.");
            model.addAttribute("fullName", session.getAttribute("fullName"));

            return "student_availability_page";
        } catch (IllegalArgumentException ex) {
            Course course = studentPortalService.requireStudentEnrollment(studentId, courseId);

            model.addAttribute("course", course);
            model.addAttribute("courseSlug", toSlug(course.getCourseName()));
            model.addAttribute("form", form);
            model.addAttribute("courseSlots", courseTimeSlotRepository.findByCourseId(courseId));
            model.addAttribute("selectedSlots", availabilityService.getStudentAvailabilitySet(studentId, courseId));
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("fullName", session.getAttribute("fullName"));

            return "student_availability_page";
        }
    }

    private boolean isStudent(HttpSession session) {
        return "STUDENT".equals(session.getAttribute("role"));
    }

    private String toSlug(String input) {
        if (input == null) {
            return "course";
        }
        String lower = input.trim().toLowerCase(Locale.ROOT);
        String normalized = NON_ALNUM.matcher(lower).replaceAll("-");
        String slug = normalized.replaceAll("^-+|-+$", "");
        return slug.isBlank() ? "course" : slug;
    }
}
