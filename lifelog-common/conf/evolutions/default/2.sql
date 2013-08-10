## ダイエット記録

# --- !Ups

CREATE SEQUENCE diet_logs_id_seq;
CREATE TABLE diet_logs (
    id BIGINT NOT NULL DEFAULT nextval('diet_logs_id_seq'),
    member_id BIGINT NOT NULL,
    dtm DATETIME NOT NULL,
    weight DECIMAL(4, 1) NOT NULL,
    fat_rate DECIMAL(3, 1) NOT NULL,
    height DECIMAL(4, 1),
    note VARCHAR(1024),
    updated_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);
ALTER TABLE diet_logs ADD CONSTRAINT diet_logs_fk_1
    FOREIGN KEY (member_id) REFERENCES members (id);
CREATE INDEX diet_logs_member_id ON diet_logs(member_id);

# --- !Downs

DROP INDEX diet_logs_member_id;
DROP TABLE diet_logs;
DROP SEQUENCE diet_logs_id_seq;
