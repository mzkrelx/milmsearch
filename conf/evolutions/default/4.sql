# site_setting schema

# --- !Ups

CREATE TABLE site_setting (
    id              BIGINT       NOT NULL,
    footer_html     TEXT         NOT NULL DEFAULT '',
    CONSTRAINT site_setting_pkey PRIMARY KEY (id),
);
INSERT INTO site_setting VALUES (
    1,
    ''
);

# --- !Downs

DROP TABLE site_setting;
