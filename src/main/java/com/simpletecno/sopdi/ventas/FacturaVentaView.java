package com.simpletecno.sopdi.ventas;

import com.simpletecno.sopdi.configuracion.EnvironmentVars;
import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.simpletecno.sopdi.compras.CargarArchivoIngresoDocumentos;
import com.simpletecno.sopdi.contabilidad.TransaccionesEspecialesISRForm;
import com.simpletecno.sopdi.contabilidad.TransaccionesEspecialesIVAForm;
import com.simpletecno.sopdi.extras.infile.InfileClient;
import com.vaadin.addon.tableexport.DefaultTableHolder;
import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.addon.tableexport.TableHolder;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.SelectionEvent;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.renderers.ClickableRenderer;
import com.vaadin.ui.themes.ValoTheme;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.MultiFileUpload;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadFinishedHandler;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadStateWindow;

import java.io.*;
import java.math.BigDecimal;
import java.net.ConnectException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.simpletecno.sopdi.compras.IngresoDocumentosPDF.stPreparedQuery;

public class FacturaVentaView extends VerticalLayout implements View {
    File pdfFile = null;

    double totalMonto, totalQueztales, totalHaber, totalDebe;

    StreamResource logoStreamResource = null;
    MultiFileUpload singleUpload;
    Image logoImage;
    String parametro1, parametro2;
    Long parametro3;
    long fileSize;
    public File file;

    public Grid facturasVentaGrid;
    public IndexedContainer container = new IndexedContainer();
    static final String ID_PROPERTY = "Id";
    static final String FECHA_PROPERTY = "Fecha";
    static final String DIAHOY_PROPERTY = "Días";
    static final String PROVEEDOR_PROPERTY = "Cliente";
    static final String ID_PROVEEDOR_PROPERTY = "Id Proveedor";
    static final String CODIGO_PROPERTY = "Código";
    static final String NIT_PROVEEDOR_PROPERTY = "NIT";
    static final String FACTURA_PROPERTY = "Factura";
    static final String UUID_PROPERTY = "UUID";
    static final String VALOR_PROPERTY = "Monto";
    static final String SALDO_PROPERTY = "Saldo";
    static final String TIPOCAMBIO_PROPERTY = "T_Cambio";
    static final String MONTO_QUETZALES_PROPERTY = "Monto Q.";
    static final String ESTATUS_PROPERTY = "Estatus";
    static final String USUARIO_PROPERTY = "Usuario";
    static final String DESCRIPCION_PROPERTY = "Descripción";
    static final String IMAGEN_PROPERTY = "Imagen";
    static final String ARCHIVO_PROPERTY = "Archivo";
    static final String ARCHIVO_TIPO_PROPERTY = "Tipo";
    static final String VALORSF_PROPERTY = "MSF";
    static final String MONTOQSF_PROPERTY = "MQSF";
    static final String CUOTA_PROPERTY = "CUOTA";
    Grid.FooterRow footerFacturaVenta;

    Grid partidaDocumentosGrid;
    public IndexedContainer containerPartida = new IndexedContainer();
    static final String ID_PARTIDA_PROPERTY = "Id";
    static final String CUENTA_PROPERTY = "Cuenta";
    static final String DESCRIPCION_PARTIDA_PROPERTY = "Descripción";
    static final String DEBE_PROPERTY = "Debe";
    static final String HABER_PROPERTY = "Haber";
    Grid.FooterRow footer;

    static final String TIPODOCUMENTO_PROPERTY = "TIPODOC";
    static final String DOCUMENTO_PROPERTY = "Documento";
    static final String PARTIDA_PROPERTY = "Partida";
    static final String CODIGOCC_PROPERTY = "CODIGOCC";

    Grid cuentaCorrientGrid;
    public IndexedContainer cuentaCorrienteContainer = new IndexedContainer();
    Grid.FooterRow footerCC;
    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");

    UI mainUI;
    Statement stQuery, stQuery1, stQuery2;
    ResultSet rsRecords, rsRecords1, rsRecords2;
    String queryString;

    ComboBox empresaCbx;
    String empresa;

    Button nuevaFacturaBtn, pendienteIsrBtn, consultarBtn;
    int vanderaIsr = 0;

    DateField inicioDt;
    DateField finDt;

    VerticalLayout reportLayoutPartida = new VerticalLayout();

    EnvironmentVars enviromentsVars;

    public FacturaVentaView() {
        this.mainUI = UI.getCurrent();
        setWidth("100%");
        setMargin(false);
        setSpacing(true);

        enviromentsVars = new EnvironmentVars();

        Label titleLbl = new Label("Facturas Venta");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h1_custom");

        empresaCbx = new ComboBox("Empresa:");
        empresaCbx.setWidth("95%");
        empresaCbx.addStyleName(ValoTheme.COMBOBOX_HUGE);
        empresaCbx.setInvalidAllowed(false);
        empresaCbx.setNewItemsAllowed(false);
        empresaCbx.setTextInputAllowed(false);
        empresaCbx.setNullSelectionAllowed(false);

        llenarComboEmpresa();

        empresaCbx.addValueChangeListener(event -> {
            empresa = String.valueOf(event.getProperty().getValue());
            llenarTablaFacturaVenta(empresa);
        });

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setResponsive(true);
        titleLayout.setSpacing(true);
        titleLayout.setWidth("100%");
        titleLayout.setMargin(false);
        titleLayout.addComponents(empresaCbx, titleLbl);
        titleLayout.setComponentAlignment(empresaCbx, Alignment.MIDDLE_CENTER);
        titleLayout.setComponentAlignment(titleLbl, Alignment.MIDDLE_CENTER);
        titleLayout.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

        addComponent(titleLayout);
        setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        crearTablaFacturasVenta();
        createTablaPartidaYCuentaCorriente();

        empresa = String.valueOf(empresaCbx.getValue());

        if (partidaDocumentosGrid != null) {
            llenarTablaFacturaVenta(empresa);
        }
        llenarTablaFacturaVenta(empresa);

    }

    public void llenarComboEmpresa() {
        queryString = " SELECT * from contabilidad_empresa";
        queryString += " Where IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();

        try {
            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords2 = stQuery1.executeQuery(queryString);

            while (rsRecords2.next()) { //  encontrado                
                empresaCbx.addItem(rsRecords2.getString("IdEmpresa"));
                empresaCbx.setItemCaption(rsRecords2.getString("IdEmpresa"), rsRecords2.getString("Empresa") + " REGIMEN : " + rsRecords2.getString("Regimen"));
            }
            rsRecords2.first();

            empresaCbx.select(rsRecords2.getString("IdEmpresa"));

        } catch (Exception ex1) {
            System.out.println("Error al llenar Combo empresas: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void crearTablaFacturasVenta() {
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

        consultarBtn = new Button("Consultar");
        consultarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        consultarBtn.setIcon(FontAwesome.SEARCH);
        consultarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                llenarTablaFacturaVenta(empresa);
            }
        });

        pendienteIsrBtn = new Button("Pendientes ISR");
        pendienteIsrBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        pendienteIsrBtn.setIcon(FontAwesome.SEARCH);
        pendienteIsrBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                vanderaIsr = 1; /// Si es igual a 1 busca las facturas PENDIENTES de isr
                llenarTablaFacturaVenta(String.valueOf(empresaCbx.getValue()));
            }
        });

        container.addContainerProperty(ID_PROPERTY, String.class, null);
        container.addContainerProperty(TIPODOCUMENTO_PROPERTY, String.class, "");
        container.addContainerProperty(FECHA_PROPERTY, String.class, null);
        container.addContainerProperty(DIAHOY_PROPERTY, String.class, null);
        container.addContainerProperty(PROVEEDOR_PROPERTY, String.class, null);
        container.addContainerProperty(ID_PROVEEDOR_PROPERTY, String.class, null);
        container.addContainerProperty(CODIGO_PROPERTY, String.class, null);
        container.addContainerProperty(NIT_PROVEEDOR_PROPERTY, String.class, null);
        container.addContainerProperty(FACTURA_PROPERTY, String.class, null);
        container.addContainerProperty(VALOR_PROPERTY, String.class, null);
        container.addContainerProperty(MONTO_QUETZALES_PROPERTY, String.class, null);
        container.addContainerProperty(SALDO_PROPERTY, String.class, null);
        container.addContainerProperty(TIPOCAMBIO_PROPERTY, String.class, null);
        container.addContainerProperty(ESTATUS_PROPERTY, String.class, null);
        container.addContainerProperty(DESCRIPCION_PROPERTY, String.class, null);
        container.addContainerProperty(USUARIO_PROPERTY, String.class, null);
        container.addContainerProperty(IMAGEN_PROPERTY, String.class, null);
        container.addContainerProperty(ARCHIVO_PROPERTY, String.class, null);
        container.addContainerProperty(ARCHIVO_TIPO_PROPERTY, String.class, null);
        container.addContainerProperty(VALORSF_PROPERTY, String.class, null);
        container.addContainerProperty(MONTOQSF_PROPERTY, String.class, null);
        container.addContainerProperty(CUOTA_PROPERTY, Integer.class, null);
        container.addContainerProperty(CODIGOCC_PROPERTY, String.class, null);
        container.addContainerProperty(UUID_PROPERTY, String.class, null);

        facturasVentaGrid = new Grid("", container);
        facturasVentaGrid.setWidth("100%");
        facturasVentaGrid.setImmediate(true);
        facturasVentaGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        facturasVentaGrid.setDescription("Seleccione un registro.");
        facturasVentaGrid.setHeightMode(HeightMode.ROW);
        facturasVentaGrid.setHeightByRows(5);
        facturasVentaGrid.setResponsive(true);
        facturasVentaGrid.setEditorBuffered(false);

        facturasVentaGrid.getColumn(IMAGEN_PROPERTY).setRenderer(new ButtonRenderer(e
                -> {

            if (container.getContainerProperty(e.getItemId(), IMAGEN_PROPERTY).getValue().equals("Cargar archivo")) {
                String codigoPartida = String.valueOf(container.getContainerProperty(e.getItemId(), CODIGO_PROPERTY).getValue());

                facturasVentaGrid.select(e.getItemId());

                CargarArchivoIngresoDocumentos cargarArchivo
                        = new CargarArchivoIngresoDocumentos(e.getItemId(), codigoPartida);
                UI.getCurrent().addWindow(cargarArchivo);
                cargarArchivo.center();

            } else {
                actualizarArchivo(e);
            }
        }));

        facturasVentaGrid.getColumn(ID_PROPERTY).setHidable(true).setHidden(true);
        facturasVentaGrid.getColumn(CODIGO_PROPERTY).setHidable(true).setHidden(true);
        facturasVentaGrid.getColumn(TIPODOCUMENTO_PROPERTY).setHidable(true).setHidden(false);
        facturasVentaGrid.getColumn(FECHA_PROPERTY).setHidable(true).setHidden(false);
        facturasVentaGrid.getColumn(ID_PROVEEDOR_PROPERTY).setHidable(true).setHidden(true);
        facturasVentaGrid.getColumn(NIT_PROVEEDOR_PROPERTY).setHidable(true).setHidden(true);
        facturasVentaGrid.getColumn(TIPOCAMBIO_PROPERTY).setHidable(true).setHidden(true);
        facturasVentaGrid.getColumn(USUARIO_PROPERTY).setHidable(true).setHidden(true);
        facturasVentaGrid.getColumn(DESCRIPCION_PROPERTY).setHidable(true).setHidden(true);
        facturasVentaGrid.getColumn(ARCHIVO_PROPERTY).setHidable(true).setHidden(true);
        facturasVentaGrid.getColumn(ARCHIVO_TIPO_PROPERTY).setHidable(true).setHidden(true);
        facturasVentaGrid.getColumn(VALORSF_PROPERTY).setHidable(true).setHidden(true);
        facturasVentaGrid.getColumn(MONTOQSF_PROPERTY).setHidable(true).setHidden(true);
        facturasVentaGrid.getColumn(CUOTA_PROPERTY).setHidable(true).setHidden(true);

        facturasVentaGrid.getColumn(FECHA_PROPERTY).setWidth(113);
        facturasVentaGrid.getColumn(DIAHOY_PROPERTY).setWidth(60);
        facturasVentaGrid.getColumn(VALOR_PROPERTY).setWidth(118);
        facturasVentaGrid.getColumn(SALDO_PROPERTY).setWidth(118);
        facturasVentaGrid.getColumn(MONTO_QUETZALES_PROPERTY).setWidth(118);
        facturasVentaGrid.getColumn(ESTATUS_PROPERTY).setWidth(118);
        facturasVentaGrid.getColumn(IMAGEN_PROPERTY).setWidth(100);

        facturasVentaGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if (DIAHOY_PROPERTY.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else if (MONTO_QUETZALES_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (VALOR_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (SALDO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (TIPOCAMBIO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else {
                return null;
            }
        });

        facturasVentaGrid.addSelectionListener(new SelectionEvent.SelectionListener() {
            @Override
            public void select(SelectionEvent event) {
                if (facturasVentaGrid.getSelectedRow() != null) {
                    llenarTablaPartida(String.valueOf(facturasVentaGrid.getContainerDataSource().getItem(facturasVentaGrid.getSelectedRow()).getItemProperty(NIT_PROVEEDOR_PROPERTY).getValue()),
                            String.valueOf(facturasVentaGrid.getContainerDataSource().getItem(facturasVentaGrid.getSelectedRow()).getItemProperty(FACTURA_PROPERTY).getValue()).split(" ")[0],
                            String.valueOf(facturasVentaGrid.getContainerDataSource().getItem(facturasVentaGrid.getSelectedRow()).getItemProperty(FACTURA_PROPERTY).getValue()).split(" ")[1],
                            String.valueOf(facturasVentaGrid.getContainerDataSource().getItem(facturasVentaGrid.getSelectedRow()).getItemProperty(PROVEEDOR_PROPERTY).getValue()),
                            String.valueOf(facturasVentaGrid.getContainerDataSource().getItem(facturasVentaGrid.getSelectedRow()).getItemProperty(CODIGO_PROPERTY).getValue()));
                    llenarTablaCC(String.valueOf(facturasVentaGrid.getContainerDataSource().getItem(facturasVentaGrid.getSelectedRow()).getItemProperty(NIT_PROVEEDOR_PROPERTY).getValue()),
                            String.valueOf(facturasVentaGrid.getContainerDataSource().getItem(facturasVentaGrid.getSelectedRow()).getItemProperty(FACTURA_PROPERTY).getValue()).split(" ")[0],
                            String.valueOf(facturasVentaGrid.getContainerDataSource().getItem(facturasVentaGrid.getSelectedRow()).getItemProperty(FACTURA_PROPERTY).getValue()).split(" ")[1],
                            String.valueOf(facturasVentaGrid.getContainerDataSource().getItem(facturasVentaGrid.getSelectedRow()).getItemProperty(PROVEEDOR_PROPERTY).getValue()),
                            String.valueOf(facturasVentaGrid.getContainerDataSource().getItem(facturasVentaGrid.getSelectedRow()).getItemProperty(CODIGOCC_PROPERTY).getValue()));
                }
            }
        });

//        facturasVentaGrid.getColumn(SALDO_PROPERTY).setRenderer(
//                new ButtonRenderer(e -> modificarCamposPagar(e)));

        Grid.HeaderRow filterRow = facturasVentaGrid.appendHeaderRow();
        Grid.HeaderCell cell = filterRow.getCell(FACTURA_PROPERTY);
        TextField filterField = new TextField();
        filterField.addStyleName(ValoTheme.TEXTFIELD_TINY);

        filterField.setInputPrompt("Filtrar por documento");
        filterField.setColumns(20);

        filterField.addTextChangeListener(change -> {
                    container.removeContainerFilters(FACTURA_PROPERTY);

                    // (Re)create the filter if necessary
                    if (!change.getText().isEmpty()) {
                        container.addContainerFilter(
                                new SimpleStringFilter(FACTURA_PROPERTY,
                                        change.getText(), true, false));
                    }
                    setTotal();
                }
        );
        cell.setComponent(filterField);

        Grid.HeaderCell cell2 = filterRow.getCell(PROVEEDOR_PROPERTY);
        TextField filterField2 = new TextField();
        filterField2.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField2.setInputPrompt("Filtrar por cliente");
        filterField2.setColumns(20);

        filterField2.addTextChangeListener(change -> {
                    container.removeContainerFilters(PROVEEDOR_PROPERTY);
                    if (!change.getText().isEmpty()) {
                        container.addContainerFilter(
                                new SimpleStringFilter(PROVEEDOR_PROPERTY,
                                        change.getText(), true, false));
                    }
                    setTotal();
                }
        );
        cell2.setComponent(filterField2);

        footerFacturaVenta = facturasVentaGrid.appendFooterRow();
        footerFacturaVenta.getCell(FACTURA_PROPERTY).setText("Total 0 facturas");
        //  footerFacturaVenta.getCell(VALOR_PROPERTY).setText("0.00");
        footerFacturaVenta.getCell(MONTO_QUETZALES_PROPERTY).setText("0.00");
        //footerFacturaVenta.getCell(VALOR_PROPERTY).setStyleName("rightalign");
        footerFacturaVenta.getCell(MONTO_QUETZALES_PROPERTY).setStyleName("rightalign");

        nuevaFacturaBtn = new Button("Nueva factura");
        nuevaFacturaBtn.setIcon(FontAwesome.PLUS);
        nuevaFacturaBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        nuevaFacturaBtn.setDescription("Agregar nueva factura de venta.");
        nuevaFacturaBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                FacturaVentaInfileForm newDocument = new FacturaVentaInfileForm();
                newDocument.empresaCbx.select(empresa);
                newDocument.llenarComboCliente();
                newDocument.empresaCbx.setReadOnly(true);
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
                llenarTablaFacturaVenta(empresa);
                if (container.size() > 0) {
                    FacturasVentaPDF facturasVentaPDF = new FacturasVentaPDF(
                            empresa,
                            empresaCbx.getItemCaption(empresaCbx.getValue()),
                            getEmpresaNit(),
                            container,
                            null,
                            null
                    );
                    mainUI.addWindow(facturasVentaPDF);
                    facturasVentaPDF.center();
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
                if (container.size() > 0) {
                    FacturasVentaPDF facturasVentaPDF
                            = new FacturasVentaPDF(
                            empresa,
                            empresaCbx.getItemCaption(empresaCbx.getValue()),
                            getEmpresaNit(),
                            container,
                            Utileria.getFechaDDMMYYYY(inicioDt.getValue()),
                            Utileria.getFechaDDMMYYYY(finDt.getValue())
                    );
                    mainUI.addWindow(facturasVentaPDF);
                    facturasVentaPDF.center();
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
                if (container.size() > 0) {
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
        filtrosLayout.addComponent(consultarBtn);
        filtrosLayout.setComponentAlignment(consultarBtn, Alignment.BOTTOM_CENTER);
        filtrosLayout.addComponent(pendienteIsrBtn);
        filtrosLayout.setComponentAlignment(pendienteIsrBtn, Alignment.BOTTOM_CENTER);
        filtrosLayout.addComponent(nuevaFacturaBtn);
        filtrosLayout.setComponentAlignment(nuevaFacturaBtn, Alignment.BOTTOM_CENTER);
        filtrosLayout.addComponent(nuevaFacturaBtn);
        filtrosLayout.setComponentAlignment(nuevaFacturaBtn, Alignment.BOTTOM_CENTER);
        filtrosLayout.addComponent(generarPDFHoy);
        filtrosLayout.setComponentAlignment(generarPDFHoy, Alignment.BOTTOM_CENTER);
        filtrosLayout.addComponent(generarPDF);
        filtrosLayout.setComponentAlignment(generarPDF, Alignment.BOTTOM_CENTER);
        filtrosLayout.addComponent(generarExcel);
        filtrosLayout.setComponentAlignment(generarExcel, Alignment.BOTTOM_CENTER);
        reportLayout.addComponent(filtrosLayout);
        reportLayout.setComponentAlignment(filtrosLayout, Alignment.TOP_CENTER);

        layoutGrid.addComponent(facturasVentaGrid);
        layoutGrid.setComponentAlignment(facturasVentaGrid, Alignment.TOP_CENTER);
        reportLayout.addComponent(layoutGrid);
        addComponent(reportLayout);

        setComponentAlignment(reportLayout, Alignment.MIDDLE_CENTER);
    }

    public void llenarTablaFacturaVenta(String empresa) {

        footer.getCell(DEBE_PROPERTY).setText("0.00");
        footer.getCell(HABER_PROPERTY).setText("0.00");

        footerFacturaVenta.getCell(MONTO_QUETZALES_PROPERTY).setText("0.00");

        container.removeAllItems();
        containerPartida.removeAllItems();

        facturasVentaGrid.getHeaderRow(0).getCell(FACTURA_PROPERTY).setText("");
        facturasVentaGrid.getHeaderRow(0).getCell(PROVEEDOR_PROPERTY).setText("");

        if (inicioDt.getValue().before(finDt.getValue())) {

            totalMonto = 0.00;
            totalQueztales = 0.00;
            String monedaSimbolo = "Q.";

            queryString = " SELECT *,";
            queryString += " DATEDIFF(CURDATE(),cp.Fecha) as DiasHoy, usuario.Nombre  as NombreUsuario ";
            queryString += " FROM contabilidad_partida cp ";
            queryString += " INNER JOIN usuario on usuario.IdUsuario = cp.CreadoUsuario ";
            queryString += " WHERE cp.Fecha between ";
            queryString += "     '" + Utileria.getFechaYYYYMMDD_1(inicioDt.getValue()) + "'";
            queryString += " AND '" + Utileria.getFechaYYYYMMDD_1(finDt.getValue()) + "'";
            queryString += " And cp.TipoDocumento IN ('FACTURA VENTA', 'RECIBO CONTABLE', 'RECIBO CONTABLE VENTA', 'NOTA DE CREDITO VENTA', 'NOTA DE DEBITO VENTA', 'EXENCIÓN IVA') ";
            queryString += " And cp.IdEmpresa = " + empresaCbx.getValue();
            queryString += " And cp.IdNomenclatura IN (" + ((SopdiUI) mainUI).cuentasContablesDefault.getClientes() + ", " +
                                                                             ((SopdiUI) mainUI).cuentasContablesDefault.getAnticiposClientes() + ", " +
                                                                             ((SopdiUI) mainUI).cuentasContablesDefault.getProveedores() + ", " +
                                                                             ((SopdiUI) mainUI).cuentasContablesDefault.getAnticiposProveedor() + ") ";
            if (vanderaIsr == 1) {
                queryString += " AND cp.Referencia = 'SI'";  // Para listado de facturas pendientes de IRS
            }
            queryString += " GROUP by cp.CodigoPartida ";

            try {

                double saldo = 0.00;
                double saldoQ = 0.00;

                stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                rsRecords = stQuery.executeQuery(queryString);

                if (rsRecords.next()) {
                    do {

                        saldo = rsRecords.getDouble("MontoDocumento");
                        saldoQ = rsRecords.getDouble("DebeQuetzales");

                        queryString = " SELECT SUM(contabilidad_partida.Debe) TOTALDEBE, SUM(contabilidad_partida.Haber) TOTALHABER,";
                        queryString += " SUM(contabilidad_partida.DebeQuetzales) TOTALDEBEQ, SUM(contabilidad_partida.HaberQuetzales) TOTALHABERQ,";
                        queryString += " SUM(DEBE - HABER) TOTALSALDO, SUM(DebeQuetzales - HaberQuetzales) TOTALSALDOQ, Estatus, TipoDocumento, EsCuota";
                        queryString += " FROM contabilidad_partida";
                        queryString += " WHERE contabilidad_partida.CodigoCC = '" + rsRecords.getString("CodigoCC") + "'";
                        queryString += " AND contabilidad_partida.IdNomenclatura in (" +  ((SopdiUI) mainUI).cuentasContablesDefault.getClientes() + ", " + ((SopdiUI) mainUI).cuentasContablesDefault.getAnticiposClientes() + ") ";
                        queryString += " GROUP BY TipoDocumento";
                        queryString += " ORDER BY TOTALSALDO DESC";

                        stQuery2 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                        rsRecords2 = stQuery2.executeQuery(queryString);

                        boolean recuento = true;
                        while (rsRecords2.next()) {
                            if(recuento) {
                                saldo = 0;
                                saldoQ = 0;
                                recuento = false;
                            }

                            String estatus =  rsRecords2.getString("Estatus");
                            String tipo = rsRecords2.getString("TipoDocumento");

                            if (tipo.equals(rsRecords.getString("TipoDocumento"))) {
                                saldo += Math.abs(rsRecords2.getDouble("TOTALSALDO"));
                                saldoQ += Math.abs(rsRecords2.getDouble("TOTALSALDOQ"));
                            }

                            if(!estatus.equals("ANULADO") && !tipo.equals("FACTURA VENTA") && rsRecords.getString("TipoDocumento").equals("FACTURA VENTA")){
                                saldo += rsRecords2.getDouble("TOTALSALDO");
                                //saldoQ += rsRecords2.getDouble("TOTALSALDOQ");
                            }
                            if(!estatus.equals("ANULADO") && !tipo.equals("RECIBO CONTABLE VENTA") && rsRecords.getString("TipoDocumento").equals("RECIBO CONTABLE VENTA")){
                                saldo += rsRecords2.getDouble("TOTALSALDO");
                                //saldoQ += rsRecords2.getDouble("TOTALSALDOQ");
                            }
                        }

                        Object itemId = container.addItem();

                        container.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords.getString("IdPartida"));
                        container.getContainerProperty(itemId, TIPODOCUMENTO_PROPERTY).setValue(rsRecords.getString("TipoDocumento"));
                        container.getContainerProperty(itemId, FECHA_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha")));
                        container.getContainerProperty(itemId, DIAHOY_PROPERTY).setValue(rsRecords.getString("DiasHoy"));
                        container.getContainerProperty(itemId, ID_PROVEEDOR_PROPERTY).setValue(rsRecords.getString("IdProveedor"));
                        container.getContainerProperty(itemId, PROVEEDOR_PROPERTY).setValue(rsRecords.getString("NombreProveedor"));
                        container.getContainerProperty(itemId, CODIGO_PROPERTY).setValue(rsRecords.getString("CodigoPartida"));
                        container.getContainerProperty(itemId, NIT_PROVEEDOR_PROPERTY).setValue(rsRecords.getString("NITProveedor"));
                        container.getContainerProperty(itemId, FACTURA_PROPERTY).setValue(rsRecords.getString("SerieDocumento") + " " + rsRecords.getString("NumeroDocumento"));
                        if (rsRecords.getString("MonedaDocumento").equals("QUETZALES")) {
                            monedaSimbolo = "Q.";
                        } else {
                            monedaSimbolo = "$.";
                        }
                        container.getContainerProperty(itemId, VALOR_PROPERTY).setValue(monedaSimbolo + numberFormat.format(rsRecords.getDouble("MontoDocumento")));
                        container.getContainerProperty(itemId, SALDO_PROPERTY).setValue(monedaSimbolo + numberFormat.format(saldo));
                        container.getContainerProperty(itemId, TIPOCAMBIO_PROPERTY).setValue(rsRecords.getString("TipoCambio"));
                        container.getContainerProperty(itemId, MONTO_QUETZALES_PROPERTY).setValue("Q." + numberFormat.format(saldoQ));
                        container.getContainerProperty(itemId, ESTATUS_PROPERTY).setValue(rsRecords.getString("Estatus"));
                        container.getContainerProperty(itemId, USUARIO_PROPERTY).setValue(rsRecords.getString("NombreUsuario"));
                        container.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(rsRecords.getString("Descripcion"));
                        if (rsRecords.getObject("ArchivoNombre") == null || rsRecords.getString("ArchivoNombre").trim().isEmpty() ) {
                            container.getContainerProperty(itemId, IMAGEN_PROPERTY).setValue("Cargar archivo");
                        } else {
                            container.getContainerProperty(itemId, IMAGEN_PROPERTY).setValue("Visualizar");
                        }
                        container.getContainerProperty(itemId, ARCHIVO_PROPERTY).setValue(rsRecords.getString("ArchivoNombre"));
                        container.getContainerProperty(itemId, ARCHIVO_TIPO_PROPERTY).setValue(rsRecords.getString("ArchivoTipo"));

                        container.getContainerProperty(itemId, VALORSF_PROPERTY).setValue(rsRecords.getString("Debe"));
                        container.getContainerProperty(itemId, MONTOQSF_PROPERTY).setValue(rsRecords.getString("DebeQuetzales"));
                        container.getContainerProperty(itemId, CUOTA_PROPERTY).setValue(rsRecords.getInt("EsCuota"));
                        container.getContainerProperty(itemId, CODIGOCC_PROPERTY).setValue(rsRecords.getString("CodigoCC"));
                        container.getContainerProperty(itemId, UUID_PROPERTY).setValue(rsRecords.getString("UUID"));

                        totalQueztales += rsRecords.getDouble("DebeQuetzales");

                    } while (rsRecords.next());

                    footerFacturaVenta.getCell(FACTURA_PROPERTY).setText("Total " + container.size() + " facturas.");
                    footerFacturaVenta.getCell(MONTO_QUETZALES_PROPERTY).setText("Q." + numberFormat.format(totalQueztales));
                }
                vanderaIsr = 0;
            } catch (Exception ex) {
                System.out.println("Error al buscar registros FACTURAS VENTA : " + ex);
                ex.printStackTrace();
            }
        } else {
            Notification notif = new Notification("La fecha hasta no puede contener un valor menor a la fecha de inicio.",
                    Notification.Type.WARNING_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.WARNING);
            notif.show(Page.getCurrent());
            inicioDt.focus();
        }
    }

    public void createTablaPartidaYCuentaCorriente() {

        reportLayoutPartida.setWidth("75%");
        reportLayoutPartida.addStyleName("rcorners3");

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

        containerPartida.addContainerProperty(ID_PARTIDA_PROPERTY, String.class, null);
        containerPartida.addContainerProperty(CUENTA_PROPERTY, String.class, null);
        containerPartida.addContainerProperty(DESCRIPCION_PARTIDA_PROPERTY, String.class, null);
        containerPartida.addContainerProperty(DEBE_PROPERTY, String.class, null);
        containerPartida.addContainerProperty(HABER_PROPERTY, String.class, null);

        partidaDocumentosGrid = new Grid("Partida contable", containerPartida);
        partidaDocumentosGrid.setImmediate(true);
        partidaDocumentosGrid.setSelectionMode(Grid.SelectionMode.NONE);
        partidaDocumentosGrid.setHeightMode(HeightMode.ROW);
        partidaDocumentosGrid.setHeightByRows(3);
        partidaDocumentosGrid.setWidth("100%");
        partidaDocumentosGrid.setResponsive(true);
        partidaDocumentosGrid.setEditorBuffered(false);

        partidaDocumentosGrid.getColumn(ID_PARTIDA_PROPERTY).setHidable(true).setHidden(true);

        partidaDocumentosGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {

            if (DEBE_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (HABER_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }

        });

        footer = partidaDocumentosGrid.appendFooterRow();
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
        cuentaCorrienteContainer.addContainerProperty(IMAGEN_PROPERTY, String.class, null);

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

        cuentaCorrientGrid.getColumn(IMAGEN_PROPERTY).setRenderer(new ButtonRenderer(e
                -> {

            if (container.getContainerProperty(e.getItemId(), IMAGEN_PROPERTY).getValue().equals("Cargar archivo")) {
                String codigoPartida = String.valueOf(container.getContainerProperty(e.getItemId(), CODIGO_PROPERTY).getValue());

                facturasVentaGrid.select(e.getItemId());

                CargarArchivoIngresoDocumentos cargarArchivo
                        = new CargarArchivoIngresoDocumentos(e.getItemId(), codigoPartida);
                UI.getCurrent().addWindow(cargarArchivo);
                cargarArchivo.center();

            } else {
                actualizarArchivo(e);
            }
        }));

        footerCC = cuentaCorrientGrid.appendFooterRow();
        footerCC.getCell(TIPODOCUMENTO_PROPERTY).setText("SUMAS : ");
        footerCC.getCell(DEBE_PROPERTY).setText("0.00");
        footerCC.getCell(HABER_PROPERTY).setText("0.00");
        footerCC.getCell(DEBE_PROPERTY).setStyleName("rightalign");
        footerCC.getCell(HABER_PROPERTY).setStyleName("rightalign");

        facturasYPartidasLayout.addComponent(partidaDocumentosGrid);
        facturasYPartidasLayout.addComponent(cuentaCorrientGrid);

        Button notaCreditoBtn = new Button("NOTA DE CREDITO");
        notaCreditoBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        notaCreditoBtn.setDescription("NOTA DE CREDITO");
        notaCreditoBtn.addListener((Button.ClickListener) (Button.ClickEvent event) -> {

            if (facturasVentaGrid.getSelectedRow() == null) {
                Notification notif = new Notification("Por favor, seleccione la factura para hacer nota de crédito.",
                        Notification.Type.WARNING_MESSAGE);
                notif.setDelayMsec(1500);
                notif.setPosition(Position.MIDDLE_CENTER);
                notif.setIcon(FontAwesome.WARNING);
                notif.show(Page.getCurrent());

            } else {
                if(container.getContainerProperty(facturasVentaGrid.getSelectedRow(), TIPODOCUMENTO_PROPERTY) == null) return;
                if (String.valueOf(container.getContainerProperty(facturasVentaGrid.getSelectedRow(), TIPODOCUMENTO_PROPERTY).getValue()).equals("FACTURA VENTA")) {

//                    Notification.show("NO DISPONIBLE EN ESTA VERSION!!!", Notification.Type.HUMANIZED_MESSAGE);

// JA 2023-11-16
//                    FacturaVentaForm newDocument = new FacturaVentaForm(String.valueOf(container.getContainerProperty(facturasVentaGrid.getSelectedRow(), CODIGO_PROPERTY).getValue()));
//                    newDocument.empresaCbx.select(empresa);
//                    newDocument.llenarComboCliente();
//                    newDocument.empresaCbx.setReadOnly(true);
//                    UI.getCurrent().addWindow(newDocument);
//                    newDocument.center();

                    NotaCreditoVentaInfileForm notaCreditoVentaForm
                            = new NotaCreditoVentaInfileForm(
                            String.valueOf(container.getContainerProperty(facturasVentaGrid.getSelectedRow(), CODIGO_PROPERTY).getValue()),
                            String.valueOf(container.getContainerProperty(facturasVentaGrid.getSelectedRow(), FACTURA_PROPERTY).getValue()).split(" ")[0],
                            String.valueOf(container.getContainerProperty(facturasVentaGrid.getSelectedRow(), FACTURA_PROPERTY).getValue()).split(" ")[1],
                            String.valueOf(container.getContainerProperty(facturasVentaGrid.getSelectedRow(), UUID_PROPERTY).getValue())
                    );
                    UI.getCurrent().addWindow(notaCreditoVentaForm);
                    notaCreditoVentaForm.center();

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

        Button editBtn = new Button("EDITAR");
        editBtn.setIcon(FontAwesome.EDIT);
        editBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        editBtn.setDescription("Actualizar datos");
        editBtn.addListener((Button.ClickListener) (Button.ClickEvent event) -> {

            if (facturasVentaGrid.getSelectedRow() == null) {
                Notification notif = new Notification("Por favor, seleccione el registro correspondiente que desea modificar.",
                        Notification.Type.WARNING_MESSAGE);
                notif.setDelayMsec(1500);
                notif.setPosition(Position.MIDDLE_CENTER);
                notif.setIcon(FontAwesome.WARNING);
                notif.show(Page.getCurrent());

            } else {

                if (String.valueOf(container.getContainerProperty(facturasVentaGrid.getSelectedRow(), ESTATUS_PROPERTY).getValue()).equals("REVISADO")) {
                    Notification notif = new Notification("No se puede editar una factura ya REVISADA. Por favor Seleccione una INGRESADA.",
                            Notification.Type.WARNING_MESSAGE);
                    notif.setDelayMsec(1500);
                    notif.setPosition(Position.MIDDLE_CENTER);
                    notif.setIcon(FontAwesome.WARNING);
                    notif.show(Page.getCurrent());
                } else {
                    EditarPartidaFacturaVenta editFacturasGasto
                            = new EditarPartidaFacturaVenta(
                            String.valueOf(container.getContainerProperty(facturasVentaGrid.getSelectedRow(), CODIGO_PROPERTY).getValue()),
                            String.valueOf(container.getContainerProperty(facturasVentaGrid.getSelectedRow(), DESCRIPCION_PROPERTY).getValue()),
                            "FACTURA VENTA"
                    );
                    editFacturasGasto.llenarComboProveedor();
                    editFacturasGasto.cuentaContable1Cbx.focus();
                    editFacturasGasto.llenarCampos();
                    UI.getCurrent().addWindow(editFacturasGasto);
                    editFacturasGasto.center();
                }
            }
        });

        Button isrBtn = new Button("RETENCION ISR");
        isrBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        isrBtn.setDescription("RETENCION DE ISR A UNA FACTURA");
        isrBtn.addListener((Button.ClickListener) (Button.ClickEvent event) -> {

            if (facturasVentaGrid.getSelectedRow() == null) {
                Notification notif = new Notification("Por favor, seleccione el registro correspondiente.",
                        Notification.Type.WARNING_MESSAGE);
                notif.setDelayMsec(1500);
                notif.setPosition(Position.MIDDLE_CENTER);
                notif.setIcon(FontAwesome.WARNING);
                notif.show(Page.getCurrent());
            } else if (container.getContainerProperty(facturasVentaGrid.getSelectedRow(), ESTATUS_PROPERTY).getValue().equals("REVISADO")) {

                queryString = "SELECT * FROM contabilidad_partida ";
                queryString += "WHERE CodigoCC = '" + String.valueOf(container.getContainerProperty(facturasVentaGrid.getSelectedRow(), CODIGO_PROPERTY).getValue()) + "' ";
                queryString += "AND CodigoPatida != '" + String.valueOf(container.getContainerProperty(facturasVentaGrid.getSelectedRow(), CODIGO_PROPERTY).getValue()) + "' ";
                queryString += "AND IdNomenclatura In (" + ((SopdiUI) mainUI).cuentasContablesDefault.getIsrGasto() + "," + ((SopdiUI) mainUI).cuentasContablesDefault.getIsrOpcionalMensualPorPagar() + ") ";
                queryString += "AND IdEmpresa =" + empresaCbx.getValue();
//                queryString += " AND TipoDocumento = 'CONSTANCIA ISR VENTA'";

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
                        TransaccionesEspecialesISRForm nuevaTransaccionIsr
                                = new TransaccionesEspecialesISRForm(
                                empresa,
                                String.valueOf(container.getContainerProperty(facturasVentaGrid.getSelectedRow(), CODIGO_PROPERTY).getValue()),
                                "CONSTANCIA ISR VENTA",
                                1
                        );
                        UI.getCurrent().addWindow(nuevaTransaccionIsr);
                        nuevaTransaccionIsr.center();
                    }
                } catch (Exception ex) {
                    System.out.println("Error al momento de buscar si la factura ya tiene una retencion por isr");
                }
            } else {

                Notification notif = new Notification("Primero debe REVISAR (dar por revisada) la factura antes de crear una CONSTANCIA ISR.",
                        Notification.Type.WARNING_MESSAGE);
                notif.setDelayMsec(1500);
                notif.setPosition(Position.MIDDLE_CENTER);
                notif.setIcon(FontAwesome.WARNING);
                notif.show(Page.getCurrent());

            }
        });

        Button ivaBtn = new Button("RETENCION IVA");
        ivaBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        ivaBtn.setDescription("RETENCION DE IVA A UNA FACTURA");
        ivaBtn.addListener((Button.ClickListener) (Button.ClickEvent event) -> {

            if (facturasVentaGrid.getSelectedRow() == null) {
                Notification notif = new Notification("Por favor, seleccione el registro correspondiente.",
                        Notification.Type.WARNING_MESSAGE);
                notif.setDelayMsec(1500);
                notif.setPosition(Position.MIDDLE_CENTER);
                notif.setIcon(FontAwesome.WARNING);
                notif.show(Page.getCurrent());
            } else if (container.getContainerProperty(facturasVentaGrid.getSelectedRow(), ESTATUS_PROPERTY).getValue().equals("REVISADO")) {

                queryString = " SELECT * FROM contabilidad_partida ";
                queryString += " WHERE CodigoCC = '" + String.valueOf(container.getContainerProperty(facturasVentaGrid.getSelectedRow(), CODIGO_PROPERTY).getValue()) + "'";
                queryString += " AND IdNomenclatura In (" + ((SopdiUI) mainUI).cuentasContablesDefault.getIvaPorPagar() + ")";
                queryString += " AND DEBE <> 0 ";
                queryString += " AND IdEmpresa =" + empresaCbx.getValue();
                queryString += " AND TipoDocumento = 'CONSTANCIA RETENCION IVA'";

                try {

                    stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                    rsRecords = stQuery.executeQuery(queryString);

                    if (rsRecords.next()) {

                        Notification notif = new Notification("Esta factura ya tiene una retencion de IVA.",
                                Notification.Type.WARNING_MESSAGE);
                        notif.setDelayMsec(1500);
                        notif.setPosition(Position.MIDDLE_CENTER);
                        notif.setIcon(FontAwesome.WARNING);
                        notif.show(Page.getCurrent());

                    } else {
                        TransaccionesEspecialesIVAForm nuevaTransaccionIva
                                = new TransaccionesEspecialesIVAForm(
                                empresa,
                                String.valueOf(container.getContainerProperty(facturasVentaGrid.getSelectedRow(), CODIGO_PROPERTY).getValue()),
                                "CONSTANCIA RETENCION IVA",
                                1
                        );
                        UI.getCurrent().addWindow(nuevaTransaccionIva);
                        nuevaTransaccionIva.center();
                    }
                } catch (Exception ex) {
                    System.out.println("Error al momento de buscar si la factura ya tiene una retencion por isr");
                }
            } else {

                Notification notif = new Notification("Primero debe REVISAR (dar por revisada) la factura antes de crear una CONSTANCIA IC.",
                        Notification.Type.WARNING_MESSAGE);
                notif.setDelayMsec(1500);
                notif.setPosition(Position.MIDDLE_CENTER);
                notif.setIcon(FontAwesome.WARNING);
                notif.show(Page.getCurrent());

            }
        });

        Button revisadoBtn = new Button("REVISADO");
        revisadoBtn.setIcon(FontAwesome.CHECK);
        revisadoBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        revisadoBtn.setDescription("Actualizar estatus");
        revisadoBtn.addListener((Button.ClickListener) (Button.ClickEvent event) -> {

            if (facturasVentaGrid.getSelectedRow() == null) {
                Notification notif = new Notification("Por favor, seleccione el registro correspondiente que desea modificar.",
                        Notification.Type.WARNING_MESSAGE);
                notif.setDelayMsec(1500);
                notif.setPosition(Position.MIDDLE_CENTER);
                notif.setIcon(FontAwesome.WARNING);
                notif.show(Page.getCurrent());
            } else {

                if (String.valueOf(container.getContainerProperty(facturasVentaGrid.getSelectedRow(), ESTATUS_PROPERTY).getValue()).equals("REVISADO")) {
                    Notification.show("La factura ya esta REVISADA.", Notification.Type.ERROR_MESSAGE);
                } else {

                    try {

                        queryString = "UPDATE  contabilidad_partida";
                        queryString += " set Estatus = 'REVISADO'";
                        queryString += " where NombreProveedor = '" + String.valueOf(container.getContainerProperty(facturasVentaGrid.getSelectedRow(), PROVEEDOR_PROPERTY).getValue()) + "'";
                        queryString += " and NITProveedor = '" + String.valueOf(container.getContainerProperty(facturasVentaGrid.getSelectedRow(), NIT_PROVEEDOR_PROPERTY).getValue()) + "'";
                        queryString += " and IdEmpresa = " + empresa;
                        queryString += " and SerieDocumento  = '" + (String.valueOf(container.getContainerProperty(facturasVentaGrid.getSelectedRow(), FACTURA_PROPERTY).getValue()).split(" ")[0]) + "'";
                        queryString += " and NumeroDocumento = '" + (String.valueOf(container.getContainerProperty(facturasVentaGrid.getSelectedRow(), FACTURA_PROPERTY).getValue()).split(" ")[1]) + "'";

                        stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                        stQuery.executeUpdate(queryString);

                        llenarTablaFacturaVenta(empresa);

                    } catch (SQLException ex) {
                        System.out.println("Error al intentar modificar estatus a revisado" + ex);
                        ex.printStackTrace();
                    }
                }
            }
        });

        Button anularBtn = new Button("ANULAR");
        anularBtn.addStyleName(ValoTheme.BUTTON_DANGER);
        anularBtn.setDescription("ANULAR DOCUMENTO");
        anularBtn.addListener((Button.ClickListener) (Button.ClickEvent event) -> {

            if (facturasVentaGrid.getSelectedRow() == null) {
                Notification notif = new Notification("Por favor, seleccione el documento a anular.",
                        Notification.Type.WARNING_MESSAGE);
                notif.setDelayMsec(1500);
                notif.setPosition(Position.MIDDLE_CENTER);
                notif.setIcon(FontAwesome.WARNING);
                notif.show(Page.getCurrent());

            } else {

                if (String.valueOf(container.getContainerProperty(facturasVentaGrid.getSelectedRow(), ESTATUS_PROPERTY).getValue()).equals("ANULADO")) {
                    Notification notif = new Notification("DOCUMENTO ANULADO.",
                            Notification.Type.WARNING_MESSAGE);
                    notif.setDelayMsec(1500);
                    notif.setPosition(Position.MIDDLE_CENTER);
                    notif.setIcon(FontAwesome.WARNING);
                    notif.show(Page.getCurrent());
                    return;
                }

                double saldo = Double.valueOf(String.valueOf(container.getContainerProperty(facturasVentaGrid.getSelectedRow(), SALDO_PROPERTY).getValue()).replaceAll(",", "").replaceAll("Q.", "").replaceAll("\\$.", ""));

                if (saldo == 0.00 && ((int)container.getContainerProperty(facturasVentaGrid.getSelectedRow(), CUOTA_PROPERTY).getValue()) == 0) {

                    Notification notif = new Notification("DOCUMENTO CON SALDO = 0.00, NO SE PUEDE ANULAR.",
                            Notification.Type.WARNING_MESSAGE);
                    notif.setDelayMsec(2000);
                    notif.setPosition(Position.MIDDLE_CENTER);
                    notif.setIcon(FontAwesome.WARNING);
                    notif.show(Page.getCurrent());
                    return;
                }

                FacturaVentaAnularForm anularWindow = new FacturaVentaAnularForm(
                        String.valueOf(container.getContainerProperty(facturasVentaGrid.getSelectedRow(), CODIGO_PROPERTY).getValue()),
                        String.valueOf(container.getContainerProperty(facturasVentaGrid.getSelectedRow(), CODIGOCC_PROPERTY).getValue()),
                        String.valueOf(container.getContainerProperty(facturasVentaGrid.getSelectedRow(), TIPODOCUMENTO_PROPERTY).getValue()),
                        (int) container.getContainerProperty(facturasVentaGrid.getSelectedRow(), CUOTA_PROPERTY).getValue());
                UI.getCurrent().addWindow(anularWindow);
                anularWindow.center();

                llenarTablaFacturaVenta(empresa);
            }
        });

//        botonesLayout.addComponent(editBtn);
//        botonesLayout.setComponentAlignment(editBtn, Alignment.BOTTOM_LEFT);
        botonesLayout.addComponent(notaCreditoBtn);
        botonesLayout.setComponentAlignment(notaCreditoBtn, Alignment.BOTTOM_LEFT);
        botonesLayout.addComponent(isrBtn);
        botonesLayout.setComponentAlignment(isrBtn, Alignment.BOTTOM_CENTER);
        botonesLayout.addComponent(ivaBtn);
        botonesLayout.setComponentAlignment(ivaBtn, Alignment.BOTTOM_CENTER);
        botonesLayout.addComponent(revisadoBtn);
        botonesLayout.setComponentAlignment(revisadoBtn, Alignment.BOTTOM_RIGHT);
        botonesLayout.addComponent(anularBtn);
        botonesLayout.setComponentAlignment(anularBtn, Alignment.BOTTOM_RIGHT);

        addComponent(detalleLayout);
        setComponentAlignment(detalleLayout, Alignment.MIDDLE_CENTER);

    }

    public void llenarTablaPartida(String nit, String serie, String numero, String proveedor, String codigoPartida) {
        containerPartida.removeAllItems();
        reportLayoutPartida.setEnabled(true);

        if (partidaDocumentosGrid != null) {
            partidaDocumentosGrid.setCaption("Partida contable del documento : " + serie + " " + numero);
        }

        totalDebe = 0.00;
        totalHaber = 0.00;

        queryString = " select contabilidad_partida.*,contabilidad_nomenclatura.N5, contabilidad_nomenclatura.NoCuenta";
        queryString += " from contabilidad_partida,contabilidad_nomenclatura";
        queryString += " where TipoDocumento IN ('FACTURA VENTA', 'RECIBO CONTABLE', 'RECIBO CONTABLE VENTA')";
        queryString += " and contabilidad_partida.CodigoPartida = '" + codigoPartida + "'";
        queryString += " and contabilidad_partida.IdEmpresa = " + empresa;
        queryString += " and contabilidad_nomenclatura.IdNomenclatura = contabilidad_partida.IdNomenclatura";

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado                                                
                do {

                    Object itemId = containerPartida.addItem();

                    containerPartida.getContainerProperty(itemId, ID_PARTIDA_PROPERTY).setValue(rsRecords.getString("IdPartida"));
                    containerPartida.getContainerProperty(itemId, CUENTA_PROPERTY).setValue(rsRecords.getString("NoCuenta"));
                    containerPartida.getContainerProperty(itemId, DESCRIPCION_PARTIDA_PROPERTY).setValue(rsRecords.getString("N5"));
                    if (rsRecords.getDouble("Debe") > 0.00) {
                        if (rsRecords.getString("MonedaDocumento").equals("QUETZALES")) {
                            containerPartida.getContainerProperty(itemId, DEBE_PROPERTY).setValue("Q." + numberFormat.format(rsRecords.getDouble("Debe")));
                        } else {
                            containerPartida.getContainerProperty(itemId, DEBE_PROPERTY).setValue("$." + numberFormat.format(rsRecords.getDouble("Debe")));
                        }

                    } else {
                        containerPartida.getContainerProperty(itemId, DEBE_PROPERTY).setValue("");
                    }
                    if (rsRecords.getDouble("Haber") > 0.00) {
                        if (rsRecords.getString("MonedaDocumento").equals("QUETZALES")) {
                            containerPartida.getContainerProperty(itemId, HABER_PROPERTY).setValue("Q." + numberFormat.format(rsRecords.getDouble("Haber")));
                        } else {
                            containerPartida.getContainerProperty(itemId, HABER_PROPERTY).setValue("$." + numberFormat.format(rsRecords.getDouble("Haber")));
                        }

                    } else {
                        containerPartida.getContainerProperty(itemId, HABER_PROPERTY).setValue("");
                    }

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

    public void llenarTablaCC(String nit, String serie, String numero, String idProveedor, String codigoCC) {
        cuentaCorrienteContainer.removeAllItems();
        reportLayoutPartida.setEnabled(true);

        if (cuentaCorrientGrid != null) {
            cuentaCorrientGrid.setCaption("Cuenta corriente del documento : " + serie + " " + numero);
        }

        totalDebe = 0.00;
        totalHaber = 0.00;

        footerCC.getCell(DEBE_PROPERTY).setText(numberFormat.format(totalDebe));
        footerCC.getCell(HABER_PROPERTY).setText(numberFormat.format(totalHaber));

        queryString = "SELECT Fecha, SerieDocumento, NumeroDocumento, TipoDocumento,";
        queryString += "CodigoPartida, MonedaDocumento, Debe, Haber ";
        queryString += "FROM contabilidad_partida ";
        queryString += "WHERE CodigoCC = '" + codigoCC + "' ";
        queryString += "AND IdNomenclatura = " +  ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getClientes() + " ";
        queryString += "AND TipoDocumento IN ('CHEQUE', 'NOTA DE DEBITO VENTA', 'NOTA DE CREDITO VENTA', 'TRANSFERENCIA', 'PAGO DOCUMENTO') ";
        queryString += "AND IdEmpresa = " + empresa;
//        queryString += " and DATE_FORMAT(contabilidad_partida.Fecha,\"%d-%m-%Y\") >= '" + fecha + "'";
        queryString += " Order By Fecha, Haber Desc";

System.out.println("QUERY llenarTablaCC Ingreso Documentos: " + queryString);

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

    public void actualizarArchivo(ClickableRenderer.RendererClickEvent e) {

        Object selectedObject = e.getItemId();
        String codigoPartida = String.valueOf(container.getContainerProperty(e.getItemId(), CODIGO_PROPERTY).getValue());
        String archivoNombre = String.valueOf(container.getContainerProperty(e.getItemId(), ARCHIVO_PROPERTY).getValue());
        String archivoTipo = String.valueOf(container.getContainerProperty(e.getItemId(), ARCHIVO_TIPO_PROPERTY).getValue());
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        Date fecha = null;
        try {
            fecha = df.parse(String.valueOf(container.getContainerProperty(e.getItemId(), FECHA_PROPERTY).getValue()));
        } catch (ParseException ex) {
            throw new RuntimeException(ex);
        }
        String[] serieNumeor = (String.valueOf(container.getContainerProperty(e.getItemId(), FACTURA_PROPERTY).getValue())).split(" ", 2);
        String uuid = String.valueOf(container.getContainerProperty(e.getItemId(), UUID_PROPERTY).getValue());

        facturasVentaGrid.select(e.getItemId());

        try {

            final byte docBytes[] = Files.readAllBytes(new File(archivoNombre).toPath());
            final String fileName = archivoNombre;

            if (docBytes == null) {
                Notification notif = new Notification("Documento no disponible, por favor ingrese uno nuevo!",
                        Notification.Type.WARNING_MESSAGE);
                notif.setDelayMsec(1500);
                notif.setPosition(Position.MIDDLE_CENTER);
                notif.setIcon(FontAwesome.CHECK);
                notif.show(Page.getCurrent());
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
                            System.out.println("length=" + stream.available());
                            if (mimeType.contains("png") || mimeType.contains("jpeg") || mimeType.contains("jpg") || mimeType.contains("pdf")) {
                                System.out.println("\nfileName=" + fileName);

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
                                Notification notif = new Notification("El archivo no contiene un formato compatible. solo puede subir archivos con formato 'PNG','JEPG','JPG','PDF'",
                                        Notification.Type.WARNING_MESSAGE);
                                notif.setDelayMsec(1500);
                                notif.setPosition(Position.MIDDLE_CENTER);
                                notif.setIcon(FontAwesome.CHECK);
                                notif.show(Page.getCurrent());
                                return;
                            }
                        } catch (java.io.IOException fIoEx) {
                            Notification notif = new Notification("Error al cargar el archivo adjunto, por favor intente nuevamente!",
                                    Notification.Type.WARNING_MESSAGE);
                            notif.setDelayMsec(1500);
                            notif.setPosition(Position.MIDDLE_CENTER);
                            notif.setIcon(FontAwesome.WARNING);
                            notif.show(Page.getCurrent());
                            fIoEx.printStackTrace();
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

                Button cagarAinnova = new Button("Cargar Ainnova");
                cagarAinnova.setIcon(FontAwesome.UPLOAD);
                cagarAinnova.setStyleName(ValoTheme.BUTTON_PRIMARY);
                Date finalFecha = fecha;
                cagarAinnova.addClickListener(new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        obtenerFacturaPdf(serieNumeor[0], serieNumeor[1], finalFecha);
                    }
                });
                cagarAinnova.setEnabled(!uuid.isEmpty());

                Button cargarInfile = new Button("Cargar Infile");
                cargarInfile.setIcon(FontAwesome.UPLOAD);
                cargarInfile.setStyleName(ValoTheme.BUTTON_PRIMARY);
                cargarInfile.addClickListener(new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        String path = ((SopdiUI) UI.getCurrent()).enviromentsVars.getDtePath() + serieNumeor[0] + "_" +  serieNumeor[1] + ".pdf";
                        pdfFile = InfileClient.obtenerDTEPdf(uuid, path);
                    }
                });

                HorizontalLayout buttons = new HorizontalLayout();
                buttons.addComponents(singleUpload, cagarAinnova, cargarInfile);

                buttons.setComponentAlignment(singleUpload, Alignment.BOTTOM_LEFT);
                buttons.setComponentAlignment(cagarAinnova, Alignment.BOTTOM_CENTER);
                buttons.setComponentAlignment(cargarInfile, Alignment.BOTTOM_RIGHT);

                pdfLayout.addComponents(buttons);

                window.setContent(pdfLayout);

                pdfLayout.setExpandRatio(browserFrame, 2);

            } else {
                //    window.setWidth("98%");
                //    window.setHeight("98%");

                VerticalLayout imageLayout = new VerticalLayout();
                imageLayout.setSizeFull();
                //    imageLayout.setSpacing(true);

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
                            Notification notif = new Notification("Error al cargar el archivo adjunto, vuelva intentarlo por favor!",
                                    Notification.Type.WARNING_MESSAGE);
                            notif.setDelayMsec(1500);
                            notif.setPosition(Position.MIDDLE_CENTER);
                            notif.setIcon(FontAwesome.WARNING);
                            notif.show(Page.getCurrent());

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

            UI.getCurrent().addWindow(window);
            window.center();

        } catch (Exception allEx) {
            Notification notif = new Notification("Error al intentar visualizar el archivo.!",
                    Notification.Type.WARNING_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.WARNING);
            notif.show(Page.getCurrent());

            allEx.printStackTrace();
        }

    }

    public void recibirParametrosImagen(String fileName, String mimeType, long peso) {
        parametro1 = fileName;
        parametro2 = mimeType;
        parametro3 = peso;
    }

    public void modificarCamposPagar(ClickableRenderer.RendererClickEvent e) {
        facturasVentaGrid.select(e.getItemId());
        String saldoFormateado = String.valueOf(container.getContainerProperty(e.getItemId(), SALDO_PROPERTY).getValue());
        double saldo = Double.valueOf(saldoFormateado.replaceAll(",", "").replaceAll("\\$.", "").replaceAll("Q.", ""));

        if (saldo == 0.00) {

            Notification.show("No se puede modificar el saldo con valor 0 ...", Notification.Type.WARNING_MESSAGE);

        } else {

            IngresoSaldoFacturaVenta montoPagar = new IngresoSaldoFacturaVenta(
                    empresa,
                    String.valueOf(container.getContainerProperty(e.getItemId(), CODIGO_PROPERTY).getValue()),
                    String.valueOf(container.getContainerProperty(e.getItemId(), SALDO_PROPERTY).getValue()),
                    String.valueOf(container.getContainerProperty(e.getItemId(), PROVEEDOR_PROPERTY).getValue()),
                    String.valueOf(container.getContainerProperty(e.getItemId(), FACTURA_PROPERTY).getValue())
            );
            UI.getCurrent().addWindow(montoPagar);
            montoPagar.center();
        }
    }


    public void guardarArchivo(Object selectedObject, String codigoPartida, String fileName) {
        try {
            queryString = " Update contabilidad_partida set  ";
            queryString += "  ArchivoTipo ='" + parametro2 + "'";
            queryString += ", ArchivoPeso = " + parametro3;
            queryString += ", ArchivoNombre = '" + fileName + "'";
            queryString += " where CodigoPartida = '" + codigoPartida + "'";

            stPreparedQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().prepareStatement(queryString);
            stPreparedQuery.executeUpdate();

            container.getContainerProperty(selectedObject, FacturaVentaView.IMAGEN_PROPERTY).setValue("Visualizar");
            container.getContainerProperty(selectedObject, FacturaVentaView.ARCHIVO_PROPERTY).setValue(fileName);
            container.getContainerProperty(selectedObject, FacturaVentaView.ARCHIVO_TIPO_PROPERTY).setValue(parametro2);

        } catch (Exception ex) {
            System.out.println("Error al intentar insertar Imagen" + ex);
            Notification.show("Error al insertar la imagen : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }

    public String getEmpresaNit() {
        String strNit = "N/A";

        String queryString = " SELECT Nit from contabilidad_empresa ";
        queryString += " Where IdEmpresa = " + empresa;

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
        if (this.facturasVentaGrid.getHeightByRows() > 0) {
            TableHolder tableHolder = new DefaultTableHolder(facturasVentaGrid);
            ExcelExport excelExport = new ExcelExport(tableHolder);
            excelExport.excludeCollapsedColumns();
            excelExport.setDisplayTotals(false);
            String fileexport;
            fileexport = (empresa + "_" + empresaCbx.getItemCaption(empresaCbx.getValue()).replaceAll(" ", "_").replaceAll(",", "_").replaceAll("[()]", "").replaceAll("[.]", "").replaceAll("ñ", "n").replaceAll("Ñ", "N").replaceAll("ó", "o").replaceAll("é", "") + "_DOCUMENTOS.xls").replaceAll(" ", "").replaceAll(",", "");
            excelExport.setExportFileName(fileexport);
            excelExport.export();
        }
        return true;
    }

    private void setTotal() {

        BigDecimal total = new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal totalQ = new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP);

        for (Object rid : facturasVentaGrid.getContainerDataSource()
                .getItemIds()) {
            if (rid == null) {
                return;
            }
            if (container.getContainerProperty(rid, VALORSF_PROPERTY).getValue() == null) {
                return;
            }
            total = total.add(new BigDecimal(
                    Double.parseDouble(
                            String.valueOf(container.getContainerProperty(rid, VALORSF_PROPERTY).getValue())
                    )));
            totalQ = total.add(new BigDecimal(
                    Double.parseDouble(
                            String.valueOf(container.getContainerProperty(rid, MONTOQSF_PROPERTY).getValue())
                    )));
        }
        footerFacturaVenta.getCell(VALOR_PROPERTY).setText(numberFormat.format(total));
        footerFacturaVenta.getCell(MONTO_QUETZALES_PROPERTY).setText(numberFormat.format(totalQ));
    }

    private void obtenerFacturaPdf(String serie, String numero, Date fecha) {
        Utileria utileria = new Utileria();
        long fileSize = 0;
        byte[] ba1 = new byte[1024];
        int baLength;

        try {

            Thread.sleep(1000);

//                URL pdfFileUrl = new URL("https://dte.guatefacturas.com/reports/rwservlet?repfel&report=r65_2170&destype=cache&desformat=pdf&paramform=no&P_NIT_EMISOR=" + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyTaxId() + "&P_UUID=" + serie);

            String credemciales = "P_NIT_EMISOR=" + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyTaxId() + "&P_SERIE=" + serie + "&P_NUMERO=" + numero + "&P_FECHA=" + utileria.getFechaSinFormato_v2(fecha);
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

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event
    ) {
        Page.getCurrent().setTitle("Sopdi - Factura venta");
    }

}
