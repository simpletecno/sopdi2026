/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.operativo;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.addon.tableexport.DefaultTableHolder;
import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.addon.tableexport.TableHolder;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author joseaguirre
 */
public class ProgramaTrabajoEmpleadoLibreForm extends Window {

    VerticalLayout mainLayout;
    MarginInfo  marginInfo;

    Statement stQuery = null;
    Statement stQuery1 = null;
    ResultSet rsRecords = null;
    ResultSet rsRecords1 = null;

    Grid rhGrid;
    IndexedContainer rhContainer = new IndexedContainer();

    DateField fechaFinal;
    UI mainUI;

    public ProgramaTrabajoEmpleadoLibreForm(
            DateField fechaFinal
    ) {
        this.fechaFinal = fechaFinal;
        this.mainUI = UI.getCurrent();

        setResponsive(true);
        setCaption("EMPLEADOS SIN ASIGNACION DEL : " + Utileria.getFechaYYYYMMDD_1(new java.util.Date()) + " AL : " + Utileria.getFechaYYYYMMDD_1(fechaFinal.getValue()));
        setWidth("60%");
        setHeight("70%");
        
        marginInfo = new MarginInfo(true,true,false,true);
        
        mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();

        createGrid();

        llenarGridEmpleados();

        setContent(mainLayout);
    }

    private void createGrid() {
        rhContainer.addContainerProperty("fecha", String.class, "");
        rhContainer.addContainerProperty("idEmpleado", String.class, "NO TIENE");
        rhContainer.addContainerProperty("nombre", String.class, "NO TIENE");

        rhGrid = new Grid("", rhContainer);
        rhGrid.setWidth("100%");
        rhGrid.setImmediate(true);
//        rhGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
//        rhGrid.setDescription("Seleccione un registro.");
        rhGrid.setHeightMode(HeightMode.ROW);
        rhGrid.setHeightByRows(15);
       //rhGrid.setSizeFull();

        mainLayout.addComponent(rhGrid);

        Button generarExcel = new Button("Excel");
        generarExcel.setIcon(FontAwesome.FILE_EXCEL_O);
        generarExcel.addStyleName(ValoTheme.BUTTON_PRIMARY);

        generarExcel.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (rhContainer.size() > 0) {
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

        mainLayout.addComponent(generarExcel);
        mainLayout.setComponentAlignment(generarExcel, Alignment.BOTTOM_CENTER);

    }
    void llenarGridEmpleados() {

        rhContainer.removeAllItems();

        long daysDiff = 0;

        try {
            java.util.GregorianCalendar dateBefore = new java.util.GregorianCalendar();
            dateBefore.setTime(new java.util.Date());

            java.util.GregorianCalendar dateAfter = new java.util.GregorianCalendar();
            dateAfter.setTime(fechaFinal.getValue());

            long dateBeforeInMs = dateBefore.getTimeInMillis();
            long dateAfterInMs = dateAfter.getTimeInMillis();

            long timeDiff = Math.abs(dateAfterInMs - dateBeforeInMs);

            daysDiff = TimeUnit.DAYS.convert(timeDiff, TimeUnit.MILLISECONDS);

//System.out.println("dateBeforeInMS=" + dateBeforeInMs + " dateAfterInMs=" + dateAfterInMs + " timeDiff=" + timeDiff + " daysDiff=" + daysDiff);

        }catch(Exception e){
            e.printStackTrace();
            Notification.show("ERROR : al calcular los dias : " + e.getMessage(), Notification.Type.ERROR_MESSAGE);
            return;
        }

        String queryString = "SELECT IdProveedor, Nombre, Cargo, EsJefe";
        queryString += " FROM proveedor";
        queryString += " WHERE EsPlanilla = 1";
        queryString += " AND INHABILITADO = 0";
        queryString += " AND Cargo IN ('RH1', 'RH2')";
        queryString += " AND EstatusTrabajo NOT IN ('AUSENTE', 'DE BAJA') ";
        queryString += " AND IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
        queryString += " ORDER BY Nombre";

        try {

            stQuery  = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery1 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { // POR CADA EMPLEADO RH1 Y RH2

                Object itemId = 0;

                do {

                    Instant esteDia = new java.util.Date().toInstant();

                    for(int dia = 1; dia <= (daysDiff+1); dia++) {  //POR CADA DIA PARA ESTE EMPLEADO...
//System.out.println("dia=" + dia + " fecha=" + Utileria.getFechaYYYYMMDD_1(java.util.Date.from(esteDia)) );
                        queryString = "SELECT plan_trabajo_idex_rh.Id ";
                        queryString += " FROM plan_trabajo_idex_rh";
                        queryString += " INNER JOIN plan_trabajo_idex ON plan_trabajo_idex.Id = plan_trabajo_idex_rh.idPlanTrabajoIdex";
                        queryString += " WHERE plan_trabajo_idex_rh.IdEmpleado = " + rsRecords.getString("IdProveedor");
                        queryString += " AND '" + Utileria.getFechaYYYYMMDD_1(java.util.Date.from(esteDia)) + "'";
                        queryString += "     BETWEEN DATE(plan_trabajo_idex.FechaInicioPlaneada) AND DATE(plan_trabajo_idex.FechaFinPlaneada)";

//if(rsRecords.getString("IdProveedor").equals("97369")) {
//    System.out.println("****---->97369 query RH=" + queryString);
//}
                        rsRecords1 = stQuery1.executeQuery(queryString);

                        if (rsRecords1.next() == false) { // si esta libre
                            itemId = rhContainer.addItem();

                            rhContainer.getContainerProperty(itemId, "fecha").setValue(Utileria.getFechaYYYYMMDD_1(java.util.Date.from(esteDia)));
                            rhContainer.getContainerProperty(itemId, "idEmpleado").setValue(rsRecords.getString("IdProveedor"));
                            rhContainer.getContainerProperty(itemId, "nombre").setValue(rsRecords.getString("Nombre"));
                        }
                        esteDia = esteDia.plus(1, ChronoUnit.DAYS);
                    }//end for
                } while (rsRecords.next());
            } //no hay empleados libres
        }
        catch(Exception ex1) {
            Notification.show("ERROR FATAL DEL SISTEMA", Notification.Type.ERROR_MESSAGE);
            System.out.println("ERROR AL INTENTAR BUSCAR CATALOGO DE EMPLEADOS : " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public boolean exportToExcel() {
        if (this.rhGrid.getHeightByRows() > 0) {
            TableHolder tableHolder = new DefaultTableHolder(rhGrid);
            ExcelExport excelExport = new ExcelExport(tableHolder);
            excelExport.excludeCollapsedColumns();
            excelExport.setDisplayTotals(false);
            String fileexport;
            fileexport = ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId() +  "_" + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyName().replaceAll(" ", "_").replaceAll(",", "_").replaceAll("[()]", "").replaceAll("[.]", "").replaceAll("ñ", "n").replaceAll("Ñ", "N").replaceAll("ó", "o").replaceAll("é", "") + "_EMPLEADOSLIBRES.xls";
            excelExport.setExportFileName(fileexport);
            excelExport.export();
        }
        return true;
    }

}