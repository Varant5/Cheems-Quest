create table encounter (
    id           uuid primary key,
    version      bigint                   not null,
    character_id uuid                     not null references game_character (id),
    status       varchar(16)              not null,
    started_at   timestamp with time zone not null,
    ended_at     timestamp with time zone
);

-- A character can be in at most one ongoing fight.
create unique index uq_encounter_active_per_character
    on encounter (character_id)
    where status = 'PLAYER_TURN';

create table encounter_enemy (
    encounter_id uuid        not null references encounter (id),
    position     integer     not null,
    enemy_code   varchar(64) not null references enemy (code),
    current_hp   integer     not null check (current_hp >= 0),
    primary key (encounter_id, position)
);
