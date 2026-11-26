alter table quest alter column target_enemy_code drop not null;

alter table quest add column target_location_code varchar(64) references location (code);

alter table quest drop constraint quest_objective_type_check;

alter table quest add constraint quest_objective_type_check
    check (objective_type in ('DEFEAT_ENEMY', 'VISIT_LOCATION'));

alter table quest add constraint quest_target_check check (
    (objective_type = 'DEFEAT_ENEMY') = (target_enemy_code is not null)
    and (objective_type = 'VISIT_LOCATION') = (target_location_code is not null));

insert into quest (code, name, description, objective_type, target_location_code, required_amount, reward_exp, reward_item_code) values
    ('MYSTERY_SMELL', 'Much Mystery Smell',
     'Something in Much Swamp smells even worse than usual. Go sniff it out. Very brave nose.',
     'VISIT_LOCATION', 'MUCH_SWAMP', 1, 25, 'CHEEMSBURGER');
