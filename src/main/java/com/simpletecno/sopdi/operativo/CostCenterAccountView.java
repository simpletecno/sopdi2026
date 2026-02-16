/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.operativo;

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
import com.vaadin.ui.Grid.HeaderCell;
import com.vaadin.ui.Grid.HeaderRow;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vaadin.dialogs.ConfirmDialog;

/**
 *
 * @author JAguirre
 */
@SuppressWarnings("serial")
public class CostCenterAccountView extends VerticalLayout implements View {

    public Statement stQuery = null;
    public ResultSet rsRecords = null;

    static final String ID_PROPERTY = "Id";
    static final String CENTRO_COSTO_PROPERTY = "Código";
    static final String DESCRIPCION_PROPERTY = "Descripción";
    static final String GRUPO_PROPERTY = "Grupo";
    static final String CLASIFICACION_PROPERTY = "Clasificación";
    static final String TIPO_PROPERTY = "Tipo";
    static final String UNIDAD_PROPERTY = "Medida";
    static final String ESTATUS_PROPERTY = "Estatus";

    MarginInfo marginInfo;

    public IndexedContainer container = new IndexedContainer();
    Grid costCenterAccountTrackGrid;

    UI mainUI;

    String queryString = "";

    public CostCenterAccountView() {
        this.mainUI = UI.getCurrent();

        Responsive.makeResponsive(this);
        setMargin(true);
        setSpacing(true);

        marginInfo = new MarginInfo(true, true, false, true);

        Label titleLbl = new Label("Cuentas de centros de costo");
        titleLbl.addStyleName(ValoTheme.LABEL_H1);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h1_custom");

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setWidth("100%");

        titleLayout.addComponents(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.TOP_LEFT);

        addComponent(titleLayout);
        setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        createCostCenterGrid();

        createButtons();

        fillCostCenterGrid();

    }

    private void createCostCenterGrid() {

        HorizontalLayout reportLayout = new HorizontalLayout();
        reportLayout.setWidth("98%");
        reportLayout.addStyleName("rcorners3");
        reportLayout.setResponsive(true);
        reportLayout.setMargin(true);

        container.addContainerProperty(ID_PROPERTY, String.class, null);
        container.addContainerProperty(CENTRO_COSTO_PROPERTY, String.class, null);
        container.addContainerProperty(DESCRIPCION_PROPERTY, String.class, null);
        container.addContainerProperty(GRUPO_PROPERTY, String.class, null);
        container.addContainerProperty(CLASIFICACION_PROPERTY, String.class, null);
        container.addContainerProperty(TIPO_PROPERTY, String.class, null);
        container.addContainerProperty(UNIDAD_PROPERTY, String.class, null);
        container.addContainerProperty(ESTATUS_PROPERTY, String.class, null);

        costCenterAccountTrackGrid = new Grid("Listado de cuentas de centros de costo", container);

        costCenterAccountTrackGrid.setImmediate(true);
        costCenterAccountTrackGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        costCenterAccountTrackGrid.setDescription("Seleccione un registro.");
        costCenterAccountTrackGrid.setHeightMode(HeightMode.ROW);
        costCenterAccountTrackGrid.setHeightByRows(12);
        costCenterAccountTrackGrid.setWidth("100%");
        costCenterAccountTrackGrid.setResponsive(true);
        costCenterAccountTrackGrid.setEditorBuffered(false);

        reportLayout.addComponent(costCenterAccountTrackGrid);
        reportLayout.setComponentAlignment(costCenterAccountTrackGrid, Alignment.MIDDLE_CENTER);

        costCenterAccountTrackGrid.getColumn(ID_PROPERTY).setHidable(true).setHidden(true);
        costCenterAccountTrackGrid.getColumn(ID_PROPERTY).setMaximumWidth(10);
        costCenterAccountTrackGrid.getColumn(CENTRO_COSTO_PROPERTY).setMaximumWidth(100);
        costCenterAccountTrackGrid.getColumn(GRUPO_PROPERTY).setMaximumWidth(100);
        costCenterAccountTrackGrid.getColumn(CLASIFICACION_PROPERTY).setMaximumWidth(200);
        costCenterAccountTrackGrid.getColumn(UNIDAD_PROPERTY).setMaximumWidth(90);

        HeaderRow filterRow = costCenterAccountTrackGrid.appendHeaderRow();

        HeaderCell cell = filterRow.getCell(CENTRO_COSTO_PROPERTY);

        TextField filterField = new TextField();
        filterField.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField.setInputPrompt("Filtrar");
        filterField.setColumns(8);

        filterField.addTextChangeListener(change -> {
            container.removeContainerFilters(CENTRO_COSTO_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(
                        new SimpleStringFilter(CENTRO_COSTO_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell.setComponent(filterField);

        HeaderCell cell1 = filterRow.getCell(DESCRIPCION_PROPERTY);

        TextField filterField1 = new TextField();
        filterField1.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField1.setInputPrompt("Filtrar");
        filterField1.setColumns(20);

        filterField1.addTextChangeListener(change -> {
            container.removeContainerFilters(DESCRIPCION_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(
                        new SimpleStringFilter(DESCRIPCION_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell1.setComponent(filterField1);

        HeaderCell cell2 = filterRow.getCell(GRUPO_PROPERTY);

        TextField filterField2 = new TextField();
        filterField2.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField2.setInputPrompt("Filtrar");
        filterField2.setColumns(8);

        filterField2.addTextChangeListener(change -> {
            container.removeContainerFilters(GRUPO_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(
                        new SimpleStringFilter(GRUPO_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell2.setComponent(filterField2);

        HeaderCell cell3 = filterRow.getCell(CLASIFICACION_PROPERTY);

        TextField filterField3 = new TextField();
        filterField3.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField3.setInputPrompt("Filtrar");
        filterField3.setColumns(15);

        filterField3.addTextChangeListener(change -> {
            container.removeContainerFilters(CLASIFICACION_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(
                        new SimpleStringFilter(CLASIFICACION_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell3.setComponent(filterField3);

        addComponent(reportLayout);
        setComponentAlignment(reportLayout, Alignment.TOP_CENTER);
    }

    private void createButtons() {

        Button editBtn = new Button("Editar");
        editBtn.setIcon(FontAwesome.EDIT);
        editBtn.setWidth(140, Sizeable.UNITS_PIXELS);
        editBtn.setDescription("Actualizar datos");
        editBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (costCenterAccountTrackGrid.getSelectedRow() == null) {
                    Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
                } else {
                    try {

                        queryString = "SELECT * FROM contabilidad_partida";
                        queryString += " WHERE IdCentroCosto = " + String.valueOf(container.getContainerProperty(costCenterAccountTrackGrid.getSelectedRow(), ID_PROPERTY).getValue());

                        stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                        rsRecords = stQuery.executeQuery(queryString);

                        if (rsRecords.next()) {
                            Notification.show("No se puede editar este centro de costo debido a que tiene historial de compras.", Notification.Type.WARNING_MESSAGE);
                        } else {
                            CostCenterAccountForm costCenterAccountForm
                                    = new CostCenterAccountForm();
                            costCenterAccountForm.idCentroCostoTxt.setReadOnly(false);
                            costCenterAccountForm.idCentroCostoTxt.setValue(String.valueOf(container.getContainerProperty(costCenterAccountTrackGrid.getSelectedRow(), ID_PROPERTY).getValue()));
                            costCenterAccountForm.idCentroCostoTxt.setReadOnly(true);
                            UI.getCurrent().addWindow(costCenterAccountForm);
                            costCenterAccountForm.setData(this);
                            costCenterAccountForm.center();
                            costCenterAccountForm.fillData();
                        }
                    } catch (Exception e) {
                        System.out.println("Error al intentar buscar el historial de compras del centro de costos seleccionado." + e);
                        e.printStackTrace();
                    }
                }
            }
        });

        Button newBtn = new Button("Nuevo");
        newBtn.setIcon(FontAwesome.PLUS);
        newBtn.setWidth(140, Sizeable.UNITS_PIXELS);
        newBtn.setDescription("Agregar nueva cuenta centro de costo");
        newBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                CostCenterAccountForm costCenterAccountForm
                        = new CostCenterAccountForm();
                costCenterAccountForm.idCentroCostoTxt.setReadOnly(false);
                costCenterAccountForm.idCentroCostoTxt.setValue("0");
                costCenterAccountForm.idCentroCostoTxt.setReadOnly(true);
                UI.getCurrent().addWindow(costCenterAccountForm);
                costCenterAccountForm.setData(this);
                costCenterAccountForm.center();
            }
        });

        Button deleteBtn = new Button("Eliminar");
        deleteBtn.setIcon(FontAwesome.TRASH);
        deleteBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        deleteBtn.setDescription("Eliminar el registro.");
        deleteBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (costCenterAccountTrackGrid.getSelectedRow() == null) {
                    Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
                } else {
                    try {
                        queryString = "SELECT * FROM contabilidad_partida";
                        queryString += " WHERE IdCentroCosto = " + String.valueOf(container.getContainerProperty(costCenterAccountTrackGrid.getSelectedRow(), ID_PROPERTY).getValue());

                        stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                        rsRecords = stQuery.executeQuery(queryString);

                        if (rsRecords.next()) {
                            Notification.show("No se puede eliminar este centro de costo debido a que tiene historial de compras.", Notification.Type.WARNING_MESSAGE);
                        } else {

                            ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de eliminar el registro?",
                                    "SI", "NO", new ConfirmDialog.Listener() {

                                public void onClose(ConfirmDialog dialog) {
                                    if (dialog.isConfirmed()) {
                                        Notification.show("NO DISPONIBLE EN ESTA VERSION", Notification.Type.WARNING_MESSAGE);
                                    }
                                }
                            });
                        }
                    } catch (Exception e) {
                        System.out.println("Error al momento de buscar historial del centro de costo seleccionado.." + e);
                        e.printStackTrace();
                    }
                }
            }
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setMargin(false);
        buttonsLayout.addComponent(editBtn);
        buttonsLayout.addComponent(newBtn);
        buttonsLayout.addComponent(deleteBtn);

        addComponent(buttonsLayout);
        setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);
    }

    public void fillCostCenterGrid() {

        if (container == null) {
            return;
        }

        container.removeAllItems();

        queryString = "Select *  ";
        queryString += " From centro_costo_cuenta ";
        queryString += " Where IdProyecto = " + ((SopdiUI) mainUI).sessionInformation.getStrProjectId();

        try {

            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado

                do {

                    Object itemId = container.addItem();

                    container.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords.getString("IdCuentaCentroCosto"));
                    container.getContainerProperty(itemId, CENTRO_COSTO_PROPERTY).setValue(rsRecords.getString("CodigoCuentaCentroCosto"));
                    container.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(rsRecords.getString("Descripcion"));
                    container.getContainerProperty(itemId, UNIDAD_PROPERTY).setValue(rsRecords.getString("UnidadMedida"));
                    container.getContainerProperty(itemId, GRUPO_PROPERTY).setValue(rsRecords.getString("Grupo"));
                    container.getContainerProperty(itemId, TIPO_PROPERTY).setValue(rsRecords.getString("Tipo"));
                    container.getContainerProperty(itemId, CLASIFICACION_PROPERTY).setValue(rsRecords.getString("Clasificacion"));
                    container.getContainerProperty(itemId, ESTATUS_PROPERTY).setValue(rsRecords.getString("Estatus"));

                } while (rsRecords.next());

                rsRecords.last();
                costCenterAccountTrackGrid.select(container.firstItemId());
            }
        } catch (Exception ex) {
            Logger.getLogger(CostCenterAccountView.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de CUENTAS DE CENTROS DE COSTO : " + ex.getMessage());
            Notification.show("Error al intentar leer registros de CUENTAS DE CENTROS DE COSTO..!", Notification.Type.ERROR_MESSAGE);
        }
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        // TODO Auto-generated method stub
        Page.getCurrent().setTitle("Sopdi - Cuentas de centros de costo");
    }
}
