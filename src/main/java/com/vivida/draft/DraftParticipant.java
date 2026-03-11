package com.vivida.draft;

import com.vivida.auth.User;
import com.vivida.social.team.Team;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "draft_participants", uniqueConstraints = {
    @UniqueConstraint(name = "uk_draft_participants_draft_user", columnNames = {"draft_id", "user_id"}),
    @UniqueConstraint(name = "uk_draft_participants_draft_position", columnNames = {"draft_id", "draft_position"})
}, indexes = {
    @Index(name = "idx_draft_participants_draft_id", columnList = "draft_id"),
    @Index(name = "idx_draft_participants_user_id", columnList = "user_id"),
    @Index(name = "idx_draft_participants_team_id", columnList = "team_id")
})
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DraftParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "draft_id", nullable = false)
    private Draft draft;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    @Column(name = "draft_position", nullable = false)
    private Integer draftPosition;

    @Column(nullable = false)
    private Integer picksMade;

    @Column(nullable = false)
    private Boolean active;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (picksMade == null) {
            picksMade = 0;
        }
        if (active == null) {
            active = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}