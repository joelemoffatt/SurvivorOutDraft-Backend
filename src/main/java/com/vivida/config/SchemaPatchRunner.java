package com.vivida.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Applies tiny idempotent schema patches needed for local/dev startup.
 */
@Component
@ConditionalOnProperty(name = "vivida.schema.patch.enabled", havingValue = "true", matchIfMissing = true)
public class SchemaPatchRunner {

    private final JdbcTemplate jdbcTemplate;

    public SchemaPatchRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void applyPatches() {
        patchGroupsTable();
        patchDraftsTable();
        patchDraftParticipantsTable();
        patchDraftPicksTable();
        patchDraftCastawaysTable();
    }

    private void patchGroupsTable() {
        safeExecute("ALTER TABLE IF EXISTS groups ADD COLUMN IF NOT EXISTS latest_episode_watched INTEGER");
        safeExecute("UPDATE groups SET latest_episode_watched = 0 WHERE latest_episode_watched IS NULL");
        safeExecute("ALTER TABLE IF EXISTS groups ALTER COLUMN latest_episode_watched SET DEFAULT 0");
        safeExecute("ALTER TABLE IF EXISTS groups ALTER COLUMN latest_episode_watched SET NOT NULL");
    }

    private void patchDraftsTable() {
        safeExecute("ALTER TABLE IF EXISTS drafts ADD COLUMN IF NOT EXISTS total_participants INTEGER");
        safeExecute("ALTER TABLE IF EXISTS drafts ADD COLUMN IF NOT EXISTS total_castaways INTEGER");
        safeExecute("ALTER TABLE IF EXISTS drafts ADD COLUMN IF NOT EXISTS max_drafts_per_castaway INTEGER");
        safeExecute("ALTER TABLE IF EXISTS drafts ADD COLUMN IF NOT EXISTS total_picks INTEGER");
        safeExecute("ALTER TABLE IF EXISTS drafts ADD COLUMN IF NOT EXISTS current_pick_number INTEGER");
        safeExecute("ALTER TABLE IF EXISTS drafts ADD COLUMN IF NOT EXISTS team_size INTEGER");
        safeExecute("ALTER TABLE IF EXISTS drafts ADD COLUMN IF NOT EXISTS status VARCHAR(30)");
        safeExecute("ALTER TABLE IF EXISTS drafts ADD COLUMN IF NOT EXISTS style VARCHAR(30)");

        safeExecute("UPDATE drafts SET total_participants = 0 WHERE total_participants IS NULL");
        safeExecute("UPDATE drafts SET total_castaways = 0 WHERE total_castaways IS NULL");
        safeExecute("UPDATE drafts SET max_drafts_per_castaway = 1 WHERE max_drafts_per_castaway IS NULL");
        safeExecute("UPDATE drafts SET total_picks = 0 WHERE total_picks IS NULL");
        safeExecute("UPDATE drafts SET current_pick_number = 1 WHERE current_pick_number IS NULL");

        safeExecute("ALTER TABLE IF EXISTS drafts ALTER COLUMN total_participants SET DEFAULT 0");
        safeExecute("ALTER TABLE IF EXISTS drafts ALTER COLUMN total_castaways SET DEFAULT 0");
        safeExecute("ALTER TABLE IF EXISTS drafts ALTER COLUMN max_drafts_per_castaway SET DEFAULT 1");
        safeExecute("ALTER TABLE IF EXISTS drafts ALTER COLUMN total_picks SET DEFAULT 0");
        safeExecute("ALTER TABLE IF EXISTS drafts ALTER COLUMN current_pick_number SET DEFAULT 1");

        safeExecute("ALTER TABLE IF EXISTS drafts ALTER COLUMN total_participants SET NOT NULL");
        safeExecute("ALTER TABLE IF EXISTS drafts ALTER COLUMN total_castaways SET NOT NULL");
        safeExecute("ALTER TABLE IF EXISTS drafts ALTER COLUMN max_drafts_per_castaway SET NOT NULL");
        safeExecute("ALTER TABLE IF EXISTS drafts ALTER COLUMN total_picks SET NOT NULL");
        safeExecute("ALTER TABLE IF EXISTS drafts ALTER COLUMN current_pick_number SET NOT NULL");
    }

    private void patchDraftParticipantsTable() {
        safeExecute("ALTER TABLE IF EXISTS draft_participants ADD COLUMN IF NOT EXISTS draft_position INTEGER");
        safeExecute("ALTER TABLE IF EXISTS draft_participants ADD COLUMN IF NOT EXISTS picks_made INTEGER");
        safeExecute("ALTER TABLE IF EXISTS draft_participants ADD COLUMN IF NOT EXISTS active BOOLEAN");
        safeExecute("ALTER TABLE IF EXISTS draft_participants ADD COLUMN IF NOT EXISTS created_at TIMESTAMP");
        safeExecute("ALTER TABLE IF EXISTS draft_participants ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP");

        safeExecute("UPDATE draft_participants SET picks_made = 0 WHERE picks_made IS NULL");
        safeExecute("UPDATE draft_participants SET active = TRUE WHERE active IS NULL");
        safeExecute("UPDATE draft_participants SET created_at = NOW() WHERE created_at IS NULL");
        safeExecute("UPDATE draft_participants SET updated_at = NOW() WHERE updated_at IS NULL");

        safeExecute("ALTER TABLE IF EXISTS draft_participants ALTER COLUMN picks_made SET DEFAULT 0");
        safeExecute("ALTER TABLE IF EXISTS draft_participants ALTER COLUMN active SET DEFAULT TRUE");

        safeExecute("ALTER TABLE IF EXISTS draft_participants ALTER COLUMN picks_made SET NOT NULL");
        safeExecute("ALTER TABLE IF EXISTS draft_participants ALTER COLUMN active SET NOT NULL");
        safeExecute("ALTER TABLE IF EXISTS draft_participants ALTER COLUMN created_at SET NOT NULL");
        safeExecute("ALTER TABLE IF EXISTS draft_participants ALTER COLUMN updated_at SET NOT NULL");
    }

    private void patchDraftPicksTable() {
        safeExecute("ALTER TABLE IF EXISTS draft_picks ADD COLUMN IF NOT EXISTS round_number INTEGER");
        safeExecute("ALTER TABLE IF EXISTS draft_picks ADD COLUMN IF NOT EXISTS draft_position INTEGER");
        safeExecute("ALTER TABLE IF EXISTS draft_picks ADD COLUMN IF NOT EXISTS picked_at TIMESTAMP");

        safeExecute("UPDATE draft_picks SET round_number = 1 WHERE round_number IS NULL");
        safeExecute("UPDATE draft_picks SET draft_position = 0 WHERE draft_position IS NULL");

        safeExecute("ALTER TABLE IF EXISTS draft_picks ALTER COLUMN round_number SET NOT NULL");
        safeExecute("ALTER TABLE IF EXISTS draft_picks ALTER COLUMN draft_position SET NOT NULL");

        // Pick slots are pre-created before selection, so these must remain nullable.
        safeExecute("ALTER TABLE IF EXISTS draft_picks ALTER COLUMN castaway_performance_id DROP NOT NULL");
        safeExecute("ALTER TABLE IF EXISTS draft_picks ALTER COLUMN picked_at DROP NOT NULL");
    }

    private void patchDraftCastawaysTable() {
        safeExecute("ALTER TABLE IF EXISTS draft_castaways DROP COLUMN IF EXISTS is_drafted");
        safeExecute("ALTER TABLE IF EXISTS draft_castaways ADD COLUMN IF NOT EXISTS created_at TIMESTAMP");
        safeExecute("UPDATE draft_castaways SET created_at = NOW() WHERE created_at IS NULL");
        safeExecute("ALTER TABLE IF EXISTS draft_castaways ALTER COLUMN created_at SET NOT NULL");
    }

    private void safeExecute(String sql) {
        try {
            jdbcTemplate.execute(sql);
        } catch (Exception ignored) {
            // Intentionally ignore idempotent patch mismatches on legacy local schemas.
        }
    }
}
