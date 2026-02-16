package com.simpletecno.sopdi.configuracion;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Responsive;
import com.vaadin.server.Sizeable;
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
 * @author user
 */
public class EmpresasContablesView extends VerticalLayout implements View {

    static final String ID_EMPRESA_PROPERTY = "Id";
    static final String NOMBRE_PROPERTY = "Nombre";
    static final String NOMBRE_CORTO_PROPERTY = "Nombre corto";
    static final String NIT_PROPERTY = "NIT";
    static final String ULTIMA_LIQUI_PROPERTY = "Ultima Liq.";
    static final String USER_FEL_PROPERTY = "FEL";
    static final String REGIMEN_PROPERTY = "Regimen";

    public IndexedContainer container = new IndexedContainer();
    Grid empresasGrid;

    UI mainUI;
    Statement stQuery;
    ResultSet rsRecords;
    String queryString;

    public EmpresasContablesView() {
        this.mainUI = UI.getCurrent();

        Label titleLbl = new Label("Empresas contables");
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
        llenarTablaEmpresas();
        createButtons();

    }

    public void createTablaCuentasContables() {

        HorizontalLayout reportLayout = new HorizontalLayout();
        reportLayout.setWidth("75%");
        reportLayout.addStyleName("rcorners2");
        reportLayout.setResponsive(true);
        reportLayout.setMargin(true);

        container.addContainerProperty(ID_EMPRESA_PROPERTY, String.class, null);
        container.addContainerProperty(NOMBRE_PROPERTY, String.class, null);
        container.addContainerProperty(NOMBRE_CORTO_PROPERTY, String.class, null);
        container.addContainerProperty(NIT_PROPERTY, String.class, null);
        container.addContainerProperty(ULTIMA_LIQUI_PROPERTY, String.class, null);
        container.addContainerProperty(USER_FEL_PROPERTY, String.class, null);
        container.addContainerProperty(REGIMEN_PROPERTY, String.class, null);

        empresasGrid = new Grid("Listado de empresas CONTABLES", container);
        empresasGrid.setImmediate(true);
        empresasGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        empresasGrid.setDescription("Seleccione un registro.");
        empresasGrid.setHeightMode(HeightMode.ROW);
        empresasGrid.setHeightByRows(10);
        empresasGrid.setWidth("100%");
        empresasGrid.setResponsive(true);
        empresasGrid.setEditorBuffered(false);

        reportLayout.addComponent(empresasGrid);
        reportLayout.setComponentAlignment(empresasGrid, Alignment.MIDDLE_CENTER);

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
                if (empresasGrid.getSelectedRow() == null) {
                    Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
                } else {
                    EmpresasContablesForm empresaForm = new EmpresasContablesForm();
                    empresaForm.idEmpresaEdit = String.valueOf(container.getContainerProperty(empresasGrid.getSelectedRow(), ID_EMPRESA_PROPERTY).getValue());
                    empresaForm.llenarCampos();
                    UI.getCurrent().addWindow(empresaForm);
                }

            } catch (Exception ex) {
                System.out.println("Error en el boton editar cuenta" + ex);
            }
        });

        Button newBtn = new Button("Nueva");
        newBtn.setIcon(FontAwesome.PLUS);
        newBtn.setWidth(140, Sizeable.UNITS_PIXELS);
        newBtn.setDescription("Agregar nueva empresa contable.");
        newBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                EmpresasContablesForm empresaForm = new EmpresasContablesForm();
                empresaForm.center();
                UI.getCurrent().addWindow(empresaForm);
            }
        });

        Button eliminarBtn = new Button("Eliminar");
        eliminarBtn.setIcon(FontAwesome.TRASH);
        eliminarBtn.setDescription("Eliminar cuenta.");
        eliminarBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (empresasGrid.getSelectedRow() == null) {
                    Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
                } else {

                    queryString = " select * from contabilidad_partida";
                    queryString += " where IdEmpresa = " + String.valueOf(container.getContainerProperty(empresasGrid.getSelectedRow(), ID_EMPRESA_PROPERTY).getValue());

                    try {

                        stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                        rsRecords = stQuery.executeQuery(queryString);

                        if (rsRecords.next()) {
                            Notification.show("La EMPRESA seleccionada contiene movimientos en partidas no se puede eliminar.", Notification.Type.ERROR_MESSAGE);
                        } else {

                            queryString = " delete from contabilidad_empresa";
                            queryString += " where IdEmpresa = " + String.valueOf(container.getContainerProperty(empresasGrid.getSelectedRow(), ID_EMPRESA_PROPERTY).getValue());
                            ;

                            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                            stQuery.executeUpdate(queryString);

                            Notification.show("EMPRESA eliminada con exito!", Notification.Type.HUMANIZED_MESSAGE);

                            llenarTablaEmpresas();
                        }
                    } catch (SQLException ex) {
                        System.out.println("Error al buscar registros en contabilidad_partida" + ex);
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

    public void llenarTablaEmpresas() {

        container.removeAllItems();

        queryString = "  select *";
        queryString += " from contabilidad_empresa";

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);
            if (rsRecords.next()) { //  encontrado                                                
                do {
                    Object itemId = container.addItem();
                    container.getContainerProperty(itemId, ID_EMPRESA_PROPERTY).setValue(rsRecords.getString("IdEmpresa"));
                    container.getContainerProperty(itemId, NOMBRE_PROPERTY).setValue(rsRecords.getString("Empresa"));
                    container.getContainerProperty(itemId, NOMBRE_CORTO_PROPERTY).setValue(rsRecords.getString("NombreCorto"));
                    container.getContainerProperty(itemId, NIT_PROPERTY).setValue(rsRecords.getString("NIT"));
                    container.getContainerProperty(itemId, ULTIMA_LIQUI_PROPERTY).setValue(rsRecords.getString("IdUltimaLiquidacion"));
                    container.getContainerProperty(itemId, USER_FEL_PROPERTY).setValue(rsRecords.getString("UsuarioFEL"));
                    container.getContainerProperty(itemId, REGIMEN_PROPERTY).setValue(rsRecords.getString("Regimen"));

                } while (rsRecords.next());
            }
        } catch (Exception ex) {
            System.out.println("Error al listar tabla empresas contables :" + ex);
            ex.printStackTrace();
        }
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - Empresas contables");
    }

}
