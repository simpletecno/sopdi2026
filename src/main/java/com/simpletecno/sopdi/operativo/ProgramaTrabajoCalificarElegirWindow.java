/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.operativo;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.data.Property;
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
public class ProgramaTrabajoCalificarElegirWindow extends Window {

    FormLayout mainForm;
    OptionGroup valueOption;
    Button guardarBtn;

    String tipo;
    String idPlanTrabajoIdex;
    String idCaracteristicaOEmpleado;
    Object selectedRow;
    IndexedContainer container;

    PreparedStatement stPreparedQuery;
    ResultSet rsRecords;

    UI mainUI;

    public ProgramaTrabajoCalificarElegirWindow(
            String tipo,
            Object selectedRow,
            IndexedContainer container
    ) {
        this.tipo = tipo;
        this.idPlanTrabajoIdex = idPlanTrabajoIdex;
        this.selectedRow = selectedRow;
        this.container = container;

        this.mainUI = UI.getCurrent();
        setResponsive(true);      

        mainForm = new FormLayout();
        MarginInfo marginInfo = new MarginInfo(false,true,true,true);
        mainForm.setMargin(marginInfo);
        mainForm.setSpacing(true);

        valueOption = new OptionGroup("Valores : ");
        valueOption.addItems("1","2", "3", "4", "5");
        valueOption.select("1");
        valueOption.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);

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

        mainForm.addComponent(valueOption);
        mainForm.addComponent(buttonsLayout);

        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setSizeUndefined();

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setWidth("100%");
        titleLayout.setMargin(false);

        Label titleLbl = new Label("Caracteristica");
        if(tipo.equals("PERSONAL")) {
            titleLbl.setValue("EMPLEADO");
        }
        titleLbl.addStyleName(ValoTheme.LABEL_H1);
        titleLbl.setSizeUndefined();

        titleLayout.addComponent(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.TOP_CENTER);

        contentLayout.addComponent(titleLayout);
        contentLayout.setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        HorizontalLayout titleLayout2 = new HorizontalLayout();
        titleLayout2.setWidth("100%");
        titleLayout2.setMargin(new MarginInfo(false,false,true, false));

        Label titleLbl2 = new Label("");

        if(tipo.equals("CALIDAD")) {
            titleLbl2 = new Label("Caracteristica : " + container.getContainerProperty(selectedRow, "caracteristica").getValue());
        }
        if(tipo.equals("PERSONAL")) {
            titleLbl2 = new Label("Empleado : " + container.getContainerProperty(selectedRow, "empleado").getValue());
        }
        titleLbl2.addStyleName(ValoTheme.LABEL_H3);
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

            container.getContainerProperty(selectedRow, "valor").setValue(valueOption.getValue());

            close();
            
        } catch (Exception ex) {
            System.out.println("Error" + ex);
            Notification.show("Error al intentar actualizar registro de calificación ", Notification.Type.ERROR_MESSAGE);
        }
    }

}
