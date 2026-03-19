/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.operativo;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
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
 * @author user
 */
public class CambiarEstatusVisitaTareas extends Window {

    FormLayout mainForm;
    ComboBox estatusCbx;

    Button guardarBtn;

    String idTarea;
    String queryString;

    UI mainUI;
    Statement stQuery;

    public CambiarEstatusVisitaTareas(String idTarea) {
        this.idTarea = idTarea;
        this.mainUI = UI.getCurrent();
        setResponsive(true);      

        mainForm = new FormLayout();
        MarginInfo marginInfo = new MarginInfo(false,true,true,true);
        mainForm.setMargin(marginInfo);
        mainForm.setSpacing(true);

        estatusCbx = new ComboBox("Estatus :");
        estatusCbx.addStyleName(ValoTheme.COMBOBOX_HUGE);
        estatusCbx.setWidth("14em");
//        estatusCbx.addItem("CREADA");
//        estatusCbx.addItem("INICIADA");
//        estatusCbx.addItem("TERMINADA");
        estatusCbx.addItem("AUTORIZADA");
        estatusCbx.addItem("RECHAZADA");
//        estatusCbx.addItem("SUSPENDIDA");

        guardarBtn = new Button("Cambiar");
        guardarBtn.setIcon(FontAwesome.SAVE);
        guardarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        guardarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                modificarEstatus();
            }
        });

        Button salirBtn = new Button("Salir");
        salirBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        salirBtn.setIcon(FontAwesome.ARROW_RIGHT);
        salirBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();
            }
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.addComponents(salirBtn,guardarBtn);
        buttonsLayout.setComponentAlignment(salirBtn, Alignment.BOTTOM_LEFT);
        buttonsLayout.setComponentAlignment(guardarBtn, Alignment.BOTTOM_CENTER);

        mainForm.addComponent(estatusCbx);
        mainForm.addComponent(buttonsLayout);

        Label titleLbl = new Label("Cambio de estatus de tarea");
        titleLbl.addStyleName(Runo.LABEL_H2);
        titleLbl.setSizeUndefined();

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setWidth("100%");
        titleLayout.setMargin(true);
        titleLayout.addComponent(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.TOP_CENTER);

        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setSizeUndefined();
        contentLayout.addComponents(titleLayout,mainForm);
        contentLayout.setComponentAlignment(titleLayout, Alignment.TOP_CENTER);
        contentLayout.setComponentAlignment(mainForm, Alignment.TOP_CENTER);

        setContent(contentLayout);

    }

    public void modificarEstatus() {
        try {

            queryString = " Update visita_inspeccion_tarea SET ";
            queryString += " Estatus = '" + estatusCbx.getValue() + "'";            
            queryString += " FechaUltimoEstatus = '" + Utileria.getFechaYYYYMMDD_1(new java.util.Date()) + "'";
            queryString += " WHERE IdVisitaInspeccionTarea = " + idTarea;

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);
            
            Notification.show("Estatus de tarea modificado con exito!", Notification.Type.HUMANIZED_MESSAGE);
            ((InspectionsTaskTrackView) (mainUI.getNavigator().getCurrentView())).fillInspectionsTaskGrid();

            close();
        } catch (SQLException ex) {
            System.out.println("error al modificar estatus" + ex);

        }
    }

}
