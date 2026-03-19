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
 * PERMISOS PARA TIPOS DE ORDENES DE COMPRA
 * @author joseaguirre
 */
public class UsuarioPermisosOrdenCompraForm extends Window {
    VerticalLayout mainLayout;
    MarginInfo  marginInfo;

    private int idUsuario = 0;

    Button saveBtn;

    Statement stQuery = null;
    ResultSet rsRecords = null;

    IndexedContainer permisosContainer = new IndexedContainer();
    Grid permisosGrid = new Grid("", permisosContainer);

    UI mainUI;

    public UsuarioPermisosOrdenCompraForm(
            int idUsuario,
            String usuarioNombre
    ) {
        this.idUsuario = idUsuario;
        this.mainUI = UI.getCurrent();

        setResponsive(true);
        setCaption("Permisos Usuario Tipos Ordenes de Compra : " + idUsuario + " " + usuarioNombre);
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
        permisosContainer.addContainerProperty("TIPO ORDEN DE COMPRA", String.class, "");

        permisosGrid.setWidth("100%");
        permisosGrid.setImmediate(true);
        permisosGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        permisosGrid.setDescription("Seleccione uno o varios TIPOS DE ORDENES DE COMPRA.");
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

        String queryString = "SELECT TOC.*, UPT.IdUsuario FROM tipo_orden_compra TOC ";
        queryString += "LEFT OUTER JOIN usuario_permisos_tipo_orden_compra UPT ON UPT.IdTipoOrdenDeCompra = TOC.Id ";
        queryString += "AND UPT.IdUsuario= " + idUsuario;

        permisosContainer.removeAllItems();

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords = stQuery.executeQuery (queryString);

            while(rsRecords.next()) { //  encontrado
                Object itemId = permisosContainer.addItem();
                permisosContainer.getContainerProperty(itemId, "ID").setValue(rsRecords.getString("Id"));
                permisosContainer.getContainerProperty(itemId, "TIPO ORDEN DE COMPRA").setValue(rsRecords.getString("Descripcion"));
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
            queryString =  "DELETE FROM usuario_permisos_tipo_orden_compra ";
            queryString += " WHERE IdUsuario = " + idUsuario;

            stQuery.executeUpdate(queryString);

            //INSERT

            queryString =  "Insert Into usuario_permisos_tipo_orden_compra (IdUsuario, IdTipoOrdenDeCompra) ";
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