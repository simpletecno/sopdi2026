/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.configuracion;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.seguridad.UserPreferences;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.server.FileResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.MultiFileUpload;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadFinishedHandler;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadStateWindow;
import org.vaadin.ui.NumberField;

/**
 *
 * @author joseaguirre
 */
public class ProjectForm extends Window {
    
    VerticalLayout mainLayout;    
    TabSheet tabSheet;
    MarginInfo  marginInfo;
    

    FormLayout proyectoForm;

    Button saveBtn;
    
    Statement stQuery = null;
    static PreparedStatement stPreparedQuery;
    ResultSet rsRecords = null;
        
    TextField idProyectoTxt;
    TextField nombreTxt;
    ComboBox paisCbx;
    TextField ubicacionTxt;
    TextField responsableTxt;
    TextField telefonoTxt;
    TextField sitioWebTxt;
    PopupDateField fechaInicioDt;
    PopupDateField fechaFinDt;
    NumberField diasAntesHoyFechaTareaProgramadaTxt;
    NumberField diasDespuesHoyFechaTareaProgramadaTxt;
    PasswordField passwordTxt;
    NumberField factorPresupuestoClienteTxt;
    ComboBox estatusCbx;
    
    MultiFileUpload singleUpload;
    Image logoImage;
    public File file;
    StreamResource logoStreamResource = null;

    UI mainUI;
    
    public ProjectForm() {
        this.mainUI = UI.getCurrent();
       
        setResponsive(true);
        setCaption("Ficha del proyecto : " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrProjectName());
        setWidth("70%");
        setHeight("75%");
        
        marginInfo = new MarginInfo(true,true,false,true);
        
        mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        
        tabSheet = new TabSheet();
        tabSheet.setCaption("Proyecto : " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrProjectName());
        tabSheet.addStyleName(ValoTheme.TABSHEET_COMPACT_TABBAR);
        tabSheet.addStyleName(ValoTheme.TABSHEET_FRAMED);
        tabSheet.addStyleName(ValoTheme.TABSHEET_PADDED_TABBAR);
        tabSheet.setWidth("100%");
        
        tabSheet.addTab(buildTab1(), "Datos generales",  FontAwesome.BUILDING);
        tabSheet.addTab(buildTab5(), "Logo", FontAwesome.BULLSEYE);

        mainLayout.addComponent(tabSheet);
        mainLayout.setComponentAlignment(tabSheet, Alignment.MIDDLE_CENTER);

        saveBtn = new Button("Guardar");
        saveBtn.setIcon(FontAwesome.SAVE); 
        saveBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        saveBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick ( Button.ClickEvent event )
            {
                saveProject();
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
    
    private Component buildTab1() {
        VerticalLayout tab1Layout = new VerticalLayout();
        tab1Layout.setSpacing(true);
        tab1Layout.setMargin(true);
        tab1Layout.setWidth("100%");
        
        proyectoForm  = new FormLayout();
        proyectoForm.setMargin(marginInfo);
        proyectoForm.setSpacing(false);
        proyectoForm.addStyleName(ValoTheme.FORMLAYOUT_LIGHT);
        proyectoForm.setWidth("60%");

        idProyectoTxt = new TextField("Id Proyecto : ");
//        idProyectoTxt.setWidth("8em");
        idProyectoTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);
        idProyectoTxt.setReadOnly(true);

        nombreTxt = new TextField("Nombre : ");
        nombreTxt.setWidth("100%");
        nombreTxt.setRequired(true);
        nombreTxt.setRequiredError("POR FAVOR INGRESE EL NOMBRE DEL PROYECTO");

        paisCbx = new ComboBox("Pais : ");
//        paisCbx.setWidth("10em");
        paisCbx.setNewItemsAllowed(false);
        paisCbx.setInvalidAllowed(false);
        paisCbx.setTextInputAllowed(false);
        
        llenarComboPais();

        ubicacionTxt = new TextField("Ubicación : ");
        ubicacionTxt.setWidth("100%");
        ubicacionTxt.setRequired(true);
        ubicacionTxt.setRequiredError("POR FAVOR INGRESE LA UBICACION DEL PROYECTO");

        responsableTxt = new TextField("Responsable : ");
//        responsableTxt.setWidth("10em");

        telefonoTxt = new TextField("Teléfono : ");
//        telefonoTxt.setWidth("10em");
        telefonoTxt.setRequired(true);
        telefonoTxt.setRequiredError("POR FAVOR INGRESE EL TELEFONO DEL PROYECTO");

        sitioWebTxt = new TextField("Sitio web : ");

        fechaInicioDt = new PopupDateField("Fecha inicio : ");      
        fechaInicioDt.setValue(new java.util.Date());        
        fechaInicioDt.setResolution(Resolution.DAY);
//        fechaInicioDt.setWidth("125px");
        fechaInicioDt.setDateFormat("dd/MM/yyyy");

        fechaFinDt = new PopupDateField("Fecha fin : ");        
        fechaFinDt.setValue(new java.util.Date());        
        fechaFinDt.setResolution(Resolution.DAY);
//        fechaFinDt.setWidth("125px");
        fechaFinDt.setDateFormat("dd/MM/yyyy");

        diasAntesHoyFechaTareaProgramadaTxt = new NumberField("Dias Antes Hoy Fecha Tarea Programada :");
        diasAntesHoyFechaTareaProgramadaTxt.setWidth("8em");
        diasAntesHoyFechaTareaProgramadaTxt.setDecimalPrecision(0);
        diasAntesHoyFechaTareaProgramadaTxt.setValue(-7.0d);
        diasAntesHoyFechaTareaProgramadaTxt.setImmediate(true);
        diasAntesHoyFechaTareaProgramadaTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);
        diasAntesHoyFechaTareaProgramadaTxt.addStyleName(ValoTheme.TEXTFIELD_SMALL);

        diasDespuesHoyFechaTareaProgramadaTxt = new NumberField("Dias Despues Hoy Fecha Tarea Programada :");
        diasDespuesHoyFechaTareaProgramadaTxt.setWidth("8em");
        diasDespuesHoyFechaTareaProgramadaTxt.setDecimalPrecision(0);
        diasDespuesHoyFechaTareaProgramadaTxt.setValue(7.0d);
        diasDespuesHoyFechaTareaProgramadaTxt.setImmediate(true);
        diasDespuesHoyFechaTareaProgramadaTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);
        diasDespuesHoyFechaTareaProgramadaTxt.addStyleName(ValoTheme.TEXTFIELD_SMALL);

        passwordTxt = new PasswordField("Clave para autorizar :");
        passwordTxt.setIcon(FontAwesome.LOCK);
        passwordTxt.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
        
        factorPresupuestoClienteTxt = new NumberField("% Factor presupuesto cliente :");
        factorPresupuestoClienteTxt.setWidth("8em");
        factorPresupuestoClienteTxt.setDecimalPrecision(2);
        factorPresupuestoClienteTxt.setDecimalSeparator('.');
        factorPresupuestoClienteTxt.setValue(0.00d);
        factorPresupuestoClienteTxt.setGroupingUsed(true);
        factorPresupuestoClienteTxt.setGroupingSeparator(',');
        factorPresupuestoClienteTxt.setImmediate(true);
        factorPresupuestoClienteTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        factorPresupuestoClienteTxt.addStyleName(ValoTheme.TEXTFIELD_SMALL);

        estatusCbx = new ComboBox("Estatus : ");
        estatusCbx.setNewItemsAllowed(false);
        estatusCbx.setInvalidAllowed(false);
        estatusCbx.addItem("ACTIVO");
        estatusCbx.addItem("INACTIVO");
        estatusCbx.select("ACTIVO");

        proyectoForm.addComponent(idProyectoTxt);
        proyectoForm.addComponent(nombreTxt);
        proyectoForm.addComponent(paisCbx);
        proyectoForm.addComponent(ubicacionTxt);
        proyectoForm.addComponent(responsableTxt);
        proyectoForm.addComponent(telefonoTxt);
        proyectoForm.addComponent(sitioWebTxt);
        proyectoForm.addComponent(fechaInicioDt);
        proyectoForm.addComponent(fechaFinDt);
        proyectoForm.addComponent(passwordTxt);
        proyectoForm.addComponent(factorPresupuestoClienteTxt);
        proyectoForm.addComponent(diasAntesHoyFechaTareaProgramadaTxt);
        proyectoForm.addComponent(diasDespuesHoyFechaTareaProgramadaTxt);
        proyectoForm.addComponent(estatusCbx);
        
        tab1Layout.addComponent(proyectoForm);
        tab1Layout.setComponentAlignment(proyectoForm, Alignment.MIDDLE_CENTER);
        
        nombreTxt.focus();
        
        return tab1Layout;
    }
    
     private Component buildTab5() {
        
        UploadFinishedHandler handler;
        handler = new UploadFinishedHandler() {
            @Override
            public void handleFile(InputStream stream, String fileName, String mimeType, long length) {
                
                File targetFile;
                
                try {
                    
System.out.println("\nfileName="+fileName);
System.out.println("length="+stream.available());
System.out.println("mimeType="+mimeType);
                
                    byte[] buffer = new byte[stream.available()];
                    stream.read(buffer);
                    String filePath = VaadinService.getCurrent()
                            .getBaseDirectory().getAbsolutePath() + "/projectfiles/" + ((SopdiUI) mainUI).sessionInformation.getStrCompanyId() + "/";
                    
                    new File(filePath).mkdirs();
                    
                    fileName = filePath + fileName;
                    targetFile = new File(fileName);
                    OutputStream outStream = new FileOutputStream(targetFile);
                    outStream.write(buffer);
                    outStream.close();
                    stream.close();
                    
System.out.println("\ntargetFile = " + fileName);

                    logoStreamResource = null;

                    if(buffer != null ) {
                        logoStreamResource = new StreamResource(
                            new StreamResource.StreamSource() {
                                public InputStream getStream() {
                                    return new ByteArrayInputStream(buffer);
                                }
                            },idProyectoTxt.getValue()
                        );
                    }
                    logoImage.setSource(logoStreamResource);
                    file = targetFile;
                }
                catch( java.io.IOException fIoEx) {
                    fIoEx.printStackTrace();
                    Notification.show("Error al cargar el archivo adjunto!", Notification.Type.ERROR_MESSAGE);
                    return;
                }                
            }
        };

        UploadStateWindow window = new UploadStateWindow();

        singleUpload = new MultiFileUpload(handler,window, false);
//        singleUpload.setCaption("Cargar archivo");
//        singleUpload2.setRootDirectory(newDoc.getAbsolutePath());
//        singleUpload.setPanelCaption("Buscar");
        singleUpload.setIcon(FontAwesome.UPLOAD);
        singleUpload.setImmediate(true);
        singleUpload.getSmartUpload().setUploadButtonCaptions("Cargar logo del proyecto", "");
        
        JavaScript.getCurrent().execute("document.getElementsByClassName('gwt-FileUpload')[0].setAttribute('accept', '.png')");
        JavaScript.getCurrent().execute("document.getElementsByClassName('gwt-FileUpload')[1].setAttribute('accept', '.jpg')");

/**
        List<String> acceptedMimeTypes = new ArrayList();  
        acceptedMimeTypes.add("application/octet-stream");
        acceptedMimeTypes.add("application/ms-project");
        acceptedMimeTypes.add("application/vnd.ms-project");
        acceptedMimeTypes.add("application/msproj");
        acceptedMimeTypes.add("application/msproject");
        acceptedMimeTypes.add("application/x-msproject");
        acceptedMimeTypes.add("application/x-ms-project");
        acceptedMimeTypes.add("application/x-dos_ms_project");
        acceptedMimeTypes.add("application/mpp");
        acceptedMimeTypes.add("zz-application/zz-winassoc-mpp");
//        singleUpload.setAcceptedMimeTypes(acceptedMimeTypes);
**/
        
        logoImage = new Image();
        logoImage.setImmediate(true);
        logoImage.setWidth("300px");
        logoImage.setHeight("300px"); 
        logoImage.setIcon(FontAwesome.IMAGE);
//        logoImage.addStyleName("menu-logo-empresa");
        
        VerticalLayout pictureLayout = new VerticalLayout();
        pictureLayout.setWidth("100%");
        pictureLayout.setSpacing(true);
        pictureLayout.setMargin(true);
        pictureLayout.addComponent(logoImage);
        pictureLayout.addComponent(singleUpload);
        pictureLayout.setComponentAlignment(logoImage, Alignment.TOP_CENTER);
        pictureLayout.setComponentAlignment(singleUpload, Alignment.BOTTOM_CENTER);
        
        return pictureLayout;
        
    }    
   
    void llenarComboPais() {
        String queryString = "Select * ";
        queryString += " From pais ";
 
        paisCbx.removeAllItems();

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords = stQuery.executeQuery (queryString);
                        
            while(rsRecords.next()) { //  encontrado                
                paisCbx.addItem(rsRecords.getString("IdPais"));
                paisCbx.setItemCaption(rsRecords.getString("IdPais"), rsRecords.getString("Nombre"));
            }
            
            if(rsRecords.first()) {
                paisCbx.select(rsRecords.getString("IdPais"));
            }
        }
        catch(Exception ex1) {
            Notification.show("ERROR FATAL DEL SISTEMA", Notification.Type.ERROR_MESSAGE);
            System.out.println("ERROR AL INTENTAR BUSCAR CATALOGO DE PAIS : " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void fillData() {
        String queryString = "";
        
        queryString =  "SELECT * ";
        queryString += " FROM  proyecto ";
        queryString += " WHERE IdProyecto = " + idProyectoTxt.getValue();

//System.out.println("\n\n"+queryString);

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();            
            rsRecords = stQuery.executeQuery (queryString);

            if(rsRecords.next()) { //  encontrado

                nombreTxt.setValue(rsRecords.getString("Nombre"));
                paisCbx.select(rsRecords.getString("IdPais"));
                ubicacionTxt.setValue(rsRecords.getString("Ubicacion"));
                responsableTxt.setValue(rsRecords.getString("Responsable"));
                telefonoTxt.setValue(rsRecords.getString("TelefonoResponsable"));
                sitioWebTxt.setValue(rsRecords.getString("SitioWeb"));
                fechaInicioDt.setValue(rsRecords.getDate("FechaInicio"));
                fechaFinDt.setValue(rsRecords.getDate("FechaFin"));
                factorPresupuestoClienteTxt.setValue(rsRecords.getDouble("FactorPresupuestoCliente"));
                diasAntesHoyFechaTareaProgramadaTxt.setValue(rsRecords.getString("DiasAntesHoyFechaTareaProgramada"));
                diasDespuesHoyFechaTareaProgramadaTxt.setValue(rsRecords.getString("DiasDespuesHoyFechaTareaProgramada"));
                estatusCbx.select(rsRecords.getString("Estatus"));
                
                final byte docBytes[] = rsRecords.getBytes("Logo");
                StreamResource logoStreamResource = null;

                if(docBytes != null ) {
                    logoStreamResource = new StreamResource(
                        new StreamResource.StreamSource() {
                            public InputStream getStream() {
                                return new ByteArrayInputStream(docBytes);
                            }
                        },rsRecords.getString("IdProyecto")
                    );
                }
                logoImage.setSource(logoStreamResource);
             }
        } 
        catch (Exception ex) {
            Logger.getLogger(ProjectForm.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de proyectos : " + ex.getMessage());
            Notification.show("Error al intentar leer registros de proyectos..!", Notification.Type.ERROR_MESSAGE);
        } 
    }

    private void saveProject() {
        
        if(nombreTxt.getValue().trim().isEmpty()) {
            Notification.show("Por favor, ingrese el nombre!", Notification.Type.WARNING_MESSAGE);
            nombreTxt.focus();
            return;
        }
        if(ubicacionTxt.getValue().trim().isEmpty()) {
            Notification.show("Por favor, ingrese la ubicación!", Notification.Type.WARNING_MESSAGE);
            ubicacionTxt.focus();
            return;
        }
        if(responsableTxt.getValue().trim().isEmpty()) {
            Notification.show("Por favor, ingrese el nombre del responsable!", Notification.Type.WARNING_MESSAGE);
            telefonoTxt.focus();
            return;
        }
        if(telefonoTxt.getValue().trim().isEmpty()) {
            Notification.show("Por favor, ingrese el teléfono!", Notification.Type.WARNING_MESSAGE);
            telefonoTxt.focus();
            return;
        }
        
        if(fechaFinDt.getValue() != null) {
            if(fechaFinDt.getValue().before(fechaInicioDt.getValue())) {
                Notification.show("La fecha final no puede ser menor que la fecha de inicio!", Notification.Type.ERROR_MESSAGE);
                fechaFinDt.focus();
                return;                
            }
        }

        ByteArrayInputStream inputStream1 = null;
        
        String queryString;
        
        if(idProyectoTxt.getValue().compareTo("0") == 0) {
            queryString =  "INSERT INTO proyecto (Nombre, Ubicacion, IdEmpresa, IdPais,";
            queryString += " Responsable, TelefonoResponsable, SitioWeb, UnidadMedida, PrecioUnidadMedida, MontoReserva, PorcentajeEnganche, ";
            queryString += " FechaInicio, FechaFin, DiasAntesHoyFechaTareaProgramada, DiasDespuesHoyFechaTareaProgramada,";
            queryString += " ClaveParaAutorizar, FactorPresupuestoCliente, Logo)";
            queryString += " VALUES (";
            queryString += "'"  + nombreTxt.getValue()      + "'";
            queryString += ",'" + ubicacionTxt.getValue()   + "'";
            queryString += ", " + ((SopdiUI) mainUI).sessionInformation.getStrCompanyId();
            queryString += ", '" + String.valueOf(paisCbx.getValue()) + "'";
            queryString += ",'" + responsableTxt.getValue()    + "'";
            queryString += ",'" + telefonoTxt.getValue()    + "'";
            queryString += ",'" + sitioWebTxt.getValue()    + "'";
            queryString += ",'" + new Utileria().getFechaYYYYMMDD_1(fechaInicioDt.getValue()) + "'";
            if(fechaFinDt.getValue() != null) {
                queryString += ",'" + new Utileria().getFechaYYYYMMDD_1(fechaFinDt.getValue()) + "'";
            }
            else {
                queryString += ",null";
            }
            queryString += ", " + diasAntesHoyFechaTareaProgramadaTxt.getValue();
            queryString += ", " + diasDespuesHoyFechaTareaProgramadaTxt.getValue();
            queryString += ",?";
            queryString += ",Sha1('" + passwordTxt.getValue()       + "')";
            queryString += "," + factorPresupuestoClienteTxt.getDoubleValueDoNotThrow();
            queryString += ")";
        }
        else {
            queryString =  "UPDATE proyecto SET ";
            queryString += " Nombre       = '" + nombreTxt.getValue()         + "'";
            queryString += ",Ubicacion    = '" + ubicacionTxt.getValue()      + "'";
            queryString += ",IdPais       = '" + String.valueOf(paisCbx.getValue()) + "'";
            queryString += ",Responsable  = '" + responsableTxt.getValue()       + "'";
            queryString += ",TelefonoResponsable = '" + telefonoTxt.getValue()       + "'";
            queryString += ",SitioWeb     = '" + sitioWebTxt.getValue()       + "'";
            queryString += ",FechaInicio = '" + new Utileria().getFechaYYYYMMDD_1(fechaInicioDt.getValue()) + "'";
            if(fechaFinDt.getValue() != null) {
                queryString += ",FechaFin    = '" + new Utileria().getFechaYYYYMMDD_1(fechaFinDt.getValue()) + "'";
            }
            queryString += ",DiasAntesHoyFechaTareaProgramada = " + diasAntesHoyFechaTareaProgramadaTxt.getValue();
            queryString += ",DiasDespuesHoyFechaTareaProgramada = " + diasDespuesHoyFechaTareaProgramadaTxt.getValue();
            if(!passwordTxt.getValue().trim().isEmpty()) {
                queryString += ",ClaveParaAutorizar = Sha1('" + passwordTxt.getValue()  + "')";
            }
            queryString += ",FactorPresupuestoCliente = " + factorPresupuestoClienteTxt.getDoubleValueDoNotThrow();
            queryString += ",Logo = ?";
            
            queryString += " WHERE IdProyecto = " + idProyectoTxt.getValue();
        }
//System.out.println("queryString="+queryString);
        try {
            stPreparedQuery  = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().prepareStatement(queryString);

            if(logoStreamResource != null) {
                
                stPreparedQuery.setBinaryStream(1, logoStreamResource.getStream().getStream(), logoStreamResource.getStream().getStream().available());
//                    receiver.file.delete(); 
            }
            else {
                stPreparedQuery.setBinaryStream(1, null, 0);                
            }

System.out.println("preparedQuery="+stPreparedQuery.toString());

            stPreparedQuery.execute();                
               
            Notification.show("OPERACION EXITOSA!", Notification.Type.HUMANIZED_MESSAGE);

//            if(mainUI.getNavigator().getCurrentView().getClass().getSimpleName().compareTo("ProyectosView") == 0) {
//                ((ProjectsView)(mainUI.getNavigator().getCurrentView())).fillProyectoTable();
//            }            
        }
        catch(Exception ex)
        {
            Notification.show("Error al actualizar proyecto : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }    

    // Implement both receiver that saves upload in a file and
    // listener for successful upload
    class ImageUploader implements Upload.Receiver, Upload.SucceededListener {
        public File file;
        public FileOutputStream fos = null; // Stream to write to
        public FileInputStream fis = null;
        @Override
        public OutputStream receiveUpload(String filename,
            String mimeType) {
            // Create upload stream
            try {
                // Open the file for writing.
                
                new File(VaadinService.getCurrent().getBaseDirectory().getAbsolutePath() + "/WEB-INF/LOGOS/" + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrCompanyId()).mkdirs();
                file = new File(VaadinService.getCurrent().getBaseDirectory().getAbsolutePath() + "/WEB-INF/LOGOS/" + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrCompanyId() + "/" + filename);
System.out.println("\nfile="+file.getAbsolutePath());
                fos = new FileOutputStream(file);
            } catch (final java.io.FileNotFoundException e) {
                new Notification("El archivo se puede abrir o leer",
                e.getMessage(),
                Notification.Type.ERROR_MESSAGE)
                .show(Page.getCurrent());
                return null;
            }
            return fos; // Return the output stream to write to
        }
        @Override
        public void uploadSucceeded(Upload.SucceededEvent event) {
            Notification.show("Archivo cargado con exito!", Notification.Type.TRAY_NOTIFICATION);
            logoImage.setSource(new FileResource(file));
            try {
                fis = new FileInputStream(file);
                //file.delete();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(UserPreferences.class.getName()).log(Level.SEVERE, null, ex);
            }
            //resize form
        }
    };
}