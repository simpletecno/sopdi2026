package com.simpletecno.sopdi.operativo;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.SelectionEvent;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.sql.ResultSet;
import java.sql.Statement;

public class ResumenCalificacionCalidadView extends VerticalLayout implements View {

    public Statement stQuery = null;
    public ResultSet rsRecords = null;
    public ResultSet rsRecords1 = null;
    private String queryString = "";

    UI mainUI;
    ComboBox empresaCbx;
    String empresa;

    // GRID IDEX
    static final String IDEX_PROPERTY = "IDEX";
    static final String PROJECT_PROPERTY = "Project";
    static final String CENTRO_COSTO_PROPERTY = "Centro Costo";
    static final String NOMBRE_TAREA_PRPERTY = "Tarea";

    // GRID CALIFICACION

    static final String NOMBRE_CARACTERISTICAS_PROPERTY = "Caracteristicas";
    static final String NOMBRE_AREA_PROPERTY = "Area";
    static final String CALIFICACION_MAESTRO_PROPERTY = "Maestro";
    static final String CALIFICACION_SUPERVISOR_PROPERTY = "Supervisor";

    static final String PROMEDIO_PROPERTY = "Promedio"; // <-- Se comparte entre tablas

    Grid resumengrid;
    IndexedContainer resumenContainer = new IndexedContainer();

    Grid idexGrid;
    IndexedContainer idexContainer = new IndexedContainer();

    public ResumenCalificacionCalidadView(){

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

        crearIdexGrid();
        crearResumenGrid();
        llenarIndexGrid();
    }

    public void llenarComboEmpresa() {
        queryString = " SELECT * from contabilidad_empresa";
        queryString += " Where IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();

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

    private void crearIdexGrid(){

        idexContainer.addContainerProperty(IDEX_PROPERTY, String.class, "");
        idexContainer.addContainerProperty(PROJECT_PROPERTY, String.class, "");
        idexContainer.addContainerProperty(CENTRO_COSTO_PROPERTY, String.class, "");
        idexContainer.addContainerProperty(NOMBRE_TAREA_PRPERTY, String.class, "");
        idexContainer.addContainerProperty(PROMEDIO_PROPERTY, String.class, "");


        idexGrid = new Grid("Reseumn Calificacion Empleado ", idexContainer);
        idexGrid.setWidth("95%");
        idexGrid.setImmediate(true);
        idexGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        idexGrid.setDescription("Seleccione un registro.");
        idexGrid.setHeightMode(HeightMode.ROW);
        idexGrid.setHeightByRows(8);
        idexGrid.setResponsive(true);
        idexGrid.setResponsive(true);
        idexGrid.setEditorBuffered(false);

        idexGrid.getColumn(IDEX_PROPERTY).setExpandRatio(1);
        idexGrid.getColumn(PROJECT_PROPERTY).setExpandRatio(1);
        idexGrid.getColumn(CENTRO_COSTO_PROPERTY).setExpandRatio(1);
        idexGrid.getColumn(NOMBRE_TAREA_PRPERTY).setExpandRatio(3);
        idexGrid.getColumn(PROMEDIO_PROPERTY).setExpandRatio(1);

        Grid.HeaderRow filterRow = idexGrid.appendHeaderRow();

        Utileria.addTextFilter(filterRow, IDEX_PROPERTY, idexContainer, 7);
        Utileria.addTextFilter(filterRow, PROJECT_PROPERTY, idexContainer, 5);
        Utileria.addTextFilter(filterRow, CENTRO_COSTO_PROPERTY, idexContainer, 5);
        Utileria.addTextFilter(filterRow, NOMBRE_TAREA_PRPERTY, idexContainer, 30);


        idexGrid.addSelectionListener(new SelectionEvent.SelectionListener() {
            @Override
            public void select(SelectionEvent event
            ) {
                if (idexGrid.getSelectedRow() != null) {

                    llenarResumenGrid((String) idexContainer.getContainerProperty(idexGrid.getSelectedRow(), IDEX_PROPERTY).getValue());

                }
            };
        });

        addComponent(idexGrid);
        setComponentAlignment(idexGrid, Alignment.MIDDLE_CENTER);


    }

    private void crearResumenGrid(){
        resumenContainer.addContainerProperty(NOMBRE_AREA_PROPERTY, String.class, "");
        resumenContainer.addContainerProperty(NOMBRE_CARACTERISTICAS_PROPERTY, String.class, "");
        resumenContainer.addContainerProperty(CALIFICACION_SUPERVISOR_PROPERTY, String.class, "");
        resumenContainer.addContainerProperty(CALIFICACION_MAESTRO_PROPERTY, String.class, "");
        resumenContainer.addContainerProperty(PROMEDIO_PROPERTY, String.class, "");

        resumengrid = new Grid("Reseumn Calificacion IDEX: ", resumenContainer);
        resumengrid.setWidth("95%");
        resumengrid.setImmediate(true);
        resumengrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        resumengrid.setDescription("Seleccione un registro.");
        resumengrid.setHeightMode(HeightMode.ROW);
        resumengrid.setHeightByRows(8);
        resumengrid.setResponsive(true);
        resumengrid.setResponsive(true);
        resumengrid.setEditorBuffered(false);

        resumengrid.getColumn(NOMBRE_AREA_PROPERTY).setExpandRatio(2);
        resumengrid.getColumn(NOMBRE_CARACTERISTICAS_PROPERTY).setExpandRatio(3);
        resumengrid.getColumn(CALIFICACION_SUPERVISOR_PROPERTY).setExpandRatio(1);
        resumengrid.getColumn(CALIFICACION_MAESTRO_PROPERTY).setExpandRatio(1);
        resumengrid.getColumn(PROMEDIO_PROPERTY).setExpandRatio(1);

        Grid.HeaderRow filterRow = resumengrid.appendHeaderRow();

        Utileria.addTextFilter(filterRow, NOMBRE_AREA_PROPERTY, resumenContainer, 15);
        Utileria.addTextFilter(filterRow, NOMBRE_CARACTERISTICAS_PROPERTY, resumenContainer, 30);

        addComponent(resumengrid);
        setComponentAlignment(resumengrid, Alignment.MIDDLE_CENTER);
    }

    private void llenarIndexGrid(){
        Object item;
        String promedio;

        queryString = "SELECT pt.IDEX, p.Numero, pt.IdCentroCosto, pt.Descripcion, AVG(IFNULL(ptic.Valor, 0)) AS Promedio ";
        queryString += "FROM project_tarea pt ";
        queryString += "INNER JOIN project p ON pt.IdProject = p.Id ";
        queryString += "INNER JOIN plan_trabajo_idex pti ON pt.IDEX = pti.IDEX ";
        queryString += "LEFT JOIN plan_trabajo_idex_ca ptic on pti.Id = ptic.IdPlanTrabajoIdex ";
        queryString += "WHERE p.Estatus = 'ACTIVO' ";
        queryString += "GROUP BY pti.Id";

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if(rsRecords.next()){
                do{
                     promedio = (rsRecords.getDouble("Promedio") != 0d) ?  Utileria.format(rsRecords.getDouble("Promedio")) : "-.--";

                    item = idexContainer.addItem();
                    idexContainer.getContainerProperty(item, IDEX_PROPERTY).setValue(rsRecords.getString("IDEX"));
                    idexContainer.getContainerProperty(item, PROJECT_PROPERTY).setValue(rsRecords.getString("Numero"));
                    idexContainer.getContainerProperty(item, CENTRO_COSTO_PROPERTY).setValue(rsRecords.getString("IdCentroCosto"));
                    idexContainer.getContainerProperty(item, NOMBRE_TAREA_PRPERTY).setValue(rsRecords.getString("Descripcion"));
                    idexContainer.getContainerProperty(item, PROMEDIO_PROPERTY).setValue(promedio);
                }while (rsRecords.next());
            }
        }catch (Exception ex){
            Notification.show("ERROR EN RESUMEN CALIFICACION EMPLEADOS", Notification.Type.ERROR_MESSAGE);
            System.out.println("ERROR EN RESUMEN CALIFICACION EMPLEADOS: " + ex.getMessage());
            ex.printStackTrace();
        }

    }

    private void llenarResumenGrid(String idex){
        resumenContainer.removeAllItems();

        Object item;
        String maestro = "";
        String jefe = "";
        String promedio = "";
        queryString = "SELECT IFNULL(AVG(CASE WHEN p.Cargo LIKE '%SUPERVISOR%' THEN ptic.valor END), 0) AS PromedioSupervisor, ";
        queryString +=       "IFNULL(AVG(CASE WHEN p.Cargo LIKE '%MAESTRO%' THEN ptic.valor END), 0) AS PromedioMaestro, ";
        queryString +=       "IFNULL(SUM(ptic.valor)/COUNT(ptic.valor), 0) AS Promedio, ";
        queryString +=       "cld.Descripcion AS NombreCaracteristica, cl.Descripcion AS NombreLista, pti.IDEX ";
        queryString += "FROM plan_trabajo_idex_ca ptic ";
        queryString += "INNER JOIN calidad_listas_detalle cld ON ptic.IdCaracteristica = cld.Id ";
        queryString += "LEFT JOIN calidad_listas cl ON cld.IdLista  = cl.Id ";
        queryString += "INNER JOIN proveedor p ON p.IDProveedor = ptic.IdEmpleado ";
        queryString += "INNER JOIN plan_trabajo_idex pti ON ptic.IdPlanTrabajoIdex = pti.id ";
        queryString += "WHERE pti.IDEX = " + idex + " ";
        queryString += "GROUP BY ptic.IdPlanTrabajoIdex, ptic.IdCaracteristica ";
        queryString += "ORDER BY ptic.IdPlanTrabajoIdex, cld.IdLista, ptic.IdCaracteristica ";

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if(rsRecords.next()){
                do{
                    maestro = (rsRecords.getDouble("PromedioMaestro") != 0d) ?  Utileria.format(rsRecords.getDouble("PromedioMaestro")) : "-.--";
                    jefe = (rsRecords.getDouble("PromedioSupervisor") != 0d) ?  Utileria.format(rsRecords.getDouble("PromedioSupervisor")) : "-.--";
                    promedio = (rsRecords.getDouble("Promedio") != 0d) ?  Utileria.format(rsRecords.getDouble("Promedio")) : "-.--";

                    item = resumenContainer.addItem();
                    resumenContainer.getContainerProperty(item, NOMBRE_AREA_PROPERTY).setValue(rsRecords.getString("NombreLista"));
                    resumenContainer.getContainerProperty(item, NOMBRE_CARACTERISTICAS_PROPERTY).setValue(rsRecords.getString("NombreCaracteristica"));
                    resumenContainer.getContainerProperty(item, CALIFICACION_MAESTRO_PROPERTY).setValue(maestro);
                    resumenContainer.getContainerProperty(item, CALIFICACION_SUPERVISOR_PROPERTY).setValue(jefe);
                    resumenContainer.getContainerProperty(item, PROMEDIO_PROPERTY).setValue(promedio);
                }while (rsRecords.next());
            }
        }catch (Exception ex){
            Notification.show("ERROR EN RESUMEN CALIFICACION EMPLEADOS", Notification.Type.ERROR_MESSAGE);
            System.out.println("ERROR EN RESUMEN CALIFICACION EMPLEADOS: " + ex.getMessage());
            ex.printStackTrace();
        }

    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent viewChangeEvent) {
        Page.getCurrent().setTitle("Sopdi - Calificacion Empleados");
    }


}
