package com.example.security.controller;

import com.example.security.dto.CreateNoteDto;
import com.example.security.dto.UpdateNoteDto;
import com.example.security.model.User;
import com.example.security.service.NoteUseCases;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Controller
@RequestMapping("/notes")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class NoteController {

    private final NoteUseCases noteUseCases;

    @GetMapping
    String showNotesPage(Model model, @AuthenticationPrincipal User user) {
        final var notes = noteUseCases.getAllUserNotes(user);
        model.addAttribute("notes", notes);
        return "notes";
    }

    @GetMapping("/read/{id}")
    String showNotePage(@PathVariable UUID id, Model model, @AuthenticationPrincipal User user) {
        final var note = noteUseCases.getNote(id, user);
        model.addAttribute("note", note);
        return "note";
    }

    @GetMapping("/create")
    String showCreateNoteForm(Model model) {
        model.addAttribute("createNoteDto", new CreateNoteDto("", ""));
        return "create-note-form";
    }

    @PostMapping("/create")
    String createNote(@Valid CreateNoteDto createNoteDto, BindingResult bindingResult, @AuthenticationPrincipal User user, Model model) {
        if (bindingResult.hasErrors()) {
            return "create-note-form";
        }

        try {
            noteUseCases.createNote(createNoteDto, user);
        } catch (IllegalStateException exception) {
            model.addAttribute("error", exception.getMessage());
            return "create-note-form";
        }

        return "redirect:/notes";
    }

    @GetMapping("/edit/{id}")
    String showEditNoteForm(@PathVariable UUID id, Model model, @AuthenticationPrincipal User user) {
        final var note = noteUseCases.getNote(id, user);
        model.addAttribute("updateNoteDto", new UpdateNoteDto(note.title(), note.content()));
        model.addAttribute("noteId", id);
        return "edit-note-form";
    }

    @PostMapping("/edit/{id}")
    String updateNote(@PathVariable UUID id, @Valid UpdateNoteDto updateNoteDto, BindingResult bindingResult, @AuthenticationPrincipal User user, Model model) {
        if (bindingResult.hasErrors()) {
            return "edit-note-form";
        }

        try {
            noteUseCases.updateNote(id, updateNoteDto, user);
        } catch (IllegalStateException exception) {
            model.addAttribute("error", exception.getMessage());
            return "edit-note-form";
        }

        return "redirect:/notes";
    }

    @GetMapping("/delete/{id}")
    String deleteNote(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        noteUseCases.deleteNote(id, user);
        return "redirect:/notes";
    }

}
