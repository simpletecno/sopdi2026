/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.contabilidad;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
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
import java.util.ListIterator;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author joseaguirre
 */ 
public class ImportarPartidaInicial extends Window {

    Statement stQuery = null;
    Statement stQuery1 = null;
    PreparedStatement stPreparedQuery = null;
    ResultSet rsRecords = null;
    ResultSet rsRecords1 = null;

    MarginInfo marginInfo;

    String empresa;
    Button nextBtn;
    Button prevBtn;
    List<String> empresaLst;
    Label empresaLbl;

    MultiFileUpload singleUpload;

    Button limpiarBtn;
    Button cargarBtn;

    public File partidaFile;
    public XSSFWorkbook workbook;
    public XSSFSheet sheet, sheet2;
    private FileInputStream fileInputStream;

    Table partidaTable;

    public static Locale locale = new Locale("ES", "GT");
    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");
    static DecimalFormat numberFormat2 = new DecimalFormat("##,###");

    BigDecimal totalDebe = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
    BigDecimal totalHaber = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);

    BigDecimal totalDebeQ = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
    BigDecimal totalHaberQ = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);

    UI mainUI;

    public ImportarPartidaInicial() {
        this.mainUI = UI.getCurrent();

        setWidth("70%");
//        setHeight("100%");
        center();
        
        marginInfo = new MarginInfo(true, true, true, true);

        empresaLbl = new Label("");
        empresaLbl.setWidth("320px");
        empresaLbl.addStyleName(ValoTheme.LABEL_H2);

//        setCaption(((SopdiUI) mainUI).sessionInformation.getStrCompanyName() + " - Importar archivo EXCEL de planilla de la empresa : " + String.valueOf(selectEmpresa.getValue()));

        Label titleLbl = new Label("IMPORTAR PARTIDA INICIAL");
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

                cargarPartida(targetFile);

                partidaFile = targetFile;

                //   cargarBtn.setEnabled(true);
            } catch (java.io.IOException fIoEx) {
                fIoEx.printStackTrace();
                Notification.show("Error al cargar el archivo adjunto!", Notification.Type.ERROR_MESSAGE);
                return;
            }
        };

        UploadStateWindow window = new UploadStateWindow();

        singleUpload = new MultiFileUpload(handler, window, false);
//        singleUpload.setCaption("Cargar archivo");
//        singleUpload2.setRootDirectory(newDoc.getAbsolutePath());
//        singleUpload.setPanelCaption("Buscar");
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
        contenidoLayout.setSizeUndefined();
        contenidoLayout.setSpacing(true);
        contenidoLayout.addStyleName("rcorners3");

        partidaTable = new Table();
        partidaTable.setWidth("100%");

        partidaTable.addContainerProperty("Cuenta", String.class, "");
        partidaTable.addContainerProperty("Descripcion", String.class, "");
        partidaTable.addContainerProperty("Debe", String.class, "");
        partidaTable.addContainerProperty("Haber", String.class, "");
        partidaTable.addContainerProperty("Moneda", String.class, "");
        partidaTable.addContainerProperty("TipoCambio", String.class, "");
        partidaTable.addContainerProperty("DebeQuetzales", String.class, "");
        partidaTable.addContainerProperty("HaberQuetzales", String.class, "");

        partidaTable.setColumnAlignments(new Table.Align[]{
            Table.Align.LEFT, Table.Align.LEFT, Table.Align.RIGHT, Table.Align.RIGHT
        });

        partidaTable.setColumnWidth("Cuenta", 100);
        partidaTable.setColumnWidth("Descripción", 160);
        partidaTable.setColumnWidth("Debe", 125);
        partidaTable.setColumnWidth("Haber", 125);
        partidaTable.setColumnWidth("Moneda", 125);
        partidaTable.setColumnWidth("TipoCambio", 125);
        partidaTable.setColumnWidth("DebeQuetzales", 125);
        partidaTable.setColumnWidth("HaberQuetzales", 125);

        partidaTable.setFooterVisible(true);
        partidaTable.setColumnFooter("Descripcion", "Cuadre");
        partidaTable.setColumnFooter("Debe", "0.00");
        partidaTable.setColumnFooter("Haber", "0.00");
        partidaTable.setColumnFooter("DebeQuetzales", "0.00");
        partidaTable.setColumnFooter("HaberQuetzales", "0.00");

        partidaTable.setSelectable(true);

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setMargin(false);

        final int EPREV = 0;
        final int ENEXT = 1;

        empresaLst = new ArrayList<String>();

        llenarComboEmpresa();

        ListIterator<String> listIterator = empresaLst.listIterator();

        prevBtn = new Button("Anterior");
        prevBtn.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        prevBtn.addStyleName("flechas");
        prevBtn.addStyleName(ValoTheme.BUTTON_LARGE);
        prevBtn.setIcon(FontAwesome.ARROW_LEFT);
        prevBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (listIterator.hasPrevious()) {
                    prevBtn.setEnabled(true);
                    nextBtn.setEnabled(true);
                    empresaLbl.setValue(listIterator.previous());
                    empresa = empresaLbl.getValue().substring(1, 3);

                    if (partidaTable.size() > 0) {
                        partidaTable.removeAllItems();
                        cargarBtn.setEnabled(false);
                    }
                } else {
                    prevBtn.setEnabled(false);
                }
            }
        });

        nextBtn = new Button("Siguiente");
        nextBtn.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        nextBtn.addStyleName("flechas");
        nextBtn.addStyleName(ValoTheme.BUTTON_LARGE);
        nextBtn.setIcon(FontAwesome.ARROW_RIGHT);
        nextBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (listIterator.hasNext()) {
                    prevBtn.setEnabled(true);
                    nextBtn.setEnabled(true);

                    empresaLbl.setValue(listIterator.next());
                    empresa = empresaLbl.getValue().substring(1, 3);

                    if (partidaTable.size() > 0) {
                        partidaTable.removeAllItems();
                        cargarBtn.setEnabled(false);
                    }
                } else {
                    nextBtn.setEnabled(false);
                }
            }
        });

        HorizontalLayout empresaLayout = new HorizontalLayout();
        empresaLayout.setSizeUndefined();        
        empresaLayout.setResponsive(true);
     //   empresaLayout.setMargin(true);
        empresaLayout.setSpacing(true);
        empresaLayout.addStyleName("rcorners4");
        empresaLayout.addComponents(empresaLbl, prevBtn, nextBtn,singleUpload);
        empresaLayout.setComponentAlignment(prevBtn, Alignment.MIDDLE_LEFT);
        empresaLayout.setComponentAlignment(nextBtn, Alignment.MIDDLE_LEFT);
        empresaLayout.setComponentAlignment(singleUpload, Alignment.TOP_LEFT);        
        
        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setSpacing(true);
        titleLayout.setResponsive(true);
         titleLayout.setSizeUndefined();   
      //  titleLayout.setWidth("100%");
      //  titleLayout.setMargin(false);
        titleLayout.addComponents(empresaLayout, titleLbl);
        titleLayout.setComponentAlignment(empresaLayout, Alignment.TOP_LEFT);        
        titleLayout.setComponentAlignment(titleLbl, Alignment.TOP_RIGHT);
      

        contenidoLayout.addComponent(titleLayout);
      //  setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        contenidoLayout.addComponent(partidaTable);
        contenidoLayout.setComponentAlignment(partidaTable, Alignment.MIDDLE_CENTER);
        cargarBtn = new Button("APLICAR PARTIDA");
        cargarBtn.setIcon(FontAwesome.SAVE);
//        cargarBtn.setWidth(130,Sizeable.UNITS_PIXEL);
        cargarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (partidaTable.size() > 0) {
                    validarYGuardar();
                }
            }
        });

        Page.getCurrent().setTitle(String.valueOf(empresaLbl.getValue()) + " - Importar partida inicial");
        cargarBtn.setEnabled(false);
        cargarBtn.addStyleName(ValoTheme.BUTTON_LINK);
        cargarBtn.addStyleName(".v-button { text-decoration : underline;}");

        limpiarBtn = new Button("LIMPIAR");
        limpiarBtn.setIcon(FontAwesome.ERASER);
//        limpiarBtn.setWidth(130,Sizeable.UNITS_PIXELS);
        limpiarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                partidaTable.removeAllItems();
            }
        });

        buttonsLayout.addComponent(limpiarBtn);
        buttonsLayout.setComponentAlignment(limpiarBtn, Alignment.BOTTOM_LEFT);
        buttonsLayout.addComponent(cargarBtn);

        contenidoLayout.addComponent(buttonsLayout);
        contenidoLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);

        String empresaString = String.valueOf(empresaLst.iterator().next());

        empresaLbl.setValue(empresaString);

        empresa = empresaString.substring(1, 3);

        if (partidaTable != null) {
            empresa = empresaString.substring(1, 3);
        }
        
        setContent(contenidoLayout);
    }

    public void llenarComboEmpresa() {
        String queryString = " SELECT * from contabilidad_empresa";

        try {
            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords1 = stQuery1.executeQuery(queryString);

            while (rsRecords1.next()) { //  encontrado                
                empresaLst.add("(" + rsRecords1.getString("IdEmpresa") + ") " + rsRecords1.getString("Empresa"));

                if (empresaLst.size() == 1) {
                    empresaLbl.setValue("(" + rsRecords1.getString("IdEmpresa") + ") " + rsRecords1.getString("Empresa"));
                }
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar empresas: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    private void cargarPartida(File partidaFile) {

        partidaTable.removeAllItems();
        partidaTable.setColumnFooter("Debe", "0.00");
        partidaTable.setColumnFooter("Haber", "0.00");
        partidaTable.setColumnFooter("DebeQuetzales", "0.00");
        partidaTable.setColumnFooter("HaberQuetzales", "0.00");

        singleUpload.setEnabled(false);

        try {

            fileInputStream = new FileInputStream(partidaFile);

            workbook = new XSSFWorkbook(fileInputStream);

            sheet = workbook.getSheetAt(2);

System.out.println("\n Total lineas en archivo=" + sheet.getLastRowNum());
System.out.println("...INICIO...");

            cargarBtn.setEnabled(true);

            for (int linea = 1; linea <= sheet.getLastRowNum(); linea++) {
                
                if (sheet.getRow(linea).getCell(0).getRawValue() == null) {
                    continue;
                }
                if (!sheet.getRow(linea).getCell(0).getRawValue().equals(empresa)) {
                    Notification.show("No es posible cargar el documento. Por favor revisar que la empresa del"
                            + " programa coincida con la del documento", Notification.Type.ERROR_MESSAGE);
                    cargarBtn.setEnabled(false);
                    break;
                }
                partidaTable.addItem(new Object[]{
                    sheet.getRow(linea).getCell(1).getRawValue(), // cuenta
                    sheet.getRow(linea).getCell(2).getStringCellValue(), // descripcion
                    numberFormat.format(sheet.getRow(linea).getCell(3).getNumericCellValue()), //debe
                    numberFormat.format(sheet.getRow(linea).getCell(4).getNumericCellValue()), //haber
                    sheet.getRow(linea).getCell(5).getStringCellValue(), // moneda
                    sheet.getRow(linea).getCell(6).getStringCellValue(), // tipocambio
                    numberFormat.format(sheet.getRow(linea).getCell(7).getNumericCellValue()), //debe Q
                    numberFormat.format(sheet.getRow(linea).getCell(8).getNumericCellValue())  //haber Q
                }, partidaTable.size() + 1);

                totalDebe = totalDebe.add(new BigDecimal(sheet.getRow(linea).getCell(3).getNumericCellValue())).setScale(2, BigDecimal.ROUND_HALF_UP);
                totalHaber = totalHaber.add(new BigDecimal(sheet.getRow(linea).getCell(4).getNumericCellValue())).setScale(2, BigDecimal.ROUND_HALF_UP);
                totalDebeQ = totalDebe.add(new BigDecimal(sheet.getRow(linea).getCell(7).getNumericCellValue())).setScale(2, BigDecimal.ROUND_HALF_UP);
                totalHaberQ = totalHaber.add(new BigDecimal(sheet.getRow(linea).getCell(8).getNumericCellValue())).setScale(2, BigDecimal.ROUND_HALF_UP);

//System.out.println("IDEX="+String.valueOf(Double.valueOf(sheet.getRow(linea).getCell(1).getNumericCellValue()).intValue()));
            } //endfor

            partidaTable.setColumnFooter("Debe", numberFormat.format(totalDebe.doubleValue()));
            partidaTable.setColumnFooter("Haber", numberFormat.format(totalHaber.doubleValue()));
            partidaTable.setColumnFooter("DebeQuetzales", numberFormat.format(totalDebeQ.doubleValue()));
            partidaTable.setColumnFooter("HaberQuetzales", numberFormat.format(totalHaberQ.doubleValue()));

System.out.println("...FIN...");

        } catch (Exception ex1) {
            new Notification("Error al intentar cargar la partida del archivo EXCEL.",
                    ex1.getMessage(),
                    Notification.Type.ERROR_MESSAGE)
                    .show(Page.getCurrent());
            ex1.printStackTrace();
        }
        singleUpload.setEnabled(true);
    }

    void validarYGuardar() {

        if (partidaTable.size() == 0) {
            Notification.show("No exite partida para cargar, revise!", Notification.Type.WARNING_MESSAGE);
            return;
        }

        if (totalDebeQ.compareTo(totalDebeQ) != 0) {
            Notification.show("El debe no cuadra con el haber,  por favor revise!", Notification.Type.WARNING_MESSAGE);
            return;
        }

        ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de GUARDAR esta partida de la empresa : " + String.valueOf(empresaLbl.getValue()) + ",  en base de datos?",
                "SI", "NO", new ConfirmDialog.Listener() {

            public void onClose(ConfirmDialog dialog) {
                if (dialog.isConfirmed()) {

                    String queryString;

                    try {

                        stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();

                        //dd-mm-yyyy

                        String codigoPartida = empresa + Utileria.getFechaYYYYMMDD_1(new java.util.Date()).substring(0,4) + "01010001";

                        String idNomenclatura;

                        ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().setAutoCommit(false);

                        stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

                        fileInputStream = new FileInputStream(partidaFile);

                        workbook = new XSSFWorkbook(fileInputStream);

                        sheet = workbook.getSheetAt(2);

                        for (int linea = 1; linea <= sheet.getLastRowNum(); linea++) {

                            if (sheet.getRow(linea).getCell(0).getRawValue() == null) {
                                continue;
                            }

                            queryString = " Select IdNomenclatura From contabilidad_nomenclatura ";
                            queryString += " Where NoCuenta = '" + sheet.getRow(linea).getCell(1).getRawValue() + "'";

                            System.out.println(queryString);

                            rsRecords1 = stQuery1.executeQuery(queryString);

                            if (!rsRecords1.next()) {
                                ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().rollback();
                                ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().setAutoCommit(true);
                                Notification.show("Error,  la cuenta " + sheet.getRow(linea).getCell(1).getRawValue() + " No existe en la tabla de nomenclatura de cuentas.", Notification.Type.ERROR_MESSAGE);
                                return;
                            }

                            idNomenclatura = rsRecords1.getString("IdNomenclatura");

                            queryString = " Insert Into contabilidad_partida (IdEmpresa, Estatus, CodigoPartida, CodigoCC, ";
                            queryString += " TipoDocumento, Fecha, IdProveedor, NITProveedor, NombreProveedor,";
                            queryString += " SerieDocumento, NumeroDocumento, IdNomenclatura, MonedaDocumento, Debe, Haber,";
                            queryString += " DebeQuetzales, HaberQuetzales, TipoCambio, Saldo,";
                            queryString += " Descripcion, CreadoUsuario, CreadoFechaYHora)";
                            queryString += " Values ";
                            queryString += " (";
                            queryString += empresa;
                            queryString += ",'INGRESADO'";
                            queryString += ",'" + codigoPartida + "'";
                            queryString += ",'" + codigoPartida + "'"; //codigocc
                            queryString += ",'PARTIDA APERTURA'";
                            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(new java.util.Date()).substring(0,4) + "0101'";
                            queryString += ",0";
                            queryString += ",''";// + nitProveedotTxt.getValue() + "'";
                            queryString += ",''";
                            queryString += ",''";
                            queryString += ",''";
                            queryString += "," + idNomenclatura;
                            queryString += ",'" + sheet.getRow(linea).getCell(5).getStringCellValue() + "'"; // moneda
                            queryString += "," + sheet.getRow(linea).getCell(3).getNumericCellValue(); //DEBE
                            queryString += "," + sheet.getRow(linea).getCell(4).getNumericCellValue(); //HABER
                            queryString += "," + sheet.getRow(linea).getCell(7).getNumericCellValue(); //DEBE Q
                            queryString += "," + sheet.getRow(linea).getCell(8).getNumericCellValue(); //DEBE Q
                            queryString += "," + sheet.getRow(linea).getCell(6).getNumericCellValue(); //TIPO CAMBIO
                            queryString += ",0.00"; //saldo
                            queryString += ",'PARTIDA APERTURA'";
                            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                            queryString += ",current_timestamp";

                            queryString += ")";

System.out.println(queryString);

//                            stQuery.executeUpdate(queryString);
//
//                            queryString =  " Insert Into contabilidad_balance_saldo (IdEmpresa, IdNomenclatura, AnioMesCierre,";
//                            queryString += " SaldoAnterior, Debe, Haber, SaldoFinal ) Values (";
//                            queryString += " "  + empresa;
//                            queryString += ","  + idNomenclatura;
//                            queryString += ",'" + 201712 + "'";
//                            queryString += ","  + "0.00";
//                            queryString += ","  + sheet.getRow(linea).getCell(3).getNumericCellValue();
//                            queryString += ","  + sheet.getRow(linea).getCell(4).getNumericCellValue();
//                            queryString += ","  + (sheet.getRow(linea).getCell(3).getNumericCellValue() - sheet.getRow(linea).getCell(4).getNumericCellValue());
//                            queryString += ")";
//
//                            stQuery.executeUpdate(queryString);
                            
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

                    partidaTable.removeAllItems();

                } else {
                    Notification.show("Operación cacelada!", Notification.Type.WARNING_MESSAGE);
                }
            }
        });

    }
}
