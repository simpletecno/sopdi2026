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
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.ui.NumberField;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joseaguirre
 */
public class InspectionTaskForm extends Window {

    final long MILLSECS_PER_DAY = 24 * 60 * 60 * 1000; //Milisegundos al día         
    VerticalLayout mainLayout;
    MarginInfo marginInfo;

    FormLayout tareaForm;

    Button saveBtn;
    Button salirBtn;
    Button eliminarBtn;

    Statement stQuery = null;
    ResultSet rsRecords = null;

    TextField codigoTareaTxt;
    ComboBox rubroCbx;
    ComboBox responsableCbx;
    ComboBox ejecutorCbx;
    TextField descripcionTxt;
    ComboBox garantiaCbx;
    ComboBox esTareaCbx;
    ComboBox presupuestoCbx;
    ComboBox centroCostoCbx;
    ComboBox visibleParaClienteCbx;
    ComboBox autorizadorTipoCbx;
    NumberField diasHabilesN;
    ComboBox equipoDibujoCbx;

    ComboBox estatusCbx;
    TextArea instruccionTxt;

    UI mainUI;

    String idVisita;
    String idTarea;
    String codigoVisita;
    String idCentroCosto;

    boolean siEditarCentroCosto;

    public InspectionTaskForm(String idVisita, String idTarea,
            String codigoVisita, String idCentroCosto, boolean siEditarCentroCosto) {
        this.idVisita = idVisita;
        this.idTarea = idTarea;
        this.codigoVisita = codigoVisita;
        this.idCentroCosto = idCentroCosto;
        this.mainUI = UI.getCurrent();
        this.siEditarCentroCosto = siEditarCentroCosto;

        setWidth("50%");

        setCaption(((SopdiUI) mainUI).sessionInformation.getStrCompanyName() + " - Ficha de tarea por visita o reunión");
        Page.getCurrent().setTitle(((SopdiUI) mainUI).sessionInformation.getStrCompanyName() + " - Ficha de tarea por visita de reunión");

        marginInfo = new MarginInfo(true, true, true, true);

        mainLayout = new VerticalLayout();
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);

        tareaForm = new FormLayout();

        codigoTareaTxt = new TextField("Código Tarea : ");
        codigoTareaTxt.setWidth("8em");
        codigoTareaTxt.setReadOnly(true);
        codigoTareaTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);

        rubroCbx = createComboRubro();

        descripcionTxt = new TextField("Descripción : ");
        descripcionTxt.setWidth("35em");
        descripcionTxt.setHeight("3em");
        descripcionTxt.setMaxLength(128);
        descripcionTxt.setRequired(true);
        descripcionTxt.setRequiredError("POR FAVOR INGRESE LA DESCRIPCION DE LA TAREA");

        responsableCbx = createComboPersona("Responsable :");
        
        ejecutorCbx = createComboPersona("Ejecutor :");

        esTareaCbx = new ComboBox("Es Tarea : ");
        esTareaCbx.addItem("SI");
        esTareaCbx.addItem("NO");
        esTareaCbx.select("NO");
        esTareaCbx.setInvalidAllowed(false);
        esTareaCbx.setNewItemsAllowed(false);
        esTareaCbx.setNullSelectionAllowed(false);
        esTareaCbx.setWidth("5em");

        garantiaCbx = new ComboBox("Garantía : ");
        garantiaCbx.addItem("SI");
        garantiaCbx.addItem("NO");
        garantiaCbx.select("NO");
        garantiaCbx.setInvalidAllowed(false);
        garantiaCbx.setNewItemsAllowed(false);
        garantiaCbx.setNullSelectionAllowed(false);
        garantiaCbx.setWidth("5em");

        presupuestoCbx = new ComboBox("Presupuesto : ");
        presupuestoCbx.addItem("SI");
        presupuestoCbx.addItem("NO");
        presupuestoCbx.select("NO");
        presupuestoCbx.setInvalidAllowed(false);
        presupuestoCbx.setNewItemsAllowed(false);
        presupuestoCbx.setNullSelectionAllowed(false);
        presupuestoCbx.setWidth("5em");

        centroCostoCbx = new ComboBox("Centro costo :");
        centroCostoCbx.addContainerProperty("idCentroCosto", String.class, null);
        fillComboCentroCosto();
        centroCostoCbx.setEnabled(siEditarCentroCosto);

        autorizadorTipoCbx = new ComboBox("Autorizador : ");
        autorizadorTipoCbx.addItem("CLIENTE");
        autorizadorTipoCbx.addItem("GERENCIA");
        autorizadorTipoCbx.addItem("ADMINISTRADOR");
        autorizadorTipoCbx.addItem("COMITE TECNICO");
        autorizadorTipoCbx.addItem("JUNTA DIRECTIVA");
        autorizadorTipoCbx.addItem("NO APLICA");
        autorizadorTipoCbx.setInvalidAllowed(false);
        autorizadorTipoCbx.setNewItemsAllowed(false);
        autorizadorTipoCbx.setNullSelectionAllowed(false);
        autorizadorTipoCbx.select("CLIENTE");
        autorizadorTipoCbx.setWidth("20em");

        diasHabilesN = new NumberField("Días Hábiles que tomará : ");
        diasHabilesN.setRequired(true);
        diasHabilesN.setImmediate(true);
        diasHabilesN.setValue(0d);
        diasHabilesN.setRequiredError("POR FAVOR INGRESE El TIEMPO ESTIMADO");

        equipoDibujoCbx = new ComboBox("Equipo Dibujo : ");
        equipoDibujoCbx.addItem("SI");
        equipoDibujoCbx.addItem("NO");
        equipoDibujoCbx.select("NO");
        equipoDibujoCbx.setInvalidAllowed(false);
        equipoDibujoCbx.setNewItemsAllowed(false);
        equipoDibujoCbx.setNullSelectionAllowed(false);
        equipoDibujoCbx.setWidth("5em");

        visibleParaClienteCbx = new ComboBox("Visible para cliente : ");
        visibleParaClienteCbx.addItem("SI");
        visibleParaClienteCbx.addItem("NO");
        visibleParaClienteCbx.select("NO");
        visibleParaClienteCbx.setInvalidAllowed(false);
        visibleParaClienteCbx.setNewItemsAllowed(false);
        visibleParaClienteCbx.setNullSelectionAllowed(false);
        visibleParaClienteCbx.setWidth("5em");

        estatusCbx = new ComboBox("ESTATUS :");
        estatusCbx.setWidth("75%");
        estatusCbx.setInvalidAllowed(false);
        estatusCbx.setNewItemsAllowed(false);
        estatusCbx.setFilteringMode(FilteringMode.CONTAINS);
        estatusCbx.setInvalidAllowed(false);
        estatusCbx.setNewItemsAllowed(false);
        estatusCbx.setNullSelectionAllowed(false);
        estatusCbx.addItem("AUTORIZADA");
        estatusCbx.addItem("RECHAZADA");
        estatusCbx.setEnabled(false);

        instruccionTxt = new TextArea("Instrucción:");
        instruccionTxt.setWidth("35em");
        instruccionTxt.setHeight("12em");
        instruccionTxt.setMaxLength(1024);
        instruccionTxt.setDescription("Ingrese hasta 1024 caracteres...");

        tareaForm.addComponent(descripcionTxt);
        tareaForm.addComponent(rubroCbx);
        tareaForm.addComponent(responsableCbx);
        tareaForm.addComponent(ejecutorCbx);
        tareaForm.addComponent(esTareaCbx);
        tareaForm.addComponent(garantiaCbx);
        tareaForm.addComponent(presupuestoCbx);
        tareaForm.addComponent(centroCostoCbx);
        tareaForm.addComponent(autorizadorTipoCbx);
        tareaForm.addComponent(diasHabilesN);
        tareaForm.addComponent(equipoDibujoCbx);
        tareaForm.addComponent(visibleParaClienteCbx);
        tareaForm.addComponent(estatusCbx);
        tareaForm.addComponent(instruccionTxt);

        mainLayout.addComponent(tareaForm);

        saveBtn = new Button("Guardar");
        saveBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        saveBtn.setIcon(FontAwesome.SAVE);
        saveBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                saveTarea();
            }
        });

        eliminarBtn = new Button("Eliminar");
        eliminarBtn.addStyleName(ValoTheme.BUTTON_DANGER);
        eliminarBtn.setIcon(FontAwesome.TRASH);
        eliminarBtn.setVisible(!idTarea.trim().isEmpty());
        eliminarBtn.addClickListener(new Button.ClickListener() {
             @Override
             public void buttonClick(Button.ClickEvent event) { eliminarTarea(); }
         });

        salirBtn = new Button("Salir");
        salirBtn.setIcon(FontAwesome.ARROW_RIGHT);
        salirBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
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
        buttonsLayout.addComponent(saveBtn);
        buttonsLayout.setComponentAlignment(saveBtn, Alignment.BOTTOM_CENTER);
        buttonsLayout.addComponent(eliminarBtn);
        buttonsLayout.setComponentAlignment(eliminarBtn, Alignment.BOTTOM_CENTER);

        mainLayout.addComponent(buttonsLayout);
        mainLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);

        setContent(mainLayout);
    }

    private void fillComboCentroCosto() {

        if (centroCostoCbx == null) {
            return;
        }

        String queryString = "SELECT * ";
        queryString += " FROM centro_costo ";
        queryString += " WHERE IdProyecto = " + ((SopdiUI) mainUI).sessionInformation.getStrProjectId();
        queryString += " AND IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
        queryString += " AND Inhabilitado = 0";

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            centroCostoCbx.removeAllItems();
            centroCostoCbx.clear();
            centroCostoCbx.addItem("0");
            centroCostoCbx.setItemCaption("0", "No Aplica");
            // centroCostoCbx.select(0);

            while (rsRecords.next()) { //  encontrado
                centroCostoCbx.addItem(rsRecords.getString("CodigoCentroCosto"));
                centroCostoCbx.getContainerProperty(rsRecords.getString("CodigoCentroCosto"), "idCentroCosto").setValue(rsRecords.getString("IdCentroCosto"));
            }
        } catch (Exception ex1) {
            Notification.show("ERROR DEL SISTEMA AL BUSCAR CENTROS DE COSTO", Notification.Type.ERROR_MESSAGE);
            System.out.println("ERROR AL INTENTAR BUSCAR CATALOGO DE CENTROS DE COSTO : " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    private ComboBox createComboRubro() {

        ComboBox comboBox = new ComboBox("Rubro :");
        comboBox.setImmediate(true);
        comboBox.setNullSelectionAllowed(false);
        comboBox.setPageLength(7);
        comboBox.setNewItemsAllowed(false);
        comboBox.setWidth("20em");

        String queryString = "SELECT * ";
        queryString += " FROM visita_inspeccion_agenda ";
        queryString += " WHERE IdVisitaInspeccion = " + idVisita;

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado
                String select = rsRecords.getString("PuntoAgenda");
                do{
                    comboBox.addItem(rsRecords.getString("PuntoAgenda"));
                }while (rsRecords.next());
                comboBox.select(select);
            }
        } catch (Exception ex1) {
            Notification.show("ERROR DEL SISTEMA AL BUSCAR PUNTOS DE AGENDA", Notification.Type.ERROR_MESSAGE);
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "ERROR DEL SISTEMA AL BUSCAR PUNTOS DE AGENDA", ex1);
            ex1.printStackTrace();
        }

        return comboBox;
    }

    private ComboBox createComboPersona(String texto) {

        ComboBox comboBox = new ComboBox(texto);
        comboBox.setImmediate(true);
        comboBox.setNullSelectionAllowed(false);
        comboBox.setNewItemsAllowed(true);
        comboBox.setWidth("20em");
        comboBox.clear();

        comboBox.addItem("");

        String queryString = "SELECT * FROM proveedor ";
        queryString += " WHERE EsVisitaResponsable = 1";
        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) { //  encontrado                
                comboBox.addItem(rsRecords.getString("Nombre"));
            }

        } catch (Exception ex1) {
            Notification.show("ERROR DEL SISTEMA AL BUSCAR PARTICIPANTES DE REUNIONES", Notification.Type.ERROR_MESSAGE);
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "ERROR AL INTENTAR BUSCAR CATALOGO DE PROVEEDORES (PARTICIPANTES DE REUNIONES)", ex1);
            ex1.printStackTrace();
        }

        return comboBox;
    }

    public void fillData() {

        if (idTarea.trim().isEmpty()) {
            centroCostoCbx.select(idCentroCosto);
            descripcionTxt.focus();
            return;
        }

        String queryString = "";

        queryString = "SELECT * ";
        queryString += " FROM  visita_inspeccion_tarea ";
        queryString += " WHERE IdVisitaInspeccionTarea = " + idTarea;

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado

                rubroCbx.select(rsRecords.getString("Rubro"));
                descripcionTxt.setValue(rsRecords.getString("Descripcion"));
                instruccionTxt.setValue(rsRecords.getString("Instruccion"));
                responsableCbx.select(rsRecords.getString("Responsable"));
                ejecutorCbx.select(rsRecords.getString("Ejecutor"));
                esTareaCbx.select(rsRecords.getString("EsTarea"));
                garantiaCbx.select(rsRecords.getString("Garantia"));
                presupuestoCbx.select(rsRecords.getString("Presupuesto"));
                centroCostoCbx.select(rsRecords.getString("IdCentroCosto"));
                autorizadorTipoCbx.select(rsRecords.getString("AutorizadoTipo"));
                diasHabilesN.setValue((double) rsRecords.getInt("DiasHabiles"));
                equipoDibujoCbx.select(rsRecords.getString("EquipoDibujo"));
                visibleParaClienteCbx.select(rsRecords.getString("VisibleParaCliente"));
                estatusCbx.select(rsRecords.getString("Estatus"));

                queryString = "SELECT ArchivoNombre ";
                queryString += " FROM visita_inspeccion";
                queryString += " WHERE IdVisitaInspeccion = " + idVisita;

                rsRecords = stQuery.executeQuery (queryString);

                rsRecords.next();

                estatusCbx.setEnabled(!rsRecords.getString("ArchivoNombre").trim().isEmpty());

//                if(((SopdiUI)mainUI).sessionInformation.getStrUserProfileName().toUpperCase().equals("ADMINISTRADOR")) {
//                    autorizadorTipoCbx.setVisible(true);
//                }
            } else {
                Notification.show("Error, no se encotró registro de esta tarea!", Notification.Type.ERROR_MESSAGE);
                close();
            }
        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error al intentar leer registros de tareas", ex);
            System.out.println("Error al intentar leer registros de tareas : " + ex.getMessage());
            Notification.show("Error al intentar leer registros de tareas..!", Notification.Type.ERROR_MESSAGE);
        }
    }

    private void eliminarTarea(){
        String queryString;

        try {
            queryString = "DELETE FROM visita_inspeccion_tarea ";
            queryString += " WHERE IdVisitaInspeccionTarea  = " + idTarea;

            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "queryString ELIMINAR TAREA = " + queryString);

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            Notification.show("TAREA ELIMINADA EXITOSAMENTE!", Notification.Type.HUMANIZED_MESSAGE);

            if (mainUI.getNavigator().getCurrentView().getClass().getSimpleName().equals("VisitasView")) {
                ((VisitasView) (mainUI.getNavigator().getCurrentView())).fillInspectionTaskGrid();
            }

        } catch (Exception ex) {
            Notification.show("Error al eliminar tarea : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "ERROR AL ELIMINAR TAREA. ", ex);
        }

        close();
    }

    private void saveTarea() {

        if (descripcionTxt.getValue().trim().isEmpty()) {
            Notification.show("Error, falta la descripción!", Notification.Type.ERROR_MESSAGE);
            descripcionTxt.focus();
            return;
        }
        if (String.valueOf(rubroCbx.getValue()).trim().isEmpty()) {
            Notification.show("Error, falta el rubro!", Notification.Type.ERROR_MESSAGE);
            rubroCbx.focus();
            return;
        }

        if (rubroCbx.size() == 0) {
            Notification.show("Error, falta el rubro!", Notification.Type.ERROR_MESSAGE);
            rubroCbx.focus();
            return;
        }

        if (autorizadorTipoCbx.getValue().equals("NO APLICA")) {
            if (garantiaCbx.getValue().equals("NO") || presupuestoCbx.getValue().equals("NO")) {

            } else {
                Notification.show("Por favor seleccione un Autorizador!", Notification.Type.ERROR_MESSAGE);
                rubroCbx.focus();
                return;
            }
        }

        String codigoTarea, queryString;

        try {
            if (idTarea.trim().isEmpty()) {

                queryString = "SELECT CodigoTarea";
                queryString += " FROM  visita_inspeccion_tarea ";
                queryString += " WHERE IdVisitaInspeccion = " + idVisita;
                queryString += " ORDER BY CodigoTarea DESC";
                queryString += " LIMIT 1";

                stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                rsRecords = stQuery.executeQuery(queryString);

                //0123456789012345
                //XXYYZZDDMMAACCtt
                if (rsRecords.next()) { // encontrado la ultima tarea...
                    codigoTarea = codigoVisita.substring(0, 14) + String.format("%02d", Integer.valueOf(rsRecords.getString("CodigoTarea").substring(14, 16)) + 1);
                } else {
                    codigoTarea = codigoVisita.substring(0, 14) + "01";
                }

//                System.out.println("codigoTarea = " + codigoTarea);

                queryString = "INSERT INTO visita_inspeccion_tarea (IdVisitaInspeccion, CodigoTarea, Rubro, ";
                queryString += " Descripcion, Instruccion, Responsable, Ejecutor, ";
                queryString += " EsTarea, Garantia, Presupuesto, AutorizadoTipo, IdCentroCosto, EquipoDibujo, ";
                queryString += " VisibleParaCliente, Estatus, FechaUltimoEstatus, DiasHabiles) ";
                queryString += " VALUES (";
                queryString += "  " + idVisita;
                queryString += ",'" + codigoTarea + "'";
                queryString += ",'" + rubroCbx.getValue() + "'";
                queryString += ",'" + descripcionTxt.getValue().replaceAll("'", "''") + "'";
                queryString += ",'" + instruccionTxt.getValue().replaceAll("'", "''") + "'";
                queryString += ",'" + String.valueOf(responsableCbx.getValue()) + "'";
                queryString += ",'" + String.valueOf(ejecutorCbx.getValue()) + "'";
                queryString += ",'" + String.valueOf(esTareaCbx.getValue()) + "'";
                queryString += ",'" + String.valueOf(garantiaCbx.getValue()) + "'";
                queryString += ",'" + String.valueOf(presupuestoCbx.getValue()) + "'";
                queryString += ",'" + String.valueOf(autorizadorTipoCbx.getValue()) + "'";
                queryString += ",'" + String.valueOf(centroCostoCbx.getValue()) + "'";
                queryString += ",'" + String.valueOf(equipoDibujoCbx.getValue()) + "'";
                queryString += ",'" + String.valueOf(visibleParaClienteCbx.getValue()) + "'";
                queryString += ",'PENDIENTE'";
                queryString += ",'" + Utileria.getFechaYYYYMMDD_1(new java.util.Date()) + "'";
                queryString += "," + ((int) diasHabilesN.getDoubleValueDoNotThrow());
                queryString += ")";
            } else {
                queryString = "UPDATE visita_inspeccion_tarea SET ";
                queryString += " Rubro = '" + rubroCbx.getValue() + "'";
                queryString += ",Descripcion = '" + descripcionTxt.getValue().replaceAll("'", "''") + "'";
                queryString += ",Instruccion = '" + instruccionTxt.getValue().replaceAll("'", "''") + "'";
                queryString += ",Responsable = '" + String.valueOf(responsableCbx.getValue()) + "'";
                queryString += ",Ejecutor = '" + String.valueOf(ejecutorCbx.getValue()) + "'";
                queryString += ",EsTarea = '" + String.valueOf(esTareaCbx.getValue()) + "'";
                queryString += ",Garantia = '" + String.valueOf(garantiaCbx.getValue()) + "'";
                queryString += ",Presupuesto = '" + String.valueOf(presupuestoCbx.getValue()) + "'";
                queryString += ",AutorizadoTipo = '" + String.valueOf(autorizadorTipoCbx.getValue()) + "'";
                queryString += ",IdCentroCosto = '" + String.valueOf(centroCostoCbx.getValue()) + "'";
                queryString += ",EquipoDibujo = '" + String.valueOf(equipoDibujoCbx.getValue()) + "'";
                queryString += ",VisibleParaCliente = '" + String.valueOf(visibleParaClienteCbx.getValue()) + "'";
                queryString += ",Estatus = '" + String.valueOf(estatusCbx.getValue()) + "'";
                queryString += ",FechaUltimoEstatus = '" + Utileria.getFechaYYYYMMDD_1(new java.util.Date()) + "'";
                queryString += ",DiasHabiles = " + (int)diasHabilesN.getDoubleValueDoNotThrow();
                queryString += " WHERE IdVisitaInspeccionTarea  = " + idTarea;
            }

            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "queryString TAREA = " + queryString);

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            Notification.show("OPERACION EXITOSA!", Notification.Type.HUMANIZED_MESSAGE);

            if (mainUI.getNavigator().getCurrentView().getClass().getSimpleName().equals("VisitasView")) {
                ((VisitasView) (mainUI.getNavigator().getCurrentView())).fillInspectionTaskGrid();
            }

        } catch (Exception ex) {
            Notification.show("Error al actualizar tarea : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "ERROR AL REGISTRAR O MODIFICAR TAREA. ", ex);
        }

        close();
    }
}
