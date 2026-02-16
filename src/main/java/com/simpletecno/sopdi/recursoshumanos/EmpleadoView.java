/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.recursoshumanos;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.simpletecno.sopdi.configuracion.ProveedorForm;
import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.SelectionEvent;
import com.vaadin.event.SelectionEvent.SelectionListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.Grid.HeaderCell;
import com.vaadin.ui.Grid.HeaderRow;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.ui.NumberField;

import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JAguirre
 */
@SuppressWarnings("serial")
public class EmpleadoView extends VerticalLayout implements View {

    public Statement stQuery = null;
    public ResultSet rsRecords = null;

    static final String ID_PROPERTY = "Id";
    static final String CUENTA_CONTABLE_PROPERTY = "Cuenta Contable";
    static final String DESDE_PROPERTY = "A partir del";
    static final String NO_PROPERTY = "NO";
    static final String MONTO_PROPERTY = "Monto";

    HorizontalLayout mainLayout = new HorizontalLayout();
    VerticalLayout leftLayout = new VerticalLayout();
    VerticalLayout rightLayout = new VerticalLayout();

    public IndexedContainer empleadosContainer = new IndexedContainer();
    public Grid empleadosGrid;

    MarginInfo marginInfo;

    Button refreshBtn;
    Button nuevoBtn;
    Button salarioBtn;
    Button vacacionesBtn;
    Button deleteBtn;

    ComboBox generoCbx = new ComboBox("Género : ");
    ComboBox cargoCbx = new ComboBox("Cargo/Plaza : ");
    TextField idEmpleadoTxt =  new TextField("Id Empleado : ");
    TextField primerNombreTxt =  new TextField("Primer Nombre : ");
    TextField segundoNombreTxt =  new TextField("Segundo Nombre : ");
    TextField primerApellidoTxt =  new TextField("Primer Apellido : ");
    TextField segundoApellidoTxt =  new TextField("Segundo Apellido : ");
    TextField apellidoCasadaTxt =  new TextField("Apellido de Casada : ");
    TextField nombreCompletoTxt =  new TextField("Nombre Completo (para IGSS) : ");
    TextArea direccionTxt =  new TextArea("Dirección : ");
    TextField nacionalidadTxt =  new TextField("Nacionalidad : ");
    TextField telefonoTxt =  new TextField("Teléfono : ");
    TextField telefonoEmergenciaTxt =  new TextField("Teléfono emergencia : ");
    TextField nitTxt =  new TextField("NIT : ");
    TextField dpiTxt =  new TextField("DPI : ");
    TextField afiliacionIgssTxt =  new TextField("Afiliación IGSS : ");
    TextField codigoOcupacionTxt =  new TextField("Código Ocupación : ");
    TextField condicionLaboralTxt =  new TextField("Condición Laboral : ");
    CheckBox aplicaAnticipoChb = new CheckBox("Aplica Anticipo");
    CheckBox obraAsignadaChb = new CheckBox("Tiene obra asignada");
    CheckBox esLiquidador = new CheckBox("Es liquidador");
    TextField correlativoTxt =  new TextField("Correlativo planilla : ");
    TextField cuentaBancariaTxt =  new TextField("Cuenta Bancaria : ");
    DateField fechaIngresoDt = new DateField("Fecha Ingreso : ");
    DateField fechaEgresoDt = new DateField("Fecha Egreso : ");
    CheckBox aplicaIndemnizacion = new CheckBox("Aplica Indenmización");
    CheckBox inhabilitadoChb = new CheckBox("Inhabilitado");
    Label idLiquidacion = new Label("0");
    NumberField vacacionesDiasDerechoTxt =  new NumberField("Dias vacaciones derecho : ");
    NumberField vacacionesDiasGozadosTxt =  new NumberField("Dias vacaciones gozados : ");

    public static Locale locale = new Locale("ES", "GT");
    private static DecimalFormat numberFormat = new DecimalFormat("##,###,##0.00");

    private Date egresoDateMemory = null;
    
    boolean esNuevo;

    private UI mainUI;

    public EmpleadoView() {

        this.mainUI = UI.getCurrent();

        marginInfo = new MarginInfo(true, false, false, false);
        setSpacing(true);

        Label h1 = new Label("Empleados " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyName());
        h1.addStyleName("h3");
        h1.setWidth("100%");

        HorizontalLayout filterLayout = new HorizontalLayout();
        filterLayout.setSpacing(true);
        filterLayout.addComponent(h1);
        filterLayout.setMargin(false);
        filterLayout.setWidth("100%");
        filterLayout.addStyleName("rcorners2");

        addComponent(filterLayout);
        setComponentAlignment(filterLayout, Alignment.TOP_CENTER);

        mainLayout.setSpacing(true);
        mainLayout.setSizeFull();

        addComponent(mainLayout);

        createLeftContent();
        createRightContent();

        mainLayout.setExpandRatio(leftLayout, 1.0f);
        mainLayout.setExpandRatio(rightLayout, 1.5f);

    }

    private void createLeftContent() {

        leftLayout.addStyleName("rcorners3");
        leftLayout.setSizeFull();

        mainLayout.addComponent(leftLayout);

        empleadosContainer.addContainerProperty("id", String.class, null);
        empleadosContainer.addContainerProperty("nombre", String.class, null);

        empleadosGrid = new Grid("Empleados", empleadosContainer);
        empleadosGrid.setWidth("100%");
        empleadosGrid.setImmediate(true);
        empleadosGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        empleadosGrid.setDescription("Seleccione un registro.");
        empleadosGrid.setHeightMode(HeightMode.ROW);
        empleadosGrid.setHeightByRows(20);
        empleadosGrid.setResponsive(true);
        empleadosGrid.getColumn("id").setExpandRatio(1);
        empleadosGrid.getColumn("nombre").setExpandRatio(2);

        empleadosGrid.addSelectionListener(new SelectionListener() {
            @Override
            public void select(SelectionEvent event) {
                if (empleadosGrid.getSelectedRow() != null) {
                    esNuevo = false;
                    mostrarDatos(String.valueOf(empleadosContainer.getContainerProperty(empleadosGrid.getSelectedRow(), "id").getValue()));
                }
            }
        });

        HeaderRow filterRow = empleadosGrid.appendHeaderRow();

        HeaderCell cell = filterRow.getCell("nombre");

        TextField filterField = new TextField();
        filterField.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField.setInputPrompt("Filtrar");
        filterField.setColumns(15);

        filterField.addTextChangeListener(change -> {
            empleadosContainer.removeContainerFilters("nombre");

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                empleadosContainer.addContainerFilter(
                        new SimpleStringFilter("nombre",
                                change.getText(), true, false));
            }
        });
        cell.setComponent(filterField);

        fillGridEmpleados();

        leftLayout.addComponent(empleadosGrid);

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setMargin(false);
        buttonsLayout.setWidth("100%");
        buttonsLayout.addStyleName("rcorners3");

        refreshBtn = new Button("Refrescar");
        refreshBtn.setIcon(FontAwesome.REFRESH);
        refreshBtn.setDescription("Refrescar");
        refreshBtn.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        refreshBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                fillGridEmpleados();
            }
        });

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
                clearForms();
                idEmpleadoTxt.focus();
                esNuevo = true;
            }
        });

        salarioBtn = new Button("Salario");
        salarioBtn.setIcon(FontAwesome.MONEY);
        salarioBtn.setDescription("SALARIO");
        salarioBtn.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        salarioBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
//                if (!((SopdiUI) mainUI).sessionInformation.getStrUserProfile().equals("ADMINISTRADOR")) {
//                    Notification.show("Usuario no tiene permiso para esta operación.", Notification.Type.WARNING_MESSAGE);
//                    return;
//                }
                if(empleadosGrid.getSelectedRow() == null) {
                    Notification.show("Por favor seleccione un empleado de la lista!", Notification.Type.HUMANIZED_MESSAGE);
                    return;
                }
                EmpleadoSalarioForm empleadoSalarioForm = new EmpleadoSalarioForm(String.valueOf(empleadosContainer.getContainerProperty(empleadosGrid.getSelectedRow(), "id").getValue()));
                mainUI.addWindow(empleadoSalarioForm);
                empleadoSalarioForm.center();
            }
        });

        vacacionesBtn = new Button("Vacaciones");
        vacacionesBtn.setIcon(FontAwesome.BATTERY_4);
        vacacionesBtn.setDescription("Vacacinoes");
        vacacionesBtn.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        vacacionesBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
//                if (!((SopdiUI) mainUI).sessionInformation.getStrUserProfile().equals("ADMINISTRADOR")) {
//                    Notification.show("Usuario no tiene permiso para esta operación.", Notification.Type.WARNING_MESSAGE);
//                    return;
//                }
                if(empleadosGrid.getSelectedRow() == null) {
                    Notification.show("Por favor seleccione un empleado de la lista!", Notification.Type.HUMANIZED_MESSAGE);
                    return;
                }
                EmpleadoAusenciasForm empleadoAusenciasForm = new EmpleadoAusenciasForm(
                        String.valueOf(empleadosContainer.getContainerProperty(empleadosGrid.getSelectedRow(), "id").getValue()),
                        (String) cargoCbx.getValue());
                mainUI.addWindow(empleadoAusenciasForm);
                empleadoAusenciasForm.center();
            }
        });

        deleteBtn = new Button("Eliminar");
        deleteBtn.setIcon(FontAwesome.REMOVE);
        deleteBtn.setDescription("Eliminar");
        deleteBtn.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        deleteBtn.addStyleName(ValoTheme.BUTTON_DANGER);
        deleteBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (!((SopdiUI) mainUI).sessionInformation.getStrUserProfile().equals("ADMINISTRADOR")) {
                    Notification.show("Usuario no tiene permiso para esta operación.", Notification.Type.WARNING_MESSAGE);
                    return;
                }
                if (empleadosContainer.size() > 0) {
                    if (empleadosGrid.getSelectedRow() != null) {

//                        if (historialContratoTable.size() > 0) {
//                            Notification.show("Este proveedor/cliente tiene historial de contrato, no se puede eliminar.", Notification.Type.WARNING_MESSAGE);
//                            return;
//                        }
//                        if (historialCambiosTable.size() > 0) {
//                            Notification.show("Este proveedor/cliente tiene historial de cambios, no se puede eliminar.", Notification.Type.WARNING_MESSAGE);
//                            return;
//                        }
//                        if (historialContableTable.size() > 0) {
//                            Notification.show("Este proveedor/cliente tiene historial de contable, no se puede eliminar.", Notification.Type.WARNING_MESSAGE);
//                            return;
//                        }
                        ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de eliminar el registro del empleado?",
                                "SI", "NO", new ConfirmDialog.Listener() {

                                    public void onClose(ConfirmDialog dialog) {
                                        if (dialog.isConfirmed()) {
//                                            deleteProveedor();
                                        }
                                    }
                                });
                    } else {
                        if (empleadosGrid.getSelectedRow() == null) {
                            Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
                        }
                    }
                }
            }
        });

        buttonsLayout.addComponent(refreshBtn);
        buttonsLayout.addComponent(nuevoBtn);
        buttonsLayout.addComponent(salarioBtn);
        buttonsLayout.addComponent(vacacionesBtn);
        buttonsLayout.addComponent(deleteBtn);
        buttonsLayout.setComponentAlignment(deleteBtn, Alignment.BOTTOM_RIGHT);

        leftLayout.addComponent(buttonsLayout);

        leftLayout.setComponentAlignment(empleadosGrid,Alignment.TOP_CENTER);
        leftLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);

        mainLayout.addComponent(leftLayout);

    }

    private void fillGridEmpleados() {
        empleadosContainer.removeAllItems();

        String queryString = " SELECT * FROM proveedor ";
        queryString += " WHERE Inhabilitado = 0 ";
        queryString += " AND EsPlanilla = 1";
        queryString += " AND IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
        queryString += " ORDER BY Nombre ";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            Object itemId;
            while (rsRecords.next()) {

                itemId = empleadosContainer.addItem();

                System.out.println(itemId);
                System.out.println(empleadosContainer.getContainerPropertyIds());
                System.out.println(empleadosContainer.getContainerProperty(itemId, "id"));
                empleadosContainer.getContainerProperty(itemId, "id").setValue(rsRecords.getString("IdProveedor"));
                if(rsRecords.getString("PrimerNombre").equals("")){
                    empleadosContainer.getContainerProperty(itemId, "nombre").setValue(rsRecords.getString("Nombre"));
                }else{
                    empleadosContainer.getContainerProperty(itemId, "nombre").setValue(rsRecords.getString("PrimerNombre") + " " + rsRecords.getString("SegundoNombre") + " " + rsRecords.getString("PrimerApellido") + " " + rsRecords.getString("SegundoApellido") +  " " + rsRecords.getString("ApellidoCasada") );
                }
//                empleadoCbx.getItem(rsRecords.getString("IDProveedor")).getItemProperty(NIT_PROPERTY).setValue(rsRecords.getString("NIT"));
//                empleadoCbx.getItem(rsRecords.getString("IDProveedor")).getItemProperty(GRUPO_PROPERTY).setValue(rsRecords.getString("GRUPO"));
//                empleadoCbx.getItem(rsRecords.getString("IDProveedor")).getItemProperty(NOMBRESINCODIGO_PROPERTY).setValue(rsRecords.getString("Nombre"));
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar Proveedores/Empleados " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    private void createRightContent() {

        rightLayout.addStyleName("rcorners3");
//        rightLayout.setSizeFull();

        mainLayout.addComponent(rightLayout);

        HorizontalLayout formsLayout = new HorizontalLayout();
        formsLayout.setWidth("100%");

        HorizontalLayout middleLayout = new HorizontalLayout();
        middleLayout.addStyleName("rcorners3");
        middleLayout.setWidth("100%");
        middleLayout.setSpacing(true);

        rightLayout.addComponents(formsLayout, middleLayout);

        FormLayout leftFormLayout = new FormLayout();
        FormLayout rightFormLayout = new FormLayout();

        formsLayout.addComponents(leftFormLayout, rightFormLayout);


        generoCbx.addItem("Masculino");
        generoCbx.addItem("Femenino");
        generoCbx.setNullSelectionAllowed(false);
        generoCbx.setInvalidAllowed(false);
        generoCbx.setTextInputAllowed(false);
        generoCbx.select("Masculino");

        cargoCbx.setWidth("95%");
        cargoCbx.addItem("");

        String queryString = " SELECT * FROM empleado_cargo WHERE IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) {
                cargoCbx.addItem(rsRecords.getString("Cargo"));
             }
        } catch (Exception ex1) {
            System.out.println("Error al listar Proveedores/Empleados " + ex1.getMessage());
            ex1.printStackTrace();
        }
        cargoCbx.addItem("");
        cargoCbx.setNullSelectionAllowed(false);
        cargoCbx.setInvalidAllowed(false);
        cargoCbx.setTextInputAllowed(false);
        cargoCbx.setNewItemsAllowed(false);
        cargoCbx.select("");

        direccionTxt.setWidth("100%");
        direccionTxt.setHeight("5em");

        nombreCompletoTxt.setWidth("100%");

        primerNombreTxt.addValueChangeListener(event
                -> completarNombre()
        );
        segundoNombreTxt.addValueChangeListener(event
                -> completarNombre()
        );
        primerApellidoTxt.addValueChangeListener(event
                -> completarNombre()
        );
        segundoApellidoTxt.addValueChangeListener(event
                -> completarNombre()
        );
        apellidoCasadaTxt.addValueChangeListener(event
                -> completarNombre()
        );

        fechaIngresoDt.setDateFormat("dd/MM/yyyy");
        fechaEgresoDt.setDateFormat("dd/MM/yyyy");


        idLiquidacion.setCaption("Planilla Liquidación No.");

        leftFormLayout.addComponents(idEmpleadoTxt, cargoCbx, generoCbx, primerNombreTxt, segundoNombreTxt, primerApellidoTxt, segundoApellidoTxt);
        leftFormLayout.addComponents(apellidoCasadaTxt, nacionalidadTxt, direccionTxt, telefonoTxt, telefonoEmergenciaTxt);
        rightFormLayout.addComponents(nitTxt, dpiTxt, afiliacionIgssTxt, codigoOcupacionTxt, condicionLaboralTxt, cuentaBancariaTxt,correlativoTxt);
        rightFormLayout.addComponents(fechaIngresoDt, fechaEgresoDt, aplicaIndemnizacion, aplicaAnticipoChb, obraAsignadaChb, esLiquidador, inhabilitadoChb);
        middleLayout.addComponents(idLiquidacion, vacacionesDiasDerechoTxt, vacacionesDiasGozadosTxt);

        rightLayout.addComponent(nombreCompletoTxt);

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setMargin(false);
        buttonsLayout.setWidth("100%");

        rightLayout.addComponent(buttonsLayout);

        Button saveBtn = new Button("Guardar");
        saveBtn.setIcon(FontAwesome.SAVE);
        saveBtn.setWidth(120, Sizeable.UNITS_PIXELS);
        saveBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                guardarDatos();
            }
        });

        buttonsLayout.addComponent(saveBtn);
        buttonsLayout.setComponentAlignment(saveBtn,Alignment.MIDDLE_CENTER);

    }

    private void completarNombre() {
        nombreCompletoTxt.setValue(
                primerNombreTxt.getValue()
                + " "
                + segundoNombreTxt.getValue()
                + " "
                + primerApellidoTxt.getValue()
                + " "
                + segundoApellidoTxt.getValue()
                + " "
                + apellidoCasadaTxt.getValue()
        );
    }

    private void clearForms() {
        idEmpleadoTxt.setValue("");
        cargoCbx.setValue("");
        generoCbx.setValue("Masculino");
        primerNombreTxt.setValue("");
        segundoNombreTxt.setValue("");
        primerApellidoTxt.setValue("");
        segundoApellidoTxt.setValue("");
        apellidoCasadaTxt.setValue("");
//        nombreCompletoTxt.setValue("");  lo hace el listener del event on change
        direccionTxt.setValue("");
        nacionalidadTxt.setValue("Guatemalteco");
        telefonoTxt.setValue("");
        telefonoEmergenciaTxt.setValue("");
        nitTxt.setValue("");
        dpiTxt.setValue("");
        afiliacionIgssTxt.setValue("");
        codigoOcupacionTxt.setValue("");
        condicionLaboralTxt.setValue("");
        aplicaAnticipoChb.setValue(false);
        obraAsignadaChb.setValue(false);
        esLiquidador.setValue(false);
        inhabilitadoChb.setValue(false);
        correlativoTxt.setValue("0");
        fechaEgresoDt.setValue(null);
        fechaIngresoDt.setValue(new Date());
        aplicaIndemnizacion.setValue(false);
        vacacionesDiasDerechoTxt.setValue(15d);
        vacacionesDiasGozadosTxt.setValue(0d);

    }

    private void mostrarDatos(String idProveedor) {

        clearForms();

        String queryString = " SELECT * FROM proveedor ";
        queryString += " WHERE IdProveedor = " + idProveedor;
        queryString += " AND IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
        queryString += " AND EsPlanilla = 1";

//System.out.println(queryString);

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if(rsRecords.next()) {
                idEmpleadoTxt.setValue(rsRecords.getString("IDProveedor"));
                cargoCbx.setValue(rsRecords.getString("Cargo"));
                generoCbx.setValue(rsRecords.getString("Genero"));
                primerNombreTxt.setValue(rsRecords.getString("PrimerNombre"));
                segundoNombreTxt.setValue(rsRecords.getString("SegundoNombre"));
                primerApellidoTxt.setValue(rsRecords.getString("PrimerApellido"));
                segundoApellidoTxt.setValue(rsRecords.getString("SegundoApellido"));
                apellidoCasadaTxt.setValue(rsRecords.getString("ApellidoCasada"));
                direccionTxt.setValue(rsRecords.getString("Direccion"));
                nacionalidadTxt.setValue(rsRecords.getString("nacionalidad"));
                telefonoTxt.setValue(rsRecords.getString("Telefono"));
                telefonoEmergenciaTxt.setValue(rsRecords.getString("TelefonoEmergencia"));
                nitTxt.setValue(rsRecords.getString("Nit"));
                dpiTxt.setValue(rsRecords.getString("Dpi"));
                afiliacionIgssTxt.setValue(rsRecords.getString("AfiliacionIgss"));
                codigoOcupacionTxt.setValue(rsRecords.getString("CodigoOcupacion"));
                condicionLaboralTxt.setValue(rsRecords.getString("CondicionLaboral"));
                aplicaAnticipoChb.setValue(rsRecords.getString("AplicaAnticipoSalario").equals("1"));
                obraAsignadaChb.setValue(rsRecords.getString("AsignadoObra").equals("1"));
                esLiquidador.setValue(rsRecords.getString("EsLiquidador").equals("1"));
                cuentaBancariaTxt.setValue(rsRecords.getString("BancoCuenta"));
                fechaIngresoDt.setValue(rsRecords.getDate("FechaIngreso"));
                if(rsRecords.getObject("FechaEgreso") != null) {
                    fechaEgresoDt.setValue(rsRecords.getDate("FechaEgreso"));
                    egresoDateMemory = rsRecords.getDate("FechaEgreso");
                }
                else {
                    fechaEgresoDt.setValue(null);
                }
                correlativoTxt.setValue(rsRecords.getString("IdCorrFinal"));
                inhabilitadoChb.setValue(rsRecords.getString("Inhabilitado").equals("1"));
                aplicaIndemnizacion.setValue(rsRecords.getString("AplicaIndemnizacion").equals("1"));

                idLiquidacion.setValue(rsRecords.getString("IdPlanillaLiquidacion"));

                vacacionesDiasDerechoTxt.setValue(rsRecords.getDouble("DiasVacacionesDerecho"));
                vacacionesDiasGozadosTxt.setValue(rsRecords.getDouble("DiasVacacionesGozados"));

            }

        } catch (Exception ex1) {
            System.out.println("Error al listar Proveedores/Empleados " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public boolean exportToExcel(Table tableToExport) {
        ExcelExport excelExport;

        excelExport = new ExcelExport(tableToExport);
        excelExport.excludeCollapsedColumns();
        excelExport.setExportFileName("NISA_CATALOGO_EMPLEADOS.xls");

        String mainTitle = "CATALOGO DE EMPLEADOS AL: " + new Utileria().getFechaYYYYMMDD_1(new Date());

        excelExport.setReportTitle(mainTitle);

        excelExport.export();

        return true;

    }

    private void guardarDatos() {

        if (idEmpleadoTxt.getValue().trim().isEmpty()) {
            Notification.show("Error, falta el Id del Empleado!", Notification.Type.ERROR_MESSAGE);
            idEmpleadoTxt.focus();
            return;
        }
        if (primerNombreTxt.getValue().trim().isEmpty()) {
            Notification.show("Error, falta el nombre del empleado!", Notification.Type.ERROR_MESSAGE);
            primerApellidoTxt.focus();
            return;
        }
        if (primerApellidoTxt.getValue().trim().isEmpty()) {
            Notification.show("Error, falta el apellido del empleado!", Notification.Type.ERROR_MESSAGE);
            primerApellidoTxt.focus();
            return;
        }
        if (nombreCompletoTxt.getValue().trim().isEmpty()) {
            Notification.show("Error, falta el nombre del empleado!", Notification.Type.ERROR_MESSAGE);
            primerNombreTxt.focus();
            return;
        }
        if (cargoCbx.getValue() == null) {
            Notification.show("Error, falta el cargo/puesto/plazo del empleado!", Notification.Type.ERROR_MESSAGE);
            cargoCbx.focus();
            return;
        }
        if (direccionTxt.getValue().trim().isEmpty()) {
            Notification.show("Error, falta la dirección del empleado!", Notification.Type.ERROR_MESSAGE);
            direccionTxt.focus();
            return;
        }
        if (dpiTxt.getValue().trim().isEmpty()) {
            Notification.show("Error, falta el DPI del empleado!", Notification.Type.ERROR_MESSAGE);
            dpiTxt.focus();
            return;
        }
        if (fechaIngresoDt.getValue() == null) {
            Notification.show("Error, falta la fecha de ingreso del empleado!", Notification.Type.ERROR_MESSAGE);
            fechaIngresoDt.focus();
            return;
        }
        if (telefonoTxt.getValue() == null) {
            Notification.show("Error, falta teléfono del empleado!", Notification.Type.ERROR_MESSAGE);
            telefonoTxt.focus();
            return;
        }
        if (telefonoEmergenciaTxt.getValue() == null) {
            Notification.show("Error, falta teléfono de emergencia del empleado!", Notification.Type.ERROR_MESSAGE);
            telefonoEmergenciaTxt.focus();
            return;
        }

        if(fechaEgresoDt.getValue() == null) {
            if(aplicaIndemnizacion.getValue()) {
                Notification.show("Aplica indemnización debe ser solamente cuando el empleado tiene fecha de EGRESO!", Notification.Type.WARNING_MESSAGE);
                aplicaIndemnizacion.setValue(false);
                return;
            }
        }

        String queryString = "";

        if (esNuevo ) {
            queryString = "Insert Into proveedor (N0, Grupo0, N1, Grupo, N2, Tipo, N3, Numero, N4, IDProveedor, ";
            queryString += " Nombre, Producto, NIT, DPI,Regimen, GrupoTrabajo, EstatusTrabajo, Razon,";
            queryString += " AnticipoLote, Provision, DiasAnticipo,";
            queryString += " DiasCredito, AnticipoUnidad, DiaProvision, Email, ";
            queryString += " IdEmpresa, CuentaAnticiposLiquidar, CuentaAcreedores, ";
            queryString += " Inhabilitado, EsProveedor, EsCliente,";
            queryString += " EsLiquidador, EsComite, EsPlanilla, EsRelacionada, EsBanco, ";
            queryString += " EsAgenteRetenedorISR,  EsAgenteRetenedorIVA, EsJefe, Cargo, IdUsuario, ";
            queryString += " PrimerNombre, SegundoNombre, PrimerApellido, SegundoApellido, ApellidoCasada, ";
            queryString += " Banco, BancoCuenta, Nacionalidad, Direccion, Telefono, TelefonoEmergencia, Genero, TituloAcademico,  ";
            queryString += " AfiliacionIgss, FechaIngreso, FechaEgreso, CodigoOcupacion, CondicionLaboral,";
            queryString += " AplicaAnticipoSalario, AsignadoObra, IdCorrFinal, AplicaIndemnizacion, DiasVacacionesDerecho, DiasVacacionesGozados";
            queryString += ")";
            queryString += " Values (";
            queryString += "9";
            queryString += ",'Empleado'";
            queryString += "," + ((SopdiUI)mainUI).sessionInformation.getStrAccountingCompanyId().charAt(0); //N1
            queryString += ",'Planilla'";
            queryString += "," + ((SopdiUI)mainUI).sessionInformation.getStrAccountingCompanyId().charAt(1); //N2
            queryString += ",'Construccion'";
            queryString += "," + ((String)idEmpleadoTxt.getValue()).charAt(3); //N3
            queryString += ",'Empresa'";
            queryString += "," + ((String)idEmpleadoTxt.getValue()).substring(3, 5); //N4
            queryString += "," + idEmpleadoTxt.getValue();
            queryString += ",'" + nombreCompletoTxt.getValue() + "'";
            queryString += ",'X'";
            queryString += ",'" + nitTxt.getValue() + "'";
            queryString += ",'" + dpiTxt.getValue() + "'";
            queryString += ",'NORMAL'";
            queryString += ",''";
            queryString += ",''";
            queryString += ",''";
            queryString += ",0";
            queryString += ",0";
            queryString += ",0";
            queryString += ",0";
            queryString += ",0";
            queryString += ",0";
            queryString += ",''";
            queryString += ", " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
            queryString += ",''";
            queryString += ",''";
            queryString += ", 0"; // INHABILITADO
            queryString += ", 0"; // ESPROVEEDOR
            queryString += ", 0"; // ESCLIENTE
            queryString += ","  + (esLiquidador.getValue() ? "1" : "0");
            queryString += ", 0"; // ESCOMITE
            queryString += ", 1"; // ESPLANILLA
            queryString += ", 0"; // ESRELACIONADA
            queryString += ", 0"; // ESBANCO
            queryString += ", 0"; // ESAGENTERENEDORISR
            queryString += ", 0"; // ESAGENTERENEDORIVA
            queryString += ", 0"; // ESJEFE
            queryString += ", '" + cargoCbx.getValue() + "'";
            queryString += ",0";
            queryString += ",'" + primerNombreTxt.getValue() + "'";
            queryString += ",'" + segundoNombreTxt.getValue() + "'";
            queryString += ",'" + primerApellidoTxt.getValue() + "'";
            queryString += ",'" + segundoApellidoTxt.getValue() + "'";
            queryString += ",'" + apellidoCasadaTxt.getValue() + "'";
            queryString += ",'Banco Industrial'";
            queryString += ",'" + cuentaBancariaTxt.getValue() + "'";
            queryString += ",'" + nacionalidadTxt.getValue() + "'";
            queryString += ",'" + direccionTxt.getValue() + "'";
            queryString += ",'" + telefonoTxt.getValue() + "'";
            queryString += ",'" + telefonoEmergenciaTxt.getValue() + "'";
            queryString += ",'" + generoCbx.getValue() + "'";
            queryString += ",''"; //TITULOACADEMICO
            queryString += ",'" + afiliacionIgssTxt.getValue() + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaIngresoDt.getValue()) + "'";
            if(fechaEgresoDt.getValue() != null) {
                queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaEgresoDt.getValue()) + "'";
            }
            else {
                queryString += ",null";
            }
            queryString += ",'" + codigoOcupacionTxt.getValue() + "'";
            queryString += ",'" + condicionLaboralTxt.getValue() + "'";
            queryString += ","  + (aplicaAnticipoChb.getValue() ? "1" : "0");
            queryString += ","  + (obraAsignadaChb.getValue() ? "1" : "0");
            queryString += ","  + correlativoTxt.getValue() ;
            queryString += ","  + (aplicaIndemnizacion.getValue() ? "1" : "0");
            queryString += ","  + vacacionesDiasDerechoTxt.getValue();
            queryString += ","  + vacacionesDiasGozadosTxt.getValue();
            queryString += ")";
        } else {
            queryString = "Update proveedor Set ";
            queryString += " IDProveedor = " + idEmpleadoTxt.getValue();
            queryString += ",NIT = '" + nitTxt.getValue() + "'";
            queryString += ",DPI = '" + dpiTxt.getValue() + "'";
//            queryString += ",Email = '" + emailTxt.getValue() + "'";
            queryString += ",PrimerNombre = '" + primerNombreTxt.getValue() + "'";
            queryString += ",SegundoNombre = '" + segundoNombreTxt.getValue() + "'";
            queryString += ",PrimerApellido = '" + primerApellidoTxt.getValue() + "'";
            queryString += ",SegundoApellido = '" + segundoApellidoTxt.getValue() + "'";
            queryString += ",ApellidoCasada = '" + apellidoCasadaTxt.getValue() + "'";
            queryString += ",BancoCuenta = '" + cuentaBancariaTxt.getValue() + "'";
            queryString += ",Nacionalidad = '" + nacionalidadTxt.getValue() + "'";
            queryString += ",Direccion = '" + direccionTxt.getValue() + "'";
            queryString += ",Telefono = '" + telefonoTxt.getValue() + "'";
            queryString += ",TelefonoEmergencia = '" + telefonoEmergenciaTxt.getValue() + "'";
            queryString += ",Genero = '" + generoCbx.getValue() + "'";
            queryString += ",AfiliacionIgss = '" + afiliacionIgssTxt.getValue() + "'";
            queryString += ",FechaIngreso = '" + Utileria.getFechaYYYYMMDD_1(fechaIngresoDt.getValue()) + "'";
            if(fechaEgresoDt.getValue() != null) {
                queryString += ",FechaEgreso = '" + Utileria.getFechaYYYYMMDD_1(fechaEgresoDt.getValue()) + "'";
            }
            queryString += ",CodigoOcupacion = '" + codigoOcupacionTxt.getValue() + "'";
            queryString += ",CondicionLaboral = '" + condicionLaboralTxt.getValue() + "'";
            queryString += ",AplicaAnticipoSalario = "  + (aplicaAnticipoChb.getValue() ? "1" : "0");
            queryString += ",AsignadoObra = "  + (obraAsignadaChb.getValue() ? "1" : "0");
            queryString += ",EsLiquidador = "  + (esLiquidador.getValue() ? "1" : "0");
            queryString += ",IdCorrFinal = "  + correlativoTxt.getValue() ;
            queryString += ",Inhabilitado = "  + (inhabilitadoChb.getValue() ? "1" : "0");
            queryString += ",Cargo = '" + cargoCbx.getValue() + "'";
            queryString += ",AplicaIndemnizacion = " + (aplicaIndemnizacion.getValue() ? "1" : "0");
            queryString += ",DiasVacacionesDerecho = "  + vacacionesDiasDerechoTxt.getValue();
            queryString += ",DiasVacacionesGozados = "  + vacacionesDiasGozadosTxt.getValue();
            queryString += " Where IdProveedor = " + idEmpleadoTxt.getValue();

        }

System.out.println("empleado queryString = " + queryString);

//        Object selectedItem = ((ProveedorView) (mainUI.getNavigator().getCurrentView())).proveedorGrid.getSelectedRow();

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            Notification.show("OPERACION EXITOSA!", Notification.Type.HUMANIZED_MESSAGE);

            Logger.getLogger(ProveedorForm.class.getName()).log(Level.INFO, "El usuario {0} ha actualizado el registro del empleado {1} - {2}",
                    new Object[]{((SopdiUI) mainUI).sessionInformation.getStrUserName(),
                        idEmpleadoTxt.getValue(),
                        nombreCompletoTxt.getValue()});
            actualizarAsisitencia();

            this.fillGridEmpleados();

        }
        catch(Exception exc99) {
            Notification.show("Error al actualizar registro : " + exc99.getMessage(), Notification.Type.ERROR_MESSAGE);
            exc99.printStackTrace();
        }

    }

    private void actualizarAsisitencia() {
        String queryString = "";
        Date fechaPrint = null;
        String estado = "";

        if (fechaEgresoDt.getValue() == egresoDateMemory){
            egresoDateMemory = null;
            return;
        }

        try {
            if (fechaEgresoDt.getValue() == null && egresoDateMemory != null) {
                queryString = "UPDATE empleado_asistencia SET "
                        + "Estatus = 'PRESENTE', "
                        + "Razon = '', "
                        + "EsDescuento = 0, "
                        + "EsDefinitiva = 0 "
                        + "WHERE IdEmpleado = " + idEmpleadoTxt.getValue() + " "
                        + "AND Fecha = '" + Utileria.getFechaYYYYMMDD_1(egresoDateMemory) + "'";
                fechaPrint = egresoDateMemory;
                egresoDateMemory = null;
                estado = "PRESENTE";

            }else if(fechaEgresoDt.getValue() != null) {
                queryString = "UPDATE empleado_asistencia SET "
                        + "Estatus = 'DE BAJA', "
                        + "Razon = 'Retiro de labores', "
                        + "EsDescuento = 1, "
                        + "EsDefinitiva = 1 "
                        + "WHERE IdEmpleado = " + idEmpleadoTxt.getValue() + " "
                        + "AND Fecha = '" + Utileria.getFechaYYYYMMDD_1(fechaEgresoDt.getValue()) + "'";
                fechaPrint = fechaEgresoDt.getValue();
                egresoDateMemory = fechaEgresoDt.getValue();
                estado = "DE BAJA";
            }

            if(!queryString.isEmpty()) {
                stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                stQuery.executeUpdate(queryString);


                Logger.getLogger(ProveedorForm.class.getName()).log(Level.INFO, "ACUTALIZACION DE EGRESO ({0}) EN ASISTENCIA DEL EMPLEADO {1} - {2} CON ESTADO {3}",
                        new Object[]{
                                Utileria.getStaticFecha(fechaPrint),
                                idEmpleadoTxt.getValue(),
                                nombreCompletoTxt.getValue(),
                                estado}   
                );
            }
        } catch (Exception ex) {
            Logger.getLogger(ProveedorForm.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al actualizar asistencia del empleado : " + ex.getMessage());
        }
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - EMPLEADOS");
    }

    private void deleteEmpleado() {
//        if (historialContableTable.size() > 0) {
//            Notification.show("Este registro tiene historial contable,  no se puede eliminar su registro!");
//            return;
//        }
//        String queryString = "Delete ";
//        queryString += " From  proveedor_nota ";
//        queryString += " Where IdProveedor = " + String.valueOf(salarioContainer.getContainerProperty(proveedorGrid.getSelectedRow(), IDPROVEEDOR_PROPERTY).getValue());
//
//        try {
//            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
//            stQuery.executeUpdate(queryString);
//
//            queryString = "Delete ";
//            queryString += " From  proveedor ";
//            queryString += " Where IdProveedor = " + String.valueOf(salarioContainer.getContainerProperty(proveedorGrid.getSelectedRow(), IDPROVEEDOR_PROPERTY).getValue());
//
//            stQuery.executeUpdate(queryString);
//
//            Notification.show("Operación exitosa!", Notification.Type.TRAY_NOTIFICATION);
//
//            salarioContainer.removeItem(proveedorGrid.getSelectedRow());
//
//        } catch (Exception ex) {
//
//            Logger.getLogger(EmpleadoView.class.getName()).log(Level.SEVERE, null, ex);
//            System.out.println("Error al ELIMINAR registros de proveedor : " + ex.getMessage());
//            Notification.show("Error al ELIMINAR registros de proveedor..!", Notification.Type.ERROR_MESSAGE);
//
//        }
    }
}
