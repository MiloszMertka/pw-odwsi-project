package com.example.security.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class HomeController {

    @GetMapping
    String redirectToNotesPage() {
        return "redirect:notes";
    }

}
