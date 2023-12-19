package com.example.security.controller;

import com.example.security.dto.ChangePasswordDto;
import com.example.security.dto.CreatePasswordResetTokenDto;
import com.example.security.dto.LoginUserDto;
import com.example.security.dto.RegisterUserDto;
import com.example.security.model.User;
import com.example.security.service.UserUseCases;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class UserController {

    private final UserUseCases userUseCases;

    @GetMapping("/login")
    String showLoginForm(Model model) {
        model.addAttribute("loginUserDto", new LoginUserDto("", "", ""));
        return "login";
    }

    @GetMapping("/register")
    String showRegisterForm(Model model) {
        model.addAttribute("registerUserDto", new RegisterUserDto("", "", "", ""));
        return "register";
    }

    @PostMapping("/register")
    String registerUser(@Valid RegisterUserDto registerUserDto, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            final var qr = userUseCases.registerUser(registerUserDto);
            model.addAttribute("qr", qr);
            return "qr-code";
        } catch (IllegalStateException exception) {
            model.addAttribute("error", exception.getMessage());
            return "register";
        }
    }

    @GetMapping("/last-successful-logins")
    String showLastSuccessfulLoginsPage(Model model, @AuthenticationPrincipal User user) {
        model.addAttribute("lastSuccessfulLogins", userUseCases.getLastSuccessfulLogins(user));
        return "last-successful-logins";
    }

    @GetMapping("/password-reset")
    String showPasswordResetForm(Model model) {
        model.addAttribute("createPasswordResetTokenDto", new CreatePasswordResetTokenDto(""));
        return "password-reset-form";
    }

    @PostMapping("/password-reset")
    String createPasswordResetToken(@Valid CreatePasswordResetTokenDto createPasswordResetTokenDto, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "password-reset-form";
        }

        try {
            userUseCases.createPasswordResetToken(createPasswordResetTokenDto);
            model.addAttribute("success", "Sent email with password reset link to " + createPasswordResetTokenDto.email());
            return "password-reset-form";
        } catch (IllegalStateException exception) {
            model.addAttribute("error", exception.getMessage());
            return "password-reset-form";
        }
    }

    @GetMapping("/change-password")
    String showChangePasswordForm(@RequestParam("token") String token, Model model) {
        if (!userUseCases.isPasswordResetTokenValid(token)) {
            return "redirect:/users/login";
        }

        model.addAttribute("changePasswordDto", new ChangePasswordDto("", "", token));
        return "change-password-form";
    }

    @PostMapping("/change-password")
    String changePassword(@Valid ChangePasswordDto changePasswordDto, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "change-password-form";
        }

        try {
            userUseCases.changePassword(changePasswordDto);
            return "redirect:/users/login";
        } catch (IllegalStateException exception) {
            model.addAttribute("error", exception.getMessage());
            return "change-password-form";
        }
    }

}
