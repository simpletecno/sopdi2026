package com.simpletecno.sopdi.compras;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Responsive;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.sql.ResultSet;
import java.sql.Statement;

public class ProveedoresInstitucionalesView extends VerticalLayout implements View {
    UI mainUI;
    Statement stQuery;
    ResultSet rsRecords;

    ComboBox cuentaContable1Cbx;
    ComboBox cuentaContable2Cbx;

    public ProveedoresInstitucionalesView() {

        Responsive.makeResponsive(this);
        setMargin(true);
        setSpacing(true);
        this.mainUI = UI.getCurrent();

        Label titleLbl = new Label("Proveedores Institucionales para : " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId() + " "+ ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName());
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

        createTablaCuentasContables();
        createButtons();
    }

    public void createTablaCuentasContables() {

        cuentaContable1Cbx = new ComboBox("SAT : ");
        cuentaContable1Cbx.setWidth("29em");
        cuentaContable1Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable1Cbx.setInvalidAllowed(false);
        cuentaContable1Cbx.setNewItemsAllowed(false);

        cuentaContable2Cbx = new ComboBox("IGSS : ");
        cuentaContable2Cbx.setWidth("29em");
        cuentaContable2Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable2Cbx.setInvalidAllowed(false);
        cuentaContable2Cbx.setNewItemsAllowed(false);

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setMargin(new MarginInfo(false, true, true, true));
        horizontalLayout.setSpacing(true);
        horizontalLayout.setResponsive(true);

        VerticalLayout leftVerticalLayout = new VerticalLayout();
        leftVerticalLayout.setMargin(true);
        leftVerticalLayout.setSpacing(true);
        leftVerticalLayout.setResponsive(true);
        leftVerticalLayout.setCaption("");

        VerticalLayout rightVerticalLayout = new VerticalLayout();
        rightVerticalLayout.setMargin(true);
        rightVerticalLayout.setSpacing(true);
        rightVerticalLayout.setResponsive(true);
        rightVerticalLayout.setCaption("");

        leftVerticalLayout.addComponents(
                cuentaContable1Cbx
        );

        rightVerticalLayout.addComponents(
                cuentaContable2Cbx
        );

        horizontalLayout.addComponents(leftVerticalLayout, rightVerticalLayout);

        addComponent(horizontalLayout);
        setComponentAlignment(horizontalLayout, Alignment.TOP_CENTER);

        llenarComboCuentaContable();
    }

    public void llenarComboCuentaContable() {

        String queryString = " SELECT * FROM proveedor";
        queryString += " WHERE EsProveedor = 1";
        queryString += " AND N0 = 6";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) { //  encontrado
                cuentaContable1Cbx.addItem(rsRecords.getString("IDProveedor"));
                cuentaContable1Cbx.setItemCaption(rsRecords.getString("IDProveedor"), rsRecords.getString("Nombre") + " " + rsRecords.getString("NIT"));

                cuentaContable2Cbx.addItem(rsRecords.getString("IDProveedor"));
                cuentaContable2Cbx.setItemCaption(rsRecords.getString("IDProveedor"), rsRecords.getString("Nombre") + " " + rsRecords.getString("NIT"));
            }
            selectProveedoresInstitucionales();

        } catch (Exception ex1) {
            System.out.println("Error al LEER combo proveedor institucional: " + ex1.getMessage());
            ex1.printStackTrace();
            Notification.show("Error al leer proveedor institucional: " + ex1.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }

    void selectProveedoresInstitucionales() {
        try {
            cuentaContable1Cbx.select(((SopdiUI) mainUI).proveedoresInstitucionales.getSat());
            cuentaContable2Cbx.select(((SopdiUI) mainUI).proveedoresInstitucionales.getIgss());
        } catch (Exception ex1) {
            System.out.println("Error al intntentar obtener proveeodres Institucionales:  " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    private void createButtons() {

        Button newBtn = new Button("GUARDAR");
        newBtn.setIcon(FontAwesome.PLUS);
        newBtn.setWidth(140, Unit.PIXELS);
        newBtn.setDescription("GUARDAR CAMBIOS.");
        newBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                try {
                    stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

                    //DELETE
                    String queryString =  "DELETE FROM proveedor_institucionales ";
                    queryString += " WHERE IdEmpresa = " +  ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();

                    stQuery.executeUpdate(queryString);

                    //INSERT
                    queryString =  "INSERT INTO proveedor_institucionales (";
                    queryString += "IdEmpresa, SAT, IGSS";
                    queryString +=  ") ";
                    queryString += " VALUES ( ";
                    queryString +=  ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
                    queryString +=  ",'" + cuentaContable1Cbx.getValue() + "'";
                    queryString +=  ",'" + cuentaContable2Cbx.getValue() + "'";
                    queryString += " ) ";

                    stQuery.executeUpdate(queryString);

                    ((SopdiUI) mainUI).fillProveedoresInstitucionales();
                    Notification.show("OPERACION EXITOSA!", Notification.Type.HUMANIZED_MESSAGE);

                } catch (Exception ex) {
                    Notification.show("Error al crear/actualizar proveedor institucional: " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                    System.out.println("Error en el boton guardar cambios cuentas proveedor institucional");
                    ex.printStackTrace();
                }
            }
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setMargin(false);
        buttonsLayout.addComponent(newBtn);

        addComponent(buttonsLayout);
        setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - Cuentas contables Default");
    }
}