
package com.simpletecno.sopdi.utilerias;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.ui.NumberField;

import java.sql.ResultSet;
import java.sql.Statement;

//tipo de cambio del Banco de Guatemala
public class TasaCambioForm extends Window {
    
    VerticalLayout mainLayout;
    
    NumberField tasaCambioTxt;
    Button grabarBtn;

    UI mainUI;
    Statement stQuery;
    ResultSet rsRecords;
    String queryString;
    
    public TasaCambioForm(String tipoCambioDolar){
        
        this.mainUI = UI.getCurrent();
        setResponsive(true);
        setWidth("45%");
        setHeight("40%");
               
        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setResponsive(true);
                
        HorizontalLayout layoutTitle = new HorizontalLayout();
        layoutTitle.setSpacing(true);
        layoutTitle.setMargin(true);
        layoutTitle.setWidth("100%");
        
        HorizontalLayout layoutToken = new HorizontalLayout();
        layoutToken.setSpacing(true);
        //layoutToken.setMargin(true);
                
        Label titleLbl = new Label("TASA DE CAMBIO DE HOY! ");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h2_custom");                       
        
        tasaCambioTxt = new NumberField();
        tasaCambioTxt.setStyleName(ValoTheme.TEXTFIELD_HUGE);
        tasaCambioTxt.setDecimalPrecision(5);
        tasaCambioTxt.setMinimumFractionDigits(2);
        tasaCambioTxt.setValue(Double.valueOf(tipoCambioDolar));
        if(!tipoCambioDolar.trim().isEmpty() && !tipoCambioDolar.equals("0.00")) {
            tasaCambioTxt.setReadOnly(true);
        }

        grabarBtn = new Button("Guardar");
        grabarBtn.setIcon(FontAwesome.SAVE);
        grabarBtn.setStyleName(ValoTheme.BUTTON_PRIMARY);
        grabarBtn.setStyleName(ValoTheme.BUTTON_HUGE);
        grabarBtn.addClickListener((event) -> {
           insertarTasa();
        });
        if(!tipoCambioDolar.trim().isEmpty() && !tipoCambioDolar.equals("0.00")) {
            grabarBtn.setEnabled(false);
        }

        layoutTitle.addComponent(titleLbl);
        layoutTitle.setComponentAlignment(titleLbl, Alignment.TOP_CENTER);              
        
        layoutToken.addComponents(tasaCambioTxt,grabarBtn);
        layoutToken.setComponentAlignment(grabarBtn, Alignment.BOTTOM_CENTER);
                
        mainLayout.addComponent(layoutTitle);
        mainLayout.addComponent(layoutToken);
        
        mainLayout.setComponentAlignment(layoutToken, Alignment.MIDDLE_CENTER);
        
        setContent(mainLayout);
    }
    
    public void insertarTasa(){
        
        queryString = " INSERT INTO contabilidad_tasa_cambio (Fecha, Tasa, CreadoUsuario, CreadoFechaYHora) ";
        queryString += " VALUES ( ";
        queryString += " current_date()";
        queryString += "," + tasaCambioTxt.getDoubleValueDoNotThrow();
        queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
        queryString += ",current_timestamp";
        queryString += ")";          
                               
        try {            
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);       
                
            Notification.show("TASA DE HOY GUARDADA EXITOSAMENTE!", Notification.Type.HUMANIZED_MESSAGE);
            close();

            ((SopdiUI) UI.getCurrent()).tipoCambioDolar = String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
            ((SopdiUI) UI.getCurrent()).sessionInformation.setFltlExchangeRate((float)tasaCambioTxt.getDoubleValueDoNotThrow());

        } catch (Exception e) {
            System.out.println("Error " +e);            
            e.printStackTrace();
            Notification.show("ERROR AL INSERTAR EN BASE DE DATOS : " + e.getMessage(), Notification.Type.ERROR_MESSAGE);
        }        
    }
}
