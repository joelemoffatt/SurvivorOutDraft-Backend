package com.vivida;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.*;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired private SeasonRepository seasonRepository;
    @Autowired private CastawayRepository castawayRepository;
    @Autowired private CastawayPerformanceRepository castawayPerformanceRepository;
    @Autowired private EpisodeRepository episodeRepository;
    @Autowired private TribeRepository tribeRepository;
    @Autowired private ChallengeRepository challengeRepository;
    @Autowired private ChallengePerformanceRepository challengePerformanceRepository;
    @Autowired private TribalRepository tribalRepository;
    @Autowired private VoteRoundRepository voteRoundRepository;
    @Autowired private VoteRepository voteRepository;
    @Autowired private JuryVoteRepository juryVoteRepository;
    @Autowired private JourneyRepository journeyRepository;
    @Autowired private BootRepository bootRepository;
    @Autowired private AdvantageMovementRepository advantageMovementRepository;
    @Autowired private TribeMappingRepository tribeMappingRepository;
    
    @PersistenceContext
    private EntityManager entityManager;

    private ObjectMapper mapper = new ObjectMapper();
    private static final String DATA_PATH = "/Users/joelmoffatt/VSCode/SurvivorOutDraft/survivoR/data/class-entities/";
    private static final int BATCH_SIZE = 100;
    
    private Map<String, Castaway> castawayCache = new HashMap<>();
    private Map<Integer, Season> seasonCache = new HashMap<>();
    private Map<String, Tribe> tribeByKeyCache = new HashMap<>();  // key: "seasonId:tribeName"
    private Map<Integer, Episode> episodeCache = new HashMap<>();
    private Map<String, CastawayPerformance> perfCache = new HashMap<>();  // key: "seasonNum:castawayId"
    private Map<Integer, Challenge> challengeById = new HashMap<>();  // key: challenge_id

    private List<Map<String, Object>> loadJsonFile(String filename) throws Exception {
        File file = new File(DATA_PATH + filename);
        if (!file.exists()) {
            System.out.println("⚠  " + filename + " not found");
            return new ArrayList<>();
        }
        List<Map<String, Object>> data = mapper.readValue(file, mapper.getTypeFactory().constructCollectionType(List.class, Map.class));
        return data;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("LOADING SURVIVOR DATA INTO DATABASE");
        System.out.println("=".repeat(80) + "\n");

        long totalStart = System.currentTimeMillis();
        try {
            clearAllData();
            
            loadSeasons();
            seasonRepository.findAll().forEach(s -> seasonCache.put(s.getSeason(), s));
            
            loadCastaways();
            castawayRepository.findAll().forEach(c -> castawayCache.put(c.getJson_id(), c));
            
            loadCastawayPerformances();
            castawayPerformanceRepository.findAll().forEach(p -> 
                perfCache.put(p.getSeason().getSeason() + ":" + p.getCastaway().getJson_id(), p));
            
            loadTribes();
            tribeRepository.findAll().forEach(t -> tribeByKeyCache.put(t.getSeason().getSeason() + ":" + t.getName(), t));
            
            loadEpisodes();
            episodeRepository.findAll().forEach(e -> episodeCache.put(e.getId(), e));
            
            loadChallenges();
            challengeRepository.findAll().forEach(c -> challengeById.put(c.getChallenge_id(), c));
            loadTribal();
            loadTribeMapping();
            loadVoteRounds();
            loadChallengePerformances();
            loadVotes();
            loadJuryVotes();
            loadJourneys();
            loadBoots();
            loadAdvantageMovements();
            
            System.out.println("\n" + "=".repeat(80));
            long totalTime = (System.currentTimeMillis() - totalStart) / 1000;
            System.out.println("✓ DATA LOAD COMPLETE in " + totalTime + " seconds");
            System.out.println("=".repeat(80) + "\n");
        } catch (Exception e) {
            System.err.println("ERROR LOADING DATA: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    private void clearAllData() {
        System.out.println("Clearing all existing data...");
        advantageMovementRepository.deleteAll();
        bootRepository.deleteAll();
        journeyRepository.deleteAll();
        juryVoteRepository.deleteAll();
        voteRepository.deleteAll();
        voteRoundRepository.deleteAll();
        tribeMappingRepository.deleteAll();
        challengePerformanceRepository.deleteAll();
        challengeRepository.deleteAll();
        tribalRepository.deleteAll();
        castawayPerformanceRepository.deleteAll();
        episodeRepository.deleteAll();
        tribeRepository.deleteAll();
        castawayRepository.deleteAll();
        seasonRepository.deleteAll();
        entityManager.flush();
        System.out.println("✓ All data cleared\n");
    }
    
    private <T> void saveInBatches(List<T> entities, String entityName) {
        if (entities.isEmpty()) return;
        
        System.out.println("Saving " + entities.size() + " " + entityName + " in batches of " + BATCH_SIZE + "...");
        
        for (int i = 0; i < entities.size(); i++) {
            entityManager.persist(entities.get(i));
            
            if (i > 0 && i % BATCH_SIZE == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        entityManager.flush();
        entityManager.clear();
        
        System.out.println("✓ Saved " + entities.size() + " " + entityName + "\n");
    }

    private void loadSeasons() throws Exception {
        List<Map<String, Object>> data = loadJsonFile("season.json");
        if (data.isEmpty()) return;

        System.out.println("Loading Seasons...");
        List<Season> seasons = new ArrayList<>();
        for (Map<String, Object> item : data) {
            Season season = new Season();
            Integer seasonNum = getInt(item, "season");
            season.setSeason(seasonNum);
            season.setVersion(getString(item, "version"));
            seasons.add(season);
        }
        saveInBatches(seasons, "seasons");
        
        // Rebuild cache after save
        seasonRepository.findAll().forEach(s -> seasonCache.put(s.getSeason(), s));
    }

    private void loadCastaways() throws Exception {
        List<Map<String, Object>> data = loadJsonFile("castaways.json");
        if (data.isEmpty()) return;

        System.out.println("Loading Castaways...");
        List<Castaway> castaways = new ArrayList<>();
        for (Map<String, Object> item : data) {
            Castaway castaway = new Castaway();
            String jsonId = getString(item, "json_id");
            castaway.setJson_id(jsonId);
            castaway.setName(getString(item, "name"));
            castaway.setFull_name(getString(item, "full_name"));
            castaway.setCity(getString(item, "city"));
            castaway.setState(getString(item, "state"));
            castaway.setOccupation(getString(item, "occupation"));
            castaway.setGender(getString(item, "gender"));
            castaway.setDate_of_birth(getString(item, "date_of_birth"));
            castaways.add(castaway);
        }
        saveInBatches(castaways, "castaways");
        
        // Rebuild cache after save
        castawayRepository.findAll().forEach(c -> castawayCache.put(c.getJson_id(), c));
    }

    private void loadCastawayPerformances() throws Exception {
        List<Map<String, Object>> data = loadJsonFile("castaway_performances.json");
        if (data.isEmpty()) return;

        System.out.println("Loading Castaway Performances...");
        List<CastawayPerformance> performances = new ArrayList<>();
        int seasonFails = 0, castawayFails = 0;
        for (Map<String, Object> item : data) {
            Integer seasonNum = getInt(item, "season");
            String castawayId = getString(item, "castaway_id");
            
            Season season = seasonCache.get(seasonNum);
            Castaway castaway = castawayCache.getOrDefault(castawayId, null);
            
            if (season == null) {
                seasonFails++;
                continue;
            }
            if (castaway == null) {
                castawayFails++;
                continue;
            }
            
            CastawayPerformance perf = new CastawayPerformance();
            perf.setSeason(season);
            perf.setCastaway(castaway);
            performances.add(perf);
        }
        saveInBatches(performances, "castaway performances");
        if (seasonFails > 0 || castawayFails > 0) System.out.println("  (fails: season=" + seasonFails + ", castaway=" + castawayFails + ")");
    }

    private void loadTribes() throws Exception {
        List<Map<String, Object>> data = loadJsonFile("tribe.json");
        if (data.isEmpty()) return;

        System.out.println("Loading Tribes...");
        List<Tribe> tribes = new ArrayList<>();
        for (Map<String, Object> item : data) {
            Integer seasonNum = getInt(item, "season");
            Season season = seasonCache.get(seasonNum);
            
            if (season == null) {
                System.out.println("⚠  Skipping tribe: season=" + seasonNum);
                continue;
            }
            
            Tribe tribe = new Tribe();
            tribe.setSeason(season);
            tribe.setName(getString(item, "name"));
            tribe.setColor(getString(item, "color"));
            tribes.add(tribe);
        }
        saveInBatches(tribes, "tribes");
    }

    private void loadEpisodes() throws Exception {
        List<Map<String, Object>> data = loadJsonFile("episode.json");
        if (data.isEmpty()) return;

        System.out.println("Loading Episodes...");
        List<Episode> episodes = new ArrayList<>();
        for (Map<String, Object> item : data) {
            Integer seasonNum = getInt(item, "season");
            Integer episodeNum = getInt(item, "episode_number");
            Season season = seasonCache.get(seasonNum);
            
            if (season == null) {
                System.out.println("⚠  Skipping episode: season=" + seasonNum);
                continue;
            }
            
            Episode episode = new Episode();
            episode.setId(seasonNum * 1000 + episodeNum);
            episode.setSeason(season);
            episode.setEpisodeNumber(episodeNum);
            episode.setEpisodeTitle(getString(item, "episode_title"));
            episode.setEpisodeDate(getString(item, "episode_date"));
            episode.setEpisodeLength(getInt(item, "episode_length"));
            episode.setEpisodeSummary(getString(item, "episode_summary"));
            episode.setIsFinale(Boolean.TRUE.equals(getBoolean(item, "is_finale")));
            episodes.add(episode);
        }
        saveInBatches(episodes, "episodes");
    }

    private void loadChallenges() throws Exception {
        List<Map<String, Object>> data = loadJsonFile("challenge.json");
        if (data.isEmpty()) return;

        System.out.println("Loading Challenges...");
        List<Challenge> challenges = new ArrayList<>();
        for (Map<String, Object> item : data) {
            Integer seasonNum = getInt(item, "season");
            Season season = seasonCache.get(seasonNum);
            
            if (season == null) {
                System.out.println("⚠  Skipping challenge: season=" + seasonNum);
                continue;
            }
            
            Challenge challenge = new Challenge();
            challenge.setSeason(season);
            challenge.setChallenge_id(getInt(item, "challenge_id"));
            challenge.setChallenge_number(getInt(item, "challenge_number"));
            challenge.setChallenge_type(getString(item, "challenge_type"));
            
            Integer episodeNum = getInt(item, "episode");
            if (episodeNum != null) {
                Episode episode = episodeRepository.findById(seasonNum * 1000 + episodeNum).orElse(null);
                challenge.setEpisode(episode);
            }

            if (challenge.getEpisode() == null) {
                System.out.println("⚠  Skipping challenge: missing episode season=" + seasonNum + ", episode=" + episodeNum);
                continue;
            }
            
            challenge.setName(getString(item, "name"));
            challenge.setBalance(getBoolean(item, "balance"));
            challenge.setEndurance(getBoolean(item, "endurance"));
            challenge.setPuzzle(getBoolean(item, "puzzle"));
            challenge.setPrecision(getBoolean(item, "precision"));
            challenge.setWater(getBoolean(item, "water"));
            challenges.add(challenge);
        }
        challengeRepository.saveAll(challenges);
        System.out.println("✓ Loaded " + challenges.size() + " challenges\n");
    }

    private void loadTribal() throws Exception {
        List<Map<String, Object>> data = loadJsonFile("tribal.json");
        if (data.isEmpty()) return;

        System.out.println("Loading Tribal Councils...");
        List<Tribal> tribals = new ArrayList<>();
        int seasonFails = 0, episodeFails = 0, tribeFails = 0;
        for (Map<String, Object> item : data) {
            Integer seasonNum = getInt(item, "season");
            Integer episodeNum = getInt(item, "episode");
            String tribeName = getString(item, "tribe");
            
            Season season = seasonCache.get(seasonNum);
            if (season == null) {
                seasonFails++;
                continue;
            }
            
            Episode episode = episodeRepository.findById(seasonNum * 1000 + episodeNum).orElse(null);
            if (episode == null) {
                episodeFails++;
                continue;
            }
            
            String tribeKey = seasonNum + ":" + tribeName;
            Tribe tribe = tribeByKeyCache.get(tribeKey);
            if (tribe == null) {
                tribeFails++;
                continue;
            }
            
            Tribal tribal = new Tribal();
            tribal.setEpisode(episode);
            tribal.setTribe(tribe);
            tribals.add(tribal);
        }
        tribalRepository.saveAll(tribals);
        System.out.println("✓ Loaded " + tribals.size() + " tribal councils (season_fails=" + seasonFails + ", episode_fails=" + episodeFails + ", tribe_fails=" + tribeFails + ")\n");
    }

    private void loadChallengePerformances() throws Exception {
        List<Map<String, Object>> data = loadJsonFile("challengePerformance.json");
        if (data.isEmpty()) return;

        System.out.println("Loading Challenge Performances...");
        List<ChallengePerformance> performances = new ArrayList<>();
        int challengeFails = 0, castawayFails = 0, perfFails = 0;
        for (Map<String, Object> item : data) {
            Integer challengeId = getInt(item, "challenge_id");
            String castawayId = getString(item, "castaway_id");
            Integer seasonNum = getInt(item, "season");
            
            Challenge challenge = challengeById.getOrDefault(challengeId, null);
            if (challenge == null) {
                challengeFails++;
                continue;
            }
            
            Castaway castaway = castawayCache.getOrDefault(castawayId, null);
            if (castaway == null) {
                castawayFails++;
                continue;
            }
            
            CastawayPerformance castawayPerf = perfCache.get(seasonNum + ":" + castawayId);
            if (castawayPerf == null) {
                perfFails++;
                continue;
            }
            
            ChallengePerformance perf = new ChallengePerformance();
            perf.setChallenge(challenge);
            perf.setCastaway(castawayPerf);
            perf.setPlace(getInt(item, "place"));
            perf.setSatOut(getBoolean(item, "sat_out"));
            perf.setWon(getBoolean(item, "won"));
            performances.add(perf);
        }
        challengePerformanceRepository.saveAll(performances);
        System.out.println("✓ Loaded " + performances.size() + " challenge performances (challenge_fails=" + challengeFails + ", castaway_fails=" + castawayFails + ", perf_fails=" + perfFails + ")\n");
    }

    private void loadTribeMapping() throws Exception {
        List<Map<String, Object>> data = loadJsonFile("tribeMapping.json");
        if (data.isEmpty()) return;

        System.out.println("Loading Tribe Mappings...");
        List<TribeMapping> mappings = new ArrayList<>();
        int seasonFails = 0, episodeFails = 0, castawayFails = 0, perfFails = 0, tribeFails = 0;
        for (Map<String, Object> item : data) {
            Integer seasonNum = getInt(item, "season");
            Integer episodeNum = getInt(item, "episode");
            String castawayId = getString(item, "castaway_id");
            String tribeName = getString(item, "tribe");
            
            Season season = seasonCache.get(seasonNum);
            if (season == null) {
                seasonFails++;
                continue;
            }
            
            Episode episode = episodeRepository.findById(seasonNum * 1000 + episodeNum).orElse(null);
            if (episode == null) {
                episodeFails++;
                continue;
            }
            
            Castaway castaway = castawayCache.getOrDefault(castawayId, null);
            if (castaway == null) {
                castawayFails++;
                continue;
            }
            
            CastawayPerformance castawayPerf = perfCache.get(seasonNum + ":" + castawayId);
            if (castawayPerf == null) {
                perfFails++;
                continue;
            }
            
            String tribeKey = seasonNum + ":" + tribeName;
            Tribe oldTribe = tribeByKeyCache.get(tribeKey);
            if (oldTribe == null) {
                tribeFails++;
                continue;
            }
            
            TribeMapping mapping = new TribeMapping();
            mapping.setSeason(season);
            mapping.setEpisode(episode);
            mapping.setCastawayPerformance(castawayPerf);
            mapping.setTribe(oldTribe);
            mapping.setStatus(getString(item, "status"));
            mappings.add(mapping);
        }
        tribeMappingRepository.saveAll(mappings);
        System.out.println("✓ Loaded " + mappings.size() + " tribe mappings (season=" + seasonFails + ", episode=" + episodeFails + ", castaway=" + castawayFails + ", perf=" + perfFails + ", tribe=" + tribeFails + ")\n");
    }

    private void loadVoteRounds() throws Exception {
        List<Map<String, Object>> data = loadJsonFile("voteRound.json");
        if (data.isEmpty()) return;

        System.out.println("Loading Vote Rounds...");
        List<VoteRound> voteRounds = new ArrayList<>();
        int tribalFails = 0;
        for (Map<String, Object> item : data) {
            Integer seasonNum = getInt(item, "season");
            Integer episodeNum = getInt(item, "episode");
            String tribeName = getString(item, "tribe");
            
            Season season = seasonCache.get(seasonNum);
            if (season == null) continue;
            
            Tribal tribal = null;
            for (Tribal t : tribalRepository.findAll()) {
                if (t.getEpisode().getSeason().getSeason().equals(seasonNum) && 
                    t.getEpisode().getEpisodeNumber().equals(episodeNum) &&
                    t.getTribe().getName().equals(tribeName)) {
                    tribal = t;
                    break;
                }
            }
            if (tribal == null) {
                tribalFails++;
                continue;
            }
            
            VoteRound voteRound = new VoteRound();
            voteRound.setTribal(tribal);
            voteRound.setIsTie(getBoolean(item, "is_tie"));
            voteRounds.add(voteRound);
        }
        voteRoundRepository.saveAll(voteRounds);
        System.out.println("✓ Loaded " + voteRounds.size() + " vote rounds\n");
    }

    private void loadVotes() throws Exception {
        List<Map<String, Object>> data = loadJsonFile("vote.json");
        if (data.isEmpty()) return;

        System.out.println("Loading Votes...");
        List<Vote> votes = new ArrayList<>();
        for (Map<String, Object> item : data) {
            Integer voteRoundId = getInt(item, "vote_round_id");
            if (voteRoundId == null) {
                System.out.println("⚠  Skipping vote: vote_round_id field missing");
                continue;
            }
            
            VoteRound voteRound = voteRoundRepository.findById(voteRoundId).orElse(null);
            if (voteRound == null) continue;

            String castawayId = getString(item, "castaway_id");
            String votedForId = getString(item, "voted_for_id");
            Season season = voteRound.getTribal().getEpisode().getSeason();
            Integer seasonNum = season.getSeason();
            
            CastawayPerformance castawayPerf = perfCache.get(seasonNum + ":" + castawayId);
            CastawayPerformance votedForPerf = perfCache.get(seasonNum + ":" + votedForId);
            if (castawayPerf == null || votedForPerf == null) continue;
            
            Vote vote = new Vote();
            vote.setVoteRound(voteRound);
            vote.setCastaway(castawayPerf);
            vote.setVotedFor(votedForPerf);
            vote.setNullified(getBoolean(item, "nullified"));
            votes.add(vote);
        }
        voteRepository.saveAll(votes);
        System.out.println("✓ Loaded " + votes.size() + " votes\n");
    }

    private void loadJuryVotes() throws Exception {
        List<Map<String, Object>> data = loadJsonFile("juryVote.json");
        if (data.isEmpty()) return;

        System.out.println("Loading Jury Votes...");
        List<JuryVote> juryVotes = new ArrayList<>();
        for (Map<String, Object> item : data) {
            Integer seasonNum = getInt(item, "season");
            String castawayId = getString(item, "castaway_id");
            String votedForId = getString(item, "voted_for_id");
            
            Season season = seasonCache.get(seasonNum);
            if (season == null) continue;
            Episode finaleEpisode = episodeRepository.findBySeasonAndIsFinaleTrue(season);
            if (finaleEpisode == null) continue;
            
            CastawayPerformance castawayPerf = perfCache.get(seasonNum + ":" + castawayId);
            CastawayPerformance votedForPerf = perfCache.get(seasonNum + ":" + votedForId);
            if (castawayPerf == null || votedForPerf == null) continue;
            
            JuryVote juryVote = new JuryVote();
            juryVote.setEpisode(finaleEpisode);
            juryVote.setCastaway(castawayPerf);
            juryVote.setVotedFor(votedForPerf);
            juryVotes.add(juryVote);
        }
        juryVoteRepository.saveAll(juryVotes);
        System.out.println("✓ Loaded " + juryVotes.size() + " jury votes\n");
    }

    private void loadJourneys() throws Exception {
        List<Map<String, Object>> data = loadJsonFile("journey.json");
        if (data.isEmpty()) return;

        System.out.println("Loading Journeys...");
        List<Journey> journeys = new ArrayList<>();
        for (Map<String, Object> item : data) {
            Integer seasonNum = getInt(item, "season");
            Integer episodeNum = getInt(item, "episode");
            String castawayId = getString(item, "castaway_id");
            
            Season season = seasonCache.get(seasonNum);
            if (season == null) continue;
            
            Episode episode = episodeRepository.findById(seasonNum * 1000 + episodeNum).orElse(null);
            if (episode == null) continue;

            Castaway castaway = castawayCache.getOrDefault(castawayId, null);
            if (castaway == null) continue;

            CastawayPerformance castawayPerf = perfCache.get(seasonNum + ":" + castawayId);
            if (castawayPerf == null) continue;
            
            Journey journey = new Journey();
            journey.setEpisode(episode);
            journey.setCastaway(castawayPerf);
            journey.setReward(getString(item, "reward"));
            journey.setLostVote(getBoolean(item, "lost_vote"));
            journey.setChoseToPlay(getBoolean(item, "chose_to_play"));
            journey.setEvent(getString(item, "event"));
            journeys.add(journey);
        }
        journeyRepository.saveAll(journeys);
        System.out.println("✓ Loaded " + journeys.size() + " journeys\n");
    }

    private void loadBoots() throws Exception {
        List<Map<String, Object>> data = loadJsonFile("boot.json");
        if (data.isEmpty()) return;

        System.out.println("Loading Boots...");
        List<Boot> boots = new ArrayList<>();
        for (Map<String, Object> item : data) {
            Integer seasonNum = getInt(item, "season");
            Integer episodeNum = getInt(item, "episode");
            String castawayId = getString(item, "castaway_id");
            
            Season season = seasonCache.get(seasonNum);
            if (season == null) continue;

            Episode episode = episodeRepository.findById(seasonNum * 1000 + episodeNum).orElse(null);
            if (episode == null) continue;

            Castaway castaway = castawayCache.getOrDefault(castawayId, null);
            if (castaway == null) continue;
            
            CastawayPerformance castawayPerf = perfCache.get(seasonNum + ":" + castawayId);
            if (castawayPerf == null) continue;
            
            Boot boot = new Boot();
            boot.setEpisode(episode);
            boot.setCastaway(castawayPerf);
            boot.setBootOrder(getInt(item, "boot_order"));
            boot.setEvent(getString(item, "event"));
            boots.add(boot);
        }
        bootRepository.saveAll(boots);
        System.out.println("✓ Loaded " + boots.size() + " boots\n");
    }

    private void loadAdvantageMovements() throws Exception {
        List<Map<String, Object>> data = loadJsonFile("advantageMovement.json");
        if (data.isEmpty()) return;

        System.out.println("Loading Advantage Movements...");
        List<AdvantageMovement> movements = new ArrayList<>();
        for (Map<String, Object> item : data) {
            String castawayId = getString(item, "castaway_id");
            String playedForId = getString(item, "played_for_id");
            Integer seasonNum = getInt(item, "season");
            Integer episodeNum = getInt(item, "episode");
            
            Castaway castaway = castawayCache.getOrDefault(castawayId, null);
            if (castaway == null) continue;
            
            Season season = seasonCache.get(seasonNum);
            if (season == null) continue;
            
            CastawayPerformance castawayPerf = perfCache.get(seasonNum + ":" + castawayId);
            if (castawayPerf == null) continue;
            
            CastawayPerformance playedForPerf = null;
            if (playedForId != null) {
                playedForPerf = perfCache.get(seasonNum + ":" + playedForId);
            }

            Episode episode = episodeRepository.findById(seasonNum * 1000 + episodeNum).orElse(null);
            if (episode == null) continue;
            
            AdvantageMovement movement = new AdvantageMovement();
            movement.setCastawayId(castawayPerf);
            movement.setPlayedForId(playedForPerf);
            movement.setEpisode(episode);
            movement.setEvent(getString(item, "event"));
            movement.setAdvantageType(getString(item, "advantage_type"));
            movement.setSuccess(getString(item, "success"));
            movement.setVotesNullified(getInt(item, "votes_nullified"));
            movements.add(movement);
        }
        advantageMovementRepository.saveAll(movements);
        System.out.println("✓ Loaded " + movements.size() + " advantage movements\n");
    }

    // Helper methods
    private String getString(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : null;
    }

    private Integer getInt(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) return null;
        if (val instanceof Integer) return (Integer) val;
        if (val instanceof Number) return ((Number) val).intValue();
        return null;
    }

    private Boolean getBoolean(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Boolean) return (Boolean) val;
        return null;
    }
}


// ================================================================================
// LOADING SURVIVOR DATA INTO DATABASE
// ================================================================================

// Loading Seasons...
// ✓ Loaded 50 seasons

// Loading Castaways...
// ✓ Loaded 751 castaways

// Loading Castaway Performances...
// ✓ Loaded 917 castaway performances

// Loading Tribes...
// ✓ Loaded 188 tribes
// ✓ Loaded 751 castaways

// Loading Castaway Performances...
// ^C✓ Loaded 917 castaway performances

// Loading Episodes...