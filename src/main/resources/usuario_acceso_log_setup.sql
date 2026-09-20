-- =====================================================================
-- SOPDI - Bitácora de accesos de usuarios
-- =====================================================================
-- Crea la tabla `usuario_acceso_log` que registra cada intento de
-- inicio de sesión (exitoso o fallido).
--
-- Columnas:
--   IdUsuario : FK a usuario.IdUsuario (NULL si las credenciales no
--               coinciden con ningún usuario).
--   Usuario   : nombre de usuario ingresado en el formulario.
--   FechaHora : marca de tiempo del intento (CURRENT_TIMESTAMP).
--   IP        : dirección IPv4/IPv6 del cliente; considera X-Forwarded-For.
--   Resultado : código del resultado:
--                 EXITO           – login correcto.
--                 CLAVE_INCORRECTA – usuario/contraseña no coinciden.
--                 INACTIVO        – usuario encontrado pero Estatus=INACTIVO.
--                 EMPRESA_INACTIVA – empresa del usuario con Estatus=INACTIVA.
--                 HORARIO         – acceso fuera del horario autorizado.
--                 IP_DENEGADA     – IP del cliente no está en la lista blanca.
--   Detalle   : mensaje de error cuando Resultado != EXITO; NULL si exitoso.
--
-- La aplicación ejecuta CREATE TABLE IF NOT EXISTS en cada login, por lo
-- que este script es informativo / para despliegue manual.
-- =====================================================================

CREATE TABLE IF NOT EXISTS usuario_acceso_log (
    IdLog     INT          NOT NULL AUTO_INCREMENT,
    IdUsuario INT          NULL,
    Usuario   VARCHAR(100) NULL,
    FechaHora DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    IP        VARCHAR(45)  NULL,
    Resultado VARCHAR(20)  NOT NULL,
    Detalle   VARCHAR(512) NULL,
    PRIMARY KEY (IdLog),
    KEY idx_uacceso_usuario (IdUsuario),
    KEY idx_uacceso_fecha   (FechaHora)
);
