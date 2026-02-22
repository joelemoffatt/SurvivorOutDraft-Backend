package com.vivida.scoring;

import com.vivida.social.group.Group;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "point_rules")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PointRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RuleType ruleType;

    @Column(nullable = false)
    private Integer points;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Boolean active = true;
}
