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

    private Integer teamSize;

    /**
     * The last episode this group has watched for its season.
     * Used to hide spoilers by removing castaways booted after this episode.
     */
    @ManyToOne
    @JoinColumn(name = "latest_episode_watched_id")
    private Episode latestEpisodeWatched;

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

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = GroupStatus.PENDING;
        }
    }
}
