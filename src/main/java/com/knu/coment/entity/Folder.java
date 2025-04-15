package com.knu.coment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Folder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String folderName;

    @OneToMany(mappedBy = "folder", cascade = CascadeType.PERSIST)
    private Set<CsQuestion> questions = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Folder(User user) {
        this.user = user;
        this.folderName = "default";
    }
    public Folder(String folderName, User user) {
        this.folderName = folderName;
        this.user = user;
    }

}
