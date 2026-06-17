-- =====================================================================
-- SOPDI - Calendario de eventos por usuario
-- =====================================================================
-- Crea la tabla `usuario_evento` que almacena los eventos del calendario
-- de cada usuario (CalendarView).
--
-- Notas:
--   * IdUsuario referencia a usuario.IdUsuario (dueño del evento).
--   * FechaInicio / FechaFin son DATETIME (fecha y hora).
--   * TodoElDia = 1 para eventos de día completo (se ignora la hora).
--   * Color : nombre de estilo del evento en el calendario (color1..color4).
--   * Estatus : 'ACTIVO' / 'ELIMINADO' (borrado lógico). El CalendarView
--     sólo muestra los 'ACTIVO'.
--
-- La aplicación también ejecuta un CREATE TABLE IF NOT EXISTS al abrir la
-- vista, por lo que este script es informativo / para despliegue manual.
-- =====================================================================

CREATE TABLE IF NOT EXISTS usuario_evento (
    IdEvento      INT          NOT NULL AUTO_INCREMENT,
    IdUsuario     INT          NOT NULL,
    Titulo        VARCHAR(255) NOT NULL,
    Descripcion   TEXT         NULL,
    Lugar         VARCHAR(255) NULL,
    FechaInicio   DATETIME     NOT NULL,
    FechaFin      DATETIME     NOT NULL,
    TodoElDia     TINYINT(1)   NOT NULL DEFAULT 0,
    Color         VARCHAR(20)  NULL,
    Estatus       VARCHAR(20)  NOT NULL DEFAULT 'ACTIVO',
    FechaCreacion TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (IdEvento),
    KEY idx_usuario_evento_usuario (IdUsuario),
    KEY idx_usuario_evento_fechas (FechaInicio, FechaFin)
);
