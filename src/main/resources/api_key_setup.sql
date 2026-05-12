-- Tabla para gestionar las API Keys de la REST API
CREATE TABLE IF NOT EXISTS api_key (
    Id          INT AUTO_INCREMENT PRIMARY KEY,
    KeyValue    VARCHAR(255) NOT NULL UNIQUE,
    Descripcion VARCHAR(255),
    Activo      TINYINT(1)   NOT NULL DEFAULT 1,
    CreadoEn    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Insertar una API Key de ejemplo (cámbiala en producción)
INSERT INTO api_key (KeyValue, Descripcion, Activo)
VALUES ('sopdi-api-key-2026', 'Clave inicial de desarrollo', 1);