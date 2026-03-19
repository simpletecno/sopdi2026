package com.simpletecno.sopdi.operativo;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ValoTheme;

import java.sql.ResultSet;
import java.sql.Statement;

public class ResumenCalificacionEmpleadoView extends VerticalLayout implements View {

    public Statement stQuery = null;
    public ResultSet rsRecords = null;
    public ResultSet rsRecords1 = null;
    private String queryString = "";

    UI mainUI;
    ComboBox empresaCbx;
    String empresa;

    static final String ID_PROVEEDOR_PROPERTY = "Id";
    static final String CARGO_PROVEEDOR_PROPERTY = "Cargo";
    static final String NOMBRE_PROVEEDOR_PROPERTY = "Nombre";
    static final String PROMEDIO_PROVEEDOR_PROPERTY = "Promedio";
    static final String PROMEDIO_JEFE_PROPERTY = "Promedio Jefe";

    Grid resumengrid;
    IndexedContainer resumenContainer = new IndexedContainer();

    public ResumenCalificacionEmpleadoView(){

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

        crearResumenGrid();
        llenarResumenGrid();
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

    private void crearResumenGrid(){
        resumenContainer.addContainerProperty(ID_PROVEEDOR_PROPERTY, String.class, "");
        resumenContainer.addContainerProperty(CARGO_PROVEEDOR_PROPERTY, String.class, "");
        resumenContainer.addContainerProperty(NOMBRE_PROVEEDOR_PROPERTY, String.class, "");
        resumenContainer.addContainerProperty(PROMEDIO_PROVEEDOR_PROPERTY, String.class, "");
        resumenContainer.addContainerProperty(PROMEDIO_JEFE_PROPERTY, String.class, "");

        resumengrid = new Grid("Reseumn Calificacion Empleado ", resumenContainer);
        resumengrid.setWidth("95%");
        resumengrid.setImmediate(true);
        resumengrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        resumengrid.setDescription("Seleccione un registro.");
        resumengrid.setHeightMode(HeightMode.ROW);
        resumengrid.setHeightByRows(20);
        resumengrid.setResponsive(true);
        resumengrid.setResponsive(true);
        resumengrid.setEditorBuffered(false);

        resumengrid.getColumn(ID_PROVEEDOR_PROPERTY).setExpandRatio(1);
        resumengrid.getColumn(CARGO_PROVEEDOR_PROPERTY).setExpandRatio(2);
        resumengrid.getColumn(NOMBRE_PROVEEDOR_PROPERTY).setExpandRatio(3);
        resumengrid.getColumn(PROMEDIO_PROVEEDOR_PROPERTY).setExpandRatio(1);
        resumengrid.getColumn(PROMEDIO_JEFE_PROPERTY).setExpandRatio(1);


        Grid.HeaderRow filterRow = resumengrid.appendHeaderRow();
        Grid.HeaderCell cell1 = filterRow.getCell(ID_PROVEEDOR_PROPERTY);
        Grid.HeaderCell cell2 = filterRow.getCell(CARGO_PROVEEDOR_PROPERTY);
        Grid.HeaderCell cell3 = filterRow.getCell(NOMBRE_PROVEEDOR_PROPERTY);

        TextField filterField1 = new TextField();
        TextField filterField2 = new TextField();
        TextField filterField3 = new TextField();

        filterField1.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField2.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField3.addStyleName(ValoTheme.TEXTFIELD_TINY);

        filterField1.setInputPrompt("Filtrar por documento");
        filterField1.setColumns(10);
        filterField2.setInputPrompt("Filtrar por documento");
        filterField2.setColumns(15);
        filterField3.setInputPrompt("Filtrar por documento");
        filterField3.setColumns(30);

        filterField1.addTextChangeListener(change -> {
            resumenContainer.removeContainerFilters(ID_PROVEEDOR_PROPERTY);
                    // (Re)create the filter if necessary
                    if (!change.getText().isEmpty()) {
                        resumenContainer.addContainerFilter(
                                new SimpleStringFilter(ID_PROVEEDOR_PROPERTY,
                                        change.getText(), true, false));
                    }
                }
        );
        cell1.setComponent(filterField1);

        filterField2.addTextChangeListener(change -> {
                    resumenContainer.removeContainerFilters(CARGO_PROVEEDOR_PROPERTY);
                    // (Re)create the filter if necessary
                    if (!change.getText().isEmpty()) {
                        resumenContainer.addContainerFilter(
                                new SimpleStringFilter(CARGO_PROVEEDOR_PROPERTY,
                                        change.getText(), true, false));
                    }
                }
        );
        cell2.setComponent(filterField2);

        filterField3.addTextChangeListener(change -> {
                    resumenContainer.removeContainerFilters(NOMBRE_PROVEEDOR_PROPERTY);
                    // (Re)create the filter if necessary
                    if (!change.getText().isEmpty()) {
                        resumenContainer.addContainerFilter(
                                new SimpleStringFilter(NOMBRE_PROVEEDOR_PROPERTY,
                                        change.getText(), true, false));
                    }
                }
        );
        cell3.setComponent(filterField3);

        addComponent(resumengrid);
        setComponentAlignment(resumengrid, Alignment.MIDDLE_CENTER);
    }

    private void llenarResumenGrid(){
        Object item;
        String normal = "";
        String jefe = "";

        queryString = "SELECT IFNULL(AVG(CASE WHEN ptir.EsJefe = 'SI' THEN ptirc.valor END), 0) AS PromedioJefe, ";
        queryString +=       "IFNULL(AVG(CASE WHEN ptir.EsJefe = 'NO' THEN ptirc.valor END), 0) AS PromedioNormal, ";
        queryString +=       "ptirc.IdEmpleado, p.Nombre, p.Cargo ";
        queryString += "FROM plan_trabajo_idex_rh_ca ptirc ";
        queryString += "INNER JOIN proveedor p ON p.IDProveedor = ptirc.IdEmpleado ";
        queryString += "INNER JOIN plan_trabajo_idex_rh ptir ";
        queryString +=         "ON ptirc.IdPlanTrabajoIdex = ptir.IdPlanTrabajoIdex ";
        queryString +=         "AND p.IDProveedor = ptir.IdEmpleado ";
        queryString += "GROUP BY ptirc.IdEmpleado, p.Nombre, p.Cargo ";
        queryString += "ORDER BY p.Cargo, p.IDProveedor";

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if(rsRecords.next()){
                do{

                    normal = (rsRecords.getDouble("PromedioNormal") != 0d) ?  Utileria.format(rsRecords.getDouble("PromedioNormal")) : "-.--";
                    jefe = (rsRecords.getDouble("Promediojefe") != 0d) ?  Utileria.format(rsRecords.getDouble("Promediojefe")) : "-.--";

                    item = resumenContainer.addItem();
                    resumenContainer.getContainerProperty(item, ID_PROVEEDOR_PROPERTY).setValue(rsRecords.getString("IdEmpleado"));
                    resumenContainer.getContainerProperty(item, CARGO_PROVEEDOR_PROPERTY).setValue(rsRecords.getString("Cargo"));
                    resumenContainer.getContainerProperty(item, NOMBRE_PROVEEDOR_PROPERTY).setValue(rsRecords.getString("Nombre"));
                    resumenContainer.getContainerProperty(item, PROMEDIO_PROVEEDOR_PROPERTY).setValue(normal);
                    resumenContainer.getContainerProperty(item, PROMEDIO_JEFE_PROPERTY).setValue(jefe);
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
