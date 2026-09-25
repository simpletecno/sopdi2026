package com.simpletecno.sopdi.utilerias;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Calcula los recordatorios de EVENTOS del calendario del usuario (tabla
 * usuario_evento):
 *   - Eventos de HOY (su rango incluye la fecha actual) que aún no se realizaron.
 *   - Eventos VENCIDOS (ya pasó su fecha fin) que no se marcaron como realizados.
 *
 * Igual que {@link RecordatorioSeguimientoService}, recibe la {@link Connection}
 * como parámetro para poder reutilizarse desde la UI y desde un scheduler.
 *
 * @author Jose Aguirre
 */
public class RecordatorioEventoService {

    private static final Logger LOG = Logger.getLogger(RecordatorioEventoService.class.getName());

    /**
     * Asegura la columna {@code Realizado} en usuario_evento (baja de cumplimiento,
     * independiente de {@code Estatus} que marca ACTIVO/ELIMINADO). Patrón
     * idempotente con information_schema.
     */
    public void asegurarColumnas(Connection cnx) {
        String existeSql = "SELECT COUNT(*) FROM information_schema.COLUMNS "
                + " WHERE TABLE_SCHEMA = DATABASE() "
                + " AND TABLE_NAME = 'usuario_evento' "
                + " AND COLUMN_NAME = 'Realizado'";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(existeSql)) {
            boolean existe = rs.next() && rs.getInt(1) > 0;
            if (!existe) {
                try (Statement stAlter = cnx.createStatement()) {
                    stAlter.executeUpdate("ALTER TABLE usuario_evento "
                            + " ADD COLUMN Realizado TINYINT(1) NOT NULL DEFAULT 0");
                }
            }
        } catch (Exception ex) {
            LOG.log(Level.WARNING, "No se pudo asegurar la columna usuario_evento.Realizado: {0}", ex.getMessage());
        }
    }

    /**
     * Devuelve los recordatorios de eventos del usuario: los de hoy y los
     * vencidos no realizados.
     */
    public List<Recordatorio> obtenerPendientes(Connection cnx, String idUsuario) {
        List<Recordatorio> lista = new ArrayList<>();
        if (cnx == null || idUsuario == null) {
            return lista;
        }

        String sql =
            " SELECT IdEvento, Titulo, Descripcion, Lugar, FechaInicio, FechaFin, "
          + "        CASE WHEN DATE(FechaFin) < CURDATE() THEN 1 ELSE 0 END AS Vencido "
          + " FROM usuario_evento "
          + " WHERE IdUsuario = ? "
          + "   AND Estatus = 'ACTIVO' "
          + "   AND Realizado = 0 "
          + "   AND ( "
          + "        (DATE(FechaInicio) <= CURDATE() AND DATE(FechaFin) >= CURDATE()) "  // ocurre hoy
          + "        OR DATE(FechaFin) < CURDATE() "                                      // vencido
          + "   ) "
          + " ORDER BY FechaInicio ASC ";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
                while (rs.next()) {
                    Recordatorio r = new Recordatorio();
                    r.tipo = (rs.getInt("Vencido") == 1)
                            ? Recordatorio.TIPO_EVENTO_VENCIDO
                            : Recordatorio.TIPO_EVENTO_HOY;
                    r.idEvento = rs.getString("IdEvento");
                    r.idSeguimiento = null;
                    r.referencia = "Calendario";
                    r.lugarCliente = rs.getString("Lugar") == null ? "" : rs.getString("Lugar");
                    r.titulo = rs.getString("Titulo") == null ? "" : rs.getString("Titulo");
                    r.detalle = rs.getString("Descripcion") == null ? "" : rs.getString("Descripcion");
                    Timestamp fi = rs.getTimestamp("FechaInicio");
                    r.fecha = (fi == null) ? "" : df.format(fi);
                    lista.add(r);
                }
            }
        } catch (Exception ex) {
            LOG.log(Level.WARNING, "Error al obtener recordatorios de eventos: {0}", ex.getMessage());
        }

        return lista;
    }

    /**
     * Marca un evento como realizado para que deje de aparecer en los avisos.
     */
    public boolean marcarRealizado(Connection cnx, String idEvento) {
        if (cnx == null || idEvento == null) {
            return false;
        }
        String sql = "UPDATE usuario_evento SET Realizado = 1 WHERE IdEvento = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, idEvento);
            return ps.executeUpdate() > 0;
        } catch (Exception ex) {
            LOG.log(Level.WARNING, "Error al marcar evento realizado: {0}", ex.getMessage());
            return false;
        }
    }
}
