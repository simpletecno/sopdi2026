package com.simpletecno.sopdi.tesoreria;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.SelectionEvent;
import com.vaadin.event.SelectionEvent.SelectionListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.Grid.HeaderCell;
import com.vaadin.ui.Grid.HeaderRow;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.renderers.ClickableRenderer;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.ui.NumberField;

import com.simpletecno.sopdi.contabilidad.PartidaContableService;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author user
 */
public class AutorizarPagosCorrientesView extends VerticalLayout implements View {

    VerticalLayout mainLayout;

    // --- Cuentas bancarias ---
    static final String ID_CUENTABANCO_PROPERTY = "IdCuentaBanco";
    static final String CUENTA_BANCARIA_PROPERTY = "Cuenta Bancaria";
    static final String BANCO_PROPERTY = "Banco";
    static final String MONEDA_PROPERTY = "Moneda";
    static final String SALDO_CONTABLE_PROPERTY = "Saldo contable";
    static final String PAGOS_PROPERTY = "A pagar";
    static final String NUEVO_SALDO_PROPERTY = "Nuevo saldo";
    static final String NUEVO_SALDOSF_PROPERTY = "NSALDOSF";
    static final String ULTIMO_CHEQUE_PROPERTY = "Ultimo cheque";
    static final String PAGOSSF_PROPERTY = "PagosSF";
    static final String ID_NOMENCLATURA_PROPERTY = "IdNomenclatura";

    // --- Por pagar (Tab 1) ---
    static final String TIPO_DOCUMENTO_PROPERTY = "Tipo";
    static final String ID_PROVEEDOR_PROPERTY = "IdProveedor";
    static final String PROVEEDOR_PROPERTY = "Proveedor";
    static final String FECHA_PROPERTY = "Fecha";
    static final String NUMERO_FACTURA_PROPERTY = "Número";
    static final String SALDO_PROPERTY = "Saldo";
    static final String ANTIGUEDAD_PROPERTY = "Antiguedad";
    static final String A_LIQUIDAR_PROPERTY = "A liquidar";
    static final String CHEQUE_PROPERTY = "# Cheque";
    static final String A_LIQUIDAR_ANTICIPOS_PROPERTY = "Anticipos";
    static final String A_LIQUIDAR_MONTO_CHEQUE_PROPERTY = "Cheque";
    static final String CODIGO_PARTIDA_PROPERTY = "Codigo partida";
    static final String CODIGO_CC_PROPERTY = "CodigoCC";
    static final String SALDOSF_PROPERTY = "SaldoSF";
    static final String A_LIQUIDAR_ANTICIPOSSF_PROPERTY = "APAGARANTICIPOS";
    static final String A_LIQUIDAR_MONTO_CHEQUESF_PROPERTY = "MONTOCHEQUE";
    static final String NOMBRE_PROVEEDOR_PROPERTY = "NombreProveedor";
    static final String TOTAL_SALDO_QUETZALES_PROPERTY = "TotalSaldoQtz";
    static final String CODIGO_PARTIDA_PAGO_PROPERTY = "PartidaPago";

    // --- Anticipos OC (Tab 2) ---
    static final String OC_ID_PROPERTY = "OC_ID";
    static final String OC_NOC_PROPERTY = "NOC";
    static final String OC_TIPO_PROPERTY = "Tipo";
    static final String OC_PROVEEDOR_OC_PROPERTY = "Proveedor";
    static final String OC_FECHA_OC_PROPERTY = "Fecha";
    static final String OC_MONEDA_OC_PROPERTY = "Moneda";
    static final String OC_ANTICIPO_OC_PROPERTY = "Anticipo";
    static final String OC_ANTICIPO_SF_OC_PROPERTY = "AnticipoSFOC";
    static final String OC_IDPROVEEDOR_OC_PROPERTY = "OC_IdProveedor";
    static final String OC_CHEQUE_OC_PROPERTY = "# Cheque";
    static final String OC_RESPONSABLE_OC_PROPERTY = "Responsable";
    static final String OC_RAZON_OC_PROPERTY = "Razon";
    static final String OC_ESTADO_OC_PROPERTY = "Estado";
    static final String OC_NOMBRE_PROVEEDOR_OC_PROPERTY = "OC_NombreProveedor";
    static final String OC_CENTROS_COSTO_PROPERTY = "C. Costos";
    static final String OC_CODIGO_PARTIDA_PAGO_PROPERTY = "OC_PartidaPago";

    IndexedContainer cuentasBancosContainer = new IndexedContainer();
    Grid cuentasBancosGrid;
    IndexedContainer porPagarContainer = new IndexedContainer();
    Grid porPagarGrid;
    IndexedContainer anticiposOCContainer = new IndexedContainer();
    Grid anticiposOCGrid;

    Button autorizarBtn;
    boolean darkModeActive = false;

    NumberField saldoFacturaTxt;
    NumberField totalUtilizarAnticiposTxt;
    NumberField montoPendienteChequeTxt;

    double totalMontoQuetzales = 0.00;
    double totalSaldoQueztales = 0.00;
    double totalMontoDolares = 0.00;
    double totalSaldoDolares = 0.00;
    double saldoFacturaSeleccionada = 0.00;

    UI mainUI;
    Statement stQuery, stQuery1;
    ResultSet rsRecords, rsRecords1;
    String queryString;

    String codigoPartidaFactura = "";

    /** Almacena el saldo remanente de cada anticipo (CodigoCC → saldo) tras su aplicación parcial o total. */
    Map<String, Double> anticiposOcupadosMap = new HashMap<>();

    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");
    static DecimalFormat numberFormat2 = new DecimalFormat("######0.00");

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public AutorizarPagosCorrientesView() {
        this.mainUI = UI.getCurrent();
        setWidth("100%");
        setHeightUndefined();
        addStyleName("apc-view");

        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setWidth("100%");

        addComponent(mainLayout);

        createGridCuentasBancos();
        crearGridPorPagar();
        crearGridAnticipOC();
        crearTabSheet();
        crearBotones();

        llenarGridBancos();
        llenarGridPorPagar();
        llenarGridAnticipOC();
    }

    public void createGridCuentasBancos() {
        cuentasBancosContainer.addContainerProperty(ID_CUENTABANCO_PROPERTY, String.class, "");
        cuentasBancosContainer.addContainerProperty(CUENTA_BANCARIA_PROPERTY, String.class, "");
        cuentasBancosContainer.addContainerProperty(BANCO_PROPERTY, String.class, "");
        cuentasBancosContainer.addContainerProperty(MONEDA_PROPERTY, String.class, "");
        cuentasBancosContainer.addContainerProperty(SALDO_CONTABLE_PROPERTY, String.class, "0.00");
        cuentasBancosContainer.addContainerProperty(PAGOS_PROPERTY, String.class, "0.00");
        cuentasBancosContainer.addContainerProperty(NUEVO_SALDO_PROPERTY, String.class, "0.00");
        cuentasBancosContainer.addContainerProperty(ULTIMO_CHEQUE_PROPERTY, String.class, "0");
        cuentasBancosContainer.addContainerProperty(NUEVO_SALDOSF_PROPERTY, String.class, "0.00");
        cuentasBancosContainer.addContainerProperty(PAGOSSF_PROPERTY, String.class, "0.00");
        cuentasBancosContainer.addContainerProperty(ID_NOMENCLATURA_PROPERTY, String.class, "");

        cuentasBancosGrid = new Grid("Cuenta y banco", cuentasBancosContainer);
        cuentasBancosGrid.setImmediate(true);
        cuentasBancosGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        cuentasBancosGrid.setDescription("Seleccione cuenta y banco.");
        cuentasBancosGrid.setHeightMode(HeightMode.ROW);
        cuentasBancosGrid.setHeightByRows(5);
        cuentasBancosGrid.setWidth("100%");
        cuentasBancosGrid.setEditorBuffered(false);

        cuentasBancosGrid.getColumn(ID_CUENTABANCO_PROPERTY).setHidable(true).setHidden(true);
        cuentasBancosGrid.getColumn(NUEVO_SALDOSF_PROPERTY).setHidable(true).setHidden(true);
        cuentasBancosGrid.getColumn(PAGOSSF_PROPERTY).setHidable(true).setHidden(true);
        cuentasBancosGrid.getColumn(ID_NOMENCLATURA_PROPERTY).setHidable(true).setHidden(true);

        cuentasBancosGrid.getColumn(CUENTA_BANCARIA_PROPERTY).setExpandRatio(1);
        cuentasBancosGrid.getColumn(BANCO_PROPERTY).setExpandRatio(2);
        cuentasBancosGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if (SALDO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (NUEVO_SALDO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (ULTIMO_CHEQUE_PROPERTY.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else {
                return null;
            }
        });

        cuentasBancosGrid.addSelectionListener((SelectionListener) event -> {
            if (event.getAdded().isEmpty()) return;
            Object newItemId = event.getAdded().iterator().next();
            Property monedaProperty = cuentasBancosContainer.getContainerProperty(newItemId, MONEDA_PROPERTY);
            if (monedaProperty == null) return;
            String moneda = String.valueOf(monedaProperty.getValue());
            for (Object itemId : event.getSelected()) {
                if (!itemId.equals(newItemId)) {
                    Property otherMonedaProperty = cuentasBancosContainer.getContainerProperty(itemId, MONEDA_PROPERTY);
                    if (otherMonedaProperty != null && String.valueOf(otherMonedaProperty.getValue()).equals(moneda)) {
                        Notification.show("Solo puede haber una cuenta bancaria de la misma moneda.", Notification.Type.WARNING_MESSAGE);
                        cuentasBancosGrid.deselectAll();
                        break;
                    }
                }
            }
        });

        mainLayout.addComponent(cuentasBancosGrid);
        mainLayout.setComponentAlignment(cuentasBancosGrid, Alignment.TOP_CENTER);
    }

    public void crearGridPorPagar() {

        porPagarContainer.addContainerProperty(TIPO_DOCUMENTO_PROPERTY, String.class, "");
        porPagarContainer.addContainerProperty(PROVEEDOR_PROPERTY, String.class, "");
        porPagarContainer.addContainerProperty(FECHA_PROPERTY, String.class, "");
        porPagarContainer.addContainerProperty(NUMERO_FACTURA_PROPERTY, String.class, "");
        porPagarContainer.addContainerProperty(MONEDA_PROPERTY, String.class, "QUETZALES");
        porPagarContainer.addContainerProperty(SALDO_PROPERTY, String.class, "0.00");
        porPagarContainer.addContainerProperty(ANTIGUEDAD_PROPERTY, String.class, "");
        porPagarContainer.addContainerProperty(A_LIQUIDAR_PROPERTY, String.class, "0.00");
        porPagarContainer.addContainerProperty(A_LIQUIDAR_ANTICIPOS_PROPERTY, String.class, "0.00");
        porPagarContainer.addContainerProperty(A_LIQUIDAR_MONTO_CHEQUE_PROPERTY, String.class, "0.00");
        porPagarContainer.addContainerProperty(CHEQUE_PROPERTY, String.class, "");
        porPagarContainer.addContainerProperty(ID_PROVEEDOR_PROPERTY, String.class, "");
        porPagarContainer.addContainerProperty(CODIGO_PARTIDA_PROPERTY, String.class, "");
        porPagarContainer.addContainerProperty(CODIGO_CC_PROPERTY, String.class, "");
        porPagarContainer.addContainerProperty(SALDOSF_PROPERTY, String.class, "0.00");
        porPagarContainer.addContainerProperty(A_LIQUIDAR_ANTICIPOSSF_PROPERTY, String.class, "0.00");
        porPagarContainer.addContainerProperty(A_LIQUIDAR_MONTO_CHEQUESF_PROPERTY, String.class, "0.00");
        porPagarContainer.addContainerProperty(NOMBRE_PROVEEDOR_PROPERTY, String.class, "");
        porPagarContainer.addContainerProperty(TOTAL_SALDO_QUETZALES_PROPERTY, String.class, "0.00");
        porPagarContainer.addContainerProperty(CODIGO_PARTIDA_PAGO_PROPERTY, String.class, "");

        porPagarGrid = new Grid("Cuentas por pagar", porPagarContainer);

        porPagarGrid.setWidth("100%");
        porPagarGrid.setImmediate(true);
        porPagarGrid.setDescription("Seleccione.");
        porPagarGrid.setHeightMode(HeightMode.ROW);
        porPagarGrid.setHeightByRows(7);

        porPagarGrid.getColumn(CODIGO_PARTIDA_PROPERTY).setHidable(true).setHidden(true);
        porPagarGrid.getColumn(CODIGO_CC_PROPERTY).setHidable(true).setHidden(true);
        porPagarGrid.getColumn(FECHA_PROPERTY).setHidable(true);
        porPagarGrid.getColumn(SALDOSF_PROPERTY).setHidden(true);
        porPagarGrid.getColumn(ID_PROVEEDOR_PROPERTY).setHidable(true).setHidden(true);
        porPagarGrid.getColumn(A_LIQUIDAR_ANTICIPOSSF_PROPERTY).setHidable(true).setHidden(true);
        porPagarGrid.getColumn(A_LIQUIDAR_MONTO_CHEQUESF_PROPERTY).setHidable(true).setHidden(true);
        porPagarGrid.getColumn(NOMBRE_PROVEEDOR_PROPERTY).setHidable(true).setHidden(true);
        porPagarGrid.getColumn(TOTAL_SALDO_QUETZALES_PROPERTY).setHidable(true).setHidden(true);
        porPagarGrid.getColumn(CODIGO_PARTIDA_PAGO_PROPERTY).setHidable(true).setHidden(true);

        porPagarGrid.getColumn(TIPO_DOCUMENTO_PROPERTY).setWidth(100);
        porPagarGrid.getColumn(PROVEEDOR_PROPERTY).setWidth(180);
        porPagarGrid.getColumn(FECHA_PROPERTY).setWidth(95);
        porPagarGrid.getColumn(NUMERO_FACTURA_PROPERTY).setWidth(100);
        porPagarGrid.getColumn(MONEDA_PROPERTY).setWidth(90);
        porPagarGrid.getColumn(SALDO_PROPERTY).setWidth(110);
        porPagarGrid.getColumn(A_LIQUIDAR_PROPERTY).setWidth(110);
        porPagarGrid.getColumn(CHEQUE_PROPERTY).setWidth(70);
        porPagarGrid.getColumn(ANTIGUEDAD_PROPERTY).setWidth(70);

        porPagarGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if (SALDO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (A_LIQUIDAR_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (A_LIQUIDAR_ANTICIPOS_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (A_LIQUIDAR_MONTO_CHEQUE_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (CHEQUE_PROPERTY.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else if (ANTIGUEDAD_PROPERTY.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else {
                return null;
            }
        });

        porPagarGrid.getColumn(A_LIQUIDAR_PROPERTY)
                .setRenderer(new ButtonRenderer(this::onALiquidarButtonClick))
                .setWidth(120);

        HeaderRow filterRow = porPagarGrid.appendHeaderRow();

        HeaderCell cell = filterRow.getCell(PROVEEDOR_PROPERTY);
        TextField filterField = new TextField();
        filterField.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField.setInputPrompt("Filtrar");
        filterField.setColumns(10);
        filterField.addTextChangeListener(change -> {
            porPagarContainer.removeContainerFilters(PROVEEDOR_PROPERTY);
            if (!change.getText().isEmpty()) {
                porPagarContainer.addContainerFilter(
                        new SimpleStringFilter(PROVEEDOR_PROPERTY, change.getText(), true, false));
            }
        });
        cell.setComponent(filterField);

        HeaderCell cell0 = filterRow.getCell(NUMERO_FACTURA_PROPERTY);
        TextField filterField0 = new TextField();
        filterField0.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField0.setInputPrompt("Filtrar");
        filterField0.setColumns(8);
        filterField0.addTextChangeListener(change -> {
            porPagarContainer.removeContainerFilters(NUMERO_FACTURA_PROPERTY);
            if (!change.getText().isEmpty()) {
                porPagarContainer.addContainerFilter(
                        new SimpleStringFilter(NUMERO_FACTURA_PROPERTY, change.getText(), true, false));
            }
        });
        cell0.setComponent(filterField0);

        HeaderCell cell1 = filterRow.getCell(MONEDA_PROPERTY);
        TextField filterField1 = new TextField();
        filterField1.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField1.setInputPrompt("Filtrar");
        filterField1.setColumns(6);
        filterField1.addTextChangeListener(change -> {
            porPagarContainer.removeContainerFilters(MONEDA_PROPERTY);
            if (!change.getText().isEmpty()) {
                porPagarContainer.addContainerFilter(
                        new SimpleStringFilter(MONEDA_PROPERTY, change.getText(), true, false));
            }
        });
        cell1.setComponent(filterField1);

        saldoFacturaTxt = new NumberField("Saldo de documento : ");
        saldoFacturaTxt.setDecimalAllowed(true);
        saldoFacturaTxt.setDecimalPrecision(2);
        saldoFacturaTxt.setMinimumFractionDigits(2);
        saldoFacturaTxt.setDecimalSeparator('.');
        saldoFacturaTxt.setDecimalSeparatorAlwaysShown(true);
        saldoFacturaTxt.setValue(0d);
        saldoFacturaTxt.setGroupingUsed(true);
        saldoFacturaTxt.setGroupingSeparator(',');
        saldoFacturaTxt.setGroupingSize(3);
        saldoFacturaTxt.setImmediate(true);
        saldoFacturaTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        saldoFacturaTxt.setWidth("8em");
        saldoFacturaTxt.setReadOnly(false);
        // porPagarGrid se agrega al TabSheet en crearTabSheet()
    }

    private void crearGridAnticipOC() {

        anticiposOCContainer.addContainerProperty(OC_ID_PROPERTY, String.class, "");
        anticiposOCContainer.addContainerProperty(OC_NOC_PROPERTY, String.class, "");
        anticiposOCContainer.addContainerProperty(OC_TIPO_PROPERTY, String.class, "");
        anticiposOCContainer.addContainerProperty(OC_PROVEEDOR_OC_PROPERTY, String.class, "");
        anticiposOCContainer.addContainerProperty(OC_FECHA_OC_PROPERTY, String.class, "");
        anticiposOCContainer.addContainerProperty(OC_MONEDA_OC_PROPERTY, String.class, "");
        anticiposOCContainer.addContainerProperty(OC_ANTICIPO_OC_PROPERTY, String.class, "0.00");
        anticiposOCContainer.addContainerProperty(OC_ANTICIPO_SF_OC_PROPERTY, String.class, "0.00");
        anticiposOCContainer.addContainerProperty(OC_IDPROVEEDOR_OC_PROPERTY, String.class, "");
        anticiposOCContainer.addContainerProperty(OC_CHEQUE_OC_PROPERTY, String.class, "");
        anticiposOCContainer.addContainerProperty(OC_RESPONSABLE_OC_PROPERTY, String.class, "");
        anticiposOCContainer.addContainerProperty(OC_RAZON_OC_PROPERTY, String.class, "");
        anticiposOCContainer.addContainerProperty(OC_ESTADO_OC_PROPERTY, String.class, "");
        anticiposOCContainer.addContainerProperty(OC_NOMBRE_PROVEEDOR_OC_PROPERTY, String.class, "");
        anticiposOCContainer.addContainerProperty(OC_CENTROS_COSTO_PROPERTY, String.class, "");
        anticiposOCContainer.addContainerProperty(OC_CODIGO_PARTIDA_PAGO_PROPERTY, String.class, "");

        anticiposOCGrid = new Grid("Solicitudes de Anticipos OC", anticiposOCContainer);
        anticiposOCGrid.setWidth("100%");
        anticiposOCGrid.setImmediate(true);
        anticiposOCGrid.setDescription("Haga clic en el monto de anticipo para asignar cheque.");
        anticiposOCGrid.setHeightMode(HeightMode.ROW);
        anticiposOCGrid.setHeightByRows(7);

        anticiposOCGrid.getColumn(OC_ID_PROPERTY).setHidable(true).setHidden(true);
        anticiposOCGrid.getColumn(OC_ANTICIPO_SF_OC_PROPERTY).setHidable(true).setHidden(true);
        anticiposOCGrid.getColumn(OC_IDPROVEEDOR_OC_PROPERTY).setHidable(true).setHidden(true);
        anticiposOCGrid.getColumn(OC_NOMBRE_PROVEEDOR_OC_PROPERTY).setHidable(true).setHidden(true);
        anticiposOCGrid.getColumn(OC_CODIGO_PARTIDA_PAGO_PROPERTY).setHidable(true).setHidden(true);

        anticiposOCGrid.getColumn(OC_NOC_PROPERTY).setWidth(90);
        anticiposOCGrid.getColumn(OC_TIPO_PROPERTY).setWidth(120);
        anticiposOCGrid.getColumn(OC_PROVEEDOR_OC_PROPERTY).setWidth(180);
        anticiposOCGrid.getColumn(OC_FECHA_OC_PROPERTY).setWidth(90);
        anticiposOCGrid.getColumn(OC_MONEDA_OC_PROPERTY).setWidth(90);
        anticiposOCGrid.getColumn(OC_CENTROS_COSTO_PROPERTY).setWidth(150);
        anticiposOCGrid.getColumn(OC_ANTICIPO_OC_PROPERTY).setWidth(120);
        anticiposOCGrid.getColumn(OC_CHEQUE_OC_PROPERTY).setWidth(80);
        anticiposOCGrid.getColumn(OC_RESPONSABLE_OC_PROPERTY).setWidth(120);
        anticiposOCGrid.getColumn(OC_ESTADO_OC_PROPERTY).setWidth(80);

        anticiposOCGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if (OC_ANTICIPO_OC_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (OC_CHEQUE_OC_PROPERTY.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else {
                return null;
            }
        });

        // Botón en columna Anticipo para asignar cheque (monto total, sin parcial)
        anticiposOCGrid.getColumn(OC_ANTICIPO_OC_PROPERTY)
                .setRenderer(new ButtonRenderer(this::onAsignarChequeOCButtonClick))
                .setWidth(120);
        // anticiposOCGrid se agrega al TabSheet en crearTabSheet()
    }

    private void crearTabSheet() {
        TabSheet tabSheet = new TabSheet();
        tabSheet.setWidth("100%");

        VerticalLayout tab1Layout = new VerticalLayout();
        tab1Layout.setWidth("100%");
        tab1Layout.setSpacing(true);
        tab1Layout.addComponent(porPagarGrid);

        VerticalLayout tab2Layout = new VerticalLayout();
        tab2Layout.setWidth("100%");
        tab2Layout.setSpacing(true);
        tab2Layout.addComponent(anticiposOCGrid);

        Label tab2Hint = new Label("Haga clic en el monto de Anticipo para asignar cheque. El cheque cubre el total del anticipo.");
        tab2Hint.addStyleName(ValoTheme.LABEL_SMALL);
        tab2Layout.addComponent(tab2Hint);

        tabSheet.addTab(tab1Layout, "Cuentas por pagar", FontAwesome.FILE_TEXT_O);
        tabSheet.addTab(tab2Layout, "Solicitudes de Anticipos OC", FontAwesome.SHOPPING_CART);

        mainLayout.addComponent(tabSheet);
        mainLayout.setComponentAlignment(tabSheet, Alignment.TOP_CENTER);
    }

    /**
     * Asigna automáticamente el próximo cheque de la cuenta bancaria seleccionada
     * al anticipo OC. El monto es el total del anticipo sin posibilidad de parcial.
     */
    private void onAsignarChequeOCButtonClick(ClickableRenderer.RendererClickEvent event) {

        String monedaOC = nvlC(anticiposOCContainer.getContainerProperty(event.getItemId(), OC_MONEDA_OC_PROPERTY).getValue());

        // Buscar cuenta bancaria seleccionada de la misma moneda
        boolean cuentaEncontrada = false;
        Object cuentaItemId = null;
        for (Object itemId : cuentasBancosContainer.getItemIds()) {
            if (itemId == null) continue;
            if (cuentasBancosGrid.isSelected(itemId)) {
                String monedaBanco = nvlC(cuentasBancosContainer.getContainerProperty(itemId, MONEDA_PROPERTY).getValue());
                if (monedaBanco.equalsIgnoreCase(monedaOC)) {
                    cuentaEncontrada = true;
                    cuentaItemId = itemId;
                    break;
                }
            }
        }
        if (!cuentaEncontrada) {
            Notification.show("Seleccione una cuenta bancaria de la misma moneda.", Notification.Type.WARNING_MESSAGE);
            return;
        }

        // Si ya tiene cheque asignado, des-asignar y restaurar saldo
        String chequeActual = nvlC(anticiposOCContainer.getContainerProperty(event.getItemId(), OC_CHEQUE_OC_PROPERTY).getValue());
        if (!chequeActual.isEmpty()) {
            double montoAnticipo = parseMontoSF(anticiposOCContainer.getContainerProperty(event.getItemId(), OC_ANTICIPO_SF_OC_PROPERTY).getValue());
            double saldoBco = parseMontoSF(cuentasBancosContainer.getContainerProperty(cuentaItemId, NUEVO_SALDOSF_PROPERTY).getValue());
            double pagos = parseMontoSF(cuentasBancosContainer.getContainerProperty(cuentaItemId, PAGOSSF_PROPERTY).getValue());
            cuentasBancosContainer.getContainerProperty(cuentaItemId, NUEVO_SALDO_PROPERTY).setValue(numberFormat.format(saldoBco + montoAnticipo));
            cuentasBancosContainer.getContainerProperty(cuentaItemId, NUEVO_SALDOSF_PROPERTY).setValue(numberFormat2.format(saldoBco + montoAnticipo));
            cuentasBancosContainer.getContainerProperty(cuentaItemId, PAGOS_PROPERTY).setValue(numberFormat.format(Math.max(0, pagos - montoAnticipo)));
            cuentasBancosContainer.getContainerProperty(cuentaItemId, PAGOSSF_PROPERTY).setValue(numberFormat2.format(Math.max(0, pagos - montoAnticipo)));
            anticiposOCContainer.getContainerProperty(event.getItemId(), OC_CHEQUE_OC_PROPERTY).setValue("");
            return;
        }

        String ultimoChequeStr = nvlC(cuentasBancosContainer.getContainerProperty(cuentaItemId, ULTIMO_CHEQUE_PROPERTY).getValue());
        if (ultimoChequeStr.isEmpty()) {
            Notification.show("La cuenta bancaria seleccionada no tiene chequera activa.", Notification.Type.WARNING_MESSAGE);
            return;
        }
        int ultimoCheque = Integer.parseInt(ultimoChequeStr);

        // Reusar cheque si el mismo proveedor ya tiene un cheque asignado en otros anticipos OC
        String idProveedorOC = nvlC(anticiposOCContainer.getContainerProperty(event.getItemId(), OC_IDPROVEEDOR_OC_PROPERTY).getValue());
        String chequeExistente = "";
        for (Object itemId2 : anticiposOCContainer.getItemIds()) {
            if (itemId2 == null || itemId2.equals(event.getItemId())) continue;
            if (nvlC(anticiposOCContainer.getContainerProperty(itemId2, OC_IDPROVEEDOR_OC_PROPERTY).getValue()).equals(idProveedorOC)) {
                String ch = nvlC(anticiposOCContainer.getContainerProperty(itemId2, OC_CHEQUE_OC_PROPERTY).getValue());
                if (!ch.isEmpty()) {
                    chequeExistente = ch;
                    break;
                }
            }
        }

        String noCheque;
        if (!chequeExistente.isEmpty()) {
            noCheque = chequeExistente;
        } else {
            if (!numeroChequeEnChequera(ultimoCheque + 1,
                    nvlC(cuentasBancosContainer.getContainerProperty(cuentaItemId, ID_CUENTABANCO_PROPERTY).getValue()))) {
                Notification.show("No hay cheques disponibles en chequera. Por favor revise cuentas bancarias y chequera en el sistema.",
                        Notification.Type.WARNING_MESSAGE);
                return;
            }
            noCheque = String.valueOf(ultimoCheque + 1);
            cuentasBancosContainer.getContainerProperty(cuentaItemId, ULTIMO_CHEQUE_PROPERTY).setValue(noCheque);
        }

        anticiposOCContainer.getContainerProperty(event.getItemId(), OC_CHEQUE_OC_PROPERTY).setValue(noCheque);

        // Descontar del saldo del banco
        double montoAnticipo = parseMontoSF(anticiposOCContainer.getContainerProperty(event.getItemId(), OC_ANTICIPO_SF_OC_PROPERTY).getValue());
        double saldoBco = parseMontoSF(cuentasBancosContainer.getContainerProperty(cuentaItemId, NUEVO_SALDOSF_PROPERTY).getValue());
        double pagos = parseMontoSF(cuentasBancosContainer.getContainerProperty(cuentaItemId, PAGOSSF_PROPERTY).getValue());

        cuentasBancosContainer.getContainerProperty(cuentaItemId, NUEVO_SALDO_PROPERTY).setValue(numberFormat.format(saldoBco - montoAnticipo));
        cuentasBancosContainer.getContainerProperty(cuentaItemId, NUEVO_SALDOSF_PROPERTY).setValue(numberFormat2.format(saldoBco - montoAnticipo));
        cuentasBancosContainer.getContainerProperty(cuentaItemId, PAGOS_PROPERTY).setValue(numberFormat.format(pagos + montoAnticipo));
        cuentasBancosContainer.getContainerProperty(cuentaItemId, PAGOSSF_PROPERTY).setValue(numberFormat2.format(pagos + montoAnticipo));
    }

    public void llenarGridAnticipOC() {
        anticiposOCContainer.removeAllItems();

        try {

            // orden_compra.Anticipo se alias explícitamente para evitar ambigüedad con columnas
            // homónimas que puedan existir en las tablas del JOIN (proveedor_empresa, empresa).
            queryString = " SELECT orden_compra.Id AS OC_Id, orden_compra.NOC AS OC_NOC,";
            queryString += " orden_compra.IdProveedor AS OC_IdProveedor,";
            queryString += " orden_compra.Fecha AS OC_Fecha, orden_compra.Moneda AS OC_Moneda,";
            queryString += " orden_compra.Total AS OC_Total, orden_compra.Anticipo AS OC_Anticipo,";
            queryString += " orden_compra.Responsable AS OC_Responsable, orden_compra.Razon AS OC_Razon,";
            queryString += " orden_compra.Estado AS OC_Estado,";
            queryString += " proveedor_empresa.Nombre AS ProveedorNombre,";
            queryString += " tipo_orden_compra.Descripcion AS TipoOrdenCompra,";
            queryString += " (SELECT GROUP_CONCAT(DISTINCT ocd.idcc ORDER BY ocd.idcc SEPARATOR ' / ')";
            queryString += "  FROM orden_compra_detalle ocd";
            queryString += "  WHERE ocd.IdOrdenCompra = orden_compra.Id";
            queryString += "  AND ocd.idcc IS NOT NULL AND ocd.idcc <> '') AS OC_CC";
            queryString += " FROM orden_compra";
            queryString += " LEFT JOIN empresa ON orden_compra.IdEmpresa = empresa.IdEmpresa";
            queryString += " LEFT JOIN proveedor_empresa ON orden_compra.IdProveedor = proveedor_empresa.IdProveedor";
            queryString += " LEFT JOIN tipo_orden_compra ON orden_compra.IdTipoOrdenCompra = tipo_orden_compra.Id";
            queryString += " WHERE orden_compra.CodigoCCAnticipo = '' AND orden_compra.CodigoCCDocumento = ''";
            queryString += " AND   orden_compra.IdEmpresa =" + empresaId;
            queryString += " AND   proveedor_empresa.IdEmpresa = " + empresaId;

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                do {
                    Object itemId = anticiposOCContainer.addItem();
                    anticiposOCContainer.getContainerProperty(itemId, OC_ID_PROPERTY).setValue(rsRecords.getString("OC_Id"));
                    anticiposOCContainer.getContainerProperty(itemId, OC_NOC_PROPERTY).setValue(rsRecords.getString("OC_NOC"));
                    anticiposOCContainer.getContainerProperty(itemId, OC_TIPO_PROPERTY).setValue(rsRecords.getString("TipoOrdenCompra"));
                    anticiposOCContainer.getContainerProperty(itemId, OC_PROVEEDOR_OC_PROPERTY).setValue(rsRecords.getString("ProveedorNombre"));
                    anticiposOCContainer.getContainerProperty(itemId, OC_FECHA_OC_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("OC_Fecha")));
                    anticiposOCContainer.getContainerProperty(itemId, OC_MONEDA_OC_PROPERTY).setValue(rsRecords.getString("OC_Moneda"));
                    anticiposOCContainer.getContainerProperty(itemId, OC_ANTICIPO_OC_PROPERTY).setValue(numberFormat.format(rsRecords.getDouble("OC_Anticipo")));
                    anticiposOCContainer.getContainerProperty(itemId, OC_ANTICIPO_SF_OC_PROPERTY).setValue(rsRecords.getString("OC_Anticipo"));
                    anticiposOCContainer.getContainerProperty(itemId, OC_IDPROVEEDOR_OC_PROPERTY).setValue(rsRecords.getString("OC_IdProveedor"));
                    anticiposOCContainer.getContainerProperty(itemId, OC_CHEQUE_OC_PROPERTY).setValue("");
                    anticiposOCContainer.getContainerProperty(itemId, OC_RESPONSABLE_OC_PROPERTY).setValue(rsRecords.getString("OC_Responsable"));
                    anticiposOCContainer.getContainerProperty(itemId, OC_RAZON_OC_PROPERTY).setValue(rsRecords.getString("OC_Razon"));
                    anticiposOCContainer.getContainerProperty(itemId, OC_ESTADO_OC_PROPERTY).setValue(rsRecords.getString("OC_Estado"));
                    anticiposOCContainer.getContainerProperty(itemId, OC_NOMBRE_PROVEEDOR_OC_PROPERTY).setValue(rsRecords.getString("ProveedorNombre"));
                    String cc = rsRecords.getString("OC_CC");
                    anticiposOCContainer.getContainerProperty(itemId, OC_CENTROS_COSTO_PROPERTY).setValue(cc != null ? cc : "");
                } while (rsRecords.next());
            }

        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            Notification.show("Error al listar anticipos de ordenes de compra: " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }

    /**
     * Manejador del click en el botón de la columna "A liquidar" del grid porPagar.
     */
    private void onALiquidarButtonClick(ClickableRenderer.RendererClickEvent event) {

        boolean cuentaMonedaSeleccionada = false;
        Object cuentaMonedaItemId = null;
        for (Object itemId : cuentasBancosContainer.getItemIds()) {
            if (itemId == null) continue;
            if (cuentasBancosGrid.isSelected(itemId)) {
                if (String.valueOf(cuentasBancosContainer.getContainerProperty(itemId, MONEDA_PROPERTY).getValue())
                        .equals(String.valueOf(porPagarContainer.getContainerProperty(event.getItemId(), MONEDA_PROPERTY).getValue()))) {
                    cuentaMonedaSeleccionada = true;
                    cuentaMonedaItemId = itemId;
                    break;
                }
            }
        }
        if (!cuentaMonedaSeleccionada) {
            Notification.show("Seleccione una cuenta bancaria de la misma moneda.", Notification.Type.WARNING_MESSAGE);
            return;
        }

        double saldoDoc = Double.parseDouble(String.valueOf(
                porPagarContainer.getContainerProperty(event.getItemId(), SALDOSF_PROPERTY).getValue()));
        double saldoAnticipos = getAnticiposProveedor(
                String.valueOf(porPagarContainer.getContainerProperty(event.getItemId(), ID_PROVEEDOR_PROPERTY).getValue()),
                String.valueOf(porPagarContainer.getContainerProperty(event.getItemId(), MONEDA_PROPERTY).getValue()));

        PagoProveedorWindow win = new PagoProveedorWindow();
        win.setCaption("Pagar a proveedor : " +
                porPagarContainer.getContainerProperty(event.getItemId(), PROVEEDOR_PROPERTY).getValue());
        win.setMoneda(String.valueOf(porPagarContainer.getContainerProperty(event.getItemId(), MONEDA_PROPERTY).getValue())
                .startsWith("Q") ? "Q." : "$.");
        win.setSaldoDocumento(saldoDoc);
        win.setSaldoDocumento(numberFormat.format(saldoDoc));
        win.setSaldoAnticipos(saldoAnticipos);
        win.setMontoAnticipo(saldoAnticipos);
        if (saldoAnticipos >= saldoDoc) {
            win.setMontoAnticipo(saldoDoc);
            win.setMontoCheque(0.00);
        } else {
            win.setMontoCheque(Double.parseDouble(numberFormat2.format(saldoDoc - saldoAnticipos)));
        }

        final Object finalCuentaMonedaItemId = cuentaMonedaItemId;
        win.getAceptarBtn().addClickListener(event1 -> {
            porPagarContainer.getContainerProperty(event.getItemId(), A_LIQUIDAR_PROPERTY)
                    .setValue(numberFormat.format(win.getMontoCheque() + win.getMontoAnticipo()));
            porPagarContainer.getContainerProperty(event.getItemId(), A_LIQUIDAR_ANTICIPOS_PROPERTY)
                    .setValue(numberFormat.format(win.getMontoAnticipo()));
            porPagarContainer.getContainerProperty(event.getItemId(), A_LIQUIDAR_ANTICIPOSSF_PROPERTY)
                    .setValue(numberFormat2.format(win.getMontoAnticipo()));
            porPagarContainer.getContainerProperty(event.getItemId(), A_LIQUIDAR_MONTO_CHEQUE_PROPERTY)
                    .setValue(numberFormat.format(win.getMontoCheque()));
            porPagarContainer.getContainerProperty(event.getItemId(), A_LIQUIDAR_MONTO_CHEQUESF_PROPERTY)
                    .setValue(numberFormat2.format(win.getMontoCheque()));

            String uckStr = nvlC(cuentasBancosContainer.getContainerProperty(finalCuentaMonedaItemId, ULTIMO_CHEQUE_PROPERTY).getValue());
            int ultimoCheque = uckStr.isEmpty() ? 0 : Integer.parseInt(uckStr);

            if (win.getMontoCheque() > 0) {
                double saldoBco = Double.parseDouble(String.valueOf(
                        cuentasBancosContainer.getContainerProperty(finalCuentaMonedaItemId, NUEVO_SALDOSF_PROPERTY).getValue()));
                double pagos = Double.parseDouble(String.valueOf(
                        cuentasBancosContainer.getContainerProperty(finalCuentaMonedaItemId, PAGOSSF_PROPERTY).getValue()));

                for (Object itemId2 : porPagarContainer.getItemIds()) {
                    if (itemId2 == null || itemId2.equals(event.getItemId())) continue;
                    if (String.valueOf(porPagarContainer.getContainerProperty(itemId2, ID_PROVEEDOR_PROPERTY).getValue())
                            .equals(String.valueOf(porPagarContainer.getContainerProperty(event.getItemId(), ID_PROVEEDOR_PROPERTY).getValue()))) {
                        if (!String.valueOf(porPagarContainer.getContainerProperty(itemId2, CHEQUE_PROPERTY).getValue()).isEmpty()) {
                            porPagarContainer.getContainerProperty(event.getItemId(), CHEQUE_PROPERTY)
                                    .setValue(String.valueOf(porPagarContainer.getContainerProperty(itemId2, CHEQUE_PROPERTY).getValue()));
                        }
                    }
                }

                if (String.valueOf(porPagarContainer.getContainerProperty(event.getItemId(), CHEQUE_PROPERTY).getValue()).isEmpty()) {
                    if (!numeroChequeEnChequera(ultimoCheque + 1,
                            String.valueOf(cuentasBancosContainer.getContainerProperty(finalCuentaMonedaItemId, ID_CUENTABANCO_PROPERTY).getValue()))) {
                        Notification.show("No hay cheques disponibles en chequera. Por favor revise cuentas bancarias y chequera en el sistema.",
                                Notification.Type.WARNING_MESSAGE);
                    } else {
                        porPagarContainer.getContainerProperty(event.getItemId(), CHEQUE_PROPERTY)
                                .setValue(String.valueOf(ultimoCheque + 1));
                        cuentasBancosContainer.getContainerProperty(finalCuentaMonedaItemId, ULTIMO_CHEQUE_PROPERTY)
                                .setValue(String.valueOf(ultimoCheque + 1));
                    }
                } else {
                    cuentasBancosContainer.getContainerProperty(finalCuentaMonedaItemId, NUEVO_SALDO_PROPERTY)
                            .setValue(numberFormat.format(saldoBco - win.getMontoCheque()));
                    cuentasBancosContainer.getContainerProperty(finalCuentaMonedaItemId, NUEVO_SALDOSF_PROPERTY)
                            .setValue(numberFormat2.format(saldoBco - win.getMontoCheque()));
                    cuentasBancosContainer.getContainerProperty(finalCuentaMonedaItemId, PAGOS_PROPERTY)
                            .setValue(numberFormat.format(pagos + win.getMontoCheque()));
                    cuentasBancosContainer.getContainerProperty(finalCuentaMonedaItemId, PAGOSSF_PROPERTY)
                            .setValue(numberFormat2.format(pagos + win.getMontoCheque()));
                }
            } else {
                if (!String.valueOf(porPagarContainer.getContainerProperty(event.getItemId(), CHEQUE_PROPERTY).getValue()).isEmpty()) {
                    cuentasBancosContainer.getContainerProperty(finalCuentaMonedaItemId, ULTIMO_CHEQUE_PROPERTY)
                            .setValue(String.valueOf(ultimoCheque - 1));
                    porPagarContainer.getContainerProperty(event.getItemId(), CHEQUE_PROPERTY).setValue("");
                }
            }
        });

        UI.getCurrent().addWindow(win);
        win.center();
    }

    private void crearBotones() {
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setMargin(new MarginInfo(false, true, false, true));
        buttonsLayout.setSpacing(true);
        buttonsLayout.setWidth("100%");

        Button btnClear = new Button("Limpiar");
        btnClear.setIcon(FontAwesome.ERASER);
        btnClear.setWidth("10em");
        btnClear.addClickListener((Button.ClickListener) event -> limpiar());

        Button btnAutorizarPagos = new Button("Autorizar pagos");
        btnAutorizarPagos.addStyleName(ValoTheme.BUTTON_PRIMARY);
        btnAutorizarPagos.setIcon(FontAwesome.CHECK_SQUARE_O);
        btnAutorizarPagos.setWidth("15em");
        btnAutorizarPagos.addClickListener((Button.ClickListener) event -> {
            double porLiquidar = 0.00;
            for (Object itemId : porPagarContainer.getItemIds()) {
                porLiquidar += Double.parseDouble(String.valueOf(porPagarContainer.getContainerProperty(itemId, A_LIQUIDAR_ANTICIPOSSF_PROPERTY).getValue()));
                porLiquidar += Double.parseDouble(String.valueOf(porPagarContainer.getContainerProperty(itemId, A_LIQUIDAR_MONTO_CHEQUESF_PROPERTY).getValue()));
            }
            for (Object itemId : anticiposOCContainer.getItemIds()) {
                if (!nvlC(anticiposOCContainer.getContainerProperty(itemId, OC_CHEQUE_OC_PROPERTY).getValue()).isEmpty()) {
                    porLiquidar += parseMontoSF(anticiposOCContainer.getContainerProperty(itemId, OC_ANTICIPO_SF_OC_PROPERTY).getValue());
                }
            }
            if (porLiquidar == 0) {
                Notification.show("No hay pagos por aplicar.", Notification.Type.WARNING_MESSAGE);
            } else {
                if (cuentasBancosGrid.getSelectedRows().isEmpty()) {
                    Notification.show("Seleccione al menos una cuenta bancaria.", Notification.Type.WARNING_MESSAGE);
                    return;
                }
                aplicarPagosCorrientes();
            }
        });

        Button btnTema = new Button("Modo oscuro", FontAwesome.MOON_O);
        btnTema.addStyleName("apc-theme-toggle");
        btnTema.setDescription("Cambiar entre modo claro y modo oscuro");
        btnTema.addClickListener((Button.ClickListener) event -> {
            darkModeActive = !darkModeActive;
            if (darkModeActive) {
                AutorizarPagosCorrientesView.this.addStyleName("apc-dark");
                btnTema.setCaption("Modo claro");
                btnTema.setIcon(FontAwesome.SUN_O);
            } else {
                AutorizarPagosCorrientesView.this.removeStyleName("apc-dark");
                btnTema.setCaption("Modo oscuro");
                btnTema.setIcon(FontAwesome.MOON_O);
            }
        });

        buttonsLayout.addComponents(btnClear, btnAutorizarPagos, btnTema);
        buttonsLayout.setComponentAlignment(btnClear, Alignment.TOP_LEFT);
        buttonsLayout.setComponentAlignment(btnAutorizarPagos, Alignment.TOP_RIGHT);
        buttonsLayout.setComponentAlignment(btnTema, Alignment.TOP_RIGHT);
        mainLayout.addComponent(buttonsLayout);
        mainLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);
    }

    private void limpiar() {
        for (Object itemId : porPagarContainer.getItemIds()) {
            porPagarContainer.getContainerProperty(itemId, A_LIQUIDAR_PROPERTY).setValue("0.00");
            porPagarContainer.getContainerProperty(itemId, A_LIQUIDAR_ANTICIPOS_PROPERTY).setValue("0.00");
            porPagarContainer.getContainerProperty(itemId, A_LIQUIDAR_MONTO_CHEQUE_PROPERTY).setValue("0.00");
            porPagarContainer.getContainerProperty(itemId, A_LIQUIDAR_ANTICIPOSSF_PROPERTY).setValue("0.00");
            porPagarContainer.getContainerProperty(itemId, A_LIQUIDAR_MONTO_CHEQUESF_PROPERTY).setValue("0.00");
            porPagarContainer.getContainerProperty(itemId, CHEQUE_PROPERTY).setValue("");
        }
        for (Object itemId : anticiposOCContainer.getItemIds()) {
            anticiposOCContainer.getContainerProperty(itemId, OC_CHEQUE_OC_PROPERTY).setValue("");
        }
        for (Object itemId : cuentasBancosContainer.getItemIds()) {
            cuentasBancosContainer.getContainerProperty(itemId, PAGOS_PROPERTY).setValue("0.00");
            cuentasBancosContainer.getContainerProperty(itemId, PAGOSSF_PROPERTY).setValue("0.00");
            cuentasBancosContainer.getContainerProperty(itemId, NUEVO_SALDO_PROPERTY).setValue("0.00");
            cuentasBancosContainer.getContainerProperty(itemId, NUEVO_SALDOSF_PROPERTY).setValue("0.00");
            cuentasBancosContainer.getContainerProperty(itemId, ULTIMO_CHEQUE_PROPERTY).setValue(obtenerUltimoCheque(String.valueOf(cuentasBancosContainer.getContainerProperty(itemId, ID_CUENTABANCO_PROPERTY).getValue())));
        }
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

            if (rsRecords.next()) {
                Object itemId;
                double dSaldoContable = 0.00;
                do {
                    itemId = cuentasBancosContainer.addItem();

                    cuentasBancosContainer.getContainerProperty(itemId, ID_CUENTABANCO_PROPERTY).setValue(rsRecords.getString("IdCuentaBanco"));
                    cuentasBancosContainer.getContainerProperty(itemId, CUENTA_BANCARIA_PROPERTY).setValue(rsRecords.getString("NoCuenta"));
                    cuentasBancosContainer.getContainerProperty(itemId, BANCO_PROPERTY).setValue(rsRecords.getString("prov.Nombre"));
                    cuentasBancosContainer.getContainerProperty(itemId, MONEDA_PROPERTY).setValue(rsRecords.getString("Moneda"));

                    dSaldoContable = rsRecords.getDouble("Saldo");
                    cuentasBancosContainer.getContainerProperty(itemId, SALDO_CONTABLE_PROPERTY).setValue(numberFormat.format(dSaldoContable));
                    cuentasBancosContainer.getContainerProperty(itemId, NUEVO_SALDO_PROPERTY).setValue(numberFormat.format(dSaldoContable));
                    cuentasBancosContainer.getContainerProperty(itemId, NUEVO_SALDOSF_PROPERTY).setValue(String.valueOf(dSaldoContable));
                    cuentasBancosContainer.getContainerProperty(itemId, ULTIMO_CHEQUE_PROPERTY).setValue(obtenerUltimoCheque(rsRecords.getString("IdCuentaBanco")));
                    cuentasBancosContainer.getContainerProperty(itemId, ID_NOMENCLATURA_PROPERTY).setValue(rsRecords.getString("IdCuentaBanco"));

                } while (rsRecords.next());
            }
        } catch (Exception ex) {
            System.out.println("Error al listar tabla empresas contables :" + ex);
            ex.printStackTrace();
        }
    }

    private double getSaldoContable(String idNomenclatura, String moneda) {
        double dSaldoContable = 0.00;

        queryString = " SELECT SUM(DEBE - HABER) AS SALDOCONTABLE ";
        queryString += " FROM contabilidad_partida ";
        queryString += " WHERE IdEmpresa = " + empresaId;
        queryString += " AND IdNomenclatura = '" + idNomenclatura + "'";
        queryString += " AND contabilidad_partida.Estatus <> 'ANULADO'";
        queryString += " AND contabilidad_partida.MonedaDocumento = '" + moneda + "'";

        try {
            stQuery1 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords1 = stQuery1.executeQuery(queryString);
            if (rsRecords1.next()) {
                dSaldoContable = rsRecords1.getDouble("SALDOCONTABLE");
            }
        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            Notification.show("Error al obtener saldo contable de cuentas bancarias : " + ex.getMessage(), Notification.Type.WARNING_MESSAGE);
        }
        return dSaldoContable;
    }

    /**
     * Devuelve el UltimoUtilizado de la chequera activa (con cheques disponibles).
     * Una chequera está activa cuando UltimoUtilizado < Al.
     * El siguiente cheque a usar es siempre UltimoUtilizado + 1.
     *
     * Diseño de la tabla:
     *   Del             = primer número de la chequera (inclusive)
     *   Al              = último número de la chequera (inclusive)
     *   UltimoUtilizado = último cheque entregado; se inicializa en Del-1 (ninguno usado)
     *
     * Retorna "" si no hay chequera activa (ninguna o todas agotadas).
     */
    private String obtenerUltimoCheque(String idCuentaBanco) {
        String ultimoCheque = "";

        queryString = " SELECT UltimoUtilizado ";
        queryString += " FROM contabilidad_cuentas_bancos_chequera ";
        queryString += " WHERE IdCuentaBanco = " + idCuentaBanco;
        queryString += " AND IdEmpresa = " + empresaId;
        queryString += " AND UltimoUtilizado < Al";
        queryString += " ORDER BY Del ASC LIMIT 1";

        try {
            stQuery1 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords1 = stQuery1.executeQuery(queryString);
            if (rsRecords1.next()) {
                ultimoCheque = rsRecords1.getString("UltimoUtilizado");
            }
        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            Notification.show("Error al obtener ultimo cheque de cuenta bancaria : " + ex.getMessage(), Notification.Type.WARNING_MESSAGE);
        }
        return ultimoCheque;
    }

    private boolean numeroChequeEnChequera(int numeroCheque, String idCuentaBanco) {
        boolean numeroChequeEnChequera = false;

        queryString = " SELECT Al ";
        queryString += " FROM contabilidad_cuentas_bancos_chequera ";
        queryString += " WHERE IdCuentaBanco = " + idCuentaBanco;
        queryString += " AND IdEmpresa = " + empresaId;
        queryString += " AND " + numeroCheque + " >= Del";
        queryString += " AND " + numeroCheque + " <= Al";
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Query Numero de cheque en chequera : {0}", queryString);
        try {
            stQuery1 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords1 = stQuery1.executeQuery(queryString);
            if (rsRecords1.next()) {
                numeroChequeEnChequera = true;
            }
        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            Notification.show("Error al obtener ultimo cheque de cuenta bancaria : " + ex.getMessage(), Notification.Type.WARNING_MESSAGE);
        }

        return numeroChequeEnChequera;
    }

    public double getAnticiposProveedor(String proveedorSeleccionado, String tipoMonedaSeleccionado) {

        double totalSaldoAnticipo = 0.00;

        queryString = " SELECT contabilidad_partida.CodigoPartida, contabilidad_partida.CodigoCC, SUM(DEBE) MontoAnticipo, ";
        queryString += " SUM(DEBE - HABER) TOTALSALDO, SUM(DebeQuetzales - HaberQuetzales) TOTALSALDOQ, contabilidad_partida.Fecha";
        queryString += " FROM contabilidad_partida";
        queryString += " INNER JOIN contabilidad_nomenclatura_empresa ON contabilidad_nomenclatura_empresa.IdNomenclatura = contabilidad_partida.IdNomenclatura";
        queryString += " WHERE contabilidad_partida.IdProveedor = " + proveedorSeleccionado;
        queryString += " AND contabilidad_partida.IdEmpresa = " + empresaId;
        queryString += " AND contabilidad_partida.MonedaDocumento = '" + tipoMonedaSeleccionado + "'";
        queryString += " AND contabilidad_partida.IdNomenclatura = " + ((SopdiUI) mainUI).cuentasContablesDefault.getAnticiposProveedor();
        queryString += " AND contabilidad_partida.Estatus <> 'ANULADO'";
        queryString += " AND contabilidad_nomenclatura_empresa.IdEmpresa = " + empresaId;
        queryString += " GROUP BY contabilidad_partida.CodigoCC";
        queryString += " HAVING TOTALSALDO > 0";

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "-->query para mostrar anticipos pendiente de liquidar del proveedor : " + queryString);

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery1 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                do {
                    totalSaldoAnticipo += rsRecords.getDouble("TOTALSALDO");
                } while (rsRecords.next());
            }

            double anticipoPrevio = 0.00;
            for (Object itemId : porPagarContainer.getItemIds()) {
                if (porPagarContainer.getContainerProperty(itemId, ID_PROVEEDOR_PROPERTY).getValue().equals(proveedorSeleccionado)) {
                    anticipoPrevio += parseMontoSF(porPagarContainer.getContainerProperty(itemId, A_LIQUIDAR_ANTICIPOSSF_PROPERTY).getValue());
                }
            }
            totalSaldoAnticipo -= anticipoPrevio;

        } catch (Exception ex) {
            System.out.println("Error al listar grid anticipos factura : " + ex);
            ex.printStackTrace();
        }

        return totalSaldoAnticipo;
    }

    public void llenarGridPorPagar() {
        porPagarContainer.removeAllItems();
        porPagarContainer.removeAllContainerFilters();

        totalMontoQuetzales = 0.00;
        totalSaldoQueztales = 0.00;
        totalMontoDolares = 0.00;
        totalSaldoDolares = 0.00;

        queryString = " SELECT * ";
        queryString += " FROM contabilidad_partida";
        queryString += " WHERE IdEmpresa = " + empresaId;
        queryString += " AND   Upper(TipoDocumento) IN ('FACTURA','RECIBO','RECIBO CONTABLE','RECIBO CORRIENTE','FORMULARIO IVA',";
        queryString += " 'FORMULARIO ISR', 'FORMULARIO ISR RETENIDO', 'FORMULARIO ISO', 'FORMULARIO RECTIFICACION', 'FORMULARIO ISR OPCIONAL MENSUAL')";
        queryString += " AND   IdNomenclatura IN (" + ((SopdiUI) mainUI).cuentasContablesDefault.getProveedores() + "," + ((SopdiUI) mainUI).cuentasContablesDefault.getInstituciones() + ")";
        queryString += " AND   MontoAutorizadoPagar = 0 ";
        queryString += " AND   MontoAplicarAnticipo = 0 ";
        queryString += " AND   Estatus <> 'ANULADO'";
        if (!((SopdiUI) mainUI).sessionInformation.getStrUserProfileName().equals("ADMINISTRADOR")) {
            queryString += " AND IdProveedor In (SELECT IdProveedor FROM proveedor_empresa WHERE ESAUTORIZADOPAGAR = 1 AND IdEmpresa = " + empresaId + ")";
        }
        queryString += " ORDER by contabilidad_partida.IdProveedor, contabilidad_partida.Fecha";

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);
            stQuery1 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            if (rsRecords.next()) {

                String monedaSimbolo;

                do {

                    queryString = " SELECT ";
                    queryString += " SUM(HABER - DEBE) TOTALSALDO, SUM(HaberQuetzales - DebeQuetzales) TOTALSALDOQ ";
                    queryString += " FROM contabilidad_partida";
                    queryString += " WHERE IdEmpresa = " + empresaId;
                    queryString += " AND CodigoCC = '" + rsRecords.getString("CodigoCC") + "'";
                    queryString += " AND contabilidad_partida.IdNomenclatura = " + rsRecords.getString("IdNomenclatura");
                    queryString += " AND contabilidad_partida.Estatus <> 'ANULADO'";

                    rsRecords1 = stQuery1.executeQuery(queryString);

                    if (rsRecords1.next()) {

                        if (rsRecords1.getDouble("TOTALSALDO") > 0.00) {

                            Object itemId = porPagarContainer.addItem();

                            porPagarContainer.getContainerProperty(itemId, TIPO_DOCUMENTO_PROPERTY).setValue(rsRecords.getString("TipoDocumento"));
                            porPagarContainer.getContainerProperty(itemId, PROVEEDOR_PROPERTY).setValue(rsRecords.getString("IdProveedor") + " " + rsRecords.getString("NombreProveedor"));
                            porPagarContainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha")));
                            porPagarContainer.getContainerProperty(itemId, NUMERO_FACTURA_PROPERTY).setValue(rsRecords.getString("SerieDocumento") + "-" + rsRecords.getString("NumeroDocumento"));
                            porPagarContainer.getContainerProperty(itemId, MONEDA_PROPERTY).setValue(rsRecords.getString("MonedaDocumento"));

                            if (rsRecords.getString("MonedaDocumento").equals("QUETZALES")) {
                                monedaSimbolo = "Q.";
                                totalMontoQuetzales = totalMontoQuetzales + rsRecords.getDouble("MontoDocumento");
                                totalSaldoQueztales = totalSaldoQueztales + rsRecords1.getDouble("TOTALSALDO");
                            } else {
                                monedaSimbolo = "$.";
                                totalMontoDolares = totalMontoDolares + rsRecords.getDouble("MontoDocumento");
                                totalSaldoDolares = totalSaldoDolares + rsRecords1.getDouble("TOTALSALDO");
                            }
                            porPagarContainer.getContainerProperty(itemId, SALDO_PROPERTY).setValue(monedaSimbolo + numberFormat.format(rsRecords1.getDouble("TOTALSALDO")));
                            int antiguedad = Utileria.antiguedad(rsRecords.getDate("Fecha"));
                            porPagarContainer.getContainerProperty(itemId, ANTIGUEDAD_PROPERTY).setValue(String.valueOf(antiguedad));
                            porPagarContainer.getContainerProperty(itemId, A_LIQUIDAR_PROPERTY).setValue(monedaSimbolo + numberFormat.format(rsRecords.getDouble("MontoAplicarAnticipo")));
                            porPagarContainer.getContainerProperty(itemId, ID_PROVEEDOR_PROPERTY).setValue(rsRecords.getString("IdProveedor"));
                            porPagarContainer.getContainerProperty(itemId, CODIGO_CC_PROPERTY).setValue(rsRecords.getString("CodigoCC"));
                            porPagarContainer.getContainerProperty(itemId, CODIGO_PARTIDA_PROPERTY).setValue(rsRecords.getString("CodigoPartida"));
                            porPagarContainer.getContainerProperty(itemId, SALDOSF_PROPERTY).setValue(rsRecords1.getString("TOTALSALDO"));
                            porPagarContainer.getContainerProperty(itemId, NOMBRE_PROVEEDOR_PROPERTY).setValue(rsRecords.getString("NombreProveedor"));
                            porPagarContainer.getContainerProperty(itemId, TOTAL_SALDO_QUETZALES_PROPERTY).setValue(rsRecords1.getString("TOTALSALDOQ"));
                        }
                    }

                } while (rsRecords.next());
            }

        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            Notification.show("Error al listar tabla por pagar: " + ex, Notification.Type.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    //  APLICAR PAGOS CORRIENTES
    // =========================================================================

    private void aplicarPagosCorrientes() {

        cuentasBancosGrid.setReadOnly(true);
        porPagarGrid.setReadOnly(true);
        anticiposOCGrid.setReadOnly(true);

        try {
            ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().setAutoCommit(false);
            Statement st = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            Set<String> codigos = new LinkedHashSet<>();
            codigos.addAll(crearPartidasContables(st));
            codigos.addAll(crearPartidasOCAnticipo(st));
            actualizarUltimoChequeChequera(st);

            ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().commit();
            ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().setAutoCommit(true);

            // Verificar cuadre de cada partida generada (post-commit, datos ya persistidos)
            for (String codigoPartida : codigos) {
                PartidaContableService.EsPartidaCuadrada(
                        codigoPartida,
                        ((SopdiUI) mainUI).databaseProvider.getCurrentConnection(),
                        empresaId);
            }

            Notification notif = new Notification("Pagos aplicados y partidas contables generadas.", Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(2000);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.CHECK);
            notif.show(Page.getCurrent());

            AutorizarPagosCorrientesPDF pdf = new AutorizarPagosCorrientesPDF(porPagarContainer, anticiposOCContainer);
            UI.getCurrent().addWindow(pdf);
            pdf.center();

            llenarGridBancos();
            llenarGridPorPagar();
            llenarGridAnticipOC();

        } catch (Exception ex) {
            try {
                ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().rollback();
                ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().setAutoCommit(true);
            } catch (SQLException rollbackEx) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error en rollback", rollbackEx);
            }
            Notification notif = new Notification("Error al aplicar pagos: " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            notif.setDelayMsec(3000);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.WARNING);
            notif.show(Page.getCurrent());
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error en aplicarPagosCorrientes", ex);
        }
        cuentasBancosGrid.setReadOnly(false);
        porPagarGrid.setReadOnly(false);
        anticiposOCGrid.setReadOnly(false);
    }

    private Set<String> crearPartidasContables(Statement st) throws SQLException {

        Set<String> codigosGenerados = new LinkedHashSet<>();
        anticiposOcupadosMap.clear();

        final String COLS =
            " (IdEmpresa, CodigoPartida, CodigoCC, TipoDocumento, IdNomenclatura, " +
            "  SerieDocumento, NumeroDocumento, Fecha, MonedaDocumento, MontoDocumento," +
            "  Debe, Haber, TipoCambio, DebeQuetzales, HaberQuetzales, Estatus," +
            "  Descripcion, TipoDoca, NoDoca, IdProveedor, NombreProveedor, Nombrecheque," +
            "  CreadoUsuario, CreadoFechaYHora) VALUES ";

        String cuentaProveedores = String.valueOf(((SopdiUI) mainUI).cuentasContablesDefault.getProveedores());
        String cuentaAnticipos = String.valueOf(((SopdiUI) mainUI).cuentasContablesDefault.getAnticiposProveedor());
        String cuentaBancoMonedaLocal = String.valueOf(((SopdiUI) mainUI).cuentasContablesDefault.getBancosMonedaLocal());
        String cuentaBancoMonedaExtranjera = String.valueOf(((SopdiUI) mainUI).cuentasContablesDefault.getBancosMonedaExtranjera());
        String cuentaDiferencialCambiario = String.valueOf(((SopdiUI) mainUI).cuentasContablesDefault.getDiferencialCambiario());

        Utileria util = new Utileria();

        double anticipo   = 0.00;
        double anticipoQ  = 0.00;
        double montoCheq  = 0.00;
        double montoCheqQ = 0.00;
        double totalPago  = 0.00;
        double totalPagoQ = 0.00;
        double totalDebeQ = 0.00;
        double totalHaberQ = 0.00;

        String codigoPartidaPago  = "";
        String esteProveedor      = "";
        double tipoCambio         = 0.00;
        double totalPagoChequeQ   = 0.00;
        double totalPagoAnticipoQ = 0.00;
        double acumuladoCheque   = 0.00;
        StringBuilder descripcion = new StringBuilder();
        StringBuilder documentosPagados = new StringBuilder();
        String chequeQueryString  = "";

        porPagarContainer.sort(new String[] { ID_PROVEEDOR_PROPERTY }, new boolean[] { false });

        for (Object itemId : porPagarContainer.getItemIds()) {

            anticipo  = parseMontoSF(porPagarContainer.getContainerProperty(itemId, A_LIQUIDAR_ANTICIPOSSF_PROPERTY).getValue());
            montoCheq = parseMontoSF(porPagarContainer.getContainerProperty(itemId, A_LIQUIDAR_MONTO_CHEQUESF_PROPERTY).getValue());
            totalPago = anticipo + montoCheq;

            if (totalPago <= 0.00) continue;

            String tipoDocumento = porPagarContainer.getContainerProperty(itemId, TIPO_DOCUMENTO_PROPERTY).getValue().toString();
            String idProveedor = nvlC(porPagarContainer.getContainerProperty(itemId, ID_PROVEEDOR_PROPERTY).getValue());
            String proveedor = nvlC(porPagarContainer.getContainerProperty(itemId, NOMBRE_PROVEEDOR_PROPERTY).getValue());
            String codigoCC = nvlC(porPagarContainer.getContainerProperty(itemId, CODIGO_CC_PROPERTY).getValue());
            String moneda = nvlC(porPagarContainer.getContainerProperty(itemId, MONEDA_PROPERTY).getValue());
            String noCheque = nvlC(porPagarContainer.getContainerProperty(itemId, CHEQUE_PROPERTY).getValue());
            String numeroDoc = nvlC(porPagarContainer.getContainerProperty(itemId, NUMERO_FACTURA_PROPERTY).getValue());
            String debeQuetzalesCC = nvlC(porPagarContainer.getContainerProperty(itemId, TOTAL_SALDO_QUETZALES_PROPERTY).getValue());
            System.out.println("\nnumeroDoc: " + numeroDoc + "\n");

            documentosPagados.append(numeroDoc).append(" ");

            // TipoCambio: 1.00 para Quetzales; tasa del sistema para Dólares.
            tipoCambio = moneda.equalsIgnoreCase("QUETZALES") ? 1.00
                    : parseMontoSF(((SopdiUI) mainUI).tipoCambioDolar);

            totalPagoQ = moneda.equalsIgnoreCase("DOLARES")
                    ? totalPago * tipoCambio
                    : totalPago;

            String tipoDoca = tipoDocumento;

            if (!esteProveedor.equals(String.valueOf(porPagarContainer.getContainerProperty(itemId, ID_PROVEEDOR_PROPERTY).getValue()))) {
                esteProveedor = porPagarContainer.getContainerProperty(itemId, ID_PROVEEDOR_PROPERTY).getValue().toString();
                if (codigoPartidaPago.isEmpty()) {
                    codigoPartidaPago = Utileria.nextCodigoPartida(((SopdiUI) mainUI).databaseProvider.getCurrentConnection(), empresaId, new Date(), 3);
                    codigosGenerados.add(codigoPartidaPago);
                    documentosPagados = new StringBuilder();
                } else {
                    queryString += chequeQueryString;

                    if (totalDebeQ > totalHaberQ) {
                        queryString += "(";
                        queryString += empresaId;
                        queryString += ",'" + codigoPartidaPago + "'";
                        queryString += ",'" + codigoPartidaPago + "'";
                        queryString += ",'CHEQUE'";
                        queryString += "," + cuentaDiferencialCambiario;
                        queryString += ",''";
                        queryString += ",'" + noCheque + "'";
                        queryString += ",current_date";
                        queryString += ",'" + moneda + "'";
                        queryString += "," + totalPago;
                        queryString += ",0";
                        queryString += ",0";
                        queryString += ",1";
                        queryString += "," + (totalHaberQ - totalDebeQ);
                        queryString += ",0";
                        queryString += ",'PAGADO'";
                        queryString += ",'" + descripcion + "'";
                        queryString += ",''";
                        queryString += ",''";
                        queryString += "," + idProveedor;
                        queryString += ",'" + proveedor.replace("'", "") + "'";
                        queryString += ",'" + proveedor.replace("'", "") + "'";
                        queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                        queryString += ",current_timestamp";
                        queryString += "),";
                    } else if (totalDebeQ < totalHaberQ) {
                        queryString += "(";
                        queryString += empresaId;
                        queryString += ",'" + codigoPartidaPago + "'";
                        queryString += ",'" + codigoPartidaPago + "'";
                        queryString += ",'CHEQUE'";
                        queryString += "," + cuentaDiferencialCambiario;
                        queryString += ",''";
                        queryString += ",'" + noCheque + "'";
                        queryString += ",current_date";
                        queryString += ",'" + moneda + "'";
                        queryString += "," + totalPago;
                        queryString += ",0";
                        queryString += ",0";
                        queryString += ",1";//tipo cambio
                        queryString += ",0";
                        queryString += "," + (totalDebeQ - totalHaberQ);
                        queryString += ",'PAGADO'";
                        queryString += ",'" + descripcion + "'";
                        queryString += ",''";
                        queryString += ",''";
                        queryString += "," + idProveedor;
                        queryString += ",'" + proveedor.replace("'", "") + "'";
                        queryString += ",'" + proveedor.replace("'", "") + "'";
                        queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                        queryString += ",current_timestamp";
                        queryString += "),";
                    }

                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, "(1) INSERT partida : " + codigoPartidaPago + " " + queryString.substring(0, queryString.length() - 1));
                    st.executeUpdate(queryString.substring(0, queryString.length() - 1));
                    acumuladoCheque = 0.00;
                    chequeQueryString = "";
                    totalDebeQ = 0.00;
                    totalHaberQ = 0.00;

                    String ultimos3 = codigoPartidaPago.substring((codigoPartidaPago.length() - 3));
                    codigoPartidaPago = codigoPartidaPago.substring(0, codigoPartidaPago.length() - 3) + String.format("%03d", Integer.parseInt(ultimos3) + 1);
                    codigosGenerados.add(codigoPartidaPago);
                }
                descripcion = new StringBuilder("PAGO DOC. ").append(" ").append(numeroDoc);

                queryString = "INSERT INTO contabilidad_partida " + COLS;
                queryString += "(";
                queryString += empresaId;
                queryString += ",'" + codigoPartidaPago + "'";
                queryString += ",'" + codigoCC + "'";
                queryString += ",'CHEQUE'";
                queryString += "," + cuentaProveedores;
                queryString += ",''";
                queryString += ",'" + noCheque + "'";
                queryString += ",current_date";
                queryString += ",'" + moneda + "'";
                queryString += "," + totalPago;
                queryString += "," + totalPago;
                queryString += ",0";
                queryString += "," + tipoCambio;
                queryString += "," + debeQuetzalesCC;
                queryString += ",0";
                queryString += ",'PAGADO'";
                queryString += ",'" + descripcion + "'";
                queryString += ",'" + tipoDoca + "'";
                queryString += ",'" + numeroDoc + "'";
                queryString += "," + idProveedor;
                queryString += ",'" + proveedor.replace("'", "") + "'";
                queryString += ",'" + proveedor.replace("'", "") + "'";
                queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                queryString += ",current_timestamp";
                queryString += "),";

                porPagarContainer.getContainerProperty(itemId, CODIGO_PARTIDA_PAGO_PROPERTY).setValue(codigoPartidaPago);
                totalDebeQ += Double.parseDouble(debeQuetzalesCC);
            } else { //mismo proveedor
                descripcion.append(" ").append(numeroDoc);

                queryString += "(";
                queryString += empresaId;
                queryString += ",'" + codigoPartidaPago + "'";
                queryString += ",'" + codigoCC + "'";
                queryString += ",'CHEQUE'";
                queryString += "," + cuentaProveedores;
                queryString += ",''";
                queryString += ",'" + noCheque + "'";
                queryString += ",current_date";
                queryString += ",'" + moneda + "'";
                queryString += "," + totalPago;
                queryString += "," + totalPago;
                queryString += ",0";
                queryString += "," + tipoCambio;
                queryString += "," + debeQuetzalesCC;
                queryString += ",0";
                queryString += ",'PAGADO'";
                queryString += ",'" + descripcion + "'";
                queryString += ",'" + tipoDoca + "'";
                queryString += ",'" + numeroDoc + "'";
                queryString += "," + idProveedor;
                queryString += ",'" + proveedor.replace("'", "") + "'";
                queryString += ",'" + proveedor.replace("'", "") + "'";
                queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                queryString += ",current_timestamp";
                queryString += "),";
                porPagarContainer.getContainerProperty(itemId, CODIGO_PARTIDA_PAGO_PROPERTY).setValue(codigoPartidaPago);
                totalDebeQ += Double.parseDouble(debeQuetzalesCC);
            }

            if (anticipo > 0.00) {
                String queryStringAnticipo = " SELECT contabilidad_partida.CodigoPartida, contabilidad_partida.CodigoCC, SUM(DEBE) MontoAnticipo, ";
                queryStringAnticipo += " SUM(DEBE - HABER) TOTALSALDO, SUM(DebeQuetzales - HaberQuetzales) TOTALSALDOQ, contabilidad_partida.Fecha";
                queryStringAnticipo += " FROM contabilidad_partida";
                queryStringAnticipo += " INNER JOIN contabilidad_nomenclatura_empresa ON contabilidad_nomenclatura_empresa.IdNomenclatura = contabilidad_partida.IdNomenclatura";
                queryStringAnticipo += " WHERE contabilidad_partida.IdProveedor = " + esteProveedor;
                queryStringAnticipo += " AND contabilidad_partida.IdEmpresa = " + empresaId;
                queryStringAnticipo += " AND contabilidad_partida.MonedaDocumento = '" + moneda + "'";
                queryStringAnticipo += " AND contabilidad_partida.IdNomenclatura = " + cuentaAnticipos;
                queryStringAnticipo += " AND contabilidad_partida.Estatus <> 'ANULADO'";
                queryStringAnticipo += " AND contabilidad_nomenclatura_empresa.IdEmpresa = " + empresaId;
                queryStringAnticipo += " GROUP BY contabilidad_partida.CodigoCC";
                queryStringAnticipo += " HAVING TOTALSALDO > 0";
                queryStringAnticipo += " ORDER BY contabilidad_partida.Fecha ASC";

                rsRecords = st.executeQuery(queryStringAnticipo);

                if (rsRecords.next()) {
                    double totalAnticipoPorLiquidar = anticipo;
                    double montoAnticipo = 0.00;
                    do {
                        montoAnticipo = rsRecords.getDouble("TOTALSALDO") - (anticiposOcupadosMap.get(rsRecords.getString("CodigoCC")) != null ? anticiposOcupadosMap.get(rsRecords.getString("CodigoCC")) : 0.00);

                        if (totalAnticipoPorLiquidar > 0.00 && montoAnticipo > 0.00) {

                            queryString += "(";
                            queryString += empresaId;
                            queryString += ",'" + codigoPartidaPago + "'";
                            queryString += ",'" + rsRecords.getString("CodigoCC") + "'";
                            queryString += ",'CHEQUE'";
                            queryString += "," + cuentaAnticipos;
                            queryString += ",''";
                            queryString += ",'" + noCheque + "'";
                            queryString += ",current_date";
                            queryString += ",'" + moneda + "'";
                            if (montoAnticipo <= totalAnticipoPorLiquidar) {
                                // Anticipo completo: usar el saldo en Q registrado históricamente.
                                queryString += "," + montoAnticipo;
                                queryString += ",0";
                                queryString += "," + montoAnticipo;
                                anticipoQ = moneda.equalsIgnoreCase("QUETZALES")
                                        ? montoAnticipo
                                        : rsRecords.getDouble("TOTALSALDOQ");
                                anticiposOcupadosMap.put(rsRecords.getString("CodigoCC"), montoAnticipo);
                            } else {
                                // Anticipo parcial: Q proporcional al monto efectivamente aplicado.
                                queryString += "," + totalAnticipoPorLiquidar;
                                queryString += ",0";
                                queryString += "," + totalAnticipoPorLiquidar;
                                anticipoQ = moneda.equalsIgnoreCase("QUETZALES")
                                        ? totalAnticipoPorLiquidar
                                        : totalAnticipoPorLiquidar * tipoCambio;
                                anticiposOcupadosMap.put(rsRecords.getString("CodigoCC"), totalAnticipoPorLiquidar);
                            }
                            queryString += "," + tipoCambio;
                            queryString += ",0";
                            queryString += "," + anticipoQ;
                            queryString += ",'PAGADO'";
                            queryString += ",'" + descripcion + "'";
                            queryString += ",'" + tipoDoca + "'";
                            queryString += ",'" + numeroDoc + "'";
                            queryString += "," + idProveedor;
                            queryString += ",'" + proveedor.replace("'", "") + "'";
                            queryString += ",'" + proveedor.replace("'", "") + "'";
                            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                            queryString += ",current_timestamp";
                            queryString += "),";
                            totalAnticipoPorLiquidar -= montoAnticipo;

                            totalHaberQ += anticipoQ;
                        }
                    } while (rsRecords.next());
                }
            }

            if (montoCheq > 0.00) {
                acumuladoCheque += montoCheq;

                chequeQueryString = "(";
                chequeQueryString += empresaId;
                chequeQueryString += ",'" + codigoPartidaPago + "'";
                chequeQueryString += ",'" + codigoPartidaPago + "'";
                chequeQueryString += ",'CHEQUE'";
                chequeQueryString += "," + (moneda.equals("QUETZALES") ? cuentaBancoMonedaLocal : cuentaBancoMonedaExtranjera);
                chequeQueryString += ",''";
                chequeQueryString += ",'" + noCheque + "'";
                chequeQueryString += ",current_date";
                chequeQueryString += ",'" + moneda + "'";
                chequeQueryString += "," + acumuladoCheque;
                chequeQueryString += ",0";
                chequeQueryString += "," + acumuladoCheque;
                chequeQueryString += "," + tipoCambio;
                chequeQueryString += ",0";
                chequeQueryString += "," + (acumuladoCheque * tipoCambio);
                chequeQueryString += ",'PAGADO'";
                chequeQueryString += ",'" + "PAGO DOCS : " + documentosPagados.append(" CHQ. ").append(noCheque).toString() + "'";
                chequeQueryString += ",'" + tipoDoca + "'";
                chequeQueryString += ",'" + numeroDoc + "'";
                chequeQueryString += "," + idProveedor;
                chequeQueryString += ",'" + proveedor.replace("'", "") + "'";
                chequeQueryString += ",'" + proveedor.replace("'", "") + "'";
                chequeQueryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                chequeQueryString += ",current_timestamp";
                chequeQueryString += "),";

                totalHaberQ += (acumuladoCheque * tipoCambio);
            }
        }

        if (!queryString.isEmpty() && queryString.contains("INSERT")) {
            queryString += chequeQueryString;
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "(2) INSERT partida : " + codigoPartidaPago + " " + queryString.substring(0, queryString.length() - 1));
            st.executeUpdate(queryString.substring(0, queryString.length() - 1));
        }
        return codigosGenerados;
    }

    /**
     * Crea partidas contables para los anticipos OC con cheque asignado.
     * DEBE: Cuenta Anticipos a Proveedores (registra el anticipo entregado al proveedor)
     * HABER: Cuenta Bancaria (dinero que sale del banco)
     * Luego actualiza orden_compra.CodigoCCAnticipo para marcarlos como procesados.
     */
    private Set<String> crearPartidasOCAnticipo(Statement st) throws SQLException {

        Set<String> codigosGenerados = new LinkedHashSet<>();

        // CodigoCentrocosto incluido para registrar el CC de cada línea DEBE del anticipo.
        final String COLS =
            " (IdEmpresa, CodigoPartida, CodigoCC, TipoDocumento, IdNomenclatura, " +
            "  SerieDocumento, NumeroDocumento, Fecha, MonedaDocumento, MontoDocumento," +
            "  Debe, Haber, TipoCambio, DebeQuetzales, HaberQuetzales, Estatus," +
            "  Descripcion, TipoDoca, NoDoca, IdProveedor, NombreProveedor, Nombrecheque," +
            "  CreadoUsuario, CreadoFechaYHora, CodigoCentrocosto, IdOrdenCompra) VALUES ";

        String cuentaAnticipos = String.valueOf(((SopdiUI) mainUI).cuentasContablesDefault.getAnticiposProveedor());
        String cuentaBancoMonedaLocal = String.valueOf(((SopdiUI) mainUI).cuentasContablesDefault.getBancosMonedaLocal());
        String cuentaBancoMonedaExtranjera = String.valueOf(((SopdiUI) mainUI).cuentasContablesDefault.getBancosMonedaExtranjera());

        String codigoPartidaBase = null;

        for (Object itemId : anticiposOCContainer.getItemIds()) {

            String noCheque = nvlC(anticiposOCContainer.getContainerProperty(itemId, OC_CHEQUE_OC_PROPERTY).getValue());
            if (noCheque.isEmpty()) continue;

            String ocId = nvlC(anticiposOCContainer.getContainerProperty(itemId, OC_ID_PROPERTY).getValue());
            String noc = nvlC(anticiposOCContainer.getContainerProperty(itemId, OC_NOC_PROPERTY).getValue());
            String idProveedor = nvlC(anticiposOCContainer.getContainerProperty(itemId, OC_IDPROVEEDOR_OC_PROPERTY).getValue());
            String nombreProveedor = nvlC(anticiposOCContainer.getContainerProperty(itemId, OC_NOMBRE_PROVEEDOR_OC_PROPERTY).getValue());
            String moneda = nvlC(anticiposOCContainer.getContainerProperty(itemId, OC_MONEDA_OC_PROPERTY).getValue());
            double montoAnticipo = parseMontoSF(anticiposOCContainer.getContainerProperty(itemId, OC_ANTICIPO_SF_OC_PROPERTY).getValue());
            String cuentaBanco = moneda.equalsIgnoreCase("QUETZALES") ? cuentaBancoMonedaLocal : cuentaBancoMonedaExtranjera;

            // Obtener centros de costo distintos de orden_compra_detalle
            java.util.List<String> centrosCosto = new java.util.ArrayList<>();
            String sqlCC = "SELECT DISTINCT idcc FROM orden_compra_detalle"
                    + " WHERE IdOrdenCompra = " + ocId
                    + " AND idcc IS NOT NULL AND idcc <> ''"
                    + " ORDER BY idcc";
            try (ResultSet rsCC = st.executeQuery(sqlCC)) {
                while (rsCC.next()) {
                    centrosCosto.add(rsCC.getString("idcc"));
                }
            }
            // Si no hay centros de costo registrados, usar una lista con elemento vacío
            // para que se genere al menos una línea DEBE (sin CC).
            if (centrosCosto.isEmpty()) {
                centrosCosto.add("");
            }

            // Código de partida: 1 slot por OC; incrementar localmente para múltiples OCs
            String codigoPartida;
            if (codigoPartidaBase == null) {
                codigoPartidaBase = Utileria.nextCodigoPartida(
                        ((SopdiUI) mainUI).databaseProvider.getCurrentConnection(), empresaId, new Date(), 3);
                codigosGenerados.add(codigoPartidaBase);
                codigoPartida = codigoPartidaBase;
            } else {
                String ultimos3 = codigoPartidaBase.substring(codigoPartidaBase.length() - 3);
                codigoPartidaBase = codigoPartidaBase.substring(0, codigoPartidaBase.length() - 3)
                        + String.format("%03d", Integer.parseInt(ultimos3) + 1);
                codigoPartida = codigoPartidaBase;
                codigosGenerados.add(codigoPartida);
            }

            String descripcion = ("ANTICIPO OC " + noc + " PROV." + nombreProveedor + " CHQ." + noCheque)
                    .replace("'", "").trim();

            // ── DEBE: una línea por cada centro de costo, monto dividido proporcionalmente ──
            // La última línea absorbe el residuo de redondeo.
            int nCC = centrosCosto.size();
            double montoPorCC = Math.floor((montoAnticipo / nCC) * 100) / 100; // truncar a 2 decimales
            double sumaCC = 0.00;

            StringBuilder insertBuilder = new StringBuilder("INSERT INTO contabilidad_partida " + COLS);
            boolean primerTupla = true;

            for (int i = 0; i < nCC; i++) {
                String cc = centrosCosto.get(i);
                double monto = (i == nCC - 1)
                        ? Math.round((montoAnticipo - sumaCC) * 100.0) / 100.0  // último: residuo
                        : montoPorCC;
                sumaCC += monto;

                if (!primerTupla) insertBuilder.append(",");
                primerTupla = false;

                insertBuilder.append("(");
                insertBuilder.append(empresaId);
                insertBuilder.append(",'").append(codigoPartida).append("'");  // CodigoPartida
                insertBuilder.append(",'").append(codigoPartida).append("'");  // CodigoCC
                insertBuilder.append(",'CHEQUE'");
                insertBuilder.append(",").append(cuentaAnticipos);
                insertBuilder.append(",''");
                insertBuilder.append(",'").append(noc).append("'");
                insertBuilder.append(",current_date");
                insertBuilder.append(",'").append(moneda).append("'");
                insertBuilder.append(",").append(monto);
                insertBuilder.append(",").append(monto);   // Debe
                insertBuilder.append(",0");                // Haber
                insertBuilder.append(",1");                // TipoCambio
                insertBuilder.append(",").append(monto);   // DebeQuetzales
                insertBuilder.append(",0");                // HaberQuetzales
                insertBuilder.append(",'PAGADO'");
                insertBuilder.append(",'").append(descripcion).append("'");
                insertBuilder.append(",'CHEQUE'");
                insertBuilder.append(",'").append(noCheque).append("'");
                insertBuilder.append(",").append(idProveedor);
                insertBuilder.append(",'").append(nombreProveedor.replace("'", "")).append("'");
                insertBuilder.append(",'").append(nombreProveedor.replace("'", "")).append("'");
                insertBuilder.append(",").append(((SopdiUI) mainUI).sessionInformation.getStrUserId());
                insertBuilder.append(",current_timestamp");
                insertBuilder.append(",'").append(cc).append("'"); // CodigoCentrocosto
                insertBuilder.append(",").append(ocId);
                insertBuilder.append(")");
            }

            // ── HABER: una sola línea al banco por el total del anticipo ──
            insertBuilder.append(",(");
            insertBuilder.append(empresaId);
            insertBuilder.append(",'").append(codigoPartida).append("'");
            insertBuilder.append(",'").append(codigoPartida).append("'");
            insertBuilder.append(",'CHEQUE'");
            insertBuilder.append(",").append(cuentaBanco);
            insertBuilder.append(",''");
            insertBuilder.append(",'").append(noCheque).append("'");
            insertBuilder.append(",current_date");
            insertBuilder.append(",'").append(moneda).append("'");
            insertBuilder.append(",").append(montoAnticipo);
            insertBuilder.append(",0");               // Debe
            insertBuilder.append(",").append(montoAnticipo); // Haber
            insertBuilder.append(",1"); //tipocambio
            insertBuilder.append(",0");               // DebeQuetzales
            insertBuilder.append(",").append(montoAnticipo); // HaberQuetzales
            insertBuilder.append(",'PAGADO'");
            insertBuilder.append(",'").append(descripcion).append("'");
            insertBuilder.append(",'CHEQUE'");
            insertBuilder.append(",'").append(noCheque).append("'");
            insertBuilder.append(",").append(idProveedor);
            insertBuilder.append(",'").append(nombreProveedor.replace("'", "")).append("'");
            insertBuilder.append(",'").append(nombreProveedor.replace("'", "")).append("'");
            insertBuilder.append(",").append(((SopdiUI) mainUI).sessionInformation.getStrUserId());
            insertBuilder.append(",current_timestamp");
            insertBuilder.append(",''");              // CodigoCentrocosto vacío en el HABER (banco)
            insertBuilder.append(",").append(ocId);
            insertBuilder.append(")");

            String qry = insertBuilder.toString();

            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "INSERT anticipo OC [" + noc + "]: " + qry);
            st.executeUpdate(qry);

            // Guardar el código de partida en el container para el PDF
            anticiposOCContainer.getContainerProperty(itemId, OC_CODIGO_PARTIDA_PAGO_PROPERTY).setValue(codigoPartida);

            // Marcar la OC como procesada con el CodigoCCAnticipo
            String updateOC = "UPDATE orden_compra SET CodigoCCAnticipo = '" + codigoPartida + "'";
            updateOC += " WHERE Id = " + ocId + " AND IdEmpresa = " + empresaId;
            st.executeUpdate(updateOC);
        }
        return codigosGenerados;
    }

    private void marcarDocumentosPagados(Statement st, String codigoPartidaPago) throws SQLException {

        for (Object itemId : porPagarContainer.getItemIds()) {

            double anticipo  = parseMontoSF(porPagarContainer.getContainerProperty(itemId, A_LIQUIDAR_ANTICIPOSSF_PROPERTY).getValue());
            double montoCheq = parseMontoSF(porPagarContainer.getContainerProperty(itemId, A_LIQUIDAR_MONTO_CHEQUESF_PROPERTY).getValue());
            if ((anticipo + montoCheq) <= 0.00) continue;

            String codigoPartidaDoc = nvlC(porPagarContainer.getContainerProperty(itemId, CODIGO_PARTIDA_PROPERTY).getValue());
            String noCheque         = nvlC(porPagarContainer.getContainerProperty(itemId, CHEQUE_PROPERTY).getValue());

            queryString  = " UPDATE contabilidad_partida SET ";
            queryString += "  MontoAutorizadoPagar = " + anticipo;
            queryString += ", MontoAplicarAnticipo = " + montoCheq;
            queryString += ", Estatus    = 'PAGADO'";
            queryString += ", Referencia = '" + codigoPartidaPago + "'";
            queryString += ", TipoDoca   = 'CHEQUE'";
            queryString += ", NoDoca     = '" + noCheque + "'";
            queryString += " WHERE CodigoPartida = '" + codigoPartidaDoc + "'";
            queryString += " AND IdEmpresa = " + empresaId;

            Logger.getLogger(this.getClass().getName()).log(Level.INFO,
                "marcarDocumentosPagados: CodigoPartida=" + codigoPartidaDoc);
            st.executeUpdate(queryString);
        }
    }

    /**
     * Actualiza el último número de cheque en chequera para cuentas por pagar (Tab 1)
     * y para anticipos OC (Tab 2).
     */
    private void actualizarUltimoChequeChequera(Statement st) throws SQLException {

        for (Object bancoItemId : cuentasBancosGrid.getSelectedRows()) {

            // El Set de selección de Vaadin Grid puede contener IDs obsoletos si el
            // container fue recargado (removeAllItems + re-fill) en un ciclo anterior.
            // Verificar que el ítem todavía existe antes de leer sus propiedades.
            if (cuentasBancosContainer.getItem(bancoItemId) == null) continue;

            String idCuentaBanco = nvlC(cuentasBancosContainer.getContainerProperty(bancoItemId, ID_CUENTABANCO_PROPERTY).getValue());
            String monedaBanco   = nvlC(cuentasBancosContainer.getContainerProperty(bancoItemId, MONEDA_PROPERTY).getValue());

            // Tab 1: cuentas por pagar regulares
            for (Object itemId : porPagarContainer.getItemIds()) {

                String monedaDoc  = nvlC(porPagarContainer.getContainerProperty(itemId, MONEDA_PROPERTY).getValue());
                if (!monedaDoc.equalsIgnoreCase(monedaBanco)) continue;

                double montoCheque = parseMontoSF(porPagarContainer.getContainerProperty(itemId, A_LIQUIDAR_MONTO_CHEQUESF_PROPERTY).getValue());
                if (montoCheque <= 0.00) continue;

                String noCheque = nvlC(porPagarContainer.getContainerProperty(itemId, CHEQUE_PROPERTY).getValue())
                                      .replaceAll("[^0-9]", "").trim();
                if (noCheque.isEmpty()) continue;

                queryString  = " UPDATE contabilidad_cuentas_bancos_chequera SET ";
                queryString += "  UltimoUtilizado = " + noCheque;
                queryString += " WHERE IdCuentaBanco = " + idCuentaBanco;
                queryString += " AND IdEmpresa = " + empresaId;
                queryString += " AND Del <= " + noCheque;
                queryString += " AND Al  >= " + noCheque;

                Logger.getLogger(this.getClass().getName()).log(Level.INFO,
                    "actualizarUltimoChequeChequera: IdCuentaBanco=" + idCuentaBanco + " UltimoUtilizado=" + noCheque);
                st.executeUpdate(queryString);
            }

            // Tab 2: anticipos OC
            for (Object itemId : anticiposOCContainer.getItemIds()) {

                String monedaOC = nvlC(anticiposOCContainer.getContainerProperty(itemId, OC_MONEDA_OC_PROPERTY).getValue());
                if (!monedaOC.equalsIgnoreCase(monedaBanco)) continue;

                String noCheque = nvlC(anticiposOCContainer.getContainerProperty(itemId, OC_CHEQUE_OC_PROPERTY).getValue())
                                      .replaceAll("[^0-9]", "").trim();
                if (noCheque.isEmpty()) continue;

                queryString  = " UPDATE contabilidad_cuentas_bancos_chequera SET ";
                queryString += "  UltimoUtilizado = " + noCheque;
                queryString += " WHERE IdCuentaBanco = " + idCuentaBanco;
                queryString += " AND IdEmpresa = " + empresaId;
                queryString += " AND Del <= " + noCheque;
                queryString += " AND Al  >= " + noCheque;

                Logger.getLogger(this.getClass().getName()).log(Level.INFO,
                    "actualizarUltimoChequeOC: IdCuentaBanco=" + idCuentaBanco + " NOC cheque=" + noCheque);
                st.executeUpdate(queryString);
            }
        }
    }

    private String obtenerNomenclaturaBancoPorMoneda(String moneda) {
        for (Object bancoItemId : cuentasBancosGrid.getSelectedRows()) {
            String monedaBanco = nvlC(cuentasBancosContainer.getContainerProperty(bancoItemId, MONEDA_PROPERTY).getValue());
            if (monedaBanco.equalsIgnoreCase(moneda)) {
                return nvlC(cuentasBancosContainer.getContainerProperty(bancoItemId, ID_NOMENCLATURA_PROPERTY).getValue());
            }
        }
        return "";
    }

    private double parseMontoSF(Object value) {
        try {
            if (value == null) return 0.00;
            String s = String.valueOf(value).replaceAll("[^0-9.]", "");
            return s.isEmpty() ? 0.00 : Double.parseDouble(s);
        } catch (NumberFormatException ex) {
            return 0.00;
        }
    }

    private String nvlC(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        ((SopdiUI) UI.getCurrent()).lblEmpresaYFormulario.setValue(empresaId + " " + empresaNombre + " Autorizar Pagos Corrientes");
        Page.getCurrent().setTitle("Sopdi - Pagos corrientes");
    }
}