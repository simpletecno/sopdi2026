package com.simpletecno.sopdi.operativo;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.addon.tableexport.DefaultTableHolder;
import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.addon.tableexport.TableHolder;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.SelectionEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Responsive;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.ui.NumberField;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author user
 */
@SuppressWarnings("ALL")
public class InspectionTaskBudgetWindow extends Window {
    public static final String ID_BUDGET = "Id";
    public static final String CUENTA = "Cuenta";
    public static final String DESCRIPCION = "Descripción";
    public static final String UNIDAD = "Unidad";
    public static final String PROVEEDOR = "Proveedor";
    public static final String CANTIDAD = "Cantidad";
    public static final String PRECIO = "Precio";
    public static final String TOTAL = "Total";
    public static final String MONEDA = "Moneda";

    public IndexedContainer budgetContainer = new IndexedContainer();
    Grid budgetGrid;

    Button nuevoBudgetBtn = new Button("Nuevo");
    Button editarBudgetBtn = new Button("Editar");
    Button eliminarBudgetBtn = new Button("Eliminar");
    Button guardarBudgetBtn = new Button("Guardar");
    Button cancelarBudgetBtn = new Button("Cancelar");

    VerticalLayout mainLayout = new VerticalLayout();
    HorizontalLayout botonesLayout = new HorizontalLayout();

    ComboBox cuentaCentroCostoCbx = new ComboBox("Cuenta CC :");
    TextField descripcionTxt = new TextField("Descripción :");
    TextField unidadTxt = new TextField("Medida :");
    ComboBox proveedorCbx = new ComboBox("Proveedor :");
    NumberField cantidadTxt = new NumberField("Cantidad :");
    NumberField precioTxt = new NumberField("Precio :");
    NumberField totalTxt = new NumberField("Total :");
    ComboBox monedaCbx = new ComboBox("Moneda :");

    UI mainUI;
    Statement stQuery, stQuery1;
    ResultSet rsRecords, rsRecords1;
    String queryString;
    boolean esnuevoBudget;

    Map medidas = new HashMap();
    Map precios = new HashMap();

    String tareaId;
    String codigoTarea;
    String descripcionTarea;
    String autorizadoTipo;

    String codigoPresupuesto = "";

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public InspectionTaskBudgetWindow(
            String tareaId,
            String codigoTarea,
            String descripcionTarea,
            String autorizadoTipo
    ) {
        this.tareaId = tareaId;
        this.codigoTarea = codigoTarea;
        this.descripcionTarea = descripcionTarea;
        this.autorizadoTipo = autorizadoTipo;

        this.mainUI = UI.getCurrent();

        this.setWidth("95%");
        this.setHeightUndefined();
        this.center();
        this.setResizable(true);
//        this.setPosition(50,200);
        
        mainLayout.setSpacing(false);
        mainLayout.setMargin(false);
        mainLayout.setWidth("100%");
        mainLayout.addStyleName("rcorners3");

        Responsive.makeResponsive(this);

        setContent(mainLayout);

        Label titleLbl = new Label("<h4 style=\"color:RoyalBlue;\"" + "/>Presupuesto de tarea : <br><b>" + codigoTarea + "</b> " + descripcionTarea + "</br></h4>");
        titleLbl.addStyleName(ValoTheme.LABEL_H4);
        titleLbl.setResponsive(true);
        titleLbl.setContentMode(ContentMode.HTML);

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setWidth("100%");

        titleLayout.addComponents(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.TOP_LEFT);

        mainLayout.addComponent(titleLayout);
        mainLayout.setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        createBudgetContainer();

        fillBudgetGrid();
    }

    public void createBudgetContainer() {
        HorizontalLayout budgetLayout = new HorizontalLayout();
        budgetLayout.setWidth("100%");
        budgetLayout.addStyleName("rcorners3");
        budgetLayout.setSpacing(true);

        botonesLayout.setSpacing(true);
        botonesLayout.setWidth("100%");
        botonesLayout.setVisible(true);

        HorizontalLayout formBudgetLayout = new HorizontalLayout();
        formBudgetLayout.setWidth("100%");
        formBudgetLayout.addStyleName("rcorners3");
        formBudgetLayout.setSpacing(true);

        FormLayout formLayout = new FormLayout();
        formLayout.setWidth("100%");
        formLayout.setHeightUndefined();
        formLayout.setVisible(false);
        formBudgetLayout.addComponent(formLayout);
        formBudgetLayout.setComponentAlignment(formLayout, Alignment.MIDDLE_CENTER);

        mainLayout.addComponents(budgetLayout, botonesLayout, formBudgetLayout);
        mainLayout.setComponentAlignment(budgetLayout, Alignment.TOP_CENTER);
        mainLayout.setComponentAlignment(botonesLayout, Alignment.TOP_CENTER);
        mainLayout.setComponentAlignment(formBudgetLayout, Alignment.BOTTOM_CENTER);

        budgetContainer.addContainerProperty(ID_BUDGET, String.class, "");
        budgetContainer.addContainerProperty(CUENTA, String.class, "");
        budgetContainer.addContainerProperty(DESCRIPCION, String.class, "");
        budgetContainer.addContainerProperty(UNIDAD, String.class, "");
        budgetContainer.addContainerProperty(PROVEEDOR, String.class, "");
        budgetContainer.addContainerProperty(CANTIDAD, Double.class, 0.00);
        budgetContainer.addContainerProperty(PRECIO, Double.class, 0.00);
        budgetContainer.addContainerProperty(TOTAL, Double.class, 0.00);
        budgetContainer.addContainerProperty(MONEDA, String.class, "");

        budgetGrid = new Grid("PRESUPUESTO", budgetContainer);
        budgetGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        budgetGrid.setHeightMode(HeightMode.ROW);
        budgetGrid.setHeightByRows(10);
        budgetGrid.setSizeFull();

        budgetGrid.getColumn(ID_BUDGET).setExpandRatio(1).setHidden(true).setHidable(true);

        budgetGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {

            if (CANTIDAD.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else if (PRECIO.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (TOTAL.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }
        });

        budgetGrid.addSelectionListener(new SelectionEvent.SelectionListener() {
            @Override
            public void select(SelectionEvent event
            ) {
                if (budgetGrid.getSelectedRow() != null) {

                    esnuevoBudget = false;
                    botonesLayout.setVisible(true);

                    editarBudgetBtn.setEnabled(true);
                    eliminarBudgetBtn.setEnabled(true);

                    cuentaCentroCostoCbx.select(null);
                    proveedorCbx.select(null);
                    descripcionTxt.setValue("");
                    cantidadTxt.setValue(0.00);
                    precioTxt.setValue(0.00);
                    totalTxt.setValue(0.00);
                }
            }
        });

        budgetLayout.addComponent(budgetGrid);

        nuevoBudgetBtn.setIcon(FontAwesome.PLUS);
        nuevoBudgetBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
        nuevoBudgetBtn.setDescription("Nuevo rubro de presupuesto.");
        nuevoBudgetBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                setPosition(50,10);
                descripcionTxt.setValue("");
                cantidadTxt.setValue(0.00);
                precioTxt.setValue(0.00);
                totalTxt.setValue(0.00);
                formLayout.setVisible(true);
                esnuevoBudget = true;
                botonesLayout.setVisible(false);
            }
        });

        editarBudgetBtn.setIcon(FontAwesome.EDIT);
        editarBudgetBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
        editarBudgetBtn.setDescription("Editar planilla.");
        editarBudgetBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (budgetGrid.getSelectedRow() == null) {
                    Notification.show("Por favor seleccione una planilla para editar.", Notification.Type.HUMANIZED_MESSAGE);
                    return;
                }
                setPosition(50,10);
                formLayout.setVisible(true);
                esnuevoBudget = false;
                botonesLayout.setVisible(false);

                try {
                    stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

                    queryString = "SELECT Presu.*, CCC.CodigoCuentaCentroCosto, CCC.UnidadMedida, CCC.Descripcion, Prov.Nombre ProvNombre ";
                    queryString += " FROM visita_inspeccion_tarea_presupuesto Presu";
                    queryString += " INNER JOIN centro_costo_cuenta CCC ON CCC.IdCuentaCentroCosto = Presu.IdCuentaCentroCosto";
                    queryString += " INNER JOIN proveedor Prov ON Prov.IdProveedor = Presu.IdProveedor";
                    queryString += " WHERE IdVisitaInspeccionTareaPresupuesto  = " + budgetContainer.getContainerProperty(budgetGrid.getSelectedRow(), ID_BUDGET).getValue();

                    rsRecords = stQuery.executeQuery(queryString);

                    rsRecords.next();

                    cuentaCentroCostoCbx.setData(rsRecords.getString("IdVisitaInspeccionTareaPresupuesto"));
                    cuentaCentroCostoCbx.setValue(rsRecords.getString("IdCuentaCentroCosto"));
                    descripcionTxt.setValue(rsRecords.getString("Descripcion"));
                    cantidadTxt.setValue(rsRecords.getDouble("Cantidad"));
                    unidadTxt.setReadOnly(false);
                    unidadTxt.setValue(rsRecords.getString("UnidadMedida"));
                    unidadTxt.setReadOnly(true);
                    precioTxt.setValue(rsRecords.getDouble("Precio"));
                    totalTxt.setReadOnly(false);
                    totalTxt.setValue(rsRecords.getDouble("Total"));
                    totalTxt.setReadOnly(true);
                    monedaCbx.select(rsRecords.getString("Moneda"));
                    proveedorCbx.select(rsRecords.getString("IdProveedor"));

                } catch (Exception ex) {
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error al listar tabla PRESUPUESTO : " + ex);
                    ex.printStackTrace();
                    Notification.show("ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                }
            }
        });

        eliminarBudgetBtn.setIcon(FontAwesome.TRASH);
        eliminarBudgetBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
        eliminarBudgetBtn.setDescription("Eliminar presupuesto.");
        eliminarBudgetBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (budgetGrid.getSelectedRow() == null) {
                    return;
                }

                ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de eliminar el rubro de presupuesto ?",
                        "SI", "NO", new ConfirmDialog.Listener() {
                            public void onClose(ConfirmDialog dialog) {
                                if (dialog.isConfirmed()) {
//                                    Notification.show("NO DISPONIBLE EN ESTA VERSION...", Notification.Type.HUMANIZED_MESSAGE);
//                                    return;
                                    queryString = "DELETE FROM visita_inspeccion_tarea_presupuesto ";
                                    queryString += " WHERE IdVisitaInspeccionTareaPresupuesto  = " + budgetContainer.getContainerProperty(budgetGrid.getSelectedRow(), ID_BUDGET).getValue();
                                    try {
                                        stQuery.executeUpdate(queryString);
                                        Notification.show("Registro eliminado exitosamente!", Notification.Type.HUMANIZED_MESSAGE);
                                        budgetContainer.removeItem(budgetGrid.getSelectedRow());
                                    } catch (SQLException e) {
                                        Notification.show("Error al eliminar registro!", Notification.Type.ERROR_MESSAGE);
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
            }
        });

        botonesLayout.addComponents(nuevoBudgetBtn, editarBudgetBtn, eliminarBudgetBtn);

//----------------------------------------CAMPOS----------------------------------------

        cuentaCentroCostoCbx.setNullSelectionAllowed(true);
        cuentaCentroCostoCbx.setInvalidAllowed(false);
        cuentaCentroCostoCbx.setNewItemsAllowed(false);
        cuentaCentroCostoCbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaCentroCostoCbx.addStyleName(ValoTheme.COMBOBOX_SMALL);
        cuentaCentroCostoCbx.setWidth("30em");
        cuentaCentroCostoCbx.addValueChangeListener(event -> { // Java 8
            if (cuentaCentroCostoCbx.getValue() != null && !String.valueOf(cuentaCentroCostoCbx.getValue()).equals("0")) {
                unidadTxt.setReadOnly(false);
                unidadTxt.setValue(String.valueOf(medidas.get(String.valueOf(cuentaCentroCostoCbx.getValue()))));
                unidadTxt.setReadOnly(true);
                if (proveedorCbx.getValue() != null && !String.valueOf(proveedorCbx.getValue()).equals("0")) {
                    double precioReal = getSugestedPrice(String.valueOf(cuentaCentroCostoCbx.getValue()), String.valueOf(proveedorCbx.getValue()));  //Double.valueOf(String.valueOf(precios.get(String.valueOf(cuentaCentroCostoCbx.getValue()))));
                    //if (autorizadoTipo.toUpperCase().equals("CLIENTE")) {
                    //    precioTxt.setDoubleValue(precioReal + (precioReal * (((SopdiUI) mainUI).sessionInformation.getDblBudgetCharge()) / 100));
                    // } else {
                    precioTxt.setValue(precioReal);
                    // }
                } else {
                    precioTxt.setValue(0.00);
                }
            } else {
                unidadTxt.setReadOnly(false);
                unidadTxt.setValue("");
                unidadTxt.setReadOnly(true);
                precioTxt.setValue(0.00);
            }
            calculate();
        });

        fillComboCuentaCentroCosto(cuentaCentroCostoCbx);

        descripcionTxt.setWidth("30em");

        proveedorCbx.setNullSelectionAllowed(true);
        proveedorCbx.setInvalidAllowed(false);
        proveedorCbx.setNewItemsAllowed(false);
        proveedorCbx.setFilteringMode(FilteringMode.CONTAINS);
        proveedorCbx.addStyleName(ValoTheme.COMBOBOX_SMALL);
        proveedorCbx.setWidth("30em");
        proveedorCbx.addValueChangeListener(event -> { // Java 8
            if (cuentaCentroCostoCbx.getValue() != null && !String.valueOf(cuentaCentroCostoCbx.getValue()).equals("0")) {
                unidadTxt.setReadOnly(false);
                unidadTxt.setValue(String.valueOf(medidas.get(String.valueOf(cuentaCentroCostoCbx.getValue()))));
                unidadTxt.setReadOnly(true);
                if (proveedorCbx.getValue() != null && !String.valueOf(proveedorCbx.getValue()).equals("0")) {
                    double precioReal = getSugestedPrice(String.valueOf(cuentaCentroCostoCbx.getValue()), String.valueOf(proveedorCbx.getValue()));  //Double.valueOf(String.valueOf(precios.get(String.valueOf(cuentaCentroCostoCbx.getValue()))));
                    //   if (autorizadoTipo.toUpperCase().equals("CLIENTE")) {
                    //       precioTxt.setDoubleValue(precioReal + (precioReal * (((SopdiUI) mainUI).sessionInformation.getDblBudgetCharge()) / 100));
                    //   } else {
                    precioTxt.setValue(precioReal);
                    //   }
                } else {
                    precioTxt.setValue(0.00);
                }
            } else {
                unidadTxt.setReadOnly(false);
                unidadTxt.setValue("");
                unidadTxt.setReadOnly(true);
                precioTxt.setValue(0.00);
            }
            calculate();
        });
        fillComboProveedor(proveedorCbx);

        cantidadTxt.setWidth("15em");
        cantidadTxt.setDecimalPrecision(2);
        cantidadTxt.setDecimalSeparator('.');
//        cantidadTxt.setDoubleValue(0.00d);
        cantidadTxt.setGroupingUsed(true);
        cantidadTxt.setGroupingSeparator(',');
        cantidadTxt.setImmediate(true);
        cantidadTxt.setNegativeAllowed(true);
        cantidadTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);
        cantidadTxt.addStyleName(ValoTheme.TEXTFIELD_SMALL);
        cantidadTxt.addValueChangeListener(event -> { // Java 8
            if (!cantidadTxt.getValue().trim().isEmpty()) {
                calculate();
            }
        });

        unidadTxt.setReadOnly(true);
        unidadTxt.setWidth("15em");

        precioTxt.setWidth("15em");
        precioTxt.setDecimalPrecision(2);
        precioTxt.setDecimalSeparator('.');
//        precioTxt.setDoubleValue(0.00d);
        precioTxt.setGroupingUsed(true);
        precioTxt.setGroupingSeparator(',');
        precioTxt.setImmediate(true);
        precioTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        precioTxt.addStyleName(ValoTheme.TEXTFIELD_SMALL);
        precioTxt.addValueChangeListener(event -> { // Java 8
            if (!cantidadTxt.getValue().trim().isEmpty()) {
                calculate();
            }
        });

        totalTxt.setWidth("15em");
        totalTxt.setDecimalPrecision(2);
        totalTxt.setDecimalSeparator('.');
        totalTxt.setValue(0.00d);
        totalTxt.setGroupingUsed(true);
        totalTxt.setGroupingSeparator(',');
        totalTxt.setImmediate(true);
        totalTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        totalTxt.addStyleName(ValoTheme.TEXTFIELD_SMALL);

        monedaCbx.setInvalidAllowed(false);
        monedaCbx.setNewItemsAllowed(false);
        monedaCbx.setFilteringMode(FilteringMode.CONTAINS);
        monedaCbx.addItem("QUETZALES");
        monedaCbx.addItem("DOLARES");
        monedaCbx.select("DOLARES");
        monedaCbx.addStyleName(ValoTheme.COMBOBOX_SMALL);

        guardarBudgetBtn.setIcon(FontAwesome.SAVE);
        guardarBudgetBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
        guardarBudgetBtn.setDescription("Guardar datos rubro de presupuesto.");
        guardarBudgetBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de guardar los datos de presupuesto ?",
                        "SI", "NO", new ConfirmDialog.Listener() {
                            public void onClose(ConfirmDialog dialog) {
                                if (dialog.isConfirmed()) {

                                    if (cuentaCentroCostoCbx.getValue() == null || String.valueOf(cuentaCentroCostoCbx.getValue()).trim().isEmpty() || String.valueOf(cuentaCentroCostoCbx.getValue()).equals("0")) {
                                        Notification.show("Por favor elija la CUENTA DE CENTRO DE COSTO!", Notification.Type.ERROR_MESSAGE);
                                        cuentaCentroCostoCbx.focus();
                                        return;
                                    }
                                    if (cantidadTxt.getValue().trim().isEmpty() || cantidadTxt.getDoubleValueDoNotThrow() == 0.00) {
                                        Notification.show("Por favor, ingrese la cantidad!", Notification.Type.ERROR_MESSAGE);
                                        cantidadTxt.focus();
                                        return;
                                    }
                                    if (precioTxt.getDoubleValueDoNotThrow() == 0.00) {
                                        Notification.show("Por favor, ingrese el precio!", Notification.Type.ERROR_MESSAGE);
                                        precioTxt.focus();
                                        return;
                                    }
                                    if (proveedorCbx.getValue() == null || String.valueOf(proveedorCbx.getValue()).trim().isEmpty() || String.valueOf(proveedorCbx.getValue()).equals("0")) {
                                        Notification.show("Por favor elija EL PROVEEDOR!", Notification.Type.ERROR_MESSAGE);
                                        proveedorCbx.focus();
                                        return;
                                    }

                                    crearBudget();

                                    center();
                                    formLayout.setVisible(false);

                                }//if dialog is confirmed
                            } // on close
                        }); // confirm dialog
            }
        }); //add listener

        cancelarBudgetBtn.setIcon(FontAwesome.UNDO);
        cancelarBudgetBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
        cancelarBudgetBtn.setDescription("Cancelar planilla.");
        cancelarBudgetBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
//                setPosition(50,200);
                center();
                botonesLayout.setVisible(true);
                formLayout.setVisible(false);
            }
        });

        HorizontalLayout botonesDatosLayout = new HorizontalLayout();
        botonesDatosLayout.setSpacing(true);
        botonesDatosLayout.setWidth("50%");
        botonesDatosLayout.addComponents(guardarBudgetBtn, cancelarBudgetBtn);
        botonesDatosLayout.setComponentAlignment(guardarBudgetBtn, Alignment.BOTTOM_CENTER);
        botonesDatosLayout.setComponentAlignment(cancelarBudgetBtn, Alignment.BOTTOM_CENTER);

        formLayout.addComponents(cuentaCentroCostoCbx, descripcionTxt, proveedorCbx, cantidadTxt, unidadTxt, precioTxt, totalTxt, monedaCbx,botonesDatosLayout);
        formLayout.setComponentAlignment(botonesDatosLayout, Alignment.BOTTOM_LEFT);
    }

    private void crearBudget() {
        String queryString;

        try {
            if (esnuevoBudget) {

                queryString = "SELECT CodigoPresupuesto";
                queryString += " FROM  visita_inspeccion_tarea_presupuesto ";
                queryString += " WHERE IdVisitaInspeccionTarea = " + tareaId;
                queryString += " ORDER BY CodigoPresupuesto DESC";
                queryString += " LIMIT 1";

                stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                rsRecords = stQuery.executeQuery(queryString);

                codigoPresupuesto = codigoTarea + "01";

                if (rsRecords.next()) { // encontrado la ultima tarea...
                    codigoPresupuesto = codigoTarea + String.format("%02d", Integer.valueOf(rsRecords.getString("CodigoPresupuesto").substring(16, 18)) + 1);
                }

                queryString = "INSERT INTO visita_inspeccion_tarea_presupuesto (IdVisitaInspeccionTarea, CodigoPresupuesto, ";
                queryString += " IdCuentaCentroCosto, Descripcion, Cantidad, Precio, Total, Moneda, IdProveedor) ";
                queryString += " VALUES (";
                queryString += "  " + tareaId;
                queryString += ",'" + codigoPresupuesto + "'";
                queryString += ", " + String.valueOf(cuentaCentroCostoCbx.getValue());
                queryString += ", '" + descripcionTxt.getValue() + "'";
                queryString += ", " + cantidadTxt.getDoubleValueDoNotThrow();
                queryString += ", " + precioTxt.getDoubleValueDoNotThrow();
                queryString += ", " + totalTxt.getDoubleValueDoNotThrow();
                queryString += ",'" + String.valueOf(monedaCbx.getValue()) + "'";
                queryString += ", " + String.valueOf(proveedorCbx.getValue());
                queryString += ")";
            } else {
                queryString = "UPDATE visita_inspeccion_tarea_presupuesto SET ";
                queryString += " IdCuentaCentroCosto = " + String.valueOf(cuentaCentroCostoCbx.getValue());
                queryString += ",Descripcion = '" + descripcionTxt.getValue() + "'";
                queryString += ",Cantidad = " + cantidadTxt.getDoubleValueDoNotThrow();
                queryString += ",Precio =  " + precioTxt.getDoubleValueDoNotThrow();
                queryString += ",Total =  " + totalTxt.getDoubleValueDoNotThrow();
                queryString += ",Moneda = '" + String.valueOf(monedaCbx.getValue()) + "'";
                queryString += ",IdProveedor = " + String.valueOf(proveedorCbx.getValue());
                queryString += " WHERE IdVisitaInspeccionTareaPresupuesto  = " + budgetContainer.getContainerProperty(budgetGrid.getSelectedRow(), ID_BUDGET).getValue();
            }

            Logger.getLogger(this.getClass().getName()).log(Level.INFO, queryString);

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            Notification.show("Registro guardado exitosamente!", Notification.Type.HUMANIZED_MESSAGE);

            botonesLayout.setVisible(true);

            fillBudgetGrid();

        } catch (Exception exPlanilla) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error al insertar RUBRO DE PRESUPUESTO: " + exPlanilla.getMessage());
            Notification notif = new Notification("ERROR AL GUARDAR DATOS RUBRO DE PRESUPUESTO!",
                    Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.CHECK);
            notif.show(Page.getCurrent());
            exPlanilla.printStackTrace();
        }
    }

    public void fillBudgetGrid() {
        budgetContainer.removeAllItems();

        esnuevoBudget = false;

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            queryString = "SELECT Presu.*, CCC.CodigoCuentaCentroCosto, CCC.UnidadMedida, CCC.Descripcion, Prov.Nombre ProvNombre ";
            queryString += " FROM visita_inspeccion_tarea_presupuesto Presu";
            queryString += " INNER JOIN centro_costo_cuenta CCC ON CCC.IdCuentaCentroCosto = Presu.IdCuentaCentroCosto";
            queryString += " INNER JOIN proveedor_empresa Prov ON  Prov.IdProveedor = Presu.IdProveedor";
            queryString += " WHERE Presu.IdVisitaInspeccionTarea = " + tareaId;
            queryString += " AND Prov.IdEmpresa = " + empresaId;

            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {

                Object itemId;

                do {
                    itemId = budgetContainer.addItem();
                    budgetContainer.getContainerProperty(itemId, ID_BUDGET).setValue(rsRecords.getString("IdVisitaInspeccionTareaPresupuesto"));
                    budgetContainer.getContainerProperty(itemId, CUENTA).setValue(rsRecords.getString("CodigoCuentaCentroCosto"));
                    budgetContainer.getContainerProperty(itemId, DESCRIPCION).setValue(rsRecords.getString("Descripcion"));
                    budgetContainer.getContainerProperty(itemId, UNIDAD).setValue(rsRecords.getString("UnidadMedida"));
                    budgetContainer.getContainerProperty(itemId, PROVEEDOR).setValue(rsRecords.getString("ProvNombre"));
                    budgetContainer.getContainerProperty(itemId, CANTIDAD).setValue(rsRecords.getDouble("Cantidad"));
                    budgetContainer.getContainerProperty(itemId, PRECIO).setValue(rsRecords.getDouble("Precio"));
                    budgetContainer.getContainerProperty(itemId, TOTAL).setValue(rsRecords.getDouble("Total"));
                    budgetContainer.getContainerProperty(itemId, MONEDA).setValue(rsRecords.getString("Moneda"));
                } while (rsRecords.next());

//                budgetGrid.select(budgetContainer.firstItemId());
            }
        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error al listar tabla PRESUPUESTOS DE LA TAREA : " + ex);
            ex.printStackTrace();
            Notification.show("ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }

//---------------------------------------------------------------------------------------------------------------------

    private void fillComboCuentaCentroCosto(ComboBox comboBox) {

        String queryString = "SELECT * ";
        queryString += " FROM centro_costo_cuenta ";
        queryString += " WHERE IdProyecto = " + ((SopdiUI) mainUI).sessionInformation.getStrProjectId();

// System.out.println("queryComboCCC=" + queryString);
        try {
            stQuery1 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords1 = stQuery1.executeQuery(queryString);

            comboBox.addItem("0");
            comboBox.setItemCaption("0", "<<ELIJA>>");

            medidas.put("0", "");
            precios.put("0", "0.00");

            while (rsRecords1.next()) { //  encontrado                
                comboBox.addItem(rsRecords1.getString("IdCuentaCentroCosto"));
                comboBox.setItemCaption(rsRecords1.getString("IdCuentaCentroCosto"), rsRecords1.getString("CodigoCuentaCentroCosto") + " " + rsRecords1.getString("Descripcion"));
                if (comboBox.equals(cuentaCentroCostoCbx)) {
                    medidas.put(rsRecords1.getString("IdCuentaCentroCosto"), rsRecords1.getString("UnidadMedida"));
                    precios.put(rsRecords1.getString("IdCuentaCentroCosto"), rsRecords1.getString("Precio"));
                }
            }

            comboBox.select("0");
        } catch (Exception ex1) {
            Notification.show("ERROR DEL SISTEMA AL BUSCAR CENTROS DE COSTO", Notification.Type.ERROR_MESSAGE);
            System.out.println("ERROR AL INTENTAR BUSCAR CATALOGO DE CUENTAS DE CENTROS DE COSTO : " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    private void fillComboProveedor(ComboBox comboBox) {

        String queryString = "SELECT * ";
        queryString += " FROM proveedor_empresa ";
        queryString += " WHERE IdEmpresa = " + empresaId;

        try {
            stQuery1 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords1 = stQuery1.executeQuery(queryString);

            comboBox.removeAllItems();
            comboBox.addItem(0);
            comboBox.setItemCaption(0, "<<ELIJA>>");
            comboBox.select(0);

            while (rsRecords1.next()) { //  encontrado                
                comboBox.addItem(rsRecords1.getString("IDProveedor"));
                comboBox.setItemCaption(rsRecords1.getString("IDProveedor"), rsRecords1.getString("Nombre"));

            }
        } catch (Exception ex1) {
            Notification.show("ERROR DEL SISTEMA AL BUSCAR CATALOGO DE PROVEEDORES", Notification.Type.ERROR_MESSAGE);
            System.out.println("ERROR AL INTENTAR BUSCAR CATALOGO DE PROVEEDORES : " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public boolean exportToExcel() {
        if (budgetGrid.getHeightByRows() > 0) {
            TableHolder tableHolder = new DefaultTableHolder(budgetGrid);
            ExcelExport excelExport = new ExcelExport(tableHolder);
            excelExport.excludeCollapsedColumns();
            excelExport.setDisplayTotals(false);
            String fileexport;
// produccion            fileexport = (empresa + "_" + empresaLbl.getValue().substring(5, empresaLbl.getValue().length()).replaceAll(" ", "_").replaceAll(",", "_").replaceAll("[()]", "").replaceAll("[.]", "").replaceAll("ñ", "n").replaceAll("Ñ", "N").replaceAll("ó", "o").replaceAll("é","") + "_INTEGRACION_CAMBIOS.xlsx").replaceAll(" ", "").replaceAll(",", "");
            fileexport = ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId() + "_" + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyName().replaceAll(" ", "_").replaceAll(",", "_").replaceAll("[()]", "").replaceAll("[.]", "").replaceAll("ñ", "n").replaceAll("Ñ", "N").replaceAll("ó", "o").replaceAll("é", "") + "_ASISTENCIA.xls";
            excelExport.setExportFileName(fileexport);
            excelExport.export();
        }
        return true;
    }

    public double getSugestedPrice(String idCuentaCentroCosto, String idProveedor) {
        String queryString = "";

        queryString = "SELECT Precio ";
        queryString += " FROM visita_inspeccion_tarea_presupuesto ";
        queryString += " WHERE IdProveedor = " + idProveedor;
        queryString += " AND   IdCuentaCentroCosto = " + idCuentaCentroCosto;
        queryString += " ORDER BY CodigoPresupuesto DESC Limit 1";

        System.out.println(queryString);

        double precio = 0.00;

        try {

            stQuery1 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords1 = stQuery1.executeQuery(queryString);

            if (rsRecords1.next()) { //  encontrado                
                precio = rsRecords1.getDouble("Precio");
            }

        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al obtener precio histórico : " + ex.getMessage());
            Notification.show("Error al obtener precio histórico ..!", Notification.Type.ERROR_MESSAGE);
        }
        return precio;
    }

    private void calculate() {
        if(cuentaCentroCostoCbx.getValue() == null) {
            return;
        }
        if (cuentaCentroCostoCbx.getValue().equals("0") == false) {
            if (!cantidadTxt.getValue().trim().isEmpty()) {
                totalTxt.setReadOnly(false);
                totalTxt.setValue(cantidadTxt.getDoubleValueDoNotThrow() * precioTxt.getDoubleValueDoNotThrow());
                totalTxt.setReadOnly(true);
            }
        }
    }
}