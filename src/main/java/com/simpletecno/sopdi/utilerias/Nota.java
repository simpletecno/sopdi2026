/*
 * Ventana para reigtro de Notas.
 * ...
 * @author Jose Aguirre
*/

package com.simpletecno.sopdi.utilerias;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("unchecked")
//para solicitar a usuario una nota, para cualquier tabla
public class Nota extends Window
{      
    Button acceptBtn;
    Button exitBtn;

    GridLayout gridLayout;
    TextField casoIdTxt;
    TextField nombreTxt;
    PopupDateField fechaDt;
    TextField seguimientoTxt;    
    
    UI mainUI;
    
    private VerticalLayout mainLayout = new VerticalLayout();
    
    Statement stQuery = null;
    PreparedStatement stPreparedQuery = null;    
    ResultSet rsRecords = null;

    String tabla, identificador, tipoProducto, casoId, nombre;
    
    public Nota(UI mainUI, String tabla, String identificador, String tipoProducto, String casoId, String nombre) {

        this.mainUI = mainUI;
        this.tabla = tabla;
        this.identificador = identificador;
        this.tipoProducto = tipoProducto;
        this.casoId = casoId;
        this.nombre = nombre;
                
        setSizeFull();        
        setCaption("SCG -- Seguimiento o Nota para [" + casoId + "] "  + nombre);
        
        gridLayout = new GridLayout(5,4);
        gridLayout.setMargin(true);
        gridLayout.setSpacing(true);

        casoIdTxt         = new TextField("Id:");
        casoIdTxt.setValue(String.valueOf(casoId));
        casoIdTxt.setReadOnly(true);
        casoIdTxt.setWidth("5em");

        nombreTxt      = new TextField("Nombre:");
        nombreTxt.setWidth("300px");
        nombreTxt.setValue(nombre);
        nombreTxt.setReadOnly(true);
        
        seguimientoTxt = new TextField("Texto de la Nota o Seguimiento:");
        seguimientoTxt.setWidth("800px");
        seguimientoTxt.setDescription("<<ingrese aqui el texto>>");
        seguimientoTxt.setInputPrompt("<<ingrese aqui el texto>>");
        seguimientoTxt.addStyleName("v-textfield-uppercase");
        
        gridLayout.addComponent(casoIdTxt,0,0);
        gridLayout.addComponent(nombreTxt,1,0,2,0);
        gridLayout.addComponent(seguimientoTxt, 0,3,4,3);

        seguimientoTxt.focus();

        mainLayout.addComponent(gridLayout);
        mainLayout.setComponentAlignment(gridLayout, Alignment.TOP_CENTER);
        
        acceptBtn    = new Button("Aceptar");
//        acceptBtn.setIcon(new ThemeResource("img/ok.png"));
//        acceptBtn.setWidth(100,Sizeable.UNITS_PIXELS);
        acceptBtn.setClickShortcut(KeyCode.ENTER);
        acceptBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick ( Button.ClickEvent event )
            {
                validarYGuardar();
            }
        });
        
        exitBtn    = new Button("Salir");
//        exitBtn.setIcon(new ThemeResource("img/mlogout.bmp"));
//        exitBtn.setWidth(100,Sizeable.UNITS_PIXELS);

        exitBtn.setClickShortcut(KeyCode.ESCAPE);
        exitBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick ( Button.ClickEvent event )
            {
                close();
            }
        });

        HorizontalLayout footer = new HorizontalLayout();

        footer.setSpacing(true);
//        footer.setMargin(true);
        footer.addComponent(exitBtn);
        footer.setComponentAlignment(exitBtn, Alignment.BOTTOM_LEFT);
        footer.addComponent(acceptBtn);
        footer.setComponentAlignment(acceptBtn, Alignment.BOTTOM_RIGHT);
        mainLayout.addComponent(footer);
        mainLayout.setComponentAlignment(footer, Alignment.BOTTOM_CENTER);

        setContent(mainLayout);
 
        setWidth("850px");
        setHeight("280px");
        
        setPositionX(200);
        setPositionY(220);
                
        setModal(true);        
    }
        
    public void validarYGuardar() {
        if(!datosValidos())
            return;

        try {
            String queryString;
            
            queryString = "Insert Into " + tabla + " (" + identificador + ",FechaYHora, Nota, Usuario) ";
            queryString += "     Values (";
            queryString += "  " + casoId;
            queryString += ", current_timestamp";
            queryString += ",'" + seguimientoTxt.getValue().toUpperCase() + "'";
            queryString += ",'" + ((SopdiUI) mainUI).sessionInformation.getStrUserName() + "'";
            queryString += ")";
            
//System.out.println("queryString = " + queryString);

            stPreparedQuery  = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().prepareStatement(queryString, Statement.RETURN_GENERATED_KEYS);
            stPreparedQuery.executeUpdate();
            rsRecords = stPreparedQuery.getGeneratedKeys();

            close();
        }
        catch(Exception ex1) {
            Logger.getLogger(Nota.class.getName()).log(Level.SEVERE, ex1.getMessage() );
            Notification.show("ERROR FATAL DEL SISTEMA");                
            System.out.println("ERROR AL INTENTAR INSERTAR SEGUIMIENTO O NOTA : " + ex1.getMessage());
            ex1.printStackTrace();
        }
                    
    }
    
    private boolean datosValidos(){
        if(String.valueOf(seguimientoTxt.getValue()).length() == 0) {
            Notification.show("No ha ingresado la nota o seguimiento.", Notification.Type.ERROR_MESSAGE);
            seguimientoTxt.focus();
            return false;
        }
        return true;
    }    
}
