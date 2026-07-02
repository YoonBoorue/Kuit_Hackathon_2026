package com.example.hackathon.domain.collection.repository;

import com.example.hackathon.domain.collection.entity.CollectionCard;
import com.example.hackathon.domain.collection.entity.CollectionSource;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CollectionCardRepository extends JpaRepository<CollectionCard, Long> {

    List<CollectionCard> findAllByUser_Id(Long userId);

    List<CollectionCard> findAllByUser_IdOrderByCollectedAtDescIdDesc(Long userId);

    List<CollectionCard> findAllByUser_IdAndSource(Long userId, CollectionSource source);

    List<CollectionCard> findAllByUser_IdAndFavorite(Long userId, boolean favorite);

    Optional<CollectionCard> findByIdAndUser_Id(Long collectionCardId, Long userId);

    Optional<CollectionCard> findByExchange_IdAndUser_IdAndCard_IdAndSource(
            Long exchangeId,
            Long userId,
            Long cardId,
            CollectionSource source
    );

    @Query("""
            select collectionCard
            from CollectionCard collectionCard
            where collectionCard.user.id = :userId
              and not exists (
                  select folderCard.id
                  from FolderCard folderCard
                  where folderCard.collectionCard = collectionCard
              )
            order by collectionCard.collectedAt desc, collectionCard.id desc
            """)
    List<CollectionCard> findDefaultFolderCardsByUserId(@Param("userId") Long userId);

    long countByUser_IdAndSource(Long userId, CollectionSource source);
}
