/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.operativo;

import com.simpletecno.sopdi.utilerias.MyEmailMessanger;
import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Sizeable;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.MultiFileUpload;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadFinishedHandler;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadStateWindow;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Locale;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.List;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author joseaguirre
 */
public class CargarIntegracion extends VerticalLayout implements View {
 
    Statement stQuery = null;
    PreparedStatement stPreparedQuery = null;
    ResultSet rsRecords = null;
    
    MarginInfo  marginInfo;
    
    ComboBox projectCbx;
    
    MultiFileUpload singleUpload;

    Button salirBtn;
    Button cargarBtn;
    
    public File file;
    public XSSFWorkbook workbook;
    public XSSFSheet sheet, sheet2;
    private FileInputStream fileInputStream;
    
    Table integracionTable;

    public static Locale locale = new Locale("ES","GT"); 

    int linea = 0;
    
    UI mainUI;
       
    public CargarIntegracion() {
        this.mainUI = UI.getCurrent();    

        setCaption(((SopdiUI) mainUI).sessionInformation.getStrCompanyName() + " - Importar archivo EXCEL de integración de tareas del proyecto : " + ((SopdiUI) mainUI).sessionInformation.getStrProjectName());
        Page.getCurrent().setTitle(((SopdiUI) mainUI).sessionInformation.getStrCompanyName() + " - Importar integraciones");
        setSpacing(true);
        
        marginInfo = new MarginInfo(true,true,true,true);

        HorizontalLayout encabezadoLayout;
        encabezadoLayout = new HorizontalLayout();        
        encabezadoLayout.setWidth("60%");
        encabezadoLayout.setHeight("5%");
        encabezadoLayout.setMargin(marginInfo);
        encabezadoLayout.setSpacing(true);
        encabezadoLayout.addStyleName("rcorners3");
    
        projectCbx = new ComboBox("Project de : ");
        projectCbx.addItem("Urbanización");
        projectCbx.addItem("Casas 1");
        projectCbx.addItem("Casas 2");
        projectCbx.addItem("Casas 3");
        projectCbx.addItem("Casas 4");
        projectCbx.addItem("Casas 5");
        projectCbx.select("Casas 1");

/**        
@Override
public void uploadStarted(StartedEvent event) {
    // TODO Auto-generated method stub
    System.out.println("***Upload: uploadStarted()");

    String contentType = event.getMIMEType();
    boolean allowed = false;
    for(int i=0;i<allowedMimeTypes.size();i++){
        if(contentType.equalsIgnoreCase(allowedMimeTypes.get(i))){
            allowed = true;
            break;
        }
    }
    if(allowed){
        fileNameLabel.setValue(event.getFilename());
        progressBar.setValue(0f);
        progressBar.setVisible(true);
        cancelButton.setVisible(true);
        upload.setEnabled(false);
    }else{
        Notification.show("Error", "\nAllowed MIME: "+allowedMimeTypes, Type.ERROR_MESSAGE);
        upload.interruptUpload();
    }

}
***/
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

                    cargarIntegracion(targetFile);

                    file = targetFile;
                    cargarBtn.setEnabled(true);
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
        singleUpload.getSmartUpload().setUploadButtonCaptions("Archivo", "");
        
JavaScript.getCurrent().execute("document.getElementsByClassName('gwt-FileUpload')[0].setAttribute('accept', '.xlsx')");

        List<String> acceptedMimeTypes = new ArrayList();
        acceptedMimeTypes.add("application/octet-stream");
        acceptedMimeTypes.add("application/ovnd.ms-excel");
        acceptedMimeTypes.add("application/msexcel");
        acceptedMimeTypes.add("application/x-msexcel");
        acceptedMimeTypes.add("application/x-ms-excel");
        acceptedMimeTypes.add("application/x-excel");
        acceptedMimeTypes.add("application/x-dos_ms_excel");
        acceptedMimeTypes.add("application/xls");
        acceptedMimeTypes.add("application/x-xls");
//        singleUpload.setAcceptedMimeTypes(acceptedMimeTypes);
        
//        encabezadoLayout.addComponent(h1);
        encabezadoLayout.addComponent(projectCbx);
        encabezadoLayout.addComponent(singleUpload);
        encabezadoLayout.setComponentAlignment(singleUpload, Alignment.BOTTOM_RIGHT);

        addComponent(encabezadoLayout);
        setComponentAlignment(encabezadoLayout, Alignment.TOP_CENTER);
        
        VerticalLayout compraLayout;
        compraLayout = new VerticalLayout();
//        compraLayout.setSizeUndefined();
        compraLayout.setWidth("98%");
        compraLayout.setHeight("95%");
        compraLayout.setMargin(marginInfo);
        compraLayout.setSpacing(true);
        compraLayout.addStyleName("rcorners3");
        
        integracionTable = new Table();
        integracionTable.setWidth("100%");

        integracionTable.addContainerProperty("TID", String.class, "");
        integracionTable.addContainerProperty("Cuenta", String.class, "");
        integracionTable.addContainerProperty("Descripción", String.class, "");
        
        integracionTable.addContainerProperty("Precio", String.class, "");
        integracionTable.addContainerProperty("Cantidad", String.class, "");
        integracionTable.addContainerProperty("Total", String.class, "");
        
        integracionTable.addContainerProperty("IdProveedor", String.class, "");
        integracionTable.addContainerProperty("IdUnidad", String.class, "");
        integracionTable.addContainerProperty("Nivel", String.class, "");
        
        integracionTable.addContainerProperty("Area", String.class, "");
                        
        integracionTable.setColumnAlignments(new Table.Align[] { 
                Table.Align.CENTER, Table.Align.LEFT,   Table.Align.LEFT,
                Table.Align.RIGHT,  Table.Align.CENTER, Table.Align.RIGHT, 
                Table.Align.CENTER, Table.Align.CENTER, Table.Align.CENTER,
                Table.Align.CENTER
        });
        
        integracionTable.setColumnWidth("TID", 50);
        integracionTable.setColumnWidth("Descripción", 300);
        integracionTable.setColumnWidth("Cuenta", 60);
        integracionTable.setColumnWidth("Precio", 60);
        integracionTable.setColumnWidth("Cantidad", 50);
        integracionTable.setColumnWidth("Total", 50);
        integracionTable.setColumnWidth("IdProveedor", 50); 
        integracionTable.setColumnWidth("IdUnidad", 50); 
        integracionTable.setColumnWidth("Nivel", 50);
        integracionTable.setColumnWidth("Area", 50);
        
/**        
        integracionTable.setFooterVisible(true);
        integracionTable.setColumnFooter("Total", "0.00");        
        integracionTable.setSelectable(true);
**/        
        compraLayout.addComponent(integracionTable);
        compraLayout.setComponentAlignment(integracionTable, Alignment.MIDDLE_CENTER);
            
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setMargin(false);

        cargarBtn    = new Button("CARGAR TAREAS");
        cargarBtn.setIcon(FontAwesome.ARROW_CIRCLE_O_UP);
        cargarBtn.setWidth(130,Sizeable.UNITS_PIXELS);
        cargarBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick ( Button.ClickEvent event )
            {
                if(integracionTable.size() > 0) {
                    validarYGuardar();
                } 
            }
        });
        cargarBtn.setEnabled(false);

        salirBtn    = new Button("LIMPIAR");
        salirBtn.setIcon(FontAwesome.ERASER);
        salirBtn.setWidth(130,Sizeable.UNITS_PIXELS);
        salirBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick ( Button.ClickEvent event )
            {
                integracionTable.removeAllItems();
            }
        });
        
        buttonsLayout.addComponent(salirBtn);
        buttonsLayout.setComponentAlignment(salirBtn, Alignment.BOTTOM_LEFT);
        buttonsLayout.addComponent(cargarBtn);
        
        compraLayout.addComponent(buttonsLayout);
        compraLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);
        
        addComponent(compraLayout);        
        setComponentAlignment(compraLayout, Alignment.MIDDLE_CENTER);
    }
                
    void validarYGuardar() {

        if(integracionTable.size() == 0) {
            Notification.show("No exiten integraciones para cargar, revise!", Notification.Type.WARNING_MESSAGE);
            return;
        }

        ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de GUARDAR estas integraciones en base de datos?",
            "SI", "NO", new ConfirmDialog.Listener() {

            public void onClose(ConfirmDialog dialog) {
                if (dialog.isConfirmed()) {
                    String queryString;

                    try {

                        ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().setAutoCommit(false);

                        stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();            

                        queryString = "DELETE FROM tarea_integracion WHERE (SELECT IdTarea FROM tarea WHERE IdProyecto = " + ((SopdiUI) mainUI).sessionInformation.getStrProjectName() + " AND Fase  = '" + String.valueOf(projectCbx.getValue()) + "')";
                        stQuery.executeUpdate(queryString);
                                                
                        for(int linea = 0; linea < 10; linea++ ) {
                            queryString =  "INSERT INTO tarea_integracion (IdEmpresa, IdProyecto, Fase, IdTareaProject,";
                            queryString += " Descripcion, IdTareaPadre, Predecesores, Sucesores,";
                            queryString += " FechaInicio, FechaFin, DiasDuracion, Nivel, NivelCodigo, ";
                            queryString += " CreadoUsuario, CreadoFecha) ";
                            queryString += " VALUES ( ";
                            queryString += " "  + ((SopdiUI) mainUI).sessionInformation.getStrCompanyId();                            
                            queryString += ","  + ((SopdiUI) mainUI).sessionInformation.getStrProjectName();                            
                            queryString += ",'" + String.valueOf(projectCbx.getValue()) + "'";                            
                            queryString += ",'" + ((SopdiUI) mainUI).sessionInformation.getStrUserName() + "'";
                            queryString += ",current_timestamp";
                            
                            queryString += ")";
System.out.println("query=" + queryString);
                            stQuery.executeUpdate(queryString);
                        } //end for

                        ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().commit();
                        ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().setAutoCommit(true);

                    }
                    catch(Exception ex1) {
                        try {
                            ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().rollback();
                        }
                        catch(java.sql.SQLException sqlE) {
                            //                
                        }
                        System.out.println("Error al insertar registros de integración en base de datos..Transaccion abortada..!");
                        Notification.show("Error al insertar registro de integración en base de datos..Transaccion abortada..!", Notification.Type.ERROR_MESSAGE);
                        ex1.printStackTrace();

                        String emailRecipients[] = {"alerta@simpletecno.com"};

                        MyEmailMessanger eMail = new MyEmailMessanger();

                        try { 
                            eMail.postMail(emailRecipients, "Error SOPDI Carga de Integraciones EXCEL", "ERROR AL INSERTAR REGISTROS : " + ex1.getMessage());
                        } catch (Exception ex) {
            //                    Logger.getLogger(MyUI.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        return;
                    }//endcatch//endcatch

                    Notification.show("Operación exitosa!", Notification.Type.HUMANIZED_MESSAGE);

                    cargarBtn.setEnabled(false);

                    integracionTable.removeAllItems();

                    file.delete();
                }
                else {
                    Notification.show("Operación cacelada!", Notification.Type.WARNING_MESSAGE);                    
                }
            }
        });                       

    }  
    
    private void cargarIntegracion(File file) {
        
        integracionTable.removeAllItems();
        
        singleUpload.setEnabled(false);
        
        try {

            fileInputStream  = new FileInputStream(file);

            workbook  = new XSSFWorkbook(fileInputStream);

            sheet  = workbook.getSheetAt(0);
            
System.out.println("\n Total lineas en archivo=" + sheet.getLastRowNum());

            System.out.println("...INICIO...");
            
            for(int linea = 1; linea < sheet.getLastRowNum(); linea++ ) {
               
                integracionTable.addItem(new Object[] {
                    String.valueOf(Double.valueOf(sheet.getRow(linea).getCell(1).getNumericCellValue()).intValue()),
                    sheet.getRow(linea).getCell(2).getStringCellValue(),
                    sheet.getRow(linea).getCell(3).getStringCellValue(),
                    String.valueOf(sheet.getRow(linea).getCell(4).getNumericCellValue()),
                    (sheet.getRow(linea).getCell(5) != null ? String.valueOf(sheet.getRow(linea).getCell(5).getNumericCellValue()) : "0" ),
                    (sheet.getRow(linea).getCell(6) != null ? String.valueOf(sheet.getRow(linea).getCell(6).getNumericCellValue()) : "0" ),
                    String.valueOf(Double.valueOf(sheet.getRow(linea).getCell(9).getNumericCellValue()).intValue()),
                    String.valueOf((sheet.getRow(linea).getCell(10) != null ? Double.valueOf(sheet.getRow(linea).getCell(10).getNumericCellValue()).intValue() : 0 )),
                    (sheet.getRow(linea).getCell(14) != null ? sheet.getRow(linea).getCell(14).getStringCellValue() : "0"),
                    (sheet.getRow(linea).getCell(15) != null ? sheet.getRow(linea).getCell(15).getStringCellValue() : "0")
                }, integracionTable.size()+1);

//System.out.println("IDEX="+String.valueOf(Double.valueOf(sheet.getRow(linea).getCell(1).getNumericCellValue()).intValue()));
            } //endfor
            
            System.out.println("...FIN...");
        }
        catch(Exception ex1) {
            new Notification("Error al intentar cargar las integraciones de tareas del archivo EXCEL.",
            ex1.getMessage(),
            Notification.Type.ERROR_MESSAGE)
            .show(Page.getCurrent());
            ex1.printStackTrace();
        }
        singleUpload.setEnabled(true);
    }
    
    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        // TODO Auto-generated method stub

    }
}
