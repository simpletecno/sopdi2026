/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.utilerias;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.configuracion.EnvironmentVars;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.MultiFileUpload;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadFinishedHandler;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadStateWindow;

import javax.mail.MessagingException;
import java.io.*;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joseaguirre
 */
public class TicketSoporteForm extends Window {

    MultiFileUpload singleUpload;
    public File file;
    StreamResource logoStreamResource = null;
    String parametro1 = "", parametro2 = "";
    Long parametro3 = 0L;
    long fileSize;
    EnvironmentVars enviromentsVars = new EnvironmentVars();

    VerticalLayout mainLayout;
    MarginInfo  marginInfo;
    
    FormLayout ticketSoporteForm;
    
    Button saveBtn;
    
    Statement stQuery = null;
    ResultSet rsRecords = null;
        
    TextArea descripcionTxt;
    TextArea puntoReferenciaTxt;
    ComboBox prioridadCbx;
    ComboBox categoriaCbx;
    ComboBox asignadoCbx;

    UI mainUI;
    
    public TicketSoporteForm() {
        this.mainUI = UI.getCurrent();

        setResponsive(true);
        setCaption("Ticket de soporte");
        setWidth("60%");
        setHeight("40%");
        
        marginInfo = new MarginInfo(true,true,false,true);
        
        mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();

        createFormLayout();

        saveBtn = new Button("Crear y enviar");
        saveBtn.setIcon(FontAwesome.SAVE); 
        saveBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        saveBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick ( Button.ClickEvent event )
            {
                saveTicket();
            }
        });                

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.addComponent(saveBtn);
        buttonsLayout.setComponentAlignment(saveBtn, Alignment.BOTTOM_LEFT);
        
        mainLayout.addComponent(buttonsLayout);
        mainLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);        
        
        setContent(mainLayout);
    }
    
    private void createFormLayout() {
        VerticalLayout tab1Layout = new VerticalLayout();
        tab1Layout.setSpacing(true);
        tab1Layout.setMargin(true);
        tab1Layout.setWidth("100%");
        
        ticketSoporteForm  = new FormLayout();
        ticketSoporteForm.setMargin(marginInfo);
        ticketSoporteForm.setSpacing(false);
        ticketSoporteForm.addStyleName(ValoTheme.FORMLAYOUT_LIGHT);
        ticketSoporteForm.setWidth("60%");

        descripcionTxt = new TextArea("Descripción : ");
        descripcionTxt.setWidth("100%");
        descripcionTxt.setHeight("70px");
        descripcionTxt.setRequired(true);
        descripcionTxt.setRequiredError("POR FAVOR INGRESE LA DESCRIPCION.");
        descripcionTxt.setInputPrompt("describa el asunto de manera clara y con detalle");

        puntoReferenciaTxt = new TextArea("DATO de referencia : ");
        puntoReferenciaTxt.setWidth("100%");
        puntoReferenciaTxt.setHeight("50px");
        puntoReferenciaTxt.setRequired(true);
        puntoReferenciaTxt.setRequiredError("POR FAVOR INGRESE EL PUNTO DE REFERENCIA.");
        puntoReferenciaTxt.setInputPrompt("puede ser el nombre de la función u opción donde requiera el soporte, o un dato relevante como el código de partida o número de documento...");

        categoriaCbx = new ComboBox("Categoria : ");
        categoriaCbx.setNewItemsAllowed(false);
        categoriaCbx.setInvalidAllowed(false);
        categoriaCbx.setTextInputAllowed(false);
        categoriaCbx.setNullSelectionAllowed(false);
        categoriaCbx.addItem("ERROR DEL SISTEMA");
        categoriaCbx.addItem("MEJORA");
        categoriaCbx.addItem("APOYO");
        categoriaCbx.addItem("ACCESOS Y PERMISOS");
        categoriaCbx.select("APOYO");

        prioridadCbx = new ComboBox("Prioridad : ");
        prioridadCbx.setNewItemsAllowed(false);
        prioridadCbx.setInvalidAllowed(false);
        prioridadCbx.setTextInputAllowed(false);
        prioridadCbx.setNullSelectionAllowed(false);
        prioridadCbx.addItem("BAJA");
        prioridadCbx.addItem("MODERADA");
        prioridadCbx.addItem("ALTA");
        prioridadCbx.addItem("URGENTE");
        prioridadCbx.select("BAJA");

        asignadoCbx = new ComboBox("Asignado : ");
        asignadoCbx.setNewItemsAllowed(false);
        asignadoCbx.setInvalidAllowed(false);
        asignadoCbx.setTextInputAllowed(false);
        asignadoCbx.setNullSelectionAllowed(false);
        asignadoCbx.addContainerProperty("email", String.class, "");
//        asignadoCbx.addItem("INFORMATICA");
//        asignadoCbx.addItem("ADMINISTRACION");
//        asignadoCbx.addItem("OPERACIONES");
//        asignadoCbx.addItem("GERENCIA");
//        asignadoCbx.select("INFORMATICA");

        fillComboAsignado();

        UploadFinishedHandler handler;
        handler = new UploadFinishedHandler() {
            @Override
            public void handleFile(InputStream stream, String fileName, String mimeType, long length) {

                File targetFile;

                try {
                    if (mimeType.contains("png") || mimeType.contains("jpeg")
                            || mimeType.contains("jpg") || mimeType.contains("pdf")
                            || mimeType.contains("officedocument.spreadsheetml.sheet")
                    ) {

                        fileSize = stream.available();
                        byte[] buffer = new byte[stream.available()];
                        stream.read(buffer);

                        String filePath = enviromentsVars.getDtePath();

                        new File(filePath).mkdirs();

//                        fileName = filePath + codigoPartidaUpdate + fileName.substring(fileName.length()-4, fileName.length());
                        if(fileName.endsWith(".xlsx")) {
                            fileName = filePath + new Utileria().getReferencia() + fileName.substring(fileName.length() - 5, fileName.length());
                        }
                        else {
                            fileName = filePath + new Utileria().getReferencia() + fileName.substring(fileName.length() - 4, fileName.length());
                        }

                        targetFile = new File(fileName);
                        OutputStream outStream = new FileOutputStream(targetFile);
                        outStream.write(buffer);
                        outStream.close();

                        stream.close();

                        logoStreamResource = null;

                        if (buffer != null) {
                            logoStreamResource = new StreamResource(
                                    new StreamResource.StreamSource() {
                                        public InputStream getStream() {
                                            return new ByteArrayInputStream(buffer);
                                        }
                                    }, String.valueOf(System.currentTimeMillis())
                            );
                        }

                        recibirParametrosImagen(targetFile.getName(), mimeType, fileSize);

                        file = targetFile;

                        Notification.show("Archivo cargado con exito!!..", Notification.Type.TRAY_NOTIFICATION);

                    } else {
                        Notification.show("El archivo no contiene un formato compatible. solo puede subir archivos con formato 'PNG','JEPG','JPG','PDF', 'XLS', 'XLSX'", Notification.Type.ERROR_MESSAGE);
                        return;
                    }

                } catch (Exception fIoEx) {
                    fIoEx.printStackTrace();
                    Notification.show("Error al cargar el archivo adjunto!", Notification.Type.ERROR_MESSAGE);
                    return;
                }
            }
        };

        UploadStateWindow window = new UploadStateWindow();

        singleUpload = new MultiFileUpload(handler, window, false);
        singleUpload.setIcon(FontAwesome.UPLOAD);
        singleUpload.setImmediate(true);
        singleUpload.getSmartUpload().setUploadButtonCaptions("Buscar y cargar archivo", "");

        JavaScript.getCurrent().execute("document.getElementsByClassName('gwt-FileUpload')[0].setAttribute('accept', '.png')");
        JavaScript.getCurrent().execute("document.getElementsByClassName('gwt-FileUpload')[1].setAttribute('accept', '.jpg')");
        JavaScript.getCurrent().execute("document.getElementsByClassName('gwt-FileUpload')[1].setAttribute('accept', '.jpeg')");
        JavaScript.getCurrent().execute("document.getElementsByClassName('gwt-FileUpload')[2].setAttribute('accept', '.PDF')");
        JavaScript.getCurrent().execute("document.getElementsByClassName('gwt-FileUpload')[2].setAttribute('accept', '.xls')");
        JavaScript.getCurrent().execute("document.getElementsByClassName('gwt-FileUpload')[2].setAttribute('accept', '.xlsx')");

        ticketSoporteForm.addComponent(descripcionTxt);
        ticketSoporteForm.addComponent(puntoReferenciaTxt);
        ticketSoporteForm.addComponent(categoriaCbx);
        ticketSoporteForm.addComponent(prioridadCbx);
        ticketSoporteForm.addComponent(asignadoCbx);
        ticketSoporteForm.addComponents(singleUpload);

        tab1Layout.addComponent(ticketSoporteForm);
        tab1Layout.setComponentAlignment(ticketSoporteForm, Alignment.MIDDLE_CENTER);
        
        descripcionTxt.focus();

        mainLayout.addComponent(tab1Layout);
        mainLayout.setComponentAlignment(tab1Layout, Alignment.TOP_CENTER);

    }

    void fillComboAsignado() {

        String queryString = "SELECT * ";
        queryString += " FROM usuario  ";
        queryString += " WHERE Estatus = 'ACTIVO'";
//        queryString += " AND IdUsuario <> " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserId();

        asignadoCbx.removeAllItems();

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords = stQuery.executeQuery (queryString);

            while(rsRecords.next()) { //  encontrado
                asignadoCbx.addItem(rsRecords.getString("IdUsuario"));
                asignadoCbx.setItemCaption(rsRecords.getString("IdUsuario"), rsRecords.getString("Nombre"));
                asignadoCbx.getContainerProperty(rsRecords.getString("IdUsuario"), "email").setValue(rsRecords.getString("Usuario"));
            }

            if(rsRecords.first()) {
                asignadoCbx.select(rsRecords.getString("IdUsuario"));
            }
        }
        catch(Exception ex1) {
            Notification.show("ERROR FATAL DEL SISTEMA", Notification.Type.ERROR_MESSAGE);
            System.out.println("ERROR AL INTENTAR BUSCAR CATALOGO DE USUARIOS : " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    private void saveTicket() {
        
        if(descripcionTxt.getValue().trim().isEmpty()) {
            Notification.show("Por favor, ingrese la descripcioón!", Notification.Type.WARNING_MESSAGE);
            descripcionTxt.focus();
            return;
        }

        if(puntoReferenciaTxt.getValue().trim().isEmpty()) {
            Notification.show("Por favor, ingrese el punto de referencia!", Notification.Type.WARNING_MESSAGE);
            puntoReferenciaTxt.focus();
            return;
        }

        String queryString;
        queryString =  "Insert Into ticket_soporte (IdEmpresa, Descripcion, PuntoReferencia, Categoria, Prioridad,";
        queryString += " AsignadoUsuario, UrlDocumento, UrlDocumentoTipo, CreadoUsuario, CreadoFechaYHora)";
        queryString += " Values (";
        queryString +=  ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
        queryString += ",'"  + descripcionTxt.getValue() + "'";
        queryString += ",'"  + puntoReferenciaTxt.getValue() + "'";
        queryString += ",'"  + categoriaCbx.getValue() + "'";
        queryString += ",'"  + prioridadCbx.getValue()   + "'";
        queryString += ",'"  + asignadoCbx.getValue()   + "'";
        queryString += ",'"  + parametro1 + "'"; //url documento cargado filename
        queryString += ",'"  + parametro2 + "'"; //url documento cargado type
        queryString += ","   + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
        queryString += ",current_timestamp";
        queryString += ")";

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            Notification.show("OPERACION EXITOSA!", Notification.Type.HUMANIZED_MESSAGE);

            ((TicketsSoporteView)(mainUI.getNavigator().getCurrentView())).fillTicketsGrid();

            close();

            try {
                String emailsTo[] = {String.valueOf(asignadoCbx.getContainerProperty(asignadoCbx.getValue(), "email").getValue())};
                MyEmailMessanger eMail = new MyEmailMessanger();

                String texto  = "DESCRIPCION : " + descripcionTxt.getValue() + "\n";
                       texto += "EMPRESA     : " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId() + " " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyName() + "\n";
                       texto += "CREADO POR  : " + ((SopdiUI) mainUI).sessionInformation.getStrUserFullName() + "\n";
                       texto += "PUNTO DE REF: " + puntoReferenciaTxt.getValue() + "\n";
                       texto += "CATEGORIA   : " + categoriaCbx.getValue() + "\n";
                       texto += "PRIORIDAD   : " + prioridadCbx.getValue() + "\n\n\n";
                       texto += "***Creado automaticamente por el sistema SOPDI.***";

                eMail.postMail(emailsTo, "SOPDI : Nuevo Ticket de soporte : " + descripcionTxt.getValue(), texto );
            } catch (MessagingException ex2) {
                Logger.getLogger(SopdiUI.class.getName()).log(Level.SEVERE, null, ex2);
            }

        }
        catch(Exception ex)
        {
            Notification.show("Error al creaar ticket de soporte : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public void recibirParametrosImagen(String fileName, String mimeType, long peso) {
        parametro1 = fileName;
        parametro2 = mimeType;
        parametro3 = peso;
    }


}