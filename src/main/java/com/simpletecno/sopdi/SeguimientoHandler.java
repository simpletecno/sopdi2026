/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.simpletecno.sopdi;

import com.simpletecno.sopdi.utilerias.DescripcionWindow;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 *
 * @author joseaguirre
 */
public class SeguimientoHandler extends Window implements Button.ClickListener {

    protected static final String TRACK_PROPERTY = "Observación";
    protected static final String FECHAYHORA_PROPERTY = "Fecha y hora";
    protected static final String GESTOR_PROPERTY = "Usuario";
    
    Statement stQuery = null;
    ResultSet rsRecords = null;

    private Table trackTable;   
    private Button newTrackBtn;
    private Button exitBtn;
    
    private UI mainUI;
    
    private String recordId, nombre;
    
    public SeguimientoHandler() {
        this.mainUI = UI.getCurrent();
        
        setCaption("SOPDI - Seguimientos y Observaciones");
        setClosable(true);
        setResizable(true);
        
        setWidth("900px");
        setHeight("330px");
                        
        setModal(true);
        
        setContent(createTrackTable());        
    }
    
    public VerticalLayout createTrackTable() {

        VerticalLayout vlayout = new VerticalLayout();
        vlayout.setMargin(true);
        vlayout.setSpacing(true);

        trackTable = new Table("");

        getTrackTable().setWidth("100%");
        getTrackTable().setPageLength(7);
        getTrackTable().setStyleName(ValoTheme.TABLE_COMPACT);
        trackTable.setDescription("Doble click para ver el seguimiento en otra ventana.");
        
        getTrackTable().setImmediate(true);
        getTrackTable().setSelectable(true);
        getTrackTable().setNullSelectionAllowed(false);
        getTrackTable().setColumnCollapsingAllowed(true);
        getTrackTable().setEditable(false);

        getTrackTable().addContainerProperty(TRACK_PROPERTY,     String.class, null);
        getTrackTable().setColumnWidth(TRACK_PROPERTY, 70);
        getTrackTable().setColumnExpandRatio(TRACK_PROPERTY, 70);
        
        getTrackTable().addContainerProperty(FECHAYHORA_PROPERTY,  String.class, null);
        getTrackTable().setColumnWidth(FECHAYHORA_PROPERTY, 20);
        getTrackTable().setColumnExpandRatio(FECHAYHORA_PROPERTY, 20);

        getTrackTable().addContainerProperty(GESTOR_PROPERTY,  String.class, null);
        getTrackTable().setColumnWidth(GESTOR_PROPERTY, 10);
        getTrackTable().setColumnExpandRatio(GESTOR_PROPERTY, 10);

        getTrackTable().setColumnAlignments(
                Table.Align.LEFT,   Table.Align.LEFT,   Table.Align.LEFT
        );
        
        getTrackTable().addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
//                Notification.show("Table item selected : " + trackTable.getValue());
            }
        });
        
        getTrackTable().addListener(new ItemClickEvent.ItemClickListener() {
            public void itemClick(ItemClickEvent event) {
                if (event.isDoubleClick()) {
                    DescripcionWindow descripcionWindow = new DescripcionWindow(String.valueOf(event.getItem().getItemProperty(TRACK_PROPERTY).getValue()));
                    mainUI.addWindow(descripcionWindow);
                    descripcionWindow.center();
                }
            }
        });        
        
        newTrackBtn = new Button("Agregar"); 
        newTrackBtn.setIcon(FontAwesome.PLUS);
        newTrackBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        newTrackBtn.addClickListener(this);

        vlayout.addComponent(getTrackTable());
        
        exitBtn = new Button("Salir");
        exitBtn.setIcon(FontAwesome.SIGN_OUT);
        exitBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        exitBtn.addClickListener(this);
        
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setMargin(false);
        buttonsLayout.setSpacing(true);
        
        buttonsLayout.addComponent(newTrackBtn);
        buttonsLayout.setComponentAlignment(newTrackBtn, Alignment.BOTTOM_CENTER);
        buttonsLayout.addComponent(exitBtn);
        
        vlayout.addComponent(buttonsLayout);
        vlayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);

        return vlayout;
    }
    
    public void fillTrackTable(String recordId, String nombre) {
        try {

            this.recordId = recordId;
            this.nombre = nombre;
            
            if(getTrackTable() == null)
                return;
            
            if(newTrackBtn != null) {
                newTrackBtn.setVisible(true);
            }

            getTrackTable().removeAllItems();

            trackTable.setCaption("Seguimiento de : " + nombre);
            
            String queryString;

            queryString = "Select Seg.*, Usr.Nombre UsuarioNombre  From ";
            if(mainUI.getNavigator().getCurrentView().getClass().getSimpleName().compareTo("InspectionsTaskTrackView") == 0) { 
                queryString += " visita_inspeccion_tarea_seguimiento Seg";
            }
            if(mainUI.getNavigator().getCurrentView().getClass().getSimpleName().compareTo("ChangeOrdersView") == 0) {
                queryString += " orden_cambio_seguimiento Seg";
            }            
            if(mainUI.getNavigator().getCurrentView().getClass().getSimpleName().compareTo("TaskView") == 0) {
                queryString += " tarea_seguimiento Seg";
            }            
            queryString += " Inner Join usuario Usr On Usr.IdUsuario = Seg.IdUsuario";
            if(mainUI.getNavigator().getCurrentView().getClass().getSimpleName().compareTo("InspectionsTaskTrackView") == 0) {
                queryString += " Where Seg.IdVisitaInspeccionTarea = " + recordId;
            }
            if(mainUI.getNavigator().getCurrentView().getClass().getSimpleName().compareTo("ChangeOrdersView") == 0) { 
                queryString += " Where Seg.IdOrdenCambio = " + recordId;
            }
            if(mainUI.getNavigator().getCurrentView().getClass().getSimpleName().compareTo("TaskView") == 0) { 
                queryString += " Where Seg.IdTarea = " + recordId;
            }
            queryString += " Order By Seg.FechaYHora";

System.out.println("fillTrackTable Query... " +  queryString);        

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();            
            rsRecords = stQuery.executeQuery (queryString);
                        
            if(rsRecords.next()) { //  encontrado  
                
                int primerSeguimiento = rsRecords.getInt("IdSeguimiento");
                
                do {
                    getTrackTable().addItem(new Object[] {    
                        rsRecords.getString("Observacion"),
                        Utileria.getFechaDDMMYYYY_HHMM_2(rsRecords.getTimestamp("FechaYHora")),
                        rsRecords.getString("UsuarioNombre")
                    }, rsRecords.getInt("IdSeguimiento"));
                                
                } while(rsRecords.next());
                
                getTrackTable().select(primerSeguimiento);
            }
        }
        catch(Exception  sqle) {
            Notification.show("ERROR DE BASE DE DATOS", sqle.getMessage(), Notification.Type.ERROR_MESSAGE);
            System.out.println("ERROR:" + sqle.getMessage());
            sqle.printStackTrace();
        }
    }
   
    public void clearTableData() {
       if(getTrackTable() != null) {
            if(getTrackTable().size() > 0) {
                getTrackTable().removeAllItems();
            }
        }
    }

    /**
     * @return the trackTable
     */
    public Table getTrackTable() {
        return trackTable;
    }
    
    // The listener method implementation
    public void buttonClick(ClickEvent event) {
        if(event.getButton().equals(newTrackBtn)) {
            SeguimientoWindow seguimientoWindow = new SeguimientoWindow((SeguimientoHandler)this,
                            recordId, nombre, "", "");
            mainUI.addWindow(seguimientoWindow);
            seguimientoWindow.center();
            seguimientoWindow.seguimientoTxt.focus();
        }
        if(event.getButton().equals(exitBtn)) {
            close();
        }
        
    }    
}
