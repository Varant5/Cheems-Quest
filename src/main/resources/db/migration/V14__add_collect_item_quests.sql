alter table quest add column target_item_code varchar(64) references item (code);

alter table quest drop constraint quest_objective_type_check;

alter table quest add constraint quest_objective_type_check
    check (objective_type in ('DEFEAT_ENEMY', 'VISIT_LOCATION', 'COLLECT_ITEM'));

alter table quest drop constraint quest_target_check;

alter table quest add constraint quest_target_check check (
    (objective_type = 'DEFEAT_ENEMY') = (target_enemy_code is not null)
    and (objective_type = 'VISIT_LOCATION') = (target_location_code is not null)
    and (objective_type = 'COLLECT_ITEM') = (target_item_code is not null));

insert into quest (code, name, description, objective_type, target_item_code, required_amount, reward_exp, reward_item_code) values
    ('WIG_HUNT', 'Manager Material',
     'A shady collector in a trench coat wants Karen wigs. Very fashion. Find 2 Karen''s Wigs in fights.',
     'COLLECT_ITEM', 'KARENS_WIG', 2, 80, 'SPIKY_COLLAR');
