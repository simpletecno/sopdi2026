/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.compras;

import com.simpletecno.sopdi.configuracion.EnvironmentVars;
import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.sun.istack.logging.Logger;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.SelectionEvent;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.MultiFileUpload;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadFinishedHandler;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadStateWindow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.ui.NumberField;

import java.io.*;
import java.nio.file.Files;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

/**
 *
 * @author joseaguirre
 */
public class ImportarFelSatView extends VerticalLayout implements View {

    Grid ordenCompraGrid;
    public IndexedContainer ordenCompraContainer = new IndexedContainer();
    static final String ID_PROPERTY = "Id";
    static final String NOC_PROPERTY = "NOC";
    static final String TIPO_PROPERTY = "Tipo";
    static final String FECHA_PROPERTY = "Fecha";
    static final String MONEDA_PROPERTY = "Moneda";
    static final String TOTAL_PROPERTY = "Total";

    Statement stQuery = null;
    Statement stQuery1 = null;
    Statement stQuery2 = null;
    PreparedStatement stPreparedQuery = null;
    ResultSet rsRecords = null;
    ResultSet rsRecords1 = null;

    MarginInfo marginInfo;

    MultiFileUpload singleUpload;

    IndexedContainer facturasFelContainer = new IndexedContainer();
    final Grid facturasFelGrid = new Grid("Facturas FEL SOPDI", facturasFelContainer);
    Grid.FooterRow footerFacturas;

    public File planillaFile;
    public XSSFWorkbook workbook;
    public XSSFSheet sheet;

    public static Locale locale = new Locale("ES", "GT");
    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");

    ComboBox centroCostoCbx = new ComboBox("Centro de costo");
    ComboBox proveedorAbastoCbx = new ComboBox("Proveedor o Abastos");
    ComboBox doctoAfectaCbx = new ComboBox("Documento afecta");

    FormLayout formLayout;

    String queryString;

    UI mainUI;
    EnvironmentVars environmentsVars;
    StreamResource pdfStreamResource = null;

    int facturasYaRegistradas = 0;
    int facturasContabilizadas = 0;
    int facturasAnuladas = 0;

    String UUID = "";

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public ImportarFelSatView() {

        environmentsVars = new EnvironmentVars();

        this.mainUI = UI.getCurrent();

        marginInfo = new MarginInfo(true, true, true, true);

//        setCaption(((SopdiUI) mainUI).sessionInformation.getStrCompanyName() + " - Importar archivo EXCEL de planilla de la empresa : " + String.valueOf(selectEmpresa.getValue()));
        setSpacing(true);

        Label titleLbl = new Label(empresaId + " " + empresaNombre + " IMPORTAR FEL SAT");
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
                OutputStream outStream = Files.newOutputStream(targetFile.toPath());
                outStream.write(buffer);
                outStream.close();
                stream.close();

                System.out.println("\ntargetFile = " + fileName);

                cargarArchivo(targetFile);

                planillaFile = targetFile;

                //   cargarBtn.setEnabled(true);
            } catch (java.io.IOException fIoEx) {
                fIoEx.printStackTrace();
                Notification.show("Error al cargar el archivo FEL SAT adjunto!", Notification.Type.ERROR_MESSAGE);
            }
        };

        UploadStateWindow window = new UploadStateWindow();

        singleUpload = new MultiFileUpload(handler, window, false);
//        singleUpload.setCaption("Cargar archivo");
//        singleUpload2.setRootDirectory(newDoc.getAbsolutePath());
//        singleUpload.setPanelCaption("Buscar");
        singleUpload.setIcon(FontAwesome.UPLOAD);
        singleUpload.setImmediate(true);
        singleUpload.addStyleName(ValoTheme.BUTTON_PRIMARY);
        singleUpload.getSmartUpload().setUploadButtonCaptions("Cargar archivo FEL SAT (Excel xlsx)", "");

        JavaScript.getCurrent().execute("document.getElementsByClassName('gwt-FileUpload')[0].setAttribute('accept', '.xlx')");
        JavaScript.getCurrent().execute("document.getElementsByClassName('gwt-FileUpload')[0].setAttribute('accept', '.xlsx')");

        List<String> acceptedMimeTypes = new ArrayList<>();
//        acceptedMimeTypes.add("application/octet-stream");
        acceptedMimeTypes.add("application/ovnd.ms-excel");
        acceptedMimeTypes.add("application/msexcel");
        acceptedMimeTypes.add("application/x-msexcel");
        acceptedMimeTypes.add("application/x-ms-excel");
        acceptedMimeTypes.add("application/x-excel");
        acceptedMimeTypes.add("application/x-dos_ms_excel");
        acceptedMimeTypes.add("application/xls");
        acceptedMimeTypes.add("application/x-xls");
        //       singleUpload.setAcceptedMimeTypes(acceptedMimeTypes);

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

        crearGrid();
        documentosCargados();

        crearLayouForm();
    }

    private void crearGrid() {
/**
 IdEmpresa,FechaEmision, NumeroAutorizacion, TipoDTE, Serie, Numero, IdProveedor, NitProveedor, NombreProveedor,
 CodigoEstablecimiento, NombreEstablecimiento,IdReceptor, NombreReceptor, NitCertificador, NombreCertificador,
 Moneda, Monto, Estatus, IVA,IDP, TurismoHospedaje,  TurismoPasajes, TimbrePrensa, Bomberos, BebidasAlcoholicas,
 Tabaco, Cemento, BebidasNoAlcoholicas, TarifaPortuaria, FechaCertificacion, Costo
 **/

        facturasFelContainer.addContainerProperty("id", Object.class, 0);
        facturasFelContainer.addContainerProperty("empresa", String.class, empresaId);
        facturasFelContainer.addContainerProperty("fechaEmision", String.class, "");
        facturasFelContainer.addContainerProperty("numeroAutorizacion", String.class, "");
        facturasFelContainer.addContainerProperty("tipoDte", String.class, "");
        facturasFelContainer.addContainerProperty("estatus", String.class, null);
        facturasFelContainer.addContainerProperty("serie", String.class, null);
        facturasFelContainer.addContainerProperty("numero", String.class, null);
        facturasFelContainer.addContainerProperty("idProveedor", String.class, "");
        facturasFelContainer.addContainerProperty("nitProveedor", String.class, "");
        facturasFelContainer.addContainerProperty("proveedor", String.class, "");
        facturasFelContainer.addContainerProperty("codigoEstablecimiento", String.class, ""); //10
        facturasFelContainer.addContainerProperty("establecimiento", String.class, "");
        facturasFelContainer.addContainerProperty("idReceptor", String.class, "");
        facturasFelContainer.addContainerProperty("receptor", String.class, "");
        facturasFelContainer.addContainerProperty("nitCertificador", String.class, "");
        facturasFelContainer.addContainerProperty("certificador", String.class, "");
        facturasFelContainer.addContainerProperty("moneda", String.class, null);
        facturasFelContainer.addContainerProperty("monto", String.class, null);
        facturasFelContainer.addContainerProperty("iva", String.class, null);
        facturasFelContainer.addContainerProperty("idp", String.class, null); //20
        facturasFelContainer.addContainerProperty("turismoHospedaje", String.class, null);
        facturasFelContainer.addContainerProperty("turismoPasaje", String.class, null);
        facturasFelContainer.addContainerProperty("timbrePrensa", String.class, null);
        facturasFelContainer.addContainerProperty("bomberos", String.class, null);
        facturasFelContainer.addContainerProperty("tasaMunicipal", String.class, null); //25
        facturasFelContainer.addContainerProperty("bebidasAlcoholicas", String.class, null);
        facturasFelContainer.addContainerProperty("tabaco", String.class, null);
        facturasFelContainer.addContainerProperty("cemento", String.class, null);
        facturasFelContainer.addContainerProperty("bebidasNoAlcoholicas", String.class, null);
        facturasFelContainer.addContainerProperty("tarifaPortuaria", String.class, null); //30
        facturasFelContainer.addContainerProperty("fechaCertificacion", String.class, null);
        facturasFelContainer.addContainerProperty("costo", String.class, null); //32
//        facturasFelContainer.addContainerProperty("pdf", String.class, null); //33

        facturasFelGrid.setImmediate(true);
        facturasFelGrid.setHeightMode(HeightMode.ROW);
        facturasFelGrid.setHeightByRows(10);
        facturasFelGrid.setWidth("100%");
        facturasFelGrid.setResponsive(true);
        facturasFelGrid.setEditorBuffered(true);
        facturasFelGrid.setEditorBuffered(false);

        facturasFelGrid.addStyleName("rcorners1");

        facturasFelGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {

            if ("monto".equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if ("idProveedor".equals(cellReference.getPropertyId())) {
                if (String.valueOf(cellReference.getProperty().getValue()).equals("0")) {
                    return "colred";
                } else {
                    return null;
                }
            } else if ("estatus".equals(cellReference.getPropertyId())) {
                if (String.valueOf(cellReference.getProperty().getValue()).equals("PORANULAR")) {
                    return "colred";
                } else {
                    return null;
                }
            } else {
                return null;
            }

        });
//        facturasFelGrid.getColumn("pdf").setRenderer(new ButtonRenderer(e -> {
//                VerCambiarImagen(e);
//        }));


        facturasFelGrid.addSelectionListener(new SelectionEvent.SelectionListener() {
            @Override
            public void select(SelectionEvent event) {
                if (facturasFelGrid.getSelectedRow() != null) {
                    if (String.valueOf(facturasFelGrid.getContainerDataSource()
                            .getItem(facturasFelGrid.getSelectedRow()).getItemProperty("tipoDte")
                            .getValue()).equals("NOTA DE CREDITO COMPRA")) {
                        llenarComboDoctoAfecta();
                    }
                    formLayout.setCaption("Datos para contabilizar documento : "
                            + facturasFelContainer.getContainerProperty(facturasFelGrid.getSelectedRow(), "serie")
                            .getValue() + " "
                            + facturasFelContainer.getContainerProperty(facturasFelGrid.getSelectedRow(), "numero")
                            .getValue());
//                    liquidadorCbx.select(null);
                    centroCostoCbx.select(null);
                    doctoAfectaCbx.select(null);
                    proveedorAbastoCbx.setValue("Proveedor");
                    proveedorAbastoCbx.select("Proveedor");

                    queryString = "SELECT EsAbastos FROM proveedor WHERE IdProveedor = " + facturasFelContainer.getContainerProperty(facturasFelGrid.getSelectedRow(), "idProveedor").getValue();

                    try {
                        rsRecords1 = stQuery1.executeQuery(queryString);

                        if (rsRecords1.next()) {
                            if (rsRecords1.getBoolean("EsAbastos")) {
                                proveedorAbastoCbx.setValue("Abastos");
                            }
                            else {
                                proveedorAbastoCbx.setValue("Proveedor");
                            }
                        }
                    }
                    catch (SQLException ex) {
                        Logger.getLogger(ImportarFelSatView.class).log(Level.SEVERE, null, ex);
                    }

                    contabilizar(facturasFelGrid.getSelectedRow(), Integer.parseInt(facturasFelContainer.getContainerProperty(facturasFelGrid.getSelectedRow(), "id").getValue().toString()), "");
                }
            }
        });

        facturasFelGrid.getColumn("id").setHidable(true).setHidden(true);
        facturasFelGrid.getColumn("empresa").setHidable(true).setHidden(true);
        facturasFelGrid.getColumn("fechaEmision").setExpandRatio(1);
        facturasFelGrid.getColumn("tipoDte").setExpandRatio(1);
        facturasFelGrid.getColumn("idProveedor").setExpandRatio(1);
        facturasFelGrid.getColumn("proveedor").setExpandRatio(5);
        facturasFelGrid.getColumn("nitProveedor").setExpandRatio(1);
        facturasFelGrid.getColumn("serie").setExpandRatio(2);
        facturasFelGrid.getColumn("numero").setExpandRatio(2);
//        facturasFelGrid.getColumn("moneda").setExpandRatio(1);
//        facturasFelGrid.getColumn("monto").setExpandRatio(1);

        footerFacturas = facturasFelGrid.appendFooterRow();
//        footerFacturas.getCell("proveedor").setText("Total FEL SAT : ");
        footerFacturas.getCell("numeroAutorizacion").setText("0 documentos");
        footerFacturas.getCell("tipoDte").setText("0.00");

        VerticalLayout cargaArchivoLaout = new VerticalLayout();
        cargaArchivoLaout.setSizeFull();
        cargaArchivoLaout.setResponsive(true);

        cargaArchivoLaout.addComponent(facturasFelGrid);

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setMargin(false);

        Page.getCurrent().setTitle("Sopdi- Importar FEL SAT");

        Button pdfFile = new Button("Visualizar PDF");
        pdfFile.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        pdfFile.setIcon(FontAwesome.FILE_PDF_O);
        pdfFile.addClickListener(e -> {
            if (facturasFelGrid.getSelectedRow() == null) {
                Notification.show("POR FAVOR ELIJA UN DOCUMENTO DEL LISTADO.", Notification.Type.WARNING_MESSAGE);
                return;
            }
            visualizarPdf();
        });

        Button ingresoManualBtn = new Button("Ingreso manual de RECIBO NO FEL");
        ingresoManualBtn.setIcon(FontAwesome.PLUS_CIRCLE);
        ingresoManualBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        ingresoManualBtn.addClickListener(e -> {
            ingresoManual();
        });

        buttonsLayout.addComponents(pdfFile, ingresoManualBtn);
        buttonsLayout.setComponentAlignment(pdfFile, Alignment.BOTTOM_LEFT);
        buttonsLayout.setComponentAlignment(ingresoManualBtn, Alignment.BOTTOM_LEFT);
        buttonsLayout.addComponent(singleUpload);
        buttonsLayout.setComponentAlignment(singleUpload, Alignment.BOTTOM_CENTER);

        cargaArchivoLaout.addComponent(buttonsLayout);
        cargaArchivoLaout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);

        addComponent(cargaArchivoLaout);

    }

    private void cargarArchivo(File planillaFile) {

        UUID = Utileria.getUUID();

        singleUpload.setEnabled(false);
        facturasFelContainer.removeAllItems();
        footerFacturas.getCell("numeroAutorizacion").setText("0 documentos");
        footerFacturas.getCell("tipoDte").setText("0.00");

        try {

            FileInputStream fileInputStream = new FileInputStream(planillaFile);

            workbook = new XSSFWorkbook(fileInputStream);

            sheet = workbook.getSheetAt(0);

            System.out.println("\n Total lineas en archivo=" + sheet.getLastRowNum());
            System.out.println("...INICIO...");

            Object itemId;
            int recordCount = 0;

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery1 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            for (int linea = 1; linea <= sheet.getLastRowNum(); linea++) {

                try {
                    sheet.getRow(linea).getCell(0).getRawValue();
                } catch (Exception exNull) {
                    System.out.println("OUT...");
                    break;
                }
                if (!((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyTaxId().replaceAll("-", "").equals(sheet.getRow(linea).getCell(12).getStringCellValue())) {
                    Notification.show("ESTE ARCHIVO DE FACTURAS FEL SAT NO CORRESPONDE A : " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName() + " POR FAVOR REVISE!!",
                            Notification.Type.ERROR_MESSAGE);
                    return;
                }

                if (facturaYaRegistrada(
                        sheet.getRow(linea).getCell(8).getStringCellValue(), //nitemisor
                        sheet.getRow(linea).getCell(3).getStringCellValue(), //serie
                        sheet.getRow(linea).getCell(4).getStringCellValue(), //numero
                        sheet.getRow(linea).getCell(16).getStringCellValue() //marcaAnulado (estatus)
                )) {
                    facturasYaRegistradas++;
                    continue;
                }

                String tipoDocumento ;

                switch (String.valueOf(sheet.getRow(linea).getCell(2).getStringCellValue()).toUpperCase()) {
                    case "FACT":
                    case "FCAM": // cambiaria
                        tipoDocumento = "FACTURA";
                        break;
                    case "FPEQ": // pequeño contribuyten
  //                      tipoDocumento = "FACTURA PEQUEÑO CONTRIBUYENTE";
                        tipoDocumento = "FACTURA";
                        break;
                    case "NCRE": // nota de credito
                        tipoDocumento = "NOTA DE CREDITO COMPRA";
                        break;
                    case "FCAP": // cambiaria pequeño contribuyente
                        tipoDocumento = "FACTURA";
                        break;
                    case "RECI": // recibo
                    case "RDON": // recibo por donacion
                        tipoDocumento = "RECIBO CONTABLE";
                        break;
                    default:
                        tipoDocumento = "NO CONOCIDO";
                }

                String moneda;
                switch (String.valueOf(sheet.getRow(linea).getCell(17).getStringCellValue()).toUpperCase()) {
                    case "GTQ":
                        moneda = "QUETZALES";
                        break;
                    case "USD":
                        moneda = "DOLARES";
                        break;
                    default:
                        moneda = "NO CONOCIDO";
                }
/**
 IdEmpresa,FechaEmision, NumeroAutorizacion, TipoDTE, Serie, Numero, IdProveedor, NitProveedor, NombreProveedor,
 CodigoEstablecimiento, NombreEstablecimiento,IdReceptor, NombreReceptor, NitCertificador, NombreCertificador,
 Moneda, Monto, Estatus, IVA,IDP, TurismoHospedaje,  TurismoPasajes, TimbrePrensa, Bomberos, BebidasAlcoholicas,
 Tabaco, Cemento, BebidasNoAlcoholicas, TarifaPortuaria, FechaCertificacion, Costo
 **/

                double costo = sheet.getRow(linea).getCell(18).getNumericCellValue(); //gran total
                costo -= sheet.getRow(linea).getCell(19).getNumericCellValue();//iva
                costo -= sheet.getRow(linea).getCell(22).getNumericCellValue();//idp
                costo -= sheet.getRow(linea).getCell(23).getNumericCellValue();//turismohospedaje
                costo -= sheet.getRow(linea).getCell(24).getNumericCellValue();//turismopasajes
                costo -= sheet.getRow(linea).getCell(25).getNumericCellValue();//timbreprensa
                costo -= sheet.getRow(linea).getCell(26).getNumericCellValue();//bomberos
                costo -= sheet.getRow(linea).getCell(27).getNumericCellValue();//tasamunicipal
                costo -= sheet.getRow(linea).getCell(28).getNumericCellValue();//bebidasalcoholicas
                costo -= sheet.getRow(linea).getCell(29).getNumericCellValue();//tabaco
                costo -= sheet.getRow(linea).getCell(30).getNumericCellValue();//cemento
                costo -= sheet.getRow(linea).getCell(31).getNumericCellValue();//bebidasnoalcoholicas
                costo -= sheet.getRow(linea).getCell(32).getNumericCellValue(); //tarifaportuaria
//System.out.println("costo=" + costo);

                itemId = facturasFelContainer.addItem();

                facturasFelContainer.getContainerProperty(itemId, "id").setValue(itemId);
                facturasFelContainer.getContainerProperty(itemId, "empresa").setValue(empresaId);
                facturasFelContainer.getContainerProperty(itemId, "fechaEmision").setValue(sheet.getRow(linea).getCell(0).getStringCellValue().substring(0, 10));
                facturasFelContainer.getContainerProperty(itemId, "numeroAutorizacion").setValue(sheet.getRow(linea).getCell(1).getStringCellValue());
                facturasFelContainer.getContainerProperty(itemId, "tipoDte").setValue(tipoDocumento);
                facturasFelContainer.getContainerProperty(itemId, "serie").setValue(sheet.getRow(linea).getCell(3).getStringCellValue());
                facturasFelContainer.getContainerProperty(itemId, "numero").setValue(sheet.getRow(linea).getCell(4).getStringCellValue());
                facturasFelContainer.getContainerProperty(itemId, "idProveedor").setValue(getIdProveedorPorNit(sheet.getRow(linea).getCell(8).getStringCellValue()));
                facturasFelContainer.getContainerProperty(itemId, "nitProveedor").setValue(sheet.getRow(linea).getCell(8).getStringCellValue());
                facturasFelContainer.getContainerProperty(itemId, "proveedor").setValue(sheet.getRow(linea).getCell(9).getStringCellValue());
                facturasFelContainer.getContainerProperty(itemId, "codigoEstablecimiento").setValue(sheet.getRow(linea).getCell(10).getStringCellValue());
                facturasFelContainer.getContainerProperty(itemId, "establecimiento").setValue(sheet.getRow(linea).getCell(11).getStringCellValue());
                facturasFelContainer.getContainerProperty(itemId, "idReceptor").setValue(sheet.getRow(linea).getCell(12).getStringCellValue());
                facturasFelContainer.getContainerProperty(itemId, "receptor").setValue(sheet.getRow(linea).getCell(13).getStringCellValue());
                facturasFelContainer.getContainerProperty(itemId, "nitCertificador").setValue(sheet.getRow(linea).getCell(14).getStringCellValue());
                facturasFelContainer.getContainerProperty(itemId, "certificador").setValue(sheet.getRow(linea).getCell(15).getStringCellValue());
                facturasFelContainer.getContainerProperty(itemId, "moneda").setValue(moneda);
                facturasFelContainer.getContainerProperty(itemId, "monto").setValue(numberFormat.format(sheet.getRow(linea).getCell(18).getNumericCellValue()));
                facturasFelContainer.getContainerProperty(itemId, "estatus").setValue(sheet.getRow(linea).getCell(16).getStringCellValue());
                facturasFelContainer.getContainerProperty(itemId, "iva").setValue(numberFormat.format(sheet.getRow(linea).getCell(19).getNumericCellValue()));
                facturasFelContainer.getContainerProperty(itemId, "idp").setValue(numberFormat.format(sheet.getRow(linea).getCell(22).getNumericCellValue()));// 20
                facturasFelContainer.getContainerProperty(itemId, "turismoHospedaje").setValue(numberFormat.format(sheet.getRow(linea).getCell(23).getNumericCellValue()));
                facturasFelContainer.getContainerProperty(itemId, "turismoPasaje").setValue(numberFormat.format(sheet.getRow(linea).getCell(24).getNumericCellValue()));
                facturasFelContainer.getContainerProperty(itemId, "timbrePrensa").setValue(numberFormat.format(sheet.getRow(linea).getCell(25).getNumericCellValue())); //timbreprensa
                facturasFelContainer.getContainerProperty(itemId, "bomberos").setValue(numberFormat.format(sheet.getRow(linea).getCell(26).getNumericCellValue())); //bomberos
                facturasFelContainer.getContainerProperty(itemId, "tasaMunicipal").setValue(numberFormat.format(sheet.getRow(linea).getCell(27).getNumericCellValue())); //tasamunicipal 25
                facturasFelContainer.getContainerProperty(itemId, "bebidasAlcoholicas").setValue(numberFormat.format(sheet.getRow(linea).getCell(28).getNumericCellValue())); //bebidasalcoholicas 25
                facturasFelContainer.getContainerProperty(itemId, "tabaco").setValue(numberFormat.format(sheet.getRow(linea).getCell(29).getNumericCellValue())); //tabaco
                facturasFelContainer.getContainerProperty(itemId, "cemento").setValue(numberFormat.format(sheet.getRow(linea).getCell(30).getNumericCellValue())); //cemento
                facturasFelContainer.getContainerProperty(itemId, "bebidasNoAlcoholicas").setValue(numberFormat.format(sheet.getRow(linea).getCell(31).getNumericCellValue()));//bebidasnoalcoholicas
                facturasFelContainer.getContainerProperty(itemId, "tarifaPortuaria").setValue(numberFormat.format(sheet.getRow(linea).getCell(32).getNumericCellValue()));//tarifaportuaria 30
                facturasFelContainer.getContainerProperty(itemId, "fechaCertificacion").setValue(sheet.getRow(linea).getCell(0).getStringCellValue());//fechacertificacion
                facturasFelContainer.getContainerProperty(itemId, "costo").setValue(numberFormat.format(costo)); //costo
//                facturasFelContainer.getContainerProperty(itemId,"pdf").setValue("PDF"); //pdf

//                totalDebe = totalDebe.add(new BigDecimal(sheet.getRow(linea).getCell(7).getRawValue())).setScale(2, BigDecimal.ROUND_HALF_UP);
//                totalHaber = totalHaber.add(new BigDecimal(sheet.getRow(linea).getCell(8).getRawValue())).setScale(2, BigDecimal.ROUND_HALF_UP);

//System.out.println("numero="+sheet.getRow(linea).getCell(4).getStringCellValue());

                queryString = " Insert Into documentos_fel_sat (";
                queryString += "IdEmpresa,FechaEmision, NumeroAutorizacion, TipoDTE, Serie, Numero, IdProveedor, NitProveedor, NombreProveedor," +
                        " CodigoEstablecimiento, NombreEstablecimiento,IdReceptor, NombreReceptor, NitCertificador, NombreCertificador," +
                        " Moneda, Monto, Estatus, IVA,IDP, TurismoHospedaje, TurismoPasajes, TimbrePrensa, Bomberos, TasaMunicipal, BebidasAlcoholicas," +
                        " Tabaco, Cemento, BebidasNoAlcoholicas, TarifaPortuaria, FechaCertificacion, Costo, CreadoUsuario, CreadoFechaYHora , IdCarga" +
                        ")";
                queryString += " Values ";
                queryString += " (";
                queryString += empresaId;
                queryString += ",'" + facturasFelGrid.getContainerDataSource().getContainerProperty(itemId, "fechaEmision").getValue() + "'";
                queryString += ",'" + facturasFelGrid.getContainerDataSource().getContainerProperty(itemId, "numeroAutorizacion").getValue() + "'";
                queryString += ",'" + facturasFelGrid.getContainerDataSource().getContainerProperty(itemId, "tipoDte").getValue() + "'";
                queryString += ",'" + facturasFelGrid.getContainerDataSource().getContainerProperty(itemId, "serie").getValue() + "'";
                queryString += ",'" + facturasFelGrid.getContainerDataSource().getContainerProperty(itemId, "numero").getValue() + "'";
                queryString += "," + facturasFelGrid.getContainerDataSource().getContainerProperty(itemId, "idProveedor").getValue();
                queryString += ",'" + facturasFelGrid.getContainerDataSource().getContainerProperty(itemId, "nitProveedor").getValue() + "'";
                queryString += ",'" + String.valueOf(facturasFelGrid.getContainerDataSource().getContainerProperty(itemId, "proveedor").getValue()).replaceAll("'", "") + "'";
                queryString += ",'" + String.valueOf(facturasFelGrid.getContainerDataSource().getContainerProperty(itemId, "codigoEstablecimiento").getValue()).replaceAll("'", "") + "'";
                queryString += ",'" + String.valueOf(facturasFelGrid.getContainerDataSource().getContainerProperty(itemId, "establecimiento").getValue()).replaceAll("'", "") + "'";
                queryString += ",'" + facturasFelGrid.getContainerDataSource().getContainerProperty(itemId, "idReceptor").getValue() + "'";
                queryString += ",'" + String.valueOf(facturasFelGrid.getContainerDataSource().getContainerProperty(itemId, "receptor").getValue()).replaceAll("'", "") + "'";
                queryString += ",'" + facturasFelGrid.getContainerDataSource().getContainerProperty(itemId, "nitCertificador").getValue() + "'";
                queryString += ",'" + String.valueOf(facturasFelGrid.getContainerDataSource().getContainerProperty(itemId, "certificador").getValue()).replaceAll("'", "") + "'";
                queryString += ",'" + facturasFelGrid.getContainerDataSource().getContainerProperty(itemId, "moneda").getValue() + "'";
                queryString += "," + String.valueOf(facturasFelGrid.getContainerDataSource().getContainerProperty(itemId, "monto").getValue()).replaceAll(",", "");
                queryString += "," + (String.valueOf(facturasFelGrid.getContainerDataSource().getContainerProperty(itemId, "estatus").getValue()).equals("Vigente") ? "'ACTIVA'" : "'ANULADA'");
                queryString += "," + String.valueOf(facturasFelGrid.getContainerDataSource().getContainerProperty(itemId, "iva").getValue()).replaceAll(",", "");
                queryString += "," + String.valueOf(facturasFelGrid.getContainerDataSource().getContainerProperty(itemId, "idp").getValue()).replaceAll(",", "");
                queryString += "," + String.valueOf(facturasFelGrid.getContainerDataSource().getContainerProperty(itemId, "turismoHospedaje").getValue()).replaceAll(",", "");
                queryString += "," + String.valueOf(facturasFelGrid.getContainerDataSource().getContainerProperty(itemId, "turismoPasaje").getValue()).replaceAll(",", "");
                queryString += "," + String.valueOf(facturasFelGrid.getContainerDataSource().getContainerProperty(itemId, "timbrePrensa").getValue()).replaceAll(",", "");
                queryString += "," + String.valueOf(facturasFelGrid.getContainerDataSource().getContainerProperty(itemId, "bomberos").getValue()).replaceAll(",", "");
                queryString += "," + String.valueOf(facturasFelGrid.getContainerDataSource().getContainerProperty(itemId, "tasaMunicipal").getValue()).replaceAll(",", "");
                queryString += "," + String.valueOf(facturasFelGrid.getContainerDataSource().getContainerProperty(itemId, "bebidasAlcoholicas").getValue()).replaceAll(",", "");
                queryString += "," + String.valueOf(facturasFelGrid.getContainerDataSource().getContainerProperty(itemId, "tabaco").getValue()).replaceAll(",", "");
                queryString += "," + String.valueOf(facturasFelGrid.getContainerDataSource().getContainerProperty(itemId, "cemento").getValue()).replaceAll(",", "");
                queryString += "," + String.valueOf(facturasFelGrid.getContainerDataSource().getContainerProperty(itemId, "bebidasNoAlcoholicas").getValue()).replaceAll(",", "");
                queryString += "," + String.valueOf(facturasFelGrid.getContainerDataSource().getContainerProperty(itemId, "tarifaPortuaria").getValue()).replaceAll(",", "");
                queryString += ",'" + facturasFelGrid.getContainerDataSource().getContainerProperty(itemId, "fechaCertificacion").getValue() + "'";
                queryString += "," + String.valueOf(facturasFelGrid.getContainerDataSource().getContainerProperty(itemId, "costo").getValue()).replaceAll(",", "");
                queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                queryString += ",current_timestamp";
                queryString += ",'" + UUID + "'";
                queryString += ")";

//System.out.println(queryString);

                stPreparedQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().prepareStatement(queryString, Statement.RETURN_GENERATED_KEYS);
                stPreparedQuery.executeUpdate();
                rsRecords = stPreparedQuery.getGeneratedKeys();
                rsRecords.next();

                contabilizar(itemId, rsRecords.getInt(1), "");

                recordCount++;

            } //endfor

            System.out.println("...FIN...");

            if(recordCount > 0) {
                Notification.show("Operación exitosa! documentos cargados=[" + recordCount + "]  documentos contabilizados=[" + facturasContabilizadas + "]  documentos anulados=[" + facturasAnuladas + "].", Notification.Type.HUMANIZED_MESSAGE);

                ImportarFelSatPDF importarFelSatPDF = new ImportarFelSatPDF(
                        empresaId,
                        empresaNombre,
                        ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyTaxId(),
                        UUID
                );
                UI.getCurrent().addWindow(importarFelSatPDF);
                importarFelSatPDF.center();
            }
            else {
                Notification.show("documentos cargados=[0] documentos contabilizados=[0] documentos anulados=[0]  ", Notification.Type.ERROR_MESSAGE);
            }

            documentosCargados();

        } catch (Exception ex1) {
            new Notification("Error al intentar cargar las Facturas FEL sat del archivo EXCEL. Por favor REVISE dicho archivo Excel, compruebe que el archivo tenga las columnas correctas y vuelva a intentarlo.",
                    ex1.getMessage(),
                    Notification.Type.ERROR_MESSAGE)
                    .show(Page.getCurrent());
            ex1.printStackTrace();
        }
        singleUpload.setEnabled(true);
    }

    private String getIdProveedorPorNit(String nit) {
        String idProveedor = "00000";

        queryString = " SELECT IdProveedor, Nombre FROM proveedor_empresa ";
        queryString += " WHERE REPLACE(REPLACE(Nit, '-', ''), '/', '') = '" + nit + "'";
        queryString += " AND EsProveedor = 1";
        queryString += " AND Inhabilitado = 0";
        queryString += " AND IdEmpresa = " + empresaId;

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                idProveedor = rsRecords.getString("IdProveedor");
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar Proveedores " + ex1.getMessage());
            ex1.printStackTrace();
        }

        return idProveedor;
    }

    private boolean facturaYaRegistrada(String nit, String serie, String numero, String marcaAnulado) {
        queryString = " SELECT Id, Estatus ";
        queryString += " FROM documentos_fel_sat ";
        queryString += " WHERE REPLACE(REPLACE(NitProveedor, '-', ''), '/', '') = '" + nit + "'";
        queryString += " AND Serie = '" + serie + "'";
        queryString += " AND Numero = '" + numero + "'";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                if (marcaAnulado.toUpperCase().equals("ANULADO")) { //anularon el documento
                    if (rsRecords.getString("Estatus").toUpperCase().equals("ACTIVA")) {
                        stQuery.executeUpdate("UPDATE documentos_fel_sat SET Estatus = 'ANULADA' WHERE Id =" + rsRecords.getString("Id"));
//                //ANULAR DOCUMENTO EN CONTABILIDAD SI ES QUE EXIST
                        queryString = "UPDATE contabilidad_partida SET ESTATUS = 'ANULADO'"
                        + "WHERE IdEmpresa = " + empresaId
                        + " AND SerieDocumento = '" + serie + "'"
                        + " AND NumeroDocumento = '" + numero + "'"
                        + " AND NitProveedor = '" + nit + "'"
                        + " AND Estatus IN ('INGRESADO', 'REVISADO') ";

                        stQuery.executeUpdate(queryString);
                        facturasAnuladas++;
                    }
                }

                return true;
            }
        } catch (Exception ex1) {
            Logger.getLogger(this.getClass()).log(Level.SEVERE, "Error al buscar facturas previamente registradas.", ex1);
            ex1.printStackTrace();
        }

        return false;
    }

    private void documentosCargados() {

        facturasFelContainer.removeAllItems();
        footerFacturas.getCell("numeroAutorizacion").setText("0 documentos");
        footerFacturas.getCell("tipoDte").setText("0.00");

        queryString = "UPDATE documentos_fel_sat dfs, proveedor_empresa prv ";
        queryString += "SET dfs.IdProveedor = prv.IDProveedor ";
        queryString += "WHERE dfs.IdProveedor  = 0 ";
        queryString += "AND prv.EsProveedor = 1 ";
        queryString += "AND dfs.NitProveedor = prv.NIT ";
        queryString += "AND prv.IdEmpresa = " + empresaId;

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            queryString = " SELECT *, Emp.Empresa ";
            queryString += " FROM documentos_fel_sat FE ";
            queryString += " INNER JOIN contabilidad_empresa Emp On Emp.IdEmpresa = FE.IdEmpresa";
            queryString += " WHERE FE.Estatus IN ('ACTIVA', 'PORANULAR') ";
            queryString += " AND FE.IdEmpresa = " + empresaId;
//            queryString += " AND CONCAT(FE.SERIE, FE.NUMERO, FE.NITProveedor) NOT IN (SELECT CONCAT(CP.SerieDocumento, CP.NumeroDocumento, CP.NitProveedor) FROM contabilidad_partida CP WHERE CP.IdEmpresa = FE.IdEmpresa) ";
            queryString += " AND FE.Accion IS NULL";

            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                double granTotal = 0.00;
                Object itemId;
/**
 IdEmpresa,FechaEmision, NumeroAutorizacion, TipoDTE, Serie, Numero, IdProveedor, NitProveedor, NombreProveedor,
 CodigoEstablecimiento, NombreEstablecimiento,IdReceptor, NombreReceptor, NitCertificador, NombreCertificador,
 Moneda, Monto, Estatus, IVA,IDP, TurismoHospedaje,  TurismoPasajes, TimbrePrensa, Bomberos, BebidasAlcoholicas,
 Tabaco, Cemento, BebidasNoAlcoholicas, TarifaPortuaria, FechaCertificacion, Costo
 **/
                do {

                    queryString = " SELECT IdPartida ";
                    queryString += " FROM contabilidad_partida ";
                    queryString += " WHERE IdEmpresa = " + empresaId;
                    queryString += " AND SerieDocumento  = '" + rsRecords.getString("Serie") + "'";
                    queryString += " AND NumeroDocumento = '" + rsRecords.getString("Numero") + "'";
                    queryString += " AND NitProveedor    = '" + rsRecords.getString("NitProveedor") + "'";
                    queryString += " AND (Estatus <> 'ANULADO' OR Estatus <> 'ANULADA')";

                    rsRecords1 = stQuery1.executeQuery(queryString);

                    if (!rsRecords1.next()) {
                        itemId = facturasFelContainer.addItem();

                        facturasFelContainer.getContainerProperty(itemId, "id").setValue(rsRecords.getString("Id"));
                        facturasFelContainer.getContainerProperty(itemId, "empresa").setValue(empresaId);
                        facturasFelContainer.getContainerProperty(itemId, "fechaEmision").setValue(rsRecords.getString("FechaEmision"));
                        facturasFelContainer.getContainerProperty(itemId, "numeroAutorizacion").setValue(rsRecords.getString("NumeroAutorizacion"));
                        facturasFelContainer.getContainerProperty(itemId, "tipoDte").setValue(rsRecords.getString("TipoDte"));
                        facturasFelContainer.getContainerProperty(itemId, "serie").setValue(rsRecords.getString("Serie"));
                        facturasFelContainer.getContainerProperty(itemId, "numero").setValue(rsRecords.getString("Numero"));
                        facturasFelContainer.getContainerProperty(itemId, "idProveedor").setValue(rsRecords.getString("IdProveedor"));
                        facturasFelContainer.getContainerProperty(itemId, "nitProveedor").setValue(rsRecords.getString("NitProveedor"));
                        facturasFelContainer.getContainerProperty(itemId, "proveedor").setValue(rsRecords.getString("NombreProveedor"));
                        facturasFelContainer.getContainerProperty(itemId, "codigoEstablecimiento").setValue(rsRecords.getString("CodigoEstablecimiento"));
                        facturasFelContainer.getContainerProperty(itemId, "establecimiento").setValue(rsRecords.getString("NombreEstablecimiento"));
                        facturasFelContainer.getContainerProperty(itemId, "idReceptor").setValue(rsRecords.getString("IdReceptor"));
                        facturasFelContainer.getContainerProperty(itemId, "receptor").setValue(rsRecords.getString("NombreReceptor"));
                        facturasFelContainer.getContainerProperty(itemId, "nitCertificador").setValue(rsRecords.getString("NitCertificador"));
                        facturasFelContainer.getContainerProperty(itemId, "certificador").setValue(rsRecords.getString("NombreCertificador"));
                        facturasFelContainer.getContainerProperty(itemId, "moneda").setValue(rsRecords.getString("Moneda"));
                        facturasFelContainer.getContainerProperty(itemId, "monto").setValue(numberFormat.format(rsRecords.getDouble("Monto")));
                        facturasFelContainer.getContainerProperty(itemId, "estatus").setValue(rsRecords.getString("Estatus"));
                        facturasFelContainer.getContainerProperty(itemId, "iva").setValue(numberFormat.format(rsRecords.getDouble("Iva")));
                        facturasFelContainer.getContainerProperty(itemId, "idp").setValue(numberFormat.format(rsRecords.getDouble("Idp")));// 20
                        facturasFelContainer.getContainerProperty(itemId, "turismoHospedaje").setValue(numberFormat.format(rsRecords.getDouble("TurismoHospedaje")));
                        facturasFelContainer.getContainerProperty(itemId, "turismoPasaje").setValue(numberFormat.format(rsRecords.getDouble("TurismoPasajes")));
                        facturasFelContainer.getContainerProperty(itemId, "timbrePrensa").setValue(numberFormat.format(rsRecords.getDouble("TimbrePrensa"))); //timbreprensa
                        facturasFelContainer.getContainerProperty(itemId, "bomberos").setValue(numberFormat.format(rsRecords.getDouble("Bomberos"))); //bomberos
                        facturasFelContainer.getContainerProperty(itemId, "tasaMunicipal").setValue(numberFormat.format(rsRecords.getDouble("TasaMunicipal"))); //tasamunicipal 25
                        facturasFelContainer.getContainerProperty(itemId, "bebidasAlcoholicas").setValue(numberFormat.format(rsRecords.getDouble("BebidasAlcoholicas"))); //bebidasalcoholicas 25
                        facturasFelContainer.getContainerProperty(itemId, "tabaco").setValue(numberFormat.format(rsRecords.getDouble("Tabaco"))); //tabaco
                        facturasFelContainer.getContainerProperty(itemId, "cemento").setValue(numberFormat.format(rsRecords.getDouble("Cemento"))); //cemento
                        facturasFelContainer.getContainerProperty(itemId, "bebidasNoAlcoholicas").setValue(numberFormat.format(rsRecords.getDouble("BebidasNoAlcoholicas")));//bebidasnoalcoholicas
                        facturasFelContainer.getContainerProperty(itemId, "tarifaPortuaria").setValue(numberFormat.format(rsRecords.getDouble("TarifaPortuaria")));//tarifaportuaria 30
                        facturasFelContainer.getContainerProperty(itemId, "fechaCertificacion").setValue(rsRecords.getString("FechaCertificacion"));//fechacertificacion
                        facturasFelContainer.getContainerProperty(itemId, "costo").setValue(numberFormat.format(rsRecords.getDouble("Costo"))); //costo

                        granTotal += rsRecords.getDouble("Monto");
                    }

                } while (rsRecords.next());

                footerFacturas.getCell("numeroAutorizacion").setText(facturasFelContainer.size() + " documentos");
                footerFacturas.getCell("tipoDte").setText(numberFormat.format(granTotal));

            }
        } catch (Exception ex1) {
            new Notification("Error al intentar leer registros de tabla documentos_fel_sat.",
                    ex1.getMessage(),
                    Notification.Type.ERROR_MESSAGE)
                    .show(Page.getCurrent());
            ex1.printStackTrace();
        }
    }

    private void crearLayouForm() {

        HorizontalLayout ordenCompraYFormularioLayout = new HorizontalLayout();
        ordenCompraYFormularioLayout.setSpacing(true);
        ordenCompraYFormularioLayout.setSizeFull();
        ordenCompraYFormularioLayout.setMargin(false);

        ordenCompraContainer.addContainerProperty(ID_PROPERTY, String.class, null);
        ordenCompraContainer.addContainerProperty(NOC_PROPERTY, String.class, null);
        ordenCompraContainer.addContainerProperty(TIPO_PROPERTY, String.class, null);
        ordenCompraContainer.addContainerProperty(FECHA_PROPERTY, String.class, null);
        ordenCompraContainer.addContainerProperty(MONEDA_PROPERTY, String.class, null);
        ordenCompraContainer.addContainerProperty(TOTAL_PROPERTY, String.class, null);

        ordenCompraGrid = new Grid("Listado de ordenes de compra del proveedor", ordenCompraContainer);
        ordenCompraGrid.setWidth("100%");
        ordenCompraGrid.setImmediate(true);
        ordenCompraGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        ordenCompraGrid.setDescription("Seleccione un registro.");
        ordenCompraGrid.setHeightMode(HeightMode.ROW);
        ordenCompraGrid.setHeightByRows(5);
        ordenCompraGrid.setResponsive(true);
        ordenCompraGrid.setEditorBuffered(false);
        ordenCompraGrid.getColumn(ID_PROPERTY).setHidable(true).setHidden(true);
        ordenCompraGrid.addSelectionListener((SelectionEvent.SelectionListener) event -> {
            if (facturasFelGrid.getSelectedRow() != null && ordenCompraGrid.getSelectedRow() != null) {
                if (String.valueOf(facturasFelGrid.getContainerDataSource()
                        .getItem(facturasFelGrid.getSelectedRow()).getItemProperty("tipoDte")
                        .getValue()).equals("NOTA DE CREDITO COMPRA")) {
                    Notification.show("SOLO FACTURAS O RECIBOS.No se puede contabilizar una nota de credito de compra con una orden de compra.",
                            Notification.Type.WARNING_MESSAGE);
                }
                if (!String.valueOf(facturasFelGrid.getContainerDataSource()
                        .getItem(facturasFelGrid.getSelectedRow()).getItemProperty("estatus")
                        .getValue()).equals("ACTIVA")) {
                    Notification.show("SOLO FACTURAS O RECIBOS ACTIVOS. No se puede contabilizar un documento anulado con una orden de compra.",
                            Notification.Type.WARNING_MESSAGE);
                }
                contabilizar(
                        facturasFelGrid.getSelectedRow(),
                        Integer.parseInt(facturasFelContainer.getContainerProperty(facturasFelGrid.getSelectedRow(), "id").getValue().toString()),
                        String.valueOf(ordenCompraContainer.getContainerProperty(ordenCompraGrid.getSelectedRow(), ID_PROPERTY).getValue())
                );
                llenarGridOrdenCompra();
            }
        });

        ordenCompraYFormularioLayout.addComponent(ordenCompraGrid);

        VerticalLayout rithtBottonLayout = new VerticalLayout();
        rithtBottonLayout.setSizeUndefined();
        rithtBottonLayout.addStyleName("rcorners4");

        ordenCompraYFormularioLayout.addComponent(rithtBottonLayout);

        addComponent(ordenCompraYFormularioLayout);
        setComponentAlignment(ordenCompraYFormularioLayout, Alignment.BOTTOM_CENTER);

        formLayout = new FormLayout();
        formLayout.setSizeUndefined();
        formLayout.setCaption("Datos para contabilizar documento");
        formLayout.setSpacing(false);

        centroCostoCbx.setWidth("25em");
        centroCostoCbx.setTextInputAllowed(false);
        centroCostoCbx.setInvalidAllowed(false);
        centroCostoCbx.setNewItemsAllowed(false);
        centroCostoCbx.setNullSelectionAllowed(true);
        llenarComboCentroCosto();

        proveedorAbastoCbx.setWidth("25em");
        proveedorAbastoCbx.setTextInputAllowed(false);
        proveedorAbastoCbx.setInvalidAllowed(false);
        proveedorAbastoCbx.setNewItemsAllowed(false);
        proveedorAbastoCbx.addItem("Proveedor");
        proveedorAbastoCbx.addItem("Abastos");

        doctoAfectaCbx.setWidth("25em");
        doctoAfectaCbx.setTextInputAllowed(false);
        doctoAfectaCbx.setInvalidAllowed(false);
        doctoAfectaCbx.setNewItemsAllowed(false);

        doctoAfectaCbx.setEnabled(false);

        Button saveBtn = new Button("Guardar");
        saveBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        saveBtn.setIcon(FontAwesome.SAVE);
        saveBtn.addClickListener(e -> {
            if (facturasFelGrid.getSelectedRow() == null) {
                Notification.show("POR FAVOR ELIJA UN DOCUMENTO DEL LISTADO.", Notification.Type.WARNING_MESSAGE);
                return;
            }
            if(proveedorAbastoCbx.getValue() == null) {
                Notification.show("Debe seleccionar si es proveedor o abasto.", Notification.Type.WARNING_MESSAGE);
                proveedorAbastoCbx.focus();
                return;
            }

            //Notification.show("ACCION SUSPENDIDA TEMPORALMENTE. POR FAVOR CONTACTE AL ADMINISTRADOR DEL SISTEMA.", Notification.Type.WARNING_MESSAGE);
            contabilizarForzada(
                    facturasFelGrid.getSelectedRow(),
                    Integer.parseInt(facturasFelContainer.getContainerProperty(facturasFelGrid.getSelectedRow(), "id").getValue().toString())
            );
        });

        Button noProcedeBtn = new Button("No procede");
        noProcedeBtn.addStyleName(ValoTheme.BUTTON_DANGER);
        noProcedeBtn.setIcon(FontAwesome.STOP);
        noProcedeBtn.addClickListener(e -> {
            //TODO : marcar como no procede
            queryString = "UPDATE documentos_fel_sat SET ";
            queryString += " Accion = 'NO PROCEDE'";
            queryString += " WHERE Id = " + facturasFelGrid.getContainerDataSource().getItem(facturasFelGrid.getSelectedRow()).getItemProperty("id").getValue();

            ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de registrar esta acción ?",
                    "SI", "NO", new ConfirmDialog.Listener() {

                        public void onClose(ConfirmDialog dialog) {
                            if (dialog.isConfirmed()) {
                                try {
                                    stQuery.executeUpdate(queryString);

                                    facturasFelContainer.removeItem(facturasFelGrid.getSelectedRow());

                                    Notification.show("Registro modificado (NO PROCEDE) exitosamente.");
                                } catch (Exception anyEx) {
                                    Notification.show("Error al modificar registro anular documento : " + anyEx.getMessage(), Notification.Type.ERROR_MESSAGE);
                                    Logger.getLogger(this.getClass()).log(Level.INFO, "ERROR AL MODIFICAR REGISTRO ANULAR DOCUMENTO", anyEx);
                                }
                            }
                        }
                    }
            );

        });

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setWidth(100, Unit.PERCENTAGE);
        buttonsLayout.addComponents(saveBtn, noProcedeBtn);

        formLayout.addComponents(centroCostoCbx, proveedorAbastoCbx, doctoAfectaCbx, buttonsLayout);
        formLayout.setComponentAlignment(centroCostoCbx, Alignment.MIDDLE_CENTER);
        formLayout.setComponentAlignment(proveedorAbastoCbx, Alignment.MIDDLE_CENTER);
        formLayout.setComponentAlignment(doctoAfectaCbx, Alignment.MIDDLE_CENTER);
        formLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);

        rithtBottonLayout.addComponent(formLayout);
        rithtBottonLayout.setComponentAlignment(formLayout, Alignment.MIDDLE_CENTER);
    }

    public void llenarComboCentroCosto() {

        centroCostoCbx.removeAllItems();

        queryString = " SELECT * FROM centro_costo";
        queryString += " WHERE IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
        queryString += " AND IdProyecto = " + ((SopdiUI) mainUI).sessionInformation.getStrProjectId();
        queryString += " AND Inhabilitado = 0";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                do {
                    centroCostoCbx.addItem(rsRecords.getString("IdCentroCosto"));
                    //centroCostoCbx.setItemCaption(rsRecords.getString("IdCentroCosto"), rsRecords.getString("CodigoCentroCosto") + " " + rsRecords.getString("Grupo"));
                    centroCostoCbx.setItemCaption(rsRecords.getString("IdCentroCosto"), rsRecords.getString("CodigoCentroCosto"));
                } while (rsRecords.next());
            }

        } catch (Exception ex1) {
            System.out.println("Error al llenar combo centro costo: " + ex1.getMessage());
            ex1.printStackTrace();
            Notification.show("Error al llenar combo centro costo.", Notification.Type.ERROR_MESSAGE);
        }
    }

    public void llenarComboDoctoAfecta() {

        doctoAfectaCbx.removeAllItems();

        if (facturasFelGrid.getSelectedRow() == null) {
            return;
        }

        queryString = " SELECT SerieDocumento, NumeroDocumento, CodigoCC, IdNomenclatura ";
        queryString += " FROM contabilidad_partida";
        queryString += " WHERE IdEmpresa = " + empresaId;
        queryString += " AND IdProveedor = " + facturasFelGrid.getContainerDataSource().getItem(facturasFelGrid.getSelectedRow()).getItemProperty("idProveedor").getValue();
        queryString += " AND Moneda = '" + facturasFelGrid.getContainerDataSource().getItem(facturasFelGrid.getSelectedRow()).getItemProperty("moneda").getValue() + "'";
        queryString += " AND Estatus <> 'ANULADO'";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                do {
                    queryString = " SELECT ";
                    queryString += " SUM(HABER - DEBE) TOTALSALDO, SUM(HaberQuetzales - DebeQuetzales) TOTALSALDOQ ";
                    queryString += " FROM contabilidad_partida";
                    queryString += " WHERE IdEmpresa = " + empresaId;
                    queryString += " AND CodigoCC = '" + rsRecords.getString("CodigoCC") + "'";
                    queryString += " AND contabilidad_partida.IdNomenclatura = " + rsRecords.getString("IdNomenclatura");
                    queryString += " AND contabilidad_partida.Estatus <> 'ANULADO'";

//                    java.util.logging.Logger.getLogger(this.getClass().getName()).log(Level.INFO, queryString);

                    rsRecords1 = stQuery1.executeQuery(queryString);

                    if (rsRecords1.next()) {
                        if (rsRecords1.getDouble("TOTALSALDO") > 0.00) {
                            centroCostoCbx.addItem(rsRecords.getString("SerieDocumento") + " " + rsRecords.getString("NumeroDocumento"));
                        }
                    }
                } while (rsRecords.next());
            }
        } catch (Exception ex1) {
            Notification.show("Error al leer documentos afectados : " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void llenarGridOrdenCompra() {

        ordenCompraContainer.removeAllItems();

        if (  ( String.valueOf(facturasFelGrid.getContainerDataSource()
                .getItem(facturasFelGrid.getSelectedRow()).getItemProperty("tipoDte")
                .getValue()).equals("FACTURA")
            || String.valueOf(facturasFelGrid.getContainerDataSource()
                .getItem(facturasFelGrid.getSelectedRow()).getItemProperty("tipoDte")
                .getValue()).contains("RECIBO")) ) {

            queryString = " SELECT *, tipo_orden_compra.Descripcion As TipoOrdenCompra ";
            queryString += " FROM orden_compra";
            queryString += " INNER JOIN tipo_orden_compra ON orden_compra.IdTipoOrdenCompra = tipo_orden_compra.Id";
            queryString += " WHERE orden_compra.IdProveedor = " + facturasFelGrid.getContainerDataSource().getItem(facturasFelGrid.getSelectedRow()).getItemProperty("idProveedor").getValue();
            queryString += " AND orden_compra.IdEmpresa =" + empresaId;
            queryString += " AND ((orden_compra.CodigoCCDocumento = '') OR (orden_compra.CodigoCCDocumento IS NULL) OR (orden_compra.CodigoCCDocumento = '0') )";
            queryString += " AND orden_compra.Moneda = '" + facturasFelGrid.getContainerDataSource().getItem(facturasFelGrid.getSelectedRow()).getItemProperty("moneda").getValue() + "'";

            System.out.println("llenarOrdenCompra=" + queryString);

            try {
                stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                rsRecords = stQuery.executeQuery(queryString);

                if (rsRecords.next()) { //  encontrado
                    do {
                        Object itemId = ordenCompraContainer.addItem();
                        ordenCompraContainer.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords.getString("Id"));
                        ordenCompraContainer.getContainerProperty(itemId, NOC_PROPERTY).setValue(rsRecords.getString("NOC"));
                        ordenCompraContainer.getContainerProperty(itemId, TIPO_PROPERTY).setValue(rsRecords.getString("TipoOrdenCompra"));
                        ordenCompraContainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha")));
                        ordenCompraContainer.getContainerProperty(itemId, MONEDA_PROPERTY).setValue(rsRecords.getString("Moneda"));
                        ordenCompraContainer.getContainerProperty(itemId, TOTAL_PROPERTY).setValue(numberFormat.format(rsRecords.getDouble("Total")));

                    } while (rsRecords.next());

                    //                ordenCompraGrid.select(ordenCompraContainer.firstItemId());
                }
            } catch (Exception ex) {
                System.out.println("Error al listar tabla oden de compra " + ex);
                ex.printStackTrace();
                Notification.show("Error al listar tabla orden de compra.", Notification.Type.ERROR_MESSAGE);
            }
        }//endif
    }

    /**
     * Si la factura coincide con la orden de compra activa, se procede a crear la partida contable respectiva.
     * Tomar en cuenta que si en la orden de compra hay varios centros de costo, se reparte entre lineas de la
     * partida contable, es decir linea por centro de costo. La cuenta gasto.
     */
    public void contabilizar(Object itemId, int idDocumento, String idOrdenCompra) {

        String fechaEmision = "";
        String idProveedor = "0";
        String nitProveedor = "0";
        String nombreProveedor = "";
        String numero = "";
        String serie = "";
        String monto = "0";
        String costo = "0";
        double dcosto = 0.00;
        double iva = 0.00;
        double otrosImpuestos = 0.00;
        String codigoCC = "";
        int ultimaLiquidacion = 0;
        String idLiquidador = "0";
        String nombreLiquidador = "";
        String idNomenclatura = "";
        String idCentroCosto = "";
        String codigoCentroCosto = "";
        String tipoOrdenCompra = "";
        String queryStringDOCA = "";
        String moneda = "QUETZALES";

        queryString = "SELECT *";
        queryString += " FROM documentos_fel_sat";
        queryString += " WHERE Id = " + idDocumento;
//System.out.println("documentos_fel_sat=" + queryString);

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();

            rsRecords = stQuery.executeQuery(queryString);
            rsRecords.next();

            String tipoDocumento = rsRecords.getString("TipoDTE");

            fechaEmision = rsRecords.getString("FechaEmision");
            idProveedor = rsRecords.getString("IdProveedor");
            nitProveedor = rsRecords.getString("NitProveedor");
            nombreProveedor = rsRecords.getString("NombreProveedor");
            moneda = rsRecords.getString("Moneda");
            numero = rsRecords.getString("Numero");
            serie = rsRecords.getString("Serie");
            monto = rsRecords.getString("Monto");
            iva = rsRecords.getDouble("Iva");
            costo = rsRecords.getString("Costo");
            otrosImpuestos = rsRecords.getDouble("IDP") + rsRecords.getDouble("TurismoHospedaje");
            otrosImpuestos += rsRecords.getDouble("TurismoPasajes") + rsRecords.getDouble("TimbrePrensa");
            otrosImpuestos += rsRecords.getDouble("Bomberos") + rsRecords.getDouble("TasaMunicipal");
            otrosImpuestos += rsRecords.getDouble("BebidasAlcoholicas");
            otrosImpuestos += rsRecords.getDouble("Tabaco")  + rsRecords.getDouble("Cemento");
            otrosImpuestos += rsRecords.getDouble("BebidasNoAlcoholicas")  + rsRecords.getDouble("TarifaPortuaria");

            double tasaCambio = (double)((SopdiUI) mainUI).getTasaCambioDelDia(fechaEmision);

            if(moneda.equals("DOLARES") && tasaCambio == 1.00 ) {
                Notification.show("No se ha encontrado tasa de cambio para la fecha de emisión de la factura : " + serie + " " + numero, Notification.Type.ERROR_MESSAGE);
                return;
            }
            if(moneda.equals("QUETZALES")) {
                tasaCambio = 1.00;
            }

            // para el caso de que sea una factura reportada en documento_liq_mobil
            queryString = " SELECT *, PRV.Nombre NombreLiquidador ";
            queryString += " FROM documento_liq_mobil";
            queryString += " INNER JOIN proveedor_empresa PRV ON PRV.IdProveedor = IdEmpleado";
            queryString += " WHERE documento_liq_mobil.IdProveedor = " + idProveedor;
            queryString += " AND   documento_liq_mobil.Numero = '" + numero + "'";
            queryString += " AND PRV.IdEmpresa = " + empresaId;

            rsRecords = stQuery.executeQuery(queryString);

            if(rsRecords.next()) { // es una factura liquidacion caja chica

                idLiquidador = rsRecords.getString("IdEmpleado");
                nombreLiquidador = rsRecords.getString("NombreLiquidador");
                idNomenclatura = rsRecords.getString("IdNomenclatura");
                idCentroCosto = rsRecords.getString("IdCentroCosto");
                codigoCentroCosto = rsRecords.getString("CodigoCentroCosto");

                // encontrar la ultima liquidación abierta del liquidador
                queryString = " SELECT CodigoCC, IdLiquidacion ";
                queryString += " FROM contabilidad_partida";
                queryString += " WHERE IdEmpresa = " + empresaId;
                queryString += " AND IdLiquidador = " + idLiquidador;
                queryString += " AND IdNomenclatura = " +  ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getLiquidacionesCajaChicha();
                queryString += " AND IdLiquidacion > 0 ";
                queryString += " AND Estatus IN ('INGRESADO', 'REVISADO')";
                queryString += " AND EXTRACT(YEAR FROM Fecha) > 2024";
                queryString += " GROUP BY CodigoCC, IdLiquidacion";

Logger.getLogger(this.getClass()).log(Level.INFO, "Liquidador=" + idLiquidador + " " + nombreLiquidador + " query=" + queryString);

                rsRecords1 = stQuery1.executeQuery(queryString);

                if(rsRecords1.next()) { // encontrado
                    codigoCC = rsRecords1.getString("CodigoCC");
                    ultimaLiquidacion = rsRecords1.getInt("IdLiquidacion");
                }
                else { // no tiene liquidacion abierta, crear una nueva
                    queryString = "SELECT *";
                    queryString += " FROM  contabilidad_empresa";
                    queryString += " WHERE IdEmpresa = " + empresaId;

                    rsRecords1 = stQuery1.executeQuery(queryString);

                    if(rsRecords1.next()) {
                        ultimaLiquidacion = rsRecords1.getInt("IdUltimaLiquidacion") + 1;
                    }
                    else {
                        ultimaLiquidacion = 1;
                    }

                    String fecha = fechaEmision; //Utileria.getFechaYYYYMMDD_1(new java.util.Date());
                                            //0123456789
                                            //1234567890
                    String ultimoEncontado; //yyyy-mm-dd
                    String dia = fecha.substring(8, 10);
                    String mes = fecha.substring(5, 7);
                    String año = fecha.substring(0, 4);

                    codigoCC = empresaId + año + mes + dia + "9";

                    queryString = " SELECT codigoCC FROM contabilidad_partida ";
                    queryString += " WHERE codigoCC LIKE '" + codigoCC + "%'";
                    queryString += " ORDER BY codigoCC DESC ";
                    queryString += " LIMIT 1";

                    rsRecords1 = stQuery1.executeQuery(queryString);

                    if (rsRecords1.next()) { //  encontrado
                        ultimoEncontado = rsRecords1.getString("codigoCC").substring(12, 15);
                        codigoCC += String.format("%03d", (Integer.parseInt(ultimoEncontado) + 1));
                    } else {
                        codigoCC += "001";
                    }
                }

Logger.getLogger(this.getClass()).log(Level.INFO, "Liquidador=" + idLiquidador + " " + nombreLiquidador + " codigoCC=" + codigoCC + " liquidacionId=" + ultimaLiquidacion);

                //actualizar la tabla documentos_fel_sat
                queryString = "UPDATE documentos_fel_sat SET ";
                queryString += " Accion = 'Liquidación'";
                queryString += ",IdLiquidador = " + idLiquidador;
                queryString += ",IdCentroCosto = " + idCentroCosto;
                queryString += ",IdNomenclatura = " + idNomenclatura;
                queryString += ",CodigoCentroCosto = '" + codigoCentroCosto + "'";
                queryString += ",ModificadoUsuario = " + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                queryString += ",ModificadoFechaYHora = current_timestamp";
                queryString += ",IdLiquidacion = " + ultimaLiquidacion;
                queryString += ",Contabilizada = 'S'";
                queryString += " WHERE Id = " + idDocumento;

                stQuery.executeUpdate(queryString);
                //crear la partida contable para este documento
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                String fecha = fechaEmision; //Utileria.getFechaYYYYMMDD_1(new java.util.Date());
                //0123456789
                //1234567890
                String ultimoEncontado; //yyyy-mm-dd
                String dia = fecha.substring(8, 10);
                String mes = fecha.substring(5, 7);
                String año = fecha.substring(0, 4);

                String codigoPartida = empresaId + año + mes + dia + "2";

                queryString  = " SELECT codigoPartida FROM contabilidad_partida ";
                queryString += " WHERE codigoPartida LIKE '" + codigoPartida + "%'";
                queryString += " ORDER BY codigoPartida desc ";

                rsRecords1 = stQuery1.executeQuery(queryString);

                if (rsRecords1.next()) { //  encontrado
                    ultimoEncontado = rsRecords1.getString("codigoPartida").substring(12, 15);
                    codigoPartida += String.format("%03d", (Integer.valueOf(ultimoEncontado) + 1));
                } else {
                    codigoPartida += "001";
                }
                /// HABER ingreso del LIQUIDACION
                queryString  = " INSERT INTO contabilidad_partida (IdEmpresa, Estatus, CodigoPartida, CodigoCC,";
                queryString += " TipoDocumento, Fecha, NITProveedor, IdProveedor, NombreProveedor,";
                queryString += " SerieDocumento, NumeroDocumento, IdNomenclatura, MonedaDocumento, Debe, Haber, ";
                queryString += " DebeQuetzales, HaberQuetzales, TipoCambio, MontoDocumento, Saldo, IdLiquidador, IdLiquidacion, ";
                queryString += " Descripcion, IdCentroCosto, CodigoCentroCosto, CreadoUsuario, CreadoFechaYHora)";
                queryString += " VALUES ";
                queryString += " (";
                queryString += empresaId;
                queryString += ",'INGRESADO'";
                queryString += ",'" + codigoPartida + "'";
                queryString += ",'" + codigoCC + "'";
                queryString += ",'FACTURA'";
                queryString += ",'" + fechaEmision + "'";
                queryString += ",'" + nitProveedor + "'";
                queryString += ", " + idProveedor;
                queryString += ",'" + nombreProveedor + "'";
                queryString += ",'" + serie + "'";
                queryString += ",'" + numero + "'";
                queryString += "," + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getLiquidacionesCajaChicha();
                queryString += ",'" + moneda + "'";
                queryString += ",0.00"; // DEBE
                queryString += "," + monto; //HABER
                queryString += ",0.00"; //DEBE Q
                queryString += "," + tasaCambio * Double.parseDouble(monto);
                queryString += "," + tasaCambio;
                queryString += "," + monto; // montodocumento
                queryString += "," + monto; // SALDO
                queryString += "," + idLiquidador;
                queryString += "," + ultimaLiquidacion;
                queryString += ",'LIQUIDACION GASTO " + ultimaLiquidacion + " " + nombreLiquidador + "'";
                queryString += "," + idCentroCosto;
                queryString += ",'" + codigoCentroCosto + "'";
                queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                queryString += ",current_timestamp";
                queryString += ")";

                /// DEBE ingreso del costo
                queryString += ",(";
                queryString += empresaId;
                queryString += ",'INGRESADO'";
                queryString += ",'" + codigoPartida + "'";
                queryString += ",'" + codigoCC + "'";
                queryString += ",'FACTURA'";
                queryString += ",'" + fechaEmision + "'";
                queryString += ",'" + nitProveedor + "'";
                queryString += ", " + idProveedor;
                queryString += ",'" + nombreProveedor + "'";
                queryString += ",'" + serie + "'";
                queryString += ",'" + numero + "'";
                queryString += "," + idNomenclatura;
                queryString += ",'" + moneda + "'";
                if(!((SopdiUI)UI.getCurrent()).sessionInformation.getStrAccountingCompanyRegimen().equals("EXENTA")) {
                    queryString += "," + costo; //DEBE
                    queryString += ",0.00"; // HABER
                    queryString += "," + tasaCambio * Double.parseDouble(costo); //DEBE  Q
                    queryString += ",0.00"; //HABER Q
                }
                else {
                    queryString += "," + monto; //DEBE
                    queryString += ",0.00"; // HABER
                    queryString += "," + tasaCambio * Double.parseDouble(monto); //DEBE  Q
                    queryString += ",0.00"; //HABER Q
                }
                queryString += "," + tasaCambio;
                queryString += "," + monto; // montodocumento
                queryString += "," + monto; // SALDO
                queryString += "," + idLiquidador;
                queryString += "," + ultimaLiquidacion;
                queryString += ",'LIQUIDACION GASTO " + ultimaLiquidacion + " " + nombreLiquidador + "'";
                queryString += "," + idCentroCosto;
                queryString += ",'" + codigoCentroCosto + "'";
                queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                queryString += ",current_timestamp";
                queryString += ")";

                /// DEBE ingreso del IVA
                if(iva > 0 && !((SopdiUI)UI.getCurrent()).sessionInformation.getStrAccountingCompanyRegimen().equals("EXENTA")) {
                    queryString += ",(";
                    queryString += empresaId;
                    queryString += ",'INGRESADO'";
                    queryString += ",'" + codigoPartida + "'";
                    queryString += ",'" + codigoCC + "'";
                    queryString += ",'FACTURA'";
                    queryString += ",'" + fechaEmision + "'";
                    queryString += ",'" + nitProveedor + "'";
                    queryString += ", " + idProveedor;
                    queryString += ",'" + nombreProveedor + "'";
                    queryString += ",'" + serie + "'";
                    queryString += ",'" + numero + "'";
                    queryString += "," + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getIvaPorCobrar();
                    queryString += ",'" + moneda + "'";
                    queryString += "," + iva; // DEBE
                    queryString += ",0.00"; //HABER
                    queryString += "," + tasaCambio * iva; //DEBE Q
                    queryString += ",0.00"; //HABER Q
                    queryString += "," + tasaCambio;
                    queryString += "," + monto; // montodocumento
                    queryString += "," + monto; // SALDO
                    queryString += "," + idLiquidador;
                    queryString += "," + ultimaLiquidacion;
                    queryString += ",'LIQUIDACION GASTO " + ultimaLiquidacion + " " + nombreLiquidador + "'";
                    queryString += "," + idCentroCosto;
                    queryString += ",'" + codigoCentroCosto + "'";
                    queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                    queryString += ",current_timestamp";
                    queryString += ")";
                }

                /// DEBE ingreso de OTROS IMPUESTOS
                if(otrosImpuestos > 0 && !((SopdiUI)UI.getCurrent()).sessionInformation.getStrAccountingCompanyRegimen().equals("EXENTA")) {
                    queryString += ",(";
                    queryString += empresaId;
                    queryString += ",'INGRESADO'";
                    queryString += ",'" + codigoPartida + "'";
                    queryString += ",'" + codigoCC + "'";
                    queryString += ",'FACTURA'";
                    queryString += ",'" + fechaEmision + "'";
                    queryString += ",'" + nitProveedor + "'";
                    queryString += ", " + idProveedor;
                    queryString += ",'" + nombreProveedor + "'";
                    queryString += ",'" + serie + "'";
                    queryString += ",'" + numero + "'";
                    queryString += "," + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getOtrosArbitrios();
                    queryString += ",'" + moneda + "'";
                    queryString += "," + otrosImpuestos; // DEBE
                    queryString += ",0.00"; //HABER
                    queryString += "," + otrosImpuestos; //DEBE Q
                    queryString += ",0.00"; //HABER Q
                    queryString += "," + tasaCambio;
                    queryString += "," + monto; // montodocumento
                    queryString += "," + monto; // SALDO
                    queryString += "," + idLiquidador;
                    queryString += "," + ultimaLiquidacion;
                    queryString += ",'LIQUIDACION GASTO " + ultimaLiquidacion + " " + nombreLiquidador + "'";
                    queryString += "," + idCentroCosto;
                    queryString += ",'" + codigoCentroCosto + "'";
                    queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                    queryString += ",current_timestamp";
                    queryString += ")";
                }

System.out.println("queryPartidaLiquidacionFelSatMobil="+queryString);

                stQuery1.executeUpdate(queryString);

                queryString = "UPDATE contabilidad_empresa SET";
                queryString += " IdUltimaLiquidacion = " + ultimaLiquidacion;
                queryString += " WHERE IdEmpresa = " + empresaId;

System.out.println("queryUpdateUltimaLiquidacion="+queryString);

                stQuery1.executeUpdate(queryString);

                queryString =  "UPDATE documento_liq_mobil SET";
                queryString += " Contabilizado = 1";
                queryString += " WHERE documento_liq_mobil.IdProveedor = " + idProveedor;
                queryString += " AND   documento_liq_mobil.Numero = '" + numero + "'";

                stQuery1.executeUpdate(queryString);

                facturasContabilizadas++;

                facturasFelContainer.removeItem(itemId);

//                Notification.show("Documento contabilizado correctamente.", Notification.Type.HUMANIZED_MESSAGE);

            } //end if es liquidacion caja chica
            else { // para el caso de ser una factura relacionada con una orden de compra y no liquiacion de caja chica

                queryString = " SELECT * ";
                queryString += " FROM orden_compra";
                queryString += " WHERE orden_compra.IdProveedor = " + idProveedor;
                queryString += " AND orden_compra.IdEmpresa =" + empresaId;
                queryString += " AND orden_compra.Moneda = '" + moneda + "'";
                queryString += " AND ((orden_compra.CodigoCCDocumento = '') OR (orden_compra.CodigoCCDocumento IS NULL) OR (orden_compra.CodigoCCDocumento = '0') )";
//                queryString += " AND ( (" + monto + " - orden_compra.Total) <= orden_compra.MontoTolerancia ) ";
//                queryString += " AND " + monto + " = orden_compra.Total";
                if(!idOrdenCompra.isEmpty()) {
                    queryString += " AND orden_compra.Id = " + idOrdenCompra;
                }

System.out.println("\n--->queryStringOrdenesCompra="+queryString);

                try {
//                    stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                    stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

                    rsRecords = stQuery.executeQuery(queryString);

                    if (rsRecords.next()) { // el proveedor si tiene ordenes de compra activas

                        double anticipo = 0.00;
                        String codigoCCAnticipo = "";
                        boolean encontrado = false;
                        int contador = 0;
                        String NOC = "";

                        if(idOrdenCompra.isEmpty()) {
                            do { //recorre la lista de ordenes de compra activas del proveedor

                                if (rsRecords.getDouble("MontoTolerancia") > 0) {
                                    if ((Double.parseDouble(monto) - rsRecords.getDouble("Total")) <= rsRecords.getDouble("MontoTolerancia")) {
                                        encontrado = true;
                                        contador++;
                                        idOrdenCompra = rsRecords.getString("Id");
                                        anticipo = rsRecords.getDouble("Anticipo");
                                        idNomenclatura = rsRecords.getString("IdNomenclatura");
                                        tipoOrdenCompra = rsRecords.getString("IdTipoOrdenCompra");
                                        codigoCCAnticipo = rsRecords.getString("CodigoCCAnticipo");
                                        NOC = rsRecords.getString("NOC");
//                                        break;
                                    }
                                } else {
                                    if (Double.parseDouble(monto) == rsRecords.getDouble("Total")) {
                                        encontrado = true;
                                        contador++;
                                        idOrdenCompra = rsRecords.getString("Id");
                                        anticipo = rsRecords.getDouble("Anticipo");
                                        idNomenclatura = rsRecords.getString("IdNomenclatura");//es null o 0 cuando es estimacion
                                        tipoOrdenCompra = rsRecords.getString("IdTipoOrdenCompra");
                                        codigoCCAnticipo = rsRecords.getString("CodigoCCAnticipo");
                                        NOC = rsRecords.getString("NOC");
//                                        break;
                                    }
                                }
                            } while (rsRecords.next());
//                            rsRecords.first();
                        }
                        else { // ya se tiene la orden de compra seleccionada
                            encontrado = true;
                            contador = 1;
                            anticipo = rsRecords.getDouble("Anticipo");
                            idNomenclatura = rsRecords.getString("IdNomenclatura"); // es null o 0 cuando es estimacion
                            tipoOrdenCompra = rsRecords.getString("IdTipoOrdenCompra");
                            codigoCCAnticipo = rsRecords.getString("CodigoCCAnticipo");
                        }

                        if(encontrado && contador == 1) {
                            // si se encontró una y solo 1 orden de compra activa, se procede a crear la partida contable respectiva

                            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                            String fecha = fechaEmision; //Utileria.getFechaYYYYMMDD_1(new java.util.Date());
                            //0123456789
                            //1234567890
                            String ultimoEncontado; //yyyy-mm-dd
                            String dia = fecha.substring(8, 10);
                            String mes = fecha.substring(5, 7);
                            String año = fecha.substring(0, 4);

                            String codigoPartida = empresaId + año + mes + dia + "1";

                            queryString = " SELECT codigoPartida FROM contabilidad_partida ";
                            queryString += " WHERE codigoPartida like '" + codigoPartida + "%'";
                            queryString += " ORDER BY codigoPartida DESC ";

                            rsRecords1 = stQuery1.executeQuery(queryString);

                            if (rsRecords1.next()) { //  encontrado
                                ultimoEncontado = rsRecords1.getString("codigoPartida").substring(12, 15);
                                codigoPartida += String.format("%03d", Integer.parseInt(ultimoEncontado) + 1);
                            } else {
                                codigoPartida += "001";
                            }

                            codigoCC = codigoPartida;

                            queryString = " INSERT INTO proveedor_cuentacorriente (IdEmpresa,IdProveedor, Fecha, ";
                            queryString += " TipoDocumento, SerieDocumento, NumeroDocumento, MonedaDocumento, ";
                            queryString += " Monto, MontoQuetzales, TipoCambio, ";
                            queryString += " IdUsuarioAutorizoPago,CreadoFechayHora,CreadoUsuario)";
                            queryString += " VALUES (";
                            queryString += empresaId;
                            queryString += "," + idProveedor;
                            queryString += ",'" + fechaEmision + "'";
                            queryString += ",'" + tipoDocumento + "'";
                            queryString += ",'" + serie + "'";
                            queryString += ",'" + numero + "'";
                            queryString += "," + moneda + "'";
                            queryString += "," + monto;
                            queryString += "," + tasaCambio * Double.parseDouble(monto);
                            queryString += "," + tasaCambio;
                            //        System.out.println("obtener el get value " + tasaCambioTxt.getValue());
                            //        System.out.println("obtener el get double" + tasaCambioTxt.getDoubleValueDoNotThrow());
                            queryString += ", " + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(new java.util.Date()) + "'";
                            queryString += ", " + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                            queryString += ")";

                            try {
                                stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                                stQuery.executeUpdate(queryString);
                            } catch (Exception ex1) {
//                                System.out.println("Error al insertar proveedor cuentaCorriente : " + ex1.getMessage());
//                                ex1.printStackTrace();
//                                Notification.show("Error al insertar proveedor cuentaCorriente.", Notification.Type.ERROR_MESSAGE);
                            }

                            queryString = "SELECT EsAbastos FROM proveedor_empresa WHERE IdProveedor = " + idProveedor + " AND IdEmpresa = " + empresaId;
                            rsRecords1 = stQuery1.executeQuery(queryString);
                            boolean esAbastos = false;
                            if (rsRecords1.next()) {
                                esAbastos = rsRecords1.getBoolean("EsAbastos");
                            }

                            /// HABER ingreso del DOCUMENTO
                            queryString = " INSERT INTO contabilidad_partida (IdEmpresa, Estatus, CodigoPartida,";
                            queryString += " TipoDocumento, Fecha, NITProveedor, IdProveedor, NombreProveedor,";
                            queryString += " SerieDocumento, NumeroDocumento, CodigoCC, IdNomenclatura, MonedaDocumento, Debe, Haber, ";
                            queryString += " DebeQuetzales, HaberQuetzales, TipoCambio, MontoDocumento, Saldo, IdLiquidador, IdLiquidacion, ";
                            queryString += " Descripcion, IdCentroCosto, CodigoCentroCosto, IdOrdenCompra, CreadoUsuario, CreadoFechaYHora)";
                            queryString += " VALUES ";
                            queryString += " (";
                            queryString += empresaId;
                            queryString += ",'INGRESADO'";
                            queryString += ",'" + codigoPartida + "'";
                            queryString += ",'" + tipoDocumento + "'";
                            queryString += ",'" + fechaEmision + "'";
                            queryString += ",'" + nitProveedor + "'";
                            queryString += ", " + idProveedor;
                            queryString += ",'" + nombreProveedor + "'";
                            queryString += ",'" + serie + "'";
                            queryString += ",'" + numero + "'";
                            if (anticipo > 0 ) {
                                if (codigoCCAnticipo.isEmpty()) { //no hay cheque por anticipo
                                    queryString += ",'" + codigoCC + "'";
                                    if(esAbastos) {
                                        queryString += "," + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getAbastos();
                                    } else {
                                        queryString += "," + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getProveedores();
                                    }
                                    queryString += ",'" + moneda + "'";
                                    queryString += ",0.00"; // DEBE
                                    queryString += "," + String.valueOf(Double.parseDouble(monto)); //HABER
                                    queryString += ",0.00"; //DEBE Q
                                    queryString += "," + tasaCambio * (Double.parseDouble(monto)); //HABER Q
                                } else { // hay cheque por anticipo, aplicar la diferencia en Haber
                                    queryString += ",'" + codigoCCAnticipo + "'";
                                    queryString += "," + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getAnticiposProveedor();
                                    queryString += ",'" + moneda + "'";
                                    queryString += ",0.00"; // DEBE
                                    queryString += "," + anticipo; //HABER
                                    queryString += ",0.00"; //DEBE Q
                                    queryString += "," + tasaCambio * anticipo; //HABER Q
                                }
                            } else { // no hay anticipo
                                queryString += ",'" + codigoCC + "'";
                                if(esAbastos) {
                                    queryString += "," + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getAbastos();
                                } else {
                                    queryString += "," + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getProveedores();
                                }
                                queryString += ",'" + moneda + "'";
                                queryString += ",0.00"; // DEBE
                                queryString += "," + monto; //HABER
                                queryString += ",0.00"; //DEBE Q
                                queryString += "," + tasaCambio * Double.parseDouble(monto); //HABER Q
                            }
                            queryString += "," + tasaCambio;
                            queryString += "," + monto; // montodocumento
                            queryString += "," + monto; // SALDO
                            queryString += ",0";
                            queryString += ",0";
                            queryString += ",'" + tipoDocumento + " " + nombreProveedor + " " + serie + " " + numero + "'";
                            queryString += ",0";
                            queryString += ",'" + codigoCentroCosto + "'";
                            queryString += "," + idOrdenCompra;
                            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                            queryString += ",current_timestamp";
                            queryString += ")";

                            if((anticipo > 0) && (Double.parseDouble(monto) > anticipo) && (!codigoCCAnticipo.isEmpty())) {
                                queryString += ",(";
                                queryString += empresaId;
                                queryString += ",'INGRESADO'";
                                queryString += ",'" + codigoPartida + "'";
                                queryString += ",'" + tipoDocumento + "'";
                                queryString += ",'" + fechaEmision + "'";
                                queryString += ",'" + nitProveedor + "'";
                                queryString += ", " + idProveedor;
                                queryString += ",'" + nombreProveedor + "'";
                                queryString += ",'" + serie + "'";
                                queryString += ",'" + numero + "'";
                                queryString += ",'" + codigoCC + "'";
                                if(esAbastos) {
                                    queryString += "," + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getAbastos();
                                } else {
                                    queryString += "," + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getProveedores();
                                }
                                queryString += ",'" + moneda + "'";
                                queryString += ",0.00"; // DEBE
                                queryString += "," + (Double.parseDouble(monto) - anticipo); //HABER
                                queryString += ",0.00"; //DEBE Q
                                queryString += "," + tasaCambio * (Double.parseDouble(monto) - anticipo); //HABER Q
                                queryString += "," + tasaCambio;
                                queryString += "," + monto; // montodocumento
                                queryString += "," + (Double.parseDouble(monto) - anticipo); // SALDO
                                queryString += ",0";
                                queryString += ",0";
                                queryString += ",'" + tipoDocumento + " " + nombreProveedor + " " + serie + " " + numero + "'";
                                queryString += ",0";
                                queryString += ",'" + codigoCentroCosto + "'";
                                queryString += "," + idOrdenCompra;
                                queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                                queryString += ",current_timestamp";
                                queryString += ")";
                            }

                            /// DEBE ingreso del IVA
                            if (iva > 0 && !((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyRegimen().equals("EXENTA")) {
                                queryString += ",(";
                                queryString += empresaId;
                                queryString += ",'INGRESADO'";
                                queryString += ",'" + codigoPartida + "'";
                                queryString += ",'" + tipoDocumento + "'";
                                queryString += ",'" + fechaEmision + "'";
                                queryString += ",'" + nitProveedor + "'";
                                queryString += ", " + idProveedor;
                                queryString += ",'" + nombreProveedor + "'";
                                queryString += ",'" + serie + "'";
                                queryString += ",'" + numero + "'";
                                queryString += ",'" + codigoCC + "'";
                                queryString += "," + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getIvaPorCobrar();
                                queryString += ",'" + moneda + "'";
                                queryString += "," + iva; // DEBE
                                queryString += ",0.00"; //HABER
                                queryString += "," + tasaCambio * iva; //DEBE Q
                                queryString += ",0.00"; //HABER Q
                                queryString += "," + tasaCambio;
                                queryString += "," + monto; // montodocumento
                                queryString += "," + monto; // SALDO
                                queryString += ",0";
                                queryString += ",0";
                                queryString += ",'" + tipoDocumento + " " + nombreProveedor + " " + serie + " " + numero + "'";
                                queryString += ",0";
                                queryString += ",'" + codigoCentroCosto + "'";
                                queryString += "," + idOrdenCompra;
                                queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                                queryString += ",current_timestamp";
                                queryString += ")";
                            }
                            /// DEBE ingreso de OTROS IMPUESTOS
                            if (otrosImpuestos > 0 && !((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyRegimen().equals("EXENTA")) {
                                queryString += ",(";
                                queryString += empresaId;
                                queryString += ",'INGRESADO'";
                                queryString += ",'" + codigoPartida + "'";
                                queryString += ",'" + tipoDocumento + "'";
                                queryString += ",'" + fechaEmision + "'";
                                queryString += ",'" + nitProveedor + "'";
                                queryString += ", " + idProveedor;
                                queryString += ",'" + nombreProveedor + "'";
                                queryString += ",'" + serie + "'";
                                queryString += ",'" + numero + "'";
                                queryString += ",'" + codigoCC + "'";
                                queryString += "," + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getOtrosArbitrios();
                                queryString += ",'" + moneda + "'";
                                queryString += "," + otrosImpuestos; // DEBE
                                queryString += ",0.00"; //HABER
                                queryString += "," + tasaCambio * otrosImpuestos; //DEBE Q
                                queryString += ",0.00"; //HABER Q
                                queryString += "," + tasaCambio;
                                queryString += "," + monto; // montodocumento
                                queryString += "," + monto; // SALDO
                                queryString += "," + idLiquidador;
                                queryString += "," + ultimaLiquidacion;
                                queryString += ",'" + tipoDocumento + " " + nombreProveedor + " " + serie + " " + numero + "'";
                                queryString += ",0";
                                queryString += ",'" + codigoCentroCosto + "'";
                                queryString += "," + idOrdenCompra;
                                queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                                queryString += ",current_timestamp";
                                queryString += ")";
                            }

                            /**************************************************************************************/
                            // COSTOS
                            /**************************************************************************************/

                            dcosto = 0.00;
                           String subQueryString = "";

                            if(tipoOrdenCompra.equals("1") || tipoOrdenCompra.equals("2")) { // es orden de compra estimacion o parcial, el detalle ya tiene el monto por cada centro de costo

                                subQueryString = " SELECT DISTINCT centro_costo.IdCentroCosto, centro_costo.CodigoCentroCosto, ";
                                subQueryString += "  centro_costo.IdNomenclatura, centro_costo.IdNomenclaturaProvision, (SUM(orden_compra_detalle.Total) / 1.12) AS TotalCosto "; //costo es el total - iva
                                subQueryString += " FROM orden_compra_detalle";
                                subQueryString += " INNER JOIN centro_costo ON centro_costo.CodigoCentroCosto = orden_compra_detalle.IdCC";
                                subQueryString += " WHERE orden_compra_detalle.IdOrdenCompra = " + idOrdenCompra;
                                subQueryString += " AND centro_costo.IdEmpresa = " + empresaId;
                                subQueryString += " GROUP BY centro_costo.IdCentroCosto";

System.out.println("\n\nTEMPORALLOG=subQueryString=" + subQueryString);

                                stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

                                rsRecords1 = stQuery1.executeQuery(subQueryString);

                                rsRecords1.next();

                                //test
                                /// DEBE ingreso del costo
                                do { // recorre la lista de centros de costo de la orden de compra
                                    costo = rsRecords1.getString("TotalCosto");

                                    queryString += ",(";
                                    queryString += empresaId;
                                    queryString += ",'INGRESADO'";
                                    queryString += ",'" + codigoPartida + "'";
                                    queryString += ",'" + tipoDocumento + "'";
                                    queryString += ",'" + fechaEmision + "'";
                                    queryString += ",'" + nitProveedor + "'";
                                    queryString += ", " + idProveedor;
                                    queryString += ",'" + nombreProveedor + "'";
                                    queryString += ",'" + serie + "'";
                                    queryString += ",'" + numero + "'";
                                    queryString += ",'" + codigoCC + "'";

                                    // Leer tabla contabilidad_partida para validar si ya se ha emitido una Factura Venta para el Centro de Costo
                                    // De ser asi, se debe utilizar el idNomenclaturaProvision del Centro de Costo (leer tabla centro_costo)

                                    subQueryString = " SELECT * FROM contabilidad_partida ";
                                    subQueryString += " WHERE contabilidad_partida.CodigoCentroCosto = '" + rsRecords1.getString("CodigoCentroCosto") + "'";
                                    subQueryString += " AND contabilidad_partida.TipoDocumento = 'FACTURA VENTA' ";
                                    subQueryString += " AND contabilidad_partida.Estatus <> 'ANULADO' ";
                                    subQueryString += " LIMIT 1 ";

System.out.println("TEMPORALLOG=subQueryStringVerificaFacturaVenta=" + subQueryString);

                                    stQuery2 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();

                                    rsRecords = stQuery2.executeQuery(subQueryString);
                                    if(rsRecords.next()) { // el centro de costo ya tiene una factura de venta asociada
                                        queryString += "," + rsRecords1.getString("IdNomenclaturaProvision");
                                    }
                                    else {
                                        queryString += "," + rsRecords1.getString("IdNomenclatura");
                                    }
                                    queryString += ",'" + moneda + "'";
                                    queryString += "," + costo; //DEBE
                                    queryString += ",0.00"; // HABER
                                    queryString += "," + tasaCambio * Double.parseDouble(costo); //DEBE  Q
                                    queryString += ",0.00"; //HABER Q
                                    queryString += "," + tasaCambio;
                                    queryString += "," + monto; // montodocumento
                                    queryString += "," + monto; // SALDO
                                    queryString += ",0";
                                    queryString += ",0";
                                    queryString += ",'" + tipoDocumento + " " + nombreProveedor + " " + serie + " " + numero + "'";
                                    queryString += "," + rsRecords1.getString("IdCentroCosto");
                                    queryString += ",'" + rsRecords1.getString("CodigoCentroCosto") + "'";
                                    queryString += "," + idOrdenCompra;
                                    queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                                    queryString += ",current_timestamp";
                                    queryString += ")";

                                } while (rsRecords1.next());  //centros costo para orden de compra tipo estimacion

                                subQueryString = " SELECT DISTINCT centro_costo.IdCentroCosto, orden_compra_detalle.* ";
                                subQueryString += " FROM orden_compra_detalle";
                                subQueryString += " INNER JOIN centro_costo ON centro_costo.CodigoCentroCosto = orden_compra_detalle.IdCC";
                                subQueryString += " WHERE orden_compra_detalle.IdOrdenCompra = " + idOrdenCompra;
                                subQueryString += " AND centro_costo.IdEmpresa = " + empresaId;

//                                System.out.println("\n\nTEMPORALLOG=subQueryStringCentrosCostoDetalle=" + subQueryString);

                                stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

                                rsRecords1 = stQuery1.executeQuery(subQueryString);

                                if(rsRecords1.next()) {
                                    /// DEBE ingreso del costo
                                    do { // recorre la lista de centros de c

                                        if (rsRecords1.isFirst()) {
                                            queryStringDOCA = "INSERT INTO DocumentosContablesAplicados (";
                                            queryStringDOCA += " TipoDoc, Serie, NoDocumento, Fecha, IdProveedor, Proveedor, NoCuenta, Descripcion,";
                                            queryStringDOCA += " IdProject, IdCC, Empresa, IdEmpresa, Total, Moneda, Unidades, Precio, Provision,";
                                            queryStringDOCA += " TotEst, Estimacion, Tasa, TotalQ, FechaIngreso,";
                                            queryStringDOCA += " Noc, Lote, IdexAnterior, Rebajado, NoRefCi, Idex, IdNomenclatura )";
                                            queryStringDOCA += " VALUES ";
                                            queryStringDOCA += "('Factura'";  //tipodoc
                                        } else {
                                            queryStringDOCA += ",( 'Factura'";  //tipodoc
                                        }
                                        queryStringDOCA += ",'" + serie + "'"; //serie
                                        queryStringDOCA += ",'" + numero + "'";
                                        queryStringDOCA += ",'" + fechaEmision + "'"; // fecha de la factura
                                        queryStringDOCA += ",'" + idProveedor + "'";
                                        queryStringDOCA += ",'" + nombreProveedor + "'";
                                        queryStringDOCA += ",'" + rsRecords1.getString("NoCuenta") + "'"; //nocuenta
                                        queryStringDOCA += ",'" + rsRecords1.getString("Descripcion") + "'";  //descripcion;
                                        queryStringDOCA += ",'" + rsRecords1.getString("IdProject") + "'"; //idproject numero
                                        queryStringDOCA += ",'" + rsRecords1.getString("IDCC") + "'"; //centro costo
                                        queryStringDOCA += ",'" + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName() + "'";
                                        queryStringDOCA += "," + empresaId;
                                        queryStringDOCA += "," + rsRecords1.getString("Total");; //total
                                        queryStringDOCA += ",'" + moneda + "'"; //moneda
                                        queryStringDOCA += "," + rsRecords1.getString("Cantidad");
                                        queryStringDOCA += "," + rsRecords1.getString("Precio");
                                        queryStringDOCA += ",0"; //provision
                                        queryStringDOCA += "," + rsRecords1.getString("Total"); //totest
                                        queryStringDOCA += ",0";//estiamacion
                                        queryStringDOCA += ",'" + tasaCambio + "'"; //tasa
                                        queryStringDOCA += "," + tasaCambio * rsRecords1.getDouble("Total"); //totalq
                                        queryStringDOCA += ",current_date"; //fechaingreso
                                        queryStringDOCA += ",'" + NOC.substring(7, NOC.length()) + "'"; // noc
                                        queryStringDOCA += ",0"; //lote
                                        queryStringDOCA += ",0"; //idexanterior
                                        queryStringDOCA += ",null";// rebajado
                                        queryStringDOCA += ",null";//norefci
                                        queryStringDOCA += ",'" + rsRecords1.getString("IDEX") + "'";
                                        queryStringDOCA += "," + idNomenclatura;
                                        queryStringDOCA += ")";

                                    } while (rsRecords1.next());  //centros costo para orden de compra tipo no estimacion
                                } // end if rsRecords1.next()

                            }
                            else { // no es orden de compra estimacion o parcial, entonces se debe dividir el monto entre los centros de costo
                                subQueryString = " SELECT DISTINCT centro_costo.IdCentroCosto, centro_costo.CodigoCentroCosto";
                                subQueryString += " FROM orden_compra_detalle";
                                subQueryString += " INNER JOIN centro_costo ON centro_costo.CodigoCentroCosto = orden_compra_detalle.IdCC";
                                subQueryString += " AND centro_costo.IdEmpresa = " + empresaId;
                                subQueryString += " WHERE orden_compra_detalle.IdOrdenCompra = " + idOrdenCompra;

                                stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

                                rsRecords1 = stQuery1.executeQuery(subQueryString);

                                if(!rsRecords1.next()) {
                                    Notification.show("La Orden de Compra parece que no tiene Centros de Costo asociados.", Notification.Type.ERROR_MESSAGE);
                                    return;
                                }
                                else {
                                    rsRecords1.first(); // solo para retornar al primer registro
                                }

                                int size = 1; //por lo menos 1 centro de costo, y para evitar dividir entre 0
                                if (rsRecords1 != null) {
                                    rsRecords1.last(); //asi sabemos cuantos registro son
                                    size = rsRecords1.getRow();
                                }
                                if (!((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyRegimen().equals("EXENTA")) {
                                    dcosto = Double.valueOf(costo) / size;
                                    costo = String.valueOf(dcosto);
                                } else {
                                    dcosto = Double.valueOf(monto) / size;
                                    costo = String.valueOf(dcosto);
                                }

                                // mover el cursor al primer registro centros de costo,
                                // porque anteriormeinte fue movido a ultimo registro para saber cuantos registros hay
                                rsRecords1.first();

                                do { // recorre la lista de centros de costo de la orden de compra NO estimacion
                                    /// DEBE ingreso del costo
                                    queryString += ",(";
                                    queryString += empresaId;
                                    queryString += ",'INGRESADO'";
                                    queryString += ",'" + codigoPartida + "'";
                                    queryString += ",'" + tipoDocumento + "'";
                                    queryString += ",'" + fechaEmision + "'";
                                    queryString += ",'" + nitProveedor + "'";
                                    queryString += ", " + idProveedor;
                                    queryString += ",'" + nombreProveedor + "'";
                                    queryString += ",'" + serie + "'";
                                    queryString += ",'" + numero + "'";
                                    queryString += ",'" + codigoCC + "'";
                                    queryString += "," + idNomenclatura;
                                    queryString += ",'" + moneda + "'";
                                    queryString += "," + costo; //DEBE
                                    queryString += ",0.00"; // HABER
                                    queryString += "," + tasaCambio * Double.parseDouble(costo); //DEBE  Q
                                    queryString += ",0.00"; //HABER Q
                                    queryString += "," + tasaCambio;
                                    queryString += "," + monto; // montodocumento
                                    queryString += "," + monto; // SALDO
                                    queryString += ",0";
                                    queryString += ",0";
                                    queryString += ",'" + tipoDocumento + " " + nombreProveedor + " " + serie + " " + numero + "'";
                                    queryString += "," + rsRecords1.getString("IdCentroCosto");
                                    queryString += ",'" + rsRecords1.getString("CodigoCentroCosto") + "'";
                                    queryString += "," + idOrdenCompra;
                                    queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                                    queryString += ",current_timestamp";
                                    queryString += ")";

                                } while (rsRecords1.next());  //centros costo para orden de compra tipo no estimacion

                            } // fin else no es orden de compra estimacion

System.out.println("TEMPORALLOG=queryString=" + queryString);
System.out.println("TEMPORALLOG=queryStringInsertDOCA=" + queryStringDOCA);

                            stQuery.executeUpdate(queryString);

                            if (!queryStringDOCA.isEmpty()) {
                                stQuery.executeUpdate(queryStringDOCA);
                            }

                            if (tipoOrdenCompra.equals("1") || tipoOrdenCompra.equals("2") || tipoOrdenCompra.equals("3") || tipoOrdenCompra.equals("4")) {
                                queryString = "UPDATE orden_compra SET ";
                                queryString += " CodigoCCDocumento = '" + codigoCC + "'";
                                queryString += ",Estado = 'CERRADA'";
                                queryString += " WHERE Id = " + idOrdenCompra;
//System.out.println("\n\nqueryStringUpdateOrdenCompra=" + queryString);
                                stQuery.executeUpdate(queryString);
                            }

                            //actualizar la tabla documentos_fel_sat
                            queryString = "UPDATE documentos_fel_sat SET ";
                            queryString += " Accion = 'Proveedor'";
                            queryString += ",ModificadoUsuario = " + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                            queryString += ",ModificadoFechaYHora = current_timestamp";
                            queryString += ",Contabilizada = 'S'";
                            queryString += " WHERE Id = " + idDocumento;

                            stQuery.executeUpdate(queryString);

                            facturasContabilizadas++;

                            facturasFelContainer.removeItem(itemId);

                            verificarPartida(codigoPartida);

                        }//end if encontrado
                        else {
                            llenarGridOrdenCompra();
                        }
                    }//end if record.next

                } catch (Exception ex) {
                    ex.printStackTrace();
                    Notification.show("Error al contabilizar documento." + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                }
            }
        } catch (Exception ex1) {
            ex1.printStackTrace();
            Notification.show("Error al contabilizar documento." + ex1.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }

    /**
     * Contabilizacion forzada, sin tiene orden de compra
     */
    public void contabilizarForzada(Object itemId, int idDocumento) {

        String fechaEmision = "";
        String idProveedor = "0";
        String nitProveedor = "0";
        String nombreProveedor = "";
        String numero = "";
        String serie = "";
        String monto = "0";
        String costo = "0";
        double iva = 0.00;
        double otrosImpuestos = 0.00;
        double tasaCambio = 1.0;
        String codigoCC = "";
        String codigoCentroCosto = "";
        String moneda = "QUETZALES";
        int idOrdenCompra = 0;

        if(centroCostoCbx.getValue() == null) {
            Notification.show("Debe seleccionar un centro de costo.", Notification.Type.WARNING_MESSAGE);
            return;
        }
        else {
            codigoCentroCosto = centroCostoCbx.getItemCaption(centroCostoCbx.getValue());
        }
        
        queryString = "SELECT *";
        queryString += " FROM documentos_fel_sat";
        queryString += " WHERE Id = " + idDocumento;

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();

            rsRecords = stQuery.executeQuery(queryString);
            rsRecords.next();

            String tipoDocumento = rsRecords.getString("TipoDTE");

            fechaEmision = rsRecords.getString("FechaEmision");
            idProveedor = rsRecords.getString("IdProveedor");
            nitProveedor = rsRecords.getString("NitProveedor");
            nombreProveedor = rsRecords.getString("NombreProveedor");
            moneda = rsRecords.getString("Moneda");
            numero = rsRecords.getString("Numero");
            serie = rsRecords.getString("Serie");
            monto = rsRecords.getString("Monto");
            iva = rsRecords.getDouble("Iva");
            costo = rsRecords.getString("Costo");
            otrosImpuestos = rsRecords.getDouble("IDP") + rsRecords.getDouble("TurismoHospedaje");
            otrosImpuestos += rsRecords.getDouble("TurismoPasajes") + rsRecords.getDouble("TimbrePrensa");
            otrosImpuestos += rsRecords.getDouble("Bomberos") + rsRecords.getDouble("TasaMunicipal");
            otrosImpuestos += rsRecords.getDouble("BebidasAlcoholicas");
            otrosImpuestos += rsRecords.getDouble("Tabaco")  + rsRecords.getDouble("Cemento");
            otrosImpuestos += rsRecords.getDouble("BebidasNoAlcoholicas")  + rsRecords.getDouble("TarifaPortuaria");

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            String fecha = fechaEmision; //Utileria.getFechaYYYYMMDD_1(new java.util.Date());
            //0123456789
            //1234567890
            String ultimoEncontado; //yyyy-mm-dd
            String dia = fecha.substring(8, 10);
            String mes = fecha.substring(5, 7);
            String año = fecha.substring(0, 4);

            String codigoPartida = empresaId + año + mes + dia + "1";

            queryString = " SELECT codigoPartida FROM contabilidad_partida ";
            queryString += " WHERE codigoPartida like '" + codigoPartida + "%'";
            queryString += " ORDER BY codigoPartida DESC ";

            rsRecords1 = stQuery1.executeQuery(queryString);

            if (rsRecords1.next()) { //  encontrado
                ultimoEncontado = rsRecords1.getString("codigoPartida").substring(12, 15);
                codigoPartida += String.format("%03d", Integer.parseInt(ultimoEncontado) + 1);
            } else {
                codigoPartida += "001";
            }

            codigoCC = codigoPartida;

            queryString = "SELECT IdNomenclatura, IdNomenclaturaProvision ";
            queryString += " FROM centro_costo ";
            queryString += " WHERE IdCentroCosto = " + centroCostoCbx.getValue();
            queryString += " AND IdEmpresa = " + empresaId;

            rsRecords1 = stQuery1.executeQuery(queryString);
            String idNomenclatura = "0";
            if (rsRecords1.next()) {
                idNomenclatura = rsRecords1.getString("IdNomenclatura");
            }

            /// INGRESO A CUENTA CORRIENTE DEL PROVEEDOR
            queryString = " INSERT INTO proveedor_cuentacorriente (IdEmpresa,IdProveedor, Fecha, ";
            queryString += " TipoDocumento, SerieDocumento, NumeroDocumento, MonedaDocumento, ";
            queryString += " Monto, MontoQuetzales, TipoCambio, ";
            queryString += " IdUsuarioAutorizoPago,CreadoFechayHora,CreadoUsuario)";
            queryString += " VALUES (";
            queryString += empresaId;
            queryString += "," + idProveedor;
            queryString += ",'" + fechaEmision + "'";
            queryString += ",'" + tipoDocumento + "'";
            queryString += ",'" + serie + "'";
            queryString += ",'" + numero + "'";
            queryString += "," + moneda + "'";
            queryString += "," + monto;
            queryString += "," + tasaCambio * Double.parseDouble(monto);
            queryString += "," + tasaCambio;
            //        System.out.println("obtener el get value " + tasaCambioTxt.getValue());
            //        System.out.println("obtener el get double" + tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += ", " + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(new java.util.Date()) + "'";
            queryString += ", " + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ")";

            try {
                stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                stQuery.executeUpdate(queryString);
            } catch (Exception ex1) {
//                                System.out.println("Error al insertar proveedor cuentaCorriente : " + ex1.getMessage());
//                                ex1.printStackTrace();
//                                Notification.show("Error al insertar proveedor cuentaCorriente.", Notification.Type.ERROR_MESSAGE);
            }

//            queryString = "SELECT EsAbastos FROM proveedor WHERE IdProveedor = " + idProveedor;
//            rsRecords1 = stQuery1.executeQuery(queryString);
//            boolean esAbastos = false;
//            proveedorAbastoCbx.setValue("Proveedor");
//            proveedorAbastoCbx.select("Proveedor");
//            if (rsRecords1.next()) {
//                esAbastos = rsRecords1.getBoolean("EsAbastos");
//            }

            /// HABER ingreso del DOCUMENTO
            queryString = " Insert Into contabilidad_partida (IdEmpresa, Estatus, CodigoPartida,";
            queryString += " TipoDocumento, Fecha, NITProveedor, IdProveedor, NombreProveedor,";
            queryString += " SerieDocumento, NumeroDocumento, CodigoCC, IdNomenclatura, MonedaDocumento, Debe, Haber, ";
            queryString += " DebeQuetzales, HaberQuetzales, TipoCambio, MontoDocumento, Saldo, IdLiquidador, IdLiquidacion, ";
            queryString += " Descripcion, IdCentroCosto, CodigoCentroCosto, IdOrdenCompra, CreadoUsuario, CreadoFechaYHora)";
            queryString += " Values ";
            queryString += " (";
            queryString += empresaId;
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + tipoDocumento + "'";
            queryString += ",'" + fechaEmision + "'";
            queryString += ",'" + nitProveedor + "'";
            queryString += ", " + idProveedor;
            queryString += ",'" + nombreProveedor + "'";
            queryString += ",'" + serie + "'";
            queryString += ",'" + numero + "'";
            queryString += ",'" + codigoCC + "'";
            if(proveedorAbastoCbx.getValue().toString().equals("Abastos")) {
                queryString += "," + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getAbastos();
            } else {
                queryString += "," + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getProveedores();
            }
            queryString += ",'" + moneda + "'";
            queryString += ",0.00"; // DEBE
            queryString += "," + monto; //HABER
            queryString += ",0.00"; //DEBE Q
            queryString += "," + tasaCambio * Double.parseDouble(monto); //HABER Q
            queryString += "," + tasaCambio;
            queryString += "," + monto; // montodocumento
            queryString += "," + monto; // SALDO
            queryString += ",0";
            queryString += ",0";
            queryString += ",'" + tipoDocumento + " " + nombreProveedor + " " + serie + " " + numero + "'";
            queryString += "," + centroCostoCbx.getValue();
            queryString += ",'" + centroCostoCbx.getItemCaption(centroCostoCbx.getValue())+ "'";
            queryString += "," + idOrdenCompra;
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";

            /// DEBE ingreso del IVA
            if (iva > 0 && !((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyRegimen().equals("EXENTA")) {
                queryString += ",(";
                queryString += empresaId;
                queryString += ",'INGRESADO'";
                queryString += ",'" + codigoPartida + "'";
                queryString += ",'" + tipoDocumento + "'";
                queryString += ",'" + fechaEmision + "'";
                queryString += ",'" + nitProveedor + "'";
                queryString += ", " + idProveedor;
                queryString += ",'" + nombreProveedor + "'";
                queryString += ",'" + serie + "'";
                queryString += ",'" + numero + "'";
                queryString += ",'" + codigoCC + "'";
                queryString += "," + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getIvaPorCobrar();
                queryString += ",'" + moneda + "'";
                queryString += "," + iva; // DEBE
                queryString += ",0.00"; //HABER
                queryString += "," + tasaCambio * iva; //DEBE Q
                queryString += ",0.00"; //HABER Q
                queryString += "," + tasaCambio;
                queryString += "," + monto; // montodocumento
                queryString += "," + monto; // SALDO
                queryString += ",0";
                queryString += ",0";
                queryString += ",'" + tipoDocumento + " " + nombreProveedor + " " + serie + " " + numero + "'";
                queryString += "," + centroCostoCbx.getValue();
                queryString += ",'" + centroCostoCbx.getItemCaption(centroCostoCbx.getValue())+ "'";
                queryString += "," + idOrdenCompra;
                queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                queryString += ",current_timestamp";
                queryString += ")";
            }
            /// DEBE ingreso de OTROS IMPUESTOS
            if (otrosImpuestos > 0 && !((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyRegimen().equals("EXENTA")) {
                queryString += ",(";
                queryString += empresaId;
                queryString += ",'INGRESADO'";
                queryString += ",'" + codigoPartida + "'";
                queryString += ",'" + tipoDocumento + "'";
                queryString += ",'" + fechaEmision + "'";
                queryString += ",'" + nitProveedor + "'";
                queryString += ", " + idProveedor;
                queryString += ",'" + nombreProveedor + "'";
                queryString += ",'" + serie + "'";
                queryString += ",'" + numero + "'";
                queryString += ",'" + codigoCC + "'";
                queryString += "," + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getOtrosArbitrios();
                queryString += ",'" + moneda + "'";
                queryString += "," + otrosImpuestos; // DEBE
                queryString += ",0.00"; //HABER
                queryString += "," + tasaCambio * otrosImpuestos; //DEBE Q
                queryString += ",0.00"; //HABER Q
                queryString += "," + tasaCambio;
                queryString += "," + monto; // montodocumento
                queryString += "," + monto; // SALDO
                queryString += ",0";
                queryString += ",0" ;
                queryString += ",'" + tipoDocumento + " " + nombreProveedor + " " + serie + " " + numero + "'";
                queryString += "," + centroCostoCbx.getValue();
                queryString += ",'" + centroCostoCbx.getItemCaption(centroCostoCbx.getValue())+ "'";
                queryString += "," + idOrdenCompra;
                queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                queryString += ",current_timestamp";
                queryString += ")";
            }

            /// DEBE ingreso del costo
            queryString += ",(";
            queryString += empresaId;
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + tipoDocumento + "'";
            queryString += ",'" + fechaEmision + "'";
            queryString += ",'" + nitProveedor + "'";
            queryString += ", " + idProveedor;
            queryString += ",'" + nombreProveedor + "'";
            queryString += ",'" + serie + "'";
            queryString += ",'" + numero + "'";
            queryString += ",'" + codigoCC + "'";
            // Leer tabla contabilidad_partida para validar si ya se ha emitido una Factura Venta para el Centro de Costo
            // De ser asi, se debe utilizar el idNomenclaturaProvision del Centro de Costo (leer tabla centro_costo)

            String subQueryString = " SELECT * FROM contabilidad_partida ";
            subQueryString += " WHERE contabilidad_partida.CodigoCentroCosto = '" + rsRecords1.getString("CodigoCentroCosto") + "'";
            subQueryString += " AND contabilidad_partida.TipoDocumento = 'FACTURA VENTA' ";
            subQueryString += " AND contabilidad_partida.Estatus <> 'ANULADO' ";
            subQueryString += " LIMIT 1 ";

            System.out.println("TEMPORALLOG=subQueryStringVerificaFacturaVenta=" + subQueryString);

            stQuery2 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();

            rsRecords = stQuery2.executeQuery(subQueryString);
            if(rsRecords.next()) { // el centro de costo ya tiene una factura de venta asociada
                queryString += "," + rsRecords1.getString("IdNomenclaturaProvision");
            }
            else {
                queryString += "," + rsRecords1.getString("IdNomenclatura");
            }

            queryString += "," + idNomenclatura;
            queryString += ",'" + moneda + "'";
            queryString += "," + costo; //DEBE
            queryString += ",0.00"; // HABER
            queryString += "," + tasaCambio * Double.parseDouble(costo); //DEBE  Q
            queryString += ",0.00"; //HABER Q
            queryString += "," + tasaCambio;
            queryString += "," + monto; // montodocumento
            queryString += "," + monto; // SALDO
            queryString += ",0";
            queryString += ",0";
            queryString += ",'" + tipoDocumento + " " + nombreProveedor + " " + serie + " " + numero + "'";
            queryString += "," + centroCostoCbx.getValue();
            queryString += ",'" + centroCostoCbx.getItemCaption(centroCostoCbx.getValue())+ "'";
            queryString += "," + idOrdenCompra;
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";

            stQuery.executeUpdate(queryString);

            //actualizar la tabla documentos_fel_sat
            queryString = "UPDATE documentos_fel_sat SET ";
            queryString += " Accion = 'Proveedor'";
            queryString += ",ModificadoUsuario = " + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",ModificadoFechaYHora = current_timestamp";
            queryString += ",Contabilizada = 'S'";
            queryString += " WHERE Id = " + idDocumento;

            stQuery.executeUpdate(queryString);

            facturasContabilizadas++;

            facturasFelContainer.removeItem(itemId);

            Notification.show("Documento contabilizado con exito!!..Código de Partida = " + codigoPartida, Notification.Type.TRAY_NOTIFICATION);

            verificarPartida(codigoPartida);

        } catch (Exception ex1) {
            ex1.printStackTrace();
            Notification.show("Error al contabilizar documento." + ex1.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }

    public void visualizarPdf() {

        Object selectedObject = facturasFelGrid.getSelectedRow();
        String numeroAutorizacion = String.valueOf(facturasFelContainer.getContainerProperty(selectedObject, "numeroAutorizacion").getValue());

        try {

            String filePath = environmentsVars.getDtePath();

            String fileName = filePath + numeroAutorizacion + ".pdf";
//Logger.getLogger(this.getClass()).log(Level.INFO, fileName);
            final byte docBytes[] = Files.readAllBytes(new File(fileName).toPath());

            if (docBytes == null) {
                Notification.show("Documento PDF no disponible para visualizar!");
                return;
            }
            Window window = new Window();
            window.setResizable(true);
            window.setWidth("50%");
            window.setHeight("50%");
            window.center();

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
            documentStreamResource.setMIMEType("PDF");
            documentStreamResource.setFilename(fileName);
            documentStreamResource.getStream().setParameter("Content-Disposition", "attachment; filename=" + fileName);

//            System.out.println("Tipo de Archivo : " + rsRecords.getString("ArchivoTipo"));
//            System.out.println("Archivo Nombre :" + rsRecords.getString("ArchivoNombre"));
//            System.out.println("Tamanio :" + rsRecords.getString("ArchivoPeso"));
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

                            long fileSize = stream.available();
                            byte[] buffer = new byte[stream.available()];
                            stream.read(buffer);

//                                String filePath = VaadinService.getCurrent()
//                                        .getBaseDirectory().getAbsolutePath() + "/projectfiles/" + ((SopdiUI) mainUI).sessionInformation.getStrCompanyId() + "/";
                            String filePath = environmentsVars.getDtePath();

                            new File(filePath).mkdirs();

                            fileName = filePath + numeroAutorizacion + ".pdf";

                            new File(filePath).mkdirs();

                            targetFile = new File(fileName);
                            OutputStream outStream = new FileOutputStream(targetFile);
                            outStream.write(buffer);
                            outStream.close();

                            stream.close();

                            System.out.println("\ntargetFile = " + fileName);

                            pdfStreamResource = null;

                            if (buffer != null) {
                                pdfStreamResource = new StreamResource(
                                        new StreamResource.StreamSource() {
                                            public InputStream getStream() {
                                                return new ByteArrayInputStream(buffer);
                                            }
                                        }, String.valueOf(System.currentTimeMillis())
                                );
                            }

                            recibirParametrosImagen(targetFile.getName(), mimeType, fileSize);

                            Notification.show("Archivo PDF cargado con exito!!..", Notification.Type.TRAY_NOTIFICATION);

                            window.close();
                        } else {
                            Notification notif = new Notification("El archivo no contiene un formato compatible. solo puede subir archivos con formato 'PDF'",
                                    Notification.Type.WARNING_MESSAGE);
                            notif.setDelayMsec(1500);
                            notif.setPosition(Position.MIDDLE_CENTER);
                            notif.setIcon(FontAwesome.WARNING);
                            notif.show(Page.getCurrent());
                            return;
                        }
                    } catch (java.io.IOException fIoEx) {
                        fIoEx.printStackTrace();
                        Notification.show("Error al cargar el archivo PDF!", Notification.Type.ERROR_MESSAGE);
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

            UI.getCurrent().addWindow(window);
            window.center();

        } catch (Exception allEx) {
            Notification.show("No existe archivo PDF,  o no se puede leer el archivo.", Notification.Type.ERROR_MESSAGE);
            allEx.printStackTrace();
        }

    }

    /**
     * Verifica si la partida ya fue contabilizada y si esta cuadrada, si no esta cuadrada avisa al usuario
     *
     * @param codigoPartida
     */
    private void verificarPartida(String codigoPartida) {
        String queryString = " SELECT MAX(IdPartida) MaxIDPartida ";
        queryString += " FROM contabilidad_partida ";
        queryString += " WHERE CodigoPartida = '" + codigoPartida + "'";

        int maxIdPartida = 0;

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);
            if (rsRecords.next()) {
                maxIdPartida = rsRecords.getInt("MaxIDPartida");
            }

            queryString = " SELECT SUM(DEBE) SUMADEBE, SUM(HABER) SUMAHABER";
            queryString += " FROM contabilidad_partida ";
            queryString += " WHERE CodigoPartida = '" + codigoPartida + "'";

            rsRecords = stQuery.executeQuery(queryString);
            if (rsRecords.next()) {
                if(rsRecords.getDouble("SUMADEBE") != rsRecords.getDouble("SUMAHABER")) {
                    //Notification.show("Atención: La partida no está cuadrada, por favor verifique en el módulo de contabilidad. Código de Partida = " + codigoPartida, Notification.Type.WARNING_MESSAGE);
                    queryString = "UPDATE contabilidad_partida SET ";
                    if(rsRecords.getDouble("SUMADEBE") > rsRecords.getDouble("SUMAHABER")) {
                        queryString += " Debe = Debe - " + (rsRecords.getDouble("SUMADEBE") - rsRecords.getDouble("SUMAHABER")) + " ";
                    } else { /*HABER > DEBE*/
                        queryString += " Debe = Debe + " + (rsRecords.getDouble("SUMAHABER") - rsRecords.getDouble("SUMADEBE")) + " ";
                    }
                    queryString += " WHERE CodigoPartida = '" + codigoPartida + "'";
                    queryString += " AND IdPartida = " + maxIdPartida;
                    stQuery.executeUpdate(queryString);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void recibirParametrosImagen(String fileName, String mimeType, long peso) {
        String parametro1 = fileName;
        String parametro2 = mimeType;
        long parametro3 = peso;
    }

    private void ingresoManual() {

        Window window = new Window("Ingreso manual de RECIBO NO FEL");
        window.setWidth("25%");
        window.setHeight("40%");
        window.setModal(true);

        DateField fechaDt;
        ComboBox proveedorCbx = new ComboBox("Proveedor : " );
        TextField serieTxt = new TextField("Serie factura : ");
        TextField numeroTxt = new TextField("Número factura : ");
        NumberField montoTxt = new NumberField("Monto : ");
        NumberField exentoTxt;
        Label baseLbl = new Label();
        Label ivaLbl = new Label();

        baseLbl.addStyleName(ValoTheme.LABEL_SUCCESS);
        baseLbl.setValue("BASE = 0.00");
        ivaLbl.addStyleName(ValoTheme.LABEL_SUCCESS);
        ivaLbl.setValue("IVA = 0.00");

        serieTxt = new TextField();
        serieTxt.setWidth("100%");
        serieTxt.addStyleName("mayusculas");
        serieTxt.setInputPrompt("Serie del documento");
        serieTxt.setDescription("Serie del documento");

        numeroTxt = new TextField();
        numeroTxt.setWidth("100%");
        numeroTxt.addStyleName("mayusculas");
        numeroTxt.setInputPrompt("Número del documento");
        numeroTxt.setDescription("Número del documento");

        proveedorCbx = new ComboBox();
        proveedorCbx.setWidth("100%");
        proveedorCbx.setInputPrompt("Proveedor");
        proveedorCbx.setInvalidAllowed(false);
        proveedorCbx.setNewItemsAllowed(false);
        proveedorCbx.setFilteringMode(FilteringMode.CONTAINS);
        proveedorCbx.setInvalidAllowed(false);
        proveedorCbx.setNewItemsAllowed(false);
        proveedorCbx.setNullSelectionAllowed(false);
        proveedorCbx.addContainerProperty("nit", String.class, "");

        fechaDt = new DateField();
        fechaDt.setDateFormat("dd/MM/yyyy");
        fechaDt.setWidth("100%");
        fechaDt.setValue(new java.util.Date());

        montoTxt = new NumberField();
        montoTxt.setInputPrompt("Monto del documento");
        montoTxt.setDescription("Monto del documento");
        montoTxt.setDecimalAllowed(true);
        montoTxt.setDecimalPrecision(2);
        montoTxt.setMinimumFractionDigits(2);
        montoTxt.setDecimalSeparator('.');
        montoTxt.setDecimalSeparatorAlwaysShown(true);
        montoTxt.setValue(0d);
        montoTxt.setGroupingUsed(true);
        montoTxt.setGroupingSeparator(',');
        montoTxt.setGroupingSize(3);
        montoTxt.setImmediate(true);
        montoTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        montoTxt.setWidth("100%");
        NumberField finalMontoTxt = montoTxt;
//        montoTxt.addValueChangeListener(event -> {
//            if (finalMontoTxt.getDoubleValueDoNotThrow() <= 0) {
//                return;
//            }
//
//            if(!((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyRegimen().toUpperCase().equals("EXENTA")) {
//                baseLbl.setValue("BASE = " + Utileria.numberFormatMoney.format((finalMontoTxt.getDoubleValueDoNotThrow()) / 1.12));
//                ivaLbl.setValue("IVA = " + Utileria.numberFormatMoney.format(((finalMontoTxt.getDoubleValueDoNotThrow()) / 1.12) * .12));
//            }
//
//        });

        String queryString = " SELECT * ";
        queryString += " FROM  proveedor_empresa ";
        queryString += " WHERE IdEmpresa = " + empresaId;
        queryString += " ORDER BY Nombre ";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) {

                proveedorCbx.addItem(rsRecords.getString("IdProveedor"));
                proveedorCbx.setItemCaption(rsRecords.getString("IdProveedor"), rsRecords.getString("Nombre"));
                proveedorCbx.getContainerProperty(rsRecords.getString("IdProveedor"), "nit").setValue(rsRecords.getString("NIT"));
            }

        } catch (Exception ex1) {
            Notification.show("ERROR AL BUSCAR PROVEEDORES", Notification.Type.ERROR_MESSAGE);
            ex1.printStackTrace();
        }

        TextField finalSerieTxt = serieTxt;
        TextField finalNumeroTxt = numeroTxt;
        ComboBox finalProveedorCbx = proveedorCbx;
        NumberField finalMontoTxt1 = montoTxt;

        Button guardarBtn = new Button("Guardar");
        guardarBtn.setIcon(FontAwesome.SAVE);
        guardarBtn.setStyleName(ValoTheme.BUTTON_PRIMARY);
        guardarBtn.addClickListener((Button.ClickListener) event -> {
            Object itemId = facturasFelContainer.addItem();

            facturasFelContainer.getContainerProperty(itemId, "id").setValue(facturasFelContainer.size()+1);
            facturasFelContainer.getContainerProperty(itemId, "empresa").setValue(empresaId);
            facturasFelContainer.getContainerProperty(itemId, "fechaEmision").setValue(Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()));
            facturasFelContainer.getContainerProperty(itemId, "numeroAutorizacion").setValue("");
            facturasFelContainer.getContainerProperty(itemId, "tipoDte").setValue("RECIBO CONTABLE");
            facturasFelContainer.getContainerProperty(itemId, "serie").setValue(finalSerieTxt.getValue());
            facturasFelContainer.getContainerProperty(itemId, "numero").setValue(finalNumeroTxt.getValue());
            facturasFelContainer.getContainerProperty(itemId, "idProveedor").setValue(finalProveedorCbx.getValue());
            facturasFelContainer.getContainerProperty(itemId, "nitProveedor").setValue(finalProveedorCbx.getContainerProperty(finalProveedorCbx.getValue(), "nit").getValue());
            facturasFelContainer.getContainerProperty(itemId, "proveedor").setValue(finalProveedorCbx.getItemCaption(finalProveedorCbx.getValue()));
            facturasFelContainer.getContainerProperty(itemId, "idReceptor").setValue(((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyTaxId());
            facturasFelContainer.getContainerProperty(itemId, "receptor").setValue(((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName());
            facturasFelContainer.getContainerProperty(itemId, "moneda").setValue("QUETZALES");
            facturasFelContainer.getContainerProperty(itemId, "monto").setValue(numberFormat.format(finalMontoTxt1.getDoubleValueDoNotThrow()));
            facturasFelContainer.getContainerProperty(itemId, "iva").setValue("0.0");
            facturasFelContainer.getContainerProperty(itemId, "costo").setValue(numberFormat.format(finalMontoTxt1.getDoubleValueDoNotThrow()));
            facturasFelContainer.getContainerProperty(itemId, "estatus").setValue("ACTIVA");

            String queryString1 = " INSERT INTO documentos_fel_sat (";
            queryString1 += "IdEmpresa, FechaEmision, TipoDTE, Serie, Numero, IdProveedor, NitProveedor, NombreProveedor," +
                    " IdReceptor, NombreReceptor, Moneda, Monto, Estatus, IVA,Costo, CreadoUsuario, CreadoFechaYHora" +
                    ")";
            queryString1 += " VALUES ";
            queryString1 += " (";
            queryString1 += empresaId;
            queryString1 += ",'" + facturasFelGrid.getContainerDataSource().getContainerProperty(itemId, "fechaEmision").getValue() + "'";
            queryString1 += ",'" + facturasFelGrid.getContainerDataSource().getContainerProperty(itemId, "tipoDte").getValue() + "'";
            queryString1 += ",'" + facturasFelGrid.getContainerDataSource().getContainerProperty(itemId, "serie").getValue() + "'";
            queryString1 += ",'" + facturasFelGrid.getContainerDataSource().getContainerProperty(itemId, "numero").getValue() + "'";
            queryString1 += "," + facturasFelGrid.getContainerDataSource().getContainerProperty(itemId, "idProveedor").getValue();
            queryString1 += ",'" + facturasFelGrid.getContainerDataSource().getContainerProperty(itemId, "nitProveedor").getValue() + "'";
            queryString1 += ",'" + String.valueOf(facturasFelGrid.getContainerDataSource().getContainerProperty(itemId, "proveedor").getValue()).replaceAll("'", "") + "'";
            queryString1 += ",'" + facturasFelGrid.getContainerDataSource().getContainerProperty(itemId, "idReceptor").getValue() + "'";
            queryString1 += ",'" + String.valueOf(facturasFelGrid.getContainerDataSource().getContainerProperty(itemId, "receptor").getValue()).replaceAll("'", "") + "'";
            queryString1 += ",'" + facturasFelGrid.getContainerDataSource().getContainerProperty(itemId, "moneda").getValue() + "'";
            queryString1 += "," + String.valueOf(facturasFelGrid.getContainerDataSource().getContainerProperty(itemId, "monto").getValue()).replaceAll(",", "");
            queryString1 += ",'" + facturasFelGrid.getContainerDataSource().getContainerProperty(itemId, "estatus").getValue() + "'";
            queryString1 += "," + String.valueOf(facturasFelGrid.getContainerDataSource().getContainerProperty(itemId, "iva").getValue()).replaceAll(",", "");
            queryString1 += "," + String.valueOf(facturasFelGrid.getContainerDataSource().getContainerProperty(itemId, "costo").getValue()).replaceAll(",", "");
            queryString1 += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString1 += ",current_timestamp";
            queryString1 += ")";

//System.out.println(queryString);

            try {
                stPreparedQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().prepareStatement(queryString1, Statement.RETURN_GENERATED_KEYS);
                stPreparedQuery.executeUpdate();
                rsRecords = stPreparedQuery.getGeneratedKeys();
                rsRecords.next();

                int idDocumento = rsRecords.getInt(1);
                facturasFelContainer.getContainerProperty(itemId, "id").setValue(idDocumento);
                facturasFelGrid.select(itemId);  //llama a contabilizar()

//                contabilizar(itemId, idDocumento, "");

                Notification.show("RECIBO MANUAL ingresado correctamente.", Notification.Type.TRAY_NOTIFICATION);
                window.close();

            } catch (Exception ex1) {
                Notification.show("ERROR AL INSERTAR REGISTRO MANUAL DE RECIBO NO FEL", Notification.Type.ERROR_MESSAGE);
                ex1.printStackTrace();
            }

        });

        HorizontalLayout botonLayout = new HorizontalLayout();
        botonLayout.setResponsive(true);
        botonLayout.setSpacing(true);
        botonLayout.setMargin(true);
        botonLayout.setWidth("100%");

        botonLayout.addComponent(guardarBtn);
        botonLayout.setComponentAlignment(guardarBtn, Alignment.BOTTOM_CENTER);

        FormLayout form = new FormLayout();
        //form.addComponents(fechaDt, proveedorCbx, serieTxt, numeroTxt, montoTxt, baseLbl, ivaLbl, botonLayout);
        form.addComponents(fechaDt, proveedorCbx, serieTxt, numeroTxt, montoTxt, botonLayout);

        window.setContent(form);
        UI.getCurrent().addWindow(window);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - Importar COMPRAS FEL SAT");
    }
}