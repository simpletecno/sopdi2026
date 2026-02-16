package com.simpletecno.sopdi.ventas;

import com.simpletecno.sopdi.utilerias.MyEmailMessanger;
import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.simpletecno.sopdi.utilerias.ValidarTokenForm;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NotaCreditoVentaForm extends Window {

    static final String NIT_PROPERTY = "NIT";
    static final String NOMBRESINCODIGO_PROPERTY = "NSC";
    static final String RETIENEISR_PROPERTY = "RISR";
    static final String RETIENEIVA_PROPERTY = "RIVA";
    static final String DESCRIPCION_PROPERTY = "DESC";

    VerticalLayout mainLayout;
    VerticalLayout headerLayout;

    /* title */
    ComboBox empresaCbx;
    Date fechaHoy;

    ComboBox tipoFacturaVentaCbx;
    ComboBox centroCostoCbx;
    DateField fechaDt;
    ComboBox clienteCbx;
    TextField nitClienteTxt;

    TextField serieTxt;
    TextField numeroTxt;
    ComboBox monedaNotaCbx;
    ComboBox monedaDocBaseCbx;
    
    NumberField tasaCambioTxt;
    NumberField montoTxt;

    Button grabarBtn;

    UI mainUI;
    Statement stQuery;
    ResultSet rsRecords;
    Statement stQuery2;
    ResultSet rsRecords2;
    String queryString;

    String variableTemp = "";
    String xmlRequest;
    String xmlResponse;
    String referencia = "";
    String nombre = "";
    String direccion = "";
    String telefono = "";
    String uuid = "";
    String fechaYHoraCertificacion = "";
    File pdfFile = null;

    boolean exenta;

    String codigoPartidaFactura;
    String serieFactura;
    String numeroFactura;

    Double montoTotal = 0.0d;
    Double notaMontoTotal = 0.0d;
    Double netoMontoTotal = 0.0d;
    Double ivaMontoTotal = 0.0d;

    String monedaDocumento = "";

    List<Producto> productoList = new ArrayList<>();

    List<TextField> tipoList = new ArrayList<>();

    List<ComboBox> productoCbxList = new ArrayList<>();

    List<NumberField> cantidadlist = new ArrayList<>();

    List<NumberField> notaMontotxtList = new ArrayList<>();

    List<NumberField> montoList = new ArrayList<>();

    List<NumberField> motonResultantetxtlist = new ArrayList<>();

    List<TextField> razontxtList = new ArrayList<>();

    public NotaCreditoVentaForm(
            String codigoPartidaFactura,
            String serieFactura,
            String numeroFactura) {

        this.codigoPartidaFactura = codigoPartidaFactura;
        this.serieFactura = serieFactura;
        this.numeroFactura = numeroFactura;

        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setResponsive(true);
        mainLayout.setMargin(false);
        mainLayout.setWidth("100%");

        HorizontalLayout layoutTitle = new HorizontalLayout();
        layoutTitle.setSpacing(true);
        layoutTitle.setMargin(new MarginInfo(true, true, false, true));
        layoutTitle.setWidth("100%");

        HorizontalLayout footherLayout = new HorizontalLayout();
        footherLayout.setWidth("100%");
        footherLayout.setMargin(false);

        this.mainUI = UI.getCurrent();
        setResponsive(true);
        setWidth("80%");
//        setHeight("100%");

        empresaCbx = new ComboBox("EMPRESA :");
        empresaCbx.setStyleName(ValoTheme.COMBOBOX_HUGE);
        empresaCbx.setWidth("95%");
        empresaCbx.setInvalidAllowed(false);
        empresaCbx.setNewItemsAllowed(false);
        empresaCbx.setTextInputAllowed(false);
        empresaCbx.setNullSelectionAllowed(false);
        empresaCbx.addItem(((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId());
        empresaCbx.setItemCaption(((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId(), ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName() + " : " +  ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyRegimen());
        empresaCbx.select(empresaCbx.getItemIds().iterator().next());

        Label titleLbl = new Label("NOTA DE CREDITO FACTURA VENTA");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h2_custom");

        layoutTitle.addComponent(empresaCbx);
        layoutTitle.setComponentAlignment(empresaCbx, Alignment.MIDDLE_LEFT);
        layoutTitle.addComponent(titleLbl);
        layoutTitle.setComponentAlignment(titleLbl, Alignment.BOTTOM_RIGHT);

        mainLayout.addComponent(layoutTitle);

        crearComponentes();

        llenarDatosFactura();

        // Productos en la Factura
        createDocumentDetail();

        mainLayout.addComponent(footherLayout);

        grabarBtn = new Button("Grabar");
        grabarBtn.setIcon(FontAwesome.SAVE);
        grabarBtn.setStyleName(ValoTheme.BUTTON_PRIMARY);
        grabarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                generarDocumento();
            }
        });

        footherLayout.addComponents(grabarBtn);
        footherLayout.setComponentAlignment(grabarBtn, Alignment.BOTTOM_RIGHT);

        setContent(mainLayout);
    }

    public void crearComponentes() {

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
        fechaDt.setValue(new Date());

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
                    }
                }
        );

        nitClienteTxt = new TextField("Nit : ");
        nitClienteTxt.setWidth("100%");

        firstLineHeaderLayout.addComponents(tipoFacturaVentaCbx,centroCostoCbx,fechaDt, clienteCbx,nitClienteTxt);
        firstLineHeaderLayout.setComponentAlignment(tipoFacturaVentaCbx, Alignment.MIDDLE_LEFT);
        firstLineHeaderLayout.setComponentAlignment(centroCostoCbx, Alignment.MIDDLE_LEFT);
        firstLineHeaderLayout.setComponentAlignment(fechaDt, Alignment.MIDDLE_LEFT);
        firstLineHeaderLayout.setComponentAlignment(clienteCbx, Alignment.MIDDLE_LEFT);
        firstLineHeaderLayout.setComponentAlignment(nitClienteTxt, Alignment.MIDDLE_LEFT);

        firstLineHeaderLayout.setExpandRatio(tipoFacturaVentaCbx, 1.5f);
        firstLineHeaderLayout.setExpandRatio(centroCostoCbx, 1.5f);
        firstLineHeaderLayout.setExpandRatio(fechaDt, 1.5f);
        firstLineHeaderLayout.setExpandRatio(clienteCbx, 4.0f);
        firstLineHeaderLayout.setExpandRatio(nitClienteTxt, 2.0f);

        serieTxt = new TextField("Serie : ");
        serieTxt.setWidth("15em");
        serieTxt.addStyleName("mayusculas");

        numeroTxt = new TextField("Número : ");
        numeroTxt.setWidth("15em");

        monedaNotaCbx = new ComboBox("Moneda Nota de Credito:");
        monedaNotaCbx.setWidth("15em");
        monedaNotaCbx.addItem("QUETZALES");
        monedaNotaCbx.addItem("DOLARES");
        monedaNotaCbx.select("QUETZALES");
        monedaNotaCbx.setInvalidAllowed(false);
        monedaNotaCbx.setNewItemsAllowed(false);
        monedaNotaCbx.addValueChangeListener(evet -> {
            if(monedaNotaCbx.getValue() != null){
                if(monedaNotaCbx.getValue().equals("DOLARES")){
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

        monedaDocBaseCbx = new ComboBox("Moneda Docmuento Original:");
        monedaDocBaseCbx.setWidth("15em");
        monedaDocBaseCbx.addItem("QUETZALES");
        monedaDocBaseCbx.addItem("DOLARES");
        monedaDocBaseCbx.setInvalidAllowed(false);
        monedaDocBaseCbx.setNewItemsAllowed(false);
        monedaDocBaseCbx.setEnabled(false);


        secondLineHaderLayout.addComponents(serieTxt, numeroTxt, monedaNotaCbx, tasaCambioTxt, monedaDocBaseCbx);
        secondLineHaderLayout.setComponentAlignment(serieTxt, Alignment.MIDDLE_LEFT);
        secondLineHaderLayout.setComponentAlignment(numeroTxt, Alignment.MIDDLE_LEFT);
        secondLineHaderLayout.setComponentAlignment(monedaNotaCbx, Alignment.MIDDLE_LEFT);
        secondLineHaderLayout.setComponentAlignment(tasaCambioTxt, Alignment.MIDDLE_LEFT);
        secondLineHaderLayout.setComponentAlignment(monedaDocBaseCbx, Alignment.MIDDLE_RIGHT);


        llenarComboCliente();

    }

    private void createDocumentDetail() {
        HorizontalLayout detailLayout = new HorizontalLayout();
        detailLayout.setWidth("100%");
        detailLayout.setSpacing(true);
        detailLayout.setMargin(false);
//        detailLayout.setMargin(new MarginInfo(false, true, false, true));
        detailLayout.setMargin(new MarginInfo(false, true, false, true));
        detailLayout.addStyleName("rcorners3");

        VerticalLayout centralVerticalLayout = new VerticalLayout();
        centralVerticalLayout.setWidth("100%");
        centralVerticalLayout.setSpacing(true);
//        centralVerticalLayout.setMargin(new MarginInfo(false, true, false, false));

        detailLayout.addComponent(centralVerticalLayout);
        detailLayout.setComponentAlignment(centralVerticalLayout, Alignment.TOP_CENTER);

        mainLayout.addComponents(detailLayout);

        int numeroProductos = productoList.size(); /* <-- No cuenta el producto agregado dentro de For */

        // --- Agregar los productos
        for(int i = 0; i <= numeroProductos; i++) {
            Producto producto;
            if (i == numeroProductos) {
                producto = new Producto(null, "6", "", " ", "", montoTotal);
                productoList.add(producto);
            } else {
                producto = productoList.get(i);
            }

            // Crear Horizontal Layout del ProductoNota
            HorizontalLayout layoutHorizontal = new HorizontalLayout();
            layoutHorizontal.setResponsive(true);
            layoutHorizontal.setSpacing(true);

            // Declaraciones
            NumberField cantidadTxt = new NumberField();
            ComboBox productoCbx = new ComboBox();
            TextField razonTxt = new TextField();
            NumberField montotxt = new NumberField();
            NumberField notaMontotxt = new NumberField();
            NumberField montoResultadotxt = new NumberField();


            // CANTIDAD
            if (i == 0) cantidadTxt.setCaption("CANT.");
            if (i == numeroProductos) cantidadTxt.setCaption(" ");
            cantidadTxt.setDecimalAllowed(true);
            cantidadTxt.setDecimalPrecision(0);
            cantidadTxt.setMinimumFractionDigits(0);
            cantidadTxt.setValue(1d);
            cantidadTxt.setImmediate(true);
            cantidadTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);
            cantidadTxt.setWidth("5em");
            if (i == numeroProductos) cantidadTxt.setEnabled(false);
            cantidadTxt.addValueChangeListener(event -> {
                notaMontotxt.setValue(notaMontotxt.getDoubleValueDoNotThrow() + 1); // Chapus
                notaMontotxt.setValue(notaMontotxt.getDoubleValueDoNotThrow() - 1); // Chapus
            });

            cantidadlist.add(cantidadTxt);

            // Tipo Documento
            TextField tipoDocumento = new TextField();
            if (i == 0) tipoDocumento.setCaption("Tipo :");
            if (i == numeroProductos) tipoDocumento.setCaption(" ");
            tipoDocumento.setWidth("3em");
            tipoDocumento.setEnabled(false);
            String tipo = producto.getTipo().substring(0,1);
            tipoDocumento.setValue(tipo);

            tipoList.add(tipoDocumento);

            // ProductoNota
            if (i == 0) productoCbx.setCaption("PRODUCTO O SERVICIO");
            if (i == numeroProductos) productoCbx.setCaption(" ");
            productoCbx.setWidth("20em");
            productoCbx.setEnabled(false);
            productoCbx.addItem(producto.getNombre());
            productoCbx.select(producto.getNombre());

            productoCbxList.add(productoCbx);

            // Razon
            if (i == 0) razonTxt.setCaption("Razon :");
            if (i == numeroProductos) razonTxt.setCaption(" ");
            razonTxt.setWidth("20em");
            if (i == numeroProductos) razonTxt.setEnabled(false);

            razontxtList.add(razonTxt);

            // Monto Factura
            if (i == 0) montotxt.setCaption("Monto Original: ");
            if (i == numeroProductos) montotxt.setCaption("Total :");
            montotxt.setDecimalAllowed(true);
            montotxt.setNegativeAllowed(false);
            montotxt.setDecimalPrecision(2);
            montotxt.setMinimumFractionDigits(2);
            montotxt.setDecimalSeparator('.');
            montotxt.setDecimalSeparatorAlwaysShown(true);
            montotxt.setGroupingUsed(true);
            montotxt.setGroupingSeparator(',');
            montotxt.setGroupingSize(3);
            montotxt.setImmediate(true);
            montotxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
            montotxt.setWidth("7em");
            montotxt.setValue(0.00);
            montotxt.setEnabled(false);
            montotxt.setValue(Utileria.format(producto.getMonto()));

            montoList.add(montotxt);

            // Monto Nota
            if (i == 0) notaMontotxt.setCaption("Monto Nota: ");
            if (i == numeroProductos) notaMontotxt.setCaption(" ");
            notaMontotxt.setDecimalAllowed(true);
            notaMontotxt.setNegativeAllowed(false);
            notaMontotxt.setDecimalPrecision(2);
            notaMontotxt.setMinimumFractionDigits(2);
            notaMontotxt.setDecimalSeparator('.');
            notaMontotxt.setDecimalSeparatorAlwaysShown(true);
            notaMontotxt.setGroupingUsed(true);
            notaMontotxt.setGroupingSeparator(',');
            notaMontotxt.setGroupingSize(3);
            notaMontotxt.setImmediate(true);
            notaMontotxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
            notaMontotxt.setWidth("7em");
            notaMontotxt.setValue(0.00);
            if (i == numeroProductos) {
                notaMontotxt.setEnabled(false);
            } else {
                notaMontotxt.addValueChangeListener(event -> {
                    Double monto = notaMontotxt.getDoubleValueDoNotThrow();
                    montoResultadotxt.setValue(Utileria.format(montotxt.getDoubleValueDoNotThrow() - (monto * cantidadTxt.getDoubleValueDoNotThrow())));

                    producto.setNeto(Utileria.format(monto / 1.12));

                    Double neto = new Double(producto.getNeto());

                    producto.setIva(Utileria.format(monto - neto));

                    notaMontoTotal = 0.0d;
                    netoMontoTotal = 0.0d;
                    ivaMontoTotal = 0.0d;
                    for (int j = 0; (j + 1) < notaMontotxtList.size(); j++) {
                        notaMontoTotal += notaMontotxtList.get(j).getDoubleValueDoNotThrow() * cantidadlist.get(j).getDoubleValueDoNotThrow();
                        netoMontoTotal += new Double(productoList.get(j).getNeto());
                        ivaMontoTotal += new Double(productoList.get(j).getIva());
                    }
                    notaMontotxtList.get(numeroProductos).setValue(Utileria.format(notaMontoTotal));
                    motonResultantetxtlist.get(numeroProductos).setValue(Utileria.format(montoTotal - notaMontoTotal));

                });
            }

            notaMontotxtList.add(notaMontotxt);

            // Monto Resutalnte
            if (i == 0) montoResultadotxt.setCaption("Monto Final: ");
            if (i == numeroProductos) montoResultadotxt.setCaption(" ");
            montoResultadotxt.setDecimalAllowed(true);
            montoResultadotxt.setNegativeAllowed(false);
            montoResultadotxt.setDecimalPrecision(2);
            montoResultadotxt.setMinimumFractionDigits(2);
            montoResultadotxt.setDecimalSeparator('.');
            montoResultadotxt.setDecimalSeparatorAlwaysShown(true);
            montoResultadotxt.setGroupingUsed(true);
            montoResultadotxt.setGroupingSeparator(',');
            montoResultadotxt.setGroupingSize(3);
            montoResultadotxt.setImmediate(true);
            montoResultadotxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
            montoResultadotxt.setWidth("7em");
            montoResultadotxt.setValue(0.00);
            montoResultadotxt.setEnabled(false);


            motonResultantetxtlist.add(montoResultadotxt);


            // Orden en el Layout
            layoutHorizontal.addComponents(cantidadTxt, tipoDocumento, productoCbx, razonTxt, montotxt, notaMontotxt, montoResultadotxt);

            centralVerticalLayout.addComponent(layoutHorizontal);
            centralVerticalLayout.setComponentAlignment(layoutHorizontal, Alignment.MIDDLE_CENTER);
        }


        // Espacio en Blanco
        HorizontalLayout whitespace = new HorizontalLayout();
        whitespace.setResponsive(true);
        whitespace.setSpacing(true);
        centralVerticalLayout.addComponent(whitespace);
        centralVerticalLayout.setComponentAlignment(whitespace, Alignment.MIDDLE_CENTER);
    }



    public void llenarComboCentroCosto() {

        queryString = " SELECT * from centro_costo";
        queryString += " WHERE IdProyecto = " + ((SopdiUI) mainUI).sessionInformation.getStrProjectId();
        queryString += " AND IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                do {
                    centroCostoCbx.addItem(rsRecords.getString("IdCentroCosto"));
                    centroCostoCbx.setItemCaption(rsRecords.getString("IdCentroCosto"), rsRecords.getString("CodigoCentroCosto"));
                } while (rsRecords.next());
            }

        } catch (Exception ex1) {
            System.out.println("Error al llenar combo centro costo: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void llenarComboCliente() {

        queryString = " SELECT * from proveedor ";
        queryString += " WHERE Inhabilitado = 0 ";
        queryString += " AND EsCliente = 1";
        queryString += " Order By Nombre";

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

    public void llenarDatosFactura() {

        exenta = false;

        queryString = "SELECT * ";
        queryString += "FROM contabilidad_partida cp ";
        queryString += "INNER JOIN producto_venta_empresa pve ON cp.IdProducto = pve.IdProducto ";
        queryString += "WHERE cp.CodigoPartida = '" + codigoPartidaFactura + "' ";
        queryString += "AND cp.IdEmpresa = " + empresaCbx.getValue() + " ";
        queryString += "AND pve.IdEmpresa = " + empresaCbx.getValue() + " ";
        queryString += "AND pve.Especial = 0 ";

        Logger.getLogger(this.getClass().getName()).log(Level.INFO,"QUERY BUSCAR FACTURA VENTA  : " + queryString);

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { // 0
                clienteCbx.select(rsRecords.getString("IdProveedor"));
                centroCostoCbx.select(rsRecords.getString("IdCentroCosto"));

                monedaDocumento = rsRecords.getString("MonedaDocumento");
                if(rsRecords.getString("ExentoIVA").equals("SI")){
                    exenta = true;
                }
                do {
                    Double monto = 0.0d;
                    if(rsRecords.getString("ExentoIVA").equals("NO")){
                        monto = rsRecords.getDouble("Haber") * 1.12;
                        montoTotal = montoTotal + monto;
                    }else{
                        monto = rsRecords.getDouble("Haber");
                        montoTotal = montoTotal + monto;
                    }
                    Producto p = new Producto(  rsRecords.getString("IdProducto"), rsRecords.getString("IdNomenclatura"),
                                                rsRecords.getString("ExentoIVA"), rsRecords.getString("Tipo"),
                                                rsRecords.getString("NombreProducto"), monto);
                    productoList.add(p);
                    monedaDocBaseCbx.select(rsRecords.getString("MonedaDocumento"));
                } while(rsRecords.next());
            }

        } catch (Exception ex) {
            System.out.println("Error al leer tabla de FACTURA VENTA : " + ex);
            Notification.show("ERROR AL LEER FACTURA VENTA PARA NOTA DE CREDITO : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void generarDocumento() {

        if (datosValidos()) {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "DATOS VALIDOS OK!");
            if (!((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyFelUser().trim().isEmpty()) {
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
        if (monedaNotaCbx.getValue() == null) {
            Notification.show("Por favor ingrese el tipo de moneda.", Notification.Type.WARNING_MESSAGE);
            monedaNotaCbx.focus();
            return false;
        }

        for (NumberField resultado : motonResultantetxtlist){
            if (resultado.getDoubleValueDoNotThrow() < 0.0d) {
                Notification.show("El Monto de la nota una de Credito es Mayor al del su Monto Original. ", Notification.Type.WARNING_MESSAGE);
                resultado.focus();
                return false;
            }

        }

        if(!monedaNotaCbx.getValue().equals(monedaDocumento)){
            Notification.show("La MONEDA de la NOTA DE CREDITO tiene que ser la misma que la del Domcumento Referenciado.", Notification.Type.WARNING_MESSAGE);
            monedaNotaCbx.focus();
            return false;
        }

        if (notaMontoTotal == 0) {
            Notification.show("Por favor ingrese el monto de la factura.", Notification.Type.WARNING_MESSAGE);
            return false;
        }



//        totalHaber = new BigDecimal(haber1Txt.getDoubleValueDoNotThrow()
//                + haber2Txt.getDoubleValueDoNotThrow() + haber3Txt.getDoubleValueDoNotThrow()
//                + haber4Txt.getDoubleValueDoNotThrow() + haber5Txt.getDoubleValueDoNotThrow()
//                + haber6Txt.getDoubleValueDoNotThrow() + haber7Txt.getDoubleValueDoNotThrow()
//                + haber8Txt.getDoubleValueDoNotThrow()).setScale(2, BigDecimal.ROUND_HALF_UP);
//
//        totalHaber.setScale(2, BigDecimal.ROUND_HALF_UP);

//        if (totalHaber.doubleValue() != montoTxt.getDoubleValueDoNotThrow()) {
//            Notification notif = new Notification("EL MONTO DEL DEBE Y EL HABER NO COINCIDEN!. MONTO DEL DEBE : " + montoTxt.getDoubleValueDoNotThrow() + " MONTO DEL HABER : " + totalHaber.doubleValue(),
//                    Notification.Type.ERROR_MESSAGE);
//            notif.setDelayMsec(1500);
//            notif.setPosition(Position.MIDDLE_CENTER);
//            notif.setIcon(FontAwesome.WARNING);
//            notif.show(Page.getCurrent());
//            return false;
//        }

        queryString = " Select * from contabilidad_partida";
        queryString += " Where SerieDocumento  = '" + serieTxt.getValue().toUpperCase().trim() + "'";
        queryString += " And   NumeroDocumento = '" + numeroTxt.getValue().toUpperCase().trim() + "'";
//        queryString += " And   IdProveedor     =  " + String.valueOf(clienteCbx.getValue());
        if(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getIvaPorPagar() != null) {
            queryString += " And   TipoDocumento = 'FACTURA VENTA'";
        }
        else {
            queryString += " And   TipoDocumento = 'RECIBO CONTABLE VENTA'";
        }
        queryString += " And   IdEmpresa = " + empresaCbx.getValue();

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

        xmlRequest ="<![CDATA[<DocElectronico>\n" +
                        "<Encabezado>\n" +
                            "<Receptor>\n" +
                                "<NITReceptor>" + nitClienteTxt.getValue().replaceAll("-", "") + "</NITReceptor>\n" +
                                "<Nombre>" + clienteCbx.getContainerProperty(clienteCbx.getValue(), NOMBRESINCODIGO_PROPERTY).getValue() + "</Nombre>\n" +
                                "<Direccion>Ciudad Guatemala</Direccion>\n" +
                            "</Receptor>\n" +
                        "<InfoDoc>\n" +
                            "<TipoVenta>S</TipoVenta>\n" +
                            "<DestinoVenta>1</DestinoVenta>\n" +
                            "<Fecha>" + Utileria.getFechaDDMMYYYY(fechaDt.getValue()) + "</Fecha>\n";
        /* Dolares */           if(monedaNotaCbx.getValue().equals("DOLARES")) {
            xmlRequest +=   "<Moneda>2</Moneda>\n"+
                    "       <Tasa>" + ((SopdiUI) UI.getCurrent()).sessionInformation.getFltExchangeRate() + "</Tasa>\n" ;
        }
        /* Quetzales */         else {
            xmlRequest +=   "<Moneda>1</Moneda>\n" +
                            "<Tasa>1</Tasa>\n" ;
        }
        xmlRequest +=       "<Referencia>" + new Utileria().getReferencia() + "</Referencia>\n" +
                            "<NumeroAcceso></NumeroAcceso>\n" +
                            "<SerieAdmin>" + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanySmallName() + "</SerieAdmin>\n" +
                            "<NumeroAdmin>" + new Utileria().getReferencia() + "</NumeroAdmin>\n" +
                            "<Reversion>N</Reversion>\n" +
                        "</InfoDoc>\n" +
    /* Totales */       "<Totales>\n" ;

        // EXENTO
        if(exenta) {
            xmlRequest +=   "<Bruto>" + Utileria.format(notaMontoTotal) + "</Bruto>\n" +
                            "<Descuento>0.00</Descuento>\n" +
                            "<Exento>" + Utileria.format(notaMontoTotal) + "</Exento>\n" +
                            "<Otros>0.00</Otros>\n" +
                            "<Neto>0.00</Neto>\n" +
                            "<Isr>0.00</Isr>\n" +
                            "<Iva>0.00</Iva>\n" +
                            "<Total>" + Utileria.format(notaMontoTotal) + "</Total>\n";
        }
        else {
            xmlRequest +=   "<Bruto>" + Utileria.format(notaMontoTotal) + "</Bruto>\n" +
                            "<Descuento>0.00</Descuento>\n" +
                            "<Exento>0.00</Exento>\n" +
                            "<Otros>0.00</Otros>\n" +
                            "<Neto>" + Utileria.format(netoMontoTotal) + "</Neto>\n" +
                            "<Isr>0.00</Isr>\n" +
                            "<Iva>" + Utileria.format(ivaMontoTotal) + "</Iva>\n" +
                            "<Total>" + Utileria.format(notaMontoTotal) + "</Total>\n";
        }
        xmlRequest +=   "</Totales>\n" +
                        "</Encabezado>\n" +
                        "<Detalles>\n";

        // --- Genrando ProductoNota o Servicio
        for (int i = 0; (i + 1) < productoList.size(); i++){


            if(cantidadlist.get(i).getDoubleValueDoNotThrow() > 0 && notaMontotxtList.get(i).getDoubleValueDoNotThrow() > 0) {

                Producto producto = productoList.get(i); // ProductoNota a Facturar
                Boolean exentoIva = producto.getExentoIva().equals("SI"); // Si el ProductoNota es Exento de IVA
                Double neto = new Double(producto.getNeto());
                Double iva = new Double(producto.getIva());

                // Verificar que ambos sean exentos.
                if(exenta != exentoIva) {
                    Notification.show("La Factura y los Productos no comparten SI son Exentas de IVA o NO, Verifique.", Notification.Type.WARNING_MESSAGE);
                    return false;
                }


                xmlRequest +=   "<Productos>\n";
                if(exentoIva) {
                    xmlRequest +=   "<ProductoNota>" + producto.getIdProducto() + "</ProductoNota>\n" +
                                    "<Descripcion>" + producto.getNombre() + " NOTA CREADIO RAZON: " + razontxtList.get(i).getValue().trim() + "</Descripcion>\n" +
                                    "<Medida>1</Medida>\n" +
                                    "<Cantidad>" + cantidadlist.get(i).getValue() + "</Cantidad>\n" +
                                    "<Precio>" + Utileria.format(notaMontotxtList.get(i).getDoubleValueDoNotThrow()) + "</Precio>\n" +
                                    "<PorcDesc>0.00</PorcDesc>\n" +
                                    "<ImpBruto>" + Utileria.format(cantidadlist.get(i).getDoubleValueDoNotThrow() * notaMontotxtList.get(i).getDoubleValueDoNotThrow()) + "</ImpBruto>\n" +
                                    "<ImpDescuento>0.00</ImpDescuento>\n" +
                                    "<ImpExento>" + Utileria.format(cantidadlist.get(i).getDoubleValueDoNotThrow() * notaMontotxtList.get(i).getDoubleValueDoNotThrow()) + "</ImpExento>\n" +
                                    "<ImpNeto>0.00</ImpNeto>\n"+
                                    "<ImpOtros>0.00</ImpOtros>\n" +
                                    "<ImpIsr>0.00</ImpIsr>\n" +
                                    "<ImpIva>0.00</ImpIva>\n" +
                                    "<ImpTotal>" + Utileria.format(cantidadlist.get(i).getDoubleValueDoNotThrow() * notaMontotxtList.get(i).getDoubleValueDoNotThrow()) + "</ImpTotal>\n" +
                                    "<productoDet>" + producto.getTipo().substring(0, 1) + "</productoDet>\n";
                }
                else {
                    xmlRequest +=   "<ProductoNota>" + producto.getIdProducto() + "</ProductoNota>\n" +
                                    "<Descripcion>" + producto.getNombre() + " NOTA CREADIO RAZON: " + razontxtList.get(i).getValue().trim() + "</Descripcion>\n" +
                                    "<Medida>1</Medida>\n" +
                                    "<Cantidad>" + cantidadlist.get(i).getValue() + "</Cantidad>\n" +
                                    "<Precio>" + Utileria.format(notaMontotxtList.get(i).getDoubleValueDoNotThrow()) + "</Precio>\n" +
                                    "<PorcDesc>0.00</PorcDesc>\n" +
                                    "<ImpBruto>" + Utileria.format((cantidadlist.get(i).getDoubleValueDoNotThrow() * notaMontotxtList.get(i).getDoubleValueDoNotThrow())) + "</ImpBruto>\n" +
                                    "<ImpDescuento>0.00</ImpDescuento>\n" +
                                    "<ImpExento>0.0</ImpExento>\n" +
                                    "<ImpNeto>" + Utileria.format(neto * cantidadlist.get(i).getDoubleValueDoNotThrow()) + "</ImpNeto>\n" +
                                    "<ImpOtros>0.00</ImpOtros>\n" +
                                    "<ImpIsr>0.00</ImpIsr>\n" +
                                    "<ImpIva>" + Utileria.format(iva * cantidadlist.get(i).getDoubleValueDoNotThrow()) + "</ImpIva>\n" +
                                    "<ImpTotal>" + Utileria.format((cantidadlist.get(i).getDoubleValueDoNotThrow() * notaMontotxtList.get(i).getDoubleValueDoNotThrow())) + "</ImpTotal>\n" +
                                    "<productoDet>" + producto.getTipo().substring(0, 1) + "</productoDet>\n";
                }
                xmlRequest +=   "</Productos>\n";
            }
        }

                xmlRequest += "<DocAsociados>\n";
                xmlRequest += "<DASerie>" + serieFactura + "</DASerie>\n";
                xmlRequest += "<DAPreimpreso>" + numeroFactura + "</DAPreimpreso>\n";
                xmlRequest += "</DocAsociados>\n";

                xmlRequest += "</Detalles>\n";
                xmlRequest += "</DocElectronico>]]>";

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "xmlRequest=" + xmlRequest);

        xmlResponse = "<?xml version=\"1.0\"?>" + guateFacPort. generaDocumento(
                ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyFelUser(),
                ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyFelPass(),
                ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyTaxId(),
                new BigDecimal(1),
                new BigDecimal(10), //nota de credito
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

            Node resultado = doc.getFirstChild();
            if (resultado.getChildNodes().getLength() > 2) {
                serieTxt.setValue(resultado.getChildNodes().item(0).getTextContent());
                numeroTxt.setValue(resultado.getChildNodes().item(1).getTextContent());
                nombre = resultado.getChildNodes().item(2).getTextContent();                    // Nombre
                direccion = resultado.getChildNodes().item(3).getTextContent();                 // Direccion
                telefono = resultado.getChildNodes().item(4).getTextContent();
                referencia = resultado.getChildNodes().item(5).getTextContent(); // uuid
                uuid = resultado.getChildNodes().item(5).getTextContent(); // uuid
                fechaYHoraCertificacion = resultado.getChildNodes().item(6).getTextContent();

                obtenerFacturaPdf(serieTxt.getValue(), numeroTxt.getValue());

            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "ERROR AL CERTIFICAR LA NOTA DE CREDITO : " + resultado.getTextContent());
                Notification.show("ERROR AL CERTIFICAR LA NOTA DE CREDITO  : " + resultado.getTextContent(), Notification.Type.ERROR_MESSAGE);
                return false;
            }

        } catch (Exception e) {
            Notification.show("ERROR AL CERTIFICAR LA NOTA DE CREDITO  : " + e.getMessage());
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "ERROR AL CERTIFICAR LA NOTA DE CREDITO");
            e.printStackTrace();
            return false;
        }

        return true;
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
        String ultimoEncontrado;
        String dia = fecha.substring(8, 10);
        String mes = fecha.substring(5, 7);
        String año = fecha.substring(0, 4);

        String codigoPartidaNC = String.valueOf(empresaCbx.getValue()) + año + mes + dia + "0";

        queryString = " select codigoPartida from contabilidad_partida ";
        queryString += " where codigoPartida like '" + codigoPartidaNC + "%'";
        queryString += " order by codigoPartida desc ";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado

                ultimoEncontrado = rsRecords.getString("codigoPartida").substring(12, 15);

                codigoPartidaNC += String.format("%03d", (Integer.valueOf(ultimoEncontrado) + 1));

            } else {
                codigoPartidaNC += "001";
            }

        } catch (Exception ex1) {
            System.out.println("Error al buscar el ultimo codigoPartida" + ex1.getMessage());
            ex1.printStackTrace();
        }

        queryString = " Insert Into proveedor_cuentacorriente (IdEmpresa,IdProveedor, Fecha,";
        queryString += " TipoDocumento, SerieDocumento,NumeroDocumento, MonedaDocumento, ";
        queryString += " Monto, MontoQuetzales, TipoCambio ";
        queryString += ", IdUsuarioAutorizoPago,CreadoFechayHora,CreadoUsuario)";
        queryString += " Values(";
        queryString += empresaCbx.getValue();
        queryString += "," + clienteCbx.getValue();
        queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
        queryString += ",'NOTA DE CREDITO VENTA'";
        queryString += ",UPPER('" + serieTxt.getValue().trim() + "')";
        queryString += ",'" + numeroTxt.getValue().trim() + "'";
        queryString += ",'" + monedaNotaCbx.getValue() + "'";
        queryString += ", " + Utileria.format(notaMontoTotal);
        queryString += ", " + Utileria.format(notaMontoTotal * tasaCambioTxt.getDoubleValueDoNotThrow());
        queryString += ", " + tasaCambioTxt.getValue();
        queryString += ", " + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
        queryString += ",'" + Utileria.getFechaYYYYMMDD_1(new Date()) + "'";
        queryString += ", " + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
        queryString += ")";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);
        } catch (Exception ex1) {
            System.out.println("Error al insertar proveedor cuentaCorriente : " + ex1.getMessage());
            ex1.printStackTrace();
        }

        queryString = " Insert Into contabilidad_partida (IdEmpresa, CodigoPartida, CodigoCC,";
        queryString += " TipoDocumento, Fecha, IdOrdenCompra, IdProveedor, NITProveedor, NombreProveedor,";
        queryString += " SerieDocumento, NumeroDocumento, IdNomenclatura, MonedaDocumento, MontoDocumento, ";
        queryString += " Debe, Haber, DebeQuetzales, HaberQuetzales, TipoCambio, Saldo, Estatus, ";
        queryString += " IdLiquidacion, IdLiquidador, Descripcion, Referencia,";
        queryString += " CreadoUsuario, CreadoFechaYHora, Archivo, ArchivoTipo, ArchivoPeso, ArchivoNombre, ";
        queryString += " IdCentroCosto, CodigoCentroCosto, UUID, FechaYHoraCertificacion, XmlRequest, XmlResponse, IdProducto";
        queryString += ")";
        queryString += " Values ";

        for (int i = 0; i < productoList.size(); i++ ){

            Producto producto = productoList.get(i);
            Double neto = new Double(producto.getNeto());
            Double iva = new Double(producto.getIva());

            // 0: IVA | 1: Monto
            for (int j = 0; j < 2; j++){
                if(i == (productoList.size() - 1)) j = 1; // Total
                queryString += " (";
                queryString += ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
                queryString += ",'" + codigoPartidaNC + "'";
                queryString += ",'" + codigoPartidaFactura + "'";
                queryString += ",'NOTA DE CREDITO VENTA'";
                queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
                queryString += ",0";
                queryString += "," + clienteCbx.getValue();
                queryString += ",'" + nitClienteTxt.getValue() + "'";
                queryString += ",'" + clienteCbx.getContainerProperty(clienteCbx.getValue(), NOMBRESINCODIGO_PROPERTY).getValue() + "'";
                queryString += ",'" + serieTxt.getValue().toUpperCase().trim() + "'";
                queryString += ",'" + numeroTxt.getValue().trim() + "'";
                if (j == 1) {
                    queryString += "," + producto.getIdNomenclatura();
                }else {
                    queryString += "," + ((SopdiUI) mainUI).cuentasContablesDefault.getIvaPorPagar();
                }
                queryString += ",'" + monedaNotaCbx.getValue() + "'";
                queryString += "," + notaMontoTotal; //MONTO DOCUMENTO
                if (i < (productoList.size() - 1)) { //reversar debe
                    Double montoPartida = 0.0d;
                    if (j == 1){    // Monto Base
                        montoPartida = neto  * cantidadlist.get(i).getDoubleValueDoNotThrow();
                    }
                    else{           // Monto IVA
                        montoPartida = iva * cantidadlist.get(i).getDoubleValueDoNotThrow();
                    }
                    queryString += "," + Utileria.format(montoPartida); //DEBE
                    queryString += ",0.00"; //HABER
                    queryString += "," + Utileria.format(montoPartida * tasaCambioTxt.getDoubleValueDoNotThrow()); //DEBE Q.
                    queryString += ",0.00"; //HABER Q
                } else {  //reversar haber
                    queryString +=  ",0.00"; //DEBE
                    queryString += "," + Utileria.format(notaMontotxtList.get(i).getDoubleValueDoNotThrow()); //HABER
                    queryString +=  ",0.00";
                    queryString += "," + Utileria.format(notaMontotxtList.get(i).getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow()); //HABER Q.
                }
                queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow()); //SALDO
                queryString += ",0.00"; //SALDO
                queryString += ",'INGRESADO'";
                queryString += ", 0";
                queryString += ", 0";
                queryString += ",'NOTA DE CREDITO VENTA PARA : " + serieFactura + "-" + numeroFactura + " "
                            + clienteCbx.getContainerProperty(clienteCbx.getValue(), NOMBRESINCODIGO_PROPERTY).getValue()
                            + "Prdocuto: " + producto.getNombre() + " Razon: " + razontxtList.get(i).getValue() + "'";
                queryString += ",'NO'";
                queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                queryString += ",current_timestamp";
                queryString += ",null"; //archivo
                queryString += ",'application/pdf'"; //archivo tipo
                queryString += ","  + (pdfFile != null ? pdfFile.length() : 0);    //archivo size
                queryString += ",'" + (pdfFile != null ? pdfFile.getAbsolutePath() : "") + "'";
                queryString += ", " + centroCostoCbx.getValue();
                queryString += ",'" + centroCostoCbx.getItemCaption(centroCostoCbx.getValue()) + "'";
                queryString += ",'" + uuid + "'";
                queryString += ",'" + fechaYHoraCertificacion + "'";
                queryString += ",'" + xmlRequest + "'";
                queryString += ",'" + xmlResponse + "'";

                queryString += ", " + productoList.get(i).getIdProducto() + ")\n,";
            }
        }


        queryString = queryString.substring(0, queryString.length() - 1);

        try {
            stQuery2 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery2.executeUpdate(queryString);
        } catch (Exception ex1) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "HA OCURRIDO UN ERROR DE BASE DE DATOS : " + ex1.getMessage());
            ex1.printStackTrace();
            Notification notif = new Notification("HA OCURRIDO UN ERROR DE BASE DE DATOS : " + ex1.getMessage(),
                    Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.WARNING);
            notif.show(Page.getCurrent());
        }



    Logger.getLogger(this.getClass().getName()).log(Level.INFO, queryString);

    if (!variableTemp.isEmpty()) {
        cambiarEstatusToken(codigoPartidaNC);
    }

    Logger.getLogger(this.getClass().getName()).log(Level.INFO, "NOTA DE CREDITO VENTA GENERADA EXITOSAMENTE.");

    Notification notif = new Notification("NOTA DE CREDITO VENTA GENERADA EXITOSAMENTE.", Notification.Type.HUMANIZED_MESSAGE);
    notif.setDelayMsec(1500);
    notif.setPosition(Position.MIDDLE_CENTER);
    notif.setIcon(FontAwesome.CHECK);
    notif.show(Page.getCurrent());

    ((FacturaVentaView) (mainUI.getNavigator().getCurrentView())).llenarTablaFacturaVenta(String.valueOf(empresaCbx.getValue()));

    // Insertamos El espejo de Guatefactura
    insertarDocumentoElectronico(codigoPartidaNC);

    close();

    }

    private void insertarDocumentoElectronico(String codigoPartida){

        queryString =   "INSERT INTO guatefactura_documento_electronico (";
        queryString +=  "SistemaEmisor, IdEmpresa, CodigoPartida, Serie, Preimpreso, Nombre, Direccion, ";
        queryString +=  "Telefono, NumeroAutorizacion, Referencia, IdTipo, Estado, FechaCreacionFel)";
        queryString +=  "VALUES (";
        queryString +=  "1"; // IMPORTANTE, ESTO CAMBIARA EN UN FUTURO, RECORDAR FUTURA API
        queryString +=  ", " + empresaCbx.getValue();
        queryString +=  ", '" + codigoPartida + "'";
        queryString +=  ", '" + serieTxt.getValue() + "'";
        queryString +=  ", " + numeroTxt.getValue();
        queryString +=  ", '" + nombre + "'";
        queryString +=  ", '" + direccion + "'";
        queryString +=  ", '" + telefono + "'";
        queryString +=  ", '" + uuid + "'";
        queryString +=  ", '" + fechaYHoraCertificacion + "'";
        queryString +=  ", 'NIT'";
        queryString +=  ", 'INGRESADO'";
        queryString +=  ", current_timestamp)";


        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "QUERY DOCUMENTO ELECTROCNICO : " + queryString);

        try {

            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            ((FacturaVentaView) (mainUI.getNavigator().getCurrentView())).llenarTablaFacturaVenta(String.valueOf(empresaCbx.getValue()));

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
            queryString += " Where Codigo = '" + variableTemp + "'";

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
        private String nombre;
        private Double monto;
        private String neto;
        private String iva;

        Producto(String IdProducto, String IdNomenclatura,  String ExentoIva, String tipo, String nombre, Double monto){
            this.IdProducto = IdProducto;
            this.IdNomenclatura = IdNomenclatura;
            this.ExentoIva = ExentoIva;
            this.tipo = tipo;
            this.nombre = nombre;
            this.monto = monto;
            this.neto = "0.0";
            this.iva = "0.0";
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

        public String getNombre(){
            return nombre;
        }

        public Double getMonto(){
            return monto;
        }

        public String getNeto() {
            return neto;
        }

        public void setNeto(String neto){
            this.neto = neto;
        }

        public String getIva(){
            return iva;
        }

        public void setIva(String iva){
            this.iva = iva;
        }

    }
}
