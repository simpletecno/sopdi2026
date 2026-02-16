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
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.dialogs.ConfirmDialog;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author user
 */
public class ProgramaTrabajoView extends VerticalLayout implements View {

    static final String ID_PROPERTY = "ID";
    static final String IDCC_PROPERTY = "IDCCosto";
    static final String PROJECT_PROPERTY = "Project";
    static final String IDEX_PROPERTY = "IDEX";
    static final String DESCRIPCION_PROPERTY = "Descripción";
    static final String FECHAINICIO_PROPERTY = "Prg Inicio";
    static final String FECHAFINAL_PROPERTY = "Prg Fin";
    static final String FECHAINICIOREAL_PROPERTY = "Real Inicio";
    static final String FECHAFINAREAL_PROPERTY = "Real Fin";
    static final String DURACION_PROPERTY = "Duración";
    static final String DURACIONREAL_PROPERTY = "Duración Real";
    static final String DIFERENCIA_PROPERTY = "Diferencia";
    static final String RH1_PROPERTY = "RH1";
    static final String RH2_PROPERTY = "RH2";
    static final String RH1_REAL_PROPERTY = "Rh1 Real";
    static final String RH2_REAL_PROPERTY = "Rh2 Real";
    static final String RH1_DIFERENCIA_PROPERTY = "Rh1 Diferencia";
    static final String RH2_DIFERENCIA_PROPERTY = "Rh2 Diferencia";
    static final String INSTRUCCIONES_PROPERTY = "Instrucciones";
    static final String ESTATUS_PROPERTY = "Estatus";
    static final String IDPROJECT_PROPERTY = "IdProject";

    static final String IDEMPLEADO_PROPERTY = "IdEmpleado";
    static final String NOMBRE_PROPERTY = "Nombre";
    static final String CARGO_PROPERTY = "Cargo";
    static final String ESJEFE_PROPERTY = "ES JEFE";
    static final String OPTIONS_PROPERTY = "-";

    VerticalLayout mainLayout = new VerticalLayout();

    Label rightLbl;
    ComboBox projectCbx;
    DateField delDt;
    DateField alDt;
    Button mostrarPrograrmacionBtn;
    Button asignarRHBtn;

    public IndexedContainer idexContainer = new IndexedContainer();
    Grid idexGrid;

    public IndexedContainer rhContainer = new IndexedContainer();
    Grid rhGrid;

    public IndexedContainer rhContainerMaestroSupervidor = new IndexedContainer();

    Button rhNoAsignadoBtn;
    Button instruccionesIdexBtn;
    Button printIdexBtn;

    UI mainUI;
    Statement stQuery;
    ResultSet rsRecords;
    Statement stQuery1;
    Statement stQuery2;
    ResultSet rsRecords1;
    ResultSet rsRecords2;
    String queryString;
    PreparedStatement stPreparedQuery;

    public ProgramaTrabajoView() {
        this.mainUI = UI.getCurrent();

        setResponsive(true);
        setCaption("Programación de trabajo " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrProjectName());
//        setSizeFull();

        mainLayout.setWidth("100%");
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);
        mainLayout.setResponsive(true);

        addComponent(mainLayout);

        Label leftLbl = new Label(((SopdiUI) UI.getCurrent()).sessionInformation.getStrProjectName());
        leftLbl.addStyleName(ValoTheme.LABEL_H2);
        leftLbl.setSizeUndefined();
        leftLbl.addStyleName("h1_custom");

        rightLbl = new Label("Programación de trabajo");
        rightLbl.addStyleName(ValoTheme.LABEL_H2);
        rightLbl.setSizeUndefined();
        rightLbl.addStyleName("h1_custom");

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setResponsive(true);
        titleLayout.setWidth("100%");
        titleLayout.setMargin(false);
        titleLayout.addComponents(leftLbl, rightLbl);
        titleLayout.setComponentAlignment(leftLbl, Alignment.TOP_LEFT);
        titleLayout.setComponentAlignment(rightLbl, Alignment.TOP_RIGHT);

        mainLayout.addComponent(titleLayout);
        mainLayout.setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setSpacing(true);

        projectCbx = new ComboBox("Project : ");
        projectCbx.setWidth("30em");
        projectCbx.setFilteringMode(FilteringMode.CONTAINS);
        projectCbx.setInvalidAllowed(false);
        projectCbx.setNullSelectionAllowed(false);
        projectCbx.setNewItemsAllowed(false);
        projectCbx.addValueChangeListener(event -> {
//            fillIdexGrid();
        });
        llenarComboProject();

        //TODO : PRIMER DIA DE LOS PROJECTS ACTIVOS

        delDt = new DateField("FECHA DE INICIO DEL :");
        delDt.setDateFormat("dd/MMM/yyyy");
        delDt.setValue(getPrimeraFechaInicio());
        delDt.setWidth("10em");
        delDt.addValueChangeListener(event -> {
            if (delDt.getValue().after(alDt.getValue())) {
                Notification.show("Fecha inicio es menor que fecha final, revise!!", Notification.Type.WARNING_MESSAGE);
            }
        });
        LocalDate now = LocalDate.now();
        ZoneId z = ZoneId.of( "America/Guatemala" );
        ZonedDateTime zdt = now.plusDays(15).atStartOfDay( z );
        alDt = new DateField("FECHA DE INICIO AL :");
        alDt.setDateFormat("dd/MMM/yyyy");
        alDt.setValue(java.util.Date.from(zdt.toInstant()));
        alDt.setWidth("10em");
        alDt.addValueChangeListener(event -> {
            if (alDt.getValue().before(delDt.getValue())) {
                Notification.show("Fecha final es menor que fecha inicial, revise!!", Notification.Type.WARNING_MESSAGE);
            }
        });

        mostrarPrograrmacionBtn = new Button("PROGRAMACION");
        mostrarPrograrmacionBtn.setIcon(FontAwesome.REFRESH);
        mostrarPrograrmacionBtn.addStyleName(ValoTheme.BUTTON_LINK);
        mostrarPrograrmacionBtn.setWidth(220, Sizeable.UNITS_PIXELS);
        mostrarPrograrmacionBtn.setDescription("Mostrar los idex y sus RH en el rango de fechas.");
        mostrarPrograrmacionBtn.addClickListener((Button.ClickListener) event -> {
            fillIdexGrid(true);
       });

        asignarRHBtn = new Button("ASIGNAR RH");
        asignarRHBtn.setIcon(FontAwesome.USERS);
        asignarRHBtn.addStyleName(ValoTheme.BUTTON_LINK);
        asignarRHBtn.setWidth(220, Sizeable.UNITS_PIXELS);
        asignarRHBtn.setDescription("Asignar RH1 y RH2 a todos los IDEX pendientes de iniciar.");
        asignarRHBtn.addClickListener((Button.ClickListener) event -> {
                asignarRH();
        });

        rhNoAsignadoBtn = new Button("RH NO ASIGNADO");
        rhNoAsignadoBtn.setIcon(FontAwesome.PAPER_PLANE);
        rhNoAsignadoBtn.addStyleName(ValoTheme.BUTTON_LINK);
        rhNoAsignadoBtn.setWidth(220, Sizeable.UNITS_PIXELS);
        rhNoAsignadoBtn.setDescription("REPORTE DE RH NO ASIGNADO A NINGUNA TAREA EN EL RANGO DE FECHAS.");
        rhNoAsignadoBtn.addClickListener((Button.ClickListener) event -> {
            ProgramaTrabajoEmpleadoLibreForm planTrabajoEmpleadoLibreForm
                    = new ProgramaTrabajoEmpleadoLibreForm(
                            alDt
            );
            mainUI.addWindow(planTrabajoEmpleadoLibreForm);
            planTrabajoEmpleadoLibreForm.center();
        });

        headerLayout.addComponents(delDt, alDt, mostrarPrograrmacionBtn, asignarRHBtn, rhNoAsignadoBtn);
        headerLayout.setComponentAlignment(mostrarPrograrmacionBtn, Alignment.BOTTOM_CENTER);
        headerLayout.setComponentAlignment(asignarRHBtn, Alignment.BOTTOM_CENTER);
        headerLayout.setComponentAlignment(rhNoAsignadoBtn, Alignment.BOTTOM_CENTER);

        mainLayout.addComponent(headerLayout);

        createIdexGrid();

        createRhGrid();

        fillIdexGrid(false);
    }

    private Date getPrimeraFechaInicio() { //segun los projects
        queryString = "SELECT IFNULL(MIN(pt.FECHAINICIO), CURRENT_DATE()) PRIMERAFECHA ";
        queryString += "FROM project_tarea pt ";
        queryString += "INNER JOIN project p ON p.Id  = pt.IdProject ";
        queryString += "WHERE  p.Estatus  = 'ACTIVO' ";
        queryString += "AND pt.FechaRealFin  <> '0000-00-00' ";
        queryString += "AND (pt.RH1  + pt.RH2) > 0 ";
        queryString += "AND pt.IdCentroCosto <> '' ";
        queryString += "AND pt.IdCentroCosto <> '0' ";
        queryString += "AND pt.ModoTarea  = 'SI' ";

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado
                return(rsRecords.getDate("PRIMERAFECHA"));
            }
        }
        catch (Exception ex1) {
            Notification.show("ERROR DEL SISTEMA AL BUSCAR PRIMERA FECHA DE PROJECTS", Notification.Type.ERROR_MESSAGE);
            System.out.println("ERROR AL INTENTAR BUSCAR PRIMERA FECHA DE PROJECTS: " + ex1.getMessage());
            ex1.printStackTrace();
        }
        return new java.util.Date();
    }

//    private boolean calificacionesBloqueada() {
//        boolean bloqueada = false;
//
//        queryString = "Select calificacionesBloqueda ";
//        queryString += " From proyecto ";
//        queryString += " Where IdProyecto = " + ((SopdiUI)UI.getCurrent()).sessionInformation.getStrProjectId();
//
//        try {
//            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
//            rsRecords = stQuery.executeQuery(queryString);
//
//            if (rsRecords.next()) { //  encontrado
//                bloqueada = (rsRecords.getInt("calificacionesBloqueda") == 1 ? true : false);
//            }
//        }
//        catch (Exception ex1) {
//            Notification.show("ERROR DEL SISTEMA AL BUSCAR PROYECTO", Notification.Type.ERROR_MESSAGE);
//            System.out.println("ERROR AL INTENTAR BUSCAR PROYECTO: " + ex1.getMessage());
//            ex1.printStackTrace();
//        }
//        return bloqueada;
//    }

//    private void setButtonBehavior() {
//        if(calificacionesBloqueada()) {
//            calificacionesBtn.setData(1);
//            calificacionesBtn.setCaption("DESBLOQUEAR calificaciones");
//            calificacionesBtn.setIcon(FontAwesome.CHECK);
//        }
//        else {
//            calificacionesBtn.setData(0);
//            calificacionesBtn.setCaption("BLOQUEAR calificaciones");
//            calificacionesBtn.setIcon(FontAwesome.STOP);
//        }
//    }
//
    private void llenarComboProject() {
        String queryString = "";

        queryString = "Select *";
        queryString += " From  project";
        queryString += " WHERE Estatus = 'ACTIVO'";
        queryString += " Order By Numero";

        try {

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado

                do {
                    projectCbx.addItem(rsRecords.getInt("Id"));
                    projectCbx.setItemCaption(rsRecords.getInt("Id"),
                            rsRecords.getString("Numero")
                                    + " " + rsRecords.getString("Descripcion")
                                    + " " + rsRecords.getString("CreadoFecha"));

                } while (rsRecords.next());
            }
        } catch (Exception ex) {
            Logger.getLogger(ProjectTaskView.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de projects : " + ex.getMessage());
            Notification.show("Error al intentar leer registros de projects..!", Notification.Type.ERROR_MESSAGE);
        }

    }

    private void createIdexGrid() {

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

        idexContainer.addContainerProperty(ID_PROPERTY, String.class, "");
        idexContainer.addContainerProperty(IDCC_PROPERTY, String.class, "NO TIENE");
        idexContainer.addContainerProperty(IDEX_PROPERTY, String.class, "NO TIENE");
        idexContainer.addContainerProperty(PROJECT_PROPERTY, String.class, "");
        idexContainer.addContainerProperty(DESCRIPCION_PROPERTY, String.class, "");
        idexContainer.addContainerProperty(FECHAINICIO_PROPERTY, String.class, "");
        idexContainer.addContainerProperty(FECHAFINAL_PROPERTY, String.class, "");
        idexContainer.addContainerProperty(DURACION_PROPERTY, Integer.class, 0);
        idexContainer.addContainerProperty(ESTATUS_PROPERTY, String.class, "");
        idexContainer.addContainerProperty(FECHAINICIOREAL_PROPERTY, String.class, "");
        idexContainer.addContainerProperty(FECHAFINAREAL_PROPERTY, String.class, "");
        idexContainer.addContainerProperty(DURACIONREAL_PROPERTY, Integer.class, 0);
        idexContainer.addContainerProperty(DIFERENCIA_PROPERTY, Integer.class, 0);
        idexContainer.addContainerProperty(RH1_PROPERTY, Integer.class, 0);
        idexContainer.addContainerProperty(RH2_PROPERTY, Integer.class, 0);
        idexContainer.addContainerProperty(RH1_REAL_PROPERTY, Integer.class, 0);
        idexContainer.addContainerProperty(RH2_REAL_PROPERTY, Integer.class, 0);
        idexContainer.addContainerProperty(RH1_DIFERENCIA_PROPERTY, Integer.class, 0);
        idexContainer.addContainerProperty(RH2_DIFERENCIA_PROPERTY, Integer.class, 0);
        idexContainer.addContainerProperty(INSTRUCCIONES_PROPERTY, String.class, "");
        idexContainer.addContainerProperty(IDPROJECT_PROPERTY, String.class, "");

        idexGrid = new Grid("", idexContainer);
        idexGrid.setWidth("100%");
        idexGrid.setImmediate(true);
        idexGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        idexGrid.setDescription("Seleccione un registro.");
        idexGrid.setHeightMode(HeightMode.ROW);
        idexGrid.setHeightByRows(7);
        idexGrid.setSizeFull();
//        idexGrid.getColumn(INSTRUCCIONES_PROPERTY).setEditorField(getComboState());
//        idexGrid.getColumn(PLANOS_PROPERTY).setEditorField(getEditTextRazon());
        idexGrid.addSelectionListener(new SelectionEvent.SelectionListener() {
            @Override
            public void select(SelectionEvent event
            ) {
                if (idexGrid.getSelectedRow() != null) {
                    fillRhGridPlan(idexGrid.getSelectedRow());
                }
            }
        });

        idexGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if (   DURACION_PROPERTY.equals(cellReference.getPropertyId())
                || DURACIONREAL_PROPERTY.equals(cellReference.getPropertyId())
                || DIFERENCIA_PROPERTY.equals(cellReference.getPropertyId())
                || RH1_PROPERTY.equals(cellReference.getPropertyId())
                || RH2_PROPERTY.equals(cellReference.getPropertyId())
                || RH1_REAL_PROPERTY.equals(cellReference.getPropertyId())
                || RH2_REAL_PROPERTY.equals(cellReference.getPropertyId())
            ) {
                return "centeralign";
            } else if(RH1_DIFERENCIA_PROPERTY.equals(cellReference.getPropertyId())) {
                if(Integer.valueOf(String.valueOf(cellReference.getValue())).intValue() < 0) {
                    return "centeralignred";
                }
                else {
                    return "centeralign";
                }
            } else if(RH2_DIFERENCIA_PROPERTY.equals(cellReference.getPropertyId())) {
                if(Integer.valueOf(String.valueOf(cellReference.getValue())).intValue() < 0) {
                    return "centeralignred";
                }
                else {
                    return "centeralign";
                }
            }
            else {
                return null;
            }
        });

        idexGrid.getColumn(ID_PROPERTY).setHidable(true).setHidden(true).setExpandRatio(1);
        idexGrid.getColumn(IDCC_PROPERTY).setExpandRatio(1);
        idexGrid.getColumn(IDEX_PROPERTY).setExpandRatio(1);
        idexGrid.getColumn(DESCRIPCION_PROPERTY).setExpandRatio(4);
        idexGrid.getColumn(FECHAINICIO_PROPERTY).setExpandRatio(2);
        idexGrid.getColumn(FECHAFINAL_PROPERTY).setExpandRatio(2);
        idexGrid.getColumn(ESTATUS_PROPERTY).setExpandRatio(3);
        idexGrid.getColumn(DURACION_PROPERTY).setExpandRatio(1);
        idexGrid.getColumn(FECHAINICIOREAL_PROPERTY).setExpandRatio(2);
        idexGrid.getColumn(FECHAFINAREAL_PROPERTY).setExpandRatio(2);
        idexGrid.getColumn(DURACIONREAL_PROPERTY).setExpandRatio(1);
        idexGrid.getColumn(DIFERENCIA_PROPERTY).setExpandRatio(1);
        idexGrid.getColumn(RH1_PROPERTY).setExpandRatio(1);
        idexGrid.getColumn(RH2_PROPERTY).setExpandRatio(1);
        idexGrid.getColumn(RH1_REAL_PROPERTY).setExpandRatio(1);
        idexGrid.getColumn(RH2_REAL_PROPERTY).setExpandRatio(1);
        idexGrid.getColumn(RH1_DIFERENCIA_PROPERTY).setExpandRatio(1);
        idexGrid.getColumn(RH2_DIFERENCIA_PROPERTY).setExpandRatio(1);
        idexGrid.getColumn(INSTRUCCIONES_PROPERTY).setExpandRatio(2);

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

        instruccionesIdexBtn = new Button("INSTRUCCIONES");
        instruccionesIdexBtn.setIcon(FontAwesome.BUILDING);
        instruccionesIdexBtn.addStyleName(ValoTheme.BUTTON_LINK);
        instruccionesIdexBtn.setDescription("SELECCIONE UN REGISTRO PARA ACTUALIZAR LAS INSTRUCCIONES");
        instruccionesIdexBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {

                if (idexGrid.getSelectedRow() == null || idexContainer.size() == 0) {
                    Notification.show("Por favor seleccione el IDEX!", Notification.Type.WARNING_MESSAGE);
                } else {
                    ProgramaTrabajoIdexInstruccionesForm programaTrabajoIdexInstruccionesForm = new ProgramaTrabajoIdexInstruccionesForm(
                            idexContainer,
                            idexGrid.getSelectedRow(),
                            String.valueOf(idexContainer.getContainerProperty(idexGrid.getSelectedRow(), ID_PROPERTY).getValue()),
                            String.valueOf(idexContainer.getContainerProperty(idexGrid.getSelectedRow(), IDCC_PROPERTY).getValue()),
                            String.valueOf(idexContainer.getContainerProperty(idexGrid.getSelectedRow(), IDEX_PROPERTY).getValue()),
                            String.valueOf(idexContainer.getContainerProperty(idexGrid.getSelectedRow(), DESCRIPCION_PROPERTY).getValue()),
                            String.valueOf(idexContainer.getContainerProperty(idexGrid.getSelectedRow(), INSTRUCCIONES_PROPERTY).getValue())
                    );
                    programaTrabajoIdexInstruccionesForm.center();
                    programaTrabajoIdexInstruccionesForm.setModal(true);
                    UI.getCurrent().addWindow(programaTrabajoIdexInstruccionesForm);
                }
            }
        });

        printIdexBtn = new Button("IMPRIMIR IDEX");
        printIdexBtn.setIcon(FontAwesome.FILE_PDF_O);
        printIdexBtn.addStyleName(ValoTheme.BUTTON_LINK);
        printIdexBtn.setDescription("SELECCIONE UN IDEX PARA IMPIRIMIR PDF.");
        printIdexBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {

                if (idexGrid.getSelectedRow() == null || idexContainer.size() == 0) {
                    Notification.show("Por favor seleccione el IDEX!", Notification.Type.WARNING_MESSAGE);
                } else {
                    ProgramaTrabajoIdexPDF programaTrabaIdexPDF = new ProgramaTrabajoIdexPDF(
                            idexGrid.getSelectedRow(),
                            idexContainer,
                            rhContainer,
                            String.valueOf(idexContainer.getContainerProperty(idexGrid.getSelectedRow(), INSTRUCCIONES_PROPERTY).getValue())
                    );
                    UI.getCurrent().addWindow(programaTrabaIdexPDF);
                }
            }
        });
        printIdexBtn.setVisible(false); // 09/Agosto/2023

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setWidth("100%");
        buttonsLayout.addComponent(instruccionesIdexBtn);
        buttonsLayout.setComponentAlignment(instruccionesIdexBtn, Alignment.BOTTOM_CENTER);
        buttonsLayout.addComponent(printIdexBtn);
        buttonsLayout.setComponentAlignment(printIdexBtn, Alignment.BOTTOM_CENTER);

        reportLayout.addComponent(buttonsLayout);
        reportLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);

        mainLayout.addComponent(reportLayout);
        mainLayout.setComponentAlignment(reportLayout, Alignment.MIDDLE_CENTER);
    }

    public void createRhGrid() {

        VerticalLayout reportLayout = new VerticalLayout();
        reportLayout.addStyleName("rcorners3");
        reportLayout.setWidth("100%");
        reportLayout.setResponsive(true);
        reportLayout.setSpacing(true);
        reportLayout.setMargin(false);
//        reportLayout.setHeightUndefined();

        HorizontalLayout layoutGrid = new HorizontalLayout();
        layoutGrid.setWidth("100%");
        layoutGrid.setSpacing(true);
        layoutGrid.setMargin(false);

        rhContainer.addContainerProperty(ID_PROPERTY, String.class, "");
        rhContainer.addContainerProperty(IDEMPLEADO_PROPERTY, String.class, "");
        rhContainer.addContainerProperty(NOMBRE_PROPERTY, String.class, "");
        rhContainer.addContainerProperty(CARGO_PROPERTY, String.class, "");
        rhContainer.addContainerProperty(ESJEFE_PROPERTY, String.class, "");
        rhContainer.addContainerProperty(OPTIONS_PROPERTY,   MenuBar.class, null);

        rhGrid = new Grid("", rhContainer);
        rhGrid.setWidth("100%");
        rhGrid.setImmediate(true);
        rhGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        rhGrid.setDescription("Seleccione un registro.");
        rhGrid.setHeightMode(HeightMode.ROW);
        rhGrid.setHeightByRows(5);
        rhGrid.setResponsive(true);
        rhGrid.setEditorBuffered(false);
//        rhGrid.setSizeFull();

        rhGrid.getColumn(IDEMPLEADO_PROPERTY).setExpandRatio(1);
        rhGrid.getColumn(NOMBRE_PROPERTY).setExpandRatio(4);
        rhGrid.getColumn(CARGO_PROPERTY).setExpandRatio(1);
        rhGrid.getColumn(ESJEFE_PROPERTY).setExpandRatio(1);

        rhGrid.getColumn(ID_PROPERTY).setHidden(true).setHidable(true);

        layoutGrid.addComponent(rhGrid);
        layoutGrid.setComponentAlignment(rhGrid, Alignment.TOP_CENTER);
        reportLayout.addComponent(layoutGrid);

        rhContainerMaestroSupervidor.addContainerProperty(IDEMPLEADO_PROPERTY, String.class, "");
        rhContainerMaestroSupervidor.addContainerProperty(NOMBRE_PROPERTY, String.class, "");
        rhContainerMaestroSupervidor.addContainerProperty(CARGO_PROPERTY, String.class, "");

        Button addEmployeeBtn = new Button("Agregar RH al programa");
        addEmployeeBtn.setIcon(FontAwesome.PLUS);
        addEmployeeBtn.addStyleName(ValoTheme.BUTTON_LINK);
        addEmployeeBtn.setDescription("Agregar un RH1 o RH2 a este programa");
        addEmployeeBtn.addListener((Button.ClickListener) (Button.ClickEvent event) -> {
                    if (idexContainer.size() == 0) {
                        Notification notif = new Notification("No hay PROGRAMA.",
                                Notification.Type.WARNING_MESSAGE);
                        notif.setDelayMsec(1500);
                        notif.setPosition(Position.MIDDLE_CENTER);
                        notif.setIcon(FontAwesome.WARNING);
                        notif.show(Page.getCurrent());
                    } else if (idexGrid.getSelectedRow() == null) {
                        Notification notif = new Notification("Por favor elija un programa.",
                                Notification.Type.WARNING_MESSAGE);
                        notif.setDelayMsec(1500);
                        notif.setPosition(Position.MIDDLE_CENTER);
                        notif.setIcon(FontAwesome.WARNING);
                        notif.show(Page.getCurrent());
                    } else if (String.valueOf(idexContainer.getContainerProperty(idexGrid.getSelectedRow(), ESTATUS_PROPERTY).getValue()).equals("INACTIVO")) {
                        Notification notif = new Notification("PROGRAM CON ESTATUS INACTIVO, NO SE PUEDEN CAMBIAR.",
                                Notification.Type.WARNING_MESSAGE);
                        notif.setDelayMsec(1500);
                        notif.setPosition(Position.MIDDLE_CENTER);
                        notif.setIcon(FontAwesome.WARNING);
                        notif.show(Page.getCurrent());
                    } else {
                        ProgramaTrabajoEmpleadoForm planTrabajoEmpleadoForm
                                = new ProgramaTrabajoEmpleadoForm(
                                        rhContainer,
                                String.valueOf(idexContainer.getContainerProperty(idexGrid.getSelectedRow(), ID_PROPERTY).getValue()),
                                String.valueOf(idexContainer.getContainerProperty(idexGrid.getSelectedRow(), DESCRIPCION_PROPERTY).getValue()),
                                String.valueOf(idexContainer.getContainerProperty(idexGrid.getSelectedRow(), FECHAINICIO_PROPERTY).getValue())
                        );
                        mainUI.addWindow(planTrabajoEmpleadoForm);
                        planTrabajoEmpleadoForm.center();
                    }
                }
        );

        Button delEmployeeBtn = new Button("DES-ASIGNAR RH");
        delEmployeeBtn.setIcon(FontAwesome.TRASH);
        delEmployeeBtn.addStyleName(ValoTheme.BUTTON_LINK);
        delEmployeeBtn.setWidth(190, Sizeable.UNITS_PIXELS);
        delEmployeeBtn.setDescription("Des-Asignar un RH1 o RH2 a este prograam");
        delEmployeeBtn.addListener((Button.ClickListener) (Button.ClickEvent event) -> {
            if (rhContainer.size() == 0) {
                Notification notif = new Notification("No hay RH.", Notification.Type.WARNING_MESSAGE);
                notif.setDelayMsec(1500);
                notif.setPosition(Position.MIDDLE_CENTER);
                notif.setIcon(FontAwesome.WARNING);
                notif.show(Page.getCurrent());
            } else if (rhGrid.getSelectedRow() == null) {
                Notification notif = new Notification("Por favor elija un RH1 o RH2 para quitar.", Notification.Type.WARNING_MESSAGE);
                notif.setDelayMsec(1500);
                notif.setPosition(Position.MIDDLE_CENTER);
                notif.setIcon(FontAwesome.WARNING);
                notif.show(Page.getCurrent());
            } else {
                try {
                    ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de DES-ASIGNAR el RH en este programa?",
                            "SI", "NO", new ConfirmDialog.Listener() {

                                public void onClose(ConfirmDialog dialog) {
                                    if (dialog.isConfirmed()) {
                                        try {

                                            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

                                            queryString = "DELETE FROM plan_trabajo_idex_rh  ";
                                            queryString += " WHERE Id = " + rhContainer.getContainerProperty(rhGrid.getSelectedRow(), ID_PROPERTY).getValue();

                                            stQuery.executeUpdate(queryString);

                                            rhContainer.getContainerProperty(rhGrid.getSelectedRow(), ESTATUS_PROPERTY).setValue("INACTIVO");

                                            Notification.show("REGISTRO ACTUALIZADO CON EXITO!", Notification.Type.HUMANIZED_MESSAGE);

                                        } catch (SQLException ex) {
                                            ex.printStackTrace();
                                            Notification.show("ERROR EN BASE DE DATOS...", Notification.Type.ERROR_MESSAGE);
                                        }
                                    } else {
                                        Notification.show("Operación cancelada!", Notification.Type.WARNING_MESSAGE);
                                    }
                                }
                            });
                } catch (Exception ex) {
                    System.out.println("Error al ACTUALIZAR REGISTRO DEL listado de RH del IDEX : " + ex);
                    ex.printStackTrace();
                    Notification.show("ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                }

            }
        });

        Button jefeBtn = new Button("Asignar como JEFE ");
        jefeBtn.setIcon(FontAwesome.CHECK);
        jefeBtn.addStyleName(ValoTheme.BUTTON_LINK);
        jefeBtn.setWidth(190, Sizeable.UNITS_PIXELS);
        jefeBtn.setDescription("Des-Asignar un RH1 o RH2 a esta tarea");
        jefeBtn.addListener((Button.ClickListener) (Button.ClickEvent event) -> {
            if (rhContainer.size() == 0) {
                Notification notif = new Notification("No hay RH.", Notification.Type.WARNING_MESSAGE);
                notif.setDelayMsec(1500);
                notif.setPosition(Position.MIDDLE_CENTER);
                notif.setIcon(FontAwesome.WARNING);
                notif.show(Page.getCurrent());
            } else if (rhGrid.getSelectedRow() == null) {
                Notification notif = new Notification("Por favor elija un RH1 o RH2 para nombrarlo como JEFE.", Notification.Type.WARNING_MESSAGE);
                notif.setDelayMsec(1500);
                notif.setPosition(Position.MIDDLE_CENTER);
                notif.setIcon(FontAwesome.WARNING);
                notif.show(Page.getCurrent());
            } else {
                try {
                    if(hayJefe()) {
                        Notification.show("Ya existe un JEFE para esta tarea.", Notification.Type.WARNING_MESSAGE);
                        return;
                    }
                    ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de ASIGNAR como JEFE el RH en esta tarea?",
                            "SI", "NO", new ConfirmDialog.Listener() {

                                public void onClose(ConfirmDialog dialog) {
                                    if (dialog.isConfirmed()) {
                                        try {

                                            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

                                            queryString = " UPDATE plan_trabajo_idex_rh SET";
                                            queryString += " EsJefe = 'SI'";
                                            queryString += " WHERE Id = " + String.valueOf(rhContainer.getContainerProperty(rhGrid.getSelectedRow(), ID_PROPERTY).getValue());

                                            stQuery.executeUpdate(queryString);
System.out.println("query update esjefe=" + queryString);

                                            queryString = " UPDATE proveedor SET";
                                            queryString += " EsJefe = 1";
                                            queryString += " WHERE IdProveedor = " + String.valueOf(rhContainer.getContainerProperty(rhGrid.getSelectedRow(), IDEMPLEADO_PROPERTY).getValue());

                                            stQuery.executeUpdate(queryString);

                                            rhContainer.getContainerProperty(rhGrid.getSelectedRow(), ESJEFE_PROPERTY).setValue("SI");

                                            Notification.show("REGISTRO ACTUALIZADO CON EXITO!", Notification.Type.HUMANIZED_MESSAGE);

                                        } catch (SQLException ex) {
                                            ex.printStackTrace();
                                            Notification.show("ERROR EN BASE DE DATOS...", Notification.Type.ERROR_MESSAGE);
                                        }
                                    } else {
                                        Notification.show("Operación cancelada!", Notification.Type.WARNING_MESSAGE);
                                    }
                                }
                            });
                } catch (Exception ex) {
                    System.out.println("Error al ACTUALIZAR REGISTRO DEL listado de RH del IDEX : " + ex);
                    ex.printStackTrace();
                    Notification.show("ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                }

            }
        });

        Button noJefeBtn = new Button("Des Asignar como JEFE ");
        noJefeBtn.setIcon(FontAwesome.UNDO);
        noJefeBtn.addStyleName(ValoTheme.BUTTON_LINK);
        noJefeBtn.setWidth(190, Sizeable.UNITS_PIXELS);
        noJefeBtn.setDescription("Des-Asignar como JEFE un RH1 o RH2 a esta tarea");
        noJefeBtn.addListener((Button.ClickListener) (Button.ClickEvent event) -> {
            if (rhContainer.size() == 0) {
                Notification notif = new Notification("No hay RH.", Notification.Type.WARNING_MESSAGE);
                notif.setDelayMsec(1500);
                notif.setPosition(Position.MIDDLE_CENTER);
                notif.setIcon(FontAwesome.WARNING);
                notif.show(Page.getCurrent());
            } else if (rhGrid.getSelectedRow() == null) {
                Notification notif = new Notification("Por favor elija un RH1 o RH2 para des asignarlo como JEFE.", Notification.Type.WARNING_MESSAGE);
                notif.setDelayMsec(1500);
                notif.setPosition(Position.MIDDLE_CENTER);
                notif.setIcon(FontAwesome.WARNING);
                notif.show(Page.getCurrent());
            } else {
                try {
                    ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de DES ASIGNAR como JEFE el RH en esta tarea?",
                            "SI", "NO", new ConfirmDialog.Listener() {

                                public void onClose(ConfirmDialog dialog) {
                                    if (dialog.isConfirmed()) {
                                        try {

                                            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

                                            queryString = " UPDATE plan_trabajo_idex_rh SET";
                                            queryString += " EsJefe = 'NO'";
                                            queryString += " WHERE Id = " + String.valueOf(rhContainer.getContainerProperty(rhGrid.getSelectedRow(), ID_PROPERTY).getValue());

                                            stQuery.executeUpdate(queryString);

                                            queryString = " UPDATE proveedor SET";
                                            queryString += " EsJefe = 0";
                                            queryString += " WHERE IdProveedor = " + String.valueOf(rhContainer.getContainerProperty(rhGrid.getSelectedRow(), IDEMPLEADO_PROPERTY).getValue());

                                            stQuery.executeUpdate(queryString);

                                            rhContainer.getContainerProperty(rhGrid.getSelectedRow(), ESJEFE_PROPERTY).setValue("NO");

                                            Notification.show("REGISTRO ACTUALIZADO CON EXITO!", Notification.Type.HUMANIZED_MESSAGE);

                                        } catch (SQLException ex) {
                                            ex.printStackTrace();
                                            Notification.show("ERROR EN BASE DE DATOS...", Notification.Type.ERROR_MESSAGE);
                                        }
                                    } else {
                                        Notification.show("Operación cancelada!", Notification.Type.WARNING_MESSAGE);
                                    }
                                }
                            });
                } catch (Exception ex) {
                    System.out.println("Error al ACTUALIZAR REGISTRO DEL listado de RH del IDEX : " + ex);
                    ex.printStackTrace();
                    Notification.show("ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                }

            }
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setMargin(new MarginInfo(false,true, true, true));
        buttonsLayout.setWidth("100%");
        buttonsLayout.addComponent(addEmployeeBtn);
        buttonsLayout.addComponent(delEmployeeBtn);
        buttonsLayout.addComponent(jefeBtn);
        buttonsLayout.addComponent(noJefeBtn);
        buttonsLayout.setComponentAlignment(addEmployeeBtn, Alignment.BOTTOM_LEFT);
        buttonsLayout.setComponentAlignment(delEmployeeBtn, Alignment.BOTTOM_RIGHT);
        buttonsLayout.setComponentAlignment(jefeBtn, Alignment.BOTTOM_RIGHT);
        buttonsLayout.setComponentAlignment(noJefeBtn, Alignment.BOTTOM_RIGHT);

        reportLayout.addComponent(buttonsLayout);
        reportLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);

        mainLayout.addComponent(reportLayout);
    }

    public void fillIdexGrid(boolean eliminar) {

//        if(projectCbx.getValue() == null) {
//            Notification.show("POR FAVOR ELIJA UN PROJECT.", Notification.Type.WARNING_MESSAGE);
//            return;
//        }

        if (delDt.getValue().after(alDt.getValue())) {
            Notification.show("Fecha inicio es menor que fecha final, revise!!", Notification.Type.WARNING_MESSAGE);
            return;
        }
        if (alDt.getValue().before(delDt.getValue())) {
            Notification.show("Fecha final es menor que fecha inicial, revise!!", Notification.Type.WARNING_MESSAGE);
            return;
        }

        idexContainer.removeAllItems();

        idexGrid.setCaption("IDEX (TAREAS) DEL PROGRAMA DE TRABAJO");

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery1 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            if(eliminar) {
                ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está acción eliminará la asignación de RH a tareas NO INICIADAS. Está seguro de continuar?",
                        "SI", "NO", new ConfirmDialog.Listener() {

                    public void onClose(ConfirmDialog dialog) {
                        if (dialog.isConfirmed()) {
                            try {
                                //POR  UN TRIGGER EN LA TABLA, DEBI HACER LO SIGUIENTE
                                queryString = "SELECT Id FROM plan_trabajo_idex";
                                queryString += " WHERE ISNULL(FechaInicioSegunJefe) = 1";

                                rsRecords1 = stQuery1.executeQuery(queryString);
                                String ids = "0";

                                while (rsRecords1.next()) {
                                    ids += "," + rsRecords1.getString("Id");
                                }
                                //            ids = (!ids.trim().isEmpty() ? (ids.substring(0, ids.length()-1)) : ids);

                                //ELIMINAR LOS REGISTROS DE plan_trabajo_idex de tareas o idex no iniciados...
                                queryString = "DELETE FROM plan_trabajo_idex_rh";
                                queryString += " WHERE IdPlanTrabajoIdex IN (" + ids + ")";
                                //System.out.println("--->" + queryString);
                                stQuery1.executeUpdate(queryString);

                                queryString = "DELETE FROM plan_trabajo_idex";
                                queryString += " WHERE ISNULL(FechaInicioSegunJefe) = 1";

                                stQuery1.executeUpdate(queryString);
                            } catch (Exception ex1) {
                                System.out.println("Error al eliminar tabla DE IDEX (TAREAS) DEL PROGRAMA : " + ex1);
                                ex1.printStackTrace();
                                Notification.show("ERROR DE BASE DE DATOS : " + ex1.getMessage(), Notification.Type.ERROR_MESSAGE);
                            }

                        } else {
                            Notification.show("Operación cancelada por usuario!", Notification.Type.WARNING_MESSAGE);
                        }
                    }
                });
            }

            queryString = "SELECT * ";
            queryString += " FROM project_tarea ";
            queryString += " INNER JOIN project ON project.Id = project_tarea.IdProject";
            queryString += " WHERE project_tarea.FechaInicio BETWEEN '" + Utileria.getFechaYYYYMMDD_1(delDt.getValue()) + "'";
            queryString += " AND '" + Utileria.getFechaYYYYMMDD_1(alDt.getValue()) + "'";
            queryString += " AND (project_tarea.RH1 + project_tarea.RH1) > 0 ";
            queryString += " AND project.Estatus = 'ACTIVO'";
            queryString += " AND project_tarea.FechaRealFin = '0000-00-00'";
            queryString += " ORDER BY project_tarea.FechaInicio";

            Logger.getLogger(this.getClass().getName()).log(Level.INFO, queryString);

            rsRecords = stQuery.executeQuery(queryString);

            if(rsRecords.next() == false) {
                Notification.show("NO HAY IDEX EN EL RANGO DE FECHAS.", Notification.Type.WARNING_MESSAGE);
                return;
            }

            int milisecondsByDay = 86400000;
            int diasPrg = 0, diasReal = 0;
            int rh1 = 0, rh2 = 0;
            Object itemId;
            String idProject;

            do {
                idProject = rsRecords.getString("IdProject");

                diasPrg = 0; diasReal = 0;
                rh1 = 0; rh2 = 0;

                itemId = idexContainer.addItem();

                idexContainer.getContainerProperty(itemId, ID_PROPERTY).setValue("");
                idexContainer.getContainerProperty(itemId, PROJECT_PROPERTY).setValue(rsRecords.getString("Numero"));
                idexContainer.getContainerProperty(itemId, IDEX_PROPERTY).setValue(rsRecords.getString("Idex"));
                if (rsRecords.getObject("IdCentroCosto") != null) {
                    idexContainer.getContainerProperty(itemId, IDCC_PROPERTY).setValue(rsRecords.getString("IdCentroCosto"));
                }
                idexContainer.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(rsRecords.getString("Descripcion"));
                idexContainer.getContainerProperty(itemId, FECHAINICIO_PROPERTY).setValue(rsRecords.getString("FechaInicio"));
                idexContainer.getContainerProperty(itemId, FECHAFINAL_PROPERTY).setValue(rsRecords.getString("FechaFin"));
                idexContainer.getContainerProperty(itemId, DURACION_PROPERTY).setValue(rsRecords.getInt("DiasDuracion"));
                idexContainer.getContainerProperty(itemId, ESTATUS_PROPERTY).setValue(rsRecords.getString("Estatus"));

                queryString = "SELECT Id, PTI.RH1, PTI.RH2, Instrucciones, ";
                queryString += "(CASE";
                queryString += " WHEN ISNULL(PTI.FechaInicioSegunSupervisor) = 0 THEN PTI.FechaInicioSegunSupervisor";
                queryString += " WHEN ISNULL(PTI.FechaInicioSegunMaestro) = 0 THEN PTI.FechaInicioSegunMaestro";
                queryString += " WHEN ISNULL(PTI.FechaInicioSegunJefe) = 0 THEN PTI.FechaInicioSegunJefe";
                queryString += " WHEN ISNULL(PTI.FechaInicioSegunJefe) = 1 THEN 'NO INICIADA'";
                queryString += " END ) As FechaInicioReal, ";
                queryString += " (CASE ";
                queryString += " WHEN ISNULL(PTI.FechaFinSegunSupervisor) = 0 THEN PTI.FechaFinSegunSupervisor ";
                queryString += " WHEN ISNULL(PTI.FechaFinSegunMaestro) = 0 THEN PTI.FechaFinSegunMaestro ";
                queryString += " WHEN ISNULL(PTI.FechaFinSegunJefe) = 0 THEN PTI.FechaFinSegunJefe";
                queryString += " WHEN ISNULL(PTI.FechaFinSegunJefe) = 1 THEN 'NO FINALIZADA'";
                queryString += " END ) As FechaFinReal";
                queryString += " FROM plan_trabajo_idex PTI ";
                queryString += " WHERE PTI.Idex = '" + rsRecords.getString("Idex") + "'";

//System.out.println("queryFECHASREALES=" + queryString);

                rsRecords1 = stQuery1.executeQuery(queryString);

                if (rsRecords1.next()) { //la tarea le fue asignado RH
                    idexContainer.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords1.getString("Id"));
                    idexContainer.getContainerProperty(itemId, FECHAINICIOREAL_PROPERTY).setValue(rsRecords1.getString("FechaInicioReal"));
                    idexContainer.getContainerProperty(itemId, FECHAFINAREAL_PROPERTY).setValue(rsRecords1.getString("FechaFinReal"));
                    if (rsRecords1.getString("FechaInicioReal").equals("NO INICIADA") == false) {
                        if (rsRecords1.getString("FechaFinReal").equals("NO FINALIZADA") == false) {
                            diasReal = Integer.valueOf(String.valueOf((rsRecords1.getDate("FechaFinReal").getTime() - rsRecords1.getDate("FechaInicioReal").getTime()) / milisecondsByDay) + 1);
                        }
                    }
                    idexContainer.getContainerProperty(itemId, DURACIONREAL_PROPERTY).setValue(diasReal);
                    if (diasReal > 0) {
                        idexContainer.getContainerProperty(itemId, DIFERENCIA_PROPERTY).setValue(diasReal - diasPrg);
                    }
                    idexContainer.getContainerProperty(itemId, INSTRUCCIONES_PROPERTY).setValue(rsRecords1.getString("Instrucciones"));
                    rh1 = getCountRH1RH2("RH1",rsRecords1.getString("Id"));
                    rh2 = getCountRH1RH2("RH2",rsRecords1.getString("Id"));
                }

                idexContainer.getContainerProperty(itemId, RH1_PROPERTY).setValue(rsRecords.getInt("RH1"));
                idexContainer.getContainerProperty(itemId, RH2_PROPERTY).setValue(rsRecords.getInt("RH2"));
                idexContainer.getContainerProperty(itemId, RH1_REAL_PROPERTY).setValue(rh1);
                idexContainer.getContainerProperty(itemId, RH2_REAL_PROPERTY).setValue(rh2);
                idexContainer.getContainerProperty(itemId, RH1_DIFERENCIA_PROPERTY).setValue((rh1 - rsRecords.getInt("RH1")));
                idexContainer.getContainerProperty(itemId, RH2_DIFERENCIA_PROPERTY).setValue((rh2 - rsRecords.getInt("RH2")));
                idexContainer.getContainerProperty(itemId, IDPROJECT_PROPERTY).setValue(idProject);

            } while (rsRecords.next());

//            idexGrid.select(idexContainer.firstItemId());
//            idexGrid.select(null);
        } catch (Exception ex) {
            System.out.println("Error al listar tabla DE IDEX (TAREAS) DEL PROGRAMA : " + ex);
            ex.printStackTrace();
            Notification.show("ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }

    private void asignarRH() {

        if(idexContainer.size() == 0) {
            Notification.show("NO HAY IDEX POR EJECUTAR O EN EJECUCION EN EL RANGO DE FECHAS.", Notification.Type.WARNING_MESSAGE);
            return;
        }

        ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de ASIGNAR recurso RH1 y RH2 a las tareas no iniciadas ?  \n\n\n Esta ventana se cerrará cuando haya terminado el proceso...",
                "SI", "NO", new ConfirmDialog.Listener() {

                    public void onClose(ConfirmDialog dialog) {
                        if (dialog.isConfirmed()) {
                            String queryString;

                            try {
                                //POR  UN TRIGGER EN LA TABLA, DEBI HACER LO SIGUIENTE
                                queryString = "SELECT Id FROM plan_trabajo_idex";
                                queryString += " WHERE ISNULL(FechaInicioSegunJefe) = 1";

                                rsRecords1 = stQuery1.executeQuery(queryString);
                                String ids = "0";

                                while(rsRecords1.next()) {
                                    ids += "," + rsRecords1.getString("Id");
                                }
                                ids = (!ids.trim().equals("0") ? (ids.substring(0, ids.length()-1)) : ids);

                                //ELIMINAR LOS REGISTROS DE plan_trabajo_idex de tareas o idex no iniciados...
                                queryString = "DELETE FROM plan_trabajo_idex_rh";
                                queryString += " WHERE IdPlanTrabajoIdex IN ("+ ids + ")";
//System.out.println("--->" + queryString);
                                stQuery1.executeUpdate(queryString);

                                queryString = "DELETE FROM plan_trabajo_idex";
                                queryString += " WHERE ISNULL(FechaInicioSegunJefe) = 1";

                                stQuery1.executeUpdate(queryString);

                                for(Object idexObject : idexContainer.getItemIds()) {
                                    //VALIDAR LA FECHA DE INICIO DE EJECUCION, SI TIENE FECHA ENTONCES OMITIR
                                    if(String.valueOf(idexContainer.getContainerProperty(idexObject, FECHAINICIOREAL_PROPERTY).getValue()).trim().isEmpty() == false) {
                                        continue;
                                    }
                                    queryString =  "Insert Into plan_trabajo_idex (Idex, IdProject, FechaInicioPlaneada, FechaFinPlaneada)";
                                    queryString += " Values (";
                                    queryString += "'"  + idexContainer.getContainerProperty(idexObject, IDEX_PROPERTY).getValue() + "'";
                                    queryString += ","  + idexContainer.getContainerProperty(idexObject, IDPROJECT_PROPERTY).getValue();
                                    queryString += ",'" + idexContainer.getContainerProperty(idexObject, FECHAINICIO_PROPERTY).getValue() + "'";
                                    queryString += ",'" + idexContainer.getContainerProperty(idexObject, FECHAFINAL_PROPERTY).getValue() + "'";
//                                    queryString += ", " + idexContainer.getContainerProperty(idexObject, RH1_PROPERTY).getValue();
//                                    queryString += ", " + idexContainer.getContainerProperty(idexObject, RH2_PROPERTY).getValue();
                                    queryString += ")";

//System.out.println("INSERT PLANTRABAJOIDEX ="+queryString);

                                    stPreparedQuery  = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().prepareStatement(queryString, Statement.RETURN_GENERATED_KEYS);
                                    stPreparedQuery.executeUpdate();
                                    rsRecords = stPreparedQuery.getGeneratedKeys();

                                    rsRecords.next();

                                    String idPlanTrabajoIdex = rsRecords.getString(1);

                                    idexContainer.getContainerProperty(idexObject, ID_PROPERTY).setValue(idPlanTrabajoIdex);

                                    fillRhGrid(idexObject);

                                    for(Object rhObject : rhContainer.getItemIds()) {
                                        queryString = "Insert Into plan_trabajo_idex_rh (IdPlanTrabajoIdex, IdEmpleado, EsJefe, Cargo)";
                                        queryString += " Values (";
                                        queryString += " " + idPlanTrabajoIdex;
                                        queryString += ",'" + rhContainer.getContainerProperty(rhObject, IDEMPLEADO_PROPERTY).getValue() + "'";
                                        queryString += ",'" + rhContainer.getContainerProperty(rhObject, ESJEFE_PROPERTY).getValue() + "'";
                                        queryString += ",'" + rhContainer.getContainerProperty(rhObject, CARGO_PROPERTY).getValue() + "'";
                                        queryString += ")";

//System.out.println("queryString="+queryString);

                                        stQuery1.executeUpdate(queryString);

                                    } //endfor empleados

                                    //MAESTRO Y SUPERVISOR
                                    for(Object rhObject : rhContainerMaestroSupervidor.getItemIds()) {
                                        queryString = "Insert Into plan_trabajo_idex_rh (IdPlanTrabajoIdex, IdEmpleado, EsJefe, Cargo)";
                                        queryString += " Values (";
                                        queryString += " " + idPlanTrabajoIdex;
                                        queryString += ",'" + rhContainerMaestroSupervidor.getContainerProperty(rhObject, IDEMPLEADO_PROPERTY).getValue() + "'";
                                        queryString += ",'NO'";
                                        queryString += ",'" + rhContainerMaestroSupervidor.getContainerProperty(rhObject, CARGO_PROPERTY).getValue() + "'";
                                        queryString += ")";

//System.out.println("queryString="+queryString);

                                        stQuery1.executeUpdate(queryString);

                                    } //endfor MAESTRO O SUPERVISOR
                                }//endfor idex

                            } catch (Exception ex) {
                                ex.printStackTrace();
                                Notification.show("ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                            }
                        }
                        else {
                            Notification.show("PROCESO CANCELADO POR USUARIO", Notification.Type.HUMANIZED_MESSAGE);
                        }
                    }
                });
    }

    private int getCountRH1RH2(String rh, String id) {
        int cantidad = 0;

        try {
            stQuery2 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            queryString = "SELECT count(*) Cantidad";
            queryString += " FROM plan_trabajo_idex_rh ";
            queryString += " WHERE IdPlanTrabajoIdex = " + id;
            queryString += " AND Cargo = '" + rh + "'";

            rsRecords2 = stQuery2.executeQuery(queryString);

            if(rsRecords2.next()) {
                cantidad = rsRecords2.getInt("Cantidad");
            }
        } catch (Exception ex) {
            System.out.println("Error al obtener RH1 o RH2 DE tarea de programa de trabajo: " + ex);
            ex.printStackTrace();
            Notification.show("ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }

        return cantidad;
    }

    public void fillRhGrid(Object selectedItem) {

        rhContainer.removeAllItems();
        rhContainerMaestroSupervidor.removeAllItems();

        if (selectedItem == null) {
            return;
        }

        if(String.valueOf(idexContainer.getContainerProperty(selectedItem, ID_PROPERTY).getValue()).trim().isEmpty()) {
            return;
        }

        rhGrid.setCaption("RECURSO HUMANO  DEL PROGRAMA PARA IDEX : " + idexContainer.getContainerProperty(selectedItem, IDCC_PROPERTY).getValue() + " " + idexContainer.getContainerProperty(selectedItem, IDEX_PROPERTY).getValue() );

        String queryString;

        //2. Leer los registros de tabla proveedor donde cargo = RH1 o RH2 (done)
        queryString = "SELECT *";
        queryString += " FROM proveedor";
        queryString += " WHERE EsPlanilla = 1";
        queryString += " AND Cargo IN ('RH1', 'RH2')";
        queryString += " AND EstatusTrabajo NOT IN ('DE BAJA', 'AUSENTE')";
        queryString += " AND Inhabilitado  = 0";
        queryString += " AND IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
        queryString += " ORDER BY Nombre";

        int rh1 = 0, rh2 = 0;
        boolean hayJefe = false;

        try {

            stQuery  = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery1 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { // POR CADA EMPLEADO RH1 Y RH2

                Object itemId;

                do {
                    queryString = "SELECT *";
                    queryString += " FROM plan_trabajo_idex_rh";
                    queryString += " INNER JOIN plan_trabajo_idex ON plan_trabajo_idex.Id = plan_trabajo_idex_rh.IdPlanTrabajoIdex";
                    queryString += " WHERE plan_trabajo_idex_rh.IdEmpleado = " + rsRecords.getString("IdProveedor");
                    queryString += " AND '" + String.valueOf(idexContainer.getContainerProperty(selectedItem, FECHAINICIO_PROPERTY).getValue()) +  "'";
                    queryString += "     BETWEEN plan_trabajo_idex.FechaInicioPlaneada AND plan_trabajo_idex.FechaFinPlaneada";

                    rsRecords1 = stQuery1.executeQuery(queryString);

                    if (!rsRecords1.next()) { // NO TIENE TAREAS ASIGNADAS EN LAS FECHAS
                        if (rsRecords.getString("Cargo").equals("RH1")) {
                            if (rh1 >= Integer.valueOf(String.valueOf(idexContainer.getContainerProperty(selectedItem, RH1_PROPERTY).getValue()))) {
                                continue;
                            }
                            rh1++;
                        }
                        if (rsRecords.getString("Cargo").equals("RH2")) {
                            if (rh2 >= Integer.valueOf(String.valueOf(idexContainer.getContainerProperty(selectedItem, RH2_PROPERTY).getValue()))) {
                                continue;
                            }
                            rh2++;
                        }

                        itemId = rhContainer.addItem();

                        rhContainer.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords.getString("IdProveedor"));
                        rhContainer.getContainerProperty(itemId, IDEMPLEADO_PROPERTY).setValue(rsRecords.getString("IdProveedor"));
                        rhContainer.getContainerProperty(itemId, NOMBRE_PROPERTY).setValue(rsRecords.getString("Nombre"));
                        rhContainer.getContainerProperty(itemId, CARGO_PROPERTY).setValue(rsRecords.getString("Cargo"));
                        if(!hayJefe && rsRecords.getString("Cargo").equals("RH1")) { //no hay jefe y es rh
//                            rhContainer.getContainerProperty(itemId, ESJEFE_PROPERTY).setValue(rsRecords.getString("EsJefe").equals("1") ? "SI" : "");
                             rhContainer.getContainerProperty(itemId, ESJEFE_PROPERTY).setValue("SI");
                            hayJefe = true;
                        }
                        else {
                            rhContainer.getContainerProperty(itemId, ESJEFE_PROPERTY).setValue("NO");
//                        rhContainer.getContainerProperty(itemId, OPTIONS_PROPERTY).setValue(null)
                        };
//System.out.println("rhContainer.size()="+rhContainer.size());
                    }

                } while (rsRecords.next());

                //UPDATE DEL GRID IDEX
                idexContainer.getContainerProperty(selectedItem, RH1_REAL_PROPERTY).setValue(rh1);
                idexContainer.getContainerProperty(selectedItem, RH2_REAL_PROPERTY).setValue(rh2);
                rh1 = rh1 - Integer.valueOf(String.valueOf(idexContainer.getContainerProperty(selectedItem, RH1_PROPERTY).getValue()));
                rh2 = rh2 - Integer.valueOf(String.valueOf(idexContainer.getContainerProperty(selectedItem, RH2_PROPERTY).getValue()));
                idexContainer.getContainerProperty(selectedItem, RH1_DIFERENCIA_PROPERTY).setValue(rh1);
                idexContainer.getContainerProperty(selectedItem, RH2_DIFERENCIA_PROPERTY).setValue(rh2);

            }

            //insertar los MAESTROs DE OBRAS Y SUPEREVISORES

            queryString = "SELECT cce.*";
            queryString += " FROM centro_costo_encargado cce";
            queryString += " INNER JOIN centro_costo cc ON cce.CodigoCentroCosto = cc.CodigoCentroCosto";
            queryString += " WHERE cc.CodigoCentroCosto = " + String.valueOf(idexContainer.getContainerProperty(selectedItem, IDCC_PROPERTY).getValue());
            queryString += " AND cce.Eliminado = 0";

           rsRecords = stQuery.executeQuery(queryString);

            while(rsRecords.next()) {
                Object itemId = rhContainerMaestroSupervidor.addItem();

                rhContainerMaestroSupervidor.getContainerProperty(itemId, IDEMPLEADO_PROPERTY).setValue(rsRecords.getString("IdProveedor"));
                rhContainerMaestroSupervidor.getContainerProperty(itemId, NOMBRE_PROPERTY).setValue(rsRecords.getString("Nombre"));
                rhContainerMaestroSupervidor.getContainerProperty(itemId, CARGO_PROPERTY).setValue(rsRecords.getString("Tipo"));
            }

        } catch (Exception ex) {
            Logger.getLogger(ProgramaTrabajoView.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de detalle de empleados : " + ex.getMessage());
            Notification.show("Error al intentar leer detalle de RH..!", Notification.Type.ERROR_MESSAGE);
        }

    }

    public void fillRhGridPlan(Object selectedItem) {

        rhContainer.removeAllItems();
        rhContainerMaestroSupervidor.removeAllItems();

        if (selectedItem == null) {
            return;
        }

        if(String.valueOf(idexContainer.getContainerProperty(selectedItem, ID_PROPERTY).getValue()).trim().isEmpty()) {
            return;
        }

        rhGrid.setCaption("RECURSO HUMANO  DEL PROGRAMA PARA IDEX : " + idexContainer.getContainerProperty(selectedItem, IDCC_PROPERTY).getValue() + " " + idexContainer.getContainerProperty(selectedItem, IDEX_PROPERTY).getValue() );

        String queryString;

        queryString = "SELECT *";
        queryString += " FROM proveedor";
        queryString += " INNER JOIN plan_trabajo_idex_rh ON plan_trabajo_idex_rh.IdEmpleado = proveedor.IdProveedor";
        queryString += " WHERE plan_trabajo_idex_rh.IdPlanTrabajoIdex = " + idexContainer.getContainerProperty(selectedItem, ID_PROPERTY).getValue();
        queryString += " ORDER BY proveedor.Cargo";
//System.out.println("queryRH=" + queryString);
        int rh1 = 0, rh2 = 0;

        try {

            stQuery  = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { // POR CADA EMPLEADO RH1 Y RH2

                boolean hayJefe = false;
                Object itemId;

                do {
                    if (rsRecords.getString("Cargo").equals("RH1")) {
                        rh1++;
                    }
                    if (rsRecords.getString("Cargo").equals("RH2")) {
                        rh2++;
                    }

                    if(   rsRecords.getString("Cargo").toUpperCase().contains("MAESTRO")
                       || rsRecords.getString("Cargo").toUpperCase().contains("SUPERVISOR")) {

                        itemId = rhContainerMaestroSupervidor.addItem();

                        rhContainerMaestroSupervidor.getContainerProperty(itemId, IDEMPLEADO_PROPERTY).setValue(rsRecords.getString("IdEmpleado"));
                        rhContainerMaestroSupervidor.getContainerProperty(itemId, NOMBRE_PROPERTY).setValue(rsRecords.getString("Nombre"));
                        rhContainerMaestroSupervidor.getContainerProperty(itemId, CARGO_PROPERTY).setValue(rsRecords.getString("Cargo"));
                    }
                    else {
                        itemId = rhContainer.addItem();

                        rhContainer.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords.getString("plan_trabajo_idex_rh.Id"));
                        rhContainer.getContainerProperty(itemId, IDEMPLEADO_PROPERTY).setValue(rsRecords.getString("IdEmpleado"));
                        rhContainer.getContainerProperty(itemId, NOMBRE_PROPERTY).setValue(rsRecords.getString("Nombre"));
                        rhContainer.getContainerProperty(itemId, CARGO_PROPERTY).setValue(rsRecords.getString("Cargo"));

                        if(!hayJefe) { //no hay jefe
                            rhContainer.getContainerProperty(itemId, ESJEFE_PROPERTY).setValue(rsRecords.getString("plan_trabajo_idex_rh.EsJefe"));
                            hayJefe = rsRecords.getString("EsJefe").equals("SI");
                        }
                        else {
                            rhContainer.getContainerProperty(itemId, ESJEFE_PROPERTY).setValue("NO");
                        }

//                        rhContainer.getContainerProperty(itemId, OPTIONS_PROPERTY).setValue("");
                    }

                } while (rsRecords.next());

                idexContainer.getContainerProperty(selectedItem, RH1_REAL_PROPERTY).setValue(rh1);
                idexContainer.getContainerProperty(selectedItem, RH2_REAL_PROPERTY).setValue(rh2);
//                if(Integer.valueOf(String.valueOf(idexContainer.getContainerProperty(selectedItem, RH1_REAL_PROPERTY).getValue())) > 0) {
                    int rhD1 = Integer.valueOf(String.valueOf(idexContainer.getContainerProperty(selectedItem, RH1_REAL_PROPERTY).getValue())) - rh1;
//                    idexContainer.getContainerProperty(selectedItem, RH1_DIFERENCIA_PROPERTY).setValue(rhD1);
//                }
//                if(Integer.valueOf(String.valueOf(idexContainer.getContainerProperty(selectedItem, RH2_REAL_PROPERTY).getValue())) > 0) {
                    int rhD2 = Integer.valueOf(String.valueOf(idexContainer.getContainerProperty(selectedItem, RH2_REAL_PROPERTY).getValue())) - rh2;
//                    idexContainer.getContainerProperty(selectedItem, RH2_DIFERENCIA_PROPERTY).setValue(rhD2);
//                }
            }

        } catch (Exception ex) {
            Logger.getLogger(ProgramaTrabajoView.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de detalle de empleados : " + ex.getMessage());
            Notification.show("Error al intentar leer detalle de RH..!", Notification.Type.ERROR_MESSAGE);
        }

    }

    private boolean hayJefe() {
        boolean siHayJefe = false;
        for(Object itemId : rhContainer.getItemIds()) {
            if(String.valueOf(rhContainer.getContainerProperty(itemId, ESJEFE_PROPERTY).getValue()).equals("SI")) {
                siHayJefe =true;
                break;
            }
        }
        return siHayJefe;
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - PROGRAMA DE TRABAJO");
    }
}


/**
 *                 // Define a common menu command for all the menu items.
 *                 MenuBar.Command mycommand = new MenuBar.Command() {
 *                     @Override
 *                     public void menuSelected(MenuBar.MenuItem selectedItem) {
 *                         if(rhGrid.getSelectedRow() != null) {
 *                             MenuBar menuBar = (MenuBar)rhContainer.getContainerProperty(rhGrid.getSelectedRow(), OPTIONS_PROPERTY).getValue();
 *                             if(menuBar.getItems().get(0).getChildren().contains(selectedItem)) {
 *
 *                                 if(selectedItem.getId() == 1) { // JEFE
 *                                     ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de nombrar como JEFE?",
 *                                             "SI", "NO", new ConfirmDialog.Listener() {
 *
 *                                                 public void onClose(ConfirmDialog dialog) {
 *                                                     if (dialog.isConfirmed()) {
 *                                                         try {
 *                                                             for (Object objectItem : rhContainer.getItemIds()) {
 *                                                                 if (String.valueOf(rhContainer.getContainerProperty(objectItem, ESJEFE_PROPERTY).getValue()).equals("SI")) {
 *                                                                     Notification.show("YA EXISTE UN EMPLEADO COMO JEFE EN ESTA TAREA, DEBE PRIMERO QUITAR COMO JEFE AL EMPLEADO JEFE ACTUAL.", Notification.Type.WARNING_MESSAGE);
 *                                                                     return;
 *                                                                 }
 *
 *                                                             }// end for;
 *
 *                                                             stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
 *
 *                                                             String queryString = " UPDATE plan_trabajo_idex_rh SET";
 *                                                             queryString += " EsJefe = 'SI''";
 *                                                             queryString += " WHERE Id = " + String.valueOf(rhContainer.getContainerProperty(rhGrid.getSelectedRow(), ID_PROPERTY).getValue());
 *
 *                                                             stQuery.executeUpdate(queryString);
 *
 *                                                             rhContainer.getContainerProperty(rhGrid.getSelectedRow(), ESJEFE_PROPERTY).setValue("SI");
 *
 *                                                             Notification.show("REGISTRO ACTUALIZADO CON EXITO!", Notification.Type.HUMANIZED_MESSAGE);
 *
 *                                                         } catch (SQLException ex) {
 *                                                             ex.printStackTrace();
 *                                                             Notification.show("ERROR EN BASE DE DATOS...", Notification.Type.ERROR_MESSAGE);
 *                                                         }
 *                                                     }
 *                                                 }
 *                                             });
 *                                 }
 *                                 if(selectedItem.getId() == 2) { // quitar como jefe
 *                                     ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de DES-ASIGNAR el RH en este programa?",
 *                                             "SI", "NO", new ConfirmDialog.Listener() {
 *
 *                                                 public void onClose(ConfirmDialog dialog) {
 *                                                     if (dialog.isConfirmed()) {
 *                                                         try {
 *
 *                                                             stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
 *
 *                                                             String queryString = " UPDATE plan_trabajo_idex_rh SET";
 *                                                             queryString += " EsJefe = 'NO'";
 *                                                             queryString += " WHERE Id = " + String.valueOf(rhContainer.getContainerProperty(rhGrid.getSelectedRow(), ID_PROPERTY).getValue());
 *
 *                                                             stQuery.executeUpdate(queryString);
 *
 *                                                             rhContainer.getContainerProperty(rhGrid.getSelectedRow(), ESJEFE_PROPERTY).setValue("NO");
 *
 *                                                             Notification.show("REGISTRO ACTUALIZADO CON EXITO!", Notification.Type.HUMANIZED_MESSAGE);
 *
 *                                                         } catch (SQLException ex) {
 *                                                             ex.printStackTrace();
 *                                                             Notification.show("ERROR EN BASE DE DATOS...", Notification.Type.ERROR_MESSAGE);
 *                                                         }
 *                                                     } else {
 *                                                         Notification.show("Operación cancelada!", Notification.Type.WARNING_MESSAGE);
 *                                                     }
 *                                                 }
 *                                             });
 *                                 }
 *                                 if(selectedItem.getId() == 3) { // DES ASIGNAR RH
 *                                     ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de DES-ASIGNAR el RH en este programa?",
 *                                             "SI", "NO", new ConfirmDialog.Listener() {
 *
 *                                                 public void onClose(ConfirmDialog dialog) {
 *                                                     if (dialog.isConfirmed()) {
 *                                                         try {
 *
 *                                                             stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
 *
 *                                                             String queryString = " DELETE plan_trabajo_idex_rh ";
 *                                                             queryString += " WHERE Id = " + String.valueOf(rhContainer.getContainerProperty(rhGrid.getSelectedRow(), ID_PROPERTY).getValue());
 *
 *                                                             stQuery.executeUpdate(queryString);
 *
 *                                                             rhContainer.getContainerProperty(rhGrid.getSelectedRow(), ESTATUS_PROPERTY).setValue("INACTIVO");
 *
 *                                                             Notification.show("REGISTRO ACTUALIZADO CON EXITO!", Notification.Type.HUMANIZED_MESSAGE);
 *
 *                                                         } catch (SQLException ex) {
 *                                                             ex.printStackTrace();
 *                                                             Notification.show("ERROR EN BASE DE DATOS...", Notification.Type.ERROR_MESSAGE);
 *                                                         }
 *                                                     } else {
 *                                                         Notification.show("Operación cancelada!", Notification.Type.WARNING_MESSAGE);
 *                                                     }
 *                                                 }
 *                                             });
 *                                 }
 *                             }
 *                             else {
 *                                 Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
 *                             }
 *                         }
 *                     }
 *                 };
 *
 *
 *                                     MenuBar contactMenu = new MenuBar();
 *                     contactMenu.setCaption("Menú");
 *                     contactMenu.addStyleName(ValoTheme.MENUBAR_BORDERLESS);
 *                     contactMenu.addStyleName(ValoTheme.MENUBAR_SMALL);
 *                     contactMenu.addStyleName(ValoTheme.MENU_APPEAR_ON_HOVER);
 *                     contactMenu.setSizeUndefined();
 *                     contactMenu.setData(rsRecords.getInt("plan_trabajo_idex_rh.Id"));
 *                     MenuBar.MenuItem menuItem = contactMenu.addItem("", FontAwesome.EDIT, null);
 *                     menuItem.addItem("Nombrar como JEFE", FontAwesome.EYE, mycommand);
 *                     menuItem.addSeparator();
 *                     menuItem.addItem("Dar de baja", FontAwesome.CHECK, mycommand);
 *                     menuItem.addSeparator();
 */