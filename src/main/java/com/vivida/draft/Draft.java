package com.vivida.draft;

import com.vivida.auth.User;
import com.vivida.game.season.Season;
import com.vivida.social.group.Group;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "drafts", indexes = {
    @Index(name = "idx_drafts_group_id", columnList = "group_id"),
    @Index(name = "idx_drafts_status", columnList = "status"),
    @Index(name = "idx_drafts_current_turn_user_id", columnList = "current_turn_user_id")
})
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Draft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne
    @JoinColumn(name = "season_id", nullable = false)
    private Season season;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DraftStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DraftStyle style;

    private LocalDateTime scheduledAt;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    @Column(nullable = false)
    private Integer teamSize;

    @Column(nullable = false)
    private Integer totalParticipants;

    @Column(nullable = false)
    private Integer totalCastaways;

    /**
     * How many times any single castaway may be drafted before the cap increments.
     * Starts at 1. Increments to 2 once every castaway in the season has been
     * drafted once, then to 3 once every castaway has been drafted twice, and so on.
     */
    @Column(nullable = false)
    private Integer maxDraftsPerCastaway;

    @Column(nullable = false)
    private Integer totalPicks;

    @Column(nullable = false)
    private Integer currentPickNumber;

    @ManyToOne
    @JoinColumn(name = "current_turn_user_id")
    private User currentTurnUser;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "draft", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("draftPosition ASC")
    private List<DraftParticipant> participants = new ArrayList<>();

    @OneToMany(mappedBy = "draft", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("pickNumber ASC")
    private List<DraftPick> picks = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) {
            status = DraftStatus.PENDING;
        }
        if (style == null) {
            style = DraftStyle.SNAKE;
        }
        if (currentPickNumber == null) {
            currentPickNumber = 1;
        }
        if (totalCastaways == null) {
            totalCastaways = 0;
        }
        if (maxDraftsPerCastaway == null) {
            maxDraftsPerCastaway = 1;
        }
        if (totalParticipants == null) {
            totalParticipants = 0;
        }
        if (totalPicks == null) {
            totalPicks = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}