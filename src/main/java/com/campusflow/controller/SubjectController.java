package com.campusflow.controller;

import com.campusflow.dto.SubjectDto;
import com.campusflow.entity.Assignment;
import com.campusflow.entity.Subject;
import com.campusflow.entity.User;
import com.campusflow.exception.SubjectConflictException;
import com.campusflow.service.AssignmentService;
import com.campusflow.service.SecurityService;
import com.campusflow.service.SubjectService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/subjects")
public class SubjectController {

    private final SubjectService subjectService;
    private final AssignmentService assignmentService;
    private final SecurityService securityService;

    public SubjectController(SubjectService subjectService,
                             AssignmentService assignmentService,
                             SecurityService securityService) {
        this.subjectService = subjectService;
        this.assignmentService = assignmentService;
        this.securityService = securityService;
    }

    @GetMapping
    public String listSubjects(Model model) {
        User user = securityService.getCurrentUser();
        List<SubjectDto> subjects = subjectService.getSubjectsForUser(user.getId());

        model.addAttribute("subjects", subjects);
        model.addAttribute("activeNav", "subjects");

        return "subjects/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("subjectDto", new SubjectDto());
        model.addAttribute("isEdit", false);
        model.addAttribute("activeNav", "subjects");
        return "subjects/form";
    }

    @PostMapping
    public String createSubject(@Valid @ModelAttribute("subjectDto") SubjectDto subjectDto,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        User user = securityService.getCurrentUser();

        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", false);
            model.addAttribute("activeNav", "subjects");
            return "subjects/form";
        }

        try {
            subjectService.createSubject(subjectDto, user.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Subject '" + subjectDto.getName() + "' created successfully!");
            return "redirect:/subjects";
        } catch (SubjectConflictException e) {
            bindingResult.rejectValue("name", "error.subjectDto", e.getMessage());
            model.addAttribute("isEdit", false);
            model.addAttribute("activeNav", "subjects");
            return "subjects/form";
        }
    }

    @GetMapping("/{id}")
    public String viewSubjectDetails(@PathVariable("id") Long id, Model model) {
        User user = securityService.getCurrentUser();
        Subject subject = subjectService.getSubjectById(id, user.getId());
        List<Assignment> assignments = assignmentService.getAssignmentsForSubject(id, user.getId());

        model.addAttribute("subject", subject);
        model.addAttribute("assignments", assignments);
        model.addAttribute("activeNav", "subjects");

        return "subjects/detail";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        User user = securityService.getCurrentUser();
        SubjectDto subjectDto = subjectService.getSubjectDtoById(id, user.getId());

        model.addAttribute("subjectDto", subjectDto);
        model.addAttribute("isEdit", true);
        model.addAttribute("activeNav", "subjects");

        return "subjects/form";
    }

    @PostMapping("/{id}/edit")
    public String updateSubject(@PathVariable("id") Long id,
                                @Valid @ModelAttribute("subjectDto") SubjectDto subjectDto,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        User user = securityService.getCurrentUser();

        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", true);
            model.addAttribute("activeNav", "subjects");
            return "subjects/form";
        }

        try {
            subjectService.updateSubject(id, subjectDto, user.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Subject updated successfully!");
            return "redirect:/subjects";
        } catch (SubjectConflictException e) {
            bindingResult.rejectValue("name", "error.subjectDto", e.getMessage());
            model.addAttribute("isEdit", true);
            model.addAttribute("activeNav", "subjects");
            return "subjects/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteSubject(@PathVariable("id") Long id,
                                RedirectAttributes redirectAttributes) {
        User user = securityService.getCurrentUser();
        try {
            subjectService.deleteSubject(id, user.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Subject deleted successfully.");
        } catch (SubjectConflictException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/subjects";
    }
}
