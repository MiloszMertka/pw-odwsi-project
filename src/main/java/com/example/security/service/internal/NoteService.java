package com.example.security.service.internal;

import com.example.security.dto.*;
import com.example.security.model.Note;
import com.example.security.model.User;
import com.example.security.repository.NoteRepository;
import com.example.security.repository.UserRepository;
import com.example.security.service.NoteUseCases;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.owasp.html.Sanitizers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class NoteService implements NoteUseCases {

    private static final String USER_NOT_AUTHORIZED_MESSAGE = "User is not authorized to perform this action";
    private static final String CANNOT_SHARE_ENCRYPTED_NOTE_MESSAGE = "Cannot share encrypted note";
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final NoteEncryptionService noteEncryptionService;

    @Override
    @Transactional(readOnly = true)
    public List<NoteDto> getAllUserNotes(@NonNull User user) {
        return noteRepository.findAllByAuthor(user).stream()
                .map(this::mapNoteToNoteDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoteDto> getAllPublicNotes(@NonNull User user) {
        return noteRepository.findAllByIsPublicIsTrue().stream()
                .filter(note -> !note.getAuthor().equals(user))
                .map(this::mapNoteToNoteDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoteDto> getAllSharedNotes(@NonNull User user) {
        return noteRepository.findAllByReadersContains(user).stream()
                .filter(note -> !note.getAuthor().equals(user))
                .map(this::mapNoteToNoteDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public NoteDto getNoteToRead(@NonNull UUID id, @NonNull User user) {
        final var note = noteRepository.findNoteWithReadAccess(id, user).orElseThrow();
        return mapNoteToNoteDto(note);
    }

    @Override
    public NoteDto getNoteToRead(@NonNull UUID id, @NonNull User user, @NonNull GetNoteDto getNoteDto) {
        final var note = noteRepository.findNoteWithReadAccess(id, user).orElseThrow();
        final var decryptedContent = noteEncryptionService.decrypt(note.getContent(), getNoteDto.password());
        note.setContent(decryptedContent);
        return mapNoteToNoteDto(note);
    }

    @Override
    @Transactional(readOnly = true)
    public NoteDto getNoteToEdit(@NonNull UUID id, @NonNull User user) {
        final var note = noteRepository.findByIdAndAuthor(id, user).orElseThrow();
        return mapNoteToNoteDto(note);
    }

    @Override
    public NoteDto getNoteToEdit(@NonNull UUID id, @NonNull User user, @NonNull GetNoteDto getNoteDto) {
        final var note = noteRepository.findByIdAndAuthor(id, user).orElseThrow();
        final var decryptedContent = noteEncryptionService.decrypt(note.getContent(), getNoteDto.password());
        note.setContent(decryptedContent);
        return mapNoteToNoteDto(note);
    }

    @Override
    @Transactional
    public void createNote(@NonNull CreateNoteDto createNoteDto, @NonNull User user) {
        final var sanitizedContent = sanitizeHtmlContent(createNoteDto.content());
        final var password = createNoteDto.password();
        final var isEncrypted = password != null && !password.isBlank();
        final var content = isEncrypted ? noteEncryptionService.encrypt(sanitizedContent, password) : sanitizedContent;
        final var note = new Note(createNoteDto.title(), content, user, createNoteDto.isPublic(), isEncrypted);
        validateNoteIsNotBothEncryptedAndPublic(note);
        noteRepository.save(note);
    }

    @Override
    @Transactional
    public void updateNote(@NonNull UUID id, @NonNull UpdateNoteDto updateNoteDto, @NonNull User user) {
        final var note = noteRepository.findById(id).orElseThrow();
        validateUserIsNoteAuthor(user, note);
        final var sanitizedContent = sanitizeHtmlContent(updateNoteDto.content());
        final var password = updateNoteDto.password();
        final var isEncrypted = password != null && !password.isBlank();
        final var content = isEncrypted ? noteEncryptionService.encrypt(sanitizedContent, password) : sanitizedContent;
        note.setTitle(updateNoteDto.title());
        note.setContent(content);
        note.setIsPublic(updateNoteDto.isPublic());
        validateNoteIsNotBothEncryptedAndPublic(note);
        noteRepository.save(note);
    }

    @Override
    @Transactional
    public void deleteNote(@NonNull UUID id, @NonNull User user) {
        final var note = noteRepository.findById(id).orElseThrow();
        validateUserIsNoteAuthor(user, note);
        noteRepository.delete(note);
    }

    @Override
    @Transactional
    public void shareNote(@NonNull UUID id, @NonNull ShareNoteDto shareNoteDto, @NonNull User user) {
        final var note = noteRepository.findById(id).orElseThrow();
        validateUserIsNoteAuthor(user, note);
        validateNoteIsNotEncrypted(note);
        final var users = userRepository.findAllByUsernameIsIn(shareNoteDto.usernames().stream().toList());
        users.remove(user);
        note.getReaders().clear();
        note.getReaders().addAll(users);
        noteRepository.save(note);
    }

    private NoteDto mapNoteToNoteDto(Note note) {
        return new NoteDto(note.getId(),
                note.getTitle(),
                note.getContent(),
                note.getIsEncrypted(),
                note.getIsPublic(),
                note.getAuthor().getUsername(),
                note.getReaders().stream().map(User::getUsername).collect(Collectors.toSet()));
    }

    private void validateUserIsNoteAuthor(User user, Note note) {
        if (!note.getAuthor().equals(user)) {
            throw new IllegalStateException(USER_NOT_AUTHORIZED_MESSAGE);
        }
    }

    private void validateNoteIsNotEncrypted(Note note) {
        if (note.getIsEncrypted()) {
            throw new IllegalStateException(CANNOT_SHARE_ENCRYPTED_NOTE_MESSAGE);
        }
    }

    private void validateNoteIsNotBothEncryptedAndPublic(Note note) {
        if (note.getIsEncrypted() && note.getIsPublic()) {
            throw new IllegalStateException(CANNOT_SHARE_ENCRYPTED_NOTE_MESSAGE);
        }
    }

    private String sanitizeHtmlContent(String content) {
        final var policyFactory = Sanitizers.FORMATTING.and(Sanitizers.LINKS).and(Sanitizers.IMAGES).and(Sanitizers.BLOCKS);
        return policyFactory.sanitize(content);
    }

}
