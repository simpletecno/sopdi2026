package com.simpletecno.sopdi.tesoreria;

import com.simpletecno.sopdi.utilerias.MyEmailMessanger;
import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.SelectionEvent;
import com.vaadin.event.SelectionEvent.SelectionListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Field;
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.vaadin.ui.NumberField;

import javax.mail.MessagingException;

/**
 * @author user
 */
public class AutorizarPagoFacturaForm extends Window {

    VerticalLayout mainLayout;

    static final String TIPO_DOCUMENTO_PROPERTY = "T. Documento";
    static final String ID_PROVEEDOR_PROPERTY = "Id Proveedor";
    static final String PROVEEDOR_PROPERTY = "Proveedor";
    static final String FECHA_PROPERTY = "Fecha";
    static final String NUMERO_FACTURA_PROPERTY = "Número";
    static final String DESCRIPCION_PROPERTY = "Descripción";
    static final String MONEDA_PROPERTY = "Moneda";
    static final String MONTO_PROPERTY = "Monto";
    static final String SALDO_PROPERTY = "Saldo";
    static final String MONTO_AUTORIZADO_PROPERTY = "M. Autorizado";
    static final String MONTO_ANTICIPO_PROPERTY = "M. Anticipo";
    static final String CODIGO_PARTIDA_PROPERTY = "Codigo partida";
    static final String CODIGO_CC_PROPERTY = "CodigoCC";

    static final String CODIGO_PARTIDA2_PROPERTY = "CodigoPartida";
    static final String CODIGO_CC2_PROPERTY = "CodigoCC";
    static final String FECHA2__PROPERTY = "Fecha";
    static final String DOCUMENTO_PROPERTY = "Cheque/Transf";
    static final String MONTO2_PROPERTY = "Monto";
    static final String SALDO2_PROPERTY = "Saldo";
    static final String UTILIZAR_PROPERTY = "Utilizar";

    ComboBox empresaCbx;

    IndexedContainer facturasContainer = new IndexedContainer();
    Grid facturasGrid;

    IndexedContainer anticiposPagoContainer = new IndexedContainer();
    Grid anticiposPagoGrid;

    Button cuentaCorrienteBtn;
    Button salirBtn;
    Button autorizarBtn;

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

    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");

    public AutorizarPagoFacturaForm() {
        this.mainUI = UI.getCurrent();
        setResponsive(true);
        setWidth("95%");
        setHeightUndefined();

        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setResponsive(true);

        HorizontalLayout layoutTitle = new HorizontalLayout();
        layoutTitle.setSpacing(true);
        layoutTitle.setMargin(new MarginInfo(false, true, false, true));
        layoutTitle.setWidth("100%");

        empresaCbx = new ComboBox("EMPRESA :");
        empresaCbx.setStyleName(ValoTheme.COMBOBOX_HUGE);
        empresaCbx.setWidth("90%");
        empresaCbx.setInvalidAllowed(false);
        empresaCbx.setNewItemsAllowed(false);
        empresaCbx.setTextInputAllowed(false);
        empresaCbx.setNullSelectionAllowed(false);
        llenarComboEmpresa();

        Label titleLbl = new Label("Autorizar " + AutorizacionesPagoView.PAGO_DOCUMENTO);
        if (mainUI.getPage().getBrowserWindowWidth() >= 736) {
            titleLbl.addStyleName(ValoTheme.LABEL_H2);
        }
        else {
            titleLbl.addStyleName(ValoTheme.LABEL_H4);
        }
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h2_custom");

        layoutTitle.addComponent(empresaCbx);
        layoutTitle.setComponentAlignment(empresaCbx, Alignment.MIDDLE_LEFT);

        layoutTitle.addComponent(titleLbl);
        layoutTitle.setComponentAlignment(titleLbl, Alignment.BOTTOM_RIGHT);

        mainLayout.addComponent(layoutTitle);

        setContent(mainLayout);
        crearGridFactura();
        llenarGridFactura();
        crearGridAnticipos();

    }

    public void llenarComboEmpresa() {

        queryString = " SELECT * from contabilidad_empresa";
        queryString += " Where IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();

        try {

            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords1 = stQuery1.executeQuery(queryString);

            while (rsRecords1.next()) { //  encontrado

                empresaCbx.addItem(rsRecords1.getString("IdEmpresa"));
                empresaCbx.setItemCaption(rsRecords1.getString("IdEmpresa"), rsRecords1.getString("Empresa"));
            }
            rsRecords1.first();

            empresaCbx.select(rsRecords1.getString("IdEmpresa"));

        } catch (Exception ex1) {
            System.out.println("Error al listar empresas: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void crearGridFactura() {

        HorizontalLayout documentosLayout = new HorizontalLayout();
        documentosLayout.setWidth("100%");
        documentosLayout.addStyleName("rcorners3");
        documentosLayout.setMargin(new MarginInfo(false, true, false, true));
        documentosLayout.setResponsive(true);
//        documentosLayout.setSizeUndefined();
//        documentosLayout.addStyleName(ValoTheme.LAYOUT_HORIZONTAL_WRAPPING);

        facturasContainer.addContainerProperty(TIPO_DOCUMENTO_PROPERTY, String.class, null);
        facturasContainer.addContainerProperty(ID_PROVEEDOR_PROPERTY, String.class, null);
        facturasContainer.addContainerProperty(PROVEEDOR_PROPERTY, String.class, null);
        facturasContainer.addContainerProperty(FECHA_PROPERTY, String.class, null);
        facturasContainer.addContainerProperty(NUMERO_FACTURA_PROPERTY, String.class, null);
        facturasContainer.addContainerProperty(DESCRIPCION_PROPERTY, String.class, null);
        facturasContainer.addContainerProperty(MONEDA_PROPERTY, String.class, null);
        facturasContainer.addContainerProperty(MONTO_PROPERTY, String.class, null);
        facturasContainer.addContainerProperty(SALDO_PROPERTY, String.class, null);
        facturasContainer.addContainerProperty(MONTO_AUTORIZADO_PROPERTY, String.class, null);
        facturasContainer.addContainerProperty(MONTO_ANTICIPO_PROPERTY, String.class, null);
        facturasContainer.addContainerProperty(CODIGO_PARTIDA_PROPERTY, String.class, null);
        facturasContainer.addContainerProperty(CODIGO_CC_PROPERTY, String.class, null);

        facturasGrid = new Grid("Listado de documentos por pagar", facturasContainer);

        facturasGrid.setWidth("100%");
        facturasGrid.setImmediate(true);
        facturasGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        facturasGrid.setDescription("Seleccione un registro.");
        facturasGrid.setHeightMode(HeightMode.ROW);
        facturasGrid.setHeightByRows(7);
        facturasGrid.setResponsive(true);
        facturasGrid.setEditorBuffered(false);
        facturasGrid.setResponsive(true);

        facturasGrid.getColumn(CODIGO_PARTIDA_PROPERTY).setHidable(true).setHidden(true);
        facturasGrid.getColumn(CODIGO_CC_PROPERTY).setHidable(true).setHidden(true);
        facturasGrid.getColumn(DESCRIPCION_PROPERTY).setHidable(true).setHidden(true);
        facturasGrid.getColumn(FECHA_PROPERTY).setHidable(true);

        facturasGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {

            if (MONTO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (SALDO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }

        });

        facturasGrid.addSelectionListener(new SelectionListener() {
            @Override
            public void select(SelectionEvent event) {

                if (facturasGrid.getSelectedRow() != null) {
                    saldoFacturaTxt.setReadOnly(false);
                    String saldo = String.valueOf(facturasContainer.getContainerProperty(facturasGrid.getSelectedRow(), SALDO_PROPERTY).getValue());
                    saldoFacturaSeleccionada = Double.valueOf(saldo.replaceAll(",", "").replaceAll("Q.", "").replaceAll("\\$.", ""));
                    saldoFacturaTxt.setValue(saldoFacturaSeleccionada);
                    montoPendienteChequeTxt.setValue(saldoFacturaSeleccionada);
                    System.out.println("montoPendienteCheque=" + montoPendienteChequeTxt.getValue());
                    saldoFacturaTxt.setReadOnly(true);
                    codigoPartidaFactura = String.valueOf(facturasContainer.getContainerProperty(facturasGrid.getSelectedRow(), CODIGO_PARTIDA_PROPERTY).getValue());
                    llenarTablaAnticipos();
                    if(anticiposPagoContainer.size() == 0){
                        totalUtilizarAnticiposTxt.setReadOnly(true);
                    }else{
                        totalUtilizarAnticiposTxt.setReadOnly(false);
                    }
                }
            }
        });

        HeaderRow filterRow = facturasGrid.appendHeaderRow();

        HeaderCell cell = filterRow.getCell(PROVEEDOR_PROPERTY);

        TextField filterField = new TextField();
        filterField.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField.setInputPrompt("Filtrar");
        filterField.setColumns(10);

        filterField.addTextChangeListener(change -> {
            facturasContainer.removeContainerFilters(PROVEEDOR_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                facturasContainer.addContainerFilter(
                        new SimpleStringFilter(PROVEEDOR_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell.setComponent(filterField);

        HeaderCell cell0 = filterRow.getCell(NUMERO_FACTURA_PROPERTY);

        TextField filterField0 = new TextField();
        filterField0.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField0.setInputPrompt("Filtrar");
        filterField0.setColumns(8);

        filterField0.addTextChangeListener(change -> {
            facturasContainer.removeContainerFilters(NUMERO_FACTURA_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                facturasContainer.addContainerFilter(
                        new SimpleStringFilter(NUMERO_FACTURA_PROPERTY,
                                change.getText(), true, false));
            }
        });

        cell0.setComponent(filterField0);

        HeaderCell cell1 = filterRow.getCell(MONEDA_PROPERTY);

        TextField filterField1 = new TextField();
        filterField1.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField1.setInputPrompt("Filtrar");
        filterField1.setColumns(6);

        filterField1.addTextChangeListener(change -> {
            facturasContainer.removeContainerFilters(MONEDA_PROPERTY);
            if (!change.getText().isEmpty()) {
                facturasContainer.addContainerFilter(new SimpleStringFilter(MONEDA_PROPERTY, change.getText(), true, false));
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

        documentosLayout.addComponent(facturasGrid);
        documentosLayout.setComponentAlignment(facturasGrid, Alignment.TOP_CENTER);

        cuentaCorrienteBtn = new Button("Cuenta corriente");
        cuentaCorrienteBtn.setWidth("10em");
        cuentaCorrienteBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if(facturasGrid.getSelectedRow() == null) {
                    Notification.show("Por favor elija un documento y vuelva a intentar.", Notification.Type.WARNING_MESSAGE);
                    return;
                }
                CuentaCorrienteDocumentoForm ccForm =
                        new CuentaCorrienteDocumentoForm(String.valueOf(facturasContainer.getContainerProperty(facturasGrid.getSelectedRow(), CODIGO_CC_PROPERTY).getValue()));
                mainUI.addWindow(ccForm);
                ccForm.center();
            }
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setMargin(new MarginInfo(false, true, false, true));
        buttonsLayout.setSpacing(true);
        buttonsLayout.setWidth("100%");

        buttonsLayout.addComponents(cuentaCorrienteBtn, saldoFacturaTxt);
        buttonsLayout.setComponentAlignment(cuentaCorrienteBtn, Alignment.TOP_LEFT);
        buttonsLayout.setComponentAlignment(saldoFacturaTxt, Alignment.TOP_RIGHT);

        mainLayout.addComponent(documentosLayout);
        mainLayout.setComponentAlignment(documentosLayout, Alignment.TOP_CENTER);

        mainLayout.addComponent(buttonsLayout);
        mainLayout.setComponentAlignment(buttonsLayout, Alignment.TOP_RIGHT);
    }

    public void llenarGridFactura() {
        anticiposPagoContainer.removeAllItems();
        facturasContainer.removeAllItems();
        facturasContainer.removeAllContainerFilters();

        totalMontoQuetzales = 0.00;
        totalSaldoQueztales = 0.00;
        totalMontoDolares = 0.00;
        totalSaldoDolares = 0.00;

        queryString = " SELECT * ";
        queryString += " FROM contabilidad_partida";
        queryString += " WHERE IdEmpresa = " + empresaCbx.getValue();
        //queryString += " AND   Extract(YEAR From Fecha) >= 2019";
        queryString += " AND   Upper(TipoDocumento) IN ('FACTURA','RECIBO','RECIBO CONTABLE','RECIBO CORRIENTE','FORMULARIO IVA',";
        queryString += " 'FORMULARIO ISR', 'FORMULARIO ISR RETENIDO', 'FORMULARIO ISO', 'FORMULARIO RECTIFICACION', 'FORMULARIO ISR OPCIONAL MENSUAL')";
        queryString += " AND   IdNomenclatura IN (" + ((SopdiUI) mainUI).cuentasContablesDefault.getProveedores() + "," + ((SopdiUI) mainUI).cuentasContablesDefault.getInstituciones() + ")";
        queryString += " AND   MontoAutorizadoPagar = 0 ";
        queryString += " AND   MontoAplicarAnticipo = 0 ";
        queryString += " AND   Estatus <> 'ANULADO'";
        if(!((SopdiUI) mainUI).sessionInformation.getStrUserProfileName().equals("ADMINISTRADOR")) {
            queryString += " AND IdProveedor In (SELECT IdProveedor FROM proveedor WHERE ESAUTORIZADOPAGAR = 1)";
        }
        queryString += " ORDER by contabilidad_partida.CodigoPartida";

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
                    queryString += " WHERE IdEmpresa = " + empresaCbx.getValue();
                    queryString += " AND CodigoCC = '" + rsRecords.getString("CodigoCC") + "'";
                    queryString += " AND contabilidad_partida.IdNomenclatura = " + rsRecords.getString("IdNomenclatura");
                    queryString += " AND contabilidad_partida.Estatus <> 'ANULADO'";

                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, queryString);

                    rsRecords1 = stQuery1.executeQuery(queryString);

                    if (rsRecords1.next()) {

                        if(rsRecords1.getDouble("TOTALSALDO") > 0.00) {

                            Object itemId = facturasContainer.addItem();

                            facturasContainer.getContainerProperty(itemId, TIPO_DOCUMENTO_PROPERTY).setValue(rsRecords.getString("TipoDocumento"));
                            facturasContainer.getContainerProperty(itemId, ID_PROVEEDOR_PROPERTY).setValue(rsRecords.getString("IdProveedor"));
                            facturasContainer.getContainerProperty(itemId, PROVEEDOR_PROPERTY).setValue(rsRecords.getString("NombreProveedor"));
                            facturasContainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha")));
                            facturasContainer.getContainerProperty(itemId, NUMERO_FACTURA_PROPERTY).setValue(rsRecords.getString("SerieDocumento") + " " + rsRecords.getString("NumeroDocumento"));
                            facturasContainer.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(rsRecords.getString("Descripcion"));
                            facturasContainer.getContainerProperty(itemId, MONEDA_PROPERTY).setValue(rsRecords.getString("MonedaDocumento"));

                            if (rsRecords.getString("MonedaDocumento").equals("QUETZALES")) {
                                monedaSimbolo = "Q.";
                                totalMontoQuetzales = totalMontoQuetzales + rsRecords.getDouble("MontoDocumento");
                                totalSaldoQueztales = totalSaldoQueztales + rsRecords1.getDouble("TOTALSALDO");
                            } else {
                                monedaSimbolo = "$.";
                                totalMontoDolares = totalMontoDolares + rsRecords.getDouble("MontoDocumento");
                                totalSaldoDolares = totalSaldoDolares + rsRecords1.getDouble("TOTALSALDO");
                            }
                            facturasContainer.getContainerProperty(itemId, MONTO_PROPERTY).setValue(monedaSimbolo + numberFormat.format(rsRecords.getDouble("MontoDocumento")));
                            facturasContainer.getContainerProperty(itemId, SALDO_PROPERTY).setValue(monedaSimbolo + numberFormat.format(rsRecords1.getDouble("TOTALSALDO")));
                            facturasContainer.getContainerProperty(itemId, MONTO_AUTORIZADO_PROPERTY).setValue(monedaSimbolo + numberFormat.format(rsRecords.getDouble("MontoAutorizadoPagar")));
                            facturasContainer.getContainerProperty(itemId, MONTO_ANTICIPO_PROPERTY).setValue(monedaSimbolo + numberFormat.format(rsRecords.getDouble("MontoAplicarAnticipo")));
                            facturasContainer.getContainerProperty(itemId, CODIGO_CC_PROPERTY).setValue(rsRecords.getString("CodigoCC"));
                            facturasContainer.getContainerProperty(itemId, CODIGO_PARTIDA_PROPERTY).setValue(rsRecords.getString("CodigoPartida"));
                        } // saldo = 0.00
                    }//no tiene cuenta corriente

                } while (rsRecords.next());

            }

        } catch (Exception ex) {
            System.out.println("Error al listar tabla Facturas en AutorizarPagoFacturaForm: " + ex.getMessage());
            ex.printStackTrace();
        }

        ((AutorizacionesPagoView)(mainUI.getNavigator().getCurrentView())).pagoDocumentoBtn.setEnabled(true);
    }

    public void crearGridAnticipos() {

        HorizontalLayout reportLayout = new HorizontalLayout();
        reportLayout.setWidth("100%");
        reportLayout.addStyleName("rcorners3");
        reportLayout.setResponsive(true);
        reportLayout.setMargin(new MarginInfo(false, true, false, true));

        HorizontalLayout layoutFiltros = new HorizontalLayout();
        layoutFiltros.setMargin(new MarginInfo(false, true, false, true));
        //layoutFiltros.setSpacing(true);
        layoutFiltros.setWidth("100%");

        anticiposPagoContainer.addContainerProperty(CODIGO_PARTIDA2_PROPERTY, String.class, null);
        anticiposPagoContainer.addContainerProperty(CODIGO_CC2_PROPERTY, String.class, null);
        anticiposPagoContainer.addContainerProperty(FECHA2__PROPERTY, String.class, null);
        anticiposPagoContainer.addContainerProperty(DOCUMENTO_PROPERTY, String.class, null);
        anticiposPagoContainer.addContainerProperty(MONTO2_PROPERTY, String.class, null);
        anticiposPagoContainer.addContainerProperty(SALDO2_PROPERTY, String.class, null);
        anticiposPagoContainer.addContainerProperty(UTILIZAR_PROPERTY, String.class, null);

        anticiposPagoGrid = new Grid("Anticipos al proveedor,  pendientes de liquidar.", anticiposPagoContainer);

        anticiposPagoGrid.setImmediate(true);
        anticiposPagoGrid.setSelectionMode(Grid.SelectionMode.NONE);
        anticiposPagoGrid.setDescription("Seleccione un registro para ingresar o editar. Doble click para seleccionar el monto.");
        anticiposPagoGrid.setHeightMode(HeightMode.ROW);
        anticiposPagoGrid.setHeightByRows(5);
        anticiposPagoGrid.setWidth("100%");
        anticiposPagoGrid.setResponsive(true);
        anticiposPagoGrid.setEditorBuffered(false);
        anticiposPagoGrid.setSizeFull();
        anticiposPagoGrid.setEditorEnabled(true);
        anticiposPagoGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if (MONTO2_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (SALDO2_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }
        });
        anticiposPagoGrid.getColumn(UTILIZAR_PROPERTY).setEditorField(getAmmountField(UTILIZAR_PROPERTY));
        anticiposPagoGrid.addItemClickListener((event) -> {
            if (event != null) {
                montoPendienteChequeTxt.setValue(0.00);
                totalUtilizarAnticiposTxt.setReadOnly(false);
                totalUtilizarAnticiposTxt.setValue(0.00);
                totalUtilizarAnticiposTxt.setReadOnly(false);
                anticiposPagoGrid.editItem(event.getItemId());
            }
        });

        anticiposPagoGrid.getColumn(CODIGO_PARTIDA2_PROPERTY).setHidable(true).setHidden(true);

        totalUtilizarAnticiposTxt = new NumberField("Anticipos utilizados :");
        totalUtilizarAnticiposTxt.setDecimalAllowed(true);
        totalUtilizarAnticiposTxt.setDecimalPrecision(2);
        totalUtilizarAnticiposTxt.setMinimumFractionDigits(2);
        totalUtilizarAnticiposTxt.setDecimalSeparator('.');
        totalUtilizarAnticiposTxt.setDecimalSeparatorAlwaysShown(true);
        totalUtilizarAnticiposTxt.setValue(0d);
        totalUtilizarAnticiposTxt.setGroupingUsed(true);
        totalUtilizarAnticiposTxt.setGroupingSeparator(',');
        totalUtilizarAnticiposTxt.setGroupingSize(3);
        totalUtilizarAnticiposTxt.setImmediate(true);
        totalUtilizarAnticiposTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        totalUtilizarAnticiposTxt.setWidth("8em");
        totalUtilizarAnticiposTxt.setReadOnly(true);

        montoPendienteChequeTxt = new NumberField("Monto para cheque :");
        montoPendienteChequeTxt.setDecimalAllowed(true);
        montoPendienteChequeTxt.setDecimalPrecision(2);
        montoPendienteChequeTxt.setMinimumFractionDigits(2);
        montoPendienteChequeTxt.setDecimalSeparator('.');
        montoPendienteChequeTxt.setDecimalSeparatorAlwaysShown(true);
        montoPendienteChequeTxt.setValue(0d);
        montoPendienteChequeTxt.setGroupingUsed(true);
        montoPendienteChequeTxt.setGroupingSeparator(',');
        montoPendienteChequeTxt.setGroupingSize(3);
        montoPendienteChequeTxt.setImmediate(true);
        montoPendienteChequeTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        montoPendienteChequeTxt.setWidth("8em");

        autorizarBtn = new Button("Autorizar");
        autorizarBtn.setIcon(FontAwesome.SAVE);
//        autorizarBtn.setWidth("60%");
//        autorizarBtn.setHeight("80%");
        autorizarBtn.setStyleName(ValoTheme.BUTTON_PRIMARY);
        autorizarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                actualizarFactura();
            }
        });

        salirBtn = new Button("Salir");
//        salirBtn.setWidth("7em");
        salirBtn.setStyleName(ValoTheme.BUTTON_DANGER);
        salirBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                ((AutorizacionesPagoView)(mainUI.getNavigator().getCurrentView())).pagoDocumentoBtn.setEnabled(true);
                close();
            }
        });

        reportLayout.addComponent(anticiposPagoGrid);
        reportLayout.setComponentAlignment(anticiposPagoGrid, Alignment.TOP_CENTER);

        HorizontalLayout montoLayout = new HorizontalLayout();
        montoLayout.setMargin(true);
        montoLayout.setSpacing(true);
        montoLayout.setWidth("100%");
        montoLayout.setResponsive(true);
        montoLayout.setImmediate(true);

        montoLayout.addComponents(salirBtn, montoPendienteChequeTxt, totalUtilizarAnticiposTxt, autorizarBtn);
        montoLayout.setComponentAlignment(salirBtn, Alignment.BOTTOM_LEFT);
        montoLayout.setComponentAlignment(montoPendienteChequeTxt, Alignment.TOP_RIGHT);
        montoLayout.setComponentAlignment(totalUtilizarAnticiposTxt, Alignment.TOP_RIGHT);
        montoLayout.setComponentAlignment(autorizarBtn, Alignment.BOTTOM_RIGHT);

        layoutFiltros.addComponent(montoLayout);
        layoutFiltros.setComponentAlignment(montoLayout, Alignment.TOP_RIGHT);

        mainLayout.addComponent(reportLayout);
        mainLayout.setComponentAlignment(reportLayout, Alignment.TOP_CENTER);

        mainLayout.addComponent(layoutFiltros);
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
        valueTxt.setDescription("Doble click para selecionar todo el monto...");
        valueTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        valueTxt.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (event.getProperty().getValue() != null) {
                    if (!String.valueOf(event.getProperty().getValue()).trim().isEmpty()) {
                        if (anticiposPagoContainer.size() > 0) {

                            for (Object itemId : anticiposPagoContainer.getItemIds()) {
                                Item item = anticiposPagoContainer.getItem(itemId);
                                Object propertyValue = item.getItemProperty(propertyId).getValue(); ///Utilizar"UTILIZAR"
                                Object propertyValue2 = item.getItemProperty(SALDO2_PROPERTY).getValue();

                                if (Double.valueOf(String.valueOf(propertyValue).replaceAll(",", "")) > Double.valueOf(String.valueOf(propertyValue2).replaceAll(",", ""))) {
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

        double total = 0.00;
        double montoCheque = 0.00;
        double utilizarAnticipos = 0.00;

        for (Object itemId : anticiposPagoContainer.getItemIds()) {
            Item item = anticiposPagoContainer.getItem(itemId);
            Object propertyValue = item.getItemProperty(propertyId).getValue();

            total += Double.valueOf(String.valueOf(propertyValue));
        }
//        utilizarAnticipos = Double.parseDouble(String.valueOf(anticiposPagoFooter.getCell(UTILIZAR_PROPERTY).getText().replaceAll(",", "")));
        totalUtilizarAnticiposTxt.setReadOnly(false);
        totalUtilizarAnticiposTxt.setValue(total);
        totalUtilizarAnticiposTxt.setReadOnly(true);
        montoCheque = (saldoFacturaSeleccionada - totalUtilizarAnticiposTxt.getDoubleValueDoNotThrow());
        if(montoCheque < 0) {
            montoCheque = 0.00;
        }
        montoPendienteChequeTxt.setValue(montoCheque);

        System.out.println("montocheque " + montoCheque);
        System.out.println("saldofactura" + saldoFacturaSeleccionada);
        System.out.println("utilizarAnticipos " + total);
        System.out.println("utilizarAnticipos format " + numberFormat.format(total));
    }

    public void llenarTablaAnticipos() {

        anticiposPagoContainer.removeAllItems();
        anticiposPagoContainer.removeAllContainerFilters();

        double totalMontoAnticipo = 0.00;
        double totalSaldoAnticipo = 0.00;

        String proveedorSeleccionado = String.valueOf(facturasContainer.getContainerProperty(facturasGrid.getSelectedRow(), ID_PROVEEDOR_PROPERTY).getValue());
        String tipoMonedaSeleccionado = String.valueOf(facturasContainer.getContainerProperty(facturasGrid.getSelectedRow(), MONEDA_PROPERTY).getValue());

        queryString = " SELECT contabilidad_partida.CodigoPartida, contabilidad_partida.CodigoCC, SUM(DEBE) MontoAnticipo, ";
        queryString += " SUM(DEBE - HABER) TOTALSALDO, SUM(DebeQuetzales - HaberQuetzales) TOTALSALDOQ, contabilidad_partida.Fecha";
        queryString += " FROM contabilidad_partida";
        queryString += " INNER JOIN contabilidad_nomenclatura ON contabilidad_nomenclatura.IdNomenclatura = contabilidad_partida.IdNomenclatura";
        queryString += " WHERE contabilidad_partida.IdProveedor = " + proveedorSeleccionado;
        queryString += " AND contabilidad_partida.IdEmpresa = " + empresaCbx.getValue();
        queryString += " AND contabilidad_partida.MonedaDocumento = '" + tipoMonedaSeleccionado + "'";
        queryString += " AND contabilidad_partida.IdNomenclatura = " + ((SopdiUI) mainUI).cuentasContablesDefault.getAnticiposProveedor();
//        queryString += " AND contabilidad_partida.TipoDocumento In ('CHEQUE', 'TRANSFERENCIA', 'NOTA DE CREDITO', 'DEPOSITO', 'PAGO DOCUMENTO','FORMULARIO ISR OPCIONAL MENSUAL' )";
//        queryString += " AND EXTRACT(YEAR FROM Fecha) >= '2020' ";
        queryString += " AND contabilidad_partida.Estatus <> 'ANULADO'";
//        queryString += " AND contabilidad_partida.CodigoCC Not In (Select CodigoCCRelacionado from autorizacion_pago ) ";
        queryString += " GROUP BY contabilidad_partida.CodigoCC";
        queryString += " HAVING TOTALSALDO > 0";

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "-->query para mostrar anticipos pendiente de liquidar del proveedor : " + queryString);

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery1 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {

                do {

                    queryString = " SELECT IFNULL(SUM(Monto),0) TOTALOCUPADO";
                    queryString += " FROM autorizacion_pago";
                    queryString += " WHERE IdProveedor = " + proveedorSeleccionado;
                    queryString += " AND IdEmpresa = " + empresaCbx.getValue();
                    queryString += " AND CodigoCCRelacionado = '" + rsRecords.getString("CodigoCC") + "'";

                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, "-->query obtener saldo real anticipo : " + queryString);

                    rsRecords1 = stQuery1.executeQuery(queryString);

                    rsRecords1.next();

                    Object itemId = anticiposPagoContainer.addItem();

                    anticiposPagoContainer.getContainerProperty(itemId, CODIGO_PARTIDA2_PROPERTY).setValue(rsRecords.getString("CodigoPartida"));
                    anticiposPagoContainer.getContainerProperty(itemId, CODIGO_CC2_PROPERTY).setValue(rsRecords.getString("CodigoCC"));
                    anticiposPagoContainer.getContainerProperty(itemId, FECHA2__PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha")));
                    anticiposPagoContainer.getContainerProperty(itemId, DOCUMENTO_PROPERTY).setValue("ANTICIPO A PROVEEDORES");
                    anticiposPagoContainer.getContainerProperty(itemId, MONTO2_PROPERTY).setValue(numberFormat.format((rsRecords.getDouble("MontoAnticipo"))));
                    anticiposPagoContainer.getContainerProperty(itemId, SALDO2_PROPERTY).setValue(numberFormat.format(rsRecords.getDouble("TOTALSALDO") - rsRecords1.getDouble("TOTALOCUPADO")));
                    anticiposPagoContainer.getContainerProperty(itemId, UTILIZAR_PROPERTY).setValue(numberFormat.format((0.00)));

                    totalMontoAnticipo += rsRecords.getDouble("MontoAnticipo");
                    totalSaldoAnticipo += rsRecords.getDouble("TOTALSALDO");

                } while (rsRecords.next());

            }
        } catch (Exception ex) {
            System.out.println("Error al listar grid anticipos factura : " + ex);
            ex.printStackTrace();
        }
    }

    private void actualizarFactura() {

        try {

            double montoAuotizadoPagar = 0.00;
            double montoAplicarAnticipo = 0.00;

            montoAuotizadoPagar = montoPendienteChequeTxt.getDoubleValueDoNotThrow()
                    + Double.valueOf(String.valueOf(
                    facturasContainer.getContainerProperty(
                            facturasGrid.getSelectedRow(), MONTO_AUTORIZADO_PROPERTY).getValue()).
                    replaceAll("Q.", "").
                    replaceAll("\\$.", "").
                    replaceAll(",", ""));

            montoAplicarAnticipo = totalUtilizarAnticiposTxt.getDoubleValueDoNotThrow() + Double.valueOf(String.valueOf(facturasContainer.getContainerProperty(facturasGrid.getSelectedRow(), MONTO_ANTICIPO_PROPERTY).getValue()).replaceAll("Q.", "").replaceAll("\\$.", "").replaceAll(",", ""));

            queryString = "Update contabilidad_partida Set ";
            queryString += " MontoAutorizadoPagar = " + montoAuotizadoPagar;
            queryString += ", MontoAplicarAnticipo = " + montoAplicarAnticipo;
            //           queryString += ", Saldo = " + nuevoSaldo;
            queryString += " Where CodigoPartida = '" + String.valueOf(facturasContainer.getContainerProperty(facturasGrid.getSelectedRow(), CODIGO_PARTIDA_PROPERTY).getValue()) + "'";
            queryString += " and IdEmpresa = " + String.valueOf(empresaCbx.getValue());

            Logger.getLogger(this.getClass().getName()).log(Level.INFO, Utileria.getFechaDDMMYYYY_HHMM() + " actualizar FACTURA AUTORIZADA PARA PAGAR..." + queryString);

            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            queryString = "  Insert Into autorizacion_pago (TipoAutorizacion, IdEmpresa, IdProveedor, ";
            queryString += " Fecha, Moneda, Monto, CodigoCC, CodigoCCRelacionado, CuentaContableLiquidar, ";
            queryString += " Objetivo, CreadoUsuario, CreadoFechaYHora)";
            queryString += " Values ";
            queryString += "(";
            queryString += "'" + AutorizacionesPagoView.PAGO_DOCUMENTO + "'";
            queryString += "," + String.valueOf(empresaCbx.getValue());
            queryString += "," + String.valueOf(facturasContainer.getContainerProperty(facturasGrid.getSelectedRow(), ID_PROVEEDOR_PROPERTY).getValue());
            queryString += ",current_date";
            queryString += ",'" + String.valueOf(facturasContainer.getContainerProperty(facturasGrid.getSelectedRow(), MONEDA_PROPERTY).getValue()) + "'";
            queryString += ","  + montoAuotizadoPagar ;
            queryString += ",'" + String.valueOf(facturasContainer.getContainerProperty(facturasGrid.getSelectedRow(), CODIGO_CC_PROPERTY).getValue()) + "'";
            queryString += ",''";
            queryString += ",''"; // cuentacontableliquidar
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
                String emailsTo[] = {"alerta@simpletecno.com"};
                MyEmailMessanger eMail = new MyEmailMessanger();

                eMail.postMail(emailsTo, "Error en SOPDI", "Error en base de datos :  " + this.getClass().getName() + " -->" + ex.getMessage());
            } catch (MessagingException ex2) {
                Logger.getLogger(SopdiUI.class.getName()).log(Level.SEVERE, null, ex2);
            }
            ex.printStackTrace();

        }
    }

    private void insertAnticiposAutorizadosFactura() {

        String proveedorSeleccionado = String.valueOf(facturasContainer.getContainerProperty(facturasGrid.getSelectedRow(), ID_PROVEEDOR_PROPERTY).getValue());
        String codigoCC = String.valueOf(facturasContainer.getContainerProperty(facturasGrid.getSelectedRow(),  CODIGO_CC_PROPERTY).getValue());
        String moneda = String.valueOf(facturasContainer.getContainerProperty(facturasGrid.getSelectedRow(),  MONEDA_PROPERTY).getValue());

        try {

            for (Object itemId : anticiposPagoContainer.getItemIds()) {

                Item item = anticiposPagoContainer.getItem(itemId);

                Object codigoCCAnticipo = item.getItemProperty(CODIGO_CC2_PROPERTY).getValue();
                Object montoUtilziar = item.getItemProperty(UTILIZAR_PROPERTY).getValue();

                double montoUtilizarVariable = Double.parseDouble(String.valueOf(montoUtilziar).replaceAll(",", ""));

                if (montoUtilizarVariable > 0) {

                    queryString = " Delete From autorizacion_pago ";
                    queryString += " Where CodigoCCRelacionado = '" + codigoCCAnticipo + "'";
                    queryString += " And   CodigoCC = '" + codigoCC + "'";

                    stQuery.executeUpdate(queryString);

                    queryString = "  Insert Into autorizacion_pago (TipoAutorizacion, IdEmpresa, IdProveedor, ";
                    queryString += " Fecha, Moneda, Monto, CodigoCC, CodigoCCRelacionado, CuentaContableLiquidar, ";
                    queryString += " Objetivo, CreadoUsuario, CreadoFechaYHora)";
                    queryString += " Values ";
                    queryString += "(";
                    queryString += "'" + AutorizacionesPagoView.PAGO_DOCUMENTO + "'";
                    queryString += "," + String.valueOf(empresaCbx.getValue());
                    queryString += "," + proveedorSeleccionado;
                    queryString += ",current_date";
                    queryString += ",'" + moneda + "'";
                    queryString += "," + montoUtilizarVariable;
                    queryString += ",'" + codigoCC + "'";
                    queryString += ",'" + codigoCCAnticipo + "'";
                    queryString += ",''"; // cuentacontableliquidar
                    queryString += ",'" + AutorizacionesPagoView.PAGO_DOCUMENTO + "'";
                    queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                    queryString += ",current_timestamp";
                    queryString += ")";

                    stQuery.executeUpdate(queryString);

                } // if monto utilizado
            } // endfor

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
                String emailsTo[] = {"alerta@simpletecno.com"};
                MyEmailMessanger eMail = new MyEmailMessanger();

                eMail.postMail(emailsTo, "Error en SOPDI", "Error en base de datos :  " + this.getClass().getName() + " -->" + ex.getMessage());
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

        saldoFacturaTxt.setReadOnly(false);
        saldoFacturaTxt.setValue(0.00);
        saldoFacturaTxt.setReadOnly(true);

        Notification notif = new Notification("AUTORIZACION EXITOSA",
                Notification.Type.HUMANIZED_MESSAGE);
        notif.setDelayMsec(1500);
        notif.setPosition(Position.MIDDLE_CENTER);
        notif.setIcon(FontAwesome.CHECK);
        notif.show(Page.getCurrent());

    }
}