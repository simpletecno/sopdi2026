package com.simpletecno.sopdi.tesoreria;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.Runo;
import com.vaadin.ui.themes.ValoTheme;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author user
 */
public class CuentasBancosForm extends Window {

    static final String NOCUENTA_PROPERTY = "";

    String idCuentaBancoEdit, tipoTransaccion;

    UI mainUI;
    Statement stQuery = null;
    ResultSet rsRecords = null;
    String queryString = "";

    VerticalLayout mainForm;

    ComboBox cuentaContableCbx;
    ComboBox monedaCbx;
    TextField noCuentaTxt;
    ComboBox proveedorCbx;
    
    Button guardarBtn;
    Button salirBtn;

    public CuentasBancosForm(String tipoTransaccion, String idCuentaBancoEdit) {
        this.mainUI = UI.getCurrent();
        this.tipoTransaccion = tipoTransaccion;
        this.idCuentaBancoEdit = idCuentaBancoEdit;
        setWidth("60%");
        setHeight("50%");
        setResponsive(true);
        setModal(true);

        mainForm = new VerticalLayout();
        mainForm.setWidth("100%");
        mainForm.setMargin(true);
        mainForm.setSpacing(true);

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setResponsive(true);
        titleLayout.setWidth("100%");

        Label titleLbl = new Label("ASIGNACION DE CUENTAS BANCO A " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName().toUpperCase());
        titleLbl.addStyleName(Runo.LABEL_H2);
        titleLbl.setSizeUndefined();

        titleLayout.addComponent(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.BOTTOM_CENTER);

        mainForm.addComponent(titleLayout);
        mainForm.setComponentAlignment(titleLayout, Alignment.BOTTOM_CENTER);

        crearComponents();

        if (this.tipoTransaccion.equals("1")) {
            llenarDatos();
        }

        setContent(mainForm);

    }

    public void crearComponents() {

        FormLayout form = new FormLayout();
        form.setSpacing(true);
        form.setMargin(true);
        form.setWidth("90%");
        form.setResponsive(true);

        cuentaContableCbx = new ComboBox("Seleccione la cuenta de banco.");
        cuentaContableCbx.setIcon(FontAwesome.MONEY);
        cuentaContableCbx.setWidth("75%");
        cuentaContableCbx.addContainerProperty(NOCUENTA_PROPERTY, String.class, "");
        cuentaContableCbx.setInvalidAllowed(false);
        cuentaContableCbx.setNewItemsAllowed(false);
        cuentaContableCbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContableCbx.setInvalidAllowed(false);
        cuentaContableCbx.setNewItemsAllowed(false);
        cuentaContableCbx.setNullSelectionAllowed(false);

        form.addComponent(cuentaContableCbx);
        form.setComponentAlignment(cuentaContableCbx, Alignment.TOP_CENTER);

        monedaCbx = new ComboBox("Seleccione la moneda correspondiente.");
        monedaCbx.setIcon(FontAwesome.BALANCE_SCALE);
        monedaCbx.setWidth("75%");
        monedaCbx.addItem("QUETZALES");
        monedaCbx.addItem("DOLARES");
        monedaCbx.select("QUETZALES");
        monedaCbx.setInvalidAllowed(false);
        monedaCbx.setNewItemsAllowed(false);     
        form.addComponent(monedaCbx);
        form.setComponentAlignment(monedaCbx, Alignment.TOP_CENTER);
        
        noCuentaTxt = new TextField("Ingrese el número de cuenta bancario.");
        noCuentaTxt.setIcon(FontAwesome.BANK);
        noCuentaTxt.setWidth("75%");                       
        form.addComponent(noCuentaTxt);
        form.setComponentAlignment(noCuentaTxt, Alignment.TOP_CENTER);
        
        proveedorCbx = new ComboBox("Seleccione el banco.");
        proveedorCbx.setIcon(FontAwesome.BANK);
        proveedorCbx.setWidth("75%");
        proveedorCbx.setInvalidAllowed(false);
        proveedorCbx.setNewItemsAllowed(false);
        proveedorCbx.setFilteringMode(FilteringMode.CONTAINS);
        proveedorCbx.setInvalidAllowed(false);
        proveedorCbx.setNewItemsAllowed(false);
        proveedorCbx.setNullSelectionAllowed(false);
        form.addComponent(proveedorCbx);
        form.setComponentAlignment(proveedorCbx, Alignment.TOP_CENTER);
       
        llenarComboNomenclatura();
        llenarComboProveedor();

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
                insertarCuentasBancos();
            }
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setResponsive(true);
        buttonsLayout.setSpacing(true);
        buttonsLayout.addComponents(salirBtn, guardarBtn);
        buttonsLayout.setComponentAlignment(salirBtn, Alignment.BOTTOM_LEFT);
        buttonsLayout.setComponentAlignment(guardarBtn, Alignment.BOTTOM_CENTER);

        mainForm.addComponent(form);
        mainForm.setComponentAlignment(form, Alignment.MIDDLE_RIGHT);

        mainForm.addComponent(buttonsLayout);
        mainForm.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);
    }
    
    public void llenarComboProveedor() {
        queryString = " SELECT * FROM proveedor ";
        queryString += " WHERE EsBanco = 1";
        queryString += " ORDER BY Nombre";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) {

                proveedorCbx.addItem(rsRecords.getString("IdProveedor"));
                proveedorCbx.setItemCaption(rsRecords.getString("IdProveedor"), rsRecords.getString("Nombre"));
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar proveedores de tipo banco " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void llenarComboNomenclatura() {
        queryString = " SELECT * FROM contabilidad_nomenclatura ";
        queryString += " WHERE Estatus = 'HABILITADA'";
        queryString += " ORDER BY N5";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) {

                cuentaContableCbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContableCbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("N5"));
                cuentaContableCbx.getItem(rsRecords.getString("IdNomenclatura")).getItemProperty(NOCUENTA_PROPERTY).setValue(rsRecords.getString("NoCuenta"));
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar cuentas contables " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void llenarDatos() {
        
        queryString = " SELECT * FROM contabilidad_cuentas_bancos ";
        queryString += " WHERE IdCuentaBanco = " + idCuentaBancoEdit;

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                cuentaContableCbx.select(rsRecords.getString("IdNomenclatura"));
                monedaCbx.select(rsRecords.getString("Moneda"));
                noCuentaTxt.setValue(rsRecords.getString("NoCuenta"));
                proveedorCbx.select(rsRecords.getString("IdProveedor"));
            }
        } catch (Exception ex1) {
            System.out.println("Error al llenar registro de contabilidad_cuentas_banco " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void insertarCuentasBancos() {

        try {

            if (cuentaContableCbx.getValue() == null) {
                Notification.show("Por favor selecciona una cuenta ", Notification.Type.WARNING_MESSAGE);
                return;
            }
            
            if (monedaCbx.getValue() == null) {
                Notification.show("Por favor seleccione un tipo de moneda ", Notification.Type.WARNING_MESSAGE);
                return;
            }            
            
            if (proveedorCbx.getValue() == null) {
                Notification.show("Por favor seleccione un banco ", Notification.Type.WARNING_MESSAGE);
                return;
            }

            if (tipoTransaccion.equals("0")) {

                queryString = "Insert Into contabilidad_cuentas_bancos ";
                queryString += "(IdEmpresa, IdNomenclatura, IdProveedor, NoCuenta, Moneda)";
                queryString += " Values ";
                queryString += "(" + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
                queryString += "," + cuentaContableCbx.getValue();
                queryString += "," + proveedorCbx.getValue();
                queryString += ",'" + noCuentaTxt.getValue() + "'";
                queryString += ", '" + monedaCbx.getValue() + "'";
                queryString += ")";

            }else{
                
                queryString = "UPDATE contabilidad_cuentas_bancos SET ";
                queryString += "  IdNomenclatura = " + cuentaContableCbx.getValue();
                queryString += ", IdProveedor = " + proveedorCbx.getValue();
                queryString += ", NoCuenta = '" +noCuentaTxt.getValue() + "'";                
                queryString += ", Moneda = '" + monedaCbx.getValue() +"'";                
                queryString += " Where IdCuentasBanco = " +  idCuentaBancoEdit;
                            
            }

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            ((CuentasContablesBancosView) (mainUI.getNavigator().getCurrentView())).llenarTablaCuentas();
            ((CuentasContablesBancosView) (mainUI.getNavigator().getCurrentView())).cuentasGrid.select(null);           

            Notification notif = new Notification("REGISTRO AGREGADO o MODIFICADO EXITOSAMENTE.", Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.CHECK);
            notif.show(Page.getCurrent());
            
            close();

        } catch (SQLException ex) {
            Notification notif = new Notification("HA OCURRIDO UN ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.WARNING);
            notif.show(Page.getCurrent());
            System.out.println("Error en base datos al intentar hacer update o insert en tabla cuentas_bancos " + ex);
            ex.printStackTrace();
        }
    }

}
