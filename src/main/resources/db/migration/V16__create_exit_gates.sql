create table exit_gate (
    location_code       varchar(64) not null,
    direction           varchar(16) not null,
    required_level      integer check (required_level > 1),
    required_quest_code varchar(64) references quest (code),
    required_item_code  varchar(64) references item (code),
    primary key (location_code, direction),
    foreign key (location_code, direction) references location_exit (location_code, direction),
    check (required_level is not null or required_quest_code is not null or required_item_code is not null)
);

insert into exit_gate (location_code, direction, required_level) values
    ('WOW_FOREST', 'NORTH', 3);
