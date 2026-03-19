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
public class LibroComprasView extends VerticalLayout implements View {

    static final String FECHA_PROPERTY = "Fecha";
    static final String TIPO_PROPERTY = "TIPODOC";
    static final String SERIE_PROPERTY = "Serie";
    static final String FACTURA_PROPERTY = "Docto.";
    static final String NIT_PROPERTY = "NIT";
    static final String PROVEEDOR_PROPERTY = "Proveedor";
    static final String IVAREM_PROPERTY = "IvaRem";
    static final String PEQCONT_PROPERTY = "PeqCont";
    static final String NO_AFECTO_PROPERTY = "NoAfecto";
    static final String COMPRA_PROPERTY = "--COMPRA--";
    static final String SERVICIO_PROPERTY = "--SERVICIO--";
    static final String IMPORTACION_PROPERTY = "IMPORTA";
    static final String COMBUSTIBLES_PROPERTY = "COMBUST";
    static final String IVA_PROPERTY = "----IVA----";
    static final String MONTO_PROPERTY = "--MONTO--";

    Utileria utileria = new Utileria();

    Grid libroComprasGrid;

    public IndexedContainer libroComprasContainer = new IndexedContainer();

    UI mainUI;
    Statement stQuery;
    ResultSet rsRecords;
    Statement stQuery1;
    ResultSet rsRecords1;

    Button exportExcelBtn;
    PopupDateField monthDt;
    NumberField folioTxt;

    int totalFacturas = 0;
    int totalNotasCredito = 0;

    int cantidadFacturasPeqCont = 0, cantidadFacturasNoAfecto = 0, cantidadFacturasCombustible = 0;
    int cantidadFacturasCompra = 0, cantidadFacturasServicio = 0;

    int cantidadNotasPeqCont = 0, cantidadNotasNoAfecto = 0, cantidadNotasCombustible = 0;
    int cantidadNotaCompra = 0, cantidadNotaServicio = 0;

    static DecimalFormat numberFormat = new DecimalFormat("##,###,##0.00");

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public LibroComprasView() {

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

                if (libroComprasGrid != null) {
                    llenarGridLibroCompras(empresaId);
                }
            }
        });

        Label titleLbl = new Label(empresaId + " " + empresaNombre + " LIBRO COMPRAS");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setResponsive(true);
        titleLayout.setSpacing(true);
        titleLayout.setWidth("100%");
        titleLayout.setMargin(false);
        titleLayout.addComponents(titleLbl, monthDt);
        titleLayout.setComponentAlignment(titleLbl, Alignment.MIDDLE_CENTER);
        titleLayout.setComponentAlignment(monthDt, Alignment.MIDDLE_CENTER);
        titleLayout.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

        addComponent(titleLayout);
        setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        crearTablaLibroCompras();

    }

    public void crearTablaLibroCompras() {
        VerticalLayout layoutTablaLibroCompras = new VerticalLayout();
        layoutTablaLibroCompras.setWidth("100%");
//        layoutTablaLibroCompras.setHeightUndefined();
        layoutTablaLibroCompras.addStyleName("rcorners3");

        HorizontalLayout layoutButtons = new HorizontalLayout();
        layoutButtons.setMargin(true);
        layoutButtons.setSpacing(true);

        libroComprasContainer.addContainerProperty(FECHA_PROPERTY, String.class, "");
        libroComprasContainer.addContainerProperty(TIPO_PROPERTY, String.class, "");
        libroComprasContainer.addContainerProperty(SERIE_PROPERTY, String.class, "");
        libroComprasContainer.addContainerProperty(FACTURA_PROPERTY, String.class, "");
        libroComprasContainer.addContainerProperty(NIT_PROPERTY, String.class, "");
        libroComprasContainer.addContainerProperty(PROVEEDOR_PROPERTY, String.class, "");
        libroComprasContainer.addContainerProperty(IVAREM_PROPERTY, String.class, "");
        libroComprasContainer.addContainerProperty(PEQCONT_PROPERTY, String.class, "");
        libroComprasContainer.addContainerProperty(NO_AFECTO_PROPERTY, String.class, "");
        libroComprasContainer.addContainerProperty(COMBUSTIBLES_PROPERTY, String.class, "");
        libroComprasContainer.addContainerProperty(COMPRA_PROPERTY, String.class, "'");
        libroComprasContainer.addContainerProperty(SERVICIO_PROPERTY, String.class, "");
        libroComprasContainer.addContainerProperty(IMPORTACION_PROPERTY, String.class, "");
        libroComprasContainer.addContainerProperty(IVA_PROPERTY, String.class, "");
        libroComprasContainer.addContainerProperty(MONTO_PROPERTY, String.class, "");

        libroComprasGrid = new Grid(libroComprasContainer);
        libroComprasGrid.setImmediate(true);
        libroComprasGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        libroComprasGrid.setHeightMode(HeightMode.ROW);
        libroComprasGrid.setHeightByRows(15);
        libroComprasGrid.setWidth("100%");
        libroComprasGrid.setResponsive(true);
        libroComprasGrid.setEditorBuffered(false);
        libroComprasGrid.setColumnResizeMode(ColumnResizeMode.SIMPLE);
        libroComprasGrid.setColumnReorderingAllowed(false);

        libroComprasGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {

            if (IVAREM_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (PEQCONT_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (NO_AFECTO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (COMPRA_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (COMBUSTIBLES_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (SERVICIO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (IVA_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (MONTO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }

        });

        libroComprasGrid.getColumn(FECHA_PROPERTY).setExpandRatio(1);
//        libroComprasGrid.getColumn(TIPO_PROPERTY).setExpandRatio(1).setMaximumWidth(110);
        libroComprasGrid.getColumn(TIPO_PROPERTY).setMaximumWidth(110);
        libroComprasGrid.getColumn(SERIE_PROPERTY).setExpandRatio(1).setHidable(true).setHidden(true);
        libroComprasGrid.getColumn(FACTURA_PROPERTY).setExpandRatio(2);
        libroComprasGrid.getColumn(NIT_PROPERTY).setExpandRatio(1).setHidable(true).setHidden(true);
        libroComprasGrid.getColumn(PROVEEDOR_PROPERTY).setExpandRatio(2);
        libroComprasGrid.getColumn(IVAREM_PROPERTY).setExpandRatio(1);
        libroComprasGrid.getColumn(PEQCONT_PROPERTY).setExpandRatio(1);
        libroComprasGrid.getColumn(NO_AFECTO_PROPERTY).setExpandRatio(1);
        libroComprasGrid.getColumn(COMPRA_PROPERTY).setExpandRatio(4);
        libroComprasGrid.getColumn(SERVICIO_PROPERTY).setExpandRatio(4);
        libroComprasGrid.getColumn(IMPORTACION_PROPERTY).setExpandRatio(1);
        libroComprasGrid.getColumn(COMBUSTIBLES_PROPERTY).setExpandRatio(1);
        libroComprasGrid.getColumn(IVA_PROPERTY).setExpandRatio(4);
        libroComprasGrid.getColumn(MONTO_PROPERTY).setExpandRatio(5);

        HeaderRow filterRow = libroComprasGrid.appendHeaderRow();

        HeaderCell cell = filterRow.getCell(SERIE_PROPERTY);

        TextField filterField = new TextField();
        filterField.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField.setInputPrompt("Filtrar");
        filterField.setColumns(8);

        filterField.addTextChangeListener(change -> {
            libroComprasContainer.removeContainerFilters(SERIE_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                libroComprasContainer.addContainerFilter(
                        new SimpleStringFilter(SERIE_PROPERTY,
                                change.getText(), true, false));
            }
        });

        cell.setComponent(filterField);

        HeaderCell cell0 = filterRow.getCell(TIPO_PROPERTY);
        TextField filterField0 = new TextField();
        filterField0.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField0.setInputPrompt("Filtrar");
        filterField0.setColumns(8);

        filterField0.addTextChangeListener(change -> {
            libroComprasContainer.removeContainerFilters(TIPO_PROPERTY);
            if (!change.getText().isEmpty()) {
                libroComprasContainer.addContainerFilter(
                        new SimpleStringFilter(TIPO_PROPERTY,
                                change.getText(), true, false));
            }
        });

        cell0.setComponent(filterField0);

        HeaderCell cell2 = filterRow.getCell(FACTURA_PROPERTY);
        TextField filterField2 = new TextField();
        filterField2.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField2.setInputPrompt("Filtrar");
        filterField2.setColumns(8);

        filterField2.addTextChangeListener(change -> {
            libroComprasContainer.removeContainerFilters(FACTURA_PROPERTY);
            if (!change.getText().isEmpty()) {
                libroComprasContainer.addContainerFilter(
                        new SimpleStringFilter(FACTURA_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell2.setComponent(filterField2);

        HeaderCell cell3 = filterRow.getCell(PROVEEDOR_PROPERTY);
        TextField filterField3 = new TextField();
        filterField3.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField3.setInputPrompt("Filtrar");
        filterField3.setColumns(8);

        filterField3.addTextChangeListener(change -> {
            libroComprasContainer.removeContainerFilters(PROVEEDOR_PROPERTY);
            if (!change.getText().isEmpty()) {
                libroComprasContainer.addContainerFilter(
                        new SimpleStringFilter(PROVEEDOR_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell3.setComponent(filterField3);

        HeaderCell cell4 = filterRow.getCell(NIT_PROPERTY);
        TextField filterField4 = new TextField();
        filterField4.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField4.setInputPrompt("Filtrar");
        filterField4.setColumns(8);

        filterField4.addTextChangeListener(change -> {
            libroComprasContainer.removeContainerFilters(NIT_PROPERTY);
            if (!change.getText().isEmpty()) {
                libroComprasContainer.addContainerFilter(
                        new SimpleStringFilter(NIT_PROPERTY,
                                change.getText(), true, false));
            }
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
        folioTxt.setValue("1");
        folioTxt.setValidationVisible(false);

        Button generarPDF = new Button("Generar PDF");
        generarPDF.setIcon(FontAwesome.PAPER_PLANE);
        generarPDF.addStyleName(ValoTheme.BUTTON_PRIMARY);

        generarPDF.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (libroComprasContainer.size() > 0) {

                    LibroComprasPDF libroComprasPdf
                            = new LibroComprasPDF(
                                    empresaId,
                                    empresaNombre,
                                    ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyTaxId(),
                                    libroComprasContainer,
                                    Utileria.getFechaMMYYYY(monthDt.getValue()).replaceAll("/", ""),
                                    folioTxt.getValue()
                            );
                    mainUI.addWindow(libroComprasPdf);
                    libroComprasPdf.center();

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
                if (libroComprasGrid.getHeightByRows() > 0) {
                    TableHolder tableHolder = new DefaultTableHolder(libroComprasGrid);
                    ExcelExport excelExport = new ExcelExport(tableHolder);
                    excelExport.excludeCollapsedColumns();
                    excelExport.setDisplayTotals(false);
                    String fileexport;
                    fileexport = "LibroIvaCompras_" + empresaNombre.replaceAll(" ", "_").replaceAll(",", "_").replaceAll("[()]", "").replaceAll("[.]", "").replaceAll("ñ", "n").replaceAll("Ñ", "N").replaceAll("ó", "o").replaceAll("é", "") + ".xls";
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

        layoutTablaLibroCompras.addComponent(libroComprasGrid);
        layoutTablaLibroCompras.addComponent(layoutButtons);
        layoutTablaLibroCompras.setComponentAlignment(layoutButtons, Alignment.MIDDLE_CENTER);

        addComponent(layoutTablaLibroCompras);
        setComponentAlignment(layoutTablaLibroCompras, Alignment.MIDDLE_CENTER);

    }

    public void llenarGridLibroCompras(String empresa) {

        if (libroComprasContainer == null) {
            return;
        }

        libroComprasContainer.removeAllItems();

        if (libroComprasContainer.getContainerPropertyIds().size() < 7) {
            return;
        }

        BigDecimal peqCont = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal noAfecto = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal compra = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal servicio = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal combustibles = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal iva = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal monto = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);

        BigDecimal totalPeqCont = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal totalNoAfecto = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal totalCompra = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal totalServicio = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal totalCombustibles = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal totalIva = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal totalMonto = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);

        double ivaRemanente = 0.00;

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            //buscar el ultimo formulario....para obtener el IVA REMANENTE, que debe estar en 7 DEBE.
            String queryString = "SELECT CodigoCC, Fecha, SerieDocumento, NumeroDocumento, NombreProveedor, Debe ";
            queryString += " FROM contabilidad_partida";
            queryString += " WHERE  IdEmpresa = " + empresaId;
            queryString += " AND Fecha <= '" + Utileria.getFechaYYYYMMDD_1(monthDt.getValue()) + "'";
            queryString += " AND IdNomenclatura = " + ((SopdiUI) mainUI).cuentasContablesDefault.getIvaPorCobrar();  //iva por cobrar
            queryString += " AND DEBE > 0"; // solo las lineas cuenta por cobrar
            queryString += " AND TipoDocumento = 'FORMULARIO IVA'";

            rsRecords = stQuery.executeQuery(queryString);

            Object itemId = null;

            if (rsRecords.next()) {

                itemId = libroComprasContainer.addItem();
                libroComprasContainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha")));  // AQUI VA FECHA DE PRESENTACION
                libroComprasContainer.getContainerProperty(itemId, TIPO_PROPERTY).setValue("IVA REMANENTE");
                libroComprasContainer.getContainerProperty(itemId, SERIE_PROPERTY).setValue("2237");
                libroComprasContainer.getContainerProperty(itemId, FACTURA_PROPERTY).setValue(rsRecords.getString("NumeroDocumento"));  //AQUI VA EL FORMULARIO
                libroComprasContainer.getContainerProperty(itemId, NIT_PROPERTY).setValue("1669394-9"); //AQUI VA EL NIT SAT
                libroComprasContainer.getContainerProperty(itemId, PROVEEDOR_PROPERTY).setValue("SAT");

                libroComprasContainer.getContainerProperty(itemId, IVAREM_PROPERTY).setValue(numberFormat.format(rsRecords.getDouble("Debe"))); //AQUI VA EL IVA REMANENTE
                libroComprasContainer.getContainerProperty(itemId, PEQCONT_PROPERTY).setValue("");
                libroComprasContainer.getContainerProperty(itemId, NO_AFECTO_PROPERTY).setValue("");
                libroComprasContainer.getContainerProperty(itemId, COMPRA_PROPERTY).setValue("");
                libroComprasContainer.getContainerProperty(itemId, SERVICIO_PROPERTY).setValue("");
                libroComprasContainer.getContainerProperty(itemId, IMPORTACION_PROPERTY).setValue("");
                libroComprasContainer.getContainerProperty(itemId, COMBUSTIBLES_PROPERTY).setValue("");
                libroComprasContainer.getContainerProperty(itemId, IVA_PROPERTY).setValue("");
                libroComprasContainer.getContainerProperty(itemId, MONTO_PROPERTY).setValue("");

                ivaRemanente = rsRecords.getDouble("Debe");

            }

            itemId = null;

            queryString = " SELECT contabilidad_partida.TipoDocumento, contabilidad_partida.SerieDocumento, contabilidad_partida.NumeroDocumento, ";
            queryString += " contabilidad_partida.CodigoPartida,  contabilidad_nomenclatura_empresa.NoCuenta,";
            queryString += " contabilidad_partida.NitProveedor, contabilidad_partida.NombreProveedor, IFNULL(proveedor.Regimen, 'SINREGIMEN') PROV_REGIMEN,";
            queryString += " contabilidad_partida.DebeQuetzales, contabilidad_partida.HaberQuetzales,";
            queryString += " contabilidad_partida.Fecha, contabilidad_nomenclatura_empresa.IdNomenclatura, contabilidad_nomenclatura_empresa.Tipo ";
            queryString += " FROM contabilidad_partida ";
            queryString += " INNER JOIN contabilidad_nomenclatura_empresa ON contabilidad_nomenclatura_empresa.IdNomenclatura = contabilidad_partida.IdNomenclatura ";
            queryString += " LEFT JOIN proveedor ON proveedor.IdProveedor = contabilidad_partida.IdProveedor";
            queryString += " WHERE contabilidad_partida.IdEmpresa = " + empresa;
            queryString += " AND contabilidad_partida.TIPODOCUMENTO IN ('FACTURA', 'NOTA DE CREDITO COMPRA', 'RECIBO CONTABLE')";
            queryString += " AND Extract(YEAR_MONTH From contabilidad_partida.Fecha) = " + utileria.getFechaHoraSinFormato(monthDt.getValue()).substring(0, 6);
            queryString += " AND contabilidad_partida.Estatus <> 'ANULADO'";
            queryString += " AND contabilidad_nomenclatura_empresa.IdEmpresa = " + empresa;
            queryString += " ORDER BY contabilidad_partida.CodigoPartida, contabilidad_partida.Debe DESC";

//            System.out.println("query libro compras " + queryString);

            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado
                String codigoPartidaAnterior = "";
                String regimen = rsRecords.getString("PROV_REGIMEN");
                String tipoDocumento = rsRecords.getString("TipoDocumento");

                do {

                    if (rsRecords.getString("PROV_REGIMEN").equals("INSTITUCION")) {
                        //                       itemId = null;
//                        if(rsRecords.next()) {
                        codigoPartidaAnterior = "";
                        continue;
//                        }
//                        else {
//                            break;
//                        }
                    }

                    if (!codigoPartidaAnterior.equals(rsRecords.getString("CodigoPartida"))) {

                        if (itemId != null && iva.doubleValue() == 0) {  //se supone PEQUENO CONTRIBUYENTE...
                            if (regimen.equals("PEQUEÑO CONTRIBUYENTE")) {
                                peqCont = peqCont.add(monto);

                                libroComprasContainer.getContainerProperty(itemId, PEQCONT_PROPERTY).setValue(numberFormat.format(peqCont));
                                totalPeqCont = totalPeqCont.add(peqCont);

                                totalCompra = totalCompra.subtract(compra);
                                totalServicio = totalServicio.subtract(servicio);

                                libroComprasContainer.getContainerProperty(itemId, COMPRA_PROPERTY).setValue("");
                                libroComprasContainer.getContainerProperty(itemId, SERVICIO_PROPERTY).setValue("");

                            } else { //regimen normal  no declarada a tiempo
                                if (!tipoDocumento.equals("RECIBO CONTABLE")) {
                                    totalCompra = totalCompra.subtract(compra);
                                    totalServicio = totalServicio.subtract(servicio);

                                    noAfecto = noAfecto.add(compra.add(servicio));

                                    libroComprasContainer.getContainerProperty(itemId, NO_AFECTO_PROPERTY).setValue(numberFormat.format(noAfecto));
                                    totalNoAfecto = totalNoAfecto.add(noAfecto);

                                    compra.subtract(compra);
                                    libroComprasContainer.getContainerProperty(itemId, COMPRA_PROPERTY).setValue("");
//                                    libroComprasContainer.removeItem(itemId);
                                }
                            }

                        }

                        itemId = libroComprasContainer.addItem();
                        libroComprasContainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha")));
                        libroComprasContainer.getContainerProperty(itemId, TIPO_PROPERTY).setValue(rsRecords.getString("TipoDocumento"));
                        libroComprasContainer.getContainerProperty(itemId, SERIE_PROPERTY).setValue(rsRecords.getString("SerieDocumento"));
                        libroComprasContainer.getContainerProperty(itemId, FACTURA_PROPERTY).setValue(rsRecords.getString("NumeroDocumento"));
                        libroComprasContainer.getContainerProperty(itemId, NIT_PROPERTY).setValue(rsRecords.getString("NitProveedor"));
                        libroComprasContainer.getContainerProperty(itemId, PROVEEDOR_PROPERTY).setValue(rsRecords.getString("NombreProveedor"));

                        libroComprasContainer.getContainerProperty(itemId, PEQCONT_PROPERTY).setValue("");
                        libroComprasContainer.getContainerProperty(itemId, NO_AFECTO_PROPERTY).setValue("");
                        libroComprasContainer.getContainerProperty(itemId, COMPRA_PROPERTY).setValue("");
                        libroComprasContainer.getContainerProperty(itemId, SERVICIO_PROPERTY).setValue("");
                        libroComprasContainer.getContainerProperty(itemId, IMPORTACION_PROPERTY).setValue("");
                        libroComprasContainer.getContainerProperty(itemId, COMBUSTIBLES_PROPERTY).setValue("");
                        libroComprasContainer.getContainerProperty(itemId, IVA_PROPERTY).setValue("");
                        libroComprasContainer.getContainerProperty(itemId, MONTO_PROPERTY).setValue("");

                        peqCont = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
                        noAfecto = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
                        compra = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
                        servicio = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
                        combustibles = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
                        iva = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
                        monto = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);

                        regimen = rsRecords.getString("PROV_REGIMEN");
                        tipoDocumento = rsRecords.getString("TipoDocumento");
                        codigoPartidaAnterior = rsRecords.getString("CodigoPartida");
                    }

//if(rsRecords.getString("NumeroDocumento").equals("20505")) {
//    System.out.println("Cuenta=" + rsRecords.getString("IdNomenclatura") + " Tipo="+rsRecords.getString("Tipo") + " Debe=" + numberFormat.format(rsRecords.getDouble("DebeQuetzales"))+ " Haber=" + numberFormat.format(rsRecords.getDouble("HaberQuetzales")));
//}
                    switch (rsRecords.getString("Tipo").toUpperCase()) {
                        case "NO AFECTO":
                            noAfecto = noAfecto.add(new BigDecimal((rsRecords.getDouble("DebeQuetzales"))).setScale(2, BigDecimal.ROUND_HALF_UP)).setScale(2, BigDecimal.ROUND_HALF_UP);
                            libroComprasContainer.getContainerProperty(itemId, NO_AFECTO_PROPERTY).setValue(numberFormat.format(noAfecto));
                            totalNoAfecto = totalNoAfecto.add(new BigDecimal((rsRecords.getDouble("DebeQuetzales"))).setScale(2, BigDecimal.ROUND_HALF_UP)).setScale(2, BigDecimal.ROUND_HALF_UP);
                            break;
                        case "COMPRA":
                            if (rsRecords.getString("TipoDocumento").equals("RECIBO CONTABLE")) {
                                noAfecto = noAfecto.add(new BigDecimal((rsRecords.getDouble("DebeQuetzales") - rsRecords.getDouble("HaberQuetzales"))).setScale(2, BigDecimal.ROUND_HALF_UP)).setScale(2, BigDecimal.ROUND_HALF_UP);
                                libroComprasContainer.getContainerProperty(itemId, NO_AFECTO_PROPERTY).setValue(numberFormat.format(noAfecto));
                                totalNoAfecto = totalNoAfecto.add(noAfecto);
                                monto = monto.add(noAfecto);
                                libroComprasContainer.getContainerProperty(itemId, MONTO_PROPERTY).setValue(numberFormat.format(monto));
                                totalMonto = totalMonto.add(noAfecto);
                            } else if (rsRecords.getString("TipoDocumento").contains("NOTA DE CREDITO")) {
                                compra = compra.add(new BigDecimal((rsRecords.getDouble("DebeQuetzales") - rsRecords.getDouble("HaberQuetzales"))).setScale(2, BigDecimal.ROUND_HALF_UP)).setScale(2, BigDecimal.ROUND_HALF_UP);
                                libroComprasContainer.getContainerProperty(itemId, COMPRA_PROPERTY).setValue(numberFormat.format(compra));
                                totalCompra = totalCompra.add(new BigDecimal((rsRecords.getDouble("DebeQuetzales") - rsRecords.getDouble("HaberQuetzales"))).setScale(2, BigDecimal.ROUND_HALF_UP)).setScale(2, BigDecimal.ROUND_HALF_UP);
                            } else {
                                compra = compra.add(new BigDecimal((rsRecords.getDouble("DebeQuetzales") - rsRecords.getDouble("HaberQuetzales"))).setScale(2, BigDecimal.ROUND_HALF_UP)).setScale(2, BigDecimal.ROUND_HALF_UP);
                                libroComprasContainer.getContainerProperty(itemId, COMPRA_PROPERTY).setValue(numberFormat.format(compra));
                                totalCompra = totalCompra.add(new BigDecimal((rsRecords.getDouble("DebeQuetzales") - rsRecords.getDouble("HaberQuetzales"))).setScale(2, BigDecimal.ROUND_HALF_UP)).setScale(2, BigDecimal.ROUND_HALF_UP);
                            }
                            break;
                        case "SERVICIO":
                            if (rsRecords.getString("TipoDocumento").equals("RECIBO CONTABLE")) {
                                noAfecto = noAfecto.add(new BigDecimal((rsRecords.getDouble("DebeQuetzales") - rsRecords.getDouble("HaberQuetzales"))).setScale(2, BigDecimal.ROUND_HALF_UP)).setScale(2, BigDecimal.ROUND_HALF_UP);
                                libroComprasContainer.getContainerProperty(itemId, NO_AFECTO_PROPERTY).setValue(numberFormat.format(noAfecto));
                                totalNoAfecto = totalNoAfecto.add(noAfecto);
                                monto = monto.add(noAfecto);
                                libroComprasContainer.getContainerProperty(itemId, MONTO_PROPERTY).setValue(numberFormat.format(monto));
                                totalMonto = totalMonto.add(noAfecto);
                            } else if (rsRecords.getString("TipoDocumento").contains("NOTA DE CREDITO")) {
                                servicio = servicio.add(new BigDecimal((rsRecords.getDouble("DebeQuetzales") - rsRecords.getDouble("HaberQuetzales"))).setScale(2, BigDecimal.ROUND_HALF_UP)).setScale(2, BigDecimal.ROUND_HALF_UP);
                                libroComprasContainer.getContainerProperty(itemId, SERVICIO_PROPERTY).setValue(numberFormat.format(servicio));
                                totalServicio = totalServicio.add(new BigDecimal((rsRecords.getDouble("DebeQuetzales") - rsRecords.getDouble("HaberQuetzales"))).setScale(2, BigDecimal.ROUND_HALF_UP)).setScale(2, BigDecimal.ROUND_HALF_UP);
                            } else {
                                servicio = servicio.add(new BigDecimal((rsRecords.getDouble("DebeQuetzales") - rsRecords.getDouble("HaberQuetzales"))).setScale(2, BigDecimal.ROUND_HALF_UP)).setScale(2, BigDecimal.ROUND_HALF_UP);
                                libroComprasContainer.getContainerProperty(itemId, SERVICIO_PROPERTY).setValue(numberFormat.format(servicio));
                                totalServicio = totalServicio.add(new BigDecimal((rsRecords.getDouble("DebeQuetzales") - rsRecords.getDouble("HaberQuetzales"))).setScale(2, BigDecimal.ROUND_HALF_UP)).setScale(2, BigDecimal.ROUND_HALF_UP);
                            }
                            break;
                        case "COMBUSTIBLES":
                            if (rsRecords.getString("TipoDocumento").contains("NOTA DE CREDITO")) {
                                combustibles = combustibles.add(new BigDecimal((rsRecords.getDouble("DebeQuetzales") - rsRecords.getDouble("HaberQuetzales"))).setScale(2, BigDecimal.ROUND_HALF_UP)).setScale(2, BigDecimal.ROUND_HALF_UP);
                                libroComprasContainer.getContainerProperty(itemId, COMBUSTIBLES_PROPERTY).setValue(numberFormat.format(combustibles));
                                totalCombustibles = totalCombustibles.add(new BigDecimal((rsRecords.getDouble("DebeQuetzales") - rsRecords.getDouble("HaberQuetzales"))).setScale(2, BigDecimal.ROUND_HALF_UP)).setScale(2, BigDecimal.ROUND_HALF_UP);
                            } else {
                                combustibles = combustibles.add(new BigDecimal((rsRecords.getDouble("DebeQuetzales") - rsRecords.getDouble("HaberQuetzales"))).setScale(2, BigDecimal.ROUND_HALF_UP)).setScale(2, BigDecimal.ROUND_HALF_UP);
                                libroComprasContainer.getContainerProperty(itemId, COMBUSTIBLES_PROPERTY).setValue(numberFormat.format(combustibles));
                                totalCombustibles = totalCombustibles.add(new BigDecimal((rsRecords.getDouble("DebeQuetzales") - rsRecords.getDouble("HaberQuetzales"))).setScale(2, BigDecimal.ROUND_HALF_UP)).setScale(2, BigDecimal.ROUND_HALF_UP);
                            }
                            break;
                        case "IVA":
                            if (rsRecords.getString("TipoDocumento").contains("NOTA DE CREDITO")) {
                                iva = iva.add(new BigDecimal((rsRecords.getDouble("DebeQuetzales") - rsRecords.getDouble("HaberQuetzales"))).setScale(2, BigDecimal.ROUND_HALF_UP)).setScale(2, BigDecimal.ROUND_HALF_UP);
                                libroComprasContainer.getContainerProperty(itemId, IVA_PROPERTY).setValue(numberFormat.format(iva));
                                totalIva = totalIva.add(new BigDecimal((rsRecords.getDouble("DebeQuetzales") - rsRecords.getDouble("HaberQuetzales"))).setScale(2, BigDecimal.ROUND_HALF_UP)).setScale(2, BigDecimal.ROUND_HALF_UP);
                            } else {
                                iva = iva.add(new BigDecimal((rsRecords.getDouble("DebeQuetzales") - rsRecords.getDouble("HaberQuetzales"))).setScale(2, BigDecimal.ROUND_HALF_UP)).setScale(2, BigDecimal.ROUND_HALF_UP);
                                libroComprasContainer.getContainerProperty(itemId, IVA_PROPERTY).setValue(numberFormat.format(iva));
                                totalIva = totalIva.add(new BigDecimal((rsRecords.getDouble("DebeQuetzales") - rsRecords.getDouble("HaberQuetzales"))).setScale(2, BigDecimal.ROUND_HALF_UP)).setScale(2, BigDecimal.ROUND_HALF_UP);
                            }
                            break;
                        default:
                            if (rsRecords.getString("TipoDocumento").contains("NOTA DE CREDITO")) {
                                monto = monto.add(new BigDecimal((rsRecords.getDouble("DebeQuetzales"))).setScale(2, BigDecimal.ROUND_HALF_UP)).setScale(2, BigDecimal.ROUND_HALF_UP);
                                libroComprasContainer.getContainerProperty(itemId, MONTO_PROPERTY).setValue(numberFormat.format(monto));
                                totalMonto = totalMonto.add(new BigDecimal((rsRecords.getDouble("DebeQuetzales"))).setScale(2, BigDecimal.ROUND_HALF_UP)).setScale(2, BigDecimal.ROUND_HALF_UP);
                            } else if (!rsRecords.getString("TipoDocumento").equals("RECIBO CONTABLE")) {
                                monto = monto.add(new BigDecimal((rsRecords.getDouble("HaberQuetzales"))).setScale(2, BigDecimal.ROUND_HALF_UP)).setScale(2, BigDecimal.ROUND_HALF_UP);
                                libroComprasContainer.getContainerProperty(itemId, MONTO_PROPERTY).setValue(numberFormat.format(monto));
                                totalMonto = totalMonto.add(new BigDecimal((rsRecords.getDouble("HaberQuetzales"))).setScale(2, BigDecimal.ROUND_HALF_UP)).setScale(2, BigDecimal.ROUND_HALF_UP);
                            }
                            break;
                    }
                } while (rsRecords.next());

                if (iva.doubleValue() == 0) {  //se supone PEQUENO CONTRIBUYENTE...
                    if (regimen.equals("PEQUEÑO CONTRIBUYENTE")) {
                        peqCont = peqCont.add(monto);

                        libroComprasContainer.getContainerProperty(itemId, PEQCONT_PROPERTY).setValue(numberFormat.format(peqCont));
                        totalPeqCont = totalPeqCont.add(peqCont);

                        totalCompra = totalCompra.subtract(compra);
                        totalServicio = totalServicio.subtract(servicio);

                        libroComprasContainer.getContainerProperty(itemId, COMPRA_PROPERTY).setValue("");
                        libroComprasContainer.getContainerProperty(itemId, SERVICIO_PROPERTY).setValue("");
                    } else { //regimen normal  no declarada a tiempo
                        if (!tipoDocumento.equals("RECIBO CONTABLE")) {
                            totalCompra = totalCompra.subtract(compra);
                            totalServicio = totalServicio.subtract(servicio);

                            noAfecto = noAfecto.add(compra.add(servicio));

                            libroComprasContainer.getContainerProperty(itemId, NO_AFECTO_PROPERTY).setValue(numberFormat.format(noAfecto));
                            totalNoAfecto = totalNoAfecto.add(noAfecto);

                            compra.subtract(compra);
                            libroComprasContainer.getContainerProperty(itemId, COMPRA_PROPERTY).setValue("");
                        }
                    }
                }
            }

            if (libroComprasContainer.size() > 0) {
                setTotal();

                itemId = libroComprasContainer.addItem();
                libroComprasContainer.getContainerProperty(itemId, PROVEEDOR_PROPERTY).setValue("Totales : ");
                libroComprasContainer.getContainerProperty(itemId, IVAREM_PROPERTY).setValue(String.valueOf(numberFormat.format(ivaRemanente)));
                libroComprasContainer.getContainerProperty(itemId, PEQCONT_PROPERTY).setValue(String.valueOf(numberFormat.format(totalPeqCont)));
                libroComprasContainer.getContainerProperty(itemId, NO_AFECTO_PROPERTY).setValue(String.valueOf(numberFormat.format(totalNoAfecto)));
                libroComprasContainer.getContainerProperty(itemId, COMBUSTIBLES_PROPERTY).setValue(String.valueOf(numberFormat.format(totalCombustibles)));
                libroComprasContainer.getContainerProperty(itemId, COMPRA_PROPERTY).setValue(String.valueOf(numberFormat.format(totalCompra)));
                libroComprasContainer.getContainerProperty(itemId, SERVICIO_PROPERTY).setValue(String.valueOf(numberFormat.format(totalServicio)));
                libroComprasContainer.getContainerProperty(itemId, IVA_PROPERTY).setValue(String.valueOf(numberFormat.format(totalIva)));
                libroComprasContainer.getContainerProperty(itemId, MONTO_PROPERTY).setValue(String.valueOf(numberFormat.format(totalMonto)));

                itemId = libroComprasContainer.addItem();
                libroComprasContainer.getContainerProperty(itemId, PROVEEDOR_PROPERTY).setValue("Total Facturas :" + String.valueOf(totalFacturas));
                libroComprasContainer.getContainerProperty(itemId, PEQCONT_PROPERTY).setValue(String.valueOf(cantidadFacturasPeqCont));
                libroComprasContainer.getContainerProperty(itemId, NO_AFECTO_PROPERTY).setValue(String.valueOf(cantidadFacturasNoAfecto));
                libroComprasContainer.getContainerProperty(itemId, COMBUSTIBLES_PROPERTY).setValue(String.valueOf(cantidadFacturasCombustible));
                libroComprasContainer.getContainerProperty(itemId, COMPRA_PROPERTY).setValue(String.valueOf(cantidadFacturasCompra));
                libroComprasContainer.getContainerProperty(itemId, SERVICIO_PROPERTY).setValue(String.valueOf(cantidadFacturasServicio));

                itemId = libroComprasContainer.addItem();
                libroComprasContainer.getContainerProperty(itemId, PROVEEDOR_PROPERTY).setValue("Total Notas de credito: " + String.valueOf(totalNotasCredito));
                libroComprasContainer.getContainerProperty(itemId, PEQCONT_PROPERTY).setValue(String.valueOf(cantidadNotasPeqCont));
                libroComprasContainer.getContainerProperty(itemId, NO_AFECTO_PROPERTY).setValue(String.valueOf(cantidadNotasNoAfecto));
                libroComprasContainer.getContainerProperty(itemId, COMBUSTIBLES_PROPERTY).setValue(String.valueOf(cantidadNotasCombustible));
                libroComprasContainer.getContainerProperty(itemId, COMPRA_PROPERTY).setValue(String.valueOf(cantidadNotaCompra));
                libroComprasContainer.getContainerProperty(itemId, SERVICIO_PROPERTY).setValue(String.valueOf(cantidadNotaServicio));

            }
        } catch (Exception ex) {
            System.out.println("Error al listar tabla Facturas en LibroComprasView :" + ex);
            ex.printStackTrace();
        }
    }

    private void setTotal() {

        cantidadFacturasPeqCont = 0;
        cantidadFacturasNoAfecto = 0;
        cantidadFacturasCombustible = 0;
        cantidadFacturasCompra = 0;
        cantidadFacturasServicio = 0;

        cantidadNotasPeqCont = 0;
        cantidadNotasNoAfecto = 0;
        cantidadNotasCombustible = 0;
        cantidadNotaCompra = 0;
        cantidadNotaServicio = 0;

        for (Object grid : libroComprasGrid.getContainerDataSource().getItemIds()) {
            if (grid == null) {
                return;
            }

            if (String.valueOf(libroComprasContainer.getContainerProperty(grid, TIPO_PROPERTY).getValue()).equals("FACTURA")) {

                if (!String.valueOf(libroComprasContainer.getContainerProperty(grid, PEQCONT_PROPERTY).getValue()).trim().isEmpty()) {
                    cantidadFacturasPeqCont += 1;
                } else if (!String.valueOf(libroComprasContainer.getContainerProperty(grid, NO_AFECTO_PROPERTY).getValue()).trim().isEmpty()) {
                    cantidadFacturasNoAfecto += 1;

                } else if (!String.valueOf(libroComprasContainer.getContainerProperty(grid, COMBUSTIBLES_PROPERTY).getValue()).trim().isEmpty()) {
                    cantidadFacturasCombustible += 1;

                } else if (!String.valueOf(libroComprasContainer.getContainerProperty(grid, COMPRA_PROPERTY).getValue()).trim().isEmpty()) {
                    cantidadFacturasCompra += 1;

                } else if (!String.valueOf(libroComprasContainer.getContainerProperty(grid, SERVICIO_PROPERTY).getValue()).trim().isEmpty()) {
                    cantidadFacturasServicio += 1;

                }

                totalFacturas += 1;

            } else if (String.valueOf(libroComprasContainer.getContainerProperty(grid, TIPO_PROPERTY).getValue()).equals("NOTA DE CREDITO")
                    || String.valueOf(libroComprasContainer.getContainerProperty(grid, TIPO_PROPERTY).getValue()).equals("NOTA DE CREDITO COMPRA")) {

                if (!String.valueOf(libroComprasContainer.getContainerProperty(grid, PEQCONT_PROPERTY).getValue()).trim().isEmpty()) {
                    cantidadNotasPeqCont += 1;

                } else if (!String.valueOf(libroComprasContainer.getContainerProperty(grid, NO_AFECTO_PROPERTY).getValue()).trim().isEmpty()) {
                    cantidadNotasNoAfecto += 1;

                } else if (!String.valueOf(libroComprasContainer.getContainerProperty(grid, COMBUSTIBLES_PROPERTY).getValue()).trim().isEmpty()) {
                    cantidadNotasCombustible += 1;

                } else if (!String.valueOf(libroComprasContainer.getContainerProperty(grid, COMPRA_PROPERTY).getValue()).trim().isEmpty()) {
                    cantidadNotaCompra += 1;

                } else if (!String.valueOf(libroComprasContainer.getContainerProperty(grid, SERVICIO_PROPERTY).getValue()).trim().isEmpty()) {
                    cantidadNotaServicio += 1;
                }

                totalNotasCredito += 1;
            }

        }
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - Libro Compras");
    }
}
