/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.operativo;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.addon.tableexport.DefaultTableHolder;
import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.addon.tableexport.TableHolder;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.SelectionEvent;
import com.vaadin.event.SelectionEvent.SelectionListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.HeaderCell;
import com.vaadin.ui.Grid.HeaderRow;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JAguirre
 */
@SuppressWarnings("serial")
public class IntegracionActual extends VerticalLayout {
    
    public Statement stQuery = null;
    public Statement stQuery1 = null;
    public ResultSet rsRecords  = null;
    public ResultSet rsRecords1 = null;
    
    protected static final String CODIGO_PROPERTY    = "Id";
    protected static final String CUENTA_PROPERTY     = "Cuenta";
    protected static final String CCOSTO_PROPERTY     = "CCosto";
    protected static final String DESCRIPCION_PROPERTY  = "Descripción";
    protected static final String CANTIDAD_PROPERTY    = "Cantidad";
    protected static final String PRECIO_PROPERTY    = "Precio";
    protected static final String TOTAL_PROPERTY    = "Total";
    protected static final String MONEDA_PROPERTY    = "Moneda";
    protected static final String PROJECT_PROPERTY = "Project";
    protected static final String LOTE_PROPERTY = "Lote";
    protected static final String PROVEEDOR_PROPERTY   = "Proveedor";
    protected static final String IDEX_PROPERTY   = "IDEX";

    String empresa, empresaNombre;

    Button exportExcelBtn;

    IndexedContainer integracionContainer = new IndexedContainer();
    Grid integracionGrid;

    Grid.FooterRow documentosFooter;
            
    final UI mainUI = UI.getCurrent();

    static DecimalFormat moneyFormat = new DecimalFormat("#,###,##0.00");
    static DecimalFormat integerFormat2 = new DecimalFormat("##,##0");
       
    public IntegracionActual() {
        
        MarginInfo marginInfo = new MarginInfo(true,true,false,true); 

        createReportTable();
            
        exportExcelBtn    = new Button("Exportar a Excel");
        exportExcelBtn.setIcon(FontAwesome.FILE_EXCEL_O);
//        exportExcelBtn.setWidth(130,Sizeable.UNITS_PIXELS);
        exportExcelBtn.addStyleName(ValoTheme.BUTTON_LINK);
        exportExcelBtn.addListener ( new Button.ClickListener()
        {
            @Override
            public void buttonClick ( Button.ClickEvent event )
            {
                if(integracionContainer.size() > 0) {
                    exportToExcel();
                }
            }
        });
        
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setMargin(true);
        buttonsLayout.addComponent(exportExcelBtn);
        buttonsLayout.setComponentAlignment(exportExcelBtn, Alignment.BOTTOM_RIGHT);

        addComponent(buttonsLayout);
        setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);
    }
            
    public void createReportTable() {

        HorizontalLayout reportLayout = new HorizontalLayout();
        reportLayout.setWidth("98%");
        reportLayout.addStyleName("rcorners3");
        reportLayout.setResponsive(true);
        reportLayout.setMargin(true);

        integracionContainer.addContainerProperty(CODIGO_PROPERTY,          Integer.class, null);
        integracionContainer.addContainerProperty(CUENTA_PROPERTY,          String.class, null);
        integracionContainer.addContainerProperty(CCOSTO_PROPERTY,          String.class, null);
        integracionContainer.addContainerProperty(DESCRIPCION_PROPERTY,     String.class, null);
        
        integracionContainer.addContainerProperty(CANTIDAD_PROPERTY,        Double.class, null);
        integracionContainer.addContainerProperty(PRECIO_PROPERTY,          Double.class, null);
        integracionContainer.addContainerProperty(TOTAL_PROPERTY,           Double.class, null);
        integracionContainer.addContainerProperty(MONEDA_PROPERTY,          String.class, null);
        
        integracionContainer.addContainerProperty(PROJECT_PROPERTY,         Integer.class, null);        
        integracionContainer.addContainerProperty(PROVEEDOR_PROPERTY,       String.class, null);
        integracionContainer.addContainerProperty(LOTE_PROPERTY,            Integer.class, null);
        
        integracionContainer.addContainerProperty(IDEX_PROPERTY,            String.class, null);

        integracionGrid = new Grid("Integración ACTUAL", integracionContainer);

        integracionGrid.setWidth("100%");
        integracionGrid.setImmediate(true);
        integracionGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        integracionGrid.setDescription("Seleccione un registro.");
        integracionGrid.setHeightMode(HeightMode.ROW);
        integracionGrid.setHeightByRows(10);
        integracionGrid.setResponsive(true);
        integracionGrid.setEditorBuffered(false);
                
        integracionGrid.setResponsive(true);
        integracionGrid.setEditorBuffered(false);

        integracionGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {

            if (PRECIO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (TOTAL_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (CANTIDAD_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (PROJECT_PROPERTY.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else if (LOTE_PROPERTY.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else {
                return null;
            }

        });

        integracionGrid.getColumn(CODIGO_PROPERTY).setExpandRatio(1).setHidable(true).setHidden(true);
        integracionGrid.getColumn(CUENTA_PROPERTY).setExpandRatio(2).setWidth(105);
        integracionGrid.getColumn(CCOSTO_PROPERTY).setExpandRatio(1).setWidth(80);
        integracionGrid.getColumn(DESCRIPCION_PROPERTY).setExpandRatio(2).setWidth(185);
        integracionGrid.getColumn(CANTIDAD_PROPERTY).setExpandRatio(1).setWidth(70);
        integracionGrid.getColumn(PRECIO_PROPERTY).setExpandRatio(1).setWidth(80);
        integracionGrid.getColumn(TOTAL_PROPERTY).setExpandRatio(1).setWidth(110);
        integracionGrid.getColumn(MONEDA_PROPERTY).setExpandRatio(1).setWidth(100);
        integracionGrid.getColumn(PROJECT_PROPERTY).setExpandRatio(1).setHidable(true);
        integracionGrid.getColumn(LOTE_PROPERTY).setExpandRatio(1).setHidable(true);
        integracionGrid.getColumn(PROVEEDOR_PROPERTY).setExpandRatio(2).setWidth(150);
        integracionGrid.getColumn(IDEX_PROPERTY).setExpandRatio(1).setHidable(true);

        integracionGrid.addSelectionListener(new SelectionListener() {
            @Override
            public void select(SelectionEvent event) {
                if (integracionGrid.getSelectedRow() != null) {
//                    mostrarAnticipos();
                }
            }
        });

        HeaderRow filterRow = integracionGrid.appendHeaderRow();

        HeaderCell cell = filterRow.getCell(CUENTA_PROPERTY);

        TextField filterField = new TextField();
        filterField.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField.setInputPrompt("Filtrar");
        filterField.setColumns(8);

        filterField.addTextChangeListener(change -> {
            integracionContainer.removeContainerFilters(CUENTA_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                integracionContainer.addContainerFilter(
                        new SimpleStringFilter(CUENTA_PROPERTY,
                                change.getText(), true, false));
            }
            setTotal();
        });
        cell.setComponent(filterField);

        HeaderCell cell0 = filterRow.getCell(CCOSTO_PROPERTY);

        TextField filterField0 = new TextField();
        filterField0.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField0.setInputPrompt("Filtrar");
        filterField0.setColumns(5);

        filterField0.addTextChangeListener(change -> {
            integracionContainer.removeContainerFilters(CCOSTO_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                integracionContainer.addContainerFilter(
                        new SimpleStringFilter(CCOSTO_PROPERTY,
                                change.getText(), true, false));
            }
            setTotal();
        });
        cell0.setComponent(filterField0);
         
        HeaderCell cell00 = filterRow.getCell(DESCRIPCION_PROPERTY);

        TextField filterField00 = new TextField();
        filterField00.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField00.setInputPrompt("Filtrar");
        filterField00.setColumns(15);

        filterField00.addTextChangeListener(change -> {
            integracionContainer.removeContainerFilters(DESCRIPCION_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                integracionContainer.addContainerFilter(
                        new SimpleStringFilter(DESCRIPCION_PROPERTY,
                                change.getText(), true, false));
            }
            setTotal();
        });
        cell00.setComponent(filterField00);

        HeaderCell cell1 = filterRow.getCell(MONEDA_PROPERTY);
        
        TextField filterField1 = new TextField();
        filterField1.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField1.setInputPrompt("Filtrar"); filterField1.setColumns(6);
        
        filterField1.addTextChangeListener(change -> {
        integracionContainer.removeContainerFilters(MONEDA_PROPERTY);
        
            // (Re)create the filter if necessary if
            if (!change.getText().isEmpty()) {
                integracionContainer.addContainerFilter( new
                SimpleStringFilter(MONEDA_PROPERTY, change.getText(), true, false));
            } 
            setTotal();
        }); 
        cell1.setComponent(filterField1);        
        
        HeaderCell cell2 = filterRow.getCell(PROVEEDOR_PROPERTY);

        TextField filterField2 = new TextField();
        filterField2.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField2.setInputPrompt("Filtrar");
        filterField2.setColumns(15);

        filterField2.addTextChangeListener(change -> {
            integracionContainer.removeContainerFilters(PROVEEDOR_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                integracionContainer.addContainerFilter(
                        new SimpleStringFilter(PROVEEDOR_PROPERTY,
                                change.getText(), true, false));
            }
            setTotal();
        });
        cell2.setComponent(filterField2);

        HeaderCell cellIdex = filterRow.getCell(IDEX_PROPERTY);

        TextField filterFieldIdex = new TextField();
        filterFieldIdex.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterFieldIdex.setInputPrompt("Filtrar");
        filterFieldIdex.setColumns(15);

        filterFieldIdex.addTextChangeListener(change -> {
            integracionContainer.removeContainerFilters(IDEX_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                integracionContainer.addContainerFilter(
                        new SimpleStringFilter(IDEX_PROPERTY,
                                change.getText(), true, false));
            }
            setTotal();
        });
        cellIdex.setComponent(filterFieldIdex);

        documentosFooter = integracionGrid.appendFooterRow();
        documentosFooter.getCell(DESCRIPCION_PROPERTY).setText("Totales");
        documentosFooter.getCell(DESCRIPCION_PROPERTY).setStyleName("rightalign");
        documentosFooter.getCell(TOTAL_PROPERTY).setText("0.00");
        documentosFooter.getCell(TOTAL_PROPERTY).setStyleName("rightalign");

        reportLayout.addComponent(integracionGrid);
        reportLayout.setComponentAlignment(integracionGrid, Alignment.MIDDLE_CENTER);

        addComponent(reportLayout);        
        setComponentAlignment(reportLayout, Alignment.MIDDLE_CENTER);
    }

    public void fillReport(String empresa, String empresaNombre) {
        
        this.empresa = empresa;
        this.empresaNombre = empresaNombre;
        
        integracionContainer.removeAllItems();
        documentosFooter.getCell(TOTAL_PROPERTY).setText("0.00");

        String queryString;
        
        queryString =  "SELECT DITEMC.NoCuenta, DITEMC.Descripcion, DITEMC.IdCC, Prov.IdProveedor, ";
        queryString += " Prov.Nombre ProveedorNombre, DITEMC.Lote, DITEMC.IdProject, DITEMC.Moneda, DITEMC.Idex, ";
        queryString += " SUM(DITEMC.Total / DITEMC.Cantidad) PrecioTotal, SUM(DITEMC.Cantidad) CantidadTotal, SUM(DITEMC.Total) TotalTotal ";
        queryString += " FROM  DetalleItemsCostos DITEMC";
        queryString += " LEFT JOIN proveedor_empresa Prov On Prov.IdProveedor = DITEMC.IdProveedor";
        queryString += " WHERE DITEMC.IdEmpresa = " + empresa;
        queryString += " AND DITEMC.Tipo IN ('INTINI', 'DOCA')";
        queryString += " AND Prov.IdEmpresa = " + empresa;
        queryString += " GROUP BY DITEMC.NoCuenta, DITEMC.Descripcion, DITEMC.IdCC, Prov.IdProveedor, ";
        queryString += " Prov.Nombre, DITEMC.Lote, DITEMC.IdProject, DITEMC.Moneda, DITEMC.Idex";
        queryString += " ORDER BY DITEMC.NoCuenta";

System.out.println("\n\n"+queryString);

        try {

            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords = stQuery.executeQuery (queryString);

            if(rsRecords.next()) { //  encontrado

                BigDecimal granTotal = new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP);

                do {
                    
                    Object itemId = integracionContainer.addItem();

                    integracionContainer.getContainerProperty(itemId, CODIGO_PROPERTY).setValue(integracionContainer.size()+1);
                    integracionContainer.getContainerProperty(itemId, CUENTA_PROPERTY).setValue(rsRecords.getString("NoCuenta"));
                    integracionContainer.getContainerProperty(itemId, CCOSTO_PROPERTY).setValue(rsRecords.getString("IdCC"));
                    integracionContainer.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(rsRecords.getString("Descripcion"));
                    integracionContainer.getContainerProperty(itemId, CANTIDAD_PROPERTY).setValue(rsRecords.getDouble("CantidadTotal"));
                    integracionContainer.getContainerProperty(itemId, PRECIO_PROPERTY).setValue(rsRecords.getDouble("PrecioTotal"));
                    integracionContainer.getContainerProperty(itemId, TOTAL_PROPERTY).setValue(rsRecords.getDouble("TotalTotal"));
                    integracionContainer.getContainerProperty(itemId, MONEDA_PROPERTY).setValue(rsRecords.getString("Moneda"));
                    integracionContainer.getContainerProperty(itemId, PROJECT_PROPERTY).setValue(rsRecords.getInt("IdProject"));
                    integracionContainer.getContainerProperty(itemId, LOTE_PROPERTY).setValue(rsRecords.getInt("Lote"));
                    integracionContainer.getContainerProperty(itemId, PROVEEDOR_PROPERTY).setValue(rsRecords.getString("IdProveedor") + " " + rsRecords.getString("ProveedorNombre"));
                    integracionContainer.getContainerProperty(itemId, IDEX_PROPERTY).setValue(rsRecords.getString("Idex"));

                    granTotal = granTotal.add(new BigDecimal(rsRecords.getDouble("TotalTotal"))).setScale(2, BigDecimal.ROUND_HALF_UP);

                }while(rsRecords.next());

                documentosFooter.getCell(TOTAL_PROPERTY).setText(moneyFormat.format(granTotal));
            }
        } 
        catch (Exception ex) {
            Logger.getLogger(IntegracionActual.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de detalle de integracion : " + ex.getMessage());
            Notification.show("Error al intentar leer detalle de integracion..!", Notification.Type.ERROR_MESSAGE);
        }

    } 
    
    private void setTotal() {
        BigDecimal total = new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP);
        for (Object rid: integracionGrid.getContainerDataSource()
                     .getItemIds()) {
                     total = total.add(new BigDecimal(
                             Double.parseDouble(
                                     String.valueOf(integracionContainer.getContainerProperty(rid, TOTAL_PROPERTY).getValue())
                             )));
        }
        documentosFooter.getCell(TOTAL_PROPERTY).setText(moneyFormat.format(total));
    }
        
    public boolean exportToExcel() {
        if (integracionGrid.getHeightByRows() > 0) {
            TableHolder tableHolder = new DefaultTableHolder(integracionGrid);
            ExcelExport excelExport = new ExcelExport(tableHolder);
            excelExport.excludeCollapsedColumns();
            excelExport.setDisplayTotals(false);
            String fileexport;
            fileexport = (empresa+"_" + empresaNombre.substring(5, empresaNombre.length()).replaceAll(" ", "_").replaceAll(",", "_").replaceAll("[()]", "").replaceAll("[.]", "").replaceAll("ñ", "n").replaceAll("Ñ", "N").replaceAll("ó", "o").replaceAll("é","") + "_INTEGRACION_CAMBIOS.xlsx").replaceAll(" ", "").replaceAll(",", "");
            excelExport.setExportFileName(fileexport);
            excelExport.export();
        }
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

}