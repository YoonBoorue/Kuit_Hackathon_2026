package com.example.hackathon.domain.folder.repository;

import com.example.hackathon.domain.folder.entity.Folder;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FolderRepository extends JpaRepository<Folder, Long> {

    List<Folder> findAllByUser_Id(Long userId);

    Optional<Folder> findByIdAndUser_Id(Long folderId, Long userId);

    boolean existsByUser_IdAndName(Long userId, String name);

    boolean existsByUser_IdAndNameAndIdNot(Long userId, String name, Long folderId);
}
