package com.knu.coment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FolderListDto {
    private Long folderId;
    private String fileName;
}
