package com.simpletecno.sopdi.tesoreria;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.SelectionEvent.SelectionListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.Grid.HeaderCell;
import com.vaadin.ui.Grid.HeaderRow;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.renderers.ClickableRenderer;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.ui.NumberField;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author user
 */
public class AutorizarPagosEspecialesView extends VerticalLayout implements View {

    /** Opciones del combo de tipo de pago especial. */
    public static final String ANTICIPO_HONORARIOS              = "ANTICIPO HONORARIOS";
    public static final String ANTICIPO_SUELDOS                 = "ANTICIPO SUELDOS";
    public static final String ANTICIPOS_POR_LIQUIDAR_EMPRESA   = "ANTICIPOS POR LIQUIDAR EMPRESA";
    public static final String VENTA_DE_MONEDA                  = "VENTA DE MONEDA";
    public static final String TRASLADO_CUENTAS                 = "TRASLADO CUENTAS";
    public static final String PRESTAMOS_BANCARIOS              = "PRESTAMOS BANCARIOS";
    public static final String ACREEDORES_POR_LIQUIDAR_EMPRESA  = "ACREEDORES POR LIQUIDAR EMPRESA";
    public static final String PAGO_PRESTAMOS_CORTO_PLAZO       = "PAGO PRESTAMOS CORTO PLAZO";
    public static final String DEVOLUCION_ANTICIPO_CLIENTE      = "DEVOLUCION ANTICIPO CLIENTE";
    public static final String EFECTIVO_EN_TRANSITO             = "EFECTIVO EN TRANSITO";

    /** Lista ordenada de las opciones, en el orden solicitado para el combo. */
    public static final String[] TIPOS_PAGO_ESPECIAL = {
            ANTICIPO_HONORARIOS,
            ANTICIPO_SUELDOS,
            ANTICIPOS_POR_LIQUIDAR_EMPRESA,
            VENTA_DE_MONEDA,
            TRASLADO_CUENTAS,
            PRESTAMOS_BANCARIOS,
            ACREEDORES_POR_LIQUIDAR_EMPRESA,
            PAGO_PRESTAMOS_CORTO_PLAZO,
            DEVOLUCION_ANTICIPO_CLIENTE,
            EFECTIVO_EN_TRANSITO
    };

    VerticalLayout mainLayout;

    /** Combo de elección del tipo de pago especial (al inicio de la vista). */
    ComboBox tipoPagoCbx;

    // ── Controles del layout de campos de pago (entre los dos grids) ──────────
    ComboBox  medioCbx;
    TextField documentoTxt;
    DateField fechaDt;
    ComboBox  proveedorCbx;
    NumberField montoTxt;
    NumberField tasaCambioTxt;
    TextField nombreChequeTxt;
    TextField descripcionTxt;

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

    static final String TIPO_DOCUMENTO_PROPERTY = "Tipo";
    static final String ID_PROVEEDOR_PROPERTY = "IdProveedor";
    static final String PROVEEDOR_PROPERTY = "Proveedor";
    static final String FECHA_PROPERTY = "Fecha";
    static final String CHEQUE_PROPERTY = "# Cheque";
    static final String A_LIQUIDAR_MONTO_CHEQUE_PROPERTY = "Cheque";
    static final String CODIGO_PARTIDA_PROPERTY = "Codigo partida";
    static final String CODIGO_CC_PROPERTY = "CodigoCC";
    static final String A_LIQUIDAR_MONTO_CHEQUESF_PROPERTY = "MONTOCHEQUE";
    static final String NOMBRE_PROVEEDOR_PROPERTY = "NombreProveedor";

    IndexedContainer cuentasBancosContainer = new IndexedContainer();
    Grid cuentasBancosGrid;
    IndexedContainer porPagarContainer = new IndexedContainer();
    Grid porPagarGrid;

    Button agregarBtn = new Button("Autorizar pago");
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

    /** Anticipos de cliente seleccionados para devolución (tipo DEVOLUCION ANTICIPO CLIENTE). */
    List<SeleccionAnticiposDevolucionForm.DevolucionItem> devolucionesSeleccionadas = new ArrayList<>();
    double totalDevolucion = 0.00;

    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");
    static DecimalFormat numberFormat2 = new DecimalFormat("######0.00");

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public AutorizarPagosEspecialesView() {
        this.mainUI = UI.getCurrent();
        setWidth("100%");
        setHeightUndefined();
        addStyleName("apc-view");

        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setWidth("100%");

        addComponent(mainLayout);

        createGridCuentasBancos();
        crearComboTipoPagoYProveedorCliente();
        crearLayoutCamposPago();
        crearGridPorPagar();
        crearBotones();

        llenarGridBancos();
        llenarGridPorPagar();

    }

    /**
     * Crea el combo de elección con los tipos de pago especial y lo coloca al
     * inicio de la vista, debajo del título.
     */
    private void crearComboTipoPagoYProveedorCliente() {
        proveedorCbx = new ComboBox("Proveedor/Cliente : ");
        proveedorCbx.setFilteringMode(FilteringMode.CONTAINS);
        proveedorCbx.setDescription("Seleccione el tipo de pago especial");
        proveedorCbx.setWidth("35em");
        proveedorCbx.setRequired(true);
        proveedorCbx.setRequiredError("Debe seleccionar un tipo de pago especial");
        proveedorCbx.setNullSelectionAllowed(false);
        proveedorCbx.setNewItemsAllowed(false);
        proveedorCbx.setInvalidAllowed(false);
        proveedorCbx.setTextInputAllowed(false);
        proveedorCbx.setImmediate(true);
        llenarComboProveedor();

        tipoPagoCbx = new ComboBox("Tipo de pago especial :");
        tipoPagoCbx.setWidth("25em");
        tipoPagoCbx.setDescription("Seleccione el tipo de pago especial");
        tipoPagoCbx.setRequired(true);
        tipoPagoCbx.setRequiredError("Debe seleccionar un tipo de pago especial");
        tipoPagoCbx.addStyleName("apc-combobox");
        tipoPagoCbx.setNullSelectionAllowed(false);
        tipoPagoCbx.setNewItemsAllowed(false);
        tipoPagoCbx.setInvalidAllowed(false);
        tipoPagoCbx.setTextInputAllowed(false);
        tipoPagoCbx.setImmediate(true);
        tipoPagoCbx.setInputPrompt("Seleccione el tipo de pago");

        for (String tipo : TIPOS_PAGO_ESPECIAL) {
            tipoPagoCbx.addItem(tipo);
        }
//        tipoPagoCbx.select(TIPOS_PAGO_ESPECIAL[0]);

        // Al elegir DEVOLUCION ANTICIPO CLIENTE se abre el formulario de selección
        // de anticipos; para cualquier otro tipo se limpia la selección previa.
        tipoPagoCbx.addValueChangeListener(event -> {
            if(proveedorCbx.getValue() == null) {
                Notification.show("Debe seleccionar un proveedor/cliente", Notification.Type.WARNING_MESSAGE);
                return;
            }
            if (DEVOLUCION_ANTICIPO_CLIENTE.equals(tipoPagoCbx.getValue())) {
                abrirSeleccionAnticiposDevolucion();
            } else {
                limpiarDevolucion();
            }
        });

        HorizontalLayout tipoPagoLayout = new HorizontalLayout();
        tipoPagoLayout.setSpacing(false);
        tipoPagoLayout.setMargin(false);
        tipoPagoLayout.setWidth("100%");
        tipoPagoLayout.addComponents(proveedorCbx, tipoPagoCbx);

        mainLayout.addComponent(tipoPagoLayout);
        mainLayout.setComponentAlignment(tipoPagoLayout, Alignment.TOP_LEFT);
    }

    /**
     * Abre el formulario de selección de anticipos de cliente a devolver. Al aceptar,
     * guarda la selección, suma los montos escritos y precarga los campos del cheque
     * (proveedor, nombre y monto).
     */
    private void abrirSeleccionAnticiposDevolucion() {
        String idProveedorFiltro = proveedorCbx.getValue() == null ? null : String.valueOf(proveedorCbx.getValue());
        SeleccionAnticiposDevolucionForm form = new SeleccionAnticiposDevolucionForm((items, total) -> {
            devolucionesSeleccionadas = items;
            totalDevolucion = total;

            if (!items.isEmpty()) {
                String idProveedor = items.get(0).idProveedor;
                String nombreProveedor = items.get(0).nombreProveedor;

                if (proveedorCbx.containsId(idProveedor)) {
                    proveedorCbx.select(idProveedor);
                }
                if (nombreChequeTxt != null) {
                    nombreChequeTxt.setReadOnly(false);
                    nombreChequeTxt.setValue(nombreProveedor);
                }
            }
            montoTxt.setValue(total);
        }, idProveedorFiltro);
        UI.getCurrent().addWindow(form);
        form.center();
    }

    /** Descarta la selección de devolución y restablece el monto. */
    private void limpiarDevolucion() {
        devolucionesSeleccionadas = new ArrayList<>();
        totalDevolucion = 0.00;
        if (montoTxt != null) {
            montoTxt.setValue(0d);
        }
    }

    /**
     * Crea el layout horizontal con los campos de pago (basado en
     * PagoFacturaProveedorForm, campos líneas 105-113) y lo coloca entre el
     * grid de cuentas bancarias y el grid de cuentas por pagar.
     */
    private void crearLayoutCamposPago() {

        medioCbx = new ComboBox("Medio de pago");
        medioCbx.setWidth("100%");
        medioCbx.setDescription("Seleccione cheque o nota de debito(transferencia)");
        llenarComboMedio();

        documentoTxt = new TextField("# Documento");
        documentoTxt.setWidth("100%");

        fechaDt = new DateField("Fecha");
        fechaDt.setDateFormat("dd/MM/yyyy");
        fechaDt.setWidth("100%");
        fechaDt.setValue(new java.util.Date());
        fechaDt.setReadOnly(true);

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
        tasaCambioTxt.setValue(((SopdiUI)mainUI).getTasaCambioDelDia(Utileria.getFechaYYYYMMDD_1(fechaDt.getValue())));

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

        nombreChequeTxt = new TextField("Nombre cheque / nota");
        nombreChequeTxt.setWidth("100%");
        nombreChequeTxt.setReadOnly(((SopdiUI) mainUI).sessionInformation.getStrUserProfileName().equals("CONTADOR"));
        nombreChequeTxt.setReadOnly(((SopdiUI) mainUI).sessionInformation.getStrUserProfileName().equals("AUXILIAR"));

        descripcionTxt = new TextField("Descripción del pago");
        descripcionTxt.setWidth("100%");

        agregarBtn.addClickListener(event -> {
           // agregar al grid porPagarContainer
            llenarGridPorPagar();
        });

        // Al cambiar el proveedor, sugerir su nombre para el cheque/nota
        proveedorCbx.addValueChangeListener(event -> {
            if (nombreChequeTxt == null) return;
            nombreChequeTxt.setReadOnly(false);
            if (proveedorCbx.getValue() == null) {
                nombreChequeTxt.setValue("");
            } else {
                nombreChequeTxt.setValue(proveedorCbx.getItemCaption(proveedorCbx.getValue()));
            }
        });

        HorizontalLayout camposPagoLayout = new HorizontalLayout();
        camposPagoLayout.setSpacing(true);
        camposPagoLayout.setMargin(false);
        camposPagoLayout.setWidth("100%");
        camposPagoLayout.addComponents(medioCbx, documentoTxt, fechaDt, tasaCambioTxt, montoTxt);
        for (int i = 0; i < camposPagoLayout.getComponentCount(); i++) {
            camposPagoLayout.setComponentAlignment(camposPagoLayout.getComponent(i), Alignment.BOTTOM_LEFT);
        }
        camposPagoLayout.setExpandRatio(medioCbx, 1f);
        camposPagoLayout.setExpandRatio(documentoTxt, 1f);
        camposPagoLayout.setExpandRatio(fechaDt, 1f);
        camposPagoLayout.setExpandRatio(tasaCambioTxt, 0.5f);
        camposPagoLayout.setExpandRatio(montoTxt, 1.0f);

        HorizontalLayout camposPagoLayout2 = new HorizontalLayout();
        camposPagoLayout2.setSpacing(true);
        camposPagoLayout2.setMargin(false);
        camposPagoLayout2.setWidth("100%");
        camposPagoLayout2.addComponents(nombreChequeTxt, descripcionTxt, agregarBtn);
        for (int i = 0; i < camposPagoLayout2.getComponentCount(); i++) {
            camposPagoLayout2.setComponentAlignment(camposPagoLayout2.getComponent(i), Alignment.BOTTOM_LEFT);
        }
        camposPagoLayout2.setExpandRatio(nombreChequeTxt, 1f);
        camposPagoLayout2.setExpandRatio(descripcionTxt, 2f);

        mainLayout.addComponent(camposPagoLayout);
        mainLayout.setComponentAlignment(camposPagoLayout, Alignment.TOP_CENTER);
        mainLayout.addComponent(camposPagoLayout2);
        mainLayout.setComponentAlignment(camposPagoLayout2, Alignment.TOP_CENTER);
    }

    /** Popula el combo de proveedores (proveedor_empresa de la empresa actual). */
    private void llenarComboProveedor() {
        String q = " SELECT * ";
        q += " FROM proveedor_empresa ";
        q += " WHERE Inhabilitado = 0 ";
//        q += " AND EsProveedor = 1 ";
        q += " AND IdEmpresa = " + empresaId;
        q += " ORDER BY Nombre ";

        proveedorCbx.removeAllItems();

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(q);
            while (rsRecords.next()) {
                proveedorCbx.addItem(rsRecords.getString("IDProveedor"));
                proveedorCbx.setItemCaption(rsRecords.getString("IDProveedor"), rsRecords.getString("Nombre"));
            }
        } catch (Exception ex1) {
            System.out.println("Error al listar Proveedores " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    /** Popula el combo de medio de pago. */
    private void llenarComboMedio() {
        medioCbx.removeAllItems();
        medioCbx.addItem("CHEQUE");
        medioCbx.addItem("NOTA DE DEBITO");
        medioCbx.select("CHEQUE");
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
        cuentasBancosGrid.setHeightByRows(4);
        cuentasBancosGrid.setWidth("100%");
        cuentasBancosGrid.setEditorBuffered(false);

        cuentasBancosGrid.getColumn(ID_CUENTABANCO_PROPERTY).setHidable(true).setHidden(true);
        cuentasBancosGrid.getColumn(NUEVO_SALDOSF_PROPERTY).setHidable(true).setHidden(true);
        cuentasBancosGrid.getColumn(PAGOSSF_PROPERTY).setHidable(true).setHidden(true);
        cuentasBancosGrid.getColumn(ID_NOMENCLATURA_PROPERTY).setHidable(true).setHidden(true);

        cuentasBancosGrid.getColumn(CUENTA_BANCARIA_PROPERTY).setExpandRatio(1);
        cuentasBancosGrid.getColumn(BANCO_PROPERTY).setExpandRatio(2);
        cuentasBancosGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {

            if (NUEVO_SALDO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (ULTIMO_CHEQUE_PROPERTY.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else {
                return null;
            }

        });

        //Validar que solamente una cuenta banco puede ser seleccionada de la misma moneda
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
        porPagarContainer.addContainerProperty(MONEDA_PROPERTY, String.class, "QUETZALES");
        porPagarContainer.addContainerProperty(A_LIQUIDAR_MONTO_CHEQUE_PROPERTY, String.class, "0.00");
        porPagarContainer.addContainerProperty(CHEQUE_PROPERTY, String.class, "");
        porPagarContainer.addContainerProperty(ID_PROVEEDOR_PROPERTY, String.class, "");
        porPagarContainer.addContainerProperty(CODIGO_PARTIDA_PROPERTY, String.class, "");
        porPagarContainer.addContainerProperty(CODIGO_CC_PROPERTY, String.class, "");
        porPagarContainer.addContainerProperty(A_LIQUIDAR_MONTO_CHEQUESF_PROPERTY, String.class, "0.00");
        porPagarContainer.addContainerProperty(NOMBRE_PROVEEDOR_PROPERTY, String.class, "");

        porPagarGrid = new Grid("Cuentas por pagar", porPagarContainer);

        porPagarGrid.setWidth("100%");
        porPagarGrid.setImmediate(true);
        porPagarGrid.setDescription("Seleccione.");
        porPagarGrid.setHeightMode(HeightMode.ROW);
        porPagarGrid.setHeightByRows(5);

        porPagarGrid.getColumn(CODIGO_PARTIDA_PROPERTY).setHidable(true).setHidden(true);
        porPagarGrid.getColumn(CODIGO_CC_PROPERTY).setHidable(true).setHidden(true);
        porPagarGrid.getColumn(FECHA_PROPERTY).setHidable(true);
        porPagarGrid.getColumn(ID_PROVEEDOR_PROPERTY).setHidable(true).setHidden(true);
        porPagarGrid.getColumn(A_LIQUIDAR_MONTO_CHEQUESF_PROPERTY).setHidable(true).setHidden(true);
        porPagarGrid.getColumn(NOMBRE_PROVEEDOR_PROPERTY).setHidable(true).setHidden(true);

        // Anchos de columna: Proveedor reducido para que las demás sean visibles
        porPagarGrid.getColumn(TIPO_DOCUMENTO_PROPERTY).setWidth(100);
        porPagarGrid.getColumn(PROVEEDOR_PROPERTY).setWidth(180);
        porPagarGrid.getColumn(FECHA_PROPERTY).setWidth(95);
        porPagarGrid.getColumn(MONEDA_PROPERTY).setWidth(90);
        porPagarGrid.getColumn(CHEQUE_PROPERTY).setWidth(70);

        porPagarGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if (A_LIQUIDAR_MONTO_CHEQUE_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (CHEQUE_PROPERTY.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else {
                return null;
            }

        });

        // Columna con botón embebido para abrir formulario entrada monto
        porPagarGrid.getColumn(A_LIQUIDAR_MONTO_CHEQUE_PROPERTY)
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

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                porPagarContainer.addContainerFilter(
                        new SimpleStringFilter(PROVEEDOR_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell.setComponent(filterField);

        HeaderCell cell1 = filterRow.getCell(MONEDA_PROPERTY);

        TextField filterField1 = new TextField();
        filterField1.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField1.setInputPrompt("Filtrar");
        filterField1.setColumns(6);

        filterField1.addTextChangeListener(change -> {
            porPagarContainer.removeContainerFilters(MONEDA_PROPERTY);
            if (!change.getText().isEmpty()) {
                porPagarContainer.addContainerFilter(new SimpleStringFilter(MONEDA_PROPERTY, change.getText(), true, false));
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

        mainLayout.addComponent(porPagarGrid);
        mainLayout.setComponentAlignment(porPagarGrid, Alignment.TOP_CENTER);
    }

    /**
     * Manejador del click en el botón de la columna "A liquidar" del grid porPagar.
     * Valida que haya una cuenta bancaria seleccionada de la misma moneda del documento,
     * luego abre la ventana PagoProveedorWindow y al confirmar actualiza los montos
     * de anticipo y cheque tanto en porPagarContainer como en cuentasBancosContainer.
     */
    private void onALiquidarButtonClick(ClickableRenderer.RendererClickEvent event) {

        // Buscar cuenta bancaria seleccionada con la misma moneda que el documento
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

        // Preparar datos del documento
        double saldoDoc = Double.parseDouble(String.valueOf(
                porPagarContainer.getContainerProperty(event.getItemId(), A_LIQUIDAR_MONTO_CHEQUESF_PROPERTY).getValue()));

        // Construir y configurar la ventana de pago
        PagoProveedorWindow win = new PagoProveedorWindow();
        win.setCaption("Pagar a proveedor : " +
                porPagarContainer.getContainerProperty(event.getItemId(), PROVEEDOR_PROPERTY).getValue());
        win.setMoneda(String.valueOf(porPagarContainer.getContainerProperty(event.getItemId(), MONEDA_PROPERTY).getValue())
                .startsWith("Q") ? "Q." : "$.");
        win.setSaldoDocumento(saldoDoc);
        win.setSaldoDocumento(numberFormat.format(saldoDoc));

        // Al confirmar en la ventana, actualizar containers
        final Object finalCuentaMonedaItemId = cuentaMonedaItemId;
        win.getAceptarBtn().addClickListener(event1 -> {
            porPagarContainer.getContainerProperty(event.getItemId(), A_LIQUIDAR_MONTO_CHEQUE_PROPERTY)
                    .setValue(numberFormat.format(win.getMontoCheque() + win.getMontoAnticipo()));
            porPagarContainer.getContainerProperty(event.getItemId(), A_LIQUIDAR_MONTO_CHEQUESF_PROPERTY)
                    .setValue(numberFormat2.format(win.getMontoCheque()));

            int ultimoCheque = Integer.parseInt(String.valueOf(
                    cuentasBancosContainer.getContainerProperty(finalCuentaMonedaItemId, ULTIMO_CHEQUE_PROPERTY).getValue()));

            if (win.getMontoCheque() > 0) {
                double saldoBco = Double.parseDouble(String.valueOf(
                        cuentasBancosContainer.getContainerProperty(finalCuentaMonedaItemId, NUEVO_SALDOSF_PROPERTY).getValue()));
                double pagos = Double.parseDouble(String.valueOf(
                        cuentasBancosContainer.getContainerProperty(finalCuentaMonedaItemId, PAGOSSF_PROPERTY).getValue()));


                // Reusar cheque si el proveedor ya tiene otro documento con cheque asignado
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
        btnClear.addClickListener((Button.ClickListener) event -> {
            limpiar();
        });

        Button btnAutorizarPagos = new Button("Autorizar pagos");
        btnAutorizarPagos.addStyleName(ValoTheme.BUTTON_PRIMARY);
        btnAutorizarPagos.setIcon(FontAwesome.CHECK_SQUARE_O);
        btnAutorizarPagos.setWidth("15em");
        btnAutorizarPagos.addClickListener((Button.ClickListener) event -> {
            if (DEVOLUCION_ANTICIPO_CLIENTE.equals(tipoPagoCbx.getValue())) {
                aplicarDevolucionAnticipoCliente();
                return;
            }
            double porLiquidar = 0.00;
            for(Object itemId : porPagarContainer.getItemIds()) {
                porLiquidar+= Double.parseDouble(String.valueOf(porPagarContainer.getContainerProperty(itemId, A_LIQUIDAR_MONTO_CHEQUESF_PROPERTY).getValue()));
            }
            if(porLiquidar == 0) {
                Notification.show("No hay pagos por aplicar.", Notification.Type.WARNING_MESSAGE);
            }
            else {
                if(cuentasBancosGrid.getSelectedRows().isEmpty()) {
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
                AutorizarPagosEspecialesView.this.addStyleName("apc-dark");
                btnTema.setCaption("Modo claro");
                btnTema.setIcon(FontAwesome.SUN_O);
            } else {
                AutorizarPagosEspecialesView.this.removeStyleName("apc-dark");
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
        //Iterar grid por cada item y limpiar los campos
        for(Object itemId : porPagarContainer.getItemIds()) {
            porPagarContainer.getContainerProperty(itemId, A_LIQUIDAR_MONTO_CHEQUE_PROPERTY).setValue("0.00");
            porPagarContainer.getContainerProperty(itemId, A_LIQUIDAR_MONTO_CHEQUESF_PROPERTY).setValue("0.00");
            porPagarContainer.getContainerProperty(itemId, CHEQUE_PROPERTY).setValue("");
        }
        for(Object itemId : cuentasBancosContainer.getItemIds()) {
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

            if (rsRecords.next()) { //  encontrado
                Object itemId;
                double dSaldoContable = 0.00;
                do {
                    itemId = cuentasBancosContainer.addItem();

                    cuentasBancosContainer.getContainerProperty(itemId, ID_CUENTABANCO_PROPERTY).setValue(rsRecords.getString("IdCuentaBanco"));
                    cuentasBancosContainer.getContainerProperty(itemId, CUENTA_BANCARIA_PROPERTY).setValue(rsRecords.getString("NoCuenta"));
                    cuentasBancosContainer.getContainerProperty(itemId, BANCO_PROPERTY).setValue(rsRecords.getString("prov.Nombre"));
                    cuentasBancosContainer.getContainerProperty(itemId, MONEDA_PROPERTY).setValue(rsRecords.getString("Moneda"));

//                    dSaldoContable = getSaldoContable(rsRecords.getString("IdNomenclatura"), rsRecords.getString("Moneda"));
                    dSaldoContable = rsRecords.getDouble("Saldo");
                    cuentasBancosContainer.getContainerProperty(itemId, SALDO_CONTABLE_PROPERTY).setValue(numberFormat.format(dSaldoContable));
                    cuentasBancosContainer.getContainerProperty(itemId, NUEVO_SALDO_PROPERTY).setValue(numberFormat.format(dSaldoContable));
                    cuentasBancosContainer.getContainerProperty(itemId, NUEVO_SALDOSF_PROPERTY).setValue(String.valueOf(dSaldoContable));
                    cuentasBancosContainer.getContainerProperty(itemId, ULTIMO_CHEQUE_PROPERTY).setValue(obtenerUltimoCheque(rsRecords.getString("IdCuentaBanco")));
                    cuentasBancosContainer.getContainerProperty(itemId, ID_NOMENCLATURA_PROPERTY).setValue(rsRecords.getString("ban.IdNomenclatura"));

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
            if(rsRecords1.next()) {
                dSaldoContable = rsRecords1.getDouble("SALDOCONTABLE");
            }
        }
        catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            Notification.show("Error al obtener saldo contable de cuentas bancarias : " + ex.getMessage(), Notification.Type.WARNING_MESSAGE);
        }
        return dSaldoContable;
    }

    /**
     * Devuelve UltimoUtilizado de la chequera activa (con cheques disponibles).
     * Activa = UltimoUtilizado < Al. El siguiente cheque es UltimoUtilizado + 1.
     * UltimoUtilizado se inicializa en Del-1; al usar el primer cheque pasa a Del.
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
            if(rsRecords1.next()) {
                ultimoCheque = rsRecords1.getString("UltimoUtilizado");
            }
        }
        catch (Exception ex) {
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
            if(rsRecords1.next()) {
                numeroChequeEnChequera = true;
            }
        }
        catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            Notification.show("Error al obtener ultimo cheque de cuenta bancaria : " + ex.getMessage(), Notification.Type.WARNING_MESSAGE);
        }

        return numeroChequeEnChequera;
    }

    public void llenarGridPorPagar() {

        Object itemId = porPagarContainer.addItem();
        String monedaSimbolo = "Q.";

//        porPagarContainer.getContainerProperty(itemId, TIPO_DOCUMENTO_PROPERTY).setValue(rsRecords.getString("TipoDocumento"));
//        porPagarContainer.getContainerProperty(itemId, PROVEEDOR_PROPERTY).setValue(rsRecords.getString("IdProveedor") + " " + rsRecords.getString("NombreProveedor"));
//        porPagarContainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha")));
//        porPagarContainer.getContainerProperty(itemId, MONEDA_PROPERTY).setValue(rsRecords.getString("MonedaDocumento"));

//        if (rsRecords.getString("MonedaDocumento").equals("QUETZALES")) {
//            monedaSimbolo = "Q.";
//            totalMontoQuetzales = totalMontoQuetzales + rsRecords.getDouble("MontoDocumento");
//            totalSaldoQueztales = totalSaldoQueztales + rsRecords1.getDouble("TOTALSALDO");
//        } else {
//            monedaSimbolo = "$.";
//            totalMontoDolares = totalMontoDolares + rsRecords.getDouble("MontoDocumento");
//            totalSaldoDolares = totalSaldoDolares + rsRecords1.getDouble("TOTALSALDO");
//        }
//        porPagarContainer.getContainerProperty(itemId, ID_PROVEEDOR_PROPERTY).setValue(rsRecords.getString("IdProveedor"));
//        porPagarContainer.getContainerProperty(itemId, CODIGO_CC_PROPERTY).setValue(rsRecords.getString("CodigoCC"));
//        porPagarContainer.getContainerProperty(itemId, CODIGO_PARTIDA_PROPERTY).setValue(rsRecords.getString("CodigoPartida"));
//        porPagarContainer.getContainerProperty(itemId, NOMBRE_PROVEEDOR_PROPERTY).setValue(rsRecords.getString("NombreProveedor"));

    }

    // =========================================================================
    //  APLICAR PAGOS CORRIENTES
    //  ─────────────────────────────────────────────────────────────────────
    //  Flujo transaccional de 4 pasos que se ejecutan en una sola transacción:
    //    1. crearPartidasContables()        – INSERT en contabilidad_partida. CHEQUE o finiquitar ANTICIPO.
    //    2. marcarDocumentosPagados()       – UPDATE Estatus='PAGADO' en documentos
    //    3. actualizarUltimoChequeChequera() – UPDATE UltimoUtilizado en chequera
    // =========================================================================

    /**
     * Orquestador principal: ejecuta los 4 pasos dentro de una transacción JDBC.
     * Al finalizar correctamente genera el reporte PDF y refresca las grillas.
     */
    private void aplicarPagosCorrientes() {

        cuentasBancosGrid.setReadOnly(true);
        porPagarGrid.setReadOnly(true);

        try {
            ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().setAutoCommit(false);
            Statement st = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            crearPartidasContables(st);
            actualizarUltimoChequeChequera(st);

            ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().commit();
            ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().setAutoCommit(true);

            Notification notif = new Notification("Pagos aplicados y partidas contables generadas.", Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(2000);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.CHECK);
            notif.show(Page.getCurrent());

            AutorizarPagosCorrientesPDF pdf = new AutorizarPagosCorrientesPDF(porPagarContainer);
            UI.getCurrent().addWindow(pdf);
            pdf.center();

            llenarGridBancos();
            llenarGridPorPagar();

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
    }

    // =========================================================================
    //  DEVOLUCION ANTICIPO CLIENTE
    // =========================================================================

    /**
     * Aplica la devolución de anticipos de cliente seleccionados en
     * {@link SeleccionAnticiposDevolucionForm}.
     *
     * Genera una sola partida contable (un cheque / nota de débito) con:
     *   - Una línea DEBE por cada anticipo seleccionado, en la cuenta contable del
     *     anticipo y con su propio CodigoCC (así la partida incluye los CodigoCC de
     *     los anticipos devueltos).
     *   - Una línea HABER en la cuenta bancaria por el total devuelto.
     *
     * Todo se ejecuta en una transacción; si el medio es CHEQUE, se toma el
     * siguiente número de la chequera y se actualiza al confirmar.
     */
    private void aplicarDevolucionAnticipoCliente() {

        if (devolucionesSeleccionadas == null || devolucionesSeleccionadas.isEmpty() || totalDevolucion <= 0.00) {
            Notification.show("Seleccione los anticipos a devolver (combo DEVOLUCION ANTICIPO CLIENTE).", Notification.Type.WARNING_MESSAGE);
            return;
        }

        String moneda = devolucionesSeleccionadas.get(0).moneda;

        // Buscar la cuenta bancaria seleccionada de la misma moneda
        Object bancoItemId = null;
        for (Object itemId : cuentasBancosGrid.getSelectedRows()) {
            if (nvlC(cuentasBancosContainer.getContainerProperty(itemId, MONEDA_PROPERTY).getValue()).equalsIgnoreCase(moneda)) {
                bancoItemId = itemId;
                break;
            }
        }
        if (bancoItemId == null) {
            Notification.show("Seleccione una cuenta bancaria de la misma moneda del anticipo (" + moneda + ").", Notification.Type.WARNING_MESSAGE);
            return;
        }

        String medio = nvlC(medioCbx.getValue());
        String idCuentaBanco = nvlC(cuentasBancosContainer.getContainerProperty(bancoItemId, ID_CUENTABANCO_PROPERTY).getValue());

        // Determinar número de documento / cheque
        String noCheque;
        boolean esCheque = "CHEQUE".equalsIgnoreCase(medio);
        if (esCheque) {
            String ultimo = obtenerUltimoCheque(idCuentaBanco);
            if (ultimo == null || ultimo.trim().isEmpty()) {
                Notification.show("No se pudo obtener el último cheque de la cuenta bancaria. Revise la chequera.", Notification.Type.WARNING_MESSAGE);
                return;
            }
            int siguiente = Integer.parseInt(ultimo.trim()) + 1;
            if (!numeroChequeEnChequera(siguiente, idCuentaBanco)) {
                Notification.show("No hay cheques disponibles en la chequera. Revise cuentas bancarias y chequera.", Notification.Type.WARNING_MESSAGE);
                return;
            }
            noCheque = String.valueOf(siguiente);
        } else {
            noCheque = nvlC(documentoTxt.getValue());
            if (noCheque.isEmpty()) {
                Notification.show("Ingrese el # de documento (nota de débito).", Notification.Type.WARNING_MESSAGE);
                return;
            }
        }

        double tipoCambio = "DOLARES".equalsIgnoreCase(moneda)
                ? parseMontoSF(tasaCambioTxt.getValue())
                : 1.00;
        if (tipoCambio <= 0.00) tipoCambio = 1.00;

        // Cuenta contable (IdNomenclatura) de la cuenta bancaria seleccionada en el grid
        String cuentaBanco = nvlC(cuentasBancosContainer.getContainerProperty(bancoItemId, ID_NOMENCLATURA_PROPERTY).getValue());
        if (cuentaBanco.isEmpty()) {
            Notification.show("La cuenta bancaria seleccionada no tiene nomenclatura contable asociada.", Notification.Type.WARNING_MESSAGE);
            return;
        }

        String idProveedor = devolucionesSeleccionadas.get(0).idProveedor;
        String nombreProveedor = devolucionesSeleccionadas.get(0).nombreProveedor.replace("'", "");
        String nombreCheque = nvlC(nombreChequeTxt.getValue()).replace("'", "");
        if (nombreCheque.isEmpty()) nombreCheque = nombreProveedor;
        String descripcion = ("DEVOLUCION ANTICIPO " + nombreProveedor + " " + medio + "." + noCheque).replace("'", "").trim();

        final String COLS =
            " (IdEmpresa, CodigoPartida, CodigoCC, TipoDocumento, IdNomenclatura, " +
            "  SerieDocumento, NumeroDocumento, Fecha, MonedaDocumento, MontoDocumento," +
            "  Debe, Haber, TipoCambio, DebeQuetzales, HaberQuetzales, Estatus," +
            "  Descripcion, TipoDoca, NoDoca, IdProveedor, NombreProveedor, Nombrecheque," +
            "  CreadoPor, FechaYHoraCreado) VALUES ";

        cuentasBancosGrid.setReadOnly(true);

        try {
            ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().setAutoCommit(false);
            Statement st = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            String codigoPartida = Utileria.nextCodigoPartida(
                    ((SopdiUI) mainUI).databaseProvider.getCurrentConnection(), empresaId, new java.util.Date(), 3);

            String usuario = ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            StringBuilder sql = new StringBuilder("INSERT INTO contabilidad_partida " + COLS);

            // Una línea DEBE por cada anticipo devuelto (con su propio CodigoCC)
            for (SeleccionAnticiposDevolucionForm.DevolucionItem item : devolucionesSeleccionadas) {
                double montoDevolver = item.montoDevolver;
                double montoQ = montoDevolver * tipoCambio;

                sql.append("(");
                sql.append(empresaId);
                sql.append(",'").append(codigoPartida).append("'");           // CodigoPartida
                sql.append(",'").append(item.codigoCC).append("'");           // CodigoCC del anticipo
                sql.append(",'CHEQUE'");                                       // TipoDocumento
                sql.append(",").append(item.idNomenclatura);                  // IdNomenclatura del anticipo
                sql.append(",''");                                             // SerieDocumento
                sql.append(",'").append(noCheque).append("'");                // NumeroDocumento
                sql.append(",current_date");
                sql.append(",'").append(moneda).append("'");
                sql.append(",").append(montoDevolver);                        // MontoDocumento
                sql.append(",").append(montoDevolver);                        // Debe
                sql.append(",0");                                              // Haber
                sql.append(",").append(tipoCambio);
                sql.append(",").append(montoQ);                               // DebeQuetzales
                sql.append(",0");                                             // HaberQuetzales
                sql.append(",'PAGADO'");
                sql.append(",'").append(descripcion).append("'");
                sql.append(",'").append(item.tipo.replace("'", "")).append("'");
                sql.append(",'").append(noCheque).append("'");
                sql.append(",").append(idProveedor);
                sql.append(",'").append(nombreProveedor).append("'");
                sql.append(",'").append(nombreCheque).append("'");
                sql.append(",").append(usuario);
                sql.append(",current_timestamp");
                sql.append("),");
            }

            // Línea HABER en la cuenta bancaria por el total devuelto
            double totalQ = totalDevolucion * tipoCambio;
            sql.append("(");
            sql.append(empresaId);
            sql.append(",'").append(codigoPartida).append("'");
            sql.append(",'").append(codigoPartida).append("'");               // CodigoCC = partida
            sql.append(",'CHEQUE'");
            sql.append(",").append(cuentaBanco);                              // IdNomenclatura banco
            sql.append(",''");
            sql.append(",'").append(noCheque).append("'");
            sql.append(",current_date");
            sql.append(",'").append(moneda).append("'");
            sql.append(",").append(totalDevolucion);                          // MontoDocumento
            sql.append(",0");                                                 // Debe
            sql.append(",").append(totalDevolucion);                          // Haber
            sql.append(",").append(tipoCambio);
            sql.append(",0");                                                 // DebeQuetzales
            sql.append(",").append(totalQ);                                   // HaberQuetzales
            sql.append(",'PAGADO'");
            sql.append(",'").append(descripcion).append("'");
            sql.append(",''");
            sql.append(",'").append(noCheque).append("'");
            sql.append(",").append(idProveedor);
            sql.append(",'").append(nombreProveedor).append("'");
            sql.append(",'").append(nombreCheque).append("'");
            sql.append(",").append(usuario);
            sql.append(",current_timestamp");
            sql.append(")");

            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "INSERT devolucion anticipo cliente : {0}", sql.toString());
            st.executeUpdate(sql.toString());

            // Actualizar chequera si fue cheque
            if (esCheque) {
                String updChequera = " UPDATE contabilidad_cuentas_bancos_chequera SET "
                        + " UltimoUtilizado = " + noCheque
                        + " WHERE IdCuentaBanco = " + idCuentaBanco
                        + " AND IdEmpresa = " + empresaId
                        + " AND Del <= " + noCheque
                        + " AND Al  >= " + noCheque;
                st.executeUpdate(updChequera);
            }

            ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().commit();
            ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().setAutoCommit(true);

            Notification notif = new Notification("Devolución de anticipo aplicada y partida generada.", Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(2000);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.CHECK);
            notif.show(Page.getCurrent());

            limpiarDevolucion();
            llenarGridBancos();

        } catch (Exception ex) {
            try {
                ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().rollback();
                ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().setAutoCommit(true);
            } catch (SQLException rollbackEx) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error en rollback", rollbackEx);
            }
            Notification notif = new Notification("Error al aplicar devolución: " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            notif.setDelayMsec(3000);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.WARNING);
            notif.show(Page.getCurrent());
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error en aplicarDevolucionAnticipoCliente", ex);
        }
        cuentasBancosGrid.setReadOnly(false);
    }

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * PASO 1 – Inserta las líneas de la partida contable para cada documento pagado.
     *
     * Por cada documento con monto > 0 se insertan DOS líneas en contabilidad_partida:
     *   Línea DEBE  → cuenta de proveedores  (reduce el pasivo – cuentas por pagar)
     *   Línea HABER → cuenta bancaria         (sale dinero del banco)
     *
     * CodigoCC del nuevo pago  : codigoPartidaPago (identificador único de este lote)
     * CodigoCC_Doc             : CodigoCC del documento original pagado
     *
     * TODO: Para documentos en DOLARES completar tipoCambio y totalPagoQ con la
     *       tasa de cambio vigente del día (fuente: tabla o campo a definir).
     */
    private void crearPartidasContables(Statement st) throws SQLException {

        anticiposOcupadosMap.clear();

        final String COLS =
            " (IdEmpresa, CodigoPartida, CodigoCC, TipoDocumento, IdNomenclatura, " +
            "  SerieDocumento, NumeroDocumento, Fecha, MonedaDocumento, MontoDocumento," +
            "  Debe, Haber, TipoCambio, DebeQuetzales, HaberQuetzales, Estatus," +
            "  Descripcion, TipoDoca, NoDoca, IdProveedor, NombreProveedor, Nombrecheque," +
            "  CreadoPor, FechaYHoraCreado) VALUES ";

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
        double acumuladoCheque   = 0.00;
        String descripcion        = "";
        String chequeQueryString  = "";

        String numeroDoc = "1111";

        porPagarContainer.sort(new String[] { ID_PROVEEDOR_PROPERTY }, new boolean[] { false });

        //Iterar por cada documento a pagar
        for (Object itemId : porPagarContainer.getItemIds()) {

            montoCheq = parseMontoSF(porPagarContainer.getContainerProperty(itemId, A_LIQUIDAR_MONTO_CHEQUESF_PROPERTY).getValue());
            totalPago = anticipo + montoCheq;

            if (totalPago <= 0.00) continue;

            String tipoDocumento = porPagarContainer.getContainerProperty(itemId, TIPO_DOCUMENTO_PROPERTY).getValue().toString();
            String idProveedor = nvlC(porPagarContainer.getContainerProperty(itemId, ID_PROVEEDOR_PROPERTY).getValue());
            String proveedor = nvlC(porPagarContainer.getContainerProperty(itemId, NOMBRE_PROVEEDOR_PROPERTY).getValue());
            String codigoCC = nvlC(porPagarContainer.getContainerProperty(itemId, CODIGO_CC_PROPERTY).getValue());
            String moneda = nvlC(porPagarContainer.getContainerProperty(itemId, MONEDA_PROPERTY).getValue());
            String noCheque = nvlC(porPagarContainer.getContainerProperty(itemId, CHEQUE_PROPERTY).getValue());
            String debeQuetzalesCC = "";//nvlC(porPagarContainer.getContainerProperty(itemId, CHEQUE_PROPERTY).getValue());
            // TODO: Para DOLARES para los DEBEs. Calcular con base al monto HaberQuetzales del documento a pagar.

            totalPagoQ = moneda.equalsIgnoreCase("DOLARES")
                    ? totalPago * tipoCambio
                    : totalPago;

            descripcion = ("PAGO " + proveedor + " CHQ." + numeroDoc + (!noCheque.isEmpty() ? " CHQ." + noCheque : " CON ANTICIPO"))
                    .replace("'", "").trim();
            String tipoDoca = tipoDocumento;

            if (!esteProveedor.equals(String.valueOf(porPagarContainer.getContainerProperty(itemId, ID_PROVEEDOR_PROPERTY).getValue()))) {
                esteProveedor = porPagarContainer.getContainerProperty(itemId, ID_PROVEEDOR_PROPERTY).getValue().toString();
                if (codigoPartidaPago.isEmpty()) { // es la primera vez...
                    codigoPartidaPago = Utileria.nextCodigoPartida(((SopdiUI) mainUI).databaseProvider.getCurrentConnection(), empresaId, new java.util.Date(), 3);
                } else { //ya hubo partida contable
                    //Concatenar chequeQueryString a queryString
                    queryString += chequeQueryString;

                    if(totalDebeQ > totalHaberQ) {
                        //insertar linea diferencial cambiario en el haber
                        queryString += "(";
                        queryString += empresaId;
                        queryString += ",'" + codigoPartidaPago + "'"; // Codigo de Partida
                        queryString += ",'" + codigoPartidaPago + "'";          // Codigo CC del documento a pagar
                        queryString += ",'CHEQUE'";                    // TipoDocumento
                        queryString += "," + cuentaDiferencialCambiario; // Id Nomenclatura
                        queryString += ",''";                          // SerieDocumento
                        queryString += ",'" + noCheque + "'";          // NumeroDocumento
                        queryString += ",current_date";
                        queryString += ",'" + moneda + "'";
                        queryString += "," + totalPago;                // MontoDocumento
                        queryString += ",0";                           // Debe
                        queryString += ",0";                           // Haber
                        queryString += ",1";
                        queryString += "," + (totalHaberQ - totalDebeQ); // DebeQuetzales
                        queryString += ",0";                             // HaberQuetzales
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
                    else if(totalDebeQ < totalHaberQ) {
                        //insertar linea diferencial cambiario en el debe
                        queryString += "(";
                        queryString += empresaId;
                        queryString += ",'" + codigoPartidaPago + "'"; // Codigo de Partida
                        queryString += ",'" + codigoPartidaPago + "'";          // Codigo CC del documento a pagar
                        queryString += ",'CHEQUE'";                    // TipoDocumento
                        queryString += "," + cuentaDiferencialCambiario; // Id Nomenclatura
                        queryString += ",''";                          // SerieDocumento
                        queryString += ",'" + noCheque + "'";          // NumeroDocumento
                        queryString += ",current_date";
                        queryString += ",'" + moneda + "'";
                        queryString += "," + totalPago;                // MontoDocumento
                        queryString += ",0";                           // Debe
                        queryString += ",0";                           // Haber
                        queryString += ",1";
                        queryString += ",0"; // DebeQuetzales
                        queryString += "," + (totalDebeQ - totalHaberQ);  // HaberQuetzales
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

                    //Ejecutar queryString
                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, "(1) INSERT partida : " + codigoPartidaPago + " " + queryString.substring(0, queryString.length() - 1));
                    st.executeUpdate(queryString.substring(0, queryString.length() - 1));
                    acumuladoCheque = 0.00;
                    chequeQueryString = "";
                    totalDebeQ = 0.00; totalHaberQ = 0.00;

                    //Seguir con el correlativo de codigoPartidaPago, los ultimos 3 digitos en memoria (no se ha hecho commit)
                    String ultimos3 = codigoPartidaPago.substring((codigoPartidaPago.length() - 3));
                    codigoPartidaPago = codigoPartidaPago.substring(0, codigoPartidaPago.length() - 3) + String.format("%03d", Integer.parseInt(ultimos3) + 1);
//                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, "ultimos3: " + ultimos3);
//                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, "codigoPartidaPago: " + codigoPartidaPago);
                }
                // ── LÍNEA 1: DEBE en cuenta de proveedores ────────────────────
                queryString = "INSERT INTO contabilidad_partida " + COLS;
                queryString += "(";
                queryString += empresaId;
                queryString += ",'" + codigoPartidaPago + "'"; // Codigo de Partida
                queryString += ",'" + codigoCC + "'";          // Codigo CC del documento a pagar
                queryString += ",'CHEQUE'";                    // TipoDocumento
                queryString += "," + cuentaProveedores;        // Id Nomenclatura
                queryString += ",''";                          // SerieDocumento
                queryString += ",'" + noCheque + "'";          // NumeroDocumento
                queryString += ",current_date";
                queryString += ",'" + moneda + "'";
                queryString += "," + totalPago;                // MontoDocumento
                queryString += "," + totalPago;                // Debe
                queryString += ",0";                           // Haber
                queryString += "," + tipoCambio;
                queryString += "," + (tipoCambio * totalPago);          // DebeQuetzales (saldo o haber del documento)
                queryString += ",0";                           // HaberQuetzales
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

//                totalDebeQ+= Double.parseDouble(debeQuetzalesCC);
            }
            else { // mismo proveedor
                // ── LÍNEA 1: DEBE en cuenta de proveedores ────────────────────
                queryString += "(";
                queryString += empresaId;
                queryString += ",'" + codigoPartidaPago + "'"; // Codigo de Partida
                queryString += ",'" + codigoCC + "'";          // Codigo CC del documento a pagar
                queryString += ",'CHEQUE'";                    // TipoDocumento
                queryString += "," + cuentaProveedores;        // Id Nomenclatura
                queryString += ",''";                          // SerieDocumento
                queryString += ",'" + noCheque + "'";          // NumeroDocumento
                queryString += ",current_date";
                queryString += ",'" + moneda + "'";
                queryString += "," + totalPago;                // MontoDocumento
                queryString += "," + totalPago;                // Debe
                queryString += ",0";                           // Haber
                queryString += "," + tipoCambio;
                queryString += "," + debeQuetzalesCC;          // DebeQuetzales
                queryString += ",0";                           // HaberQuetzales
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
//                totalDebeQ+= Double.parseDouble(debeQuetzalesCC);
            }

            // ── LÍNEA 2 a n : HABER en cuenta anticipos a proveedores, solamente si hay anticipos por liquidar ─────────────────────
            if(anticipo > 0.00) {
                //Buscar los anticipos vigentes o con saldo, del proveedor
                //Crear linea de HABER con cuenta Anticipos a Proveedores perso solamente de los Anticipos pendientes de liquidar o con Saldo > 0

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

//                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "-->query para buscar anticipos pendiente de liquidar del proveedor : " + queryStringAnticipo);

                rsRecords = st.executeQuery(queryStringAnticipo);

                if (rsRecords.next()) {
                    double totalAnticipoPorLiquidar = anticipo;
                    double montoAnticipo = 0.00;
                    do {
//                        System.out.println("(1)montoAnticipo: " + rsRecords.getDouble("TOTALSALDO") );
                        montoAnticipo = rsRecords.getDouble("TOTALSALDO") - (anticiposOcupadosMap.get(rsRecords.getString("CodigoCC")) != null ? anticiposOcupadosMap.get(rsRecords.getString("CodigoCC")) : 0.00);
//                        System.out.println("(2)montoAnticipo: " + montoAnticipo );

                        if(totalAnticipoPorLiquidar > 0.00 && montoAnticipo > 0.00) {

                            queryString += "(";
                            queryString += empresaId;
                            queryString += ",'" + codigoPartidaPago + "'"; // Codigo de Partida
                            queryString += ",'" + rsRecords.getString("CodigoCC") + "'"; // Codigo CC del anticipo
                            queryString += ",'CHEQUE'";                    // TipoDocumento
                            queryString += "," + cuentaAnticipos;          // Id Nomenclatura
                            queryString += ",''";                          // SerieDocumento
                            queryString += ",'" + noCheque + "'";          // NumeroDocumento
                            queryString += ",current_date";
                            queryString += ",'" + moneda + "'";
                            if (montoAnticipo <= totalAnticipoPorLiquidar) {
                                queryString += "," + montoAnticipo;        // MontoDocumento
                                queryString += ",0";                       // Debe
                                queryString += "," + montoAnticipo;        // Haber
//                                anticipoQ = moneda.equalsIgnoreCase("DOLARES")
//                                        ? montoAnticipo * tipoCambio
//                                        : montoAnticipo;
                                anticipoQ = rsRecords.getDouble("TOTALSALDOQ");
                                anticiposOcupadosMap.put(rsRecords.getString("CodigoCC"), montoAnticipo);
                            }
                            else {
                                queryString += "," + totalAnticipoPorLiquidar; // MontoDocumento
                                queryString += ",0";                           // Debe
                                queryString += "," + totalAnticipoPorLiquidar; // Haber
//                                anticipoQ = moneda.equalsIgnoreCase("DOLARES")
//                                        ? totalAnticipoPorLiquidar * tipoCambio
//                                        : totalAnticipoPorLiquidar;
                                anticipoQ = rsRecords.getDouble("TOTALSALDOQ");
                                anticiposOcupadosMap.put(rsRecords.getString("CodigoCC"), totalAnticipoPorLiquidar);
                            }
                            queryString += "," + tipoCambio;
                            queryString += ",0";                           // DebeQuetzales
                            queryString += "," + anticipoQ;                // HaberQuetzales
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

                            totalHaberQ+= anticipoQ;

//                            System.out.println("queryString Anticipo por liquidar: " + queryString);
                        }
                    } while (rsRecords.next());
                }
            }//endif anticipo > 0.00

            // ── LÍNEA n+1: HABER en cuenta bancaria, solamente si hay monto cheque ─────────────────────
            if(montoCheq > 0.00) {
                acumuladoCheque+= montoCheq;

                chequeQueryString = "(";
                chequeQueryString += empresaId;
                chequeQueryString += ",'" + codigoPartidaPago + "'"; // Codigo de Partida
                chequeQueryString += ",'" + codigoPartidaPago + "'"; // Codigo CC
                chequeQueryString += ",'CHEQUE'";                    // TipoDocumento
                chequeQueryString += "," + (moneda.equals("QUETZALES") ? cuentaBancoMonedaLocal : cuentaBancoMonedaExtranjera);        // Id Nomenclatura
                chequeQueryString += ",''";                          // SerieDocumento
                chequeQueryString += ",'" + noCheque + "'";          // NumeroDocumento
                chequeQueryString += ",current_date";
                chequeQueryString += ",'" + moneda + "'";
                chequeQueryString += "," + acumuladoCheque;                // MontoDocumento
                chequeQueryString += ",0";                           // Debe
                chequeQueryString += "," + acumuladoCheque;                // Haber
                chequeQueryString += "," + tipoCambio;
                chequeQueryString += ",0";                           // DebeQuetzales
                chequeQueryString += "," + (acumuladoCheque * tipoCambio);         // HaberQuetzales
                chequeQueryString += ",'PAGADO'";
                chequeQueryString += ",'" + descripcion + "'";
                chequeQueryString += ",'" + tipoDoca + "'";
                chequeQueryString += ",'" + numeroDoc + "'";
                chequeQueryString += "," + idProveedor;
                chequeQueryString += ",'" + proveedor.replace("'", "") + "'";
                chequeQueryString += ",'" + proveedor.replace("'", "") + "'";
                chequeQueryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                chequeQueryString += ",current_timestamp";
                chequeQueryString += "),";

                totalHaberQ+= (acumuladoCheque * tipoCambio);

            } //endif montoCheq > 0.00
        }//endfor
        //Ejecutar el ultmimo query
        //Concatenar chequeQueryString a queryString
        queryString+= chequeQueryString;
        //Ejecutar queryString
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "(2) INSERT partida : " + codigoPartidaPago + " " + queryString.substring(0, queryString.length() - 1));
        st.executeUpdate(queryString.substring(0, queryString.length() - 1));
    }

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * PASO 4 – Actualiza el último número de cheque utilizado en la chequera activa
     * del banco correspondiente.
     *
     * Solo aplica para los documentos donde A_LIQUIDAR_MONTO_CHEQUE > 0 (pagados con cheque).
     *
     *   UPDATE contabilidad_cuentas_bancos_chequera
     *   SET    UltimoUtilizado = noCheque
     *   WHERE  IdCuentaBanco = ?  AND IdEmpresa = ?
     *     AND  Del <= noCheque   AND Al >= noCheque
     *
     * TODO: validar que el número de cheque no supere el campo Al (límite de la chequera).
     *       Si supera, mostrar advertencia al usuario antes de confirmar.
     */
    private void actualizarUltimoChequeChequera(Statement st) throws SQLException {

        for (Object bancoItemId : cuentasBancosGrid.getSelectedRows()) {

            String idCuentaBanco = nvlC(cuentasBancosContainer.getContainerProperty(bancoItemId, ID_CUENTABANCO_PROPERTY).getValue());
            String monedaBanco   = nvlC(cuentasBancosContainer.getContainerProperty(bancoItemId, MONEDA_PROPERTY).getValue());

            for (Object itemId : porPagarContainer.getItemIds()) {

                String monedaDoc  = nvlC(porPagarContainer.getContainerProperty(itemId, MONEDA_PROPERTY).getValue());
                if (!monedaDoc.equalsIgnoreCase(monedaBanco)) continue;

                double montoCheque = parseMontoSF(porPagarContainer.getContainerProperty(itemId, A_LIQUIDAR_MONTO_CHEQUESF_PROPERTY).getValue());
                if (montoCheque <= 0.00) continue;  // solo pagos con cheque mueven la chequera

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
        }
    }

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retorna el IdNomenclatura de la cuenta bancaria seleccionada cuya moneda
     * coincide con la indicada. Devuelve cadena vacía si no hay banco para esa moneda.
     */
    private String obtenerNomenclaturaBancoPorMoneda(String moneda) {
        for (Object bancoItemId : cuentasBancosGrid.getSelectedRows()) {
            String monedaBanco = nvlC(cuentasBancosContainer.getContainerProperty(bancoItemId, MONEDA_PROPERTY).getValue());
            if (monedaBanco.equalsIgnoreCase(moneda)) {
                return nvlC(cuentasBancosContainer.getContainerProperty(bancoItemId, ID_NOMENCLATURA_PROPERTY).getValue());
            }
        }
        return "";
    }

    /**
     * Parsea un valor numérico almacenado en el container.
     * Elimina símbolos de moneda, comas y espacios antes de convertir.
     */
    private double parseMontoSF(Object value) {
        try {
            if (value == null) return 0.00;
            String s = String.valueOf(value).replaceAll("[^0-9.]", "");
            return s.isEmpty() ? 0.00 : Double.parseDouble(s);
        } catch (NumberFormatException ex) {
            return 0.00;
        }
    }

    /** Retorna cadena vacía si el valor del container es null, de lo contrario su toString(). */
    private String nvlC(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    // =========================================================================

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event
    ) {
        ((SopdiUI) UI.getCurrent()).lblEmpresaYFormulario.setValue(empresaId + " " + empresaNombre + " Autorizar Pagos Especiales");
        Page.getCurrent().setTitle("Sopdi - Pagos especiales");
    }
}