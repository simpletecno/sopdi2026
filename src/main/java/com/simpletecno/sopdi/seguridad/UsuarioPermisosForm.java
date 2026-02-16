/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.seguridad;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 *
 * @author joseaguirre
 */
public class UsuarioPermisosForm extends Window {
    VerticalLayout mainLayout;
    MarginInfo  marginInfo;

    private int idUsuario = 0;

    Button saveBtn;

    Statement stQuery = null;
    static PreparedStatement stPreparedQuery;
    ResultSet rsRecords = null;

    IndexedContainer permisosContainer = new IndexedContainer();
    Grid permisosGrid = new Grid("", permisosContainer);

    UI mainUI;

    public UsuarioPermisosForm(
            int idUsuario,
            String usuarioNombre
    ) {
        this.idUsuario = idUsuario;
        this.mainUI = UI.getCurrent();

        setResponsive(true);
        setCaption("Permisos de Usuario  : " + idUsuario + " " + usuarioNombre);
        setWidth("50%");
        setHeight("75%");
        center();
        
        marginInfo = new MarginInfo(true,true,false,true);
        
        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setImmediate(true);
        mainLayout.setResponsive(true);

        createGridIdex();

        saveBtn = new Button("Guardar");
        saveBtn.setIcon(FontAwesome.SAVE); 
        saveBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        saveBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick ( Button.ClickEvent event )
            {
                saveGroup();
            }
        });                

        
        mainLayout.addComponent(saveBtn);
        mainLayout.setComponentAlignment(saveBtn, Alignment.BOTTOM_CENTER);
        
        setContent(mainLayout);
    }
    
    private void createGridIdex() {

        permisosContainer.addContainerProperty("ID", String.class, "");
        permisosContainer.addContainerProperty("OPCION", String.class, "");

        permisosGrid.setWidth("90%");
        permisosGrid.setImmediate(true);
        permisosGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        permisosGrid.setDescription("Seleccione uno o varios permisos.");
        permisosGrid.setHeight("100%");
        permisosGrid.setResponsive(true);
        
        llenarGridPermisos();

        mainLayout.addComponent(permisosGrid);
        mainLayout.setComponentAlignment(permisosGrid, Alignment.TOP_CENTER);

    }

    void llenarGridPermisos() {

        if(permisosGrid == null) {
            return;
        }

        String queryString = " SELECT * ";
        queryString += " FROM opcion_menu OM ";
        queryString += " LEFT OUTER JOIN usuario_permisos UP ON UP.IdOpcionMenu = OM.IdOpcionMenu AND UP.IdUsuario= " + idUsuario;
        queryString += " WHERE OM.Estatus = 'ACTIVO'";
        queryString += " ORDER BY OM.Orden";

        permisosContainer.removeAllItems();

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords = stQuery.executeQuery (queryString);

            while(rsRecords.next()) { //  encontrado
                Object itemId = permisosContainer.addItem();
                permisosContainer.getContainerProperty(itemId, "ID").setValue(rsRecords.getString("IdOpcionMenu"));
                if(rsRecords.getString("Clase") == null){
                    permisosContainer.getContainerProperty(itemId, "OPCION").setValue(">> " + rsRecords.getString("Descripcion") + " <<");
                }else if(rsRecords.getString("Clase").isEmpty()){
                    permisosContainer.getContainerProperty(itemId, "OPCION").setValue(">> " + rsRecords.getString("Descripcion") + " <<");
                }
                else {
                    permisosContainer.getContainerProperty(itemId, "OPCION").setValue(rsRecords.getString("Descripcion"));
                }
                if(rsRecords.getObject("IdUsuarioPermiso") != null) {
                    permisosGrid.select(itemId);
                }
            }
        }
        catch(Exception ex1) {
            Notification.show("ERROR FATAL DEL SISTEMA", Notification.Type.ERROR_MESSAGE);
            System.out.println("ERROR AL INTENTAR BUSCAR CATALOGO DE PERMISOS DE USUARIO : " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    private void saveGroup() {

        String queryString;

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            //DELETE
            queryString =  "DELETE FROM usuario_permisos ";
            queryString += " WHERE IdUsuario = " + idUsuario;

            stQuery.executeUpdate(queryString);

            //INSERT

            queryString =  "Insert Into usuario_permisos (IdUsuario, IdOpcionMenu)";
            queryString += " Values ";

            for(Object selectedItem: permisosGrid.getSelectedRows()) {
                queryString += "(";
                queryString += idUsuario;
                queryString += "," + permisosContainer.getContainerProperty(selectedItem, "ID").getValue();
                queryString += "),";
            }

            queryString = queryString.substring(0, queryString.length()-1);

            stQuery.executeUpdate(queryString);

            Notification.show("OPERACION EXITOSA!", Notification.Type.HUMANIZED_MESSAGE);

            close();

        }
        catch(Exception ex)
        {
            Notification.show("Error al creaar/actualizar permisos de usuario: " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}