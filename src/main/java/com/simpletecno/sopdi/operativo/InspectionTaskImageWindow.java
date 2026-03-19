/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.operativo;

import com.simpletecno.sopdi.utilerias.DocumentWindow;
import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.StreamResource;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.MultiFileUpload;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadFinishedHandler;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadStateWindow;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 *
 * @author JAguirre
 */
public class InspectionTaskImageWindow extends Window {

    public static Statement stQuery;
    public static PreparedStatement stPreparedQuery;
    public static ResultSet rsRecords;    
    
    public String queryString;

    VerticalLayout mainLayout = new VerticalLayout();
    HorizontalLayout imageLayout;    
    MultiFileUpload singleUpload;
    File file;
    StreamResource logoStreamResource = null;

    String idVisitaInspeccionTarea;
    String codigoTarea;
    String descripcion;
    boolean editar = false;
                    
    public InspectionTaskImageWindow(
            String idVisitaInspeccionTarea,
            String codigoTarea,
            String descripcion,
            boolean editar)
    {
        this.idVisitaInspeccionTarea = idVisitaInspeccionTarea;
        this.codigoTarea = codigoTarea;
        this.descripcion = descripcion;
        this.editar = editar;
        
        Label titleLbl = new Label("Imagenes de la tarea : " + codigoTarea + " " + descripcion);
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h3_custom");
        
        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setWidth("100%");
        
        titleLayout.addComponents(titleLbl);
        titleLayout.setComponentAlignment(titleLbl,  Alignment.TOP_LEFT);
                
        imageLayout = new HorizontalLayout();
        imageLayout.setSpacing(true);
        imageLayout.setMargin(true);
        imageLayout.setSizeUndefined();
        imageLayout.addStyleName(ValoTheme.LAYOUT_HORIZONTAL_WRAPPING);

        mainLayout.addComponent(titleLayout);
        mainLayout.setComponentAlignment(titleLayout, Alignment.TOP_CENTER);
        mainLayout.addComponent(imageLayout);
        mainLayout.setComponentAlignment(imageLayout, Alignment.TOP_CENTER);
        
        setContent(mainLayout);
        
        fillInspectionTaskImages();
        
    }
    
    private void fillInspectionTaskImages() {
        
        imageLayout.removeAllComponents();
        
        try {
            
            queryString = "SELECT * ";
            queryString += " FROM visita_inspeccion_tarea_imagen";
            queryString += " WHERE IdVisitaInspeccionTarea = " + idVisitaInspeccionTarea;

            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery (queryString);

            if(rsRecords.next()) {

                Panel panel;
                VerticalLayout contentLayout;

                do {

                    panel = new Panel("<strong>Imagen de tarea : </strong>" + codigoTarea);
                    panel.setCaptionAsHtml(true);
                    panel.setIcon(FontAwesome.PICTURE_O);
                    panel.addStyleName("colorApp");

                    contentLayout = new VerticalLayout();

                    final byte docBytes[] = rsRecords.getBytes("Archivo");
                    StreamResource logoStreamResource = null;

                    if(docBytes != null ) {
                        logoStreamResource = new StreamResource(
                            new StreamResource.StreamSource() {
                                @Override
                                public InputStream getStream() {
                                    return new ByteArrayInputStream(docBytes);
                                }
                            },rsRecords.getString("IdVisitaInspeccionTareaImagen")
                        );
                    }

                    Image image1 = new Image("",logoStreamResource);
                    image1.setWidth("200px");
                    image1.setHeight("200px"); 
                    image1.setData(rsRecords.getInt("IdVisitaInspeccionTareaImagen"));

                    Button imageBtn = new Button("");
                    imageBtn.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
                    imageBtn.setData(rsRecords.getInt("IdVisitaInspeccionTareaImagen"));
                    imageBtn.setIcon(FontAwesome.DESKTOP);
                    imageBtn.setDescription("Click para visualizar imagen");
                    imageBtn.addClickListener((Button.ClickEvent event) -> {
                        DocumentWindow documentWindow = new DocumentWindow(String.valueOf(event.getButton().getData()));
                    });
                    
                    Button deleteImageBtn = new Button("");
                    deleteImageBtn.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
                    deleteImageBtn.setData(rsRecords.getInt("IdVisitaInspeccionTareaImagen"));
                    deleteImageBtn.setIcon(FontAwesome.TRASH);
                    deleteImageBtn.setDescription("Eliminar");
                    deleteImageBtn.addClickListener((Button.ClickEvent event) -> {
                        deleteInspeccionImagen(String.valueOf(event.getButton().getData()));
                    });

                    deleteImageBtn.setEnabled(editar);
                    
                    HorizontalLayout optionsLayout = new HorizontalLayout();
                    
                    optionsLayout.setWidth("100%");
                    optionsLayout.setSpacing(true);
                    optionsLayout.addComponents(imageBtn, deleteImageBtn);
                    optionsLayout.setComponentAlignment(imageBtn, Alignment.BOTTOM_LEFT);
                    optionsLayout.setComponentAlignment(deleteImageBtn, Alignment.BOTTOM_RIGHT);

                    contentLayout.addComponent(image1);
                    contentLayout.addComponent(optionsLayout);

                    panel.setContent(contentLayout);

                    imageLayout.addComponent(panel);                        

                } while(rsRecords.next());
                
                if(contentLayout == null) {
                    contentLayout = new VerticalLayout();
                }

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
                                    .getBaseDirectory().getAbsolutePath() + "/projectfiles/" + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrCompanyId() + "/";

                            new File(filePath).mkdirs();

                            fileName = filePath + fileName;
                            targetFile = new File(fileName);
                            OutputStream outStream = new FileOutputStream(targetFile);
                            outStream.write(buffer);
                            outStream.close();

                            long fileSize = stream.available();

                            stream.close();

        System.out.println("\ntargetFile = " + fileName);

                            logoStreamResource = null;

                            if(buffer != null ) {
                                logoStreamResource = new StreamResource(
                                    new StreamResource.StreamSource() {
                                        public InputStream getStream() {
                                            return new ByteArrayInputStream(buffer);
                                        }
                                    },idVisitaInspeccionTarea
                                );
                            }

                            insertarImagen(targetFile.getName(), mimeType, fileSize);

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
                singleUpload.setIcon(FontAwesome.UPLOAD);
                singleUpload.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);
                singleUpload.setImmediate(true);
                singleUpload.getSmartUpload().setUploadButtonCaptions("Cargar nueva Fotografia/Imagen", "");
                singleUpload.setEnabled(editar);

                JavaScript.getCurrent().execute("document.getElementsByClassName('gwt-FileUpload')[0].setAttribute('accept', '.png')");
                JavaScript.getCurrent().execute("document.getElementsByClassName('gwt-FileUpload')[1].setAttribute('accept', '.jpg')");
                JavaScript.getCurrent().execute("document.getElementsByClassName('gwt-FileUpload')[2].setAttribute('accept', '.jpeg')");

                HorizontalLayout pictureLayout = new HorizontalLayout();
                pictureLayout.addComponent(singleUpload);
                pictureLayout.setComponentAlignment(singleUpload, Alignment.BOTTOM_CENTER);
                pictureLayout.setWidth("100%");

                if(editar) contentLayout.addComponent(pictureLayout);
            }
            else {
                Panel panel;
                VerticalLayout contentLayout;

                panel = new Panel("<strong>Imagen de tarea : </strong>" + codigoTarea);
                panel.setCaptionAsHtml(true);
                panel.setIcon(FontAwesome.PICTURE_O);
                panel.addStyleName("colorApp");

                contentLayout = new VerticalLayout();

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
                                    .getBaseDirectory().getAbsolutePath() + "/projectfiles/" + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrCompanyId() + "/";

                            new File(filePath).mkdirs();

                            fileName = filePath + fileName;
                            targetFile = new File(fileName);
                            OutputStream outStream = new FileOutputStream(targetFile);
                            outStream.write(buffer);
                            outStream.close();

                            long fileSize = stream.available();

                            stream.close();

        System.out.println("\ntargetFile = " + fileName);

                            logoStreamResource = null;

                            if(buffer != null ) {
                                logoStreamResource = new StreamResource(
                                    new StreamResource.StreamSource() {
                                        public InputStream getStream() {
                                            return new ByteArrayInputStream(buffer);
                                        }
                                    },idVisitaInspeccionTarea
                                );
                            }

                            insertarImagen(targetFile.getName(), mimeType, fileSize);

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
                singleUpload.setIcon(FontAwesome.UPLOAD);
                singleUpload.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);
                singleUpload.setImmediate(true);
                singleUpload.getSmartUpload().setUploadButtonCaptions("Cargar nueva Fotografia/Imagen", "");
                singleUpload.setEnabled(editar);

                JavaScript.getCurrent().execute("document.getElementsByClassName('gwt-FileUpload')[0].setAttribute('accept', '.png')");
                JavaScript.getCurrent().execute("document.getElementsByClassName('gwt-FileUpload')[1].setAttribute('accept', '.jpg')");
                JavaScript.getCurrent().execute("document.getElementsByClassName('gwt-FileUpload')[2].setAttribute('accept', '.jpeg')");

                HorizontalLayout pictureLayout = new HorizontalLayout();
                pictureLayout.addComponent(singleUpload);
                pictureLayout.setComponentAlignment(singleUpload, Alignment.BOTTOM_CENTER);
                pictureLayout.setWidth("100%");

                if(editar) contentLayout.addComponent(pictureLayout);
                panel.setContent(contentLayout);
                imageLayout.addComponent(panel);
            }
        }
        catch(Exception allEx) {
            Notification.show("Error al intentar mostrar las imagenes/fotos.", Notification.Type.ERROR_MESSAGE);
            allEx.printStackTrace();
        }
 
        setWidth("80%");
        setHeight("80%");
        this.fireResize();

    }
        
    public void insertarImagen(String fileName, String mimeType, long peso) {
        if(logoStreamResource == null) {
            return;
        }
                
        String queryString;
        
        queryString =  "INSERT INTO visita_inspeccion_tarea_imagen (IdVisitaInspeccionTarea, Archivo, ArchivoTipo, ArchivoPeso, ArchivoNombre)";
        queryString += " VALUES (";
        queryString += " "  + idVisitaInspeccionTarea;
        queryString += ",?";
        queryString += ",'" + mimeType + "'";
        queryString += ", " + peso;
        queryString += ",'" + fileName + "'";
        queryString += ")";
                
        try {
            stPreparedQuery  = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().prepareStatement(queryString, Statement.RETURN_GENERATED_KEYS);
            stPreparedQuery.setBinaryStream(1, logoStreamResource.getStream().getStream(), logoStreamResource.getStream().getStream().available());
            
//System.out.println("preparedQuery="+stPreparedQuery.toString());

            stPreparedQuery.executeUpdate();
            rsRecords = stPreparedQuery.getGeneratedKeys();

            rsRecords.next();

            int newImagenId = rsRecords.getInt(1);
            this.fillInspectionTaskImages();
            
            Notification.show("OPERACION EXITOSA!", Notification.Type.HUMANIZED_MESSAGE);
            
            for (Window subWindow : ((SopdiUI) UI.getCurrent()).getWindows()) {
//System.out.println("windows caption = " + subWindow.getCaption());
                if(subWindow.getCaption().equals("Tareas de visita o reunion")) {
//System.out.println("encontrado = " + subWindow.getCaption() + " selectedItemId = " + ((InspectionTasksWindow)subWindow).tareasGrid.getEditedItemId());
                    ((InspectionTasksWindow)subWindow).tareasContainer. getContainerProperty(((InspectionTasksWindow)subWindow).tareasGrid.getEditedItemId(), "Fotografía").setValue("SI");
                    ((InspectionTasksWindow)subWindow).tareasGrid.refreshRows(((InspectionTasksWindow)subWindow).tareasGrid.getEditedItemId());
                    break;
                }
            }
        }
        catch(Exception excep1) {
            Notification.show("Error al insertar imagen de modelo de construcción : " + excep1.getMessage(), Notification.Type.ERROR_MESSAGE);
            excep1.printStackTrace();
        }
        
    }
    
    public void deleteInspeccionImagen(String idVisitaInspeccionTareaImagen) {
        String queryString;
        
        queryString =  "DELETE FROM visita_inspeccion_tarea_imagen";
        queryString += " WHERE IdVisitaInspeccionTareaImagen = " + idVisitaInspeccionTareaImagen;
        
//System.out.println("deeteImagenQuery="+queryString);
                
        try {
            stQuery  = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            this.fillInspectionTaskImages();
            
            Notification.show("OPERACION EXITOSA!", Notification.Type.HUMANIZED_MESSAGE);
        }
        catch(Exception excep1) {
            Notification.show("Error al elininar imagen o foto de visita por inspección : " + excep1.getMessage(), Notification.Type.ERROR_MESSAGE);
            excep1.printStackTrace();
            return;
        }        
    }
    
}
