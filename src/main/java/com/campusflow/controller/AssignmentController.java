package com.campusflow.controller;

import com.campusflow.dto.AssignmentDto;
import com.campusflow.dto.AssignmentFilterDto;
import com.campusflow.entity.Assignment;
import com.campusflow.entity.AssignmentStatus;
import com.campusflow.entity.Priority;
import com.campusflow.entity.Subject;
import com.campusflow.entity.User;
import com.campusflow.service.AssignmentService;
import com.campusflow.service.SecurityService;
import com.campusflow.service.SubjectService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/assignments")
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final SubjectService subjectService;
    private final SecurityService securityService;

    public AssignmentController(AssignmentService assignmentService,
                                SubjectService subjectService,
                                SecurityService securityService) {
        this.assignmentService = assignmentService;
        this.subjectService = subjectService;
        this.securityService = securityService;
    }

    @GetMapping
    public String listAssignments(@ModelAttribute("filter") AssignmentFilterDto filter, Model model) {
        User user = securityService.getCurrentUser();
        Page<Assignment> assignmentsPage = assignmentService.getAssignments(user.getId(), filter);
        List<Subject> subjects = subjectService.getSubjectEntitiesForUser(user.getId());

        model.addAttribute("assignmentsPage", assignmentsPage);
        model.addAttribute("subjects", subjects);
        model.addAttribute("filter", filter);
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("statuses", AssignmentStatus.values());
        model.addAttribute("activeNav", "assignments");

        return "assignments/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        User user = securityService.getCurrentUser();
        List<Subject> subjects = subjectService.getSubjectEntitiesForUser(user.getId());

        if (subjects.isEmpty()) {
            model.addAttribute("noSubjectsWarning", true);
        }

        AssignmentDto assignmentDto = new AssignmentDto();
        assignmentDto.setDueDateTime(LocalDateTime.now().plusDays(3).withHour(23).withMinute(59));

        model.addAttribute("assignmentDto", assignmentDto);
        model.addAttribute("subjects", subjects);
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("statuses", AssignmentStatus.values());
        model.addAttribute("isEdit", false);
        model.addAttribute("activeNav", "assignments");

        return "assignments/form";
    }

    @PostMapping
    public String createAssignment(@Valid @ModelAttribute("assignmentDto") AssignmentDto assignmentDto,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes,
                                   Model model) {
        User user = securityService.getCurrentUser();

        if (bindingResult.hasErrors()) {
            List<Subject> subjects = subjectService.getSubjectEntitiesForUser(user.getId());
            model.addAttribute("subjects", subjects);
            model.addAttribute("priorities", Priority.values());
            model.addAttribute("statuses", AssignmentStatus.values());
            model.addAttribute("isEdit", false);
            model.addAttribute("activeNav", "assignments");
            return "assignments/form";
        }

        assignmentService.createAssignment(assignmentDto, user.getId());
        redirectAttributes.addFlashAttribute("successMessage", "Assignment '" + assignmentDto.getTitle() + "' created successfully!");
        return "redirect:/assignments";
    }

    @GetMapping("/{id}")
    public String viewAssignmentDetails(@PathVariable("id") Long id, Model model) {
        User user = securityService.getCurrentUser();
        Assignment assignment = assignmentService.getAssignmentById(id, user.getId());

        model.addAttribute("assignment", assignment);
        model.addAttribute("activeNav", "assignments");

        return "assignments/detail";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        User user = securityService.getCurrentUser();
        AssignmentDto assignmentDto = assignmentService.getAssignmentDtoById(id, user.getId());
        List<Subject> subjects = subjectService.getSubjectEntitiesForUser(user.getId());

        model.addAttribute("assignmentDto", assignmentDto);
        model.addAttribute("subjects", subjects);
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("statuses", AssignmentStatus.values());
        model.addAttribute("isEdit", true);
        model.addAttribute("activeNav", "assignments");

        return "assignments/form";
    }

    @PostMapping("/{id}/edit")
    public String updateAssignment(@PathVariable("id") Long id,
                                   @Valid @ModelAttribute("assignmentDto") AssignmentDto assignmentDto,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes,
                                   Model model) {
        User user = securityService.getCurrentUser();

        if (bindingResult.hasErrors()) {
            List<Subject> subjects = subjectService.getSubjectEntitiesForUser(user.getId());
            model.addAttribute("subjects", subjects);
            model.addAttribute("priorities", Priority.values());
            model.addAttribute("statuses", AssignmentStatus.values());
            model.addAttribute("isEdit", true);
            model.addAttribute("activeNav", "assignments");
            return "assignments/form";
        }

        assignmentService.updateAssignment(id, assignmentDto, user.getId());
        redirectAttributes.addFlashAttribute("successMessage", "Assignment updated successfully!");
        return "redirect:/assignments/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteAssignment(@PathVariable("id") Long id,
                                   RedirectAttributes redirectAttributes) {
        User user = securityService.getCurrentUser();
        assignmentService.deleteAssignment(id, user.getId());
        redirectAttributes.addFlashAttribute("successMessage", "Assignment deleted successfully.");
        return "redirect:/assignments";
    }

    @PostMapping("/{id}/toggle-complete")
    public String toggleCompletion(@PathVariable("id") Long id,
                                   @RequestHeader(value = "Referer", required = false) String referer,
                                   RedirectAttributes redirectAttributes) {
        User user = securityService.getCurrentUser();
        Assignment assignment = assignmentService.toggleAssignmentCompletion(id, user.getId());

        String statusMsg = assignment.getStatus() == AssignmentStatus.COMPLETED
                ? "Assignment marked as completed! Great job!"
                : "Assignment status updated to pending.";

        redirectAttributes.addFlashAttribute("successMessage", statusMsg);

        if (referer != null && !referer.isBlank() && !referer.contains("/login")) {
            return "redirect:" + referer;
        }
        return "redirect:/assignments";
    }
}
