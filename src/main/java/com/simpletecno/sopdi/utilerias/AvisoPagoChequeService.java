package com.simpletecno.sopdi.utilerias;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gestiona los avisos de pago/cheque para el usuario impresor de cheques.
 *
 * Tabla: aviso_pago_cheque
 *   IdAviso       INT AUTO_INCREMENT PK
 *   IdEmpresa     INT NOT NULL
 *   IdUsuario     INT NOT NULL       -- destinatario (Isabel Garcia = 15)
 *   Mensaje       VARCHAR(500)
 *   FechaYHora    DATETIME DEFAULT CURRENT_TIMESTAMP
 *   Atendido      TINYINT(1) DEFAULT 0
 *   UsuarioOrigen VARCHAR(50)        -- usuario que autorizó el pago
 */
public class AvisoPagoChequeService {

    /** IdUsuario de Isabel Garcia, responsable de imprimir los cheques. */
    public static final int ID_USUARIO_IMPRESORA = 15;

    private static final Logger LOG = Logger.getLogger(AvisoPagoChequeService.class.getName());
    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    // ── Crear tabla si no existe ──────────────────────────────────────────────

    public static void autoCrearTabla(Connection cnx) {
        try (Statement st = cnx.createStatement()) {
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS aviso_pago_cheque ("
                + " IdAviso       INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,"
                + " IdEmpresa     INT          NOT NULL,"
                + " IdUsuario     INT          NOT NULL,"
                + " Mensaje       VARCHAR(500) NOT NULL,"
                + " FechaYHora   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + " Atendido      TINYINT(1)  NOT NULL DEFAULT 0,"
                + " UsuarioOrigen VARCHAR(50)  NULL,"
                + " PdfBytes      MEDIUMBLOB   NULL"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        } catch (Exception ex) {
            LOG.log(Level.WARNING, "No se pudo crear aviso_pago_cheque: {0}", ex.getMessage());
        }
        // Agregar columna PdfBytes si la tabla ya existía sin ella
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT COUNT(*) FROM information_schema.COLUMNS"
                     + " WHERE TABLE_SCHEMA = DATABASE()"
                     + " AND TABLE_NAME = 'aviso_pago_cheque'"
                     + " AND COLUMN_NAME = 'PdfBytes'")) {
            if (rs.next() && rs.getInt(1) == 0) {
                cnx.createStatement().executeUpdate(
                        "ALTER TABLE aviso_pago_cheque ADD COLUMN PdfBytes MEDIUMBLOB NULL");
            }
        } catch (Exception ex) {
            LOG.log(Level.WARNING, "No se pudo agregar columna PdfBytes: {0}", ex.getMessage());
        }
    }

    // ── Insertar aviso ────────────────────────────────────────────────────────

    public static void crearAviso(Connection cnx, String idEmpresa,
                                   int idUsuario, String mensaje, String usuarioOrigen) {
        crearAviso(cnx, idEmpresa, idUsuario, mensaje, usuarioOrigen, null);
    }

    public static void crearAviso(Connection cnx, String idEmpresa,
                                   int idUsuario, String mensaje, String usuarioOrigen,
                                   byte[] pdfBytes) {
        autoCrearTabla(cnx);
        String sql = "INSERT INTO aviso_pago_cheque (IdEmpresa, IdUsuario, Mensaje, UsuarioOrigen, PdfBytes)"
                   + " VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, idEmpresa);
            ps.setInt(2, idUsuario);
            ps.setString(3, mensaje);
            ps.setString(4, usuarioOrigen);
            if (pdfBytes != null && pdfBytes.length > 0) {
                ps.setBytes(5, pdfBytes);
            } else {
                ps.setNull(5, java.sql.Types.BLOB);
            }
            ps.executeUpdate();
        } catch (Exception ex) {
            LOG.log(Level.WARNING, "Error al crear aviso de pago: {0}", ex.getMessage());
        }
    }

    /** Retorna los bytes del PDF guardado para un aviso, o null si no existe. */
    public static byte[] getPdfBytes(Connection cnx, String idAviso) {
        if (cnx == null || idAviso == null || idAviso.isEmpty()) return null;
        try (PreparedStatement ps = cnx.prepareStatement(
                "SELECT PdfBytes FROM aviso_pago_cheque WHERE IdAviso = ?")) {
            ps.setString(1, idAviso);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBytes("PdfBytes");
            }
        } catch (Exception ex) {
            LOG.log(Level.WARNING, "Error al obtener PDF del aviso: {0}", ex.getMessage());
        }
        return null;
    }

    // ── Obtener pendientes (para la campana) ──────────────────────────────────

    public static List<Recordatorio> obtenerPendientes(Connection cnx,
                                                        String idUsuario, String idEmpresa) {
        List<Recordatorio> lista = new ArrayList<>();
        if (cnx == null || idUsuario == null) return lista;

        autoCrearTabla(cnx);
        String sql = "SELECT IdAviso, Mensaje, FechaYHora, UsuarioOrigen"
                   + " FROM aviso_pago_cheque"
                   + " WHERE IdUsuario = ? AND IdEmpresa = ? AND Atendido = 0"
                   + " ORDER BY FechaYHora DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, idUsuario);
            ps.setString(2, idEmpresa);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Recordatorio r = new Recordatorio();
                    r.tipo        = Recordatorio.TIPO_AVISO_PAGO;
                    r.idAviso     = rs.getString("IdAviso");
                    r.referencia  = idEmpresa;
                    r.lugarCliente = nvl(rs.getString("UsuarioOrigen"));
                    r.titulo      = "Cheques por imprimir";
                    r.detalle     = nvl(rs.getString("Mensaje"));
                    java.sql.Timestamp ts = rs.getTimestamp("FechaYHora");
                    r.fecha       = ts == null ? "" : SDF.format(ts);
                    r.idSeguimiento = null;
                    r.idEvento      = null;
                    lista.add(r);
                }
            }
        } catch (Exception ex) {
            LOG.log(Level.WARNING, "Error al obtener avisos de pago: {0}", ex.getMessage());
        }
        return lista;
    }

    // ── Marcar atendido ───────────────────────────────────────────────────────

    public static boolean marcarAtendido(Connection cnx, String idAviso) {
        if (cnx == null || idAviso == null || idAviso.isEmpty()) return false;
        try (PreparedStatement ps = cnx.prepareStatement(
                "UPDATE aviso_pago_cheque SET Atendido = 1 WHERE IdAviso = ?")) {
            ps.setString(1, idAviso);
            return ps.executeUpdate() > 0;
        } catch (Exception ex) {
            LOG.log(Level.WARNING, "Error al marcar aviso atendido: {0}", ex.getMessage());
            return false;
        }
    }

    private static String nvl(String s) { return s == null ? "" : s; }
}
