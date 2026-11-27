delete from enemy_skill where skill = 'BONKING_SPIN';

update enemy_skill set weight = 2 where enemy_code = 'KAREN_DOGE' and skill = 'BONK';

insert into enemy_skill (enemy_code, skill, weight) values
    ('SNOW_DOGE', 'BITE',     1),
    ('SNOW_DOGE', 'HEADBUTT', 1);
