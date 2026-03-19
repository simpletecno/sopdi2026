package com.simpletecno.sopdi.inventario;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.Runo;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.ui.NumberField;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author user
 */
public class ProveedorPluForm extends Window {

    public String idPluEdit = "0";

    String queryString = "";

    UI mainUI;
    Statement stQuery = null;
    ResultSet rsRecords = null;

    MarginInfo marginInfo;

    FormLayout mainForm;

    ComboBox proveedorCbx = new ComboBox("Proveedor :");
    ComboBox cuentaCentroCostoCbx = new ComboBox("Cuenta CC :");
    TextField pluTxt = new TextField("PLU :");
    TextField descripcionTxt = new TextField("DESCRIPCION :");
    NumberField cantidadTxt = new NumberField("Cantidad :");
    NumberField precioTxt = new NumberField("Precio :");

    Button guardarBtn;
    Button salirBtn;

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public ProveedorPluForm() {
        this.mainUI = UI.getCurrent();
        setResponsive(true);
        setModal(true);

        marginInfo = new MarginInfo(true, true, false, true);

        mainForm = new FormLayout();
        mainForm.setWidth("100%");
        mainForm.setMargin(true);
        mainForm.setSpacing(true);

        proveedorCbx.setNullSelectionAllowed(true);
        proveedorCbx.setInvalidAllowed(false);
        proveedorCbx.setNewItemsAllowed(false);
        proveedorCbx.setFilteringMode(FilteringMode.CONTAINS);
        proveedorCbx.addStyleName(ValoTheme.COMBOBOX_SMALL);
        proveedorCbx.setWidth("30em");
        fillComboProveedor(proveedorCbx);

        cuentaCentroCostoCbx.setNullSelectionAllowed(true);
        cuentaCentroCostoCbx.setInvalidAllowed(false);
        cuentaCentroCostoCbx.setNewItemsAllowed(false);
        cuentaCentroCostoCbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaCentroCostoCbx.addStyleName(ValoTheme.COMBOBOX_SMALL);
        cuentaCentroCostoCbx.setWidth("30em");
        fillComboCuentaCentroCosto(cuentaCentroCostoCbx);

        pluTxt.setValue("");
        pluTxt.setWidth("15em");
        descripcionTxt.setValue("");
        descripcionTxt.setWidth("30em");

        cantidadTxt.setWidth("15em");
        cantidadTxt.setDecimalPrecision(2);
        cantidadTxt.setDecimalSeparator('.');
//        cantidadTxt.setDoubleValue(0.00d);
        cantidadTxt.setGroupingUsed(true);
        cantidadTxt.setGroupingSeparator(',');
        cantidadTxt.setImmediate(true);
        cantidadTxt.setNegativeAllowed(true);
        cantidadTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);
        cantidadTxt.addStyleName(ValoTheme.TEXTFIELD_SMALL);

        precioTxt.setWidth("15em");
        precioTxt.setDecimalPrecision(2);
        precioTxt.setDecimalSeparator('.');
//        precioTxt.setDoubleValue(0.00d);
        precioTxt.setGroupingUsed(true);
        precioTxt.setGroupingSeparator(',');
        precioTxt.setImmediate(true);
        precioTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        precioTxt.addStyleName(ValoTheme.TEXTFIELD_SMALL);

        salirBtn = new Button("Salir");
        salirBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        salirBtn.setIcon(FontAwesome.ARROW_RIGHT);
        salirBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();
            }
        });

        guardarBtn = new Button("Guardar");
        guardarBtn.setIcon(FontAwesome.SAVE);
        guardarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                guardar();
            }
        });

        mainForm.addComponent(proveedorCbx);
        mainForm.addComponent(cuentaCentroCostoCbx);
        mainForm.addComponent(pluTxt);
        mainForm.addComponent(descripcionTxt);
        mainForm.addComponent(cantidadTxt);
        mainForm.addComponent(precioTxt);

        if (!idPluEdit.equals("0")) {
            llenarCampos();
        }

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.addComponents(salirBtn, guardarBtn);
        buttonsLayout.setComponentAlignment(salirBtn, Alignment.BOTTOM_LEFT);
        buttonsLayout.setComponentAlignment(guardarBtn, Alignment.BOTTOM_CENTER);

        mainForm.addComponent(buttonsLayout);
        mainForm.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);

        Label titleLbl = new Label(empresaId + " " + empresaNombre + " PLU de Proveedor");
        titleLbl.addStyleName(Runo.LABEL_H2);
        titleLbl.setSizeUndefined();

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setWidth("100%");
        titleLayout.setMargin(true);
        titleLayout.addComponent(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.TOP_CENTER);

        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setSizeUndefined();
        contentLayout.addComponent(titleLayout);
        contentLayout.setComponentAlignment(titleLayout, Alignment.TOP_CENTER);
        contentLayout.addComponent(mainForm);
        contentLayout.setComponentAlignment(mainForm, Alignment.TOP_CENTER);

        setContent(contentLayout);

    }

    private void fillComboProveedor(ComboBox comboBox) {

        String queryString = "SELECT * ";
        queryString += " FROM proveedor_empresa ";
        queryString += " WHERE IdEmpresa = " + empresaId;

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            comboBox.removeAllItems();
            comboBox.addItem(0);
            comboBox.setItemCaption(0, "<<ELIJA>>");
            comboBox.select(0);

            while (rsRecords.next()) { //  encontrado
                comboBox.addItem(rsRecords.getString("IDProveedor"));
                comboBox.setItemCaption(rsRecords.getString("IDProveedor"), rsRecords.getString("Nombre"));

            }
        } catch (Exception ex1) {
            Notification.show("ERROR DEL SISTEMA AL BUSCAR CATALOGO DE PROVEEDORES", Notification.Type.ERROR_MESSAGE);
            System.out.println("ERROR AL INTENTAR BUSCAR CATALOGO DE PROVEEDORES : " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    private void fillComboCuentaCentroCosto(ComboBox comboBox) {

        String queryString = "SELECT * ";
        queryString += " FROM centro_costo_cuenta ";
        queryString += " WHERE IdProyecto = " + ((SopdiUI) mainUI).sessionInformation.getStrProjectId();

// System.out.println("queryComboCCC=" + queryString);
        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            comboBox.addItem("0");
            comboBox.setItemCaption("0", "<<ELIJA>>");

            while (rsRecords.next()) { //  encontrado
                comboBox.addItem(rsRecords.getString("IdCuentaCentroCosto"));
                comboBox.setItemCaption(rsRecords.getString("IdCuentaCentroCosto"), rsRecords.getString("CodigoCuentaCentroCosto") + " " + rsRecords.getString("Descripcion"));
            }

            comboBox.select("0");
        } catch (Exception ex1) {
            Notification.show("ERROR DEL SISTEMA AL BUSCAR CENTROS DE COSTO", Notification.Type.ERROR_MESSAGE);
            System.out.println("ERROR AL INTENTAR BUSCAR CATALOGO DE CUENTAS DE CENTROS DE COSTO : " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void llenarCampos() {
        try {

            queryString = " SELECT *";
            queryString += " FROM proveedor_plu";
            queryString += " WHERE Id = " + idPluEdit;

            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                proveedorCbx.setValue(rsRecords.getString("IDProveedor"));
                cuentaCentroCostoCbx.setValue(rsRecords.getString("IdCuentaCentroCosto"));
                pluTxt.setValue(rsRecords.getString("PLU"));
                descripcionTxt.setValue(rsRecords.getString("DescripcionProveedor"));
                cantidadTxt.setValue(rsRecords.getString("Cantidad"));
                precioTxt.setValue(rsRecords.getString("Precio"));

            }
        } catch (SQLException ex) {
            System.out.println("Error al llenar Campos " + ex);
            ex.printStackTrace();
        }
    }

    public void guardar() {
        try {

            if (idPluEdit.equals("0")) {
                queryString = "INSERT INTO proveedor_plu ";
                queryString += "(IDProveedor, IdCuentaCentroCosto, PLU, DescripcionProveedor, Cantidad, Precio) ";
                queryString += " VALUES (";
                queryString += proveedorCbx.getValue() + ",";
                queryString += cuentaCentroCostoCbx.getValue() + ",";
                queryString += "'" + pluTxt.getValue() + "',";
                queryString += "'" + descripcionTxt.getValue() + "',";
                queryString += cantidadTxt.getValue() + ",";
                queryString += precioTxt.getValue();
                queryString += ")";
            } else { //update
                queryString = "UPDATE proveedor_plu SET ";
                queryString += "IDProveedor = " + proveedorCbx.getValue() + ",";
                queryString += "IdCuentaCentroCosto = " + cuentaCentroCostoCbx.getValue() + ",";
                queryString += "PLU = '" + pluTxt.getValue() + "',";
                queryString += "DescripcionProveedor = '" + descripcionTxt.getValue() + "',";
                queryString += "Cantidad = " + cantidadTxt.getValue() + ",";
                queryString += "Precio = " + precioTxt.getValue() + " ";
                queryString += " WHERE Id = " + idPluEdit;
            }

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            ((ProveedorPluView) (mainUI.getNavigator().getCurrentView())).llenarTabla();

            Notification notif = new Notification("REGISTRO AGREGADO o MODIFICADO EXITOSAMENTE.",
                    Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.CHECK);
            notif.show(Page.getCurrent());

        } catch (SQLException ex) {
            System.out.println("Error al insertar o editar " + queryString);
            Notification notif = new Notification("HA OCURRIDO UN ERROR DE BASE DE DATOS : " + ex.getMessage(),
                    Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.WARNING);
            notif.show(Page.getCurrent());
            ex.printStackTrace();
        }
    }
}
