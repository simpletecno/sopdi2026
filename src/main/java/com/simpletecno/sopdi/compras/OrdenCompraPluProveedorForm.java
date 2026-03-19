package com.simpletecno.sopdi.compras;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
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

    public IndexedContainer pluContainer = new IndexedContainer();
    Grid pluGrid;
    
    Button guardarBtn;

    UI mainUI;
    Statement stQuery;
    ResultSet rsRecords;

    String queryString;

    String idOrdenCompra;
    String noCuenta;
    String idArea;
    String idProveedor;

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public OrdenCompraPluProveedorForm(
            String idOrdenCompra,
            String noCuenta,
            String idArea,
            String idProveedor
    ) {
        this.mainUI = UI.getCurrent();
        this.idOrdenCompra = idOrdenCompra;
        this.noCuenta = noCuenta;
        this.idArea = idArea;
        this.idProveedor = idProveedor;

        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setResponsive(true);

        setContent(mainLayout);

        setResponsive(true);
        setWidth("50%");
        setHeight("60%");

        setModal(true);
        setClosable(true);

        Label titleLbl = new Label("PLUS DEL PROVEEDOR : " + idProveedor + "  CUENTA : " + noCuenta + " AREA : " + idArea);
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();

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
        pluContainer.addContainerProperty(PLU_PROPERTY, String.class, null);
        pluContainer.addContainerProperty(DESCRIPCION_PROVEEDOR, String.class, null);

        pluGrid = new Grid("", pluContainer);
        pluGrid.setWidth("100%");
        pluGrid.setEditorEnabled(true);
        pluGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        pluGrid.setDescription("Seleccione un registro.");
        pluGrid.setHeightMode(HeightMode.ROW);
        pluGrid.setHeightByRows(5);
        pluGrid.setResponsive(true);
        pluGrid.setEditorBuffered(true);
        pluGrid.addItemClickListener((event) -> {
            if (event != null) {
                pluGrid.editItem(event.getItemId());
            }
        });

        guardarBtn = new Button("Guardar");
        guardarBtn.setIcon(FontAwesome.SAVE);
        guardarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                save();
            }
        });

        mainLayout.addComponent(pluGrid);
        mainLayout.setComponentAlignment(pluGrid, Alignment.MIDDLE_CENTER);
        mainLayout.addComponent(guardarBtn);
        mainLayout.setComponentAlignment(guardarBtn, Alignment.MIDDLE_CENTER);
    }

    public void llenarTabla() {

        pluContainer.removeAllItems();

        queryString = "SELECT * ";
        queryString += " FROM orden_compra_detalle ";
        queryString += " WHERE IdOrdenCompra = " + idOrdenCompra;
        queryString += " AND NoCuenta = '" + noCuenta + "'";
        queryString += " AND IdArea = " + idArea;

Logger.getLogger(OrdenCompraPluProveedorForm.class.getName()).log(Level.INFO, "QueryPLUs=" + queryString);

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado
                do {

                    Object itemId = pluContainer.addItem();

                    pluContainer.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords.getString("Id"));
                    pluContainer.getContainerProperty(itemId, PLU_PROPERTY).setValue(rsRecords.getString("PluPrv"));
                    pluContainer.getContainerProperty(itemId, DESCRIPCION_PROVEEDOR).setValue(rsRecords.getString("PluPrvDes"));

                } while (rsRecords.next());
            }

        } catch (Exception ex) {
            System.out.println("Error al listar tabla de plus de proveedor en detalle de orden de compra : " + ex);
            ex.printStackTrace();
        }

    }

    public void save() {
        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            Iterator items = pluContainer.getItemIds().iterator();

            while (items.hasNext()) {
                Object itemId = items.next();
                String id = (String) pluContainer.getContainerProperty(itemId, ID_PROPERTY).getValue();
                String plu = (String) pluContainer.getContainerProperty(itemId, PLU_PROPERTY).getValue();
                String descripcion = (String) pluContainer.getContainerProperty(itemId, DESCRIPCION_PROVEEDOR).getValue();

                queryString = "UPDATE orden_compra_detalle SET ";
                queryString += " PluPrv = '" + plu + "', ";
                queryString += " PluPrvDes = '" + descripcion + "' ";
                queryString += " WHERE Id = " + id;

                stQuery.executeUpdate(queryString);

                Logger.getLogger(OrdenCompraPluProveedorForm.class.getName()).log(Level.INFO, "QueryUpdatePLUs=" + queryString);
            } //ENDWHILE

            Notification.show("Plus del proveedor actualizado con éxito.", Notification.Type.HUMANIZED_MESSAGE);
            this.close();
        } catch (SQLException ex) {
            Logger.getLogger(OrdenCompraPluProveedorForm.class.getName()).log(Level.SEVERE, null, ex);
            Notification.show("Error al actualizar plus del proveedor : " + ex, Notification.Type.ERROR_MESSAGE);
        }
    }
}