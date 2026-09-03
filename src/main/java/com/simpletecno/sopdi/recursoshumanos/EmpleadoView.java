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
import com.vaadin.server.Responsive;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import com.vaadin.ui.Grid.HeaderCell;
import com.vaadin.ui.Grid.HeaderRow;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.ui.NumberField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
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
    Button saveBtn;

    Label formularioEstadoLbl = new Label("Nuevo empleado");

    CheckBox mostrarInhabilitadosChb = new CheckBox("Mostrar inhabilitados");

    ComboBox generoCbx = new ComboBox("Género");
    ComboBox cargoCbx = new ComboBox("Cargo / plaza");
    ComboBox departamentoCbx = new ComboBox("Departamento");
    TextField idEmpleadoTxt =  new TextField("ID empleado");
    TextField primerNombreTxt =  new TextField("Primer nombre");
    TextField segundoNombreTxt =  new TextField("Segundo nombre");
    TextField primerApellidoTxt =  new TextField("Primer apellido");
    TextField segundoApellidoTxt =  new TextField("Segundo apellido");
    TextField apellidoCasadaTxt =  new TextField("Apellido de casada");
    TextField nombreCompletoTxt =  new TextField("Nombre completo para IGSS");
    TextArea direccionTxt =  new TextArea("Dirección");
    TextField nacionalidadTxt =  new TextField("Nacionalidad");
    TextField telefonoTxt =  new TextField("Teléfono");
    TextField telefonoEmergenciaTxt =  new TextField("Teléfono de emergencia");
    TextField nitTxt =  new TextField("NIT");
    TextField dpiTxt =  new TextField("DPI");
    TextField afiliacionIgssTxt =  new TextField("Afiliación IGSS");
    TextField codigoOcupacionTxt =  new TextField("Código de ocupación");
    TextField condicionLaboralTxt =  new TextField("Condición laboral");
    CheckBox aplicaAnticipoChb = new CheckBox("Aplica Anticipo");
    CheckBox obraAsignadaChb = new CheckBox("Tiene obra asignada");
    CheckBox esLiquidador = new CheckBox("Es liquidador");
    TextField correlativoTxt =  new TextField("Correlativo de planilla");
    TextField cuentaBancariaTxt =  new TextField("Cuenta bancaria");
    DateField fechaIngresoDt = new DateField("Fecha de ingreso");
    DateField fechaEgresoDt = new DateField("Fecha de egreso");
    CheckBox aplicaIndemnizacion = new CheckBox("Aplica indemnización");
    CheckBox inhabilitadoChb = new CheckBox("Inhabilitado");
    Label idLiquidacion = new Label("0");
    NumberField vacacionesDiasDerechoTxt =  new NumberField("Días de vacaciones con derecho");
    NumberField vacacionesDiasGozadosTxt =  new NumberField("Días de vacaciones gozados");

    public static Locale locale = new Locale("ES", "GT");
    private static final DecimalFormat numberFormat = new DecimalFormat("##,###,##0.00");

    private Date egresoDateMemory = null;
    private String idEmpleadoOriginal = null;
    
    boolean esNuevo;

    private final UI mainUI;

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public EmpleadoView() {

        this.mainUI = UI.getCurrent();

        marginInfo = new MarginInfo(true, false, false, false);
        setSpacing(true);
        setSizeFull();

        mainLayout.setSpacing(true);
        mainLayout.setSizeFull();
        mainLayout.addStyleName("empleado-main");
        Responsive.makeResponsive(mainLayout);

        addComponent(mainLayout);
        setExpandRatio(mainLayout, 1.0f);

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
        empleadosContainer.addContainerProperty("inhabilitado", Boolean.class, false);

        empleadosGrid = new Grid("Empleados", empleadosContainer);
        empleadosGrid.setWidth("100%");
        empleadosGrid.setHeight("100%");
        empleadosGrid.setImmediate(true);
        empleadosGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        empleadosGrid.setDescription("Seleccione un registro.");
        empleadosGrid.setResponsive(true);
        empleadosGrid.removeColumn("inhabilitado");
        empleadosGrid.getColumn("id").setExpandRatio(1);
        empleadosGrid.getColumn("nombre").setExpandRatio(2);

        empleadosGrid.setRowStyleGenerator(new Grid.RowStyleGenerator() {
            @Override
            public String getStyle(Grid.RowReference row) {
                Object inhabilitado = row.getItem().getItemProperty("inhabilitado").getValue();
                return Boolean.TRUE.equals(inhabilitado) ? "inhabilitado" : null;
            }
        });

        empleadosGrid.addSelectionListener(new SelectionListener() {
            @Override
            public void select(SelectionEvent event) {
                if (empleadosGrid.getSelectedRow() != null) {
                    esNuevo = false;
                    mostrarDatos(String.valueOf(empleadosContainer.getContainerProperty(empleadosGrid.getSelectedRow(), "id").getValue()));
                    actualizarEstadoAcciones(true);
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

        HeaderCell idCell = filterRow.getCell("id");

        TextField idFilterField = new TextField();
        idFilterField.addStyleName(ValoTheme.TEXTFIELD_TINY);
        idFilterField.setInputPrompt("Filtrar");
        idFilterField.setColumns(8);

        idFilterField.addTextChangeListener(change -> {
            empleadosContainer.removeContainerFilters("id");

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                empleadosContainer.addContainerFilter(
                        new SimpleStringFilter("id",
                                change.getText(), true, false));
            }
        });
        idCell.setComponent(idFilterField);

        mostrarInhabilitadosChb.setDescription("Mostrar empleados inhabilitados");
        mostrarInhabilitadosChb.addValueChangeListener(event -> fillGridEmpleados());

        fillGridEmpleados();

        leftLayout.addComponent(mostrarInhabilitadosChb);
        leftLayout.addComponent(empleadosGrid);

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setMargin(false);
        buttonsLayout.setWidth("100%");
        buttonsLayout.addStyleName("rcorners3");

        refreshBtn = new Button();
        refreshBtn.setIcon(FontAwesome.REFRESH);
        refreshBtn.setDescription("Refrescar");
        refreshBtn.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        refreshBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                fillGridEmpleados();
            }
        });

        nuevoBtn = new Button();
        nuevoBtn.setIcon(FontAwesome.PLUS);
        nuevoBtn.setDescription("Nuevo");
        nuevoBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
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
                formularioEstadoLbl.setValue("Nuevo empleado");
                formularioEstadoLbl.removeStyleName("empleado-estado-edicion");
                actualizarEstadoAcciones(false);
            }
        });

        salarioBtn = new Button();
        salarioBtn.setIcon(FontAwesome.MONEY);
        salarioBtn.setDescription("Consultar salarios del empleado seleccionado");
        salarioBtn.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        salarioBtn.setEnabled(false);
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

        vacacionesBtn = new Button();
        vacacionesBtn.setIcon(FontAwesome.BATTERY_4);
        vacacionesBtn.setDescription("Administrar vacaciones y ausencias");
        vacacionesBtn.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        vacacionesBtn.setEnabled(false);
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

        deleteBtn = new Button();
        deleteBtn.setIcon(FontAwesome.REMOVE);
        deleteBtn.setDescription("Inhabilitar al empleado seleccionado");
        deleteBtn.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        deleteBtn.addStyleName(ValoTheme.BUTTON_DANGER);
        deleteBtn.setEnabled(false);
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
                        ConfirmDialog.show(UI.getCurrent(), "Confirme:", "¿Está seguro de inhabilitar al empleado?",
                                "SI", "NO", new ConfirmDialog.Listener() {

                                    public void onClose(ConfirmDialog dialog) {
                                        if (dialog.isConfirmed()) {
                                            deleteEmpleado();
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

        leftLayout.setExpandRatio(empleadosGrid, 1.0f);
        leftLayout.setComponentAlignment(empleadosGrid,Alignment.TOP_CENTER);
        leftLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);

        mainLayout.addComponent(leftLayout);

    }

    private void fillGridEmpleados() {
        empleadosContainer.removeAllItems();

        String queryString = "SELECT IdProveedor, Inhabilitado, Nombre, PrimerNombre, SegundoNombre, "
                + "PrimerApellido, SegundoApellido, ApellidoCasada FROM proveedor_empresa "
                + "WHERE EsPlanilla = 1";
        if (!mostrarInhabilitadosChb.getValue()) {
            queryString += " AND Inhabilitado = 0 ";
        }
        queryString += " AND IdEmpresa = ?";
        queryString += " ORDER BY Nombre ";

        try {
            Connection connection = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection();
            try (PreparedStatement statement = connection.prepareStatement(queryString)) {
                statement.setString(1, empresaId);
                try (ResultSet records = statement.executeQuery()) {

                    Object itemId;
                    while (records.next()) {

                        itemId = empleadosContainer.addItem();

                        empleadosContainer.getContainerProperty(itemId, "id").setValue(records.getString("IdProveedor"));
                        empleadosContainer.getContainerProperty(itemId, "inhabilitado").setValue(records.getBoolean("Inhabilitado"));
                        String primerNombre = valueOrEmpty(records.getString("PrimerNombre"));
                        if (primerNombre.isEmpty()) {
                            empleadosContainer.getContainerProperty(itemId, "nombre").setValue(valueOrEmpty(records.getString("Nombre")));
                        } else {
                            empleadosContainer.getContainerProperty(itemId, "nombre").setValue(buildNombreCompleto(
                                    primerNombre, records.getString("SegundoNombre"), records.getString("PrimerApellido"),
                                    records.getString("SegundoApellido"), records.getString("ApellidoCasada")));
                        }
                    }
                }
            }
        } catch (Exception ex1) {
            Logger.getLogger(EmpleadoView.class.getName()).log(Level.SEVERE, "Error al listar empleados", ex1);
            Notification.show("No fue posible cargar los empleados.", Notification.Type.ERROR_MESSAGE);
        }
    }

    private void createRightContent() {

        rightLayout.addStyleName("rcorners3");
        rightLayout.addStyleName("empleado-detalle");
        rightLayout.setWidth("100%");
        rightLayout.setHeightUndefined();
        rightLayout.setSpacing(true);

        mainLayout.addComponent(rightLayout);

        TabSheet empleadoTabs = new TabSheet();
        empleadoTabs.setWidth("100%");
        empleadoTabs.setHeightUndefined();
        empleadoTabs.addStyleName("empleado-tabs");

        FormLayout datosPersonalesForm = crearTabFormulario();
        FormLayout datosLaboralesForm = crearTabFormulario();
        FormLayout vacacionesForm = crearTabFormulario();

        empleadoTabs.addTab(datosPersonalesForm, "Datos personales", FontAwesome.USER);
        empleadoTabs.addTab(datosLaboralesForm, "Datos laborales", FontAwesome.BRIEFCASE);
        empleadoTabs.addTab(vacacionesForm, "Vacaciones y liquidación", FontAwesome.CALENDAR);

        formularioEstadoLbl.addStyleName(ValoTheme.LABEL_H2);
        formularioEstadoLbl.addStyleName(ValoTheme.LABEL_COLORED);
        formularioEstadoLbl.addStyleName("empleado-estado");
        rightLayout.addComponent(formularioEstadoLbl);
        rightLayout.addComponent(empleadoTabs);


        generoCbx.addItem("Masculino");
        generoCbx.addItem("Femenino");
        generoCbx.setNullSelectionAllowed(false);
        generoCbx.setInvalidAllowed(false);
        generoCbx.setTextInputAllowed(false);
        generoCbx.select("Masculino");

        cargoCbx.setWidth("95%");
        cargoCbx.addItem("");

        String queryString = "SELECT Cargo FROM empleado_cargo WHERE IdEmpresa = ?";

        try (PreparedStatement statement = ((SopdiUI) UI.getCurrent()).databaseProvider
                .getCurrentConnection().prepareStatement(queryString)) {
            statement.setString(1, empresaId);
            try (ResultSet records = statement.executeQuery()) {
                while (records.next()) {
                    cargoCbx.addItem(records.getString("Cargo"));
                }
            }
        } catch (Exception ex1) {
            Logger.getLogger(EmpleadoView.class.getName()).log(Level.SEVERE, "Error al listar cargos", ex1);
            Notification.show("No fue posible cargar los cargos.", Notification.Type.ERROR_MESSAGE);
        }
        cargoCbx.setNullSelectionAllowed(false);
        cargoCbx.setInvalidAllowed(false);
        cargoCbx.setTextInputAllowed(false);
        cargoCbx.setNewItemsAllowed(false);
        cargoCbx.select("");

        departamentoCbx.setWidth("95%");
        departamentoCbx.addItem("");
        try (PreparedStatement deptPs = ((SopdiUI) UI.getCurrent()).databaseProvider
                .getCurrentConnection().prepareStatement(
                        "SELECT Departamento FROM empleado_departamento WHERE IdEmpresa = ? ORDER BY Departamento")) {
            deptPs.setString(1, empresaId);
            try (ResultSet deptRs = deptPs.executeQuery()) {
                while (deptRs.next()) {
                    departamentoCbx.addItem(deptRs.getString("Departamento"));
                }
            }
        } catch (Exception ex1) {
            Logger.getLogger(EmpleadoView.class.getName()).log(Level.SEVERE, "Error al listar departamentos", ex1);
        }
        departamentoCbx.setNullSelectionAllowed(false);
        departamentoCbx.setInvalidAllowed(false);
        departamentoCbx.setTextInputAllowed(false);
        departamentoCbx.setNewItemsAllowed(false);
        departamentoCbx.select("");

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

        configurarCamposFormulario();

        inhabilitadoChb.addValueChangeListener(event -> {
            aplicarEstiloInhabilitado(inhabilitadoChb.getValue());
            if (deleteBtn != null) {
                deleteBtn.setEnabled(empleadosGrid.getSelectedRow() != null
                        && !Boolean.TRUE.equals(inhabilitadoChb.getValue()));
            }
        });


        idLiquidacion.setCaption("Planilla Liquidación No.");

        datosPersonalesForm.addComponents(idEmpleadoTxt, generoCbx, primerNombreTxt, segundoNombreTxt,
                primerApellidoTxt, segundoApellidoTxt, apellidoCasadaTxt, nombreCompletoTxt,
                nacionalidadTxt, direccionTxt, telefonoTxt, telefonoEmergenciaTxt);

        datosLaboralesForm.addComponents(cargoCbx, departamentoCbx, nitTxt, dpiTxt, afiliacionIgssTxt,
                codigoOcupacionTxt, condicionLaboralTxt, cuentaBancariaTxt, correlativoTxt,
                fechaIngresoDt, fechaEgresoDt, aplicaIndemnizacion, aplicaAnticipoChb,
                obraAsignadaChb, esLiquidador, inhabilitadoChb);

        vacacionesForm.addComponents(idLiquidacion, vacacionesDiasDerechoTxt, vacacionesDiasGozadosTxt);

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setMargin(false);
        buttonsLayout.setWidth("100%");

        rightLayout.addComponent(buttonsLayout);

        saveBtn = new Button("Guardar cambios");
        saveBtn.setIcon(FontAwesome.SAVE);
        saveBtn.setWidth(180, Sizeable.UNITS_PIXELS);
        saveBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        saveBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                guardarDatosSeguro();
            }
        });

        buttonsLayout.addComponent(saveBtn);
        buttonsLayout.setComponentAlignment(saveBtn,Alignment.MIDDLE_CENTER);

    }

    private FormLayout crearTabFormulario() {
        FormLayout formulario = new FormLayout();
        formulario.setWidth("100%");
        formulario.setHeightUndefined();
        formulario.setMargin(true);
        formulario.setSpacing(true);
        formulario.addStyleName("empleado-tab-form");
        return formulario;
    }

    private void aplicarEstiloInhabilitado(Boolean inhabilitado) {
        if (Boolean.TRUE.equals(inhabilitado)) {
            rightLayout.addStyleName("rightform-inhabilitado");
        } else {
            rightLayout.removeStyleName("rightform-inhabilitado");
        }
    }

    private void configurarCamposFormulario() {
        Component[] camposAnchoCompleto = {
                idEmpleadoTxt, cargoCbx, generoCbx, primerNombreTxt, segundoNombreTxt,
                primerApellidoTxt, segundoApellidoTxt, apellidoCasadaTxt, nacionalidadTxt,
                direccionTxt, telefonoTxt, telefonoEmergenciaTxt, nitTxt, dpiTxt,
                afiliacionIgssTxt, codigoOcupacionTxt, condicionLaboralTxt, cuentaBancariaTxt,
                correlativoTxt, fechaIngresoDt, fechaEgresoDt, nombreCompletoTxt,
                vacacionesDiasDerechoTxt, vacacionesDiasGozadosTxt
        };
        for (Component campo : camposAnchoCompleto) {
            campo.setWidth("100%");
        }

        idEmpleadoTxt.setRequired(true);
        cargoCbx.setRequired(true);
        primerNombreTxt.setRequired(true);
        primerApellidoTxt.setRequired(true);
        direccionTxt.setRequired(true);
        telefonoTxt.setRequired(true);
        telefonoEmergenciaTxt.setRequired(true);
        dpiTxt.setRequired(true);
        fechaIngresoDt.setRequired(true);

        idEmpleadoTxt.setInputPrompt("Código único");
        telefonoTxt.setInputPrompt("Ej. 5555-5555");
        telefonoEmergenciaTxt.setInputPrompt("Ej. 5555-5555");
        dpiTxt.setInputPrompt("13 dígitos");
        nombreCompletoTxt.setDescription("Nombre que se reportará al IGSS");
    }

    private void actualizarEstadoAcciones(boolean empleadoSeleccionado) {
        salarioBtn.setEnabled(empleadoSeleccionado);
        vacacionesBtn.setEnabled(empleadoSeleccionado);
        deleteBtn.setEnabled(empleadoSeleccionado && !Boolean.TRUE.equals(inhabilitadoChb.getValue()));
    }

    private void completarNombre() {
        nombreCompletoTxt.setValue(buildNombreCompleto(primerNombreTxt.getValue(), segundoNombreTxt.getValue(),
                primerApellidoTxt.getValue(), segundoApellidoTxt.getValue(), apellidoCasadaTxt.getValue()));
    }

    private void clearForms() {
        idEmpleadoTxt.setReadOnly(false);
        idEmpleadoTxt.setValue("");
        cargoCbx.setValue("");
        departamentoCbx.setValue("");
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
        aplicarEstiloInhabilitado(false);
        correlativoTxt.setValue("0");
        fechaEgresoDt.setValue(null);
        fechaIngresoDt.setValue(new Date());
        aplicaIndemnizacion.setValue(false);
        vacacionesDiasDerechoTxt.setValue(15d);
        vacacionesDiasGozadosTxt.setValue(0d);
        cuentaBancariaTxt.setValue("");
        idLiquidacion.setValue("0");
        egresoDateMemory = null;
        idEmpleadoOriginal = null;

    }

    private void mostrarDatos(String idProveedor) {

        clearForms();

        String queryString = "SELECT * FROM proveedor_empresa WHERE IdProveedor = ? AND IdEmpresa = ? AND EsPlanilla = 1";

        try (PreparedStatement statement = ((SopdiUI) UI.getCurrent()).databaseProvider
                .getCurrentConnection().prepareStatement(queryString)) {
            statement.setString(1, idProveedor);
            statement.setString(2, empresaId);
            try (ResultSet records = statement.executeQuery()) {
                if(records.next()) {
                idEmpleadoOriginal = records.getString("IDProveedor");
                idEmpleadoTxt.setValue(idEmpleadoOriginal);
                idEmpleadoTxt.setReadOnly(true);
                cargoCbx.setValue(valueOrEmpty(records.getString("Cargo")));
                try { departamentoCbx.setValue(valueOrEmpty(records.getString("Departamento"))); } catch (Exception ignored) {}
                generoCbx.setValue(valueOrDefault(records.getString("Genero"), "Masculino"));
                primerNombreTxt.setValue(valueOrEmpty(records.getString("PrimerNombre")));
                segundoNombreTxt.setValue(valueOrEmpty(records.getString("SegundoNombre")));
                primerApellidoTxt.setValue(valueOrEmpty(records.getString("PrimerApellido")));
                segundoApellidoTxt.setValue(valueOrEmpty(records.getString("SegundoApellido")));
                apellidoCasadaTxt.setValue(valueOrEmpty(records.getString("ApellidoCasada")));
                direccionTxt.setValue(valueOrEmpty(records.getString("Direccion")));
                nacionalidadTxt.setValue(valueOrEmpty(records.getString("nacionalidad")));
                telefonoTxt.setValue(valueOrEmpty(records.getString("Telefono")));
                telefonoEmergenciaTxt.setValue(valueOrEmpty(records.getString("TelefonoEmergencia")));
                nitTxt.setValue(valueOrEmpty(records.getString("Nit")));
                dpiTxt.setValue(valueOrEmpty(records.getString("Dpi")));
                afiliacionIgssTxt.setValue(valueOrEmpty(records.getString("AfiliacionIgss")));
                codigoOcupacionTxt.setValue(valueOrEmpty(records.getString("CodigoOcupacion")));
                condicionLaboralTxt.setValue(valueOrEmpty(records.getString("CondicionLaboral")));
                aplicaAnticipoChb.setValue(records.getBoolean("AplicaAnticipoSalario"));
                obraAsignadaChb.setValue(records.getBoolean("AsignadoObra"));
                esLiquidador.setValue(records.getBoolean("EsLiquidador"));
                cuentaBancariaTxt.setValue(valueOrEmpty(records.getString("BancoCuenta")));
                fechaIngresoDt.setValue(records.getDate("FechaIngreso"));
                if(records.getObject("FechaEgreso") != null) {
                    fechaEgresoDt.setValue(records.getDate("FechaEgreso"));
                    egresoDateMemory = records.getDate("FechaEgreso");
                }
                else {
                    fechaEgresoDt.setValue(null);
                    egresoDateMemory = null;
                }
                correlativoTxt.setValue(valueOrDefault(records.getString("IdCorrFinal"), "0"));
                inhabilitadoChb.setValue(records.getBoolean("Inhabilitado"));
                aplicarEstiloInhabilitado(inhabilitadoChb.getValue());
                aplicaIndemnizacion.setValue(records.getBoolean("AplicaIndemnizacion"));

                idLiquidacion.setValue(valueOrDefault(records.getString("IdPlanillaLiquidacion"), "0"));

                vacacionesDiasDerechoTxt.setValue(records.getDouble("DiasVacacionesDerecho"));
                vacacionesDiasGozadosTxt.setValue(records.getDouble("DiasVacacionesGozados"));

                formularioEstadoLbl.setValue("Editando empleado " + idEmpleadoOriginal);
                formularioEstadoLbl.addStyleName("empleado-estado-edicion");

                }
            }
        } catch (Exception ex1) {
            Logger.getLogger(EmpleadoView.class.getName()).log(Level.SEVERE, "Error al cargar empleado", ex1);
            Notification.show("No fue posible cargar el empleado.", Notification.Type.ERROR_MESSAGE);
        }
    }

    public boolean exportToExcel(Table tableToExport) {
        ExcelExport excelExport;

        excelExport = new ExcelExport(tableToExport);
        excelExport.excludeCollapsedColumns();
        excelExport.setExportFileName(empresaId+"_CATALOGO_EMPLEADOS.xls");

        new Utileria();
        String mainTitle = empresaNombre + " CATALOGO DE EMPLEADOS AL: " + Utileria.getFechaYYYYMMDD_1(new Date());

        excelExport.setReportTitle(mainTitle);

        excelExport.export();

        return true;

    }

    private void asegurarColumnasEmpleado(Connection conn) {
        String[][] columnas = {
                {"Cargo",        "VARCHAR(150) NULL DEFAULT NULL"},
                {"Departamento", "VARCHAR(150) NULL DEFAULT NULL"},
        };
        for (String[] col : columnas) {
            try {
                java.sql.Statement st = conn.createStatement();
                java.sql.ResultSet rs = st.executeQuery(
                        "SELECT COUNT(*) FROM information_schema.COLUMNS"
                        + " WHERE TABLE_SCHEMA = DATABASE()"
                        + " AND TABLE_NAME = 'proveedor_empresa'"
                        + " AND COLUMN_NAME = '" + col[0] + "'");
                boolean existe = rs.next() && rs.getInt(1) > 0;
                rs.close();
                if (!existe) {
                    st.executeUpdate("ALTER TABLE proveedor_empresa ADD COLUMN " + col[0] + " " + col[1]);
                }
            } catch (Exception ex) {
                Logger.getLogger(EmpleadoView.class.getName()).log(Level.WARNING,
                        "No se pudo asegurar columna proveedor_empresa." + col[0], ex);
            }
        }
    }

    private void guardarDatosSeguro() {
        if (!validarFormulario()) {
            return;
        }

        Connection connection = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection();
        asegurarColumnasEmpleado(connection);
        boolean autoCommitOriginal = true;
        try {
            autoCommitOriginal = connection.getAutoCommit();
            connection.setAutoCommit(false);

            if (esNuevo) {
                insertarProveedor(connection);
                insertarEmpleado(connection);
            } else {
                actualizarEmpleado(connection);
            }
            actualizarAsistencia(connection);
            connection.commit();

            idEmpleadoOriginal = idEmpleadoTxt.getValue().trim();
            egresoDateMemory = fechaEgresoDt.getValue();
            esNuevo = false;
            Notification.show("OPERACIÓN EXITOSA!", Notification.Type.HUMANIZED_MESSAGE);
            Logger.getLogger(EmpleadoView.class.getName()).log(Level.INFO,
                    "El usuario {0} actualizó el empleado {1}",
                    new Object[]{((SopdiUI) mainUI).sessionInformation.getStrUserName(), idEmpleadoOriginal});
            fillGridEmpleados();
        } catch (Exception ex) {
            try {
                connection.rollback();
            } catch (SQLException rollbackError) {
                ex.addSuppressed(rollbackError);
            }
            Logger.getLogger(EmpleadoView.class.getName()).log(Level.SEVERE, "Error al guardar empleado", ex);
            Notification.show("No fue posible guardar el empleado. Verifique que el ID no esté duplicado y que los datos sean válidos.",
                    Notification.Type.ERROR_MESSAGE);
        } finally {
            try {
                connection.setAutoCommit(autoCommitOriginal);
            } catch (SQLException ex) {
                Logger.getLogger(EmpleadoView.class.getName()).log(Level.SEVERE, "No fue posible restaurar la conexión", ex);
            }
        }
    }

    private boolean validarFormulario() {
        if (isBlank(idEmpleadoTxt.getValue())) {
            return mostrarErrorValidacion("Error, falta el Id del Empleado!", idEmpleadoTxt);
        }
        if (isBlank(primerNombreTxt.getValue())) {
            return mostrarErrorValidacion("Error, falta el nombre del empleado!", primerNombreTxt);
        }
        if (isBlank(primerApellidoTxt.getValue())) {
            return mostrarErrorValidacion("Error, falta el apellido del empleado!", primerApellidoTxt);
        }
        if (isBlank(nombreCompletoTxt.getValue())) {
            return mostrarErrorValidacion("Error, falta el nombre completo del empleado!", primerNombreTxt);
        }
        if (cargoCbx.getValue() == null || isBlank(String.valueOf(cargoCbx.getValue()))) {
            return mostrarErrorValidacion("Error, falta el cargo/puesto/plaza del empleado!", cargoCbx);
        }
        if (isBlank(direccionTxt.getValue())) {
            return mostrarErrorValidacion("Error, falta la dirección del empleado!", direccionTxt);
        }
        if (isBlank(dpiTxt.getValue())) {
            return mostrarErrorValidacion("Error, falta el DPI del empleado!", dpiTxt);
        }
        if (fechaIngresoDt.getValue() == null) {
            return mostrarErrorValidacion("Error, falta la fecha de ingreso del empleado!", fechaIngresoDt);
        }
        if (isBlank(telefonoTxt.getValue())) {
            return mostrarErrorValidacion("Error, falta el teléfono del empleado!", telefonoTxt);
        }
        if (isBlank(telefonoEmergenciaTxt.getValue())) {
            return mostrarErrorValidacion("Error, falta el teléfono de emergencia del empleado!", telefonoEmergenciaTxt);
        }
        if (fechaEgresoDt.getValue() == null && aplicaIndemnizacion.getValue()) {
            aplicaIndemnizacion.setValue(false);
            Notification.show("Aplica indemnización solamente cuando el empleado tiene fecha de egreso.",
                    Notification.Type.WARNING_MESSAGE);
            return false;
        }
        if (fechaEgresoDt.getValue() != null && fechaEgresoDt.getValue().before(fechaIngresoDt.getValue())) {
            return mostrarErrorValidacion("La fecha de egreso no puede ser anterior a la fecha de ingreso.", fechaEgresoDt);
        }
        if (vacacionesDiasDerechoTxt.getValue() == null || vacacionesDiasGozadosTxt.getValue() == null) {
            Notification.show("Los días de vacaciones son obligatorios.", Notification.Type.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private boolean mostrarErrorValidacion(String mensaje, AbstractField<?> campo) {
        Notification.show(mensaje, Notification.Type.ERROR_MESSAGE);
        campo.focus();
        return false;
    }

    private void insertarProveedor(Connection connection) throws SQLException {
        String sql = "INSERT INTO proveedor (Codigo, CodigoAnterior, Nit, TipoPersona, Regimen, "
                + "Genero, Nombre, PrimerNombre, SegundoNombre, PrimerApellido, SegundoApellido, ApellidoCasada, "
                + "Nacionalidad, Dpi, Direccion, Telefono, TelefonoEmergencia, Email, "
                + "EsProveedor, EsCliente, EsBanco, EsAgenteRetenedorISR, EsAgenteRetenedorIVA, "
                + "EsInstitucionFiscal, EsInstitucionSeguroSocial, EsSujetoRetencionDefinitivaISR, Inhabilitado) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int index = 1;
            statement.setString(index++, idEmpleadoTxt.getValue().trim());       // Codigo
            statement.setString(index++, "");                                     // CodigoAnterior
            statement.setString(index++, nitTxt.getValue().trim());               // Nit
            statement.setString(index++, "INDIVIDUAL");                           // TipoPersona
            statement.setString(index++, "Opcional Simplificado");               // Regimen
            statement.setString(index++, String.valueOf(generoCbx.getValue()));   // Genero
            statement.setString(index++, nombreCompletoTxt.getValue().trim());    // Nombre
            statement.setString(index++, primerNombreTxt.getValue().trim());      // PrimerNombre
            statement.setString(index++, segundoNombreTxt.getValue().trim());     // SegundoNombre
            statement.setString(index++, primerApellidoTxt.getValue().trim());    // PrimerApellido
            statement.setString(index++, segundoApellidoTxt.getValue().trim());   // SegundoApellido
            statement.setString(index++, apellidoCasadaTxt.getValue().trim());    // ApellidoCasada
            statement.setString(index++, nacionalidadTxt.getValue().trim());      // Nacionalidad
            statement.setString(index++, dpiTxt.getValue().trim());               // Dpi
            statement.setString(index++, direccionTxt.getValue().trim());         // Direccion
            statement.setString(index++, telefonoTxt.getValue().trim());          // Telefono
            statement.setString(index++, telefonoEmergenciaTxt.getValue().trim()); // TelefonoEmergencia
            statement.setString(index++, "");                                     // Email
            statement.setBoolean(index++, false);  // EsProveedor
            statement.setBoolean(index++, false);  // EsCliente
            statement.setBoolean(index++, false);  // EsBanco
            statement.setBoolean(index++, false);  // EsAgenteRetenedorISR
            statement.setBoolean(index++, false);  // EsAgenteRetenedorIVA
            statement.setBoolean(index++, false);  // EsInstitucionFiscal
            statement.setBoolean(index++, false);  // EsInstitucionSeguroSocial
            statement.setBoolean(index++, false);  // EsSujetoRetencionDefinitivaISR
            statement.setBoolean(index,   false);  // Inhabilitado
            statement.executeUpdate();
        }
    }

    private void insertarEmpleado(Connection connection) throws SQLException {
        String sql = "INSERT INTO proveedor_empresa (IDProveedor, IdEmpresa, Nombre, NIT, DPI, Regimen, "
                + "EsPlanilla, Cargo, Departamento, PrimerNombre, SegundoNombre, PrimerApellido, SegundoApellido, ApellidoCasada, "
                + "Banco, BancoCuenta, Nacionalidad, Direccion, Telefono, TelefonoEmergencia, Genero, TituloAcademico, "
                + "AfiliacionIgss, FechaIngreso, FechaEgreso, CodigoOcupacion, CondicionLaboral, AplicaAnticipoSalario, "
                + "AsignadoObra, IdCorrFinal, AplicaIndemnizacion, DiasVacacionesDerecho, DiasVacacionesGozados, "
                + "EsLiquidador, Inhabilitado) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int index = 1;
            statement.setString(index++, idEmpleadoTxt.getValue().trim());
            statement.setString(index++, empresaId);
            statement.setString(index++, nombreCompletoTxt.getValue().trim());
            statement.setString(index++, nitTxt.getValue().trim());
            statement.setString(index++, dpiTxt.getValue().trim());
            statement.setString(index++, "NORMAL");
            statement.setBoolean(index++, true);
            statement.setString(index++, String.valueOf(cargoCbx.getValue()));
            statement.setString(index++, String.valueOf(departamentoCbx.getValue()));
            statement.setString(index++, primerNombreTxt.getValue().trim());
            statement.setString(index++, segundoNombreTxt.getValue().trim());
            statement.setString(index++, primerApellidoTxt.getValue().trim());
            statement.setString(index++, segundoApellidoTxt.getValue().trim());
            statement.setString(index++, apellidoCasadaTxt.getValue().trim());
            statement.setString(index++, "Banco Industrial");
            statement.setString(index++, cuentaBancariaTxt.getValue().trim());
            statement.setString(index++, nacionalidadTxt.getValue().trim());
            statement.setString(index++, direccionTxt.getValue().trim());
            statement.setString(index++, telefonoTxt.getValue().trim());
            statement.setString(index++, telefonoEmergenciaTxt.getValue().trim());
            statement.setString(index++, String.valueOf(generoCbx.getValue()));
            statement.setString(index++, "");
            statement.setString(index++, afiliacionIgssTxt.getValue().trim());
            statement.setDate(index++, toSqlDate(fechaIngresoDt.getValue()));
            setNullableDate(statement, index++, fechaEgresoDt.getValue());
            statement.setString(index++, codigoOcupacionTxt.getValue().trim());
            statement.setString(index++, condicionLaboralTxt.getValue().trim());
            statement.setBoolean(index++, aplicaAnticipoChb.getValue());
            statement.setBoolean(index++, obraAsignadaChb.getValue());
            statement.setString(index++, valueOrDefault(correlativoTxt.getValue(), "0").trim());
            statement.setBoolean(index++, aplicaIndemnizacion.getValue());
            statement.setDouble(index++, Double.parseDouble(vacacionesDiasDerechoTxt.getValue()));
            statement.setDouble(index++, Double.parseDouble(vacacionesDiasGozadosTxt.getValue()));
            statement.setBoolean(index++, esLiquidador.getValue());
            statement.setBoolean(index, inhabilitadoChb.getValue());
            statement.executeUpdate();
        }
    }

    private void actualizarEmpleado(Connection connection) throws SQLException {
        if (idEmpleadoOriginal == null) {
            throw new SQLException("No se conoce el ID original del empleado");
        }
        String sql = "UPDATE proveedor_empresa SET IDProveedor=?, Nombre=?, NIT=?, DPI=?, PrimerNombre=?, "
                + "SegundoNombre=?, PrimerApellido=?, SegundoApellido=?, ApellidoCasada=?, BancoCuenta=?, "
                + "Nacionalidad=?, Direccion=?, Telefono=?, TelefonoEmergencia=?, Genero=?, AfiliacionIgss=?, "
                + "FechaIngreso=?, FechaEgreso=?, CodigoOcupacion=?, CondicionLaboral=?, AplicaAnticipoSalario=?, "
                + "AsignadoObra=?, EsLiquidador=?, IdCorrFinal=?, Inhabilitado=?, Cargo=?, Departamento=?, AplicaIndemnizacion=?, "
                + "DiasVacacionesDerecho=?, DiasVacacionesGozados=? WHERE IdProveedor=? AND IdEmpresa=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int index = 1;
            statement.setString(index++, idEmpleadoTxt.getValue().trim());
            statement.setString(index++, nombreCompletoTxt.getValue().trim());
            statement.setString(index++, nitTxt.getValue().trim());
            statement.setString(index++, dpiTxt.getValue().trim());
            statement.setString(index++, primerNombreTxt.getValue().trim());
            statement.setString(index++, segundoNombreTxt.getValue().trim());
            statement.setString(index++, primerApellidoTxt.getValue().trim());
            statement.setString(index++, segundoApellidoTxt.getValue().trim());
            statement.setString(index++, apellidoCasadaTxt.getValue().trim());
            statement.setString(index++, cuentaBancariaTxt.getValue().trim());
            statement.setString(index++, nacionalidadTxt.getValue().trim());
            statement.setString(index++, direccionTxt.getValue().trim());
            statement.setString(index++, telefonoTxt.getValue().trim());
            statement.setString(index++, telefonoEmergenciaTxt.getValue().trim());
            statement.setString(index++, String.valueOf(generoCbx.getValue()));
            statement.setString(index++, afiliacionIgssTxt.getValue().trim());
            statement.setDate(index++, toSqlDate(fechaIngresoDt.getValue()));
            setNullableDate(statement, index++, fechaEgresoDt.getValue());
            statement.setString(index++, codigoOcupacionTxt.getValue().trim());
            statement.setString(index++, condicionLaboralTxt.getValue().trim());
            statement.setBoolean(index++, aplicaAnticipoChb.getValue());
            statement.setBoolean(index++, obraAsignadaChb.getValue());
            statement.setBoolean(index++, esLiquidador.getValue());
            statement.setString(index++, valueOrDefault(correlativoTxt.getValue(), "0").trim());
            statement.setBoolean(index++, inhabilitadoChb.getValue());
            statement.setString(index++, String.valueOf(cargoCbx.getValue()));
            statement.setString(index++, String.valueOf(departamentoCbx.getValue()));
            statement.setBoolean(index++, aplicaIndemnizacion.getValue());
            statement.setDouble(index++, Double.parseDouble(vacacionesDiasDerechoTxt.getValue()));
            statement.setDouble(index++, Double.parseDouble(vacacionesDiasGozadosTxt.getValue()));
            statement.setString(index++, idEmpleadoOriginal);
            statement.setString(index, empresaId);
            if (statement.executeUpdate() != 1) {
                throw new SQLException("El empleado no existe o fue modificado por otro usuario");
            }
        }
    }

    private void actualizarAsistencia(Connection connection) throws SQLException {
        Date fechaEgresoNueva = fechaEgresoDt.getValue();
        if (Objects.equals(fechaEgresoNueva, egresoDateMemory)) {
            return;
        }
        if (egresoDateMemory != null) {
            actualizarEstadoAsistencia(connection, egresoDateMemory, "PRESENTE", "", false, false);
        }
        if (fechaEgresoNueva != null) {
            actualizarEstadoAsistencia(connection, fechaEgresoNueva, "DE BAJA", "Retiro de labores", true, true);
        }
    }

    private void actualizarEstadoAsistencia(Connection connection, Date fecha, String estatus, String razon,
                                             boolean descuento, boolean definitiva) throws SQLException {
        String sql = "UPDATE empleado_asistencia SET Estatus=?, Razon=?, EsDescuento=?, EsDefinitiva=? "
                + "WHERE IdEmpleado=? AND Fecha=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, estatus);
            statement.setString(2, razon);
            statement.setBoolean(3, descuento);
            statement.setBoolean(4, definitiva);
            statement.setString(5, valueOrDefault(idEmpleadoOriginal, idEmpleadoTxt.getValue().trim()));
            statement.setDate(6, toSqlDate(fecha));
            statement.executeUpdate();
        }
    }

    private static java.sql.Date toSqlDate(Date value) {
        return new java.sql.Date(value.getTime());
    }

    private static void setNullableDate(PreparedStatement statement, int index, Date value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.DATE);
        } else {
            statement.setDate(index, toSqlDate(value));
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String valueOrDefault(String value, String defaultValue) {
        return isBlank(value) ? defaultValue : value;
    }

    private static String buildNombreCompleto(String... partes) {
        StringBuilder nombre = new StringBuilder();
        for (String parte : partes) {
            if (!isBlank(parte)) {
                if (nombre.length() > 0) {
                    nombre.append(' ');
                }
                nombre.append(parte.trim());
            }
        }
        return nombre.toString();
    }

    @Deprecated
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

        String correlativoValor = (correlativoTxt.getValue() == null || correlativoTxt.getValue().trim().isEmpty())
                ? "0" : correlativoTxt.getValue().trim();

        String queryString = "";

        if (esNuevo ) {
            queryString = "INSERT INTO proveedor_empresa (IDProveedor, IdEmpresa, ";
            queryString += " Nombre, NIT, DPI,Regimen, EsPlanilla, Cargo, ";
            queryString += " PrimerNombre, SegundoNombre, PrimerApellido, SegundoApellido, ApellidoCasada, ";
            queryString += " Banco, BancoCuenta, Nacionalidad, Direccion, Telefono, TelefonoEmergencia, Genero, TituloAcademico,  ";
            queryString += " AfiliacionIgss, FechaIngreso, FechaEgreso, CodigoOcupacion, CondicionLaboral,";
            queryString += " AplicaAnticipoSalario, AsignadoObra, IdCorrFinal, AplicaIndemnizacion, DiasVacacionesDerecho, DiasVacacionesGozados";
            queryString += ")";
            queryString += " VALUES (";
            queryString += idEmpleadoTxt.getValue();
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
            queryString += ",'" + nombreCompletoTxt.getValue() + "'";
            queryString += ",'" + nitTxt.getValue() + "'";
            queryString += ",'" + dpiTxt.getValue() + "'";
            queryString += ",'NORMAL'";
            queryString += ", 1"; // ESPLANILLA
            queryString += ", '" + cargoCbx.getValue() + "'";
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
            queryString += ","  + correlativoValor ;
            queryString += ","  + (aplicaIndemnizacion.getValue() ? "1" : "0");
            queryString += ","  + vacacionesDiasDerechoTxt.getValue();
            queryString += ","  + vacacionesDiasGozadosTxt.getValue();
            queryString += ")";
        } else {
            queryString = "UPDATE proveedor_empresa SET ";
            queryString += " IDProveedor = " + idEmpleadoTxt.getValue();
            queryString += ",NIT = '" + nitTxt.getValue() + "'";
            queryString += ",DPI = '" + dpiTxt.getValue() + "'";
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
            queryString += ",IdCorrFinal = "  + correlativoValor ;
            queryString += ",Inhabilitado = "  + (inhabilitadoChb.getValue() ? "1" : "0");
            queryString += ",Cargo = '" + cargoCbx.getValue() + "'";
            queryString += ",AplicaIndemnizacion = " + (aplicaIndemnizacion.getValue() ? "1" : "0");
            queryString += ",DiasVacacionesDerecho = "  + vacacionesDiasDerechoTxt.getValue();
            queryString += ",DiasVacacionesGozados = "  + vacacionesDiasGozadosTxt.getValue();
            queryString += " WHERE IdProveedor = " + idEmpleadoTxt.getValue();
            queryString += " AND IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();

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
        ((SopdiUI) UI.getCurrent()).lblEmpresaYFormulario.setValue(empresaId + " " + empresaNombre + " EMPLEADOS");
        Page.getCurrent().setTitle("Sopdi - EMPLEADOS");
    }

    private void deleteEmpleado() {
        Object selectedRow = empleadosGrid.getSelectedRow();
        if (selectedRow == null) {
            Notification.show("Seleccione un empleado.", Notification.Type.WARNING_MESSAGE);
            return;
        }
        String idEmpleado = String.valueOf(
                empleadosContainer.getContainerProperty(selectedRow, "id").getValue());
        String sql = "UPDATE proveedor_empresa SET Inhabilitado = 1 "
                + "WHERE IdProveedor = ? AND IdEmpresa = ? AND EsPlanilla = 1";
        try (PreparedStatement statement = ((SopdiUI) mainUI).databaseProvider
                .getCurrentConnection().prepareStatement(sql)) {
            statement.setString(1, idEmpleado);
            statement.setString(2, empresaId);
            if (statement.executeUpdate() == 1) {
                Notification.show("Empleado inhabilitado.", Notification.Type.TRAY_NOTIFICATION);
                clearForms();
                esNuevo = true;
                fillGridEmpleados();
            } else {
                Notification.show("El empleado ya no existe o fue modificado.", Notification.Type.WARNING_MESSAGE);
            }
        } catch (SQLException ex) {
            Logger.getLogger(EmpleadoView.class.getName()).log(Level.SEVERE, "Error al inhabilitar empleado", ex);
            Notification.show("No fue posible inhabilitar el empleado.", Notification.Type.ERROR_MESSAGE);
        }
    }
}
