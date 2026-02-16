package com.simpletecno.sopdi.compras;

import com.simpletecno.sopdi.utilerias.MyEmailMessanger;
import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.simpletecno.sopdi.utilerias.ValidarTokenForm;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.SelectionEvent;
import com.vaadin.event.SelectionEvent.SelectionListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
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
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.renderers.ClickableRenderer.RendererClickEvent;
import com.vaadin.ui.themes.ValoTheme;
import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.ui.NumberField;

import javax.mail.MessagingException;

/**
 *
 * @author user
 */
public class IngresoLiquidacionGastoForm extends Window {

    VerticalLayout mainLayout;
    VerticalLayout reportLayoutFactura = new VerticalLayout();

    ComboBox porContabilizarCbx = new ComboBox("Por contabilizar");

    ComboBox centroCostoCbx;
    ComboBox tipoDocumentoCbx;
    TextField serieTxt;
    TextField numeroTxt;
    TextField nitProveedorTxt;
    TextField idLiquidacionTxt;

    ComboBox empresaCbx;
    ComboBox liquidadorCbx;
    ComboBox proveedorCbx;
    ComboBox monedaCbx;
    ComboBox cuentaContable1Cbx;
    ComboBox cuentaContable2Cbx;
    ComboBox cuentaContable3Cbx;
    ComboBox cuentaContable4Cbx;

    DateField fechaDt;
    Label haberLbl = new Label();

    NumberField montoTxt;
    NumberField tasaCambioTxt;
    NumberField debe1Txt;
    NumberField debe2Txt;
    NumberField debe3Txt;
    NumberField debe4Txt;

    Button grabarBtn;
    Button cerrarLiquidacionBtn;

    String idPartidaEdit;
    String idLiquidacionEdit;
    String idLiquidadorEdit;
    String idEmpresaEdit;

    String idLiquidacionNuevo;

    BigDecimal totalDebe;

    Grid facturaGastoGrid;
    public IndexedContainer container = new IndexedContainer();
    static final String ID_PROPERTY = "Id";
    static final String SERIE_PROPERTY = "SERIE";
    static final String CODIGO_PARTIDA_PROPERTY = "C.Partida";
    static final String NUMERO_PROPERTY = "Número";
    static final String NIT_PROPERTY = "Nit";
    static final String PROVEEDOR_PROPERTY = "Proveedor";
    static final String FECHA_PROPERTY = "Fecha";
    static final String MONTO_PROPERTY = "Monto";
    static final String MONEDA_PROPERTY = "Moneda";
    static final String TIPOCAMBIO_PROPERTY = "Tipo cambio";
    static final String ELIMINAR_PROPERTY = "Eliminar";
    Grid.FooterRow footer;

    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");
    static DecimalFormat numberFormat2 = new DecimalFormat("#,###,##0");

    MarginInfo marginInfo;
    UI mainUI;
    Statement stQuery;
    ResultSet rsRecords;
    Statement stQuery1;
    ResultSet rsRecords1;
    Statement stQuery2;
    ResultSet rsRecords2;

    String variableTemp = "";
    String codigoCC;

    Boolean esNuevo = false;
    
    public IngresoLiquidacionGastoForm(
            String idPartidaEdit, 
            String idLiquidacionEdit, 
            String idLiquidadorEdit, 
            String idEmpresaEdit,
            String codigoCC
    ) {

        this.mainUI = UI.getCurrent();
        this.idPartidaEdit = idPartidaEdit;
        this.idLiquidacionEdit = idLiquidacionEdit;
        this.idLiquidadorEdit = idLiquidadorEdit;
        this.idEmpresaEdit = idEmpresaEdit;
        this.codigoCC = codigoCC;

        if(idLiquidacionEdit.equals("")){
            esNuevo = true;
        }

        setWidth("95%");
        setHeight("100%");
        this.setResponsive(true);

        Page.getCurrent().setTitle(((SopdiUI) mainUI).sessionInformation.getStrCompanyName() + " - Documento Liquidación");

        marginInfo = new MarginInfo(true, true, true, true);

        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setResponsive(true);
        mainLayout.setMargin(true);

        Label titleLbl = new Label("Ingreso de liquidación");
        titleLbl.addStyleName(ValoTheme.LABEL_H3);

        createEncabezado();
        crearDocumentoYPartida();

        if (!idPartidaEdit.trim().isEmpty()) {   // edit            
            llenarTablaLiquidacion();
        } else {
            //   buscarUltimaLiquidacion();
        }

        setContent(mainLayout);
    }

    public void createEncabezado() {

        HorizontalLayout layoutEncabezado = new HorizontalLayout();
        layoutEncabezado.setSpacing(true);
        
        layoutEncabezado.setWidth("95%");
        layoutEncabezado.addStyleName("rcorners3");

        empresaCbx = new ComboBox("Empresa :");
        empresaCbx.setStyleName(ValoTheme.COMBOBOX_HUGE);
        empresaCbx.setWidth("100%");
        empresaCbx.setInvalidAllowed(false);
        empresaCbx.setNewItemsAllowed(false);
        empresaCbx.setTextInputAllowed(false);
        empresaCbx.setNullSelectionAllowed(false);        

        llenarComboEmpresa();

        if (idLiquidacionEdit.isEmpty()) {
            empresaCbx.addValueChangeListener((Property.ValueChangeEvent change) -> {
                llenarTxtLiquidacion(String.valueOf(empresaCbx.getValue()));
            });
        }

        idLiquidacionTxt = new TextField("Liquidación:");
        idLiquidacionTxt.addStyleName(ValoTheme.TEXTFIELD_HUGE);
        idLiquidacionTxt.setWidth("30%");
        idLiquidacionTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);

        liquidadorCbx = new ComboBox("Liquidador:");
        liquidadorCbx.addStyleName(ValoTheme.COMBOBOX_HUGE);
        liquidadorCbx.setWidth("100%");
        liquidadorCbx.addValueChangeListener(e -> {
            llenarComboPorContabilizar();
        });

        llenarComboLiquidador();

        reportLayoutFactura.setWidth("85%");
        reportLayoutFactura.addStyleName("rcorners3");
        reportLayoutFactura.setResponsive(true);
        reportLayoutFactura.setMargin(true);

        container.addContainerProperty(ID_PROPERTY, String.class, null);
        container.addContainerProperty(CODIGO_PARTIDA_PROPERTY, String.class, null);
        container.addContainerProperty(SERIE_PROPERTY, String.class, null);
        container.addContainerProperty(NUMERO_PROPERTY, String.class, null);
        container.addContainerProperty(PROVEEDOR_PROPERTY, String.class, null);
        container.addContainerProperty(FECHA_PROPERTY, String.class, null);
        container.addContainerProperty(MONTO_PROPERTY, String.class, null);
        container.addContainerProperty(MONEDA_PROPERTY, String.class, null);
        container.addContainerProperty(TIPOCAMBIO_PROPERTY, String.class, null);
        container.addContainerProperty(ELIMINAR_PROPERTY, String.class, null);

        facturaGastoGrid = new Grid("Facturas", container);

        facturaGastoGrid.setImmediate(true);
        facturaGastoGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        facturaGastoGrid.setDescription("Seleccione un registro.");
        facturaGastoGrid.setHeightMode(HeightMode.ROW);
        facturaGastoGrid.setHeightByRows(3);

        facturaGastoGrid.setWidth("100%");
//        facturaGastoGrid.setHeight("50%");
        facturaGastoGrid.setResponsive(true);
        facturaGastoGrid.setEditorBuffered(false);

        facturaGastoGrid.getColumn(ELIMINAR_PROPERTY).setRenderer(new ButtonRenderer(e
                -> eliminarRegistroTabla(e)));

        facturaGastoGrid.getColumn(ID_PROPERTY).setHidable(true).setHidden(true);
        facturaGastoGrid.getColumn(CODIGO_PARTIDA_PROPERTY).setHidable(true).setHidden(true);
        facturaGastoGrid.getColumn(SERIE_PROPERTY);
        facturaGastoGrid.getColumn(NUMERO_PROPERTY);
        facturaGastoGrid.getColumn(PROVEEDOR_PROPERTY);
        facturaGastoGrid.getColumn(FECHA_PROPERTY);
        facturaGastoGrid.getColumn(MONTO_PROPERTY);
        facturaGastoGrid.setCellStyleGenerator(cellRef
                -> MONTO_PROPERTY.equals(cellRef.getPropertyId())
                ? "rightalign" : null);

        facturaGastoGrid.addSelectionListener(new SelectionListener() {
            @Override
            public void select(SelectionEvent event) {
                if (facturaGastoGrid.getSelectedRow() != null) {
                    llenarCamposFactura(
                            String.valueOf(facturaGastoGrid.getContainerDataSource().getItem(facturaGastoGrid.getSelectedRow()).getItemProperty(SERIE_PROPERTY).getValue()),
                            String.valueOf(facturaGastoGrid.getContainerDataSource().getItem(facturaGastoGrid.getSelectedRow()).getItemProperty(NUMERO_PROPERTY).getValue()),
                            String.valueOf(facturaGastoGrid.getContainerDataSource().getItem(facturaGastoGrid.getSelectedRow()).getItemProperty(PROVEEDOR_PROPERTY).getValue()),
                            String.valueOf(facturaGastoGrid.getContainerDataSource().getItem(facturaGastoGrid.getSelectedRow()).getItemProperty(FECHA_PROPERTY).getValue()),
                            String.valueOf(facturaGastoGrid.getContainerDataSource().getItem(facturaGastoGrid.getSelectedRow()).getItemProperty(CODIGO_PARTIDA_PROPERTY).getValue()));
                }
            }
        });

        footer = facturaGastoGrid.appendFooterRow();

        reportLayoutFactura.addComponent(facturaGastoGrid);
        reportLayoutFactura.setComponentAlignment(facturaGastoGrid, Alignment.MIDDLE_CENTER);

        Button nuevaBtn = new Button("Nueva factura");
        nuevaBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        nuevaBtn.setIcon(FontAwesome.PLUS);
        nuevaBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if(liquidadorCbx.getValue() == null) {
                    Notification.show("Por favor elija a la persona liquidador.");
                    liquidadorCbx.focus();
                    return;
                }
                nuevaliquidacion();
            }
        });
        reportLayoutFactura.addComponent(nuevaBtn);
        reportLayoutFactura.setComponentAlignment(nuevaBtn, Alignment.MIDDLE_CENTER);

        layoutEncabezado.addComponent(empresaCbx);
        layoutEncabezado.setComponentAlignment(empresaCbx, Alignment.TOP_LEFT);
        layoutEncabezado.addComponent(idLiquidacionTxt);
        layoutEncabezado.setComponentAlignment(idLiquidacionTxt, Alignment.TOP_CENTER);
        layoutEncabezado.addComponent(liquidadorCbx);
        layoutEncabezado.setComponentAlignment(liquidadorCbx, Alignment.TOP_CENTER);

        mainLayout.addComponent(layoutEncabezado);
        mainLayout.setComponentAlignment(layoutEncabezado, Alignment.TOP_CENTER);
        mainLayout.addComponent(reportLayoutFactura);
        mainLayout.setComponentAlignment(reportLayoutFactura, Alignment.MIDDLE_CENTER);

    }

    public void llenarComboProveedor() {
        String queryString  = " SELECT * FROM proveedor ";
        queryString += " WHERE Inhabilitado = 0 ";
        queryString += " AND EsProveedor = 1";
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

    public void verificarProveedor() {
         if (nitProveedorTxt == null) {
            return;
        }
        
        if (proveedorCbx == null) {
            return;
        }
        if (proveedorCbx.getValue() == null) {
            return;
        }
        try {
            Integer.valueOf(String.valueOf(proveedorCbx.getValue()));
        }
        catch(Exception strE) {
            return;
        }

        nitProveedorTxt.setValue("");
        
        nitProveedorTxt.setValue(String.valueOf(proveedorCbx.getItem(proveedorCbx.getValue()).getItemProperty(NIT_PROPERTY).getValue()));
    }

    public void crearDocumentoYPartida() {

        HorizontalLayout horizontalLayout =  new HorizontalLayout();
        horizontalLayout.setSpacing(true);
        horizontalLayout.setResponsive(true);
        horizontalLayout.setSizeFull();

        VerticalLayout leftVerticalLayout =  new VerticalLayout();
        leftVerticalLayout.setSpacing(true);
        leftVerticalLayout.setResponsive(true);
        leftVerticalLayout.setWidth("100%");
        leftVerticalLayout.addStyleName("rcorners3");

        VerticalLayout rightVerticalLayout =  new VerticalLayout();
        rightVerticalLayout.setSpacing(true);
        rightVerticalLayout.setResponsive(true);
        rightVerticalLayout.setSizeFull();
        rightVerticalLayout.addStyleName("rcorners3");

        horizontalLayout.addComponents(leftVerticalLayout, rightVerticalLayout);
        horizontalLayout.setComponentAlignment(leftVerticalLayout, Alignment.MIDDLE_LEFT);
        horizontalLayout.setComponentAlignment(leftVerticalLayout, Alignment.MIDDLE_RIGHT);

        mainLayout.addComponent(horizontalLayout);

        porContabilizarCbx.setWidth("100%");
        porContabilizarCbx.setNullSelectionAllowed(true);
        porContabilizarCbx.setInvalidAllowed(false);
        porContabilizarCbx.setNewItemsAllowed(false);
        porContabilizarCbx.addValueChangeListener(e -> {
            llenarFormularioPorContabilizar();
        });

        llenarComboPorContabilizar();

        centroCostoCbx = new ComboBox();
        centroCostoCbx.setInputPrompt("Centro de costo");
        centroCostoCbx.setDescription("Centro de costo");
        centroCostoCbx.setWidth("100%");
        centroCostoCbx.setTextInputAllowed(false);
        centroCostoCbx.setNewItemsAllowed(false);
        centroCostoCbx.setNullSelectionAllowed(true);
        centroCostoCbx.addValueChangeListener(event -> {
            if (event.getProperty() == null) {
                return;
            }
//            verificarCuentaContableAplicar();
        });

        llenarComboCentroCosto();

        tipoDocumentoCbx = new ComboBox();
        tipoDocumentoCbx.setInputPrompt("Tipo de documento");
        tipoDocumentoCbx.setDescription("Tipo de documento");
        tipoDocumentoCbx.setWidth("100%");
        tipoDocumentoCbx.addItem("FACTURA");
        tipoDocumentoCbx.addItem("RECIBO CONTABLE");
        tipoDocumentoCbx.addItem("FORMULARIO");
        tipoDocumentoCbx.select("FACTURA");
        tipoDocumentoCbx.addValueChangeListener(event -> {
            if (event.getProperty() == null) {
                return;
            }
            if(proveedorCbx != null) {
                if(tipoDocumentoCbx.getValue().equals("RECIBO CONTABLE") || tipoDocumentoCbx.getValue().equals("FORMULARIO")) {
                    proveedorCbx.setInvalidAllowed(false);
                    proveedorCbx.setNewItemsAllowed(false);
                }
                else {
                    proveedorCbx.setInvalidAllowed(true);
                    proveedorCbx.setNewItemsAllowed(true);
                }
            }
        });

        serieTxt = new TextField();
        serieTxt.setInputPrompt("Serie de documento");
        serieTxt.setDescription("Serie de documento");
        serieTxt.setWidth("100%");
        serieTxt.addStyleName("mayusculas");

        numeroTxt = new TextField();
        numeroTxt.setInputPrompt("Número de documento");
        numeroTxt.setDescription("Número de documento");
        numeroTxt.setWidth("100%");

        proveedorCbx = new ComboBox();
        proveedorCbx.setInputPrompt("Proveedores");
        proveedorCbx.setWidth("100%");
        proveedorCbx.addContainerProperty(NIT_PROPERTY, String.class, "");
//        proveedorCbx.setNullSelectionAllowed(true);
        proveedorCbx.setInvalidAllowed(true);
        proveedorCbx.setNewItemsAllowed(true);
        proveedorCbx.setFilteringMode(FilteringMode.CONTAINS);
        llenarComboProveedor();
        proveedorCbx.addValueChangeListener(event -> {
            if (event.getProperty() == null) {
		        return;
            }
            verificarProveedor();
//            Notification.show(String.valueOf(proveedorCbx.getValue()), Notification.Type.HUMANIZED_MESSAGE);
//            Notification.show(proveedorCbx.getItemCaption(proveedorCbx.getValue()), Notification.Type.HUMANIZED_MESSAGE);
        });

        nitProveedorTxt = new TextField();
        nitProveedorTxt.setInputPrompt("Nit del proveeedor");
        nitProveedorTxt.setDescription("Nit del proveedor");
        nitProveedorTxt.setWidth("100%");
//        nitProveedorTxt.addValueChangeListener(event
//                -> buscarProveedorPorNit()
//        );

        fechaDt = new DateField("Fecha :");
        fechaDt.setDateFormat("dd/MM/yyyy");
        fechaDt.setWidth("100%");
        fechaDt.setValue(new java.util.Date());

        montoTxt = new NumberField();
        montoTxt.setInputPrompt("Monto");
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

        montoTxt.addValueChangeListener(event -> {
            if (cuentaContable1Cbx != null) {
                haberLbl.setValue("HABER = " + cuentaContable1Cbx.getItemCaption(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getLiquidacionesCajaChicha()) + " " + montoTxt.getFormattedValue());
                if(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getIvaPorCobrar() == null) { //empresa exonerada
                    debe1Txt.setValue(montoTxt.getDoubleValueDoNotThrow());
                }
            }
        });

        monedaCbx = new ComboBox();
        monedaCbx.setInputPrompt("Moneda");
        monedaCbx.setWidth("100%");
        monedaCbx.addItem("QUETZALES");
        monedaCbx.addItem("DOLARES");
        monedaCbx.select("QUETZALES");
        monedaCbx.setVisible(true);
        monedaCbx.setInvalidAllowed(false);
        monedaCbx.setNewItemsAllowed(false);
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
        tasaCambioTxt.setVisible(true);

        leftVerticalLayout.addComponent(porContabilizarCbx);
        leftVerticalLayout.addComponent(tipoDocumentoCbx);
        leftVerticalLayout.addComponent(centroCostoCbx);
        leftVerticalLayout.addComponent(fechaDt);
        leftVerticalLayout.addComponent(serieTxt);
        leftVerticalLayout.addComponent(numeroTxt);
        leftVerticalLayout.addComponent(proveedorCbx);
        leftVerticalLayout.addComponent(nitProveedorTxt);
        leftVerticalLayout.addComponent(montoTxt);
        leftVerticalLayout.addComponent(monedaCbx);
        leftVerticalLayout.addComponent(tasaCambioTxt);

        HorizontalLayout layoutHorizontal1 = new HorizontalLayout();
        layoutHorizontal1.setSpacing(true);

        HorizontalLayout layoutHorizontal2 = new HorizontalLayout();
        layoutHorizontal2.setSpacing(true);

        HorizontalLayout layoutHorizontal3 = new HorizontalLayout();
        layoutHorizontal3.setSpacing(true);

        HorizontalLayout layoutHorizontal4 = new HorizontalLayout();
        layoutHorizontal4.setSpacing(true);

        cuentaContable1Cbx = new ComboBox("Cuenta contable :");
        cuentaContable1Cbx.setWidth("34em");
        cuentaContable1Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable1Cbx.setInvalidAllowed(false);
        cuentaContable1Cbx.setNewItemsAllowed(false);

        cuentaContable2Cbx = new ComboBox();
        cuentaContable2Cbx.setWidth("34em");
        cuentaContable2Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable2Cbx.setInvalidAllowed(false);
        cuentaContable2Cbx.setNewItemsAllowed(false);

        cuentaContable3Cbx = new ComboBox();
        cuentaContable3Cbx.setWidth("34em");
        cuentaContable3Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable3Cbx.setInvalidAllowed(false);
        cuentaContable3Cbx.setNewItemsAllowed(false);

        cuentaContable4Cbx = new ComboBox();
        cuentaContable4Cbx.setWidth("34em");
        cuentaContable4Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable4Cbx.setInvalidAllowed(false);
        cuentaContable4Cbx.setNewItemsAllowed(false);

        llenarComboCuentaContable();

        HorizontalLayout haberLayout = new HorizontalLayout();

        haberLbl.addStyleName(ValoTheme.LABEL_SUCCESS);
        haberLbl.setValue("HABER = " + cuentaContable1Cbx.getItemCaption(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getLiquidacionesCajaChicha()) + " " + montoTxt.getFormattedValue());

        haberLayout.addComponent(haberLbl);
        haberLayout.setComponentAlignment(haberLbl, Alignment.TOP_CENTER);

        debe1Txt = new NumberField("Monto : ");
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

        layoutHorizontal1.addComponent(cuentaContable1Cbx);
        layoutHorizontal1.addComponent(debe1Txt);

        layoutHorizontal2.addComponent(cuentaContable2Cbx);
        layoutHorizontal2.addComponent(debe2Txt);

        layoutHorizontal3.addComponent(cuentaContable3Cbx);
        layoutHorizontal3.addComponent(debe3Txt);

        layoutHorizontal4.addComponent(cuentaContable4Cbx);
        layoutHorizontal4.addComponent(debe4Txt);

        rightVerticalLayout.addComponent(haberLayout);
        rightVerticalLayout.setComponentAlignment(haberLayout, Alignment.MIDDLE_CENTER);

        rightVerticalLayout.addComponent(layoutHorizontal1);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal1, Alignment.MIDDLE_CENTER);

        rightVerticalLayout.addComponent(layoutHorizontal2);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal2, Alignment.MIDDLE_CENTER);

        rightVerticalLayout.addComponent(layoutHorizontal3);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal3, Alignment.MIDDLE_CENTER);

        rightVerticalLayout.addComponent(layoutHorizontal4);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal4, Alignment.MIDDLE_CENTER);

        HorizontalLayout layoutHorizontal5 = new HorizontalLayout();
        layoutHorizontal5.setSpacing(true);

        grabarBtn = new Button("Guardar");
        grabarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        grabarBtn.setIcon(FontAwesome.SAVE);
        grabarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                insertTablaFactura();
            }
        });

        cerrarLiquidacionBtn = new Button("Cerrar liquidación");
        cerrarLiquidacionBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        if (idLiquidacionEdit.isEmpty()) {
            cerrarLiquidacionBtn.setEnabled(false);
        } else {
            cerrarLiquidacionBtn.setEnabled(true);
        }
        cerrarLiquidacionBtn.setIcon(FontAwesome.CHECK);
        cerrarLiquidacionBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de CERRAR esta liquidación  ?",
                        "SI", "NO", new ConfirmDialog.Listener() {

                            public void onClose(ConfirmDialog dialog) {
                                if (dialog.isConfirmed()) {
                                    cerrarLiquidacion();
                                }
                            }
                        });
            }
        });

        layoutHorizontal5.addComponent(grabarBtn);
        layoutHorizontal5.setComponentAlignment(grabarBtn, Alignment.BOTTOM_CENTER);
        layoutHorizontal5.addComponent(cerrarLiquidacionBtn);
        layoutHorizontal5.setComponentAlignment(cerrarLiquidacionBtn, Alignment.BOTTOM_RIGHT);

        rightVerticalLayout.addComponent(layoutHorizontal5);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal5, Alignment.MIDDLE_CENTER);

    }

    private void llenarComboPorContabilizar() {
        porContabilizarCbx.removeAllItems();

        String queryString = " SELECT *";
        queryString += " FROM documentos_fel_sat ";
        queryString += " WHERE Estatus = 'ACTIVA' ";
        queryString += " AND IdEmpresa = " + empresaCbx.getValue();
        queryString += " AND Contabilizada = 'N'";
        queryString += " AND UPPER(Accion) = 'LIQUIDACIÓN'";
        if (idLiquidacionEdit.isEmpty()) {
//            queryString += " AND IdLiquidacion = " + idLiquidacionNuevo;
            queryString += " AND IdLiquidador  = " + liquidadorCbx.getValue();
        } else {
//            queryString += " AND IdLiquidacion = " + idLiquidacionEdit;
            queryString += " AND IdLiquidador  = " + idLiquidadorEdit;
        }

Logger.getLogger(this.getClass().getName()).log(Level.INFO, queryString);

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if(rsRecords.next()) {
/**
 IdEmpresa,FechaEmision, NumeroAutorizacion, TipoDTE, Serie, Numero, IdProveedor, NitProveedor, NombreProveedor,
 CodigoEstablecimiento, NombreEstablecimiento,IdReceptor, NombreReceptor, NitCertificador, NombreCertificador,
 Moneda, Monto, Estatus, IVA,IDP, TurismoHospedaje,  TurismoPasajes, TimbrePrensa, Bomberos, BebidasAlcoholicas,
 Tabaco, Cemento, BebidasNoAlcoholicas, TarifaPortuaria, FechaCertificacion, Costo
 **/
                do {
                    porContabilizarCbx.addItem(rsRecords.getString("Id"));
                    porContabilizarCbx.setItemCaption(rsRecords.getString("Id"), rsRecords.getString("NombreProveedor") + " " + rsRecords.getString("Serie") + " " + rsRecords.getString("Numero"));

                } while(rsRecords.next());

            }
        } catch (Exception ex1) {
            new Notification("Error al intentar leer registros de tabla documentos_fel_sat.",
                    ex1.getMessage(),
                    Notification.Type.ERROR_MESSAGE)
                    .show(Page.getCurrent());
            ex1.printStackTrace();
            try {
                ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().rollback();
                ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().setAutoCommit(true);
            } catch (java.sql.SQLException sqlE) {
                //
            }
        }

    }

    private void llenarFormularioPorContabilizar() {

        tipoDocumentoCbx.setEnabled(true);
//        centroCostoCbx.setEnabled(true);
        fechaDt.setEnabled(true);
        proveedorCbx.setEnabled(true);
        nitProveedorTxt.setEnabled(true);
        serieTxt.setEnabled(true);
        numeroTxt.setEnabled(true);
        montoTxt.setEnabled(true);
        monedaCbx.setEnabled(true);

        tipoDocumentoCbx.select("FACTURA");
        centroCostoCbx.select(null);
        serieTxt.setValue("");
        numeroTxt.setValue("");
        montoTxt.setValue(0.00);
        nitProveedorTxt.setValue("");
        proveedorCbx.select(null);

        if(porContabilizarCbx.getValue() == null) {
            return;
        }

        String queryString = " SELECT *";
        queryString += " FROM documentos_fel_sat ";
        queryString += " WHERE IdEmpresa = " + empresaCbx.getValue();
        queryString += " AND Id = " + porContabilizarCbx.getValue();

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if(rsRecords.next()) {
/**
 IdEmpresa,FechaEmision, NumeroAutorizacion, TipoDTE, Serie, Numero, IdProveedor, NitProveedor, NombreProveedor,
 CodigoEstablecimiento, NombreEstablecimiento,IdReceptor, NombreReceptor, NitCertificador, NombreCertificador,
 Moneda, Monto, Estatus, IVA,IDP, TurismoHospedaje,  TurismoPasajes, TimbrePrensa, Bomberos, BebidasAlcoholicas,
 Tabaco, Cemento, BebidasNoAlcoholicas, TarifaPortuaria, FechaCertificacion, Costo
 **/
                tipoDocumentoCbx.select(rsRecords.getString("TipoDte"));
                centroCostoCbx.select(rsRecords.getString("IdCentroCosto"));
                fechaDt.setValue(rsRecords.getDate("FechaEmision"));
                proveedorCbx.select(rsRecords.getString("IdProveedor"));
                serieTxt.setValue(rsRecords.getString("Serie"));
                numeroTxt.setValue(rsRecords.getString("Numero"));
                montoTxt.setValue(rsRecords.getDouble("Monto"));
                monedaCbx.select(rsRecords.getString("Moneda"));

                tipoDocumentoCbx.setEnabled(false);
                centroCostoCbx.setEnabled(false);
                fechaDt.setEnabled(false);
                proveedorCbx.setEnabled(false);
                nitProveedorTxt.setEnabled(false);
                serieTxt.setEnabled(false);
                numeroTxt.setEnabled(false);
                montoTxt.setEnabled(false);
                monedaCbx.setEnabled(false);

                debe1Txt.setValue(rsRecords.getDouble("Iva"));
                double otrosImpuestos = rsRecords.getDouble("IDP") + rsRecords.getDouble("TurismoHospedaje");
                otrosImpuestos += rsRecords.getDouble("TurismoPasajes") + rsRecords.getDouble("TimbrePrensa");
                otrosImpuestos += rsRecords.getDouble("Bomberos") + rsRecords.getDouble("TasaMunicipal");
                otrosImpuestos += rsRecords.getDouble("BebidasAlcoholicas");
                otrosImpuestos += rsRecords.getDouble("Tabaco")  + rsRecords.getDouble("Cemento");
                otrosImpuestos += rsRecords.getDouble("BebidasNoAlcoholicas")  + rsRecords.getDouble("TarifaPortuaria");

                cuentaContable1Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getCompras());
                debe1Txt.setValue(Utileria.numberFormatEntero.format(rsRecords.getDouble("Costo")));

                if(rsRecords.getDouble("IVA")  > 0.00) {
                    cuentaContable2Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getIvaPorCobrar());
                    debe2Txt.setValue(Utileria.numberFormatEntero.format(rsRecords.getDouble("IVA")));
                }

                if(otrosImpuestos  > 0.00) {
                    if(rsRecords.getDouble("IVA") == 0.00) {
                        cuentaContable2Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getOtrosArbitrios());
                        debe2Txt.setValue(Utileria.numberFormatEntero.format(otrosImpuestos));
                    }
                    else {
                        cuentaContable3Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getOtrosArbitrios());
                        debe3Txt.setValue(Utileria.numberFormatEntero.format(otrosImpuestos));
                    }
                }
            }
        } catch (Exception ex1) {
            new Notification("Error al intentar leer registros de tabla documentos_fel_sat.",
                    ex1.getMessage(),
                    Notification.Type.ERROR_MESSAGE)
                    .show(Page.getCurrent());
            ex1.printStackTrace();
            try {
                ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().rollback();
                ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().setAutoCommit(true);
            } catch (java.sql.SQLException sqlE) {
                //
            }
        }

    }

    public void llenarComboCentroCosto() {

        String queryString = " SELECT * FROM centro_costo";
        queryString += " WHERE IdProyecto = " + ((SopdiUI) mainUI).sessionInformation.getStrProjectId();
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

    public void comprobarCampos() {
        
        if (this.serieTxt.getValue().trim().isEmpty()) {
            Notification.show("Por favor ingrese la serie del documento.", Notification.Type.WARNING_MESSAGE);
            serieTxt.focus();
            return;
        }
        if (this.numeroTxt.getValue().trim().isEmpty()) {
            Notification.show("Por favor ingrese el número de documento.", Notification.Type.WARNING_MESSAGE);
            numeroTxt.focus();
            return;
        }

        if (proveedorCbx.isEmpty()) {
            Notification.show("Por favor ingrese el proveedor.", Notification.Type.WARNING_MESSAGE);
            proveedorCbx.focus();
            return;
        }
        if (nitProveedorTxt.isEmpty()) {
            Notification.show("Por favor ingrese el Nit del proveedor.", Notification.Type.WARNING_MESSAGE);
            nitProveedorTxt.focus();
            return;
        }
        if (monedaCbx.getValue() == null) {
            Notification.show("Por favor ingrese el tipo de moneda.", Notification.Type.WARNING_MESSAGE);
            monedaCbx.focus();
            return;
        }

        if (this.montoTxt.getDoubleValueDoNotThrow() == 0) {
            Notification.show("Por favor ingrese el monto de la factura.", Notification.Type.WARNING_MESSAGE);
            montoTxt.focus();
            return;
        }
        
        empresaCbx.setReadOnly(true);
        serieTxt.setReadOnly(true);
        numeroTxt.setReadOnly(true);
        nitProveedorTxt.setReadOnly(true);
        proveedorCbx.setReadOnly(true);
        fechaDt.setReadOnly(true);
        montoTxt.setReadOnly(true);
        monedaCbx.setReadOnly(true);
        tasaCambioTxt.setReadOnly(true);

    }

    public void nuevaliquidacion() {
        limpiarTodo();
        serieTxt.focus();
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

        try{
            if(esNuevo) {
                String queryString = "SELECT MAX(IdLiquidacion) = IdUltimaLiquidacion AS Igual ";
                queryString += "FROM contabilidad_partida cp ";
                queryString += "INNER JOIN contabilidad_empresa ce ON ce.IdEmpresa = cp.IdEmpresa ";
                queryString += "AND ce.IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();

                stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                rsRecords = stQuery.executeQuery(queryString);
                if (rsRecords.next()) {
                    if (!rsRecords.getBoolean("Igual")) {
                        Notification.show("El numero de liquidacion en base de datos tiene error!, Informar al encargado de sistemas", Notification.Type.WARNING_MESSAGE);
                        idLiquidacionTxt.focus();
                        return;
                    }
                }
            }

        }catch (Exception e){
            System.out.println("Error al buscar comparar las liquidaciones " + e);
            e.printStackTrace();
        }



        if (liquidadorCbx.getValue() == null) {
            Notification.show("Por favor elija a la persona liquidador.",  Notification.Type.ERROR_MESSAGE);
            liquidadorCbx.focus();
            return;
        }
        
        if ( ((SopdiUI) UI.getCurrent()).esMesCerrado(String.valueOf(empresaCbx.getValue()), Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()))) {
            Notification.show("La fecha del documento no puede ser de un mes ya cerrado contablemente, revise!", Notification.Type.ERROR_MESSAGE);
            fechaDt.focus();
            return;
        }

        if ( !((SopdiUI) UI.getCurrent()).esPrimerMesAbierto(String.valueOf(empresaCbx.getValue()), Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()))) {
            Notification.show("El mes abierto a operaciones es : " + ((SopdiUI) UI.getCurrent()).primerMesAbierto(String.valueOf(empresaCbx.getValue())), Notification.Type.WARNING_MESSAGE);
            fechaDt.focus();
            return;
        }
        if (!tipoDocumentoCbx.getValue().equals("FACTURA") && !tipoDocumentoCbx.getValue().equals("RECIBO CONTABLE") && !tipoDocumentoCbx.getValue().equals("FORMULARIO")) {
            Notification.show("Solamente puede ingresar FACTURA o RECIBO CONTABLE.",  Notification.Type.ERROR_MESSAGE);
            tipoDocumentoCbx.focus();
            return;
        }

        if (this.serieTxt.getValue().trim().isEmpty()) {
            Notification.show("Por favor ingrese la serie del documento.", Notification.Type.WARNING_MESSAGE);
            serieTxt.focus();
            return;
        }
        if (this.numeroTxt.getValue().trim().isEmpty()) {
            Notification.show("Por favor ingrese el número de documento.", Notification.Type.WARNING_MESSAGE);
            numeroTxt.focus();
            return;
        }
        if (proveedorCbx.isEmpty()) {
            Notification.show("Por favor ingrese el proveedor.", Notification.Type.WARNING_MESSAGE);
            proveedorCbx.focus();
            return;
        }
        if (nitProveedorTxt.isEmpty()) {
            Notification.show("Por favor ingrese el Nit del proveedor.", Notification.Type.WARNING_MESSAGE);
            nitProveedorTxt.focus();
            return;
        }
        if (monedaCbx.getValue() == null) {
            Notification.show("Por favor ingrese el tipo de moneda.", Notification.Type.WARNING_MESSAGE);
            monedaCbx.focus();
            return;
        }
        if (this.montoTxt.getDoubleValueDoNotThrow() == 0) {
            Notification.show("Por favor ingrese el monto de la factura.", Notification.Type.WARNING_MESSAGE);
            montoTxt.focus();
            return;

        }
        
        /// validar montos antes de ingresar cualquier registro
        totalDebe = new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP);
        totalDebe.setScale(2, BigDecimal.ROUND_HALF_UP);        
        totalDebe = totalDebe.add(new BigDecimal(debe1Txt.getDoubleValueDoNotThrow())).setScale(2, BigDecimal.ROUND_HALF_UP);
        totalDebe = totalDebe.add(new BigDecimal(debe2Txt.getDoubleValueDoNotThrow())).setScale(2, BigDecimal.ROUND_HALF_UP);
        totalDebe = totalDebe.add(new BigDecimal(debe3Txt.getDoubleValueDoNotThrow())).setScale(2, BigDecimal.ROUND_HALF_UP);
        totalDebe = totalDebe.add(new BigDecimal(debe4Txt.getDoubleValueDoNotThrow())).setScale(2, BigDecimal.ROUND_HALF_UP);

        System.out.println("debe redondeado = " + totalDebe.round(MathContext.DECIMAL32));
        
        if (totalDebe.doubleValue() != montoTxt.getDoubleValueDoNotThrow()) {
            Notification notif = new Notification("EL MONTO DEL DEBE Y EL HABER NO COINCIDEN!. MONTO DEL DEBE : " + totalDebe.doubleValue() + " MONTO DEL HABER : " + montoTxt.getDoubleValueDoNotThrow(),
                    Notification.Type.ERROR_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.WARNING);
            notif.show(Page.getCurrent());
            return;
        }
        
        String queryString = " Select * from contabilidad_partida";
        queryString += " Where SerieDocumento  = '" + serieTxt.getValue().toUpperCase().trim() + "'";
        queryString += " And   NumeroDocumento = '" + numeroTxt.getValue().toUpperCase().trim() + "'";
        //queryString += " And   IdProveedor     =  " + String.valueOf(proveedorCbx.getValue());
        queryString += " And IdEmpresa = " + String.valueOf(empresaCbx.getValue());
        queryString += " And TipoDocumento = '" + String.valueOf(tipoDocumentoCbx.getValue()) + "'";
        queryString += " And MonedaDocumento = '" + monedaCbx.getValue() + "'";

System.out.println("\n\nQuery=" + queryString + "\n\n");

        try {
            rsRecords = stQuery.executeQuery(queryString);
            
            if(rsRecords.next()) {
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

        String codigoPartida = String.valueOf(empresaCbx.getValue()) + año + mes + dia + "2";

        queryString  = " select codigoPartida from contabilidad_partida ";
        queryString += " where codigoPartida like '" + codigoPartida + "%'";
        queryString += " order by codigoPartida desc ";

        try {
            stQuery2 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords2 = stQuery2.executeQuery(queryString);

            if (rsRecords2.next()) { //  encontrado                               

                ultimoEncontado = rsRecords2.getString("codigoPartida").substring(12, 15);

                System.out.println("ultimo encontrado " + ultimoEncontado);

                codigoPartida += String.format("%03d", (Integer.valueOf(ultimoEncontado) + 1));

            } else {
                codigoPartida += "001";
            }

        } catch (Exception ex1) {
            System.out.println("Error al buscar el ultimo codigoPartida" + ex1.getMessage());
            ex1.printStackTrace();
        }

        /// ingreso del haber
        queryString  = " Insert Into contabilidad_partida (IdEmpresa, Estatus, CodigoPartida, CodigoCC,";
        queryString += " TipoDocumento, Fecha, NITProveedor, IdProveedor, NombreProveedor,";
        queryString += " SerieDocumento, NumeroDocumento, IdNomenclatura, MonedaDocumento, Debe, Haber, ";
        queryString += " DebeQuetzales, HaberQuetzales, TipoCambio, Saldo, IdLiquidador, IdLiquidacion, ";
        queryString += " Descripcion, IdCentroCosto, CodigoCentroCosto, CreadoUsuario, CreadoFechaYHora)";
        queryString += " Values ";
        queryString += " (";
        queryString += empresaCbx.getValue();
        queryString += ",'INGRESADO'";
        queryString += ",'" + codigoPartida + "'";
        queryString += ",'" + codigoCC + "'";
        queryString += ",'" + String.valueOf(tipoDocumentoCbx.getValue())+ "'";
        queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
        queryString += ",'" + nitProveedorTxt.getValue().replaceAll("'", "") + "'";
        try {
            Integer.parseInt(String.valueOf(proveedorCbx.getValue()));
            queryString += ", " + proveedorCbx.getValue(); 
            queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
        }
        catch(Exception ex) {
            queryString += ",0";             
            queryString += ",'" + proveedorCbx.getValue() + "'";
        }
        queryString += ",UPPER('" + serieTxt.getValue().trim() + "')";
        queryString += ",'" + numeroTxt.getValue().trim() + "'";
        queryString += "," + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getLiquidacionesCajaChicha();
        queryString += ",'" + monedaCbx.getValue() + "'";
        queryString += ",0.00"; // DEBE
        queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow()); //HABER
        queryString += ",0.00"; //DEBE Q
        queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
        queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
        queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow()); // SALDO        
        if (idLiquidacionEdit.isEmpty()) {
            queryString += "," + String.valueOf(liquidadorCbx.getValue());
            queryString += "," + idLiquidacionNuevo;
        } else {
            queryString += "," + idLiquidadorEdit;
            queryString += "," + idLiquidacionEdit;
        }
        queryString += ",'LIQUIDACION GASTO " + (idLiquidacionEdit.isEmpty() ? idLiquidacionNuevo : idLiquidacionEdit) + " " + liquidadorCbx.getItemCaption(liquidadorCbx.getValue()) + "'";
        queryString += "," + centroCostoCbx.getValue();
        queryString += ",'" + centroCostoCbx.getItemCaption(centroCostoCbx.getValue()) + "'";
        queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
        queryString += ",current_timestamp";
        queryString += ")";

//// primer ingreso
        if (cuentaContable1Cbx.getValue() != null && debe1Txt.getDoubleValueDoNotThrow() != 0) {
            queryString += ",(";
            queryString += empresaCbx.getValue();
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + String.valueOf(tipoDocumentoCbx.getValue()) + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += ",'" + nitProveedorTxt.getValue().replaceAll("'", "") + "'";
            try {
                Integer.parseInt(String.valueOf(proveedorCbx.getValue()));
                queryString += ", " + proveedorCbx.getValue(); 
                queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            }
            catch(Exception ex) {
                queryString += ",0";             
                queryString += ",'" + proveedorCbx.getValue() + "'";
            }
            queryString += ",UPPER('" + serieTxt.getValue().trim() + "')";
            queryString += ",'" + numeroTxt.getValue().trim() + "'";
            queryString += "," + cuentaContable1Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(debe1Txt.getDoubleValueDoNotThrow()); //DEBE
            queryString += ",0.00"; // HABER
            queryString += "," + String.valueOf(debe1Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00"; //HABER Q
            queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00";
            if (idLiquidacionEdit.isEmpty()) {
                queryString += "," + String.valueOf(liquidadorCbx.getValue());
                queryString += "," + idLiquidacionNuevo;
            } else {
                queryString += ","  + idLiquidadorEdit;
                queryString += ","  + idLiquidacionEdit;
            }
            queryString += ",'LIQUIDACION GASTO " + (idLiquidacionEdit.isEmpty() ? idLiquidacionNuevo : idLiquidacionEdit) + " " + liquidadorCbx.getItemCaption(liquidadorCbx.getValue()) + "'";
            queryString += "," + centroCostoCbx.getValue();
            queryString += ",'" + centroCostoCbx.getItemCaption(centroCostoCbx.getValue()) + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";
        }
       // 

//// segundo  ingreso
        if (cuentaContable2Cbx.getValue() != null && debe2Txt.getDoubleValueDoNotThrow() != 0) {
            queryString += ",(";
            queryString += empresaCbx.getValue();
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + String.valueOf(tipoDocumentoCbx.getValue()) + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += ",'" + nitProveedorTxt.getValue().replaceAll("'", "") + "'";
            try {
                Integer.parseInt(String.valueOf(proveedorCbx.getValue()));
                queryString += ", " + proveedorCbx.getValue(); 
                queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            }
            catch(Exception ex) {
                queryString += ",0";             
                queryString += ",'" + proveedorCbx.getValue() + "'";
            }
            queryString += ",UPPER('" + serieTxt.getValue().trim() + "')";
            queryString += ",'" + numeroTxt.getValue().trim() + "'";
            queryString += "," + cuentaContable2Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(debe2Txt.getDoubleValueDoNotThrow()); //DEBE
            queryString += ",0.00"; // HABER
            queryString += "," + String.valueOf(debe2Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00"; //HABER Q
            queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00";
            if (idLiquidacionEdit.isEmpty()) {
                queryString += "," + String.valueOf(liquidadorCbx.getValue());
                queryString += "," + idLiquidacionNuevo;

            } else {
                queryString += "," + idLiquidadorEdit;
                queryString += "," + idLiquidacionEdit;

            }
            queryString += ",'LIQUIDACION GASTO " + (idLiquidacionEdit.isEmpty() ? idLiquidacionNuevo : idLiquidacionEdit) + " " + liquidadorCbx.getItemCaption(liquidadorCbx.getValue()) + "'";
            queryString += "," + centroCostoCbx.getValue();
            queryString += ",'" + centroCostoCbx.getItemCaption(centroCostoCbx.getValue()) + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";
        }
        //totalDebe = totalDebe.add(new BigDecimal(debe2Txt.getDoubleValueDoNotThrow())).setScale(2, BigDecimal.ROUND_HALF_UP);

//// tercer ingreso
        if (cuentaContable3Cbx.getValue() != null && debe3Txt.getDoubleValueDoNotThrow() != 0) {
            queryString += ",(";
            queryString += empresaCbx.getValue();
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + String.valueOf(tipoDocumentoCbx.getValue()) + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += ",'" + nitProveedorTxt.getValue().replaceAll("'", "") + "'";
            try {
                Integer.parseInt(String.valueOf(proveedorCbx.getValue()));
                queryString += ", " + proveedorCbx.getValue(); 
                queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            }
            catch(Exception ex) {
                queryString += ",0";             
                queryString += ",'" + proveedorCbx.getValue() + "'";
            }
            queryString += ",UPPER('" + serieTxt.getValue().trim() + "')";
            queryString += ",'" + numeroTxt.getValue().trim() + "'";
            queryString += "," + cuentaContable3Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(debe3Txt.getDoubleValueDoNotThrow()); //DEBE
            queryString += ",0.00"; // HABER
            queryString += "," + String.valueOf(debe3Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00"; //HABER Q
            queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00";
            if (idLiquidacionEdit.isEmpty()) {
                queryString += "," + String.valueOf(liquidadorCbx.getValue());
                queryString += "," + idLiquidacionNuevo;

            } else {
                queryString += "," + idLiquidadorEdit;
                queryString += "," + idLiquidacionEdit;

            }
            queryString += ",'LIQUIDACION GASTO " + (idLiquidacionEdit.isEmpty() ? idLiquidacionNuevo : idLiquidacionEdit) + " " + liquidadorCbx.getItemCaption(liquidadorCbx.getValue()) + "'";
            queryString += "," + centroCostoCbx.getValue();
            queryString += ",'" + centroCostoCbx.getItemCaption(centroCostoCbx.getValue()) + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";
        }
        //totalDebe = totalDebe.add(new BigDecimal(debe3Txt.getDoubleValueDoNotThrow())).setScale(2, BigDecimal.ROUND_HALF_UP);

//// cuarto ingreso
        if (cuentaContable4Cbx.getValue() != null && debe4Txt.getDoubleValueDoNotThrow() != 0) {
            queryString += ",(";
            queryString += empresaCbx.getValue();
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + String.valueOf(tipoDocumentoCbx.getValue()) + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += ",'" + nitProveedorTxt.getValue().replaceAll("'", "") + "'";
            try {
                Integer.parseInt(String.valueOf(proveedorCbx.getValue()));
                queryString += ", " + proveedorCbx.getValue(); 
                queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            }
            catch(Exception ex) {
                queryString += ",0";             
                queryString += ",'" + proveedorCbx.getValue() + "'";
            }
            queryString += ",UPPER('" + serieTxt.getValue().trim() + "')";
            queryString += ",'" + numeroTxt.getValue().trim() + "'";
            queryString += "," + cuentaContable4Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(debe4Txt.getDoubleValueDoNotThrow()); //DEBE
            queryString += ",0.00"; // HABER
            queryString += "," + String.valueOf(debe4Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00"; //HABER Q
            queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00";
            if (idLiquidacionEdit.isEmpty()) {
                queryString += "," + String.valueOf(liquidadorCbx.getValue());
                queryString += "," + idLiquidacionNuevo;

            } else {
                queryString += "," + idLiquidadorEdit;
                queryString += "," + idLiquidacionEdit;
            }
            queryString += ",'LIQUIDACION GASTO " + (idLiquidacionEdit.isEmpty() ? idLiquidacionNuevo : idLiquidacionEdit) + " " + liquidadorCbx.getItemCaption(liquidadorCbx.getValue()) + "'";
            queryString += "," + centroCostoCbx.getValue();
            queryString += ",'" + centroCostoCbx.getItemCaption(centroCostoCbx.getValue()) + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";
        }

       // totalDebe = totalDebe.add(new BigDecimal(debe4Txt.getDoubleValueDoNotThrow())).setScale(2, BigDecimal.ROUND_HALF_UP);       

        if (cuentaContable1Cbx.getValue() != null) { // hay una cuenta seleccioada

            if (cuentaContable2Cbx.getValue() != null) { // hay una cuenta seleccioada

                if (cuentaContable2Cbx.getValue().equals(cuentaContable1Cbx.getValue())) {
                    Notification.show("No puede utilizar la misma cuenta para dos registros de la partida.", Notification.Type.ERROR_MESSAGE);
                    return;
                }

                if (cuentaContable3Cbx.getValue() != null) {
                    if (cuentaContable1Cbx.getValue().equals(cuentaContable3Cbx.getValue())
                            || cuentaContable2Cbx.getValue().equals(cuentaContable3Cbx.getValue())) {
                        Notification.show("No puede utilizar la misma cuenta para dos registros de la partida.", Notification.Type.ERROR_MESSAGE);
                        return;
                    }

                    if (cuentaContable4Cbx.getValue() != null) {
                        if (cuentaContable4Cbx.getValue().equals(cuentaContable1Cbx.getValue())
                                || cuentaContable4Cbx.getValue().equals(cuentaContable3Cbx.getValue())
                                || cuentaContable4Cbx.getValue().equals(cuentaContable2Cbx.getValue())) {
                            return;
                        }
                    }
                }

                if (cuentaContable4Cbx.getValue() != null) {
                    if (cuentaContable4Cbx.getValue().equals(cuentaContable1Cbx.getValue())
                            || cuentaContable4Cbx.getValue().equals(cuentaContable3Cbx.getValue())
                            || cuentaContable4Cbx.getValue().equals(cuentaContable2Cbx.getValue())) {
                        return;
                    }
                }
            }
        }
        if (cuentaContable2Cbx.getValue() != null) { // hay una cuenta seleccioada

            if (cuentaContable1Cbx.getValue() != null) { // hay una cuenta seleccioada
                if (cuentaContable2Cbx.getValue().equals(cuentaContable1Cbx.getValue())) {
                    Notification.show("No puede utilizar la misma cuenta para dos registros de la partida.", Notification.Type.ERROR_MESSAGE);
                    return;
                }
            }
            if (cuentaContable3Cbx.getValue() != null) { // hay una cuenta seleccioada
                if (cuentaContable2Cbx.getValue().equals(cuentaContable3Cbx.getValue())) {
                    Notification.show("No puede utilizar la misma cuenta para dos registros de la partida.", Notification.Type.ERROR_MESSAGE);
                    return;
                }
            }
            if (cuentaContable4Cbx.getValue() != null) { // hay una cuenta seleccioada
                if (cuentaContable2Cbx.getValue().equals(cuentaContable4Cbx.getValue())) {
                    Notification.show("No puede utilizar la misma cuenta para dos registros de la partida.", Notification.Type.ERROR_MESSAGE);
                    return;
                }
            }
        }
        if (cuentaContable3Cbx.getValue() != null) { // hay una cuenta seleccioada

            if (cuentaContable1Cbx.getValue() != null) { // hay una cuenta seleccioada
                if (cuentaContable3Cbx.getValue().equals(cuentaContable1Cbx.getValue())) {
                    Notification.show("No puede utilizar la misma cuenta para dos registros de la partida.", Notification.Type.ERROR_MESSAGE);
                    return;
                }
            }
            if (cuentaContable2Cbx.getValue() != null) { // hay una cuenta seleccioada
                if (cuentaContable2Cbx.getValue().equals(cuentaContable3Cbx.getValue())) {
                    Notification.show("No puede utilizar la misma cuenta para dos registros de la partida.", Notification.Type.ERROR_MESSAGE);
                    return;
                }
            }
            if (cuentaContable4Cbx.getValue() != null) { // hay una cuenta seleccioada
                if (cuentaContable4Cbx.getValue().equals(cuentaContable3Cbx.getValue())) {
                    Notification.show("No puede utilizar la misma cuenta para dos registros de la partida.", Notification.Type.ERROR_MESSAGE);
                    return;
                }
            }
        }
        if (cuentaContable4Cbx.getValue() != null) { // hay una cuenta seleccioada

            if (cuentaContable1Cbx.getValue() != null) { // hay una cuenta seleccioada
                if (cuentaContable1Cbx.getValue().equals(cuentaContable4Cbx.getValue())) {
                    Notification.show("No puede utilizar la misma cuenta para dos registros de la partida.", Notification.Type.ERROR_MESSAGE);
                    return;
                }
            }
            if (cuentaContable2Cbx.getValue() != null) { // hay una cuenta seleccioada
                if (cuentaContable2Cbx.getValue().equals(cuentaContable4Cbx.getValue())) {
                    Notification.show("No puede utilizar la misma cuenta para dos registros de la partida.", Notification.Type.ERROR_MESSAGE);
                    return;
                }
            }
            if (cuentaContable3Cbx.getValue() != null) { // hay una cuenta seleccioada
                if (cuentaContable3Cbx.getValue().equals(cuentaContable4Cbx.getValue())) {
                    Notification.show("No puede utilizar la misma cuenta para dos registros de la partida.", Notification.Type.ERROR_MESSAGE);
                    return;
                }
            }
        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, queryString);

        try {
            
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            if(porContabilizarCbx.getValue() != null) {
                queryString  = " UPDATE documentos_fel_sat ";
                queryString += " SET Contabilizada = 'S'";
                if (idLiquidacionEdit.isEmpty()) {
                    queryString += ", IdLiquidacion = " + idLiquidacionNuevo;
                } else {
                    queryString += ", IdLiquidacion = " + idLiquidacionEdit;
                }
                queryString += " WHERE IdEmpresa = " + empresaCbx.getValue();
                queryString += " AND Id = " + porContabilizarCbx.getValue();

                stQuery.executeUpdate(queryString);
            }
            if(!variableTemp.isEmpty()){
                cambiarEstatusToken(codigoPartida);
            }

            if (idLiquidadorEdit.trim().isEmpty()) { // es una liquidacion nueva
                queryString = "Update contabilidad_empresa";
                queryString += " set IdUltimaLiquidacion = " + idLiquidacionNuevo;
                queryString += " where IdEmpresa = " + String.valueOf(empresaCbx.getValue());

                stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                stQuery.executeUpdate(queryString);
            }

            Notification.show("Registro agregado con exito!", Notification.Type.HUMANIZED_MESSAGE);

            llenarTablaLiquidacion();

            ((IngresoLiquidacionGastoView) (mainUI.getNavigator().getCurrentView())).llenarTablaLiquidacion(String.valueOf(empresaCbx.getValue()));

            limpiarTodo();

            serieTxt.focus();

            cerrarLiquidacionBtn.setEnabled(true);

        } catch (Exception ex1) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "ERROR AL INSERTAR DOCUMENTO", ex1);
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

    public void llenarTablaLiquidacion() {
        container.removeAllItems();

        String queryString = " select *";
        queryString += " from contabilidad_partida";
        if (idLiquidacionEdit.isEmpty()) {
            queryString += " Where IdLiquidador = " + String.valueOf(liquidadorCbx.getValue());
            queryString += " and IdLiquidacion = " + idLiquidacionNuevo;
            queryString += " and IdEmpresa = " + String.valueOf(empresaCbx.getValue());
        } else {
            queryString += " Where IdLiquidador = " + idLiquidadorEdit;
            queryString += " and IdLiquidacion = " + idLiquidacionEdit;
            queryString += " and IdEmpresa = " + idEmpresaEdit;
        }
        queryString += " and IdNomenclatura = " + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getLiquidacionesCajaChicha();

//        queryString += " group by IdLiquidacion,IdEmpresa,IdLiquidador,NumeroDocumento,SerieDocumento,NombreProveedor,NITProveedor";

System.out.println("query liquidacion "+ queryString);

        try {
            stQuery1 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords1 = stQuery1.executeQuery(queryString);

            if (rsRecords1.next()) { //  encontrado

                empresaCbx.select(rsRecords1.getString("IdEmpresa"));
                idLiquidacionTxt.setValue(rsRecords1.getString("IdLiquidacion"));
                liquidadorCbx.select(rsRecords1.getString("IdLiquidador"));

                idLiquidacionTxt.setReadOnly(true);
                empresaCbx.setReadOnly(true);
                liquidadorCbx.setReadOnly(true);
                monedaCbx.setReadOnly(false);
                monedaCbx.select(rsRecords1.getString("MonedaDocumento"));
                monedaCbx.setReadOnly(true);
                if(rsRecords1.getString("MonedaDocumento").equals("QUETZALES")) {
                    tasaCambioTxt.setReadOnly(false);
                    tasaCambioTxt.setValue(1.0);
                    tasaCambioTxt.setReadOnly(true);
                }
                else {
                    tasaCambioTxt.setReadOnly(false);
                    tasaCambioTxt.setValue(1.0);
                }

                do {
                    Object itemId = container.addItem();
                    container.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords1.getString("IdPartida"));
                    container.getContainerProperty(itemId, CODIGO_PARTIDA_PROPERTY).setValue(rsRecords1.getString("CodigoPartida"));
                    container.getContainerProperty(itemId, SERIE_PROPERTY).setValue(rsRecords1.getString("SerieDocumento"));
                    container.getContainerProperty(itemId, NUMERO_PROPERTY).setValue(rsRecords1.getString("NumeroDocumento"));
                    container.getContainerProperty(itemId, PROVEEDOR_PROPERTY).setValue(rsRecords1.getString("NombreProveedor"));
                    container.getContainerProperty(itemId, FECHA_PROPERTY).setValue(rsRecords1.getString("Fecha"));
                    container.getContainerProperty(itemId, MONTO_PROPERTY).setValue(numberFormat.format(rsRecords1.getDouble("Haber")));
                    container.getContainerProperty(itemId, MONEDA_PROPERTY).setValue(rsRecords1.getString("MonedaDocumento"));
                    container.getContainerProperty(itemId, TIPOCAMBIO_PROPERTY).setValue(rsRecords1.getString("TipoCambio"));
                    container.getContainerProperty(itemId, ELIMINAR_PROPERTY).setValue("Eliminar");
                } while (rsRecords1.next());
                idLiquidacionTxt.setReadOnly(true);
                //nombreLiquidadorTxt.setReadOnly(true);
            }
        } catch (Exception ex) {
            System.out.println("Error al listar tabla liquidaciones:" + ex);
            ex.printStackTrace();
        }
        footer.getCell(PROVEEDOR_PROPERTY).setText(container.size() + " facturas");
    }

    public void llenarComboEmpresa() {

        String queryString = "";
        queryString += " select * from contabilidad_empresa";
        queryString += " Where IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) { //  encontrado                
                empresaCbx.addItem(rsRecords.getString("IdEmpresa"));
                empresaCbx.setItemCaption(rsRecords.getString("IdEmpresa"), rsRecords.getString("Empresa"));
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar empresas : " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void buscarProveedorPorNit() {
//        for (Iterator<?> i = proveedorCbx.getItemIds().iterator(); i.hasNext();) {
//            String id = (String) i.next();
//            Item item = proveedorCbx.getItem(id);
//
//            if (nitProveedorTxt.getValue().equals(String.valueOf(item.getItemProperty(NIT_PROPERTY).getValue()).trim())) {
//                proveedorCbx.select(id);
//                break;
//            }
//        }
    }

    public void llenarComboCuentaContable() {
        String queryString = "";
        queryString += " SELECT * from contabilidad_nomenclatura ";
        queryString += " where Estatus = 'HABILITADA' ";
//        queryString += " And FiltrarIngresoDocumentos = 'S'";
        queryString += " Order By N5";

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

            }

        } catch (Exception ex1) {
            System.out.println("Error al listar cuentas contables: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void cerrarLiquidacion() {
 //       System.out.println("Mostar idLiquidador nuevo" + liquidadorCbx.getValue());
        String queryString = "";
        queryString = " UPDATE contabilidad_partida";
        queryString += " SET Estatus = 'CERRADO'";

        if (idLiquidacionEdit.isEmpty()) {
            queryString += " where IdLiquidador = " + liquidadorCbx.getValue();
            queryString += " and  IdLiquidacion = " + idLiquidacionNuevo;
            queryString += " and  IdEmpresa = " + String.valueOf(empresaCbx.getValue());
        } else {
            queryString += " where IdLiquidador = " + idLiquidadorEdit;
            queryString += " and  IdEmpresa = " + idEmpresaEdit;
            queryString += " and  IdLiquidacion = " + idLiquidacionEdit;
        }

        System.out.println("Query actualizar" + queryString);
        
        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            ((IngresoLiquidacionGastoView) (mainUI.getNavigator().getCurrentView())).llenarTablaLiquidacion(String.valueOf(empresaCbx.getValue()));
            close();
            Notification.show("Factura cerrada con exito", Notification.Type.HUMANIZED_MESSAGE);
        } catch (Exception ex) {
            System.out.println("Error al intentar Modificar Estatus" + ex);
        }

    }

    public void limpiarTodo() {

        serieTxt.setReadOnly(false);
        numeroTxt.setReadOnly(false);
        nitProveedorTxt.setReadOnly(false);
//        monedaCbx.setReadOnly(false);
        fechaDt.setReadOnly(false);
        montoTxt.setReadOnly(false);
//        tasaCambioTxt.setReadOnly(false);

        serieTxt.setValue("");
        numeroTxt.setValue("");
        nitProveedorTxt.setValue("");
        montoTxt.setValue(0.00);
        cuentaContable1Cbx.clear();
        cuentaContable2Cbx.clear();
        cuentaContable3Cbx.clear();
        cuentaContable4Cbx.clear();

        debe1Txt.setValue(0.00);
        debe2Txt.setValue(0.00);
        debe3Txt.setValue(0.00);
        debe4Txt.setValue(0.00);

        proveedorCbx.unselect(proveedorCbx.getValue());

    }

    public void llenarTxtLiquidacion(String idEmpresa) {
        
        idLiquidacionTxt.setReadOnly(false);
        
        String queryString = "Select *";
        queryString += " From  contabilidad_empresa";
        queryString += " Where IdEmpresa = " + idEmpresa;
        
        try {

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                idLiquidacionTxt.setValue(String.valueOf(rsRecords.getInt("IdUltimaLiquidacion") + 1));
                idLiquidacionNuevo = idLiquidacionTxt.getValue();
                idLiquidacionTxt.setReadOnly(true);
            } else {
                idLiquidacionTxt.setValue("");
                idLiquidacionNuevo = "0";
            }
            
            String fecha = Utileria.getFechaYYYYMMDD_1(new java.util.Date());
            String ultimoEncontado;
            String dia = fecha.substring(8, 10);
            String mes = fecha.substring(5, 7);
            String año = fecha.substring(0, 4);

            codigoCC = String.valueOf(empresaCbx.getValue()) + año + mes + dia + "9";

            queryString  = " select codigoCC from contabilidad_partida ";
            queryString += " where codigoCC like '" + codigoCC + "%'";
            queryString += " order by codigoCC desc ";

            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado                               

                ultimoEncontado = rsRecords.getString("codigoCC").substring(12, 15);

                System.out.println("ultimo encontrado " + ultimoEncontado);

                codigoCC += String.format("%03d", (Integer.valueOf(ultimoEncontado) + 1));

            } else {
                codigoCC += "001";
            }           

        } catch (Exception ex) {
            System.out.println("Error al llenar txt Liquidacion " + ex);
            ex.printStackTrace();
        }

    }

    public void llenarComboLiquidador() {

        String queryString  = " SELECT * from proveedor ";
        queryString += " WHERE EsLiquidador = 1";
        queryString += " AND Inhabilitado = 0";
        queryString += " Order By Nombre ";

        try {

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) {
                liquidadorCbx.addItem(rsRecords.getString("IdProveedor"));
                liquidadorCbx.setItemCaption(rsRecords.getString("IdProveedor"), rsRecords.getString("Nombre"));
            }

        } catch (Exception ex) {
            System.out.println("Error al llenar combo de liquidadores" + ex);
            ex.printStackTrace();
        }

    }

    public void llenarCamposFactura(String serie, String numero, String proveedor, String fecha, String codigoPartida) {
        cuentaContable1Cbx.clear();
        cuentaContable2Cbx.clear();
        cuentaContable3Cbx.clear();
        cuentaContable4Cbx.clear();

        debe1Txt.setValue(0.00);
        debe2Txt.setValue(0.00);
        debe3Txt.setValue(0.00);
        debe4Txt.setValue(0.00);

        String queryString = " Select *";
        queryString += " From  contabilidad_partida";
        //      queryString += " where Haber =0.00";
        if (idLiquidadorEdit.isEmpty()) {
            queryString += " where IdLiquidacion = " + idLiquidacionTxt.getValue();
            queryString += " and IdLiquidador = " + liquidadorCbx.getValue();
        } else {
            queryString += " where IdLiquidacion = " + idLiquidacionTxt.getValue();
            queryString += " and IdLiquidador = " + idLiquidadorEdit;
        }
        queryString += " and IdEmpresa = " + empresaCbx.getValue();
        queryString += " and CodigoPartida = '" + codigoPartida + "'";
//        queryString += " and SerieDocumento = '" + serie + "'";
//        queryString += " and NumeroDocumento = '" + numero + "'";
//        queryString += " and NombreProveedor = '" + proveedor + "'";
//        queryString += " and Fecha = '" + fecha + "'";
        
System.out.println("query liquidacion = " + queryString);

        try {

            stQuery1 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords1 = stQuery1.executeQuery(queryString);

            int contador = 0;
            while (rsRecords1.next()) { //  encontrado                

                if(rsRecords1.getString("IdNomenclatura").equals(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getLiquidacionesCajaChicha())) {
                    montoTxt.setValue(rsRecords1.getDouble("Haber"));
                }

                if (contador == 0) {

                    empresaCbx.select(rsRecords1.getString("IdEmpresa"));
                    serieTxt.setValue(rsRecords1.getString("SerieDocumento"));
                    numeroTxt.setValue(rsRecords1.getString("NumeroDocumento"));
                    nitProveedorTxt.setValue(rsRecords1.getString("NITProveedor"));
                    
                    if(rsRecords1.getString("IdProveedor").equals("0") || rsRecords1.getObject("IdProveedor") == null) {
                        proveedorCbx.select(0);
                        proveedorCbx.setValue(rsRecords1.getString("NombreProveedor"));
                    }
                    else {
                        proveedorCbx.select(rsRecords1.getString("IdProveedor"));
                    }
                    fechaDt.setValue(rsRecords1.getDate("Fecha"));
                    monedaCbx.setValue(rsRecords1.getString("MonedaDocumento"));
                    tasaCambioTxt.setValue(rsRecords1.getDouble("TipoCambio"));
                    cuentaContable1Cbx.select(rsRecords1.getString("IdNomenclatura"));
                    debe1Txt.setValue(rsRecords1.getString("Debe"));

                }
                if (contador == 1) {
                    cuentaContable2Cbx.select(rsRecords1.getString("IdNomenclatura"));
                    debe2Txt.setValue(rsRecords1.getString("Debe"));
                }
                if (contador == 2) {
                    cuentaContable3Cbx.select(rsRecords1.getString("IdNomenclatura"));
                    debe3Txt.setValue(rsRecords1.getString("Debe"));
                }
                if (contador == 3) {
                    cuentaContable4Cbx.select(rsRecords1.getString("IdNomenclatura"));
                    debe4Txt.setValue(rsRecords1.getString("Debe"));
                }

//                if (contador == 4) {
//                    cuentaContable5Cbx.select(rsRecords1.getString("IdNomenclatura"));
//                    debe5Txt.setValue(rsRecords1.getString("Debe"));
//                }

                contador = contador + 1;
            }

        } catch (Exception ex) {
            System.out.println("Error en llenar campos factura" + ex);
            ex.printStackTrace();
        }

    }

    public void eliminarRegistroTabla(RendererClickEvent e) {

        facturaGastoGrid.select(e.getItemId());

        ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de Eliminar la factura ?",
                "SI", "NO", new ConfirmDialog.Listener() {

            public void onClose(ConfirmDialog dialog) {
                if (dialog.isConfirmed()) {

                    String queryString = "";
                    queryString += "DELETE FROM contabilidad_partida";
                    queryString += " WHERE NumeroDocumento = '" + String.valueOf(container.getContainerProperty(e.getItemId(), NUMERO_PROPERTY).getValue()) + "'";
                    queryString += " AND SerieDocumento = '" + String.valueOf(container.getContainerProperty(e.getItemId(), SERIE_PROPERTY).getValue()) + "'";
                    queryString += " AND CodigoPartida = '" + String.valueOf(container.getContainerProperty(e.getItemId(), CODIGO_PARTIDA_PROPERTY).getValue()) + "'";
                    queryString += " AND IdEmpresa = " + empresaCbx.getValue();

                    if (liquidadorCbx.getValue() == null) {
                        queryString += " AND IdLiquidador = " + idLiquidadorEdit;
                    } else {
                        queryString += " AND IdLiquidador = " + liquidadorCbx.getValue();
                    }

                    queryString += " AND IdLiquidacion = " + idLiquidacionTxt.getValue();
                    queryString += " AND NombreProveedor ='" + String.valueOf(container.getContainerProperty(e.getItemId(), PROVEEDOR_PROPERTY).getValue()) + "'";
                    
System.out.println("query delete liquidacion : " + queryString);

                    try {
                        stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                        stQuery.executeUpdate(queryString);
                        
                        //TODO :
                        //      1) si es la unica o ultima factura, entonces...
                        //      2) descontar el correlativo de liquidaciones...
                        //      3) cerrar esta ventana...
                        //      4) marcar como no contabilizada en tabla fel_sat

                        //4)
                        queryString = "UPDATE documentos_fel_sat SET ";
                        queryString += " Contabilizada = 'N'";
                        queryString += " WHERE NumeroDocumento = '" + String.valueOf(container.getContainerProperty(e.getItemId(), NUMERO_PROPERTY).getValue()) + "'";
                        queryString += " AND SerieDocumento = '" + String.valueOf(container.getContainerProperty(e.getItemId(), SERIE_PROPERTY).getValue()) + "'";
                        if (liquidadorCbx.getValue() == null) {
                            queryString += " AND IdLiquidador = " + idLiquidadorEdit;
                        } else {
                            queryString += " AND IdLiquidador = " + liquidadorCbx.getValue();
                        }

                        stQuery.executeUpdate(queryString);

                        llenarTablaLiquidacion();
                        limpiarTodo();                        
                        
                        ((IngresoLiquidacionGastoView) (mainUI.getNavigator().getCurrentView())).llenarTablaLiquidacion(String.valueOf(empresaCbx.getValue()));

                        Notification.show("Registro eliminado exitosamente!.", Notification.Type.TRAY_NOTIFICATION);
                        
                    } catch (SQLException ex) {
                        System.out.println("Error al intentar eliminar " + ex);
                        ex.printStackTrace();

                    }
                } else {
                    Notification.show("Operación cacelada!", Notification.Type.WARNING_MESSAGE);
                }
            }
        });
    }
    public void cambiarEstatusToken(String codigoPartida){
        
        try {
            String queryString = "UPDATE token SET ";
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
