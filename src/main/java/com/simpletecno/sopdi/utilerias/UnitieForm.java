/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.utilerias;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.event.ShortcutAction;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
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
import org.vaadin.ui.NumberField;
import java.io.ByteArrayInputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joseaguirre
 */
public class UnitieForm extends Window {
    
    VerticalLayout mainLayout;
    HorizontalLayout formsLayout;
    MarginInfo  marginInfo;
    
    public  int idUnitie = 0;
    
    FormLayout unitieForm;
    FormLayout unitieForm1;
    
    Button saveBtn;
    Button exitBtn;
    
    Statement stQuery = null;
    static PreparedStatement stPreparedQuery;
    ResultSet rsRecords = null;
        
    TextField idUnitieTxt;
    TextField codigoTxt;
    TextField descripcionTxt;
    TextField ubicacionTxt;
    TextField direccionCompletaTxt;
    ComboBox unidadMedidaCbx;
    NumberField medidaCuadradaTxt;
    NumberField medidaFrenteTxt;
    NumberField medidaFondoTxt;
    NumberField medidaSinValorTxt;
    NumberField medidaInclinacionPosteriorTxt;
    NumberField medidaInclinacionLateralTxt;
    NumberField precioTxt;
    NumberField premiumTxt;
    ComboBox estatusCbx;

    UI mainUI;
    
    public UnitieForm() {
        this.mainUI = UI.getCurrent();
       
        setResponsive(true);
        setCaption("Ficha de la unidad de base");
        setModal(true);
        
        marginInfo = new MarginInfo(true,true,false,true);
                
        mainLayout = new VerticalLayout();     
        mainLayout.setSpacing(true);
        
        unitieForm  = new FormLayout();
        unitieForm.setMargin(marginInfo);
        unitieForm.setSpacing(false);
        unitieForm.addStyleName(ValoTheme.FORMLAYOUT_LIGHT);
        unitieForm1  = new FormLayout();
        unitieForm1.setMargin(marginInfo);
        unitieForm1.setSpacing(false);
        unitieForm1.addStyleName(ValoTheme.FORMLAYOUT_LIGHT);

        idUnitieTxt = new TextField("Id Unidad :");
//        idUnitieTxt.setWidth("8em");
        idUnitieTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);
        idUnitieTxt.setReadOnly(true);

        codigoTxt = new TextField("Código :");
//        idUnitieTxt.setWidth("8em");
        codigoTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);
//        codigoTxt.setReadOnly(true);

        descripcionTxt = new TextField("Descripción :");
//        descripcionTxt.setWidth("10em");
        descripcionTxt.setRequired(true);
        descripcionTxt.setRequiredError("POR FAVOR INGRESE LA DESCRIPCION DE LA UNIDAD BASE");

        ubicacionTxt = new TextField("Ubicación :");
//        ubicacionTxt.setWidth("15em");
//        ubicacionTxt.setRequired(true);
//        ubicacionTxt.setRequiredError("POR FAVOR INGRESE LA UBICACION DE LA UNIDAD BASE");

        direccionCompletaTxt = new TextField("Dirección completa :");
//        direccionCompletaTxt.setWidth("15em");

        unidadMedidaCbx = new ComboBox("Unidad de medida :");
        unidadMedidaCbx.addItem("VARA CUADRADA");
        unidadMedidaCbx.addItem("METRO CUADRADO");
        unidadMedidaCbx.select("VARA CUADRADA");
        unidadMedidaCbx.setNewItemsAllowed(false);
        unidadMedidaCbx.setInvalidAllowed(false);
        unidadMedidaCbx.setTextInputAllowed(false);
        unidadMedidaCbx.setImmediate(true);
        unidadMedidaCbx.setReadOnly(true);

        medidaCuadradaTxt = new NumberField("Medida cuadrada :");
//        medidaCuadradaTxt.setWidth("10em");
        medidaCuadradaTxt.setDecimalPrecision(2);
        medidaCuadradaTxt.setDecimalSeparator('.');
        medidaCuadradaTxt.setValue(0d);
        medidaCuadradaTxt.setGroupingUsed(true);
        medidaCuadradaTxt.setGroupingSeparator(',');
        medidaCuadradaTxt.setImmediate(true);
        medidaCuadradaTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);

        medidaFrenteTxt = new NumberField("Medida Frente :");
//        medidaFrenteTxt.setWidth("10em");
        medidaFrenteTxt.setDecimalPrecision(0);
        medidaFrenteTxt.setDecimalSeparator('.');
        medidaFrenteTxt.setValue(20d);
        medidaFrenteTxt.setGroupingUsed(false);
        medidaFrenteTxt.setGroupingSeparator(',');
        medidaFrenteTxt.setImmediate(true);
        medidaFrenteTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        
        medidaFondoTxt = new NumberField("Medida Fondo :");
//        medidaFrenteTxt.setWidth("10em");
        medidaFondoTxt.setDecimalPrecision(0);
        medidaFondoTxt.setDecimalSeparator('.');
        medidaFondoTxt.setValue(20d);
        medidaFondoTxt.setGroupingUsed(false);
        medidaFondoTxt.setGroupingSeparator(',');
        medidaFondoTxt.setImmediate(true);
        medidaFondoTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);

        medidaSinValorTxt = new NumberField("Medida sin valor :");
        medidaSinValorTxt.setDecimalPrecision(2);
        medidaSinValorTxt.setDecimalSeparator('.');
        medidaSinValorTxt.setValue(0d);
        medidaSinValorTxt.setGroupingUsed(true);
        medidaSinValorTxt.setGroupingSeparator(',');
        medidaSinValorTxt.setImmediate(true);
        medidaSinValorTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);

        medidaInclinacionPosteriorTxt = new NumberField("Medida inclinación posterior :");
        medidaInclinacionPosteriorTxt.setDecimalPrecision(2);
        medidaInclinacionPosteriorTxt.setDecimalSeparator('.');
        medidaInclinacionPosteriorTxt.setValue(0d);
        medidaInclinacionPosteriorTxt.setGroupingUsed(true);
        medidaInclinacionPosteriorTxt.setGroupingSeparator(',');
        medidaInclinacionPosteriorTxt.setImmediate(true);
        medidaInclinacionPosteriorTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);

        medidaInclinacionLateralTxt = new NumberField("Medida inclinación lateral :");
        medidaInclinacionLateralTxt.setDecimalPrecision(2);
        medidaInclinacionLateralTxt.setDecimalSeparator('.');
        medidaInclinacionLateralTxt.setValue(0d);
        medidaInclinacionLateralTxt.setGroupingUsed(true);
        medidaInclinacionLateralTxt.setGroupingSeparator(',');
        medidaInclinacionLateralTxt.setImmediate(true);
        medidaInclinacionLateralTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);

        precioTxt = new NumberField("Precio :");
        precioTxt.setDecimalPrecision(2);
        precioTxt.setDecimalSeparator('.');
        precioTxt.setValue(1d);
        precioTxt.setGroupingUsed(true);
        precioTxt.setGroupingSeparator(',');
        precioTxt.setImmediate(true);
        precioTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);

        premiumTxt = new NumberField("Premium :");
        premiumTxt.setDecimalPrecision(2);
        premiumTxt.setDecimalSeparator('.');
        premiumTxt.setValue(1d);
        premiumTxt.setGroupingUsed(true);
        premiumTxt.setGroupingSeparator(',');
        premiumTxt.setImmediate(true);
        premiumTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);

        estatusCbx = new ComboBox("Estatus :");
        estatusCbx.setNewItemsAllowed(false);
        estatusCbx.setInvalidAllowed(false);
        estatusCbx.addItem("ACTIVO");
        estatusCbx.addItem("INACTIVO");
        estatusCbx.select("ACTIVO");

        unitieForm.addComponent(idUnitieTxt);
        unitieForm.addComponent(codigoTxt);
        unitieForm.addComponent(descripcionTxt);
        unitieForm.addComponent(unidadMedidaCbx);
        unitieForm.addComponent(medidaCuadradaTxt);
        unitieForm.addComponent(medidaFrenteTxt);
        unitieForm.addComponent(medidaFondoTxt);
        unitieForm1.addComponent(medidaSinValorTxt);
        unitieForm1.addComponent(medidaInclinacionPosteriorTxt);
        unitieForm1.addComponent(medidaInclinacionLateralTxt);
        unitieForm1.addComponent(precioTxt);
        unitieForm1.addComponent(premiumTxt);
        unitieForm1.addComponent(ubicacionTxt);
        unitieForm1.addComponent(direccionCompletaTxt);
        unitieForm1.addComponent(estatusCbx);
        
        formsLayout = new HorizontalLayout();
        formsLayout.setMargin(false);
        formsLayout.setSpacing(false);
        
        formsLayout.addComponent(unitieForm);
        formsLayout.addComponent(unitieForm1);
        
        mainLayout.addComponent(formsLayout);
        
        saveBtn = new Button("Guardar");
        saveBtn.setIcon(FontAwesome.SAVE); 
        saveBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        saveBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick ( Button.ClickEvent event )
            {
                saveCliente();
            }
        });
                
        exitBtn    = new Button("Salir");
        exitBtn.setIcon(FontAwesome.SIGN_OUT);
        exitBtn.setClickShortcut(ShortcutAction.KeyCode.ESCAPE);
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
        buttonsLayout.addComponent(exitBtn);
        buttonsLayout.setComponentAlignment(exitBtn, Alignment.BOTTOM_LEFT);
        buttonsLayout.addComponent(saveBtn);
        buttonsLayout.setComponentAlignment(saveBtn, Alignment.BOTTOM_CENTER);
        
        mainLayout.addComponent(buttonsLayout);
        mainLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);
        
        setContent(mainLayout);
    }    

    public void fillData() {
        String queryString = "";
        
        queryString =  "Select Uni.*, Pro.UnidadMedida, Pro.Moneda ";
        queryString += " From  unidad_base Uni";
        queryString += " Inner Join proyecto Pro On Pro.IdProyecto = Uni.IdProyecto";
        queryString += " Where Uni.IdUnidadBase = " + idUnitieTxt.getValue();

//System.out.println("\n\n"+queryString);

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery (queryString);

            if(rsRecords.next()) { //  encontrado

                setCaption("Ficha de la unidad de base : " + rsRecords.getString("Descripcion"));
                
                codigoTxt.setValue(rsRecords.getString("Codigo"));
                descripcionTxt.setValue(rsRecords.getString("Descripcion"));
                ubicacionTxt.setValue(rsRecords.getString("UbicacionGeografica"));
                direccionCompletaTxt.setValue(rsRecords.getString("DireccionCompleta"));
                unidadMedidaCbx.setReadOnly(false);
                unidadMedidaCbx.select(rsRecords.getString("UnidadMedida"));
                unidadMedidaCbx.setReadOnly(true);
                medidaCuadradaTxt.setValue(rsRecords.getDouble("MedidaCuadrada"));
                medidaFrenteTxt.setValue(rsRecords.getDouble("MedidaFrente"));
                medidaFondoTxt.setValue(rsRecords.getDouble("MedidaFondo"));
                medidaSinValorTxt.setValue(rsRecords.getDouble("MedidaSinValor"));
                medidaInclinacionPosteriorTxt.setValue(rsRecords.getDouble("MedidaInclinacionPosterior"));
                medidaInclinacionLateralTxt.setValue(rsRecords.getDouble("MedidaInclinacionLateral"));
                precioTxt.setCaption("Precio " + rsRecords.getString("Moneda") + " : ");
                precioTxt.setValue(rsRecords.getDouble("Precio"));
                premiumTxt.setCaption("Premium " + rsRecords.getString("Moneda") + " : ");
                premiumTxt.setValue(rsRecords.getDouble("Premium"));
                estatusCbx.select(rsRecords.getString("Estatus"));
            }                
        } 
        catch (Exception ex) {
            Logger.getLogger(UnitieForm.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de unidades de venta : " + ex.getMessage());
            Notification.show("Error al intentar leer registros de unidades de venta..!", Notification.Type.ERROR_MESSAGE);
        } 
    }

    private void saveCliente() {
        
        if(descripcionTxt.getValue().trim().isEmpty()) {
            Notification.show("Error, falta la descripción de la unidad de venta!", Notification.Type.ERROR_MESSAGE);
            descripcionTxt.focus();
            return;
        }
        if(ubicacionTxt.getValue().trim().isEmpty()) {
            Notification.show("Error, falta la ubicación!", Notification.Type.ERROR_MESSAGE);
            ubicacionTxt.focus();
            return;
        }
        if(direccionCompletaTxt.getValue().trim().isEmpty()) {
            Notification.show("Error, falta la dirección completa!", Notification.Type.ERROR_MESSAGE);
            direccionCompletaTxt.focus();
            return;
        }

        ByteArrayInputStream inputStream1 = null;
        
        String queryString;
        
        if(idUnitieTxt.getValue().compareTo("0") == 0) {
            queryString =  "Insert Into unidad_base (IdProyecto, Descripcion, Codigo, MedidaCuadrada, ";
            queryString += " MedidaFrente, MedidaFondo, MedidaSinValor, MedidaInclinacionPosterior, ";
            queryString += " MedidaInclinacionLateral, DireccionCompleta, UbicacionGeografica, ";
            queryString += " Precio, Premium)";
            queryString += " Values (";
            queryString += " "  + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrProjectId();
            queryString += ",'" + descripcionTxt.getValue()      + "'";
            queryString += ",'" + codigoTxt.getValue()   + "'";
            queryString += ", " + medidaCuadradaTxt.getDoubleValueDoNotThrow();
            queryString += ", " + medidaFrenteTxt.getDoubleValueDoNotThrow();
            queryString += ", " + medidaFondoTxt.getDoubleValueDoNotThrow();
            queryString += ", " + medidaSinValorTxt.getDoubleValueDoNotThrow();
            queryString += ", " + medidaInclinacionPosteriorTxt.getDoubleValueDoNotThrow();
            queryString += ", " + medidaInclinacionLateralTxt.getDoubleValueDoNotThrow();
            queryString += ",'" + direccionCompletaTxt.getValue()   + "'";
            queryString += ",'" + ubicacionTxt.getValue()   + "'";
            queryString += ", " + precioTxt.getDoubleValueDoNotThrow();
            queryString += ", " + premiumTxt.getDoubleValueDoNotThrow();
            queryString += ")";
        }
        else {
            queryString =  "Update unidad_base Set ";
            queryString += " Descripcion = '" + descripcionTxt.getValue()      + "'";
            queryString += ",Codigo = '" + codigoTxt.getValue()   + "'";
            queryString += ",MedidaCuadrada = " + medidaCuadradaTxt.getDoubleValueDoNotThrow();
            queryString += ",MedidaFrente = " + medidaFrenteTxt.getDoubleValueDoNotThrow();
            queryString += ",MedidaFondo = " + medidaFondoTxt.getDoubleValueDoNotThrow();
            queryString += ",MedidaSinValor = " + medidaSinValorTxt.getDoubleValueDoNotThrow();
            queryString += ",MedidaInclinacionPosterior = " + medidaInclinacionPosteriorTxt.getDoubleValueDoNotThrow();
            queryString += ",MedidaInclinacionLateral = " + medidaInclinacionLateralTxt.getDoubleValueDoNotThrow();
            queryString += ",DireccionCompleta = '" + direccionCompletaTxt.getValue()   + "'";
            queryString += ",UbicacionGeografica = '" + ubicacionTxt.getValue()   + "'";
            queryString += ",Precio = " + precioTxt.getDoubleValueDoNotThrow();
            queryString += ",Premium = " + premiumTxt.getDoubleValueDoNotThrow();
            queryString += " Where IdUnidadBase = " + idUnitieTxt.getValue();
        }
//System.out.println("queryString="+queryString);
        try {
            stQuery  = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);                
               
            Notification.show("OPERACION EXITOSA!", Notification.Type.HUMANIZED_MESSAGE);

            if(mainUI.getNavigator().getCurrentView().getClass().getSimpleName().compareTo("UnitiesView") == 0) {
                ((Unities)(mainUI.getNavigator().getCurrentView())).fillUnitiesTable();
            }            
        }
        catch(Exception ex)
        {
            Notification.show("Error al actualizar unidad base venta : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
        close();
    }    
}