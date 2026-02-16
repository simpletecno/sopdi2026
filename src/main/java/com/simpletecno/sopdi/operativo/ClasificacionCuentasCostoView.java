package com.simpletecno.sopdi.operativo;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
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
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author user
 */
public class ClasificacionCuentasCostoView extends VerticalLayout implements View {

    static final String ID_CLASIFICACION_PROPERTY = "Id Clasificación";
    static final String DESCRIPCION_PROPERTY = "Descripción";

    MarginInfo marginInfo;

    public IndexedContainer container = new IndexedContainer();
    Grid clasificacionGrid;

    UI mainUI;
    Statement stQuery;
    ResultSet rsRecords;
    String queryString;

    public ClasificacionCuentasCostoView() {
        this.mainUI = UI.getCurrent();
        Label titleLbl = new Label("CLASIFICACION DE CENTRO DE CUENTAS COSTO");
        titleLbl.addStyleName(ValoTheme.LABEL_H1);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h1_custom");

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setMargin(true);
        titleLayout.setSpacing(true);

        titleLayout.setWidth("100%");

        titleLayout.addComponents(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.TOP_LEFT);

        addComponent(titleLayout);
        setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        Responsive.makeResponsive(this);
        setMargin(true);
        setSpacing(true);

        createTablaCuentasContables();
        llenarTablaCalificacion();
        createButtons();

    }

    public void createTablaCuentasContables() {
        HorizontalLayout reportLayout = new HorizontalLayout();
        reportLayout.setWidth("98%");
        reportLayout.addStyleName("rcorners3");
        reportLayout.setResponsive(true);
        reportLayout.setMargin(true);

        container.addContainerProperty(ID_CLASIFICACION_PROPERTY, String.class, null);
        container.addContainerProperty(DESCRIPCION_PROPERTY, String.class, null);

        clasificacionGrid = new Grid("Listado de clasificación", container);

        clasificacionGrid.setImmediate(true);
        clasificacionGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        clasificacionGrid.setDescription("Seleccione un registro.");
        clasificacionGrid.setHeightMode(HeightMode.ROW);
        clasificacionGrid.setHeightByRows(10);
        clasificacionGrid.setWidth("100%");
        clasificacionGrid.setResponsive(true);
        clasificacionGrid.setEditorBuffered(false);

        reportLayout.addComponent(clasificacionGrid);
        reportLayout.setComponentAlignment(clasificacionGrid, Alignment.MIDDLE_CENTER);

        addComponent(reportLayout);
        setComponentAlignment(reportLayout, Alignment.TOP_CENTER);
    }

    public void cambiarEstatusCuenta() {

    }

    private void createButtons() {
        Button editBtn = new Button("Editar");
        editBtn.setIcon(FontAwesome.EDIT);
        editBtn.setWidth(140, Sizeable.UNITS_PIXELS);
        editBtn.setDescription("Actualizar datos");
        editBtn.addListener((Button.ClickListener) (Button.ClickEvent event) -> {
            try {
                if (clasificacionGrid.getSelectedRow() == null) {
                    Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
                } else {
                    ClasificacionCuentasCostoForm clasificacionForm = new ClasificacionCuentasCostoForm(
                            String.valueOf(container.getContainerProperty(clasificacionGrid.getSelectedRow(), ID_CLASIFICACION_PROPERTY).getValue()));
                    UI.getCurrent().addWindow(clasificacionForm);
                    clasificacionForm.center();
                }

            } catch (Exception ex) {
                System.out.println("Error en el boton editar clasificacion" + ex);
            }
        });

        Button newBtn = new Button("Nueva");
        newBtn.setIcon(FontAwesome.PLUS);
        newBtn.setWidth(140, Sizeable.UNITS_PIXELS);
        newBtn.setDescription("Agregar nueva cuenta contable.");
        newBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {

                ClasificacionCuentasCostoForm cuentasForm = new ClasificacionCuentasCostoForm("");
                cuentasForm.center();
                UI.getCurrent().addWindow(cuentasForm);

            }
        });

        Button eliminarBtn = new Button("Eliminar");
        eliminarBtn.setIcon(FontAwesome.TRASH);
        eliminarBtn.setDescription("Eliminar");
        eliminarBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (clasificacionGrid.getSelectedRow() == null) {
                    Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
                } else {
                    try {

                        queryString = "";
                        queryString += " delete from clasificacion";
                        queryString += " where IdClasificacion = " + String.valueOf(container.getContainerProperty(clasificacionGrid.getSelectedRow(), ID_CLASIFICACION_PROPERTY).getValue());

                        stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                        stQuery.executeUpdate(queryString);
                        llenarTablaCalificacion();
                        Notification.show("Registro de clasificación eliminada con exito!", Notification.Type.HUMANIZED_MESSAGE);

                    } catch (SQLException ex) {
                        System.out.println("Error al intentar eliminar registro" + ex);
                    }

                }
            }
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setMargin(false);
        buttonsLayout.addComponent(editBtn);
        buttonsLayout.addComponent(newBtn);
        buttonsLayout.addComponent(eliminarBtn);

        addComponent(buttonsLayout);
        setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);
    }

    public void llenarTablaCalificacion() {
        container.removeAllItems();

        String queryString = "";
        queryString = "  select *";
        queryString += " from clasificacion";

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);
            if (rsRecords.next()) { //  encontrado                                                
                do {
                    Object itemId = container.addItem();
                    container.getContainerProperty(itemId, ID_CLASIFICACION_PROPERTY).setValue(rsRecords.getString("IdClasificacion"));
                    container.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(rsRecords.getString("Descripcion"));

                } while (rsRecords.next());
            }
        } catch (Exception ex) {
            System.out.println("Error al listar tabla de clasficacion:" + ex);
            ex.printStackTrace();
        }
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {

    }

}
