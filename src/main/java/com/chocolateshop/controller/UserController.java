package com.chocolateshop.controller;

import com.chocolateshop.dto.UserDto;
import com.chocolateshop.entity.AppUser;
import com.chocolateshop.entity.Enums;
import com.chocolateshop.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("activeNav", "users");
        return "users/list";
    }

    @GetMapping("/new")
    public String newUserForm(Model model) {
        UserDto dto = new UserDto();
        dto.setStatus(Enums.Status.ACTIVE);
        dto.setRole(Enums.Role.CASHIER);
        model.addAttribute("userDto", dto);
        model.addAttribute("roles", Enums.Role.values());
        model.addAttribute("statuses", Enums.Status.values());
        model.addAttribute("activeNav", "users");
        return "users/form";
    }

    @GetMapping("/edit/{id}")
    public String editUserForm(@PathVariable Long id, Model model) {
        AppUser user = userService.getUserById(id);
        UserDto dto = UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .status(user.getStatus())
                .build();

        model.addAttribute("userDto", dto);
        model.addAttribute("roles", Enums.Role.values());
        model.addAttribute("statuses", Enums.Status.values());
        model.addAttribute("activeNav", "users");
        return "users/form";
    }

    @PostMapping("/save")
    public String saveUser(@Valid @ModelAttribute("userDto") UserDto dto,
                           BindingResult result,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("roles", Enums.Role.values());
            model.addAttribute("statuses", Enums.Status.values());
            model.addAttribute("activeNav", "users");
            return "users/form";
        }

        try {
            if (dto.getId() == null) {
                userService.createUser(dto);
                redirectAttributes.addFlashAttribute("successMessage", "User account created successfully: " + dto.getUsername());
            } else {
                userService.updateUser(dto.getId(), dto);
                redirectAttributes.addFlashAttribute("successMessage", "User account updated successfully: " + dto.getUsername());
            }
            return "redirect:/users";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("roles", Enums.Role.values());
            model.addAttribute("statuses", Enums.Status.values());
            model.addAttribute("activeNav", "users");
            return "users/form";
        }
    }

    @PostMapping("/toggle/{id}")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.toggleStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "User status updated");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/users";
    }
}
