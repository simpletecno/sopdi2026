/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.operativo;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
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
public class CambiarEstatusSeguimientoForm extends Window {

    String idTareaSeguimiento;
    String estatus;
    String codigoVisita;

    UI mainUI;
    Statement stQuery = null;
    String queryString = "";

    VerticalLayout mainForm;

    ComboBox estatusCbx;

    Button guardarBtn;
    Button salirBtn;

    public CambiarEstatusSeguimientoForm(String idVisita, String estatus, String codigoVista) {

        this.mainUI = UI.getCurrent();
        this.idTareaSeguimiento = idVisita;
        this.estatus = estatus;
        this.codigoVisita = codigoVista;
        
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

        Label titleLbl = new Label("CAMBIAR ESTATUS DE LA TAREA NO " + idTareaSeguimiento);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName(Runo.LABEL_H2);
        
        Label titleLb2 = new Label("CODIGO : " + codigoVisita);
        titleLb2.setSizeUndefined();
        titleLb2.addStyleName(Runo.LABEL_H2);
        
        titleLayout.addComponent(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.BOTTOM_CENTER);
        
        titleLayout.addComponent(titleLb2);
        titleLayout.setComponentAlignment(titleLb2, Alignment.BOTTOM_CENTER);

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
//        estatusCbx.addItem("CREADA");
//        estatusCbx.addItem("INICIADA");
        estatusCbx.addItem("AUTORIZADA");
//        estatusCbx.addItem("TERMINADA");
        estatusCbx.addItem("RECHAZADA");
//        estatusCbx.addItem("SUSPENDIDA");
//        System.out.println("este es el select e " + estatus);
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

            queryString = "UPDATE visita_inspeccion_tarea SET ";
            queryString += "  Estatus = '" + estatusCbx.getValue() + "'";
            queryString += ", FechaUltimoEstatus = '" + Utileria.getFechaYYYYMMDD_1(new java.util.Date()) + "'";
            queryString += " WHERE IdVisitaInspeccionTarea = " + idTareaSeguimiento;

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            ((InspectionsTaskTrackView) (mainUI.getNavigator().getCurrentView())).fillInspectionsTaskGrid();
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
    /*
    void endTask(Object taskId) {
        ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de CONCLUIR la tarea?",
                "SI", "NO", new ConfirmDialog.Listener() {

            public void onClose(ConfirmDialog dialog) {
                if (dialog.isConfirmed()) {
                    queryString = "Update visita_inspeccion_tarea Set ";
                    queryString += " Estatus = 'CONCLUIDA'";
                    queryString += ", FechaUltimoEstatus = '" + Utileria.getFechaYYYYMMDD_1(new java.util.Date()) + "'";
                    queryString += " Where IdVisitaInspeccionTarea  = " + String.valueOf(inspectionsTaskTrackGrid.getContainerDataSource().getItem(taskId).getItemProperty(ID_PROPERTY).getValue());

                    try {
                        stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                        stQuery.executeUpdate(queryString);

                        container.getContainerProperty(taskId, ESTATUS_PROPERTY).setValue("CONCLUIDA");
                        container.getContainerProperty(taskId, ULTIMO_ESTATUS_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(new java.util.Date()));

                        Notification.show("OPERACION EXITOSA!", Notification.Type.HUMANIZED_MESSAGE);

                    } catch (Exception ex) {
                        Notification.show("Error al actualizar tarea : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                }
            }
        });
    }
     */

}
