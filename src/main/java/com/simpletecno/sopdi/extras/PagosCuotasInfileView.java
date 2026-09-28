package com.simpletecno.sopdi.extras;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.simpletecno.sopdi.extras.infile.InfileClient;
import com.simpletecno.sopdi.extras.infile.Producto;
import com.simpletecno.sopdi.extras.infile.Receptor;
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
import com.vaadin.ui.renderers.DateRenderer;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.ui.NumberField;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PagosCuotasInfileView extends VerticalLayout implements View {

    UI mainUI;

    //                                        (MONTO IVA) * RETENCION
    static final Double FACTOR_IVA_RETENIDO = 0.12 / 1.12 * 0.15;
    //                  Monto sin Iva en el cual si aplica la retencion de IVA
    static final Double LIMITE_IVA_RETENIDO = 2755.0;
    static final BigDecimal BD_FACTOR_IVA_RETENIDO = BigDecimal.valueOf(FACTOR_IVA_RETENIDO);
    static final BigDecimal BD_LIMITE_IVA_RETENIDO = BigDecimal.valueOf(LIMITE_IVA_RETENIDO);
    static final BigDecimal IVA_DIVISOR = new BigDecimal("1.12");
    static final BigDecimal IVA_RATE = new BigDecimal("0.12");
    static final BigDecimal ISR_LIMIT = new BigDecimal("30000.00");
    static final BigDecimal ISR_RATE_LOW = new BigDecimal("0.05");
    static final BigDecimal ISR_RATE_HIGH = new BigDecimal("0.07");

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
    static final String PRODUCTO = "InfileProducto";

    // Columnas Pagadas
    static final String CODIGO_PARTIDA = "Codigo Partida";
    static final String FECHA_PAGADO = "Fecha Pagado";

    // IdCuenta
    static final String ID_PROVEEDOR = "IdProveedor";
    static final String NIT_PROVEEDOR = "Nit";
    static final String NOMBRE_PROVEEDOR = "Nombre Proveedor";
    static final String RETIENEISR_PROPERTY = "Retenedor Isr";
    static final String RETIENEIVA_PROPERTY = "Retenedor Iva";
    static final String NOMBRESINCODIGO_PROPERTY = "NSC";
    static final String DIRECCION_PROPERTY = "DIRECCION";
    static final String CORREO_PROPERTY = "CORREO";

    VerticalLayout mainLayout;
    InfileClient infileClient;

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

    List<Object> pagarList;
    Double montoTotal = 0.0;
    Double netoMontoTotal = 0.0;
    Double ivaMontoTotal = 0.0;
    Double ivaExento = 0.0;
    Double isrMonto = 0.0;

    Date fechaDocumentoVenta;
    String fechasPagadas;

    String tiposEnFactura;

    String codigoPartidaAnticipo;
    String codigoPartidaCuota;

    File pdfFile = null;

    Boolean aplicaRetencionIva = false;

    String empresa = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public PagosCuotasInfileView() {
        setResponsive(true);

        this.mainUI = UI.getCurrent();
        mainLayout = new VerticalLayout();
        mainLayout.setResponsive(true);

        infileClient = new InfileClient(((SopdiUI) mainUI).sessionInformation.getInfileEmisor());

        addComponent(mainLayout);
        setComponentAlignment(mainLayout, Alignment.TOP_CENTER);

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
        cuotasPendientesContainer.addContainerProperty(PRODUCTO, Producto.class, null);
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
        cuotasPendientesGrid.getColumn(PRODUCTO).setHidden(true);
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
        cuentaCbx.addContainerProperty(NOMBRESINCODIGO_PROPERTY, String.class, "");
        cuentaCbx.addContainerProperty(DIRECCION_PROPERTY, String.class, "Guatemala, Guatemala");
        cuentaCbx.addContainerProperty(CORREO_PROPERTY, String.class, "");
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
        fechaDt.setValue(new Date());
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
                try {
                    pdfFile = null;
                    generarDatosDePago();

                    if (pagarList == null || pagarList.isEmpty() || montoTotal <= 0) {
                        Notification.show("No hay cuotas con monto para pagar.", Notification.Type.WARNING_MESSAGE);
                        return;
                    }

                    if (!fechaFacturaValida()) {
                        return;
                    }

                    reservarCodigosPartida();

                    if (documentoCeritficaroInfile(cuentaCbx)){
                        insertarPartidas(cuentaCbx);
                    } else {
                        Notification.show("No se pudo certificar el documento en Infile.", Notification.Type.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Error al pagar cuotas", ex);
                    Notification.show("HA OCURRIDO UN ERROR AL PAGAR CUOTAS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
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

    private void llenarProveedores(){
        String queryString = "SELECT * FROM proveedor_empresa " +
                "WHERE (EsCliente=1 OR EsEncargadoCuenta=1 OR EsResidente=1) " +
                "AND Inhabilitado = 0 " +
                "AND IdEmpresa = ? " +
                "ORDER BY IdProveedor";

        Object itemId;

        try (PreparedStatement pstQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().prepareStatement(queryString, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            pstQuery.setString(1, ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId());

            try (ResultSet rsRecords1 = pstQuery.executeQuery()) {
            if (rsRecords1.next()) { //  encontrado
                do {

                    itemId = cuentaCbx.addItem();
                    cuentaCbx.setItemCaption(itemId, rsRecords1.getInt("IDProveedor") + " " + rsRecords1.getString("Nombre"));
                    cuentaCbx.getContainerProperty(itemId, ID_PROVEEDOR).setValue(rsRecords1.getInt("IDProveedor"));
                    cuentaCbx.getContainerProperty(itemId, NIT_PROVEEDOR).setValue(rsRecords1.getString("NIT"));
                    cuentaCbx.getContainerProperty(itemId, NOMBRE_PROVEEDOR).setValue(rsRecords1.getString("Nombre"));
                    cuentaCbx.getContainerProperty(itemId, RETIENEISR_PROPERTY).setValue(rsRecords1.getString("EsAgenteRetenedorIsr"));
                    cuentaCbx.getContainerProperty(itemId, RETIENEIVA_PROPERTY).setValue(rsRecords1.getString("EsAgenteRetenedorIva"));
                    cuentaCbx.getContainerProperty(itemId, NOMBRESINCODIGO_PROPERTY).setValue(rsRecords1.getString("Nombre"));
                    cuentaCbx.getContainerProperty(itemId, DIRECCION_PROPERTY).setValue(rsRecords1.getString("Direccion"));
                    cuentaCbx.getContainerProperty(itemId, CORREO_PROPERTY).setValue(rsRecords1.getString("Email"));
                }while (rsRecords1.next());
            }
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar Proveedores: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void llenarCuotasPendientesGrid(){
        cuotasPendientesContainer.removeAllItems();

        String queryString =   "SELECT c.*, pve.* \n" +
                        "FROM cuotas c \n" +
                        "INNER JOIN cuotas_tipo ct ON c.TipoCuota = ct.IdCuota \n" +
                        "INNER JOIN producto_venta_empresa pve on ct.IdProducto = pve.CorrelativoProducto \n" +
                        "WHERE c.IdEmpresa = ? \n" +
                        "AND pve.IdEmpresa = ? \n" +
                        "AND c.Inhabilitado = 0 \n" +
                        "AND (ISNULL(c.CodigoPartida) \n" +
                        "OR c.MontoPagado != (c.Cuota + IF(c.UltimoDiaPago < c.FechaBoleta, c.CobroAdicional, 0))) \n" +
                        "AND c.IdCuenta = ? \n" +
                        "AND pve.Especial = 0 " +
                        "ORDER BY c.Fecha, c.idCobro , c.IdUnidad";

        try (PreparedStatement pstQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().prepareStatement(queryString, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            pstQuery.setString(1, ((SopdiUI)mainUI).sessionInformation.getStrAccountingCompanyId());
            pstQuery.setString(2, ((SopdiUI)mainUI).sessionInformation.getStrAccountingCompanyId());
            pstQuery.setObject(3, cuentaCbx.getContainerProperty(cuentaCbx.getValue(), ID_PROVEEDOR).getValue());

            try (ResultSet rsRecords1 = pstQuery.executeQuery()) {
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

                    Map<Integer, Integer> frases = new HashMap<>(); //<Frase, Escenario>
                    String frasesQueryString =  "SELECT * " +
                            "FROM producto_venta_frases ea " +
                            "WHERE CorrelativoProducto = ? "; // <-- se le asigna en el setString()

                    try (PreparedStatement pstFrasesQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().prepareStatement(frasesQueryString)) {
                        pstFrasesQuery.setInt(1, rsRecords1.getInt("CorrelativoProducto"));
                        try (ResultSet rsRecords2 = pstFrasesQuery.executeQuery()) {
                        while (rsRecords2.next()){
                            frases.put(
                                    rsRecords2.getInt("Frase"),
                                    rsRecords2.getInt("Escenario"));
                        }
                        }

                    }
                    catch (Exception ex) {
                        System.out.println("Error al buscar frases de producto: " + ex.getMessage());
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Error al buscar frases de producto", ex);
                    }

                    Producto p = new Producto(
                            rsRecords1.getString("NombreProducto"),
                            0.00,
                            1,
                            ", " + rsRecords1.getString("Referencia") + ", " + (rsRecords1.getDate("Fecha").getYear() + 1900),
                            rsRecords1.getString("InfileTipo"),
                            frases
                    );

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
                    cuotasPendientesContainer.getContainerProperty(itemId, PRODUCTO).setValue(p);

                }while (rsRecords1.next());
            }
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar empresas: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void llenarCuotasPagadasGrid(){
        cuotasPagadasContainer.removeAllItems();

        String queryString = "SELECT * from cuotas " +
                "WHERE IdEmpresa = ? " +
                "AND CodigoPartida IS NOT NULL " +
                "AND MontoPagado = (Cuota + IF(UltimoDiaPago < FechaBoleta, CobroAdicional, 0)) " +
                "AND IdCuenta = ? " +
                "AND Inhabilitado = 0 " +
                "ORDER BY Fecha, IdUnidad ";

        Object itemId;

        try (PreparedStatement pstQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().prepareStatement(queryString, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            pstQuery.setString(1, ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId());
            pstQuery.setObject(2, cuentaCbx.getContainerProperty(cuentaCbx.getValue(), ID_PROVEEDOR).getValue());

            try (ResultSet rsRecords1 = pstQuery.executeQuery()) {
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
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar empresas: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public boolean documentoCeritficaroInfile(ComboBox proveedorCbx){
        String correo;
        Date fechaEmision = fechaDt.getValue();

        if (proveedorCbx.getContainerProperty(proveedorCbx.getValue(), CORREO_PROPERTY).getValue() == null) {
            correo = "";
        } else {
            correo = proveedorCbx.getContainerProperty(proveedorCbx.getValue(), CORREO_PROPERTY).getValue().toString();
        }
        Receptor receptor = new Receptor(
                nitCuentaTxt.getValue().replaceAll("-", ""),
                proveedorCbx.getContainerProperty(proveedorCbx.getValue(), NOMBRESINCODIGO_PROPERTY).getValue().toString(),
                correo,
                proveedorCbx.getContainerProperty(proveedorCbx.getValue(), DIRECCION_PROPERTY).getValue().toString()
        );

        List<Producto> productoList = new ArrayList<>();
        Boolean exentoIva = true;
        for (Object itemId : pagarList) {
            exentoIva &= cuotasPendientesContainer.getContainerProperty(itemId, EXENTO_IVA).getValue().equals("SI");
            Producto p = ((Producto) cuotasPendientesContainer.getContainerProperty(itemId, PRODUCTO).getValue());
            if (p != null && p.tieneMontoMayorCero()) {
                productoList.add(p);
            }

        }
        if (productoList.isEmpty()) {
            Notification.show("Debe ingresar al menos un producto para continuar.", Notification.Type.WARNING_MESSAGE);
            return false;
        }


        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -5);  // hoy - 5 días
        Date fechaLimite = cal.getTime();

        // fechaEmision es más de 5 días antes que hoy
        if (fechaEmision.before(fechaLimite)) {
            fechaEmision = fechaLimite;
        }


        infileClient = new InfileClient(((SopdiUI)mainUI).sessionInformation.getInfileEmisor());
        return infileClient.generarDocumentoBase(
                receptor,
                codigoPartidaCuota,
                productoList,
                exentoIva ?"RDON":"FACT",
                "",
                fechaEmision,
                "GTQ",
                1.00
        );
    }

    public void generarDatosDePago(){
        aplicaRetencionIva = false;     // <--- IMPORTANTÍSIMO
        BigDecimal anticipos = toMoney(anticiposPendientes(cuentaCbx));
        BigDecimal montoSobrante = toMoney(montoBoletaTxt.getDoubleValueDoNotThrow()).add(anticipos); // Variable a reducir para saber con cuanto aun puedo pagar

        calcularCuotasAPagar(montoSobrante);
        // Si el monto de la factura es mayor a 2800
        if (cuentaCbx.getContainerProperty(cuentaCbx.getValue(), RETIENEIVA_PROPERTY).getValue() != null && !esRegimenExento()){
            aplicaRetencionIva =  Double.compare(montoTotal, LIMITE_IVA_RETENIDO) >= 0;
            aplicaRetencionIva = aplicaRetencionIva && cuentaCbx.getContainerProperty(cuentaCbx.getValue(), RETIENEIVA_PROPERTY).getValue().equals("1");
        }
        if(aplicaRetencionIva){

            montoSobrante = toMoney(montoBoletaTxt.getDoubleValueDoNotThrow())
                    .divide(BigDecimal.ONE.subtract(BD_FACTOR_IVA_RETENIDO), 0, RoundingMode.HALF_UP)
                    .add(anticipos);
            calcularCuotasAPagar(montoSobrante);
        }

        Date fechaInicial = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(fechaInicial);
        cal.add(Calendar.DAY_OF_YEAR, -5);

        // Si pasan 5 dias
        fechaDocumentoVenta = fechaDt.getValue().before(cal.getTime()) ? fechaInicial : fechaDt.getValue();


        BigDecimal base = toMoney(BigDecimal.valueOf(montoTotal).divide(IVA_DIVISOR, 2, RoundingMode.HALF_UP));

        if(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getIvaPorPagar() != null) {
            base  = toMoney(montoTotal);
        }
        else {
            ivaExento = toMoney(base.multiply(IVA_RATE)).doubleValue();
        }

        if(base.compareTo(ISR_LIMIT) <= 0) {
            isrMonto = toMoney(base.multiply(ISR_RATE_LOW)).doubleValue();
        }
        else {
            BigDecimal isr1 = ISR_LIMIT.multiply(ISR_RATE_LOW);
            BigDecimal isr2 = base.subtract(ISR_LIMIT).multiply(ISR_RATE_HIGH);
            isrMonto = toMoney(isr1.add(isr2)).doubleValue();
        }

        if(!((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyRegimen().equals("Opcional Simplificado sobre Ingresos de Actividades Lucrativas")) {
            isrMonto = 0.0;
        }


    }

    public Double anticiposPendientes(ComboBox proveedorCbx){
        try {
            return anticiposPendientes(proveedorCbx, ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection()).doubleValue();
        } catch (Exception ex1) {
            System.out.println("Error al buscar anticipos pendientes" + ex1.getMessage());
            ex1.printStackTrace();
            return 0.0;
        }
    }


    public boolean datosValidos(ComboBox proveedorCbx) {

        if (((SopdiUI) UI.getCurrent()).esMesCerrado(empresa, Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()))) {
            Notification.show("La fecha del documento no puede ser de un mes ya cerrado contablemente, revise!", Notification.Type.WARNING_MESSAGE);
            fechaDt.focus();
            return false;
        }
        if (!((SopdiUI) UI.getCurrent()).esPrimerMesAbierto(empresa, Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()))) {
            Notification.show("El mes abierto a operaciones es : " + ((SopdiUI) UI.getCurrent()).primerMesAbierto(empresa), Notification.Type.WARNING_MESSAGE);
            fechaDt.focus();
            return false;
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

        String queryString = "SELECT IdEmpresa, Fecha FROM contabilidad_partida " +
                "WHERE NumeroDocumento = ? " +
                "AND IdProveedor = ? " +
                "AND TipoDocumento = ? " +
                "AND IdEmpresa = ? " +
                "LIMIT 1";

        try (PreparedStatement pstQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().prepareStatement(queryString)) {
            pstQuery.setString(1, numeroBoletaTxt.getValue().toUpperCase().trim());
            pstQuery.setObject(2, proveedorCbx.getContainerProperty(proveedorCbx.getValue(), ID_PROVEEDOR).getValue());
            pstQuery.setString(3, tipoBoletaCbx.getValue().toString());
            pstQuery.setString(4, empresa);

            try (ResultSet rsRecords1 = pstQuery.executeQuery()) {

            if (rsRecords1.next()) {
                Notification.show("Este documento ya fué ingresado, revise!. Empresa = " + rsRecords1.getString("IdEmpresa") + " Fecha : " + rsRecords1.getString("Fecha"), Notification.Type.WARNING_MESSAGE);
                numeroBoletaTxt.focus();
                return false;
            }
            }
        } catch (Exception ex1) {
            System.out.println("Error al buscar documento : " + ex1.getMessage());
            ex1.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean fechaFacturaValida() {
        if (((SopdiUI) UI.getCurrent()).esMesCerrado(empresa, Utileria.getFechaYYYYMMDD_1(fechaDocumentoVenta))) {
            Notification.show("La fecha de la factura no puede ser de un mes ya cerrado contablemente, revise!", Notification.Type.WARNING_MESSAGE);
            fechaDt.focus();
            return false;
        }
        if (!((SopdiUI) UI.getCurrent()).esPrimerMesAbierto(empresa, Utileria.getFechaYYYYMMDD_1(fechaDocumentoVenta))) {
            Notification.show("El mes abierto a operaciones para la factura es : " + ((SopdiUI) UI.getCurrent()).primerMesAbierto(empresa), Notification.Type.WARNING_MESSAGE);
            fechaDt.focus();
            return false;
        }

        return true;
    }

    private void reservarCodigosPartida() {
        Connection conn = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection();
        String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
        Date fechaBoleta = fechaDt.getValue();

        if (mismaFecha(fechaBoleta, fechaDocumentoVenta)) {
            String[] codigosPartida = Utileria.nextCodigosPartida(conn, empresaId, fechaBoleta, 0, 2);
            codigoPartidaAnticipo = codigosPartida[0];
            codigoPartidaCuota = codigosPartida[1];
            return;
        }

        codigoPartidaAnticipo = Utileria.nextCodigoPartida(conn, empresaId, fechaBoleta, 0);
        codigoPartidaCuota = Utileria.nextCodigoPartida(conn, empresaId, fechaDocumentoVenta, 0);
    }

    private boolean mismaFecha(Date fecha1, Date fecha2) {
        return fecha1 != null && fecha2 != null
                && Utileria.getFechaYYYYMMDD_1(fecha1).equals(Utileria.getFechaYYYYMMDD_1(fecha2));
    }

    public void insertarPartidas(ComboBox proveedorCbx) {
        insertarPartidasTransaccional(proveedorCbx);
    }

    private void insertarDocumentoElectronico(String codigoPartida){
        try {
            insertarDocumentoElectronico(((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection(), codigoPartida);
        } catch (Exception ex1) {
            notificarErrorBaseDatos("Error al insertar Documento Electronico", ex1);
        }
    }

    private void updateCuota(){
        try {
            updateCuota(((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection());
        } catch (Exception ex1) {
            notificarErrorBaseDatos("Error al Actualizar la Cuota", ex1);
        }
    }

    private void insertarPartidasTransaccional(ComboBox proveedorCbx) {
        Connection conn = null;
        boolean autoCommitOriginal = true;
        boolean transaccionIniciada = false;

        try {
            String tipoDocumento = esRegimenExento() ? "RECIBO CONTABLE VENTA" : "FACTURA VENTA";
            pdfFile = infileClient.obtenerDTEPdf(((SopdiUI) UI.getCurrent()).enviromentsVars.getDtePath() + infileClient.getSerie() + "_" + infileClient.getNumero() + ".pdf");

            if (pdfFile == null) {
                throw new IllegalStateException("ERROR AL OBTENER PDF DEL DTE, NOTIFIQUE!");
            }

            conn = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection();
            autoCommitOriginal = conn.getAutoCommit();
            conn.setAutoCommit(false);
            transaccionIniciada = true;

            insertarProveedorCuentaCorriente(conn, proveedorCbx, tipoDocumento);

            BigDecimal saldo = toMoney(montoBoletaTxt.getDoubleValueDoNotThrow()).add(anticiposPendientes(proveedorCbx, conn));
            BigDecimal saldoSobrante = saldo.subtract(toMoney(montoTotal));

            if(aplicaRetencionIva) {
                saldoSobrante = saldo.subtract(toMoney(montoTotal).multiply(BigDecimal.ONE.subtract(BD_FACTOR_IVA_RETENIDO)));
            }

            if (saldoSobrante.compareTo(BigDecimal.ZERO) < 0) {
                saldoSobrante = BigDecimal.ZERO;
            }

            insertarPartidasContables(conn, proveedorCbx, tipoDocumento, saldo, toMoney(saldoSobrante));
            insertarDocumentoElectronico(conn, codigoPartidaCuota);
            updateCuota(conn);

            conn.commit();

            Notification notif = new Notification("FACTURA VENTA GENERADA EXITOSAMENTE.", Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.CHECK);
            notif.show(Page.getCurrent());

            resetValoresBoleta();
        } catch (Exception ex1) {
            if (transaccionIniciada && conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Error al revertir pago de cuotas", rollbackEx);
                }
            }
            notificarErrorBaseDatos("Error al insertar pago de cuotas", ex1);
            if (infileClient != null && infileClient.getUUID() != null && !infileClient.getUUID().isEmpty()) {
                Notification.show("DTE certificado en Infile. UUID: " + infileClient.getUUID() + ". Revise el registro local manualmente.", Notification.Type.WARNING_MESSAGE);
            }
        } finally {
            if (transaccionIniciada && conn != null) {
                try {
                    conn.setAutoCommit(autoCommitOriginal);
                } catch (SQLException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Error restaurando autocommit", ex);
                }
            }
        }
    }

    private void insertarProveedorCuentaCorriente(Connection conn, ComboBox proveedorCbx, String tipoDocumento) throws SQLException {
        String sql = "INSERT INTO proveedor_cuentacorriente (IdEmpresa,IdProveedor, Fecha, " +
                "TipoDocumento, SerieDocumento, NumeroDocumento, MonedaDocumento, " +
                "Monto, MontoQuetzales, TipoCambio, IdUsuarioAutorizoPago, CreadoFechayHora, CreadoUsuario) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, empresa);
            ps.setObject(2, getProveedorProperty(proveedorCbx, ID_PROVEEDOR));
            ps.setString(3, Utileria.getFechaYYYYMMDD_1(fechaDocumentoVenta));
            ps.setString(4, tipoDocumento);
            ps.setString(5, serieInfile());
            ps.setString(6, String.valueOf(infileClient.getNumero()));
            ps.setString(7, "QUETZALES");
            ps.setBigDecimal(8, toMoney(montoTotal));
            ps.setBigDecimal(9, toMoney(montoTotal));
            ps.setBigDecimal(10, BigDecimal.ONE);
            ps.setString(11, ((SopdiUI) mainUI).sessionInformation.getStrUserId());
            ps.setString(12, Utileria.getFechaYYYYMMDD_1(new Date()));
            ps.setString(13, ((SopdiUI) mainUI).sessionInformation.getStrUserId());
            ps.executeUpdate();
        }
    }

    private void insertarPartidasContables(Connection conn, ComboBox proveedorCbx, String tipoDocumento, BigDecimal saldo, BigDecimal saldoSobrante) throws SQLException {
        List<PartidaContable> partidas = new ArrayList<>();
        BigDecimal montoBoleta = toMoney(montoBoletaTxt.getDoubleValueDoNotThrow());
        BigDecimal montoTotalBd = toMoney(montoTotal);
        BigDecimal ivaMontoTotalBd = toMoney(ivaMontoTotal);
        BigDecimal isrMontoBd = toMoney(isrMonto);

        String descripcionAnticipo = "ANTICIPO CUOTA " + getProveedorProperty(proveedorCbx, NOMBRE_PROVEEDOR) + " - " + fechasPagadas;
        String descripcionFactura = "FACTURA VENTA CUOTA " + getProveedorProperty(proveedorCbx, NOMBRE_PROVEEDOR) + " - " + fechasPagadas;

        partidas.add(crearPartida(proveedorCbx, codigoPartidaAnticipo, codigoPartidaAnticipo, tipoBoletaCbx.getValue().toString(), tiposEnFactura,
                fechaDt.getValue(), ((SopdiUI) mainUI).cuentasContablesDefault.getAnticiposClientes(), montoBoleta,
                BigDecimal.ZERO, montoBoleta, saldo, descripcionAnticipo, false, 0, null, "", numeroBoletaTxt.getValue()));

        partidas.add(crearPartida(proveedorCbx, codigoPartidaAnticipo, codigoPartidaAnticipo, tipoBoletaCbx.getValue().toString(), tiposEnFactura,
                fechaDt.getValue(), ((SopdiUI) mainUI).cuentasContablesDefault.getBancosMonedaLocal(), montoBoleta,
                montoBoleta, BigDecimal.ZERO, saldo, descripcionAnticipo, false, 0, null, "", numeroBoletaTxt.getValue()));

        for(Object itemId : pagarList) {
            boolean exentoIva = cuotasPendientesContainer.getContainerProperty(itemId, EXENTO_IVA).getValue().equals("SI");
            if(cuotasPendientesContainer.getContainerProperty(itemId, ID_PRODUCTO).getValue() != null && montoBoletaTxt.getDoubleValueDoNotThrow() > 0 ) {
                Producto p = ((Producto) cuotasPendientesContainer.getContainerProperty(itemId, PRODUCTO).getValue());
                BigDecimal monto = toMoney(p.getMonto().multiply(BigDecimal.valueOf(p.getCantidad())));
                BigDecimal haber = monto;
                if (((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getIvaPorPagar() != null && !exentoIva) {
                    haber = monto.divide(IVA_DIVISOR, 2, RoundingMode.HALF_UP);
                }

                partidas.add(crearPartida(proveedorCbx, codigoPartidaCuota, codigoPartidaAnticipo, tipoDocumento,
                        cuotasPendientesContainer.getContainerProperty(itemId, TIPO_PRODUCTO).getValue().toString(),
                        fechaDocumentoVenta, cuotasPendientesContainer.getContainerProperty(itemId, ID_NOMENCLATURA).getValue(), montoTotalBd,
                        BigDecimal.ZERO, haber, saldoSobrante, tipoDocumento + " CUOTA " + getProveedorProperty(proveedorCbx, NOMBRE_PROVEEDOR) + " - " + fechasPagadas,
                        true, 1, cuotasPendientesContainer.getContainerProperty(itemId, ID_PRODUCTO).getValue(), serieInfile(), String.valueOf(infileClient.getNumero())));
            }
        }

        partidas.add(crearPartida(proveedorCbx, codigoPartidaCuota, codigoPartidaAnticipo, tipoDocumento, tiposEnFactura,
                fechaDocumentoVenta, ((SopdiUI) mainUI).cuentasContablesDefault.getAnticiposClientes(), montoTotalBd,
                montoTotalBd, BigDecimal.ZERO, saldoSobrante, descripcionFactura, true, 1, null, serieInfile(), String.valueOf(infileClient.getNumero())));

        if(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getIvaPorPagar() != null && !esRegimenExento()) {
            partidas.add(crearPartida(proveedorCbx, codigoPartidaCuota, codigoPartidaAnticipo, tipoDocumento, tiposEnFactura,
                    fechaDocumentoVenta, ((SopdiUI) mainUI).cuentasContablesDefault.getIvaPorPagar(), montoTotalBd,
                    BigDecimal.ZERO, ivaMontoTotalBd, saldoSobrante, descripcionFactura, true, 1, null, serieInfile(), String.valueOf(infileClient.getNumero())));
        }

        if(!esRegimenExento()) {
            if ("1".equals(String.valueOf(getProveedorProperty(proveedorCbx, RETIENEISR_PROPERTY))) && isrMontoBd.compareTo(BigDecimal.ZERO) > 0) {
                partidas.add(crearPartida(proveedorCbx, codigoPartidaCuota, codigoPartidaAnticipo, tipoDocumento, tiposEnFactura,
                        fechaDocumentoVenta, ((SopdiUI) mainUI).cuentasContablesDefault.getIsrGasto(), montoTotalBd,
                        isrMontoBd, BigDecimal.ZERO, saldoSobrante, descripcionFactura, true, 1, null, serieInfile(), String.valueOf(infileClient.getNumero())));

                partidas.add(crearPartida(proveedorCbx, codigoPartidaCuota, codigoPartidaAnticipo, tipoDocumento, tiposEnFactura,
                        fechaDocumentoVenta, ((SopdiUI) mainUI).cuentasContablesDefault.getIsrOpcionalMensualPorPagar(), montoTotalBd,
                        BigDecimal.ZERO, isrMontoBd, saldoSobrante, descripcionFactura, true, 1, null, serieInfile(), String.valueOf(infileClient.getNumero())));
            }
        }

        String sql = "INSERT INTO contabilidad_partida (IdEmpresa, Estatus, CodigoPartida, CodigoCC, " +
                "TipoDocumento, TipoVenta, Fecha, IdProveedor, NitProveedor, NombreProveedor, " +
                "SerieDocumento, NumeroDocumento, IdNomenclatura, MonedaDocumento, MontoDocumento, Debe, Haber, " +
                "DebeQuetzales, HaberQuetzales, TipoCambio, Saldo, Descripcion, Referencia, IdCentroCosto, CodigoCentroCosto, " +
                "CreadoUsuario, CreadoFechaYHora, Archivo, ArchivoTipo, ArchivoPeso, ArchivoNombre, " +
                "UUID, FechaYHoraCertificacion, XmlRequest, XmlResponse, EsCuota, IdProducto) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, current_timestamp, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (PartidaContable partida : partidas) {
                bindPartida(ps, partida);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private PartidaContable crearPartida(ComboBox proveedorCbx, String codigoPartida, String codigoCC, String tipoDocumento, String tipoVenta,
                                         Date fecha, Object idNomenclatura, BigDecimal montoDocumento, BigDecimal debe, BigDecimal haber,
                                         BigDecimal saldo, String descripcion, boolean incluirArchivo, int esCuota, Object idProducto,
                                         String serieDocumento, String numeroDocumento) {
        PartidaContable partida = new PartidaContable();
        partida.idEmpresa = empresa;
        partida.estatus = "INGRESADO";
        partida.codigoPartida = codigoPartida;
        partida.codigoCC = codigoCC;
        partida.tipoDocumento = tipoDocumento;
        partida.tipoVenta = tipoVenta;
        partida.fecha = fechaString(fecha);
        partida.idProveedor = getProveedorProperty(proveedorCbx, ID_PROVEEDOR);
        partida.nitProveedor = String.valueOf(getProveedorProperty(proveedorCbx, NIT_PROVEEDOR));
        partida.nombreProveedor = String.valueOf(getProveedorProperty(proveedorCbx, NOMBRE_PROVEEDOR));
        partida.serieDocumento = serieDocumento;
        partida.numeroDocumento = numeroDocumento;
        partida.idNomenclatura = idNomenclatura;
        partida.monedaDocumento = "QUETZALES";
        partida.montoDocumento = toMoney(montoDocumento);
        partida.debe = toMoney(debe);
        partida.haber = toMoney(haber);
        partida.debeQuetzales = toMoney(debe);
        partida.haberQuetzales = toMoney(haber);
        partida.tipoCambio = BigDecimal.ONE;
        partida.saldo = toMoney(saldo);
        partida.descripcion = descripcion;
        partida.referencia = "NO";
        partida.idCentroCosto = 0;
        partida.codigoCentroCosto = "";
        partida.creadoUsuario = ((SopdiUI) mainUI).sessionInformation.getStrUserId();
        partida.archivoTipo = "application/pdf";
        partida.archivoPeso = incluirArchivo && pdfFile != null ? pdfFile.length() : 0;
        partida.archivoNombre = incluirArchivo && pdfFile != null ? pdfFile.getAbsolutePath().replace("\\", "/") : "";
        partida.uuid = infileClient.getUUID();
        partida.fechaHoraCertificacion = fechaString(infileClient.getFechaHoraCertificacion());
        partida.xmlRequest = "";
        partida.xmlResponse = "";
        partida.esCuota = esCuota;
        partida.idProducto = idProducto;
        return partida;
    }

    private void bindPartida(PreparedStatement ps, PartidaContable partida) throws SQLException {
        int i = 1;
        ps.setString(i++, partida.idEmpresa);
        ps.setString(i++, partida.estatus);
        ps.setString(i++, partida.codigoPartida);
        ps.setString(i++, partida.codigoCC);
        ps.setString(i++, partida.tipoDocumento);
        ps.setString(i++, partida.tipoVenta);
        setNullableString(ps, i++, partida.fecha);
        ps.setObject(i++, partida.idProveedor);
        ps.setString(i++, partida.nitProveedor);
        ps.setString(i++, partida.nombreProveedor);
        ps.setString(i++, partida.serieDocumento);
        ps.setString(i++, partida.numeroDocumento);
        ps.setObject(i++, partida.idNomenclatura);
        ps.setString(i++, partida.monedaDocumento);
        ps.setBigDecimal(i++, partida.montoDocumento);
        ps.setBigDecimal(i++, partida.debe);
        ps.setBigDecimal(i++, partida.haber);
        ps.setBigDecimal(i++, partida.debeQuetzales);
        ps.setBigDecimal(i++, partida.haberQuetzales);
        ps.setBigDecimal(i++, partida.tipoCambio);
        ps.setBigDecimal(i++, partida.saldo);
        ps.setString(i++, partida.descripcion);
        ps.setString(i++, partida.referencia);
        ps.setInt(i++, partida.idCentroCosto);
        ps.setString(i++, partida.codigoCentroCosto);
        ps.setString(i++, partida.creadoUsuario);
        ps.setNull(i++, Types.BINARY);
        ps.setString(i++, partida.archivoTipo);
        ps.setLong(i++, partida.archivoPeso);
        ps.setString(i++, partida.archivoNombre);
        ps.setString(i++, partida.uuid);
        setNullableString(ps, i++, partida.fechaHoraCertificacion);
        ps.setString(i++, partida.xmlRequest);
        ps.setString(i++, partida.xmlResponse);
        ps.setInt(i++, partida.esCuota);
        if (partida.idProducto == null) {
            ps.setNull(i, Types.INTEGER);
        } else {
            ps.setObject(i, partida.idProducto);
        }
    }

    private void insertarDocumentoElectronico(Connection conn, String codigoPartida) throws SQLException {
        String sql = "INSERT INTO certificado_fel_infile (" +
                "Fecha, Origen, Descripcion, Saldo, Creditos, AlertasInfile, AlertasSat, " +
                "InformacionAdicional, UUID, Serie, Numero, JsonResponse, CodigoPartida, IdEmpresa, Estado) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setNullableString(ps, 1, fechaHoraString(infileClient.getFechaHoraEmision()));
            ps.setString(2, infileClient.getOrigen());
            ps.setString(3, infileClient.getDescripcion());
            ps.setString(4, infileClient.getSaldo());
            ps.setString(5, infileClient.getCreditos());
            ps.setBoolean(6, infileClient.getAlertasInfile());
            ps.setBoolean(7, infileClient.getAlertasSAT());
            ps.setString(8, infileClient.getInformacionAdicional());
            ps.setString(9, infileClient.getUUID());
            ps.setString(10, infileClient.getSerie());
            ps.setString(11, String.valueOf(infileClient.getNumero()));
            ps.setString(12, infileClient.getRespuesta());
            ps.setString(13, codigoPartida);
            ps.setString(14, ((SopdiUI)mainUI).sessionInformation.getStrAccountingCompanyId());
            ps.setString(15, "INGRESADO");
            ps.executeUpdate();
        }
    }

    private void updateCuota(Connection conn) throws SQLException {
        String sql = "UPDATE cuotas SET " +
                "MontoPagado = MontoPagado + ?, " +
                "FechaPago = ?, " +
                "FechaBoleta = IF(FechaBoleta IS NULL, ?, FechaBoleta), " +
                "CodigoPartida = ? " +
                "WHERE Id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for(Object itemId : pagarList) {
                Producto p = ((Producto) cuotasPendientesContainer.getContainerProperty(itemId, PRODUCTO).getValue());
                BigDecimal pago = toMoney(p.getMonto().multiply(BigDecimal.valueOf(p.getCantidad())));
                ps.setBigDecimal(1, pago);
                ps.setString(2, Utileria.getFechaYYYYMMDD_1(fechaDocumentoVenta));
                ps.setString(3, Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()));
                ps.setString(4, codigoPartidaCuota);
                ps.setObject(5, cuotasPendientesContainer.getContainerProperty(itemId, ID_CUOTA).getValue());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private BigDecimal anticiposPendientes(ComboBox proveedorCbx, Connection conn) throws SQLException {
        String queryString = "SELECT Saldo " +
                "FROM contabilidad_partida " +
                "WHERE IdProveedor = ? " +
                "AND IdNomenclatura = ? " +
                "AND TipoDocumento = 'FACTURA VENTA' " +
                "AND IdEmpresa = ? " +
                "ORDER BY Fecha " +
                "LIMIT 1";

        try (PreparedStatement pstQuery = conn.prepareStatement(queryString)) {
            pstQuery.setObject(1, getProveedorProperty(proveedorCbx, ID_PROVEEDOR));
            pstQuery.setObject(2, ((SopdiUI)mainUI).cuentasContablesDefault.getAnticiposClientes());
            pstQuery.setString(3, ((SopdiUI)mainUI).sessionInformation.getStrAccountingCompanyId());

            try (ResultSet rsRecords1 = pstQuery.executeQuery()) {
                return rsRecords1.next() ? toMoney(rsRecords1.getDouble("Saldo")) : BigDecimal.ZERO;
            }
        }
    }

    private void calcularCuotasAPagar(BigDecimal montoSobrante) {
        pagarList = new LinkedList<>();
        BigDecimal montoTotalBd = BigDecimal.ZERO;
        BigDecimal netoMontoTotalBd = BigDecimal.ZERO;
        BigDecimal ivaMontoTotalBd = BigDecimal.ZERO;

        fechasPagadas = "";
        tiposEnFactura = "";

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

            BigDecimal montoActual = toMoney((Double) cuotasPendientesContainer.getContainerProperty(itemId, FALTANTE).getValue());
            BigDecimal montoACobrar = montoSobrante.compareTo(montoActual) < 0 ? montoSobrante : montoActual;

            montoSobrante = montoSobrante.subtract(montoACobrar);
            montoTotalBd = montoTotalBd.add(montoACobrar);
            BigDecimal neto = montoACobrar.divide(IVA_DIVISOR, 2, RoundingMode.HALF_UP);
            netoMontoTotalBd = netoMontoTotalBd.add(neto);
            ivaMontoTotalBd = ivaMontoTotalBd.add(neto.multiply(IVA_RATE).setScale(2, RoundingMode.HALF_UP));

            ((Producto) cuotasPendientesContainer.getContainerProperty(itemId, PRODUCTO).getValue()).setMonto(toMoney(montoACobrar));
            pagarList.add(itemId);
            if(montoSobrante.compareTo(BigDecimal.ZERO) <= 0) break;
        }

        montoTotal = toMoney(montoTotalBd).doubleValue();
        netoMontoTotal = toMoney(netoMontoTotalBd).doubleValue();
        ivaMontoTotal = toMoney(ivaMontoTotalBd).doubleValue();
    }

    private BigDecimal toMoney(Double value) {
        return value == null ? BigDecimal.ZERO : BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal toMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value.setScale(2, RoundingMode.HALF_UP);
    }

    private boolean esRegimenExento() {
        return ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyRegimen().equalsIgnoreCase("EXENTA");
    }

    private Object getProveedorProperty(ComboBox proveedorCbx, String property) {
        return proveedorCbx.getContainerProperty(proveedorCbx.getValue(), property).getValue();
    }

    private String serieInfile() {
        return infileClient.getSerie() == null ? "" : infileClient.getSerie().trim().toUpperCase();
    }

    private String fechaString(Date fecha) {
        return fecha == null ? null : Utileria.getFechaYYYYMMDD_1(fecha);
    }

    private String fechaHoraString(Date fecha) {
        return fecha == null ? null : Utileria.getFechaYYYYMMDDHHMMSS(fecha);
    }

    private void setNullableString(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.VARCHAR);
        } else {
            ps.setString(index, value);
        }
    }

    private void notificarErrorBaseDatos(String mensaje, Exception ex1) {
        Logger.getLogger(getClass().getName()).log(Level.SEVERE, mensaje, ex1);
        Notification notif = new Notification("HA OCURRIDO UN ERROR DE BASE DE DATOS : " + ex1.getMessage(),
                Notification.Type.ERROR_MESSAGE);
        notif.setDelayMsec(1500);
        notif.setPosition(Position.MIDDLE_CENTER);
        notif.setIcon(FontAwesome.WARNING);
        notif.show(Page.getCurrent());
    }

    private static class PartidaContable {
        String idEmpresa;
        String estatus;
        String codigoPartida;
        String codigoCC;
        String tipoDocumento;
        String tipoVenta;
        String fecha;
        Object idProveedor;
        String nitProveedor;
        String nombreProveedor;
        String serieDocumento;
        String numeroDocumento;
        Object idNomenclatura;
        String monedaDocumento;
        BigDecimal montoDocumento;
        BigDecimal debe;
        BigDecimal haber;
        BigDecimal debeQuetzales;
        BigDecimal haberQuetzales;
        BigDecimal tipoCambio;
        BigDecimal saldo;
        String descripcion;
        String referencia;
        int idCentroCosto;
        String codigoCentroCosto;
        String creadoUsuario;
        String archivoTipo;
        long archivoPeso;
        String archivoNombre;
        String uuid;
        String fechaHoraCertificacion;
        String xmlRequest;
        String xmlResponse;
        int esCuota;
        Object idProducto;
    }

    private void resetValoresBoleta(){
        tipoBoletaCbx.setValue("NOTA DE CREDITO");
        numeroBoletaTxt.setValue("");
        fechaDt.setValue(new Date());
        montoBoletaTxt.setValue(0.0);
        pdfFile = null;
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent viewChangeEvent) {
        ((SopdiUI) UI.getCurrent()).lblEmpresaYFormulario.setValue("PAGO DE CUOTAS");
        Page.getCurrent().setTitle("Sopdi - PAGO DE CUOTAS");
    }
}
