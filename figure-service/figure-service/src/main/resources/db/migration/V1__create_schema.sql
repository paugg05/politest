-- Base de datos propia del figure-service.
CREATE TABLE figure (
    code        VARCHAR(30) NOT NULL,
    birth_year  SMALLINT    NOT NULL,
    death_year  SMALLINT    NOT NULL,
    color       CHAR(7)     NOT NULL,
    sort_order  INT         NOT NULL,
    active      TINYINT(1)  NOT NULL DEFAULT 1,
    PRIMARY KEY (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- Nombre y descripción de cada personaje en cada idioma.
CREATE TABLE figure_text (
    figure_code  VARCHAR(30)  NOT NULL,
    lang         CHAR(2)      NOT NULL,
    name         VARCHAR(100) NOT NULL,
    description  VARCHAR(300) NOT NULL,
    PRIMARY KEY (figure_code, lang),
    CONSTRAINT fk_text_figure FOREIGN KEY (figure_code) REFERENCES figure (code),
    CONSTRAINT chk_text_lang CHECK (lang IN ('es', 'ca', 'en'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- Un personaje puede no tener posición en algún eje (si no era un tema de su época).
CREATE TABLE figure_position (
    figure_code  VARCHAR(30)      NOT NULL,
    axis_code    VARCHAR(20)      NOT NULL,
    right_pct    TINYINT UNSIGNED NOT NULL,
    confidence   TINYINT UNSIGNED NOT NULL,
    note         VARCHAR(400)     NOT NULL,
    PRIMARY KEY (figure_code, axis_code),
    CONSTRAINT fk_position_figure FOREIGN KEY (figure_code) REFERENCES figure (code),
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
