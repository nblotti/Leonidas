CREATE TABLE IF NOT EXISTS `ACCOUNT_RELATION`
(

    ID                int NOT NULL AUTO_INCREMENT PRIMARY KEY,
    FIRST_ACCOUNT_ID  int NOT NULL,
    SECOND_ACCOUNT_ID int NOT NULL,
    CREATION_DATE     timestamp,
    RELATION_TYPE     varchar(10),
    RELATION_STATUS   varchar(10)
);
