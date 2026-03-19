package com.simpletecno.sopdi.ventas;

import com.simpletecno.sopdi.utilerias.MyEmailMessanger;
import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.simpletecno.sopdi.utilerias.ValidarTokenForm;
import com.simpletecno.sopdi.extras.custom.ToggleSwitch;
import com.vaadin.data.Item;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
//import guatefac.Guatefac;
//import guatefac.Guatefac_Service;
import guatefac.Guatefac;
import guatefac.SimpleGuatefacService;
import org.vaadin.ui.NumberField;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.mail.MessagingException;
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
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FacturaVentaForm extends Window {

    static final String NIT_PROPERTY = "NIT";
    static final String NOMBRESINCODIGO_PROPERTY = "NSC";
    static final String RETIENEISR_PROPERTY = "RISR";
    static final String RETIENEIVA_PROPERTY = "RIVA";
    static final String DESCRIPCION_PROPERTY = "DESC";
    static final int TOTAL_PRODUCTOS = 8;

    static DecimalFormat numberFormat = new DecimalFormat("######0.00");

    VerticalLayout mainLayout;
    VerticalLayout headerLayout;

    Date fechaHoy;

    ComboBox tipoFacturaVentaCbx;
    ComboBox centroCostoCbx;
    DateField fechaDt;
    ComboBox clienteCbx;
    TextField nitClienteTxt;
    Label retieneIsrLbl;
    Label retieneIvaLbl;
    CheckBox exentaChb;

    TextField serieTxt;
    TextField numeroTxt;
    ComboBox monedaCbx;
    NumberField tasaCambioTxt;
    NumberField totalFacturadoMesTxt;

    ComboBox nit_cui_passCbx;

    List<NumberField> cantidadlist = new ArrayList<>();

    List<ComboBox> productoList = new ArrayList<>();

    List<ComboBox> cuentaContableList = new ArrayList<>();

    List<TextField> referenciaList = new ArrayList<>();

    List<NumberField> montoList = new ArrayList<>();

    List<TextField> tipoList = new ArrayList<>();

    Map<String, Producto> mapaProductoEmpresa = new HashMap<>();

    Double ivaMontoTotal = 0.0d;
    Double netoMontoTotal = 0.0d;

    NumberField montoTxt;
    NumberField ivaTxt;
    NumberField isrTxt;

    Button grabarBtn;

    ToggleSwitch modoOg;  // Si se agrega La seria y numero manualmente o no

    UI mainUI;
    Statement stQuery;
    ResultSet rsRecords;
    String queryString;

    String xmlRequest;
    String xmlResponse;
    String variableTemp = "";
    String uuid = ""; // Numero Autorizacion
    String fechaYHoraCertificacion = "";
    String nombre = "";
    String direccion = "";
    String telefono = "";
    File pdfFile = null;

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public FacturaVentaForm(String codigoPartidaFactura) {
        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setResponsive(true);
        mainLayout.setMargin(false);
        mainLayout.setWidth("100%");

        HorizontalLayout layoutTitle = new HorizontalLayout();
        layoutTitle.setSpacing(true);
        layoutTitle.setMargin(new MarginInfo(true, true, false, true));
        layoutTitle.setWidth("100%");

        this.mainUI = UI.getCurrent();
        setResponsive(true);
        setWidth("90%");
        setHeight("90%");

        Label titleLbl = new Label(empresaId + " " + empresaNombre + " FACTURA VENTA");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h2_custom");

        layoutTitle.addComponent(titleLbl);
        layoutTitle.setComponentAlignment(titleLbl, Alignment.BOTTOM_RIGHT);

        mainLayout.addComponent(layoutTitle);

        System.out.println("Hancho Ventana" + mainUI.getPage().getBrowserWindowWidth());

        setContent(mainLayout);

        crearComponentes();

        mostrarFacturadoMes();
    }

    public void crearComponentes() {
        createDocumentHeader();
        createDocumentDetail();
        createDocumentFoother();
    }

    private void createDocumentHeader() {
        headerLayout = new VerticalLayout();
        headerLayout.setWidth("100%");
        headerLayout.setSpacing(true);

        HorizontalLayout firstLineHeaderLayout = new HorizontalLayout();
        firstLineHeaderLayout.setSizeFull();
        firstLineHeaderLayout.setSpacing(true);
        firstLineHeaderLayout.setMargin(new MarginInfo(false, true, false, true));
        firstLineHeaderLayout.addStyleName("rcorners3");

        HorizontalLayout secondLineHaderLayout = new HorizontalLayout();
        secondLineHaderLayout.setSizeFull();
        secondLineHaderLayout.setSpacing(true);
        secondLineHaderLayout.setMargin(new MarginInfo(false, true, false, true));
        secondLineHaderLayout.addStyleName("rcorners3");

        headerLayout.addComponents(firstLineHeaderLayout, secondLineHaderLayout);

        mainLayout.addComponents(headerLayout);
        mainLayout.setComponentAlignment(headerLayout, Alignment.TOP_CENTER);

        tipoFacturaVentaCbx = new ComboBox("Venta de :");
        tipoFacturaVentaCbx.setWidth("100%");
//        if(((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId().equals("11")) {
        if(((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyRegimen().equals("EXENTA")) {
            tipoFacturaVentaCbx.addItem("NORMAL");
            tipoFacturaVentaCbx.addItem("Factura Anticipos CBJ");
            tipoFacturaVentaCbx.select("NORMAL");
        }
        else {
            tipoFacturaVentaCbx.addItem("NORMAL");
            tipoFacturaVentaCbx.addItem("CASA");
            tipoFacturaVentaCbx.addItem("VILLA");
            tipoFacturaVentaCbx.addItem("APARTAMENTO");
            tipoFacturaVentaCbx.addItem("ACCION");
            tipoFacturaVentaCbx.select("CASA");
        }
        tipoFacturaVentaCbx.setInvalidAllowed(false);
        tipoFacturaVentaCbx.setNewItemsAllowed(false);
        tipoFacturaVentaCbx.setTextInputAllowed(false);
        tipoFacturaVentaCbx.addValueChangeListener((event) -> {
            if(tipoFacturaVentaCbx.getValue().equals("Factura Anticipos CBJ")) {
                FacturarAnticiposForm facturarAnticiposForm = new FacturarAnticiposForm();
                UI.getCurrent().addWindow(facturarAnticiposForm);
                facturarAnticiposForm.center();
            }
        });

        centroCostoCbx = new ComboBox("Centro Costo");
        centroCostoCbx.setWidth("100%");
        centroCostoCbx.setDescription("Centro de costo");
        centroCostoCbx.setTextInputAllowed(false);
//        centroCostoCbx.setInvalidAllowed(true);
        centroCostoCbx.setNullSelectionAllowed(true);
        llenarComboCentroCosto();

        fechaDt = new DateField("Fecha: ");
        fechaDt.setWidth("100%");
        fechaDt.setDateFormat("dd/MM/yyyy");
        fechaDt.setValue(new java.util.Date());

        fechaHoy = fechaDt.getValue();

        clienteCbx = new ComboBox("Cliente : ");
        clienteCbx.setWidth("100%");
        clienteCbx.setFilteringMode(FilteringMode.CONTAINS);
        clienteCbx.addContainerProperty(NIT_PROPERTY, String.class, "NO TIENE NIT");
        clienteCbx.addContainerProperty(NOMBRESINCODIGO_PROPERTY, String.class, "");
        clienteCbx.addContainerProperty(RETIENEISR_PROPERTY, String.class, "0");
        clienteCbx.addContainerProperty(RETIENEIVA_PROPERTY, String.class, "0");
        clienteCbx.setInvalidAllowed(false);
        clienteCbx.setNewItemsAllowed(false);
        clienteCbx.addValueChangeListener(event -> {
                    if (clienteCbx.getValue() != null) {
                        nitClienteTxt.setValue(clienteCbx.getContainerProperty(clienteCbx.getValue(), NIT_PROPERTY).getValue().toString());
                        retieneIsrLbl.setValue((String.valueOf(clienteCbx.getContainerProperty(clienteCbx.getValue(), RETIENEISR_PROPERTY).getValue()).equals("1")) ? "RETIENE ISR" : "");
                        retieneIvaLbl.setValue((String.valueOf(clienteCbx.getContainerProperty(clienteCbx.getValue(), RETIENEIVA_PROPERTY).getValue()).equals("1")) ? "RETIENE IVA" : "");
                    }
                }
        );

        nitClienteTxt = new TextField("Nit : ");
        nitClienteTxt.setWidth("100%");

        nit_cui_passCbx = new ComboBox("NIT, DPI o Pasaporte: ");
        nit_cui_passCbx.setDescription("Que ID se usará para indentificar al Cliente, \n\t- NIT \n\t- DPI \n\t- Pasaporte");
        nit_cui_passCbx.setWidth("100%");
//        if(((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId().equals("11")) {
        if(((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyRegimen().equals("EXENTA")) {
            nit_cui_passCbx.addItem("NIT");
            nit_cui_passCbx.addItem("DPI");
            nit_cui_passCbx.addItem("Pasaporte");
        }
        else {
            nit_cui_passCbx.addItem("NIT");
        }
        nit_cui_passCbx.select("NIT");
        nit_cui_passCbx.addValueChangeListener(event -> {
            nitClienteTxt.setCaption(nit_cui_passCbx.getValue() + " :");
        });

        retieneIsrLbl = new Label();
        retieneIsrLbl.setDescription("El cliente es Agente Retenedor del ISR");
        retieneIvaLbl = new Label();
        retieneIvaLbl.setDescription("El cliente es Agente Retenedor del IVA");

        exentaChb = new CheckBox("Exenta IVA");
        exentaChb.setDescription("Factura exenta del IVA");
        exentaChb.setImmediate(true);
        exentaChb.setValue(((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyRegimen().toUpperCase().equals("EXENTA"));
        exentaChb.addValueChangeListener(event -> {
            if((Boolean)event.getProperty().getValue()) {
                ivaTxt.setValue(0.00);
            }
            else {
                calcularMontos();
            }
        });
//        exentaChb.setReadOnly(true);

        firstLineHeaderLayout.addComponents(tipoFacturaVentaCbx,centroCostoCbx,fechaDt, clienteCbx, nitClienteTxt, nit_cui_passCbx, retieneIsrLbl, retieneIvaLbl, exentaChb);
        firstLineHeaderLayout.setComponentAlignment(tipoFacturaVentaCbx, Alignment.MIDDLE_LEFT);
        firstLineHeaderLayout.setComponentAlignment(centroCostoCbx, Alignment.MIDDLE_LEFT);
        firstLineHeaderLayout.setComponentAlignment(fechaDt, Alignment.MIDDLE_LEFT);
        firstLineHeaderLayout.setComponentAlignment(clienteCbx, Alignment.MIDDLE_LEFT);
        firstLineHeaderLayout.setComponentAlignment(nitClienteTxt, Alignment.MIDDLE_LEFT);
        firstLineHeaderLayout.setComponentAlignment(nit_cui_passCbx, Alignment.MIDDLE_LEFT);
        firstLineHeaderLayout.setComponentAlignment(retieneIsrLbl, Alignment.BOTTOM_LEFT);
        firstLineHeaderLayout.setComponentAlignment(retieneIvaLbl, Alignment.BOTTOM_LEFT);
        firstLineHeaderLayout.setComponentAlignment(exentaChb, Alignment.BOTTOM_RIGHT);

        firstLineHeaderLayout.setExpandRatio(tipoFacturaVentaCbx, 1.5f);
        firstLineHeaderLayout.setExpandRatio(centroCostoCbx, 1.5f);
        firstLineHeaderLayout.setExpandRatio(fechaDt, 1.5f);
        firstLineHeaderLayout.setExpandRatio(clienteCbx, 3.5f);
        firstLineHeaderLayout.setExpandRatio(nitClienteTxt, 2.0f);
        firstLineHeaderLayout.setExpandRatio(nit_cui_passCbx, 1.0f);
        firstLineHeaderLayout.setExpandRatio(retieneIsrLbl, 0.5f);
        firstLineHeaderLayout.setExpandRatio(retieneIvaLbl, 0.5f);
        firstLineHeaderLayout.setExpandRatio(exentaChb, 1.0f);

        serieTxt = new TextField("Serie : ");
        serieTxt.setWidth("12em");
        serieTxt.addStyleName("mayusculas");

        numeroTxt = new TextField("Número : ");
        numeroTxt.setWidth("12em");

        monedaCbx = new ComboBox("Moneda :");
        monedaCbx.setWidth("12em");
        monedaCbx.addItem("QUETZALES");
        monedaCbx.addItem("DOLARES");
        monedaCbx.select("QUETZALES");
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

        tasaCambioTxt = new NumberField("Tipo de Cambio : ");
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
        tasaCambioTxt.setWidth("5em");
        tasaCambioTxt.setValue(1.00);

        modoOg = new ToggleSwitch("Agregar", "Crear", event -> {
            boolean value = (boolean) event.getProperty().getValue();
            numeroTxt.setEnabled(!value);
            serieTxt.setEnabled(!value);
        });

        if(((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyFelUser().trim().isEmpty()){
            modoOg.setValue(false);
        }else{
            modoOg.setValue(true);
        }

        totalFacturadoMesTxt = new NumberField("<b>Total Facturado Mes<B>");
        totalFacturadoMesTxt.setDecimalAllowed(true);
        totalFacturadoMesTxt.setDecimalPrecision(2);
        totalFacturadoMesTxt.setMinimumFractionDigits(2);
        totalFacturadoMesTxt.setDecimalSeparator('.');
        totalFacturadoMesTxt.setDecimalSeparatorAlwaysShown(true);
        totalFacturadoMesTxt.setGroupingUsed(true);
        totalFacturadoMesTxt.setGroupingSeparator(',');
        totalFacturadoMesTxt.setGroupingSize(3);
        totalFacturadoMesTxt.setImmediate(true);
        totalFacturadoMesTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        totalFacturadoMesTxt.setWidth("7em");
        totalFacturadoMesTxt.setValue(0.00);
        totalFacturadoMesTxt.setCaptionAsHtml(true);

        secondLineHaderLayout.addComponents(serieTxt, numeroTxt, monedaCbx, tasaCambioTxt, modoOg, totalFacturadoMesTxt);
        secondLineHaderLayout.setComponentAlignment(serieTxt, Alignment.MIDDLE_LEFT);
        secondLineHaderLayout.setComponentAlignment(numeroTxt, Alignment.MIDDLE_LEFT);
        secondLineHaderLayout.setComponentAlignment(monedaCbx, Alignment.MIDDLE_LEFT);
        secondLineHaderLayout.setComponentAlignment(tasaCambioTxt, Alignment.MIDDLE_LEFT);
        secondLineHaderLayout.setComponentAlignment(modoOg, Alignment.MIDDLE_RIGHT);
        secondLineHaderLayout.setComponentAlignment(totalFacturadoMesTxt, Alignment.MIDDLE_RIGHT);
    }

    public void llenarComboCentroCosto() {

        centroCostoCbx.addItem("0");
        centroCostoCbx.setItemCaption("0", "0");
        centroCostoCbx.select("0");

        queryString = " SELECT * FROM centro_costo";
        queryString += " WHERE IdProyecto = " + ((SopdiUI) mainUI).sessionInformation.getStrProjectId();
        queryString += " AND Inhabilitado = 0";
        queryString += " AND IdEmpresa = " + empresaId;

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                do {
                    centroCostoCbx.addItem(rsRecords.getString("CodigoCentroCosto"));
                    centroCostoCbx.setItemCaption(rsRecords.getString("CodigoCentroCosto"), rsRecords.getString("CodigoCentroCosto") );
                } while (rsRecords.next());
            }

        } catch (Exception ex1) {
            System.out.println("Error al llenar combo centro costo: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    private void createDocumentDetail() {
        HorizontalLayout detailLayout = new HorizontalLayout();
        detailLayout.setWidth("100%");
        detailLayout.setSpacing(true);
        detailLayout.setMargin(false);
//        detailLayout.setMargin(new MarginInfo(false, true, false, true));

        VerticalLayout centralVerticalLayout = new VerticalLayout();
        centralVerticalLayout.setWidth("100%");
        centralVerticalLayout.setSpacing(true);
//        centralVerticalLayout.setMargin(new MarginInfo(false, true, false, false));

        detailLayout.addComponent(centralVerticalLayout);
        detailLayout.setComponentAlignment(centralVerticalLayout, Alignment.TOP_CENTER);

        mainLayout.addComponents(detailLayout);

    // --- Agregar los productos
        for(int i = 0; i < TOTAL_PRODUCTOS; i++){
            // Crear Horizontal Layout del ProductoNota
            HorizontalLayout layoutHorizontal = new HorizontalLayout();
            layoutHorizontal.setResponsive(true);
            layoutHorizontal.setSpacing(true);

            // Elementos
            NumberField cantidadTxt = new NumberField();    // Cantidad
            TextField tipoDocumento = new TextField();      // Tipo Documento (Servicio | producto)
            ComboBox cuentaContableCbx = new ComboBox();    // Cuenta Contable
            ComboBox productoCbx = new ComboBox();          // ProductoNota
            TextField referenciaTxt = new TextField();      // Referencia
            NumberField haberTxt = new NumberField();       // Monto


            // CANTIDAD
            if(i == 0) cantidadTxt.setCaption("CANT.");
            cantidadTxt.setDecimalAllowed(true);
            cantidadTxt.setDecimalPrecision(0);
            cantidadTxt.setMinimumFractionDigits(0);
            cantidadTxt.setValue(1d);
            cantidadTxt.setImmediate(true);
            cantidadTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);
            cantidadTxt.setWidth("5em");
            cantidadTxt.addValueChangeListener(event -> {
                calcularMontos();
            });

            cantidadlist.add(cantidadTxt);

            // Tipo Documento
            if (i == 0) tipoDocumento.setCaption("Tipo :");
            tipoDocumento.setWidth("3em");
            tipoDocumento.setEnabled(false);

            tipoList.add(tipoDocumento);

            // ProductoNota
            if(i == 0) productoCbx.setCaption("PRODUCTO O SERVICIO");
            productoCbx.setWidth("16em");
            productoCbx.addValueChangeListener((event) -> {
                System.out.println(mapaProductoEmpresa.get(productoCbx.getValue()));
                cuentaContableCbx.select(mapaProductoEmpresa.get(productoCbx.getValue()).getIdNomenclatura());
                tipoDocumento.setValue(mapaProductoEmpresa.get(productoCbx.getValue()).getTipo().substring(0, 1));
                haberTxt.setEnabled(true);
                referenciaTxt.setEnabled(true);
            });

            productoList.add(productoCbx);

            // Cuenta Contable
            if (i == 0) cuentaContableCbx.setCaption("CUENTA CONTABLE: ");
            cuentaContableCbx.setWidth("16em");
            cuentaContableCbx.setFilteringMode(FilteringMode.CONTAINS);
            cuentaContableCbx.setInvalidAllowed(false);
            cuentaContableCbx.setNewItemsAllowed(false);
            cuentaContableCbx.addContainerProperty(DESCRIPCION_PROPERTY, String.class, "");
            cuentaContableCbx.setEnabled(false);

            cuentaContableList.add(cuentaContableCbx);

            // Referencia
            if (i == 0) referenciaTxt.setCaption("Referencia :");
            referenciaTxt.setWidth("16em");
            referenciaTxt.setEnabled(false);

            referenciaList.add(referenciaTxt);

            // Monto
            if (i == 0) haberTxt.setCaption("Monto : ");
            haberTxt.setDecimalAllowed(true);
            haberTxt.setDecimalPrecision(2);
            haberTxt.setMinimumFractionDigits(2);
            haberTxt.setDecimalSeparator('.');
            haberTxt.setDecimalSeparatorAlwaysShown(true);
            haberTxt.setGroupingUsed(true);
            haberTxt.setGroupingSeparator(',');
            haberTxt.setGroupingSize(3);
            haberTxt.setImmediate(true);
            haberTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
            haberTxt.setWidth("7em");
            haberTxt.setValue(0.00);
            haberTxt.setEnabled(false);
            haberTxt.addValueChangeListener(event -> {
                calcularMontos();
            });

            montoList.add(haberTxt);

            //
            layoutHorizontal.addComponents(cantidadTxt, tipoDocumento, productoCbx, cuentaContableCbx, referenciaTxt, haberTxt);

            centralVerticalLayout.addComponent(layoutHorizontal);
            centralVerticalLayout.setComponentAlignment(layoutHorizontal, Alignment.MIDDLE_CENTER);
        }

        llenarComboProductoServicio();
        llenarComboCuentaContable();
        llenarComboCliente();

        // Espacio en Blanco
        HorizontalLayout whitespace = new HorizontalLayout();
        whitespace.setResponsive(true);
        whitespace.setSpacing(true);
        centralVerticalLayout.addComponent(whitespace);
        centralVerticalLayout.setComponentAlignment(whitespace, Alignment.MIDDLE_CENTER);
    }

    private void createDocumentFoother() {
        HorizontalLayout footherLayout = new HorizontalLayout();
        footherLayout.setWidth("100%");
        footherLayout.setMargin(false);

        VerticalLayout depositosLayout = new VerticalLayout();
        depositosLayout.setSpacing(true);
        depositosLayout.setWidth("100%");
        depositosLayout.setMargin(new MarginInfo(false, true, false, true));

        VerticalLayout montosLayout = new VerticalLayout();
        montosLayout.setSpacing(true);
        montosLayout.setWidth("100%");
        montosLayout.setMargin(new MarginInfo(false, true, false, true));

        footherLayout.addComponents(depositosLayout, montosLayout);
        footherLayout.setComponentAlignment(depositosLayout, Alignment.TOP_LEFT);
        footherLayout.setComponentAlignment(montosLayout, Alignment.MIDDLE_RIGHT);
        footherLayout.setExpandRatio(depositosLayout, 3.0f);
        footherLayout.setExpandRatio(montosLayout, 1.5f);

        mainLayout.addComponents(footherLayout);

        montoTxt = new NumberField();
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

        ivaTxt = new NumberField("IVA : ");
        ivaTxt.setDecimalAllowed(true);
        ivaTxt.setDecimalPrecision(2);
        ivaTxt.setMinimumFractionDigits(2);
        ivaTxt.setDecimalSeparator('.');
        ivaTxt.setDecimalSeparatorAlwaysShown(true);
        ivaTxt.setValue(0d);
        ivaTxt.setGroupingUsed(true);
        ivaTxt.setGroupingSeparator(',');
        ivaTxt.setGroupingSize(3);
        ivaTxt.setImmediate(true);
        ivaTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);

        if(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getIvaPorPagar() == null) {
            ivaTxt.setVisible(false);
        }

        isrTxt = new NumberField("ISR : ");
        isrTxt.setDecimalAllowed(true);
        isrTxt.setDecimalPrecision(2);
        isrTxt.setMinimumFractionDigits(2);
        isrTxt.setDecimalSeparator('.');
        isrTxt.setDecimalSeparatorAlwaysShown(true);
        isrTxt.setValue(0d);
        isrTxt.setGroupingUsed(true);
        isrTxt.setGroupingSeparator(',');
        isrTxt.setGroupingSize(3);
        isrTxt.setImmediate(true);
        isrTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);

        grabarBtn = new Button("Grabar");
        grabarBtn.setIcon(FontAwesome.SAVE);
        grabarBtn.setStyleName(ValoTheme.BUTTON_PRIMARY);
        grabarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                insertTablaFactura();
            }
        });

        montosLayout.addComponents(montoTxt, ivaTxt, isrTxt, grabarBtn);
        montosLayout.setComponentAlignment(montoTxt, Alignment.TOP_RIGHT);
        montosLayout.setComponentAlignment(ivaTxt, Alignment.TOP_RIGHT);
        montosLayout.setComponentAlignment(isrTxt, Alignment.TOP_RIGHT);
        montosLayout.setComponentAlignment(grabarBtn, Alignment.BOTTOM_RIGHT);

    }

    public void llenarComboCliente() {

        queryString = " SELECT * FROM proveedor_empresa ";
        queryString += " WHERE Inhabilitado = 0 ";
        queryString += " AND EsCliente = 1";
        queryString += " AND IdEmpresa = " + empresaId;
        queryString += " ORDER BY Nombre";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) { //  encontrado
                clienteCbx.addItem(rsRecords.getString("IDProveedor"));
                clienteCbx.setItemCaption(rsRecords.getString("IDProveedor"), "(" + rsRecords.getString("IDProveedor") + ") " + rsRecords.getString("Nombre"));
                clienteCbx.getItem(rsRecords.getString("IDProveedor")).getItemProperty(NIT_PROPERTY).setValue(rsRecords.getString("NIT"));
                clienteCbx.getItem(rsRecords.getString("IDProveedor")).getItemProperty(NOMBRESINCODIGO_PROPERTY).setValue(rsRecords.getString("Nombre"));
                clienteCbx.getItem(rsRecords.getString("IDProveedor")).getItemProperty(RETIENEISR_PROPERTY).setValue(rsRecords.getString("EsAgenteRetenedorIsr"));
                clienteCbx.getItem(rsRecords.getString("IDProveedor")).getItemProperty(RETIENEIVA_PROPERTY).setValue(rsRecords.getString("EsAgenteRetenedorIva"));
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar de clientes (tabla proveedores) " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void mostrarFacturadoMes() {

        totalFacturadoMesTxt.setReadOnly(false);
        totalFacturadoMesTxt.setValue(0.00);

        queryString = " SELECT SUM(DEBE - HABER) as TOTALFACTURADO ";
        queryString += " FROM contabilidad_partida ";
        queryString += " WHERE IdNomenclatura = " + ((SopdiUI) mainUI).cuentasContablesDefault.getClientes();
        queryString += " AND TipoDocumento IN ('FACTURA VENTA', 'RECIBO CONTABLE',  'RECIBO CONTABLE VENTA')";
        queryString += " AND IdEmpresa = " + empresaId;
        queryString += " AND Extract(YEAR_MONTH FROM Fecha) = " + Utileria.getFechaYYYYMM(new java.util.Date());

        Logger.getLogger(this.getClass().getName()).log(Level.INFO,"QUERY BUSCAR ACUMULADO FACTURAS VENTA DEl MES : " + queryString);

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                totalFacturadoMesTxt.setValue(rsRecords.getDouble("TOTALFACTURADO"));
            }

        } catch (Exception ex) {
            System.out.println("Error al listar tabla de DOCUMENTOS : " + ex);
            Notification.show("ERROR AL LEER DOCUMENTOS VENTA PARA REGISTRAR PAGOS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }

        totalFacturadoMesTxt.setReadOnly(true);
    }

    private void calcularMontos() {
        Double total = 0.0d;
        if(montoTxt == null) {
            return;
        }

        montoTxt.setValue(0.0);
        ivaTxt.setValue(0.0);
        isrTxt.setValue(0.0);

        netoMontoTotal = 0.0;
        ivaMontoTotal = 0.0;

        for (int i = 0; i < TOTAL_PRODUCTOS; i++){
            if(cantidadlist.get(i).getDoubleValueDoNotThrow() > 0) {
                if(montoList.get(i).getDoubleValueDoNotThrow() > 0) {
                    total = total + (cantidadlist.get(i).getDoubleValueDoNotThrow() * montoList.get(i).getDoubleValueDoNotThrow());
                    netoMontoTotal += new Double(Utileria.format(cantidadlist.get(i).getDoubleValueDoNotThrow() * montoList.get(i).getDoubleValueDoNotThrow() / 1.12));
                    ivaMontoTotal += new Double(Utileria.format((cantidadlist.get(i).getDoubleValueDoNotThrow() * montoList.get(i).getDoubleValueDoNotThrow() / 1.12) * 0.12));
                }
            }
        }

        montoTxt.setValue(numberFormat.format(total));


        double base = Double.valueOf(Utileria.format((montoTxt.getDoubleValueDoNotThrow() / 1.12)));

        if(exentaChb.getValue()) {
            base  = montoTxt.getDoubleValueDoNotThrow();
        }
        else {
            ivaTxt.setValue(Double.valueOf(Utileria.format((base * 0.12))));
        }

        if(base <= 30000.00) {
            isrTxt.setValue(Double.valueOf(Utileria.format((base * 0.05))));
        }
        else {
            double isr1 = 30000.00 * 0.05;
            double isr2 = (base - 30000.00) * 0.07;
            isrTxt.setValue(Double.valueOf(Utileria.format(isr1 + isr2)));
        }

        if(!((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyRegimen().equals("Opcional Simplificado sobre Ingresos de Actividades Lucrativas")) {
            isrTxt.setValue(0.00);
        }

    }

    public void llenarComboProductoServicio() {
        queryString = "SELECT * from producto_venta_empresa ";
        queryString += "WHERE IdEmpresa = " + empresaId;
        queryString += "AND Especial = 0 ";

//Logger.getLogger(this.getClass().getName()).log(Level.INFO, queryString);

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) { //  encontrado
//Logger.getLogger(this.getClass().getName()).log(Level.INFO, rsRecords.getString("N5"));
                Producto p = new Producto(rsRecords.getString("IdProducto"), rsRecords.getString("IdNomenclatura"), rsRecords.getString("ExentoIVA"), rsRecords.getString("Tipo"));
                mapaProductoEmpresa.put(rsRecords.getString("IdProducto"), p);

                for (ComboBox producto : productoList){
                    producto.addItem(rsRecords.getString("IdProducto"));
                    producto.setItemCaption(rsRecords.getString("IdProducto"), rsRecords.getString("NombreProducto"));
                }
            }

            cuentaContableList.get(0).select(((SopdiUI) mainUI).cuentasContablesDefault.getVentas());

        } catch (Exception ex1) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error al combo cuentas contables: ", ex1);
            ex1.printStackTrace();
        }
    }

    public void llenarComboCuentaContable() {

        queryString = "SELECT * FROM contabilidad_nomenclatura_empresa cn, producto_venta_empresa pve ";
        queryString += "WHERE cn.FiltrarIngresoDocumentos = 'S' ";
        queryString += "AND cn.IdNomenclatura = pve.IdNomenclatura ";
        queryString += "AND Estatus = 'HABILITADA' ";
        queryString += "AND UPPER(N1) = 'INGRESOS' ";
        queryString += "AND pve.Especial = 0 ";
        queryString += "AND cn.IdEmpresa = " + empresaId;
        queryString += "ORDER BY N5";

//Logger.getLogger(this.getClass().getName()).log(Level.INFO, queryString);

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) { //  encontrado
//Logger.getLogger(this.getClass().getName()).log(Level.INFO, rsRecords.getString("N5"));
                if(rsRecords.getString("cn.Tipo").equals("SERVICIO") || rsRecords.getString("cn.Tipo").equals("PRODUCTO")  || rsRecords.getString("cn.Tipo").equals("VENTA") || rsRecords.getString("cn.Tipo").equals("N/A")) {

                    for (ComboBox cuentaContable : cuentaContableList){
                        cuentaContable.addItem(rsRecords.getString("IdNomenclatura"));
                        cuentaContable.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));
                        cuentaContable.getContainerProperty(rsRecords.getString("IdNomenclatura"), DESCRIPCION_PROPERTY).setValue(rsRecords.getString("N5"));
                    }

                }
            }

            cuentaContableList.get(0).select(((SopdiUI) mainUI).cuentasContablesDefault.getVentas());

        } catch (Exception ex1) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error al combo cuentas contables: ", ex1);
            ex1.printStackTrace();
        }
    }

    public void insertTablaFactura() {
        if (datosValidos()) {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "DATOS VALIDOS OK!");
            if (!((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyFelUser().trim().isEmpty() && modoOg.getValue()) {
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "ES FEL!");
                if (documentoCertificado()) {
                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, "DOCUMENTO CERTIFICDADO FEL OK!");
                    Notification.show("DOCUMENTO CERTIFICADO FEL OK!", Notification.Type.HUMANIZED_MESSAGE);
                } else {
                    return;
                }
            }
            insertarPartidas();
        }
    }
    private boolean datosValidos() {
        try {
            Utileria utileria = new Utileria();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

            Date fechaInicial = dateFormat.parse(utileria.getFecha());
            Date fechaFinal = dateFormat.parse(utileria.getFechaYYYYMMDD(fechaDt.getValue()));

            int dias = (int) ((fechaInicial.getTime() - fechaFinal.getTime()) / 86400000);

//            System.out.println("Hay " + dias + " dias de diferencia");

            if (dias > 30) {

                if (((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserToken().isEmpty()) {
                    ValidarTokenForm validarTokenForm = new ValidarTokenForm(false);
                    UI.getCurrent().addWindow(validarTokenForm);
                    validarTokenForm.center();
                    return false;
                } else {
                    variableTemp = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserToken();
                    ((SopdiUI) UI.getCurrent()).sessionInformation.setStrUserToken("");
                }
            }

        } catch (Exception e) {
            System.out.println("Error validar fechas " + e);
            e.printStackTrace();
            return false;
        }

        if (((SopdiUI) UI.getCurrent()).esMesCerrado(empresaId, Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()))) {
            Notification.show("La fecha del documento no puede ser de un mes ya cerrado contablemente, revise!", Notification.Type.WARNING_MESSAGE);
            fechaDt.focus();
            return false;
        }
        if (!((SopdiUI) UI.getCurrent()).esPrimerMesAbierto(empresaId, Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()))) {
            Notification.show("El mes abierto a operaciones es : " + ((SopdiUI) UI.getCurrent()).primerMesAbierto(empresaId), Notification.Type.WARNING_MESSAGE);
            fechaDt.focus();
            return false;
        }
        if (((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyFelUser().trim().isEmpty()) {
            if (this.serieTxt.getValue().trim().isEmpty()) {
                Notification.show("Por favor ingrese la serie del documento.", Notification.Type.WARNING_MESSAGE);
                serieTxt.focus();
                return false;
            }
            if (this.numeroTxt.getValue().trim().isEmpty()) {
                Notification.show("Por favor ingrese el número de documento.", Notification.Type.WARNING_MESSAGE);
                numeroTxt.focus();
                return false;
            }
        }
        if (clienteCbx.getValue() == null || clienteCbx.getValue().equals("0")) {
            Notification.show("Por favor ingrese el cliente.", Notification.Type.WARNING_MESSAGE);
            clienteCbx.focus();
            return false;
        }
        if (nitClienteTxt.isEmpty()) {
            Notification.show("Por favor ingrese el Nit del cliente.", Notification.Type.WARNING_MESSAGE);
            nitClienteTxt.focus();
            return false;
        }
        if (monedaCbx.getValue() == null) {
            Notification.show("Por favor ingrese el tipo de moneda.", Notification.Type.WARNING_MESSAGE);
            monedaCbx.focus();
            return false;
        }

        if (this.montoTxt.getDoubleValueDoNotThrow() == 0) {
            Notification.show("Por favor ingrese el monto de la factura.", Notification.Type.WARNING_MESSAGE);
            montoTxt.focus();
            return false;
        }

        queryString = " Select * from contabilidad_partida";
        queryString += " Where SerieDocumento  = '" + serieTxt.getValue().toUpperCase().trim() + "'";
        queryString += " And   NumeroDocumento = '" + numeroTxt.getValue().toUpperCase().trim() + "'";
        if(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getIvaPorPagar() != null) {
            queryString += " And   TipoDocumento = 'FACTURA VENTA'";
        }
        else {
            queryString += " And   TipoDocumento = 'RECIBO CONTABLE VENTA'";
        }
        queryString += " And   IdEmpresa = " + empresaId;

        try {
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                Notification.show("Este documento ya fué ingresado, revise.!. Empresa = " + rsRecords.getString("IdEmpresa") + " Fecha : " + rsRecords.getString("Fecha"), Notification.Type.WARNING_MESSAGE);
                numeroTxt.focus();
                return false;
            }
        } catch (Exception ex1) {
            System.out.println("Error al buscar documento : " + ex1.getMessage());
            ex1.printStackTrace();
            return false;
        }
        return true;
    }

//<WL5G3N3:address location="https://pdte.guatefacturas.com:443/webservices63/feltest/Guatefac"/>

    private boolean documentoCertificado() {

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

        if(nit_cui_passCbx.getValue().equals("NIT")){ // Si es NIT
            xmlRequest +=       "<NITReceptor>" + nitClienteTxt.getValue().replaceAll("-", "") + "</NITReceptor>\n" +
                                "<Nombre>" + clienteCbx.getContainerProperty(clienteCbx.getValue(), NOMBRESINCODIGO_PROPERTY).getValue() + "</Nombre>\n" +
                                "<Direccion>" + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyBillingDirection() + "</Direccion>\n";

        }else if(nit_cui_passCbx.getValue().equals("DPI")){ //  Si es DPI o CUI
            xmlRequest +=       "<NITReceptor>" + nitClienteTxt.getValue() + "</NITReceptor>\n" +
                                "<Nombre>" + clienteCbx.getContainerProperty(clienteCbx.getValue(), NOMBRESINCODIGO_PROPERTY).getValue() + "</Nombre>\n" +
                                "<Direccion>" + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyBillingDirection() + "</Direccion>\n";

        }else if(nit_cui_passCbx.getValue().equals("Pasaporte")){ // Si es Pasaporte
            xmlRequest +=       "<NITReceptor>" + nitClienteTxt.getValue() + "</NITReceptor>\n" +
                                "<Nombre>" + nitClienteTxt.getValue() + "</Nombre>\n" +
                                "<Direccion>" + nitClienteTxt.getValue() + "</Direccion>\n";

        }else {
            return false;

        }
            xmlRequest +=   "</Receptor>\n" +
                            "<InfoDoc>\n" +
                                "<TipoVenta>S</TipoVenta>\n" +
                                "<DestinoVenta>1</DestinoVenta>\n" +
                                "<Fecha>" + Utileria.getFechaDDMMYYYY(fechaDt.getValue()) + "</Fecha>\n";
        /* Dolares */           if(monedaCbx.getValue().equals("DOLARES")) {
                                    xmlRequest +=   "<Moneda>2</Moneda>\n"+
                                                    "<Tasa>" + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow())+ "</Tasa>\n" ;
                                }
        /* Quetzales */         else {
                                    xmlRequest +=   "<Moneda>1</Moneda>\n" +
                                                    "<Tasa>1</Tasa>\n" ;
                                }
        xmlRequest +=
                                "<Referencia>" + new Utileria().getReferencia() + "</Referencia>\n" +
                                "<NumeroAcceso></NumeroAcceso>\n" +
                                "<SerieAdmin>" + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanySmallName() + "</SerieAdmin>\n" +
                                "<NumeroAdmin>" + new Utileria().getReferencia() + "</NumeroAdmin>\n" +
                                "<Reversion>N</Reversion>\n" +
                            "</InfoDoc>\n" +
        /* Totales */       "<Totales>\n" ;

        // EXENTO
        if(exentaChb.getValue()) {
            xmlRequest +=       "<Bruto>" + Utileria.format(montoTxt.getDoubleValueDoNotThrow()) + "</Bruto>\n" +
                                "<Descuento>0.00</Descuento>\n" +
                                "<Exento>" + Utileria.format(montoTxt.getDoubleValueDoNotThrow()) + "</Exento>\n" +
                                "<Otros>0.00</Otros>\n" +
                                "<Neto>0.00</Neto>\n" +
                                "<Isr>0.00</Isr>\n" +
                                "<Iva>0.00</Iva>\n" +
                                "<Total>" + Utileria.format(montoTxt.getDoubleValueDoNotThrow()) + "</Total>\n";
        }
        else {
            xmlRequest +=       "<Bruto>" + Utileria.format(montoTxt.getDoubleValueDoNotThrow()) + "</Bruto>\n" +
                                "<Descuento>0.00</Descuento>\n" +
                                "<Exento>0.00</Exento>\n" +
                                "<Otros>0.00</Otros>\n" +
                                "<Neto>" + Utileria.format(netoMontoTotal) + "</Neto>\n" +
                                "<Isr>0.00</Isr>\n" +
                                "<Iva>" + Utileria.format(ivaMontoTotal) + "</Iva>\n" +
                                "<Total>" + Utileria.format(montoTxt.getDoubleValueDoNotThrow()) + "</Total>\n";
        }
        xmlRequest +=       "</Totales>\n" +
                            "<DatosAdicionales>\n";
        if(nit_cui_passCbx.getValue().equals("NIT")){ // Si es NIT
            xmlRequest +=       "<TipoReceptor>4</TipoReceptor>\n"; // 2: CUI | 3: ID_Extranjero | 4: NIT

        }else if(nit_cui_passCbx.getValue().equals("DPI")){ //  Si es DPI o CUI
            xmlRequest +=       "<TipoReceptor>2</TipoReceptor>\n"; // 2: CUI | 3: ID_Extranjero | 4: NIT

        }else if(nit_cui_passCbx.getValue().equals("Pasaporte")){ // Si es Pasaporte
            xmlRequest +=       "<TipoReceptor>3</TipoReceptor>\n"; // 2: CUI | 3: ID_Extranjero | 4: NIT

        }

        xmlRequest +=       "</DatosAdicionales>\n" +
                        "</Encabezado>\n" +
                                "<Detalles>\n";

    // --- Genrando ProductoNota o Servicio
        for (int i = 0; i < TOTAL_PRODUCTOS; i++){
            
            if(cantidadlist.get(i).getDoubleValueDoNotThrow() > 0 && productoList.get(i).getValue() != null && montoList.get(i).getDoubleValueDoNotThrow() > 0 ) {

                String producto = (String) productoList.get(i).getValue(); // Id del ProductoNota a Facturar
                Boolean exentoIva = mapaProductoEmpresa.get(producto).getExentoIva().equals("SI"); // Si el ProductoNota es Exento de IVA

                // Verificar que ambos sean exentos.
                if(exentaChb.getValue() != exentoIva) {
                    Notification.show("La Factura y los Productos no comparten SI son Exentas de IVA o NO, Verifique.", Notification.Type.WARNING_MESSAGE);
                    exentaChb.focus();
                    return false;
                }


                xmlRequest +=       "<Productos>\n";
                if(exentoIva) {
                    xmlRequest +=       "<ProductoNota>" + producto + "</ProductoNota>\n" +
                                        "<Descripcion>" + productoList.get(i).getItemCaption(producto) + " " + referenciaList.get(i).getValue().trim() + "</Descripcion>\n" +
                                        "<Medida>1</Medida>\n" +
                                        "<Cantidad>" + cantidadlist.get(i).getValue() + "</Cantidad>\n" +
                                        "<Precio>" + Utileria.format(montoList.get(i).getDoubleValueDoNotThrow()) + "</Precio>\n" +
                                        "<PorcDesc>0.00</PorcDesc>\n" +
                                        "<ImpBruto>" + Utileria.format(cantidadlist.get(i).getDoubleValueDoNotThrow() * montoList.get(i).getDoubleValueDoNotThrow()) + "</ImpBruto>\n" +
                                        "<ImpDescuento>0.00</ImpDescuento>\n" +
                                        "<ImpExento>" + Utileria.format(cantidadlist.get(i).getDoubleValueDoNotThrow() * montoList.get(i).getDoubleValueDoNotThrow()) + "</ImpExento>\n" +
                                        "<ImpNeto>0.00</ImpNeto>\n"+
                                        "<ImpOtros>0.00</ImpOtros>\n" +
                                        "<ImpIsr>0.00</ImpIsr>\n" +
                                        "<ImpIva>0.00</ImpIva>\n" +
                                        "<ImpTotal>" + Utileria.format(cantidadlist.get(i).getDoubleValueDoNotThrow() * montoList.get(i).getDoubleValueDoNotThrow()) + "</ImpTotal>\n" +
                                        "<productoDet>" + mapaProductoEmpresa.get(producto).getTipo().substring(0, 1) + "</productoDet>\n";
                }
                else {
                    xmlRequest +=       "<ProductoNota>" + producto + "</ProductoNota>\n" +
                                        "<Descripcion>" + productoList.get(i).getItemCaption(producto) + " " + referenciaList.get(i).getValue().trim() + "</Descripcion>\n" +
                                        "<Medida>1</Medida>\n" +
                                        "<Cantidad>" + cantidadlist.get(i).getValue() + "</Cantidad>\n" +
                                        "<Precio>" + Utileria.format(montoList.get(i).getDoubleValueDoNotThrow()) + "</Precio>\n" +
                                        "<PorcDesc>0.00</PorcDesc>\n" +
                                        "<ImpBruto>" + Utileria.format((cantidadlist.get(i).getDoubleValueDoNotThrow() * montoList.get(i).getDoubleValueDoNotThrow())) + "</ImpBruto>\n" +
                                        "<ImpDescuento>0.00</ImpDescuento>\n" +
                                        "<ImpExento>0.0</ImpExento>\n" +                                                                                                                                // Chapuz 7.7
                                        "<ImpNeto>" + Utileria.format((cantidadlist.get(i).getDoubleValueDoNotThrow() * montoList.get(i).getDoubleValueDoNotThrow()) / 1.12) + "</ImpNeto>\n";
                    xmlRequest +=       "<ImpOtros>0.00</ImpOtros>\n" +
                                        "<ImpIsr>0.00</ImpIsr>\n" +
                                        "<ImpIva>" + Utileria.format((((cantidadlist.get(i).getDoubleValueDoNotThrow() * montoList.get(i).getDoubleValueDoNotThrow()) / 1.12) * .12)) + "</ImpIva>\n" +
                                        "<ImpTotal>" + Utileria.format((cantidadlist.get(i).getDoubleValueDoNotThrow() * montoList.get(i).getDoubleValueDoNotThrow())) + "</ImpTotal>\n" +
                                        "<productoDet>" + mapaProductoEmpresa.get(producto).getTipo().substring(0, 1) + "</productoDet>\n";
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
                serieTxt.setValue(resultado.getChildNodes().item(0).getTextContent());          // Serie                | Se Usa en Partida
                numeroTxt.setValue(resultado.getChildNodes().item(1).getTextContent());         // Preimpreso           | Se Usa en Partida
                nombre = resultado.getChildNodes().item(2).getTextContent();                    // Nombre
                direccion = resultado.getChildNodes().item(3).getTextContent();                 // Direccion
                telefono = resultado.getChildNodes().item(4).getTextContent();                  // Telefono
                uuid = resultado.getChildNodes().item(5).getTextContent();                      // Numero Autorizacion  | Se Usa en Partida
                fechaYHoraCertificacion = resultado.getChildNodes().item(6).getTextContent();   // Referencia           | Se Usa en Partida

                obtenerFacturaPdf(serieTxt.getValue(), numeroTxt.getValue());

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

    private void obtenerFacturaPdf(String serie, String numero) {

        Utileria utileria = new Utileria();

        long fileSize = 0;
        byte[] ba1 = new byte[1024];
        int baLength;

        try {

            Thread.sleep(1000);

//                URL pdfFileUrl = new URL("https://dte.guatefacturas.com/reports/rwservlet?repfel&report=r65_2170&destype=cache&desformat=pdf&paramform=no&P_NIT_EMISOR=" + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyTaxId() + "&P_UUID=" + referencia);
            String credemciales = "P_NIT_EMISOR=" + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyTaxId() + "&P_SERIE=" + serie + "&P_NUMERO=" + numero + "&P_FECHA=" + utileria.getFechaSinFormato_v2(fechaDt.getValue());
            URL pdfFileUrl = new URL("https://dte.guatefacturas.com/reports/rwservlet?reportesfel&report=r65_0014&destype=cache&desformat=pdf&paramform=no&" + credemciales);
            System.out.println(((SopdiUI) UI.getCurrent()).enviromentsVars.getDtePath() +"\n_"+ serie + "\n_" + numero + ".pdf");
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

    private void insertarPartidas() {
        String fecha = Utileria.getFechaYYYYMMDD_1(fechaDt.getValue());
        String ultimoEncontado;
        String dia = fecha.substring(8, 10);
        String mes = fecha.substring(5, 7);
        String año = fecha.substring(0, 4);

        String codigoPartida = empresaId + año + mes + dia + "0";

        queryString = " SELECT codigoPartida FROM contabilidad_partida ";
        queryString += " WHERE codigoPartida LIKE '" + codigoPartida + "%'";
        queryString += " ORDER BY codigoPartida DESC ";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado                               

                ultimoEncontado = rsRecords.getString("codigoPartida").substring(12, 15);

                codigoPartida += String.format("%03d", (Integer.valueOf(ultimoEncontado) + 1));

            } else {
                codigoPartida += "001";
            }

        } catch (Exception ex1) {
            System.out.println("Error al buscar el ultimo codigoPartida" + ex1.getMessage());
            ex1.printStackTrace();
        }

        queryString = " INSERT INTO proveedor_cuentacorriente (IdEmpresa,IdProveedor, Fecha,";
        queryString += " TipoDocumento, SerieDocumento,NumeroDocumento, MonedaDocumento, ";
        queryString += " Monto, MontoQuetzales, TipoCambio ";
        queryString += ", IdUsuarioAutorizoPago,CreadoFechayHora,CreadoUsuario)";
        queryString += " VALUES(";
        queryString += empresaId;
        queryString += "," + clienteCbx.getValue();
        queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
        if(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getIvaPorPagar() != null) {
            queryString += ",'FACTURA VENTA'";
        }
        else {
            queryString += ",'RECIBO CONTABLE VENTA'";
        }
        queryString += ",UPPER('" + serieTxt.getValue().trim() + "')";
        queryString += ",'" + numeroTxt.getValue().trim() + "'";
        queryString += ",'" + monedaCbx.getValue() + "'";
        queryString += ", " + String.valueOf(montoTxt.getDoubleValueDoNotThrow());
        queryString += "," + Utileria.format(montoTxt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
        queryString += ", " + tasaCambioTxt.getValue();
        queryString += ", " + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
        queryString += ",'" + Utileria.getFechaYYYYMMDD_1(new java.util.Date()) + "'";
        queryString += ", " + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
        queryString += ")";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);
        } catch (Exception ex1) {
            System.out.println("Error al insertar proveedor cuentaCorriente : " + ex1.getMessage());
            ex1.printStackTrace();
        }

        queryString = " INSERT INTO contabilidad_partida (IdEmpresa, Estatus, CodigoPartida,CodigoCC, ";
        queryString += " TipoDocumento, TipoVenta, Fecha, IdProveedor, NitProveedor, NombreProveedor,";
        queryString += " SerieDocumento, NumeroDocumento, IdNomenclatura, MonedaDocumento, MontoDocumento, Debe, Haber,";
        queryString += " DebeQuetzales, HaberQuetzales, TipoCambio, Saldo, Descripcion, Referencia,IdCentroCosto, CodigoCentroCosto,";
        queryString += " CreadoUsuario, CreadoFechaYHora, Archivo, ArchivoTipo, ArchivoPeso, ArchivoNombre,";
        queryString += " UUID, FechaYHoraCertificacion, XmlRequest, XmlResponse, IdProducto";
        queryString += ")";
        queryString += " Values ";
        queryString += "(";
        queryString += empresaId;
        queryString += ",'INGRESADO'";
        queryString += ",'" + codigoPartida + "'";
        queryString += ",'" + codigoPartida + "'";
        if(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getIvaPorPagar() != null) {
            queryString += ",'FACTURA VENTA'";
        }
        else {
            queryString += ",'RECIBO CONTABLE VENTA'";
        }
        queryString += ",'" + String.valueOf(tipoList.get(0).getValue()) + "'";
        queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
        queryString += "," + clienteCbx.getValue();
        queryString += ",'" + nitClienteTxt.getValue() + "'";
        queryString += ",'" + String.valueOf(clienteCbx.getItem(clienteCbx.getValue()).getItemProperty(NOMBRESINCODIGO_PROPERTY).getValue()) + "'";
        queryString += ",UPPER('" + serieTxt.getValue().trim() + "')";
        queryString += ",'" + numeroTxt.getValue().trim() + "'";

        queryString += "," + ((SopdiUI) mainUI).cuentasContablesDefault.getClientes();

        queryString += ",'" + monedaCbx.getValue() + "'";
        queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow()); //MONTODOCUMENTO
        queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow()); //DEBE
        queryString += ",0.00"; //HABER
        queryString += "," + Utileria.format(montoTxt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
        queryString += ",0.00"; //HABER Q.
        queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
        queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow()); // SALDO
        if(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getIvaPorPagar() != null) {
            queryString += ",'FACTURA VENTA " + String.valueOf(clienteCbx.getItem(clienteCbx.getValue()).getItemProperty(NOMBRESINCODIGO_PROPERTY).getValue()) + "'";
        }
        else {
            queryString += ",'RECIBO CONTABLE VENTA " + String.valueOf(clienteCbx.getItem(clienteCbx.getValue()).getItemProperty(NOMBRESINCODIGO_PROPERTY).getValue()) + "'";
        }
        queryString += ",'NO'"; //referencia
        queryString += ",'" + centroCostoCbx.getItemCaption(centroCostoCbx.getValue()) + "'";
        queryString += "," + centroCostoCbx.getValue();
        queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
        queryString += ",current_timestamp";
        queryString += ",null"; //archivo
        queryString += ",'application/pdf'"; //archivo tipo
        queryString += ","  + (pdfFile != null ? pdfFile.length() : 0);    //archivo size
        queryString += ",'" + (pdfFile != null ? pdfFile.getAbsolutePath() : "") + "'";
        queryString += ",'" + uuid + "'";
        queryString += ",'" + fechaYHoraCertificacion + "'";
        queryString += ",'" + xmlRequest + "'";
        queryString += ",'" + xmlResponse + "'";
        queryString += ",null)"; // IdProducto

        for(int i = 0; i < TOTAL_PRODUCTOS; i++) {
            if (cuentaContableList.get(i).getValue() != null && montoList.get(i).getDoubleValueDoNotThrow() != 0) {
                queryString += ",(";
                queryString += empresaId;
                queryString += ",'INGRESADO'";
                queryString += ",'" + codigoPartida + "'";
                queryString += ",'" + codigoPartida + "'";
                if (((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getIvaPorPagar() != null) {
                    queryString += ",'FACTURA VENTA'";
                } else {
                    queryString += ",'RECIBO CONTABLE VENTA'";
                }
                queryString += ",'" + String.valueOf(tipoList.get(i).getValue()) + "'";
                queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
                queryString += "," + clienteCbx.getValue();
                queryString += ",'" + nitClienteTxt.getValue() + "'";
                queryString += ",'" + String.valueOf(clienteCbx.getItem(clienteCbx.getValue()).getItemProperty(NOMBRESINCODIGO_PROPERTY).getValue()) + "'";
                queryString += ",UPPER('" + serieTxt.getValue().trim() + "')";
                queryString += ",'" + numeroTxt.getValue().trim() + "'";
                queryString += "," + cuentaContableList.get(i).getValue();
                queryString += ",'" + monedaCbx.getValue() + "'";
                queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow()); //MONTODOCUMENTO
                queryString += ",0.00"; //DEBE
                if (((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getIvaPorPagar() != null && !exentaChb.getValue()) {
                    queryString += "," + Utileria.format((cantidadlist.get(i).getDoubleValueDoNotThrow() * montoList.get(i).getDoubleValueDoNotThrow()) / 1.12); // HABER
                } else {
                    queryString += "," + Utileria.format((cantidadlist.get(i).getDoubleValueDoNotThrow() * montoList.get(i).getDoubleValueDoNotThrow())); // HABER
                }
                queryString += ",0.00"; //DEBE Q.
                if (((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getIvaPorPagar() != null && !exentaChb.getValue()) {
                    queryString += "," + Utileria.format(((cantidadlist.get(i).getDoubleValueDoNotThrow() * montoList.get(i).getDoubleValueDoNotThrow()) / 1.12) * tasaCambioTxt.getDoubleValueDoNotThrow());
                } else {
                    queryString += "," + Utileria.format(((cantidadlist.get(i).getDoubleValueDoNotThrow() * montoList.get(i).getDoubleValueDoNotThrow())) * tasaCambioTxt.getDoubleValueDoNotThrow());
                }
                queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
                queryString += ",0.00"; //SALDO
                if (((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getIvaPorPagar() != null) {
                    queryString += ",'FACTURA VENTA " + String.valueOf(clienteCbx.getItem(clienteCbx.getValue()).getItemProperty(NOMBRESINCODIGO_PROPERTY).getValue()) + "'";
                } else {
                    queryString += ",'RECIBO CONTABLE VENTA " + String.valueOf(clienteCbx.getItem(clienteCbx.getValue()).getItemProperty(NOMBRESINCODIGO_PROPERTY).getValue()) + "'";
                }
                queryString += ",'NO'"; //referencia
                queryString += "," + 0;
                queryString += ",'" + centroCostoCbx.getItemCaption(centroCostoCbx.getValue()) + "'";
                queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                queryString += ",current_timestamp";
                queryString += ",null"; //archivo
                queryString += ",'application/pdf'"; //archivo tipo
                queryString += "," + (pdfFile != null ? pdfFile.length() : 0);    //archvio size
                queryString += ",'" + (pdfFile != null ? pdfFile.getName() : "") + "'";
                queryString += ",'" + uuid + "'";
                queryString += ",'" + fechaYHoraCertificacion + "'";
                queryString += ",''";
                queryString += ",''";
                queryString += ", " + mapaProductoEmpresa.get(productoList.get(i).getValue()).getIdProducto() + ")";
            }
        }

        if(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getIvaPorPagar() != null && !exentaChb.getValue()) {

System.out.println("entra a insertar linea del iva.  exentaChb.getValue()=" + exentaChb.getValue() + " getIvaPorPagar()=" + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getIvaPorPagar());
            //// INSERTAR EL IVA
            queryString += ",(";
            queryString += empresaId;
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigoPartida + "'";
            if(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getIvaPorPagar() != null) {
                queryString += ",'FACTURA VENTA'";
            }
            else {
                queryString += ",'RECIBO CONTABLE VENTA'";
            }
            queryString += ",'" + String.valueOf(tipoList.get(0).getValue()) + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + clienteCbx.getValue();
            queryString += ",'" + nitClienteTxt.getValue() + "'";
            queryString += ",'" + String.valueOf(clienteCbx.getItem(clienteCbx.getValue()).getItemProperty(NOMBRESINCODIGO_PROPERTY).getValue()) + "'";
            queryString += ",UPPER('" + serieTxt.getValue().trim() + "')";
            queryString += ",'" + numeroTxt.getValue().trim() + "'";
            queryString += "," + ((SopdiUI) mainUI).cuentasContablesDefault.getIvaPorPagar();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow()); //MONTODOCUMENTO
            queryString += ",0.00"; //DEBE
            queryString += "," + String.valueOf(ivaTxt.getDoubleValueDoNotThrow()); // HABER
            queryString += ",0.00"; //DEBE Q.
            queryString += "," + Utileria.format(ivaTxt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00"; //SALDO
            if(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getIvaPorPagar() != null) {
                queryString += ",'FACTURA VENTA " + String.valueOf(clienteCbx.getItem(clienteCbx.getValue()).getItemProperty(NOMBRESINCODIGO_PROPERTY).getValue()) + "'";
            }
            else {
                queryString += ",'RECIBO CONTABLE VENTA " + String.valueOf(clienteCbx.getItem(clienteCbx.getValue()).getItemProperty(NOMBRESINCODIGO_PROPERTY).getValue()) + "'";
            }
            queryString += ",'NO'"; //referencia
            queryString += "," + centroCostoCbx.getValue();
            queryString += ",'" + centroCostoCbx.getItemCaption(centroCostoCbx.getValue()) + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ",null"; //archivo
            queryString += ",'application/pdf'"; //archivo tipo
            queryString += "," + (pdfFile != null ? pdfFile.length() : 0);    //archvio size
            queryString += ",'" + (pdfFile != null ? pdfFile.getName() : "") + "'";
            queryString += ",'" + uuid + "'";
            queryString += ",'" + fechaYHoraCertificacion + "'";
            queryString += ",''";
            queryString += ",''";
            queryString += ",null)";
        }

        if(    ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyRegimen().equals("Opcional Simplificado sobre Ingresos de Actividades Lucrativas")
            && !((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyRegimen().equals("EXENTA")) {

            //SI EL CLIENTE NO ES AGENTE RETENEDOR DEL ISR, INSERTAR LINEAS PARA EL ISR GASTO Y EL ISR OPCIONAL MENSUAL POR PAGAR.
            if (retieneIsrLbl.getValue().isEmpty()) { //
                //// ISR GASTO
                queryString += ",(";
                queryString += empresaId;
                queryString += ",'INGRESADO'";
                queryString += ",'" + codigoPartida + "'";
                queryString += ",'" + codigoPartida + "'";
                queryString += ",'FACTURA VENTA'";
                queryString += ",'" + String.valueOf(tipoList.get(0).getValue()) + "'";
                queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
                queryString += "," + clienteCbx.getValue();
                queryString += ",'" + nitClienteTxt.getValue() + "'";
                queryString += ",'" + String.valueOf(clienteCbx.getItem(clienteCbx.getValue()).getItemProperty(NOMBRESINCODIGO_PROPERTY).getValue()) + "'";
                queryString += ",UPPER('" + serieTxt.getValue().trim() + "')";
                queryString += ",'" + numeroTxt.getValue().trim() + "'";
                queryString += "," + ((SopdiUI) mainUI).cuentasContablesDefault.getIsrGasto();
                queryString += ",'" + monedaCbx.getValue() + "'";
                queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow()); //MONTODOCUMENTO
                queryString += "," + String.valueOf(isrTxt.getDoubleValueDoNotThrow()); // DEBE
                queryString += ",0.00"; //HABER
                queryString += "," + Utileria.format(isrTxt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
                queryString += ",0.00"; //HABER Q.
                queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
                queryString += ",0.00"; //SALDO
                queryString += ",'FACTURA VENTA " + String.valueOf(clienteCbx.getItem(clienteCbx.getValue()).getItemProperty(NOMBRESINCODIGO_PROPERTY).getValue()) + "'";
                queryString += ",'NO'"; //referencia
                queryString += "," + centroCostoCbx.getValue();
                queryString += ",'" + centroCostoCbx.getItemCaption(centroCostoCbx.getValue()) + "'";
                queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                queryString += ",current_timestamp";
                queryString += ",null"; //archivo
                queryString += ",'application/pdf'"; //archivo tipo
                queryString += "," + (pdfFile != null ? pdfFile.length() : 0);    //archvio size
                queryString += ",'" + (pdfFile != null ? pdfFile.getName() : "") + "'";
                queryString += ",'" + uuid + "'";
                queryString += ",'" + fechaYHoraCertificacion + "'";
                queryString += ",''";
                queryString += ",''";
                queryString += ",null)";

                //// ISR OPCIONAL MENSUAL POR PAGAR
                queryString += ",(";
                queryString += empresaId;
                queryString += ",'INGRESADO'";
                queryString += ",'" + codigoPartida + "'";
                queryString += ",'" + codigoPartida + "'";
                queryString += ",'FACTURA VENTA'";
                queryString += ",'" + String.valueOf(tipoList.get(0).getValue()) + "'";
                queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
                queryString += "," + clienteCbx.getValue();
                queryString += ",'" + nitClienteTxt.getValue() + "'";
                queryString += ",'" + String.valueOf(clienteCbx.getItem(clienteCbx.getValue()).getItemProperty(NOMBRESINCODIGO_PROPERTY).getValue()) + "'";
                queryString += ",UPPER('" + serieTxt.getValue().trim() + "')";
                queryString += ",'" + numeroTxt.getValue().trim() + "'";
                queryString += "," + ((SopdiUI) mainUI).cuentasContablesDefault.getIsrOpcionalMensualPorPagar();
                queryString += ",'" + monedaCbx.getValue() + "'";
                queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow()); //MONTODOCUMENTO
                queryString += ",0.00"; //DEBE
                queryString += "," + String.valueOf(isrTxt.getDoubleValueDoNotThrow()); // HABER
                queryString += ",0.00"; //DEBE Q.
                queryString += "," + Utileria.format(isrTxt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
                queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
                queryString += ",0.00"; //SALDO
                queryString += ",'FACTURA VENTA " + String.valueOf(clienteCbx.getItem(clienteCbx.getValue()).getItemProperty(NOMBRESINCODIGO_PROPERTY).getValue()) + "'";
                queryString += ",'NO'"; //referencia
                queryString += "," + centroCostoCbx.getValue();
                queryString += ",'" + centroCostoCbx.getItemCaption(centroCostoCbx.getValue()) + "'";
                queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                queryString += ",current_timestamp";
                queryString += ",null"; //archivo
                queryString += ",'application/pdf'"; //archivo tipo
                queryString += "," + (pdfFile != null ? pdfFile.length() : 0);    //archvio size
                queryString += ",'" + (pdfFile != null ? pdfFile.getName() : "") + "'";
                queryString += ",'" + uuid + "'";
                queryString += ",'" + fechaYHoraCertificacion + "'";
                queryString += ",''";
                queryString += ",''";
                queryString += ",null)";

            }
        }//END IF REGIMEN

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "QUERY FACTURA VENTA : " + queryString);

        try {

            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            if (!variableTemp.isEmpty()) {
                cambiarEstatusToken(codigoPartida);
            }

            Notification notif = new Notification("FACTURA VENTA GENERADA EXITOSAMENTE.", Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.CHECK);
            notif.show(Page.getCurrent());

            ((FacturaVentaView) (mainUI.getNavigator().getCurrentView())).llenarTablaFacturaVenta();

            close();

        } catch (Exception ex1) {
            System.out.println("Error al insertar facturas  : " + ex1.getMessage());
            ex1.printStackTrace();
            Notification notif = new Notification("HA OCURRIDO UN ERROR DE BASE DE DATOS : " + ex1.getMessage(),
                    Notification.Type.HUMANIZED_MESSAGE);
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

        // Insertamos El espejo de Guatefactura
        insertarDocumentoElectronico(codigoPartida);

    }

    private void insertarDocumentoElectronico(String codigoPartida){

        queryString =   "INSERT INTO guatefactura_documento_electronico (";
        queryString +=  "SistemaEmisor, IdEmpresa, CodigoPartida, Serie, Preimpreso, Nombre, Direccion, ";
        queryString +=  "Telefono, NumeroAutorizacion, Referencia, IdTipo, Estado, FechaCreacionFel)";
        queryString +=  "VALUES (";
        queryString +=  "1"; // IMPORTANTE, ESTO CAMBIARA EN UN FUTURO, RECORDAR FUTURA API
        queryString +=  ", " + empresaId;
        queryString +=  ", '" + codigoPartida + "'";
        queryString +=  ", '" + serieTxt.getValue() + "'";
        queryString +=  ", " + numeroTxt.getValue();
        queryString +=  ", '" + nombre + "'";
        queryString +=  ", '" + direccion + "'";
        queryString +=  ", '" + telefono + "'";
        queryString +=  ", '" + uuid + "'";
        queryString +=  ", '" + fechaYHoraCertificacion + "'";
        queryString +=  ", '" + nit_cui_passCbx.getValue() + "'";
        queryString +=  ", 'INGRESADO'";
        queryString +=  ", current_timestamp)";

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "QUERY DOCUMENTO ELECTROCNICO : " + queryString);

        try {

            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            ((FacturaVentaView) (mainUI.getNavigator().getCurrentView())).llenarTablaFacturaVenta();

            close();

        } catch (Exception ex1) {
            System.out.println("Error al insertar Documetno Electronico  : " + ex1.getMessage());
            ex1.printStackTrace();
            Notification notif = new Notification("HA OCURRIDO UN ERROR DE BASE DE DATOS : " + ex1.getMessage(),
                    Notification.Type.HUMANIZED_MESSAGE);
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

    public void cambiarEstatusToken(String codigoPartida) {

        try {
            queryString = "UPDATE token SET ";
            queryString += " IdUsuario = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserId();
            queryString += ", Usuario = '" + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserFullName() + "'";
            queryString += ", FechaUsado = current_timestamp";
            queryString += ", CodigoPartida = '" + codigoPartida + "'";
            queryString += ", Estatus = 'UTILIZADO'";
            queryString += " WHERE Codigo = '" + variableTemp + "'";

            variableTemp = "";

            stQuery.executeUpdate(queryString);

        } catch (Exception e) {
            System.out.println("Error al intentar cambiar estatus token : " + e);
            e.printStackTrace();
        }

    }

    class Producto{
        private String IdProducto;
        private String IdNomenclatura;
        private String ExentoIva;
        private String tipo;

        Producto(String IdProducto, String IdNomenclatura, String ExentoIva, String tipo){
            this.IdProducto = IdProducto;
            this.IdNomenclatura = IdNomenclatura;
            this.ExentoIva = ExentoIva;
            this.tipo = tipo;
        }

        public String getTipo(){
            return tipo;
        }

        public String getExentoIva() {
            return ExentoIva;
        }

        public String getIdNomenclatura() {
            return IdNomenclatura;
        }

        public String getIdProducto() {
            return IdProducto;
        }
    }

}
