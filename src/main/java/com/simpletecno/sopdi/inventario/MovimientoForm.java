package com.simpletecno.sopdi.inventario;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
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
import org.vaadin.ui.NumberField;

/**
 *
 * @author user
 */
public class MovimientoForm extends Window {

    String idMovimiento;

    UI mainUI;
    Statement stQuery = null;
    ResultSet rsRecords = null;
    String queryString = "";

    VerticalLayout mainForm;

    ComboBox productoCbx;
    ComboBox bodegaCbx;
    ComboBox tipoMovimientoCbx;

    NumberField cantidadTxt;
    TextField razonTxt;

    DateField fechaDt;

    Button guardarBtn;
    Button salirBtn;

    public MovimientoForm(String idMoviemiento) {
        this.mainUI = UI.getCurrent();
        this.idMovimiento = idMoviemiento;
        setWidth("50%");
        setHeight("60%");
        setResponsive(true);
        setModal(true);

        mainForm = new VerticalLayout();
        mainForm.setWidth("100%");
        mainForm.setMargin(true);
        mainForm.setSpacing(true);

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setResponsive(true);
        titleLayout.setWidth("100%");

        Label titleLbl;

        if (idMovimiento.trim().isEmpty()) {
            titleLbl = new Label("NUEVO MOVIMIENTO");
            titleLbl.setSizeUndefined();
            titleLbl.addStyleName(Runo.LABEL_H2);
        } else {
            titleLbl = new Label("EDITAR MOVIMIENTO");
            titleLbl.setSizeUndefined();
            titleLbl.addStyleName(Runo.LABEL_H2);
        }

        titleLayout.addComponent(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.BOTTOM_CENTER);

        mainForm.addComponent(titleLayout);
        mainForm.setComponentAlignment(titleLayout, Alignment.BOTTOM_CENTER);

        setContent(mainForm);
        crearComponents();

        if (!idMovimiento.trim().isEmpty()) {
            llenarDatos();
            System.out.println("entro a la funcion + " + idMovimiento);
        }

    }

    public void crearComponents() {

        HorizontalLayout contenedorHorizontal = new HorizontalLayout();
        //contenedorHorizontal.setStyleName("rcorners3");
        contenedorHorizontal.setWidth("80%");

        FormLayout form = new FormLayout();
        form.setSpacing(true);
        form.setMargin(true);
        form.setWidth("100%");
        form.setResponsive(true);

        productoCbx = new ComboBox("Producto : ");
        productoCbx.setWidth("70%");
        productoCbx.setFilteringMode(FilteringMode.CONTAINS);

        bodegaCbx = new ComboBox("Bodega : ");
        bodegaCbx.setWidth("70%");
        bodegaCbx.setFilteringMode(FilteringMode.CONTAINS);

        tipoMovimientoCbx = new ComboBox("Tipo Movimiento : ");
        tipoMovimientoCbx.setWidth("70%");
        tipoMovimientoCbx.setFilteringMode(FilteringMode.CONTAINS);
        tipoMovimientoCbx.addItem("ENTRADA");
        tipoMovimientoCbx.addItem("SALIDA");
        tipoMovimientoCbx.select("ENTRADA");

        fechaDt = new DateField("Fecha : ");
        fechaDt.setDateFormat("dd/MM/yyyy");
        fechaDt.setValue(new java.util.Date());
        fechaDt.setWidth("70%");

        cantidadTxt = new NumberField("Cantidad : ");
        cantidadTxt.setGroupingUsed(true);
        cantidadTxt.setGroupingSeparator(',');
        cantidadTxt.setGroupingSize(3);
        cantidadTxt.setImmediate(true);
        cantidadTxt.setWidth("70%");

        razonTxt = new TextField("Razón : ");
        razonTxt.setWidth("70%");

        form.addComponents(productoCbx, bodegaCbx, tipoMovimientoCbx, fechaDt, cantidadTxt, razonTxt);
        form.setComponentAlignment(productoCbx, Alignment.TOP_CENTER);
        form.setComponentAlignment(bodegaCbx, Alignment.TOP_CENTER);
        form.setComponentAlignment(tipoMovimientoCbx, Alignment.TOP_CENTER);
        form.setComponentAlignment(fechaDt, Alignment.TOP_CENTER);
        form.setComponentAlignment(cantidadTxt, Alignment.TOP_CENTER);
        form.setComponentAlignment(razonTxt, Alignment.TOP_CENTER);

        llenarComboProducto();
        llenarComboBodega();

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

        contenedorHorizontal.addComponent(form);
        contenedorHorizontal.setComponentAlignment(form, Alignment.MIDDLE_RIGHT);

        mainForm.addComponent(contenedorHorizontal);
        mainForm.setComponentAlignment(contenedorHorizontal, Alignment.MIDDLE_RIGHT);

        mainForm.addComponent(buttonsLayout);
        mainForm.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);
    }

    public void llenarComboProducto() {
        queryString = " SELECT *";
        queryString += " FROM inv_producto";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) { //  encontrado                
                productoCbx.addItem(rsRecords.getString("IdProducto"));
                productoCbx.setItemCaption(rsRecords.getString("IdProducto"), rsRecords.getString("Descripcion"));
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar productos: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void llenarComboBodega() {
        queryString = " SELECT * from inv_bodega";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);
            while (rsRecords.next()) { //  encontrado                
                bodegaCbx.addItem(rsRecords.getString("IdBodega"));
                bodegaCbx.setItemCaption(rsRecords.getString("IdBodega"), rsRecords.getString("Nombre"));
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar bodegas: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void llenarDatos() {

        queryString = " SELECT * FROM inv_movimiento ";
        queryString += " WHERE IdInventario = " + idMovimiento;

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {

                productoCbx.select(rsRecords.getString("IdProducto"));
                bodegaCbx.select(rsRecords.getString("IdBodega"));
                tipoMovimientoCbx.select(rsRecords.getString("TipoMovimiento"));
                cantidadTxt.setValue(rsRecords.getString("Cantidad"));
                razonTxt.setValue(rsRecords.getString("Razon"));
                fechaDt.setValue(rsRecords.getDate("FechaMovimiento"));

            }
        } catch (Exception ex1) {
            System.out.println("Error al llenar registro inv_bodega " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void insertarCuentasBancos() {

        try {

            if (productoCbx.getValue() == null) {
                Notification.show("Por favor selecciona un producto ", Notification.Type.WARNING_MESSAGE);
                productoCbx.focus();
                return;
            }

            if (bodegaCbx.getValue() == null) {
                Notification.show("Por favor seleccione una bodega ", Notification.Type.WARNING_MESSAGE);
                bodegaCbx.focus();
                return;
            }

            if (tipoMovimientoCbx.getValue() == null) {
                Notification.show("Por favor seleccione un tipo de movimiento ", Notification.Type.WARNING_MESSAGE);
                tipoMovimientoCbx.focus();
                return;
            }
            if (fechaDt.getValue() == null) {
                Notification.show("Por favor seleccione la fecha del movimiento ", Notification.Type.WARNING_MESSAGE);
                fechaDt.focus();
                return;
            }

            if (razonTxt.getValue().trim().isEmpty()) {
                Notification.show("Por favor seleccione un tipo de moneda ", Notification.Type.WARNING_MESSAGE);
                razonTxt.focus();
                return;
            }

            if (idMovimiento.trim().isEmpty()) { /// NUEVO REGISTRO

                queryString = "Insert into inv_movimiento ";
                queryString += "(IdProducto, IdBodega, TipoMovimiento, FechaMovimiento, Cantidad, Razon, IdUsuario, CreadoFechaYHora)";
                queryString += " Values ";
                queryString += "(" + productoCbx.getValue();
                queryString += "," + bodegaCbx.getValue();
                queryString += ",'" + tipoMovimientoCbx.getValue() + "'";
                queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
                queryString += "," + cantidadTxt.getValue();
                queryString += ",'" + razonTxt.getValue() + "'";
                queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                queryString += ",current_timestamp";
                queryString += ")";

            } else {

                queryString = "UPDATE inv_movimiento SET ";
                queryString += "  IdProducto = " + productoCbx.getValue();
                queryString += ", IdBodega = " + bodegaCbx.getValue();
                queryString += ", TipoMovimiento = '" + tipoMovimientoCbx.getValue() + "'";
                queryString += ", FechaMovimiento = '" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
                queryString += ", Cantidad = " + cantidadTxt.getValue();
                queryString += ", Razon = '" + razonTxt.getValue() + "'";
                queryString += ", IdUsuario = " + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                queryString += ", CreadoFechaYHora = current_timestamp";
                queryString += " Where IdInventario = " + idMovimiento;

            }

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            if (idMovimiento.trim().isEmpty()) {
                queryString = " UPDATE inv_bodega SET ";
                queryString += " FechaUltimoMovimiento = '" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
                queryString += " Where IdBodega = " + bodegaCbx.getValue();

                stQuery.executeUpdate(queryString);
            }

            ((InvBodegasView) (mainUI.getNavigator().getCurrentView())).llenarTablaMovimientos(String.valueOf(bodegaCbx.getValue()));

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
            System.out.println("Error en base datos al intentar hacer update o insert en tabla inv_movimiento " + ex);
            ex.printStackTrace();
        }
    }
}
