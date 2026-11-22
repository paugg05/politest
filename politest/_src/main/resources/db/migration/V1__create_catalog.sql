-- V1: esquema del catálogo (ejes y preguntas) con traducciones
-- Convención: left_label = polo A (0 %), right_label = polo B (100 %)

CREATE TABLE axis (
    code       VARCHAR(20) NOT NULL,
    sort_order INT         NOT NULL,
    PRIMARY KEY (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE axis_translation (
    axis_code   VARCHAR(20) NOT NULL,
    locale      VARCHAR(5)  NOT NULL,
    name        VARCHAR(60) NOT NULL,
    left_label  VARCHAR(80) NOT NULL,
    right_label VARCHAR(80) NOT NULL,
    PRIMARY KEY (axis_code, locale),
    CONSTRAINT fk_axis_translation_axis FOREIGN KEY (axis_code) REFERENCES axis (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE question (
    id        BIGINT      NOT NULL AUTO_INCREMENT,
    axis_code VARCHAR(20) NOT NULL,
    direction INT         NOT NULL,
    weight    DOUBLE      NOT NULL DEFAULT 1.0,
    active    BOOLEAN     NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    CONSTRAINT fk_question_axis FOREIGN KEY (axis_code) REFERENCES axis (code),
    CONSTRAINT chk_question_direction CHECK (direction IN (-1, 1))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE question_translation (
    question_id BIGINT       NOT NULL,
    locale      VARCHAR(5)   NOT NULL,
    statement   VARCHAR(400) NOT NULL,
    PRIMARY KEY (question_id, locale),
    CONSTRAINT fk_question_translation_question FOREIGN KEY (question_id) REFERENCES question (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
