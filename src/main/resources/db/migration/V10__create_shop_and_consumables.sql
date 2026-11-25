alter table item alter column slot drop not null;
alter table item add column effect varchar(16);
alter table item add column effect_power integer not null default 0 check (effect_power >= 0);
alter table item add column buy_price integer check (buy_price > 0);
alter table item add constraint item_slot_or_effect check ((slot is null) <> (effect is null));

insert into item (code, name, description, slot, rarity, effect, effect_power, sell_value, buy_price) values
    ('CHEEMSBURGER', 'Cheemsburger', 'Much cheese. Very burger. Heals 30 HP.',                           null, 'COMMON',    'HEAL',             30,   5,  15),
    ('LUCKY_DICE',   'Lucky Dice',   'Always rolls a six. Your next attack in a fight is a sure crit.',  null, 'RARE',      'GUARANTEED_CRIT',   0,  15,  40),
    ('DOGE_COIN',    'Doge Coin',    'To the moon! Eat it for a permanent +10 ATK. Such investment.',    null, 'LEGENDARY', 'PERMANENT_ATTACK', 10, 200, 500);

create table shop_item (
    location_code varchar(64) not null references location (code),
    item_code     varchar(64) not null references item (code),
    primary key (location_code, item_code)
);

insert into shop_item (location_code, item_code) values
    ('CHEEMS_DOGHOUSE', 'CHEEMSBURGER'),
    ('CHEEMS_DOGHOUSE', 'LUCKY_DICE'),
    ('CHEEMS_DOGHOUSE', 'DOGE_COIN');
