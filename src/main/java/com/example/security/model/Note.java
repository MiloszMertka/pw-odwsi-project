package com.example.security.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.lang.NonNull;

import java.util.UUID;

@Entity
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Setter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 5000)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private String content;

    @Column(nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Boolean isPublic;

    @ManyToOne(optional = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @Setter(AccessLevel.NONE)
    private User author;

    public Note(@NonNull String title, @NonNull String content, @NonNull User author, @NonNull Boolean isPublic) {
        this.title = title;
        this.content = content;
        this.author = author;
        this.isPublic = isPublic;
    }

}
