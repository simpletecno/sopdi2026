
package com.simpletecno.sopdi.operativo;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.themes.Runo;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Esta ventana procesa la gestion de venta :
 * @author JAguirre
 */
@SuppressWarnings("serial")
public class ChangeOrderForm extends Window {
        
    FormLayout formLayout = new FormLayout();
    
    TextField idChangeOrderTxt;
    PopupDateField fechaDt;
    TextField solicitadoPorTxt;
    ComboBox solicitadoRolCbx;
    TextArea instruccionTxt;
    
    private Button saveBtn;
    private Button exitBtn;
    
    Statement stQuery = null;
    ResultSet rsRecords = null;
    Statement stQuery1 = null;
    PreparedStatement stPreparedQuery = null;
    ResultSet rsRecords1 = null;
        
    private UI mainUI;
    
    public ChangeOrderForm() {
    
        this.mainUI = UI.getCurrent();
        setResponsive(true);
        setModal(true);
                
        formLayout.setMargin(true);
        formLayout.setWidth("100%");
        formLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        formLayout.setMargin(false);
        formLayout.addStyleName(ValoTheme.FORMLAYOUT_LIGHT);

        idChangeOrderTxt = new TextField("Id Orden de cambio : ");
        idChangeOrderTxt.setWidth("8em");
        idChangeOrderTxt.setValue("0");
        idChangeOrderTxt.setReadOnly(true);
        idChangeOrderTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);
        
        fechaDt = new PopupDateField("Fecha :");
        fechaDt.setValue(new java.util.Date());
        fechaDt.setResolution(Resolution.DAY);
        fechaDt.setDateFormat("dd-MM-yyyy");
        fechaDt.setWidth("120px");

        solicitadoPorTxt = new TextField("Solicitado por :");
        solicitadoPorTxt.setWidth("25em");
        solicitadoPorTxt.setRequired(true);
        solicitadoPorTxt.setRequiredError("POR FAVOR INGRESE EL NOMBRE DE LA PERSONA QUE SOLICITA LA ORDEN DE CAMBIO.");
//        solicitadoPorTxt.addStyleName("v-textfield-uppercase");
                
        solicitadoRolCbx = new ComboBox("Rol :");
        solicitadoRolCbx.addItem("<<ELIJA>>");
        solicitadoRolCbx.addItem("Gerente");
        solicitadoRolCbx.addItem("Accionista");
        solicitadoRolCbx.addItem("Asesor");
        solicitadoRolCbx.addItem("Cliente");
        solicitadoRolCbx.addItem("Entidad gubernamental");
        solicitadoRolCbx.select("<<ELIJA>>");
                                
        instruccionTxt    = new TextArea("Requerimientos :");
        instruccionTxt.setWidth("30em");
        instruccionTxt.setHeight("10em");
        instruccionTxt.setValue("");
        
        saveBtn = new Button("Guardar");
        saveBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        saveBtn.setIcon(FontAwesome.SAVE);
        saveBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick ( Button.ClickEvent event )
            {
                validarYGuardar();
            }
        });

        exitBtn    = new Button("Salir");
        exitBtn.setIcon(FontAwesome.SIGN_OUT);
        exitBtn.setClickShortcut(KeyCode.ESCAPE);
        exitBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        exitBtn.addStyleName(ValoTheme.BUTTON_SMALL);
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
        buttonsLayout.addComponent(saveBtn);
        buttonsLayout.setComponentAlignment(saveBtn, Alignment.BOTTOM_LEFT);
        buttonsLayout.addComponent(exitBtn);
        buttonsLayout.setComponentAlignment(exitBtn, Alignment.BOTTOM_RIGHT);

        formLayout.addComponent(idChangeOrderTxt);
        formLayout.addComponent(fechaDt);
        formLayout.addComponent(solicitadoPorTxt);
        formLayout.addComponent(solicitadoRolCbx);
        formLayout.addComponent(instruccionTxt);

        formLayout.addComponent(buttonsLayout);

        VerticalLayout  contentLayout = new VerticalLayout();
        contentLayout.setSizeUndefined();
        
        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setWidth("100%");
        titleLayout.setMargin(false);
//        titleLayout.addStyleName(Runo.LAYOUT_DARKER);

        Label titleLbl = new Label("NUEVA ORDEN DE CAMBIO");
        titleLbl.addStyleName(Runo.LABEL_H2);
        titleLbl.setSizeUndefined();

        titleLayout.addComponent(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.TOP_CENTER);
        
        contentLayout.addComponent(titleLayout);
        contentLayout.setComponentAlignment(titleLayout, Alignment.TOP_CENTER);
        contentLayout.addComponent(formLayout);
        contentLayout.setComponentAlignment(formLayout, Alignment.TOP_CENTER);        

        setContent(contentLayout);
    }
                
    public void fillData() {
        
        if(idChangeOrderTxt.getValue().compareTo("0") == 0) {
            return;
        }
        
        String queryString = "";
        
        queryString =  "SELECT * ";
        queryString += " FROM  orden_cambio ";
        queryString += " WHERE IdOrdenCambio = " + idChangeOrderTxt.getValue();

//System.out.println("\n\n"+queryString);

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery (queryString);

            if(rsRecords.next()) { //  encontrado

                fechaDt.setValue(rsRecords.getDate("Fecha"));
                solicitadoPorTxt.setValue(rsRecords.getString("SolicitadoPor"));
                solicitadoRolCbx.select(rsRecords.getString("SolicitadoRol"));
                instruccionTxt.setValue(rsRecords.getString("Instruccion"));                

            }
            else {
                Notification.show("Error, no se encotró registro de orden de cabmio!", Notification.Type.ERROR_MESSAGE);
                close();
            }
        } 
        catch (Exception ex) {
            Logger.getLogger(ProjectTaskView.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de orden de cambio : " + ex.getMessage());
            Notification.show("Error al intentar leer registros de orden de cambio..!", Notification.Type.ERROR_MESSAGE);
        } 
    }

    private void validarYGuardar() {
        
        if(solicitadoPorTxt.getValue().isEmpty()) {
            Notification.show("Debe ingresar el nombre de la persona que solicita la orden de cambio.");
            solicitadoPorTxt.focus();
            return;            
        }
        if(String.valueOf(solicitadoRolCbx.getValue()).equals("<<ELIJA>>")) {
            Notification.show("Debe ingresar el rol de la persona solicitante.");
            solicitadoRolCbx.focus();
            return;            
        }
        
        String queryString = "";
        
        if(idChangeOrderTxt.getValue().equals("0")) {
            queryString = "INSERT INTO orden_cambio ";
            queryString += "(IdProyecto, Fecha, SolicitadoPor, SolicitadoRol, ";
            queryString += " Instruccion, CreadoUsuario, CreadoFechaYHora) ";
            queryString += " VALUES (";
            queryString += "  " + ((SopdiUI)UI.getCurrent()).sessionInformation.getStrProjectId();
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += ",'" + solicitadoPorTxt.getValue()   + "'";
            queryString += ",'" + String.valueOf(solicitadoRolCbx.getValue()) + "'";
            queryString += ",'" + instruccionTxt.getValue()   + "'";
            queryString += ", " + ((SopdiUI)mainUI).sessionInformation.getStrUserId(); //creado usuario
            queryString += ",current_timestamp"; // creado fechayhora
            queryString += ")";
        }
        else {
            queryString = "UPDATE orden_cambio SET";
            queryString += " Fecha = '" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += ",SolicitadoPor = '" + solicitadoPorTxt.getValue()   + "'";
            queryString += ",SolicitadoRol = '" + String.valueOf(solicitadoRolCbx.getValue()) + "'";
            queryString += ",Instruccion = '" + instruccionTxt.getValue()   + "'";
            queryString += " WHERE IdOrdenCambio = " + idChangeOrderTxt.getValue();
        }

System.out.println("\nQUERY="+queryString + "\n");

        try {
/**
            stPreparedQuery  = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().prepareStatement(queryString, Statement.RETURN_GENERATED_KEYS);
            stPreparedQuery.executeUpdate();
            rsRecords = stPreparedQuery.getGeneratedKeys();

            rsRecords.next();

            int idVisitaInspecion = rsRecords.getInt(1);

            insertSeguimiento(String.valueOf(idVisitaInspecion), instruccionTxt.getValue());
**/
            stQuery = ((SopdiUI)mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);
            
            Notification.show("OPERACION EXITOSA!", Notification.Type.WARNING_MESSAGE);

            if(mainUI.getNavigator().getCurrentView().getClass().getSimpleName().compareTo("VisitasView") == 0) {
                ((VisitasView)(mainUI.getNavigator().getCurrentView())).fillVisitasTable();
            }
            if(mainUI.getNavigator().getCurrentView().getClass().getSimpleName().compareTo("ChangeOrdersView") == 0) {
                ((ChangeOrdersView)(mainUI.getNavigator().getCurrentView())).fillChangeOrdersTable();
            }
 
            close();
/**
            MyEmailMessanger eMail = new MyEmailMessanger(((SopdiUI) mainUI).mainWindow);
            eMail.avisoVentaNueva(
                    String.valueOf(idContacto),
                    ((SopdiUI) mainUI).sessionInformation.getStrUserSupervisorEMail(), 
                    ((SopdiUI) mainUI).sessionInformation.getStrUserName(), 
                    solicitadoPorTxt.getValue());
**/
        }
        catch(Exception ex1) {
            Notification.show("ERROR FATAL DEL SISTEMA");
            System.out.println("ERROR AL INTENTAR GUARDAR LOS CAMBIOS : " + ex1.getMessage());
            ex1.printStackTrace();
        }
        
    }
    
    private void insertSeguimiento(String inspeccionId, String texto) {

        String queryString;
        
        queryString = "INSERT INTO orden_cambio_seguimiento (FechaYHora, IdOrdenCambio, ";
        queryString += " Observacion, IdUsuario) ";
        queryString += " VALUES (";
        queryString += "  current_timestamp";
        queryString += ", " + inspeccionId;
        queryString += ",'" + texto + "'";
        queryString += ", " + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
        queryString += ")";
        
System.out.println(queryString);

        try {
            stQuery = ((SopdiUI)mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);
        }
        catch(Exception ex1) {
            Notification.show("ERROR FATAL DEL SISTEMA");
            System.out.println("ERROR AL INTENTAR GUARDAR SEGUIMIENTO : " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }
        
}
