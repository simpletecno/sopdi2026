/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.configuracion;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.inventario.ProveedorPluView;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.simpletecno.sopdi.compras.ImportarProveedoresForm;
import com.vaadin.addon.tableexport.DefaultTableHolder;
import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.addon.tableexport.TableHolder;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Sizeable;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.Grid.HeaderCell;
import com.vaadin.ui.Grid.HeaderRow;
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
public class ProveedorView extends VerticalLayout implements View {

    public Statement stQuery = null;
    public ResultSet rsRecords = null;

    static final String ID_PROPERTY = "Id";
    static final String IDPROVEEDOR_PROPERTY = "CODIGO";
    static final String NOMBRE_PROPERTY = "Nombre o Razón Social";
    static final String NIT_PROPERTY = "NIT";
    static final String INHABILIDATO_PROPERTY = "Inhabilitado";
    static final String ESNOMBRE_PROPERTY = "Es proveedor";
    static final String ESCLIENTE_PROPERTY = "Es cliente";

    public IndexedContainer containerProveedor = new IndexedContainer();

    MarginInfo marginInfo;

    Button editarBtn;
    Button nuevoBtn;
    Button deleteBtn;
    Button exportExcelBtn;
    Button importExcelBtn;
    Button notasBtn;
    Button plusBtn;

    VerticalLayout proveedorLayout;
    Grid proveedorGrid;
    Grid.FooterRow footerProveedor;

    Label saldoLbl;
    public static Locale locale = new Locale("ES", "GT");
    private static DecimalFormat numberFormat = new DecimalFormat("##,###,##0.00");

    private UI mainUI;

    public ProveedorView() {

        this.mainUI = UI.getCurrent();

        marginInfo = new MarginInfo(true, false, false, false);
        setSpacing(true);

        addComponent(createProveedorTable());
        setComponentAlignment(proveedorLayout, Alignment.MIDDLE_CENTER);

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setMargin(false);

        editarBtn = new Button("Editar");
        editarBtn.setIcon(FontAwesome.EDIT);
        editarBtn.setWidth(120, Sizeable.UNITS_PIXELS);
        editarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (!((SopdiUI) mainUI).sessionInformation.getStrUserProfile().equals("ADMINISTRADOR")) {
                    Notification.show("Usuario no tiene permiso para esta operación", Notification.Type.WARNING_MESSAGE);
                    return;
                }
                if (containerProveedor.size() > 0) {
                    if (proveedorGrid.getSelectedRow() != null) {
                        ProveedorForm formProveedor =
                                new ProveedorForm(String.valueOf(containerProveedor.getContainerProperty(proveedorGrid.getSelectedRow(), ID_PROPERTY).getValue()));
                        mainUI.addWindow(formProveedor);
                        formProveedor.center();
                    } else {
                        if (proveedorGrid.getSelectedRow() == null) {
                            Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
                        }
                    }
                }
            }
        });

        nuevoBtn = new Button("Nuevo");
        nuevoBtn.setIcon(FontAwesome.PLUS);
        nuevoBtn.setWidth(120, Sizeable.UNITS_PIXELS);
        nuevoBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (!((SopdiUI) mainUI).sessionInformation.getStrUserProfile().equals("ADMINISTRADOR")) {
                    Notification.show("Usuario no tiene permiso para esta operación.", Notification.Type.WARNING_MESSAGE);
                    return;
                }
                ProveedorForm formProveedor = new ProveedorForm("0");
                mainUI.addWindow(formProveedor);
                formProveedor.center();
            }
        });

        deleteBtn = new Button("Eliminar");
        deleteBtn.setIcon(FontAwesome.REMOVE);
        deleteBtn.setWidth(120, Sizeable.UNITS_PIXELS);
        deleteBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (!((SopdiUI) mainUI).sessionInformation.getStrUserProfile().equals("ADMINISTRADOR")) {
                    Notification.show("Usuario no tiene permiso para esta operación.", Notification.Type.WARNING_MESSAGE);
                    return;
                }
                if (containerProveedor.size() > 0) {
                    if (proveedorGrid.getSelectedRow() != null) {

//                        if (historialContableTable.size() > 0) {
//                            Notification.show("Este proveedor/cliente tiene historial de contable, no se puede eliminar.", Notification.Type.WARNING_MESSAGE);
//                            return;
//                        }
                        ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de eliminar el registro del proveedor/cliente?",
                                "SI", "NO", new ConfirmDialog.Listener() {

                            public void onClose(ConfirmDialog dialog) {
                                if (dialog.isConfirmed()) {
                                    deleteProveedor();
                                }
                            }
                        });
                    } else {
                        if (proveedorGrid.getSelectedRow() == null) {
                            Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
                        }
                    }
                }
            }
        });

        exportExcelBtn = new Button("Exportar");
        exportExcelBtn.setIcon(FontAwesome.FILE_EXCEL_O);
        exportExcelBtn.setWidth(120, Sizeable.UNITS_PIXELS);
        exportExcelBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if(proveedorGrid.getHeightByRows() > 0) {
                    TableHolder tableHolder = new DefaultTableHolder(proveedorGrid);
                    ExcelExport excelExport = new ExcelExport (tableHolder);
                    excelExport.excludeCollapsedColumns();
                    excelExport.setDisplayTotals(false);
                    excelExport.export();                    
                }
            }
        });

        importExcelBtn = new Button("Importar");
        importExcelBtn.setIcon(FontAwesome.FILE_EXCEL_O);
        importExcelBtn.setWidth(120, Sizeable.UNITS_PIXELS);
        importExcelBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                ImportarProveedoresForm formImportarProveedores = new ImportarProveedoresForm();
                mainUI.addWindow(formImportarProveedores);
                formImportarProveedores.center();
            }
        });

        notasBtn = new Button("Notas");
        notasBtn.setIcon(FontAwesome.NEWSPAPER_O);
        notasBtn.setWidth(120, Sizeable.UNITS_PIXELS);
        notasBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
//                ImportarProveedoresForm formImportarProveedores = new ImportarProveedoresForm();
//                mainUI.addWindow(formImportarProveedores);
//                formImportarProveedores.center();
            }
        });

        plusBtn = new Button("PLUs");
        plusBtn.setIcon(FontAwesome.BARCODE);
        plusBtn.setWidth(120, Sizeable.UNITS_PIXELS);
        plusBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                ProveedorPluView proveedorPluView = new ProveedorPluView();
                mainUI.addWindow(proveedorPluView);
                proveedorPluView.center();
            }
        });

        buttonsLayout.addComponent(editarBtn);
        buttonsLayout.addComponent(nuevoBtn);
        buttonsLayout.addComponent(deleteBtn);
        buttonsLayout.addComponent(exportExcelBtn);
        buttonsLayout.addComponent(importExcelBtn);
        buttonsLayout.addComponent(notasBtn);
        buttonsLayout.addComponent(plusBtn);

        addComponent(buttonsLayout);
        setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);

        fillProveedorTable();
    }

    public VerticalLayout createProveedorTable() {

        containerProveedor.addContainerProperty(ID_PROPERTY, String.class, null);
        containerProveedor.addContainerProperty(IDPROVEEDOR_PROPERTY, String.class, null);
        containerProveedor.addContainerProperty(NOMBRE_PROPERTY, String.class, null);
        containerProveedor.addContainerProperty(NIT_PROPERTY, String.class, null);
        containerProveedor.addContainerProperty(INHABILIDATO_PROPERTY, String.class, null);
        containerProveedor.addContainerProperty(ESNOMBRE_PROPERTY, String.class, null);
        containerProveedor.addContainerProperty(ESCLIENTE_PROPERTY, String.class, null);

        proveedorGrid = new Grid("Proveedores/Clientes/Otros", containerProveedor);
        proveedorGrid.addStyleName(ValoTheme.TABLE_COMPACT);
        proveedorGrid.setWidth("100%");
        proveedorGrid.setDescription("Seleccione un registro.");
        proveedorGrid.setHeightMode(HeightMode.ROW);
        proveedorGrid.setHeightByRows(15);
        proveedorGrid.setImmediate(true);
        proveedorGrid.setImmediate(true);

        proveedorGrid.getColumn(ID_PROPERTY).setWidth(0).setHidden(true);

        HeaderRow filterRow = proveedorGrid.appendHeaderRow();

        HeaderCell cellPro = filterRow.getCell(NOMBRE_PROPERTY);

        TextField filterFieldPro = new TextField();
        filterFieldPro.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterFieldPro.setInputPrompt("Filtrar");
        filterFieldPro.setColumns(10);

        filterFieldPro.addTextChangeListener(change -> {
            containerProveedor.removeContainerFilters(NOMBRE_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                containerProveedor.addContainerFilter(
                        new SimpleStringFilter(NOMBRE_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cellPro.setComponent(filterFieldPro);

        HeaderCell cellProId = filterRow.getCell(IDPROVEEDOR_PROPERTY);

        TextField filterFieldProId = new TextField();
        filterFieldProId.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterFieldProId.setInputPrompt("Filtrar");
        filterFieldProId.setColumns(10);

        filterFieldProId.addTextChangeListener(change -> {
            containerProveedor.removeContainerFilters(IDPROVEEDOR_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                containerProveedor.addContainerFilter(
                        new SimpleStringFilter(IDPROVEEDOR_PROPERTY,
                                change.getText(), true, true));
            }
        });
        cellProId.setComponent(filterFieldProId);

        proveedorLayout = new VerticalLayout();
        proveedorLayout.addStyleName("rcorners3");
        proveedorLayout.setWidth("98%");

        proveedorLayout.addComponent(proveedorGrid);
        proveedorLayout.setComponentAlignment(proveedorGrid, Alignment.TOP_CENTER);

        return proveedorLayout;
    }

    public void fillProveedorTable() {

        containerProveedor.removeAllItems();
        containerProveedor.removeAllContainerFilters();

        String queryString = "SELECT * ";
        queryString += " FROM  proveedor";
        queryString += " WHERE Id > 0";
        queryString += " ORDER BY Nombre";

//System.out.println("\n\n"+queryString);
        try {

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado
                
                Object itemId;
                
                do {
                    itemId = containerProveedor.addItem();

                    containerProveedor.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords.getString("Id"));
                    containerProveedor.getContainerProperty(itemId, IDPROVEEDOR_PROPERTY).setValue(rsRecords.getString("Codigo"));
                    containerProveedor.getContainerProperty(itemId, NOMBRE_PROPERTY).setValue(rsRecords.getString("Nombre"));
                    containerProveedor.getContainerProperty(itemId, NIT_PROPERTY).setValue(rsRecords.getString("NIT"));
                    containerProveedor.getContainerProperty(itemId, INHABILIDATO_PROPERTY).setValue(rsRecords.getString("Inhabilitado"));
                    containerProveedor.getContainerProperty(itemId, ESNOMBRE_PROPERTY).setValue(rsRecords.getString("EsProveedor"));
                    containerProveedor.getContainerProperty(itemId, ESCLIENTE_PROPERTY).setValue(rsRecords.getString("EsCliente"));

                } while (rsRecords.next());
            }

        } catch (Exception ex) {
            Logger.getLogger(ProveedorView.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de proveedores : " + ex.getMessage());
            Notification.show("Error al intentar leer registros de proveedores..!", Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }

    }

    public boolean exportToExcel(Table tableToExport) {
        ExcelExport excelExport;

        excelExport = new ExcelExport(tableToExport);
        excelExport.excludeCollapsedColumns();
        excelExport.setExportFileName("CATALOGO_MASTER_PROVEEDORES.xls");

        String mainTitle = "CATALOGO MASTER DE PROVEEDORES AL: " + new Utileria().getFechaYYYYMMDD_1(new Date());

        excelExport.setReportTitle(mainTitle);

        excelExport.export();

        return true;

    }

    /**
     * This class creates a streamresource. This class implements the
     * StreamSource interface which defines the getStream method.
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

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - Proveedores");
    }

    private void deleteProveedor() {
//        if (historialContableTable.size() > 0) {
//            Notification.show("Este registro tiene historial contable,  no se puede eliminar su registro!");
//            return;
//        }

        try {
            String queryString = "DELETE FROM proveedor ";
            queryString += " WHERE Id = " + String.valueOf(containerProveedor.getContainerProperty(proveedorGrid.getSelectedRow(), ID_PROPERTY).getValue());

            stQuery.executeUpdate(queryString);

            Notification.show("Operación exitosa!", Notification.Type.TRAY_NOTIFICATION);
            
            containerProveedor.removeItem(proveedorGrid.getSelectedRow());
            
        } catch (Exception ex) {
            Logger.getLogger(ProveedorView.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al ELIMINAR registros de proveedor : " + ex.getMessage());
            Notification.show("Error al ELIMINAR registros de proveedor..!", Notification.Type.ERROR_MESSAGE);
            
        }
    }

}
