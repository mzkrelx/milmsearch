# ML Proposals schema

# --- !Ups

ALTER TABLE ml_proposal DROP CONSTRAINT ml_proposal_archive_type_check;
ALTER TABLE ml_proposal ADD CONSTRAINT ml_proposal_archive_type_check CHECK (archive_type in ('sourceforgejp', 'mailman', 'other'));


# --- !Downs

ALTER TABLE ml_proposal DROP CONSTRAINT ml_proposal_archive_type_check;
ALTER TABLE ml_proposal ADD CONSTRAINT ml_proposal_archive_type_check CHECK (archive_type in ('mailman', 'other'));
