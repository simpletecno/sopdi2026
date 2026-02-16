package com.simpletecno.sopdi.operativo;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class ProgramaTrabajoIdexInstruccionesForm extends  Window {
    VerticalLayout mainLayout;
    MarginInfo marginInfo;

    FormLayout grupoTrabajoForm;

    Button saveBtn;

    Statement stQuery = null;
    static PreparedStatement stPreparedQuery;
    ResultSet rsRecords = null;

    TextArea instruccionesTxtA;

    UI mainUI;

    IndexedContainer idexContainer;
    Object selectedItem;
    String idPlanTrabajo;
    String centroCosto;
    String idex;
    String descripcion;
    String instrucciones;

    public ProgramaTrabajoIdexInstruccionesForm(
            IndexedContainer idexContainer,
            Object selectedItem,
            String idPlanTrabajo,
            String centroCosto,
            String idex,
            String descripcion,
            String instrucciones
            )
     {
         this.idexContainer = idexContainer;
         this.selectedItem = selectedItem;
        this.mainUI = UI.getCurrent();
        this.idPlanTrabajo = idPlanTrabajo;
        this.centroCosto = centroCosto;
        this.idex = idex;
        this.descripcion = descripcion;
        this.instrucciones = instrucciones;

        setResponsive(true);
        setCaptionAsHtml(true);
        setCaption("INSTRUCCIONES Centro Costo : "
                + centroCosto
                + "<br>IDEX : " + idex
                + " " + descripcion
                + "</br>");
        setWidth("50%");
        setHeight("50%");

        marginInfo = new MarginInfo(true,true,false,true);

        mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();

        createFormLayout();

        saveBtn = new Button("Guardar");
        saveBtn.setIcon(FontAwesome.SAVE);
        saveBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        saveBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick ( Button.ClickEvent event )
            {
                saveInstrucciones();
            }
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.addComponent(saveBtn);
        buttonsLayout.setComponentAlignment(saveBtn, Alignment.BOTTOM_LEFT);

        mainLayout.addComponent(buttonsLayout);
        mainLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);

        setContent(mainLayout);
    }

    private void createFormLayout() {
        VerticalLayout tab1Layout = new VerticalLayout();
        tab1Layout.setSpacing(true);
        tab1Layout.setMargin(true);
        tab1Layout.setWidth("100%");

        grupoTrabajoForm = new FormLayout();
        grupoTrabajoForm.setMargin(marginInfo);
        grupoTrabajoForm.setSpacing(true);
//        grupoTrabajoForm.addStyleName(ValoTheme.FORMLAYOUT_LIGHT);
        grupoTrabajoForm.setSizeFull();

        instruccionesTxtA = new TextArea("Instrucciones");
        instruccionesTxtA.setWidth("100%");
        instruccionesTxtA.setHeight("10em");
        instruccionesTxtA.setValue(instrucciones);
        instruccionesTxtA.setMaxLength(512);

        grupoTrabajoForm.addComponent(instruccionesTxtA);

        tab1Layout.addComponent(grupoTrabajoForm);
        tab1Layout.setComponentAlignment(grupoTrabajoForm, Alignment.MIDDLE_CENTER);

        mainLayout.addComponent(tab1Layout);
        mainLayout.setComponentAlignment(tab1Layout, Alignment.TOP_CENTER);

    }

    private void saveInstrucciones() {

        String queryString;

        queryString = "UPDATE plan_trabajo_idex ";
        queryString += " SET Instrucciones = '" + instruccionesTxtA.getValue() + "'";
        queryString += " WHERE Id = " + idPlanTrabajo;

//System.out.println("queryString="+queryString);

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            Notification.show("INSTRUCCINOES REGISTRADAS!", Notification.Type.HUMANIZED_MESSAGE);

            idexContainer.getContainerProperty(selectedItem, ProgramaTrabajoView.INSTRUCCIONES_PROPERTY).setValue(instruccionesTxtA.getValue());

            close();
        }
        catch(Exception ex)
        {
            Notification.show("Error al actualizar INSTRUCCIONES del IDEX  : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
