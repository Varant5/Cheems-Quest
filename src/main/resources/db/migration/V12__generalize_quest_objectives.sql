alter table quest
    add column objective_type varchar(32) not null default 'DEFEAT_ENEMY'
        constraint quest_objective_type_check check (objective_type in ('DEFEAT_ENEMY'));

alter table quest alter column objective_type drop default;

alter table quest rename column required_kills to required_amount;

alter table quest_progress rename column kills to progress;
