package com.simpletecno.sopdi.compras;

import com.simpletecno.sopdi.utilerias.MyEmailMessanger;
import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.simpletecno.sopdi.utilerias.ValidarTokenForm;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.SelectionEvent;
import com.vaadin.event.SelectionEvent.SelectionListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.renderers.ClickableRenderer.RendererClickEvent;
import com.vaadin.ui.themes.ValoTheme;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.ui.NumberField;

import javax.mail.MessagingException;

/**
 *
 * @author user
 */
public class IngresoLiquidacionGastoForm extends Window {

    VerticalLayout mainLayout;
    VerticalLayout reportLayoutFactura = new VerticalLayout();

    ComboBox porContabilizarCbx = new ComboBox("Por contabilizar");

    ComboBox centroCostoCbx;
    ComboBox tipoDocumentoCbx;
    TextField serieTxt;
    TextField numeroTxt;
    TextField nitProveedorTxt;
    TextField idLiquidacionTxt;

    ComboBox liquidadorCbx;
    ComboBox proveedorCbx;
    ComboBox monedaCbx;
    ComboBox cuentaContable1Cbx;
    ComboBox cuentaContable2Cbx;
    ComboBox cuentaContable3Cbx;
    ComboBox cuentaContable4Cbx;

    DateField fechaDt;
    Label haberLbl = new Label();

    NumberField montoTxt;
    NumberField tasaCambioTxt;
    NumberField debe1Txt;
    NumberField debe2Txt;
    NumberField debe3Txt;
    NumberField debe4Txt;

    Button grabarBtn;
    Button cerrarLiquidacionBtn;

    ComboBox liquidacionesCbx;
    private Liquidacion liquidacionSeleccionada;
    private boolean nuevaLiquidacion;
    private boolean seleccionManual;
    private boolean actualizandoSeleccion;

    Grid facturaGastoGrid;
    public IndexedContainer container = new IndexedContainer();
    static final String ID_PROPERTY = "Id";
    static final String SERIE_PROPERTY = "SERIE";
    static final String CODIGO_PARTIDA_PROPERTY = "C.Partida";
    static final String NUMERO_PROPERTY = "Número";
    static final String NIT_PROPERTY = "Nit";
    static final String PROVEEDOR_PROPERTY = "Proveedor";
    static final String FECHA_PROPERTY = "Fecha";
    static final String MONTO_PROPERTY = "Monto";
    static final String MONEDA_PROPERTY = "Moneda";
    static final String TIPOCAMBIO_PROPERTY = "Tipo cambio";
    static final String ELIMINAR_PROPERTY = "Eliminar";
    Grid.FooterRow footer;

    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");

    MarginInfo marginInfo;
    UI mainUI;

    String variableTemp = "";

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();


    static final class Liquidacion {
        final long idLiquidacion;
        final String idLiquidador;
        final String idEmpresa;
        final String codigoCC;

        Liquidacion(long idLiquidacion, String idLiquidador, String idEmpresa, String codigoCC) {
            this.idLiquidacion = idLiquidacion;
            this.idLiquidador = idLiquidador;
            this.idEmpresa = idEmpresa;
            this.codigoCC = codigoCC;
        }
    }

    static final class Renglon {
        final String cuenta;
        final BigDecimal debe;
        final BigDecimal haber;
        final BigDecimal debeQuetzales;
        final BigDecimal haberQuetzales;

        Renglon(String cuenta, BigDecimal debe, BigDecimal haber, BigDecimal tipoCambio) {
            this(cuenta, debe, haber, centavos(debe).multiply(tipoCambio), centavos(haber).multiply(tipoCambio));
        }

        private Renglon(String cuenta, BigDecimal debe, BigDecimal haber,
                BigDecimal debeQuetzales, BigDecimal haberQuetzales) {
            this.cuenta = cuenta;
            this.debe = centavos(debe);
            this.haber = centavos(haber);
            this.debeQuetzales = centavos(debeQuetzales);
            this.haberQuetzales = centavos(haberQuetzales);
        }

        boolean esAjusteRedondeo() {
            return debe.signum() == 0 && haber.signum() == 0
                    && (debeQuetzales.signum() != 0 || haberQuetzales.signum() != 0);
        }
    }

    static final class Factura {
        String idEmpresa;
        String idLiquidador;
        String nombreLiquidador;
        String cuentaLiquidaciones;
        String tipoDocumento;
        java.sql.Date fecha;
        String nitProveedor;
        String idProveedor;
        String nombreProveedor;
        String serie;
        String numero;
        String moneda;
        BigDecimal tipoCambio;
        String idCentroCosto;
        String codigoCentroCosto;
        String idUsuario;
        String nombreUsuario;
        String token;
        String idFel;
        List<Renglon> renglones;
    }

    interface Operacion<T> {
        T ejecutar() throws SQLException;
    }

    private Connection conexion() {
        return ((SopdiUI) mainUI).databaseProvider.getCurrentConnection();
    }

    static PreparedStatement preparar(Connection connection, String sql, Object... valores) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        try {
            for (int indice = 0; indice < valores.length; indice++) {
                if (valores[indice] instanceof BigDecimal) {
                    statement.setBigDecimal(indice + 1, (BigDecimal) valores[indice]);
                } else {
                    statement.setObject(indice + 1, valores[indice]);
                }
            }
            return statement;
        } catch (SQLException | RuntimeException exception) {
            statement.close();
            throw exception;
        }
    }

    static <T> T enTransaccion(Connection connection, Operacion<T> operacion) throws SQLException {
        boolean autoCommitOriginal = connection.getAutoCommit();
        if (!autoCommitOriginal) {
            throw new SQLException("La conexión ya tiene una transacción activa. Intente nuevamente.");
        }
        boolean confirmada = false;
        Exception fallo = null;
        try {
            connection.setAutoCommit(false);
            T resultado = operacion.ejecutar();
            connection.commit();
            confirmada = true;
            return resultado;
        } catch (SQLException | RuntimeException exception) {
            fallo = exception;
            try {
                connection.rollback();
            } catch (SQLException rollbackException) {
                exception.addSuppressed(rollbackException);
                try {
                    connection.close();
                } catch (SQLException closeException) {
                    exception.addSuppressed(closeException);
                }
            }
            throw exception;
        } finally {
            try {
                if (!connection.isClosed()) {
                    connection.setAutoCommit(autoCommitOriginal);
                }
            } catch (SQLException restoreException) {
                if (fallo != null) {
                    fallo.addSuppressed(restoreException);
                } else if (confirmada) {
                    Logger.getLogger(IngresoLiquidacionGastoForm.class.getName()).log(
                            Level.SEVERE, "Transacción confirmada; error al restaurar autocommit", restoreException);
                } else {
                    throw restoreException;
                }
            }
        }
    }

    static BigDecimal centavos(BigDecimal importe) {
        return importe.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal importe(NumberField campo) {
        try {
            return new BigDecimal(campo.getValue().replace(",", "").trim());
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Ingrese un importe numérico válido.");
        }
    }

    static long numeroLiquidacion(String numero) {
        try {
            long valor = Long.parseLong(numero.trim());
            if (valor > 0) {
                return valor;
            }
        } catch (RuntimeException exception) {
        }
        throw new IllegalArgumentException("Seleccione una liquidación o ingrese un número válido.");
    }

    static List<Renglon> crearRenglones(BigDecimal monto, BigDecimal tipoCambio,
            String cuentaHaber, String[] cuentas, BigDecimal[] importes, String cuentaRedondeo) {
        if (monto.signum() <= 0 || centavos(monto).signum() <= 0 || tipoCambio.signum() <= 0) {
            throw new IllegalArgumentException("El monto y la tasa de cambio deben ser mayores que cero.");
        }
        numeroLiquidacion(cuentaHaber);
        List<Renglon> renglones = new ArrayList<>();
        renglones.add(new Renglon(cuentaHaber, BigDecimal.ZERO, monto, tipoCambio));
        Set<String> cuentasUsadas = new HashSet<>();
        for (int indice = 0; indice < importes.length; indice++) {
            if (importes[indice].signum() < 0) {
                throw new IllegalArgumentException("Los montos del debe no pueden ser negativos.");
            }
            if (importes[indice].signum() != 0) {
                if (cuentas[indice] == null || cuentas[indice].trim().isEmpty()) {
                    throw new IllegalArgumentException("Todo monto distinto de cero debe tener una cuenta contable.");
                }
                numeroLiquidacion(cuentas[indice]);
                if (!cuentasUsadas.add(cuentas[indice])) {
                    throw new IllegalArgumentException("No puede utilizar la misma cuenta para dos registros del debe.");
                }
                if (centavos(importes[indice]).signum() != 0) {
                    renglones.add(new Renglon(cuentas[indice], importes[indice], BigDecimal.ZERO, tipoCambio));
                }
            }
        }
        BigDecimal diferenciaDocumento = BigDecimal.ZERO;
        BigDecimal diferenciaQuetzales = BigDecimal.ZERO;
        for (Renglon renglon : renglones) {
            diferenciaDocumento = diferenciaDocumento.add(renglon.debe).subtract(renglon.haber);
            diferenciaQuetzales = diferenciaQuetzales.add(renglon.debeQuetzales).subtract(renglon.haberQuetzales);
        }
        if (diferenciaDocumento.signum() != 0) {
            throw new IllegalArgumentException("La partida no cuadra en la moneda del documento. Diferencia: "
                    + centavos(diferenciaDocumento) + ". Revise los montos.");
        }
        if (diferenciaQuetzales.signum() != 0) {
            long idCuentaRedondeo;
            try {
                idCuentaRedondeo = numeroLiquidacion(cuentaRedondeo);
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException("Configure una cuenta de Redondeo válida para registrar la diferencia en quetzales.");
            }
            if (idCuentaRedondeo == numeroLiquidacion(cuentaHaber)) {
                throw new IllegalArgumentException("La cuenta de Redondeo debe ser distinta de la cuenta de liquidaciones.");
            }
            // Los renglones anteriores se convirtieron con la misma tasa; solo se compensa su redondeo.
            renglones.add(new Renglon(cuentaRedondeo, BigDecimal.ZERO, BigDecimal.ZERO,
                    diferenciaQuetzales.signum() < 0 ? diferenciaQuetzales.negate() : BigDecimal.ZERO,
                    diferenciaQuetzales.signum() > 0 ? diferenciaQuetzales : BigDecimal.ZERO));
        }
        validarCuadre(renglones);
        return renglones;
    }

    static void validarCuadre(List<Renglon> renglones) {
        BigDecimal diferencia = BigDecimal.ZERO;
        BigDecimal diferenciaQuetzales = BigDecimal.ZERO;
        for (Renglon renglon : renglones) {
            diferencia = diferencia.add(renglon.debe).subtract(renglon.haber);
            diferenciaQuetzales = diferenciaQuetzales.add(renglon.debeQuetzales).subtract(renglon.haberQuetzales);
        }
        if (renglones.size() < 2 || diferencia.signum() != 0 || diferenciaQuetzales.signum() != 0) {
            throw new IllegalArgumentException("La partida no cuadra. Diferencia en moneda del documento: "
                    + centavos(diferencia) + "; diferencia en quetzales: " + centavos(diferenciaQuetzales)
                    + ". Revise los montos y la tasa de cambio.");
        }
    }

    static long siguienteLiquidacion(Connection connection, String idEmpresa, boolean bloquear) throws SQLException {
        String sql = "SELECT IdUltimaLiquidacion FROM contabilidad_empresa WHERE IdEmpresa = ?"
                + (bloquear ? " FOR UPDATE" : "");
        try (PreparedStatement statement = preparar(connection, sql, idEmpresa);
                ResultSet registros = statement.executeQuery()) {
            if (!registros.next()) {
                throw new IllegalArgumentException("No se encontró la empresa de la liquidación.");
            }
            long ultimo = registros.getLong("IdUltimaLiquidacion");
            if (ultimo < 0 || ultimo == Long.MAX_VALUE) {
                throw new IllegalArgumentException("El correlativo de liquidaciones de la empresa no es válido.");
            }
            return ultimo + 1;
        }
    }

    static Set<Long> liquidacionesAbiertas(Connection connection, String idEmpresa,
            String idLiquidador, boolean bloquear) throws SQLException {
        String sql = "SELECT IdLiquidacion FROM contabilidad_partida WHERE IdEmpresa = ?"
                + " AND IdLiquidador = ? AND IdLiquidacion > 0 AND Estatus IN ('INGRESADO', 'REVISADO')"
                + (bloquear ? " FOR UPDATE" : "");
        Set<Long> numeros = new java.util.TreeSet<>();
        try (PreparedStatement statement = preparar(connection, sql, idEmpresa, idLiquidador);
                ResultSet registros = statement.executeQuery()) {
            while (registros.next()) {
                numeros.add(registros.getLong("IdLiquidacion"));
            }
        }
        return numeros;
    }

    static Liquidacion buscarLiquidacion(Connection connection, String idEmpresa, String idLiquidador,
            long numero, String cuentaLiquidaciones, boolean bloquear) throws SQLException {
        String sql = "SELECT IdEmpresa, IdLiquidador, IdNomenclatura, Estatus, CodigoCC"
                + " FROM contabilidad_partida WHERE IdEmpresa = ? AND IdLiquidacion = ? ORDER BY IdPartida"
                + (bloquear ? " FOR UPDATE" : "");
        String codigoCC = null;
        boolean encontrada = false;
        try (PreparedStatement statement = preparar(connection, sql, idEmpresa, numero);
                ResultSet registros = statement.executeQuery()) {
            while (registros.next()) {
                encontrada = true;
                if (registros.getLong("IdLiquidador") != numeroLiquidacion(idLiquidador)
                        || registros.getLong("IdEmpresa") != numeroLiquidacion(idEmpresa)) {
                    throw new IllegalArgumentException("La liquidación no pertenece al liquidador y empresa seleccionados.");
                }
                String estatus = registros.getString("Estatus");
                if (!"INGRESADO".equals(estatus) && !"REVISADO".equals(estatus)) {
                    throw new IllegalArgumentException("La liquidación está cerrada, pagada o no está abierta.");
                }
                if (registros.getLong("IdNomenclatura") == numeroLiquidacion(cuentaLiquidaciones)) {
                    String codigo = registros.getString("CodigoCC");
                    if (codigo == null || codigo.trim().isEmpty() || "null".equalsIgnoreCase(codigo.trim())
                            || (codigoCC != null && !codigoCC.equals(codigo))) {
                        throw new IllegalArgumentException("La liquidación no tiene un CodigoCC válido y único.");
                    }
                    codigoCC = codigo;
                }
            }
        }
        if (!encontrada) {
            throw new IllegalArgumentException("El número de liquidación no existe. Use Nueva liquidación para crear una.");
        }
        if (codigoCC == null) {
            throw new IllegalArgumentException("No se encontró la cuenta de liquidaciones con un CodigoCC válido.");
        }
        return new Liquidacion(numero, idLiquidador, idEmpresa, codigoCC);
    }

    static String siguienteCodigo(Connection connection, String idEmpresa, java.sql.Date fecha,
            boolean centroCosto) throws SQLException {
        String prefijo = idEmpresa + new SimpleDateFormat("yyyyMMdd").format(fecha) + (centroCosto ? "9" : "2");
        String sql = centroCosto
                ? "SELECT CodigoCC AS Codigo FROM contabilidad_partida WHERE IdEmpresa = ? AND CodigoCC LIKE ?"
                : "SELECT CodigoPartida AS Codigo FROM contabilidad_partida WHERE IdEmpresa = ? AND CodigoPartida LIKE ?";
        int ultimo = 0;
        try (PreparedStatement statement = preparar(connection, sql + " FOR UPDATE", idEmpresa, prefijo + "%");
                ResultSet registros = statement.executeQuery()) {
            while (registros.next()) {
                String sufijo = registros.getString("Codigo").substring(prefijo.length());
                if (sufijo.matches("[0-9]{3}")) {
                    ultimo = Math.max(ultimo, Integer.parseInt(sufijo));
                }
            }
        }
        if (ultimo >= 999) {
            throw new IllegalArgumentException("Se agotó el correlativo diario de partidas o de CodigoCC.");
        }
        return prefijo + String.format("%03d", ultimo + 1);
    }

    static Liquidacion guardarFactura(Connection connection, Factura factura,
            boolean crearNueva, long numero, boolean numeroManual) throws SQLException {
        validarCuadre(factura.renglones);
        return enTransaccion(connection, () -> {
            long siguiente = siguienteLiquidacion(connection, factura.idEmpresa, true);
            try (PreparedStatement statement = preparar(connection,
                    "SELECT IdProveedor FROM proveedor_empresa WHERE IdEmpresa = ? AND IdProveedor = ?"
                            + " AND EsLiquidador = 1 AND Inhabilitado = 0 FOR UPDATE",
                    factura.idEmpresa, factura.idLiquidador);
                    ResultSet registros = statement.executeQuery()) {
                if (!registros.next()) {
                    throw new IllegalArgumentException("El liquidador no está habilitado para esta empresa.");
                }
            }
            Liquidacion liquidacion;
            if (crearNueva) {
                if (!liquidacionesAbiertas(connection, factura.idEmpresa, factura.idLiquidador, true).isEmpty()) {
                    throw new IllegalArgumentException("Ya existe una liquidación abierta para este liquidador y empresa. Selecciónela.");
                }
                try (PreparedStatement statement = preparar(connection,
                        "SELECT IdPartida FROM contabilidad_partida WHERE IdEmpresa = ? AND IdLiquidacion = ? FOR UPDATE",
                        factura.idEmpresa, siguiente); ResultSet registros = statement.executeQuery()) {
                    if (registros.next()) {
                        throw new IllegalArgumentException("El siguiente correlativo ya existe. Revise la configuración de la empresa.");
                    }
                }
                liquidacion = new Liquidacion(siguiente, factura.idLiquidador, factura.idEmpresa,
                        siguienteCodigo(connection, factura.idEmpresa, factura.fecha, true));
            } else {
                if (numeroManual && numero > siguiente) {
                    throw new IllegalArgumentException("El número manual no puede superar el siguiente correlativo: " + siguiente + ".");
                }
                liquidacion = buscarLiquidacion(connection, factura.idEmpresa, factura.idLiquidador,
                        numero, factura.cuentaLiquidaciones, true);
            }
            try (PreparedStatement statement = preparar(connection,
                    "SELECT IdPartida FROM contabilidad_partida WHERE IdEmpresa = ? AND SerieDocumento = ?"
                            + " AND NumeroDocumento = ? AND TipoDocumento = ? AND MonedaDocumento = ? FOR UPDATE",
                    factura.idEmpresa, factura.serie, factura.numero, factura.tipoDocumento, factura.moneda);
                    ResultSet registros = statement.executeQuery()) {
                if (registros.next()) {
                    throw new IllegalArgumentException("Este documento ya fue ingresado. Revise la serie y el número.");
                }
            }
            for (Renglon renglon : factura.renglones) {
                try (PreparedStatement statement = preparar(connection,
                        "SELECT IdNomenclatura FROM contabilidad_nomenclatura_empresa"
                                + " WHERE IdEmpresa = ? AND IdNomenclatura = ? AND Estatus = 'HABILITADA' FOR UPDATE",
                        factura.idEmpresa, renglon.cuenta); ResultSet registros = statement.executeQuery()) {
                    if (!registros.next()) {
                        throw new IllegalArgumentException((renglon.esAjusteRedondeo()
                                ? "La cuenta de Redondeo " : "La cuenta contable ")
                                + renglon.cuenta + " no está habilitada para esta empresa.");
                    }
                }
            }
            String codigoPartida = siguienteCodigo(connection, factura.idEmpresa, factura.fecha, false);
            String sql = "INSERT INTO contabilidad_partida (IdEmpresa, Estatus, CodigoPartida, CodigoCC,"
                    + " TipoDocumento, Fecha, NITProveedor, IdProveedor, NombreProveedor, SerieDocumento, NumeroDocumento,"
                    + " IdNomenclatura, MonedaDocumento, Debe, Haber, DebeQuetzales, HaberQuetzales, TipoCambio, Saldo,"
                    + " IdLiquidador, IdLiquidacion, Descripcion, IdCentroCosto, CodigoCentroCosto, CreadoUsuario, CreadoFechaYHora)"
                    + " VALUES (?, 'INGRESADO', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
            for (Renglon renglon : factura.renglones) {
                try (PreparedStatement statement = preparar(connection, sql, liquidacion.idEmpresa, codigoPartida,
                        liquidacion.codigoCC, factura.tipoDocumento, factura.fecha, factura.nitProveedor,
                        factura.idProveedor, factura.nombreProveedor, factura.serie, factura.numero, renglon.cuenta,
                        factura.moneda, renglon.debe, renglon.haber, renglon.debeQuetzales, renglon.haberQuetzales,
                        factura.tipoCambio, renglon.haber, liquidacion.idLiquidador, liquidacion.idLiquidacion,
                        (renglon.esAjusteRedondeo() ? "AJUSTE POR REDONDEO LIQUIDACION GASTO " : "LIQUIDACION GASTO ")
                                + liquidacion.idLiquidacion + " " + factura.nombreLiquidador,
                        factura.idCentroCosto, factura.codigoCentroCosto, factura.idUsuario)) {
                    statement.executeUpdate();
                }
            }
            if (factura.idFel != null) {
                try (PreparedStatement statement = preparar(connection,
                        "UPDATE documentos_fel_sat SET Contabilizada = 'S', IdLiquidacion = ?"
                                + " WHERE IdEmpresa = ? AND IdLiquidador = ? AND Id = ? AND Contabilizada = 'N'"
                                + " AND Estatus = 'ACTIVA' AND UPPER(Accion) = 'LIQUIDACIÓN' AND Serie = ? AND Numero = ?",
                        liquidacion.idLiquidacion, liquidacion.idEmpresa, liquidacion.idLiquidador,
                        factura.idFel, factura.serie, factura.numero)) {
                    if (statement.executeUpdate() != 1) {
                        throw new IllegalArgumentException("El documento FEL cambió o ya fue contabilizado. Vuelva a seleccionarlo.");
                    }
                }
            }
            if (!factura.token.isEmpty()) {
                actualizarToken(connection, factura.token, codigoPartida, factura.idUsuario, factura.nombreUsuario);
            }
            if (crearNueva) {
                try (PreparedStatement statement = preparar(connection,
                        "UPDATE contabilidad_empresa SET IdUltimaLiquidacion = ? WHERE IdEmpresa = ?",
                        liquidacion.idLiquidacion, liquidacion.idEmpresa)) {
                    if (statement.executeUpdate() != 1) {
                        throw new SQLException("No se pudo actualizar el correlativo de la empresa.");
                    }
                }
            }
            return liquidacion;
        });
    }

    static void actualizarToken(Connection connection, String token, String codigoPartida,
            String idUsuario, String nombreUsuario) throws SQLException {
        try (PreparedStatement statement = preparar(connection,
                "UPDATE token SET IdUsuario = ?, Usuario = ?, FechaUsado = CURRENT_TIMESTAMP,"
                        + " CodigoPartida = ?, Estatus = 'UTILIZADO' WHERE Codigo = ? AND Estatus <> 'UTILIZADO'",
                idUsuario, nombreUsuario, codigoPartida, token)) {
            if (statement.executeUpdate() != 1) {
                throw new IllegalArgumentException("El token no está disponible.");
            }
        }
    }

    public IngresoLiquidacionGastoForm(
            String idPartidaEdit, 
            String idLiquidacionEdit, 
            String idLiquidadorEdit, 
            String idEmpresaEdit,
            String codigoCC
    ) {
        addCloseShortcut(com.vaadin.event.ShortcutAction.KeyCode.ESCAPE, null);
        setModal(true);

        this.mainUI = UI.getCurrent();

        setWidth("95%");
        setHeight("100%");
        this.setResponsive(true);

        Page.getCurrent().setTitle(((SopdiUI) mainUI).sessionInformation.getStrCompanyName() + " - Documento Liquidación");

        marginInfo = new MarginInfo(true, true, true, true);

        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setResponsive(true);
        mainLayout.setMargin(true);

        Label titleLbl = new Label(empresaId + " " + empresaNombre + " Ingreso de liquidación");
        titleLbl.addStyleName(ValoTheme.LABEL_H3);

        createEncabezado();
        crearDocumentoYPartida();

        if (idLiquidacionEdit != null && !idLiquidacionEdit.trim().isEmpty()) {
            if (idEmpresaEdit == null || !idEmpresaEdit.equals(empresaId)
                    || idLiquidadorEdit == null || !liquidadorCbx.containsId(idLiquidadorEdit)) {
                Notification.show("La liquidación no pertenece a un liquidador habilitado de esta empresa.",
                        Notification.Type.WARNING_MESSAGE);
            } else {
                liquidadorCbx.select(idLiquidadorEdit);
                seleccionarLiquidacion(idLiquidacionEdit, false);
            }
        }

        setContent(mainLayout);
    }

    public void createEncabezado() {

        HorizontalLayout layoutEncabezado = new HorizontalLayout();
        layoutEncabezado.setSpacing(true);
        
        layoutEncabezado.setWidth("95%");
        layoutEncabezado.addStyleName("rcorners3");

        idLiquidacionTxt = new TextField("Liquidación:");
        idLiquidacionTxt.addStyleName(ValoTheme.TEXTFIELD_HUGE);
        idLiquidacionTxt.setWidth("30%");
        idLiquidacionTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);

        liquidadorCbx = new ComboBox("Liquidador:");
        liquidadorCbx.addStyleName(ValoTheme.COMBOBOX_HUGE);
        liquidadorCbx.setWidth("100%");
        liquidadorCbx.setInvalidAllowed(false);
        liquidadorCbx.setNewItemsAllowed(false);
        liquidadorCbx.addValueChangeListener(e -> {
            if (!actualizandoSeleccion) {
                limpiarSeleccionLiquidacion();
                cargarLiquidacionesAbiertas();
                llenarComboPorContabilizar();
                if (montoTxt != null) {
                    limpiarTodo();
                }
            }
        });

        llenarComboLiquidador();

        reportLayoutFactura.setWidth("85%");
        reportLayoutFactura.addStyleName("rcorners3");
        reportLayoutFactura.setResponsive(true);
        reportLayoutFactura.setMargin(true);

        container.addContainerProperty(ID_PROPERTY, String.class, null);
        container.addContainerProperty(CODIGO_PARTIDA_PROPERTY, String.class, null);
        container.addContainerProperty(SERIE_PROPERTY, String.class, null);
        container.addContainerProperty(NUMERO_PROPERTY, String.class, null);
        container.addContainerProperty(PROVEEDOR_PROPERTY, String.class, null);
        container.addContainerProperty(FECHA_PROPERTY, String.class, null);
        container.addContainerProperty(MONTO_PROPERTY, String.class, null);
        container.addContainerProperty(MONEDA_PROPERTY, String.class, null);
        container.addContainerProperty(TIPOCAMBIO_PROPERTY, String.class, null);
        container.addContainerProperty(ELIMINAR_PROPERTY, String.class, null);

        facturaGastoGrid = new Grid("Facturas", container);

        facturaGastoGrid.setImmediate(true);
        facturaGastoGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        facturaGastoGrid.setDescription("Seleccione un registro.");
        facturaGastoGrid.setHeightMode(HeightMode.ROW);
        facturaGastoGrid.setHeightByRows(3);

        facturaGastoGrid.setWidth("100%");
//        facturaGastoGrid.setHeight("50%");
        facturaGastoGrid.setResponsive(true);
        facturaGastoGrid.setEditorBuffered(false);

        facturaGastoGrid.getColumn(ELIMINAR_PROPERTY).setRenderer(new ButtonRenderer(this::eliminarRegistroTabla));

        facturaGastoGrid.getColumn(ID_PROPERTY).setHidable(true).setHidden(true);
        facturaGastoGrid.getColumn(CODIGO_PARTIDA_PROPERTY).setHidable(true).setHidden(true);
        facturaGastoGrid.getColumn(SERIE_PROPERTY);
        facturaGastoGrid.getColumn(NUMERO_PROPERTY);
        facturaGastoGrid.getColumn(PROVEEDOR_PROPERTY);
        facturaGastoGrid.getColumn(FECHA_PROPERTY);
        facturaGastoGrid.getColumn(MONTO_PROPERTY);
        facturaGastoGrid.setCellStyleGenerator(cellRef
                -> MONTO_PROPERTY.equals(cellRef.getPropertyId())
                ? "rightalign" : null);

        facturaGastoGrid.addSelectionListener(new SelectionListener() {
            @Override
            public void select(SelectionEvent event) {
                if (facturaGastoGrid.getSelectedRow() != null) {
                    llenarCamposFactura(
                            String.valueOf(facturaGastoGrid.getContainerDataSource().getItem(facturaGastoGrid.getSelectedRow()).getItemProperty(SERIE_PROPERTY).getValue()),
                            String.valueOf(facturaGastoGrid.getContainerDataSource().getItem(facturaGastoGrid.getSelectedRow()).getItemProperty(NUMERO_PROPERTY).getValue()),
                            String.valueOf(facturaGastoGrid.getContainerDataSource().getItem(facturaGastoGrid.getSelectedRow()).getItemProperty(PROVEEDOR_PROPERTY).getValue()),
                            String.valueOf(facturaGastoGrid.getContainerDataSource().getItem(facturaGastoGrid.getSelectedRow()).getItemProperty(FECHA_PROPERTY).getValue()),
                            String.valueOf(facturaGastoGrid.getContainerDataSource().getItem(facturaGastoGrid.getSelectedRow()).getItemProperty(CODIGO_PARTIDA_PROPERTY).getValue()));
                }
            }
        });

        footer = facturaGastoGrid.appendFooterRow();

        reportLayoutFactura.addComponent(facturaGastoGrid);
        reportLayoutFactura.setComponentAlignment(facturaGastoGrid, Alignment.MIDDLE_CENTER);

        Button nuevaBtn = new Button("Nueva factura");
        nuevaBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        nuevaBtn.setIcon(FontAwesome.PLUS);
        nuevaBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if(liquidadorCbx.getValue() == null) {
                    Notification.show("Por favor elija a la persona liquidador.");
                    liquidadorCbx.focus();
                    return;
                }
                nuevaliquidacion();
            }
        });
        reportLayoutFactura.addComponent(nuevaBtn);
        reportLayoutFactura.setComponentAlignment(nuevaBtn, Alignment.MIDDLE_CENTER);

        liquidacionesCbx = new ComboBox("Liquidaciones abiertas:");
        liquidacionesCbx.setWidth("100%");
        liquidacionesCbx.setNewItemsAllowed(false);
        liquidacionesCbx.setInvalidAllowed(false);
        liquidacionesCbx.setNullSelectionAllowed(false);
        liquidacionesCbx.addValueChangeListener(event -> {
            if (!actualizandoSeleccion && liquidacionesCbx.getValue() != null) {
                seleccionarLiquidacion(liquidacionesCbx.getValue().toString(), false);
            }
        });
        idLiquidacionTxt.setWidth("100%");
        idLiquidacionTxt.setImmediate(true);
        idLiquidacionTxt.addValueChangeListener(event -> {
            if (!actualizandoSeleccion) {
                invalidarLiquidacion();
            }
        });
        Button usarNumeroBtn = new Button("Usar número");
        usarNumeroBtn.addClickListener(event -> seleccionarLiquidacion(idLiquidacionTxt.getValue(), true));
        Button nuevaLiquidacionBtn = new Button("Nueva liquidación");
        nuevaLiquidacionBtn.setIcon(FontAwesome.PLUS);
        nuevaLiquidacionBtn.setDescription("El correlativo se asigna al guardar la primera factura.");
        nuevaLiquidacionBtn.addClickListener(event -> prepararNuevaLiquidacion());
        HorizontalLayout accionesLiquidacion = new HorizontalLayout(usarNumeroBtn, nuevaLiquidacionBtn);
        accionesLiquidacion.setSpacing(true);
        VerticalLayout seleccionLayout = new VerticalLayout(idLiquidacionTxt, liquidacionesCbx, accionesLiquidacion);
        seleccionLayout.setWidth("60%");
        seleccionLayout.setSpacing(true);
        layoutEncabezado.addComponent(seleccionLayout);
        layoutEncabezado.setComponentAlignment(seleccionLayout, Alignment.TOP_CENTER);
        layoutEncabezado.addComponent(liquidadorCbx);
        layoutEncabezado.setComponentAlignment(liquidadorCbx, Alignment.TOP_CENTER);

        mainLayout.addComponent(layoutEncabezado);
        mainLayout.setComponentAlignment(layoutEncabezado, Alignment.TOP_CENTER);
        mainLayout.addComponent(reportLayoutFactura);
        mainLayout.setComponentAlignment(reportLayoutFactura, Alignment.MIDDLE_CENTER);

    }


    private void invalidarLiquidacion() {
        liquidacionSeleccionada = null;
        nuevaLiquidacion = false;
        seleccionManual = false;
        actualizandoSeleccion = true;
        try {
            liquidacionesCbx.clear();
        } finally {
            actualizandoSeleccion = false;
        }
        container.removeAllItems();
        footer.getCell(PROVEEDOR_PROPERTY).setText("0 facturas");
        if (cerrarLiquidacionBtn != null) {
            cerrarLiquidacionBtn.setEnabled(false);
        }
    }

    private void limpiarSeleccionLiquidacion() {
        invalidarLiquidacion();
        actualizandoSeleccion = true;
        try {
            idLiquidacionTxt.setReadOnly(false);
            idLiquidacionTxt.setValue("");
            idLiquidacionTxt.setInputPrompt("Seleccione o ingrese un número");
        } finally {
            actualizandoSeleccion = false;
        }
    }

    private void cargarLiquidacionesAbiertas() {
        actualizandoSeleccion = true;
        try {
            liquidacionesCbx.removeAllItems();
            if (liquidadorCbx.getValue() == null) {
                return;
            }
            Set<Long> abiertas = liquidacionesAbiertas(conexion(), empresaId, liquidadorCbx.getValue().toString(), false);
            for (Long numero : abiertas) {
                liquidacionesCbx.addItem(numero.toString());
                liquidacionesCbx.setItemCaption(numero.toString(), numero.toString());
            }
            if (abiertas.size() > 1) {
                Notification.show("Existen varias liquidaciones abiertas. Seleccione explícitamente cuál utilizar.",
                        Notification.Type.WARNING_MESSAGE);
            }
        } catch (SQLException exception) {
            informarError("No se pudieron consultar las liquidaciones abiertas.", exception);
        } finally {
            actualizandoSeleccion = false;
        }
    }

    private void seleccionarLiquidacion(String numero, boolean manual) {
        invalidarLiquidacion();
        actualizandoSeleccion = true;
        try {
            idLiquidacionTxt.setReadOnly(false);
            idLiquidacionTxt.setValue(numero == null ? "" : numero);
        } finally {
            actualizandoSeleccion = false;
        }
        try {
            if (liquidadorCbx.getValue() == null) {
                throw new IllegalArgumentException("Seleccione primero al liquidador.");
            }
            long idLiquidacion = numeroLiquidacion(numero);
            long siguiente = siguienteLiquidacion(conexion(), empresaId, false);
            if (manual && idLiquidacion > siguiente) {
                throw new IllegalArgumentException("El número manual no puede superar el siguiente correlativo: " + siguiente + ".");
            }
            Liquidacion liquidacion = buscarLiquidacion(conexion(), empresaId, liquidadorCbx.getValue().toString(),
                    idLiquidacion, ((SopdiUI) mainUI).cuentasContablesDefault.getLiquidacionesCajaChicha(), false);
            aplicarLiquidacion(liquidacion);
            seleccionManual = manual;
            limpiarTodo();
            llenarTablaLiquidacion();
        } catch (SQLException | IllegalArgumentException exception) {
            informarError("No se pudo seleccionar la liquidación.", exception);
        }
    }

    private void aplicarLiquidacion(Liquidacion liquidacion) {
        actualizandoSeleccion = true;
        try {
            liquidacionSeleccionada = liquidacion;
            nuevaLiquidacion = false;
            seleccionManual = false;
            idLiquidacionTxt.setReadOnly(false);
            idLiquidacionTxt.setInputPrompt("Seleccione o ingrese un número");
            idLiquidacionTxt.setValue(Long.toString(liquidacion.idLiquidacion));
            if (!liquidacionesCbx.containsId(Long.toString(liquidacion.idLiquidacion))) {
                liquidacionesCbx.addItem(Long.toString(liquidacion.idLiquidacion));
            }
            liquidacionesCbx.select(Long.toString(liquidacion.idLiquidacion));
        } finally {
            actualizandoSeleccion = false;
        }
        cerrarLiquidacionBtn.setEnabled(true);
    }

    private void prepararNuevaLiquidacion() {
        limpiarSeleccionLiquidacion();
        try {
            if (liquidadorCbx.getValue() == null) {
                throw new IllegalArgumentException("Seleccione primero al liquidador.");
            }
            if (!liquidacionesAbiertas(conexion(), empresaId, liquidadorCbx.getValue().toString(), false).isEmpty()) {
                cargarLiquidacionesAbiertas();
                throw new IllegalArgumentException("Ya existe una liquidación abierta para este liquidador y empresa. Selecciónela.");
            }
            nuevaLiquidacion = true;
            idLiquidacionTxt.setInputPrompt("Nueva: correlativo al guardar");
            idLiquidacionTxt.setReadOnly(true);
            limpiarTodo();
            Notification.show("Nueva liquidación preparada. Se creará al guardar la primera factura.",
                    Notification.Type.HUMANIZED_MESSAGE);
        } catch (SQLException | IllegalArgumentException exception) {
            informarError("No se pudo preparar la nueva liquidación.", exception);
        }
    }

    private void informarError(String mensaje, Exception exception) {
        if (exception instanceof SQLException) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, mensaje, exception);
        }
        Notification.show(mensaje + " " + exception.getMessage(), Notification.Type.WARNING_MESSAGE);
    }

    private void actualizarVista() {
        if (mainUI.getNavigator().getCurrentView() instanceof IngresoLiquidacionGastoView) {
            ((IngresoLiquidacionGastoView) mainUI.getNavigator().getCurrentView()).llenarTablaLiquidacion(empresaId);
        }
    }

    public void llenarComboProveedor() {
        try (PreparedStatement statement = preparar(conexion(),
                "SELECT IdProveedor, Nombre, NIT FROM proveedor_empresa"
                        + " WHERE Inhabilitado = 0 AND EsProveedor = 1 AND IdEmpresa = ? ORDER BY Nombre", empresaId);
                ResultSet registros = statement.executeQuery()) {
            while (registros.next()) {
                String id = registros.getString("IdProveedor");
                proveedorCbx.addItem(id);
                proveedorCbx.setItemCaption(id, registros.getString("Nombre"));
                proveedorCbx.getItem(id).getItemProperty(NIT_PROPERTY).setValue(registros.getString("NIT"));
            }
        } catch (SQLException exception) {
            informarError("No se pudieron consultar los proveedores.", exception);
        }
    }

    public void verificarProveedor() {
         if (nitProveedorTxt == null) {
            return;
        }
        
        if (proveedorCbx == null) {
            return;
        }
        if (proveedorCbx.getValue() == null) {
            return;
        }
        try {
            Integer.valueOf(String.valueOf(proveedorCbx.getValue()));
        }
        catch(Exception strE) {
            return;
        }

        nitProveedorTxt.setValue("");
        
        nitProveedorTxt.setValue(String.valueOf(proveedorCbx.getItem(proveedorCbx.getValue()).getItemProperty(NIT_PROPERTY).getValue()));
    }

    public void crearDocumentoYPartida() {

        HorizontalLayout horizontalLayout =  new HorizontalLayout();
        horizontalLayout.setSpacing(true);
        horizontalLayout.setResponsive(true);
        horizontalLayout.setSizeFull();

        VerticalLayout leftVerticalLayout =  new VerticalLayout();
        leftVerticalLayout.setSpacing(true);
        leftVerticalLayout.setResponsive(true);
        leftVerticalLayout.setWidth("100%");
        leftVerticalLayout.addStyleName("rcorners3");

        VerticalLayout rightVerticalLayout =  new VerticalLayout();
        rightVerticalLayout.setSpacing(true);
        rightVerticalLayout.setResponsive(true);
        rightVerticalLayout.setSizeFull();
        rightVerticalLayout.addStyleName("rcorners3");

        horizontalLayout.addComponents(leftVerticalLayout, rightVerticalLayout);
        horizontalLayout.setComponentAlignment(leftVerticalLayout, Alignment.MIDDLE_LEFT);
        horizontalLayout.setComponentAlignment(leftVerticalLayout, Alignment.MIDDLE_RIGHT);

        mainLayout.addComponent(horizontalLayout);

        porContabilizarCbx.setWidth("100%");
        porContabilizarCbx.setNullSelectionAllowed(true);
        porContabilizarCbx.setInvalidAllowed(false);
        porContabilizarCbx.setNewItemsAllowed(false);
        porContabilizarCbx.addValueChangeListener(e -> {
            llenarFormularioPorContabilizar();
        });

        llenarComboPorContabilizar();

        centroCostoCbx = new ComboBox();
        centroCostoCbx.setInputPrompt("Centro de costo");
        centroCostoCbx.setDescription("Centro de costo");
        centroCostoCbx.setWidth("100%");
        centroCostoCbx.setTextInputAllowed(false);
        centroCostoCbx.setNewItemsAllowed(false);
        centroCostoCbx.setNullSelectionAllowed(true);
        centroCostoCbx.addValueChangeListener(event -> {
            if (event.getProperty() == null) {
            }
//            verificarCuentaContableAplicar();
        });

        llenarComboCentroCosto();

        tipoDocumentoCbx = new ComboBox();
        tipoDocumentoCbx.setInputPrompt("Tipo de documento");
        tipoDocumentoCbx.setDescription("Tipo de documento");
        tipoDocumentoCbx.setWidth("100%");
        tipoDocumentoCbx.addItem("FACTURA");
        tipoDocumentoCbx.addItem("RECIBO CONTABLE");
        tipoDocumentoCbx.addItem("FORMULARIO");
        tipoDocumentoCbx.select("FACTURA");
        tipoDocumentoCbx.addValueChangeListener(event -> {
            if (event.getProperty() == null) {
                return;
            }
            if(proveedorCbx != null) {
                if(tipoDocumentoCbx.getValue().equals("RECIBO CONTABLE") || tipoDocumentoCbx.getValue().equals("FORMULARIO")) {
                    proveedorCbx.setInvalidAllowed(false);
                    proveedorCbx.setNewItemsAllowed(false);
                }
                else {
                    proveedorCbx.setInvalidAllowed(true);
                    proveedorCbx.setNewItemsAllowed(true);
                }
            }
        });

        serieTxt = new TextField();
        serieTxt.setInputPrompt("Serie de documento");
        serieTxt.setDescription("Serie de documento");
        serieTxt.setWidth("100%");
        serieTxt.addStyleName("mayusculas");

        numeroTxt = new TextField();
        numeroTxt.setInputPrompt("Número de documento");
        numeroTxt.setDescription("Número de documento");
        numeroTxt.setWidth("100%");

        proveedorCbx = new ComboBox();
        proveedorCbx.setInputPrompt("Proveedores");
        proveedorCbx.setWidth("100%");
        proveedorCbx.addContainerProperty(NIT_PROPERTY, String.class, "");
//        proveedorCbx.setNullSelectionAllowed(true);
        proveedorCbx.setInvalidAllowed(true);
        proveedorCbx.setNewItemsAllowed(true);
        proveedorCbx.setFilteringMode(FilteringMode.CONTAINS);
        llenarComboProveedor();
        proveedorCbx.addValueChangeListener(event -> {
            if (event.getProperty() == null) {
		        return;
            }
            verificarProveedor();
//            Notification.show(String.valueOf(proveedorCbx.getValue()), Notification.Type.HUMANIZED_MESSAGE);
//            Notification.show(proveedorCbx.getItemCaption(proveedorCbx.getValue()), Notification.Type.HUMANIZED_MESSAGE);
        });

        nitProveedorTxt = new TextField();
        nitProveedorTxt.setInputPrompt("Nit del proveeedor");
        nitProveedorTxt.setDescription("Nit del proveedor");
        nitProveedorTxt.setWidth("100%");
//        nitProveedorTxt.addValueChangeListener(event
//                -> buscarProveedorPorNit()
//        );

        fechaDt = new DateField("Fecha :");
        fechaDt.setDateFormat("dd/MM/yyyy");
        fechaDt.setWidth("100%");
        fechaDt.setValue(new java.util.Date());

        montoTxt = new NumberField();
        montoTxt.setInputPrompt("Monto");
        montoTxt.setDescription("Monto del documento");
        montoTxt.setDecimalAllowed(true);
        montoTxt.setDecimalPrecision(2);
        montoTxt.setMinimumFractionDigits(2);
        montoTxt.setDecimalSeparator('.');
        montoTxt.setDecimalSeparatorAlwaysShown(true);
        montoTxt.setValue(0d);
        montoTxt.setGroupingUsed(true);
        montoTxt.setGroupingSeparator(',');
        montoTxt.setGroupingSize(3);
        montoTxt.setImmediate(true);
        montoTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        montoTxt.setWidth("100%");

        montoTxt.addValueChangeListener(event -> {
            if (cuentaContable1Cbx != null) {
                haberLbl.setValue("HABER = " + cuentaContable1Cbx.getItemCaption(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getLiquidacionesCajaChicha()) + " " + montoTxt.getFormattedValue());
                if(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getIvaPorCobrar() == null) { //empresa exonerada
                    debe1Txt.setValue(montoTxt.getValue());
                }
            }
        });

        monedaCbx = new ComboBox();
        monedaCbx.setInputPrompt("Moneda");
        monedaCbx.setWidth("100%");
        monedaCbx.addItem("QUETZALES");
        monedaCbx.addItem("DOLARES");
        monedaCbx.select("QUETZALES");
        monedaCbx.setVisible(true);
        monedaCbx.setInvalidAllowed(false);
        monedaCbx.setNewItemsAllowed(false);
        monedaCbx.addValueChangeListener(evet -> {
            if(monedaCbx.getValue() != null){
                if(monedaCbx.getValue().equals("DOLARES")){                                       
                   tasaCambioTxt.setValue((Float.toString(((SopdiUI) UI.getCurrent()).sessionInformation.getFltExchangeRate())));                   
                }else{
                    tasaCambioTxt.setValue(1.00);
                }
            }else{
                 tasaCambioTxt.setValue(1.00);
            }
        });

        tasaCambioTxt = new NumberField();
        tasaCambioTxt.setInputPrompt("Tasa de cambio");
        tasaCambioTxt.setDescription("Tasa de cambio");
        tasaCambioTxt.setDecimalAllowed(true);
        tasaCambioTxt.setDecimalPrecision(5);
        tasaCambioTxt.setMinimumFractionDigits(5);
        tasaCambioTxt.setDecimalSeparator('.');
        tasaCambioTxt.setDecimalSeparatorAlwaysShown(true);        
        tasaCambioTxt.setGroupingUsed(true);
        tasaCambioTxt.setGroupingSeparator(',');
        tasaCambioTxt.setGroupingSize(3);
        tasaCambioTxt.setImmediate(true);
        tasaCambioTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        tasaCambioTxt.setWidth("100%");
        tasaCambioTxt.setValue(1.00);
        tasaCambioTxt.setVisible(true);

        leftVerticalLayout.addComponent(porContabilizarCbx);
        leftVerticalLayout.addComponent(tipoDocumentoCbx);
        leftVerticalLayout.addComponent(centroCostoCbx);
        leftVerticalLayout.addComponent(fechaDt);
        leftVerticalLayout.addComponent(serieTxt);
        leftVerticalLayout.addComponent(numeroTxt);
        leftVerticalLayout.addComponent(proveedorCbx);
        leftVerticalLayout.addComponent(nitProveedorTxt);
        leftVerticalLayout.addComponent(montoTxt);
        leftVerticalLayout.addComponent(monedaCbx);
        leftVerticalLayout.addComponent(tasaCambioTxt);

        HorizontalLayout layoutHorizontal1 = new HorizontalLayout();
        layoutHorizontal1.setSpacing(true);

        HorizontalLayout layoutHorizontal2 = new HorizontalLayout();
        layoutHorizontal2.setSpacing(true);

        HorizontalLayout layoutHorizontal3 = new HorizontalLayout();
        layoutHorizontal3.setSpacing(true);

        HorizontalLayout layoutHorizontal4 = new HorizontalLayout();
        layoutHorizontal4.setSpacing(true);

        cuentaContable1Cbx = new ComboBox("Cuenta contable :");
        cuentaContable1Cbx.setWidth("34em");
        cuentaContable1Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable1Cbx.setInvalidAllowed(false);
        cuentaContable1Cbx.setNewItemsAllowed(false);

        cuentaContable2Cbx = new ComboBox();
        cuentaContable2Cbx.setWidth("34em");
        cuentaContable2Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable2Cbx.setInvalidAllowed(false);
        cuentaContable2Cbx.setNewItemsAllowed(false);

        cuentaContable3Cbx = new ComboBox();
        cuentaContable3Cbx.setWidth("34em");
        cuentaContable3Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable3Cbx.setInvalidAllowed(false);
        cuentaContable3Cbx.setNewItemsAllowed(false);

        cuentaContable4Cbx = new ComboBox();
        cuentaContable4Cbx.setWidth("34em");
        cuentaContable4Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable4Cbx.setInvalidAllowed(false);
        cuentaContable4Cbx.setNewItemsAllowed(false);

        llenarComboCuentaContable();

        HorizontalLayout haberLayout = new HorizontalLayout();

        haberLbl.addStyleName(ValoTheme.LABEL_SUCCESS);
        haberLbl.setValue("HABER = " + cuentaContable1Cbx.getItemCaption(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getLiquidacionesCajaChicha()) + " " + montoTxt.getFormattedValue());

        haberLayout.addComponent(haberLbl);
        haberLayout.setComponentAlignment(haberLbl, Alignment.TOP_CENTER);

        debe1Txt = new NumberField("Monto : ");
        debe1Txt.setDecimalAllowed(true);
        debe1Txt.setDecimalPrecision(2);
        debe1Txt.setMinimumFractionDigits(2);
        debe1Txt.setDecimalSeparator('.');
        debe1Txt.setDecimalSeparatorAlwaysShown(true);
        debe1Txt.setValue(0d);
        debe1Txt.setGroupingUsed(true);
        debe1Txt.setGroupingSeparator(',');
        debe1Txt.setGroupingSize(3);
        debe1Txt.setImmediate(true);
        debe1Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        debe1Txt.setWidth("7em");
        debe1Txt.setValue(0.00);

        debe2Txt = new NumberField();
        debe2Txt.setDecimalAllowed(true);
        debe2Txt.setDecimalPrecision(2);
        debe2Txt.setMinimumFractionDigits(2);
        debe2Txt.setDecimalSeparator('.');
        debe2Txt.setDecimalSeparatorAlwaysShown(true);
        debe2Txt.setValue(0d);
        debe2Txt.setGroupingUsed(true);
        debe2Txt.setGroupingSeparator(',');
        debe2Txt.setGroupingSize(3);
        debe2Txt.setImmediate(true);
        debe2Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        debe2Txt.setWidth("7em");
        debe2Txt.setValue(0.00);

        debe3Txt = new NumberField();
        debe3Txt.setDecimalAllowed(true);
        debe3Txt.setDecimalPrecision(2);
        debe3Txt.setMinimumFractionDigits(2);
        debe3Txt.setDecimalSeparator('.');
        debe3Txt.setDecimalSeparatorAlwaysShown(true);
        debe3Txt.setValue(0d);
        debe3Txt.setGroupingUsed(true);
        debe3Txt.setGroupingSeparator(',');
        debe3Txt.setGroupingSize(3);
        debe3Txt.setImmediate(true);
        debe3Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        debe3Txt.setWidth("7em");
        debe3Txt.setValue(0.00);

        debe4Txt = new NumberField();
        debe4Txt.setDecimalAllowed(true);
        debe4Txt.setDecimalPrecision(2);
        debe4Txt.setMinimumFractionDigits(2);
        debe4Txt.setDecimalSeparator('.');
        debe4Txt.setDecimalSeparatorAlwaysShown(true);
        debe4Txt.setValue(0d);
        debe4Txt.setGroupingUsed(true);
        debe4Txt.setGroupingSeparator(',');
        debe4Txt.setGroupingSize(3);
        debe4Txt.setImmediate(true);
        debe4Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        debe4Txt.setWidth("7em");
        debe4Txt.setValue(0.00);

        layoutHorizontal1.addComponent(cuentaContable1Cbx);
        layoutHorizontal1.addComponent(debe1Txt);

        layoutHorizontal2.addComponent(cuentaContable2Cbx);
        layoutHorizontal2.addComponent(debe2Txt);

        layoutHorizontal3.addComponent(cuentaContable3Cbx);
        layoutHorizontal3.addComponent(debe3Txt);

        layoutHorizontal4.addComponent(cuentaContable4Cbx);
        layoutHorizontal4.addComponent(debe4Txt);

        rightVerticalLayout.addComponent(haberLayout);
        rightVerticalLayout.setComponentAlignment(haberLayout, Alignment.MIDDLE_CENTER);

        rightVerticalLayout.addComponent(layoutHorizontal1);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal1, Alignment.MIDDLE_CENTER);

        rightVerticalLayout.addComponent(layoutHorizontal2);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal2, Alignment.MIDDLE_CENTER);

        rightVerticalLayout.addComponent(layoutHorizontal3);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal3, Alignment.MIDDLE_CENTER);

        rightVerticalLayout.addComponent(layoutHorizontal4);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal4, Alignment.MIDDLE_CENTER);

        HorizontalLayout layoutHorizontal5 = new HorizontalLayout();
        layoutHorizontal5.setSpacing(true);

        grabarBtn = new Button("Guardar");
        grabarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        grabarBtn.setIcon(FontAwesome.SAVE);
        grabarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                insertTablaFactura();
            }
        });

        cerrarLiquidacionBtn = new Button("Cerrar liquidación");
        cerrarLiquidacionBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        cerrarLiquidacionBtn.setEnabled(false);
        cerrarLiquidacionBtn.setIcon(FontAwesome.CHECK);
        cerrarLiquidacionBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de CERRAR esta liquidación  ?",
                        "SI", "NO", new ConfirmDialog.Listener() {

                            public void onClose(ConfirmDialog dialog) {
                                if (dialog.isConfirmed()) {
                                    cerrarLiquidacion();
                                }
                            }
                        });
            }
        });

        layoutHorizontal5.addComponent(grabarBtn);
        layoutHorizontal5.setComponentAlignment(grabarBtn, Alignment.BOTTOM_CENTER);
        layoutHorizontal5.addComponent(cerrarLiquidacionBtn);
        layoutHorizontal5.setComponentAlignment(cerrarLiquidacionBtn, Alignment.BOTTOM_RIGHT);

        rightVerticalLayout.addComponent(layoutHorizontal5);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal5, Alignment.MIDDLE_CENTER);

    }

    private void llenarComboPorContabilizar() {
        porContabilizarCbx.removeAllItems();
        if (liquidadorCbx.getValue() == null) {
            return;
        }
        try (PreparedStatement statement = preparar(conexion(),
                "SELECT Id, NombreProveedor, Serie, Numero FROM documentos_fel_sat"
                        + " WHERE Estatus = 'ACTIVA' AND IdEmpresa = ? AND Contabilizada = 'N'"
                        + " AND UPPER(Accion) = 'LIQUIDACIÓN' AND IdLiquidador = ?",
                empresaId, liquidadorCbx.getValue());
                ResultSet registros = statement.executeQuery()) {
            while (registros.next()) {
                String id = registros.getString("Id");
                porContabilizarCbx.addItem(id);
                porContabilizarCbx.setItemCaption(id, registros.getString("NombreProveedor") + " "
                        + registros.getString("Serie") + " " + registros.getString("Numero"));
            }
        } catch (Exception exception) {
            informarError("No se pudo cargar el documento FEL.", exception);
        }
    }

    private void llenarFormularioPorContabilizar() {

        tipoDocumentoCbx.setEnabled(true);
        centroCostoCbx.setEnabled(true);
        fechaDt.setEnabled(true);
        proveedorCbx.setEnabled(true);
        nitProveedorTxt.setEnabled(true);
        serieTxt.setEnabled(true);
        numeroTxt.setEnabled(true);
        montoTxt.setEnabled(true);
        monedaCbx.setEnabled(true);

        cuentaContable1Cbx.clear();
        cuentaContable2Cbx.clear();
        cuentaContable3Cbx.clear();
        cuentaContable4Cbx.clear();
        debe1Txt.setValue("0.00");
        debe2Txt.setValue("0.00");
        debe3Txt.setValue("0.00");
        debe4Txt.setValue("0.00");
        tipoDocumentoCbx.select("FACTURA");
        centroCostoCbx.select(null);
        serieTxt.setValue("");
        numeroTxt.setValue("");
        montoTxt.setValue(0.00);
        nitProveedorTxt.setValue("");
        proveedorCbx.select(null);

        if(porContabilizarCbx.getValue() == null) {
            return;
        }

        try (PreparedStatement statement = preparar(conexion(),
                "SELECT * FROM documentos_fel_sat WHERE IdEmpresa = ? AND Id = ? AND IdLiquidador = ?"
                        + " AND Contabilizada = 'N' AND Estatus = 'ACTIVA' AND UPPER(Accion) = 'LIQUIDACIÓN'",
                empresaId, porContabilizarCbx.getValue(), liquidadorCbx.getValue());
                ResultSet rsRecords = statement.executeQuery()) {

            if(rsRecords.next()) {
/**
 IdEmpresa,FechaEmision, NumeroAutorizacion, TipoDTE, Serie, Numero, IdProveedor, NitProveedor, NombreProveedor,
 CodigoEstablecimiento, NombreEstablecimiento,IdReceptor, NombreReceptor, NitCertificador, NombreCertificador,
 Moneda, Monto, Estatus, IVA,IDP, TurismoHospedaje,  TurismoPasajes, TimbrePrensa, Bomberos, BebidasAlcoholicas,
 Tabaco, Cemento, BebidasNoAlcoholicas, TarifaPortuaria, FechaCertificacion, Costo
 **/
                tipoDocumentoCbx.select(rsRecords.getString("TipoDte"));
                centroCostoCbx.select(rsRecords.getString("IdCentroCosto"));
                fechaDt.setValue(rsRecords.getDate("FechaEmision"));
                proveedorCbx.select(rsRecords.getString("IdProveedor"));
                serieTxt.setValue(rsRecords.getString("Serie"));
                numeroTxt.setValue(rsRecords.getString("Numero"));
                montoTxt.setValue(centavos(rsRecords.getBigDecimal("Monto")).toPlainString());
                monedaCbx.select(rsRecords.getString("Moneda"));

                tipoDocumentoCbx.setEnabled(false);
                centroCostoCbx.setEnabled(false);
                fechaDt.setEnabled(false);
                proveedorCbx.setEnabled(false);
                nitProveedorTxt.setEnabled(false);
                serieTxt.setEnabled(false);
                numeroTxt.setEnabled(false);
                montoTxt.setEnabled(false);
                monedaCbx.setEnabled(false);

                BigDecimal otrosImpuestos = BigDecimal.ZERO;
                String[] impuestos = {"IDP", "TurismoHospedaje", "TurismoPasajes", "TimbrePrensa",
                        "Bomberos", "TasaMunicipal", "BebidasAlcoholicas", "Tabaco", "Cemento",
                        "BebidasNoAlcoholicas", "TarifaPortuaria"};
                for (String impuesto : impuestos) {
                    BigDecimal valor = rsRecords.getBigDecimal(impuesto);
                    otrosImpuestos = otrosImpuestos.add(valor == null ? BigDecimal.ZERO : valor);
                }
                BigDecimal iva = rsRecords.getBigDecimal("IVA");
                iva = iva == null ? BigDecimal.ZERO : centavos(iva);
                cuentaContable1Cbx.select(((SopdiUI) mainUI).cuentasContablesDefault.getCompras());
                debe1Txt.setValue(centavos(rsRecords.getBigDecimal("Costo")).toPlainString());
                if (iva.signum() > 0) {
                    cuentaContable2Cbx.select(((SopdiUI) mainUI).cuentasContablesDefault.getIvaPorCobrar());
                    debe2Txt.setValue(iva.toPlainString());
                }
                if (otrosImpuestos.signum() > 0) {
                    ComboBox cuenta = iva.signum() == 0 ? cuentaContable2Cbx : cuentaContable3Cbx;
                    NumberField monto = iva.signum() == 0 ? debe2Txt : debe3Txt;
                    cuenta.select(((SopdiUI) mainUI).cuentasContablesDefault.getOtrosArbitrios());
                    monto.setValue(centavos(otrosImpuestos).toPlainString());
                }
            }
        } catch (SQLException exception) {
            informarError("No se pudieron consultar los documentos FEL.", exception);
        }
    }

    public void llenarComboCentroCosto() {
        try (PreparedStatement statement = preparar(conexion(),
                "SELECT IdCentroCosto, CodigoCentroCosto FROM centro_costo WHERE IdProyecto = ? AND Inhabilitado = 0",
                ((SopdiUI) mainUI).sessionInformation.getStrProjectId());
                ResultSet registros = statement.executeQuery()) {
            while (registros.next()) {
                String id = registros.getString("IdCentroCosto");
                centroCostoCbx.addItem(id);
                centroCostoCbx.setItemCaption(id, registros.getString("CodigoCentroCosto"));
            }
        } catch (SQLException exception) {
            informarError("No se pudieron consultar los centros de costo.", exception);
        }
    }

    public void comprobarCampos() {
        
        if (this.serieTxt.getValue().trim().isEmpty()) {
            Notification.show("Por favor ingrese la serie del documento.", Notification.Type.WARNING_MESSAGE);
            serieTxt.focus();
            return;
        }
        if (this.numeroTxt.getValue().trim().isEmpty()) {
            Notification.show("Por favor ingrese el número de documento.", Notification.Type.WARNING_MESSAGE);
            numeroTxt.focus();
            return;
        }

        if (proveedorCbx.isEmpty()) {
            Notification.show("Por favor ingrese el proveedor.", Notification.Type.WARNING_MESSAGE);
            proveedorCbx.focus();
            return;
        }
        if (nitProveedorTxt.isEmpty()) {
            Notification.show("Por favor ingrese el Nit del proveedor.", Notification.Type.WARNING_MESSAGE);
            nitProveedorTxt.focus();
            return;
        }
        if (monedaCbx.getValue() == null) {
            Notification.show("Por favor ingrese el tipo de moneda.", Notification.Type.WARNING_MESSAGE);
            monedaCbx.focus();
            return;
        }

        if (centavos(importe(montoTxt)).signum() <= 0) {
            Notification.show("Por favor ingrese el monto de la factura.", Notification.Type.WARNING_MESSAGE);
            montoTxt.focus();
            return;
        }
        
        serieTxt.setReadOnly(true);
        numeroTxt.setReadOnly(true);
        nitProveedorTxt.setReadOnly(true);
        proveedorCbx.setReadOnly(true);
        fechaDt.setReadOnly(true);
        montoTxt.setReadOnly(true);
        monedaCbx.setReadOnly(true);
        tasaCambioTxt.setReadOnly(true);

    }

    public void nuevaliquidacion() {
        limpiarTodo();
        serieTxt.focus();
    }

    public void insertTablaFactura() {
        try {
            if (liquidadorCbx.getValue() == null) {
                throw new IllegalArgumentException("Por favor elija a la persona liquidador.");
            }
            long numero = nuevaLiquidacion ? 0 : numeroLiquidacion(idLiquidacionTxt.getValue());
            if (fechaDt.getValue() == null) {
                throw new IllegalArgumentException("Ingrese la fecha del documento.");
            }
            String fecha = Utileria.getFechaYYYYMMDD_1(fechaDt.getValue());
            if (((SopdiUI) mainUI).esMesCerrado(empresaId, fecha)) {
                throw new IllegalArgumentException("La fecha del documento pertenece a un mes cerrado contablemente.");
            }
            if (!((SopdiUI) mainUI).esPrimerMesAbierto(empresaId, fecha)) {
                throw new IllegalArgumentException("El mes abierto a operaciones es: "
                        + ((SopdiUI) mainUI).primerMesAbierto(empresaId));
            }
            Object tipo = tipoDocumentoCbx.getValue();
            if (!"FACTURA".equals(tipo) && !"RECIBO CONTABLE".equals(tipo) && !"FORMULARIO".equals(tipo)) {
                throw new IllegalArgumentException("Seleccione FACTURA, RECIBO CONTABLE o FORMULARIO.");
            }
            if (serieTxt.getValue().trim().isEmpty() || numeroTxt.getValue().trim().isEmpty()) {
                throw new IllegalArgumentException("Ingrese la serie y el número del documento.");
            }
            if (proveedorCbx.getValue() == null || nitProveedorTxt.getValue().trim().isEmpty()) {
                throw new IllegalArgumentException("Ingrese el proveedor y su NIT.");
            }
            String moneda = (String) monedaCbx.getValue();
            if (!"QUETZALES".equals(moneda) && !"DOLARES".equals(moneda)) {
                throw new IllegalArgumentException("Seleccione la moneda del documento.");
            }
            BigDecimal tasa = importe(tasaCambioTxt).setScale(5, RoundingMode.HALF_UP);
            if (tasa.signum() <= 0 || ("QUETZALES".equals(moneda) && tasa.compareTo(BigDecimal.ONE) != 0)) {
                throw new IllegalArgumentException("Ingrese una tasa positiva; para quetzales debe ser 1.");
            }
            Factura factura = new Factura();
            factura.idEmpresa = empresaId;
            factura.idLiquidador = liquidadorCbx.getValue().toString();
            factura.nombreLiquidador = liquidadorCbx.getItemCaption(liquidadorCbx.getValue());
            factura.cuentaLiquidaciones = ((SopdiUI) mainUI).cuentasContablesDefault.getLiquidacionesCajaChicha();
            factura.tipoDocumento = tipo.toString();
            factura.fecha = java.sql.Date.valueOf(fecha);
            factura.nitProveedor = nitProveedorTxt.getValue().trim();
            String proveedor = proveedorCbx.getValue().toString();
            factura.idProveedor = proveedor.matches("[0-9]+") && proveedorCbx.containsId(proveedor) ? proveedor : "0";
            factura.nombreProveedor = "0".equals(factura.idProveedor) ? proveedor
                    : proveedorCbx.getItemCaption(proveedorCbx.getValue());
            factura.serie = serieTxt.getValue().trim().toUpperCase(java.util.Locale.ROOT);
            factura.numero = numeroTxt.getValue().trim().toUpperCase(java.util.Locale.ROOT);
            factura.moneda = moneda;
            factura.tipoCambio = tasa;
            factura.idCentroCosto = centroCostoCbx.getValue() == null ? null : centroCostoCbx.getValue().toString();
            factura.codigoCentroCosto = centroCostoCbx.getValue() == null ? ""
                    : centroCostoCbx.getItemCaption(centroCostoCbx.getValue());
            factura.idUsuario = ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            factura.nombreUsuario = ((SopdiUI) mainUI).sessionInformation.getStrUserFullName();
            factura.idFel = porContabilizarCbx.getValue() == null ? null : porContabilizarCbx.getValue().toString();
            ComboBox[] cuentas = {cuentaContable1Cbx, cuentaContable2Cbx, cuentaContable3Cbx, cuentaContable4Cbx};
            NumberField[] montos = {debe1Txt, debe2Txt, debe3Txt, debe4Txt};
            String[] idsCuentas = new String[cuentas.length];
            BigDecimal[] importes = new BigDecimal[montos.length];
            for (int indice = 0; indice < cuentas.length; indice++) {
                idsCuentas[indice] = cuentas[indice].getValue() == null ? null : cuentas[indice].getValue().toString();
                importes[indice] = importe(montos[indice]);
            }
            factura.renglones = crearRenglones(importe(montoTxt), tasa, factura.cuentaLiquidaciones, idsCuentas, importes,
                    ((SopdiUI) mainUI).cuentasContablesDefault.getRedondeo());
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            Date hoy = dateFormat.parse(new Utileria().getFecha());
            long dias = (hoy.getTime() - fechaDt.getValue().getTime()) / 86400000;
            if (dias > 30 && variableTemp.isEmpty()) {
                String token = ((SopdiUI) mainUI).sessionInformation.getStrUserToken();
                if (token == null || token.isEmpty()) {
                    ValidarTokenForm validarTokenForm = new ValidarTokenForm(false);
                    UI.getCurrent().addWindow(validarTokenForm);
                    validarTokenForm.center();
                    return;
                }
                variableTemp = token;
                ((SopdiUI) mainUI).sessionInformation.setStrUserToken("");
            }
            factura.token = variableTemp;
            Liquidacion guardada = guardarFactura(conexion(), factura, nuevaLiquidacion, numero,
                    liquidacionSeleccionada == null || seleccionManual);
            variableTemp = "";
            aplicarLiquidacion(guardada);
            Notification.show("Registro agregado con éxito.", Notification.Type.HUMANIZED_MESSAGE);
            cargarLiquidacionesAbiertas();
            aplicarLiquidacion(guardada);
            porContabilizarCbx.clear();
            llenarComboPorContabilizar();
            llenarTablaLiquidacion();
            actualizarVista();
            limpiarTodo();
            serieTxt.focus();
        } catch (Exception exception) {
            informarError("No se pudo guardar la factura.", exception);
            if (exception instanceof SQLException) {
                try {
                    String[] emailsTo = {"alerta@simpletecno.com"};
                    MyEmailMessanger eMail = new MyEmailMessanger();
                    eMail.postMail(emailsTo, "Error en SOPDI", "Error en base de datos: "
                            + getClass().getName() + " -->" + exception.getMessage());
                } catch (MessagingException mailException) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, mailException);
                }
            }
        }
    }

    public void llenarTablaLiquidacion() {
        container.removeAllItems();
        if (liquidacionSeleccionada == null) {
            footer.getCell(PROVEEDOR_PROPERTY).setText("0 facturas");
            return;
        }
        Liquidacion liquidacion = liquidacionSeleccionada;
        try (PreparedStatement statement = preparar(conexion(),
                "SELECT * FROM contabilidad_partida WHERE IdEmpresa = ? AND IdLiquidador = ?"
                        + " AND IdLiquidacion = ? AND IdNomenclatura = ? ORDER BY IdPartida",
                liquidacion.idEmpresa, liquidacion.idLiquidador, liquidacion.idLiquidacion,
                ((SopdiUI) mainUI).cuentasContablesDefault.getLiquidacionesCajaChicha());
                ResultSet registros = statement.executeQuery()) {
            while (registros.next()) {
                Object itemId = container.addItem();
                container.getContainerProperty(itemId, ID_PROPERTY).setValue(registros.getString("IdPartida"));
                container.getContainerProperty(itemId, CODIGO_PARTIDA_PROPERTY).setValue(registros.getString("CodigoPartida"));
                container.getContainerProperty(itemId, SERIE_PROPERTY).setValue(registros.getString("SerieDocumento"));
                container.getContainerProperty(itemId, NUMERO_PROPERTY).setValue(registros.getString("NumeroDocumento"));
                container.getContainerProperty(itemId, PROVEEDOR_PROPERTY).setValue(registros.getString("NombreProveedor"));
                container.getContainerProperty(itemId, FECHA_PROPERTY).setValue(registros.getString("Fecha"));
                container.getContainerProperty(itemId, MONTO_PROPERTY).setValue(numberFormat.format(registros.getBigDecimal("Haber")));
                container.getContainerProperty(itemId, MONEDA_PROPERTY).setValue(registros.getString("MonedaDocumento"));
                container.getContainerProperty(itemId, TIPOCAMBIO_PROPERTY).setValue(registros.getString("TipoCambio"));
                container.getContainerProperty(itemId, ELIMINAR_PROPERTY).setValue("Eliminar");
            }
        } catch (SQLException exception) {
            informarError("No se pudieron consultar las facturas de la liquidación.", exception);
        }
        footer.getCell(PROVEEDOR_PROPERTY).setText(container.size() + " facturas");
    }

    public void buscarProveedorPorNit() {
//        for (Iterator<?> i = proveedorCbx.getItemIds().iterator(); i.hasNext();) {
//            String id = (String) i.next();
//            Item item = proveedorCbx.getItem(id);
//
//            if (nitProveedorTxt.getValue().equals(String.valueOf(item.getItemProperty(NIT_PROPERTY).getValue()).trim())) {
//                proveedorCbx.select(id);
//                break;
//            }
//        }
    }

    public void llenarComboCuentaContable() {
        try (PreparedStatement statement = preparar(conexion(),
                "SELECT IdNomenclatura, NoCuenta, N5 FROM contabilidad_nomenclatura_empresa"
                        + " WHERE Estatus = 'HABILITADA' AND IdEmpresa = ? ORDER BY N5", empresaId);
                ResultSet registros = statement.executeQuery()) {
            while (registros.next()) {
                String id = registros.getString("IdNomenclatura");
                String descripcion = registros.getString("NoCuenta") + " " + registros.getString("N5");
                for (ComboBox cuenta : new ComboBox[]{cuentaContable1Cbx, cuentaContable2Cbx,
                        cuentaContable3Cbx, cuentaContable4Cbx}) {
                    cuenta.addItem(id);
                    cuenta.setItemCaption(id, descripcion);
                }
            }
        } catch (SQLException exception) {
            informarError("No se pudieron consultar las cuentas contables.", exception);
        }
    }

    public void cerrarLiquidacion() {
        if (liquidacionSeleccionada == null) {
            Notification.show("Seleccione una liquidación existente.", Notification.Type.WARNING_MESSAGE);
            return;
        }
        Liquidacion seleccionada = liquidacionSeleccionada;
        Connection connection = conexion();
        try {
            enTransaccion(connection, () -> {
                siguienteLiquidacion(connection, seleccionada.idEmpresa, true);
                Liquidacion liquidacion = buscarLiquidacion(connection, seleccionada.idEmpresa,
                        seleccionada.idLiquidador, seleccionada.idLiquidacion,
                        ((SopdiUI) mainUI).cuentasContablesDefault.getLiquidacionesCajaChicha(), true);
                try (PreparedStatement statement = preparar(connection,
                        "UPDATE contabilidad_partida SET Estatus = 'CERRADO'"
                                + " WHERE IdEmpresa = ? AND IdLiquidador = ? AND IdLiquidacion = ?",
                        liquidacion.idEmpresa, liquidacion.idLiquidador, liquidacion.idLiquidacion)) {
                    statement.executeUpdate();
                }
                return null;
            });
            actualizarVista();
            close();
            Notification.show("Liquidación cerrada con éxito.", Notification.Type.HUMANIZED_MESSAGE);
        } catch (SQLException | IllegalArgumentException exception) {
            informarError("No se pudo cerrar la liquidación.", exception);
        }
    }

    public void limpiarTodo() {
        porContabilizarCbx.clear();
        tipoDocumentoCbx.setEnabled(true);
        centroCostoCbx.setEnabled(true);
        fechaDt.setEnabled(true);
        proveedorCbx.setEnabled(true);
        proveedorCbx.setReadOnly(false);
        nitProveedorTxt.setEnabled(true);
        serieTxt.setEnabled(true);
        numeroTxt.setEnabled(true);
        montoTxt.setEnabled(true);
        monedaCbx.setEnabled(true);
        monedaCbx.setReadOnly(false);
        tasaCambioTxt.setReadOnly(false);

        serieTxt.setReadOnly(false);
        numeroTxt.setReadOnly(false);
        nitProveedorTxt.setReadOnly(false);
//        monedaCbx.setReadOnly(false);
        fechaDt.setReadOnly(false);
        montoTxt.setReadOnly(false);
//        tasaCambioTxt.setReadOnly(false);

        serieTxt.setValue("");
        numeroTxt.setValue("");
        nitProveedorTxt.setValue("");
        montoTxt.setValue(0.00);
        cuentaContable1Cbx.clear();
        cuentaContable2Cbx.clear();
        cuentaContable3Cbx.clear();
        cuentaContable4Cbx.clear();

        debe1Txt.setValue(0.00);
        debe2Txt.setValue(0.00);
        debe3Txt.setValue(0.00);
        debe4Txt.setValue(0.00);

        proveedorCbx.unselect(proveedorCbx.getValue());

    }

    public void llenarComboLiquidador() {
        try (PreparedStatement statement = preparar(conexion(),
                "SELECT IdProveedor, Nombre FROM proveedor_empresa"
                        + " WHERE EsLiquidador = 1 AND Inhabilitado = 0 AND IdEmpresa = ? ORDER BY Nombre", empresaId);
                ResultSet registros = statement.executeQuery()) {
            while (registros.next()) {
                String id = registros.getString("IdProveedor");
                liquidadorCbx.addItem(id);
                liquidadorCbx.setItemCaption(id, registros.getString("Nombre"));
            }
        } catch (SQLException exception) {
            informarError("No se pudieron consultar los liquidadores.", exception);
        }
    }

    public void llenarCamposFactura(String serie, String numero, String proveedor, String fecha, String codigoPartida) {
        cuentaContable1Cbx.clear();
        cuentaContable2Cbx.clear();
        cuentaContable3Cbx.clear();
        cuentaContable4Cbx.clear();

        debe1Txt.setValue(0.00);
        debe2Txt.setValue(0.00);
        debe3Txt.setValue(0.00);
        debe4Txt.setValue(0.00);

        if (liquidacionSeleccionada == null) {
            return;
        }
        Liquidacion liquidacion = liquidacionSeleccionada;
        try (PreparedStatement statement = preparar(conexion(),
                "SELECT * FROM contabilidad_partida WHERE IdEmpresa = ? AND IdLiquidador = ?"
                        + " AND IdLiquidacion = ? AND CodigoPartida = ? ORDER BY IdPartida",
                liquidacion.idEmpresa, liquidacion.idLiquidador, liquidacion.idLiquidacion, codigoPartida);
                ResultSet rsRecords1 = statement.executeQuery()) {
            int contador = 0;
            boolean encabezadoLeido = false;
            ComboBox[] cuentas = {cuentaContable1Cbx, cuentaContable2Cbx, cuentaContable3Cbx, cuentaContable4Cbx};
            NumberField[] montos = {debe1Txt, debe2Txt, debe3Txt, debe4Txt};
            while (rsRecords1.next()) { //  encontrado                
                if (!encabezadoLeido) {

                    serieTxt.setValue(rsRecords1.getString("SerieDocumento"));
                    numeroTxt.setValue(rsRecords1.getString("NumeroDocumento"));
                    nitProveedorTxt.setValue(rsRecords1.getString("NITProveedor"));
                    
                    if(rsRecords1.getObject("IdProveedor") == null || "0".equals(rsRecords1.getString("IdProveedor"))) {
                        proveedorCbx.select(0);
                        proveedorCbx.setValue(rsRecords1.getString("NombreProveedor"));
                    }
                    else {
                        proveedorCbx.select(rsRecords1.getString("IdProveedor"));
                    }
                    fechaDt.setValue(rsRecords1.getDate("Fecha"));
                    monedaCbx.setValue(rsRecords1.getString("MonedaDocumento"));
                    tasaCambioTxt.setValue(rsRecords1.getBigDecimal("TipoCambio").toPlainString());
                    encabezadoLeido = true;
                }

                if (rsRecords1.getString("IdNomenclatura").equals(
                        ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getLiquidacionesCajaChicha())) {
                    montoTxt.setValue(rsRecords1.getBigDecimal("Haber").toPlainString());
                    continue;
                }

                BigDecimal debe = rsRecords1.getBigDecimal("Debe");
                if (debe != null && debe.signum() > 0 && contador < cuentas.length) {
                    cuentas[contador].select(rsRecords1.getString("IdNomenclatura"));
                    montos[contador].setValue(debe.toPlainString());
                    contador++;
                }
            }

        } catch (Exception ex) {
            System.out.println("Error en llenar campos factura" + ex);
            ex.printStackTrace();
        }

    }

    public void eliminarRegistroTabla(RendererClickEvent event) {
        if (liquidacionSeleccionada == null) {
            Notification.show("Seleccione una liquidación existente.", Notification.Type.WARNING_MESSAGE);
            return;
        }
        Liquidacion seleccionada = liquidacionSeleccionada;
        String numero = container.getContainerProperty(event.getItemId(), NUMERO_PROPERTY).getValue().toString();
        String serie = container.getContainerProperty(event.getItemId(), SERIE_PROPERTY).getValue().toString();
        String codigoPartida = container.getContainerProperty(event.getItemId(), CODIGO_PARTIDA_PROPERTY).getValue().toString();
        ConfirmDialog.show(UI.getCurrent(), "Confirme:", "¿Está seguro de eliminar la factura?",
                "SI", "NO", dialog -> {
                    if (!dialog.isConfirmed()) {
                        return;
                    }
                    try {
                        eliminarFactura(conexion(), seleccionada, codigoPartida, serie, numero,
                                ((SopdiUI) mainUI).cuentasContablesDefault.getLiquidacionesCajaChicha());
                        llenarTablaLiquidacion();
                        limpiarTodo();
                        llenarComboPorContabilizar();
                        actualizarVista();
                        Notification.show("Registro eliminado exitosamente.", Notification.Type.TRAY_NOTIFICATION);
                    } catch (SQLException | IllegalArgumentException exception) {
                        informarError("No se pudo eliminar la factura.", exception);
                    }
                });
    }

    static void eliminarFactura(Connection connection, Liquidacion seleccionada, String codigoPartida,
            String serie, String numero, String cuentaLiquidaciones) throws SQLException {
        enTransaccion(connection, () -> {
            siguienteLiquidacion(connection, seleccionada.idEmpresa, true);
            Liquidacion liquidacion = buscarLiquidacion(connection, seleccionada.idEmpresa, seleccionada.idLiquidador,
                    seleccionada.idLiquidacion, cuentaLiquidaciones, true);
            try (PreparedStatement statement = preparar(connection,
                    "DELETE FROM contabilidad_partida WHERE IdEmpresa = ? AND IdLiquidador = ?"
                            + " AND IdLiquidacion = ? AND CodigoPartida = ? AND SerieDocumento = ? AND NumeroDocumento = ?",
                    liquidacion.idEmpresa, liquidacion.idLiquidador, liquidacion.idLiquidacion, codigoPartida, serie, numero)) {
                if (statement.executeUpdate() == 0) {
                    throw new IllegalArgumentException("La factura ya no pertenece a la liquidación seleccionada.");
                }
            }
            try (PreparedStatement statement = preparar(connection,
                    "UPDATE documentos_fel_sat SET Contabilizada = 'N', IdLiquidacion = 0"
                            + " WHERE IdEmpresa = ? AND IdLiquidador = ? AND IdLiquidacion = ? AND Serie = ? AND Numero = ?",
                    liquidacion.idEmpresa, liquidacion.idLiquidador, liquidacion.idLiquidacion, serie, numero)) {
                statement.executeUpdate();
            }
            return null;
        });
    }

}
