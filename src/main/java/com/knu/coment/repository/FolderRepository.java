package com.knu.coment.repository;

import com.knu.coment.entity.Folder;
import com.knu.coment.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FolderRepository extends JpaRepository<Folder, Long>{
    Optional<Folder> findByUserAndFolderName(User user, String folderName);
    List<Folder> findAllByUser(User user);
}
