package com.simpletecno.sopdi.compras;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.simpletecno.sopdi.utilerias.ValidarTokenForm;
import com.simpletecno.sopdi.contabilidad.LibroDiarioView;
import com.vaadin.server.FontAwesome;
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
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.vaadin.ui.NumberField;

/**
 *
 * @author user
 */
public class EditarPartidaLiquidacion extends Window {

    VerticalLayout mainLayout;

    TextField tipoDocumentoTxt;
    TextField serieTxt;
    TextField numeroTxt;
    TextField nitProveedotTxt;

    ComboBox empresaCbx;
    ComboBox proveedorCbx;
    ComboBox monedaCbx;
    ComboBox cuentaContableCbx;
    ComboBox cuentaContable1Cbx;
    ComboBox cuentaContable2Cbx;
    ComboBox cuentaContable3Cbx;
    ComboBox cuentaContable4Cbx;

    DateField fechaDt;

    NumberField montoTxt;
    NumberField tasaCambioTxt;

    NumberField haberTxt;
    NumberField haber1Txt;
    NumberField haber2Txt;
    NumberField haber3Txt;
    NumberField haber4Txt;

    NumberField debeTxt;
    NumberField debe1Txt;
    NumberField debe2Txt;
    NumberField debe3Txt;
    NumberField debe4Txt;

    Button grabarBtn;
    Button cerrarFacturasBtn;

    static final String NIT_PROPERTY = "Eliminar";

    UI mainUI;
    Statement stQuery;
    ResultSet rsRecords;
    Statement stQuery2;
    ResultSet rsRecords2;
    String queryString;
    BigDecimal totalDebe;
    BigDecimal totalHaber;

    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");
    static DecimalFormat numberFormat2 = new DecimalFormat("#,###,##0");

    String idLiquidacionEdit;
    String idLiquidadorEdit;
    String liquidadorNombre;
    String codigoPartidaEdit;
    String nuevoCodigoPartida;
    String estatusPartidaEdit;
    String idEmpresa;
    String descripcionEdit;

    String codigoCC;
    
    String variableTemp ="";

    public EditarPartidaLiquidacion(String codigoPartida, String codigoCC) {

        this.codigoPartidaEdit = codigoPartida;
        this.codigoCC = codigoCC;

        this.mainUI = UI.getCurrent();
        setResponsive(true);
        setWidth("87%");
        setHeight("85%");

        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);

        HorizontalLayout layoutTitle = new HorizontalLayout();
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
        empresaCbx.select(empresaCbx.getItemIds().iterator().next());

        Label titleLbl = new Label("EDITAR LIQUIDACION");
        titleLbl.addStyleName(ValoTheme.LABEL_H3);
        titleLbl.addStyleName("h3_custom");

        layoutTitle.addComponent(empresaCbx);
        layoutTitle.setComponentAlignment(empresaCbx, Alignment.MIDDLE_LEFT);

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

        tipoDocumentoTxt = new TextField();
        tipoDocumentoTxt.setInputPrompt("Tipo de documento");
        tipoDocumentoTxt.setDescription("Tipo de documento");
        tipoDocumentoTxt.setWidth("100%");
        tipoDocumentoTxt.addStyleName("mayusculas");
        tipoDocumentoTxt.setValue("FACTURA");
        tipoDocumentoTxt.setDescription("FACTURA/RECIBO");

        serieTxt = new TextField();
        serieTxt.setWidth("100%");
        serieTxt.addStyleName("mayusculas");
        serieTxt.setInputPrompt("Serie del documento");
        serieTxt.setDescription("Serie del documento");

        numeroTxt = new TextField();
        numeroTxt.setWidth("100%");
        numeroTxt.setInputPrompt("Número del documento");
        numeroTxt.setDescription("Número del documento");

        nitProveedotTxt = new TextField();
        nitProveedotTxt.setWidth("100%");
        nitProveedotTxt.setInputPrompt("Nit del proveedor");
        nitProveedotTxt.setDescription("Nit del proveedor");

        proveedorCbx = new ComboBox();
        proveedorCbx.setWidth("100%");
        proveedorCbx.setInputPrompt("Proveedor");
        proveedorCbx.addContainerProperty(NIT_PROPERTY, String.class, "");
        proveedorCbx.setFilteringMode(FilteringMode.CONTAINS);
        proveedorCbx.setInvalidAllowed(true);
        proveedorCbx.setNewItemsAllowed(true);
        proveedorCbx.addValueChangeListener(event -> {
            if (event.getProperty() == null) {
                return;
            }
            verificarProveedorInsititucion(String.valueOf(proveedorCbx.getValue()));
        });

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

        monedaCbx = new ComboBox("Moneda :");
        monedaCbx.setInputPrompt("Moneda");
        monedaCbx.setWidth("100%");
        monedaCbx.addItem("QUETZALES");
        monedaCbx.addItem("DOLARES");
        monedaCbx.select("QUETZALES");
        monedaCbx.addValueChangeListener(evet -> {
            if(monedaCbx.getValue() != null){
                if(monedaCbx.getValue().equals("DOLARES")){                                       
                   tasaCambioTxt.setValue((Float.toString(((SopdiUI) UI.getCurrent()).sessionInformation.getFltExchangeRate())));                   
                }else{
                    tasaCambioTxt.setValue(1.00);
                }
            }else{
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
        tasaCambioTxt.setGroupingUsed(true);
        tasaCambioTxt.setGroupingSeparator(',');
        tasaCambioTxt.setGroupingSize(3);
        tasaCambioTxt.setImmediate(true);
        tasaCambioTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        tasaCambioTxt.setWidth("100%");
        tasaCambioTxt.setValue(1.00);

        leftVerticalLayout.addComponent(tipoDocumentoTxt);
        leftVerticalLayout.addComponent(serieTxt);
        leftVerticalLayout.addComponent(numeroTxt);
        leftVerticalLayout.addComponent(nitProveedotTxt);
        leftVerticalLayout.addComponent(proveedorCbx);
        leftVerticalLayout.addComponent(fechaDt);
        leftVerticalLayout.addComponent(montoTxt);
        leftVerticalLayout.addComponent(monedaCbx);
        leftVerticalLayout.addComponent(tasaCambioTxt);

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

        cuentaContableCbx = new ComboBox("Cuenta contable");
        cuentaContableCbx.setWidth("23em");
        cuentaContableCbx.setFilteringMode(FilteringMode.CONTAINS);

        cuentaContable1Cbx = new ComboBox();
        cuentaContable1Cbx.setWidth("23em");
        cuentaContable1Cbx.setFilteringMode(FilteringMode.CONTAINS);

        cuentaContable2Cbx = new ComboBox();
        cuentaContable2Cbx.setWidth("23em");
        cuentaContable2Cbx.setFilteringMode(FilteringMode.CONTAINS);

        cuentaContable3Cbx = new ComboBox();
        cuentaContable3Cbx.setWidth("23em");
        cuentaContable3Cbx.setFilteringMode(FilteringMode.CONTAINS);

        cuentaContable4Cbx = new ComboBox();
        cuentaContable4Cbx.setWidth("23em");
        cuentaContable4Cbx.setFilteringMode(FilteringMode.CONTAINS);

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

        grabarBtn = new Button("Grabar");
        grabarBtn.setIcon(FontAwesome.SAVE);
        grabarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                insertTablaFactura();
            }
        });

        cerrarFacturasBtn = new Button("Cerrar factura");
        cerrarFacturasBtn.setIcon(FontAwesome.CHECK);
        cerrarFacturasBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                //comprobarBalance();
            }
        });

        layoutHorizontal.addComponent(cuentaContableCbx);
        layoutHorizontal.addComponent(debeTxt);
        layoutHorizontal.addComponent(haberTxt);

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

        layoutHorizontal5.addComponent(grabarBtn);
        layoutHorizontal5.setComponentAlignment(grabarBtn, Alignment.BOTTOM_CENTER);

        rightVerticalLayout.addComponent(layoutHorizontal);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal, Alignment.MIDDLE_LEFT);

        rightVerticalLayout.addComponent(layoutHorizontal1);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal1, Alignment.MIDDLE_LEFT);

        rightVerticalLayout.addComponent(layoutHorizontal2);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal2, Alignment.MIDDLE_LEFT);

        rightVerticalLayout.addComponent(layoutHorizontal3);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal3, Alignment.MIDDLE_LEFT);

        rightVerticalLayout.addComponent(layoutHorizontal4);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal4, Alignment.MIDDLE_LEFT);

        rightVerticalLayout.addComponent(layoutHorizontal5);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal5, Alignment.MIDDLE_CENTER);

        horizontalLayout.addComponents(leftVerticalLayout, rightVerticalLayout);
        //horizontalLayout.setComponentAlignment(leftVerticalLayout, Alignment.MIDDLE_LEFT);
        horizontalLayout.setComponentAlignment(rightVerticalLayout, Alignment.TOP_LEFT);

        return horizontalLayout;

    }

    public void llenarComboProveedor() {
        queryString = " SELECT * from proveedor ";
        queryString += " WHERE Inhabilitado = 0";
        queryString += " AND EsProveedor = 1";
        queryString += " Order By Nombre ";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) { //  encontrado
                proveedorCbx.addItem(rsRecords.getString("IDProveedor"));
                proveedorCbx.setItemCaption(rsRecords.getString("IDProveedor"), rsRecords.getString("NOMBRE"));
                proveedorCbx.getItem(rsRecords.getString("IDProveedor")).getItemProperty(NIT_PROPERTY).setValue(rsRecords.getString("NIT"));
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar Proveedores " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void verificarProveedorInsititucion(String id) {
        if (id == null) {
            return;
        }
        /**
         * cuentaContableCbx.setReadOnly(false);
         *
         * queryString = " SELECT * from proveedor "; queryString += " WHERE
         * IDProveedor = " + id;
         *
         * try { stQuery = ((SopdiUI)
         * UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
         * rsRecords = stQuery.executeQuery(queryString);
         *
         * if (rsRecords.next()) { // encontrado if
         * (rsRecords.getString("Grupo").equals("Instituciones")) { //
         * cuentaContableCbx.select(cuentaInstituciones); } else { //
         * cuentaContableCbx.select(cuentaProveedoresLocales); } }
         * cuentaContableCbx.setReadOnly(true);
         *
         * } catch (Exception ex1) { System.out.println("Error al listar
         * Proveedores " + ex1.getMessage()); ex1.printStackTrace(); }
         *
         */
    }

    public void llenarCampos() {

        estatusPartidaEdit = "INGRESADO";

        queryString = " SELECT contabilidad_partida.*, proveedor.Nombre as LiquidadorNombre ";
        queryString += " From contabilidad_partida ";
        queryString += " Left Join proveedor On proveedor.IdProveedor =  contabilidad_partida.IdLiquidador ";
        queryString += " Where contabilidad_partida.CodigoPartida = '" + codigoPartidaEdit + "'";

        try {
            stQuery2 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords2 = stQuery2.executeQuery(queryString);

            int contador = 0;
            double montoFactura = 0.00;

            while (rsRecords2.next()) { //  encontrado

                if (contador == 0) {
                    tipoDocumentoTxt.setValue(rsRecords2.getString("TipoDocumento"));
                    estatusPartidaEdit = rsRecords2.getString("Estatus");
                    empresaCbx.select(rsRecords2.getString("IdEmpresa"));
                    serieTxt.setValue(rsRecords2.getString("SerieDocumento"));
                    numeroTxt.setValue(rsRecords2.getString("NumeroDocumento"));
                    nitProveedotTxt.setValue(rsRecords2.getString("NITProveedor"));
//System.out.println("NombreProveedor=" + rsRecords2.getString("NombreProveedor"));
                    if (rsRecords2.getString("IdProveedor").equals("0") || rsRecords2.getObject("IdProveedor") == null) {
                        proveedorCbx.addItem("0");
                        proveedorCbx.setItemCaption("0", rsRecords2.getString("NombreProveedor"));
                        proveedorCbx.getItem("0").getItemProperty(NIT_PROPERTY).setValue(rsRecords2.getString("NITProveedor"));
                        proveedorCbx.select("0");
                    } else {
                        proveedorCbx.select(rsRecords2.getString("IdProveedor"));
                    }
                    fechaDt.setValue(rsRecords2.getDate("Fecha"));
                    monedaCbx.setValue(rsRecords2.getString("MonedaDocumento"));
                    tasaCambioTxt.setValue(rsRecords2.getDouble("TipoCambio"));
                    cuentaContableCbx.select(rsRecords2.getString("IdNomenclatura"));
                    haberTxt.setValue(rsRecords2.getDouble("Haber"));
                    debeTxt.setReadOnly(true);

                    descripcionEdit = rsRecords2.getString("Descripcion");
                    idLiquidacionEdit = rsRecords2.getString("IdLiquidacion");
                    idLiquidadorEdit = rsRecords2.getString("IdLiquidador");
                    liquidadorNombre = rsRecords2.getString("LiquidadorNombre");
                    idEmpresa = rsRecords2.getString("IdEmpresa");

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

                contador = contador + 1;
                montoFactura += rsRecords2.getDouble("Haber");

            }

            montoTxt.setValue(montoFactura);
            empresaCbx.setReadOnly(false);
            serieTxt.setReadOnly(false);
            numeroTxt.setReadOnly(false);
            nitProveedotTxt.setReadOnly(false);
            proveedorCbx.setReadOnly(false);
            fechaDt.setReadOnly(false);
            montoTxt.setReadOnly(false);
            monedaCbx.setReadOnly(false);
            tasaCambioTxt.setReadOnly(false);
            cuentaContableCbx.setReadOnly(true);

        } catch (Exception ex1) {
            System.out.println("Error al listar cuentas contables: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void llenarComboCuentaContable() {

        queryString = " SELECT * from contabilidad_nomenclatura ";
        queryString += " WHERE  FiltrarFormularioLiquidacion = 'S'";

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

            }

        } catch (Exception ex1) {
            System.out.println("Error al listar cuentas contables: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void llenarComboEmpresa() {
        queryString = " SELECT * from contabilidad_empresa";
        queryString += " Where IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) { //  encontrado
                empresaCbx.addItem(rsRecords.getString("IdEmpresa"));
                empresaCbx.setItemCaption(rsRecords.getString("IdEmpresa"), rsRecords.getString("Empresa"));
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar empresas: " + ex1.getMessage());
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
             
//System.out.println("Hay "+dias+" dias de diferencia");
            
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

        totalDebe = totalDebe = new BigDecimal(debe1Txt.getDoubleValueDoNotThrow()
                + debe2Txt.getDoubleValueDoNotThrow() + debe3Txt.getDoubleValueDoNotThrow()
                + debe4Txt.getDoubleValueDoNotThrow()).setScale(2, BigDecimal.ROUND_HALF_UP);

        totalHaber = totalDebe = new BigDecimal(haberTxt.getDoubleValueDoNotThrow() + haber1Txt.getDoubleValueDoNotThrow()
                + haber2Txt.getDoubleValueDoNotThrow() + haber3Txt.getDoubleValueDoNotThrow()
                + haber4Txt.getDoubleValueDoNotThrow()).setScale(2, BigDecimal.ROUND_HALF_UP);

        totalDebe.setScale(2, BigDecimal.ROUND_DOWN);
        totalHaber.setScale(2, BigDecimal.ROUND_DOWN);

        if (((SopdiUI) UI.getCurrent()).esMesCerrado(String.valueOf(empresaCbx.getValue()), Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()))) {
            Notification.show("La fecha del documento no puede ser de un mes ya cerrado contablemente, revise!", Notification.Type.WARNING_MESSAGE);
            fechaDt.focus();
            return;
        }
        if (!((SopdiUI) UI.getCurrent()).esPrimerMesAbierto(String.valueOf(empresaCbx.getValue()), Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()))) {
            Notification.show("El mes abierto a operaciones es : " + ((SopdiUI) UI.getCurrent()).primerMesAbierto(String.valueOf(empresaCbx.getValue())), Notification.Type.WARNING_MESSAGE);
            fechaDt.focus();
            return;
        }
        if (totalDebe.doubleValue() != montoTxt.getDoubleValueDoNotThrow()) {
            Notification.show("Partida está descuadrada DEBE, por favor revisar.", Notification.Type.ERROR_MESSAGE);
            System.out.println("monto=" + montoTxt.getDoubleValueDoNotThrow() + " RAW debe=" + totalDebe);
            return;
        }
        if (totalHaber.doubleValue() != montoTxt.getDoubleValueDoNotThrow()) {
            Notification.show("Partida está descuadrada HABER, por favor revisar.", Notification.Type.ERROR_MESSAGE);
            System.out.println("monto=" + montoTxt.getDoubleValueDoNotThrow() + " RAW haber=" + totalHaber);
            return;
        }
        if (cuentaContable1Cbx.getValue() == null && debe1Txt.getDoubleValueDoNotThrow() > 0) {
            Notification.show("Partida está descuadrada, por favor revisar.", Notification.Type.ERROR_MESSAGE);
            return;
        }
        if (cuentaContable2Cbx.getValue() == null && debe2Txt.getDoubleValueDoNotThrow() > 0) {
            Notification.show("Partida está descuadrada, por favor revisar.", Notification.Type.ERROR_MESSAGE);
            return;
        }
        if (cuentaContable3Cbx.getValue() == null && debe3Txt.getDoubleValueDoNotThrow() > 0) {
            Notification.show("Partida está descuadrada, por favor revisar.", Notification.Type.ERROR_MESSAGE);
            return;
        }
        if (cuentaContable4Cbx.getValue() == null && debe4Txt.getDoubleValueDoNotThrow() > 0) {
            Notification.show("Partida está descuadrada, por favor revisar.", Notification.Type.ERROR_MESSAGE);
            return;
        }
        if (cuentaContable1Cbx.getValue() == null && haber1Txt.getDoubleValueDoNotThrow() > 0) {
            Notification.show("Partida está descuadrada, por favor revisar.", Notification.Type.ERROR_MESSAGE);
            return;
        }
        if (cuentaContable2Cbx.getValue() == null && haber2Txt.getDoubleValueDoNotThrow() > 0) {
            Notification.show("Partida está descuadrada, por favor revisar.", Notification.Type.ERROR_MESSAGE);
            return;
        }
        if (cuentaContable3Cbx.getValue() == null && haber3Txt.getDoubleValueDoNotThrow() > 0) {
            Notification.show("Partida está descuadrada, por favor revisar.", Notification.Type.ERROR_MESSAGE);
            return;
        }
        if (cuentaContable4Cbx.getValue() == null && haber4Txt.getDoubleValueDoNotThrow() > 0) {
            Notification.show("Partida está descuadrada, por favor revisar.", Notification.Type.ERROR_MESSAGE);
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
        }

        String fecha = Utileria.getFechaYYYYMMDD_1(fechaDt.getValue());
        String ultimoEncontado;
        String dia = fecha.substring(8, 10);
        String mes = fecha.substring(5, 7);
        String año = fecha.substring(0, 4);

        nuevoCodigoPartida = String.valueOf(empresaCbx.getValue()) + año + mes + dia + "2";

        queryString = " select codigoPartida from contabilidad_partida ";
        queryString += " where codigoPartida like '" + nuevoCodigoPartida + "%'";
        queryString += " order by codigoPartida desc ";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado

                ultimoEncontado = rsRecords.getString("codigoPartida").substring(12, 15);

                System.out.println("ultimo encontrado " + ultimoEncontado);

                nuevoCodigoPartida += String.format("%03d", (Integer.valueOf(ultimoEncontado) + 1));

            } else {
                nuevoCodigoPartida += "001";
            }

        } catch (Exception ex1) {
            System.out.println("Error al buscar el ultimo codigoPartida" + ex1.getMessage());
            ex1.printStackTrace();
        }

        queryString = " Insert Into contabilidad_partida (IdEmpresa, Estatus, CodigoPartida, CodigoCC, ";
        queryString += " TipoDocumento, Fecha, NITProveedor, IdProveedor, NombreProveedor,";
        queryString += " SerieDocumento, NumeroDocumento, IdNomenclatura, MonedaDocumento, Debe, Haber, ";
        queryString += " DebeQuetzales, HaberQuetzales, TipoCambio, Saldo, ";
        queryString += " IdLiquidador, IdLiquidacion,  ";
        queryString += " Descripcion, CreadoUsuario, CreadoFechaYHora)";
        queryString += " Values ";

//// primer ingreso
        if (cuentaContableCbx.getValue() != null && haberTxt.getDoubleValueDoNotThrow() > 0) {
            queryString += "(";
            queryString += empresaCbx.getValue();
            queryString += ",'" + estatusPartidaEdit + "'";
            queryString += ",'" + nuevoCodigoPartida + "'";
            queryString += ",'" + codigoCC + "'";
            queryString += ",'" + tipoDocumentoTxt.getValue() + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += ",'" + nitProveedotTxt.getValue().replaceAll("'", "") + "'";
            try {
                Integer.parseInt(String.valueOf(proveedorCbx.getValue()));
                queryString += ", " + proveedorCbx.getValue();
                queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            } catch (Exception ex) {
                queryString += ",0";
                queryString += ",'" + proveedorCbx.getValue() + "'";
            }
            queryString += ",UPPER('" + serieTxt.getValue() + "')";
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += "," + cuentaContableCbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += ",0.00"; //DEBE
            queryString += "," + String.valueOf(haberTxt.getDoubleValueDoNotThrow()); // Haber
            queryString += ",0.00"; //DEBEQ.
            queryString += "," + String.valueOf(haberTxt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(haberTxt.getDoubleValueDoNotThrow()); // Haber (SALDO)
//            queryString += "," + montoTxt.getDoubleValueDoNotThrow();
            queryString += "," + idLiquidadorEdit;
            queryString += "," + idLiquidacionEdit;
            queryString += ",'" + descripcionEdit + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";
        }

//// segundo  ingreso
        if ((cuentaContable1Cbx.getValue() != null && debe1Txt.getDoubleValueDoNotThrow() > 0)
                || (cuentaContable1Cbx.getValue() != null && haber1Txt.getDoubleValueDoNotThrow() > 0)) {
            queryString += ",(";
            queryString += empresaCbx.getValue();
            queryString += ",'" + estatusPartidaEdit + "'";
            queryString += ",'" + nuevoCodigoPartida + "'";
            queryString += ",'" + nuevoCodigoPartida + "'";
            queryString += ",'" + tipoDocumentoTxt.getValue() + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += ",'" + nitProveedotTxt.getValue().replaceAll("'", "") + "'";
            try {
                Integer.parseInt(String.valueOf(proveedorCbx.getValue()));
                queryString += ", " + proveedorCbx.getValue();
                queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            } catch (Exception ex) {
                queryString += ",0";
                queryString += ",'" + proveedorCbx.getValue() + "'";
            }
            queryString += ",UPPER('" + serieTxt.getValue() + "')";
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += "," + cuentaContable1Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
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
            queryString += "," + idLiquidadorEdit;
            queryString += "," + idLiquidacionEdit;
            queryString += ",'" + descripcionEdit + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";
        }

//// tercer ingreso
        if (cuentaContable2Cbx.getValue() != null && debe2Txt.getDoubleValueDoNotThrow() > 0
                || (cuentaContable2Cbx.getValue() != null && haber2Txt.getDoubleValueDoNotThrow() > 0)) {

            queryString += ",(";
            queryString += empresaCbx.getValue();
            queryString += ",'" + estatusPartidaEdit + "'";
            queryString += ",'" + nuevoCodigoPartida + "'";
            queryString += ",'" + nuevoCodigoPartida + "'";
            queryString += ",'" + tipoDocumentoTxt.getValue() + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += ",'" + nitProveedotTxt.getValue().replaceAll("'", "") + "'";
            try {
                Integer.parseInt(String.valueOf(proveedorCbx.getValue()));
                queryString += ", " + proveedorCbx.getValue();
                queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            } catch (Exception ex) {
                queryString += ",0";
                queryString += ",'" + proveedorCbx.getValue() + "'";
            }
            queryString += ",UPPER('" + serieTxt.getValue() + "')";
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += "," + cuentaContable2Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
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

            queryString += "," + idLiquidadorEdit;
            queryString += "," + idLiquidacionEdit;
            queryString += ",'" + descripcionEdit + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";
        }

//// cuarto ingreso
        if (cuentaContable3Cbx.getValue() != null && debe3Txt.getDoubleValueDoNotThrow() > 0
                || (cuentaContable3Cbx.getValue() != null && haber3Txt.getDoubleValueDoNotThrow() > 0)) {

            queryString += ",(";
            queryString += empresaCbx.getValue();
            queryString += ",'" + estatusPartidaEdit + "'";
            queryString += ",'" + nuevoCodigoPartida + "'";
            queryString += ",'" + nuevoCodigoPartida + "'";
            queryString += ",'" + tipoDocumentoTxt.getValue() + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += ",'" + nitProveedotTxt.getValue().replaceAll("'", "") + "'";
            try {
                Integer.parseInt(String.valueOf(proveedorCbx.getValue()));
                queryString += ", " + proveedorCbx.getValue();
                queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            } catch (Exception ex) {
                queryString += ",0";
                queryString += ",'" + proveedorCbx.getValue() + "'";
            }
            queryString += ",UPPER('" + serieTxt.getValue() + "')";
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += "," + cuentaContable3Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
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
            queryString += "," + idLiquidadorEdit;
            queryString += "," + idLiquidacionEdit;
            queryString += ",'" + descripcionEdit + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";
        }

//// QUINTO INGRESO
        if (cuentaContable4Cbx.getValue() != null && debe4Txt.getDoubleValueDoNotThrow() != 0
                || (cuentaContable4Cbx.getValue() != null && haber4Txt.getDoubleValueDoNotThrow() > 0)) {

            queryString += ",(";
            queryString += empresaCbx.getValue();
            queryString += ",'" + estatusPartidaEdit + "'";
            queryString += ",'" + nuevoCodigoPartida + "'";
            queryString += ",'" + nuevoCodigoPartida + "'";
            queryString += ",'" + tipoDocumentoTxt.getValue() + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += ",'" + nitProveedotTxt.getValue().replaceAll("'", "") + "'";
            try {
                Integer.parseInt(String.valueOf(proveedorCbx.getValue()));
                queryString += ", " + proveedorCbx.getValue();
                queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            } catch (Exception ex) {
                queryString += ",0";
                queryString += ",'" + proveedorCbx.getValue() + "'";
            }
            queryString += ",UPPER('" + serieTxt.getValue() + "')";
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += "," + cuentaContable4Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";

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
            queryString += "," + idLiquidadorEdit;
            queryString += "," + idLiquidacionEdit;
            queryString += ",'" + descripcionEdit + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";
        }

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);
            
            if(!variableTemp.isEmpty()){
                cambiarEstatusToken(codigoPartidaEdit);
            }

            Notification.show("Registro agregado con exito!", Notification.Type.HUMANIZED_MESSAGE);

            if (mainUI.getNavigator().getCurrentView().getClass().getSimpleName().equals("LibroDiarioView")) {
                ((LibroDiarioView) (mainUI.getNavigator().getCurrentView())).llenarGridLibroDiario(String.valueOf(idEmpresa));
            } else {
                ((IngresoLiquidacionGastoView) (mainUI.getNavigator().getCurrentView())).llenarTablaPartida(idLiquidacionEdit, codigoPartidaEdit);
                ((IngresoLiquidacionGastoView) (mainUI.getNavigator().getCurrentView())).llenarTablaLiquidacion(idEmpresa);
                ((IngresoLiquidacionGastoView) (mainUI.getNavigator().getCurrentView())).llenarTablaFactura(idLiquidacionEdit);
            }

            close();

        } catch (Exception ex1) {
            System.out.println("Error al insertar facturas  : " + ex1.getMessage());
            ex1.printStackTrace();
        }

    }
    
    public void cambiarEstatusToken(String codigoPartida){
        
        try {
            queryString = "UPDATE token SET ";
            queryString += " IdUsuario = " +((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserId();
            queryString += ", Usuario = '" + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserFullName() + "'";
            queryString += ", FechaUsado = current_timestamp";
            queryString += ", CodigoPartida = '" + codigoPartida +"'";
            queryString += ", Estatus = 'UTILIZADO'";
            queryString += " Where Codigo = '" + variableTemp +"'";
            
            variableTemp = "";
            
            stQuery.executeUpdate(queryString);           
        } catch (Exception e) {
            System.out.println("Error al intentar cambiar estatus token : " + e);
            e.printStackTrace();
        }                        
    }

}
