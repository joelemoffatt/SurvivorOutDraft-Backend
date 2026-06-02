package com.vivida.social.group;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

import com.vivida.auth.User;
import com.vivida.draft.Draft;
import com.vivida.game.episode.Episode;
import com.vivida.game.season.Season;
import com.vivida.scoring.PointRule;
import com.vivida.social.team.Team;

@Entity
@Table(name = "groups")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin;

    @ManyToOne
    @JoinColumn(name = "season_id", nullable = false)
    private Season season;

    @ManyToOne
    @JoinColumn(name = "draft_id")
    private Draft draft;

    /**
     * The last episode this group has watched for its season.
     * Used to hide spoilers by removing castaways booted after this episode.
     */
    @ManyToOne
    @JoinColumn(name = "latest_episode_watched_id")
    private Episode latestEpisodeWatched;

    /**
     * First episode number eligible for scoring.
     * Events before this episode are excluded from points.
     */
    @Column(nullable = false)
    private Integer firstScoringEpisodeNumber = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private List<GroupMember> members;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private List<Team> teams;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private List<PointRule> pointRules;

    /**
     * Version number for point rules in this group.
     * Incremented whenever any point rule changes.
     * Used to detect stale score calculations.
     */
    @Column(nullable = false)
    private Integer ruleVersion = 1;

    @Column(name = "is_loading", nullable = false)
    private boolean loading = false;

    @Column(name = "loading_text")
    private String loadingText;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = GroupStatus.PENDING;
        }
        if (firstScoringEpisodeNumber == null || firstScoringEpisodeNumber < 1) {
            firstScoringEpisodeNumber = 1;
        }
        if (ruleVersion == null) {
            ruleVersion = 1;
        }
    }
}
