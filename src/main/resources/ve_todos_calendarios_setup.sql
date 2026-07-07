-- =====================================================================
-- SOPDI - Permiso para ver los calendarios de otros usuarios
-- =====================================================================
-- Agrega la columna `VeTodosCalendarios` a la tabla `usuario`:
--   VeTodosCalendarios : 1 = puede ver el calendario de cualquier usuario
--                        0 = solo ve su propio calendario (valor por defecto)
--
-- Nota: la aplicacion (CalendarView) crea esta columna de forma automatica
--       al abrir el calendario (consulta information_schema y ejecuta el ALTER
--       solo si no existe), por lo que este script es principalmente de
--       referencia / documentacion.
-- =====================================================================


-- ---------------------------------------------------------------------
-- MySQL (idempotente; MySQL NO soporta "ADD COLUMN IF NOT EXISTS")
-- ---------------------------------------------------------------------
SET @exist := (SELECT COUNT(*) FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE()
                 AND TABLE_NAME   = 'usuario'
                 AND COLUMN_NAME  = 'VeTodosCalendarios');
SET @ddl := IF(@exist = 0,
               'ALTER TABLE usuario ADD COLUMN VeTodosCalendarios TINYINT(1) NOT NULL DEFAULT 0',
               'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;


-- ---------------------------------------------------------------------
-- MariaDB 10.0+  (soporta la sintaxis condicional directamente)
-- ---------------------------------------------------------------------
-- ALTER TABLE usuario
--     ADD COLUMN IF NOT EXISTS VeTodosCalendarios TINYINT(1) NOT NULL DEFAULT 0;


-- ---------------------------------------------------------------------
-- SQL Server  (ejecutar SOLO si el motor es SQL Server)
-- ---------------------------------------------------------------------
-- IF COL_LENGTH('usuario', 'VeTodosCalendarios') IS NULL
--     ALTER TABLE usuario ADD VeTodosCalendarios TINYINT NOT NULL DEFAULT 0;


-- =====================================================================
-- Ejemplos de configuracion (opcionales)
-- =====================================================================

-- 1) Habilitar a un administrador/gerente para ver todos los calendarios:
-- UPDATE usuario
--    SET VeTodosCalendarios = 1
--  WHERE Usuario = 'jaguirre@simpletecno.com';

-- 2) Habilitar a todos los usuarios con perfil de administrador:
-- UPDATE usuario
--    SET VeTodosCalendarios = 1
--  WHERE Upper(Perfil) LIKE '%ADMIN%';

-- 3) Revocar el permiso a un usuario:
-- UPDATE usuario
--    SET VeTodosCalendarios = 0
--  WHERE Usuario = 'jperez';
