package com.simpletecno.sopdi.inventario;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.SelectionEvent;
import com.vaadin.event.SelectionEvent.SelectionListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import org.vaadin.dialogs.ConfirmDialog;

/**
 *
 * @author user
 */
public class InvBodegasView extends VerticalLayout implements View {

    public IndexedContainer bodegasContainer = new IndexedContainer();
    Grid bodegasGrid;
    static final String ID_PROPERTY = "Id";
    static final String EMPRESA_PROPERTY = "Empresa";
    static final String NOMBRE_BODEGA_PROPERTY = "Nombre";
    static final String UBICACION_PROPERTY = "Ubicación";
    static final String FECHAULTMOV_PROPERTY = "Ult_Mov";
    static final String ESTATUS_PROPERTY = "Estatus";
    static final String RAZON_PROPERTY = "Razón";

    Grid existenciasGrid;
    public IndexedContainer existenciasContainer = new IndexedContainer();
    //static final String ID_PRODUCTO_PROPERTY = "Id";
    static final String NO_CUENTA_PRODUCTO_PROPERTY = "No Cuenta";
    static final String NOMBRE_PRODUCTO_PROPERTY = "Nombre";
    static final String UNIDAD_PRODUCTO_PROPERTY = "Unidad";
    static final String CANTIDAD_PRODUCTO_PROPERTY = "Cantidad";
    //static final String ESTATUS_PRODUCTO_PROPERTY = "Estatus";

    Grid movimientoGrid;
    public IndexedContainer movimientoContainer = new IndexedContainer();
    //static final String ID_INVENTARIO_PROPERTY = "Id Inventario";
    static final String ID_PRODUCTO_PROPERTY = "Id Producto";
    //static final String DESCRIPCION_PROPERTY = "Nombre";
    static final String TIPO_MOVIMIENTO_PROPERTY = "Tipo Movimiento";
    static final String FECHA_MOVIMIENTO_PROPERTY = "Fecha Movimiento";
    //static final String CANTIDAD_PROPERTY = "Cantidad";
    //static final String ESTATUS_MOVIMIENTO_PROPERTY = "Estatus";
    static final String CREADO_USUARIO_PROPERTY = "Creado Usuario";

    Button editBtn;
    Button newBtn;

    Button editarMovBtn;
    Button nuevoMovBtn;
    Button buscarMovBtn;
    Button eliminarMovBtn;

    UI mainUI;
    Statement stQuery, stQuery2;
    ResultSet rsRecords, rsRecords2;
    String queryString;

    TabSheet tabSheet;

    ComboBox productoCbx;
    //ComboBox bodegaCbx;

    CheckBox todoCheck;
    CheckBox entradaCheck;
    CheckBox salidaCheck;

    DateField desdeDt;
    DateField hastaDt;

    VerticalLayout mainLayout = new VerticalLayout();

    public InvBodegasView() {

        this.mainUI = UI.getCurrent();
        setWidth("100%");
        setMargin(false);
        setSpacing(true);

        Label titleLbl = new Label("INVENTARIO DE BODEGAS");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h1_custom");

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setResponsive(true);
        titleLayout.setWidth("100%");
        titleLayout.setMargin(new MarginInfo(false, true, false, true));
        titleLayout.addComponent(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.TOP_RIGHT);

        addComponent(titleLayout);
        setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        createBodegasGrid();
        createVisitasTabSheet();

        fillBodegasGrid();
    }

    public void createBodegasGrid() {

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

        bodegasContainer.addContainerProperty(ID_PROPERTY, String.class, null);
        bodegasContainer.addContainerProperty(EMPRESA_PROPERTY, String.class, null);
        bodegasContainer.addContainerProperty(NOMBRE_BODEGA_PROPERTY, String.class, null);
        bodegasContainer.addContainerProperty(UBICACION_PROPERTY, String.class, null);
        bodegasContainer.addContainerProperty(FECHAULTMOV_PROPERTY, String.class, null);
        bodegasContainer.addContainerProperty(ESTATUS_PROPERTY, String.class, null);
        bodegasContainer.addContainerProperty(RAZON_PROPERTY, String.class, null);

        bodegasGrid = new Grid("", bodegasContainer);
        bodegasGrid.setWidth("100%");
        bodegasGrid.setImmediate(true);
        bodegasGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        bodegasGrid.setDescription("Seleccione un registro.");
        bodegasGrid.setHeightMode(HeightMode.ROW);
        bodegasGrid.setHeightByRows(3);
        bodegasGrid.setResponsive(true);
        bodegasGrid.setEditorBuffered(false);

        bodegasGrid.getColumn(ID_PROPERTY).setHidable(true).setHidden(true);

        bodegasGrid.getColumn(ID_PROPERTY).setExpandRatio(1);
        bodegasGrid.getColumn(EMPRESA_PROPERTY).setExpandRatio(1);
        bodegasGrid.getColumn(NOMBRE_BODEGA_PROPERTY).setExpandRatio(3);
        bodegasGrid.getColumn(UBICACION_PROPERTY).setExpandRatio(3);
        bodegasGrid.getColumn(FECHAULTMOV_PROPERTY).setExpandRatio(1);
        bodegasGrid.getColumn(ESTATUS_PROPERTY).setExpandRatio(1);
        bodegasGrid.getColumn(RAZON_PROPERTY).setExpandRatio(2);

        bodegasGrid.addSelectionListener(new SelectionListener() {
            @Override
            public void select(SelectionEvent event
            ) {
                if (bodegasGrid.getSelectedRow() != null) {
                    String idBodega = String.valueOf(bodegasContainer.getContainerProperty(bodegasGrid.getSelectedRow(), ID_PROPERTY).getValue());
                    llenarTablaExistenias(idBodega);
                    llenarTablaMovimientos(idBodega);
                }
            }
        });

        layoutGrid.addComponent(bodegasGrid);
        layoutGrid.setComponentAlignment(bodegasGrid, Alignment.TOP_CENTER);
        reportLayout.addComponent(layoutGrid);

        newBtn = new Button("Nueva bodega");
        newBtn.setIcon(FontAwesome.PLUS_CIRCLE);
        newBtn.setWidth(190, Sizeable.UNITS_PIXELS);
        newBtn.setDescription("Registrar nueva bodega");
        newBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                BodegasForm invBodegaForm = new BodegasForm("");
                mainUI.addWindow(invBodegaForm);
                invBodegaForm.center();

            }
        });

        editBtn = new Button("Editar");
        editBtn.setIcon(FontAwesome.EDIT);
        editBtn.setWidth(190, Sizeable.UNITS_PIXELS);
        editBtn.setDescription("Actualizar datos de bodega");
        editBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (bodegasContainer.size() == 0) {
                    Notification.show("POR FAVOR SELECCIONE UNA BODEGA.");
                    return;
                }
                if (bodegasGrid.getSelectedRow() != null) {
                    BodegasForm invBodegaForm = new BodegasForm(String.valueOf(bodegasContainer.getContainerProperty(bodegasGrid.getSelectedRow(), ID_PROPERTY).getValue()));
                    mainUI.addWindow(invBodegaForm);
                    invBodegaForm.center();
                }
            }
        });

        Button eliminarBtn = new Button("Eliminar");
        eliminarBtn.setIcon(FontAwesome.TRASH);
        eliminarBtn.setWidth(190, Sizeable.UNITS_PIXELS);
        eliminarBtn.setDescription("Eliminar cuenta.");
        eliminarBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (bodegasGrid.getSelectedRow() == null) {
                    Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
                } else {
                    try {

                        queryString = " SELECT * FROM inv_movimiento";
                        queryString += " WHERE IdBodega = " + String.valueOf(bodegasContainer.getContainerProperty(bodegasGrid.getSelectedRow(), ID_PROPERTY).getValue());

                        stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                        rsRecords = stQuery.executeQuery(queryString);

                        if (rsRecords.next()) {
                            Notification notif = new Notification("La bodega no puede ser eliminada por que contiene movimientos..", Notification.Type.WARNING_MESSAGE);
                            notif.setDelayMsec(1500);
                            notif.setIcon(FontAwesome.WARNING);
                            notif.setPosition(Position.MIDDLE_CENTER);
                            notif.show(Page.getCurrent());
                        } else {

                            queryString = " DELETE FROM inv_bodega";
                            queryString += " WHERE IdBodega = " + String.valueOf(bodegasContainer.getContainerProperty(bodegasGrid.getSelectedRow(), ID_PROPERTY).getValue());

                            stQuery.executeUpdate(queryString);

                            Notification notif = new Notification("Registro eliminado con exito!.", Notification.Type.HUMANIZED_MESSAGE);
                            notif.setDelayMsec(1500);
                            notif.setPosition(Position.MIDDLE_CENTER);
                            notif.show(Page.getCurrent());

                            bodegasContainer.removeItem(bodegasGrid.getSelectedRow());
                            bodegasGrid.select(null);
                        }

                    } catch (SQLException ex) {
                        System.out.println("Error al eliminar registro de inv_bodega" + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            }
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.addComponent(editBtn);
        buttonsLayout.addComponent(newBtn);
        buttonsLayout.addComponent(eliminarBtn);
        buttonsLayout.setComponentAlignment(editBtn, Alignment.BOTTOM_LEFT);
        buttonsLayout.setComponentAlignment(newBtn, Alignment.BOTTOM_CENTER);
        buttonsLayout.setComponentAlignment(eliminarBtn, Alignment.BOTTOM_RIGHT);

        reportLayout.addComponent(buttonsLayout);
        reportLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);

        addComponent(reportLayout);
        setComponentAlignment(reportLayout, Alignment.MIDDLE_CENTER);
    }

    private void createVisitasTabSheet() {
        tabSheet = new TabSheet();
        tabSheet.addStyleName(ValoTheme.TABSHEET_FRAMED);
        tabSheet.addStyleName(ValoTheme.TABSHEET_PADDED_TABBAR);
        tabSheet.addStyleName(ValoTheme.TABSHEET_PADDED_TABBAR);
        tabSheet.addStyleName(ValoTheme.TABSHEET_CENTERED_TABS);
        tabSheet.addStyleName(ValoTheme.TABSHEET_EQUAL_WIDTH_TABS);
        tabSheet.addSelectedTabChangeListener(new TabSheet.SelectedTabChangeListener() {
            @Override
            public void selectedTabChange(TabSheet.SelectedTabChangeEvent event) {

                TabSheet tabsheet = event.getTabSheet();

                if (!tabSheet.getSelectedTab().getClass().getName().equals("com.vaadin.ui.Label")) {
                    Layout tab = (Layout) tabsheet.getSelectedTab();

                    String caption = tabsheet.getTab(tab).getCaption();

                    System.out.println("\nTab Caption = " + caption);

                    /*  if (caption.contains("Existencias")) {
                        llenarTablaExistenias(String.valueOf(bodegasContainer.getContainerProperty(bodegasGrid.getSelectedRow(), BODEGAID_PROPERTY).getValue()));
                    }

                    if (caption.contains("Movimientos")) {
                        llenarTablaMovimientos(String.valueOf(bodegasContainer.getContainerProperty(bodegasGrid.getSelectedRow(), BODEGAID_PROPERTY).getValue()));
                    }
                     */
                }
            }
        });

        addTabExistencias();
        addTabMovimientos();
        addComponent(tabSheet);
    }

    private void addTabExistencias() {
        VerticalLayout layoutExistencias = new VerticalLayout();
        layoutExistencias.setSpacing(true);
        layoutExistencias.setMargin(true);
        layoutExistencias.setWidth(("100%"));

        existenciasContainer.addContainerProperty(ID_PRODUCTO_PROPERTY, String.class, null);
        existenciasContainer.addContainerProperty(NO_CUENTA_PRODUCTO_PROPERTY, String.class, null);
        existenciasContainer.addContainerProperty(NOMBRE_PRODUCTO_PROPERTY, String.class, null);
        existenciasContainer.addContainerProperty(UNIDAD_PRODUCTO_PROPERTY, String.class, null);
        existenciasContainer.addContainerProperty(CANTIDAD_PRODUCTO_PROPERTY, String.class, null);
        existenciasContainer.addContainerProperty(ESTATUS_PROPERTY, String.class, null);

        existenciasGrid = new Grid("Listado de productos existentes", existenciasContainer);
        existenciasGrid.setWidth("100%");
        existenciasGrid.setImmediate(true);
        existenciasGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        existenciasGrid.setDescription("Seleccione un registro.");
        existenciasGrid.setHeightMode(HeightMode.ROW);
        existenciasGrid.setHeightByRows(10);
        existenciasGrid.setResponsive(true);
        existenciasGrid.setEditorBuffered(false);

        existenciasGrid.getColumn(ID_PRODUCTO_PROPERTY).setHidden(true).setHidable(true);

        existenciasGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if (CANTIDAD_PRODUCTO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else {
                return null;
            }

        });

        Grid.HeaderRow filterRow = existenciasGrid.appendHeaderRow();
        Grid.HeaderCell cell = filterRow.getCell(NO_CUENTA_PRODUCTO_PROPERTY);
        TextField filterField = new TextField();
        filterField.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField.setInputPrompt("Filtrar");
        filterField.setColumns(10);

        filterField.addTextChangeListener(change -> {
            existenciasContainer.removeContainerFilters(NO_CUENTA_PRODUCTO_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                existenciasContainer.addContainerFilter(
                        new SimpleStringFilter(NO_CUENTA_PRODUCTO_PROPERTY,
                                change.getText(), true, false));
            }
            //   setTotal();
        }
        );
        cell.setComponent(filterField);

        Grid.HeaderCell cell2 = filterRow.getCell(NOMBRE_PRODUCTO_PROPERTY);
        TextField filterField2 = new TextField();
        filterField2.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField2.setInputPrompt("Filtrar");
        filterField2.setColumns(10);

        filterField2.addTextChangeListener(change -> {
            existenciasContainer.removeContainerFilters(NOMBRE_PRODUCTO_PROPERTY);
            if (!change.getText().isEmpty()) {
                existenciasContainer.addContainerFilter(
                        new SimpleStringFilter(NOMBRE_PRODUCTO_PROPERTY,
                                change.getText(), true, false));
            }
            // setTotal();
        }
        );
        cell2.setComponent(filterField2);

        layoutExistencias.addComponents(existenciasGrid);
        layoutExistencias.setComponentAlignment(existenciasGrid, Alignment.TOP_CENTER);

        TabSheet.Tab newTab = tabSheet.addTab(layoutExistencias, "Existencias");
        newTab.setIcon(FontAwesome.CLIPBOARD);
        newTab.setId("1");
    }

    private void addTabMovimientos() {

        VerticalLayout layoutMovimientos = new VerticalLayout();
        layoutMovimientos.setSpacing(true);
        layoutMovimientos.setMargin(true);
        layoutMovimientos.setWidth(("100%"));

        HorizontalLayout lineaLayout1 = new HorizontalLayout();
        lineaLayout1.setSpacing(true);

        productoCbx = new ComboBox("Producto");
        productoCbx.setWidth("15em");
        productoCbx.setFilteringMode(FilteringMode.CONTAINS);

        todoCheck = new CheckBox("Todo");
        todoCheck.addStyleName(ValoTheme.CHECKBOX_LARGE);
        todoCheck.setValue(true);
        todoCheck.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (todoCheck.getValue() == true) {
                    entradaCheck.setValue(false);
                    salidaCheck.setValue(false);
                }
            }
        });

        entradaCheck = new CheckBox("Entrada");
        entradaCheck.addStyleName(ValoTheme.CHECKBOX_LARGE);
        entradaCheck.setValue(false);
        entradaCheck.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (entradaCheck.getValue() == true) {
                    todoCheck.setValue(false);
                    salidaCheck.setValue(false);
                }
            }
        });

        salidaCheck = new CheckBox("Salida");
        salidaCheck.addStyleName(ValoTheme.CHECKBOX_LARGE);
        salidaCheck.setValue(false);
        salidaCheck.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (salidaCheck.getValue() == true) {
                    todoCheck.setValue(false);
                    entradaCheck.setValue(false);
                }
            }
        });

        desdeDt = new DateField("Desde:");
        desdeDt.setWidth("10em");
        desdeDt.setDateFormat("dd/MM/yyyy");
        Date primerDia = Utileria.getPrimerDiaDelMes();
        desdeDt.setValue(primerDia);

        hastaDt = new DateField("Hasta:");
        hastaDt.setWidth("10em");
        hastaDt.setDateFormat("dd/MM/yyyy");
        Date ultimoDia = Utileria.getUltimoDiaDelMes();
        hastaDt.setValue(ultimoDia);

        buscarMovBtn = new Button("");
        buscarMovBtn.setIcon(FontAwesome.SEARCH);
        buscarMovBtn.setWidth(100, Sizeable.UNITS_PIXELS);
        buscarMovBtn.setDescription("Buscar movimiento..");
        buscarMovBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (bodegasGrid.getSelectedRow() != null) {
                    llenarTablaMovimientos(String.valueOf(bodegasContainer.getContainerProperty(bodegasGrid.getSelectedRow(), ID_PROPERTY).getValue()));
                }
            }
        });
        llenarComboProducto();

        lineaLayout1.addComponents(productoCbx, desdeDt, hastaDt, todoCheck, entradaCheck, salidaCheck, buscarMovBtn);
        lineaLayout1.setComponentAlignment(todoCheck, Alignment.BOTTOM_RIGHT);
        lineaLayout1.setComponentAlignment(entradaCheck, Alignment.BOTTOM_RIGHT);
        lineaLayout1.setComponentAlignment(salidaCheck, Alignment.BOTTOM_RIGHT);
        lineaLayout1.setComponentAlignment(buscarMovBtn, Alignment.BOTTOM_RIGHT);

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);

        nuevoMovBtn = new Button("Nuevo Movimiento");
        nuevoMovBtn.setDescription("Registrar nuevo movimiento");
        nuevoMovBtn.setIcon(FontAwesome.PLUS_CIRCLE);
        nuevoMovBtn.setWidth(190, Sizeable.UNITS_PIXELS);
        nuevoMovBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                //  if (movimientoGrid.getSelectedRow() != null) {
                MovimientoForm movimientoForm = new MovimientoForm("");
                UI.getCurrent().addWindow(movimientoForm);
                movimientoForm.bodegaCbx.select(String.valueOf(bodegasContainer.getContainerProperty(bodegasGrid.getSelectedRow(), ID_PROPERTY).getValue()));
                movimientoForm.center();
                //   } else {
                //Notification notif = new Notification("Por favor, seleccione uno de los productos para agregar un nuevo movimiento!.", Notification.Type.WARNING_MESSAGE);
                //notif.setDelayMsec(1500);
                //notif.setPosition(Position.MIDDLE_CENTER);
                //notif.show(Page.getCurrent());
                //     }
            }
        });

        eliminarMovBtn = new Button("Eliminar");
        eliminarMovBtn.setIcon(FontAwesome.TRASH);
        eliminarMovBtn.setWidth(190, Sizeable.UNITS_PIXELS);
        eliminarMovBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (movimientoGrid.getSelectedRow() != null) {
                    ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de eliminar este movimiento?",
                            "SI", "NO", new ConfirmDialog.Listener() {
                        public void onClose(ConfirmDialog dialog) {
                            if (dialog.isConfirmed()) {
                                queryString = "DELETE FROM inv_movimiento";
                                queryString += " WHERE IdInventario = " + movimientoContainer.getContainerProperty(movimientoGrid.getSelectedRow(), ID_PROPERTY).getValue();

                                try {
                                    stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                                    stQuery.executeUpdate(queryString);

                                    movimientoContainer.removeItem(movimientoGrid.getSelectedRow());
                                    movimientoGrid.select(null);

                                } catch (Exception e) {
                                    System.out.println("Error al intentar eliminar el movimiento" + e);
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                } else {
                    Notification notif = new Notification("Por favor, seleccione el registro que desea eliminar.. ", Notification.Type.WARNING_MESSAGE);
                    notif.setDelayMsec(1500);
                    notif.setPosition(Position.MIDDLE_CENTER);
                    notif.setIcon(FontAwesome.WARNING);
                    notif.show(Page.getCurrent());
                }
            }
        });

        editarMovBtn = new Button("Editar");
        editarMovBtn.setIcon(FontAwesome.EDIT);
        editarMovBtn.setWidth(190, Sizeable.UNITS_PIXELS);
        editarMovBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (movimientoGrid.getSelectedRow() != null) {
                    MovimientoForm movimientoForm = new MovimientoForm(String.valueOf(movimientoContainer.getContainerProperty(movimientoGrid.getSelectedRow(), ID_PROPERTY).getValue()));
                    UI.getCurrent().addWindow(movimientoForm);
                    movimientoForm.center();
                } else {
                    Notification notif = new Notification("Por favor, seleccione el movimiento que desea editar.", Notification.Type.WARNING_MESSAGE);
                    notif.setDelayMsec(1500);
                    notif.setPosition(Position.MIDDLE_CENTER);
                    notif.setIcon(FontAwesome.WARNING);
                    notif.show(Page.getCurrent());
                }
            }
        });

        movimientoContainer.addContainerProperty(ID_PROPERTY, String.class, null);
        movimientoContainer.addContainerProperty(ID_PRODUCTO_PROPERTY, String.class, null);
        movimientoContainer.addContainerProperty(NO_CUENTA_PRODUCTO_PROPERTY, String.class, null);
        movimientoContainer.addContainerProperty(NOMBRE_PRODUCTO_PROPERTY, String.class, null);
        movimientoContainer.addContainerProperty(TIPO_MOVIMIENTO_PROPERTY, String.class, null);
        movimientoContainer.addContainerProperty(FECHA_MOVIMIENTO_PROPERTY, String.class, null);
        movimientoContainer.addContainerProperty(CANTIDAD_PRODUCTO_PROPERTY, String.class, null);
        movimientoContainer.addContainerProperty(RAZON_PROPERTY, String.class, null);
        movimientoContainer.addContainerProperty(CREADO_USUARIO_PROPERTY, String.class, null);

        movimientoGrid = new Grid("Listado de movimientos", movimientoContainer);
        movimientoGrid.setWidth("100%");
        movimientoGrid.setImmediate(true);
        movimientoGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        movimientoGrid.setDescription("Seleccione un registro.");
        movimientoGrid.setHeightMode(HeightMode.ROW);
        movimientoGrid.setHeightByRows(10);
        movimientoGrid.setResponsive(true);
        movimientoGrid.setEditorBuffered(false);

        movimientoGrid.getColumn(ID_PROPERTY).setHidden(true).setHidable(true);
        movimientoGrid.getColumn(ID_PRODUCTO_PROPERTY).setHidden(true).setHidable(true);

        movimientoGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if (CANTIDAD_PRODUCTO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else {
                return null;
            }

        });

        buttonsLayout.addComponents(editarMovBtn, nuevoMovBtn, eliminarMovBtn);
        buttonsLayout.setComponentAlignment(editarMovBtn, Alignment.TOP_LEFT);
        buttonsLayout.setComponentAlignment(nuevoMovBtn, Alignment.TOP_CENTER);
        buttonsLayout.setComponentAlignment(eliminarMovBtn, Alignment.TOP_RIGHT);

        layoutMovimientos.addComponents(lineaLayout1, movimientoGrid, buttonsLayout);
        layoutMovimientos.setComponentAlignment(lineaLayout1, Alignment.TOP_LEFT);
        layoutMovimientos.setComponentAlignment(movimientoGrid, Alignment.TOP_CENTER);
        layoutMovimientos.setComponentAlignment(buttonsLayout, Alignment.TOP_CENTER);

        TabSheet.Tab newTab = tabSheet.addTab(layoutMovimientos, "Movimientos");
        newTab.setIcon(FontAwesome.ARROWS_ALT);
        newTab.setId("2");
    }

    public void fillBodegasGrid() {
        bodegasContainer.removeAllItems();

        queryString = "SELECT BOD.*, EMP.Empresa EmpresaNombre";
        queryString += " FROM inv_bodega BOD";
        queryString += " INNER JOIN contabilidad_empresa EMP ON EMP.IdEmpresa = BOD.IdEmpresa ";
        queryString += " WHERE EMP.IdEmpresa =  " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado                                                
                do {

                    Object itemId = bodegasContainer.addItem();

                    bodegasContainer.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords.getString("IdBodega"));
                    bodegasContainer.getContainerProperty(itemId, EMPRESA_PROPERTY).setValue(rsRecords.getString("EmpresaNombre"));
                    bodegasContainer.getContainerProperty(itemId, NOMBRE_BODEGA_PROPERTY).setValue(rsRecords.getString("Nombre"));
                    bodegasContainer.getContainerProperty(itemId, UBICACION_PROPERTY).setValue(rsRecords.getString("Ubicacion"));
                    if (rsRecords.getObject("FechaUltimoMovimiento") != null) {
                        bodegasContainer.getContainerProperty(itemId, FECHAULTMOV_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("FechaUltimoMovimiento")));
                    } else {
                        bodegasContainer.getContainerProperty(itemId, FECHAULTMOV_PROPERTY).setValue("");
                    }
                    bodegasContainer.getContainerProperty(itemId, ESTATUS_PROPERTY).setValue(rsRecords.getString("Estatus"));
                    bodegasContainer.getContainerProperty(itemId, RAZON_PROPERTY).setValue(rsRecords.getString("Razon"));
                } while (rsRecords.next());

            }

            bodegasGrid.select(bodegasContainer.firstItemId());

        } catch (Exception ex) {
            System.out.println("Error al listar tabla BODEGAS:" + ex.getMessage());
            ex.printStackTrace();
            Notification.show("ERROR DE BASE DE BODEGAS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }

    public void llenarTablaMovimientos(String bodegaId) {

        movimientoContainer.removeAllItems();

        try {

            queryString = "  SELECT *, usuario.Nombre AS CreadoUsuario, inv_producto.Descripcion AS NombreProducto, inv_producto.Estatus ";
            queryString += " FROM inv_movimiento MOV ";
            queryString += " INNER JOIN inv_producto on MOV.IdProducto = inv_producto.IdProducto ";
            queryString += " INNER JOIN usuario on MOV.IdUsuario = usuario.IdUsuario ";
            queryString += " WHERE MOV.IdBodega = " + bodegaId;
            queryString += " AND MOV.FechaMovimiento between '" + Utileria.getFechaYYYYMMDD_1(desdeDt.getValue()) + "'";
            queryString += " AND '" + Utileria.getFechaYYYYMMDD_1(hastaDt.getValue()) + "'";
            if (todoCheck.getValue() == true) {
                queryString += " AND MOV.TipoMovimiento IN ('ENTRADA','SALIDA')";
            } else if (entradaCheck.getValue() == true) {
                queryString += " AND MOV.TipoMovimiento ='ENTRADA'";
            } else if (salidaCheck.getValue() == true) {
                queryString += " AND MOV.TipoMovimiento ='SALIDA'";
            }
            if (productoCbx.getValue() != null) {
                queryString += " AND MOV.IdProducto = " + productoCbx.getValue();
            }

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado

                do {
                    Object itemId = movimientoContainer.addItem();
                    movimientoContainer.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords.getString("IdInventario"));
                    movimientoContainer.getContainerProperty(itemId, ID_PRODUCTO_PROPERTY).setValue(rsRecords.getString("MOV.IdProducto"));
                    movimientoContainer.getContainerProperty(itemId, NOMBRE_PRODUCTO_PROPERTY).setValue(rsRecords.getString("NombreProducto"));
                    movimientoContainer.getContainerProperty(itemId, TIPO_MOVIMIENTO_PROPERTY).setValue(rsRecords.getString("TipoMovimiento"));
                    movimientoContainer.getContainerProperty(itemId, FECHA_MOVIMIENTO_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("FechaMovimiento")));
                    movimientoContainer.getContainerProperty(itemId, CANTIDAD_PRODUCTO_PROPERTY).setValue(String.valueOf(rsRecords.getInt("Cantidad")));
                    movimientoContainer.getContainerProperty(itemId, RAZON_PROPERTY).setValue(rsRecords.getString("Razon"));
                    movimientoContainer.getContainerProperty(itemId, CREADO_USUARIO_PROPERTY).setValue(rsRecords.getString("CreadoUsuario"));

                } while (rsRecords.next());

            }
        } catch (Exception ex) {
            System.out.println("Error al listar tabla movimientos de bodega : " + ex);
            ex.printStackTrace();
        }
    }

    public void llenarTablaExistenias(String bodegaId) {

        try {
            existenciasContainer.removeAllItems();

            queryString = " SELECT MOV.IdProducto, PROD.NoCuenta, PROD.Descripcion, SUM(Cantidad) As Entradas,";
            queryString += " PROD.Unidad, PROD.Estatus	";
            queryString += " FROM inv_movimiento MOV";
            queryString += " INNER JOIN inv_producto PROD ON PROD.IdProducto = MOV.IdProducto";
            queryString += " WHERE MOV.IdBodega = " + bodegaId;
            queryString += " AND MOV.TipoMovimiento = 'ENTRADA'";
            queryString += " GROUP BY MOV.IdProducto";

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery2 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado

                do {

                    queryString = " SELECT SUM(Cantidad) as Salidas";
                    queryString += " FROM inv_movimiento MOV";
                    queryString += " WHERE MOV.IdBodega = " + bodegaId;
                    queryString += " AND MOV.TipoMovimiento = 'SALIDA'";
                    queryString += " AND IdProducto = " + rsRecords.getString("IdProducto");

                    rsRecords2 = stQuery2.executeQuery(queryString);

                    Object itemId = existenciasContainer.addItem();
                    existenciasContainer.getContainerProperty(itemId, ID_PRODUCTO_PROPERTY).setValue(rsRecords.getString("MOV.IdProducto"));
                    existenciasContainer.getContainerProperty(itemId, NO_CUENTA_PRODUCTO_PROPERTY).setValue(rsRecords.getString("NoCuenta"));
                    existenciasContainer.getContainerProperty(itemId, NOMBRE_PRODUCTO_PROPERTY).setValue(rsRecords.getString("PROD.Descripcion"));
                    existenciasContainer.getContainerProperty(itemId, UNIDAD_PRODUCTO_PROPERTY).setValue(rsRecords.getString("Unidad"));
                    existenciasContainer.getContainerProperty(itemId, ESTATUS_PROPERTY).setValue(rsRecords.getString("PROD.Estatus"));

                    if (rsRecords2.next()) { // SI HAY SALIDAS ENTONCES RESTAR 
                        existenciasContainer.getContainerProperty(itemId, CANTIDAD_PRODUCTO_PROPERTY).setValue(String.valueOf(rsRecords.getDouble("Entradas") - rsRecords2.getDouble("Salidas")));
                    } else {
                        existenciasContainer.getContainerProperty(itemId, CANTIDAD_PRODUCTO_PROPERTY).setValue(rsRecords.getString("Entradas"));
                    }

                } while (rsRecords.next());
            }
        } catch (Exception ex) {
            System.out.println("Error al llenar tabla existencias : " + ex);
            ex.printStackTrace();
        }
    }

    public void llenarComboProducto() {
        queryString = " SELECT *";
        queryString += " FROM inv_producto";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) { //  encontrado                
                productoCbx.addItem(rsRecords.getString("IdProducto"));
                productoCbx.setItemCaption(rsRecords.getString("IdProducto"), rsRecords.getString("Descripcion"));
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar productos: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - Inventario de Bodegas");
    }
}
