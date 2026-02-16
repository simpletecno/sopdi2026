package com.simpletecno.sopdi.contabilidad;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.addon.tableexport.DefaultTableHolder;
import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.addon.tableexport.TableHolder;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.shared.ui.grid.ColumnResizeMode;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.HeaderCell;
import com.vaadin.ui.Grid.HeaderRow;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.ui.NumberField;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;

/**
 *
 * @author user
 */
public class LibroVentasView extends VerticalLayout implements View {

    static final String FECHA_PROPERTY = "Fecha";
    static final String SERIE_PROPERTY = "Serie";
    static final String FACTURA_PROPERTY = "Factura";
    static final String NIT_PROPERTY = "NIT";
    static final String CLIENTE_PROPERTY = "Cliente";
    static final String NO_AFECTO_PROPERTY = "NoAfecto";
    static final String PEQCONT_PROPERTY = "PeqCont";
    static final String VENTA_PROPERTY = "--VENTA--";
    static final String SERVICIO_PROPERTY = "--SERVICIO--";
    static final String IVA_PROPERTY = "--IVA--";
    static final String MONTO_PROPERTY = "----MONTO----";

    Utileria utileria = new Utileria();

    Grid libroVentasGrid;
    public IndexedContainer libroVentasContainer = new IndexedContainer();  

    UI mainUI;
    Statement stQuery;
    ResultSet rsRecords;
    Statement stQuery1;
    ResultSet rsRecords1;

    Button exportExcelBtn;
    ComboBox empresaCbx;
    String empresa;
    PopupDateField monthDt;
    NumberField folioTxt;

    static DecimalFormat numberFormat = new DecimalFormat("##,###,##0.00");

    String queryString;

    int totalFacturasVenta = 0;

    int cntFacturasPeqCon = 0, cntFacturasNoAfecto = 0, cntFacturastVenta = 0, cntFacturasServicio = 0;

    public LibroVentasView() {

        this.mainUI = UI.getCurrent();
        setWidth("100%");
        setSpacing(true);
        setMargin(false);
        setHeightUndefined();

        monthDt = new PopupDateField("Mes : ");
        monthDt.setWidth("10em");
        monthDt.setValue(new java.util.Date());
        monthDt.setResolution(Resolution.MONTH);
        monthDt.setDateFormat("MM/yyyy");
        monthDt.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {

                if (libroVentasGrid != null) {
                    llenarGridLibroVentas(empresa);
                }
            }
        });

        Label titleLbl = new Label("LIBRO VENTAS");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
//        titleLbl.addStyleName("h2_custom");

        empresaCbx = new ComboBox("Empresa:");
        empresaCbx.setWidth("400px");
        empresaCbx.addStyleName(ValoTheme.COMBOBOX_HUGE);
        empresaCbx.setInvalidAllowed(false);
        empresaCbx.setNewItemsAllowed(false);
        empresaCbx.setTextInputAllowed(false);
        empresaCbx.setNullSelectionAllowed(false);

        llenarComboEmpresa();

        empresaCbx.addValueChangeListener(event -> {
            empresa = String.valueOf(event.getProperty().getValue());
            llenarGridLibroVentas(empresa);
        });

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setResponsive(true);
        titleLayout.setSpacing(true);
        titleLayout.setWidth("100%");
        titleLayout.setMargin(false);
        titleLayout.addComponents(empresaCbx, titleLbl, monthDt);
        titleLayout.setComponentAlignment(empresaCbx, Alignment.MIDDLE_CENTER);
        titleLayout.setComponentAlignment(titleLbl, Alignment.MIDDLE_CENTER);
        titleLayout.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

        addComponent(titleLayout);
        setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        empresa = String.valueOf(empresaCbx.getValue());

        crearTablaLibroVentas();

        llenarGridLibroVentas(empresa);

    }

    public void crearTablaLibroVentas() {
        VerticalLayout layoutTablaLibroVentas = new VerticalLayout();
        layoutTablaLibroVentas.setWidth("100%");
//        layoutTablaLibroVentas.setHeightUndefined();
        layoutTablaLibroVentas.addStyleName("rcorners3");

        HorizontalLayout layoutButtons = new HorizontalLayout();
        layoutButtons.setMargin(true);
        layoutButtons.setSpacing(true);

        libroVentasContainer.addContainerProperty(FECHA_PROPERTY, String.class, null);
        libroVentasContainer.addContainerProperty(SERIE_PROPERTY, String.class, null);
        libroVentasContainer.addContainerProperty(FACTURA_PROPERTY, String.class, null);
        libroVentasContainer.addContainerProperty(NIT_PROPERTY, String.class, null);
        libroVentasContainer.addContainerProperty(CLIENTE_PROPERTY, String.class, null);
        libroVentasContainer.addContainerProperty(PEQCONT_PROPERTY, String.class, null);
        libroVentasContainer.addContainerProperty(NO_AFECTO_PROPERTY, String.class, null);
        libroVentasContainer.addContainerProperty(VENTA_PROPERTY, String.class, null);
        libroVentasContainer.addContainerProperty(SERVICIO_PROPERTY, String.class, null);
        libroVentasContainer.addContainerProperty(IVA_PROPERTY, String.class, null);
        libroVentasContainer.addContainerProperty(MONTO_PROPERTY, String.class, null);

        libroVentasGrid = new Grid(libroVentasContainer);
        libroVentasGrid.setImmediate(true);
        libroVentasGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        libroVentasGrid.setHeightMode(HeightMode.ROW);
        libroVentasGrid.setHeightByRows(15);
        libroVentasGrid.setWidth("100%");
        libroVentasGrid.setResponsive(true);
        libroVentasGrid.setEditorBuffered(false);
        libroVentasGrid.setColumnResizeMode(ColumnResizeMode.SIMPLE);
        libroVentasGrid.setColumnReorderingAllowed(false);

        libroVentasGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {

            if (NO_AFECTO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (VENTA_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (SERVICIO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (IVA_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (MONTO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (PEQCONT_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }

        });

        libroVentasGrid.getColumn(FECHA_PROPERTY).setExpandRatio(1);
        libroVentasGrid.getColumn(SERIE_PROPERTY).setExpandRatio(1).setHidable(true).setHidden(true);
        libroVentasGrid.getColumn(FACTURA_PROPERTY).setExpandRatio(2);
        libroVentasGrid.getColumn(NIT_PROPERTY).setExpandRatio(1).setHidable(true).setHidden(true);
        libroVentasGrid.getColumn(CLIENTE_PROPERTY).setExpandRatio(2);
        libroVentasGrid.getColumn(PEQCONT_PROPERTY).setExpandRatio(1);
        libroVentasGrid.getColumn(NO_AFECTO_PROPERTY).setExpandRatio(1);
        libroVentasGrid.getColumn(VENTA_PROPERTY).setExpandRatio(4);
        libroVentasGrid.getColumn(SERVICIO_PROPERTY).setExpandRatio(4);
        libroVentasGrid.getColumn(IVA_PROPERTY).setExpandRatio(4);
        libroVentasGrid.getColumn(MONTO_PROPERTY).setExpandRatio(5);

        HeaderRow filterRow = libroVentasGrid.appendHeaderRow();

        HeaderCell cell = filterRow.getCell(SERIE_PROPERTY);

        TextField filterField = new TextField();
        filterField.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField.setInputPrompt("Filtrar");
        filterField.setColumns(8);

        filterField.addTextChangeListener(change -> {
            libroVentasContainer.removeContainerFilters(SERIE_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                libroVentasContainer.addContainerFilter(
                        new SimpleStringFilter(SERIE_PROPERTY,
                                change.getText(), true, false));
            }
            setTotal();
        });

        cell.setComponent(filterField);

        HeaderCell cell2 = filterRow.getCell(FACTURA_PROPERTY);
        TextField filterField2 = new TextField();
        filterField2.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField2.setInputPrompt("Filtrar");
        filterField2.setColumns(8);

        filterField2.addTextChangeListener(change -> {
            libroVentasContainer.removeContainerFilters(FACTURA_PROPERTY);
            if (!change.getText().isEmpty()) {
                libroVentasContainer.addContainerFilter(
                        new SimpleStringFilter(FACTURA_PROPERTY,
                                change.getText(), true, false));
            }
            setTotal();
        });
        cell2.setComponent(filterField2);

        HeaderCell cell3 = filterRow.getCell(CLIENTE_PROPERTY);
        TextField filterField3 = new TextField();
        filterField3.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField3.setInputPrompt("Filtrar");
        filterField3.setColumns(12);

        filterField3.addTextChangeListener(change -> {
            libroVentasContainer.removeContainerFilters(CLIENTE_PROPERTY);
            if (!change.getText().isEmpty()) {
                libroVentasContainer.addContainerFilter(
                        new SimpleStringFilter(CLIENTE_PROPERTY,
                                change.getText(), true, false));
            }
            setTotal();
        });
        cell3.setComponent(filterField3);

        HeaderCell cell4 = filterRow.getCell(NIT_PROPERTY);
        TextField filterField4 = new TextField();
        filterField4.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField4.setInputPrompt("Filtrar");
        filterField4.setColumns(8);

        filterField4.addTextChangeListener(change -> {
            libroVentasContainer.removeContainerFilters(NIT_PROPERTY);
            if (!change.getText().isEmpty()) {
                libroVentasContainer.addContainerFilter(
                        new SimpleStringFilter(NIT_PROPERTY,
                                change.getText(), true, false));
            }
            setTotal();
        });
        cell4.setComponent(filterField4);      

        folioTxt = new NumberField("Folio Inicial :");
        folioTxt.setDecimalAllowed(false);
        folioTxt.setDecimalPrecision(0);
        folioTxt.setMinimumFractionDigits(0);
        folioTxt.setDecimalSeparatorAlwaysShown(false);
        folioTxt.setValue(1d);
        folioTxt.setGroupingUsed(true);
        folioTxt.setGroupingSize(0);
        folioTxt.setImmediate(true);
        folioTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);
        folioTxt.setWidth("8em");
        folioTxt.setValidationVisible(false);

        Button generarPDF = new Button("Generar PDF");
        generarPDF.setIcon(FontAwesome.PAPER_PLANE);
        generarPDF.addStyleName(ValoTheme.BUTTON_PRIMARY);

        generarPDF.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (libroVentasContainer.size() > 0) {

                    LibroVentasPDF libroVentasPdf
                            = new LibroVentasPDF(
                                    empresa,
                                    empresaCbx.getItemCaption(empresaCbx.getValue()),
                                    getEmpresaNit(),
                                    libroVentasContainer,
                                    Utileria.getFechaMMYYYY(monthDt.getValue()).replaceAll("/", ""),
                                    folioTxt.getValue()
                            );
                    mainUI.addWindow(libroVentasPdf);
                    libroVentasPdf.center();

                } else {
                    Notification.show("La vista no contiene registros disponibles.. ", Notification.Type.WARNING_MESSAGE);
                }
            }
        });

        exportExcelBtn = new Button("Excel");
        exportExcelBtn.setIcon(FontAwesome.FILE_EXCEL_O);
        exportExcelBtn.setWidth(120, Sizeable.UNITS_PIXELS);
        exportExcelBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (libroVentasGrid.getHeightByRows() > 0) {
                    TableHolder tableHolder = new DefaultTableHolder(libroVentasGrid);
                    ExcelExport excelExport = new ExcelExport(tableHolder);
                    excelExport.excludeCollapsedColumns();
                    excelExport.setDisplayTotals(false);
                    String fileexport;
                    fileexport = "LibroVentas_" + empresaCbx.getItemCaption(empresaCbx.getValue()).replaceAll(" ", "_").replaceAll(",", "_").replaceAll("[()]", "").replaceAll("[.]", "").replaceAll("ñ", "n").replaceAll("Ñ", "N").replaceAll("ó", "o").replaceAll("é", "") + ".xls";
                    excelExport.setExportFileName(fileexport);
                    excelExport.export();
                }
            }
        });

        layoutButtons.addComponent(folioTxt);
        layoutButtons.setComponentAlignment(folioTxt, Alignment.MIDDLE_LEFT);
        layoutButtons.addComponent(generarPDF);
        layoutButtons.setComponentAlignment(generarPDF, Alignment.BOTTOM_CENTER);
        layoutButtons.addComponent(exportExcelBtn);
        layoutButtons.setComponentAlignment(exportExcelBtn, Alignment.BOTTOM_CENTER);

        layoutTablaLibroVentas.addComponent(libroVentasGrid);
        layoutTablaLibroVentas.addComponent(layoutButtons);
        layoutTablaLibroVentas.setComponentAlignment(layoutButtons, Alignment.MIDDLE_CENTER);

        addComponent(layoutTablaLibroVentas);
        setComponentAlignment(layoutTablaLibroVentas, Alignment.MIDDLE_CENTER);
    }

    public void llenarGridLibroVentas(String empresa) {

        if (libroVentasContainer == null) {
            return;
        }

        libroVentasContainer.removeAllItems();

        setTotal();

        if (libroVentasContainer.getContainerPropertyIds().size() < 7) {
            return;
        }

        BigDecimal noAfecto = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal peq = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal venta = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal servicio = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal iva = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal monto = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);

        BigDecimal totalPeq = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal totalNoAfecto = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal totalVenta = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal totalServicio = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal totalIva = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal totalMonto = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);

        try {

            queryString = " select contabilidad_partida.SerieDocumento, contabilidad_partida.NumeroDocumento, ";
            queryString += " contabilidad_partida.CodigoPartida,  contabilidad_nomenclatura.NoCuenta,";
            queryString += " contabilidad_partida.NitProveedor, contabilidad_partida.NombreProveedor,";
            queryString += " contabilidad_partida.DebeQuetzales, contabilidad_partida.HaberQuetzales,";
            queryString += " contabilidad_partida.Fecha, contabilidad_nomenclatura.Tipo ";
            queryString += " from contabilidad_partida,contabilidad_nomenclatura";
            queryString += " where contabilidad_partida.IdEmpresa  = " + empresa;
            queryString += " And contabilidad_partida.TipoDocumento IN ('FACTURA VENTA', 'CONSTANCIA RETENCION IVA')";
            queryString += " and contabilidad_nomenclatura.IdNomenclatura = contabilidad_partida.IdNomenclatura";
            queryString += " and Extract(YEAR_MONTH From contabilidad_partida.Fecha) = " + utileria.getFechaHoraSinFormato(monthDt.getValue()).substring(0, 6);
//            queryString += " Order By contabilidad_partida.CodigoPartida, contabilidad_nomenclatura.NoCuenta";
            queryString += " And contabilidad_partida.Estatus <> 'ANULADO'";
            queryString += " Order By contabilidad_partida.CodigoPartida, contabilidad_partida.Debe Desc";

            System.out.println("\nQUERY LIBRO VENTAS = " + queryString + "\n");

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado                                                

                String codigoPartida = "";

                Object itemId = null;

                do {

                    if (!codigoPartida.equals(rsRecords.getString("CodigoPartida"))) {

                        itemId = libroVentasContainer.addItem();
                        libroVentasContainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha")));
                        libroVentasContainer.getContainerProperty(itemId, SERIE_PROPERTY).setValue(rsRecords.getString("SerieDocumento"));
                        libroVentasContainer.getContainerProperty(itemId, FACTURA_PROPERTY).setValue(rsRecords.getString("NumeroDocumento"));
                        libroVentasContainer.getContainerProperty(itemId, NIT_PROPERTY).setValue(rsRecords.getString("NitProveedor"));
                        libroVentasContainer.getContainerProperty(itemId, CLIENTE_PROPERTY).setValue(rsRecords.getString("NombreProveedor"));

                        libroVentasContainer.getContainerProperty(itemId, PEQCONT_PROPERTY).setValue("");
                        libroVentasContainer.getContainerProperty(itemId, NO_AFECTO_PROPERTY).setValue("");
                        libroVentasContainer.getContainerProperty(itemId, VENTA_PROPERTY).setValue("");
                        libroVentasContainer.getContainerProperty(itemId, SERVICIO_PROPERTY).setValue("");
                        libroVentasContainer.getContainerProperty(itemId, IVA_PROPERTY).setValue("");
                        libroVentasContainer.getContainerProperty(itemId, MONTO_PROPERTY).setValue("");

                        codigoPartida = rsRecords.getString("CodigoPartida");

                        noAfecto = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
                        peq = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
                        venta = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
                        servicio = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
                        iva = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
                        monto = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
                    }
                    switch (rsRecords.getString("Tipo").toUpperCase()) {
                        case "NO AFECTO":
                            noAfecto = noAfecto.add(new BigDecimal(rsRecords.getDouble("HaberQuetzales")).setScale(2, BigDecimal.ROUND_HALF_UP)).setScale(2, BigDecimal.ROUND_HALF_UP);
                            libroVentasContainer.getContainerProperty(itemId, NO_AFECTO_PROPERTY).setValue(numberFormat.format(noAfecto));
                            totalNoAfecto = totalNoAfecto.add(new BigDecimal(rsRecords.getDouble("HaberQuetzales")).setScale(2, BigDecimal.ROUND_HALF_UP)).setScale(2, BigDecimal.ROUND_HALF_UP);
                            break;
                        case "PRODUCTO":
                        case "VENTA":
                            venta = venta.add(new BigDecimal(rsRecords.getDouble("HaberQuetzales")).setScale(2, BigDecimal.ROUND_HALF_UP)).setScale(2, BigDecimal.ROUND_HALF_UP);
                            libroVentasContainer.getContainerProperty(itemId, VENTA_PROPERTY).setValue(numberFormat.format(venta));
                            totalVenta = totalVenta.add(new BigDecimal(rsRecords.getDouble("HaberQuetzales")).setScale(2, BigDecimal.ROUND_HALF_UP)).setScale(2, BigDecimal.ROUND_HALF_UP);
                            break;
                        case "SERVICIO":
                            servicio = servicio.add(new BigDecimal(rsRecords.getDouble("HaberQuetzales")).setScale(2, BigDecimal.ROUND_HALF_UP)).setScale(2, BigDecimal.ROUND_HALF_UP);
                            libroVentasContainer.getContainerProperty(itemId, SERVICIO_PROPERTY).setValue(numberFormat.format(servicio));
                            totalServicio = totalServicio.add(new BigDecimal(rsRecords.getDouble("HaberQuetzales")).setScale(2, BigDecimal.ROUND_HALF_UP)).setScale(2, BigDecimal.ROUND_HALF_UP);
                            break;
                        case "IVA":
                            iva = iva.add(new BigDecimal(rsRecords.getDouble("HaberQuetzales")).setScale(2, BigDecimal.ROUND_HALF_UP)).setScale(2, BigDecimal.ROUND_HALF_UP);
                            libroVentasContainer.getContainerProperty(itemId, IVA_PROPERTY).setValue(numberFormat.format(iva));
                            totalIva = totalIva.add(new BigDecimal(rsRecords.getDouble("HaberQuetzales")).setScale(2, BigDecimal.ROUND_HALF_UP)).setScale(2, BigDecimal.ROUND_HALF_UP);
                            break;
                        default:
                            monto = monto.add(new BigDecimal(rsRecords.getDouble("DebeQuetzales") - rsRecords.getDouble("HaberQuetzales")).setScale(2, BigDecimal.ROUND_HALF_UP)).setScale(2, BigDecimal.ROUND_HALF_UP);
                            libroVentasContainer.getContainerProperty(itemId, MONTO_PROPERTY).setValue(numberFormat.format(monto));
                            totalMonto = totalMonto.add(new BigDecimal(rsRecords.getDouble("DebeQuetzales") - rsRecords.getDouble("HaberQuetzales")).setScale(2, BigDecimal.ROUND_HALF_UP)).setScale(2, BigDecimal.ROUND_HALF_UP);
                            break;
                    }

                } while (rsRecords.next());

                setTotal();

                itemId = libroVentasContainer.addItem();
                libroVentasContainer.getContainerProperty(itemId, CLIENTE_PROPERTY).setValue("Totales : ");
                libroVentasContainer.getContainerProperty(itemId, PEQCONT_PROPERTY).setValue(String.valueOf(numberFormat.format(totalPeq)));
                libroVentasContainer.getContainerProperty(itemId, NO_AFECTO_PROPERTY).setValue(String.valueOf(numberFormat.format(totalNoAfecto)));
                libroVentasContainer.getContainerProperty(itemId, VENTA_PROPERTY).setValue(String.valueOf(numberFormat.format(totalVenta)));
                libroVentasContainer.getContainerProperty(itemId, SERVICIO_PROPERTY).setValue(String.valueOf(numberFormat.format(totalServicio)));
                libroVentasContainer.getContainerProperty(itemId, IVA_PROPERTY).setValue(String.valueOf(numberFormat.format(totalIva)));
                libroVentasContainer.getContainerProperty(itemId, MONTO_PROPERTY).setValue(String.valueOf(numberFormat.format(totalMonto)));

                itemId = libroVentasContainer.addItem();
                libroVentasContainer.getContainerProperty(itemId, CLIENTE_PROPERTY).setValue("Total facturas : ");
                libroVentasContainer.getContainerProperty(itemId, PEQCONT_PROPERTY).setValue(String.valueOf(cntFacturasPeqCon));
                libroVentasContainer.getContainerProperty(itemId, NO_AFECTO_PROPERTY).setValue(String.valueOf(cntFacturasNoAfecto));
                libroVentasContainer.getContainerProperty(itemId, VENTA_PROPERTY).setValue(String.valueOf(cntFacturastVenta));
                libroVentasContainer.getContainerProperty(itemId, SERVICIO_PROPERTY).setValue(String.valueOf(cntFacturasServicio));
            }

        } catch (Exception ex) {
            System.out.println("Error al listar tabla Facturas en LibroVentasView:" + ex);
            ex.printStackTrace();
        }
    }

    private void setTotal() {

        totalFacturasVenta = 0;
        cntFacturasPeqCon = 0;
        cntFacturasNoAfecto = 0;
        cntFacturastVenta = 0;
        cntFacturasServicio = 0;

        for (Object grid : libroVentasGrid.getContainerDataSource().getItemIds()) {
            if (grid == null) {
                return;
            }
            if (libroVentasContainer.getContainerProperty(grid, FACTURA_PROPERTY).getValue() == null) {
                return;
            }

            if (!String.valueOf(libroVentasContainer.getContainerProperty(grid, PEQCONT_PROPERTY).getValue()).trim().isEmpty()) {
                cntFacturasPeqCon += 1;

            } else if (!String.valueOf(libroVentasContainer.getContainerProperty(grid, NO_AFECTO_PROPERTY).getValue()).trim().isEmpty()) {
                cntFacturasNoAfecto += 1;

            } else if (!String.valueOf(libroVentasContainer.getContainerProperty(grid, VENTA_PROPERTY).getValue()).trim().isEmpty()) {
                cntFacturastVenta += 1;

            } else if (!String.valueOf(libroVentasContainer.getContainerProperty(grid, SERVICIO_PROPERTY).getValue()).trim().isEmpty()) {
                cntFacturasServicio += 1;

            }
            totalFacturasVenta += 1;
        }

    }

    public void llenarComboEmpresa() {
        queryString = " SELECT * from contabilidad_empresa";
        queryString += " Where IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();

        try {
            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords1 = stQuery1.executeQuery(queryString);

            while (rsRecords1.next()) { //  encontrado                
                empresaCbx.addItem(rsRecords1.getString("IdEmpresa"));
                empresaCbx.setItemCaption(rsRecords1.getString("IdEmpresa"), rsRecords1.getString("Empresa"));
            }
            rsRecords1.first();

            empresaCbx.select(rsRecords1.getString("IdEmpresa"));

        } catch (Exception ex1) {
            System.out.println("Error al listar empresas: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public String getEmpresaNit() {
        String strNit = "N/A";

        queryString = " SELECT Nit from contabilidad_empresa ";
        queryString += " Where IdEmpresa = " + empresa;

        try {
            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords1 = stQuery1.executeQuery(queryString);

            if (rsRecords1.next()) {
                strNit = rsRecords1.getString("Nit");
            }

        } catch (Exception ex1) {
            System.out.println("Error al buscar NIT de empresa: " + ex1.getMessage());
            ex1.printStackTrace();
        }

        return strNit;
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - Libro ventas");
    }
}
