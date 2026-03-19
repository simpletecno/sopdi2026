package com.simpletecno.sopdi.operativo;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.SelectionEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;

import com.vaadin.ui.themes.ValoTheme;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.vaadin.dialogs.ConfirmDialog;

/**
 *
 * @author user
 */
public class InspectionTaskOCWindow extends Window {

    static final String ID_PROPERTY = "ID";
    static final String PROJECT_PROPERTY = "Project";
    static final String IDEX_PROPERTY = "IDEX";
    static final String CUENTA_PROPERTY = "Cuenta";
    static final String DESCRIPCION_PROPERTY = "Descripción";
    static final String UNIDAD_PROPERTY = "Unidad";
    static final String EMPRESA_PROPERTY = "Empresa";
    static final String PROVEEDOR_PROPERTY = "Proveedor";
    static final String MONEDA_PROPERTY = "Moneda";
    static final String CANTIDAD_PROPERTY = "Cantidad";
    static final String PRECIO_PROPERTY = "Pre_Prom.";
    static final String SALDO_DIC_PROPERTY = "Saldo DIC";
    static final String TOTAL_PROPERTY = "Total DIC";
    static final String ULT_PRECIO_PROPERTY = "Ult_Precio";
    static final String OC_CANTIDAD_PROPERTY = "OC_Cant.";
    static final String OC_PRECIO_PROPERTY = "OC_Precio";
    static final String OC_TOTAL_PROPERTY = "OC_Total";
    static final String IDPROJECT_PROPERTY = "IDProject";
    static final String ES_NUEVO_PROPERTY = "Nuevo";

    static final String IDOC_PROPERTY = "IDOC";
    static final String LOTE_PROPERTY = "Lote";

    static final String CODIGOOC_PROPERTY = "CodigoOC";
    static final String FECHA_PROPERTY = "Fecha";
    static final String USUARIO_PROPERTY = "Usuario";
    static final String TOTAL_OC_PROPERTY = "Total";
    static final String TOTAL_DOLARES_OC_PROPERTY = "Total Dólares";
    static final String ESTATUS_PROPERTY = "Estatus";

    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");
    static DecimalFormat numberFormat3 = new DecimalFormat("######0.00");

    VerticalLayout mainLayout = new VerticalLayout();

    ComboBox projectCbx;

    public IndexedContainer ocContainer = new IndexedContainer();
    Grid ocGrid;
    public IndexedContainer ocIdexContainer = new IndexedContainer();
    Grid ocIdexGrid;

    Button nuevaOCBtn;
    Button nuevoRubroBtn;
    Button nuevoRubroDesdeSeleccionadoBtn;
    Button eliminarRubroBtn;
    Button enPreparacionBtn;
    Button enRevisionBtn;
    Button autorizarBtn;
    Button finalizarBtn;

    UI mainUI;
    Statement stQuery;
    ResultSet rsRecords;
    Statement stQuery2;
    ResultSet rsRecords2;
    PreparedStatement stPreparedQuery;

    String queryString;

    String idOC;
    String tareaId;
    String codigoTarea;
    String descripcionTarea;
    String autorizadoTipo;
    String idcc;
    String cliente;
    String lote;

    String codigoOc;

    double cantidadOC;
    double totalOC;
    double precioOC;

    public InspectionTaskOCWindow(
            String idOC,
            String tareaId,
            String codigoTarea,
            String descripcionTarea,
            String autorizadoTipo,
            String idcc,
            String cliente,
            String lote
    ) {
        this.idOC = idOC;
        this.tareaId = tareaId;
        this.codigoTarea = codigoTarea;
        this.descripcionTarea = descripcionTarea;
        this.autorizadoTipo = autorizadoTipo;
        this.idcc = idcc;
        this.cliente = cliente;
        this.lote = lote;

        this.mainUI = UI.getCurrent();
        setWidth("98%");
        setHeight("98%");

        mainLayout.setWidth("100%");
        mainLayout.setHeightUndefined();

        setContent(mainLayout);

        Label titleLbl = new Label("<h4 style=\"color:RoyalBlue;\"" + "/> " +
                "IdOC = " + idOC + " para tarea : <b>" + codigoTarea + "</b> " + descripcionTarea +
                "<br>Centro de Costo : <b>" + idcc + " " + cliente + "</b></br>" +
                "<h4>");
        titleLbl.addStyleName(ValoTheme.LABEL_H4);
        titleLbl.setResponsive(true);
        titleLbl.setContentMode(ContentMode.HTML);

        projectCbx = new ComboBox("Project : ");
        projectCbx.setWidth("30em");
        projectCbx.setFilteringMode(FilteringMode.CONTAINS);
        projectCbx.setInvalidAllowed(false);
        projectCbx.setNullSelectionAllowed(false);
        projectCbx.setNewItemsAllowed(false);
        projectCbx.addContainerProperty("id", String.class, "");

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setResponsive(true);
        titleLayout.setWidth("100%");
        titleLayout.setMargin(false);
        titleLayout.addComponents(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.TOP_RIGHT);

        mainLayout.addComponent(titleLayout);
        mainLayout.setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        llenarComboProject();

        createOcGrid();
        createOcIdexGrid();

        fillOcGrid();

        actualizarBotones();
    }

    private void llenarComboProject() {
        String queryString=
        "SELECT *";
        queryString += " FROM project";
        queryString += " WHERE Estatus = 'ACTIVO'";
        queryString += " ORDER BY Numero";

//System.out.println("\n\n"+queryString);

        try {

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado

                do {

                    projectCbx.addItem(rsRecords.getString("Numero"));
                    projectCbx.setItemCaption(rsRecords.getString("Numero"), rsRecords.getString("Descripcion")
                            + " " + rsRecords.getString("CreadoFecha"));
                    projectCbx.getContainerProperty(rsRecords.getString("Numero"), "id").setValue(rsRecords.getString("Id"));

                } while (rsRecords.next());
            }
        } catch (Exception ex) {
            Logger.getLogger(ProjectTaskView.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de projects : " + ex.getMessage());
            Notification.show("Error al intentar leer registros de projects..!", Notification.Type.ERROR_MESSAGE);
        }

    }

    private void createOcGrid() {

        VerticalLayout reportLayout = new VerticalLayout();
        reportLayout.addStyleName("rcorners3");
        reportLayout.setWidth("100%");
        reportLayout.setResponsive(true);
        reportLayout.setSpacing(true);
        reportLayout.setMargin(false);

        HorizontalLayout layoutGrid = new HorizontalLayout();
        layoutGrid.setWidth("100%");
        layoutGrid.setSpacing(true);
        layoutGrid.setMargin(false);

        ocContainer.addContainerProperty(IDOC_PROPERTY, String.class, "");
        ocContainer.addContainerProperty(CODIGOOC_PROPERTY, String.class, "");
        ocContainer.addContainerProperty(DESCRIPCION_PROPERTY, String.class, "");
        ocContainer.addContainerProperty(FECHA_PROPERTY, String.class, "");
        ocContainer.addContainerProperty(USUARIO_PROPERTY, String.class, "");
        ocContainer.addContainerProperty(TOTAL_OC_PROPERTY, String.class, "");
        ocContainer.addContainerProperty(TOTAL_DOLARES_OC_PROPERTY, String.class, "");
        ocContainer.addContainerProperty(ESTATUS_PROPERTY, String.class, "");

        ocGrid = new Grid("ORDENES DE CAMBIO ", ocContainer);
        ocGrid.setWidth("100%");
        ocGrid.setImmediate(true);
        ocGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        ocGrid.setDescription("CLICK aqui para ver DETALLE de OC.");
        ocGrid.setHeightMode(HeightMode.ROW);
        ocGrid.setHeightByRows(5);
        ocGrid.setResponsive(true);
        ocGrid.setSizeFull();
        ocGrid.addSelectionListener((SelectionEvent.SelectionListener) event -> {
            if (ocGrid.getSelectedRow() != null) {
                fillOcIdexGrid();
                actualizarBotones();
            }
        });
        ocGrid.setRowStyleGenerator(line -> {
            String valor = String.valueOf(line.getItem().getItemProperty(ESTATUS_PROPERTY).getValue());
            if (!valor.trim().isEmpty()) {
                if (String.valueOf(line.getItem().getItemProperty(ESTATUS_PROPERTY).getValue()).equals("FINALIZADA")) {
                    return "red";
                } else {
                    return "green";
                }
            }
            return null;
        });

        layoutGrid.addComponent(ocGrid);
        layoutGrid.setComponentAlignment(ocGrid, Alignment.TOP_CENTER);
        reportLayout.addComponent(layoutGrid);

        nuevaOCBtn = new Button("Nueva Orden de Cambio");
        nuevaOCBtn.setIcon(FontAwesome.PLUS);
        nuevaOCBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        nuevaOCBtn.setDescription("Agregar nueva orden de cambio.");
        nuevaOCBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                ocGrid.deselectAll();
                crearNuevaOc();
            }
        });

        enPreparacionBtn = new Button("EN PREPARACION");
        enPreparacionBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        enPreparacionBtn.setIcon(FontAwesome.EDIT);
        enPreparacionBtn.addClickListener((Button.ClickListener) event -> {
            if (ocGrid.getSelectedRow() != null) {
                if (  String.valueOf(ocContainer.getContainerProperty(ocGrid.getSelectedRow(), ESTATUS_PROPERTY).getValue()).equals("EN REVISION")) {
                    ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de cambiar a estatus EN PREPARACION la orden de cambio ?",
                            "SI", "NO", new ConfirmDialog.Listener() {
                                public void onClose(ConfirmDialog dialog) {
                                    if (dialog.isConfirmed()) {
                                        actualizarEstatusOC("EN PREPARACION");
                                    }
                                }
                            } //end confirmdialog
                    ); //end dialog
                } // if estatus pendiente
                else {
                    Notification.show("LA ORDEN DE CAMBIO YA NO ESTA PENDIENTE DE REVISAR.", Notification.Type.WARNING_MESSAGE);
                }
            }
        });
        if (!((SopdiUI) mainUI).sessionInformation.getStrUserProfile().equals("ADMINISTRADOR")) {
            enPreparacionBtn.setEnabled(false);
        }

        enRevisionBtn = new Button("EN REVISION");
        enRevisionBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        enRevisionBtn.setIcon(FontAwesome.CHECK_CIRCLE);
        enRevisionBtn.addClickListener((Button.ClickListener) event -> {
            if (ocGrid.getSelectedRow() != null) {
                if (String.valueOf(ocContainer.getContainerProperty(ocGrid.getSelectedRow(), ESTATUS_PROPERTY).getValue()).equals("EN PREPARACION")
                        || String.valueOf(ocContainer.getContainerProperty(ocGrid.getSelectedRow(), ESTATUS_PROPERTY).getValue()).equals("AUTORIZADA")) {
                    ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de cambiar a estatus EN REVISION la orden de cambio ?",
                            "SI", "NO", new ConfirmDialog.Listener() {
                                public void onClose(ConfirmDialog dialog) {
                                    if (dialog.isConfirmed()) {
                                        actualizarEstatusOC("EN REVISION");
                                    }
                                }
                            } //end confirmdialog
                    ); //end dialog
                } // if estatus pendiente
                else {
                    Notification.show("LA ORDEN DE CAMBIO YA NO ESTA EN PREPARACION O AUTORIZADA.", Notification.Type.WARNING_MESSAGE);
                }
            }
        });

        autorizarBtn = new Button("AUTORIZADA");
        autorizarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        autorizarBtn.setIcon(FontAwesome.CHECK_CIRCLE);
        // button
        autorizarBtn.addClickListener((Button.ClickListener) event -> {
            if (ocGrid.getSelectedRow() != null) {
                if ( String.valueOf(ocContainer.getContainerProperty(ocGrid.getSelectedRow(), ESTATUS_PROPERTY).getValue()).equals("EN REVISION")) {
                    ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de cambiar estauts a AUTORIZADA la orden de cambio ?",
                            "SI", "NO", new ConfirmDialog.Listener() {
                                public void onClose(ConfirmDialog dialog) {
                                    if (dialog.isConfirmed()) {
                                        autorizarOC();
                                    }
                                }
                            } //end confirmdialog
                    ); //end dialog
                } // if estatus pendiente
                else {
                    Notification.show("LA ORDEN DE CAMBIO NO ESTA EN REVISION.", Notification.Type.WARNING_MESSAGE);
                }
            } //if selected row
        });

        finalizarBtn = new Button("FINALIZADA");
        finalizarBtn.addStyleName(ValoTheme.BUTTON_DANGER);
        finalizarBtn.setIcon(FontAwesome.STOP);
        // button
        finalizarBtn.addClickListener((Button.ClickListener) event -> {
            if (ocGrid.getSelectedRow() != null) {
                if (String.valueOf(ocContainer.getContainerProperty(ocGrid.getSelectedRow(), ESTATUS_PROPERTY).getValue()).equals("AUTORIZADA")) {
                    ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de FINALIZAR la orden de cambio ?",
                            "SI", "NO", new ConfirmDialog.Listener() {
                                public void onClose(ConfirmDialog dialog) {
                                    if (dialog.isConfirmed()) {
                                        actualizarEstatusOC("FINALIZADA");
                                    }
                                }
                            } //end confirmdialog
                    ); //end dialog
                } // if estatus pendiente
                else {
                    Notification.show("LA ORDEN DE CAMBIO NO ESTA AUTORIZADA.", Notification.Type.WARNING_MESSAGE);
                }
            } //if selected row
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.addComponent(nuevaOCBtn);
        buttonsLayout.setComponentAlignment(nuevaOCBtn, Alignment.BOTTOM_CENTER);
        buttonsLayout.addComponents(enPreparacionBtn, enRevisionBtn, autorizarBtn, finalizarBtn);
        buttonsLayout.setComponentAlignment(enPreparacionBtn, Alignment.BOTTOM_RIGHT);
        buttonsLayout.setComponentAlignment(enPreparacionBtn, Alignment.BOTTOM_RIGHT);
        buttonsLayout.setComponentAlignment(autorizarBtn, Alignment.BOTTOM_RIGHT);
        buttonsLayout.setComponentAlignment(finalizarBtn, Alignment.BOTTOM_RIGHT);

        reportLayout.addComponent(buttonsLayout);
        reportLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);

        mainLayout.addComponent(reportLayout);
        mainLayout.setComponentAlignment(reportLayout, Alignment.MIDDLE_CENTER);
    }

    private void createOcIdexGrid() {

        VerticalLayout reportLayout = new VerticalLayout();
        reportLayout.addStyleName("rcorners3");
        reportLayout.setWidth("100%");
        reportLayout.setResponsive(true);
        reportLayout.setSpacing(true);
        reportLayout.setMargin(false);

        HorizontalLayout layoutGrid = new HorizontalLayout();
        layoutGrid.setWidth("100%");
        layoutGrid.setSpacing(true);
        layoutGrid.setMargin(false);

        ocIdexContainer.addContainerProperty(ID_PROPERTY, String.class, "");
        ocIdexContainer.addContainerProperty(PROJECT_PROPERTY, String.class, "");
        ocIdexContainer.addContainerProperty(IDEX_PROPERTY, String.class, "");
        ocIdexContainer.addContainerProperty(CUENTA_PROPERTY, String.class, "");
        ocIdexContainer.addContainerProperty(DESCRIPCION_PROPERTY, String.class, "");
        ocIdexContainer.addContainerProperty(UNIDAD_PROPERTY, String.class, "");
        ocIdexContainer.addContainerProperty(EMPRESA_PROPERTY, String.class, "");
        ocIdexContainer.addContainerProperty(LOTE_PROPERTY, String.class, "");
        ocIdexContainer.addContainerProperty(PROVEEDOR_PROPERTY, String.class, "");
        ocIdexContainer.addContainerProperty(MONEDA_PROPERTY, String.class, "");
        ocIdexContainer.addContainerProperty(CANTIDAD_PROPERTY, String.class, "");
        ocIdexContainer.addContainerProperty(PRECIO_PROPERTY, String.class, "");
        ocIdexContainer.addContainerProperty(TOTAL_PROPERTY, String.class, "");
        ocIdexContainer.addContainerProperty(SALDO_DIC_PROPERTY, String.class, "");
        ocIdexContainer.addContainerProperty(ULT_PRECIO_PROPERTY, String.class, "");
        ocIdexContainer.addContainerProperty(OC_CANTIDAD_PROPERTY, String.class, "");
        ocIdexContainer.addContainerProperty(OC_PRECIO_PROPERTY, String.class, "");
        ocIdexContainer.addContainerProperty(OC_TOTAL_PROPERTY, String.class, "");
        ocIdexContainer.addContainerProperty(IDOC_PROPERTY, String.class, "");
        ocIdexContainer.addContainerProperty(CODIGOOC_PROPERTY, String.class, "");
        ocIdexContainer.addContainerProperty(IDPROJECT_PROPERTY, String.class, "");
        ocIdexContainer.addContainerProperty(ES_NUEVO_PROPERTY, String.class, "NO");

        ocIdexGrid = new Grid("IDEX DEL CENTRO DE COSTO Y PROJECT ", ocIdexContainer);
        ocIdexGrid.setWidth("100%");
        ocIdexGrid.setImmediate(true);
        ocIdexGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        ocIdexGrid.setDescription("Doble CLICK aqui para editar los datos de esta linea.");
        ocIdexGrid.setHeightMode(HeightMode.ROW);
        ocIdexGrid.setHeightByRows(10);
        ocIdexGrid.setResponsive(true);
        ocIdexGrid.setEditorEnabled(false);
        ocIdexGrid.setSizeFull();
        ocIdexGrid.addItemClickListener((event) -> {
            if (event != null) {
                if (event.isDoubleClick()) {
                    ocGrid.setReadOnly(true);
                    if (ocGrid.getSelectedRow() != null) {
                        if (String.valueOf(ocContainer.getContainerProperty(ocGrid.getSelectedRow(), ESTATUS_PROPERTY).getValue()).equals("EN PREPARACION")) {
                            InspectionTaskOCForm inspectionTaskOCForm
                                    = new InspectionTaskOCForm(
                                    event.getItemId(),
                                    ocIdexContainer,
                                    ocIdexGrid,
                                    0,
                                    String.valueOf(ocContainer.getContainerProperty(ocGrid.getSelectedRow(), IDOC_PROPERTY).getValue()), //id de orden de cambio header
                                    String.valueOf(ocIdexContainer.getContainerProperty(event.getItemId(), ID_PROPERTY).getValue()), //id de la linea detalle
                                    String.valueOf(ocIdexContainer.getContainerProperty(event.getItemId(), IDOC_PROPERTY).getValue()), //id de la linea detalle OC
                                    String.valueOf(6),
                                    String.valueOf(25),
                                    false,
                                    idcc,
                                    false,
                                    lote
                            );
                            inspectionTaskOCForm.center();
                            mainUI.addWindow(inspectionTaskOCForm);
                        } else {
                            Notification.show("LA ORDE DE CAMBIO YA ESTA FINALIZADA, NO SE PUEDE ALTERAR.", Notification.Type.WARNING_MESSAGE);
                        }

                    }
                    ocIdexGrid.setReadOnly(false);
                }
            }
        });

        ocIdexGrid.setRowStyleGenerator(line -> {
            String valor = String.valueOf(line.getItem().getItemProperty(OC_CANTIDAD_PROPERTY).getValue());
            if (!valor.trim().isEmpty()) {
                if (Double.valueOf(String.valueOf(line.getItem().getItemProperty(OC_CANTIDAD_PROPERTY).getValue())).doubleValue() != 0.00) {
                    return "green";
                }
                if (String.valueOf(line.getItem().getItemProperty(SALDO_DIC_PROPERTY).getValue()).trim().isEmpty()) {
                    return "green";
                }
            }
            return null;
        });

        ocIdexGrid.getColumn(PROJECT_PROPERTY).setExpandRatio(1);
        ocIdexGrid.getColumn(IDEX_PROPERTY).setExpandRatio(1);
        ocIdexGrid.getColumn(CUENTA_PROPERTY).setExpandRatio(1);
        ocIdexGrid.getColumn(DESCRIPCION_PROPERTY).setExpandRatio(2);
        ocIdexGrid.getColumn(UNIDAD_PROPERTY).setExpandRatio(1);
        ocIdexGrid.getColumn(EMPRESA_PROPERTY).setExpandRatio(1);
        ocIdexGrid.getColumn(PROVEEDOR_PROPERTY).setExpandRatio(1);
        ocIdexGrid.getColumn(MONEDA_PROPERTY).setExpandRatio(1);
        ocIdexGrid.getColumn(CANTIDAD_PROPERTY).setExpandRatio(1);
        ocIdexGrid.getColumn(PRECIO_PROPERTY).setExpandRatio(1);
        ocIdexGrid.getColumn(TOTAL_PROPERTY).setExpandRatio(1);
        ocIdexGrid.getColumn(SALDO_DIC_PROPERTY).setExpandRatio(1);
        ocIdexGrid.getColumn(ULT_PRECIO_PROPERTY).setExpandRatio(1);
        ocIdexGrid.getColumn(OC_CANTIDAD_PROPERTY).setExpandRatio(1);
        ocIdexGrid.getColumn(OC_PRECIO_PROPERTY).setExpandRatio(1);
        ocIdexGrid.getColumn(OC_TOTAL_PROPERTY).setExpandRatio(1);
        ocIdexGrid.getColumn(IDOC_PROPERTY).setHidable(true).setHidden(true);
        ocIdexGrid.getColumn(CODIGOOC_PROPERTY).setHidable(true).setHidden(true);
        ocIdexGrid.getColumn(IDPROJECT_PROPERTY).setHidable(true).setHidden(true);

        ocIdexGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if (CANTIDAD_PROPERTY.equals(cellReference.getPropertyId()) || OC_CANTIDAD_PROPERTY.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else if (PRECIO_PROPERTY.equals(cellReference.getPropertyId()) || OC_PRECIO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (TOTAL_PROPERTY.equals(cellReference.getPropertyId()) || OC_TOTAL_PROPERTY.equals(cellReference.getPropertyId()) || SALDO_DIC_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }
        });

        Grid.HeaderRow filterRow = ocIdexGrid.appendHeaderRow();

        Grid.HeaderCell cell = filterRow.getCell(IDEX_PROPERTY);

        TextField filterField = new TextField();
        filterField.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField.setInputPrompt("Filtrar");
        filterField.setColumns(8);

        filterField.addTextChangeListener(change -> {
            ocIdexContainer.removeContainerFilters(IDEX_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                ocIdexContainer.addContainerFilter(
                        new SimpleStringFilter(IDEX_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell.setComponent(filterField);

        Grid.HeaderCell cell2 = filterRow.getCell(PROJECT_PROPERTY);

        TextField filterField2 = new TextField();
        filterField2.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField2.setInputPrompt("Filtrar");
        filterField2.setColumns(8);

        filterField2.addTextChangeListener(change -> {
            ocIdexContainer.removeContainerFilters(PROJECT_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                ocIdexContainer.addContainerFilter(
                        new SimpleStringFilter(PROJECT_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell2.setComponent(filterField2);

        Grid.HeaderCell cell3 = filterRow.getCell(CUENTA_PROPERTY);

        TextField filterField3 = new TextField();
        filterField3.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField3.setInputPrompt("Filtrar");
        filterField3.setColumns(8);

        filterField3.addTextChangeListener(change -> {
            ocIdexContainer.removeContainerFilters(CUENTA_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                ocIdexContainer.addContainerFilter(
                        new SimpleStringFilter(CUENTA_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell3.setComponent(filterField3);


        Grid.HeaderCell cellDesc = filterRow.getCell(DESCRIPCION_PROPERTY);

        TextField filterFieldDesc = new TextField();
        filterFieldDesc.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterFieldDesc.setInputPrompt("Filtrar");
        filterFieldDesc.setColumns(15);

        filterFieldDesc.addTextChangeListener(change -> {
            ocIdexContainer.removeContainerFilters(DESCRIPCION_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                ocIdexContainer.addContainerFilter(
                        new SimpleStringFilter(DESCRIPCION_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cellDesc.setComponent(filterFieldDesc);

        Grid.HeaderCell cellProv = filterRow.getCell(PROVEEDOR_PROPERTY);

        TextField filterFieldProv = new TextField();
        filterFieldProv.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterFieldProv.setInputPrompt("Filtrar");
        filterFieldProv.setColumns(15);

        filterFieldProv.addTextChangeListener(change -> {
            ocIdexContainer.removeContainerFilters(PROVEEDOR_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                ocIdexContainer.addContainerFilter(
                        new SimpleStringFilter(PROVEEDOR_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cellProv.setComponent(filterFieldProv);

        layoutGrid.addComponent(ocIdexGrid);
        layoutGrid.setComponentAlignment(ocIdexGrid, Alignment.TOP_CENTER);
        reportLayout.addComponent(layoutGrid);

        nuevoRubroBtn = new Button("NUEVO RUBRO");
        nuevoRubroBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        nuevoRubroBtn.setIcon(FontAwesome.PLUS);
        nuevoRubroBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (ocGrid.getSelectedRow() != null) {
//                    if(projectCbx.getValue() == null) {
//                        Notification.show("Por favor elija un project!", Notification.Type.HUMANIZED_MESSAGE);
//                        projectCbx.focus();
//                        return;
//                    }
                    InspectionTaskOCForm inspectionTaskOCForm
                            = new InspectionTaskOCForm(
                            ocGrid.getSelectedRow(),
                            ocIdexContainer,
                            ocIdexGrid,
                            0,
                            String.valueOf(ocContainer.getContainerProperty(ocGrid.getSelectedRow(), IDOC_PROPERTY).getValue()), //id de orden de cambio header
                            "0", //String.valueOf(ocIdexContainer.getContainerProperty(ocIdexGrid.getSelectedRow(), ID_PROPERTY).getValue()) ,//id de la linea detalle
                            "0", //id de la linea detalle OC
                            "0",
                            "0",
                            true,
                            idcc,
                            false,
                            lote
                    );
                    inspectionTaskOCForm.center();
                    mainUI.addWindow(inspectionTaskOCForm);
                } //if selected row
            } // button
        });

        nuevoRubroDesdeSeleccionadoBtn = new Button("NUEVO RUBRO DESDE SELECCIONADO");
        nuevoRubroDesdeSeleccionadoBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        nuevoRubroDesdeSeleccionadoBtn.setIcon(FontAwesome.PLUS);
        nuevoRubroDesdeSeleccionadoBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (ocGrid.getSelectedRow() != null && ocIdexGrid.getSelectedRow() != null) {
                    InspectionTaskOCForm inspectionTaskOCForm
                            = new InspectionTaskOCForm(
                            ocGrid.getSelectedRow(),
                            ocIdexContainer,
                            ocIdexGrid,
                            ocIdexGrid.getSelectedRow(),
                            String.valueOf(ocContainer.getContainerProperty(ocGrid.getSelectedRow(), IDOC_PROPERTY).getValue()), //id de orden de cambio header
                            "0", //String.valueOf(ocIdexContainer.getContainerProperty(ocIdexGrid.getSelectedRow(), ID_PROPERTY).getValue()) ,//id de la linea detalle
                            "0", //id de la linea detalle OC
                            String.valueOf(ocIdexContainer.getContainerProperty(ocIdexGrid.getSelectedRow(), PROJECT_PROPERTY).getValue()),
                            String.valueOf(ocIdexContainer.getContainerProperty(ocIdexGrid.getSelectedRow(), IDPROJECT_PROPERTY).getValue()),
                            true,
                            idcc,
                            true,
                            lote
                    );
                    inspectionTaskOCForm.center();
                    mainUI.addWindow(inspectionTaskOCForm);
                } //if selected row
            } // button
        });

        eliminarRubroBtn = new Button("ELMINAR RUBRO");
        eliminarRubroBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        eliminarRubroBtn.addStyleName(ValoTheme.BUTTON_DANGER);
        eliminarRubroBtn.setIcon(FontAwesome.TRASH);
        eliminarRubroBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (ocGrid.getSelectedRow() != null && ocIdexGrid.getSelectedRow() != null) {
                    if (String.valueOf(ocContainer.getContainerProperty(ocGrid.getSelectedRow(), ESTATUS_PROPERTY).getValue()).equals("EN PREPARACION")) {
                        if (String.valueOf(ocIdexContainer.getContainerProperty(ocIdexGrid.getSelectedRow(), ES_NUEVO_PROPERTY).getValue()).equals("SI")) {
                            ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de eliminar este rubro de la orden de cambio ?",
                                    "SI", "NO", new ConfirmDialog.Listener() {
                                        public void onClose(ConfirmDialog dialog) {
                                            if (dialog.isConfirmed()) {
                                                eliminarRubro();
                                            }
                                        }
                                    } //end confirmdialog
                            ); //end dialog
                        }
                        else {
                            Notification.show("EL RUBRO NO FUE CREADO EN ESTA ORDEN DE CAMBIO, NO SE PUEDE ELIMINAR.", Notification.Type.WARNING_MESSAGE);
                        }
                    } else {
                        Notification.show("LA ORDEN DE CAMBIO NO ESTA EN PREPARACION, NO SE PUEDE ALTERAR.", Notification.Type.WARNING_MESSAGE);
                    }
                }
                else {
                    Notification.show("POR FAVOR SELECCIONE UN RUBRO PARA ELIMINAR.", Notification.Type.WARNING_MESSAGE);
                }
            } // button
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setWidth("100%");
        buttonsLayout.setImmediate(true);
        buttonsLayout.setPrimaryStyleName(ValoTheme.BUTTON_SMALL);

        buttonsLayout.addComponents(nuevoRubroBtn, nuevoRubroDesdeSeleccionadoBtn, eliminarRubroBtn);
        buttonsLayout.setComponentAlignment(nuevoRubroBtn, Alignment.BOTTOM_LEFT);
        buttonsLayout.setComponentAlignment(nuevoRubroDesdeSeleccionadoBtn, Alignment.BOTTOM_LEFT);
        buttonsLayout.setComponentAlignment(eliminarRubroBtn, Alignment.BOTTOM_RIGHT);

        reportLayout.addComponent(buttonsLayout);
        reportLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);

        mainLayout.addComponent(reportLayout);
        mainLayout.setComponentAlignment(reportLayout, Alignment.MIDDLE_CENTER);
    }

    public void fillOcGrid() {

        if (ocGrid == null) {
            return;
        }
        ocContainer.removeAllItems();
//        ocIdexGrid.select(null);

        try {

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            String descripcion = "Tarea : " + codigoTarea + " " + descripcionTarea + " Centro de Costo : " + idcc + " " + cliente;

            String queryString = "";

            if (!tareaId.isEmpty()) {
                queryString = "SELECT *, usuario.Nombre AS NombreUsuario";
                queryString += " FROM  visita_inspeccion_tarea_oc";
                queryString += " INNER JOIN usuario ON usuario.IdUsuario = visita_inspeccion_tarea_oc.CreadoUsuario";
                queryString += " WHERE IdVisitaInspeccionTarea = " + tareaId;
                if (!idOC.equals("0")) {
                    queryString += " AND IdVisitaInspeccionTareaOC = " + idOC;
                }
            }

            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado

                Object itemId = 0;

                do {
                    itemId = ocContainer.addItem();

                    ocContainer.getContainerProperty(itemId, IDOC_PROPERTY).setValue(rsRecords.getString("IdVisitaInspeccionTareaOC"));
                    ocContainer.getContainerProperty(itemId, CODIGOOC_PROPERTY).setValue(rsRecords.getString("CodigoOC"));
                    ocContainer.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(descripcion);
                    ocContainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue(rsRecords.getString("Fecha"));
                    ocContainer.getContainerProperty(itemId, USUARIO_PROPERTY).setValue(rsRecords.getString("NombreUsuario"));
                    ocContainer.getContainerProperty(itemId, TOTAL_OC_PROPERTY).setValue(numberFormat.format(rsRecords.getDouble("Total")));
                    ocContainer.getContainerProperty(itemId, TOTAL_DOLARES_OC_PROPERTY).setValue(numberFormat.format(rsRecords.getDouble("TotalDolares")));
                    ocContainer.getContainerProperty(itemId, ESTATUS_PROPERTY).setValue(rsRecords.getString("Estatus"));

                } while (rsRecords.next());

                if (ocContainer.size() == 1) {
                    ocGrid.select(ocContainer.getIdByIndex(0));
                    fillOcIdexGrid();
                } else {
                    ocGrid.select(null);
                    ocIdexContainer.removeAllItems();
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error al listar tabla OC.", ex);
            ex.printStackTrace();
            Notification.show("ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }

    private void crearNuevaOc() {
        try {
            queryString = "SELECT CodigoOC";
            queryString += " FROM  visita_inspeccion_tarea_oc ";
            queryString += " WHERE IdVisitaInspeccionTarea = " + tareaId;
            queryString += " ORDER BY CodigoOC DESC";
            queryString += " LIMIT 1";

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { // encontrado la ultima tarea...
                codigoOc = codigoTarea.substring(0, 16) + String.format("%02d", Integer.valueOf(rsRecords.getString("CodigoOC").substring(16, 18)) + 1);
            } else {
                codigoOc = codigoTarea.substring(0, 16) + "01";
            }

            queryString = "INSERT INTO visita_inspeccion_tarea_oc ";
            queryString += " (IdVisitaInspeccionTarea, CodigoOC, Fecha, CreadoUsuario, Estatus) ";
            queryString += " VALUES (";
            queryString += "  " + tareaId;
            queryString += ",'" + codigoOc + "'";
            queryString += ",current_date";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",'EN PREPARACION'";
            queryString += ")";

            stPreparedQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().prepareStatement(queryString, Statement.RETURN_GENERATED_KEYS);
            stPreparedQuery.executeUpdate();
            rsRecords2 = stPreparedQuery.getGeneratedKeys();

            rsRecords2.next();

            String recordKey = rsRecords2.getString(1);

            Object itemObject = ocContainer.addItem();

            ocContainer.getContainerProperty(itemObject, IDOC_PROPERTY).setValue(recordKey);
            ocContainer.getContainerProperty(itemObject, CODIGOOC_PROPERTY).setValue(codigoOc);
            ocContainer.getContainerProperty(itemObject, FECHA_PROPERTY).setValue(new Utileria().getFecha());
            ocContainer.getContainerProperty(itemObject, USUARIO_PROPERTY).setValue(((SopdiUI) mainUI).sessionInformation.getStrUserFullName());
            ocContainer.getContainerProperty(itemObject, ESTATUS_PROPERTY).setValue("EN PREPARACION");

            ocGrid.select(itemObject);

            Notification.show("OPERACION EXITOSA! IDOC = [" + recordKey + "] LOS IDEX YA ESTAN DESPLEGADOS PUEDE ELEGIR UN IDEX (LA COMBINACION DE IDEX, AREA, PROVEEDOR) PARA INICIAR LA ORDEN DE CAMBIO.", Notification.Type.HUMANIZED_MESSAGE);

        } catch (Exception ex) {
            Notification.show("Error al actualizar tarea con OC : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "ERROR AL REGISTRAR O MODIFICAR TAREA CON OC. ", ex);
        }
    }

    //    public void fillOcIdexGrid(String project) {
    public void fillOcIdexGrid() {

        if (ocIdexGrid == null) {
            return;
        }
        ocIdexContainer.removeAllItems();
//        ocIdexGrid.select(null);

        try {

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            String queryString = "SELECT DIC.Id, DIC.IdProject, DIC.NoCuenta, CCC.Descripcion CuentaDescripcion, ";
            queryString += " DIC.IdEmpresa, DIC.IdProveedor, Prov.Nombre NombreProveedor, ";
            queryString += " DIC.Idex, DIC.Unidad, DIC.Moneda, Lote, ";
            queryString += " SUM(DIC.Total / DIC.Cantidad) PrecioTotal, SUM(DIC.Cantidad) CantidadTotal, SUM(DIC.Total) TotalTotal";
            queryString += " FROM  DetalleItemsCostos DIC";
            queryString += " INNER JOIN proveedor Prov ON Prov.IdProveedor = DIC.IdProveedor";
            queryString += " INNER JOIN centro_costo_cuenta CCC ON CCC.CodigoCuentaCentroCosto = DIC.NoCuenta";
            queryString += " INNER JOIN area On area.IdArea = DIC.IdArea";
            queryString += " WHERE DIC.IdProject In (Select PRJ.Numero From project PRJ WHERE PRJ.Estatus = 'ACTIVO')";
            queryString += " AND DIC.Tipo In ('INTINI', 'DOCA')";
            queryString += " AND DIC.IdCC = '" + idcc + "'";
            queryString += " GROUP BY DIC.IdProject, DIC.NoCuenta, DIC.IdEmpresa, DIC.IdProveedor, DIC.Idex, DIC.Lote";
            queryString += " ORDER BY DIC.IdProject, DIC.Idex, DIC.NoCuenta ";

//System.out.println(queryString);

            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado

                double cantidadTotal = 0.00;
                double montoTotal = 0.00;
                double precioPromedio = 0.00;
                Object itemId = 0;

                String idRecordProject = "0";

                do {

                    cantidadTotal = rsRecords.getDouble("CantidadTotal");
                    precioPromedio = rsRecords.getDouble("PrecioTotal");
                    montoTotal = cantidadTotal * precioPromedio;

                    montoTotal = montoTotal - getSaldoDocumentosContablesAplicados(
                            idcc,
                            rsRecords.getString("IdProject"),
                            rsRecords.getString("Idex"),
                            rsRecords.getString("NoCuenta"),
                            rsRecords.getString("IdEmpresa"),
                            rsRecords.getString("IdProveedor")
                    );

                    if (montoTotal > 0.00) {

                        idRecordProject = getRecordIdProject(rsRecords.getString("IdProject"));

                        itemId = ocIdexContainer.addItem();

                        ocIdexContainer.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords.getString("Id"));
                        ocIdexContainer.getContainerProperty(itemId, PROJECT_PROPERTY).setValue(rsRecords.getString("IdProject")); //es el numero de project
                        ocIdexContainer.getContainerProperty(itemId, IDPROJECT_PROPERTY).setValue(idRecordProject); //es el ID de project
                        ocIdexContainer.getContainerProperty(itemId, IDEX_PROPERTY).setValue(rsRecords.getString("Idex"));
                        ocIdexContainer.getContainerProperty(itemId, CUENTA_PROPERTY).setValue(rsRecords.getString("NoCuenta"));
                        ocIdexContainer.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(rsRecords.getString("CuentaDescripcion"));
                        ocIdexContainer.getContainerProperty(itemId, UNIDAD_PROPERTY).setValue(rsRecords.getString("Unidad"));
                        ocIdexContainer.getContainerProperty(itemId, EMPRESA_PROPERTY).setValue(rsRecords.getString("IdEmpresa"));
                        ocIdexContainer.getContainerProperty(itemId, LOTE_PROPERTY).setValue(rsRecords.getString("Lote"));
                        ocIdexContainer.getContainerProperty(itemId, PROVEEDOR_PROPERTY).setValue(rsRecords.getString("IdProveedor") + " " + rsRecords.getString("NombreProveedor"));
                        ocIdexContainer.getContainerProperty(itemId, MONEDA_PROPERTY).setValue(rsRecords.getString("Moneda"));
                        ocIdexContainer.getContainerProperty(itemId, CANTIDAD_PROPERTY).setValue(numberFormat3.format(cantidadTotal));
                        ocIdexContainer.getContainerProperty(itemId, PRECIO_PROPERTY).setValue(numberFormat.format(precioPromedio));
                        ocIdexContainer.getContainerProperty(itemId, TOTAL_PROPERTY).setValue(numberFormat.format(cantidadTotal * precioPromedio));
                        ocIdexContainer.getContainerProperty(itemId, SALDO_DIC_PROPERTY).setValue(numberFormat.format(montoTotal));
                        ocIdexContainer.getContainerProperty(itemId, ULT_PRECIO_PROPERTY).setValue(
                                UltimoPrecio(
                                        rsRecords.getString("IdProject"),
                                        idcc,
                                        rsRecords.getString("Idex"),
                                        rsRecords.getString("NoCuenta"),
                                        rsRecords.getString("IdEmpresa"),
                                        rsRecords.getString("IdProveedor")
                                )
                        );
                        ocIdexContainer.getContainerProperty(itemId, ES_NUEVO_PROPERTY).setValue("NO");

                        if (ocGrid.getSelectedRow() != null) { // no es nueva OC
                            if (buscarOC(
                                    itemId,
                                    idRecordProject,
                                    idcc,
                                    rsRecords.getString("Idex"),
                                    rsRecords.getString("NoCuenta"),
                                    rsRecords.getString("IdEmpresa"),
                                    rsRecords.getString("IdProveedor")) == false) {

                                if (   String.valueOf(ocContainer.getContainerProperty(ocGrid.getSelectedRow(), ESTATUS_PROPERTY).getValue()).equals("EN REVISION")
                                    || String.valueOf(ocContainer.getContainerProperty(ocGrid.getSelectedRow(), ESTATUS_PROPERTY).getValue()).equals("AUTORIZADA")
                                    || String.valueOf(ocContainer.getContainerProperty(ocGrid.getSelectedRow(), ESTATUS_PROPERTY).getValue()).equals("FINALIZADA")) {
                                    ocIdexContainer.removeItem(itemId);
                                }

                            }
                        } else {
                            itemId = ocIdexContainer.addItem();
                        }

                    }

                } while (rsRecords.next());

                agregarOcsAlGrid();

            }
            ocIdexGrid.sort(OC_TOTAL_PROPERTY, SortDirection.DESCENDING);
        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error al listar tabla IDEX de project para OC.", ex);
            ex.printStackTrace();
            Notification.show("ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }

    private String getRecordIdProject(String idProject) {
        String recordIdProject = "0";
        try {
            stQuery2 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            String queryString = "SELECT Id ";
            queryString += " FROM  project";
            queryString += " WHERE Numero = " + idProject;
            queryString += " AND Estatus = 'ACTIVO'";

            rsRecords2 = stQuery2.executeQuery(queryString);

            if (rsRecords2.next()) { //  encontrado
                recordIdProject = rsRecords2.getString("Id");
            }
        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error al obtener ULTIMO PROJECT ACTIVO.", ex);
            ex.printStackTrace();
            Notification.show("ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
        return recordIdProject;
    }

    private String UltimoPrecio(
            String idProject,
            String idcc,
            String idex,
            String noCuenta,
//            String idArea,
            String idEmpresa,
            String idProveedor
    ) {

        String ultimoPrecio = "0.00";

        try {
            stQuery2 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            String queryString = "SELECT Precio ";
            queryString += " FROM  DetalleItemsCostos";
            queryString += " WHERE IdCC = '" + idcc + "'";
            queryString += " AND IdProject = " + idProject;
            queryString += " AND Idex = '" + idex + "'";
            queryString += " AND NoCuenta = '" + noCuenta + "'";
//            queryString += " And IdArea = " + idArea;
            queryString += " AND IdEmpresa = " + idEmpresa;
            queryString += " AND IdProveedor = " + idProveedor;
            queryString += " ORDER BY ID DESC LIMIT 1";

            rsRecords2 = stQuery2.executeQuery(queryString);

            if (rsRecords2.next()) { //  encontrado
                ultimoPrecio = rsRecords2.getString("Precio");
            }
        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error al obtener ULTIMO PRECIO DE IDEX.", ex);
            ex.printStackTrace();
            Notification.show("ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }

        return ultimoPrecio;
    }

    private void getSaldoSegunOC(
            String CENTROCOSTO,
            String PROJECT,
            String IDEX,
            String CUENTA,
//            String IDAREA,
            String EMPRESA,
            String PROVEEDOR
    ) {

        totalOC = 0.00;
        cantidadOC = 0.00;
        precioOC = 0.00;

        String queryString = "SELECT SUM(Cantidad) TotalCantidad, SUM(Precio) TotalPrecio, SUM(Total) TotalTotal ";
        queryString += " FROM  DetalleItemsCostos ";
        queryString += " WHERE IDCC      = '" + CENTROCOSTO + "'";
        queryString += " AND IdProject   = " + PROJECT;
        queryString += " AND Idex        = '" + IDEX + "'";
        queryString += " AND NoCuenta    = '" + CUENTA + "'";
        queryString += " AND IdEmpresa   = " + EMPRESA;
        queryString += " AND IdProveedor = " + PROVEEDOR;
        queryString += " AND Tipo = 'DOCA'";

        try {

            stQuery2 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords2 = stQuery2.executeQuery(queryString);

            if (rsRecords2.next()) { //  encontrado
                cantidadOC = rsRecords2.getDouble("TotalCantidad");
                precioOC = rsRecords2.getDouble("TotalPrecio");
                totalOC = rsRecords2.getDouble("TotalTotal");
            }
        } catch (Exception ex) {
            Logger.getLogger(IntegracionActual.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de detalle de integracion DCA : " + ex.getMessage());
            Notification.show("Error al intentar leer detalle de integracion DCA..! ", Notification.Type.ERROR_MESSAGE);
        }
    }

    private double getSaldoDocumentosContablesAplicados(
            String CENTROCOSTO,
            String PROJECT,
            String IDEX,
            String CUENTA,
            String EMPRESA,
            String PROVEEDOR
    ) {

        double total = 0.00;

        String
                queryString = "SELECT SUM(Total) TotalTotal ";
        queryString += " FROM  DocumentosContablesAplicados ";
        queryString += " WHERE IDCC      = '" + CENTROCOSTO + "'";
        queryString += " AND IdProject   = " + PROJECT;
        queryString += " AND Idex        = '" + IDEX + "'";
        queryString += " AND NoCuenta    = '" + CUENTA + "'";
        queryString += " AND IdEmpresa   = " + EMPRESA;
        queryString += " AND IdProveedor = " + PROVEEDOR;

        try {

            stQuery2 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords2 = stQuery2.executeQuery(queryString);

            if (rsRecords2.next()) { //  encontrado
                total = rsRecords2.getDouble("TotalTotal");
            }
        } catch (Exception ex) {
            Logger.getLogger(IntegracionActual.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de detalle de integracion DCA : " + ex.getMessage());
            Notification.show("Error al intentar leer detalle de integracion DCA..! ", Notification.Type.ERROR_MESSAGE);
        }

        return total;

    }

    boolean buscarOC(
            Object itemId,
            String IDPROJECT,
            String CENTROCOSTO,
            String IDEX,
            String CUENTA,
            String EMPRESA,
            String PROVEEDOR
    ) {

        boolean siTieneOC = false;

        try {

            stQuery2 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            queryString = "SELECT * ";
            queryString += " FROM visita_inspeccion_tarea_oc_detalle";
            queryString += " WHERE IDCC = '" + CENTROCOSTO + "'";
            queryString += " AND IdProject  = " + IDPROJECT;
            queryString += " AND Idex     = '" + IDEX + "'";
            queryString += " AND NoCuenta = '" + CUENTA + "'";
            queryString += " AND IdEmpresa   = " + EMPRESA;
            queryString += " AND IdProveedor = " + PROVEEDOR;
            queryString += " AND IdVisitaInspeccionTareaOC = " + ocContainer.getContainerProperty(ocGrid.getSelectedRow(), IDOC_PROPERTY).getValue();

            rsRecords2 = stQuery2.executeQuery(queryString);

            if (rsRecords2.next()) { //  encontrado

                ocIdexContainer.getContainerProperty(itemId, OC_CANTIDAD_PROPERTY).setValue(rsRecords2.getString("Cantidad"));
                ocIdexContainer.getContainerProperty(itemId, OC_PRECIO_PROPERTY).setValue(rsRecords2.getString("Precio"));
                ocIdexContainer.getContainerProperty(itemId, OC_TOTAL_PROPERTY).setValue(rsRecords2.getString("Total"));
                ocIdexContainer.getContainerProperty(itemId, IDOC_PROPERTY).setValue(rsRecords2.getString("IdVisitaInspeccionTareaOCDetalle"));
                ocIdexContainer.getContainerProperty(itemId, CODIGOOC_PROPERTY).setValue(ocContainer.getContainerProperty(ocGrid.getSelectedRow(), CODIGOOC_PROPERTY).getValue());

                siTieneOC = true;

            } else {
                ocIdexContainer.getContainerProperty(itemId, OC_CANTIDAD_PROPERTY).setValue("0");
                ocIdexContainer.getContainerProperty(itemId, OC_PRECIO_PROPERTY).setValue("0");
                ocIdexContainer.getContainerProperty(itemId, OC_TOTAL_PROPERTY).setValue("0");
                ocIdexContainer.getContainerProperty(itemId, CODIGOOC_PROPERTY).setValue("0");
                ocIdexContainer.getContainerProperty(itemId, IDOC_PROPERTY).setValue("0");
            }
        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error BUSCAR IDEX OC.", ex);
            ex.printStackTrace();
            Notification.show("ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }

        return siTieneOC;
    }

    private void agregarOcsAlGrid() {

        if (ocGrid.getSelectedRow() == null) {
            return;
        }

        Object itemId = 0;

        try {

            String queryString = "SELECT DIC.IdVisitaInspeccionTareaOCDetalle, DIC.IdProject, DIC.NoCuenta, CCC.Descripcion CuentaDescripcion, ";
            queryString += " DIC.IdEmpresa, DIC.IdProveedor, Prov.Nombre NombreProveedor, ";
            queryString += " DIC.Idex, DIC.Unidad, DIC.Moneda, Lote, ";
            queryString += " DIC.Cantidad, DIC.Precio, DIC.Total";
            queryString += " FROM  visita_inspeccion_tarea_oc_detalle DIC";
            queryString += " INNER JOIN proveedor Prov ON Prov.IdProveedor = DIC.IdProveedor";
            queryString += " INNER JOIN centro_costo_cuenta CCC ON CCC.CodigoCuentaCentroCosto = DIC.NoCuenta";
            queryString += " WHERE DIC.IdVisitaInspeccionTareaOC = " + ocContainer.getContainerProperty(ocGrid.getSelectedRow(), IDOC_PROPERTY).getValue();
            queryString += " AND DIC.IdCC = '" + idcc + "'";

            rsRecords2 = stQuery2.executeQuery(queryString);

            if (rsRecords2.next()) {

                do {

                    itemId = ocIdexContainer.addItem();

                    ocIdexContainer.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords2.getString("IdVisitaInspeccionTareaOCDetalle"));
                    ocIdexContainer.getContainerProperty(itemId, PROJECT_PROPERTY).setValue(rsRecords2.getString("IdProject"));
                    ocIdexContainer.getContainerProperty(itemId, IDEX_PROPERTY).setValue(rsRecords2.getString("Idex"));
                    ocIdexContainer.getContainerProperty(itemId, CUENTA_PROPERTY).setValue(rsRecords2.getString("NoCuenta"));
                    ocIdexContainer.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(rsRecords2.getString("CuentaDescripcion"));
                    ocIdexContainer.getContainerProperty(itemId, UNIDAD_PROPERTY).setValue(rsRecords2.getString("Unidad"));
                    ocIdexContainer.getContainerProperty(itemId, EMPRESA_PROPERTY).setValue(rsRecords2.getString("IdEmpresa"));
                    ocIdexContainer.getContainerProperty(itemId, LOTE_PROPERTY).setValue(rsRecords2.getString("Lote"));
                    ocIdexContainer.getContainerProperty(itemId, PROVEEDOR_PROPERTY).setValue(rsRecords2.getString("IdProveedor") + " " + rsRecords2.getString("NombreProveedor"));
                    ocIdexContainer.getContainerProperty(itemId, MONEDA_PROPERTY).setValue(rsRecords2.getString("Moneda"));
                    ocIdexContainer.getContainerProperty(itemId, CANTIDAD_PROPERTY).setValue(numberFormat3.format(rsRecords2.getDouble("Cantidad")));
                    ocIdexContainer.getContainerProperty(itemId, PRECIO_PROPERTY).setValue(numberFormat3.format(rsRecords2.getDouble("Precio")));
                    ocIdexContainer.getContainerProperty(itemId, TOTAL_PROPERTY).setValue(numberFormat.format(rsRecords2.getDouble("Total")));
                    ocIdexContainer.getContainerProperty(itemId, ES_NUEVO_PROPERTY).setValue("SI");
                } while (rsRecords2.next());

            }

        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error BUSCAR IDEX OC cambios.", ex);
            ex.printStackTrace();
            Notification.show("ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }

    private void actualizarBotones() {
        if(ocGrid.getSelectedRow() == null) {
            return;
        }
        if (String.valueOf(ocContainer.getContainerProperty(ocGrid.getSelectedRow(), ESTATUS_PROPERTY).getValue()).equals("FINALIZADA")) {
            nuevoRubroBtn.setEnabled(false);
            nuevoRubroDesdeSeleccionadoBtn.setEnabled(false);
            enPreparacionBtn.setEnabled(false);
            enRevisionBtn.setEnabled(false);
            autorizarBtn.setEnabled(false);
            finalizarBtn.setEnabled(false);
        } else if (String.valueOf(ocContainer.getContainerProperty(ocGrid.getSelectedRow(), ESTATUS_PROPERTY).getValue()).equals("EN REVISION")) {
            nuevoRubroBtn.setEnabled(false);
            nuevoRubroDesdeSeleccionadoBtn.setEnabled(false);
            enPreparacionBtn.setEnabled(true);
            enRevisionBtn.setEnabled(false);
            autorizarBtn.setEnabled(true);
            finalizarBtn.setEnabled(false);
        } else if (String.valueOf(ocContainer.getContainerProperty(ocGrid.getSelectedRow(), ESTATUS_PROPERTY).getValue()).equals("AUTORIZADA")) {
            nuevoRubroBtn.setEnabled(false);
            nuevoRubroDesdeSeleccionadoBtn.setEnabled(false);
            enPreparacionBtn.setEnabled(false);
            enRevisionBtn.setEnabled(false);
            autorizarBtn.setEnabled(false);
            finalizarBtn.setEnabled(true);
        } else { //pendiente o en preparacion
            nuevoRubroBtn.setEnabled(true);
            nuevoRubroDesdeSeleccionadoBtn.setEnabled(true);
            enPreparacionBtn.setEnabled(false);
            enRevisionBtn.setEnabled(true);
            autorizarBtn.setEnabled(false);
            finalizarBtn.setEnabled(false);
        }

        if (!((SopdiUI) mainUI).sessionInformation.getStrUserProfile().equals("ADMINISTRADOR")) {
            autorizarBtn.setEnabled(false);
        }
    }
    private void actualizarEstatusOC(String estatus) {

        try {

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            queryString = "UPDATE visita_inspeccion_tarea_oc SET ";
            queryString += " Estatus = '" + estatus + "'";
            queryString += ",Total = (SELECT SUM(Total) FROM visita_inspeccion_tarea_oc_detalle Where Moneda = 'QUETZALES')";
            queryString += ",TotalDolares = (SELECT SUM(Total) FROM visita_inspeccion_tarea_oc_detalle Where Moneda = 'DOLARES')";
            queryString += " WHERE IdVisitaInspeccionTareaOC  = " + ocContainer.getContainerProperty(ocGrid.getSelectedRow(), IDOC_PROPERTY).getValue();

            stQuery.executeUpdate(queryString);

            ocContainer.getContainerProperty(ocGrid.getSelectedRow(), ESTATUS_PROPERTY).setValue(estatus);

            this.fillOcIdexGrid();

            Notification.show("Operación EXITOSA!!!", Notification.Type.HUMANIZED_MESSAGE);

            actualizarBotones();

        } catch (Exception exA) {
            try {
                ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().rollback();
                ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().setAutoCommit(true);
            } catch (SQLException sqle) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error al actualizar TAREA CON ORDEN DE CAMBIO.", sqle);
            }
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error al actualizar TAREA CON ORDEN DE CAMBIO.", exA);
            Notification.show("ERROR DE BASE DE DATOS : " + exA.getMessage(), Notification.Type.ERROR_MESSAGE);
        }

    }

    private void autorizarOC() {

        try {

            ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().setAutoCommit(false);

            actualizarEstatusOC("AUTORIZADA");

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            queryString = "SELECT *, CCC.Descripcion CuentaDescripcion, EMP.Empresa, OCD.Total ToTalItem ";
            queryString += " FROM visita_inspeccion_tarea_oc OC";
            queryString += " INNER JOIN visita_inspeccion_tarea_oc_detalle OCD ON OCD.IdVisitaInspeccionTareaOC = OC.IdVisitaInspeccionTareaOC";
            queryString += " INNER JOIN centro_costo_cuenta CCC ON CCC.CodigoCuentaCentroCosto = OCD.NoCuenta";
            queryString += " INNER JOIN contabilidad_empresa EMP ON EMP.IdEmpresa = OCD.IdEmpresa";
            queryString += " WHERE OC.IdVisitaInspeccionTareaOC  = " + ocContainer.getContainerProperty(ocGrid.getSelectedRow(), IDOC_PROPERTY).getValue();

            rsRecords2 = stQuery2.executeQuery(queryString);

            if (rsRecords2.next()) { //  encontrado

                queryString = "INSERT INTO DetalleItemsCostos ";
                queryString += " (NoCuenta, Descripcion, Precio, Cantidad, Total, IdProject, IDProveedor, ";
                queryString += " IdCC, IdArea, Lote, Moneda, IdEmpresa, Empresa, IDTarea, RazonOC, Unidad, ";
                queryString += " Tipo, FechaIngreso, NOC, OCInicial, IDEXAN, IDEX, CodItemPro, IDVisita) ";
                queryString += " VALUES ";

                do {
                    queryString += "(";
                    queryString += "'" + rsRecords2.getString("NoCuenta") + "'";
                    queryString += ",'" + rsRecords2.getString("CuentaDescripcion") + "'";
                    queryString += ", " + rsRecords2.getString("Precio");
                    queryString += ", " + rsRecords2.getString("Cantidad");
                    queryString += ", " + rsRecords2.getString("TotalItem");
                    queryString += "," + rsRecords2.getString("IdProject");
                    queryString += ", " + rsRecords2.getString("IdProveedor"); //7
                    queryString += ",'" + rsRecords2.getString("IdCC") + "'";
                    queryString += ", " + rsRecords2.getString("IdArea");
                    queryString += "," + rsRecords2.getString("Lote");
                    queryString += ",'" + rsRecords2.getString("Moneda") + "'";
                    queryString += ", " + rsRecords2.getString("IdEmpresa");
                    queryString += ",'" + rsRecords2.getString("Empresa") + "'";
                    queryString += ",'" + codigoTarea + "'";
                    queryString += ",'" + descripcionTarea + "'";
                    queryString += ",'" + rsRecords2.getString("Unidad") + "'"; //16
                    queryString += ",'" + rsRecords2.getString("Tipo") + "'";
                    queryString += ",'" + rsRecords2.getString("Fecha") + "'";
                    queryString += "," +  String.valueOf(ocContainer.getContainerProperty(ocGrid.getSelectedRows().iterator().next(), CODIGOOC_PROPERTY).getValue());
                    queryString += ",0"; //OCInicial
                    queryString += "," + rsRecords2.getString("Idex"); //idexan
                    queryString += ",'" + rsRecords2.getString("Idex") + "'";
                    queryString += ",''"; //coditempro
                    queryString += ",'" + codigoTarea.substring(0, codigoTarea.length()-2) + "'";
                    queryString += "),";

                } while(rsRecords2.next());

                queryString = queryString.substring(0, queryString.length()-1);

                Logger.getLogger(this.getClass().getName()).log(Level.INFO, queryString);

                stQuery.executeUpdate(queryString);

            }

            ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().commit();
            ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().setAutoCommit(true);

            fillOcGrid();

            Notification.show("Operación EXITOSA!!!", Notification.Type.HUMANIZED_MESSAGE);
        } catch (Exception exA) {
            try {
                ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().rollback();
                ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().setAutoCommit(true);
            }
            catch(SQLException sqle) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error al actualizar TAREA CON ORDEN DE CAMBIO.", sqle);
            }
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error al actualizar TAREA CON ORDEN DE CAMBIO.", exA);
            Notification.show("ERROR DE BASE DE DATOS : " + exA.getMessage(), Notification.Type.ERROR_MESSAGE);
        }

    }

    private void eliminarRubro() {

        try {

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            queryString = "DELETE FROM visita_inspeccion_tarea_oc_detalle ";
            queryString += " WHERE IdVisitaInspeccionTareaOCDetalle  = " + ocIdexContainer.getContainerProperty(ocIdexGrid.getSelectedRow(), ID_PROPERTY).getValue();

            stQuery.executeUpdate(queryString);

            ocIdexContainer.removeItem(ocIdexGrid.getSelectedRow());

            Notification.show("Rubro eliminado exitosamente.", Notification.Type.HUMANIZED_MESSAGE);

        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error al eliminar rubro de OC.", ex);
            Notification.show("ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }

}