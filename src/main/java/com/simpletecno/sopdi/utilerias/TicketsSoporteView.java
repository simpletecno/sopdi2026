package com.simpletecno.sopdi.utilerias;

import com.simpletecno.sopdi.SeguimientoWindow;
import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.configuracion.EnvironmentVars;
import com.simpletecno.sopdi.extras.InfileTest;
import com.vaadin.data.Property;
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
import com.vaadin.ui.*;
import com.vaadin.ui.Grid.HeaderCell;
import com.vaadin.ui.Grid.HeaderRow;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.renderers.ClickableRenderer;
import com.vaadin.ui.themes.ValoTheme;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.MultiFileUpload;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadFinishedHandler;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadStateWindow;
import org.vaadin.dialogs.ConfirmDialog;

import javax.mail.MessagingException;
import java.io.*;
import java.nio.file.Files;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author user
 */
public class TicketsSoporteView extends VerticalLayout implements View {

    MultiFileUpload singleUpload;
    public File file;
    StreamResource logoStreamResource = null;
    String parametro1 = "", parametro2 = "";
    Long parametro3 = 0L;
    long fileSize;
    EnvironmentVars enviromentsVars = new EnvironmentVars();
    
    public Grid ticketsGrid;
    public IndexedContainer ticketsContainer = new IndexedContainer();
    static final String ID_PROPERTY = "Id";
    static final String FECHA_PROPERTY = "Fecha";
    static final String DESCRIPCION_PROPERTY = "Descripción";
    static final String SOLICITANTE_PROPERTY = "Solicitante";
    static final String REFERENCIA_PROPERTY = "Referencia";
    static final String CATEGORIA_PROPERTY = "Categoria";
    static final String PRIORIDAD_PROPERTY = "Prioridadd";
    public static final String ESTATUS_PROPERTY = "Estatus";
    static final String ASIGNADO_PROPERTY = "Asignado a";
    static final String ARCHIVO_PROPERTY = "Archivo";
    static final String ARCHIVO_TIPO_PROPERTY = "Type";
    static final String USUARIO_PROPERTY = "Usuario";
    static final String USUARIOID_SOLICITANTE_PROPERTY = "IdUsuarioSolicitante";
    static final String USUARIOID_ASIGNADO_PROPERTY = "IdUsuarioAsignado";
    static final String MAILTO_SOLICITANTE_PROPERTY = "MailToSolicitante";
    static final String MAILTO_ASIGNADO_PROPERTY = "MailToAsignado";
    Grid.FooterRow ticketsFooter;

    Grid seguimientoGrid;
    public IndexedContainer seguimientoContainer = new IndexedContainer();
    Grid.FooterRow footer;
    
    OptionGroup fechaOpcion;
    OptionGroup categoriaOpcion;
    CheckBox incluirCerradosChb;

    UI mainUI;
    Statement stQuery, stQuery1, stQuery2;
    ResultSet rsRecords, rsRecords2;
    String queryString;

    Button nuevoTicketBtn;
    Button seguimientoBtn;
    Button cerrarTicketBtn;
    Button TestButton;

    VerticalLayout mainLayout = new VerticalLayout();

    public TicketsSoporteView() {

        mainLayout.setEnabled(false);
        this.mainUI = UI.getCurrent();
        setWidth("100%");
        setMargin(false);
        setSpacing(true);

        enviromentsVars = new EnvironmentVars();

        Label titleLbl = new Label("TICKETS DE SOPORTE - MOD");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h1_custom");

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setResponsive(true);
        titleLayout.setSpacing(true);
        titleLayout.setWidth("100%");
        titleLayout.setMargin(false);
        titleLayout.addComponent(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.MIDDLE_CENTER);
        titleLayout.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

        addComponent(titleLayout);
        setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        createTicketsGrid();
        createSeguimientoGrid();

        fillTicketsGrid();
    }

    public void createTicketsGrid() {

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

        fechaOpcion = new OptionGroup("De fecha : ");
        fechaOpcion.addItems("Este mes", "Mes anterior", "Este año", "TODOS");
        fechaOpcion.select("Este mes");
        fechaOpcion.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
        fechaOpcion.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                fillTicketsGrid();
            }
        });

        categoriaOpcion = new OptionGroup("Categoria : ");
        categoriaOpcion.addItems("ERROR","MEJORA", "NUEVA FUNCION", "APOYO", "TODOS");
        categoriaOpcion.select("TODOS");
        categoriaOpcion.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
        categoriaOpcion.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                fillTicketsGrid();
            }
        });

        incluirCerradosChb = new CheckBox("Incluir CERRADOS");
        incluirCerradosChb.setValue(false);
        incluirCerradosChb.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                fillTicketsGrid();
            }
        });

        ticketsContainer.addContainerProperty(ID_PROPERTY, String.class, null);
        ticketsContainer.addContainerProperty(FECHA_PROPERTY, String.class, null);
        ticketsContainer.addContainerProperty(DESCRIPCION_PROPERTY, String.class, null);
        ticketsContainer.addContainerProperty(SOLICITANTE_PROPERTY, String.class, null);
        ticketsContainer.addContainerProperty(REFERENCIA_PROPERTY, String.class, null);
        ticketsContainer.addContainerProperty(CATEGORIA_PROPERTY, String.class, null);
        ticketsContainer.addContainerProperty(PRIORIDAD_PROPERTY, String.class, null);
        ticketsContainer.addContainerProperty(ASIGNADO_PROPERTY, String.class, null);
        ticketsContainer.addContainerProperty(ESTATUS_PROPERTY, String.class, null);
        ticketsContainer.addContainerProperty(ARCHIVO_PROPERTY, String.class, null);
        ticketsContainer.addContainerProperty(ARCHIVO_TIPO_PROPERTY, String.class, null);
        ticketsContainer.addContainerProperty(USUARIOID_SOLICITANTE_PROPERTY, String.class, null);
        ticketsContainer.addContainerProperty(MAILTO_SOLICITANTE_PROPERTY, String.class, null);
        ticketsContainer.addContainerProperty(USUARIOID_ASIGNADO_PROPERTY, String.class, null);
        ticketsContainer.addContainerProperty(MAILTO_ASIGNADO_PROPERTY, String.class, null);

        ticketsGrid = new Grid("", ticketsContainer);
        ticketsGrid.setWidth("100%");
        ticketsGrid.setImmediate(true);
        ticketsGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        ticketsGrid.setDescription("Seleccione un registro.");
        ticketsGrid.setHeightMode(HeightMode.ROW);
        ticketsGrid.setHeightByRows(4);
        ticketsGrid.setResponsive(true);
        ticketsGrid.setEditorBuffered(false);

//        ticketsGrid.addItemClickListener(e -> {
//            if(e.isDoubleClick()) {
//                Notification.show(String.valueOf(ticketsContainer.getContainerProperty(e.getItemId(), DESCRIPCION_PROPERTY).getValue()), Notification.Type.HUMANIZED_MESSAGE);
//            }
//        });

        ticketsGrid.getColumn(ID_PROPERTY).setExpandRatio(1);
        ticketsGrid.getColumn(FECHA_PROPERTY).setExpandRatio(1);
        ticketsGrid.getColumn(DESCRIPCION_PROPERTY).setExpandRatio(4);
        ticketsGrid.getColumn(REFERENCIA_PROPERTY).setExpandRatio(1);
        ticketsGrid.getColumn(SOLICITANTE_PROPERTY).setExpandRatio(1);
        ticketsGrid.getColumn(CATEGORIA_PROPERTY).setExpandRatio(1);
        ticketsGrid.getColumn(PRIORIDAD_PROPERTY).setExpandRatio(1);
        ticketsGrid.getColumn(ASIGNADO_PROPERTY).setExpandRatio(1);
        ticketsGrid.getColumn(ESTATUS_PROPERTY).setExpandRatio(1);
        ticketsGrid.getColumn(ARCHIVO_PROPERTY).setExpandRatio(1);
        ticketsGrid.getColumn(ARCHIVO_TIPO_PROPERTY).setHidable(true).setHidden(true);
        ticketsGrid.getColumn(MAILTO_SOLICITANTE_PROPERTY).setHidable(true).setHidden(true);
        ticketsGrid.getColumn(USUARIOID_SOLICITANTE_PROPERTY).setHidable(true).setHidden(true);
        ticketsGrid.getColumn(MAILTO_ASIGNADO_PROPERTY).setHidable(true).setHidden(true);
        ticketsGrid.getColumn(USUARIOID_ASIGNADO_PROPERTY).setHidable(true).setHidden(true);

        ticketsGrid.getColumn(ARCHIVO_PROPERTY).setRenderer(new ButtonRenderer(e -> {
             verCambiarImagen(
                     e,
                     String.valueOf(ticketsContainer.getContainerProperty(e.getItemId(), ARCHIVO_PROPERTY).getValue()),
                     String.valueOf(ticketsContainer.getContainerProperty(e.getItemId(),   ARCHIVO_TIPO_PROPERTY).getValue())
            );
        }));

        ticketsGrid.addSelectionListener(new SelectionListener() {
            @Override
            public void select(SelectionEvent event
            ) {
                if (ticketsGrid.getSelectedRow() != null) {
                    fillSeguimientoGrid();
                    if( ((SopdiUI) mainUI).sessionInformation.getStrProjectId() != null) {
                        Notification.show(String.valueOf(ticketsContainer.getContainerProperty(ticketsGrid.getSelectedRow(), DESCRIPCION_PROPERTY).getValue()), Notification.Type.HUMANIZED_MESSAGE);
                    }
                }
            }
        });

        HeaderRow filterRow = ticketsGrid.appendHeaderRow();

        HeaderCell cell0 = filterRow.getCell(DESCRIPCION_PROPERTY);

        TextField filterField0 = new TextField();
        filterField0.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField0.setInputPrompt("Filtrar");
        filterField0.setColumns(12);

        filterField0.addTextChangeListener(change -> {
            ticketsContainer.removeContainerFilters(DESCRIPCION_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                ticketsContainer.addContainerFilter(
                        new SimpleStringFilter(DESCRIPCION_PROPERTY,
                                change.getText(), true, false));
            }
            setTotal();
        });

        cell0.setComponent(filterField0);

        HeaderCell cell = filterRow.getCell(CATEGORIA_PROPERTY);

        TextField filterField = new TextField();
        filterField.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField.setInputPrompt("Filtrar");
        filterField.setColumns(8);

        filterField.addTextChangeListener(change -> {
            ticketsContainer.removeContainerFilters(CATEGORIA_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                ticketsContainer.addContainerFilter(
                        new SimpleStringFilter(CATEGORIA_PROPERTY,
                                change.getText(), true, false));
            }
            setTotal();
        });
        cell.setComponent(filterField);

        HeaderCell cell11 = filterRow.getCell(PRIORIDAD_PROPERTY);

        TextField filterField11 = new TextField();
        filterField11.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField11.setInputPrompt("Filtrar");
        filterField11.setColumns(8);

        filterField11.addTextChangeListener(change -> {
            ticketsContainer.removeContainerFilters(PRIORIDAD_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                ticketsContainer.addContainerFilter(
                        new SimpleStringFilter(PRIORIDAD_PROPERTY,
                                change.getText(), true, false));
            }
            setTotal();
        });
        cell11.setComponent(filterField11);

        HeaderCell cell2 = filterRow.getCell(SOLICITANTE_PROPERTY);
        TextField filterField2 = new TextField();
        filterField2.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField2.setInputPrompt("Filtrar");
        filterField2.setColumns(10);

        filterField2.addTextChangeListener(change -> {
            ticketsContainer.removeContainerFilters(SOLICITANTE_PROPERTY);
            if (!change.getText().isEmpty()) {
                ticketsContainer.addContainerFilter(
                        new SimpleStringFilter(SOLICITANTE_PROPERTY,
                                change.getText(), true, false));
            }
            setTotal();
        });
        cell2.setComponent(filterField2);

        HeaderCell cell3 = filterRow.getCell(ESTATUS_PROPERTY);
        TextField filterField3 = new TextField();
        filterField3.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField3.setInputPrompt("Filtrar");
        filterField3.setColumns(10);

        filterField3.addTextChangeListener(change -> {
            ticketsContainer.removeContainerFilters(ESTATUS_PROPERTY);
            if (!change.getText().isEmpty()) {
                ticketsContainer.addContainerFilter(
                        new SimpleStringFilter(ESTATUS_PROPERTY,
                                change.getText(), true, false));
            }
            setTotal();
        });
        cell3.setComponent(filterField3);

        HeaderCell cell4 = filterRow.getCell(ASIGNADO_PROPERTY);
        TextField filterField4 = new TextField();
        filterField4.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField4.setInputPrompt("Filtrar");
        filterField4.setColumns(10);

        filterField4.addTextChangeListener(change -> {
            ticketsContainer.removeContainerFilters(ASIGNADO_PROPERTY);
            if (!change.getText().isEmpty()) {
                ticketsContainer.addContainerFilter(
                        new SimpleStringFilter(ASIGNADO_PROPERTY,
                                change.getText(), true, false));
            }
            setTotal();
        });
        cell4.setComponent(filterField4);

        ticketsFooter = ticketsGrid.appendFooterRow();

        ticketsFooter.getCell(DESCRIPCION_PROPERTY).setText("Total");
        ticketsFooter.getCell(ASIGNADO_PROPERTY).setText("0");

        nuevoTicketBtn = new Button("Nuevo");
        nuevoTicketBtn.setIcon(FontAwesome.PLUS);
        nuevoTicketBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        nuevoTicketBtn.setDescription("Agregar nuevo ticket.");
        nuevoTicketBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                TicketSoporteForm ticketSoporteForm = new TicketSoporteForm();
                mainUI.addWindow(ticketSoporteForm);
                ticketSoporteForm.center();
            }
        });

        filtrosLayout.addComponent(fechaOpcion);
        filtrosLayout.setComponentAlignment(fechaOpcion, Alignment.BOTTOM_CENTER);
        filtrosLayout.addComponent(categoriaOpcion);
        filtrosLayout.setComponentAlignment(categoriaOpcion, Alignment.BOTTOM_CENTER);
        filtrosLayout.addComponent(incluirCerradosChb);
        filtrosLayout.setComponentAlignment(incluirCerradosChb, Alignment.BOTTOM_CENTER);
        filtrosLayout.addComponent(nuevoTicketBtn);
        filtrosLayout.setComponentAlignment(nuevoTicketBtn, Alignment.BOTTOM_CENTER);
        reportLayout.addComponent(filtrosLayout);
        reportLayout.setComponentAlignment(filtrosLayout, Alignment.TOP_CENTER);

        layoutGrid.addComponent(ticketsGrid);
        layoutGrid.setComponentAlignment(ticketsGrid, Alignment.TOP_CENTER);
        reportLayout.addComponent(layoutGrid);
        addComponent(reportLayout);

        setComponentAlignment(reportLayout, Alignment.MIDDLE_CENTER);
    }


    public void createSeguimientoGrid() {

        VerticalLayout detalleLayout = new VerticalLayout();
        detalleLayout.setWidth("100%");
        detalleLayout.addStyleName("rcorners2");
        detalleLayout.setSpacing(true);

        HorizontalLayout botonesLayout = new HorizontalLayout();
        botonesLayout.setSpacing(true);
        botonesLayout.setWidth("100%");

        seguimientoContainer.addContainerProperty(ID_PROPERTY, String.class, null);
        seguimientoContainer.addContainerProperty(FECHA_PROPERTY, String.class, null);
        seguimientoContainer.addContainerProperty(USUARIO_PROPERTY, String.class, null);
        seguimientoContainer.addContainerProperty(DESCRIPCION_PROPERTY, String.class, null);
        seguimientoContainer.addContainerProperty(ARCHIVO_PROPERTY, String.class, null);
        seguimientoContainer.addContainerProperty(ARCHIVO_TIPO_PROPERTY, String.class, null);

        seguimientoGrid = new Grid("Seguimiento de ticket", seguimientoContainer);
        seguimientoGrid.setImmediate(true);
        seguimientoGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        seguimientoGrid.setHeightMode(HeightMode.ROW);
        seguimientoGrid.setHeightByRows(5);
        seguimientoGrid.setWidth("100%");
        seguimientoGrid.setResponsive(true);

        seguimientoGrid.getColumn(ID_PROPERTY).setHidable(true).setHidden(true);
        seguimientoGrid.getColumn(FECHA_PROPERTY).setExpandRatio(1);
        seguimientoGrid.getColumn(DESCRIPCION_PROPERTY).setExpandRatio(5);
        seguimientoGrid.getColumn(USUARIO_PROPERTY).setExpandRatio(1);
        seguimientoGrid.getColumn(ARCHIVO_PROPERTY).setExpandRatio(1);
        seguimientoGrid.getColumn(ARCHIVO_TIPO_PROPERTY).setExpandRatio(1).setHidable(true).setHidden(true);

        seguimientoGrid.getColumn(ARCHIVO_PROPERTY).setRenderer(new ButtonRenderer(e -> {
            verCambiarImagen(
                    e,
                    String.valueOf(seguimientoContainer.getContainerProperty(e.getItemId(), ARCHIVO_PROPERTY).getValue()),
                    String.valueOf(seguimientoContainer.getContainerProperty(e.getItemId(), ARCHIVO_TIPO_PROPERTY).getValue())
            );
        }));

        seguimientoGrid.addSelectionListener(new SelectionListener() {
            @Override
            public void select(SelectionEvent event
            ) {
                if (seguimientoGrid.getSelectedRow() != null) {
//                    fillSeguimientoGrid();
                    if( ((SopdiUI) mainUI).sessionInformation.getStrProjectId() != null) {
                        Notification.show(String.valueOf(seguimientoContainer.getContainerProperty(seguimientoGrid.getSelectedRow(), DESCRIPCION_PROPERTY).getValue()), Notification.Type.HUMANIZED_MESSAGE);
                    }
                }
            }
        });

        detalleLayout.addComponent(seguimientoGrid);

        seguimientoBtn = new Button("NUEVO SEGUIMIENTO");
        seguimientoBtn.setIcon(FontAwesome.EDIT);
        seguimientoBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        seguimientoBtn.setDescription("NUEVO SEGUIMIENTO");
        seguimientoBtn.addListener((Button.ClickListener) (Button.ClickEvent event) -> {

            if (ticketsGrid.getSelectedRow() == null) {
                Notification notif = new Notification("Por favor, seleccione el registro correspondiente.",
                        Notification.Type.WARNING_MESSAGE);
                notif.setDelayMsec(1500);
                notif.setPosition(Position.MIDDLE_CENTER);
                notif.setIcon(FontAwesome.WARNING);
                notif.show(Page.getCurrent());
            } else {
                String eMailTo = "";
                if(String.valueOf(ticketsContainer.getContainerProperty(ticketsGrid.getSelectedRow(), USUARIOID_SOLICITANTE_PROPERTY).getValue()).equals(((SopdiUI) mainUI).sessionInformation.getStrUserId())) {
                    eMailTo = String.valueOf(ticketsContainer.getContainerProperty(ticketsGrid.getSelectedRow(), MAILTO_ASIGNADO_PROPERTY).getValue());
                }
                else {
                    eMailTo = String.valueOf(ticketsContainer.getContainerProperty(ticketsGrid.getSelectedRow(), MAILTO_SOLICITANTE_PROPERTY).getValue());
                }
                SeguimientoWindow seguimientoWindow = new SeguimientoWindow(
                        null,
                       String.valueOf(ticketsContainer.getContainerProperty(ticketsGrid.getSelectedRow(), ID_PROPERTY).getValue()),
                        "TICKET: " + String.valueOf(ticketsContainer.getContainerProperty(ticketsGrid.getSelectedRow(), DESCRIPCION_PROPERTY).getValue()),
                        "EN PROCESO",
                        eMailTo);
                mainUI.addWindow(seguimientoWindow);
                seguimientoWindow.center();
                seguimientoWindow.seguimientoTxt.focus();
            }
        });

        UploadFinishedHandler handler;
        handler = new UploadFinishedHandler() {
            @Override
            public void handleFile(InputStream stream, String fileName, String mimeType, long length) {

                File targetFile;
System.out.println("mimeType=" + mimeType);
                try {
                    if (mimeType.contains("png") || mimeType.contains("jpeg")
                            || mimeType.contains("jpg") || mimeType.contains("pdf")
                            || mimeType.contains("officedocument.spreadsheetml.sheet")
                    ) {

                        fileSize = stream.available();
                        byte[] buffer = new byte[stream.available()];
                        stream.read(buffer);

                        String filePath = enviromentsVars.getDtePath();

                        new File(filePath).mkdirs();

//                        fileName = filePath + codigoPartidaUpdate + fileName.substring(fileName.length()-4, fileName.length());

                        if(fileName.endsWith(".xlsx")) {
                            fileName = filePath + new Utileria().getReferencia() + fileName.substring(fileName.length() - 5, fileName.length());
                        }
                        else {
                            fileName = filePath + new Utileria().getReferencia() + fileName.substring(fileName.length() - 4, fileName.length());
                        }

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

                        if (ticketsGrid.getSelectedRow() == null) {
                            Notification notif = new Notification("Por favor, seleccione el registro correspondiente.",
                                    Notification.Type.WARNING_MESSAGE);
                            notif.setDelayMsec(1500);
                            notif.setPosition(Position.MIDDLE_CENTER);
                            notif.setIcon(FontAwesome.WARNING);
                            notif.show(Page.getCurrent());
                            return;
                        }
                        else {
                            guardarArchivoSeguimiento();
                        }

                    } else {
                        Notification.show("El archivo no contiene un formato compatible. solo puede subir archivos con formato 'PNG','JEPG','JPG','PDF', 'XLS', 'XLSX'", Notification.Type.ERROR_MESSAGE);
                        return;
                    }

                } catch (Exception fIoEx) {
                    fIoEx.printStackTrace();
                    Notification.show("Error al cargar el archivo adjunto!", Notification.Type.ERROR_MESSAGE);
                    return;
                }
            }
        };

        UploadStateWindow window = new UploadStateWindow();

        singleUpload = new MultiFileUpload(handler, window, false);
        singleUpload.setIcon(FontAwesome.UPLOAD);
        singleUpload.setImmediate(true);
        singleUpload.getSmartUpload().setUploadButtonCaptions("Buscar y cargar archivo", "");

        JavaScript.getCurrent().execute("document.getElementsByClassName('gwt-FileUpload')[0].setAttribute('accept', '.png')");
        JavaScript.getCurrent().execute("document.getElementsByClassName('gwt-FileUpload')[1].setAttribute('accept', '.jpg')");
        JavaScript.getCurrent().execute("document.getElementsByClassName('gwt-FileUpload')[1].setAttribute('accept', '.jpeg')");
        JavaScript.getCurrent().execute("document.getElementsByClassName('gwt-FileUpload')[2].setAttribute('accept', '.PDF')");
        JavaScript.getCurrent().execute("document.getElementsByClassName('gwt-FileUpload')[2].setAttribute('accept', '.xls')");
        JavaScript.getCurrent().execute("document.getElementsByClassName('gwt-FileUpload')[2].setAttribute('accept', '.xlsx')");

        cerrarTicketBtn = new Button("Cerrar ticket");
        cerrarTicketBtn.addStyleName(ValoTheme.BUTTON_DANGER);
        cerrarTicketBtn.setDescription("SEGUIMIENTO");
        cerrarTicketBtn.addListener((Button.ClickListener) (Button.ClickEvent event) -> {

            if (ticketsGrid.getSelectedRow() == null) {
                Notification notif = new Notification("Por favor, seleccione el registro correspondiente.",
                        Notification.Type.WARNING_MESSAGE);
                notif.setDelayMsec(1500);
                notif.setPosition(Position.MIDDLE_CENTER);
                notif.setIcon(FontAwesome.WARNING);
                notif.show(Page.getCurrent());
                return;
            }
            if (String.valueOf(ticketsContainer.getContainerProperty(ticketsGrid.getSelectedRow(), ESTATUS_PROPERTY).getValue()).equals("CERRADO")) {
                Notification notif = new Notification("EL TICKET YA ESTA CERRADO!!.",
                        Notification.Type.WARNING_MESSAGE);
                notif.setDelayMsec(1500);
                notif.setPosition(Position.MIDDLE_CENTER);
                notif.setIcon(FontAwesome.WARNING);
                notif.show(Page.getCurrent());
                return;
            }

            ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de CERRAR el ticket ?",
                    "SI", "NO", new ConfirmDialog.Listener() {

                        public void onClose(ConfirmDialog dialog) {
                            if (dialog.isConfirmed()) {
                                String queryString;
                                queryString =  "Update ticket_soporte Set Estatus = 'CERRADO'";
                                queryString += " Where IdTicket = " + String.valueOf(ticketsContainer.getContainerProperty(ticketsGrid.getSelectedRow(), ID_PROPERTY).getValue());

                                try {

                                    stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                                    stQuery.executeUpdate(queryString);

                                    queryString = "Insert Into ticket_soporte_seguimiento (IdTicket, Observacion, CreadoFechaYHora, CreadoUsuario)";
                                    queryString += " Values (";
                                    queryString += " " + String.valueOf(ticketsContainer.getContainerProperty(ticketsGrid.getSelectedRow(), ID_PROPERTY).getValue());
                                    queryString += ",'CERRADO POR USUARIO'";
                                    queryString += ",current_timestamp";
                                    queryString += ", " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserId();
                                    queryString += ")";

                                    stQuery.executeUpdate(queryString);

                                    fillSeguimientoGrid();
                                    ticketsContainer.getContainerProperty(ticketsGrid.getSelectedRow(), ESTATUS_PROPERTY).setValue("CERRADO");

                                    String eMailTo = "";
                                    if(String.valueOf(ticketsContainer.getContainerProperty(ticketsGrid.getSelectedRow(), USUARIOID_SOLICITANTE_PROPERTY).getValue()).equals(((SopdiUI) mainUI).sessionInformation.getStrUserId())) {
                                        eMailTo = String.valueOf(ticketsContainer.getContainerProperty(ticketsGrid.getSelectedRow(), USUARIOID_ASIGNADO_PROPERTY).getValue());
                                    }
                                    else {
                                        eMailTo = String.valueOf(ticketsContainer.getContainerProperty(ticketsGrid.getSelectedRow(), USUARIOID_SOLICITANTE_PROPERTY).getValue());
                                    }
                                    try {
                                        String emailsTo[] = {eMailTo};
                                        MyEmailMessanger eMail = new MyEmailMessanger();

                                        String texto  = "!!! NUEVO SEGUIMIENTO !!!\n";
                                        texto += "EMPRESA     : " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId() + " " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyName() + "\n";
                                        texto += "CREADO POR  : " + ((SopdiUI) mainUI).sessionInformation.getStrUserFullName() + "\n";
                                        texto += "ESTATUS     : CERRADO\n";
                                        texto += "OBSERVACION : TICKET HA SIDO CERRADO." + "\n";
                                        texto += "\n\n\n***Creado automaticamente por el sistema SOPDI.***";

                                        eMail.postMail(emailsTo, "SOPDI : Ticket de soporte : " + String.valueOf(ticketsContainer.getContainerProperty(ticketsGrid.getSelectedRow(), DESCRIPCION_PROPERTY).getValue()), texto );
                                    } catch (MessagingException ex2) {
                                        Logger.getLogger(SopdiUI.class.getName()).log(Level.SEVERE, null, ex2);
                                    }

                                }
                                catch(Exception ex) {
                                    Notification.show("Error al actualizar TICKET : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                                    ex.printStackTrace();
                                }
                            }
                        }
                    }
            );

        });

        TestButton = new Button("Pruebas Infile (Crear)");
        TestButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        TestButton.addClickListener(clickEvent -> {
            InfileTest infileTest = new InfileTest();
            UI.getCurrent().addWindow(infileTest);
            infileTest.center();
        });

        botonesLayout.addComponent(seguimientoBtn);
        botonesLayout.setComponentAlignment(seguimientoBtn, Alignment.BOTTOM_LEFT);
        botonesLayout.addComponent(singleUpload);
        botonesLayout.setComponentAlignment(singleUpload, Alignment.BOTTOM_LEFT);
        botonesLayout.addComponent(cerrarTicketBtn);
        botonesLayout.setComponentAlignment(cerrarTicketBtn, Alignment.BOTTOM_LEFT);
        botonesLayout.addComponent(TestButton);
        botonesLayout.setComponentAlignment(TestButton, Alignment.BOTTOM_LEFT);

        detalleLayout.addComponent(botonesLayout);

        addComponent(detalleLayout);
        setComponentAlignment(detalleLayout, Alignment.MIDDLE_CENTER);

    }

    public void fillSeguimientoGrid() {
        seguimientoContainer.removeAllItems();
        mainLayout.setEnabled(true);

        if (seguimientoGrid != null) {
            seguimientoGrid.setCaption("Seguimiento del ticket : " + ticketsContainer.getContainerProperty(ticketsGrid.getSelectedRow(), ID_PROPERTY).getValue() + " " + ticketsContainer.getContainerProperty(ticketsGrid.getSelectedRow(), DESCRIPCION_PROPERTY).getValue());
        }
        else {
            return;
        }

        queryString = " select t.*, u.Nombre NombreUsuario ";
        queryString += "  from ticket_soporte_seguimiento t";
        queryString += "  inner join usuario u on u.IdUsuario = t.CreadoUsuario";
        queryString += " where t.IdTicket = " + ticketsContainer.getContainerProperty(ticketsGrid.getSelectedRow(), ID_PROPERTY).getValue();
        queryString += " order by t.CreadoFechaYHora Desc";

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado
                do {

                    Object itemId = seguimientoContainer.addItem();

                    seguimientoContainer.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords.getString("IdTicketSoporteSeguimiento"));
                    seguimientoContainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue(Utileria.getFechaDDMMYYYY_HHMM_2(rsRecords.getTimestamp("CreadoFechaYHora")));
                    seguimientoContainer.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(rsRecords.getString("Observacion"));
                    seguimientoContainer.getContainerProperty(itemId, USUARIO_PROPERTY).setValue(rsRecords.getString("NombreUsuario"));
                    seguimientoContainer.getContainerProperty(itemId, ARCHIVO_PROPERTY).setValue(rsRecords.getString("UrlDocumento"));
                    seguimientoContainer.getContainerProperty(itemId, ARCHIVO_TIPO_PROPERTY).setValue(rsRecords.getString("UrlDocumentoTipo"));

                } while (rsRecords.next());
            }
        } catch (Exception ex) {
            System.out.println("Error al listar tabla seguimiento de tickets:" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void fillTicketsGrid() {

        ticketsContainer.removeAllItems();
        seguimientoContainer.removeAllItems();

        setTotal();

        try {

            queryString = " SELECT t.*, u.Nombre as Solicitante, u.Usuario MailToSolicitante, ";
            queryString += " u2.Nombre AsignadoA, u2.Usuario MailToAsignado ";
            queryString += " FROM ticket_soporte t ";
            queryString += " INNER JOIN usuario u  on u.IdUsuario = t.CreadoUsuario";
            queryString += " INNER JOIN usuario u2 on u2.IdUsuario = t.AsignadoUsuario";
            queryString += " WHERE Trim(t.Descripcion) <> '' ";
            if(String.valueOf(fechaOpcion.getValue()).equals("Este mes")) {
                queryString += " AND Extract(YEAR_MONTH FROM t.CreadoFechaYHora) = Extract(YEAR_MONTH FROM CURDATE()) ";
            }
            if(String.valueOf(fechaOpcion.getValue()).equals("Mes anterior")) {
                queryString += " AND Extract(YEAR_MONTH FROM t.CreadoFechaYHora) = Extract(YEAR_MONTH FROM (CURDATE() - INTERVAL 1 MONTH )) ";
            }
            if(String.valueOf(fechaOpcion.getValue()).equals("Este año")) {
                queryString += " AND Extract(YEAR FROM t.CreadoFechaYHora) = Extract(YEAR FROM CURDATE()) ";
            }
            if(!String.valueOf(categoriaOpcion.getValue()).equals("TODOS")) {
                queryString += " AND t.Categoria = '" + categoriaOpcion.getValue() + "'";
            }
            if (!((SopdiUI) mainUI).sessionInformation.getStrUserProfileName().equals("Administrador")) {
                queryString += " AND (   t.CreadoUsuario   = " + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                queryString += "      OR t.AsignadoUsuario = " + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                queryString += "     )";
            }
            if(!incluirCerradosChb.getValue()) {
                queryString += " AND t.Estatus <> 'CERRADO'";
            }
//            queryString += " And t.IdEmpresa = " + empresaCbx.getValue();
            queryString += " order by t.CreadoFechaYHora Desc";

//System.out.println("Query TICKETS : " + queryString);

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {

                do {
                    Object itemId = ticketsContainer.addItem();

                    ticketsContainer.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords.getString("IdTicket"));
                    ticketsContainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("CreadoFechaYHora")));
                    ticketsContainer.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(rsRecords.getString("Descripcion"));
                    ticketsContainer.getContainerProperty(itemId, SOLICITANTE_PROPERTY).setValue(rsRecords.getString("Solicitante"));
                    ticketsContainer.getContainerProperty(itemId, CATEGORIA_PROPERTY).setValue(rsRecords.getString("Categoria"));
                    ticketsContainer.getContainerProperty(itemId, PRIORIDAD_PROPERTY).setValue(rsRecords.getString("Prioridad"));
                    ticketsContainer.getContainerProperty(itemId, ASIGNADO_PROPERTY).setValue(rsRecords.getString("AsignadoA"));
                    ticketsContainer.getContainerProperty(itemId, REFERENCIA_PROPERTY).setValue(rsRecords.getString("PuntoReferencia"));
                    ticketsContainer.getContainerProperty(itemId, ESTATUS_PROPERTY).setValue(rsRecords.getString("Estatus"));
                    ticketsContainer.getContainerProperty(itemId, ARCHIVO_PROPERTY).setValue(rsRecords.getString("UrlDocumento"));
                    ticketsContainer.getContainerProperty(itemId, ARCHIVO_TIPO_PROPERTY).setValue(rsRecords.getString("UrlDocumentoTipo"));
                    ticketsContainer.getContainerProperty(itemId, USUARIOID_SOLICITANTE_PROPERTY).setValue(rsRecords.getString("CreadoUsuario"));
                    ticketsContainer.getContainerProperty(itemId, USUARIOID_ASIGNADO_PROPERTY).setValue(rsRecords.getString("AsignadoUsuario"));
                    ticketsContainer.getContainerProperty(itemId, MAILTO_SOLICITANTE_PROPERTY).setValue(rsRecords.getString("MailToSolicitante"));
                    ticketsContainer.getContainerProperty(itemId, MAILTO_ASIGNADO_PROPERTY).setValue(rsRecords.getString("MailToAsignado"));

                } while (rsRecords.next());

                ticketsGrid.select(ticketsGrid.getContainerDataSource().getIdByIndex(0));

                setTotal();

            }
        } catch (Exception ex) {
            System.out.println("Error al listar tickets de soporte : " + ex);
            ex.printStackTrace();
        }
    }

    public void verCambiarImagen(
            ClickableRenderer.RendererClickEvent e,
            String archivoNombre,
            String archivoTipo
    ) {

        try {

            String filePath = enviromentsVars.getDtePath();
//System.out.println("file="+filePath + archivoNombre);
            final byte docBytes[] = Files.readAllBytes(new File(filePath + archivoNombre).toPath());
            final String fileName = filePath + archivoNombre;

            if (docBytes == null) {
                Notification.show("Documento no disponible para visualizar!");
                return;
            }
            Window window = new Window();
            window.setResizable(true);
            window.setWidth("80%");
            window.setHeight("80%");
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
            if (archivoTipo.contains("pdf") || archivoTipo.contains("officedocument.spreadsheetml.sheet")) {
                window.setWidth("98%");
                window.setHeight("98%");

                VerticalLayout pdfLayout = new VerticalLayout();
                pdfLayout.setSizeFull();
                pdfLayout.setSpacing(true);

                BrowserFrame browserFrame = new BrowserFrame();
                browserFrame.setSizeFull();
                browserFrame.setSource(documentStreamResource);

                pdfLayout.addComponent(browserFrame);
                window.setContent(browserFrame);

            } else {
                //window.setWidth("98%");
                //window.setHeight("98%");

                VerticalLayout imageLayout = new VerticalLayout();
                imageLayout.setSizeUndefined();
                //imageLayout.setSpacing(true);

                Image imageViewComponent = new Image(null, documentStreamResource);
                imageViewComponent.setImmediate(true);
                imageViewComponent.setSizeFull();
                imageViewComponent.addStyleName("my-img-button");
                imageViewComponent.setData(archivoNombre);

                imageLayout.addComponent(imageViewComponent);
                imageLayout.setComponentAlignment(imageViewComponent, Alignment.MIDDLE_CENTER);
                window.setContent(imageLayout);

            }

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

    public void guardarArchivo(Object selectedObject, String idTicket, String fileName) {
        try {
            queryString = " Update ticket_soporte set  ";
            queryString += "  UrlDocumento = '" + fileName + "'";
            queryString += " ,UrlDocumentoTipo = '" + parametro2 + "'";
            queryString += " Where IdTicket = " + idTicket;

            PreparedStatement stPreparedQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().prepareStatement(queryString);
            stPreparedQuery.executeUpdate();

            ticketsContainer.getContainerProperty(selectedObject, ARCHIVO_PROPERTY).setValue(fileName);
            ticketsContainer.getContainerProperty(selectedObject, ARCHIVO_TIPO_PROPERTY).setValue(parametro2);

        } catch (Exception ex) {
            System.out.println("Error al intentar actualizar documento en ticket." + ex);
        }
    }

    public void guardarArchivoSeguimiento() {
        try {
            queryString = " Update ticket_soporte_seguimiento set  ";
            queryString += "  UrlDocumento = '" + parametro1 + "'";
            queryString += " ,UrlDocumentoTipo = '" + parametro2 + "'";
            queryString += " Where IdTicketSoporteSeguimiento = " + seguimientoContainer.getContainerProperty(seguimientoGrid.getSelectedRow(), ID_PROPERTY).getValue();
//System.out.println("queryStringArchivoSeguimiento="+queryString);
            PreparedStatement stPreparedQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().prepareStatement(queryString);
            stPreparedQuery.executeUpdate();

            seguimientoContainer.getContainerProperty(seguimientoGrid.getSelectedRow(), ARCHIVO_PROPERTY).setValue(parametro1);
            seguimientoContainer.getContainerProperty(seguimientoGrid.getSelectedRow(), ARCHIVO_TIPO_PROPERTY).setValue(parametro2);

        } catch (Exception ex) {
            System.out.println("Error al intentar actualizar documento en seguimiento de ticket." + ex);
        }
    }

    private void setTotal() {

        ticketsFooter.getCell(ESTATUS_PROPERTY).setText(String.valueOf(ticketsContainer.size()));
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - Tickets");
    }
}