/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.utilerias;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Image;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 *
 * @author JAguirre
 */
public class DocumentWindow  {

    public static Statement stQuery;
    public static PreparedStatement stPreparedQuery;
    public static ResultSet rsRecords;    
    
    public String queryString;
    public String fileName;
    public String documentoId;
                    
    public DocumentWindow(String documentoId)
    {
        this.documentoId = documentoId;
        try {
            
            queryString = "Select * ";
            queryString += " From visita_inspeccion_tarea_imagen";
            queryString += " Where IdVisitaInspeccionTareaImagen = " + documentoId;

            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery (queryString);

            if(!rsRecords.next()) { //  no encontrado  
                Notification.show("Documento no se puede visualizar!", Notification.Type.ERROR_MESSAGE);
                return;
            }
            
            Window window = new Window();
            window.setResizable(true);
            window.setWidth("95%");
            window.setHeight("95%");
            
            final byte docBytes[] = rsRecords.getBytes("Archivo");
            StreamResource documentStreamResource = null;

            if(docBytes != null ) {
                documentStreamResource = new StreamResource(
                    new StreamResource.StreamSource() {
                        public InputStream getStream() {
                            return new ByteArrayInputStream(docBytes);
                        }
                    },rsRecords.getString("IdVisitaInspeccionTareaImagen")
                );
            }
            documentStreamResource.setMIMEType(rsRecords.getString("ArchivoTipo"));
            documentStreamResource.setFilename(rsRecords.getString("ArchivoNombre"));
            
            if(rsRecords.getString("ArchivoTipo").contains("pdf")) {
                        
                BrowserFrame browserFrame = new BrowserFrame();
                browserFrame.setSizeFull();

                browserFrame.setSource(documentStreamResource); 
/*
                if(rsRecords.getString("Tipo").contains("image")) {
                    browserFrame.setHeight("600px");
                    browserFrame.setWidth("600px");
                }
*/
                window.setContent(browserFrame);
            }
            else {
                window.setWidth("80%");
                window.setHeight("80%");
                VerticalLayout imageLayout = new VerticalLayout();
                imageLayout.setSizeFull();                
                
                Image imageViewComponent = new Image(null, documentStreamResource);
                imageViewComponent.setImmediate(true);
                imageViewComponent.setSizeFull();
//                imageViewComponent.setWidth("600px");
//                imageViewComponent.setHeight("600px");
                imageViewComponent.addStyleName("my-img-button");
                imageViewComponent.setData(rsRecords.getString("IdVisitaInspeccionTareaImagen"));
                
                imageLayout.addComponent(imageViewComponent);
                imageLayout.setComponentAlignment(imageViewComponent, Alignment.MIDDLE_CENTER);
                
                Panel imagePanel = new Panel();
                imagePanel.setResponsive(true);
                imagePanel.setContent(imageLayout);

                window.setContent(imagePanel);
            }
                
            UI.getCurrent().addWindow(window);
            window.center();                
            
        }
        catch(Exception allEx) {
            Notification.show("Error al intentar mostrar el archivo.", Notification.Type.ERROR_MESSAGE);
            allEx.printStackTrace();
        }
 
    }
        
}