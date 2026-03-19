/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.operativo;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author user
 */
public class InspectionTextWindow extends Window {

    FormLayout mainForm;
    TextField  puntoAgendaTxt;
    TextArea resolucionTxt;
    Button guardarBtn;

    String visitaId;
    String codigoVisita;
    Integer corr;
    Object item;
    Integer id;

    Statement stQuery;

    UI mainUI;

    public InspectionTextWindow(
            String visitaId, 
            String codigoVisita,
            Integer corr,
            Object item,
            Integer id) {
        this.visitaId = visitaId;
        this.codigoVisita =  codigoVisita;
        this.corr = corr;
        this.item = item;
        this.id = id;
        this.mainUI = UI.getCurrent();
        setResponsive(true);      

        mainForm = new FormLayout();
        MarginInfo marginInfo = new MarginInfo(false,true,true,true);
        mainForm.setMargin(marginInfo);
        mainForm.setSpacing(true);

        puntoAgendaTxt = new TextField("Punto de Agenda : ");
        puntoAgendaTxt.setWidth("20em");
        
        resolucionTxt = new TextArea("Resolución:");
        resolucionTxt.setWidth("50em");
        resolucionTxt.setHeight("10em");
        resolucionTxt.setMaxLength(1024);
        resolucionTxt.setDescription("Ingrese hasta 1024 caracteres...");

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

        mainForm.addComponent(puntoAgendaTxt);
        mainForm.addComponent(resolucionTxt);
        mainForm.addComponent(buttonsLayout);

        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setSizeUndefined();

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setWidth("100%");
        titleLayout.setMargin(false);

        Label titleLbl = new Label("Punto de Agenda y Resolución");
        titleLbl.addStyleName(ValoTheme.LABEL_H1);
        titleLbl.setSizeUndefined();

        titleLayout.addComponent(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.TOP_CENTER);

        contentLayout.addComponent(titleLayout);
        contentLayout.setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        HorizontalLayout titleLayout2 = new HorizontalLayout();
        titleLayout2.setWidth("100%");
        titleLayout2.setMargin(new MarginInfo(false,false,true, false));

        Label titleLbl2 = new Label("CODIGO : " + codigoVisita);
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
            if(!visitaId.equals("")) {
                String queryString = "";
                if (!((VisitasView) (mainUI.getNavigator().getCurrentView())).agendaList.contains(corr)) {
                    queryString = "INSERT INTO visita_inspeccion_agenda (";
                    queryString += "IdVisitaInspeccion, IdEmpresa, CodigoVisita, PuntoAgenda, Resolucion, CreadoUsuario, CreadoFechaYHora) Values ";
                    queryString += "(" + visitaId + ", ";
                    queryString += ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId() + ", ";
                    queryString += "'" + codigoVisita + "', ";
                    queryString += "'" + puntoAgendaTxt.getValue() + "', ";
                    queryString += "'" + resolucionTxt.getValue() + "', ";
                    queryString += ((SopdiUI) mainUI).sessionInformation.getStrUserId() + ", ";
                    queryString += "current_timestamp) ";
                    ((VisitasView) (mainUI.getNavigator().getCurrentView())).agendaList.add(corr);
                    ((VisitasView) (mainUI.getNavigator().getCurrentView())).addGridAgenda(false);

                } else {
                    queryString += "UPDATE visita_inspeccion_agenda SET ";
                    queryString += "IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId() + ", ";
                    queryString += "PuntoAgenda = '" + puntoAgendaTxt.getValue() + "', ";
                    queryString += "Resolucion = '" + resolucionTxt.getValue() + "', ";
                    queryString += "CreadoUsuario = " + ((SopdiUI) mainUI).sessionInformation.getStrUserId() + ", ";
                    queryString += "CreadoFechaYHora = current_timestamp ";
                    queryString += "WHERE IdAgenda = " + id;
                }


                stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                stQuery.executeUpdate(queryString);

                if (!((VisitasView) (mainUI.getNavigator().getCurrentView())).agendaList.contains(corr)) ((VisitasView) (mainUI.getNavigator().getCurrentView())).addGridAgenda(false);

            }else{
                if (!((VisitasView) (mainUI.getNavigator().getCurrentView())).agendaList.contains(corr)) {
                    ((VisitasView) (mainUI.getNavigator().getCurrentView())).agendaList.add(corr);
                }
            }
            ((VisitasView) (mainUI.getNavigator().getCurrentView())).agendaYResolucionesContainer.getContainerProperty(item, "Punto de Agenda").setValue(puntoAgendaTxt.getValue());
            ((VisitasView) (mainUI.getNavigator().getCurrentView())).agendaYResolucionesContainer.getContainerProperty(item, "Resolucion").setValue(resolucionTxt.getValue());
            close();
            
        } catch (SQLException ex) {
            System.out.println("Error" + ex);
            Notification.show("Error al intentar actualizar registro de visita ", Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

}
