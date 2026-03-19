/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.recursoshumanos;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.dialogs.ConfirmDialog;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joseaguirre
 */
public class EmpleadoLiquidadorForm extends Window {

    VerticalLayout mainLayout;
    MarginInfo marginInfo;

    public FormLayout mainFormLayout = new FormLayout();

    ComboBox liquidadorCbx = new ComboBox("Liquidador : " );
    ComboBox proveedorCbx = new ComboBox("Proveedor : " );
    ComboBox cuentaContableCbx = new ComboBox("Cuenta contable : ");

    Button nuevoBtn;
    Button saveBtn;
    Button deleteBtn;
    Button salirBtn;

    boolean esNuevo;

    Statement stQuery = null;
    ResultSet rsRecords = null;

    UI mainUI;
    String idEmpleado;
    String idRegistro;

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public EmpleadoLiquidadorForm(String idRegistro) {
        this.idEmpleado = idEmpleado;
        this.idRegistro = idRegistro;
        this.mainUI = UI.getCurrent();

        setWidth("50%");
        setHeight("50%");

        center();

        setCaption(empresaId + " " + empresaNombre + " CUENTAS CONTABLES AUTORIZADAS PARA EMPLEADO LIQUIDADOR");

        marginInfo = new MarginInfo(true, true, true, true);

        mainLayout = new VerticalLayout();
        mainLayout.setMargin(new MarginInfo(true, true, false, true));
        mainLayout.setSpacing(true);
        mainLayout.setSizeUndefined();

        setContent(mainLayout);

        createForm();

        fillForm(idRegistro);
    }

    private void createForm() {

        mainLayout.setWidth("100%");

        liquidadorCbx.setWidth("100%");
        liquidadorCbx.setInputPrompt("Liquidador");
        liquidadorCbx.setInvalidAllowed(false);
        liquidadorCbx.setNewItemsAllowed(false);
        liquidadorCbx.setNullSelectionAllowed(false);
        liquidadorCbx.setFilteringMode(FilteringMode.CONTAINS);
        llenarComboLiquidador();

        proveedorCbx.setWidth("100%");
        proveedorCbx.setInputPrompt("Proveedor");
        proveedorCbx.setInvalidAllowed(false);
        proveedorCbx.setNewItemsAllowed(false);
        proveedorCbx.setNullSelectionAllowed(false);
        proveedorCbx.setFilteringMode(FilteringMode.CONTAINS);
        llenarComboProveedor();

        cuentaContableCbx.setImmediate(true);
        cuentaContableCbx.setNullSelectionAllowed(false);
        cuentaContableCbx.setTextInputAllowed(true);
        cuentaContableCbx.setInvalidAllowed(false);
        cuentaContableCbx.setNewItemsAllowed(false);
        cuentaContableCbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContableCbx.clear();
        cuentaContableCbx.setWidth("100%");
        fillComboCuentaContable();

        mainFormLayout.addComponents(liquidadorCbx, proveedorCbx, cuentaContableCbx);

        mainLayout.addComponent(mainFormLayout);
        mainLayout.setComponentAlignment(mainFormLayout, Alignment.MIDDLE_CENTER);

        nuevoBtn = new Button("Nuevo");
        nuevoBtn.setIcon(FontAwesome.PLUS);
        nuevoBtn.setDescription("Nuevo");
        nuevoBtn.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        nuevoBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
//                if (!((SopdiUI) mainUI).sessionInformation.getStrUserProfile().equals("ADMINISTRADOR")) {
//                    Notification.show("Usuario no tiene permiso para esta operación.", Notification.Type.WARNING_MESSAGE);
//                    return;
//                }
                cuentaContableCbx.select(null);
                cuentaContableCbx.focus();
                esNuevo = true;
                deleteBtn.setVisible(false);
            }
        });

        saveBtn = new Button("Guardar");
        saveBtn.setIcon(FontAwesome.SAVE);
        saveBtn.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        saveBtn.addClickListener(
                (Button.ClickListener)
                        event -> {
                            if(!esNuevo) {
                                saveData(String.valueOf(idRegistro));
                            }
                            else {
                                saveData("");
                            }
                        }
        );

        deleteBtn = new Button("Eliminar");
        deleteBtn.setIcon(FontAwesome.REMOVE);
        deleteBtn.setDescription("Eliminar");
        deleteBtn.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        deleteBtn.addStyleName(ValoTheme.BUTTON_DANGER);
        deleteBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
//                if (!((SopdiUI) mainUI).sessionInformation.getStrUserProfile().equals("ADMINISTRADOR")) {
//                    Notification.show("Usuario no tiene permiso para esta operación.", Notification.Type.WARNING_MESSAGE);
//                    return;
//                }
                ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de eliminar el registro del rubro de salaria del empleado?",
                        "SI", "NO", new ConfirmDialog.Listener() {

                            public void onClose(ConfirmDialog dialog) {
                                if (dialog.isConfirmed()) {
//                                    deleteliquidador(String.valueOf(liquidadorContaier.getContainerProperty(liquidadorGrid.getSelectedRow(), "id").getValue()));
                                }
                            }
                        });
                }
        });
        deleteBtn.setVisible(false);

        salirBtn = new Button("Salir");
        salirBtn.setIcon(FontAwesome.ARROW_RIGHT);
        salirBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        salirBtn.addClickListener((Button.ClickListener) event -> close());

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setMargin(true);

//        buttonsLayout.addComponent(nuevoBtn);
        buttonsLayout.addComponent(saveBtn);
        buttonsLayout.addComponent(deleteBtn);
        buttonsLayout.addComponent(salirBtn);
        buttonsLayout.setComponentAlignment(salirBtn, Alignment.BOTTOM_RIGHT);

        mainLayout.addComponent(buttonsLayout);
        mainLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);

        proveedorCbx.select(null);
        proveedorCbx.focus();

        esNuevo = true;
        deleteBtn.setVisible(false);

    }

    public void llenarComboLiquidador() {
        String queryString = " SELECT * FROM proveedor_empresa ";
        queryString += " WHERE Inhabilitado = 0 ";
        queryString += " AND EsLiquidador= 1";
        queryString += " AND IdEmpresa = " + empresaId;
        queryString += " ORDER BY Nombre ";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) {
                liquidadorCbx.addItem(rsRecords.getString("IDProveedor"));
                liquidadorCbx.setItemCaption(rsRecords.getString("IDProveedor"), rsRecords.getString("Nombre"));
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar Liquidadores " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void llenarComboProveedor() {
        String queryString = " SELECT * FROM proveedor_empresa ";
        queryString += " WHERE Inhabilitado = 0 ";
        queryString += " AND EsProveedor = 1";
        queryString += " AND IdEmpresa = " + empresaId;
        queryString += " ORDER BY Nombre ";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) {
                proveedorCbx.addItem(rsRecords.getString("IDProveedor"));
                proveedorCbx.setItemCaption(rsRecords.getString("IDProveedor"), rsRecords.getString("Nombre"));
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar Proveedores " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    private void fillComboCuentaContable() {

        String queryString = " SELECT * FROM contabilidad_nomenclatura_empresa";
        queryString += " WHERE Estatus = 'HABILITADA'";
        queryString += " AND IdEmpresa = " + empresaId;
//        queryString += " AND NoCuenta IN ('61101003', '61101004', '61101001'");
//        queryString += " AND UPPER(N4) ='PERSONAL' AND FiltrarIngresoDocumentos = 'B'";
        queryString += " ORDER BY N5";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) { //  encontrado
                cuentaContableCbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContableCbx.setItemCaption(rsRecords.getString("IdNomenclatura"),  rsRecords.getString("NoCuenta") + " (" +  rsRecords.getString("N5") + ")");
            }
            if(cuentaContableCbx.size() > 0) {
                cuentaContableCbx.select(cuentaContableCbx.getItemIds().iterator().next());
            }
        } catch (Exception ex1) {
            System.out.println("Error al combo cuentas contables: " + ex1.getMessage());
            Notification.show("Error al leer cuentas contables.", Notification.Type.ERROR_MESSAGE);
            ex1.printStackTrace();
        }

    }

    public void fillForm(String id) {

        if(id.trim().isEmpty()) {
            return;
        }

        String queryString = "";

        queryString = "SELECT * ";
        queryString += " FROM empleado_liquidador ";
        queryString += " WHERE Id = " + id;

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {

                liquidadorCbx.select(rsRecords.getString("IdEmpleado"));
                proveedorCbx.select(rsRecords.getString("IdProveedor"));
                cuentaContableCbx.select(rsRecords.getString("IdNomenclatura"));
            }

        } catch (Exception ex) {
            Logger.getLogger(EmpleadoLiquidadorForm.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de liquidador de empleado : " + ex.getMessage());
            Notification.show("Error al intentar leer registros de liquidador de empleado..!", Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void saveData(String id) {
//        if (montoTxt.getValue() == null) {
//            Notification.show("Por favor ingrese el monto.", Notification.Type.WARNING_MESSAGE);
//            montoTxt.focus();
//            return;
//        }
//
//        if(esOrdinarioChb.getValue()) {
//            if(esNuevo) {
//                for (Object itemId : liquidadorContaier.getItemIds()) {
//                    if (String.valueOf(liquidadorContaier.getContainerProperty(itemId, "esOrdinario").getValue()).equals("SI")) {
//                        Notification.show("SOLO UN VALOR PUEDE SER ORDINARIO. YA EXISTE UN REGISTRO CON VALOR ORDINARIO", Notification.Type.HUMANIZED_MESSAGE);
//                        return;
//                    }
//                }
//            }
//            else {
//                for (Object itemId : liquidadorContaier.getItemIds()) {
//                    if(itemId != liquidadorGrid.getSelectedRow()) {
//                        if (String.valueOf(liquidadorContaier.getContainerProperty(itemId, "esOrdinario").getValue()).equals("SI")) {
//                            Notification.show("SOLO UN VALOR PUEDE SER ORDINARIO. YA EXISTE UN REGISTRO CON VALOR ORDINARIO", Notification.Type.HUMANIZED_MESSAGE);
//                            return;
//                        }
//                    }
//                }
//            }
//        }

        String queryString;

        try {
            if(esNuevo) {
                queryString = "SELECT * ";
                queryString += " FROM empleado_liquidador ";
                queryString += " WHERE IdEmpleado = " + liquidadorCbx.getValue();
                queryString += " AND   IdProveedor = " + proveedorCbx.getValue();
                queryString += " AND   IdNomenclatura = " + cuentaContableCbx.getValue();
    //System.out.println("queryEmpleadoliquidador=" + queryString);
                stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                rsRecords = stQuery.executeQuery(queryString);

                if(rsRecords.next()) {
                    Notification.show("NUEVO :  YA EXISTE UN REGISTRO CON ESTE PROVEEDOR Y ESTA NOMENCLATURA CONTABLE.", Notification.Type.ERROR_MESSAGE);
                    return;
                }
                queryString = "INSERT INTO empleado_liquidador ";
//                queryString += "(IdEmpleado, IdNomenclatura, Fecha, Valor, EsOrdinario)";
                queryString += "(IdEmpleado, IdEmpresa, IdProveedor, IdNomenclatura)";
                queryString += " VALUES ";
                queryString += "(";
                queryString += liquidadorCbx.getValue();
                queryString += ", " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
                queryString += "," + proveedorCbx.getValue();
                queryString += "," + cuentaContableCbx.getValue();
                queryString += ")";

            } else {

                queryString = "UPDATE empleado_liquidador SET ";
                queryString += " IdEmpleado = " + liquidadorCbx.getValue();
                queryString += ",IdProveedor = " + proveedorCbx.getValue();
                queryString += ",IdNomenclatura = " + cuentaContableCbx.getValue();
                queryString += ",IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
                queryString += " Where Id = " + id;

            }

System.out.println("saveData="+queryString);

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            close();

            deleteBtn.setVisible(false);

            Notification notif = new Notification("REGISTRO AGREGADO o MODIFICADO EXITOSAMENTE.", Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.CHECK);
            notif.show(Page.getCurrent());

        } catch (SQLException ex) {
            Notification notif = new Notification("HA OCURRIDO UN ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.WARNING);
            notif.show(Page.getCurrent());
            System.out.println("Error en base datos al intentar hacer update o insert en tabla empleado_liquidador " + ex);
            ex.printStackTrace();
        }
    }

    public void deleteliquidador(String id) {

        String queryString = "";

        queryString = "DELETE ";
        queryString += " FROM empleado_liquidador ";
        queryString += " WHERE Id = " + id;
//System.out.println("queryEmpleado=" + queryString);
        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            Notification.show("Registro ha sido eliminado!", Notification.Type.WARNING_MESSAGE);
            close();

        } catch (Exception ex) {
            Logger.getLogger(EmpleadoLiquidadorForm.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar eliminar registros de empleado liquidador : " + ex.getMessage());
            Notification.show("Error al intentar eliminar registros de empleado liquidador..!", Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}