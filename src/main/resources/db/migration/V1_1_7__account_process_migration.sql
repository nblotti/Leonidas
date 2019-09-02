CREATE TABLE IF NOT EXISTS `MARKET_PROCESS`
(

    ID                   int NOT NULL AUTO_INCREMENT PRIMARY KEY,
    ACCOUNT_ID           int NOT NULL,
    ORDER_ID             int NOT NULL,
    CASH_ENTRY           timestamp,
    ORDER_TYPE           varchar(10),
    SECURITY_ENTRY       timestamp,
    CASH_POSITION        timestamp,
    SECURITY_POSITION    timestamp,
    CASH_PERFORMANCE     timestamp,
    SECURITY_PERFORMANCE timestamp
);

create VIEW IF NOT EXISTS `RUNNING_ORDER_PROCESS` as
    SELECT account_id, count(*)
    FROM MARKET_PROCESS
    where ORDER_TYPE = 1
      and (CASH_ENTRY is null or CASH_POSITION is null or CASH_PERFORMANCE is null)
    group by ACCOUNT_ID
    union
    SELECT account_id, count(*)
    FROM MARKET_PROCESS
    where ORDER_TYPE = 2
      and (SECURITY_ENTRY is null or SECURITY_POSITION is null or SECURITY_PERFORMANCE is null)
    group by ACCOUNT_ID
    union
    SELECT account_id, count(*)
    FROM MARKET_PROCESS
    where ORDER_TYPE = 0
      and (CASH_ENTRY is null or SECURITY_ENTRY is null or CASH_POSITION is null or SECURITY_POSITION is null or
           CASH_PERFORMANCE is null or SECURITY_PERFORMANCE is null)
    group by ACCOUNT_ID;










