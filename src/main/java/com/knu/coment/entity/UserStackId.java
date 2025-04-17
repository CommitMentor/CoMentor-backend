package com.knu.coment.entity;

import com.knu.coment.global.Stack;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStackId implements Serializable {
    private Long userId;
    private Stack stack;
}

