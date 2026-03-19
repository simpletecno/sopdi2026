package com.simpletecno.sopdi.seguridad;

import com.simpletecno.sopdi.utilerias.MyEmailMessanger;
import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.SelectionEvent;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;
import org.vaadin.dialogs.ConfirmDialog;

public class UsuarioCorreoForm extends Window {

    VerticalLayout mainLayout;

    UI mainUI;
    Statement stQuery;
    ResultSet rsRecords;
    String queryString;
    String correo = "";
    String nombre = "";
    String token = "";

    public IndexedContainer usuarioContainer = new IndexedContainer();
    Grid usuarioGrid;

    static final String ID_PROPERTY = "ID.";
    static final String NOMBRE_PROPERTY = "Nombre";
    static final String CORREO_PROPERTY = "Correo";

    public UsuarioCorreoForm(String token) {       
        this.mainUI = UI.getCurrent();
        this.token = token;
        
        setResponsive(true);
        setWidth("50%");
        setHeight("45%");

        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setResponsive(true);

        setContent(mainLayout);

        crearGridUsuarios();

        llenarGridUsuarios();
    }

    public void crearGridUsuarios() {

        HorizontalLayout reportLayout = new HorizontalLayout();
        reportLayout.setWidth("96%");
        reportLayout.addStyleName("rcorners3");
        reportLayout.setResponsive(true);
        reportLayout.setMargin(true);

        usuarioContainer.addContainerProperty(ID_PROPERTY, String.class, null);
        usuarioContainer.addContainerProperty(NOMBRE_PROPERTY, String.class, null);
        usuarioContainer.addContainerProperty(CORREO_PROPERTY, String.class, null);

        usuarioGrid = new Grid("Listado de Usuarios Disponibles. ", usuarioContainer);
        usuarioGrid.setImmediate(true);
        usuarioGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        usuarioGrid.setDescription("Seleccione un usuario.");
        usuarioGrid.setHeightMode(HeightMode.ROW);
        usuarioGrid.setHeightByRows(7);
        usuarioGrid.setWidth("80%");
        usuarioGrid.setResponsive(true);
        usuarioGrid.setEditorBuffered(false);
        usuarioGrid.setEditorEnabled(false);

        usuarioGrid.addSelectionListener(new SelectionEvent.SelectionListener() {
            @Override
            public void select(SelectionEvent event) {
                if (usuarioGrid.getSelectedRows() != null) {
                    if (usuarioGrid.getSelectedRow() != null) {
                        
                        correo = String.valueOf(usuarioContainer.getContainerProperty(usuarioGrid.getSelectedRow(), CORREO_PROPERTY).getValue());                        
                        nombre = String.valueOf(usuarioContainer.getContainerProperty(usuarioGrid.getSelectedRow(), NOMBRE_PROPERTY).getValue());                        

                        ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de Enviar el Token al usuario seleccionado?",
                                "SI", "NO", new ConfirmDialog.Listener() {

                            public void onClose(ConfirmDialog dialog) {
                                if (dialog.isConfirmed()) {
                                    try {
                                        enviarTokenCorreo(nombre, correo);
                                    } catch (Exception ex) {
                                        System.out.println("Error al intentar eliminar " + ex);
                                        ex.printStackTrace();
                                    }
                                }
                            }
                        });
                    }
                }
            }
        });

        reportLayout.addComponent(usuarioGrid);
        reportLayout.setComponentAlignment(usuarioGrid, Alignment.TOP_CENTER);

        mainLayout.addComponent(reportLayout);
        mainLayout.setComponentAlignment(reportLayout, Alignment.MIDDLE_CENTER);

    }

    public void llenarGridUsuarios() {

        usuarioContainer.removeAllItems();

        queryString = " SELECT *";
        queryString += " FROM usuario ";

        System.out.println("Query usuario " + queryString);

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                do {
                    Object itemId = usuarioContainer.addItem();

                    usuarioContainer.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords.getString("IdUsuario"));
                    usuarioContainer.getContainerProperty(itemId, NOMBRE_PROPERTY).setValue(rsRecords.getString("Nombre"));
                    usuarioContainer.getContainerProperty(itemId, CORREO_PROPERTY).setValue(rsRecords.getString("Email"));

                } while (rsRecords.next());
            }
        } catch (Exception ex) {
            System.out.println("Error al consultar la tabla usuarios en base de datos " + ex);
            ex.printStackTrace();

        }

    }

    public void enviarTokenCorreo(String nombre, String correo) {
        try {
            
            try {
                    String emailsTo[] = {correo};
                    MyEmailMessanger eMail = new MyEmailMessanger();

                    String texto  = "DESCRIPCION : " + "Token de autorización para ingreso de documento" + "\n";
                    texto += "EMPRESA      : " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId() + " " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyName() + "\n";
                    texto += "CREADO POR   : " + ((SopdiUI) mainUI).sessionInformation.getStrUserFullName() + "\n";                   
                    texto += "TOKEN        : " + token+ "\n";
                    texto += "\n\n\n***Creado automaticamente por el sistema SOPDI.***";

                    eMail.postMail(emailsTo, "SOPDI : Token de Autorizacion " , texto);
                } catch (MessagingException ex2) {
                    Logger.getLogger(SopdiUI.class.getName()).log(Level.SEVERE, null, ex2);
                }
            
            Notification.show("El Token fue enviado con éxito a " + nombre , Notification.Type.HUMANIZED_MESSAGE);
            
            close();

        } catch (Exception ex2) {
            System.out.println("Error al intetnar enviar token por correo" + ex2);
            ex2.printStackTrace();
        }
    }
}
