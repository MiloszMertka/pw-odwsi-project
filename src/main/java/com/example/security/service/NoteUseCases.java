package com.example.security.service;

import com.example.security.dto.CreateNoteDto;
import com.example.security.dto.NoteDto;
import com.example.security.dto.UpdateNoteDto;
import com.example.security.model.User;

import java.util.List;
import java.util.UUID;

public interface NoteUseCases {

    List<NoteDto> getAllUserNotes(User user);

    NoteDto getNote(UUID id, User user);

    void createNote(CreateNoteDto createNoteDto, User user);

    void updateNote(UUID id, UpdateNoteDto updateNoteDto, User user);

    void deleteNote(UUID id, User user);

}