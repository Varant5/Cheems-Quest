create table visited_location (
    character_id  uuid        not null references game_character (id),
    location_code varchar(64) not null references location (code),
    primary key (character_id, location_code)
);

insert into visited_location (character_id, location_code)
select id, 'CHEEMS_DOGHOUSE' from game_character
union
select id, location_code from game_character;
