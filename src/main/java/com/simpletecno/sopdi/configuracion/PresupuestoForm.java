/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.configuracion;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.DateField;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Runo;
import com.vaadin.ui.themes.ValoTheme;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vaadin.ui.NumberField;

/**
 *
 * @author joseaguirre
 */
public class PresupuestoForm extends Window {
    
    public int presupuestoId;
    public String empresa;
    public String empresaNombre;
    
    MarginInfo  marginInfo;
        
    FormLayout presupuestoForm;
    
    Button exitBtn;
    Button saveBtn;
    
    Statement stQuery = null;
    PreparedStatement stPreparedQuery = null;
    ResultSet rsRecords = null;
    
    DateField fechaDt;    
    TextField cuentaTxt;
    TextField descripcionTxt;
    NumberField quetzalesTxt;
    NumberField dolaresTxt;
    NumberField tipoCambioTxt;
    DateField mesDt;    
    DateField fechaAutorizadoDt;    

    UI mainUI;
    
    public PresupuestoForm() {
        this.mainUI = UI.getCurrent();
        
        setResponsive(true);
        setCaption("Ficha de rubro de presupuesto");
        setModal(true);
               
        marginInfo = new MarginInfo(true,true,false,true);
            //FontAwesome.DESKTOP
        presupuestoForm  = new FormLayout();
        presupuestoForm.setMargin(marginInfo);
        presupuestoForm.setSpacing(true);
        presupuestoForm.addStyleName(ValoTheme.FORMLAYOUT_LIGHT);

        fechaDt = new DateField("Fecha : ");
        fechaDt.setDateFormat("dd/MM/yyyy");
        fechaDt.setWidth("9em");
        fechaDt.setValue(new java.util.Date());

        cuentaTxt = new TextField("Cuenta :");
        cuentaTxt.setWidth("10em");
        cuentaTxt.setMaxLength(32);

        descripcionTxt = new TextField("Descripcion :");
        descripcionTxt.setWidth("20em");
        descripcionTxt.setMaxLength(128);
        
        quetzalesTxt = new NumberField("Quetzales : ");
        quetzalesTxt.setValidationVisible(false);
        quetzalesTxt.setDecimalAllowed(true);
        quetzalesTxt.setDecimalPrecision(2);
        quetzalesTxt.setMinimumFractionDigits(2);
        quetzalesTxt.setDecimalSeparator('.');
        quetzalesTxt.setDecimalSeparatorAlwaysShown(true);
        quetzalesTxt.setValue(0d);
        quetzalesTxt.setGroupingUsed(true);
        quetzalesTxt.setGroupingSeparator(',');
        quetzalesTxt.setGroupingSize(3);
        quetzalesTxt.setImmediate(true);
        quetzalesTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        quetzalesTxt.setWidth("7em");

        dolaresTxt = new NumberField("Dolares : ");
        dolaresTxt.setValidationVisible(false);
        dolaresTxt.setDecimalAllowed(true);
        dolaresTxt.setDecimalPrecision(2);
        dolaresTxt.setMinimumFractionDigits(2);
        dolaresTxt.setDecimalSeparator('.');
        dolaresTxt.setDecimalSeparatorAlwaysShown(true);
        dolaresTxt.setValue(0d);
        dolaresTxt.setGroupingUsed(true);
        dolaresTxt.setGroupingSeparator(',');
        dolaresTxt.setGroupingSize(3);
        dolaresTxt.setImmediate(true);
        dolaresTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        dolaresTxt.setWidth("7em");

        tipoCambioTxt = new NumberField("T.Cambio : ");
        tipoCambioTxt.setDecimalAllowed(true);
        tipoCambioTxt.setDecimalPrecision(5);
        tipoCambioTxt.setMinimumFractionDigits(5);
        tipoCambioTxt.setDecimalSeparator('.');
        tipoCambioTxt.setDecimalSeparatorAlwaysShown(true);
        tipoCambioTxt.setGroupingUsed(true);
        tipoCambioTxt.setGroupingSeparator(',');
        tipoCambioTxt.setGroupingSize(3);
        tipoCambioTxt.setImmediate(true);
        tipoCambioTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        tipoCambioTxt.setWidth("5em");
        tipoCambioTxt.setValue((Float.toString(((SopdiUI)UI.getCurrent()).sessionInformation.getFltExchangeRate())));
        
        mesDt = new DateField("Fecha : ");
        mesDt.setDateFormat("dd/MM/yyyy");
        mesDt.setWidth("9em");
        mesDt.setValue(new java.util.Date());

        fechaAutorizadoDt = new DateField("Fecha autorizado : ");
        fechaAutorizadoDt.setDateFormat("dd/MM/yyyy");
        fechaAutorizadoDt.setWidth("9em");
        fechaAutorizadoDt.setValue(new java.util.Date());
        
        exitBtn = new Button("Salir");
        exitBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        exitBtn.setIcon(FontAwesome.ARROW_RIGHT); 
        exitBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick ( Button.ClickEvent event )
            {
                close();
            }
        });        

        saveBtn = new Button("Guardar");
        saveBtn.setIcon(FontAwesome.SAVE); 
        saveBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick ( Button.ClickEvent event )
            {
                saveData();
            }
        });
                
        presupuestoForm.addComponent(fechaDt);
        presupuestoForm.addComponent(cuentaTxt);
        presupuestoForm.addComponent(descripcionTxt);
        presupuestoForm.addComponent(quetzalesTxt);
        presupuestoForm.addComponent(dolaresTxt);
        presupuestoForm.addComponent(tipoCambioTxt);
        presupuestoForm.addComponent(mesDt);
        presupuestoForm.addComponent(fechaAutorizadoDt);

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        
        buttonsLayout.addComponent(exitBtn);
        buttonsLayout.setComponentAlignment(exitBtn, Alignment.BOTTOM_LEFT);
        buttonsLayout.addComponent(saveBtn);
        buttonsLayout.setComponentAlignment(saveBtn, Alignment.BOTTOM_CENTER);
        
        presupuestoForm.addComponent(buttonsLayout);
        presupuestoForm.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);
        
        VerticalLayout  contentLayout = new VerticalLayout();
        contentLayout.setSizeUndefined();
        
        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setWidth("100%");
        titleLayout.setMargin(false);

        Label titleLbl = new Label("EDITAR RUBRO DE PRESUPUESTO");
        if(presupuestoId == 0) {
            titleLbl.setValue("NUEVO RUBRO DE PRESUPUESTO");
        }
        titleLbl.addStyleName(Runo.LABEL_H2);
        titleLbl.setSizeUndefined();

        titleLayout.addComponent(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.TOP_CENTER);
                
        contentLayout.addComponent(titleLayout);
        contentLayout.setComponentAlignment(titleLayout, Alignment.TOP_CENTER);
        contentLayout.addComponent(presupuestoForm);
        contentLayout.setComponentAlignment(presupuestoForm, Alignment.TOP_CENTER);

        setContent(contentLayout);
    }    
 
    public void fillData() {
        String queryString = "";
        
        queryString =  "Select * ";
        queryString += " From  presupuesto ";
        queryString += " Where IdPresupuesto = " + String.valueOf(presupuestoId);

//System.out.println("\n\n"+queryString);

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();            
            rsRecords = stQuery.executeQuery (queryString);

            if(rsRecords.next()) { //  encontrado
                fechaDt.setValue(rsRecords.getDate("Fecha"));
                cuentaTxt.setValue(rsRecords.getString("Cuenta"));
                descripcionTxt.setValue(rsRecords.getString("Descripcion"));
                quetzalesTxt.setValue(rsRecords.getDouble("MontoQuetzales"));
                dolaresTxt.setValue(rsRecords.getDouble("MontoDolares"));
                tipoCambioTxt.setValue(rsRecords.getDouble("TipoCambio"));
                mesDt.setValue(rsRecords.getDate("Mes"));
                fechaAutorizadoDt.setValue(rsRecords.getDate("FechaAutorizado"));
            }
            
        } 
        catch (Exception ex) {
            Logger.getLogger(PresupuestoForm.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Error al intentar leer registros de presupuesto : " + ex.getMessage());
            Notification.show("Error al intentar leer registros de presupuesto..!", Notification.Type.ERROR_MESSAGE);
        } 
    }

    private void saveData() {

        if(cuentaTxt.getValue().trim().isEmpty()) {
            Notification.show("Error, falta la cuenta!", Notification.Type.ERROR_MESSAGE);
            cuentaTxt.focus();
            return;
        }
        if(descripcionTxt.getValue().trim().isEmpty()) {
            Notification.show("Error, falta la descripcion!", Notification.Type.ERROR_MESSAGE);
            descripcionTxt.focus();
            return;
        }
        if(quetzalesTxt.getDoubleValueDoNotThrow() == 0.00) {
            Notification.show("Error, falta quetzales!", Notification.Type.ERROR_MESSAGE);
            quetzalesTxt.focus();
            return;
        }
        if(dolaresTxt.getDoubleValueDoNotThrow() == 0.00) {
            Notification.show("Error, falta dolares!", Notification.Type.ERROR_MESSAGE);
            dolaresTxt.focus();
            return;
        }

        String queryString;
        
        try {
            if(presupuestoId == 0) {
                queryString = "Insert Into presupuesto (Fecha, Cuenta, Descripcion, MontoQuetzales, MontoDolares, ";
                queryString += " TipoCambio, Mes, IdEmpresa, Empresa, Tipo, FechaAutorizado) ";
                queryString += " Values (";
                queryString += " '" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
                queryString += ",'" + cuentaTxt.getValue() + "'";
                queryString += ",'" + descripcionTxt.getValue() + "'";
                queryString += ", " + String.valueOf(quetzalesTxt.getDoubleValueDoNotThrow());
                queryString += ", " + String.valueOf(dolaresTxt.getDoubleValueDoNotThrow());
                queryString += ", " + String.valueOf(tipoCambioTxt.getDoubleValueDoNotThrow());
                queryString += ",'" + Utileria.getFechaYYYYMMDD_1(mesDt.getValue()) + "'";
                queryString += ", " + empresa;
                queryString += ",'" + empresaNombre + "'";
                queryString += ",'INICIAL'";
                queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaAutorizadoDt.getValue()) + "'";
                queryString += ")";
            }
            else {
                queryString =  "Update presupuesto Set ";
                queryString += " Cuenta = '" + cuentaTxt.getValue()  + "'";
                queryString += ",Descripcion = '" + descripcionTxt.getValue()  + "'";
                queryString += ",MontoQuetzales = " + String.valueOf(quetzalesTxt.getDoubleValueDoNotThrow());
                queryString += ",MontoDolares = " + String.valueOf(dolaresTxt.getDoubleValueDoNotThrow());
                queryString += ",TipoCambio = " + String.valueOf(tipoCambioTxt.getDoubleValueDoNotThrow());
                queryString += ",Mes ='" + Utileria.getFechaYYYYMMDD_1(mesDt.getValue()) + "'";
                queryString += ",Tipo = 'MODIFICADO'";
                queryString += ",FechaAutorizado = '" + Utileria.getFechaYYYYMMDD_1(fechaAutorizadoDt.getValue()) + "'";
                queryString += " Where IdPresupuesto = " + String.valueOf(presupuestoId);
            }

System.out.println("queryString="+queryString);

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();            
            stQuery.executeUpdate(queryString);

            Notification.show("OPERACION EXITOSA!", Notification.Type.HUMANIZED_MESSAGE);

            ((PresupuestoView)(mainUI.getNavigator().getCurrentView())).fillReportTable(empresa);
        }
        catch(Exception ex)
        {
            Notification.show("Error al actualizar cliente : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
        close();
    }    

}