package com.simpletecno.sopdi.inventario;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Field;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Runo;
import com.vaadin.ui.themes.ValoTheme;
import java.sql.ResultSet;
import java.sql.Statement;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.ui.NumberField;

/**
 *
 * @author Administrador
 */
public class AsignarProveedorProductoForm extends Window {

    String idProducto;
    String nombreProducto;
    String noCuenta;
    String idProveedor;

    UI mainUI;
    Statement stQuery = null;
    ResultSet rsRecords;

    Statement stQuery2 = null;
    ResultSet rsRecords2;

    String queryString = "";

    VerticalLayout mainForm;

    public IndexedContainer proveedorContainer = new IndexedContainer();
    Grid proveedorGrid;
    public static final String ID_PROVEEDOR_PROPERTY = "Id Proveedor";
    public static final String NOMBRE_PROPERTY = "Nombre";
    public static final String PLU_PROPERTY = "PLU";
    public static final String PRECIO_PROPERTY = "Precio referencia";

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public AsignarProveedorProductoForm(String idProducto, String nombre, String noCuenta, String idProveedor) {

        this.mainUI = UI.getCurrent();
        this.idProducto = idProducto;
        this.nombreProducto = nombre;
        this.noCuenta = noCuenta;
        this.idProveedor = idProveedor;

        setWidth("60%");
        setHeight("92%");
        setResponsive(true);
        setModal(true);

        mainForm = new VerticalLayout();
        mainForm.setWidth("100%");
        mainForm.setMargin(true);
        mainForm.setSpacing(true);

        VerticalLayout titleLayout = new VerticalLayout();
        titleLayout.setResponsive(true);
        titleLayout.setMargin(new MarginInfo(true, false, false, false));
        titleLayout.setWidth("100%");

        Label titleLbl = new Label(empresaId + " " + empresaNombre + " FORMULARIO ASIGNACIÓN DE PROVEEDORES AL PRODUCTO ");
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName(Runo.LABEL_H2);

        Label titleLb2 = new Label("# " + noCuenta + " " + nombre.toUpperCase());
        titleLb2.setSizeUndefined();
        titleLb2.addStyleName(Runo.LABEL_H2);

        titleLayout.addComponent(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.BOTTOM_CENTER);

        titleLayout.addComponent(titleLb2);
        titleLayout.setComponentAlignment(titleLb2, Alignment.BOTTOM_CENTER);

        mainForm.addComponent(titleLayout);
        mainForm.setComponentAlignment(titleLayout, Alignment.BOTTOM_CENTER);

        setContent(mainForm);
        crearComponents();
        if (!idProveedor.trim().isEmpty()) {
            llenarDatos(); // EDITAR UN SOLO REGISTRO 
        } else {
            llenarTablaProveedores();
        }

    }

    public void crearComponents() {

        VerticalLayout detalleLayout = new VerticalLayout();
        detalleLayout.setWidth("100%");
        detalleLayout.addStyleName("rcorners3");
        detalleLayout.setSpacing(true);

        proveedorContainer.addContainerProperty(ID_PROVEEDOR_PROPERTY, String.class, null);
        proveedorContainer.addContainerProperty(NOMBRE_PROPERTY, String.class, null);
        proveedorContainer.addContainerProperty(PLU_PROPERTY, String.class, null);
        proveedorContainer.addContainerProperty(PRECIO_PROPERTY, String.class, null);

        proveedorGrid = new Grid("Listado de proveedores..", proveedorContainer);
        proveedorGrid.setImmediate(true);
        proveedorGrid.setSelectionMode(Grid.SelectionMode.NONE);
        proveedorGrid.setHeightMode(HeightMode.ROW);
        proveedorGrid.setHeightByRows(12);
        proveedorGrid.setWidth("100%");
        proveedorGrid.setResponsive(true);
        proveedorGrid.setEditorBuffered(false);
        proveedorGrid.setSizeFull();
        proveedorGrid.setEditorEnabled(true);
        proveedorGrid.getColumn(ID_PROVEEDOR_PROPERTY).setEditable(false);
        proveedorGrid.getColumn(NOMBRE_PROPERTY).setEditable(false);
        proveedorGrid.getColumn(PRECIO_PROPERTY).setEditorField(getAmmountField());
        proveedorGrid.getColumn(PLU_PROPERTY).setEditorField(getFieldTxt());
        proveedorGrid.addItemClickListener((event) -> {
            if (event != null) {
                proveedorGrid.editItem(event.getItemId());
            }
        });

        proveedorGrid.getColumn(ID_PROVEEDOR_PROPERTY).setHidden(true).setHidable(true);

        Grid.HeaderRow filterRow = proveedorGrid.appendHeaderRow();

        Grid.HeaderCell cell = filterRow.getCell(NOMBRE_PROPERTY);
        TextField filterField = new TextField();
        filterField.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField.setInputPrompt("Filtrar");
        filterField.setColumns(10);
        filterField.addTextChangeListener(change -> {
            proveedorContainer.removeContainerFilters(NOMBRE_PROPERTY);
            if (!change.getText().isEmpty()) {
                proveedorContainer.addContainerFilter(
                        new SimpleStringFilter(NOMBRE_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell.setComponent(filterField);

        HorizontalLayout idexYEmpleadosLayout = new HorizontalLayout();
        idexYEmpleadosLayout.setWidth("100%");
        idexYEmpleadosLayout.addStyleName("rcorners3");
        idexYEmpleadosLayout.setSpacing(true);

        idexYEmpleadosLayout.addComponent(proveedorGrid);

        Button actualizarBtn = new Button("Guardar");
        actualizarBtn.setIcon(FontAwesome.SAVE);
        actualizarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        actualizarBtn.setDescription("Actualizar todos los registros");
        actualizarBtn.addListener((Button.ClickListener) (Button.ClickEvent event) -> {
            ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de actualizar los registros",
                    "SI", "NO", new ConfirmDialog.Listener() {
                public void onClose(ConfirmDialog dialog) {
                    if (dialog.isConfirmed()) {
                        actualizarTablaProveedorProductos();
                    }
                }
            });
        });

        HorizontalLayout botonesLayout = new HorizontalLayout();
        botonesLayout.setSpacing(true);
        botonesLayout.setWidth("100%");

        botonesLayout.addComponents(actualizarBtn);
        botonesLayout.setComponentAlignment(actualizarBtn, Alignment.BOTTOM_CENTER);

        detalleLayout.addComponents(idexYEmpleadosLayout, botonesLayout);

        mainForm.addComponent(detalleLayout);
        mainForm.setComponentAlignment(detalleLayout, Alignment.MIDDLE_CENTER);
    }

    private Field<?> getAmmountField() {

        NumberField valueTxt = new NumberField("Monto :");
        valueTxt.setWidth("10em");
        valueTxt.setDecimalAllowed(true);
        valueTxt.setDecimalPrecision(2);
        valueTxt.setMinimumFractionDigits(2);
        valueTxt.setDecimalSeparator('.');
        valueTxt.setDecimalSeparatorAlwaysShown(true);
        valueTxt.setValue(0d);
        valueTxt.setGroupingUsed(true);
        valueTxt.setGroupingSeparator(',');
        valueTxt.setGroupingSize(3);
        valueTxt.setImmediate(true);
        valueTxt.selectAll();
        valueTxt.setDescription("Doble click para selecionar todo el monto...");
        valueTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);

        return valueTxt;
    }

    private Field<?> getFieldTxt() {

        TextField valueTxt = new TextField("PLU :");
        valueTxt.setWidth("8em");
        valueTxt.setDescription("Doble click para selecionar todo el monto...");

        return valueTxt;
    }

    public void llenarTablaProveedores() {
        try {
            proveedorContainer.removeAllItems();

            queryString = " SELECT *";
            queryString += " FROM proveedor_empresa";
            queryString += " WHERE EsProveedor = 1";
            queryString += " AND IdEmpresa = " + empresaId;

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery2 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado
                do {
                    queryString = " SELECT *";
                    queryString += " FROM proveedor_productos";
                    queryString += " WHERE IdProveedor = " + rsRecords.getString("IdProveedor");
                    queryString += " AND IdProducto = " + idProducto;

                    rsRecords2 = stQuery2.executeQuery(queryString);

                    Object itemId = proveedorContainer.addItem();
                    proveedorContainer.getContainerProperty(itemId, ID_PROVEEDOR_PROPERTY).setValue(rsRecords.getString("IdProveedor"));
                    proveedorContainer.getContainerProperty(itemId, NOMBRE_PROPERTY).setValue(rsRecords.getString("Nombre"));

                    if (rsRecords2.next()) {
                        proveedorContainer.getContainerProperty(itemId, PLU_PROPERTY).setValue(rsRecords2.getString("PLU"));
                        proveedorContainer.getContainerProperty(itemId, PRECIO_PROPERTY).setValue(rsRecords2.getString("Precio"));
                    } else {
                        proveedorContainer.getContainerProperty(itemId, PLU_PROPERTY).setValue("SIN PLU");
                        proveedorContainer.getContainerProperty(itemId, PRECIO_PROPERTY).setValue("0.00");
                    }

                } while (rsRecords.next());
            }
        } catch (Exception ex) {
            System.out.println("Error al listar tabla proveedores : " + ex);
            ex.printStackTrace();
        }
    }

    public void llenarDatos() {
        try {
            proveedorContainer.removeAllItems();

            queryString = " SELECT *, proveedor.Nombre AS nombreProveedor, proveedor.IdProveedor as IdProv";
            queryString += " FROM proveedor_productos";
            queryString += " INNER JOIN proveedor_empresa on proveedor_productos.IdProveedor = proveedor_empresa.IdProveedor";
            queryString += " WHERE proveedor_productos.IdProveedor = " + idProveedor;
            queryString += " AND IdProducto = " + idProducto;
            queryString += " AND proveedor_empresa.IdEmpresa = " + empresaId;
            
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                Object itemId = proveedorContainer.addItem();

                proveedorContainer.getContainerProperty(itemId, ID_PROVEEDOR_PROPERTY).setValue(rsRecords.getString("IdProv"));
                proveedorContainer.getContainerProperty(itemId, NOMBRE_PROPERTY).setValue(rsRecords.getString("nombreProveedor"));
                proveedorContainer.getContainerProperty(itemId, PLU_PROPERTY).setValue(rsRecords.getString("PLU"));
                proveedorContainer.getContainerProperty(itemId, PRECIO_PROPERTY).setValue(rsRecords.getString("proveedor_productos.Precio"));
            }
        } catch (Exception ex) {
            System.out.println("Error al listar tabla proveedores : " + ex);
            ex.printStackTrace();
        }
    }

    public void actualizarTablaProveedorProductos() {
        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            
            for (Object itemId : proveedorContainer.getItemIds()) {
                Item item = proveedorContainer.getItem(itemId);

                double precio = Double.valueOf(String.valueOf(item.getItemProperty(PRECIO_PROPERTY).getValue()));

                if (item.getItemProperty(PRECIO_PROPERTY) != null && precio > 0) {

                    queryString = "SELECT * FROM proveedor_productos";
                    queryString += " WHERE IdProveedor = " + String.valueOf(item.getItemProperty(ID_PROVEEDOR_PROPERTY).getValue());
                    queryString += " AND IdProducto = " + idProducto;
                    
                    rsRecords = stQuery.executeQuery(queryString);

                    if (rsRecords.next()) {
                        queryString = "  UPDATE proveedor_productos SET";
                        queryString += " PLU ='" + String.valueOf(item.getItemProperty(PLU_PROPERTY).getValue()) + "'";
                        queryString += ",Precio ="+ String.valueOf(item.getItemProperty(PRECIO_PROPERTY).getValue());
                        queryString += " WHERE Id =" + rsRecords.getString("Id");
                    } else {
                        queryString = "  INSERT INTO proveedor_productos(IdProveedor, IdProducto, PLU, PLUDescripcion, Precio) ";
                        queryString += " VALUES ";
                        queryString += "(";
                        queryString += String.valueOf(item.getItemProperty(ID_PROVEEDOR_PROPERTY).getValue());
                        queryString += "," + idProducto;
                        queryString += ",'" + String.valueOf(item.getItemProperty(PLU_PROPERTY).getValue()) + "'";
                        queryString += ",'" + nombreProducto + "'";
                        queryString += "," + String.valueOf(item.getItemProperty(PRECIO_PROPERTY).getValue());
                        queryString += ")";
                    }

                    stQuery.executeUpdate(queryString);

                }

                Notification notif = new Notification("REGISTROS ACTUALIZADOS CON EXITO!.", Notification.Type.HUMANIZED_MESSAGE);
                notif.setDelayMsec(1500);
                notif.setPosition(Position.MIDDLE_CENTER);
                notif.show(Page.getCurrent());
            }
        } catch (Exception e) {
            System.out.println("Error al insertar proveedor_productos " + e);
            e.printStackTrace();
        }
    }

}
