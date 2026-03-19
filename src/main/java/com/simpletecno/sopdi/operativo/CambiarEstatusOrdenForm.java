/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.operativo;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.compras.OrdenCompraView;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Runo;
import com.vaadin.ui.themes.ValoTheme;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author Administrador
 */
public class CambiarEstatusOrdenForm extends Window {

    String idOrdenCompra;
    String estatus;

    UI mainUI;
    Statement stQuery = null;
    String queryString = "";

    VerticalLayout mainForm;

    ComboBox estatusCbx;

    Button guardarBtn;
    Button salirBtn;

    public CambiarEstatusOrdenForm(String idOrdenCompra, String estatus) {

        this.mainUI = UI.getCurrent();
        this.idOrdenCompra = idOrdenCompra;
        this.estatus = estatus;

        setWidth("50%");
        setHeight("40%");
        setResponsive(true);
        setModal(true);

        mainForm = new VerticalLayout();
        mainForm.setWidth("100%");
        mainForm.setMargin(true);
        mainForm.setSpacing(true);

        VerticalLayout titleLayout = new VerticalLayout();
        titleLayout.setResponsive(true);
        titleLayout.setMargin(new MarginInfo(true, false, false, false));
        titleLayout.setWidth("100%");

        Label titleLbl = new Label("CAMBIAR ESTATUS DE LA ORDEN DE COMPRA NO " + idOrdenCompra);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName(Runo.LABEL_H2);

        titleLayout.addComponent(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.BOTTOM_CENTER);

        mainForm.addComponent(titleLayout);
        mainForm.setComponentAlignment(titleLayout, Alignment.BOTTOM_CENTER);

        setContent(mainForm);
        crearComponents();

    }

    public void crearComponents() {

        FormLayout form = new FormLayout();
        form.setSpacing(true);
        form.setMargin(true);
        form.setWidth("90%");
        form.setResponsive(true);

        estatusCbx = new ComboBox("SELECCIONE EL NUEVO ESTATUS :");
        estatusCbx.setWidth("75%");
        estatusCbx.setInvalidAllowed(false);
        estatusCbx.setNewItemsAllowed(false);
        estatusCbx.setFilteringMode(FilteringMode.CONTAINS);
        estatusCbx.setInvalidAllowed(false);
        estatusCbx.setNewItemsAllowed(false);
        estatusCbx.setNullSelectionAllowed(false);
        estatusCbx.addItem("CREADA");
        estatusCbx.addItem("ENVIADA AL PROVEEDOR");
        estatusCbx.addItem("ORDEN REGISTRADA");
        estatusCbx.addItem("PENDIENTE DE COMPLETAR");
        estatusCbx.addItem("CERRADA");
        estatusCbx.select(estatus);

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

    public void insertarCuentasBancos() {

        try {

            if (estatusCbx.getValue() == null) {
                Notification.show("Por favor selecciona un ESTATUS ", Notification.Type.WARNING_MESSAGE);
                return;
            }

            queryString = "UPDATE orden_compra SET ";
            queryString += "  Estado = '" + estatusCbx.getValue() + "'";
            queryString += " WHERE Id = " + idOrdenCompra;

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            ((OrdenCompraView) (mainUI.getNavigator().getCurrentView())).llenarTablaOrdenCompra();
            Notification notif = new Notification("", Notification.Type.HUMANIZED_MESSAGE);
            notif.setCaption("Registro modificado con exito!");
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
            System.out.println("Error en base datos al intentar hacer update del estatus de la tarea " + ex);
            ex.printStackTrace();
        }
    }
}
