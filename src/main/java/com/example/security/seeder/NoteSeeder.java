package com.example.security.seeder;

import com.example.security.model.Note;
import com.example.security.model.User;
import com.example.security.repository.NoteRepository;
import com.example.security.repository.UserRepository;
import com.github.javafaker.Faker;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Component
@ConditionalOnProperty(name = "seeders.enabled", havingValue = "true", matchIfMissing = true)
@Transactional
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class NoteSeeder implements Seeder {

    private final Faker faker;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    @Override
    public void seed(int objectsNumber) {
        final Set<Note> notes = HashSet.newHashSet(objectsNumber);
        while (notes.size() < objectsNumber) {
            final var note = createNote();
            notes.add(note);
        }

        noteRepository.saveAll(notes);
    }

    @Override
    public void clean() {
        noteRepository.deleteAll();
    }

    private Note createNote() {
        final var title = faker.lorem().sentence();
        final var content = faker.lorem().paragraph();
        final var author = findRandomUser();
        final var isPublic = faker.bool().bool();
        final var note = new Note(title, content, author, isPublic);
        final var totalUsersCount = Math.toIntExact(userRepository.count());
        final var users = findRandomUsers(faker.random().nextInt(totalUsersCount));
        note.getReaders().addAll(users);
        return note;
    }

    private User findRandomUser() {
        final var users = userRepository.findAll();
        final var randomIndex = faker.random().nextInt(users.size());
        return users.get(randomIndex);
    }

    private Set<User> findRandomUsers(int usersNumber) {
        final var users = userRepository.findAll();
        final Set<User> randomUsers = HashSet.newHashSet(usersNumber);
        while (randomUsers.size() < usersNumber) {
            final var randomIndex = faker.random().nextInt(users.size());
            randomUsers.add(users.get(randomIndex));
        }

        return randomUsers;
    }

}
