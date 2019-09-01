CREATE TABLE IF NOT EXISTS `cash_entry`
(

  ID                             int         NOT NULL AUTO_INCREMENT PRIMARY KEY,
  CI_ORD_ID                      varchar(50) NOT NULL,
  CASH_ACCOUNT                   int         NOT NULL,
  DEBIT_CREDIT_CODE              int         NOT NULL,
  ENTRY_DATE                     timestamp   NOT NULL,
  VALUE_DATE                     timestamp   NOT NULL,
  NET_AMOUNT                     FLOAT       NOT NULL,
  ACCOUNT_REPORTING_CURRENCY     varchar(50) NOT NULL,
  ENTRY_VALUE_REPORTING_CURRENCY FLOAT,
  GROSS_AMOUNT                   FLOAT       NOT NULL,
  CURRENCY                       varchar(50) NOT NULL,
  FX_EXCHANGE_RATE               float,
  STATUS                         int default 0

);





