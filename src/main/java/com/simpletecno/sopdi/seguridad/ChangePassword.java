/*
 * Ventana para Cambiar Clave.
 * ...
 * @author Jose Aguirre
*/

package com.simpletecno.sopdi.seguridad;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.data.validator.AbstractValidator;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("unchecked")
public class ChangePassword extends Window implements Button.ClickListener
{      
    private static Statement stQuery;
    private static ResultSet rsRecords;

    Button acceptBtn;
    Button exitBtn;
    
    TextField txtUserName;
    public PasswordField txtPasswordActual;
    PasswordField txtPasswordNueva;   
    PasswordField txtPasswordNueva1;
    
    String userId;
    String lastLogin;
   
    public ChangePassword(String userId, String userName, String lastLogin) {

        setSizeFull();
        setCaption("Cambio de contraseña para : " + userName);
        this.userId=userId;
        this.lastLogin = lastLogin;

        acceptBtn    = new Button("Aceptar");
        acceptBtn.setIcon(FontAwesome.CHECK);
        acceptBtn.setWidth(120,Sizeable.UNITS_PIXELS);
        acceptBtn.addListener((Button.ClickListener) this);
//        acceptBtn.setClickShortcut(KeyCode.ENTER);

        exitBtn    = new Button("Salir");
        exitBtn.setIcon(FontAwesome.SIGN_OUT);
        exitBtn.setWidth(120,Sizeable.UNITS_PIXELS);
        exitBtn.addListener((Button.ClickListener) this);
//        exitBtn.setClickShortcut(KeyCode.ESCAPE);

        txtUserName  = new TextField("Usuario");
        txtUserName.setImmediate(true);
        txtUserName.setValue(userName);
        txtUserName.setReadOnly(true);
        txtUserName.setIcon(FontAwesome.USER);
        txtUserName.setWidth("180px");
        txtPasswordActual  = new PasswordField("Contraseña Actual");
        txtPasswordActual.setImmediate(true);
        txtPasswordActual.setWidth("180px");
        txtPasswordActual.setMaxLength(64);
        txtPasswordNueva  = new PasswordField("Nueva contraseña");
        txtPasswordNueva.setIcon(FontAwesome.LOCK);
        txtPasswordNueva.setImmediate(true);
        txtPasswordNueva.setWidth("180px");
        txtPasswordNueva.addValidator(new PasswordValidator());
        txtPasswordNueva.setMaxLength(64);
        txtPasswordNueva1  = new PasswordField("Confirme contraseña");
        txtPasswordNueva1.setImmediate(true);
        txtPasswordNueva1.setIcon(FontAwesome.LOCK);
        txtPasswordNueva1.setWidth("180px");
        txtPasswordNueva1.addValidator(new PasswordValidator());
        txtPasswordNueva1.setMaxLength(64);
        
//        txtPasswordActual.focus();
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        
        layout.addComponent(txtUserName);
        layout.setComponentAlignment(txtUserName, Alignment.MIDDLE_CENTER);
        layout.addComponent(txtPasswordActual);
        layout.setComponentAlignment(txtPasswordActual, Alignment.MIDDLE_CENTER);
        layout.addComponent(txtPasswordNueva);
        layout.setComponentAlignment(txtPasswordNueva, Alignment.MIDDLE_CENTER);
        layout.addComponent(txtPasswordNueva1);
        layout.setComponentAlignment(txtPasswordNueva1, Alignment.MIDDLE_CENTER);
        
        setWidth("360px");
        setHeight("400px");
        
        HorizontalLayout footer = new HorizontalLayout();

        footer.setSpacing(true);
        footer.addComponent(exitBtn);
        footer.setComponentAlignment(exitBtn, Alignment.BOTTOM_LEFT);
        footer.addComponent(acceptBtn);
        footer.setComponentAlignment(acceptBtn, Alignment.BOTTOM_RIGHT);
        layout.addComponent(footer);
        layout.setComponentAlignment(footer, Alignment.BOTTOM_CENTER);

        setContent(layout);
        setPositionX(510);
        setPositionY(400);
        setModal(true);
    }

// Validator for validating the passwords
    private static final class PasswordValidator extends
            AbstractValidator<String> {

        public PasswordValidator() {
            super("Contraseña no es válida, debe ser por lo menos 8 caracteres y debe contener por lo menos un número");
        }

        @Override
        protected boolean isValidValue(String value) {
            //
            // Password must be at least 8 characters long and contain at least
            // one number
            //
            if (value != null
                    && (value.length() < 8 || !value.matches(".*\\d.*"))) {
                return false;
            }
            return true;
        }

        @Override
        public Class<String> getType() {
            return String.class;
        }
    }
    
    @Override
    public void buttonClick(ClickEvent event) {
        final Button source = event.getButton();
        
        if(source == acceptBtn) {
            
            String queryString = "Select * From usuario ";
            queryString += " Where IdUsuario = " + userId;
            if(((SopdiUI)UI.getCurrent()).databaseProvider.getUsedDBDataSource().equals("MYSQL")) {
                queryString += " And   Clave    = Sha1('" + txtPasswordActual.getValue() + "')";
            }
            else {
                queryString += " And   Clave    = SUBSTRING(master.dbo.fn_varbintohexstr(HASHBYTES('SHA1', '" + txtPasswordActual.getValue() + "')),3,40)";
            }

            try {
                stQuery  = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                
                rsRecords = stQuery.executeQuery(queryString);

                if(rsRecords.next()) { //  encontrado
                    
                    if(String.valueOf(txtPasswordNueva.getValue()).compareTo(String.valueOf(txtPasswordNueva1.getValue())) != 0) {
                        Notification.show("Las contraseñas no coinciden", Notification.Type.ERROR_MESSAGE);
                        txtPasswordNueva1.setValue("");
                        txtPasswordNueva1.focus();
                        return;
                    }
                    queryString = "Update usuario Set ";

                    if(((SopdiUI) UI.getCurrent()).databaseProvider.getUsedDBDataSource().equals("MYSQL")) {            
                        queryString += " Clave    = Sha1('" + txtPasswordNueva.getValue() + "')";
                    }
                    else {
                        queryString += " Clave    = SUBSTRING(master.dbo.fn_varbintohexstr(HASHBYTES('SHA1', '" + txtPasswordNueva.getValue() + "')),3,40)";
                    }
                    if(lastLogin == null) {
                        queryString += ", UltimoLogin = current_timestamp";
                    }
                    queryString += " Where IdUsuario = " + userId;
                    
System.out.println("query cambio de clave="+queryString);

                    stQuery.executeUpdate(queryString);
                    
                    Notification.show("Contraseña ACTUALIZADA CON EXITO!", Notification.Type.HUMANIZED_MESSAGE);

                    if(lastLogin != null) {
                        ((SopdiUI) UI.getCurrent()).logOff();
                    }
                    else {
                        close();
                    }
                    
                }
                else {
                    Notification.show("Usuario incorrecto o contraseña incorrecta", Notification.Type.ERROR_MESSAGE);
                    txtPasswordActual.setValue("");
                    txtPasswordNueva.setValue("");
                    txtPasswordNueva1.setValue("");
                    txtUserName.focus();
                }
            }
            catch(Exception ex1) {
                Logger.getLogger(ChangePassword.class.getName()).log(Level.SEVERE, ex1.getMessage() );
                Notification.show("ERROR FATAL DEL SISTEMA", Notification.Type.ERROR_MESSAGE); 
                System.out.println("ERROR AL INTENTAR BUSCAR USUARIO : " + ex1.getMessage());
            }
            
        }

        if(source == exitBtn) {
            this.close();
        }
    }

}
