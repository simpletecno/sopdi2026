/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.operativo;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.ui.NumberField;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joseaguirre
 */
public class ProjectTaskForm extends Window {
    
    final long MILLSECS_PER_DAY = 24 * 60 * 60 * 1000; //Milisegundos al día         
    VerticalLayout mainLayout;
    MarginInfo  marginInfo;
    
    FormLayout tareaForm;
    
    Button saveBtn;
    Button salirBtn;
    
    Statement stQuery = null;
    ResultSet rsRecords = null;
        
    TextField idTareaTxt;
    ComboBox projectCbx;
    TextField descripcionTxt;
    NumberField diasDuracionTxt;
    PopupDateField startDt;
    PopupDateField endDt;
    TextField predecesoresTxt;
    TextField sucesoresTxt;
    ComboBox nivelCbx;
    TextField nivelCodigoTxt;
    ComboBox  proveedorCbx;
    
    ComboBox  prioridadCbx;

    UI mainUI;
    
    public ProjectTaskForm() {
        this.mainUI = UI.getCurrent();
        
        setWidth("50%");
       
        setCaption(((SopdiUI) mainUI).sessionInformation.getStrCompanyName() + " - Ficha de tarea project");
        Page.getCurrent().setTitle(((SopdiUI) mainUI).sessionInformation.getStrCompanyName() + " - Ficha de tarea project");
        
        marginInfo = new MarginInfo(true,true,true,true);
                
        mainLayout = new VerticalLayout();                
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);
        
        tareaForm  = new FormLayout();

        idTareaTxt = new TextField("Id Tarea : ");
        idTareaTxt.setWidth("8em");
        idTareaTxt.setReadOnly(true);
        idTareaTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);
             
        projectCbx = new ComboBox("Project de : ");
        projectCbx.addItem("Urbanización");
        projectCbx.addItem("Casas 1");
        projectCbx.addItem("Casas 2");
        projectCbx.addItem("Casas 3");
        projectCbx.addItem("Casas 4");
        projectCbx.addItem("Casas 5");
        projectCbx.select("Casas 1");

        descripcionTxt = new TextField("Descripción : ");
        descripcionTxt.setWidth("30em");
        descripcionTxt.setRequired(true);
        descripcionTxt.setRequiredError("POR FAVOR INGRESE LA DESCRIPCION DE LA TAREA");

        diasDuracionTxt = new NumberField("Días duración : ");
        diasDuracionTxt.setWidth("10em");
        diasDuracionTxt.setDecimalPrecision(0);
        diasDuracionTxt.setDecimalSeparator('.');
        diasDuracionTxt.setValue(0d);
        diasDuracionTxt.setGroupingUsed(true);
        diasDuracionTxt.setGroupingSeparator(',');
        diasDuracionTxt.setImmediate(true);
        diasDuracionTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);
        diasDuracionTxt.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                cambiarFechas((int)diasDuracionTxt.getDoubleValueDoNotThrow());
            }
        });

        startDt = new PopupDateField("Fecha inicio : ");        
        startDt.setValue(new java.util.Date());        
        startDt.setResolution(Resolution.DAY);
        startDt.setWidth("125px");
        startDt.setDateFormat("dd/MM/yyyy");
        startDt.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                cambiarFechas(0);
            }
        });
        
        endDt = new PopupDateField("Fecha fin : ");        
        endDt.setValue(new java.util.Date());        
        endDt.setResolution(Resolution.DAY);
        endDt.setWidth("125px");
        endDt.setDateFormat("dd/MM/yyyy");
        endDt.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                cambiarFechas(0);
            }
        });

        predecesoresTxt = new TextField("Predecesores : ");
        predecesoresTxt.setWidth("8em");
        predecesoresTxt.setReadOnly(true);
        predecesoresTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);

        sucesoresTxt = new TextField("Sucesores : ");
        sucesoresTxt.setWidth("8em");
        sucesoresTxt.setReadOnly(true);
        sucesoresTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);

        nivelCbx = new ComboBox("Nivel : ");
        nivelCbx.addItem(0);
        nivelCbx.addItem(1);
        nivelCbx.addItem(2);
        nivelCbx.addItem(3);
        nivelCbx.addItem(4);
        nivelCbx.select(0);
        nivelCbx.setNewItemsAllowed(false);
        nivelCbx.setNullSelectionAllowed(false);
        nivelCbx.setReadOnly(true);
        
        nivelCodigoTxt = new TextField("Nivel código : ");
        nivelCodigoTxt.setWidth("8em");
        nivelCodigoTxt.setReadOnly(true);
        nivelCodigoTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);

        proveedorCbx = new ComboBox("Contratista : ");
        proveedorCbx.setWidth("20em");
        proveedorCbx.setNewItemsAllowed(true);
        proveedorCbx.setInvalidAllowed(true);
        proveedorCbx.setTextInputAllowed(true);
      
        llenarComboProveedor();

        prioridadCbx = new ComboBox("Prioridad :");
        prioridadCbx.setWidth("8em");
        prioridadCbx.addItem("ALTA");
        prioridadCbx.addItem("MEDIA");
        prioridadCbx.addItem("BAJA");
        prioridadCbx.select("BAJA");
        
        tareaForm.addComponent(idTareaTxt);
        tareaForm.addComponent(projectCbx);
        tareaForm.addComponent(descripcionTxt);
        tareaForm.addComponent(diasDuracionTxt);
        tareaForm.addComponent(startDt);
        tareaForm.addComponent(endDt);
        tareaForm.addComponent(predecesoresTxt);
        tareaForm.addComponent(sucesoresTxt);
        tareaForm.addComponent(nivelCbx);
        tareaForm.addComponent(nivelCodigoTxt);
        tareaForm.addComponent(proveedorCbx);
        
        mainLayout.addComponent(tareaForm);
        
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

    void llenarComboProveedor() {
        String queryString = "SELECT * ";
        queryString += " FROM proveedor ";
        queryString += " WHERE N0 <> 1";
        queryString += " ORDER BY Nombre";
 
        proveedorCbx.removeAllItems();

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();            
            rsRecords = stQuery.executeQuery (queryString);
                                    
            while(rsRecords.next()) { //  encontrado                
                proveedorCbx.addItem(rsRecords.getInt("IdProveedor"));
                proveedorCbx.setItemCaption(rsRecords.getInt("IdProveedor"), rsRecords.getString("Proveedor"));
            }
        }
        catch(Exception ex1) {
            Notification.show("ERROR FATAL DEL SISTEMA", Notification.Type.ERROR_MESSAGE);
            System.out.println("ERROR AL INTENTAR BUSCAR CATALOGO DE PROVEEDORES : " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void fillData() {
        
        if(idTareaTxt.getValue().compareTo("0") == 0) {
            nivelCbx.setReadOnly(false);
            nivelCodigoTxt.setReadOnly(false);
            descripcionTxt.focus();
            return;
        }
        
        String queryString = "";
        
        queryString =  "SELECT * ";
        queryString += " FROM  tarea ";
        queryString += " WHERE IdTarea = " + idTareaTxt.getValue();

//System.out.println("\n\n"+queryString);

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();            
            rsRecords = stQuery.executeQuery (queryString);

            if(rsRecords.next()) { //  encontrado

                projectCbx.setReadOnly(false);
                projectCbx.select(rsRecords.getString("Fase"));
                projectCbx.setReadOnly(true);
                descripcionTxt.setValue(rsRecords.getString("Descripcion"));
                startDt.setValue(rsRecords.getDate("FechaInicio"));
                endDt.setValue(rsRecords.getDate("FechaFin"));
                diasDuracionTxt.setValue(rsRecords.getDouble("DiasDuracion"));
                predecesoresTxt.setReadOnly(false);
                predecesoresTxt.setValue(rsRecords.getString("IdPredecesores"));
                predecesoresTxt.setReadOnly(true);
                sucesoresTxt.setReadOnly(false);
                sucesoresTxt.setValue(rsRecords.getString("IdSucesores"));
                sucesoresTxt.setReadOnly(true);
                nivelCbx.setReadOnly(false);
                nivelCbx.select(rsRecords.getInt("Nivel"));
                nivelCbx.setReadOnly(true);
                nivelCodigoTxt.setReadOnly(false);
                nivelCodigoTxt.setValue(rsRecords.getString("NivelCodigo"));
                nivelCodigoTxt.setReadOnly(true);
                proveedorCbx.select(rsRecords.getInt("IdProveedor"));
                prioridadCbx.setValue(rsRecords.getString("Prioridad"));
                
                if(String.valueOf(nivelCbx.getValue()).compareTo("4") != 0) {
                    this.tareaForm.setReadOnly(true);
                    saveBtn.setEnabled(false);
                    Notification.show("Solamente se puede editar tareas de nivel 4.", Notification.Type.WARNING_MESSAGE);
                    close();
                }
                else {
                    descripcionTxt.focus();
                }

            }
            else {
                Notification.show("Error, no se encotró registro de esta tarea!", Notification.Type.ERROR_MESSAGE);
                close();
            }
        } 
        catch (Exception ex) {
            Logger.getLogger(ProjectTaskView.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de tareas : " + ex.getMessage());
            Notification.show("Error al intentar leer registros de tareas..!", Notification.Type.ERROR_MESSAGE);
        } 
    }

    private void saveTarea() {
        
        if(descripcionTxt.getValue().trim().isEmpty()) {
            Notification.show("Error, falta la descripción!", Notification.Type.ERROR_MESSAGE);
            descripcionTxt.focus();
            return;
        }
        if(String.valueOf(proveedorCbx.getValue()).trim().isEmpty()) {
            Notification.show("Error, falta a quien ha sido asignada la tarea!", Notification.Type.ERROR_MESSAGE);
            proveedorCbx.focus();
            return;
        }

        String queryString;
        
        if(idTareaTxt.getValue().compareTo("0") == 0) {
            queryString =  "INSERT INTO tarea (IdProyecto, Descripcion, FechaInicio, FechaFin, DiasDuracion, ";
            queryString += " IdProveedor, IdEmpresa, IdPredecesores, IdSucesores, Nivel, NivelCodigo)";
            queryString += " VALUES (";
            queryString += "  " + ((SopdiUI) mainUI).sessionInformation.getStrProjectId();
            queryString += ",'"  + descripcionTxt.getValue()      + "'";
            queryString += ",'" + new Utileria().getFechaYYYYMMDD_1(startDt.getValue()) + "'";
            queryString += ",'" + new Utileria().getFechaYYYYMMDD_1(endDt.getValue()) + "'";
            queryString += ",'" + String.valueOf(proveedorCbx.getValue()) + "'";
            queryString += ",'" + String.valueOf(prioridadCbx.getValue()) + "'";
            queryString += ", " + ((SopdiUI) mainUI).sessionInformation.getStrCompanyId();
            queryString += ",'" + predecesoresTxt.getValue() + "'";
            queryString += ",'" + sucesoresTxt.getValue() + "'";
            queryString += ","  + String.valueOf(nivelCbx.getValue());
            queryString += ",'" + nivelCodigoTxt.getValue() + "'";
            queryString += ")";
        }
        else {
            queryString =  "UPDATE tarea SET ";
            queryString += " Descripcion = '" + descripcionTxt.getValue()         + "'";
            queryString += ",FechaInicio = '" + new Utileria().getFechaYYYYMMDD_1(startDt.getValue()) + "'";
            queryString += ",FechaFin    = '" + new Utileria().getFechaYYYYMMDD_1(endDt.getValue()) + "'";
            queryString += ",DiasDuracion=  " + diasDuracionTxt.getValue() + "'";
            queryString += ",IdProveedor = '" + String.valueOf(proveedorCbx.getValue()) + "'";
            queryString += ",IdPredecesores = '" + predecesoresTxt.getValue() + "'";
            queryString += ",IdSucesores    = '" + sucesoresTxt.getValue() + "'";
            queryString += ",Nivel = "  + String.valueOf(nivelCbx.getValue());
            queryString += ",NivelCodigo = '" + nivelCodigoTxt.getValue() + "'";
            queryString += " WHERE IdTarea  = " + idTareaTxt.getValue();
        }

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();            
            stQuery.executeUpdate(queryString);
               
            Notification.show("OPERACION EXITOSA!", Notification.Type.HUMANIZED_MESSAGE);
        }
        catch(Exception ex)
        {
            Notification.show("Error al actualizar tarea : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
        
        close();
    }    
    
    void cambiarFechas(int diasDuracion) {
                
        if(diasDuracion == 0) {
            diasDuracionTxt.setValue(String.valueOf((endDt.getValue().getTime() - startDt.getValue().getTime())/MILLSECS_PER_DAY));
        }
        else {
            Calendar c = Calendar.getInstance();
            c.setTime(startDt.getValue());
            c.add(Calendar.DATE, diasDuracion); 
            endDt.setValue(c.getTime());
        }
    }
}