package com.simpletecno.sopdi.tesoreria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;

/** Traslado de cheque: la anulacion y su reemplazo se confirman juntos. */
public final class ReemisionChequeService {
    private ReemisionChequeService() { }

    public static final class Resultado {
        public final String codigoPartida;
        public final long numeroCheque;
        Resultado(String codigoPartida, long numeroCheque) {
            this.codigoPartida = codigoPartida;
            this.numeroCheque = numeroCheque;
        }
    }

    public static Resultado trasladar(Connection cnx, String empresa, String nomenclatura,
            String original, Date fecha, String usuario) throws Exception {
        java.sql.Date dia = NumeracionChequesService.fechaContable(fecha);
        int tipo = NumeracionChequesService.tipoPartida(empresa, original);
        NumeracionChequesService.validarTransacciones(cnx,
                "contabilidad_partida", "contabilidad_cuentas_bancos_chequera");
        return NumeracionChequesService.transaccion(cnx, () -> {
            String cuenta = null;
            try (PreparedStatement ps = cnx.prepareStatement(
                    "SELECT IdCuentaBanco FROM contabilidad_cuentas_bancos"
                    + " WHERE IdEmpresa=? AND IdNomenclatura=?")) {
                ps.setString(1, empresa);
                ps.setString(2, nomenclatura);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) cuenta = rs.getString(1);
                    if (cuenta == null || rs.next()) {
                        throw new SQLException("No se puede identificar una sola cuenta bancaria para este cheque.");
                    }
                }
            }
            NumeracionChequesService.Lote lote = NumeracionChequesService.cargar(cnx, empresa, cuenta, true);
            String numeroOriginal = null;
            try (PreparedStatement ps = cnx.prepareStatement(
                    "SELECT TipoDocumento,Estatus,NumeroDocumento FROM contabilidad_partida"
                    + " WHERE IdEmpresa=? AND CodigoPartida=? FOR UPDATE")) {
                ps.setString(1, empresa);
                ps.setString(2, original);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        if (!"CHEQUE".equalsIgnoreCase(rs.getString(1))
                                || "ANULADO".equalsIgnoreCase(rs.getString(2))) {
                            throw new SQLException("La partida original no es un cheque vigente.");
                        }
                        String numero = rs.getString(3);
                        if (numeroOriginal != null && !numeroOriginal.equals(numero)) {
                            throw new SQLException("La partida original contiene distintos numeros de cheque.");
                        }
                        numeroOriginal = numero;
                    }
                }
            }
            if (numeroOriginal == null) throw new SQLException("No existe la partida original en esta empresa.");
            long nuevoNumero = lote.asignar(Arrays.asList("REEMISION")).get("REEMISION");
            String codigo = NumeracionChequesService.nuevaPartida(cnx, empresa, dia, tipo);

            String columnas = "IdEmpresa,Estatus,CodigoPartida,CodigoCC,Fecha,Descripcion,"
                    + "IdProveedor,NITProveedor,NombreProveedor,NombreCheque,NumeroDocumento,"
                    + "SerieDocumento,TipoDocumento,IdNomenclatura,MonedaDocumento,MontoDocumento,"
                    + "Debe,Haber,TipoCambio,DebeQuetzales,HaberQuetzales,Saldo,"
                    + "MontoAutorizadoPagar,MontoAplicarAnticipo,IdLiquidacion,IdLiquidador,"
                    + "TipoEnganche,TipoVenta,TipoDOCA,NoDOCA,Referencia,"
                    + "CreadoFechaYHora,CreadoUsuario,Año,PagadoIva,IdOrdenCompra,"
                    + "IdConciliacion,IdCentroCosto,CodigoCentroCosto,IdProducto,EsCuota,"
                    + "SueldoOrdinario,FechaYHoraCreado,CreadoPor";
            String seleccion = "IdEmpresa,'INGRESADO',?,"
                    // Conservar CC de anticipos/documentos: otras partidas pueden haberlos aplicado.
                    + "CASE WHEN IdNomenclatura=? AND CodigoCC=? THEN ? ELSE CodigoCC END,?,Descripcion,"
                    + "IdProveedor,NITProveedor,NombreProveedor,NombreCheque,?,"
                    + "SerieDocumento,TipoDocumento,IdNomenclatura,MonedaDocumento,MontoDocumento,"
                    + "Debe,Haber,TipoCambio,DebeQuetzales,HaberQuetzales,Saldo,"
                    + "MontoAutorizadoPagar,MontoAplicarAnticipo,IdLiquidacion,IdLiquidador,"
                    + "TipoEnganche,TipoVenta,TipoDOCA,"
                    + "CASE WHEN UPPER(TipoDOCA)='CHEQUE' AND NoDOCA=? THEN ? ELSE NoDOCA END,Referencia,"
                    + "CURRENT_TIMESTAMP,?,YEAR(?),PagadoIva,IdOrdenCompra,"
                    + "IdConciliacion,IdCentroCosto,CodigoCentroCosto,IdProducto,EsCuota,"
                    + "SueldoOrdinario,CURRENT_TIMESTAMP,CreadoPor";
            try (PreparedStatement ps = cnx.prepareStatement(
                    "INSERT INTO contabilidad_partida (" + columnas + ") SELECT " + seleccion
                    + " FROM contabilidad_partida WHERE IdEmpresa=? AND CodigoPartida=?")) {
                ps.setString(1, codigo);
                ps.setString(2, nomenclatura);
                ps.setString(3, original);
                ps.setString(4, codigo);
                ps.setDate(5, dia);
                ps.setString(6, String.valueOf(nuevoNumero));
                ps.setString(7, numeroOriginal);
                ps.setString(8, String.valueOf(nuevoNumero));
                ps.setString(9, usuario);
                ps.setDate(10, dia);
                ps.setString(11, empresa);
                ps.setString(12, original);
                if (ps.executeUpdate() == 0) throw new SQLException("No se pudo crear el cheque de reemplazo.");
            }
            NumeracionChequesService.validarCuadre(cnx, empresa, codigo);
            try (PreparedStatement ps = cnx.prepareStatement(
                    "UPDATE contabilidad_partida SET Estatus='ANULADO'"
                    + " WHERE IdEmpresa=? AND CodigoPartida=? AND Estatus <> 'ANULADO'")) {
                ps.setString(1, empresa);
                ps.setString(2, original);
                if (ps.executeUpdate() == 0) throw new SQLException("El cheque original ya fue anulado.");
            }
            lote.guardar(cnx);
            return new Resultado(codigo, nuevoNumero);
        });
    }
}
