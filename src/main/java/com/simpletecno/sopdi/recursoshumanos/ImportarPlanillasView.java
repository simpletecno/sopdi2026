/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.recursoshumanos;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.MultiFileUpload;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadFinishedHandler;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadStateWindow;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
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
public class ImportarPlanillasView extends VerticalLayout implements View {

    Statement stQuery = null;
    Statement stQuery1 = null;
    PreparedStatement stPreparedQuery = null;
    ResultSet rsRecords = null;
    ResultSet rsRecords1 = null;

    MarginInfo marginInfo;

    MultiFileUpload singleUpload;

    Button limpiarBtn;
    Button cargarBtn;

    public File planillaFile;
    public XSSFWorkbook workbook;
    public XSSFSheet sheet;
    private FileInputStream fileInputStream;

    Table planillasTable;

    public static Locale locale = new Locale("ES", "GT");
    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");

    BigDecimal totalDebe = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
    BigDecimal totalHaber = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);

    String codigoCC;
    UI mainUI;

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public ImportarPlanillasView() {
        this.mainUI = UI.getCurrent();

        marginInfo = new MarginInfo(true, true, true, true);

        setSpacing(true);

        Label titleLbl = new Label(empresaId + " " + empresaNombre + " IMPORTAR PLANILLAS");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h1_custom");

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

                planillaFile = targetFile;

                //   cargarBtn.setEnabled(true);
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
        singleUpload.getSmartUpload().setUploadButtonCaptions("Abrir archivo", "");

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

        VerticalLayout contenidoLayout;
        contenidoLayout = new VerticalLayout();
//        contenidoLayout.setSizeUndefined();
        contenidoLayout.setWidth("98%");
        contenidoLayout.setHeight("95%");
        contenidoLayout.setMargin(marginInfo);
        contenidoLayout.setSpacing(true);
        contenidoLayout.addStyleName("rcorners3");

        planillasTable = new Table();
        planillasTable.setWidth("100%");

        planillasTable.addContainerProperty("Fecha", String.class, "");
        planillasTable.addContainerProperty("Docto", String.class, "");
        planillasTable.addContainerProperty("Planilla", String.class, "");

        planillasTable.addContainerProperty("Cuenta", String.class, "");
        planillasTable.addContainerProperty("Descripción", String.class, "");
        planillasTable.addContainerProperty("Nombre", String.class, "");

        planillasTable.addContainerProperty("Debe", String.class, "");
        planillasTable.addContainerProperty("Haber", String.class, "");
        planillasTable.addContainerProperty("IdEmpleado", String.class, "");

        planillasTable.addContainerProperty("IdEmpresa", String.class, "");
        planillasTable.addContainerProperty("CodigoCC", String.class, "");

        planillasTable.setColumnAlignments(new Table.Align[]{
                Table.Align.LEFT, Table.Align.CENTER, Table.Align.CENTER,
                Table.Align.LEFT, Table.Align.LEFT, Table.Align.LEFT,
                Table.Align.RIGHT, Table.Align.RIGHT, Table.Align.CENTER,
                Table.Align.CENTER, Table.Align.CENTER
        });

        planillasTable.setColumnWidth("Fecha", 90);
        planillasTable.setColumnWidth("Docto", 60);
        planillasTable.setColumnWidth("Planilla", 60);
        planillasTable.setColumnWidth("Cuenta", 100);
        planillasTable.setColumnWidth("Descripción", 200);
        planillasTable.setColumnWidth("Nombre", 200);
        planillasTable.setColumnWidth("Debe", 90);
        planillasTable.setColumnWidth("Haber", 90);
        planillasTable.setColumnWidth("IdEmpleado", 60);
        planillasTable.setColumnWidth("IdEmpresa", 60);
        planillasTable.setColumnWidth("CodigoCC", 60);

        planillasTable.setFooterVisible(true);
        planillasTable.setColumnFooter("Nombre", "Cuadre");
        planillasTable.setColumnFooter("Debe", "0.00");
        planillasTable.setColumnFooter("Haber", "0.00");

        planillasTable.setSelectable(true);

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setMargin(false);

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setResponsive(true);
        titleLayout.setSpacing(true);
        titleLayout.setWidth("100%");
        titleLayout.setMargin(false);
        titleLayout.addComponents(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.MIDDLE_CENTER);
        titleLayout.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

        addComponent(titleLayout);
        setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        contenidoLayout.addComponent(planillasTable);
        contenidoLayout.setComponentAlignment(planillasTable, Alignment.MIDDLE_CENTER);
        cargarBtn = new Button("VALIDAR PLANILLA");
        cargarBtn.setIcon(FontAwesome.SAVE);
//        cargarBtn.setWidth(130,Sizeable.UNITS_PIXEL);
        cargarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (planillasTable.size() > 0) {
                    validarYGuardar();
                }
            }
        });

        Page.getCurrent().setTitle("Sopdi- Importar planilla");
        cargarBtn.setEnabled(false);
        cargarBtn.addStyleName(ValoTheme.BUTTON_LINK);
        cargarBtn.addStyleName(".v-button { text-decoration : underline;}");

        limpiarBtn = new Button("LIMPIAR");
        limpiarBtn.setIcon(FontAwesome.ERASER);
//        limpiarBtn.setWidth(130,Sizeable.UNITS_PIXELS);
        limpiarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                planillasTable.removeAllItems();
            }
        });

        buttonsLayout.addComponent(singleUpload);
        buttonsLayout.setComponentAlignment(singleUpload, Alignment.BOTTOM_CENTER);
        buttonsLayout.addComponent(cargarBtn);
        buttonsLayout.setComponentAlignment(cargarBtn, Alignment.BOTTOM_CENTER);
        buttonsLayout.addComponent(limpiarBtn);
        buttonsLayout.setComponentAlignment(limpiarBtn, Alignment.BOTTOM_LEFT);

        contenidoLayout.addComponent(buttonsLayout);
        contenidoLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);

        addComponent(contenidoLayout);
        setComponentAlignment(contenidoLayout, Alignment.MIDDLE_CENTER);
    }

    private void cargarPlanilla(File planillaFile) {

        planillasTable.removeAllItems();
        planillasTable.setColumnFooter("Debe", "Q.0.00");
        planillasTable.setColumnFooter("Haber", "Q.0.00");

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
                System.out.println(Utileria.getFechaDDMMYYYY(sheet.getRow(linea).getCell(0).getDateCellValue()));
                System.out.println(sheet.getRow(linea).getCell(1).getNumericCellValue());
                System.out.println(sheet.getRow(linea).getCell(3).getNumericCellValue());
                System.out.println(sheet.getRow(linea).getCell(4).getStringCellValue());
                System.out.println(sheet.getRow(linea).getCell(5).getStringCellValue());
                System.out.println(sheet.getRow(linea).getCell(6).getStringCellValue());
                System.out.println(sheet.getRow(linea).getCell(7).getRawValue());
                System.out.println(sheet.getRow(linea).getCell(8).getRawValue());
                System.out.println(String.valueOf(sheet.getRow(linea).getCell(9).getNumericCellValue()));
                System.out.println(String.valueOf(sheet.getRow(linea).getCell(10).getNumericCellValue()));
                System.out.println(sheet.getRow(linea).getCell(11).getRawValue());

                planillasTable.addItem(new Object[]{
                        Utileria.getFechaDDMMYYYY(sheet.getRow(linea).getCell(0).getDateCellValue()),
                        String.valueOf(sheet.getRow(linea).getCell(1).getNumericCellValue()).split("\\.")[0],//documento
                        String.valueOf(sheet.getRow(linea).getCell(3).getNumericCellValue()).split("\\.")[0],//planilla
                        String.valueOf(sheet.getRow(linea).getCell(4).getStringCellValue()),//cuenta
                        String.valueOf(sheet.getRow(linea).getCell(5).getStringCellValue()),//descripcion
                        String.valueOf(sheet.getRow(linea).getCell(6).getStringCellValue()),//nombre
                        "Q." + numberFormat.format(sheet.getRow(linea).getCell(7).getNumericCellValue()),
                        "Q." + numberFormat.format(sheet.getRow(linea).getCell(8).getNumericCellValue()),
//                        "Q." + numberFormat.format(Double.valueOf(sheet.getRow(linea).getCell(8).getStringCellValue())),
                        String.valueOf(sheet.getRow(linea).getCell(9).getNumericCellValue()).split("\\.")[0], //idEmpleado

                        String.valueOf(sheet.getRow(linea).getCell(10).getNumericCellValue()).split("\\.")[0],//idEmpresa
                        String.valueOf(sheet.getRow(linea).getCell(11).getStringCellValue())//codigocc
                }, planillasTable.size() + 1);

                totalDebe = totalDebe.add(new BigDecimal(sheet.getRow(linea).getCell(7).getRawValue())).setScale(2, BigDecimal.ROUND_HALF_UP);
                totalHaber = totalHaber.add(new BigDecimal(sheet.getRow(linea).getCell(8).getRawValue())).setScale(2, BigDecimal.ROUND_HALF_UP);

//System.out.println("IDEX="+String.valueOf(Double.valueOf(sheet.getRow(linea).getCell(1).getNumericCellValue()).intValue()));
            } //endfor

            planillasTable.setColumnFooter("Debe", "Q." + numberFormat.format(totalDebe.doubleValue()));
            planillasTable.setColumnFooter("Haber", "Q." + numberFormat.format(totalHaber.doubleValue()));

            System.out.println("...FIN...");

        } catch (Exception ex1) {
            new Notification("Error al intentar cargar las planillas del archivo EXCEL.",
                    ex1.getMessage(),
                    Notification.Type.ERROR_MESSAGE)
                    .show(Page.getCurrent());
            ex1.printStackTrace();
        }
        singleUpload.setEnabled(true);
    }

    void validarYGuardar() {

        if (planillasTable.size() == 0) {
            Notification.show("No exiten planillas para cargar, revise!", Notification.Type.WARNING_MESSAGE);
            return;
        }

        if (totalDebe.compareTo(totalDebe) != 0) {
            Notification.show("El debe no cuadra con el haber,  por favor revise!", Notification.Type.WARNING_MESSAGE);
            return;
        }

        ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de CARGAR esta planilla ?",
                "SI", "NO", new ConfirmDialog.Listener() {

                    public void onClose(ConfirmDialog dialog) {
                        if (dialog.isConfirmed()) {

                            String queryString;

                            dialog.setCaption("Procesando...");
                            dialog.setDescription(" Espere por favor.... ");
                            dialog.getOkButton().setEnabled(false);

                            try {

                                stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();

//                        //dd-mm-yyyy
//                        String fecha = String.valueOf(planillasTable.getItem(planillasTable.firstItemId()).getItemProperty("Fecha").getValue());
//                        String ultimoEncontado;
//                        String dia = fecha.substring(0, 2);
//                        String mes = fecha.substring(3, 5);
//                        String año = fecha.substring(6, 10);
//
//                        String codigoPartida = empresa + año + mes + dia + "3";
//
////System.out.println("pre-codigo partia  = " + codigoPartida);
//                        queryString = "";
//                        queryString += " select codigoPartida from contabilidad_partida ";
//                        queryString += " where codigoPartida like '" + codigoPartida + "%'";
//                        queryString += " order by codigoPartida desc ";
//
//                        try {
//                            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
//                            rsRecords = stQuery.executeQuery(queryString);
//
//                            if (rsRecords.next()) { //  encontrado                               
//
//                                ultimoEncontado = rsRecords.getString("codigoPartida").substring(11, 13);
//
//                                System.out.println("ultimo encontrado " + ultimoEncontado);
//
//                                codigoPartida += String.format("%02d", (Integer.valueOf(ultimoEncontado) + 1));
//
//                            } else {
//                                codigoPartida += "01";
//                            }
//
//                        } catch (Exception ex1) {
//                            System.out.println("Error al buscar el ultimo codigoPartida" + ex1.getMessage());
//                            Notification.show("Error al intentar formar identificador de partida.", Notification.Type.ERROR_MESSAGE);
//                            ex1.printStackTrace();
//                            return;
//                        }

                                String idNomenclatura;
                                String fecha;
                                String ultimoEncontado;
                                String dia;
                                String mes;
                                String año;

                                String codigoPartida = "";

                                ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().setAutoCommit(false);

                                stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

                                fileInputStream = new FileInputStream(planillaFile);

                                workbook = new XSSFWorkbook(fileInputStream);

                                sheet = workbook.getSheetAt(0);

                                String planillaAnterior = "";
                                String documentoAnterior = "";

                                int correlativo = 0;

                                for (int linea = 1; linea <= sheet.getLastRowNum(); linea++) {

                                    try {
                                        sheet.getRow(linea).getCell(0).getRawValue();
                                    }
                                    catch(Exception exNull) {
                                        break;
                                    }

                                    if(!planillaAnterior.equals(String.valueOf(sheet.getRow(linea).getCell(3).getNumericCellValue()))) {

                                        planillaAnterior = String.valueOf(sheet.getRow(linea).getCell(3).getNumericCellValue());

                                        documentoAnterior = String.valueOf(sheet.getRow(linea).getCell(1).getNumericCellValue());

                                        try {

                                            //0123456789
                                            //1234567890
                                            //yyyy-mm-dd
                                            fecha = Utileria.getFechaYYYYMMDD_1(sheet.getRow(linea).getCell(0).getDateCellValue());
                                            año = fecha.substring(0, 4);
                                            mes = fecha.substring(5, 7);
                                            dia = fecha.substring(8, 10);

                                            codigoPartida = empresaId + año + mes + dia + "4";

                                            //System.out.println("pre-codigo partia  = " + codigoPartida);
                                            queryString  = " SELECT codigoPartida FROM contabilidad_partida ";
                                            queryString += " WHERE codigoPartida LIKE '" + codigoPartida + "%'";
                                            queryString += " ORDER BY codigoPartida DESC ";

                                            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                                            rsRecords = stQuery.executeQuery(queryString);

                                            if (rsRecords.next()) { //  encontrado

                                                ultimoEncontado = rsRecords.getString("codigoPartida").substring(12, 15);
                                                correlativo = Integer.parseInt(ultimoEncontado) + 1;

//System.out.println("ultimo encontrado " + ultimoEncontado);

                                                codigoPartida += String.format("%03d", correlativo);

                                            } else {
                                                codigoPartida += "001";
                                                correlativo = 1;
                                            }

                                        } catch (Exception ex1) {
                                            System.out.println("Error al buscar el ultimo codigoPartida" + ex1.getMessage());
                                            Notification.show("Error al intentar formar identificador de partida.", Notification.Type.ERROR_MESSAGE);
                                            ex1.printStackTrace();
                                            return;
                                        }
                                    }
                                    else { // es la misma planilla ,  siguiente empleado (documento)

                                        if(!documentoAnterior.equals(String.valueOf(sheet.getRow(linea).getCell(1).getNumericCellValue()))) {
                                            documentoAnterior = String.valueOf(sheet.getRow(linea).getCell(1).getNumericCellValue());
                                            correlativo++;
                                            //0123456789012
                                            //1234567890123
                                            //eeddmmaaaatcc
                                            codigoPartida = codigoPartida.substring(0,12) + String.format("%03d", correlativo);
                                        }
                                    }

                                    codigoCC = codigoPartida;

                                    queryString = " SELECT IdNomenclatura FROM contabilidad_nomenclatura ";
                                    queryString += " WHERE NoCuenta = '" + sheet.getRow(linea).getCell(4).getStringCellValue() + "'";

                                    System.out.println(queryString);

                                    rsRecords1 = stQuery1.executeQuery(queryString);

                                    if (!rsRecords1.next()) {
                                        ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().rollback();
                                        ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().setAutoCommit(true);
                                        Notification.show("Error,  la cuenta " + sheet.getRow(linea).getCell(4).getStringCellValue() + " No existe en la tabla de nomenclatura de cuentas.", Notification.Type.ERROR_MESSAGE);
                                        return;
                                    }

                                    idNomenclatura = rsRecords1.getString("IdNomenclatura");

                                    queryString = " SELECT IdPartida FROM contabilidad_partida ";
                                    queryString += " WHERE  IdEmpresa = " + empresaId;
                                    queryString += " AND  SerieDocumento = '" + String.valueOf(sheet.getRow(linea).getCell(2).getStringCellValue()).split("\\.")[0] + "'";
                                    queryString += " AND  NumeroDocumento = '" + String.valueOf(sheet.getRow(linea).getCell(1).getNumericCellValue()).split("\\.")[0] + "'";
                                    queryString += " AND  IdProveedor     =  " + String.valueOf(sheet.getRow(linea).getCell(9).getNumericCellValue()).split("\\.")[0];
                                    queryString += " AND  Fecha = '" + Utileria.getFechaYYYYMMDD_1(sheet.getRow(linea).getCell(0).getDateCellValue()) + "'";
                                    queryString += " AND  TipoDocumento = 'PLANILLA'";
                                    queryString += " AND  IdNomenclatura = " + idNomenclatura;

                                    rsRecords1 = stQuery1.executeQuery(queryString);

                                    if (rsRecords1.next()) {
                                        ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().rollback();
                                        ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().setAutoCommit(true);
                                        Notification.show("Error, ya existe una planilla previamente resistrada para : " + String.valueOf(sheet.getRow(linea).getCell(6).getRawValue()), Notification.Type.ERROR_MESSAGE);
                                        return;
                                    }

                                    queryString = " INSERT INTO contabilidad_partida (IdEmpresa, Estatus, CodigoPartida, CodigoCC,";
                                    queryString += " TipoDocumento, Fecha, IdProveedor, NITProveedor, NombreProveedor,";
                                    queryString += " SerieDocumento, NumeroDocumento, IdNomenclatura, MonedaDocumento, Debe, Haber,";
                                    queryString += " DebeQuetzales, HaberQuetzales, TipoCambio, Descripcion, ";
                                    queryString += " CreadoUsuario, CreadoFechaYHora, Saldo, MontoAutorizadoPagar)";
                                    queryString += " VALUES ";
                                    queryString += " (";
                                    queryString += empresaId;
                                    queryString += ",'REVISADO'";
                                    queryString += ",'" + codigoPartida + "'";

                                    if(sheet.getRow(linea).getCell(11).getStringCellValue().trim().isEmpty()  // CODIGO CC
                                    && idNomenclatura.equals("87")){ ///CUENTA
                                        queryString += ",'" + codigoPartida + "'"; //codigocc
                                    }else{
                                        queryString += ",'" + sheet.getRow(linea).getCell(11).getStringCellValue().replace("'", "") + "'"; //codigocc
                                    }
                                    queryString += ",'PLANILLA'";
                                    queryString += ",'" + Utileria.getFechaYYYYMMDD_1(sheet.getRow(linea).getCell(0).getDateCellValue()) + "'";
                                    queryString += "," + String.valueOf(sheet.getRow(linea).getCell(9).getNumericCellValue()).split("\\.")[0];
                                    queryString += ",''";// + nitProveedotTxt.getValue() + "'";
                                    queryString += ",'" + sheet.getRow(linea).getCell(6).getStringCellValue().split("\\.")[0] + "'";
                                    queryString += ",'" + String.valueOf(sheet.getRow(linea).getCell(3).getNumericCellValue()).split("\\.")[0] + "'";
                                    queryString += ",'" + String.valueOf(sheet.getRow(linea).getCell(1).getNumericCellValue()).split("\\.")[0] + "'";
                                    queryString += "," + idNomenclatura;
                                    queryString += ",'QUETZALES'";

                                    if (sheet.getRow(linea).getCell(7).getNumericCellValue() > 0.00) {
                                        queryString += "," + sheet.getRow(linea).getCell(7).getNumericCellValue(); //DEBE
                                        queryString += ",0.00"; //HABER
                                        queryString += "," + sheet.getRow(linea).getCell(7).getNumericCellValue(); //DEBE Q
                                        queryString += ",0.00"; //HABER Q
                                    } else {
                                        queryString += ",0.00"; //DEBE
                                        queryString += "," + sheet.getRow(linea).getCell(8).getNumericCellValue(); //HABER
                                        queryString += ",0.00"; //DEBE Q
                                        queryString += "," + sheet.getRow(linea).getCell(8).getNumericCellValue(); //HABER Q
                                    }
                                    queryString += ",1.00"; //tasa cambio

                                    queryString += ",'PLANILLA : " + String.valueOf(sheet.getRow(linea).getCell(3).getNumericCellValue()) + " " + String.valueOf(sheet.getRow(linea).getCell(5).getStringCellValue()) + "'";
                                    queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                                    queryString += ",current_timestamp";

                                    if (sheet.getRow(linea).getCell(4).getStringCellValue().equals("21104001")) { //sueldos por pagar
                                        queryString += "," + sheet.getRow(linea).getCell(8).getNumericCellValue(); //saldo
                                        queryString += "," + sheet.getRow(linea).getCell(8).getNumericCellValue(); //montoautorizadopagar
                                    }
                                    else {
                                        queryString += ",0.00"; //saldo
                                        queryString += ",0.00"; //montoautorizadopagar
                                    }

                                    queryString += ")";

System.out.println("queryplanilla=" + queryString);

                                    stQuery.executeUpdate(queryString);

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

                            planillasTable.removeAllItems();

                        } else {
                            Notification.show("Operación cacelada!", Notification.Type.WARNING_MESSAGE);
                        }
                    }
                });
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - Importar planilla");
    }
}