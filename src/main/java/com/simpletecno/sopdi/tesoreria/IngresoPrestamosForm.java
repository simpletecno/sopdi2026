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
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.ui.NumberField;

import javax.mail.MessagingException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author user
 */
public class IngresoPrestamosForm extends Window {

    static final String NIT_PROPERTY = "NIT";

    UI mainUI;
    Statement stQuery;
    ResultSet rsRecords;

    VerticalLayout mainLayout;
    HorizontalLayout layoutTitle;
    Label titleLbl;

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

    String queryString;
    String codigoPartida;
    String variableTemp = "";

    Button guardarBtn;
    Button salirBtn;

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public IngresoPrestamosForm(String codigoPartida) {
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

        titleLbl = new Label(empresaId + " " + empresaNombre + " INGRESO A BANCOS PRESTAMOS");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h2_custom");

        layoutTitle.addComponent(titleLbl);
        layoutTitle.setComponentAlignment(titleLbl, Alignment.BOTTOM_RIGHT);

        mainLayout.addComponent(layoutTitle);
        setContent(mainLayout);
        mainLayout.addComponent(crearComponentes());

        generarPartidaPrestamos();

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
        tipoIngresoCbx.addItem("PRESTAMOS");
        tipoIngresoCbx.select("PRESTAMOS");
        tipoIngresoCbx.setReadOnly(true);

        proveedorCbx = new ComboBox("Proveedor o Cliente");
        proveedorCbx.addContainerProperty(NIT_PROPERTY, String.class, "");
        proveedorCbx.setInvalidAllowed(false);
        proveedorCbx.setNewItemsAllowed(false);
        proveedorCbx.setNullSelectionAllowed(false);
        proveedorCbx.setWidth("100%");
        proveedorCbx.setFilteringMode(FilteringMode.CONTAINS);

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
        fechaDt.addValueChangeListener((event) -> {
            if(tipoCambioTxt != null) {
                if (monedaCbx.getValue().equals("DOLARES")) {
                    tipoCambioTxt.setReadOnly(false);
//                    tipoCambioTxt.setValue(Float.toString(((SopdiUI)UI.getCurrent()).sessionInformation.getFltExchangeRate()));
                    tipoCambioTxt.setReadOnly(false);
                    tipoCambioTxt.setValue(((SopdiUI) UI.getCurrent()).getTasaCambioDelDia(Utileria.getFechaYYYYMMDD_1(fechaDt.getValue())));
                    tipoCambioTxt.setReadOnly(true);
                } else {
                    tipoCambioTxt.setReadOnly(false);
                    tipoCambioTxt.setValue(1.00);
                    tipoCambioTxt.setReadOnly(true);
                }
            }
        });

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
        montoTxt.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(Property.ValueChangeEvent event) {
               debe1Txt.setValue(montoTxt.getDoubleValueDoNotThrow());
               haber2Txt.setValue(montoTxt.getDoubleValueDoNotThrow());
            }
        });

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
//                    tipoCambioTxt.setValue(Float.toString(((SopdiUI)UI.getCurrent()).sessionInformation.getFltExchangeRate()));
                    tipoCambioTxt.setReadOnly(false);
                    tipoCambioTxt.setValue(((SopdiUI) UI.getCurrent()).getTasaCambioDelDia(Utileria.getFechaYYYYMMDD_1(fechaDt.getValue())));
                    tipoCambioTxt.setReadOnly(true);
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
        cuentaContable4Cbx.setReadOnly(true);

        cuentaContable5Cbx = new ComboBox();
        cuentaContable5Cbx.setWidth("24em");
        cuentaContable5Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable5Cbx.setInvalidAllowed(false);
        cuentaContable5Cbx.setNewItemsAllowed(false);
        cuentaContable5Cbx.setReadOnly(true);

        llenarComboCuentaContable();
        llenarComboProveedor(); // proveedores

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
        haber4Txt.setReadOnly(true);

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
        haber5Txt.setReadOnly(true);

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
        debe4Txt.setReadOnly(true);

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
        debe5Txt.setReadOnly(true);

        guardarBtn = new Button("Grabar");
        guardarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        guardarBtn.setIcon(FontAwesome.SAVE);
        guardarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                ingresarPrestamos();
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

    public void llenarComboProveedor() {
        queryString = " SELECT * FROM proveedor_empresa ";
        queryString += " WHERE Inhabilitado = 0 ";
        queryString += " AND EsBanco = 1";
        queryString += " AND IdEmpresa = " + empresaId;
        queryString += " ORDER BY Nombre";

        proveedorCbx.removeAllItems();

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) {  /// encontrado
                proveedorCbx.addItem(rsRecords.getString("IDProveedor"));
                proveedorCbx.setItemCaption(rsRecords.getString("IDProveedor"), rsRecords.getString("Nombre"));
                proveedorCbx.getItem(rsRecords.getString("IDProveedor")).getItemProperty(NIT_PROPERTY).setValue(rsRecords.getString("NIT"));
            }
            
            proveedorCbx.select(proveedorCbx.getItemIds().iterator().next());

        } catch (Exception ex1) {
            System.out.println("Error al listar de clientes (tabla proveedores) " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void llenarComboCuentaContable() {

        queryString = " SELECT * FROM contabilidad_nomenclatura_empresa";
        queryString += " WHERE Estatus = 'HABILITADA'";
        queryString += " AND IdEmpresa = " + empresaId;
        queryString += " ORDER BY N5";

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

    public void generarPartidaPrestamos() {

            monedaCbx.select("QUETZALES");
            cuentaContable1Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaLocal());
            haber1Txt.setReadOnly(true);
            cuentaContable2Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getPrestamos());
            debe2Txt.setReadOnly(true);

            this.proveedorCbx.setCaption("Proveedor");
            llenarComboProveedor();
    }

    public void ingresarPrestamos() {
        
        try {
            Utileria utileria = new Utileria();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
 
            Date fechaInicial = dateFormat.parse(utileria.getFecha());            
            Date fechaFinal = dateFormat.parse(utileria.getFechaYYYYMMDD(fechaDt.getValue()));

            int dias=(int) ((fechaInicial.getTime()-fechaFinal.getTime())/86400000);
             
//            System.out.println("Hay "+dias+" dias de diferencia");
            
            if(dias > 30){                              
                
               if (((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserToken().isEmpty()) {
                    ValidarTokenForm validarTokenForm = new ValidarTokenForm(false);
                    UI.getCurrent().addWindow(validarTokenForm);
                    validarTokenForm.center();                                                                     
                    return;
               }else{     
                   variableTemp = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserToken();                   
                   ((SopdiUI) UI.getCurrent()).sessionInformation.setStrUserToken("");
               }                
            }            
        } catch (Exception e) {
            System.out.println("Error validar fechas " + e);
            e.printStackTrace();            
        }
        
        if (((SopdiUI) UI.getCurrent()).esMesCerrado(empresaId, Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()))) {
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
        if (debe1Txt.getDoubleValueDoNotThrow() != montoTxt.getDoubleValueDoNotThrow()) {
            Notification.show("La partida esta descuadrada. Por favor, revise las cantidades..", Notification.Type.ERROR_MESSAGE);
            debe1Txt.focus();
            return;
        }

        if (cuentaContable3Cbx.getValue() != null) {
            if ((debe3Txt.getDoubleValueDoNotThrow() + haber3Txt.getDoubleValueDoNotThrow()) == 0) {
                Notification.show("Por favor, seleccione una cuenta del HABER..", Notification.Type.ERROR_MESSAGE);
                debe3Txt.focus();
                return;
            }
        }

        if ((haber2Txt.getDoubleValueDoNotThrow() + haber3Txt.getDoubleValueDoNotThrow()) != montoTxt.getDoubleValueDoNotThrow()) {
            Notification.show("La partida esta descuadrada. Por favor, revise las cantidades.", Notification.Type.ERROR_MESSAGE);
            haber2Txt.focus();
            return;
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

       // if (descripcionTxt.getValue().isEmpty()) {
       //     Notification.show("Por favor, Ingrese una descripción..", Notification.Type.ERROR_MESSAGE);
       //     descripcionTxt.focus();
       //     return;
       // }

        String fecha = Utileria.getFechaYYYYMMDD_1(fechaDt.getValue());
        String ultimoEncontado;
        String dia = fecha.substring(8, 10);
        String mes = fecha.substring(5, 7);
        String año = fecha.substring(0, 4);

        codigoPartida = empresaId + año + mes + dia + "5";

        queryString = "SELECT codigoPartida FROM contabilidad_partida ";
        queryString += " WHERE codigoPartida LIKE '" + codigoPartida + "%'";
        queryString += " ORDER BY codigoPartida DESC ";

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

        queryString = "INSERT INTO contabilidad_partida (IdEmpresa, Estatus, CodigoPartida, ";
        queryString += " TipoDocumento, Fecha, IdProveedor, NombreProveedor, SerieDocumento,";
        queryString += " NumeroDocumento, IdNomenclatura, MonedaDocumento, MontoDocumento, Debe, Haber,";
        queryString += " DebeQuetzales, HaberQuetzales, TipoCambio, Saldo, Descripcion,";
        queryString += " CreadoUsuario, CreadoFechaYHora)";
        queryString += " Values ";
        queryString += "(";
        queryString += empresaId;
        queryString += ",'INGRESADO'";
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
        queryString += empresaId;
        queryString += ",'INGRESADO'";
        queryString += ",'" + codigoPartida + "'";
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
            queryString += empresaId;
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
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

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "queryString Ingreso Bancos Prestamos = " + queryString);

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);
            
            if(!variableTemp.isEmpty()){
                cambiarEstatusToken(codigoPartida);
            }

            Notification.show("Ingreso realizado con exito!", Notification.Type.HUMANIZED_MESSAGE);

            ((IngresoBancosView) (mainUI.getNavigator().getCurrentView())).llenarTablaFactura(empresaId);

            close();
        } catch (Exception ex1) {
            System.out.println("Error al insertar un prestamo : " + ex1.getMessage());
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
    
    public void cambiarEstatusToken(String codigoPartida){
        
        try {
            queryString = "UPDATE token SET ";
            queryString += " IdUsuario = " +((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserId();
            queryString += ", Usuario = '" + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserFullName() + "'";
            queryString += ", FechaUsado = current_timestamp";
            queryString += ", CodigoPartida = '" + codigoPartida +"'";
            queryString += ", Estatus = 'UTILIZADO'";
            queryString += " WHERE Codigo = '" + variableTemp +"'";
            
            variableTemp = "";

            stQuery.executeUpdate(queryString);
            
        } catch (Exception e) {
            System.out.println("Error al intentar cambiar estatus token : " + e);
            e.printStackTrace();
        }                       
    }
}
