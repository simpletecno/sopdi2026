package com.simpletecno.sopdi.contabilidad;

import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.ui.Notification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servicios de validación y consulta sobre partidas contables.
 */
public class PartidaContableService {

    private static final Logger log = Logger.getLogger(PartidaContableService.class.getName());

    private static final double TOLERANCIA = 0.01; // diferencia máxima aceptable por redondeo

    /**
     * Verifica si una partida contable está cuadrada:
     *   SUM(Debe)          == SUM(Haber)
     *   SUM(DebeQuetzales) == SUM(HaberQuetzales)
     *
     * Si hay descuadre muestra una Notification de ERROR sin auto-cierre para
     * que el usuario la vea, y registra el detalle en el log.
     *
     * @param codigoPartida código de la partida a verificar
     * @param conn          conexión JDBC activa (puede estar en autoCommit=true)
     * @param empresaId     ID de la empresa
     * @return true si cuadrada (o si no se encontraron filas), false si descuadrada
     */
    public static boolean EsPartidaCuadrada(String codigoPartida, Connection conn, String empresaId) {

        String sql = "SELECT"
                + "  ROUND(SUM(Debe),          2) AS TotalDebe,"
                + "  ROUND(SUM(Haber),         2) AS TotalHaber,"
                + "  ROUND(SUM(DebeQuetzales), 2) AS TotalDebeQ,"
                + "  ROUND(SUM(HaberQuetzales),2) AS TotalHaberQ"
                + " FROM contabilidad_partida"
                + " WHERE CodigoPartida = ? AND IdEmpresa = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codigoPartida);
            ps.setString(2, empresaId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return true; // sin filas → nada que verificar

                double debe   = rs.getDouble("TotalDebe");
                double haber  = rs.getDouble("TotalHaber");
                double debeQ  = rs.getDouble("TotalDebeQ");
                double haberQ = rs.getDouble("TotalHaberQ");

                double diffMoneda    = Math.abs(debe - haber);
                double diffQuetzales = Math.abs(debeQ - haberQ);

                boolean cuadradaMoneda    = diffMoneda    <= TOLERANCIA;
                boolean cuadradaQuetzales = diffQuetzales <= TOLERANCIA;

                if (cuadradaMoneda && cuadradaQuetzales) return true;

                // ── Construir mensaje de descuadre ───────────────────────────
                StringBuilder msg = new StringBuilder();
                msg.append("PARTIDA DESCUADRADA [").append(codigoPartida).append("]");

                if (!cuadradaMoneda) {
                    msg.append("\n  Debe=").append(String.format("%.2f", debe))
                       .append("  Haber=").append(String.format("%.2f", haber))
                       .append("  Diferencia=").append(String.format("%.2f", debe - haber));
                }
                if (!cuadradaQuetzales) {
                    msg.append("\n  DebeQ=").append(String.format("%.2f", debeQ))
                       .append("  HaberQ=").append(String.format("%.2f", haberQ))
                       .append("  DiferenciaQ=").append(String.format("%.2f", debeQ - haberQ));
                }
                msg.append("\nPor favor notifique al área contable.");

                log.log(Level.WARNING, msg.toString());

                Notification notif = new Notification(msg.toString(), Notification.Type.ERROR_MESSAGE);
                notif.setDelayMsec(-1); // sin auto-cierre: el usuario debe verla
                notif.setPosition(Position.MIDDLE_CENTER);
                notif.show(Page.getCurrent());

                return false;
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error al verificar cuadre de partida " + codigoPartida, ex);
            return true; // si no se puede verificar no bloqueamos el flujo
        }
    }
}
