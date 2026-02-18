package com.vivida;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/point-rules")
public class PointRuleController {

    private final PointRuleService pointRuleService;

    public PointRuleController(PointRuleService pointRuleService) {
        this.pointRuleService = pointRuleService;
    }

    @GetMapping
    public List<PointRule> getPointRules() {
        return pointRuleService.getAllPointRules();
    }

    @GetMapping("{id}")
    public PointRule getPointRuleById(@PathVariable Integer id) {
        return pointRuleService.getPointRuleById(id);
    }

    @GetMapping("group/{groupId}")
    public List<PointRule> getPointRulesByGroupId(@PathVariable Integer groupId) {
        return pointRuleService.getPointRulesByGroupId(groupId);
    }

    @GetMapping("group/{groupId}/active")
    public List<PointRule> getActivePointRulesByGroupId(@PathVariable Integer groupId) {
        return pointRuleService.getActivePointRulesByGroupId(groupId);
    }

    @PostMapping
    public void addPointRule(@RequestBody PointRule pointRule) {
        pointRuleService.insertPointRule(pointRule);
    }

    @PutMapping
    public void updatePointRule(@RequestBody PointRule pointRule) {
        pointRuleService.updatePointRule(pointRule);
    }

    @DeleteMapping("{id}")
    public void deletePointRule(@PathVariable Integer id) {
        pointRuleService.deletePointRuleById(id);
    }
}
