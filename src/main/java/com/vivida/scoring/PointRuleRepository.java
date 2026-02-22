package com.vivida.scoring;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointRuleRepository extends JpaRepository<PointRule, Integer> {
    List<PointRule> findByGroupId(Integer groupId);
    List<PointRule> findByGroupIdAndActive(Integer groupId, Boolean active);
    List<PointRule> findByGroupIdAndRuleType(Integer groupId, RuleType ruleType);
}
