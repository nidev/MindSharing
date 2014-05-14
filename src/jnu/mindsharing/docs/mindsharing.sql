USE mindsharing;

-- 어휘 표현
CREATE TABLE Expression (
	id INT NOT NULL AUTO_INCREMENT,
	word    VARCHAR(128) NOT NULL, -- 단어
	type    SMALLINT     NOT NULL, -- 단어유형(형용사, 동사, 이모티콘)
	joy     SMALLINT     NOT NULL, -- joy
	sorrow  SMALLINT     NOT NULL, -- sorrow
	growth  SMALLINT     NOT NULL, -- growth
	cease   SMALLINT     NOT NULL,  -- cease
	PRIMARY KEY (id)
) ENGINE=INNODB;

-- 해시키와 단어에 대해 인덱스를 생성한다.
CREATE INDEX ExprIndex ON Expression (id, word);

CREATE TABLE EmotionMemory (
	id         INT      NOT NULL AUTO_INCREMENT, -- 번호
	exprid     INT      NOT NULL,
	n_joy      DOUBLE       NOT NULL, -- 정규화 joy값
	n_sorrow   DOUBLE       NOT NULL, -- 정규화 sorrow값
	n_growth   DOUBLE       NOT NULL, -- 정규화 growth값
	n_cease    DOUBLE       NOT NULL, -- 정규화 cease값
	timestamp  DATE         NOT NULL, -- 입력 시간(초)
	sourcetext VARCHAR(255) NULL ,     -- sourcesentence
	PRIMARY KEY(id),
	CONSTRAINT fk_exprid FOREIGN KEY (exprid) REFERENCES expression (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=INNODB;



