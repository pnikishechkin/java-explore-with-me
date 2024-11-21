DROP TABLE IF EXISTS stats;
CREATE TABLE stats
(
    id   INT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    app  VARCHAR(255),
    uri  VARCHAR(255),
    ip   VARCHAR(15),
    time TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_stats PRIMARY KEY (id)
)