create table encounter_status (
    encounter_id uuid        not null references encounter (id),
    combatant    integer     not null check (combatant >= -1),
    type         varchar(16) not null check (type in ('POISON', 'STUN')),
    turns        integer     not null check (turns > 0),
    power        integer     not null check (power >= 0),
    primary key (encounter_id, combatant, type)
);

insert into enemy_skill (enemy_code, skill, weight) values
    ('SOGGY_SHIBE',        'TOXIC_DROOL', 3),
    ('CAVE_BAT_SHIBE',     'TOXIC_DROOL', 2),
    ('MOUNTAIN_GOAT_DOGE', 'HEADBUTT',    3);
