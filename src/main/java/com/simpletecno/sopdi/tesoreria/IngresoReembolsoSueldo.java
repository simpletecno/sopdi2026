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
import com.vaadin.data.Property;
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
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author user
 */
public class IngresoReembolsoSueldo extends Window {

    static final String NIT_PROPERTY = "NIT";
    static final String NOMBRESINCODIGO_PROPERTY = "NSC";

    UI mainUI;
    Statement stQuery, stQuerySueldos;
    ResultSet rsRecords, rsRecordsSueldos;

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

    TextField codigo1Txt;
    TextField codigo2Txt;
    TextField codigo3Txt;
    TextField codigo4Txt;
    TextField codigo5Txt;

    String codigoPartida;
    String queryString;
    String variableTemp = "";

    Button guardarBtn;
    Button salirBtn;

    BigDecimal totalDebe;
    BigDecimal totalHaber;

    public IndexedContainer sueldosContainer = new IndexedContainer();
    Grid sueldosGrid;
    static final String CODIGO_PARTIDA_PROPERTY = "Codigo Partida";
    static final String CODIGO_CC_PROPERTY = "Codigo CC";
    static final String NUMERO_DOCUMENTO_PROPERTY = "# Documento";
    static final String MONEDA_DOCUMENTO_PROPERTY = "Moneda";
    static final String DEBE_PROPERTY = "Debe";
    static final String HABER_PROPERTY = "Haber";
    static final String DEBE_QUETZALES_PROPERTY = "Debe Q.";
    static final String HABER_QUETZALES_PROPERTY = "Haber Q.";
    static final String SALDO_DOCUMENTO_PROPERTY = "Saldo";

    public IngresoReembolsoSueldo() {

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

        titleLbl = new Label("INGRESO A BANCOS REEMBOLSO SUELDO");
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

        generarPartidaReembolsoSueldo();

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
        tipoIngresoCbx.addItem("REEMBOLSO DE SUELDOS");
        tipoIngresoCbx.select("REEMBOLSO DE SUELDOS");
        tipoIngresoCbx.setReadOnly(true);

        proveedorCbx = new ComboBox("Proveedor o Cliente");
        proveedorCbx.addContainerProperty(NIT_PROPERTY, String.class, "");
        proveedorCbx.setInvalidAllowed(false);
        proveedorCbx.setNewItemsAllowed(false);
        proveedorCbx.setNullSelectionAllowed(false);
        proveedorCbx.setWidth("100%");
        proveedorCbx.setFilteringMode(FilteringMode.CONTAINS);
        proveedorCbx.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (proveedorCbx.getValue() != null || !proveedorCbx.getValue().equals("0")) {
                    llenarTablaReembolsoSueldos();
                }
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

        numeroTxt = new TextField("# Depósito/Transferencia:");
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

        sueldosContainer.addContainerProperty(CODIGO_PARTIDA_PROPERTY, String.class, null);
        sueldosContainer.addContainerProperty(CODIGO_CC_PROPERTY, String.class, null);
        sueldosContainer.addContainerProperty(NUMERO_DOCUMENTO_PROPERTY, String.class, null);
        sueldosContainer.addContainerProperty(MONEDA_DOCUMENTO_PROPERTY, String.class, null);
        sueldosContainer.addContainerProperty(DEBE_PROPERTY, String.class, null);
        sueldosContainer.addContainerProperty(HABER_PROPERTY, String.class, null);
        sueldosContainer.addContainerProperty(DEBE_QUETZALES_PROPERTY, String.class, null);
        sueldosContainer.addContainerProperty(HABER_QUETZALES_PROPERTY, String.class, null);
        sueldosContainer.addContainerProperty(SALDO_DOCUMENTO_PROPERTY, String.class, null);

        sueldosGrid = new Grid("", sueldosContainer);
        sueldosGrid.setWidth("100%");
        sueldosGrid.setImmediate(true);
        sueldosGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        sueldosGrid.setDescription("Seleccione uno o varios registros del mismo proveedor.");
        sueldosGrid.setHeightMode(HeightMode.ROW);
        sueldosGrid.setHeightByRows(5);
        sueldosGrid.setResponsive(true);
        sueldosGrid.setEditorBuffered(false);

        sueldosGrid.addSelectionListener(new SelectionEvent.SelectionListener() {
            @Override
            public void select(SelectionEvent event) {
                if (sueldosGrid.getSelectedRows() != null) {

                    Object gridItem;
                    String moneda = "";
                    double montoBancos = 0;

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
                    moneda = String.valueOf(sueldosGrid.getContainerDataSource().getItem(gridItem).getItemProperty(MONEDA_DOCUMENTO_PROPERTY).getValue());
                    montoBancos = Double.valueOf(String.valueOf(sueldosGrid.getContainerDataSource().getItem(gridItem).getItemProperty(SALDO_DOCUMENTO_PROPERTY).getValue()));
                    limpiarPartida();

                    while (iter.hasNext()) {

                        if (!moneda.equals(String.valueOf(sueldosGrid.getContainerDataSource().getItem(gridItem).getItemProperty(MONEDA_DOCUMENTO_PROPERTY).getValue()))) {
                            Notification.show("SOLO SE PUEDEN PAGAR VARIAS FACTURAS DEL MISMO PROVEEDOR Y DE LA MISMA MONEDA, REVISE!", Notification.Type.WARNING_MESSAGE);
                            sueldosGrid.deselect(gridItem);
                            return;
                        }
                        gridItem = iter.next();
                        montoBancos += Double.valueOf(String.valueOf(sueldosGrid.getContainerDataSource().getItem(gridItem).getItemProperty(SALDO_DOCUMENTO_PROPERTY).getValue()));

                    }
                    limpiarPartida();

                    if (monedaCbx.getValue() == "DOLARES") {
                        cuentaContable1Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaExtranjera());
                        debe1Txt.setValue(montoBancos);
                    } else {
                        cuentaContable1Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaLocal());
                        debe1Txt.setValue(montoBancos);
                    }

                    Iterator iter2 = sueldosGrid.getSelectedRows().iterator();

                    while (iter2.hasNext()) {

                        Object gridItem2 = iter2.next();
                        if (cuentaContable2Cbx.getValue() == null) {
                            cuentaContable2Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getAnticiposSueldos());
                            haber2Txt.setValue(String.valueOf(sueldosGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(SALDO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", ""));
                            codigo2Txt.setValue(String.valueOf(sueldosGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(CODIGO_CC_PROPERTY).getValue()));

                        } else if (cuentaContable3Cbx.getValue() == null) {
                            cuentaContable3Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getAnticiposSueldos());
                            haber3Txt.setValue(String.valueOf(sueldosGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(SALDO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", ""));
                            codigo3Txt.setValue(String.valueOf(sueldosGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(CODIGO_CC_PROPERTY).getValue()));

                        } else if (cuentaContable4Cbx.getValue() == null) {
                            cuentaContable4Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getAnticiposSueldos());
                            haber4Txt.setValue(String.valueOf(sueldosGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(SALDO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", ""));
                            codigo4Txt.setValue(String.valueOf(sueldosGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(CODIGO_CC_PROPERTY).getValue()));

                        } else if (cuentaContable5Cbx.getValue() == null) {
                            cuentaContable5Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getAnticiposSueldos());
                            haber5Txt.setValue(String.valueOf(sueldosGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(SALDO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", ""));
                            codigo5Txt.setValue(String.valueOf(sueldosGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(CODIGO_CC_PROPERTY).getValue()));
                        }

                    }
                }
            }
        });

        sueldosGrid.getColumn(CODIGO_PARTIDA_PROPERTY).setHidable(true).setHidden(true);
        sueldosGrid.getColumn(CODIGO_CC_PROPERTY).setHidable(true).setHidden(true);
        sueldosGrid.getColumn(DEBE_QUETZALES_PROPERTY).setHidable(true).setHidden(true);
        sueldosGrid.getColumn(HABER_QUETZALES_PROPERTY).setHidable(true).setHidden(true);

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

        guardarBtn = new Button("Grabar");
        guardarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        guardarBtn.setIcon(FontAwesome.SAVE);
        guardarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                ingresarReembolsos(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getAnticiposSueldos());
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

        layoutHorizontal6.addComponents(salirBtn, guardarBtn);
        layoutHorizontal6.setComponentAlignment(guardarBtn, Alignment.BOTTOM_CENTER);
        layoutHorizontal6.setComponentAlignment(salirBtn, Alignment.BOTTOM_CENTER);

        rightVerticalLayout.addComponent(sueldosGrid);
        rightVerticalLayout.setComponentAlignment(sueldosGrid, Alignment.MIDDLE_CENTER);
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

        horizontalLayout.addComponents(leftVerticalLayout, rightVerticalLayout);

        return horizontalLayout;

    }

    public void llenarTablaReembolsoSueldos() {

        sueldosContainer.removeAllItems();

        queryString = " SELECT contabilidad_partida.codigoPartida, contabilidad_partida.CodigoCc, contabilidad_partida.IdNomenclatura,";
        queryString += " contabilidad_nomenclatura.NoCuenta, contabilidad_partida.MonedaDocumento,";
        queryString += " contabilidad_partida.IdEmpresa , contabilidad_partida.IdProveedor, contabilidad_partida.NumeroDocumento,";
        queryString += " SUM(contabilidad_partida.Debe) TOTALDEBE, SUM(contabilidad_partida.Haber) TOTALHABER,";
        queryString += " SUM(contabilidad_partida.DebeQuetzales) TOTALDEBEQ, SUM(contabilidad_partida.HaberQuetzales) TOTALHABERQ,";
        queryString += " SUM(DEBE - HABER) TOTALSALDO, SUM(DebeQuetzales - HaberQuetzales) TOTALSALDOQ";
        queryString += " FROM contabilidad_partida";
        queryString += " INNER JOIN contabilidad_nomenclatura ON contabilidad_nomenclatura.IdNomenclatura = contabilidad_partida.IdNomenclatura";
        queryString += " And contabilidad_partida.IdProveedor = " + proveedorCbx.getValue();
        queryString += " And contabilidad_partida.IdEmpresa = " + empresaCbx.getValue();
        queryString += " AND (trim(contabilidad_partida.CodigoCC) <> '' AND contabilidad_partida.CodigoCC <> '0')";
        queryString += " AND contabilidad_nomenclatura.IdNomenclatura = " + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getAnticiposSueldos();
        queryString += " GROUP BY contabilidad_partida.CodigoCC, contabilidad_partida.IdNomenclatura";
        queryString += " HAVING TOTALSALDO > 0";

        System.out.println("Query para mostrar anticipos " + queryString);

        try {
            stQuerySueldos = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecordsSueldos = stQuerySueldos.executeQuery(queryString);

            if (rsRecordsSueldos.next()) { //  encontrado
                do {
                    Object itemId = sueldosContainer.addItem();

                    sueldosContainer.getContainerProperty(itemId, CODIGO_PARTIDA_PROPERTY).setValue(rsRecordsSueldos.getString("CodigoPartida"));
                    sueldosContainer.getContainerProperty(itemId, CODIGO_CC_PROPERTY).setValue(rsRecordsSueldos.getString("CodigoCC"));
                    sueldosContainer.getContainerProperty(itemId, NUMERO_DOCUMENTO_PROPERTY).setValue(rsRecordsSueldos.getString("NumeroDocumento"));
                    sueldosContainer.getContainerProperty(itemId, MONEDA_DOCUMENTO_PROPERTY).setValue(rsRecordsSueldos.getString("MonedaDocumento"));
                    sueldosContainer.getContainerProperty(itemId, DEBE_PROPERTY).setValue(rsRecordsSueldos.getString("TOTALDEBE"));
                    sueldosContainer.getContainerProperty(itemId, HABER_PROPERTY).setValue(rsRecordsSueldos.getString("TOTALHABER"));
                    sueldosContainer.getContainerProperty(itemId, DEBE_QUETZALES_PROPERTY).setValue(rsRecordsSueldos.getString("TOTALDEBEQ"));
                    sueldosContainer.getContainerProperty(itemId, HABER_QUETZALES_PROPERTY).setValue(rsRecordsSueldos.getString("TOTALHABERQ"));
                    sueldosContainer.getContainerProperty(itemId, SALDO_DOCUMENTO_PROPERTY).setValue(rsRecordsSueldos.getString("TOTALSALDO"));

                } while (rsRecordsSueldos.next());
            }

        } catch (Exception ex) {
            System.out.println("Error al listar tabla de anticipos sueldo : " + ex);
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
//2022-03-29 ja        queryString = " SELECT DISTINCT prov.* ";
//        queryString += " FROM proveedor prov";
//        queryString += " INNER JOIN contabilidad_cuentas_por_liquidar ctas ON ctas.IdProveedor = prov.IdProveedor";
//        queryString += " WHERE prov.Inhabilitado = 0 ";
//        queryString += " AND ctas.IdNomenclatura IN (15,16)";
////        queryString += " AND prov.EsCliente = 1";
//        queryString += " Order By prov.Nombre";

        queryString = " SELECT * ";
        queryString += " FROM proveedor ";
        queryString += " WHERE Inhabilitado = 0 ";
        queryString += " AND EsPlanilla = 1";
        queryString += " Order By Nombre";

        proveedorCbx.removeAllItems();

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) {  /// encontrado
                proveedorCbx.addItem(rsRecords.getString("IDProveedor"));
                proveedorCbx.setItemCaption(rsRecords.getString("IDProveedor"), rsRecords.getString("Nombre"));
                proveedorCbx.getItem(rsRecords.getString("IDProveedor")).getItemProperty(NIT_PROPERTY).setValue(rsRecords.getString("NIT"));
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

            }

        } catch (Exception ex1) {
            System.out.println("Error al combo cuentas contables: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void generarPartidaReembolsoSueldo() {

        monedaCbx.select("QUETZALES");
        cuentaContable1Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaLocal());
        cuentaContable2Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getAnticiposSueldos());
        cuentaContable3Cbx.clear();
        debe2Txt.setReadOnly(true);
        debe3Txt.setReadOnly(true);
        haber1Txt.setReadOnly(true);

        this.proveedorCbx.setCaption("Proveedor");
        llenarComboProveedor();
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
        ).setScale(2, BigDecimal.ROUND_HALF_UP);

        totalHaber = new BigDecimal(haber1Txt.getDoubleValueDoNotThrow()
                + haber2Txt.getDoubleValueDoNotThrow() + haber3Txt.getDoubleValueDoNotThrow()
                + haber4Txt.getDoubleValueDoNotThrow() + haber5Txt.getDoubleValueDoNotThrow()
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

        queryString = " Insert Into contabilidad_partida (IdEmpresa, Estatus, CodigoPartida, CodigoCC,";
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
        queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
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
        queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
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
            queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
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

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "queryString Ingreso Bancos ReembolsoSueldos = " + queryString);

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