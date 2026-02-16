/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.tesoreria;

import com.simpletecno.sopdi.utilerias.MyEmailMessanger;
import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vaadin.ui.NumberField;
import javax.mail.MessagingException;

/**
 *
 * @author user
 */
public class PagoFacturaMantenimientoForm extends Window {

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
    ComboBox tipoEngancheCbx;
    ComboBox monedaCbx;
    ComboBox facturasPorPagarCbx;
    NumberField tipoCambioTxt;
    NumberField montoTxt;
    TextField referenciaTxt;
    TextField descripcionTxt;
    TextField numeroTxt;

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
    ComboBox cuentaContable11Cbx;
    ComboBox cuentaContable12Cbx;
    ComboBox cuentaContable13Cbx;
    ComboBox cuentaContable14Cbx;
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
    NumberField haber11Txt;
    NumberField haber12Txt;
    NumberField haber13Txt;
    NumberField haber14Txt;
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
    NumberField debe11Txt;
    NumberField debe12Txt;
    NumberField debe13Txt;
    NumberField debe14Txt;

    String codigoPartida;
    DateField fechaDt;

    BigDecimal totalDebe;
    BigDecimal totalHaber;
    double valorFacturaPorPagar;

    public PagoFacturaMantenimientoForm() {

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
        setHeight("85%");

        Label titleLbl = new Label("PAGO DE FACTURA DE MANTENIMIENTO");
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

    }

    public void llenarComboEmpresa() {
        queryString = " SELECT * FROM contabilidad_empresa";
        queryString += " Where IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();

        try {
            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords1 = stQuery1.executeQuery(queryString);

            while (rsRecords1.next()) { //  encontrado                
                empresaCbx.addItem(rsRecords1.getString("IdEmpresa"));
                empresaCbx.setItemCaption(rsRecords1.getString("IdEmpresa"), rsRecords1.getString("Empresa"));
            }

            empresaCbx.select(empresaCbx.getItemIds().iterator().next());

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

        proveedorCbx = new ComboBox("Cliente");
        proveedorCbx.addContainerProperty(NIT_PROPERTY, String.class, "");
        proveedorCbx.setInvalidAllowed(false);
        proveedorCbx.setNewItemsAllowed(false);
        proveedorCbx.setNullSelectionAllowed(false);
        proveedorCbx.setWidth("24em");
        proveedorCbx.setFilteringMode(FilteringMode.CONTAINS);
        proveedorCbx.addValueChangeListener((event) -> {
            llenarComboFacturasPagar();

        });

        llenarComboProveedor();

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
                    cuentaContable1Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaExtranjera());
                } else {
                    cuentaContable1Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaLocal());
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

        facturasPorPagarCbx = new ComboBox("Facturas por pagar");
        facturasPorPagarCbx.setWidth("16em");
        facturasPorPagarCbx.setFilteringMode(FilteringMode.CONTAINS);
        facturasPorPagarCbx.setInvalidAllowed(false);
        facturasPorPagarCbx.setNewItemsAllowed(false);
        facturasPorPagarCbx.addValueChangeListener((event) -> {
            generarPartidaDocumentosVenta();

        });

        tipoEngancheCbx = new ComboBox("Tipo de Enganche");
        tipoEngancheCbx.addItem("ORDINARIO");
        tipoEngancheCbx.addItem("ORDEN DE CAMBIO");
        tipoEngancheCbx.setVisible(false);
        tipoEngancheCbx.setWidth("16em");
        tipoEngancheCbx.setInvalidAllowed(false);
        tipoEngancheCbx.setNewItemsAllowed(false);
        tipoEngancheCbx.setNullSelectionAllowed(false);
        tipoEngancheCbx.select("ORDEN DE CAMBIO");

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

        horizontalLayout3.addComponent(facturasPorPagarCbx);
        horizontalLayout3.setComponentAlignment(facturasPorPagarCbx, Alignment.MIDDLE_CENTER);
        horizontalLayout3.addComponent(tipoEngancheCbx);
        horizontalLayout3.setComponentAlignment(tipoEngancheCbx, Alignment.MIDDLE_CENTER);
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

        HorizontalLayout layoutHorizontal12 = new HorizontalLayout();
        layoutHorizontal12.setResponsive(true);
        layoutHorizontal12.setSpacing(true);

        HorizontalLayout layoutHorizontal13 = new HorizontalLayout();
        layoutHorizontal13.setResponsive(true);
        layoutHorizontal13.setSpacing(true);

        HorizontalLayout layoutHorizontal14 = new HorizontalLayout();
        layoutHorizontal14.setResponsive(true);
        layoutHorizontal14.setSpacing(true);

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

        cuentaContable5Cbx = new ComboBox();
        cuentaContable5Cbx.setWidth("30em");
        cuentaContable5Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable5Cbx.setInvalidAllowed(false);
        cuentaContable5Cbx.setNewItemsAllowed(false);

        cuentaContable6Cbx = new ComboBox();
        cuentaContable6Cbx.setWidth("30em");
        cuentaContable6Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable6Cbx.setInvalidAllowed(false);
        cuentaContable6Cbx.setNewItemsAllowed(false);

        cuentaContable7Cbx = new ComboBox();
        cuentaContable7Cbx.setWidth("30em");
        cuentaContable7Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable7Cbx.setInvalidAllowed(false);
        cuentaContable7Cbx.setNewItemsAllowed(false);

        cuentaContable8Cbx = new ComboBox();
        cuentaContable8Cbx.setWidth("30em");
        cuentaContable8Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable8Cbx.setInvalidAllowed(false);
        cuentaContable8Cbx.setNewItemsAllowed(false);

        cuentaContable9Cbx = new ComboBox();
        cuentaContable9Cbx.setWidth("30em");
        cuentaContable9Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable9Cbx.setInvalidAllowed(false);
        cuentaContable9Cbx.setNewItemsAllowed(false);

        cuentaContable10Cbx = new ComboBox();
        cuentaContable10Cbx.setWidth("30em");
        cuentaContable10Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable10Cbx.setInvalidAllowed(false);
        cuentaContable10Cbx.setNewItemsAllowed(false);

        cuentaContable11Cbx = new ComboBox();
        cuentaContable11Cbx.setWidth("30em");
        cuentaContable11Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable11Cbx.setInvalidAllowed(false);
        cuentaContable11Cbx.setNewItemsAllowed(false);

        cuentaContable12Cbx = new ComboBox();
        cuentaContable12Cbx.setWidth("30em");
        cuentaContable12Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable12Cbx.setInvalidAllowed(false);
        cuentaContable12Cbx.setNewItemsAllowed(false);

        cuentaContable13Cbx = new ComboBox();
        cuentaContable13Cbx.setWidth("30em");
        cuentaContable13Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable13Cbx.setInvalidAllowed(false);
        cuentaContable13Cbx.setNewItemsAllowed(false);

        cuentaContable14Cbx = new ComboBox();
        cuentaContable14Cbx.setWidth("30em");
        cuentaContable14Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable14Cbx.setInvalidAllowed(false);
        cuentaContable14Cbx.setNewItemsAllowed(false);

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
        haber5Txt.setWidth("8em");
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
        haber6Txt.setWidth("8em");
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
        haber7Txt.setWidth("8em");
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
        haber8Txt.setWidth("8em");
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
        haber9Txt.setWidth("8em");
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
        haber10Txt.setWidth("8em");
        haber10Txt.setValue(0.00);

        haber11Txt = new NumberField();
        haber11Txt.setDecimalAllowed(true);
        haber11Txt.setDecimalPrecision(2);
        haber11Txt.setMinimumFractionDigits(2);
        haber11Txt.setDecimalSeparator('.');
        haber11Txt.setDecimalSeparatorAlwaysShown(true);
        haber11Txt.setValue(0d);
        haber11Txt.setGroupingUsed(true);
        haber11Txt.setGroupingSeparator(',');
        haber11Txt.setGroupingSize(3);
        haber11Txt.setImmediate(true);
        haber11Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        haber11Txt.setWidth("8em");
        haber11Txt.setValue(0.00);

        haber12Txt = new NumberField();
        haber12Txt.setDecimalAllowed(true);
        haber12Txt.setDecimalPrecision(2);
        haber12Txt.setMinimumFractionDigits(2);
        haber12Txt.setDecimalSeparator('.');
        haber12Txt.setDecimalSeparatorAlwaysShown(true);
        haber12Txt.setValue(0d);
        haber12Txt.setGroupingUsed(true);
        haber12Txt.setGroupingSeparator(',');
        haber12Txt.setGroupingSize(3);
        haber12Txt.setImmediate(true);
        haber12Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        haber12Txt.setWidth("8em");
        haber12Txt.setValue(0.00);

        haber13Txt = new NumberField();
        haber13Txt.setDecimalAllowed(true);
        haber13Txt.setDecimalPrecision(2);
        haber13Txt.setMinimumFractionDigits(2);
        haber13Txt.setDecimalSeparator('.');
        haber13Txt.setDecimalSeparatorAlwaysShown(true);
        haber13Txt.setValue(0d);
        haber13Txt.setGroupingUsed(true);
        haber13Txt.setGroupingSeparator(',');
        haber13Txt.setGroupingSize(3);
        haber13Txt.setImmediate(true);
        haber13Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        haber13Txt.setWidth("8em");
        haber13Txt.setValue(0.00);

        haber14Txt = new NumberField();
        haber14Txt.setDecimalAllowed(true);
        haber14Txt.setDecimalPrecision(2);
        haber14Txt.setMinimumFractionDigits(2);
        haber14Txt.setDecimalSeparator('.');
        haber14Txt.setDecimalSeparatorAlwaysShown(true);
        haber14Txt.setValue(0d);
        haber14Txt.setGroupingUsed(true);
        haber14Txt.setGroupingSeparator(',');
        haber14Txt.setGroupingSize(3);
        haber14Txt.setImmediate(true);
        haber14Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        haber14Txt.setWidth("8em");
        haber14Txt.setValue(0.00);

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
        debe5Txt.setWidth("8em");
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
        debe6Txt.setWidth("8em");
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
        debe7Txt.setWidth("8em");
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
        debe8Txt.setWidth("8em");
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
        debe9Txt.setWidth("8em");
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
        debe10Txt.setWidth("8em");
        debe10Txt.setValue(0.00);

        debe11Txt = new NumberField();
        debe11Txt.setDecimalAllowed(true);
        debe11Txt.setDecimalPrecision(2);
        debe11Txt.setMinimumFractionDigits(2);
        debe11Txt.setDecimalSeparator('.');
        debe11Txt.setDecimalSeparatorAlwaysShown(true);
        debe11Txt.setValue(0d);
        debe11Txt.setGroupingUsed(true);
        debe11Txt.setGroupingSeparator(',');
        debe11Txt.setGroupingSize(3);
        debe11Txt.setImmediate(true);
        debe11Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        debe11Txt.setWidth("8em");
        debe11Txt.setValue(0.00);

        debe12Txt = new NumberField();
        debe12Txt.setDecimalAllowed(true);
        debe12Txt.setDecimalPrecision(2);
        debe12Txt.setMinimumFractionDigits(2);
        debe12Txt.setDecimalSeparator('.');
        debe12Txt.setDecimalSeparatorAlwaysShown(true);
        debe12Txt.setValue(0d);
        debe12Txt.setGroupingUsed(true);
        debe12Txt.setGroupingSeparator(',');
        debe12Txt.setGroupingSize(3);
        debe12Txt.setImmediate(true);
        debe12Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        debe12Txt.setWidth("8em");
        debe12Txt.setValue(0.00);

        debe13Txt = new NumberField();
        debe13Txt.setDecimalAllowed(true);
        debe13Txt.setDecimalPrecision(2);
        debe13Txt.setMinimumFractionDigits(2);
        debe13Txt.setDecimalSeparator('.');
        debe13Txt.setDecimalSeparatorAlwaysShown(true);
        debe13Txt.setValue(0d);
        debe13Txt.setGroupingUsed(true);
        debe13Txt.setGroupingSeparator(',');
        debe13Txt.setGroupingSize(3);
        debe13Txt.setImmediate(true);
        debe13Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        debe13Txt.setWidth("8em");
        debe13Txt.setValue(0.00);

        debe14Txt = new NumberField();
        debe14Txt.setDecimalAllowed(true);
        debe14Txt.setDecimalPrecision(2);
        debe14Txt.setMinimumFractionDigits(2);
        debe14Txt.setDecimalSeparator('.');
        debe14Txt.setDecimalSeparatorAlwaysShown(true);
        debe14Txt.setValue(0d);
        debe14Txt.setGroupingUsed(true);
        debe14Txt.setGroupingSeparator(',');
        debe14Txt.setGroupingSize(3);
        debe14Txt.setImmediate(true);
        debe14Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        debe14Txt.setWidth("8em");
        debe14Txt.setValue(0.00);

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

        layoutHorizontal5.addComponents(cuentaContable5Cbx, debe5Txt, haber5Txt);
        layoutHorizontal5.setComponentAlignment(cuentaContable5Cbx, Alignment.BOTTOM_CENTER);
        layoutHorizontal5.setComponentAlignment(debe5Txt, Alignment.BOTTOM_CENTER);
        layoutHorizontal5.setComponentAlignment(haber5Txt, Alignment.BOTTOM_CENTER);

        layoutHorizontal6.addComponents(cuentaContable6Cbx, debe6Txt, haber6Txt);
        layoutHorizontal6.setComponentAlignment(cuentaContable6Cbx, Alignment.BOTTOM_CENTER);
        layoutHorizontal6.setComponentAlignment(debe6Txt, Alignment.BOTTOM_CENTER);
        layoutHorizontal6.setComponentAlignment(haber6Txt, Alignment.BOTTOM_CENTER);

        layoutHorizontal7.addComponents(cuentaContable7Cbx, debe7Txt, haber7Txt);
        layoutHorizontal7.setComponentAlignment(cuentaContable7Cbx, Alignment.BOTTOM_CENTER);
        layoutHorizontal7.setComponentAlignment(debe7Txt, Alignment.BOTTOM_CENTER);
        layoutHorizontal7.setComponentAlignment(haber7Txt, Alignment.BOTTOM_CENTER);

        layoutHorizontal8.addComponents(cuentaContable8Cbx, debe8Txt, haber8Txt);
        layoutHorizontal8.setComponentAlignment(cuentaContable8Cbx, Alignment.BOTTOM_CENTER);
        layoutHorizontal8.setComponentAlignment(debe8Txt, Alignment.BOTTOM_CENTER);
        layoutHorizontal8.setComponentAlignment(haber8Txt, Alignment.BOTTOM_CENTER);

        layoutHorizontal9.addComponents(cuentaContable9Cbx, debe9Txt, haber9Txt);
        layoutHorizontal9.setComponentAlignment(cuentaContable9Cbx, Alignment.BOTTOM_CENTER);
        layoutHorizontal9.setComponentAlignment(debe9Txt, Alignment.BOTTOM_CENTER);
        layoutHorizontal9.setComponentAlignment(haber9Txt, Alignment.BOTTOM_CENTER);

        layoutHorizontal10.addComponents(cuentaContable10Cbx, debe10Txt, haber10Txt);
        layoutHorizontal10.setComponentAlignment(cuentaContable10Cbx, Alignment.BOTTOM_CENTER);
        layoutHorizontal10.setComponentAlignment(debe10Txt, Alignment.BOTTOM_CENTER);
        layoutHorizontal10.setComponentAlignment(haber10Txt, Alignment.BOTTOM_CENTER);

        layoutHorizontal11.addComponents(cuentaContable11Cbx, debe11Txt, haber11Txt);
        layoutHorizontal11.setComponentAlignment(cuentaContable11Cbx, Alignment.BOTTOM_CENTER);
        layoutHorizontal11.setComponentAlignment(debe11Txt, Alignment.BOTTOM_CENTER);
        layoutHorizontal11.setComponentAlignment(haber11Txt, Alignment.BOTTOM_CENTER);

        layoutHorizontal12.addComponents(cuentaContable12Cbx, debe12Txt, haber12Txt);
        layoutHorizontal12.setComponentAlignment(cuentaContable12Cbx, Alignment.BOTTOM_CENTER);
        layoutHorizontal12.setComponentAlignment(debe12Txt, Alignment.BOTTOM_CENTER);
        layoutHorizontal12.setComponentAlignment(haber12Txt, Alignment.BOTTOM_CENTER);

        layoutHorizontal13.addComponents(cuentaContable13Cbx, debe13Txt, haber13Txt);
        layoutHorizontal13.setComponentAlignment(cuentaContable13Cbx, Alignment.BOTTOM_CENTER);
        layoutHorizontal13.setComponentAlignment(debe13Txt, Alignment.BOTTOM_CENTER);
        layoutHorizontal13.setComponentAlignment(haber13Txt, Alignment.BOTTOM_CENTER);

        layoutHorizontal14.addComponents(cuentaContable14Cbx, debe14Txt, haber14Txt);
        layoutHorizontal14.setComponentAlignment(cuentaContable14Cbx, Alignment.BOTTOM_CENTER);
        layoutHorizontal14.setComponentAlignment(debe14Txt, Alignment.BOTTOM_CENTER);
        layoutHorizontal14.setComponentAlignment(haber14Txt, Alignment.BOTTOM_CENTER);

        contenedorLayout.addComponent(layoutHorizontal1);
        contenedorLayout.setComponentAlignment(layoutHorizontal1, Alignment.BOTTOM_CENTER);
        contenedorLayout.addComponent(layoutHorizontal2);
        contenedorLayout.setComponentAlignment(layoutHorizontal2, Alignment.BOTTOM_CENTER);
        contenedorLayout.addComponent(layoutHorizontal3);
        contenedorLayout.setComponentAlignment(layoutHorizontal3, Alignment.BOTTOM_CENTER);
        contenedorLayout.addComponent(layoutHorizontal4);
        contenedorLayout.setComponentAlignment(layoutHorizontal4, Alignment.BOTTOM_CENTER);
        contenedorLayout.addComponent(layoutHorizontal5);
        contenedorLayout.setComponentAlignment(layoutHorizontal5, Alignment.BOTTOM_CENTER);
        contenedorLayout.addComponent(layoutHorizontal6);
        contenedorLayout.setComponentAlignment(layoutHorizontal6, Alignment.BOTTOM_CENTER);
        contenedorLayout.addComponent(layoutHorizontal7);
        contenedorLayout.setComponentAlignment(layoutHorizontal7, Alignment.BOTTOM_CENTER);
        contenedorLayout.addComponent(layoutHorizontal8);
        contenedorLayout.setComponentAlignment(layoutHorizontal8, Alignment.BOTTOM_CENTER);
        contenedorLayout.addComponent(layoutHorizontal9);
        contenedorLayout.setComponentAlignment(layoutHorizontal9, Alignment.BOTTOM_CENTER);
        contenedorLayout.addComponent(layoutHorizontal10);
        contenedorLayout.setComponentAlignment(layoutHorizontal10, Alignment.BOTTOM_CENTER);
        contenedorLayout.addComponent(layoutHorizontal11);
        contenedorLayout.setComponentAlignment(layoutHorizontal11, Alignment.BOTTOM_CENTER);
        contenedorLayout.addComponent(layoutHorizontal12);
        contenedorLayout.setComponentAlignment(layoutHorizontal12, Alignment.BOTTOM_CENTER);
        contenedorLayout.addComponent(layoutHorizontal13);
        contenedorLayout.setComponentAlignment(layoutHorizontal13, Alignment.BOTTOM_CENTER);
        contenedorLayout.addComponent(layoutHorizontal14);
        contenedorLayout.setComponentAlignment(layoutHorizontal14, Alignment.BOTTOM_CENTER);

        mainLayout.addComponent(contenedorLayout);
        mainLayout.setComponentAlignment(contenedorLayout, Alignment.MIDDLE_CENTER);

    }

    public void llenarComboFacturasPagar() {
        queryString = " SELECT * from contabilidad_Partida ";
        queryString += " WHERE IdNomenclatura = " + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getClientes();
        queryString += " And TipoDocumento In ('FACTURA VENTA', 'RECIBO CONTABLE') ";
        queryString += " And IdProveedor = " + proveedorCbx.getValue();

        facturasPorPagarCbx.removeAllItems();

        System.out.println("QUERY BUSCAR FACTURAS VENTA DE UN PROVEEDOR (CIENTE) : " + queryString);

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                do {

                    // se busca el saldo real...
                    queryString = " SELECT ";
                    queryString += " SUM(DEBE - HABER) as TOTALSALDO, SUM(DebeQuetzales - HaberQuetzales) as TOTALSALDOQ ";
                    queryString += " FROM contabilidad_partida";
                    queryString += " WHERE contabilidad_partida.CodigoCC = '" + rsRecords.getString("CodigoCC") + "'";
                    queryString += " GROUP BY contabilidad_partida.CodigoCC, contabilidad_partida.IdNomenclatura";
                    queryString += " HAVING TOTALSALDO > 0";

                    stQuery2 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                    rsRecords2 = stQuery2.executeQuery(queryString);

                    if (rsRecords2.next()) {
                        facturasPorPagarCbx.addItem(rsRecords2.getString("CodigoPartida"));
                        facturasPorPagarCbx.setItemCaption(rsRecords2.getString("CodigoPartida"), rsRecords2.getString("SerieDocumento") + "" + rsRecords2.getString("NumeroDocumento") + " " + rsRecords2.getDouble("Saldo"));
                    }
                } while (rsRecords.next());
            }
        }catch(Exception ex){
            System.out.println("Error al listar tabla de DOCUMENTOS : " + ex);
            Notification.show("ERROR AL LEER DOCUMENTOS VENTA PARA REGISTRAR PAGOS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public void llenarComboProveedor() {
        queryString = " SELECT * from proveedor ";
        queryString += " WHERE Inhabilitado = 0 ";
        queryString += " AND EsCliente = 1";
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

                cuentaContable11Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable11Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable12Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable12Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable13Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable13Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable14Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable14Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

            }

        } catch (Exception ex1) {
            System.out.println("Error al combo cuentas contables: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void generarPartidaDocumentosVenta() {

        codigoDepositosList = new ArrayList<String>();
        tipoDocaList = new ArrayList<String>();
        noDocaList = new ArrayList<String>();

        codigoDepositosList.clear();
        tipoDocaList.clear();
        noDocaList.clear();

        String cuentaContable = "";

        try {

            queryString = " select * from contabilidad_partida ";
            queryString += " where IdProveedor = " + proveedorCbx.getValue();
            queryString += " and IdNomenclatura = " + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getAnticiposClientes();
            queryString += " and IdEmpresa = " + empresaCbx.getValue();
            queryString += " and Saldo > 0.00 ";

            System.out.println("mostrar anticipos " + queryString);

            int contador = 0;

            stQuery2 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords2 = stQuery2.executeQuery(queryString);

            while (rsRecords2.next()) { //  encontrado                

                if (contador == 0) {
                    codigoDepositosList.add(rsRecords2.getString("CodigoPartida"));
                    tipoDocaList.add(rsRecords2.getString("TipoDoca"));
                    noDocaList.add(rsRecords2.getString("NoDoca"));
                    cuentaContable1Cbx.select(rsRecords2.getString("IdNomenclatura"));
                    debe1Txt.setValue(rsRecords2.getDouble("haber"));
                    //haber1Txt.setValue(rsRecords2.getDouble("Haber"));
                }
                if (contador == 1) {
                    codigoDepositosList.add(rsRecords2.getString("CodigoPartida"));
                    tipoDocaList.add(rsRecords2.getString("TipoDoca"));
                    noDocaList.add(rsRecords2.getString("NoDoca"));
                    cuentaContable2Cbx.select(rsRecords2.getString("IdNomenclatura"));
                    debe2Txt.setValue(rsRecords2.getString("haber"));
                    //haber2Txt.setValue(rsRecords2.getDouble("Haber"));
                }
                if (contador == 2) {
                    codigoDepositosList.add(rsRecords2.getString("CodigoPartida"));
                    tipoDocaList.add(rsRecords2.getString("TipoDoca"));
                    noDocaList.add(rsRecords2.getString("NoDoca"));
                    cuentaContable3Cbx.select(rsRecords2.getString("IdNomenclatura"));
                    debe3Txt.setValue(rsRecords2.getString("haber"));
                    //haber3Txt.setValue(rsRecords2.getDouble("Haber"));
                }
                if (contador == 3) {
                    codigoDepositosList.add(rsRecords2.getString("CodigoPartida"));
                    tipoDocaList.add(rsRecords2.getString("TipoDoca"));
                    noDocaList.add(rsRecords2.getString("NoDoca"));
                    cuentaContable4Cbx.select(rsRecords2.getString("IdNomenclatura"));
                    debe4Txt.setValue(rsRecords2.getString("haber"));
                    //haber4Txt.setValue(rsRecords2.getDouble("Haber"));
                }
                if (contador == 4) {
                    codigoDepositosList.add(rsRecords2.getString("CodigoPartida"));
                    tipoDocaList.add(rsRecords2.getString("TipoDoca"));
                    noDocaList.add(rsRecords2.getString("NoDoca"));
                    cuentaContable5Cbx.select(rsRecords2.getString("IdNomenclatura"));
                    debe5Txt.setValue(rsRecords2.getString("haber"));
                    //haber5Txt.setValue(rsRecords2.getDouble("Haber"));
                }
                if (contador == 5) {
                    codigoDepositosList.add(rsRecords2.getString("CodigoPartida"));
                    tipoDocaList.add(rsRecords2.getString("TipoDoca"));
                    noDocaList.add(rsRecords2.getString("NoDoca"));
                    cuentaContable6Cbx.select(rsRecords2.getString("IdNomenclatura"));
                    debe6Txt.setValue(rsRecords2.getString("haber"));
                  //  haber6Txt.setValue(rsRecords2.getDouble("Haber"));
                }
                if (contador == 6) {
                    codigoDepositosList.add(rsRecords2.getString("CodigoPartida"));
                    tipoDocaList.add(rsRecords2.getString("TipoDoca"));
                    noDocaList.add(rsRecords2.getString("NoDoca"));
                    cuentaContable7Cbx.select(rsRecords2.getString("IdNomenclatura"));
                    debe7Txt.setValue(rsRecords2.getString("haber"));
                //    haber7Txt.setValue(rsRecords2.getDouble("Haber"));
                }
                if (contador == 7) {
                    codigoDepositosList.add(rsRecords2.getString("CodigoPartida"));
                    tipoDocaList.add(rsRecords2.getString("TipoDoca"));
                    noDocaList.add(rsRecords2.getString("NoDoca"));
                    cuentaContable8Cbx.select(rsRecords2.getString("IdNomenclatura"));
                    debe8Txt.setValue(rsRecords2.getString("haber"));
                   // haber8Txt.setValue(rsRecords2.getDouble("Haber"));
                }
                if (contador == 8) {
                    codigoDepositosList.add(rsRecords2.getString("CodigoPartida"));
                    tipoDocaList.add(rsRecords2.getString("TipoDoca"));
                    noDocaList.add(rsRecords2.getString("NoDoca"));
                    cuentaContable9Cbx.select(rsRecords2.getString("IdNomenclatura"));
                    debe9Txt.setValue(rsRecords2.getString("haber"));
              //      haber9Txt.setValue(rsRecords2.getDouble("Haber"));
                }
                if (contador == 9) {
                    codigoDepositosList.add(rsRecords2.getString("CodigoPartida"));
                    tipoDocaList.add(rsRecords2.getString("TipoDoca"));
                    noDocaList.add(rsRecords2.getString("NoDoca"));
                    cuentaContable10Cbx.select(rsRecords2.getString("IdNomenclatura"));
                    debe10Txt.setValue(rsRecords2.getString("haber"));
                  //  haber10Txt.setValue(rsRecords2.getDouble("Haber"));
                }
                if (contador == 10) {
                    codigoDepositosList.add(rsRecords2.getString("CodigoPartida"));
                    tipoDocaList.add(rsRecords2.getString("TipoDoca"));
                    noDocaList.add(rsRecords2.getString("NoDoca"));
                    cuentaContable11Cbx.select(rsRecords2.getString("IdNomenclatura"));
                    debe11Txt.setValue(rsRecords2.getString("haber"));
                 //   haber11Txt.setValue(rsRecords2.getDouble("Haber"));
                }
                if (contador == 11) {
                    codigoDepositosList.add(rsRecords2.getString("CodigoPartida"));
                    tipoDocaList.add(rsRecords2.getString("TipoDoca"));
                    noDocaList.add(rsRecords2.getString("NoDoca"));
                    cuentaContable12Cbx.select(rsRecords2.getString("IdNomenclatura"));
                    debe12Txt.setValue(rsRecords2.getString("haber"));
                //    haber12Txt.setValue(rsRecords2.getDouble("Haber"));
                }
                if (contador == 12) {
                    codigoDepositosList.add(rsRecords2.getString("CodigoPartida"));
                    tipoDocaList.add(rsRecords2.getString("TipoDoca"));
                    noDocaList.add(rsRecords2.getString("NoDoca"));
                    cuentaContable13Cbx.select(rsRecords2.getString("IdNomenclatura"));
                    debe13Txt.setValue(rsRecords2.getString("haber"));
              //      haber13Txt.setValue(rsRecords2.getDouble("Haber"));
                }
                if (contador == 13) {
                    codigoDepositosList.add(rsRecords2.getString("CodigoPartida"));
                    tipoDocaList.add(rsRecords2.getString("TipoDoca"));
                    noDocaList.add(rsRecords2.getString("NoDoca"));
                    cuentaContable14Cbx.select(rsRecords2.getString("IdNomenclatura"));
                    debe14Txt.setValue(rsRecords2.getString("haber"));
            //        haber14Txt.setValue(rsRecords2.getDouble("Haber"));
                }

                contador = contador + 1;
            }

            queryString = " SELECT * from contabilidad_Partida ";
            queryString += " WHERE IdNomenclatura = " + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getClientes();
            queryString += " And TipoDocumento = 'FACTURA VENTA' ";
            queryString += " And Saldo > 0.00";
            queryString += " And IdProveedor = " + proveedorCbx.getValue();
            queryString += " And CodigoPartida = " + facturasPorPagarCbx.getValue();

//            System.out.println("mostrar el monto " + queryString);

            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords1 = stQuery1.executeQuery(queryString);

            while (rsRecords1.next()) {
                valorFacturaPorPagar = rsRecords1.getDouble("Debe");
                cuentaContable = rsRecords1.getString("IdNomenclatura");
            }

            if (cuentaContable2Cbx.getValue() == null) {
                cuentaContable2Cbx.select(cuentaContable);
                haber2Txt.setValue(valorFacturaPorPagar);
            } else if (cuentaContable3Cbx.getValue() == null) {
                cuentaContable3Cbx.select(cuentaContable);
                haber3Txt.setValue(valorFacturaPorPagar);
            } else if (cuentaContable4Cbx.getValue() == null) {
                cuentaContable4Cbx.select(cuentaContable);
                haber4Txt.setValue(valorFacturaPorPagar);
            } else if (cuentaContable5Cbx.getValue() == null) {
                cuentaContable5Cbx.select(cuentaContable);
                haber5Txt.setValue(valorFacturaPorPagar);
            } else if (cuentaContable6Cbx.getValue() == null) {
                cuentaContable6Cbx.select(cuentaContable);
                haber6Txt.setValue(valorFacturaPorPagar);
            } else if (cuentaContable7Cbx.getValue() == null) {
                cuentaContable7Cbx.select(cuentaContable);
                haber7Txt.setValue(valorFacturaPorPagar);
            } else if (cuentaContable8Cbx.getValue() == null) {
                cuentaContable8Cbx.select(cuentaContable);
                haber8Txt.setValue(valorFacturaPorPagar);
            } else if (cuentaContable9Cbx.getValue() == null) {
                cuentaContable9Cbx.select(cuentaContable);
                haber9Txt.setValue(valorFacturaPorPagar);
            } else if (cuentaContable10Cbx.getValue() == null) {
                cuentaContable10Cbx.select(cuentaContable);
                haber10Txt.setValue(valorFacturaPorPagar);
            } else if (cuentaContable10Cbx.getValue() == null) {
                cuentaContable10Cbx.select(cuentaContable);
                haber10Txt.setValue(valorFacturaPorPagar);
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar cuentas contables: " + ex1.getMessage());
            ex1.printStackTrace();
        }

    }

    public void actualizarSaldosFacturas() {

        for (int i = 0; i < codigoDepositosList.size(); i++) {

            queryString = " Update  contabilidad_partida ";
            queryString += " Set Saldo = 0";
            queryString += " Where CodigoPartida = '" + codigoDepositosList.get(i) + "'";
//            System.out.println("update 1" + queryString);
            try {
                stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                stQuery.executeUpdate(queryString);

            } catch (Exception ex1) {
                System.out.println("Error al actualizar el saldo de los depositos" + ex1.getMessage());
                ex1.printStackTrace();
            }
        }

        queryString = " Update  contabilidad_partida ";
        queryString += " Set Saldo = 0";
        queryString += " Where CodigoPartida = '" + facturasPorPagarCbx.getValue() + "'";
//        System.out.println("update 2" + queryString);
        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

        } catch (Exception ex1) {
            System.out.println("Error al actualizar el saldo de la factura " + ex1.getMessage());
            ex1.printStackTrace();
        }

    }

    public void ingresarPagoDocumentoVenta() {

        totalDebe = new BigDecimal(debe1Txt.getDoubleValueDoNotThrow()
                + debe2Txt.getDoubleValueDoNotThrow() + debe3Txt.getDoubleValueDoNotThrow()
                + debe4Txt.getDoubleValueDoNotThrow() + debe5Txt.getDoubleValueDoNotThrow()
                + debe6Txt.getDoubleValueDoNotThrow() + debe7Txt.getDoubleValueDoNotThrow()
                + debe8Txt.getDoubleValueDoNotThrow() + debe9Txt.getDoubleValueDoNotThrow()
                + debe10Txt.getDoubleValueDoNotThrow() + debe11Txt.getDoubleValueDoNotThrow()
                + debe12Txt.getDoubleValueDoNotThrow() + debe13Txt.getDoubleValueDoNotThrow()
                + debe14Txt.getDoubleValueDoNotThrow()).setScale(2, BigDecimal.ROUND_HALF_UP);

        totalHaber = new BigDecimal(haber1Txt.getDoubleValueDoNotThrow()
                + haber2Txt.getDoubleValueDoNotThrow() + haber3Txt.getDoubleValueDoNotThrow()
                + haber4Txt.getDoubleValueDoNotThrow() + haber5Txt.getDoubleValueDoNotThrow()
                + haber6Txt.getDoubleValueDoNotThrow() + haber7Txt.getDoubleValueDoNotThrow()
                + haber8Txt.getDoubleValueDoNotThrow() + haber9Txt.getDoubleValueDoNotThrow()
                + haber10Txt.getDoubleValueDoNotThrow() + haber11Txt.getDoubleValueDoNotThrow()
                + haber12Txt.getDoubleValueDoNotThrow() + haber13Txt.getDoubleValueDoNotThrow()
                + haber14Txt.getDoubleValueDoNotThrow()).setScale(2, BigDecimal.ROUND_HALF_UP);

        totalDebe.setScale(2, BigDecimal.ROUND_HALF_UP);
        totalHaber.setScale(2, BigDecimal.ROUND_HALF_UP);

        if (proveedorCbx.getValue() == null) {
            Notification.show("Por favor, seleccione un cliente..", Notification.Type.ERROR_MESSAGE);
            proveedorCbx.focus();
            return;
        }

        if (facturasPorPagarCbx.getValue() == null) {
            Notification.show("Por favor, seleccione una factura por pagar..", Notification.Type.ERROR_MESSAGE);
            proveedorCbx.focus();
            return;
        }

        if (totalDebe.doubleValue() != totalHaber.doubleValue()) {
            System.out.println("Debe =" + totalDebe.doubleValue() + "  haber=" + totalHaber);
            Notification.show("La partida es descuadrada, por favor revisar"
                    + " Debe = " + totalDebe.doubleValue() + "  Haber = " + totalHaber, Notification.Type.WARNING_MESSAGE);
            return;
        }

        if (montoTxt.getDoubleValueDoNotThrow() == 0.00) {
            Notification.show("Por favor, Ingrese un monto..", Notification.Type.ERROR_MESSAGE);
            montoTxt.focus();
            return;
        }

        if (descripcionTxt.getValue().isEmpty()) {
            Notification.show("Por favor, Ingrese una descripción..", Notification.Type.ERROR_MESSAGE);
            descripcionTxt.focus();
            return;
        }
        if (monedaCbx.getValue().equals("DOLARES") && tipoCambioTxt.getDoubleValueDoNotThrow() <= 1.00) {
            Notification.show("Si la transacción es en DOLARES, debe llebar tipo de cambio. Por favor, revise la moneda.", Notification.Type.ERROR_MESSAGE);
            monedaCbx.focus();
            return;
        }
        if (medioCbx.getValue() == null) {
            Notification.show("Por favor seleccionar un Medio.", Notification.Type.ERROR_MESSAGE);
            medioCbx.focus();
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
        queryString += " SerieDocumento, NumeroDocumento, IdNomenclatura, MonedaDocumento, Debe, Haber,";
        queryString += " DebeQuetzales, HaberQuetzales, TipoCambio, Saldo, Descripcion,TipoDoca, NoDoca,";
        queryString += " CreadoUsuario, CreadoFechaYHora)";
        queryString += " Values ";
        queryString += "(";
        queryString += String.valueOf(empresaCbx.getValue());
        queryString += ",'INGRESADO'";
        queryString += ",'" + codigoPartida + "'";
        queryString += ",'" + codigoPartida + "'";
        queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
        queryString += ",'" + String.valueOf(tipoEngancheCbx.getValue()) + "'";
        queryString += ",'" + referenciaTxt.getValue() + "'";
        queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
        queryString += "," + proveedorCbx.getValue();
        queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
        queryString += ",''";
        queryString += ",'" + numeroTxt.getValue() + "'";
        queryString += "," + cuentaContable1Cbx.getValue();
        queryString += ",'" + monedaCbx.getValue() + "'";
        queryString += "," + String.valueOf(debe1Txt.getDoubleValueDoNotThrow()); // DEBE
        queryString += ",0.00"; //HABER        
        queryString += "," + String.valueOf(debe1Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());
        queryString += ",0.00"; //HABER Q.        
        queryString += "," + String.valueOf(tipoCambioTxt.getDoubleValueDoNotThrow());
        queryString += ",0.00";
        queryString += ",'Ingreso por pago de mantenimiento de factura " + descripcionTxt.getValue() + "'";
        queryString += ",''";
        queryString += ",''";
        queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
        queryString += ",current_timestamp";
        queryString += ")";

        if ((cuentaContable2Cbx.getValue() != null && debe2Txt.getDoubleValueDoNotThrow() != 0.00)
                || (cuentaContable2Cbx.getValue() != null && haber2Txt.getDoubleValueDoNotThrow() != 0.00)) {

            queryString += ",(";
            queryString += String.valueOf(empresaCbx.getValue());
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";

            if (codigoDepositosList.size() > 0) {
                queryString += ",'" + codigoDepositosList.get(0) + "'";
            } else {
                queryString += ",'" + facturasPorPagarCbx.getValue() + "'";
            }

            queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
            queryString += ",'" + String.valueOf(tipoEngancheCbx.getValue()) + "'";
            queryString += ",'" + referenciaTxt.getValue() + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            queryString += ",''";
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += "," + cuentaContable2Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(debe2Txt.getDoubleValueDoNotThrow());  //debe
            queryString += "," + String.valueOf(haber2Txt.getDoubleValueDoNotThrow());  //HABER        
            queryString += "," + String.valueOf(debe2Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());  // DEBE Q.
            queryString += "," + String.valueOf(haber2Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00";
            queryString += ",'Ingreso por pago de mantenimiento de factura " + descripcionTxt.getValue() + "'";

            if (tipoDocaList.size() > 0) {
                queryString += ",'" + tipoDocaList.get(0) + "'";
            } else {
                queryString += ",''";
            }
            if (noDocaList.size() > 0) {
                queryString += ",'" + noDocaList.get(0) + "'";
            } else {
                queryString += ",''";
            }
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";
        }

        if ((cuentaContable3Cbx.getValue() != null && debe3Txt.getDoubleValueDoNotThrow() != 0.00)
                || (cuentaContable3Cbx.getValue() != null && haber3Txt.getDoubleValueDoNotThrow() != 0.00)) {

            queryString += ",(";
            queryString += String.valueOf(empresaCbx.getValue());
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";

            if (codigoDepositosList.size() > 1) {
                queryString += ",'" + codigoDepositosList.get(1) + "'";
            } else {
                queryString += ",'" + facturasPorPagarCbx.getValue() + "'";
            }

            queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
            queryString += ",'" + String.valueOf(tipoEngancheCbx.getValue()) + "'";
            queryString += ",'" + referenciaTxt.getValue() + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            queryString += ",''";
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += "," + cuentaContable3Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(debe3Txt.getDoubleValueDoNotThrow());  //debe
            queryString += "," + String.valueOf(haber3Txt.getDoubleValueDoNotThrow());  //HABER        
            queryString += "," + String.valueOf(debe3Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());  // DEBE Q.
            queryString += "," + String.valueOf(haber3Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00";
            queryString += ",'Ingreso por pago de mantenimiento de factura " + descripcionTxt.getValue() + "'";
            if (tipoDocaList.size() > 1) {
                queryString += ",'" + tipoDocaList.get(1) + "'";
            } else {
                queryString += ",''";
            }
            if (noDocaList.size() > 1) {
                queryString += ",'" + noDocaList.get(1) + "'";
            } else {
                queryString += ",''";
            }
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";
        }

        if ((cuentaContable4Cbx.getValue() != null && debe4Txt.getDoubleValueDoNotThrow() != 0.00)
                || (cuentaContable4Cbx.getValue() != null && haber4Txt.getDoubleValueDoNotThrow() != 0.00)) {
            queryString += ",(";
            queryString += String.valueOf(empresaCbx.getValue());
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";

            if (codigoDepositosList.size() > 2) {
                queryString += ",'" + codigoDepositosList.get(2) + "'";
            } else {
                queryString += ",'" + facturasPorPagarCbx.getValue() + "'";
            }

            queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
            queryString += ",'" + String.valueOf(tipoEngancheCbx.getValue()) + "'";
            queryString += ",'" + referenciaTxt.getValue() + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            queryString += ",''";
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += "," + cuentaContable4Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(debe4Txt.getDoubleValueDoNotThrow());  //debe
            queryString += "," + String.valueOf(haber4Txt.getDoubleValueDoNotThrow());  //HABER        
            queryString += "," + String.valueOf(debe4Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());  // DEBE Q.
            queryString += "," + String.valueOf(haber4Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00";
            queryString += ",'Ingreso por pago de mantenimiento de factura " + descripcionTxt.getValue() + "'";
            if (tipoDocaList.size() > 2) {
                queryString += ",'" + tipoDocaList.get(2) + "'";
            } else {
                queryString += ",''";
            }
            if (noDocaList.size() > 2) {
                queryString += ",'" + noDocaList.get(2) + "'";
            } else {
                queryString += ",''";
            }
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";
        }

        if ((cuentaContable5Cbx.getValue() != null && debe5Txt.getDoubleValueDoNotThrow() != 0.00)
                || (cuentaContable5Cbx.getValue() != null && haber5Txt.getDoubleValueDoNotThrow() != 0.00)) {

            queryString += ",(";
            queryString += String.valueOf(empresaCbx.getValue());
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";

            if (codigoDepositosList.size() > 3) {
                queryString += ",'" + codigoDepositosList.get(3) + "'";
            } else {
                queryString += ",'" + facturasPorPagarCbx.getValue() + "'";
            }
            queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
            queryString += ",'" + String.valueOf(tipoEngancheCbx.getValue()) + "'";
            queryString += ",'" + referenciaTxt.getValue() + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            queryString += ",''";
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += "," + cuentaContable5Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(debe5Txt.getDoubleValueDoNotThrow());  //debe
            queryString += "," + String.valueOf(haber5Txt.getDoubleValueDoNotThrow());  //HABER        
            queryString += "," + String.valueOf(debe5Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());  // DEBE Q.
            queryString += "," + String.valueOf(haber5Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00";
            queryString += ",'Ingreso por pago de mantenimineto de factura " + descripcionTxt.getValue() + "'";
            if (tipoDocaList.size() > 3) {
                queryString += ",'" + tipoDocaList.get(3) + "'";
            } else {
                queryString += ",''";
            }
            if (noDocaList.size() > 3) {
                queryString += ",'" + noDocaList.get(3) + "'";
            } else {
                queryString += ",''";
            }
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";
        }

        if ((cuentaContable6Cbx.getValue() != null && debe6Txt.getDoubleValueDoNotThrow() != 0.00)
                || (cuentaContable6Cbx.getValue() != null && haber6Txt.getDoubleValueDoNotThrow() != 0.00)) {

            queryString += ",(";
            queryString += String.valueOf(empresaCbx.getValue());
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";

            if (codigoDepositosList.size() > 4) {
                queryString += ",'" + codigoDepositosList.get(4) + "'";
            } else {
                queryString += ",'" + facturasPorPagarCbx.getValue() + "'";
            }

            queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
            queryString += ",'" + String.valueOf(tipoEngancheCbx.getValue()) + "'";
            queryString += ",'" + referenciaTxt.getValue() + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            queryString += ",''";
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += "," + cuentaContable6Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(debe6Txt.getDoubleValueDoNotThrow());  //debe
            queryString += "," + String.valueOf(haber6Txt.getDoubleValueDoNotThrow());  //HABER        
            queryString += "," + String.valueOf(debe6Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());  // DEBE Q.
            queryString += "," + String.valueOf(haber6Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00";
            queryString += ",'Ingreso por pago de mantenimineto de factura " + descripcionTxt.getValue() + "'";
            if (tipoDocaList.size() > 4) {
                queryString += ",'" + tipoDocaList.get(4) + "'";
            } else {
                queryString += ",''";
            }
            if (noDocaList.size() > 4) {
                queryString += ",'" + noDocaList.get(4) + "'";
            } else {
                queryString += ",''";
            }
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";
        }

        if ((cuentaContable7Cbx.getValue() != null && debe7Txt.getDoubleValueDoNotThrow() != 0.00)
                || (cuentaContable7Cbx.getValue() != null && haber7Txt.getDoubleValueDoNotThrow() != 0.00)) {
            queryString += ",(";
            queryString += String.valueOf(empresaCbx.getValue());
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            if (codigoDepositosList.size() > 5) {
                queryString += ",'" + codigoDepositosList.get(5) + "'";
            } else {
                queryString += ",'" + facturasPorPagarCbx.getValue() + "'";
            }
            queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
            queryString += ",'" + String.valueOf(tipoEngancheCbx.getValue()) + "'";
            queryString += ",'" + referenciaTxt.getValue() + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            queryString += ",''";
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += "," + cuentaContable7Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(debe7Txt.getDoubleValueDoNotThrow());  //debe
            queryString += "," + String.valueOf(haber7Txt.getDoubleValueDoNotThrow());  //HABER        
            queryString += "," + String.valueOf(debe7Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());  // DEBE Q.
            queryString += "," + String.valueOf(haber7Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00";
            queryString += ",'Ingreso por pago de mantenimineto de factura " + descripcionTxt.getValue() + "'";
            if (tipoDocaList.size() > 5) {
                queryString += ",'" + tipoDocaList.get(5) + "'";
            } else {
                queryString += ",''";
            }
            if (noDocaList.size() > 5) {
                queryString += ",'" + noDocaList.get(5) + "'";
            } else {
                queryString += ",''";
            }
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";

        }

        if ((cuentaContable8Cbx.getValue() != null && debe8Txt.getDoubleValueDoNotThrow() != 0.00)
                || (cuentaContable8Cbx.getValue() != null && haber8Txt.getDoubleValueDoNotThrow() != 0.00)) {

            queryString += ",(";
            queryString += String.valueOf(empresaCbx.getValue());
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            if (codigoDepositosList.size() > 6) {
                queryString += ",'" + codigoDepositosList.get(6) + "'";
            } else {
                queryString += ",'" + facturasPorPagarCbx.getValue() + "'";
            }

            queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
            queryString += ",'" + String.valueOf(tipoEngancheCbx.getValue()) + "'";
            queryString += ",'" + referenciaTxt.getValue() + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            queryString += ",''";
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += "," + cuentaContable8Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(debe8Txt.getDoubleValueDoNotThrow());  //debe
            queryString += "," + String.valueOf(haber8Txt.getDoubleValueDoNotThrow());  //HABER        
            queryString += "," + String.valueOf(debe8Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());  // DEBE Q.
            queryString += "," + String.valueOf(haber8Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00";
            queryString += ",'Ingreso por pago de mantenimineto de factura " + descripcionTxt.getValue() + "'";
            if (tipoDocaList.size() > 6) {
                queryString += ",'" + tipoDocaList.get(6) + "'";
            } else {
                queryString += ",''";
            }
            if (noDocaList.size() > 6) {
                queryString += ",'" + noDocaList.get(6) + "'";
            } else {
                queryString += ",''";
            }
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";

        }

        if ((cuentaContable9Cbx.getValue() != null && debe9Txt.getDoubleValueDoNotThrow() != 0.00)
                || (cuentaContable9Cbx.getValue() != null && haber9Txt.getDoubleValueDoNotThrow() != 0.00)) {

            queryString += ",(";
            queryString += String.valueOf(empresaCbx.getValue());
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            if (codigoDepositosList.size() > 7) {
                queryString += ",'" + codigoDepositosList.get(7) + "'";
            } else {
                queryString += ",'" + facturasPorPagarCbx.getValue() + "'";
            }

            queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
            queryString += ",'" + String.valueOf(tipoEngancheCbx.getValue()) + "'";
            queryString += ",'" + referenciaTxt.getValue() + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            queryString += ",''";
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += "," + cuentaContable9Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(debe9Txt.getDoubleValueDoNotThrow());  //debe
            queryString += "," + String.valueOf(haber9Txt.getDoubleValueDoNotThrow());  //HABER        
            queryString += "," + String.valueOf(debe9Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());  // DEBE Q.
            queryString += "," + String.valueOf(haber9Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00";
            queryString += ",'Ingreso por pago de mantenimineto de factura " + descripcionTxt.getValue() + "'";
            if (tipoDocaList.size() > 7) {
                queryString += ",'" + tipoDocaList.get(7) + "'";
            } else {
                queryString += ",''";
            }
            if (noDocaList.size() > 7) {
                queryString += ",'" + noDocaList.get(7) + "'";
            } else {
                queryString += ",''";
            }
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";
        }

        if ((cuentaContable10Cbx.getValue() != null && debe10Txt.getDoubleValueDoNotThrow() != 0.00)
                || (cuentaContable10Cbx.getValue() != null && haber10Txt.getDoubleValueDoNotThrow() != 0.00)) {

            queryString += ",(";
            queryString += String.valueOf(empresaCbx.getValue());
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            if (codigoDepositosList.size() > 8) {
                queryString += ",'" + codigoDepositosList.get(8) + "'";
            } else {
                queryString += ",'" + facturasPorPagarCbx.getValue() + "'";
            }

            queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
            queryString += ",'" + String.valueOf(tipoEngancheCbx.getValue()) + "'";
            queryString += ",'" + referenciaTxt.getValue() + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            queryString += ",''";
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += "," + cuentaContable10Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(debe10Txt.getDoubleValueDoNotThrow());  //debe
            queryString += "," + String.valueOf(haber10Txt.getDoubleValueDoNotThrow());  //HABER        
            queryString += "," + String.valueOf(debe10Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());  // DEBE Q.
            queryString += "," + String.valueOf(haber10Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00";
            queryString += ",'Ingreso por pago de mantenimineto de factura " + descripcionTxt.getValue() + "'";
            if (tipoDocaList.size() > 8) {
                queryString += ",'" + tipoDocaList.get(8) + "'";
            } else {
                queryString += ",''";
            }
            if (noDocaList.size() > 8) {
                queryString += ",'" + noDocaList.get(8) + "'";
            } else {
                queryString += ",''";
            }
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";
        }

        if ((cuentaContable11Cbx.getValue() != null && debe11Txt.getDoubleValueDoNotThrow() != 0.00)
                || (cuentaContable11Cbx.getValue() != null && haber11Txt.getDoubleValueDoNotThrow() != 0.00)) {
            queryString += ",(";
            queryString += String.valueOf(empresaCbx.getValue());
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            if (codigoDepositosList.size() > 9) {
                queryString += ",'" + codigoDepositosList.get(9) + "'";
            } else {
                queryString += ",'" + facturasPorPagarCbx.getValue() + "'";
            }

            queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
            queryString += ",'" + String.valueOf(tipoEngancheCbx.getValue()) + "'";
            queryString += ",'" + referenciaTxt.getValue() + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            queryString += ",''";
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += "," + cuentaContable11Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(debe11Txt.getDoubleValueDoNotThrow());  //debe
            queryString += "," + String.valueOf(haber11Txt.getDoubleValueDoNotThrow());  //HABER        
            queryString += "," + String.valueOf(debe11Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());  // DEBE Q.
            queryString += "," + String.valueOf(haber11Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00";
            queryString += ",'Ingreso por pago de mantenimineto de factura " + descripcionTxt.getValue() + "'";
            if (tipoDocaList.size() > 9) {
                queryString += ",'" + tipoDocaList.get(9) + "'";
            } else {
                queryString += ",''";
            }
            if (noDocaList.size() > 9) {
                queryString += ",'" + noDocaList.get(9) + "'";
            } else {
                queryString += ",''";
            }
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";

        }

        if ((cuentaContable12Cbx.getValue() != null && debe12Txt.getDoubleValueDoNotThrow() != 0.00)
                || (cuentaContable12Cbx.getValue() != null && haber12Txt.getDoubleValueDoNotThrow() != 0.00)) {

            queryString += ",(";
            queryString += String.valueOf(empresaCbx.getValue());
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            if (codigoDepositosList.size() > 10) {
                queryString += ",'" + codigoDepositosList.get(10) + "'";
            } else {
                queryString += ",'" + facturasPorPagarCbx.getValue() + "'";
            }

            queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
            queryString += ",'" + String.valueOf(tipoEngancheCbx.getValue()) + "'";
            queryString += ",'" + referenciaTxt.getValue() + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            queryString += ",''";
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += "," + cuentaContable12Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(debe12Txt.getDoubleValueDoNotThrow());  //debe
            queryString += "," + String.valueOf(haber12Txt.getDoubleValueDoNotThrow());  //HABER        
            queryString += "," + String.valueOf(debe12Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());  // DEBE Q.
            queryString += "," + String.valueOf(haber12Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00";
            queryString += ",'Ingreso por pago de mantenimineto de factura " + descripcionTxt.getValue() + "'";
            if (tipoDocaList.size() > 10) {
                queryString += ",'" + tipoDocaList.get(10) + "'";
            } else {
                queryString += ",''";
            }
            if (noDocaList.size() > 10) {
                queryString += ",'" + noDocaList.get(10) + "'";
            } else {
                queryString += ",''";
            }
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";
        }
        
         if ((cuentaContable13Cbx.getValue() != null && debe13Txt.getDoubleValueDoNotThrow() != 0.00)
                || (cuentaContable13Cbx.getValue() != null && haber13Txt.getDoubleValueDoNotThrow() != 0.00)) {

            queryString += ",(";
            queryString += String.valueOf(empresaCbx.getValue());
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            if (codigoDepositosList.size() > 11) {
                queryString += ",'" + codigoDepositosList.get(11) + "'";
            } else {
                queryString += ",'" + facturasPorPagarCbx.getValue() + "'";
            }

            queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
            queryString += ",'" + String.valueOf(tipoEngancheCbx.getValue()) + "'";
            queryString += ",'" + referenciaTxt.getValue() + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            queryString += ",''";
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += "," + cuentaContable13Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(debe13Txt.getDoubleValueDoNotThrow());  //debe
            queryString += "," + String.valueOf(haber13Txt.getDoubleValueDoNotThrow());  //HABER        
            queryString += "," + String.valueOf(debe13Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());  // DEBE Q.
            queryString += "," + String.valueOf(haber13Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00";
            queryString += ",'Ingreso por pago de mantenimineto de factura " + descripcionTxt.getValue() + "'";
            if (tipoDocaList.size() > 11) {
                queryString += ",'" + tipoDocaList.get(11) + "'";
            } else {
                queryString += ",''";
            }
            if (noDocaList.size() > 11) {
                queryString += ",'" + noDocaList.get(11) + "'";
            } else {
                queryString += ",''";
            }
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";
        }
         
          if ((cuentaContable14Cbx.getValue() != null && debe14Txt.getDoubleValueDoNotThrow() != 0.00)
                || (cuentaContable14Cbx.getValue() != null && haber14Txt.getDoubleValueDoNotThrow() != 0.00)) {

            queryString += ",(";
            queryString += String.valueOf(empresaCbx.getValue());
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            if (codigoDepositosList.size() > 12) {
                queryString += ",'" + codigoDepositosList.get(12) + "'";
            } else {
                queryString += ",'" + facturasPorPagarCbx.getValue() + "'";
            }

            queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
            queryString += ",'" + String.valueOf(tipoEngancheCbx.getValue()) + "'";
            queryString += ",'" + referenciaTxt.getValue() + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            queryString += ",''";
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += "," + cuentaContable14Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(debe14Txt.getDoubleValueDoNotThrow());  //debe
            queryString += "," + String.valueOf(haber14Txt.getDoubleValueDoNotThrow());  //HABER        
            queryString += "," + String.valueOf(debe14Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());  // DEBE Q.
            queryString += "," + String.valueOf(haber14Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00";
            queryString += ",'Ingreso por pago de mantenimineto de factura " + descripcionTxt.getValue() + "'";
            if (tipoDocaList.size() > 12) {
                queryString += ",'" + tipoDocaList.get(12) + "'";
            } else {
                queryString += ",''";
            }
            if (noDocaList.size() > 12) {
                queryString += ",'" + noDocaList.get(12) + "'";
            } else {
                queryString += ",''";
            }
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";
        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "queryString Ingreso de pago de documento venta = " + queryString);

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            Notification.show("Ingreso REGISTRADO con exito!", Notification.Type.HUMANIZED_MESSAGE);

            close();

            limpiarCampos();
        } catch (Exception ex1) {
            System.out.println("Error al insertar pago documento venta: " + ex1.getMessage());
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

    public void limpiarCampos() {

        tipoEngancheCbx.setVisible(false);
        referenciaTxt.setVisible(false);

        proveedorCbx.clear();
        tipoEngancheCbx.clear();
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
                actualizarSaldosFacturas();
                ingresarPagoDocumentoVenta();

            }
        });
        mainLayout.addComponent(guardarBtn);
        mainLayout.setComponentAlignment(guardarBtn, Alignment.BOTTOM_CENTER);
    }

}
