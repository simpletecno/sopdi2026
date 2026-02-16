package com.simpletecno.sopdi.recursoshumanos;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.ShortcutAction;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joseaguirre
 */
public class PlanRHShow extends Window {

    VerticalLayout  mainLayout;
    MarginInfo marginInfo;

    Button exitBtn;

    UI mainUI;

    String idPlanTrabajoIdex;
    String centroCosto;

    public PlanRHShow(String idPlanTrabajoIdex, String centroCosto) {
        this.idPlanTrabajoIdex = idPlanTrabajoIdex;
        this.centroCosto = centroCosto;

        this.mainUI = UI.getCurrent();

        setResponsive(true);
        setCaption("RH del plan de trabajo IDEX");
        setModal(true);
        setWidth("70%");
        setHeight("50%");

        marginInfo = new MarginInfo(true, true, false, true);

        mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();

        setContent(mainLayout);

        showRH();
    }

    private void showRH() {

        IndexedContainer rhContainer = new IndexedContainer();
        rhContainer.addContainerProperty("idEmpleado", String.class, "");
        rhContainer.addContainerProperty("nombre", String.class, "");
        rhContainer.addContainerProperty("cargo", String.class, "");
        rhContainer.addContainerProperty("esJefe", String.class, "");
        rhContainer.addContainerProperty("estatus", String.class, "");

        Grid rhGrid = new Grid("", rhContainer);
        rhGrid.setWidth("100%");
        rhGrid.setImmediate(true);
        rhGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
//        rhGrid.setDescription("Seleccione un registro.");
        rhGrid.setHeightMode(HeightMode.ROW);
        rhGrid.setHeightByRows(15);
        rhGrid.setResponsive(true);
        rhGrid.setEditorBuffered(false);
        rhGrid.setSizeFull();

        rhGrid.getColumn("idEmpleado").setExpandRatio(1);
        rhGrid.getColumn("nombre").setExpandRatio(4);
        rhGrid.getColumn("cargo").setExpandRatio(1);
        rhGrid.getColumn("esJefe").setExpandRatio(1);
        rhGrid.getColumn("estatus").setExpandRatio(2);

        mainLayout.addComponent(rhGrid);
        mainLayout.setComponentAlignment(rhGrid, Alignment.MIDDLE_CENTER);

        String queryString;

        queryString = "SELECT *";
        queryString += " FROM plan_trabajo_idex_rh ";
        queryString += " INNER JOIN proveedor ON proveedor.IdProveedor = plan_trabajo_idex_rh.IdEmpleado";
        queryString += " WHERE plan_trabajo_idex_rh.IdPlanTrabajoIdex = " + idPlanTrabajoIdex;
        queryString += " AND proveedor.IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
        queryString += " ORDER BY proveedor.Nombre";

        int rh1 = 0, rh2 = 0;

        try {

            Statement stQuery;
            ResultSet rsRecords;

            stQuery  = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { // POR CADA EMPLEADO RH1 Y RH2
                do {
                    if (rsRecords.getString("Cargo").equals("RH1")) {
                        rh1++;
                        }
                        if (rsRecords.getString("Cargo").equals("RH2")) {
                            rh2++;
                        }

                        Object itemId;
                        itemId = rhContainer.addItem();

                        rhContainer.getContainerProperty(itemId, "idEmpleado").setValue(rsRecords.getString("IdEmpleado"));
                        rhContainer.getContainerProperty(itemId, "nombre").setValue(rsRecords.getString("Nombre"));
                        rhContainer.getContainerProperty(itemId, "cargo").setValue(rsRecords.getString("Cargo"));
                        rhContainer.getContainerProperty(itemId, "esJefe").setValue(rsRecords.getString("EsJefe").equals("1") ? "SI" : "");
                        rhContainer.getContainerProperty(itemId, "estatus").setValue(rsRecords.getString("EstatusTrabajo"));

                } while (rsRecords.next());
            }
        } catch (Exception ex) {
            Logger.getLogger(PlanRHShow.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de detalle de empleados : " + ex.getMessage());
            Notification.show("Error al intentar leer detalle de RH..!", Notification.Type.ERROR_MESSAGE);
        }
        exitBtn = new Button("Salir");
        exitBtn.setIcon(FontAwesome.SIGN_OUT);
        exitBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        exitBtn.setClickShortcut(ShortcutAction.KeyCode.ESCAPE);
        exitBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();
            }
        });
        mainLayout.addComponent(exitBtn);
        mainLayout.setComponentAlignment(exitBtn, Alignment.BOTTOM_CENTER);
    }
}
