package com.simpletecno.sopdi.operativo;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.Property;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.sql.ResultSet;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ProgramaTrabajoFechasForm extends  Window {
    VerticalLayout mainLayout;
    MarginInfo marginInfo;

    FormLayout grupoTrabajoForm;

    Button saveBtn;

    Statement stQuery = null;

    DateField fechaRealInicioDt;
    DateField fechaRealFinDt;
    ComboBox razonCbx;
    TextField observacionesTxt;

    UI mainUI;

    String idPlanTrabajo;
    String centroCosto;
    String idex;
    String descripcion;
    String fechaInicio;
    String fechaFin;
    boolean esSupervisor;
    boolean esInicio;

    Date date1;
    Date date2;

    public ProgramaTrabajoFechasForm(
            String idPlanTrabajo,
            String centroCosto,
            String idex,
            String descripcion,
            String fechaInicio,
            String fechaFin,
            boolean esSupervisor,
            boolean esInicio)
     {
        this.mainUI = UI.getCurrent();
        this.idPlanTrabajo = idPlanTrabajo;
        this.centroCosto = centroCosto;
        this.idex = idex;
        this.descripcion = descripcion;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.esSupervisor = esSupervisor;
        this.esInicio = esInicio;

        setResponsive(true);
        setCaptionAsHtml(true);
        setCaption("Fechas reales REVISION DE TAREA : Centro Costo ["
                + centroCosto
                + "]<br>IDEX [" + idex
                + " " + descripcion
                + "]</br>");

         setHeight("40%");
         if (mainUI.getPage().getBrowserWindowWidth() >= 736) {
             setWidth("60%");
         }
         else {
             setWidth("30%");
         }

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
                saveGroupIdexFechas();
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
        grupoTrabajoForm.setWidth("100%");

        fechaRealInicioDt = new DateField("Fecha real inicio " + (esSupervisor ? "según Supervidor" : "según Maestro") );
        fechaRealInicioDt.setDateFormat("dd/MM/yyyy");
        fechaRealInicioDt.addStyleName(ValoTheme.DATEFIELD_LARGE);
        if (fechaRealInicioDt.getValue() == null) {
            date1 = null;
            try {
                date1 = new SimpleDateFormat("dd/MM/yyyy").parse(fechaInicio);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            fechaRealInicioDt.setValue(date1);
        }
        fechaRealInicioDt.setWidth("100%");

        fechaRealInicioDt.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                llenarRazonAtraso(fechaRealInicioDt.getValue().equals(date1));
            }
        });

        fechaRealFinDt = new DateField("Fecha real fin " + (esSupervisor ? "según Supervidor" : "según Maestro") );
        fechaRealFinDt.setDateFormat("dd/MM/yyyy");
        fechaRealFinDt.addStyleName(ValoTheme.DATEFIELD_LARGE);
        if (fechaRealFinDt.getValue() == null) {
            date2 = null;
            try {
                date2 = new SimpleDateFormat("dd/MM/yyyy").parse(fechaFin);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            fechaRealFinDt.setValue(date2);
        }
        fechaRealFinDt.setWidth("100%");

        fechaRealFinDt.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                llenarRazonAtraso(fechaRealFinDt.getValue().equals(date2));
            }
        });

        if(esInicio) {
            fechaRealFinDt.setVisible(false);
        }
        else {
            fechaRealInicioDt.setVisible(false);
        }

        razonCbx = new ComboBox("Razón :");

        llenarRazonAtraso(true);

        observacionesTxt = new TextField("Observaciones:");
        observacionesTxt.setWidth("100%");

        grupoTrabajoForm.addComponent(fechaRealInicioDt);
        grupoTrabajoForm.addComponent(fechaRealFinDt);
        grupoTrabajoForm.addComponent(razonCbx);
        grupoTrabajoForm.addComponent(observacionesTxt);

        tab1Layout.addComponent(grupoTrabajoForm);
        tab1Layout.setComponentAlignment(grupoTrabajoForm, Alignment.MIDDLE_CENTER);

        mainLayout.addComponent(tab1Layout);
        mainLayout.setComponentAlignment(tab1Layout, Alignment.TOP_CENTER);

    }

    private void saveGroupIdexFechas() {

        if(fechaRealFinDt.getValue() != null) {
            if (fechaRealInicioDt.getValue().after(fechaRealFinDt.getValue())) {
                Notification.show("FECHAS SON INCORRECTAS, REVISE!", Notification.Type.ERROR_MESSAGE);
                return;
            }
        }

        String queryString;

        queryString = "UPDATE plan_trabajo_idex ";
        if(esInicio) {

            if (esSupervisor) {
                queryString += " SET FechaInicioSegunSupervisor = '" + Utileria.getFechaYYYYMMDD_1(fechaRealInicioDt.getValue()) + "'";
                queryString += ", RazonCambioInicioSupervisor = '" + razonCbx.getValue() + "'";
                queryString += ", IdSupervisorInicio = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrIdProveedor();
            }
            else {
                queryString += " SET FechaInicioSegunMaestro = '" + Utileria.getFechaYYYYMMDD_1(fechaRealInicioDt.getValue()) + "'";
                queryString += ", RazonCambioInicioMaestro = '" + razonCbx.getValue() + "'";
                queryString += ", IdMaestroInicio = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrIdProveedor();
            }
        }
        else { // es fin
            if (esSupervisor) {
                queryString += " SET FechaFinSegunSupervisor = '" + Utileria.getFechaYYYYMMDD_1(fechaRealFinDt.getValue()) + "'";
                queryString += ", RazonCambioFinSupervisor = '" + razonCbx.getValue() + "'";
                queryString += ", IdSupervisorFin = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrIdProveedor();
            } else {
                queryString += " SET FechaFinSegunMaestro = '" + Utileria.getFechaYYYYMMDD_1(fechaRealFinDt.getValue()) + "'";
                queryString += ", RazonCambioFinMaestro = '" + razonCbx.getValue() + "'";
                queryString += ", IdMaestroFin = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrIdProveedor();
            }
        }
        queryString += ",Observaciones = '" + observacionesTxt.getValue() + "'";
        queryString += " WHERE Id = " + idPlanTrabajo;

//System.out.println("queryString="+queryString);

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            queryString = "UPDATE project_tarea, project ";
            if(esInicio) {
                queryString += " SET project_tarea.FechaRealInicio = '" + Utileria.getFechaYYYYMMDD_1(fechaRealInicioDt.getValue()) + "'";
            }
            else { // es fin
                queryString += " SET project_tarea.FechaRealFin = '" + Utileria.getFechaYYYYMMDD_1(fechaRealFinDt.getValue()) + "'";
            }
            queryString += ",project_tarea.Estatus = 'EJECUTADA'";
            queryString += " WHERE project_tarea.Idex = '" + idex + "'";
            queryString += " AND project_tarea.IdProject = project.Id ";
            queryString += " AND project.Estatus = 'ACTIVO'";

            stQuery.executeUpdate(queryString);

            Notification.show("NUEVAS FECHAS ACEPTADAS!", Notification.Type.HUMANIZED_MESSAGE);

            close();
        }
        catch(Exception ex)
        {
            Notification.show("Error al actualizar fechas del IDEX de este PROGRAMA de trabajo : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void llenarRazonAtraso(Boolean mismaFecha){

        String queryString;
        ResultSet rsRecords;
        String primero = "";

        razonCbx.setWidth("100%");
        razonCbx.setNewItemsAllowed(false);
        razonCbx.setInvalidAllowed(false);
        razonCbx.setFilteringMode(FilteringMode.CONTAINS);
        razonCbx.setNullSelectionAllowed(false);
        razonCbx.removeAllItems();
        if(!mismaFecha){
            try {
                queryString = "SELECT * FROM razon_atraso_idex";

                stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                rsRecords = stQuery.executeQuery(queryString);

                if (rsRecords.next()){
                    primero = rsRecords.getString("Razon");
                    do {
                        razonCbx.addItem(rsRecords.getString("Razon"));
                    } while (rsRecords.next());
                }
                razonCbx.select(primero);
            }
            catch(Exception ex)
            {
                Notification.show("ERROR: NO SE ENCONTRARON RAZONES DE ATRASO : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }else{
            razonCbx.addItem("MISMA FECHA");
            razonCbx.select("MISMA FECHA");
        }

    }
}
