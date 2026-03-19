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
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 *
 * @author joseaguirre
 */
public class UsuarioPermisosEmpresaForm extends Window {
    VerticalLayout mainLayout;
    MarginInfo  marginInfo;

    private int idUsuario = 0;

    Button saveBtn;

    Statement stQuery = null;
    ResultSet rsRecords = null;

    IndexedContainer permisosContainer = new IndexedContainer();
    Grid permisosGrid = new Grid("", permisosContainer);

    UI mainUI;

    public UsuarioPermisosEmpresaForm(
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
        mainLayout.setSizeFull();

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

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.addComponent(saveBtn);
        buttonsLayout.setComponentAlignment(saveBtn, Alignment.BOTTOM_LEFT);
        
        mainLayout.addComponent(buttonsLayout);
        mainLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);        
        
        setContent(mainLayout);
    }
    
    private void createGridIdex() {

        permisosContainer.addContainerProperty("ID", String.class, "");
        permisosContainer.addContainerProperty("NOMBRE", String.class, "");
        permisosContainer.addContainerProperty("NIT", String.class, "");

        permisosGrid.setWidth("100%");
        permisosGrid.setImmediate(true);
        permisosGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        permisosGrid.setDescription("Seleccione uno o varios permisos.");
        permisosGrid.setHeightMode(HeightMode.ROW);
        permisosGrid.setHeightByRows(15);
        permisosGrid.setResponsive(true);
        permisosGrid.setResponsive(true);
        permisosGrid.setSizeFull();
        
        llenarGridPermisos();

        mainLayout.addComponent(permisosGrid);
        mainLayout.setComponentAlignment(permisosGrid, Alignment.TOP_CENTER);

    }

    void llenarGridPermisos() {

        if(permisosGrid == null) {
            return;
        }

        String queryString = "SELECT CE.*, UPE.IdUsuario FROM contabilidad_empresa CE ";
        queryString += "LEFT OUTER JOIN usuario_permisos_empresa UPE ON UPE.IdEmpresa = CE.IdEmpresa ";
        queryString += "AND UPE.IdUsuario= " + idUsuario;

        permisosContainer.removeAllItems();

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords = stQuery.executeQuery (queryString);

            while(rsRecords.next()) { //  encontrado
                Object itemId = permisosContainer.addItem();
                permisosContainer.getContainerProperty(itemId, "ID").setValue(rsRecords.getString("IdEmpresa"));
                permisosContainer.getContainerProperty(itemId, "NOMBRE").setValue(rsRecords.getString("NombreCorto"));
                permisosContainer.getContainerProperty(itemId, "NIT").setValue(rsRecords.getString("NIT"));
                if(rsRecords.getObject("IdUsuario") != null) {
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
            queryString =  "DELETE FROM usuario_permisos_empresa ";
            queryString += " WHERE IdUsuario = " + idUsuario;

            stQuery.executeUpdate(queryString);

            //INSERT

            queryString =  "INSERT INTO usuario_permisos_empresa (IdUsuario, IdEmpresa)";
            queryString += " VALUES ";

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