/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.recursoshumanos;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.SelectionEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.ui.NumberField;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joseaguirre
 */
public class EmpleadoSalarioForm extends Window {

    VerticalLayout mainLayout;
    MarginInfo marginInfo;

    public IndexedContainer salarioContaier = new IndexedContainer();
    public Grid salarioGrid;
    public FormLayout mainFormLayout = new FormLayout();

    ComboBox cuentaContableCbx = new ComboBox("Cuenta contable : ");
    DateField desdeDt =  new DateField("Desde : ");
    NumberField montoTxt =  new NumberField("Monto : ");
    CheckBox esOrdinarioChb = new CheckBox("Ordinario :");

    Button nuevoBtn;
    Button saveBtn;
    Button deleteBtn;
    Button salirBtn;

    boolean esNuevo;

    Statement stQuery = null;
    ResultSet rsRecords = null;

    UI mainUI;
    String idEmpleado;

    public EmpleadoSalarioForm(String idEmpleado) {
        this.idEmpleado = idEmpleado;
        this.mainUI = UI.getCurrent();

        setWidth("50%");
        setHeight("80%");

        setCaption("SALARIO DE EMPLEADO");

        marginInfo = new MarginInfo(true, true, true, true);

        mainLayout = new VerticalLayout();
        mainLayout.setMargin(new MarginInfo(true, true, false, true));
        mainLayout.setSpacing(true);
        mainLayout.setSizeUndefined();

        setContent(mainLayout);

        createGrid();

        createForm();

        fillData();
    }

    private void createGrid() {
        salarioContaier.addContainerProperty("id", String.class, "0");
        salarioContaier.addContainerProperty("cuentaContable", String.class, "");
        salarioContaier.addContainerProperty("fechaDesde", String.class, "");
        salarioContaier.addContainerProperty("valor", String.class, "0");
        salarioContaier.addContainerProperty("esOrdinario", String.class, "0");

        salarioGrid = new Grid("SALARIO DE EMPLEADO", salarioContaier);
        salarioGrid.setWidth("100%");
        salarioGrid.setImmediate(true);
        salarioGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        salarioGrid.setDescription("Seleccione un registro.");
        salarioGrid.setHeightMode(HeightMode.ROW);
        salarioGrid.setHeightByRows(10);
        salarioGrid.setResponsive(true);
        salarioGrid.getColumn("id").setExpandRatio(1);
        salarioGrid.getColumn("cuentaContable").setExpandRatio(2);
        salarioGrid.getColumn("fechaDesde").setExpandRatio(1);
        salarioGrid.getColumn("valor").setExpandRatio(1);
        salarioGrid.getColumn("esOrdinario").setExpandRatio(1);
        salarioGrid.addSelectionListener(new SelectionEvent.SelectionListener() {
            @Override
            public void select(SelectionEvent event) {
                fillForm(String.valueOf(salarioContaier.getContainerProperty(salarioGrid.getSelectedRow(), "id").getValue()));
                esNuevo = false;
                deleteBtn.setVisible(true);
            }
        });

        mainLayout.addComponent(salarioGrid);
        mainLayout.setComponentAlignment(salarioGrid, Alignment.TOP_CENTER);
    }

    private void createForm() {

        mainLayout.setWidth("100%");

        cuentaContableCbx.setImmediate(true);
        cuentaContableCbx.setNullSelectionAllowed(false);
        cuentaContableCbx.setTextInputAllowed(true);
        cuentaContableCbx.setInvalidAllowed(false);
        cuentaContableCbx.setNewItemsAllowed(false);
        cuentaContableCbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContableCbx.clear();
        cuentaContableCbx.setWidth("100%");
        fillComboCuentaContable();

        desdeDt = new DateField("Desde : ");
        desdeDt.setDateFormat("dd/MM/yyyy");
        desdeDt.setWidth("100%");
        desdeDt.setValue(new java.util.Date());

        montoTxt = new NumberField("Monto : ");
        montoTxt.setInputPrompt("Valor del rubro");
        montoTxt.setDescription("Valor del rubro");
        montoTxt.setDecimalAllowed(true);
        montoTxt.setDecimalPrecision(2);
        montoTxt.setMinimumFractionDigits(2);
        montoTxt.setDecimalSeparator('.');
        montoTxt.setDecimalSeparatorAlwaysShown(true);
        montoTxt.setValue(0d);
        montoTxt.setGroupingUsed(true);
        montoTxt.setGroupingSeparator(',');
        montoTxt.setGroupingSize(3);
        montoTxt.setImmediate(true);
        montoTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        montoTxt.setWidth("100%");

//        mainFormLayout.addComponents(cuentaContableCbx, desdeDt, montoTxt);
        mainFormLayout.addComponents(cuentaContableCbx, montoTxt, esOrdinarioChb);

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
                desdeDt.setValue(new java.util.Date());
                montoTxt.setValue(0.00);
                cuentaContableCbx.select(null);
                cuentaContableCbx.focus();
                esOrdinarioChb.setValue(false);
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
                                if (salarioGrid.getSelectedRow() != null) {
                                    saveData(String.valueOf(salarioContaier.getContainerProperty(salarioGrid.getSelectedRow(), "id").getValue()));
                                }
                                else {
                                    Notification.show("NO HA SELECCIONADO UN REGISTRO.", Notification.Type.ERROR_MESSAGE);
                                    return;
                                }
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
                if (salarioContaier.size() > 0) {
                    if (salarioGrid.getSelectedRow() != null) {

                        ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de eliminar el registro del rubro de salaria del empleado?",
                                "SI", "NO", new ConfirmDialog.Listener() {

                                    public void onClose(ConfirmDialog dialog) {
                                        if (dialog.isConfirmed()) {
                                            deleteSalario(String.valueOf(salarioContaier.getContainerProperty(salarioGrid.getSelectedRow(), "id").getValue()));
                                        }
                                    }
                                });
                    } else {
                        if (salarioGrid.getSelectedRow() == null) {
                            Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
                        }
                    }
                }
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

        buttonsLayout.addComponent(nuevoBtn);
        buttonsLayout.addComponent(saveBtn);
        buttonsLayout.addComponent(deleteBtn);
        buttonsLayout.addComponent(salirBtn);
        buttonsLayout.setComponentAlignment(salirBtn, Alignment.BOTTOM_RIGHT);

        mainLayout.addComponent(buttonsLayout);
        mainLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);

    }

    public void fillData() {

        salarioContaier.removeAllItems();

        String queryString = "";

        queryString = "Select * ";
        queryString += " From empleado_salario ";
        queryString += " Inner Join contabilidad_nomenclatura on contabilidad_nomenclatura.IdNomenclatura = empleado_salario.IdNomenclatura";
        queryString += " Where IdEmpleado = " + idEmpleado;
//System.out.println("queryEmpleado=" + queryString);
        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            Object itemId;
            while (rsRecords.next()) {

                itemId = salarioContaier.addItem();
//System.out.println("CuentaContalbe=" + rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));
                salarioContaier.getContainerProperty(itemId, "id").setValue(rsRecords.getString("Id"));
                salarioContaier.getContainerProperty(itemId, "cuentaContable").setValue(rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));
                salarioContaier.getContainerProperty(itemId, "fechaDesde").setValue(rsRecords.getString("Fecha"));
                salarioContaier.getContainerProperty(itemId, "valor").setValue(rsRecords.getString("Valor"));
                salarioContaier.getContainerProperty(itemId, "esOrdinario").setValue(rsRecords.getString("EsOrdinario").equals("1") ? "SI" : "NO");
            }

        } catch (Exception ex) {
            Logger.getLogger(EmpleadoSalarioForm.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de salario de empleado : " + ex.getMessage());
            Notification.show("Error al intentar leer registros de salario de empleado..!", Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void fillComboCuentaContable() {

        String queryString = "SELECT * FROM contabilidad_nomenclatura ";
        queryString += "WHERE Estatus = 'HABILITADA' ";
//        queryString += " AND NoCuenta IN ('61101003', '61101004', '61101001'");
        queryString += "AND idNomenclatura in (" + ((SopdiUI)mainUI).cuentasContablesDefault.getSueldoOrdinario() + ", ";
        queryString +=                             ((SopdiUI)mainUI).cuentasContablesDefault.getBonificacionDCTO07_2001() + ", ";
        queryString +=                             ((SopdiUI)mainUI).cuentasContablesDefault.getBonificacionDCTO78_89() + ") ";
        queryString += "ORDER BY N5";

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

        desdeDt.setValue(new java.util.Date());
        montoTxt.setValue(0.00);

        String queryString = "";

        queryString = "Select * ";
        queryString += " From empleado_salario ";
        queryString += " Inner Join contabilidad_nomenclatura on contabilidad_nomenclatura.IdNomenclatura = empleado_salario.IdNomenclatura";
        queryString += " Where Id = " + id;
System.out.println("queryEmpleado=" + queryString);
        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {

                cuentaContableCbx.select(rsRecords.getString("IdNomenclatura"));
                montoTxt.setValue(rsRecords.getDouble("Valor"));
//                desdeDt.setValue(rsRecords.getDate("Fecha"));
                esOrdinarioChb.setValue(rsRecords.getString("EsOrdinario").equals("1"));
            }

        } catch (Exception ex) {
            Logger.getLogger(EmpleadoSalarioForm.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de salario de empleado : " + ex.getMessage());
            Notification.show("Error al intentar leer registros de salario de empleado..!", Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void saveData(String id) {
        if (montoTxt.getValue() == null) {
            Notification.show("Por favor ingrese el monto.", Notification.Type.WARNING_MESSAGE);
            montoTxt.focus();
            return;
        }

        if(esOrdinarioChb.getValue()) {
            if(esNuevo) {
                for (Object itemId : salarioContaier.getItemIds()) {
                    if (String.valueOf(salarioContaier.getContainerProperty(itemId, "esOrdinario").getValue()).equals("SI")) {
                        Notification.show("SOLO UN VALOR PUEDE SER ORDINARIO. YA EXISTE UN REGISTRO CON VALOR ORDINARIO", Notification.Type.HUMANIZED_MESSAGE);
                        return;
                    }
                }
            }
            else {
                for (Object itemId : salarioContaier.getItemIds()) {
                    if(itemId != salarioGrid.getSelectedRow()) {
                        if (String.valueOf(salarioContaier.getContainerProperty(itemId, "esOrdinario").getValue()).equals("SI")) {
                            Notification.show("SOLO UN VALOR PUEDE SER ORDINARIO. YA EXISTE UN REGISTRO CON VALOR ORDINARIO", Notification.Type.HUMANIZED_MESSAGE);
                            return;
                        }
                    }
                }
            }
        }

        String queryString;

        try {
            if(esNuevo) {
                queryString = "SELECT * ";
                queryString += " FROM empleado_salario ";
                queryString += " WHERE IdEmpleado = " + idEmpleado;
                queryString += " AND   IdNomenclatura = " + cuentaContableCbx.getValue();
    //System.out.println("queryEmpleadoSALARIO=" + queryString);
                stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                rsRecords = stQuery.executeQuery(queryString);

                if(rsRecords.next()) {
                    Notification.show("NUEVO :  YA EXISTE UN REGISTRO CON ESATA NOMENCLATURA CONTABLE.", Notification.Type.ERROR_MESSAGE);
                    return;
                }
                queryString = "Insert into empleado_salario ";
                queryString += "(IdEmpleado, IdNomenclatura, Fecha, Valor, EsOrdinario)";
                queryString += " Values ";
                queryString += "(";
                queryString += idEmpleado;
                queryString += "," + cuentaContableCbx.getValue();
//                queryString += ",'" + Utileria.getFechaYYYYMMDD_1(desdeDt.getValue()) + "'";
                queryString += ",current_date";
                queryString += ", " + montoTxt.getDoubleValueDoNotThrow();
                queryString += "," + (esOrdinarioChb.getValue() ? 1 : 0);
                queryString += ")";

            } else {

                queryString = "UPDATE empleado_salario SET ";
                queryString += " IdNomenclatura = " + cuentaContableCbx.getValue();
//                queryString += ",Fecha = '" + Utileria.getFechaYYYYMMDD_1(desdeDt.getValue() )+ "'";
                queryString += ",Valor = " + montoTxt.getDoubleValueDoNotThrow();
                queryString += ",EsOrdinario = " + (esOrdinarioChb.getValue() ? 1 : 0);
                queryString += " Where Id = " + id;

            }

System.out.println("saveData="+queryString);

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            fillData();
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
            System.out.println("Error en base datos al intentar hacer update o insert en tabla empleado_salario " + ex);
            ex.printStackTrace();
        }
    }

    public void deleteSalario(String id) {

        desdeDt.setValue(new java.util.Date());
        montoTxt.setValue(0.00);

        String queryString = "";

        queryString = "Delete ";
        queryString += " From empleado_salario ";
        queryString += " Where Id = " + id;
//System.out.println("queryEmpleado=" + queryString);
        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            Notification.show("Registro ha sido eliminado!", Notification.Type.WARNING_MESSAGE);
            fillData();

        } catch (Exception ex) {
            Logger.getLogger(EmpleadoSalarioForm.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar eliminar registros de salario de empleado : " + ex.getMessage());
            Notification.show("Error al intentar eliminar registros de salario de empleado..!", Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}