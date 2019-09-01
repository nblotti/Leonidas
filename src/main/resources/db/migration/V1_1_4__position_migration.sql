CREATE TABLE IF NOT EXISTS `POSITIONS`
(
  ID                             int       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  UNIQUE_ID                      VARCHAR(50),
  POS_DATE                       timestamp NOT NULL,
  POS_TYPE                       int       not NULL,
  SECURITY_ID                    VARCHAR(50),
  CURRENCY                       VARCHAR(50),
  QUANTITY                       float,
  ACCOUNT_ID                     int       not null,
  EXCHANGE                       VARCHAR(50),
  POS_VALUE                      FLOAT, //TODO NBL FIX ME
  POS_VALUE_REPORTING_CURRENCY   FLOAT,
  CMA                            FLOAT,
  UNREALIZED                     FLOAT,
  REALIZED                       FLOAT default 0,
  ACCOUNT_REPORTING_CURRENCY     VARCHAR(50) NOT NULL,
  TMA FLOAT,

);

CREATE INDEX position_date_currency ON POSITIONS   (POS_DATE, CURRENCY);
