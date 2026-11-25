alter table enemy add column gold_min integer not null default 0;
alter table enemy add column gold_max integer not null default 0;
alter table enemy add constraint enemy_gold_range check (gold_min >= 0 and gold_max >= gold_min);

update enemy set gold_min = 2,  gold_max = 5  where code = 'LIL_DOGE';
update enemy set gold_min = 5,  gold_max = 10 where code = 'KAREN_DOGE';
update enemy set gold_min = 4,  gold_max = 8  where code = 'SOGGY_SHIBE';
update enemy set gold_min = 40, gold_max = 60 where code = 'BUFF_DOGE';
