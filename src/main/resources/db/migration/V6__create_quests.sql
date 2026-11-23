create table quest (
    code              varchar(64) primary key,
    name              varchar(255)  not null,
    description       varchar(1000) not null,
    target_enemy_code varchar(64)   not null references enemy (code),
    required_kills    integer       not null check (required_kills > 0),
    reward_exp        integer       not null check (reward_exp >= 0),
    reward_item_code  varchar(64) references item (code)
);

create table quest_progress (
    id           uuid primary key,
    character_id uuid                     not null references game_character (id),
    quest_code   varchar(64)              not null references quest (code),
    kills        integer                  not null check (kills >= 0),
    status       varchar(16)              not null check (status in ('ACTIVE', 'COMPLETED')),
    accepted_at  timestamp with time zone not null,
    completed_at timestamp with time zone,
    unique (character_id, quest_code)
);

insert into quest (code, name, description, target_enemy_code, required_kills, reward_exp, reward_item_code) values
    ('KAREN_PROBLEMS', 'Karen Problems',
     'The Karens keep asking to speak to the manager of the forest. Bonk 3 Wild Karen Doges and bring peace.',
     'KAREN_DOGE', 3, 60, 'STONKS_HELMET');
