package com.vivida.scoring;

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
    public List<PointRuleDTO> getPointRules() {
        return pointRuleService.getAllPointRules().stream()
                .map(PointRuleDTO::new)
                .toList();
    }

    @GetMapping("{id}")
    public PointRuleDTO getPointRuleById(@PathVariable Integer id) {
        return new PointRuleDTO(pointRuleService.getPointRuleById(id));
    }

    @GetMapping("group/{groupId}")
    public List<PointRuleDTO> getPointRulesByGroupId(@PathVariable Integer groupId) {
        return pointRuleService.getPointRulesByGroupId(groupId).stream()
                .map(PointRuleDTO::new)
                .toList();
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
