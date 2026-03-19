/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.operativo;

import com.vaadin.server.StreamResource;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Window;

import java.io.*;

/**
 *
 * @author JAguirre
 */
public class PlanoShowPDF extends Window {

    String planoFile;

    public PlanoShowPDF(
            String planoFile
    ) {

        this.planoFile = planoFile;

        try {
            BrowserFrame e = new BrowserFrame();
            e.setSizeFull();
            setWidth("90%");
            setHeight("90%");
            center();

            StreamResource pdfResource;
            pdfResource = new StreamResource(new Pdf(planoFile), planoFile+System.currentTimeMillis());
            pdfResource.setMIMEType("application/pdf");

            e.setSource(pdfResource);
            setContent(e);

        } catch (Exception allEx) {
            Notification.show("Error al generar el balance de saldos.", Type.ERROR_MESSAGE);
            allEx.printStackTrace();
        }
    }

    public class Pdf implements StreamResource.StreamSource, Serializable {

        private final ByteArrayOutputStream os = new ByteArrayOutputStream();

        public Pdf(String fileName) {

            try {
                FileInputStream fileInputStream = new FileInputStream(fileName);

                int bytes;
                while ((bytes = fileInputStream.read()) != -1) {
                    os.write(bytes);
                }
            } catch (Exception ex) {
                Notification.show("Error al intentar leer PDF.." + ex.getMessage(), Type.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }

        @Override
        public InputStream getStream() {
            return new ByteArrayInputStream(os.toByteArray());
        }
    }
}
