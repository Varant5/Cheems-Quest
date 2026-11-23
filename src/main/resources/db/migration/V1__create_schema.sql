create table player (
    id         uuid primary key,
    username   varchar(32)              not null unique,
    created_at timestamp with time zone not null
);

create table enemy (
    code       varchar(64) primary key,
    name       varchar(255) not null,
    max_hp     integer      not null check (max_hp > 0),
    attack     integer      not null check (attack >= 0),
    defense    integer      not null check (defense >= 0),
    exp_reward integer      not null check (exp_reward >= 0)
);

create table enemy_skill (
    enemy_code varchar(64) not null references enemy (code),
    skill      varchar(32) not null,
    weight     integer     not null check (weight > 0),
    primary key (enemy_code, skill)
);

create table location (
    code             varchar(64) primary key,
    name             varchar(255)     not null,
    description      varchar(1000)    not null,
    encounter_chance double precision not null check (encounter_chance between 0 and 1)
);

create table location_exit (
    location_code varchar(64) not null references location (code),
    direction     varchar(16) not null,
    target_code   varchar(64) not null references location (code),
    primary key (location_code, direction)
);

create table location_spawn (
    location_code varchar(64) not null references location (code),
    enemy_code    varchar(64) not null references enemy (code),
    weight        integer     not null check (weight > 0),
    primary key (location_code, enemy_code)
);

create table game_character (
    id            uuid primary key,
    player_id     uuid                     not null references player (id),
    name          varchar(32)              not null,
    level         integer                  not null check (level >= 1),
    experience    integer                  not null check (experience >= 0),
    max_hp        integer                  not null check (max_hp > 0),
    attack        integer                  not null check (attack >= 0),
    defense       integer                  not null check (defense >= 0),
    current_hp    integer                  not null,
    location_code varchar(64)              not null references location (code),
    created_at    timestamp with time zone not null,
    check (current_hp between 0 and max_hp)
);

create index idx_game_character_player on game_character (player_id);
