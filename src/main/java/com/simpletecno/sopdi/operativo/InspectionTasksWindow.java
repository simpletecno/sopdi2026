/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.operativo;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Responsive;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Field;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JAguirre
 */
@SuppressWarnings("serial")
public class InspectionTasksWindow extends Window {

    private Statement stQuery3 = null;
    private ResultSet rsRecords3 = null;
    private Statement stQuery2 = null;
    private ResultSet rsRecords2 = null;
    private Statement stQuery = null;
    private ResultSet rsRecords = null;
    private PreparedStatement stPreparedQuery = null;

    VerticalLayout mainLayout = new VerticalLayout();

    IndexedContainer tareasContainer = new IndexedContainer();
    Grid tareasGrid;

    String queryString = "";

    UI mainUI;
    Button cargarBtn;

    String visitaInspeccionId;
    String codigoVisita;
    String descripcionVisita;
    String idCentroCostoDefault;
    String cliente;

    String codigoTarea;

    MarginInfo marginInfo = new MarginInfo(false, false, false, false);
    
    public InspectionTasksWindow(
            String visitaInspeccionId,
            String codigoVisita,
            String descripcionVisita,
            String idCentroCostoDefault,
            String cliente) {
        this.visitaInspeccionId = visitaInspeccionId;
        this.codigoVisita = codigoVisita;
        this.descripcionVisita = descripcionVisita;
        this.idCentroCostoDefault = idCentroCostoDefault;
        if (cliente == null) {
            cliente = "";
        }
        this.cliente = cliente;

        this.mainUI = UI.getCurrent();

        mainLayout.setSpacing(false);
        mainLayout.setMargin(false);
        mainLayout.setWidth("98%");
        mainLayout.addStyleName("rcorners3");

        Responsive.makeResponsive(this);

        setContent(mainLayout);

        Label titleLbl = new Label("Tareas de visita o reunión : " + codigoVisita + " " + descripcionVisita + " " + cliente);
        titleLbl.addStyleName(ValoTheme.LABEL_H3);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h3_custom");

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setWidth("100%");

        titleLayout.addComponents(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.TOP_LEFT);

        mainLayout.addComponent(titleLayout);
        mainLayout.setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        createEditionLayout();

        fillComboRubro();
        fillInspectionTaskGrid();

        Button exitBtn = new Button("Salir");
        exitBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        exitBtn.setIcon(FontAwesome.ARROW_RIGHT);
        exitBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();
            }
        });

        Button saveBtn = new Button("Guardar");
        saveBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        saveBtn.setIcon(FontAwesome.SAVE);
        saveBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                guardar();
            }
        });

        cargarBtn = new Button("Subir fotografía");
        cargarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        cargarBtn.setIcon(FontAwesome.FILE_IMAGE_O);
        cargarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (tareasGrid.getEditedItemId() != null) {
//System.out.println("getEditedItemId() = " + tareasGrid.getEditedItemId());
                    if (String.valueOf(tareasContainer.getContainerProperty(tareasGrid.getEditedItemId(), "Descripción").getValue()).trim().isEmpty()) {
                        Notification.show("Por favor guarde cambios antes de subir una imagen o fotografia", Notification.Type.WARNING_MESSAGE);
                        return;
                    }
                    InspectionTaskImageWindow inspectionTaskImageWindow
                            = new InspectionTaskImageWindow(
                                    String.valueOf(tareasContainer.getContainerProperty(tareasGrid.getEditedItemId(), "CTarea").getValue()),
                                    codigoVisita + String.valueOf(tareasContainer.getContainerProperty(tareasGrid.getEditedItemId(), "CTarea").getValue()),
                                    String.valueOf(tareasContainer.getContainerProperty(tareasGrid.getEditedItemId(), "Descripción").getValue()),
                                    true
                            );
                    UI.getCurrent().addWindow(inspectionTaskImageWindow);
                    inspectionTaskImageWindow.center();
                }
                else {
                    Notification.show("POR FAVOR SELECCIONE UN REGISTRO.", Notification.Type.HUMANIZED_MESSAGE);
                }
            }
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSizeFull();
        buttonsLayout.setMargin(true);
        buttonsLayout.setSpacing(false);

        buttonsLayout.addComponent(exitBtn);
        buttonsLayout.setComponentAlignment(exitBtn, Alignment.BOTTOM_LEFT);
        buttonsLayout.addComponent(saveBtn);
        buttonsLayout.setComponentAlignment(saveBtn, Alignment.BOTTOM_CENTER);
        buttonsLayout.addComponent(cargarBtn);
        buttonsLayout.setComponentAlignment(cargarBtn, Alignment.BOTTOM_RIGHT);

        mainLayout.addComponent(buttonsLayout);
        mainLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);

        setWidth("98%");
        setHeight("98%");

    }

    private void createEditionLayout() {

        tareasContainer.addContainerProperty("#", String.class, "");
        tareasContainer.addContainerProperty("Rubro", String.class, "");
        tareasContainer.addContainerProperty("Descripción", String.class, "");
        tareasContainer.addContainerProperty("Responsable", String.class, "");
        tareasContainer.addContainerProperty("CentroCosto", String.class, "");
        tareasContainer.addContainerProperty("Garantia", String.class, "");
        tareasContainer.addContainerProperty("Es Tarea", String.class, "");
        tareasContainer.addContainerProperty("Presupuesto", String.class, "");
        tareasContainer.addContainerProperty("Autorizar", String.class, "");
        tareasContainer.addContainerProperty("CTarea", String.class, "");
        tareasContainer.addContainerProperty("Fotografía", String.class, "");

        tareasGrid = new Grid("Listado de Tareas", tareasContainer);
        tareasGrid.setEditorBuffered(false);
        tareasGrid.setHeightMode(HeightMode.ROW);
        tareasGrid.setHeightByRows(10);
        tareasGrid.setEditorEnabled(true);
        tareasGrid.setSelectionMode(Grid.SelectionMode.NONE);
        tareasGrid.setWidth("100%");
        tareasGrid.getColumn("Rubro").setEditorField(getComboRubro());
        tareasGrid.getColumn("CentroCosto").setEditorField(getComboCCosto());
        tareasGrid.getColumn("Autorizar").setEditorField(getComboAutorizador());
        tareasGrid.getColumn("Garantia").setEditorField(getComboGarantia());
        tareasGrid.getColumn("Es Tarea").setEditorField(getComboTarea());
        tareasGrid.getColumn("Presupuesto").setEditorField(getComboPresupuesto());
        tareasGrid.getColumn("CTarea").setHidden(true);
        tareasGrid.getColumn("Fotografía").setEditable(false);
        tareasGrid.getColumn("CTarea").getEditorField().addValueChangeListener((event) -> {
            if(tareasContainer.getContainerProperty(tareasGrid.getEditedItemId(), "Fotografía").getValue()
                    .equals("NO")){
                  cargarBtn.setCaption("Subir fotografía");
            }else{
                  cargarBtn.setCaption("Ver/cambiar fotografía");
            }
            
        });
        
       tareasGrid.addItemClickListener((event) -> {
            if (event != null) {
                tareasGrid.editItem(event.getItemId());
            }
        });        

       for(int iTask = 1; iTask <= 10; iTask++) {
            Object itemId = tareasContainer.addItem();

            tareasContainer.getContainerProperty(itemId, "#").setValue(String.format("%02d", iTask));
            tareasContainer.getContainerProperty(itemId, "Rubro").setValue("");
            tareasContainer.getContainerProperty(itemId, "Descripción").setValue("");
            tareasContainer.getContainerProperty(itemId, "Responsable").setValue("");
            tareasContainer.getContainerProperty(itemId, "CentroCosto").setValue("");
            tareasContainer.getContainerProperty(itemId, "Garantia").setValue("");
            tareasContainer.getContainerProperty(itemId, "Es Tarea").setValue("");
            tareasContainer.getContainerProperty(itemId, "Presupuesto").setValue("");
            tareasContainer.getContainerProperty(itemId, "Autorizar").setValue("");
            tareasContainer.getContainerProperty(itemId, "CTarea").setValue("");
            tareasContainer.getContainerProperty(itemId, "Fotografía").setValue("");
        }

        mainLayout.addComponent(tareasGrid);
    }

    private Field<?> getComboRubro() {

        ComboBox comboBox = new ComboBox();
        comboBox.setImmediate(true);
        comboBox.setNullSelectionAllowed(false);
        //comboBox.setNewItemsAllowed(true);
        comboBox.clear();

        queryString = "SELECT * ";
        queryString += " FROM visita_inspeccion ";
        queryString += " WHERE IdVisitaInspeccion = " + visitaInspeccionId;

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            comboBox.removeAllItems();

            if(rsRecords.next()) { //  encontrado                
                comboBox.addItem(rsRecords.getString("PuntoAgenda1"));
                comboBox.addItem(rsRecords.getString("PuntoAgenda2"));
                comboBox.addItem(rsRecords.getString("PuntoAgenda3"));
                comboBox.addItem(rsRecords.getString("PuntoAgenda4"));
                comboBox.addItem(rsRecords.getString("PuntoAgenda5"));
                comboBox.addItem(rsRecords.getString("PuntoAgenda6"));
                comboBox.addItem(rsRecords.getString("PuntoAgenda7"));
            }
        } catch (Exception ex1) {
            Notification.show("ERROR DEL SISTEMA AL BUSCAR CENTROS DE COSTO", Notification.Type.ERROR_MESSAGE);
            System.out.println("ERROR AL INTENTAR BUSCAR CATALOGO DE CENTROS DE COSTO : " + ex1.getMessage());
            ex1.printStackTrace();
        }

        return comboBox;
    }

    private Field<?> getComboCCosto() {

        ComboBox comboBox = new ComboBox();
        comboBox.setImmediate(true);
        comboBox.setNullSelectionAllowed(false);
        comboBox.clear();
        comboBox.removeAllItems();

        comboBox.addItem("");

        queryString = "SELECT * ";
        queryString += " FROM centro_costo ";
        queryString += " WHERE IdProyecto = " + ((SopdiUI) mainUI).sessionInformation.getStrProjectId();
        queryString += " AND IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            comboBox.removeAllItems();

            while (rsRecords.next()) { //  encontrado                
                comboBox.addItem(rsRecords.getString("CodigoCentroCosto"));
            }
            if (idCentroCostoDefault.equals("0") == false) {
               // comboBox.select(Integer.valueOf(idCentroCostoDefault));
                comboBox.setEnabled(false);
            }
        } catch (Exception ex1) {
            Notification.show("ERROR DEL SISTEMA AL BUSCAR CENTROS DE COSTO", Notification.Type.ERROR_MESSAGE);
            System.out.println("ERROR AL INTENTAR BUSCAR CATALOGO DE CENTROS DE COSTO : " + ex1.getMessage());
            ex1.printStackTrace();
        }

        return comboBox;
    }

    private Field<?> getComboAutorizador() {

        ComboBox comboBox = new ComboBox();
        comboBox.setImmediate(true);
        comboBox.setNullSelectionAllowed(false);
        comboBox.clear();
        comboBox.removeAllItems();

        comboBox.addItem("");
        comboBox.addItem("CLIENTE");
        comboBox.addItem("GERENCIA");
        comboBox.addItem("ADMINISTRADOR");
        comboBox.addItem("COMITE TECNICO");
        comboBox.addItem("JUNTA DIRECTIVA");
        comboBox.addItem("N/A");
        comboBox.select("CLIENTE");

        comboBox.addValueChangeListener((event) -> {
            try {
                tareasGrid.saveEditor();
            } catch (FieldGroup.CommitException ex) {
                ex.printStackTrace();
                System.out.println("Error en el value change" + ex);
            }
        });

        return comboBox;
    }

    private Field<?> getComboGarantia() {

        ComboBox comboBox = new ComboBox();
        comboBox.setImmediate(true);
        comboBox.setNullSelectionAllowed(false);
        comboBox.clear();
        comboBox.removeAllItems();

        comboBox.addItem("");
        comboBox.addItem("SI");
        comboBox.addItem("NO");
        comboBox.select("SI");

        return comboBox;
    }

    private Field<?> getComboTarea() {
        ComboBox comboBox = new ComboBox();
        comboBox.setImmediate(true);
        comboBox.setNullSelectionAllowed(false);
        comboBox.clear();
        comboBox.removeAllItems();

        comboBox.addItem("");
        comboBox.addItem("SI");
        comboBox.addItem("NO");
        comboBox.select("SI");

        return comboBox;
    }

    private Field<?> getComboPresupuesto() {

        ComboBox comboBox = new ComboBox();
        comboBox.setImmediate(true);
        comboBox.setNullSelectionAllowed(false);
        comboBox.clear();
        comboBox.removeAllItems();

        comboBox.addItem("");
        comboBox.addItem("SI");
        comboBox.addItem("NO");
        comboBox.select("SI");

        return comboBox;
    }

    private void fillComboRubro() {

        queryString = "SELECT * ";
        queryString += " FROM visita_inspeccion ";
        queryString += " WHERE IdVisitaInspeccion = " + visitaInspeccionId;

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if(rsRecords.next()) {
                tareasContainer.getContainerProperty(1, "Rubro").setValue(rsRecords.getString("PuntoAgenda1"));
                tareasContainer.getContainerProperty(2, "Rubro").setValue(rsRecords.getString("PuntoAgenda2"));
                tareasContainer.getContainerProperty(3, "Rubro").setValue(rsRecords.getString("PuntoAgenda3"));
                tareasContainer.getContainerProperty(4, "Rubro").setValue(rsRecords.getString("PuntoAgenda4"));
                tareasContainer.getContainerProperty(5, "Rubro").setValue(rsRecords.getString("PuntoAgenda5"));
                tareasContainer.getContainerProperty(6, "Rubro").setValue(rsRecords.getString("PuntoAgenda6"));
                tareasContainer.getContainerProperty(7, "Rubro").setValue(rsRecords.getString("PuntoAgenda7"));
            }

        } catch (Exception ex1) {
            Notification.show("ERROR DEL SISTEMA AL BUSCAR CENTROS DE COSTO", Notification.Type.ERROR_MESSAGE);
            System.out.println("ERROR AL INTENTAR BUSCAR CATALOGO DE CENTROS DE COSTO : " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void fillInspectionTaskGrid() {

        this.setCaption("Tareas de visita o reunion");
        
        queryString = "SELECT * ";
        queryString += " FROM visita_inspeccion_tarea";
        queryString += " WHERE IdVisitaInspeccion = " + visitaInspeccionId;

        try {

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado

                int itemId = 1;
                
                do {
                    
                    queryString = "SELECT * ";
                    queryString += "FROM visita_inspeccion_tarea ";
                    queryString += "INNER JOIN visita_inspeccion_tarea_imagen ";
                    queryString += "ON visita_inspeccion_tarea_imagen.IdVisitaInspeccionTarea ";
                    queryString += "= visita_inspeccion_tarea.IdVisitaInspeccionTarea";
                    queryString += " WHERE visita_inspeccion_tarea.IdVisitaInspeccionTarea = " + rsRecords.getString("IdVisitaInspeccionTarea");

                    stQuery3 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                    rsRecords3 = stQuery3.executeQuery(queryString);
                    if (rsRecords3.next()) {
                        tareasContainer.getContainerProperty(itemId, "Fotografía").setValue("SI");
                    } else {
                        tareasContainer.getContainerProperty(itemId, "Fotografía").setValue("NO");
                    }

                    tareasContainer.getContainerProperty(itemId, "CTarea").setValue(rsRecords.getString("IdVisitaInspeccionTarea"));
                    tareasContainer.getContainerProperty(itemId, "#").setValue(rsRecords.getString("CodigoTarea").substring(12, 14));
                    tareasContainer.getContainerProperty(itemId, "Rubro").setValue(rsRecords.getString("Referencia"));
                    tareasContainer.getContainerProperty(itemId, "Descripción").setValue(rsRecords.getString("Instruccion"));
                    tareasContainer.getContainerProperty(itemId, "Responsable").setValue(rsRecords.getString("Responsable"));
                    tareasContainer.getContainerProperty(itemId, "CentroCosto").setValue(rsRecords.getString("IdCentroCosto"));
                    tareasContainer.getContainerProperty(itemId, "Garantia").setValue(rsRecords.getString("Garantia"));
                    tareasContainer.getContainerProperty(itemId, "Es Tarea").setValue(rsRecords.getString("EsTarea"));
                    tareasContainer.getContainerProperty(itemId, "Presupuesto").setValue(rsRecords.getString("Presupuesto"));
                    tareasContainer.getContainerProperty(itemId, "Autorizar").setValue(rsRecords.getString("AutorizadoTipo"));
                    
                    itemId++;
                    
                }while(rsRecords.next());    
            }
        } catch (Exception ex) {
            Logger.getLogger(InspectionTasksWindow.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al leer registros de tareas visita  : " + ex.getMessage());
            Notification.show("Error al leer registros de tareas visitas..!", Notification.Type.ERROR_MESSAGE);
        }
    }

    public void guardar() {

        boolean nuevo;
        
        try {
            
            for(Object itemObject : tareasContainer.getItemIds()) {
            
                if (tareasContainer.getContainerProperty(itemObject, "Descripción").getValue() != "") {

                    if (tareasContainer.getContainerProperty(itemObject, "CTarea").getValue().equals("")) {//nuevo
                        queryString = "SELECT CodigoTarea";
                        queryString += " FROM  visita_inspeccion_tarea ";
                        queryString += " WHERE IdVisitaInspeccion = " + visitaInspeccionId;
                        queryString += " ORDER BY CodigoTarea DESC";
                        queryString += " LIMIT 1";

                        stQuery2 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                        rsRecords2 = stQuery2.executeQuery(queryString);

                        if (rsRecords2.next()) { // encontrado la ultima tarea...
                            codigoTarea = codigoVisita.substring(0, 12) + String.format("%02d", Integer.valueOf(rsRecords2.getString("CodigoTarea").substring(12, 14)) + 1);
                        } else {
                            codigoTarea = codigoVisita + String.format("%02d", Integer.valueOf(String.valueOf(itemObject)));
                        }
                        queryString = "INSERT INTO visita_inspeccion_tarea (IdVisitaInspeccion, CodigoTarea, ";
                        queryString += " IdCentroCosto, Instruccion, Referencia, Responsable, Garantia,EsTarea, ";
                        queryString += " Presupuesto, AutorizadoTipo) ";
                        queryString += " VALUES (";
                        queryString += "  " + visitaInspeccionId;
                        queryString += ",'" + codigoTarea + "'";
                        if(tareasContainer.getContainerProperty(itemObject, "CentroCosto").getValue().equals("")) {
                            queryString += ",'0'";
                        }
                        else {
                            queryString += ",'" + tareasContainer.getContainerProperty(itemObject, "CentroCosto").getValue() + "'";
                        }
                        queryString += ",'" + tareasContainer.getContainerProperty(itemObject, "Descripción").getValue() + "'";
                        queryString += ",'" + tareasContainer.getContainerProperty(itemObject, "Rubro").getValue() + "'";
                        queryString += ",'" + tareasContainer.getContainerProperty(itemObject, "Responsable").getValue() + "'";
                        queryString += ",'" + tareasContainer.getContainerProperty(itemObject, "Garantia").getValue() + "'";
                        queryString += ",'" + tareasContainer.getContainerProperty(itemObject, "Es Tarea").getValue() + "'";
                        queryString += ",'" + tareasContainer.getContainerProperty(itemObject, "Presupuesto").getValue() + "'";
                        queryString += ",'" + tareasContainer.getContainerProperty(itemObject, "Autorizar").getValue() + "'";
                        queryString += ")";
                        nuevo = true;

                    } else {
                        nuevo = false;
                        queryString = "UPDATE visita_inspeccion_tarea SET ";
                        if(tareasContainer.getContainerProperty(itemObject, "CentroCosto").getValue().equals("")) {
                            queryString += " IdCentroCosto = '0'";
                        }
                        else {
                            queryString += " IdCentroCosto = '" + tareasContainer.getContainerProperty(itemObject, "CentroCosto").getValue() + "'";
                        }
                        queryString += ",Instruccion = '" + tareasContainer.getContainerProperty(itemObject, "Descripción").getValue() + "'";
                        queryString += ",Referencia = '" + tareasContainer.getContainerProperty(itemObject, "Rubro").getValue() + "'";
                        queryString += ",Responsable = '" + tareasContainer.getContainerProperty(itemObject, "Responsable").getValue() + "'";
                        queryString += ",Garantia = '" + tareasContainer.getContainerProperty(itemObject, "Garantia").getValue() + "'";
                        queryString += ",EsTarea = '" + tareasContainer.getContainerProperty(itemObject, "Es Tarea").getValue() + "'";
                        queryString += ",Presupuesto = '" + tareasContainer.getContainerProperty(itemObject, "Presupuesto").getValue() + "'";
                        queryString += ",AutorizadoTipo = '" + tareasContainer.getContainerProperty(itemObject, "Autorizar").getValue() + "'";
                        queryString += " WHERE IdVisitaInspeccionTarea  = " + tareasContainer.getContainerProperty(itemObject, "CTarea").getValue();
                    }

                    if (nuevo) {
                        stPreparedQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().prepareStatement(queryString, Statement.RETURN_GENERATED_KEYS);
                        stPreparedQuery.executeUpdate();
                        rsRecords2 = stPreparedQuery.getGeneratedKeys();

                        rsRecords2.next();

                        tareasContainer.getContainerProperty(itemObject, "CentroCosto").setValue(rsRecords2.getString(1));
                        
                    } else {
                        stQuery2 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                        stQuery2.executeUpdate(queryString);
                    }
                    
                }
                
            } //end for
            fillInspectionTaskGrid();
            Notification.show("OPERACION EXITOSA!", Notification.Type.HUMANIZED_MESSAGE);

        } catch (SQLException ex) {
            Logger.getLogger(InspectionTasksWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
