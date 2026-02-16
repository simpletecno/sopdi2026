/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.inventario;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joseaguirre
 */
public class InvBodegaForm extends Window {
    
    VerticalLayout mainLayout;
    MarginInfo  marginInfo;
    
    public  int idInvBodega = 0;
    
    FormLayout invBodegaForm;
    
    Button saveBtn;
    
    Statement stQuery = null;
    static PreparedStatement stPreparedQuery;
    ResultSet rsRecords = null;
        
    TextField idInvBodegaTxt;
    TextField nombreTxt;
    TextField ubicacionTxt;
    ComboBox estatusCbx;
    TextField razonTxt;

    UI mainUI;
    
    public InvBodegaForm(int idInvBodega, String invBodegaNombre) {
        this.idInvBodega = idInvBodega;
        this.mainUI = UI.getCurrent();
       
        setResponsive(true);
        setCaption("Ficha del bodega  : " + idInvBodega + " " + invBodegaNombre);
        setWidth("50%");
        setHeight("50%");
        
        marginInfo = new MarginInfo(true,true,false,true);
        
        mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();

        createFormLayout();

        saveBtn = new Button("Guardar");
        saveBtn.setIcon(FontAwesome.SAVE); 
        saveBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        saveBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick ( Button.ClickEvent event )
            {
                saveProject();
            }
        });                

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.addComponent(saveBtn);
        buttonsLayout.setComponentAlignment(saveBtn, Alignment.BOTTOM_LEFT);
        
        mainLayout.addComponent(buttonsLayout);
        mainLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);        
        
        setContent(mainLayout);

        if(idInvBodega > 0) {
            fillData();
        }
    }
    
    private void createFormLayout() {
        VerticalLayout tab1Layout = new VerticalLayout();
        tab1Layout.setSpacing(true);
        tab1Layout.setMargin(true);
        tab1Layout.setWidth("100%");
        
        invBodegaForm  = new FormLayout();
        invBodegaForm.setMargin(marginInfo);
        invBodegaForm.setSpacing(false);
        invBodegaForm.addStyleName(ValoTheme.FORMLAYOUT_LIGHT);
        invBodegaForm.setWidth("60%");

        idInvBodegaTxt = new TextField("Id bodega : ");
//        idInvBodegaTxt.setWidth("8em");
        idInvBodegaTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);
        idInvBodegaTxt.setValue(String.valueOf(idInvBodega));
        idInvBodegaTxt.setReadOnly(true);

        nombreTxt = new TextField("Nombre : ");
        nombreTxt.setWidth("100%");
        nombreTxt.setRequired(true);
        nombreTxt.setRequiredError("POR FAVOR INGRESE EL NOMBRE DE BODEGA");

        ubicacionTxt = new TextField("Ubicación : ");
        ubicacionTxt.setWidth("100%");
        ubicacionTxt.setRequired(true);
        ubicacionTxt.setRequiredError("POR FAVOR INGRESE UBICACION DE BODEGA");
        
        estatusCbx = new ComboBox("Estatus : ");
        estatusCbx.setNewItemsAllowed(false);
        estatusCbx.setInvalidAllowed(false);
        estatusCbx.addItem("ACTIVO");
        estatusCbx.addItem("INACTIVO");
        estatusCbx.select("ACTIVO");

        razonTxt = new TextField("Razón : ");

        invBodegaForm.addComponent(idInvBodegaTxt);
        invBodegaForm.addComponent(nombreTxt);
        invBodegaForm.addComponent(ubicacionTxt);
        invBodegaForm.addComponent(estatusCbx);
        invBodegaForm.addComponent(razonTxt);

        tab1Layout.addComponent(invBodegaForm);
        tab1Layout.setComponentAlignment(invBodegaForm, Alignment.MIDDLE_CENTER);
        
        nombreTxt.focus();

        mainLayout.addComponent(tab1Layout);
        mainLayout.setComponentAlignment(tab1Layout, Alignment.TOP_CENTER);
        
    }
    
    public void fillData() {
        String queryString = "";
        
        queryString =  "Select * ";
        queryString += " From  inv_bodega ";
        queryString += " Where IdBodega = " + idInvBodegaTxt.getValue();

//System.out.println("\n\n"+queryString);

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();            
            rsRecords = stQuery.executeQuery (queryString);

            if(rsRecords.next()) { //  encontrado
                nombreTxt.setValue(rsRecords.getString("Nombre"));
                ubicacionTxt.setValue(rsRecords.getString("Ubicación"));
                estatusCbx.select(rsRecords.getString("Estatus"));
                razonTxt.setValue(rsRecords.getString("Razon"));
             }
        } 
        catch (Exception ex) {
            Logger.getLogger(InvBodegaForm.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de bodega : " + ex.getMessage());
            Notification.show("Error al intentar leer registros de bodega..!", Notification.Type.ERROR_MESSAGE);
        } 
    }

    private void saveProject() {
        
        if(nombreTxt.getValue().trim().isEmpty()) {
            Notification.show("Por favor, ingrese el nombre!", Notification.Type.WARNING_MESSAGE);
            nombreTxt.focus();
            return;
        }
        if(ubicacionTxt.getValue().trim().isEmpty()) {
            Notification.show("Por favor, ingrese la ubicación!", Notification.Type.WARNING_MESSAGE);
            ubicacionTxt.focus();
            return;
        }

        String queryString;
        
        if(idInvBodegaTxt.getValue().compareTo("0") == 0) {
            queryString =  "Insert Into inv_bodega (IdEmpresa, Nombre, Ubicacion)";
            queryString += " Values (";
            queryString +=  ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
            queryString += ",'"  + nombreTxt.getValue()      + "'";
            queryString += ",'"  + ubicacionTxt.getValue()      + "'";
            queryString += ")";
        }
        else {
            queryString =  "Update inv_bodega Set ";
            queryString += " Nombre       = '" + nombreTxt.getValue()    + "'";
            queryString += ",Ubicacion    = '" + ubicacionTxt.getValue() + "'";
            queryString += " Where IdBodega = " + idInvBodegaTxt.getValue();
        }
//System.out.println("queryString="+queryString);
        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            Notification.show("OPERACION EXITOSA!", Notification.Type.HUMANIZED_MESSAGE);

            close();

            if(mainUI.getNavigator().getCurrentView().getClass().getSimpleName().equals("InvBodegasView")) {
                ((InvBodegasView)(mainUI.getNavigator().getCurrentView())).fillBodegasGrid();
            }

        }
        catch(Exception ex)
        {
            Notification.show("Error al actualizar bodega : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }    

}