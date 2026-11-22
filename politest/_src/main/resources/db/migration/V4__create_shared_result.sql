-- V4: resultados compartidos por enlace.
-- Solo se guarda el porcentaje de cada eje (sin nombre, IP ni otros datos personales)
-- y únicamente cuando la persona pulsa "Crear enlace para compartir".
-- El id es un token aleatorio sensible a mayúsculas: por eso usa ascii_bin.

CREATE TABLE shared_result (
    id         VARCHAR(24) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE shared_result_score (
    result_id VARCHAR(24) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    axis_code VARCHAR(20) NOT NULL,
    right_pct INT         NOT NULL,
    PRIMARY KEY (result_id, axis_code),
    CONSTRAINT fk_shared_result_score_result FOREIGN KEY (result_id) REFERENCES shared_result (id) ON DELETE CASCADE,
    CONSTRAINT fk_shared_result_score_axis FOREIGN KEY (axis_code) REFERENCES axis (code),
    CONSTRAINT chk_shared_result_score_pct CHECK (right_pct BETWEEN 0 AND 100)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
