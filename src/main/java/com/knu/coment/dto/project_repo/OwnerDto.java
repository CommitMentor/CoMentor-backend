package com.knu.coment.dto.project_repo;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor
public class OwnerDto {
    private String login;

    public OwnerDto(String login) {
        this.login = login;
    }
}
