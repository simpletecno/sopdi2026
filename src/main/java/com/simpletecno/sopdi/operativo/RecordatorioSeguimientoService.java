package com.simpletecno.sopdi.operativo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Calcula los recordatorios pendientes de seguimiento de VISITAS y REUNIONES
 * para un usuario. Es la fuente única de la lógica: la usa la UI (aviso en
 * pantalla) y, en el futuro, un proceso programado para envío por correo.
 *
 * Por eso todos los métodos reciben la {@link Connection} como parámetro: desde
 * la UI se pasa la conexión de sesión y desde un scheduler se pasaría una
 * conexión propia (ver MyDatabaseProvider.getNewConnection()).
 *
 * @author Jose Aguirre
 */
public class RecordatorioSeguimientoService {

    private static final Logger LOG = Logger.getLogger(RecordatorioSeguimientoService.class.getName());

    /**
     * Asegura que existan en {@code visita_inspeccion_tarea_seguimiento} las
     * columnas que necesita el recordatorio. Idéntico patrón idempotente que
     * ProveedorEmpresaForm.asegurarColumnas(): consulta information_schema antes
     * de cada ALTER porque MariaDB no siempre soporta ADD COLUMN IF NOT EXISTS.
     */
    public void asegurarColumnas(Connection cnx) {
        String[][] columnas = {
            {"FechaProximoSeguimiento", "DATE NULL"},
            {"Atendido", "TINYINT(1) NOT NULL DEFAULT 0"}
        };
        for (String[] col : columnas) {
            String existeSql = "SELECT COUNT(*) FROM information_schema.COLUMNS "
                    + " WHERE TABLE_SCHEMA = DATABASE() "
                    + " AND TABLE_NAME = 'visita_inspeccion_tarea_seguimiento' "
                    + " AND COLUMN_NAME = '" + col[0] + "'";
            try (Statement st = cnx.createStatement();
                 ResultSet rs = st.executeQuery(existeSql)) {
                boolean existe = rs.next() && rs.getInt(1) > 0;
                if (!existe) {
                    try (Statement stAlter = cnx.createStatement()) {
                        stAlter.executeUpdate("ALTER TABLE visita_inspeccion_tarea_seguimiento "
                                + " ADD COLUMN " + col[0] + " " + col[1]);
                    }
                }
            } catch (Exception ex) {
                LOG.log(Level.WARNING, "No se pudo asegurar la columna "
                        + "visita_inspeccion_tarea_seguimiento." + col[0] + ": {0}", ex.getMessage());
            }
        }
    }

    /**
     * Devuelve los recordatorios pendientes del usuario en la empresa dada:
     *   A) seguimientos cuya FechaProximoSeguimiento ya llegó y siguen abiertos
     *      (destinatario: quien creó el seguimiento).
     *   B) tareas de visita no autorizadas cuyo plazo (FechaUltimoEstatus o
     *      inicio de la visita + DiasHabiles) ya venció
     *      (destinatario: quien creó la visita, CreadoUsuario).
     *
     * El plazo de la consulta B usa días calendario como aproximación de días
     * hábiles (limitación conocida de la v1).
     */
    public List<Recordatorio> obtenerPendientes(Connection cnx, String idUsuario, String idEmpresa) {
        List<Recordatorio> lista = new ArrayList<>();
        if (cnx == null || idUsuario == null || idEmpresa == null) {
            return lista;
        }

        // ---- Consulta A: seguimientos con fecha próxima vencida ----
        String sqlA =
            " SELECT s.IdSeguimiento, s.Observacion, s.FechaProximoSeguimiento, "
          + "        t.Descripcion AS Tarea, v.CodigoVisita, cli.Nombre AS Cliente "
          + " FROM visita_inspeccion_tarea_seguimiento s "
          + " INNER JOIN visita_inspeccion_tarea t ON t.IdVisitaInspeccionTarea = s.IdVisitaInspeccionTarea "
          + " INNER JOIN visita_inspeccion v ON v.IdVisitaInspeccion = t.IdVisitaInspeccion "
          + " LEFT JOIN proveedor_empresa cli ON cli.IdProveedor = v.IdCliente AND cli.IdEmpresa = v.IdEmpresa "
          + " WHERE s.IdUsuario = ? "
          + "   AND v.IdEmpresa = ? "
          + "   AND s.FechaProximoSeguimiento IS NOT NULL "
          + "   AND s.FechaProximoSeguimiento <= CURDATE() "
          + "   AND s.Atendido = 0 "
          + " ORDER BY s.FechaProximoSeguimiento ASC ";

        try (PreparedStatement ps = cnx.prepareStatement(sqlA)) {
            ps.setString(1, idUsuario);
            ps.setString(2, idEmpresa);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Recordatorio r = new Recordatorio();
                    r.tipo = Recordatorio.TIPO_SEGUIMIENTO;
                    r.idSeguimiento = rs.getString("IdSeguimiento");
                    r.referencia = rs.getString("CodigoVisita");
                    r.lugarCliente = rs.getString("Cliente") == null ? "" : rs.getString("Cliente");
                    r.titulo = rs.getString("Tarea") == null ? "" : rs.getString("Tarea");
                    r.detalle = rs.getString("Observacion") == null ? "" : rs.getString("Observacion");
                    java.sql.Date f = rs.getDate("FechaProximoSeguimiento");
                    r.fecha = (f == null) ? "" : new java.text.SimpleDateFormat("dd/MM/yyyy").format(f);
                    lista.add(r);
                }
            }
        } catch (Exception ex) {
            LOG.log(Level.WARNING, "Error en consulta A (seguimientos vencidos): {0}", ex.getMessage());
        }

        // ---- Consulta B: tareas de visita vencidas ----
        String sqlB =
            " SELECT t.Descripcion AS Tarea, t.Estatus, "
          + "        DATE_ADD(COALESCE(t.FechaUltimoEstatus, v.FechaYHoraInicio), INTERVAL t.DiasHabiles DAY) AS FechaLimite, "
          + "        v.CodigoVisita, cli.Nombre AS Cliente "
          + " FROM visita_inspeccion_tarea t "
          + " INNER JOIN visita_inspeccion v ON v.IdVisitaInspeccion = t.IdVisitaInspeccion "
          + " LEFT JOIN proveedor_empresa cli ON cli.IdProveedor = v.IdCliente AND cli.IdEmpresa = v.IdEmpresa "
          + " WHERE v.CreadoUsuario = ? "
          + "   AND v.IdEmpresa = ? "
          + "   AND (t.Estatus IS NULL OR t.Estatus NOT IN ('AUTORIZADA','RECHAZADA')) "
          + "   AND t.DiasHabiles > 0 "
          + "   AND DATE_ADD(COALESCE(t.FechaUltimoEstatus, v.FechaYHoraInicio), INTERVAL t.DiasHabiles DAY) < CURDATE() "
          + " ORDER BY FechaLimite ASC ";

        try (PreparedStatement ps = cnx.prepareStatement(sqlB)) {
            ps.setString(1, idUsuario);
            ps.setString(2, idEmpresa);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Recordatorio r = new Recordatorio();
                    r.tipo = Recordatorio.TIPO_TAREA_VENCIDA;
                    r.idSeguimiento = null;
                    r.referencia = rs.getString("CodigoVisita");
                    r.lugarCliente = rs.getString("Cliente") == null ? "" : rs.getString("Cliente");
                    r.titulo = rs.getString("Tarea") == null ? "" : rs.getString("Tarea");
                    String estatus = rs.getString("Estatus") == null ? "PENDIENTE" : rs.getString("Estatus");
                    r.detalle = "Tarea " + estatus.trim() + " con plazo vencido.";
                    java.sql.Date f = rs.getDate("FechaLimite");
                    r.fecha = (f == null) ? "" : new java.text.SimpleDateFormat("dd/MM/yyyy").format(f);
                    lista.add(r);
                }
            }
        } catch (Exception ex) {
            LOG.log(Level.WARNING, "Error en consulta B (tareas vencidas): {0}", ex.getMessage());
        }

        return lista;
    }

    /**
     * Marca un seguimiento como atendido para que deje de aparecer en los avisos.
     */
    public boolean marcarAtendido(Connection cnx, String idSeguimiento) {
        if (cnx == null || idSeguimiento == null) {
            return false;
        }
        String sql = "UPDATE visita_inspeccion_tarea_seguimiento SET Atendido = 1 WHERE IdSeguimiento = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, idSeguimiento);
            return ps.executeUpdate() > 0;
        } catch (Exception ex) {
            LOG.log(Level.WARNING, "Error al marcar seguimiento atendido: {0}", ex.getMessage());
            return false;
        }
    }
}
