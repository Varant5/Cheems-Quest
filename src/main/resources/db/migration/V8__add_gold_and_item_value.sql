alter table item add column sell_value integer not null default 0 check (sell_value >= 0);

update item set sell_value = case code
    when 'VERY_STICK'     then 2
    when 'SOGGY_RAINCOAT' then 5
    when 'KARENS_WIG'     then 15
    when 'SPIKY_COLLAR'   then 15
    when 'STONKS_HELMET'  then 40
    when 'BUFF_HARNESS'   then 50
    when 'LEGENDARY_BONE' then 150
    else 0
end;

alter table game_character add column gold integer not null default 0 check (gold >= 0);
