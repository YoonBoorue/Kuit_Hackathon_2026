package com.example.hackathon.domain.folder.entity;

import com.example.hackathon.domain.collection.entity.CollectionCard;
import com.example.hackathon.domain.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "folder_cards",
        indexes = {
                @Index(name = "idx_folder_cards_folder_id", columnList = "folder_id"),
                @Index(name = "idx_folder_cards_collection_card_id", columnList = "collection_card_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_folder_cards_folder_collection_card",
                        columnNames = {"folder_id", "collection_card_id"}
                ),
                @UniqueConstraint(
                        name = "uk_folder_cards_collection_card",
                        columnNames = "collection_card_id"
                )
        }
)
public class FolderCard extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "folder_id", nullable = false)
    private Folder folder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "collection_card_id", nullable = false)
    private CollectionCard collectionCard;

    @Column(name = "added_at", nullable = false)
    private OffsetDateTime addedAt;

    protected FolderCard() {
    }

    public FolderCard(Folder folder, CollectionCard collectionCard, OffsetDateTime addedAt) {
        this.folder = folder;
        this.collectionCard = collectionCard;
        this.addedAt = addedAt;
    }

    public Long getId() {
        return id;
    }

    public Folder getFolder() {
        return folder;
    }

    public CollectionCard getCollectionCard() {
        return collectionCard;
    }

    public OffsetDateTime getAddedAt() {
        return addedAt;
    }
}
