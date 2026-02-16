package com.simpletecno.sopdi.extras;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;

import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.renderers.DateRenderer;
import com.vaadin.ui.themes.ValoTheme;
import guatefac.Guatefac;
import guatefac.SimpleGuatefacService;
import org.vaadin.ui.NumberField;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.ConnectException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PagosCuotasView extends VerticalLayout implements View {

    UI mainUI;
    String queryString;
    Statement stQuery1;
    ResultSet rsRecords1;
    Statement stQuery2;
    ResultSet rsRecords2;

    String xmlRequest;
    String xmlResponse;

    //                                        (MONTO IVA) * RETENCION
    static final Double FACTOR_IVA_RETENIDO = 0.12 / 1.12 * 0.15;
    //                  Monto sin Iva en el cual si aplica la retencion de IVA
    static final Double LIMITE_IVA_RETENIDO = 2755.0;

    // Columnas Compartidas
    static final String ID_CUOTA = "IdCuota";
    static final String TIPO_CUOTA = "Tipo Cuota";
    static final String ID_UNIDAD = "IdUnidad";
    static final String NOMBRE_UNIDAD = "Nombre";
    static final String FECHA_CUOTA = "Fecha Cuota";
    static final String FECHA_MULTA = "Fecha Limite";
    static final String TIPO_INGRESO = "Tipo Ingreso";
    static final String REFERENCIA = "Referencia";
    static final String MONTO_MULTA = "Monto Multa";
    static final String MONTO_CUOTA = "Monto Cuota";
    static final String TOTAL_CUOTA = "TOTAL";

    // Columnas Pendientes
    static final String ID_NOMENCLATURA = "IdNomenclatura";
    static final String ID_PRODUCTO = "IdProducto";
    static final String NOMBRE_PRODUCTO = "Nombre Producto";
    static final String TIPO_PRODUCTO = "Tipo_Producto";
    static final String EXENTO_IVA = "Exento Iva";
    static final String PAGADO = "Pagado";
    static final String FALTANTE = "Faltante";

    // Columnas Pagadas
    static final String CODIGO_PARTIDA = "Codigo Partida";
    static final String FECHA_PAGADO = "Fecha Pagado";

    // IdCuenta
    static final String ID_PROVEEDOR = "IdProveedor";
    static final String NIT_PROVEEDOR = "Nit";
    static final String NOMBRE_PROVEEDOR = "Nombre Proveedor";
    static final String RETIENEISR_PROPERTY = "Retenedor Isr";
    static final String RETIENEIVA_PROPERTY = "Retenedor Iva";

    VerticalLayout mainLayout;

    HorizontalLayout tituloLayout;
    ComboBox empresaCbx;
    Label tituloLbl;

    VerticalLayout cuotasLayout;
    IndexedContainer cuotasPendientesContainer;
    Grid cuotasPendientesGrid;
    IndexedContainer cuotasPagadasContainer;
    Grid cuotasPagadasGrid;

    VerticalLayout boletaLayout;
    ComboBox cuentaCbx;
    TextField nitCuentaTxt;
    TextField numeroBoletaTxt;
    NumberField montoBoletaTxt;
    Button pagarBtn;
    Button agregarCuotaBtn;
    Button pagoEspecialBtn;
    DateField fechaDt;
    ComboBox tipoIdentificacionCbx;
    ComboBox tipoBoletaCbx;

    LinkedHashMap<Object, Double> pagarMap;
    Double montoTotal = 0.0;
    Double netoMontoTotal = 0.0;
    Double ivaMontoTotal = 0.0;
    Double ivaExento = 0.0;
    Double isrMotno = 0.0;

    Date fechaDocumentoVenta;
    String fechasPagadas;

    String tiposEnFactura;

    String codigoPartidaAnticipo;
    String codigoPartidaCuota;

    String serie = "";
    String numero = "";
    String uuid = ""; // Numero Autorizacion
    String fechaYHoraCertificacion = "";
    String nombre = "";
    String direccion = "";
    String telefono = "";
    File pdfFile = null;

    Boolean aplicaRetencionIva = false;

    public PagosCuotasView(){
        setResponsive(true);

        this.mainUI = UI.getCurrent();
        mainLayout = new VerticalLayout();
        mainLayout.setResponsive(true);

        addComponent(mainLayout);
        setComponentAlignment(mainLayout, Alignment.TOP_CENTER);

        tituloLayout = new HorizontalLayout();
        tituloLayout.setSpacing(true);
        tituloLayout.setMargin(true);
        tituloLayout.setWidth("100%");

        empresaCbx = new ComboBox("Empresa:");
        empresaCbx.setWidth("400px");
        empresaCbx.addStyleName(ValoTheme.COMBOBOX_HUGE);
        empresaCbx.setInvalidAllowed(false);
        empresaCbx.setNewItemsAllowed(false);
        empresaCbx.setTextInputAllowed(false);
        empresaCbx.setNullSelectionAllowed(false);

        tituloLbl = new Label("PAGO Y CONTROL DE CUOTAS");
        tituloLbl.addStyleName(ValoTheme.LABEL_H2);
        tituloLbl.setSizeUndefined();

        tituloLayout.addComponent(empresaCbx);
        tituloLayout.setComponentAlignment(empresaCbx, Alignment.MIDDLE_LEFT);

        tituloLayout.addComponent(tituloLbl);
        tituloLayout.setComponentAlignment(tituloLbl, Alignment.BOTTOM_RIGHT);

        mainLayout.addComponent(tituloLayout);
        mainLayout.setComponentAlignment(tituloLayout, Alignment.TOP_CENTER);

        cuotasLayout = new VerticalLayout();
        cuotasLayout.setWidth("100%");
        cuotasLayout.setResponsive(true);
        cuotasLayout.setMargin(true);
        mainLayout.addComponent(cuotasLayout);
        mainLayout.setComponentAlignment(cuotasLayout, Alignment.TOP_CENTER);


        boletaLayout = new VerticalLayout();
        boletaLayout.setWidth("75%");
        boletaLayout.addStyleName("rcorners3");
        boletaLayout.setResponsive(true);
        boletaLayout.setMargin(true);

        VerticalLayout wrapperLayout = new VerticalLayout();
        wrapperLayout.setWidth("100%");
        wrapperLayout.setMargin(true);
        wrapperLayout.addComponent(boletaLayout);
        wrapperLayout.setComponentAlignment(boletaLayout, Alignment.TOP_CENTER);
        mainLayout.addComponent(wrapperLayout);
        mainLayout.setComponentAlignment(wrapperLayout, Alignment.TOP_CENTER);

        createIngresoDatos();
        createCuotasPendientesGrid();
        createCuotasPagadasGrid();

        llenarProveedores();
        llenarComboEmpresa();
    }

    private void createCuotasPendientesGrid(){
        cuotasPendientesContainer = new IndexedContainer();
        cuotasPendientesContainer.addContainerProperty(ID_CUOTA, Integer.class, 0);
        cuotasPendientesContainer.addContainerProperty(TIPO_CUOTA, Integer.class, 0);
        cuotasPendientesContainer.addContainerProperty(ID_NOMENCLATURA, Integer.class, 0);
        cuotasPendientesContainer.addContainerProperty(ID_PRODUCTO, Integer.class, 0);
        cuotasPendientesContainer.addContainerProperty(NOMBRE_PRODUCTO, String.class, "Producto");
        cuotasPendientesContainer.addContainerProperty(TIPO_PRODUCTO, String.class, "");
        cuotasPendientesContainer.addContainerProperty(EXENTO_IVA, String.class, "NO");
        cuotasPendientesContainer.addContainerProperty(ID_UNIDAD, String.class, "");
        cuotasPendientesContainer.addContainerProperty(NOMBRE_UNIDAD, String.class, "");
        cuotasPendientesContainer.addContainerProperty(FECHA_CUOTA, Date.class, null);
        cuotasPendientesContainer.addContainerProperty(FECHA_MULTA, Date.class, null);
        cuotasPendientesContainer.addContainerProperty(TIPO_INGRESO, String.class, "");
        cuotasPendientesContainer.addContainerProperty(REFERENCIA, String.class, "");
        cuotasPendientesContainer.addContainerProperty(MONTO_MULTA, Double.class, 0d);
        cuotasPendientesContainer.addContainerProperty(MONTO_CUOTA, Double.class, 0d);
        cuotasPendientesContainer.addContainerProperty(TOTAL_CUOTA, Double.class, 0d);
        cuotasPendientesContainer.addContainerProperty(PAGADO, Double.class, 0d);
        cuotasPendientesContainer.addContainerProperty(FALTANTE, Double.class, 0d);
        cuotasPendientesContainer.addContainerProperty(FECHA_PAGADO, String.class, "");

        cuotasPendientesGrid = new Grid("Pendientes", cuotasPendientesContainer);
        cuotasPendientesGrid.setImmediate(true);
        cuotasPendientesGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        cuotasPendientesGrid.setHeightMode(HeightMode.ROW);
        cuotasPendientesGrid.setHeightByRows(5);
        cuotasPendientesGrid.setSizeFull();

        cuotasPendientesGrid.getColumn(ID_CUOTA).setHidable(true);
        cuotasPendientesGrid.getColumn(ID_CUOTA).setHidden(true);
        cuotasPendientesGrid.getColumn(TIPO_CUOTA).setHidden(true);
        cuotasPendientesGrid.getColumn(ID_NOMENCLATURA).setHidden(true);
        cuotasPendientesGrid.getColumn(ID_PRODUCTO).setHidden(true);
        cuotasPendientesGrid.getColumn(NOMBRE_PRODUCTO).setHidden(true);
        cuotasPendientesGrid.getColumn(TIPO_PRODUCTO).setHidden(true);
        cuotasPendientesGrid.getColumn(EXENTO_IVA).setHidden(true);
        cuotasPendientesGrid.getColumn(ID_UNIDAD).setExpandRatio(1);
        cuotasPendientesGrid.getColumn(NOMBRE_UNIDAD).setExpandRatio(2);
        cuotasPendientesGrid.getColumn(FECHA_CUOTA).setExpandRatio(2);
        cuotasPendientesGrid.getColumn(FECHA_CUOTA).setRenderer(new DateRenderer(new SimpleDateFormat("dd/MM/yyyy")));
        cuotasPendientesGrid.getColumn(FECHA_MULTA).setExpandRatio(2);
        cuotasPendientesGrid.getColumn(FECHA_MULTA).setRenderer(new DateRenderer(new SimpleDateFormat("dd/MM/yyyy")));
        cuotasPendientesGrid.getColumn(TIPO_INGRESO).setExpandRatio(3);
        cuotasPendientesGrid.getColumn(REFERENCIA).setExpandRatio(3);
        cuotasPendientesGrid.getColumn(MONTO_MULTA).setExpandRatio(2);
        cuotasPendientesGrid.getColumn(MONTO_CUOTA).setExpandRatio(2);
        cuotasPendientesGrid.getColumn(TOTAL_CUOTA).setExpandRatio(2);
        cuotasPendientesGrid.getColumn(PAGADO).setExpandRatio(2);
        cuotasPendientesGrid.getColumn(FALTANTE).setExpandRatio(2);
        cuotasPendientesGrid.getColumn(FECHA_PAGADO).setHidable(true);
        cuotasPendientesGrid.getColumn(FECHA_PAGADO).setHidden(true);

        VerticalLayout pendientesLayout = new VerticalLayout();
        pendientesLayout.setWidth("98%");
        pendientesLayout.addStyleName("rcorners3");
        pendientesLayout.setResponsive(true);
        pendientesLayout.setMargin(true);
        pendientesLayout.addComponents(cuotasPendientesGrid);
        pendientesLayout.setComponentAlignment(cuotasPendientesGrid, Alignment.MIDDLE_CENTER);

        cuotasLayout.addComponent(pendientesLayout);
        cuotasLayout.setComponentAlignment(pendientesLayout, Alignment.TOP_CENTER);
    }

    private void createCuotasPagadasGrid(){
        cuotasPagadasContainer = new IndexedContainer();
        cuotasPagadasContainer.addContainerProperty(ID_CUOTA, Integer.class, 0);
        cuotasPagadasContainer.addContainerProperty(ID_UNIDAD, String.class, "");
        cuotasPagadasContainer.addContainerProperty(NOMBRE_UNIDAD, String.class, "");
        cuotasPagadasContainer.addContainerProperty(FECHA_CUOTA, String.class, "");
        cuotasPagadasContainer.addContainerProperty(FECHA_MULTA, String.class, "");
        cuotasPagadasContainer.addContainerProperty(TIPO_INGRESO, String.class, "");
        cuotasPagadasContainer.addContainerProperty(REFERENCIA, String.class, "");
        cuotasPagadasContainer.addContainerProperty(MONTO_MULTA, Double.class, 0d);
        cuotasPagadasContainer.addContainerProperty(MONTO_CUOTA, Double.class, 0d);
        cuotasPagadasContainer.addContainerProperty(TOTAL_CUOTA, Double.class, 0d);
        cuotasPagadasContainer.addContainerProperty(CODIGO_PARTIDA, String.class, "");
        cuotasPagadasContainer.addContainerProperty(FECHA_PAGADO, String.class, "");

        cuotasPagadasGrid = new Grid("Pagadas", cuotasPagadasContainer);

        cuotasPagadasGrid.setImmediate(true);
        cuotasPagadasGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        cuotasPagadasGrid.setHeightMode(HeightMode.ROW);
        cuotasPagadasGrid.setHeightByRows(5);
        cuotasPagadasGrid.setSizeFull();

        cuotasPagadasGrid.getColumn(ID_CUOTA).setHidable(true);
        cuotasPagadasGrid.getColumn(ID_CUOTA).setHidden(true);
        cuotasPagadasGrid.getColumn(ID_UNIDAD).setExpandRatio(1);
        cuotasPagadasGrid.getColumn(NOMBRE_UNIDAD).setExpandRatio(2);
        cuotasPagadasGrid.getColumn(FECHA_CUOTA).setExpandRatio(2);
        cuotasPagadasGrid.getColumn(FECHA_MULTA).setExpandRatio(2);
        cuotasPagadasGrid.getColumn(TIPO_INGRESO).setExpandRatio(3);
        cuotasPagadasGrid.getColumn(REFERENCIA).setExpandRatio(3);
        cuotasPagadasGrid.getColumn(MONTO_MULTA).setExpandRatio(2);
        cuotasPagadasGrid.getColumn(MONTO_CUOTA).setExpandRatio(2);
        cuotasPagadasGrid.getColumn(TOTAL_CUOTA).setExpandRatio(2);
        cuotasPagadasGrid.getColumn(CODIGO_PARTIDA).setExpandRatio(2);
        cuotasPagadasGrid.getColumn(FECHA_PAGADO).setExpandRatio(2);

        VerticalLayout pagadosLayout = new VerticalLayout();
        pagadosLayout.setWidth("98%");
        pagadosLayout.addStyleName("rcorners3");
        pagadosLayout.setResponsive(true);
        pagadosLayout.setMargin(true);
        pagadosLayout.setSpacing(true);
        pagadosLayout.addComponents(cuotasPagadasGrid);
        pagadosLayout.setComponentAlignment(cuotasPagadasGrid, Alignment.MIDDLE_CENTER);

        cuotasLayout.addComponent(pagadosLayout);
        cuotasLayout.setComponentAlignment(pagadosLayout, Alignment.BOTTOM_CENTER);

    }

    private void createIngresoDatos(){
        HorizontalLayout datosCuentaLayout = new HorizontalLayout();
        datosCuentaLayout.setMargin(new MarginInfo(true, true, false, true));
        datosCuentaLayout.setSpacing(true);
        datosCuentaLayout.setResponsive(true);
        datosCuentaLayout.setWidth("100%");

        HorizontalLayout datosBoletaLayout = new HorizontalLayout();
        datosBoletaLayout.setMargin(new MarginInfo(false, true, true, true));
        datosBoletaLayout.setSpacing(true);
        datosBoletaLayout.setResponsive(true);
        datosBoletaLayout.setWidth("100%");

        HorizontalLayout botonesLayout = new HorizontalLayout();
        botonesLayout.setSpacing(true);
        botonesLayout.setResponsive(true);
        botonesLayout.setWidth("100%");

        cuentaCbx = new ComboBox("Cuenta:");
        cuentaCbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaCbx.setTextInputAllowed(true);
        cuentaCbx.setNewItemsAllowed(false);
        cuentaCbx.setNullSelectionAllowed(false);
        cuentaCbx.addItem("");
        cuentaCbx.select("");
        cuentaCbx.setRequired(true);
        cuentaCbx.setResponsive(true);
        cuentaCbx.addContainerProperty(ID_PROVEEDOR, Integer.class, 0);
        cuentaCbx.addContainerProperty(NIT_PROVEEDOR, String.class, "");
        cuentaCbx.addContainerProperty(NOMBRE_PROVEEDOR, String.class, "");
        cuentaCbx.addContainerProperty(RETIENEISR_PROPERTY, String.class, "");
        cuentaCbx.addContainerProperty(RETIENEIVA_PROPERTY, String.class, "");
        cuentaCbx.setSizeFull();
        cuentaCbx.addValueChangeListener(valueChangeEvent -> {
            if (fechaDt != null) {
                if (!fechaDt.isEmpty()) {
                    nitCuentaTxt.setValue(cuentaCbx.getContainerProperty(cuentaCbx.getValue(), NIT_PROVEEDOR).getValue().toString());
                    llenarCuotasPendientesGrid();
                    llenarCuotasPagadasGrid();
                }
            }
        });

        nitCuentaTxt = new TextField("Nit:");
        nitCuentaTxt.setResponsive(true);
        nitCuentaTxt.setSizeFull();
        nitCuentaTxt.setEnabled(false);

        tipoIdentificacionCbx = new ComboBox("Tipo Identificación: ");
        tipoIdentificacionCbx.setDescription("Que ID se usara para indentificar al Cliente, \n\t- NIT \n\t- DPI \n\t- Pasaporte");
        tipoIdentificacionCbx.setSizeFull();
        tipoIdentificacionCbx.setTextInputAllowed(true);
        tipoIdentificacionCbx.setNewItemsAllowed(false);
        tipoIdentificacionCbx.setNullSelectionAllowed(false);
        tipoIdentificacionCbx.addValueChangeListener(event -> {
            nitCuentaTxt.setCaption(tipoIdentificacionCbx.getValue() + " :");
        });
        tipoIdentificacionCbx.addItem("NIT");
        tipoIdentificacionCbx.addItem("DPI");
        tipoIdentificacionCbx.addItem("Pasaporte");
        tipoIdentificacionCbx.select("NIT");

        tipoBoletaCbx = new ComboBox("Tipo Boleta: ");
        tipoBoletaCbx.setDescription("Que ID se usara para indentificar al Cliente, \n\t- NIT \n\t- DPI \n\t- Pasaporte");
        tipoBoletaCbx.setSizeFull();
        tipoBoletaCbx.setTextInputAllowed(true);
        tipoBoletaCbx.setNewItemsAllowed(false);
        tipoBoletaCbx.setNullSelectionAllowed(false);
        tipoBoletaCbx.addItem("NOTA DE CREDITO");
        tipoBoletaCbx.addItem("DEPOSITO");
        tipoBoletaCbx.select("NOTA DE CREDITO");

        numeroBoletaTxt = new TextField("Numero Boleta:");
        numeroBoletaTxt.setRequired(true);
        numeroBoletaTxt.setResponsive(true);
        numeroBoletaTxt.setSizeFull();

        fechaDt = new DateField("Fecha: ");
        fechaDt.setSizeFull();
        fechaDt.setDateFormat("dd/MM/yyyy");
        fechaDt.setValue(new java.util.Date());
        fechaDt.addValueChangeListener(valueChangeEvent -> {
            if (cuentaCbx != null) {
                if (cuentaCbx.getValue() != null) {
                    llenarCuotasPendientesGrid();
                    llenarCuotasPagadasGrid();
                }
            }
        });

        montoBoletaTxt = new NumberField("Monto Boleta:");
        montoBoletaTxt.setDecimalAllowed(true);
        montoBoletaTxt.setDecimalPrecision(2);
        montoBoletaTxt.setMinimumFractionDigits(2);
        montoBoletaTxt.setDecimalSeparator('.');
        montoBoletaTxt.setDecimalSeparatorAlwaysShown(true);
        montoBoletaTxt.setGroupingUsed(true);
        montoBoletaTxt.setGroupingSeparator(',');
        montoBoletaTxt.setGroupingSize(3);
        montoBoletaTxt.setImmediate(true);
        montoBoletaTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        montoBoletaTxt.setSizeFull();
        montoBoletaTxt.setValue(0.00);
        montoBoletaTxt.setRequired(true);
        montoBoletaTxt.setResponsive(true);

        pagarBtn = new Button("Pagar Cuotas");
        pagarBtn.addClickListener(clickEvent -> {
            if(datosValidos(cuentaCbx)){
                generarDatosDePago();
                if (documentoCertificado(cuentaCbx)){
                    insertarPartidas(cuentaCbx);
                }
            }
            llenarCuotasPendientesGrid();
            llenarCuotasPagadasGrid();
        });

        agregarCuotaBtn = new Button("Agregar Cuota");
        agregarCuotaBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if(cuentaCbx.getValue() != null){
                    if(!cuentaCbx.getValue().equals("")) {
                        String nombreProveedor = (String) cuentaCbx.getContainerProperty(cuentaCbx.getValue(), NOMBRE_PROVEEDOR).getValue();
                        String idProveedor = cuentaCbx.getContainerProperty(cuentaCbx.getValue(), ID_PROVEEDOR).getValue() + "";

                        PagosCuotasNuevaForm pagosCuotasNuevaForm = new PagosCuotasNuevaForm(idProveedor, nombreProveedor);

                        UI.getCurrent().addWindow(pagosCuotasNuevaForm);
                        pagosCuotasNuevaForm.center();
                    }else {
                        Notification.show("Por favor seleccione la cuanta a quien agregar al nueva Cuota", Notification.Type.WARNING_MESSAGE);
                        cuentaCbx.focus();
                    }
                }else {
                    Notification.show("Por favor seleccione la cuanta a quien agregar al nueva Cuota", Notification.Type.WARNING_MESSAGE);
                    cuentaCbx.focus();
                }
            }
        });

        pagoEspecialBtn = new Button("Pago Especial");
        pagoEspecialBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if(cuentaCbx.getValue() != null){
                    if(!cuentaCbx.getValue().equals("")) {

                        PagoCuotasEspecialForm pagoCuotasEspecialForm = new PagoCuotasEspecialForm(cuentaCbx);

                        UI.getCurrent().addWindow(pagoCuotasEspecialForm);
                        pagoCuotasEspecialForm.center();
                    }else {
                        Notification.show("Por favor seleccione la cuanta a quien sustituir", Notification.Type.WARNING_MESSAGE);
                        cuentaCbx.focus();
                    }
                }else {
                    Notification.show("Por favor seleccione la cuanta a quien agregar al nueva Cuota", Notification.Type.WARNING_MESSAGE);
                    cuentaCbx.focus();
                }
            }
        });
        datosCuentaLayout.addComponents(cuentaCbx, nitCuentaTxt, tipoIdentificacionCbx);
        datosCuentaLayout.setComponentAlignment(cuentaCbx, Alignment.BOTTOM_CENTER);
        datosCuentaLayout.setComponentAlignment(nitCuentaTxt, Alignment.BOTTOM_CENTER);
        datosCuentaLayout.setComponentAlignment(tipoIdentificacionCbx, Alignment.BOTTOM_CENTER);

        datosCuentaLayout.setExpandRatio(cuentaCbx, 3.0f);
        datosCuentaLayout.setExpandRatio(nitCuentaTxt, 1.5f);
        datosCuentaLayout.setExpandRatio(tipoIdentificacionCbx, 1.0f);

        datosBoletaLayout.addComponents(tipoBoletaCbx, numeroBoletaTxt, fechaDt, montoBoletaTxt);
        datosBoletaLayout.setComponentAlignment(tipoBoletaCbx, Alignment.BOTTOM_CENTER);
        datosBoletaLayout.setComponentAlignment(numeroBoletaTxt, Alignment.BOTTOM_CENTER);
        datosBoletaLayout.setComponentAlignment(fechaDt, Alignment.BOTTOM_CENTER);
        datosBoletaLayout.setComponentAlignment(montoBoletaTxt, Alignment.BOTTOM_CENTER);

        datosBoletaLayout.setExpandRatio(tipoBoletaCbx, 1.5f);
        datosBoletaLayout.setExpandRatio(numeroBoletaTxt, 1.5f);
        datosBoletaLayout.setExpandRatio(fechaDt, 1.0f);
        datosBoletaLayout.setExpandRatio(montoBoletaTxt, 1.5f);

        botonesLayout.addComponents(pagoEspecialBtn, agregarCuotaBtn, pagarBtn);
        botonesLayout.setComponentAlignment(pagoEspecialBtn, Alignment.BOTTOM_LEFT);
        botonesLayout.setComponentAlignment(agregarCuotaBtn, Alignment.BOTTOM_CENTER);
        botonesLayout.setComponentAlignment(pagarBtn, Alignment.BOTTOM_CENTER);

        boletaLayout.addComponents(datosCuentaLayout, datosBoletaLayout, botonesLayout);
        boletaLayout.setComponentAlignment(datosCuentaLayout, Alignment.TOP_CENTER);
        boletaLayout.setComponentAlignment(datosBoletaLayout, Alignment.TOP_CENTER);
        boletaLayout.setComponentAlignment(botonesLayout, Alignment.BOTTOM_CENTER);

    }

    public void llenarComboEmpresa() {
        queryString = " SELECT * from contabilidad_empresa";
        queryString += " Where IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();

        try {
            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords1 = stQuery1.executeQuery(queryString);

            while (rsRecords1.next()) { //  encontrado
                empresaCbx.addItem(rsRecords1.getString("IdEmpresa"));
                empresaCbx.setItemCaption(rsRecords1.getString("IdEmpresa"), rsRecords1.getString("Empresa"));
            }
            rsRecords1.first();

            empresaCbx.select(rsRecords1.getString("IdEmpresa"));

        } catch (Exception ex1) {
            System.out.println("Error al listar empresas: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    private void llenarProveedores(){
        queryString = "SELECT * FROM proveedor ";
        //queryString += "WHERE N0 IN (1, 2, 3, 4) ";
        queryString += "WHERE N0 IN (4) ";
        queryString += "AND Inhabilitado = 0 ";
        queryString += "ORDER BY IdProveedor";

        Object itemId;

        try {
            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords1 = stQuery1.executeQuery(queryString);

            if (rsRecords1.next()) { //  encontrado
                do {

                    itemId = cuentaCbx.addItem();
                    cuentaCbx.setItemCaption(itemId, rsRecords1.getInt("IDProveedor") + " " + rsRecords1.getString("Nombre"));
                    cuentaCbx.getContainerProperty(itemId, ID_PROVEEDOR).setValue(rsRecords1.getInt("IDProveedor"));
                    cuentaCbx.getContainerProperty(itemId, NIT_PROVEEDOR).setValue(rsRecords1.getString("NIT"));
                    cuentaCbx.getContainerProperty(itemId, NOMBRE_PROVEEDOR).setValue(rsRecords1.getString("Nombre"));
                    cuentaCbx.getContainerProperty(itemId, RETIENEISR_PROPERTY).setValue(rsRecords1.getString("EsAgenteRetenedorIsr"));
                    cuentaCbx.getContainerProperty(itemId, RETIENEIVA_PROPERTY).setValue(rsRecords1.getString("EsAgenteRetenedorIva"));
                }while (rsRecords1.next());
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar Proveedores: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void llenarCuotasPendientesGrid(){
        cuotasPendientesContainer.removeAllItems();

        queryString =   "SELECT c.*, pve.* \n" +
                        "FROM cuotas c \n" +
                        "INNER JOIN cuotas_tipo ct ON c.TipoCuota = ct.IdCuota \n" +
                        "INNER JOIN producto_venta_empresa pve on ct.IdProducto = pve.CorrelativoProducto \n" +
                        "WHERE c.IdEmpresa = " + ((SopdiUI)mainUI).sessionInformation.getStrAccountingCompanyId() + " \n" +
                        "AND pve.IdEmpresa = " + ((SopdiUI)mainUI).sessionInformation.getStrAccountingCompanyId() + " \n" +
                        "AND c.Inhabilitado = 0 \n" +
                        "AND (ISNULL(c.CodigoPartida) \n" +
                        "OR c.MontoPagado != (c.Cuota + IF(c.UltimoDiaPago < c.FechaBoleta, c.CobroAdicional, 0))) \n" +
                        "AND c.IdCuenta = " + cuentaCbx.getContainerProperty(cuentaCbx.getValue(), ID_PROVEEDOR).getValue() + " \n" +
                        "AND pve.Especial = 0 " +
                        "ORDER BY c.Fecha, c.idCobro , c.IdUnidad";

        try {
            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords1 = stQuery1.executeQuery(queryString);

            if (rsRecords1.next()) { //  encontrado
                do {
                    Object itemId;
                    itemId = cuotasPendientesContainer.addItem();


                    Double total = rsRecords1.getDouble("Cuota");
                    // Si la boleta se genero antes de la multa, no se tiene que pagar
                    if(rsRecords1.getDate("FechaBoleta") != null) {

                        if (rsRecords1.getDate("FechaBoleta").after(rsRecords1.getDate("UltimoDiaPago"))) {
                            total += rsRecords1.getDouble("CobroAdicional");
                        }
                    }else {
                        if (fechaDt.getValue().after(Utileria.getTomorrow(rsRecords1.getDate("UltimoDiaPago")))) {
                            total += rsRecords1.getDouble("CobroAdicional");
                        }
                    }
                    Double faltante = total - rsRecords1.getDouble("MontoPagado");

                    cuotasPendientesContainer.getContainerProperty(itemId, ID_CUOTA).setValue(rsRecords1.getInt("Id"));
                    cuotasPendientesContainer.getContainerProperty(itemId, TIPO_CUOTA).setValue(rsRecords1.getInt("TipoCuota"));
                    cuotasPendientesContainer.getContainerProperty(itemId, ID_NOMENCLATURA).setValue(rsRecords1.getInt("IdNomenclatura"));
                    cuotasPendientesContainer.getContainerProperty(itemId, ID_PRODUCTO).setValue(rsRecords1.getInt("IdProducto"));
                    cuotasPendientesContainer.getContainerProperty(itemId, NOMBRE_PRODUCTO).setValue(rsRecords1.getString("NombreProducto"));
                    cuotasPendientesContainer.getContainerProperty(itemId, TIPO_PRODUCTO).setValue(rsRecords1.getString("Tipo"));
                    cuotasPendientesContainer.getContainerProperty(itemId, EXENTO_IVA).setValue(rsRecords1.getString("ExentoIva"));
                    cuotasPendientesContainer.getContainerProperty(itemId, ID_UNIDAD).setValue(rsRecords1.getString("IdUnidad"));
                    cuotasPendientesContainer.getContainerProperty(itemId, NOMBRE_UNIDAD).setValue(rsRecords1.getString("Nombre"));
                    cuotasPendientesContainer.getContainerProperty(itemId, FECHA_CUOTA).setValue(rsRecords1.getDate("Fecha"));
                    cuotasPendientesContainer.getContainerProperty(itemId, FECHA_MULTA).setValue(rsRecords1.getDate("UltimoDiaPago"));
                    cuotasPendientesContainer.getContainerProperty(itemId, TIPO_INGRESO).setValue(rsRecords1.getString("TipoIngreso"));
                    cuotasPendientesContainer.getContainerProperty(itemId, REFERENCIA).setValue(rsRecords1.getString("Referencia"));
                    cuotasPendientesContainer.getContainerProperty(itemId, MONTO_MULTA).setValue(Double.valueOf(Utileria.format(rsRecords1.getDouble("CobroAdicional"))));
                    cuotasPendientesContainer.getContainerProperty(itemId, MONTO_CUOTA).setValue(Double.valueOf(Utileria.format(rsRecords1.getDouble("Cuota"))));
                    cuotasPendientesContainer.getContainerProperty(itemId, TOTAL_CUOTA).setValue(Double.valueOf(Utileria.format(total)));
                    cuotasPendientesContainer.getContainerProperty(itemId, PAGADO).setValue(Double.valueOf(Utileria.format(rsRecords1.getDouble("MontoPagado"))));
                    cuotasPendientesContainer.getContainerProperty(itemId, FALTANTE).setValue(Double.valueOf(Utileria.format(faltante)));
                    cuotasPendientesContainer.getContainerProperty(itemId, FECHA_PAGADO).setValue(rsRecords1.getString("FechaBoleta"));

                }while (rsRecords1.next());
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar empresas: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void llenarCuotasPagadasGrid(){
        cuotasPagadasContainer.removeAllItems();

        queryString = "SELECT * from cuotas ";
        queryString += "WHERE IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId() + " ";
        queryString += "AND CodigoPartida IS NOT NULL ";
        queryString += "AND MontoPagado = (Cuota + IF(UltimoDiaPago < FechaBoleta, CobroAdicional, 0)) ";
        queryString += "AND IdCuenta = " + cuentaCbx.getContainerProperty(cuentaCbx.getValue(), ID_PROVEEDOR).getValue() + " ";
        queryString += "AND Inhabilitado = 0 ";
        queryString += "ORDER BY Fecha, IdUnidad ";

        Object itemId;

        try {
            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords1 = stQuery1.executeQuery(queryString);

            if (rsRecords1.next()) { //  encontrado
                do {
                    itemId = cuotasPagadasContainer.addItem();

                    cuotasPagadasContainer.getContainerProperty(itemId, ID_CUOTA).setValue(rsRecords1.getInt("Id"));
                    cuotasPagadasContainer.getContainerProperty(itemId, ID_UNIDAD).setValue(rsRecords1.getString("IdUnidad"));
                    cuotasPagadasContainer.getContainerProperty(itemId, NOMBRE_UNIDAD).setValue(rsRecords1.getString("Nombre"));
                    cuotasPagadasContainer.getContainerProperty(itemId, FECHA_CUOTA).setValue(Utileria.getStaticFecha(rsRecords1.getDate("Fecha")));
                    cuotasPagadasContainer.getContainerProperty(itemId, FECHA_MULTA).setValue(Utileria.getStaticFecha(rsRecords1.getDate("UltimoDiaPago")));
                    cuotasPagadasContainer.getContainerProperty(itemId, TIPO_INGRESO).setValue(rsRecords1.getString("TipoIngreso"));
                    cuotasPagadasContainer.getContainerProperty(itemId, REFERENCIA).setValue(rsRecords1.getString("Referencia"));
                    cuotasPagadasContainer.getContainerProperty(itemId, MONTO_MULTA).setValue(Double.valueOf(Utileria.format(rsRecords1.getDouble("CobroAdicional"))));
                    cuotasPagadasContainer.getContainerProperty(itemId, MONTO_CUOTA).setValue(Double.valueOf(Utileria.format(rsRecords1.getDouble("Cuota"))));
                    cuotasPagadasContainer.getContainerProperty(itemId, TOTAL_CUOTA).setValue(Double.valueOf(Utileria.format(rsRecords1.getDouble("MontoPagado"))));
                    cuotasPagadasContainer.getContainerProperty(itemId, CODIGO_PARTIDA).setValue(rsRecords1.getString("CodigoPartida"));
                    cuotasPagadasContainer.getContainerProperty(itemId, FECHA_PAGADO).setValue(Utileria.getStaticFecha(rsRecords1.getDate("FechaPago")));

                }while (rsRecords1.next());
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar empresas: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void generarDatosDePago(){
        pagarMap = new LinkedHashMap<>();
        montoTotal = 0d;
        netoMontoTotal = 0d;
        ivaMontoTotal = 0d;

        fechasPagadas = "";
        tiposEnFactura = "";

        Double montoSobrante = montoBoletaTxt.getDoubleValueDoNotThrow(); // Variable a reducir para saber con cuanto aun puedo pagar
        montoSobrante += anticiposPendientes(cuentaCbx); // Agregar cualquier saldo que aun no se alla usado.
        Double montoActual = 0d;
        Double montoACobrar = 0d;

        boolean productoBool = true;
        boolean servicioBool = true;

        for (Object itemId : cuotasPendientesContainer.getItemIds()){
            fechasPagadas += Utileria.getStaticFecha((Date) cuotasPendientesContainer.getContainerProperty(itemId, FECHA_MULTA).getValue()) + " ";

            if(productoBool && cuotasPendientesContainer.getContainerProperty(itemId, TIPO_PRODUCTO).getValue().equals("PRODUCTO")) {
                productoBool = false;
                tiposEnFactura += "PRODUCTO ";
            }
            if(servicioBool && cuotasPendientesContainer.getContainerProperty(itemId, TIPO_PRODUCTO).getValue().equals("SERVICIO")) {
                servicioBool = false;
                tiposEnFactura += "SERVICIO ";
            }

            montoActual = (Double) cuotasPendientesContainer.getContainerProperty(itemId, FALTANTE).getValue();

            // Usar el monto sobrante de la boleta cuando aún se pueda pagar parcialmente una cuota, pero no total.
            montoACobrar = (montoSobrante < montoActual) ? montoSobrante : montoActual;

            // Reflejar el saldo que aun puedo usar para pagar cuotas
            montoSobrante -= montoACobrar;

            montoTotal += montoACobrar;
            netoMontoTotal += new Double(Utileria.format(montoACobrar / 1.12));
            ivaMontoTotal += new Double(Utileria.format((montoACobrar / 1.12) * 0.12));

            pagarMap.put(itemId, montoACobrar);
            if(montoSobrante <= 0) break;
        }

        // Si el monto de la factura es mayor a 2800
        if (cuentaCbx.getContainerProperty(cuentaCbx.getValue(), RETIENEIVA_PROPERTY).getValue() != null && !((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyRegimen().equals("EXENTA")){
            aplicaRetencionIva =  Double.compare(montoTotal, LIMITE_IVA_RETENIDO) >= 0;
            aplicaRetencionIva = aplicaRetencionIva && cuentaCbx.getContainerProperty(cuentaCbx.getValue(), RETIENEIVA_PROPERTY).getValue().equals("1");
        }
        if(aplicaRetencionIva){

            montoTotal = 0d;
            netoMontoTotal = 0d;
            ivaMontoTotal = 0d;

            pagarMap = new LinkedHashMap<>();

            montoSobrante = Math.round(montoBoletaTxt.getDoubleValueDoNotThrow() / (1 - FACTOR_IVA_RETENIDO)) + 0.0; // Variable a reducir para saber con cuanto aun puedo pagar
            montoSobrante += anticiposPendientes(cuentaCbx); // Agregar cualquier saldo que aun no se alla usado.

            for (Object itemId : cuotasPendientesContainer.getItemIds()){
                fechasPagadas += Utileria.getStaticFecha((Date) cuotasPendientesContainer.getContainerProperty(itemId, FECHA_MULTA).getValue()) + " ";

                montoActual = (Double) cuotasPendientesContainer.getContainerProperty(itemId, FALTANTE).getValue();

                // Usar el monto sobrante de la boleta cuando aún se pueda pagar parcialmente una cuota, pero no total.
                montoACobrar = (montoSobrante < montoActual) ? montoSobrante : montoActual;

                // Reflejar el saldo que aun puedo usar para pagar cuotas
                montoSobrante -= montoACobrar;

                montoTotal += montoACobrar;
                netoMontoTotal += new Double(Utileria.format(montoACobrar / 1.12));
                ivaMontoTotal += new Double(Utileria.format((montoACobrar / 1.12) * 0.12));

                pagarMap.put(itemId, montoACobrar);
                if(montoSobrante <= 0) break;
            }
        }

        Date fechaInicial = new Date();
        long diffMillis = Math.abs(fechaInicial.getTime() - fechaDt.getValue().getTime());
        long diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis);

        // Si pasan 5 dias
        fechaDocumentoVenta = diffDays > 5 ? fechaInicial : fechaDt.getValue();


        double base = Double.valueOf(Utileria.format((montoTotal / 1.12)));

        if(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getIvaPorPagar() != null) {
            base  = montoTotal;
        }
        else {
            ivaExento = (Double.valueOf(Utileria.format((base * 0.12))));
        }

        if(base <= 30000.00) {
            isrMotno = (Double.valueOf(Utileria.format((base * 0.05))));
        }
        else {
            double isr1 = 30000.00 * 0.05;
            double isr2 = (base - 30000.00) * 0.07;
            isrMotno = (Double.valueOf(Utileria.format(isr1 + isr2)));
        }

        if(!((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyRegimen().equals("Opcional Simplificado sobre Ingresos de Actividades Lucrativas")) {
            isrMotno = 0.0;
        }


    }

    public Double anticiposPendientes(ComboBox proveedorCbx){
        queryString = "SELECT * ";
        queryString += "FROM contabilidad_partida ";
        queryString += "WHERE IdProveedor = " + proveedorCbx.getContainerProperty(proveedorCbx.getValue(), ID_PROVEEDOR).getValue() + " ";
        queryString += "AND IdNomenclatura = " + ((SopdiUI)mainUI).cuentasContablesDefault.getAnticiposClientes() + " ";
        queryString += "AND TipoDocumento = 'FACTURA VENTA' ";
        queryString += "AND IdEmpresa = " + ((SopdiUI)mainUI).sessionInformation.getStrAccountingCompanyId() + " ";
        queryString += "ORDER BY Fecha ";

        try {
            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords1 = stQuery1.executeQuery(queryString);

            return rsRecords1.next() ? rsRecords1.getDouble("Saldo") : 0d;

        } catch (Exception ex1) {
            System.out.println("Error al buscar el ultimo codigoPartida" + ex1.getMessage());
            ex1.printStackTrace();
        }
        return 0.0;
    }

    public boolean documentoCertificado(ComboBox proveedorCbx) {

        /** cerGuatefac_Sertificacion FEL **/
        SimpleGuatefacService guatefacInterface = new SimpleGuatefacService();

        Guatefac guateFacPort = null;
        try {
            guateFacPort = guatefacInterface.createService("usr_guatefac", "usrguatefac");
        } catch (Exception e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "ERROR AL CERTIFICAR LA FACTURA : " + e);
            Notification.show("ERROR AL CONECTAR CON GUATEFACTURA : " + e);
            return false;
        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "URL:" + guatefacInterface.getUrl());
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "NIT:" + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyTaxId());
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Usuario:" + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyFelUser());
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Passwd:" + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyFelPass());

        xmlRequest = "<![CDATA[<DocElectronico>\n" +
                "<Encabezado>\n" +
                "<Receptor>\n";

        if(tipoIdentificacionCbx.getValue().equals("NIT")){ // Si es NIT
            xmlRequest +=       "<NITReceptor>" + proveedorCbx.getContainerProperty(proveedorCbx.getValue(), NIT_PROVEEDOR).getValue().toString().replaceAll("-", "") + "</NITReceptor>\n" +
                    "<Nombre>" + proveedorCbx.getContainerProperty(proveedorCbx.getValue(), NOMBRE_PROVEEDOR).getValue() + "</Nombre>\n" +
                    "<Direccion>" + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyBillingDirection() + "</Direccion>\n";

        }else if(tipoIdentificacionCbx.getValue().equals("DPI")){ //  Si es DPI o CUI
            xmlRequest +=       "<NITReceptor>" + proveedorCbx.getContainerProperty(proveedorCbx.getValue(), NIT_PROVEEDOR).getValue().toString() + "</NITReceptor>\n" +
                    "<Nombre>" + proveedorCbx.getContainerProperty(proveedorCbx.getValue(), NOMBRE_PROVEEDOR).getValue() + "</Nombre>\n" +
                    "<Direccion>" + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyBillingDirection() + "</Direccion>\n";

        }else if(tipoIdentificacionCbx.getValue().equals("Pasaporte")){ // Si es Pasaporte
            xmlRequest +=       "<NITReceptor>" + proveedorCbx.getContainerProperty(proveedorCbx.getValue(), NIT_PROVEEDOR).getValue().toString() + "</NITReceptor>\n" +
                    "<Nombre>" + proveedorCbx.getContainerProperty(proveedorCbx.getValue(), NIT_PROVEEDOR).getValue().toString() + "</Nombre>\n" +
                    "<Direccion>" + proveedorCbx.getContainerProperty(proveedorCbx.getValue(), NIT_PROVEEDOR).getValue().toString() + "</Direccion>\n";

        }else {
            return false;

        }
        xmlRequest +=   "</Receptor>\n" +
                "<InfoDoc>\n" +
                "<TipoVenta>S</TipoVenta>\n" +
                "<DestinoVenta>1</DestinoVenta>\n" +
                "<Fecha>" + Utileria.getFechaDDMMYYYY(fechaDocumentoVenta) + "</Fecha>\n" +
                "<Moneda>1</Moneda>\n" +
                "<Tasa>1</Tasa>\n" ;
        /* Dolares        if(monedaCbx.getValue().equals("DOLARES")) {
            xmlRequest +=   "<Moneda>2</Moneda>\n"+
                    "<Tasa>" + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow())+ "</Tasa>\n" ;
        }
        /* Quetzales          else {
            xmlRequest +=   "<Moneda>1</Moneda>\n" +
                    "<Tasa>1</Tasa>\n" ;
        }*/
        xmlRequest +=
                "<Referencia>" + new Utileria().getReferencia() + "</Referencia>\n" +
                        "<NumeroAcceso></NumeroAcceso>\n" +
                        "<SerieAdmin>" + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanySmallName() + "</SerieAdmin>\n" +
                        "<NumeroAdmin>" + new Utileria().getReferencia() + "</NumeroAdmin>\n" +
                        "<Reversion>N</Reversion>\n" +
                        "</InfoDoc>\n" +
    /* Totales */       "<Totales>\n" ;

        // EXENTO
        if(((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyRegimen().equals("EXENTA")) {
            xmlRequest +=       "<Bruto>" + Utileria.format(montoTotal) + "</Bruto>\n" +
                    "<Descuento>0.00</Descuento>\n" +
                    "<Exento>" + Utileria.format(montoTotal) + "</Exento>\n" +
                    "<Otros>0.00</Otros>\n" +
                    "<Neto>0.00</Neto>\n" +
                    "<Isr>0.00</Isr>\n" +
                    "<Iva>0.00</Iva>\n" +
                    "<Total>" + Utileria.format(montoTotal) + "</Total>\n";
        } else {
            xmlRequest +=       "<Bruto>" + Utileria.format(montoTotal) + "</Bruto>\n" +
                    "<Descuento>0.00</Descuento>\n" +
                    "<Exento>0.00</Exento>\n" +
                    "<Otros>0.00</Otros>\n" +
                    "<Neto>" + Utileria.format(netoMontoTotal) + "</Neto>\n" +
                    "<Isr>0.00</Isr>\n" +
                    "<Iva>" + Utileria.format(ivaMontoTotal) + "</Iva>\n" +
                    "<Total>" + Utileria.format(montoTotal) + "</Total>\n";
        }
        xmlRequest +=       "</Totales>\n" +
                "<DatosAdicionales>\n";
        if(tipoIdentificacionCbx.getValue().equals("NIT")){ // Si es NIT
            xmlRequest +=       "<TipoReceptor>4</TipoReceptor>\n"; // 2: CUI | 3: ID_Extranjero | 4: NIT

        }else if(tipoIdentificacionCbx.getValue().equals("DPI")){ //  Si es DPI o CUI
            xmlRequest +=       "<TipoReceptor>2</TipoReceptor>\n"; // 2: CUI | 3: ID_Extranjero | 4: NIT

        }else if(tipoIdentificacionCbx.getValue().equals("Pasaporte")){ // Si es Pasaporte
            xmlRequest +=       "<TipoReceptor>3</TipoReceptor>\n"; // 2: CUI | 3: ID_Extranjero | 4: NIT

        }

        xmlRequest +=       "</DatosAdicionales>\n" +
                "</Encabezado>\n" +
                "<Detalles>\n";

        // --- Genrando Producto o Servicio
        for (Object itemId : pagarMap.keySet()){

            if(cuotasPendientesContainer.getContainerProperty(itemId, ID_PRODUCTO).getValue() != null && montoBoletaTxt.getDoubleValueDoNotThrow() > 0 ) {

                String producto = cuotasPendientesContainer.getContainerProperty(itemId, ID_PRODUCTO).getValue() + ""; // Id del Producto a Facturar
                Boolean exentoIva = cuotasPendientesContainer.getContainerProperty(itemId, EXENTO_IVA).getValue().equals("SI"); // Si el Producto es Exento de IVA

                // Verificar que ambos sean exentos.
                if(((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyRegimen().equals("EXENTA") != exentoIva) {
                    Notification.show("La Factura y los Productos no comparten SI son Exentas de IVA o NO, Verifique.", Notification.Type.WARNING_MESSAGE);
                    return false;
                }

                xmlRequest +=       "<Productos>\n";
                if(exentoIva) {
                    xmlRequest +=       "<Producto>" + producto + "</Producto>\n" +
                            "<Descripcion>" + cuotasPendientesContainer.getContainerProperty(itemId, NOMBRE_PRODUCTO).getValue() + " | " + cuotasPendientesContainer.getContainerProperty(itemId, REFERENCIA).getValue() + " | " + (((Date)cuotasPendientesContainer.getContainerProperty(itemId, FECHA_CUOTA).getValue()).getYear() + 1900) + "</Descripcion>\n" +
                            "<Medida>1</Medida>\n" +
                            "<Cantidad>" + 1 + "</Cantidad>\n" +
                            "<Precio>" + Utileria.format(pagarMap.get(itemId))  + "</Precio>\n" +
                            "<PorcDesc>0.00</PorcDesc>\n" +
                            "<ImpBruto>" + Utileria.format(pagarMap.get(itemId))  + "</ImpBruto>\n" +
                            "<ImpDescuento>0.00</ImpDescuento>\n" +
                            "<ImpExento>" + Utileria.format(pagarMap.get(itemId))  + "</ImpExento>\n" +
                            "<ImpNeto>0.00</ImpNeto>\n"+
                            "<ImpOtros>0.00</ImpOtros>\n" +
                            "<ImpIsr>0.00</ImpIsr>\n" +
                            "<ImpIva>0.00</ImpIva>\n" +
                            "<ImpTotal>" + Utileria.format(pagarMap.get(itemId))  + "</ImpTotal>\n" +
                            "<productoDet>" + ((String)cuotasPendientesContainer.getContainerProperty(itemId, TIPO_PRODUCTO).getValue()).charAt(0) + "</productoDet>\n";
                }
                else {
                    xmlRequest +=       "<Producto>" + producto + "</Producto>\n" +
                            "<Descripcion>" + cuotasPendientesContainer.getContainerProperty(itemId, NOMBRE_PRODUCTO).getValue() + " | " + cuotasPendientesContainer.getContainerProperty(itemId, REFERENCIA).getValue() + " | " + (((Date)cuotasPendientesContainer.getContainerProperty(itemId, FECHA_CUOTA).getValue()).getYear() + 1900) + "</Descripcion>\n" +
                            "<Medida>1</Medida>\n" +
                            "<Cantidad>" + 1 + "</Cantidad>\n" +
                            "<Precio>" + Utileria.format(pagarMap.get(itemId))  + "</Precio>\n" +
                            "<PorcDesc>0.00</PorcDesc>\n" +
                            "<ImpBruto>" + Utileria.format(pagarMap.get(itemId))  + "</ImpBruto>\n" +
                            "<ImpDescuento>0.00</ImpDescuento>\n" +
                            "<ImpExento>0.0</ImpExento>\n" +                                                                                                                                // Chapuz 7.7
                            "<ImpNeto>" + Utileria.format(Double.parseDouble(Utileria.format(pagarMap.get(itemId) )) / 1.12) + "</ImpNeto>\n" +
                            "<ImpOtros>0.00</ImpOtros>\n" +
                            "<ImpIsr>0.00</ImpIsr>\n" +
                            "<ImpIva>" + Utileria.format(((Double.parseDouble(Utileria.format(pagarMap.get(itemId))) / 1.12) * .12)) + "</ImpIva>\n" +
                            "<ImpTotal>" + Utileria.format(pagarMap.get(itemId) ) + "</ImpTotal>\n" +
                            "<productoDet>" + ((String)cuotasPendientesContainer.getContainerProperty(itemId, TIPO_PRODUCTO).getValue()).charAt(0) + "</productoDet>\n";
                }
                xmlRequest +=       "</Productos>\n";
            }
        }

//                xmlRequest += "</Productos>\n" +  //creo que está demás (verificar)
        xmlRequest += "</Detalles>\n" +
                "</DocElectronico>]]>";

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "xmlRequest:" + xmlRequest);

        // 1 = factura
        // 7 = recibo por donación
        // 8 = recibo
        String tipoDocumentoFel = "1";

        if(((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyRegimen().equals("EXENTA")) {
            tipoDocumentoFel = "7";
        }

        xmlResponse = "<?xml version=\"1.0\"?>" + guateFacPort.generaDocumento(
                ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyFelUser(),
                ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyFelPass(),
                ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyTaxId(),
                new BigDecimal(1), // establecimiento
                new BigDecimal(tipoDocumentoFel), // tipodocumento
                "1",
                "R",
                xmlRequest
        );
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "xmlResponse=" + xmlResponse);

        //Parser that produces DOM object trees from XML content
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setIgnoringElementContentWhitespace(true);
        factory.setNamespaceAware(true);

        //API to obtain DOM Document instance
        DocumentBuilder builder = null;
        try {
            //Create DocumentBuilder with default configuration
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            builder = factory.newDocumentBuilder();

            //Parse the content to Document object
            Document doc = builder.parse(new InputSource(new StringReader(xmlResponse)));
            doc.getDocumentElement().normalize();

            // Respuesta de Guatefactyras
            Node resultado = doc.getFirstChild();
            if (resultado.getChildNodes().getLength() > 2) {
                serie = (resultado.getChildNodes().item(0).getTextContent());          // Serie                | Se Usa en Partida
                numero = (resultado.getChildNodes().item(1).getTextContent());         // Preimpreso           | Se Usa en Partida
                nombre = resultado.getChildNodes().item(2).getTextContent();                    // Nombre
                direccion = resultado.getChildNodes().item(3).getTextContent();                 // Direccion
                telefono = resultado.getChildNodes().item(4).getTextContent();                  // Telefono
                uuid = resultado.getChildNodes().item(5).getTextContent();                      // Numero Autorizacion  | Se Usa en Partida
                fechaYHoraCertificacion = resultado.getChildNodes().item(6).getTextContent();   // Referencia           | Se Usa en Partida

                obtenerFacturaPdf(serie, numero);

            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "ERROR AL CERTIFICAR LA FACTURA : " + resultado.getTextContent());
                Notification.show("ERROR AL CERTIFICAR LA FACTURA  : " + resultado.getTextContent(), Notification.Type.ERROR_MESSAGE);
                return false;
            }

        } catch (Exception e) {
            Notification.show("ERROR AL CERTIFICAR LA FACTURA  : " + e.getMessage());
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "ERROR AL CERTIFICAR LA FACTURA");
            e.printStackTrace();
            return false;
        }

        return true;
//        return false;
    }

    public void obtenerFacturaPdf(String serie, String numero) {

        Utileria utileria = new Utileria();

        long fileSize = 0;
        byte[] ba1 = new byte[1024];
        int baLength;

        try {

            Thread.sleep(1000);

//                URL pdfFileUrl = new URL("https://dte.guatefacturas.com/reports/rwservlet?repfel&report=r65_2170&destype=cache&desformat=pdf&paramform=no&P_NIT_EMISOR=" + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyTaxId() + "&P_UUID=" + referencia);
            String credemciales = "P_NIT_EMISOR=" + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyTaxId() + "&P_SERIE=" + serie + "&P_NUMERO=" + numero + "&P_FECHA=" + utileria.getFechaSinFormato_v2(fechaDt.getValue());
            URL pdfFileUrl = new URL("https://dte.guatefacturas.com/reports/rwservlet?reportesfel&report=r65_0014&destype=cache&desformat=pdf&paramform=no&" + credemciales);
            pdfFile = new File(((SopdiUI) UI.getCurrent()).enviromentsVars.getDtePath() + serie + "_" + numero + ".pdf");
            FileOutputStream fos1 = new FileOutputStream(pdfFile);

            // Contacting the URL
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Connecting to " + pdfFileUrl.toString() + " ... ");
            URLConnection urlConn = pdfFileUrl.openConnection();

            // Checking whether the URL contains a PDF
            if (!urlConn.getContentType().equalsIgnoreCase("application/pdf")) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "ERROR AL OBTENER EL ARCHIVO PDF DE LA FACTUR.");
                Notification.show("ERROR AL OBTENER EL ARCHIVO PDF DE LA FACTURA...", Notification.Type.ERROR_MESSAGE);
            } else {
                try {
                    // Read the PDF from the URL and save to a local file
                    InputStream is1 = pdfFileUrl.openStream();
                    while ((baLength = is1.read(ba1)) != -1) {
                        fos1.write(ba1, 0, baLength);
                    }
                    fos1.flush();
                    fos1.close();
                    is1.close();

                    // Load the PDF document and display its page count
                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, "DONE.Processing the PDF ... ");

                } catch (ConnectException ce) {
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "ERROR AL OBTENER EL ARCHIVO PDF DE LA FACTURA.\n[" + ce.getMessage() + "]\n");
                    Notification.show("ERROR AL OBTENER EL ARCHIVO PDF DE LA FACTURA...", Notification.Type.ERROR_MESSAGE);
                    ce.printStackTrace();
                }
            }

        } catch (Exception exep) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "FAILED.\n[" + exep.getMessage() + "]\n");
            Notification.show("ERROR AL OBTENER EL ARCHIVO PDF DE LA FACTURA...", Notification.Type.ERROR_MESSAGE);
        }
    }

    public boolean datosValidos(ComboBox proveedorCbx) {

        if (((SopdiUI) UI.getCurrent()).esMesCerrado(String.valueOf(empresaCbx.getValue()), Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()))) {
            Notification.show("La fecha del documento no puede ser de un mes ya cerrado contablemente, revise!", Notification.Type.WARNING_MESSAGE);
            fechaDt.focus();
            return false;
        }
        if (!((SopdiUI) UI.getCurrent()).esPrimerMesAbierto(String.valueOf(empresaCbx.getValue()), Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()))) {
            Notification.show("El mes abierto a operaciones es : " + ((SopdiUI) UI.getCurrent()).primerMesAbierto(String.valueOf(empresaCbx.getValue())), Notification.Type.WARNING_MESSAGE);
            fechaDt.focus();
            return false;
        }
        if (((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyFelUser().trim().isEmpty()) {
            if (this.empresaCbx.getValue() != null) {
                Notification.show("Empresa sin Usuario FEL, comuníquese son el administrador", Notification.Type.WARNING_MESSAGE);
                empresaCbx.focus();
                return false;
            }
        }
        if (proveedorCbx.getValue() == null || proveedorCbx.getValue().equals("0")) {
            Notification.show("Por favor ingrese el cliente.", Notification.Type.WARNING_MESSAGE);
            proveedorCbx.focus();
            return false;
        }

        if (tipoBoletaCbx.getValue() == null || tipoBoletaCbx.getValue().equals("")) {
            Notification.show("Por favor ingrese el tipo de boleta.", Notification.Type.WARNING_MESSAGE);
            tipoBoletaCbx.focus();
            return false;
        }

        if (proveedorCbx.getContainerProperty(proveedorCbx.getValue(), NIT_PROVEEDOR).getValue().toString().isEmpty()) {
            Notification.show("Por favor ingrese el Nit del cliente.", Notification.Type.WARNING_MESSAGE);
            nitCuentaTxt.focus();
            return false;
        }

        if (numeroBoletaTxt.isEmpty()) {
            Notification.show("Por favor ingrese el Numero de la Boleta.", Notification.Type.WARNING_MESSAGE);
            numeroBoletaTxt.focus();
            return false;
        }

        if (this.montoBoletaTxt.getDoubleValueDoNotThrow() <= 0) {
            Notification.show("Por favor ingrese el monto de la factura.", Notification.Type.WARNING_MESSAGE);
            montoBoletaTxt.focus();
            return false;
        }

        codigoPartidaAnticipo = Utileria.nextCodigoPartida(
                ((SopdiUI)mainUI).databaseProvider.getCurrentConnection(),
                ((SopdiUI)mainUI).sessionInformation.getStrAccountingCompanyId(),
                fechaDt.getValue(),
                0
        );

        codigoPartidaCuota = Utileria.nextCodigoPartida(
                ((SopdiUI)mainUI).databaseProvider.getCurrentConnection(),
                ((SopdiUI)mainUI).sessionInformation.getStrAccountingCompanyId(),
                fechaDt.getValue(),
                0
        );


        queryString = "Select * FROM contabilidad_partida ";
        queryString += "WHERE NumeroDocumento = '" + numeroBoletaTxt.getValue().toUpperCase().trim() + "' ";
        queryString += "AND IdProveedor =  " + proveedorCbx.getValue() + " ";
        queryString += "AND TipoDocumento = '" + tipoBoletaCbx.getValue() + "' ";
        queryString += "AND IdEmpresa = " + empresaCbx.getValue();

        try {
            rsRecords1 = stQuery1.executeQuery(queryString);

            if (rsRecords1.next()) {
                Notification.show("Este documento ya fué ingresado, revise!. Empresa = " + rsRecords1.getString("IdEmpresa") + " Fecha : " + rsRecords1.getString("Fecha"), Notification.Type.WARNING_MESSAGE);
                numeroBoletaTxt.focus();
                return false;
            }
        } catch (Exception ex1) {
            System.out.println("Error al buscar documento : " + ex1.getMessage());
            ex1.printStackTrace();
            return false;
        }
        return true;
    }

    public void insertarPartidas(ComboBox proveedorCbx) {

        String tipoDocumento;
        if(((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyRegimen().equalsIgnoreCase("EXENTA")) {
            tipoDocumento = "'RECIBO CONTABLE VENTA'";
        }
        else {
            tipoDocumento = "'FACTURA VENTA'";
        }

        queryString = " Insert Into proveedor_cuentacorriente (IdEmpresa,IdProveedor, Fecha,";
        queryString += " TipoDocumento, SerieDocumento,NumeroDocumento, MonedaDocumento, ";
        queryString += " Monto, MontoQuetzales, TipoCambio ";
        queryString += ", IdUsuarioAutorizoPago,CreadoFechayHora,CreadoUsuario)";
        queryString += " Values(";
        queryString += empresaCbx.getValue();
        queryString += "," + proveedorCbx.getContainerProperty(proveedorCbx.getValue(), ID_PROVEEDOR).getValue();
        queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
        queryString += "," + tipoDocumento;
        queryString += ",UPPER('" + serie.trim() + "')";
        queryString += ",'" + numero.trim() + "'";
        //queryString += ",'" + monedaCbx.getValue() + "'";
        queryString += ", 'QUETZALES'";
        queryString += ", " + String.valueOf(montoTotal);
        //queryString += "," + String.valueOf(montoTotal * tasaCambioTxt.getDoubleValueDoNotThrow());
        queryString += "," + String.valueOf(montoTotal * 1);
        queryString += ", " + 1;
        queryString += ", " + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
        queryString += ",'" + Utileria.getFechaYYYYMMDD_1(new java.util.Date()) + "'";
        queryString += ", " + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
        queryString += ")";

        try {
            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery1.executeUpdate(queryString);
        } catch (Exception ex1) {
            System.out.println("Error al insertar proveedor cuentaCorriente : " + ex1.getMessage());
            ex1.printStackTrace();
        }

        double saldo = montoBoletaTxt.getDoubleValueDoNotThrow() + anticiposPendientes(proveedorCbx);
        double saldoSobrante = saldo - montoTotal;

        if(aplicaRetencionIva) {
            saldoSobrante = saldo - (montoTotal * (1 - FACTOR_IVA_RETENIDO));
        }

        saldoSobrante = saldoSobrante > 0 ? saldoSobrante : 0;

        queryString = " INSERT INTO contabilidad_partida (IdEmpresa, Estatus, CodigoPartida, CodigoCC, ";
        queryString += " TipoDocumento, TipoVenta, Fecha, IdProveedor, NitProveedor, NombreProveedor,";
        queryString += " SerieDocumento, NumeroDocumento, IdNomenclatura, MonedaDocumento, MontoDocumento, Debe, Haber,";
        queryString += " DebeQuetzales, HaberQuetzales, TipoCambio, Saldo, Descripcion, Referencia, IdCentroCosto, CodigoCentroCosto,";
        queryString += " CreadoUsuario, CreadoFechaYHora, Archivo, ArchivoTipo, ArchivoPeso, ArchivoNombre,";
        queryString += " UUID, FechaYHoraCertificacion, XmlRequest, XmlResponse, EsCuota, IdProducto";
        queryString += ")";
        queryString += " VALUES ";

        ///---------------------------------------- ANTICIPO -----------------------------------------------///
        //// ANTICIPO BOlETA
        queryString += "(";
        queryString += empresaCbx.getValue();  // IdEmpresa
        queryString += ",'INGRESADO'";  //Estatus
        queryString += ",'" + codigoPartidaAnticipo + "'";  //CodigoPartida
        queryString += ",'" + codigoPartidaAnticipo + "'";  //CodigoCC
        queryString += ",'" + tipoBoletaCbx.getValue() + "'"; // TipoDocumento
        queryString += ",'" + tiposEnFactura + "'";  //TipoVenta
        queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";  //Fecha
        queryString += "," + proveedorCbx.getContainerProperty(proveedorCbx.getValue(), ID_PROVEEDOR).getValue();  //IdProveedor
        queryString += ",'" + proveedorCbx.getContainerProperty(proveedorCbx.getValue(), NIT_PROVEEDOR).getValue() + "'";  //NitProveedor
        queryString += ",'" + proveedorCbx.getContainerProperty(proveedorCbx.getValue(), NOMBRE_PROVEEDOR).getValue() + "'";  //NombreProveedor
        queryString += ",''";  //SerieDocumento
        queryString += ",'" + numeroBoletaTxt.getValue() + "'";  //NumeroDocumento
        queryString += "," + ((SopdiUI) mainUI).cuentasContablesDefault.getAnticiposClientes();
        queryString += ",'QUETZALES'";
        queryString += "," + Utileria.format(montoBoletaTxt.getDoubleValueDoNotThrow()); //MONTODOCUMENTO
        queryString += ",0.00"; //DEBE
        queryString += "," + Utileria.format(Double.valueOf(montoBoletaTxt.getDoubleValueDoNotThrow())); // HABER
        queryString += ",0.00"; //DEBE Q.
        queryString += "," + Utileria.format(Double.valueOf(montoBoletaTxt.getDoubleValueDoNotThrow())); // HABER Q.
        queryString += "," + 1;  //TipoCambio
        queryString += "," + Utileria.format(saldo); //Saldo
        queryString += ",'ANTICIPO CUOTA " + String.valueOf(proveedorCbx.getContainerProperty(proveedorCbx.getValue(), NOMBRE_PROVEEDOR).getValue()) + " - " + fechasPagadas + "'";  //Descripcion
        queryString += ",'NO'";  //referencia
        queryString += "," + 0;  //IdCentroCosto
        queryString += ",''";  //CodigoCentroCosto
        queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();  //CreadoUsuario
        queryString += ",current_timestamp";  //CreadoFechaYHora
        queryString += ",null";  //Archivo
        queryString += ",'application/pdf'";  //ArchivoTipo
        queryString += ",0";  //ArchivoPeso
        queryString += ",''";  //ArchivoNombre
        queryString += ",'" + uuid + "'";   //UUID
        queryString += ",'" + fechaYHoraCertificacion + "'";  //FechaYHoraCertificacion
        queryString += ",''";  //XmlRequest
        queryString += ",''";  //XmlResponse
        queryString += ",0";  //EsCuota
        queryString += ", null)";  //IdProducto

        //// BANCOS
        queryString += ",(";
        queryString += empresaCbx.getValue();  // IdEmpresa
        queryString += ",'INGRESADO'";  //Estatus
        queryString += ",'" + codigoPartidaAnticipo + "'";  //CodigoPartida
        queryString += ",'" + codigoPartidaAnticipo + "'";  //CodigoCC
        queryString += ",'" + tipoBoletaCbx.getValue() + "'"; // TipoDocumento
        queryString += ",'" + tiposEnFactura + "'";  //TipoVenta
        queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";  //Fecha
        queryString += "," + proveedorCbx.getContainerProperty(proveedorCbx.getValue(), ID_PROVEEDOR).getValue();  //IdProveedor
        queryString += ",'" + proveedorCbx.getContainerProperty(proveedorCbx.getValue(), NIT_PROVEEDOR).getValue() + "'";  //NitProveedor
        queryString += ",'" + proveedorCbx.getContainerProperty(proveedorCbx.getValue(), NOMBRE_PROVEEDOR).getValue() + "'";  //NombreProveedor
        queryString += ",''";  //SerieDocumento
        queryString += ",'" + numeroBoletaTxt.getValue() + "'";  //NumeroDocumento
        queryString += "," + ((SopdiUI) mainUI).cuentasContablesDefault.getBancosMonedaLocal();
        queryString += ",'QUETZALES'";
        queryString += "," + Utileria.format(montoBoletaTxt.getDoubleValueDoNotThrow()); //MONTODOCUMENTO
        queryString += "," + Utileria.format(Double.valueOf(montoBoletaTxt.getDoubleValueDoNotThrow())); // DEBE
        queryString += ",0.00";  //HABER
        queryString += "," + Utileria.format(Double.valueOf(montoBoletaTxt.getDoubleValueDoNotThrow())); // DEBE Q.
        queryString += ",0.00";  //HABER Q.
        queryString += "," + 1;  // TipoCambio
        queryString += "," + Utileria.format(saldo); //Saldo
        queryString += ",'ANTICIPO CUOTA " + String.valueOf(proveedorCbx.getContainerProperty(proveedorCbx.getValue(), NOMBRE_PROVEEDOR).getValue()) + " - " + fechasPagadas + "'";  //Descripcion
        queryString += ",'NO'";  //referencia
        queryString += "," + 0;  //IdCentroCosto
        queryString += ",''";  //CodigoCentroCosto
        queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();  //CreadoUsuario
        queryString += ",current_timestamp";  //CreadoFechaYHora
        queryString += ",null";  //Archivo
        queryString += ",'application/pdf'";  //ArchivoTipo
        queryString += ",0";  //ArchivoPeso
        queryString += ",''";  //ArchivoNombre
        queryString += ",'" + uuid + "'";  //UUID
        queryString += ",'" + fechaYHoraCertificacion + "'";  //FechaYHoraCertificacion
        queryString += ",''";  //XmlRequest
        queryString += ",''";  //XmlResponse
        queryString += ",0";   //EsCuota
        queryString += ", null)";  //IdProducto

        /// ---------------------------------------- VENTA -----------------------------------------------///
        /// DOCUMENTO VENTA
        for(Object itemId : pagarMap.keySet()) {

            boolean exentoIva = cuotasPendientesContainer.getContainerProperty(itemId, EXENTO_IVA).getValue().equals("SI");
            if(cuotasPendientesContainer.getContainerProperty(itemId, ID_PRODUCTO).getValue() != null && montoBoletaTxt.getDoubleValueDoNotThrow() > 0 ) {
                queryString += ",(";
                queryString += empresaCbx.getValue();  // IdEmpresa
                queryString += ",'INGRESADO'";  //Estatus
                queryString += ",'" + codigoPartidaCuota + "'";  //CodigoPartida
                queryString += ",'" + codigoPartidaAnticipo + "'";  //CodigoCC
                queryString += "," + tipoDocumento;
                queryString += ",'" + ((String)cuotasPendientesContainer.getContainerProperty(itemId, TIPO_PRODUCTO).getValue()) + "'";  //TipoVenta
                queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDocumentoVenta) + "'";  //Fecha
                queryString += "," + proveedorCbx.getContainerProperty(proveedorCbx.getValue(), ID_PROVEEDOR).getValue();  //IdProveedor
                queryString += ",'" + proveedorCbx.getContainerProperty(proveedorCbx.getValue(), NIT_PROVEEDOR).getValue() + "'";  //NitProveedor
                queryString += ",'" + proveedorCbx.getContainerProperty(proveedorCbx.getValue(), NOMBRE_PROVEEDOR).getValue() + "'";  //NombreProveedor
                queryString += ",UPPER('" + serie.trim() + "')";  //SerieDocumento
                queryString += ",'" + numero.trim() + "'";  //NumeroDocumento
                queryString += "," + (cuotasPendientesContainer.getContainerProperty(itemId, ID_NOMENCLATURA).getValue());  //IdNomenclatura
                queryString += ",'QUETZALES'";  //MonedaDocumento
                queryString += "," + montoTotal; //MontoDocumento
                queryString += ",0.00"; //DEBE
                if (((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getIvaPorPagar() != null && !exentoIva) {
                    queryString += "," + Utileria.format((1 * pagarMap.get(itemId)) / 1.12); // HABER
                } else {
                    queryString += "," + Utileria.format(1 * pagarMap.get(itemId)); // HABER
                }
                queryString += ",0.00"; //DebeQuetzales
                if (((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getIvaPorPagar() != null && !exentoIva) {
                    queryString += "," + Utileria.format(((1 * pagarMap.get(itemId)) / 1.12) * 1);  //HaberQuetzales
                } else {
                    queryString += "," + Utileria.format((1 * pagarMap.get(itemId)) * 1);  //HaberQuetzales
                }
                queryString += "," + String.valueOf(1);  //TipoCambio
                queryString += "," + (saldoSobrante); //Saldo
                if (((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getIvaPorPagar() != null) {
                    queryString += ",'FACTURA VENTA CUOTA " + String.valueOf(proveedorCbx.getContainerProperty(proveedorCbx.getValue(), NOMBRE_PROVEEDOR).getValue()) + " - " + fechasPagadas + "'";  //Descripcion
                } else {
                    queryString += ",'RECIBO CONTABLE VENTA CUOTA " +  String.valueOf(proveedorCbx.getContainerProperty(proveedorCbx.getValue(), NOMBRE_PROVEEDOR).getValue()) + " - " + fechasPagadas + "'";  //Descripcion
                }
                queryString += ",'NO'";  //referencia
                queryString += "," + 0;  //IdCentroCosto
                queryString += ",''";  //CodigoCentroCosto
                queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();  //CreadoUsuario
                queryString += ",current_timestamp";  //CreadoFechaYHora
                queryString += ",null";  //Archivo
                queryString += ",'application/pdf'";  //ArchivoTipo
                queryString += "," + (pdfFile != null ? pdfFile.length() : 0);  //ArchivoPeso
                queryString += ",'" + (pdfFile != null ? pdfFile.getAbsolutePath() : "") + "'";  //ArchivoNombre
                queryString += ",'" + uuid + "'";  //UUID
                queryString += ",'" + fechaYHoraCertificacion + "'";  //FechaYHoraCertificacion
                if (((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getIvaPorPagar() != null) {
                    queryString += ",'" + xmlRequest + "'";
                    queryString += ",'" + xmlResponse + "'";
                } else {
                    queryString += ",''";  //XmlRequest
                    queryString += ",''";  //XmlResponse
                }
                queryString += ",1";  //EsCuota
                queryString += ", " + cuotasPendientesContainer.getContainerProperty(itemId, ID_PRODUCTO).getValue() + ")";  //IdProducto
            }
        }
        
        //// ANTICIPO DOCUMENTO VENTA
        queryString += ",(";
        queryString += empresaCbx.getValue();  // IdEmpresa
        queryString += ",'INGRESADO'";  //Estatus
        queryString += ",'" + codigoPartidaCuota + "'";  //CodigoPartida
        queryString += ",'" + codigoPartidaAnticipo + "'";  //CodigoCC
        queryString += "," + tipoDocumento;; // TipoDocumento
        queryString += ",'" + tiposEnFactura + "'";  //TipoVenta
        queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDocumentoVenta) + "'";  //Fecha
        queryString += "," + proveedorCbx.getContainerProperty(proveedorCbx.getValue(), ID_PROVEEDOR).getValue();  //IdProveedor
        queryString += ",'" + proveedorCbx.getContainerProperty(proveedorCbx.getValue(), NIT_PROVEEDOR).getValue() + "'";  //NitProveedor
        queryString += ",'" + proveedorCbx.getContainerProperty(proveedorCbx.getValue(), NOMBRE_PROVEEDOR).getValue() + "'";  //NombreProveedor
        queryString += ",UPPER('" + serie.trim() + "')";  //SerieDocumento
        queryString += ",'" + numero.trim() + "'";  //NumeroDocumento
        queryString += "," + ((SopdiUI) mainUI).cuentasContablesDefault.getAnticiposClientes();
        queryString += ",'QUETZALES'";
        queryString += "," + Utileria.format(montoTotal); //MONTODOCUMENTO
        queryString += "," + Utileria.format(montoTotal); // DEBE
        queryString += ",0.00"; //HABER Q.
        queryString += "," + Utileria.format(montoTotal); // DEBE Q.
        queryString += ",0.00"; //HABER
        queryString += "," + 1;
        queryString += "," + (saldoSobrante); //Saldo
        queryString += ",'FACTURA VENTA CUOTA " + String.valueOf(proveedorCbx.getContainerProperty(proveedorCbx.getValue(), NOMBRE_PROVEEDOR).getValue()) + " - " + fechasPagadas + "'";  //Descripcion
        queryString += ",'NO'";  //referencia
        queryString += "," + 0;  //IdCentroCosto
        queryString += ",''";  //CodigoCentroCosto
        queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();  //CreadoUsuario
        queryString += ",current_timestamp";  //CreadoFechaYHora
        queryString += ",null";  //Archivo
        queryString += ",'application/pdf'";  //ArchivoTipo
        queryString += "," + (pdfFile != null ? pdfFile.length() : 0);  //ArchivoPeso
        queryString += ",'" + (pdfFile != null ? pdfFile.getAbsolutePath() : "") + "'";  //ArchivoNombre
        queryString += ",'" + uuid + "'";  //UUID
        queryString += ",'" + fechaYHoraCertificacion + "'";  //FechaYHoraCertificacion
        queryString += ",''";  //XmlRequest
        queryString += ",''";  //XmlResponse
        queryString += ",1";  //EsCuota
        queryString += ", null)";  //IdProducto

        if(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getIvaPorPagar() != null && !((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyRegimen().equals("EXENTA")) {

            System.out.println("entra a insertar linea del iva.  exentaChb.getValue()=" + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyRegimen().equals("EXENTA") + " getIvaPorPagar()=" + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getIvaPorPagar());
            //// INSERTAR EL IVA
            queryString += ",(";
            queryString += empresaCbx.getValue();  // IdEmpresa
            queryString += ",'INGRESADO'";  //Estatus
            queryString += ",'" + codigoPartidaCuota + "'";  //CodigoPartida
            queryString += ",'" + codigoPartidaAnticipo + "'";  //CodigoCC
            queryString += "," + tipoDocumento;; // TipoDocumento
            queryString += ",'" + tiposEnFactura + "'";  //TipoVenta
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDocumentoVenta) + "'";  //Fecha
            queryString += "," + proveedorCbx.getContainerProperty(proveedorCbx.getValue(), ID_PROVEEDOR).getValue();  //IdProveedor
            queryString += ",'" + proveedorCbx.getContainerProperty(proveedorCbx.getValue(), NIT_PROVEEDOR).getValue() + "'";  //NitProveedor
            queryString += ",'" + proveedorCbx.getContainerProperty(proveedorCbx.getValue(), NOMBRE_PROVEEDOR).getValue() + "'";  //NombreProveedor
            queryString += ",UPPER('" + serie.trim() + "')";  //SerieDocumento
            queryString += ",'" + numero.trim() + "'";  //NumeroDocumento
            queryString += "," + ((SopdiUI) mainUI).cuentasContablesDefault.getIvaPorPagar();//IdNomenclatura
            queryString += ",'QUETZALES'";  //MonedaDocumento
            queryString += "," + Utileria.format(montoTotal); //MontoDocumento
            queryString += ",0.00";  //DEBE
            queryString += "," + Utileria.format(ivaMontoTotal);  //Haber
            queryString += ",0.00";  //DebeQuetzales
            queryString += "," + Utileria.format(ivaMontoTotal);  //HaberQuetzales
            queryString += "," + String.valueOf(1);  //TipoCambio
            queryString += "," + (saldoSobrante); //Saldo
            queryString += ",'FACTURA VENTA CUOTA " + String.valueOf(proveedorCbx.getContainerProperty(proveedorCbx.getValue(), NOMBRE_PROVEEDOR).getValue()) + " - " + fechasPagadas + "'";  //Descripcion
            queryString += ",'NO'";  //referencia
            queryString += "," + 0;  //IdCentroCosto
            queryString += ",''";  //CodigoCentroCosto
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();  //CreadoUsuario
            queryString += ",current_timestamp";  //CreadoFechaYHora
            queryString += ",null";  //Archivo
            queryString += ",'application/pdf'";  //ArchivoTipo
            queryString += "," + (pdfFile != null ? pdfFile.length() : 0);  //ArchivoPeso
            queryString += ",'" + (pdfFile != null ? pdfFile.getName() : "") + "'";  //ArchivoNombre
            queryString += ",'" + uuid + "'";  //UUID
            queryString += ",'" + fechaYHoraCertificacion + "'";  //FechaYHoraCertificacion
            queryString += ",''";  //XmlRequest
            queryString += ",''";  //XmlResponse
            queryString += ",1";  //EsCuota
            queryString += ", null)";  //IdProducto
        }

        if(!((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyRegimen().equals("EXENTA")) {

            //SI EL CLIENTE NO ES AGENTE RETENEDOR DEL ISR, INSERTAR LINEAS PARA EL ISR GASTO Y EL ISR OPCIONAL MENSUAL POR PAGAR.
            if (proveedorCbx.getContainerProperty(proveedorCbx.getValue(), RETIENEISR_PROPERTY).getValue() != null &&
                isrMotno > 0) {
                //// ISR GASTO
                queryString += ",(";
                queryString += empresaCbx.getValue();  // IdEmpresa
                queryString += ",'INGRESADO'";  //Estatus
                queryString += ",'" + codigoPartidaCuota + "'";  //CodigoPartida
                queryString += ",'" + codigoPartidaAnticipo + "'";  //CodigoCC
                queryString += "," + tipoDocumento;; // TipoDocumento
                queryString += ",'" + tiposEnFactura + "'";  //TipoVenta
                queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDocumentoVenta) + "'";  //Fecha
                queryString += "," + proveedorCbx.getContainerProperty(proveedorCbx.getValue(), ID_PROVEEDOR).getValue();  //IdProveedor
                queryString += ",'" + proveedorCbx.getContainerProperty(proveedorCbx.getValue(), NIT_PROVEEDOR).getValue() + "'";  //NitProveedor
                queryString += ",'" + proveedorCbx.getContainerProperty(proveedorCbx.getValue(), NOMBRE_PROVEEDOR).getValue() + "'";  //NombreProveedor
                queryString += ",UPPER('" + serie.trim() + "')";  //SerieDocumento
                queryString += ",'" + numero.trim() + "'";  //NumeroDocumento
                queryString += "," + ((SopdiUI) mainUI).cuentasContablesDefault.getIsrGasto();  //IdNomenclatura
                queryString += ",'QUETZALES'";  //MonedaDocumento
                queryString += "," + Utileria.format(montoTotal);  //MontoDocumento
                queryString += "," + Utileria.format(isrMotno);  // DEBE
                queryString += ",0.00"; //HABER
                queryString += "," + Utileria.format(isrMotno);  // DEBE Q.
                queryString += ",0.00"; //HABER Q.
                queryString += "," + 1;
                queryString += "," + (saldoSobrante); //Saldo
                queryString += ",'FACTURA VENTA CUOTA " + String.valueOf(proveedorCbx.getContainerProperty(proveedorCbx.getValue(), NOMBRE_PROVEEDOR).getValue()) + " - " + fechasPagadas + "'";  //Descripcion
                queryString += ",'NO'";  //referencia
                queryString += "," + 0;  //IdCentroCosto
                queryString += ",''";  //CodigoCentroCosto
                queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();  //CreadoUsuario
                queryString += ",current_timestamp";  //CreadoFechaYHora
                queryString += ",null";  //Archivo
                queryString += ",'application/pdf'";  //ArchivoTipo
                queryString += "," + (pdfFile != null ? pdfFile.length() : 0);  //ArchivoPeso
                queryString += ",'" + (pdfFile != null ? pdfFile.getName() : "") + "'";  //ArchivoNombre
                queryString += ",'" + uuid + "'";  //UUID
                queryString += ",'" + fechaYHoraCertificacion + "'";  //FechaYHoraCertificacion
                queryString += ",''";  //XmlRequest
                queryString += ",''";  //XmlResponse
                queryString += ",1";  //EsCuota
                queryString += ", null)";  //IdProducto

                //// ISR OPCIONAL MENSUAL POR PAGAR
                queryString += ",(";
                queryString += empresaCbx.getValue();  // IdEmpresa
                queryString += ",'INGRESADO'";  //Estatus
                queryString += ",'" + codigoPartidaCuota + "'";  //CodigoPartida
                queryString += ",'" + codigoPartidaAnticipo + "'";  //CodigoCC
                queryString += "," + tipoDocumento;; // TipoDocumento
                queryString += ",'" + tiposEnFactura + "'";  //TipoVenta
                queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDocumentoVenta) + "'";  //Fecha
                queryString += "," + proveedorCbx.getContainerProperty(proveedorCbx.getValue(), ID_PROVEEDOR).getValue();  //IdProveedor
                queryString += ",'" + proveedorCbx.getContainerProperty(proveedorCbx.getValue(), NIT_PROVEEDOR).getValue() + "'";  //NitProveedor
                queryString += ",'" + proveedorCbx.getContainerProperty(proveedorCbx.getValue(), NOMBRE_PROVEEDOR).getValue() + "'";  //NombreProveedor
                queryString += ",UPPER('" + serie.trim() + "')";  //SerieDocumento
                queryString += ",'" + numero.trim() + "'";  //NumeroDocumento
                queryString += "," + ((SopdiUI) mainUI).cuentasContablesDefault.getIsrOpcionalMensualPorPagar();
                queryString += ",'QUETZALES'";
                queryString += "," + Utileria.format(montoTotal); //MONTODOCUMENTO
                queryString += ",0.00"; //DEBE
                queryString += "," + Utileria.format(isrMotno); // HABER
                queryString += ",0.00"; //DEBE Q.
                queryString += "," + Utileria.format(isrMotno); // HABER Q.
                queryString += "," + 1;
                queryString += "," + (saldoSobrante); //Saldo
                queryString += ",'FACTURA VENTA CUOTA " + String.valueOf(proveedorCbx.getContainerProperty(proveedorCbx.getValue(), NOMBRE_PROVEEDOR).getValue()) + " - " + fechasPagadas + "'";  //Descripcion
                queryString += ",'NO'";  //referencia
                queryString += "," + 0;  //IdCentroCosto
                queryString += ",''";  //CodigoCentroCosto
                queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();  //CreadoUsuario
                queryString += ",current_timestamp";  //CreadoFechaYHora
                queryString += ",null";  //Archivo
                queryString += ",'application/pdf'";  //ArchivoTipo
                queryString += "," + (pdfFile != null ? pdfFile.length() : 0);  //ArchivoPeso
                queryString += ",'" + (pdfFile != null ? pdfFile.getName() : "") + "'";  //ArchivoNombre
                queryString += ",'" + uuid + "'";  //UUID
                queryString += ",'" + fechaYHoraCertificacion + "'";  //FechaYHoraCertificacion
                queryString += ",''";  //XmlRequest
                queryString += ",''";  //XmlResponse
                queryString += ",1";  //EsCuota
                queryString += ", null)";  //IdProducto
            }
        }//END IF REGIMEN

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "QUERY FACTURA VENTA : " + queryString);

        try {

            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery1.executeUpdate(queryString);

            Notification notif = new Notification("FACTURA VENTA GENERADA EXITOSAMENTE.", Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.CHECK);
            notif.show(Page.getCurrent());



        } catch (Exception ex1) {
            System.out.println("Error al insertar facturas  : " + ex1.getMessage());
            ex1.printStackTrace();
            Notification notif = new Notification("HA OCURRIDO UN ERROR DE BASE DE DATOS : " + ex1.getMessage(),
                    Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.WARNING);
            notif.show(Page.getCurrent());
        }

        // Insertamos El espejo de Guatefactura
        insertarDocumentoElectronico(codigoPartidaCuota);
        updateCuota();
        resetValoresBoleta();
    }

    private void insertarDocumentoElectronico(String codigoPartida){

        queryString =   "INSERT INTO guatefactura_documento_electronico (";
        queryString +=  "SistemaEmisor, IdEmpresa, CodigoPartida, Serie, Preimpreso, Nombre, Direccion, ";
        queryString +=  "Telefono, NumeroAutorizacion, Referencia, IdTipo, Estado, FechaCreacionFel)";
        queryString +=  "VALUES (";
        queryString +=  "1"; // IMPORTANTE, ESTO CAMBIARA EN UN FUTURO, RECORDAR FUTURA API
        queryString +=  ", " + empresaCbx.getValue();
        queryString +=  ", '" + codigoPartida + "'";
        queryString +=  ", '" + serie + "'";
        queryString +=  ", " + numero;
        queryString +=  ", '" + nombre + "'";
        queryString +=  ", '" + direccion + "'";
        queryString +=  ", '" + telefono + "'";
        queryString +=  ", '" + uuid + "'";
        queryString +=  ", '" + fechaYHoraCertificacion + "'";
        queryString +=  ", '" + tipoIdentificacionCbx.getValue() + "'";
        queryString +=  ", 'INGRESADO'";
        queryString +=  ", current_timestamp)";


        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "QUERY DOCUMENTO ELECTROCNICO : " + queryString);

        try {

            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery1.executeUpdate(queryString);

        } catch (Exception ex1) {
            System.out.println("Error al insertar Documetno Electronico  : " + ex1.getMessage());
            ex1.printStackTrace();
            Notification notif = new Notification("HA OCURRIDO UN ERROR DE BASE DE DATOS : " + ex1.getMessage(),
                    Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.WARNING);
            notif.show(Page.getCurrent());
            /*
            try {
                String emailsTo[] = {"alerta@simpletecno.com"};
                MyEmailMessanger eMail = new MyEmailMessanger();

                eMail.postMail(emailsTo, "Error en SOPDI", "Error en base de datos :  " + this.getClass().getName() + " -->" + ex1.getMessage());
            } catch (MessagingException ex2) {
                Logger.getLogger(SopdiUI.class.getName()).log(Level.SEVERE, null, ex2);
            }*/
        }
    }

    private void updateCuota(){

        for(Object itemId : pagarMap.keySet()) {
            queryString =   "UPDATE cuotas SET ";
            queryString +=  "MontoPagado = MontoPagado + " + Utileria.format(pagarMap.get(itemId)) + ", ";
            queryString +=  "FechaPago = '" + Utileria.getFechaYYYYMMDD_1(fechaDocumentoVenta) + "', ";
            queryString += "FechaBoleta = IF(FechaBoleta IS NULL, '" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "', FechaBoleta), ";
            queryString +=  "CodigoPartida = " + codigoPartidaCuota + " ";
            queryString +=  "WHERE Id = " + cuotasPendientesContainer.getContainerProperty(itemId, ID_CUOTA).getValue();

            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "QUERY ACTUALIZACION CUOTA : " + queryString);

            try {

                stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                stQuery1.executeUpdate(queryString);

            } catch (Exception ex1) {
                System.out.println("Error al Actualizar la Cuota : " + ex1.getMessage());
                ex1.printStackTrace();
                Notification notif = new Notification("HA OCURRIDO UN ERROR DE BASE DE DATOS : " + ex1.getMessage(),
                        Notification.Type.HUMANIZED_MESSAGE);
                notif.setDelayMsec(1500);
                notif.setPosition(Position.MIDDLE_CENTER);
                notif.setIcon(FontAwesome.WARNING);
                notif.show(Page.getCurrent());
            }
        }
    }

    private void resetValoresBoleta(){
        tipoBoletaCbx.setValue("NOTA DE CREDITO");
        numeroBoletaTxt.setValue("");
        fechaDt.setValue(new Date());
        montoBoletaTxt.setValue(0.0);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent viewChangeEvent) {

    }
}
