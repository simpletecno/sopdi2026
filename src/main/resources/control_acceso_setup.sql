-- =====================================================================
-- SOPDI - Restricciones de acceso por usuario (horario / IPs autorizadas)
-- =====================================================================
-- Agrega 3 columnas a la tabla `usuario`:
--   HorarioAccesoInicio : hora de inicio del acceso permitido (NULL = sin restriccion)
--   HorarioAccesoFin    : hora de fin    del acceso permitido (NULL = sin restriccion)
--   IpsAutorizadas      : lista de IPs/CIDR separadas por coma  (NULL/'' = sin restriccion)
--
-- Reglas:
--   * Ambas restricciones son OPCIONALES por usuario.
--   * Horario: requiere AMBAS horas. Soporta rangos que cruzan medianoche
--     (ej. 22:00 - 06:00).
--   * IPs: acepta IP exacta (192.168.1.10) o rango CIDR (200.30.40.0/24),
--     separadas por coma.
-- =====================================================================


-- ---------------------------------------------------------------------
-- MySQL / MariaDB
-- ---------------------------------------------------------------------
ALTER TABLE usuario
    ADD COLUMN HorarioAccesoInicio TIME         NULL,
    ADD COLUMN HorarioAccesoFin    TIME         NULL,
    ADD COLUMN IpsAutorizadas      VARCHAR(512) NULL;


-- ---------------------------------------------------------------------
-- SQL Server  (ejecutar SOLO si el motor es SQL Server; comentar el bloque MySQL de arriba)
-- ---------------------------------------------------------------------
-- ALTER TABLE usuario ADD
--     HorarioAccesoInicio TIME          NULL,
--     HorarioAccesoFin    TIME          NULL,
--     IpsAutorizadas      VARCHAR(512)  NULL;


-- =====================================================================
-- Ejemplos de configuracion (opcionales)
-- =====================================================================

-- 1) Usuario que solo puede ingresar de 08:00 a 18:00 (sin restriccion de IP):
-- UPDATE usuario
--    SET HorarioAccesoInicio = '08:00:00',
--        HorarioAccesoFin    = '18:00:00'
--  WHERE Usuario = 'jperez';

-- 2) Usuario que solo puede ingresar desde la red de la oficina (sin restriccion de horario):
-- UPDATE usuario
--    SET IpsAutorizadas = '192.168.1.0/24, 200.30.40.15'
--  WHERE Usuario = 'mlopez';

-- 3) Usuario con ambas restricciones (horario nocturno + IP fija):
-- UPDATE usuario
--    SET HorarioAccesoInicio = '22:00:00',
--        HorarioAccesoFin    = '06:00:00',
--        IpsAutorizadas      = '10.0.0.25'
--  WHERE Usuario = 'vigilancia';

-- 4) Quitar todas las restricciones a un usuario:
-- UPDATE usuario
--    SET HorarioAccesoInicio = NULL,
--        HorarioAccesoFin    = NULL,
--        IpsAutorizadas      = NULL
--  WHERE Usuario = 'jperez';