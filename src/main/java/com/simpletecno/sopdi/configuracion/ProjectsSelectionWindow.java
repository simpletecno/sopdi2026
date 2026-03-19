/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.configuracion;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.Item;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JAguirre
 */
@SuppressWarnings("serial")
public class ProjectsSelectionWindow extends Window {
    
    public Statement stQuery = null;
    public Statement stQuery1 = null;
    public ResultSet rsRecords = null;
    public ResultSet rsRecords1 = null;
    
    static final String LOGO_PROPERTY = "Logo";
    static final String NOMBRE_PROPERTY = "Nombre";
    static final String ELEGIR_PROPERTY = "Elegir";

    VerticalLayout selectionPanel = new VerticalLayout();
    
    Table projectsTable;
    
    Button saveBtn;
    Button exitBtn;
                        
    UI mainUI;
       
    String idUsuario;
    
    public ProjectsSelectionWindow(String idUsuario) {

        mainUI = UI.getCurrent();
        this.idUsuario = idUsuario;
        setResponsive(true);
        setCaption("Relacionar proyectos y usuario");
    
        Component selectionForm = buildSelectionForm();
                
        setContent(selectionForm);
        center();
        
        setWidth("70%");
        setHeight("50%");
        
    }

    private Component buildSelectionForm() {
            
        selectionPanel.setSizeUndefined();
        selectionPanel.setSpacing(true);
        selectionPanel.setImmediate(true);
        selectionPanel.setWidth("100%");

        selectionPanel.addComponent(createTable());

        saveBtn = new Button("Salvar");
        saveBtn.setIcon(FontAwesome.SAVE); 
        saveBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        saveBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick ( Button.ClickEvent event )
            {
                save();
            }
        });

    
        exitBtn    = new Button("Salir");
        exitBtn.setIcon(FontAwesome.SIGN_OUT);
        exitBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        exitBtn.addStyleName(ValoTheme.BUTTON_SMALL);
        exitBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick ( Button.ClickEvent event )
            {
                close();
            }
        });
        
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.addComponent(saveBtn);
        buttonsLayout.addComponent(exitBtn);
        
        selectionPanel.addComponent(buttonsLayout);
        selectionPanel.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);
        
        fillProjectsTable();

        return selectionPanel;
    }

    private Component createTable() {

        HorizontalLayout reportLayout = new HorizontalLayout();
        reportLayout.setWidth("100%");
        reportLayout.addStyleName("rcorners4");
        
        projectsTable = new Table("Seleccione los proyectos para el usuario");
        projectsTable.addStyleName(ValoTheme.TABLE_SMALL);
        projectsTable.addStyleName(ValoTheme.TABLE_COMPACT);
        projectsTable.addStyleName(ValoTheme.TABLE_NO_HORIZONTAL_LINES);
        projectsTable.setImmediate(true);
        projectsTable.setSelectable(true);

        reportLayout.addComponent(projectsTable);
        reportLayout.setComponentAlignment(projectsTable, Alignment.MIDDLE_CENTER);

        projectsTable.setWidth("100%");
        projectsTable.setPageLength(5);
        
//        projectsTable.addContainerProperty(CODIGO_PROPERTY,    String.class, null);
        projectsTable.addContainerProperty(LOGO_PROPERTY,      Image.class, null);
        projectsTable.addContainerProperty(NOMBRE_PROPERTY,    String.class, null);
        projectsTable.addContainerProperty(ELEGIR_PROPERTY,    CheckBox.class, null);

        projectsTable.setColumnAlignments(new Table.Align[] { 
               /* Table.Align.CENTER,*/ Table.Align.CENTER, Table.Align.LEFT, Table.Align.CENTER
        });
        
        return reportLayout;
    }

    public void fillProjectsTable() {
        
        projectsTable.removeAllItems();
                
        String queryString = "";
        
        queryString =  "SELECT * ";
        queryString += " FROM  proyecto ";
        queryString += " WHERE IdProyecto > 0"; //solo para tener los And's
        if(((SopdiUI) mainUI).sessionInformation.getStrUserProfile().compareTo("DESARROLLADOR") == 0) {
            queryString += " AND IdProyecto In (Select IdProyecto From proyecto_usuario where IdUsuario = " + ((SopdiUI)mainUI).sessionInformation.getStrUserId() + ")";
        }

        try {
            
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();            
            stQuery1 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();            
            rsRecords = stQuery.executeQuery (queryString);

            if(rsRecords.next()) { //  encontrado
                
                Image proyectoLogo;
                
                do {
                    final byte docBytes[] = rsRecords.getBytes("Logo");
                    StreamResource logoStreamResource = null;

                    if(docBytes != null ) {
                        logoStreamResource = new StreamResource(
                            new StreamResource.StreamSource() {
                                @Override
                                public InputStream getStream() {
                                    return new ByteArrayInputStream(docBytes);
                                }
                            },rsRecords.getString("IdProyecto")
                        );
                    }

                    proyectoLogo = new Image(null, logoStreamResource);
                    proyectoLogo.setImmediate(true);
                    proyectoLogo.setWidth("45px");
                    proyectoLogo.setHeight("45px"); 
                      
                    CheckBox checkBox = new CheckBox("");
                    checkBox.setData(rsRecords.getString("IdProyecto"));
                    checkBox.setImmediate(true);
                    
                    queryString =  "SELECT IdUsuario ";
                    queryString += " FROM  proyecto_usuario ";
                    queryString += " WHERE IdUsuario = "  + idUsuario;
                    queryString += " AND   IdProyecto = " + rsRecords.getString("IdProyecto");
                     
                    rsRecords1 = stQuery1.executeQuery (queryString);

                    if(rsRecords1.next()) { //  encontrado
                        checkBox.setValue(true);
                    }
                    
                    projectsTable.addItem(new Object[] {    
//                        rsRecords.getString("IdProyecto"),
                        proyectoLogo,
                        rsRecords.getString("Nombre"),
                        checkBox
                    }, rsRecords.getInt("IdProyecto"));

                }while(rsRecords.next());

            }
        } 
        catch (Exception ex) {
            Logger.getLogger(ProjectsSelectionWindow.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de proyectos : " + ex.getMessage());
            Notification.show("Error al intentar leer registros de proyectos..!", Notification.Type.ERROR_MESSAGE);
        }

    } 
   
    private void save() {
        

        String queryString = "";
        
        queryString =  "DELETE FROM proyecto_usuario ";
        queryString += " WHERE IdUsuario = " + idUsuario;

System.out.println("\n\nQuery="+queryString);

        try {
            
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();            
            stQuery.executeUpdate(queryString);
            
            for (Iterator i = projectsTable.getItemIds().iterator(); i.hasNext();) {
                // Get the current item identifier, which is an integer.
                int iid = (Integer) i.next();
    
                // Now get the actual item from the table.
                Item item = projectsTable.getItem(iid);
    
                // And now we can get to the actual checkbox object.
                CheckBox checkBox = (CheckBox)
                    (item.getItemProperty(ELEGIR_PROPERTY).getValue());
            
                if(checkBox.getValue()) {

                    queryString = "INSERT INTO proyecto_usuario (IdProyecto, IdUsuario, Rol, Estatus) Values (";
                    queryString += " " + String.valueOf(checkBox.getData());
                    queryString += "," + idUsuario;
                    queryString += ",'ASESOR'";
                    queryString += ",'ACTIVO'";
                    queryString += ")";

//System.out.println("\n\nQuery="+queryString);

                    stQuery.executeUpdate(queryString);
                } //end if
            } //end for
            Notification.show("OPERACION RELIZADA CON EXITO!");
        }
        catch (Exception ex) {
            Logger.getLogger(ProjectsSelectionWindow.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar actualizar proyecto_usuario : " + ex.getMessage());
            Notification.show("Error al intentar actualizar proyecto_usuario..!", Notification.Type.ERROR_MESSAGE);
        }
    }
    
}