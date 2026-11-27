alter table enemy_skill add column phase integer not null default 1 check (phase >= 1);

alter table enemy_skill drop constraint enemy_skill_pkey;

alter table enemy_skill add primary key (enemy_code, phase, skill);

create table enemy_phase (
    enemy_code   varchar(64)      not null references enemy (code),
    phase        integer          not null check (phase >= 2),
    hp_threshold double precision not null check (hp_threshold > 0 and hp_threshold < 1),
    announcement varchar(255)     not null,
    primary key (enemy_code, phase)
);

insert into enemy_phase (enemy_code, phase, hp_threshold, announcement) values
    ('SNOW_DOGE', 2, 0.5, 'Abominable Snow Doge is ENRAGED! The air turns to ice.');

insert into enemy_skill (enemy_code, phase, skill, weight) values
    ('SNOW_DOGE', 2, 'FROST_BREATH', 3),
    ('SNOW_DOGE', 2, 'BITE',         2),
    ('SNOW_DOGE', 2, 'BONK',         1);

insert into item (code, name, description, slot, rarity, bonus_max_hp, bonus_attack, bonus_defense, sell_value) values
    ('SNOW_CROWN', 'Snow Crown', 'Never melts. Much royal. Very cold head.', 'HEAD', 'LEGENDARY', 20, 4, 4, 200);

insert into quest (code, name, description, objective_type, target_enemy_code, required_amount, reward_exp, reward_item_code, prerequisite_code) values
    ('YETI_HUNT', 'The Abominable Snow Doge',
     'Something huge lives on top of Bonk Peak. It gets very angry when hurt. Bonk it anyway.',
     'DEFEAT_ENEMY', 'SNOW_DOGE', 1, 300, 'SNOW_CROWN', 'LEG_DAY');
