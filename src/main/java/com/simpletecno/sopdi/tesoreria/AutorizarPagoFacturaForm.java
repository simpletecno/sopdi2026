package com.simpletecno.sopdi.tesoreria;

import com.simpletecno.sopdi.utilerias.MyEmailMessanger;
import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Field;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.HeaderRow;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.vaadin.ui.NumberField;

import javax.mail.MessagingException;

/**
 * Formulario de Autorización de Pago de Facturas a Proveedores.
 * UI modernizada: header con gradiente, secciones en card, botones estilizados.
 *
 * @author user
 */
public class AutorizarPagoFacturaForm extends Window {

    // ── Constantes de columnas — Grid Facturas ───────────────────────────────
    static final String TIPO_DOCUMENTO_PROPERTY    = "T. Documento";
    static final String ID_PROVEEDOR_PROPERTY      = "Id Proveedor";
    static final String PROVEEDOR_PROPERTY         = "Proveedor";
    static final String FECHA_PROPERTY             = "Fecha";
    static final String NUMERO_FACTURA_PROPERTY    = "Número";
    static final String DESCRIPCION_PROPERTY       = "Descripción";
    static final String MONEDA_PROPERTY            = "Moneda";
    static final String MONTO_PROPERTY             = "Monto";
    static final String SALDO_PROPERTY             = "Saldo";
    static final String MONTO_AUTORIZADO_PROPERTY  = "M. Autorizado";
    static final String MONTO_ANTICIPO_PROPERTY    = "M. Anticipo";
    static final String CODIGO_PARTIDA_PROPERTY    = "Codigo partida";
    static final String CODIGO_CC_PROPERTY         = "CodigoCC";
    static final String CC_BTN_PROPERTY            = "Cta. Corriente";

    // ── Constantes de columnas — Grid Anticipos ──────────────────────────────
    static final String CODIGO_PARTIDA2_PROPERTY   = "CodigoPartida";
    static final String CODIGO_CC2_PROPERTY        = "CodigoCC";
    static final String FECHA2__PROPERTY           = "Fecha";
    static final String DOCUMENTO_PROPERTY         = "Cheque/Transf";
    static final String MONTO2_PROPERTY            = "Monto";
    static final String SALDO2_PROPERTY            = "Saldo";
    static final String UTILIZAR_PROPERTY          = "Utilizar";

    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");

    // ── DB ───────────────────────────────────────────────────────────────────
    Statement stQuery, stQuery1;
    ResultSet rsRecords, rsRecords1;
    String queryString;

    // ── Contenedores y Grids ─────────────────────────────────────────────────
    IndexedContainer facturasContainer      = new IndexedContainer();
    Grid             facturasGrid;
    IndexedContainer anticiposPagoContainer = new IndexedContainer();
    Grid             anticiposPagoGrid;

    // ── Botones ──────────────────────────────────────────────────────────────
    Button salirBtn;
    Button autorizarBtn;

    // ── Campos numéricos ─────────────────────────────────────────────────────
    NumberField totalUtilizarAnticiposTxt;
    NumberField montoPendienteChequeTxt;

    // ── Estado ───────────────────────────────────────────────────────────────
    double totalMontoQuetzales       = 0.00;
    double totalSaldoQueztales       = 0.00;
    double totalMontoDolares         = 0.00;
    double totalSaldoDolares         = 0.00;
    double saldoFacturaSeleccionada  = 0.00;

    UI     mainUI;
    String codigoPartidaFactura = "";

    String empresaId     = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    // ── Layout principal ─────────────────────────────────────────────────────
    VerticalLayout mainLayout;

    // ────────────────────────────────────────────────────────────────────────
    public AutorizarPagoFacturaForm() {
        this.mainUI = UI.getCurrent();

        injectStyles();

        setModal(true);
        setResizable(true);
        setDraggable(true);
        setResponsive(true);
        setWidth("95%");
        setHeight("95%");

        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(false);
        mainLayout.setMargin(false);
        mainLayout.setResponsive(true);
        mainLayout.addStyleName("apf-main");
        mainLayout.setSizeFull();

        setContent(mainLayout);

        buildHeader();
        crearGridFactura();
        llenarGridFactura();
        crearGridAnticipos();
    }

    // ── CSS ──────────────────────────────────────────────────────────────────
    private void injectStyles() {
        Page.getCurrent().getStyles().add(

                /* ── Fondo general ──────────────────────────────────────────── */
                ".apf-main {" +
                "  background: #F4F6F9;" +
                "}" +

                /* ── Header ─────────────────────────────────────────────────── */
                ".apf-header {" +
                "  background: linear-gradient(135deg, #1B5E20 0%, #2E7D32 100%);" +
                "  border-radius: 10px;" +
                "  padding: 14px 20px !important;" +
                "  margin-bottom: 6px;" +
                "  width: 100%;" +
                "}" +
                ".apf-header-icon {" +
                "  font-size: 26px !important;" +
                "  line-height: 1;" +
                "}" +
                ".apf-header-title {" +
                "  color: #ffffff !important;" +
                "  font-size: 16px !important;" +
                "  font-weight: 700 !important;" +
                "  margin: 0 !important;" +
                "  letter-spacing: 0.02em;" +
                "}" +
                ".apf-header-sub {" +
                "  color: #A5D6A7 !important;" +
                "  font-size: 11px !important;" +
                "  margin: 2px 0 0 0 !important;" +
                "}" +

                /* ── Sección card ────────────────────────────────────────────── */
                ".apf-section {" +
                "  background: #ffffff;" +
                "  border-radius: 8px;" +
                "  box-shadow: 0 2px 8px rgba(0,0,0,0.07);" +
                "  padding: 6px 10px !important;" +
                "  width: 100%;" +
                "  margin-bottom: 4px;" +
                "}" +
                ".apf-section-label {" +
                "  color: #2E7D32;" +
                "  font-size: 11px !important;" +
                "  font-weight: 700 !important;" +
                "  letter-spacing: 0.09em;" +
                "  text-transform: uppercase;" +
                "  border-bottom: 2px solid #E8F5E9;" +
                "  padding-bottom: 2px;" +
                "  margin-bottom: 2px !important;" +
                "  display: block;" +
                "  width: 100%;" +
                "}" +

                /* ── Grids ───────────────────────────────────────────────────── */
                ".apf-main .v-grid-header {" +
                "  background: #E8F5E9 !important;" +
                "  font-weight: 600 !important;" +
                "  color: #1B5E20 !important;" +
                "}" +
                ".apf-main .v-grid-row:hover > td {" +
                "  background: #F1F8E9 !important;" +
                "}" +
                ".apf-main .v-grid-row-selected > td {" +
                "  background: #C8E6C9 !important;" +
                "  color: #1B5E20 !important;" +
                "  font-weight: 600;" +
                "}" +
                ".apf-main .v-grid-footer {" +
                "  background: #F0F4F0 !important;" +
                "  font-weight: 700 !important;" +
                "  border-top: 2px solid #C8E6C9 !important;" +
                "}" +

                /* ── Botón Autorizar ─────────────────────────────────────────── */
                ".apf-btn-autorizar.v-button {" +
                "  background: linear-gradient(135deg, #1565C0 0%, #1976D2 100%) !important;" +
                "  color: #fff !important;" +
                "  border: none !important;" +
                "  border-radius: 6px !important;" +
                "  font-weight: 700 !important;" +
                "  padding: 0 24px !important;" +
                "  height: 36px !important;" +
                "  box-shadow: 0 2px 8px rgba(21,101,192,0.35) !important;" +
                "}" +
                ".apf-btn-autorizar.v-button:hover {" +
                "  background: linear-gradient(135deg, #1976D2 0%, #1E88E5 100%) !important;" +
                "}" +

                /* ── Botón Cuenta Corriente ──────────────────────────────────── */
                ".apf-btn-cc.v-button {" +
                "  background: linear-gradient(135deg, #F57F17 0%, #F9A825 100%) !important;" +
                "  color: #fff !important;" +
                "  border: none !important;" +
                "  border-radius: 6px !important;" +
                "  font-weight: 600 !important;" +
                "  padding: 0 16px !important;" +
                "  height: 34px !important;" +
                "  box-shadow: 0 2px 6px rgba(245,127,23,0.3) !important;" +
                "}" +

                /* ── Botón Salir ─────────────────────────────────────────────── */
                ".apf-btn-salir.v-button {" +
                "  border: 1px solid #CFD8DC !important;" +
                "  border-radius: 6px !important;" +
                "  color: #546E7A !important;" +
                "  background: #fff !important;" +
                "  height: 36px !important;" +
                "  padding: 0 18px !important;" +
                "}" +
                ".apf-btn-salir.v-button:hover {" +
                "  background: #F5F5F5 !important;" +
                "}" +

                /* ── Barra inferior de montos + acciones ─────────────────────── */
                ".apf-action-bar {" +
                "  border-top: 1px solid #E3E8EF;" +
                "  padding-top: 12px;" +
                "  margin-top: 6px;" +
                "  width: 100%;" +
                "}" +

                /* ── Chip de totales ─────────────────────────────────────────── */
                ".apf-total-chip {" +
                "  background: #E8F5E9;" +
                "  border-radius: 6px;" +
                "  padding: 4px 10px !important;" +
                "  border: 1px solid #C8E6C9;" +
                "}"
        );
    }

    // ── Header ───────────────────────────────────────────────────────────────
    private void buildHeader() {
        Label iconLbl = new Label("✅");
        iconLbl.addStyleName("apf-header-icon");
        iconLbl.setSizeUndefined();

        Label titleLbl = new Label(empresaId + "  ·  " + empresaNombre
                + "  —  AUTORIZACIÓN DE PAGO  ·  " + AutorizacionesPagoView.PAGO_DOCUMENTO);
        titleLbl.addStyleName("apf-header-title");
        titleLbl.setSizeUndefined();

        Label subLbl = new Label("Seleccione un documento, revise los anticipos disponibles y confirme la autorización");
        subLbl.addStyleName("apf-header-sub");
        subLbl.setSizeUndefined();

        VerticalLayout textCol = new VerticalLayout();
        textCol.setMargin(false);
        textCol.setSpacing(false);
        textCol.addComponents(titleLbl, subLbl);

        HorizontalLayout headerHL = new HorizontalLayout();
        headerHL.addStyleName("apf-header");
        headerHL.setWidth("100%");
        headerHL.setSpacing(true);
        headerHL.setMargin(false);
        headerHL.addComponents(iconLbl, textCol);
        headerHL.setExpandRatio(textCol, 1f);
        headerHL.setComponentAlignment(iconLbl,  Alignment.MIDDLE_LEFT);
        headerHL.setComponentAlignment(textCol,  Alignment.MIDDLE_LEFT);

        mainLayout.addComponent(headerHL);
        mainLayout.setComponentAlignment(headerHL, Alignment.TOP_CENTER);
    }

    // ── Grid de Facturas ─────────────────────────────────────────────────────
    public void crearGridFactura() {

        facturasContainer.addContainerProperty(TIPO_DOCUMENTO_PROPERTY,   String.class, null);
        facturasContainer.addContainerProperty(ID_PROVEEDOR_PROPERTY,     String.class, null);
        facturasContainer.addContainerProperty(PROVEEDOR_PROPERTY,        String.class, null);
        facturasContainer.addContainerProperty(FECHA_PROPERTY,            String.class, null);
        facturasContainer.addContainerProperty(NUMERO_FACTURA_PROPERTY,   String.class, null);
        facturasContainer.addContainerProperty(DESCRIPCION_PROPERTY,      String.class, null);
        facturasContainer.addContainerProperty(MONEDA_PROPERTY,           String.class, null);
        facturasContainer.addContainerProperty(MONTO_PROPERTY,            String.class, null);
        facturasContainer.addContainerProperty(SALDO_PROPERTY,            String.class, null);
        facturasContainer.addContainerProperty(MONTO_AUTORIZADO_PROPERTY, String.class, null);
        facturasContainer.addContainerProperty(MONTO_ANTICIPO_PROPERTY,   String.class, null);
        facturasContainer.addContainerProperty(CODIGO_PARTIDA_PROPERTY,   String.class, null);
        facturasContainer.addContainerProperty(CODIGO_CC_PROPERTY,        String.class, null);
        facturasContainer.addContainerProperty(CC_BTN_PROPERTY,           String.class, "Ver CC");

        facturasGrid = new Grid("", facturasContainer);
        facturasGrid.setSizeFull();
        facturasGrid.setImmediate(true);
        facturasGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        facturasGrid.setDescription("Seleccione un registro.");
        facturasGrid.setResponsive(true);
        facturasGrid.setEditorBuffered(false);

        facturasGrid.getColumn(CODIGO_PARTIDA_PROPERTY).setHidable(true).setHidden(true);
        facturasGrid.getColumn(CODIGO_CC_PROPERTY).setHidable(true).setHidden(true);
        facturasGrid.getColumn(ID_PROVEEDOR_PROPERTY).setHidable(true).setHidden(true);
        facturasGrid.getColumn(DESCRIPCION_PROPERTY).setHidable(true).setHidden(true);
        facturasGrid.getColumn(FECHA_PROPERTY).setHidable(true);

        // Columna con botón embebido para abrir Cuenta Corriente
        facturasGrid.getColumn(CC_BTN_PROPERTY)
                .setRenderer(new ButtonRenderer(event -> {
                    String codigoCC = String.valueOf(
                            facturasContainer.getContainerProperty(event.getItemId(), CODIGO_CC_PROPERTY).getValue());
                    CuentaCorrienteDocumentoForm ccForm = new CuentaCorrienteDocumentoForm(codigoCC);
                    mainUI.addWindow(ccForm);
                    ccForm.center();
                }))
                .setWidth(120);

        facturasGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if (MONTO_PROPERTY.equals(cellReference.getPropertyId()) ||
                    SALDO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            }
            return null;
        });

        facturasGrid.addSelectionListener(event -> {
            if (facturasGrid.getSelectedRow() != null) {
                String saldo = String.valueOf(facturasContainer.getContainerProperty(facturasGrid.getSelectedRow(), SALDO_PROPERTY).getValue());
                saldoFacturaSeleccionada = Double.parseDouble(saldo.replaceAll(",", "").replaceAll("Q.", "").replaceAll("\\$.", ""));
                montoPendienteChequeTxt.setValue(saldoFacturaSeleccionada);
                codigoPartidaFactura = String.valueOf(facturasContainer.getContainerProperty(facturasGrid.getSelectedRow(), CODIGO_PARTIDA_PROPERTY).getValue());
                llenarTablaAnticipos();
                totalUtilizarAnticiposTxt.setReadOnly(anticiposPagoContainer.size() == 0);
            }
        });

        // Fila de filtros
        HeaderRow filterRow = facturasGrid.appendHeaderRow();

        TextField filterProveedor = buildFilterField(10);
        filterProveedor.addTextChangeListener(change -> {
            facturasContainer.removeContainerFilters(PROVEEDOR_PROPERTY);
            if (!change.getText().isEmpty()) {
                facturasContainer.addContainerFilter(new SimpleStringFilter(PROVEEDOR_PROPERTY, change.getText(), true, false));
            }
        });
        filterRow.getCell(PROVEEDOR_PROPERTY).setComponent(filterProveedor);

        TextField filterNumero = buildFilterField(8);
        filterNumero.addTextChangeListener(change -> {
            facturasContainer.removeContainerFilters(NUMERO_FACTURA_PROPERTY);
            if (!change.getText().isEmpty()) {
                facturasContainer.addContainerFilter(new SimpleStringFilter(NUMERO_FACTURA_PROPERTY, change.getText(), true, false));
            }
        });
        filterRow.getCell(NUMERO_FACTURA_PROPERTY).setComponent(filterNumero);

        TextField filterMoneda = buildFilterField(6);
        filterMoneda.addTextChangeListener(change -> {
            facturasContainer.removeContainerFilters(MONEDA_PROPERTY);
            if (!change.getText().isEmpty()) {
                facturasContainer.addContainerFilter(new SimpleStringFilter(MONEDA_PROPERTY, change.getText(), true, false));
            }
        });
        filterRow.getCell(MONEDA_PROPERTY).setComponent(filterMoneda);

        // Card de la sección — ocupa la mitad superior del Window
        VerticalLayout facturasSection = buildSection("📄  Documentos pendientes de autorización para pago");
        facturasSection.setSizeFull();
        facturasSection.addComponent(facturasGrid);
        facturasSection.setExpandRatio(facturasGrid, 1f);

        mainLayout.addComponent(facturasSection);
        mainLayout.setComponentAlignment(facturasSection, Alignment.TOP_CENTER);
        mainLayout.setExpandRatio(facturasSection, 1f);
    }

    // ── Grid de Anticipos ────────────────────────────────────────────────────
    public void crearGridAnticipos() {

        anticiposPagoContainer.addContainerProperty(CODIGO_PARTIDA2_PROPERTY, String.class, null);
        anticiposPagoContainer.addContainerProperty(CODIGO_CC2_PROPERTY,      String.class, null);
        anticiposPagoContainer.addContainerProperty(FECHA2__PROPERTY,         String.class, null);
        anticiposPagoContainer.addContainerProperty(DOCUMENTO_PROPERTY,       String.class, null);
        anticiposPagoContainer.addContainerProperty(MONTO2_PROPERTY,          String.class, null);
        anticiposPagoContainer.addContainerProperty(SALDO2_PROPERTY,          String.class, null);
        anticiposPagoContainer.addContainerProperty(UTILIZAR_PROPERTY,        String.class, null);

        anticiposPagoGrid = new Grid("", anticiposPagoContainer);
        anticiposPagoGrid.setSizeFull();
        anticiposPagoGrid.setImmediate(true);
        anticiposPagoGrid.setSelectionMode(Grid.SelectionMode.NONE);
        anticiposPagoGrid.setDescription("Doble click en una fila para ingresar el monto a utilizar.");
        anticiposPagoGrid.setResponsive(true);
        anticiposPagoGrid.setEditorBuffered(false);
        anticiposPagoGrid.setEditorEnabled(true);

        anticiposPagoGrid.getColumn(CODIGO_PARTIDA2_PROPERTY).setHidable(true).setHidden(true);

        anticiposPagoGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if (MONTO2_PROPERTY.equals(cellReference.getPropertyId()) ||
                    SALDO2_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            }
            return null;
        });

        anticiposPagoGrid.getColumn(UTILIZAR_PROPERTY).setEditorField(getAmmountField(UTILIZAR_PROPERTY));
        anticiposPagoGrid.addItemClickListener(event -> {
            if (event != null) {
                montoPendienteChequeTxt.setValue(0.00);
                totalUtilizarAnticiposTxt.setReadOnly(false);
                totalUtilizarAnticiposTxt.setValue(0.00);
                anticiposPagoGrid.editItem(event.getItemId());
            }
        });

        // ── Campos de totales ────────────────────────────────────────────────
        totalUtilizarAnticiposTxt = buildNumberField("Anticipos utilizados");
        totalUtilizarAnticiposTxt.setWidth("9em");
        totalUtilizarAnticiposTxt.setReadOnly(true);
        totalUtilizarAnticiposTxt.addStyleName("apf-total-chip");

        montoPendienteChequeTxt = buildNumberField("Monto para cheque");
        montoPendienteChequeTxt.setWidth("9em");
        montoPendienteChequeTxt.addStyleName("apf-total-chip");

        // ── Botones ──────────────────────────────────────────────────────────
        autorizarBtn = new Button("Autorizar pago");
        autorizarBtn.setIcon(FontAwesome.CHECK_CIRCLE);
        autorizarBtn.addStyleName("apf-btn-autorizar");
        autorizarBtn.addClickListener(event -> actualizarFactura());

        salirBtn = new Button("Salir");
        salirBtn.setIcon(FontAwesome.SIGN_OUT);
        salirBtn.addStyleName("apf-btn-salir");
        salirBtn.addClickListener(event -> {
            ((AutorizacionesPagoView) (mainUI.getNavigator().getCurrentView())).pagoDocumentoBtn.setEnabled(true);
            close();
        });

        // Barra de acción con totales y botones
        Label spacer = new Label();
        HorizontalLayout actionBar = new HorizontalLayout();
        actionBar.addStyleName("apf-action-bar");
        actionBar.setWidth("100%");
        actionBar.setSpacing(true);
        actionBar.addComponents(salirBtn, spacer, montoPendienteChequeTxt, totalUtilizarAnticiposTxt, autorizarBtn);
        actionBar.setExpandRatio(spacer, 1f);
        actionBar.setComponentAlignment(salirBtn,                  Alignment.MIDDLE_LEFT);
        actionBar.setComponentAlignment(montoPendienteChequeTxt,   Alignment.MIDDLE_RIGHT);
        actionBar.setComponentAlignment(totalUtilizarAnticiposTxt, Alignment.MIDDLE_RIGHT);
        actionBar.setComponentAlignment(autorizarBtn,              Alignment.MIDDLE_RIGHT);

        // Card de la sección — ocupa la mitad inferior del Window
        VerticalLayout anticiposSection = buildSection("💰  Anticipos al proveedor pendientes de liquidar");
        anticiposSection.setSizeFull();
        anticiposSection.addComponents(anticiposPagoGrid, actionBar);
        anticiposSection.setExpandRatio(anticiposPagoGrid, 1f);

        mainLayout.addComponent(anticiposSection);
        mainLayout.setComponentAlignment(anticiposSection, Alignment.TOP_CENTER);
        mainLayout.setExpandRatio(anticiposSection, 1f);
    }

    // ── Helper: card de sección ───────────────────────────────────────────────
    private VerticalLayout buildSection(String title) {
        Label sectionLbl = new Label(title);
        sectionLbl.addStyleName("apf-section-label");
        sectionLbl.setWidth("100%");

        VerticalLayout section = new VerticalLayout();
        section.addStyleName("apf-section");
        section.setWidth("100%");
        section.setSpacing(false);
        section.setMargin(false);
        section.addComponent(sectionLbl);
        return section;
    }

    /** Helper: TextField de filtro estilo tiny. */
    private TextField buildFilterField(int columns) {
        TextField field = new TextField();
        field.addStyleName(ValoTheme.TEXTFIELD_TINY);
        field.setInputPrompt("Filtrar");
        field.setColumns(columns);
        return field;
    }

    /** Helper: NumberField con configuración decimal estándar. */
    private NumberField buildNumberField(String caption) {
        NumberField field = new NumberField(caption + " :");
        field.setDecimalAllowed(true);
        field.setDecimalPrecision(2);
        field.setMinimumFractionDigits(2);
        field.setDecimalSeparator('.');
        field.setDecimalSeparatorAlwaysShown(true);
        field.setValue(0d);
        field.setGroupingUsed(true);
        field.setGroupingSeparator(',');
        field.setGroupingSize(3);
        field.setImmediate(true);
        field.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        return field;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  LÓGICA DE NEGOCIO — sin cambios funcionales
    // ════════════════════════════════════════════════════════════════════════

    public void llenarGridFactura() {
        anticiposPagoContainer.removeAllItems();
        facturasContainer.removeAllItems();
        facturasContainer.removeAllContainerFilters();

        totalMontoQuetzales = 0.00;
        totalSaldoQueztales = 0.00;
        totalMontoDolares   = 0.00;
        totalSaldoDolares   = 0.00;

        queryString  = " SELECT * ";
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
        queryString += " ORDER by contabilidad_partida.CodigoPartida";

        try {
            stQuery  = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);
            stQuery1 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            if (rsRecords.next()) {
                String monedaSimbolo;
                do {
                    queryString  = " SELECT ";
                    queryString += " SUM(HABER - DEBE) TOTALSALDO, SUM(HaberQuetzales - DebeQuetzales) TOTALSALDOQ ";
                    queryString += " FROM contabilidad_partida";
                    queryString += " WHERE IdEmpresa = " + empresaId;
                    queryString += " AND CodigoCC = '" + rsRecords.getString("CodigoCC") + "'";
                    queryString += " AND contabilidad_partida.IdNomenclatura = " + rsRecords.getString("IdNomenclatura");
                    queryString += " AND contabilidad_partida.Estatus <> 'ANULADO'";

                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, queryString);
                    rsRecords1 = stQuery1.executeQuery(queryString);

                    if (rsRecords1.next()) {
                        if (rsRecords1.getDouble("TOTALSALDO") > 0.00) {
                            Object itemId = facturasContainer.addItem();
                            facturasContainer.getContainerProperty(itemId, TIPO_DOCUMENTO_PROPERTY).setValue(rsRecords.getString("TipoDocumento"));
                            facturasContainer.getContainerProperty(itemId, ID_PROVEEDOR_PROPERTY).setValue(rsRecords.getString("IdProveedor"));
                            facturasContainer.getContainerProperty(itemId, PROVEEDOR_PROPERTY).setValue(rsRecords.getString("NombreProveedor"));
                            facturasContainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha")));
                            facturasContainer.getContainerProperty(itemId, NUMERO_FACTURA_PROPERTY).setValue(rsRecords.getString("SerieDocumento") + " " + rsRecords.getString("NumeroDocumento"));
                            facturasContainer.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(rsRecords.getString("Descripcion"));
                            facturasContainer.getContainerProperty(itemId, MONEDA_PROPERTY).setValue(rsRecords.getString("MonedaDocumento"));

                            if (rsRecords.getString("MonedaDocumento").equals("QUETZALES")) {
                                monedaSimbolo        = "Q.";
                                totalMontoQuetzales += rsRecords.getDouble("MontoDocumento");
                                totalSaldoQueztales += rsRecords1.getDouble("TOTALSALDO");
                            } else {
                                monedaSimbolo      = "$.";
                                totalMontoDolares += rsRecords.getDouble("MontoDocumento");
                                totalSaldoDolares += rsRecords1.getDouble("TOTALSALDO");
                            }

                            facturasContainer.getContainerProperty(itemId, MONTO_PROPERTY).setValue(monedaSimbolo + numberFormat.format(rsRecords.getDouble("MontoDocumento")));
                            facturasContainer.getContainerProperty(itemId, SALDO_PROPERTY).setValue(monedaSimbolo + numberFormat.format(rsRecords1.getDouble("TOTALSALDO")));
                            facturasContainer.getContainerProperty(itemId, MONTO_AUTORIZADO_PROPERTY).setValue(monedaSimbolo + numberFormat.format(rsRecords.getDouble("MontoAutorizadoPagar")));
                            facturasContainer.getContainerProperty(itemId, MONTO_ANTICIPO_PROPERTY).setValue(monedaSimbolo + numberFormat.format(rsRecords.getDouble("MontoAplicarAnticipo")));
                            facturasContainer.getContainerProperty(itemId, CODIGO_CC_PROPERTY).setValue(rsRecords.getString("CodigoCC"));
                            facturasContainer.getContainerProperty(itemId, CODIGO_PARTIDA_PROPERTY).setValue(rsRecords.getString("CodigoPartida"));
                        }
                    }
                } while (rsRecords.next());
            }

        } catch (Exception ex) {
            System.out.println("Error al listar tabla Facturas en AutorizarPagoFacturaForm: " + ex.getMessage());
            ex.printStackTrace();
        }

        ((AutorizacionesPagoView) (mainUI.getNavigator().getCurrentView())).pagoDocumentoBtn.setEnabled(true);
    }

    public void llenarTablaAnticipos() {
        anticiposPagoContainer.removeAllItems();
        anticiposPagoContainer.removeAllContainerFilters();

        double totalMontoAnticipo = 0.00;
        double totalSaldoAnticipo = 0.00;

        String proveedorSeleccionado  = String.valueOf(facturasContainer.getContainerProperty(facturasGrid.getSelectedRow(), ID_PROVEEDOR_PROPERTY).getValue());
        String tipoMonedaSeleccionado = String.valueOf(facturasContainer.getContainerProperty(facturasGrid.getSelectedRow(), MONEDA_PROPERTY).getValue());

        queryString  = " SELECT contabilidad_partida.CodigoPartida, contabilidad_partida.CodigoCC, SUM(DEBE) MontoAnticipo, ";
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
            stQuery   = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery1  = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                do {
                    queryString  = " SELECT IFNULL(SUM(Monto),0) TOTALOCUPADO";
                    queryString += " FROM autorizacion_pago";
                    queryString += " WHERE IdProveedor = " + proveedorSeleccionado;
                    queryString += " AND IdEmpresa = " + empresaId;
                    queryString += " AND CodigoCCRelacionado = '" + rsRecords.getString("CodigoCC") + "'";

                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, "-->query obtener saldo real anticipo : " + queryString);
                    rsRecords1 = stQuery1.executeQuery(queryString);
                    rsRecords1.next();

                    Object itemId = anticiposPagoContainer.addItem();
                    anticiposPagoContainer.getContainerProperty(itemId, CODIGO_PARTIDA2_PROPERTY).setValue(rsRecords.getString("CodigoPartida"));
                    anticiposPagoContainer.getContainerProperty(itemId, CODIGO_CC2_PROPERTY).setValue(rsRecords.getString("CodigoCC"));
                    anticiposPagoContainer.getContainerProperty(itemId, FECHA2__PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha")));
                    anticiposPagoContainer.getContainerProperty(itemId, DOCUMENTO_PROPERTY).setValue("ANTICIPO A PROVEEDORES");
                    anticiposPagoContainer.getContainerProperty(itemId, MONTO2_PROPERTY).setValue(numberFormat.format(rsRecords.getDouble("MontoAnticipo")));
                    anticiposPagoContainer.getContainerProperty(itemId, SALDO2_PROPERTY).setValue(numberFormat.format(rsRecords.getDouble("TOTALSALDO") - rsRecords1.getDouble("TOTALOCUPADO")));
                    anticiposPagoContainer.getContainerProperty(itemId, UTILIZAR_PROPERTY).setValue(numberFormat.format(0.00));

                    totalMontoAnticipo += rsRecords.getDouble("MontoAnticipo");
                    totalSaldoAnticipo += rsRecords.getDouble("TOTALSALDO");

                } while (rsRecords.next());
            }
        } catch (Exception ex) {
            System.out.println("Error al listar grid anticipos factura : " + ex);
            ex.printStackTrace();
        }
    }

    private Field<?> getAmmountField(String propertyId) {
        NumberField valueTxt = new NumberField("Monto :");
        valueTxt.setWidth("10em");
        valueTxt.setDecimalAllowed(true);
        valueTxt.setDecimalPrecision(2);
        valueTxt.setMinimumFractionDigits(2);
        valueTxt.setDecimalSeparator('.');
        valueTxt.setDecimalSeparatorAlwaysShown(true);
        valueTxt.setValue(0d);
        valueTxt.setGroupingUsed(true);
        valueTxt.setGroupingSeparator(',');
        valueTxt.setGroupingSize(3);
        valueTxt.setImmediate(true);
        valueTxt.selectAll();
        valueTxt.setDescription("Doble click para seleccionar todo el monto...");
        valueTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        valueTxt.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (event.getProperty().getValue() != null) {
                    if (!String.valueOf(event.getProperty().getValue()).trim().isEmpty()) {
                        if (anticiposPagoContainer.size() > 0) {
                            for (Object itemId : anticiposPagoContainer.getItemIds()) {
                                Item item = anticiposPagoContainer.getItem(itemId);
                                Object propertyValue  = item.getItemProperty(propertyId).getValue();
                                Object propertyValue2 = item.getItemProperty(SALDO2_PROPERTY).getValue();
                                if (Double.valueOf(String.valueOf(propertyValue).replaceAll(",", "")) >
                                        Double.valueOf(String.valueOf(propertyValue2).replaceAll(",", ""))) {
                                    Notification.show("El Monto utilizar no puede ser mayor al monto del anticipo", Notification.Type.ERROR_MESSAGE);
                                    valueTxt.setValue(0.00);
                                    return;
                                }
                            }
                            setFooterTotal(propertyId);
                        }
                    }
                }
            }
        });
        return valueTxt;
    }

    private void setFooterTotal(String propertyId) {
        double total       = 0.00;
        double montoCheque = 0.00;

        for (Object itemId : anticiposPagoContainer.getItemIds()) {
            Item item = anticiposPagoContainer.getItem(itemId);
            Object propertyValue = item.getItemProperty(propertyId).getValue();
            total += Double.valueOf(String.valueOf(propertyValue));
        }

        totalUtilizarAnticiposTxt.setReadOnly(false);
        totalUtilizarAnticiposTxt.setValue(total);
        totalUtilizarAnticiposTxt.setReadOnly(true);

        montoCheque = saldoFacturaSeleccionada - totalUtilizarAnticiposTxt.getDoubleValueDoNotThrow();
        if (montoCheque < 0) montoCheque = 0.00;
        montoPendienteChequeTxt.setValue(montoCheque);
    }

    private void actualizarFactura() {
        try {
            double montoAuotizadoPagar = montoPendienteChequeTxt.getDoubleValueDoNotThrow()
                    + Double.valueOf(String.valueOf(
                            facturasContainer.getContainerProperty(
                                    facturasGrid.getSelectedRow(), MONTO_AUTORIZADO_PROPERTY).getValue())
                    .replaceAll("Q.", "").replaceAll("\\$.", "").replaceAll(",", ""));

            double montoAplicarAnticipo = totalUtilizarAnticiposTxt.getDoubleValueDoNotThrow()
                    + Double.valueOf(String.valueOf(
                            facturasContainer.getContainerProperty(
                                    facturasGrid.getSelectedRow(), MONTO_ANTICIPO_PROPERTY).getValue())
                    .replaceAll("Q.", "").replaceAll("\\$.", "").replaceAll(",", ""));

            queryString  = "UPDATE contabilidad_partida SET ";
            queryString += " MontoAutorizadoPagar = " + montoAuotizadoPagar;
            queryString += ", MontoAplicarAnticipo = " + montoAplicarAnticipo;
            queryString += " WHERE CodigoPartida = '" + String.valueOf(facturasContainer.getContainerProperty(facturasGrid.getSelectedRow(), CODIGO_PARTIDA_PROPERTY).getValue()) + "'";
            queryString += " AND IdEmpresa = " + empresaId;

            Logger.getLogger(this.getClass().getName()).log(Level.INFO,
                    Utileria.getFechaDDMMYYYY_HHMM() + " actualizar FACTURA AUTORIZADA PARA PAGAR..." + queryString);

            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            queryString  = "  INSERT INTO autorizacion_pago (TipoAutorizacion, IdEmpresa, IdProveedor, ";
            queryString += " Fecha, Moneda, Monto, CodigoCC, CodigoCCRelacionado, CuentaContableLiquidar, ";
            queryString += " Objetivo, CreadoUsuario, CreadoFechaYHora)";
            queryString += " VALUES ";
            queryString += "(";
            queryString += "'" + AutorizacionesPagoView.PAGO_DOCUMENTO + "'";
            queryString += "," + empresaId;
            queryString += "," + String.valueOf(facturasContainer.getContainerProperty(facturasGrid.getSelectedRow(), ID_PROVEEDOR_PROPERTY).getValue());
            queryString += ",current_date";
            queryString += ",'" + String.valueOf(facturasContainer.getContainerProperty(facturasGrid.getSelectedRow(), MONEDA_PROPERTY).getValue()) + "'";
            queryString += "," + montoAuotizadoPagar;
            queryString += ",'" + String.valueOf(facturasContainer.getContainerProperty(facturasGrid.getSelectedRow(), CODIGO_CC_PROPERTY).getValue()) + "'";
            queryString += ",''";
            queryString += ",''";
            queryString += ",'" + AutorizacionesPagoView.PAGO_DOCUMENTO + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";

            stQuery.executeUpdate(queryString);

            insertAnticiposAutorizadosFactura();

        } catch (Exception ex) {
            Notification notif = new Notification("HA OCURRIDO UN ERROR DE BASE DE DATOS : " + ex.getMessage(),
                    Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.WARNING);
            notif.show(Page.getCurrent());
            try {
                String[] emailsTo = {"alerta@simpletecno.com"};
                MyEmailMessanger eMail = new MyEmailMessanger();
                eMail.postMail(emailsTo, "Error en SOPDI", "Error en base de datos :  " + this.getClass().getName() + " --> " + ex.getMessage());
            } catch (MessagingException ex2) {
                Logger.getLogger(SopdiUI.class.getName()).log(Level.SEVERE, null, ex2);
            }
            ex.printStackTrace();
        }
    }

    private void insertAnticiposAutorizadosFactura() {
        String proveedorSeleccionado = String.valueOf(facturasContainer.getContainerProperty(facturasGrid.getSelectedRow(), ID_PROVEEDOR_PROPERTY).getValue());
        String codigoCC              = String.valueOf(facturasContainer.getContainerProperty(facturasGrid.getSelectedRow(), CODIGO_CC_PROPERTY).getValue());
        String moneda                = String.valueOf(facturasContainer.getContainerProperty(facturasGrid.getSelectedRow(), MONEDA_PROPERTY).getValue());

        try {
            for (Object itemId : anticiposPagoContainer.getItemIds()) {
                Item item = anticiposPagoContainer.getItem(itemId);
                Object codigoCCAnticipo   = item.getItemProperty(CODIGO_CC2_PROPERTY).getValue();
                Object montoUtilziar      = item.getItemProperty(UTILIZAR_PROPERTY).getValue();
                double montoUtilizarVariable = Double.parseDouble(String.valueOf(montoUtilziar).replaceAll(",", ""));

                if (montoUtilizarVariable > 0) {
                    queryString  = " DELETE FROM autorizacion_pago ";
                    queryString += " WHERE CodigoCCRelacionado = '" + codigoCCAnticipo + "'";
                    queryString += " AND   CodigoCC = '" + codigoCC + "'";
                    stQuery.executeUpdate(queryString);

                    queryString  = "  INSERT INTO autorizacion_pago (TipoAutorizacion, IdEmpresa, IdProveedor, ";
                    queryString += " Fecha, Moneda, Monto, CodigoCC, CodigoCCRelacionado, CuentaContableLiquidar, ";
                    queryString += " Objetivo, CreadoUsuario, CreadoFechaYHora)";
                    queryString += " VALUES ";
                    queryString += "(";
                    queryString += "'" + AutorizacionesPagoView.PAGO_DOCUMENTO + "'";
                    queryString += "," + empresaId;
                    queryString += "," + proveedorSeleccionado;
                    queryString += ",current_date";
                    queryString += ",'" + moneda + "'";
                    queryString += "," + montoUtilizarVariable;
                    queryString += ",'" + codigoCC + "'";
                    queryString += ",'" + codigoCCAnticipo + "'";
                    queryString += ",''";
                    queryString += ",'" + AutorizacionesPagoView.PAGO_DOCUMENTO + "'";
                    queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                    queryString += ",current_timestamp";
                    queryString += ")";
                    stQuery.executeUpdate(queryString);
                }
            }
        } catch (SQLException ex) {
            System.out.println("Error al insertar en la tabla autorizacion_pago (FACTURA)" + ex);
            Logger.getLogger(AutorizarPagoFacturaForm.class.getName()).log(Level.SEVERE, null, ex);
            Notification notif = new Notification("HA OCURRIDO UN ERROR DE BASE DE DATOS : " + ex.getMessage(),
                    Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.WARNING);
            notif.show(Page.getCurrent());
            try {
                String[] emailsTo = {"alerta@simpletecno.com"};
                MyEmailMessanger eMail = new MyEmailMessanger();
                eMail.postMail(emailsTo, "Error en SOPDI", "Error en base de datos :  " + this.getClass().getName() + " --> " + ex.getMessage());
            } catch (MessagingException ex2) {
                Logger.getLogger(SopdiUI.class.getName()).log(Level.SEVERE, null, ex2);
            }
            return;
        }

        montoPendienteChequeTxt.setValue(0.00);

        totalUtilizarAnticiposTxt.setReadOnly(false);
        totalUtilizarAnticiposTxt.setValue(0.00);
        totalUtilizarAnticiposTxt.setReadOnly(true);

        facturasContainer.removeItem(facturasGrid.getSelectedRow());

        Notification notif = new Notification("AUTORIZACIÓN EXITOSA", Notification.Type.HUMANIZED_MESSAGE);
        notif.setDelayMsec(1500);
        notif.setPosition(Position.MIDDLE_CENTER);
        notif.setIcon(FontAwesome.CHECK);
        notif.show(Page.getCurrent());
    }
}