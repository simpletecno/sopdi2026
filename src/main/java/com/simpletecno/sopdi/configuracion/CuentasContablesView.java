package com.simpletecno.sopdi.configuracion;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.contabilidad.SaldosCuentasForm;
import com.vaadin.addon.tableexport.DefaultTableHolder;
import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.addon.tableexport.TableHolder;
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
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.HeaderCell;
import com.vaadin.ui.Grid.HeaderRow;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.themes.ValoTheme;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author user
 */
public class CuentasContablesView extends VerticalLayout implements View {

    static final String ID_NOMENCLATURA_PROPERTY = "Id";
    static final String REPORTE_PROPERTY = "REPORTE";
    static final String ID1_PROPERTY = "ID1";
    static final String N1_PROPERTY = "N1";
    static final String ID2_PROPERTY = "ID2";
    static final String N2_PROPERTY = "N2";
    static final String ID3_PROPERTY = "ID3";
    static final String N3_PROPERTY = "N3";
    static final String ID4_PROPERTY = "ID4";
    static final String N4_PROPERTY = "N4";
    static final String ID5_PROPERTY = "ID5";
    static final String NO_CUENTA_PROPERTY = "NoCuenta";
    static final String N5_PROPERTY = "N5";
    static final String ESTATUS_PROPERTY = "Estatus";
    static final String SALDO_PROPERTY = "Saldo de cuenta";

    public IndexedContainer container = new IndexedContainer();
    Grid cuentasContablesGrid;

    UI mainUI;
    Statement stQuery;
    ResultSet rsRecords;
    String queryString;
    Button inhabilitarBtn;
    Button exportExcelBtn;

    public CuentasContablesView() {

        Responsive.makeResponsive(this);
        setMargin(true);
        setSpacing(true);
        this.mainUI = UI.getCurrent();

        Label titleLbl = new Label("Cuentas contables");
        titleLbl.addStyleName(ValoTheme.LABEL_H1);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h1_custom");

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setMargin(true);
        titleLayout.setSpacing(true);
        titleLayout.setWidth("100%");
        titleLayout.addComponents(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.TOP_LEFT);

        addComponent(titleLayout);
        setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        createTablaCuentasContables();
        llenarTablaCuentas();

        createButtons();

    }

    public void createTablaCuentasContables() {
        HorizontalLayout reportLayout = new HorizontalLayout();
        reportLayout.setWidth("98%");
        reportLayout.addStyleName("rcorners3");
        reportLayout.setResponsive(true);
        reportLayout.setMargin(true);

        container.addContainerProperty(ID_NOMENCLATURA_PROPERTY, String.class, null);
        container.addContainerProperty(REPORTE_PROPERTY, String.class, null);
        container.addContainerProperty(ID1_PROPERTY, String.class, null);
        container.addContainerProperty(N1_PROPERTY, String.class, null);
        container.addContainerProperty(ID2_PROPERTY, String.class, null);
        container.addContainerProperty(N2_PROPERTY, String.class, null);
        container.addContainerProperty(ID3_PROPERTY, String.class, null);
        container.addContainerProperty(N3_PROPERTY, String.class, null);
        container.addContainerProperty(ID4_PROPERTY, String.class, null);
        container.addContainerProperty(N4_PROPERTY, String.class, null);
        container.addContainerProperty(ID5_PROPERTY, String.class, null);
        container.addContainerProperty(NO_CUENTA_PROPERTY, String.class, null);
        container.addContainerProperty(N5_PROPERTY, String.class, null);
        container.addContainerProperty(ESTATUS_PROPERTY, String.class, null);
        container.addContainerProperty(SALDO_PROPERTY, String.class, null);

        cuentasContablesGrid = new Grid("Listado de nomenclaturas", container);
        cuentasContablesGrid.setImmediate(true);
        cuentasContablesGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        cuentasContablesGrid.setDescription("Seleccione un registro.");
        cuentasContablesGrid.setHeightMode(HeightMode.ROW);
        cuentasContablesGrid.setHeightByRows(10);
        cuentasContablesGrid.setWidth("100%");
        cuentasContablesGrid.setResponsive(true);
        cuentasContablesGrid.setEditorBuffered(false);

        cuentasContablesGrid.getColumn(ID_NOMENCLATURA_PROPERTY).setHidable(true).setHidden(true);

        cuentasContablesGrid.addSelectionListener(new SelectionListener() {
            @Override
            public void select(SelectionEvent event) {
                if (cuentasContablesGrid.getSelectedRow() != null) {

                    if (String.valueOf(cuentasContablesGrid.getContainerDataSource().getItem(cuentasContablesGrid.getSelectedRow()).getItemProperty(ESTATUS_PROPERTY).getValue()).equals("HABILITADA")) {
                        inhabilitarBtn.setCaption("Inhabilitar");
                        inhabilitarBtn.setIcon(FontAwesome.HAND_O_DOWN);
                    } else {
                        inhabilitarBtn.setCaption("Habilitar");
                        inhabilitarBtn.setIcon(FontAwesome.HAND_O_UP);
                    }
                }
            }
        });

        cuentasContablesGrid.getColumn(SALDO_PROPERTY).setRenderer(new ButtonRenderer(e
                -> {
            if (container.getContainerProperty(e.getItemId(), SALDO_PROPERTY).getValue().equals("Ver saldo")) {
                String idNomenclatura = String.valueOf(container.getContainerProperty(e.getItemId(), ID_NOMENCLATURA_PROPERTY).getValue());

                cuentasContablesGrid.select(e.getItemId());

                SaldosCuentasForm saldos
                        = new SaldosCuentasForm(idNomenclatura);
                UI.getCurrent().addWindow(saldos);
                saldos.center();
            }
        }));

        HeaderRow filterRow = cuentasContablesGrid.appendHeaderRow();

        HeaderCell cell = filterRow.getCell(NO_CUENTA_PROPERTY);
        TextField filterField = new TextField();
        filterField.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField.setInputPrompt("Filtrar");
        filterField.setColumns(10);

        filterField.addTextChangeListener(change -> {
            container.removeContainerFilters(NO_CUENTA_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(
                        new SimpleStringFilter(NO_CUENTA_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell.setComponent(filterField);

        HeaderCell cell0 = filterRow.getCell(N5_PROPERTY);
        TextField filterField0 = new TextField();
        filterField0.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField0.setInputPrompt("Filtrar");
        filterField0.setColumns(8);

        filterField0.addTextChangeListener(change -> {
            container.removeContainerFilters(N5_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(
                        new SimpleStringFilter(N5_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell0.setComponent(filterField0);

        HeaderCell cell1 = filterRow.getCell(N1_PROPERTY);
        TextField filterField01 = new TextField();
        filterField01.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField01.setInputPrompt("Filtrar");
        filterField01.setColumns(8);

        filterField01.addTextChangeListener(change -> {
            container.removeContainerFilters(N1_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(
                        new SimpleStringFilter(N1_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell1.setComponent(filterField01);

        HeaderCell cell11 = filterRow.getCell(N2_PROPERTY);
        TextField filterField011 = new TextField();
        filterField011.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField011.setInputPrompt("Filtrar");
        filterField011.setColumns(8);

        filterField011.addTextChangeListener(change -> {
            container.removeContainerFilters(N2_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(
                        new SimpleStringFilter(N2_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell11.setComponent(filterField011);

        HeaderCell cellN4 = filterRow.getCell(N4_PROPERTY);
        TextField filterFieldN4 = new TextField();
        filterFieldN4.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterFieldN4.setInputPrompt("Filtrar");
        filterFieldN4.setColumns(8);

        filterFieldN4.addTextChangeListener(change -> {
            container.removeContainerFilters(N4_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(
                        new SimpleStringFilter(N4_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cellN4.setComponent(filterFieldN4);

        reportLayout.addComponent(cuentasContablesGrid);
        reportLayout.setComponentAlignment(cuentasContablesGrid, Alignment.MIDDLE_CENTER);

        addComponent(reportLayout);
        setComponentAlignment(reportLayout, Alignment.TOP_CENTER);
    }

    private void createButtons() {

        Button editBtn = new Button("Editar");
        editBtn.setIcon(FontAwesome.EDIT);
        editBtn.setWidth(140, Sizeable.UNITS_PIXELS);
        editBtn.setDescription("Actualizar datos");
        editBtn.addListener((Button.ClickListener) (Button.ClickEvent event) -> {
            try {
                if (cuentasContablesGrid.getSelectedRow() == null) {
                    Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
                } else {
                    CuentasContablesForm cuentasForm = new CuentasContablesForm();
                    cuentasForm.idNomenclaturaEdit = String.valueOf(container.getContainerProperty(cuentasContablesGrid.getSelectedRow(), ID_NOMENCLATURA_PROPERTY).getValue());
                    cuentasForm.llenarCampos();
                    UI.getCurrent().addWindow(cuentasForm);
                }
                cuentasContablesGrid.select(null);
            } catch (Exception ex) {
                System.out.println("Error en el boton editar cuenta" + ex);
            }
        });

        Button newBtn = new Button("Nueva");
        newBtn.setIcon(FontAwesome.PLUS);
        newBtn.setWidth(140, Sizeable.UNITS_PIXELS);
        newBtn.setDescription("Agregar nueva cuenta contable.");
        newBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                try {
                    if (cuentasContablesGrid.getSelectedRow() == null) {
                        CuentasContablesForm cuentasForm = new CuentasContablesForm();
                        cuentasForm.center();
                        UI.getCurrent().addWindow(cuentasForm);
                    } else if (cuentasContablesGrid.getSelectedRow() != null){
                        CuentasContablesForm cuentasForm = new CuentasContablesForm();
                        cuentasForm.llenarCamposInsert(String.valueOf(container.getContainerProperty(cuentasContablesGrid.getSelectedRow(), ID_NOMENCLATURA_PROPERTY).getValue()));
                        cuentasForm.center();
                        UI.getCurrent().addWindow(cuentasForm);
                    }

                } catch (Exception ex) {
                    System.out.println("Error en el boton nueva cuenta" + ex);
                }
            }
        });

        Button eliminarBtn = new Button("Eliminar");
        eliminarBtn.setIcon(FontAwesome.TRASH);
        eliminarBtn.setDescription("Eliminar cuenta.");
        eliminarBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (cuentasContablesGrid.getSelectedRow() == null) {
                    Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
                } else {

                    queryString = " SELECT * FROM contabilidad_partida";
                    queryString += " WHERE IdNomenclatura = " + String.valueOf(container.getContainerProperty(cuentasContablesGrid.getSelectedRow(), ID_NOMENCLATURA_PROPERTY).getValue());

                    try {

                        stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                        rsRecords = stQuery.executeQuery(queryString);

                        if (rsRecords.next()) {
                            Notification.show("La cuenta seleccionada contiene movimientos en partidas no se puede eliminar.", Notification.Type.ERROR_MESSAGE);
                        } else {

                            queryString = " DELETE FROM contabilidad_nomenclatura";
                            queryString += " WHERE IdNomenclatura = " + String.valueOf(container.getContainerProperty(cuentasContablesGrid.getSelectedRow(), ID_NOMENCLATURA_PROPERTY).getValue());
                            ;

                            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                            stQuery.executeUpdate(queryString);

                            Notification.show("Cuenta eliminada con exito!", Notification.Type.HUMANIZED_MESSAGE);

                            llenarTablaCuentas();

                        }
                    } catch (SQLException ex) {
                        System.out.println("Error al buscar registros en contabilidad_partida" + ex);
                    }

                }
            }
        });
        inhabilitarBtn = new Button("Inhabilitar");
        inhabilitarBtn.setIcon(FontAwesome.HAND_O_DOWN);
        inhabilitarBtn.setDescription("Eliminar cuenta.");
        inhabilitarBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (cuentasContablesGrid.getSelectedRow() == null) {
                    Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
                } else {

                    queryString = " UPDATE contabilidad_nomenclatura";
                    if (inhabilitarBtn.getCaption().equals("Habilitar")) {
                        queryString += " SET Estatus = 'HABILITADA'";
                        container.getContainerProperty(cuentasContablesGrid.getSelectedRow(), ESTATUS_PROPERTY).setValue("HABILITADA");
                        inhabilitarBtn.setCaption("Inhabilitar");
                        inhabilitarBtn.setIcon(FontAwesome.HAND_O_DOWN);
                    } else {
                        queryString += " SET Estatus = 'INHABILITADA'";
                        container.getContainerProperty(cuentasContablesGrid.getSelectedRow(), ESTATUS_PROPERTY).setValue("INHABILITADA");
                        inhabilitarBtn.setCaption("Habilitar");
                        inhabilitarBtn.setIcon(FontAwesome.HAND_O_UP);
                    }

                    queryString += " WHERE IdNomenclatura = " + String.valueOf(container.getContainerProperty(cuentasContablesGrid.getSelectedRow(), ID_NOMENCLATURA_PROPERTY).getValue());

                    try {
                        stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                        stQuery.executeUpdate(queryString);
                    } catch (SQLException ex) {
                        System.out.println("Error a Inabilitar cuenta contable " + ex);
                        ex.printStackTrace();
                    }
                }
            }
        });

        exportExcelBtn    = new Button("Exportar a Excel");
        exportExcelBtn.setIcon(FontAwesome.FILE_EXCEL_O);
//        exportExcelBtn.setWidth(130,Sizeable.UNITS_PIXELS);
        exportExcelBtn.addStyleName(ValoTheme.BUTTON_LINK);
        exportExcelBtn.addListener ( new Button.ClickListener()
        {
            @Override
            public void buttonClick ( Button.ClickEvent event )
            {
                if (cuentasContablesGrid.getHeightByRows() > 0) {
                    TableHolder tableHolder = new DefaultTableHolder(cuentasContablesGrid);
                    ExcelExport excelExport = new ExcelExport(tableHolder);
                    excelExport.excludeCollapsedColumns();
                    excelExport.setDisplayTotals(false);
                    String fileexport;
                    fileexport = "NomenclaturaContable_.xls";
                    excelExport.setExportFileName(fileexport);
                    excelExport.export();
                }
            }
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setMargin(false);
        buttonsLayout.addComponent(editBtn);
        buttonsLayout.addComponent(newBtn);
        buttonsLayout.addComponent(eliminarBtn);
        buttonsLayout.addComponent(inhabilitarBtn);
        buttonsLayout.addComponent(exportExcelBtn);

        addComponent(buttonsLayout);
        setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);
    }

    public void llenarTablaCuentas() {
        container.removeAllItems();

        queryString = "  select *";
        queryString += " from contabilidad_nomenclatura";

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado                                                
                do {
                    Object itemId = container.addItem();
                    container.getContainerProperty(itemId, ID_NOMENCLATURA_PROPERTY).setValue(rsRecords.getString("IdNomenclatura"));
                    container.getContainerProperty(itemId, REPORTE_PROPERTY).setValue(rsRecords.getString("Reporte"));
                    container.getContainerProperty(itemId, ID1_PROPERTY).setValue(rsRecords.getString("ID1"));
                    container.getContainerProperty(itemId, N1_PROPERTY).setValue(rsRecords.getString("N1"));
                    container.getContainerProperty(itemId, ID2_PROPERTY).setValue(rsRecords.getString("ID2"));
                    container.getContainerProperty(itemId, N2_PROPERTY).setValue(rsRecords.getString("N2"));
                    container.getContainerProperty(itemId, ID3_PROPERTY).setValue(rsRecords.getString("ID3"));
                    container.getContainerProperty(itemId, N3_PROPERTY).setValue(rsRecords.getString("N3"));
                    container.getContainerProperty(itemId, ID4_PROPERTY).setValue(rsRecords.getString("ID4"));
                    container.getContainerProperty(itemId, N4_PROPERTY).setValue(rsRecords.getString("N4"));
                    container.getContainerProperty(itemId, ID5_PROPERTY).setValue(rsRecords.getString("ID5"));
                    container.getContainerProperty(itemId, NO_CUENTA_PROPERTY).setValue(rsRecords.getString("NoCuenta"));
                    container.getContainerProperty(itemId, N5_PROPERTY).setValue(rsRecords.getString("N5"));
                    container.getContainerProperty(itemId, ESTATUS_PROPERTY).setValue(rsRecords.getString("Estatus"));
                    container.getContainerProperty(itemId, SALDO_PROPERTY).setValue("Ver saldo");

                } while (rsRecords.next());
            }
        } catch (Exception ex) {
            System.out.println("Error al listar tabla nomenclatura contable en CuentasContablesView:" + ex);
            ex.printStackTrace();
        }
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - Cuentas contables");
    }

}
