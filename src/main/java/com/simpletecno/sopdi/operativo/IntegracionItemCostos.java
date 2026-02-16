/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.operativo;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.addon.tableexport.DefaultTableHolder;
import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.addon.tableexport.TableHolder;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.SelectionEvent;
import com.vaadin.event.SelectionEvent.SelectionListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.Grid.HeaderCell;
import com.vaadin.ui.Grid.HeaderRow;
import com.vaadin.ui.themes.ValoTheme;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JAguirre
 */
@SuppressWarnings("serial")
public final class IntegracionItemCostos extends Window {

    public Statement stQuery    = null;
    public Statement stQuery1   = null;
    public ResultSet rsRecords  = null;
    public ResultSet rsRecords1 = null;

    protected static final String INICIAL = "INTINI";
    protected static final String CAMBIOS = "DOCA";
    protected static final String PROYECCION = "0";
    protected static final String ACTUAL = "ACTUAL";
    protected static final String SALDO = "SALDO";

    protected static final String EMPRESA_PROPERTY = "EMPRESA";
    protected static final String CODIGO_PROPERTY = "Id";
    protected static final String CUENTA_PROPERTY = "Cuenta";
    protected static final String CCOSTO_PROPERTY = "CCosto";
    protected static final String DESCRIPCION_PROPERTY = "Descripción";
    protected static final String CANTIDAD_PROPERTY = "Cantidad";
    protected static final String PRECIO_PROPERTY = "Precio";
    protected static final String TOTAL_PROPERTY = "Total";
    protected static final String SALDO_PROPERTY = "Saldo";
    protected static final String MONEDA_PROPERTY = "Moneda";
    protected static final String PROJECT_PROPERTY = "Project";
    protected static final String LOTE_PROPERTY = "Lote";
    protected static final String PROVEEDOR_PROPERTY = "Proveedor";
    protected static final String IDVISITA_PROPERTY = "Visita";
    protected static final String IDTAREA_PROPERTY = "Tarea";
    protected static final String IDEX_PROPERTY = "IDEX";

    Button exportExcelBtn;

    TabSheet tabSheet;
    TabSheet.Tab inicialTab;
    TabSheet.Tab cambiosTab;
    TabSheet.Tab proyeccionTab;
    TabSheet.Tab actualTab;
    TabSheet.Tab saldoTab;
    TabSheet.Tab selectedTab;

    IndexedContainer integracionInicialContainer = new IndexedContainer();
    IndexedContainer integracionCambiosContainer = new IndexedContainer();
    IndexedContainer integracionProyeccionContainer = new IndexedContainer();
    IndexedContainer integracionActualContainer = new IndexedContainer();
    IndexedContainer integracionSaldoContainer = new IndexedContainer();
    final Grid integracionInicialGrid = new Grid(INICIAL,integracionInicialContainer);
    final Grid integracionCambiosGrid = new Grid(CAMBIOS,integracionCambiosContainer);
    final Grid integracionProyeccionGrid = new Grid(CAMBIOS,integracionProyeccionContainer);
    final Grid integracionActualGrid  = new Grid(ACTUAL,integracionActualContainer);
    final Grid integracionSaldoGrid  = new Grid(ACTUAL,integracionSaldoContainer);

    final Grid.FooterRow integracionInicialFooter =  integracionInicialGrid.appendFooterRow();;
    final Grid.FooterRow integracionCambiosFooter =  integracionCambiosGrid.appendFooterRow();;
    final Grid.FooterRow integracionProyeccionFooter =  integracionProyeccionGrid.appendFooterRow();;
    final Grid.FooterRow integracionActualFooter  =  integracionActualGrid.appendFooterRow();;
    final Grid.FooterRow integracionSaldoFooter  =  integracionSaldoGrid.appendFooterRow();;

    final UI mainUI = UI.getCurrent();

    static DecimalFormat moneyFormat = new DecimalFormat("#,###,##0.00");
    static DecimalFormat integerFormat2 = new DecimalFormat("##,##0");

    String projectNumber;

    VerticalLayout mainLayout;

    public IntegracionItemCostos(String projecNumber) {

        this.projectNumber = projecNumber;

        setResponsive(true);
        setWidth("80%");
        setHeight("90%");

        MarginInfo marginInfo = new MarginInfo(true, true, true, true);

        mainLayout = new VerticalLayout();
        mainLayout.setMargin(marginInfo);
        mainLayout.setSpacing(true);
        mainLayout.addStyleName("rcorners3");
//        mainLayout.setSizeFull();

        setContent(mainLayout);

        Label titleLbl = new Label(((SopdiUI) mainUI).sessionInformation.getStrProjectName() + " -- DETALLE ITEMS COSTOS (INTEGRACION) DEL PROJECT # " + projecNumber);
        titleLbl.addStyleName(ValoTheme.LABEL_H4);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h1_custom");

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setResponsive(true);
        titleLayout.setWidth("100%");
        titleLayout.setMargin(false);
        titleLayout.addComponents(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.TOP_LEFT);

        mainLayout.addComponent(titleLayout);
        mainLayout.setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        exportExcelBtn = new Button("Exportar a Excel");
        exportExcelBtn.setIcon(FontAwesome.FILE_EXCEL_O);
//        exportExcelBtn.setWidth(130,Sizeable.UNITS_PIXELS);
        exportExcelBtn.addStyleName(ValoTheme.BUTTON_LINK);
        exportExcelBtn.addClickListener((Button.ClickListener) event -> {
            if (tabSheet.getSelectedTab().equals(inicialTab)) {
                exportToExcel(INICIAL, integracionInicialContainer, integracionInicialGrid);
            } else if (tabSheet.getSelectedTab().equals(cambiosTab)) {
                exportToExcel(CAMBIOS, integracionCambiosContainer, integracionCambiosGrid);
            } else if (tabSheet.getSelectedTab().equals(proyeccionTab)) {
                exportToExcel(PROYECCION, integracionCambiosContainer, integracionCambiosGrid);
            } else if (tabSheet.getSelectedTab().equals(actualTab)) {
                exportToExcel(PROYECCION, integracionCambiosContainer, integracionCambiosGrid);
            }
            else {
                exportToExcel(SALDO, integracionSaldoContainer, integracionSaldoGrid);
            }
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setMargin(true);
        buttonsLayout.addComponent(exportExcelBtn);
        buttonsLayout.setComponentAlignment(exportExcelBtn, Alignment.BOTTOM_RIGHT);

        crearTabSheet();

        fillIntegraciones(INICIAL, integracionInicialContainer, integracionInicialGrid, integracionInicialFooter);
        fillIntegraciones(CAMBIOS, integracionCambiosContainer, integracionCambiosGrid, integracionCambiosFooter);
        fillIntegraciones(PROYECCION, integracionProyeccionContainer, integracionProyeccionGrid, integracionProyeccionFooter);
        fillIntegraciones(ACTUAL, integracionActualContainer, integracionActualGrid, integracionActualFooter);
        fillIntegraciones(SALDO, integracionSaldoContainer, integracionSaldoGrid, integracionSaldoFooter);

        mainLayout.addComponent(buttonsLayout);
        mainLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);
    }

    private void crearTabSheet() {
        tabSheet = new TabSheet();
        tabSheet.addStyleName(ValoTheme.TABSHEET_FRAMED);
        tabSheet.addStyleName(ValoTheme.TABSHEET_PADDED_TABBAR);
        tabSheet.addStyleName(ValoTheme.TABSHEET_CENTERED_TABS);
        tabSheet.addStyleName(ValoTheme.TABSHEET_EQUAL_WIDTH_TABS);

        addTabIntegracionInicial();
        addTabIntegracionCambios();
        addTabIntegracionProyeccion();
        addTabIntegracionActual();
        addTabIntegracionSaldo();

        mainLayout.addComponent(tabSheet);
        mainLayout.setComponentAlignment(tabSheet, Alignment.TOP_CENTER);
    }

    private void addTabIntegracionInicial() {
        inicialTab = tabSheet.addTab(createGrid( integracionInicialContainer, integracionInicialGrid, integracionInicialFooter), "Inicial");
        inicialTab.setIcon(FontAwesome.FLAG_CHECKERED);
        inicialTab.setId("1");
        inicialTab.setStyleName("dirtyTabCaption");
    }

    private void addTabIntegracionCambios() {
        cambiosTab = tabSheet.addTab(createGrid( integracionCambiosContainer, integracionCambiosGrid, integracionCambiosFooter), "Cambios");
        cambiosTab.setIcon(FontAwesome.ALIGN_CENTER);
        cambiosTab.setId("2");
        cambiosTab.setStyleName("dirtyTabCaption");
    }

    private void addTabIntegracionProyeccion() {
        proyeccionTab = tabSheet.addTab(createGrid( integracionProyeccionContainer, integracionProyeccionGrid, integracionProyeccionFooter), "Proyección");
        proyeccionTab.setIcon(FontAwesome.CALENDAR_O);
        proyeccionTab.setId("3");
        proyeccionTab.setStyleName("dirtyTabCaption");
    }

    private void addTabIntegracionActual() {
        actualTab = tabSheet.addTab(createGrid(integracionActualContainer, integracionActualGrid, integracionActualFooter), "Actual");
        actualTab.setIcon(FontAwesome.CHECK);
        actualTab.setId("4");
        actualTab.setStyleName("dirtyTabCaption");
    }

    private void addTabIntegracionSaldo() {
        saldoTab = tabSheet.addTab(createGrid(integracionSaldoContainer, integracionSaldoGrid, integracionSaldoFooter), "SALDO");
        saldoTab.setIcon(FontAwesome.MONEY);
        saldoTab.setId("5");
        saldoTab.setStyleName("dirtyTabCaption");
    }

    public VerticalLayout createGrid(IndexedContainer indexedContainer, Grid grid, Grid.FooterRow gridFooter) {

        VerticalLayout reportLayout = new VerticalLayout();
        reportLayout.setWidth("98%");
        reportLayout.addStyleName("rcorners3");
        reportLayout.setResponsive(true);
        reportLayout.setMargin(true);

        indexedContainer.addContainerProperty(PROJECT_PROPERTY, Integer.class, null);
        indexedContainer.addContainerProperty(IDEX_PROPERTY, String.class, null);
        indexedContainer.addContainerProperty(EMPRESA_PROPERTY, String.class, null);
//        indexedContainer.addContainerProperty(CODIGO_PROPERTY, String.class, null);
        indexedContainer.addContainerProperty(CUENTA_PROPERTY, String.class, null);
        indexedContainer.addContainerProperty(DESCRIPCION_PROPERTY, String.class, null);
        indexedContainer.addContainerProperty(CCOSTO_PROPERTY, String.class, null);
        indexedContainer.addContainerProperty(MONEDA_PROPERTY, String.class, null);
        indexedContainer.addContainerProperty(CANTIDAD_PROPERTY, Double.class, null);
        indexedContainer.addContainerProperty(PRECIO_PROPERTY, Double.class, null);
        indexedContainer.addContainerProperty(TOTAL_PROPERTY, Double.class, null);
        if(indexedContainer.equals(integracionSaldoContainer)) {
            indexedContainer.addContainerProperty(SALDO_PROPERTY, Double.class, null);
        }
        indexedContainer.addContainerProperty(PROVEEDOR_PROPERTY, String.class, null);
        indexedContainer.addContainerProperty(LOTE_PROPERTY, Integer.class, null);
        indexedContainer.addContainerProperty(IDVISITA_PROPERTY, String.class, null);
        indexedContainer.addContainerProperty(IDTAREA_PROPERTY, String.class, null);

        grid.setWidth("100%");
        grid.setImmediate(true);
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.setDescription("Seleccione un registro.");
        grid.setHeightMode(HeightMode.ROW);
        grid.setHeightByRows(15);
        grid.setResponsive(true);
        grid.setEditorBuffered(false);

        grid.setResponsive(true);
        grid.setEditorBuffered(false);

        grid.setCellStyleGenerator((Grid.CellReference cellReference) -> {

            if (PRECIO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (TOTAL_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (SALDO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (CANTIDAD_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (PROJECT_PROPERTY.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else if (LOTE_PROPERTY.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else {
                return null;
            }

        });

//        grid.getColumn(CODIGO_PROPERTY).setExpandRatio(1).setHidable(true).setHidden(true);
        grid.getColumn(CUENTA_PROPERTY).setExpandRatio(2).setWidth(105);
        grid.getColumn(CCOSTO_PROPERTY).setExpandRatio(1).setWidth(80);
        grid.getColumn(DESCRIPCION_PROPERTY).setExpandRatio(2).setWidth(185);
        grid.getColumn(CANTIDAD_PROPERTY).setExpandRatio(1).setWidth(70);
        grid.getColumn(PRECIO_PROPERTY).setExpandRatio(1).setWidth(80);
        grid.getColumn(TOTAL_PROPERTY).setExpandRatio(1).setWidth(110);
        grid.getColumn(MONEDA_PROPERTY).setExpandRatio(1).setWidth(100);
        grid.getColumn(PROJECT_PROPERTY).setExpandRatio(1).setHidable(true);
        grid.getColumn(LOTE_PROPERTY).setExpandRatio(1).setHidable(true);
        grid.getColumn(PROVEEDOR_PROPERTY).setExpandRatio(2).setWidth(150);
        grid.getColumn(IDVISITA_PROPERTY).setExpandRatio(1).setHidable(true).setHidden(true);
        grid.getColumn(IDTAREA_PROPERTY).setExpandRatio(1).setHidable(true).setHidden(true);
        grid.getColumn(IDEX_PROPERTY).setExpandRatio(1).setHidable(true);

        grid.addSelectionListener(new SelectionListener() {
            @Override
            public void select(SelectionEvent event) {
                if (grid.getSelectedRow() != null) {
//                    mostrarAnticipos();
                }
            }
        });

        HeaderRow filterRow = grid.appendHeaderRow();

        HeaderCell cell = filterRow.getCell(CUENTA_PROPERTY);

        TextField filterField = new TextField();
        filterField.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField.setInputPrompt("Filtrar");
        filterField.setColumns(8);

        filterField.addTextChangeListener(change -> {
            indexedContainer.removeContainerFilters(CUENTA_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                indexedContainer.addContainerFilter(
                        new SimpleStringFilter(CUENTA_PROPERTY,
                                change.getText(), true, false));
            }
            setTotal(indexedContainer, grid, gridFooter);
        });
        cell.setComponent(filterField);

        HeaderCell cell0 = filterRow.getCell(CCOSTO_PROPERTY);

        TextField filterField0 = new TextField();
        filterField0.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField0.setInputPrompt("Filtrar");
        filterField0.setColumns(5);

        filterField0.addTextChangeListener(change -> {
            indexedContainer.removeContainerFilters(CCOSTO_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                indexedContainer.addContainerFilter(
                        new SimpleStringFilter(CCOSTO_PROPERTY,
                                change.getText(), true, false));
            }
            setTotal(indexedContainer, grid, gridFooter);
        });
        cell0.setComponent(filterField0);

        HeaderCell cellA = filterRow.getCell(EMPRESA_PROPERTY);

        TextField filterFieldA = new TextField();
        filterFieldA.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterFieldA.setInputPrompt("Filtrar");
        filterFieldA.setColumns(15);

        filterFieldA.addTextChangeListener(change -> {
            indexedContainer.removeContainerFilters(EMPRESA_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                indexedContainer.addContainerFilter(
                        new SimpleStringFilter(EMPRESA_PROPERTY,
                                change.getText(), true, false));
            }
            setTotal(indexedContainer, grid, gridFooter);
        });
        cellA.setComponent(filterFieldA);

        HeaderCell cell00 = filterRow.getCell(DESCRIPCION_PROPERTY);

        TextField filterField00 = new TextField();
        filterField00.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField00.setInputPrompt("Filtrar");
        filterField00.setColumns(15);

        filterField00.addTextChangeListener(change -> {
            indexedContainer.removeContainerFilters(DESCRIPCION_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                indexedContainer.addContainerFilter(
                        new SimpleStringFilter(DESCRIPCION_PROPERTY,
                                change.getText(), true, false));
            }
            setTotal(indexedContainer, grid, gridFooter);
        });
        cell00.setComponent(filterField00);

        HeaderCell cell1 = filterRow.getCell(MONEDA_PROPERTY);

        TextField filterField1 = new TextField();
        filterField1.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField1.setInputPrompt("Filtrar");
        filterField1.setColumns(6);

        filterField1.addTextChangeListener(change -> {
            indexedContainer.removeContainerFilters(MONEDA_PROPERTY);

            // (Re)create the filter if necessary if
            if (!change.getText().isEmpty()) {
                indexedContainer.addContainerFilter(new
                        SimpleStringFilter(MONEDA_PROPERTY, change.getText(), true, false));
            }
            setTotal(indexedContainer, grid, gridFooter);
        });
        cell1.setComponent(filterField1);

        HeaderCell cell2 = filterRow.getCell(PROVEEDOR_PROPERTY);

        TextField filterField2 = new TextField();
        filterField2.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField2.setInputPrompt("Filtrar");
        filterField2.setColumns(15);

        filterField2.addTextChangeListener(change -> {
            indexedContainer.removeContainerFilters(PROVEEDOR_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                indexedContainer.addContainerFilter(
                        new SimpleStringFilter(PROVEEDOR_PROPERTY,
                                change.getText(), true, false));
            }
            setTotal(indexedContainer, grid, gridFooter);
        });
        cell2.setComponent(filterField2);

        HeaderCell cellIdex = filterRow.getCell(IDEX_PROPERTY);

        TextField filterFieldIdex = new TextField();
        filterFieldIdex.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterFieldIdex.setInputPrompt("Filtrar");
        filterFieldIdex.setColumns(15);

        filterFieldIdex.addTextChangeListener(change -> {
            indexedContainer.removeContainerFilters(IDEX_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                indexedContainer.addContainerFilter(
                        new SimpleStringFilter(IDEX_PROPERTY,
                                change.getText(), true, false));
            }
            setTotal(indexedContainer, grid, gridFooter);
        });
        cellIdex.setComponent(filterFieldIdex);

        gridFooter.getCell(DESCRIPCION_PROPERTY).setText("Totales");
        gridFooter.getCell(DESCRIPCION_PROPERTY).setStyleName("rightalign");
        gridFooter.getCell(TOTAL_PROPERTY).setText("0.00");
        gridFooter.getCell(TOTAL_PROPERTY).setStyleName("rightalign");

        reportLayout.addComponent(grid);
        reportLayout.setComponentAlignment(grid, Alignment.MIDDLE_CENTER);

        return reportLayout;
    }

    public void fillIntegraciones(String tipo, IndexedContainer indexedContainer, Grid grid, Grid.FooterRow gridFooter) {

        indexedContainer.removeAllItems();
        gridFooter.getCell(TOTAL_PROPERTY).setText("0.00");

        String queryString;

        if(tipo.equals(ACTUAL) || tipo.equals(SALDO)) {
            queryString = "Select DITEMC.IdEmpresa, DITEMC.Empresa, DITEMC.NoCuenta, DITEMC.Descripcion, ";
            queryString += " DITEMC.IdCC, Prov.IdProveedor, Prov.Nombre ProveedorNombre, ";
            queryString += " DITEMC.Lote, DITEMC.IdProject, DITEMC.Moneda, DITEMC.Idex, ";
            queryString += " SUM(DITEMC.Total / DITEMC.Cantidad) PrecioTotal, SUM(DITEMC.Cantidad) CantidadTotal, SUM(DITEMC.Total) TotalTotal ";
            queryString += " From  DetalleItemsCostos DITEMC";
            queryString += " Left Join proveedor Prov On Prov.IdProveedor = DITEMC.IdProveedor";
//            queryString += " Where DITEMC.IdEmpresa = " + empresa;
            queryString += " Where DITEMC.IdProject = " + projectNumber;
            queryString += " And DITEMC.Tipo In ('"  + INICIAL + "','" + CAMBIOS + "')";
            queryString += " Group By DITEMC.IdEmpresa, DITEMC.Empresa, DITEMC.NoCuenta, DITEMC.IdCC,";
            queryString += " Prov.IdProveedor, DITEMC.Idex ";
            queryString += " Order By DITEMC.NoCuenta";
        }
        else {
            queryString = "Select DITEMC.*, Prov.Nombre ProveedorNombre ";
            queryString += " From  DetalleItemsCostos DITEMC";
            queryString += " Left Join proveedor Prov On Prov.IdProveedor = DITEMC.IdProveedor";
            queryString += " Where DITEMC.IdProject = " + projectNumber;
            queryString += " And DITEMC.Tipo = '" + tipo + "'";
            queryString += " Order By DITEMC.NoCuenta";
        }

//System.out.println("\n\n" + queryString);

        try {

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado

                BigDecimal totalCuentaQuetzales = new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP);
                BigDecimal totalCuentaDolares = new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP);

                do {

                    Object itemId = indexedContainer.addItem();

                    indexedContainer.getContainerProperty(itemId, PROJECT_PROPERTY).setValue(rsRecords.getInt("IdProject"));
                    indexedContainer.getContainerProperty(itemId, IDEX_PROPERTY).setValue(rsRecords.getString("Idex"));
                    indexedContainer.getContainerProperty(itemId, EMPRESA_PROPERTY).setValue(rsRecords.getString("IdEMPRESA") + " " + rsRecords.getString("Empresa"));
//                    indexedContainer.getContainerProperty(itemId, CODIGO_PROPERTY).setValue(rsRecords.getString("Id"));
                    indexedContainer.getContainerProperty(itemId, CUENTA_PROPERTY).setValue(rsRecords.getString("NoCuenta"));
                    indexedContainer.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(rsRecords.getString("Descripcion"));
                    indexedContainer.getContainerProperty(itemId, CCOSTO_PROPERTY).setValue(rsRecords.getString("IdCC"));
                    indexedContainer.getContainerProperty(itemId, MONEDA_PROPERTY).setValue(rsRecords.getString("Moneda"));

                    if(tipo.equals(ACTUAL) || tipo.equals(SALDO)) {
                        indexedContainer.getContainerProperty(itemId, CANTIDAD_PROPERTY).setValue(rsRecords.getDouble("CantidadTotal"));
                        indexedContainer.getContainerProperty(itemId, PRECIO_PROPERTY).setValue(rsRecords.getDouble("PrecioTotal"));
                        indexedContainer.getContainerProperty(itemId, TOTAL_PROPERTY).setValue(rsRecords.getDouble("TotalTotal"));
                        if(tipo.equals(SALDO)) {
//                            indexedContainer.getContainerProperty(itemId, SALDO_PROPERTY).setValue(rsRecords.getDouble("SALDOTOTAL"));
//String EMPRESA, String IDEX, String CUENTA, String CENTROCOSTO, String PROVEEDOR
                            indexedContainer.getContainerProperty(itemId, SALDO_PROPERTY).setValue(
                                    (rsRecords.getDouble("TotalTotal") - getSaldo(
                                            rsRecords.getString("IdEmpresa"),
                                            rsRecords.getString("Idex"),
                                            rsRecords.getString("NoCuenta"),
                                            rsRecords.getString("IdCC"),
                                            rsRecords.getString("IdProveedor")
                                    ))
                            );
                        }
                    }
                    else {
                        indexedContainer.getContainerProperty(itemId, CANTIDAD_PROPERTY).setValue(rsRecords.getDouble("Cantidad"));
                        indexedContainer.getContainerProperty(itemId, PRECIO_PROPERTY).setValue(rsRecords.getDouble("Precio"));
                        indexedContainer.getContainerProperty(itemId, TOTAL_PROPERTY).setValue(rsRecords.getDouble("Total"));
                    }
                    indexedContainer.getContainerProperty(itemId, LOTE_PROPERTY).setValue(rsRecords.getInt("Lote"));
                    indexedContainer.getContainerProperty(itemId, PROVEEDOR_PROPERTY).setValue(rsRecords.getString("IdProveedor") + " " + rsRecords.getString("ProveedorNombre"));
                    if(tipo.equals(CAMBIOS)) {
                        indexedContainer.getContainerProperty(itemId, IDVISITA_PROPERTY).setValue(rsRecords.getString("IdVisita"));
                        indexedContainer.getContainerProperty(itemId, IDTAREA_PROPERTY).setValue(rsRecords.getString("IdTarea"));
                    }
                    else {
                        indexedContainer.getContainerProperty(itemId, IDVISITA_PROPERTY).setValue("");
                        indexedContainer.getContainerProperty(itemId, IDTAREA_PROPERTY).setValue("");
                    }

                    if(tipo.equals(ACTUAL) || tipo.equals(SALDO)) {
                        totalCuentaQuetzales = totalCuentaQuetzales.add(new BigDecimal(rsRecords.getDouble("TotalTotal")).setScale(2, BigDecimal.ROUND_HALF_UP));
                        totalCuentaDolares = totalCuentaDolares.add(new BigDecimal(rsRecords.getDouble("TotalTotal")).setScale(2, BigDecimal.ROUND_HALF_UP));
                    }
                    else {
                        totalCuentaQuetzales = totalCuentaQuetzales.add(new BigDecimal(rsRecords.getDouble("Total")).setScale(2, BigDecimal.ROUND_HALF_UP));
                        totalCuentaDolares = totalCuentaDolares.add(new BigDecimal(rsRecords.getDouble("Total")).setScale(2, BigDecimal.ROUND_HALF_UP));
                    }

                } while (rsRecords.next());

                gridFooter.getCell(TOTAL_PROPERTY).setText(moneyFormat.format(totalCuentaQuetzales));

//                if(tipo.equals(SALDO)) {
//                    TableHolder tableHolder = new DefaultTableHolder(grid);
//                    ExcelExport excelExport = new ExcelExport(tableHolder);
//                    excelExport.excludeCollapsedColumns();
//                    excelExport.setDisplayTotals(false);
//                    String fileexport;
//                    fileexport = "dic_consaldo.xls";
//                    excelExport.setExportFileName(fileexport);
//                    excelExport.export();
//
//                }
            }
        } catch (Exception ex) {
            Logger.getLogger(IntegracionItemCostos.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de detalle de integracion : " + ex.getMessage());
            Notification.show("Error al intentar leer detalle de integracion..!", Notification.Type.ERROR_MESSAGE);
        }

    }

    private void setTotal(IndexedContainer indexedContainer, Grid grid, Grid.FooterRow gridFooter) {
        BigDecimal total = new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP);
        for (Object rid : grid.getContainerDataSource()
                .getItemIds()) {
            total = total.add(new BigDecimal(
                    Double.parseDouble(
                            String.valueOf(indexedContainer.getContainerProperty(rid, TOTAL_PROPERTY).getValue())
                    )));
        }
        gridFooter.getCell(TOTAL_PROPERTY).setText(moneyFormat.format(total));
    }

    public boolean exportToExcel(String tipo, IndexedContainer indexedContainer, Grid grid) {
            TableHolder tableHolder = new DefaultTableHolder(grid);
            ExcelExport excelExport = new ExcelExport(tableHolder);
            excelExport.excludeCollapsedColumns();
            excelExport.setDisplayTotals(false);
            String fileexport;
            fileexport = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId() + "_"  + "_INTEGRACION_" + tipo + ".xls";
            excelExport.setExportFileName(fileexport);
            excelExport.export();
        return true;
    }

    /**
     * This class creates a streamresource. This class implements
     * the StreamSource interface which defines the getStream method.
     */
    public static class ShowExcelFile implements StreamResource.StreamSource {

        private final ByteArrayOutputStream os = new ByteArrayOutputStream();

        public ShowExcelFile(File fileToOpen) {
            try {

                FileOutputStream fost = new FileOutputStream(fileToOpen);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public InputStream getStream() {
            // Here we return the pdf contents as a byte-array
            return new ByteArrayInputStream(os.toByteArray());
        }
    }

    private double getSaldo(String EMPRESA, String IDEX, String CUENTA, String CENTROCOSTO, String PROVEEDOR) {

        String
        queryString =  "Select SUM(Total) TotalTotal ";
        queryString += " From  DocumentosContablesAplicados ";
        queryString += " Where  Idex     = '" + IDEX + "'";
        queryString += " And IdEmpresa   = " + EMPRESA;
        queryString += " And IdProveedor = " + PROVEEDOR;
        queryString += " And NoCuenta = '" + CUENTA + "'";
        queryString += " And IDCC = '" + CENTROCOSTO + "'";

System.out.println(queryString);

        double total = 0.00;

        try {

            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords1 = stQuery1.executeQuery (queryString);

            if(rsRecords1.next()) { //  encontrado
                total = rsRecords1.getDouble("TotalTotal");
            }
        }
        catch (Exception ex) {
            Logger.getLogger(IntegracionActual.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de detalle de integracion : " + ex.getMessage());
            Notification.show("Error al intentar leer detalle de integracion..!", Notification.Type.ERROR_MESSAGE);
        }

        return total;
    }

}

/****
 *
 *         else if(tipo.equals("SALDO")) {
 *             queryString = "Select DITEMC.IdEmpresa, DITEMC.Empresa, DITEMC.NoCuenta, DITEMC.Descripcion, ";
 *             queryString += " DITEMC.IdCC, Prov.IdProveedor, Prov.Nombre ProveedorNombre, DITEMC.IdVisita, ";
 *             queryString += " DITEMC.Lote, DITEMC.IdProject, DITEMC.Moneda, DITEMC.Idex, DITEMC.IdTarea, ";
 *             queryString += " SUM(DITEMC.Total / DITEMC.Cantidad) PrecioTotal, SUM(DITEMC.Cantidad) CantidadTotal, SUM(DITEMC.Total) TotalTotal, ";
 *             queryString += " SUM(DITEMC.Total - DCA.TOTAL) SALDOTOTAL";
 *             queryString += " From  DetalleItemsCostos DITEMC";
 *             queryString += " Left Join proveedor Prov On Prov.IdProveedor = DITEMC.IdProveedor";
 *             queryString += " Inner Join DocumentosContablesAplicados DCA On DCA.IdProject = DITEMC.IdProject ";
 * //            queryString += " Where DITEMC.IdEmpresa = " + empresa;
 *             queryString += " Where DITEMC.IdProject = " + projectNumber;
 *             queryString += " And DITEMC.Tipo In ('"  + INICIAL + "','" + CAMBIOS + "')";
 *             queryString += " And DCA.IdEmpresa   = DITEMC.IdEmpresa";
 *             queryString += " And DCA.IdProveedor = DITEMC.IdProveedor";
 *             queryString += " And DCA.NoCuenta = DITEMC.NoCuenta";
 *             queryString += " And DCA.IDEX = DITEMC.Idex";
 *             queryString += " Group By DITEMC.IdEmpresa, DITEMC.Empresa, DITEMC.NoCuenta, DITEMC.Descripcion, DITEMC.IdCC,";
 *             queryString += " Prov.IdProveedor, Prov.Nombre, DITEMC.IdVisita, DITEMC.Lote, DITEMC.IdProject, ";
 *             queryString += " DITEMC.Moneda, DITEMC.Idex, DITEMC.IdTarea ";
 *             queryString += " Order By DITEMC.NoCuenta";
 *
 */