package com.vivida;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class PointRuleService {

    private final PointRuleRepository pointRuleRepository;

    public PointRuleService(PointRuleRepository pointRuleRepository) {
        this.pointRuleRepository = pointRuleRepository;
    }

    public List<PointRule> getAllPointRules() {
        return pointRuleRepository.findAll();
    }

    public PointRule getPointRuleById(int id) {
        return pointRuleRepository.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "PointRule not found with id " + id
        ));
    }

    public List<PointRule> getPointRulesByGroupId(int groupId) {
        return pointRuleRepository.findByGroupId(groupId);
    }

    public List<PointRule> getActivePointRulesByGroupId(int groupId) {
        return pointRuleRepository.findByGroupIdAndActive(groupId, true);
    }

    public void insertPointRule(PointRule pointRule) {
        pointRuleRepository.save(pointRule);
    }

    public void updatePointRule(PointRule pointRule) {
        pointRuleRepository.save(pointRule);
    }

    public void deletePointRuleById(int id) {
        pointRuleRepository.deleteById(id);
    }
}
