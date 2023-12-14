package com.example.security.controller;

import com.example.security.dto.CreateNoteDto;
import com.example.security.dto.ShareNoteDto;
import com.example.security.dto.UpdateNoteDto;
import com.example.security.model.User;
import com.example.security.service.NoteUseCases;
import com.example.security.service.UserUseCases;
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
    private final UserUseCases userUseCases;

    @GetMapping
    String showNotesPage(Model model, @AuthenticationPrincipal User user) {
        final var userNotes = noteUseCases.getAllUserNotes(user);
        final var sharedNotes = noteUseCases.getAllSharedNotes(user);
        final var publicNotes = noteUseCases.getAllPublicNotes(user);
        model.addAttribute("userNotes", userNotes);
        model.addAttribute("sharedNotes", sharedNotes);
        model.addAttribute("publicNotes", publicNotes);
        return "notes";
    }

    @GetMapping("/read/{id}")
    String showNotePage(@PathVariable UUID id, Model model, @AuthenticationPrincipal User user) {
        final var note = noteUseCases.getNoteToRead(id, user);
        model.addAttribute("note", note);
        return "note";
    }

    @GetMapping("/create")
    String showCreateNoteForm(Model model) {
        model.addAttribute("createNoteDto", new CreateNoteDto("", "", false));
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
        final var note = noteUseCases.getNoteToEdit(id, user);
        model.addAttribute("updateNoteDto", new UpdateNoteDto(note.title(), note.content(), note.isPublic()));
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

    @GetMapping("/share/{id}")
    String showShareNoteForm(@PathVariable UUID id, Model model, @AuthenticationPrincipal User user) {
        final var note = noteUseCases.getNoteToEdit(id, user);
        final var otherUsers = userUseCases.getAllOtherUsers(user);
        model.addAttribute("shareNoteDto", new ShareNoteDto(note.readers()));
        model.addAttribute("users", otherUsers);
        model.addAttribute("noteId", id);
        return "share-note-form";
    }

    @PostMapping("/share/{id}")
    String shareNote(@PathVariable UUID id, @Valid ShareNoteDto shareNoteDto, BindingResult bindingResult, @AuthenticationPrincipal User user, Model model) {
        if (bindingResult.hasErrors()) {
            return "share-note-form";
        }

        try {
            noteUseCases.shareNote(id, shareNoteDto, user);
        } catch (IllegalStateException exception) {
            model.addAttribute("error", exception.getMessage());
            return "share-note-form";
        }

        return "redirect:/notes";
    }

}
