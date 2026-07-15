package com.simpletecno.sopdi.tesoreria;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.ui.NumberField;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Vista de Movimientos Especiales de Banco.
 *
 * Clonada de {@link AutorizarPagosEspecialesView}, pero en lugar de aplicar pagos
 * registra <b>Créditos (ingresos)</b> y <b>Débitos (egresos)</b> directos a las
 * cuentas contables bancarias.
 *
 * Al igual que en la vista original se muestra el grid con las cuentas contables
 * de bancos (cuentas por defecto) y se elige <b>una sola</b> cuenta para realizar
 * el movimiento. La moneda del movimiento se deriva de la cuenta bancaria elegida.
 *
 * La partida contable generada tiene dos líneas:
 *   - CRÉDITO (INGRESO): DEBE en la cuenta bancaria, HABER en la cuenta de ingreso.
 *   - DÉBITO  (EGRESO) : HABER en la cuenta bancaria, DEBE en la cuenta de gasto.
 *
 * Los tipos de operación disponibles se inspiran en {@link IngresosVariadosForm}
 * (préstamos, enganches de cliente, intereses devengados, cobro servicio banco, ...)
 * y solo sirven como atajo para precargar la cuenta contable y la dirección del
 * movimiento; el usuario puede modificar ambos manualmente.
 */
public class MovimientosEspecialesBancoView extends VerticalLayout implements View {

    // ── Tipos de movimiento ───────────────────────────────────────────────────
    public static final String CREDITO_INGRESO = "CREDITO (INGRESO)";
    public static final String DEBITO_EGRESO   = "DEBITO (EGRESO)";

    /**
     * Descriptor de un tipo de operación: dirección del movimiento (crédito=ingreso)
     * y el "getter" de la cuenta contable por defecto (contraparte de ingreso o gasto).
     * La cuenta se resuelve en tiempo de ejecución contra {@code cuentasContablesDefault}.
     */
    private static class TipoOperacion {
        final boolean esCredito;   // true = ingreso (crédito), false = egreso (débito)
        final String cuentaDefault; // IdNomenclatura por defecto ("" = manual)

        TipoOperacion(boolean esCredito, String cuentaDefault) {
            this.esCredito = esCredito;
            this.cuentaDefault = cuentaDefault == null ? "" : cuentaDefault;
        }
    }

    /** Operaciones disponibles en el combo, en orden de aparición. */
    Map<String, TipoOperacion> operaciones = new LinkedHashMap<>();

    // ── Propiedades del grid de cuentas bancarias ─────────────────────────────
    static final String ID_CUENTABANCO_PROPERTY = "IdCuentaBanco";
    static final String CUENTA_BANCARIA_PROPERTY = "Cuenta Bancaria";
    static final String BANCO_PROPERTY = "Banco";
    static final String MONEDA_PROPERTY = "Moneda";
    static final String SALDO_CONTABLE_PROPERTY = "Saldo contable";
    static final String ID_NOMENCLATURA_PROPERTY = "IdNomenclatura";

    // ── Propiedades del grid de previsualización de la partida ────────────────
    static final String PREVIEW_CUENTA_PROPERTY = "Cuenta contable";
    static final String PREVIEW_DEBE_PROPERTY = "Debe";
    static final String PREVIEW_HABER_PROPERTY = "Haber";

    VerticalLayout mainLayout;

    IndexedContainer cuentasBancosContainer = new IndexedContainer();
    Grid cuentasBancosGrid;

    IndexedContainer partidaContainer = new IndexedContainer();
    Grid partidaGrid;

    // ── Controles del formulario ──────────────────────────────────────────────
    ComboBox  tipoOperacionCbx;
    ComboBox  tipoMovimientoCbx;
    ComboBox  cuentaContableCbx;
    ComboBox  proveedorCbx;
    ComboBox  medioCbx;
    TextField documentoTxt;
    DateField fechaDt;
    NumberField tasaCambioTxt;
    NumberField montoTxt;
    TextField descripcionTxt;

    boolean darkModeActive = false;

    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");

    UI mainUI;
    Statement stQuery;
    ResultSet rsRecords;
    String queryString;

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public MovimientosEspecialesBancoView() {
        this.mainUI = UI.getCurrent();
        setWidth("100%");
        setHeightUndefined();
        addStyleName("apc-view");

        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setWidth("100%");
        addComponent(mainLayout);

        cargarOperaciones();

        createGridCuentasBancos();
        crearLayoutCampos();
        crearGridPartida();
        crearBotones();

        llenarGridBancos();
        actualizarPreviewPartida();
    }

    /**
     * Define las operaciones disponibles y su dirección/cuenta por defecto.
     * Las cuentas se toman de la configuración de cuentas contables por defecto.
     */
    private void cargarOperaciones() {
        com.simpletecno.sopdi.configuracion.CuentasContablesDefault cc = ((SopdiUI) mainUI).cuentasContablesDefault;

        // INGRESOS (crédito al banco)
        operaciones.put("PRESTAMOS BANCARIOS",   new TipoOperacion(true,  cc.getPrestamos()));
        operaciones.put("PRESTAMOS DE TERCEROS", new TipoOperacion(true,  cc.getPrestamos()));
        operaciones.put("ENGANCHE CLIENTE",      new TipoOperacion(true,  cc.getEnganches()));
        operaciones.put("ANTICIPO CLIENTE",      new TipoOperacion(true,  cc.getAnticiposClientes()));
        operaciones.put("INTERESES DEVENGADOS",  new TipoOperacion(true,  cc.getInteresesDevengados()));
        operaciones.put("OTRO INGRESO",          new TipoOperacion(true,  ""));

        // EGRESOS (débito al banco)
        operaciones.put("COBRO SERVICIO BANCO",  new TipoOperacion(false, cc.getServiciosBancos()));
        operaciones.put("INTERESES SOBRE PRESTAMO", new TipoOperacion(false, cc.getInteresesPrestamo()));
        operaciones.put("CHEQUE DEVUELTO",       new TipoOperacion(false, cc.getChequesDevueltos()));
        operaciones.put("OTRO EGRESO",           new TipoOperacion(false, ""));
    }

    // =========================================================================
    //  GRID DE CUENTAS BANCARIAS
    // =========================================================================

    public void createGridCuentasBancos() {
        cuentasBancosContainer.addContainerProperty(ID_CUENTABANCO_PROPERTY, String.class, "");
        cuentasBancosContainer.addContainerProperty(CUENTA_BANCARIA_PROPERTY, String.class, "");
        cuentasBancosContainer.addContainerProperty(BANCO_PROPERTY, String.class, "");
        cuentasBancosContainer.addContainerProperty(MONEDA_PROPERTY, String.class, "");
        cuentasBancosContainer.addContainerProperty(SALDO_CONTABLE_PROPERTY, String.class, "0.00");
        cuentasBancosContainer.addContainerProperty(ID_NOMENCLATURA_PROPERTY, String.class, "");

        cuentasBancosGrid = new Grid("Cuenta y banco", cuentasBancosContainer);
        cuentasBancosGrid.setImmediate(true);
        // Solo se puede elegir UNA cuenta bancaria para el movimiento
        cuentasBancosGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        cuentasBancosGrid.setDescription("Seleccione una sola cuenta y banco para el movimiento.");
        cuentasBancosGrid.setHeightMode(HeightMode.ROW);
        cuentasBancosGrid.setHeightByRows(4);
        cuentasBancosGrid.setWidth("100%");

        cuentasBancosGrid.getColumn(ID_CUENTABANCO_PROPERTY).setHidable(true).setHidden(true);
        cuentasBancosGrid.getColumn(ID_NOMENCLATURA_PROPERTY).setHidable(true).setHidden(true);

        cuentasBancosGrid.getColumn(CUENTA_BANCARIA_PROPERTY).setExpandRatio(1);
        cuentasBancosGrid.getColumn(BANCO_PROPERTY).setExpandRatio(2);
        cuentasBancosGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if (SALDO_CONTABLE_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            }
            return null;
        });

        // Al cambiar de cuenta bancaria se recalcula la previsualización (moneda / tasa)
        cuentasBancosGrid.addSelectionListener(event -> {
            Object itemId = cuentasBancosGrid.getSelectedRow();
            if (itemId != null && "DOLARES".equalsIgnoreCase(monedaCuenta(itemId))) {
                tasaCambioTxt.setValue(((SopdiUI) mainUI).getTasaCambioDelDia(Utileria.getFechaYYYYMMDD_1(fechaDt.getValue())));
            } else {
                tasaCambioTxt.setValue(1.00);
            }
            actualizarPreviewPartida();
        });

        mainLayout.addComponent(cuentasBancosGrid);
        mainLayout.setComponentAlignment(cuentasBancosGrid, Alignment.TOP_CENTER);
    }

    public void llenarGridBancos() {

        cuentasBancosContainer.removeAllItems();

        queryString = "  SELECT *, cuen.N5, emp.Empresa, prov.Nombre ";
        queryString += " FROM contabilidad_cuentas_bancos AS ban";
        queryString += " INNER JOIN contabilidad_nomenclatura_empresa AS cuen";
        queryString += " ON ban.IdNomenclatura = cuen.IdNomenclatura";
        queryString += " INNER JOIN contabilidad_empresa AS emp ON ban.IdEmpresa = emp.IdEmpresa";
        queryString += " INNER JOIN proveedor_empresa AS prov ON ban.IdProveedor = prov.IdProveedor";
        queryString += " WHERE ban.IdEmpresa = " + empresaId;
        queryString += " AND cuen.IdEmpresa = " + empresaId;
        queryString += " AND prov.IdEmpresa = " + empresaId;
        queryString += " ORDER BY ban.IdEmpresa, ban.IdNomenclatura";

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) {
                Object itemId = cuentasBancosContainer.addItem();
                cuentasBancosContainer.getContainerProperty(itemId, ID_CUENTABANCO_PROPERTY).setValue(rsRecords.getString("IdCuentaBanco"));
                cuentasBancosContainer.getContainerProperty(itemId, CUENTA_BANCARIA_PROPERTY).setValue(rsRecords.getString("NoCuenta"));
                cuentasBancosContainer.getContainerProperty(itemId, BANCO_PROPERTY).setValue(rsRecords.getString("prov.Nombre"));
                cuentasBancosContainer.getContainerProperty(itemId, MONEDA_PROPERTY).setValue(rsRecords.getString("Moneda"));
                cuentasBancosContainer.getContainerProperty(itemId, SALDO_CONTABLE_PROPERTY).setValue(numberFormat.format(rsRecords.getDouble("Saldo")));
                cuentasBancosContainer.getContainerProperty(itemId, ID_NOMENCLATURA_PROPERTY).setValue(rsRecords.getString("ban.IdNomenclatura"));
            }
        } catch (Exception ex) {
            System.out.println("Error al listar cuentas bancarias :" + ex);
            ex.printStackTrace();
        }
    }

    // =========================================================================
    //  LAYOUT DE CAMPOS
    // =========================================================================

    private void crearLayoutCampos() {

        tipoOperacionCbx = new ComboBox("Tipo de operación :");
        tipoOperacionCbx.setWidth("100%");
        tipoOperacionCbx.setNullSelectionAllowed(false);
        tipoOperacionCbx.setNewItemsAllowed(false);
        tipoOperacionCbx.setTextInputAllowed(false);
        tipoOperacionCbx.setImmediate(true);
        for (String op : operaciones.keySet()) {
            tipoOperacionCbx.addItem(op);
        }
        tipoOperacionCbx.addValueChangeListener(e -> aplicarOperacion());

        tipoMovimientoCbx = new ComboBox("Movimiento :");
        tipoMovimientoCbx.setWidth("100%");
        tipoMovimientoCbx.setNullSelectionAllowed(false);
        tipoMovimientoCbx.setNewItemsAllowed(false);
        tipoMovimientoCbx.setTextInputAllowed(false);
        tipoMovimientoCbx.setImmediate(true);
        tipoMovimientoCbx.addItem(CREDITO_INGRESO);
        tipoMovimientoCbx.addItem(DEBITO_EGRESO);
        tipoMovimientoCbx.select(CREDITO_INGRESO);
        tipoMovimientoCbx.addValueChangeListener(e -> actualizarPreviewPartida());

        cuentaContableCbx = new ComboBox("Cuenta contable (ingreso / gasto) :");
        cuentaContableCbx.setWidth("100%");
        cuentaContableCbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContableCbx.setInvalidAllowed(false);
        cuentaContableCbx.setNewItemsAllowed(false);
        cuentaContableCbx.setImmediate(true);
        cuentaContableCbx.addValueChangeListener(e -> actualizarPreviewPartida());
        llenarComboCuentaContable();

        proveedorCbx = new ComboBox("Proveedor / Cliente :");
        proveedorCbx.setWidth("100%");
        proveedorCbx.setFilteringMode(FilteringMode.CONTAINS);
        proveedorCbx.setNewItemsAllowed(false);
        proveedorCbx.setInvalidAllowed(false);
        proveedorCbx.setNullSelectionAllowed(false);
        proveedorCbx.setRequired(true);
        proveedorCbx.setRequiredError("Debe seleccionar un proveedor / cliente");
        proveedorCbx.setImmediate(true);
        llenarComboProveedor();

        medioCbx = new ComboBox("Medio :");
        medioCbx.setWidth("100%");
        medioCbx.addItem("DEPOSITO");
        medioCbx.addItem("NOTA DE CREDITO");
        medioCbx.addItem("NOTA DE DEBITO");
        medioCbx.addItem("CHEQUE");
        medioCbx.setNullSelectionAllowed(false);
        medioCbx.select("DEPOSITO");

        documentoTxt = new TextField("# Documento");
        documentoTxt.setWidth("100%");

        fechaDt = new DateField("Fecha");
        fechaDt.setDateFormat("dd/MM/yyyy");
        fechaDt.setWidth("100%");
        fechaDt.setValue(new java.util.Date());

        tasaCambioTxt = new NumberField("T. Cambio");
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
        tasaCambioTxt.addValueChangeListener(e -> actualizarPreviewPartida());

        montoTxt = new NumberField("Monto");
        montoTxt.setValidationVisible(false);
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
        montoTxt.addValueChangeListener(e -> actualizarPreviewPartida());

        descripcionTxt = new TextField("Descripción del movimiento");
        descripcionTxt.setWidth("100%");

        HorizontalLayout fila1 = new HorizontalLayout();
        fila1.setSpacing(true);
        fila1.setWidth("100%");
        fila1.addComponents(tipoOperacionCbx, tipoMovimientoCbx, cuentaContableCbx, proveedorCbx);
        fila1.setExpandRatio(tipoOperacionCbx, 1f);
        fila1.setExpandRatio(tipoMovimientoCbx, 1f);
        fila1.setExpandRatio(cuentaContableCbx, 2f);
        fila1.setExpandRatio(proveedorCbx, 2f);

        HorizontalLayout fila2 = new HorizontalLayout();
        fila2.setSpacing(true);
        fila2.setWidth("100%");
        fila2.addComponents(medioCbx, documentoTxt, fechaDt, tasaCambioTxt, montoTxt, descripcionTxt);
        for (int i = 0; i < fila2.getComponentCount(); i++) {
            fila2.setComponentAlignment(fila2.getComponent(i), Alignment.BOTTOM_LEFT);
        }
        fila2.setExpandRatio(medioCbx, 1f);
        fila2.setExpandRatio(documentoTxt, 1f);
        fila2.setExpandRatio(fechaDt, 1f);
        fila2.setExpandRatio(tasaCambioTxt, 0.7f);
        fila2.setExpandRatio(montoTxt, 1f);
        fila2.setExpandRatio(descripcionTxt, 2f);

        mainLayout.addComponent(fila1);
        mainLayout.setComponentAlignment(fila1, Alignment.TOP_CENTER);
        mainLayout.addComponent(fila2);
        mainLayout.setComponentAlignment(fila2, Alignment.TOP_CENTER);
    }

    /** Al elegir un tipo de operación, precarga dirección y cuenta contable. */
    private void aplicarOperacion() {
        String op = nvlC(tipoOperacionCbx.getValue());
        TipoOperacion desc = operaciones.get(op);
        if (desc == null) return;

        tipoMovimientoCbx.select(desc.esCredito ? CREDITO_INGRESO : DEBITO_EGRESO);

        if (!desc.cuentaDefault.isEmpty() && cuentaContableCbx.containsId(desc.cuentaDefault)) {
            cuentaContableCbx.select(desc.cuentaDefault);
        }
        if (descripcionTxt.getValue() == null || descripcionTxt.getValue().trim().isEmpty()) {
            descripcionTxt.setValue(op);
        }
        actualizarPreviewPartida();
    }

    private void llenarComboProveedor() {
        queryString = " SELECT * FROM proveedor_empresa ";
        queryString += " WHERE Inhabilitado = 0 ";
        queryString += " AND IdEmpresa = " + empresaId;
        queryString += " ORDER BY Nombre ";

        proveedorCbx.removeAllItems();
        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);
            while (rsRecords.next()) {
                proveedorCbx.addItem(rsRecords.getString("IDProveedor"));
                proveedorCbx.setItemCaption(rsRecords.getString("IDProveedor"),
                        "(" + rsRecords.getString("IDProveedor") + ") " + rsRecords.getString("Nombre"));
            }
        } catch (Exception ex1) {
            System.out.println("Error al listar Proveedores/Clientes " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    private void llenarComboCuentaContable() {
        queryString = " SELECT * FROM contabilidad_nomenclatura_empresa";
        queryString += " WHERE IdEmpresa = " + empresaId;
        queryString += " AND Estatus = 'HABILITADA'";
        queryString += " ORDER BY N5";

        cuentaContableCbx.removeAllItems();
        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);
            while (rsRecords.next()) {
                cuentaContableCbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContableCbx.setItemCaption(rsRecords.getString("IdNomenclatura"),
                        rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));
            }
        } catch (Exception ex1) {
            System.out.println("Error al llenar combo cuentas contables: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    // =========================================================================
    //  GRID DE PREVISUALIZACIÓN DE LA PARTIDA
    // =========================================================================

    private void crearGridPartida() {
        partidaContainer.addContainerProperty(PREVIEW_CUENTA_PROPERTY, String.class, "");
        partidaContainer.addContainerProperty(PREVIEW_DEBE_PROPERTY, String.class, "0.00");
        partidaContainer.addContainerProperty(PREVIEW_HABER_PROPERTY, String.class, "0.00");

        partidaGrid = new Grid("Partida contable (previsualización, valores en quetzales)", partidaContainer);
        partidaGrid.setWidth("100%");
        partidaGrid.setImmediate(true);
        partidaGrid.setSelectionMode(Grid.SelectionMode.NONE);
        partidaGrid.setHeightMode(HeightMode.ROW);
        partidaGrid.setHeightByRows(2);

        partidaGrid.getColumn(PREVIEW_CUENTA_PROPERTY).setExpandRatio(3);
        partidaGrid.getColumn(PREVIEW_DEBE_PROPERTY).setExpandRatio(1);
        partidaGrid.getColumn(PREVIEW_HABER_PROPERTY).setExpandRatio(1);
        partidaGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if (PREVIEW_DEBE_PROPERTY.equals(cellReference.getPropertyId())
                    || PREVIEW_HABER_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            }
            return null;
        });

        mainLayout.addComponent(partidaGrid);
        mainLayout.setComponentAlignment(partidaGrid, Alignment.TOP_CENTER);
    }

    /** Recalcula las dos líneas de la partida contable a partir del estado actual. */
    private void actualizarPreviewPartida() {
        partidaContainer.removeAllItems();

        Object bancoItemId = cuentasBancosGrid.getSelectedRow();
        boolean esCredito = CREDITO_INGRESO.equals(tipoMovimientoCbx.getValue());
        double monto = montoTxt.getDoubleValueDoNotThrow();
        double tasa = tasaCambioTxt.getDoubleValueDoNotThrow();
        if (tasa <= 0.00) tasa = 1.00;
        String moneda = bancoItemId == null ? "QUETZALES" : monedaCuenta(bancoItemId);
        double montoQ = "DOLARES".equalsIgnoreCase(moneda) ? monto * tasa : monto;

        String captionBanco = bancoItemId == null ? "(Seleccione cuenta bancaria)"
                : nvlC(cuentasBancosContainer.getContainerProperty(bancoItemId, CUENTA_BANCARIA_PROPERTY).getValue())
                  + " - " + nvlC(cuentasBancosContainer.getContainerProperty(bancoItemId, BANCO_PROPERTY).getValue());
        String captionCuenta = cuentaContableCbx.getValue() == null ? "(Seleccione cuenta de ingreso / gasto)"
                : cuentaContableCbx.getItemCaption(cuentaContableCbx.getValue());

        // Línea banco
        Object l1 = partidaContainer.addItem();
        partidaContainer.getContainerProperty(l1, PREVIEW_CUENTA_PROPERTY).setValue("BANCO: " + captionBanco);
        partidaContainer.getContainerProperty(l1, PREVIEW_DEBE_PROPERTY).setValue(numberFormat.format(esCredito ? montoQ : 0.00));
        partidaContainer.getContainerProperty(l1, PREVIEW_HABER_PROPERTY).setValue(numberFormat.format(esCredito ? 0.00 : montoQ));

        // Línea contraparte (ingreso o gasto)
        Object l2 = partidaContainer.addItem();
        partidaContainer.getContainerProperty(l2, PREVIEW_CUENTA_PROPERTY).setValue((esCredito ? "INGRESO: " : "GASTO: ") + captionCuenta);
        partidaContainer.getContainerProperty(l2, PREVIEW_DEBE_PROPERTY).setValue(numberFormat.format(esCredito ? 0.00 : montoQ));
        partidaContainer.getContainerProperty(l2, PREVIEW_HABER_PROPERTY).setValue(numberFormat.format(esCredito ? montoQ : 0.00));
    }

    // =========================================================================
    //  BOTONES
    // =========================================================================

    private void crearBotones() {
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setMargin(new MarginInfo(false, true, false, true));
        buttonsLayout.setSpacing(true);
        buttonsLayout.setWidth("100%");

        Button btnClear = new Button("Limpiar");
        btnClear.setIcon(FontAwesome.ERASER);
        btnClear.setWidth("10em");
        btnClear.addClickListener(event -> limpiar());

        Button btnAplicar = new Button("Aplicar movimiento");
        btnAplicar.addStyleName(ValoTheme.BUTTON_PRIMARY);
        btnAplicar.setIcon(FontAwesome.CHECK_SQUARE_O);
        btnAplicar.setWidth("15em");
        btnAplicar.addClickListener(event -> aplicarMovimiento());

        Button btnRevisar = new Button("Revisar movimientos");
        btnRevisar.setIcon(FontAwesome.LIST_ALT);
        btnRevisar.setWidth("15em");
        btnRevisar.setDescription("Consultar los movimientos de banco registrados.");
        btnRevisar.addClickListener(event -> {
            RevisarMovimientosBancoForm form = new RevisarMovimientosBancoForm();
            UI.getCurrent().addWindow(form);
            form.center();
        });

        Button btnTema = new Button("Modo oscuro", FontAwesome.MOON_O);
        btnTema.addStyleName("apc-theme-toggle");
        btnTema.setDescription("Cambiar entre modo claro y modo oscuro");
        btnTema.addClickListener(event -> {
            darkModeActive = !darkModeActive;
            if (darkModeActive) {
                MovimientosEspecialesBancoView.this.addStyleName("apc-dark");
                btnTema.setCaption("Modo claro");
                btnTema.setIcon(FontAwesome.SUN_O);
            } else {
                MovimientosEspecialesBancoView.this.removeStyleName("apc-dark");
                btnTema.setCaption("Modo oscuro");
                btnTema.setIcon(FontAwesome.MOON_O);
            }
        });

        buttonsLayout.addComponents(btnClear, btnRevisar, btnAplicar, btnTema);
        buttonsLayout.setComponentAlignment(btnClear, Alignment.TOP_LEFT);
        buttonsLayout.setComponentAlignment(btnRevisar, Alignment.TOP_RIGHT);
        buttonsLayout.setComponentAlignment(btnAplicar, Alignment.TOP_RIGHT);
        buttonsLayout.setComponentAlignment(btnTema, Alignment.TOP_RIGHT);
        mainLayout.addComponent(buttonsLayout);
        mainLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);
    }

    private void limpiar() {
        tipoOperacionCbx.select(null);
        tipoMovimientoCbx.select(CREDITO_INGRESO);
        cuentaContableCbx.select(null);
        proveedorCbx.select(null);
        medioCbx.select("DEPOSITO");
        documentoTxt.setValue("");
        montoTxt.setValue(0d);
        tasaCambioTxt.setValue(1.00);
        descripcionTxt.setValue("");
        cuentasBancosGrid.deselectAll();
        actualizarPreviewPartida();
    }

    // =========================================================================
    //  APLICAR MOVIMIENTO
    // =========================================================================

    /**
     * Valida y genera la partida contable del movimiento de banco. La partida tiene
     * dos líneas (banco + cuenta de ingreso/gasto) y se inserta en una transacción.
     */
    private void aplicarMovimiento() {

        Object bancoItemId = cuentasBancosGrid.getSelectedRow();
        if (bancoItemId == null) {
            Notification.show("Seleccione una cuenta bancaria.", Notification.Type.WARNING_MESSAGE);
            return;
        }
        if (cuentaContableCbx.getValue() == null) {
            Notification.show("Seleccione la cuenta contable de ingreso / gasto.", Notification.Type.WARNING_MESSAGE);
            cuentaContableCbx.focus();
            return;
        }
        if (proveedorCbx.getValue() == null) {
            Notification.show("Seleccione un proveedor / cliente.", Notification.Type.WARNING_MESSAGE);
            proveedorCbx.focus();
            return;
        }
        double monto = montoTxt.getDoubleValueDoNotThrow();
        if (monto <= 0.00) {
            Notification.show("Ingrese un monto mayor a cero.", Notification.Type.WARNING_MESSAGE);
            montoTxt.focus();
            return;
        }

        String fecha = Utileria.getFechaYYYYMMDD_1(fechaDt.getValue());
        if (((SopdiUI) mainUI).esMesCerrado(empresaId, fecha)) {
            Notification.show("La fecha del documento pertenece a un mes ya cerrado contablemente, revise!", Notification.Type.WARNING_MESSAGE);
            fechaDt.focus();
            return;
        }

        boolean esCredito = CREDITO_INGRESO.equals(tipoMovimientoCbx.getValue());
        String moneda = monedaCuenta(bancoItemId);
        double tasa = tasaCambioTxt.getDoubleValueDoNotThrow();
        if ("DOLARES".equalsIgnoreCase(moneda)) {
            if (tasa <= 1.00) {
                Notification.show("Movimiento en DOLARES: ingrese un tipo de cambio válido.", Notification.Type.WARNING_MESSAGE);
                tasaCambioTxt.focus();
                return;
            }
        } else {
            tasa = 1.00;
        }
        double montoQ = "DOLARES".equalsIgnoreCase(moneda) ? monto * tasa : monto;

        String cuentaBanco = nvlC(cuentasBancosContainer.getContainerProperty(bancoItemId, ID_NOMENCLATURA_PROPERTY).getValue());
        if (cuentaBanco.isEmpty()) {
            Notification.show("La cuenta bancaria seleccionada no tiene nomenclatura contable asociada.", Notification.Type.WARNING_MESSAGE);
            return;
        }
        String cuentaContrapartida = String.valueOf(cuentaContableCbx.getValue());

        String idProveedor = String.valueOf(proveedorCbx.getValue());
        String nombreProveedor = proveedorCbx.getItemCaption(proveedorCbx.getValue()).replace("'", "");
        String medio = nvlC(medioCbx.getValue());
        String noDocumento = nvlC(documentoTxt.getValue()).replace("'", "");
        String tipoOperacion = nvlC(tipoOperacionCbx.getValue());
        String descripcion = ((esCredito ? "INGRESO A BANCO POR " : "EGRESO DE BANCO POR ")
                + (tipoOperacion.isEmpty() ? "" : tipoOperacion + " ")
                + nvlC(descripcionTxt.getValue())).replace("'", "").trim();

        final String COLS =
            " (IdEmpresa, CodigoPartida, CodigoCC, TipoDocumento, IdNomenclatura, " +
            "  SerieDocumento, NumeroDocumento, Fecha, MonedaDocumento, MontoDocumento," +
            "  Debe, Haber, TipoCambio, DebeQuetzales, HaberQuetzales, Estatus," +
            "  Descripcion, TipoDoca, NoDoca, IdProveedor, NombreProveedor, Nombrecheque," +
            "  CreadoPor, FechaYHoraCreado) VALUES ";

        try {
            ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().setAutoCommit(false);
            Statement st = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            // Tipo de folio: 5 = ingresos a banco, 3 = egresos de banco
            int tipoFolio = esCredito ? 5 : 3;
            String codigoPartida[] = Utileria.nextCodigosPartida(
                    ((SopdiUI) mainUI).databaseProvider.getCurrentConnection(), empresaId, fechaDt.getValue(), tipoFolio, 1);

            String usuario = ((SopdiUI) mainUI).sessionInformation.getStrUserId();

            StringBuilder sql = new StringBuilder("INSERT INTO contabilidad_partida " + COLS);

            // ── Línea 1: cuenta bancaria ─────────────────────────────────────
            //    Crédito (ingreso): DEBE banco  |  Débito (egreso): HABER banco
            sql.append(lineaPartida(
                    codigoPartida[0], "",                // CodigoCC vacío para la línea de banco
                    medio, cuentaBanco, noDocumento, moneda, monto,
                    esCredito ? monto  : 0.00,        // Debe
                    esCredito ? 0.00   : monto,       // Haber
                    tasa,
                    esCredito ? montoQ : 0.00,        // DebeQuetzales
                    esCredito ? 0.00   : montoQ,      // HaberQuetzales
                    descripcion, tipoOperacion, idProveedor, nombreProveedor, usuario));
            sql.append(",");

            // ── Línea 2: cuenta de ingreso / gasto ───────────────────────────
            //    Crédito (ingreso): HABER ingreso  |  Débito (egreso): DEBE gasto
            sql.append(lineaPartida(
                    codigoPartida[0], codigoPartida[0],     // CodigoCC = partida para la contraparte
                    medio, cuentaContrapartida, noDocumento, moneda, monto,
                    esCredito ? 0.00   : monto,       // Debe
                    esCredito ? monto  : 0.00,        // Haber
                    tasa,
                    esCredito ? 0.00   : montoQ,      // DebeQuetzales
                    esCredito ? montoQ : 0.00,        // HaberQuetzales
                    descripcion, tipoOperacion, idProveedor, nombreProveedor, usuario));

            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "INSERT movimiento especial banco : {0}", sql.toString());
            st.executeUpdate(sql.toString());

            ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().commit();
            ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().setAutoCommit(true);

            Notification notif = new Notification("Movimiento de banco aplicado y partida contable generada.", Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(2000);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.CHECK);
            notif.show(Page.getCurrent());

            limpiar();
            llenarGridBancos();

        } catch (Exception ex) {
            try {
                ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().rollback();
                ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().setAutoCommit(true);
            } catch (SQLException rollbackEx) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error en rollback", rollbackEx);
            }
            Notification notif = new Notification("Error al aplicar movimiento: " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            notif.setDelayMsec(3000);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.WARNING);
            notif.show(Page.getCurrent());
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error en aplicarMovimiento", ex);
        }
    }

    /** Construye una línea "(...)" del INSERT de contabilidad_partida. */
    private String lineaPartida(String codigoPartida, String codigoCC, String tipoDocumento,
                                String idNomenclatura, String noDocumento, String moneda, double monto,
                                double debe, double haber, double tasa, double debeQ, double haberQ,
                                String descripcion, String tipoOperacion, String idProveedor,
                                String nombreProveedor, String usuario) {
        StringBuilder s = new StringBuilder();
        s.append("(");
        s.append(empresaId);
        s.append(",'").append(codigoPartida).append("'");
        s.append(",'").append(codigoCC).append("'");
        s.append(",'").append(tipoDocumento).append("'");
        s.append(",").append(idNomenclatura);
        s.append(",''");                                  // SerieDocumento
        s.append(",'").append(noDocumento).append("'");
        s.append(",current_date");
        s.append(",'").append(moneda).append("'");
        s.append(",").append(monto);
        s.append(",").append(debe);
        s.append(",").append(haber);
        s.append(",").append(tasa);
        s.append(",").append(debeQ);
        s.append(",").append(haberQ);
        s.append(",'INGRESADO'");
        s.append(",'").append(descripcion).append("'");
        s.append(",'").append(tipoOperacion.replace("'", "")).append("'");
        s.append(",'").append(noDocumento).append("'");
        s.append(",").append(idProveedor);
        s.append(",'").append(nombreProveedor).append("'");
        s.append(",'").append(nombreProveedor).append("'");
        s.append(",").append(usuario);
        s.append(",current_timestamp");
        s.append(")");
        return s.toString();
    }

    // =========================================================================

    private String monedaCuenta(Object bancoItemId) {
        return nvlC(cuentasBancosContainer.getContainerProperty(bancoItemId, MONEDA_PROPERTY).getValue());
    }

    /** Retorna cadena vacía si el valor es null, de lo contrario su toString() recortado. */
    private String nvlC(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        ((SopdiUI) UI.getCurrent()).lblEmpresaYFormulario.setValue(empresaId + " " + empresaNombre + " Movimientos Especiales de Banco");
        Page.getCurrent().setTitle("Sopdi - Movimientos especiales de banco");
    }
}
