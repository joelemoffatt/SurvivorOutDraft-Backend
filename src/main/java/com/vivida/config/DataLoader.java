package com.vivida.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vivida.auth.Role;
import com.vivida.auth.User;
import com.vivida.auth.UserRepository;
import com.vivida.draft.Draft;
import com.vivida.draft.DraftParticipant;
import com.vivida.draft.DraftParticipantRepository;
import com.vivida.draft.DraftPick;
import com.vivida.draft.DraftPickRepository;
import com.vivida.draft.DraftRepository;
import com.vivida.draft.DraftStatus;
import com.vivida.draft.DraftStyle;
import com.vivida.game.advantage.AdvantageMovement;
import com.vivida.game.advantage.AdvantageMovementRepository;
import com.vivida.game.boot.Boot;
import com.vivida.game.boot.BootRepository;
import com.vivida.game.castaway.Castaway;
import com.vivida.game.castaway.CastawayPerformance;
import com.vivida.game.castaway.CastawayPerformanceRepository;
import com.vivida.game.castaway.CastawayRepository;
import com.vivida.game.challenge.Challenge;
import com.vivida.game.challenge.ChallengePerformance;
import com.vivida.game.challenge.ChallengePerformanceRepository;
import com.vivida.game.challenge.ChallengeRepository;
import com.vivida.game.episode.Episode;
import com.vivida.game.episode.EpisodeRepository;
import com.vivida.game.journey.Journey;
import com.vivida.game.journey.JourneyRepository;
import com.vivida.game.juryVote.JuryVote;
import com.vivida.game.juryVote.JuryVoteRepository;
import com.vivida.game.season.Season;
import com.vivida.game.season.SeasonRepository;
import com.vivida.game.tribal.Tribal;
import com.vivida.game.tribal.TribalRepository;
import com.vivida.game.tribe.Tribe;
import com.vivida.game.tribe.TribeMapping;
import com.vivida.game.tribe.TribeMappingRepository;
import com.vivida.game.tribe.TribeRepository;
import com.vivida.game.vote.Vote;
import com.vivida.game.vote.VoteRepository;
import com.vivida.game.vote.VoteRound;
import com.vivida.game.vote.VoteRoundRepository;
import com.vivida.scoring.PointRule;
import com.vivida.scoring.PointRuleRepository;
import com.vivida.scoring.RuleType;
import com.vivida.scoring.ScoreProjectionService;
import com.vivida.social.group.Group;
import com.vivida.social.group.GroupMember;
import com.vivida.social.group.GroupMemberRepository;
import com.vivida.social.group.GroupRepository;
import com.vivida.social.group.GroupStatus;
import com.vivida.social.group.MembershipStatus;
import com.vivida.social.team.Team;
import com.vivida.social.team.TeamCastaway;
import com.vivida.social.team.TeamCastawayRepository;
import com.vivida.social.team.TeamRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;

@ConditionalOnProperty(name = "vivida.dataloader.enabled", havingValue = "true")
@Component
public class DataLoader implements CommandLineRunner {
    @Value("${vivida.dataloader.load-game-data:true}")
    private boolean loadGameData;
    
    @Value("${vivida.dataloader.load-users:true}")
    private boolean loadUsers;
    
    @Value("${vivida.dataloader.load-groups:true}")
    private boolean loadGroups;
    
    // Map vote_round_id to (season, episode, boot_order, vote_order)
    private final Map<Integer, String> voteRoundIdToKey = new HashMap<>();

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
    @Autowired private UserRepository userRepository;
    @Autowired private GroupRepository groupRepository;
    @Autowired private GroupMemberRepository groupMemberRepository;
    @Autowired private TeamRepository teamRepository;
    @Autowired private TeamCastawayRepository teamCastawayRepository;
    @Autowired private DraftRepository draftRepository;
    @Autowired private DraftParticipantRepository draftParticipantRepository;
    @Autowired private DraftPickRepository draftPickRepository;
    @Autowired private PointRuleRepository pointRuleRepository;
    @Autowired private ScoreProjectionService scoreProjectionService;
    @Autowired private PasswordEncoder passwordEncoder;
    
    @PersistenceContext
    private EntityManager entityManager;

    private ObjectMapper mapper = new ObjectMapper();
    private static final String DATA_PATH = "/Users/joelmoffatt/VSCode/SurvivorOutDraft/survivoR/data/class-entities/";
    private static final int BATCH_SIZE = 100;
    
    // Fail counters
    private int totalSeasonFails = 0;
    private int totalEpisodeFails = 0;
    private int totalCastawayFails = 0;
    private int totalPerfFails = 0;
    private int totalTribeFails = 0;
    private int totalTribalFails = 0;
    private int totalChallengeFails = 0;
    
    private Map<String, Castaway> castawayCache = new HashMap<>();
    private Map<Integer, Season> seasonCache = new HashMap<>();
    private Map<String, Tribe> tribeByKeyCache = new HashMap<>();  // key: "seasonId:tribeName"
    private Map<Integer, Episode> episodeCache = new HashMap<>();
    private Map<String, CastawayPerformance> perfCache = new HashMap<>();  // key: "seasonNum:castawayId"
    private Map<String, Challenge> challengeByKey = new HashMap<>();  // key: "seasonNum:challenge_id"
    private Map<String, Tribal> tribalByKey = new HashMap<>();  // key: "seasonNum:episodeNum:bootOrder"

    private List<Map<String, Object>> loadJsonFile(String filename) throws Exception {
        File file = new File(DATA_PATH + filename);
        if (!file.exists()) {
            System.out.println("⚠  " + filename + " not found");
            return new ArrayList<>();
        }
        List<Map<String, Object>> data = mapper.readValue(file, mapper.getTypeFactory().constructCollectionType(List.class, Map.class));
        return data;
    }

    private List<Map<String, Object>> loadJsonFileFromImport(String filename) throws Exception {
        String importPath = "/Users/joelmoffatt/VSCode/SurvivorOutDraft/survivoR/data/import/";
        File file = new File(importPath + filename);
        if (!file.exists()) {
            System.out.println("⚠  " + filename + " not found in import folder");
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
        System.out.println("Flags: Game=" + loadGameData + " | Users=" + loadUsers + " | Groups=" + loadGroups);
        System.out.println("=".repeat(80) + "\n");

        long totalStart = System.currentTimeMillis();
        try {
            clearAllData();
            
            if (loadGameData) {
                System.out.println("[GAME DATA] Loading...");
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
                
                updateSeasonEpisodeCounts();
                
                loadChallenges();
                challengeRepository.findAll().forEach(c ->
                    challengeByKey.put(c.getSeason().getSeason() + ":" + c.getChallenge_id(), c)
                );
                loadTribal();
                tribalRepository.findAll().forEach(t -> 
                    tribalByKey.put(t.getEpisode().getSeason().getSeason() + ":" + t.getEpisode().getEpisodeNumber() + ":" + t.getBootOrder(), t));
                
                loadTribeMapping();
                loadVoteRounds();
                loadChallengePerformances();
                loadVotes();
                loadJuryVotes();
                loadJourneys();
                loadBoots();
                loadAdvantageMovements();
                System.out.println("[GAME DATA] ✓ Complete\n");
            } else {
                System.out.println("[GAME DATA] Skipped - loading from existing database");
                // Still need to populate caches from existing data
                seasonRepository.findAll().forEach(s -> seasonCache.put(s.getSeason(), s));
                castawayRepository.findAll().forEach(c -> castawayCache.put(c.getJson_id(), c));
                castawayPerformanceRepository.findAll().forEach(p -> 
                    perfCache.put(p.getSeason().getSeason() + ":" + p.getCastaway().getJson_id(), p));
                tribeRepository.findAll().forEach(t -> tribeByKeyCache.put(t.getSeason().getSeason() + ":" + t.getName(), t));
                episodeRepository.findAll().forEach(e -> episodeCache.put(e.getId(), e));
                challengeRepository.findAll().forEach(c ->
                    challengeByKey.put(c.getSeason().getSeason() + ":" + c.getChallenge_id(), c));
                tribalRepository.findAll().forEach(t -> 
                    tribalByKey.put(t.getEpisode().getSeason().getSeason() + ":" + t.getEpisode().getEpisodeNumber() + ":" + t.getBootOrder(), t));
            }
            
            if (loadGroups) {
                System.out.println("[GROUP DATA] Loading test groups...");
                seedPointCalcTestData();
                System.out.println("[GROUP DATA] ✓ Complete\n");
            } else {
                System.out.println("[GROUP DATA] Skipped\n");
            }
            
            System.out.println("\n" + "=".repeat(80));
            long totalTime = (System.currentTimeMillis() - totalStart) / 1000;
            System.out.println("✓ DATA LOAD COMPLETE in " + totalTime + " seconds");
            System.out.println("=".repeat(80));
            
            // Print fail summary
            int totalFails = totalSeasonFails + totalEpisodeFails + totalCastawayFails + 
                            totalPerfFails + totalTribeFails + totalTribalFails + totalChallengeFails;
            if (totalFails > 0) {
                System.out.println("\n" + "=".repeat(80));
                System.out.println("FAIL SUMMARY");
                System.out.println("=".repeat(80));
                if (totalSeasonFails > 0) System.out.println("  Season lookup fails:     " + totalSeasonFails);
                if (totalEpisodeFails > 0) System.out.println("  Episode lookup fails:    " + totalEpisodeFails);
                if (totalCastawayFails > 0) System.out.println("  Castaway lookup fails:   " + totalCastawayFails);
                if (totalPerfFails > 0) System.out.println("  Performance lookup fails: " + totalPerfFails);
                if (totalTribeFails > 0) System.out.println("  Tribe lookup fails:      " + totalTribeFails);
                if (totalTribalFails > 0) System.out.println("  Tribal lookup fails:     " + totalTribalFails);
                if (totalChallengeFails > 0) System.out.println("  Challenge lookup fails:  " + totalChallengeFails);
                System.out.println("  " + "-".repeat(40));
                System.out.println("  TOTAL FAILS:             " + totalFails);
                System.out.println("=".repeat(80));
            }
            System.out.println();
        } catch (Exception e) {
            System.err.println("ERROR LOADING DATA: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    private void clearAllData() {
        System.out.println("Clearing data based on flags...");
        
        if (loadGroups) {
            // Clear group/team related data with a single truncate to avoid
            // constantly chasing new foreign-key delete ordering.
            System.out.println("  - Clearing groups, teams, rosters...");
            entityManager.createNativeQuery("""
                    TRUNCATE TABLE
                        draft_picks,
                        draft_participants,
                        draft_castaways,
                        drafts,
                        team_castaways,
                        teams,
                        point_rules,
                        team_castaway_score_events,
                        group_score_calculation_runs,
                        group_members,
                        groups
                    RESTART IDENTITY CASCADE
                    """)
                    .executeUpdate();
            entityManager.flush();
        }
        
        if (loadUsers) {
            System.out.println("  - Clearing users...");
            userRepository.deleteAllInBatch();
            entityManager.flush();
        }
        
        if (loadGameData) {
            // Clear survivor data
            System.out.println("  - Clearing game data (seasons, castaways, episodes, etc.)...");
            advantageMovementRepository.deleteAllInBatch();
            bootRepository.deleteAllInBatch();
            journeyRepository.deleteAllInBatch();
            juryVoteRepository.deleteAllInBatch();
            voteRepository.deleteAllInBatch();
            voteRoundRepository.deleteAllInBatch();
            tribeMappingRepository.deleteAllInBatch();
            challengePerformanceRepository.deleteAllInBatch();
            challengeRepository.deleteAllInBatch();
            tribalRepository.deleteAllInBatch();
            castawayPerformanceRepository.deleteAllInBatch();
            episodeRepository.deleteAllInBatch();
            tribeRepository.deleteAllInBatch();
            castawayRepository.deleteAllInBatch();
            seasonRepository.deleteAllInBatch();
            entityManager.flush();
        }
        
        System.out.println("✓ Data cleared\n");
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

    private User getOrCreateSeedUser(String username, String email, Role defaultRole) {
        User user = userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.findByEmail(email).orElse(null));

        if (user != null) {
            return user;
        }

        User created = new User();
        created.setUsername(username);
        created.setEmail(email);
        created.setPassword(passwordEncoder.encode("123"));
        created.setRole(defaultRole);
        created.setEnabled(true);
        return userRepository.save(created);
    }

    private void loadSeasons() throws Exception {
        List<Map<String, Object>> data = loadJsonFile("season.json");
        if (data.isEmpty()) return;

        System.out.println("Loading Seasons...");
        
        // Load season summary data for enrichment
        List<Map<String, Object>> summaryData = loadJsonFileFromImport("season_summary.json");
        Map<Integer, Map<String, Object>> summaryBySeasonNum = new HashMap<>();
        for (Map<String, Object> summary : summaryData) {
            Integer seasonNum = getInt(summary, "season");
            summaryBySeasonNum.put(seasonNum, summary);
        }
        
        List<Season> seasons = new ArrayList<>();
        for (Map<String, Object> item : data) {
            Season season = new Season();
            Integer seasonNum = getInt(item, "season");
            season.setSeason(seasonNum);
            season.setVersion(getString(item, "version"));
            
            // Enrich with summary data if available
            if (summaryBySeasonNum.containsKey(seasonNum)) {
                Map<String, Object> summary = summaryBySeasonNum.get(seasonNum);
                season.setSeasonName(getString(summary, "season_name"));
                season.setLocation(getString(summary, "location"));
                season.setCountry(getString(summary, "country"));
                season.setTribeSetup(getString(summary, "tribe_setup"));
                season.setFullName(getString(summary, "full_name"));
                season.setFilmingStarted(getString(summary, "filming_started"));
                season.setFilmingEnded(getString(summary, "filming_ended"));
                season.setPremiereDate(getString(summary, "premiered"));
                season.setEndingDate(getString(summary, "ended"));
                season.setViewers(getInt(summary, "viewers_mean"));
            }
            
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
            castaway.setDate_of_death(getString(item, "date_of_death"));
            castaway.setHobbies(getString(item, "hobbies"));
            castaway.setPet_peeves(getString(item, "pet_peeves"));
            castaway.setThree_words(getString(item, "three_words"));
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
                System.out.println("  ⚠ CastawayPerformance: Season not found - season=" + seasonNum);
                continue;
            }
            if (castaway == null) {
                castawayFails++;
                System.out.println("  ⚠ CastawayPerformance: Castaway not found - castaway_id=" + castawayId);
                continue;
            }
            
            CastawayPerformance perf = new CastawayPerformance();
            perf.setSeason(season);
            perf.setCastaway(castaway);
            performances.add(perf);
        }
        saveInBatches(performances, "castaway performances");
        totalSeasonFails += seasonFails;
        totalCastawayFails += castawayFails;
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

    private void updateSeasonEpisodeCounts() {
        System.out.println("Updating season episode counts...");
        List<Season> seasons = seasonRepository.findAll();
        for (Season season : seasons) {
            long episodeCount = episodeRepository.findBySeasonId(season.getSeason()).size();
            season.setEpisodesNumber((int) episodeCount);
            seasonRepository.save(season);
        }
        System.out.println("✓ Updated episode counts for " + seasons.size() + " seasons\n");
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
                System.out.println("  ⚠ Tribal: Season not found - season=" + seasonNum);
                continue;
            }
            
            Episode episode = episodeRepository.findById(seasonNum * 1000 + episodeNum).orElse(null);
            if (episode == null) {
                episodeFails++;
                System.out.println("  ⚠ Tribal: Episode not found - season=" + seasonNum + ", episode=" + episodeNum);
                continue;
            }
            
            String tribeKey = seasonNum + ":" + tribeName;
            Tribe tribe = tribeByKeyCache.get(tribeKey);
            if (tribe == null) {
                tribeFails++;
                System.out.println("  ⚠ Tribal: Tribe not found - season=" + seasonNum + ", tribe=" + tribeName);
                continue;
            }
            
            Tribal tribal = new Tribal();
            tribal.setEpisode(episode);
            tribal.setTribe(tribe);
            tribal.setBootOrder(getInt(item, "boot_order"));
            tribals.add(tribal);
        }
        tribalRepository.saveAll(tribals);
        totalSeasonFails += seasonFails;
        totalEpisodeFails += episodeFails;
        totalTribeFails += tribeFails;
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
            
            Challenge challenge = challengeByKey.getOrDefault(seasonNum + ":" + challengeId, null);
            if (challenge == null) {
                challengeFails++;
                System.out.println("  ⚠ ChallengePerformance: Challenge not found - challenge_id=" + challengeId);
                continue;
            }
            
            Castaway castaway = castawayCache.getOrDefault(castawayId, null);
            if (castaway == null) {
                castawayFails++;
                System.out.println("  ⚠ ChallengePerformance: Castaway not found - castaway_id=" + castawayId);
                continue;
            }
            
            CastawayPerformance castawayPerf = perfCache.get(seasonNum + ":" + castawayId);
            if (castawayPerf == null) {
                perfFails++;
                System.out.println("  ⚠ ChallengePerformance: Performance not found - season=" + seasonNum + ", castaway_id=" + castawayId);
                continue;
            }
            
            ChallengePerformance perf = new ChallengePerformance();
            perf.setChallenge(challenge);
            perf.setCastaway(castawayPerf);
            perf.setPlace(getInt(item, "place"));
            perf.setSatOut(getBoolean(item, "sat_out"));
            perf.setWon(getBoolean(item, "won"));
            perf.setWonIndividualImmunity(getBoolean(item, "won_individual_immunity"));
            perf.setWonTeamImmunity(getBoolean(item, "won_team_immunity"));
            perf.setWonIndividualReward(getBoolean(item, "won_individual_reward"));
            perf.setWonTeamReward(getBoolean(item, "won_team_reward"));
            performances.add(perf);
        }
        challengePerformanceRepository.saveAll(performances);
        totalChallengeFails += challengeFails;
        totalCastawayFails += castawayFails;
        totalPerfFails += perfFails;
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
                System.out.println("  ⚠ TribeMapping: Season not found - season=" + seasonNum);
                continue;
            }
            
            Episode episode = episodeRepository.findById(seasonNum * 1000 + episodeNum).orElse(null);
            if (episode == null) {
                episodeFails++;
                System.out.println("  ⚠ TribeMapping: Episode not found - season=" + seasonNum + ", episode=" + episodeNum);
                continue;
            }
            
            Castaway castaway = castawayCache.getOrDefault(castawayId, null);
            if (castaway == null) {
                castawayFails++;
                System.out.println("  ⚠ TribeMapping: Castaway not found - castaway_id=" + castawayId);
                continue;
            }
            
            CastawayPerformance castawayPerf = perfCache.get(seasonNum + ":" + castawayId);
            if (castawayPerf == null) {
                perfFails++;
                System.out.println("  ⚠ TribeMapping: Performance not found - season=" + seasonNum + ", castaway_id=" + castawayId);
                continue;
            }
            
            String tribeKey = seasonNum + ":" + tribeName;
            Tribe oldTribe = tribeByKeyCache.get(tribeKey);
            if (oldTribe == null) {
                tribeFails++;
                System.out.println("  ⚠ TribeMapping: Tribe not found - season=" + seasonNum + ", tribe=" + tribeName);
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
        totalSeasonFails += seasonFails;
        totalEpisodeFails += episodeFails;
        totalCastawayFails += castawayFails;
        totalPerfFails += perfFails;
        totalTribeFails += tribeFails;
        System.out.println("✓ Loaded " + mappings.size() + " tribe mappings (season=" + seasonFails + ", episode=" + episodeFails + ", castaway=" + castawayFails + ", perf=" + perfFails + ", tribe=" + tribeFails + ")\n");
    }

    // Map for fast lookup: season:episode:bootOrder:voteOrder -> VoteRound
    private final Map<String, VoteRound> voteRoundByKey = new HashMap<>();

    private void loadVoteRounds() throws Exception {
        List<Map<String, Object>> data = loadJsonFile("voteRound.json");
        if (data.isEmpty()) return;

        System.out.println("Loading Vote Rounds...");
        List<VoteRound> voteRounds = new ArrayList<>();
        int tribalFails = 0;
        for (Map<String, Object> item : data) {
            Integer seasonNum = getInt(item, "season");
            Integer episodeNum = getInt(item, "episode");
            Integer bootOrder = getInt(item, "boot_order");
            Integer voteOrder = getInt(item, "vote_order");
            Integer id = getInt(item, "id");

            Season season = seasonCache.get(seasonNum);
            if (season == null) continue;

            String tribalKey = seasonNum + ":" + episodeNum + ":" + bootOrder;
            Tribal tribal = tribalByKey.get(tribalKey);
            if (tribal == null) {
                tribalFails++;
                System.out.println("  ⚠ VoteRound: Tribal not found - season=" + seasonNum + ", episode=" + episodeNum + ", bootOrder=" + bootOrder);
                continue;
            }

            VoteRound voteRound = new VoteRound();
            voteRound.setTribal(tribal);
            voteRound.setIsTie(getBoolean(item, "is_tie"));
            voteRound.setVoteOrder(voteOrder);
            voteRounds.add(voteRound);

            // Map for lookup by season:episode:bootOrder:voteOrder
            String voteRoundKey = seasonNum + ":" + episodeNum + ":" + bootOrder + ":" + voteOrder;
            voteRoundByKey.put(voteRoundKey, voteRound);
            if (id != null) {
                voteRoundIdToKey.put(id, voteRoundKey);
            }
        }
        voteRoundRepository.saveAll(voteRounds);
        totalTribalFails += tribalFails;
        System.out.println("✓ Loaded " + voteRounds.size() + " vote rounds (tribal_fails=" + tribalFails + ")\n");
    }

    private void loadVotes() throws Exception {
        List<Map<String, Object>> data = loadJsonFile("vote.json");
        if (data.isEmpty()) return;

        System.out.println("Loading Votes...");
        List<Vote> votes = new ArrayList<>();
        int skippedVoteRound = 0;
        int skippedCastaway = 0;
        int skippedVotedFor = 0;
        for (Map<String, Object> item : data) {
            Integer voteRoundId = getInt(item, "vote_round_id");
            String voteRoundKey = voteRoundIdToKey.get(voteRoundId);
            if (voteRoundKey == null) {
                System.out.println("⚠  Skipping vote: voteRoundId " + voteRoundId + " not found in voteRoundIdToKey map");
                skippedVoteRound++;
                continue;
            }
            VoteRound voteRound = voteRoundByKey.get(voteRoundKey);
            if (voteRound == null) {
                System.out.println("⚠  Skipping vote: voteRound not found for key " + voteRoundKey);
                skippedVoteRound++;
                continue;
            }

            String castawayId = getString(item, "castaway_id");
            String votedForId = getString(item, "voted_for_id");
            Season season = voteRound.getTribal().getEpisode().getSeason();
            Integer seasonNum2 = season.getSeason();

            CastawayPerformance castawayPerf = perfCache.get(seasonNum2 + ":" + castawayId);
            if (castawayPerf == null) {
                System.out.println("⚠  Skipping vote: castawayPerf not found for season=" + seasonNum2 + ", castaway_id=" + castawayId);
                skippedCastaway++;
                continue;
            }
            CastawayPerformance votedForPerf = perfCache.get(seasonNum2 + ":" + votedForId);
            if (votedForPerf == null) {
                System.out.println("⚠  Skipping vote: votedForPerf not found for season=" + seasonNum2 + ", voted_for_id=" + votedForId);
                skippedVotedFor++;
                continue;
            }

            Vote vote = new Vote();
            vote.setVoteRound(voteRound);
            vote.setCastaway(castawayPerf);
            vote.setVotedFor(votedForPerf);
            vote.setNullified(getBoolean(item, "nullified"));
            votes.add(vote);
        }
        voteRepository.saveAll(votes);
        System.out.println("✓ Loaded " + votes.size() + " votes\n");
        System.out.println("Skipped votes: " + skippedVoteRound + " (vote_round_id), " + skippedCastaway + " (castaway_id), " + skippedVotedFor + " (voted_for_id)\n");
    }

    private void loadJuryVotes() throws Exception {
        List<Map<String, Object>> data = loadJsonFile("juryVote.json");
        if (data.isEmpty()) return;

        System.out.println("Loading Jury Votes...");
        // Build finale lookup once to avoid per-row DB queries.
        Map<Integer, Episode> finaleBySeason = new HashMap<>();
        for (Episode episode : episodeCache.values()) {
            if (Boolean.TRUE.equals(episode.getIsFinale())) {
                finaleBySeason.put(episode.getSeason().getSeason(), episode);
            }
        }
        List<JuryVote> juryVotes = new ArrayList<>();
        for (Map<String, Object> item : data) {
            Integer seasonNum = getInt(item, "season");
            String castawayId = getString(item, "castaway_id");
            String votedForId = getString(item, "voted_for_id");
            
            Season season = seasonCache.get(seasonNum);
            if (season == null) continue;
            Episode finaleEpisode = finaleBySeason.get(seasonNum);
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
        int seasonFails = 0, episodeFails = 0, castawayFails = 0, perfFails = 0, tribalFails = 0;
        for (Map<String, Object> item : data) {
            Integer seasonNum = getInt(item, "season");
            Integer episodeNum = getInt(item, "episode");
            String castawayId = getString(item, "castaway_id");
            
            Season season = seasonCache.get(seasonNum);
            if (season == null) {
                seasonFails++;
                System.out.println("  ⚠ Boot: Season not found - season=" + seasonNum);
                continue;
            }

            Episode episode = episodeRepository.findById(seasonNum * 1000 + episodeNum).orElse(null);
            if (episode == null) {
                episodeFails++;
                System.out.println("  ⚠ Boot: Episode not found - season=" + seasonNum + ", episode=" + episodeNum);
                continue;
            }

            Castaway castaway = castawayCache.getOrDefault(castawayId, null);
            if (castaway == null) {
                castawayFails++;
                System.out.println("  ⚠ Boot: Castaway not found - castaway_id=" + castawayId);
                continue;
            }
            
            CastawayPerformance castawayPerf = perfCache.get(seasonNum + ":" + castawayId);
            if (castawayPerf == null) {
                perfFails++;
                System.out.println("  ⚠ Boot: Performance not found - season=" + seasonNum + ", castaway_id=" + castawayId);
                continue;
            }
            
            String event = getString(item, "event");
            
            // Look up tribal council for events that happen at tribal council
            // Events at tribal: votedOut, eliminated, lostFire (final 4 fire), tiedDestiny
            // Events NOT at tribal: medEvac, quit, ejected, lostFinalFire (finale fire), first/second/third, switched, other
            Tribal tribal = null;
            boolean hasTribal = event != null && (
                event.equals("votedOut") || 
                event.equals("eliminated") || 
                event.equals("lostFire") ||
                event.equals("tiedDestiny")
            );
            
            if (hasTribal) {
                Integer bootOrder = getInt(item, "boot_order");
                tribal = tribalByKey.get(seasonNum + ":" + episodeNum + ":" + bootOrder);
                if (tribal == null) {
                    tribalFails++;
                    System.out.println("  ⚠ Boot: Tribal not found for tribal event - season=" + seasonNum + ", episode=" + episodeNum + ", bootOrder=" + bootOrder + ", event=" + event);
                }
            }
            
            Boot boot = new Boot();
            boot.setEpisode(episode);
            boot.setCastaway(castawayPerf);
            boot.setBootOrder(getInt(item, "boot_order"));
            boot.setEvent(event);
            boot.setTribal(tribal);
            boots.add(boot);
        }
        bootRepository.saveAll(boots);
        totalSeasonFails += seasonFails;
        totalEpisodeFails += episodeFails;
        totalCastawayFails += castawayFails;
        totalPerfFails += perfFails;
        totalTribalFails += tribalFails;
        System.out.println("✓ Loaded " + boots.size() + " boots (season_fails=" + seasonFails + ", episode_fails=" + episodeFails + ", castaway_fails=" + castawayFails + ", perf_fails=" + perfFails + ", tribal_fails=" + tribalFails + ")\n");
    }

    private void seedPointCalcTestData() {
        System.out.println("═".repeat(80));
        System.out.println("SEEDING TEST DATA GROUPS");
        System.out.println("═".repeat(80) + "\n");

        // ==================== SECTION 1: SEASON 50 - HARDCODED TEAM ROSTERS ====================
        System.out.println("[SECTION 1] Season 46-50 - Jeff's Probst Fan Club (Hardcoded Teams)\n");
        Map<String, List<String>> season46SRosters = new LinkedHashMap<>();
        season46SRosters.put("joel", Arrays.asList("venus", "bhanu", "jem"));
        season46SRosters.put("devin", Arrays.asList("q", "randen", "tim"));
        season46SRosters.put("kaitlin", Arrays.asList("maria", "ben", "moriah"));
        season46SRosters.put("kc", Arrays.asList("tevin", "tiffany", "charlie"));
        season46SRosters.put("jess", Arrays.asList("kenzie", "hunter", "soda"));
        
        seedJeffsProbstFanClub(46, "Jeff's Probst Fan Club 46", season46SRosters);
        
        Map<String, List<String>> season47SRosters = new LinkedHashMap<>();
        season47SRosters.put("joel", Arrays.asList("kyle", "sierra", "caroline", "kishan"));
        season47SRosters.put("mckenna", Arrays.asList("tk", "aysha", "sierra", "sol"));
        season47SRosters.put("devin", Arrays.asList("kyle", "aysha", "genevieve", "teeny"));
        season47SRosters.put("kaitlin", Arrays.asList("kyle", "sierra", "caroline", "sol"));
        season47SRosters.put("kc", Arrays.asList("sierra", "rachel", "kishan", "gabe"));
        season47SRosters.put("jess", Arrays.asList("kyle", "tiyana", "anika", "kishan"));
        
        seedJeffsProbstFanClub(47, "Jeff's Probst Fan Club 47", season47SRosters);
        
        Map<String, List<String>> season48SRosters = new LinkedHashMap<>();
        season48SRosters.put("joel", Arrays.asList("thomas", "kamilla", "kevin", "shauhin"));
        season48SRosters.put("mckenna", Arrays.asList("joe", "mitch", "mary", "star"));
        season48SRosters.put("devin", Arrays.asList("mary", "mitch", "chrissy", "star"));
        season48SRosters.put("kc", Arrays.asList("joe", "mitch", "shauhin", "mary"));
        season48SRosters.put("kaitlin", Arrays.asList("kyle", "chrissy", "bianca", "cedrek"));
        season48SRosters.put("jess", Arrays.asList("kyle", "eva", "kevin", "thomas"));
        
        seedJeffsProbstFanClub(48, "Jeff's Probst Fan Club 48", season48SRosters);
        
        Map<String, List<String>> season49S49Rosters = new LinkedHashMap<>();
        season49S49Rosters.put("joel", Arrays.asList("sophie", "nate", "savannah"));
        season49S49Rosters.put("mckenna", Arrays.asList("jawan", "jake", "mc"));
        season49S49Rosters.put("devin", Arrays.asList("steven", "sage", "nate"));
        season49S49Rosters.put("kaitlin", Arrays.asList("shannon", "rizo", "jeremiah"));
        season49S49Rosters.put("kc", Arrays.asList("jason", "kristina", "alex"));
        season49S49Rosters.put("jess", Arrays.asList("sophi", "matt", "annie"));
        
        seedJeffsProbstFanClub(49, "Jeff's Probst Fan Club 49", season49S49Rosters);
        
        Map<String, List<String>> season50Rosters = new LinkedHashMap<>();
        season50Rosters.put("joel", Arrays.asList("Jonathan", "Kamilla", "Rizo", "Charlie"));
        season50Rosters.put("jess", Arrays.asList("Cirie", "Joe", "Q", "Rick"));
        season50Rosters.put("mckenna", Arrays.asList("Colby", "Mike", "Savannah", "Christian"));
        season50Rosters.put("kc", Arrays.asList("Charlie", "Christian", "Ozzy", "Stephenie"));
        season50Rosters.put("kaitlin", Arrays.asList("Aubry", "Coach", "Dee", "Tiffany"));
        season50Rosters.put("devin", Arrays.asList("Angelina", "Chrissy", "Emily", "Genevieve"));
        
        seedJeffsProbstFanClub(50, "Jeff's Probst Fan Club 50", season50Rosters);

        System.out.println("═".repeat(80) + "\n");
        
        // ==================== SECTION 2: SEASONS 47-49 - COLOR TEAMS WITH RANDOM ROSTERS ====================
        System.out.println("[SECTION 2] Seasons 47-49 - Colors (Red/Yellow/Blue/Green with Random Teams)\n");
        for (int season : Arrays.asList(47, 48, 49)) {
            seedColorTeamGroup(season, "Colors S" + season);
        }

        recalculateSeededGroupScores();
        
        System.out.println("═".repeat(80));
        System.out.println("✓ TEST DATA SEEDING COMPLETE");
        System.out.println("═".repeat(80) + "\n");
    }

    private void recalculateSeededGroupScores() {
        System.out.println("[SECTION 3] Recalculating seeded group scores\n");

        int recalculated = 0;
        int skipped = 0;
        int failed = 0;

        List<Group> groups = groupRepository.findAll();
        for (Group group : groups) {
            List<TeamCastaway> roster = teamCastawayRepository.findByTeamGroupIdOrderByDraftOrderAsc(group.getId());
            if (roster.isEmpty()) {
                skipped++;
                continue;
            }

            try {
                scoreProjectionService.recalculateGroupScores(group);
                recalculated++;
                System.out.println("  ✓ Recalculated group: " + group.getName() + " (id=" + group.getId() + ")");
            } catch (Exception e) {
                failed++;
                System.out.println("  ⚠ Failed to recalculate group: " + group.getName() + " (id=" + group.getId() + ") - " + e.getMessage());
            }
        }

        System.out.println("\n  Recalculated: " + recalculated);
        System.out.println("  Skipped (no roster): " + skipped);
        System.out.println("  Failed: " + failed + "\n");

        if (failed > 0) {
            throw new RuntimeException("Score recalculation failed for " + failed + " seeded group(s)");
        }
    }

    private void seedSeason50PendingGroup(String groupName, List<String> usernames) {
        if (groupRepository.findByName(groupName).isPresent()) {
            System.out.println("✓ Group already exists: " + groupName + " (skipping)\n");
            return;
        }

        Season season = seasonCache.get(50);
        if (season == null) {
            System.out.println("⚠  Season 50 not found\n");
            return;
        }

        System.out.println("Creating: " + groupName);
        System.out.println("  Season: 50");
        System.out.println("  Status: PENDING\n");

        List<User> users = new ArrayList<>();
        for (String username : usernames) {
            User user = getOrCreateSeedUser(username, username + "@example.com", Role.USER);
            users.add(user);
        }

        Group group = new Group();
        group.setName(groupName);
        group.setAdmin(users.get(0));
        group.setSeason(season);
        group.setStatus(GroupStatus.PENDING);
        group.setTeamSize(10);
        Episode latestWatchedEpisode = episodeCache.get(50003);
        group.setLatestEpisodeWatched(latestWatchedEpisode);
        applyCompletedStatusIfFinale(group, latestWatchedEpisode);
        groupRepository.save(group);

        Map<Integer, Team> teamByUserId = new HashMap<>();
        for (User user : users) {
            GroupMember member = new GroupMember();
            member.setGroup(group);
            member.setUser(user);
            member.setStatus(MembershipStatus.ACCEPTED);
            groupMemberRepository.save(member);

            Team team = new Team();
            team.setGroup(group);
            team.setUser(user);
            team.setTeamName("Team " + user.getUsername());
            teamRepository.save(team);
            teamByUserId.put(user.getId(), team);
        }

        List<PointRule> rules = new ArrayList<>();
        rules.add(buildPointRule(group, RuleType.INDIVIDUAL_IMMUNITY, 2, "Individual immunity wins"));
        rules.add(buildPointRule(group, RuleType.FOUND_IDOL, 1, "Found idol"));
        rules.add(buildPointRule(group, RuleType.FOUND_ADVANTAGE, 1, "Found advantage"));
        rules.add(buildPointRule(group, RuleType.SOLE_SURVIVOR, 5, "Sole Survivor"));
        rules.add(buildPointRule(group, RuleType.RUNNER_UP, 2, "Runner-up"));
        rules.add(buildPointRule(group, RuleType.MADE_MERGE, 1, "Made the merge"));
        rules.add(buildPointRule(group, RuleType.MED_EVAC, -2, "Med evac"));
        rules.add(buildPointRule(group, RuleType.QUIT, -2, "Quit"));
        pointRuleRepository.saveAll(rules);

        createDraftForPendingGroup(group, DraftStyle.SNAKE);

        System.out.println("✓ Created: " + groupName + " (Season 50, PENDING)\n");
    }

    private void seedJeffsProbstFanClub(Integer seasonNum, String groupName, Map<String, List<String>> userRosters) {
        Optional<Group> existingGroupOpt = groupRepository.findByName(groupName);
        if (existingGroupOpt.isPresent()) {
            Group existingGroup = existingGroupOpt.get();
            Episode latestWatchedEpisode = null;
            if (seasonNum != null) {
                if (seasonNum == 46) {
                    latestWatchedEpisode = episodeCache.get(46013);
                } else if (seasonNum == 47) {
                    latestWatchedEpisode = episodeCache.get(47014);
                } else if (seasonNum == 48) {
                    latestWatchedEpisode = episodeCache.get(48013);
                } else if (seasonNum == 49) {
                    latestWatchedEpisode = episodeCache.get(49013);
                } else if (seasonNum == 50) {
                    latestWatchedEpisode = episodeCache.get(50003);
                }
            }
            if (latestWatchedEpisode != null) {
                existingGroup.setLatestEpisodeWatched(latestWatchedEpisode);
                applyCompletedStatusIfFinale(existingGroup, latestWatchedEpisode);
                groupRepository.save(existingGroup);
            }
            System.out.println("✓ Group already exists: " + groupName + " (updated watched episode)\n");
            return;
        }

        Season season = seasonCache.get(seasonNum);
        if (season == null) {
            System.out.println("⚠  Season " + seasonNum + " not found\n");
            return;
        }

        System.out.println("Creating: " + groupName);
        System.out.println("  Season: " + seasonNum);
        System.out.println("  Teams: " + userRosters.size() + " with hardcoded rosters\n");

        // Create/fetch users
        List<User> users = new ArrayList<>();
        for (String username : userRosters.keySet()) {
            User user = getOrCreateSeedUser(username, username + "@example.com", Role.USER);
            users.add(user);
        }

        // Create group
        Group group = new Group();
        group.setName(groupName);
        group.setAdmin(users.get(0));
        group.setSeason(season);
        group.setStatus(GroupStatus.ACTIVE);
        group.setTeamSize(4);  // 4 castaways per team for hardcoded rosters
        Episode latestWatchedEpisode = null;
        if (seasonNum != null) {
            if (seasonNum == 46) {
                latestWatchedEpisode = episodeCache.get(46013);
            } else if (seasonNum == 47) {
                latestWatchedEpisode = episodeCache.get(47014);
            } else if (seasonNum == 48) {
                latestWatchedEpisode = episodeCache.get(48013);
            } else if (seasonNum == 49) {
                latestWatchedEpisode = episodeCache.get(49013);
            } else if (seasonNum == 50) {
                latestWatchedEpisode = episodeCache.get(50003);
            }
        }
        group.setLatestEpisodeWatched(latestWatchedEpisode);
        applyCompletedStatusIfFinale(group, latestWatchedEpisode);
        groupRepository.save(group);

        // Add users to group
        for (User user : users) {
            GroupMember member = new GroupMember();
            member.setGroup(group);
            member.setUser(user);
            member.setStatus(MembershipStatus.ACCEPTED);
            groupMemberRepository.save(member);
        }

        // Add point rules
        List<PointRule> rules = new ArrayList<>();
        rules.add(buildPointRule(group, RuleType.INDIVIDUAL_IMMUNITY, 2, "Individual immunity wins"));
        rules.add(buildPointRule(group, RuleType.FOUND_IDOL, 1, "Found idol"));
        rules.add(buildPointRule(group, RuleType.FOUND_ADVANTAGE, 1, "Found advantage"));
        rules.add(buildPointRule(group, RuleType.SOLE_SURVIVOR, 5, "Sole Survivor"));
        rules.add(buildPointRule(group, RuleType.RUNNER_UP, 2, "Runner-up"));
        rules.add(buildPointRule(group, RuleType.MADE_MERGE, 1, "Made the merge"));
        rules.add(buildPointRule(group, RuleType.MED_EVAC, -2, "Med evac"));
        rules.add(buildPointRule(group, RuleType.QUIT, -2, "Quit"));
        pointRuleRepository.saveAll(rules);

        // Create teams and assign hardcoded castaways
        List<DraftSeedPick> seededPicks = new ArrayList<>();
        for (User user : users) {
            Team team = new Team();
            team.setGroup(group);
            team.setUser(user);
            team.setTeamName("Team " + user.getUsername());
            teamRepository.save(team);
            
            // Assign pre-configured castaways
            List<String> rosterNames = userRosters.get(user.getUsername());
            if (rosterNames != null) {
                for (String castawayName : rosterNames) {
                    CastawayPerformance perf = findCastawayPerformanceByName(castawayName, seasonNum);
                    if (perf != null) {
                        TeamCastaway tc = new TeamCastaway();
                        tc.setTeam(team);
                        tc.setCastawayPerformance(perf);
                        tc.setDraftOrder(seededPicks.size() + 1);
                        tc.setDraftedAt(LocalDateTime.now());
                        teamCastawayRepository.save(tc);
                        seededPicks.add(new DraftSeedPick(user, team, perf));
                    }
                }
            }
        }

        createCompletedDraftForGroup(group, users, seededPicks, DraftStyle.LINEAR);

        System.out.println("✓ Created: " + groupName + " with hardcoded team rosters\n");
    }

    private void seedColorTeamGroup(Integer seasonNum, String groupName) {
        if (groupRepository.findByName(groupName).isPresent()) {
            System.out.println("✓ Group already exists: " + groupName + " (skipping)\n");
            return;
        }

        Season season = seasonCache.get(seasonNum);
        if (season == null) {
            System.out.println("⚠  Season " + seasonNum + " not found\n");
            return;
        }

        System.out.println("Creating: " + groupName);
        System.out.println("  Season: " + seasonNum);
        System.out.println("  Teams: 4 color teams with random rosters\n");

        // Create/fetch users for color teams
        List<String> colorNames = Arrays.asList("red", "yellow", "blue", "green");
        List<User> users = new ArrayList<>();
        for (String colorName : colorNames) {
            User user = getOrCreateSeedUser(
                    colorName,
                    colorName + "@example.com",
                    colorName.equals("blue") ? Role.ADMIN : Role.USER
            );
            users.add(user);
        }

        // Create group
        Group group = new Group();
        group.setName(groupName);
        group.setAdmin(users.get(0));
        group.setSeason(season);
        group.setStatus(GroupStatus.COMPLETED);
        group.setTeamSize(0);
        groupRepository.save(group);

        // Add users to group
        for (User user : users) {
            GroupMember member = new GroupMember();
            member.setGroup(group);
            member.setUser(user);
            member.setStatus(MembershipStatus.ACCEPTED);
            groupMemberRepository.save(member);
        }

        // Add point rules
        List<PointRule> rules = new ArrayList<>();
        rules.add(buildPointRule(group, RuleType.INDIVIDUAL_IMMUNITY, 2, "Individual immunity wins"));
        rules.add(buildPointRule(group, RuleType.FOUND_IDOL, 1, "Found idol"));
        rules.add(buildPointRule(group, RuleType.FOUND_ADVANTAGE, 1, "Found advantage"));
        rules.add(buildPointRule(group, RuleType.SOLE_SURVIVOR, 5, "Sole Survivor"));
        rules.add(buildPointRule(group, RuleType.RUNNER_UP, 2, "Runner-up"));
        rules.add(buildPointRule(group, RuleType.MADE_MERGE, 1, "Made the merge"));
        rules.add(buildPointRule(group, RuleType.MED_EVAC, -2, "Med evac"));
        rules.add(buildPointRule(group, RuleType.QUIT, -2, "Quit"));
        pointRuleRepository.saveAll(rules);

        // Get available castaways for this season
        List<CastawayPerformance> seasonPerformances = new ArrayList<>();
        for (CastawayPerformance perf : perfCache.values()) {
            if (perf.getSeason().getSeason().equals(seasonNum)) {
                seasonPerformances.add(perf);
            }
        }

        if (seasonPerformances.isEmpty()) {
            System.out.println("⚠  No castaways found for season " + seasonNum + "\n");
            return;
        }

        // Shuffle castaways randomly
        Collections.shuffle(seasonPerformances);
        int totalCastaways = seasonPerformances.size();
        int numTeams = users.size();
        int castawaysPerTeam = totalCastaways / numTeams;
        group.setTeamSize(castawaysPerTeam);
        groupRepository.save(group);
        
        System.out.println("  Total castaways: " + totalCastaways + " | Teams: " + numTeams + " | Per team: " + castawaysPerTeam);
        
        // Create teams first
        List<Team> teams = new ArrayList<>();
        for (User user : users) {
            Team team = new Team();
            team.setGroup(group);
            team.setUser(user);
            team.setTeamName("Team " + user.getUsername());
            teamRepository.save(team);
            teams.add(team);
        }
        
        // Distribute castaways using snake draft pattern
        int draftOrder = 1;
        List<DraftSeedPick> seededPicks = new ArrayList<>();
        for (int pickNum = 0; pickNum < totalCastaways; pickNum++) {
            // Calculate team position using snake draft logic
            int roundNum = pickNum / numTeams;
            int position;
            if (roundNum % 2 == 0) {
                // Even rounds: forward (0, 1, 2, 3)
                position = pickNum % numTeams;
            } else {
                // Odd rounds: backward (3, 2, 1, 0)
                position = numTeams - 1 - (pickNum % numTeams);
            }
            
            Team team = teams.get(position);
            CastawayPerformance perf = seasonPerformances.get(pickNum);
            
            TeamCastaway tc = new TeamCastaway();
            tc.setTeam(team);
            tc.setCastawayPerformance(perf);
            tc.setDraftOrder(draftOrder++);
            tc.setDraftedAt(LocalDateTime.now());
            teamCastawayRepository.save(tc);
            seededPicks.add(new DraftSeedPick(team.getUser(), team, perf));
        }

        createCompletedDraftForGroup(group, users, seededPicks, DraftStyle.SNAKE);

        System.out.println("✓ Created: " + groupName + " with random team rosters\n");
    }

    private CastawayPerformance findCastawayPerformanceByName(String castawayName, Integer seasonNum) {
        for (CastawayPerformance perf : perfCache.values()) {
            if (perf.getSeason().getSeason().equals(seasonNum)) {
                String perfName = perf.getCastaway().getName();
                String fullName = perf.getCastaway().getFull_name();
                if (perfName != null && perfName.equalsIgnoreCase(castawayName)) {
                    return perf;
                }
                if (fullName != null && fullName.toLowerCase().contains(castawayName.toLowerCase())) {
                    return perf;
                }
            }
        }
        return null;
    }

    private List<CastawayPerformance> getSeasonCastawayPerformances(Integer seasonNum) {
        return castawayPerformanceRepository.findBySeasonId(seasonNum);
    }

    private void createDraftForPendingGroup(Group group,
                                            DraftStyle style) {
        int teamSize = group.getTeamSize() != null ? group.getTeamSize() : 0;

        Draft draft = new Draft();
        draft.setGroup(group);
        draft.setSeason(group.getSeason());
        draft.setCreatedBy(group.getAdmin());
        draft.setStatus(DraftStatus.PENDING);
        draft.setStyle(style != null ? style : DraftStyle.SNAKE);
        draft.setScheduledAt(LocalDateTime.now().plusMinutes(1));
        draft.setStartedAt(null);
        draft.setTeamSize(teamSize);
        draft.setTotalParticipants(0);
        draft.setTotalCastaways(0);
        draft.setMaxDraftsPerCastaway(1);
        draft.setTotalPicks(0);
        draft.setCurrentPickNumber(1);
        draft.setCurrentTurnUser(null);
        draftRepository.save(draft);
        group.setDraft(draft);
        groupRepository.save(group);
    }

    private void createCompletedDraftForGroup(Group group,
                                              List<User> participantsInOrder,
                                              List<DraftSeedPick> seededPicks,
                                              DraftStyle style) {
        if (participantsInOrder.isEmpty() || seededPicks.isEmpty()) {
            return;
        }

        int totalParticipants = participantsInOrder.size();
        int computedTeamSize = Math.max(1, seededPicks.size() / totalParticipants);
        int teamSize = group.getTeamSize() != null && group.getTeamSize() > 0
                ? group.getTeamSize()
                : computedTeamSize;

        if (group.getTeamSize() == null || group.getTeamSize() == 0) {
            group.setTeamSize(teamSize);
            groupRepository.save(group);
        }

        Map<Integer, Integer> participantPositionByUserId = new HashMap<>();
        for (int i = 0; i < participantsInOrder.size(); i++) {
            participantPositionByUserId.put(participantsInOrder.get(i).getId(), i);
        }

        Map<Integer, Integer> picksMadeByUserId = new HashMap<>();
        for (DraftSeedPick seedPick : seededPicks) {
            Integer userId = seedPick.user.getId();
            picksMadeByUserId.put(userId, picksMadeByUserId.getOrDefault(userId, 0) + 1);
        }

        LocalDateTime startedAt = LocalDateTime.now().minusMinutes(2);
        LocalDateTime completedAt = LocalDateTime.now().minusMinutes(1);

        Draft draft = new Draft();
        draft.setGroup(group);
        draft.setSeason(group.getSeason());
        draft.setCreatedBy(group.getAdmin());
        draft.setStatus(DraftStatus.COMPLETED);
        draft.setStyle(style != null ? style : DraftStyle.SNAKE);
        draft.setScheduledAt(startedAt.minusMinutes(1));
        draft.setStartedAt(startedAt);
        draft.setCompletedAt(completedAt);
        draft.setTeamSize(teamSize);
        draft.setTotalParticipants(totalParticipants);
        draft.setTotalCastaways(getSeasonCastawayPerformances(group.getSeason().getSeason()).size());
        draft.setMaxDraftsPerCastaway(1);
        draft.setTotalPicks(seededPicks.size());
        draft.setCurrentPickNumber(seededPicks.size() + 1);
        draft.setCurrentTurnUser(null);
        draftRepository.save(draft);
        group.setDraft(draft);
        groupRepository.save(group);

        List<DraftParticipant> participantRows = new ArrayList<>();
        for (User user : participantsInOrder) {
            DraftParticipant dp = new DraftParticipant();
            dp.setDraft(draft);
            dp.setUser(user);
            dp.setTeam(seededPicks.stream()
                    .filter(sp -> sp.user.getId().equals(user.getId()))
                    .map(sp -> sp.team)
                    .findFirst()
                    .orElse(null));
            dp.setDraftPosition(participantPositionByUserId.get(user.getId()));
            dp.setPicksMade(picksMadeByUserId.getOrDefault(user.getId(), 0));
            dp.setActive(true);
            participantRows.add(dp);
        }
        draftParticipantRepository.saveAll(participantRows);

        List<DraftPick> pickRows = new ArrayList<>();
        for (int i = 0; i < seededPicks.size(); i++) {
            DraftSeedPick seedPick = seededPicks.get(i);
            int pickNum = i + 1;

            DraftPick pick = new DraftPick();
            pick.setDraft(draft);
            pick.setPickNumber(pickNum);
            pick.setRoundNumber(((pickNum - 1) / totalParticipants) + 1);
            pick.setDraftPosition(participantPositionByUserId.get(seedPick.user.getId()));
            pick.setUser(seedPick.user);
            pick.setTeam(seedPick.team);
            pick.setCastawayPerformance(seedPick.castawayPerformance);
            pick.setPickedAt(startedAt.plusSeconds(pickNum));
            pickRows.add(pick);
        }
        draftPickRepository.saveAll(pickRows);
    }

    private int calculateDraftPosition(int pickNumber, int numPlayers, DraftStyle style) {
        switch (style) {
            case SNAKE: {
                int round = (pickNumber - 1) / numPlayers;
                if (round % 2 == 0) {
                    return (pickNumber - 1) % numPlayers;
                }
                return numPlayers - 1 - ((pickNumber - 1) % numPlayers);
            }
            case ROUND_ROBIN:
            case LINEAR:
            default:
                return (pickNumber - 1) % numPlayers;
        }
    }

    private static class DraftSeedPick {
        private final User user;
        private final Team team;
        private final CastawayPerformance castawayPerformance;

        private DraftSeedPick(User user, Team team, CastawayPerformance castawayPerformance) {
            this.user = user;
            this.team = team;
            this.castawayPerformance = castawayPerformance;
        }
    }

    private void seedGroupForSeason(Integer seasonNum, String groupName) {
        if (groupRepository.findByName(groupName).isPresent()) {
            System.out.println("✓ Test group already exists for " + groupName + ", skipping seed data\n");
            return;
        }

        if (seasonCache.isEmpty()) {
            System.out.println("⚠  No seasons available for test group\n");
            return;
        }

        Season season = seasonCache.get(seasonNum);
        if (season == null) {
            System.out.println("⚠  Season " + seasonNum + " not found for test group\n");
            return;
        }

        System.out.println("Seeding test group for Season " + seasonNum + ": " + groupName + "...");

        List<User> users = new ArrayList<>();
        String[] usernames = {"joel", "jess", "mckenna", "kc", "kaitlin", "devin"};
        for (String username : usernames) {
            String email = username + "@example.com";
            User user = getOrCreateSeedUser(username, email, username.equals("joel") ? Role.ADMIN : Role.USER);
            users.add(user);
        }

        Group group = new Group();
        group.setName(groupName);
        group.setAdmin(users.get(0));
        group.setSeason(season);
        
        // Set draft configuration for Season 50
        if (seasonNum == 50) {
            group.setStatus(GroupStatus.PENDING);
            group.setTeamSize(5);  // Each team gets 5 castaways
        } else {
            group.setStatus(GroupStatus.COMPLETED);
        }
        
        groupRepository.save(group);

        for (User user : users) {
            GroupMember member = new GroupMember();
            member.setGroup(group);
            member.setUser(user);
            member.setStatus(MembershipStatus.ACCEPTED);
            groupMemberRepository.save(member);
        }

        List<PointRule> rules = new ArrayList<>();
        rules.add(buildPointRule(group, RuleType.INDIVIDUAL_IMMUNITY, 2, "Individual immunity wins"));
        rules.add(buildPointRule(group, RuleType.FOUND_IDOL, 1, "Found idol"));
        rules.add(buildPointRule(group, RuleType.FOUND_ADVANTAGE, 1, "Found advantage"));
        rules.add(buildPointRule(group, RuleType.SOLE_SURVIVOR, 5, "Sole Survivor"));
        rules.add(buildPointRule(group, RuleType.RUNNER_UP, 2, "Runner-up"));
        rules.add(buildPointRule(group, RuleType.MADE_MERGE, 1, "Made the merge"));
        rules.add(buildPointRule(group, RuleType.MED_EVAC, -2, "Med evac"));
        rules.add(buildPointRule(group, RuleType.QUIT, -2, "Quit"));
        pointRuleRepository.saveAll(rules);

        List<Team> teams = new ArrayList<>();
        for (User user : users) {
            Team team = new Team();
            team.setGroup(group);
            team.setUser(user);
            team.setTeamName("Team " + user.getUsername());
            teamRepository.save(team);
            teams.add(team);
        }

        // For Season 50, don't pre-draft players - let the draft happen
        if (seasonNum == 50) {
            System.out.println("✓ Test group seeded for Season " + seasonNum + ": " + groupName);
            System.out.println("  Status: PENDING - Draft starts in 1 minute!");
            System.out.println("  Team size: 5 castaways per team");
            System.out.println("  Teams created: " + teams.size());
            System.out.println();
            return;  // Skip auto-drafting
        }

        List<CastawayPerformance> performances = new ArrayList<>();
        for (CastawayPerformance performance : perfCache.values()) {
            if (performance.getSeason().getSeason().equals(seasonNum)) {
                performances.add(performance);
            }
        }

        Collections.shuffle(performances, new Random());

        Map<Integer, Integer> draftOrderByTeamId = new HashMap<>();
        List<DraftSeedPick> seededPicks = new ArrayList<>();
        for (int i = 0; i < performances.size(); i++) {
            Team team = teams.get(i % teams.size());
            Integer teamId = team.getId();
            int draftOrder = draftOrderByTeamId.getOrDefault(teamId, 0) + 1;
            draftOrderByTeamId.put(teamId, draftOrder);

            TeamCastaway teamCastaway = new TeamCastaway();
            teamCastaway.setTeam(team);
            teamCastaway.setCastawayPerformance(performances.get(i));
            teamCastaway.setDraftOrder(draftOrder);
            teamCastawayRepository.save(teamCastaway);
            seededPicks.add(new DraftSeedPick(team.getUser(), team, performances.get(i)));
        }

        createCompletedDraftForGroup(group, users, seededPicks, DraftStyle.LINEAR);

        scoreProjectionService.recalculateGroupScores(group);

        System.out.println("✓ Test group seeded for Season " + seasonNum + ": " + groupName + ". Final team points:");
        for (Team team : teams) {
            Team updatedTeam = teamRepository.findById(team.getId()).orElse(team);
            System.out.println("  " + updatedTeam.getTeamName() + ": " + updatedTeam.getTotalPoints());
        }
        System.out.println();
    }

    private PointRule buildPointRule(Group group, RuleType ruleType, int points, String description) {
        PointRule rule = new PointRule();
        rule.setGroup(group);
        rule.setRuleType(ruleType);
        rule.setPoints(points);
        rule.setDescription(description);
        return rule;
    }

    private void applyCompletedStatusIfFinale(Group group, Episode latestWatchedEpisode) {
        if (group == null || latestWatchedEpisode == null) {
            return;
        }
        if (Boolean.TRUE.equals(latestWatchedEpisode.getIsFinale())) {
            group.setStatus(GroupStatus.COMPLETED);
        }
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
