# Flyway Developer Workflow

## Why Flyway Exists
Flyway is the source of truth for database schema changes in this project.

Flyway gives us:
- Versioned, ordered SQL migrations (`V1__...sql`, `V2__...sql`, ...)
- Repeatable and deterministic schema changes across local, staging, and production
- Audit trail of what changed, when, and in what order (`flyway_schema_history` table)
- Safety against ad-hoc production edits and hidden schema drift

In this project, Flyway runs on app startup and validates migration history.

## Core Rules for Developers
- Never change production schema manually.
- Never edit an already-applied migration file.
- Always add a new migration file for each schema change.
- Keep changes backward compatible first, then clean up in a later migration.
- Treat migrations like code: PR review, test locally, deploy in sequence.

## Migration File Conventions
Place files in:
- `src/main/resources/db/migration/`

File naming:
- `V2__add_group_timezone.sql`
- `V3__add_team_name_v2.sql`
- `V4__backfill_team_name_v2.sql`

Versioning rules:
- Number increases monotonically.
- One logical change per migration when possible.
- Do not reuse or reorder versions once merged.

## What to Do When a Prop Changes
"Prop" here means a field the app persists in DB (column/table relationship).

### 1) Add a New Prop (column)
Safe path:
1. Add new nullable column in a migration.
2. Deploy app code that writes/reads it.
3. Backfill data if needed.
4. Add constraints (NOT NULL/default) in a later migration.

Example:
```sql
-- V5__add_groups_timezone.sql
ALTER TABLE groups ADD COLUMN timezone VARCHAR(64);
```

Then later:
```sql
-- V6__groups_timezone_not_null.sql
UPDATE groups SET timezone = 'UTC' WHERE timezone IS NULL;
ALTER TABLE groups ALTER COLUMN timezone SET NOT NULL;
```

### 2) Rename a Prop (column)
Do not hard-rename in one step for live systems.

Use expand-contract:
1. Add new column.
2. Backfill from old column.
3. Deploy app reading/writing both (temporary compatibility window).
4. Move reads to new column only.
5. Drop old column in a later release.

Example:
```sql
-- V7__add_users_display_name.sql
ALTER TABLE users ADD COLUMN display_name VARCHAR(100);
UPDATE users SET display_name = username WHERE display_name IS NULL;
```

Later cleanup:
```sql
-- V9__drop_users_username.sql
ALTER TABLE users DROP COLUMN username;
```

### 3) Change Prop Type
Do not change type in-place if it can break reads/writes.

Safer pattern:
1. Add new column with target type.
2. Backfill with explicit conversion.
3. Deploy app using new column.
4. Drop old column later.

Example:
```sql
-- V10__add_groups_start_at_ts.sql
ALTER TABLE groups ADD COLUMN start_at_ts TIMESTAMP;
UPDATE groups
SET start_at_ts = to_timestamp(start_at_epoch_seconds)
WHERE start_at_epoch_seconds IS NOT NULL;
```

### 4) Remove a Prop
1. Stop using it in app code first.
2. Deploy and verify no reads/writes depend on it.
3. Drop it in a later migration.

Example:
```sql
-- V12__drop_groups_legacy_status.sql
ALTER TABLE groups DROP COLUMN legacy_status;
```

## Recommended Release Pattern (Expand-Contract)
Release A:
- Add new schema objects (columns/tables/indexes), keep old ones.

Release B:
- Application dual-write or dual-read migration window.

Release C:
- Remove old schema objects only after data and code are fully migrated.

## How We Interact with Flyway Day-to-Day
For every DB-affecting feature:
1. Create a new migration SQL file.
2. Implement backward-compatible app code.
3. Run backend locally and verify startup migration success.
4. Test affected endpoints and UI flows.
5. Open PR with both app code + migration.

## Local Verification Checklist
- Backend starts with no Flyway validation errors.
- New schema exists and endpoints work.
- Existing data remains intact.
- No destructive operations in first rollout unless explicitly approved.

Useful DB check:
```sql
SELECT installed_rank, version, description, success
FROM flyway_schema_history
ORDER BY installed_rank;
```

## Production Safety Checklist
- Take a backup/snapshot before deploy.
- Review migration SQL for locks and table rewrites.
- Prefer additive changes during peak traffic windows.
- Schedule destructive cleanup for low-traffic windows.
- Monitor logs immediately after deploy.

## Common Mistakes to Avoid
- Editing `V1__initial_schema.sql` after it has been applied anywhere.
- Combining many risky changes in one migration.
- Renaming/dropping columns in same release where code still depends on them.
- Assuming local empty DB behavior is same as production with real data.

## Repository-Specific Notes
- Flyway is enabled in `application.properties`.
- Hibernate is set to `validate` (not `update`).
- Data-loader seed flags are environment controlled and should stay off in production.

That means: schema evolution happens through Flyway migrations, not Hibernate auto-DDL.
