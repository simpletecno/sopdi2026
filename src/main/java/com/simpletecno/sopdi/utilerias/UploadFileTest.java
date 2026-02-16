package com.simpletecno.sopdi.utilerias;

import com.vaadin.server.FileResource;
import com.vaadin.server.Page;
import com.vaadin.ui.Image;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author joseaguirre
 */
public class UploadFileTest extends Window {
    
    final Image image = new Image("Uploaded Image");
    VerticalLayout mainLayout = new VerticalLayout();
    
    public UploadFileTest() {

        mainLayout.addComponent(image);
        
        setWidth("60%");
        setHeight("60%");
        setContent(mainLayout);
        center();
        
        class ImageUploader implements Receiver, SucceededListener {
            public File file;

            private int counter;
            private int total;
            private boolean sleep;
            
            @Override
            public OutputStream receiveUpload(String fileName,
                                              String mimeType) {
                FileOutputStream fos = null; // Stream to write to
                try {
                    // Open the file for writing.
                    file = new File("/Users/joseaguirre/Downloads/" + fileName);
                    fos = new FileOutputStream(file);
                } catch (final java.io.FileNotFoundException e) {
                    new Notification("Could not open file<br/>",
                                     e.getMessage(),
                                     Notification.Type.ERROR_MESSAGE)
                        .show(Page.getCurrent());
                    return null;
                }
                return fos; // Return the output stream to write to
            }
            
            public void uploadSucceeded(SucceededEvent event) {                
                image.setSource(new FileResource(file));
            }
        }
        
        ImageUploader receiver = new ImageUploader();

        // Create the upload with a caption and set receiver later
        Upload upload = new Upload("Upload Image Here", receiver);
        upload.addSucceededListener(receiver);
        
        mainLayout.addComponent(upload);
     }
}
