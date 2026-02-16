/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.compras;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.MultiFileUpload;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadFinishedHandler;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadStateWindow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.vaadin.dialogs.ConfirmDialog;

import java.io.*;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author joseaguirre
 */
public class ImportarProveedoresForm extends Window {

    VerticalLayout mainLayout;

    Statement stQuery = null;
    Statement stQuery1 = null;
    Statement stQuery2 = null;
    ResultSet rsRecords = null;
    ResultSet rsRecords1 = null;

    MarginInfo marginInfo;

    ComboBox empresaCbx;
    String empresa;

    MultiFileUpload singleUpload;

    Button limpiarBtn;
    Button cargarBtn;

    public File proveedorFile;
    public XSSFWorkbook workbook;
    public XSSFSheet sheet, sheet2;
    private FileInputStream fileInputStream;

    Table proveedorTable;

    public static Locale locale = new Locale("ES", "GT");

    UI mainUI;

    public ImportarProveedoresForm() {
        this.mainUI = UI.getCurrent();

        this.setWidth("90%");
        this.setHeight("80%");

        mainLayout = new VerticalLayout();
        mainLayout.setMargin(false);
        mainLayout.setSpacing(true);
//        mainLayout.setSizeFull();
        mainLayout.addStyleName("rcorners3");

        setContent(mainLayout);

        marginInfo = new MarginInfo(true, true, true, true);

        Label titleLbl = new Label("IMPORTAR PROVEEDORES");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h1_custom");

        proveedorTable = new Table();
        proveedorTable.setWidth("100%");

        proveedorTable.addContainerProperty("NO", String.class, "");
        proveedorTable.addContainerProperty("Grupo0", String.class, "");

        proveedorTable.addContainerProperty("N1", String.class, "");
        proveedorTable.addContainerProperty("Grupo", String.class, "");
        proveedorTable.addContainerProperty("N2", String.class, "");

        proveedorTable.addContainerProperty("Tipo", String.class, "");
        proveedorTable.addContainerProperty("N3", String.class, "");
        proveedorTable.addContainerProperty("IDProveedor", String.class, "");

        proveedorTable.addContainerProperty("Nombre", String.class, "");
        proveedorTable.addContainerProperty("ProductoNota", String.class, "");
        proveedorTable.addContainerProperty("NIT", String.class, "");

        proveedorTable.addContainerProperty("Inhabilitado", String.class, "");
        proveedorTable.addContainerProperty("AnticipoLote", String.class, "");
        proveedorTable.addContainerProperty("Provision", String.class, "");

        proveedorTable.addContainerProperty("DiasAnticipo", String.class, "");
        proveedorTable.addContainerProperty("DiasCredito", String.class, "");
        proveedorTable.addContainerProperty("AnticipoUnidad", String.class, "");

        proveedorTable.addContainerProperty("DiaProvision", String.class, "");
        proveedorTable.addContainerProperty("Email", String.class, "");
        proveedorTable.addContainerProperty("DPI", String.class, "");

        proveedorTable.addContainerProperty("EsProveedor", String.class, "");
        proveedorTable.addContainerProperty("EsCliente", String.class, "");
        proveedorTable.addContainerProperty("EsLiquidador", String.class, "");

        proveedorTable.addContainerProperty("EsComite", String.class, "");
        proveedorTable.addContainerProperty("EsPlanilla", String.class, "");
        proveedorTable.addContainerProperty("EsRelacionada", String.class, "");

        proveedorTable.setColumnAlignments(new Table.Align[]{
                Table.Align.LEFT, Table.Align.CENTER, Table.Align.CENTER,
                Table.Align.LEFT, Table.Align.LEFT, Table.Align.LEFT,
                Table.Align.RIGHT, Table.Align.RIGHT, Table.Align.CENTER,
                Table.Align.CENTER, Table.Align.CENTER, Table.Align.CENTER,
                Table.Align.CENTER, Table.Align.CENTER, Table.Align.CENTER,
                Table.Align.CENTER, Table.Align.CENTER, Table.Align.CENTER,
                Table.Align.CENTER, Table.Align.CENTER, Table.Align.CENTER,
                Table.Align.CENTER, Table.Align.CENTER, Table.Align.CENTER,
                Table.Align.CENTER, Table.Align.CENTER
        });

        proveedorTable.setColumnWidth("N0", 50);
        proveedorTable.setColumnWidth("Grupo0", 100);
        proveedorTable.setColumnWidth("N1", 50);
        proveedorTable.setColumnWidth("Grupo", 100);
        proveedorTable.setColumnWidth("N2", 50);
        proveedorTable.setColumnWidth("Tipo", 100);
        proveedorTable.setColumnWidth("N3", 50);
        proveedorTable.setColumnWidth("IDProveedor", 90);
        proveedorTable.setColumnWidth("Nombre", 60);
        proveedorTable.setColumnWidth("ProductoNota", 60);
        proveedorTable.setColumnWidth("NIT", 60);
        proveedorTable.setColumnWidth("Inhabilitado", 90);
        proveedorTable.setColumnWidth("AnticipoLote", 60);
        proveedorTable.setColumnWidth("Provision", 60);
        proveedorTable.setColumnWidth("DiasAnticipo", 60);
        proveedorTable.setColumnWidth("DiasCredito", 60);
        proveedorTable.setColumnWidth("AnticipoUnidad", 60);
        proveedorTable.setColumnWidth("DiaProvision", 90);
        proveedorTable.setColumnWidth("Email", 60);
        proveedorTable.setColumnWidth("DPI", 60);
        proveedorTable.setColumnWidth("EsProveedor", 60);
        proveedorTable.setColumnWidth("EsCliente", 60);
        proveedorTable.setColumnWidth("EsLiquidador", 60);
        proveedorTable.setColumnWidth("EsComite", 60);
        proveedorTable.setColumnWidth("EsPlanilla", 60);
        proveedorTable.setColumnWidth("EsRelacionada", 60);

        empresaCbx = new ComboBox("Empresa:");
        empresaCbx.setWidth("400px");
        empresaCbx.addStyleName(ValoTheme.COMBOBOX_HUGE);

        llenarComboEmpresa();

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setResponsive(true);
        titleLayout.setSpacing(true);
        titleLayout.setWidth("100%");
        titleLayout.setMargin(false);
        titleLayout.addComponents(empresaCbx, titleLbl);
        titleLayout.setComponentAlignment(empresaCbx, Alignment.MIDDLE_CENTER);
        titleLayout.setComponentAlignment(titleLbl, Alignment.MIDDLE_CENTER);
        titleLayout.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

        mainLayout.addComponent(titleLayout);
//        mainLayout.setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        mainLayout.addComponent(proveedorTable);
//        mainLayout.setComponentAlignment(proveedorTable, Alignment.MIDDLE_CENTER);

        UploadFinishedHandler handler;
        handler = (InputStream stream, String fileName, String mimeType, long length) -> {
            File targetFile;

            try {

                System.out.println("\nfileName=" + fileName);
                System.out.println("length=" + stream.available());
                System.out.println("mimeType=" + mimeType);

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

                cargarPlanilla(targetFile);

                proveedorFile = targetFile;

            } catch (java.io.IOException fIoEx) {
                fIoEx.printStackTrace();
                Notification.show("Error al cargar el archivo adjunto!", Notification.Type.ERROR_MESSAGE);
                return;
            }
        };

        UploadStateWindow window = new UploadStateWindow();

        singleUpload = new MultiFileUpload(handler, window, false);
        singleUpload.setIcon(FontAwesome.UPLOAD);
        singleUpload.setImmediate(true);
        singleUpload.getSmartUpload().setUploadButtonCaptions("Cargar archivo", "");

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

        cargarBtn = new Button("VALIDAR PROVEEDORES");
        cargarBtn.setEnabled(false);
        cargarBtn.setIcon(FontAwesome.SAVE);
        cargarBtn.addStyleName(ValoTheme.BUTTON_LINK);
        cargarBtn.addStyleName(".v-button { text-decoration : underline;}");
        cargarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (proveedorTable.size() > 0) {
                    validarYGuardar();
                }
            }
        });

        Page.getCurrent().setTitle("Sopdi- Importar Proveedores");

        limpiarBtn = new Button("LIMPIAR");
        limpiarBtn.setIcon(FontAwesome.ERASER);
        limpiarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                proveedorTable.removeAllItems();
            }
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setMargin(true);
        buttonsLayout.setWidth("100%");

        buttonsLayout.addComponent(singleUpload);
        buttonsLayout.setComponentAlignment(singleUpload, Alignment.BOTTOM_CENTER);
        buttonsLayout.addComponent(cargarBtn);
        buttonsLayout.setComponentAlignment(cargarBtn, Alignment.BOTTOM_CENTER);
        buttonsLayout.addComponent(limpiarBtn);
        buttonsLayout.setComponentAlignment(limpiarBtn, Alignment.BOTTOM_LEFT);

        mainLayout.addComponent(buttonsLayout);
//        mainLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);

        empresa = String.valueOf(empresaCbx.getValue());

    }

    public void llenarComboEmpresa() {
        String queryString = " SELECT * from contabilidad_empresa";

        try {
            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords1 = stQuery1.executeQuery(queryString);

            while (rsRecords1.next()) { //  encontrado                
                empresaCbx.addItem(rsRecords1.getString("IdEmpresa"));
                empresaCbx.setItemCaption(rsRecords1.getString("IdEmpresa"), rsRecords1.getString("Empresa"));
            }
            rsRecords1.first();

            empresaCbx.select(rsRecords1.getString("IdEmpresa"));

        } catch (Exception ex1) {
            System.out.println("Error al listar empresas: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    private void cargarPlanilla(File planillaFile) {

        proveedorTable.removeAllItems();

        singleUpload.setEnabled(false);

        try {

            fileInputStream = new FileInputStream(planillaFile);

            workbook = new XSSFWorkbook(fileInputStream);

            sheet = workbook.getSheetAt(0);

System.out.println("\n Total lineas en archivo=" + sheet.getLastRowNum());
System.out.println("...INICIO...");

            cargarBtn.setEnabled(true);

            for (int linea = 1; linea <= sheet.getLastRowNum(); linea++) {

                try {
                    sheet.getRow(linea).getCell(0).getRawValue();
                }
                catch(Exception exNull) {
                    break;
                }
//                if (!sheet.getRow(linea).getCell(10).getRawValue().equals(empresa)) {
//                    Notification.show("No es posible cargar el documento. Por favor revisar que la empresa del"
//                            + " programa coincida con la del documento", Notification.Type.ERROR_MESSAGE);
//                    cargarBtn.setEnabled(false);
//                    break;
//                }

//                if ( ((SopdiUI) UI.getCurrent()).esMesCerrado(empresa, Utileria.getFechaYYYYMMDD_1(sheet.getRow(linea).getCell(0).getDateCellValue()))) {
//                    Notification.show("La fecha del documento no puede ser de un mes ya cerrado contablemente, revise!", Notification.Type.WARNING_MESSAGE);
//                    return;
//                }


System.out.println("linea:" + linea);

//System.out.println(String.valueOf(sheet.getRow(linea).getCell(0).getStringCellValue()));
System.out.println(sheet.getRow(linea).getCell(1).getStringCellValue());
System.out.println(sheet.getRow(linea).getCell(2).getStringCellValue());
System.out.println(sheet.getRow(linea).getCell(3).getStringCellValue());
System.out.println(sheet.getRow(linea).getCell(4).getStringCellValue());
System.out.println(sheet.getRow(linea).getCell(5).getRawValue());
System.out.println(sheet.getRow(linea).getCell(6).getRawValue());
System.out.println(sheet.getRow(linea).getCell(7).getRawValue());
System.out.println(String.valueOf(sheet.getRow(linea).getCell(8).getNumericCellValue()).split("\\.")[0]);
System.out.println(sheet.getRow(linea).getCell(9).getStringCellValue());
System.out.println(sheet.getRow(linea).getCell(10).getStringCellValue());
System.out.println(sheet.getRow(linea).getCell(11).getStringCellValue());
System.out.println(sheet.getRow(linea).getCell(12).getStringCellValue());
System.out.println(sheet.getRow(linea).getCell(13).getRawValue());
System.out.println(sheet.getRow(linea).getCell(14).getRawValue());
System.out.println(sheet.getRow(linea).getCell(15).getRawValue());
System.out.println(sheet.getRow(linea).getCell(16).getRawValue());
System.out.println(sheet.getRow(linea).getCell(17).getRawValue());
System.out.println(sheet.getRow(linea).getCell(18).getRawValue());
System.out.println(sheet.getRow(linea).getCell(19).getRawValue());
System.out.println(sheet.getRow(linea).getCell(20).getRawValue());
System.out.println(sheet.getRow(linea).getCell(21).getRawValue());
System.out.println(sheet.getRow(linea).getCell(22).getRawValue());
System.out.println(sheet.getRow(linea).getCell(23).getRawValue());
System.out.println(sheet.getRow(linea).getCell(24).getRawValue());
System.out.println(sheet.getRow(linea).getCell(25).getRawValue());

                proveedorTable.addItem(new Object[]{
                        String.valueOf(sheet.getRow(linea).getCell(1).getStringCellValue()),//N0
                        String.valueOf(sheet.getRow(linea).getCell(2).getStringCellValue()),//Grupo0
                        String.valueOf(sheet.getRow(linea).getCell(3).getStringCellValue()),//N1
                        String.valueOf(sheet.getRow(linea).getCell(4).getStringCellValue()),//Grupo
                        String.valueOf(sheet.getRow(linea).getCell(5).getStringCellValue()),//N2
                        String.valueOf(sheet.getRow(linea).getCell(6).getStringCellValue()),//Tipo
                        String.valueOf(sheet.getRow(linea).getCell(7).getStringCellValue()),//N3
                        String.valueOf(sheet.getRow(linea).getCell(8).getNumericCellValue()).split("\\.")[0],//IDProveedor
                        String.valueOf(sheet.getRow(linea).getCell(9).getStringCellValue()),//Nombre
                        String.valueOf(sheet.getRow(linea).getCell(10).getStringCellValue()),//ProductoNota
                        String.valueOf(sheet.getRow(linea).getCell(11).getStringCellValue()),//NIT
                        String.valueOf(sheet.getRow(linea).getCell(12).getStringCellValue()),//Inhabilitado
                        String.valueOf(sheet.getRow(linea).getCell(13).getStringCellValue()),//AnticipoLote
                        String.valueOf(sheet.getRow(linea).getCell(14).getStringCellValue()),//Provision
                        String.valueOf(sheet.getRow(linea).getCell(15).getStringCellValue()),//DiasAnticipo
                        String.valueOf(sheet.getRow(linea).getCell(16).getStringCellValue()),//DiasCredito
                        String.valueOf(sheet.getRow(linea).getCell(17).getStringCellValue()),//AnticipoUnidad
                        String.valueOf(sheet.getRow(linea).getCell(18).getStringCellValue()),//DiaProvision
                        String.valueOf(sheet.getRow(linea).getCell(19).getStringCellValue()),//Email
                        String.valueOf(sheet.getRow(linea).getCell(20).getStringCellValue().split("\\.")[0]),//DPI
                        String.valueOf(sheet.getRow(linea).getCell(21).getStringCellValue()),//EsProveedor
                        String.valueOf(sheet.getRow(linea).getCell(22).getStringCellValue()),//Escliente
                        String.valueOf(sheet.getRow(linea).getCell(23).getStringCellValue()),//EsLiquidador
                        String.valueOf(sheet.getRow(linea).getCell(24).getStringCellValue()),//EsComite
                        String.valueOf(sheet.getRow(linea).getCell(25).getStringCellValue()),//EsPlanilla
                        String.valueOf(sheet.getRow(linea).getCell(26).getStringCellValue())//EsRelacionada
                }, proveedorTable.size() + 1);

            } //endfor


            System.out.println("...FIN...");

        } catch (Exception ex1) {
            new Notification("Error al intentar cargar archivo EXCEL.",
                    ex1.getMessage(),
                    Notification.Type.ERROR_MESSAGE)
                    .show(Page.getCurrent());
            ex1.printStackTrace();
        }
        singleUpload.setEnabled(true);
    }

    void validarYGuardar() {

        if (proveedorTable.size() == 0) {
            Notification.show("No ha elegido archivo para cargar, revise!", Notification.Type.WARNING_MESSAGE);
            return;
        }

        ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de CARGAR este archivo ?",
                "SI", "NO", new ConfirmDialog.Listener() {

                    public void onClose(ConfirmDialog dialog) {
                        if (dialog.isConfirmed()) {

                            String queryString;

                            dialog.setCaption("Procesando...");
                            dialog.setDescription(" Espere por favor.... ");
                            dialog.getOkButton().setEnabled(false);

                            try {

                                stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();

                                String idNomenclatura;
                                String fecha;
                                String ultimoEncontado;
                                String dia;
                                String mes;
                                String año;

                                String codigoPartida = "";

                                ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().setAutoCommit(false);

                                stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

                                fileInputStream = new FileInputStream(proveedorFile);

                                workbook = new XSSFWorkbook(fileInputStream);

                                sheet = workbook.getSheetAt(0);

                                String planillaAnterior = "";
                                String documentoAnterior = "";
                                queryString = "";
                                int correlativo = 0;

                                stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                                stQuery2 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();


                                for (int linea = 1; linea <= sheet.getLastRowNum(); linea++) {

                                    try {
                                        sheet.getRow(linea).getCell(0).getRawValue();
                                    } catch (Exception exNull) {
                                        break;
                                    }

                                    queryString = "Select * from proveedor";
                                    queryString += " Where IdProveedor = " + String.valueOf(sheet.getRow(linea).getCell(8).getNumericCellValue()).split("\\.")[0];

                                    rsRecords = stQuery.executeQuery(queryString);

                                    if (rsRecords.next()){ /// si existe un proveedor con ese Id ACTUALIZAR

                                        queryString = "Update proveedor Set ";
                                        queryString += " N0 = " + String.valueOf(sheet.getRow(linea).getCell(1).getStringCellValue());
                                        queryString += ",Grupo0 = '" + String.valueOf(sheet.getRow(linea).getCell(2).getStringCellValue()) + "'";
                                        queryString += ",N1 = " + String.valueOf(sheet.getRow(linea).getCell(3).getStringCellValue());
                                        queryString += ",Grupo = '" + String.valueOf(sheet.getRow(linea).getCell(4).getStringCellValue()) + "'";
                                        queryString += ",N2 = " + String.valueOf(sheet.getRow(linea).getCell(5).getStringCellValue());
                                        queryString += ",Tipo = '" + String.valueOf(sheet.getRow(linea).getCell(6).getStringCellValue()) + "'";
                                        queryString += ",N3 = " + String.valueOf(sheet.getRow(linea).getCell(7).getStringCellValue());
                                        queryString += ",IDProveedor = " + String.valueOf(sheet.getRow(linea).getCell(8).getNumericCellValue()).split("\\.")[0];
                                        queryString += ",Nombre = '" + String.valueOf(sheet.getRow(linea).getCell(9).getStringCellValue()) + "'";
                                        queryString += ",ProductoNota = '" + String.valueOf(sheet.getRow(linea).getCell(10).getStringCellValue()) + "'";
                                        queryString += ",NIT = '" + String.valueOf(sheet.getRow(linea).getCell(11).getStringCellValue()) + "'";
                                        queryString += ",Inhabilitado = '" + String.valueOf(sheet.getRow(linea).getCell(12).getStringCellValue()) + "'";
                                        queryString += ",AnticipoLote = " + String.valueOf(sheet.getRow(linea).getCell(13).getStringCellValue());
                                        queryString += ",Provision = " + String.valueOf(sheet.getRow(linea).getCell(14).getStringCellValue());
                                        if (String.valueOf(sheet.getRow(linea).getCell(15).getStringCellValue()).equals("") ||
                                                String.valueOf(sheet.getRow(linea).getCell(15).getStringCellValue())==null) {
                                            queryString += ",DiasAnticipo = 0";
                                        }else{
                                            queryString += ",DiasAnticipo = " + String.valueOf(sheet.getRow(linea).getCell(15).getStringCellValue());
                                        }

                                        if (String.valueOf(sheet.getRow(linea).getCell(16).getStringCellValue()).equals("") ||
                                                String.valueOf(sheet.getRow(linea).getCell(16).getStringCellValue())==null) {
                                            queryString += ",DiasCredito = 0";
                                        }else{
                                            queryString += ",DiasCredito = " + String.valueOf(sheet.getRow(linea).getCell(16).getStringCellValue());

                                        }
                                        queryString += ",AnticipoUnidad = " + String.valueOf(sheet.getRow(linea).getCell(17).getStringCellValue());
                                        queryString += ",DiaProvision = " + String.valueOf(sheet.getRow(linea).getCell(18).getStringCellValue());
                                        queryString += ",Email = '" + String.valueOf(sheet.getRow(linea).getCell(19).getStringCellValue()) + "'";
                                        queryString += ",DPI = '" + String.valueOf(sheet.getRow(linea).getCell(20).getStringCellValue()).split("\\.")[0] + "'";
                                        queryString += ",EsProveedor = " + String.valueOf(sheet.getRow(linea).getCell(21).getStringCellValue());
                                        queryString += ",EsCliente = " + String.valueOf(sheet.getRow(linea).getCell(22).getStringCellValue());
                                        queryString += ",EsLiquidador = " + String.valueOf(sheet.getRow(linea).getCell(23).getStringCellValue());
                                        queryString += ",EsComite    = " + String.valueOf(sheet.getRow(linea).getCell(24).getStringCellValue());
                                        queryString += ",EsPlanilla = " + String.valueOf(sheet.getRow(linea).getCell(25).getStringCellValue());
                                        queryString += ",EsRelacionada = " + String.valueOf(sheet.getRow(linea).getCell(26).getStringCellValue());
                                        queryString += " Where IDProveedor = " + String.valueOf(sheet.getRow(linea).getCell(8).getNumericCellValue()).split("\\.")[0];

                                    }else{
                                        queryString = "Insert Into proveedor (N0, Grupo0, N1, Grupo, N2,Tipo,N3,IDProveedor, ";
                                        queryString += " Nombre, ProductoNota, NIT, Inhabilitado, AnticipoLote, Provision, DiasAnticipo,";
                                        queryString += " DiasCredito, AnticipoUnidad, DiaProvision, Email, DPI, ";
                                        queryString += " EsProveedor, EsCliente,";
                                        queryString += " EsLiquidador, EsComite, EsPlanilla, EsRelacionada)";
                                        queryString += " Values (";
                                        queryString += "" + String.valueOf(sheet.getRow(linea).getCell(1).getStringCellValue());
                                        queryString += ",'" + String.valueOf(sheet.getRow(linea).getCell(2).getStringCellValue()) + "'";
                                        queryString += "," + String.valueOf(sheet.getRow(linea).getCell(3).getStringCellValue());
                                        queryString += ",'" + String.valueOf(sheet.getRow(linea).getCell(4).getStringCellValue()) + "'";
                                        queryString += "," + String.valueOf(sheet.getRow(linea).getCell(5).getStringCellValue());
                                        queryString += ",'" + String.valueOf(sheet.getRow(linea).getCell(6).getStringCellValue()) + "'";
                                        queryString += "," + String.valueOf(sheet.getRow(linea).getCell(7).getStringCellValue());
                                        queryString += "," + String.valueOf(sheet.getRow(linea).getCell(8).getNumericCellValue()).split("\\.")[0];
                                        queryString += ",'" + String.valueOf(sheet.getRow(linea).getCell(9).getStringCellValue()) + "'";
                                        queryString += ",'" + String.valueOf(sheet.getRow(linea).getCell(10).getStringCellValue()) + "'";
                                        queryString += ",'" + String.valueOf(sheet.getRow(linea).getCell(11).getStringCellValue()) + "'";
                                        queryString += "," + String.valueOf(sheet.getRow(linea).getCell(12).getStringCellValue());
                                        queryString += "," + String.valueOf(sheet.getRow(linea).getCell(13).getStringCellValue());
                                        queryString += "," + String.valueOf(sheet.getRow(linea).getCell(14).getStringCellValue());
                                        if (String.valueOf(sheet.getRow(linea).getCell(15).getStringCellValue()).equals("") ||
                                                String.valueOf(sheet.getRow(linea).getCell(15).getStringCellValue())==null) {
                                            queryString += ",0";
                                        }else{
                                            queryString += ", " + String.valueOf(sheet.getRow(linea).getCell(15).getStringCellValue());
                                        }
                                        if (String.valueOf(sheet.getRow(linea).getCell(16).getStringCellValue()).equals("") ||
                                                String.valueOf(sheet.getRow(linea).getCell(16).getStringCellValue())==null) {
                                            queryString += ",0";
                                        }else{
                                            queryString += ", " + String.valueOf(sheet.getRow(linea).getCell(16).getStringCellValue());
                                        }
                                        queryString += "," + String.valueOf(sheet.getRow(linea).getCell(17).getStringCellValue());
                                        queryString += "," + String.valueOf(sheet.getRow(linea).getCell(18).getStringCellValue());
                                        queryString += ",'" + String.valueOf(sheet.getRow(linea).getCell(19).getStringCellValue()) + "'";
                                        queryString += ",'" + String.valueOf(sheet.getRow(linea).getCell(20).getStringCellValue()).split("\\.")[0] + "'";
                                        queryString += "," + String.valueOf(sheet.getRow(linea).getCell(21).getStringCellValue());
                                        queryString += "," + String.valueOf(sheet.getRow(linea).getCell(22).getStringCellValue());
                                        queryString += "," + String.valueOf(sheet.getRow(linea).getCell(23).getStringCellValue());
                                        queryString += "," + String.valueOf(sheet.getRow(linea).getCell(24).getStringCellValue());
                                        queryString += "," + String.valueOf(sheet.getRow(linea).getCell(25).getStringCellValue());
                                        queryString += "," + String.valueOf(sheet.getRow(linea).getCell(26).getStringCellValue());
                                        queryString += ")";
                                    }


System.out.println("query proveedores=" + queryString);

                                    stQuery2.executeUpdate(queryString);

                                } //end for

                                ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().commit();
                                ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().setAutoCommit(true);

                            } catch (Exception ex1) {
                                try {
                                    ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().rollback();
                                    ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().setAutoCommit(true);
                                } catch (java.sql.SQLException sqlE) {
                                    //
                                }
                                System.out.println("Error al insertar registros de planilla en base de datos..Transaccion abortada..!");
                                Notification.show("Error al insertar registro de planilla en base de datos..Transaccion abortada..!", Notification.Type.ERROR_MESSAGE);
                                ex1.printStackTrace();

                                return;
                            }//endcatch//endcatch

                            Notification.show("Operación exitosa!", Notification.Type.HUMANIZED_MESSAGE);

                            cargarBtn.setEnabled(false);

                            proveedorTable.removeAllItems();

                        } else {
                            Notification.show("Operación cacelada!", Notification.Type.WARNING_MESSAGE);
                        }
                    }
                });
    }

}