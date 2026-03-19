package com.simpletecno.sopdi.inventario;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Responsive;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vaadin.dialogs.ConfirmDialog;

public class InvProductoView extends VerticalLayout implements View {

    static final String ID_PROPERTY = "Id";
    static final String GRUPO_PROPERTY = "Grupo";
    static final String N1_PROPERTY = "N1";
    static final String GRUPO2_PROPERTY = "Grupo 2";
    static final String N2_PROPERTY = "N2";
    static final String GRUPO3_PROPERTY = "Grupo 3";
    static final String NOCUENTA_PROPERTY = "No Cuenta";
    static final String DESCRIPCION_PROPERTY = "Descripción";
    static final String UNIDAD_PROPERTY = "Unidad";
    static final String CODIGO_PROPERTY = "Codigo";
    static final String PRECIO_PROPERTY = "Precio";
    static final String ESTATUS_PROPERTY = "Estatus";

    public IndexedContainer container = new IndexedContainer();
    Grid productoGrid;

    UI mainUI;
    Statement stQuery;
    ResultSet rsRecords;
    String queryString;

    public InvProductoView() {
        this.mainUI = UI.getCurrent();

        Label titleLbl = new Label("Catalogo de productos");
        titleLbl.addStyleName(ValoTheme.LABEL_H1);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h1_custom");

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setMargin(new MarginInfo(false, true, false, true));
        titleLayout.setSpacing(true);
        titleLayout.setWidth("100%");

        titleLayout.addComponents(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.TOP_LEFT);

        addComponent(titleLayout);
        setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        Responsive.makeResponsive(this);
        setMargin(true);
        setSpacing(true);

        createTablaProductos();
        llenarTablaProductos();
        createButtons();

    }

    public void createTablaProductos() {

        HorizontalLayout reportLayout = new HorizontalLayout();
        reportLayout.setWidth("100%");
        reportLayout.addStyleName("rcorners3");
        reportLayout.setResponsive(true);
        reportLayout.setMargin(true);

        container.addContainerProperty(ID_PROPERTY, String.class, null);
        container.addContainerProperty(GRUPO_PROPERTY, String.class, null);
        container.addContainerProperty(N1_PROPERTY, String.class, null);
        container.addContainerProperty(GRUPO2_PROPERTY, String.class, null);
        container.addContainerProperty(N2_PROPERTY, String.class, null);
        container.addContainerProperty(GRUPO3_PROPERTY, String.class, null);
        container.addContainerProperty(NOCUENTA_PROPERTY, String.class, null);
        container.addContainerProperty(DESCRIPCION_PROPERTY, String.class, null);
        container.addContainerProperty(UNIDAD_PROPERTY, String.class, null);
        container.addContainerProperty(CODIGO_PROPERTY, String.class, null);
        container.addContainerProperty(PRECIO_PROPERTY, String.class, null);
        container.addContainerProperty(ESTATUS_PROPERTY, String.class, null);

        productoGrid = new Grid("Listado de productos", container);
        productoGrid.setImmediate(true);
        productoGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        productoGrid.setDescription("Seleccione un registro.");
        productoGrid.setHeightMode(HeightMode.ROW);
        productoGrid.setHeightByRows(15);
        productoGrid.setWidth("100%");
        productoGrid.setResponsive(true);
        productoGrid.setEditorBuffered(false);

        productoGrid.getColumn(ID_PROPERTY).setHidable(true).setHidden(true);
        
        productoGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if (PRECIO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }
        });
        
        Grid.HeaderRow filterRow = productoGrid.appendHeaderRow();

        Grid.HeaderCell cell = filterRow.getCell(N1_PROPERTY);
        TextField filterField = new TextField();
        filterField.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField.setInputPrompt("Filtrar");
        filterField.setColumns(10);
        filterField.addTextChangeListener(change -> {
            container.removeContainerFilters(N1_PROPERTY);
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(
                        new SimpleStringFilter(N1_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell.setComponent(filterField);

        Grid.HeaderCell cell2 = filterRow.getCell(NOCUENTA_PROPERTY);
        TextField filterField2 = new TextField();
        filterField2.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField2.setInputPrompt("Filtrar");
        filterField2.setColumns(10);

        filterField2.addTextChangeListener(change -> {
            container.removeContainerFilters(NOCUENTA_PROPERTY);
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(
                        new SimpleStringFilter(NOCUENTA_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell2.setComponent(filterField2);

        Grid.HeaderCell cell3 = filterRow.getCell(N2_PROPERTY);
        TextField filterField3 = new TextField();
        filterField3.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField3.setInputPrompt("Filtrar");
        filterField3.setColumns(10);

        filterField3.addTextChangeListener(change -> {
            container.removeContainerFilters(N2_PROPERTY);
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(
                        new SimpleStringFilter(N2_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell3.setComponent(filterField3);
        
        Grid.HeaderCell cell4 = filterRow.getCell(DESCRIPCION_PROPERTY);
        TextField filterField4 = new TextField();
        filterField4.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField4.setInputPrompt("Filtrar");
        filterField4.setColumns(10);

        filterField4.addTextChangeListener(change -> {
            container.removeContainerFilters(DESCRIPCION_PROPERTY);
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(
                        new SimpleStringFilter(DESCRIPCION_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell4.setComponent(filterField4);
        
        Grid.HeaderCell cell5 = filterRow.getCell(ESTATUS_PROPERTY);
        TextField filterField5 = new TextField();
        filterField5.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField5.setInputPrompt("Filtrar");
        filterField5.setColumns(10);

        filterField5.addTextChangeListener(change -> {
            container.removeContainerFilters(ESTATUS_PROPERTY);
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(
                        new SimpleStringFilter(ESTATUS_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell5.setComponent(filterField5);

        reportLayout.addComponent(productoGrid);
        reportLayout.setComponentAlignment(productoGrid, Alignment.MIDDLE_CENTER);

        addComponent(reportLayout);
        setComponentAlignment(reportLayout, Alignment.TOP_CENTER);
    }

    private void createButtons() {

        Button editBtn = new Button("Editar");
        editBtn.setIcon(FontAwesome.EDIT);
        editBtn.setWidth(140, Sizeable.UNITS_PIXELS);
        editBtn.setDescription("Actualizar datos");
        editBtn.addListener((Button.ClickListener) (Button.ClickEvent event) -> {
            try {
                if (productoGrid.getSelectedRow() == null) {
                    Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
                } else {
                    ProductoForm productoForm = new ProductoForm(String.valueOf(container.getContainerProperty(productoGrid.getSelectedRow(), ID_PROPERTY).getValue()));
                    UI.getCurrent().addWindow(productoForm);
                    productoForm.center();
                }

            } catch (Exception ex) {
                System.out.println("Error en el boton editar" + ex);
                ex.printStackTrace();
            }
        });

        Button newBtn = new Button("Nuevo");
        newBtn.setIcon(FontAwesome.PLUS);
        newBtn.setWidth(140, Sizeable.UNITS_PIXELS);
        newBtn.setDescription("Agregar nuevo producto.");
        newBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                ProductoForm productoForm = new ProductoForm("");
                UI.getCurrent().addWindow(productoForm);
                productoForm.center();
            }
        });

        Button eliminarBtn = new Button("Eliminar");
        eliminarBtn.setIcon(FontAwesome.TRASH);
        eliminarBtn.setDescription("Eliminar Producto.");
        eliminarBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (productoGrid.getSelectedRow() == null) {
                    Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
                } else {

                    ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de Finalizar la conciliación?",
                            "SI", "NO", new ConfirmDialog.Listener() {

                        public void onClose(ConfirmDialog dialog) {
                            if (dialog.isConfirmed()) {

                                try {
                                    queryString = " DELETE FROM inv_producto ";
                                    queryString += " WHERE IdProducto = " + String.valueOf(container.getContainerProperty(productoGrid.getSelectedRow(), ID_PROPERTY).getValue());

                                    stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                                    stQuery.executeUpdate(queryString);

                                    Notification.show("Producto eliminado con exito!", Notification.Type.HUMANIZED_MESSAGE);

                                    llenarTablaProductos();
                                } catch (SQLException ex) {
                                    Logger.getLogger(InvProductoView.class.getName()).log(Level.SEVERE, null, ex);
                                }

                            }
                        }
                    });

                }
            }
        });

        Button asignarProveedorBtn = new Button("Asignar proveedor");
        asignarProveedorBtn.setIcon(FontAwesome.HAND_O_UP);
        asignarProveedorBtn.setDescription("Asigne un proveedor al producto.");
        asignarProveedorBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (productoGrid.getSelectedRow() == null) {
                    Notification.show("Por favor, seleccione un producto.", Notification.Type.WARNING_MESSAGE);
                } else {

                    AsignarProveedorProductoForm asignacionProductos
                            = new AsignarProveedorProductoForm(String.valueOf(container.getContainerProperty(productoGrid.getSelectedRow(), ID_PROPERTY).getValue()),
                                    String.valueOf(container.getContainerProperty(productoGrid.getSelectedRow(), DESCRIPCION_PROPERTY).getValue()),
                                    String.valueOf(container.getContainerProperty(productoGrid.getSelectedRow(), NOCUENTA_PROPERTY).getValue()),
                            "");
                    UI.getCurrent().addWindow(asignacionProductos);
                    asignacionProductos.center();
                }
            }
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setMargin(false);
        buttonsLayout.addComponent(editBtn);
        buttonsLayout.addComponent(newBtn);
        buttonsLayout.addComponent(eliminarBtn);
        buttonsLayout.addComponent(asignarProveedorBtn);

        addComponent(buttonsLayout);
        setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);
    }

    public void llenarTablaProductos() {

        container.removeAllItems();

        queryString = "  SELECT * ";
        queryString += " FROM inv_producto";

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado                                                
                do {
                    Object itemId = container.addItem();

                    container.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords.getString("IdProducto"));
                    container.getContainerProperty(itemId, GRUPO_PROPERTY).setValue(rsRecords.getString("Grupo"));
                    container.getContainerProperty(itemId, N1_PROPERTY).setValue(rsRecords.getString("N1"));
                    container.getContainerProperty(itemId, GRUPO2_PROPERTY).setValue(rsRecords.getString("Grupo2"));
                    container.getContainerProperty(itemId, N2_PROPERTY).setValue(rsRecords.getString("N2"));
                    container.getContainerProperty(itemId, GRUPO3_PROPERTY).setValue(rsRecords.getString("Grupo3"));
                    container.getContainerProperty(itemId, NOCUENTA_PROPERTY).setValue(rsRecords.getString("NoCuenta"));
                    container.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(rsRecords.getString("Descripcion"));
                    container.getContainerProperty(itemId, UNIDAD_PROPERTY).setValue(rsRecords.getString("Unidad"));
                    container.getContainerProperty(itemId, CODIGO_PROPERTY).setValue(rsRecords.getString("CodigoBarras"));
                    container.getContainerProperty(itemId, PRECIO_PROPERTY).setValue(rsRecords.getString("PrecioReferencia"));
                    container.getContainerProperty(itemId, ESTATUS_PROPERTY).setValue(rsRecords.getString("Estatus"));

                } while (rsRecords.next());
            }
        } catch (Exception ex) {
            System.out.println("Error al listar tabla inventario productos :" + ex);
            ex.printStackTrace();
        }
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - Catalogo Productos");
    }

}
