CREATE TABLE tl_quests
(
  id         INTEGER PRIMARY KEY,
  theme_id   INTEGER,
  quest      VARCHAR,
  help       VARCHAR,
  answers    JSON,
  correct    INTEGER,
  image_file VARCHAR(100)
);

CREATE TABLE tl_users
(
  id           INTEGER PRIMARY KEY,
  created_date TIMESTAMP WITHOUT TIME ZONE,
  last_payment TIMESTAMP WITHOUT TIME ZONE,
  tariff       VARCHAR(100)
);

CREATE TABLE tl_users_quests
(
  user_id         INTEGER,
  quest_id        INTEGER,
  correct_answers INTEGER,
  wrong_answers   INTEGER
);

ALTER TABLE tl_users_quests
  ADD CONSTRAINT users_quests_user_id FOREIGN KEY (user_id)
    REFERENCES tl_users (id);

ALTER TABLE tl_users_quests
  ADD CONSTRAINT users_quests_quest_id FOREIGN KEY (quest_id)
    REFERENCES tl_quests (id);


