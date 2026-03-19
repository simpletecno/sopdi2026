/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.operativo;

import com.simpletecno.sopdi.configuracion.EnvironmentVars;
import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.renderers.ClickableRenderer;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.MultiFileUpload;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadFinishedHandler;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadStateWindow;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import org.vaadin.dialogs.ConfirmDialog;

/**
 *
 * @author user
 */
public class IngresoDocumentosVisitaInspeccion extends Window {

    MultiFileUpload singleUpload;
    public File file;
    StreamResource logoStreamResource = null;
    String parametro1, parametro2;
    Long parametro3;
    long fileSize;

    UI mainUI;
    Statement stQuery;
    ResultSet rsRecords;
    String queryString;
    static PreparedStatement stPreparedQuery;

    public IndexedContainer documentsContainer = new IndexedContainer();
    static final String ID_PROPERTY = "Id";
    static final String ID_VISITA = "NumeroDeVisita";
    static final String NOMBRE_DOCUMENTO = "NombreArchivo";
    static final String IMAGEN_PROPERTY = "Imagen";
    static final String ARCHIVO_TIPO_PROPERTY = "Tipo";
    static final String ELIMINAR_PROPERTY = "Eliminar";

    Grid documentosGrid;
    String codigoVisita;
    EnvironmentVars enviromentsVars;

    public IngresoDocumentosVisitaInspeccion(String codigoVisita) {
        enviromentsVars = new EnvironmentVars();

        this.codigoVisita = codigoVisita;

        VerticalLayout mainLayout;
        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);
        mainLayout.setResponsive(true);

        HorizontalLayout layoutTitle = new HorizontalLayout();
        layoutTitle.setSpacing(true);
        layoutTitle.setMargin(true);
        layoutTitle.setWidth("100%");

        this.mainUI = UI.getCurrent();
        setResponsive(true);
        setWidth("60%");
        setHeight("60%");

        setContent(mainLayout);

        documentsContainer.addContainerProperty(ID_PROPERTY, String.class, null);
        documentsContainer.addContainerProperty(ID_VISITA, String.class, null);
        documentsContainer.addContainerProperty(NOMBRE_DOCUMENTO, String.class, null);
        documentsContainer.addContainerProperty(IMAGEN_PROPERTY, String.class, null);
        documentsContainer.addContainerProperty(ARCHIVO_TIPO_PROPERTY, String.class, null);
        documentsContainer.addContainerProperty(ELIMINAR_PROPERTY, String.class, null);

        documentosGrid = new Grid("", documentsContainer);
        documentosGrid.setWidth("100%");
        documentosGrid.setImmediate(true);
        documentosGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        documentosGrid.setDescription("Seleccione un registro.");
        documentosGrid.setHeightMode(HeightMode.ROW);
        documentosGrid.setHeightByRows(7);
        documentosGrid.setResponsive(true);
        documentosGrid.setEditorBuffered(false);
        
        documentosGrid.getColumn(ID_PROPERTY).setHidable(true).setHidden(true);

        documentosGrid.getColumn(IMAGEN_PROPERTY).setRenderer(new ButtonRenderer(e -> {
            VerCambiarImagen(e);
        }));

        UploadFinishedHandler handler;
        handler = new UploadFinishedHandler() {
            @Override
            public void handleFile(InputStream stream, String fileName, String mimeType, long length) {

                File targetFile;

                try {
                    if (mimeType.contains("png") || mimeType.contains("jpeg") || mimeType.contains("jpg") || mimeType.contains("pdf")) {
                        System.out.println("\nfileName=" + fileName);
                        System.out.println("length=" + stream.available());
                        System.out.println("mimeType=" + mimeType);

                        fileSize = stream.available();
                        byte[] buffer = new byte[stream.available()];
                        stream.read(buffer);

                        String filePath = enviromentsVars.getDtePath();

                        new File(filePath).mkdirs();

//                        fileName = filePath + codigoPartidaUpdate + fileName.substring(fileName.length()-4, fileName.length());
                        int correlativo = (documentsContainer.size()+1);
                        fileName = filePath + codigoVisita + correlativo + fileName.substring(fileName.length() - 4, fileName.length());
                        System.out.println("fileName" + fileName);

                        targetFile = new File(fileName);
                        OutputStream outStream = new FileOutputStream(targetFile);
                        outStream.write(buffer);
                        outStream.close();

                        stream.close();

                        System.out.println("\ntargetFileToSave = " + fileName);

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

                        guardarArchivo1(codigoVisita, fileName);

                    } else {
                        Notification.show("El archivo no contiene un formato compatible. solo puede subir archivos con formato 'PNG','JEPG','JPG','PDF'", Notification.Type.ERROR_MESSAGE);
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
        JavaScript.getCurrent().execute("document.getElementsByClassName('gwt-FileUpload')[2].setAttribute('accept', '.PDF')");

        documentosGrid.getColumn(ELIMINAR_PROPERTY).setRenderer(new ButtonRenderer(e
                -> eliminarRegistroTabla(e)));

        mainLayout.addComponent(documentosGrid);
        mainLayout.setComponentAlignment(documentosGrid, Alignment.MIDDLE_CENTER);
        mainLayout.addComponent(singleUpload);
        mainLayout.setComponentAlignment(singleUpload, Alignment.MIDDLE_CENTER);

        llenarTabla();

    }

    public void llenarTabla() {
        String queryString = "";

        queryString = "SELECT *";
        queryString += " FROM visita_inspeccion_documento ";
        queryString += " WHERE IdVisitaInspeccion = " + codigoVisita;

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado                                                
                do {

                    Object itemId = documentsContainer.addItem();

                    documentsContainer.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords.getString("IdVisitaDocumento"));
                    documentsContainer.getContainerProperty(itemId, ID_VISITA).setValue(rsRecords.getString("IdVisitaInspeccion"));
                    documentsContainer.getContainerProperty(itemId, NOMBRE_DOCUMENTO).setValue(rsRecords.getString("ArchivoNombre"));
                    documentsContainer.getContainerProperty(itemId, ARCHIVO_TIPO_PROPERTY).setValue(rsRecords.getString("ArchivoTipo"));
                    if (rsRecords.getObject("ArchivoNombre") == null || rsRecords.getString("ArchivoNombre").trim().isEmpty()) {
                        documentsContainer.getContainerProperty(itemId, IMAGEN_PROPERTY).setValue("Cargar archivo");
                    } else {
                        documentsContainer.getContainerProperty(itemId, IMAGEN_PROPERTY).setValue("Visualizar");
                    }
                    documentsContainer.getContainerProperty(itemId, ELIMINAR_PROPERTY).setValue("Eliminar");

                } while (rsRecords.next());
            }
        } catch (Exception ex) {

        }
    }

    public void VerCambiarImagen(ClickableRenderer.RendererClickEvent e) {

        Object selectedObject = e.getItemId();
        String codigoVis = String.valueOf(documentsContainer.getContainerProperty(e.getItemId(), ID_VISITA).getValue());
        String archivoNombre = String.valueOf(documentsContainer.getContainerProperty(e.getItemId(), NOMBRE_DOCUMENTO).getValue()).trim();
        String archivoTipo = String.valueOf(documentsContainer.getContainerProperty(e.getItemId(), ARCHIVO_TIPO_PROPERTY).getValue());

        documentosGrid.select(e.getItemId());

        try {

            System.out.println("archivo nombre" +archivoNombre.trim());
            
            final byte docBytes[] = Files.readAllBytes(new File(archivoNombre.trim()).toPath());
            final String fileName = archivoNombre;
            
            System.out.println("FIle name" + fileName);

            if (docBytes == null) {
                Notification.show("Documento scan no disponible para visualizar!");

                return;
            }

            Window window = new Window();
            window.setResizable(true);
            window.setWidth("95%");
            window.setHeight("95%");

            StreamResource documentStreamResource = null;

            if (docBytes != null) {
                documentStreamResource = new StreamResource(
                        new StreamResource.StreamSource() {
                    public InputStream getStream() {
                        return new ByteArrayInputStream(docBytes);
                    }
                }, fileName
                );
            }
            documentStreamResource.setMIMEType(archivoTipo);
            documentStreamResource.setFilename(archivoNombre);
            documentStreamResource.getStream().setParameter("Content-Disposition", "attachment; filename=" + fileName);

            if (archivoTipo.contains("pdf")) {
                window.setWidth("98%");
                window.setHeight("98%");

                VerticalLayout pdfLayout = new VerticalLayout();
                pdfLayout.setSizeFull();
                pdfLayout.setSpacing(true);

                BrowserFrame browserFrame = new BrowserFrame();
                browserFrame.setSizeFull();
                browserFrame.setSource(documentStreamResource);

                pdfLayout.addComponent(browserFrame);

                UploadFinishedHandler handler;
                handler = new UploadFinishedHandler() {
                    @Override
                    public void handleFile(InputStream stream, String fileName, String mimeType, long length) {

                        File targetFile;

                        try {
                            if (mimeType.contains("png") || mimeType.contains("jpeg") || mimeType.contains("jpg") || mimeType.contains("pdf")) {
                                System.out.println("\nfileName=" + fileName);
                                System.out.println("length=" + stream.available());
                                System.out.println("mimeType=" + mimeType);

                                fileSize = stream.available();
                                byte[] buffer = new byte[stream.available()];
                                stream.read(buffer);

//                                String filePath = VaadinService.getCurrent()
//                                        .getBaseDirectory().getAbsolutePath() + "/projectfiles/" + ((SopdiUI) mainUI).sessionInformation.getStrCompanyId() + "/";
                                String filePath = enviromentsVars.getDtePath();

                                new File(filePath).mkdirs();

                                fileName = filePath + codigoVis + fileName.substring(fileName.length() - 4, fileName.length());

                                new File(filePath).mkdirs();

                                targetFile = new File(fileName);
                                OutputStream outStream = new FileOutputStream(targetFile);
                                outStream.write(buffer);
                                outStream.close();

                                stream.close();

                                System.out.println("\ntargetFile = " + fileName);

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

                                guardarArchivo(selectedObject, codigoVis, fileName);
                                window.close();
                            } else {
                                Notification.show("El archivo no contiene un formato compatible. solo puede subir archivos con formato 'PNG','JEPG','JPG','PDF'", Notification.Type.ERROR_MESSAGE);
                                return;
                            }
                        } catch (java.io.IOException fIoEx) {
                            fIoEx.printStackTrace();
                            Notification.show("Error al cargar el archivo adjunto!", Notification.Type.ERROR_MESSAGE);
                            return;
                        }
                    }
                };

                UploadStateWindow window2 = new UploadStateWindow();

                singleUpload = new MultiFileUpload(handler, window2, false);
                singleUpload.setIcon(FontAwesome.UPLOAD);
                singleUpload.setImmediate(true);
                singleUpload.getSmartUpload().setUploadButtonCaptions("Cambiar archivo", "");

                JavaScript.getCurrent().execute("document.getElementsByClassName('gwt-FileUpload')[0].setAttribute('accept', '.png')");
                JavaScript.getCurrent().execute("document.getElementsByClassName('gwt-FileUpload')[1].setAttribute('accept', '.jpg')");
                JavaScript.getCurrent().execute("document.getElementsByClassName('gwt-FileUpload')[2].setAttribute('accept', '.PDF')");

                pdfLayout.addComponent(singleUpload);

                window.setContent(pdfLayout);

                pdfLayout.setExpandRatio(browserFrame, 2);

            } else {
                window.setWidth("98%");
                window.setHeight("98%");

                VerticalLayout imageLayout = new VerticalLayout();
                imageLayout.setSizeFull();
                imageLayout.setSpacing(true);

                Image imageViewComponent = new Image(null, documentStreamResource);
                imageViewComponent.setImmediate(true);
                imageViewComponent.setSizeFull();
                imageViewComponent.addStyleName("my-img-button");
                imageViewComponent.setData(codigoVis);

                imageLayout.addComponent(imageViewComponent);
                imageLayout.setComponentAlignment(imageViewComponent, Alignment.MIDDLE_CENTER);

                UploadFinishedHandler handler;
                handler = new UploadFinishedHandler() {
                    @Override
                    public void handleFile(InputStream stream, String fileName, String mimeType, long length) {

                        File targetFile;

                        try {
                            if (mimeType.contains("png") || mimeType.contains("jpeg") || mimeType.contains("jpg") || mimeType.contains("pdf")) {
                                System.out.println("\nfileName=" + fileName);
                                System.out.println("length=" + stream.available());
                                System.out.println("mimeType=" + mimeType);

                                fileSize = stream.available();
                                byte[] buffer = new byte[stream.available()];
                                stream.read(buffer);

//                                String filePath = VaadinService.getCurrent()
//                                        .getBaseDirectory().getAbsolutePath() + "/projectfiles/" + ((SopdiUI) mainUI).sessionInformation.getStrCompanyId() + "/";
                                String filePath = enviromentsVars.getDtePath();

                                new File(filePath).mkdirs();

                                fileName = filePath + codigoVis + fileName.substring(fileName.length() - 4, fileName.length());
                                targetFile = new File(fileName);
                                OutputStream outStream = new FileOutputStream(targetFile);
                                outStream.write(buffer);
                                outStream.close();

                                stream.close();

                                System.out.println("\ntargetFile = " + fileName);

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

                                guardarArchivo(selectedObject, codigoVis, fileName);
                                window.close();
                            } else {
                                Notification.show("El archivo no contiene un formato compatible. solo puede subir archivos con formato 'PNG','JEPG','JPG','PDF'", Notification.Type.ERROR_MESSAGE);
                                return;
                            }
                        } catch (java.io.IOException fIoEx) {
                            fIoEx.printStackTrace();
                            Notification.show("Error al cargar el archivo adjunto!", Notification.Type.ERROR_MESSAGE);
                            return;
                        }
                    }
                };

                UploadStateWindow window2 = new UploadStateWindow();

                singleUpload = new MultiFileUpload(handler, window2, false);
                singleUpload.setIcon(FontAwesome.UPLOAD);
                singleUpload.setImmediate(true);
                singleUpload.getSmartUpload().setUploadButtonCaptions("Cambiar archivo", "");

                JavaScript.getCurrent().execute("document.getElementsByClassName('gwt-FileUpload')[0].setAttribute('accept', '.png')");
                JavaScript.getCurrent().execute("document.getElementsByClassName('gwt-FileUpload')[1].setAttribute('accept', '.jpg')");
                JavaScript.getCurrent().execute("document.getElementsByClassName('gwt-FileUpload')[2].setAttribute('accept', '.PDF')");

                imageLayout.addComponent(singleUpload);
                imageLayout.setExpandRatio(imageViewComponent, 2);
                window.setContent(imageLayout);
            }

            archivoNombre = "";
            UI.getCurrent().addWindow(window);
            window.center();

        } catch (Exception allEx) {
            Notification.show("Error al intentar mostrar el archivo.", Notification.Type.ERROR_MESSAGE);
            allEx.printStackTrace();
        }

    }

    public void recibirParametrosImagen(String fileName, String mimeType, long peso) {
        parametro1 = fileName;
        parametro2 = mimeType;
        parametro3 = peso;
    }

    public void eliminarRegistroTabla(ClickableRenderer.RendererClickEvent e) {
                   
            documentosGrid.select(e.getItemId());

            ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de Eliminar la factura ?",
                    "SI", "NO", new ConfirmDialog.Listener() {

                public void onClose(ConfirmDialog dialog) {
                    if (dialog.isConfirmed()) {

                        queryString = "DELETE FROM visita_inspeccion_documento";
                        queryString += " WHERE IdVisitaDocumento = " + String.valueOf(documentsContainer.getContainerProperty(e.getItemId(), ID_PROPERTY).getValue());
                        ///queryString += " and IdEmpresa = " + empresa;

                        try {
                            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                            stQuery.executeUpdate(queryString);

                            Notification.show("Registro eliminado exitosamente!.", Notification.Type.TRAY_NOTIFICATION);

                            documentsContainer.removeItem(e.getItemId());

//                            llenarTablaFactura(empresa, 0);
                        } catch (SQLException ex) {
                            System.out.println("Error al intentar eliminar " + ex);
                            Notification.show("Error al eliminar : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                            ex.printStackTrace();

                        }
                    }
                }
            });        
  
    }

    public void guardarArchivo(Object selectedObject, String codigoVisita, String fileName) {

        try {

            queryString = " INSERT INTO visita_inspeccion_documento(IdVisitaInspeccion,ArchivoTipo,";
            queryString += " ArchivoNombre,ArchivoPeso)";
            queryString += " VALUES( ";
            queryString += codigoVisita;
            queryString += ",'" + parametro2 + "'";
            queryString += ",'" + fileName + "'";
            queryString += "," + parametro3 + " )";

            stPreparedQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().prepareStatement(queryString);
            stPreparedQuery.executeUpdate();

            documentsContainer.getContainerProperty(selectedObject, NOMBRE_DOCUMENTO).setValue(fileName);
            documentsContainer.getContainerProperty(selectedObject, ARCHIVO_TIPO_PROPERTY).setValue(parametro2);

        } catch (Exception ex) {
            System.out.println("Error al intentar insertar Imagen" + ex);
            Notification.show("Error al insertar la imagen : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public void guardarArchivo1(String codigoVisita, String fileName) {

        try {

            queryString = " INSERT INTO visita_inspeccion_documento(IdVisitaInspeccion,ArchivoTipo,";
            queryString += " ArchivoNombre,ArchivoPeso)";
            queryString += " VALUES( ";
            queryString += codigoVisita;
            queryString += ",'" + parametro2 + "'";
            queryString += ",'" + fileName + "'";
            queryString += "," + parametro3 + " )";

            stPreparedQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().prepareStatement(queryString);
            stPreparedQuery.executeUpdate();

            close();
        } catch (Exception ex) {
            System.out.println("Error al intentar insertar Imagen" + ex);
            Notification.show("Error al insertar la imagen : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
