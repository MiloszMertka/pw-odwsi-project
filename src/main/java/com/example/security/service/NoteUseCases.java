package com.example.security.service;

import com.example.security.dto.*;
import com.example.security.model.User;

import java.util.List;
import java.util.UUID;

public interface NoteUseCases {

    List<NoteDto> getAllUserNotes(User user);

    List<NoteDto> getAllPublicNotes(User user);

    List<NoteDto> getAllSharedNotes(User user);

    NoteDto getNoteToRead(UUID id, User user);

    NoteDto getNoteToRead(UUID id, User user, GetNoteDto getNoteDto);

    NoteDto getNoteToEdit(UUID id, User user);

    NoteDto getNoteToEdit(UUID id, User user, GetNoteDto getNoteDto);

    void createNote(CreateNoteDto createNoteDto, User user);

    void updateNote(UUID id, UpdateNoteDto updateNoteDto, User user);

    void deleteNote(UUID id, User user);

    void shareNote(UUID id, ShareNoteDto shareNoteDto, User user);

}
