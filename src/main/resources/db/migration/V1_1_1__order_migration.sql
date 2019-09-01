CREATE TABLE IF NOT EXISTS `orders`
(

  ID                 int         NOT NULL AUTO_INCREMENT PRIMARY KEY,
  CI_ORD_ID          varchar(50) NOT NULL,
  ACCOUNT_ID         int         NOT NULL,
  ORDER_TYPE         varchar(50) NOT NULL,
  SYMBOL             varchar(50),
  EXCHANGE           varchar(50),
  SECURITY_ID        varchar(50),
  SECURITY_ID_SOURCE varchar(50),
  SIDE               int         NOT NULL,
  TRANSACT_TIME      timestamp   NOT NULL,
  ORDER_QTY_DATA     FLOAT,
  CASH_CURRENCY      varchar(50),
  AMOUNT             FLOAT,
  STATUS             int default 0


);
