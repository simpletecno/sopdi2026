/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.seguridad;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.data.validator.EmailValidator;
import com.vaadin.event.ShortcutAction;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PasswordField;
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

/**
 *
 * @author joseaguirre
 */
public class UserForm extends Window {
    
    public int idUsuario;
    
    MarginInfo  marginInfo;
        
    FormLayout userForm;
    
    Button exitBtn;
    Button saveBtn;
    
    Statement stQuery = null;
    PreparedStatement stPreparedQuery = null;
    ResultSet rsRecords = null;
        
    ComboBox companyCbx;
    TextField divisionTxt;
    TextField usuarioTxt;
    TextField nombreTxt;
    PasswordField passwordTxt;
    PasswordField password2Txt;
    TextField emailTxt;
    TextField telefonoTxt;    
    ComboBox estatusCbx;
    ComboBox perfilCbx;
    TextField codigoEspecialTxt;
    TextField horarioInicioTxt;
    TextField horarioFinTxt;
    TextField ipsAutorizadasTxt;
    CheckBox veTodosCalendariosChk;

    UI mainUI;
    
    public UserForm() {
        this.mainUI = UI.getCurrent();
        
        setResponsive(true);
        setCaption("Ficha de usuario");
        setModal(true);
               
        marginInfo = new MarginInfo(true,true,false,true);
        
        Page.getCurrent().setTitle("SOPDI - Usuario");

        asegurarColumnaVeTodosCalendarios();

        companyCbx = new ComboBox("Empresa :");
        fillCompanyCombo();
        
        userForm  = new FormLayout();
        userForm.setMargin(marginInfo);
        userForm.setSpacing(true);
        userForm.addStyleName(ValoTheme.FORMLAYOUT_LIGHT);

        usuarioTxt = new TextField("Usuario :");
        usuarioTxt.setWidth("20em");
        usuarioTxt.setMaxLength(128);

        divisionTxt = new TextField("División/Depto. :");
        divisionTxt.setWidth("20em");
        divisionTxt.setRequiredError("POR FAVOR INGRESE LA DIVISION O DEPARTAMENTO AL QUE PERTENECE EL USUARIO.");
        divisionTxt.setMaxLength(128);

        nombreTxt = new TextField("Nombre :");
        nombreTxt.setWidth("20em");
        nombreTxt.setRequired(true);
        nombreTxt.setRequiredError("POR FAVOR INGRESE EL NOMBRE DEL USUARIO");
        nombreTxt.setMaxLength(128);

        passwordTxt = new PasswordField("Contraseña :");
        passwordTxt.setWidth("8em");
        passwordTxt.setMaxLength(64);

        password2Txt = new PasswordField("Confirme :");
        password2Txt.setWidth("8em");
        password2Txt.setMaxLength(64);

        telefonoTxt = new TextField("Teléfono :");
        telefonoTxt.setWidth("8em");
        telefonoTxt.setValue("0000 0000");
        telefonoTxt.setMaxLength(16);

        emailTxt = new TextField("Email :");
        emailTxt.setWidth("15em");
        emailTxt.setValue("noaplica@email.com");
        emailTxt.setMaxLength(128);
        emailTxt.setRequired(true);
        emailTxt.setRequiredError("POR FAVOR INGRESE EL EMAIL DEL USUARIO");
//        emailTxt.addValidator(new EmailValidator());

        perfilCbx = new ComboBox("Perfil :");
        perfilCbx.setNewItemsAllowed(false);
        perfilCbx.setInvalidAllowed(false);
        perfilCbx.addItem("ADMINISTRADOR");
        perfilCbx.addItem("SUPERVISOR");
        perfilCbx.addItem("FINANCIERO");
        perfilCbx.addItem("CONTADOR");
        perfilCbx.addItem("AUXILIAR");
        perfilCbx.addItem("PROVEEDOR");
        perfilCbx.addItem("JEFE DE GRUPO");
        perfilCbx.addItem("MAESTRO OBRAS");
        perfilCbx.addItem("SUPERVISOR OBRAS");
        perfilCbx.select("SUPERVISOR");

        codigoEspecialTxt = new TextField("Código especial :");
        codigoEspecialTxt.setWidth("8em");
        codigoEspecialTxt.setMaxLength(16);

        horarioInicioTxt = new TextField("Acceso desde (HH:mm) :");
        horarioInicioTxt.setWidth("8em");
        horarioInicioTxt.setMaxLength(5);
        horarioInicioTxt.setInputPrompt("08:00");
        horarioInicioTxt.setDescription("Hora de inicio del acceso permitido. Vacío = sin restricción de horario.");

        horarioFinTxt = new TextField("Acceso hasta (HH:mm) :");
        horarioFinTxt.setWidth("8em");
        horarioFinTxt.setMaxLength(5);
        horarioFinTxt.setInputPrompt("18:00");
        horarioFinTxt.setDescription("Hora de fin del acceso permitido. Vacío = sin restricción de horario.");

        ipsAutorizadasTxt = new TextField("IPs autorizadas :");
        ipsAutorizadasTxt.setWidth("20em");
        ipsAutorizadasTxt.setMaxLength(512);
        ipsAutorizadasTxt.setInputPrompt("192.168.1.10, 200.30.40.0/24");
        ipsAutorizadasTxt.setDescription("IPs o rangos CIDR separados por coma. Vacío = sin restricción por IP.");

        veTodosCalendariosChk = new CheckBox("Ve todos los calendarios");
        veTodosCalendariosChk.addStyleName(ValoTheme.CHECKBOX_LARGE);
        veTodosCalendariosChk.setDescription("Permite ver y gestionar el calendario de cualquier usuario.");
        veTodosCalendariosChk.setValue(false);

        estatusCbx = new ComboBox("Estatus : ");
        estatusCbx.setNewItemsAllowed(false);
        estatusCbx.setInvalidAllowed(false);
        estatusCbx.addItem("ACTIVO");
        estatusCbx.addItem("INACTIVO");
        estatusCbx.select("ACTIVO");
        
        saveBtn = new Button("Guardar");
        saveBtn.setIcon(FontAwesome.SAVE); 
        saveBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick ( Button.ClickEvent event )
            {
                saveUsuario();
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

        userForm.addComponent(companyCbx);
        userForm.addComponent(divisionTxt);
        userForm.addComponent(usuarioTxt);
        userForm.addComponent(nombreTxt);
        userForm.addComponent(passwordTxt);
        userForm.addComponent(password2Txt);
        userForm.addComponent(telefonoTxt);
        userForm.addComponent(emailTxt);
        userForm.addComponent(perfilCbx);
        userForm.addComponent(codigoEspecialTxt);
        userForm.addComponent(horarioInicioTxt);
        userForm.addComponent(horarioFinTxt);
        userForm.addComponent(ipsAutorizadasTxt);
        userForm.addComponent(veTodosCalendariosChk);
        userForm.addComponent(estatusCbx);
        userForm.addComponent(buttonsLayout);
        userForm.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);
        
        VerticalLayout  contentLayout = new VerticalLayout();
        contentLayout.setSizeUndefined();
        
        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setWidth("100%");
        titleLayout.setMargin(false);

        Label titleLbl = new Label("EDITAR USUARIO");
        titleLbl.addStyleName(Runo.LABEL_H2);
        titleLbl.setSizeUndefined();

        titleLayout.addComponent(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.TOP_CENTER);
                
        contentLayout.addComponent(titleLayout);
        contentLayout.setComponentAlignment(titleLayout, Alignment.TOP_CENTER);
        contentLayout.addComponent(userForm);
        contentLayout.setComponentAlignment(userForm, Alignment.TOP_CENTER);

        setContent(contentLayout);
    }    
 
    /**
     * Agrega la columna usuario.VeTodosCalendarios si aún no existe.
     * MySQL no soporta "ADD COLUMN IF NOT EXISTS", por lo que primero se
     * consulta information_schema y solo entonces se ejecuta el ALTER.
     */
    private void asegurarColumnaVeTodosCalendarios() {
        String existeSql = "SELECT COUNT(*) FROM information_schema.COLUMNS "
                + " WHERE TABLE_SCHEMA = DATABASE() "
                + " AND TABLE_NAME = 'usuario' "
                + " AND COLUMN_NAME = 'VeTodosCalendarios'";
        String ddl = "ALTER TABLE usuario "
                + " ADD COLUMN VeTodosCalendarios TINYINT(1) NOT NULL DEFAULT 0";
        Statement st = null;
        try {
            st = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            ResultSet rs = st.executeQuery(existeSql);
            boolean existe = rs.next() && rs.getInt(1) > 0;
            rs.close();
            if (!existe) {
                st.executeUpdate(ddl);
            }
        } catch (Exception ex) {
            Logger.getLogger(UserForm.class.getName()).log(Level.WARNING,
                    "No se pudo asegurar la columna usuario.VeTodosCalendarios: {0}", ex.getMessage());
        } finally {
            try { if (st != null) st.close(); } catch (Exception ignored) { }
        }
    }

    public void fillCompanyCombo() {
        String queryString = "";
        
        queryString =  "Select * ";
        queryString += " From  empresa ";
        if(((SopdiUI) mainUI).sessionInformation.getStrUserProfile().compareTo("DESARROLLADOR") == 0) {
            queryString += " Where IdEmpresa = " + ((SopdiUI)mainUI).sessionInformation.getStrCompanyId();
        }

//System.out.println("\n\n"+queryString);

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();            
            rsRecords = stQuery.executeQuery (queryString);

            if(rsRecords.next()) { //  encontrado
                do {
                    companyCbx.addItem(rsRecords.getInt("IdEmpresa"));
                    companyCbx.setItemCaption(rsRecords.getInt("IdEmpresa"), rsRecords.getString("Nombre"));
                } while(rsRecords.next());
            }
            companyCbx.select(1);
        } 
        catch (Exception ex) {
            Logger.getLogger(UserForm.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de usuario : " + ex.getMessage());
            Notification.show("Error al intentar leer registros de usuario..!", Notification.Type.ERROR_MESSAGE);
        } 
    }
    
    public void fillUserData() {
        String queryString = "";
        
        queryString =  "Select * ";
        queryString += " From  usuario ";
        queryString += " Where IdUsuario = " + idUsuario;

//System.out.println("\n\n"+queryString);

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();            
            rsRecords = stQuery.executeQuery (queryString);

            if(rsRecords.next()) { //  encontrado
                companyCbx.select(rsRecords.getInt("IdEmpresa"));
                divisionTxt.setValue(rsRecords.getString("Division"));
                usuarioTxt.setReadOnly(false);
                usuarioTxt.setValue(rsRecords.getString("Usuario"));
                usuarioTxt.setReadOnly(true);
                nombreTxt.setValue(rsRecords.getString("Nombre"));
                emailTxt.setValue(rsRecords.getString("Email"));
                telefonoTxt.setValue(rsRecords.getString("Telefono"));
                perfilCbx.select(rsRecords.getString("Perfil"));
                codigoEspecialTxt.setValue(rsRecords.getString("CodigoEspecial"));
                horarioInicioTxt.setValue(ControlAcceso.timeAHhmm(rsRecords.getTime("HorarioAccesoInicio")));
                horarioFinTxt.setValue(ControlAcceso.timeAHhmm(rsRecords.getTime("HorarioAccesoFin")));
                String ips = rsRecords.getString("IpsAutorizadas");
                ipsAutorizadasTxt.setValue(ips == null ? "" : ips);
                veTodosCalendariosChk.setValue("1".equals(rsRecords.getString("VeTodosCalendarios")));
                estatusCbx.select(rsRecords.getString("Estatus"));
            }
            
        } 
        catch (Exception ex) {
            Logger.getLogger(UserForm.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de usuario : " + ex.getMessage());
            Notification.show("Error al intentar leer registros de usuario..!", Notification.Type.ERROR_MESSAGE);
        } 
    }

    private void saveUsuario() {
        
        if(usuarioTxt.getValue().trim().isEmpty()) {
            Notification.show("Error, falta el usuario!", Notification.Type.ERROR_MESSAGE);
            usuarioTxt.focus();
            return;
        }
        if(nombreTxt.getValue().trim().isEmpty()) {
            Notification.show("Error, falta el nombre!", Notification.Type.ERROR_MESSAGE);
            nombreTxt.focus();
            return;
        }
        if(!passwordTxt.getValue().trim().isEmpty() || !password2Txt.getValue().trim().isEmpty()) {
            if(password2Txt.getValue().compareTo(passwordTxt.getValue()) != 0) {
                Notification.show("Error, las contraseñas no coinciden!", Notification.Type.ERROR_MESSAGE);
                passwordTxt.focus();
                return;
            }
        }

        // --- Restricciones de acceso (horario / IPs) ---
        String horaInicioSql;
        String horaFinSql;
        try {
            horaInicioSql = ControlAcceso.horaALiteralSql(horarioInicioTxt.getValue());
            horaFinSql    = ControlAcceso.horaALiteralSql(horarioFinTxt.getValue());
        } catch (IllegalArgumentException iae) {
            Notification.show(iae.getMessage(), Notification.Type.ERROR_MESSAGE);
            horarioInicioTxt.focus();
            return;
        }
        // Si se restringe por horario, deben indicarse ambas horas.
        if (horaInicioSql.equals("NULL") != horaFinSql.equals("NULL")) {
            Notification.show("Para restringir por horario debe indicar ambas horas (desde y hasta).", Notification.Type.WARNING_MESSAGE);
            horarioInicioTxt.focus();
            return;
        }
        String ipsValue = ipsAutorizadasTxt.getValue() == null ? "" : ipsAutorizadasTxt.getValue().trim();
        String ipsSql   = ipsValue.isEmpty() ? "NULL" : "'" + ipsValue + "'";

        String queryString;
        
        try {
            if(idUsuario == 0) {
                queryString =  "Select * ";
                queryString += " From  usuario ";
                queryString += " Where Usuario = '" + usuarioTxt.getValue() + "'";
                queryString += " And IdEmpresa = "  + ((SopdiUI) mainUI).sessionInformation.getStrCompanyId();

//System.out.println("\n\n"+queryString);

                stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();            
                rsRecords = stQuery.executeQuery (queryString);

                if(rsRecords.next()) { //  encontrado
                    Notification.show("Usuario existente, por favor elija otro nombre de usuario!", Notification.Type.WARNING_MESSAGE);
                    usuarioTxt.focus();
                    return;
                }
                queryString =  "Insert Into usuario (Usuario, Nombre, Clave, Email, Telefono, Perfil, CodigoEspecial, Estatus, IdEmpresa, Division, HorarioAccesoInicio, HorarioAccesoFin, IpsAutorizadas, VeTodosCalendarios)";
                queryString += " Values (";
                queryString += "'"   + usuarioTxt.getValue() + "'";
                queryString += ",'"  + nombreTxt.getValue()  + "'";

                if(((SopdiUI) mainUI).databaseProvider.getUsedDBDataSource().equals("MYSQL")) {            
                    queryString += ",Sha1('" + passwordTxt.getValue() + "')";
                }
                else {
                    queryString += ",SUBSTRING(master.dbo.fn_varbintohexstr(HASHBYTES('SHA1', '" + passwordTxt.getValue() + "')),3,40)";
                }            

                queryString += ",'" + emailTxt.getValue()    + "'";
                queryString += ",'" + telefonoTxt.getValue() + "'";
                queryString += ",'" + perfilCbx.getValue() + "'";
                queryString += ",'" + codigoEspecialTxt.getValue() + "'";
                queryString += ",'" + estatusCbx.getValue() + "'";
                queryString += ","  + companyCbx.getValue();
                queryString += ",'" + divisionTxt.getValue() + "'";
                queryString += ","  + horaInicioSql;
                queryString += ","  + horaFinSql;
                queryString += ","  + ipsSql;
                queryString += ","  + (veTodosCalendariosChk.getValue() ? "1" : "0");
                queryString += ")";
            }
            else {
                queryString =  "Update usuario Set ";
                queryString += " Nombre = '"  + nombreTxt.getValue()      + "'";
                if(!passwordTxt.getValue().trim().isEmpty()) {
                    if(((SopdiUI) mainUI).databaseProvider.getUsedDBDataSource().equals("MYSQL")) {            
                        queryString += ",Clave = Sha1('" + passwordTxt.getValue() + "')";
                    }
                    else {
                        queryString += ",Clave = SUBSTRING(master.dbo.fn_varbintohexstr(HASHBYTES('SHA1', '" + passwordTxt.getValue() + "')),3,40)";
                    }            
                }
                queryString += ",Email = '" + emailTxt.getValue()   + "'";
                queryString += ",Telefono = '" + telefonoTxt.getValue()    + "'";
                queryString += ",Perfil = '" + perfilCbx.getValue() + "'";
                queryString += ",CodigoEspecial = '" + codigoEspecialTxt.getValue() + "'";
                queryString += ",Estatus = '" + estatusCbx.getValue() + "'";
                queryString += ",IdEmpresa ="  + companyCbx.getValue();
                queryString += ",Division = '" + divisionTxt.getValue() + "'";
                queryString += ",HorarioAccesoInicio = " + horaInicioSql;
                queryString += ",HorarioAccesoFin = "    + horaFinSql;
                queryString += ",IpsAutorizadas = "      + ipsSql;
                queryString += ",VeTodosCalendarios = "  + (veTodosCalendariosChk.getValue() ? "1" : "0");
                queryString += " Where IdUsuario = " + idUsuario;
            }

System.out.println("queryString="+queryString);

            if(idUsuario > 0) {
                stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();            
                stQuery.executeUpdate(queryString);
            }
            else {
                stPreparedQuery  = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().prepareStatement(queryString, Statement.RETURN_GENERATED_KEYS);
                stPreparedQuery.executeUpdate();
                rsRecords = stPreparedQuery.getGeneratedKeys();

                rsRecords.next();

                idUsuario = rsRecords.getInt(1);                
            }

            Notification.show("OPERACION EXITOSA!", Notification.Type.HUMANIZED_MESSAGE);

            ((UsersView)(mainUI.getNavigator().getCurrentView())).fillReportTable();            
        }
        catch(Exception ex)
        {
            Notification.show("Error al actualizar usuario : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
        close();
    }    
}