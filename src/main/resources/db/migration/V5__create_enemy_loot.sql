alter table enemy add column loot_chance double precision not null default 0
    check (loot_chance between 0 and 1);

create table enemy_loot (
    enemy_code varchar(64) not null references enemy (code),
    item_code  varchar(64) not null references item (code),
    weight     integer     not null check (weight > 0),
    primary key (enemy_code, item_code)
);

update enemy set loot_chance = 0.3 where code = 'LIL_DOGE';
update enemy set loot_chance = 0.5 where code = 'KAREN_DOGE';
update enemy set loot_chance = 0.5 where code = 'SOGGY_SHIBE';
update enemy set loot_chance = 1.0 where code = 'BUFF_DOGE';

insert into enemy_loot (enemy_code, item_code, weight) values
    ('LIL_DOGE',    'VERY_STICK',     3),
    ('LIL_DOGE',    'SPIKY_COLLAR',   1),
    ('KAREN_DOGE',  'KARENS_WIG',     3),
    ('KAREN_DOGE',  'SPIKY_COLLAR',   1),
    ('SOGGY_SHIBE', 'SOGGY_RAINCOAT', 3),
    ('SOGGY_SHIBE', 'SPIKY_COLLAR',   1),
    ('BUFF_DOGE',   'BUFF_HARNESS',   3),
    ('BUFF_DOGE',   'STONKS_HELMET',  2),
    ('BUFF_DOGE',   'LEGENDARY_BONE', 1);
