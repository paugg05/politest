-- Base de datos propia del party-service.
CREATE TABLE party (
    code        VARCHAR(20)  NOT NULL,
    name        VARCHAR(100) NOT NULL,
    color       CHAR(7)      NOT NULL,
    sort_order  INT          NOT NULL,
    active      TINYINT(1)   NOT NULL DEFAULT 1,
    PRIMARY KEY (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE party_position (
    party_code  VARCHAR(20)      NOT NULL,
    axis_code   VARCHAR(20)      NOT NULL,
    right_pct   TINYINT UNSIGNED NOT NULL,
    confidence  TINYINT UNSIGNED NOT NULL,
    note        VARCHAR(600)     NOT NULL,
    source_url  VARCHAR(500)     NULL,
    PRIMARY KEY (party_code, axis_code),
    CONSTRAINT fk_position_party FOREIGN KEY (party_code) REFERENCES party (code),
    CONSTRAINT chk_right_pct CHECK (right_pct <= 100),
    CONSTRAINT chk_confidence CHECK (confidence BETWEEN 1 AND 3),
    CONSTRAINT chk_axis CHECK (axis_code IN
        ('ECON', 'WELFARE', 'LIBERTY', 'CULTURE', 'NATION', 'ECOLOGY', 'DEMOCRACY', 'CHANGE'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- Una sola fila: versión y estado del conjunto de datos.
CREATE TABLE dataset_info (
    id          TINYINT      NOT NULL,
    version     VARCHAR(40)  NOT NULL,
    status      VARCHAR(20)  NOT NULL,
    note        VARCHAR(600) NOT NULL,
    updated_on  DATE         NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT chk_single_row CHECK (id = 1)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
