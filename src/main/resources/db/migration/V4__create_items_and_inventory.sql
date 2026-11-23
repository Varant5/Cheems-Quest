create table item (
    code          varchar(64) primary key,
    name          varchar(255)  not null,
    description   varchar(1000) not null,
    slot          varchar(16)   not null,
    rarity        varchar(16)   not null,
    bonus_max_hp  integer       not null default 0,
    bonus_attack  integer       not null default 0,
    bonus_defense integer       not null default 0
);

create table inventory_slot (
    id           uuid primary key,
    character_id uuid                     not null references game_character (id),
    item_code    varchar(64)              not null references item (code),
    equipped     boolean                  not null,
    acquired_at  timestamp with time zone not null
);

create index idx_inventory_slot_character on inventory_slot (character_id);

alter table game_character drop constraint game_character_check;
alter table game_character add constraint game_character_current_hp_check check (current_hp >= 0);

insert into item (code, name, description, slot, rarity, bonus_max_hp, bonus_attack, bonus_defense) values
    ('VERY_STICK',     'Very Stick',      'A stick. Much fetch. Every hero starts somewhere.',      'WEAPON', 'COMMON',     0, 2, 0),
    ('SOGGY_RAINCOAT', 'Soggy Raincoat',  'Still wet. Smells like the swamp.',                     'BODY',   'COMMON',     5, 0, 1),
    ('KARENS_WIG',     'Karen''s Wig',    'Grants the power to speak to any manager.',             'HEAD',   'RARE',       5, 0, 1),
    ('SPIKY_COLLAR',   'Spiky Collar',    'Very spike. Much intimidate.',                          'NECK',   'RARE',       0, 1, 2),
    ('STONKS_HELMET',  'Stonks Helmet',   'Number go up. Defense also go up.',                     'HEAD',   'EPIC',      10, 0, 3),
    ('BUFF_HARNESS',   'Buff Harness',    'Leftover from Buff Doge''s leg day.',                   'BODY',   'EPIC',      20, 2, 2),
    ('LEGENDARY_BONE', 'Legendary Bone',  'Glows faintly. Smells amazing. Bonks like a truck.',    'WEAPON', 'LEGENDARY',  0, 8, 0);
