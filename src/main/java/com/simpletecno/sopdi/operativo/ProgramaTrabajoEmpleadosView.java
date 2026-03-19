/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.operativo;

import com.simpletecno.sopdi.SopdiUI;
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
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.Grid.HeaderCell;
import com.vaadin.ui.Grid.HeaderRow;
import com.vaadin.ui.themes.ValoTheme;

import java.io.*;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JAguirre
 */
@SuppressWarnings("serial")
public final class ProgramaTrabajoEmpleadosView extends VerticalLayout implements View {

    public Statement stQuery = null;
    public ResultSet rsRecords = null;
    public ResultSet rsRecords1 = null;

    protected static final String ENGRUPO = "1";
    protected static final String SINGRUPO = "2";

    protected static final String IDEMPLEADO_PROPERTY = "IdEmpleado";
    protected static final String NOMBRE_PROPERTY = "Nombre";
    protected static final String CARGO_PROPERTY = "Cargo";
    protected static final String TIPOASIGNACION_PROPERTY = "Tipo asignación";
    protected static final String ESJEFE_PROPERTY = "ES JEFE";
    protected static final String ESTATUS_PROPERTY = "Estatus";
    protected static final String RAZON_PROPERTY = "Razón";

    Button exportExcelBtn;
    Button updateBtn;

    TabSheet tabSheet;

    IndexedContainer enGrupoContainer = new IndexedContainer();
    IndexedContainer sinGrupoContainer = new IndexedContainer();
    final Grid enGrupoGrid = new Grid(ENGRUPO, enGrupoContainer);
    final Grid sinGrupoGrid = new Grid(SINGRUPO, sinGrupoContainer);

    final Grid.FooterRow enGrupoFooter = enGrupoGrid.appendFooterRow();

    final Grid.FooterRow sinGrupoFooter = sinGrupoGrid.appendFooterRow();

    final UI mainUI = UI.getCurrent();

    VerticalLayout mainLayout;

    boolean bandera = false; // esta variable cuando este en verdadero realizara un ingreso// en falso pedira que verifiquen los jefes asignados a los equipos
    String queryString;

    public ProgramaTrabajoEmpleadosView() {

        setResponsive(true);

        MarginInfo marginInfo = new MarginInfo(true, true, true, true);

        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setMargin(marginInfo);
        mainLayout.addStyleName("rcorners1");

        addComponent(mainLayout);

        Label titleLbl = new Label(((SopdiUI) mainUI).sessionInformation.getStrProjectName() + " -- EMPLEADOS -- ");
        titleLbl.addStyleName(ValoTheme.LABEL_H4);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h1_custom");

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setResponsive(true);
        titleLayout.setWidth("100%");
        titleLayout.setMargin(false);
        titleLayout.addComponents(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.TOP_LEFT);

        mainLayout.addComponent(titleLayout);
        mainLayout.setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        exportExcelBtn = new Button("Exportar a Excel");
        exportExcelBtn.setIcon(FontAwesome.FILE_EXCEL_O);
        exportExcelBtn.addStyleName(ValoTheme.BUTTON_LINK);
        exportExcelBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
            }
        });

        updateBtn = new Button("ACTUALIZAR CAMBIOS");
        updateBtn.setIcon(FontAwesome.EDIT);
        updateBtn.addStyleName(ValoTheme.BUTTON_DANGER);
        updateBtn.setDescription("Actualizar cambios");
        updateBtn.addListener((Button.ClickListener) (Button.ClickEvent event) -> {
            if (enGrupoContainer.size() == 0 && sinGrupoContainer.size() == 0) {
                Notification notif = new Notification("No hay empleados. Revise tabla de proveedores. Debe tener campo EsPlanilla = 1 para identificar a los empleados.",
                        Notification.Type.WARNING_MESSAGE);
                notif.setDelayMsec(1500);
                notif.setPosition(Position.MIDDLE_CENTER);
                notif.setIcon(FontAwesome.WARNING);
                notif.show(Page.getCurrent());
            } else {
                try {
                    stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

                    for (Object objectItem : enGrupoContainer.getItemIds()) {

                        if (String.valueOf(enGrupoContainer.getContainerProperty(objectItem, ESJEFE_PROPERTY).getValue()).equals("SI")) {

                            bandera = true;
                            if (bandera == true) {
                                queryString = " UPDATE proveedor SET ";
//                                queryString += " GrupoTrabajo = '" + String.valueOf(enGrupoContainer.getContainerProperty(objectItem, GRUPO_PROPERTY).getValue()) + "'";
                                queryString += " TipoAsignacion = '" + String.valueOf(enGrupoContainer.getContainerProperty(objectItem, TIPOASIGNACION_PROPERTY).getValue()) + "'";
//                                queryString += ",EsJefe = '" + (String.valueOf(enGrupoContainer.getContainerProperty(objectItem, ESJEFE_PROPERTY).getValue()).equals("SI") ? "1'" : "0'");
                                queryString += ",Cargo  = '" + String.valueOf(enGrupoContainer.getContainerProperty(objectItem, CARGO_PROPERTY).getValue()) + "'";
                                queryString += ",EstatusTrabajo = '" + String.valueOf(enGrupoContainer.getContainerProperty(objectItem, ESTATUS_PROPERTY).getValue()) + "'";
                                queryString += ",Razon = '" + String.valueOf(enGrupoContainer.getContainerProperty(objectItem, RAZON_PROPERTY).getValue()) + "'";
                                queryString += " WHERE IdProveedor = " + String.valueOf(enGrupoContainer.getContainerProperty(objectItem, IDEMPLEADO_PROPERTY).getValue());

                                stQuery.executeUpdate(queryString);
                            } else {
                                Notification.show("POR FAVOR REVISE LOS JEFES DE GRUPO YA QUE NO PUEDEN HABER 2 JEFES EN UN MISMO GRUPO.", Notification.Type.WARNING_MESSAGE);
                            }
                        } else {
                            queryString = " UPDATE proveedor SET ";
//                            queryString += " GrupoTrabajo = '" + String.valueOf(enGrupoContainer.getContainerProperty(objectItem, GRUPO_PROPERTY).getValue()) + "'";
                            queryString += " TipoAsignacion = '" + String.valueOf(enGrupoContainer.getContainerProperty(objectItem, TIPOASIGNACION_PROPERTY).getValue()) + "'";
                            queryString += ",EsJefe = '" + (String.valueOf(enGrupoContainer.getContainerProperty(objectItem, ESJEFE_PROPERTY).getValue()).equals("SI") ? "1'" : "0'");
                            queryString += ",Cargo = '" + String.valueOf(sinGrupoContainer.getContainerProperty(objectItem, CARGO_PROPERTY).getValue()) + "'";
                            queryString += ",EstatusTrabajo = '" + String.valueOf(enGrupoContainer.getContainerProperty(objectItem, ESTATUS_PROPERTY).getValue()) + "'";
                            queryString += ",Razon = '" + String.valueOf(enGrupoContainer.getContainerProperty(objectItem, RAZON_PROPERTY).getValue()) + "'";
                            queryString += " WHERE IdProveedor = " + String.valueOf(enGrupoContainer.getContainerProperty(objectItem, IDEMPLEADO_PROPERTY).getValue());

                            stQuery.executeUpdate(queryString);
                        }
                    }

                    for (Object objectItem : sinGrupoContainer.getItemIds()) {
                        queryString = " UPDATE proveedor SET ";
//                        queryString += " GrupoTrabajo = '" + String.valueOf(sinGrupoContainer.getContainerProperty(objectItem, GRUPO_PROPERTY).getValue()) + "'";
                        queryString += " TipoAsignacion = ''";
//                        queryString += ",EsJefe = '" + (String.valueOf(sinGrupoContainer.getContainerProperty(objectItem, ESJEFE_PROPERTY).getValue()).equals("SI") ? "1'" : "0'");
                        queryString += ",Cargo = '" + String.valueOf(sinGrupoContainer.getContainerProperty(objectItem, CARGO_PROPERTY).getValue()) + "'";
                        queryString += ",EstatusTrabajo = '" + String.valueOf(sinGrupoContainer.getContainerProperty(objectItem, ESTATUS_PROPERTY).getValue()) + "'";
                        queryString += ",Razon = '" + String.valueOf(sinGrupoContainer.getContainerProperty(objectItem, RAZON_PROPERTY).getValue()) + "'";
                        queryString += " WHERE IdProveedor = " + String.valueOf(sinGrupoContainer.getContainerProperty(objectItem, IDEMPLEADO_PROPERTY).getValue());
                        stQuery.executeUpdate(queryString);
                    }

                    Notification.show("ACTUALIZACION EXITOSA!!!", Notification.Type.HUMANIZED_MESSAGE);
                    fillGrid(ENGRUPO, enGrupoContainer, enGrupoGrid, enGrupoFooter);
                    fillGrid(SINGRUPO, sinGrupoContainer, sinGrupoGrid, sinGrupoFooter);

                } catch (Exception ex) {
                    System.out.println("Error al actualizar listado empleados en grupos : " + ex);
                    ex.printStackTrace();
                    Notification.show("ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                }

            }
        });

        crearTabSheet();

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setMargin(marginInfo);
        buttonsLayout.setWidth("100%");
//          buttonsLayout.setWidth("100%");
        buttonsLayout.addComponent(updateBtn);
        buttonsLayout.setComponentAlignment(updateBtn, Alignment.BOTTOM_CENTER);

        mainLayout.addComponent(buttonsLayout);
        mainLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);

        fillGrid(ENGRUPO, enGrupoContainer, enGrupoGrid, enGrupoFooter);
        fillGrid(SINGRUPO, sinGrupoContainer, sinGrupoGrid, sinGrupoFooter);

    }

    private void crearTabSheet() {
        tabSheet = new TabSheet();
        tabSheet.setSizeFull();
        tabSheet.addStyleName(ValoTheme.TABSHEET_FRAMED);
        tabSheet.addStyleName(ValoTheme.TABSHEET_PADDED_TABBAR);
        tabSheet.addStyleName(ValoTheme.TABSHEET_CENTERED_TABS);
        tabSheet.addStyleName(ValoTheme.TABSHEET_EQUAL_WIDTH_TABS);

        addTabEnGrupo();
        addTabSinGrupo();

        mainLayout.addComponent(tabSheet);
        mainLayout.setComponentAlignment(tabSheet, Alignment.MIDDLE_CENTER);
    }

    private void addTabEnGrupo() {
        TabSheet.Tab newTab = tabSheet.addTab(createGrid(enGrupoContainer, enGrupoGrid, enGrupoFooter), "CON PLAN DE TRABAJO");
        newTab.setIcon(FontAwesome.LOCK);
        newTab.setId("1");
        newTab.setStyleName("dirtyTabCaption");
    }

    private void addTabSinGrupo() {
        TabSheet.Tab newTab = tabSheet.addTab(createGrid(sinGrupoContainer, sinGrupoGrid, sinGrupoFooter), "SIN PLAN DE TRABAJO");
        newTab.setIcon(FontAwesome.EXCHANGE);
        newTab.setId("2");
//        newTab.setStyleName("dirtyTabCaption");
    }

    public VerticalLayout createGrid(IndexedContainer indexedContainer, Grid grid, Grid.FooterRow gridFooter) {

        VerticalLayout reportLayout = new VerticalLayout();
        reportLayout.setWidth("100%");
        reportLayout.addStyleName("rcorners3");
        reportLayout.setResponsive(true);
        reportLayout.setMargin(true);

        indexedContainer.addContainerProperty(IDEMPLEADO_PROPERTY, String.class, null);
        indexedContainer.addContainerProperty(NOMBRE_PROPERTY, String.class, null);
        indexedContainer.addContainerProperty(CARGO_PROPERTY, String.class, null);
        indexedContainer.addContainerProperty(TIPOASIGNACION_PROPERTY, String.class, null);
        indexedContainer.addContainerProperty(ESJEFE_PROPERTY, String.class, null);
        indexedContainer.addContainerProperty(ESTATUS_PROPERTY, String.class, null);
        indexedContainer.addContainerProperty(RAZON_PROPERTY, String.class, null);

        grid.setWidth("100%");
        grid.setImmediate(true);
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.setDescription("Seleccione un registro.");
        grid.setHeightMode(HeightMode.ROW);
        grid.setHeightByRows(15);
        grid.setResponsive(true);
        grid.setEditorBuffered(false);
        grid.setEditorEnabled(true);
        grid.getColumn(TIPOASIGNACION_PROPERTY).setEditorField(getComboAsignacion());
        grid.getColumn(ESTATUS_PROPERTY).setEditorField(getComboState());
        grid.getColumn(RAZON_PROPERTY).setEditorField(getEditTextRazon());
        grid.getColumn(CARGO_PROPERTY).setEditorField(getComboCargo());
        grid.addItemClickListener((event) -> {
            if (event != null) {
                grid.editItem(event.getItemId());
            }
        });

        grid.setResponsive(true);
        grid.setEditorBuffered(false);

        grid.getColumn(IDEMPLEADO_PROPERTY).setExpandRatio(1);
        grid.getColumn(NOMBRE_PROPERTY).setExpandRatio(4);
        grid.getColumn(CARGO_PROPERTY).setExpandRatio(1);
        grid.getColumn(TIPOASIGNACION_PROPERTY).setExpandRatio(2);
        grid.getColumn(ESJEFE_PROPERTY).setExpandRatio(1);
        grid.getColumn(ESTATUS_PROPERTY).setExpandRatio(2);
        grid.getColumn(RAZON_PROPERTY).setExpandRatio(3);

        HeaderRow filterRow = grid.appendHeaderRow();

        HeaderCell cell = filterRow.getCell(IDEMPLEADO_PROPERTY);

        TextField filterField = new TextField();
        filterField.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField.setInputPrompt("Filtrar");
        filterField.setColumns(5);

        filterField.addTextChangeListener(change -> {
            indexedContainer.removeContainerFilters(IDEMPLEADO_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                indexedContainer.addContainerFilter(
                        new SimpleStringFilter(IDEMPLEADO_PROPERTY,
                                change.getText(), true, false));
            }
            setTotal(indexedContainer, grid, gridFooter);
        });
        cell.setComponent(filterField);

        HeaderCell cell0 = filterRow.getCell(NOMBRE_PROPERTY);

        TextField filterField0 = new TextField();
        filterField0.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField0.setInputPrompt("Filtrar");
        filterField0.setColumns(20);

        filterField0.addTextChangeListener(change -> {
            indexedContainer.removeContainerFilters(NOMBRE_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                indexedContainer.addContainerFilter(
                        new SimpleStringFilter(NOMBRE_PROPERTY,
                                change.getText(), true, false));
            }
            setTotal(indexedContainer, grid, gridFooter);
        });
        cell0.setComponent(filterField0);

        gridFooter.getCell(NOMBRE_PROPERTY).setText("Totales");
        gridFooter.getCell(NOMBRE_PROPERTY).setStyleName("rightalign");

        reportLayout.addComponent(grid);
        reportLayout.setComponentAlignment(grid, Alignment.MIDDLE_CENTER);

        return reportLayout;
    }

    private Field<?> getComboAsignacion() {

        ComboBox comboBox = new ComboBox();
        comboBox.setImmediate(true);
        comboBox.setNullSelectionAllowed(false);
        comboBox.setTextInputAllowed(false);
        comboBox.setInvalidAllowed(false);
        comboBox.setNewItemsAllowed(false);
        comboBox.clear();
        comboBox.setWidth("15em");

        comboBox.addItem("PERMANENTE");
        comboBox.addItem("TEMPORAL");

        return comboBox;
    }

    private Field<?> getComboState() {

        ComboBox comboBox = new ComboBox();
        comboBox.setImmediate(true);
        comboBox.setNullSelectionAllowed(false);
        comboBox.setTextInputAllowed(false);
        comboBox.setInvalidAllowed(false);
        comboBox.setNewItemsAllowed(false);
        comboBox.clear();
        comboBox.setWidth("15em");

        comboBox.addItem("PRESENTE");
        comboBox.addItem("AUSENTE");
        comboBox.addItem("DE BAJA");
        comboBox.addItem("VACACIONES");
        comboBox.select("PRESENTE");

        return comboBox;
    }

    private Field<?> getComboEsJefe() {

        ComboBox comboBox = new ComboBox();
        comboBox.setImmediate(true);
        comboBox.setNullSelectionAllowed(false);
        comboBox.setTextInputAllowed(false);
        comboBox.setInvalidAllowed(false);
        comboBox.setNewItemsAllowed(false);
        comboBox.clear();
        comboBox.setWidth("6em");

        comboBox.addItem("SI");
        comboBox.addItem("NO");
        comboBox.select("NO");
        return comboBox;
    }

    private Field<?> getComboCargo() {

        ComboBox comboBox = new ComboBox();
        comboBox.setImmediate(true);
        comboBox.setNullSelectionAllowed(false);
        comboBox.setTextInputAllowed(false);
        comboBox.setInvalidAllowed(false);
        comboBox.setNewItemsAllowed(false);
        comboBox.clear();
        comboBox.setWidth("6em");

        comboBox.addItem("DIRECTOR");
        comboBox.addItem("GERENTE GENERAL");
        comboBox.addItem("GERENTE ADMINISTRATIVO");
        comboBox.addItem("GERENTE OPERACIONES");
        comboBox.addItem("GERENTE FINANCIERO");
        comboBox.addItem("SECRETARIA");
        comboBox.addItem("RECEPCIONISTA");
        comboBox.addItem("ASISTENTE");
        comboBox.addItem("CONTADOR");
        comboBox.addItem("AUXLIAR CONTABLE");
        comboBox.addItem("ASESOR VENTAS");
        comboBox.addItem("SUPERVISOR VENTAS");
        comboBox.addItem("SUPERVISOR OBRAS");
        comboBox.addItem("SUPERVISOR ARQUITECTURA");
        comboBox.addItem("CONTROLLER");
        comboBox.addItem("DIBUJANTE");
        comboBox.addItem("PROGRAMADOR");
        comboBox.addItem("MAESTRO OBRAS");
        comboBox.addItem("BODEGUERO");
        comboBox.addItem("CAPORAL");
        comboBox.addItem("PLOMERO");
        comboBox.addItem("ELECTRICISTA");
        comboBox.addItem("INSTALADOR PISO");
        comboBox.addItem("RH1");
        comboBox.addItem("RH2");
        comboBox.addItem("CONSERJE");
        comboBox.addItem("JARDINERO");

        return comboBox;
    }

    private Field<?> getEditTextRazon() {

        ComboBox comboBox = new ComboBox();
        comboBox.setImmediate(true);
        comboBox.setNullSelectionAllowed(false);
        comboBox.setTextInputAllowed(true);
        comboBox.setInvalidAllowed(true);
        comboBox.setNewItemsAllowed(true);
        comboBox.clear();
        comboBox.setWidth("6em");

        comboBox.addItem("");
        comboBox.addItem("Ausencia");
        comboBox.addItem("Enfermedad");
        comboBox.addItem("Permiso autorizado");
        comboBox.addItem("Suspensión IGSS");
        comboBox.addItem("VACACIONES");
        comboBox.addItem("Retiro de labores");

        return comboBox;
    }

    public void fillGrid(String tipo, IndexedContainer indexedContainer, Grid grid, Grid.FooterRow gridFooter) {

        indexedContainer.removeAllItems();

        String queryString;

        queryString = "SELECT * ";
        queryString += " FROM proveedor ";
        queryString += " WHERE EsPlanilla = 1";
        queryString += " AND Inhabilitado = 0";
        if (tipo.equals(ENGRUPO)) {
            queryString += " AND IdProveedor IN (Select PTIR.IdEmpleado " +
                    " FROM plan_trabajo_idex_rh PTIR " +
                    " INNER JOIN plan_trabajo_idex PTI ON PTI.Id = PTIR.IdPlanTrabajoIdex " +
                    " INNER JOIN plan_trabajo PT ON PT.Id = PTI.IdPlanTrabajo " +
                    " INNER JOIN project_tarea PTA ON PTA.Idex = PTI.Idex" +
                    " INNER JOIN project PJ ON PJ.IdProject = PTA.IdProject AND PJ.Estatus = 'ACTIVO'" +
                    " WHERE ISNULL(PTI.FechaFinTrabajo) = 1)";
        } else {
            queryString += " AND IdProveedor NOT IN (Select PTIR.IdEmpleado " +
                    " FROM plan_trabajo_idex_rh PTIR " +
                    " INNER JOIN plan_trabajo_idex PTI ON PTI.Id = PTIR.IdPlanTrabajoIdex " +
                    " INNER JOIN plan_trabajo PT ON PT.Id = PTI.IdPlanTrabajo " +
                    " INNER JOIN project_tarea PTA ON PTA.Idex = PTI.Idex" +
                    " INNER JOIN project PJ ON PJ.IdProject = PTA.IdProject AND PJ.Estatus = 'ACTIVO'" +
                    " WHERE ISNULL(PTI.FechaFinTrabajo) = 1)";
        }
        queryString += " AND EstatusTrabajo <> 'DE BAJA'";
        queryString += " AND IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
        queryString += " ORDER BY Nombre";

Logger.getLogger(this.getClass().getName()).log(Level.INFO, "\n\n" + queryString);

        try {

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado
                do {

                    Object itemId = indexedContainer.addItem();

                    indexedContainer.getContainerProperty(itemId, IDEMPLEADO_PROPERTY).setValue(rsRecords.getString("IdProveedor"));
                    indexedContainer.getContainerProperty(itemId, NOMBRE_PROPERTY).setValue(rsRecords.getString("Nombre"));
                    indexedContainer.getContainerProperty(itemId, CARGO_PROPERTY).setValue(rsRecords.getString("Cargo"));
//                    indexedContainer.getContainerProperty(itemId, GRUPO_PROPERTY).setValue(rsRecords.getString("GrupoTrabajo"));
                    indexedContainer.getContainerProperty(itemId, TIPOASIGNACION_PROPERTY).setValue(rsRecords.getString("TipoAsignacion"));
                    indexedContainer.getContainerProperty(itemId, ESJEFE_PROPERTY).setValue(rsRecords.getString("EsJefe").equals("1") ? "SI" : "");
                    indexedContainer.getContainerProperty(itemId, ESTATUS_PROPERTY).setValue(rsRecords.getString("EstatusTrabajo"));
                    indexedContainer.getContainerProperty(itemId, RAZON_PROPERTY).setValue(rsRecords.getString("Razon"));

                } while (rsRecords.next());
            }
        } catch (Exception ex) {
            Logger.getLogger(ProgramaTrabajoEmpleadosView.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de detalle de empleados : " + ex.getMessage());
            Notification.show("Error al intentar leer detalle de empleados..!", Notification.Type.ERROR_MESSAGE);
        }

    }

    public void verificarJefes(String grupo, String empleadoNombre) {

        try {
            bandera = false;
            int contador = 0;
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            queryString = "SELECT * FROM proveedor";
            queryString += " WHERE GrupoTrabajo = '" + grupo + "'";
            queryString += " AND EsJefe = 1";

            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {

                do {
                    contador = contador + 1;
                    if (contador == 1 && empleadoNombre.equals(rsRecords.getString("Nombre"))) { /// ES EL MISMO JEFE SE PUEDE ACTUALIZAR
                        bandera = true;
                    } else if(contador > 1) { // HAY MAS DE UN JEFE NO SE PUEDE ACTUALIZAR
                        bandera = false;
                    }else{
                        bandera = false;  //NO SE PUEDE ACTUALIZAR
                    }
                } while (rsRecords.next());
            } else {
                bandera = true; /// SE PUEDE ACTUALIZAR
            }
                        
        } catch (Exception e) {
            System.out.println("Error al intentar buscar jefe " + e);
            e.printStackTrace();
        }
    }

    private void setTotal(IndexedContainer indexedContainer, Grid grid, Grid.FooterRow gridFooter) {
//        gridFooter.getCell(GRUPO_PROPERTY).setText(String.valueOf(indexedContainer.size()) + " empleados.");
    }

    public boolean exportToExcel() {
//        if (integracionGrid.getHeightByRows() > 0) {
//            TableHolder tableHolder = new DefaultTableHolder(integracionGrid);
//            ExcelExport excelExport = new ExcelExport(tableHolder);
//            excelExport.excludeCollapsedColumns();
//            excelExport.setDisplayTotals(false);
//            String fileexport;
////            fileexport = (empresa + "_" + empresaLbl.getValue().substring(5, empresaLbl.getValue().length()).replaceAll(" ", "_").replaceAll(",", "_").replaceAll("[()]", "").replaceAll("[.]", "").replaceAll("ñ", "n").replaceAll("Ñ", "N").replaceAll("ó", "o").replaceAll("é", "") + "_INTEGRACION_INICIAL.xlsx");
//            excelExport.setExportFileName(fileexport);
//            excelExport.export();
//        }
        return true;
    }

    /**
     * This class creates a streamresource. This class implements the
     * StreamSource interface which defines the getStream method.
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

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        // TODO Auto-generated method stub

    }
}
