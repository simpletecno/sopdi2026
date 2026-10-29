package com.simpletecno.sopdi.compras;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Savepoint;

public final class OrdenCompraDetallePlu {

    private OrdenCompraDetallePlu() {
    }

    private static String relacionDetalle(String historico, String detalle) {
        StringBuilder relacion = new StringBuilder(historico + ".IdOrdenCompra = " + detalle + ".IdOrdenCompra");
        for (String campo : new String[]{"IdProject", "IDCC", "IDEX", "NoCuenta", "IdArea"}) {
            relacion.append(" AND (").append(historico).append('.').append(campo)
                    .append(" = ").append(detalle).append('.').append(campo)
                    .append(" OR (").append(historico).append('.').append(campo)
                    .append(" IS NULL AND ").append(detalle).append('.').append(campo).append(" IS NULL))");
        }
        return relacion.toString();
    }

    public static void sincronizar(Connection connection, String idOrdenCompra, String idUsuario) throws SQLException {
        String eliminar = "DELETE FROM orden_compra_detalle_plu WHERE IdOrdenCompra = ?"
                + " AND NOT EXISTS (SELECT 1 FROM orden_compra_detalle detalle"
                + " INNER JOIN orden_compra orden ON orden.Id = detalle.IdOrdenCompra AND orden.IdTipoOrdenCompra = 2"
                + " WHERE " + relacionDetalle("orden_compra_detalle_plu", "detalle") + ")";
        try (PreparedStatement statement = connection.prepareStatement(eliminar)) {
            statement.setString(1, idOrdenCompra);
            statement.executeUpdate();
        }

        String insertar = "INSERT INTO orden_compra_detalle_plu"
                + " (IdOrdenCompra, IDCC, IDEX, NoCuenta, Descripcion, PluPrv, PluPrvDes,"
                + " IdProject, IdArea, Cantidad, Precio, Total, CreadoFechaYHora, CreadoUsuario)"
                + " SELECT detalle.IdOrdenCompra, detalle.IDCC, detalle.IDEX, detalle.NoCuenta, detalle.Descripcion,"
                + " catalogo.PLU, catalogo.DescripcionProveedor, detalle.IdProject, detalle.IdArea,"
                + " COALESCE(catalogo.Cantidad, 0), COALESCE(catalogo.Precio, 0),"
                + " ROUND(COALESCE(catalogo.Cantidad, 0) * COALESCE(catalogo.Precio, 0), 2), CURRENT_TIMESTAMP, ?"
                + " FROM orden_compra_detalle detalle"
                + " INNER JOIN orden_compra orden ON orden.Id = detalle.IdOrdenCompra AND orden.IdTipoOrdenCompra = 2"
                + " INNER JOIN centro_costo_cuenta cuenta ON cuenta.CodigoCuentaCentroCosto = detalle.NoCuenta"
                + " INNER JOIN proveedor_plu catalogo ON catalogo.IdCuentaCentroCosto = cuenta.IdCuentaCentroCosto"
                + " AND catalogo.IdProveedor = orden.IdProveedor"
                + " LEFT JOIN orden_compra_detalle_plu historico ON " + relacionDetalle("historico", "detalle")
                + " WHERE detalle.IdOrdenCompra = ? AND historico.Id IS NULL";
        try (PreparedStatement statement = connection.prepareStatement(insertar)) {
            statement.setString(1, idUsuario);
            statement.setString(2, idOrdenCompra);
            statement.executeUpdate();
        }
    }

    public static void eliminarOrdenCompra(Connection connection, String idOrdenCompra) throws SQLException {
        boolean autoCommitOriginal = connection.getAutoCommit();
        Savepoint savepoint = null;
        if (autoCommitOriginal) {
            connection.setAutoCommit(false);
        } else {
            savepoint = connection.setSavepoint();
        }
        try {
            for (String tabla : new String[]{"orden_compra_detalle_plu", "orden_compra_detalle", "orden_compra"}) {
                String campo = tabla.equals("orden_compra") ? "Id" : "IdOrdenCompra";
                try (PreparedStatement statement = connection.prepareStatement("DELETE FROM " + tabla + " WHERE " + campo + " = ?")) {
                    statement.setString(1, idOrdenCompra);
                    statement.executeUpdate();
                }
            }
            if (autoCommitOriginal) {
                connection.commit();
            } else {
                connection.releaseSavepoint(savepoint);
            }
        } catch (SQLException exception) {
            try {
                if (autoCommitOriginal) {
                    connection.rollback();
                } else {
                    connection.rollback(savepoint);
                }
            } catch (SQLException rollbackException) {
                exception.addSuppressed(rollbackException);
            }
            throw exception;
        } finally {
            if (autoCommitOriginal) {
                connection.setAutoCommit(true);
            }
        }
    }
}
