package com.simpletecno.sopdi.tesoreria;

import com.simpletecno.sopdi.utilerias.Utileria;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/** Numeracion compartida por pagos corrientes y reemisiones. No reserva al previsualizar. */
public final class NumeracionChequesService {
    private static final ZoneId GUATEMALA = ZoneId.of("America/Guatemala");

    private NumeracionChequesService() { }

    public static java.sql.Date fechaContable(Date fecha) {
        if (fecha == null) throw new IllegalArgumentException("Seleccione una fecha de cheque.");
        LocalDate dia = fecha instanceof java.sql.Date
                ? ((java.sql.Date) fecha).toLocalDate()
                : fecha.toInstant().atZone(GUATEMALA).toLocalDate();
        return java.sql.Date.valueOf(dia);
    }

    public static int tipoPartida(String empresa, String codigo) {
        int posicion = empresa.length() + 8;
        if (codigo == null || !codigo.startsWith(empresa)
                || codigo.length() != posicion + 4
                || !codigo.substring(empresa.length()).matches("[0-9]{12}")) {
            throw new IllegalArgumentException("El codigo de partida original no tiene un formato valido.");
        }
        return Character.digit(codigo.charAt(posicion), 10);
    }

    /** Los folios antiguos pudieron avanzar manualmente sin actualizar la tabla de control. */
    public static String nuevaPartida(Connection cnx, String empresa, Date fecha, int tipo)
            throws SQLException {
        if (tipo < 0 || tipo > 9) throw new IllegalArgumentException("Tipo de partida invalido.");
        java.sql.Date dia = fechaContable(fecha);
        String fecha8 = dia.toLocalDate().format(DateTimeFormatter.BASIC_ISO_DATE);
        String prefijo = empresa + fecha8 + tipo;
        int maximo = 0;
        String sql = "SELECT COALESCE(MAX(CAST(RIGHT(CodigoPartida,3) AS UNSIGNED)),0)"
                + " FROM contabilidad_partida WHERE IdEmpresa=? AND CodigoPartida LIKE ?"
                + " AND CHAR_LENGTH(CodigoPartida)=? AND RIGHT(CodigoPartida,3) REGEXP '^[0-9]{3}$'";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, empresa);
            ps.setString(2, prefijo + "%");
            ps.setInt(3, prefijo.length() + 3);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) maximo = rs.getInt(1);
            }
        }
        try (PreparedStatement ps = cnx.prepareStatement(
                "INSERT INTO folio_codigo_partida (IdEmpresa,Fecha,Tipo,Valor) VALUES (?,?,?,?)"
                + " ON DUPLICATE KEY UPDATE Valor=GREATEST(Valor,VALUES(Valor))")) {
            ps.setString(1, empresa);
            ps.setString(2, fecha8);
            ps.setInt(3, tipo);
            ps.setInt(4, maximo);
            ps.executeUpdate();
        }
        return Utileria.nextCodigoPartida(cnx, empresa, dia, tipo);
    }

    /** Fallar antes de escribir si el motor no permite revertir pagos/chequeras. */
    public static void validarTransacciones(Connection cnx, String... tablas) throws SQLException {
        try (PreparedStatement ps = cnx.prepareStatement(
                "SELECT ENGINE FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME=?")) {
            for (String tabla : tablas) {
                ps.setString(1, tabla);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next() || !"InnoDB".equalsIgnoreCase(rs.getString(1))) {
                        throw new SQLException("La tabla " + tabla
                                + " debe usar InnoDB para guardar cheques de forma transaccional.");
                    }
                }
            }
        }
    }

    /** Una consulta fallida tampoco permite confirmar una partida. */
    public static void validarCuadre(Connection cnx, String empresa, String codigo) throws SQLException {
        try (PreparedStatement ps = cnx.prepareStatement(
                "SELECT COUNT(*),ROUND(SUM(Debe),2),ROUND(SUM(Haber),2),"
                + "ROUND(SUM(DebeQuetzales),2),ROUND(SUM(HaberQuetzales),2)"
                + " FROM contabilidad_partida WHERE IdEmpresa=? AND CodigoPartida=?")) {
            ps.setString(1, empresa);
            ps.setString(2, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                java.math.BigDecimal tolerancia = new java.math.BigDecimal("0.01");
                if (!rs.next() || rs.getLong(1) == 0
                        || rs.getBigDecimal(2) == null || rs.getBigDecimal(3) == null
                        || rs.getBigDecimal(4) == null || rs.getBigDecimal(5) == null
                        || rs.getBigDecimal(2).subtract(rs.getBigDecimal(3)).abs().compareTo(tolerancia) > 0
                        || rs.getBigDecimal(4).subtract(rs.getBigDecimal(5)).abs().compareTo(tolerancia) > 0) {
                    throw new SQLException("La partida " + codigo + " no cuadra o no tiene movimientos.");
                }
            }
        }
    }

    public interface Trabajo<T> {
        T ejecutar() throws Exception;
    }

    /** El llamador mantiene los avisos y cambios de UI fuera de esta transaccion. */
    public static <T> T transaccion(Connection cnx, Trabajo<T> trabajo) throws Exception {
        if (!cnx.getAutoCommit()) throw new SQLException("Ya existe una transaccion en esta conexion.");
        cnx.setAutoCommit(false);
        try {
            T resultado = trabajo.ejecutar();
            cnx.commit();
            return resultado;
        } catch (Exception ex) {
            try { cnx.rollback(); } catch (SQLException rollback) { ex.addSuppressed(rollback); }
            throw ex;
        } finally {
            cnx.setAutoCommit(true);
        }
    }

    public static Lote cargar(Connection cnx, String empresa, String cuenta, boolean bloquear)
            throws SQLException {
        if (bloquear && cnx.getAutoCommit()) {
            throw new SQLException("La chequera debe bloquearse dentro de una transaccion.");
        }
        String sql = "SELECT ch.IdChequera,ch.Del,ch.Al,ch.UltimoUtilizado,b.IdNomenclatura"
                + " FROM contabilidad_cuentas_bancos_chequera ch"
                + " INNER JOIN contabilidad_cuentas_bancos b"
                + " ON b.IdCuentaBanco=ch.IdCuentaBanco AND b.IdEmpresa=ch.IdEmpresa"
                + " WHERE ch.IdEmpresa=? AND ch.IdCuentaBanco=? ORDER BY ch.Del,ch.IdChequera"
                + (bloquear ? " FOR UPDATE" : "");
        List<Chequera> chequeras = new ArrayList<>();
        String nomenclatura = null;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, empresa);
            ps.setString(2, cuenta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (!chequeras.isEmpty() && rs.getLong("Del") <= chequeras.get(chequeras.size() - 1).al) {
                        throw new SQLException("Las chequeras de la cuenta tienen rangos superpuestos.");
                    }
                    nomenclatura = rs.getString("IdNomenclatura");
                    chequeras.add(new Chequera(rs.getLong("IdChequera"), rs.getLong("Del"),
                            rs.getLong("Al"), rs.getLong("UltimoUtilizado")));
                }
            }
        }
        if (chequeras.isEmpty()) throw new SQLException("La cuenta " + cuenta + " no tiene chequera.");
        // No excluir ANULADO: esos numeros conservan su partida y siguen consumidos.
        String usados = "SELECT COALESCE(MAX(CAST(NumeroDocumento AS UNSIGNED)),0)"
                + " FROM contabilidad_partida WHERE IdEmpresa=? AND IdNomenclatura=?"
                + " AND UPPER(TipoDocumento)='CHEQUE' AND NumeroDocumento REGEXP '^[0-9]+$'"
                + " AND CAST(NumeroDocumento AS UNSIGNED) BETWEEN ? AND ?";
        try (PreparedStatement ps = cnx.prepareStatement(usados)) {
            for (Chequera ch : chequeras) {
                ps.setString(1, empresa);
                ps.setString(2, nomenclatura);
                ps.setLong(3, ch.del);
                ps.setLong(4, ch.al);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) ch.ultimo = Math.max(ch.ultimo, rs.getLong(1));
                }
            }
        }
        return new Lote(empresa, cuenta, nomenclatura, chequeras);
    }

    static final class Chequera {
        final long id;
        final long del;
        final long al;
        long ultimo;
        long asignado;

        Chequera(long id, long del, long al, long ultimo) {
            this.id = id;
            this.del = del;
            this.al = al;
            this.ultimo = Math.max(del - 1, ultimo);
        }
    }

    public static final class NumeracionDesactualizadaException extends SQLException {
        NumeracionDesactualizadaException() {
            super("La numeracion cambio. Revise los numeros actualizados y vuelva a generar los pagos.");
        }
    }

    public static final class Lote {
        final String empresa;
        final String cuenta;
        public final String nomenclatura;
        final List<Chequera> chequeras;
        private final Map<String, Long> numeros = new LinkedHashMap<>();

        Lote(String empresa, String cuenta, String nomenclatura, List<Chequera> chequeras) {
            this.empresa = empresa;
            this.cuenta = cuenta;
            this.nomenclatura = nomenclatura;
            this.chequeras = chequeras;
        }

        /** Orden estable: ordenar/filtrar la grilla no cambia la numeracion del grupo. */
        public Map<String, Long> asignar(Collection<String> grupos) throws SQLException {
            for (String grupo : new TreeSet<>(grupos)) {
                if (numeros.containsKey(grupo)) continue;
                Chequera disponible = null;
                for (Chequera ch : chequeras) {
                    if (ch.ultimo < ch.al) { disponible = ch; break; }
                }
                if (disponible == null) throw new SQLException("Sin cheques disponibles en la cuenta " + cuenta + ".");
                long numero = ++disponible.ultimo;
                // Los rangos no deben solaparse, pero nunca asignar dos veces el mismo numero.
                while (numeros.containsValue(numero) && numero < disponible.al) {
                    numero = ++disponible.ultimo;
                }
                if (numeros.containsValue(numero)) throw new SQLException("Las chequeras tienen rangos superpuestos.");
                disponible.asignado = numero;
                numeros.put(grupo, numero);
            }
            return Collections.unmodifiableMap(numeros);
        }

        public void validarPropuesta(Map<String, Long> propuesta) throws SQLException {
            if (!numeros.equals(propuesta)) throw new NumeracionDesactualizadaException();
        }

        /** Solo avanzar el control cuando cada numero tiene exactamente una partida. */
        public void guardar(Connection cnx) throws SQLException {
            if (cnx.getAutoCommit()) throw new SQLException("El control de cheques debe guardarse dentro de una transaccion.");
            String sql = "SELECT COUNT(DISTINCT CodigoPartida) FROM contabilidad_partida"
                    + " WHERE IdEmpresa=? AND IdNomenclatura=? AND UPPER(TipoDocumento)='CHEQUE'"
                    + " AND NumeroDocumento REGEXP '^[0-9]+$' AND CAST(NumeroDocumento AS UNSIGNED)=?";
            try (PreparedStatement ps = cnx.prepareStatement(sql)) {
                for (Long numero : numeros.values()) {
                    ps.setString(1, empresa);
                    ps.setString(2, nomenclatura);
                    ps.setLong(3, numero);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next() || rs.getInt(1) != 1) {
                            throw new SQLException("El cheque " + numero
                                    + " debe tener una sola partida contable en esta cuenta.");
                        }
                    }
                }
            }
            try (PreparedStatement ps = cnx.prepareStatement(
                    "UPDATE contabilidad_cuentas_bancos_chequera"
                    + " SET UltimoUtilizado=GREATEST(COALESCE(UltimoUtilizado,Del-1),?)"
                    + " WHERE IdEmpresa=? AND IdCuentaBanco=? AND IdChequera=?")) {
                for (Chequera ch : chequeras) {
                    if (ch.asignado == 0) continue;
                    ps.setLong(1, ch.asignado);
                    ps.setString(2, empresa);
                    ps.setString(3, cuenta);
                    ps.setLong(4, ch.id);
                    if (ps.executeUpdate() != 1) throw new SQLException("No se pudo actualizar la chequera " + ch.id);
                }
            }
        }
    }
}
