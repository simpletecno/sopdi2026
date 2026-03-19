/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.utilerias;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vaadin.dialogs.ConfirmDialog;

/**
 *
 * @author JAguirre
 */
@SuppressWarnings("serial")
public class Unities extends VerticalLayout {
    
    protected static final String UNIDAD_PROPERTY = "Código";
    protected static final String DESCRIPCION_PROPERTY = "Descripción";
    protected static final String ESTATUS_PROPERTY = "Estatus";
    protected static final String UNIDAD_MEDIDA_PROPERTY = "Unidad de medida";
    protected static final String MEDIDA_CUADRADA_PROPERTY = "Medida total";
    protected static final String FRENTE_PROPERTY = "Frente";
    protected static final String FONDO_PROPERTY = "Fondo";
    protected static final String UNIDADES_NOUTILIZADAS_PROPERTY = "Unidades no utilizables";
    protected static final String OPTIONS_PROPERTY = "-";

    MarginInfo  marginInfo;

    private Table unitiesTable;
    Button exportExcelBtn;

    public Statement stQuery = null;
    public ResultSet rsRecords = null;
     
    final public UI mainUI;
    
    public  Unities() {
    
        mainUI = UI.getCurrent();
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
                if(unitiesTable.size() > 0) {
//                    PronetWebPayMain.getInstance().mainWindow.getWindow().showNotification("EN CONSTRUCCION!");            
                    exportToExcel(unitiesTable);
                }
            }
        });
        
        buttonsLayout.addComponent(exportExcelBtn);

        addComponent(buttonsLayout);
        setComponentAlignment(buttonsLayout, Alignment.TOP_CENTER);
                
        fillUnitiesTable();
    }
    
    private void createReport() {

        HorizontalLayout reportLayout = new HorizontalLayout();
        reportLayout.setWidth("80%");
        reportLayout.addStyleName("rcorners3");

        unitiesTable = new Table("Listado de unidades de venta ");
        unitiesTable.addStyleName(ValoTheme.TABLE_SMALL);
        unitiesTable.addStyleName(ValoTheme.TABLE_COMPACT);
        unitiesTable.setImmediate(true);
        unitiesTable.setSelectable(true);

        reportLayout.addComponent(unitiesTable);
        reportLayout.setComponentAlignment(unitiesTable, Alignment.MIDDLE_CENTER);

        unitiesTable.setWidth("100%");
        unitiesTable.setPageLength(10);
        
        unitiesTable.addContainerProperty(UNIDAD_PROPERTY,           String.class, null);
        unitiesTable.addContainerProperty(DESCRIPCION_PROPERTY,      String.class, null);
        unitiesTable.addContainerProperty(ESTATUS_PROPERTY,          String.class, null);        
        unitiesTable.addContainerProperty(UNIDAD_MEDIDA_PROPERTY,    String.class, null);
        
        unitiesTable.addContainerProperty(MEDIDA_CUADRADA_PROPERTY,  String.class, null);
        unitiesTable.addContainerProperty(FRENTE_PROPERTY,           String.class, null);
        unitiesTable.addContainerProperty(FONDO_PROPERTY,            String.class, null);
        unitiesTable.addContainerProperty(UNIDADES_NOUTILIZADAS_PROPERTY, String.class, null);
        
        unitiesTable.addContainerProperty(OPTIONS_PROPERTY,       MenuBar.class, null);

        unitiesTable.setColumnAlignments(
                Table.Align.CENTER, Table.Align.LEFT,   Table.Align.LEFT,   Table.Align.LEFT,         
                Table.Align.CENTER, Table.Align.CENTER, Table.Align.CENTER, Table.Align.CENTER,
                Table.Align.CENTER
        );

        // Enable footer
        unitiesTable.setFooterVisible(true);
        // Add some total sum and description to footer
        unitiesTable.setColumnFooter(DESCRIPCION_PROPERTY, "Total");
        unitiesTable.setColumnFooter(ESTATUS_PROPERTY, "0");
        
        addComponent(reportLayout);        
        setComponentAlignment(reportLayout, Alignment.MIDDLE_CENTER);
    }
    
    public void fillUnitiesTable() {

        unitiesTable.removeAllItems();
        
        unitiesTable.setFooterVisible(false);
                
        String queryString = "";
        
        queryString =  " Select UV.*, Pro.UnidadMedida ";
        queryString += " From  unidad_base UV";
        queryString += " Inner Join proyecto Pro On Pro.IdProyecto = UV.IdProyecto";
        queryString += " Where UV.IdProyecto = " + ((SopdiUI) mainUI).sessionInformation.getStrProjectId();

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();            
            rsRecords = stQuery.executeQuery (queryString);

            // Define a common menu command for all the menu items.
            MenuBar.Command mycommand = new MenuBar.Command() {
                    @Override
                    public void menuSelected(MenuBar.MenuItem selectedItem) {
                        if(unitiesTable.getValue() != null) {
                            MenuBar menuBar = (MenuBar)unitiesTable.getContainerProperty(unitiesTable.getValue(), OPTIONS_PROPERTY).getValue();
                            if(menuBar.getItems().get(0).getChildren().contains(selectedItem)) {
                                String msg = String.valueOf(selectedItem.getId()) + "  ";
                                msg += unitiesTable.getContainerProperty(unitiesTable.getValue(), DESCRIPCION_PROPERTY).getValue();
                                Notification.show(msg, Notification.Type.TRAY_NOTIFICATION);
                                
                                if(selectedItem.getId() == 3) { // editar
                                    UnitieForm unitieForm = new UnitieForm();
                                    unitieForm.idUnitieTxt.setReadOnly(false);
                                    unitieForm.idUnitieTxt.setValue(String.valueOf(unitiesTable.getValue()));
                                    unitieForm.idUnitieTxt.setReadOnly(true);
                                    unitieForm.fillData();
                                    mainUI.addWindow(unitieForm);
                                }
                                if(selectedItem.getId() == 5) { // nuevo
                                    UnitieForm unitieForm = new UnitieForm();
                                    unitieForm.idUnitieTxt.setReadOnly(false);
                                    unitieForm.idUnitieTxt.setValue("0");
                                    unitieForm.idUnitieTxt.setReadOnly(true);
                                    mainUI.addWindow(unitieForm);
                                }
                                if(selectedItem.getId() == 7) { // eliminar
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
            
            if(rsRecords.first()) {
                do {

                    MenuBar unitieMenu = new MenuBar();
                    unitieMenu.setCaption("Menú");
                    unitieMenu.addStyleName(ValoTheme.MENUBAR_BORDERLESS);
                    unitieMenu.addStyleName(ValoTheme.MENUBAR_SMALL);
                    unitieMenu.addStyleName(ValoTheme.MENU_APPEAR_ON_HOVER);
                    unitieMenu.setSizeUndefined();
                    unitieMenu.setData(rsRecords.getInt("IdUnidadBase"));
                    MenuBar.MenuItem menuItem = unitieMenu.addItem("", FontAwesome.EDIT, null);
                    menuItem.addItem("Editar", FontAwesome.EDIT, mycommand);                    
                    menuItem.addSeparator();
                    menuItem.addItem("Nuevo", FontAwesome.PLUS, mycommand);
                    menuItem.addSeparator();
                    menuItem.addItem("Eliminar", FontAwesome.TRASH, mycommand);

                    unitiesTable.addItem(new Object[] {    
                        rsRecords.getString("Codigo"),
                        rsRecords.getString("Descripcion"),
                        rsRecords.getString("Estatus"),
                        rsRecords.getString("UnidadMedida"),
                        rsRecords.getString("MedidaCuadrada"),
                        rsRecords.getString("MedidaFrente"),
                        rsRecords.getString("MedidaFondo"),
                        rsRecords.getString("MedidaSinValor"),
                        unitieMenu
                    }, rsRecords.getInt("IdUnidadBase"));

                } while(rsRecords.next());

                unitiesTable.setFooterVisible(true);
                unitiesTable.setColumnFooter(DESCRIPCION_PROPERTY, "Total");
                unitiesTable.setColumnFooter(ESTATUS_PROPERTY, String.valueOf(unitiesTable.size()));
                rsRecords.first();
                unitiesTable.select(rsRecords.getInt("IdUnidadBase"));
            }
            else {
                UnitieForm unitieForm = new UnitieForm();
                unitieForm.idUnitieTxt.setReadOnly(false);
                unitieForm.idUnitieTxt.setValue("0");
                unitieForm.idUnitieTxt.setReadOnly(true);
                mainUI.addWindow(unitieForm);

//                Notification.show("No ha creado ninguna unidad de venta para este proyecto,  por favor ingrese una unidad de venta.", Notification.Type.WARNING_MESSAGE);
            }
                
        } 
        catch (Exception ex) {
            Logger.getLogger(Unities.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de UNIDADES DE VENTA : " + ex.getMessage());
            Notification.show("Error al intentar leer registros UNIDADES DE VENTA..!", Notification.Type.ERROR_MESSAGE);
        }

    } 

    public boolean exportToExcel(Table tableToExport) {
        ExcelExport excelExport;

        excelExport = new ExcelExport(tableToExport);
        excelExport.excludeCollapsedColumns();
        excelExport.setExportFileName(tableToExport.getCaption() + ".xls");
        
        String mainTitle = "CIAN - " + tableToExport.getCaption() + " AL: "  + new Utileria().getFechaYYYYMMDD_1(new Date());
  
        excelExport.setReportTitle(mainTitle);

        excelExport.export();
        
        return true;

    }
}