package com.simpletecno.sopdi.compras;

import com.simpletecno.sopdi.configuracion.EnvironmentVars;
import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.simpletecno.sopdi.ventas.ExencionIvaInfileForm;
import com.simpletecno.sopdi.contabilidad.TransaccionesEspecialesForm;
import com.vaadin.addon.tableexport.DefaultTableHolder;
import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.addon.tableexport.TableHolder;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.SelectionEvent;
import com.vaadin.event.SelectionEvent.SelectionListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.HeaderCell;
import com.vaadin.ui.Grid.HeaderRow;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.renderers.ClickableRenderer;
import com.vaadin.ui.themes.ValoTheme;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.MultiFileUpload;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadFinishedHandler;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadStateWindow;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Date;

import org.vaadin.dialogs.ConfirmDialog;

/**
 * @author user
 */
public class IngresoDocumentosView extends VerticalLayout implements View {

    static final String CODIGOCC_ABASTOS = "20210401000";
    MultiFileUpload singleUpload;
    Image logoImage;
    public File file;
    StreamResource logoStreamResource = null;
    String parametro1, parametro2;
    Long parametro3;
    long fileSize;

    Double totalHaber;
    Double totalDebe;

    Grid documentosGrid;
    public IndexedContainer documentsContainer = new IndexedContainer();
    static final String ID_PROPERTY = "Id";
    static final String TIPODOCUMENTO_PROPERTY = "TIPODOC";
    static final String DOCUMENTO_PROPERTY = "Documento";
    static final String FECHA_PROPERTY = "Fecha";
    static final String DIAHOY_PROPERTY = "Días";
    static final String PROVEEDOR_PROPERTY = "Proveedor";
    static final String NIT_PROVEEDOR_PROPERTY = "NIT";
    static final String CODIGO_PROPERTY = "Partida";
    static final String VALOR_PROPERTY = "Monto";
    static final String TIPOCAMBIO_PROPERTY = "T_Cambio";
    static final String MONTO_QUETZALES_PROPERTY = "Monto Q.";
    static final String ESTATUS_PROPERTY = "Estatus";
    static final String DESCRIPCION_PROPERTY = "Descripción";
    static final String USUARIO_PROPERTY = "Usuario";
    static final String NOC_PROPERTY = "NOC";
    static final String IMAGEN_PROPERTY = "Imagen";
    static final String ELIMINAR_PROPERTY = "Eliminar";
    static final String ARCHIVO_PROPERTY = "Archivo";
    static final String ARCHIVO_TIPO_PROPERTY = "Type";
    static final String VALORSF_PROPERTY = "MSF";
    static final String MONTOQSF_PROPERTY = "MQSF";
    static final String RETISR_PROPERTY = "REST.ISR";
    static final String ID_NOMENCLATURA_PROPERTY = "ID_NOMENCLATURA";

    static final String IDCENTROCOSTO_PROPERTY = "IDCENTROCOSTO";

    Grid.FooterRow footerFacturas;

    Grid partidaDocumentosGrid;
    public IndexedContainer documentsContainerPartida = new IndexedContainer();
    static final String ID_PARTIDA_PROPERTY = "Id";
    static final String PARTIDA_PROPERTY = "Partida";
    static final String CUENTA_PROPERTY = "Cuenta";
    static final String DESCRIPCION_PARTIDA_PROPERTY = "Descripción";
    static final String DEBE_PROPERTY = "Debe";
    static final String HABER_PROPERTY = "Haber";
    static final String CODIGOCC_PROPERTY = "CODIGOCC";
    Grid.FooterRow footer;

    Grid cuentaCorrientGrid;
    public IndexedContainer cuentaCorrienteContainer = new IndexedContainer();
    Grid.FooterRow footerCC;

    OptionGroup fechaOpcion;
    DateField inicioDt;
    DateField finDt;

    TextField documentTxt;

    UI mainUI;
    Statement stQuery, stQuery1, stQuery2;
    ResultSet rsRecords, rsRecords2;
    String queryString;

    Button editBtn;
    Button exencionBtn;
    Button revisadoBtn;
    Button isrBtn;
    Button notaCreditoBtn;
    Button nuevaFacturaBtn;
    Button consultarBtn;
    Button pendienteIsr;
    Button anularBtn;

    int vanderaIsr = 0;

    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");

    VerticalLayout reportLayoutPartida = new VerticalLayout();
    EnvironmentVars enviromentsVars;

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public IngresoDocumentosView() {

        reportLayoutPartida.setEnabled(false);
        this.mainUI = UI.getCurrent();
        setWidth("100%");
        setMargin(false);
        setSpacing(true);

        enviromentsVars = new EnvironmentVars();

        Label titleLbl = new Label(empresaId + " " + empresaNombre + " DOCUMENTOS COMPRA");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h1_custom");

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setResponsive(true);
        titleLayout.setSpacing(true);
        titleLayout.setWidth("100%");
        titleLayout.setMargin(false);
        titleLayout.addComponents(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.MIDDLE_CENTER);
        titleLayout.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

        addComponent(titleLayout);
        setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        crearTablaFacturas();
        createTablaPartidaYCuentaCorriente();

        if (partidaDocumentosGrid != null) {
            llenarTablaFactura(empresaId, 0);
        }
    }

    public void crearTablaFacturas() {

        VerticalLayout reportLayout = new VerticalLayout();
        reportLayout.addStyleName("rcorners3");
        reportLayout.setWidth("100%");
        reportLayout.setResponsive(true);
        reportLayout.setSpacing(true);
        reportLayout.setMargin(false);

        HorizontalLayout layoutGrid = new HorizontalLayout();
        layoutGrid.setWidth("100%");
        layoutGrid.setSpacing(true);
        layoutGrid.setMargin(false);

        HorizontalLayout filtrosLayout = new HorizontalLayout();
        filtrosLayout.setSpacing(true);
        filtrosLayout.setMargin(false);

        inicioDt = new DateField("DEL:");
        inicioDt.setDateFormat("dd/MM/yyyy");
        Date primerDia = Utileria.getPrimerDiaDelMes();
        inicioDt.setValue(primerDia);
        inicioDt.setWidth("10em");

        finDt = new DateField("AL:");
        finDt.setDateFormat("dd/MM/yyyy");
        Date ultimoDia = Utileria.getUltimoDiaDelMes();
        finDt.setValue(ultimoDia);
        finDt.setWidth("10em");

        fechaOpcion = new OptionGroup();
        fechaOpcion.addItems("F. Ingreso", "F. Factura");

        documentTxt = new TextField("Documento : ");
//        documentTxt.set

        consultarBtn = new Button("Consultar");
        consultarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        consultarBtn.setIcon(FontAwesome.SEARCH);
        consultarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                llenarTablaFactura(empresaId, 0);
            }
        });

        pendienteIsr = new Button("Pendientes ISR");
        pendienteIsr.addStyleName(ValoTheme.BUTTON_PRIMARY);
        pendienteIsr.setIcon(FontAwesome.SEARCH);
        pendienteIsr.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                vanderaIsr = 1; /// buscar las facturas pendeitnes de isr
                llenarTablaFactura(empresaId, 0);
            }
        });

        documentsContainer.addContainerProperty(ID_PROPERTY, String.class, null);
        documentsContainer.addContainerProperty(FECHA_PROPERTY, String.class, null);
        documentsContainer.addContainerProperty(DIAHOY_PROPERTY, String.class, null);
        documentsContainer.addContainerProperty(TIPODOCUMENTO_PROPERTY, String.class, null);
        documentsContainer.addContainerProperty(DOCUMENTO_PROPERTY, String.class, null);
        documentsContainer.addContainerProperty(PROVEEDOR_PROPERTY, String.class, null);
        documentsContainer.addContainerProperty(NIT_PROVEEDOR_PROPERTY, String.class, null);
        documentsContainer.addContainerProperty(VALOR_PROPERTY, String.class, null);
        documentsContainer.addContainerProperty(TIPOCAMBIO_PROPERTY, String.class, null);
        documentsContainer.addContainerProperty(MONTO_QUETZALES_PROPERTY, String.class, null);
        documentsContainer.addContainerProperty(CODIGO_PROPERTY, String.class, null);
        documentsContainer.addContainerProperty(ESTATUS_PROPERTY, String.class, null);
        documentsContainer.addContainerProperty(DESCRIPCION_PROPERTY, String.class, null);
        documentsContainer.addContainerProperty(USUARIO_PROPERTY, String.class, null);
        documentsContainer.addContainerProperty(NOC_PROPERTY, String.class, null);
        documentsContainer.addContainerProperty(IMAGEN_PROPERTY, String.class, null);
        documentsContainer.addContainerProperty(ELIMINAR_PROPERTY, String.class, null);
        documentsContainer.addContainerProperty(CODIGOCC_PROPERTY, String.class, null);
        documentsContainer.addContainerProperty(RETISR_PROPERTY, String.class, null);
        documentsContainer.addContainerProperty(ARCHIVO_PROPERTY, String.class, null);
        documentsContainer.addContainerProperty(ARCHIVO_TIPO_PROPERTY, String.class, null);
        documentsContainer.addContainerProperty(VALORSF_PROPERTY, String.class, null);
        documentsContainer.addContainerProperty(MONTOQSF_PROPERTY, String.class, null);
        documentsContainer.addContainerProperty(ID_NOMENCLATURA_PROPERTY, String.class, null);
        documentsContainer.addContainerProperty(IDCENTROCOSTO_PROPERTY, String.class, null);

        documentosGrid = new Grid("", documentsContainer);
        documentosGrid.setWidth("100%");
        documentosGrid.setImmediate(true);
        documentosGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        documentosGrid.setDescription("Seleccione un registro.");
        documentosGrid.setHeightMode(HeightMode.ROW);
        documentosGrid.setHeightByRows(4);
        documentosGrid.setResponsive(true);
        documentosGrid.setEditorBuffered(false);

        documentosGrid.getColumn(IMAGEN_PROPERTY).setRenderer(new ButtonRenderer(e -> {

            if (documentsContainer.getContainerProperty(e.getItemId(), IMAGEN_PROPERTY).getValue().equals("Cargar archivo")) {
                String codigoPartida = String.valueOf(documentsContainer.getContainerProperty(e.getItemId(), CODIGO_PROPERTY).getValue());
                documentosGrid.select(e.getItemId());

                CargarArchivoIngresoDocumentos cargarArchivo
                        = new CargarArchivoIngresoDocumentos(e.getItemId(), codigoPartida);
                UI.getCurrent().addWindow(cargarArchivo);
                cargarArchivo.center();

            } else {
                VerCambiarImagen(e);
            }
        }));

        documentosGrid.getColumn(ID_PROPERTY).setHidable(true).setHidden(true);
        documentosGrid.getColumn(NOC_PROPERTY).setHidable(true).setHidden(true);
        documentosGrid.getColumn(CODIGO_PROPERTY).setHidable(true);
        documentosGrid.getColumn(FECHA_PROPERTY).setHidable(true).setHidden(false);
        documentosGrid.getColumn(NIT_PROVEEDOR_PROPERTY).setHidable(true).setHidden(true);
        documentosGrid.getColumn(USUARIO_PROPERTY).setHidable(true).setHidden(true);
        documentosGrid.getColumn(DESCRIPCION_PROPERTY).setHidable(true).setHidden(true);
        documentosGrid.getColumn(ID_PROPERTY).setExpandRatio(1);
        documentosGrid.getColumn(FECHA_PROPERTY).setWidth(113);
        documentosGrid.getColumn(DIAHOY_PROPERTY).setWidth(60).setHidable(true).setHidden(true);
        documentosGrid.getColumn(PROVEEDOR_PROPERTY).setExpandRatio(1);
        documentosGrid.getColumn(TIPODOCUMENTO_PROPERTY).setExpandRatio(1).setHidable(true);
        documentosGrid.getColumn(DOCUMENTO_PROPERTY).setExpandRatio(1);
        documentosGrid.getColumn(VALOR_PROPERTY).setWidth(118);
        documentosGrid.getColumn(TIPOCAMBIO_PROPERTY).setExpandRatio(1).setHidable(true).setHidden(true);
        documentosGrid.getColumn(MONTO_QUETZALES_PROPERTY).setWidth(118);
        documentosGrid.getColumn(ESTATUS_PROPERTY).setWidth(105);
        documentosGrid.getColumn(IMAGEN_PROPERTY).setWidth(100);
        documentosGrid.getColumn(CODIGO_PROPERTY).setExpandRatio(1);
        documentosGrid.getColumn(NIT_PROVEEDOR_PROPERTY).setExpandRatio(1);
        documentosGrid.getColumn(USUARIO_PROPERTY).setExpandRatio(1);
        documentosGrid.getColumn(IMAGEN_PROPERTY).setHidable(true).setHidden(true);
        documentosGrid.getColumn(ARCHIVO_PROPERTY).setHidable(true).setHidden(true);
        documentosGrid.getColumn(ARCHIVO_TIPO_PROPERTY).setHidable(true).setHidden(true);
        documentosGrid.getColumn(VALORSF_PROPERTY).setHidable(true).setHidden(true);
        documentosGrid.getColumn(MONTOQSF_PROPERTY).setHidable(true).setHidden(true);
        documentosGrid.getColumn(RETISR_PROPERTY).setHidable(true).setHidden(true);
        documentosGrid.getColumn(ID_NOMENCLATURA_PROPERTY).setHidable(true).setHidden(true);
        documentosGrid.getColumn(IDCENTROCOSTO_PROPERTY).setHidable(true).setHidden(true);

        documentosGrid.setCellStyleGenerator(
                (Grid.CellReference cellReference) -> {

                    if (DIAHOY_PROPERTY.equals(cellReference.getPropertyId())) {
                        return "centeralign";
                    } else if (MONTO_QUETZALES_PROPERTY.equals(cellReference.getPropertyId())) {
                        return "rightalign";
                    } else if (VALOR_PROPERTY.equals(cellReference.getPropertyId())) {
                        return "rightalign";
                    } else if (TIPOCAMBIO_PROPERTY.equals(cellReference.getPropertyId())) {
                        return "centeralign";
                    } else {
                        return null;
                    }

                }
        );

        documentosGrid.addSelectionListener(new SelectionListener() {
            @Override
            public void select(SelectionEvent event
            ) {
                if (documentosGrid.getSelectedRow() != null) {
                    llenarTablaPartida(String.valueOf(documentosGrid.getContainerDataSource().getItem(documentosGrid.getSelectedRow()).getItemProperty(NIT_PROVEEDOR_PROPERTY).getValue()),
                            String.valueOf(documentosGrid.getContainerDataSource().getItem(documentosGrid.getSelectedRow()).getItemProperty(DOCUMENTO_PROPERTY).getValue()).split(" ")[0],
                            String.valueOf(documentosGrid.getContainerDataSource().getItem(documentosGrid.getSelectedRow()).getItemProperty(DOCUMENTO_PROPERTY).getValue()).split(" ")[1],
                            String.valueOf(documentosGrid.getContainerDataSource().getItem(documentosGrid.getSelectedRow()).getItemProperty(PROVEEDOR_PROPERTY).getValue()),
                            String.valueOf(documentosGrid.getContainerDataSource().getItem(documentosGrid.getSelectedRow()).getItemProperty(CODIGO_PROPERTY).getValue()));
                    llenarTablaCC(String.valueOf(documentosGrid.getContainerDataSource().getItem(documentosGrid.getSelectedRow()).getItemProperty(NIT_PROVEEDOR_PROPERTY).getValue()),
                            String.valueOf(documentosGrid.getContainerDataSource().getItem(documentosGrid.getSelectedRow()).getItemProperty(DOCUMENTO_PROPERTY).getValue()).split(" ")[0],
                            String.valueOf(documentosGrid.getContainerDataSource().getItem(documentosGrid.getSelectedRow()).getItemProperty(DOCUMENTO_PROPERTY).getValue()).split(" ")[1],
                            String.valueOf(documentosGrid.getContainerDataSource().getItem(documentosGrid.getSelectedRow()).getItemProperty(FECHA_PROPERTY).getValue()),
                            String.valueOf(documentosGrid.getContainerDataSource().getItem(documentosGrid.getSelectedRow()).getItemProperty(CODIGOCC_PROPERTY).getValue()));

                    revisadoBtn.setEnabled(false);
                    editBtn.setEnabled(false);
                    //isrBtn.setEnabled(false);
                    notaCreditoBtn.setEnabled(false);
                    anularBtn.setEnabled(false);

                    switch (String.valueOf(documentosGrid.getContainerDataSource().getItem(documentosGrid.getSelectedRow()).getItemProperty(ESTATUS_PROPERTY).getValue())) {
                        case "INGRESADO":
                            if (!((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserProfileName().equals("AUXILIAR")) {
                                revisadoBtn.setEnabled(true);
                            }
                            editBtn.setEnabled(true);
                            //isrBtn.setEnabled(true);
                            notaCreditoBtn.setEnabled(true);
                            anularBtn.setEnabled(true);
                            break;
                        case "REVISADO":
                            if (!((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserProfileName().equals("AUXILIAR")) {
                                editBtn.setEnabled(true);
                            }
                            //isrBtn.setEnabled(true);
                            notaCreditoBtn.setEnabled(true);
                            anularBtn.setEnabled(true);
                            break;
                    }
                    if(Double.valueOf(String.valueOf(documentosGrid.getContainerDataSource().getItem(documentosGrid.getSelectedRow()).getItemProperty(VALORSF_PROPERTY).getValue())) > 0) {
                        notaCreditoBtn.setEnabled(true);
                    }
                    if(   String.valueOf(documentosGrid.getContainerDataSource().getItem(documentosGrid.getSelectedRow()).getItemProperty(RETISR_PROPERTY).getValue()).trim().equals("NO")
                       || String.valueOf(documentosGrid.getContainerDataSource().getItem(documentosGrid.getSelectedRow()).getItemProperty(RETISR_PROPERTY).getValue()).trim().equals("")) {
                       // isrBtn.setEnabled(false);
                    }

                }
            }
        });
        documentosGrid.getColumn(ELIMINAR_PROPERTY).setRenderer(new ButtonRenderer(e
                -> eliminarRegistroTabla(e)));

        HeaderRow filterRow = documentosGrid.appendHeaderRow();

        HeaderCell cell0 = filterRow.getCell(TIPODOCUMENTO_PROPERTY);

        TextField filterField0 = new TextField();
        filterField0.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField0.setInputPrompt("Filtrar");
        filterField0.setColumns(8);

        filterField0.addTextChangeListener(change -> {
            documentsContainer.removeContainerFilters(TIPODOCUMENTO_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                documentsContainer.addContainerFilter(
                        new SimpleStringFilter(TIPODOCUMENTO_PROPERTY,
                                change.getText(), true, false));
            }
            setTotal();
        });

        cell0.setComponent(filterField0);

        HeaderCell cell = filterRow.getCell(DOCUMENTO_PROPERTY);

        TextField filterField = new TextField();
        filterField.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField.setInputPrompt("Filtrar");
        filterField.setColumns(8);

        filterField.addTextChangeListener(change -> {
            documentsContainer.removeContainerFilters(DOCUMENTO_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                documentsContainer.addContainerFilter(
                        new SimpleStringFilter(DOCUMENTO_PROPERTY,
                                change.getText(), true, false));
            }
            setTotal();
        });

        cell.setComponent(filterField);

        HeaderCell cell2 = filterRow.getCell(PROVEEDOR_PROPERTY);
        TextField filterField2 = new TextField();
        filterField2.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField2.setInputPrompt("Filtrar");
        filterField2.setColumns(20);

        filterField2.addTextChangeListener(change -> {
            documentsContainer.removeContainerFilters(PROVEEDOR_PROPERTY);
            if (!change.getText().isEmpty()) {
                documentsContainer.addContainerFilter(
                        new SimpleStringFilter(PROVEEDOR_PROPERTY,
                                change.getText(), true, false));
            }
            setTotal();
        });
        cell2.setComponent(filterField2);

        HeaderCell cell3 = filterRow.getCell(ESTATUS_PROPERTY);
        TextField filterField3 = new TextField();
        filterField3.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField3.setInputPrompt("Filtrar");
        filterField3.setColumns(20);

        filterField3.addTextChangeListener(change -> {
            documentsContainer.removeContainerFilters(ESTATUS_PROPERTY);
            if (!change.getText().isEmpty()) {
                documentsContainer.addContainerFilter(
                        new SimpleStringFilter(ESTATUS_PROPERTY,
                                change.getText(), true, false));
            }
            setTotal();
        });
        cell3.setComponent(filterField3);

        footerFacturas = documentosGrid.appendFooterRow();

        footerFacturas.getCell(DOCUMENTO_PROPERTY).setText("Total");
        footerFacturas.getCell(MONTO_QUETZALES_PROPERTY).setText("0.00");
        footerFacturas.getCell(MONTO_QUETZALES_PROPERTY).setStyleName("rightalign");

        nuevaFacturaBtn = new Button("Nuevo");
        nuevaFacturaBtn.setIcon(FontAwesome.PLUS);
        nuevaFacturaBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        nuevaFacturaBtn.setDescription("Agregar nuevo documento.");
        nuevaFacturaBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                IngresoDocumentosForm newDocument = new IngresoDocumentosForm();
                newDocument.llenarComboOrdenCompra();
                UI.getCurrent().addWindow(newDocument);
                newDocument.center();

            }
        });

        Button generarPDFHoy = new Button("PDF hoy");
        generarPDFHoy.setIcon(FontAwesome.FILE_PDF_O);
        generarPDFHoy.addStyleName(ValoTheme.BUTTON_PRIMARY);

        generarPDFHoy.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                llenarTablaFactura(empresaId, 1);
                if (documentsContainer.size() > 0) {
                    IngresoDocumentosPDF ingresoDocumentosPDF
                            = new IngresoDocumentosPDF(
                            empresaId,
                            ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName(),
                            getEmpresaNit(),
                            documentsContainer,
                            Utileria.getFechaDDMMYYYY(new java.util.Date()),
                            Utileria.getFechaDDMMYYYY(new java.util.Date())
                    );
                    mainUI.addWindow(ingresoDocumentosPDF);
                    ingresoDocumentosPDF.center();
                } else {
                    Notification notif = new Notification("La vista no contiene registros disponibles..",
                            Notification.Type.WARNING_MESSAGE);
                    notif.setDelayMsec(1500);
                    notif.setPosition(Position.MIDDLE_CENTER);
                    notif.setIcon(FontAwesome.WARNING);
                    notif.show(Page.getCurrent());
                }
            }
        });

        Button generarPDF = new Button("PDF general");
        generarPDF.setIcon(FontAwesome.FILE_PDF_O);
        generarPDF.addStyleName(ValoTheme.BUTTON_PRIMARY);

        generarPDF.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (documentsContainer.size() > 0) {
                    IngresoDocumentosPDF ingresoDocumentosPDF
                            = new IngresoDocumentosPDF(
                            empresaId,
                            ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName(),
                            getEmpresaNit(),
                            documentsContainer,
                            Utileria.getFechaDDMMYYYY(inicioDt.getValue()),
                            Utileria.getFechaDDMMYYYY(finDt.getValue())
                    );
                    mainUI.addWindow(ingresoDocumentosPDF);
                    ingresoDocumentosPDF.center();
                } else {
                    Notification notif = new Notification("La vista no contiene registros disponibles..",
                            Notification.Type.WARNING_MESSAGE);
                    notif.setDelayMsec(1500);
                    notif.setPosition(Position.MIDDLE_CENTER);
                    notif.setIcon(FontAwesome.WARNING);
                    notif.show(Page.getCurrent());
                }
            }
        });

        Button generarExcel = new Button("Excel");
        generarExcel.setIcon(FontAwesome.FILE_EXCEL_O);
        generarExcel.addStyleName(ValoTheme.BUTTON_PRIMARY);

        generarExcel.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (documentsContainer.size() > 0) {
                    exportToExcel();
                } else {
                    Notification notif = new Notification("La vista no contiene registros disponibles..",
                            Notification.Type.WARNING_MESSAGE);
                    notif.setDelayMsec(1500);
                    notif.setPosition(Position.MIDDLE_CENTER);
                    notif.setIcon(FontAwesome.WARNING);
                    notif.show(Page.getCurrent());
                }
            }
        });

        filtrosLayout.addComponent(inicioDt);
        filtrosLayout.setComponentAlignment(inicioDt, Alignment.BOTTOM_CENTER);
        filtrosLayout.addComponent(finDt);
        filtrosLayout.setComponentAlignment(finDt, Alignment.BOTTOM_CENTER);
        filtrosLayout.addComponent(documentTxt);
        filtrosLayout.setComponentAlignment(documentTxt, Alignment.BOTTOM_CENTER);
        filtrosLayout.addComponent(consultarBtn);
        filtrosLayout.setComponentAlignment(consultarBtn, Alignment.BOTTOM_CENTER);
        filtrosLayout.addComponent(pendienteIsr);
        filtrosLayout.setComponentAlignment(pendienteIsr, Alignment.BOTTOM_CENTER);
        filtrosLayout.addComponent(nuevaFacturaBtn);
        filtrosLayout.setComponentAlignment(nuevaFacturaBtn, Alignment.BOTTOM_CENTER);
        filtrosLayout.addComponent(generarPDF);
        filtrosLayout.setComponentAlignment(generarPDF, Alignment.BOTTOM_CENTER);
        filtrosLayout.addComponent(generarPDFHoy);
        filtrosLayout.setComponentAlignment(generarPDFHoy, Alignment.BOTTOM_CENTER);
        filtrosLayout.addComponent(generarExcel);
        filtrosLayout.setComponentAlignment(generarExcel, Alignment.BOTTOM_CENTER);
        reportLayout.addComponent(filtrosLayout);
        reportLayout.setComponentAlignment(filtrosLayout, Alignment.TOP_CENTER);

        layoutGrid.addComponent(documentosGrid);
        layoutGrid.setComponentAlignment(documentosGrid, Alignment.TOP_CENTER);
        reportLayout.addComponent(layoutGrid);
        addComponent(reportLayout);

        setComponentAlignment(reportLayout, Alignment.MIDDLE_CENTER);
    }

    public void createTablaPartidaYCuentaCorriente() {

        VerticalLayout detalleLayout = new VerticalLayout();
        detalleLayout.setWidth("100%");
        detalleLayout.addStyleName("rcorners2");
        detalleLayout.setSpacing(true);

        HorizontalLayout facturasYPartidasLayout = new HorizontalLayout();
        facturasYPartidasLayout.setWidth("100%");
        facturasYPartidasLayout.addStyleName("rcorners3");
        facturasYPartidasLayout.setSpacing(true);

        HorizontalLayout botonesLayout = new HorizontalLayout();
        botonesLayout.setSpacing(true);
        botonesLayout.setWidth("100%");

        detalleLayout.addComponents(facturasYPartidasLayout, botonesLayout);

        documentsContainerPartida.addContainerProperty(ID_PARTIDA_PROPERTY, String.class, null);
        documentsContainerPartida.addContainerProperty(PARTIDA_PROPERTY, String.class, null);
        documentsContainerPartida.addContainerProperty(CUENTA_PROPERTY, String.class, null);
        documentsContainerPartida.addContainerProperty(DESCRIPCION_PARTIDA_PROPERTY, String.class, null);
        documentsContainerPartida.addContainerProperty(DEBE_PROPERTY, String.class, null);
        documentsContainerPartida.addContainerProperty(HABER_PROPERTY, String.class, null);
        documentsContainerPartida.addContainerProperty(CODIGOCC_PROPERTY, String.class, null);

        partidaDocumentosGrid = new Grid("Partida contable", documentsContainerPartida);
        partidaDocumentosGrid.setImmediate(true);
        partidaDocumentosGrid.setSelectionMode(Grid.SelectionMode.NONE);
        partidaDocumentosGrid.setHeightMode(HeightMode.ROW);
        partidaDocumentosGrid.setHeightByRows(3);
        partidaDocumentosGrid.setWidth("100%");
        partidaDocumentosGrid.setResponsive(true);
        partidaDocumentosGrid.setEditorBuffered(false);

        partidaDocumentosGrid.getColumn(PARTIDA_PROPERTY).setHidable(true).setHidden(true);
        partidaDocumentosGrid.getColumn(ID_PARTIDA_PROPERTY).setHidable(true).setHidden(true);

        partidaDocumentosGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {

//            System.out.println("celda=" + cellReference.getProperty());
            if (DEBE_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (HABER_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }

        });

        footer = partidaDocumentosGrid.appendFooterRow();
        footer.getCell(DESCRIPCION_PROPERTY).setText("SUMAS : ");
        footer.getCell(DEBE_PROPERTY).setText("0.00");
        footer.getCell(HABER_PROPERTY).setText("0.00");
        footer.getCell(DEBE_PROPERTY).setStyleName("rightalign");
        footer.getCell(HABER_PROPERTY).setStyleName("rightalign");

        cuentaCorrienteContainer.addContainerProperty(FECHA_PROPERTY, String.class, null);
        cuentaCorrienteContainer.addContainerProperty(DOCUMENTO_PROPERTY, String.class, null);
        cuentaCorrienteContainer.addContainerProperty(TIPODOCUMENTO_PROPERTY, String.class, null);
        cuentaCorrienteContainer.addContainerProperty(PARTIDA_PROPERTY, String.class, null);
        cuentaCorrienteContainer.addContainerProperty(DEBE_PROPERTY, String.class, null);
        cuentaCorrienteContainer.addContainerProperty(HABER_PROPERTY, String.class, null);
        cuentaCorrienteContainer.addContainerProperty(CODIGOCC_PROPERTY, String.class, null);

        cuentaCorrientGrid = new Grid("Cuenta Corriente", cuentaCorrienteContainer);
        cuentaCorrientGrid.setImmediate(true);
        cuentaCorrientGrid.setSelectionMode(Grid.SelectionMode.NONE);
        cuentaCorrientGrid.setHeightMode(HeightMode.ROW);
        cuentaCorrientGrid.setHeightByRows(3);
        cuentaCorrientGrid.setWidth("100%");
        cuentaCorrientGrid.setResponsive(true);
        cuentaCorrientGrid.setEditorBuffered(false);

        cuentaCorrientGrid.getColumn(PARTIDA_PROPERTY).setHidable(true).setHidden(true);
        cuentaCorrientGrid.getColumn(CODIGOCC_PROPERTY).setHidable(true).setHidden(true);

        cuentaCorrientGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {

//            System.out.println("celda=" + cellReference.getProperty());
            if (DEBE_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (HABER_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }

        });

        footerCC = cuentaCorrientGrid.appendFooterRow();
        footerCC.getCell(TIPODOCUMENTO_PROPERTY).setText("SUMAS : ");
        footerCC.getCell(DEBE_PROPERTY).setText("0.00");
        footerCC.getCell(HABER_PROPERTY).setText("0.00");
        footerCC.getCell(DEBE_PROPERTY).setStyleName("rightalign");
        footerCC.getCell(HABER_PROPERTY).setStyleName("rightalign");

        facturasYPartidasLayout.addComponent(partidaDocumentosGrid);
        facturasYPartidasLayout.addComponent(cuentaCorrientGrid);

        editBtn = new Button("EDITAR");
        editBtn.setIcon(FontAwesome.EDIT);
        editBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        editBtn.setDescription("Actualizar datos");
        editBtn.addListener((Button.ClickListener) (Button.ClickEvent event) -> {

            if (documentosGrid.getSelectedRow() == null) {
                Notification notif = new Notification("Por favor, seleccione el registro correspondiente.",
                        Notification.Type.WARNING_MESSAGE);
                notif.setDelayMsec(1500);
                notif.setPosition(Position.MIDDLE_CENTER);
                notif.setIcon(FontAwesome.WARNING);
                notif.show(Page.getCurrent());
            } else {

                if(    documentsContainer.getContainerProperty(documentosGrid.getSelectedRow(), CODIGO_PROPERTY).getValue().equals("FORMULARIO IVA")
                    || documentsContainer.getContainerProperty(documentosGrid.getSelectedRow(), CODIGO_PROPERTY).getValue().equals("FORMULARIO ISR")
                    || documentsContainer.getContainerProperty(documentosGrid.getSelectedRow(), CODIGO_PROPERTY).getValue().equals("FORMULARIO IGSS")
                    || documentsContainer.getContainerProperty(documentosGrid.getSelectedRow(), CODIGO_PROPERTY).getValue().equals("FORMULARIO ISR RETENIDO")
                ) {
                    Notification.show("ESTE TIPO DE DOCUMENTO NO SE PUEDE EDITAR!!!", Notification.Type.WARNING_MESSAGE);
                    return;
                }

                try {
                    queryString = "UPDATE  contabilidad_partida";
                    queryString += " SET Estatus = 'INGRESADO'";
                    queryString += " WHERE CodigoPartida = '" + String.valueOf(documentsContainer.getContainerProperty(documentosGrid.getSelectedRow(), CODIGO_PROPERTY).getValue()) + "'";

                    stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                    stQuery.executeUpdate(queryString);

                    documentsContainer.getContainerProperty(documentosGrid.getSelectedRow(), ESTATUS_PROPERTY).setValue("INGRESADO");

                } catch (SQLException ex) {
                    System.out.println("Error al intentar modificar estatus a INGRESADO" + ex);
                    ex.printStackTrace();
                }

                if (String.valueOf(documentsContainer.getContainerProperty(documentosGrid.getSelectedRow(), TIPODOCUMENTO_PROPERTY).getValue()).equals("NOTA DE CREDITO COMPRA")) {

                    TransaccionesEspecialesForm nuevaTransaccion
                            = new TransaccionesEspecialesForm(
                            empresaId,
                            String.valueOf(documentsContainer.getContainerProperty(documentosGrid.getSelectedRow(), CODIGO_PROPERTY).getValue()),
                            "NOTA DE CREDITO COMPRA",
                            2

                    );

                    UI.getCurrent().addWindow(nuevaTransaccion);
                    nuevaTransaccion.center();

                } else if (String.valueOf(documentsContainer.getContainerProperty(documentosGrid.getSelectedRow(), TIPODOCUMENTO_PROPERTY).getValue()).equals("CONSTANCIA ISR COMPRA")) {
                    TransaccionesEspecialesForm nuevaTransaccion
                            = new TransaccionesEspecialesForm(
                            empresaId,
                            String.valueOf(documentsContainer.getContainerProperty(documentosGrid.getSelectedRow(), CODIGO_PROPERTY).getValue()),
                            "CONSTANCIA ISR COMPRA",
                            2
                    );

                    UI.getCurrent().addWindow(nuevaTransaccion);
                    nuevaTransaccion.center();
                } else {

                    EditarIngresoDocumentos editFacturasGasto
                            = new EditarIngresoDocumentos(
                            String.valueOf(documentsContainer.getContainerProperty(documentosGrid.getSelectedRow(), CODIGO_PROPERTY).getValue()),
                            String.valueOf(documentsContainer.getContainerProperty(documentosGrid.getSelectedRow(), DESCRIPCION_PROPERTY).getValue()));
                    editFacturasGasto.cuentaContable1Cbx.focus();
                    editFacturasGasto.llenarComboOrdenCompra();
                    UI.getCurrent().addWindow(editFacturasGasto);
                    editFacturasGasto.center();
                }
            }
        });

        exencionBtn = new Button("EXENCIÓN IVA");
        exencionBtn.setEnabled(true);
        exencionBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        exencionBtn.setDescription("RETENCION DE ISR A UNA FACTURA COMPRA");
        exencionBtn.addListener((Button.ClickListener) (Button.ClickEvent event) -> {

            if (documentosGrid.getSelectedRow() == null) {
                Notification notif = new Notification("Por favor, seleccione el registro correspondiente.",
                        Notification.Type.WARNING_MESSAGE);
                notif.setDelayMsec(1500);
                notif.setPosition(Position.MIDDLE_CENTER);
                notif.setIcon(FontAwesome.WARNING);
                notif.show(Page.getCurrent());
            } else {

                if (String.valueOf(documentsContainer.getContainerProperty(documentosGrid.getSelectedRow(), TIPODOCUMENTO_PROPERTY).getValue()).equals("FACTURA")) {

                    queryString = " SELECT * FROM contabilidad_partida ";
                    queryString += " WHERE CodigoCC = '" + String.valueOf(documentsContainer.getContainerProperty(documentosGrid.getSelectedRow(), CODIGO_PROPERTY).getValue()) + "'";
                    queryString += " AND IdNomenclatura IN (" + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getProveedores() + "," + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getAbastos() + ")";
                    queryString += " AND IdEmpresa =" + empresaId;
                    queryString += " AND TipoDocumento = 'EXENCIÓN IVA'";
                    queryString += " AND Estatus <> 'ANULADO' ";
//System.out.println(queryString);
                    try {

                        stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                        rsRecords = stQuery.executeQuery(queryString);

                        if (rsRecords.next()) {

                            Notification notif = new Notification("Esta factura ya tiene una Exención.",
                                    Notification.Type.WARNING_MESSAGE);
                            notif.setDelayMsec(1500);
                            notif.setPosition(Position.MIDDLE_CENTER);
                            notif.setIcon(FontAwesome.WARNING);
                            notif.show(Page.getCurrent());

                        } else {
                            ExencionIvaInfileForm exencionIvaForm
                                    = new ExencionIvaInfileForm(
                                    empresaId,
                                    String.valueOf(documentsContainer.getContainerProperty(documentosGrid.getSelectedRow(), CODIGO_PROPERTY).getValue()),
                                    String.valueOf(documentsContainer.getContainerProperty(documentosGrid.getSelectedRow(), TIPODOCUMENTO_PROPERTY).getValue())
                            );
                            UI.getCurrent().addWindow(exencionIvaForm);
                            exencionIvaForm.center();
                        }
                    } catch (Exception ex) {
                        System.out.println("QUERY: " + queryString + "\nError al momento de buscar si la factura ya tiene una retencion por isr");
                        ex.printStackTrace();
                    }
                }
                else {
                    Notification.show("ESTE DOCUMENTO NO ES UNA FACTURA!!!", Notification.Type.WARNING_MESSAGE);
                }
            }
        });

        isrBtn = new Button("CONSTANCIA ISR");
        isrBtn.setEnabled(true);
        isrBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        isrBtn.setDescription("RETENCION DE ISR A UNA FACTURA COMPRA");
        isrBtn.addListener((Button.ClickListener) (Button.ClickEvent event) -> {

            if (documentosGrid.getSelectedRow() == null) {
                Notification notif = new Notification("Por favor, seleccione el registro correspondiente.",
                        Notification.Type.WARNING_MESSAGE);
                notif.setDelayMsec(1500);
                notif.setPosition(Position.MIDDLE_CENTER);
                notif.setIcon(FontAwesome.WARNING);
                notif.show(Page.getCurrent());
            } else {

                if (String.valueOf(documentsContainer.getContainerProperty(documentosGrid.getSelectedRow(), TIPODOCUMENTO_PROPERTY).getValue()).equals("FACTURA")) {

                    queryString = " SELECT * FROM contabilidad_partida ";
                    queryString += " WHERE CodigoCC = '" + String.valueOf(documentsContainer.getContainerProperty(documentosGrid.getSelectedRow(), CODIGO_PROPERTY).getValue()) + "'";
                    queryString += " AND IdNomenclatura IN (" + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getProveedores() + "," + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getAbastos() + ")";
                    queryString += " AND IdEmpresa =" + empresaId;
                    queryString += " AND TipoDocumento = 'CONSTANCIA ISR COMPRA'";
//System.out.println(queryString);
                    try {

                        stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                        rsRecords = stQuery.executeQuery(queryString);

                        if (rsRecords.next()) {

                            Notification notif = new Notification("Esta factura ya tiene una retencion de ISR.",
                                    Notification.Type.WARNING_MESSAGE);
                            notif.setDelayMsec(1500);
                            notif.setPosition(Position.MIDDLE_CENTER);
                            notif.setIcon(FontAwesome.WARNING);
                            notif.show(Page.getCurrent());

                        } else {
                            TransaccionesEspecialesForm nuevaTransaccionIsr
                                    = new TransaccionesEspecialesForm(
                                    empresaId,
                                    String.valueOf(documentsContainer.getContainerProperty(documentosGrid.getSelectedRow(), CODIGO_PROPERTY).getValue()),
                                    "CONSTANCIA ISR COMPRA",
                                    1
                            );
                            UI.getCurrent().addWindow(nuevaTransaccionIsr);
                            nuevaTransaccionIsr.center();
                        }
                    } catch (Exception ex) {
                        System.out.println("Error al momento de buscar si la factura ya tiene una retencion por isr");
                    }
                }
                else {
                    Notification.show("ESTE DOCUMENTO NO ES UNA FACTURA!!!", Notification.Type.WARNING_MESSAGE);
                }
            }
        });

        notaCreditoBtn = new Button("NOTA DE CREDITO");
        notaCreditoBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        notaCreditoBtn.setDescription("NOTA DE CREDITO");
        notaCreditoBtn.addListener((Button.ClickListener) (Button.ClickEvent event) -> {

            if (documentosGrid.getSelectedRow() == null) {
                Notification notif = new Notification("Por favor, seleccione el registro correspondiente.",
                        Notification.Type.WARNING_MESSAGE);
                notif.setDelayMsec(1500);
                notif.setPosition(Position.MIDDLE_CENTER);
                notif.setIcon(FontAwesome.WARNING);
                notif.show(Page.getCurrent());
            } else {

                if (String.valueOf(documentsContainer.getContainerProperty(documentosGrid.getSelectedRow(), TIPODOCUMENTO_PROPERTY).getValue()).equals("FACTURA")) {
                    NotaCreditoCompra nuevaNotaCredito
                            = new NotaCreditoCompra(
                            empresaId,
                            documentsContainer,
                            documentosGrid.getSelectedRow(),
                            String.valueOf(documentsContainer.getContainerProperty(documentosGrid.getSelectedRow(), CODIGO_PROPERTY).getValue()),
                            String.valueOf(documentsContainer.getContainerProperty(documentosGrid.getSelectedRow(), DOCUMENTO_PROPERTY).getValue()).split(" ")[0],
                            String.valueOf(documentsContainer.getContainerProperty(documentosGrid.getSelectedRow(), DOCUMENTO_PROPERTY).getValue()).split(" ")[1]
                    );
                    UI.getCurrent().addWindow(nuevaNotaCredito);
                    nuevaNotaCredito.center();
                    nuevaNotaCredito.getSerieTxt().focus();
                }
                else {
                    Notification notif = new Notification("SOLO SE PERMITEN NOTAS DE CREDITO PARA FACTURAS.",
                            Notification.Type.WARNING_MESSAGE);
                    notif.setDelayMsec(1500);
                    notif.setPosition(Position.MIDDLE_CENTER);
                    notif.setIcon(FontAwesome.WARNING);
                    notif.show(Page.getCurrent());
                }
            }
        });

        revisadoBtn = new Button("REVISAR");
        revisadoBtn.setIcon(FontAwesome.CHECK);
        revisadoBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        revisadoBtn.setDescription("Actualizar estatus a REVISADO");
        revisadoBtn.addListener((Button.ClickListener) (Button.ClickEvent event) -> {

            if (documentosGrid.getSelectedRow() == null) {
                Notification notif = new Notification("Por favor, seleccione el registro correspondiente.",
                        Notification.Type.WARNING_MESSAGE);
                notif.setDelayMsec(1500);
                notif.setPosition(Position.MIDDLE_CENTER);
                notif.setIcon(FontAwesome.WARNING);
                notif.show(Page.getCurrent());
            } else {

                ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de cambiar el estatus a  REVISADO ?",
                        "SI", "NO", new ConfirmDialog.Listener() {

                            public void onClose(ConfirmDialog dialog) {
                                if (dialog.isConfirmed()) {
                                    try {

                                        queryString = "UPDATE  contabilidad_partida";
                                        queryString += " SET Estatus = 'REVISADO'";
                                        queryString += " WHERE CodigoPartida = '" + String.valueOf(documentsContainer.getContainerProperty(documentosGrid.getSelectedRow(), CODIGO_PROPERTY).getValue()) + "'";

                                        stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                                        stQuery.executeUpdate(queryString);

                                        llenarTablaFactura(empresaId, 0);

                                    } catch (SQLException ex) {
                                        System.out.println("Error al intentar modificar estatus a REVISADO" + ex);
                                        ex.printStackTrace();
                                    }
                                }
                            }
                        });
            }
        });

        anularBtn = new Button("ANULAR");
        anularBtn.setIcon(FontAwesome.ARCHIVE);
        anularBtn.addStyleName(ValoTheme.BUTTON_DANGER);
        anularBtn.setDescription("Actualizar estatus a ANULADO");
        anularBtn.addListener((Button.ClickListener) (Button.ClickEvent event) -> {

            if (documentosGrid.getSelectedRow() == null) {
                Notification notif = new Notification("Por favor, seleccione el registro correspondiente.",
                        Notification.Type.WARNING_MESSAGE);
                notif.setDelayMsec(1500);
                notif.setPosition(Position.MIDDLE_CENTER);
                notif.setIcon(FontAwesome.WARNING);
                notif.show(Page.getCurrent());
            } else {

                ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de cambiar el estatus a  ANULADO ?",
                        "SI", "NO", new ConfirmDialog.Listener() {

                            public void onClose(ConfirmDialog dialog) {
                                if (dialog.isConfirmed()) {
                                    try {

                                        queryString = "UPDATE  contabilidad_partida";
                                        queryString += " SET Estatus = 'ANULADO'";
                                        queryString += " WHERE CodigoPartida = '" + String.valueOf(documentsContainer.getContainerProperty(documentosGrid.getSelectedRow(), CODIGO_PROPERTY).getValue()) + "'";

                                        stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                                        stQuery.executeUpdate(queryString);

                                        llenarTablaFactura(empresaId, 0);

                                    } catch (SQLException ex) {
                                        System.out.println("Error al intentar modificar estatus a ANULADO" + ex);
                                        ex.printStackTrace();
                                    }
                                }
                            }
                        });
            }
        });

        botonesLayout.addComponent(editBtn);
        botonesLayout.setComponentAlignment(editBtn, Alignment.BOTTOM_LEFT);
        botonesLayout.addComponent(exencionBtn);
        botonesLayout.setComponentAlignment(exencionBtn, Alignment.BOTTOM_CENTER);
        botonesLayout.addComponent(isrBtn);
        botonesLayout.setComponentAlignment(isrBtn, Alignment.BOTTOM_CENTER);
        botonesLayout.addComponent(notaCreditoBtn);
        botonesLayout.setComponentAlignment(notaCreditoBtn, Alignment.BOTTOM_CENTER);
        botonesLayout.addComponent(revisadoBtn);
        botonesLayout.setComponentAlignment(revisadoBtn, Alignment.BOTTOM_RIGHT);
        botonesLayout.addComponent(anularBtn);
        botonesLayout.setComponentAlignment(revisadoBtn, Alignment.BOTTOM_RIGHT);

        addComponent(detalleLayout);
        setComponentAlignment(detalleLayout, Alignment.MIDDLE_CENTER);

    }

    public void llenarTablaPartida(String nit, String serie, String numero, String proveedor, String codigoPartida) {
        documentsContainerPartida.removeAllItems();
        reportLayoutPartida.setEnabled(true);

        if (partidaDocumentosGrid != null) {
            partidaDocumentosGrid.setCaption("Partida contable del documento : " + serie + " " + numero);
        }

        totalDebe = 0.00;
        totalHaber = 0.00;

        footer.getCell(DEBE_PROPERTY).setText(numberFormat.format(totalDebe));
        footer.getCell(HABER_PROPERTY).setText(numberFormat.format(totalHaber));

        queryString = " SELECT contabilidad_partida.IdPartida, contabilidad_partida.CodigoPartida, contabilidad_partida.MonedaDocumento,";
        queryString += " contabilidad_partida.Debe, contabilidad_partida.Haber, contabilidad_partida.CodigoCC,";
        queryString += " contabilidad_nomenclatura.N5, contabilidad_nomenclatura.NoCuenta, contabilidad_partida.IdNomenclatura ";
        queryString += " FROM contabilidad_partida,contabilidad_nomenclatura";
        queryString += " WHERE contabilidad_partida.CodigoPartida = '" + codigoPartida + "'";
        queryString += " AND contabilidad_partida.IdEmpresa = " + empresaId;
        queryString += " AND contabilidad_nomenclatura.IdNomenclatura = contabilidad_partida.IdNomenclatura";
        queryString += " ORDER BY Haber DESC";

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado
                do {

                    Object itemId = documentsContainerPartida.addItem();

                    documentsContainerPartida.getContainerProperty(itemId, ID_PARTIDA_PROPERTY).setValue(rsRecords.getString("IdPartida"));
                    documentsContainerPartida.getContainerProperty(itemId, PARTIDA_PROPERTY).setValue(rsRecords.getString("CodigoPartida"));
                    documentsContainerPartida.getContainerProperty(itemId, CUENTA_PROPERTY).setValue(rsRecords.getString("NoCuenta"));
                    documentsContainerPartida.getContainerProperty(itemId, DESCRIPCION_PARTIDA_PROPERTY).setValue(rsRecords.getString("N5"));
                    if (rsRecords.getDouble("Debe") > 0.00) {
                        if (rsRecords.getString("MonedaDocumento").equals("QUETZALES")) {
                            documentsContainerPartida.getContainerProperty(itemId, DEBE_PROPERTY).setValue("Q." + numberFormat.format(rsRecords.getDouble("Debe")));
                        } else {
                            documentsContainerPartida.getContainerProperty(itemId, DEBE_PROPERTY).setValue("$." + numberFormat.format(rsRecords.getDouble("Debe")));
                        }
                    } else {
                        documentsContainerPartida.getContainerProperty(itemId, DEBE_PROPERTY).setValue("");
                    }
                    if (rsRecords.getDouble("Haber") > 0.00) {
                        if (rsRecords.getString("MonedaDocumento").equals("QUETZALES")) {
                            documentsContainerPartida.getContainerProperty(itemId, HABER_PROPERTY).setValue("Q." + numberFormat.format(rsRecords.getDouble("Haber")));
                        } else {
                            documentsContainerPartida.getContainerProperty(itemId, HABER_PROPERTY).setValue("$." + numberFormat.format(rsRecords.getDouble("Haber")));
                        }

                    } else {
                        documentsContainerPartida.getContainerProperty(itemId, HABER_PROPERTY).setValue("");
                    }
                    documentsContainerPartida.getContainerProperty(itemId, CODIGOCC_PROPERTY).setValue(rsRecords.getString("CodigoCC"));

                    totalDebe = totalDebe + rsRecords.getDouble("Debe");
                    totalHaber = totalHaber + rsRecords.getDouble("Haber");

                } while (rsRecords.next());

                footer.getCell(DEBE_PROPERTY).setText(numberFormat.format(totalDebe));
                footer.getCell(HABER_PROPERTY).setText(numberFormat.format(totalHaber));

            }
        } catch (Exception ex) {
            System.out.println("Error al listar tabla PARTIDA:" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void llenarTablaCC(String nit, String serie, String numero, String fecha, String codigoCC) {
        cuentaCorrienteContainer.removeAllItems();
        reportLayoutPartida.setEnabled(true);

        if (cuentaCorrientGrid != null) {
            cuentaCorrientGrid.setCaption("Cuenta corriente del documento : " + serie + " " + numero);
        }

        totalDebe = 0.00;
        totalHaber = 0.00;

        footerCC.getCell(DEBE_PROPERTY).setText(numberFormat.format(totalDebe));
        footerCC.getCell(HABER_PROPERTY).setText(numberFormat.format(totalHaber));

        queryString = " SELECT contabilidad_partida.Fecha, contabilidad_partida.SerieDocumento, ";
        queryString += " contabilidad_partida.NumeroDocumento, contabilidad_partida.TipoDocumento, ";
        queryString += " contabilidad_partida.CodigoPartida, contabilidad_partida.MonedaDocumento, ";
        queryString += " contabilidad_partida.Debe, contabilidad_partida.Haber ";
        queryString += " FROM contabilidad_partida";
        queryString += " WHERE contabilidad_partida.CodigoCC = '" + codigoCC + "' ";
        queryString += " AND contabilidad_partida.IdNomenclatura In (" +  ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getProveedores() + "," + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getAnticiposProveedor() + "," +  ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getInstituciones() + "," + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getAbastos() + ") ";
        if(codigoCC.equals(empresaId + CODIGOCC_ABASTOS)) {
            queryString += " AND (contabilidad_partida.SerieDocumento = '" + serie + "' And NumeroDocumento = '" + numero + "') ";
            queryString += "  OR contabilidad_partida.TipoDocumento IN ('CHEQUE', 'NOTA DEBITO', 'TRANSFERENCIA', 'PAGO DOCUMENTO', 'CONSTANCIA ISR COMPRA') ";
        }
        queryString += " AND contabilidad_partida.Estatus <> 'ANULADO' ";
        queryString += " AND contabilidad_partida.IdEmpresa = " + empresaId + " ";
//        queryString += " and DATE_FORMAT(contabilidad_partida.Fecha,\"%d-%m-%Y\") >= '" + fecha + "'";
        queryString += " ORDER BY Haber DESC";
       
//System.out.println("QUERY llenarTablaCC Ingreso Documentos: " + queryString);

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado
                do {

                    Object itemId = cuentaCorrienteContainer.addItem();

                    cuentaCorrienteContainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue(rsRecords.getString("FECHA"));
                    cuentaCorrienteContainer.getContainerProperty(itemId, TIPODOCUMENTO_PROPERTY).setValue(rsRecords.getString("TipoDocumento"));
                    cuentaCorrienteContainer.getContainerProperty(itemId, DOCUMENTO_PROPERTY).setValue(rsRecords.getString("SerieDocumento") + " " + rsRecords.getString("NumeroDocumento"));
                    cuentaCorrienteContainer.getContainerProperty(itemId, PARTIDA_PROPERTY).setValue(rsRecords.getString("CodigoPartida"));
                    if (rsRecords.getDouble("Debe") > 0.00) {
                        if (rsRecords.getString("MonedaDocumento").equals("QUETZALES")) {
                            cuentaCorrienteContainer.getContainerProperty(itemId, DEBE_PROPERTY).setValue("Q." + numberFormat.format(rsRecords.getDouble("Debe")));
                        } else {
                            cuentaCorrienteContainer.getContainerProperty(itemId, DEBE_PROPERTY).setValue("$." + numberFormat.format(rsRecords.getDouble("Debe")));
                        }
                    } else {
                        cuentaCorrienteContainer.getContainerProperty(itemId, DEBE_PROPERTY).setValue("");
                    }
                    if (rsRecords.getDouble("Haber") > 0.00) {
                        if (rsRecords.getString("MonedaDocumento").equals("QUETZALES")) {
                            cuentaCorrienteContainer.getContainerProperty(itemId, HABER_PROPERTY).setValue("Q." + numberFormat.format(rsRecords.getDouble("Haber")));
                        } else {
                            cuentaCorrienteContainer.getContainerProperty(itemId, HABER_PROPERTY).setValue("$." + numberFormat.format(rsRecords.getDouble("Haber")));
                        }

                    } else {
                        cuentaCorrienteContainer.getContainerProperty(itemId, HABER_PROPERTY).setValue("");
                    }
                    cuentaCorrienteContainer.getContainerProperty(itemId, CODIGOCC_PROPERTY).setValue(codigoCC);

                    totalDebe = totalDebe + rsRecords.getDouble("Debe");
                    totalHaber = totalHaber + rsRecords.getDouble("Haber");

                    cuentaCorrientGrid.setCaption("Cuenta corriente del documento : " + serie + " " + numero + " Saldo = " + numberFormat.format((totalHaber - totalDebe)));

                } while (rsRecords.next());

                footerCC.getCell(DEBE_PROPERTY).setText(numberFormat.format(totalDebe));
                footerCC.getCell(HABER_PROPERTY).setText(numberFormat.format(totalHaber));

            }
        } catch (Exception ex) {
            System.out.println("Error al listar tabla PARTIDA:" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void llenarTablaFactura(String empresa, int pdfHoy) {

        if (empresa == null) {
            return;
        }

        documentsContainer.removeAllItems();
        documentsContainerPartida.removeAllItems();
        
        double montoQuet = 0.00;

        setTotal();


        try {
            if (inicioDt.getValue().before(finDt.getValue())) {

                queryString = " SELECT DATEDIFF(CURDATE(),contabilidad_partida.Fecha) AS DiasHoy, ";
                queryString += " contabilidad_partida.ArchivoNombre,contabilidad_partida.ArchivoTipo,";
                queryString += " contabilidad_partida.TipoDocumento, contabilidad_partida.Referencia, ";
                queryString += " contabilidad_partida.MontoDocumento, contabilidad_partida.IdNomenclatura, ";
                queryString += " SUM(contabilidad_partida.Haber) AS total, SUM(contabilidad_partida.HaberQuetzales) AS totalQ,";
                queryString += " contabilidad_partida.Fecha, contabilidad_partida.NombreProveedor, ";
                queryString += " contabilidad_partida.CodigoPartida, contabilidad_partida.CodigoCC, ";
                queryString += " contabilidad_partida.Debe,contabilidad_partida.Haber, ";
                queryString += " contabilidad_partida.DebeQuetzales,contabilidad_partida.HaberQuetzales, contabilidad_partida.TipoCambio, ";
                queryString += " contabilidad_partida.NitProveedor, contabilidad_partida.SerieDocumento, contabilidad_partida.NumeroDocumento, ";
                queryString += " contabilidad_partida.Descripcion, contabilidad_partida.MonedaDocumento, contabilidad_partida.Estatus,";
                queryString += " usuario.Nombre as NombreUsuario, contabilidad_partida.IDProveedor, orden_compra.NOC, contabilidad_partida.CodigoCentroCosto";
                queryString += " FROM contabilidad_partida ";
                queryString += " INNER JOIN usuario on usuario.IdUsuario = contabilidad_partida.CreadoUsuario";
                queryString += " INNER JOIN proveedor_empresa ON proveedor_empresa.IDProveedor = contabilidad_partida.IdProveedor";
                queryString += " LEFT JOIN orden_compra ON orden_compra.Id = contabilidad_partida.IdOrdenCompra";
                queryString += " WHERE UPPER(contabilidad_partida.TipoDocumento) IN ('FACTURA','RECIBO','RECIBO CONTABLE', ";
                queryString += " 'FORMULARIO ISR', 'FORMULARIO ISR RETENIDO', 'FORMULARIO ISO', 'FORMULARIO IVA','FORMULARIO IGSS',";
                queryString += " 'FORMULARIO RECTIFICACION', 'FORMULARIO ISR OPCIONAL MENSUAL', 'RECIBO CORRIENTE',";
                queryString += " 'NOTA DE CREDITO COMPRA', 'CONSTANCIA ISR COMPRA')";

                if (pdfHoy == 0 && documentTxt.getValue().trim().isEmpty()) {
                    queryString += " AND contabilidad_partida.Fecha BETWEEN ";
                    queryString += "     '" + Utileria.getFechaYYYYMMDD_1(inicioDt.getValue()) + "'";
                    queryString += " AND '" + Utileria.getFechaYYYYMMDD_1(finDt.getValue()) + "'";
                } else {
                    if (pdfHoy == 1 && documentTxt.getValue().trim().isEmpty()) {
                        queryString += " AND CreadoFechaYHora BETWEEN ";
                        queryString += "     '" + Utileria.getFechaYYYYMMDD_1(new java.util.Date()) + " 00:00:00'";
                        queryString += " AND '" + Utileria.getFechaYYYYMMDD_1(new java.util.Date()) + " 23:59:59'";
                    }
                }

                queryString += " AND contabilidad_partida.IdEmpresa = " + empresaId;
                queryString += " AND contabilidad_partida.IdLiquidacion = 0";
                if (!documentTxt.getValue().trim().isEmpty()) {
                    queryString += " AND contabilidad_partida.NumeroDocumento Like '%" + documentTxt.getValue().trim() + "%'";
                }
                if (vanderaIsr == 1) {
                    queryString += " AND contabilidad_partida.Referencia = 'SI'";  // Para listado de facturas pendientes de IRS
                }
                queryString += " AND proveedor_empresa.IdEmpresa = " + empresaId;
                queryString += " GROUP by contabilidad_partida.CodigoPartida ";
                queryString += " ORDER BY contabilidad_partida.Fecha, contabilidad_partida.IdNomenclatura DESC ";

System.out.println("Query busqueda FACTURAS/DOCUMENTO COMPRA/GASTO : " + queryString);

                stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                rsRecords = stQuery.executeQuery(queryString);

                if (rsRecords.next()) {

                    String monedaSimbolo;

                    do {
                        Object itemId = documentsContainer.addItem();
                        if(itemId == null) {
                            continue;
                        }

                        documentsContainer.getContainerProperty(itemId, NOC_PROPERTY).setValue("");
                        if (rsRecords.getObject("NOC") == null) {
                            documentsContainer.getContainerProperty(itemId, NOC_PROPERTY).setValue("");
                        } else {
                            documentsContainer.getContainerProperty(itemId, NOC_PROPERTY).setValue(rsRecords.getString("NOC"));
                        }
                
                        documentsContainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha")));
                        documentsContainer.getContainerProperty(itemId, DIAHOY_PROPERTY).setValue(rsRecords.getString("DiasHoy"));
                        documentsContainer.getContainerProperty(itemId, PROVEEDOR_PROPERTY).setValue(rsRecords.getString("NombreProveedor"));
                        documentsContainer.getContainerProperty(itemId, CODIGO_PROPERTY).setValue(rsRecords.getString("CodigoPartida"));
                        documentsContainer.getContainerProperty(itemId, NIT_PROVEEDOR_PROPERTY).setValue(rsRecords.getString("NITProveedor"));
                        documentsContainer.getContainerProperty(itemId, TIPODOCUMENTO_PROPERTY).setValue(rsRecords.getString("TipoDocumento"));
                        documentsContainer.getContainerProperty(itemId, DOCUMENTO_PROPERTY).setValue(rsRecords.getString("SerieDocumento") + " " + rsRecords.getString("NumeroDocumento"));
                        if (rsRecords.getString("MonedaDocumento").equals("QUETZALES")) {
                            monedaSimbolo = "Q.";
                        } else {
                            monedaSimbolo = "$.";
                        }
                        montoQuet  = rsRecords.getDouble("MontoDocumento") * rsRecords.getDouble  ("TipoCambio");
                        documentsContainer.getContainerProperty(itemId, VALOR_PROPERTY).setValue(monedaSimbolo + numberFormat.format(rsRecords.getDouble("MontoDocumento")));
                        documentsContainer.getContainerProperty(itemId, TIPOCAMBIO_PROPERTY).setValue(rsRecords.getString("TipoCambio"));                       
                        documentsContainer.getContainerProperty(itemId, MONTO_QUETZALES_PROPERTY).setValue("Q." + numberFormat.format(montoQuet));
                        documentsContainer.getContainerProperty(itemId, ESTATUS_PROPERTY).setValue(rsRecords.getString("Estatus"));
                        documentsContainer.getContainerProperty(itemId, USUARIO_PROPERTY).setValue(rsRecords.getString("NombreUsuario"));
                        documentsContainer.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(rsRecords.getString("Descripcion"));

                        if (rsRecords.getObject("ArchivoNombre") == null || rsRecords.getString("ArchivoNombre").trim().isEmpty()) {
                            documentsContainer.getContainerProperty(itemId, IMAGEN_PROPERTY).setValue("Cargar archivo");
                        } else {
                            documentsContainer.getContainerProperty(itemId, IMAGEN_PROPERTY).setValue("Visualizar");
                        }

                        documentsContainer.getContainerProperty(itemId, ELIMINAR_PROPERTY).setValue("Eliminar");
                        documentsContainer.getContainerProperty(itemId, ARCHIVO_PROPERTY).setValue(rsRecords.getString("ArchivoNombre"));
                        documentsContainer.getContainerProperty(itemId, ARCHIVO_TIPO_PROPERTY).setValue(rsRecords.getString("ArchivoTipo"));

                        documentsContainer.getContainerProperty(itemId, VALORSF_PROPERTY).setValue(rsRecords.getString("MontoDocumento"));
                        documentsContainer.getContainerProperty(itemId, MONTOQSF_PROPERTY).setValue(String.valueOf(montoQuet));
                        documentsContainer.getContainerProperty(itemId, CODIGOCC_PROPERTY).setValue(rsRecords.getString("CODIGOCC"));
                        documentsContainer.getContainerProperty(itemId,RETISR_PROPERTY).setValue(rsRecords.getString("Referencia"));
                        documentsContainer.getContainerProperty(itemId,ID_NOMENCLATURA_PROPERTY).setValue(rsRecords.getString("IdNomenclatura"));
                        documentsContainer.getContainerProperty(itemId,IDCENTROCOSTO_PROPERTY).setValue(rsRecords.getString("CodigoCentroCosto"));

                    } while (rsRecords.next());

                    documentosGrid.select(documentosGrid.getContainerDataSource().getIdByIndex(0));

                    setTotal();

                }
            } else {
                Notification notif = new Notification("La fecha hasta no puede contener un valor menor a la fecha de inicio.", Notification.Type.WARNING_MESSAGE);
                notif.setDelayMsec(1500);
                notif.setPosition(Position.MIDDLE_CENTER);
                notif.setIcon(FontAwesome.WARNING);
                notif.show(Page.getCurrent());
                inicioDt.focus();
            }
            vanderaIsr = 0;
        } catch (Exception ex) {
            System.out.println("Error al listar tabla Facturas IngresoDocumentosView:" + ex);
            ex.printStackTrace();
        }
    }

    public void VerCambiarImagen(ClickableRenderer.RendererClickEvent e) {

        Object selectedObject = e.getItemId();
        String codigoPartida = String.valueOf(documentsContainer.getContainerProperty(e.getItemId(), CODIGO_PROPERTY).getValue());
        String archivoNombre = String.valueOf(documentsContainer.getContainerProperty(e.getItemId(), ARCHIVO_PROPERTY).getValue());
        String archivoTipo = String.valueOf(documentsContainer.getContainerProperty(e.getItemId(), ARCHIVO_TIPO_PROPERTY).getValue());

        documentosGrid.select(e.getItemId());

        try {

            final byte docBytes[] = Files.readAllBytes(new File(archivoNombre).toPath());
            final String fileName = archivoNombre;

            if (docBytes == null) {
                Notification.show("Documento scan no disponible para visualizar!");

                return;
            }
            Window window = new Window();
            window.setResizable(true);
            window.setWidth("50%");
            window.setHeight("50%");
            window.center();

            StreamResource documentStreamResource = null;

            if (docBytes != null) {
                documentStreamResource = new StreamResource(
                        new StreamResource.StreamSource() {
                            public InputStream getStream() {
                                return new ByteArrayInputStream(docBytes);
                            }
                        }, fileName
                );
            }
            documentStreamResource.setMIMEType(archivoTipo);
            documentStreamResource.setFilename(archivoNombre);
            documentStreamResource.getStream().setParameter("Content-Disposition", "attachment; filename=" + fileName);

//            System.out.println("Tipo de Archivo : " + rsRecords.getString("ArchivoTipo"));
//            System.out.println("Archivo Nombre :" + rsRecords.getString("ArchivoNombre"));
//            System.out.println("Tamanio :" + rsRecords.getString("ArchivoPeso"));
            if (archivoTipo.contains("pdf")) {
                window.setWidth("98%");
                window.setHeight("98%");

                VerticalLayout pdfLayout = new VerticalLayout();
                pdfLayout.setSizeFull();
                pdfLayout.setSpacing(true);

                BrowserFrame browserFrame = new BrowserFrame();
                browserFrame.setSizeFull();
                browserFrame.setSource(documentStreamResource);

                pdfLayout.addComponent(browserFrame);

                UploadFinishedHandler handler;
                handler = new UploadFinishedHandler() {
                    @Override
                    public void handleFile(InputStream stream, String fileName, String mimeType, long length) {

                        File targetFile;

                        try {
                            if (mimeType.contains("png") || mimeType.contains("jpeg") || mimeType.contains("jpg") || mimeType.contains("pdf")) {
                                System.out.println("\nfileName=" + fileName);
                                System.out.println("length=" + stream.available());
                                System.out.println("mimeType=" + mimeType);

                                fileSize = stream.available();
                                byte[] buffer = new byte[stream.available()];
                                stream.read(buffer);

//                                String filePath = VaadinService.getCurrent()
//                                        .getBaseDirectory().getAbsolutePath() + "/projectfiles/" + ((SopdiUI) mainUI).sessionInformation.getStrCompanyId() + "/";
                                String filePath = enviromentsVars.getDtePath();

                                new File(filePath).mkdirs();

                                fileName = filePath + codigoPartida + fileName.substring(fileName.length() - 4, fileName.length());

                                new File(filePath).mkdirs();

                                targetFile = new File(fileName);
                                OutputStream outStream = new FileOutputStream(targetFile);
                                outStream.write(buffer);
                                outStream.close();

                                stream.close();

                                System.out.println("\ntargetFile = " + fileName);

                                logoStreamResource = null;

                                if (buffer != null) {
                                    logoStreamResource = new StreamResource(
                                            new StreamResource.StreamSource() {
                                                public InputStream getStream() {
                                                    return new ByteArrayInputStream(buffer);
                                                }
                                            }, String.valueOf(System.currentTimeMillis())
                                    );
                                }

                                recibirParametrosImagen(targetFile.getName(), mimeType, fileSize);

                                file = targetFile;

                                Notification.show("Archivo cargado con exito!!..", Notification.Type.TRAY_NOTIFICATION);

                                guardarArchivo(selectedObject, codigoPartida, fileName);
                                window.close();
                            } else {
                                Notification notif = new Notification("El archivo no contiene un formato compatible. solo puede subir archivos con formato 'PNG','JEPG','JPG','PDF'",
                                        Notification.Type.WARNING_MESSAGE);
                                notif.setDelayMsec(1500);
                                notif.setPosition(Position.MIDDLE_CENTER);
                                notif.setIcon(FontAwesome.WARNING);
                                notif.show(Page.getCurrent());
                                return;
                            }
                        } catch (java.io.IOException fIoEx) {
                            fIoEx.printStackTrace();
                            Notification.show("Error al cargar el archivo adjunto!", Notification.Type.ERROR_MESSAGE);
                            return;
                        }
                    }
                };

                UploadStateWindow window2 = new UploadStateWindow();

                singleUpload = new MultiFileUpload(handler, window2, false);
                singleUpload.setIcon(FontAwesome.UPLOAD);
                singleUpload.setImmediate(true);
                singleUpload.getSmartUpload().setUploadButtonCaptions("Cambiar archivo", "");

                JavaScript.getCurrent().execute("document.getElementsByClassName('gwt-FileUpload')[0].setAttribute('accept', '.png')");
                JavaScript.getCurrent().execute("document.getElementsByClassName('gwt-FileUpload')[1].setAttribute('accept', '.jpg')");
                JavaScript.getCurrent().execute("document.getElementsByClassName('gwt-FileUpload')[2].setAttribute('accept', '.PDF')");

                pdfLayout.addComponent(singleUpload);

                window.setContent(pdfLayout);

                pdfLayout.setExpandRatio(browserFrame, 2);

            } else {
                //window.setWidth("98%");
                //window.setHeight("98%");

                VerticalLayout imageLayout = new VerticalLayout();
                imageLayout.setSizeFull();
                //imageLayout.setSpacing(true);

                Image imageViewComponent = new Image(null, documentStreamResource);
                imageViewComponent.setImmediate(true);
                imageViewComponent.setSizeFull();
                imageViewComponent.addStyleName("my-img-button");
                imageViewComponent.setData(codigoPartida);

                imageLayout.addComponent(imageViewComponent);
                imageLayout.setComponentAlignment(imageViewComponent, Alignment.MIDDLE_CENTER);

                UploadFinishedHandler handler;
                handler = new UploadFinishedHandler() {
                    @Override
                    public void handleFile(InputStream stream, String fileName, String mimeType, long length) {

                        File targetFile;

                        try {
                            if (mimeType.contains("png") || mimeType.contains("jpeg") || mimeType.contains("jpg") || mimeType.contains("pdf")) {
                                System.out.println("\nfileName=" + fileName);
                                System.out.println("length=" + stream.available());
                                System.out.println("mimeType=" + mimeType);

                                fileSize = stream.available();
                                byte[] buffer = new byte[stream.available()];
                                stream.read(buffer);

//                                String filePath = VaadinService.getCurrent()
//                                        .getBaseDirectory().getAbsolutePath() + "/projectfiles/" + ((SopdiUI) mainUI).sessionInformation.getStrCompanyId() + "/";
                                String filePath = enviromentsVars.getDtePath();

                                new File(filePath).mkdirs();

                                fileName = filePath + codigoPartida + fileName.substring(fileName.length() - 4, fileName.length());
                                targetFile = new File(fileName);
                                OutputStream outStream = new FileOutputStream(targetFile);
                                outStream.write(buffer);
                                outStream.close();

                                stream.close();

                                System.out.println("\ntargetFile = " + fileName);

                                logoStreamResource = null;

                                if (buffer != null) {
                                    logoStreamResource = new StreamResource(
                                            new StreamResource.StreamSource() {
                                                public InputStream getStream() {
                                                    return new ByteArrayInputStream(buffer);
                                                }
                                            }, String.valueOf(System.currentTimeMillis())
                                    );
                                }

                                recibirParametrosImagen(targetFile.getName(), mimeType, fileSize);

                                file = targetFile;

                                Notification.show("Archivo cargado con exito!!..", Notification.Type.TRAY_NOTIFICATION);

                                guardarArchivo(selectedObject, codigoPartida, fileName);
                                window.close();
                            } else {
                                Notification notif1 = new Notification(
                                        "El archivo no contiene un formato compatible. solo puede subir archivos con formato 'PNG','JEPG','JPG','PDF'", Notification.Type.WARNING_MESSAGE);

                                notif1.setDelayMsec(30000);
                                notif1.setPosition(Position.MIDDLE_CENTER);
                                notif1.setIcon(FontAwesome.WARNING);
                                notif1.show(Page.getCurrent());
                                return;
                            }
                        } catch (java.io.IOException fIoEx) {
                            fIoEx.printStackTrace();
                            Notification.show("Error al cargar el archivo adjunto!", Notification.Type.ERROR_MESSAGE);
                            return;
                        }
                    }
                };

                UploadStateWindow window2 = new UploadStateWindow();

                singleUpload = new MultiFileUpload(handler, window2, false);
                singleUpload.setIcon(FontAwesome.UPLOAD);
                singleUpload.setImmediate(true);
                singleUpload.getSmartUpload().setUploadButtonCaptions("Cambiar archivo", "");

                JavaScript.getCurrent().execute("document.getElementsByClassName('gwt-FileUpload')[0].setAttribute('accept', '.png')");
                JavaScript.getCurrent().execute("document.getElementsByClassName('gwt-FileUpload')[1].setAttribute('accept', '.jpg')");
                JavaScript.getCurrent().execute("document.getElementsByClassName('gwt-FileUpload')[2].setAttribute('accept', '.PDF')");

                imageLayout.addComponent(singleUpload);
                imageLayout.setExpandRatio(imageViewComponent, 2);
                window.setContent(imageLayout);
            }

            archivoNombre = "";
            UI.getCurrent().addWindow(window);
            window.center();

        } catch (Exception allEx) {
            Notification.show("Error al intentar mostrar el archivo.", Notification.Type.ERROR_MESSAGE);
            allEx.printStackTrace();
        }

    }

    public void recibirParametrosImagen(String fileName, String mimeType, long peso) {
        parametro1 = fileName;
        parametro2 = mimeType;
        parametro3 = peso;
    }

    public void eliminarRegistroTabla(ClickableRenderer.RendererClickEvent e) {
        String codigoPartida = String.valueOf(documentsContainer.getContainerProperty(e.getItemId(), CODIGO_PROPERTY).getValue());
        String estatus = String.valueOf(documentsContainer.getContainerProperty(e.getItemId(), ESTATUS_PROPERTY).getValue());
        String perfil = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserProfile();
        String tipoDocumento = String.valueOf(documentsContainer.getContainerProperty(e.getItemId(), TIPODOCUMENTO_PROPERTY).getValue());

        if (estatus.equals("PAGADO")) {
            Notification.show("DOCUMENTO YA PAGADO, NO SE PUEDE ELIMINAR, CONSULTE A SU ADMINISTRADOR!!!", Notification.Type.WARNING_MESSAGE);
            return;
        }

        if (!perfil.equals("FINANCIERO") && !perfil.equals("ADMINISTRADOR")) {
            Notification.show("USUARIO NO TIENE PERMISO PARA ELIMINAR DOCUMENTO!!!, CONSULTE A SU ADMINISTRADOR...", Notification.Type.WARNING_MESSAGE);
            return;
        }

        if(cuentaCorrienteContainer.size() > 1) {
            Notification.show("ESTE DOCUMENTO TIENE OTROS DOCUMENTOS RELACIONADOS,  POR FAVOR ELIMINE ANTES LOS DOCUMENTOS RELACIONADOS!!", Notification.Type.WARNING_MESSAGE);
            return;
        }

        documentosGrid.select(e.getItemId());

        ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de eliminar el documento ?",
            "SI", "NO", new ConfirmDialog.Listener() {

            public void onClose(ConfirmDialog dialog) {
                if (dialog.isConfirmed()) {
                    try {

                        stQuery2 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();

                        //si TipoDocumento = FORMULARIO IVA volver los ivas disponibles para declarar...
                        if(tipoDocumento.equals("FORMULARIO IVA")) {
                            queryString =  "UPDATE contabilidad_partida ";
                            queryString += " SET PagadoIva = 'NO'";
                            queryString += " WHERE CodigoCC IN (SELECT A.CodigoCC FROM contabilidad_partida A";
                            queryString += "                    WHERE A.CodigoPartida = '" + codigoPartida + "'";
                            queryString += "                    AND   A.IdNomenclatura = " + ((SopdiUI)UI.getCurrent()).cuentasContablesDefault.getIvaPorCobrar();
                            queryString += "                    AND   A.IdEmpresa = " + empresaId+ ")";
                            queryString += " AND IdNomenclatura = " + ((SopdiUI)UI.getCurrent()).cuentasContablesDefault.getIvaPorCobrar();
                            queryString += " AND IdEmpresa = " + empresaId;

                            stQuery2.executeQuery(queryString);
                        }

                        queryString = " DELETE FROM contabilidad_partida";
                        queryString += " WHERE CodigoPartida = '" + codigoPartida + "'";
                        queryString += " AND IdEmpresa = " + empresaId;

                        stQuery2.executeQuery(queryString);

                        Notification.show("Registro eliminado exitosamente!.", Notification.Type.HUMANIZED_MESSAGE);

                        documentsContainer.removeItem(e.getItemId());

                    } catch (SQLException ex) {
                        System.out.println("Error al intentar eliminar documento : " + ex.getMessage());
                        Notification.show("ERROR AL INTENTAR ELIMINAR EL DOCUMENTO : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    public void guardarArchivo(Object selectedObject, String codigoPartida, String fileName) {
        try {
            queryString = " UPDATE contabilidad_partida SET  ";
            queryString += "  ArchivoNombre ='" + fileName + "'";
            queryString += ", ArchivoTipo ='" + parametro2 + "'";
            queryString += ", ArchivoPeso = " + parametro3;
            queryString += " WHERE CodigoPartida = '" + codigoPartida + "'";

            PreparedStatement stPreparedQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().prepareStatement(queryString);
            stPreparedQuery.executeUpdate();

            documentsContainer.getContainerProperty(selectedObject, IMAGEN_PROPERTY).setValue("Visualizar");
            documentsContainer.getContainerProperty(selectedObject, ARCHIVO_PROPERTY).setValue(fileName);
            documentsContainer.getContainerProperty(selectedObject, ARCHIVO_TIPO_PROPERTY).setValue(parametro2);

        } catch (Exception ex) {
            System.out.println("Error al intentar insertar Imagen" + ex);
        }
    }

    public String getEmpresaNit() {
        String strNit = "N/A";

        String queryString = " SELECT Nit FROM contabilidad_empresa ";
        queryString += " WHERE IdEmpresa = " + empresaId;

        try {
            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords2 = stQuery1.executeQuery(queryString);

            if (rsRecords2.next()) {
                strNit = rsRecords2.getString("Nit");
            }

        } catch (Exception ex1) {
            System.out.println("Error al buscar NIT de empresa: " + ex1.getMessage());
            ex1.printStackTrace();
        }

        return strNit;
    }

    public boolean exportToExcel() {
        if (documentosGrid.getHeightByRows() > 0) {
            TableHolder tableHolder = new DefaultTableHolder(documentosGrid);
            ExcelExport excelExport = new ExcelExport(tableHolder);
            excelExport.excludeCollapsedColumns();
            excelExport.setDisplayTotals(false);
            String fileexport;
// produccion            fileexport = (empresa + "_" + empresaLbl.getValue().substring(5, empresaLbl.getValue().length()).replaceAll(" ", "_").replaceAll(",", "_").replaceAll("[()]", "").replaceAll("[.]", "").replaceAll("ñ", "n").replaceAll("Ñ", "N").replaceAll("ó", "o").replaceAll("é","") + "_INTEGRACION_CAMBIOS.xlsx").replaceAll(" ", "").replaceAll(",", "");
            fileexport = (empresaId + "_" + ((SopdiUI)UI.getCurrent()).sessionInformation.getStrAccountingCompanyName().replaceAll(" ", "_").replaceAll(",", "_").replaceAll("[()]", "").replaceAll("[.]", "").replaceAll("ñ", "n").replaceAll("Ñ", "N").replaceAll("ó", "o").replaceAll("é", "") + "_DOCUMENTOS.xls").replaceAll(" ", "").replaceAll(",", "");
            excelExport.setExportFileName(fileexport);
            excelExport.export();
        }
        return true;
    }

    private void setTotal() {

        BigDecimal total = new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal totalQ = new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP);

        for (Object rid : documentosGrid.getContainerDataSource()
                .getItemIds()) {
            if (rid == null) {
                return;
            }
            if (documentsContainer.getContainerProperty(rid, VALORSF_PROPERTY).getValue() == null) {
                return;
            }
            total = total.add(new BigDecimal(
                    Double.parseDouble(
                            String.valueOf(documentsContainer.getContainerProperty(rid, VALORSF_PROPERTY).getValue())
                    )));
            totalQ = totalQ.add(new BigDecimal(
                    Double.parseDouble(
                            String.valueOf(documentsContainer.getContainerProperty(rid, MONTOQSF_PROPERTY).getValue())
                    )));
        }
        footerFacturas.getCell(VALOR_PROPERTY).setText(numberFormat.format(total));
        footerFacturas.getCell(MONTO_QUETZALES_PROPERTY).setText(numberFormat.format(totalQ));
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - Documentos");
    }
}