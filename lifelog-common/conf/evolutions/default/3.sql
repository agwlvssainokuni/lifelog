## 非同期タスク

# --- !Ups

CREATE SEQUENCE async_tasks_id_seq;
CREATE TABLE async_tasks (
    id BIGINT NOT NULL DEFAULT nextval('async_tasks_id_seq'),
    member_id BIGINT NOT NULL,
    name VARCHAR(256) NOT NULL,
    status INTEGER NOT NULL,
    start_dtm DATETIME,
    end_dtm DATETIME,
    total_count BIGINT,
    ok_count BIGINT,
    ng_count BIGINT,
    updated_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);
ALTER TABLE async_tasks ADD CONSTRAINT async_tasks_fk_1
    FOREIGN KEY (member_id) REFERENCES members (id);
CREATE INDEX async_tasks_member_id ON async_tasks(member_id);

# --- !Downs

DROP INDEX async_tasks_member_id;
DROP TABLE async_tasks;
DROP SEQUENCE async_tasks_id_seq;
