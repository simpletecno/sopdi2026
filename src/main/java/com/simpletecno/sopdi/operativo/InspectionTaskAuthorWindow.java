/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.operativo;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Responsive;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 *
 * @author JAguirre
 */
@SuppressWarnings("serial")
public class InspectionTaskAuthorWindow extends Window {
    
    public Statement stQuery = null;
    public ResultSet rsRecords = null;
            
    MarginInfo  marginInfo;
    VerticalLayout mainLayout = new VerticalLayout();
        
    PasswordField passwordTxt = new PasswordField("Password :");
    TextField referenciaTxt = new TextField("Refencia :");
    Button saveBtn = new Button("Autorizar");
    Button exitBtn = new Button("Salir");
                        
    UI mainUI;
       
    boolean autorizar;
    String tareaId;
    String codigoTarea;
    String descripcionTarea;
    
    public InspectionTaskAuthorWindow(
            boolean autorizar,
            String tareaId,
            String codigoTarea,
            String descripcionTarea) {
        this.autorizar = autorizar;
        this.tareaId = tareaId;
        this.codigoTarea = codigoTarea;
        this.descripcionTarea = descripcionTarea;
        
        this.mainUI = UI.getCurrent();    
                
        Responsive.makeResponsive(this);
        
        setContent(mainLayout);
        
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);
        mainLayout.setWidth("100%");
        
        marginInfo = new MarginInfo(true,true,false,true); 

        Label titleLbl = new Label((autorizar ? "Autorizar " : "Rechazar ") + " tarea : " + codigoTarea + " " + descripcionTarea);
        titleLbl.addStyleName(ValoTheme.LABEL_H3);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h3_custom");
        
        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setWidth("100%");
        
        titleLayout.addComponents(titleLbl);
        titleLayout.setComponentAlignment(titleLbl,  Alignment.TOP_LEFT);
        
        mainLayout.addComponent(titleLayout);
        mainLayout.setComponentAlignment(titleLayout, Alignment.TOP_CENTER);
        
        createEditionLayout();
        
        setWidth("50%");
        setHeight("40%");

    }
            
    private void createEditionLayout() {
        
        passwordTxt.setIcon(FontAwesome.LOCK);
        passwordTxt.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
        referenciaTxt.setIcon(FontAwesome.COMMENTING_O);
        referenciaTxt.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);

        passwordTxt.setWidth("80%");
        referenciaTxt.setWidth("80%");
        
        mainLayout.addComponent(passwordTxt);
        mainLayout.addComponent(referenciaTxt);
        
        mainLayout.setComponentAlignment(passwordTxt, Alignment.TOP_CENTER);
        mainLayout.setComponentAlignment(referenciaTxt, Alignment.TOP_CENTER);

        if(!autorizar) {
            saveBtn.setCaption("Rechazar");
        }
        saveBtn.setIcon(FontAwesome.SAVE);
        saveBtn.setWidth(130,Sizeable.UNITS_PIXELS);
//        saveBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        saveBtn.setDescription("Guardar los cambios");
        saveBtn.setImmediate(true);
        saveBtn.addListener ( new Button.ClickListener()
        {
            @Override
            public void buttonClick ( Button.ClickEvent event )
            {
                saveInspeccionTarea();
            }
        });
        
        exitBtn.setIcon(FontAwesome.BAN);
//        exitBtn.setWidth(130,Sizeable.UNITS_PIXELS);
        exitBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        exitBtn.setDescription("Salir");
        exitBtn.setImmediate(true);
        exitBtn.addListener ( new Button.ClickListener()
        {
            @Override
            public void buttonClick ( Button.ClickEvent event )
            {
                close();
            }
        });
        
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setMargin(new MarginInfo(false,false,true,false));
        buttonsLayout.addComponent(exitBtn);
        buttonsLayout.setComponentAlignment(exitBtn, Alignment.BOTTOM_LEFT);
        buttonsLayout.addComponent(saveBtn);

        mainLayout.addComponent(buttonsLayout);
        mainLayout.setComponentAlignment(buttonsLayout, Alignment.TOP_CENTER);
    }
    
    private void saveInspeccionTarea() {
        
        if(passwordTxt.getValue().trim().isEmpty()) {
            Notification.show("Por favor, ingrese la clave para autorizar!", Notification.Type.ERROR_MESSAGE);
            passwordTxt.focus();
            return;
        }

        String queryString;
        
        queryString = "Select * ";
        queryString += " From proyecto";
        queryString += " Where IdProyecto = " + ((SopdiUI) mainUI).sessionInformation.getStrProjectId();
        queryString += " And ClaveParaAutorizar = sha1('" + passwordTxt.getValue() + "')";

System.out.println("queryString="+queryString);

        try {
            
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();            
            rsRecords = stQuery.executeQuery (queryString);

            if(!rsRecords.next()) {
                Notification.show("Clave para autorizar es incorrecta, intente nuevamente!", Notification.Type.WARNING_MESSAGE);
                return;
            }

            queryString =  "Update visita_inspeccion_tarea Set ";
            if(autorizar) {
                queryString += " AutorizadoPor = '" + ((SopdiUI) mainUI).sessionInformation.getStrUserId() + "'";
                queryString += ",AutorizadoFecha = current_date";
                queryString += ",AutorizadoReferencia = '" + referenciaTxt.getValue() + "'";
            }
            else {
                queryString += " RechazadoPor = '" + ((SopdiUI) mainUI).sessionInformation.getStrUserId() + "'";
                queryString += ",RechazadoFecha = current_date";
                queryString += ",RechazadoReferencia = '" + referenciaTxt.getValue() + "'";                
            }
            queryString += " Where IdVisitaInspeccionTarea  = " + tareaId;
        
System.out.println("queryString="+queryString);

            stQuery.executeUpdate(queryString);
               
            Notification.show("OPERACION EXITOSA!", Notification.Type.HUMANIZED_MESSAGE);

            close();
            
            ((InspectionsTaskTrackView)(mainUI.getNavigator().getCurrentView())).fillInspectionsTaskGrid();
            
        }
        catch(Exception ex)
        {
            Notification.show("Error al actualizar tarea : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }        
    }    
}
