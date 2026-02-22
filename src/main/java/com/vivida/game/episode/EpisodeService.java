package com.vivida.game.episode;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.vivida.game.advantage.AdvantageMovement;
import com.vivida.game.advantage.AdvantageMovementDetailDto;
import com.vivida.game.boot.Boot;
import com.vivida.game.boot.BootDetailDto;
import com.vivida.game.castaway.CastawayPerformance;
import com.vivida.game.challenge.Challenge;
import com.vivida.game.challenge.ChallengeDetailDto;
import com.vivida.game.challenge.ChallengePerformance;
import com.vivida.game.challenge.ChallengePerformanceRepository;
import com.vivida.game.challenge.ChallengePerformanceRowDto;
import com.vivida.game.journey.Journey;
import com.vivida.game.journey.JourneyDetailDto;
import com.vivida.game.juryVote.JuryVote;
import com.vivida.game.juryVote.JuryVoteDetailDto;
import com.vivida.game.juryVote.JuryVoteRepository;
import com.vivida.game.tribal.Tribal;
import com.vivida.game.tribal.TribalDetailDto;
import com.vivida.game.tribal.TribalVoteRowDto;
import com.vivida.game.tribe.TribeMappingRepository;
import com.vivida.game.tribe.TribePerformanceGroupDto;
import com.vivida.game.vote.Vote;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EpisodeService {

    private final EpisodeRepository episodeRepository;
    private final TribeMappingRepository tribeMappingRepository;
    private final ChallengePerformanceRepository challengePerformanceRepository;
    private final JuryVoteRepository juryVoteRepository;

    public EpisodeService(
            EpisodeRepository episodeRepository,
            TribeMappingRepository tribeMappingRepository,
            ChallengePerformanceRepository challengePerformanceRepository,
            JuryVoteRepository juryVoteRepository
    ) {
        this.episodeRepository = episodeRepository;
        this.tribeMappingRepository = tribeMappingRepository;
        this.challengePerformanceRepository = challengePerformanceRepository;
        this.juryVoteRepository = juryVoteRepository;
    }

    public List<Episode> getAllEpisodes() {
        return episodeRepository.findAll();
    }

    public Episode getEpisodeById(int id) {
        return episodeRepository.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Episode not found with id " + id
        ));
    }

    public List<Episode> getEpisodesBySeasonId(Integer seasonId) {
        return episodeRepository.findBySeasonId(seasonId);
    }

    @Transactional(readOnly = true)
    public EpisodeDetailDto getEpisodeDetail(Integer seasonId, Integer episodeNumber) {
        Episode episode = episodeRepository.findBySeasonAndEpisodeNumber(seasonId, episodeNumber)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Episode not found for season " + seasonId + " and episode " + episodeNumber
                ));

        Map<Integer, String> tribeByCastawayPerformanceId = tribeMappingRepository.findByEpisodeId(episode.getId())
                .stream()
                .collect(Collectors.toMap(
                        tm -> tm.getCastawayPerformance().getId(),
                        tm -> tm.getTribe() != null ? tm.getTribe().getName() : "Unknown Tribe",
                        (existing, replacement) -> existing
                ));

        List<ChallengeDetailDto> challengeDtos = toChallengeDtos(episode.getChallenges(), tribeByCastawayPerformanceId);
        List<JourneyDetailDto> journeyDtos = toJourneyDtos(episode.getJourneys());
        List<AdvantageMovementDetailDto> advantageDtos = toAdvantageDtos(episode.getAdvantageMovements());
        List<TribalDetailDto> allTribalDtos = toTribalDtos(episode.getTribals());
        List<TribalDetailDto> tribalDtos = allTribalDtos.stream()
                .filter(tribal -> tribal.votes() != null && !tribal.votes().isEmpty())
                .toList();
        Set<Integer> tribalBootOrders = allTribalDtos.stream()
                .filter(tribal -> tribal.votes() != null && !tribal.votes().isEmpty())
                .map(TribalDetailDto::bootOrder)
                .filter(bootOrder -> bootOrder != null)
                .collect(Collectors.toCollection(HashSet::new));
        List<BootDetailDto> bootDtos = toBootDtos(episode.getBoots(), tribalBootOrders);
        
        // Get jury votes and final three ranking
        List<JuryVote> juryVotes = juryVoteRepository.findByEpisodeId(episode.getId());
        List<JuryVoteDetailDto> juryVoteDtos = toJuryVoteDtos(juryVotes);
        
        // Separate final results boots (Lost fire, First, Second, Third) from other boots
        List<BootDetailDto> finalResultsBoots = bootDtos.stream()
                .filter(boot -> isFinalResultEvent(boot.event()))
                .toList();
        
        // Extract final three castaway IDs from jury votes (these are the finalists)
        Set<Integer> finalThreeCastawayIds = juryVotes.stream()
                .map(jv -> jv.getVotedFor().getId())
                .collect(Collectors.toSet());
        
        // Filter out final three from OTHER boots only (not from finale results)
        List<BootDetailDto> otherBoots = bootDtos.stream()
                .filter(boot -> !isFinalResultEvent(boot.event()))
                .filter(boot -> {
                    // Find the boot entry for this castaway name
                    return episode.getBoots().stream()
                            .filter(b -> castawayShortName(b.getCastaway()).equals(boot.castawayName()))
                            .noneMatch(b -> finalThreeCastawayIds.contains(b.getCastaway().getId()));
                })
                .toList();
        
        List<FinalRankingDetailDto> finalRankingDtos = toFinalRankingDtos(juryVotes);

        return new EpisodeDetailDto(
                episode.getEpisodeTitle(),
                episode.getEpisodeNumber(),
                challengeDtos,
                journeyDtos,
                advantageDtos,
                tribalDtos,
                otherBoots,
                finalResultsBoots,
                juryVoteDtos,
                finalRankingDtos
        );
    }

    public void insertEpisode(Episode episode) {
        episodeRepository.save(episode);
    }

    public void updateEpisode(Episode episode) {
        episodeRepository.save(episode);
    }

    public void deleteEpisodeById(int id) {
        episodeRepository.deleteById(id);
    }

    private List<ChallengeDetailDto> toChallengeDtos(
            List<Challenge> challenges,
            Map<Integer, String> tribeByCastawayPerformanceId
    ) {
        if (challenges == null) {
            return List.of();
        }

        return challenges.stream()
                .sorted(Comparator.comparing(Challenge::getChallenge_number, Comparator.nullsLast(Integer::compareTo)))
                .map(challenge -> {
                    Map<String, List<ChallengePerformanceRowDto>> grouped = new LinkedHashMap<>();
                    List<ChallengePerformance> performances = challenge.getChallengesPerformances() == null
                            ? List.of()
                            : challenge.getChallengesPerformances();
                    if (performances.isEmpty() && challenge.getId() != null) {
                        performances = challengePerformanceRepository.findByChallengeId(challenge.getId());
                    }

                    for (ChallengePerformance performance : performances) {
                        Integer castawayPerfId = performance.getCastaway() != null ? performance.getCastaway().getId() : null;
                        String tribeName = castawayPerfId != null
                                ? tribeByCastawayPerformanceId.getOrDefault(castawayPerfId, "Unknown Tribe")
                                : "Unknown Tribe";

                        grouped.computeIfAbsent(tribeName, ignored -> new ArrayList<>())
                                .add(new ChallengePerformanceRowDto(
                                        castawayShortName(performance.getCastaway()),
                                        performance.getPlace(),
                                        performance.getWon(),
                                        performance.getSatOut()
                                ));
                    }

                    List<TribePerformanceGroupDto> groups = grouped.entrySet().stream()
                            .map(entry -> new TribePerformanceGroupDto(
                                    entry.getKey(),
                                    entry.getValue().stream()
                                            .sorted(Comparator
                                                    .comparing(ChallengePerformanceRowDto::place, Comparator.nullsLast(Integer::compareTo))
                                                    .thenComparing(ChallengePerformanceRowDto::castawayName, Comparator.nullsLast(String::compareToIgnoreCase)))
                                            .toList()
                            ))
                            .toList();

                    return new ChallengeDetailDto(
                            challenge.getName(),
                            challenge.getChallenge_type(),
                            challenge.getChallenge_number(),
                            groups
                    );
                })
                .toList();
    }

    private List<JourneyDetailDto> toJourneyDtos(List<Journey> journeys) {
        if (journeys == null) {
            return List.of();
        }

        return journeys.stream()
                .sorted(Comparator.comparing(Journey::getId))
                .map(journey -> new JourneyDetailDto(
                        castawayShortName(journey.getCastaway()),
                        journey.getEvent(),
                        journey.getReward(),
                        journey.getLostVote(),
                        journey.getChoseToPlay()
                ))
                .toList();
    }

    private List<AdvantageMovementDetailDto> toAdvantageDtos(List<AdvantageMovement> advantageMovements) {
        if (advantageMovements == null) {
            return List.of();
        }

        return advantageMovements.stream()
                .sorted(Comparator.comparing(AdvantageMovement::getId))
                .map(movement -> new AdvantageMovementDetailDto(
                        castawayShortName(movement.getCastawayId()),
                        castawayShortName(movement.getPlayedForId()),
                        movement.getAdvantageType(),
                        movement.getEvent(),
                        movement.getSuccess(),
                        movement.getVotesNullified()
                ))
                .toList();
    }

    private List<TribalDetailDto> toTribalDtos(List<Tribal> tribals) {
        if (tribals == null) {
            return List.of();
        }

        List<Tribal> sortedTribals = tribals.stream()
                .sorted(Comparator.comparing(Tribal::getBootOrder, Comparator.nullsLast(Integer::compareTo)))
                .toList();

        List<List<TribalVoteRowDto>> voteRowsByTribal = sortedTribals.stream()
                .map(tribal -> tribal.getVotes() == null
                        ? List.<TribalVoteRowDto>of()
                        : tribal.getVotes().stream()
                                .flatMap(voteRound -> voteRound.getVotes() == null ? List.<Vote>of().stream() : voteRound.getVotes().stream())
                                .map(vote -> new TribalVoteRowDto(
                                        castawayShortName(vote.getCastaway()),
                                        castawayShortName(vote.getVotedFor()),
                                        vote.getNullified()
                                ))
                                .toList()
                )
                .collect(Collectors.toCollection(ArrayList::new));

        // Data fix-up for finale-style episodes where vote rows are shifted to a following fire-making tribal.
        for (int i = 0; i < sortedTribals.size() - 1; i++) {
            Tribal current = sortedTribals.get(i);
            Tribal next = sortedTribals.get(i + 1);
            List<TribalVoteRowDto> currentVotes = voteRowsByTribal.get(i);
            List<TribalVoteRowDto> nextVotes = voteRowsByTribal.get(i + 1);

            String currentEvent = current.getBoot() != null ? current.getBoot().getEvent() : null;
            String nextEvent = next.getBoot() != null ? next.getBoot().getEvent() : null;

            boolean currentNeedsVotes = currentVotes.isEmpty() && isVotingBootEvent(currentEvent);
            boolean nextLooksLikeNonVoteElimination = !nextVotes.isEmpty()
                    && (isFireStyleEvent(nextEvent) || !isVotingBootEvent(nextEvent));

            if (currentNeedsVotes && nextLooksLikeNonVoteElimination) {
                voteRowsByTribal.set(i, nextVotes);
                voteRowsByTribal.set(i + 1, List.of());
            }
        }

        List<TribalDetailDto> tribalDtos = new ArrayList<>();
        for (int tribalIndex = 0; tribalIndex < sortedTribals.size(); tribalIndex++) {
            Tribal tribal = sortedTribals.get(tribalIndex);
                    List<TribalVoteRowDto> votes = voteRowsByTribal.get(tribalIndex);

                    String votedOutName = "Not available";
                    if (tribal.getBoot() != null) {
                        votedOutName = castawayShortName(tribal.getBoot().getCastaway());
                    }

                    tribalDtos.add(new TribalDetailDto(
                            tribal.getTribe() != null ? tribal.getTribe().getName() : "Unknown Tribe",
                            tribal.getBootOrder(),
                            votedOutName,
                            votes
                    ));
        }
        return tribalDtos;
    }

    private boolean isVotingBootEvent(String event) {
        if (event == null) return false;
        String normalized = event.trim().toLowerCase();
        return normalized.equals("votedout") || normalized.equals("eliminated") || normalized.equals("tieddestiny");
    }

    private boolean isFireStyleEvent(String event) {
        if (event == null) return false;
        String normalized = event.trim().toLowerCase();
        return normalized.equals("lostfire") || normalized.equals("lostfinalfire");
    }

    private List<BootDetailDto> toBootDtos(List<Boot> boots, Set<Integer> tribalBootOrders) {
        if (boots == null) {
            return List.of();
        }

        return boots.stream()
                .filter(boot -> boot.getBootOrder() == null || !tribalBootOrders.contains(boot.getBootOrder()))
                .sorted(Comparator.comparing(Boot::getBootOrder, Comparator.nullsLast(Integer::compareTo)))
                .map(boot -> new BootDetailDto(
                        castawayShortName(boot.getCastaway()),
                        boot.getEvent(),
                        boot.getBootOrder(),
                        boot.getTribal() != null && boot.getTribal().getTribe() != null
                                ? boot.getTribal().getTribe().getName()
                                : null
                ))
                .toList();
    }

    private String castawayShortName(CastawayPerformance castawayPerformance) {
        if (castawayPerformance == null || castawayPerformance.getCastaway() == null) {
            return "Unknown";
        }
        return castawayPerformance.getCastaway().getName();
    }

    private List<JuryVoteDetailDto> toJuryVoteDtos(List<JuryVote> juryVotes) {
        if (juryVotes == null || juryVotes.isEmpty()) {
            return List.of();
        }
        return juryVotes.stream()
                .map(vote -> new JuryVoteDetailDto(
                        castawayShortName(vote.getCastaway()),
                        castawayShortName(vote.getVotedFor())
                ))
                .toList();
    }

    private List<FinalRankingDetailDto> toFinalRankingDtos(List<JuryVote> juryVotes) {
        if (juryVotes == null || juryVotes.isEmpty()) {
            return List.of();
        }
        
        // Count votes for each finalist
        Map<String, Integer> voteCount = new LinkedHashMap<>();
        juryVotes.forEach(vote -> {
            String finalist = castawayShortName(vote.getVotedFor());
            voteCount.put(finalist, voteCount.getOrDefault(finalist, 0) + 1);
        });
        
        // Sort by vote count (descending) and create rankings
        List<FinalRankingDetailDto> rankings = new ArrayList<>();
        int rank = 1;
        for (Map.Entry<String, Integer> entry : voteCount.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .toList()) {
            String placement = switch(rank) {
                case 1 -> "1st";
                case 2 -> "2nd";
                case 3 -> "3rd";
                default -> "Other";
            };
            rankings.add(new FinalRankingDetailDto(entry.getKey(), placement));
            rank++;
        }
        return rankings;
    }

    private boolean isFinalResultEvent(String event) {
        if (event == null) return false;
        String normalized = event.trim().toLowerCase();
        return normalized.equals("lost fire") || 
               normalized.equals("lostfire") ||
               normalized.equals("first") || 
               normalized.equals("1") ||
               normalized.equals("second") || 
               normalized.equals("2") ||
               normalized.equals("third") || 
               normalized.equals("3");
    }
}
