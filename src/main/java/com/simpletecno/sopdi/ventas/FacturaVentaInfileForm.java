package com.simpletecno.sopdi.ventas;

import com.simpletecno.sopdi.utilerias.MyEmailMessanger;
import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.simpletecno.sopdi.utilerias.ValidarTokenForm;
import com.simpletecno.sopdi.extras.custom.ToggleSwitch;
import com.simpletecno.sopdi.extras.infile.InfileClient;
import com.simpletecno.sopdi.extras.infile.Producto;
import com.simpletecno.sopdi.extras.infile.Receptor;
import com.vaadin.data.Item;
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
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FacturaVentaInfileForm extends Window {

    static final String NIT_PROPERTY = "NIT";
    static final String NOMBRESINCODIGO_PROPERTY = "NSC";
    static final String RETIENEISR_PROPERTY = "RISR";
    static final String RETIENEIVA_PROPERTY = "RIVA";
    static final String DIRECCION_PROPERTY = "DIRECCION";
    static final String CORREO_PROPERTY = "CORREO";

    static DecimalFormat numberFormat = new DecimalFormat("######0.00");

    Panel centerContentPanel;

    VerticalLayout mainLayout;
    VerticalLayout headerLayout;
    VerticalLayout centralVerticalLayout;
    HorizontalLayout footerLayout;

    /* title */
    ComboBox empresaCbx;
    Date fechaHoy;

    ComboBox tipoFacturaVentaCbx;
    DateField fechaDt;
    ComboBox clienteCbx;
    TextField nitClienteTxt;
    Label retieneIsrLbl;
    Label retieneIvaLbl;

    TextField serieTxt;
    TextField numeroTxt;
    ComboBox monedaCbx;
    NumberField tasaCambioTxt;
    NumberField totalFacturadoMesTxt;

    ComboBox nit_cui_passCbx;

    ProductoVenta headProductoVenta = null;
    List<ProductoVenta> productoVentaList = new LinkedList<>();
    List<ProductoVentaEmpresaDTO> productoEmpresaList = new ArrayList<>();
    Map<Integer, String> centroCostoMap = new HashMap<>();

    String codigoPartida = "";

    Double ivaMontoTotal = 0.0d;
    Double netoMontoTotal = 0.0d;

    boolean exenta;

    NumberField montoTxt;
    NumberField ivaTxt;
    NumberField isrTxt;

    Button grabarBtn;

    ToggleSwitch modoOg;  // Si se agrega La seria y numero manualmente o no

    UI mainUI;
    Statement stQuery;
    ResultSet rsRecords;
    Statement stQuery2;
    ResultSet rsRecords2;
    String queryString;

    InfileClient infileClient;

    String xmlRequest;
    String xmlResponse;
    String variableTemp = "";
    File pdfFile = null;

    public FacturaVentaInfileForm() {
        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setResponsive(true);
        mainLayout.setSizeFull();
        mainLayout.setMargin(new MarginInfo(false, true, true, true));

        HorizontalLayout layoutTitle = new HorizontalLayout();
        layoutTitle.setSpacing(true);
        layoutTitle.setMargin(new MarginInfo(true, true, false, true));
        layoutTitle.setWidth("100%");

        this.mainUI = UI.getCurrent();
        setResponsive(true);
        setResizable(true);
        setWidth("90%");
        setHeight("90%");

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

        Label titleLbl = new Label("FACTURA VENTA");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();

        layoutTitle.addComponent(empresaCbx);
        layoutTitle.setComponentAlignment(empresaCbx, Alignment.MIDDLE_LEFT);
        layoutTitle.addComponent(titleLbl);
        layoutTitle.setComponentAlignment(titleLbl, Alignment.BOTTOM_RIGHT);

        mainLayout.addComponent(layoutTitle);
        mainLayout.setComponentAlignment(layoutTitle, Alignment.TOP_CENTER);
//        mainLayout.setExpandRatio(layoutTitle, 1.0f);

        infileClient = new InfileClient(((SopdiUI)mainUI).sessionInformation.getInfileEmisor());

        setContent(mainLayout);
        llenarCentroCosto();
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
        headerLayout.setHeight("10em");
        headerLayout.setSpacing(true);
        headerLayout.setMargin(false);

        HorizontalLayout firstLineHeaderLayout = new HorizontalLayout();
        firstLineHeaderLayout.setSizeFull();
        firstLineHeaderLayout.setSpacing(true);
        firstLineHeaderLayout.setMargin(new MarginInfo(false, true, false, true));
        firstLineHeaderLayout.addStyleName("rcorners3");

        HorizontalLayout secondLineHaderLayout = new HorizontalLayout();
        secondLineHaderLayout.setSizeFull();
        secondLineHaderLayout.setSpacing(true);
        secondLineHaderLayout.setMargin(new MarginInfo(true, true, false, true));
        secondLineHaderLayout.addStyleName("rcorners3");

        headerLayout.addComponents(firstLineHeaderLayout, secondLineHaderLayout);

        mainLayout.addComponents(headerLayout);
        mainLayout.setComponentAlignment(headerLayout, Alignment.MIDDLE_CENTER);
        mainLayout.setExpandRatio(headerLayout, 2.0f);

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
                        retieneIsrLbl.setValue((String.valueOf(clienteCbx.getContainerProperty(clienteCbx.getValue(), RETIENEISR_PROPERTY).getValue()).equals("1")) ? "RETIENE ISR" : "");
                        retieneIvaLbl.setValue((String.valueOf(clienteCbx.getContainerProperty(clienteCbx.getValue(), RETIENEIVA_PROPERTY).getValue()).equals("1")) ? "RETIENE IVA" : "");
                    }
                }
        );

        nitClienteTxt = new TextField("Nit : ");
        nitClienteTxt.setWidth("100%");

        nit_cui_passCbx = new ComboBox("NIT, DPI o Pasaporte: ");
        nit_cui_passCbx.setDescription("Que ID se usara para indentificar al Cliente, \n\t- NIT \n\t- DPI \n\t- Pasaporte");
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
//        exentaChb.setReadOnly(true);

        firstLineHeaderLayout.addComponents(tipoFacturaVentaCbx,fechaDt, clienteCbx, nitClienteTxt, nit_cui_passCbx, retieneIsrLbl, retieneIvaLbl);
        firstLineHeaderLayout.setComponentAlignment(tipoFacturaVentaCbx, Alignment.MIDDLE_LEFT);
        firstLineHeaderLayout.setComponentAlignment(fechaDt, Alignment.MIDDLE_LEFT);
        firstLineHeaderLayout.setComponentAlignment(clienteCbx, Alignment.MIDDLE_LEFT);
        firstLineHeaderLayout.setComponentAlignment(nitClienteTxt, Alignment.MIDDLE_LEFT);
        firstLineHeaderLayout.setComponentAlignment(nit_cui_passCbx, Alignment.MIDDLE_LEFT);
        firstLineHeaderLayout.setComponentAlignment(retieneIsrLbl, Alignment.BOTTOM_LEFT);
        firstLineHeaderLayout.setComponentAlignment(retieneIvaLbl, Alignment.BOTTOM_LEFT);

        firstLineHeaderLayout.setExpandRatio(tipoFacturaVentaCbx, 1.5f);
        firstLineHeaderLayout.setExpandRatio(fechaDt, 1.5f);
        firstLineHeaderLayout.setExpandRatio(clienteCbx, 3.5f);
        firstLineHeaderLayout.setExpandRatio(nitClienteTxt, 2.0f);
        firstLineHeaderLayout.setExpandRatio(nit_cui_passCbx, 1.0f);
        firstLineHeaderLayout.setExpandRatio(retieneIsrLbl, 0.5f);
        firstLineHeaderLayout.setExpandRatio(retieneIvaLbl, 0.5f);

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

        modoOg.setValue(!((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyFelUser().trim().isEmpty());

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
        secondLineHaderLayout.setComponentAlignment(modoOg, Alignment.BOTTOM_RIGHT);
        secondLineHaderLayout.setComponentAlignment(totalFacturadoMesTxt, Alignment.MIDDLE_RIGHT);
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

    private void createDocumentDetail() {
        HorizontalLayout detailLayout = new HorizontalLayout();
        detailLayout.setSizeFull();
        detailLayout.setSpacing(true);
        detailLayout.setMargin(false);
//        detailLayout.setMargin(new MarginInfo(false, true, false, true));

        centerContentPanel = new Panel();
        centerContentPanel.setSizeFull();

        centralVerticalLayout = new VerticalLayout();
        centralVerticalLayout.setWidth("100%");
        centralVerticalLayout.setSpacing(true);
//        centralVerticalLayout.setMargin(new MarginInfo(false, true, false, false));

        centerContentPanel.setContent(centralVerticalLayout);

        detailLayout.addComponent(centerContentPanel);
        detailLayout.setComponentAlignment(centerContentPanel, Alignment.MIDDLE_CENTER);
        detailLayout.setExpandRatio(centerContentPanel, 1.0f);

        mainLayout.addComponents(detailLayout);
        mainLayout.setExpandRatio(detailLayout, 3.0f);


        cargarProductosDesdeBD();
        addProducto(true);

        llenarComboCliente();

    }

    public interface ProductoSeleccionadoListener {
        void productoSeleccionado();
    }

    public interface MontoTotalChangedListener {
        void montoTotalChanged();
    }

    public void montoTotalChanged() {
        calcularMontos();  // Actualiza el total
    }

    private boolean verificarFrases(){
        boolean tieneFrases = true;
        boolean frasesIguales = true;
        Map<Integer, Integer> mapaFrases = productoVentaList.get(0).getProducto().getFrases();
        for (ProductoVenta p : productoVentaList) {
            if(p.getProducto() == null) {
               continue;
            }
            if(p.getProducto().getFrases().isEmpty()) {
                tieneFrases = false;
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "ERROR NO TIENE FRASES, PRODUCTO: " + p.getIdProducto() + "|" + p.getProducto().getNombre());
                Notification.show("ERROR NO TIENE FRASES, HABLE CON ENCARGADORA DE SISTEMAS", Notification.Type.ERROR_MESSAGE);
                break;
            }
            if (!p.getProducto().getFrases().equals(mapaFrases)) {
                frasesIguales = false;
                Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "CADA PRODUCTO TIENE QUE TENER LAS MISMAS FRASES");
                Notification.show("Cada producto tiene que tener las mismas frases", Notification.Type.WARNING_MESSAGE);
                break;
            }
        }

        return frasesIguales && tieneFrases;
    }

    public void cargarProductosDesdeBD() {
        List<ProductoVentaEmpresaDTO> lista = new ArrayList<>();

        queryString = "SELECT * FROM producto_venta_empresa ";
        queryString += "WHERE IdEmpresa = " + ((SopdiUI)mainUI).sessionInformation.getStrAccountingCompanyId() + " AND Especial = 0";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) { //
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


                ProductoVentaEmpresaDTO dto = new ProductoVentaEmpresaDTO(
                        rsRecords.getInt("IdEmpresa"),
                        rsRecords.getString("IdProducto"),
                        rsRecords.getString("NombreProducto"),
                        rsRecords.getInt("IdNomenclatura"),
                        rsRecords.getString("InfileTipo"),
                        rsRecords.getInt("CorrelativoProducto"),
                        rsRecords.getBoolean("Especial"),
                        frases
                );
                lista.add(dto);
            }
            productoEmpresaList = lista;
        } catch (Exception ex1) {
            System.out.println("Error al listar de clientes (tabla proveedores) " + ex1.getMessage());
            ex1.printStackTrace();
        }

    }


    private void addProducto(boolean caption) {
        ProductoVenta p = new ProductoVenta(
                caption,
                () -> addProducto(false),
                this::montoTotalChanged,
                centroCostoMap,
                productoEmpresaList);

        if (headProductoVenta == null) {
            headProductoVenta = p;
        } else {
            productoVentaList.get(productoVentaList.size() - 1).setNext(p);
            p.setPrevious(productoVentaList.get(productoVentaList.size() - 1));
        }

        productoVentaList.add(p);

        HorizontalLayout pLayout = p.getLayoutHorizontal();

        if(productoVentaList.size() > 1) {
            Button eliminarBtn = new Button(FontAwesome.BAN);
            eliminarBtn.setStyleName(ValoTheme.BUTTON_BORDERLESS);
            eliminarBtn.setData(pLayout);
            eliminarBtn.addClickListener(event -> {
                HorizontalLayout layout = (HorizontalLayout) event.getButton().getData();
                centralVerticalLayout.removeComponent(layout);

                productoVentaList.remove(p); // Remover de la lista también, si aplica
                p.getPrevious().setNext(p.getNext());
                if (p.getNext() != null) p.getNext().setPrevious(p.getPrevious());
                // Hacer que tenga Margen inferior
                if (p.getNext() == null) p.getPrevious().getLayoutHorizontal().setMargin(new MarginInfo(false, true, true, true));
            });
            pLayout.addComponent(eliminarBtn);
            pLayout.setComponentAlignment(eliminarBtn, Alignment.BOTTOM_CENTER);
        } else {
            // Espacio del mismo tamaño para alinear visualmente
            Component placeholder = new Label();
            placeholder.setWidth("2.8em"); // Ajusta según el ancho real del botón
// Agregar el componente al layout
            pLayout.addComponent(placeholder);
            pLayout.setComponentAlignment(placeholder, Alignment.BOTTOM_CENTER);
        }

        if (p.getPrevious() != null) {
            p.getPrevious().getLayoutHorizontal().setMargin(new MarginInfo(false, true, false, true));
        }
        pLayout.setMargin(new MarginInfo(false, true, true, true));


        centralVerticalLayout.addComponent(pLayout);
        centralVerticalLayout.setComponentAlignment(pLayout, Alignment.MIDDLE_CENTER);
        centerContentPanel.setScrollTop(Integer.MAX_VALUE);
    }



    private void createDocumentFoother() {
        footerLayout = new HorizontalLayout();
        footerLayout.setWidth("50%");
        footerLayout.setHeight("5em");
        footerLayout.setStyleName("rcorners3");
        footerLayout.setSpacing(true);
        footerLayout.setMargin(new MarginInfo(false, true, true, true));

        mainLayout.addComponents(footerLayout);
        mainLayout.setComponentAlignment(footerLayout, Alignment.BOTTOM_RIGHT);
        mainLayout.setExpandRatio(footerLayout, 1.0f);

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

        if(((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyRegimen().equals("EXENTA")) {
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

        footerLayout.addComponents(montoTxt, ivaTxt, isrTxt, grabarBtn);
        footerLayout.setComponentAlignment(montoTxt, Alignment.MIDDLE_CENTER);
        footerLayout.setComponentAlignment(ivaTxt, Alignment.MIDDLE_CENTER);
        footerLayout.setComponentAlignment(isrTxt, Alignment.MIDDLE_CENTER);
        footerLayout.setComponentAlignment(grabarBtn, Alignment.BOTTOM_RIGHT);

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

    public void mostrarFacturadoMes() {

        totalFacturadoMesTxt.setReadOnly(false);
        totalFacturadoMesTxt.setValue(0.00);

        queryString = " SELECT SUM(DEBE - HABER) as TOTALFACTURADO ";
        queryString += " FROM contabilidad_partida ";
        queryString += " WHERE IdNomenclatura = " + ((SopdiUI) mainUI).cuentasContablesDefault.getClientes();
        queryString += " And TipoDocumento IN ('FACTURA VENTA', 'RECIBO CONTABLE',  'RECIBO CONTABLE VENTA')";
        queryString += " And IdEmpresa = " + empresaCbx.getValue();
        queryString += " And Extract(YEAR_MONTH FROM Fecha) = " + Utileria.getFechaYYYYMM(new Date());

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

        exenta = true;
        verificarFrases();

        for (ProductoVenta productoVenta : productoVentaList) {
            if(productoVenta.calcularMontos() > 0){
                exenta = exenta && productoVenta.getProducto().tieneFrase(InfileClient.EXENTOIVA_FRASE);
                double monto = productoVenta.calcularMontos();
                total += monto;
                netoMontoTotal += monto / 1.12;
                ivaMontoTotal += monto - (monto / 1.12);
            }
        }

        montoTxt.setValue(numberFormat.format(total));


        double base = Double.valueOf(Utileria.format((montoTxt.getDoubleValueDoNotThrow() / 1.12)));

        if(exenta) {
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

    public void insertTablaFactura() {
    /* 16/05/2025 Ya no se hara uso de esto.
    //  ---  Verificar Que no se repitan Cuentas  ---
        for(int i = 0; i < TOTAL_PRODUCTOS; i++) {
            Object temp1 = cuentaContableList.get(i).getValue();
            if(temp1 != null){
                for(int j = 0; j < TOTAL_PRODUCTOS; j++) {
                    Object temp2 = cuentaContableList.get(j).getValue();
                    if(temp2 != null){
                        if(temp2.equals(temp1) && (i != j)){
                            Notification.show("No puede utilizar la misma cuenta para dos registros de la partida.", Notification.Type.ERROR_MESSAGE);
                            return;
                        }
                    }
                }
            }
        }
        */
        if (datosValidos()) {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "DATOS VALIDOS OK!");

            if (!((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyFelUser().trim().isEmpty() && modoOg.getValue()) {
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "ES FEL!");
                if (documentoCeritficaroInfile()) {
                    String serie = infileClient.getSerie();
                    String numero = String.valueOf(infileClient.getNumero());
                    numeroTxt.setValue(numero);
                    serieTxt.setValue(serie);

                    pdfFile = infileClient.obtenerDTEPdf(((SopdiUI) UI.getCurrent()).enviromentsVars.getDtePath() + serie + "_" + numero + ".pdf");

                    if (pdfFile == null) {
                        Notification.show("ERROR AL OBTENER PDF DEL DTE, NOTIFIQUE!", Notification.Type.ERROR_MESSAGE);
                        Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "ERROR AL OBTENER PDF DEL DTE, VERIFIQUE!");
                    }
                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, "DOCUMENTO CERTIFICDADO FEL OK!");
                    Notification.show("DOCUMENTO CERTIFICADO FEL OK!", Notification.Type.HUMANIZED_MESSAGE);
                } else {

                    Notification.show("ERROR AL CERTIFICAR DOCUMENTO CON FEL, NOTIFIQUE!", Notification.Type.ERROR_MESSAGE);

                    Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "ERROR AL CERTIFICAR DOCUMENTO CON FEL, VERIFIQUE!" + infileClient.getDescripcionErrores().toString());

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

//        totalHaber = new BigDecimal(montoList.get(i).getDoubleValueDoNotThrow()
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
        if (!((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyRegimen().equals("EXENTA")) {
            queryString += " And   TipoDocumento = 'FACTURA VENTA'";
        } else {
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

        if(!verificarFrases()) return false;
        codigoPartida = Utileria.nextCodigoPartida(
                ((SopdiUI)mainUI).databaseProvider.getCurrentConnection(),
                ((SopdiUI)mainUI).sessionInformation.getStrAccountingCompanyId(),
                fechaDt.getValue(),
                0
        );

        return true;
    }

    private boolean documentoCeritficaroInfile(){

        Receptor receptor = new Receptor(
                nitClienteTxt.getValue().replaceAll("-", ""),
                clienteCbx.getContainerProperty(clienteCbx.getValue(), NOMBRESINCODIGO_PROPERTY).getValue().toString(),
                clienteCbx.getContainerProperty(clienteCbx.getValue(), CORREO_PROPERTY).getValue().toString(),
                clienteCbx.getContainerProperty(clienteCbx.getValue(), DIRECCION_PROPERTY).getValue().toString()
        );

        List<Producto> productoList = new ArrayList<>();

        for (ProductoVenta productoVenta : productoVentaList) {
            if (productoVenta.calcularMontos() > 0){
                productoList.add(productoVenta.getProducto());
            }

        }
        if (productoList.isEmpty()) {
            Notification.show("Debe ingresar al menos un producto para continuar.", Notification.Type.WARNING_MESSAGE);
            return false;
        }

        infileClient = new InfileClient(((SopdiUI)mainUI).sessionInformation.getInfileEmisor());

        return infileClient.generarDocumentoBase(
                receptor,
                codigoPartida,
                productoList,
                (((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyRegimen().equals("EXENTA"))?"RDON":"FACT",
                "",
                fechaDt.getValue(),
                monedaCbx.getValue().toString().equals("DOLARES")?"USD":"GTQ",
                tasaCambioTxt.getDoubleValueDoNotThrow()
                );

    }


    private void insertarPartidas() {

        boolean igualCentroCosto = true;
        Object primerCentroCosto = null;
        for(ProductoVenta productoVenta : productoVentaList) {
            if(productoVenta.getCentroCostoId() != null) {
                if(primerCentroCosto == null) {
                    primerCentroCosto = productoVenta.getCentroCostoId();
                }
                else {
                    if(!primerCentroCosto.equals(productoVenta.getCentroCostoId())) {
                        igualCentroCosto = false;
                        break;
                    }
                }
            }
        }

        queryString = " Insert Into proveedor_cuentacorriente (IdEmpresa,IdProveedor, Fecha,";
        queryString += " TipoDocumento, SerieDocumento,NumeroDocumento, MonedaDocumento, ";
        queryString += " Monto, MontoQuetzales, TipoCambio ";
        queryString += ", IdUsuarioAutorizoPago,CreadoFechayHora,CreadoUsuario)";
        queryString += " Values(";
        queryString += empresaCbx.getValue();
        queryString += "," + clienteCbx.getValue();
        queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
        if(!((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyRegimen().equals("EXENTA")) {
            queryString += ",'FACTURA VENTA'";
        }
        else {
            queryString += ",'RECIBO CONTABLE VENTA'";
        }
        queryString += ",UPPER('" + serieTxt.getValue().trim() + "')";
        queryString += ",'" + numeroTxt.getValue().trim() + "'";
        queryString += ",'" + monedaCbx.getValue() + "'";
        queryString += ", " + montoTxt.getDoubleValueDoNotThrow();
        queryString += "," + Utileria.format(montoTxt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
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

        queryString = " Insert Into contabilidad_partida (IdEmpresa, Estatus, CodigoPartida,CodigoCC, ";
        queryString += " TipoDocumento, TipoVenta, Fecha, IdProveedor, NitProveedor, NombreProveedor,";
        queryString += " SerieDocumento, NumeroDocumento, IdNomenclatura, MonedaDocumento, MontoDocumento, Debe, Haber,";
        queryString += " DebeQuetzales, HaberQuetzales, TipoCambio, Saldo, Descripcion, Referencia,IdCentroCosto, CodigoCentroCosto,";
        queryString += " CreadoUsuario, CreadoFechaYHora, Archivo, ArchivoTipo, ArchivoPeso, ArchivoNombre,";
        queryString += " UUID, FechaYHoraCertificacion, XmlRequest, XmlResponse, IdProducto";
        queryString += ")";
        queryString += " Values ";
        queryString += "(";
        queryString += empresaCbx.getValue();
        queryString += ",'INGRESADO'";
        queryString += ",'" + codigoPartida + "'";
        queryString += ",'" + codigoPartida + "'";
        if(!((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyRegimen().equals("EXENTA")) {
            queryString += ",'FACTURA VENTA'";
        }
        else {
            queryString += ",'RECIBO CONTABLE VENTA'";
        }
        queryString += ",'" + productoVentaList.get(0).getTipo() + "'";
        queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
        queryString += "," + clienteCbx.getValue();
        queryString += ",'" + nitClienteTxt.getValue() + "'";
        queryString += ",'" + clienteCbx.getItem(clienteCbx.getValue()).getItemProperty(NOMBRESINCODIGO_PROPERTY).getValue() + "'";
        queryString += ",UPPER('" + serieTxt.getValue().trim() + "')";
        queryString += ",'" + numeroTxt.getValue().trim() + "'";

        queryString += "," + ((SopdiUI) mainUI).cuentasContablesDefault.getClientes();

        queryString += ",'" + monedaCbx.getValue() + "'";
        queryString += "," + montoTxt.getDoubleValueDoNotThrow(); //MONTODOCUMENTO
        queryString += "," + montoTxt.getDoubleValueDoNotThrow(); //DEBE
        queryString += ",0.00"; //HABER
        queryString += "," + Utileria.format(montoTxt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
        queryString += ",0.00"; //HABER Q.
        queryString += "," + tasaCambioTxt.getDoubleValueDoNotThrow();
        queryString += "," + montoTxt.getDoubleValueDoNotThrow(); // SALDO
        if(!((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyRegimen().equals("EXENTA")) {
            queryString += ",'FACTURA VENTA " + clienteCbx.getItem(clienteCbx.getValue()).getItemProperty(NOMBRESINCODIGO_PROPERTY).getValue() + "'";
        }
        else {
            queryString += ",'RECIBO CONTABLE VENTA " + clienteCbx.getItem(clienteCbx.getValue()).getItemProperty(NOMBRESINCODIGO_PROPERTY).getValue() + "'";
        }
        queryString += ",'NO'"; //referencia
        if(igualCentroCosto && primerCentroCosto != null) {
            queryString += "," + primerCentroCosto;
            queryString += ",'" + centroCostoMap.get(primerCentroCosto) + "'";
        }
        else {
            queryString += ",null";
            queryString += ",null";
        }
        queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
        queryString += ",current_timestamp";
        queryString += ",null"; //archivo
        queryString += ",'application/pdf'"; //archivo tipo
        queryString += ","  + (pdfFile != null ? pdfFile.length() : 0);    //archivo size
        queryString += ",'" + (pdfFile != null ? pdfFile.getAbsolutePath() : "").replace("\\", "/") + "'";
        if(modoOg.getValue()) {
            queryString += ",'" + infileClient.getUUID() + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(infileClient.getFechaHoraCertificacion()) + "'";
        }else{
            queryString += ",null";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
        }
        queryString += ",''"; //XmlRequest
        queryString += ",''"; //XmlResponse
        queryString += ",null)"; // IdProducto

        for (ProductoVenta productoVenta : productoVentaList) {
            if (productoVenta.calcularMontos() != 0) {
                queryString += ",(";
                queryString += empresaCbx.getValue();
                queryString += ",'INGRESADO'";
                queryString += ",'" + codigoPartida + "'";
                queryString += ",'" + codigoPartida + "'";
                if (!((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyRegimen().equals("EXENTA")) {
                    queryString += ",'FACTURA VENTA'";
                } else {
                    queryString += ",'RECIBO CONTABLE VENTA'";
                }
                queryString += ",'" + productoVenta.getTipo() + "'";
                queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
                queryString += "," + clienteCbx.getValue();
                queryString += ",'" + nitClienteTxt.getValue() + "'";
                queryString += ",'" + clienteCbx.getItem(clienteCbx.getValue()).getItemProperty(NOMBRESINCODIGO_PROPERTY).getValue() + "'";
                queryString += ",UPPER('" + serieTxt.getValue().trim() + "')";
                queryString += ",'" + numeroTxt.getValue().trim() + "'";
                queryString += "," + productoVenta.getIdNomenclatura();
                queryString += ",'" + monedaCbx.getValue() + "'";
                queryString += "," + montoTxt.getDoubleValueDoNotThrow(); //MONTODOCUMENTO
                queryString += ",0.00"; //DEBE
                if (!exenta) {
                    queryString += "," + Utileria.format(productoVenta.calcularMontos() / 1.12); // HABER
                } else {
                    queryString += "," + Utileria.format(productoVenta.calcularMontos()); // HABER
                }
                queryString += ",0.00"; //DEBE Q.
                if (!exenta) {
                    queryString += "," + Utileria.format((productoVenta.calcularMontos() / 1.12) * tasaCambioTxt.getDoubleValueDoNotThrow());
                } else {
                    queryString += "," + Utileria.format(productoVenta.calcularMontos() * tasaCambioTxt.getDoubleValueDoNotThrow());
                }
                queryString += "," + tasaCambioTxt.getDoubleValueDoNotThrow();
                queryString += ",0.00"; //SALDO
                if (!((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyRegimen().equals("EXENTA")) {
                    queryString += ",'FACTURA VENTA " + clienteCbx.getItem(clienteCbx.getValue()).getItemProperty(NOMBRESINCODIGO_PROPERTY).getValue() + "'";
                } else {
                    queryString += ",'RECIBO CONTABLE VENTA " + clienteCbx.getItem(clienteCbx.getValue()).getItemProperty(NOMBRESINCODIGO_PROPERTY).getValue() + "'";
                }
                queryString += ",'NO'"; //referencia
                queryString += "," + productoVenta.getCentroCostoId();
                queryString += ",'" + productoVenta.getCentroCosto() + "'";
                queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                queryString += ",current_timestamp";
                queryString += ",null"; //archivo
                queryString += ",'application/pdf'"; //archivo tipo
                queryString += "," + (pdfFile != null ? pdfFile.length() : 0);    //archvio size
                queryString += ",'" + (pdfFile != null ? pdfFile.getAbsolutePath() : "").replace("\\", "/") + "'";
                if(modoOg.getValue()) {
                    queryString += ",'" + infileClient.getUUID() + "'";
                    queryString += ",'" + Utileria.getFechaYYYYMMDD_1(infileClient.getFechaHoraCertificacion()) + "'";
                }else{
                    queryString += ",null";
                    queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
                }
                queryString += ",''";
                queryString += ",''";
                queryString += ", " + productoVenta.getIdProducto() + ")";
            }
        }

        if(!((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyRegimen().equals("EXENTA")) {

System.out.println("entra a insertar linea del iva.  exenta=" + exenta + " getIvaPorPagar()=" + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getIvaPorPagar());
            //// INSERTAR EL IVA
            queryString += ",(";
            queryString += empresaCbx.getValue();
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'FACTURA VENTA'";
            queryString += ",'" + productoVentaList.get(0).getTipo() + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + clienteCbx.getValue();
            queryString += ",'" + nitClienteTxt.getValue() + "'";
            queryString += ",'" + clienteCbx.getItem(clienteCbx.getValue()).getItemProperty(NOMBRESINCODIGO_PROPERTY).getValue() + "'";
            queryString += ",UPPER('" + serieTxt.getValue().trim() + "')";
            queryString += ",'" + numeroTxt.getValue().trim() + "'";
            queryString += "," + ((SopdiUI) mainUI).cuentasContablesDefault.getIvaPorPagar();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + montoTxt.getDoubleValueDoNotThrow(); //MONTODOCUMENTO
            queryString += ",0.00"; //DEBE
            queryString += "," + ivaTxt.getDoubleValueDoNotThrow(); // HABER
            queryString += ",0.00"; //DEBE Q.
            queryString += "," + Utileria.format(ivaTxt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + tasaCambioTxt.getDoubleValueDoNotThrow();
            queryString += ",0.00"; //SALDO
            queryString += ",'FACTURA VENTA " + clienteCbx.getItem(clienteCbx.getValue()).getItemProperty(NOMBRESINCODIGO_PROPERTY).getValue() + "'";
            queryString += ",'NO'"; //referencia
            if(igualCentroCosto && primerCentroCosto != null) {
                queryString += "," + primerCentroCosto;
                queryString += ",'" + centroCostoMap.get(primerCentroCosto) + "'";
            }
            else {
                queryString += ",null";
                queryString += ",null";
            }
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ",null"; //archivo
            queryString += ",'application/pdf'"; //archivo tipo
            queryString += "," + (pdfFile != null ? pdfFile.length() : 0);    //archvio size
            queryString += ",'" + (pdfFile != null ? pdfFile.getAbsolutePath() : "").replace("\\", "/") + "'";
            if(modoOg.getValue()) {
                queryString += ",'" + infileClient.getUUID() + "'";
                queryString += ",'" + Utileria.getFechaYYYYMMDD_1(infileClient.getFechaHoraCertificacion()) + "'";
            }else{
                queryString += ",null";
                queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            }
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
                queryString += empresaCbx.getValue();
                queryString += ",'INGRESADO'";
                queryString += ",'" + codigoPartida + "'";
                queryString += ",'" + codigoPartida + "'";
                queryString += ",'FACTURA VENTA'";
                queryString += ",'" + productoVentaList.get(0).getTipo() + "'";
                queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
                queryString += "," + clienteCbx.getValue();
                queryString += ",'" + nitClienteTxt.getValue() + "'";
                queryString += ",'" + clienteCbx.getItem(clienteCbx.getValue()).getItemProperty(NOMBRESINCODIGO_PROPERTY).getValue() + "'";
                queryString += ",UPPER('" + serieTxt.getValue().trim() + "')";
                queryString += ",'" + numeroTxt.getValue().trim() + "'";
                queryString += "," + ((SopdiUI) mainUI).cuentasContablesDefault.getIsrGasto();
                queryString += ",'" + monedaCbx.getValue() + "'";
                queryString += "," + montoTxt.getDoubleValueDoNotThrow(); //MONTODOCUMENTO
                queryString += "," + isrTxt.getDoubleValueDoNotThrow(); // DEBE
                queryString += ",0.00"; //HABER
                queryString += "," + Utileria.format(isrTxt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
                queryString += ",0.00"; //HABER Q.
                queryString += "," + tasaCambioTxt.getDoubleValueDoNotThrow();
                queryString += ",0.00"; //SALDO
                queryString += ",'FACTURA VENTA " + clienteCbx.getItem(clienteCbx.getValue()).getItemProperty(NOMBRESINCODIGO_PROPERTY).getValue() + "'";
                queryString += ",'NO'"; //referencia
                if(igualCentroCosto && primerCentroCosto != null) {
                    queryString += "," + primerCentroCosto;
                    queryString += ",'" + centroCostoMap.get(primerCentroCosto) + "'";
                }
                else {
                    queryString += ",null";
                    queryString += ",null";
                }
                queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                queryString += ",current_timestamp";
                queryString += ",null"; //archivo
                queryString += ",'application/pdf'"; //archivo tipo
                queryString += "," + (pdfFile != null ? pdfFile.length() : 0);    //archvio size
                queryString += ",'" + (pdfFile != null ? pdfFile.getAbsolutePath() : "").replace("\\", "/") + "'";
                if(modoOg.getValue()) {
                    queryString += ",'" + infileClient.getUUID() + "'";
                    queryString += ",'" + Utileria.getFechaYYYYMMDD_1(infileClient.getFechaHoraCertificacion()) + "'";
                }else{
                    queryString += ",null";
                    queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
                }
                queryString += ",''";
                queryString += ",''";
                queryString += ",null)";

                //// ISR OPCIONAL MENSUAL POR PAGAR
                queryString += ",(";
                queryString += empresaCbx.getValue();
                queryString += ",'INGRESADO'";
                queryString += ",'" + codigoPartida + "'";
                queryString += ",'" + codigoPartida + "'";
                queryString += ",'FACTURA VENTA'";
                queryString += ",'" + productoVentaList.get(0).getTipo() + "'";
                queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
                queryString += "," + clienteCbx.getValue();
                queryString += ",'" + nitClienteTxt.getValue() + "'";
                queryString += ",'" + clienteCbx.getItem(clienteCbx.getValue()).getItemProperty(NOMBRESINCODIGO_PROPERTY).getValue() + "'";
                queryString += ",UPPER('" + serieTxt.getValue().trim() + "')";
                queryString += ",'" + numeroTxt.getValue().trim() + "'";
                queryString += "," + ((SopdiUI) mainUI).cuentasContablesDefault.getIsrOpcionalMensualPorPagar();
                queryString += ",'" + monedaCbx.getValue() + "'";
                queryString += "," + montoTxt.getDoubleValueDoNotThrow(); //MONTODOCUMENTO
                queryString += ",0.00"; //DEBE
                queryString += "," + isrTxt.getDoubleValueDoNotThrow(); // HABER
                queryString += ",0.00"; //DEBE Q.
                queryString += "," + Utileria.format(isrTxt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow());
                queryString += "," + tasaCambioTxt.getDoubleValueDoNotThrow();
                queryString += ",0.00"; //SALDO
                queryString += ",'FACTURA VENTA " + clienteCbx.getItem(clienteCbx.getValue()).getItemProperty(NOMBRESINCODIGO_PROPERTY).getValue() + "'";
                queryString += ",'NO'"; //referencia
                if(igualCentroCosto && primerCentroCosto != null) {
                    queryString += "," + primerCentroCosto;
                    queryString += ",'" + centroCostoMap.get(primerCentroCosto) + "'";
                }
                else {
                    queryString += ",null";
                    queryString += ",null";
                }
                queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                queryString += ",current_timestamp";
                queryString += ",null"; //archivo
                queryString += ",'application/pdf'"; //archivo tipo
                queryString += "," + (pdfFile != null ? pdfFile.length() : 0);    //archvio size
                queryString += ",'" + (pdfFile != null ? pdfFile.getAbsolutePath() : "").replace("\\", "/") + "'";
                if(modoOg.getValue()) {
                    queryString += ",'" + infileClient.getUUID() + "'";
                    queryString += ",'" + Utileria.getFechaYYYYMMDD_1(infileClient.getFechaHoraCertificacion()) + "'";
                }else{
                    queryString += ",null";
                    queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
                }
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

            ((FacturaVentaView) (mainUI.getNavigator().getCurrentView())).llenarTablaFacturaVenta(String.valueOf(empresaCbx.getValue()));

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
                String[] emailsTo = {"alerta@simpletecno.com"};
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

            /*try {
                String emailsTo[] = {"alerta@simpletecno.com"};
                MyEmailMessanger eMail = new MyEmailMessanger();

                eMail.postMail(emailsTo, "Error en SOPDI", "Error en base de datos :  " + this.getClass().getName() + " -->" + ex1.getMessage());
            } catch (MessagingException ex2) {
                Logger.getLogger(SopdiUI.class.getName()).log(Level.SEVERE, null, ex2);
            }*/
        }
    }

    private void buscarProveedorPorNit() {
        for (Iterator<?> i = clienteCbx.getItemIds().iterator(); i.hasNext();) {
            String id = (String) i.next();
            Item item = clienteCbx.getItem(id);

            if (nitClienteTxt.getValue().equals(String.valueOf(item.getItemProperty(NIT_PROPERTY).getValue()).trim())) {
                clienteCbx.select(id);
                break;
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

    class ProductoVenta {

        static final String PRODUCTO_PROPERTY = "ProductoNota";
        static final String IDNOMENCLATURA_PROPERTY = "IdNomenclatura";


        private final List<ProductoVentaEmpresaDTO> productosCache; // todos los productos disponibles

        private ProductoVenta next;
        private ProductoVenta previous;

        HorizontalLayout layoutHorizontal;

        NumberField cantidadTxt;
        ComboBox productoCbx;
        TextField referenciaTxt;
        NumberField haberTxt;
        TextField tipoDocumento;
        ComboBox centroCostoCbx;


        ProductoVenta(Boolean caption, ProductoSeleccionadoListener listenerProducto, MontoTotalChangedListener listenerMonto,
                      Map<Integer, String> centroCostoMap, List<ProductoVentaEmpresaDTO> productosCache) {

            this.productosCache = productosCache;
            layoutHorizontal = new HorizontalLayout();
            layoutHorizontal.setResponsive(true);
            layoutHorizontal.setSpacing(true);
            layoutHorizontal.setWidth("95%");

            // Elementos
            this.cantidadTxt = new NumberField();    // Cantidad
            this.tipoDocumento = new TextField();    // Tipo Documento (Servicio | producto)
            this.productoCbx = new ComboBox();       // ProductoNota
            this.referenciaTxt = new TextField();    // Referencia
            this.haberTxt = new NumberField();       // Monto
            this.centroCostoCbx = new ComboBox();    // Centro de Costo

            // CANTIDAD
            if(caption) cantidadTxt.setCaption("CANT.");
            cantidadTxt.setDecimalAllowed(true);
            cantidadTxt.setDecimalPrecision(0);
            cantidadTxt.setMinimumFractionDigits(0);
            cantidadTxt.setValue(1d);
            cantidadTxt.setImmediate(true);
            cantidadTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);
            cantidadTxt.setSizeFull();
            cantidadTxt.addValueChangeListener(event -> {
                if (listenerMonto != null) {
                    listenerMonto.montoTotalChanged();
                }
            });

            // Tipo Documento
            if (caption) tipoDocumento.setCaption("Tipo :");
            tipoDocumento.setSizeFull();
            tipoDocumento.setEnabled(false);

            // ProductoNota
            if(caption) productoCbx.setCaption("PRODUCTO O SERVICIO");
            productoCbx.setSizeFull();
            productoCbx.addContainerProperty(PRODUCTO_PROPERTY, Producto.class, null);
            productoCbx.addContainerProperty(IDNOMENCLATURA_PROPERTY, Integer.class, "");
            productoCbx.addValueChangeListener((event) -> {
                Object selected = productoCbx.getValue();
                if (selected != null) {
                    tipoDocumento.setValue(((Producto)productoCbx.getContainerProperty(selected, PRODUCTO_PROPERTY).getValue()).getBienOServicio());
                    haberTxt.setEnabled(true);
                    referenciaTxt.setEnabled(true);

                    if (listenerProducto != null && next == null) {
                        listenerProducto.productoSeleccionado();
                    }
                }
            });
            llenarComboProductoServicio();

            // Referencia
            if (caption) referenciaTxt.setCaption("Referencia :");
            referenciaTxt.setSizeFull();
            referenciaTxt.setEnabled(false);

            // Monto
            if (caption) haberTxt.setCaption("Monto : ");
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
            haberTxt.setSizeFull();
            haberTxt.setValue(0.00);
            haberTxt.setEnabled(false);
            haberTxt.addValueChangeListener(event -> {
                if (listenerMonto != null) {
                    listenerMonto.montoTotalChanged();
                }
            });

            if(caption) centroCostoCbx.setCaption("Centro Costo");
            centroCostoCbx.setWidth("100%");
            centroCostoCbx.setDescription("Centro de costo");
            centroCostoCbx.setTextInputAllowed(false);
//        centroCostoCbx.setInvalidAllowed(true);
            centroCostoCbx.setNullSelectionAllowed(true);
            for(Object o : centroCostoMap.keySet() ){
                if (o != null) {
                    centroCostoCbx.addItem(o);
                    centroCostoCbx.setItemCaption(o, centroCostoMap.get(o));
                }
            }

            //
            layoutHorizontal.addComponents(cantidadTxt, tipoDocumento, productoCbx, referenciaTxt, haberTxt, centroCostoCbx);

            layoutHorizontal.setComponentAlignment(cantidadTxt, Alignment.TOP_CENTER);
            layoutHorizontal.setComponentAlignment(tipoDocumento, Alignment.TOP_CENTER);
            layoutHorizontal.setComponentAlignment(productoCbx, Alignment.TOP_CENTER);
            layoutHorizontal.setComponentAlignment(referenciaTxt, Alignment.TOP_CENTER);
            layoutHorizontal.setComponentAlignment(haberTxt, Alignment.TOP_CENTER);
            layoutHorizontal.setComponentAlignment(centroCostoCbx, Alignment.TOP_CENTER);

            layoutHorizontal.setExpandRatio(cantidadTxt, 0.7f);
            layoutHorizontal.setExpandRatio(tipoDocumento, 1.2f);
            layoutHorizontal.setExpandRatio(productoCbx, 4f);
            layoutHorizontal.setExpandRatio(referenciaTxt, 3f);
            layoutHorizontal.setExpandRatio(haberTxt, 2f);
            layoutHorizontal.setExpandRatio(centroCostoCbx, 2f);
        }

        public String getTipo(){
            return tipoDocumento.getValue();
        }

        private void llenarComboProductoServicio() {

            // Limpia el combo si aplica
            productoCbx.removeAllItems();

            // Filtra en memoria (sin SQL)
            for (ProductoVentaEmpresaDTO dto : productosCache) {

                String idProducto = dto.idProducto;
                productoCbx.addItem(idProducto);
                productoCbx.setItemCaption(idProducto, dto.nombreProducto);
                productoCbx.getContainerProperty(idProducto, IDNOMENCLATURA_PROPERTY).setValue(dto.idNomenclatura);

                // Construye tu objeto ProductoNota como antes
                Producto p = new Producto(
                        dto.nombreProducto,
                        0.00,
                        0,
                        dto.nombreProducto,
                        dto.infileTipo
                );

                // Set de frases desde el mapa en memoria
                p.setFrases(dto.frases);

                productoCbx.getContainerProperty(idProducto, PRODUCTO_PROPERTY).setValue(p);
            }
        }



        public double calcularMontos() {
            double monto = 0.00;
            try {
                if (cantidadTxt.getDoubleValueDoNotThrow() > 0 && haberTxt.getDoubleValueDoNotThrow() >= 0) {
                    monto = cantidadTxt.getDoubleValueDoNotThrow() * haberTxt.getDoubleValueDoNotThrow();
                }
            } catch (Exception e) {
                System.out.println("Error al calcular montos: " + e.getMessage());
            }
            return Utileria.round(monto);
        }

        public String getIdNomenclatura() {
            Object selected = productoCbx.getValue();
            if (selected != null) {
                return productoCbx.getContainerProperty(selected, IDNOMENCLATURA_PROPERTY).getValue().toString();
            }
            return null;
        }

        public String getIdProducto() {
            return productoCbx.getValue().toString();
        }

        public String getCentroCosto() {
            Object selected = centroCostoCbx.getValue();
            if (selected != null) {
                return centroCostoCbx.getItemCaption(selected);
            }
            return null;
        }

        public Producto getProducto() {
            Object selected = productoCbx.getValue();
            if (selected != null) {
                Producto p = (Producto) productoCbx.getContainerProperty(selected, PRODUCTO_PROPERTY).getValue();
                if (p != null) {
                    if(referenciaTxt.getValue() != null && !referenciaTxt.getValue().isEmpty()) {
                        String re = referenciaTxt.getValue().trim();
                        p.setComentario(re);
                    } else {
                        p.setComentario("");
                    }
                    p.setCantidad((int) cantidadTxt.getDoubleValueDoNotThrow());
                    p.setMonto(haberTxt.getDoubleValueDoNotThrow());
                    return p;
                }
            }
            return null;
        }

        public Integer getCentroCostoId() {
            Object selected = centroCostoCbx.getValue();
            if (selected != null) {
                return (Integer) selected;
            }
            return null;
        }

        public HorizontalLayout getLayoutHorizontal() {
            return layoutHorizontal;
        }

        public ProductoVenta getNext() {
            return next;
        }

        public void setNext(ProductoVenta next) {
            this.next = next;
        }

        public ProductoVenta getPrevious() {
            return previous;
        }

        public void setPrevious(ProductoVenta previous) {
            this.previous = previous;
        }

    }

    public class ProductoVentaEmpresaDTO {
        public final int idEmpresa;
        public final String idProducto;
        public final String nombreProducto;
        public final int idNomenclatura;
        public final String infileTipo;
        public final int correlativoProducto;
        public final boolean especial;
        public final Map<Integer, Integer> frases;

        public ProductoVentaEmpresaDTO(int idEmpresa, String idProducto, String nombreProducto,
                                       int idNomenclatura, String infileTipo,
                                       int correlativoProducto, boolean especial,
                                       Map<Integer, Integer> frases) {
            this.idEmpresa = idEmpresa;
            this.idProducto = idProducto;
            this.nombreProducto = nombreProducto;
            this.idNomenclatura = idNomenclatura;
            this.infileTipo = infileTipo;
            this.correlativoProducto = correlativoProducto;
            this.especial = especial;
            this.frases = frases;
        }
    }

}
