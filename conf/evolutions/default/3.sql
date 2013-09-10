# ML Proposals schema

# --- !Ups

ALTER TABLE ml_proposal DROP COLUMN proposer_name;
ALTER TABLE ml_proposal ALTER COLUMN proposer_email DROP NOT NULL;


# --- !Downs

ALTER TABLE ml_proposal ADD COLUMN proposer_name VARCHAR(100) NOT NULL;
ALTER TABLE ml_proposal ALTER COLUMN proposer_email SET NOT NULL;

