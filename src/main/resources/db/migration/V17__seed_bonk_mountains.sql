insert into item (code, name, description, slot, rarity, bonus_max_hp, bonus_attack, bonus_defense, sell_value) values
    ('HEADLAMP_HAT',      'Headlamp Hat',      'A hard hat with a tiny lamp. Much light. Very see in the dark.', 'HEAD',   'RARE',  5, 0, 2, 20),
    ('BAT_FANG_NECKLACE', 'Bat Fang Necklace', 'Pointy. Squeaky. Makes every bonk a little bitey.',             'NECK',   'RARE',  0, 3, 0, 25),
    ('YETI_FUR_COAT',     'Yeti Fur Coat',     'Warm, fluffy and only slightly stolen.',                         'BODY',   'EPIC', 25, 0, 3, 60),
    ('FROST_BONE',        'Frost Bone',        'A frozen bone. Brain freeze on every bonk.',                     'WEAPON', 'EPIC',  0, 6, 1, 80);

insert into enemy (code, name, max_hp, attack, defense, exp_reward, loot_chance, gold_min, gold_max) values
    ('MOUNTAIN_GOAT_DOGE', 'Mountain Goat Doge',    55, 13, 4,  35, 0.5,  8, 14),
    ('CAVE_BAT_SHIBE',     'Cave Bat Shibe',        42, 15, 2,  40, 0.4,  6, 12),
    ('SNOW_DOGE',          'Abominable Snow Doge', 150, 18, 6, 160, 1.0, 60, 90);

insert into enemy_skill (enemy_code, skill, weight) values
    ('MOUNTAIN_GOAT_DOGE', 'BONK',         2),
    ('MOUNTAIN_GOAT_DOGE', 'BITE',         1),
    ('CAVE_BAT_SHIBE',     'BITE',         3),
    ('CAVE_BAT_SHIBE',     'BONK',         1),
    ('SNOW_DOGE',          'BONK',         2),
    ('SNOW_DOGE',          'BONKING_SPIN', 1);

insert into enemy_loot (enemy_code, item_code, weight) values
    ('MOUNTAIN_GOAT_DOGE', 'HEADLAMP_HAT',      3),
    ('MOUNTAIN_GOAT_DOGE', 'SPIKY_COLLAR',      1),
    ('CAVE_BAT_SHIBE',     'BAT_FANG_NECKLACE', 2),
    ('CAVE_BAT_SHIBE',     'HEADLAMP_HAT',      1),
    ('SNOW_DOGE',          'YETI_FUR_COAT',     3),
    ('SNOW_DOGE',          'FROST_BONE',        2);

insert into location (code, name, description, encounter_chance) values
    ('BONK_PASS', 'Bonk Pass',
     'A narrow mountain path. Rocks fall from above with suspicious bonking sounds.', 0.6),
    ('ECHO_CAVE', 'Echo Cave',
     'Pitch black. Every bark comes back three times. Something squeaks in the dark.', 0.7),
    ('BONK_PEAK', 'Bonk Peak',
     'Snow, wind and a view of the whole world. Huge pawprints lead into a cave of ice.', 1.0);

--   BONK_PEAK
--       |
--   ECHO_CAVE ---- BONK_PASS ---- MUCH_SWAMP
insert into location_exit (location_code, direction, target_code) values
    ('MUCH_SWAMP', 'WEST',  'BONK_PASS'),
    ('BONK_PASS',  'EAST',  'MUCH_SWAMP'),
    ('BONK_PASS',  'WEST',  'ECHO_CAVE'),
    ('ECHO_CAVE',  'EAST',  'BONK_PASS'),
    ('BONK_PASS',  'NORTH', 'BONK_PEAK'),
    ('BONK_PEAK',  'SOUTH', 'BONK_PASS');

insert into location_spawn (location_code, enemy_code, weight) values
    ('BONK_PASS', 'MOUNTAIN_GOAT_DOGE', 3),
    ('BONK_PASS', 'SOGGY_SHIBE',        1),
    ('ECHO_CAVE', 'CAVE_BAT_SHIBE',     3),
    ('ECHO_CAVE', 'MOUNTAIN_GOAT_DOGE', 1),
    ('BONK_PEAK', 'SNOW_DOGE',          1);

insert into exit_gate (location_code, direction, required_level, required_quest_code, required_item_code) values
    ('MUCH_SWAMP', 'WEST',  null, 'LEG_DAY', null),
    ('BONK_PASS',  'WEST',  null, null,      'HEADLAMP_HAT'),
    ('BONK_PASS',  'NORTH', 5,    null,      null);
