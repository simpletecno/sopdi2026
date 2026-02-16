/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.tesoreria;

import com.simpletecno.sopdi.utilerias.MyEmailMessanger;
import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.Property;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.ui.NumberField;

import javax.mail.MessagingException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author user
 */
public class IngresoBancosTrasladosForm extends Window {

    static final String NIT_PROPERTY = "NIT";
    static final String NOMBRESINCODIGO_PROPERTY = "NSC";

    UI mainUI;
    Statement stQuery;
    ResultSet rsRecords;
    Statement stQuery1;
    Statement stQuery2;
    ResultSet rsRecords1;
    ResultSet rsRecords2;
    String queryString;

    ArrayList<String> codigoDepositosList;
    ArrayList<String> tipoDocaList;
    ArrayList<String> noDocaList;

    VerticalLayout mainLayout = new VerticalLayout();
    ComboBox empresaCbx;

    ComboBox medioCbx;
    ComboBox proveedorCbx;
    ComboBox monedaCbx;
    NumberField tipoCambioTxt;
    NumberField montoTxt;
    TextField referenciaTxt;
    TextField descripcionTxt;
    TextField numeroTxt;

    ComboBox cuentaContable1Cbx;
    ComboBox cuentaContable2Cbx;
    ComboBox cuentaContable3Cbx;
    ComboBox cuentaContable4Cbx;
    NumberField haber1Txt;
    NumberField haber2Txt;
    NumberField haber3Txt;
    NumberField haber4Txt;
    NumberField debe1Txt;
    NumberField debe2Txt;
    NumberField debe3Txt;
    NumberField debe4Txt;

    String empresa;
    String codigoPartida;
    DateField fechaDt;

    BigDecimal totalDebe;
    BigDecimal totalHaber;

    public IngresoBancosTrasladosForm(String empresa, String codigoPartida) {
        this.empresa = empresa;
        this.codigoPartida = codigoPartida;

        this.mainUI = UI.getCurrent();
        setResponsive(true);
        setModal(true);

        mainLayout.addStyleName("rcorners3");
        mainLayout.setSpacing(true);
        mainLayout.setWidth("98%");

        HorizontalLayout layoutTitle;
        layoutTitle = new HorizontalLayout();
        layoutTitle.setSpacing(true);
        layoutTitle.setMargin(true);
        layoutTitle.setWidth("100%");

        this.mainUI = UI.getCurrent();
        setResponsive(true);
        setWidth("95%");
        setHeight("50%");

        Label titleLbl = new Label("Ingreso por traslado empresas relacionadas");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h2_custom");

        empresaCbx = new ComboBox("EMPRESA :");
        empresaCbx.setStyleName(ValoTheme.COMBOBOX_HUGE);
        empresaCbx.setWidth("90%");
        empresaCbx.setInvalidAllowed(false);
        empresaCbx.setNewItemsAllowed(false);
        empresaCbx.setTextInputAllowed(false);
        empresaCbx.setNullSelectionAllowed(false);
        llenarComboEmpresa();
        empresaCbx.select(empresa);
        empresaCbx.setReadOnly(true);

        layoutTitle.addComponent(empresaCbx);
        layoutTitle.setComponentAlignment(empresaCbx, Alignment.MIDDLE_LEFT);

        layoutTitle.addComponent(titleLbl);
        layoutTitle.setComponentAlignment(titleLbl, Alignment.BOTTOM_RIGHT);

        mainLayout.addComponent(layoutTitle);

        setContent(mainLayout);

        crearCamposFormulario();
        crearPartida();
        crearBoton();
        llenarComboCuentaContable();

        if (!codigoPartida.trim().isEmpty()) {
//            llenarDatos();
        }

    }

    public void llenarComboEmpresa() {
        queryString = " SELECT * FROM contabilidad_empresa";
        queryString += " Where IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) { //  encontrado                
                empresaCbx.addItem(rsRecords.getString("IdEmpresa"));
                empresaCbx.setItemCaption(rsRecords.getString("IdEmpresa"), rsRecords.getString("Empresa"));
            }

            rsRecords.first();
//            empresaCbx.select(rsRecords.getString("IdEmpresa"));

        } catch (Exception ex1) {
            System.out.println("Error al listar cuentas a cargar: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void crearCamposFormulario() {
        VerticalLayout camposLayout = new VerticalLayout();
        camposLayout.addStyleName("rcorners3");
        camposLayout.setWidth("100%");
        camposLayout.setSpacing(true);
        camposLayout.setMargin(true);

        HorizontalLayout horizontalLayout1 = new HorizontalLayout();
        horizontalLayout1.setSpacing(true);

        HorizontalLayout horizontalLayout3 = new HorizontalLayout();
        horizontalLayout3.setSpacing(true);

        medioCbx = new ComboBox("Medio : ");
        medioCbx.setWidth("10em");
        medioCbx.addItem("DEPOSITO");
        medioCbx.addItem("NOTA DE CREDITO");
        medioCbx.setInvalidAllowed(false);
        medioCbx.setNewItemsAllowed(false);
        medioCbx.setNullSelectionAllowed(false);
        medioCbx.select("DEPOSITO");

        fechaDt = new DateField("Fecha : ");
        fechaDt.setDateFormat("dd/MM/yyyy");
        fechaDt.setWidth("9em");
        fechaDt.setValue(new java.util.Date());

        proveedorCbx = new ComboBox("Proveedor o Cliente");
        proveedorCbx.addContainerProperty(NIT_PROPERTY, String.class, "");
        proveedorCbx.setInvalidAllowed(false);
        proveedorCbx.setNewItemsAllowed(false);
        proveedorCbx.setNullSelectionAllowed(false);
        proveedorCbx.setWidth("24em");
        proveedorCbx.setFilteringMode(FilteringMode.CONTAINS);
        llenarComboProveedor(); // proveedores

        numeroTxt = new TextField("# Depósito/Transfer.:");
        numeroTxt.setWidth("8em");

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
        montoTxt.setWidth("7em");
        montoTxt.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(Property.ValueChangeEvent event) {
                debe1Txt.setValue(montoTxt.getDoubleValueDoNotThrow());
                haber2Txt.setValue(montoTxt.getDoubleValueDoNotThrow());
            }
        });

        monedaCbx = new ComboBox("Moneda :");
        monedaCbx.addItem("QUETZALES");
        monedaCbx.addItem("DOLARES");
        monedaCbx.select("QUETZALES");
        monedaCbx.setInvalidAllowed(false);
        monedaCbx.setNewItemsAllowed(false);
        monedaCbx.setNullSelectionAllowed(false);
        monedaCbx.setWidth("9em");
        monedaCbx.addValueChangeListener((event) -> {
            if (cuentaContable1Cbx.getValue() != null) {
                if (monedaCbx.getValue().equals("DOLARES")) {
                    tipoCambioTxt.setReadOnly(false);
                    tipoCambioTxt.setValue(Float.toString(((SopdiUI)UI.getCurrent()).sessionInformation.getFltExchangeRate()));
                    cuentaContable1Cbx.select(((SopdiUI) mainUI).cuentasContablesDefault.getBancosMonedaExtranjera());
                } else {
                    cuentaContable1Cbx.select(((SopdiUI) mainUI).cuentasContablesDefault.getBancosMonedaLocal());
                    tipoCambioTxt.setReadOnly(false);
                    tipoCambioTxt.setValue(1.00);
                    tipoCambioTxt.setReadOnly(true);
                }
            }
        });

        tipoCambioTxt = new NumberField("T.Cambio :");
        tipoCambioTxt.setDecimalAllowed(true);
        tipoCambioTxt.setDecimalPrecision(5);
        tipoCambioTxt.setMinimumFractionDigits(5);
        tipoCambioTxt.setDecimalSeparator('.');
        tipoCambioTxt.setDecimalSeparatorAlwaysShown(true);
        tipoCambioTxt.setImmediate(true);
        tipoCambioTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        tipoCambioTxt.setWidth("6em");

        referenciaTxt = new TextField("Referencia");
        referenciaTxt.setWidth("32em");
        referenciaTxt.setVisible(false);

        descripcionTxt = new TextField("Descripción");
        descripcionTxt.setWidth("28em");
        descripcionTxt.setVisible(true);

        horizontalLayout1.addComponent(medioCbx);
        horizontalLayout1.setComponentAlignment(medioCbx, Alignment.MIDDLE_CENTER);
        horizontalLayout1.addComponent(fechaDt);
        horizontalLayout1.setComponentAlignment(fechaDt, Alignment.MIDDLE_CENTER);
        horizontalLayout1.addComponent(numeroTxt);
        horizontalLayout1.setComponentAlignment(numeroTxt, Alignment.MIDDLE_CENTER);
        horizontalLayout1.addComponent(proveedorCbx);
        horizontalLayout1.setComponentAlignment(proveedorCbx, Alignment.MIDDLE_CENTER);
        horizontalLayout1.addComponent(montoTxt);
        horizontalLayout1.setComponentAlignment(montoTxt, Alignment.MIDDLE_CENTER);
        horizontalLayout1.addComponent(monedaCbx);
        horizontalLayout1.setComponentAlignment(monedaCbx, Alignment.MIDDLE_CENTER);
        horizontalLayout1.addComponent(tipoCambioTxt);
        horizontalLayout1.setComponentAlignment(tipoCambioTxt, Alignment.MIDDLE_CENTER);

        horizontalLayout3.addComponent(referenciaTxt);
        horizontalLayout3.setComponentAlignment(referenciaTxt, Alignment.MIDDLE_CENTER);
        horizontalLayout3.addComponent(descripcionTxt);
        horizontalLayout3.setComponentAlignment(descripcionTxt, Alignment.MIDDLE_CENTER);

        camposLayout.addComponent(horizontalLayout1);
        camposLayout.setComponentAlignment(horizontalLayout1, Alignment.MIDDLE_CENTER);

        camposLayout.addComponent(horizontalLayout3);
        camposLayout.setComponentAlignment(horizontalLayout3, Alignment.MIDDLE_CENTER);

        mainLayout.addComponent(camposLayout);
        mainLayout.setComponentAlignment(camposLayout, Alignment.MIDDLE_CENTER);

    }

    public void crearPartida() {

        VerticalLayout contenedorLayout = new VerticalLayout();
        contenedorLayout.setWidth("100%");
        contenedorLayout.setResponsive(true);
        contenedorLayout.addStyleName("rcorners3");
        contenedorLayout.setSpacing(true);
        contenedorLayout.setMargin(true);

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

        cuentaContable1Cbx = new ComboBox("Cuentas contables :");
        cuentaContable1Cbx.setWidth("30em");
        cuentaContable1Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable1Cbx.setInvalidAllowed(false);
        cuentaContable1Cbx.setNewItemsAllowed(false);

        cuentaContable2Cbx = new ComboBox();
        cuentaContable2Cbx.setWidth("30em");
        cuentaContable2Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable2Cbx.setInvalidAllowed(false);
        cuentaContable2Cbx.setNewItemsAllowed(false);

        cuentaContable3Cbx = new ComboBox();
        cuentaContable3Cbx.setWidth("30em");
        cuentaContable3Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable3Cbx.setInvalidAllowed(false);
        cuentaContable3Cbx.setNewItemsAllowed(false);

        cuentaContable4Cbx = new ComboBox();
        cuentaContable4Cbx.setWidth("30em");
        cuentaContable4Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable4Cbx.setInvalidAllowed(false);
        cuentaContable4Cbx.setNewItemsAllowed(false);
        cuentaContable4Cbx.setVisible(false);

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
        haber1Txt.setWidth("8em");
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
        haber2Txt.setWidth("8em");
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
        haber3Txt.setWidth("8em");
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
        haber4Txt.setWidth("8em");
        haber4Txt.setValue(0.00);
        haber4Txt.setVisible(false);

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
        debe1Txt.setWidth("8em");
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
        debe2Txt.setWidth("8em");
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
        debe3Txt.setWidth("8em");
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
        debe4Txt.setWidth("8em");
        debe4Txt.setValue(0.00);
        debe4Txt.setVisible(false);

        // tipoIngresoCbx.select("PRESTAMOS");
        layoutHorizontal1.addComponents(cuentaContable1Cbx, debe1Txt, haber1Txt);
        layoutHorizontal1.setComponentAlignment(cuentaContable1Cbx, Alignment.BOTTOM_CENTER);
        layoutHorizontal1.setComponentAlignment(debe1Txt, Alignment.BOTTOM_CENTER);
        layoutHorizontal1.setComponentAlignment(haber1Txt, Alignment.BOTTOM_CENTER);

        layoutHorizontal2.addComponents(cuentaContable2Cbx, debe2Txt, haber2Txt);
        layoutHorizontal2.setComponentAlignment(cuentaContable2Cbx, Alignment.BOTTOM_CENTER);
        layoutHorizontal2.setComponentAlignment(debe2Txt, Alignment.BOTTOM_CENTER);
        layoutHorizontal2.setComponentAlignment(haber2Txt, Alignment.BOTTOM_CENTER);

        layoutHorizontal3.addComponents(cuentaContable3Cbx, debe3Txt, haber3Txt);
        layoutHorizontal3.setComponentAlignment(cuentaContable3Cbx, Alignment.BOTTOM_CENTER);
        layoutHorizontal3.setComponentAlignment(debe3Txt, Alignment.BOTTOM_CENTER);
        layoutHorizontal3.setComponentAlignment(haber3Txt, Alignment.BOTTOM_CENTER);

        layoutHorizontal4.addComponents(cuentaContable4Cbx, debe4Txt, haber4Txt);
        layoutHorizontal4.setComponentAlignment(cuentaContable4Cbx, Alignment.BOTTOM_CENTER);
        layoutHorizontal4.setComponentAlignment(debe4Txt, Alignment.BOTTOM_CENTER);
        layoutHorizontal4.setComponentAlignment(haber4Txt, Alignment.BOTTOM_CENTER);

        contenedorLayout.addComponent(layoutHorizontal1);
        contenedorLayout.setComponentAlignment(layoutHorizontal1, Alignment.BOTTOM_CENTER);
        contenedorLayout.addComponent(layoutHorizontal2);
        contenedorLayout.setComponentAlignment(layoutHorizontal2, Alignment.BOTTOM_CENTER);
        contenedorLayout.addComponent(layoutHorizontal3);
        contenedorLayout.setComponentAlignment(layoutHorizontal3, Alignment.BOTTOM_CENTER);
        contenedorLayout.addComponent(layoutHorizontal4);
        contenedorLayout.setComponentAlignment(layoutHorizontal4, Alignment.BOTTOM_CENTER);

        mainLayout.addComponent(contenedorLayout);
        mainLayout.setComponentAlignment(contenedorLayout, Alignment.MIDDLE_CENTER);

    }

    public void llenarComboProveedor() {
        queryString = " SELECT * from proveedor ";
        queryString += " WHERE Inhabilitado = 0 ";
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

            }

        } catch (Exception ex1) {
            System.out.println("Error al combo cuentas contables: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void ingresarDepositos() {

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
        double totalDebe = debe1Txt.getDoubleValueDoNotThrow() + debe3Txt.getDoubleValueDoNotThrow();
        double totalhaber = haber2Txt.getDoubleValueDoNotThrow() + haber3Txt.getDoubleValueDoNotThrow();

        if (totalDebe != totalhaber) {
            Notification.show("La partida esta descuadrada por favor revise las cantidades..", Notification.Type.WARNING_MESSAGE);
            haber3Txt.focus();
            return;
        }

        if (monedaCbx.getValue().equals("DOLARES") && tipoCambioTxt.getDoubleValueDoNotThrow() <= 1.00) {
            Notification.show("Si la transacción es en DOLARES, debe llebar tipo de cambio. Por favor, revise la moneda.", Notification.Type.ERROR_MESSAGE);
            monedaCbx.focus();
            return;
        }
        if (monedaCbx.getValue().equals("DOLARES") && !String.valueOf(cuentaContable1Cbx.getValue()).equals(((SopdiUI) mainUI).cuentasContablesDefault.getBancosMonedaExtranjera())) {
            Notification.show("Si la transacción es en DOLARES, debe llebar cuenta contable DOLARES. Por favor evise la CUENTA CONTABLE.", Notification.Type.ERROR_MESSAGE);
            cuentaContable1Cbx.focus();
            return;
        }

        if (descripcionTxt.getValue().isEmpty()) {
            Notification.show("Por favor, Ingrese una descripción..", Notification.Type.ERROR_MESSAGE);
            descripcionTxt.focus();
            return;
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

            if (rsRecords.next()) {  // encontrado                               

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

        queryString = " Insert Into contabilidad_partida (IdEmpresa, Estatus, CodigoPartida, ";
        queryString += " TipoDocumento, Fecha, IdProveedor, NombreProveedor, SerieDocumento,";
        queryString += " NumeroDocumento, IdNomenclatura, MonedaDocumento, MontoDocumento, Debe, Haber,";
        queryString += " DebeQuetzales, HaberQuetzales, TipoCambio, Saldo, Descripcion,";
        queryString += " CreadoUsuario, CreadoFechaYHora)";
        queryString += " Values ";
        queryString += "(";
        queryString += String.valueOf(empresaCbx.getValue());
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
        queryString += "," + String.valueOf(debe1Txt.getDoubleValueDoNotThrow()); // HABER
        queryString += ",0.00"; //haber                
        queryString += "," + String.valueOf(debe1Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());
        queryString += ",0.00"; //haber Q.
        queryString += "," + String.valueOf(tipoCambioTxt.getDoubleValueDoNotThrow());
        queryString += ",0.00";
        queryString += ",'" + descripcionTxt.getValue() + "'";
        queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
        queryString += ",current_timestamp";
        queryString += ")";

        //segundo  ingreso
        queryString += ",(";
        queryString += String.valueOf(empresaCbx.getValue());
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
        queryString += ",0.00"; /// DEBE
        queryString += "," + String.valueOf(haber2Txt.getDoubleValueDoNotThrow());  //HABER
        queryString += ",0.00"; //DEBE Q.
        queryString += "," + String.valueOf(haber2Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());
        queryString += "," + String.valueOf(tipoCambioTxt.getDoubleValueDoNotThrow());
        queryString += ",0.00";
        queryString += ",'" + descripcionTxt.getValue() + "'";
        queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
        queryString += ",current_timestamp";
        queryString += ")";

        if (cuentaContable3Cbx.getValue() != null && debe3Txt.getDoubleValueDoNotThrow() != 0.00) {
            queryString += ",(";
            queryString += String.valueOf(empresaCbx.getValue());
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
            queryString += "," + String.valueOf(debe3Txt.getDoubleValueDoNotThrow()); // debe
            queryString += ",0.00"; /// HABER
            queryString += "," + String.valueOf(debe3Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00"; //DEBE Q.            
            queryString += "," + String.valueOf(tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00";
            queryString += ",'" + descripcionTxt.getValue() + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";
        }
        if (cuentaContable3Cbx.getValue() != null && haber3Txt.getDoubleValueDoNotThrow() != 0.00) {
            queryString += ",(";
            queryString += String.valueOf(empresaCbx.getValue());
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
            queryString += ",0.00"; /// DEBE
            queryString += "," + String.valueOf(haber3Txt.getDoubleValueDoNotThrow()); //  haber            
            queryString += ",0.00"; //DEBE Q.            
            queryString += "," + String.valueOf(haber3Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00";
            queryString += ",'" + descripcionTxt.getValue() + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";
        }

//System.out.println("queryString Ingreso Bancos DEPOSITOS por traslado de fondos entre empresas= " + queryString);
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "queryString Ingreso Bancos TRASLADO EMPRESA RELACIONADA = " + queryString);

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            Notification.show("Ingreso realizado con exito!", Notification.Type.HUMANIZED_MESSAGE);

            ((IngresoBancosView) (mainUI.getNavigator().getCurrentView())).llenarTablaFactura(empresa);

            close();

        } catch (Exception ex1) {
            System.out.println("Error al insertar un deposito : " + ex1.getMessage());
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
                Logger.getLogger(IngresoBancosTrasladosForm.class.getName()).log(Level.SEVERE, null, ex2);
            }
        }

        ///// Diferencial cambiario
    }

    public void limpiarCampos() {
        referenciaTxt.setVisible(true);

        proveedorCbx.clear();
        haber2Txt.setReadOnly(false);
        debe1Txt.setReadOnly(false);
        haber1Txt.setValue(0.00);
        haber2Txt.setValue(0.00);
        haber3Txt.setValue(0.00);
        debe1Txt.setValue(0.00);
        debe2Txt.setValue(0.00);
        debe3Txt.setValue(0.00);
        numeroTxt.setValue("");
        referenciaTxt.setValue("");
        descripcionTxt.setValue("");
        montoTxt.setValue(0.00);

    }

    public void crearBoton() {

        Button guardarBtn = new Button("Grabar");
        guardarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        guardarBtn.setIcon(FontAwesome.SAVE);
        guardarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (!codigoPartida.trim().isEmpty()) {
                    queryString = " DELETE from contabilidad_partida ";
                    queryString += " where CodigoPartida  = '" + codigoPartida + "'";

                    try {
                        stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                        stQuery.executeUpdate(queryString);

                    } catch (SQLException ex) {
                        System.out.println("Error al intentar eliminar registros " + ex);
                        ex.printStackTrace();

                        Notification notif = new Notification("HA OCURRIDO UN ERROR DE BASE DE DATOS : " + ex.getMessage(),
                                Notification.Type.ERROR_MESSAGE);
                        notif.setDelayMsec(1500);
                        notif.setPosition(Position.MIDDLE_CENTER);
                        notif.setIcon(FontAwesome.WARNING);
                        notif.show(Page.getCurrent());

                        try {
                            String emailsTo[] = {"alerta@simpletecno.com"};
                            MyEmailMessanger eMail = new MyEmailMessanger();

                            eMail.postMail(emailsTo, "Error en SOPDI", "Error en base de datos :  " + this.getClass().getName() + " -->" + ex.getMessage());
                        } catch (MessagingException ex2) {
                            Logger.getLogger(IngresoBancosTrasladosForm.class.getName()).log(Level.SEVERE, null, ex2);
                        }
                        return;
                    }
                }
                ingresarDepositos();
            }
        });
        mainLayout.addComponent(guardarBtn);
        mainLayout.setComponentAlignment(guardarBtn, Alignment.BOTTOM_CENTER);
    }
}
