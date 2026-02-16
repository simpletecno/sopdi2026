/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.recursoshumanos;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.ui.NumberField;

import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author Administrador
 */
public class EmpleadoPlanillaExtraForm extends Window {

    Object selectedItemId;
    String idPlanillaDetalle;
    String nombreEmpleado;
    String idEmpleado;
    int tipo; // 0: Normal || 1: Liquidacion

    UI mainUI;
    Statement stQuery = null;
    String queryString = "";

    VerticalLayout mainForm;

    public NumberField montoTxt;

    Button guardarBtn;
    Button salirBtn;

    ComboBox tipoModificacionCbx;

    public EmpleadoPlanillaExtraForm(
            Object selectedItem,
            String idPlanillaDetalle,
            String nombreEmpleado,
            String idEmpleado,
            int tipo) {

        this.mainUI = UI.getCurrent();
        this.selectedItemId = selectedItem;
        this.idPlanillaDetalle = idPlanillaDetalle;
        this.nombreEmpleado = nombreEmpleado;
        this.idEmpleado = idEmpleado;
        this.tipo = tipo;

        setWidth("35%");
        setHeight("50%");
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


        Double bono_78_89 = 0d;
        Double bono_aguinaldo = 0d;
        Double decuento = 0d;

        if(tipo == 0){
            bono_78_89 = (Double)((EmpleadoCalculoSalarioView) (mainUI.getNavigator().getCurrentView())).planillaDetalleContainer.getContainerProperty(selectedItemId, EmpleadoCalculoSalarioView.BONO2).getValue();
            bono_aguinaldo =  (Double)((EmpleadoCalculoSalarioView) (mainUI.getNavigator().getCurrentView())).planillaDetalleContainer.getContainerProperty(selectedItemId, EmpleadoCalculoSalarioView.BONO3).getValue();
            decuento = (Double)((EmpleadoCalculoSalarioView) (mainUI.getNavigator().getCurrentView())).planillaDetalleContainer.getContainerProperty(selectedItemId, EmpleadoCalculoSalarioView.DESCUENTO2).getValue();
        }else if(tipo == 1){
            bono_78_89 = (Double)((EmpleadoCalculoLiquidacionView) (mainUI.getNavigator().getCurrentView())).planillaDetalleContainer.getContainerProperty(selectedItemId, EmpleadoCalculoLiquidacionView.BONO2).getValue();
            bono_aguinaldo =  (Double)((EmpleadoCalculoLiquidacionView) (mainUI.getNavigator().getCurrentView())).planillaDetalleContainer.getContainerProperty(selectedItemId, EmpleadoCalculoLiquidacionView.BONO3).getValue();
            decuento = (Double)((EmpleadoCalculoLiquidacionView) (mainUI.getNavigator().getCurrentView())).planillaDetalleContainer.getContainerProperty(selectedItemId, EmpleadoCalculoLiquidacionView.DESCUENTO2).getValue();
        }

        Label titleLbl = new Label(
                "DECUENTOS Y BONIFICACIONES EXTRA PARA:\n" +
                        "<ul>"+
                        "  <li><b>" + nombreEmpleado + "</b></li>"+
                        "</ul> "+
                        "<ul>"+
                        "   <li><b> 78-89 | Monto Actual\t\t: " + bono_78_89 + "</b></li>"+
                        "   <li><b> Aguinaldo | Monto Actual\t: " + bono_aguinaldo + "</b></li>"+
                        "   <li><b> Descuento | Monto Actual\t: " + decuento + "</b></li>"+
                        "</ul> ",
                ContentMode.HTML);
        titleLbl.addStyleName(ValoTheme.LABEL_COLORED);

        titleLayout.addComponent(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.TOP_CENTER);

        mainForm.addComponent(titleLayout);
        mainForm.setComponentAlignment(titleLayout, Alignment.BOTTOM_CENTER);

        setContent(mainForm);
        crearComponents();

    }

    public void crearComponents() {

        VerticalLayout form = new VerticalLayout();
        form.setSpacing(true);
        form.setWidth("100%");
        form.setResponsive(true);

        FormLayout bonoLayout = new FormLayout();
        bonoLayout.setSizeFull();

        tipoModificacionCbx = new ComboBox("Cambiar :");
        tipoModificacionCbx.addItem("78-89");
        tipoModificacionCbx.addItem("Extra");
        tipoModificacionCbx.addItem("Descuento");
        tipoModificacionCbx.setWidth("40%");


        montoTxt = new NumberField("Por :");
        montoTxt.setInputPrompt("Monto del bono");
        montoTxt.setDescription("Monto del bono");
        montoTxt.setDecimalAllowed(true);
        montoTxt.setDecimalPrecision(2);
        montoTxt.setMinimumFractionDigits(2);
        montoTxt.setDecimalSeparator('.');
        montoTxt.setDecimalSeparatorAlwaysShown(true);
        montoTxt.setValue(0d);
        montoTxt.setGroupingUsed(true);
        montoTxt.setGroupingSeparator(',');
        montoTxt.setGroupingSize(3);
        montoTxt.setImmediate(true);
        montoTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        montoTxt.setWidth("40%");

        bonoLayout.addComponent(tipoModificacionCbx);
        bonoLayout.setComponentAlignment(tipoModificacionCbx, Alignment.MIDDLE_RIGHT);

        bonoLayout.addComponent(montoTxt);
        bonoLayout.setComponentAlignment(montoTxt, Alignment.MIDDLE_RIGHT);

        form.addComponent(bonoLayout);
        form.setComponentAlignment(bonoLayout, Alignment.MIDDLE_CENTER);

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
                guardar();
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

    public void guardar() {
        // Validación: debe elegir tipo
        if (tipoModificacionCbx.getValue() == null) {
            Notification notif = new Notification("", Notification.Type.HUMANIZED_MESSAGE);
            notif.setCaption("No ha seleccionado un tipo de cambio (78-89 / Extra / Descuento)");
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.CHECK);
            notif.show(Page.getCurrent());
            return;
        }

        String tipoCambio = (String) tipoModificacionCbx.getValue();
        Double nuevoValor = 0.0d;

        // 1. Lee el valor actual según lo que seleccionó
        if (tipo == 0) { // planilla normal
            EmpleadoCalculoSalarioView view = (EmpleadoCalculoSalarioView) mainUI.getNavigator().getCurrentView();
            switch (tipoCambio) {
                case "78-89":
                    nuevoValor = (Double) view.planillaDetalleContainer
                            .getContainerProperty(selectedItemId, EmpleadoCalculoSalarioView.BONO2)
                            .getValue();
                    nuevoValor += montoTxt.getDoubleValueDoNotThrow();
                    view.planillaDetalleContainer
                            .getContainerProperty(selectedItemId, EmpleadoCalculoSalarioView.BONO2)
                            .setValue(nuevoValor);
                    break;

                case "Extra":
                    nuevoValor = (Double) view.planillaDetalleContainer
                            .getContainerProperty(selectedItemId, EmpleadoCalculoSalarioView.BONO3)
                            .getValue();
                    nuevoValor += montoTxt.getDoubleValueDoNotThrow();
                    view.planillaDetalleContainer
                            .getContainerProperty(selectedItemId, EmpleadoCalculoSalarioView.BONO3)
                            .setValue(nuevoValor);
                    break;

                case "Descuento":
                    nuevoValor = (Double) view.planillaDetalleContainer
                            .getContainerProperty(selectedItemId, EmpleadoCalculoSalarioView.DESCUENTO2)
                            .getValue();
                    nuevoValor += montoTxt.getDoubleValueDoNotThrow();
                    view.planillaDetalleContainer
                            .getContainerProperty(selectedItemId, EmpleadoCalculoSalarioView.DESCUENTO2)
                            .setValue(nuevoValor);
                    break;
            }
        } else { // tipo == 1 → liquidación
            EmpleadoCalculoLiquidacionView view = (EmpleadoCalculoLiquidacionView) mainUI.getNavigator().getCurrentView();
            switch (tipoCambio) {
                case "78-89":
                    nuevoValor = (Double) view.planillaDetalleContainer
                            .getContainerProperty(selectedItemId, EmpleadoCalculoLiquidacionView.BONO2)
                            .getValue();
                    nuevoValor += montoTxt.getDoubleValueDoNotThrow();
                    view.planillaDetalleContainer
                            .getContainerProperty(selectedItemId, EmpleadoCalculoLiquidacionView.BONO2)
                            .setValue(nuevoValor);
                    break;

                case "Extra":
                    nuevoValor = (Double) view.planillaDetalleContainer
                            .getContainerProperty(selectedItemId, EmpleadoCalculoLiquidacionView.BONO3)
                            .getValue();
                    nuevoValor += montoTxt.getDoubleValueDoNotThrow();
                    view.planillaDetalleContainer
                            .getContainerProperty(selectedItemId, EmpleadoCalculoLiquidacionView.BONO3)
                            .setValue(nuevoValor);
                    break;

                case "Descuento":
                    nuevoValor = (Double) view.planillaDetalleContainer
                            .getContainerProperty(selectedItemId, EmpleadoCalculoLiquidacionView.DESCUENTO2)
                            .getValue();
                    nuevoValor += montoTxt.getDoubleValueDoNotThrow();
                    view.planillaDetalleContainer
                            .getContainerProperty(selectedItemId, EmpleadoCalculoLiquidacionView.DESCUENTO2)
                            .setValue(nuevoValor);
                    break;
            }
        }

        // 2. Armar el UPDATE SQL correcto
        try {
            queryString = "UPDATE planilla_detalle SET ";

            if (tipoCambio.equals("78-89")) {
                queryString += "Bonificacion2 = " + nuevoValor;
            } else if (tipoCambio.equals("Extra")) {
                queryString += "Bonificacion3 = " + nuevoValor;
            } else if (tipoCambio.equals("Descuento")) {
                // OJO: Tu código original NUNCA actualizaba descuento en DB.
                // Si la columna en la tabla se llama, por ejemplo, Descuento2, deberías usarla aquí.
                queryString += "Descuento2 = " + nuevoValor;
            }

            queryString += " WHERE IdPlanilla = " + idPlanillaDetalle;
            queryString += " AND IdEmpleado = " + idEmpleado;

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            Notification notif = new Notification("", Notification.Type.HUMANIZED_MESSAGE);
            notif.setCaption("Registro modificado con éxito!");
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.CHECK);
            notif.show(Page.getCurrent());

            close();

        } catch (SQLException ex) {
            Notification notif = new Notification(
                    "HA OCURRIDO UN ERROR DE BASE DE DATOS : " + ex.getMessage(),
                    Notification.Type.HUMANIZED_MESSAGE
            );
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.WARNING);
            notif.show(Page.getCurrent());
            System.out.println("Error en base datos al intentar hacer update de bono para planilla " + ex);
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
