package com.simpletecno.sopdi.tesoreria;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.SelectionEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.ui.NumberField;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author user
 */
public class PagoDevolucionEngancheForm extends Window {

    VerticalLayout mainLayout;

    public IndexedContainer container = new IndexedContainer();
    Grid enganchesGrid;
    static final String TIPO = "Tipo";
    static final String NO_PROPERTY = "No.";
    static final String ID_PROVEEDOR_PROPERTY = "Cod Cliente";
    static final String PROVEEDOR_PROPERTY = "Cliente";
    static final String FECHA_PROPERTY = "Fecha";
    static final String CODIGO_CC = "Codigo CC";
    static final String MONEDA_PROPERTY = "Moneda";
    static final String MONTO_PROPERTY = "Monto";
    static final String MONTO_Q_PROPERTY = "Monto Q.";
    static final String IDNOMENCLATURA = "IdNomenclatura";

    DateField fechaDt;

    TextField nombreChequeTxt;
    TextField descripcionTxt;
    TextField numeroTxt;

    NumberField montoTxt;
    NumberField tasaCambioTxt;

    TextField codigo1Txt;
    TextField codigo2Txt;
    TextField codigo3Txt;
    TextField codigo4Txt;
    TextField codigo5Txt;
    TextField codigo6Txt;
    TextField codigo7Txt;

    NumberField debe1Txt;
    NumberField debe2Txt;
    NumberField debe3Txt;
    NumberField debe4Txt;
    NumberField debe5Txt;
    NumberField debe6Txt;
    NumberField debe7Txt;

    NumberField debe1QTxt;
    NumberField debe2QTxt;
    NumberField debe3QTxt;
    NumberField debe4QTxt;
    NumberField debe5QTxt;
    NumberField debe6QTxt;
    NumberField debe7QTxt;

    NumberField haber1Txt;
    NumberField haber2Txt;
    NumberField haber3Txt;
    NumberField haber4Txt;
    NumberField haber5Txt;
    NumberField haber6Txt;
    NumberField haber7Txt;

    NumberField haber1QTxt;
    NumberField haber2QTxt;
    NumberField haber3QTxt;
    NumberField haber4QTxt;
    NumberField haber5QTxt;
    NumberField haber6QTxt;
    NumberField haber7QTxt;

    ComboBox proveedorCbx;
    ComboBox monedaCbx;
    ComboBox medioCbx;
    ComboBox cuentaContable1Cbx;
    ComboBox cuentaContable2Cbx;
    ComboBox cuentaContable3Cbx;
    ComboBox cuentaContable4Cbx;
    ComboBox cuentaContable5Cbx;
    ComboBox cuentaContable6Cbx;
    ComboBox cuentaContable7Cbx;

    HorizontalLayout chequeLayout = new HorizontalLayout();
    HorizontalLayout chequeLayout2 = new HorizontalLayout();
    VerticalLayout partidaLayout = new VerticalLayout();

    Button grabarPartidaBtn;

    Label titleLbl;

    UI mainUI;
    Statement stQuery, stQuery2;
    ResultSet rsRecords;
    String queryString;

    String codigoPartidaNuevo;
    String proveedorId = "";
    String nombreProveedor = "";

    BigDecimal totalDebe;
    BigDecimal totalHaber;

    String idProveedor;
    
    Date fechaPago;

    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");
    static DecimalFormat numberFormat3 = new DecimalFormat("######0.00");

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public PagoDevolucionEngancheForm(String idProveedor, Date fechaPago) {

        this.mainUI = UI.getCurrent();
        this.idProveedor= idProveedor;
        this.fechaPago = fechaPago;

        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setResponsive(true);

        setContent(mainLayout);

        setResponsive(true);
        setWidth("90%");
        setHeight("90%");

        titleLbl = new Label("");
        titleLbl.setValue(empresaId + " " + empresaNombre + " DEVOLUCION A CLIENTE");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h2_custom");

        proveedorCbx = new ComboBox("Cliente : ");
        proveedorCbx.setWidth("35em");
        proveedorCbx.setVisible(false);
        proveedorCbx.setFilteringMode(FilteringMode.CONTAINS);
        proveedorCbx.addValueChangeListener(event -> {
            llenarTablaEnganches();
            nombreChequeTxt.setReadOnly(false);
            nombreChequeTxt.setValue(proveedorCbx.getItemCaption(proveedorCbx.getValue()));

        });

        llenarComboProveedor("ESCLIENTE = 1");

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setResponsive(true);
        titleLayout.setSpacing(true);
        titleLayout.setWidth("100%");
        titleLayout.setMargin(false);
        titleLayout.addComponents(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.MIDDLE_CENTER);
        titleLayout.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

        mainLayout.addComponent(titleLayout);
        mainLayout.setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        mainLayout.addComponent(proveedorCbx);
        mainLayout.setComponentAlignment(proveedorCbx, Alignment.TOP_CENTER);

        createTablaAnticipos();
        llenarTablaEnganches();
        crearLayoutCheque();
        crearPartidaLayout();
    }

    public void llenarComboProveedor(String codigos) {
        String queryString = " SELECT * FROM proveedor_empresa ";
        queryString += " WHERE " + codigos;
        queryString += " AND Inhabilitado = 0 ";
        queryString += " ORDER BY Nombre ";

        proveedorCbx.removeAllItems();

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) { //  encontrado
                proveedorCbx.addItem(rsRecords.getString("IDProveedor"));
                proveedorCbx.setItemCaption(rsRecords.getString("IDProveedor"), rsRecords.getString("Nombre"));
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar Proveedores " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void createTablaAnticipos() {

        HorizontalLayout reportLayout = new HorizontalLayout();
        reportLayout.setWidth("96%");
        reportLayout.addStyleName("rcorners3");
        reportLayout.setResponsive(true);
        reportLayout.setMargin(true);

        container.addContainerProperty(TIPO, String.class, null);
        container.addContainerProperty(NO_PROPERTY, String.class, null);
        container.addContainerProperty(CODIGO_CC, String.class, null);
        container.addContainerProperty(ID_PROVEEDOR_PROPERTY, String.class, null);
        container.addContainerProperty(PROVEEDOR_PROPERTY, String.class, null);
        container.addContainerProperty(FECHA_PROPERTY, String.class, null);
        container.addContainerProperty(MONEDA_PROPERTY, String.class, null);
        container.addContainerProperty(MONTO_PROPERTY, String.class, null);
        container.addContainerProperty(MONTO_Q_PROPERTY, String.class, null);
        container.addContainerProperty(IDNOMENCLATURA, String.class, null);

        enganchesGrid = new Grid("Devoluciones autorizadas", container);
        enganchesGrid.setWidth("100%");
        enganchesGrid.setImmediate(true);
        enganchesGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        enganchesGrid.setDescription("Seleccione uno o varios registros del mismo proveedor.");
        enganchesGrid.setHeightMode(HeightMode.ROW);
        enganchesGrid.setHeightByRows(5);
        enganchesGrid.setResponsive(true);
        enganchesGrid.setEditorBuffered(false);
        enganchesGrid.addSelectionListener(
                new SelectionEvent.SelectionListener() {
                    @Override
                    public void select(SelectionEvent event) {
                        if (enganchesGrid.getSelectedRows() != null) {
                            calcularPartida();
                        }
                    }
                }
        );

        enganchesGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if (MONTO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }
        });

        reportLayout.addComponent(enganchesGrid);
        reportLayout.setComponentAlignment(enganchesGrid, Alignment.MIDDLE_CENTER);

        mainLayout.addComponent(reportLayout);
        mainLayout.setComponentAlignment(reportLayout, Alignment.TOP_CENTER);
    }

    public void llenarTablaEnganches() {

        container.removeAllItems();
        enganchesGrid.getSelectedRows().clear();
        enganchesGrid.getSelectionModel().reset();

        queryString = "SELECT contabilidad_partida.CodigoPartida, contabilidad_partida.CodigoCC, ";
        queryString += " contabilidad_partida.NombreProveedor ,";
        queryString += " contabilidad_partida.IdNomenclatura, contabilidad_partida.TipoDocumento, ";
        queryString += " contabilidad_partida.SerieDocumento, contabilidad_partida.NumeroDocumento, ";
        queryString += " contabilidad_partida.MontoAutorizadoPagar, contabilidad_partida.MontoAplicarAnticipo,";
        queryString += " contabilidad_partida.Haber, contabilidad_partida.HaberQuetzales, ";
        queryString += " ((autorizacion_pago.Monto / contabilidad_partida.Haber) * contabilidad_partida.HaberQuetzales) ProporcionHaberQ,";
        queryString += " autorizacion_pago.Objetivo, autorizacion_pago.IdAutorizacion, autorizacion_pago.CuentaContableLiquidar, ";
        queryString += " autorizacion_pago.Moneda, autorizacion_pago.Fecha, autorizacion_pago.Monto, autorizacion_pago.IdProveedor ";
        queryString += " FROM contabilidad_partida ";
        queryString += " INNER JOIN autorizacion_pago ON autorizacion_pago.CodigoCC = contabilidad_partida.CodigoCC ";
        queryString += " WHERE contabilidad_partida.IdEmpresa =" + empresaId;
        queryString += " AND contabilidad_partida.IdProveedor = " + idProveedor;
        queryString += " AND contabilidad_partida.IdNomenclatura In (" + ((SopdiUI) mainUI).cuentasContablesDefault.getEnganches() + "," + ((SopdiUI) mainUI).cuentasContablesDefault.getAnticiposClientes() + ", " + ((SopdiUI) mainUI).cuentasContablesDefault.getAcreedoresCortoPlazo() + ")";
        queryString += " AND autorizacion_pago.TipoAutorizacion = 'DEVOLUCION A CLIENTE'";
        queryString += " GROUP BY CodigoPartida";

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);
            if (rsRecords.next()) { //  encontrado
                do {

                    Object itemId = container.addItem();

                    container.getContainerProperty(itemId, TIPO).setValue(rsRecords.getString("Objetivo"));
                    container.getContainerProperty(itemId, NO_PROPERTY).setValue(rsRecords.getString("IdAutorizacion"));
                    container.getContainerProperty(itemId, CODIGO_CC).setValue(rsRecords.getString("CodigoCC"));
                    container.getContainerProperty(itemId, ID_PROVEEDOR_PROPERTY).setValue(rsRecords.getString("IdProveedor"));
                    container.getContainerProperty(itemId, PROVEEDOR_PROPERTY).setValue(rsRecords.getString("NombreProveedor"));
                    container.getContainerProperty(itemId, FECHA_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha")));
                    container.getContainerProperty(itemId, MONEDA_PROPERTY).setValue(rsRecords.getString("Moneda"));
                    container.getContainerProperty(itemId, MONTO_PROPERTY).setValue(numberFormat.format((rsRecords.getDouble("Monto"))));
                    container.getContainerProperty(itemId, MONTO_Q_PROPERTY).setValue(numberFormat.format((rsRecords.getDouble("ProporcionHaberQ"))));
                    container.getContainerProperty(itemId, IDNOMENCLATURA).setValue(rsRecords.getString("CuentaContableLiquidar"));

                } while (rsRecords.next());
            }

        } catch (Exception ex) {
            System.out.println("Error al listar tabla nomenclatura contable:" + ex);
            ex.printStackTrace();
        }
    }

    private void crearLayoutCheque() {

        chequeLayout.setSpacing(true);
        chequeLayout.setMargin(false);
        chequeLayout.setSizeUndefined();

        chequeLayout2.setSpacing(true);
        chequeLayout2.setMargin(false);
        chequeLayout2.setSizeUndefined();

        numeroTxt = new TextField("# Documento : ");
        numeroTxt.setWidth("8em");

        medioCbx = new ComboBox("Medio : ");
        medioCbx.setWidth("10em");
        medioCbx.addItem("CHEQUE");
        medioCbx.addItem("NOTA DE DEBITO");
        medioCbx.select("CHEQUE");

        monedaCbx = new ComboBox("Moneda : ");
        monedaCbx.setWidth("10em");
        monedaCbx.addItem("QUETZALES");
        monedaCbx.addItem("DOLARES");
        monedaCbx.select("QUETZALES");

        tasaCambioTxt = new NumberField("T.Cambio : ");
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
        tasaCambioTxt.setWidth("5em");
        tasaCambioTxt.setValue(1.00);
        tasaCambioTxt.addValueChangeListener( event -> {
            calcularPartida();
        });

        montoTxt = new NumberField("Monto : ");
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
        montoTxt.setWidth("7em");

        fechaDt = new DateField("Fecha : ");
        fechaDt.setDateFormat("dd/MM/yyyy");
        fechaDt.setWidth("9em");
        fechaDt.setValue(fechaPago);
        fechaDt.setReadOnly(true);

        nombreChequeTxt = new TextField("Nombre cheque/transf. : ");
        nombreChequeTxt.setWidth("30em");
        nombreChequeTxt.setReadOnly(((SopdiUI) mainUI).sessionInformation.getStrUserProfileName().equals("CONTADOR"));
        nombreChequeTxt.setReadOnly(((SopdiUI) mainUI).sessionInformation.getStrUserProfileName().equals("AUXILIAR"));

        descripcionTxt = new TextField("Observación extra : ");
        descripcionTxt.setWidth("35em");
        descripcionTxt.setVisible(false);

        chequeLayout.addComponent(medioCbx);
        chequeLayout.setComponentAlignment(medioCbx, Alignment.MIDDLE_CENTER);
        chequeLayout.addComponent(numeroTxt);
        chequeLayout.setComponentAlignment(numeroTxt, Alignment.MIDDLE_CENTER);
        chequeLayout.addComponent(fechaDt);
        chequeLayout.setComponentAlignment(fechaDt, Alignment.MIDDLE_CENTER);
        chequeLayout.addComponent(montoTxt);
        chequeLayout.setComponentAlignment(montoTxt, Alignment.MIDDLE_CENTER);
        chequeLayout.addComponent(monedaCbx);
        chequeLayout.setComponentAlignment(monedaCbx, Alignment.MIDDLE_CENTER);
        chequeLayout.addComponent(tasaCambioTxt);
        chequeLayout.setComponentAlignment(tasaCambioTxt, Alignment.MIDDLE_CENTER);

        mainLayout.addComponent(chequeLayout);
        mainLayout.setComponentAlignment(chequeLayout, Alignment.MIDDLE_CENTER);

        chequeLayout2.addComponent(nombreChequeTxt);
        chequeLayout2.setComponentAlignment(nombreChequeTxt, Alignment.MIDDLE_CENTER);
        chequeLayout2.addComponent(descripcionTxt);
        chequeLayout2.setComponentAlignment(descripcionTxt, Alignment.MIDDLE_CENTER);

        mainLayout.addComponent(chequeLayout2);
        mainLayout.setComponentAlignment(chequeLayout2, Alignment.MIDDLE_CENTER);
    }

    public void crearPartidaLayout() {

        partidaLayout.addStyleName("rcorners3");
        partidaLayout.setWidth("70%");
        partidaLayout.setResponsive(true);
        partidaLayout.setSpacing(false);
        partidaLayout.setMargin(false);

        HorizontalLayout layoutHorizontal1 = new HorizontalLayout();
        layoutHorizontal1.setSpacing(true);
        layoutHorizontal1.setMargin(false);

        HorizontalLayout layoutHorizontal2 = new HorizontalLayout();
        layoutHorizontal2.setSpacing(true);
        layoutHorizontal2.setMargin(false);

        HorizontalLayout layoutHorizontal3 = new HorizontalLayout();
        layoutHorizontal3.setSpacing(true);
        layoutHorizontal3.setMargin(false);

        HorizontalLayout layoutHorizontal4 = new HorizontalLayout();
        layoutHorizontal4.setSpacing(true);
        layoutHorizontal4.setMargin(false);

        HorizontalLayout layoutHorizontal5 = new HorizontalLayout();
        layoutHorizontal5.setSpacing(true);
        layoutHorizontal5.setMargin(false);

        HorizontalLayout layoutHorizontal6 = new HorizontalLayout();
        layoutHorizontal6.setSpacing(true);
        layoutHorizontal6.setMargin(false);

        HorizontalLayout layoutHorizontal7 = new HorizontalLayout();
        layoutHorizontal7.setSpacing(true);
        layoutHorizontal7.setMargin(false);

        HorizontalLayout layoutHorizontalBotones = new HorizontalLayout();
        layoutHorizontalBotones.setSpacing(true);
//        layoutHorizontalBotones.setMargin(new MarginInfo(true,false,false,false));
        layoutHorizontalBotones.setMargin(true);
        layoutHorizontalBotones.setWidth("90%");
        layoutHorizontalBotones.setSpacing(true);

        codigo1Txt = new TextField("");
        codigo1Txt.setWidth("2em");
        codigo1Txt.setVisible(false);
        codigo1Txt.setValue("");

        codigo2Txt = new TextField("");
        codigo2Txt.setWidth("2em");
        codigo2Txt.setVisible(false);
        codigo2Txt.setValue("");

        codigo3Txt = new TextField("");
        codigo3Txt.setWidth("2em");
        codigo3Txt.setVisible(false);
        codigo3Txt.setValue("");

        codigo4Txt = new TextField("");
        codigo4Txt.setWidth("2em");
        codigo4Txt.setVisible(false);
        codigo4Txt.setValue("");

        codigo5Txt = new TextField("");
        codigo5Txt.setWidth("2em");
        codigo5Txt.setVisible(false);
        codigo5Txt.setValue("");

        codigo6Txt = new TextField("");
        codigo6Txt.setWidth("2em");
        codigo6Txt.setVisible(false);
        codigo6Txt.setValue("");

        codigo7Txt = new TextField("");
        codigo7Txt.setWidth("2em");
        codigo7Txt.setVisible(false);
        codigo7Txt.setValue("");

        cuentaContable1Cbx = new ComboBox("Cuenta contable :");
        cuentaContable1Cbx.setWidth("30em");
        cuentaContable1Cbx.setFilteringMode(FilteringMode.CONTAINS);

        cuentaContable2Cbx = new ComboBox();
        cuentaContable2Cbx.setWidth("30em");
        cuentaContable2Cbx.setFilteringMode(FilteringMode.CONTAINS);

        cuentaContable3Cbx = new ComboBox();
        cuentaContable3Cbx.setWidth("30em");
        cuentaContable3Cbx.setFilteringMode(FilteringMode.CONTAINS);

        cuentaContable4Cbx = new ComboBox();
        cuentaContable4Cbx.setWidth("30em");
        cuentaContable4Cbx.setFilteringMode(FilteringMode.CONTAINS);

        cuentaContable5Cbx = new ComboBox();
        cuentaContable5Cbx.setWidth("30em");
        cuentaContable5Cbx.setFilteringMode(FilteringMode.CONTAINS);

        cuentaContable6Cbx = new ComboBox();
        cuentaContable6Cbx.setWidth("30em");
        cuentaContable6Cbx.setFilteringMode(FilteringMode.CONTAINS);

        cuentaContable7Cbx = new ComboBox();
        cuentaContable7Cbx.setWidth("30em");
        cuentaContable7Cbx.setFilteringMode(FilteringMode.CONTAINS);

        llenarComboCuentaContable();

        debe1Txt = new NumberField("Debe :");
        debe1Txt.setValidationVisible(false);
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
        debe1Txt.setWidth("8em");
        debe1Txt.setValue(0.00);

        debe2Txt = new NumberField();
        debe2Txt.setValidationVisible(false);
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
        debe2Txt.setWidth("8em");
        debe2Txt.setValue(0.00);

        debe3Txt = new NumberField();
        debe3Txt.setValidationVisible(false);
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
        debe3Txt.setWidth("8em");
        debe3Txt.setValue(0.00);

        debe4Txt = new NumberField();
        debe4Txt.setValidationVisible(false);
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
        debe4Txt.setWidth("8em");
        debe4Txt.setValue(0.00);

        debe5Txt = new NumberField();
        debe5Txt.setValidationVisible(false);
        debe5Txt.setDecimalAllowed(true);
        debe5Txt.setDecimalPrecision(2);
        debe5Txt.setMinimumFractionDigits(2);
        debe5Txt.setDecimalSeparator('.');
        debe5Txt.setDecimalSeparatorAlwaysShown(true);
        debe5Txt.setValue(0d);
        debe5Txt.setGroupingUsed(true);
        debe5Txt.setGroupingSeparator(',');
        debe5Txt.setGroupingSize(3);
        debe5Txt.setImmediate(true);
        debe5Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        debe5Txt.setWidth("8em");
        debe5Txt.setValue(0.00);

        debe6Txt = new NumberField();
        debe6Txt.setValidationVisible(false);
        debe6Txt.setDecimalAllowed(true);
        debe6Txt.setDecimalPrecision(2);
        debe6Txt.setMinimumFractionDigits(2);
        debe6Txt.setDecimalSeparator('.');
        debe6Txt.setDecimalSeparatorAlwaysShown(true);
        debe6Txt.setValue(0d);
        debe6Txt.setGroupingUsed(true);
        debe6Txt.setGroupingSeparator(',');
        debe6Txt.setGroupingSize(3);
        debe6Txt.setImmediate(true);
        debe6Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        debe6Txt.setWidth("8em");
        debe6Txt.setValue(0.00);

        debe7Txt = new NumberField();
        debe7Txt.setValidationVisible(false);
        debe7Txt.setDecimalAllowed(true);
        debe7Txt.setDecimalPrecision(2);
        debe7Txt.setMinimumFractionDigits(2);
        debe7Txt.setDecimalSeparator('.');
        debe7Txt.setDecimalSeparatorAlwaysShown(true);
        debe7Txt.setValue(0d);
        debe7Txt.setGroupingUsed(true);
        debe7Txt.setGroupingSeparator(',');
        debe7Txt.setGroupingSize(3);
        debe7Txt.setImmediate(true);
        debe7Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        debe7Txt.setWidth("8em");
        debe7Txt.setValue(0.00);

        debe1QTxt = new NumberField("Debe Q. :");
        debe1QTxt.setValidationVisible(false);
        debe1QTxt.setDecimalAllowed(true);
        debe1QTxt.setDecimalPrecision(2);
        debe1QTxt.setMinimumFractionDigits(2);
        debe1QTxt.setDecimalSeparator('.');
        debe1QTxt.setDecimalSeparatorAlwaysShown(true);
        debe1QTxt.setValue(0d);
        debe1QTxt.setGroupingUsed(true);
        debe1QTxt.setGroupingSeparator(',');
        debe1QTxt.setGroupingSize(3);
        debe1QTxt.setImmediate(true);
        debe1QTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        debe1QTxt.setWidth("8em");
        debe1QTxt.setValue(0.00);

        debe2QTxt = new NumberField();
        debe2QTxt.setValidationVisible(false);
        debe2QTxt.setDecimalAllowed(true);
        debe2QTxt.setDecimalPrecision(2);
        debe2QTxt.setMinimumFractionDigits(2);
        debe2QTxt.setDecimalSeparator('.');
        debe2QTxt.setDecimalSeparatorAlwaysShown(true);
        debe2QTxt.setValue(0d);
        debe2QTxt.setGroupingUsed(true);
        debe2QTxt.setGroupingSeparator(',');
        debe2QTxt.setGroupingSize(3);
        debe2QTxt.setImmediate(true);
        debe2QTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        debe2QTxt.setWidth("8em");
        debe2QTxt.setValue(0.00);

        debe3QTxt = new NumberField();
        debe3QTxt.setValidationVisible(false);
        debe3QTxt.setDecimalAllowed(true);
        debe3QTxt.setDecimalPrecision(2);
        debe3QTxt.setMinimumFractionDigits(2);
        debe3QTxt.setDecimalSeparator('.');
        debe3QTxt.setDecimalSeparatorAlwaysShown(true);
        debe3QTxt.setValue(0d);
        debe3QTxt.setGroupingUsed(true);
        debe3QTxt.setGroupingSeparator(',');
        debe3QTxt.setGroupingSize(3);
        debe3QTxt.setImmediate(true);
        debe3QTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        debe3QTxt.setWidth("8em");
        debe3QTxt.setValue(0.00);

        debe4QTxt = new NumberField();
        debe4QTxt.setValidationVisible(false);
        debe4QTxt.setDecimalAllowed(true);
        debe4QTxt.setDecimalPrecision(2);
        debe4QTxt.setMinimumFractionDigits(2);
        debe4QTxt.setDecimalSeparator('.');
        debe4QTxt.setDecimalSeparatorAlwaysShown(true);
        debe4QTxt.setValue(0d);
        debe4QTxt.setGroupingUsed(true);
        debe4QTxt.setGroupingSeparator(',');
        debe4QTxt.setGroupingSize(3);
        debe4QTxt.setImmediate(true);
        debe4QTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        debe4QTxt.setWidth("8em");
        debe4QTxt.setValue(0.00);

        debe5QTxt = new NumberField();
        debe5QTxt.setValidationVisible(false);
        debe5QTxt.setDecimalAllowed(true);
        debe5QTxt.setDecimalPrecision(2);
        debe5QTxt.setMinimumFractionDigits(2);
        debe5QTxt.setDecimalSeparator('.');
        debe5QTxt.setDecimalSeparatorAlwaysShown(true);
        debe5QTxt.setValue(0d);
        debe5QTxt.setGroupingUsed(true);
        debe5QTxt.setGroupingSeparator(',');
        debe5QTxt.setGroupingSize(3);
        debe5QTxt.setImmediate(true);
        debe5QTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        debe5QTxt.setWidth("8em");
        debe5QTxt.setValue(0.00);

        debe6QTxt = new NumberField();
        debe6QTxt.setValidationVisible(false);
        debe6QTxt.setDecimalAllowed(true);
        debe6QTxt.setDecimalPrecision(2);
        debe6QTxt.setMinimumFractionDigits(2);
        debe6QTxt.setDecimalSeparator('.');
        debe6QTxt.setDecimalSeparatorAlwaysShown(true);
        debe6QTxt.setValue(0d);
        debe6QTxt.setGroupingUsed(true);
        debe6QTxt.setGroupingSeparator(',');
        debe6QTxt.setGroupingSize(3);
        debe6QTxt.setImmediate(true);
        debe6QTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        debe6QTxt.setWidth("8em");
        debe6QTxt.setValue(0.00);

        debe7QTxt = new NumberField();
        debe7QTxt.setValidationVisible(false);
        debe7QTxt.setDecimalAllowed(true);
        debe7QTxt.setDecimalPrecision(2);
        debe7QTxt.setMinimumFractionDigits(2);
        debe7QTxt.setDecimalSeparator('.');
        debe7QTxt.setDecimalSeparatorAlwaysShown(true);
        debe7QTxt.setValue(0d);
        debe7QTxt.setGroupingUsed(true);
        debe7QTxt.setGroupingSeparator(',');
        debe7QTxt.setGroupingSize(3);
        debe7QTxt.setImmediate(true);
        debe7QTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        debe7QTxt.setWidth("8em");
        debe7QTxt.setValue(0.00);

        haber1Txt = new NumberField("Haber:");
        haber1Txt.setValidationVisible(false);
        haber1Txt.setDecimalAllowed(true);
        haber1Txt.setDecimalPrecision(2);
        haber1Txt.setMinimumFractionDigits(2);
        haber1Txt.setDecimalSeparator('.');
        haber1Txt.setDecimalSeparatorAlwaysShown(true);
        haber1Txt.setValue(0d);
        haber1Txt.setGroupingUsed(true);
        haber1Txt.setGroupingSeparator(',');
        haber1Txt.setGroupingSize(3);
        haber1Txt.setImmediate(true);
        haber1Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        haber1Txt.setWidth("8em");
        haber1Txt.setValue(0.00);

        haber2Txt = new NumberField();
        haber2Txt.setValidationVisible(false);
        haber2Txt.setDecimalAllowed(true);
        haber2Txt.setDecimalPrecision(2);
        haber2Txt.setMinimumFractionDigits(2);
        haber2Txt.setDecimalSeparator('.');
        haber2Txt.setDecimalSeparatorAlwaysShown(true);
        haber2Txt.setValue(0d);
        haber2Txt.setGroupingUsed(true);
        haber2Txt.setGroupingSeparator(',');
        haber2Txt.setGroupingSize(3);
        haber2Txt.setImmediate(true);
        haber2Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        haber2Txt.setWidth("8em");
        haber2Txt.setValue(0.00);

        haber3Txt = new NumberField();
        haber3Txt.setValidationVisible(false);
        haber3Txt.setDecimalAllowed(true);
        haber3Txt.setDecimalPrecision(2);
        haber3Txt.setMinimumFractionDigits(2);
        haber3Txt.setDecimalSeparator('.');
        haber3Txt.setDecimalSeparatorAlwaysShown(true);
        haber3Txt.setValue(0d);
        haber3Txt.setGroupingUsed(true);
        haber3Txt.setGroupingSeparator(',');
        haber3Txt.setGroupingSize(3);
        haber3Txt.setImmediate(true);
        haber3Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        haber3Txt.setWidth("8em");
        haber3Txt.setValue(0.00);

        haber4Txt = new NumberField();
        haber4Txt.setValidationVisible(false);
        haber4Txt.setDecimalAllowed(true);
        haber4Txt.setDecimalPrecision(2);
        haber4Txt.setMinimumFractionDigits(2);
        haber4Txt.setDecimalSeparator('.');
        haber4Txt.setDecimalSeparatorAlwaysShown(true);
        haber4Txt.setValue(0d);
        haber4Txt.setGroupingUsed(true);
        haber4Txt.setGroupingSeparator(',');
        haber4Txt.setGroupingSize(3);
        haber4Txt.setImmediate(true);
        haber4Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        haber4Txt.setWidth("8em");
        haber4Txt.setValue(0.00);

        haber5Txt = new NumberField();
        haber5Txt.setValidationVisible(false);
        haber5Txt.setDecimalAllowed(true);
        haber5Txt.setDecimalPrecision(2);
        haber5Txt.setMinimumFractionDigits(2);
        haber5Txt.setDecimalSeparator('.');
        haber5Txt.setDecimalSeparatorAlwaysShown(true);
        haber5Txt.setValue(0d);
        haber5Txt.setGroupingUsed(true);
        haber5Txt.setGroupingSeparator(',');
        haber5Txt.setGroupingSize(3);
        haber5Txt.setImmediate(true);
        haber5Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        haber5Txt.setWidth("8em");
        haber5Txt.setValue(0.00);

        haber6Txt = new NumberField();
        haber6Txt.setValidationVisible(false);
        haber6Txt.setDecimalAllowed(true);
        haber6Txt.setDecimalPrecision(2);
        haber6Txt.setMinimumFractionDigits(2);
        haber6Txt.setDecimalSeparator('.');
        haber6Txt.setDecimalSeparatorAlwaysShown(true);
        haber6Txt.setValue(0d);
        haber6Txt.setGroupingUsed(true);
        haber6Txt.setGroupingSeparator(',');
        haber6Txt.setGroupingSize(3);
        haber6Txt.setImmediate(true);
        haber6Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        haber6Txt.setWidth("8em");
        haber6Txt.setValue(0.00);

        haber7Txt = new NumberField();
        haber7Txt.setValidationVisible(false);
        haber7Txt.setDecimalAllowed(true);
        haber7Txt.setDecimalPrecision(2);
        haber7Txt.setMinimumFractionDigits(2);
        haber7Txt.setDecimalSeparator('.');
        haber7Txt.setDecimalSeparatorAlwaysShown(true);
        haber7Txt.setValue(0d);
        haber7Txt.setGroupingUsed(true);
        haber7Txt.setGroupingSeparator(',');
        haber5Txt.setGroupingSize(3);
        haber7Txt.setImmediate(true);
        haber7Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        haber7Txt.setWidth("8em");
        haber7Txt.setValue(0.00);

        haber1QTxt = new NumberField("Haber Q. :");
        haber1QTxt.setValidationVisible(false);
        haber1QTxt.setDecimalAllowed(true);
        haber1QTxt.setDecimalPrecision(2);
        haber1QTxt.setMinimumFractionDigits(2);
        haber1QTxt.setDecimalSeparator('.');
        haber1QTxt.setDecimalSeparatorAlwaysShown(true);
        haber1QTxt.setValue(0d);
        haber1QTxt.setGroupingUsed(true);
        haber1QTxt.setGroupingSeparator(',');
        haber1QTxt.setGroupingSize(3);
        haber1QTxt.setImmediate(true);
        haber1QTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        haber1QTxt.setWidth("8em");
        haber1QTxt.setValue(0.00);

        haber2QTxt = new NumberField();
        haber2QTxt.setValidationVisible(false);
        haber2QTxt.setDecimalAllowed(true);
        haber2QTxt.setDecimalPrecision(2);
        haber2QTxt.setMinimumFractionDigits(2);
        haber2QTxt.setDecimalSeparator('.');
        haber2QTxt.setDecimalSeparatorAlwaysShown(true);
        haber2QTxt.setValue(0d);
        haber2QTxt.setGroupingUsed(true);
        haber2QTxt.setGroupingSeparator(',');
        haber2QTxt.setGroupingSize(3);
        haber2QTxt.setImmediate(true);
        haber2QTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        haber2QTxt.setWidth("8em");
        haber2QTxt.setValue(0.00);

        haber3QTxt = new NumberField();
        haber3QTxt.setValidationVisible(false);
        haber3QTxt.setDecimalAllowed(true);
        haber3QTxt.setDecimalPrecision(2);
        haber3QTxt.setMinimumFractionDigits(2);
        haber3QTxt.setDecimalSeparator('.');
        haber3QTxt.setDecimalSeparatorAlwaysShown(true);
        haber3QTxt.setValue(0d);
        haber3QTxt.setGroupingUsed(true);
        haber3QTxt.setGroupingSeparator(',');
        haber3QTxt.setGroupingSize(3);
        haber3QTxt.setImmediate(true);
        haber3QTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        haber3QTxt.setWidth("8em");
        haber3QTxt.setValue(0.00);

        haber4QTxt = new NumberField();
        haber4QTxt.setValidationVisible(false);
        haber4QTxt.setDecimalAllowed(true);
        haber4QTxt.setDecimalPrecision(2);
        haber4QTxt.setMinimumFractionDigits(2);
        haber4QTxt.setDecimalSeparator('.');
        haber4QTxt.setDecimalSeparatorAlwaysShown(true);
        haber4QTxt.setValue(0d);
        haber4QTxt.setGroupingUsed(true);
        haber4QTxt.setGroupingSeparator(',');
        haber4QTxt.setGroupingSize(3);
        haber4QTxt.setImmediate(true);
        haber4QTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        haber4QTxt.setWidth("8em");
        haber4QTxt.setValue(0.00);

        haber5QTxt = new NumberField();
        haber5QTxt.setValidationVisible(false);
        haber5QTxt.setDecimalAllowed(true);
        haber5QTxt.setDecimalPrecision(2);
        haber5QTxt.setMinimumFractionDigits(2);
        haber5QTxt.setDecimalSeparator('.');
        haber5QTxt.setDecimalSeparatorAlwaysShown(true);
        haber5QTxt.setValue(0d);
        haber5QTxt.setGroupingUsed(true);
        haber5QTxt.setGroupingSeparator(',');
        haber5QTxt.setGroupingSize(3);
        haber5QTxt.setImmediate(true);
        haber5QTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        haber5QTxt.setWidth("8em");
        haber5QTxt.setValue(0.00);

        haber6QTxt = new NumberField();
        haber6QTxt.setValidationVisible(false);
        haber6QTxt.setDecimalAllowed(true);
        haber6QTxt.setDecimalPrecision(2);
        haber6QTxt.setMinimumFractionDigits(2);
        haber6QTxt.setDecimalSeparator('.');
        haber6QTxt.setDecimalSeparatorAlwaysShown(true);
        haber6QTxt.setValue(0d);
        haber6QTxt.setGroupingUsed(true);
        haber6QTxt.setGroupingSeparator(',');
        haber6QTxt.setGroupingSize(3);
        haber6QTxt.setImmediate(true);
        haber6QTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        haber6QTxt.setWidth("8em");
        haber6QTxt.setValue(0.00);

        haber7QTxt = new NumberField();
        haber7QTxt.setValidationVisible(false);
        haber7QTxt.setDecimalAllowed(true);
        haber7QTxt.setDecimalPrecision(2);
        haber7QTxt.setMinimumFractionDigits(2);
        haber7QTxt.setDecimalSeparator('.');
        haber7QTxt.setDecimalSeparatorAlwaysShown(true);
        haber7QTxt.setValue(0d);
        haber7QTxt.setGroupingUsed(true);
        haber7QTxt.setGroupingSeparator(',');
        haber7QTxt.setGroupingSize(3);
        haber7QTxt.setImmediate(true);
        haber7QTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        haber7QTxt.setWidth("8em");
        haber7QTxt.setValue(0.00);

        Button desAutorizarBtn = new Button("Eliminar Autorización");
        desAutorizarBtn.addStyleName(ValoTheme.BUTTON_DANGER);
        desAutorizarBtn.setIcon(FontAwesome.TRASH);
        desAutorizarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (enganchesGrid.getSelectedRows() != null) {

                    Iterator iter;
                    iter = enganchesGrid.getSelectedRows().iterator();

                    while (iter.hasNext()) {

                        Object gridItem = iter.next();
                        String idAutorizacion = String.valueOf(enganchesGrid.getContainerDataSource().getItem(gridItem).getItemProperty(NO_PROPERTY).getValue());
                        String codigoCC = String.valueOf(enganchesGrid.getContainerDataSource().getItem(gridItem).getItemProperty(CODIGO_CC).getValue());

                        ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Desea eliminar la autorizacion de devolución de enganches ?", "SI", "NO", new ConfirmDialog.Listener() {
                            public void onClose(ConfirmDialog dialog) {
                                if (dialog.isConfirmed()) {

                                    queryString = "DELETE FROM autorizacion_pago";
                                    queryString += " WHERE IdAutorizacion = " + idAutorizacion;

                                    try {

                                        stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                                        stQuery.executeUpdate(queryString);

                                        Notification.show("Autorizacion eliminada con exito! ", Notification.Type.HUMANIZED_MESSAGE);

                                        ((PagarView) (mainUI.getNavigator().getCurrentView())).documentosContainer.removeItem(((PagarView) (mainUI.getNavigator().getCurrentView())).documentosGrid.getSelectedRow());
 /**
                                        for (Iterator iTerator = ((PagarView) (mainUI.getNavigator().getCurrentView())).documentosContainer.getItemIds().iterator(); iTerator.hasNext();) {
                                            Object itemId = iTerator.next();

                                            if(idAutorizacion.equals(String.valueOf(((PagarView) (mainUI.getNavigator().getCurrentView())).documentosContainer.getContainerProperty(itemId, PagarView.ID_AUTO_PROPERTY).getValue()))) {
                                                ((PagarView) (mainUI.getNavigator().getCurrentView())).documentosContainer.getContainerProperty(itemId, PagarView.ESTATUS_PROPERTY).setValue("NO AUTORIZADO");
                                            }
                                        }
**/
                                    } catch (SQLException throwables) {
                                        throwables.printStackTrace();
                                    }
                                }
                            }
                        });
                    }
                    llenarTablaEnganches();
                    close();

                } else {
                    Notification.show("Por favor seleccione un registro..", Notification.Type.ASSISTIVE_NOTIFICATION);
                }
            }
        });

        grabarPartidaBtn = new Button("Registrar pago");
        grabarPartidaBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        grabarPartidaBtn.setIcon(FontAwesome.SAVE);
        grabarPartidaBtn.setWidth(String.valueOf(desAutorizarBtn.getWidth()));
        grabarPartidaBtn.addClickListener((event) -> {
            insertarPartidaSimple();
        });

        Button cancelarBtn = new Button("Cancelar");
        cancelarBtn.addStyleName(ValoTheme.BUTTON_DANGER);
        cancelarBtn.setIcon(FontAwesome.BAN);
        cancelarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                proveedorCbx.setReadOnly(false);
                limpiarPartida();
                proveedorCbx.setReadOnly(true);
                proveedorCbx.focus();
            }
        });

        layoutHorizontal1.addComponent(codigo1Txt);
        layoutHorizontal1.addComponent(cuentaContable1Cbx);
        layoutHorizontal1.addComponent(debe1Txt);
        layoutHorizontal1.addComponent(haber1Txt);
        layoutHorizontal1.addComponent(debe1QTxt);
        layoutHorizontal1.addComponent(haber1QTxt);

        layoutHorizontal2.addComponent(codigo2Txt);
        layoutHorizontal2.addComponent(cuentaContable2Cbx);
        layoutHorizontal2.addComponent(debe2Txt);
        layoutHorizontal2.addComponent(haber2Txt);
        layoutHorizontal2.addComponent(debe2QTxt);
        layoutHorizontal2.addComponent(haber2QTxt);

        layoutHorizontal3.addComponent(codigo3Txt);
        layoutHorizontal3.addComponent(cuentaContable3Cbx);
        layoutHorizontal3.addComponent(debe3Txt);
        layoutHorizontal3.addComponent(haber3Txt);
        layoutHorizontal3.addComponent(debe3QTxt);
        layoutHorizontal3.addComponent(haber3QTxt);

        layoutHorizontal4.addComponent(codigo4Txt);
        layoutHorizontal4.addComponent(cuentaContable4Cbx);
        layoutHorizontal4.addComponent(debe4Txt);
        layoutHorizontal4.addComponent(haber4Txt);
        layoutHorizontal4.addComponent(debe4QTxt);
        layoutHorizontal4.addComponent(haber4QTxt);

        layoutHorizontal4.addComponent(codigo5Txt);
        layoutHorizontal5.addComponent(cuentaContable5Cbx);
        layoutHorizontal5.addComponent(debe5Txt);
        layoutHorizontal5.addComponent(haber5Txt);
        layoutHorizontal5.addComponent(debe5QTxt);
        layoutHorizontal5.addComponent(haber5QTxt);

        //reservados para cuenta bancos y diferencial cambiario cuando sean 5 enganches..., por default no tienen codigoCC
        layoutHorizontal6.addComponent(cuentaContable6Cbx);
        layoutHorizontal6.addComponent(debe6Txt);
        layoutHorizontal6.addComponent(haber6Txt);
        layoutHorizontal6.addComponent(debe6QTxt);
        layoutHorizontal6.addComponent(haber6QTxt);

        layoutHorizontal7.addComponent(cuentaContable7Cbx);
        layoutHorizontal7.addComponent(debe7Txt);
        layoutHorizontal7.addComponent(haber7Txt);
        layoutHorizontal7.addComponent(debe7QTxt);
        layoutHorizontal7.addComponent(haber7QTxt);

//        layoutHorizontalBotones.addComponent(cancelarBtn);
//        layoutHorizontalBotones.setComponentAlignment(cancelarBtn, Alignment.BOTTOM_LEFT);
        layoutHorizontalBotones.addComponent(desAutorizarBtn);
        layoutHorizontalBotones.setComponentAlignment(desAutorizarBtn, Alignment.BOTTOM_CENTER);
        layoutHorizontalBotones.addComponent(grabarPartidaBtn);
        layoutHorizontalBotones.setComponentAlignment(grabarPartidaBtn, Alignment.BOTTOM_RIGHT);

        partidaLayout.addComponent(layoutHorizontal1);
        partidaLayout.setComponentAlignment(layoutHorizontal1, Alignment.MIDDLE_CENTER);

        partidaLayout.addComponent(layoutHorizontal2);
        partidaLayout.setComponentAlignment(layoutHorizontal2, Alignment.MIDDLE_CENTER);

        partidaLayout.addComponent(layoutHorizontal3);
        partidaLayout.setComponentAlignment(layoutHorizontal3, Alignment.MIDDLE_CENTER);

        partidaLayout.addComponent(layoutHorizontal4);
        partidaLayout.setComponentAlignment(layoutHorizontal4, Alignment.MIDDLE_CENTER);

        partidaLayout.addComponent(layoutHorizontal5);
        partidaLayout.setComponentAlignment(layoutHorizontal5, Alignment.MIDDLE_CENTER);

        partidaLayout.addComponent(layoutHorizontal6);
        partidaLayout.setComponentAlignment(layoutHorizontal6, Alignment.MIDDLE_CENTER);

        partidaLayout.addComponent(layoutHorizontal7);
        partidaLayout.setComponentAlignment(layoutHorizontal7, Alignment.MIDDLE_CENTER);

        partidaLayout.addComponent(layoutHorizontalBotones);
        partidaLayout.setComponentAlignment(layoutHorizontalBotones, Alignment.MIDDLE_CENTER);

        mainLayout.addComponent(partidaLayout);
        mainLayout.setComponentAlignment(partidaLayout, Alignment.MIDDLE_CENTER);

    }

    public void calcularPartida() {

        Object gridItem;
        proveedorId = "";
        nombreProveedor = "";
        double montoTotalSeleccionado = 0.00;

        Iterator iter = enganchesGrid.getSelectedRows().iterator();

        if (iter == null) {
            limpiarPartida();

            nombreChequeTxt.setReadOnly(false);
            nombreChequeTxt.setValue("");

            montoTxt.setReadOnly(false);
            montoTxt.setValue(montoTotalSeleccionado);
            return;
        }
        if (!iter.hasNext()) {
            limpiarPartida();

            nombreChequeTxt.setReadOnly(false);
            nombreChequeTxt.setValue("");

            montoTxt.setReadOnly(false);
            montoTxt.setValue(montoTotalSeleccionado);
            return;
        }

        gridItem = iter.next();
        proveedorId = String.valueOf(enganchesGrid.getContainerDataSource().getItem(gridItem).getItemProperty(ID_PROVEEDOR_PROPERTY).getValue());
        nombreProveedor = String.valueOf(enganchesGrid.getContainerDataSource().getItem(gridItem).getItemProperty(PROVEEDOR_PROPERTY).getValue());
        montoTotalSeleccionado = Double.valueOf(String.valueOf(enganchesGrid.getContainerDataSource().getItem(gridItem).getItemProperty(MONTO_PROPERTY).getValue()).replaceAll(",", ""));

        while (iter.hasNext()) { //// Si hay mas de un registro seleccionado
            gridItem = iter.next();
            montoTotalSeleccionado += Double.valueOf(String.valueOf(enganchesGrid.getContainerDataSource().getItem(gridItem).getItemProperty(MONTO_PROPERTY).getValue()).replaceAll(",", ""));

        }
        limpiarPartida();

        Iterator iter2 = enganchesGrid.getSelectedRows().iterator();

        double montoEnganche = 0.00, montoProporcialQ = 0.00;
        String codigoCC = "";

        while (iter2.hasNext()) {  // POR CADA ENGANCHE QUE ESTAMOS SELECCINANDO

            Object gridItem2 = iter2.next();
            montoEnganche = Double.valueOf(String.valueOf(enganchesGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_PROPERTY).getValue()).replaceAll(",", ""));
            montoProporcialQ = Double.valueOf(String.valueOf(enganchesGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_Q_PROPERTY).getValue()).replaceAll(",", ""));
            codigoCC = String.valueOf(enganchesGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(CODIGO_CC).getValue());

            montoTxt.setReadOnly(false);
            montoTxt.setValue(montoTotalSeleccionado);
            nombreChequeTxt.setReadOnly(false);
            nombreChequeTxt.setValue(nombreProveedor);
//                                nombreChequeTxt.setReadOnly(true);

            monedaCbx.setReadOnly(false);
            monedaCbx.select(String.valueOf(enganchesGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONEDA_PROPERTY).getValue()));

            try {
                //// ENGANCHES SELECCIONADOS
                if (cuentaContable1Cbx.getValue() == null) {
                    cuentaContable1Cbx.setValue(enganchesGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(IDNOMENCLATURA).getValue());
                    debe1Txt.setValue(montoEnganche);
                    debe1QTxt.setValue(montoProporcialQ);
                    codigo1Txt.setValue(codigoCC);

                } else if (cuentaContable2Cbx.getValue() == null) {
                    cuentaContable2Cbx.setValue(enganchesGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(IDNOMENCLATURA).getValue());
                    debe2Txt.setValue(montoEnganche);
                    debe2QTxt.setValue(montoProporcialQ);
                    codigo2Txt.setValue(codigoCC);

                } else if (cuentaContable3Cbx.getValue() == null) {
                    cuentaContable3Cbx.setValue(enganchesGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(IDNOMENCLATURA).getValue());
                    debe3Txt.setValue(montoEnganche);
                    debe3QTxt.setValue(montoProporcialQ);
                    codigo3Txt.setValue(codigoCC);

                } else if (cuentaContable4Cbx.getValue() == null) {
                    cuentaContable4Cbx.select(enganchesGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(IDNOMENCLATURA).getValue());
                    debe4Txt.setValue(montoEnganche);
                    debe4QTxt.setValue(montoProporcialQ);
                    codigo4Txt.setValue(codigoCC);

                } else if (cuentaContable5Cbx.getValue() == null) {
                    cuentaContable5Cbx.select(enganchesGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(IDNOMENCLATURA).getValue());
                    debe5Txt.setValue(montoEnganche);
                    debe5QTxt.setValue(montoProporcialQ);
                    codigo5Txt.setValue(codigoCC);

                }
            } catch (Exception ex) {
                System.out.println("Error " + ex);
            }
        }

        if(monedaCbx.getValue().toString().equals("QUETZALES")) {
            tasaCambioTxt.setValue(1.00);
            if (cuentaContable2Cbx.getValue() == null) {
                cuentaContable2Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaLocal());
                haber2Txt.setValue(montoTotalSeleccionado);
                haber2QTxt.setReadOnly(false);
                haber2QTxt.setValue(montoTotalSeleccionado * tasaCambioTxt.getDoubleValueDoNotThrow());

            } else if (cuentaContable3Cbx.getValue() == null) {
                cuentaContable3Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaLocal());
                haber3Txt.setValue(montoTotalSeleccionado);
                haber3QTxt.setReadOnly(false);
                haber3QTxt.setValue(montoTotalSeleccionado * tasaCambioTxt.getDoubleValueDoNotThrow());

            } else if (cuentaContable4Cbx.getValue() == null) {
                cuentaContable4Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaLocal());
                haber4Txt.setValue(montoTotalSeleccionado);
                haber4QTxt.setReadOnly(false);
                haber4QTxt.setValue(montoTotalSeleccionado * tasaCambioTxt.getDoubleValueDoNotThrow());

            } else if (cuentaContable5Cbx.getValue() == null) {
                cuentaContable5Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaLocal());
                haber5Txt.setValue(montoTotalSeleccionado);
                haber5QTxt.setReadOnly(false);
                haber5QTxt.setValue(montoTotalSeleccionado * tasaCambioTxt.getDoubleValueDoNotThrow());

            } else if (cuentaContable6Cbx.getValue() == null) {
                cuentaContable6Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaLocal());
                haber6Txt.setValue(montoTotalSeleccionado);
                haber6QTxt.setReadOnly(false);
                haber6QTxt.setValue(montoTotalSeleccionado * tasaCambioTxt.getDoubleValueDoNotThrow());
            }
        }
        else {
            double enquetzales = Double.valueOf(numberFormat3.format(montoTotalSeleccionado * tasaCambioTxt.getDoubleValueDoNotThrow()));
//                                tasaCambioTxt.setValue((Float.toString(((SopdiUI) UI.getCurrent()).sessionInformation.getFltExchangeRate())));
            if (cuentaContable2Cbx.getValue() == null) {
                cuentaContable2Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaExtranjera());
                haber2Txt.setValue(montoTotalSeleccionado);
                haber2QTxt.setReadOnly(false);
                haber2QTxt.setValue(enquetzales);

            } else if (cuentaContable3Cbx.getValue() == null) {
                cuentaContable3Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaExtranjera());
                haber3Txt.setValue(montoTotalSeleccionado);
                haber3QTxt.setReadOnly(false);
                haber3QTxt.setValue(enquetzales);

            } else if (cuentaContable4Cbx.getValue() == null) {
                cuentaContable4Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaExtranjera());
                haber4Txt.setValue(montoTotalSeleccionado);
                haber4QTxt.setReadOnly(false);
                haber4QTxt.setValue(enquetzales);

            } else if (cuentaContable5Cbx.getValue() == null) {
                cuentaContable5Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaExtranjera());
                haber5Txt.setValue(montoTotalSeleccionado);
                haber5QTxt.setReadOnly(false);
                haber5QTxt.setValue(enquetzales);

            } else if (cuentaContable6Cbx.getValue() == null) {
                cuentaContable6Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaExtranjera());
                haber6Txt.setValue(montoTotalSeleccionado);
                haber6QTxt.setReadOnly(false);
                haber6QTxt.setValue(enquetzales);

            }
        }
        totalDebe = new BigDecimal(debe1QTxt.getDoubleValueDoNotThrow()
                + debe2QTxt.getDoubleValueDoNotThrow() + debe3QTxt.getDoubleValueDoNotThrow()
                + debe4QTxt.getDoubleValueDoNotThrow() + debe5QTxt.getDoubleValueDoNotThrow()
        ).setScale(2, BigDecimal.ROUND_HALF_UP);

        totalHaber = new BigDecimal(haber1QTxt.getDoubleValueDoNotThrow()
                + haber2QTxt.getDoubleValueDoNotThrow() + haber3QTxt.getDoubleValueDoNotThrow()
                + haber4QTxt.getDoubleValueDoNotThrow() + haber5QTxt.getDoubleValueDoNotThrow()
                + haber6QTxt.getDoubleValueDoNotThrow() + haber7QTxt.getDoubleValueDoNotThrow()
        ).setScale(2, BigDecimal.ROUND_HALF_UP);

        totalDebe.setScale(2, BigDecimal.ROUND_HALF_UP);
        totalHaber.setScale(2, BigDecimal.ROUND_HALF_UP);

        double diferencia = Double.valueOf(numberFormat3.format((totalHaber.doubleValue() - totalDebe.doubleValue())));

        //para diferencial cambiario, si es que lo hay ...
        if (diferencia < 0) {
            diferencia = diferencia * -1;
            if (cuentaContable3Cbx.getValue() == null) {
                cuentaContable3Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getDiferencialCambiario());
                haber3QTxt.setValue(diferencia);

            } else if (cuentaContable4Cbx.getValue() == null && !cuentaContable3Cbx.getValue().equals(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getDiferencialCambiario())) {
                cuentaContable4Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getDiferencialCambiario());
                haber4QTxt.setValue(diferencia);

            } else if (cuentaContable5Cbx.getValue() == null && !cuentaContable4Cbx.getValue().equals(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getDiferencialCambiario())) {
                cuentaContable5Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getDiferencialCambiario());
                haber5QTxt.setValue(diferencia);

            } else if (cuentaContable6Cbx.getValue() == null && !cuentaContable5Cbx.getValue().equals(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getDiferencialCambiario())) {
                cuentaContable6Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getDiferencialCambiario());
                haber6QTxt.setValue(diferencia);

            } else if (cuentaContable7Cbx.getValue() == null && !cuentaContable6Cbx.getValue().equals(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getDiferencialCambiario())) {
                cuentaContable7Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getDiferencialCambiario());
                haber7QTxt.setValue(diferencia);

            }
        }
        else {
            if (cuentaContable3Cbx.getValue() == null) {
                cuentaContable3Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getDiferencialCambiario());
                debe3QTxt.setValue(diferencia);

            } else if (cuentaContable4Cbx.getValue() == null && !cuentaContable3Cbx.getValue().equals(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getDiferencialCambiario())) {
                cuentaContable4Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getDiferencialCambiario());
                debe4QTxt.setValue(diferencia);

            } else if (cuentaContable5Cbx.getValue() == null && !cuentaContable4Cbx.getValue().equals(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getDiferencialCambiario())) {
                cuentaContable5Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getDiferencialCambiario());
                debe5QTxt.setValue(diferencia);

            } else if (cuentaContable6Cbx.getValue() == null && !cuentaContable5Cbx.getValue().equals(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getDiferencialCambiario())) {
                cuentaContable6Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getDiferencialCambiario());
                debe6QTxt.setValue(diferencia);

            } else if (cuentaContable7Cbx.getValue() == null && !cuentaContable6Cbx.getValue().equals(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getDiferencialCambiario())) {
                cuentaContable7Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getDiferencialCambiario());
                debe7QTxt.setValue(diferencia);

            }
        }
    }
    
    public void llenarComboCuentaContable() {
        String queryString = " SELECT * FROM contabilidad_nomenclatura_empresa";
        queryString += " WHERE Estatus='HABILITADA'";
        queryString += " ANd IdEmprewsa = " + empresaId;
        queryString += " ORDER BY N5";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) { //  encontrado

                cuentaContable1Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable1Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable2Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable2Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable3Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable3Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable4Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable4Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable5Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable5Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable6Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable6Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable7Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable7Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar cuentas contables: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void insertarPartidaSimple() {

        if (enganchesGrid.getSelectedRows() == null) {
            Notification.show("Por favor seleccione un registro!", Notification.Type.WARNING_MESSAGE);
            return;
        }

        if (((SopdiUI) UI.getCurrent()).esMesCerrado(empresaId, Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()))) {
            Notification.show("La fecha del documento no puede ser de un mes ya cerrado contablemente, revise!", Notification.Type.WARNING_MESSAGE);
            fechaDt.focus();
            return;
        }
        if (!((SopdiUI) UI.getCurrent()).esPrimerMesAbierto(empresaId, Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()))) {
            Notification.show("El mes abierto a operaciones es : " + ((SopdiUI) UI.getCurrent()).primerMesAbierto(empresaId), Notification.Type.WARNING_MESSAGE);
            fechaDt.focus();
            return;
        }

        totalDebe = new BigDecimal(debe1Txt.getDoubleValueDoNotThrow()
                + debe2Txt.getDoubleValueDoNotThrow() + debe3Txt.getDoubleValueDoNotThrow()
                + debe4Txt.getDoubleValueDoNotThrow() + debe5Txt.getDoubleValueDoNotThrow()
                + debe6Txt.getDoubleValueDoNotThrow() + debe7Txt.getDoubleValueDoNotThrow()
        ).setScale(2, BigDecimal.ROUND_HALF_UP);

        totalHaber = new BigDecimal(haber1Txt.getDoubleValueDoNotThrow()
                + haber2Txt.getDoubleValueDoNotThrow() + haber3Txt.getDoubleValueDoNotThrow()
                + haber4Txt.getDoubleValueDoNotThrow() + haber5Txt.getDoubleValueDoNotThrow()
                + haber6Txt.getDoubleValueDoNotThrow() + haber7Txt.getDoubleValueDoNotThrow()
        ).setScale(2, BigDecimal.ROUND_HALF_UP);

        totalDebe.setScale(2, BigDecimal.ROUND_HALF_UP);
        totalHaber.setScale(2, BigDecimal.ROUND_HALF_UP);

        if (totalDebe.doubleValue() != totalHaber.doubleValue()) {
            Notification.show("La partida es descuadrada, por favor revisar"
                    + " Debe = " + totalDebe.doubleValue() + "  Haber = " + totalHaber, Notification.Type.WARNING_MESSAGE);
            return;
        }

        if (numeroTxt.getValue().trim().isEmpty()) {
            Notification.show("Por favor, escriba el cheque o transferencia. ", Notification.Type.ERROR_MESSAGE);
            numeroTxt.focus();
            return;
        }
        if (nombreChequeTxt.getValue().trim().isEmpty()) {
            Notification.show("Por favor, escriba el nombre del cheque o transferencia. ", Notification.Type.ERROR_MESSAGE);
            nombreChequeTxt.focus();
            return;
        }
        if (montoTxt.getDoubleValueDoNotThrow() == 0.00) {
            Notification.show("Por favor primero cree la partida, con el monto respectivo. ", Notification.Type.ERROR_MESSAGE);
            montoTxt.focus();
            return;
        }
        if (cuentaContable1Cbx.getValue() == null || cuentaContable2Cbx.getValue() == null) {
            Notification.show("Por favor elija la cuenta contable que corresponda. ", Notification.Type.ERROR_MESSAGE);
            cuentaContable1Cbx.focus();
            return;
        }

        queryString = "SELECT CodigoPartida FROM contabilidad_partida ";
        queryString += " WHERE NumeroDocumento = '" + numeroTxt.getValue() + "'";
        queryString += " AND IdEmpresa = " + empresaId;
        queryString += " AND TipoDocumento = '" + String.valueOf(medioCbx.getValue()) + "'";
        queryString += " AND MonedaDocumento = '" + monedaCbx.getValue() + "'";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado
                Notification.show("Documento ya registrado en pago, codigo de partida = " + rsRecords.getString("CodigoPartida"), Notification.Type.ERROR_MESSAGE);
                numeroTxt.focus();
                return;
            }

        } catch (Exception ex1) {
            System.out.println("Error al validar el documento ingresado. " + ex1.getMessage());
            ex1.printStackTrace();
        }

        String fecha = Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()); //yyy/mm/yyyy
        String ultimoEncontado;
        String dia = fecha.substring(8, 10);
        String mes = fecha.substring(5, 7);
        String año = fecha.substring(0, 4);

        String codigoPartida = empresaId + año + mes + dia + "3";

        queryString = "SELECT codigoPartida FROM contabilidad_partida ";
        queryString += " WHERE CodigoPartida LIKE '" + codigoPartida + "%'";
        queryString += " ORDER BY CodigoPartida DESC ";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado

                ultimoEncontado = rsRecords.getString("codigoPartida").substring(12, 15);

                System.out.println("ultimo encontrado " + ultimoEncontado);

                codigoPartida += String.format("%03d", (Integer.valueOf(ultimoEncontado) + 1));

            } else {
                codigoPartida += "001";
            }

        } catch (Exception ex1) {
            System.out.println("Error al buscar siguiente correlativo de partida" + ex1.getMessage());
            ex1.printStackTrace();
        }

        codigoPartidaNuevo = codigoPartida;

        queryString = "INSERT INTO contabilidad_partida (IdEmpresa, Estatus, CodigoPartida, CodigoCC,";
        queryString += " TipoDocumento, Fecha, IdProveedor, NITProveedor, NombreProveedor, NombreCheque,";
        queryString += " MontoDocumento, SerieDocumento, NumeroDocumento, TipoDOCA, NoDOCA, IdNomenclatura, MonedaDocumento, Debe, Haber,";
        queryString += " DebeQuetzales, HaberQuetzales, TipoCambio, Saldo,";
        queryString += " Descripcion, CreadoUsuario, CreadoFechaYHora)";
        queryString += " VALUES ";
        queryString += " (";
        queryString += empresaId;
        queryString += ",'INGRESADO'";
        queryString += ",'" + codigoPartida + "'";
        queryString += ",'" + codigo1Txt.getValue() + "'";
        queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
        queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
        queryString += "," + proveedorId;
        queryString += ",''";//nitproveedor
        queryString += ",'" + nombreProveedor + "'";
        queryString += ",'" + nombreChequeTxt.getValue() + "'";
        queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow());
        queryString += ",''";  //serie documento
        queryString += ",'" + numeroTxt.getValue() + "'";
        queryString += ",''"; //tipodoca
        queryString += ",''"; //doca
        queryString += "," + String.valueOf(cuentaContable1Cbx.getValue()); //idcuentacontable
        queryString += ",'" + monedaCbx.getValue() + "'";
        queryString += "," + String.valueOf(debe1Txt.getDoubleValueDoNotThrow()); //DEBE
        queryString += ",0.00"; //HABER
        queryString += "," + String.valueOf(debe1QTxt.getDoubleValueDoNotThrow()); //DEBE Q
        queryString += ",0.00"; //HABER Q.
        queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
        queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow());
        queryString += ",'" + descripcionTxt.getValue() + "'";
        queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
        queryString += ",current_timestamp";
        queryString += ")";

        if (cuentaContable2Cbx.getValue() != null && (debe2Txt.getDoubleValueDoNotThrow() > 0 || haber2Txt.getDoubleValueDoNotThrow() > 0)) {
            queryString += ",(";
            queryString += empresaId;
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigo2Txt.getValue() + "'";
            queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorId;
            queryString += ",''"; //nitproveedor
            queryString += ",'" + nombreProveedor + "'";
            queryString += ",'" + nombreChequeTxt.getValue() + "'";
            queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow());
            queryString += ",''"; //serie documento
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += ",''"; //tipodoca
            queryString += ",''"; //doca
            queryString += "," + cuentaContable2Cbx.getValue(); //idcuentacontable
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(debe2Txt.getDoubleValueDoNotThrow()); // DEBE
            queryString += "," + String.valueOf(haber2Txt.getDoubleValueDoNotThrow()); // HABER
            queryString += "," + String.valueOf(debe2QTxt.getDoubleValueDoNotThrow()); //DEBE Q.
            queryString += "," + String.valueOf(haber2QTxt.getDoubleValueDoNotThrow() ); //HABER Q
            queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow());
            queryString += ",'" + descripcionTxt.getValue() + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";

        }

        if (cuentaContable3Cbx.getValue() != null && (debe3Txt.getDoubleValueDoNotThrow() > 0 || haber3Txt.getDoubleValueDoNotThrow() > 0)) {
            queryString += ",(";
            queryString += empresaId;
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigo3Txt.getValue() + "'";
            queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorId;
            queryString += ",''";
            queryString += ",'" + nombreProveedor + "'";
            queryString += ",'" + nombreChequeTxt.getValue() + "'";
            queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow());
            queryString += ",''"; //serie documento
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += ",''"; //tipodoca
            queryString += ",''"; //doca
            queryString += "," + cuentaContable3Cbx.getValue(); //idcuentacontable
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(debe3Txt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(haber3Txt.getDoubleValueDoNotThrow()); // HABER
            queryString += "," + String.valueOf(debe3QTxt.getDoubleValueDoNotThrow()); //DEBE Q.
            queryString += "," + String.valueOf(haber3QTxt.getDoubleValueDoNotThrow()); //HABER Q
            queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow());
            queryString += ",'" + descripcionTxt.getValue() + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";

        }
        if (cuentaContable4Cbx.getValue() != null && (debe4Txt.getDoubleValueDoNotThrow() > 0 || haber4Txt.getDoubleValueDoNotThrow() > 0)) {
            queryString += ",(";
            queryString += empresaId;
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigo4Txt.getValue() + "'";
            queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorId;
            queryString += ",''";
            queryString += ",'" + nombreProveedor + "'";
            queryString += ",'" + nombreChequeTxt.getValue() + "'";
            queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow());
            queryString += ",''"; //serie documento
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += ",''"; //tipodoca
            queryString += ",''"; //doca
            queryString += "," + cuentaContable4Cbx.getValue(); //idcuentacontable
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(debe4Txt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(haber4Txt.getDoubleValueDoNotThrow()); // HABER
            queryString += "," + String.valueOf(debe4QTxt.getDoubleValueDoNotThrow()); //DEBE Q.
            queryString += "," + String.valueOf(haber4QTxt.getDoubleValueDoNotThrow()); //HABER Q
            queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow());
            queryString += ",'" + descripcionTxt.getValue() + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";

            System.out.println("cuarto query" + queryString);

        }
        if (cuentaContable5Cbx.getValue() != null && (debe5Txt.getDoubleValueDoNotThrow() > 0 || haber5Txt.getDoubleValueDoNotThrow() > 0)) {
            queryString += ",(";
            queryString += empresaId;
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",''";
            queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorId;
            queryString += ",''";
            queryString += ",'" + nombreProveedor + "'";
            queryString += ",'" + nombreChequeTxt.getValue() + "'";
            queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow());
            queryString += ",''"; //serie documento
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += ",''"; //tipodoca
            queryString += ",''"; //doca
            queryString += "," + cuentaContable5Cbx.getValue(); //idcuentacontable
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(debe5Txt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(haber5Txt.getDoubleValueDoNotThrow()); // HABER
            queryString += "," + String.valueOf(debe5QTxt.getDoubleValueDoNotThrow()); //DEBE Q.
            queryString += "," + String.valueOf(haber5QTxt.getDoubleValueDoNotThrow()); //HABER Q
            queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow());
            queryString += ",'" + descripcionTxt.getValue() + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";

        }

        if (cuentaContable6Cbx.getValue() != null && (debe6Txt.getDoubleValueDoNotThrow() > 0 || haber6Txt.getDoubleValueDoNotThrow() > 0)) {
            queryString += ",(";
            queryString += empresaId;
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",''";
            queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorId;
            queryString += ",''";
            queryString += ",'" + nombreProveedor + "'";
            queryString += ",'" + nombreChequeTxt.getValue() + "'";
            queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow());
            queryString += ",''"; //serie documento
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += ",''"; //tipodoca
            queryString += ",''"; //doca
            queryString += "," + cuentaContable6Cbx.getValue(); //idcuentacontable
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(debe6Txt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(haber6Txt.getDoubleValueDoNotThrow()); // HABER
            queryString += "," + String.valueOf(debe6QTxt.getDoubleValueDoNotThrow()); //DEBE Q.
            queryString += "," + String.valueOf(haber6QTxt.getDoubleValueDoNotThrow()); //HABER Q
            queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow());
            queryString += ",'" + descripcionTxt.getValue() + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";

        }

        if (cuentaContable7Cbx.getValue() != null && (debe7Txt.getDoubleValueDoNotThrow() > 0 || haber7Txt.getDoubleValueDoNotThrow() > 0)) {
            queryString += ",(";
            queryString += empresaId;
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",''";
            queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorId;
            queryString += ",''";
            queryString += ",'" + nombreProveedor + "'";
            queryString += ",'" + nombreChequeTxt.getValue() + "'";
            queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow());
            queryString += ",''"; //serie documento
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += ",''"; //tipodoca
            queryString += ",''"; //doca
            queryString += "," + cuentaContable7Cbx.getValue(); //idcuentacontable
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(debe7Txt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(haber7Txt.getDoubleValueDoNotThrow()); // HABER
            queryString += "," + String.valueOf(debe7QTxt.getDoubleValueDoNotThrow()); //DEBE Q.
            queryString += "," + String.valueOf(haber7QTxt.getDoubleValueDoNotThrow()); //HABER Q
            queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow());
            queryString += ",'" + descripcionTxt.getValue() + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";
        }

        try {

            ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().setAutoCommit(false);

            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();

            stQuery.executeUpdate(queryString);

            PagoChequesPDF Pagocheques
                    = new PagoChequesPDF(
                    empresaId,
                    empresaNombre,
                    codigoPartidaNuevo,
                    "0",
                    nombreChequeTxt.getValue(),
                    numeroTxt.getValue(),
                    descripcionTxt.getValue(),
                    numberFormat3.format(montoTxt.getDoubleValueDoNotThrow())
            );
            mainUI.addWindow(Pagocheques);
            Pagocheques.center();

            actualizarAnticiposAutorizados();

            ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().commit();
            ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().setAutoCommit(true);

            Notification notif = new Notification("PAGO REALIZADO  EXITOSAMENTE.",
                    Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.CHECK);
            notif.show(Page.getCurrent());

//            ((PagarView) (mainUI.getNavigator().getCurrentView())).documentosContainer.getContainerProperty(((PagarView) (mainUI.getNavigator().getCurrentView())).documentosGrid.getSelectedRow(), PagarView.ESTATUS_PROPERTY).setValue("PAGADO");
            ((PagarView) (mainUI.getNavigator().getCurrentView())).documentosContainer.removeItem((((PagarView) (mainUI.getNavigator().getCurrentView())).documentosGrid.getSelectedRow()));

        } catch (Exception ex1) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error al insertar transacción  : ", ex1);

            Notification notif = new Notification("HA OCURRIDO UN ERROR DE BASE DE DATOS : " + ex1.getMessage(),
                    Notification.Type.ERROR_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.WARNING);
            notif.show(Page.getCurrent());

            try {
                ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().rollback();
                ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().setAutoCommit(true);
            } catch (SQLException ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public void actualizarAnticiposAutorizados() {

        if (enganchesGrid.getSelectedRows() == null) {
            Notification.show("Por favor seleccione un registro de la tabla. ", Notification.Type.ERROR_MESSAGE);
            return;
        }

        try {
            Iterator iter;

            iter = enganchesGrid.getSelectedRows().iterator();
            String idAutorizacion = "";

            stQuery2 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            Object gridItem;

            while (iter.hasNext()) {

                gridItem = iter.next();

                idAutorizacion = String.valueOf(enganchesGrid.getContainerDataSource().getItem(gridItem).getItemProperty(NO_PROPERTY).getValue());

                queryString = "DELETE FROM autorizacion_pago";
                queryString += " WHERE IdAutorizacion = " + idAutorizacion;

                stQuery2.executeUpdate(queryString);

            }

            ((PagarView) (mainUI.getNavigator().getCurrentView())).documentosContainer.removeItem(((PagarView) (mainUI.getNavigator().getCurrentView())).documentosGrid.getSelectedRow());

            llenarTablaEnganches();
            limpiarPartida();

        } catch (SQLException ex) {
            ex.printStackTrace();
            Logger.getLogger(PagoDevolucionEngancheForm.class.getName()).log(Level.SEVERE, null, ex);

            Notification notif = new Notification("HA OCURRIDO UN ERROR DE BASE DE DATOS : " + ex.getMessage(),
                    Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.WARNING);
            notif.show(Page.getCurrent());

            try {
                ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().rollback();
                ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().setAutoCommit(true);
            } catch (SQLException ex1) {
                Logger.getLogger(PagoDevolucionEngancheForm.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
    }

    public void limpiarPartida() {
        montoTxt.setReadOnly(false);
        numeroTxt.setValue("");
        montoTxt.setValue(0.00);
        nombreChequeTxt.setReadOnly(false);
        nombreChequeTxt.setValue("");
        descripcionTxt.setValue("");

        nombreChequeTxt.setReadOnly(((SopdiUI) mainUI).sessionInformation.getStrUserProfileName().equals("CONTADOR"));
        nombreChequeTxt.setReadOnly(((SopdiUI) mainUI).sessionInformation.getStrUserProfileName().equals("AUXILIAR"));

        cuentaContable1Cbx.setReadOnly(false);
        cuentaContable1Cbx.clear();
        debe1Txt.setReadOnly(false);
        haber1Txt.setReadOnly(false);
        debe1Txt.setValue(0.00);
        haber1Txt.setValue(0.00);
        debe1QTxt.setReadOnly(false);
        haber1QTxt.setReadOnly(false);
        debe1QTxt.setValue(0.00);
        haber1QTxt.setValue(0.00);

        cuentaContable2Cbx.setReadOnly(false);
        cuentaContable2Cbx.clear();
        debe2Txt.setReadOnly(false);
        haber2Txt.setReadOnly(false);
        debe2Txt.setValue(0.00);
        haber2Txt.setValue(0.00);
        debe2QTxt.setReadOnly(false);
        haber2QTxt.setReadOnly(false);
        debe2QTxt.setValue(0.00);
        haber2QTxt.setValue(0.00);

        cuentaContable3Cbx.setReadOnly(false);
        cuentaContable3Cbx.clear();
        debe3Txt.setReadOnly(false);
        haber3Txt.setReadOnly(false);
        debe3Txt.setValue(0.00);
        haber3Txt.setValue(0.00);
        debe3QTxt.setReadOnly(false);
        haber3QTxt.setReadOnly(false);
        debe3QTxt.setValue(0.00);
        haber3QTxt.setValue(0.00);

        cuentaContable4Cbx.setReadOnly(false);
        cuentaContable4Cbx.clear();
        debe4Txt.setReadOnly(false);
        haber4Txt.setReadOnly(false);
        debe4Txt.setValue(0.00);
        haber4Txt.setValue(0.00);
        debe4QTxt.setReadOnly(false);
        haber4QTxt.setReadOnly(false);
        debe4QTxt.setValue(0.00);
        haber4QTxt.setValue(0.00);

        cuentaContable5Cbx.setReadOnly(false);
        cuentaContable5Cbx.clear();
        debe5Txt.setReadOnly(false);
        haber5Txt.setReadOnly(false);
        debe5Txt.setValue(0.00);
        haber5Txt.setValue(0.00);
        debe5QTxt.setReadOnly(false);
        haber5QTxt.setReadOnly(false);
        debe5QTxt.setValue(0.00);
        haber5QTxt.setValue(0.00);

        cuentaContable6Cbx.setReadOnly(false);
        cuentaContable6Cbx.clear();
        debe6Txt.setReadOnly(false);
        haber6Txt.setReadOnly(false);
        debe6Txt.setValue(0.00);
        haber6Txt.setValue(0.00);
        debe6QTxt.setReadOnly(false);
        haber6QTxt.setReadOnly(false);
        debe6QTxt.setValue(0.00);
        haber6QTxt.setValue(0.00);

        cuentaContable7Cbx.setReadOnly(false);
        cuentaContable7Cbx.clear();
        debe7Txt.setReadOnly(false);
        haber7Txt.setReadOnly(false);
        debe7Txt.setValue(0.00);
        haber7Txt.setValue(0.00);
        debe7QTxt.setReadOnly(false);
        haber7QTxt.setReadOnly(false);
        debe7QTxt.setValue(0.00);
        haber7QTxt.setValue(0.00);

        codigo1Txt.setValue("");
        codigo2Txt.setValue("");
        codigo3Txt.setValue("");
        codigo4Txt.setValue("");
        codigo5Txt.setValue("");
    }
}