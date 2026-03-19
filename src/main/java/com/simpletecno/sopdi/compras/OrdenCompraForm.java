package com.simpletecno.sopdi.compras;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.operativo.EstimacionesWindow;
import com.simpletecno.sopdi.operativo.IntegracionActual;
import com.simpletecno.sopdi.operativo.IntegracionItemCostos;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.SelectionEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.themes.Runo;
import com.vaadin.ui.themes.ValoTheme;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.MultiFileUpload;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadFinishedHandler;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadStateWindow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.vaadin.ui.NumberField;

import java.io.*;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
//import org.vaadin.ui.NumberField;

/**
 *
 * @author user
 */
public class OrdenCompraForm extends Window {

    String idOrdenCompra;

    UI mainUI;
    Statement stQuery = null, stQuery2 = null, stQuery3 = null;
    ResultSet rsRecords = null, rsRecords2 = null, rsRecords3 = null;
    String queryString;
    PreparedStatement stPreparedQuery = null;
    Statement stQuery1   = null;
    ResultSet rsRecords1 = null;

    VerticalLayout contentLayout;
    HorizontalLayout mainForm;
    VerticalLayout leftLayout;
    VerticalLayout rightLayout;

    ComboBox tipoOrdenCompraCbx;
    ComboBox proveedorCbx;
    Label porcentajeAnticipoLbl;
    NumberField anticipoTxt;
    Label diasCretidoLbl;
    TextField responsableTxt;
    ComboBox contactoObraCbx;
    ComboBox monedaCbx;
    NumberField montoTxt;
    NumberField anticipoPendienteTxt;
    NumberField baseImponibleTxt;
    NumberField ivaTxt;
    NumberField retencionIsrTxt;
    NumberField porcentajeToleranciaTxt;
    TextField direccionTxt;
    TextField referenciaEtregaTxt;
    TextField nombreChequeTxt;
    ComboBox cuentaContableCbx;
    TextField cotizacionReferenciaTxt;
    TextField razonTxt;

    Button salirBtn;
    Button guardarBtn;

    MultiFileUpload singleUpload;

    public File planillaFile;
    public XSSFWorkbook workbook;
    public XSSFSheet sheet;
    private FileInputStream fileInputStream;

    public IndexedContainer idccContainer = new IndexedContainer();
    Grid idccGrid;
    Grid.FooterRow footerRow;

    static final String ID_PROPERTY = "Id";
    static final String PROJECT_PROPERTY = "Project";
    static final String IDCC_PROPERTY = "IDCC";
    static final String IDEX_PROPERTY = "IDEX";
    static final String DESCRIPCION_PROPERTY = "Descripción";
    static final String TOTAL_PROPERTY = "Total";
    static final String TOTALSF_PROPERTY = "TSF";
    static final String VER_PLU_PROPERTY = "Ver PLUs";

    //para otro grid, cuando sesa tipo de orden de compra 2 compra parcial
    //por el momento no se mostrará, solamente el PDF debe salir esta informacion
    static final String AREA_PROPERTY = "AREA";
    static final String CUENTA_PROPERTY = "CUENTA";
    static final String DESCRIPCION_CUENTA_PROPERTY = "Descripción cuenta";
    static final String CANTIDAD_PROPERTY = "CANTIDAD";
    static final String PRECIO_PROPERTY = "PRECIO";

    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");

    double porcentajeAnticipo = 0.00;
    double baseImponible = 0.00;
    double diferencia = 0.00;
    double retencionIsr = 0.00;

    Label titleLbl;

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public OrdenCompraForm(String idOrdenCompra) {
        this.mainUI = UI.getCurrent();
        this.idOrdenCompra = idOrdenCompra;

        setWidth("95%");
        setHeight("90%");
        setResponsive(true);
        setModal(true);

        contentLayout = new VerticalLayout();
        contentLayout.setWidth("100%");
        contentLayout.setMargin(true);
        contentLayout.setSpacing(true);

        setCaption("Orden de compra");
        setClosable(true);
        setResizable(true);
        center();
        setStyleName("rcorners1");
        setContent(contentLayout);
        setCloseShortcut(com.vaadin.event.ShortcutAction.KeyCode.ESCAPE, null);
        setDraggable(true);

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setResponsive(true);
        titleLayout.setWidth("100%");

        if(idOrdenCompra.trim().isEmpty()) {
            titleLbl = new Label(empresaId  + " " + empresaNombre + " NUEVA ORDEN DE COMPRA");
        }
        else {
            titleLbl = new Label(empresaId  + " " + empresaNombre + " EDITAR ORDEN DE COMPRA : " + idOrdenCompra);
        }
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName(Runo.LABEL_H2);
        titleLbl.setContentMode(ContentMode.HTML);
        titleLbl.setImmediate(true);

        titleLayout.addComponent(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.BOTTOM_CENTER);

        contentLayout.addComponent(titleLayout);
        contentLayout.setComponentAlignment(titleLayout, Alignment.BOTTOM_CENTER);

        mainForm = new HorizontalLayout();
        mainForm.setWidth("100%");
        mainForm.setMargin(true);
        mainForm.setSpacing(false);

        contentLayout.addComponent(mainForm);

        crearComponents();

        if (!idOrdenCompra.trim().isEmpty()) {
            llenarDatos(idOrdenCompra);
            fillGrid();
            if(  tipoOrdenCompraCbx.getValue().equals("1") || tipoOrdenCompraCbx.getValue().equals("2") ) {
                selectGrid();
            }
            else {
                selectGridCCOnly();
            }
            recalcularMontos();
        }
//        else {
//            tipoOrdenCompraCbx.select("1");
//            tipoOrdenCompraCbx.setScrollToSelectedItem(true);
//        }
    }

    private void crearComponents() {

        leftLayout = new VerticalLayout();
//        leftLayout.setWidth("100%");
        leftLayout.setMargin(true);
        leftLayout.setSpacing(false);
        leftLayout.addStyleName("rcorners3");

        rightLayout = new VerticalLayout();
//        rightLayout.setWidth("100%");
        rightLayout.setMargin(true);
        rightLayout.setSpacing(false);
        rightLayout.addStyleName("rcorners3");

        mainForm.addComponents(leftLayout,rightLayout);
        mainForm.setExpandRatio(leftLayout, 2);
        mainForm.setExpandRatio(rightLayout, 3);

        GridLayout gridLayout = new GridLayout(1,12);
        gridLayout.setMargin(new MarginInfo(false, false, false, false));
        gridLayout.setSpacing(false);
        gridLayout.addStyleName("rcorners4");
        gridLayout.setWidth("100%");

        leftLayout.addComponent(gridLayout);
        leftLayout.setComponentAlignment(gridLayout, Alignment.TOP_CENTER);
        leftLayout.setExpandRatio(gridLayout, 2);

        tipoOrdenCompraCbx = new ComboBox();
        tipoOrdenCompraCbx.setWidth("100%");
        tipoOrdenCompraCbx.setInputPrompt("Tipo orden de compra");
        tipoOrdenCompraCbx.setFilteringMode(FilteringMode.CONTAINS);
        tipoOrdenCompraCbx.addValueChangeListener((Property.ValueChangeListener) event -> {
            if (tipoOrdenCompraCbx.getValue() != null) {
                if(idccGrid != null) {
                    idccGrid.getColumn(CUENTA_PROPERTY).setHidden(true);
                    idccGrid.getColumn(CANTIDAD_PROPERTY).setHidden(true);
                    idccGrid.getColumn(PRECIO_PROPERTY).setHidden(true);
                    idccGrid.setEditorEnabled(false);
                    idccGrid.getColumn(VER_PLU_PROPERTY).setHidden(true);
                    idccGrid.getColumn(AREA_PROPERTY).setHidden(true);
                    idccGrid.getColumn(DESCRIPCION_CUENTA_PROPERTY).setHidden(true);

                    switch (String.valueOf(tipoOrdenCompraCbx.getValue())) {
                        case "1": //estimacion
                            idccGrid.setSelectionMode(Grid.SelectionMode.MULTI);
                            break;
                        case "2": //compra parcial
                            idccGrid.setSelectionMode(Grid.SelectionMode.MULTI);
                            idccGrid.getColumn(CUENTA_PROPERTY).setHidden(false);
                            idccGrid.getColumn(CANTIDAD_PROPERTY).setHidden(false);
                            idccGrid.getColumn(PRECIO_PROPERTY).setHidden(false);
                            idccGrid.getColumn(DESCRIPCION_CUENTA_PROPERTY).setHidden(false);
                            idccGrid.getColumn(AREA_PROPERTY).setHidden(false);
                            idccGrid.getColumn(VER_PLU_PROPERTY).setHidden(false);
                            idccGrid.setEditorEnabled(true);
                            idccGrid.getColumn(CANTIDAD_PROPERTY).setEditable(true);
                            break;
                        case "3": //compras constructura
                            Notification.show("No disponible en esta versión.", Notification.Type.WARNING_MESSAGE);
                            return;
                        case "4": //eventual
                        case "5": //recurrente
                            idccGrid.setSelectionMode(Grid.SelectionMode.MULTI);
                            break;
                    }
                    //                fillGrid();
                }
            }
        });
        llenarComboTipoOrdenCompra();

        proveedorCbx = new ComboBox();
        proveedorCbx.setInputPrompt("Proveedor");
        proveedorCbx.setWidth("100%");
        proveedorCbx.setFilteringMode(FilteringMode.CONTAINS);
        proveedorCbx.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (proveedorCbx.getValue() != null) {
                    if (idOrdenCompra.trim().isEmpty()) {
                        porcentajeAnticipo = Double.parseDouble((String)proveedorCbx.getContainerProperty(proveedorCbx.getValue(),"PorcentajeAnticipo").getValue());
                        porcentajeAnticipo = porcentajeAnticipo*100;
                        porcentajeAnticipoLbl.setValue(porcentajeAnticipo + "% Anticipo");
                        diasCretidoLbl.setValue(proveedorCbx.getContainerProperty(proveedorCbx.getValue(),"DiasCredito").getValue() + " Días crédito");
                        fillGrid();
                    }
                    buscarAnticiposProveedor();
                    nombreChequeTxt.setValue(proveedorCbx.getItemCaption(proveedorCbx.getValue()));
                    singleUpload.setEnabled(true);
                }
                recalcularMontos();
            }
        });
        proveedorCbx.addContainerProperty("PorcentajeAnticipo", String.class, "0.00");
        proveedorCbx.addContainerProperty("DiasCredito", String.class, "0");
        proveedorCbx.addContainerProperty("Regimen", String.class, "NORMAL");
        llenarComboProveedor();

        HorizontalLayout anticipoProveedorLayout = new HorizontalLayout();
        anticipoProveedorLayout.setWidth("100%");
        anticipoProveedorLayout.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
        anticipoProveedorLayout.setSpacing(true);

        porcentajeAnticipoLbl = new Label("% Anticipo=0.00");

        anticipoPendienteTxt = new NumberField();
        anticipoPendienteTxt.setInputPrompt("Anticipo pendiente");
        anticipoPendienteTxt.setDescription("Anticipo pendiente");
        anticipoPendienteTxt.setGroupingUsed(true);
        anticipoPendienteTxt.setGroupingSeparator(',');
        anticipoPendienteTxt.setGroupingSize(3);
        anticipoPendienteTxt.setImmediate(true);
        anticipoPendienteTxt.setWidth("100%");
        anticipoPendienteTxt.setValue(0.00);
        anticipoPendienteTxt.setReadOnly(true);
        anticipoPendienteTxt.setDecimalSeparator('.');
        anticipoPendienteTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);

        diasCretidoLbl = new Label("Dias crédito : 0");
        diasCretidoLbl.setImmediate(true);
        diasCretidoLbl.setWidth("100%");

        anticipoProveedorLayout.addComponents(porcentajeAnticipoLbl, anticipoPendienteTxt, diasCretidoLbl);

        responsableTxt = new TextField();
        responsableTxt.setInputPrompt("Responsable");
        responsableTxt.setDescription("Responsable");
        responsableTxt.setWidth("100%");
        responsableTxt.setValue(((SopdiUI)UI.getCurrent()).sessionInformation.getStrUserFullName());
        responsableTxt.setResponsive(true);

        contactoObraCbx = new ComboBox();
        contactoObraCbx.setInputPrompt("Contacto en obra");
        contactoObraCbx.setDescription("Contacto en obra");
        contactoObraCbx.setWidth("100%");
        contactoObraCbx.setFilteringMode(FilteringMode.CONTAINS);
        contactoObraCbx.setNewItemsAllowed(false);
        contactoObraCbx.setNullSelectionAllowed(false);
        contactoObraCbx.setTextInputAllowed(false);
        llenarComboContactoObra();

        direccionTxt = new TextField();
        direccionTxt.setInputPrompt("Dirección de entrega");
        direccionTxt.setDescription("Dirección de entrega");
        direccionTxt.setWidth("100%");
        direccionTxt.setValue("");
 //       direccionTxt.setValue("Blv. Acatán 30-98 zona 16, Residencial Siena San Isidro"); //((SopdiUI)UI.getCurrent()).sessionInformation.getStrAccountingCompanyDirection();

        referenciaEtregaTxt = new TextField();
        referenciaEtregaTxt.setInputPrompt("Referencia de entrega");
        referenciaEtregaTxt.setDescription("Referencia de entrega");
        referenciaEtregaTxt.setWidth("100%");
        referenciaEtregaTxt.setValue("");

        cotizacionReferenciaTxt = new TextField();
        cotizacionReferenciaTxt.setInputPrompt("Cotización referencia");
        cotizacionReferenciaTxt.setDescription("Cotización referencia");
        cotizacionReferenciaTxt.setWidth("100%");
        cotizacionReferenciaTxt.setValue("");

        monedaCbx = new ComboBox();
        monedaCbx.setInputPrompt("Moneda");
        monedaCbx.addItem("QUETZALES");
        monedaCbx.addItem("DOLARES");
        monedaCbx.select("QUETZALES");
        monedaCbx.setWidth("100%");

        montoTxt = new NumberField();
        montoTxt.setInputPrompt("Monto");
        montoTxt.setDescription("Monto");
        montoTxt.setGroupingUsed(true);
        montoTxt.setGroupingSeparator(',');
        montoTxt.setDecimalSeparator('.');
        montoTxt.setGroupingSize(3);
        montoTxt.setImmediate(true);
        montoTxt.setWidth("100%");
        montoTxt.setValue(0.00);
        montoTxt.setReadOnly(true);
        montoTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        montoTxt.addValueChangeListener( event -> {
            recalcularMontos();
        });

        baseImponibleTxt = new NumberField();
        baseImponibleTxt.setInputPrompt("Base imponible");
        baseImponibleTxt.setDescription("Base imponible");
        baseImponibleTxt.setGroupingUsed(true);
        baseImponibleTxt.setGroupingSeparator(',');
        baseImponibleTxt.setGroupingSize(3);
        baseImponibleTxt.setImmediate(true);
        baseImponibleTxt.setWidth("100%");
        baseImponibleTxt.setValue(0.00);
        baseImponibleTxt.setReadOnly(true);
        baseImponibleTxt.setDecimalSeparator('.');
        baseImponibleTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);

        ivaTxt = new NumberField();
        ivaTxt.setInputPrompt("IVA");
        ivaTxt.setDescription("IVA");
        ivaTxt.setGroupingUsed(true);
        ivaTxt.setGroupingSeparator(',');
        ivaTxt.setGroupingSize(3);
        ivaTxt.setImmediate(true);
        ivaTxt.setWidth("100%");
        ivaTxt.setValue(0.00);
        ivaTxt.setReadOnly(true);
        ivaTxt.setDecimalSeparator('.');
        ivaTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);

        retencionIsrTxt = new NumberField();
        retencionIsrTxt.setInputPrompt("ISR");
        retencionIsrTxt.setDescription("ISR");
        retencionIsrTxt.setGroupingUsed(true);
        retencionIsrTxt.setGroupingSeparator(',');
        retencionIsrTxt.setGroupingSize(3);
        retencionIsrTxt.setImmediate(true);
        retencionIsrTxt.setWidth("100%");
        retencionIsrTxt.setValue(0.00);
        retencionIsrTxt.setReadOnly(true);
        retencionIsrTxt.setDecimalSeparator('.');
        retencionIsrTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);

        HorizontalLayout labelsLayout = new HorizontalLayout();
        labelsLayout.setWidth("100%");
        labelsLayout.setSpacing(true);
        labelsLayout.setMargin(false);
        labelsLayout.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);

        labelsLayout.addComponents(new Label("Monto"), new Label("Base imponible"), new Label("ISR"));

        HorizontalLayout montosLayout = new HorizontalLayout();
        montosLayout.setWidth("100%");
        montosLayout.setSpacing(true);
        montosLayout.setMargin(false);
        montosLayout.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);

//        montosLayout.addComponents(montoTxt, baseImponibleTxt, ivaTxt, retencionIsrTxt);
        montosLayout.addComponents(montoTxt, baseImponibleTxt, retencionIsrTxt);
        montosLayout.setExpandRatio(montoTxt, 1);
        montosLayout.setExpandRatio(baseImponibleTxt, 1);
//        montosLayout.setExpandRatio(ivaTxt, 1);
        montosLayout.setExpandRatio(retencionIsrTxt, 1);

        HorizontalLayout labels2Layout = new HorizontalLayout();
        labels2Layout.setWidth("100%");
        labels2Layout.setSpacing(true);
        labels2Layout.setMargin(false);
        labels2Layout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);

        labels2Layout.addComponents(new Label("Anticipo"),  new Label("% Tolerancia desviación"));

        HorizontalLayout montos2Layout = new HorizontalLayout();
        montos2Layout.setWidth("100%");
        montos2Layout.setSpacing(true);
        montos2Layout.setMargin(false);
        montos2Layout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);

        anticipoTxt = new NumberField();
        anticipoTxt.setInputPrompt("Anticipo");
        anticipoTxt.setDescription("Anticipo");
        anticipoTxt.setGroupingUsed(true);
        anticipoTxt.setGroupingSeparator(',');
        anticipoTxt.setGroupingSize(3);
        anticipoTxt.setImmediate(true);
        anticipoTxt.setWidth("100%");
        anticipoTxt.setValue(0.00);
        //anticipoTxt.setReadOnly(true);
        anticipoTxt.setDecimalSeparator('.');
        anticipoTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);

        Label porcentajeToleranciaLbl = new Label("% Tolerancia");
            porcentajeToleranciaLbl.setWidth("100%");
            porcentajeToleranciaLbl.setImmediate(true);
            porcentajeToleranciaLbl.setDescription("Porcentaje tolerancia desviación");
        porcentajeToleranciaTxt = new NumberField();
        porcentajeToleranciaTxt.setInputPrompt("Porcentaje tolerancia desviación");
        porcentajeToleranciaTxt.setDescription("Porcentaje tolerancia desviación");
        porcentajeToleranciaTxt.setDecimalSeparator('.');
        porcentajeToleranciaTxt.setImmediate(true);
        porcentajeToleranciaTxt.setValue(0.00);
        porcentajeToleranciaTxt.setWidth("100%");
        porcentajeToleranciaTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);

//        montos2Layout.addComponents(anticipoTxt, porcentajeToleranciaLbl, porcentajeToleranciaTxt);
        montos2Layout.addComponents(anticipoTxt, porcentajeToleranciaTxt);
        montos2Layout.setExpandRatio(anticipoTxt, 1);
        montos2Layout.setExpandRatio(porcentajeToleranciaTxt, 1);

        nombreChequeTxt = new TextField();
        nombreChequeTxt.setInputPrompt("Nombre en cheque");
        nombreChequeTxt.setWidth("100%");
        nombreChequeTxt.setValue("");

        cuentaContableCbx = new ComboBox();
        cuentaContableCbx.setInputPrompt("Cuenta Contable");
        cuentaContableCbx.setWidth("100%");
        cuentaContableCbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContableCbx.setInvalidAllowed(false);
        cuentaContableCbx.setNewItemsAllowed(false);

        llenarComboCuentaContable();

        razonTxt = new TextField();
        razonTxt.setInputPrompt("Razón");
        razonTxt.setWidth("100%");
        razonTxt.setValue("");

        gridLayout. addComponent(tipoOrdenCompraCbx);
        gridLayout.addComponent(proveedorCbx);
        gridLayout.addComponents(anticipoProveedorLayout);
        gridLayout.addComponent(responsableTxt);
        gridLayout.addComponent(contactoObraCbx);
        gridLayout.addComponents(monedaCbx);
        gridLayout.addComponents(labelsLayout);
        gridLayout.addComponents(montosLayout);
        gridLayout.addComponents(labels2Layout);
        gridLayout.addComponents(montos2Layout);
        gridLayout.addComponents(nombreChequeTxt);
        gridLayout.addComponents(direccionTxt);
        gridLayout.addComponents(referenciaEtregaTxt);
        gridLayout.addComponents(cotizacionReferenciaTxt);
        gridLayout.addComponents(cuentaContableCbx);
        gridLayout.addComponents(razonTxt);

        idccContainer.addContainerProperty(ID_PROPERTY, String.class, null);
        idccContainer.addContainerProperty(IDCC_PROPERTY, String.class, "");
        idccContainer.addContainerProperty(IDEX_PROPERTY, String.class, "");
        idccContainer.addContainerProperty(DESCRIPCION_PROPERTY, String.class, "");
        idccContainer.addContainerProperty(CUENTA_PROPERTY, String.class, "0");
        idccContainer.addContainerProperty(DESCRIPCION_CUENTA_PROPERTY, String.class, "0");
        idccContainer.addContainerProperty(PROJECT_PROPERTY, String.class, "0");
        idccContainer.addContainerProperty(AREA_PROPERTY, String.class, "0");
        idccContainer.addContainerProperty(CANTIDAD_PROPERTY, String.class, "0");
        idccContainer.addContainerProperty(PRECIO_PROPERTY, String.class, "0");
        idccContainer.addContainerProperty(TOTAL_PROPERTY, String.class, "0");
        idccContainer.addContainerProperty(TOTALSF_PROPERTY, String.class, "0");
        idccContainer.addContainerProperty(VER_PLU_PROPERTY, String.class, "Ver PLUs");

        idccGrid = new Grid("LISTADO CENTROS DE COSTO Y SUS IDEX DISPONIBLES", idccContainer);
        idccGrid.setWidth("100%");
        idccGrid.setDescription("Seleccione uno o varios registros.");
        idccGrid.setHeightMode(HeightMode.ROW);
        idccGrid.setHeightByRows(12);
        idccGrid.setImmediate(true);
        idccGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        idccGrid.getColumn(ID_PROPERTY).setHidden(true).setHidable(true);
        idccGrid.getColumn(TOTALSF_PROPERTY).setHidden(true).setHidable(true);
        idccGrid.getColumn(IDCC_PROPERTY).setExpandRatio(1);
        idccGrid.getColumn(IDEX_PROPERTY).setExpandRatio(1);
        idccGrid.getColumn(TOTAL_PROPERTY).setExpandRatio(2);
        idccGrid.getColumn(DESCRIPCION_PROPERTY).setExpandRatio(3);
        idccGrid.getColumn(CUENTA_PROPERTY).setExpandRatio(1).setHidden(true).setHidable(true);
        idccGrid.getColumn(DESCRIPCION_CUENTA_PROPERTY).setExpandRatio(3).setHidden(true).setHidable(true);
        idccGrid.getColumn(AREA_PROPERTY).setExpandRatio(1).setHidden(true).setHidable(true);
        idccGrid.getColumn(CANTIDAD_PROPERTY).setExpandRatio(1).setHidden(true).setHidable(true);
        idccGrid.getColumn(PRECIO_PROPERTY).setExpandRatio(1).setHidden(true).setHidable(true);
        idccGrid.getColumn(VER_PLU_PROPERTY).setExpandRatio(1).setHidden(true).setHidable(true);

        idccGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if (TOTAL_PROPERTY.equals(cellReference.getPropertyId())) {
                    return "rightalign";
            } else {
                return null;
            }
        });
        idccGrid.getColumn(VER_PLU_PROPERTY).setRenderer(new ButtonRenderer(e
                -> {
            if(!idOrdenCompra.isEmpty()) {
                Item item = idccGrid.getContainerDataSource().getItem(e.getItemId());
                OrdenCompraPluProveedorForm ordenCompraPluProveedorForm = new OrdenCompraPluProveedorForm(
                        idOrdenCompra,
                        item.getItemProperty(CUENTA_PROPERTY).getValue().toString(),
                        item.getItemProperty(AREA_PROPERTY).getValue().toString(),
                        proveedorCbx.getValue().toString()
                );
                UI.getCurrent().addWindow(ordenCompraPluProveedorForm);
                ordenCompraPluProveedorForm.center();
            }
            else {
                Notification notif = new Notification("Atención",
                        "Debe guardar la orden de compra primero para poder agregar PLUs. Luego al Editar la orden de compra podrá agregar los PLUs que desee.",
                        Notification.Type.WARNING_MESSAGE);
                notif.setDelayMsec(4000);
                notif.setPosition(Position.TOP_CENTER);
                notif.show(Page.getCurrent());
            }
        }));

        idccGrid.addSelectionListener((SelectionEvent.SelectionListener) event -> {
            // iterar por todos los items, verificar si están seleccionados y sumar totales
            anticipoTxt.setReadOnly(!anticipoTxt.isReadOnly());
            montoTxt.setReadOnly(!montoTxt.isReadOnly());
            montoTxt.setValue(0.00);
            anticipoTxt.setValue(0.00);
            for (Object gridItem : idccGrid.getContainerDataSource().getItemIds()) {
                if (idccGrid.isSelected(gridItem)) {

                    montoTxt.setValue(
                            Double.parseDouble(
                                    new DecimalFormat(
                                            "#######.##").format(
                                            (montoTxt.getDoubleValueDoNotThrow() + Double.parseDouble(String.valueOf(idccContainer.getContainerProperty(gridItem, TOTALSF_PROPERTY).getValue()))))));
                }
            }//end for
            if (tipoOrdenCompraCbx.getValue().equals("1") || tipoOrdenCompraCbx.getValue().equals("2")) {
                montoTxt.setReadOnly(!montoTxt.isReadOnly());
                anticipoTxt.setValue(Double.parseDouble(new DecimalFormat("#######.##").format((montoTxt.getDoubleValueDoNotThrow() * (porcentajeAnticipo / 100)))));
                anticipoTxt.setReadOnly(!anticipoTxt.isReadOnly());
                footerRow.getCell(TOTAL_PROPERTY).setText(numberFormat.format(montoTxt.getDoubleValueDoNotThrow()));
            }
            else {
                montoTxt.setReadOnly(false);
                anticipoTxt.setReadOnly(false);
            }
        });
        //filtros
        Grid.HeaderRow filterRow = idccGrid.appendHeaderRow();
        Grid.HeaderCell cell = filterRow.getCell(IDCC_PROPERTY);
        TextField filterField = new TextField();
        filterField.addStyleName(ValoTheme.TEXTFIELD_TINY);

        filterField.setInputPrompt("Filtrar por IDCC");
        filterField.setColumns(5);

        filterField.addTextChangeListener(change -> {
                    idccContainer.removeContainerFilters(IDCC_PROPERTY);

                    // (Re)create the filter if necessary
                    if (!change.getText().isEmpty()) {
                        idccContainer.addContainerFilter(
                                new SimpleStringFilter(IDCC_PROPERTY,
                                        change.getText(), true, false));
                    }
//                    setTotal();
                }
        );
        cell.setComponent(filterField);

        Grid.HeaderCell cell2 = filterRow.getCell(IDEX_PROPERTY);
        TextField filterField2 = new TextField();
        filterField2.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField2.setInputPrompt("Filtrar por IDEX");
        filterField2.setColumns(5);

        filterField2.addTextChangeListener(change -> {
                    idccContainer.removeContainerFilters(IDEX_PROPERTY);
                    if (!change.getText().isEmpty()) {
                        idccContainer.addContainerFilter(
                                new SimpleStringFilter(IDEX_PROPERTY,
                                        change.getText(), true, false));
                    }
//                    setTotal();
                }
        );
        cell2.setComponent(filterField2);

        footerRow = idccGrid.appendFooterRow();
        footerRow.getCell(DESCRIPCION_PROPERTY).setText("Total");
        footerRow.getCell(TOTAL_PROPERTY).setText("0.00");

        salirBtn = new Button("Salir");
        salirBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        salirBtn.setIcon(FontAwesome.ARROW_RIGHT);
        salirBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();
            }
        });

        guardarBtn = new Button("Guardar");
        guardarBtn.setIcon(FontAwesome.SAVE);
        guardarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                guardarOrdenCompra();
            }
        });

        UploadFinishedHandler handler;
        handler = (InputStream stream, String fileName, String mimeType, long length) -> {
            File targetFile;

            try {

                System.out.println("\nfileName=" + fileName);
                System.out.println("length=" + stream.available());
                System.out.println("mimeType=" + mimeType);

                byte[] buffer = new byte[stream.available()];
                stream.read(buffer);
                String filePath = VaadinService.getCurrent()
                        .getBaseDirectory().getAbsolutePath() + "/projectfiles/" + ((SopdiUI) mainUI).sessionInformation.getStrCompanyId() + "/";

                new File(filePath).mkdirs();

                fileName = filePath + fileName;
                targetFile = new File(fileName);
                OutputStream outStream = new FileOutputStream(targetFile);
                outStream.write(buffer);
                outStream.close();
                stream.close();

                System.out.println("\ntargetFile = " + fileName);

                cargarArchivo(targetFile);

                planillaFile = targetFile;

                //   cargarBtn.setEnabled(true);
            } catch (java.io.IOException fIoEx) {
                fIoEx.printStackTrace();
                Notification.show("Error al cargar el archivo Excel IDEXs!", Notification.Type.ERROR_MESSAGE);
                return;
            }
        };

        UploadStateWindow window = new UploadStateWindow();

        singleUpload = new MultiFileUpload(handler, window, false);
        singleUpload.setIcon(FontAwesome.UPLOAD);
        singleUpload.setImmediate(true);
        singleUpload.getSmartUpload().setUploadButtonCaptions("Cargar archivo (Excel xlsx)", "");
        singleUpload.setEnabled(false);

        JavaScript.getCurrent().execute("document.getElementsByClassName('gwt-FileUpload')[0].setAttribute('accept', '.xlx')");
        JavaScript.getCurrent().execute("document.getElementsByClassName('gwt-FileUpload')[0].setAttribute('accept', '.xlsx')");

        List<String> acceptedMimeTypes = new ArrayList();
//        acceptedMimeTypes.add("application/octet-stream");
        acceptedMimeTypes.add("application/ovnd.ms-excel");
        acceptedMimeTypes.add("application/msexcel");
        acceptedMimeTypes.add("application/x-msexcel");
        acceptedMimeTypes.add("application/x-ms-excel");
        acceptedMimeTypes.add("application/x-excel");
        acceptedMimeTypes.add("application/x-dos_ms_excel");
        acceptedMimeTypes.add("application/xls");
        acceptedMimeTypes.add("application/x-xls");
        //       singleUpload.setAcceptedMimeTypes(acceptedMimeTypes);


        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setWidth("100%");
        buttonsLayout.setSpacing(true);
        buttonsLayout.setMargin(false);

        buttonsLayout.addComponents(salirBtn, singleUpload, guardarBtn);
        buttonsLayout.setComponentAlignment(salirBtn, Alignment.BOTTOM_LEFT);
        buttonsLayout.setComponentAlignment(singleUpload, Alignment.BOTTOM_CENTER);
        buttonsLayout.setComponentAlignment(guardarBtn, Alignment.BOTTOM_CENTER);

        rightLayout.addComponent(idccGrid);
        rightLayout.setComponentAlignment(idccGrid, Alignment.TOP_CENTER);
        rightLayout.setExpandRatio(idccGrid, 1);

        contentLayout.addComponent(buttonsLayout);
        contentLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);
    }

    private void recalcularMontos() {
        anticipoTxt.setReadOnly(false);
        anticipoTxt.setValue(Double.parseDouble(new DecimalFormat("#######.##").format((montoTxt.getDoubleValueDoNotThrow() * (porcentajeAnticipo / 100)))));
        retencionIsrTxt.setReadOnly(false);
        retencionIsrTxt.setValue(0.0);
        retencionIsrTxt.setReadOnly(true);

        diferencia = 0;
        retencionIsr = 0;

        //calcular base imponible, iva y retencion isr si aplica
        baseImponibleTxt.setReadOnly(false);
        baseImponibleTxt.setValue(Double.parseDouble(new DecimalFormat("#######.##").format((montoTxt.getDoubleValueDoNotThrow() / 1.12))));
        baseImponibleTxt.setReadOnly(true);

        baseImponible = baseImponibleTxt.getDoubleValueDoNotThrow();

        ivaTxt.setReadOnly(false);
        ivaTxt.setValue(Double.parseDouble(new DecimalFormat("#######.##").format((baseImponibleTxt.getDoubleValueDoNotThrow() * 0.12))));
        ivaTxt.setReadOnly(true);

        if( proveedorCbx.getValue() == null ) {
            return;
        }

        if (proveedorCbx.getItem(proveedorCbx.getValue()).getItemProperty("Regimen").getValue().toString().equals("SUJETO A RETENCION ISR")) {
            if (baseImponibleTxt.getDoubleValueDoNotThrow() <= 30000) {
                retencionIsr = baseImponible * 0.05;
            } else {
                diferencia = baseImponible - 30000;
                retencionIsr = (30000 * 0.05) + (diferencia * 0.07);
            }
            retencionIsrTxt.setReadOnly(false);
            retencionIsrTxt.setValue(Double.parseDouble(new DecimalFormat("#######.##").format(retencionIsr)));
            retencionIsrTxt.setReadOnly(true);

            anticipoTxt.setValue(Double.parseDouble(new DecimalFormat("#######.##").format(((montoTxt.getDoubleValueDoNotThrow() - retencionIsr) * (porcentajeAnticipo / 100)))));
        }
    }

    private void setTotal() {

        BigDecimal total = new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal totalQ = new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP);

        for (Object rid : idccGrid.getContainerDataSource()
                .getItemIds()) {
            if (rid == null) {
                return;
            }
            if (idccContainer.getContainerProperty(rid, TOTALSF_PROPERTY).getValue() == null) {
                return;
            }
            if(!idccGrid.isSelected(rid)) {
                return;
            }
            total = total.add(new BigDecimal(
                    Double.parseDouble(
                            String.valueOf(idccContainer.getContainerProperty(rid, TOTALSF_PROPERTY).getValue())
                    )));
        }
        footerRow.getCell(TOTAL_PROPERTY).setText(numberFormat.format(total));
    }

    private void llenarComboContactoObra() {
        queryString = " SELECT * ";
        queryString += " FROM proveedor_empresa";
        queryString += " WHERE Inhabilitado = 0 AND EsContactoObra = 1 ";
        queryString += " AND IdEmpresa = " + empresaId;

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            contactoObraCbx.addItem("0");
            contactoObraCbx.setItemCaption("0", "NO APLICA");

            if (rsRecords.next()) { //  encontrado
                do {
                    contactoObraCbx.addItem(rsRecords.getString("IdProveedor"));
                    contactoObraCbx.setItemCaption(rsRecords.getString("IdProveedor"), rsRecords.getString("Nombre") + " TEL: " + rsRecords.getString("Telefono") );
                } while (rsRecords.next());
            }
            contactoObraCbx.setValue("0");

        } catch (Exception ex1) {
            System.out.println("Error al listar proveedores: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    private void llenarComboTipoOrdenCompra() {
        queryString = " SELECT * ";
        queryString += " FROM tipo_orden_compra";
        queryString += " WHERE Id IN (Select IdTipoOrdenDeCompra FROM usuario_permisos_tipo_orden_compra WHERE IdUSuario = " + ((SopdiUI)UI.getCurrent()).sessionInformation.getStrUserId() + ")";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado
                do {
                    tipoOrdenCompraCbx.addItem(rsRecords.getString("Id"));
                    tipoOrdenCompraCbx.setItemCaption(rsRecords.getString("Id"), rsRecords.getString("Descripcion"));
                    if(rsRecords.getString("Id").equals("4")) {
                        tipoOrdenCompraCbx.setValue(rsRecords.getString("Id"));
                        tipoOrdenCompraCbx.select(rsRecords.getString("Id"));
                    }
                } while (rsRecords.next());
            }
            else {
               Notification.show("No tiene permisos para editar este tipo de ordenes de compra. Por favor revise los permisos de usaurio y tipos de ordendes de compra.", Notification.Type.WARNING_MESSAGE);
               close();
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar tipos de ordenes de compra: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    private void llenarComboProveedor() {
        queryString = " SELECT IdProveedor, Nombre, ";
        queryString += " DiasCredito, Regimen ";
        queryString += " FROM proveedor_empresa";
        queryString += " WHERE EsProveedor = 1";
        queryString += " AND Inhabilitado = 0";
        queryString += " AND IdEmpresa = " + empresaId;
        queryString += " ORDER BY proveedor.Nombre";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado
                do {
                    proveedorCbx.addItem(rsRecords.getString("IdProveedor"));
                    proveedorCbx.setItemCaption(rsRecords.getString("IdProveedor"), rsRecords.getString("Nombre"));
                    proveedorCbx.getContainerProperty(rsRecords.getString("IdProveedor"), "DiasCredito").setValue(rsRecords.getString("DiasCredito"));
                    proveedorCbx.getContainerProperty(rsRecords.getString("IdProveedor"), "PorcentajeAnticipo").setValue("0");
                    proveedorCbx.getContainerProperty(rsRecords.getString("IdProveedor"), "Regimen").setValue(rsRecords.getString("Regimen"));
//                    System.out.println("Proveedor=" + rsRecords.getString("Nombre") + " DiasCredito=" + rsRecords.getString("DiasCredito") + " Anticipo=" + rsRecords.getString("AnticipoUnidad"));
                } while (rsRecords.next());
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar proveedores: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    private void llenarComboCuentaContable() {

        queryString = " SELECT * FROM contabilidad_nomenclatura";
        queryString += " WHERE Estatus = 'HABILITADA'";
//        queryString += " AND Tipo IN ('SERVICIO', 'PRODUCTO', 'VENTA')";
//        queryString += " AND ID1=6"; //egresos
        queryString += " ORDER BY N5";

        try {
            cuentaContableCbx.addItem("0");
            cuentaContableCbx.setItemCaption("0", "SELECCIONE");

            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) {  // encontrado

                cuentaContableCbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContableCbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));
            }

//            cuentaContableCbx.setValue(((SopdiUI)UI.getCurrent()).cuentasContablesDefault.getCompras());
            cuentaContableCbx.setValue("0");

        } catch (Exception ex1) {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Error al combo cuentas contables: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    private void buscarAnticiposProveedor() {

        queryString = " SELECT SUM(DEBE - HABER) TOTALSALDO, SUM(DebeQuetzales - HaberQuetzales) TOTALSALDOQ";
        queryString += " FROM contabilidad_partida";
        queryString += " INNER JOIN contabilidad_nomenclatura ON contabilidad_nomenclatura.IdNomenclatura = contabilidad_partida.IdNomenclatura";
        queryString += " WHERE contabilidad_partida.IdProveedor = " + proveedorCbx.getValue();
        queryString += " AND contabilidad_partida.IdEmpresa = " + empresaId;
        queryString += " AND contabilidad_partida.MonedaDocumento = '" + monedaCbx.getValue() + "'";
        queryString += " AND contabilidad_partida.IdNomenclatura = " + ((SopdiUI) mainUI).cuentasContablesDefault.getAnticiposProveedor();
        queryString += " AND contabilidad_partida.Estatus <> 'ANULADO'";
        queryString += " HAVING TOTALSALDO > 0";

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "-->query para mostrar anticipos pendiente de liquidar del proveedor : " + queryString);

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                anticipoPendienteTxt.setReadOnly(false);
                anticipoPendienteTxt.setValue(rsRecords.getDouble("TOTALSALDO"));
                anticipoPendienteTxt.setReadOnly(true);
            }
        } catch (Exception ex) {
            System.out.println("Error al buscar anticipos pendientes del proveedor : " + ex);
            ex.printStackTrace();
        }
    }

    private void fillGrid() {

        if(tipoOrdenCompraCbx.getValue() == null) {
            return;
        }

        idccContainer.removeAllItems();

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "tipoOrdenCompraCbx.getValue()=" + tipoOrdenCompraCbx.getValue());

        if((tipoOrdenCompraCbx.getValue().equals("1")) || (tipoOrdenCompraCbx.getValue().equals("2"))) {// ESTIMACION O COMPRA PARCIAL
                buscarDIC();
        } else buscarCC();

    }

    private void buscarDIC() {
        Notification notif = new Notification("BUSCANDO DATOS... POR FAVOR ESPERAR!.", Notification.Type.WARNING_MESSAGE);
        notif.setDelayMsec(5000);
        notif.setPosition(Position.MIDDLE_CENTER);
        notif.setIcon(FontAwesome.CLOCK_O);
        notif.show(Page.getCurrent());

        if(  tipoOrdenCompraCbx.getValue().equals("1") // estimacion
                || tipoOrdenCompraCbx.getValue().equals("3") // constructura
                || tipoOrdenCompraCbx.getValue().equals("4") // eventual
                || tipoOrdenCompraCbx.getValue().equals("5") // recurrente
        ) {
/* comentado el 18/11/2025 JA para detalle por IDEX, cuenta, area, cantidad, precio no agrupado
            queryString = "Select DITEMC.IdCC, DITEMC.Idex, PROJT.Descripcion IdexDescripcion, SUM(DITEMC.Total) TotalTotal ";
            queryString += " From  DetalleItemsCostos DITEMC";
            queryString += " Inner Join project PROJ On PROJ.Numero = DITEMC.IdProject And PROJ.Estatus = 'ACTIVO'";
            queryString += " Inner Join project_tarea PROJT On PROJT.IdProject = PROJ.Id And PROJT.Idex = DITEMC.Idex";
            queryString += " Left Join proveedor Prov On Prov.IdProveedor = DITEMC.IdProveedor";
            queryString += " Where DITEMC.IdEmpresa = " + empresa;
            queryString += " And DITEMC.IDCC IN " + buscarCentrosCostoDIC();
            queryString += " And DITEMC.IdProveedor = " + proveedorCbx.getValue();
            queryString += " And DITEMC.Tipo In ('INTINI','DOCA')";  //60111  lote 104
            queryString += " And DITEMC.Moneda = '" + monedaCbx.getValue() + "'";
//            queryString += " And DITEMC.Lote = 104";
            queryString += " Group By Idcc, Idex, IdexDescripcion";

 */
            queryString = "SELECT DITEMC.IdCC, DITEMC.Idex, PROJT.Descripcion IdexDescripcion, DITEMC.NoCuenta, DITEMC.IdArea,";
            queryString += " DITEMC.Descripcion DescripcionCuenta, DITEMC.Cantidad, DITEMC.Precio, DITEMC.Total TotalTotal,";
            queryString += " PROJ.Numero";
            queryString += " FROM  DetalleItemsCostos DITEMC";
            queryString += " INNER JOIN project PROJ On PROJ.Numero = DITEMC.IdProject And PROJ.Estatus = 'ACTIVO'";
            queryString += " INNER JOIN project_tarea PROJT On PROJT.IdProject = PROJ.Id And PROJT.Idex = DITEMC.Idex";
            queryString += " LEFT JOIN proveedor_empresa Prov On Prov.IdProveedor = DITEMC.IdProveedor";
            queryString += " WHERE DITEMC.IdEmpresa = " + empresaId;
            queryString += " AND DITEMC.IDCC IN " + buscarCentrosCostoDIC();
            queryString += " AND DITEMC.IdProveedor = " + proveedorCbx.getValue();
            queryString += " AND DITEMC.Tipo In ('INTINI','DOCA')";  //60111  lote 104
            queryString += " And DITEMC.Moneda = '" + monedaCbx.getValue() + "'";
            queryString += " AND Prov.IdEmpresa = " + empresaId;
        }
        else { // 2=orden de compra parcial
            queryString = "SELECT DITEMC.IdCC, DITEMC.Idex, PROJT.Descripcion IdexDescripcion, DITEMC.NoCuenta, DITEMC.IdArea,";
            queryString += " DITEMC.Descripcion DescripcionCuenta, DITEMC.Cantidad, DITEMC.Precio, DITEMC.Total TotalTotal,";
            queryString += " PROJ.Numero";
            queryString += " FROM  DetalleItemsCostos DITEMC";
            queryString += " INNER JOIN project PROJ On PROJ.Numero = DITEMC.IdProject And PROJ.Estatus = 'ACTIVO'";
            queryString += " INNER JOIN project_tarea PROJT On PROJT.IdProject = PROJ.Id And PROJT.Idex = DITEMC.Idex";
            queryString += " LEFT JOIN proveedor_empresa Prov On Prov.IdProveedor = DITEMC.IdProveedor";
            queryString += " WHERE DITEMC.IdEmpresa = " + empresaId;
            queryString += " AND DITEMC.IDCC IN " + buscarCentrosCostoDIC();
            queryString += " AND DITEMC.IdProveedor = " + proveedorCbx.getValue();
            queryString += " AND DITEMC.Tipo IN ('INTINI','DOCA')";  //60111  lote 104
            queryString += " AND DITEMC.Moneda = '" + monedaCbx.getValue() + "'";
            queryString += " AND Prov.IdEmpresa = " + empresaId;
        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "TipoOrdenCompra=" + tipoOrdenCompraCbx.getValue() + " buscarDIC()Query = " + queryString);

        try {
//            Page.getCurrent().reload();

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado

//                BigDecimal totalCuentaQuetzales = new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP);
//                BigDecimal totalCuentaDolares = new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP);

                do {
                    if(   (rsRecords.getDouble("TotalTotal") > 0)
                       && (rsRecords.getDouble("TotalTotal") - getSaldo(
                                rsRecords.getString("Idex"),
                                rsRecords.getString("IdCC")) > 0)
                    ) {

                        Object itemId = idccContainer.addItem();

                        idccContainer.getContainerProperty(itemId, IDCC_PROPERTY).setValue(rsRecords.getString("IdCC"));
                        idccContainer.getContainerProperty(itemId, IDEX_PROPERTY).setValue(rsRecords.getString("Idex"));
                        idccContainer.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(rsRecords.getString("IdexDescripcion"));

//comentado el 18/11/2025 JA  if( tipoOrdenCompraCbx.getValue().equals("2") ) { // compra parcial
                            idccContainer.getContainerProperty(itemId, CUENTA_PROPERTY).setValue(rsRecords.getString("NoCuenta"));
                            idccContainer.getContainerProperty(itemId, DESCRIPCION_CUENTA_PROPERTY).setValue(rsRecords.getString("DescripcionCuenta"));
                            idccContainer.getContainerProperty(itemId, PROJECT_PROPERTY).setValue(rsRecords.getString("Numero"));
                            idccContainer.getContainerProperty(itemId, AREA_PROPERTY).setValue(rsRecords.getString("IdArea"));
                            idccContainer.getContainerProperty(itemId, CANTIDAD_PROPERTY).setValue(rsRecords.getString("Cantidad"));
                            idccContainer.getContainerProperty(itemId, PRECIO_PROPERTY).setValue(numberFormat.format(rsRecords.getDouble("Precio")));
//comentado el 18/11/2025 JA                        }
                        idccContainer.getContainerProperty(itemId, TOTAL_PROPERTY).setValue(numberFormat.format(rsRecords.getDouble("TotalTotal")));
                        idccContainer.getContainerProperty(itemId, TOTALSF_PROPERTY).setValue(rsRecords.getString("TotalTotal"));
                    }

                } while (rsRecords.next());

//                gridFooter.getCell(TOTAL_PROPERTY).setText(moneyFormat.format(totalCuentaQuetzales));
            }
        } catch (Exception ex) {
            Logger.getLogger(IntegracionItemCostos.class.getName()).log(Level.SEVERE, null, ex);
            Notification.show("Error al intentar leer detalle de integracion..!", Notification.Type.ERROR_MESSAGE);
        }
    }

    private void selectGrid() {
        /**
        queryString = " SELECT ODD.Idcc, ODD.Idex, ODD.NoCuenta, PIDX.Descripcion";
        queryString += " FROM orden_compra_detalle ODD";
        queryString += " INNER JOIN project_tarea PIDX ON PIDX.IDEX = ODD.IDEX";
        queryString += " INNER JOIN project On project.Numero = PIDX.IdProject And project.Estatus = 'ACTIVO'";
        queryString += " WHERE ODD.IdOrdenCompra = " + idOrdenCompra;
        queryString += " GROUP BY ODD.Idcc, ODD.Idex, ODD.NoCuenta, PIDX.Descripcion";
        **/

        queryString = " SELECT * ";
        queryString += " FROM orden_compra_detalle";
        queryString += " WHERE IdOrdenCompra = " + idOrdenCompra;

//        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "selectGrid() = " + queryString);

        try {
            Notification notif = new Notification("BUSCANDO DATOS... POR FAVOR ESPERAR!.", Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(5000);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.CLOCK_O);
            notif.show(Page.getCurrent());

//            Page.getCurrent().reload();

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado

                do {

                    for (Object itemId : idccContainer.getItemIds()) {
                        Item item = idccContainer.getItem(itemId);

                            if (item.getItemProperty(IDCC_PROPERTY).getValue().equals(rsRecords.getString("Idcc"))
                                    && item.getItemProperty(IDEX_PROPERTY).getValue().equals(rsRecords.getString("Idex"))
                                    && item.getItemProperty(CUENTA_PROPERTY).getValue().equals(rsRecords.getString("NoCuenta"))
                            ) {
                                idccGrid.select(itemId);
                            }
                    } // endfor

                } while (rsRecords.next());
            }
        } catch (Exception ex) {
            Logger.getLogger(IntegracionItemCostos.class.getName()).log(Level.SEVERE, null, ex);
            Notification.show("Error al intentar leer detalle de integracion..!", Notification.Type.ERROR_MESSAGE);
        }
    }

    private void selectGridCCOnly() {

        queryString = " SELECT *";
        queryString += " FROM orden_compra_detalle ODD";
        queryString += " WHERE ODD.IdOrdenCompra = " + idOrdenCompra;

//        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "selectGridOnly() = " + queryString);

        try {
            Notification notif = new Notification("BUSCANDO DATOS... POR FAVOR ESPERAR!.", Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(5000);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.CLOCK_O);
            notif.show(Page.getCurrent());

//            Page.getCurrent().reload();

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado

                do {

                    for (Object itemId : idccContainer.getItemIds()) {
                        Item item = idccContainer.getItem(itemId);

                        if(   item.getItemProperty(IDCC_PROPERTY).getValue().equals(rsRecords.getString("Idcc"))) {
                            idccGrid.select(itemId);
                        }
                    }

                } while (rsRecords.next());
            }
        } catch (Exception ex) {
            Logger.getLogger(IntegracionItemCostos.class.getName()).log(Level.SEVERE, null, ex);
            Notification.show("Error al intentar leer detalle de integracion..!", Notification.Type.ERROR_MESSAGE);
        }
    }

    private void setTotal(IndexedContainer indexedContainer, Grid grid, Grid.FooterRow gridFooter) {
        BigDecimal total = new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP);
        for (Object rid : grid.getContainerDataSource()
                .getItemIds()) {
            total = total.add(new BigDecimal(
                    Double.parseDouble(
                            String.valueOf(indexedContainer.getContainerProperty(rid, TOTALSF_PROPERTY).getValue())
                    )));
        }
    }

    /**
     * This class creates a streamresource. This class implements
     * the StreamSource interface which defines the getStream method.
     */
    public static class ShowExcelFile implements StreamResource.StreamSource {

        private final ByteArrayOutputStream os = new ByteArrayOutputStream();

        public ShowExcelFile(File fileToOpen) {
            try {

                FileOutputStream fost = new FileOutputStream(fileToOpen);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public InputStream getStream() {
            // Here we return the pdf contents as a byte-array
            return new ByteArrayInputStream(os.toByteArray());
        }
    }

    private String buscarCentrosCostoDIC() {

        StringBuilder centrosdecosto = new StringBuilder("(''");

        queryString = "SELECT DITEMC.IDCC, SUM(DITEMC.Total) TotalTotal ";
        queryString += " FROM  DetalleItemsCostos DITEMC";
        queryString += " INNER JOIN project On project.Numero = DITEMC.IdProject And project.Estatus = 'ACTIVO'";
        queryString += " LEFT JOIN proveedor_empresa Prov On Prov.IdProveedor = DITEMC.IdProveedor";
        queryString += " WHERE DITEMC.IdEmpresa = " + empresaId;
        queryString += " AND DITEMC.IdProveedor = " + proveedorCbx.getValue();
        queryString += " AND DITEMC.Tipo In ('INTINI','DOCA')";
        queryString += " AND DITEMC.Moneda = '" + monedaCbx.getValue() + "'";
        queryString += " AND Prov.IdEmpresa = " + empresaId;
        queryString += " GROUP BY DITEMC.IDCC";

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "buscarCentrosCostoDIC=" + queryString);

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado

                do {
                    if((rsRecords.getDouble("TotalTotal") - getSaldoIdcc(rsRecords.getString("IdCC"))) > 0) {
                        centrosdecosto.append(",'").append(rsRecords.getString("IdCC")).append("'");
                    }
                } while (rsRecords.next());

//                gridFooter.getCell(TOTAL_PROPERTY).setText(moneyFormat.format(totalCuentaQuetzales));
            }
        } catch (Exception ex) {
            Logger.getLogger(IntegracionItemCostos.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de detalle de integracion : " + ex.getMessage());
            Notification.show("Error al intentar leer detalle de integracion..!", Notification.Type.ERROR_MESSAGE);
        }
        centrosdecosto.append(",'')");

        return centrosdecosto.toString();
    }

    private double getSaldo(
            String IDEX,
            String CENTROCOSTO) {

        String
        queryString =  "SELECT SUM(DOCA.Total) TotalTotal ";
        queryString += " FROM  DocumentosContablesAplicados DOCA";
        queryString += " INNER JOIN project ON project.Numero = DOCA.IdProject AND project.Estatus = 'ACTIVO'";
        queryString += " WHERE  DOCA.Idex     = '" + IDEX + "'";
        queryString += " AND DOCA.IDCC = '" + CENTROCOSTO + "'";
        queryString += " AND DOCA.IdEmpresa   = " + empresaId;
        queryString += " AND DOCA.IdProveedor = " + proveedorCbx.getValue();
        queryString += " AND DOCA.Moneda = '" + monedaCbx.getValue() + "'";

//        System.out.println(queryString);

        double total = 0.00;

        try {

            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords1 = stQuery1.executeQuery (queryString);

            if(rsRecords1.next()) { //  encontrado
                total = rsRecords1.getDouble("TotalTotal");
            }
        }
        catch (Exception ex) {
            Logger.getLogger(IntegracionActual.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de detalle de integracion : " + ex.getMessage());
            Notification.show("Error al intentar leer detalle de integracion..!", Notification.Type.ERROR_MESSAGE);
        }

        return total;
    }

    private double getSaldoIdcc(String CENTROCOSTO) {

        String
                queryString =  "SELECT SUM(DOCA.Total) TotalTotal ";
        queryString += " FROM  DocumentosContablesAplicados DOCA ";
        queryString += " INNER JOIN project ON project.Numero = DOCA.IdProject AND project.Estatus = 'ACTIVO'";
        queryString += " WHERE DOCA.IdEmpresa   = " + empresaId;
        queryString += " AND DOCA.IdProveedor = " + proveedorCbx.getValue();
        queryString += " AND DOCA.IDCC = '" + CENTROCOSTO + "'";
        queryString += " AND DOCA.Moneda = '" + monedaCbx.getValue() + "'";

        Logger.getLogger(IntegracionItemCostos.class.getName()).log(Level.INFO, queryString);

        double total = 0.00;

        try {

            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords1 = stQuery1.executeQuery (queryString);

            if(rsRecords1.next()) { //  encontrado
                total = rsRecords1.getDouble("TotalTotal");
            }
        }
        catch (Exception ex) {
            Logger.getLogger(IntegracionActual.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de detalle de integracion  centro costo: " + ex.getMessage());
            Notification.show("Error al intentar leer detalle de integracion centro costo..!", Notification.Type.ERROR_MESSAGE);
        }

        return total;
    }

    private void buscarProductos(String cuenta) {
        queryString = "  SELECT *, proveedor.Nombre NombreProveedor";
        queryString += " FROM proveedor_plu ";
        queryString += " INNER JOIN proveedor_empresa on proveedor_plu.IdProveedor = proveedor_empresa.IdProveedor ";
        queryString += " INNER JOIN centro_costo_cuenta ON centro_costo_cuenta.IdCuentaCentroCosto = proveedor_plu.IdCuentaCentroCosto ";
        queryString += " WHERE proveedor_plu.IdProveedor = " + proveedorCbx.getValue();
        queryString += " AND centro_costo_cuenta.CodigoCuentaCentroCosto = " + cuenta;
        queryString += " AND proveedor_empresa.IdEmpresa = " + empresaId;

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, queryString);

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado
                do {
                    Object itemId = idccContainer.addItem();

                    idccGrid.getColumn(PROJECT_PROPERTY).setEditable(false).setHidden(true);
                    idccGrid.getColumn(IDEX_PROPERTY).setEditable(false).setHidden(true);

                    idccContainer.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords.getString("Id"));
                    idccContainer.getContainerProperty(itemId, IDEX_PROPERTY).setValue(rsRecords.getString("Idex"));

                } while (rsRecords.next());
            }
        } catch (Exception ex) {
            System.out.println("Error al llenar grid productos" + ex);
            Notification.show("Error al intentar leer productos..!", Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void buscarCC() {
        queryString = " SELECT * FROM centro_costo ";
        queryString += " WHERE Inhabilitado = 0";
        queryString += " AND IdEmpresa = " + empresaId;
        queryString += " ORDER BY CodigoCentroCosto";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                do {
                    Object itemId = idccContainer.addItem();

                    idccContainer.getContainerProperty(itemId, IDCC_PROPERTY).setValue(rsRecords.getString("CodigoCentroCosto"));

                } while (rsRecords.next());
            }
        } catch (Exception ex1) {
            System.out.println("Error al grid centro de costos: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void guardarOrdenCompra() {

        try {

            if (Integer.parseInt(tipoOrdenCompraCbx.getValue().toString()) < 3 && idccContainer.size() == 0) {
                Notification.show("DEBE AGREGAR PRODUCTOS A LA ORDEN DE COMPRA", Notification.Type.WARNING_MESSAGE);
                return;
            }
            if (proveedorCbx.getValue() == null) {
                Notification.show("POR FAVOR SELECCIONE UN PROVEEDOR.", Notification.Type.WARNING_MESSAGE);
                proveedorCbx.focus();
                return;
            }
            if (responsableTxt.getValue().trim().isEmpty()) {
                Notification.show("POR FAVOR INGRESE UN RESPONSABLE A LA ORDEN DE COMPRA.", Notification.Type.WARNING_MESSAGE);
                responsableTxt.focus();
                return;
            }
            if (monedaCbx.getValue() == null) {
                Notification.show("POR FAVOR INGRESE UNA MONEDA.", Notification.Type.WARNING_MESSAGE);
                monedaCbx.focus();
                return;
            }
            if (Integer.parseInt((String) tipoOrdenCompraCbx.getValue()) > 2 && cuentaContableCbx.getValue() == null) {
                Notification.show("POR FAVOR SELECCIONE UNA CUENTA CONTABLE.", Notification.Type.WARNING_MESSAGE);
                cuentaContableCbx.focus();
                return;
            }
//            if (cuentaContableCbx.getValue() == "0") {
//                Notification.show("POR FAVOR SELECCIONE UNA CUENTA CONTABLE.", Notification.Type.WARNING_MESSAGE);
//                cuentaContableCbx.focus();
//                return;
//            }
            if (montoTxt.getDoubleValueDoNotThrow() == 0) {
                Notification.show("POR FAVOR INGRESE EL MONTO.", Notification.Type.WARNING_MESSAGE);
                montoTxt.focus();
                return;
            }
//            if(anticipoTxt.getDoubleValueDoNotThrow() >= montoTxt.getDoubleValueDoNotThrow()) {
//                Notification.show("EL ANTICIPO NO PUEDE SER MAYOR AL MONTO DE LA ORDEN DE COMPRA.", Notification.Type.WARNING_MESSAGE);
//                anticipoTxt.focus();
//                return;
//            }
            // validacion de anticipo vs retencion isr solo cuando el proveedor es del regimen normal
            if(proveedorCbx.getItem(proveedorCbx.getValue()).getItemProperty("Regimen").getValue() != null) {
                if(montoTxt.getDoubleValueDoNotThrow() > 2500) {
                    if (proveedorCbx.getItem(proveedorCbx.getValue()).getItemProperty("Regimen").getValue().toString().equals("SUJETO A RETENCION ISR")) {
                        if (retencionIsrTxt.getDoubleValueDoNotThrow() > 0) {
                            if (anticipoTxt.getDoubleValueDoNotThrow() > (montoTxt.getDoubleValueDoNotThrow() - retencionIsrTxt.getDoubleValueDoNotThrow())) {
                                Notification.show("EL ANTICIPO NO PUEDE SER MAYOR AL MONTO DE LA ORDEN DE COMPRA - LA RETENCION ISR.", Notification.Type.WARNING_MESSAGE);
                                anticipoTxt.focus();
                                return;
                            }
                        }
                    }
                }
            }

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            if(idOrdenCompra.trim().isEmpty()) {
                int siguienteOrdenCompra = 1;

                queryString =  "SELECT MAX(CorrelativoOC) UltimaOC ";
                queryString += " FROM  orden_compra ";
                queryString += " WHERE IdEmpresa = " + empresaId;
                queryString += " AND   IdProveedor = " + proveedorCbx.getValue();
                queryString += " AND   IdProyecto  = " + ((SopdiUI) mainUI).sessionInformation.getStrProjectId();

                Logger.getLogger(this.getClass().getName()).log(Level.INFO, queryString);

                try {

                    stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                    rsRecords = stQuery.executeQuery (queryString);

                    if(rsRecords.next()) { //  encontrado
                        if(rsRecords.getObject("UltimaOC") != null) {
                            siguienteOrdenCompra = rsRecords.getInt("UltimaOC") + 1;
                        }
                    }
                }
                catch (Exception ex) {
                    Logger.getLogger(EstimacionesWindow.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("Error al obtener valor de siguiente orden de compra : " + ex.getMessage());
                    Notification.show("Error al obtener valor de siguiente orden de compra..!", Notification.Type.ERROR_MESSAGE);
                }

                queryString = "INSERT INTO orden_compra ";
                queryString += "(IdTipoOrdenCompra, NOC, CorrelativoOC, IdEmpresa, IdProyecto, IdProveedor, Fecha, ";
                queryString += " Moneda, Total, PorcentajeTolerancia, Responsable, DireccionEntrega, ReferenciaEntrega, ContactoEnObra, ";
                queryString += " Anticipo, IdNomenclatura, Nombrecheque, CotizacionReferencia, Razon, ";
                queryString += " CreadoFechaYHora, CreadoUsuario)";
                queryString += " VALUES ";
                queryString += "(" + tipoOrdenCompraCbx.getValue();
                queryString += ",'NOC" + empresaId + "_" + proveedorCbx.getValue() + String.format("%04d", siguienteOrdenCompra ) + "'";
                queryString += "," + siguienteOrdenCompra;
                queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
                queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrProjectId();
                queryString += "," + proveedorCbx.getValue();
                queryString += ",'" + Utileria.getFechaYYYYMMDD_1(new java.util.Date()) + "'";
                queryString += ",'" + monedaCbx.getValue() + "'";
                queryString += "," + montoTxt.getDoubleValueDoNotThrow();
                queryString += "," + porcentajeToleranciaTxt.getDoubleValueDoNotThrow();
                queryString += ",'" + responsableTxt.getValue() + "'";
                queryString += ",'" + direccionTxt.getValue() + "'";
                queryString += ",'" + referenciaEtregaTxt.getValue() + "'";
                queryString += ","  + contactoObraCbx.getValue();
                queryString += "," + anticipoTxt.getDoubleValueDoNotThrow();
                queryString += "," + cuentaContableCbx.getValue();
                queryString += ",'" + nombreChequeTxt.getValue() + "'";
                queryString += ",'" + cotizacionReferenciaTxt.getValue() + "'";
                queryString += ",'" + razonTxt.getValue() + "'";
                queryString += ",current_timestamp";
                queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                queryString += ")";

                Logger.getLogger(this.getClass().getName()).log(Level.INFO, queryString);

                stPreparedQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().prepareStatement(queryString, Statement.RETURN_GENERATED_KEYS);
                stPreparedQuery.executeUpdate();
                rsRecords = stPreparedQuery.getGeneratedKeys();

                rsRecords.next();

                idOrdenCompra = String.valueOf(rsRecords.getInt(1));

            } else { // actualizar orden de compra

                queryString = "UPDATE orden_compra SET ";
                queryString += " Total = " + montoTxt.getDoubleValueDoNotThrow();
                queryString += ",PorcentajeTolerancia = " + porcentajeToleranciaTxt.getDoubleValueDoNotThrow();
                queryString += ",Responsable = '" + responsableTxt.getValue() + "'";
                queryString += ",DireccionEntrega= '" + direccionTxt.getValue() + "'";
                queryString += ",ReferenciaEntrega= '" + referenciaEtregaTxt.getValue() + "'";
                queryString += ",CotizacionReferencia= '" + cotizacionReferenciaTxt.getValue() + "'";
                queryString += ",ContactoEnObra= " + contactoObraCbx.getValue();
                queryString += ",Anticipo = " + anticipoTxt.getDoubleValueDoNotThrow();
                queryString += ",Moneda = '" + monedaCbx.getValue() + "'";
                queryString += ",Razon = '" + razonTxt.getValue() + "'";
                queryString += ",IdNomenclatura = " + cuentaContableCbx.getValue();
                queryString += ",CreadoUsuario = " + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                queryString += " WHERE Id = " + idOrdenCompra;

                stQuery.executeUpdate(queryString);

            }

            ArrayList<PluTemp> pluTemps = new ArrayList<>();

            if(Integer.parseInt(tipoOrdenCompraCbx.getValue().toString()) == 2 ) {
                /* leer tabla orden_compra_detalle y guardar los registros en un arreglo temporal de Cuenta, Area, PrvPlu prvPluDes
                    para luego susbtituir el PluPrv y PluPrvDesc en el isert into
                 */
                queryString = " SELECT * FROM orden_compra_detalle WHERE IdOrdenCompra = " + idOrdenCompra;
                // Logger.getLogger(this.getClass().getName()).log(Level.INFO, queryString);
                stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                rsRecords1 = stQuery.executeQuery(queryString);
                if (rsRecords1.next()) { //  encontrado
                    do {
                        PluTemp pluTemp = new PluTemp(
                                rsRecords1.getString("NoCuenta"),
                                rsRecords1.getString("IdArea"),
                                rsRecords1.getString("PluPrv"),
                                rsRecords1.getString("PluPrvDes"));
                        pluTemps.add(pluTemp);

                    } while (rsRecords1.next());
                }
            }

            // eliminar todos los registros de orden_compra_detalle relacionados a la orden de compra
            // para luego insertar los registros seleccionados en el grid
            queryString = "delete from orden_compra_detalle where IdOrdenCompra = " + idOrdenCompra;
            stQuery.executeUpdate(queryString);

            for (Object itemId : idccContainer.getItemIds()) {
                Item item = idccContainer.getItem(itemId);

                if(idccGrid.isSelected(itemId)) {

                    queryString = "INSERT INTO orden_compra_detalle";
                    if(tipoOrdenCompraCbx.getValue().equals("2")) { //COMPRA PARCIAL
                        queryString += "(IdOrdenCompra, IDCC, IDEX, NoCuenta, Descripcion, PluPrv, PluPrvDes, ";
                        queryString += " IdProject, IdArea, Cantidad, Precio, Total, CreadoFechaYHora, CreadoUsuario)";
                        queryString += " VALUES ";

                        String cuenta = String.valueOf(item.getItemProperty(CUENTA_PROPERTY).getValue());
                        String searchQuery = "  SELECT * FROM proveedor_plu ";
                        searchQuery += " INNER JOIN centro_costo_cuenta on centro_costo_cuenta.IdCuentaCentroCosto = proveedor_plu.IdCuentaCentroCosto ";
                        searchQuery += " WHERE proveedor_plu.IdProveedor = " + proveedorCbx.getValue();
                        searchQuery += " AND centro_costo_cuenta.CodigoCuentaCentroCosto = '" + cuenta + "'";

                        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "QUERY PARA BUSCAR PRODUCTOS PLU DE LA CUENTA : " + searchQuery);

                        rsRecords1 = stQuery.executeQuery(searchQuery);

                        if(rsRecords1.next()) {
                            do {
                                queryString += "(" + idOrdenCompra;
                                queryString += ",'" + item.getItemProperty(IDCC_PROPERTY).getValue() + "'";
                                queryString += ",'" + item.getItemProperty(IDEX_PROPERTY).getValue() + "'";
                                queryString += ",'" + item.getItemProperty(CUENTA_PROPERTY).getValue() + "'";
                                queryString += ",'" + item.getItemProperty(DESCRIPCION_CUENTA_PROPERTY).getValue() + "'";
                                /*recorrer el array temporal para buscar si la cuenta y area ya tienen un plu asignado en la orden de compra
                                      si es asi, usar ese plu y descripcion
                                 */
                                String prvPlu = rsRecords1.getString("Plu");
                                String prvPluDes = rsRecords1.getString("DescripcionProveedor");

                                for(PluTemp pt : pluTemps) {
                                    if(pt.getCuenta().equals(cuenta)
                                            && pt.getArea().equals(String.valueOf(item.getItemProperty(AREA_PROPERTY).getValue()))
                                    ) {
                                        prvPlu = pt.getPrvPlu();
                                        prvPluDes = pt.getPrvPluDes();
                                        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "SE ENCONTRO PLU TEMPORAL PARA LA CUENTA : " + cuenta + " Y AREA : " + String.valueOf(item.getItemProperty(AREA_PROPERTY).getValue())
                                                + " USANDO PLU : " + prvPlu + " DESCRIPCION : " + prvPluDes);
                                        break;
                                    }
                                }
                                queryString += ",'" + prvPlu + "'";
                                queryString += ",'" + prvPluDes + "'";
                                queryString += ",'" + item.getItemProperty(PROJECT_PROPERTY).getValue() + "'";
                                queryString += ", " + item.getItemProperty(AREA_PROPERTY).getValue();
                                queryString += "," + Double.parseDouble(String.valueOf(item.getItemProperty(CANTIDAD_PROPERTY).getValue()).replaceAll(",", ""));
                                queryString += "," + Double.parseDouble(String.valueOf(item.getItemProperty(PRECIO_PROPERTY).getValue()).replaceAll(",", ""));
                                queryString += "," + Double.parseDouble(String.valueOf(item.getItemProperty(TOTAL_PROPERTY).getValue()).replaceAll(",", ""));
                                queryString += ",current_timestamp";
                                queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                                queryString += "),";

                                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "QUERY ORDEN DE COMPRA DETALLE : " + queryString );

                            } while(rsRecords1.next());
                            queryString = queryString.substring(0, queryString.length() - 1); // remove last comma
                        } else {
                            Notification notif = new Notification("NO SE HAN ENCONTRADO PRODUCTOS PLU RELACIONADOS A LA CUENTA : " + cuenta, Notification.Type.WARNING_MESSAGE);
                            notif.setDelayMsec(3000);
                            notif.setPosition(Position.MIDDLE_CENTER);
                            notif.setIcon(FontAwesome.WARNING);
                            notif.show(Page.getCurrent());
                        }
                    }
                    else { // compra ESTIMACION, eventual, recurrente
                        queryString += "(IdOrdenCompra, IDCC, IDEX, NoCuenta, Descripcion, ";
                        queryString += " IdProject, IdArea, Cantidad, Precio, Total, CreadoFechaYHora, CreadoUsuario)";
                        queryString += " VALUES ";
                        queryString += "(" + idOrdenCompra;
                        queryString += ",'" + item.getItemProperty(IDCC_PROPERTY).getValue() + "'";
                        queryString += ",'" + item.getItemProperty(IDEX_PROPERTY).getValue() + "'";
                        queryString += ",'" + item.getItemProperty(CUENTA_PROPERTY).getValue() + "'";
                        if(String.valueOf(item.getItemProperty(DESCRIPCION_CUENTA_PROPERTY).getValue()).isEmpty()) {
                            queryString += ",'" + item.getItemProperty(DESCRIPCION_PROPERTY).getValue() + "'";
                        }
                        else {
                            queryString += ",'" + item.getItemProperty(DESCRIPCION_CUENTA_PROPERTY).getValue() + "'";
                        }
                        queryString += ",'" + item.getItemProperty(PROJECT_PROPERTY).getValue() + "'";
                        queryString += ", " + item.getItemProperty(AREA_PROPERTY).getValue();
                        queryString += "," + Double.parseDouble(String.valueOf(item.getItemProperty(CANTIDAD_PROPERTY).getValue()).replaceAll(",", ""));
                        queryString += "," + Double.parseDouble(String.valueOf(item.getItemProperty(PRECIO_PROPERTY).getValue()).replaceAll(",", ""));
                        queryString += "," + Double.parseDouble(String.valueOf(item.getItemProperty(TOTAL_PROPERTY).getValue()).replaceAll(",", ""));
                        queryString += ",current_timestamp";
                        queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                        queryString += ")";
                    }
                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, "QUERY INSERT ORDEN DE COMPRA DETALLE : " + queryString );

                    stQuery.executeUpdate(queryString);
                }
            }

            Notification notif = new Notification("ORDEN DE COMPRA GENERADA EXITOSAMENTE!.", Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.CHECK);
            notif.show(Page.getCurrent());

            ((OrdenCompraView) (mainUI.getNavigator().getCurrentView())).llenarTablaOrdenCompra();

            if(tipoOrdenCompraCbx.getValue().equals("1") || tipoOrdenCompraCbx.getValue().equals("2")) {
                OrdenCompraEstimacionPDF pdfOrdenCompra =
                        new OrdenCompraEstimacionPDF(
                                idOrdenCompra,
                                String.valueOf(tipoOrdenCompraCbx.getValue()),
                                tipoOrdenCompraCbx.getItemCaption(tipoOrdenCompraCbx.getValue()),
                                (proveedorCbx.getValue() + " " + proveedorCbx.getItemCaption(proveedorCbx.getValue())),
                                anticipoTxt.getDoubleValueDoNotThrow()
                                );
                UI.getCurrent().addWindow(pdfOrdenCompra);
                pdfOrdenCompra.center();
            } else { //eventual, recurrente
                OrdenCompraPDF pdfOrdenCompra =
                        new OrdenCompraPDF(
                                idOrdenCompra,
                                String.valueOf(tipoOrdenCompraCbx.getValue()),
                                tipoOrdenCompraCbx.getItemCaption(tipoOrdenCompraCbx.getValue()),
                                (proveedorCbx.getValue() + " " + proveedorCbx.getItemCaption(proveedorCbx.getValue())),
                                anticipoTxt.getDoubleValueDoNotThrow()
                        );
                UI.getCurrent().addWindow(pdfOrdenCompra);
                pdfOrdenCompra.center();
            }

//            close();

        } catch (Exception ex) {
            Notification notif = new Notification("HA OCURRIDO UN ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.WARNING);
            notif.show(Page.getCurrent());
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }
    }

    public void llenarDatos(String idOrdenCompra) {

        idccContainer.removeAllItems();

        queryString = " SELECT * ";
        queryString += " FROM orden_compra";
        queryString += " WHERE Id = " + idOrdenCompra;

        try {
            stQuery2 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery3 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords2 = stQuery2.executeQuery(queryString);

            if (rsRecords2.next()) { //  encontrado
                tipoOrdenCompraCbx.setValue(rsRecords2.getString("IdTipoOrdenCompra"));
                tipoOrdenCompraCbx.select(rsRecords2.getString("IdTipoOrdenCompra"));
                tipoOrdenCompraCbx.setValue(rsRecords2.getString("IdTipoOrdenCompra"));
                proveedorCbx.select(rsRecords2.getString("IdProveedor"));
                proveedorCbx.setValue(rsRecords2.getString("IdProveedor"));
                responsableTxt.setValue(rsRecords2.getString("Responsable"));
                direccionTxt.setValue(rsRecords2.getString("DireccionEntrega"));
                referenciaEtregaTxt.setValue(rsRecords2.getString("ReferenciaEntrega"));
                cotizacionReferenciaTxt.setValue(rsRecords2.getString("CotizacionReferencia"));
                contactoObraCbx.select(rsRecords2.getString("ContactoEnObra"));
                contactoObraCbx.setValue(rsRecords2.getString("ContactoEnObra"));
                montoTxt.setReadOnly(false);
                montoTxt.setValue(rsRecords2.getDouble("Total"));
                montoTxt.setReadOnly(true);
                porcentajeToleranciaTxt.setValue(rsRecords2.getDouble("PorcentajeTolerancia"));
//                anticipoTxt.setReadOnly(false);
                anticipoTxt.setValue(rsRecords2.getDouble("Anticipo"));
//                anticipoTxt.setReadOnly(true);
                cuentaContableCbx.select(rsRecords2.getString("IdNomenclatura"));
                monedaCbx.select(rsRecords2.getString("Moneda"));
                razonTxt.setValue(rsRecords2.getString("Razon"));
                if(rsRecords2.getString("Estado").equals("CERRADA")) {
                    guardarBtn.setEnabled(false);
                }
                titleLbl.setValue("EDITANDO ORDEN DE COMPRA : " + rsRecords2.getString("NOC").substring(3, rsRecords2.getString("NOC").length()));
            }
        } catch (Exception ex) {
            System.out.println("Error al listar tablea orden compra detalle:" + ex);
            ex.printStackTrace();
        }
    }

    private void cargarArchivo(File planillaFile) {

        singleUpload.setEnabled(false);
        idccContainer.removeAllItems();

        try {

            fileInputStream = new FileInputStream(planillaFile);

            workbook = new XSSFWorkbook(fileInputStream);

            sheet = workbook.getSheetAt(0);

            System.out.println("\n Total lineas en archivo=" + sheet.getLastRowNum());
            System.out.println("...INICIO...");

            Object itemId; int recordCount = 0;;

            ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().setAutoCommit(false);

            stQuery  = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery1 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            for (int linea = 1; linea <= sheet.getLastRowNum(); linea++) {

                try {
                    sheet.getRow(linea).getCell(0).getRawValue();
                }
                catch(Exception exNull) {
                    System.out.println("OUT...");
                    break;
                }

                itemId = idccContainer.addItem();

                idccContainer.getContainerProperty(itemId,"IDCC").setValue(sheet.getRow(linea).getCell(0).getStringCellValue());
                idccContainer.getContainerProperty(itemId,"IDEX").setValue(sheet.getRow(linea).getCell(1).getStringCellValue());

                queryString = "SELECT DITEMC.IDCC, DITEMC.IDEX, PROJT.Descripcion IdexDescripcion, SUM(DITEMC.Total) TotalTotal ";
                queryString += " FROM  DetalleItemsCostos DITEMC";
                queryString += " INNER JOIN project PROJ ON PROJ.Numero = DITEMC.IdProject AND PROJ.Estatus = 'ACTIVO'";
                queryString += " INNER JOIN project_tarea PROJT On PROJT.IdProject = PROJ.Id And PROJT.Idex = DITEMC.Idex";
                queryString += " LEFT JOIN proveedor_empresa Prov On Prov.IdProveedor = DITEMC.IdProveedor";
                queryString += " WHERE DITEMC.IdEmpresa = " + empresaId;
                queryString += " AND DITEMC.IDCC = '" + sheet.getRow(linea).getCell(0).getStringCellValue() + "'";
                queryString += " AND DITEMC.IDEX = '" + sheet.getRow(linea).getCell(1).getStringCellValue() + "'";
                queryString += " AND DITEMC.IdProveedor = " + proveedorCbx.getValue();
                queryString += " AND DITEMC.Tipo In ('INTINI','DOCA')";
                queryString += " AND DITEMC.Moneda = '" + monedaCbx.getValue() + "'";
                queryString += " AND Prov.IdEmpresa = " + empresaId;
                queryString += " GROUP BY Idcc, Idex, IdexDescripcion";

                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "buscarDICCargaArchivo() = " + queryString);

                try {
                    stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                    rsRecords = stQuery.executeQuery(queryString);

                    if (rsRecords.next()) { //  encontrado
                        idccContainer.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(rsRecords.getString("IdexDescripcion"));
                        idccContainer.getContainerProperty(itemId, TOTAL_PROPERTY).setValue(numberFormat.format(rsRecords.getDouble("TotalTotal")));
                        idccContainer.getContainerProperty(itemId, TOTALSF_PROPERTY).setValue(rsRecords.getString("TotalTotal"));
                        idccGrid.select(itemId);
                    }
                }
                catch(Exception exNull) {
                    Notification notif = new Notification("HA OCURRIDO UN ERROR DE BASE DE DATOS : " + exNull.getMessage(), Notification.Type.HUMANIZED_MESSAGE);
                    notif.setDelayMsec(1500);
                    notif.setPosition(Position.MIDDLE_CENTER);
                    notif.setIcon(FontAwesome.WARNING);
                    notif.show(Page.getCurrent());
                    System.out.println("Error en base datos al buscar IDEX en DIC. " + exNull);
                    exNull.printStackTrace();
                }
                recordCount++;

            } //endfor

            ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().commit();
            ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().setAutoCommit(true);

            System.out.println("...FIN...");

            Notification.show("Archivo cargado exitosamente! " + recordCount + " idex cargados.", Notification.Type.HUMANIZED_MESSAGE);

        } catch (Exception ex1) {
            new Notification("Error al intentar cargar archivo Excel IDEX.",
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
        singleUpload.setEnabled(true);
    }

}

class PluTemp {
    private String cuenta;
    private String area;
    private String prvPlu;
    private String prvPluDes;

    public PluTemp(String cuenta, String area, String prvPlu, String prvPluDesc) {
        this.cuenta = cuenta;
        this.area = area;
        this.prvPlu = prvPlu;
        this.prvPluDes = prvPluDesc;
    }

    public String getPrvPlu() {
        return prvPlu;
    }

    public void setPrvPlu(String prvPlu) {
        this.prvPlu = prvPlu;
    }

    public String getPrvPluDes() {
        return prvPluDes;
    }

    public void setPrvPluDesc(String prvPluDes) {
        this.prvPluDes = prvPluDes;
    }

    public String getCuenta() {
        return cuenta;
    }

    public void setCuenta(String cuenta) {
        this.cuenta = cuenta;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }
}
