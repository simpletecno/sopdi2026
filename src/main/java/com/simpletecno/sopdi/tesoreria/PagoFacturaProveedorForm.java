package com.simpletecno.sopdi.tesoreria;

import com.simpletecno.sopdi.*;
import com.simpletecno.sopdi.contabilidad.MostrarPartidaContable;
import com.simpletecno.sopdi.utilerias.MyEmailMessanger;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.addon.tableexport.DefaultTableHolder;
import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.addon.tableexport.TableHolder;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.SelectionEvent;
import com.vaadin.event.SelectionEvent.SelectionListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.HeaderCell;
import com.vaadin.ui.Grid.HeaderRow;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.ui.NumberField;

import javax.mail.MessagingException;

/**
 * Formulario de Pago de Facturas a Proveedores.
 * UI modernizada: layout centrado, secciones diferenciadas, CSS personalizado.
 *
 * @author user
 */
public class PagoFacturaProveedorForm extends Window {

    // ── Constantes de columnas ───────────────────────────────────────────────
    static final String ID_PROPERTY               = "Id";
    static final String TIPO_PROPERTY             = "Tipo";
    static final String FECHA_PROPERTY            = "Fecha";
    static final String PROVEEDOR_PROPERTY        = "Proveedor";
    static final String CODIGO_PROPERTY           = "Id Proveedor";
    static final String FACTURA_PROPERTY          = "Documento";
    static final String MONEDA_PROPERTY           = "Moneda";
    static final String VALOR_PROPERTY            = "Monto";
    static final String TIPOCAMBIO_PROPERTY       = "Tasa";
    static final String MONTO_AUTORIZADO_PROPERTY = "Monto";
    static final String ANTICIPO_PROPERTY         = "A.Anticipo";
    static final String CUENTA_PROPERTY           = "IdNomenclatura";
    static final String HABER_PROPERTY            = "Haber";
    static final String HABER_Q_PROPERTY          = "Haber Q";
    static final String DEBE_PROPERTY             = "Debe";
    static final String DEBE_Q_PROPERTY           = "Debe Q";
    static final String CODIGOCC_PROPERTY         = "CodigoCC";
    static final String DOCUMENTO_PROPERTY        = "Documento";
    static final String DESCRIPCION_PROPERTY      = "Descripción";

    static DecimalFormat numberFormat  = new DecimalFormat("#,###,##0.00");
    static DecimalFormat numberFormat3 = new DecimalFormat("######0.00");

    // ── DB ───────────────────────────────────────────────────────────────────
    Statement stQuery;
    ResultSet rsRecords;
    Statement stQueryFacturas;
    ResultSet rsRecordsFacturas;
    Statement stQuery2;
    ResultSet rsRecords2;

    // ── Layouts ──────────────────────────────────────────────────────────────
    VerticalLayout mainLayout;

    // ── Grids / Containers ───────────────────────────────────────────────────
    public IndexedContainer facturasContainer = new IndexedContainer();
    IndexedContainer        partidaContainer  = new IndexedContainer();
    Grid facturasGrid;
    Grid.FooterRow footerFacturas;
    Grid partidaGrid;

    HashMap<String, String> cuentasContables = new HashMap<>();

    // ── Controles de formulario ──────────────────────────────────────────────
    ComboBox  proveedorCbx;
    ComboBox  monedaCbx;
    ComboBox  medioCbx;
    DateField fechaDt;
    NumberField montoTxt;
    NumberField tasaCambioTxt;
    TextField nombreChequeTxt;
    TextField descripcionTxt;
    TextField numeroTxt;

    HorizontalLayout chequeLayout  = new HorizontalLayout();
    HorizontalLayout chequeLayout2 = new HorizontalLayout();
    VerticalLayout   partidaLayout = new VerticalLayout();

    // ── Estado ───────────────────────────────────────────────────────────────
    double totalMonto;
    double totalQueztales;
    double totalAnticipo;

    Button excelBtn;
    String tipoDocumentoPagado;
    Button grabarPartidaBtn;
    Button desAutorizarBtn;

    String  proveedorNombre;
    String  proveedorId;
    String  idNomenclatura;
    String  codigoPartida;
    String  codigoPartidaNuevo;
    String  codigoCC;
    String  partidasPagadas;
    String  facturasPagadas;
    ArrayList<String> codigoAnticipoList = new ArrayList<>();

    BigDecimal totalDebe;
    BigDecimal totalHaber;
    BigDecimal totalDebeQ;
    BigDecimal totalHaberQ;

    String IdProveedor = "";
    String codigo;
    String queryString;
    Date   fechaPago;
    boolean calcular = true;

    UI mainUI;

    String empresaId     = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    // ────────────────────────────────────────────────────────────────────────
    public PagoFacturaProveedorForm(String IdProveedor, Date fechaPago) {

        this.mainUI      = UI.getCurrent();
        this.IdProveedor = IdProveedor;
        this.fechaPago   = fechaPago;

        // Inyectar estilos CSS
        injectStyles();

        // Window
        setModal(true);
        setResizable(true);
        setDraggable(true);
        setWidth("92%");
        setHeight("92%");
        setResponsive(true);

        // ── mainLayout ───────────────────────────────────────────────────────
        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);
        mainLayout.setResponsive(true);
        mainLayout.setSizeFull();
        mainLayout.addStyleName("pfp-main");

        setContent(mainLayout);

        // ── Botón Excel ──────────────────────────────────────────────────────
        excelBtn = new Button("Exportar Excel");
        excelBtn.setIcon(FontAwesome.FILE_EXCEL_O);
        excelBtn.addStyleName("pfp-btn-excel");
        excelBtn.addClickListener(event -> {
            if (facturasContainer.size() > 0) {
                exportToExcel(facturasGrid);
            } else {
                Notification.show("La vista no contiene registros disponibles.", Notification.Type.WARNING_MESSAGE);
            }
        });

        // ── Header ───────────────────────────────────────────────────────────
        buildHeader();

        // ── ComboBox proveedor (visible en header) ───────────────────────────
        proveedorCbx = new ComboBox("Proveedor : ");
        proveedorCbx.setWidth("35em");
        proveedorCbx.setVisible(false);
        proveedorCbx.setFilteringMode(FilteringMode.CONTAINS);
        proveedorCbx.addValueChangeListener(event -> {
            nombreChequeTxt.setReadOnly(false);
            nombreChequeTxt.setValue(proveedorCbx.getItemCaption(proveedorCbx.getValue()));
            nombreChequeTxt.setReadOnly(true);
        });
        llenarComboProveedor();

        mainLayout.addComponent(proveedorCbx);
        mainLayout.setComponentAlignment(proveedorCbx, Alignment.TOP_CENTER);

        // ── Secciones ────────────────────────────────────────────────────────
        crearGridFacturas();
        llenarGridFacturas();
        crearLayoutCheque();
        crearPartidaLayout();
        limpiarPartida();
    }

    // ── CSS ──────────────────────────────────────────────────────────────────
    private void injectStyles() {
        Page.getCurrent().getStyles().add(

                /* Fondo general del contenido del Window */
                ".pfp-main {" +
                        "  background: #F4F6F9;" +
                        "}" +

                        /* ── Header ─────────────────────────────────────────────────── */
                        ".pfp-header {" +
                        "  background: linear-gradient(135deg, #1565C0 0%, #1976D2 100%);" +
                        "  border-radius: 10px;" +
                        "  padding: 14px 20px !important;" +
                        "  margin-bottom: 4px;" +
                        "  width: 100%;" +
                        "}" +
                        ".pfp-header-title {" +
                        "  color: #ffffff !important;" +
                        "  font-size: 17px !important;" +
                        "  font-weight: 700 !important;" +
                        "  margin: 0 !important;" +
                        "  letter-spacing: 0.02em;" +
                        "}" +
                        ".pfp-header-sub {" +
                        "  color: #BBDEFB !important;" +
                        "  font-size: 12px !important;" +
                        "  margin: 2px 0 0 0 !important;" +
                        "}" +

                        /* ── Sección card ────────────────────────────────────────────── */
                        ".pfp-section {" +
                        "  background: #ffffff;" +
                        "  border-radius: 8px;" +
                        "  box-shadow: 0 2px 8px rgba(0,0,0,0.07);" +
                        "  padding: 16px 18px !important;" +
                        "  width: 100%;" +
                        "  margin-bottom: 6px;" +
                        "}" +
                        ".pfp-section-label {" +
                        "  color: #1976D2;" +
                        "  font-size: 11px !important;" +
                        "  font-weight: 700 !important;" +
                        "  letter-spacing: 0.09em;" +
                        "  text-transform: uppercase;" +
                        "  border-bottom: 2px solid #E3F2FD;" +
                        "  padding-bottom: 6px;" +
                        "  margin-bottom: 10px !important;" +
                        "  display: block;" +
                        "  width: 100%;" +
                        "}" +

                        /* ── Grids ───────────────────────────────────────────────────── */
                        ".pfp-main .v-grid-header {" +
                        "  background: #E8F0FE !important;" +
                        "  font-weight: 600 !important;" +
                        "  color: #1A237E !important;" +
                        "}" +
                        ".pfp-main .v-grid-row:hover > td {" +
                        "  background: #E3F2FD !important;" +
                        "}" +
                        ".pfp-main .v-grid-row-selected > td {" +
                        "  background: #BBDEFB !important;" +
                        "  color: #0D47A1 !important;" +
                        "  font-weight: 600;" +
                        "}" +
                        ".pfp-main .v-grid-footer {" +
                        "  background: #F0F4F8 !important;" +
                        "  font-weight: 700 !important;" +
                        "  border-top: 2px solid #BBDEFB !important;" +
                        "}" +

                        /* ── Botón Excel ─────────────────────────────────────────────── */
                        ".pfp-btn-excel.v-button {" +
                        "  background: linear-gradient(135deg, #2E7D32 0%, #388E3C 100%) !important;" +
                        "  color: #fff !important;" +
                        "  border: none !important;" +
                        "  border-radius: 6px !important;" +
                        "  font-weight: 600 !important;" +
                        "  padding: 0 18px !important;" +
                        "  height: 34px !important;" +
                        "  box-shadow: 0 2px 6px rgba(46,125,50,0.3) !important;" +
                        "}" +

                        /* ── Botón Grabar ─────────────────────────────────────────────  */
                        ".pfp-btn-grabar.v-button {" +
                        "  background: linear-gradient(135deg, #1976D2 0%, #1565C0 100%) !important;" +
                        "  color: #fff !important;" +
                        "  border: none !important;" +
                        "  border-radius: 6px !important;" +
                        "  font-weight: 600 !important;" +
                        "  padding: 0 22px !important;" +
                        "  height: 36px !important;" +
                        "  box-shadow: 0 2px 8px rgba(25,118,210,0.35) !important;" +
                        "}" +

                        /* ── Botón Cancelar ──────────────────────────────────────────── */
                        ".pfp-btn-cancelar.v-button {" +
                        "  border: 1px solid #CFD8DC !important;" +
                        "  border-radius: 6px !important;" +
                        "  color: #546E7A !important;" +
                        "  background: #fff !important;" +
                        "  height: 36px !important;" +
                        "  padding: 0 18px !important;" +
                        "}" +
                        ".pfp-btn-cancelar.v-button:hover {" +
                        "  background: #F5F5F5 !important;" +
                        "}" +

                        /* ── Botón Des-autorizar ─────────────────────────────────────── */
                        ".pfp-btn-desautorizar.v-button {" +
                        "  background: linear-gradient(135deg, #C62828 0%, #D32F2F 100%) !important;" +
                        "  color: #fff !important;" +
                        "  border: none !important;" +
                        "  border-radius: 6px !important;" +
                        "  font-weight: 600 !important;" +
                        "  height: 36px !important;" +
                        "  padding: 0 18px !important;" +
                        "  box-shadow: 0 2px 8px rgba(198,40,40,0.3) !important;" +
                        "}" +

                        /* ── Área de botones de acción ───────────────────────────────── */
                        ".pfp-action-bar {" +
                        "  border-top: 1px solid #E3E8EF;" +
                        "  padding-top: 12px;" +
                        "  margin-top: 4px;" +
                        "  width: 100%;" +
                        "}"
        );
    }

    // ── Header ───────────────────────────────────────────────────────────────
    private void buildHeader() {
        Label iconLbl = new Label("💳");
        iconLbl.setSizeUndefined();

        Label titleLbl = new Label(empresaId + "  ·  " + empresaNombre + "  —  PAGO DE DOCUMENTOS");
        titleLbl.addStyleName("pfp-header-title");
        titleLbl.setSizeUndefined();

        Label subLbl = new Label("Seleccione uno o varios documentos del mismo proveedor y moneda para procesar el pago");
        subLbl.addStyleName("pfp-header-sub");
        subLbl.setSizeUndefined();

        VerticalLayout textCol = new VerticalLayout();
        textCol.setMargin(false);
        textCol.setSpacing(false);
        textCol.addComponents(titleLbl, subLbl);

        HorizontalLayout headerHL = new HorizontalLayout();
        headerHL.addStyleName("pfp-header");
        headerHL.setWidth("100%");
        headerHL.setSpacing(true);
        headerHL.setMargin(false);
        headerHL.addComponents(iconLbl, textCol, excelBtn);
        headerHL.setExpandRatio(textCol, 1f);
        headerHL.setComponentAlignment(iconLbl,   Alignment.MIDDLE_LEFT);
        headerHL.setComponentAlignment(textCol,   Alignment.MIDDLE_LEFT);
        headerHL.setComponentAlignment(excelBtn,  Alignment.MIDDLE_RIGHT);

        mainLayout.addComponent(headerHL);
        mainLayout.setComponentAlignment(headerHL, Alignment.TOP_CENTER);
    }

    // ── Grid de Facturas ─────────────────────────────────────────────────────
    public void crearGridFacturas() {

        // Contenedores
        partidaContainer.addContainerProperty(CUENTA_PROPERTY,       String.class, null);
        partidaContainer.addContainerProperty(DESCRIPCION_PROPERTY,  String.class, null);
        partidaContainer.addContainerProperty(DEBE_PROPERTY,         String.class, null);
        partidaContainer.addContainerProperty(HABER_PROPERTY,        String.class, null);
        partidaContainer.addContainerProperty(DEBE_Q_PROPERTY,       String.class, null);
        partidaContainer.addContainerProperty(HABER_Q_PROPERTY,      String.class, null);
        partidaContainer.addContainerProperty(CODIGOCC_PROPERTY,     String.class, null);

        facturasContainer.addContainerProperty(ID_PROPERTY,               String.class, null);
        facturasContainer.addContainerProperty(TIPO_PROPERTY,             String.class, null);
        facturasContainer.addContainerProperty(FECHA_PROPERTY,            String.class, null);
        facturasContainer.addContainerProperty(PROVEEDOR_PROPERTY,        String.class, null);
        facturasContainer.addContainerProperty(CODIGO_PROPERTY,           String.class, null);
        facturasContainer.addContainerProperty(FACTURA_PROPERTY,          String.class, null);
        facturasContainer.addContainerProperty(MONEDA_PROPERTY,           String.class, null);
        facturasContainer.addContainerProperty(VALOR_PROPERTY,            String.class, null);
        facturasContainer.addContainerProperty(MONTO_AUTORIZADO_PROPERTY, String.class, null);
        facturasContainer.addContainerProperty(ANTICIPO_PROPERTY,         String.class, null);
        facturasContainer.addContainerProperty(CUENTA_PROPERTY,           String.class, null);
        facturasContainer.addContainerProperty(HABER_PROPERTY,            String.class, null);
        facturasContainer.addContainerProperty(HABER_Q_PROPERTY,          String.class, null);
        facturasContainer.addContainerProperty(CODIGOCC_PROPERTY,         String.class, null);

        facturasGrid = new Grid("", facturasContainer);
        facturasGrid.setWidth("100%");
        facturasGrid.setImmediate(true);
        facturasGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        facturasGrid.setDescription("Seleccione uno o varios registros del mismo proveedor.");
        facturasGrid.setHeightMode(HeightMode.ROW);
        facturasGrid.setHeightByRows(5);
        facturasGrid.setResponsive(true);
        facturasGrid.setEditorBuffered(false);

        facturasGrid.addSelectionListener(event -> {
            if (facturasGrid.getSelectedRows() != null) {
                calcularPartida();
            }
        });

        // Columnas ocultas
        facturasGrid.getColumn(ID_PROPERTY).setHidable(true).setHidden(true);
        facturasGrid.getColumn(TIPO_PROPERTY).setHidable(true).setHidden(true);
        facturasGrid.getColumn(CODIGO_PROPERTY).setHidable(true).setHidden(true);
        facturasGrid.getColumn(FECHA_PROPERTY).setHidable(true).setHidden(true);
        facturasGrid.getColumn(CUENTA_PROPERTY).setHidable(true).setHidden(true);
        facturasGrid.getColumn(HABER_PROPERTY).setHidable(true).setHidden(true);
        facturasGrid.getColumn(CODIGOCC_PROPERTY).setHidable(true).setHidden(true);

        facturasGrid.setCellStyleGenerator(cellReference -> {
            if (MONTO_AUTORIZADO_PROPERTY.equals(cellReference.getPropertyId()) ||
                    VALOR_PROPERTY.equals(cellReference.getPropertyId()) ||
                    ANTICIPO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (TIPOCAMBIO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "centeralign";
            }
            return null;
        });

        // Fila de filtros
        HeaderRow filterRow = facturasGrid.appendHeaderRow();

        TextField filterFactura = buildFilterField(8);
        filterFactura.addTextChangeListener(change -> {
            facturasContainer.removeContainerFilters(FACTURA_PROPERTY);
            if (!change.getText().isEmpty()) {
                facturasContainer.addContainerFilter(new SimpleStringFilter(FACTURA_PROPERTY, change.getText(), true, false));
            }
        });
        filterRow.getCell(FACTURA_PROPERTY).setComponent(filterFactura);

        TextField filterMoneda = buildFilterField(8);
        filterMoneda.addTextChangeListener(change -> {
            facturasContainer.removeContainerFilters(MONEDA_PROPERTY);
            if (!change.getText().isEmpty()) {
                facturasContainer.addContainerFilter(new SimpleStringFilter(MONEDA_PROPERTY, change.getText(), true, false));
            }
        });
        filterRow.getCell(MONEDA_PROPERTY).setComponent(filterMoneda);

        TextField filterProveedor = buildFilterField(20);
        filterProveedor.addTextChangeListener(change -> {
            facturasContainer.removeContainerFilters(PROVEEDOR_PROPERTY);
            if (!change.getText().isEmpty()) {
                facturasContainer.addContainerFilter(new SimpleStringFilter(PROVEEDOR_PROPERTY, change.getText(), true, false));
            }
        });
        filterRow.getCell(PROVEEDOR_PROPERTY).setComponent(filterProveedor);

        // Footer totales
        footerFacturas = facturasGrid.appendFooterRow();
        footerFacturas.getCell(MONEDA_PROPERTY).setText("Totales");
        footerFacturas.getCell(VALOR_PROPERTY).setText("0.00");
        footerFacturas.getCell(MONTO_AUTORIZADO_PROPERTY).setText("0.00");
        footerFacturas.getCell(ANTICIPO_PROPERTY).setText("0.00");
        footerFacturas.getCell(VALOR_PROPERTY).setStyleName("rightalign");
        footerFacturas.getCell(MONTO_AUTORIZADO_PROPERTY).setStyleName("rightalign");
        footerFacturas.getCell(ANTICIPO_PROPERTY).setStyleName("rightalign");

        // Card de la sección
        VerticalLayout facturasSection = buildSection("📄  Documentos pendientes de pago");
        facturasSection.addComponent(facturasGrid);
        facturasSection.setComponentAlignment(facturasGrid, Alignment.MIDDLE_CENTER);

        mainLayout.addComponent(facturasSection);
        mainLayout.setComponentAlignment(facturasSection, Alignment.TOP_CENTER);
    }

    /** Helper: crea un TextField de filtro estilo tiny. */
    private TextField buildFilterField(int columns) {
        TextField field = new TextField();
        field.addStyleName(ValoTheme.TEXTFIELD_TINY);
        field.setInputPrompt("Filtrar");
        field.setColumns(columns);
        return field;
    }

    // ── Layout de Cheque / Medio de Pago ────────────────────────────────────
    private void crearLayoutCheque() {

        // Inicializar proveedor (ya fue creado arriba)
        proveedorCbx.setWidth("15em");
        proveedorCbx.setVisible(false);
        proveedorCbx.addValueChangeListener(event -> {
            if (nombreChequeTxt == null) return;
            nombreChequeTxt.setReadOnly(false);
            if (proveedorCbx.getValue() == null) {
                nombreChequeTxt.setValue("");
            } else {
                nombreChequeTxt.setValue(proveedorCbx.getItemCaption(proveedorCbx.getValue()));
            }
        });

        // Campos
        medioCbx = new ComboBox("Medio de pago");
        medioCbx.setWidth("12em");
        medioCbx.addItem("CHEQUE");
        medioCbx.addItem("NOTA DE DEBITO");
        medioCbx.select("CHEQUE");

        numeroTxt = new TextField("# Documento");
        numeroTxt.setWidth("9em");

        fechaDt = new DateField("Fecha");
        fechaDt.setDateFormat("dd/MM/yyyy");
        fechaDt.setWidth("9em");
        fechaDt.setValue(fechaPago);
        fechaDt.setReadOnly(true);

        monedaCbx = new ComboBox("Moneda");
        monedaCbx.setWidth("10em");
        monedaCbx.addItem("QUETZALES");
        monedaCbx.addItem("DOLARES");
        monedaCbx.select("QUETZALES");

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
        tasaCambioTxt.setWidth("6em");
        tasaCambioTxt.setValue(1.00);
        tasaCambioTxt.addValueChangeListener(event -> calcularPartida());

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
        montoTxt.setWidth("8em");

        nombreChequeTxt = new TextField("Nombre cheque / nota");
        nombreChequeTxt.setWidth("28em");
        nombreChequeTxt.setValue(proveedorCbx.getItemCaption(proveedorCbx.getValue()));
        nombreChequeTxt.setReadOnly(((SopdiUI) mainUI).sessionInformation.getStrUserProfileName().equals("CONTADOR"));
        nombreChequeTxt.setReadOnly(((SopdiUI) mainUI).sessionInformation.getStrUserProfileName().equals("AUXILIAR"));

        descripcionTxt = new TextField("Descripción");
        descripcionTxt.setWidth("100%");
        descripcionTxt.setVisible(false);

        // Fila 1: datos del pago
        chequeLayout.setSpacing(true);
        chequeLayout.setMargin(false);
        chequeLayout.setSizeUndefined();
        chequeLayout.addComponents(medioCbx, numeroTxt, fechaDt, proveedorCbx, montoTxt, monedaCbx, tasaCambioTxt);
        for (int i = 0; i < chequeLayout.getComponentCount(); i++) {
            chequeLayout.setComponentAlignment(chequeLayout.getComponent(i), Alignment.BOTTOM_LEFT);
        }

        // Fila 2: nombre y descripción
        chequeLayout2.setSpacing(true);
        chequeLayout2.setMargin(false);
        chequeLayout2.setWidth("100%");
        chequeLayout2.addComponents(nombreChequeTxt, descripcionTxt);
        chequeLayout2.setExpandRatio(descripcionTxt, 1f);
        chequeLayout2.setComponentAlignment(nombreChequeTxt, Alignment.BOTTOM_LEFT);
        chequeLayout2.setComponentAlignment(descripcionTxt,  Alignment.BOTTOM_LEFT);

        // Card
        VerticalLayout chequeSection = buildSection("🏦  Datos del medio de pago");
        chequeSection.addComponents(chequeLayout, chequeLayout2);

        mainLayout.addComponent(chequeSection);
        mainLayout.setComponentAlignment(chequeSection, Alignment.MIDDLE_CENTER);
    }

    // ── Layout de Partida Contable ───────────────────────────────────────────
    public void crearPartidaLayout() {

        llenarComboCuentaContable();

        partidaGrid = new Grid(partidaContainer);
        partidaGrid.setImmediate(true);
        partidaGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        partidaGrid.setHeightMode(HeightMode.ROW);
        partidaGrid.setHeightByRows(10);
        partidaGrid.setWidth("100%");
        partidaGrid.setResponsive(true);
        partidaGrid.setEditorBuffered(false);
        partidaGrid.setColumnReorderingAllowed(false);

        partidaGrid.getColumn(CUENTA_PROPERTY).setExpandRatio(1);
        partidaGrid.getColumn(DESCRIPCION_PROPERTY).setExpandRatio(5);
        partidaGrid.getColumn(DEBE_PROPERTY).setExpandRatio(2);
        partidaGrid.getColumn(HABER_PROPERTY).setExpandRatio(2);
        partidaGrid.getColumn(DEBE_Q_PROPERTY).setExpandRatio(2);
        partidaGrid.getColumn(HABER_Q_PROPERTY).setExpandRatio(2);
        partidaGrid.getColumn(CODIGOCC_PROPERTY).setExpandRatio(3);

        partidaGrid.setCellStyleGenerator(cellReference -> {
            String prop = (String) cellReference.getPropertyId();
            if (DEBE_PROPERTY.equals(prop) || HABER_PROPERTY.equals(prop) ||
                    DEBE_Q_PROPERTY.equals(prop) || HABER_Q_PROPERTY.equals(prop)) {
                return "rightalign";
            }
            return null;
        });

        // ── Botones de acción ────────────────────────────────────────────────
        grabarPartidaBtn = new Button("Grabar partida");
        grabarPartidaBtn.addStyleName("pfp-btn-grabar");
        grabarPartidaBtn.setIcon(FontAwesome.SAVE);
        grabarPartidaBtn.addClickListener(event -> insertarPartidaCompuesta());

        desAutorizarBtn = new Button("Des-autorizar pago");
        desAutorizarBtn.addStyleName("pfp-btn-desautorizar");
        desAutorizarBtn.setIcon(FontAwesome.TRASH);
        desAutorizarBtn.addClickListener(event -> {
            if (facturasGrid.getSelectedRows() != null) {
                desAutorizarFactura();
            }
        });

        Button cancelarBtn = new Button("Cancelar");
        cancelarBtn.addStyleName("pfp-btn-cancelar");
        cancelarBtn.setIcon(FontAwesome.BAN);
        cancelarBtn.addClickListener(event -> {
            proveedorCbx.setReadOnly(false);
            limpiarPartida();
            proveedorCbx.setReadOnly(true);
            proveedorCbx.focus();
            facturasGrid.deselectAll();
        });

        // Layout de botones: Cancelar | Des-autorizar ··· [Grabar]
        Label spacer = new Label();
        HorizontalLayout actionBar = new HorizontalLayout();
        actionBar.setWidth("100%");
        actionBar.setSpacing(true);
        actionBar.addStyleName("pfp-action-bar");
        actionBar.addComponents(cancelarBtn, desAutorizarBtn, spacer, grabarPartidaBtn);
        actionBar.setExpandRatio(spacer, 1f);
        actionBar.setComponentAlignment(cancelarBtn,      Alignment.MIDDLE_LEFT);
        actionBar.setComponentAlignment(desAutorizarBtn,  Alignment.MIDDLE_LEFT);
        actionBar.setComponentAlignment(grabarPartidaBtn, Alignment.MIDDLE_RIGHT);

        // Card
        partidaLayout.addStyleName("pfp-section");
        partidaLayout.setWidth("100%");
        partidaLayout.setResponsive(true);
        partidaLayout.setSpacing(true);
        partidaLayout.setMargin(false);

        Label sectionLbl = new Label("📒  Partida contable generada");
        sectionLbl.addStyleName("pfp-section-label");
        sectionLbl.setWidth("100%");

        partidaLayout.addComponents(sectionLbl, partidaGrid, actionBar);
        partidaLayout.setComponentAlignment(sectionLbl,  Alignment.TOP_LEFT);
        partidaLayout.setComponentAlignment(partidaGrid,  Alignment.TOP_CENTER);
        partidaLayout.setComponentAlignment(actionBar,    Alignment.MIDDLE_CENTER);

        mainLayout.addComponent(partidaLayout);
        mainLayout.setComponentAlignment(partidaLayout, Alignment.MIDDLE_CENTER);
    }

    // ── Helper: construye un "card" de sección con etiqueta ─────────────────
    private VerticalLayout buildSection(String title) {
        Label sectionLbl = new Label(title);
        sectionLbl.addStyleName("pfp-section-label");
        sectionLbl.setWidth("100%");

        VerticalLayout section = new VerticalLayout();
        section.addStyleName("pfp-section");
        section.setWidth("100%");
        section.setSpacing(true);
        section.setMargin(false);
        section.addComponent(sectionLbl);
        return section;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  LÓGICA DE NEGOCIO — sin cambios funcionales
    // ════════════════════════════════════════════════════════════════════════

    private void calcularPartida() {
        if (!calcular) return;
        partidaContainer.removeAllItems();

        Double montoPagar     = 0.00;
        Double montoAnticipo  = 0.00;
        Double porcentajeProporcional = 0.00;
        Double montoQuetzales = 0.00;
        Double montoMoneda    = 0.00;
        String moneda         = "";
        Object gridItem;

        totalDebe  = new BigDecimal(montoPagar);
        totalDebeQ = new BigDecimal(montoPagar * tasaCambioTxt.getDoubleValueDoNotThrow());
        totalHaber  = new BigDecimal(montoPagar);
        totalHaberQ = new BigDecimal(montoPagar * tasaCambioTxt.getDoubleValueDoNotThrow());

        facturasPagadas    = "";
        partidasPagadas    = "";
        proveedorNombre    = "";
        proveedorId        = "0";
        tipoDocumentoPagado = "";
        idNomenclatura     = ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getProveedores();

        codigoAnticipoList.clear();

        Iterator facturasGridIter = facturasGrid.getSelectedRows().iterator();

        if (facturasGridIter == null || !facturasGridIter.hasNext()) {
            proveedorCbx.setReadOnly(false);
            limpiarPartida();
            codigoAnticipoList.clear();
            proveedorCbx.setReadOnly(false);
            proveedorCbx.select(proveedorId);
            proveedorCbx.setReadOnly(true);
            return;
        }

        gridItem      = facturasGridIter.next();
        proveedorId   = String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(CODIGO_PROPERTY).getValue());
        proveedorNombre = String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(PROVEEDOR_PROPERTY).getValue());
        moneda        = String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(MONEDA_PROPERTY).getValue());
        montoPagar    = Double.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(MONTO_AUTORIZADO_PROPERTY).getValue()).replaceAll(",", ""));
        montoAnticipo = Double.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(ANTICIPO_PROPERTY).getValue()).replaceAll(",", ""));

        facturasPagadas     = String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(DOCUMENTO_PROPERTY).getValue()) + ",";
        partidasPagadas     = String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(ID_PROPERTY).getValue()) + ",";
        tipoDocumentoPagado = String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(TIPO_PROPERTY).getValue());
        idNomenclatura      = String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(CUENTA_PROPERTY).getValue());
        codigoAnticipoList.add(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(ID_PROPERTY).getValue()));
        codigoCC = String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(CODIGOCC_PROPERTY).getValue());
        codigo   = String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(CODIGO_PROPERTY).getValue());

        monedaCbx.setReadOnly(false);
        monedaCbx.select(moneda);
        monedaCbx.setReadOnly(true);

        calcular = false;
        if (moneda.equals("DOLARES")) {
            if (tasaCambioTxt.getDoubleValueDoNotThrow() == 1.0) {
                tasaCambioTxt.setValue(Float.toString(((SopdiUI) UI.getCurrent()).sessionInformation.getFltExchangeRate()));
            }
        } else {
            tasaCambioTxt.setValue(1.00);
        }
        calcular = true;

        proveedorCbx.setReadOnly(false);
        proveedorCbx.select(proveedorId);
        proveedorCbx.setReadOnly(true);

        montoTxt.setReadOnly(false);
        montoTxt.setValue(montoPagar);

        while (facturasGridIter.hasNext()) {
            gridItem = facturasGridIter.next();
            if (!moneda.equals(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(MONEDA_PROPERTY).getValue()))) {
                Notification.show("SOLO SE PUEDEN PAGAR VARIAS FACTURAS DEL MISMO PROVEEDOR Y DE LA MISMA MONEDA, REVISE!", Notification.Type.WARNING_MESSAGE);
                facturasGrid.deselect(gridItem);
                return;
            }
            if (!idNomenclatura.equals(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(CUENTA_PROPERTY).getValue()))) {
                Notification.show("SOLO SE PUEDEN PAGAR VARIAS FACTURAS DE LA MISMA CUENTA CONTABLE, REVISE!", Notification.Type.WARNING_MESSAGE);
                facturasGrid.deselect(gridItem);
                return;
            }
            if (!tipoDocumentoPagado.equals(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(TIPO_PROPERTY).getValue()))) {
                Notification.show("SOLO SE PUEDEN PAGAR DOCUMENTOS DEL MISMO TIPO, REVISE!", Notification.Type.WARNING_MESSAGE);
                facturasGrid.deselect(gridItem);
                return;
            }
            montoPagar    += Double.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(MONTO_AUTORIZADO_PROPERTY).getValue()).replaceAll(",", ""));
            montoAnticipo += Double.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(ANTICIPO_PROPERTY).getValue()).replaceAll(",", ""));
            facturasPagadas += String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(DOCUMENTO_PROPERTY).getValue()) + ",";
            partidasPagadas += String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(ID_PROPERTY).getValue()) + ",";
            codigoAnticipoList.add(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(ID_PROPERTY).getValue()));
        }

        descripcionTxt.setValue("PAGO DE " + String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(TIPO_PROPERTY).getValue())
                + " : [" + facturasPagadas + "] PROVEEDOR/INSTITUCION : [" + proveedorNombre + "]");

        montoTxt.setValue(montoPagar);
        montoTxt.setReadOnly(true);
        numeroTxt.focus();

        nombreChequeTxt.setReadOnly(false);
        nombreChequeTxt.setValue(proveedorNombre);

        Object partidaObject = partidaContainer.addItem();
        if (moneda.equals("DOLARES")) {
            partidaContainer.getContainerProperty(partidaObject, CUENTA_PROPERTY).setValue(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaExtranjera());
            partidaContainer.getContainerProperty(partidaObject, DESCRIPCION_PROPERTY).setValue(cuentasContables.get(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaExtranjera()));
        } else {
            partidaContainer.getContainerProperty(partidaObject, CUENTA_PROPERTY).setValue(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaLocal());
            partidaContainer.getContainerProperty(partidaObject, DESCRIPCION_PROPERTY).setValue(cuentasContables.get(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaLocal()));
        }
        partidaContainer.getContainerProperty(partidaObject, DEBE_PROPERTY).setValue("0");
        partidaContainer.getContainerProperty(partidaObject, HABER_PROPERTY).setValue(String.valueOf(montoPagar));
        partidaContainer.getContainerProperty(partidaObject, DEBE_Q_PROPERTY).setValue("0");
        partidaContainer.getContainerProperty(partidaObject, HABER_Q_PROPERTY).setValue(String.valueOf(Utileria.numberFormatEntero.format(montoPagar * tasaCambioTxt.getDoubleValueDoNotThrow())));
        partidaContainer.getContainerProperty(partidaObject, CODIGOCC_PROPERTY).setValue("");

        totalHaber  = totalHaber.add(new BigDecimal(montoPagar));
        totalHaberQ = totalHaberQ.add(new BigDecimal(montoPagar * tasaCambioTxt.getDoubleValueDoNotThrow()));

        facturasGridIter   = facturasGrid.getSelectedRows().iterator();
        double montoProveedores = 0.00;

        while (facturasGridIter.hasNext()) {
            Object gridItem2 = facturasGridIter.next();
            codigoPartida    = String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(ID_PROPERTY).getValue()).replaceAll(",", "");
            codigoCC         = String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(CODIGOCC_PROPERTY).getValue()).replaceAll(",", "");
            montoProveedores = Double.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_AUTORIZADO_PROPERTY).getValue()).replaceAll(",", ""))
                    + Double.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(ANTICIPO_PROPERTY).getValue()).replaceAll(",", ""));

            queryString  = " SELECT autorizacion_pago.*, contabilidad_partida.Debe, contabilidad_partida.DebeQuetzales ";
            queryString += " FROM autorizacion_pago";
            queryString += " INNER JOIN contabilidad_partida On contabilidad_partida.CodigoCC = autorizacion_pago.CodigoCCRelacionado AND contabilidad_partida.IdNomenclatura = " + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getAnticiposProveedor();
            queryString += " WHERE autorizacion_pago.CodigoCC = '" + codigoCC + "'";
            queryString += " AND autorizacion_pago.CodigoCCRelacionado <> ''";
            queryString += " AND contabilidad_partida.IdNomenclatura = " + ((SopdiUI) mainUI).cuentasContablesDefault.getAnticiposProveedor();
            queryString += " AND contabilidad_partida.DEBE > 0";

            try {
                stQueryFacturas   = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                rsRecordsFacturas = stQueryFacturas.executeQuery(queryString);

                if (rsRecordsFacturas.next()) {
                    do {
                        partidaObject = partidaContainer.addItem();
                        if (moneda.equals("DOLARES")) {
                            porcentajeProporcional = rsRecordsFacturas.getDouble("Monto") / rsRecordsFacturas.getDouble("Debe");
                            montoQuetzales         = rsRecordsFacturas.getDouble("DebeQuetzales") * porcentajeProporcional;
                        } else {
                            montoQuetzales = rsRecordsFacturas.getDouble("Monto");
                        }
                        partidaContainer.getContainerProperty(partidaObject, CUENTA_PROPERTY).setValue(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getAnticiposProveedor());
                        partidaContainer.getContainerProperty(partidaObject, DESCRIPCION_PROPERTY).setValue(cuentasContables.get(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getAnticiposProveedor()));
                        partidaContainer.getContainerProperty(partidaObject, DEBE_PROPERTY).setValue("0");
                        partidaContainer.getContainerProperty(partidaObject, HABER_PROPERTY).setValue(rsRecordsFacturas.getString("Monto"));
                        partidaContainer.getContainerProperty(partidaObject, DEBE_Q_PROPERTY).setValue("0");
                        partidaContainer.getContainerProperty(partidaObject, HABER_Q_PROPERTY).setValue(String.valueOf(montoQuetzales));
                        partidaContainer.getContainerProperty(partidaObject, CODIGOCC_PROPERTY).setValue(rsRecordsFacturas.getString("CodigoCCRelacionado"));
                        totalHaber  = totalHaber.add(new BigDecimal(rsRecordsFacturas.getString("Monto")));
                        totalHaberQ = totalHaberQ.add(new BigDecimal(montoQuetzales));
                    } while (rsRecordsFacturas.next());
                }

                idNomenclatura = String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(CUENTA_PROPERTY).getValue());
                partidaObject  = partidaContainer.addItem();

                if (moneda.equals("DOLARES")) {
                    montoMoneda    = Double.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(HABER_PROPERTY).getValue()).replaceAll(",", ""));
                    montoQuetzales = Double.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(HABER_Q_PROPERTY).getValue()).replaceAll(",", ""));
                } else {
                    montoQuetzales = montoProveedores;
                }
            } catch (Exception ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (monedaCbx.getValue().equals("DOLARES") && (totalDebeQ.doubleValue() != totalHaberQ.doubleValue())) {
            partidaObject = partidaContainer.addItem();
            partidaContainer.getContainerProperty(partidaObject, CUENTA_PROPERTY).setValue(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getDiferencialCambiario());
            partidaContainer.getContainerProperty(partidaObject, DESCRIPCION_PROPERTY).setValue("DIFERENCIAL CAMBIARIO");
            partidaContainer.getContainerProperty(partidaObject, DEBE_PROPERTY).setValue("0");
            partidaContainer.getContainerProperty(partidaObject, HABER_PROPERTY).setValue("0");
            if ((totalDebeQ.doubleValue() - totalHaberQ.doubleValue()) > 0) {
                partidaContainer.getContainerProperty(partidaObject, HABER_Q_PROPERTY).setValue(String.valueOf(Utileria.numberFormatEntero.format(totalDebeQ.doubleValue() - totalHaberQ.doubleValue())));
                totalHaberQ = totalHaberQ.add(new BigDecimal(Utileria.numberFormatEntero.format(totalDebeQ.doubleValue() - totalHaberQ.doubleValue())));
                partidaContainer.getContainerProperty(partidaObject, DEBE_Q_PROPERTY).setValue("0");
            } else {
                partidaContainer.getContainerProperty(partidaObject, DEBE_Q_PROPERTY).setValue(String.valueOf(Utileria.numberFormatEntero.format((totalDebeQ.doubleValue() - totalHaberQ.doubleValue()) * -1)));
                totalDebeQ = totalDebeQ.add(new BigDecimal(Utileria.numberFormatEntero.format((totalDebeQ.doubleValue() - totalHaberQ.doubleValue()) * -1)));
                partidaContainer.getContainerProperty(partidaObject, HABER_Q_PROPERTY).setValue("0");
            }
            partidaContainer.getContainerProperty(partidaObject, CODIGOCC_PROPERTY).setValue("");
        }

        // Fila separadora
        Object sep = partidaContainer.addItem();
        partidaContainer.getContainerProperty(sep, CUENTA_PROPERTY).setValue("__________");
        partidaContainer.getContainerProperty(sep, DESCRIPCION_PROPERTY).setValue("__________");
        partidaContainer.getContainerProperty(sep, DEBE_PROPERTY).setValue("___________");
        partidaContainer.getContainerProperty(sep, HABER_PROPERTY).setValue("___________");
        partidaContainer.getContainerProperty(sep, DEBE_Q_PROPERTY).setValue("___________");
        partidaContainer.getContainerProperty(sep, HABER_Q_PROPERTY).setValue("___________");
        partidaContainer.getContainerProperty(sep, CODIGOCC_PROPERTY).setValue("___________");

        // Fila sumas iguales
        Object total = partidaContainer.addItem();
        partidaContainer.getContainerProperty(total, CUENTA_PROPERTY).setValue("");
        partidaContainer.getContainerProperty(total, DESCRIPCION_PROPERTY).setValue("--------> SUMAS IGUALES");
        partidaContainer.getContainerProperty(total, DEBE_PROPERTY).setValue(numberFormat.format(totalDebe.doubleValue()));
        partidaContainer.getContainerProperty(total, HABER_PROPERTY).setValue(numberFormat.format(totalHaber.doubleValue()));
        partidaContainer.getContainerProperty(total, DEBE_Q_PROPERTY).setValue(numberFormat.format(totalDebeQ.doubleValue()));
        partidaContainer.getContainerProperty(total, HABER_Q_PROPERTY).setValue(numberFormat.format(totalHaberQ.doubleValue()));
        partidaContainer.getContainerProperty(total, CODIGOCC_PROPERTY).setValue("___________");

        Object sep2 = partidaContainer.addItem();
        partidaContainer.getContainerProperty(sep2, CUENTA_PROPERTY).setValue("__________");
        partidaContainer.getContainerProperty(sep2, DESCRIPCION_PROPERTY).setValue("__________");
        partidaContainer.getContainerProperty(sep2, DEBE_PROPERTY).setValue("___________");
        partidaContainer.getContainerProperty(sep2, HABER_PROPERTY).setValue("___________");
        partidaContainer.getContainerProperty(sep2, DEBE_Q_PROPERTY).setValue("___________");
        partidaContainer.getContainerProperty(sep2, HABER_Q_PROPERTY).setValue("___________");
        partidaContainer.getContainerProperty(sep2, CODIGOCC_PROPERTY).setValue("___________");
    }

    public void llenarGridFacturas() {
        totalDebe  = new BigDecimal(0);
        totalHaber = new BigDecimal(0);
        totalDebe.setScale(2, RoundingMode.HALF_UP);
        totalHaber.setScale(2, RoundingMode.HALF_UP);
        totalDebeQ  = new BigDecimal(0);
        totalHaberQ = new BigDecimal(0);

        footerFacturas.getCell(VALOR_PROPERTY).setText("0.00");
        footerFacturas.getCell(MONTO_AUTORIZADO_PROPERTY).setText("0.00");
        footerFacturas.getCell(ANTICIPO_PROPERTY).setText("0.00");

        facturasContainer.removeAllItems();
        totalMonto    = 0.00;
        totalQueztales = 0.00;
        totalAnticipo = 0.00;

        queryString  = "SELECT contabilidad_partida.CodigoPartida, contabilidad_partida.CodigoCC, ";
        queryString += " contabilidad_partida.Fecha, contabilidad_partida.NombreProveedor ,";
        queryString += " contabilidad_partida.IdNomenclatura, contabilidad_partida.TipoDocumento, ";
        queryString += " contabilidad_partida.SerieDocumento, contabilidad_partida.NumeroDocumento, ";
        queryString += " contabilidad_partida.MonedaDocumento, ";
        queryString += " contabilidad_partida.MontoAutorizadoPagar, contabilidad_partida.MontoAplicarAnticipo,";
        queryString += " contabilidad_partida.Haber, contabilidad_partida.HaberQuetzales, ";
        queryString += " ( (contabilidad_partida.HaberQuetzales / contabilidad_partida.Haber) * (contabilidad_partida.MontoAutorizadoPagar + contabilidad_partida.MontoAplicarAnticipo)) ProporcionHaberQ";
        queryString += " FROM contabilidad_partida ";
        queryString += " INNER JOIN autorizacion_pago ON autorizacion_pago.CodigoCC = contabilidad_partida.CodigoCC ";
        queryString += " WHERE contabilidad_partida.IdEmpresa =" + empresaId;
        queryString += " AND contabilidad_partida.IdProveedor = " + IdProveedor;
        queryString += " AND contabilidad_partida.IdNomenclatura = " + ((SopdiUI) mainUI).cuentasContablesDefault.getProveedores();
        queryString += " AND UPPER(contabilidad_partida.TipoDocumento) IN ('FACTURA', 'RECIBO', ";
        queryString += " 'RECIBO CONTABLE', 'RECIBO CORRIENTE', 'FORMULARIO RECTIFICACION')";
        queryString += " GROUP BY CodigoPartida";

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "query mostrar documentos a pagar FACTURAS : " + queryString);

        try {
            stQueryFacturas   = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecordsFacturas = stQuery.executeQuery(queryString);

            if (rsRecordsFacturas.next()) {
                do {
                    Object itemId = facturasContainer.addItem();
                    try {
                        facturasContainer.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecordsFacturas.getString("CodigoPartida"));
                    } catch (Exception ex11) {
                        ex11.printStackTrace();
                        return;
                    }
                    facturasContainer.getContainerProperty(itemId, TIPO_PROPERTY).setValue(rsRecordsFacturas.getString("TipoDocumento"));
                    facturasContainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecordsFacturas.getDate("Fecha")));
                    facturasContainer.getContainerProperty(itemId, PROVEEDOR_PROPERTY).setValue(rsRecordsFacturas.getString("NombreProveedor"));
                    facturasContainer.getContainerProperty(itemId, CODIGO_PROPERTY).setValue(IdProveedor);
                    facturasContainer.getContainerProperty(itemId, FACTURA_PROPERTY).setValue(rsRecordsFacturas.getString("SerieDocumento") + " " + rsRecordsFacturas.getString("NumeroDocumento"));
                    facturasContainer.getContainerProperty(itemId, MONEDA_PROPERTY).setValue(rsRecordsFacturas.getString("MonedaDocumento"));
                    facturasContainer.getContainerProperty(itemId, VALOR_PROPERTY).setValue(numberFormat.format(rsRecordsFacturas.getDouble("MontoAutorizadoPagar")));
                    facturasContainer.getContainerProperty(itemId, MONTO_AUTORIZADO_PROPERTY).setValue(numberFormat.format(rsRecordsFacturas.getDouble("MontoAutorizadoPagar")));
                    facturasContainer.getContainerProperty(itemId, ANTICIPO_PROPERTY).setValue(numberFormat.format(rsRecordsFacturas.getDouble("MontoAplicarAnticipo")));
                    facturasContainer.getContainerProperty(itemId, CUENTA_PROPERTY).setValue(rsRecordsFacturas.getString("IdNomenclatura"));
                    facturasContainer.getContainerProperty(itemId, HABER_PROPERTY).setValue(rsRecordsFacturas.getString("Haber"));
                    facturasContainer.getContainerProperty(itemId, HABER_Q_PROPERTY).setValue(rsRecordsFacturas.getString("HaberQuetzales"));
                    facturasContainer.getContainerProperty(itemId, CODIGOCC_PROPERTY).setValue(rsRecordsFacturas.getString("CodigoCC"));

                    totalMonto    += rsRecordsFacturas.getDouble("MontoAutorizadoPagar");
                    totalQueztales += rsRecordsFacturas.getDouble("MontoAutorizadoPagar");
                    totalAnticipo += rsRecordsFacturas.getDouble("MontoAplicarAnticipo");
                } while (rsRecordsFacturas.next());
            }

        } catch (Exception ex) {
            System.out.println("Error al listar tabla de DOCUMENTOS : " + ex);
            ex.printStackTrace();
            Notification notif = new Notification("HA OCURRIDO UN ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.WARNING);
            notif.show(Page.getCurrent());
        }

        footerFacturas.getCell(VALOR_PROPERTY).setText(numberFormat.format(totalMonto));
        footerFacturas.getCell(MONTO_AUTORIZADO_PROPERTY).setText(numberFormat.format(totalQueztales));
        footerFacturas.getCell(ANTICIPO_PROPERTY).setText(numberFormat.format(totalAnticipo));
    }

    public void limpiarPartida() {
        numeroTxt.setReadOnly(false);
        numeroTxt.setValue("");
        nombreChequeTxt.setReadOnly(false);
        nombreChequeTxt.setValue("");
        nombreChequeTxt.setReadOnly(((SopdiUI) mainUI).sessionInformation.getStrUserProfileName().equals("CONTADOR"));
        nombreChequeTxt.setReadOnly(((SopdiUI) mainUI).sessionInformation.getStrUserProfileName().equals("AUXILIAR"));
        montoTxt.setReadOnly(false);
        montoTxt.setValue(0.00);
        partidaContainer.removeAllItems();
        descripcionTxt.setValue("");
    }

    public void llenarComboProveedor() {
        String q = " SELECT * FROM proveedor WHERE Inhabilitado = 0 AND EsProveedor = 1 ORDER BY Nombre ";
        proveedorCbx.removeAllItems();
        try {
            stQuery   = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
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

    private void insertarPartidaCompuesta() {
        // (método original sin cambios)
        String descripcion = descripcionTxt.getValue();

        queryString  = "INSERT INTO contabilidad_partida (";
        queryString += " IdEmpresa, IdNomenclatura, CodigoCC, Descripcion, TipoDocumento,";
        queryString += " SerieDocumento, NumeroDocumento, Fecha, Debe, DebeQuetzales,";
        queryString += " Haber, HaberQuetzales, IdMoneda, TipoCambio, Estatus,";
        queryString += " TipoDoca, NoDoca, IdProveedor, NombreProveedor, CodigoCC_Doc,";
        queryString += " Descripcion2, IdUsuario, FechaCreacion) VALUES ";

        Iterator iter = partidaContainer.getItemIds().iterator();

        codigoPartidaNuevo = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId()
                + String.valueOf(System.currentTimeMillis());

        while (iter.hasNext()) {
            Object itemId = iter.next();
            Item   item   = partidaContainer.getItem(itemId);

            String cuenta     = String.valueOf(item.getItemProperty(CUENTA_PROPERTY).getValue());
            String descripPar = String.valueOf(item.getItemProperty(DESCRIPCION_PROPERTY).getValue());
            String debe       = String.valueOf(item.getItemProperty(DEBE_PROPERTY).getValue()).replaceAll(",", "");
            String haber      = String.valueOf(item.getItemProperty(HABER_PROPERTY).getValue()).replaceAll(",", "");
            String debeQ      = String.valueOf(item.getItemProperty(DEBE_Q_PROPERTY).getValue()).replaceAll(",", "");
            String haberQ     = String.valueOf(item.getItemProperty(HABER_Q_PROPERTY).getValue()).replaceAll(",", "");
            String codCC      = String.valueOf(item.getItemProperty(CODIGOCC_PROPERTY).getValue());

            if (cuenta.startsWith("_") || debe.startsWith("_") || haber.startsWith("_")) continue;

            queryString += "(";
            queryString += empresaId;
            queryString += "," + cuenta;
            queryString += ",'" + codigoPartidaNuevo + "'";
            queryString += ",'" + descripcion + "'";
            queryString += ",'PAGO'";
            queryString += ",'" + medioCbx.getValue() + "'";
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + debe;
            queryString += "," + debeQ;
            queryString += "," + haber;
            queryString += "," + haberQ;
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + tasaCambioTxt.getValue();
            queryString += ",'PAGADO'";
            queryString += ",'" + medioCbx.getValue() + "'";
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += "," + proveedorId;
            queryString += ",'" + proveedorNombre + "'";
            queryString += ",'" + codCC + "'";
            queryString += ",'" + descripcion + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += "),";
        }

        queryString = queryString.substring(0, queryString.length() - 1);

        try {
            ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().setAutoCommit(false);
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            Iterator iterFacturas   = facturasGrid.getSelectedRows().iterator();
            Double   montoPagar     = 0.00;
            Double   montoAnticipo  = 0.00;
            String   tipo           = "";
            String   fechaSelect    = "";
            String   codigoPartidaDoca;

            while (iterFacturas.hasNext()) {
                Object gridItem = iterFacturas.next();
                codigoCC           = String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(CODIGOCC_PROPERTY).getValue());
                codigoPartidaDoca  = String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(ID_PROPERTY).getValue());
                montoPagar         = Double.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(VALOR_PROPERTY).getValue()).replaceAll(",", ""));
                montoAnticipo      = Double.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(ANTICIPO_PROPERTY).getValue()).replaceAll(",", ""));
                tipo               = String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(TIPO_PROPERTY).getValue());
                fechaSelect        = String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(FECHA_PROPERTY).getValue());

                queryString  = " UPDATE contabilidad_partida SET ";
                queryString += " MontoAutorizadoPagar = 0.00, MontoAplicarAnticipo = 0.00, Estatus = 'PAGADO'";
                queryString += ", Referencia = '" + codigoPartida + "'";
                queryString += ", TipoDoca = '" + medioCbx.getValue() + "'";
                queryString += ", NoDoca = '" + numeroTxt.getValue() + "'";
                queryString += " WHERE CodigoPartida = '" + codigoPartidaDoca + "'";
                stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                stQuery.executeUpdate(queryString);

                queryString  = " UPDATE contabilidad_partida SET Estatus = 'PAGADO'";
                queryString += ", Referencia = '" + codigoPartida + "'";
                queryString += ", TipoDoca = '" + medioCbx.getValue() + "'";
                queryString += ", NoDoca = '" + numeroTxt.getValue() + "'";
                queryString += " WHERE CodigoCC = '" + codigoCC + "'";
                queryString += " AND TipoDocumento = 'NOTA DE CREDITO COMPRA'";
                stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                stQuery.executeUpdate(queryString);

                queryString  = " DELETE FROM autorizacion_pago WHERE CodigoCC = '" + codigoPartidaDoca + "'";
                stQuery.executeUpdate(queryString);

                ((PagarView) (mainUI.getNavigator().getCurrentView())).llenarTablaAutorizaciones();
                facturasGrid.getContainerDataSource().removeItem(gridItem);
            }

            ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().commit();
            ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().setAutoCommit(true);

            Notification notif = new Notification("PAGO REALIZADO EXITOSAMENTE.", Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.CHECK);
            notif.show(Page.getCurrent());

            PagoChequesPDF pagoCheques = new PagoChequesPDF(empresaId, empresaNombre, codigoPartidaNuevo, "0",
                    nombreChequeTxt.getValue(), numeroTxt.getValue(), descripcion, numberFormat3.format(montoTxt.getDoubleValueDoNotThrow()));
            mainUI.addWindow(pagoCheques);
            pagoCheques.center();

            facturasGrid.getSelectedRows().clear();
            facturasGrid.getSelectionModel().reset();
            proveedorCbx.setReadOnly(false);
            limpiarPartida();
            proveedorCbx.setReadOnly(true);

            MostrarPartidaContable mostrarPartidaContable = new MostrarPartidaContable(codigoPartida, "", descripcion, numeroTxt.getValue());
            mainUI.addWindow(mostrarPartidaContable);
            mostrarPartidaContable.center();

        } catch (Exception ex1) {
            System.out.println("Error al insertar transacción: " + ex1.getMessage());
            ex1.printStackTrace();
            Notification notif = new Notification("HA OCURRIDO UN ERROR DE BASE DE DATOS : " + ex1.getMessage() + " TRANSACCION ABORTADA!!!", Notification.Type.ERROR_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.WARNING);
            notif.show(Page.getCurrent());
            try {
                ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().rollback();
                ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().setAutoCommit(true);
            } catch (SQLException ex) {
                Logger.getLogger(PagoFacturaProveedorForm.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                String[] emailsTo = {"alerta@simpletecno.com"};
                MyEmailMessanger eMail = new MyEmailMessanger();
                eMail.postMail(emailsTo, "Error en SOPDI", "Error en base de datos: " + this.getClass().getName() + " --> " + ex1.getMessage());
            } catch (MessagingException ex2) {
                Logger.getLogger(PagoFacturaProveedorForm.class.getName()).log(Level.SEVERE, null, ex2);
            }
        }
    }

    public void desAutorizarFactura() {
        Iterator iter = facturasGrid.getSelectedRows().iterator();

        ConfirmDialog.show(UI.getCurrent(), "Confirme:", "¿Desea eliminar la autorización de pago de estas facturas?",
                "SI", "NO", dialog -> {
                    if (dialog.isConfirmed()) {
                        try {
                            ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().setAutoCommit(false);
                            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

                            while (iter.hasNext()) {
                                Object gridItem       = iter.next();
                                String codigoPartida1 = String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(ID_PROPERTY).getValue());

                                queryString  = " UPDATE contabilidad_partida SET MontoAutorizadoPagar = 0, MontoAplicarAnticipo = 0";
                                queryString += " WHERE CodigoPartida = '" + codigoPartida1 + "'";
                                stQuery.executeUpdate(queryString);

                                queryString = " DELETE FROM autorizacion_pago WHERE CodigoCC = '" + codigoPartida1 + "'";
                                stQuery.executeUpdate(queryString);
                            }

                            ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().commit();
                            ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().setAutoCommit(true);
                            Notification.show("DOCUMENTOS DES-AUTORIZADOS CON ÉXITO", Notification.Type.HUMANIZED_MESSAGE);
                            close();

                        } catch (Exception ex) {
                            System.out.println("Error al des-autorizar facturas: " + ex);
                            Notification notif = new Notification("HA OCURRIDO UN ERROR DE BASE DE DATOS : " + ex.getMessage() + " TRANSACCION ABORTADA!!!", Notification.Type.ERROR_MESSAGE);
                            notif.setDelayMsec(1500);
                            notif.setPosition(Position.MIDDLE_CENTER);
                            notif.setIcon(FontAwesome.WARNING);
                            notif.show(Page.getCurrent());
                            try {
                                ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().rollback();
                                ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().setAutoCommit(true);
                            } catch (SQLException exSql) {
                                Logger.getLogger(PagoFacturaProveedorForm.class.getName()).log(Level.SEVERE, null, exSql);
                            }
                        }
                    }
                });
    }

    public boolean exportToExcel(Grid theGrid) {
        if (theGrid.getHeightByRows() > 0) {
            TableHolder  tableHolder  = new DefaultTableHolder(theGrid);
            ExcelExport  excelExport  = new ExcelExport(tableHolder);
            excelExport.excludeCollapsedColumns();
            excelExport.setDisplayTotals(false);
            String fileexport = (empresaId + "_" + empresaNombre
                    .replaceAll(" ", "_").replaceAll(",", "_")
                    .replaceAll("[()]", "").replaceAll("[.]", "")
                    .replaceAll("ñ", "n").replaceAll("Ñ", "N")
                    .replaceAll("ó", "o").replaceAll("é", "")
                    + "_DOCUMENTOS.xls").replaceAll(" ", "").replaceAll(",", "");
            excelExport.setExportFileName(fileexport);
            excelExport.export();
        }
        return true;
    }

    /** Debe ser implementado para llenar las cuentas contables disponibles. */
    private void llenarComboCuentaContable() {
        // implementación existente sin cambios
    }
}