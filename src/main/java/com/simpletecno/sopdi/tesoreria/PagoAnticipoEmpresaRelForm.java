package com.simpletecno.sopdi.tesoreria;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
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
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vaadin.ui.NumberField;

/**
 *
 * @author user
 */
public class PagoAnticipoEmpresaRelForm extends Window {

    VerticalLayout mainLayout;

    static final String NO = "No.";
    static final String ID_PROVEEDOR = "Cod Proveevor";
    static final String PROVEEDOR = "Proveedor";
    static final String CREADOFECHA = "Fecha";
    static final String CREADOPOR = "Creado por";
    static final String MONEDA = "Moneda";
    static final String MONTO = "Monto";
    static final String OBJETIVO = "OBJETIVO";
    static final String CUENTA = "CUENTA";

    public IndexedContainer container = new IndexedContainer();
    Grid empresaGrid;

    ComboBox proveedorCbx;
    ComboBox monedaCbx;
    ComboBox medioCbx;
    DateField fechaDt;
    NumberField montoTxt;
    NumberField tasaCambioTxt;
    TextField nombreChequeTxt;
    TextField descripcionTxt;
    TextField numeroTxt;

    NumberField debe1Txt;
    NumberField debe2Txt;
    NumberField debe3Txt;
    NumberField debe4Txt;
    NumberField debe5Txt;
    NumberField haber1Txt;
    NumberField haber2Txt;
    NumberField haber3Txt;
    NumberField haber4Txt;
    NumberField haber5Txt;

    ComboBox cuentaContable1Cbx;
    ComboBox cuentaContable2Cbx;
    ComboBox cuentaContable3Cbx;
    ComboBox cuentaContable4Cbx;
    ComboBox cuentaContable5Cbx;

    HorizontalLayout chequeLayout = new HorizontalLayout();
    HorizontalLayout chequeLayout2 = new HorizontalLayout();
    VerticalLayout partidaLayout = new VerticalLayout();

    String cuentaContable;

    Button grabarPartidaBtn;

    UI mainUI;
    Statement stQuery;
    ResultSet rsRecords;
    Statement stQuery1;
    Statement stQuery2;
    ResultSet rsRecords2;

    String queryString;

    String codigoPartidaNuevo;
    String codigoPartidaSeleccionado;
    
    Date fechaPago;

    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");
    static DecimalFormat numberFormat3 = new DecimalFormat("######0.00");

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public PagoAnticipoEmpresaRelForm(String codigoPartidaSeleccionado, Date fechaPago) {

        this.mainUI = UI.getCurrent();
        this.codigoPartidaSeleccionado = codigoPartidaSeleccionado;
        this.fechaPago  = fechaPago;

        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setResponsive(true);

        setContent(mainLayout);

        setResponsive(true);
        setWidth("90%");
        setHeight("90%");

        Label titleLbl = new Label("");
        titleLbl.setValue(empresaId + " " + empresaNombre + " TRASLADO DE FONDOS A EMPRESA RELACIONADA");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h2_custom");

        proveedorCbx = new ComboBox("Empresas : ");
        proveedorCbx.setWidth("35em");
        proveedorCbx.setVisible(false);
        proveedorCbx.setFilteringMode(FilteringMode.CONTAINS);
        proveedorCbx.addValueChangeListener(event -> {
            llenarTablaAnticipos();
            nombreChequeTxt.setReadOnly(false);
            nombreChequeTxt.setValue(proveedorCbx.getItemCaption(proveedorCbx.getValue()));
            nombreChequeTxt.setReadOnly(true);

        });

        llenarComboProveedor();

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

        llenarTablaAnticipos();

        crearLayoutCheque();

        crearPartidaLayout();

        if (container.size()>0){
            empresaGrid.select(container.firstItemId());
        }

    }

    public void llenarComboProveedor() {

        queryString = " SELECT IdProveedor, Nombre";
        queryString += " FROM proveedor_empresa ";
        queryString += " WHERE EsRelacionada = 1";
        queryString += " AND IdEmpresa = " + empresaId;
        queryString += " AND Inhabilitado = 0 ";

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

        container.addContainerProperty(NO, String.class, null);
        container.addContainerProperty(ID_PROVEEDOR, String.class, null);
        container.addContainerProperty(PROVEEDOR, String.class, null);
        container.addContainerProperty(CREADOFECHA, String.class, null);
        container.addContainerProperty(CREADOPOR, String.class, null);
        container.addContainerProperty(MONEDA, String.class, null);
        container.addContainerProperty(MONTO, String.class, null);
        container.addContainerProperty(OBJETIVO, String.class, null);
        container.addContainerProperty(CUENTA, String.class, null);

        empresaGrid = new Grid("Traslado de fondos a empresas relacionada", container);
        empresaGrid.setImmediate(true);
        empresaGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        empresaGrid.setDescription("Seleccione un registro.");
        empresaGrid.setHeightMode(HeightMode.ROW);
        empresaGrid.setHeightByRows(4);
        empresaGrid.setWidth("100%");
        empresaGrid.setResponsive(true);
        empresaGrid.setEditorBuffered(false);
        empresaGrid.addSelectionListener(new SelectionListener() {
            @Override
            public void select(SelectionEvent event) {

                if (empresaGrid.getSelectedRow() != null) {

                    llenarComboCuentaContable();

                    generarAnticipo();

                    monedaCbx.setReadOnly(false);
                    String mondeda = String.valueOf(container.getContainerProperty(empresaGrid.getSelectedRow(), MONEDA).getValue());
                    validadMoneda(mondeda);
                    monedaCbx.setReadOnly(true);

                    montoTxt.setReadOnly(false);
                    String monto = String.valueOf(container.getContainerProperty(empresaGrid.getSelectedRow(), MONTO).getValue()).replaceAll(",", "");
                    montoTxt.setValue(monto);
                    montoTxt.setReadOnly(true);

                    debe1Txt.setValue(montoTxt.getValue());
                    haber2Txt.setValue(montoTxt.getValue());

                    nombreChequeTxt.setValue(String.valueOf(container.getContainerProperty(empresaGrid.getSelectedRow(), PROVEEDOR).getValue()));

                }
            }
        });

        empresaGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if (MONTO.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }
        });

        reportLayout.addComponent(empresaGrid);
        reportLayout.setComponentAlignment(empresaGrid, Alignment.MIDDLE_CENTER);

        mainLayout.addComponent(reportLayout);
        mainLayout.setComponentAlignment(reportLayout, Alignment.TOP_CENTER);
    }

    public void validadMoneda(String moneda) {
        if (moneda.equals("DOLARES")) {
            tasaCambioTxt.setReadOnly(false);
            tasaCambioTxt.setValue((Float.toString(((SopdiUI) UI.getCurrent()).sessionInformation.getFltExchangeRate())));
            monedaCbx.select("DOLARES");
            cuentaContable2Cbx.setReadOnly(false);
            cuentaContable2Cbx.setValue(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaExtranjera());
            cuentaContable2Cbx.setReadOnly(true);
        }
        else {
            tasaCambioTxt.setReadOnly(false);
            tasaCambioTxt.setValue(1.00);
            tasaCambioTxt.setReadOnly(true);
            monedaCbx.select("QUETZALES");
            cuentaContable2Cbx.setReadOnly(false);
            cuentaContable2Cbx.setValue(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaLocal());
            cuentaContable2Cbx.setReadOnly(true);
        }
    }

    public void llenarTablaAnticipos() {
        container.removeAllItems();

        queryString = " SELECT autorizacion_pago.*, proveedor.Nombre ";
        queryString += " FROM autorizacion_pago  ";
        queryString += " INNER JOIN proveedor_empresa ON autorizacion_pago.IdProveedor = proveedor_empresa.IdProveedor";
        queryString += " WHERE autorizacion_pago.IdEmpresa = " + empresaId;
        queryString += " AND proveedor_empresa.IdEmpresa = " + empresaId;
        queryString += " AND IdAutorizacion = " + codigoPartidaSeleccionado;

//        System.out.println("query anticipos empresa relacionada" + queryString);

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);
            if (rsRecords.next()) { //  encontrado                                                
                do {
                    Object itemId = container.addItem();

                    container.getContainerProperty(itemId, NO).setValue(rsRecords.getString("IdAutorizacion"));
                    container.getContainerProperty(itemId, ID_PROVEEDOR).setValue(rsRecords.getString("IdProveedor"));
                    container.getContainerProperty(itemId, PROVEEDOR).setValue(rsRecords.getString("Nombre"));
                    container.getContainerProperty(itemId, CREADOFECHA).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha")));
                    container.getContainerProperty(itemId, CREADOPOR).setValue(rsRecords.getString("nombre"));
                    container.getContainerProperty(itemId, MONEDA).setValue(rsRecords.getString("Moneda"));
                    container.getContainerProperty(itemId, MONTO).setValue(numberFormat.format((rsRecords.getDouble("Monto"))));
                    container.getContainerProperty(itemId, OBJETIVO).setValue(rsRecords.getString("Objetivo"));
                    container.getContainerProperty(itemId, CUENTA).setValue(rsRecords.getString("CuentaContableLiquidar"));

                } while (rsRecords.next());
            }
        } catch (Exception ex) {
            System.out.println("Error al listar tabla llenarTablaAnticipos:" + ex);
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
        
        tasaCambioTxt = new NumberField("Tipo de Cambio : ");
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
        montoTxt.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(Property.ValueChangeEvent event) {

                debe1Txt.setReadOnly(false);
                debe1Txt.setValue(montoTxt.getDoubleValueDoNotThrow());
                haber2Txt.setReadOnly(false);
                haber2Txt.setValue(montoTxt.getDoubleValueDoNotThrow());
            }

        });

        fechaDt = new DateField("Fecha : ");
        fechaDt.setDateFormat("dd/MM/yyyy");
        fechaDt.setWidth("9em");
        //fechaDt.setValue(new java.util.Date());
        fechaDt.setValue(fechaPago);
        fechaDt.setReadOnly(true);

        nombreChequeTxt = new TextField("Nombre cheque/transferencia : ");
        nombreChequeTxt.setWidth("20em");
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
//        layoutHorizontal6.setMargin(new MarginInfo(true,false,false,false));
        layoutHorizontal6.setMargin(false);
        layoutHorizontal6.setWidth("90%");

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

        grabarPartidaBtn = new Button("Grabar");
        grabarPartidaBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        grabarPartidaBtn.setIcon(FontAwesome.SAVE);
        grabarPartidaBtn.addClickListener((event) -> {
            insertarPartidaSimple();
        });

        Button cancelarBtn = new Button("Cancelar");
        cancelarBtn.addStyleName(ValoTheme.BUTTON_DANGER);
        cancelarBtn.setIcon(FontAwesome.TRASH);
        cancelarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                proveedorCbx.setReadOnly(false);
                limpiarPartida();
                proveedorCbx.setReadOnly(true);
                proveedorCbx.focus();
            }
        });

        layoutHorizontal1.addComponent(cuentaContable1Cbx);
        layoutHorizontal1.addComponent(debe1Txt);
        layoutHorizontal1.addComponent(haber1Txt);

        layoutHorizontal2.addComponent(cuentaContable2Cbx);
        layoutHorizontal2.addComponent(debe2Txt);
        layoutHorizontal2.addComponent(haber2Txt);

        layoutHorizontal3.addComponent(cuentaContable3Cbx);
        layoutHorizontal3.addComponent(debe3Txt);
        layoutHorizontal3.addComponent(haber3Txt);

        layoutHorizontal4.addComponent(cuentaContable4Cbx);
        layoutHorizontal4.addComponent(debe4Txt);
        layoutHorizontal4.addComponent(haber4Txt);

        layoutHorizontal5.addComponent(cuentaContable5Cbx);
        layoutHorizontal5.addComponent(debe5Txt);
        layoutHorizontal5.addComponent(haber5Txt);

        layoutHorizontal6.addComponent(cancelarBtn);
        layoutHorizontal6.setComponentAlignment(cancelarBtn, Alignment.BOTTOM_LEFT);
        layoutHorizontal6.addComponent(grabarPartidaBtn);
        layoutHorizontal6.setComponentAlignment(grabarPartidaBtn, Alignment.BOTTOM_RIGHT);

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

        mainLayout.addComponent(partidaLayout);
        mainLayout.setComponentAlignment(partidaLayout, Alignment.MIDDLE_CENTER);

    }

    public void llenarComboCuentaContable() {
        if(empresaGrid.getSelectedRow() == null) {
            return;
        }

        String queryString = " SELECT * FROM contabilidad_nomenclatura_empresa ";
        queryString += " WHERE Estatus='HABILITADA'";
        queryString += " AND IdEmpresa=" + empresaGrid.getSelectedRow();
        queryString += " ORDER BY N5";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) { //  encontrado
                if (rsRecords.getString("NoCuenta").equals(String.valueOf(container.getContainerProperty(empresaGrid.getSelectedRow(), CUENTA).getValue()))) {
                    cuentaContable = rsRecords.getString("IdNomenclatura");
                }

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
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar cuentas contables: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void generarAnticipo() {

        cuentaContable1Cbx.setReadOnly(false);

        cuentaContable1Cbx.select(cuentaContable);

        debe1Txt.setReadOnly(false);
        debe1Txt.setValue(montoTxt.getDoubleValueDoNotThrow());

        haber1Txt.setReadOnly(true);

        cuentaContable2Cbx.setReadOnly(false);
        cuentaContable2Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaLocal());

        debe2Txt.setReadOnly(true);
        haber2Txt.setReadOnly(false);
        haber2Txt.setValue(montoTxt.getDoubleValueDoNotThrow());

        cuentaContable3Cbx.setReadOnly(true);

        debe3Txt.setReadOnly(true);
        haber3Txt.setReadOnly(true);

        cuentaContable4Cbx.setReadOnly(true);

        debe4Txt.setReadOnly(true);
        haber4Txt.setReadOnly(true);

        cuentaContable5Cbx.setReadOnly(true);

        debe5Txt.setReadOnly(true);
        haber5Txt.setReadOnly(true);

        proveedorCbx.setReadOnly(false);

    }

    public void insertarPartidaSimple() {

        if (empresaGrid.getSelectedRow() == null) {
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

        double haber = (haber2Txt.getDoubleValueDoNotThrow() + haber3Txt.getDoubleValueDoNotThrow());

        if (haber != montoTxt.getDoubleValueDoNotThrow()) {
            Notification.show("El total del haber no coincide con el monto..", Notification.Type.WARNING_MESSAGE);
            cuentaContable2Cbx.focus();
            return;
        }

        if (debe1Txt.getDoubleValueDoNotThrow() != montoTxt.getDoubleValueDoNotThrow()) {
            Notification.show("El monto del debe esta descuadrado por favor revisarlo", Notification.Type.WARNING_MESSAGE);
            debe1Txt.focus();
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
        if (debe1Txt.getDoubleValueDoNotThrow() == 0.00) {
            Notification.show("Monto del DEBE no es correcto. ", Notification.Type.ERROR_MESSAGE);
            debe1Txt.focus();
            return;
        }
        if (haber2Txt.getDoubleValueDoNotThrow() == 0.00) {
            Notification.show("Monto del HABER no es correcto. ", Notification.Type.ERROR_MESSAGE);
            haber2Txt.focus();;
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

                codigoPartida += String.format("%03d", (Integer.valueOf(ultimoEncontado) + 1));

            } else {
                codigoPartida += "001";
            }

        } catch (Exception ex1) {
            System.out.println("Error al buscar siguiente correlativo de partida" + ex1.getMessage());
            ex1.printStackTrace();
        }

        codigoPartidaNuevo = codigoPartida;

        String descripcion = "";

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
        queryString += ",'" + codigoPartida + "'";
        queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
        queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
        queryString += "," + String.valueOf(container.getContainerProperty(empresaGrid.getSelectedRow(), ID_PROVEEDOR).getValue());
        queryString += ",''";//nitproveedor
        queryString += ",'" + String.valueOf(container.getContainerProperty(empresaGrid.getSelectedRow(), PROVEEDOR).getValue()) + "'";
        queryString += ",'" + nombreChequeTxt.getValue() + "'";
        queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow());
        queryString += ",''";  //serie documento
        queryString += ",'" + numeroTxt.getValue() + "'";
        queryString += ",''"; //tipodoca
        queryString += ",''"; //doca
        queryString += "," + String.valueOf(cuentaContable1Cbx.getValue()); //idcuentacontable
        descripcion = " TRASLADO DE FONDOS " + nombreChequeTxt.getValue() + " " + descripcionTxt.getValue().trim();
        descripcion += " " + descripcionTxt.getValue().trim();
        queryString += ",'" + monedaCbx.getValue() + "'";
        queryString += "," + String.valueOf(debe1Txt.getDoubleValueDoNotThrow()); //DEBE
        queryString += ",0.00"; //HABER
        queryString += "," + String.valueOf(debe1Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow()); //DEBE Q
        queryString += ",0.00"; //HABER Q.
        queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
        queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow());
        queryString += ",'" + descripcion + "'";
        queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
        queryString += ",current_timestamp";
        queryString += ")";

        if (cuentaContable2Cbx.getValue() != null && haber2Txt.getDoubleValueDoNotThrow() != 0) {
            queryString += ",(";
            queryString += empresaId;
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + String.valueOf(container.getContainerProperty(empresaGrid.getSelectedRow(), ID_PROVEEDOR).getValue());
            queryString += ",''"; //nitproveedor
            queryString += ",'" + String.valueOf(container.getContainerProperty(empresaGrid.getSelectedRow(), PROVEEDOR).getValue()) + "'";
            queryString += ",'" + nombreChequeTxt.getValue() + "'";
            queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow());
            queryString += ",''"; //serie documento
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += ",''"; //tipodoca
            queryString += ",''"; //doca
            queryString += "," + cuentaContable2Cbx.getValue(); //idcuentacontable
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += ",0.00"; // DEBE
            queryString += "," + String.valueOf(haber2Txt.getDoubleValueDoNotThrow()); // HABER
            queryString += ",0.00"; //DEBE Q.
            queryString += "," + String.valueOf(haber2Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow()); //HABER Q
            queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow());
            queryString += ",'" + descripcion + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";

        }

        if (cuentaContable3Cbx.getValue() != null && haber3Txt.getDoubleValueDoNotThrow() != 0) {
            queryString += ",(";
            queryString += empresaId;
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + String.valueOf(container.getContainerProperty(empresaGrid.getSelectedRow(), ID_PROVEEDOR).getValue());
            queryString += ",''";
            queryString += ",'" + String.valueOf(container.getContainerProperty(empresaGrid.getSelectedRow(), PROVEEDOR).getValue()) + "'";
            queryString += ",'" + nombreChequeTxt.getValue() + "'";
            queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow());
            queryString += ",''"; //serie documento
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += ",''"; //tipodoca
            queryString += ",''"; //doca
            queryString += "," + cuentaContable3Cbx.getValue(); //idcuentacontable
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += ",0.00"; // DEBE
            queryString += "," + String.valueOf(haber3Txt.getDoubleValueDoNotThrow()); // HABER
            queryString += ",0.00"; //DEBE Q.
            queryString += "," + String.valueOf(haber3Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow()); //HABER Q
            queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow());
            queryString += ",'" + descripcion + "'";
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
                    descripcion,
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

            ((PagarView) (mainUI.getNavigator().getCurrentView())).documentosContainer.removeItem(((PagarView) (mainUI.getNavigator().getCurrentView())).documentosGrid.getSelectedRow());

//            ((PagarView) (mainUI.getNavigator().getCurrentView())).documentosContainer.getContainerProperty(((PagarView) (mainUI.getNavigator().getCurrentView())).documentosGrid.getSelectedRow(), PagarView.ESTATUS_PROPERTY).setValue("PAGADO");

        } catch (Exception ex1) {
            System.out.println("Error al insertar transacción  : " + ex1.getMessage());
            ex1.printStackTrace();

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
                Logger.getLogger(PagoAnticipoEmpresaRelForm.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public void actualizarAnticiposAutorizados() {

        if (empresaGrid.getSelectedRow() == null) {
            Notification.show("Por favor seleccione un registro de la tabla. ", Notification.Type.ERROR_MESSAGE);
            return;
        }

        try {

            queryString = "DELETE FROM autorizacion_pago";
            queryString += " WHERE IdAutorizacion = " + String.valueOf(container.getContainerProperty(empresaGrid.getSelectedRow(), NO).getValue());;

            stQuery2 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery2.executeUpdate(queryString);

            llenarTablaAnticipos();
            limpiarPartida();

        } catch (SQLException ex) {
            ex.printStackTrace();
            Logger.getLogger(PagoAnticipoEmpresaRelForm.class.getName()).log(Level.SEVERE, null, ex);
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
                Logger.getLogger(PagoAnticipoEmpresaRelForm.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
    }

    public void limpiarPartida() {
       
        montoTxt.setReadOnly(false);
        numeroTxt.setValue("");
        montoTxt.setValue(0.00);
        nombreChequeTxt.setValue("");
        descripcionTxt.setValue("");

        nombreChequeTxt.setReadOnly(((SopdiUI) mainUI).sessionInformation.getStrUserProfileName().equals("CONTADOR"));
        nombreChequeTxt.setReadOnly(((SopdiUI) mainUI).sessionInformation.getStrUserProfileName().equals("AUXILIAR"));

        cuentaContable1Cbx.setReadOnly(false);
        haber1Txt.setReadOnly(true);
        debe1Txt.setReadOnly(false);
        debe1Txt.setValue(0.00);

        cuentaContable2Cbx.setReadOnly(false);
        haber2Txt.setReadOnly(false);
        debe2Txt.setReadOnly(true);
        haber2Txt.setValue(0.00);
    }

}