/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.configuracion;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.data.Property;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Sizeable;
import com.vaadin.server.StreamResource;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.MultiFileUpload;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadFinishedHandler;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadStateWindow;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author JAguirre
 */
@SuppressWarnings("serial")
public class PresupuestoView extends VerticalLayout implements View {
    
    public Statement stQuery = null;
    public ResultSet rsRecords  = null;
    public ResultSet rsRecords1 = null;
    
    protected static final String CODIGO_PROPERTY    = "Id";
    protected static final String FECHA_PROPERTY    = "Fecha";
    protected static final String CUENTA_PROPERTY     = "Cuenta";
    protected static final String DESCRIPCION_PROPERTY  = "Descripcion";
    protected static final String QUETZALES_PROPERTY    = "Quetzales";
    protected static final String DOLARES_PROPERTY = "Dolares";
    protected static final String TIPOCAMBIO_PROPERTY   = "T_Cambio";
    protected static final String MES_PROPERTY   = "Mes";
    protected static final String ID_EMPRESA_PROPERTY   = "IdEmpresa";
    protected static final String EMPRESA_PROPERTY   = "Empresa";
    protected static final String TIPO_PROPERTY   = "Tipo";
    protected static final String FECHA_AUTORIZADO_PROPERTY = "F.Autorizado";

    Button nextBtn;
    Button prevBtn;
    List<String> empresaLst;
    ListIterator<String> listIterator;
    String empresa;
    Label empresaLbl;

    CheckBox agruparCuentaChb;
    CheckBox agruparMesChb;
    Button newBtn;
    Button editBtn;
    Button exportExcelBtn;

    MultiFileUpload singleUpload;
    public File planillaFile;
    public XSSFWorkbook workbook;
    public XSSFSheet sheet, sheet2;
    private FileInputStream fileInputStream;
    
    TextField nombreTxt;
    public Table presupuestoTable;
            
    final UI mainUI = UI.getCurrent();
       
    public PresupuestoView() {
        
        setResponsive(true);
        MarginInfo marginInfo = new MarginInfo(true,true,false,true); 

        Label titleLbl = new Label("PRESUPUESTO");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h1_custom");

        empresaLbl = new Label("");
        empresaLbl.setWidth("340px");
        empresaLbl.addStyleName(ValoTheme.LABEL_H2);

        final int EPREV = 0;
        final int ENEXT = 1;

        empresaLst = new ArrayList<String>();

        llenarComboEmpresa();
        listIterator = empresaLst.listIterator();

        prevBtn = new Button("Anterior");
        prevBtn.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        prevBtn.addStyleName(ValoTheme.BUTTON_LARGE);
        prevBtn.addStyleName("flechas");
        prevBtn.setIcon(FontAwesome.ARROW_LEFT);
        prevBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (listIterator.hasPrevious()) {
                    prevBtn.setEnabled(true);
                    nextBtn.setEnabled(true);
                    empresaLbl.setValue(listIterator.previous());
                    empresa = empresaLbl.getValue().substring(1, 3);
                    fillReportTable(empresa);
                } else {
                    prevBtn.setEnabled(false);
                }
            }
        });

        nextBtn = new Button("Siguiente");
        nextBtn.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        nextBtn.addStyleName(ValoTheme.BUTTON_LARGE);
        nextBtn.addStyleName("flechas");
        nextBtn.setIcon(FontAwesome.ARROW_RIGHT);
        nextBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (listIterator.hasNext()) {
                    prevBtn.setEnabled(true);
                    nextBtn.setEnabled(true);

                    empresaLbl.setValue(listIterator.next());
                    empresa = empresaLbl.getValue().substring(1, 3);

                    fillReportTable(empresa);
                } else {
                    nextBtn.setEnabled(false);
                }
            }
        });

        HorizontalLayout empresaLayout = new HorizontalLayout();
        empresaLayout.setSizeUndefined();
        empresaLayout.setResponsive(true);
        empresaLayout.setSpacing(true);
        empresaLayout.addStyleName("rcorners4");
        empresaLayout.addComponents(empresaLbl, prevBtn, nextBtn);
        empresaLayout.setComponentAlignment(empresaLbl, Alignment.MIDDLE_LEFT);
        empresaLayout.setComponentAlignment(prevBtn, Alignment.MIDDLE_LEFT);
        empresaLayout.setComponentAlignment(nextBtn, Alignment.MIDDLE_LEFT);

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setResponsive(true);
        titleLayout.setWidth("100%");
        titleLayout.setMargin(false);
        titleLayout.addComponents(empresaLayout, titleLbl);
        titleLayout.setComponentAlignment(empresaLayout, Alignment.MIDDLE_LEFT);
        titleLayout.setComponentAlignment(titleLbl, Alignment.TOP_RIGHT);

        addComponent(titleLayout);
        setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        createReportTable();
            
        agruparCuentaChb = new CheckBox("Agrupar por cuenta");
        agruparCuentaChb.setValue(false);
        agruparCuentaChb.setIcon(FontAwesome.GROUP);
        agruparCuentaChb.addStyleName(ValoTheme.CHECKBOX_SMALL);
        agruparCuentaChb.addValueChangeListener(event ->
                        fillReportTable(empresa));
        
        agruparMesChb = new CheckBox("Agrupar por mes");
        agruparMesChb.setValue(false);
        agruparMesChb.setIcon(FontAwesome.CALENDAR);
        agruparMesChb.addStyleName(ValoTheme.CHECKBOX_SMALL);
        agruparCuentaChb.addValueChangeListener(event ->
                        fillReportTable(empresa));

        newBtn    = new Button("Nuevo rubro");
        newBtn.setIcon(FontAwesome.PLUS_CIRCLE);
        newBtn.setWidth(130,Sizeable.UNITS_PIXELS);
//        newBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        newBtn.setDescription("Registrar nuevo rubro de presupuesto");
        newBtn.addListener ( new Button.ClickListener()
        {
            @Override
            public void buttonClick ( Button.ClickEvent event )
            {
                PresupuestoForm presupuestoForm = new PresupuestoForm();
                presupuestoForm.presupuestoId = 0;
                presupuestoForm.empresa = empresa;
                presupuestoForm.empresaNombre = empresaLbl.getValue();
                presupuestoForm.fechaDt.focus();
                UI.getCurrent().addWindow(presupuestoForm);
            }
        });

        editBtn    = new Button("Editar rubro");
        editBtn.setIcon(FontAwesome.PLUS_CIRCLE);
        editBtn.setWidth(130,Sizeable.UNITS_PIXELS);
//        editBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        editBtn.setDescription("Editar rubro de presupuesto");
        editBtn.addListener ( new Button.ClickListener()
        {
            @Override
            public void buttonClick ( Button.ClickEvent event )
            {
                if(presupuestoTable.isEmpty() == false) {
                    if(presupuestoTable.getValue() != null) {
                        PresupuestoForm presupuestoForm = new PresupuestoForm();
                        presupuestoForm.presupuestoId = Integer.valueOf(String.valueOf(presupuestoTable.getValue()));
                        presupuestoForm.empresa = empresa;
                        presupuestoForm.empresaNombre = empresaLbl.getValue();
                        presupuestoForm.fillData();
                        presupuestoForm.fechaDt.focus();
                        UI.getCurrent().addWindow(presupuestoForm);
                    }
                }
            }
        });

        exportExcelBtn    = new Button("Exportar a Excel");
        exportExcelBtn.setIcon(FontAwesome.FILE_EXCEL_O);
//        exportExcelBtn.setWidth(130,Sizeable.UNITS_PIXELS);
        exportExcelBtn.addStyleName(ValoTheme.BUTTON_LINK);
        exportExcelBtn.addListener ( new Button.ClickListener()
        {
            @Override
            public void buttonClick ( Button.ClickEvent event )
            {
                if(presupuestoTable.size() > 0) {
//                    PronetWebPayMain.getInstance().mainWindow.getWindow().showNotification("EN CONSTRUCCION!");            
                    exportToExcel();
                }
            }
        });

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

                cargarPresupuesto(targetFile);

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
//        singleUpload.setCaption("Cargar archivo");
//        singleUpload2.setRootDirectory(newDoc.getAbsolutePath());
//        singleUpload.setPanelCaption("Buscar");
        singleUpload.setIcon(FontAwesome.UPLOAD);
        singleUpload.setImmediate(true);
        singleUpload.getSmartUpload().setUploadButtonCaptions("CARGAR PRESUPUESTO INICIAL", "");
        singleUpload.getSmartUpload().addStyleName(ValoTheme.BUTTON_LINK);
        singleUpload.getSmartUpload().addStyleName(".v-button { text-decoration : underline;}");

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

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setMargin(true);
        buttonsLayout.addComponent(agruparCuentaChb);
//        buttonsLayout.addComponent(agruparMesChb);
        buttonsLayout.addComponent(newBtn);
        buttonsLayout.addComponent(editBtn);
        buttonsLayout.addComponent(singleUpload);
        buttonsLayout.addComponent(exportExcelBtn);
        buttonsLayout.setComponentAlignment(agruparCuentaChb, Alignment.BOTTOM_LEFT);
//        buttonsLayout.setComponentAlignment(agruparMesChb, Alignment.BOTTOM_LEFT);
        buttonsLayout.setComponentAlignment(newBtn, Alignment.BOTTOM_LEFT);
        buttonsLayout.setComponentAlignment(editBtn, Alignment.BOTTOM_LEFT);
        buttonsLayout.setComponentAlignment(singleUpload, Alignment.BOTTOM_CENTER);
        buttonsLayout.setComponentAlignment(exportExcelBtn, Alignment.BOTTOM_RIGHT);

        addComponent(buttonsLayout);
        setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);

        String empresaString = String.valueOf(listIterator.next());

        empresaLbl.setValue(empresaString);

        empresa = empresaString.substring(1, 3);

        if (presupuestoTable != null) {
            empresa = empresaString.substring(1, 3);
            fillReportTable(empresa);
        }
    }
            
    public void llenarComboEmpresa() {
        String queryString = " SELECT * from contabilidad_empresa";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) { //  encontrado                
                empresaLst.add("(" + rsRecords.getString("IdEmpresa") + ") " + rsRecords.getString("Empresa"));
            }
            rsRecords.first();
            empresaLbl.setValue("(" + rsRecords.getString("IdEmpresa") + ") " + rsRecords.getString("Empresa"));

        } catch (Exception ex1) {
            System.out.println("Error al llenar Combo empresas: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void createReportTable() {

        HorizontalLayout reportLayout = new HorizontalLayout();
        reportLayout.setWidth("95%");
        reportLayout.addStyleName("rcorners3");
        reportLayout.setResponsive(true);
        reportLayout.setMargin(true);

        presupuestoTable = new Table("Tabla de presupuesto "){

            @Override
            protected String formatPropertyValue(Object rowId, Object colId,
                    Property property) {
                Object v = property.getValue();
                if (v instanceof Date) {
                    Date dateValue = (Date) v;
                    if(v.equals(MES_PROPERTY)) {
                        return new SimpleDateFormat("MMM-yyyy").format(dateValue);
                    }
                    else {
                        return new SimpleDateFormat("dd-MMM-yyyy").format(dateValue);                        
                    }
                }
                return super.formatPropertyValue(rowId, colId, property);
            }

        };
        
        reportLayout.addComponent(presupuestoTable);
        reportLayout.setComponentAlignment(presupuestoTable, Alignment.MIDDLE_CENTER);

        presupuestoTable.setWidth("100%");
        presupuestoTable.setResponsive(true);
        presupuestoTable.setPageLength(10);
        
        presupuestoTable.setImmediate(true);
        presupuestoTable.setSelectable(true);
/*        
    protected static final String CODIGO_PROPERTY    = "Id";
    protected static final String FECHA_PROPERTY    = "Fecha";
    protected static final String CUENTA_PROPERTY     = "Cuenta";
    protected static final String DESCRIPCION_PROPERTY  = "Descripcion";
    protected static final String QUETZALES_PROPERTY    = "Quetzales";
    protected static final String DOLARES_PROPERTY = "Dolares";
    protected static final String TIPOCAMBIO_PROPERTY   = "T_Cambio";
    protected static final String MES_PROPERTY   = "Mes";
    protected static final String ID_EMPRESA_PROPERTY   = "IdEmpresa";
    protected static final String EMPRESA_PROPERTY   = "Empresa";
    protected static final String TIPO_PROPERTY   = "Tipo";
    protected static final String FECHA_AUTORIZADO_PROPERTY = "F.Autorizado";
*/
        presupuestoTable.addContainerProperty(CODIGO_PROPERTY,          String.class, null);
        presupuestoTable.addContainerProperty(FECHA_PROPERTY,           Date.class, null);
        presupuestoTable.addContainerProperty(CUENTA_PROPERTY,          String.class, null);
        
        presupuestoTable.addContainerProperty(DESCRIPCION_PROPERTY,     String.class, null);
        presupuestoTable.addContainerProperty(QUETZALES_PROPERTY,       Double.class, null);
        presupuestoTable.addContainerProperty(DOLARES_PROPERTY,         Double.class, null);
        
        presupuestoTable.addContainerProperty(TIPOCAMBIO_PROPERTY,      Double.class, null);
        presupuestoTable.addContainerProperty(MES_PROPERTY,             Date.class, null);
        presupuestoTable.addContainerProperty(ID_EMPRESA_PROPERTY,      Integer.class, null);
        
        presupuestoTable.addContainerProperty(EMPRESA_PROPERTY,         String.class, null);        
        presupuestoTable.addContainerProperty(TIPO_PROPERTY,            String.class, null);
        presupuestoTable.addContainerProperty(FECHA_AUTORIZADO_PROPERTY,Date.class, null);

        presupuestoTable.setColumnAlignments(new Table.Align[] { 
                Table.Align.CENTER, Table.Align.LEFT,   Table.Align.LEFT,
                Table.Align.LEFT,   Table.Align.RIGHT,  Table.Align.RIGHT,
                Table.Align.CENTER, Table.Align.LEFT,   Table.Align.CENTER,
                Table.Align.LEFT,   Table.Align.CENTER, Table.Align.LEFT
        });
        
        presupuestoTable.setColumnWidth(CODIGO_PROPERTY, 0);

        addComponent(reportLayout);        
        setComponentAlignment(reportLayout, Alignment.MIDDLE_CENTER);
    }

    public void fillReportTable(String empresa) {
        
        presupuestoTable.removeAllItems();        
        presupuestoTable.setFooterVisible(false);
                
        String queryString = "";
        
        queryString =  "Select * ";
        queryString += " From  presupuesto";
        queryString += " Where IdEmpresa = " + empresa;
        if(agruparCuentaChb.getValue()) {
            if(agruparMesChb.getValue()) {
                queryString += " Order By Cuenta, Mes";
            }
            else {
                queryString += " Order By Cuenta";
            }
        }
        else {
            if(agruparMesChb.getValue()) {
                queryString += " Order By Mes";
            }
        }

//System.out.println("\n\n"+queryString);

        try {

            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords = stQuery.executeQuery (queryString);

            if(rsRecords.next()) { //  encontrado
/*
                // Define a common menu command for all the menu items.
                MenuBar.Command mycommand = new MenuBar.Command() {
                    @Override
                    public void menuSelected(MenuBar.MenuItem selectedItem) {
                        if(presupuestoTable.getValue() != null) {
                            MenuBar menuBar = (MenuBar)presupuestoTable.getContainerProperty(presupuestoTable.getValue(), OPTIONS_PROPERTY).getValue();
                            if(menuBar.getItems().get(0).getChildren().contains(selectedItem)) {
                                String msg = String.valueOf(selectedItem.getId()) + "  ";
                                msg += presupuestoTable.getContainerProperty(presupuestoTable.getValue(), NOMBRE_PROPERTY).getValue();
                                Notification.show(msg, Notification.Type.TRAY_NOTIFICATION);
                                
                                if(selectedItem.getId() == 3) { // editar
                                    PresupuestoForm presupuestoForm = new PresupuestoForm();
                                    presupuestoForm.idCliente = Integer.valueOf(String.valueOf(presupuestoTable.getValue()));
                                    presupuestoForm.fillUserData();
                                    presupuestoForm.nombreTxt.focus();
                                    UI.getCurrent().addWindow(presupuestoForm);
                                }
                                if(selectedItem.getId() == 5) { // eliminar
                                    ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de eliminar el registro?",
                                        "SI", "NO", new ConfirmDialog.Listener() {

                                        public void onClose(ConfirmDialog dialog) {
                                            if (dialog.isConfirmed()) {
                                                Notification.show("NO DISPONIBLE EN ESTA VERSION!", Notification.Type.WARNING_MESSAGE);
                                            }
                                        }
                                    });
                                }
                            }
                            else {
                                Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
                            }
                        }
                    }  
                };
  */                              
                SimpleDateFormat df  = new SimpleDateFormat("dd/MM/yyyy");
                SimpleDateFormat df2 = new SimpleDateFormat("MMM/yyyy");
                
                String cuentaActual = rsRecords.getString("Cuenta");
                java.util.Date mesActual = rsRecords.getDate("Mes");
                
                BigDecimal totalCuentaQuetzales = new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP);
                BigDecimal totalCuentaDolares   = new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP);
                BigDecimal totalMesQuetzales = new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP);
                BigDecimal totalMesDolares   = new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP);
                BigDecimal granTotalQuetzales = new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP);
                BigDecimal granTotalDolares   = new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP);

                do {
/*
                    MenuBar contactMenu = new MenuBar();
                    contactMenu.setCaption("Menú");
                    contactMenu.addStyleName(ValoTheme.MENUBAR_BORDERLESS);
                    contactMenu.addStyleName(ValoTheme.MENUBAR_SMALL);
                    contactMenu.addStyleName(ValoTheme.MENU_APPEAR_ON_HOVER);
                    contactMenu.setSizeUndefined();
                    contactMenu.setData(rsRecords.getInt("IdCliente"));
                    MenuBar.MenuItem menuItem = contactMenu.addItem("", FontAwesome.EDIT, null);
                    menuItem.addItem("Editar", FontAwesome.EYE, mycommand);                    
                    menuItem.addSeparator();
                    menuItem.addItem("Eliminar", FontAwesome.TRASH, mycommand);
 */                  

                    if(agruparCuentaChb.getValue()) {
                        if(!cuentaActual.equals(rsRecords.getString("Cuenta"))) {
                            presupuestoTable.addItem(new Object[] {    
                                "",
                                null,
                                "",
                                "SUB TOTALES",
                                totalCuentaQuetzales.doubleValue(),
                                totalCuentaDolares.doubleValue(),
                                0.00,
                                null,
                                0,
                                "",
                                "",
                                null
                            }, presupuestoTable.size() + 1);
                            
                            totalCuentaQuetzales = totalCuentaQuetzales.subtract(totalCuentaQuetzales);
                            totalCuentaDolares = totalCuentaDolares.subtract(totalCuentaDolares);
                            
                            cuentaActual = rsRecords.getString("Cuenta");
                                    
                        }
                    }
                    
                    presupuestoTable.addItem(new Object[] {    
                        rsRecords.getString("IdPresupuesto"),
                        rsRecords.getDate("Fecha"),
                        rsRecords.getString("Cuenta"),
                        rsRecords.getString("Descripcion"),
                        rsRecords.getDouble("MontoQuetzales"),
                        rsRecords.getDouble("MontoDolares"),
                        rsRecords.getDouble("TipoCambio"),
                        rsRecords.getDate("Mes"),
                        rsRecords.getInt("IdEmpresa"),
                        rsRecords.getString("Empresa"),
                        rsRecords.getString("Tipo"),
                        rsRecords.getDate("FechaAutorizado")
                    }, presupuestoTable.size() + 1);

                    totalCuentaQuetzales = totalCuentaQuetzales.add(new BigDecimal(rsRecords.getDouble("MontoQuetzales")).setScale(2, BigDecimal.ROUND_HALF_UP));
                    totalCuentaDolares = totalCuentaDolares.add(new BigDecimal(rsRecords.getDouble("MontoDolares")).setScale(2, BigDecimal.ROUND_HALF_UP));

                }while(rsRecords.next());

                if(agruparCuentaChb.getValue()) {
                    presupuestoTable.addItem(new Object[] {    
                        "",
                        null,
                        "",
                        "SUB TOTALES",
                        totalCuentaQuetzales.doubleValue(),
                        totalCuentaDolares.doubleValue(),
                        0.00,
                        null,
                        0,
                        "",
                        "",
                        null
                    }, presupuestoTable.size() + 1);
                }
                
                if(rsRecords.first()) {
                    presupuestoTable.select(rsRecords.getInt("IdPresupuesto"));
                }
            }
        } 
        catch (Exception ex) {
            Logger.getLogger(PresupuestoView.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de clientes : " + ex.getMessage());
            Notification.show("Error al intentar leer registros clientes..!", Notification.Type.ERROR_MESSAGE);
        }

    } 
        
    public boolean exportToExcel() {
        ExcelExport excelExport;

        excelExport = new ExcelExport(presupuestoTable);
        excelExport.excludeCollapsedColumns();
        excelExport.setExportFileName("SOPDI_Presupuesto_" + empresaLbl.getValue().replaceAll(" ", "_").replaceAll(",", "_").replaceAll("[()]", "").replaceAll("[.]", "").replaceAll("ñ", "n").replaceAll("Ñ", "N").replaceAll("ó", "o").replaceAll("é","") + "_" +  new Utileria().getFechaHoraSinFormato(new Date()) + ".xls");
        
        String mainTitle = "SOPDI - PRESUPUESTO DE " + empresaLbl.getCaption() + " AL: "  + new Utileria().getFechaYYYYMMDD_1(new Date());
  
        excelExport.setReportTitle(mainTitle);

        excelExport.export();
        
        return true;

    }

    /**
     * This class creates a streamresource. This class implements
     * the StreamSource interface which defines the getStream method.
     */
    public static class ShowExcelFile implements StreamResource.StreamSource {

        private final ByteArrayOutputStream os = new ByteArrayOutputStream();

        public ShowExcelFile(File fileToOpen) {
            try {           
               
                FileOutputStream fost = new FileOutputStream(fileToOpen);

            } catch (Exception e) {
                e.printStackTrace();
            }
            
        }

        @Override
        public InputStream getStream() {
            // Here we return the pdf contents as a byte-array
            return new ByteArrayInputStream(os.toByteArray());
        }    
    }    

    private void cargarPresupuesto(File planillaFile) {

        presupuestoTable.removeAllItems();
        
//        presupuestoTable.setColumnFooter("Debe", "Q.0.00");
//        presupuestoTable.setColumnFooter("Haber", "Q.0.00");

        singleUpload.setEnabled(false);

        int linea = 0;
            
        try {

            fileInputStream = new FileInputStream(planillaFile);

            workbook = new XSSFWorkbook(fileInputStream);

            sheet = workbook.getSheetAt(0);

System.out.println("\n Total lineas en archivo=" + String.valueOf(sheet.getLastRowNum()));
System.out.println("...INICIO...");
            
            ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().setAutoCommit(false);
            
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            
            String queryString = " Delete from presupuesto Where IdEmpresa = " + empresa;
            stQuery.executeUpdate(queryString);
            
            for (linea = 1; linea <= sheet.getLastRowNum(); linea++) {

                if (sheet.getRow(linea).getCell(0, MissingCellPolicy.CREATE_NULL_AS_BLANK).getRawValue().isEmpty()) {
                    break;
                }
  
//                if (!sheet.getRow(linea).getCell(7).getRawValue().equals(empresa)) {
//                    ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().rollback();
//                    Notification.show("No es posible cargar el documento. Por favor revisar que la empresa del"
//                            + " programa coincida con la del archivo, por favor revise linea : " + String.valueOf(linea), Notification.Type.ERROR_MESSAGE);
//                    break;
//                }
                                
                queryString = "Insert Into presupuesto (Fecha, Cuenta, Descripcion, MontoQuetzales, MontoDolares, ";
                queryString += " TipoCambio, Mes, IdEmpresa, Empresa, Tipo, FechaAutorizado) ";
                queryString += " Values (";
                queryString += " '" + Utileria.getFechaYYYYMMDD_1(sheet.getRow(linea).getCell(0).getDateCellValue()) + "'";
                queryString += ",'" + sheet.getRow(linea).getCell(1).getStringCellValue() + "'";
                queryString += ",'" + sheet.getRow(linea).getCell(2).getStringCellValue() + "'";
                queryString += ", " + String.valueOf(sheet.getRow(linea).getCell(3).getRawValue());
                queryString += ", " + String.valueOf(sheet.getRow(linea).getCell(4).getRawValue());
                queryString += ", " + String.valueOf(sheet.getRow(linea).getCell(5).getRawValue());
                queryString += ",'" + Utileria.getFechaYYYYMMDD_1(sheet.getRow(linea).getCell(6).getDateCellValue()) + "'";
                queryString += ", " + String.valueOf(sheet.getRow(linea).getCell(7).getRawValue());
                queryString += ",'" + sheet.getRow(linea).getCell(8).getStringCellValue() + "'";
                queryString += ",'" + sheet.getRow(linea).getCell(9).getStringCellValue() + "'";
                queryString += ",'" + Utileria.getFechaYYYYMMDD_1(sheet.getRow(linea).getCell(10).getDateCellValue()) + "'";
                queryString += ")";
                
System.out.println("(" + String.valueOf(linea) + ") " + queryString);

                stQuery.executeUpdate(queryString);
/**                
                presupuestoTable.addItem(new Object[]{
  
                    sheet.getRow(linea).getCell(0).getDateCellValue(), //fecha
                    String.valueOf(sheet.getRow(linea).getCell(1).getRawValue()),//cuenta
                    String.valueOf(sheet.getRow(linea).getCell(2).getRawValue()),//descripcion
                    sheet.getRow(linea).getCell(3).getNumericCellValue(),//quetzales
                    sheet.getRow(linea).getCell(4).getNumericCellValue(),//dolares
                    sheet.getRow(linea).getCell(5).getNumericCellValue(),//tipocambio
                    sheet.getRow(linea).getCell(6).getDateCellValue(),//mes
                    sheet.getRow(linea).getCell(7).getNumericCellValue(),//idempresa
                    String.valueOf(sheet.getRow(linea).getCell(8).getRawValue()),//empresa
                    String.valueOf(sheet.getRow(linea).getCell(9).getRawValue()),//tipo
                    sheet.getRow(linea).getCell(10).getDateCellValue(), //fechaautorizado

                }, presupuestoTable.size() + 1);
**/
//                totalDebe = totalDebe.add(new BigDecimal(sheet.getRow(linea).getCell(7).getNumericCellValue())).setScale(2, BigDecimal.ROUND_HALF_UP);
//                totalHaber = totalHaber.add(new BigDecimal(sheet.getRow(linea).getCell(8).getNumericCellValue())).setScale(2, BigDecimal.ROUND_HALF_UP);

//System.out.println("IDEX="+String.valueOf(Double.valueOf(sheet.getRow(linea).getCell(1).getNumericCellValue()).intValue()));
            } //endfor

//            presupuestoTable.setColumnFooter("Debe", "Q." + numberFormat.format(totalDebe.doubleValue()));
//            presupuestoTable.setColumnFooter("Haber", "Q." + numberFormat.format(totalHaber.doubleValue()));

            ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().commit();
            
            fillReportTable(empresa);
            
System.out.println("...FIN...");

        } catch (Exception ex1) {
            try {
                ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().rollback();
            } catch (SQLException ex) {
                Logger.getLogger(PresupuestoView.class.getName()).log(Level.SEVERE, null, ex);
            }
            new Notification("Error al intentar cargar el archivo EXCEL. Linea = " + String.valueOf(linea),
                    ex1.getMessage(),
                    Notification.Type.ERROR_MESSAGE)
                    .show(Page.getCurrent());
            ex1.printStackTrace();
        }
        singleUpload.setEnabled(true);
    }

    void setTableTitle(String tableTitle) {
        if(presupuestoTable != null) {
            presupuestoTable.setCaption(tableTitle);
            presupuestoTable.setDescription(tableTitle);
        }            
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - PRESUPUESTO");
    }
}