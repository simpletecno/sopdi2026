package com.simpletecno.sopdi.tesoreria;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.FontAwesome;
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
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;

public class CuentasContablesBancosView extends VerticalLayout implements View {

    static final String ID_CUENTABANCO_PROPERTY = "Id";
    static final String ID_EMPRESA_PROPERTY = "Id Empresa";
    static final String EMPRESA_PROPERTY = "Empresa";
    static final String ID_NOMENCLATURA_PROPERTY = "Id Nomenclatura";
    static final String N5_PROPERTY = "N5";
    static final String PROVEEDOR_PROPERTY = "Banco";
    static final String NOCUENTA_PROPERTY = "No Cuenta";
    static final String MONEDA_PROPERTY = "Moneda";

    public IndexedContainer container = new IndexedContainer();
    Grid cuentasGrid;

    UI mainUI;
    Statement stQuery;
    ResultSet rsRecords;
    String queryString;

    String idEmpresa;
    String idCuentaBanco;

    public CuentasContablesBancosView() {
        this.mainUI = UI.getCurrent();

        Label titleLbl = new Label("Cuentas contables de Bancos");
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
        llenarTablaCuentas();
        createButtons();

    }

    public void createTablaCuentasContables() {

        HorizontalLayout reportLayout = new HorizontalLayout();
        reportLayout.setWidth("75%");
        reportLayout.addStyleName("rcorners3");
        reportLayout.setResponsive(true);
        reportLayout.setMargin(true);

        container.addContainerProperty(ID_CUENTABANCO_PROPERTY, String.class, null);
        container.addContainerProperty(PROVEEDOR_PROPERTY, String.class, null);
        container.addContainerProperty(ID_NOMENCLATURA_PROPERTY, String.class, null);
        container.addContainerProperty(N5_PROPERTY, String.class, null);
        container.addContainerProperty(NOCUENTA_PROPERTY, String.class, null);
        container.addContainerProperty(MONEDA_PROPERTY, String.class, null);
        container.addContainerProperty(ID_EMPRESA_PROPERTY, String.class, null);
        container.addContainerProperty(EMPRESA_PROPERTY, String.class, null);

        cuentasGrid = new Grid("Listado de cuentas", container);
        cuentasGrid.setImmediate(true);
        cuentasGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        cuentasGrid.setDescription("Seleccione un registro.");
        cuentasGrid.setHeightMode(HeightMode.ROW);
        cuentasGrid.setHeightByRows(10);
        cuentasGrid.setWidth("100%");
        cuentasGrid.setResponsive(true);
        cuentasGrid.setEditorBuffered(false);

        cuentasGrid.getColumn(ID_CUENTABANCO_PROPERTY).setHidable(true).setHidden(true);
        cuentasGrid.getColumn(ID_EMPRESA_PROPERTY).setHidable(true).setHidden(true);
        cuentasGrid.getColumn(ID_NOMENCLATURA_PROPERTY).setHidable(true).setHidden(true);

        cuentasGrid.getColumn(ID_CUENTABANCO_PROPERTY).setExpandRatio(1);
        cuentasGrid.getColumn(ID_NOMENCLATURA_PROPERTY).setExpandRatio(1);
        cuentasGrid.getColumn(ID_EMPRESA_PROPERTY).setExpandRatio(1);
        cuentasGrid.getColumn(N5_PROPERTY).setExpandRatio(4);
        cuentasGrid.getColumn(PROVEEDOR_PROPERTY).setExpandRatio(3);
        cuentasGrid.getColumn(NOCUENTA_PROPERTY).setExpandRatio(2);
        cuentasGrid.getColumn(MONEDA_PROPERTY).setExpandRatio(2);
        cuentasGrid.getColumn(EMPRESA_PROPERTY).setExpandRatio(4);

        reportLayout.addComponent(cuentasGrid);
        reportLayout.setComponentAlignment(cuentasGrid, Alignment.MIDDLE_CENTER);

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
                if (cuentasGrid.getSelectedRow() == null) {
                    Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
                } else {

                    idEmpresa = String.valueOf(container.getContainerProperty(cuentasGrid.getSelectedRow(), ID_EMPRESA_PROPERTY).getValue());
                    idCuentaBanco = String.valueOf(container.getContainerProperty(cuentasGrid.getSelectedRow(), ID_CUENTABANCO_PROPERTY).getValue());

                    if (!idEmpresa.equals(((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId())) {
                        Notification.show("Debe seleccionar una cuenta que pertenezca a " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName(), Notification.Type.WARNING_MESSAGE);
                        return;
                    }

                    CuentasBancosForm cuentasBancosForm = new CuentasBancosForm("1", idCuentaBanco); // 1 SIGNIFICA EDITAR
                    UI.getCurrent().addWindow(cuentasBancosForm);
                    cuentasBancosForm.center();
                }

            } catch (Exception ex) {
                System.out.println("Error en el boton editar cuenta" + ex);
                ex.printStackTrace();
            }
        });

        Button newBtn = new Button("Nueva");
        newBtn.setIcon(FontAwesome.PLUS);
        newBtn.setWidth(140, Sizeable.UNITS_PIXELS);
        newBtn.setDescription("Agregar nueva empresa.");
        newBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                CuentasBancosForm cuentasBancosForm = new CuentasBancosForm("0", ""); // 0 SIGNIFICA NUEVA CUENTA
                UI.getCurrent().addWindow(cuentasBancosForm);
                cuentasBancosForm.center();
            }
        });

        Button eliminarBtn = new Button("Eliminar");
        eliminarBtn.setIcon(FontAwesome.TRASH);
        eliminarBtn.setDescription("Eliminar cuenta.");
        eliminarBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (cuentasGrid.getSelectedRow() == null) {

                    Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
                } else {
                    if (cuentasGrid.getSelectedRow() == null) {
                        Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
                        return;
                    }

                    idEmpresa = String.valueOf(container.getContainerProperty(cuentasGrid.getSelectedRow(), ID_EMPRESA_PROPERTY).getValue());
                    idCuentaBanco = String.valueOf(container.getContainerProperty(cuentasGrid.getSelectedRow(), ID_CUENTABANCO_PROPERTY).getValue());

                    if (!idEmpresa.equals(((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId())) {
                        Notification.show("Debe seleccionar una cuenta que pertenezca a " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName(), Notification.Type.WARNING_MESSAGE);
                        return;
                    }

                    try {

                        queryString = " delete from contabilidad_cuentas_bancos";
                        queryString += " where IdCuentaBanco = " + idCuentaBanco;

                        stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                        stQuery.executeUpdate(queryString);

                        Notification.show("Cuenta eliminada con exito!", Notification.Type.HUMANIZED_MESSAGE);

                        llenarTablaCuentas();

                    } catch (SQLException ex) {
                        System.out.println("Error al buscar registros en contabilidad_cuentas_bancos" + ex.getMessage());
                        ex.printStackTrace();
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

    public void llenarTablaCuentas() {

        container.removeAllItems();        

        queryString = "  SELECT *, cuen.N5, emp.Empresa, prov.Nombre ";
        queryString += " FROM contabilidad_cuentas_bancos AS ban";
        queryString += " INNER JOIN contabilidad_nomenclatura AS cuen";
        queryString += " ON ban.IdNomenclatura = cuen.IdNomenclatura";
        queryString += " INNER JOIN contabilidad_empresa AS emp ON ban.IdEmpresa = emp.IdEmpresa";
        queryString += " INNER JOIN proveedor AS prov ON ban.IdProveedor = prov.IdProveedor";
        queryString += " WHERE ban.IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
        queryString += " ORDER BY ban.IdEmpresa, ban.IdNomenclatura";

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);
            
            if (rsRecords.next()) { //  encontrado                                                
                do {
                    Object itemId = container.addItem();

                    container.getContainerProperty(itemId, ID_CUENTABANCO_PROPERTY).setValue(rsRecords.getString("IdCuentaBanco"));
                    container.getContainerProperty(itemId, PROVEEDOR_PROPERTY).setValue(rsRecords.getString("prov.Nombre"));
                    container.getContainerProperty(itemId, ID_EMPRESA_PROPERTY).setValue(rsRecords.getString("IdEmpresa"));
                    container.getContainerProperty(itemId, EMPRESA_PROPERTY).setValue(rsRecords.getString("Empresa"));
                    container.getContainerProperty(itemId, ID_NOMENCLATURA_PROPERTY).setValue(rsRecords.getString("IdNomenclatura"));
                    container.getContainerProperty(itemId, N5_PROPERTY).setValue(rsRecords.getString("N5"));
                    container.getContainerProperty(itemId, NOCUENTA_PROPERTY).setValue(rsRecords.getString("NoCuenta"));
                    container.getContainerProperty(itemId, MONEDA_PROPERTY).setValue(rsRecords.getString("Moneda"));

                } while (rsRecords.next());
            }
        } catch (Exception ex) {
            System.out.println("Error al listar tabla empresas contables :" + ex);
            ex.printStackTrace();
        }
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - Cuentas Banco");
    }
}
