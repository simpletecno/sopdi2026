package com.simpletecno.sopdi.operativo;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.SelectionEvent;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.sql.ResultSet;
import java.sql.Statement;

/**
 *
 * @author user
 */
public class ProgramaTrabajoCheckView extends VerticalLayout implements View {

    ComboBox empresaCbx;
    String empresa;

    // Tabla Tarea
    static final String ID_PROPERTY = "ID";
    static final String IDCC_PROPERTY = "IDCC";
    static final String PROJECT_PROPERTY = "Project";
    static final String IDEX_PROPERTY = "IDEX";
    static final String DESCRIPCION_PROPERTY = "Descripción";
    static final String FECHAINICIO_PROPERTY = "Inicio";
    static final String FECHAFINAL_PROPERTY = "Fin";
    static final String FECHAINICIOREAL_PROPERTY = "Inicio Real";
    static final String FECHAFINAREAL_PROPERTY = "Fin Real";
    static final String INSTRUCCIONES_PROPERTY = "Instrucciones";
    static final String ESTILO_PROPERTY = "ESTILO";
    static final String ID_NIVEL_PROPERTY = "IDNIVEL";
    static final String CODIGO_PLANOS_PROPERTY = "Códigos de planos";

    // Tabla Tiempos
    static final String TIPO_PROPERTY = "Tipo";
    static final String ENCARGADO_PROPERTY = "Encargado";
    static final String RAZON_INICIO_PROPERTY = "Razon Inicio";
    static final String INICIO_PROPERTY = "Inicio";
    static final String RAZON_FIN_PROPERTY = "Razon Fin";
    static final String FIN_PROPERTY = "Fin";

    public IndexedContainer idexContainer = new IndexedContainer();
    public IndexedContainer tiemposContainer = new IndexedContainer();

    Grid idexGrid;
    Grid tiemposGrid;
    Label tiempoLabel;

    Button idexBtn;
    Button calificacionesBtn;
    Button printTasksBtn;

    UI mainUI;
    Statement stQuery;
    ResultSet rsRecords;
    String queryString;

    public ProgramaTrabajoCheckView() {

        this.mainUI = UI.getCurrent();
        setWidth("100%");
        setMargin(false);
        setSpacing(true);
        setResponsive(true);

        Label titleLbl = new Label("VERIFICAR TAREAS");
        if (mainUI.getPage().getBrowserWindowWidth() >= 736) {
            titleLbl.addStyleName(ValoTheme.LABEL_H2);
        }
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h1_custom");

        empresaCbx = new ComboBox("Empresa:");
        if (mainUI.getPage().getBrowserWindowWidth() >= 736) {
            empresaCbx.setWidth("400px");
        }
        empresaCbx.setInvalidAllowed(false);
        empresaCbx.setNewItemsAllowed(false);
        empresaCbx.setTextInputAllowed(false);
        empresaCbx.setNullSelectionAllowed(false);
        if (mainUI.getPage().getBrowserWindowWidth() >= 736) {
            empresaCbx.addStyleName(ValoTheme.COMBOBOX_HUGE);
        }

        llenarComboEmpresa();

        empresaCbx.addValueChangeListener(event -> {
            empresa = String.valueOf(event.getProperty().getValue());
            fillIdexGrid();
        });

        empresa = String.valueOf(empresaCbx.getValue());

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

        createIdexGrid();

        // Si es un telemfono o un jefe
        if(mainUI.getPage().getBrowserWindowWidth() <= 736 || ((SopdiUI) mainUI).sessionInformation.getStrUserProfileName().contains("JEFE")) {
            HorizontalLayout reportLayout = new HorizontalLayout();
            reportLayout.addStyleName("rcorners3");
            reportLayout.setWidth("95%");
            reportLayout.setHeightUndefined();
            reportLayout.setResponsive(true);
            reportLayout.setSpacing(true);
            reportLayout.setMargin(false);

            tiempoLabel = new Label("<b>" + ((SopdiUI)mainUI).sessionInformation.getStrUserProfileName() + ": " + ((SopdiUI)mainUI).sessionInformation.getStrUserFullName() + "</b>\n"
                    ,ContentMode.HTML);
            reportLayout.addComponent(tiempoLabel);
            reportLayout.setComponentAlignment(tiempoLabel, Alignment.TOP_CENTER);
            addComponent(reportLayout);
        }
        else {
            createTiemposGrid();
        }

        fillIdexGrid();
    }

    public void llenarComboEmpresa() {
        queryString = " SELECT * FROM contabilidad_empresa";
        queryString += " WHERE IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) { //  encontrado
                empresaCbx.addItem(rsRecords.getString("IdEmpresa"));
                empresaCbx.setItemCaption(rsRecords.getString("IdEmpresa"), rsRecords.getString("Empresa"));
            }
            rsRecords.first();

            empresaCbx.select(rsRecords.getString("IdEmpresa"));

        } catch (Exception ex1) {
            System.out.println("Error al llenar Combo empresas: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void createTiemposGrid() {

        VerticalLayout reportLayout = new VerticalLayout();
        reportLayout.addStyleName("rcorners3");
        reportLayout.setWidth("95%");
        reportLayout.setHeightUndefined();
        reportLayout.setResponsive(true);
        reportLayout.setSpacing(true);
        reportLayout.setMargin(false);

        HorizontalLayout layoutGrid = new HorizontalLayout();
        layoutGrid.setWidth("100%");
        layoutGrid.setHeightUndefined();
        layoutGrid.setSpacing(true);
        layoutGrid.setMargin(false);

        tiemposContainer.addContainerProperty(TIPO_PROPERTY, String.class, "");
        tiemposContainer.addContainerProperty(ENCARGADO_PROPERTY, String.class, "");
        tiemposContainer.addContainerProperty(RAZON_INICIO_PROPERTY, String.class, "");
        tiemposContainer.addContainerProperty(INICIO_PROPERTY, String.class, "");
        tiemposContainer.addContainerProperty(RAZON_FIN_PROPERTY, String.class, "");
        tiemposContainer.addContainerProperty(FIN_PROPERTY, String.class, "");

        tiemposGrid = new Grid("IDEX ", tiemposContainer);
        tiemposGrid.setWidth("95%");
        tiemposGrid.setImmediate(true);
        tiemposGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        tiemposGrid.setDescription("Seleccione un registro.");
        tiemposGrid.setHeightMode(HeightMode.ROW);
        tiemposGrid.setHeightByRows(5);
        tiemposGrid.setResponsive(true);
        tiemposGrid.setResponsive(true);
        tiemposGrid.setEditorBuffered(false);
//        tiemposGrid.setSizeFull();


        layoutGrid.addComponent(tiemposGrid);
        layoutGrid.setComponentAlignment(tiemposGrid, Alignment.BOTTOM_CENTER);
        reportLayout.addComponent(layoutGrid);

        addComponent(reportLayout);
    }

    public void creatRolTiempo(String idex){
        String maestroList = "";
        String supervisorList = "";

        String colorInicio = "black";
        String colorFin = "black";
        String fechaInicio = "";
        String fechaFin = "";
        String nombreJefe = "No asignado";

        queryString = "SELECT pti.*, cce.tipo, cce.Nombre ";
        queryString += " FROM plan_trabajo_idex pti";
        queryString += " INNER JOIN project_tarea pt ON pti.IDEX = pt.IDEX";
        queryString += " INNER JOIN centro_costo_encargado cce ON cce.CodigoCentroCosto = pt.IdCentroCosto";
        queryString += " WHERE pti.IDEX = " + idex;
        queryString += " AND cce.Eliminado = 0";
        try {
            rsRecords = stQuery.executeQuery(queryString);
            if(rsRecords.next()){
                if(rsRecords.getDate("FechaInicioSegunSupervisor") != null){
                    fechaInicio = Utileria.getFechaDDMMYYYY(rsRecords.getDate("FechaInicioSegunSupervisor"));
                    colorInicio = "green";
                }else if (rsRecords.getDate("FechaInicioSegunMaestro") != null){
                    fechaInicio = Utileria.getFechaDDMMYYYY(rsRecords.getDate("FechaInicioSegunMaestro"));
                    colorInicio = "green";
                }else if((rsRecords.getDate("FechaInicioSegunJefe") != null)){
                    fechaInicio = Utileria.getFechaDDMMYYYY(rsRecords.getDate("FechaInicioSegunJefe"));
                    colorInicio = "blue";
                }else{
                    fechaInicio = Utileria.getFechaDDMMYYYY(rsRecords.getDate("FechaInicioPlaneada"));
                }

                if(rsRecords.getDate("FechaFinSegunSupervisor") != null){
                    fechaFin = Utileria.getFechaDDMMYYYY(rsRecords.getDate("FechaFinSegunSupervisor"));
                    colorFin = "green";
                }else if (rsRecords.getDate("FechaFinSegunMaestro") != null){
                    fechaFin = Utileria.getFechaDDMMYYYY(rsRecords.getDate("FechaFinSegunMaestro"));
                    colorFin = "green";
                }else if((rsRecords.getDate("FechaFinSegunJefe") != null)){
                    fechaFin = Utileria.getFechaDDMMYYYY(rsRecords.getDate("FechaFinSegunJefe"));
                    colorFin = "blue";
                }else{
                    fechaFin = Utileria.getFechaDDMMYYYY(rsRecords.getDate("FechaFinPlaneada"));
                }
                // Lista MAESTROS DE OBRA y SUPERVISORES
                do{
                    if(rsRecords.getString("Tipo").toUpperCase().contains("MAESTRO")){
                        maestroList += rsRecords.getString("Nombre") + ", ";
                    }else{
                        supervisorList += rsRecords.getString("Nombre") + ", ";
                    }
                }while(rsRecords.next());
            }

            queryString = "SELECT p.Nombre";
            queryString += " FROM plan_trabajo_idex pti";
            queryString += " INNER JOIN plan_trabajo_idex_rh ptir ON pti.Id = ptir.IdPlanTrabajoIdex";
            queryString += " INNER JOIN proveedor p ON p.IDProveedor = ptir.IdEmpleado";
            queryString += " WHERE pti.IDEX = " + idex;
            queryString += " AND ptir.EsJefe = 'SI'";

            rsRecords = stQuery.executeQuery(queryString);
            if(rsRecords.next()) {
                nombreJefe = rsRecords.getString("Nombre");
            }

        } catch (Exception ex) {
            System.out.println("NO SE ENCONTRO IDEX: " + ex);
            ex.printStackTrace();
            Notification.show("NO SE ENCONTRO IDEX: " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }


        tiempoLabel.setValue(
                        "<b>" + ((SopdiUI)mainUI).sessionInformation.getStrUserProfileName() + ": " + ((SopdiUI)mainUI).sessionInformation.getStrUserFullName() + "</b>\n"+
                        "<ul>"+
                        " <li><b>Fecha Inicio: </b> <b style='color:" + colorInicio + "'>" + fechaInicio + "</b></li>\n"+
                        " <li><b>Fecha Fin: </b> <b style='color:" + colorFin + "'>" + fechaFin + "</b></li>\n"+
                        "</ul>" +
                        "<div><b>Jefe: </b>" + nombreJefe + "</div>\n"+
                        "<div><b>Maestro(s) de Obra: </b>" + maestroList.substring(0, (maestroList.length()-2)) + "</div>\n" +
                        "<div><b>Supervisor(es): </b>" + supervisorList.substring(0, (supervisorList.length()-2)) + "</div>\n");


    }

    public void createIdexGrid() {

        VerticalLayout reportLayout = new VerticalLayout();
        reportLayout.addStyleName("rcorners3");
        reportLayout.setWidth("95%");
        reportLayout.setHeightUndefined();
        reportLayout.setResponsive(true);
        reportLayout.setSpacing(true);
        reportLayout.setMargin(false);

        HorizontalLayout layoutGrid = new HorizontalLayout();
        layoutGrid.setWidth("100%");
        layoutGrid.setHeightUndefined();
        layoutGrid.setSpacing(true);
        layoutGrid.setMargin(false);

        idexContainer.addContainerProperty(ID_PROPERTY, String.class, null);
        idexContainer.addContainerProperty(IDCC_PROPERTY, String.class, "");
        idexContainer.addContainerProperty(PROJECT_PROPERTY, String.class, "");
        idexContainer.addContainerProperty(IDEX_PROPERTY, String.class, "");
        idexContainer.addContainerProperty(DESCRIPCION_PROPERTY, String.class, "");
        idexContainer.addContainerProperty(FECHAINICIO_PROPERTY, String.class, "");
        idexContainer.addContainerProperty(FECHAFINAL_PROPERTY, String.class, "");
        idexContainer.addContainerProperty(FECHAINICIOREAL_PROPERTY, String.class, "");
        idexContainer.addContainerProperty(FECHAFINAREAL_PROPERTY, String.class, "");
        idexContainer.addContainerProperty(INSTRUCCIONES_PROPERTY, String.class, "");
        idexContainer.addContainerProperty(CODIGO_PLANOS_PROPERTY, String.class, "");
        idexContainer.addContainerProperty(ESTILO_PROPERTY, String.class, "");
        idexContainer.addContainerProperty(ID_NIVEL_PROPERTY, String.class, "");

        idexGrid = new Grid("TAREAS (IDEX) DEL CENTRO DE COSTO", idexContainer);
        idexGrid.setWidth("95%");
        idexGrid.setImmediate(true);
        idexGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        idexGrid.setDescription("Seleccione un registro.");
        idexGrid.setHeightMode(HeightMode.ROW);
        idexGrid.setHeightByRows(10);
        idexGrid.setResponsive(true);
        idexGrid.setResponsive(true);
        idexGrid.setEditorBuffered(false);
//        idexGrid.setSizeFull();

        idexGrid.getColumn(ID_PROPERTY).setHidable(true).setHidden(true);

        if(mainUI.getPage().getBrowserWindowWidth() > 736) {
            idexGrid.getColumn(IDEX_PROPERTY).setExpandRatio(1);
            idexGrid.getColumn(DESCRIPCION_PROPERTY).setExpandRatio(3);
            idexGrid.getColumn(FECHAINICIO_PROPERTY).setExpandRatio(2);
            idexGrid.getColumn(FECHAFINAL_PROPERTY).setExpandRatio(2);
            idexGrid.getColumn(FECHAINICIOREAL_PROPERTY).setExpandRatio(2);
            idexGrid.getColumn(FECHAFINAREAL_PROPERTY).setExpandRatio(2);
        }

        if(mainUI.getPage().getBrowserWindowWidth() <= 736) {
            idexGrid.getColumn(FECHAINICIOREAL_PROPERTY).setHidable(true).setHidden(true);
            idexGrid.getColumn(FECHAFINAREAL_PROPERTY).setHidable(true).setHidden(true);
            idexGrid.getColumn(CODIGO_PLANOS_PROPERTY).setHidable(true).setHidden(true);
            idexGrid.getColumn(ESTILO_PROPERTY).setHidable(true).setHidden(true);
            idexGrid.getColumn(ID_NIVEL_PROPERTY).setHidable(true).setHidden(true);
        }

        idexGrid.addSelectionListener((SelectionEvent.SelectionListener) event -> {
            if (idexGrid.getSelectedRow() != null) {
                if(mainUI.getPage().getBrowserWindowWidth() <= 736 || ((SopdiUI) mainUI).sessionInformation.getStrUserProfileName().contains("JEFE")) {
                    creatRolTiempo((String) idexContainer.getContainerProperty(idexGrid.getSelectedRow(), IDEX_PROPERTY).getValue());
                }
                else {
                    fillTiemposGrid((String) idexContainer.getContainerProperty(idexGrid.getSelectedRow(), IDEX_PROPERTY).getValue());
                }
            }
        });

        Grid.HeaderRow filterRow = idexGrid.appendHeaderRow();

        Grid.HeaderCell cell0 = filterRow.getCell(IDCC_PROPERTY);

        TextField filterField0 = new TextField();
        filterField0.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField0.setInputPrompt("Filtrar");
        filterField0.setColumns(8);

        filterField0.addTextChangeListener(change -> {
            idexContainer.removeContainerFilters(IDCC_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                idexContainer.addContainerFilter(
                        new SimpleStringFilter(IDCC_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell0.setComponent(filterField0);

        Grid.HeaderCell cell1 = filterRow.getCell(IDEX_PROPERTY);

        TextField filterField1 = new TextField();
        filterField1.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField1.setInputPrompt("Filtrar");
        filterField1.setColumns(8);

        filterField1.addTextChangeListener(change -> {
            idexContainer.removeContainerFilters(IDEX_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                idexContainer.addContainerFilter(
                        new SimpleStringFilter(IDEX_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell1.setComponent(filterField1);

        Grid.HeaderCell cell2 = filterRow.getCell(PROJECT_PROPERTY);

        TextField filterField2 = new TextField();
        filterField2.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField2.setInputPrompt("Filtrar");
        filterField2.setColumns(8);

        filterField2.addTextChangeListener(change -> {
            idexContainer.removeContainerFilters(PROJECT_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                idexContainer.addContainerFilter(
                        new SimpleStringFilter(PROJECT_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell2.setComponent(filterField2);

        layoutGrid.addComponent(idexGrid);
        layoutGrid.setComponentAlignment(idexGrid, Alignment.TOP_CENTER);
        reportLayout.addComponent(layoutGrid);

        idexBtn = new Button("DATOS DE TAREA");
        idexBtn.setIcon(FontAwesome.TASKS);
        idexBtn.setWidth(190, Sizeable.UNITS_PIXELS);
        idexBtn.setDescription("SELECCIONE UNA TAREA PARA VERIFICACIONES");
        idexBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {

                if (idexGrid.getSelectedRow() == null || idexContainer.size() == 0) {
                    Notification.show("Por favor seleccione una TAREA!", Notification.Type.WARNING_MESSAGE);
                } else {
                    ProgramaTrabajoCheckForm planTrabajoCheckForm = new ProgramaTrabajoCheckForm(
                            String.valueOf(idexContainer.getContainerProperty(idexGrid.getSelectedRow(), ID_PROPERTY).getValue()),
                            String.valueOf(idexContainer.getContainerProperty(idexGrid.getSelectedRow(), IDCC_PROPERTY).getValue()),
                            String.valueOf(idexContainer.getContainerProperty(idexGrid.getSelectedRow(), IDEX_PROPERTY).getValue()),
                            String.valueOf(idexContainer.getContainerProperty(idexGrid.getSelectedRow(), DESCRIPCION_PROPERTY).getValue()),
                            String.valueOf(idexContainer.getContainerProperty(idexGrid.getSelectedRow(), FECHAINICIO_PROPERTY).getValue()),
                            String.valueOf(idexContainer.getContainerProperty(idexGrid.getSelectedRow(), FECHAFINAL_PROPERTY).getValue()),
                            String.valueOf(idexContainer.getContainerProperty(idexGrid.getSelectedRow(), FECHAINICIOREAL_PROPERTY).getValue()),
                            String.valueOf(idexContainer.getContainerProperty(idexGrid.getSelectedRow(), FECHAFINAREAL_PROPERTY).getValue()),
                            String.valueOf(idexContainer.getContainerProperty(idexGrid.getSelectedRow(), INSTRUCCIONES_PROPERTY).getValue()),
                            String.valueOf(idexContainer.getContainerProperty(idexGrid.getSelectedRow(), CODIGO_PLANOS_PROPERTY).getValue()),
                            String.valueOf(idexContainer.getContainerProperty(idexGrid.getSelectedRow(), ESTILO_PROPERTY).getValue()),
                            String.valueOf(idexContainer.getContainerProperty(idexGrid.getSelectedRow(), ID_NIVEL_PROPERTY).getValue())
                    );
                    planTrabajoCheckForm.center();
                    planTrabajoCheckForm.setModal(true);
                    UI.getCurrent().addWindow(planTrabajoCheckForm);
                }
            }
        });

        calificacionesBtn = new Button("VER CALIFICACIONES");
        calificacionesBtn.addStyleName(ValoTheme.BUTTON_LINK);
        calificacionesBtn.setWidth(220, Sizeable.UNITS_PIXELS);
        calificacionesBtn.addClickListener((Button.ClickListener) event -> {
            if (idexGrid.getSelectedRow() == null || idexContainer.size() == 0) {
                Notification.show("Por favor seleccione una TAREA!", Notification.Type.WARNING_MESSAGE);
            } else {
                ProgramaTrabajoCalificacionWindow programaTrabajoCalificacionWindow = new ProgramaTrabajoCalificacionWindow(
                        String.valueOf(idexContainer.getContainerProperty(idexGrid.getSelectedRow(), ID_PROPERTY).getValue()),
                        String.valueOf(idexContainer.getContainerProperty(idexGrid.getSelectedRow(), IDCC_PROPERTY).getValue()),
                        String.valueOf(idexContainer.getContainerProperty(idexGrid.getSelectedRow(), IDEX_PROPERTY).getValue()),
                        String.valueOf(idexContainer.getContainerProperty(idexGrid.getSelectedRow(), DESCRIPCION_PROPERTY).getValue())
                );
                programaTrabajoCalificacionWindow.center();
                programaTrabajoCalificacionWindow.setModal(true);
                UI.getCurrent().addWindow(programaTrabajoCalificacionWindow);
            }
        });

        printTasksBtn = new Button("IMPRIMIR TAREAS");
        printTasksBtn.setIcon(FontAwesome.FILE_PDF_O);
        printTasksBtn.addStyleName(ValoTheme.BUTTON_LINK);
        printTasksBtn.setDescription("IMPRIMIR TODAS LAS TAREAS EN PDF.");
        printTasksBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (idexContainer.size() == 0) {
                    Notification.show("NO HAY TAREAS!", Notification.Type.WARNING_MESSAGE);
                } else {
                    ProgramaTrabajoTaskPDF programaTrabajoTaskPDF = new ProgramaTrabajoTaskPDF(
                            idexContainer);
                    UI.getCurrent().addWindow(programaTrabajoTaskPDF);

                }
            }
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.addComponents(idexBtn, calificacionesBtn, printTasksBtn);
        buttonsLayout.setComponentAlignment(idexBtn, Alignment.BOTTOM_CENTER);
        buttonsLayout.setComponentAlignment(calificacionesBtn, Alignment.BOTTOM_CENTER);
        buttonsLayout.setComponentAlignment(printTasksBtn, Alignment.BOTTOM_RIGHT);

        reportLayout.addComponent(buttonsLayout);
        reportLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);

        addComponent(reportLayout);
    }

    public void fillIdexGrid() {

        idexContainer.removeAllItems();

        idexGrid.setCaption("IDEX (TAREAS) " + ((SopdiUI) mainUI).sessionInformation.getStrUserProfileName());

        String idProveedor = "0";

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            String queryString = " SELECT IdProveedor FROM proveedor WHERE IdUsuario = " + ((SopdiUI) mainUI).sessionInformation.getStrUserId();

            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado
                idProveedor = rsRecords.getString("IdProveedor");
            }

            queryString = "SELECT PTI.*, PTA.Idex, PTA.Descripcion, PTA.FechaInicio, PTA.FechaFin, ";
            queryString += " PTA.RH1, PTA.RH2, RH3, RH4, PTA.NUMERO, PTA.IdCentroCosto, PTA.CodigoEstilo, PTA.IdNivel, PTA.CodPlanos";
            queryString += " FROM plan_trabajo_idex PTI ";
            queryString += " INNER JOIN project_tarea PTA ON PTA.Idex = PTI.Idex ";
            queryString += " INNER JOIN project PJ ON PJ.Id = PTA.IdProject";
            queryString += " INNER JOIN centro_costo CC ON CC.CodigoCentroCosto = PTA.IdCentrocosto";
            queryString += " INNER JOIN plan_trabajo_idex_rh PTIR ON PTI.Id = PTIR.IdPlanTrabajoIdex ";
System.out.println("**** profile=" +((SopdiUI) mainUI).sessionInformation.getStrUserProfileName());
            if(((SopdiUI) mainUI).sessionInformation.getStrUserProfileName().contains("MAESTRO") || ((SopdiUI) mainUI).sessionInformation.getStrUserProfileName().contains("SUPERVISOR")) {
                queryString += "INNER JOIN (SELECT PRV.Nombre, CCE.CodigoCentroCosto FROM proveedor PRV INNER JOIN centro_costo_encargado CCE ON PRV.IdProveedor = CCE.IdProveedor WHERE CCE.Eliminado = 0 AND PRV.IDProveedor = " + idProveedor + ") PRV ON PRV.CodigoCentroCosto = CC.CodigoCentroCosto";
            }
            else if(((SopdiUI) mainUI).sessionInformation.getStrUserProfileName().contains("JEFE")) {
                queryString += " AND PTI.Id IN (Select PTIRH.IdPlanTrabajoIdex FROM plan_trabajo_idex_rh PTIRH WHERE PTIRH.IdEmpleado = " + idProveedor + " AND PTIRH.EsJefe = 'SI')";
//2023-08-19                queryString += " AND ISNULL(PTI.FechaFinSegunSupervisor) = 1";
            }
            queryString += " AND PJ.Estatus = 'ACTIVO'";
            queryString += " AND PTIR.EsJefe = 'SI'";
            queryString += " AND PTI.IdProject = PJ.Id";
            queryString += " ORDER BY PTA.FechaInicio";

System.out.println("queryCheck="+queryString);

            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado

                Object itemId;

                do {
                    String fechaInicio = "";
                    String fechaFin = "";
                    itemId = idexContainer.addItem();
                    idexContainer.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords.getString("Id"));
                    idexContainer.getContainerProperty(itemId, PROJECT_PROPERTY).setValue(rsRecords.getString("Numero"));
                    idexContainer.getContainerProperty(itemId, IDCC_PROPERTY).setValue(rsRecords.getString("IdCentroCosto"));
                    idexContainer.getContainerProperty(itemId, IDEX_PROPERTY).setValue(rsRecords.getString("Idex"));
                    idexContainer.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(rsRecords.getString("Descripcion"));
                    idexContainer.getContainerProperty(itemId, FECHAINICIO_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("FechaInicio")));
                    idexContainer.getContainerProperty(itemId, FECHAFINAL_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("FechaFin")));

                    if(rsRecords.getDate("FechaInicioSegunSupervisor") != null){
                        fechaInicio = Utileria.getFechaDDMMYYYY(rsRecords.getDate("FechaInicioSegunSupervisor"));
                    }else if (rsRecords.getDate("FechaInicioSegunMaestro") != null){
                        fechaInicio = Utileria.getFechaDDMMYYYY(rsRecords.getDate("FechaInicioSegunMaestro"));
                    }else if((rsRecords.getDate("FechaInicioSegunJefe") != null)){
                        fechaInicio = Utileria.getFechaDDMMYYYY(rsRecords.getDate("FechaInicioSegunJefe"));
                    }

                    if(rsRecords.getDate("FechaFinSegunSupervisor") != null){
                        fechaFin = Utileria.getFechaDDMMYYYY(rsRecords.getDate("FechaFinSegunSupervisor"));
                    }else if (rsRecords.getDate("FechaFinSegunMaestro") != null){
                        fechaFin = Utileria.getFechaDDMMYYYY(rsRecords.getDate("FechaFinSegunMaestro"));
                    }else if((rsRecords.getDate("FechaFinSegunJefe") != null)){
                        fechaFin = Utileria.getFechaDDMMYYYY(rsRecords.getDate("FechaFinSegunJefe"));
                    }

                    idexContainer.getContainerProperty(itemId, FECHAINICIOREAL_PROPERTY).setValue(fechaInicio);
                    idexContainer.getContainerProperty(itemId, FECHAFINAREAL_PROPERTY).setValue(fechaFin);
                    idexContainer.getContainerProperty(itemId, INSTRUCCIONES_PROPERTY).setValue(rsRecords.getString("Instrucciones"));
                    idexContainer.getContainerProperty(itemId, ESTILO_PROPERTY).setValue(rsRecords.getString("CodigoEstilo"));
                    idexContainer.getContainerProperty(itemId, ID_NIVEL_PROPERTY).setValue(rsRecords.getString("IdNivel"));
                    idexContainer.getContainerProperty(itemId, CODIGO_PLANOS_PROPERTY).setValue(rsRecords.getString("CodPlanos"));

                } while (rsRecords.next());
            }
            idexGrid.select(null);
        } catch (Exception ex) {
            System.out.println("Error al listar tabla DE IDEX (PROGRAMA DE TAREAS) : " + ex);
            ex.printStackTrace();
            Notification.show("ERROR DE LECTURA DE BASE DE DATOS PROGRAMA DE TAREAS: " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }

    public void fillTiemposGrid(String idex) {

        tiemposContainer.removeAllItems();

        tiemposGrid.setCaption("IDEX: " + idex);

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            queryString =  "SELECT PTI.*, P.Nombre, P.IDProveedor, PTIRH.EsJefe, PTIRH.Cargo, ";
            queryString +=      "IFNULL(P.IDProveedor = PTI.IdMaestroInicio , FALSE) as MI, IFNULL(P.IDProveedor = PTI.IdMaestroFin, FALSE) as MF, " ;
            queryString +=      "IFNULL(P.IDProveedor = PTI.IdSupervisorInicio , FALSE) as SI, IFNULL(P.IDProveedor = PTI.IdSupervisorFin , FALSE) as SF ";
            queryString += "FROM plan_trabajo_idex PTI ";
            queryString += "INNER JOIN plan_trabajo_idex_rh PTIRH ON PTI.id = PTIRH.IdPlanTrabajoIdex ";
            queryString += "INNER JOIN proveedor P ON PTIRH.IdEmpleado = P.IDProveedor ";
            queryString += "WHERE PTI.IDEX =" + idex;

            System.out.println("queryCheck=" + queryString);

            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado

                Object itemId;
                // Tabla Tiempos
                do {
                    if(rsRecords.getString("EsJefe").equals("SI")){
                        itemId = tiemposContainer.addItem();
                        tiemposContainer.getContainerProperty(itemId, TIPO_PROPERTY).setValue(rsRecords.getString("Cargo"));
                        tiemposContainer.getContainerProperty(itemId, ENCARGADO_PROPERTY).setValue(rsRecords.getString("Nombre"));
                        tiemposContainer.getContainerProperty(itemId, RAZON_INICIO_PROPERTY).setValue("-------------------");
                        if(rsRecords.getDate("FechaInicioSegunJefe") != null){
                            tiemposContainer.getContainerProperty(itemId, INICIO_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("FechaInicioSegunJefe")));
                        } else {
                            tiemposContainer.getContainerProperty(itemId, INICIO_PROPERTY).setValue("");
                        }
                        tiemposContainer.getContainerProperty(itemId, RAZON_FIN_PROPERTY).setValue("-------------------");
                        if(rsRecords.getDate("FechaFinSegunJefe") != null){
                            tiemposContainer.getContainerProperty(itemId, FIN_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("FechaFinSegunJefe")));
                        } else {
                            tiemposContainer.getContainerProperty(itemId, FIN_PROPERTY).setValue("");
                        }
                    }else if (rsRecords.getString("Cargo").toUpperCase().contains("MAESTRO")){
                        itemId = tiemposContainer.addItem();
                        tiemposContainer.getContainerProperty(itemId, TIPO_PROPERTY).setValue(rsRecords.getString("Cargo") + " | JEFE" );
                        tiemposContainer.getContainerProperty(itemId, ENCARGADO_PROPERTY).setValue(rsRecords.getString("Nombre"));
                        if(rsRecords.getBoolean("MI")){
                            tiemposContainer.getContainerProperty(itemId, RAZON_INICIO_PROPERTY).setValue(rsRecords.getString("RazonCambioInicioMaestro"));
                            tiemposContainer.getContainerProperty(itemId, INICIO_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("FechaInicioSegunMaestro")));
                        }else{
                            tiemposContainer.getContainerProperty(itemId, RAZON_INICIO_PROPERTY).setValue("-------------------");
                            tiemposContainer.getContainerProperty(itemId, INICIO_PROPERTY).setValue("");
                        }

                        if(rsRecords.getBoolean("MF")){
                            tiemposContainer.getContainerProperty(itemId, RAZON_FIN_PROPERTY).setValue(rsRecords.getString("RazonCambioFinMaestro"));
                            tiemposContainer.getContainerProperty(itemId, FIN_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("FechaFinSegunMaestro")));
                        }else{
                            tiemposContainer.getContainerProperty(itemId, RAZON_FIN_PROPERTY).setValue("-------------------");
                            tiemposContainer.getContainerProperty(itemId, FIN_PROPERTY).setValue("");
                        }

                    }else if (rsRecords.getString("Cargo").toUpperCase().contains("SUPERVISOR")) {
                        itemId = tiemposContainer.addItem();
                        tiemposContainer.getContainerProperty(itemId, TIPO_PROPERTY).setValue(rsRecords.getString("Cargo"));
                        tiemposContainer.getContainerProperty(itemId, ENCARGADO_PROPERTY).setValue(rsRecords.getString("Nombre"));
                        if(rsRecords.getBoolean("SI")){
                            tiemposContainer.getContainerProperty(itemId, RAZON_INICIO_PROPERTY).setValue(rsRecords.getString("RazonCambioInicioSupervisor"));
                            tiemposContainer.getContainerProperty(itemId, INICIO_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("FechaInicioSegunSupervisor")));
                        }else{
                            tiemposContainer.getContainerProperty(itemId, RAZON_INICIO_PROPERTY).setValue("-------------------");
                            tiemposContainer.getContainerProperty(itemId, INICIO_PROPERTY).setValue("");
                        }

                        if(rsRecords.getBoolean("SF")){
                            tiemposContainer.getContainerProperty(itemId, RAZON_FIN_PROPERTY).setValue(rsRecords.getString("RazonCambioFinSupervisor"));
                            tiemposContainer.getContainerProperty(itemId, FIN_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("FechaFinSegunSupervisor")));
                        }else{
                            tiemposContainer.getContainerProperty(itemId, RAZON_FIN_PROPERTY).setValue("-------------------");
                            tiemposContainer.getContainerProperty(itemId, FIN_PROPERTY).setValue("");
                        }
                    }
                } while (rsRecords.next());
            }
            tiemposGrid.select(null);
        } catch (Exception ex) {
            System.out.println("Error al listar tabla DE IDEX (PROGRAMA DE TAREAS) : " + ex);
            ex.printStackTrace();
            Notification.show("ERROR DE LECTURA DE BASE DE DATOS PROGRAMA DE TAREAS: " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - VERIFICAR TAREAS TRABAJO");
    }
}
