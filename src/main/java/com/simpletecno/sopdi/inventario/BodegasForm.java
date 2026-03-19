package com.simpletecno.sopdi.inventario;

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
public class BodegasForm extends Window {

    String idBodega;

    UI mainUI;
    Statement stQuery = null;
    ResultSet rsRecords = null;
    String queryString = "";

    VerticalLayout mainForm;

    TextField nombreTxt;
    TextField ubicacionTxt;
    ComboBox estatusCbx;
    TextField razonTxt;

    Button guardarBtn;
    Button salirBtn;

    public BodegasForm(String idBodega) {

        this.mainUI = UI.getCurrent();
        this.idBodega = idBodega;
        setWidth("55%");
        setHeight("55%");
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

        if (this.idBodega.trim().isEmpty()) {
            titleLbl = new Label("NUEVA BODEGA");
            titleLbl.setSizeUndefined();
            titleLbl.addStyleName(Runo.LABEL_H2);
        } else {
            titleLbl = new Label("EDITAR BODEGA");
            titleLbl.setSizeUndefined();
            titleLbl.addStyleName(Runo.LABEL_H2);
        }

        titleLayout.addComponent(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.BOTTOM_CENTER);

        mainForm.addComponent(titleLayout);
        mainForm.setComponentAlignment(titleLayout, Alignment.BOTTOM_CENTER);

        setContent(mainForm);
        crearComponents();

        if (!idBodega.trim().isEmpty()) {
            llenarDatos();
        }
    }

    public void crearComponents() {

        FormLayout form = new FormLayout();
        form.setSpacing(true);
        form.setMargin(true);
        form.setWidth("90%");
        form.setResponsive(true);

        nombreTxt = new TextField("Ingrese el nombre de la bodega.");
        nombreTxt.setWidth("75%");
        form.addComponent(nombreTxt);
        form.setComponentAlignment(nombreTxt, Alignment.TOP_CENTER);

        ubicacionTxt = new TextField("Ingrese la ubicación de la bodega.");
        ubicacionTxt.setWidth("75%");
        form.addComponent(ubicacionTxt);
        form.setComponentAlignment(ubicacionTxt, Alignment.TOP_CENTER);

        razonTxt = new TextField("Ingrese una razón.");
        razonTxt.setWidth("75%");
        form.addComponent(razonTxt);
        form.setComponentAlignment(razonTxt, Alignment.TOP_CENTER);

        estatusCbx = new ComboBox("Seleccione el estatus.");
        estatusCbx.setWidth("75%");
        estatusCbx.addItem("ACTIVA");
        estatusCbx.addItem("INACTIVA");
        estatusCbx.select("ACTIVA");
        estatusCbx.setInvalidAllowed(false);
        estatusCbx.setNewItemsAllowed(false);
        estatusCbx.setFilteringMode(FilteringMode.CONTAINS);
        estatusCbx.setInvalidAllowed(false);
        estatusCbx.setNewItemsAllowed(false);
        estatusCbx.setNullSelectionAllowed(false);

        form.addComponent(estatusCbx);
        form.setComponentAlignment(estatusCbx, Alignment.TOP_CENTER);

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

    public void llenarDatos() {

        queryString = " SELECT * FROM inv_bodega ";
        queryString += " WHERE IdBodega = " + idBodega;

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {

                nombreTxt.setValue(rsRecords.getString("Nombre"));
                ubicacionTxt.setValue(rsRecords.getString("Ubicacion"));
                estatusCbx.select(rsRecords.getString("Estatus"));
                razonTxt.setValue(rsRecords.getString("Razon"));

            }
        } catch (Exception ex1) {
            System.out.println("Error al llenar registro inv_bodega " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void insertarCuentasBancos() {

        try {

            if (nombreTxt.getValue() == null) {
                Notification.show("Por favor selecciona una cuenta ", Notification.Type.WARNING_MESSAGE);
                return;
            }

            if (ubicacionTxt.getValue() == null) {
                Notification.show("Por favor seleccione un banco ", Notification.Type.WARNING_MESSAGE);
                return;
            }

            if (razonTxt.getValue() == null) {
                Notification.show("Por favor seleccione un tipo de moneda ", Notification.Type.WARNING_MESSAGE);
                return;
            }

            if (idBodega.trim().isEmpty()) { /// NUEVO REGISTRO

                queryString = "Insert inv_bodega ";
                queryString += "(Nombre, Ubicacion, IdEmpresa, Estatus, Razon)";
                queryString += " Values ";
                queryString += "('" + nombreTxt.getValue() + "'";
                queryString += ",'" + ubicacionTxt.getValue() + "'";
                queryString += "," + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
                queryString += ",'" + estatusCbx.getValue() + "'";
                queryString += ",'" + razonTxt.getValue() + "'";
                queryString += ")";

            } else {

                queryString = "UPDATE inv_bodega SET ";
                queryString += "  Nombre = '" + nombreTxt.getValue() + "'";
                queryString += ", Ubicacion = '" + ubicacionTxt.getValue() + "'";
                queryString += ", IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
                queryString += ", Estatus = '" + estatusCbx.getValue() + "'";
                queryString += ", Razon = '" + razonTxt.getValue() + "'";
                queryString += " Where IdBodega = " + idBodega;

            }

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            ((InvBodegasView) (mainUI.getNavigator().getCurrentView())).fillBodegasGrid();

            Notification notif = new Notification("", Notification.Type.HUMANIZED_MESSAGE);
            if (idBodega.trim().isEmpty()) {
                notif.setCaption("Registro agregado con exito!");
            } else {
                notif.setCaption("Registro modificado con exito!");
            }

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
            System.out.println("Error en base datos al intentar hacer update o insert en tabla inv_bodega " + ex);
            ex.printStackTrace();
        }
    }

}
