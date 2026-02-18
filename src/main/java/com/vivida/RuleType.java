package com.vivida;

public enum RuleType {
    // Challenge wins
    IMMUNITY_WIN,
    REWARD_WIN,
    INDIVIDUAL_IMMUNITY,
    
    // Tribal/voting
    SURVIVED_TRIBAL,
    VOTED_OUT,
    VOTES_AGAINST,
    
    // Advantages/idols
    FOUND_IDOL,
    PLAYED_IDOL_SUCCESSFULLY,
    PLAYED_IDOL_UNSUCCESSFULLY,
    FOUND_ADVANTAGE,
    
    // Jury/finale
    JURY_VOTE_RECEIVED,
    SOLE_SURVIVOR,
    RUNNER_UP,
    
    // Journey/twist
    JOURNEY_REWARD,
    JOURNEY_LOST_VOTE,
    
    // Social
    CONFESSIONAL_COUNT,
    
    // Placement bonuses
    MERGE_BONUS,
    FINAL_THREE_BONUS,
    
    // Custom
    CUSTOM
}
