## ドライブ記録

# --- !Ups

CREATE SEQUENCE drive_logs_id_seq;
CREATE TABLE drive_logs (
    id BIGINT NOT NULL DEFAULT nextval('drive_logs_id_seq'),
    member_id BIGINT NOT NULL,
    dt DATETIME NOT NULL,
    tripmeter DECIMAL(5, 1) NOT NULL,
    fuelometer DECIMAL(3, 1) NOT NULL,
    remaining DECIMAL(4, 0) NOT NULL,
    odometer DECIMAL(9, 0) NOT NULL,
    note VARCHAR(1024),
    updated_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);
ALTER TABLE drive_logs ADD CONSTRAINT drive_logs_fk_1
    FOREIGN KEY (member_id) REFERENCES members (id);
CREATE INDEX drive_logs_member_id ON drive_logs(member_id);

CREATE TABLE refuel_logs (
    id BIGINT NOT NULL,
    unit DECIMAL(5, 1) NOT NULL,
    quantity DECIMAL(5, 2) NOT NULL,
    price DECIMAL(7, 0) NOT NULL,
    note VARCHAR(1024),
    updated_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);
ALTER TABLE refuel_logs ADD CONSTRAINT refuel_logs_fk_1
    FOREIGN KEY (id) REFERENCES drive_logs (id);

# --- !Downs

DROP TABLE refuel_logs;
DROP INDEX drive_logs_member_id;
DROP TABLE drive_logs;
DROP SEQUENCE drive_logs_id_seq;
