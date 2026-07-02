package com.example.hackathon.domain.folder.repository;

import com.example.hackathon.domain.folder.entity.FolderCard;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FolderCardRepository extends JpaRepository<FolderCard, Long> {

    List<FolderCard> findAllByFolder_Id(Long folderId);

    List<FolderCard> findAllByFolder_IdOrderByAddedAtDescIdDesc(Long folderId);

    Optional<FolderCard> findByFolder_IdAndCollectionCard_Id(Long folderId, Long collectionCardId);

    Optional<FolderCard> findByCollectionCard_Id(Long collectionCardId);

    long countByFolder_Id(Long folderId);

    void deleteAllByFolder_Id(Long folderId);

    void deleteAllByCollectionCard_Id(Long collectionCardId);
}
