/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.recursoshumanos;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.SelectionEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.renderers.DateRenderer;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.ui.NumberField;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joseaguirre
 */
public class   EmpleadoAusenciasForm extends Window {
    // Grid Ausencias
    private static final String ID_PROPERTY = "Id";
    private static final String TIPO_PROPERTY = "AUSENCIA";
    private static final String FECHA_INICIO_PROPERTY = "Fecha Inicio";
    private static final String FECHA_FIN_PROPERTY = "Fecha Fin";
    private static final String DIAS_PROPERTY = "DIAS";

    // Tipo Ausencias
    private static final String ES_DESCUENTO_PROPERTY = "EsDescuento";
    private static final String ES_DESCUENTO_PLANILLA_PROPERTY = "EsDescuentoEnPlanilla";
    private static final String PRIORIDAD_PROPERTY = "EsDescuentoEnPlanilla";

    VerticalLayout mainLayout;
    MarginInfo marginInfo;

    public IndexedContainer ausenciaContaier = new IndexedContainer();
    public Grid ausenciaGrid;
    GridLayout totalDiasLayout = new GridLayout();

    HorizontalLayout horizontalLayout;

    PopupDateField mesanioInicioField;
    PopupDateField mesanioFinField;

    DateField inicioDt =  new DateField("Desde : ");
    DateField finDt =  new DateField("Hasta : ");
    NumberField diasTxt =  new NumberField("Dias : ");

    Map<String, Label> totalDiasMap = new HashMap<>();

    ComboBox tipoAusenciaCbx = new ComboBox("Tipo : ");
    CheckBox medioDiaInicioChbx = new CheckBox("Medio Dia");
    CheckBox medioDiaFinChbx = new CheckBox("Medio Dia");
    CheckBox sabadosChbx = new CheckBox("Sábado(s)");

    TextField boletaTxT =  new TextField("Boleta : ");

    Button nuevoBtn;
    Button saveBtn;
    Button deleteBtn;
    Button salirBtn;

    boolean esNuevo;

    Statement stQuery = null;
    ResultSet rsRecords = null;

    UI mainUI;
    String idEmpleado;
    String cargo;

    public EmpleadoAusenciasForm(String idEmpleado, String cargo) {
        this.idEmpleado = idEmpleado;
        this.cargo = cargo;
        this.mainUI = UI.getCurrent();

        setWidth("80%");
        setHeight("80%");

        setCaption("Ausencia DEL EMPLEADO: " + idEmpleado);

        marginInfo = new MarginInfo(true, true, true, true);

        mainLayout = new VerticalLayout();
        mainLayout.setMargin(new MarginInfo(true, true, false, true));
        mainLayout.setSpacing(true);
        mainLayout.setSizeUndefined();

        horizontalLayout = new HorizontalLayout();
        horizontalLayout.setSpacing(true);

        setContent(mainLayout);

        mainLayout.addComponent(totalDiasLayout);
        mainLayout.setComponentAlignment(totalDiasLayout, Alignment.TOP_CENTER);

        createGrid();

        mainLayout.addComponent(horizontalLayout);
        mainLayout.setComponentAlignment(horizontalLayout, Alignment.BOTTOM_CENTER);

        creatRangoFechas();

        createForm();

        getTipoAusencia();

        creatDiasTotales();

        fillData();
    }

    private void createGrid() {
        ausenciaContaier.addContainerProperty(ID_PROPERTY, String.class, "0");
        ausenciaContaier.addContainerProperty(TIPO_PROPERTY, String.class, "");
        ausenciaContaier.addContainerProperty(FECHA_INICIO_PROPERTY, Date.class, "");
        ausenciaContaier.addContainerProperty(FECHA_FIN_PROPERTY, Date.class, "");
        ausenciaContaier.addContainerProperty(DIAS_PROPERTY, String.class, "0");

        ausenciaGrid = new Grid(ausenciaContaier);
        ausenciaGrid.setWidth("100%");
        ausenciaGrid.setImmediate(true);
        ausenciaGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        ausenciaGrid.setDescription("Seleccione un registro.");
        ausenciaGrid.setHeightMode(HeightMode.ROW);
        ausenciaGrid.setHeightByRows(6);
        ausenciaGrid.setResponsive(true);
        ausenciaGrid.getColumn(ID_PROPERTY).setExpandRatio(1);
        ausenciaGrid.getColumn(TIPO_PROPERTY).setExpandRatio(1);
        ausenciaGrid.getColumn(FECHA_INICIO_PROPERTY).setExpandRatio(2);
        ausenciaGrid.getColumn(FECHA_INICIO_PROPERTY).setRenderer(new DateRenderer(new SimpleDateFormat("dd/MM/yyyy")));
        ausenciaGrid.getColumn(FECHA_FIN_PROPERTY).setExpandRatio(2);
        ausenciaGrid.getColumn(FECHA_FIN_PROPERTY).setRenderer(new DateRenderer(new SimpleDateFormat("dd/MM/yyyy")));
        ausenciaGrid.getColumn(DIAS_PROPERTY).setExpandRatio(1);
        ausenciaGrid.getColumn(ID_PROPERTY).setHidden(true);
        ausenciaGrid.addSelectionListener(new SelectionEvent.SelectionListener() {
            @Override
            public void select(SelectionEvent event) {
                fillForm(String.valueOf(ausenciaContaier.getContainerProperty(ausenciaGrid.getSelectedRow(), ID_PROPERTY).getValue()));
                esNuevo = false;
                deleteBtn.setVisible(true);
                saveBtn.setVisible(true);
            }
        });

        Grid.HeaderRow filterRow = ausenciaGrid.appendHeaderRow();
        Utileria.addComboFilter(filterRow, TIPO_PROPERTY, ausenciaContaier);

        mainLayout.addComponent(ausenciaGrid);
        mainLayout.setComponentAlignment(ausenciaGrid, Alignment.TOP_CENTER);

    }

    private void creatRangoFechas(){
        mesanioInicioField = new PopupDateField("Mes Inicio : ");
        mesanioInicioField.setDateFormat("MM/yyyy");
        mesanioInicioField.setResolution(Resolution.MONTH);
        mesanioInicioField.setValue(new Date());

        mesanioFinField = new PopupDateField("Mes Fin : ");
        mesanioFinField.setDateFormat("MM/yyyy");
        mesanioFinField.setResolution(Resolution.MONTH);
        mesanioFinField.setValue(new Date());

        FormLayout mesanioLayout = new FormLayout(mesanioInicioField, mesanioFinField);
        mesanioLayout.setMargin(true);
        mesanioLayout.setComponentAlignment(mesanioInicioField, Alignment.TOP_RIGHT);
        mesanioLayout.setComponentAlignment(mesanioFinField, Alignment.TOP_RIGHT);

        Label divider = new Label();
        divider.setWidth("1px");
        divider.setHeight("100%");
        divider.addStyleName("vertical-divider"); // Estilo que definiremos en CSS

        horizontalLayout.addComponents(mesanioLayout, divider);
        horizontalLayout.setComponentAlignment(mesanioLayout, Alignment.TOP_LEFT);


    }

    private void creatDiasTotales(){
        Label totalDias = new Label("| TOTAL = 0 días |");

        int columnas = 6;

        // Filas = (tipos ausencias) / columnas
        totalDiasLayout.setRows((int) Math.ceil((double) totalDiasMap.size() /columnas));
        totalDiasLayout.setColumns(columnas);
        totalDiasLayout.setSpacing(true);

        totalDiasLayout.addComponent(totalDias, 0,0);
        totalDiasLayout.setComponentAlignment(totalDias, Alignment.TOP_LEFT);

        int column = 1;
        int row = 0;
        for(String tipo :totalDiasMap.keySet()) {
            if((column % columnas) == 0) row++;
            totalDiasLayout.addComponent(totalDiasMap.get(tipo), (column % columnas), row);
            totalDiasLayout.setComponentAlignment(totalDiasMap.get(tipo), Alignment.TOP_LEFT);
            column++;
        }

        totalDiasMap.put("Total", totalDias);
    }

    private void createForm() {

        mainLayout.setWidth("100%");

        tipoAusenciaCbx = new ComboBox("Tipo : ");
        tipoAusenciaCbx.addContainerProperty(ES_DESCUENTO_PROPERTY, Integer.class, 0);
        tipoAusenciaCbx.addContainerProperty(ES_DESCUENTO_PLANILLA_PROPERTY, Integer.class, 0);
        tipoAusenciaCbx.addContainerProperty(PRIORIDAD_PROPERTY, Integer.class, 0);
        tipoAusenciaCbx.setWidth("100%");

        inicioDt = new DateField("Desde : ");
        inicioDt.setDateFormat("dd/MM/yyyy");
        inicioDt.setWidth("100%");
        inicioDt.setValue(new java.util.Date());
        inicioDt.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                diasTxt.setReadOnly(false);
                diasTxt.setValue((double)dias());
                diasTxt.setReadOnly(true);
            }
        });

        finDt = new DateField("Hasta : ");
        finDt.setDateFormat("dd/MM/yyyy");
        finDt.setWidth("100%");
        finDt.setValue(new java.util.Date());
        finDt.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                diasTxt.setReadOnly(false);
                diasTxt.setValue((double)dias());
                diasTxt.setReadOnly(true);
            }
        });

        boletaTxT = new TextField("Boleta : ");
        boletaTxT.setWidth("100%");

        medioDiaInicioChbx = new CheckBox("1/2 Dia (Mañana)");
        medioDiaInicioChbx.setWidth("100%");
        medioDiaInicioChbx.addValueChangeListener(valueChangeEvent -> {
            diasTxt.setReadOnly(false);
            diasTxt.setValue(dias());
            diasTxt.setReadOnly(true);
        });

        medioDiaFinChbx = new CheckBox("1/2 Dia (Tarde)");
        medioDiaFinChbx.setWidth("100%");
        medioDiaFinChbx.addValueChangeListener(valueChangeEvent -> {
            diasTxt.setReadOnly(false);
            diasTxt.setValue(dias());
            diasTxt.setReadOnly(true);
        });

        sabadosChbx = new CheckBox("Sábado(s)");
        sabadosChbx.setWidth("100%");
        sabadosChbx.addValueChangeListener(valueChangeEvent -> {
            diasTxt.setReadOnly(false);
            diasTxt.setValue(dias());
            diasTxt.setReadOnly(true);
        });

        diasTxt = new NumberField("Dias : ");
        diasTxt.setInputPrompt("Dias de Ausencias");
        diasTxt.setDescription("Dias de Ausencias");
        diasTxt.setDecimalAllowed(true);
        diasTxt.setDecimalPrecision(2);
        diasTxt.setMinimumFractionDigits(2);
        diasTxt.setDecimalSeparator('.');
        diasTxt.setDecimalSeparatorAlwaysShown(true);
        diasTxt.setValue(1d);
        diasTxt.setGroupingUsed(true);
        diasTxt.setGroupingSeparator(',');
        diasTxt.setGroupingSize(3);
        diasTxt.setImmediate(true);
        diasTxt.addStyleName(ValoTheme.TEXTFIELD_HUGE);
        diasTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);
        diasTxt.setWidth("100%");
        diasTxt.setHeight("100%");
        diasTxt.setReadOnly(true);

        nuevoBtn = new Button("Nuevo");
        nuevoBtn.setIcon(FontAwesome.PLUS);
        nuevoBtn.setDescription("Nuevo");
        nuevoBtn.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        nuevoBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
//                if (!((SopdiUI) mainUI).sessionInformation.getStrUserProfile().equals("ADMINISTRADOR")) {
//                    Notification.show("Usuario no tiene permiso para esta operación.", Notification.Type.WARNING_MESSAGE);
//                    return;
//                }
                inicioDt.setValue(new java.util.Date());
                finDt.setValue(new java.util.Date());
                diasTxt.setValue(1d);
                esNuevo = true;
                deleteBtn.setVisible(false);
                saveBtn.setVisible(true);
                inicioDt.focus();
            }
        });

        saveBtn = new Button("Guardar");
        saveBtn.setIcon(FontAwesome.SAVE);
        saveBtn.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        saveBtn.addClickListener(
                (Button.ClickListener)
                        event -> {
                            if(!esNuevo) {
                                if (ausenciaGrid.getSelectedRow() != null) {
                                    saveData(String.valueOf(ausenciaContaier.getContainerProperty(ausenciaGrid.getSelectedRow(), ID_PROPERTY).getValue()));
                                }
                                else {
                                    Notification.show("NO HA SELECCIONADO UN REGISTRO.", Notification.Type.ERROR_MESSAGE);
                                    return;
                                }
                            }
                            else {
                                saveData("");
                            }
                            saveBtn.setVisible(false);
                        }
        );
        saveBtn.setVisible(false);

        deleteBtn = new Button("Eliminar");
        deleteBtn.setIcon(FontAwesome.REMOVE);
        deleteBtn.setDescription("Eliminar");
        deleteBtn.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        deleteBtn.addStyleName(ValoTheme.BUTTON_DANGER);
        deleteBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
//                if (!((SopdiUI) mainUI).sessionInformation.getStrUserProfile().equals("ADMINISTRADOR")) {
//                    Notification.show("Usuario no tiene permiso para esta operación.", Notification.Type.WARNING_MESSAGE);
//                    return;
//                }
                if (ausenciaContaier.size() > 0) {
                    if (ausenciaGrid.getSelectedRow() != null) {

                        ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de eliminar el registro Ausencias del empleado?",
                                "SI", "NO", new ConfirmDialog.Listener() {

                                    public void onClose(ConfirmDialog dialog) {
                                        if (dialog.isConfirmed()) {
                                            deleteAusencias(String.valueOf(ausenciaContaier.getContainerProperty(ausenciaGrid.getSelectedRow(), ID_PROPERTY).getValue()));
                                        }
                                    }
                                });
                        saveBtn.setVisible(false);
                    } else {
                        if (ausenciaGrid.getSelectedRow() == null) {
                            Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
                        }
                    }
                }
            }
        });
        deleteBtn.setVisible(false);

        salirBtn = new Button("Salir");
        salirBtn.setIcon(FontAwesome.ARROW_RIGHT);
        salirBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        salirBtn.addClickListener((Button.ClickListener) event -> close());

        HorizontalLayout layout1 = new HorizontalLayout(medioDiaInicioChbx, medioDiaFinChbx);
        layout1.setComponentAlignment(medioDiaInicioChbx, Alignment.BOTTOM_CENTER);
        layout1.setComponentAlignment(medioDiaFinChbx, Alignment.BOTTOM_CENTER);
        layout1.setSpacing(true);
        layout1.setWidth("100%");


        GridLayout layoutGrid = new GridLayout(17,2);

        layoutGrid.addComponent(diasTxt, 0,0, 2, 1);

        layoutGrid.addComponent(tipoAusenciaCbx, 3,0, 7,0);
        layoutGrid.addComponent(boletaTxT, 8,0, 12,0);

        layoutGrid.addComponent(inicioDt, 3,1, 7,1);
        layoutGrid.addComponent(finDt, 8,1, 12,1);

        VerticalLayout layoutCheckbox = new VerticalLayout(medioDiaInicioChbx, medioDiaFinChbx, sabadosChbx);
        layoutCheckbox.setHeight("80%");
        layoutCheckbox.setComponentAlignment(medioDiaInicioChbx, Alignment.TOP_LEFT);
        layoutCheckbox.setComponentAlignment(medioDiaFinChbx, Alignment.MIDDLE_LEFT);
        layoutCheckbox.setComponentAlignment(sabadosChbx, Alignment.BOTTOM_LEFT);

        layoutGrid.addComponent(layoutCheckbox, 13,0, 16,1);
        layoutGrid.setComponentAlignment(layoutCheckbox, Alignment.BOTTOM_CENTER);

        layoutGrid.setSpacing(true);
        layoutGrid.setWidth("80%");

        horizontalLayout.addComponent(layoutGrid);
        horizontalLayout.setComponentAlignment(layoutGrid, Alignment.MIDDLE_CENTER);

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setMargin(true);

        buttonsLayout.addComponent(nuevoBtn);
        buttonsLayout.addComponent(saveBtn);
        buttonsLayout.addComponent(deleteBtn);
        buttonsLayout.addComponent(salirBtn);
        buttonsLayout.setComponentAlignment(salirBtn, Alignment.BOTTOM_RIGHT);

    }

    private double dias() {

        double dias = 0;

        java.util.GregorianCalendar dateBefore;
        dateBefore = new java.util.GregorianCalendar();

        java.util.GregorianCalendar dateAfter;
        dateAfter = new java.util.GregorianCalendar();

        try {
            dateBefore.setTime(inicioDt.getValue());

            dateAfter.setTime(finDt.getValue());

            long dateBeforeInMs = dateBefore.getTimeInMillis();
            long dateAfterInMs = dateAfter.getTimeInMillis();

            long timeDiff = Math.abs(dateAfterInMs - dateBeforeInMs);

            dias = 1 + TimeUnit.DAYS.convert(timeDiff, TimeUnit.MILLISECONDS);


            if(sabadosChbx.getValue()){
                // (DiasAusencia + DiaSemana[0, 6]) / 7 sin aproximacion
                double  sabados = ((dias + (dateBefore.get(Calendar.DAY_OF_WEEK))) - 1) / 7;
                dias += ((int) sabados) * 0.5;
            }

            if(medioDiaInicioChbx.getValue()) dias -= 0.5d;

            if(medioDiaFinChbx.getValue()) dias -= 0.5d;

        } catch(Exception e){
            e.printStackTrace();
            Notification.show("ERROR : al calcular los dias : " + e.getMessage(), Notification.Type.ERROR_MESSAGE);
        }

        return dias;
    }

    // Se encarga de agregar la informacion tanto a TipoAusencia como a la lista de totales.
    private void getTipoAusencia(){
        String queryString = "";

        queryString = "Select * ";
        queryString += "From razon_ausencia ";

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);
            String tipoAusencia;
            if(rsRecords.next()){
                do {
                    tipoAusencia =  rsRecords.getString("Razon");
                    tipoAusenciaCbx.addItem(tipoAusencia);
                    totalDiasMap.put(tipoAusencia, new Label( "| " + tipoAusencia + " = 0 días |"));
                    tipoAusenciaCbx.getContainerProperty(tipoAusencia, ES_DESCUENTO_PROPERTY).setValue(rsRecords.getInt("EsDescuento"));
                    tipoAusenciaCbx.getContainerProperty(tipoAusencia, ES_DESCUENTO_PLANILLA_PROPERTY).setValue(rsRecords.getInt("EsDescuentoEnPlanilla"));
                    tipoAusenciaCbx.getContainerProperty(tipoAusencia, PRIORIDAD_PROPERTY).setValue(rsRecords.getInt("Prioridad"));
                }while (rsRecords.next());
            }

        } catch (Exception ex) {
            Logger.getLogger(EmpleadoAusenciasForm.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de Ausencias de empleado : " + ex.getMessage());
            Notification.show("Error al intentar leer registros de Ausencias de empleado..!", Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public void fillData() {

        ausenciaContaier.removeAllItems();

        String queryString = "";

        queryString = "Select * ";
        queryString += "From empleado_ausencia ";
        queryString += "Where IdEmpleado = " + idEmpleado + " ";
        queryString += "AND FechaInicio <= '" + Utileria.getFechaYYYYMMDD_1(Utileria.getInicioMesDate(mesanioInicioField.getValue())) + "' ";
        queryString += "AND FechaFin >= '" + Utileria.getFechaYYYYMMDD_1(Utileria.getInicioMesDate(mesanioFinField.getValue())) + "' ";
        queryString += "Order By FechaInicio, FechaFin";

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            Object itemId;
            int totalDias = 0;
            while (rsRecords.next()) {

                itemId = ausenciaContaier.addItem();
//System.out.println("CuentaContalbe=" + rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));
                ausenciaContaier.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords.getString("Id"));
                ausenciaContaier.getContainerProperty(itemId, TIPO_PROPERTY).setValue(rsRecords.getDate("FechaInicio"));
                ausenciaContaier.getContainerProperty(itemId, FECHA_INICIO_PROPERTY).setValue(rsRecords.getDate("FechaInicio"));
                ausenciaContaier.getContainerProperty(itemId, FECHA_FIN_PROPERTY).setValue(rsRecords.getString("FechaFin"));
                ausenciaContaier.getContainerProperty(itemId, DIAS_PROPERTY).setValue(rsRecords.getString("Dias"));

                totalDias+= rsRecords.getInt("Dias");

            }

        } catch (Exception ex) {
            Logger.getLogger(EmpleadoAusenciasForm.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de Ausencias de empleado : " + ex.getMessage());
            Notification.show("Error al intentar leer registros de Ausencias de empleado..!", Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    public void fillForm(String id) {

        inicioDt.setValue(new java.util.Date());
        diasTxt.setValue(0.00);

        String queryString = "";

        queryString = "Select * ";
        queryString += "From empleado_ausencia ";
        queryString += "Where Id = " + id;
        System.out.println("queryEmpleado=" + queryString);
        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {

                inicioDt.setValue(rsRecords.getDate("FechaInicio"));
                finDt.setValue(rsRecords.getDate("FechaFin"));
                diasTxt.setValue(rsRecords.getDouble("Dias"));
            }

        } catch (Exception ex) {
            Logger.getLogger(EmpleadoAusenciasForm.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de Ausencias de empleado : " + ex.getMessage());
            Notification.show("Error al intentar leer registros de Ausencias de empleado..!", Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }


    private void saveData(String id) {
        if(saveEmpleadoAusencia(id)){
            if(saveEmpleadoAsistencia()){
                Notification notif = new Notification("REGISTRO AGREGADO o MODIFICADO EXITOSAMENTE.", Notification.Type.HUMANIZED_MESSAGE);
                notif.setDelayMsec(1500);
                notif.setPosition(Position.MIDDLE_CENTER);
                notif.setIcon(FontAwesome.CHECK);
                notif.show(Page.getCurrent());
            }
        }


    }

    private boolean saveEmpleadoAusencia(String id){
        if (diasTxt.getValue() == null) {
            Notification.show("Por favor asegúrese de que si tenga dias de ausencia.", Notification.Type.WARNING_MESSAGE);
            diasTxt.focus();
            return false;
        }

        String queryString;

        try {
            if(esNuevo) {
                queryString = "SELECT (Boleta = '" + boletaTxT.getValue() + "') AS Boleta";
                queryString += "FROM empleado_ausencia ";
                queryString += "WHERE IdEmpleado = " + idEmpleado + " ";
                queryString += "AND Tipo = '" + tipoAusenciaCbx.getValue() + "' ";
                queryString += "OR (FechaInicio >= '" + Utileria.getFechaYYYYMMDD_1(inicioDt.getValue()) + "' ";
                queryString += "OR FechaFin <= '" + Utileria.getFechaYYYYMMDD_1(finDt.getValue()) + "') ";
                queryString += "OR Boleta = '" + boletaTxT.getValue() + "' ";
                //System.out.println("queryEmpleadoAusencias=" + queryString);
                stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                rsRecords = stQuery.executeQuery(queryString);

                if(rsRecords.next()) {
                    String mensaje = "NUEVO : YA EXISTE UN REGISTRO CON ESTOS DATOS.";
                    if(rsRecords.getBoolean("Boleta") && !boletaTxT.getValue().isEmpty()) {
                        mensaje += " CODIGO BOLETA REPETIDO";
                    }
                    Notification.show(mensaje, Notification.Type.ERROR_MESSAGE);
                    return false;
                }
                queryString = "Insert into empleado_ausencia ";
                queryString += "(Tipo, IdEmpleado, FechaInicio, FechaFin, Dias, CreadoUsuario, ";
                queryString += "MedioDiaMañana, MedioDiaTarde, Sabados, Boleta) ";
                queryString += "Values ";
                queryString += "(";
                queryString += "'" + tipoAusenciaCbx.getValue() + "'";
                queryString += "," + idEmpleado;
                queryString += ",'" + Utileria.getFechaYYYYMMDD_1(inicioDt.getValue()) + "'";
                queryString += ",'" + Utileria.getFechaYYYYMMDD_1(finDt.getValue()) + "'";
                queryString += ","  + diasTxt.getDoubleValueDoNotThrow();
                queryString += ","  + ((SopdiUI)mainUI).sessionInformation.getStrUserId();
                queryString += ","  + medioDiaInicioChbx.getValue();
                queryString += ","  + medioDiaFinChbx.getValue();
                queryString += ","  + sabadosChbx.getValue();
                queryString += ",'" + boletaTxT.getValue() + "'";
                queryString += ")";

            } else {

                queryString = "UPDATE empleado_ausencia SET ";
                queryString += "FechaInicio = '" + Utileria.getFechaYYYYMMDD_1(inicioDt.getValue() )+ "' ";
                queryString += ",FechaFin = '" + Utileria.getFechaYYYYMMDD_1(finDt.getValue() )+ "' ";
                queryString += ",Dias = " + diasTxt.getDoubleValueDoNotThrow() + " ";
                queryString += ",MedioDiaMañana = " + diasTxt.getDoubleValueDoNotThrow() + " ";
                queryString += ",MedioDiaTarde = " + diasTxt.getDoubleValueDoNotThrow() + " ";
                queryString += ",Sabados = " + diasTxt.getDoubleValueDoNotThrow() + " ";
                queryString += ",Boleta = '" + boletaTxT.getValue() + "' ";
                queryString += " Where Id = " + id;

            }

            System.out.println("saveData="+queryString);

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

        } catch (SQLException ex) {
            Notification notif = new Notification("HA OCURRIDO UN ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.WARNING);
            notif.show(Page.getCurrent());
            System.out.println("Error en base datos al intentar hacer update o insert en tabla empleado_Ausencias " + ex);
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    private boolean saveEmpleadoAsistencia(){
        String queryString;
        Date fechaInicio = inicioDt.getValue();
        Date fechaFin = finDt.getValue();

        if(esNuevo) {
            if(((Date)ausenciaContaier.getContainerProperty(ausenciaGrid.getSelectedRow(), FECHA_INICIO_PROPERTY).getValue()).before(inicioDt.getValue())){
                fechaInicio = (Date)ausenciaContaier.getContainerProperty(ausenciaGrid.getSelectedRow(), FECHA_INICIO_PROPERTY).getValue();
            }
            if(((Date)ausenciaContaier.getContainerProperty(ausenciaGrid.getSelectedRow(), FECHA_FIN_PROPERTY).getValue()).before(finDt.getValue())){
                fechaFin = (Date)ausenciaContaier.getContainerProperty(ausenciaGrid.getSelectedRow(), FECHA_FIN_PROPERTY).getValue();
            }
        }

        try {

            queryString = "DELETE ea.* ";
            queryString += "FROM empleado_asistencia ea ";
            queryString += "LEFT JOIN razon_ausencia ra ON ea.Razon = ra.Razon";
            queryString += "WHERE Fecha BETWEEN '" + Utileria.getFechaYYYYMMDD_1(fechaInicio) + "' ";
            queryString += "AND '" + Utileria.getFechaYYYYMMDD_1(fechaFin) + "' ";
            queryString += "AND ra.Prioridad < " + tipoAusenciaCbx.getContainerProperty(tipoAusenciaCbx.getValue(), PRIORIDAD_PROPERTY).getValue();

            System.out.println("saveData="+queryString);

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

        } catch (SQLException ex) {
            Notification notif = new Notification("HA OCURRIDO UN ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.WARNING);
            notif.show(Page.getCurrent());
            System.out.println("Error en base datos al intentar hacer update o insert en tabla empleado_Ausencias " + ex);
            ex.printStackTrace();
            return false;
        }

        try {

            queryString = "SELECT ea.* ";
            queryString += "FROM empleado_asistencia ea ";
            queryString += "LEFT JOIN razon_ausencia ra ON ea.Razon = ra.Razon";
            queryString += "WHERE Fecha BETWEEN '" + Utileria.getFechaYYYYMMDD_1(fechaInicio) + "' ";
            queryString += "AND '" + Utileria.getFechaYYYYMMDD_1(fechaFin) + "' ";
            queryString += "AND ra.Prioridad < " + tipoAusenciaCbx.getContainerProperty(tipoAusenciaCbx.getValue(), PRIORIDAD_PROPERTY).getValue();

            System.out.println("saveData="+queryString);

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

        } catch (SQLException ex) {
            Notification notif = new Notification("HA OCURRIDO UN ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.WARNING);
            notif.show(Page.getCurrent());
            System.out.println("Error en base datos al intentar hacer update o insert en tabla empleado_Ausencias " + ex);
            ex.printStackTrace();
            return false;
        }

        try {

            Date fechaTemp = fechaInicio;

            queryString = "INSERT INTO empleado_asistencia ";
            queryString += "(IdEmpleado, Cargo, Fecha, HorasExtra, HorasExtraDoble, Estatus, Razon, DiasVacaciones, ";
            queryString += "EsDefinitiva, EsDescuento, EsAusenteSinGoceDeSueldo, CreadoFechaYHora, CreadoIdUsuario) ";
            queryString += "VALUES ";

            do {
                double tiempodia = 1;
                if(fechaTemp.equals(inicioDt.getValue())) tiempodia -= medioDiaInicioChbx.getValue()?0.5:0;
                if(fechaTemp.equals(finDt.getValue())) tiempodia -= medioDiaFinChbx.getValue()?0.5:0;

                if (fechaTemp.equals(inicioDt.getValue()) ||                                            // Si es el inicio
                   (fechaTemp.before(inicioDt.getValue()) && fechaTemp.before(finDt.getValue())) ||     // Si esta en medio
                    fechaTemp.equals(finDt.getValue())){                                                // Si esta al final
                    queryString += "(" + idEmpleado + ",";                                                  // IdEmpleado
                    queryString += "'" + cargo + "',";                                                      // Cargo
                    queryString += "'" + Utileria.getFechaYYYYMMDD_1(fechaTemp) + "',";                     // Fecha
                    queryString += 0 + ",";                                                                 // HorasExtra
                    queryString += 0 + ",";                                                                 // HorasExtraDoble
                    queryString += "'AUSENTE',";                                                            // Estatus
                    queryString += "'" + tipoAusenciaCbx.getValue() + "',";                                 // Razon
                    queryString += tipoAusenciaCbx.getValue().equals("Vacaciones")?tiempodia:0 + ",";       // DiasVacaciones
                    queryString += 0 + ",";                                                                 // EsDefinitiva
                    queryString += tipoAusenciaCbx.getContainerProperty(tipoAusenciaCbx.getValue(), ES_DESCUENTO_PROPERTY).getValue() + ","; // EsDescuento
                    queryString += tipoAusenciaCbx.getContainerProperty(tipoAusenciaCbx.getValue(), ES_DESCUENTO_PLANILLA_PROPERTY).getValue() + ","; // EsAusenteSinGoceDeSueldo
                    queryString += "current_timestamp,";                                                    // CreadoFechaYHora
                    queryString += ((SopdiUI)mainUI).sessionInformation.getStrUserId();                     // CreadoIdUsuario
                }else{
                    queryString += "(" + idEmpleado + ",";                                                  // IdEmpleado
                    queryString += "'" + cargo + "',";                                                      // Cargo
                    queryString += "'" + Utileria.getFechaYYYYMMDD_1(fechaTemp) + "',";                     // Fecha
                    queryString += 0 + ",";                                                                 // HorasExtra
                    queryString += 0 + ",";                                                                 // HorasExtraDoble
                    queryString += "'PRESENTE',";                                                           // Estatus
                    queryString += "'',";                                                                   // Razon
                    queryString += 0 + ",";                                                                 // DiasVacaciones
                    queryString += 0 + ",";                                                                 // EsDefinitiva
                    queryString += 0 + ",";                                                                 // EsDescuento
                    queryString += 0 + ",";                                                                 // EsAusenteSinGoceDeSueldo
                    queryString += "current_timestamp,";                                                    // CreadoFechaYHora
                    queryString += ((SopdiUI)mainUI).sessionInformation.getStrUserId();                     // CreadoIdUsuario
                }

                queryString += "),";

                fechaTemp = Utileria.getTomorrow(fechaTemp);
            }while (!fechaTemp.after(fechaFin));

            queryString = queryString.substring(0, queryString.length() - 1);

            System.out.println("saveData="+queryString);

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

        } catch (SQLException ex) {
            Notification notif = new Notification("HA OCURRIDO UN ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.WARNING);
            notif.show(Page.getCurrent());
            System.out.println("Error en base datos al intentar hacer update o insert en tabla empleado_Ausencias " + ex);
            ex.printStackTrace();
            return false;
        }

        fillData();
        deleteBtn.setVisible(false);
        return true;
    }

    public void deleteAusencias(String id) {

        inicioDt.setValue(new java.util.Date());
        diasTxt.setValue(0.00);

        String queryString = "";

        queryString = "Delete ";
        queryString += " From empleado_ausencia ";
        queryString += " Where Id = " + id;
//System.out.println("queryEmpleado=" + queryString);
        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            Notification.show("Registro ha sido eliminado!", Notification.Type.WARNING_MESSAGE);
            fillData();

        } catch (Exception ex) {
            Logger.getLogger(EmpleadoAusenciasForm.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar eliminar registros de Ausencias de empleado : " + ex.getMessage());
            Notification.show("Error al intentar eliminar registros de Ausencias de empleado..!", Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}