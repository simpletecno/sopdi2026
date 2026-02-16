/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.operativo;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joseaguirre
 */
public class CostCenterAccountForm extends Window {
    
    VerticalLayout mainLayout;
    MarginInfo  marginInfo;
    
    FormLayout centroCostoForm;
    
    Button saveBtn;
    Button salirBtn;
    
    Statement stQuery = null;
    ResultSet rsRecords = null;
        
    TextField idCentroCostoTxt;
    TextField codigoTxt;
    TextField descripcionTxt;
    ComboBox grupoCbx;
    ComboBox clasificacionCbx;
    ComboBox tipoCbx;
    ComboBox unidadMedidaCbx;
    ComboBox estatusCbx;

    UI mainUI;
    
    public CostCenterAccountForm() {
        this.mainUI = UI.getCurrent();
        
        setWidth("50%");
       
        setCaption(((SopdiUI) mainUI).sessionInformation.getStrCompanyName() + " - Ficha de cuenta de centro de costo");
        Page.getCurrent().setTitle(((SopdiUI) mainUI).sessionInformation.getStrCompanyName() + " - Ficha de cuenta de centro de costo");
        
        marginInfo = new MarginInfo(true,true,true,true);
                
        mainLayout = new VerticalLayout();                
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);
        
        centroCostoForm  = new FormLayout();

        idCentroCostoTxt = new TextField("Id Cuenta Centro Costo : ");
        idCentroCostoTxt.setWidth("8em");
        idCentroCostoTxt.setReadOnly(true);
        idCentroCostoTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);
             
        codigoTxt = new TextField("Código : ");
        codigoTxt.setWidth("8em");
        codigoTxt.setRequired(true);
        codigoTxt.setRequiredError("POR FAVOR INGRESE EL CODIGO DE CUENTA DE CENTRO DE COSTO");

        descripcionTxt = new TextField("Descripción : ");
        descripcionTxt.setWidth("30em");
        descripcionTxt.setRequired(true);
        descripcionTxt.setRequiredError("POR FAVOR INGRESE LA DESCRIPCION DE LA TAREA");

        grupoCbx = new ComboBox("Grupo : ");
        grupoCbx.setWidth("30em");
        grupoCbx.setInvalidAllowed(true);
        grupoCbx.setNewItemsAllowed(true);        
        grupoCbx.setFilteringMode(FilteringMode.CONTAINS);        

        fillComboGrupo();

        clasificacionCbx = new ComboBox("Clasificación : ");
        clasificacionCbx.setWidth("30em");
        clasificacionCbx.setInvalidAllowed(true);
        clasificacionCbx.setNewItemsAllowed(true);        
        clasificacionCbx.setFilteringMode(FilteringMode.CONTAINS);        

        fillComboClasificacion();

        tipoCbx = new ComboBox("Tipo : ");
        tipoCbx.setWidth("8em");
        tipoCbx.setInvalidAllowed(true);
        tipoCbx.setNewItemsAllowed(true);        
        tipoCbx.setFilteringMode(FilteringMode.CONTAINS);
        tipoCbx.addItem("1");
        tipoCbx.addItem("2");
        tipoCbx.select("1");

        unidadMedidaCbx = new ComboBox("Unidad de medida : ");
        unidadMedidaCbx.setWidth("8em");
        unidadMedidaCbx.setInvalidAllowed(true);
        unidadMedidaCbx.setNewItemsAllowed(true);        
        unidadMedidaCbx.setFilteringMode(FilteringMode.CONTAINS);        

        fillComboUnidadMedida();

        estatusCbx = new ComboBox("Estatus : ");
        estatusCbx.setWidth("8em");
        estatusCbx.setInvalidAllowed(true);
        estatusCbx.setNewItemsAllowed(true);        
        estatusCbx.setFilteringMode(FilteringMode.CONTAINS);
        estatusCbx.addItem("ACTIVA");
        estatusCbx.addItem("INACTIVA");
        estatusCbx.select("ACTIVA");

        centroCostoForm.addComponent(idCentroCostoTxt);
        centroCostoForm.addComponent(codigoTxt);
        centroCostoForm.addComponent(descripcionTxt);
        centroCostoForm.addComponent(grupoCbx);
        centroCostoForm.addComponent(clasificacionCbx);
        centroCostoForm.addComponent(tipoCbx);
        centroCostoForm.addComponent(unidadMedidaCbx);
        centroCostoForm.addComponent(estatusCbx);
        
        mainLayout.addComponent(centroCostoForm);

        saveBtn = new Button("Guardar");
        saveBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        saveBtn.setIcon(FontAwesome.SAVE); 
        saveBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick ( Button.ClickEvent event )
            {
                saveTarea();
            }
        });

        salirBtn = new Button("Salir");
        salirBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        salirBtn.setIcon(FontAwesome.ARROW_RIGHT); 
        salirBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick ( Button.ClickEvent event )
            {
                close();
            }
        });        
        
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        
        buttonsLayout.addComponent(salirBtn);
        buttonsLayout.setComponentAlignment(salirBtn, Alignment.BOTTOM_LEFT);
        buttonsLayout.addComponent(saveBtn);
        buttonsLayout.setComponentAlignment(saveBtn, Alignment.BOTTOM_CENTER);
        
        mainLayout.addComponent(buttonsLayout);
        mainLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);
                
        setContent(mainLayout);
    }    

    void fillComboGrupo() {
        String queryString = "Select Distinct Grupo ";
        queryString += " From centro_costo_cuenta ";
 
        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();            
            rsRecords = stQuery.executeQuery (queryString);
                                    
            while(rsRecords.next()) { //  encontrado                
                grupoCbx.addItem(rsRecords.getString("Grupo"));
            }
        }
        catch(Exception ex1) {
            Notification.show("ERROR FATAL DEL SISTEMA", Notification.Type.ERROR_MESSAGE);
            System.out.println("ERROR AL INTENTAR BUSCAR CATALOGO DE GRUPOS DE CENTRO DE COSTO : " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    void fillComboClasificacion() {
        String queryString = "Select Distinct Clasificacion ";
        queryString += " From centro_costo_cuenta ";
 
        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();            
            rsRecords = stQuery.executeQuery (queryString);
                                    
            while(rsRecords.next()) { //  encontrado                
                clasificacionCbx.addItem(rsRecords.getString("Clasificacion"));
            }
        }
        catch(Exception ex1) {
            Notification.show("ERROR FATAL DEL SISTEMA", Notification.Type.ERROR_MESSAGE);
            System.out.println("ERROR AL INTENTAR BUSCAR CATALOGO DE CLASIFICACION DE CENTRO DE COSTO : " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    void fillComboUnidadMedida() {
        String queryString = "Select Distinct UnidadMedida ";
        queryString += " From centro_costo_cuenta ";
 
        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();            
            rsRecords = stQuery.executeQuery (queryString);
                                    
            while(rsRecords.next()) { //  encontrado                
                unidadMedidaCbx.addItem(rsRecords.getString("UnidadMedida"));
            }
        }
        catch(Exception ex1) {
            Notification.show("ERROR FATAL DEL SISTEMA", Notification.Type.ERROR_MESSAGE);
            System.out.println("ERROR AL INTENTAR BUSCAR CATALOGO DE UNIDAD DE MEDIDA DE CENTRO DE COSTO : " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void fillData() {
        
        if(idCentroCostoTxt.getValue().compareTo("0") == 0) {
            codigoTxt.focus();
            return;
        }
        
        String queryString = "";
        
        queryString =  "Select * ";
        queryString += " From  centro_costo_cuenta ";
        queryString += " Where IdCuentaCentroCosto = " + idCentroCostoTxt.getValue();

//System.out.println("\n\n"+queryString);

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();            
            rsRecords = stQuery.executeQuery (queryString);

            if(rsRecords.next()) { //  encontrado
                codigoTxt.setValue(rsRecords.getString("CodigoCuentaCentroCosto"));
                descripcionTxt.setValue(rsRecords.getString("Descripcion"));
                codigoTxt.focus();
                grupoCbx.select(rsRecords.getString("Grupo"));
                clasificacionCbx.select(rsRecords.getString("Clasificacion"));
                tipoCbx.select(rsRecords.getString("Tipo"));
                unidadMedidaCbx.select(rsRecords.getString("UnidadMedida"));
                estatusCbx.select(rsRecords.getString("Estatus"));
                
                codigoTxt.setReadOnly(true);            
            }            
            else {
                Notification.show("Error, no se encotró registro de centro de costo!", Notification.Type.ERROR_MESSAGE);
                close();
            }
        } 
        catch (Exception ex) {
            Logger.getLogger(ProjectTaskView.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de centros de costo : " + ex.getMessage());
            Notification.show("Error al intentar leer registros de centros de costo..!", Notification.Type.ERROR_MESSAGE);
        } 
    }

    private void saveTarea() {
        
        if(codigoTxt.getValue().trim().isEmpty()) {
            Notification.show("Error, falta el código de cuenta de centro de costo!", Notification.Type.ERROR_MESSAGE);
            codigoTxt.focus();
            return;
        }

        if(descripcionTxt.getValue().trim().isEmpty()) {
            Notification.show("Error, falta la descripción de cuenta de centro de costo!", Notification.Type.ERROR_MESSAGE);
            descripcionTxt.focus();
            return;
        }

        String queryString;
        
        if(idCentroCostoTxt.getValue().compareTo("0") == 0) {
            queryString =  "Insert Into centro_costo_cuenta (IdProyecto, CodigoCuentaCentroCosto, Grupo, ";
            queryString += " Clasificacion, Tipo, UnidadMedida, Estatus )";
            queryString += " Values (";
            queryString += "  " + ((SopdiUI) mainUI).sessionInformation.getStrProjectId();
            queryString += ",'" + codigoTxt.getValue() + "'";
            queryString += ",'" + String.valueOf(grupoCbx.getValue()) + "'";
            queryString += ",'" + String.valueOf(clasificacionCbx.getValue()) + "'";
            queryString += ",'" + String.valueOf(tipoCbx.getValue()) + "'";
            queryString += ",'" + String.valueOf(unidadMedidaCbx.getValue()) + "'";
            queryString += ",'" + String.valueOf(estatusCbx.getValue()) + "'";
            queryString += ")";
        }
        else {
            queryString =  "Update centro_costo_cuenta Set ";
            queryString += " CodigoCuentaCentroCosto = '" + codigoTxt.getValue()      + "'";
            queryString += ",Grupo = '" + String.valueOf(grupoCbx.getValue()) + "'";
            queryString += ",Clasificacion = '" + String.valueOf(clasificacionCbx.getValue()) + "'";
            queryString += ",Tipo = '" + String.valueOf(tipoCbx.getValue()) + "'";
            queryString += ",UnidadMedida = '" + String.valueOf(unidadMedidaCbx.getValue()) + "'";
            queryString += ",Estatus = '" + String.valueOf(estatusCbx.getValue()) + "'";
            queryString += " Where IdCuentaCentroCosto = " + idCentroCostoTxt.getValue();
        }
//System.out.println("queryString="+queryString);
        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();            
            stQuery.executeUpdate(queryString);
               
            Notification.show("OPERACION EXITOSA!", Notification.Type.HUMANIZED_MESSAGE);
            
            ((CostCenterAccountView)(mainUI.getNavigator().getCurrentView())).fillCostCenterGrid();
        }
        catch(Exception ex)
        {
            Notification.show("Error al actualizar cuenta de centro de costo : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }        
    }        
}