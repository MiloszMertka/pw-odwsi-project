package com.example.security.service.internal;

import com.example.security.dto.CreateNoteDto;
import com.example.security.dto.NoteDto;
import com.example.security.dto.UpdateNoteDto;
import com.example.security.model.Note;
import com.example.security.model.User;
import com.example.security.repository.NoteRepository;
import com.example.security.service.NoteUseCases;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.owasp.html.Sanitizers;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class NoteService implements NoteUseCases {

    private static final String USER_NOT_AUTHORIZED_MESSAGE = "User is not authorized to perform this action";
    private final NoteRepository noteRepository;

    @Override
    public List<NoteDto> getAllUserNotes(@NonNull User user) {
        return noteRepository.findAllByAuthor(user).stream()
                .map(this::mapNoteToNoteDto)
                .toList();
    }

    @Override
    public List<NoteDto> getAllPublicNotes(@NonNull User user) {
        return noteRepository.findAllByIsPublicIsTrue().stream()
                .filter(note -> !note.getAuthor().equals(user))
                .map(this::mapNoteToNoteDto)
                .toList();
    }

    @Override
    public NoteDto getNoteToRead(@NonNull UUID id, @NonNull User user) {
        final var note = noteRepository.findByIdAndAuthorOrIsPublicIsTrue(id, user).orElseThrow();
        return mapNoteToNoteDto(note);
    }

    @Override
    public NoteDto getNoteToEdit(@NonNull UUID id, @NonNull User user) {
        final var note = noteRepository.findByIdAndAuthor(id, user).orElseThrow();
        return mapNoteToNoteDto(note);
    }

    @Override
    public void createNote(@NonNull CreateNoteDto createNoteDto, @NonNull User user) {
        final var sanitizedContent = sanitizeHtmlContent(createNoteDto.content());
        final var note = new Note(createNoteDto.title(), sanitizedContent, user, createNoteDto.isPublic());
        noteRepository.save(note);
    }

    @Override
    public void updateNote(@NonNull UUID id, @NonNull UpdateNoteDto updateNoteDto, @NonNull User user) {
        final var note = noteRepository.findById(id).orElseThrow();
        validateUserIsNoteAuthor(user, note);
        final var sanitizedContent = sanitizeHtmlContent(updateNoteDto.content());
        note.setTitle(updateNoteDto.title());
        note.setContent(sanitizedContent);
        note.setIsPublic(updateNoteDto.isPublic());
        noteRepository.save(note);
    }

    @Override
    public void deleteNote(@NonNull UUID id, @NonNull User user) {
        final var note = noteRepository.findById(id).orElseThrow();
        validateUserIsNoteAuthor(user, note);
        noteRepository.delete(note);
    }

    private NoteDto mapNoteToNoteDto(Note note) {
        return new NoteDto(note.getId(), note.getTitle(), note.getContent(), note.getIsPublic(), note.getAuthor().getUsername());
    }

    private void validateUserIsNoteAuthor(User user, Note note) {
        if (!note.getAuthor().equals(user)) {
            throw new IllegalStateException(USER_NOT_AUTHORIZED_MESSAGE);
        }
    }

    private String sanitizeHtmlContent(String content) {
        final var policyFactory = Sanitizers.FORMATTING.and(Sanitizers.LINKS).and(Sanitizers.IMAGES).and(Sanitizers.BLOCKS);
        return policyFactory.sanitize(content);
    }

}
