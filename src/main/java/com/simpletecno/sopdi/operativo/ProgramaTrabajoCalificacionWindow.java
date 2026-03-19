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
import com.vaadin.server.FontAwesome;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.Grid.HeaderCell;
import com.vaadin.ui.Grid.HeaderRow;
import com.vaadin.ui.themes.ValoTheme;

import java.io.*;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JAguirre
 */
@SuppressWarnings("serial")
public final class ProgramaTrabajoCalificacionWindow extends Window {

    public Statement stQuery    = null;
    public Statement stQuery1   = null;
    public ResultSet rsRecords  = null;
    public ResultSet rsRecords1 = null;

    protected static final String CALIDAD_M = "CALIDAD MAESTRO";
    protected static final String CALIDAD_S = "CALIDAD SUPERVISOR";
    protected static final String PERSONAL = "PERSONAL";
    protected static final String JEFE = "JEFE";
    protected static final String MAESTRO = "MAESTRO";

    protected static final String NOMBRE_PROPERTY = "Nombre";
    protected static final String CARACTERISTICA_PROPERTY = "Característica";
    protected static final String VALOR_PROPERTY = "Valor";

    Button exportExcelBtn;

    TabSheet tabSheet;

    IndexedContainer calidadMContainer = new IndexedContainer();
    IndexedContainer calidadSContainer = new IndexedContainer();
    IndexedContainer personalContainer = new IndexedContainer();
    IndexedContainer jefeContainer = new IndexedContainer();
    IndexedContainer maestroContainer = new IndexedContainer();
    IndexedContainer supervisionesContainer = new IndexedContainer();

    final Grid calidadMGrid = new Grid(CALIDAD_M, calidadMContainer);
    final Grid calidadSGrid = new Grid(CALIDAD_S,calidadSContainer);
    final Grid personalGrid = new Grid(PERSONAL,personalContainer);
    final Grid jefeGrid = new Grid(JEFE, jefeContainer);
    final Grid maestroGrid  = new Grid(MAESTRO,maestroContainer);
    final Grid supervisionesGrid  = new Grid("Supervisiones",supervisionesContainer);

    final Grid.FooterRow calidadMFooter =  calidadMGrid.appendFooterRow();;
    final Grid.FooterRow calidadSFooter =  calidadSGrid.appendFooterRow();;
    final Grid.FooterRow personalFooter =  personalGrid.appendFooterRow();;
    final Grid.FooterRow jefeFooter =  jefeGrid.appendFooterRow();;
    final Grid.FooterRow maestroFooter  =  maestroGrid.appendFooterRow();;

    final UI mainUI = UI.getCurrent();

    static DecimalFormat moneyFormat = new DecimalFormat("#,###,##0.00");
    static DecimalFormat integerFormat2 = new DecimalFormat("##,##0");

    String programaTrabajoIdex;
    String centroCosto;
    String idex;
    String descripcion;

    VerticalLayout mainLayout;

    public ProgramaTrabajoCalificacionWindow(
            String programaTrabajoIdex,
            String centroCosto,
            String idex,
            String descripcion
    ) {

        this.programaTrabajoIdex = programaTrabajoIdex;
        this.centroCosto = centroCosto;
        this.idex = idex;
        this.descripcion = descripcion;

        setResponsive(true);
        setWidth("80%");
        setHeight("80%");

        MarginInfo marginInfo = new MarginInfo(true, true, true, true);

        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.addStyleName("rcorners3");

        setContent(mainLayout);

        Label titleLbl = new Label(((SopdiUI) mainUI).sessionInformation.getStrProjectName() + " -- CALIFICACIONES ");
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
//        exportExcelBtn.setWidth(130,Sizeable.UNITS_PIXELS);
        exportExcelBtn.addStyleName(ValoTheme.BUTTON_LINK);
        exportExcelBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
//                if (integracionContainer.size() > 0) {
//                    PronetWebPayMain.getInstance().mainWindow.getWindow().showNotification("EN CONSTRUCCION!");            
//                    exportToExcel();
//                }
            }
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setMargin(true);
        buttonsLayout.addComponent(exportExcelBtn);
        buttonsLayout.setComponentAlignment(exportExcelBtn, Alignment.BOTTOM_RIGHT);

        crearTabSheet();

        fillCalificaciones(CALIDAD_M, calidadMContainer, calidadMGrid, calidadMFooter);
        fillCalificaciones(CALIDAD_S, calidadSContainer, calidadSGrid, calidadSFooter);
        fillCalificaciones(PERSONAL, personalContainer, personalGrid, personalFooter);
        fillCalificaciones(JEFE, jefeContainer, jefeGrid, jefeFooter);
        fillCalificaciones(MAESTRO, maestroContainer, maestroGrid, maestroFooter);

        llenarGridSupervisiones();

//        addComponent(buttonsLayout);
//        setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);
    }

    private void crearTabSheet() {
        tabSheet = new TabSheet();
        tabSheet.addStyleName(ValoTheme.TABSHEET_FRAMED);
        tabSheet.addStyleName(ValoTheme.TABSHEET_PADDED_TABBAR);
        tabSheet.addStyleName(ValoTheme.TABSHEET_CENTERED_TABS);
        tabSheet.addStyleName(ValoTheme.TABSHEET_EQUAL_WIDTH_TABS);

        if(((SopdiUI) mainUI).sessionInformation.getStrUserProfileName().toUpperCase().contains("JEFE")) {
            addTabPersonal();
        }
        else if(((SopdiUI) mainUI).sessionInformation.getStrUserProfileName().toUpperCase().contains("MAESTRO")) {
            addTabICalidad(CALIDAD_M);
            addTabPersonal();
            addTabJefe();
        }
        else {
            addTabPersonal();
            addTabJefe();
            addTabMaestro();
            addTabICalidad(CALIDAD_M);
            addTabICalidad(CALIDAD_S);
            addTabSupervisiones();
        }

        mainLayout.addComponent(tabSheet);
        mainLayout.setComponentAlignment(tabSheet, Alignment.TOP_CENTER);
    }

    private void addTabICalidad(String quien) {
        TabSheet.Tab newTab;

        if(quien.equals(CALIDAD_M)) {
            newTab = tabSheet.addTab(createGrid(calidadMContainer, calidadMGrid, calidadMFooter, CARACTERISTICA_PROPERTY), CALIDAD_M);
        }
        else {
            newTab = tabSheet.addTab(createGrid(calidadSContainer, calidadSGrid, calidadSFooter,CARACTERISTICA_PROPERTY), CALIDAD_S);
        }
        newTab.setIcon(FontAwesome.FLAG_CHECKERED);
        newTab.setId("1");
        newTab.setStyleName("dirtyTabCaption");
    }

    private void addTabPersonal() {
        TabSheet.Tab newTab = tabSheet.addTab(createGrid( personalContainer, personalGrid, personalFooter, NOMBRE_PROPERTY), PERSONAL);
        newTab.setIcon(FontAwesome.ALIGN_CENTER);
        newTab.setId("2");
        newTab.setStyleName("dirtyTabCaption");
    }

    private void addTabJefe() {
        TabSheet.Tab newTab = tabSheet.addTab(createGrid( jefeContainer, jefeGrid, jefeFooter, NOMBRE_PROPERTY), JEFE);
        newTab.setIcon(FontAwesome.CALENDAR_O);
        newTab.setId("3");
        newTab.setStyleName("dirtyTabCaption");
    }

    private void addTabMaestro() {
        TabSheet.Tab newTab = tabSheet.addTab(createGrid(maestroContainer, maestroGrid, maestroFooter, NOMBRE_PROPERTY), MAESTRO);
        newTab.setIcon(FontAwesome.CHECK);
        newTab.setId("4");
        newTab.setStyleName("dirtyTabCaption");
    }

    private void addTabSupervisiones() {
        TabSheet.Tab newTab = tabSheet.addTab(createGrid(supervisionesContainer, supervisionesGrid, null, CARACTERISTICA_PROPERTY), "SUPERVISIONES");
        newTab.setIcon(FontAwesome.EYE);
        newTab.setId("5");
        newTab.setStyleName("dirtyTabCaption");
    }

    public VerticalLayout createGrid(IndexedContainer indexedContainer, Grid grid, Grid.FooterRow gridFooter, String propertyX) {

        VerticalLayout reportLayout = new VerticalLayout();
        reportLayout.setWidth("98%");
        reportLayout.addStyleName("rcorners3");
        reportLayout.setResponsive(true);
        reportLayout.setMargin(true);

        if(indexedContainer.equals(supervisionesContainer)) {
            indexedContainer.addContainerProperty("fecha", String.class, "");
            indexedContainer.addContainerProperty("supervisor", String.class, "");
            indexedContainer.addContainerProperty("caracteristica", String.class, "");
            indexedContainer.addContainerProperty("comentario", String.class, "");
        }
        else {
            indexedContainer.addContainerProperty(propertyX, String.class, "");
            indexedContainer.addContainerProperty(VALOR_PROPERTY, Double.class, 0.00);
        }

        grid.setWidth("100%");
        grid.setImmediate(true);
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.setDescription("Seleccione un registro.");
        grid.setHeightMode(HeightMode.ROW);
        grid.setHeightByRows(15);
        grid.setResponsive(true);
        grid.setEditorBuffered(false);

        grid.setResponsive(true);
        grid.setEditorBuffered(false);

        grid.setCellStyleGenerator((Grid.CellReference cellReference) -> {

            if (VALOR_PROPERTY.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else {
                return null;
            }

        });

        grid.addSelectionListener(new SelectionListener() {
            @Override
            public void select(SelectionEvent event) {
                if (grid.getSelectedRow() != null) {
//                    mostrarAnticipos();
                }
            }
        });

        if(gridFooter != null) {
            gridFooter.getCell(propertyX).setText("Promedio");
            gridFooter.getCell(propertyX).setStyleName("rightalign");
            gridFooter.getCell(VALOR_PROPERTY).setText("0.00");
            gridFooter.getCell(VALOR_PROPERTY).setStyleName("centeralign");
        }
        reportLayout.addComponent(grid);
        reportLayout.setComponentAlignment(grid, Alignment.MIDDLE_CENTER);

        return reportLayout;
    }

    public void fillCalificaciones(String tipo, IndexedContainer indexedContainer, Grid grid, Grid.FooterRow gridFooter) {

        indexedContainer.removeAllItems();

        if(indexedContainer.getContainerPropertyIds().size() == 0) {
            return;
        }

        gridFooter.getCell(VALOR_PROPERTY).setText("0.00");

        String queryString;

        if(tipo.equals(CALIDAD_M) || tipo.equals(CALIDAD_S) ) {
            queryString = "SELECT cc.Descripcion, ptic.Valor ";
            queryString += " FROM calidad_listas_detalle cc ";
            queryString += " INNER JOIN plan_trabajo_idex_ca ptic ON ptic.IdCaracteristica  = cc.Id ";
            queryString += " AND ptic.IdPlanTrabajoIdex = " + programaTrabajoIdex;
            if(tipo.equals(CALIDAD_M)) {
                queryString += " WHERE ptic.IdEmpleado = (SELECT cc.IdMaestroObras FROM centro_costo cc WHERE cc.CodigoCentroCosto = '" + centroCosto + "')";
            }
            else {
                queryString += " WHERE ptic.IdEmpleado = (SELECT cc.IdSupervisor FROM centro_costo cc WHERE cc.CodigoCentroCosto = '" + centroCosto + "')";
            }
        }
        else {
            queryString = "SELECT rhca.*, prv.Nombre";
            queryString += " FROM plan_trabajo_idex_rh_ca rhca";
            queryString += " INNER JOIN plan_trabajo_idex_rh rh ON rh.IdEmpleado = rhca.IdEmpleado AND rh.IdPlanTrabajoIdex = rhca.IdPlanTrabajoIdex ";
            queryString += " INNER JOIN proveedor prv ON prv.IdProveedor = rh.IdEmpleado";
            queryString += " WHERE rh.IdPlanTrabajoIdex = "  + programaTrabajoIdex;
            queryString += " AND prv.IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
            if(tipo.equals(PERSONAL)) {
                queryString += " And rh.Cargo In ('RH1', 'RH2') ";
                queryString += " And rh.EsJefe <> 'SI'";
            } else if(tipo.equals(JEFE)) {
                queryString += " And rh.EsJefe = 'SI'";
            } else if(tipo.equals(MAESTRO)) {
                queryString += " AND rh.Cargo Like 'MAESTRO%'";
            }
            queryString += " ORDER BY prv.Nombre";
        }

//if(((SopdiUI) mainUI).sessionInformation.getStrUserProfileName().toUpperCase().contains("MAESTRO")) {

System.out.println("\n\nQuery Calificacioes de " + tipo + " --> "  + queryString);

        try {

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado

                int promedio = 0;

                do {

                    Object itemId = indexedContainer.addItem();

                    if (tipo.equals(CALIDAD_M) || tipo.equals(CALIDAD_S) ) {
                        indexedContainer.getContainerProperty(itemId, CARACTERISTICA_PROPERTY).setValue(rsRecords.getString("Descripcion"));
                    }
                    else {
                        indexedContainer.getContainerProperty(itemId, NOMBRE_PROPERTY).setValue(rsRecords.getString("Nombre"));
                    }

                    indexedContainer.getContainerProperty(itemId, VALOR_PROPERTY).setValue(rsRecords.getDouble("Valor"));

                    promedio+= rsRecords.getInt("Valor");

                } while (rsRecords.next());

                gridFooter.getCell(VALOR_PROPERTY).setText(String.valueOf(promedio/indexedContainer.size()));
            }
        } catch (Exception ex) {
            Logger.getLogger(ProgramaTrabajoCalificacionWindow.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de detalle de integracion : " + ex.getMessage());
            Notification.show("Error al intentar leer detalle de integracion..!", Notification.Type.ERROR_MESSAGE);
        }

    }

    private void llenarGridSupervisiones() {

        String queryString = "SELECT plan_trabajo_idex_su.*, prv.Nombre, car.Descripcion ";
        queryString += " FROM plan_trabajo_idex_su ";
        queryString += " INNER JOIN proveedor prv ON prv.IdProveedor = plan_trabajo_idex_su.IdEmpleado";
        queryString += " INNER JOIN calidad_listas_detalle car ON car.Id = plan_trabajo_idex_su.IdCaracteristica ";
        queryString += " WHERE plan_trabajo_idex_su.IdPlanTrabajoIdex = " + programaTrabajoIdex;
        queryString += " AND prv.IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
        queryString += " AND LENGTH(TRIM(plan_trabajo_idex_su.Comentario)) > 0";
        queryString += " ORDER BY plan_trabajo_idex_su.Id";

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado

                Object objectItem;

                do {
                    objectItem = supervisionesContainer.addItem();
                    supervisionesContainer.getContainerProperty(objectItem, "fecha").setValue(rsRecords.getString("FechaYHora"));
                    supervisionesContainer.getContainerProperty(objectItem, "supervisor").setValue(rsRecords.getString("Nombre"));
                    supervisionesContainer.getContainerProperty(objectItem, "caracteristica").setValue(rsRecords.getString("Descripcion"));
                    supervisionesContainer.getContainerProperty(objectItem, "comentario").setValue(rsRecords.getString("Comentario"));

                } while(rsRecords.next());
            }
        }
        catch (Exception ex1) {
            Notification.show("ERROR DEL SISTEMA AL BUSCAR SUPERVISIONES DE CALIDAD", Notification.Type.ERROR_MESSAGE);
            System.out.println("ERROR AL INTENTAR BUSCAR BUSCAR SUPERVISIONES DE CALIDAD : " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public boolean exportToExcel() {
//        if (integracionGrid.getHeightByRows() > 0) {
//            TableHolder tableHolder = new DefaultTableHolder(integracionGrid);
//            ExcelExport excelExport = new ExcelExport(tableHolder);
//            excelExport.excludeCollapsedColumns();
//            excelExport.setDisplayTotals(false);
//            String fileexport;
////            fileexport = (empresa + "_" + empresaLbl.getValue().substring(5, empresaLbl.getValue().length()).replaceAll(" ", "_").replaceAll(",", "_").replaceAll("[()]", "").replaceAll("[.]", "").replaceAll("ñ", "n").replaceAll("Ñ", "N").replaceAll("ó", "o").replaceAll("é", "") + "_INTEGRACION_CALIDAD.xlsx");
//            excelExport.setExportFileName(fileexport);
//            excelExport.export();
//        }
        return true;
    }

}
