package com.simpletecno.sopdi.inventario;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.addon.tableexport.DefaultTableHolder;
import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.addon.tableexport.TableHolder;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.sql.ResultSet;
import java.sql.Statement;
import org.vaadin.dialogs.ConfirmDialog;

/**
 *
 * @author user
 */
public class ProductosProveedorView extends VerticalLayout implements View {

    public static final String ID_PROPERTY = "Id";
    public static final String ID_PROVEEDOR_PROPERTY = "Id proveedor";
    public static final String PROVEEDOR_PROPERTY = "Proveedor";
    public static final String ID_PRODUCTO_PROPERTY = "Id Producto";
    public static final String PRODUCTO_PROPERTY = "Producto";    
    public static final String PLU_PROPERTY = "PLU";
    public static final String PLUDESCRIPCION_PROPERTY = "Plu Descripcion";
    public static final String PRECIO_PROPERTY = "Precio";

    public IndexedContainer productosContainer = new IndexedContainer();
    Grid productosGrid;

    UI mainUI;
    Statement stQuery;
    ResultSet rsRecords;
    String queryString;

    VerticalLayout mainLayout = new VerticalLayout();

    int vanderaEdicion = 0;

    public ProductosProveedorView() {

        this.mainUI = UI.getCurrent();
        setWidth("100%");
        setMargin(false);
        setSpacing(true);

        Label titleLbl = new Label("PRODUCTOS Y SUS PROVEEDORES");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h1_custom");

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setResponsive(true);
        titleLayout.setWidth("100%");
        titleLayout.setMargin(new MarginInfo(false, true, false, false));
        titleLayout.addComponent(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.TOP_RIGHT);

        addComponent(titleLayout);
        setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        createDetailsGrid();

        fillproductosGrid();

    }

    public void createDetailsGrid() {

        VerticalLayout detalleLayout = new VerticalLayout();
        detalleLayout.setWidth("100%");
        detalleLayout.addStyleName("rcorners3");
        detalleLayout.setSpacing(true);

        HorizontalLayout idexYEmpleadosLayout = new HorizontalLayout();
        idexYEmpleadosLayout.setWidth("100%");
        idexYEmpleadosLayout.addStyleName("rcorners3");
        idexYEmpleadosLayout.setSpacing(true);

        HorizontalLayout botonesLayout = new HorizontalLayout();
        botonesLayout.setSpacing(true);
        botonesLayout.setWidth("100%");

        detalleLayout.addComponents(idexYEmpleadosLayout, botonesLayout);

        productosContainer.addContainerProperty(ID_PROPERTY, String.class, null);
        productosContainer.addContainerProperty(ID_PROVEEDOR_PROPERTY, String.class, null);
        productosContainer.addContainerProperty(PROVEEDOR_PROPERTY, String.class, null);
        productosContainer.addContainerProperty(ID_PRODUCTO_PROPERTY, String.class, null);
        productosContainer.addContainerProperty(PRODUCTO_PROPERTY, String.class, null);
        productosContainer.addContainerProperty(PLU_PROPERTY, String.class, null);
        productosContainer.addContainerProperty(PLUDESCRIPCION_PROPERTY, String.class, null);
        productosContainer.addContainerProperty(PRECIO_PROPERTY, String.class, null);

        productosGrid = new Grid("Listado de productos y proveedores ", productosContainer);
        productosGrid.setImmediate(true);
        productosGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        productosGrid.setHeightMode(HeightMode.ROW);
        productosGrid.setHeightByRows(15);
        productosGrid.setWidth("100%");
        productosGrid.setResponsive(true);
        productosGrid.setSizeFull();

        productosGrid.getColumn(ID_PROPERTY).setHidden(true).setHidable(true);
        productosGrid.getColumn(ID_PROVEEDOR_PROPERTY).setHidden(true).setHidable(true);
        productosGrid.getColumn(ID_PRODUCTO_PROPERTY).setHidden(true).setHidable(true);

        productosGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if (PRECIO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }
        });

        Grid.HeaderRow filterRow = productosGrid.appendHeaderRow();

        Grid.HeaderCell cell = filterRow.getCell(PROVEEDOR_PROPERTY);
        TextField filterField = new TextField();
        filterField.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField.setInputPrompt("Filtrar");
        filterField.setColumns(10);
        filterField.addTextChangeListener(change -> {
            productosContainer.removeContainerFilters(PROVEEDOR_PROPERTY);
            if (!change.getText().isEmpty()) {
                productosContainer.addContainerFilter(
                        new SimpleStringFilter(PROVEEDOR_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell.setComponent(filterField);

        Grid.HeaderCell cell2 = filterRow.getCell(PRODUCTO_PROPERTY);
        TextField filterField2 = new TextField();
        filterField2.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField2.setInputPrompt("Filtrar");
        filterField2.setColumns(10);

        filterField2.addTextChangeListener(change -> {
            productosContainer.removeContainerFilters(PRODUCTO_PROPERTY);
            if (!change.getText().isEmpty()) {
                productosContainer.addContainerFilter(
                        new SimpleStringFilter(PRODUCTO_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell2.setComponent(filterField2);

        Grid.HeaderCell cell3 = filterRow.getCell(PLU_PROPERTY);
        TextField filterField3 = new TextField();
        filterField3.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField3.setInputPrompt("Filtrar");
        filterField3.setColumns(10);

        filterField3.addTextChangeListener(change -> {
            productosContainer.removeContainerFilters(PLU_PROPERTY);
            if (!change.getText().isEmpty()) {
                productosContainer.addContainerFilter(
                        new SimpleStringFilter(PLU_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell3.setComponent(filterField3);

        idexYEmpleadosLayout.addComponent(productosGrid);

        Button eliminarBtn = new Button("Eliminar");
        eliminarBtn.setIcon(FontAwesome.TRASH);
        eliminarBtn.addStyleName(ValoTheme.BUTTON_DANGER);
        eliminarBtn.setDescription("Eliminar registro.");
        eliminarBtn.addListener((Button.ClickListener) (Button.ClickEvent event) -> {
            if (productosGrid.getSelectedRow() != null) {
                ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de eliminar el registro?",
                        "SI", "NO", new ConfirmDialog.Listener() {
                    public void onClose(ConfirmDialog dialog) {
                        if (dialog.isConfirmed()) {

                            try {
                                queryString = "DELETE FROM proveedor_productos";
                                queryString += " WHERE Id = " + productosContainer.getContainerProperty(productosGrid.getSelectedRow(), ID_PROPERTY).getValue();

                                stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                                stQuery.executeUpdate(queryString);

                                Notification notif = new Notification("Registro eliminado con exito! ", Notification.Type.HUMANIZED_MESSAGE);
                                notif.setDelayMsec(1500);
                                notif.setPosition(Position.MIDDLE_CENTER);
                                notif.show(Page.getCurrent());

                            } catch (Exception ex) {
                                System.out.println("Erro al intetnar eliminar registro " + ex);
                                ex.printStackTrace();
                            }
                        }
                    }
                });
            }
        });

        Button actualizarBtn = new Button("Editar");
        actualizarBtn.setIcon(FontAwesome.EDIT);
        actualizarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        actualizarBtn.setDescription("Actualizar registro.");
        actualizarBtn.addListener((Button.ClickListener) (Button.ClickEvent event) -> {
            if (productosGrid.getSelectedRow() != null) {
AsignarProveedorProductoForm asignacionProductos
                            = new AsignarProveedorProductoForm(String.valueOf(productosContainer.getContainerProperty(productosGrid.getSelectedRow(), ID_PRODUCTO_PROPERTY).getValue()),
                                    String.valueOf(productosContainer.getContainerProperty(productosGrid.getSelectedRow(), PRODUCTO_PROPERTY).getValue()),
                                    "",
                            String.valueOf(productosContainer.getContainerProperty(productosGrid.getSelectedRow(), ID_PROVEEDOR_PROPERTY).getValue()));
                    UI.getCurrent().addWindow(asignacionProductos);
                    asignacionProductos.center();            }
        });

        Button generarExcel = new Button("Exportar a Excel");
        generarExcel.setIcon(FontAwesome.FILE_EXCEL_O);
        generarExcel.addStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
        generarExcel.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (productosContainer.size() > 0) {
                    exportToExcel();
                } else {
                    Notification notif = new Notification("La vista no contiene registros disponibles..", Notification.Type.WARNING_MESSAGE);
                    notif.setDelayMsec(1500);
                    notif.setPosition(Position.MIDDLE_CENTER);
                    notif.setIcon(FontAwesome.WARNING);
                    notif.show(Page.getCurrent());
                }
            }
        });

        botonesLayout.addComponents(eliminarBtn, actualizarBtn, generarExcel);
        botonesLayout.setComponentAlignment(eliminarBtn, Alignment.BOTTOM_LEFT);
        botonesLayout.setComponentAlignment(actualizarBtn, Alignment.BOTTOM_CENTER);
        botonesLayout.setComponentAlignment(generarExcel, Alignment.BOTTOM_RIGHT);

        addComponent(detalleLayout);
        setComponentAlignment(detalleLayout, Alignment.MIDDLE_CENTER);

    }

    public void fillproductosGrid() {
        try {
            productosContainer.removeAllItems();

            queryString = " SELECT *, inv_producto.Descripcion AS nomProducto, proveedor.Nombre AS nomProveedor, proveedor.IdProveedor as IdProvee ";
            queryString += " FROM proveedor_productos";
            queryString += " INNER JOIN inv_producto on proveedor_productos.IdProducto = inv_producto.IdProducto";
            queryString += " INNER JOIN proveedor on proveedor_productos.IdProveedor = proveedor.IdProveedor";

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado
                do {
                    Object itemId = productosContainer.addItem();
                    productosContainer.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords.getString("Id"));
                    productosContainer.getContainerProperty(itemId, ID_PROVEEDOR_PROPERTY).setValue(rsRecords.getString("IdProvee"));
                    productosContainer.getContainerProperty(itemId, PROVEEDOR_PROPERTY).setValue(rsRecords.getString("nomProveedor"));
                    productosContainer.getContainerProperty(itemId, ID_PRODUCTO_PROPERTY).setValue(rsRecords.getString("IdProducto"));
                    productosContainer.getContainerProperty(itemId, PRODUCTO_PROPERTY).setValue(rsRecords.getString("nomProducto"));
                    productosContainer.getContainerProperty(itemId, PLU_PROPERTY).setValue(rsRecords.getString("PLU"));
                    productosContainer.getContainerProperty(itemId, PLUDESCRIPCION_PROPERTY).setValue(rsRecords.getString("PLUDescripcion"));
                    productosContainer.getContainerProperty(itemId, PRECIO_PROPERTY).setValue(rsRecords.getString("Precio"));

                } while (rsRecords.next());
            }
        } catch (Exception ex) {
            System.out.println("Error al listar tabla PRODUCTOS : " + ex);
            ex.printStackTrace();
        }
    }

    public boolean exportToExcel() {
        if (this.productosGrid.getHeightByRows() > 0) {
            TableHolder tableHolder = new DefaultTableHolder(productosGrid);
            ExcelExport excelExport = new ExcelExport(tableHolder);
            excelExport.excludeCollapsedColumns();
            excelExport.setDisplayTotals(false);
            String fileexport;
            fileexport = "Listado_de_productos_por_proveedor.xls";
            excelExport.setExportFileName(fileexport);
            excelExport.export();
        }
        return true;
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - PRODUCTOS Y SUS PROVEEDORES");
    }
}
