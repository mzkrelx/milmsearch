# ML Proposals schema

# --- !Ups

CREATE SEQUENCE ml_proposal_id_seq;
CREATE TABLE ml_proposal (
    id             BIGINT       DEFAULT nextval('ml_proposal_id_seq'),
    proposer_name  VARCHAR(100) NOT NULL,
    proposer_email VARCHAR(100) NOT NULL,
    ml_title       VARCHAR(100) NOT NULL,
    status         VARCHAR(20)  NOT NULL,
    archive_type   VARCHAR(20)  NOT NULL,
    archive_url    VARCHAR(200) NOT NULL,
    message        TEXT         NOT NULL,
    judged_at      TIMESTAMP,
    created_at     TIMESTAMP    NOT NULL,
    updated_at     TIMESTAMP    NOT NULL,
    CONSTRAINT ml_proposal_pkey PRIMARY KEY (id),
    CONSTRAINT ml_proposal_status_check CHECK (status in ('new', 'accepted', 'rejected')),
    CONSTRAINT ml_proposal_archive_type_check CHECK (archive_type in ('mailman', 'other'))
);

# --- !Downs

DROP TABLE ml_proposal;
DROP SEQUENCE ml_proposal_id_seq;
