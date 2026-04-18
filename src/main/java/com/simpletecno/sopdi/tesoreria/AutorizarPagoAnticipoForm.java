package com.simpletecno.sopdi.tesoreria;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.renderers.ClickableRenderer;
import com.vaadin.ui.themes.ValoTheme;

import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.logging.Logger;

import org.vaadin.ui.NumberField;

/**
 * Formulario de Autorización de Pago de Anticipos a Proveedores.
 * UI modernizada: header ámbar, secciones en card, botones estilizados.
 *
 * @author user
 */
public class AutorizarPagoAnticipoForm extends Window {

    // ── Constantes de columnas ───────────────────────────────────────────────
    static final String CODIGO_PARTIDA  = "Codigo";
    static final String CODIGO_CC       = "CodigoCC";
    static final String FECHA           = "Fecha";
    static final String MONEDA_DOCUMENTO = "Moneda";
    static final String DEBE            = "Debe";
    static final String HABER           = "Haber";
    static final String TIPO_CAMBIO     = "Tasa";
    static final String DEBE_QUETZALES  = "Debe Q.";
    static final String HABER_QUETZALES = "Haber Q.";
    static final String SALDO           = "Saldo";
    static final String ACCION          = "Autorizar";

    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");

    // ── DB ───────────────────────────────────────────────────────────────────
    Statement  stQuery, stQuery1;
    ResultSet  rsRecords, rsRecords1;
    String     queryString;

    // ── Contenedor y Grid ────────────────────────────────────────────────────
    public IndexedContainer container = new IndexedContainer();
    Grid             anticiposGrind;
    Grid.FooterRow   anticiposFooter;

    // ── Controles ────────────────────────────────────────────────────────────
    NumberField montoAutorizarTxt;
    ComboBox    proveedorCbx;
    ComboBox    monedaCbx;
    Button      salirBtn;
    Button      autorizarBtn;

    // ── Estado ───────────────────────────────────────────────────────────────
    double totalDebeQuetzales = 0.00, totalHaberQueztales = 0.00;
    double totalDebe = 0.00, totalHaber = 0.00;
    double saldo = 0.00;

    String tipo;
    UI     mainUI;

    String empresaId     = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    // ── Layout principal ─────────────────────────────────────────────────────
    VerticalLayout mainLayout;

    // ────────────────────────────────────────────────────────────────────────
    public AutorizarPagoAnticipoForm(String tipo) {
        this.tipo   = tipo;
        this.mainUI = UI.getCurrent();

        injectStyles();

        setModal(true);
        setResizable(true);
        setDraggable(true);
        setResponsive(true);
        setWidth("92%");
        setHeight("92%");

        mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setSpacing(false);
        mainLayout.setMargin(false);
        mainLayout.addStyleName("apa-main");

        setContent(mainLayout);

        buildHeader();
        buildFiltrosSection();
        createTablaAnticipos();
        crearComponentes();
    }

    // ── CSS ──────────────────────────────────────────────────────────────────
    private void injectStyles() {
        Page.getCurrent().getStyles().add(

                /* ── Fondo general ──────────────────────────────────────────── */
                ".apa-main {" +
                "  background: #F4F6F9;" +
                "}" +

                /* ── Header ─────────────────────────────────────────────────── */
                ".apa-header {" +
                "  background: linear-gradient(135deg, #E65100 0%, #F57C00 100%);" +
                "  border-radius: 10px;" +
                "  padding: 12px 18px !important;" +
                "  margin: 6px 6px 4px 6px;" +
                "}" +
                ".apa-header-icon {" +
                "  font-size: 26px !important;" +
                "  line-height: 1;" +
                "}" +
                ".apa-header-title {" +
                "  color: #ffffff !important;" +
                "  font-size: 16px !important;" +
                "  font-weight: 700 !important;" +
                "  margin: 0 !important;" +
                "  letter-spacing: 0.02em;" +
                "}" +
                ".apa-header-sub {" +
                "  color: #FFE0B2 !important;" +
                "  font-size: 11px !important;" +
                "  margin: 2px 0 0 0 !important;" +
                "}" +

                /* ── Sección card ────────────────────────────────────────────── */
                ".apa-section {" +
                "  background: #ffffff;" +
                "  border-radius: 8px;" +
                "  box-shadow: 0 2px 8px rgba(0,0,0,0.07);" +
                "  padding: 6px 10px !important;" +
                "  margin: 0 6px 4px 6px;" +
                "}" +
                ".apa-section-label {" +
                "  color: #E65100;" +
                "  font-size: 11px !important;" +
                "  font-weight: 700 !important;" +
                "  letter-spacing: 0.09em;" +
                "  text-transform: uppercase;" +
                "  border-bottom: 2px solid #FFE0B2;" +
                "  padding-bottom: 2px;" +
                "  margin-bottom: 4px !important;" +
                "  display: block;" +
                "  width: 100%;" +
                "}" +

                /* ── Grids ───────────────────────────────────────────────────── */
                ".apa-main .v-grid-header {" +
                "  background: #FFF3E0 !important;" +
                "  font-weight: 600 !important;" +
                "  color: #BF360C !important;" +
                "}" +
                ".apa-main .v-grid-row:hover > td {" +
                "  background: #FFF8F0 !important;" +
                "}" +
                ".apa-main .v-grid-row-selected > td {" +
                "  background: #FFCC80 !important;" +
                "  color: #BF360C !important;" +
                "  font-weight: 600;" +
                "}" +
                ".apa-main .v-grid-footer {" +
                "  background: #FFF3E0 !important;" +
                "  font-weight: 700 !important;" +
                "  border-top: 2px solid #FFCC80 !important;" +
                "}" +

                /* ── Botón Autorizar ─────────────────────────────────────────── */
                ".apa-btn-autorizar.v-button {" +
                "  background: linear-gradient(135deg, #E65100 0%, #F57C00 100%) !important;" +
                "  color: #fff !important;" +
                "  border: none !important;" +
                "  border-radius: 6px !important;" +
                "  font-weight: 700 !important;" +
                "  padding: 0 24px !important;" +
                "  height: 36px !important;" +
                "  box-shadow: 0 2px 8px rgba(230,81,0,0.35) !important;" +
                "}" +
                ".apa-btn-autorizar.v-button:hover {" +
                "  background: linear-gradient(135deg, #F57C00 0%, #FF9800 100%) !important;" +
                "}" +

                /* ── Botón Salir ─────────────────────────────────────────────── */
                ".apa-btn-salir.v-button {" +
                "  border: 1px solid #CFD8DC !important;" +
                "  border-radius: 6px !important;" +
                "  color: #546E7A !important;" +
                "  background: #fff !important;" +
                "  height: 36px !important;" +
                "  padding: 0 18px !important;" +
                "}" +
                ".apa-btn-salir.v-button:hover {" +
                "  background: #F5F5F5 !important;" +
                "}" +

                /* ── Barra de acciones inferior ──────────────────────────────── */
                ".apa-action-bar {" +
                "  border-top: 1px solid #FFE0B2;" +
                "  padding-top: 8px;" +
                "  margin-top: 4px;" +
                "  width: 100%;" +
                "}" +

                /* ── Chip de monto ───────────────────────────────────────────── */
                ".apa-amount-chip {" +
                "  background: #FFF3E0;" +
                "  border-radius: 6px;" +
                "  padding: 2px 8px !important;" +
                "  border: 1px solid #FFCC80;" +
                "}"
        );
    }

    // ── Header ───────────────────────────────────────────────────────────────
    private void buildHeader() {
        Label iconLbl = new Label("💸");
        iconLbl.addStyleName("apa-header-icon");
        iconLbl.setSizeUndefined();

        Label titleLbl = new Label(empresaId + "  ·  " + empresaNombre + "  —  " + tipo);
        titleLbl.addStyleName("apa-header-title");
        titleLbl.setSizeUndefined();

        Label subLbl = new Label("Seleccione el proveedor, moneda y monto para registrar la autorización de anticipo");
        subLbl.addStyleName("apa-header-sub");
        subLbl.setSizeUndefined();

        VerticalLayout textCol = new VerticalLayout();
        textCol.setMargin(false);
        textCol.setSpacing(false);
        textCol.addComponents(titleLbl, subLbl);

        HorizontalLayout headerHL = new HorizontalLayout();
        headerHL.addStyleName("apa-header");
        headerHL.setWidth("100%");
        headerHL.setSpacing(true);
        headerHL.setMargin(false);
        headerHL.addComponents(iconLbl, textCol);
        headerHL.setExpandRatio(textCol, 1f);
        headerHL.setComponentAlignment(iconLbl, Alignment.MIDDLE_LEFT);
        headerHL.setComponentAlignment(textCol, Alignment.MIDDLE_LEFT);

        mainLayout.addComponent(headerHL);
    }

    // ── Sección de filtros ────────────────────────────────────────────────────
    private void buildFiltrosSection() {

        proveedorCbx = new ComboBox("Proveedor");
        proveedorCbx.setWidth("30em");
        proveedorCbx.setFilteringMode(FilteringMode.CONTAINS);
        proveedorCbx.setInvalidAllowed(false);
        proveedorCbx.setNewItemsAllowed(false);
        proveedorCbx.setNullSelectionAllowed(false);
        proveedorCbx.addValueChangeListener(event -> {
            if (proveedorCbx.getValue() != null && !proveedorCbx.getValue().equals("0")) {
                montoAutorizarTxt.setValue("0.00");
                llenarTablaAnticipos();
            }
        });
        llenarComboProveedor();

        monedaCbx = new ComboBox("Moneda");
        monedaCbx.setWidth("10em");
        monedaCbx.addItem("QUETZALES");
        monedaCbx.addItem("DOLARES");
        monedaCbx.select("QUETZALES");
        monedaCbx.setInvalidAllowed(false);
        monedaCbx.setNewItemsAllowed(false);

        montoAutorizarTxt = new NumberField("Monto");
        montoAutorizarTxt.setDecimalAllowed(true);
        montoAutorizarTxt.setDecimalPrecision(2);
        montoAutorizarTxt.setMinimumFractionDigits(2);
        montoAutorizarTxt.setDecimalSeparator('.');
        montoAutorizarTxt.setDecimalSeparatorAlwaysShown(true);
        montoAutorizarTxt.setValue(0d);
        montoAutorizarTxt.setGroupingUsed(true);
        montoAutorizarTxt.setGroupingSeparator(',');
        montoAutorizarTxt.setGroupingSize(3);
        montoAutorizarTxt.setImmediate(true);
        montoAutorizarTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        montoAutorizarTxt.addStyleName("apa-amount-chip");
        montoAutorizarTxt.setWidth("10em");

        autorizarBtn = new Button("Autorizar anticipo");
        autorizarBtn.setIcon(FontAwesome.CHECK_CIRCLE);
        autorizarBtn.addStyleName("apa-btn-autorizar");
        autorizarBtn.addClickListener(event -> insertTablaAnticipo());

        HorizontalLayout filtrosHL = new HorizontalLayout();
        filtrosHL.addStyleName(ValoTheme.LAYOUT_HORIZONTAL_WRAPPING);
        filtrosHL.setSpacing(true);
        filtrosHL.setMargin(false);
        filtrosHL.setSizeUndefined();
        filtrosHL.addComponents(proveedorCbx, monedaCbx, montoAutorizarTxt, autorizarBtn);
        filtrosHL.setComponentAlignment(autorizarBtn, Alignment.BOTTOM_LEFT);

        VerticalLayout filtrosSection = buildSection("🔍  Parámetros de autorización");
        filtrosSection.addComponent(filtrosHL);

        mainLayout.addComponent(filtrosSection);
    }

    // ── Grid de Anticipos ────────────────────────────────────────────────────
    public void createTablaAnticipos() {

        container.addContainerProperty(CODIGO_PARTIDA,  String.class, null);
        container.addContainerProperty(CODIGO_CC,       String.class, null);
        container.addContainerProperty(FECHA,           String.class, null);
        container.addContainerProperty(MONEDA_DOCUMENTO, String.class, null);
        container.addContainerProperty(DEBE,            String.class, null);
        container.addContainerProperty(HABER,           String.class, null);
        container.addContainerProperty(TIPO_CAMBIO,     String.class, null);
        container.addContainerProperty(DEBE_QUETZALES,  String.class, null);
        container.addContainerProperty(HABER_QUETZALES, String.class, null);
        container.addContainerProperty(SALDO,           String.class, null);
        container.addContainerProperty(ACCION,          String.class, "Des-autorizar");

        anticiposGrind = new Grid("", container);
        anticiposGrind.setSizeFull();
        anticiposGrind.setImmediate(true);
        anticiposGrind.setSelectionMode(Grid.SelectionMode.SINGLE);
        anticiposGrind.setDescription("Seleccione un registro.");
        anticiposGrind.setResponsive(true);
        anticiposGrind.setEditorBuffered(false);

        anticiposGrind.getColumn(CODIGO_PARTIDA).setHidable(true).setHidden(true);
        anticiposGrind.getColumn(CODIGO_CC).setHidable(true).setHidden(true);

        anticiposGrind.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            String prop = String.valueOf(cellReference.getPropertyId());
            if (DEBE.equals(prop) || HABER.equals(prop) ||
                    DEBE_QUETZALES.equals(prop) || HABER_QUETZALES.equals(prop) ||
                    SALDO.equals(prop)) {
                return "rightalign";
            }
            return null;
        });

        anticiposGrind.getColumn(ACCION)
                .setRenderer(new ButtonRenderer(e -> updateAnticipo(e)))
                .setWidth(120);

        anticiposFooter = anticiposGrind.appendFooterRow();
        anticiposFooter.getCell(MONEDA_DOCUMENTO).setText("TOTALES");
        anticiposFooter.getCell(TIPO_CAMBIO).setText("TOTALES");
        anticiposFooter.getCell(DEBE).setText("0.00");
        anticiposFooter.getCell(HABER).setText("0.00");
        anticiposFooter.getCell(DEBE_QUETZALES).setText("0.00");
        anticiposFooter.getCell(HABER_QUETZALES).setText("0.00");
        anticiposFooter.getCell(SALDO).setText("0.00");

        anticiposFooter.getCell(DEBE).setStyleName("rightalign");
        anticiposFooter.getCell(HABER).setStyleName("rightalign");
        anticiposFooter.getCell(DEBE_QUETZALES).setStyleName("rightalign");
        anticiposFooter.getCell(HABER_QUETZALES).setStyleName("rightalign");
        anticiposFooter.getCell(SALDO).setStyleName("rightalign");

        // Card de la sección — expande para llenar el espacio restante
        VerticalLayout anticiposSection = buildSection("📋  Historial de anticipos activos del proveedor");
        anticiposSection.setSizeFull();
        anticiposSection.addComponent(anticiposGrind);
        anticiposSection.setExpandRatio(anticiposGrind, 1f);

        mainLayout.addComponent(anticiposSection);
        mainLayout.setExpandRatio(anticiposSection, 1f);
    }

    // ── Barra inferior con botón Salir ───────────────────────────────────────
    public void crearComponentes() {

        salirBtn = new Button("Salir");
        salirBtn.setIcon(FontAwesome.SIGN_OUT);
        salirBtn.addStyleName("apa-btn-salir");
        salirBtn.addClickListener(event -> close());

        HorizontalLayout actionBar = new HorizontalLayout();
        actionBar.addStyleName("apa-action-bar");
        actionBar.setMargin(false);
        actionBar.setSpacing(true);
        actionBar.setSizeUndefined();
        actionBar.addComponent(salirBtn);
        actionBar.setComponentAlignment(salirBtn, Alignment.MIDDLE_LEFT);

        HorizontalLayout wrapper = new HorizontalLayout();
        wrapper.setWidth("100%");
        wrapper.setMargin(false);
        wrapper.setSpacing(false);
        wrapper.addStyleName("apa-section");
        wrapper.addComponent(actionBar);

        mainLayout.addComponent(wrapper);
    }

    // ── Helper: card de sección ───────────────────────────────────────────────
    private VerticalLayout buildSection(String title) {
        Label sectionLbl = new Label(title);
        sectionLbl.addStyleName("apa-section-label");
        sectionLbl.setWidth("100%");

        VerticalLayout section = new VerticalLayout();
        section.addStyleName("apa-section");
        section.setWidth("100%");
        section.setSpacing(false);
        section.setMargin(false);
        section.addComponent(sectionLbl);
        return section;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  LÓGICA DE NEGOCIO — sin cambios funcionales
    // ════════════════════════════════════════════════════════════════════════

    public void llenarComboProveedor() {
        queryString  = " SELECT * FROM proveedor_empresa ";
        queryString += " WHERE Inhabilitado = 0";
        if (tipo.equals(AutorizacionesPagoView.ANTICIPO_PROVEEDOR)) {
            queryString += " And EsProveedor = 1";
        }
        queryString += " AND IdEmpresa = " + empresaId;
        queryString += " ORDER BY Nombre ";

        proveedorCbx.removeAllItems();

        try {
            stQuery   = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);
            while (rsRecords.next()) {
                proveedorCbx.addItem(rsRecords.getString("IDProveedor"));
                proveedorCbx.setItemCaption(rsRecords.getString("IDProveedor"), rsRecords.getString("Nombre"));
            }
        } catch (Exception ex1) {
            System.out.println("Error al listar Proveedores " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    private void updateAnticipo(ClickableRenderer.RendererClickEvent e) {
        String idAnticipo = String.valueOf(container.getContainerProperty(e.getItemId(), CODIGO_PARTIDA).getValue());

        queryString  = "UPDATE autorizacion_pago SET  ";
        queryString += " CodigoCC = '" + "TEMP_" + new java.util.Date().getTime() + "'";
        queryString += " WHERE IdAutorizacion = " + idAnticipo;

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            Notification notif = new Notification("AUTORIZACIÓN EXITOSA", Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.CHECK);
            notif.show(Page.getCurrent());

            montoAutorizarTxt.setValue("0.00");

        } catch (Exception ex1) {
            System.out.println("Error al actualizar registro en autorizacion_pago: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void llenarTablaAnticipos() {
        container.removeAllItems();
        anticiposFooter.getCell(DEBE).setText("0.00");
        anticiposFooter.getCell(HABER).setText("0.00");
        anticiposFooter.getCell(DEBE_QUETZALES).setText("0.00");
        anticiposFooter.getCell(HABER_QUETZALES).setText("0.00");
        anticiposFooter.getCell(SALDO).setText("0.00");

        totalDebeQuetzales = 0.00;
        totalHaberQueztales = 0.00;
        totalDebe  = 0.00;
        totalHaber = 0.00;
        saldo      = 0.00;

        queryString  = " SELECT CodigoPartida, CodigoCC, Fecha, MonedaDocumento, Debe, Haber,  ";
        queryString += " TipoCambio, DebeQuetzales, HaberQuetzales, IdNomenclatura ";
        queryString += " FROM contabilidad_partida";
        queryString += " WHERE IdEmpresa = " + empresaId;
        queryString += " AND   Fecha >= '2020-01-01'";
        queryString += " AND   Upper(TipoDocumento) IN ('CHEQUE','TRANSFERENCIA', 'NOTA DE DEBITO')";
        queryString += " AND   IdProveedor = " + proveedorCbx.getValue();
        if (tipo.equals(AutorizacionesPagoView.ANTICIPO_PROVEEDOR)) {
            queryString += " AND IdNomenclatura = " + ((SopdiUI) mainUI).cuentasContablesDefault.getAnticiposProveedor();
        } else {
            queryString += " AND IdNomenclatura = " + ((SopdiUI) mainUI).cuentasContablesDefault.getAnticiposSueldos();
        }
        queryString += " GROUP by CodigoPartida ";
        queryString += " ORDER by CodigoPartida";

        Logger.getLogger(AutorizacionesPagoView.class.getName()).info(queryString);

        try {
            stQuery   = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);
            stQuery1  = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            if (rsRecords.next()) {
                do {
                    queryString  = " SELECT ";
                    queryString += " SUM(HABER - DEBE) TOTALSALDO, SUM(HaberQuetzales - DebeQuetzales) TOTALSALDOQ ";
                    queryString += " FROM contabilidad_partida";
                    queryString += " WHERE IdEmpresa = " + empresaId;
                    queryString += " AND CodigoCC = '" + rsRecords.getString("CodigoCC") + "'";
                    queryString += " AND IdNomenclatura = " + rsRecords.getString("IdNomenclatura");

                    rsRecords1 = stQuery1.executeQuery(queryString);

                    if (rsRecords1.next()) {
                        if (rsRecords1.getDouble("TOTALSALDO") > 0.00) {
                            Object itemId = container.addItem();
                            container.getContainerProperty(itemId, CODIGO_PARTIDA).setValue(rsRecords.getString("CodigoPartida"));
                            container.getContainerProperty(itemId, CODIGO_CC).setValue(rsRecords.getString("CodigoCC"));
                            container.getContainerProperty(itemId, FECHA).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha")));
                            container.getContainerProperty(itemId, MONEDA_DOCUMENTO).setValue(rsRecords.getString("MonedaDocumento"));
                            container.getContainerProperty(itemId, DEBE).setValue(numberFormat.format(rsRecords.getDouble("Debe")));
                            container.getContainerProperty(itemId, HABER).setValue(numberFormat.format(rsRecords.getDouble("Haber")));
                            container.getContainerProperty(itemId, TIPO_CAMBIO).setValue(numberFormat.format(rsRecords.getDouble("TipoCambio")));
                            container.getContainerProperty(itemId, DEBE_QUETZALES).setValue(numberFormat.format(rsRecords.getDouble("DebeQuetzales")));
                            container.getContainerProperty(itemId, HABER_QUETZALES).setValue(numberFormat.format(rsRecords.getDouble("HaberQuetzales")));
                            container.getContainerProperty(itemId, SALDO).setValue(numberFormat.format(rsRecords1.getDouble("TOTALSALDO")));

                            totalDebe           += rsRecords.getDouble("Debe");
                            totalHaber          += rsRecords.getDouble("Haber");
                            totalDebeQuetzales  += rsRecords.getDouble("DebeQuetzales");
                            totalHaberQueztales += rsRecords.getDouble("HaberQuetzales");
                            saldo               += rsRecords1.getDouble("TOTALSALDO");
                        }
                    }
                } while (rsRecords.next());

                anticiposFooter.getCell(DEBE).setText(numberFormat.format(totalDebe));
                anticiposFooter.getCell(HABER).setText(numberFormat.format(totalHaber));
                anticiposFooter.getCell(DEBE_QUETZALES).setText(numberFormat.format(totalDebeQuetzales));
                anticiposFooter.getCell(HABER_QUETZALES).setText(numberFormat.format(totalHaberQueztales));
                anticiposFooter.getCell(SALDO).setText(numberFormat.format(saldo));
            }
        } catch (Exception ex) {
            System.out.println("Error al listar anticipos VIVOS. " + ex);
            ex.printStackTrace();
        }
    }

    public void insertTablaAnticipo() {
        if (proveedorCbx.getValue() == null || proveedorCbx.getValue().equals("0")) {
            Notification.show("Por favor, seleccione un proveedor.", Notification.Type.ERROR_MESSAGE);
            proveedorCbx.focus();
            return;
        }
        if (montoAutorizarTxt.getDoubleValueDoNotThrow() == 0.00) {
            Notification.show("Por favor, ingrese un monto.", Notification.Type.ERROR_MESSAGE);
            montoAutorizarTxt.focus();
            return;
        }

        queryString  = "INSERT INTO autorizacion_pago (TipoAutorizacion, IdEmpresa, IdProveedor, ";
        queryString += " Fecha, Moneda, Monto, CodigoCC, CuentaContableLiquidar, ";
        queryString += " Objetivo, CreadoUsuario, CreadoFechaYHora)";
        queryString += " VALUES ";
        queryString += "(";
        queryString += "'" + tipo + "'";
        queryString += "," + empresaId;
        queryString += "," + proveedorCbx.getValue();
        queryString += ",current_date";
        queryString += ",'" + monedaCbx.getValue() + "'";
        queryString += "," + montoAutorizarTxt.getValue();
        queryString += ",'" + "TEMP_" + new java.util.Date().getTime() + "'";
        queryString += ",''";
        queryString += ",'" + tipo + "'";
        queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
        queryString += ",current_timestamp";
        queryString += ")";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            Notification notif = new Notification("AUTORIZACIÓN EXITOSA", Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.CHECK);
            notif.show(Page.getCurrent());

            montoAutorizarTxt.setValue("0.00");

        } catch (Exception ex1) {
            System.out.println("Error al insertar registro en autorizacion_pago: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }
}
