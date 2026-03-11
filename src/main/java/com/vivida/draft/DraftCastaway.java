package com.vivida.draft;

import com.vivida.game.castaway.CastawayPerformance;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "draft_castaways", uniqueConstraints = {
        @UniqueConstraint(name = "uk_draft_castaways_draft_castaway", columnNames = {"draft_id", "castaway_performance_id"})
}, indexes = {
        @Index(name = "idx_draft_castaways_draft_id", columnList = "draft_id"),
        @Index(name = "idx_draft_castaways_castaway_performance_id", columnList = "castaway_performance_id")
})
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DraftCastaway {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "draft_id", nullable = false)
    private Draft draft;

    @ManyToOne
    @JoinColumn(name = "castaway_performance_id", nullable = false)
    private CastawayPerformance castawayPerformance;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
