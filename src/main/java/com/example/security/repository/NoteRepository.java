package com.example.security.repository;

import com.example.security.model.Note;
import com.example.security.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NoteRepository extends JpaRepository<Note, UUID> {

    List<Note> findAllByAuthor(User author);

    Optional<Note> findByIdAndAuthor(UUID id, User author);

}
