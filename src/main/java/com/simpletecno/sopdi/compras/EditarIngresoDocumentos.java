package com.simpletecno.sopdi.compras;

import com.simpletecno.sopdi.utilerias.MyEmailMessanger;
import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.simpletecno.sopdi.utilerias.ValidarTokenForm;
import com.simpletecno.sopdi.ventas.FacturaVentaView;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.vaadin.ui.NumberField;

import javax.mail.MessagingException;

/**
 *
 * @author user
 */
public class EditarIngresoDocumentos extends Window {

    static final String IMAGEN_PROPERTY = "Imagen";

    VerticalLayout mainLayout;

    TextField serieTxt;
    TextField numeroTxt;
    TextField nitProveedorTxt;

    ComboBox tipoDocumentoCbx;
    ComboBox proveedorCbx;
    ComboBox monedaCbx;
    ComboBox ordenCompraCbx;
    ComboBox centroCostoCbx;
    ComboBox cuentaContableCbx;
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

    CheckBox checkbox1;

    String nombreArchvivo, tipoArchivo;
    Long pesoArchivo;

    DateField fechaDt;

    NumberField montoTxt;
    NumberField tasaCambioTxt;

    NumberField haberTxt;
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

    NumberField debeTxt;
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

    TextField centroCostoTxt;
    TextField centroCosto1Txt;
    TextField centroCosto2Txt;
    TextField centroCosto3Txt;
    TextField centroCosto4Txt;
    TextField centroCosto5Txt;
    TextField centroCosto6Txt;
    TextField centroCosto7Txt;
    TextField centroCosto8Txt;
    TextField centroCosto9Txt;
    TextField centroCosto10Txt;

    Button grabarBtn;
    Button cerrarFacturasBtn;
    StreamResource documentStreamResource;

    static final String NIT_PROPERTY = "Eliminar";

    Statement stQuery, stQuery2;
    PreparedStatement stPreparedQuery;
    ResultSet rsRecords, rsRecords2;

    BigDecimal totalDebe;
    BigDecimal totalHaber;

    String queryString;
    String variableTemp = "";
    String codigoPartidaEdit = "";
    String estatusPartidaEdit;
    String nuevoCodigoPartida;
    String descripcionEdit;

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public EditarIngresoDocumentos(String codigoPartida, String descripcion) {

        this.codigoPartidaEdit = codigoPartida;
        this.descripcionEdit = descripcion;

        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setResponsive(true);

        HorizontalLayout layoutTitle;
        layoutTitle = new HorizontalLayout();
        layoutTitle.setSpacing(true);
        layoutTitle.setMargin(true);
        layoutTitle.setWidth("100%");

        setResponsive(true);
        setWidth("98%");
        setHeight("98%");

        String titulo = empresaId+ " " + empresaNombre + " EDITAR DOCUMENTOS";

        Label titleLbl = new Label( titulo);
        titleLbl.addStyleName(ValoTheme.LABEL_H3);
        titleLbl.addStyleName("h3_custom");

        layoutTitle.addComponent(titleLbl);
        layoutTitle.setComponentAlignment(titleLbl, Alignment.BOTTOM_RIGHT);

        mainLayout.addComponent(layoutTitle);

        setContent(mainLayout);

        mainLayout.addComponent(crearComponentes());

        llenarCampos();

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

        centroCostoCbx = new ComboBox();
        centroCostoCbx.setInputPrompt("Centro de costo");
        centroCostoCbx.setDescription("Centro de costo");
        centroCostoCbx.setTextInputAllowed(false);
        centroCostoCbx.setNewItemsAllowed(false);
        centroCostoCbx.setNullSelectionAllowed(true);
        centroCostoCbx.addValueChangeListener(event -> {
            if (event.getProperty() == null) {
                return;
            }

            centroCostoTxt.setValue(centroCostoCbx.getItemCaption(centroCostoCbx.getValue()));
            centroCosto1Txt.setValue(centroCostoCbx.getItemCaption(centroCostoCbx.getValue()));
            centroCosto2Txt.setValue(centroCostoCbx.getItemCaption(centroCostoCbx.getValue()));
            centroCosto3Txt.setValue(centroCostoCbx.getItemCaption(centroCostoCbx.getValue()));
            centroCosto4Txt.setValue(centroCostoCbx.getItemCaption(centroCostoCbx.getValue()));
            centroCosto5Txt.setValue(centroCostoCbx.getItemCaption(centroCostoCbx.getValue()));
            centroCosto6Txt.setValue(centroCostoCbx.getItemCaption(centroCostoCbx.getValue()));
            centroCosto7Txt.setValue(centroCostoCbx.getItemCaption(centroCostoCbx.getValue()));
            centroCosto8Txt.setValue(centroCostoCbx.getItemCaption(centroCostoCbx.getValue()));
            centroCosto9Txt.setValue(centroCostoCbx.getItemCaption(centroCostoCbx.getValue()));
            centroCosto10Txt.setValue(centroCostoCbx.getItemCaption(centroCostoCbx.getValue()));

        });
        centroCostoCbx.setWidth("100%");

        llenarComboCentroCosto();

        ordenCompraCbx = new ComboBox("NOC");
        ordenCompraCbx.setWidth("100%");
        llenarComboOrdenCompra();

        tipoDocumentoCbx = new ComboBox();
        tipoDocumentoCbx.setInputPrompt("Tipo de documento");
        tipoDocumentoCbx.setDescription("Tipo de documento");
        tipoDocumentoCbx.setWidth("100%");
        tipoDocumentoCbx.addItem("FACTURA");
        tipoDocumentoCbx.addItem("RECIBO");
        tipoDocumentoCbx.addItem("RECIBO CONTABLE");
        tipoDocumentoCbx.addItem("RECIBO CORRIENTE");
        tipoDocumentoCbx.addItem("FORMULARIO IVA");
        tipoDocumentoCbx.addItem("FORMULARIO ISR");
        tipoDocumentoCbx.addItem("FORMULARIO ISR RETENIDO");
        tipoDocumentoCbx.addItem("FORMULARIO ISO");
        tipoDocumentoCbx.select("FACTURA");
        tipoDocumentoCbx.addValueChangeListener(event -> {
            if (event.getProperty().getValue().equals("FORMULARIO IVA")
                    || event.getProperty().getValue().equals("FORMULARIO ISR")
                    || event.getProperty().getValue().equals("FORMULARIO ISR RETENIDO")
                    || event.getProperty().getValue().equals("FORMULARIO ISO")) {
                cuentaContableCbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getInstituciones());
            } else {
                cuentaContableCbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getProveedores());
            }
        });

        serieTxt = new TextField();
        serieTxt.setInputPrompt("Serie del documento");
        serieTxt.setDescription("Serie del documento");
        serieTxt.setWidth("100%");
        serieTxt.addStyleName("mayusculas");

        numeroTxt = new TextField();
        numeroTxt.setInputPrompt("Número de documento");
        numeroTxt.setDescription("Número de documento");
        numeroTxt.setWidth("100%");

        proveedorCbx = new ComboBox();
        proveedorCbx.setInputPrompt("Proveedor");
        proveedorCbx.setDescription("Proveedor");
        proveedorCbx.setWidth("100%");
        proveedorCbx.addContainerProperty(NIT_PROPERTY, String.class, "");
        proveedorCbx.setFilteringMode(FilteringMode.CONTAINS);
        proveedorCbx.setInvalidAllowed(false);
        proveedorCbx.setNewItemsAllowed(false);
        proveedorCbx.setNullSelectionAllowed(false);
        proveedorCbx.addValueChangeListener(event -> {
            if (event.getProperty() == null) {
                return;
            }
            verificarProveedorInsititucion(String.valueOf(proveedorCbx.getValue()));
        });

        nitProveedorTxt = new TextField();
        nitProveedorTxt.setInputPrompt("Nit del proveedor");
        nitProveedorTxt.setDescription("Nit del proveedor");
        nitProveedorTxt.setWidth("100%");

        fechaDt = new DateField("Fecha : ");
        fechaDt.setDateFormat("dd/MM/yyyy");
        fechaDt.setWidth("100%");
        fechaDt.setValue(new java.util.Date());

        montoTxt = new NumberField();
        montoTxt.setInputPrompt("Monto del documento");
        montoTxt.setDescription("Monto del documento");
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

        monedaCbx = new ComboBox();
        monedaCbx.setInputPrompt("Moneda");
        monedaCbx.setWidth("100%");
        monedaCbx.addItem("QUETZALES");
        monedaCbx.addItem("DOLARES");
        monedaCbx.select("QUETZALES");
        monedaCbx.setInvalidAllowed(false);
        monedaCbx.setNewItemsAllowed(false);
        monedaCbx.addValueChangeListener((event) -> {
            if (monedaCbx.getValue() != null) {
                if (monedaCbx.getValue().equals("DOLARES")) {
                    tasaCambioTxt.setValue((Float.toString(((SopdiUI) UI.getCurrent()).sessionInformation.getFltExchangeRate())));
                } else {
                    tasaCambioTxt.setValue(1.00);
                }
            } else {
                tasaCambioTxt.setValue(1.00);
            }
        });

        tasaCambioTxt = new NumberField();
        tasaCambioTxt.setInputPrompt("Tasa de cambio");
        tasaCambioTxt.setDescription("Tasa de cambio");
        tasaCambioTxt.setDecimalAllowed(true);
        tasaCambioTxt.setDecimalPrecision(5);
        tasaCambioTxt.setMinimumFractionDigits(5);
        tasaCambioTxt.setDecimalSeparator('.');
        tasaCambioTxt.setDecimalSeparatorAlwaysShown(true);
        tasaCambioTxt.setValue(0d);
        tasaCambioTxt.setGroupingUsed(true);
        tasaCambioTxt.setGroupingSeparator(',');
        tasaCambioTxt.setGroupingSize(3);
        tasaCambioTxt.setImmediate(true);
        tasaCambioTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        tasaCambioTxt.setWidth("100%");
        tasaCambioTxt.setValue(1.00);

        checkbox1 = new CheckBox("Hacer retencion IRS");
        checkbox1.addStyleName(ValoTheme.CHECKBOX_LARGE);
        checkbox1.setValue(false);

        leftVerticalLayout.addComponent(centroCostoCbx);
        leftVerticalLayout.addComponent(ordenCompraCbx);
        leftVerticalLayout.addComponent(tipoDocumentoCbx);
        leftVerticalLayout.addComponent(fechaDt);
        leftVerticalLayout.addComponent(serieTxt);
        leftVerticalLayout.addComponent(numeroTxt);
        leftVerticalLayout.addComponent(proveedorCbx);
        leftVerticalLayout.addComponent(nitProveedorTxt);
        leftVerticalLayout.addComponent(montoTxt);
        leftVerticalLayout.addComponent(monedaCbx);
        leftVerticalLayout.addComponent(tasaCambioTxt);
        leftVerticalLayout.addComponent(checkbox1);

        HorizontalLayout layoutHorizontal = new HorizontalLayout();
        layoutHorizontal.setResponsive(true);
        layoutHorizontal.setSpacing(true);

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

        cuentaContableCbx = new ComboBox("Cuenta contable DEBE : ");
        cuentaContableCbx.setWidth("23em");
        cuentaContableCbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContableCbx.setInvalidAllowed(false);
        cuentaContableCbx.setNewItemsAllowed(false);

        cuentaContable1Cbx = new ComboBox();
        cuentaContable1Cbx.setWidth("23em");
        cuentaContable1Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable1Cbx.setInvalidAllowed(false);
        cuentaContable1Cbx.setNewItemsAllowed(false);

        cuentaContable2Cbx = new ComboBox();
        cuentaContable2Cbx.setWidth("23em");
        cuentaContable2Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable2Cbx.setInvalidAllowed(false);
        cuentaContable2Cbx.setNewItemsAllowed(false);

        cuentaContable3Cbx = new ComboBox();
        cuentaContable3Cbx.setWidth("23em");
        cuentaContable3Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable3Cbx.setInvalidAllowed(false);
        cuentaContable3Cbx.setNewItemsAllowed(false);

        cuentaContable4Cbx = new ComboBox();
        cuentaContable4Cbx.setWidth("23em");
        cuentaContable4Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable4Cbx.setInvalidAllowed(false);
        cuentaContable4Cbx.setNewItemsAllowed(false);

        cuentaContable5Cbx = new ComboBox();
        cuentaContable5Cbx.setWidth("23em");
        cuentaContable5Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable5Cbx.setInvalidAllowed(false);
        cuentaContable5Cbx.setNewItemsAllowed(false);

        cuentaContable6Cbx = new ComboBox();
        cuentaContable6Cbx.setWidth("23em");
        cuentaContable6Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable6Cbx.setInvalidAllowed(false);
        cuentaContable6Cbx.setNewItemsAllowed(false);

        cuentaContable7Cbx = new ComboBox();
        cuentaContable7Cbx.setWidth("23em");
        cuentaContable7Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable7Cbx.setInvalidAllowed(false);
        cuentaContable7Cbx.setNewItemsAllowed(false);

        cuentaContable8Cbx = new ComboBox();
        cuentaContable8Cbx.setWidth("23em");
        cuentaContable8Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable8Cbx.setInvalidAllowed(false);
        cuentaContable8Cbx.setNewItemsAllowed(false);

        cuentaContable9Cbx = new ComboBox();
        cuentaContable9Cbx.setWidth("23em");
        cuentaContable9Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable9Cbx.setInvalidAllowed(false);
        cuentaContable9Cbx.setNewItemsAllowed(false);

        cuentaContable10Cbx = new ComboBox();
        cuentaContable10Cbx.setWidth("23em");
        cuentaContable10Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable10Cbx.setInvalidAllowed(false);
        cuentaContable10Cbx.setNewItemsAllowed(false);

        llenarComboCuentaContable();
        llenarComboProveedor();

        haberTxt = new NumberField("HABER : ");
        haberTxt.setDecimalAllowed(true);
        haberTxt.setDecimalPrecision(2);
        haberTxt.setMinimumFractionDigits(2);
        haberTxt.setDecimalSeparator('.');
        haberTxt.setDecimalSeparatorAlwaysShown(true);
        haberTxt.setValue(0d);
        haberTxt.setGroupingUsed(true);
        haberTxt.setGroupingSeparator(',');
        haberTxt.setGroupingSize(3);
        haberTxt.setImmediate(true);
        haberTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        haberTxt.setWidth("7em");
        haberTxt.setValue(0.00);

        haber1Txt = new NumberField();
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

        debeTxt = new NumberField("DEBE :");
        debeTxt.setDecimalAllowed(true);
        debeTxt.setDecimalPrecision(2);
        debeTxt.setMinimumFractionDigits(2);
        debeTxt.setDecimalSeparator('.');
        debeTxt.setDecimalSeparatorAlwaysShown(true);
        debeTxt.setValue(0d);
        debeTxt.setGroupingUsed(true);
        debeTxt.setGroupingSeparator(',');
        debeTxt.setGroupingSize(3);
        debeTxt.setImmediate(true);
        debeTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        debeTxt.setWidth("7em");
        debeTxt.setValue(0.00);

        debe1Txt = new NumberField();
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
        debe10Txt.addValueChangeListener(event -> {
//            generarTotalDebe();
        });

        centroCostoTxt = new NumberField("Centro costo");
        centroCosto1Txt = new NumberField();
        centroCosto2Txt = new NumberField();
        centroCosto3Txt = new NumberField();
        centroCosto4Txt = new NumberField();
        centroCosto5Txt = new NumberField();
        centroCosto6Txt = new NumberField();
        centroCosto7Txt = new NumberField();
        centroCosto8Txt = new NumberField();
        centroCosto9Txt = new NumberField();
        centroCosto10Txt = new NumberField();
//        centroCosto1Txt.addValidator(Validator.createStringLengthValidator("El centro de costo debe tener 6 caracteres", 6, 6, false));

        grabarBtn = new Button("Grabar");
        grabarBtn.setIcon(FontAwesome.SAVE);
        grabarBtn.addClickListener((Button.ClickListener) event -> insertTablaFactura());

        cerrarFacturasBtn = new Button("Cerrar factura");
        cerrarFacturasBtn.setIcon(FontAwesome.CHECK);
        cerrarFacturasBtn.addClickListener((Button.ClickListener) event -> {
            //comprobarBalance();
        });

        layoutHorizontal.addComponent(cuentaContableCbx);
        layoutHorizontal.addComponent(debeTxt);
        layoutHorizontal.addComponent(haberTxt);
        layoutHorizontal.addComponent(centroCostoTxt);

        layoutHorizontal1.addComponent(cuentaContable1Cbx);
        layoutHorizontal1.addComponent(debe1Txt);
        layoutHorizontal1.addComponent(haber1Txt);
        layoutHorizontal1.addComponent(centroCosto1Txt);

        layoutHorizontal2.addComponent(cuentaContable2Cbx);
        layoutHorizontal2.addComponent(debe2Txt);
        layoutHorizontal2.addComponent(haber2Txt);
        layoutHorizontal2.addComponent(centroCosto2Txt);

        layoutHorizontal3.addComponent(cuentaContable3Cbx);
        layoutHorizontal3.addComponent(debe3Txt);
        layoutHorizontal3.addComponent(haber3Txt);
        layoutHorizontal3.addComponent(centroCosto3Txt);

        layoutHorizontal4.addComponent(cuentaContable4Cbx);
        layoutHorizontal4.addComponent(debe4Txt);
        layoutHorizontal4.addComponent(haber4Txt);
        layoutHorizontal4.addComponent(centroCosto4Txt);

        layoutHorizontal5.addComponent(cuentaContable5Cbx);
        layoutHorizontal5.addComponent(debe5Txt);
        layoutHorizontal5.addComponent(haber5Txt);
        layoutHorizontal5.addComponent(centroCosto5Txt);

        layoutHorizontal6.addComponent(cuentaContable6Cbx);
        layoutHorizontal6.addComponent(debe6Txt);
        layoutHorizontal6.addComponent(haber6Txt);
        layoutHorizontal6.addComponent(centroCosto6Txt);

        layoutHorizontal7.addComponent(cuentaContable7Cbx);
        layoutHorizontal7.addComponent(debe7Txt);
        layoutHorizontal7.addComponent(haber7Txt);
        layoutHorizontal7.addComponent(centroCosto7Txt);

        layoutHorizontal8.addComponent(cuentaContable8Cbx);
        layoutHorizontal8.addComponent(debe8Txt);
        layoutHorizontal8.addComponent(haber8Txt);
        layoutHorizontal8.addComponent(centroCosto8Txt);

        layoutHorizontal9.addComponent(cuentaContable9Cbx);
        layoutHorizontal9.addComponent(debe9Txt);
        layoutHorizontal9.addComponent(haber9Txt);
        layoutHorizontal9.addComponent(centroCosto9Txt);

        layoutHorizontal10.addComponent(cuentaContable10Cbx);
        layoutHorizontal10.addComponent(debe10Txt);
        layoutHorizontal10.addComponent(haber10Txt);
        layoutHorizontal10.addComponent(centroCosto10Txt);

        layoutHorizontal11.addComponent(grabarBtn);
        layoutHorizontal11.setComponentAlignment(grabarBtn, Alignment.BOTTOM_CENTER);

        rightVerticalLayout.addComponent(layoutHorizontal);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal, Alignment.MIDDLE_CENTER);

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

    public void llenarComboCentroCosto() {

        queryString = " SELECT * from centro_costo";
        queryString += " WHERE IdProyecto = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrProjectId();
        queryString += " AND IdEmpresa = " + empresaId;
        queryString += " AND Inhabilitado = 0";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                do {
                    centroCostoCbx.addItem(rsRecords.getString("IdCentroCosto"));
//                    centroCostoCbx.setItemCaption(rsRecords.getString("IdCentroCosto"), rsRecords.getString("CodigoCentroCosto") + " " + rsRecords.getString("Grupo"));
                    centroCostoCbx.setItemCaption(rsRecords.getString("IdCentroCosto"), rsRecords.getString("CodigoCentroCosto"));
                } while (rsRecords.next());
            }

        } catch (Exception ex1) {
            System.out.println("Error al llenar combo centro costo: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void llenarCampos() {

        estatusPartidaEdit = "INGRESADO";

        queryString = " SELECT * from contabilidad_partida";
        queryString += " where CodigoPartida = '" + codigoPartidaEdit + "'";

        try {
            int contador = 0;
            double montoFactura = 0.00;

            stQuery2 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords2 = stQuery2.executeQuery(queryString);

            while (rsRecords2.next()) { //  encontrado

                if (contador == 0) {
                    estatusPartidaEdit = rsRecords2.getString("Estatus");
                    ordenCompraCbx.select(rsRecords2.getString("IdOrdenCompra"));
                    tipoDocumentoCbx.select(rsRecords2.getString("TipoDocumento"));
                    serieTxt.setValue(rsRecords2.getString("SerieDocumento"));
                    numeroTxt.setValue(rsRecords2.getString("NumeroDocumento"));
                    nitProveedorTxt.setValue(rsRecords2.getString("NITProveedor"));
                    proveedorCbx.select(rsRecords2.getString("IdProveedor"));
                    fechaDt.setValue(rsRecords2.getDate("Fecha"));
                    monedaCbx.setValue(rsRecords2.getString("MonedaDocumento"));
                    tasaCambioTxt.setValue(rsRecords2.getDouble("TipoCambio"));
                    cuentaContableCbx.setReadOnly(false);
                    cuentaContableCbx.select(rsRecords2.getString("IdNomenclatura"));
                    cuentaContableCbx.setReadOnly(true);
                    haberTxt.setValue(rsRecords2.getDouble("Haber"));
                    debeTxt.setReadOnly(true);
                    centroCostoCbx.setValue(rsRecords2.getString("IdCentroCosto"));

                    if (rsRecords2.getString("Referencia") == null) {
                        checkbox1.setValue(false);
                    } else if (rsRecords2.getString("Referencia").equals("NO")) {
                        checkbox1.setValue(false);
                    } else if (rsRecords2.getString("Referencia").equals("SI")) {
                        checkbox1.setValue(true);
                    }

                }
                if (contador == 1) {
                    cuentaContable1Cbx.select(rsRecords2.getString("IdNomenclatura"));
                    debe1Txt.setValue(rsRecords2.getDouble("Debe"));
                    haber1Txt.setValue(rsRecords2.getDouble("Haber"));
                }
                if (contador == 2) {
                    cuentaContable2Cbx.select(rsRecords2.getString("IdNomenclatura"));
                    debe2Txt.setValue(rsRecords2.getString("Debe"));
                    haber2Txt.setValue(rsRecords2.getDouble("Haber"));
                }
                if (contador == 3) {
                    cuentaContable3Cbx.select(rsRecords2.getString("IdNomenclatura"));
                    debe3Txt.setValue(rsRecords2.getString("Debe"));
                    haber3Txt.setValue(rsRecords2.getDouble("Haber"));
                }
                if (contador == 4) {
                    cuentaContable4Cbx.select(rsRecords2.getString("IdNomenclatura"));
                    debe4Txt.setValue(rsRecords2.getString("Debe"));
                    haber4Txt.setValue(rsRecords2.getDouble("Haber"));
                }
                if (contador == 5) {
                    cuentaContable5Cbx.select(rsRecords2.getString("IdNomenclatura"));
                    debe5Txt.setValue(rsRecords2.getString("Debe"));
                    haber5Txt.setValue(rsRecords2.getDouble("Haber"));
                }
                if (contador == 6) {
                    cuentaContable6Cbx.select(rsRecords2.getString("IdNomenclatura"));
                    debe6Txt.setValue(rsRecords2.getString("Debe"));
                    haber6Txt.setValue(rsRecords2.getDouble("Haber"));
                }

                if (contador == 7) {
                    cuentaContable7Cbx.select(rsRecords2.getString("IdNomenclatura"));
                    debe7Txt.setValue(rsRecords2.getString("Debe"));
                    haber7Txt.setValue(rsRecords2.getDouble("Haber"));
                }

                if (contador == 8) {
                    cuentaContable8Cbx.select(rsRecords2.getString("IdNomenclatura"));
                    debe8Txt.setValue(rsRecords2.getString("Debe"));
                    haber8Txt.setValue(rsRecords2.getDouble("Haber"));
                }

                if (contador == 9) {
                    cuentaContable9Cbx.select(rsRecords2.getString("IdNomenclatura"));
                    debe9Txt.setValue(rsRecords2.getString("Debe"));
                    haber9Txt.setValue(rsRecords2.getDouble("Haber"));
                }

                if (contador == 10) {
                    cuentaContable10Cbx.select(rsRecords2.getString("IdNomenclatura"));
                    debe10Txt.setValue(rsRecords2.getString("Debe"));
                    haber10Txt.setValue(rsRecords2.getDouble("Haber"));
                }

                contador = contador + 1;
                montoFactura += rsRecords2.getDouble("Haber");

            }
            montoTxt.setValue(montoFactura);
            serieTxt.setReadOnly(false);
            numeroTxt.setReadOnly(false);
            nitProveedorTxt.setReadOnly(false);
            proveedorCbx.setReadOnly(false);
            fechaDt.setReadOnly(false);
            montoTxt.setReadOnly(false);
            monedaCbx.setReadOnly(false);
            tasaCambioTxt.setReadOnly(false);
            cuentaContableCbx.setReadOnly(true);
            centroCostoCbx.setReadOnly(false);

        } catch (Exception ex1) {
            System.out.println("Error al listar cuentas contables: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void buscarArchivo() {
        try {

            queryString = "Select * ";
            queryString += " From contabilidad_partida";
            queryString += " Where CodigoPartida = " + codigoPartidaEdit;

            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (!rsRecords.next()) { //  no encontrado
                Notification.show("Documento scan no disponible para visualizar!", Notification.Type.ERROR_MESSAGE);
                return;
            }
            if (rsRecords.getObject("Archivo") == null) {
                Notification.show("Documento scan no disponible para visualizar!");

                return;
            }

            Window window = new Window();
            window.setResizable(true);
            window.setWidth("95%");
            window.setHeight("95%");

            final byte docBytes[] = rsRecords.getBytes("Archivo");
            final String fileName = rsRecords.getString("ArchivoNombre");
            documentStreamResource = null;

            if (docBytes != null) {
                documentStreamResource = new StreamResource(
                        new StreamResource.StreamSource() {
                    public InputStream getStream() {
                        return new ByteArrayInputStream(docBytes);
                    }
                }, fileName
                );
            }
            documentStreamResource.setMIMEType(rsRecords.getString("ArchivoTipo"));
            documentStreamResource.setFilename(rsRecords.getString("ArchivoNombre"));
            documentStreamResource.getStream().setParameter("Content-Disposition", "attachment; filename=" + fileName);

            System.out.println("Tipo de Archivo : " + rsRecords.getString("ArchivoTipo"));
            System.out.println("Archivo Nombre :" + rsRecords.getString("ArchivoNombre"));
            System.out.println("Tamanio :" + rsRecords.getString("ArchivoPeso"));

            tipoArchivo = rsRecords.getString("ArchivoTipo");
            nombreArchvivo = rsRecords.getString("ArchivoNombre");
            pesoArchivo = rsRecords.getLong("ArchivoPeso");

        } catch (Exception ex) {
            System.out.println("Error al intentar buscar el archivo de la partida " + ex);
            ex.printStackTrace();
        }
    }

    public void verificarProveedorInsititucion(String id) {
        if (id == null) {
            return;
        }

        cuentaContableCbx.setReadOnly(false);

        queryString = " SELECT * from proveedor ";
        queryString += " WHERE Inhabilitado = 0";
        queryString += " AND IDProveedor = " + id;

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado
                if (rsRecords.getString("Grupo").equals("Instituciones")) {
                    cuentaContableCbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getInstituciones());
                } else {
                    cuentaContableCbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getProveedores());
                }
            }
            cuentaContableCbx.setReadOnly(true);

        } catch (Exception ex1) {
            System.out.println("Error al listar Proveedores " + ex1.getMessage());
            ex1.printStackTrace();
        }

    }

    public void llenarComboCuentaContable() {

        queryString = " SELECT * from contabilidad_nomenclatura";
        queryString += " where FiltrarIngresoDocumentos = 'S'";
        queryString += " and Estatus = 'HABILITADA'";
        queryString += " and IdNomenclatura <> " + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getIvaPorPagar(); // solicitado por Shwaony 1/9/2021
        queryString += " Order By N5";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) { //  encontrado

                cuentaContableCbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContableCbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

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
            System.out.println("Error al listar cuentas contables: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void insertTablaFactura() {

        try {
            Utileria utileria = new Utileria();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
 
            Date fechaInicial = dateFormat.parse(utileria.getFecha());
            Date fechaFinal = dateFormat.parse(utileria.getFechaYYYYMMDD(fechaDt.getValue()));

            int dias=(int) ((fechaInicial.getTime()-fechaFinal.getTime())/86400000);
             
            System.out.println("Hay "+dias+" dias de diferencia");
            
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

        totalDebe = new BigDecimal(debeTxt.getDoubleValueDoNotThrow()
                + debe1Txt.getDoubleValueDoNotThrow() + debe2Txt.getDoubleValueDoNotThrow()
                + debe3Txt.getDoubleValueDoNotThrow() + debe4Txt.getDoubleValueDoNotThrow()
                + debe5Txt.getDoubleValueDoNotThrow() + debe6Txt.getDoubleValueDoNotThrow()
                + debe7Txt.getDoubleValueDoNotThrow() + debe8Txt.getDoubleValueDoNotThrow()
                + debe9Txt.getDoubleValueDoNotThrow() + debe10Txt.getDoubleValueDoNotThrow()).setScale(2, BigDecimal.ROUND_HALF_UP);

        /////pregunta en la suma del total haber por que se suma habertxt, luego haber1 y asi sucedibamente
        totalHaber = new BigDecimal(haberTxt.getDoubleValueDoNotThrow()
                + haber1Txt.getDoubleValueDoNotThrow() + haber2Txt.getDoubleValueDoNotThrow()
                + haber3Txt.getDoubleValueDoNotThrow() + haber4Txt.getDoubleValueDoNotThrow()
                + haber5Txt.getDoubleValueDoNotThrow() + haber6Txt.getDoubleValueDoNotThrow()
                + haber7Txt.getDoubleValueDoNotThrow() + haber8Txt.getDoubleValueDoNotThrow()
                + haber9Txt.getDoubleValueDoNotThrow() + haber10Txt.getDoubleValueDoNotThrow()).setScale(2, BigDecimal.ROUND_HALF_UP);

        totalDebe.setScale(2, BigDecimal.ROUND_HALF_UP);
        totalHaber.setScale(2, BigDecimal.ROUND_HALF_UP);

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

        //        if (totalDebe.round(MathContext.DECIMAL32).doubleValue() != montoTxt.getDoubleValueDoNotThrow()) {
        if (totalDebe.doubleValue() != montoTxt.getDoubleValueDoNotThrow()) {
            Notification.show("Partida está descuadrada DEBE, por favor revisar.", Notification.Type.ERROR_MESSAGE);
            System.out.println("monto=" + montoTxt.getDoubleValueDoNotThrow() + " RAW debe=" + totalDebe);
            return;
        }
        //        if (totalHaber.round(MathContext.DECIMAL32).doubleValue() != montoTxt.getDoubleValueDoNotThrow()) {
        if (totalHaber.doubleValue() != montoTxt.getDoubleValueDoNotThrow()) {
            Notification.show("Partida está descuadrada HABER, por favor revisar.", Notification.Type.ERROR_MESSAGE);
            System.out.println("monto=" + montoTxt.getDoubleValueDoNotThrow() + " RAW haber=" + totalHaber);
            return;
        }

        if (cuentaContable1Cbx.getValue() == null && debe1Txt.getDoubleValueDoNotThrow() > 0) {
            Notification.show("Partida está descuadrada (cuenta 1), por favor revisar.", Notification.Type.ERROR_MESSAGE);
            return;
        }
        if (cuentaContable2Cbx.getValue() == null && debe2Txt.getDoubleValueDoNotThrow() > 0) {
            Notification.show("Partida está descuadrada (cuenta 2), por favor revisar.", Notification.Type.ERROR_MESSAGE);
            return;
        }
        if (cuentaContable3Cbx.getValue() == null && debe3Txt.getDoubleValueDoNotThrow() > 0) {
            Notification.show("Partida está descuadrada (cuenta 3), por favor revisar.", Notification.Type.ERROR_MESSAGE);
            return;
        }
        if (cuentaContable4Cbx.getValue() == null && debe4Txt.getDoubleValueDoNotThrow() > 0) {
            Notification.show("Partida está descuadrada (cuenta 4), por favor revisar.", Notification.Type.ERROR_MESSAGE);
            return;
        }
        if (cuentaContable4Cbx.getValue() == null && debe4Txt.getDoubleValueDoNotThrow() > 0) {
            Notification.show("Partida está descuadrada (cuenta 4), por favor revisar.", Notification.Type.ERROR_MESSAGE);
            return;
        }
        if (cuentaContable5Cbx.getValue() == null && debe5Txt.getDoubleValueDoNotThrow() > 0) {
            Notification.show("Partida está descuadrada (cuenta 5), por favor revisar.", Notification.Type.ERROR_MESSAGE);
            return;
        }
        if (cuentaContable6Cbx.getValue() == null && debe6Txt.getDoubleValueDoNotThrow() > 0) {
            Notification.show("Partida está descuadrada (cuenta 6), por favor revisar.", Notification.Type.ERROR_MESSAGE);
            return;
        }

        if (cuentaContable1Cbx.getValue() == null && haber1Txt.getDoubleValueDoNotThrow() > 0) {
            Notification.show("Partida está descuadrada (cuenta 1), por favor revisar.", Notification.Type.ERROR_MESSAGE);
            return;
        }
        if (cuentaContable2Cbx.getValue() == null && haber2Txt.getDoubleValueDoNotThrow() > 0) {
            Notification.show("Partida está descuadrada (cuenta 2), por favor revisar.", Notification.Type.ERROR_MESSAGE);
            return;
        }
        if (cuentaContable3Cbx.getValue() == null && haber3Txt.getDoubleValueDoNotThrow() > 0) {
            Notification.show("Partida está descuadrada (cuenta 3), por favor revisar.", Notification.Type.ERROR_MESSAGE);
            return;
        }
        if (cuentaContable4Cbx.getValue() == null && haber4Txt.getDoubleValueDoNotThrow() > 0) {
            Notification.show("Partida está descuadrada (cuenta 4), por favor revisar.", Notification.Type.ERROR_MESSAGE);
            return;
        }
        if (cuentaContable5Cbx.getValue() == null && haber5Txt.getDoubleValueDoNotThrow() > 0) {
            Notification.show("Partida está descuadrada (cuenta 5), por favor revisar.", Notification.Type.ERROR_MESSAGE);
            return;
        }
        if (cuentaContable6Cbx.getValue() == null && haber6Txt.getDoubleValueDoNotThrow() > 0) {
            Notification.show("Partida está descuadrada (cuenta 6), por favor revisar.", Notification.Type.ERROR_MESSAGE);
            return;
        }
        if (cuentaContable7Cbx.getValue() == null && haber7Txt.getDoubleValueDoNotThrow() > 0) {
            Notification.show("Partida está descuadrada (cuenta 7), por favor revisar.", Notification.Type.ERROR_MESSAGE);
            return;
        }
        if (cuentaContable8Cbx.getValue() == null && haber8Txt.getDoubleValueDoNotThrow() > 0) {
            Notification.show("Partida está descuadrada (cuenta 8), por favor revisar.", Notification.Type.ERROR_MESSAGE);
            return;
        }
        if (cuentaContable9Cbx.getValue() == null && haber9Txt.getDoubleValueDoNotThrow() > 0) {
            Notification.show("Partida está descuadrada (cuenta 9), por favor revisar.", Notification.Type.ERROR_MESSAGE);
            return;
        }
        if (cuentaContable10Cbx.getValue() == null && haber10Txt.getDoubleValueDoNotThrow() > 0) {
            Notification.show("Partida está descuadrada (cuenta 10), por favor revisar.", Notification.Type.ERROR_MESSAGE);
            return;
        }

        queryString = " DELETE from contabilidad_partida ";
        queryString += " where CodigoPartida  ='" + codigoPartidaEdit + "'";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

        } catch (SQLException ex) {
            System.out.println("Error al intentar eliminar registros " + ex);
            ex.printStackTrace();
            return;
        }

        String fecha = Utileria.getFechaYYYYMMDD_1(fechaDt.getValue());
        String ultimoEncontado;
        String dia = fecha.substring(8, 10);
        String mes = fecha.substring(5, 7);
        String año = fecha.substring(0, 4);

        nuevoCodigoPartida = empresaId + año + mes + dia + "1";

        queryString = " select codigoPartida from contabilidad_partida ";
        queryString += " where codigoPartida like '" + nuevoCodigoPartida + "%'";
        queryString += " order by codigoPartida desc ";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado

                ultimoEncontado = rsRecords.getString("codigoPartida").substring(12, 15);

                nuevoCodigoPartida += String.format("%03d", (Integer.valueOf(ultimoEncontado) + 1));

            } else {
                nuevoCodigoPartida += "001";
            }

        } catch (Exception ex1) {
            System.out.println("Error al buscar el ultimo codigoPartida" + ex1.getMessage());
            ex1.printStackTrace();
        }

        queryString = " Select * from contabilidad_partida";
        queryString += " Where SerieDocumento  = '" + serieTxt.getValue().toUpperCase().trim() + "'";
        queryString += " And   NumeroDocumento = '" + numeroTxt.getValue().toUpperCase().trim() + "'";
        queryString += " And   NombreProveedor = '" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";

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

        queryString = " Insert Into contabilidad_partida (IdEmpresa, Estatus, CodigoPartida, CodigoCC, ";
        queryString += " TipoDocumento, Fecha, IdProveedor, NITProveedor, NombreProveedor, Referencia,";
        queryString += " SerieDocumento, NumeroDocumento, IdNomenclatura, MonedaDocumento, MontoDocumento,";
        queryString += " Debe, Haber, DebeQuetzales, HaberQuetzales, TipoCambio, Saldo, ";
        queryString += " IdLiquidador, Descripcion, CreadoUsuario, CreadoFechaYHora, ";
        queryString += " Archivo, ArchivoTipo, ArchivoPeso, ArchivoNombre, IdOrdenCompra,IdCentroCosto, CodigoCentroCosto)";
        queryString += " Values ";

        //// ingreso del haber
        if (cuentaContableCbx.getValue() != null && haberTxt.getDoubleValueDoNotThrow() > 0) {
            queryString += "(";
            queryString += empresaId;
            queryString += ",'" + estatusPartidaEdit + "'";
            queryString += ",'" + nuevoCodigoPartida + "'";
            queryString += ",'" + nuevoCodigoPartida + "'"; //codigoCC
            queryString += ",'" + tipoDocumentoCbx.getValue() + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + nitProveedorTxt.getValue() + "'";
            queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            if (checkbox1.getValue().equals(true)) {
                queryString += ",'SI'";
            } else {
                queryString += ",'NO'";
            }
            queryString += ",UPPER('" + serieTxt.getValue().trim() + "')";
            queryString += ",'" + numeroTxt.getValue().trim() + "'";
            queryString += "," + cuentaContableCbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow()); //MONTO DOCUMENTO
            queryString += ",0.00"; //DEBE
            queryString += "," + String.valueOf(haberTxt.getDoubleValueDoNotThrow()); // Haber
            queryString += ",0.00"; //DEBEQ.
            queryString += "," + String.valueOf(haberTxt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());

//            queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow());
            if (String.valueOf(cuentaContableCbx.getValue()).equals("310")) {
                queryString += ",0.00"; //SALDO ABASTOS
            } else {
                queryString += "," + String.valueOf(haberTxt.getDoubleValueDoNotThrow()); // Haber (SALDO)
            }
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + tipoDocumentoCbx.getValue() + " " + serieTxt.getValue().trim() + " " + numeroTxt.getValue() + " " + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            queryString += "," + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            if (nombreArchvivo == null && tipoArchivo == null) {
                queryString += ",null";
                queryString += ",'0'";
                queryString += ",0";
                queryString += ",'0'";
            } else {
                queryString += ",?";
                queryString += ",'" + tipoArchivo + "'";
                queryString += "," + pesoArchivo;
                queryString += ",'" + nombreArchvivo + "'";

            }
            queryString += "," + ordenCompraCbx.getValue();
            queryString += "," + centroCostoCbx.getValue();
            queryString += ",'" + centroCostoCbx.getItemCaption(centroCostoCbx.getValue()) + "'";
            queryString += "),";
        }

//// primer ingreso
        if ((cuentaContable1Cbx.getValue() != null && debe1Txt.getDoubleValueDoNotThrow() > 0)
                || (cuentaContable1Cbx.getValue() != null && haber1Txt.getDoubleValueDoNotThrow() > 0)) {

            queryString += "(";
            queryString += empresaId;
            queryString += ",'" + estatusPartidaEdit + "'";
            queryString += ",'" + nuevoCodigoPartida + "'";
            queryString += ",'" + nuevoCodigoPartida + "'";//codigoCC
            queryString += ",'" + tipoDocumentoCbx.getValue() + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + nitProveedorTxt.getValue() + "'";
            queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            if (checkbox1.getValue().equals(true)) {
                queryString += ",'SI'";
            } else {
                queryString += ",'NO'";
            }
            queryString += ",UPPER('" + serieTxt.getValue().trim() + "')";
            queryString += ",'" + numeroTxt.getValue().trim() + "'";
            queryString += "," + cuentaContable1Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow()); //MONTO DOCUMENTO
            if (debe1Txt.getDoubleValueDoNotThrow() > 0 && haber1Txt.getDoubleValueDoNotThrow() == 0.00) {
                queryString += "," + String.valueOf(debe1Txt.getDoubleValueDoNotThrow()); // DEBE
                queryString += ",0.00"; //HABER
                queryString += "," + String.valueOf(debe1Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
                queryString += ",0.00"; //HABER Q.
            } else {
                if (haber1Txt.getDoubleValueDoNotThrow() > 0 && debe1Txt.getDoubleValueDoNotThrow() == 0.00) {
                    queryString += ",0.00"; //DEBE
                    queryString += "," + String.valueOf(haber1Txt.getDoubleValueDoNotThrow()); // Haber
                    queryString += ",0.00"; //DEBEQ.
                    queryString += "," + String.valueOf(haber1Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
                }
            }
            queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + tipoDocumentoCbx.getValue() + " " + serieTxt.getValue().trim() + " " + numeroTxt.getValue() + " " + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            queryString += "," + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ",null";
            queryString += ",'0'";
            queryString += ",0";
            queryString += ",'0'";
            queryString += "," + ordenCompraCbx.getValue();
            queryString += ",0";
            queryString += ",'" + centroCosto1Txt.getValue() + "'";
            queryString += ")";
        }

//// segundo  ingreso
        if (cuentaContable2Cbx.getValue() != null && debe2Txt.getDoubleValueDoNotThrow() > 0
                || (cuentaContable2Cbx.getValue() != null && haber2Txt.getDoubleValueDoNotThrow() > 0)) {

            queryString += ",(";
            queryString += empresaId;
            queryString += ",'" + estatusPartidaEdit + "'";
            queryString += ",'" + nuevoCodigoPartida + "'";
            queryString += ",'" + nuevoCodigoPartida + "'";//codigoCC
            queryString += ",'" + tipoDocumentoCbx.getValue() + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + nitProveedorTxt.getValue() + "'";
            queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            if (checkbox1.getValue().equals(true)) {
                queryString += ",'SI'";
            } else {
                queryString += ",'NO'";
            }
            queryString += ",UPPER('" + serieTxt.getValue().trim() + "')";
            queryString += ",'" + numeroTxt.getValue().trim() + "'";
            queryString += "," + cuentaContable2Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow()); //MONTO DOCUMENTO
            if (debe2Txt.getDoubleValueDoNotThrow() > 0 && haber2Txt.getDoubleValueDoNotThrow() == 0.00) {
                queryString += "," + String.valueOf(debe2Txt.getDoubleValueDoNotThrow()); // DEBE
                queryString += ",0.00"; //HABER
                queryString += "," + String.valueOf(debe2Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
                queryString += ",0.00"; //HABER Q.
            } else {
                if (haber2Txt.getDoubleValueDoNotThrow() > 0 && debe2Txt.getDoubleValueDoNotThrow() == 0.00) {
                    queryString += ",0.00"; //DEBE
                    queryString += "," + String.valueOf(haber2Txt.getDoubleValueDoNotThrow()); // Haber
                    queryString += ",0.00"; //DEBEQ.
                    queryString += "," + String.valueOf(haber2Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
                }
            }
            queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + tipoDocumentoCbx.getValue() + " " + serieTxt.getValue().trim() + " " + numeroTxt.getValue() + " " + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            queryString += "," + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ",null";
            queryString += ",'0'";
            queryString += ",0";
            queryString += ",'0'";
            queryString += "," + ordenCompraCbx.getValue();
            queryString += ",0";
            queryString += ",'" + centroCosto2Txt.getValue() + "'";
            queryString += ")";
        }

//// tercer ingreso
        if (cuentaContable3Cbx.getValue() != null && debe3Txt.getDoubleValueDoNotThrow() > 0
                || (cuentaContable3Cbx.getValue() != null && haber3Txt.getDoubleValueDoNotThrow() > 0)) {

            queryString += ",(";
            queryString += empresaId;
            queryString += ",'" + estatusPartidaEdit + "'";
            queryString += ",'" + nuevoCodigoPartida + "'";
            queryString += ",'" + nuevoCodigoPartida + "'";//codigoCC
            queryString += ",'" + tipoDocumentoCbx.getValue() + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + nitProveedorTxt.getValue() + "'";
            queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            if (checkbox1.getValue().equals(true)) {
                queryString += ",'SI'";
            } else {
                queryString += ",'NO'";
            }
            queryString += ",UPPER('" + serieTxt.getValue().trim() + "')";
            queryString += ",'" + numeroTxt.getValue().trim() + "'";
            queryString += "," + cuentaContable3Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow()); //MONTO DOCUMENTO
            if (debe3Txt.getDoubleValueDoNotThrow() > 0 && haber3Txt.getDoubleValueDoNotThrow() == 0.00) {
                queryString += "," + String.valueOf(debe3Txt.getDoubleValueDoNotThrow()); // DEBE
                queryString += ",0.00"; //HABER
                queryString += "," + String.valueOf(debe3Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
                queryString += ",0.00"; //HABER Q.
            } else {
                if (haber3Txt.getDoubleValueDoNotThrow() > 0 && debe3Txt.getDoubleValueDoNotThrow() == 0.00) {
                    queryString += ",0.00"; //DEBE
                    queryString += "," + String.valueOf(haber3Txt.getDoubleValueDoNotThrow()); // Haber
                    queryString += ",0.00"; //DEBEQ.
                    queryString += "," + String.valueOf(haber3Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
                }
            }
            queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + tipoDocumentoCbx.getValue() + " " + serieTxt.getValue().trim() + " " + numeroTxt.getValue() + " " + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            queryString += "," + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ",null";
            queryString += ",'0'";
            queryString += ",0";
            queryString += ",'0'";
            queryString += "," + ordenCompraCbx.getValue();
            queryString += ",0";
            queryString += ",'" + centroCosto3Txt.getValue() + "'";
            queryString += ")";
        }

//// cuarto ingreso
        if (cuentaContable4Cbx.getValue() != null && debe4Txt.getDoubleValueDoNotThrow() != 0
                || (cuentaContable4Cbx.getValue() != null && haber4Txt.getDoubleValueDoNotThrow() > 0)) {

            queryString += ",(";
            queryString += empresaId;
            queryString += ",'" + estatusPartidaEdit + "'";
            queryString += ",'" + nuevoCodigoPartida + "'";
            queryString += ",'" + nuevoCodigoPartida + "'";//codigoCC
            queryString += ",'" + tipoDocumentoCbx.getValue() + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + nitProveedorTxt.getValue() + "'";
            queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            if (checkbox1.getValue().equals(true)) {
                queryString += ",'SI'";
            } else {
                queryString += ",'NO'";
            }
            queryString += ",UPPER('" + serieTxt.getValue().trim() + "')";
            queryString += ",'" + numeroTxt.getValue().trim() + "'";
            queryString += "," + cuentaContable4Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow()); //MONTO DOCUMENTO
            if (debe4Txt.getDoubleValueDoNotThrow() > 0 && haber4Txt.getDoubleValueDoNotThrow() == 0.00) {
                queryString += "," + String.valueOf(debe4Txt.getDoubleValueDoNotThrow()); // DEBE
                queryString += ",0.00"; //HABER
                queryString += "," + String.valueOf(debe4Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
                queryString += ",0.00"; //HABER Q.
            } else {
                if (haber4Txt.getDoubleValueDoNotThrow() > 0 && debe4Txt.getDoubleValueDoNotThrow() == 0.00) {
                    queryString += ",0.00"; //DEBE
                    queryString += "," + String.valueOf(haber4Txt.getDoubleValueDoNotThrow()); // Haber
                    queryString += ",0.00"; //DEBEQ.
                    queryString += "," + String.valueOf(haber4Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
                }
            }
            queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + tipoDocumentoCbx.getValue() + " " + serieTxt.getValue().trim() + " " + numeroTxt.getValue() + " " + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            queryString += "," + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ",null";
            queryString += ",'0'";
            queryString += ",0";
            queryString += ",'0'";
            queryString += "," + ordenCompraCbx.getValue();
            queryString += ",0";
            queryString += ",'" + centroCosto4Txt.getValue() + "'";
            queryString += ")";
        }
//// quinto ingreso
        if (cuentaContable5Cbx.getValue() != null && debe5Txt.getDoubleValueDoNotThrow() != 0
                || (cuentaContable5Cbx.getValue() != null && haber5Txt.getDoubleValueDoNotThrow() > 0)) {

            queryString += ",(  ";
            queryString += empresaId;
            queryString += ",'" + estatusPartidaEdit + "'";
            queryString += ",'" + nuevoCodigoPartida + "'";
            queryString += ",'" + nuevoCodigoPartida + "'";//codigoCC
            queryString += ",'" + tipoDocumentoCbx.getValue() + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + nitProveedorTxt.getValue() + "'";
            queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            if (checkbox1.getValue().equals(true)) {
                queryString += ",'SI'";
            } else {
                queryString += ",'NO'";
            }
            queryString += ",UPPER('" + serieTxt.getValue().trim() + "')";
            queryString += ",'" + numeroTxt.getValue().trim() + "'";
            queryString += "," + cuentaContable5Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow()); //MONTO DOCUMENTO
            if (debe5Txt.getDoubleValueDoNotThrow() > 0 && haber5Txt.getDoubleValueDoNotThrow() == 0.00) {
                queryString += "," + String.valueOf(debe5Txt.getDoubleValueDoNotThrow()); // DEBE
                queryString += ",0.00"; //HABER
                queryString += "," + String.valueOf(debe5Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
                queryString += ",0.00"; //HABER Q.
            } else {
                if (haber5Txt.getDoubleValueDoNotThrow() > 0 && debe5Txt.getDoubleValueDoNotThrow() == 0.00) {
                    queryString += ",0.00"; //DEBE
                    queryString += "," + String.valueOf(haber5Txt.getDoubleValueDoNotThrow()); // Haber
                    queryString += ",0.00"; //DEBEQ.
                    queryString += "," + String.valueOf(haber5Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
                }
            }
            queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + tipoDocumentoCbx.getValue() + " " + serieTxt.getValue().trim() + " " + numeroTxt.getValue() + " " + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            queryString += "," + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ",null";
            queryString += ",'0'";
            queryString += ",0";
            queryString += ",'0'";
            queryString += "," + ordenCompraCbx.getValue();
            queryString += ",0";
            queryString += ",'" + centroCosto5Txt.getValue() + "'";
            queryString += ")";
        }
//// sexto ingreso
        if (cuentaContable6Cbx.getValue() != null && debe6Txt.getDoubleValueDoNotThrow() != 0
                || (cuentaContable6Cbx.getValue() != null && haber6Txt.getDoubleValueDoNotThrow() > 0)) {

            queryString += ",(";
            queryString += empresaId;
            queryString += ",'" + estatusPartidaEdit + "'";
            queryString += ",'" + nuevoCodigoPartida + "'";
            queryString += ",'" + nuevoCodigoPartida + "'";//codigoCC
            queryString += ",'" + tipoDocumentoCbx.getValue() + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + nitProveedorTxt.getValue() + "'";
            queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            if (checkbox1.getValue().equals(true)) {
                queryString += ",'SI'";
            } else {
                queryString += ",'NO'";
            }
            queryString += ",UPPER('" + serieTxt.getValue().trim() + "')";
            queryString += ",'" + numeroTxt.getValue().trim() + "'";
            queryString += "," + cuentaContable6Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow()); //MONTO DOCUMENTO
            if (debe6Txt.getDoubleValueDoNotThrow() > 0 && haber6Txt.getDoubleValueDoNotThrow() == 0.00) {
                queryString += "," + String.valueOf(debe6Txt.getDoubleValueDoNotThrow()); // DEBE
                queryString += ",0.00"; //HABER
                queryString += "," + String.valueOf(debe6Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
                queryString += ",0.00"; //HABER Q.
            } else {
                if (haber6Txt.getDoubleValueDoNotThrow() > 0 && debe6Txt.getDoubleValueDoNotThrow() == 0.00) {
                    queryString += ",0.00"; //DEBE
                    queryString += "," + String.valueOf(haber6Txt.getDoubleValueDoNotThrow()); // Haber
                    queryString += ",0.00"; //DEBEQ.
                    queryString += "," + String.valueOf(haber6Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
                }
            }
            queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + tipoDocumentoCbx.getValue() + " " + serieTxt.getValue().trim() + " " + numeroTxt.getValue() + " " + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            queryString += "," + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ",null";
            queryString += ",'0'";
            queryString += ",0";
            queryString += ",'0'";
            queryString += "," + ordenCompraCbx.getValue();
            queryString += ",0";
            queryString += ",'" + centroCosto6Txt.getValue() + "'";
            queryString += ")";
        }

//// septimo ingreso
        if (cuentaContable7Cbx.getValue() != null && debe7Txt.getDoubleValueDoNotThrow() != 0
                || (cuentaContable7Cbx.getValue() != null && haber7Txt.getDoubleValueDoNotThrow() > 0)) {

            queryString += ",(";
            queryString += empresaId;
            queryString += ",'" + estatusPartidaEdit + "'";
            queryString += ",'" + nuevoCodigoPartida + "'";
            queryString += ",'" + nuevoCodigoPartida + "'";//codigoCC
            queryString += ",'" + tipoDocumentoCbx.getValue() + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + nitProveedorTxt.getValue() + "'";
            queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            if (checkbox1.getValue().equals(true)) {
                queryString += ",'SI'";
            } else {
                queryString += ",'NO'";
            }
            queryString += ",UPPER('" + serieTxt.getValue().trim() + "')";
            queryString += ",'" + numeroTxt.getValue().trim() + "'";
            queryString += "," + cuentaContable7Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow()); //MONTO DOCUMENTO
            if (debe7Txt.getDoubleValueDoNotThrow() > 0 && haber7Txt.getDoubleValueDoNotThrow() == 0.00) {
                queryString += "," + String.valueOf(debe7Txt.getDoubleValueDoNotThrow()); // DEBE
                queryString += ",0.00"; //HABER
                queryString += "," + String.valueOf(debe7Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
                queryString += ",0.00"; //HABER Q.
            } else {
                if (haber7Txt.getDoubleValueDoNotThrow() > 0 && debe7Txt.getDoubleValueDoNotThrow() == 0.00) {
                    queryString += ",0.00"; //DEBE
                    queryString += "," + String.valueOf(haber7Txt.getDoubleValueDoNotThrow()); // Haber
                    queryString += ",0.00"; //DEBEQ.
                    queryString += "," + String.valueOf(haber7Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
                }
            }
            queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + tipoDocumentoCbx.getValue() + " " + serieTxt.getValue().trim() + " " + numeroTxt.getValue() + " " + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            queryString += "," + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ",null";
            queryString += ",'0'";
            queryString += ",0";
            queryString += ",'0'";
            queryString += "," + ordenCompraCbx.getValue();
            queryString += ",0";
            queryString += ",'" + centroCosto7Txt.getValue() + "'";
            queryString += ")";
        }

//// octavo ingreso
        if (cuentaContable8Cbx.getValue() != null && debe8Txt.getDoubleValueDoNotThrow() != 0
                || (cuentaContable8Cbx.getValue() != null && haber8Txt.getDoubleValueDoNotThrow() > 0)) {

            queryString += ",(";
            queryString += empresaId;
            queryString += ",'" + estatusPartidaEdit + "'";
            queryString += ",'" + nuevoCodigoPartida + "'";
            queryString += ",'" + nuevoCodigoPartida + "'";//codigoCC
            queryString += ",'" + tipoDocumentoCbx.getValue() + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + nitProveedorTxt.getValue() + "'";
            queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            if (checkbox1.getValue().equals(true)) {
                queryString += ",'SI'";
            } else {
                queryString += ",'NO'";
            }
            queryString += ",UPPER('" + serieTxt.getValue().trim() + "')";
            queryString += ",'" + numeroTxt.getValue().trim() + "'";
            queryString += "," + cuentaContable8Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow()); //MONTO DOCUMENTO
            if (debe8Txt.getDoubleValueDoNotThrow() > 0 && haber8Txt.getDoubleValueDoNotThrow() == 0.00) {
                queryString += "," + String.valueOf(debe8Txt.getDoubleValueDoNotThrow()); // DEBE
                queryString += ",0.00"; //HABER
                queryString += "," + String.valueOf(debe8Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
                queryString += ",0.00"; //HABER Q.
            } else {
                if (haber8Txt.getDoubleValueDoNotThrow() > 0 && debe8Txt.getDoubleValueDoNotThrow() == 0.00) {
                    queryString += ",0.00"; //DEBE
                    queryString += "," + String.valueOf(haber8Txt.getDoubleValueDoNotThrow()); // Haber
                    queryString += ",0.00"; //DEBEQ.
                    queryString += "," + String.valueOf(haber8Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
                }
            }
            queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + tipoDocumentoCbx.getValue() + " " + serieTxt.getValue().trim() + " " + numeroTxt.getValue() + " " + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            queryString += "," + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ",null";
            queryString += ",'0'";
            queryString += ",0";
            queryString += ",'0'";
            queryString += "," + ordenCompraCbx.getValue();
            queryString += ",0";
            queryString += ",'" + centroCosto8Txt.getValue() + "'";
            queryString += ")";
        }

//// noveno ingreso
        if (cuentaContable9Cbx.getValue() != null && debe9Txt.getDoubleValueDoNotThrow() != 0
                || (cuentaContable9Cbx.getValue() != null && haber9Txt.getDoubleValueDoNotThrow() > 0)) {

            queryString += ",(";
            queryString += empresaId;
            queryString += ",'" + estatusPartidaEdit + "'";
            queryString += ",'" + nuevoCodigoPartida + "'";
            queryString += ",'" + nuevoCodigoPartida + "'";//codigoCC
            queryString += ",'" + tipoDocumentoCbx.getValue() + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + nitProveedorTxt.getValue() + "'";
            queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            if (checkbox1.getValue().equals(true)) {
                queryString += ",'SI'";
            } else {
                queryString += ",'NO'";
            }
            queryString += ",UPPER('" + serieTxt.getValue().trim() + "')";
            queryString += ",'" + numeroTxt.getValue().trim() + "'";
            queryString += "," + cuentaContable9Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow()); //MONTO DOCUMENTO
            if (debe9Txt.getDoubleValueDoNotThrow() > 0 && haber9Txt.getDoubleValueDoNotThrow() == 0.00) {
                queryString += "," + String.valueOf(debe9Txt.getDoubleValueDoNotThrow()); // DEBE
                queryString += ",0.00"; //HABER
                queryString += "," + String.valueOf(debe9Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
                queryString += ",0.00"; //HABER Q.
            } else {
                if (haber9Txt.getDoubleValueDoNotThrow() > 0 && debe9Txt.getDoubleValueDoNotThrow() == 0.00) {
                    queryString += ",0.00"; //DEBE
                    queryString += "," + String.valueOf(haber9Txt.getDoubleValueDoNotThrow()); // Haber
                    queryString += ",0.00"; //DEBEQ.
                    queryString += "," + String.valueOf(haber9Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
                }
            }
            queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + tipoDocumentoCbx.getValue() + " " + serieTxt.getValue().trim() + " " + numeroTxt.getValue() + " " + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            queryString += "," + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ",null";
            queryString += ",'0'";
            queryString += ",0";
            queryString += ",'0'";
            queryString += "," + ordenCompraCbx.getValue();
            queryString += ",0";
            queryString += ",'" + centroCosto9Txt.getValue() + "'";
            queryString += ")";
        }

//// decimo ingreso
        if (cuentaContable10Cbx.getValue() != null && debe10Txt.getDoubleValueDoNotThrow() != 0
                || (cuentaContable10Cbx.getValue() != null && haber10Txt.getDoubleValueDoNotThrow() > 0)) {

            queryString += ",(";
            queryString += empresaId;
            queryString += ",'" + estatusPartidaEdit + "'";
            queryString += ",'" + nuevoCodigoPartida + "'";
            queryString += ",'" + nuevoCodigoPartida + "'";//codigoCC
            queryString += ",'" + tipoDocumentoCbx.getValue() + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + nitProveedorTxt.getValue() + "'";
            queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            if (checkbox1.getValue().equals(true)) {
                queryString += ",'SI'";
            } else {
                queryString += ",'NO'";
            }
            queryString += ",UPPER('" + serieTxt.getValue().trim() + "')";
            queryString += ",'" + numeroTxt.getValue().trim() + "'";
            queryString += "," + cuentaContable10Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow()); //MONTO DOCUMENTO
            if (debe10Txt.getDoubleValueDoNotThrow() > 0 && haber10Txt.getDoubleValueDoNotThrow() == 0.00) {
                queryString += "," + String.valueOf(debe10Txt.getDoubleValueDoNotThrow()); // DEBE
                queryString += ",0.00"; //HABER
                queryString += "," + String.valueOf(debe10Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
                queryString += ",0.00"; //HABER Q.
            } else {
                if (haber10Txt.getDoubleValueDoNotThrow() > 0 && debe10Txt.getDoubleValueDoNotThrow() == 0.00) {
                    queryString += ",0.00"; //DEBE
                    queryString += "," + String.valueOf(haber10Txt.getDoubleValueDoNotThrow()); // Haber
                    queryString += ",0.00"; //DEBEQ.
                    queryString += "," + String.valueOf(haber10Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
                }
            }
            queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + tipoDocumentoCbx.getValue() + " " + serieTxt.getValue().trim() + " " + numeroTxt.getValue() + " " + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            queryString += "," + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ",null";
            queryString += ",'0'";
            queryString += ",0";
            queryString += ",'0'";
            queryString += "," + ordenCompraCbx.getValue();
            queryString += ",0";
            queryString += ",'" + centroCosto10Txt.getValue() + "'";
            queryString += ")";
        }
        
System.out.println(queryString);

        if (queryString.contains(",current_timestamp") == false) {
            Notification notif = new Notification("POR FAVOR COMPLETAR LOS DATOS DE LA PARTIDA.", Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.WARNING);
            notif.show(Page.getCurrent());
            return;
        }

        try {
            if (nombreArchvivo == null && tipoArchivo == null) {
                stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                stQuery.executeUpdate(queryString);
            } else {
                stPreparedQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().prepareStatement(queryString);
                stPreparedQuery.setBinaryStream(1, documentStreamResource.getStream().getStream(), documentStreamResource.getStream().getStream().available());
                stPreparedQuery.executeUpdate();
            }

            //         if(!variableTemp.isEmpty()){
            //             cambiarEstatusToken(nuevoCodigoPartida);
            //         }
            Notification notif = new Notification("REGISTRO AGREGADO EXITOSAMENTE.", Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.CHECK);
            notif.show(Page.getCurrent());

            UI mainUI = UI.getCurrent();

            if (mainUI.getNavigator().getCurrentView().getClass().getSimpleName().equals("FacturaVentaView")) {
                Object selectedObject = ((FacturaVentaView) (mainUI.getNavigator().getCurrentView())).facturasVentaGrid.getSelectedRow();
                ((FacturaVentaView) (mainUI.getNavigator().getCurrentView())).container.getContainerProperty(selectedObject, IMAGEN_PROPERTY).setValue("Visualizar");
            }
            if (mainUI.getNavigator().getCurrentView().getClass().getSimpleName().equals("IngresoDocumentosView")) {
                Object selectedObject = ((IngresoDocumentosView) (mainUI.getNavigator().getCurrentView())).documentosGrid.getSelectedRow();
                ((IngresoDocumentosView) (mainUI.getNavigator().getCurrentView())).documentsContainer.getContainerProperty(selectedObject, IMAGEN_PROPERTY).setValue("Visualizar");
            }

            ((IngresoDocumentosView) (mainUI.getNavigator().getCurrentView())).llenarTablaFactura(empresaId, 0);

            Notification.show("Registro agregado exitosamente!", Notification.Type.TRAY_NOTIFICATION);

            close();

        } catch (Exception ex1) {
            System.out.println("Error al insertar facturas  : " + ex1.getMessage());

            Notification notif = new Notification("HA OCURRIDO UN ERROR DE BASE DE DATOS : " + ex1.getMessage(), Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.WARNING);
            notif.show(Page.getCurrent());
            ex1.printStackTrace();

            try {
                String emailsTo[] = {"alerta@simpletecno.com"};
                MyEmailMessanger eMail = new MyEmailMessanger();

                eMail.postMail(emailsTo, "Error en SOPDI", "Error en base de datos :  " + this.getClass().getName() + " -->" + ex1.getMessage());
            } catch (MessagingException ex2) {
                Logger.getLogger(SopdiUI.class.getName()).log(Level.SEVERE, null, ex2);
            }

        }

    }

    public void llenarComboOrdenCompra() {
        queryString = " SELECT * FROM orden_compra ";
        queryString += " Where IdEmpresa = " + empresaId;
        queryString += " AND IdProyecto = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrProjectId();;

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) { //  encontrado                
                ordenCompraCbx.addItem(rsRecords.getString("Id"));
                ordenCompraCbx.setItemCaption(rsRecords.getString("Id"), rsRecords.getString("NOC"));
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar la tabla de orden de compra " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void llenarComboProveedor() {
        queryString = " SELECT * FROM proveedor_empresa ";
        queryString += " WHERE Inhabilitado = 0";
        queryString += " AND EsProveedor = 1";
        queryString += " AND IdEmpresa = " + empresaId;
        queryString += " ORDER BY Nombre ";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) { //  encontrado                
                proveedorCbx.addItem(rsRecords.getString("IDProveedor"));
                proveedorCbx.setItemCaption(rsRecords.getString("IDProveedor"), rsRecords.getString("Nombre"));
                proveedorCbx.getItem(rsRecords.getString("IDProveedor")).getItemProperty(NIT_PROPERTY).setValue(rsRecords.getString("NIT"));
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar Proveedores " + ex1.getMessage());
            ex1.printStackTrace();
        }
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
