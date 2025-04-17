package com.knu.coment.repository;

import com.knu.coment.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FolderRepository extends JpaRepository<Folder, Long>{
    Optional<Folder> findByUserIdAndId(Long userId, Long folderId);
    Optional<Folder> findByUserIdAndFileName(Long userId, String folderName);
    List<Folder> findAllByUserId(Long userId);
    long countByUserIdAndFileNameStartingWith(Long userId, String prefix);
    boolean existsByUserIdAndFileName(Long userId, String folderName);
}
