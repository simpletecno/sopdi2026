/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.operativo;

import com.simpletecno.sopdi.configuracion.EnvironmentVars;
import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
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

/**
 *
 * @author user
 */
public class CargarDocumentoVisita extends Window {

    UI mainUI;

    MultiFileUpload singleUpload;
    public File file;
    StreamResource logoStreamResource = null;
    String parametro1, parametro2;
    Long parametro3;
    long fileSize;

    static PreparedStatement stPreparedQuery;

    Object selectedObjectUpdate;
    String codigoVisitaUpdate;
    
    public CargarDocumentoVisita(Object selectedObject, String codigoVisita) {
        this.mainUI = UI.getCurrent();
        this.selectedObjectUpdate = selectedObject;
        this.codigoVisitaUpdate = codigoVisita;

        setResponsive(true);
        setWidth("50%");
        setHeight("30%");
        
        EnvironmentVars enviromentsVars = new EnvironmentVars();
        
        Label titleLbl = new Label("CARGAR DOCUMENTO VISITA");
        titleLbl.addStyleName(ValoTheme.LABEL_H1);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h1_custom");

        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setSpacing(true);
        contentLayout.setMargin(true);
        contentLayout.addComponents(titleLbl);
        contentLayout.setComponentAlignment(titleLbl, Alignment.TOP_CENTER);
        
        UploadFinishedHandler handler;
        handler = new UploadFinishedHandler() {
            @Override
            public void handleFile(InputStream stream, String fileName, String mimeType, long length) {
                
                File targetFile;
                
                try {
                    if (!mimeType.contains("pdf")) {
                        return;
                    }
                    fileSize = stream.available();
                    byte[] buffer = new byte[stream.available()];
                    stream.read(buffer);

                    String filePath = enviromentsVars.getDtePath();

                    new File(filePath).mkdirs();

                    fileName = codigoVisitaUpdate + fileName.substring(fileName.length()-4, fileName.length());

                    targetFile = new File(filePath + fileName);
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

                    guardarArchivo(selectedObject, codigoVisita, fileName);
                        
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
        
        JavaScript.getCurrent().execute("document.getElementsByClassName('gwt-FileUpload')[0].setAttribute('accept', '.PDF')");
        
        HorizontalLayout componentsLayout = new HorizontalLayout();
        componentsLayout.setMargin(true);
        componentsLayout.setSpacing(true);
        
        componentsLayout.addComponents(singleUpload);
        contentLayout.addComponent(componentsLayout);
        contentLayout.setComponentAlignment(componentsLayout, Alignment.TOP_CENTER);
        
        setContent(contentLayout);
        
    }
    
    public void recibirParametrosImagen(String fileName, String mimeType, long peso) {
        parametro1 = fileName;
        parametro2 = mimeType;
        parametro3 = peso;
    }
    
    public void guardarArchivo(Object selectedObject, String codigoVisita, String fileName) {
           
        try {
            String queryString = " UPDATE visita_inspeccion SET  ";
            queryString += "  ArchivoNombre ='" + fileName + "'";
            queryString += ", ArchivoTipo ='" + parametro2 + "'";
            queryString += ", ArchivoPeso = " + parametro3;
            queryString += " WHERE CodigoVisita = '" + codigoVisita + "'";
            
            stPreparedQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().prepareStatement(queryString);
            stPreparedQuery.executeUpdate();
                        
            if (mainUI.getNavigator().getCurrentView().getClass().getSimpleName().equals("VisitasView")) {             
                ((VisitasView) (mainUI.getNavigator().getCurrentView())).visitasContainer.getContainerProperty(selectedObject, VisitasView.ARCHIVO_PROPERTY).setValue(fileName);
            }
            
            close();
            
        } catch (Exception ex) {
            System.out.println("Error al intentar insertar Imagen" + ex);
            Notification.show("Error al insertar la imagen : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
}