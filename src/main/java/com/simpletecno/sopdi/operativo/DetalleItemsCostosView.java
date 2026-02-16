package com.simpletecno.sopdi.operativo;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.data.Item;
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
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.dialogs.ConfirmDialog;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DetalleItemsCostosView extends VerticalLayout implements View {

    static final String ID_PROPERTY = "Id";
    static final String IDCC_PROPERTY = "IDCC";
    static final String IDEX_PROPERTY = "IDEX";
    static final String IDPROVEEDOR_PROPERTY = "IdProveedor";
    static final String PROVEEDOR_PROPERTY = "Proveedor";
    static final String CUENTA_PROPERTY = "No Cuenta";
    static final String AREA_PROPERTY = "Area";
    static final String DESCRIPCION_PROPERTY = "Descripción";
    static final String UNIDAD_PROPERTY = "Unidad";
    static final String CODITEMPRO_PROPERTY = "PLU";
    static final String DESITEMPRO_PROPERTY = "Descripción proveedor";
    static final String MODTIME_PRO0PERTY = "Modificado";

    public IndexedContainer container = new IndexedContainer();
    Grid detalleITemsCostosGrid;

    UI mainUI;
    Statement stQuery;
    ResultSet rsRecords;
    String queryString;

    public DetalleItemsCostosView() {
        this.mainUI = UI.getCurrent();
        this.setHeightUndefined();

        Label titleLbl = new Label("Detalle Items Costos PLU Proveedor");
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
        reportLayout.setHeight("100%");
        reportLayout.addStyleName("rcorners3");
        reportLayout.setResponsive(true);
        reportLayout.setMargin(true);

        container.addContainerProperty(ID_PROPERTY, String.class, null);
        container.addContainerProperty(IDCC_PROPERTY, String.class, null);
        container.addContainerProperty(IDEX_PROPERTY, String.class, null);
        container.addContainerProperty(IDPROVEEDOR_PROPERTY, String.class, null);
        container.addContainerProperty(PROVEEDOR_PROPERTY, String.class, null);
        container.addContainerProperty(CUENTA_PROPERTY, String.class, null);
        container.addContainerProperty(AREA_PROPERTY, String.class, null);
        container.addContainerProperty(DESCRIPCION_PROPERTY, String.class, null);
        container.addContainerProperty(UNIDAD_PROPERTY, String.class, null);
        container.addContainerProperty(CODITEMPRO_PROPERTY, String.class, null);
        container.addContainerProperty(DESITEMPRO_PROPERTY, String.class, null);
        container.addContainerProperty(MODTIME_PRO0PERTY, String.class, "");

        detalleITemsCostosGrid = new Grid("Listado de productos", container);
        detalleITemsCostosGrid.setImmediate(true);
        detalleITemsCostosGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        detalleITemsCostosGrid.setDescription("Doble click para selecciona un registro y editar.");
//        detalleITemsCostosGrid.setHeightMode(HeightMode.ROW);
//        detalleITemsCostosGrid.setHeightByRows(15);
        detalleITemsCostosGrid.setHeight("100%");
        detalleITemsCostosGrid.setWidth("100%");
        detalleITemsCostosGrid.setResponsive(true);
        detalleITemsCostosGrid.setEditorBuffered(true);
        detalleITemsCostosGrid.setEditorEnabled(true);

        detalleITemsCostosGrid.getColumn(ID_PROPERTY).setHidable(true).setHidden(true).setEditable(false);

        detalleITemsCostosGrid.getColumn(IDCC_PROPERTY).setEditable(false);
        detalleITemsCostosGrid.getColumn(IDEX_PROPERTY).setEditable(false);
        detalleITemsCostosGrid.getColumn(IDPROVEEDOR_PROPERTY).setEditable(false);
        detalleITemsCostosGrid.getColumn(PROVEEDOR_PROPERTY).setEditable(false);
        detalleITemsCostosGrid.getColumn(CUENTA_PROPERTY).setEditable(false);
        detalleITemsCostosGrid.getColumn(AREA_PROPERTY).setEditable(false);
        detalleITemsCostosGrid.getColumn(DESCRIPCION_PROPERTY).setEditable(false);
        detalleITemsCostosGrid.getColumn(UNIDAD_PROPERTY).setEditable(false);
        detalleITemsCostosGrid.getColumn(MODTIME_PRO0PERTY).setEditable(false).setHidable(true).setHidden(true);

        detalleITemsCostosGrid.getColumn(IDCC_PROPERTY).setExpandRatio(1);
        detalleITemsCostosGrid.getColumn(IDEX_PROPERTY).setExpandRatio(1);
        detalleITemsCostosGrid.getColumn(IDPROVEEDOR_PROPERTY).setExpandRatio(1);
        detalleITemsCostosGrid.getColumn(PROVEEDOR_PROPERTY).setExpandRatio(1);
        detalleITemsCostosGrid.getColumn(CUENTA_PROPERTY).setExpandRatio(1);
        detalleITemsCostosGrid.getColumn(AREA_PROPERTY).setExpandRatio(1);
        detalleITemsCostosGrid.getColumn(DESCRIPCION_PROPERTY).setExpandRatio(2);
        detalleITemsCostosGrid.getColumn(UNIDAD_PROPERTY).setExpandRatio(1);
        detalleITemsCostosGrid.getColumn(CODITEMPRO_PROPERTY).setExpandRatio(3);
        detalleITemsCostosGrid.getColumn(DESITEMPRO_PROPERTY).setExpandRatio(4);

        Grid.HeaderRow filterRow = detalleITemsCostosGrid.appendHeaderRow();

        Grid.HeaderCell cell = filterRow.getCell(IDCC_PROPERTY);
        TextField filterField = new TextField();
        filterField.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField.setInputPrompt("Filtrar");
        filterField.setColumns(3);
        filterField.addTextChangeListener(change -> {
            container.removeContainerFilters(IDCC_PROPERTY);
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(
                        new SimpleStringFilter(IDCC_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell.setComponent(filterField);

        Grid.HeaderCell cell2 = filterRow.getCell(CUENTA_PROPERTY);
        TextField filterField2 = new TextField();
        filterField2.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField2.setInputPrompt("Filtrar");
        filterField2.setColumns(7);

        filterField2.addTextChangeListener(change -> {
            container.removeContainerFilters(CUENTA_PROPERTY);
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(
                        new SimpleStringFilter(CUENTA_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell2.setComponent(filterField2);

        Grid.HeaderCell cell3 = filterRow.getCell(IDEX_PROPERTY);
        TextField filterField3 = new TextField();
        filterField3.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField3.setInputPrompt("Filtrar");
        filterField3.setColumns(5);

        filterField3.addTextChangeListener(change -> {
            container.removeContainerFilters(IDEX_PROPERTY);
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(
                        new SimpleStringFilter(IDEX_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell3.setComponent(filterField3);
        
        Grid.HeaderCell cell4 = filterRow.getCell(DESCRIPCION_PROPERTY);
        TextField filterField4 = new TextField();
        filterField4.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField4.setInputPrompt("Filtrar");
        filterField4.setColumns(15);

        filterField4.addTextChangeListener(change -> {
            container.removeContainerFilters(DESCRIPCION_PROPERTY);
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(
                        new SimpleStringFilter(DESCRIPCION_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell4.setComponent(filterField4);

        Grid.HeaderCell cell5 = filterRow.getCell(PROVEEDOR_PROPERTY);
        TextField filterField5 = new TextField();
        filterField5.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField5.setInputPrompt("Filtrar");
        filterField5.setColumns(15);

        filterField5.addTextChangeListener(change -> {
            container.removeContainerFilters(PROVEEDOR_PROPERTY);
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(
                        new SimpleStringFilter(PROVEEDOR_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell5.setComponent(filterField5);

        Grid.HeaderCell cell6 = filterRow.getCell(IDPROVEEDOR_PROPERTY);
        TextField filterField6 = new TextField();
        filterField6.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField6.setInputPrompt("Filtrar");
        filterField6.setColumns(5);

        filterField6.addTextChangeListener(change -> {
            container.removeContainerFilters(IDPROVEEDOR_PROPERTY);
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(
                        new SimpleStringFilter(IDPROVEEDOR_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell6.setComponent(filterField6);

        Grid.HeaderCell cell7 = filterRow.getCell(AREA_PROPERTY);
        TextField filterField7 = new TextField();
        filterField7.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField7.setInputPrompt("Filtrar");
        filterField7.setColumns(3);

        filterField7.addTextChangeListener(change -> {
            container.removeContainerFilters(AREA_PROPERTY);
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(
                        new SimpleStringFilter(AREA_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell7.setComponent(filterField7);

        reportLayout.addComponent(detalleITemsCostosGrid);
        reportLayout.setComponentAlignment(detalleITemsCostosGrid, Alignment.MIDDLE_CENTER);

        addComponent(reportLayout);
        setComponentAlignment(reportLayout, Alignment.TOP_CENTER);
    }

    private void createButtons() {

        Button saveBtn = new Button("Guardar cambios");
        saveBtn.setIcon(FontAwesome.SAVE);
        saveBtn.setDescription("Guardar cambios.");
        saveBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {

                try {
                    for (Object itemId : container.getItemIds()) {
                        if (container.getContainerProperty(itemId, CODITEMPRO_PROPERTY).getValue() != null) {
                            queryString = "UPDATE DetalleItemsCostos SET";
                            queryString += " CodItemPro = '" + container.getContainerProperty(itemId, CODITEMPRO_PROPERTY).getValue() + "'";
                            queryString += ",DesItemPro = '" + container.getContainerProperty(itemId, DESITEMPRO_PROPERTY).getValue() + "'";
                            queryString += " WHERE      Idcc = '" + container.getContainerProperty(itemId, IDCC_PROPERTY).getValue() + "'";
                            queryString += " AND        Idex = '" + container.getContainerProperty(itemId, IDEX_PROPERTY).getValue() + "'";
                            queryString += " AND    NoCuenta = '" + container.getContainerProperty(itemId, CUENTA_PROPERTY).getValue() + "'";
                            queryString += " AND      IdArea =  " + container.getContainerProperty(itemId, AREA_PROPERTY).getValue();
                            queryString += " AND      Unidad = '" + container.getContainerProperty(itemId, UNIDAD_PROPERTY).getValue() + "'";
                            queryString += " AND IdProveedor =  " + container.getContainerProperty(itemId, IDPROVEEDOR_PROPERTY).getValue();
                            queryString += " AND IdEmpresa   =  " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();

                            stQuery.executeUpdate(queryString);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Notification.show("Error al actualizar DIC..!", Notification.Type.ERROR_MESSAGE);
                    return;
                }

                Notification.show("ACTUALIZACION EXITOSA DIC..!", Notification.Type.HUMANIZED_MESSAGE);

            }
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setWidth("100%");
        buttonsLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        buttonsLayout.setSpacing(true);
        buttonsLayout.setMargin(false);
        buttonsLayout.addComponent(saveBtn);

        addComponent(buttonsLayout);
        setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);
    }

    public void llenarTablaProductos() {

        container.removeAllItems();

        queryString = "  SELECT DISTINCT DITEMC.Idcc, DITEMC.Idex, DITEMC.IdProveedor, DITEMC.NoCuenta,";
        queryString += " DITEMC.IdArea, DITEMC.Descripcion, DITEMC.Unidad, DITEMC.CodItemPro, DITEMC.DesItemPro, PROV.Nombre NombreProveedor ";
        queryString += " FROM DetalleItemsCostos DITEMC";
        queryString += " Inner Join project PROJ On PROJ.Numero = DITEMC.IdProject And PROJ.Estatus = 'ACTIVO'";
        queryString += " Left Join proveedor PROV On PROV.IdProveedor = DITEMC.IdProveedor";
        queryString += " Where DITEMC.IdEmpresa = " + ((SopdiUI)UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
        queryString += " And DITEMC.Tipo = 'INTINI'";

        Logger.getLogger(DetalleItemsCostosView.class.getName()).log(Level.INFO, queryString);

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado                                                
                do {
                    Object itemId = container.addItem();

                    container.getContainerProperty(itemId, IDCC_PROPERTY).setValue(rsRecords.getString("IDCC"));
                    container.getContainerProperty(itemId, IDEX_PROPERTY).setValue(rsRecords.getString("IDEx"));
                    container.getContainerProperty(itemId, IDPROVEEDOR_PROPERTY).setValue(rsRecords.getString("IdProveedor"));
                    container.getContainerProperty(itemId, PROVEEDOR_PROPERTY).setValue(rsRecords.getString("NombreProveedor"));
                    container.getContainerProperty(itemId, CUENTA_PROPERTY).setValue(rsRecords.getString("NoCuenta"));
                    container.getContainerProperty(itemId, AREA_PROPERTY).setValue(rsRecords.getString("IdArea"));
                    container.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(rsRecords.getString("Descripcion"));
                    container.getContainerProperty(itemId, UNIDAD_PROPERTY).setValue(rsRecords.getString("Unidad"));
                    container.getContainerProperty(itemId, CODITEMPRO_PROPERTY).setValue(rsRecords.getString("CodItemPro"));
                    container.getContainerProperty(itemId, DESITEMPRO_PROPERTY).setValue(rsRecords.getString("DesItemPro"));

                } while (rsRecords.next());
            }
        } catch (Exception ex) {
            System.out.println("Error al listar tabla DetalleItemsCostos :" + ex);
            ex.printStackTrace();
        }
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - Catalogo Productos DetalleItemsCostos");
    }

}
