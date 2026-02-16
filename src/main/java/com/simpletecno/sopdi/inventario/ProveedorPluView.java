package com.simpletecno.sopdi.inventario;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.addon.tableexport.DefaultTableHolder;
import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.addon.tableexport.TableHolder;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.SelectionEvent;
import com.vaadin.event.SelectionEvent.SelectionListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Responsive;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.Grid.HeaderCell;
import com.vaadin.ui.Grid.HeaderRow;
import com.vaadin.ui.themes.ValoTheme;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author user
 */
public class ProveedorPluView extends Window {

    static final String ID_PROPERTY= "Id";

    static final String NO_CUENTA_PROPERTY = "NoCuenta";
    static final String DESCRIPCION_PROPERTY = "Descripcion";
    static final String ID_PROVEEDOR_PROPERTY = "IdProveedor";
    static final String PROVEEDOR_PROPERTY = "Proveedor";
    static final String PLU_PROPERTY = "PLU";
    static final String DESCRIPCIONPRV_PROPERTY = "DescripcionPrv";
    static final String CANTIDAD_PROPERTY = "Cantidad";
    static final String PRECION_PROPERTY  = "Precio";

    VerticalLayout mainLayout = new VerticalLayout();

    public IndexedContainer container = new IndexedContainer();
    Grid plusGrid;

    UI mainUI;
    Statement stQuery;
    ResultSet rsRecords;
    String queryString;
    Button inhabilitarBtn;
    Button exportExcelBtn;

    public ProveedorPluView() {

        Responsive.makeResponsive(this);
//        setMargin(true);
//        setSpacing(true);
        this.mainUI = UI.getCurrent();

        setContent(mainLayout);

        Label titleLbl = new Label("PLUs de Proveedores");
        titleLbl.addStyleName(ValoTheme.LABEL_H1);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h1_custom");

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setMargin(true);
        titleLayout.setSpacing(true);
        titleLayout.setWidth("100%");
        titleLayout.addComponents(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.TOP_LEFT);

        mainLayout.addComponent(titleLayout);
        mainLayout.setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        createTabla();
        llenarTabla();

        createButtons();

    }

    public void createTabla() {
        HorizontalLayout reportLayout = new HorizontalLayout();
        reportLayout.setWidth("98%");
        reportLayout.addStyleName("rcorners3");
        reportLayout.setResponsive(true);
        reportLayout.setMargin(true);

        container.addContainerProperty(ID_PROPERTY, String.class, "");
        container.addContainerProperty(ID_PROVEEDOR_PROPERTY, String.class, "");
        container.addContainerProperty(PROVEEDOR_PROPERTY, String.class, "");
        container.addContainerProperty(NO_CUENTA_PROPERTY, String.class, "");
        container.addContainerProperty(DESCRIPCION_PROPERTY, String.class, "");
        container.addContainerProperty(PLU_PROPERTY, String.class, "");
        container.addContainerProperty(DESCRIPCIONPRV_PROPERTY, String.class, "");
        container.addContainerProperty(CANTIDAD_PROPERTY, String.class, "");
        container.addContainerProperty(PRECION_PROPERTY, String.class, "");

        plusGrid = new Grid("Listado de PLUS", container);
        plusGrid.setImmediate(true);
        plusGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        plusGrid.setDescription("Seleccione un registro.");
        plusGrid.setHeightMode(HeightMode.ROW);
        plusGrid.setHeightByRows(10);
        plusGrid.setWidth("100%");
        plusGrid.setResponsive(true);
        plusGrid.setEditorBuffered(false);

        plusGrid.addSelectionListener(new SelectionListener() {
            @Override
            public void select(SelectionEvent event) {
                if (plusGrid.getSelectedRow() != null) {

//                    if (String.valueOf(plusGrid.getContainerDataSource().getItem(plusGrid.getSelectedRow()).getItemProperty(ESTATUS_PROPERTY).getValue()).equals("HABILITADA")) {
//                        inhabilitarBtn.setCaption("Inhabilitar");
//                        inhabilitarBtn.setIcon(FontAwesome.HAND_O_DOWN);
//                    } else {
//                        inhabilitarBtn.setCaption("Habilitar");
//                        inhabilitarBtn.setIcon(FontAwesome.HAND_O_UP);
//                    }
                }
            }
        });

        HeaderRow filterRow = plusGrid.appendHeaderRow();

        HeaderCell cell = filterRow.getCell(NO_CUENTA_PROPERTY);
        TextField filterField = new TextField();
        filterField.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField.setInputPrompt("Filtrar");
        filterField.setColumns(10);

        filterField.addTextChangeListener(change -> {
            container.removeContainerFilters(NO_CUENTA_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(
                        new SimpleStringFilter(NO_CUENTA_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell.setComponent(filterField);

        HeaderCell cell0 = filterRow.getCell(PLU_PROPERTY);
        TextField filterField0 = new TextField();
        filterField0.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField0.setInputPrompt("Filtrar");
        filterField0.setColumns(8);

        filterField0.addTextChangeListener(change -> {
            container.removeContainerFilters(PLU_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(
                        new SimpleStringFilter(PLU_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell0.setComponent(filterField0);

        HeaderCell cell1 = filterRow.getCell(PROVEEDOR_PROPERTY);
        TextField filterField01 = new TextField();
        filterField01.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField01.setInputPrompt("Filtrar");
        filterField01.setColumns(10);

        filterField01.addTextChangeListener(change -> {
            container.removeContainerFilters(PROVEEDOR_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(
                        new SimpleStringFilter(PROVEEDOR_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell1.setComponent(filterField01);

        reportLayout.addComponent(plusGrid);
        reportLayout.setComponentAlignment(plusGrid, Alignment.MIDDLE_CENTER);

        mainLayout.addComponent(reportLayout);
        mainLayout.setComponentAlignment(reportLayout, Alignment.TOP_CENTER);
    }

    private void createButtons() {

        Button editBtn = new Button("Editar");
        editBtn.setIcon(FontAwesome.EDIT);
        editBtn.setWidth(140, Sizeable.UNITS_PIXELS);
        editBtn.setDescription("Actualizar datos");
        editBtn.addListener((Button.ClickListener) (Button.ClickEvent event) -> {
            try {
                if (plusGrid.getSelectedRow() == null) {
                    Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
                } else {
                    ProveedorPluForm proveedorPluForm = new ProveedorPluForm();
                    proveedorPluForm.idPluEdit = String.valueOf(container.getContainerProperty(plusGrid.getSelectedRow(), ID_PROPERTY).getValue());
                    proveedorPluForm.llenarCampos();
                    UI.getCurrent().addWindow(proveedorPluForm);
                }
                plusGrid.select(null);
            } catch (Exception ex) {
                System.out.println("Error en el boton editar registro" + ex);
            }
        });

        Button newBtn = new Button("Nueva");
        newBtn.setIcon(FontAwesome.PLUS);
        newBtn.setWidth(140, Sizeable.UNITS_PIXELS);
        newBtn.setDescription("Agregar nueva cuenta contable.");
        newBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                try {
                    ProveedorPluForm proveedorPluForm = new ProveedorPluForm();
                    proveedorPluForm.idPluEdit = "0";
                    UI.getCurrent().addWindow(proveedorPluForm);

                } catch (Exception ex) {
                    System.out.println("Error en el Nuevo Registro" + ex);
                }
            }
        });

        Button eliminarBtn = new Button("Eliminar");
        eliminarBtn.setIcon(FontAwesome.TRASH);
        eliminarBtn.setDescription("Eliminar cuenta.");
        eliminarBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (plusGrid.getSelectedRow() == null) {
                    Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
                } else {

                    queryString = " select * from orden_compra_detalle opd";
                    queryString += " inner join orden_compra oc on opd.IdOrdenCompra = oc.IdOrdenCompra";
                    queryString += " where opd.PluPrv = " + String.valueOf(container.getContainerProperty(plusGrid.getSelectedRow(), PLU_PROPERTY).getValue());
                    queryString += " and oc.IdProveedor = " + String.valueOf(container.getContainerProperty(plusGrid.getSelectedRow(), ID_PROVEEDOR_PROPERTY).getValue());

                    try {

                        stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                        rsRecords = stQuery.executeQuery(queryString);

                        if (rsRecords.next()) {
                            Notification.show("La cuenta seleccionada contiene movimientos en ordenes de compra, no se puede eliminar.", Notification.Type.ERROR_MESSAGE);
                        } else {

                            queryString = " delete from proveedor_plu";
                            queryString += " where Id = " + String.valueOf(container.getContainerProperty(plusGrid.getSelectedRow(), ID_PROPERTY).getValue());

                            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                            stQuery.executeUpdate(queryString);

                            Notification.show("Registro eliminado con exito!", Notification.Type.HUMANIZED_MESSAGE);

                            llenarTabla();

                        }
                    } catch (SQLException ex) {
                        System.out.println("Error al buscar registros en ordenes de compra" + ex);
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
                if (plusGrid.getHeightByRows() > 0) {
                    TableHolder tableHolder = new DefaultTableHolder(plusGrid);
                    ExcelExport excelExport = new ExcelExport(tableHolder);
                    excelExport.excludeCollapsedColumns();
                    excelExport.setDisplayTotals(false);
                    String fileexport;
                    fileexport = "ProveedorPlus_.xls";
                    excelExport.setExportFileName(fileexport);
                    excelExport.export();
                }
            }
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setMargin(false);
        buttonsLayout.addComponent(editBtn);
        buttonsLayout.addComponent(newBtn);
        buttonsLayout.addComponent(eliminarBtn);
        buttonsLayout.addComponent(exportExcelBtn);

        mainLayout.addComponent(buttonsLayout);
        mainLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);
    }

    public void llenarTabla() {
        container.removeAllItems();

        queryString = "  select plu.*, ccc.CodigoCuentaCentroCosto, ccc.Descripcion DescripcionCCC, prv.Nombre NombreProveedor";
        queryString += " from proveedor_plu plu";
        queryString += " inner join centro_costo_cuenta ccc on ccc.IdCuentaCentroCosto = plu.IdCuentaCentroCosto";
        queryString += " inner join proveedor prv on plu.IdProveedor = prv.IdProveedor";

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado                                                
                do {
                    Object itemId = container.addItem();
                    container.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords.getString("Id"));
                    container.getContainerProperty(itemId, ID_PROVEEDOR_PROPERTY).setValue(rsRecords.getString("IdProveedor"));
                    container.getContainerProperty(itemId, PROVEEDOR_PROPERTY).setValue(rsRecords.getString("NombreProveedor"));
                    container.getContainerProperty(itemId, NO_CUENTA_PROPERTY).setValue(rsRecords.getString("CodigoCuentaCentroCosto"));
                    container.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(rsRecords.getString("DescripcionCCC"));
                    container.getContainerProperty(itemId, PLU_PROPERTY).setValue(rsRecords.getString("PLU"));
                    container.getContainerProperty(itemId, DESCRIPCIONPRV_PROPERTY).setValue(rsRecords.getString("DescripcionProveedor"));
                    container.getContainerProperty(itemId, CANTIDAD_PROPERTY).setValue(rsRecords.getString("Cantidad"));
                    container.getContainerProperty(itemId, PRECION_PROPERTY).setValue(rsRecords.getString("Precio"));

                } while (rsRecords.next());
            }
        } catch (Exception ex) {
            System.out.println("Error al listar tabla nomenclatura contable en CuentasContablesView:" + ex);
            ex.printStackTrace();
        }
    }

//    @Override
//    public void enter(ViewChangeListener.ViewChangeEvent event) {
//        Page.getCurrent().setTitle("Sopdi - PLUs de proveedores");
//    }

}
