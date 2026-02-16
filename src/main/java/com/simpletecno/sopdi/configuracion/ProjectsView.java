/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.configuracion;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Sizeable;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vaadin.dialogs.ConfirmDialog;

/**
 *
 * @author JAguirre
 */
@SuppressWarnings("serial")
public class ProjectsView extends VerticalLayout implements View {
    
    public Statement stQuery = null;
    public ResultSet rsRecords = null;
    
    static final String CODIGO_PROPERTY = "Id";
    static final String LOGO_PROPERTY = "Logo";
    static final String NOMBRE_PROPERTY = "Nombre";
    static final String PAIS_PROPERTY = "Pais";
    static final String RESPONSABLE_PROPERTY   = "Responsable";
    static final String TELEFONO_PROPERTY = "Teléfono";
    static final String FECHA_INICIO_PROPERTY = "Fecha inicio";
    static final String FECHA_FIN_PROPERTY = "Fecha fin";
    static final String OPTIONS_PROPERTY = "-";
    static final String ESTATUS_PROPERTY = "Estatus";
    
    Utileria utileria = new Utileria();
    MarginInfo  marginInfo;

    Table empresaTable;
    Table projectsTable;
    
    Button exportExcelBtn;
        
    public static Locale locale = new Locale("ES","GT"); 
    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");
                
    UI mainUI;
       
    public ProjectsView() {
        this.mainUI = UI.getCurrent();    
        
        setSpacing(true);
        setMargin(true);
        setResponsive(true);
        marginInfo = new MarginInfo(true,true,false,true); 

        createReport();
        
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
//        buttonsLayout.setMargin(false);

        exportExcelBtn    = new Button("Excel");
        exportExcelBtn.setIcon(FontAwesome.FILE_EXCEL_O);
        exportExcelBtn.setWidth(120,Sizeable.UNITS_PIXELS);
        exportExcelBtn.setDescription("Exportar los datos a Excel");
        exportExcelBtn.addListener ( new Button.ClickListener()
        {
            @Override
            public void buttonClick ( Button.ClickEvent event )
            {
                if(projectsTable.size() > 0) {
                    exportToExcel(projectsTable);
                }
            }
        });
        
        buttonsLayout.addComponent(exportExcelBtn);

        addComponent(buttonsLayout);
        setComponentAlignment(buttonsLayout, Alignment.TOP_CENTER);
                
        fillProyectoTable();
    }
            
    private void createReport() {

        HorizontalLayout reportLayout = new HorizontalLayout();
        reportLayout.setWidth("95%");
        reportLayout.addStyleName("rcorners3");
        
        projectsTable = new Table("Listado de proyectos ");
        projectsTable.addStyleName(ValoTheme.TABLE_SMALL);
        projectsTable.addStyleName(ValoTheme.TABLE_COMPACT);
        projectsTable.setImmediate(true);
        projectsTable.setSelectable(true);

        reportLayout.addComponent(projectsTable);
        reportLayout.setComponentAlignment(projectsTable, Alignment.MIDDLE_CENTER);

        projectsTable.setWidth("100%");
        projectsTable.setPageLength(10);
        
        projectsTable.addContainerProperty(CODIGO_PROPERTY,    String.class, null);
        projectsTable.addContainerProperty(LOGO_PROPERTY,  Image.class, null);
        projectsTable.addContainerProperty(ESTATUS_PROPERTY,  String.class, null);
        projectsTable.addContainerProperty(NOMBRE_PROPERTY,    String.class, null);

        projectsTable.addContainerProperty(PAIS_PROPERTY, String.class, null);
        projectsTable.addContainerProperty(RESPONSABLE_PROPERTY,  String.class, null);
        projectsTable.addContainerProperty(TELEFONO_PROPERTY,  String.class, null);
        projectsTable.addContainerProperty(FECHA_INICIO_PROPERTY,  String.class, null);
        projectsTable.addContainerProperty(FECHA_FIN_PROPERTY,  String.class, null);
        projectsTable.addContainerProperty(OPTIONS_PROPERTY,    MenuBar.class, null);
        
        projectsTable.setColumnAlignments(new Table.Align[] { 
                Table.Align.CENTER, Table.Align.CENTER,Table.Align.CENTER, Table.Align.LEFT,
                Table.Align.LEFT,   Table.Align.LEFT,  Table.Align.LEFT,   Table.Align.LEFT, 
                Table.Align.LEFT,   Table.Align.CENTER
        });

        projectsTable.setFooterVisible(true);
        projectsTable.setColumnFooter(ESTATUS_PROPERTY, "Total");
        projectsTable.setColumnFooter(NOMBRE_PROPERTY, "0");
        
        addComponent(reportLayout);        
        setComponentAlignment(reportLayout, Alignment.MIDDLE_CENTER);
    }

    public void fillProyectoTable() {
        
        projectsTable.removeAllItems();
        projectsTable.setColumnFooter(NOMBRE_PROPERTY, "0");
                
        String queryString = "";
        
        queryString =  "Select Pro.*, Pai.Nombre PaisNombre ";
        queryString += " From  proyecto Pro";
        queryString += " Inner Join pais    Pai On Pai.IdPais    = Pro.IdPais";
        queryString += " Where Pro.IdProyecto > 0";
        queryString += " Order By Pro.Nombre";

//System.out.println("\n\n"+queryString);

        try {
            
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery (queryString);

            if(rsRecords.next()) { //  encontrado

                int primerRegistro = rsRecords.getInt("IdProyecto");
                 
                Image proyectoLogo;
                
                // Define a common menu command for all the menu items.
                MenuBar.Command mycommand = new MenuBar.Command() {
                    @Override
                    public void menuSelected(MenuBar.MenuItem selectedItem) {
                        if(projectsTable.getValue() != null) {
                            MenuBar menuBar = (MenuBar)projectsTable.getContainerProperty(projectsTable.getValue(), OPTIONS_PROPERTY).getValue();
                            if(menuBar.getItems().get(0).getChildren().contains(selectedItem)) {
//                                String msg = String.valueOf(selectedItem.getId()) + "  ";
//                                msg += projectsTable.getContainerProperty(projectsTable.getValue(), NOMBRE_PROPERTY).getValue();
//                                Notification.show(msg, Notification.Type.TRAY_NOTIFICATION);
                                
                                if(selectedItem.getId() == 3) { // editar
                                    ProjectForm formProyecto = new ProjectForm();
                                    formProyecto.idProyectoTxt.setReadOnly(false);
                                    formProyecto.idProyectoTxt.setValue(String.valueOf(projectsTable.getValue()));
                                    formProyecto.idProyectoTxt.setReadOnly(true);
                                    formProyecto.fillData();
                                    formProyecto.center();                        
                                    mainUI.addWindow(formProyecto);
                                }
                                if(selectedItem.getId() == 5) { // nuevo
                                    ProjectForm formProyecto = new ProjectForm();
                                    formProyecto.idProyectoTxt.setReadOnly(false);
                                    formProyecto.idProyectoTxt.setValue("0");
                                    formProyecto.idProyectoTxt.setReadOnly(true);
                                    mainUI.addWindow(formProyecto);
                                }
                                if(selectedItem.getId() == 7) { // eliminar
                                    ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de eliminar el registro?",
                                        "SI", "NO", new ConfirmDialog.Listener() {

                                        public void onClose(ConfirmDialog dialog) {
                                            if (dialog.isConfirmed()) {
                                                deleteProyecto();
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

                do {

                    String fechaFin = "";
                    
                    if(rsRecords.getObject("FechaFin") != null) {
                        fechaFin = utileria.getFecha(rsRecords.getDate("FechaFin"));
                    }

                    final byte docBytes[] = rsRecords.getBytes("Logo");
                    StreamResource logoStreamResource = null;

                    if(docBytes != null ) {
                        logoStreamResource = new StreamResource(
                            new StreamResource.StreamSource() {
                                public InputStream getStream() {
                                    return new ByteArrayInputStream(docBytes);
                                }
                            },rsRecords.getString("IdProyecto")
                        );
                    }

                    proyectoLogo = new Image(null, logoStreamResource);
                    proyectoLogo.setImmediate(true);
                    proyectoLogo.setWidth("35px");
                    proyectoLogo.setHeight("35px"); 

                    MenuBar projectMenu = new MenuBar();
                    projectMenu.setCaption("Menú");
                    projectMenu.addStyleName(ValoTheme.MENUBAR_BORDERLESS);
                    projectMenu.addStyleName(ValoTheme.MENUBAR_SMALL);
                    projectMenu.addStyleName(ValoTheme.MENU_APPEAR_ON_HOVER);
                    projectMenu.setSizeUndefined();
                    projectMenu.setData(rsRecords.getInt("IdProyecto"));
                    MenuBar.MenuItem menuItem = projectMenu.addItem("", FontAwesome.EDIT, null);
                    menuItem.addItem("Editar", FontAwesome.EYE, mycommand);                    
                    menuItem.addSeparator();
                    menuItem.addItem("Nuevo", FontAwesome.PLUS, mycommand);                    
                    menuItem.addSeparator();
                    menuItem.addItem("Eliminar", FontAwesome.TRASH, mycommand);

                    projectsTable.addItem(new Object[] {    
                        rsRecords.getString("IdProyecto"),
                        proyectoLogo,
                        rsRecords.getString("Estatus"),
                        rsRecords.getString("Nombre"),
                        rsRecords.getString("PaisNombre"),
                        rsRecords.getString("Responsable"),
                        rsRecords.getString("TelefonoResponsable"),
                        utileria.getFecha(rsRecords.getDate("FechaInicio")),
                        fechaFin,
                        projectMenu
                    }, rsRecords.getInt("IdProyecto"));

                }while(rsRecords.next());

                projectsTable.select(primerRegistro);
            }
            projectsTable.setColumnFooter(NOMBRE_PROPERTY, String.valueOf(projectsTable.size()));
        } 
        catch (Exception ex) {
            Logger.getLogger(ProjectsView.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de proyectos : " + ex.getMessage());
            Notification.show("Error al intentar leer registros de proyectos..!", Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }

    } 
        
    public boolean exportToExcel(Table tableToExport) {
        ExcelExport excelExport;

        excelExport = new ExcelExport(tableToExport);
        excelExport.excludeCollapsedColumns();
        excelExport.setExportFileName(tableToExport.getCaption() + ".xls");
        
        String mainTitle = "Sopdi - " + tableToExport.getCaption() + " AL: "  + new Utileria().getFechaYYYYMMDD_1(new Date());
  
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
    
    void setTableTitle(String tableTitle) {
        if(projectsTable != null) {
            projectsTable.setCaption(tableTitle);
            projectsTable.setDescription(tableTitle);
        }            
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Cian - Proyectos");
    }

    private void deleteProyecto() {        
        
        String queryString = "";

        queryString =  "Delete ";
        queryString += " From  cliente_nota ";
        queryString += " Where IdCliente = " + String.valueOf(projectsTable.getValue());

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();            
            stQuery.executeUpdate(queryString);

            queryString =  "Delete ";
            queryString += " From  proyecto ";
            queryString += " Where IdProyecto = " + String.valueOf(projectsTable.getValue());

            stQuery.executeUpdate(queryString);

            Notification.show("Operación exitosa!", Notification.Type.TRAY_NOTIFICATION);
            fillProyectoTable();
        } 
        catch (Exception ex) {
            Logger.getLogger(ProjectsView.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al ELIINAR registros de proyecto : " + ex.getMessage());
            Notification.show("Error al ELIMINAR registros de proyecto..!", Notification.Type.ERROR_MESSAGE);
        } 
    }
}