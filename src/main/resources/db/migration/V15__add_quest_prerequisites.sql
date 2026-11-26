alter table quest add column prerequisite_code varchar(64) references quest (code)
    constraint quest_prerequisite_check check (prerequisite_code <> code);

insert into quest (code, name, description, objective_type, target_enemy_code, required_amount, reward_exp, reward_item_code, prerequisite_code) values
    ('LEG_DAY', 'Never Skip Leg Day',
     'With the Karens gone, Buff Doge calls you out from his protein tower. Bonk him once and for all.',
     'DEFEAT_ENEMY', 'BUFF_DOGE', 1, 150, 'DOGE_COIN', 'KAREN_PROBLEMS');
