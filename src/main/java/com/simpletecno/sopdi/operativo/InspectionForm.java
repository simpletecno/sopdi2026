package com.simpletecno.sopdi.operativo;

import com.simpletecno.sopdi.*;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Sizeable;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.themes.Runo;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vaadin.dialogs.ConfirmDialog;

/**
 * Esta ventana procesa la gestion de venta :
 *
 * @author JAguirre
 */
@SuppressWarnings("serial")
public class InspectionForm extends Window {

    static final String NOMBRE_PROPERTY = "Nombre";
    static final String DPI_PROPERTY = "DPI";
    static final String EMAIL_PROPERTY = "Email";
    static final String PUNTO_AGENDA_PROPERTY = "Punto de Agenda";

    static final String CORRELATIVO_PROPERTY = "#"; // <-- Compartida Participantes Y Agenda

    TextField idVisitaInspeccionTxt;
    TextField codigoVisitaInspeccionTxt;
    TextField referenciaTxt;
    TextField lugarTxt;
    TextArea observacionesTxt;
    DateField fechaYHoraInicioDt;
    DateField fechaYHoraFinDt;
    ComboBox motivoCbx;
    ComboBox clienteCbx;
    ComboBox centroCostoCbx;

    IndexedContainer participantesContainer = new IndexedContainer();
    Grid participantesGrid;

    IndexedContainer agendaContainer = new IndexedContainer();
    Grid agendaGrid;

    Button saveBtn;
    Button visualizarBtn;
    Button resolucionesBtn;
    Button cargarDocumentosBtn;
    Button exitBtn;
    Button tasksBtn;

    Statement stQuery = null;
    ResultSet rsRecords = null;
    Statement stQuery1 = null;
    ResultSet rsRecords1 = null;
    PreparedStatement stPreparedQuery = null;

    private UI mainUI;
    String codigoVisita = "";
    String queryString = "";
    String idVisitaInspeccion = "";
    String codigoVisitaInspeccion = "";

    public File file;
    StreamResource logoStreamResource = null;
    String parametro1, parametro2;
    Long parametro3;

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public InspectionForm(
            String idVisitaInspeccion, 
            String codigoVisita
    ) {
        this.idVisitaInspeccion = idVisitaInspeccion;
        this.codigoVisitaInspeccion = codigoVisita;
        this.mainUI = UI.getCurrent();

        setResponsive(true);
        setModal(true);
        setWidth("80%");

        FormLayout formLayout = new FormLayout();
        formLayout.setMargin(true);
        formLayout.setWidth("50%");
        formLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        formLayout.setMargin(false);
        formLayout.addStyleName(ValoTheme.FORMLAYOUT_LIGHT);

        idVisitaInspeccionTxt = new TextField("Id visita : ");
        idVisitaInspeccionTxt.setWidth("5em");
        idVisitaInspeccionTxt.setValue(idVisitaInspeccion);
        idVisitaInspeccionTxt.setReadOnly(true);
        idVisitaInspeccionTxt.setVisible(false);

        codigoVisitaInspeccionTxt = new TextField("Código : ");
        codigoVisitaInspeccionTxt.setWidth("5em");
        codigoVisitaInspeccionTxt.setReadOnly(true);

        fechaYHoraInicioDt = new DateField("Fecha y hora inicio :");
        fechaYHoraInicioDt.setValue(new java.util.Date());
        fechaYHoraInicioDt.setResolution(Resolution.MINUTE);
        fechaYHoraInicioDt.setDateFormat("dd-MM-yyyy hh:mm");
//        fechaYHoraInicioDt.setWidth("150px");
//        fechaYHoraInicioDt.addStyleName("time-only");

        fechaYHoraFinDt = new DateField("Fecha y hora fin:");
        fechaYHoraFinDt.setValue(new java.util.Date());
        fechaYHoraFinDt.setResolution(Resolution.MINUTE);
        fechaYHoraFinDt.setDateFormat("dd-MM-yyyy hh:mm");
//        fechaYHoraFinDt.setWidth("150px");
//        fechaYHoraFinDt.addStyleName("time-only");

        motivoCbx = new ComboBox("Motivo :");
        motivoCbx.addContainerProperty("Codigo", String.class, "");
        motivoCbx.setInvalidAllowed(false);
        motivoCbx.setNewItemsAllowed(false);
        motivoCbx.setPageLength(15);        
        motivoCbx.addItem("<<ELIJA>>").getItemProperty("Codigo").setValue("0");                 
        motivoCbx.addItem("Visita de cliente").getItemProperty("Codigo").setValue("1");        
        motivoCbx.addItem("Reunión de obra").getItemProperty("Codigo").setValue("2");       
        motivoCbx.addItem("Comité técnico").getItemProperty("Codigo").setValue("3");        
        motivoCbx.addItem("Visita gerente").getItemProperty("Codigo").setValue("4");                
        motivoCbx.addItem("Visita director").getItemProperty("Codigo").setValue("5");          
        motivoCbx.addItem("Visita accionista").getItemProperty("Codigo").setValue("6");        
        motivoCbx.addItem("Visita gubernamental").getItemProperty("Codigo").setValue("7");        
        motivoCbx.addItem("Inspección de rutina").getItemProperty("Codigo").setValue("8");        
        motivoCbx.addItem("Reunión con residente").getItemProperty("Codigo").setValue("9");        
        motivoCbx.addItem("Reunión de consejo").getItemProperty("Codigo").setValue("10");        
        motivoCbx.addItem("Correo electrónico").getItemProperty("Codigo").setValue("11");        
        motivoCbx.addItem("Informática").getItemProperty("Codigo").setValue("12");
        
        motivoCbx.addValueChangeListener(event -> {
            if (participantesGrid != null) {
                participantesGrid.cancelEditor();
                participantesGrid.getColumn("Nombre").setEditorField(getComboState());
            }
            if (String.valueOf(motivoCbx.getItemCaption(motivoCbx.getValue())).contains("Visita")) {
                lugarTxt.setValue("Proyecto");
            } else {
                lugarTxt.setValue("");
            }
            
            if (String.valueOf(motivoCbx.getItemCaption(motivoCbx.getValue())).equals("Visita de cliente")
                    || String.valueOf(motivoCbx.getItemCaption(motivoCbx.getValue())).equals("Correo electrónico")) {
                centroCostoCbx.setVisible(true);
            } else {
                centroCostoCbx.setValue("");
                centroCostoCbx.setVisible(false);
            }
                        
            if (String.valueOf(motivoCbx.getItemCaption(motivoCbx.getValue())).equals("Visita de cliente")
                    || String.valueOf(motivoCbx.getItemCaption(motivoCbx.getValue())).equals("Reunión con residente")) {                
                clienteCbx.setVisible(true);
            } else {
                clienteCbx.select(0);
                clienteCbx.setVisible(false);
            }

        });

        centroCostoCbx = new ComboBox("Centro costo :");
        centroCostoCbx.addContainerProperty("CodigoCentroCosto", String.class, null);
        fillComboCentroCosto();

        clienteCbx = new ComboBox("Cliente :");
        clienteCbx.setWidth("25em");
        clienteCbx.setRequired(true);
        clienteCbx.setRequiredError("POR FAVOR ELIJA AL CLIENTE.");
        clienteCbx.setInvalidAllowed(false);
        clienteCbx.setNewItemsAllowed(false);
        clienteCbx.setFilteringMode(FilteringMode.CONTAINS);
        clienteCbx.addContainerProperty("CodigoCentroCosto", String.class, null);
        this.fillComboCliente();
        clienteCbx.setVisible(false);

        clienteCbx.addValueChangeListener(event -> {
System.out.println("cliente=" + clienteCbx.getValue() + " centrocosto=" + clienteCbx.getItem( clienteCbx.getValue()).getItemProperty("CodigoCentroCosto").getValue());
            if (centroCostoCbx != null) {
                centroCostoCbx.setEnabled(true);
            }
            centroCostoCbx.getItemIds().forEach((_item) -> {

                if (centroCostoCbx.getItem(_item) != null) {
System.out.println("centro costo=" + centroCostoCbx.getItem(_item));
                    if (String.valueOf(centroCostoCbx.getItem(_item).getItemProperty("CodigoCentroCosto").getValue()).equals(String.valueOf(clienteCbx.getItem( clienteCbx.getValue()).getItemProperty("CodigoCentroCosto").getValue()))) {
                        centroCostoCbx.select(_item);
                    }
                } else {
                    //centroCostoCbx.select(0);
                }
                Object itemid = participantesContainer.addItem();

                participantesContainer.getContainerProperty(itemid, NOMBRE_PROPERTY).setValue(clienteCbx.getItemCaption(clienteCbx.getValue()));
                participantesContainer.getContainerProperty(itemid, DPI_PROPERTY).setValue("");
                participantesContainer.getContainerProperty(itemid, EMAIL_PROPERTY).setValue("");

            });
        });

        referenciaTxt = new TextField("Referencia : ");
        referenciaTxt.setWidth("25em");

        lugarTxt = new TextField("Lugar : ");
        lugarTxt.setWidth("25em");

        observacionesTxt = new TextArea("Obsevaciónes : ");
        observacionesTxt.setWidth("25em");
        observacionesTxt.setHeight("3em");

        participantesContainer.addContainerProperty(NOMBRE_PROPERTY, String.class, "");
        participantesContainer.addContainerProperty(DPI_PROPERTY, String.class, "");
        participantesContainer.addContainerProperty(EMAIL_PROPERTY, String.class, "");

        participantesGrid = new Grid("Participantes", participantesContainer);
        participantesGrid.setEditorBuffered(false);
        participantesGrid.setHeightMode(HeightMode.ROW);
        participantesGrid.setHeightByRows(7);
        participantesGrid.setEditorEnabled(true);
        participantesGrid.setSelectionMode(SelectionMode.NONE);
        participantesGrid.setWidth("100%");
        participantesGrid.setDescription("DOBLE Click aqui para editar lista de participantes.");
        participantesGrid.getColumn("Nombre").setEditorField(getComboState());
        participantesGrid.addItemClickListener((event) -> {
            if (event != null) {
                participantesGrid.editItem(event.getItemId());
                participantesGrid.cancelEditor();
            }

        });
        /**
         * participantesGrid.addItemClickListener(event -> // Java 8 {
         * if(event.isDoubleClick()) { selectedRow =
         * Integer.valueOf(String.valueOf(event.getItemId()));
         * System.out.println("String.valueOf(event.getItemId())=" +
         * String.valueOf(event.getItemId())); System.out.println("selectedRow =
         * " + selectedRow); } } );
         *
         */

/////// Creacion de grid agenda
        agendaContainer.addContainerProperty(CORRELATIVO_PROPERTY, String.class, "");
        agendaContainer.addContainerProperty(PUNTO_AGENDA_PROPERTY, String.class, "");

        agendaGrid = new Grid("Agenda", agendaContainer);
        agendaGrid.setEditorBuffered(false);
        agendaGrid.setHeightMode(HeightMode.ROW);
        agendaGrid.setHeightByRows(7);
        agendaGrid.setEditorEnabled(true);
        agendaGrid.setSelectionMode(SelectionMode.NONE);
        agendaGrid.setWidth("100%");
        agendaGrid.setDescription("DOBLE Click aqui para editar puntos de agenda.");
        agendaGrid.getColumn("#").setMaximumWidth(50);
        agendaGrid.getColumn("#").setEditable(false);    
        agendaGrid.addItemClickListener((event) -> {
            if (event != null) {
                agendaGrid.editItem(event.getItemId());
            }

        });
      
        Object itemdId2 = agendaContainer.addItem();
        agendaContainer.getContainerProperty(itemdId2, "#").setValue("1");
        agendaContainer.getContainerProperty(itemdId2, "Punto de agenda").setValue("");
        itemdId2 = agendaContainer.addItem();
        agendaContainer.getContainerProperty(itemdId2, "#").setValue("2");
        agendaContainer.getContainerProperty(itemdId2, "Punto de agenda").setValue("");
        itemdId2 = agendaContainer.addItem();
        agendaContainer.getContainerProperty(itemdId2, "#").setValue("3");
        agendaContainer.getContainerProperty(itemdId2, "Punto de agenda").setValue("");
        itemdId2 = agendaContainer.addItem();
        agendaContainer.getContainerProperty(itemdId2, "#").setValue("4");
        agendaContainer.getContainerProperty(itemdId2, "Punto de agenda").setValue("");
        itemdId2 = agendaContainer.addItem();
        agendaContainer.getContainerProperty(itemdId2, "#").setValue("5");
        agendaContainer.getContainerProperty(itemdId2, "Punto de agenda").setValue("");
        itemdId2 = agendaContainer.addItem();
        agendaContainer.getContainerProperty(itemdId2, "#").setValue("6");
        agendaContainer.getContainerProperty(itemdId2, "Punto de agenda").setValue("");
        itemdId2 = agendaContainer.addItem();
        agendaContainer.getContainerProperty(itemdId2, "#").setValue("7");
        agendaContainer.getContainerProperty(itemdId2, "Punto de agenda").setValue("");

        saveBtn = new Button("Guardar");
        saveBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        saveBtn.setIcon(FontAwesome.SAVE);
        saveBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                validarYGuardar();
            }
        });

        exitBtn = new Button("Salir");
        exitBtn.setIcon(FontAwesome.SIGN_OUT);
        exitBtn.setClickShortcut(KeyCode.ESCAPE);
//        exitBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        exitBtn.addStyleName(ValoTheme.BUTTON_SMALL);
        exitBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
        exitBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();
            }
        });
        
        resolucionesBtn = new Button("Resoluciones");        
        resolucionesBtn.setIcon(FontAwesome.LIST_OL);
        resolucionesBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {                
           
                if(!codigoVisitaInspeccion.equals("0")){                    
                    IngresoResolucionesForm resoluciones =
                            new IngresoResolucionesForm(
                                    idVisitaInspeccion,
                            codigoVisitaInspeccion
                            );
                    mainUI.addWindow(resoluciones);
                    resoluciones.center();
                    resoluciones.llenarTabla();
                }else{
                    Notification.show("Ingresar antes puntos de agenda", Notification.Type.WARNING_MESSAGE);
                }
                
            }
        });        
        resolucionesBtn.setVisible(true);
        
        tasksBtn = new Button("Tareas");
        tasksBtn.setIcon(FontAwesome.TASKS);
        tasksBtn.setWidth(150, Sizeable.UNITS_PIXELS);
        tasksBtn.setDescription("Tareas de la visita o reunión");
        tasksBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (!idVisitaInspeccion.equals("0")) {

                    InspectionTasksWindow inspectionTaskView
                            = new InspectionTasksWindow(
                                    String.valueOf(idVisitaInspeccion),
                                    String.valueOf(codigoVisitaInspeccion),
                                    String.valueOf(motivoCbx.getItemCaption(motivoCbx.getValue())),
                                    String.valueOf(centroCostoCbx.getItemCaption(centroCostoCbx.getValue())),
                                    String.valueOf(clienteCbx.getItemCaption(clienteCbx.getCaption()))
                            );
                    mainUI.addWindow(inspectionTaskView);
                    inspectionTaskView.center();
                } else {
                    Notification.show("Debe crear primero una visita !", Notification.Type.WARNING_MESSAGE);
                }
            }
        });

        visualizarBtn = new Button("Visualizar reporte");
        visualizarBtn.setIcon(FontAwesome.FILE_PDF_O);
        visualizarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                ReporteVisitasReunionesPDF reporteVisitasReunionesPDF
                        = new ReporteVisitasReunionesPDF(
                                idVisitaInspeccion,
                                codigoVisitaInspeccion,
                                String.valueOf(Utileria.getFechaDDMMYYYY_HHMM_2(fechaYHoraInicioDt.getValue())),
                                String.valueOf(Utileria.getFechaDDMMYYYY_HHMM_2(fechaYHoraFinDt.getValue())),
                                String.valueOf(motivoCbx.getValue()),
                                "",
                                String.valueOf(centroCostoCbx.getItemCaption(centroCostoCbx.getValue())),
                                String.valueOf(clienteCbx.getValue()),
                                String.valueOf(referenciaTxt.getValue()),
                                String.valueOf(lugarTxt.getValue()),
                                agendaContainer, participantesContainer);
                UI.getCurrent().addWindow(reporteVisitasReunionesPDF);
            }
        });

        cargarDocumentosBtn = new Button("Documentos");
        cargarDocumentosBtn.setDescription("Cargar y visualizar documentos de la visita");
//        cargarDocumentosBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        cargarDocumentosBtn.setIcon(FontAwesome.UPLOAD);
        cargarDocumentosBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                IngresoDocumentosVisitaInspeccion cargarDocumentos = new IngresoDocumentosVisitaInspeccion(idVisitaInspeccion);
                UI.getCurrent().addWindow(cargarDocumentos);
                cargarDocumentos.center();
            }
        });
    
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setMargin(false);
        buttonsLayout.setWidth("100%");
        buttonsLayout.addComponent(exitBtn);
        buttonsLayout.setComponentAlignment(exitBtn, Alignment.BOTTOM_LEFT);
        buttonsLayout.addComponent(saveBtn);
        buttonsLayout.setComponentAlignment(saveBtn, Alignment.BOTTOM_CENTER);        
        buttonsLayout.addComponent(resolucionesBtn);
        buttonsLayout.setComponentAlignment(resolucionesBtn, Alignment.BOTTOM_CENTER);
        buttonsLayout.addComponent(tasksBtn);
        buttonsLayout.setComponentAlignment(tasksBtn, Alignment.BOTTOM_RIGHT);
        buttonsLayout.addComponent(visualizarBtn);
        buttonsLayout.setComponentAlignment(visualizarBtn, Alignment.BOTTOM_RIGHT);
        buttonsLayout.addComponent(cargarDocumentosBtn);
        buttonsLayout.setComponentAlignment(cargarDocumentosBtn, Alignment.BOTTOM_RIGHT);

        formLayout.addComponent(idVisitaInspeccionTxt);
        formLayout.addComponent(fechaYHoraInicioDt);
        formLayout.addComponent(fechaYHoraFinDt);
        formLayout.addComponent(motivoCbx);
        formLayout.addComponent(clienteCbx);
        formLayout.addComponent(centroCostoCbx);
        formLayout.addComponent(referenciaTxt);
        formLayout.addComponent(lugarTxt);
        formLayout.addComponent(observacionesTxt);

        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setWidth("100%");
        contentLayout.setSpacing(true);
        contentLayout.setMargin(false);

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setWidth("100%");
        titleLayout.setMargin(false);
//        titleLayout.addStyleName(Runo.LAYOUT_DARKER);

        Label titleLbl = new Label("NUEVA VISITA O REUNION");
        if (!idVisitaInspeccionTxt.getValue().equals("0")) {
            titleLbl.setValue("EDITAR VISITA O REUNION # " + codigoVisita);
        }
        titleLbl.addStyleName(Runo.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.setImmediate(true);

        titleLayout.addComponent(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.TOP_CENTER);

        HorizontalLayout participantesLayout = new HorizontalLayout();
        participantesLayout.setWidth("100%");
        participantesLayout.setMargin(new MarginInfo(true, true, false, true));
        participantesLayout.setSpacing(true);
//        participantesLayout.addStyleName("rcorners2");
        participantesLayout.addComponent(agendaGrid);
        participantesLayout.setComponentAlignment(agendaGrid, Alignment.MIDDLE_LEFT);
        participantesLayout.addComponent(participantesGrid);
        participantesLayout.setComponentAlignment(participantesGrid, Alignment.MIDDLE_RIGHT);

        contentLayout.addComponent(titleLayout);
        contentLayout.setComponentAlignment(titleLayout, Alignment.TOP_CENTER);
        contentLayout.addComponent(formLayout);
        contentLayout.setComponentAlignment(formLayout, Alignment.TOP_CENTER);
        contentLayout.addComponent(participantesLayout);
        contentLayout.setComponentAlignment(participantesLayout, Alignment.TOP_CENTER);
        contentLayout.addComponent(buttonsLayout);
        contentLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);

        setContent(contentLayout);  
    }

    public void recibirParametrosImagen(String fileName, String mimeType, long peso) {
        parametro1 = fileName;
        parametro2 = mimeType;
        parametro3 = peso;
    }

    public void guardarArchivo() {
        try {

            queryString = " UPDATE visita_inspeccion SET  ";
            queryString += " Archivo = ?";
            queryString += " ,ArchivoNombre ='" + parametro1 + "'";
            queryString += " ,ArchivoTipo = '" + parametro2 + "'";
            queryString += " ,ArchivoPeso = " + parametro3;
            queryString += " WHERE IdVisitaInspeccion = " + idVisitaInspeccion;

            stPreparedQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().prepareStatement(queryString);
            stPreparedQuery.setBinaryStream(1, logoStreamResource.getStream().getStream(), logoStreamResource.getStream().getStream().available());
            stPreparedQuery.executeUpdate();

            Notification.show("Registro agregado con exito!", Notification.Type.HUMANIZED_MESSAGE);

        } catch (Exception ex) {
            System.out.println("Error al intentar insertar documento" + ex);
        }
    }

    private void fillComboCliente() {

        if (clienteCbx == null) {
            return;
        }

        queryString = "SELECT * ";
        queryString += " FROM proveedor_empresa ";
        queryString += " WHERE (N0 = 1 OR N0=7) ";
        queryString += " AND (IdEmpresa = " + empresaId;
        queryString += " ORDER BY Nombre";

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            clienteCbx.removeAllItems();
            clienteCbx.addItem(0);
            clienteCbx.setItemCaption(0, "<<ELIJA>>");
            clienteCbx.select(0);

            while (rsRecords.next()) { //  encontrado 

//System.out.println("CentroCosto=" + rsRecords.getString("IdProveedor").substring(1, 5));
                clienteCbx.addItem(rsRecords.getString("IdProveedor"));
                clienteCbx.getContainerProperty(rsRecords.getString("IdProveedor"), "CodigoCentroCosto").setValue(rsRecords.getString("IdProveedor").substring(0, 5));
                clienteCbx.setItemCaption(rsRecords.getString("IdProveedor"), "(" + rsRecords.getString("IdProveedor").substring(0, 4) + ") " + rsRecords.getString("Nombre"));
            }
        } catch (Exception ex1) {
            Notification.show("ERROR DEL SISTEMA AL BUSCAR CLIENTES", Notification.Type.ERROR_MESSAGE);
            System.out.println("ERROR AL INTENTAR BUSCAR CATALOGO CLIENTES : " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    private void fillComboCentroCosto() {

        if (centroCostoCbx == null) {
            return;
        }

        queryString = "SELECT * ";
        queryString += " FROM centro_costo ";
        queryString += " WHERE IdProyecto = " + ((SopdiUI) mainUI).sessionInformation.getStrProjectId();
        queryString += " AND IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
        queryString += " AND Inhabilitado = 0";
        queryString += " AND Grupo = 'CASAS'";

        try {
            stQuery1 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords1 = stQuery1.executeQuery(queryString);

            centroCostoCbx.removeAllItems();
            centroCostoCbx.clear();
            centroCostoCbx.addItem(0);
            centroCostoCbx.setItemCaption(0, "<<ELIJA>>");

            while (rsRecords1.next()) { //  encontrado                
                centroCostoCbx.addItem(rsRecords1.getString("IdCentroCosto")).getItemProperty("CodigoCentroCosto").setValue(rsRecords1.getString("CodigoCentroCosto"));
                centroCostoCbx.setItemCaption(rsRecords1.getString("IdCentroCosto"), rsRecords1.getString("CodigoCentroCosto"));
            }
        } catch (Exception ex1) {
            Notification.show("ERROR DEL SISTEMA AL BUSCAR CENTROS DE COSTO", Notification.Type.ERROR_MESSAGE);
            System.out.println("ERROR AL INTENTAR BUSCAR CATALOGO DE CENTROS DE COSTO : " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    private Field<?> getComboState() {

        ComboBox comboBox = new ComboBox();
        comboBox.setImmediate(true);
        comboBox.setNullSelectionAllowed(false);
        comboBox.setNewItemsAllowed(true);
        comboBox.clear();
        comboBox.addContainerProperty("Empresa", String.class, "");
        comboBox.addContainerProperty("Email", String.class, "");

        comboBox.addItem("");

        queryString = "SELECT * ";
        queryString += " FROM proveedor_empresa ";
        queryString += " WHERE IdEmpresa = " + empresaId;
        queryString += " AND EsComite = 1 ";

//        if (String.valueOf(motivoCbx.getValue()).equals("Comité técnico")) {
//            queryString += " AND Grupo = 'Comité Tecnico' ";
//        } else if (String.valueOf(motivoCbx.getValue()).equals("Reunión de obra")) {
//            queryString += " AND Grupo = 'Reunion Obra' ";
//        } else if (String.valueOf(motivoCbx.getValue()).equals("Reunión de consejo")) {
//            queryString += " AND Grupo = 'Consejo Administracion' ";
//        } else {
//            queryString += " AND Grupo <> 'Consejo Administracion' ";
//        }
        queryString += " ORDER BY Nombre";

        try {
            stQuery1 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords1 = stQuery1.executeQuery(queryString);

            while (rsRecords1.next()) { //  encontrado                
                comboBox.addItem(rsRecords1.getString("Nombre"));
                comboBox.getItem(rsRecords1.getString("Nombre")).getItemProperty("DPI").setValue(rsRecords1.getString("DPI"));
                comboBox.getItem(rsRecords1.getString("Nombre")).getItemProperty("Email").setValue(rsRecords1.getString("Email"));
            }

        } catch (Exception ex1) {
            Notification.show("ERROR DEL SISTEMA AL BUSCAR PARTICIPANTES DE REUNIONES", Notification.Type.ERROR_MESSAGE);
            System.out.println("ERROR AL INTENTAR BUSCAR CATALOGO DE PROVEEDORES (PARTICIPANTES DE REUNIONES) : " + ex1.getMessage());
            ex1.printStackTrace();
        }

        comboBox.addValueChangeListener(event -> {
            if (event != null) {
                if (event.getProperty().getValue() != null) {
                    if (participantesGrid != null) {

                        if (comboBox.getContainerDataSource().getContainerProperty(event.getProperty().getValue(), DPI_PROPERTY) != null) {
                            String dpi = (comboBox.getContainerProperty(event.getProperty().getValue(), DPI_PROPERTY).getValue() != null) ? String.valueOf( comboBox.getContainerProperty(event.getProperty().getValue(), DPI_PROPERTY).getValue()) : "";
                            String email = (comboBox.getContainerProperty(event.getProperty().getValue(), EMAIL_PROPERTY).getValue() != null) ? String.valueOf( comboBox.getContainerProperty(event.getProperty().getValue(), EMAIL_PROPERTY).getValue()) : "";

                            participantesContainer.getContainerProperty(participantesGrid.getEditedItemId(), DPI_PROPERTY).setValue(dpi);
                            participantesContainer.getContainerProperty(participantesGrid.getEditedItemId(), EMAIL_PROPERTY).setValue(email);

                        }

                    }

                }
            }
        });

        return comboBox;
    }

    public void fillData() {
        if (idVisitaInspeccionTxt.getValue().compareTo("0") == 0) {
            return;
        }

        queryString = "SELECT * ";
        queryString += " FROM  visita_inspeccion ";
        queryString += " WHERE IdVisitaInspeccion = " + idVisitaInspeccionTxt.getValue();

//System.out.println("\n\n"+queryString);
        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado

                codigoVisitaInspeccionTxt.setReadOnly(false);
                codigoVisitaInspeccionTxt.setValue(rsRecords.getString("CodigoVisita"));
                codigoVisitaInspeccionTxt.setReadOnly(true);
                fechaYHoraInicioDt.setValue(rsRecords.getDate("FechaYHoraInicio"));
                fechaYHoraFinDt.setValue(rsRecords.getDate("FechaYHorafin"));
                motivoCbx.select(rsRecords.getString("Motivo"));
                clienteCbx.select(rsRecords.getString("IdCliente"));

                centroCostoCbx.select(rsRecords.getInt("IdCentroCosto"));
                referenciaTxt.setValue(rsRecords.getString("Referencia"));
                lugarTxt.setValue(rsRecords.getString("Lugar"));
                observacionesTxt.setValue(rsRecords.getString("Observaciones"));


                queryString = "SELECT * ";
                queryString += " FROM  visita_inspeccion_participante ";
                queryString += " WHERE IdVisitaInspeccion = " + idVisitaInspeccionTxt.getValue();


                stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                rsRecords = stQuery.executeQuery(queryString);

                if(rsRecords.next()) {
                    do{
                        Object itemid = participantesContainer.addItem();
                        participantesContainer.getContainerProperty(itemid, NOMBRE_PROPERTY).setValue(rsRecords.getString("Nombre"));
                        participantesContainer.getContainerProperty(itemid, DPI_PROPERTY).setValue(rsRecords.getString("DPI"));
                        participantesContainer.getContainerProperty(itemid, EMAIL_PROPERTY).setValue(rsRecords.getString("Email"));
                    }while ((rsRecords.next()));
                }

                queryString = "SELECT * ";
                queryString += " FROM  visita_inspeccion_agenda ";
                queryString += " WHERE IdVisitaInspeccion = " + idVisitaInspeccionTxt.getValue();


                stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                rsRecords = stQuery.executeQuery(queryString);

                if(rsRecords.next()) {
                    do{
                        Object itemid = agendaContainer.addItem();
                        agendaContainer.getContainerProperty(itemid, "Punto de Agenda").setValue(rsRecords.getString("PuntoAgenda"));
                    }while ((rsRecords.next()));
                }


                motivoCbx.focus();

            } else {
                Notification.show("Error, no se encotró registro!", Notification.Type.ERROR_MESSAGE);
                close();
            }
        } catch (Exception ex) {
            Logger.getLogger(ProjectTaskView.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros : " + ex.getMessage());
            Notification.show("Error al intentar leer registros..!", Notification.Type.ERROR_MESSAGE);
        }

    }

    private void validarYGuardar() {

        if (String.valueOf(motivoCbx.getValue()).equals("Visita de cliente")
                || String.valueOf(motivoCbx.getValue()).equals("Reunión con residente")) {
            if (String.valueOf(clienteCbx.getValue()).equals("0")) {
                Notification.show("Por favor, elija al cliente.", Notification.Type.WARNING_MESSAGE);
                clienteCbx.focus();
                return;
            }
        }

        try {

            if (idVisitaInspeccionTxt.getValue().equals("0")) {
                String codigo = String.valueOf(motivoCbx.getItem(motivoCbx.getValue()).getItemProperty("Codigo").getValue());
                SimpleDateFormat df = new SimpleDateFormat("ddMMyy");
                codigoVisita = String.format("%02d", Integer.valueOf(((SopdiUI) mainUI).sessionInformation.getStrProjectId()).intValue())
                        + String.format("%02d",Integer.valueOf(codigo))
                        + df.format(fechaYHoraInicioDt.getValue());

                queryString = "SELECT CodigoVisita";
                queryString += " FROM  visita_inspeccion ";
                queryString += " WHERE IdProyecto = " + ((SopdiUI) mainUI).sessionInformation.getStrProjectId();
                queryString += " AND   CodigoVisita Like '" + codigoVisita + "%'";
                queryString += " ORDER BY CodigoVisita DESC";
                queryString += " LIMIT 1";

                //System.out.println("\n\n"+queryString);
                stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                rsRecords = stQuery.executeQuery(queryString);

                if (rsRecords.next()) { //  encontrado
                    codigoVisita = rsRecords.getString("CodigoVisita");

                    codigoVisita = codigoVisita.substring(0, 10) + String.format("%02d", Integer.valueOf(codigoVisita.substring(10, 12)) + 1);
                } else {
                    codigoVisita += "01";
                }

                System.out.println("codigo visita" + codigoVisita);

                queryString = "INSERT INTO visita_inspeccion ";
                queryString += "(IdProyecto, CodigoVisita, FechaYHoraInicio, FechaYHoraFin, ";
                queryString += " Motivo, IdCliente, IdCentroCosto, Referencia, ";
                queryString += " Participante1,Participante1Empresa,Participante1Email,";
                queryString += " Participante2,Participante2Empresa,Participante2Email,";
                queryString += " Participante3,Participante3Empresa,Participante3Email,";
                queryString += " Participante4,Participante4Empresa,Participante4Email,";
                queryString += " Participante5,Participante5Empresa,Participante5Email,";
                queryString += " Participante6,Participante6Empresa,Participante6Email,";
                queryString += " Participante7,Participante7Empresa,Participante7Email,";
                queryString += " PuntoAgenda1,PuntoAgenda2,PuntoAgenda3,PuntoAgenda4,";
                queryString += " PuntoAgenda5,PuntoAgenda6,PuntoAgenda7,";
                queryString += " CreadoUsuario, CreadoFechaYHora,Lugar,Observaciones) ";
                queryString += " VALUES (";
                queryString += "  " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrProjectId();
                queryString += ",'" + codigoVisita + "'";
                queryString += ",'" + Utileria.getFechaYYYYMMDDHHMMSS(fechaYHoraInicioDt.getValue()) + "'";
                queryString += ",'" + Utileria.getFechaYYYYMMDDHHMMSS(fechaYHoraFinDt.getValue()) + "'";
                queryString += ",'" + String.valueOf(motivoCbx.getItemCaption(motivoCbx.getValue())) + "'";
                queryString += ", " + String.valueOf(clienteCbx.getValue());
                queryString += ", " + String.valueOf(centroCostoCbx.getValue());
                queryString += ",'" + referenciaTxt.getValue() + "'";
                queryString += ",'" + participantesContainer.getContainerProperty(1, "Nombre").getValue() + "'";
                queryString += ",'" + participantesContainer.getContainerProperty(1, "Empresa").getValue() + "'";
                queryString += ",'" + participantesContainer.getContainerProperty(1, "Email").getValue() + "'";
                queryString += ",'" + participantesContainer.getContainerProperty(2, "Nombre").getValue() + "'";
                queryString += ",'" + participantesContainer.getContainerProperty(2, "Empresa").getValue() + "'";
                queryString += ",'" + participantesContainer.getContainerProperty(2, "Email").getValue() + "'";
                queryString += ",'" + participantesContainer.getContainerProperty(3, "Nombre").getValue() + "'";
                queryString += ",'" + participantesContainer.getContainerProperty(3, "Empresa").getValue() + "'";
                queryString += ",'" + participantesContainer.getContainerProperty(3, "Email").getValue() + "'";
                queryString += ",'" + participantesContainer.getContainerProperty(4, "Nombre").getValue() + "'";
                queryString += ",'" + participantesContainer.getContainerProperty(4, "Empresa").getValue() + "'";
                queryString += ",'" + participantesContainer.getContainerProperty(4, "Email").getValue() + "'";
                queryString += ",'" + participantesContainer.getContainerProperty(5, "Nombre").getValue() + "'";
                queryString += ",'" + participantesContainer.getContainerProperty(5, "Empresa").getValue() + "'";
                queryString += ",'" + participantesContainer.getContainerProperty(5, "Email").getValue() + "'";
                queryString += ",'" + participantesContainer.getContainerProperty(6, "Nombre").getValue() + "'";
                queryString += ",'" + participantesContainer.getContainerProperty(6, "Empresa").getValue() + "'";
                queryString += ",'" + participantesContainer.getContainerProperty(6, "Email").getValue() + "'";
                queryString += ",'" + participantesContainer.getContainerProperty(7, "Nombre").getValue() + "'";
                queryString += ",'" + participantesContainer.getContainerProperty(7, "Empresa").getValue() + "'";
                queryString += ",'" + participantesContainer.getContainerProperty(7, "Email").getValue() + "'";
                queryString += ",'" + agendaContainer.getContainerProperty(1, "Punto de agenda").getValue() + "'";
                queryString += ",'" + agendaContainer.getContainerProperty(2, "Punto de agenda").getValue() + "'";
                queryString += ",'" + agendaContainer.getContainerProperty(3, "Punto de agenda").getValue() + "'";
                queryString += ",'" + agendaContainer.getContainerProperty(4, "Punto de agenda").getValue() + "'";
                queryString += ",'" + agendaContainer.getContainerProperty(5, "Punto de agenda").getValue() + "'";
                queryString += ",'" + agendaContainer.getContainerProperty(6, "Punto de agenda").getValue() + "'";
                queryString += ",'" + agendaContainer.getContainerProperty(7, "Punto de agenda").getValue() + "'";
                queryString += ", " + ((SopdiUI) mainUI).sessionInformation.getStrUserId(); //creado usuario
                queryString += ",current_timestamp"; // creado fechayhora
                queryString += ",'" + lugarTxt.getValue() + "'";
                queryString += ",'" + observacionesTxt.getValue() + "'";
                queryString += ")";
            } else {
                queryString = "UPDATE visita_inspeccion SET";
                queryString += " IdCliente = " + String.valueOf(clienteCbx.getValue());
                queryString += ",FechaYHoraInicio = '" + Utileria.getFechaYYYYMMDDHHMMSS(fechaYHoraInicioDt.getValue()) + "'";
                queryString += ",FechaYHoraFin = '" + Utileria.getFechaYYYYMMDDHHMMSS(fechaYHoraFinDt.getValue()) + "'";
                queryString += ",Motivo = '" + String.valueOf(motivoCbx.getItemCaption(motivoCbx.getValue())) + "'";
                queryString += ",IdCentroCosto = " + String.valueOf(centroCostoCbx.getValue());
                queryString += ",Referencia = '" + referenciaTxt.getValue() + "'";
                queryString += ",Participante1 = '" + participantesContainer.getContainerProperty(1, "Nombre").getValue() + "'";
                queryString += ",Participante1Empresa = '" + participantesContainer.getContainerProperty(1, "Empresa").getValue() + "'";
                queryString += ",Participante1Email = '" + participantesContainer.getContainerProperty(1, "Email").getValue() + "'";
                queryString += ",Participante2 = '" + participantesContainer.getContainerProperty(2, "Nombre").getValue() + "'";
                queryString += ",Participante2Empresa = '" + participantesContainer.getContainerProperty(2, "Empresa").getValue() + "'";
                queryString += ",Participante2Email = '" + participantesContainer.getContainerProperty(2, "Email").getValue() + "'";
                queryString += ",Participante3 = '" + participantesContainer.getContainerProperty(3, "Nombre").getValue() + "'";
                queryString += ",Participante3Empresa = '" + participantesContainer.getContainerProperty(3, "Empresa").getValue() + "'";
                queryString += ",Participante3Email = '" + participantesContainer.getContainerProperty(3, "Email").getValue() + "'";
                queryString += ",Participante4 = '" + participantesContainer.getContainerProperty(4, "Nombre").getValue() + "'";
                queryString += ",Participante4Empresa = '" + participantesContainer.getContainerProperty(4, "Empresa").getValue() + "'";
                queryString += ",Participante4Email = '" + participantesContainer.getContainerProperty(4, "Email").getValue() + "'";
                queryString += ",Participante5 = '" + participantesContainer.getContainerProperty(5, "Nombre").getValue() + "'";
                queryString += ",Participante5Empresa = '" + participantesContainer.getContainerProperty(5, "Empresa").getValue() + "'";
                queryString += ",Participante5Email = '" + participantesContainer.getContainerProperty(5, "Email").getValue() + "'";
                queryString += ",Participante6 = '" + participantesContainer.getContainerProperty(6, "Nombre").getValue() + "'";
                queryString += ",Participante6Empresa = '" + participantesContainer.getContainerProperty(6, "Empresa").getValue() + "'";
                queryString += ",Participante6Email = '" + participantesContainer.getContainerProperty(6, "Email").getValue() + "'";
                queryString += ",Participante7 = '" + participantesContainer.getContainerProperty(7, "Nombre").getValue() + "'";
                queryString += ",Participante7Empresa = '" + participantesContainer.getContainerProperty(7, "Empresa").getValue() + "'";
                queryString += ",Participante7Email = '" + participantesContainer.getContainerProperty(7, "Email").getValue() + "'";

                queryString += ",PuntoAgenda1 = '" + agendaContainer.getContainerProperty(1, "Punto de agenda").getValue() + "'";
                queryString += ",PuntoAgenda2 = '" + agendaContainer.getContainerProperty(2, "Punto de agenda").getValue() + "'";
                queryString += ",PuntoAgenda3 = '" + agendaContainer.getContainerProperty(3, "Punto de agenda").getValue() + "'";
                queryString += ",PuntoAgenda4 = '" + agendaContainer.getContainerProperty(4, "Punto de agenda").getValue() + "'";
                queryString += ",PuntoAgenda5 = '" + agendaContainer.getContainerProperty(5, "Punto de agenda").getValue() + "'";
                queryString += ",PuntoAgenda6 = '" + agendaContainer.getContainerProperty(6, "Punto de agenda").getValue() + "'";
                queryString += ",PuntoAgenda7 = '" + agendaContainer.getContainerProperty(7, "Punto de agenda").getValue() + "'";
                queryString += ",Lugar = '" + lugarTxt.getValue() + "'";
                queryString += ",Observaciones = '" + observacionesTxt.getValue() + "'";
                queryString += " WHERE IdVisitaInspeccion = " + idVisitaInspeccionTxt.getValue();
            }

 //           System.out.println("\nQUERY=" + queryString + "\n");

            if (idVisitaInspeccionTxt.getValue().equals("0")) { //nuevo
                stPreparedQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().prepareStatement(queryString, Statement.RETURN_GENERATED_KEYS);
                stPreparedQuery.executeUpdate();
                rsRecords = stPreparedQuery.getGeneratedKeys();

                rsRecords.next();

                idVisitaInspeccionTxt.setReadOnly(false);
                idVisitaInspeccionTxt.setValue(rsRecords.getString(1));
                idVisitaInspeccionTxt.setReadOnly(true);

                ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Registro creado exitosamente!!!,  quiere ingresar las tareas de esta visita?",
                        "SI", "NO", new ConfirmDialog.Listener() {

                    public void onClose(ConfirmDialog dialog) {
                        if (dialog.isConfirmed()) {
                            InspectionTasksWindow inspectionTaskView
                                    = new InspectionTasksWindow(
                                            idVisitaInspeccionTxt.getValue(),
                                            codigoVisita,
                                            String.valueOf(motivoCbx.getValue()),
                                            String.valueOf(centroCostoCbx.getValue()),
                                            clienteCbx.getItemCaption(clienteCbx.getValue())
                                    );
                            mainUI.addWindow(inspectionTaskView);
                            inspectionTaskView.center();
                        }
                    }
                });
                close();
            } else {
                stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                stQuery.executeUpdate(queryString);

                Notification.show("Registro modificado!!!", Notification.Type.WARNING_MESSAGE);
            }

            ((VisitasView) (mainUI.getNavigator().getCurrentView())).fillVisitasTable();

//            close();
            /**
             * MyEmailMessanger eMail = new MyEmailMessanger(((SopdiUI)
             * mainUI).mainWindow); eMail.avisoVentaNueva(
             * String.valueOf(idContacto), ((SopdiUI)
             * mainUI).sessionInformation.getStrUserSupervisorEMail(),
             * ((SopdiUI) mainUI).sessionInformation.getStrUserName(),
             * clienteCbx.getValue());
             *
             */
        } catch (Exception ex1) {
            Notification.show("ERROR FATAL DEL SISTEMA");
            System.out.println("ERROR AL INTENTAR GUARDAR LOS CAMBIOS : " + ex1.getMessage());
            ex1.printStackTrace();
        }

    }
}
