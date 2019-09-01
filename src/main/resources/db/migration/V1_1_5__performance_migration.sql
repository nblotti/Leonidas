
create view IF NOT EXISTS ACCOUNT_TWR_PERF AS
select * from(
               select account_id, pos_date, 1 type,   ((sum(POS_VALUE_REPORTING_CURRENCY) - sum(QUANTITY*  CMA *tMA) ) / sum(QUANTITY*  CMA *tMA) )*100 perf from positions  group by account_id, POS_DATE
               union
               select account_id, pos_date, 2 type, (( sum(POS_VALUE* tMA)-sum(QUANTITY*  CMA *tMA) ) /sum(QUANTITY*  CMA *tMA))*100 perf  from positions  group by account_id, POS_DATE
               union
               select account_id, pos_date, 3 type, ((sum(POS_VALUE_REPORTING_CURRENCY) -sum (POS_VALUE*tMA)) / sum (QUANTITY*  CMA *tMA))*100 perf from positions  group by account_id, POS_DATE)
order by pos_date asc, type asc;
