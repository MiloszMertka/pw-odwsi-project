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
import org.springframework.web.bind.annotation.*;

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
    String createNote(@ModelAttribute @Valid CreateNoteDto createNoteDto, @AuthenticationPrincipal User user, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "create-note-form";
        }

        noteUseCases.createNote(createNoteDto, user);
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
    String updateNote(@PathVariable UUID id, @ModelAttribute @Valid UpdateNoteDto updateNoteDto, @AuthenticationPrincipal User user, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "edit-note-form";
        }

        noteUseCases.updateNote(id, updateNoteDto, user);
        return "redirect:/notes";
    }

    @GetMapping("/delete/{id}")
    String deleteNote(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        noteUseCases.deleteNote(id, user);
        return "redirect:/notes";
    }

}
