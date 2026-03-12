package com.vivida.scoring;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a single scoreable event fact extracted from raw game data.
 * 
 * This is used internally by PointCalculationService to return structured
 * scoring facts that ScoreProjectionService can transform into persisted
 * TeamCastawayScoreEvent entities.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ScorableEventFact {
    
    /**
     * The rule type that matched this event
     */
    private RuleType ruleType;
    
    /**
     * The source table type (CHALLENGE_PERFORMANCE, ADVANTAGE_MOVEMENT, etc.)
     */
    private ScoreEventSourceType sourceType;
    
    /**
     * The ID of the source record
     */
    private Integer sourceId;
    
    /**
     * Episode number when this event occurred
     */
    private Integer episodeNumber;
    
    /**
     * Human-readable label for the event
     * e.g., "Found idol", "Made merge", "Won individual immunity"
     */
    private String eventLabel;
    
    /**
     * How many times this event occurred (usually 1)
     */
    private Integer countValue;
    
    /**
     * Points assigned by the rule for each occurrence
     */
    private Integer pointsEach;
    
    /**
     * Multiplier to apply (default 1)
     */
    private Integer multiplier;
    
    public Integer getTotalPoints() {
        int count = countValue != null ? countValue : 0;
        int points = pointsEach != null ? pointsEach : 0;
        int mult = multiplier != null ? multiplier : 1;
        return count * points * mult;
    }
}
