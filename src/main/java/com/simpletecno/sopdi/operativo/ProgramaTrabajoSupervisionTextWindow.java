/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.operativo;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author user
 */
public class ProgramaTrabajoSupervisionTextWindow extends Window {

    FormLayout mainForm;
    TextArea supervisionTxt;
    Button guardarBtn;

    String idPlanTrabajoIdex;
    String idCaracteristica;
    String caracteristica;
    IndexedContainer container;

    PreparedStatement stPreparedQuery;
    ResultSet rsRecords;

    UI mainUI;

    public ProgramaTrabajoSupervisionTextWindow(
            String idPlanTrabajoIdex,
            String idCaracteristica,
            String caracteristica,
            IndexedContainer container
    ) {
        this.idPlanTrabajoIdex = idPlanTrabajoIdex;
        this.idCaracteristica =  idCaracteristica;
        this.caracteristica = caracteristica;
        this.container = container;

        this.mainUI = UI.getCurrent();
        setResponsive(true);      

        mainForm = new FormLayout();
        MarginInfo marginInfo = new MarginInfo(false,true,true,true);
        mainForm.setMargin(marginInfo);
        mainForm.setSpacing(true);

        supervisionTxt = new TextArea("Observación:");
        supervisionTxt.setWidth("50em");
        supervisionTxt.setHeight("10em");
        supervisionTxt.setMaxLength(1024);
        supervisionTxt.setDescription("Ingrese hasta 256 caracteres...");

        guardarBtn = new Button("Guardar");
        guardarBtn.setIcon(FontAwesome.SAVE);
        guardarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        guardarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                guardar();
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

        buttonsLayout.addComponent(salirBtn);
        buttonsLayout.setComponentAlignment(salirBtn, Alignment.BOTTOM_LEFT);
        buttonsLayout.addComponent(guardarBtn);
        buttonsLayout.setComponentAlignment(guardarBtn, Alignment.BOTTOM_CENTER);

        mainForm.addComponent(supervisionTxt);
        mainForm.addComponent(buttonsLayout);

        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setSizeUndefined();

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setWidth("100%");
        titleLayout.setMargin(false);

        Label titleLbl = new Label("Caracteristica y Observación");
        titleLbl.addStyleName(ValoTheme.LABEL_H1);
        titleLbl.setSizeUndefined();

        titleLayout.addComponent(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.TOP_CENTER);

        contentLayout.addComponent(titleLayout);
        contentLayout.setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        HorizontalLayout titleLayout2 = new HorizontalLayout();
        titleLayout2.setWidth("100%");
        titleLayout2.setMargin(new MarginInfo(false,false,true, false));

        Label titleLbl2 = new Label("Caracteristica : " + caracteristica);
        titleLbl2.addStyleName(ValoTheme.LABEL_H2);
        titleLbl2.setSizeUndefined();

        titleLayout2.addComponent(titleLbl2);
        titleLayout2.setComponentAlignment(titleLbl2, Alignment.TOP_CENTER);

        contentLayout.addComponent(titleLayout2);
        contentLayout.setComponentAlignment(titleLayout2, Alignment.TOP_CENTER);

        contentLayout.addComponent(mainForm);
        contentLayout.setComponentAlignment(mainForm, Alignment.TOP_CENTER);

        setContent(contentLayout);

    }

    public void guardar() {
        try {

            String queryString = "";
            queryString = "INSERT INTO plan_trabajo_idex_su (IdPlanTrabajoIdex, IdUsuario, IdCaracteristica, IdEmpleado, FechaYHora, Comentario)";
            queryString += " VALUES (";
            queryString += idPlanTrabajoIdex;
            queryString += "," + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserId();
            queryString += "," + idCaracteristica;
            queryString += "," + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrIdProveedor();
            queryString += ",current_timestamp";
            queryString += ",'" + supervisionTxt.getValue() + "'";
            queryString += ")";

            stPreparedQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().prepareStatement(queryString, Statement.RETURN_GENERATED_KEYS);
            stPreparedQuery.executeUpdate();
            rsRecords = stPreparedQuery.getGeneratedKeys();

            rsRecords.next();

            if (mainUI.getWindows() != null) {
                System.out.println("SIZE="+mainUI.getWindows().size());
                for (Window subWindow : mainUI.getWindows()) {
                    System.out.println("Caption="+subWindow.getCaption());
                    if( subWindow.getCaption().equals("SUPERVISIONES")) {
                        ((ProgramaTrabajoCalificarForm) subWindow).llenarSupervisionesGrid(Integer.parseInt(idCaracteristica));
                        break;
                    }
                }
            }

            close();
            
        } catch (SQLException ex) {
            System.out.println("Error" + ex);
            Notification.show("Error al intentar actualizar registro de supervisiones ", Notification.Type.ERROR_MESSAGE);
        }
    }

}
