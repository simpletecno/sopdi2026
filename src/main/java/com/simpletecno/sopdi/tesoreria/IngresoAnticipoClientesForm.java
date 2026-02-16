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
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author user
 */
public class IngresoAnticipoClientesForm extends Window {

    static final String NIT_PROPERTY = "NIT";
    static final String NOMBRESINCODIGO_PROPERTY = "NSC";

    UI mainUI;

    Statement stQuery;
    ResultSet rsRecords;

    VerticalLayout mainLayout;
    HorizontalLayout layoutTitle;
    ComboBox empresaCbx;
    Label titleLbl;

    ComboBox proveedorCbx;
    ComboBox medioCbx;
    DateField fechaDt;
    TextField numeroTxt;
    NumberField montoTxt;
    ComboBox monedaCbx;
    NumberField tipoCambioTxt;
    TextField descripcionTxt;
    ComboBox tipoEngancheCbx;
    TextField referenciaTxt;

    ComboBox cuentaContable1Cbx;
    ComboBox cuentaContable2Cbx;
    ComboBox cuentaContable3Cbx;

    NumberField debe1Txt;
    NumberField debe2Txt;
    NumberField debe3Txt;

    NumberField haber1Txt;
    NumberField haber2Txt;
    NumberField haber3Txt;

    NumberField debe1QTxt;
    NumberField debe2QTxt;
    NumberField debe3QTxt;

    NumberField haber1QTxt;
    NumberField haber2QTxt;
    NumberField haber3QTxt;

    Button guardarBtn;
    Button salirBtn;

    String queryString;
    String codigoPartida;
    String variableTemp = "";

    boolean cuentaContableModificada = false; // Uso para recuperar lo modificado en caso de ser una cuenta equivalente

    public IngresoAnticipoClientesForm() {
        this.codigoPartida = codigoPartida;

        this.mainUI = UI.getCurrent();
        setResponsive(true);
        setWidth("95%");
        setHeight("95%");
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

        titleLbl = new Label("INGRESO A BANCOS ANTICIPOS CLIENTE");
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

        generarPartidaEnganches();
    }

    public HorizontalLayout crearComponentes() {

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.addStyleName("rcorners2");
        horizontalLayout.setSpacing(true);
        horizontalLayout.setMargin(true);
        horizontalLayout.setSizeFull();

        VerticalLayout leftVerticalLayout = new VerticalLayout();
        leftVerticalLayout.addStyleName("rcorners3");
        leftVerticalLayout.setWidth("60%");
//        leftVerticalLayout.setSizeUndefined();
        leftVerticalLayout.setSpacing(true);
        leftVerticalLayout.setMargin(true);

        VerticalLayout rightVerticalLayout = new VerticalLayout();
        rightVerticalLayout.addStyleName("rcorners3");
        rightVerticalLayout.setWidth("100%");
//        rightVerticalLayout.setSizeUndefined();
        rightVerticalLayout.setSpacing(true);
        rightVerticalLayout.setMargin(true);

        horizontalLayout.addComponents(leftVerticalLayout, rightVerticalLayout);
        horizontalLayout.setComponentAlignment(leftVerticalLayout, Alignment.MIDDLE_LEFT);
        horizontalLayout.setComponentAlignment(rightVerticalLayout, Alignment.MIDDLE_RIGHT);

//        horizontalLayout.setExpandRatio(leftVerticalLayout, 3);
//        horizontalLayout.setExpandRatio(rightVerticalLayout, 3);

        proveedorCbx = new ComboBox("Cliente");
        proveedorCbx.addContainerProperty(NIT_PROPERTY, String.class, "");
        proveedorCbx.addContainerProperty(NOMBRESINCODIGO_PROPERTY, String.class, "");
        proveedorCbx.setInvalidAllowed(false);
        proveedorCbx.setNewItemsAllowed(false);
        proveedorCbx.setNullSelectionAllowed(false);
        proveedorCbx.setWidth("100%");
        proveedorCbx.setFilteringMode(FilteringMode.CONTAINS);
        proveedorCbx.addValueChangeListener((Property.ValueChangeEvent event) -> {
            if (proveedorCbx.getValue() != null) {
                if(cuentaContable2Cbx != null){
                    Object idProveedor = proveedorCbx.getValue();

                    if(cuentaContableModificada){
                        ((SopdiUI) mainUI).sessionInformation.getEmpresaCuentasEquivalentesHelper().restoreAll(cuentaContable2Cbx);
                        cuentaContableModificada = false;
                    }
                    Set<Long> set = ((SopdiUI)mainUI).sessionInformation.getEmpresaCuentasEquivalentesHelper().getNomenclaturas_e(idProveedor);
                    if(!set.isEmpty() && !cuentaContableModificada) {
                        ((SopdiUI) mainUI).sessionInformation.getEmpresaCuentasEquivalentesHelper().change(
                                cuentaContable2Cbx,
                                set
                        );
                        cuentaContableModificada = true;
                    }
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
        montoTxt.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(Property.ValueChangeEvent event) {
                cambioQuetzales();
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
        monedaCbx.setWidth("100%");
        monedaCbx.addValueChangeListener((event) -> {
            if (cuentaContable1Cbx.getValue() != null) {
                if (monedaCbx.getValue().equals("DOLARES")) {
                    if(((SopdiUI) mainUI).cuentasContablesDefault.getBancosMonedaExtranjera() == null) {
                        Notification.show("NO HAY CUENTA CONTABLE PARA MONEDA EXTRANJERA, REVISE LA NOMENCLATURA CONTABLE.", Notification.Type.WARNING_MESSAGE);
                        monedaCbx.select("QUETZALES");
                    }
                    else {
                        cuentaContable1Cbx.select(((SopdiUI) mainUI).cuentasContablesDefault.getBancosMonedaExtranjera());
                        tipoCambioTxt.setValue(Float.toString(((SopdiUI) UI.getCurrent()).sessionInformation.getFltExchangeRate()));
                    }
                    debe3Txt.setVisible(false);
                    haber3Txt.setVisible(false);
                    debe3QTxt.setVisible(false);
                    haber3QTxt.setVisible(false);
                    cuentaContable3Cbx.setVisible(false);
                } else { // QUETZALES
                    cuentaContable1Cbx.select(((SopdiUI) mainUI).cuentasContablesDefault.getBancosMonedaLocal());
                    tipoCambioTxt.setValue(1.0);
                    debe3Txt.setVisible(true);
                    haber3Txt.setVisible(true);
                    debe3QTxt.setVisible(true);
                    haber3QTxt.setVisible(true);
                    cuentaContable3Cbx.setVisible(true);
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
        tipoCambioTxt.addValueChangeListener(valueChangeEvent -> {
            cambioQuetzales();
        });

        tipoEngancheCbx = new ComboBox("Tipo de Anticipo :");
        tipoEngancheCbx.addItem("ORDINARIO");
        tipoEngancheCbx.addItem("ORDEN DE CAMBIO");
        tipoEngancheCbx.setVisible(false);
        tipoEngancheCbx.setWidth("100%");
        tipoEngancheCbx.setInvalidAllowed(false);
        tipoEngancheCbx.setNewItemsAllowed(false);
        tipoEngancheCbx.setNullSelectionAllowed(false);
        tipoEngancheCbx.select("ORDINARIO");

        referenciaTxt = new TextField("Referencia :");
        referenciaTxt.setWidth("100%");
        referenciaTxt.setVisible(false);

        descripcionTxt = new TextField("Descripción :");
        descripcionTxt.setWidth("100%");
        descripcionTxt.setVisible(false);

        leftVerticalLayout.addComponent(proveedorCbx);
        leftVerticalLayout.addComponent(medioCbx);
        leftVerticalLayout.addComponent(fechaDt);
        leftVerticalLayout.addComponent(numeroTxt);
        leftVerticalLayout.addComponent(montoTxt);
        leftVerticalLayout.addComponent(monedaCbx);
        leftVerticalLayout.addComponent(tipoCambioTxt);
        leftVerticalLayout.addComponent(descripcionTxt);
//2023-05-11        leftVerticalLayout.addComponent(tipoEngancheCbx);
        leftVerticalLayout.addComponent(referenciaTxt);

        HorizontalLayout layoutHorizontal1 = new HorizontalLayout();
        layoutHorizontal1.setResponsive(true);
        layoutHorizontal1.setSpacing(true);
        layoutHorizontal1.setWidth("100%");

        HorizontalLayout layoutHorizontal2 = new HorizontalLayout();
        layoutHorizontal2.setResponsive(true);
        layoutHorizontal2.setSpacing(true);
        layoutHorizontal2.setWidth("100%");

        HorizontalLayout layoutHorizontal3 = new HorizontalLayout();
        layoutHorizontal3.setResponsive(true);
        layoutHorizontal3.setSpacing(true);
        layoutHorizontal3.setWidth("100%");

        HorizontalLayout layoutHorizontal4 = new HorizontalLayout();
        layoutHorizontal4.setResponsive(true);
        layoutHorizontal4.setSpacing(true);
        layoutHorizontal4.setWidth("100%");

        HorizontalLayout layoutHorizontal5 = new HorizontalLayout();
        layoutHorizontal5.setResponsive(true);
        layoutHorizontal5.setSpacing(true);
        layoutHorizontal5.setWidth("100%");

        HorizontalLayout layoutHorizontal6 = new HorizontalLayout();
        layoutHorizontal6.setResponsive(true);
        layoutHorizontal6.setSpacing(true);
        layoutHorizontal6.setWidth("100%");

        cuentaContable1Cbx = new ComboBox("Cuentas contables :");
//        cuentaContable1Cbx.setWidth("24em");
        cuentaContable1Cbx.setWidth("100%");
        cuentaContable1Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable1Cbx.setInvalidAllowed(false);
        cuentaContable1Cbx.setNewItemsAllowed(false);

        cuentaContable2Cbx = new ComboBox();
//        cuentaContable2Cbx.setWidth("24em");
        cuentaContable2Cbx.setWidth("100%");
        cuentaContable2Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable2Cbx.setInvalidAllowed(false);
        cuentaContable2Cbx.setNewItemsAllowed(false);

        cuentaContable3Cbx = new ComboBox();
//        cuentaContable3Cbx.setWidth("24em");
        cuentaContable3Cbx.setWidth("100%");
        cuentaContable3Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable3Cbx.setInvalidAllowed(false);
        cuentaContable3Cbx.setNewItemsAllowed(false);

        llenarComboCuentaContable();
        llenarComboProveedor(); // clientes

        debe1Txt = new NumberField("DEBE $. : ");
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
//        debe1Txt.setWidth("7em");
        debe1Txt.setWidth("100%");
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
//        debe2Txt.setWidth("7em");
        debe2Txt.setWidth("100%");
        debe2Txt.setValue(0.00);
        debe2Txt.setEnabled(false);

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
//        debe3Txt.setWidth("7em");
        debe3Txt.setWidth("100%");
        debe3Txt.setValue(0.00);

        debe1QTxt = new NumberField("DEBE Q. : ");
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
//        debe1QTxt.setWidth("7em");
        debe1QTxt.setWidth("100%");
        debe1QTxt.setValue(0.00);

        debe2QTxt = new NumberField();
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
//        debe2QTxt.setWidth("7em");
        debe2QTxt.setWidth("100%");
        debe2QTxt.setValue(0.00);
        debe2QTxt.setEnabled(false);

        debe3QTxt = new NumberField();
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
//        debe3QTxt.setWidth("7em");
        debe3QTxt.setWidth("100%");
        debe3QTxt.setValue(0.00);

        haber1Txt = new NumberField("HABER $. : ");
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
//        haber1Txt.setWidth("7em");
        haber1Txt.setWidth("100%");
        haber1Txt.setValue(0.00);
        haber1Txt.setEnabled(false);

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
//        haber2Txt.setWidth("7em");
        haber2Txt.setWidth("100%");
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
//        haber3Txt.setWidth("7em");
        haber3Txt.setWidth("100%");
        haber3Txt.setValue(0.00);

        haber1QTxt = new NumberField("HABER Q. : ");
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
//        haber1QTxt.setWidth("7em");
        haber1QTxt.setWidth("100%");
        haber1QTxt.setValue(0.00);
        haber1QTxt.setEnabled(false);

        haber2QTxt = new NumberField();
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
//        haber2QTxt.setWidth("7em");
        haber2QTxt.setWidth("100%");
        haber2QTxt.setValue(0.00);

        haber3QTxt = new NumberField();
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
//        haber3QTxt.setWidth("7em");
        haber3QTxt.setWidth("100%");
        haber3QTxt.setValue(0.00);

        guardarBtn = new Button("Grabar");
        guardarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        guardarBtn.setIcon(FontAwesome.SAVE);
        guardarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                ingresarEnganches();
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
        layoutHorizontal1.addComponent(debe1QTxt);
        layoutHorizontal1.addComponent(haber1QTxt);

        layoutHorizontal1.setExpandRatio(cuentaContable1Cbx, 3.0f);
        layoutHorizontal1.setExpandRatio(debe1Txt, 1.0f);
        layoutHorizontal1.setExpandRatio(haber1Txt, 1.0f);
        layoutHorizontal1.setExpandRatio(debe1QTxt, 1.0f);
        layoutHorizontal1.setExpandRatio(haber1QTxt, 1.0f);

        layoutHorizontal2.addComponent(cuentaContable2Cbx);
        layoutHorizontal2.addComponent(debe2Txt);
        layoutHorizontal2.addComponent(haber2Txt);
        layoutHorizontal2.addComponent(debe2QTxt);
        layoutHorizontal2.addComponent(haber2QTxt);

        layoutHorizontal2.setExpandRatio(cuentaContable2Cbx, 3.0f);
        layoutHorizontal2.setExpandRatio(debe2Txt, 1.0f);
        layoutHorizontal2.setExpandRatio(haber2Txt, 1.0f);
        layoutHorizontal2.setExpandRatio(debe2QTxt, 1.0f);
        layoutHorizontal2.setExpandRatio(haber2QTxt, 1.0f);

        layoutHorizontal3.addComponent(cuentaContable3Cbx);
        layoutHorizontal3.addComponent(debe3Txt);
        layoutHorizontal3.addComponent(haber3Txt);
        layoutHorizontal3.addComponent(debe3QTxt);
        layoutHorizontal3.addComponent(haber3QTxt);

        layoutHorizontal3.setExpandRatio(cuentaContable3Cbx, 3.0f);
        layoutHorizontal3.setExpandRatio(debe3Txt, 1.0f);
        layoutHorizontal3.setExpandRatio(haber3Txt, 1.0f);
        layoutHorizontal3.setExpandRatio(debe3QTxt, 1.0f);
        layoutHorizontal3.setExpandRatio(haber3QTxt, 1.0f);

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

        horizontalLayout.setExpandRatio(leftVerticalLayout, 2.0f);
        horizontalLayout.setExpandRatio(rightVerticalLayout, 3.0f);

        return horizontalLayout;

    }

    private void cambioQuetzales(){
        if (monedaCbx.getValue().equals("DOLARES")) {
            debe1Txt.setValue(montoTxt.getDoubleValueDoNotThrow());
            haber1Txt.setValue(0.00);
            debe2Txt.setValue(0.00);
            debe3Txt.setValue(0.00);  //diferencial cambiario
            haber2Txt.setValue(montoTxt.getDoubleValueDoNotThrow());
            haber3Txt.setValue(0.00); //diferencial cambiario

            debe1QTxt.setValue(Utileria.numberFormatEntero.format(montoTxt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow()));
            haber1QTxt.setValue(0.00);
            debe2QTxt.setValue(0.00);
            debe3QTxt.setValue(0.00);  //diferencial cambiario
            haber2QTxt.setValue(Utileria.numberFormatEntero.format(montoTxt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow()));
            haber3QTxt.setValue(0.00); //diferencial cambiario
        }
        else { // QUETZALES
            debe1Txt.setValue(Utileria.numberFormatEntero.format(montoTxt.getDoubleValueDoNotThrow() / tipoCambioTxt.getDoubleValueDoNotThrow()));
            haber1Txt.setValue(0.00);
            debe2Txt.setValue(0.00);
            haber2Txt.setValue(Utileria.numberFormatEntero.format(montoTxt.getDoubleValueDoNotThrow() / tipoCambioTxt.getDoubleValueDoNotThrow()));
            if(tipoCambioTxt.getDoubleValueDoNotThrow() > 1.0) {
                debe3Txt.setValue(Utileria.numberFormatEntero.format(montoTxt.getDoubleValueDoNotThrow() - (montoTxt.getDoubleValueDoNotThrow() / tipoCambioTxt.getDoubleValueDoNotThrow())));  //diferencial cambiario
                haber3Txt.setValue(0.00);  //diferencial cambiario
            }

            debe1QTxt.setValue(montoTxt.getDoubleValueDoNotThrow());
            haber1QTxt.setValue(0.00);
            debe2QTxt.setValue(0.00);
            debe3QTxt.setValue(0.00); //diferencial cambiario
            haber2QTxt.setValue(montoTxt.getDoubleValueDoNotThrow());
            haber3QTxt.setValue(0.00); //diferencial cambiario
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
        queryString = " SELECT * FROM proveedor ";
        queryString += " WHERE Inhabilitado = 0 ";
//2023-03-29 JA        queryString += " And IdProveedor Like '4%'";
        queryString += " AND EsCliente = 1 ";
        queryString += " ORDER BY Nombre";

        proveedorCbx.removeAllItems();

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) {  /// encontrado
                proveedorCbx.addItem(rsRecords.getString("IDProveedor"));
                proveedorCbx.setItemCaption(rsRecords.getString("IDProveedor"), "(" + rsRecords.getString("IDProveedor") + ") " + rsRecords.getString("Nombre"));
                proveedorCbx.getItem(rsRecords.getString("IDProveedor")).getItemProperty(NIT_PROPERTY).setValue(rsRecords.getString("NIT"));
                proveedorCbx.getItem(rsRecords.getString("IDProveedor")).getItemProperty(NOMBRESINCODIGO_PROPERTY).setValue(rsRecords.getString("Nombre"));
            }
//            System.out.println("Seleccionar este");
            // proveedorCbx.select("0");

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

            }

        } catch (Exception ex1) {
            System.out.println("Error al combo cuentas contables: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void generarPartidaEnganches() {

        limpiarCampos();

        monedaCbx.select("QUETZALES");
        cuentaContable1Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaLocal());
        cuentaContable2Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getAnticiposClientes());
//        cuentaContable3Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getDiferencialCambiario());

        tipoEngancheCbx.setVisible(true);
        referenciaTxt.setVisible(false);

        this.proveedorCbx.setCaption("Cliente");
        llenarComboProveedor();
    }

    public void ingresarEnganches() {

        try {
            Utileria utileria = new Utileria();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

            Date fechaInicial = dateFormat.parse(utileria.getFecha());
            Date fechaFinal = dateFormat.parse(utileria.getFechaYYYYMMDD(fechaDt.getValue()));

            int dias = (int) ((fechaInicial.getTime() - fechaFinal.getTime()) / 86400000);

  //          System.out.println("Hay " + dias + " dias de diferencia");

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

//        if (tipoEngancheCbx.getValue() == null) {
//            Notification.show("Por favor, Seleccione un tipo de enganche..", Notification.Type.ERROR_MESSAGE);
//            tipoEngancheCbx.focus();
//            return;
//        }
        /*
        
        if (referenciaTxt.getValue().isEmpty()) {
            Notification.show("Por favor, Ingrese una referencia..", Notification.Type.ERROR_MESSAGE);
            referenciaTxt.focus();
            return;
        }
        if (descripcionTxt.getValue().isEmpty()) {
            Notification.show("Por favor, Ingrese una descripción..", Notification.Type.ERROR_MESSAGE);
            descripcionTxt.focus();
            return;
        }
         */
//        if (debe1Txt.getDoubleValueDoNotThrow() != montoTxt.getDoubleValueDoNotThrow()) {
//            Notification.show("La partida esta descuadrada por favor revisarla", Notification.Type.ERROR_MESSAGE);
//            debe1Txt.focus();
//            return;
//        }
//        if (haber2Txt.getDoubleValueDoNotThrow() != montoTxt.getDoubleValueDoNotThrow()) {
//            Notification.show("La partida esta descuadrada por favor revisarla", Notification.Type.ERROR_MESSAGE);
//            haber2Txt.focus();
//            return;
//        }
        if (monedaCbx.getValue().equals("DOLARES") && tipoCambioTxt.getDoubleValueDoNotThrow() <= 1.00) {
            Notification.show("Si la transacción es en DOLARES, debe llebar tipo de cambio. Por favor, revise la moneda.", Notification.Type.ERROR_MESSAGE);
            monedaCbx.focus();
            return;
        }
        
//        if (monedaCbx.getValue().equals("QUETZALES") && tipoCambioTxt.getDoubleValueDoNotThrow() < 1.00) {
//            Notification.show("Si la transacción es en QUETZALES, debe llebar 1.00 de tipo de cambio. Por favor revise.", Notification.Type.ERROR_MESSAGE);
//            monedaCbx.focus();
//            return;
//        }

        if (monedaCbx.getValue().equals("DOLARES") && !String.valueOf(cuentaContable1Cbx.getValue()).equals(((SopdiUI) mainUI).cuentasContablesDefault.getBancosMonedaExtranjera())) {
            Notification.show("Si la transacción es en DOLARES, debe llebar cuenta contable DOLARES. Por favor evise la CUENTA CONTABLE.", Notification.Type.ERROR_MESSAGE);
            cuentaContable1Cbx.focus();
            return;
        }

        queryString = " Select * from contabilidad_partida";
        queryString += " Where NumeroDocumento = '" + numeroTxt.getValue().toUpperCase().trim() + "'";
        queryString += " And IdProveedor     =  " + String.valueOf(proveedorCbx.getValue());
        queryString += " And IdEmpresa = " + String.valueOf(empresaCbx.getValue());
        queryString += " And TipoDocumento = '" + String.valueOf(medioCbx.getValue())  + "'";
        queryString += " And MonedaDocumento = '" + monedaCbx.getValue() + "'";

//        System.out.println("\n\nQuery=" + queryString + "\n\n");

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

            if (rsRecords.next()) {   //encontrado                               

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
        queryString += " TipoDocumento, TipoEnganche, Referencia, Fecha, IdProveedor,  NombreProveedor,";
        queryString += " SerieDocumento, NumeroDocumento, IdNomenclatura, MonedaDocumento, MontoDocumento, Debe, Haber,";
        queryString += " DebeQuetzales, HaberQuetzales, TipoCambio, Saldo, Descripcion,";
        queryString += " CreadoUsuario, CreadoFechaYHora)";
        queryString += " Values ";
        queryString += "(";
        queryString += String.valueOf(empresaCbx.getValue());
        queryString += ",'INGRESADO'";
        queryString += ",'" + codigoPartida + "'";
        queryString += ",''";
        queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
        queryString += ",'" + String.valueOf(tipoEngancheCbx.getValue()) + "'";
        queryString += ",'" + referenciaTxt.getValue() + "'";
        queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
        queryString += "," + proveedorCbx.getValue();
        queryString += ",'" + proveedorCbx.getContainerProperty(proveedorCbx.getValue(), NOMBRESINCODIGO_PROPERTY).getValue() + "'";
        queryString += ",''";
        queryString += ",'" + numeroTxt.getValue() + "'";
        queryString += "," + cuentaContable1Cbx.getValue();
        queryString += ",'" + monedaCbx.getValue() + "'";
        queryString += "," + montoTxt.getDoubleValueDoNotThrow();
        queryString += "," + String.valueOf(debe1Txt.getDoubleValueDoNotThrow());
        queryString += "," + String.valueOf(haber1Txt.getDoubleValueDoNotThrow());
        queryString += "," + String.valueOf(debe1QTxt.getDoubleValueDoNotThrow());
        queryString += "," + String.valueOf(haber1QTxt.getDoubleValueDoNotThrow());
        queryString += "," + String.valueOf(tipoCambioTxt.getDoubleValueDoNotThrow());
        queryString += "," + montoTxt.getDoubleValueDoNotThrow();
        queryString += ",'Ingreso por ANTICIPO CLIENTE " + String.valueOf(tipoEngancheCbx.getValue()) + " " + descripcionTxt.getValue() + "'";
        queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
        queryString += ",current_timestamp";
        queryString += ")";

// segundo  ingreso
        queryString += ",(";
        queryString += String.valueOf(empresaCbx.getValue());
        queryString += ",'INGRESADO'";
        queryString += ",'" + codigoPartida + "'";
        queryString += ",'" + codigoPartida + "'";
        queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
        queryString += ",'" + String.valueOf(tipoEngancheCbx.getValue()) + "'";
        queryString += ",'" + referenciaTxt.getValue() + "'";
        queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
        queryString += "," + proveedorCbx.getValue();
        queryString += ",'" + proveedorCbx.getContainerProperty(proveedorCbx.getValue(), NOMBRESINCODIGO_PROPERTY).getValue() + "'";
        queryString += ",''";
        queryString += ",'" + numeroTxt.getValue() + "'";
        queryString += "," + cuentaContable2Cbx.getValue();
        queryString += ",'" + monedaCbx.getValue() + "'";
        queryString += "," + montoTxt.getDoubleValueDoNotThrow();
        queryString += "," + String.valueOf(debe2Txt.getDoubleValueDoNotThrow());
        queryString += "," + String.valueOf(haber2Txt.getDoubleValueDoNotThrow());
        queryString += "," + String.valueOf(debe2QTxt.getDoubleValueDoNotThrow());
        queryString += "," + String.valueOf(haber2QTxt.getDoubleValueDoNotThrow());
        queryString += "," + String.valueOf(tipoCambioTxt.getDoubleValueDoNotThrow());
        queryString += "," + montoTxt.getDoubleValueDoNotThrow();
        queryString += ",'Ingreso por ANTICIPO CLIENTE " + String.valueOf(tipoEngancheCbx.getValue()) + " " + descripcionTxt.getValue() + "'";
        queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
        queryString += ",current_timestamp";
        queryString += ")";

// tercer  ingreso
//        queryString += ",(";
//        queryString += String.valueOf(empresaCbx.getValue());
//        queryString += ",'INGRESADO'";
//        queryString += ",'" + codigoPartida + "'";
//        queryString += ",'" + codigoPartida + "'";
//        queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
//        queryString += ",'" + String.valueOf(tipoEngancheCbx.getValue()) + "'";
//        queryString += ",'" + referenciaTxt.getValue() + "'";
//        queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
//        queryString += "," + proveedorCbx.getValue();
//        queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
//        queryString += ",''";
//        queryString += ",'" + numeroTxt.getValue() + "'";
//        queryString += "," + cuentaContable3Cbx.getValue();
//        queryString += ",'" + monedaCbx.getValue() + "'";
//        queryString += "," + montoTxt.getDoubleValueDoNotThrow();
//        queryString += "," + String.valueOf(debe3Txt.getDoubleValueDoNotThrow());
//        queryString += "," + String.valueOf(haber3Txt.getDoubleValueDoNotThrow());
//        queryString += "," + String.valueOf(debe3QTxt.getDoubleValueDoNotThrow());
//        queryString += "," + String.valueOf(haber3QTxt.getDoubleValueDoNotThrow());
//        queryString += "," + String.valueOf(tipoCambioTxt.getDoubleValueDoNotThrow());
//        queryString += "," + montoTxt.getDoubleValueDoNotThrow();
//        queryString += ",'Ingreso por ANTICIPO CLIENTE " + String.valueOf(tipoEngancheCbx.getValue()) + " " + descripcionTxt.getValue() + "'";
//        queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
//        queryString += ",current_timestamp";
//        queryString += ")";

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "queryString Ingreso Bancos Enganche o Anticipo Cliente = " + queryString);

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            if (!variableTemp.isEmpty()) {
                cambiarEstatusToken(codigoPartida);
            }

            Notification.show("Ingreso a banco realizado con exito!", Notification.Type.HUMANIZED_MESSAGE);

            ((IngresoBancosView) (mainUI.getNavigator().getCurrentView())).llenarTablaFactura(String.valueOf(empresaCbx.getValue()));

            close();

            limpiarCampos();
        } catch (Exception ex1) {
            System.out.println("Error al insertar enganche o anticipo : " + ex1.getMessage());
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

        cuentaContable1Cbx.clear();
        haber1Txt.setValue(0.00);
        debe1Txt.setValue(0.00);

        cuentaContable2Cbx.clear();
        debe2Txt.setValue(0.00);
        haber2Txt.setValue(0.00);

        cuentaContable3Cbx.clear();
        debe3Txt.setValue(0.00);
        haber3Txt.setValue(0.00);

    }

    public void limpiarCampos() {

        proveedorCbx.clear();
        tipoEngancheCbx.clear();

        debe1Txt.setValue(0.00);
        debe2Txt.setValue(0.00);
        debe3Txt.setValue(0.00);
        debe1QTxt.setValue(0.00);
        debe2QTxt.setValue(0.00);
        debe3QTxt.setValue(0.00);

        haber1Txt.setValue(0.00);
        haber2Txt.setValue(0.00);
        haber3Txt.setValue(0.00);
        haber1QTxt.setValue(0.00);
        haber2QTxt.setValue(0.00);
        haber3QTxt.setValue(0.00);

        numeroTxt.setValue("");
        referenciaTxt.setValue("");
        descripcionTxt.setValue("");
        montoTxt.setValue(0.00);

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