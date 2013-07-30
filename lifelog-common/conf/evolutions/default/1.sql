## 管理アカウント

# --- !Ups

CREATE SEQUENCE admins_id_seq;
CREATE TABLE admins (
    id INTEGER NOT NULL DEFAULT nextval('admins_id_seq'),
    login_id VARCHAR(32) NOT NULL,
    nickname VARCHAR(255) NOT NULL,
    passwd VARCHAR(64) NOT NULL DEFAULT '',
    updated_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);
CREATE UNIQUE INDEX admins_login_id ON admins(login_id);

INSERT INTO admins (
    id,
    login_id,
    nickname,
    updated_at
) VALUES (
    0,
    'superadmin',
    'SuperAdmin',
    CURRENT_TIMESTAMP
);

# --- !Downs

DROP INDEX admins_login_id;
DROP TABLE admins;
DROP SEQUENCE admins_id_seq;
