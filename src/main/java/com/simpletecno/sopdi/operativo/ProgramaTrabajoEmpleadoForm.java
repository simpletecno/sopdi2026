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
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 *
 * @author joseaguirre
 */
public class ProgramaTrabajoEmpleadoForm extends Window {

    VerticalLayout mainLayout;
    MarginInfo  marginInfo;

    Button saveBtn;

    Statement stQuery = null;
    Statement stQuery1 = null;
    ResultSet rsRecords = null;
    ResultSet rsRecords1 = null;
    PreparedStatement stPreparedQuery;

    ComboBox empleadoCbx;

    IndexedContainer rhContainer;
    String idPlanTrabajoIdex;
    String fechaInicio;
    UI mainUI;

    public ProgramaTrabajoEmpleadoForm(
            IndexedContainer rhContainer,
            String idPlanTrabajoIdex,
            String idexName,
            String fechaInicio
    ) {
        this.rhContainer = rhContainer;
        this.idPlanTrabajoIdex = idPlanTrabajoIdex;
        this.fechaInicio = fechaInicio;
        this.mainUI = UI.getCurrent();

        setResponsive(true);
        setCaption("IDEX : " + idexName);
        setWidth("30%");
        setHeight("30%");
        
        marginInfo = new MarginInfo(true,true,false,true);
        
        mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();

        empleadoCbx = new ComboBox("Empleado : ");
        empleadoCbx.setWidth("100%");
        empleadoCbx.setInputPrompt("<<Elija empleado>>");
        empleadoCbx.setNewItemsAllowed(false);
        empleadoCbx.setInvalidAllowed(false);
        empleadoCbx.setTextInputAllowed(true);
        empleadoCbx.setNullSelectionAllowed(false);
        empleadoCbx.setFilteringMode(FilteringMode.CONTAINS);
        empleadoCbx.setPageLength(10);
        empleadoCbx.addStyleName(ValoTheme.COMBOBOX_HUGE);
        empleadoCbx.addContainerProperty("nombre", String.class, "");
        empleadoCbx.addContainerProperty("cargo", String.class, "");
        empleadoCbx.addContainerProperty("esJefe", String.class, "");

        llenarComboEmpleados();

        mainLayout.addComponent(empleadoCbx);

        saveBtn = new Button("Guardar");
        saveBtn.setIcon(FontAwesome.SAVE); 
        saveBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        saveBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick ( Button.ClickEvent event )
            {
                saveRH();
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

    void llenarComboEmpleados() {

        empleadoCbx.removeAllItems();

        String queryString = "SELECT IdProveedor, Nombre, Cargo, EsJefe";
        queryString += " FROM proveedor";
        queryString += " WHERE EsPlanilla = 1";
        queryString += " AND INHABILITADO = 0";
        queryString += " AND Cargo IN ('RH1', 'RH2')";
        queryString += " AND EstatusTrabajo NOT IN ('AUSENTE', 'DE BAJA') ";
        queryString += " AND IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
        queryString += " ORDER BY Nombre";

        try {
            stQuery  = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery1 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { // POR CADA EMPLEADO RH1 Y RH2

                do {
                    queryString = "SELECT plan_trabajo_idex_rh.Id ";
                    queryString += " FROM plan_trabajo_idex_rh";
                    queryString += " INNER JOIN plan_trabajo_idex ON plan_trabajo_idex.Id = plan_trabajo_idex_rh.idPlanTrabajoIdex";
                    queryString += " WHERE plan_trabajo_idex_rh.IdEmpleado = " + rsRecords.getString("IdProveedor");
                    queryString += " AND '" + fechaInicio + "'";
                    queryString += "     BETWEEN DATE(plan_trabajo_idex.FechaInicioPlaneada) AND DATE(plan_trabajo_idex.FechaFinPlaneada)";

System.out.println("query RH=" + queryString);

                    rsRecords1 = stQuery1.executeQuery(queryString);

                    if (rsRecords1.next() == false) { // si esta libre
System.out.println("query RHRH=" + queryString);
                        empleadoCbx.addItem(rsRecords.getString("IdProveedor"));
                        empleadoCbx.setItemCaption(rsRecords.getString("IdProveedor"), rsRecords.getString("IdProveedor") + " " + rsRecords.getString("Nombre"));
                        empleadoCbx.getContainerProperty(rsRecords.getString("IdProveedor"), "nombre").setValue(rsRecords.getString("Nombre"));
                        empleadoCbx.getContainerProperty(rsRecords.getString("IdProveedor"), "cargo").setValue(rsRecords.getString("Cargo"));
                        empleadoCbx.getContainerProperty(rsRecords.getString("IdProveedor"), "esJefe").setValue((rsRecords.getString("EsJefe").equals("1") ? "SI" : "NO"));
                    }
                } while (rsRecords.next());
            } //no hay empleados libres
        }
        catch(Exception ex1) {
            Notification.show("ERROR FATAL DEL SISTEMA", Notification.Type.ERROR_MESSAGE);
            System.out.println("ERROR AL INTENTAR BUSCAR CATALOGO DE EMPLEADOS : " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    private void saveRH() {

        if(empleadoCbx.getValue() == null) {
            Notification.show("Por favor, elija al empleado!", Notification.Type.WARNING_MESSAGE);
            empleadoCbx.focus();
            return;
        }

        String queryString;

        try {
            queryString =  "Insert Into plan_trabajo_idex_rh (idPlanTrabajoIdex, IdEmpleado, EsJefe, Cargo)";
            queryString += " Values (";
            queryString +=  idPlanTrabajoIdex;
            queryString += ","  + empleadoCbx.getValue();
            queryString += ",'" + empleadoCbx.getContainerProperty(empleadoCbx.getValue(), "esJefe").getValue() + "'";
            queryString += ",'" + empleadoCbx.getContainerProperty(empleadoCbx.getValue(), "cargo").getValue() + "'";
            queryString += ")";

            stPreparedQuery  = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().prepareStatement(queryString, Statement.RETURN_GENERATED_KEYS);
            stPreparedQuery.executeUpdate();
            rsRecords = stPreparedQuery.getGeneratedKeys();

            rsRecords.next();

            Notification.show("OPERACION EXITOSA!", Notification.Type.HUMANIZED_MESSAGE);

            Object itemId = rhContainer.addItem();

            rhContainer.getContainerProperty(itemId, ProgramaTrabajoView.ID_PROPERTY).setValue(rsRecords.getString(1));
            rhContainer.getContainerProperty(itemId, ProgramaTrabajoView.IDEMPLEADO_PROPERTY).setValue(empleadoCbx.getValue());
            rhContainer.getContainerProperty(itemId, ProgramaTrabajoView.NOMBRE_PROPERTY).setValue(empleadoCbx.getItemCaption(empleadoCbx.getValue()));
            rhContainer.getContainerProperty(itemId, ProgramaTrabajoView.CARGO_PROPERTY).setValue(empleadoCbx.getContainerProperty(empleadoCbx.getValue(), "cargo").getValue());
            rhContainer.getContainerProperty(itemId, ProgramaTrabajoView.ESJEFE_PROPERTY).setValue(empleadoCbx.getContainerProperty(empleadoCbx.getValue(), "esJefe").getValue());

            close();

//            ((GruposTrabajoView)(mainUI.getNavigator().getCurrentView())).fillgroupEmpleadosGrid();
        }
        catch(Exception ex)
        {
            Notification.show("Error al actualizar empleado de PROGRAMA de trabajo : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

}