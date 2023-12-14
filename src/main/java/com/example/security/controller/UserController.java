package com.example.security.controller;

import com.example.security.dto.LoginUserDto;
import com.example.security.dto.RegisterUserDto;
import com.example.security.service.UserUseCases;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class UserController {
    
    private final UserUseCases userUseCases;

    @GetMapping("/login")
    String showLoginForm(Model model) {
        model.addAttribute("loginUserDto", new LoginUserDto("", ""));
        return "login";
    }

    @GetMapping("/register")
    String showRegisterForm(Model model) {
        model.addAttribute("registerUserDto", new RegisterUserDto("", "", ""));
        return "register";
    }

    @PostMapping("/register")
    String registerUser(@ModelAttribute @Valid RegisterUserDto registerUserDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        userUseCases.registerUser(registerUserDto);
        return "redirect:/users/login";
    }

}
