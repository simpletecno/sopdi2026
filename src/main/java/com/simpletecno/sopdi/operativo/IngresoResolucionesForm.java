/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.operativo;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author user
 */
public class IngresoResolucionesForm extends Window {

    UI mainUI;
    Statement stQuery;
    ResultSet rsRecords;
    String queryString;
    static PreparedStatement stPreparedQuery;

    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");
    static DecimalFormat numberFormat2 = new DecimalFormat("#,###,##0");

    IndexedContainer resolucionesContainer = new IndexedContainer();
    Grid resolucionesGrid;

    String visitaId;
    String codigoVisita;

    Button saveBtn;

    public IngresoResolucionesForm(
            String visitaId, 
            String codigoVisita) {

        this.visitaId = visitaId;
        this.codigoVisita = codigoVisita;

        VerticalLayout mainLayout;
        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);
        mainLayout.setResponsive(true);

        HorizontalLayout layoutTitle = new HorizontalLayout();
        layoutTitle.setSpacing(true);
        layoutTitle.setMargin(true);
        layoutTitle.setWidth("100%");

        this.mainUI = UI.getCurrent();
        
        setResponsive(true);
        setWidth("90%");
//        setHeight("50%");

        setContent(mainLayout);

        resolucionesContainer.addContainerProperty("#", String.class, "");
        resolucionesContainer.addContainerProperty("Punto de Agenda", String.class, "");
        resolucionesContainer.addContainerProperty("Resolución", String.class, "");

        resolucionesGrid = new Grid(resolucionesContainer);
        resolucionesGrid.setEditorBuffered(false);
        resolucionesGrid.setHeightByRows(7);
        resolucionesGrid.setWidthUndefined();
//        resolucionesGrid.setEditorEnabled(true);
        resolucionesGrid.addStyleName("resoluciones");
        //resolucionesGrid.setStyleName("cell");
        resolucionesGrid.setSelectionMode(SelectionMode.SINGLE);
        resolucionesGrid.setWidth("100%");
        resolucionesGrid.setDescription("DOBLE Click aqui para editar las resoluciones.");
        resolucionesGrid.getColumn("#").setMaximumWidth(50).setEditable(false);
        resolucionesGrid.getColumn("Punto de Agenda").setEditable(false).setMaximumWidth(375);
        resolucionesGrid.addItemClickListener((event) -> {
            if (event != null) {
                if(event.isDoubleClick()) {
                    if(event.getItemId() == null) {
                        return;
                    }
                    if(String.valueOf(event.getItem().getItemProperty("Punto de Agenda").getValue()).trim().isEmpty()) {
                        return;
                    }
//                    InspectionTextWindow inspectionTextWindow = 
//                            new InspectionTextWindow(
//                                    visitaId,
//                                    codigoVisita,
//                                    String.valueOf(event.getItem().getItemProperty("Punto de Agenda").getValue()),
//                                    String.valueOf(event.getItemId()),
//                                    String.valueOf(event.getItem().getItemProperty("Resolución").getValue())
//                    );
//                    mainUI.addWindow(inspectionTextWindow);
//                    inspectionTextWindow.center();
                    
                }
            }

        });

        Object itemdId2 = resolucionesContainer.addItem();
        resolucionesContainer.getContainerProperty(itemdId2, "#").setValue("1");
        resolucionesContainer.getContainerProperty(itemdId2, "Punto de Agenda").setValue("");
        resolucionesContainer.getContainerProperty(itemdId2, "Resolución").setValue("");
        itemdId2 = resolucionesContainer.addItem();
        resolucionesContainer.getContainerProperty(itemdId2, "#").setValue("2");
        resolucionesContainer.getContainerProperty(itemdId2, "Punto de Agenda").setValue("");
        resolucionesContainer.getContainerProperty(itemdId2, "Resolución").setValue("");
        itemdId2 = resolucionesContainer.addItem();
        resolucionesContainer.getContainerProperty(itemdId2, "#").setValue("3");
        resolucionesContainer.getContainerProperty(itemdId2, "Punto de Agenda").setValue("");
        resolucionesContainer.getContainerProperty(itemdId2, "Resolución").setValue("");
        itemdId2 = resolucionesContainer.addItem();
        resolucionesContainer.getContainerProperty(itemdId2, "#").setValue("4");
        resolucionesContainer.getContainerProperty(itemdId2, "Punto de Agenda").setValue("");
        resolucionesContainer.getContainerProperty(itemdId2, "Resolución").setValue("");
        itemdId2 = resolucionesContainer.addItem();
        resolucionesContainer.getContainerProperty(itemdId2, "#").setValue("5");
        resolucionesContainer.getContainerProperty(itemdId2, "Punto de Agenda").setValue("");
        resolucionesContainer.getContainerProperty(itemdId2, "Resolución").setValue("");
        itemdId2 = resolucionesContainer.addItem();
        resolucionesContainer.getContainerProperty(itemdId2, "#").setValue("6");
        resolucionesContainer.getContainerProperty(itemdId2, "Punto de Agenda").setValue("");
        resolucionesContainer.getContainerProperty(itemdId2, "Resolución").setValue("");
        itemdId2 = resolucionesContainer.addItem();
        resolucionesContainer.getContainerProperty(itemdId2, "#").setValue("7");
        resolucionesContainer.getContainerProperty(itemdId2, "Punto de Agenda").setValue("");
        resolucionesContainer.getContainerProperty(itemdId2, "Resolución").setValue("");

        saveBtn = new Button("Guardar");
        saveBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        saveBtn.setIcon(FontAwesome.SAVE);
        saveBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                validarYGuardar();
            }
        });

        mainLayout.addComponent(resolucionesGrid);
        mainLayout.setComponentAlignment(resolucionesGrid, Alignment.BOTTOM_CENTER);
//        mainLayout.addComponent(saveBtn);
//        mainLayout.setComponentAlignment(saveBtn, Alignment.MIDDLE_CENTER);

    }

    public void llenarTabla() {

        setCaption("Resoluciones");

        String queryString;

        queryString = "Select *";
        queryString += " From visita_inspeccion ";
        queryString += " Where IdVisitaInspeccion = " + visitaId;

//System.out.println("\n\n"+queryString);
        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado

                do {
                    System.out.println(rsRecords.getString("PuntoAgenda1"));

                    resolucionesContainer.getContainerProperty(1, "Punto de Agenda").setValue(rsRecords.getString("PuntoAgenda1"));
                    resolucionesContainer.getContainerProperty(2, "Punto de Agenda").setValue(rsRecords.getString("PuntoAgenda2"));
                    resolucionesContainer.getContainerProperty(3, "Punto de Agenda").setValue(rsRecords.getString("PuntoAgenda3"));
                    resolucionesContainer.getContainerProperty(4, "Punto de Agenda").setValue(rsRecords.getString("PuntoAgenda4"));
                    resolucionesContainer.getContainerProperty(5, "Punto de Agenda").setValue(rsRecords.getString("PuntoAgenda5"));
                    resolucionesContainer.getContainerProperty(6, "Punto de Agenda").setValue(rsRecords.getString("PuntoAgenda6"));
                    resolucionesContainer.getContainerProperty(7, "Punto de Agenda").setValue(rsRecords.getString("PuntoAgenda7"));
                    
                    resolucionesContainer.getContainerProperty(1, "Resolución").setValue(rsRecords.getString("Resolucion1"));
                    resolucionesContainer.getContainerProperty(2, "Resolución").setValue(rsRecords.getString("Resolucion2"));
                    resolucionesContainer.getContainerProperty(3, "Resolución").setValue(rsRecords.getString("Resolucion3"));
                    resolucionesContainer.getContainerProperty(4, "Resolución").setValue(rsRecords.getString("Resolucion4"));
                    resolucionesContainer.getContainerProperty(5, "Resolución").setValue(rsRecords.getString("Resolucion5"));
                    resolucionesContainer.getContainerProperty(6, "Resolución").setValue(rsRecords.getString("Resolucion6"));
                    resolucionesContainer.getContainerProperty(7, "Resolución").setValue(rsRecords.getString("Resolucion7"));

                } while (rsRecords.next());

            }
        } catch (Exception ex) {

        }
    }

    public void validarYGuardar() {
        try {
            queryString = " Update visita_inspeccion Set";
            queryString += " Resolucion1 = '" + resolucionesContainer.getContainerProperty(1, "Resolución").getValue() + "'";
            queryString += ",Resolucion2 = '" + resolucionesContainer.getContainerProperty(2, "Resolución").getValue() + "'";
            queryString += ",Resolucion3 = '" + resolucionesContainer.getContainerProperty(3, "Resolución").getValue() + "'";
            queryString += ",Resolucion4 = '" + resolucionesContainer.getContainerProperty(4, "Resolución").getValue() + "'";
            queryString += ",Resolucion5 = '" + resolucionesContainer.getContainerProperty(5, "Resolución").getValue() + "'";
            queryString += ",Resolucion6 = '" + resolucionesContainer.getContainerProperty(6, "Resolución").getValue() + "'";
            queryString += ",Resolucion7 = '" + resolucionesContainer.getContainerProperty(7, "Resolución").getValue() + "'";
            queryString += " Where IdVisitaInspeccion = " +codigoVisita;
            
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);
            
            Notification.show("Registro modificado!!!", Notification.Type.HUMANIZED_MESSAGE);
            
        } catch (SQLException ex) {
            Logger.getLogger(IngresoResolucionesForm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
