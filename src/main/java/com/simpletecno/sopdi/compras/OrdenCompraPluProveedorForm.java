package com.simpletecno.sopdi.compras;

import com.simpletecno.sopdi.MyDatabaseProvider;
import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.renderers.NumberRenderer;
import com.vaadin.ui.themes.ValoTheme;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author user
 */
public class OrdenCompraPluProveedorForm extends Window {

    VerticalLayout mainLayout;

    static final String ID_PROPERTY = "Id";
    static final String PLU_PROPERTY = "PLU";
    static final String DESCRIPCION_PROVEEDOR = "Descripción Proveedor";
    static final String CANTIDAD_PROPERTY = "Cantidad";
    static final String PRECIO_PROPERTY = "Precio";
    static final String TOTAL_PROPERTY = "Total";

    public IndexedContainer pluContainer = new IndexedContainer();
    Grid pluGrid;

    Button guardarBtn;

    UI mainUI;

    String idOrdenCompra;
    String noCuenta;
    String idArea;
    String idProveedor;
    String idcc;
    String idex;
    String idProject;

    public OrdenCompraPluProveedorForm(
            String idOrdenCompra,
            String noCuenta,
            String idArea,
            String idProveedor,
            String idcc,
            String idex,
            String idProject
    ) {
        addCloseShortcut(com.vaadin.event.ShortcutAction.KeyCode.ESCAPE, null);
        this.mainUI = UI.getCurrent();
        this.idOrdenCompra = idOrdenCompra;
        this.noCuenta = noCuenta;
        this.idArea = idArea;
        this.idProveedor = idProveedor;
        this.idcc = idcc;
        this.idex = idex;
        this.idProject = idProject;

        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setResponsive(true);

        setContent(mainLayout);

        setResponsive(true);
        setWidth("90%");
        setHeight("60%");

        setModal(true);
        setClosable(true);

        Label titleLbl = new Label("PLUs DEL PROVEEDOR : " + idProveedor + "  CUENTA : " + noCuenta
                + "  AREA : " + idArea + "  CC : " + idcc + "  IDEX : " + idex);
        titleLbl.addStyleName(ValoTheme.LABEL_H2);

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setResponsive(true);
        titleLayout.setSpacing(true);
        titleLayout.setWidth("100%");
        titleLayout.setMargin(false);
        titleLayout.addComponent(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.MIDDLE_CENTER);
        titleLayout.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

        mainLayout.addComponent(titleLayout);
        mainLayout.setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        createTabla();
        llenarTabla();
    }

    public void createTabla() {
        pluContainer.addContainerProperty(ID_PROPERTY, String.class, null);
        pluContainer.addContainerProperty(PLU_PROPERTY, String.class, "");
        pluContainer.addContainerProperty(DESCRIPCION_PROVEEDOR, String.class, "");
        pluContainer.addContainerProperty(CANTIDAD_PROPERTY, BigDecimal.class, BigDecimal.ZERO);
        pluContainer.addContainerProperty(PRECIO_PROPERTY, BigDecimal.class, BigDecimal.ZERO);
        pluContainer.addContainerProperty(TOTAL_PROPERTY, BigDecimal.class, BigDecimal.ZERO);

        pluGrid = new Grid("", pluContainer);
        pluGrid.setWidth("100%");
        pluGrid.setEditorEnabled(true);
        pluGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        pluGrid.setDescription("Seleccione un registro.");
        pluGrid.setHeightMode(HeightMode.ROW);
        pluGrid.setHeightByRows(5);
        pluGrid.setResponsive(true);
        pluGrid.setEditorBuffered(false);
        pluGrid.getColumn(ID_PROPERTY).setEditable(false).setHidden(true);
        pluGrid.getColumn(CANTIDAD_PROPERTY).setRenderer(new NumberRenderer("%1$,.4f"));
        pluGrid.getColumn(PRECIO_PROPERTY).setRenderer(new NumberRenderer("%1$,.4f"));
        pluGrid.getColumn(TOTAL_PROPERTY).setEditable(false).setRenderer(new NumberRenderer("%1$,.2f"));
        pluGrid.addItemClickListener(event -> pluGrid.editItem(event.getItemId()));

        guardarBtn = new Button("Guardar");
        guardarBtn.setIcon(FontAwesome.SAVE);
        guardarBtn.addClickListener(event -> save());

        mainLayout.addComponent(pluGrid);
        mainLayout.setComponentAlignment(pluGrid, Alignment.MIDDLE_CENTER);
        mainLayout.addComponent(guardarBtn);
        mainLayout.setComponentAlignment(guardarBtn, Alignment.MIDDLE_CENTER);
    }

    public void llenarTabla() {
        pluContainer.removeAllItems();

        String consulta = "SELECT Id, PluPrv, PluPrvDes, Cantidad, Precio, Total"
                + " FROM orden_compra_detalle_plu"
                + " WHERE IdOrdenCompra = ? AND NoCuenta = ? AND IdArea = ?"
                + " AND IDCC = ? AND IDEX = ? AND IdProject = ? ORDER BY Id";

        try (PreparedStatement statement = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().prepareStatement(consulta)) {
            statement.setString(1, idOrdenCompra);
            statement.setString(2, noCuenta);
            statement.setString(3, idArea);
            statement.setString(4, idcc);
            statement.setString(5, idex);
            statement.setString(6, idProject);
            try (ResultSet records = statement.executeQuery()) {
                while (records.next()) {
                    Object itemId = pluContainer.addItem();
                    pluContainer.getContainerProperty(itemId, ID_PROPERTY).setValue(records.getString("Id"));
                    pluContainer.getContainerProperty(itemId, PLU_PROPERTY).setValue(records.getString("PluPrv"));
                    pluContainer.getContainerProperty(itemId, DESCRIPCION_PROVEEDOR).setValue(records.getString("PluPrvDes"));
                    pluContainer.getContainerProperty(itemId, CANTIDAD_PROPERTY).setValue(records.getBigDecimal("Cantidad"));
                    pluContainer.getContainerProperty(itemId, PRECIO_PROPERTY).setValue(records.getBigDecimal("Precio"));
                    pluContainer.getContainerProperty(itemId, TOTAL_PROPERTY).setValue(records.getBigDecimal("Total"));

                    ((Property.ValueChangeNotifier) pluContainer.getContainerProperty(itemId, CANTIDAD_PROPERTY))
                            .addValueChangeListener(event -> recalcularTotal(itemId));
                    ((Property.ValueChangeNotifier) pluContainer.getContainerProperty(itemId, PRECIO_PROPERTY))
                            .addValueChangeListener(event -> recalcularTotal(itemId));
                }
            }
            if (pluContainer.size() == 0) {
                Notification.show("No hay PLUs guardados para esta distribución. Registre los productos del proveedor y vuelva a guardar la orden.",
                        Notification.Type.WARNING_MESSAGE);
            }
        } catch (SQLException exception) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Error al cargar PLUs de la orden", exception);
            Notification.show("Error al cargar los PLUs de la orden de compra.", Notification.Type.ERROR_MESSAGE);
        }
    }

    private BigDecimal valorDecimal(Object itemId, String propiedad) {
        BigDecimal valor = (BigDecimal) pluContainer.getContainerProperty(itemId, propiedad).getValue();
        return valor == null ? BigDecimal.ZERO : valor;
    }

    private void recalcularTotal(Object itemId) {
        BigDecimal total = valorDecimal(itemId, CANTIDAD_PROPERTY)
                .multiply(valorDecimal(itemId, PRECIO_PROPERTY)).setScale(2, RoundingMode.HALF_UP);
        pluContainer.getContainerProperty(itemId, TOTAL_PROPERTY).setValue(total);
    }

    public void save() {
        if (pluGrid.isEditorActive()) {
            try {
                pluGrid.saveEditor();
                pluGrid.cancelEditor();
            } catch (Exception exception) {
                Notification.show("Revise los valores del producto antes de guardar.", Notification.Type.WARNING_MESSAGE);
                return;
            }
        }

        for (Object itemId : pluContainer.getItemIds()) {
            if (valorDecimal(itemId, CANTIDAD_PROPERTY).signum() < 0
                    || valorDecimal(itemId, PRECIO_PROPERTY).signum() < 0) {
                Notification.show("La cantidad y el precio no pueden ser negativos.", Notification.Type.WARNING_MESSAGE);
                return;
            }
        }

        String actualizar = "UPDATE orden_compra_detalle_plu"
                + " SET PluPrv = ?, PluPrvDes = ?, Cantidad = ?, Precio = ?, Total = ?"
                + " WHERE Id = ? AND IdOrdenCompra = ? AND NoCuenta = ? AND IdArea = ?"
                + " AND IDCC = ? AND IDEX = ? AND IdProject = ?";

        try (Connection connection = new MyDatabaseProvider().getNewConnection()) {
            if (connection == null) {
                throw new SQLException("No se pudo conectar para guardar los PLUs.");
            }
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(actualizar)) {
                for (Object itemId : pluContainer.getItemIds()) {
                    statement.setString(1, String.valueOf(pluContainer.getContainerProperty(itemId, PLU_PROPERTY).getValue()));
                    statement.setString(2, String.valueOf(pluContainer.getContainerProperty(itemId, DESCRIPCION_PROVEEDOR).getValue()));
                    statement.setBigDecimal(3, valorDecimal(itemId, CANTIDAD_PROPERTY));
                    statement.setBigDecimal(4, valorDecimal(itemId, PRECIO_PROPERTY));
                    statement.setBigDecimal(5, valorDecimal(itemId, TOTAL_PROPERTY));
                    statement.setString(6, String.valueOf(pluContainer.getContainerProperty(itemId, ID_PROPERTY).getValue()));
                    statement.setString(7, idOrdenCompra);
                    statement.setString(8, noCuenta);
                    statement.setString(9, idArea);
                    statement.setString(10, idcc);
                    statement.setString(11, idex);
                    statement.setString(12, idProject);
                    statement.addBatch();
                }
                for (int actualizados : statement.executeBatch()) {
                    if (actualizados == 0) {
                        throw new SQLException("El detalle de la orden cambió. Cierre y vuelva a abrir los PLUs.");
                    }
                }
                connection.commit();
            } catch (SQLException exception) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    exception.addSuppressed(rollbackException);
                }
                throw exception;
            }

            Notification.show("PLUs de la orden actualizados con éxito.", Notification.Type.HUMANIZED_MESSAGE);
            close();
        } catch (SQLException exception) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Error al actualizar PLUs de la orden", exception);
            Notification.show("Error al actualizar los PLUs del proveedor.", Notification.Type.ERROR_MESSAGE);
        }
    }
}
