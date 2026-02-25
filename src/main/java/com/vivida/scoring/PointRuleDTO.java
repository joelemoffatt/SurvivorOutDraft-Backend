package com.vivida.scoring;

/**
 * DTO for PointRule response - breaks circular references
 */
public class PointRuleDTO {
    public Integer id;
    public Integer groupId;
    public String ruleType;
    public Integer points;
    public String description;
    public Boolean active;

    public PointRuleDTO(PointRule pointRule) {
        this.id = pointRule.getId();
        this.groupId = pointRule.getGroup() != null ? pointRule.getGroup().getId() : null;
        this.ruleType = pointRule.getRuleType().name();
        this.points = pointRule.getPoints();
        this.description = pointRule.getDescription();
        this.active = pointRule.getActive();
    }
}
