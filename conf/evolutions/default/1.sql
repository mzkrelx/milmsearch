# ML Proposals schema

# --- !Ups

CREATE SEQUENCE ml_proposal_id_seq;
CREATE TABLE ml_proposal (
    id             INTEGER      DEFAULT nextval('ml_proposal_id_seq') PRIMARY KEY,
    proposer_name  VARCHAR(100) NOT NULL,
    proposer_email VARCHAR(100) NOT NULL,
    ml_title       VARCHAR(100) NOT NULL,
    status         VARCHAR(20)  NOT NULL CHECK (status in ('new', 'accepted', 'rejected')),
    archive_type   VARCHAR(20)  NOT NULL CHECK (archive_type in ('mailman', 'other')),
    archive_url    VARCHAR(200) NOT NULL,
    message        TEXT         NOT NULL,
    judged_at      TIMESTAMP,
    created_at     TIMESTAMP    NOT NULL,
    updated_at     TIMESTAMP    NOT NULL
);

# --- !Downs

DROP TABLE ml_proposal;
