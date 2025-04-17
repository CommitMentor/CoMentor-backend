package com.knu.coment.entity;

import com.knu.coment.global.Status;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String description;
    private String role;
    @Enumerated(EnumType.STRING)
    private Status status;
    @LastModifiedDate
    private LocalDateTime updatedAt;

    private Long userId;
    private Long repoId;

    public Project(String description, String role, Status status,  LocalDateTime updatedAt, Long userId, Long repoId) {
        this.description = description;
        this.role = role;
        this.status = status;
        this.updatedAt = updatedAt;
        this.userId = userId;
        this.repoId = repoId;
    }
    public void update(String description, String role, Status status){
        this.description = description;
        this.role = role;
        this.status = status;
    }

}
