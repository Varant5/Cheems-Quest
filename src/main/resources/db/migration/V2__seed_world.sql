insert into enemy (code, name, max_hp, attack, defense, exp_reward) values
    ('LIL_DOGE',    'Neighbor''s Lil Doge', 20,  6, 0,   8),
    ('KAREN_DOGE',  'Wild Karen Doge',      35,  9, 2,  20),
    ('SOGGY_SHIBE', 'Soggy Shibe',          28,  8, 1,  14),
    ('BUFF_DOGE',   'Buff Doge',           120, 16, 5, 100);

insert into enemy_skill (enemy_code, skill, weight) values
    ('LIL_DOGE',    'BONK',         1),
    ('LIL_DOGE',    'BITE',         1),
    ('KAREN_DOGE',  'BONK',         3),
    ('KAREN_DOGE',  'BITE',         1),
    ('SOGGY_SHIBE', 'BITE',         2),
    ('SOGGY_SHIBE', 'BONK',         1),
    ('BUFF_DOGE',   'BONK',         2),
    ('BUFF_DOGE',   'BITE',         1),
    ('BUFF_DOGE',   'BONKING_SPIN', 1);

insert into location (code, name, description, encounter_chance) values
    ('CHEEMS_DOGHOUSE', 'Cheems'' Doghouse',
     'A cozy doghouse that smells of old cheeseburgers. Nothing can bonk you here.', 0.0),
    ('WOW_FOREST', 'Wow Forest',
     'Very trees. Much leaves. Something growls behind a suspiciously round bush.', 0.5),
    ('KARENS_BASEMENT', 'Karen''s Basement',
     'Dark, damp and covered in sticky notes full of complaints. Someone wants to speak to your manager.', 0.7),
    ('MUCH_SWAMP', 'Much Swamp',
     'Your paws sink into the mud. Such stink. Many mosquitoes.', 0.6),
    ('BUFF_DOGE_TOWER', 'Buff Doge Tower',
     'A tower built entirely from protein powder tubs. At the top, Buff Doge flexes menacingly.', 1.0);

--                 BUFF_DOGE_TOWER
--                        |
--   MUCH_SWAMP ---- WOW_FOREST
--                        |
--                 CHEEMS_DOGHOUSE ---- KARENS_BASEMENT
insert into location_exit (location_code, direction, target_code) values
    ('CHEEMS_DOGHOUSE', 'NORTH', 'WOW_FOREST'),
    ('CHEEMS_DOGHOUSE', 'EAST',  'KARENS_BASEMENT'),
    ('KARENS_BASEMENT', 'WEST',  'CHEEMS_DOGHOUSE'),
    ('WOW_FOREST',      'SOUTH', 'CHEEMS_DOGHOUSE'),
    ('WOW_FOREST',      'WEST',  'MUCH_SWAMP'),
    ('WOW_FOREST',      'NORTH', 'BUFF_DOGE_TOWER'),
    ('MUCH_SWAMP',      'EAST',  'WOW_FOREST'),
    ('BUFF_DOGE_TOWER', 'SOUTH', 'WOW_FOREST');

insert into location_spawn (location_code, enemy_code, weight) values
    ('WOW_FOREST',      'LIL_DOGE',    3),
    ('WOW_FOREST',      'KAREN_DOGE',  1),
    ('KARENS_BASEMENT', 'KAREN_DOGE',  3),
    ('KARENS_BASEMENT', 'LIL_DOGE',    1),
    ('MUCH_SWAMP',      'SOGGY_SHIBE', 3),
    ('MUCH_SWAMP',      'LIL_DOGE',    1),
    ('BUFF_DOGE_TOWER', 'BUFF_DOGE',   1);
