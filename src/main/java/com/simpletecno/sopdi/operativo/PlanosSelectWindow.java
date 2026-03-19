package com.simpletecno.sopdi.operativo;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.*;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.dialogs.ConfirmDialog;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlanosSelectWindow extends Window {

    static final String ID_PROPERTY = "Id";
    static final String CENTROCOSTO_PROPERTY = "CentroCosto";
    static final String CATEGORIA_PROPERTY = "Categoria";
    static final String NOMBRE_PROPERTY = "Nombre";
    static final String DESCRIPCION_PROPERTY = "Descripción";
    static final String PLANO_PROPERTY = "Plano";

    Button asignarBtn;

    public IndexedContainer container = new IndexedContainer();
    Grid planosGrid;

    UI mainUI;

    VerticalLayout mainLayout;

    Statement stQuery;
    ResultSet rsRecords;
    String queryString;

    String idGrupoTrabajoPlan;

    public PlanosSelectWindow(String idGrupoTrabajoPlan, String centroCosto, String idex) {
        this.idGrupoTrabajoPlan = idGrupoTrabajoPlan;
        this.mainUI = UI.getCurrent();

        Responsive.makeResponsive(this);

        Label titleLbl = new Label("Seleccionar Planos para IDEX : " + centroCosto + " " + idex);
        titleLbl.addStyleName(ValoTheme.LABEL_H1);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h1_custom");

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setMargin(new MarginInfo(false, true, false, true));
        titleLayout.setSpacing(true);
        titleLayout.setWidth("100%");

        titleLayout.addComponents(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.TOP_LEFT);

        mainLayout =  new VerticalLayout();
        mainLayout.setWidth("100%");
        mainLayout.setSpacing(true);

        setContent(mainLayout);

        mainLayout.addComponent(titleLayout);
        mainLayout.setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        createTablaPlanos();
        llenarTablaPlanos(centroCosto);
        createButtons();
    }

    public void createTablaPlanos() {

        HorizontalLayout reportLayout = new HorizontalLayout();
        reportLayout.setWidth("100%");
        reportLayout.addStyleName("rcorners3");
        reportLayout.setResponsive(true);
        reportLayout.setMargin(true);

        container.addContainerProperty(ID_PROPERTY, String.class, null);
        container.addContainerProperty(CENTROCOSTO_PROPERTY, String.class, null);
        container.addContainerProperty(CATEGORIA_PROPERTY, String.class, null);
        container.addContainerProperty(NOMBRE_PROPERTY, String.class, null);
        container.addContainerProperty(DESCRIPCION_PROPERTY, String.class, null);
        container.addContainerProperty(PLANO_PROPERTY, String.class, null);

        planosGrid = new Grid("Listado de planos por categoria", container);
        planosGrid.setImmediate(true);
        planosGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        planosGrid.setDescription("Seleccione un registro.");
        planosGrid.setHeightMode(HeightMode.ROW);
        planosGrid.setHeightByRows(15);
        planosGrid.setWidth("100%");
        planosGrid.setResponsive(true);
        planosGrid.setEditorBuffered(false);

        planosGrid.getColumn(ID_PROPERTY).setHidable(true).setHidden(true);

        reportLayout.addComponent(planosGrid);
        reportLayout.setComponentAlignment(planosGrid, Alignment.MIDDLE_CENTER);

        mainLayout.addComponent(reportLayout);
        mainLayout.setComponentAlignment(reportLayout, Alignment.TOP_CENTER);
    }

    private void createButtons() {

        asignarBtn = new Button("Asignar los planos seleccionados");
        asignarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        asignarBtn.setIcon(FontAwesome.SEARCH);
        asignarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (planosGrid.getSelectedRows() == null) {
                    Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
                } else {

                    ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de eliminar el plano?",
                            "SI", "NO", new ConfirmDialog.Listener() {

                        public void onClose(ConfirmDialog dialog) {
                            if (dialog.isConfirmed()) {

                                try {
                                    //eliminar registros tabla grupo_trabajo_plan_planos
                                    queryString = " DELETE FROM grupo_trabajo_plan_planos ";
                                    queryString += " WHERE IdGrupoTrabajoPlan = " + idGrupoTrabajoPlan;

                                    stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                                    stQuery.executeUpdate(queryString);

                                    //recorrer todos los seleccionados e insertarlos
                                    for(Object selectedItem : planosGrid.getSelectedRows()) {
                                        queryString = "INSERT INTO grupo_trabajo_plan_planos ";
                                        queryString += "(IdGrupoTrabajoPlan, IdPlano, CreadoUsuario, CreadoFechaYHora) ";
                                        queryString += "VALUES (";
                                        queryString += " " + idGrupoTrabajoPlan;
                                        queryString += "," + container.getContainerProperty(selectedItem, ID_PROPERTY).getValue();
                                        queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                                        queryString += ",current_timestamp";
                                        queryString += ")";

                                        stQuery.executeUpdate(queryString);
                                    }

                                } catch (SQLException ex) {
                                    Notification.show("ERROR EN BASE DE DATOS AL INSERTAR PLANO DE IDEX.", Notification.Type.ERROR_MESSAGE);
                                    Logger.getLogger(PlanosSelectWindow.class.getName()).log(Level.SEVERE, null, ex);
                                }

                            }
                        }
                    });

                }
            }
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setMargin(false);
        buttonsLayout.addComponent(asignarBtn);
        buttonsLayout.setComponentAlignment(asignarBtn, Alignment.MIDDLE_CENTER);

        mainLayout.addComponent(buttonsLayout);
        mainLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);
    }

    public void llenarTablaPlanos(String centroCosto) {

        container.removeAllItems();

        queryString = "  SELECT *, GTPP.Id IdPlanoYaAsignado ";
        queryString += " FROM planos PLA";
        queryString += " LEFT JOIN grupo_trabajo_plan_planos GTPP  ON GTPP.IdPlano = PLA.Id";
        queryString += " WHERE PLA.IdCentroCosto = '" + centroCosto + "'";

System.out.println(queryString);

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado                                                
                do {
                    Object itemId = container.addItem();

                    container.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords.getString("Id"));
                    container.getContainerProperty(itemId, CENTROCOSTO_PROPERTY).setValue(rsRecords.getString("IdCentroCosto"));
                    container.getContainerProperty(itemId, CATEGORIA_PROPERTY).setValue(rsRecords.getString("Categoria"));
                    container.getContainerProperty(itemId, NOMBRE_PROPERTY).setValue(rsRecords.getString("Nombre"));
                    container.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(rsRecords.getString("Descripcion"));
                    container.getContainerProperty(itemId, PLANO_PROPERTY).setValue(rsRecords.getString("Plano"));

                    if(rsRecords.getObject("IdPlanoYaAsignado") != null) { // plano ya seleccionado , marcarlo
                        planosGrid.select(itemId);
                    }

                } while (rsRecords.next());
            }
        } catch (Exception ex) {
            System.out.println("Error al listar tabla de planos para asignar a idex :" + ex);
            ex.printStackTrace();
            Notification.show("Error al listar tabla de planos para asignar a idex. ", Notification.Type.ERROR_MESSAGE);
        }
    }
}