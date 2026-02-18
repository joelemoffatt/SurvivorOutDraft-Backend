package com.vivida;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TeamCastawayService {

    private final TeamCastawayRepository teamCastawayRepository;

    public TeamCastawayService(TeamCastawayRepository teamCastawayRepository) {
        this.teamCastawayRepository = teamCastawayRepository;
    }

    public List<TeamCastaway> getAllTeamCastaways() {
        return teamCastawayRepository.findAll();
    }

    public TeamCastaway getTeamCastawayById(int id) {
        return teamCastawayRepository.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "TeamCastaway not found with id " + id
        ));
    }

    public List<TeamCastaway> getTeamCastawaysByTeamId(int teamId) {
        return teamCastawayRepository.findByTeamIdOrderByDraftOrderAsc(teamId);
    }

    public void draftCastaway(TeamCastaway teamCastaway) {
        if (teamCastawayRepository.existsByTeamIdAndCastawayPerformanceId(
                teamCastaway.getTeam().getId(),
                teamCastaway.getCastawayPerformance().getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Castaway already on this team");
        }
        teamCastawayRepository.save(teamCastaway);
    }

    public void updateTeamCastaway(TeamCastaway teamCastaway) {
        teamCastawayRepository.save(teamCastaway);
    }

    public void deleteTeamCastawayById(int id) {
        teamCastawayRepository.deleteById(id);
    }
}
