package com.simpletecno.sopdi.compras;

import com.simpletecno.sopdi.configuracion.EnvironmentVars;
import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.simpletecno.sopdi.tesoreria.PagoChequesPDF;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.SelectionEvent;
import com.vaadin.event.SelectionEvent.SelectionListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
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
import java.nio.file.Files;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;

/**
 *
 * @author user
 */
public class ConsultaDocumentosView extends VerticalLayout implements View {

    MultiFileUpload singleUpload;
    public File file;
    StreamResource logoStreamResource = null;
    String parametro1, parametro2;
    Long parametro3;
    long fileSize;

    Double totalHaber;
    Double totalDebe;

    static final String EMPRESAID_PROPERTY = "EmpresaId";
    static final String EMPRESA_PROPERTY = "Empresa";
    static final String FECHA_PROPERTY = "Fecha";
    static final String TIPODOCUMENTO_PROPERTY = "Tipo";
    static final String DOCUMENTO_PROPERTY = "Documento";
    static final String MONTO_PROPERTY = "Monto";
    static final String MONEDA_PROPERTY = "Moneda";
    static final String ESTATUS_PROPERTY = "Estatus";
    static final String SALDO_PROPERTY = "Saldo";
    static final String PROVEEDOR_PROPERTY = "Proveedor";
    static final String USUARIO_PROPERTY = "Ingresado por";
    static final String DESCRIPCION_PROPERTY = "Descripción";
    static final String IMAGEN_PROPERTY = "Imagen";
    static final String ARCHIVO_PROPERTY = "Archivo";
    static final String ARCHIVO_TIPO_PROPERTY = "Type";
    static final String NOMBRECHEQUE_PROPERTY = "NCH";

    public IndexedContainer documentsContainer = new IndexedContainer();
    Grid documentosGrid;

    public IndexedContainer documentsContainerPartida = new IndexedContainer();
    static final String ID_PARTIDA_PROPERTY = "Id";
    static final String PARTIDA_PROPERTY = "Partida";
    static final String CUENTA_PROPERTY = "Cuenta";
    static final String DESCRIPCION_PARTIDA_PROPERTY = "Descripción";
    static final String DEBE_PROPERTY = "Debe";
    static final String HABER_PROPERTY = "Haber";

    Grid partidaDocumentosGrid;
    Grid.FooterRow footer;

    ComboBox tipoTransaccionCbx;
    TextField documentTxt;
    Button consultarBtn;

    UI mainUI;
    Statement stQuery;
    ResultSet rsRecords;
    String queryString;
    
    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");

    VerticalLayout reportLayoutPartida = new VerticalLayout();
    EnvironmentVars enviromentsVars;

    public ConsultaDocumentosView() {

        reportLayoutPartida.setEnabled(false);
        this.mainUI = UI.getCurrent();
        setWidth("100%");
        setMargin(false);
        setSpacing(true);

        enviromentsVars = new EnvironmentVars();

        Label titleLbl = new Label("DOCUMENTOS");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h1_custom");

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setResponsive(true);
        titleLayout.setWidth("100%");
        titleLayout.setMargin(false);
        titleLayout.addComponent(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.TOP_RIGHT);

        addComponent(titleLayout);
        setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        crearTablaFacturas();
        createTablaPartida();

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

        tipoTransaccionCbx = new ComboBox("Tipo transacción");
        tipoTransaccionCbx.setNewItemsAllowed(true);
        tipoTransaccionCbx.setWidth("15em");
        tipoTransaccionCbx.addItem("<<TODAS>>");
        tipoTransaccionCbx.addItem("FACTURA");
        tipoTransaccionCbx.addItem("FACTURA VENTA");
        tipoTransaccionCbx.addItem("RECIBO");
        tipoTransaccionCbx.addItem("RECIBO CORRIENTE");
        tipoTransaccionCbx.addItem("RECIBO CONTABLE");
        tipoTransaccionCbx.addItem("FORMULARIO");
        tipoTransaccionCbx.addItem("CHEQUE");
        tipoTransaccionCbx.addItem("TRANSFERENCIA ");
        tipoTransaccionCbx.addItem("NOTA DE CREDITO COMPRA");
        tipoTransaccionCbx.addItem("NOTA DE DEBITO COMPRA");
        tipoTransaccionCbx.addItem("NOTA DE CREDITO");
        tipoTransaccionCbx.addItem("NOTA DE DEBITO");
        tipoTransaccionCbx.addItem("PLANILLA");
        tipoTransaccionCbx.addItem("PRESTAMOS");
        tipoTransaccionCbx.addItem("ENGANCHES");
        tipoTransaccionCbx.addItem("DEPOSITO POR COMPRA DE MONEDA");
        tipoTransaccionCbx.addItem("INTERESES DEVENGADOS");
        tipoTransaccionCbx.addItem("REEMBOLSO DE ANTICIPOS");
        tipoTransaccionCbx.addItem("TRANSACCION ESPECIAL");
        tipoTransaccionCbx.addItem("PAGO DOCUMENTO");
        tipoTransaccionCbx.select("<<TODAS>>");

        documentTxt = new TextField("Documento : ");
        documentTxt.addStyleName(ValoTheme.TEXTAREA_ALIGN_CENTER);
        
        consultarBtn = new Button("Consultar");
        consultarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        consultarBtn.setIcon(FontAwesome.SEARCH);
        consultarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                llenarTablaFactura();
            }
        });

        documentsContainer.addContainerProperty(PARTIDA_PROPERTY, String.class, null);
        documentsContainer.addContainerProperty(EMPRESAID_PROPERTY, String.class, null);
        documentsContainer.addContainerProperty(EMPRESA_PROPERTY, String.class, null);
        documentsContainer.addContainerProperty(FECHA_PROPERTY, String.class, null);
        documentsContainer.addContainerProperty(DOCUMENTO_PROPERTY, String.class, null);
        documentsContainer.addContainerProperty(TIPODOCUMENTO_PROPERTY, String.class, null);
        documentsContainer.addContainerProperty(MONTO_PROPERTY, String.class, null);
        documentsContainer.addContainerProperty(MONEDA_PROPERTY, String.class, null);
        documentsContainer.addContainerProperty(ESTATUS_PROPERTY, String.class, null);
        documentsContainer.addContainerProperty(SALDO_PROPERTY, String.class, null);
        documentsContainer.addContainerProperty(PROVEEDOR_PROPERTY, String.class, null);
        documentsContainer.addContainerProperty(NOMBRECHEQUE_PROPERTY, String.class, null);
        documentsContainer.addContainerProperty(USUARIO_PROPERTY, String.class, null);
        documentsContainer.addContainerProperty(DESCRIPCION_PROPERTY, String.class, null);
        documentsContainer.addContainerProperty(IMAGEN_PROPERTY, String.class, null);
        documentsContainer.addContainerProperty(ARCHIVO_PROPERTY, String.class, null);
        documentsContainer.addContainerProperty(ARCHIVO_TIPO_PROPERTY, String.class, null);

        documentosGrid = new Grid("", documentsContainer);
        documentosGrid.setWidth("100%");
        documentosGrid.setImmediate(true);
        documentosGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        documentosGrid.setDescription("Seleccione un registro.");
        documentosGrid.setHeightMode(HeightMode.ROW);
        documentosGrid.setHeightByRows(7);
        documentosGrid.setResponsive(true);
        documentosGrid.setEditorBuffered(false);

        documentosGrid.getColumn(IMAGEN_PROPERTY).setRenderer(new ButtonRenderer(e -> {

            if (documentsContainer.getContainerProperty(e.getItemId(), IMAGEN_PROPERTY).getValue().equals("Cargar archivo")) {
                String codigoPartida = String.valueOf(documentsContainer.getContainerProperty(e.getItemId(), PARTIDA_PROPERTY).getValue());
                documentosGrid.select(e.getItemId());

                CargarArchivoIngresoDocumentos cargarArchivo
                        = new CargarArchivoIngresoDocumentos(e.getItemId(), codigoPartida);
                UI.getCurrent().addWindow(cargarArchivo);
                cargarArchivo.center();

            } else {
                VerCambiarImagen(e);
            }
        }));

        documentosGrid.getColumn(EMPRESAID_PROPERTY).setHidable(true).setHidden(true);
        documentosGrid.getColumn(PARTIDA_PROPERTY).setHidable(true).setHidden(true);
        documentosGrid.getColumn(TIPODOCUMENTO_PROPERTY).setExpandRatio(1);
        documentosGrid.getColumn(DOCUMENTO_PROPERTY).setExpandRatio(1);
        documentosGrid.getColumn(USUARIO_PROPERTY).setHidable(true).setHidden(true);
        documentosGrid.getColumn(DESCRIPCION_PROPERTY).setHidable(true).setHidden(true);
        documentosGrid.getColumn(ARCHIVO_PROPERTY).setHidable(true).setHidden(true);
        documentosGrid.getColumn(ARCHIVO_TIPO_PROPERTY).setHidable(true).setHidden(true);
        documentosGrid.getColumn(NOMBRECHEQUE_PROPERTY).setHidable(true).setHidden(true);

        documentosGrid.getColumn(EMPRESA_PROPERTY).setExpandRatio(2);
        documentosGrid.getColumn(MONTO_PROPERTY).setExpandRatio(2);
        documentosGrid.getColumn(FECHA_PROPERTY).setExpandRatio(2);
        documentosGrid.getColumn(DOCUMENTO_PROPERTY).setExpandRatio(2);
        documentosGrid.getColumn(TIPODOCUMENTO_PROPERTY).setExpandRatio(2);
        documentosGrid.getColumn(PROVEEDOR_PROPERTY).setExpandRatio(3);
        documentosGrid.getColumn(ESTATUS_PROPERTY).setExpandRatio(2);
        documentosGrid.getColumn(IMAGEN_PROPERTY).setExpandRatio(1);
        documentosGrid.getColumn(USUARIO_PROPERTY).setExpandRatio(1);

        documentosGrid.setCellStyleGenerator(
                (Grid.CellReference cellReference) -> {

                    if (USUARIO_PROPERTY.equals(cellReference.getPropertyId())) {
                        return "centeralign";
                    } else if (MONTO_PROPERTY.equals(cellReference.getPropertyId())) {
                        return "rightalign";
                    } else if (SALDO_PROPERTY.equals(cellReference.getPropertyId())) {
                        return "rightalign";
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
                    llenarTablaPartida(String.valueOf(documentosGrid.getContainerDataSource().getItem(documentosGrid.getSelectedRow()).getItemProperty(PARTIDA_PROPERTY).getValue()));
                }                
            }
        });

        filtrosLayout.addComponent(tipoTransaccionCbx);
        filtrosLayout.setComponentAlignment(tipoTransaccionCbx, Alignment.BOTTOM_CENTER);
        filtrosLayout.addComponent(documentTxt);
        filtrosLayout.setComponentAlignment(documentTxt, Alignment.BOTTOM_CENTER);
        filtrosLayout.addComponent(consultarBtn);
        filtrosLayout.setComponentAlignment(consultarBtn, Alignment.BOTTOM_CENTER);
        reportLayout.addComponent(filtrosLayout);
        reportLayout.setComponentAlignment(filtrosLayout, Alignment.TOP_CENTER);

        layoutGrid.addComponent(documentosGrid);
        layoutGrid.setComponentAlignment(documentosGrid, Alignment.TOP_CENTER);
        reportLayout.addComponent(layoutGrid);
        addComponent(reportLayout);

        setComponentAlignment(reportLayout, Alignment.MIDDLE_CENTER);
    }

    public void createTablaPartida() {

        reportLayoutPartida.setWidth("70%");
        reportLayoutPartida.addStyleName("rcorners3");

        documentsContainerPartida.addContainerProperty(ID_PARTIDA_PROPERTY, String.class, null);
        documentsContainerPartida.addContainerProperty(PARTIDA_PROPERTY, String.class, null);
        documentsContainerPartida.addContainerProperty(CUENTA_PROPERTY, String.class, null);
        documentsContainerPartida.addContainerProperty(DESCRIPCION_PARTIDA_PROPERTY, String.class, null);
        documentsContainerPartida.addContainerProperty(DEBE_PROPERTY, String.class, null);
        documentsContainerPartida.addContainerProperty(HABER_PROPERTY, String.class, null);

        partidaDocumentosGrid = new Grid("Partida contable", documentsContainerPartida);
        partidaDocumentosGrid.setImmediate(true);
        partidaDocumentosGrid.setSelectionMode(Grid.SelectionMode.NONE);
        partidaDocumentosGrid.setHeightMode(HeightMode.ROW);
        partidaDocumentosGrid.setHeightByRows(5);
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
        footer.getCell(DEBE_PROPERTY).setText("0.00");
        footer.getCell(HABER_PROPERTY).setText("0.00");
        footer.getCell(DEBE_PROPERTY).setStyleName("rightalign");
        footer.getCell(HABER_PROPERTY).setStyleName("rightalign");

        reportLayoutPartida.addComponent(partidaDocumentosGrid);

        addComponent(reportLayoutPartida);
        setComponentAlignment(reportLayoutPartida, Alignment.MIDDLE_CENTER);

    }

    public void llenarTablaPartida(String codigoPartida) {
        documentsContainerPartida.removeAllItems();
        reportLayoutPartida.setEnabled(true);

        if (partidaDocumentosGrid != null) {
            partidaDocumentosGrid.setCaption("Partida : " + codigoPartida);
        }

        totalDebe = 0.00;
        totalHaber = 0.00;

        queryString = " select contabilidad_partida.IdPartida, contabilidad_partida.MonedaDocumento,";
        queryString += " contabilidad_partida.Debe, contabilidad_partida.Haber,";
        queryString += " contabilidad_nomenclatura.N5, contabilidad_nomenclatura.NoCuenta";
        queryString += " from contabilidad_partida,contabilidad_nomenclatura";
        queryString += " where contabilidad_partida.CodigoPartida = '" + codigoPartida + "'";
        queryString += " and  contabilidad_nomenclatura.IdNomenclatura = contabilidad_partida.IdNomenclatura";               

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado                                                
                do {

                    Object itemId = documentsContainerPartida.addItem();

                    documentsContainerPartida.getContainerProperty(itemId, ID_PARTIDA_PROPERTY).setValue(rsRecords.getString("IdPartida"));
                    documentsContainerPartida.getContainerProperty(itemId, PARTIDA_PROPERTY).setValue(codigoPartida);
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

    public void llenarTablaFactura() {

        documentsContainer.removeAllItems();
        documentsContainerPartida.removeAllItems();
        
        try {

            queryString = " SELECT contabilidad_partida.IdEmpresa, contabilidad_empresa.Empresa EmpresaNombre, ";
            queryString += " contabilidad_partida.ArchivoNombre,contabilidad_partida.ArchivoTipo, contabilidad_partida.NombreCheque,";
            queryString += " contabilidad_partida.TipoDocumento, contabilidad_partida.CreadoFechaYHora,";
            queryString += " SUM(contabilidad_partida.Haber) AS total, SUM(contabilidad_partida.HaberQuetzales) AS totalQ,";
            queryString += " contabilidad_partida.Fecha, contabilidad_partida.NombreProveedor,contabilidad_partida.MonedaDocumento, ";
            queryString += " contabilidad_partida.CodigoPartida,contabilidad_partida.Debe,contabilidad_partida.Haber, ";
            queryString += " contabilidad_partida.DebeQuetzales,contabilidad_partida.HaberQuetzales, contabilidad_partida.TipoCambio, ";
            queryString += " contabilidad_partida.Saldo, contabilidad_partida.SerieDocumento, contabilidad_partida.NumeroDocumento, ";
            queryString += " contabilidad_partida.Descripcion, contabilidad_partida.MonedaDocumento, contabilidad_partida.Estatus,";
            queryString += " usuario.Nombre as NombreUsuario, contabilidad_partida.IDProveedor, orden_compra.NOC";
            queryString += " FROM contabilidad_partida ";
            queryString += " INNER JOIN contabilidad_empresa On contabilidad_empresa.IdEmpresa = contabilidad_partida.IdEmpresa";
            queryString += " INNER JOIN usuario on usuario.IdUsuario = contabilidad_partida.CreadoUsuario";
            queryString += " LEFT JOIN proveedor ON proveedor.IDProveedor = contabilidad_partida.IdProveedor";
            queryString += " LEFT JOIN orden_compra ON orden_compra.Id = contabilidad_partida.IdOrdenCompra";
            queryString += " WHERE contabilidad_partida.NumeroDocumento Like '%" + documentTxt.getValue().trim() + "%'";
            if (!String.valueOf(tipoTransaccionCbx.getValue()).equals("<<TODAS>>")) {
                queryString += " And contabilidad_partida.TipoDocumento = '" + String.valueOf(tipoTransaccionCbx.getValue()) + "'";
            }
            queryString += " GROUP by contabilidad_partida.CodigoPartida ";
            queryString += " ORDER By contabilidad_partida.Fecha";

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado                                   

                do {
                    Object itemId = documentsContainer.addItem();
                    documentsContainer.getContainerProperty(itemId, PARTIDA_PROPERTY).setValue(rsRecords.getString("CodigoPartida"));
                    documentsContainer.getContainerProperty(itemId, EMPRESAID_PROPERTY).setValue(rsRecords.getString("IdEmpresa"));
                    documentsContainer.getContainerProperty(itemId, EMPRESA_PROPERTY).setValue(rsRecords.getString("EmpresaNombre"));
                    documentsContainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha")));
                    documentsContainer.getContainerProperty(itemId, DOCUMENTO_PROPERTY).setValue(rsRecords.getString("SerieDocumento") + " " + rsRecords.getString("NumeroDocumento"));
                    documentsContainer.getContainerProperty(itemId, TIPODOCUMENTO_PROPERTY).setValue(rsRecords.getString("TipoDocumento"));
                    documentsContainer.getContainerProperty(itemId, MONTO_PROPERTY).setValue(numberFormat.format(rsRecords.getDouble("total")));
                    documentsContainer.getContainerProperty(itemId, MONEDA_PROPERTY).setValue(rsRecords.getString("MonedaDocumento"));
                    documentsContainer.getContainerProperty(itemId, ESTATUS_PROPERTY).setValue(rsRecords.getString("Estatus"));
                    documentsContainer.getContainerProperty(itemId, SALDO_PROPERTY).setValue(numberFormat.format(rsRecords.getDouble("SALDO")));
                    documentsContainer.getContainerProperty(itemId, PROVEEDOR_PROPERTY).setValue(rsRecords.getString("NombreProveedor"));
                    documentsContainer.getContainerProperty(itemId, USUARIO_PROPERTY).setValue(rsRecords.getString("NombreUsuario") + " " + rsRecords.getString("CreadoFechaYHora"));
                    documentsContainer.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(rsRecords.getString("Descripcion"));
                    documentsContainer.getContainerProperty(itemId, NOMBRECHEQUE_PROPERTY).setValue(rsRecords.getString("NombreCheque"));

                    if (rsRecords.getString("TipoDocumento").equals("CHEQUE")) {
                        documentsContainer.getContainerProperty(itemId, IMAGEN_PROPERTY).setValue("Visualizar");                       
                    }
                    else if (rsRecords.getObject("ArchivoNombre") == null || rsRecords.getString("ArchivoNombre").trim().isEmpty()) {
                        documentsContainer.getContainerProperty(itemId, IMAGEN_PROPERTY).setValue("Cargar archivo");
                    } else {
                        documentsContainer.getContainerProperty(itemId, IMAGEN_PROPERTY).setValue("Visualizar");
                    }

                    documentsContainer.getContainerProperty(itemId, ARCHIVO_PROPERTY).setValue(rsRecords.getString("ArchivoNombre"));
                    documentsContainer.getContainerProperty(itemId, ARCHIVO_TIPO_PROPERTY).setValue(rsRecords.getString("ArchivoTipo"));

                } while (rsRecords.next());
            }
        } catch (Exception ex) {
            System.out.println("Error al listar tabla Facturas en Consulta de Documentos:" + ex);
            ex.printStackTrace();
        }
    }

    public void VerCambiarImagen(ClickableRenderer.RendererClickEvent e) {

        Object selectedObject = e.getItemId();
        String empresaId = String.valueOf(documentsContainer.getContainerProperty(e.getItemId(), EMPRESAID_PROPERTY).getValue());
        String empresaNombre = String.valueOf(documentsContainer.getContainerProperty(e.getItemId(), EMPRESA_PROPERTY).getValue());
        String codigoPartida = String.valueOf(documentsContainer.getContainerProperty(e.getItemId(), PARTIDA_PROPERTY).getValue());
        String archivoNombre = String.valueOf(documentsContainer.getContainerProperty(e.getItemId(), ARCHIVO_PROPERTY).getValue());
        String archivoTipo = String.valueOf(documentsContainer.getContainerProperty(e.getItemId(), ARCHIVO_TIPO_PROPERTY).getValue());
        String descripcion   = String.valueOf(documentsContainer.getContainerProperty(e.getItemId(), DESCRIPCION_PROPERTY).getValue());
        String nombre        = String.valueOf(documentsContainer.getContainerProperty(e.getItemId(), NOMBRECHEQUE_PROPERTY).getValue());
        String documento     = String.valueOf(documentsContainer.getContainerProperty(e.getItemId(), DOCUMENTO_PROPERTY).getValue());
        String montoDocumento= String.valueOf(documentsContainer.getContainerProperty(e.getItemId(), MONTO_PROPERTY).getValue());

        if(String.valueOf(documentsContainer.getContainerProperty(e.getItemId(), TIPODOCUMENTO_PROPERTY).getValue()).equals("CHEQUE")) {

                PagoChequesPDF Pagocheques
                        = new PagoChequesPDF(
                                empresaId,
                                empresaNombre,
                                codigoPartida,
                                "0",
                                nombre,
                                documento,
                                descripcion,
                                montoDocumento.replace(",", "").replace("Q.", "").replace("$.","")
                        );
                mainUI.addWindow(Pagocheques);
                Pagocheques.center();
                return;
        }

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
            window.setWidth("95%");
            window.setHeight("95%");

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
                            if (mimeType.contains("png") || mimeType.contains("jpeg") || mimeType.contains("jpg") || mimeType.contains("pdf")) {

                                fileSize = stream.available();
                                byte[] buffer = new byte[stream.available()];
                                stream.read(buffer);

                                String filePath = enviromentsVars.getDtePath();

                                new File(filePath).mkdirs();

                                fileName = filePath + codigoPartida + fileName.substring(fileName.length() - 4, fileName.length());

                                new File(filePath).mkdirs();

                                targetFile = new File(fileName);
                                OutputStream outStream = new FileOutputStream(targetFile);
                                outStream.write(buffer);
                                outStream.close();

                                stream.close();

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
                        
                                documentsContainer.getContainerProperty(e.getItemId(), IMAGEN_PROPERTY).setValue("Visualizar");
        
                                guardarArchivo(selectedObject, codigoPartida, fileName);
                                window.close();
                            } else {
                                Notification.show("El archivo no contiene un formato compatible. solo puede subir archivos con formato 'PNG','JEPG','JPG','PDF'", Notification.Type.ERROR_MESSAGE);
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
                window.setWidth("98%");
                window.setHeight("98%");

                VerticalLayout imageLayout = new VerticalLayout();
                imageLayout.setSizeFull();
                imageLayout.setSpacing(true);

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
                                Notification.show("El archivo no contiene un formato compatible. solo puede subir archivos con formato 'PNG','JEPG','JPG','PDF'", Notification.Type.ERROR_MESSAGE);
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
    
    public void guardarArchivo(Object selectedObject, String codigoPartida, String fileName) {
        try {
            queryString = " Update contabilidad_partida set  ";
            queryString += "  ArchivoNombre ='" + fileName + "'";
            queryString += ", ArchivoTipo ='" + parametro2 + "'";
            queryString += ", ArchivoPeso = " + parametro3;
            queryString += " where CodigoPartida = '" + codigoPartida + "'";

            PreparedStatement stPreparedQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().prepareStatement(queryString);
            stPreparedQuery.executeUpdate();

            documentsContainer.getContainerProperty(selectedObject, IMAGEN_PROPERTY).setValue("Visualizar");
            documentsContainer.getContainerProperty(selectedObject, ARCHIVO_PROPERTY).setValue(fileName);
            documentsContainer.getContainerProperty(selectedObject, ARCHIVO_TIPO_PROPERTY).setValue(parametro2);

        } catch (Exception ex) {
            System.out.println("Error al intentar insertar Imagen" + ex);
        }
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - CONSULTAR Documentos");
    }
}
