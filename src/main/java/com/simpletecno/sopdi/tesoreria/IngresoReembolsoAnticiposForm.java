/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.tesoreria;

import com.simpletecno.sopdi.utilerias.MyEmailMessanger;
import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.simpletecno.sopdi.utilerias.ValidarTokenForm;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.SelectionEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.ui.NumberField;

import javax.mail.MessagingException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author user
 */
public class IngresoReembolsoAnticiposForm extends Window {

    static final String NIT_PROPERTY = "NIT";
    static final String NOMBRESINCODIGO_PROPERTY = "NSC";

    UI mainUI;
    Statement stQuery, stQueryAnticipos;
    ResultSet rsRecords, rsRecorfsAnticipos;

    String queryString;

    VerticalLayout mainLayout;
    HorizontalLayout layoutTitle;
    Label titleLbl;
    ComboBox empresaCbx;

    ComboBox tipoIngresoCbx;
    ComboBox proveedorCbx;
    ComboBox medioCbx;
    DateField fechaDt;
    TextField numeroTxt;
    NumberField montoTxt;
    ComboBox monedaCbx;
    NumberField tipoCambioTxt;
    TextField descripcionTxt;

    ComboBox cuentaContable1Cbx;
    ComboBox cuentaContable2Cbx;
    ComboBox cuentaContable3Cbx;
    ComboBox cuentaContable4Cbx;
    ComboBox cuentaContable5Cbx;
    ComboBox cuentaContable6Cbx;
    ComboBox cuentaContable7Cbx;
    ComboBox cuentaContable8Cbx;
    ComboBox cuentaContable9Cbx;
    ComboBox cuentaContable10Cbx;

    NumberField debe1Txt;
    NumberField debe2Txt;
    NumberField debe3Txt;
    NumberField debe4Txt;
    NumberField debe5Txt;
    NumberField debe6Txt;
    NumberField debe7Txt;
    NumberField debe8Txt;
    NumberField debe9Txt;
    NumberField debe10Txt;

    NumberField debe1QTxt;
    NumberField debe2QTxt;
    NumberField debe3QTxt;
    NumberField debe4QTxt;
    NumberField debe5QTxt;
    NumberField debe6QTxt;
    NumberField debe7QTxt;
    NumberField debe8QTxt;
    NumberField debe9QTxt;
    NumberField debe10QTxt;

    NumberField haber1Txt;
    NumberField haber2Txt;
    NumberField haber3Txt;
    NumberField haber4Txt;
    NumberField haber5Txt;
    NumberField haber6Txt;
    NumberField haber7Txt;
    NumberField haber8Txt;
    NumberField haber9Txt;
    NumberField haber10Txt;

    NumberField haber1QTxt;
    NumberField haber2QTxt;
    NumberField haber3QTxt;
    NumberField haber4QTxt;
    NumberField haber5QTxt;
    NumberField haber6QTxt;
    NumberField haber7QTxt;
    NumberField haber8QTxt;
    NumberField haber9QTxt;
    NumberField haber10QTxt;

    TextField codigo1Txt;
    TextField codigo2Txt;
    TextField codigo3Txt;
    TextField codigo4Txt;
    TextField codigo5Txt;
    TextField codigo6Txt;
    TextField codigo7Txt;
    TextField codigo8Txt;
    TextField codigo9Txt;
    TextField codigo10Txt;

    String codigoPartida;
    String variableTemp = "";

    public IndexedContainer anticiposContainer = new IndexedContainer();
    Grid anticiposGrid;
    static final String CODIGO_PARTIDA_PROPERTY = "Codigo Partida";
    static final String CODIGO_CC_PROPERTY = "Codigo CC";
    static final String NUMERO_DOCUMENTO_PROPERTY = "# Documento";
    static final String MONEDA_DOCUMENTO_PROPERTY = "Moneda";
    static final String DEBE_PROPERTY = "Debe";
    static final String HABER_PROPERTY = "Haber";
    static final String DEBE_QUETZALES_PROPERTY = "Debe Q.";
    static final String HABER_QUETZALES_PROPERTY = "Haber Q.";
    static final String SALDO_DOCUMENTO_PROPERTY = "Saldo";

    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");
    static DecimalFormat numberFormat3 = new DecimalFormat("######0.00");

    Button guardarBtn;
    Button salirBtn;

    BigDecimal totalDebe;
    BigDecimal totalHaber;

    public IngresoReembolsoAnticiposForm(String codigoPartida) {
        this.codigoPartida = codigoPartida;

        this.mainUI = UI.getCurrent();
        setResponsive(true);
        setWidth("87%");
        setHeight("85%");
        setModal(true);

        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setResponsive(true);

        layoutTitle = new HorizontalLayout();
        layoutTitle.setSpacing(true);
        layoutTitle.setMargin(true);
        layoutTitle.setWidth("100%");

        empresaCbx = new ComboBox("EMPRESA :");
        empresaCbx.setStyleName(ValoTheme.COMBOBOX_HUGE);
        empresaCbx.setWidth("90%");
        empresaCbx.setInvalidAllowed(false);
        empresaCbx.setNewItemsAllowed(false);
        empresaCbx.setTextInputAllowed(false);
        empresaCbx.setNullSelectionAllowed(false);
        llenarComboEmpresa();

        titleLbl = new Label("INGRESO A BANCOS REEMBOLSO ANTICIPO DE PROV.");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h2_custom");

        layoutTitle.addComponent(empresaCbx);
        layoutTitle.setComponentAlignment(empresaCbx, Alignment.MIDDLE_LEFT);

        layoutTitle.addComponent(titleLbl);
        layoutTitle.setComponentAlignment(titleLbl, Alignment.BOTTOM_RIGHT);

        mainLayout.addComponent(layoutTitle);
        setContent(mainLayout);
        mainLayout.addComponent(crearComponentes());

        generarPartidaReembolsoAnticipos();
    }

    public HorizontalLayout crearComponentes() {

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.addStyleName("rcorners3");
        horizontalLayout.setSpacing(true);
        horizontalLayout.setMargin(true);
        horizontalLayout.setWidth("100%");

        VerticalLayout leftVerticalLayout = new VerticalLayout();
        leftVerticalLayout.setWidth("100%");
        leftVerticalLayout.setSpacing(true);
        leftVerticalLayout.setMargin(true);

        VerticalLayout rightVerticalLayout = new VerticalLayout();
        rightVerticalLayout.setWidth("100%");
        rightVerticalLayout.setSpacing(true);
        rightVerticalLayout.setMargin(true);

        horizontalLayout.addComponents(leftVerticalLayout, rightVerticalLayout);
        horizontalLayout.setComponentAlignment(leftVerticalLayout, Alignment.MIDDLE_LEFT);
        horizontalLayout.setComponentAlignment(leftVerticalLayout, Alignment.MIDDLE_RIGHT);

        tipoIngresoCbx = new ComboBox("Tipo de Ingreso :");
        tipoIngresoCbx.setWidth("100%");
        tipoIngresoCbx.setFilteringMode(FilteringMode.CONTAINS);
        tipoIngresoCbx.setInvalidAllowed(false);
        tipoIngresoCbx.setNewItemsAllowed(false);
        tipoIngresoCbx.setNullSelectionAllowed(false);
        tipoIngresoCbx.addItem("REEMBOLSO DE ANTICIPOS");
        tipoIngresoCbx.select("REEMBOLSO DE ANTICIPOS");
        tipoIngresoCbx.setReadOnly(true);

        proveedorCbx = new ComboBox("Proveedor");
        proveedorCbx.addContainerProperty(NIT_PROPERTY, String.class, "");
        proveedorCbx.addContainerProperty(NOMBRESINCODIGO_PROPERTY, String.class, "");
        proveedorCbx.setInvalidAllowed(false);
        proveedorCbx.setNewItemsAllowed(false);
        proveedorCbx.setNullSelectionAllowed(false);
        proveedorCbx.setWidth("100%");
        proveedorCbx.setFilteringMode(FilteringMode.CONTAINS);
        proveedorCbx.addValueChangeListener((event) -> {
            if (proveedorCbx.getValue() != null || proveedorCbx.getValue().equals("0")) {
                llenarTablaAnticiposProveedor();
            }
        });

        medioCbx = new ComboBox("Medio : ");
        medioCbx.setWidth("100%");
        medioCbx.addItem("DEPOSITO");
        medioCbx.addItem("NOTA DE CREDITO");
        medioCbx.setInvalidAllowed(false);
        medioCbx.setNewItemsAllowed(false);
        medioCbx.setNullSelectionAllowed(false);
        medioCbx.select("DEPOSITO");

        fechaDt = new DateField("Fecha : ");
        fechaDt.setDateFormat("dd/MM/yyyy");
        fechaDt.setWidth("100%");
        fechaDt.setValue(new java.util.Date());

        numeroTxt = new TextField("# Depósito/Nota de crédito:");
        numeroTxt.setWidth("100%");

        montoTxt = new NumberField("Monto : ");
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

        monedaCbx = new ComboBox("Moneda :");
        monedaCbx.setWidth("100%");
        monedaCbx.addItem("QUETZALES");
        monedaCbx.addItem("DOLARES");
        monedaCbx.select("QUETZALES");
        monedaCbx.setInvalidAllowed(false);
        monedaCbx.setNewItemsAllowed(false);
        monedaCbx.setNullSelectionAllowed(false);
        monedaCbx.addValueChangeListener((event) -> {
            if (cuentaContable1Cbx.getValue() != null) {
                if (monedaCbx.getValue().equals("DOLARES")) {
                    tipoCambioTxt.setReadOnly(false);
                    tipoCambioTxt.setValue(Float.toString(((SopdiUI) UI.getCurrent()).sessionInformation.getFltExchangeRate()));
                    cuentaContable1Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaExtranjera());
                } else {
                    cuentaContable1Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaLocal());
                    tipoCambioTxt.setReadOnly(false);
                    tipoCambioTxt.setValue(1.00);
                    tipoCambioTxt.setReadOnly(true);
                }
            }
        });

        tipoCambioTxt = new NumberField("Tipo de Cambio :");
        tipoCambioTxt.setDecimalAllowed(true);
        tipoCambioTxt.setDecimalPrecision(5);
        tipoCambioTxt.setMinimumFractionDigits(5);
        tipoCambioTxt.setDecimalSeparator('.');
        tipoCambioTxt.setDecimalSeparatorAlwaysShown(true);
        tipoCambioTxt.setImmediate(true);
        tipoCambioTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        tipoCambioTxt.setWidth("100%");
        tipoCambioTxt.setValue(1.00);
        tipoCambioTxt.setReadOnly(true);

        descripcionTxt = new TextField("Descripción :");
        descripcionTxt.setWidth("100%");
        descripcionTxt.setVisible(false);

        leftVerticalLayout.addComponent(tipoIngresoCbx);
        leftVerticalLayout.addComponent(proveedorCbx);
        leftVerticalLayout.addComponent(medioCbx);
        leftVerticalLayout.addComponent(fechaDt);
        leftVerticalLayout.addComponent(numeroTxt);
        leftVerticalLayout.addComponent(montoTxt);
        leftVerticalLayout.addComponent(monedaCbx);
        leftVerticalLayout.addComponent(tipoCambioTxt);
        leftVerticalLayout.addComponent(descripcionTxt);

        HorizontalLayout layoutHorizontal1 = new HorizontalLayout();
        layoutHorizontal1.setResponsive(true);
        layoutHorizontal1.setSpacing(true);

        HorizontalLayout layoutHorizontal2 = new HorizontalLayout();
        layoutHorizontal2.setResponsive(true);
        layoutHorizontal2.setSpacing(true);

        HorizontalLayout layoutHorizontal3 = new HorizontalLayout();
        layoutHorizontal3.setResponsive(true);
        layoutHorizontal3.setSpacing(true);

        HorizontalLayout layoutHorizontal4 = new HorizontalLayout();
        layoutHorizontal4.setResponsive(true);
        layoutHorizontal4.setSpacing(true);

        HorizontalLayout layoutHorizontal5 = new HorizontalLayout();
        layoutHorizontal5.setResponsive(true);
        layoutHorizontal5.setSpacing(true);

        HorizontalLayout layoutHorizontal6 = new HorizontalLayout();
        layoutHorizontal6.setResponsive(true);
        layoutHorizontal6.setSpacing(true);

        HorizontalLayout layoutHorizontal7 = new HorizontalLayout();
        layoutHorizontal7.setResponsive(true);
        layoutHorizontal7.setSpacing(true);

        HorizontalLayout layoutHorizontal8 = new HorizontalLayout();
        layoutHorizontal8.setResponsive(true);
        layoutHorizontal8.setSpacing(true);

        HorizontalLayout layoutHorizontal9 = new HorizontalLayout();
        layoutHorizontal9.setResponsive(true);
        layoutHorizontal9.setSpacing(true);

        HorizontalLayout layoutHorizontal10 = new HorizontalLayout();
        layoutHorizontal10.setResponsive(true);
        layoutHorizontal10.setSpacing(true);

        HorizontalLayout layoutHorizontal11 = new HorizontalLayout();
        layoutHorizontal11.setResponsive(true);
        layoutHorizontal11.setSpacing(true);

        anticiposContainer.addContainerProperty(CODIGO_PARTIDA_PROPERTY, String.class, null);
        anticiposContainer.addContainerProperty(CODIGO_CC_PROPERTY, String.class, null);
        anticiposContainer.addContainerProperty(NUMERO_DOCUMENTO_PROPERTY, String.class, null);
        anticiposContainer.addContainerProperty(MONEDA_DOCUMENTO_PROPERTY, String.class, null);
        anticiposContainer.addContainerProperty(DEBE_PROPERTY, String.class, null);
        anticiposContainer.addContainerProperty(HABER_PROPERTY, String.class, null);
        anticiposContainer.addContainerProperty(DEBE_QUETZALES_PROPERTY, String.class, null);
        anticiposContainer.addContainerProperty(HABER_QUETZALES_PROPERTY, String.class, null);
        anticiposContainer.addContainerProperty(SALDO_DOCUMENTO_PROPERTY, String.class, null);

        anticiposGrid = new Grid("", anticiposContainer);
        anticiposGrid.setWidth("100%");
        anticiposGrid.setImmediate(true);
        anticiposGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        anticiposGrid.setDescription("Seleccione uno o varios registros del mismo proveedor.");
        anticiposGrid.setHeightMode(HeightMode.ROW);
        anticiposGrid.setHeightByRows(5);
        anticiposGrid.setResponsive(true);
        anticiposGrid.setEditorBuffered(false);

        anticiposGrid.addSelectionListener(new SelectionEvent.SelectionListener() {
            @Override
            public void select(SelectionEvent event) {
                if (anticiposGrid.getSelectedRows() != null) {

                    Object gridItem;
                    String moneda = "";
                    Double montoBancos = 0.00;

                    Iterator iter = event.getSelected().iterator();

                    if (iter == null) {
                        limpiarPartida();
                        return;

                    }
                    if (!iter.hasNext()) {
                        limpiarPartida();
                        return;
                    }

                    gridItem = iter.next();
                    moneda = String.valueOf(anticiposGrid.getContainerDataSource().getItem(gridItem).getItemProperty(MONEDA_DOCUMENTO_PROPERTY).getValue());
                    montoBancos = Double.valueOf(String.valueOf(anticiposGrid.getContainerDataSource().getItem(gridItem).getItemProperty(SALDO_DOCUMENTO_PROPERTY).getValue()));
                    limpiarPartida();

                    while (iter.hasNext()) {

                        if (!moneda.equals(String.valueOf(anticiposGrid.getContainerDataSource().getItem(gridItem).getItemProperty(MONEDA_DOCUMENTO_PROPERTY).getValue()))) {
                            Notification.show("SOLO SE PUEDEN PAGAR VARIAS FACTURAS DEL MISMO PROVEEDOR Y DE LA MISMA MONEDA, REVISE!", Notification.Type.WARNING_MESSAGE);
                            anticiposGrid.deselect(gridItem);
                            return;
                        }
                        gridItem = iter.next();
                        montoBancos += Double.valueOf(String.valueOf(anticiposGrid.getContainerDataSource().getItem(gridItem).getItemProperty(SALDO_DOCUMENTO_PROPERTY).getValue()));

                    }
                    limpiarPartida();

                    if (monedaCbx.getValue() == "DOLARES") {
                        cuentaContable1Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaExtranjera());
                        debe1Txt.setValue(montoBancos);
                    } else {
                        cuentaContable1Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaLocal());
                        debe1Txt.setValue(montoBancos);
                    }

                    Iterator iter2 = anticiposGrid.getSelectedRows().iterator();

                    while (iter2.hasNext()) {

                        Object gridItem2 = iter2.next();
                        if (cuentaContable2Cbx.getValue() == null) {
                            cuentaContable2Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getAnticiposProveedor());
                            haber2Txt.setValue(String.valueOf(anticiposGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(SALDO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", ""));
                            codigo2Txt.setValue(String.valueOf(anticiposGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(CODIGO_CC_PROPERTY).getValue()));
                        } else if (cuentaContable3Cbx.getValue() == null) {
                            cuentaContable3Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getAnticiposProveedor());
                            haber3Txt.setValue(String.valueOf(anticiposGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(SALDO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", ""));
                            codigo3Txt.setValue(String.valueOf(anticiposGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(CODIGO_CC_PROPERTY).getValue()));
                        } else if (cuentaContable4Cbx.getValue() == null) {
                            cuentaContable4Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getAnticiposProveedor());
                            haber4Txt.setValue(String.valueOf(anticiposGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(SALDO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", ""));
                            codigo4Txt.setValue(String.valueOf(anticiposGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(CODIGO_CC_PROPERTY).getValue()));
                        } else if (cuentaContable5Cbx.getValue() == null) {
                            cuentaContable5Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getAnticiposProveedor());
                            haber5Txt.setValue(String.valueOf(anticiposGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(SALDO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", ""));
                            codigo5Txt.setValue(String.valueOf(anticiposGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(CODIGO_CC_PROPERTY).getValue()));
                        } else if (cuentaContable6Cbx.getValue() == null) {
                            cuentaContable6Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getAnticiposProveedor());
                            haber6Txt.setValue(String.valueOf(anticiposGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(SALDO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", ""));
                            codigo6Txt.setValue(String.valueOf(anticiposGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(CODIGO_CC_PROPERTY).getValue()));
                        } else if (cuentaContable7Cbx.getValue() == null) {
                            cuentaContable7Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getAnticiposProveedor());
                            haber7Txt.setValue(String.valueOf(anticiposGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(SALDO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", ""));
                            codigo7Txt.setValue(String.valueOf(anticiposGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(CODIGO_CC_PROPERTY).getValue()));
                        } else if (cuentaContable8Cbx.getValue() == null) {
                            cuentaContable8Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getAnticiposProveedor());
                            haber8Txt.setValue(String.valueOf(anticiposGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(SALDO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", ""));
                            codigo8Txt.setValue(String.valueOf(anticiposGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(CODIGO_CC_PROPERTY).getValue()));
                        } else if (cuentaContable9Cbx.getValue() == null) {
                            cuentaContable9Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getAnticiposProveedor());
                            haber9Txt.setValue(String.valueOf(anticiposGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(SALDO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", ""));
                            codigo9Txt.setValue(String.valueOf(anticiposGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(CODIGO_CC_PROPERTY).getValue()));
                        } else if (cuentaContable10Cbx.getValue() == null) {
                            cuentaContable10Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getAnticiposProveedor());
                            haber10Txt.setValue(String.valueOf(anticiposGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(SALDO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", ""));
                            codigo10Txt.setValue(String.valueOf(anticiposGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(CODIGO_CC_PROPERTY).getValue()));
                        }
                    }
                }
            }
        });

        anticiposGrid.getColumn(CODIGO_PARTIDA_PROPERTY).setHidable(true).setHidden(true);
        anticiposGrid.getColumn(CODIGO_CC_PROPERTY).setHidable(true).setHidden(true);
        anticiposGrid.getColumn(DEBE_QUETZALES_PROPERTY).setHidable(true).setHidden(true);
        anticiposGrid.getColumn(HABER_QUETZALES_PROPERTY).setHidable(true).setHidden(true);

        cuentaContable1Cbx = new ComboBox("Cuentas contables :");
        cuentaContable1Cbx.setWidth("24em");
        cuentaContable1Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable1Cbx.setInvalidAllowed(false);
        cuentaContable1Cbx.setNewItemsAllowed(false);

        cuentaContable2Cbx = new ComboBox();
        cuentaContable2Cbx.setWidth("24em");
        cuentaContable2Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable2Cbx.setInvalidAllowed(false);
        cuentaContable2Cbx.setNewItemsAllowed(false);

        cuentaContable3Cbx = new ComboBox();
        cuentaContable3Cbx.setWidth("24em");
        cuentaContable3Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable3Cbx.setInvalidAllowed(false);
        cuentaContable3Cbx.setNewItemsAllowed(false);

        cuentaContable4Cbx = new ComboBox();
        cuentaContable4Cbx.setWidth("24em");
        cuentaContable4Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable4Cbx.setInvalidAllowed(false);
        cuentaContable4Cbx.setNewItemsAllowed(false);

        cuentaContable5Cbx = new ComboBox();
        cuentaContable5Cbx.setWidth("24em");
        cuentaContable5Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable5Cbx.setInvalidAllowed(false);
        cuentaContable5Cbx.setNewItemsAllowed(false);

        cuentaContable6Cbx = new ComboBox();
        cuentaContable6Cbx.setWidth("24em");
        cuentaContable6Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable6Cbx.setInvalidAllowed(false);
        cuentaContable6Cbx.setNewItemsAllowed(false);

        cuentaContable7Cbx = new ComboBox();
        cuentaContable7Cbx.setWidth("24em");
        cuentaContable7Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable7Cbx.setInvalidAllowed(false);
        cuentaContable7Cbx.setNewItemsAllowed(false);

        cuentaContable8Cbx = new ComboBox();
        cuentaContable8Cbx.setWidth("24em");
        cuentaContable8Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable8Cbx.setInvalidAllowed(false);
        cuentaContable8Cbx.setNewItemsAllowed(false);

        cuentaContable9Cbx = new ComboBox();
        cuentaContable9Cbx.setWidth("24em");
        cuentaContable9Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable9Cbx.setInvalidAllowed(false);
        cuentaContable9Cbx.setNewItemsAllowed(false);

        cuentaContable10Cbx = new ComboBox();
        cuentaContable10Cbx.setWidth("24em");
        cuentaContable10Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable10Cbx.setInvalidAllowed(false);
        cuentaContable10Cbx.setNewItemsAllowed(false);

        llenarComboCuentaContable();
        llenarComboProveedor(); // proveedores

        codigo1Txt = new TextField();
        codigo1Txt.setVisible(false);

        codigo2Txt = new TextField();
        codigo2Txt.setVisible(false);

        codigo3Txt = new TextField();
        codigo3Txt.setVisible(false);

        codigo4Txt = new TextField();
        codigo4Txt.setVisible(false);

        codigo5Txt = new TextField();
        codigo5Txt.setVisible(false);

        codigo6Txt = new TextField();
        codigo6Txt.setVisible(false);

        codigo7Txt = new TextField();
        codigo7Txt.setVisible(false);

        codigo8Txt = new TextField();
        codigo8Txt.setVisible(false);

        codigo9Txt = new TextField();
        codigo9Txt.setVisible(false);

        codigo10Txt = new TextField();
        codigo10Txt.setVisible(false);

        haber1Txt = new NumberField("HABER : ");
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
        haber1Txt.setWidth("7em");
        haber1Txt.setValue(0.00);

        haber2Txt = new NumberField();
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
        haber2Txt.setWidth("7em");
        haber2Txt.setValue(0.00);

        haber3Txt = new NumberField();
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
        haber3Txt.setWidth("7em");
        haber3Txt.setValue(0.00);

        haber4Txt = new NumberField();
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
        haber4Txt.setWidth("7em");
        haber4Txt.setValue(0.00);

        haber5Txt = new NumberField();
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
        haber5Txt.setWidth("7em");
        haber5Txt.setValue(0.00);

        haber6Txt = new NumberField();
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
        haber6Txt.setWidth("7em");
        haber6Txt.setValue(0.00);

        haber7Txt = new NumberField();
        haber7Txt.setDecimalAllowed(true);
        haber7Txt.setDecimalPrecision(2);
        haber7Txt.setMinimumFractionDigits(2);
        haber7Txt.setDecimalSeparator('.');
        haber7Txt.setDecimalSeparatorAlwaysShown(true);
        haber7Txt.setValue(0d);
        haber7Txt.setGroupingUsed(true);
        haber7Txt.setGroupingSeparator(',');
        haber7Txt.setGroupingSize(3);
        haber7Txt.setImmediate(true);
        haber7Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        haber7Txt.setWidth("7em");
        haber7Txt.setValue(0.00);

        haber8Txt = new NumberField();
        haber8Txt.setDecimalAllowed(true);
        haber8Txt.setDecimalPrecision(2);
        haber8Txt.setMinimumFractionDigits(2);
        haber8Txt.setDecimalSeparator('.');
        haber8Txt.setDecimalSeparatorAlwaysShown(true);
        haber8Txt.setValue(0d);
        haber8Txt.setGroupingUsed(true);
        haber8Txt.setGroupingSeparator(',');
        haber8Txt.setGroupingSize(3);
        haber8Txt.setImmediate(true);
        haber8Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        haber8Txt.setWidth("7em");
        haber8Txt.setValue(0.00);

        haber9Txt = new NumberField();
        haber9Txt.setDecimalAllowed(true);
        haber9Txt.setDecimalPrecision(2);
        haber9Txt.setMinimumFractionDigits(2);
        haber9Txt.setDecimalSeparator('.');
        haber9Txt.setDecimalSeparatorAlwaysShown(true);
        haber9Txt.setValue(0d);
        haber9Txt.setGroupingUsed(true);
        haber9Txt.setGroupingSeparator(',');
        haber9Txt.setGroupingSize(3);
        haber9Txt.setImmediate(true);
        haber9Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        haber9Txt.setWidth("7em");
        haber9Txt.setValue(0.00);

        haber10Txt = new NumberField();
        haber10Txt.setDecimalAllowed(true);
        haber10Txt.setDecimalPrecision(2);
        haber10Txt.setMinimumFractionDigits(2);
        haber10Txt.setDecimalSeparator('.');
        haber10Txt.setDecimalSeparatorAlwaysShown(true);
        haber10Txt.setValue(0d);
        haber10Txt.setGroupingUsed(true);
        haber10Txt.setGroupingSeparator(',');
        haber10Txt.setGroupingSize(3);
        haber10Txt.setImmediate(true);
        haber10Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        haber10Txt.setWidth("7em");
        haber10Txt.setValue(0.00);

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

        haber8QTxt = new NumberField();
        haber8QTxt.setValidationVisible(false);
        haber8QTxt.setDecimalAllowed(true);
        haber8QTxt.setDecimalPrecision(2);
        haber8QTxt.setMinimumFractionDigits(2);
        haber8QTxt.setDecimalSeparator('.');
        haber8QTxt.setDecimalSeparatorAlwaysShown(true);
        haber8QTxt.setValue(0d);
        haber8QTxt.setGroupingUsed(true);
        haber8QTxt.setGroupingSeparator(',');
        haber8QTxt.setGroupingSize(3);
        haber8QTxt.setImmediate(true);
        haber8QTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        haber8QTxt.setWidth("8em");
        haber8QTxt.setValue(0.00);

        haber9QTxt = new NumberField();
        haber9QTxt.setValidationVisible(false);
        haber9QTxt.setDecimalAllowed(true);
        haber9QTxt.setDecimalPrecision(2);
        haber9QTxt.setMinimumFractionDigits(2);
        haber9QTxt.setDecimalSeparator('.');
        haber9QTxt.setDecimalSeparatorAlwaysShown(true);
        haber9QTxt.setValue(0d);
        haber9QTxt.setGroupingUsed(true);
        haber9QTxt.setGroupingSeparator(',');
        haber9QTxt.setGroupingSize(3);
        haber9QTxt.setImmediate(true);
        haber9QTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        haber9QTxt.setWidth("8em");
        haber9QTxt.setValue(0.00);

        haber10QTxt = new NumberField();
        haber10QTxt.setValidationVisible(false);
        haber10QTxt.setDecimalAllowed(true);
        haber10QTxt.setDecimalPrecision(2);
        haber10QTxt.setMinimumFractionDigits(2);
        haber10QTxt.setDecimalSeparator('.');
        haber10QTxt.setDecimalSeparatorAlwaysShown(true);
        haber10QTxt.setValue(0d);
        haber10QTxt.setGroupingUsed(true);
        haber10QTxt.setGroupingSeparator(',');
        haber10QTxt.setGroupingSize(3);
        haber10QTxt.setImmediate(true);
        haber10QTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        haber10QTxt.setWidth("8em");
        haber10QTxt.setValue(0.00);

        debe1Txt = new NumberField("DEBE : ");
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

        debe5Txt = new NumberField();
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
        debe5Txt.setWidth("7em");
        debe5Txt.setValue(0.00);

        debe6Txt = new NumberField();
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
        debe6Txt.setWidth("7em");
        debe6Txt.setValue(0.00);

        debe7Txt = new NumberField();
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
        debe7Txt.setWidth("7em");
        debe7Txt.setValue(0.00);

        debe8Txt = new NumberField();
        debe8Txt.setDecimalAllowed(true);
        debe8Txt.setDecimalPrecision(2);
        debe8Txt.setMinimumFractionDigits(2);
        debe8Txt.setDecimalSeparator('.');
        debe8Txt.setDecimalSeparatorAlwaysShown(true);
        debe8Txt.setValue(0d);
        debe8Txt.setGroupingUsed(true);
        debe8Txt.setGroupingSeparator(',');
        debe8Txt.setGroupingSize(3);
        debe8Txt.setImmediate(true);
        debe8Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        debe8Txt.setWidth("7em");
        debe8Txt.setValue(0.00);

        debe9Txt = new NumberField();
        debe9Txt.setDecimalAllowed(true);
        debe9Txt.setDecimalPrecision(2);
        debe9Txt.setMinimumFractionDigits(2);
        debe9Txt.setDecimalSeparator('.');
        debe9Txt.setDecimalSeparatorAlwaysShown(true);
        debe9Txt.setValue(0d);
        debe9Txt.setGroupingUsed(true);
        debe9Txt.setGroupingSeparator(',');
        debe9Txt.setGroupingSize(3);
        debe9Txt.setImmediate(true);
        debe9Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        debe9Txt.setWidth("7em");
        debe9Txt.setValue(0.00);

        debe10Txt = new NumberField();
        debe10Txt.setDecimalAllowed(true);
        debe10Txt.setDecimalPrecision(2);
        debe10Txt.setMinimumFractionDigits(2);
        debe10Txt.setDecimalSeparator('.');
        debe10Txt.setDecimalSeparatorAlwaysShown(true);
        debe10Txt.setValue(0d);
        debe10Txt.setGroupingUsed(true);
        debe10Txt.setGroupingSeparator(',');
        debe10Txt.setGroupingSize(3);
        debe10Txt.setImmediate(true);
        debe10Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        debe10Txt.setWidth("7em");
        debe10Txt.setValue(0.00);

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
        debe5QTxt.setDecimalAllowed(true);
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

        debe8QTxt = new NumberField();
        debe8QTxt.setValidationVisible(false);
        debe8QTxt.setDecimalAllowed(true);
        debe8QTxt.setDecimalPrecision(2);
        debe8QTxt.setMinimumFractionDigits(2);
        debe8QTxt.setDecimalSeparator('.');
        debe8QTxt.setDecimalSeparatorAlwaysShown(true);
        debe8QTxt.setValue(0d);
        debe8QTxt.setGroupingUsed(true);
        debe8QTxt.setGroupingSeparator(',');
        debe8QTxt.setGroupingSize(3);
        debe8QTxt.setImmediate(true);
        debe8QTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        debe8QTxt.setWidth("8em");
        debe8QTxt.setValue(0.00);

        debe9QTxt = new NumberField();
        debe9QTxt.setValidationVisible(false);
        debe9QTxt.setDecimalAllowed(true);
        debe9QTxt.setDecimalPrecision(2);
        debe9QTxt.setMinimumFractionDigits(2);
        debe9QTxt.setDecimalSeparator('.');
        debe9QTxt.setDecimalSeparatorAlwaysShown(true);
        debe9QTxt.setValue(0d);
        debe9QTxt.setGroupingUsed(true);
        debe9QTxt.setGroupingSeparator(',');
        debe9QTxt.setGroupingSize(3);
        debe9QTxt.setImmediate(true);
        debe9QTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        debe9QTxt.setWidth("8em");
        debe9QTxt.setValue(0.00);

        debe10QTxt = new NumberField();
        debe10QTxt.setValidationVisible(false);
        debe10QTxt.setDecimalAllowed(true);
        debe10QTxt.setDecimalPrecision(2);
        debe10QTxt.setMinimumFractionDigits(2);
        debe10QTxt.setDecimalSeparator('.');
        debe10QTxt.setDecimalSeparatorAlwaysShown(true);
        debe10QTxt.setValue(0d);
        debe10QTxt.setGroupingUsed(true);
        debe10QTxt.setGroupingSeparator(',');
        debe10QTxt.setGroupingSize(3);
        debe10QTxt.setImmediate(true);
        debe10QTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        debe10QTxt.setWidth("8em");
        debe10QTxt.setValue(0.00);

        guardarBtn = new Button("Grabar");
        guardarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        guardarBtn.setIcon(FontAwesome.SAVE);
        guardarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                ingresarReembolsos(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getAnticiposProveedor());
            }
        });

        salirBtn = new Button("Salir");
        salirBtn.addStyleName(ValoTheme.BUTTON_DANGER);
        salirBtn.setWidth("7em");
        salirBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();
            }
        });

        layoutHorizontal1.addComponent(cuentaContable1Cbx);
        layoutHorizontal1.addComponent(debe1Txt);
        layoutHorizontal1.addComponent(haber1Txt);
        layoutHorizontal1.addComponent(codigo1Txt);
        layoutHorizontal1.addComponent(debe1QTxt);
        layoutHorizontal1.addComponent(haber1QTxt);

        layoutHorizontal2.addComponent(cuentaContable2Cbx);
        layoutHorizontal2.addComponent(debe2Txt);
        layoutHorizontal2.addComponent(haber2Txt);
        layoutHorizontal2.addComponent(codigo2Txt);
        layoutHorizontal2.addComponent(debe2QTxt);
        layoutHorizontal2.addComponent(haber2QTxt);

        layoutHorizontal3.addComponent(cuentaContable3Cbx);
        layoutHorizontal3.addComponent(debe3Txt);
        layoutHorizontal3.addComponent(haber3Txt);
        layoutHorizontal3.addComponent(codigo3Txt);
        layoutHorizontal3.addComponent(debe3QTxt);
        layoutHorizontal3.addComponent(haber3QTxt);

        layoutHorizontal4.addComponent(cuentaContable4Cbx);
        layoutHorizontal4.addComponent(debe4Txt);
        layoutHorizontal4.addComponent(haber4Txt);
        layoutHorizontal4.addComponent(codigo4Txt);
        layoutHorizontal4.addComponent(debe4QTxt);
        layoutHorizontal4.addComponent(haber4QTxt);

        layoutHorizontal5.addComponent(cuentaContable5Cbx);
        layoutHorizontal5.addComponent(debe5Txt);
        layoutHorizontal5.addComponent(haber5Txt);
        layoutHorizontal5.addComponent(codigo5Txt);
        layoutHorizontal5.addComponent(debe5QTxt);
        layoutHorizontal5.addComponent(haber5QTxt);

        layoutHorizontal6.addComponent(cuentaContable6Cbx);
        layoutHorizontal6.addComponent(debe6Txt);
        layoutHorizontal6.addComponent(haber6Txt);
        layoutHorizontal6.addComponent(codigo6Txt);
        layoutHorizontal6.addComponent(debe6QTxt);
        layoutHorizontal6.addComponent(haber6QTxt);

        layoutHorizontal7.addComponent(cuentaContable7Cbx);
        layoutHorizontal7.addComponent(debe7Txt);
        layoutHorizontal7.addComponent(haber7Txt);
        layoutHorizontal7.addComponent(codigo7Txt);
        layoutHorizontal7.addComponent(haber7QTxt);
        layoutHorizontal7.addComponent(debe7QTxt);

        layoutHorizontal8.addComponent(cuentaContable8Cbx);
        layoutHorizontal8.addComponent(debe8Txt);
        layoutHorizontal8.addComponent(haber8Txt);
        layoutHorizontal8.addComponent(codigo8Txt);
        layoutHorizontal8.addComponent(debe8QTxt);
        layoutHorizontal8.addComponent(haber8QTxt);

        layoutHorizontal9.addComponent(cuentaContable9Cbx);
        layoutHorizontal9.addComponent(debe9Txt);
        layoutHorizontal9.addComponent(haber9Txt);
        layoutHorizontal9.addComponent(codigo9Txt);
        layoutHorizontal9.addComponent(debe9QTxt);
        layoutHorizontal9.addComponent(haber9QTxt);

        layoutHorizontal10.addComponent(cuentaContable10Cbx);
        layoutHorizontal10.addComponent(debe10Txt);
        layoutHorizontal10.addComponent(haber10Txt);
        layoutHorizontal10.addComponent(codigo10Txt);
        layoutHorizontal10.addComponent(debe10QTxt);
        layoutHorizontal10.addComponent(haber10QTxt);

        layoutHorizontal11.addComponents(salirBtn, guardarBtn);
        layoutHorizontal11.setComponentAlignment(guardarBtn, Alignment.BOTTOM_CENTER);
        layoutHorizontal11.setComponentAlignment(salirBtn, Alignment.BOTTOM_CENTER);

        rightVerticalLayout.addComponent(anticiposGrid);
        rightVerticalLayout.setComponentAlignment(anticiposGrid, Alignment.TOP_CENTER);
        rightVerticalLayout.addComponent(layoutHorizontal1);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal1, Alignment.MIDDLE_CENTER);

        rightVerticalLayout.addComponent(layoutHorizontal2);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal2, Alignment.MIDDLE_CENTER);

        rightVerticalLayout.addComponent(layoutHorizontal3);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal3, Alignment.MIDDLE_CENTER);

        rightVerticalLayout.addComponent(layoutHorizontal4);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal4, Alignment.MIDDLE_CENTER);

        rightVerticalLayout.addComponent(layoutHorizontal5);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal5, Alignment.MIDDLE_CENTER);

        rightVerticalLayout.addComponent(layoutHorizontal6);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal6, Alignment.MIDDLE_CENTER);

        rightVerticalLayout.addComponent(layoutHorizontal7);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal7, Alignment.MIDDLE_CENTER);

        rightVerticalLayout.addComponent(layoutHorizontal8);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal8, Alignment.MIDDLE_CENTER);

        rightVerticalLayout.addComponent(layoutHorizontal9);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal9, Alignment.MIDDLE_CENTER);

        rightVerticalLayout.addComponent(layoutHorizontal10);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal10, Alignment.MIDDLE_CENTER);

        rightVerticalLayout.addComponent(layoutHorizontal11);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal11, Alignment.MIDDLE_CENTER);

        horizontalLayout.addComponents(leftVerticalLayout, rightVerticalLayout);

        return horizontalLayout;

    }

    public void llenarTablaAnticiposProveedor() {

        anticiposContainer.removeAllItems();

        queryString = " SELECT contabilidad_partida.CodigoPartida, contabilidad_partida.CodigoCC, contabilidad_partida.IdNomenclatura,";
        queryString += " contabilidad_nomenclatura.NoCuenta, contabilidad_partida.MonedaDocumento, contabilidad_partida.IdEmpresa, ";
        queryString += " contabilidad_partida.IdProveedor, SUM(contabilidad_partida.Debe) TOTALDEBE, SUM(contabilidad_partida.Haber) TOTALHABER,";
        queryString += " SUM(contabilidad_partida.DebeQuetzales) TOTALDEBEQ, SUM(contabilidad_partida.HaberQuetzales) TOTALHABERQ,";
        queryString += " SUM(DEBE - HABER) TOTALSALDO, SUM(DebeQuetzales - HaberQuetzales) TOTALSALDOQ, contabilidad_partida.NumeroDocumento, ";
        queryString += " ((" + montoTxt.getDoubleValueDoNotThrow() + " / contabilidad_partida.Debe) * contabilidad_partida.DebeQuetzales) ProporcionDebeQ, ";
        queryString += " ((" + montoTxt.getDoubleValueDoNotThrow() + " / contabilidad_partida.Haber) * contabilidad_partida.HaberQuetzales) ProporcionHaberQ";
        queryString += " FROM contabilidad_partida";
        queryString += " INNER JOIN contabilidad_nomenclatura ON contabilidad_nomenclatura.IdNomenclatura = contabilidad_partida.IdNomenclatura";
        queryString += " And contabilidad_partida.IdProveedor = " + proveedorCbx.getValue();
        queryString += " And contabilidad_partida.IdEmpresa = " + empresaCbx.getValue();
        queryString += " AND (trim(contabilidad_partida.CodigoCC) <> '' AND contabilidad_partida.CodigoCC <> '0')";
        queryString += " AND contabilidad_nomenclatura.IdNomenclatura = " + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getAnticiposProveedor();
        queryString += " GROUP BY contabilidad_partida.CodigoCC, contabilidad_partida.IdNomenclatura";
        queryString += " HAVING TOTALSALDO > 0";

        System.out.println("Query para mostrar anticipos " + queryString);

        try {
            stQueryAnticipos = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecorfsAnticipos = stQueryAnticipos.executeQuery(queryString);

            if (rsRecorfsAnticipos.next()) { //  encontrado
                do {
                    Object itemId = anticiposContainer.addItem();

                    anticiposContainer.getContainerProperty(itemId, CODIGO_PARTIDA_PROPERTY).setValue(rsRecorfsAnticipos.getString("CodigoPartida"));
                    anticiposContainer.getContainerProperty(itemId, CODIGO_CC_PROPERTY).setValue(rsRecorfsAnticipos.getString("CodigoCC"));
                    anticiposContainer.getContainerProperty(itemId, NUMERO_DOCUMENTO_PROPERTY).setValue(rsRecorfsAnticipos.getString("NumeroDocumento"));
                    anticiposContainer.getContainerProperty(itemId, MONEDA_DOCUMENTO_PROPERTY).setValue(rsRecorfsAnticipos.getString("MonedaDocumento"));
                    anticiposContainer.getContainerProperty(itemId, DEBE_PROPERTY).setValue(rsRecorfsAnticipos.getString("TOTALDEBE"));
                    anticiposContainer.getContainerProperty(itemId, HABER_PROPERTY).setValue(rsRecorfsAnticipos.getString("TOTALHABER"));
                    anticiposContainer.getContainerProperty(itemId, DEBE_QUETZALES_PROPERTY).setValue(rsRecorfsAnticipos.getString("ProporcionDebeQ"));
                    anticiposContainer.getContainerProperty(itemId, HABER_QUETZALES_PROPERTY).setValue(rsRecorfsAnticipos.getString("ProporcionHaberQ"));
                    anticiposContainer.getContainerProperty(itemId, SALDO_DOCUMENTO_PROPERTY).setValue(rsRecorfsAnticipos.getString("TOTALSALDO"));

                } while (rsRecorfsAnticipos.next());
            }

        } catch (Exception ex) {
            System.out.println("Error al listar tabla de Anticipos : " + ex);
            ex.printStackTrace();
        }

    }

    public void llenarComboEmpresa() {

        queryString = " SELECT * from contabilidad_empresa";
        queryString += " Where IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) { //  encontrado
                empresaCbx.addItem(rsRecords.getString("IdEmpresa"));
                empresaCbx.setItemCaption(rsRecords.getString("IdEmpresa"), rsRecords.getString("Empresa"));
            }
            rsRecords.first();

            empresaCbx.select(rsRecords.getString("IdEmpresa"));

        } catch (Exception ex1) {
            System.out.println("Error al llenar combo empresas: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void llenarComboProveedor() {
        queryString = " SELECT DISTINCT prov.* ";
        queryString += " FROM proveedor prov";
        queryString += " WHERE prov.Inhabilitado = 0 ";
        queryString += " AND (ESPROVEEDOR = 1 OR ESRELACIONADA = 1)";
        queryString += " Order By prov.Nombre";

        proveedorCbx.removeAllItems();

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) {  /// encontrado
                proveedorCbx.addItem(rsRecords.getString("IDProveedor"));
//                proveedorCbx.setItemCaption(rsRecords.getString("IDProveedor"), rsRecords.getString("Nombre"));
                proveedorCbx.setItemCaption(rsRecords.getString("IDProveedor"), "(" + rsRecords.getString("IDProveedor") + ") " + rsRecords.getString("Nombre"));
                proveedorCbx.getItem(rsRecords.getString("IDProveedor")).getItemProperty(NIT_PROPERTY).setValue(rsRecords.getString("NIT"));
                proveedorCbx.getItem(rsRecords.getString("IDProveedor")).getItemProperty(NOMBRESINCODIGO_PROPERTY).setValue(rsRecords.getString("Nombre") );

            }

        } catch (Exception ex1) {
            System.out.println("Error al listar de clientes (tabla proveedores) " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void llenarComboCuentaContable() {

        queryString = " SELECT * from contabilidad_nomenclatura";
        queryString += " where Estatus = 'HABILITADA'";
        queryString += " Order By N5";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) {  // encontrado

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

                cuentaContable8Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable8Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable9Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable9Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable10Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable10Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

            }

        } catch (Exception ex1) {
            System.out.println("Error al combo cuentas contables: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void generarPartidaReembolsoAnticipos() {
        monedaCbx.select("QUETZALES");
        cuentaContable1Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaLocal());
        cuentaContable2Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getAnticiposProveedor());
        cuentaContable3Cbx.clear();
        debe2Txt.setReadOnly(true);
        haber1Txt.setReadOnly(true);

        this.proveedorCbx.setCaption("Proveedor");
        llenarComboProveedor();
    }

    public void calcularPartida() {

        Object gridItem;
        double montoTotalSeleccionado = 0.00;

        Iterator iter = anticiposGrid.getSelectedRows().iterator();

        if (iter == null) {
            limpiarPartida();

            montoTxt.setReadOnly(false);
            montoTxt.setValue(montoTotalSeleccionado);
            montoTxt.setReadOnly(true);
            return;
        }
        if (!iter.hasNext()) {
            limpiarPartida();

            montoTxt.setReadOnly(false);
            montoTxt.setValue(montoTotalSeleccionado);
            montoTxt.setReadOnly(true);
            return;
        }

        gridItem = iter.next();
        montoTotalSeleccionado = Double.valueOf(String.valueOf(anticiposGrid.getContainerDataSource().getItem(gridItem).getItemProperty(SALDO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", ""));

        while (iter.hasNext()) { //// Si hay mas de un registro seleccionado
            gridItem = iter.next();
            montoTotalSeleccionado += Double.valueOf(String.valueOf(anticiposGrid.getContainerDataSource().getItem(gridItem).getItemProperty(SALDO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", ""));

        }
        limpiarPartida();

        Iterator iter2 = anticiposGrid.getSelectedRows().iterator();

        double montoEnganche = 0.00, montoProporcialQ = 0.00;
        String codigoCC = "";

        while (iter2.hasNext()) {  // POR CADA ENGANCHE QUE ESTAMOS SELECCINANDO

            Object gridItem2 = iter2.next();
            montoEnganche = Double.valueOf(String.valueOf(anticiposGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(SALDO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", ""));
            montoProporcialQ = Double.valueOf(String.valueOf(anticiposGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(DEBE_QUETZALES_PROPERTY).getValue()).replaceAll(",", ""));
            codigoCC = String.valueOf(anticiposGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(CODIGO_CC_PROPERTY).getValue());

//            montoTxt.setReadOnly(false);
//            montoTxt.setValue(montoTotalSeleccionado);
//            montoTxt.setReadOnly(true);
//
//            monedaCbx.setReadOnly(false);
//            monedaCbx.select(String.valueOf(anticiposGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONEDA_PROPERTY).getValue()));
//            monedaCbx.setReadOnly(true);

            try {
                //// ENGANCHES SELECCIONADOS
                if (cuentaContable1Cbx.getValue() == null) {
                    cuentaContable1Cbx.setValue(((SopdiUI)UI.getCurrent()).cuentasContablesDefault.getAnticiposProveedor());
                    debe1Txt.setValue(montoEnganche);
                    debe1QTxt.setValue(montoProporcialQ);
                    codigo1Txt.setValue(codigoCC);

                    haber1Txt.setReadOnly(true);
                    haber1QTxt.setReadOnly(true);

                } else if (cuentaContable2Cbx.getValue() == null) {
                    cuentaContable2Cbx.setValue(((SopdiUI)UI.getCurrent()).cuentasContablesDefault.getAnticiposProveedor());
                    debe2Txt.setValue(montoEnganche);
                    debe2QTxt.setValue(montoProporcialQ);
                    codigo2Txt.setValue(codigoCC);

                    haber2Txt.setReadOnly(true);
                    haber2QTxt.setReadOnly(true);
                } else if (cuentaContable3Cbx.getValue() == null) {
                    cuentaContable3Cbx.setValue(((SopdiUI)UI.getCurrent()).cuentasContablesDefault.getAnticiposProveedor());
                    debe3Txt.setValue(montoEnganche);
                    debe3QTxt.setValue(montoProporcialQ);
                    codigo3Txt.setValue(codigoCC);

                    haber3Txt.setReadOnly(true);
                    haber3QTxt.setReadOnly(true);
                } else if (cuentaContable4Cbx.getValue() == null) {
                    cuentaContable4Cbx.select(((SopdiUI)UI.getCurrent()).cuentasContablesDefault.getAnticiposProveedor());
                    debe4Txt.setValue(montoEnganche);
                    debe4QTxt.setValue(montoProporcialQ);
                    codigo4Txt.setValue(codigoCC);

                    haber4Txt.setReadOnly(true);
                    haber4QTxt.setReadOnly(true);
                } else if (cuentaContable5Cbx.getValue() == null) {
                    cuentaContable5Cbx.select(((SopdiUI)UI.getCurrent()).cuentasContablesDefault.getAnticiposProveedor());
                    debe5Txt.setValue(montoEnganche);
                    debe5QTxt.setValue(montoProporcialQ);
                    codigo5Txt.setValue(codigoCC);

                    haber5Txt.setReadOnly(true);
                    haber5QTxt.setReadOnly(true);
                }
            } catch (Exception ex) {
                System.out.println("Error " + ex);
            }
        }

        if(monedaCbx.getValue().toString().equals("QUETZALES")) {
            tipoCambioTxt.setValue(1.00);
            if (cuentaContable2Cbx.getValue() == null) {
                cuentaContable2Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaLocal());
                haber2Txt.setValue(montoTotalSeleccionado);
                haber2QTxt.setReadOnly(false);
                haber2QTxt.setValue(montoProporcialQ);
                haber2QTxt.setReadOnly(true);

                debe2Txt.setReadOnly(true);
                debe2QTxt.setReadOnly(true);
            } else if (cuentaContable3Cbx.getValue() == null) {
                cuentaContable3Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaLocal());
                haber3Txt.setValue(montoTotalSeleccionado);
                haber3QTxt.setReadOnly(false);
                haber3QTxt.setValue(montoProporcialQ);
                haber2QTxt.setReadOnly(true);

                debe3Txt.setReadOnly(true);
                debe3QTxt.setReadOnly(true);
            } else if (cuentaContable4Cbx.getValue() == null) {
                cuentaContable4Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaLocal());
                haber4Txt.setValue(montoTotalSeleccionado);
                haber4QTxt.setReadOnly(false);
                haber4QTxt.setValue(montoProporcialQ);
                haber4QTxt.setReadOnly(true);

                debe4Txt.setReadOnly(true);
                debe4QTxt.setReadOnly(true);
            } else if (cuentaContable5Cbx.getValue() == null) {
                cuentaContable5Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaLocal());
                haber5Txt.setValue(montoTotalSeleccionado);
                haber5QTxt.setReadOnly(false);
                haber5QTxt.setValue(montoProporcialQ);
                haber5QTxt.setReadOnly(true);

                debe5Txt.setReadOnly(true);
                debe5QTxt.setReadOnly(true);
            } else if (cuentaContable6Cbx.getValue() == null) {
                cuentaContable6Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaLocal());
                haber6Txt.setValue(montoTotalSeleccionado);
                haber6QTxt.setReadOnly(false);
                haber6QTxt.setValue(montoProporcialQ);
                haber6QTxt.setReadOnly(true);

                debe6Txt.setReadOnly(true);
                debe6QTxt.setReadOnly(true);
            }
        }
        else {
            double enquetzales = montoProporcialQ;
//                                tasaCambioTxt.setValue((Float.toString(((SopdiUI) UI.getCurrent()).sessionInformation.getFltExchangeRate())));
            if (cuentaContable2Cbx.getValue() == null) {
                cuentaContable2Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaExtranjera());
                haber2Txt.setValue(montoTotalSeleccionado);
                haber2QTxt.setReadOnly(false);
                haber2QTxt.setValue(enquetzales);
                haber2QTxt.setReadOnly(true);

                debe2Txt.setReadOnly(true);
                debe2QTxt.setReadOnly(true);
            } else if (cuentaContable3Cbx.getValue() == null) {
                cuentaContable3Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaExtranjera());
                haber3Txt.setValue(montoTotalSeleccionado);
                haber3QTxt.setReadOnly(false);
                haber3QTxt.setValue(enquetzales);
                haber3QTxt.setReadOnly(true);

                debe3Txt.setReadOnly(true);
                debe3QTxt.setReadOnly(true);
            } else if (cuentaContable4Cbx.getValue() == null) {
                cuentaContable4Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaExtranjera());
                haber4Txt.setValue(montoTotalSeleccionado);
                haber4QTxt.setReadOnly(false);
                haber4QTxt.setValue(enquetzales);
                haber4QTxt.setReadOnly(true);

                debe4Txt.setReadOnly(true);
                debe4QTxt.setReadOnly(true);
            } else if (cuentaContable5Cbx.getValue() == null) {
                cuentaContable5Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaExtranjera());
                haber5Txt.setValue(montoTotalSeleccionado);
                haber5QTxt.setReadOnly(false);
                haber5QTxt.setValue(enquetzales);
                haber5QTxt.setReadOnly(true);

                debe5Txt.setReadOnly(true);
                debe5QTxt.setReadOnly(true);
            } else if (cuentaContable6Cbx.getValue() == null) {
                cuentaContable6Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaExtranjera());
                haber6Txt.setValue(montoTotalSeleccionado);
                haber6QTxt.setReadOnly(false);
                haber6QTxt.setValue(enquetzales);
                haber6QTxt.setReadOnly(true);

                debe6Txt.setReadOnly(true);
                debe6QTxt.setReadOnly(true);
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

                debe3Txt.setReadOnly(true);
                debe3QTxt.setReadOnly(true);
            } else if (cuentaContable4Cbx.getValue() == null && !cuentaContable3Cbx.getValue().equals(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getDiferencialCambiario())) {
                cuentaContable4Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getDiferencialCambiario());
                haber4QTxt.setValue(diferencia);

                debe4Txt.setReadOnly(true);
                debe4QTxt.setReadOnly(true);
            } else if (cuentaContable5Cbx.getValue() == null && !cuentaContable4Cbx.getValue().equals(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getDiferencialCambiario())) {
                cuentaContable5Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getDiferencialCambiario());
                haber5QTxt.setValue(diferencia);

                debe5Txt.setReadOnly(true);
                debe5QTxt.setReadOnly(true);
            } else if (cuentaContable6Cbx.getValue() == null && !cuentaContable5Cbx.getValue().equals(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getDiferencialCambiario())) {
                cuentaContable6Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getDiferencialCambiario());
                haber6QTxt.setValue(diferencia);

                debe6Txt.setReadOnly(true);
                debe6QTxt.setReadOnly(true);
            } else if (cuentaContable7Cbx.getValue() == null && !cuentaContable6Cbx.getValue().equals(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getDiferencialCambiario())) {
                cuentaContable7Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getDiferencialCambiario());
                haber7QTxt.setValue(diferencia);

                debe7Txt.setReadOnly(true);
                debe7QTxt.setReadOnly(true);
            }
        }
        else {
            if (cuentaContable3Cbx.getValue() == null) {
                cuentaContable3Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getDiferencialCambiario());
                debe3QTxt.setValue(diferencia);

                haber3QTxt.setReadOnly(true);
                haber3QTxt.setReadOnly(true);
            } else if (cuentaContable4Cbx.getValue() == null && !cuentaContable3Cbx.getValue().equals(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getDiferencialCambiario())) {
                cuentaContable4Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getDiferencialCambiario());
                debe4QTxt.setValue(diferencia);

                haber4Txt.setReadOnly(true);
                haber4QTxt.setReadOnly(true);
            } else if (cuentaContable5Cbx.getValue() == null && !cuentaContable4Cbx.getValue().equals(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getDiferencialCambiario())) {
                cuentaContable5Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getDiferencialCambiario());
                debe5QTxt.setValue(diferencia);

                haber5QTxt.setReadOnly(true);
                haber5QTxt.setReadOnly(true);
            } else if (cuentaContable6Cbx.getValue() == null && !cuentaContable5Cbx.getValue().equals(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getDiferencialCambiario())) {
                cuentaContable6Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getDiferencialCambiario());
                debe6QTxt.setValue(diferencia);

                haber6Txt.setReadOnly(true);
                haber6QTxt.setReadOnly(true);
            } else if (cuentaContable7Cbx.getValue() == null && !cuentaContable6Cbx.getValue().equals(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getDiferencialCambiario())) {
                cuentaContable7Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getDiferencialCambiario());
                debe7QTxt.setValue(diferencia);

                haber7Txt.setReadOnly(true);
                haber7QTxt.setReadOnly(true);
            }
        }
    }

    public void ingresarReembolsos(String cuentaContable) {

        try {
            Utileria utileria = new Utileria();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

            Date fechaInicial = dateFormat.parse(utileria.getFecha());
            Date fechaFinal = dateFormat.parse(utileria.getFechaYYYYMMDD(fechaDt.getValue()));

            int dias = (int) ((fechaInicial.getTime() - fechaFinal.getTime()) / 86400000);

            if (dias > 30) {

                if (((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserToken().isEmpty()) {
                    ValidarTokenForm validarTokenForm = new ValidarTokenForm(false);
                    UI.getCurrent().addWindow(validarTokenForm);
                    validarTokenForm.center();
                    return;
                } else {
                    variableTemp = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserToken();
                    ((SopdiUI) UI.getCurrent()).sessionInformation.setStrUserToken("");
                }
            }
        } catch (Exception e) {
            System.out.println("Error validar fechas " + e);
            e.printStackTrace();

        }

        totalDebe = new BigDecimal(debe1Txt.getDoubleValueDoNotThrow()
                + debe2Txt.getDoubleValueDoNotThrow() + debe3Txt.getDoubleValueDoNotThrow()
                + debe4Txt.getDoubleValueDoNotThrow() + debe5Txt.getDoubleValueDoNotThrow()
                + debe6Txt.getDoubleValueDoNotThrow() + debe7Txt.getDoubleValueDoNotThrow()
                + debe8Txt.getDoubleValueDoNotThrow() + debe9Txt.getDoubleValueDoNotThrow()
                + debe10Txt.getDoubleValueDoNotThrow()
        ).setScale(2, BigDecimal.ROUND_HALF_UP);

        totalHaber = new BigDecimal(haber1Txt.getDoubleValueDoNotThrow()
                + haber2Txt.getDoubleValueDoNotThrow() + haber3Txt.getDoubleValueDoNotThrow()
                + haber4Txt.getDoubleValueDoNotThrow() + haber5Txt.getDoubleValueDoNotThrow()
                + haber6Txt.getDoubleValueDoNotThrow() + haber7Txt.getDoubleValueDoNotThrow()
                + haber8Txt.getDoubleValueDoNotThrow() + haber9Txt.getDoubleValueDoNotThrow()
                + haber10Txt.getDoubleValueDoNotThrow()
        ).setScale(2, BigDecimal.ROUND_HALF_UP);

        totalDebe.setScale(2, BigDecimal.ROUND_HALF_UP);
        totalHaber.setScale(2, BigDecimal.ROUND_HALF_UP);

        if (totalDebe.doubleValue() != totalHaber.doubleValue()) {
            System.out.println("Debe =" + totalDebe.doubleValue() + "  haber=" + totalHaber);
            Notification.show("La partida es descuadrada, por favor revisar"
                    + " Debe = " + totalDebe.doubleValue() + "  Haber = " + totalHaber, Notification.Type.WARNING_MESSAGE);
            return;
        }

        if (montoTxt.getDoubleValueDoNotThrow() != totalDebe.doubleValue()) {
            Notification.show("La Monto del documento esta descuadrado por favor revisar", Notification.Type.WARNING_MESSAGE);
            return;
        }

        if (((SopdiUI) UI.getCurrent()).esMesCerrado(String.valueOf(empresaCbx.getValue()), Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()))) {
            Notification.show("La fecha del documento no puede ser de un mes ya cerrado contablemente, revise!", Notification.Type.WARNING_MESSAGE);
            fechaDt.focus();
            return;
        }
        if (cuentaContable1Cbx.getValue() == null) {
            Notification.show("Por favor, seleccione una cuenta del DEBE..", Notification.Type.ERROR_MESSAGE);
            cuentaContable1Cbx.focus();
            return;
        }
        if (cuentaContable2Cbx.getValue() == null) {
            Notification.show("Por favor, seleccione una cuenta del HABER..", Notification.Type.ERROR_MESSAGE);
            cuentaContable2Cbx.focus();
            return;
        }
        if (proveedorCbx.getValue() == null) {
            Notification.show("Por favor, seleccione un cliente..", Notification.Type.ERROR_MESSAGE);
            proveedorCbx.focus();
            return;
        }
        if (montoTxt.getDoubleValueDoNotThrow() == 0.00) {
            Notification.show("Por favor, Ingrese un monto..", Notification.Type.ERROR_MESSAGE);
            montoTxt.focus();
            return;
        }
        if (cuentaContable3Cbx.getValue() != null) {
            if ((debe3Txt.getDoubleValueDoNotThrow() + haber3Txt.getDoubleValueDoNotThrow()) == 0) {
                Notification.show("Por favor, seleccione una cuenta del HABER..", Notification.Type.ERROR_MESSAGE);
                debe3Txt.focus();
                return;
            }
        }
        if (monedaCbx.getValue().equals("DOLARES") && tipoCambioTxt.getDoubleValueDoNotThrow() <= 1.00) {
            Notification.show("Si la transacción es en DOLARES, debe llebar tipo de cambio. Por favor, revise la moneda.", Notification.Type.ERROR_MESSAGE);
            monedaCbx.focus();
            return;
        }
        if (monedaCbx.getValue().equals("QUETZALES") && tipoCambioTxt.getDoubleValueDoNotThrow() < 1.00) {
            Notification.show("Si la transacción es en QUETZALES, debe llebar 1.00 de tipo de cambio. Por favor revise.", Notification.Type.ERROR_MESSAGE);
            monedaCbx.focus();
            return;
        } 
        if (monedaCbx.getValue().equals("DOLARES") && !String.valueOf(cuentaContable1Cbx.getValue()).equals(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaExtranjera())) {
            Notification.show("Si la transacción es en DOLARES, debe llebar cuenta contable DOLARES. Por favor evise la CUENTA CONTABLE.", Notification.Type.ERROR_MESSAGE);
            cuentaContable1Cbx.focus();
            return;
        }
        /*
        if (descripcionTxt.getValue().isEmpty()) {
            Notification.show("Por favor, Ingrese una descripción..", Notification.Type.ERROR_MESSAGE);
            descripcionTxt.focus();
            return;
        }
         */
        String codigoPartida = "";
        int contador = 0;
        Iterator iter = anticiposGrid.getSelectedRows().iterator();

        while (iter.hasNext()) {

            Object gridItem = iter.next();

            codigoPartida = String.valueOf(anticiposGrid.getContainerDataSource().getItem(gridItem).getItemProperty(CODIGO_PARTIDA_PROPERTY).getValue());
            contador = 2;

            queryString = " Update  contabilidad_partida ";

            if (contador == 2) { ///REBAJAR EL SALDO POR CADA FACTURA SELECCIONADA Y SU MONTO UTILIZADO CORRESPONDIENTE
                queryString += " Set Saldo = Saldo -" + haber2Txt.getDoubleValueDoNotThrow();
            } else if (contador == 3) {
                System.out.println("entro a contador numero 3");
                queryString += " Set Saldo = Saldo -" + haber3Txt.getDoubleValueDoNotThrow();
            } else if (contador == 4) {
                System.out.println("entro a contador numero 4");
                queryString += " Set Saldo = Saldo -" + haber4Txt.getDoubleValueDoNotThrow();
            } else if (contador == 5) {
                queryString += " Set Saldo = Saldo -" + haber5Txt.getDoubleValueDoNotThrow();
            } else if (contador == 6) {
                queryString += " Set Saldo = Saldo -" + haber6Txt.getDoubleValueDoNotThrow();
            } else if (contador == 7) {
                queryString += " Set Saldo = Saldo -" + haber7Txt.getDoubleValueDoNotThrow();
            } else if (contador == 8) {
                queryString += " Set Saldo = Saldo -" + haber8Txt.getDoubleValueDoNotThrow();
            } else if (contador == 9) {
                queryString += " Set Saldo = Saldo -" + haber9Txt.getDoubleValueDoNotThrow();
            } else if (contador == 10) {
                queryString += " Set Saldo = Saldo -" + haber10Txt.getDoubleValueDoNotThrow();
            }
            queryString += " Where CodigoPartida = '" + codigoPartida + "'";
            queryString += " And IdEmpresa = " + empresaCbx.getValue();
            queryString += " And IdProveedor = " + proveedorCbx.getValue();

            System.out.println("Query actualizar saldo de anticipos :" + queryString);

            try {
                stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                stQuery.executeUpdate(queryString);

                contador = +1;

            } catch (Exception ex1) {
                System.out.println("Error al actualizar el saldo de facturas seleccionadas : " + ex1.getMessage());
                ex1.printStackTrace();
            }
        }

        queryString = " Select * from contabilidad_partida";
        queryString += " Where NumeroDocumento = '" + numeroTxt.getValue().toUpperCase().trim() + "'";
        queryString += " And IdProveedor     =  " + String.valueOf(proveedorCbx.getValue());
        queryString += " And IdEmpresa = " + String.valueOf(empresaCbx.getValue());
        queryString += " And TipoDocumento = '" + String.valueOf(medioCbx.getValue())  + "'";
        queryString += " And MonedaDocumento = '" + monedaCbx.getValue() + "'";

        System.out.println("\n\nQuery=" + queryString + "\n\n");

        try {
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                Notification.show("Este documento ya fué ingresado, revise.!.", Notification.Type.WARNING_MESSAGE);
                numeroTxt.focus();
                return;
            }
        } catch (Exception ex1) {
            System.out.println("Error al buscar documento : " + ex1.getMessage());
            ex1.printStackTrace();
        }

        String fecha = Utileria.getFechaYYYYMMDD_1(fechaDt.getValue());
        String ultimoEncontado;
        String dia = fecha.substring(8, 10);
        String mes = fecha.substring(5, 7);
        String año = fecha.substring(0, 4);

        codigoPartida = String.valueOf(empresaCbx.getValue()) + año + mes + dia + "5";

        queryString = " select codigoPartida from contabilidad_partida ";
        queryString += " where codigoPartida like '" + codigoPartida + "%'";
        queryString += " order by codigoPartida desc ";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {   ///encontrado

                ultimoEncontado = rsRecords.getString("codigoPartida").substring(12, 15);

                System.out.println("ultimo encontrado " + ultimoEncontado);

                codigoPartida += String.format("%03d", (Integer.valueOf(ultimoEncontado) + 1));

            } else {
                codigoPartida += "001";
            }

        } catch (Exception ex1) {
            System.out.println("Error al buscar el ultimo codigoPartida" + ex1.getMessage());
            ex1.printStackTrace();
        }

        queryString = " Insert Into contabilidad_partida (IdEmpresa, Estatus, CodigoPartida, CodigoCC, ";
        queryString += " TipoDocumento, Fecha, IdProveedor, NombreProveedor, SerieDocumento,";
        queryString += " NumeroDocumento, IdNomenclatura, MonedaDocumento, MontoDocumento, Debe, Haber,";
        queryString += " DebeQuetzales, HaberQuetzales, TipoCambio, Saldo, Descripcion,";
        queryString += " CreadoUsuario, CreadoFechaYHora)";
        queryString += " Values ";
        queryString += "(";
        queryString += String.valueOf(empresaCbx.getValue());
        queryString += ",'INGRESADO'";
        queryString += ",'" + codigoPartida + "'";
        queryString += ",'" + codigoPartida + "'";
        queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
        queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
        queryString += "," + proveedorCbx.getValue();
        queryString += ",'" + proveedorCbx.getContainerProperty(proveedorCbx.getValue(), NOMBRESINCODIGO_PROPERTY).getValue() + "'";
        queryString += ",''";
        queryString += ",'" + numeroTxt.getValue() + "'";
        queryString += "," + cuentaContable1Cbx.getValue();
        queryString += ",'" + monedaCbx.getValue() + "'";
        queryString += "," + montoTxt.getDoubleValueDoNotThrow();
        queryString += "," + String.valueOf(debe1Txt.getDoubleValueDoNotThrow()); // DEBE
        queryString += ",0.00"; // HABER
        queryString += "," + String.valueOf(debe1Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());
        queryString += ",0.00"; ///HABER Q.
        queryString += "," + String.valueOf(tipoCambioTxt.getDoubleValueDoNotThrow());
        queryString += ",0.00";
        queryString += ",'Ingreso por " + String.valueOf(tipoIngresoCbx.getValue()) + " " + descripcionTxt.getValue() + "'";
        queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
        queryString += ",current_timestamp";
        queryString += ")";

        //segundo  ingreso
        queryString += ",(";
        queryString += String.valueOf(empresaCbx.getValue());
        queryString += ",'INGRESADO'";
        queryString += ",'" + codigoPartida + "'";
        queryString += ",'" + codigo2Txt.getValue() + "'";
        queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
        queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
        queryString += "," + proveedorCbx.getValue();
        queryString += ",'" + proveedorCbx.getContainerProperty(proveedorCbx.getValue(), NOMBRESINCODIGO_PROPERTY).getValue() + "'";
        queryString += ",''";
        queryString += ",'" + numeroTxt.getValue() + "'";
        queryString += "," + cuentaContable2Cbx.getValue();
        queryString += ",'" + monedaCbx.getValue() + "'";
        queryString += "," + montoTxt.getDoubleValueDoNotThrow();
        queryString += ",0.00";// DEBE
        queryString += "," + String.valueOf(haber2Txt.getDoubleValueDoNotThrow()); // HABER
        queryString += ",0.00"; //DEBE Q.
        queryString += "," + String.valueOf(haber2Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());
        queryString += "," + String.valueOf(tipoCambioTxt.getDoubleValueDoNotThrow());
        queryString += ",0.00";
        queryString += ",'Ingreso por " + String.valueOf(tipoIngresoCbx.getValue()) + " " + descripcionTxt.getValue() + "'";
        queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
        queryString += ",current_timestamp";
        queryString += ")";

        if (cuentaContable3Cbx.getValue() != null) {
            //tercer  ingreso
            queryString += ",(";
            queryString += String.valueOf(empresaCbx.getValue());
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigo3Txt.getValue() + "'";
            queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + proveedorCbx.getContainerProperty(proveedorCbx.getValue(), NOMBRESINCODIGO_PROPERTY).getValue() + "'";
            queryString += ",''";
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += "," + cuentaContable3Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + montoTxt.getDoubleValueDoNotThrow();
            queryString += ",0.00";// DEBE
            queryString += "," + String.valueOf(haber3Txt.getDoubleValueDoNotThrow()); // HABER
            queryString += ",0.00"; //DEBE Q.
            queryString += "," + String.valueOf(haber3Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00";
            queryString += ",'Ingreso por " + String.valueOf(tipoIngresoCbx.getValue()) + " " + descripcionTxt.getValue() + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";

        }
        if (cuentaContable4Cbx.getValue() != null) {
            //tercer  ingreso
            queryString += ",(";
            queryString += String.valueOf(empresaCbx.getValue());
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigo4Txt.getValue() + "'";
            queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + proveedorCbx.getContainerProperty(proveedorCbx.getValue(), NOMBRESINCODIGO_PROPERTY).getValue() + "'";
            queryString += ",''";
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += "," + cuentaContable4Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + montoTxt.getDoubleValueDoNotThrow();
            queryString += ",0.00";// DEBE
            queryString += "," + String.valueOf(haber4Txt.getDoubleValueDoNotThrow()); // HABER
            queryString += ",0.00"; //DEBE Q.
            queryString += "," + String.valueOf(haber4Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00";
            queryString += ",'Ingreso por " + String.valueOf(tipoIngresoCbx.getValue()) + " " + descripcionTxt.getValue() + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";
        }
        if (cuentaContable5Cbx.getValue() != null) {
            //tercer  ingreso
            queryString += ",(";
            queryString += String.valueOf(empresaCbx.getValue());
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigo5Txt.getValue() + "'";
            queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + proveedorCbx.getContainerProperty(proveedorCbx.getValue(), NOMBRESINCODIGO_PROPERTY).getValue() + "'";
            queryString += ",''";
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += "," + cuentaContable5Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + montoTxt.getDoubleValueDoNotThrow();
            queryString += ",0.00";// DEBE
            queryString += "," + String.valueOf(haber5Txt.getDoubleValueDoNotThrow()); // HABER
            queryString += ",0.00"; //DEBE Q.
            queryString += "," + String.valueOf(haber5Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00";
            queryString += ",'Ingreso por " + String.valueOf(tipoIngresoCbx.getValue()) + " " + descripcionTxt.getValue() + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";
        }
        if (cuentaContable6Cbx.getValue() != null) {
            //tercer  ingreso
            queryString += ",(";
            queryString += String.valueOf(empresaCbx.getValue());
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigo6Txt.getValue() + "'";
            queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + proveedorCbx.getContainerProperty(proveedorCbx.getValue(), NOMBRESINCODIGO_PROPERTY).getValue() + "'";
            queryString += ",''";
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += "," + cuentaContable6Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + montoTxt.getDoubleValueDoNotThrow();
            queryString += ",0.00";// DEBE
            queryString += "," + String.valueOf(haber6Txt.getDoubleValueDoNotThrow()); // HABER
            queryString += ",0.00"; //DEBE Q.
            queryString += "," + String.valueOf(haber6Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00";
            queryString += ",'Ingreso por " + String.valueOf(tipoIngresoCbx.getValue()) + " " + descripcionTxt.getValue() + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";
        }
        if (cuentaContable7Cbx.getValue() != null) {
            //tercer  ingreso
            queryString += ",(";
            queryString += String.valueOf(empresaCbx.getValue());
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigo7Txt.getValue() + "'";
            queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + proveedorCbx.getContainerProperty(proveedorCbx.getValue(), NOMBRESINCODIGO_PROPERTY).getValue() + "'";
            queryString += ",''";
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += "," + cuentaContable7Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + montoTxt.getDoubleValueDoNotThrow();
            queryString += ",0.00";// DEBE
            queryString += "," + String.valueOf(haber7Txt.getDoubleValueDoNotThrow()); // HABER
            queryString += ",0.00"; //DEBE Q.
            queryString += "," + String.valueOf(haber7Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00";
            queryString += ",'Ingreso por " + String.valueOf(tipoIngresoCbx.getValue()) + " " + descripcionTxt.getValue() + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";
        }
        if (cuentaContable8Cbx.getValue() != null) {
            //tercer  ingreso
            queryString += ",(";
            queryString += String.valueOf(empresaCbx.getValue());
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigo8Txt.getValue() + "'";
            queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + proveedorCbx.getContainerProperty(proveedorCbx.getValue(), NOMBRESINCODIGO_PROPERTY).getValue() + "'";
            queryString += ",''";
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += "," + cuentaContable8Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + montoTxt.getDoubleValueDoNotThrow();
            queryString += ",0.00";// DEBE
            queryString += "," + String.valueOf(haber8Txt.getDoubleValueDoNotThrow()); // HABER
            queryString += ",0.00"; //DEBE Q.
            queryString += "," + String.valueOf(haber8Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00";
            queryString += ",'Ingreso por " + String.valueOf(tipoIngresoCbx.getValue()) + " " + descripcionTxt.getValue() + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";
        }
        if (cuentaContable9Cbx.getValue() != null) {
            //tercer  ingreso
            queryString += ",(";
            queryString += String.valueOf(empresaCbx.getValue());
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigo9Txt.getValue() + "'";
            queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + proveedorCbx.getContainerProperty(proveedorCbx.getValue(), NOMBRESINCODIGO_PROPERTY).getValue() + "'";
            queryString += ",''";
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += "," + cuentaContable9Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + montoTxt.getDoubleValueDoNotThrow();
            queryString += ",0.00";// DEBE
            queryString += "," + String.valueOf(haber9Txt.getDoubleValueDoNotThrow()); // HABER
            queryString += ",0.00"; //DEBE Q.
            queryString += "," + String.valueOf(haber9Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00";
            queryString += ",'Ingreso por " + String.valueOf(tipoIngresoCbx.getValue()) + " " + descripcionTxt.getValue() + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";
        }
        if (cuentaContable10Cbx.getValue() != null) {
            //tercer  ingreso
            queryString += ",(";
            queryString += String.valueOf(empresaCbx.getValue());
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigo10Txt.getValue() + "'";
            queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + proveedorCbx.getContainerProperty(proveedorCbx.getValue(), NOMBRESINCODIGO_PROPERTY).getValue() + "'";
            queryString += ",''";
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += "," + cuentaContable10Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + montoTxt.getDoubleValueDoNotThrow();
            queryString += ",0.00";// DEBE
            queryString += "," + String.valueOf(haber10Txt.getDoubleValueDoNotThrow()); // HABER
            queryString += ",0.00"; //DEBE Q.
            queryString += "," + String.valueOf(haber10Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00";
            queryString += ",'Ingreso por " + String.valueOf(tipoIngresoCbx.getValue()) + " " + descripcionTxt.getValue() + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";
        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "queryString Ingreso Bancos Reembolso de Anticipo = " + queryString);
//        System.out.println("queryString Ingreso Bancos Reembolsos = " + queryString);

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            if (!variableTemp.isEmpty()) {
                cambiarEstatusToken(codigoPartida);
            }

            Notification.show("Ingreso realizado con exito!", Notification.Type.HUMANIZED_MESSAGE);

            ((IngresoBancosView) (mainUI.getNavigator().getCurrentView())).llenarTablaFactura(String.valueOf(empresaCbx.getValue()));

            close();
        } catch (Exception ex1) {
            System.out.println("Error al insertar un ingreso por reembolso : " + ex1.getMessage());
            ex1.printStackTrace();
            Notification notif = new Notification("HA OCURRIDO UN ERROR DE BASE DE DATOS : " + ex1.getMessage(),
                    Notification.Type.ERROR_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.WARNING);
            notif.show(Page.getCurrent());

            try {
                String emailsTo[] = {"alerta@simpletecno.com"};
                MyEmailMessanger eMail = new MyEmailMessanger();

                eMail.postMail(emailsTo, "Error en SOPDI", "Error en base de datos :  " + this.getClass().getName() + " -->" + ex1.getMessage());
            } catch (MessagingException ex2) {
                Logger.getLogger(SopdiUI.class.getName()).log(Level.SEVERE, null, ex2);
            }
        }

    }

    public void limpiarPartida() {
        montoTxt.setValue(0.00);

        cuentaContable1Cbx.setReadOnly(false);
        cuentaContable1Cbx.clear();
        haber1Txt.setReadOnly(false);
        debe1Txt.setReadOnly(false);
        haber1Txt.setValue(0.00);
        debe1Txt.setValue(0.00);

        cuentaContable2Cbx.setReadOnly(false);
        cuentaContable2Cbx.clear();
        debe2Txt.setReadOnly(false);
        debe2Txt.setValue(0.00);
        haber2Txt.setReadOnly(false);
        haber2Txt.setValue(0.00);

        cuentaContable3Cbx.setReadOnly(false);
        cuentaContable3Cbx.clear();
        debe3Txt.setReadOnly(false);
        debe3Txt.setValue(0.00);
        haber3Txt.setReadOnly(false);
        haber3Txt.setValue(0.00);

        cuentaContable4Cbx.setReadOnly(false);
        cuentaContable4Cbx.clear();
        debe4Txt.setReadOnly(false);
        haber4Txt.setReadOnly(false);
        debe4Txt.setValue(0.00);
        haber4Txt.setValue(0.00);

        cuentaContable5Cbx.setReadOnly(false);
        cuentaContable5Cbx.clear();
        debe5Txt.setReadOnly(false);
        haber5Txt.setReadOnly(false);
        debe5Txt.setValue(0.00);
        haber5Txt.setValue(0.00);

        cuentaContable6Cbx.setReadOnly(false);
        cuentaContable6Cbx.clear();
        debe6Txt.setReadOnly(false);
        haber6Txt.setReadOnly(false);
        debe6Txt.setValue(0.00);
        haber6Txt.setValue(0.00);

        cuentaContable7Cbx.setReadOnly(false);
        cuentaContable7Cbx.clear();
        debe7Txt.setReadOnly(false);
        haber7Txt.setReadOnly(false);
        debe7Txt.setValue(0.00);
        haber7Txt.setValue(0.00);

        cuentaContable8Cbx.setReadOnly(false);
        cuentaContable8Cbx.clear();
        debe8Txt.setReadOnly(false);
        haber8Txt.setReadOnly(false);
        debe8Txt.setValue(0.00);
        haber8Txt.setValue(0.00);

        cuentaContable9Cbx.setReadOnly(false);
        cuentaContable9Cbx.clear();
        debe9Txt.setReadOnly(false);
        haber9Txt.setReadOnly(false);
        debe9Txt.setValue(0.00);
        haber9Txt.setValue(0.00);

        cuentaContable10Cbx.setReadOnly(false);
        cuentaContable10Cbx.clear();
        debe10Txt.setReadOnly(false);
        haber10Txt.setReadOnly(false);
        debe10Txt.setValue(0.00);
        haber10Txt.setValue(0.00);
    }

    public void cambiarEstatusToken(String codigoPartida) {

        try {
            queryString = "UPDATE token SET ";
            queryString += " IdUsuario = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserId();
            queryString += ", Usuario = '" + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserFullName() + "'";
            queryString += ", FechaUsado = current_timestamp";
            queryString += ", CodigoPartida = '" + codigoPartida + "'";
            queryString += ", Estatus = 'UTILIZADO'";
            queryString += " Where Codigo = '" + variableTemp + "'";

            variableTemp = "";

            stQuery.executeUpdate(queryString);

        } catch (Exception e) {
            System.out.println("Error al intentar cambiar estatus token : " + e);
            e.printStackTrace();
        }

    }
}
