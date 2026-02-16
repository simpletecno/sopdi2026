package com.simpletecno.sopdi.ventas;

import com.simpletecno.sopdi.utilerias.MyEmailMessanger;
import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.simpletecno.sopdi.utilerias.ValidarTokenForm;
import com.simpletecno.sopdi.extras.custom.SegmentedField;
import com.simpletecno.sopdi.extras.custom.ToggleSwitch;
import com.simpletecno.sopdi.extras.infile.InfileClient;
import com.simpletecno.sopdi.extras.infile.Producto;
import com.simpletecno.sopdi.extras.infile.Receptor;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.ui.NumberField;

import javax.mail.MessagingException;
import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NotaCreditoVentaInfileForm extends Window {

    static final String NIT_PROPERTY = "NIT";
    static final String NOMBRESINCODIGO_PROPERTY = "NSC";
    static final String RETIENEISR_PROPERTY = "RISR";
    static final String RETIENEIVA_PROPERTY = "RIVA";
    static final String DIRECCION_PROPERTY = "DIRECCION";
    static final String CORREO_PROPERTY = "CORREO";

    VerticalLayout mainLayout;
    VerticalLayout headerLayout;

    /* title */
    ComboBox empresaCbx;
    Date fechaHoy;

    ComboBox tipoFacturaVentaCbx;
    DateField fechaDt;
    ComboBox clienteCbx;
    TextField nitClienteTxt;

    TextField serieTxt;
    TextField numeroTxt;
    ComboBox monedaNotaCbx;
    ComboBox monedaDocBaseCbx;
    SegmentedField segmentedField;

    ToggleSwitch modoOg;  // Si se agrega La seria y numero manualmente o no

    NumberField tasaCambioTxt;

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
    String UUIDDocumentoBase = "";
    String UUIDNotaCredito = "";
    File pdfFile = null;

    InfileClient infileClient;

    boolean exenta;
    boolean centroCostoFlag = false;   // evita recursión en listeners

    String codigoPartidaFactura;
    String codigoPartidaNC;
    String serieFactura;
    String numeroFactura;
    Date fechaFactura;

    Double montoTotal = 0.0d;
    Double notaMontoTotal = 0.0d;
    Double netoMontoTotal = 0.0d;
    Double ivaMontoTotal = 0.0d;

    String monedaDocumento = "";

    List<ProductoNota> productoNotaList = new ArrayList<>();

    List<TextField> tipoList = new ArrayList<>();

    List<ComboBox> productoCbxList = new ArrayList<>();

    List<NumberField> cantidadlist = new ArrayList<>();

    List<NumberField> notaMontotxtList = new ArrayList<>();

    List<NumberField> montoList = new ArrayList<>();

    List<NumberField> motonResultantetxtlist = new ArrayList<>();

    List<TextField> razontxtList = new ArrayList<>();

    List<ComboBox> centroCostoList = new ArrayList<>();

    Map<Integer, String> centroCostoMap = new HashMap<>();

    public NotaCreditoVentaInfileForm(
            String codigoPartidaFactura,
            String serieFactura,
            String numeroFactura,
            String UUID) {

        this.codigoPartidaFactura = codigoPartidaFactura;
        this.serieFactura = serieFactura;
        this.numeroFactura = numeroFactura;
        this.UUIDDocumentoBase = UUID;

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
        llenarCentroCosto();
        llenarDatosFactura();

        // Productos en la Factura
        createDocumentDetail();

        mainLayout.addComponent(footherLayout);

        infileClient = new InfileClient(((SopdiUI)mainUI).sessionInformation.getInfileEmisor());

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
        clienteCbx.addContainerProperty(DIRECCION_PROPERTY, String.class, "Guatemala, Guatemala");
        clienteCbx.addContainerProperty(CORREO_PROPERTY, String.class, "");
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

        firstLineHeaderLayout.addComponents(tipoFacturaVentaCbx,fechaDt, clienteCbx,nitClienteTxt);
        firstLineHeaderLayout.setComponentAlignment(tipoFacturaVentaCbx, Alignment.MIDDLE_LEFT);
        firstLineHeaderLayout.setComponentAlignment(fechaDt, Alignment.MIDDLE_LEFT);
        firstLineHeaderLayout.setComponentAlignment(clienteCbx, Alignment.MIDDLE_LEFT);
        firstLineHeaderLayout.setComponentAlignment(nitClienteTxt, Alignment.MIDDLE_LEFT);

        firstLineHeaderLayout.setExpandRatio(tipoFacturaVentaCbx, 1.5f);
        firstLineHeaderLayout.setExpandRatio(fechaDt, 1.5f);
        firstLineHeaderLayout.setExpandRatio(clienteCbx, 4.0f);
        firstLineHeaderLayout.setExpandRatio(nitClienteTxt, 2.0f);

        serieTxt = new TextField("Serie : ");
        serieTxt.setWidth("100%");
        serieTxt.addStyleName("mayusculas");
        serieTxt.setResponsive(true);

        numeroTxt = new TextField("Número : ");
        numeroTxt.setWidth("100%");
        numeroTxt.setResponsive(true);

        monedaNotaCbx = new ComboBox("Moneda Nota de Credito:");
        monedaNotaCbx.setWidth("100%");
        monedaNotaCbx.addItem("QUETZALES");
        monedaNotaCbx.addItem("DOLARES");
        monedaNotaCbx.select("QUETZALES");
        monedaNotaCbx.setInvalidAllowed(false);
        monedaNotaCbx.setNewItemsAllowed(false);
        monedaNotaCbx.setResponsive(true);
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
        tasaCambioTxt.setWidth("50%");
        tasaCambioTxt.setResponsive(true);
        tasaCambioTxt.setValue(1.00);

        modoOg = new ToggleSwitch("Agregar", "Crear", event -> {
            boolean value = (boolean) event.getProperty().getValue();
            numeroTxt.setEnabled(!value);
            serieTxt.setEnabled(!value);
        });

        modoOg.setValue(!((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyFelUser().trim().isEmpty());

        monedaDocBaseCbx = new ComboBox("Moneda Docmuento Original:");
        monedaDocBaseCbx.setWidth("100%");
        monedaDocBaseCbx.addItem("QUETZALES");
        monedaDocBaseCbx.addItem("DOLARES");
        monedaDocBaseCbx.setInvalidAllowed(false);
        monedaDocBaseCbx.setNewItemsAllowed(false);
        monedaDocBaseCbx.setEnabled(false);


        secondLineHaderLayout.addComponents(serieTxt, numeroTxt, monedaNotaCbx, tasaCambioTxt, modoOg, monedaDocBaseCbx);
        secondLineHaderLayout.setComponentAlignment(serieTxt, Alignment.MIDDLE_LEFT);
        secondLineHaderLayout.setComponentAlignment(numeroTxt, Alignment.MIDDLE_LEFT);
        secondLineHaderLayout.setComponentAlignment(monedaNotaCbx, Alignment.MIDDLE_LEFT);
        secondLineHaderLayout.setComponentAlignment(tasaCambioTxt, Alignment.MIDDLE_LEFT);
        secondLineHaderLayout.setComponentAlignment(modoOg, Alignment.BOTTOM_RIGHT);
        secondLineHaderLayout.setComponentAlignment(monedaDocBaseCbx, Alignment.MIDDLE_RIGHT);


        llenarComboCliente();

    }

    private void createDocumentDetail() {
        HorizontalLayout detailLayout = new HorizontalLayout();
        detailLayout.setWidth("100%");
        detailLayout.setSpacing(true);
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

        int numeroProductos = productoNotaList.size(); /* <-- No cuenta el producto agregado dentro de For */
        boolean centroCostoIgual = true;
        Integer lastCentroCosto = null;

        // --- Agregar los productos
        for(int i = 0; i <= numeroProductos; i++) {
            ProductoNota productoNota;
            if (i == numeroProductos) {
                productoNota = new ProductoNota(null, "6", "", " ", "", montoTotal, null,null);
                productoNotaList.add(productoNota);
            } else {
                productoNota = productoNotaList.get(i);
            }

            // Crear Horizontal Layout del ProductoNota
            HorizontalLayout layoutHorizontal = new HorizontalLayout();
            layoutHorizontal.setResponsive(true);
            layoutHorizontal.setSpacing(true);

            // Declaraciones
            NumberField cantidadTxt = new NumberField();
            ComboBox productoCbx = new ComboBox();
            TextField razonTxt = new TextField();
            ComboBox centroCostoCbx = new ComboBox();
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
            cantidadTxt.setResponsive(true);
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
            tipoDocumento.setResponsive(true);
            String tipo = productoNota.getTipo().substring(0,1);
            tipoDocumento.setValue(tipo);

            tipoList.add(tipoDocumento);

            // ProductoNota
            if (i == 0) productoCbx.setCaption("PRODUCTO O SERVICIO");
            if (i == numeroProductos) productoCbx.setCaption(" ");
            productoCbx.setWidth("20em");
            productoCbx.setEnabled(false);
            productoCbx.setResponsive(true);
            productoCbx.addItem(productoNota.getNombre());
            productoCbx.select(productoNota.getNombre());

            productoCbxList.add(productoCbx);

            // Razon
            if (i == 0) razonTxt.setCaption("Razon :");
            if (i == numeroProductos) razonTxt.setCaption(" ");
            razonTxt.setWidth("20em");
            if (i == numeroProductos) razonTxt.setEnabled(false);
            razonTxt.addValueChangeListener(event -> {;
                productoNota.getProducto().setComentario(razonTxt.getValue());
            });


            razontxtList.add(razonTxt);

            // Centro Costo
            if (i == 0) {
                centroCostoCbx.setCaption("Centro Costo: ");
                lastCentroCosto = productoNota.getCentroCosto();
            }
            centroCostoCbx.setWidth("10em");
            centroCostoCbx.setDescription("Centro de costo");
            centroCostoCbx.setResponsive(true);
            centroCostoCbx.setTextInputAllowed(false);
            for (Integer idCC : centroCostoMap.keySet()) {
                centroCostoCbx.addItem(idCC);
                centroCostoCbx.setItemCaption(idCC, centroCostoMap.get(idCC));
            }

            // Selección inicial
            if (i < numeroProductos) { // filas reales
                centroCostoCbx.select(productoNota.getCentroCosto());
                if (i == 0) {
                    lastCentroCosto = productoNota.getCentroCosto();
                } else if (centroCostoIgual) {
                    centroCostoIgual = (lastCentroCosto == null)
                            ? (productoNota.getCentroCosto() == null)
                            : lastCentroCosto.equals(productoNota.getCentroCosto());
                    lastCentroCosto = productoNota.getCentroCosto();
                }
            } else {
                // Fila resumen: agrega item 0 → "----"
                if (!centroCostoCbx.getItemIds().contains(0)) {
                    centroCostoCbx.addItem(0);
                    centroCostoCbx.setItemCaption(0, "----");
                }
                // valor inicial del resumen acorde al estado actual
                centroCostoCbx.select(centroCostoIgual && lastCentroCosto != null ? lastCentroCosto : 0);
                centroCostoCbx.setCaption(" ");
                centroCostoCbx.setEnabled(false); // opcional: que sea solo indicador
            }

            // Guarda el combo en la lista
            centroCostoList.add(centroCostoCbx);

            // Listener SOLO para los combos de filas reales (no el último)
            final int idx = i;
            if (idx < numeroProductos) {
                centroCostoCbx.addValueChangeListener(e -> actualizarUltimoCentroCosto(numeroProductos));
            }

        // Monto Factura
            if (i == 0) montotxt.setCaption("Monto Original: ");
            if (i == numeroProductos) montotxt.setCaption("Total :");
            montotxt.setDecimalAllowed(true);
            montotxt.setNegativeAllowed(false);
            montotxt.setResponsive(true);
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
            montotxt.setValue(Utileria.format(productoNota.getMonto()));

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
            notaMontotxt.setResponsive(true);
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

                    productoNota.setNeto(Utileria.format(monto / 1.12));
                    productoNota.getProducto().setMonto(monto);

                    Double neto = new Double(productoNota.getNeto());

                    productoNota.setIva(Utileria.format(monto - neto));

                    notaMontoTotal = 0.0d;
                    netoMontoTotal = 0.0d;
                    ivaMontoTotal = 0.0d;
                    for (int j = 0; (j + 1) < notaMontotxtList.size(); j++) {
                        notaMontoTotal += notaMontotxtList.get(j).getDoubleValueDoNotThrow() * cantidadlist.get(j).getDoubleValueDoNotThrow();
                        netoMontoTotal += new Double(productoNotaList.get(j).getNeto());
                                    ivaMontoTotal += new Double(productoNotaList.get(j).getIva());
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
            montoResultadotxt.setResponsive(true);
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
            layoutHorizontal.addComponents(cantidadTxt, tipoDocumento, productoCbx, razonTxt, centroCostoCbx, montotxt, notaMontotxt, montoResultadotxt);

            centralVerticalLayout.addComponent(layoutHorizontal);
            centralVerticalLayout.setComponentAlignment(layoutHorizontal, Alignment.MIDDLE_CENTER);
        }


        actualizarUltimoCentroCosto(numeroProductos);

        // Espacio en Blanco
        HorizontalLayout whitespace = new HorizontalLayout();
        whitespace.setResponsive(true);
        whitespace.setSpacing(true);
        centralVerticalLayout.addComponent(whitespace);
        centralVerticalLayout.setComponentAlignment(whitespace, Alignment.MIDDLE_CENTER);
    }

    private void actualizarUltimoCentroCosto(int summaryIndex) {
        if (centroCostoFlag) return; // evita recursion
        if (centroCostoList.isEmpty() || summaryIndex < 0 || summaryIndex >= centroCostoList.size()) return;

        ComboBox resumen = centroCostoList.get(summaryIndex);

        Object firstValue = null;
        boolean todosIguales = true;

        // Compara sólo los combos de 0..summaryIndex-1
        for (int i = 0; i < summaryIndex; i++) {
            Object v = centroCostoList.get(i).getValue();
            if (firstValue == null) {
                firstValue = v;
            } else if ((v == null && firstValue != null) || (v != null && !v.equals(firstValue))) {
                todosIguales = false;
                break;
            }
        }

        // Asegura que exista el item 0 → "----" en el resumen
        if (!resumen.getItemIds().contains(0)) {
            resumen.addItem(0);
            resumen.setItemCaption(0, "----");
        }

        // Escribe el valor del resumen sin disparar loops
        centroCostoFlag = true;
        try {
            if (todosIguales && firstValue != null) {
                // Si el valor común no existe como item en el resumen, lo agregamos con caption del mapa
                if (!resumen.getItemIds().contains(firstValue)) {
                    resumen.addItem(firstValue);
                    if (firstValue instanceof Integer && centroCostoMap.containsKey(firstValue)) {
                        resumen.setItemCaption(firstValue, centroCostoMap.get((Integer) firstValue));
                    }
                }
                resumen.setValue(firstValue);
            } else {
                resumen.setValue(0); // "----"
            }
        } finally {
            centroCostoFlag = false;
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
                String idProveedor = rsRecords.getString("IDProveedor");
                String nit = rsRecords.getString("NIT");
                String nombre = rsRecords.getString("Nombre");
                String EsAgenteRetenedorIsr = rsRecords.getString("EsAgenteRetenedorIsr");
                String EsAgenteRetenedorIva = rsRecords.getString("EsAgenteRetenedorIva");
                String direccion = rsRecords.getString("Direccion");
                String email = rsRecords.getString("Email");
                if (email == null || email.isEmpty()) {
                    email = "";
                }

                clienteCbx.addItem(idProveedor);
                clienteCbx.setItemCaption(idProveedor, "(" + idProveedor + ") " + nombre);
                clienteCbx.getItem(idProveedor).getItemProperty(NIT_PROPERTY).setValue(nit);
                clienteCbx.getItem(idProveedor).getItemProperty(NOMBRESINCODIGO_PROPERTY).setValue(nombre);
                clienteCbx.getItem(idProveedor).getItemProperty(RETIENEISR_PROPERTY).setValue(EsAgenteRetenedorIsr);
                clienteCbx.getItem(idProveedor).getItemProperty(RETIENEIVA_PROPERTY).setValue(EsAgenteRetenedorIva);
                clienteCbx.getItem(idProveedor).getItemProperty(DIRECCION_PROPERTY).setValue(direccion);
                clienteCbx.getItem(idProveedor).getItemProperty(CORREO_PROPERTY).setValue(email);
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar de clientes (tabla proveedores) " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void llenarCentroCosto() {

        queryString = " SELECT * FROM centro_costo";
        queryString += " WHERE IdProyecto = " + ((SopdiUI) mainUI).sessionInformation.getStrProjectId();
        queryString += " AND Inhabilitado = 0";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                do {
                    centroCostoMap.put(rsRecords.getInt("IdCentroCosto"), rsRecords.getString("CodigoCentroCosto"));
                } while (rsRecords.next());
            }

        } catch (Exception ex1) {
            System.out.println("Error al llenar combo centro costo: " + ex1.getMessage());
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

                monedaDocumento = rsRecords.getString("MonedaDocumento");
                if(rsRecords.getString("ExentoIVA").equals("SI")){
                    exenta = true;
                }

                fechaFactura = rsRecords.getDate("Fecha");
                do {

                    Map<Integer, Integer> frases = new HashMap<>(); //<Frase, Escenario>
                    queryString =  "SELECT * ";
                    queryString += "FROM producto_venta_frases ea ";
                    queryString += "WHERE CorrelativoProducto = ? "; // <-- se le asigna en el setString()

                    try {
                        PreparedStatement pstQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().prepareStatement(queryString);
                        pstQuery.setInt(1, rsRecords.getInt("CorrelativoProducto"));
                        rsRecords2 = pstQuery.executeQuery();
                        while (rsRecords2.next()){
                            frases.put(
                                    rsRecords2.getInt("Frase"),
                                    rsRecords2.getInt("Escenario"));
                        }

                    }
                    catch (Exception ex) {
                        System.out.println("Error al buscar frases de producto: " + ex.getMessage());
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Error al buscar frases de producto", ex);
                    }

                    Producto p = new Producto(
                            rsRecords.getString("NombreProducto"),
                            0.00,
                            1,
                            "",
                            rsRecords.getString("InfileTipo"),
                            frases);

                    Double monto = 0.0d;
                    if(rsRecords.getString("ExentoIVA").equals("NO")){
                        monto = Utileria.round(rsRecords.getDouble("Haber") * 1.12);
                        montoTotal = montoTotal + monto;
                    }else{
                        monto = rsRecords.getDouble("Haber");
                        montoTotal = montoTotal + monto;
                    }
                    ProductoNota pNC = new ProductoNota(
                            rsRecords.getString("IdProducto"),
                            rsRecords.getString("IdNomenclatura"),
                            rsRecords.getString("ExentoIVA"),
                            rsRecords.getString("Tipo"),
                            rsRecords.getString("NombreProducto"),
                            monto,
                            rsRecords.getInt("IdCentroCosto"),
                            p);
                    productoNotaList.add(pNC);
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

            codigoPartidaNC = Utileria.nextCodigoPartida(
                    ((SopdiUI) mainUI).databaseProvider.getCurrentConnection(),
                    ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId(),
                    fechaDt.getValue(),
                    0
            );

            if (!((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyFelUser().trim().isEmpty() && modoOg.getValue()) {
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "ES FEL!");
                if (documentoCeritficaroInfile()) {
                    String serie = infileClient.getSerie();
                    String numero = String.valueOf(infileClient.getNumero());
                    numeroTxt.setValue(numero);
                    serieTxt.setValue(serie);

                    pdfFile = infileClient.obtenerDTEPdf(((SopdiUI) UI.getCurrent()).enviromentsVars.getDtePath() + serie + "_" + numero + ".pdf");

                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, "DOCUMENTO CERTIFICDADO FEL OK!");
                    Notification.show("DOCUMENTO CERTIFICADO FEL OK!", Notification.Type.HUMANIZED_MESSAGE);
                } else {
                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Error datos FEL!" + infileClient.getDescripcionErrores().toString());
                    Notification.show("Error datos FEL!", Notification.Type.WARNING_MESSAGE);
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


    private boolean documentoCeritficaroInfile() {

        Receptor receptor = new Receptor(
                nitClienteTxt.getValue().replaceAll("-", ""),
                clienteCbx.getContainerProperty(clienteCbx.getValue(), NOMBRESINCODIGO_PROPERTY).getValue().toString(),
                clienteCbx.getContainerProperty(clienteCbx.getValue(), CORREO_PROPERTY).getValue().toString(),
                clienteCbx.getContainerProperty(clienteCbx.getValue(), DIRECCION_PROPERTY).getValue().toString()
        );

        List<Producto> productoList = new ArrayList<>();

        for (int i = 0; i < productoNotaList.size() - 1; i++) {
            ProductoNota productoNota = productoNotaList.get(i);
            if (productoNota.getMonto() > 0) {
                productoList.add(productoNota.getProducto());
            }
        }

        if (productoList.isEmpty()) {
            Notification.show("Debe ingresar al menos un producto para continuar.", Notification.Type.WARNING_MESSAGE);
            return false;
        }

        infileClient = new InfileClient(((SopdiUI) mainUI).sessionInformation.getInfileEmisor());

        return infileClient.generarDocumentoDependiente(
                receptor,
                codigoPartidaNC,
                productoList,
                "NCRE",
                "",
                UUIDDocumentoBase,
                serieFactura,
                numeroFactura,
                fechaDt.getValue(),
                fechaFactura,
                monedaNotaCbx.getValue().toString().equals("DOLARES") ? "USD" : "GTQ",
                tasaCambioTxt.getDoubleValueDoNotThrow(),
                "NOTA DE CREDITO POR VENTA"
        );
    }

//

    private void insertarPartidas() {

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

        for (int i = 0; i < productoNotaList.size(); i++ ){

            ProductoNota productoNota = productoNotaList.get(i);
            Double neto = new Double(productoNota.getNeto());
            Double iva = new Double(productoNota.getIva());

            // 0: IVA | 1: Monto
            for (int j = 0; j < 2; j++){
                if(i == (productoNotaList.size() - 1)) j = 1; // Total
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
                    queryString += "," + productoNota.getIdNomenclatura();
                }else {
                    queryString += "," + ((SopdiUI) mainUI).cuentasContablesDefault.getIvaPorPagar();
                }
                queryString += ",'" + monedaNotaCbx.getValue() + "'";
                queryString += "," + notaMontoTotal; //MONTO DOCUMENTO
                if (i < (productoNotaList.size() - 1)) { //reversar debe
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
                            + "Prdocuto: " + productoNota.getNombre() + " Razon: " + razontxtList.get(i).getValue() + "'";
                queryString += ",'NO'";
                queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                queryString += ",current_timestamp";
                queryString += ",null"; //archivo
                queryString += ",'application/pdf'"; //archivo tipo
                queryString += "," + (pdfFile != null ? pdfFile.length() : 0);    //archvio size
                queryString += ",'" + (pdfFile != null ? pdfFile.getAbsolutePath() : "").replace("\\", "/") + "'";
                queryString += ", " + productoNota.getCentroCosto();
                queryString += ",'" + centroCostoMap.get(productoNota.getCentroCosto()) + "'";
                queryString += ",'" + infileClient.getUUID() + "'";
                queryString += ",'" + Utileria.getFechaYYYYMMDD_1(infileClient.getFechaHoraCertificacion()) + "'";
                queryString += ",'" + xmlRequest + "'";
                queryString += ",'" + xmlResponse + "'";

                queryString += ", " + productoNotaList.get(i).getIdProducto() + ")\n,";
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

        queryString =   "INSERT INTO certificado_fel_infile (";
        queryString +=  "Fecha, Origen, Descripcion, Saldo, Creditos, AlertasInfile, AlertasSat, ";
        queryString +=  "InformacionAdicional, UUID, Serie, Numero, JsonResponse, CodigoPartida, IdEmpresa, Estado) ";
        queryString +=  "VALUES (";
        queryString +=  "'" +Utileria.getFechaYYYYMMDDHHMMSS(infileClient.getFechaHoraCertificacion()) + "'";
        queryString +=  ", '" + infileClient.getOrigen() + "'";
        queryString +=  ", '" + infileClient.getDescripcion() + "'";
        queryString +=  ", '" + infileClient.getSaldo() + "'";
        queryString +=  ", '" + infileClient.getCreditos() + "'";
        queryString +=  ", " + infileClient.getAlertasInfile();
        queryString +=  ", " + infileClient.getAlertasSAT();
        queryString +=  ", '" + infileClient.getInformacionAdicional() + "'";
        queryString +=  ", '" + infileClient.getUUID() + "'";
        queryString +=  ", '" + infileClient.getSerie() + "'";
        queryString +=  ", '" + infileClient.getNumero() + "'";
        queryString +=  ", '" + infileClient.getRespuesta() + "'";
        queryString +=  ", '" + codigoPartida + "'";
        queryString +=  ", '" + ((SopdiUI)mainUI).sessionInformation.getStrAccountingCompanyId() + "'";
        queryString +=  ", 'INGRESADO')";


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

    class ProductoNota {
        private String IdProducto;
        private String IdNomenclatura;
        private String ExentoIva;
        private String tipo;
        private String nombre;
        private Double monto;
        private String neto;
        private String iva;
        private Integer CentroCosto;
        private Producto p;

        ProductoNota(
                String IdProducto, String IdNomenclatura, String ExentoIva, String tipo,
                String nombre, Double monto, Integer centroCosto, Producto p){
            this.IdProducto = IdProducto;
            this.IdNomenclatura = IdNomenclatura;
            this.ExentoIva = ExentoIva;
            this.tipo = tipo;
            this.nombre = nombre;
            this.monto = monto;
            this.neto = "0.0";
            this.iva = "0.0";
            this.CentroCosto = centroCosto;
            this.p = p;
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

        public String getIva(){
            return iva;
        }

        public Integer getCentroCosto() {
            return CentroCosto;
        }

        public Producto getProducto() {
            return p;
        }

        public void setNeto(String neto){
            this.neto = neto;
        }

        public void setIva(String iva){
            this.iva = iva;
        }

        public void setProducto(Producto p){
            this.p = p;
        }

    }
}
