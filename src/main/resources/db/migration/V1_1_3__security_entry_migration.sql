CREATE TABLE IF NOT EXISTS `security_entry`
(

    ID                             int         NOT NULL AUTO_INCREMENT PRIMARY KEY,
    ORDER_ID                       int         NOT NULL,
    SECURITY_ACCOUNT               int         NOT NULL,
    DEBIT_CREDIT_CODE              int         NOT NULL,
    ENTRY_DATE                     timestamp   NOT NULL,
    VALUE_DATE                     timestamp   NOT NULL,
    QUANTITY                       float       NOT NULL,
    EXCHANGE                       varchar(50) NOT NULL,
    SECURITY_ID                    varchar(50) NOT NULL,
    NET_AMOUNT                     varchar(50) NOT NULL,
    ACCOUNT_REPORTING_CURRENCY     varchar(50) NOT NULL,
    ENTRY_VALUE_REPORTING_CURRENCY FLOAT,
    GROSS_AMOUNT                   varchar(50) NOT NULL,
    CURRENCY                       varchar(50) NOT NULL,
    FX_EXCHANGE_RATE               float       NOT NULL,
    STATUS                         int default 0

);





