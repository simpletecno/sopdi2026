package com.simpletecno.sopdi.compras;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.simpletecno.sopdi.tesoreria.AutorizarPagoLiquidacionForm;
import com.vaadin.addon.tableexport.DefaultTableHolder;
import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.addon.tableexport.TableHolder;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.SelectionEvent;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.Grid.HeaderCell;
import com.vaadin.ui.Grid.HeaderRow;
import com.vaadin.ui.themes.ValoTheme;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FacturasPendientesPagosView extends VerticalLayout implements View {

    IndexedContainer container = new IndexedContainer();
    Grid facturasGrid;
    IndexedContainer anticiposPagoContainer = new IndexedContainer();
    Grid anticiposPagoGrid;
    UI mainUI;
    Statement stQuery, stQuery1;
    ResultSet rsRecords, rsRecords1;

    String queryString;

    double totalMontoQuetzales = 0.00;
    double totalSaldoQueztales = 0.00;
    double totalMontoDolares = 0.00;
    double totalSaldoDolares = 0.00;

    static final String CODIGO_PARTIDA_PROPERTY = "Codigo partida o CC";
    static final String TIPO_DOCUMENTO_PROPERTY = "Tipo Documento";
    static final String ID_LIQU_O_PROV_PROPERTY = "Id Liq o Id Prov";
    static final String LIQUID_PROVEE_PROPERTY = "Liquidador o Proveedor";
    static final String DESCRIPCION_PROPERTY = "Descripción";
    static final String FECHA_PROPERTY = "Fecha";
    static final String DIASHOY_PROPERTY = "Días";
    static final String NUM_DOC_O_LIQUIDA_PROPERTY = "# Docto o Liquidación ";
    static final String MONEDA_PROPERTY = "Moneda";
    static final String MONTO_QUETZALES_PROPERTY = "Monto";
    static final String SALDO_PROPERTY = "Saldo";
    static final String AUTORIZADO_PAGAR_PROPERTY = "Autorizado";
    static final String AUTORIZADO_ANTICIPO_PROPERTY = "Anticipo";

    static final String CODIGO_PARTIDA2_PROPERTY = "CodigoPartida";
    static final String CODIGO_CC2_PROPERTY = "CodigoCC";
    static final String FECHA2__PROPERTY = "Fecha";
    static final String DOCUMENTO_PROPERTY = "Cheque/Transf";
    static final String MONTO2_PROPERTY = "Monto";
    static final String SALDO2_PROPERTY = "Saldo";
    static final String UTILIZAR_PROPERTY = "Utilizar";
    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public FacturasPendientesPagosView() {
        this.mainUI = UI.getCurrent();
        setWidth("100%");
        setMargin(false);
        setSpacing(true);

        HorizontalLayout layoutTitle = new HorizontalLayout();
        layoutTitle.setSpacing(true);
        layoutTitle.setMargin(new MarginInfo(false, true, false, true));
        layoutTitle.setWidth("100%");

        Label titleLbl = new Label(empresaId + " " + empresaNombre + " FACTURAS Y LIQUIDACIONES PENDIENTES DE PAGO");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h2_custom");

        layoutTitle.addComponent(titleLbl);
        layoutTitle.setComponentAlignment(titleLbl, Alignment.BOTTOM_RIGHT);

        addComponent(layoutTitle);

        crearGridLiquidaciones();
        crearGridAnticipos();
        buscarLiquidacionesPendientes();
    }

    public void crearGridLiquidaciones() {

        HorizontalLayout reportLayout = new HorizontalLayout();
        reportLayout.setWidth("100%");
        reportLayout.addStyleName("rcorners3");
        reportLayout.setResponsive(true);
        reportLayout.setMargin(new MarginInfo(false, true, false, true));

        container.addContainerProperty(CODIGO_PARTIDA_PROPERTY, String.class, null);
        container.addContainerProperty(TIPO_DOCUMENTO_PROPERTY, String.class, null);
        container.addContainerProperty(ID_LIQU_O_PROV_PROPERTY, String.class, null);
        container.addContainerProperty(LIQUID_PROVEE_PROPERTY, String.class, null);
        container.addContainerProperty(FECHA_PROPERTY, String.class, null);
        container.addContainerProperty(DIASHOY_PROPERTY, String.class, null);
        container.addContainerProperty(NUM_DOC_O_LIQUIDA_PROPERTY, String.class, null);
        container.addContainerProperty(MONEDA_PROPERTY, String.class, null);
        container.addContainerProperty(MONTO_QUETZALES_PROPERTY, String.class, null);
        container.addContainerProperty(SALDO_PROPERTY, String.class, null);
        container.addContainerProperty(AUTORIZADO_PAGAR_PROPERTY, String.class, null);
        container.addContainerProperty(AUTORIZADO_ANTICIPO_PROPERTY, String.class, null);
        container.addContainerProperty(DESCRIPCION_PROPERTY, String.class, null);

        facturasGrid = new Grid("Liquidaciones y Facturas pendientes de pagar", container);
        facturasGrid.setImmediate(true);
        facturasGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        facturasGrid.setDescription("Seleccione un registro.");
        facturasGrid.setHeightMode(HeightMode.ROW);
        facturasGrid.setHeightByRows(12);
        facturasGrid.setWidth("100%");
        facturasGrid.setResponsive(true);
        facturasGrid.setEditorBuffered(false);
        facturasGrid.setSizeFull();

        facturasGrid.getColumn(ID_LIQU_O_PROV_PROPERTY).setHidable(true).setHidden(true);
        facturasGrid.getColumn(AUTORIZADO_PAGAR_PROPERTY).setHidable(true).setHidden(true);
        facturasGrid.getColumn(AUTORIZADO_ANTICIPO_PROPERTY).setHidable(true).setHidden(true);

        facturasGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if (MONTO_QUETZALES_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (SALDO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (AUTORIZADO_PAGAR_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (AUTORIZADO_ANTICIPO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (DIASHOY_PROPERTY.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else {
                return null;
            }
        });
        facturasGrid.addSelectionListener(new SelectionEvent.SelectionListener() {
            @Override
            public void select(SelectionEvent event) {

                if (facturasGrid.getSelectedRow() != null) {
                    llenarTablaAnticipos();
                }
            }
        });

        HeaderRow filterRow = facturasGrid.appendHeaderRow();

        HeaderCell cell0 = filterRow.getCell(TIPO_DOCUMENTO_PROPERTY);

        TextField filterField0 = new TextField();
        filterField0.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField0.setInputPrompt("Filtrar");
        filterField0.setColumns(8);

        filterField0.addTextChangeListener(change -> {
            container.removeContainerFilters(TIPO_DOCUMENTO_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(
                        new SimpleStringFilter(TIPO_DOCUMENTO_PROPERTY,
                                change.getText(), true, false));
            }
        });

        cell0.setComponent(filterField0);

        HeaderCell cell = filterRow.getCell(CODIGO_PARTIDA_PROPERTY);

        TextField filterField = new TextField();
        filterField.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField.setInputPrompt("Filtrar");
        filterField.setColumns(8);

        filterField.addTextChangeListener(change -> {
            container.removeContainerFilters(CODIGO_PARTIDA_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(
                        new SimpleStringFilter(CODIGO_PARTIDA_PROPERTY,
                                change.getText(), true, false));
            }
        });

        cell.setComponent(filterField);

        HeaderCell cell2 = filterRow.getCell(LIQUID_PROVEE_PROPERTY);
        TextField filterField2 = new TextField();
        filterField2.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField2.setInputPrompt("Filtrar");
        filterField2.setColumns(10);

        HeaderCell cell3 = filterRow.getCell(NUM_DOC_O_LIQUIDA_PROPERTY);
        TextField filterField3 = new TextField();
        filterField3.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField3.setInputPrompt("Filtrar");
        filterField3.setColumns(10);

        filterField3.addTextChangeListener(change -> {
            container.removeContainerFilters(NUM_DOC_O_LIQUIDA_PROPERTY);
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(
                        new SimpleStringFilter(NUM_DOC_O_LIQUIDA_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell3.setComponent(filterField3);

        HeaderCell cell4 = filterRow.getCell(LIQUID_PROVEE_PROPERTY);
        TextField filterField4 = new TextField();
        filterField4.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField4.setInputPrompt("Filtrar");
        filterField4.setColumns(12);

        filterField4.addTextChangeListener(change -> {
            container.removeContainerFilters(LIQUID_PROVEE_PROPERTY);
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(
                        new SimpleStringFilter(LIQUID_PROVEE_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell4.setComponent(filterField4);

        Button generarExcel = new Button("Excel");
        generarExcel.setIcon(FontAwesome.FILE_EXCEL_O);
        generarExcel.addStyleName(ValoTheme.BUTTON_PRIMARY);

        generarExcel.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (container.size() > 0) {
                    exportToExcel();
                } else {
                    Notification notif = new Notification("La vista no contiene registros disponibles..",
                            Notification.Type.WARNING_MESSAGE);
                    notif.setDelayMsec(1500);
                    notif.setPosition(Position.MIDDLE_CENTER);
                    notif.setIcon(FontAwesome.WARNING);
                    notif.show(Page.getCurrent());
                }
            }
        });

        reportLayout.addComponent(facturasGrid);
        reportLayout.setComponentAlignment(facturasGrid, Alignment.TOP_CENTER);

        addComponent(reportLayout);
        setComponentAlignment(reportLayout, Alignment.TOP_CENTER);

        addComponent(generarExcel);
        setComponentAlignment(generarExcel, Alignment.TOP_CENTER);

    }

    public void crearGridAnticipos() {

        HorizontalLayout reportLayout = new HorizontalLayout();
        reportLayout.setWidth("100%");
        reportLayout.addStyleName("rcorners3");
        reportLayout.setResponsive(true);
        reportLayout.setMargin(new MarginInfo(false, true, false, true));

        HorizontalLayout layoutFiltros = new HorizontalLayout();
        layoutFiltros.setMargin(new MarginInfo(false, true, false, true));
        //layoutFiltros.setSpacing(true);
        layoutFiltros.setWidth("100%");

        anticiposPagoContainer.addContainerProperty(CODIGO_PARTIDA2_PROPERTY, String.class, null);
        anticiposPagoContainer.addContainerProperty(CODIGO_CC2_PROPERTY, String.class, null);
        anticiposPagoContainer.addContainerProperty(FECHA2__PROPERTY, String.class, null);
        anticiposPagoContainer.addContainerProperty(DOCUMENTO_PROPERTY, String.class, null);
        anticiposPagoContainer.addContainerProperty(MONTO2_PROPERTY, String.class, null);
        anticiposPagoContainer.addContainerProperty(SALDO2_PROPERTY, String.class, null);
//        anticiposPagoContainer.addContainerProperty(UTILIZAR_PROPERTY, String.class, null);

        anticiposPagoGrid = new Grid("Anticipos al proveedor,  pendientes de liquidar.", anticiposPagoContainer);

        anticiposPagoGrid.setImmediate(true);
        anticiposPagoGrid.setSelectionMode(Grid.SelectionMode.NONE);
        anticiposPagoGrid.setDescription("Seleccione un registro para ingresar o editar. Doble click para seleccionar el monto.");
        anticiposPagoGrid.setHeightMode(HeightMode.ROW);
        anticiposPagoGrid.setHeightByRows(5);
        anticiposPagoGrid.setWidth("100%");
        anticiposPagoGrid.setResponsive(true);
        anticiposPagoGrid.setEditorBuffered(false);
        anticiposPagoGrid.setSizeFull();
        anticiposPagoGrid.setEditorEnabled(true);
        anticiposPagoGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if (MONTO2_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (SALDO2_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }
        });

        anticiposPagoGrid.getColumn(CODIGO_PARTIDA2_PROPERTY).setHidable(true).setHidden(true);

        reportLayout.addComponent(anticiposPagoGrid);
        reportLayout.setComponentAlignment(anticiposPagoGrid, Alignment.TOP_CENTER);

        addComponent(reportLayout);
        setComponentAlignment(reportLayout, Alignment.TOP_CENTER);
    }

    public void buscarLiquidacionesPendientes() {
        try {

            String monedaSimbolo = "";
            container.removeAllItems();
            container.removeAllContainerFilters();
            anticiposPagoContainer.removeAllItems();

            queryString = " SELECT contabilidad_partida.IdEmpresa,contabilidad_partida.IdLiquidacion, contabilidad_partida.CodigoCC, contabilidad_partida.IdNomenclatura, ";
            queryString += " contabilidad_partida.IdLiquidador, proveedor_empresa.Nombre as NLiquidador, contabilidad_partida.MonedaDocumento, contabilidad_partida.Fecha";
            queryString += " FROM contabilidad_partida";
            queryString += " INNER JOIN proveedor_empresa ON proveedor_empresa.IDProveedor = contabilidad_partida.IdLiquidador";
            queryString += " And contabilidad_partida.IdEmpresa = " + empresaId;
            queryString += " AND contabilidad_partida.IdNomenclatura = " + ((SopdiUI) mainUI).cuentasContablesDefault.getLiquidacionesCajaChicha();
            queryString += " AND contabilidad_partida.TipoDocumento In ('FACTURA','RECIBO','RECIBO CONTABLE', ";
            queryString += " 'FORMULARIO ISR', 'FORMULARIO ISR RETENIDO', 'FORMULARIO ISO', 'FORMULARIO IVA','FORMULARIO IGSS',";
            queryString += " 'FORMULARIO RECTIFICACION', 'FORMULARIO ISR OPCIONAL MENSUAL', 'RECIBO CORRIENTE',";
            queryString += " 'NOTA DE CREDITO COMPRA', 'CONSTANCIA ISR COMPRA')";
            queryString += " AND contabilidad_partida.IdLiquidacion > 0 ";
            queryString += " AND contabilidad_partida.MontoAutorizadoPagar = 0";
            queryString += " And proveedor_empresa.IdProveedor = contabilidad_partida.IdLiquidador";
            queryString += " AND proveedor_empresa.IdEmpresa = " + empresaId;
            queryString += " AND contabilidad_partida.Estatus <> 'ANULADO'";
            queryString += " GROUP BY contabilidad_partida.IdLiquidacion";
            queryString += " ORDER BY contabilidad_partida.IdLiquidacion";

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            stQuery1 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            if (rsRecords.next()) {

                do {

                    queryString = " SELECT SUM(HABER - DEBE) TOTALSALDO, ";
                    queryString += " SUM(HaberQuetzales - DebeQuetzales) TOTALSALDOQ ";
                    queryString += " FROM contabilidad_partida";
                    queryString += " WHERE IdEmpresa = " + empresaId;
                    queryString += " AND CodigoCC = '" + rsRecords.getString("CodigoCC") + "'";
                    queryString += " AND contabilidad_partida.IdNomenclatura = " + rsRecords.getString("IdNomenclatura");
                    queryString += " AND contabilidad_partida.Estatus <> 'ANULADO'";

                    rsRecords1 = stQuery1.executeQuery(queryString);

                    if (rsRecords1.next()) {

                        if (rsRecords1.getDouble("TOTALSALDO") > 0.00) {
                            Object itemId = container.addItem();

System.out.println("tiene saldo..." + rsRecords.getString("IdLiquidacion"));

                            /**
container.addContainerProperty(CODIGO_PARTIDA_PROPERTY, String.class, null);
container.addContainerProperty(TIPO_DOCUMENTO_PROPERTY, String.class, null);
container.addContainerProperty(ID_LIQU_O_PROV_PROPERTY, String.class, null);
container.addContainerProperty(LIQUID_PROVEE_PROPERTY, String.class, null);
container.addContainerProperty(FECHA_PROPERTY, String.class, null);
container.addContainerProperty(DIASHOY_PROPERTY, String.class, null);
container.addContainerProperty(NUM_DOC_O_LIQUIDA_PROPERTY, String.class, null);
container.addContainerProperty(MONEDA_PROPERTY, String.class, null);
container.addContainerProperty(MONTO_QUETZALES_PROPERTY, String.class, null);
container.addContainerProperty(SALDO_PROPERTY, String.class, null);
container.addContainerProperty(AUTORIZADO_PAGAR_PROPERTY, String.class, null);
container.addContainerProperty(AUTORIZADO_ANTICIPO_PROPERTY, String.class, null);
container.addContainerProperty(DESCRIPCION_PROPERTY, String.class, null);
**/
                            container.getContainerProperty(itemId, CODIGO_PARTIDA_PROPERTY).setValue(rsRecords.getString("CodigoCC"));
                            container.getContainerProperty(itemId, TIPO_DOCUMENTO_PROPERTY).setValue("LIQUIDACÍON");
                            container.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue("ES LIQUIDACIÓN");
                            container.getContainerProperty(itemId, ID_LIQU_O_PROV_PROPERTY).setValue(rsRecords.getString("IdLiquidador"));
                            container.getContainerProperty(itemId, MONEDA_PROPERTY).setValue(rsRecords.getString("MonedaDocumento"));
                            container.getContainerProperty(itemId, LIQUID_PROVEE_PROPERTY).setValue(rsRecords.getString("NLiquidador"));
                            container.getContainerProperty(itemId, NUM_DOC_O_LIQUIDA_PROPERTY).setValue(rsRecords.getString("IdLiquidacion"));

                            if (rsRecords.getString("MonedaDocumento").equals("QUETZALES")) {
                                monedaSimbolo = "Q.";

                            } else {
                                monedaSimbolo = "$.";
                            }
                            container.getContainerProperty(itemId, MONTO_QUETZALES_PROPERTY).setValue(monedaSimbolo + numberFormat.format(rsRecords1.getDouble("TOTALSALDO")));
                            container.getContainerProperty(itemId, AUTORIZADO_ANTICIPO_PROPERTY).setValue("ES LIQUIDACIÓN");
                            container.getContainerProperty(itemId, AUTORIZADO_PAGAR_PROPERTY).setValue("ES LIQUIDACIÓN");
                            container.getContainerProperty(itemId, FECHA_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha")));
                            container.getContainerProperty(itemId, SALDO_PROPERTY).setValue("ES LIQUIDACIÓN");
                            container.getContainerProperty(itemId, DIASHOY_PROPERTY).setValue("");

                        }
                    }
                } while (rsRecords.next());
            }

            //// DESPUES DE BUSCAR LOS PRIMEROS REGISTROS OSEA LIQUIDACIONES BUSCARA LAS FACTURAS
            buscarFacturasCompra();
        } catch (SQLException ex) {
            System.out.println("Error en listar liquidaciones " + ex);
            Logger.getLogger(AutorizarPagoLiquidacionForm.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void buscarFacturasCompra() {

        HorizontalLayout layoutTitle = new HorizontalLayout();
        String monedaSimbolo = "";
//        container.removeAllItems();
//        container.removeAllContainerFilters();
//        anticiposPagoContainer.removeAllItems();
//        totalMontoQuetzales = 0.00;
//        totalSaldoQueztales = 0.00;
//        totalMontoDolares = 0.00;
//        totalSaldoDolares = 0.00;

        queryString = " SELECT *, DATEDIFF(CURDATE(),contabilidad_partida.Fecha) AS DiasHoy ";
        queryString += " FROM contabilidad_partida";
        queryString += " WHERE  Upper(TipoDocumento) IN ('FACTURA','RECIBO','RECIBO CONTABLE','RECIBO CORRIENTE','FORMULARIO IVA',";
        queryString += " 'FORMULARIO ISR', 'FORMULARIO ISR RETENIDO', 'FORMULARIO ISO', 'FORMULARIO RECTIFICACION')";
        queryString += " And   IdEmpresa = " + empresaId;
        queryString += " AND   IdNomenclatura IN (" + ((SopdiUI) mainUI).cuentasContablesDefault.getProveedores() + "," + ((SopdiUI) mainUI).cuentasContablesDefault.getInstituciones() + ")";
        queryString += " AND   MontoAutorizadoPagar = 0 ";
        queryString += " AND   MontoAplicarAnticipo = 0 ";
        queryString += " AND   Estatus <> 'ANULADO'";
        queryString += " ORDER by contabilidad_partida.CodigoPartida";

        try {

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);
            stQuery1 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            if (rsRecords.next()) {

                do {

                    queryString = " SELECT ";
                    queryString += " SUM(HABER - DEBE) TOTALSALDO, SUM(HaberQuetzales - DebeQuetzales) TOTALSALDOQ ";
                    queryString += " FROM contabilidad_partida";
                    queryString += " WHERE IdEmpresa = " + empresaId;
                    queryString += " AND CodigoCC = '" + rsRecords.getString("CodigoCC") + "'";
                    queryString += " AND contabilidad_partida.IdNomenclatura = " + rsRecords.getString("IdNomenclatura");
                    queryString += " AND contabilidad_partida.Estatus <> 'ANULADO'";

                    rsRecords1 = stQuery1.executeQuery(queryString);

                    if (rsRecords1.next()) {

                        if (rsRecords1.getDouble("TOTALSALDO") > 0.00) {

                            Object itemId = container.addItem();

                            container.getContainerProperty(itemId, TIPO_DOCUMENTO_PROPERTY).setValue(rsRecords.getString("TipoDocumento"));
                            container.getContainerProperty(itemId, ID_LIQU_O_PROV_PROPERTY).setValue(rsRecords.getString("IdProveedor"));
                            container.getContainerProperty(itemId, LIQUID_PROVEE_PROPERTY).setValue(rsRecords.getString("NombreProveedor"));
                            container.getContainerProperty(itemId, FECHA_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha")));
                            container.getContainerProperty(itemId, DIASHOY_PROPERTY).setValue(rsRecords.getString("DiasHoy"));
                            container.getContainerProperty(itemId, NUM_DOC_O_LIQUIDA_PROPERTY).setValue(rsRecords.getString("SerieDocumento") + " " + rsRecords.getString("NumeroDocumento"));
                            container.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(rsRecords.getString("Descripcion"));
                            container.getContainerProperty(itemId, MONEDA_PROPERTY).setValue(rsRecords.getString("MonedaDocumento"));

                            if (rsRecords.getString("MonedaDocumento").equals("QUETZALES")) {
                                totalMontoQuetzales = totalMontoQuetzales + rsRecords.getDouble("MontoDocumento");
                                totalSaldoQueztales = totalSaldoQueztales + rsRecords1.getDouble("TOTALSALDO");
                            } else {
                                totalMontoDolares = totalMontoDolares + rsRecords.getDouble("MontoDocumento");
                                totalSaldoDolares = totalSaldoDolares + rsRecords1.getDouble("TOTALSALDO");
                            }
                            container.getContainerProperty(itemId, MONTO_QUETZALES_PROPERTY).setValue(numberFormat.format(rsRecords.getDouble("MontoDocumento")));
                            container.getContainerProperty(itemId, SALDO_PROPERTY).setValue(numberFormat.format(rsRecords1.getDouble("TOTALSALDO")));
                            container.getContainerProperty(itemId, AUTORIZADO_PAGAR_PROPERTY).setValue(numberFormat.format(rsRecords.getDouble("MontoAutorizadoPagar")));
                            container.getContainerProperty(itemId, AUTORIZADO_ANTICIPO_PROPERTY).setValue(numberFormat.format(rsRecords.getDouble("MontoAplicarAnticipo")));
                            container.getContainerProperty(itemId, CODIGO_PARTIDA_PROPERTY).setValue(rsRecords.getString("CodigoPartida"));
                        }
                    }
                } while (rsRecords.next());
            }
        } catch (Exception ex) {
            System.out.println("Error al listar tabla Facturas en AutorizarPagoFacturaForm: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void llenarTablaAnticipos() {

        anticiposPagoContainer.removeAllItems();
        anticiposPagoContainer.removeAllContainerFilters();

        double totalMontoAnticipo = 0.00;
        double totalSaldoAnticipo = 0.00;

        String proveedorSeleccionado = String.valueOf(container.getContainerProperty(facturasGrid.getSelectedRow(), ID_LIQU_O_PROV_PROPERTY).getValue());
        String tipoMonedaSeleccionado = String.valueOf(container.getContainerProperty(facturasGrid.getSelectedRow(), MONEDA_PROPERTY).getValue());

        queryString = " SELECT contabilidad_partida.CodigoPartida, contabilidad_partida.CodigoCC, SUM(DEBE) MontoAnticipo, ";
        queryString += " SUM(DEBE - HABER) TOTALSALDO, SUM(DebeQuetzales - HaberQuetzales) TOTALSALDOQ, contabilidad_partida.Fecha";
        queryString += " FROM contabilidad_partida";
        queryString += " INNER JOIN contabilidad_nomenclatura_empresa ON contabilidad_nomenclatura_empresa.IdNomenclatura = contabilidad_partida.IdNomenclatura";
        queryString += " WHERE contabilidad_partida.IdProveedor = " + proveedorSeleccionado;
        queryString += " AND contabilidad_partida.IdEmpresa = " + empresaId;
        queryString += " AND contabilidad_partida.MonedaDocumento = '" + tipoMonedaSeleccionado + "'";
        queryString += " AND contabilidad_partida.IdNomenclatura = " + ((SopdiUI) mainUI).cuentasContablesDefault.getAnticiposProveedor();
//        queryString += " AND contabilidad_partida.TipoDocumento In ('CHEQUE', 'TRANSFERENCIA', 'NOTA DE CREDITO', 'DEPOSITO', 'PAGO DOCUMENTO','FORMULARIO ISR OPCIONAL MENSUAL' )";
//        queryString += " AND EXTRACT(YEAR FROM Fecha) >= '2020' ";
        queryString += " AND contabilidad_partida.Estatus <> 'ANULADO'";
//        queryString += " AND contabilidad_partida.CodigoCC Not In (Select CodigoCCRelacionado from autorizacion_pago ) ";
        queryString += " AND contabilidad_nomenclatura_empresa = " + empresaId;
        queryString += " GROUP BY contabilidad_partida.CodigoCC";
        queryString += " HAVING TOTALSALDO > 0";

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "-->query para mostrar anticipos pendiente de liquidar del proveedor : " + queryString);

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery1 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {

                do {

                    queryString = " SELECT IFNULL(SUM(Monto),0) TOTALOCUPADO";
                    queryString += " FROM autorizacion_pago";
                    queryString += " WHERE IdProveedor = " + proveedorSeleccionado;
                    queryString += " AND IdEmpresa = " + empresaId;
                    queryString += " AND CodigoCCRelacionado = '" + rsRecords.getString("CodigoCC") + "'";

                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, "-->query obtener saldo real anticipo : " + queryString);

                    rsRecords1 = stQuery1.executeQuery(queryString);

                    rsRecords1.next();

                    Object itemId = anticiposPagoContainer.addItem();

                    anticiposPagoContainer.getContainerProperty(itemId, CODIGO_PARTIDA2_PROPERTY).setValue(rsRecords.getString("CodigoPartida"));
                    anticiposPagoContainer.getContainerProperty(itemId, CODIGO_CC2_PROPERTY).setValue(rsRecords.getString("CodigoCC"));
                    anticiposPagoContainer.getContainerProperty(itemId, FECHA2__PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha")));
                    anticiposPagoContainer.getContainerProperty(itemId, DOCUMENTO_PROPERTY).setValue("ANTICIPO A PROVEEDORES");
                    anticiposPagoContainer.getContainerProperty(itemId, MONTO2_PROPERTY).setValue(numberFormat.format((rsRecords.getDouble("MontoAnticipo"))));
                    anticiposPagoContainer.getContainerProperty(itemId, SALDO2_PROPERTY).setValue(numberFormat.format(rsRecords.getDouble("TOTALSALDO") - rsRecords1.getDouble("TOTALOCUPADO")));
//                    anticiposPagoContainer.getContainerProperty(itemId, UTILIZAR_PROPERTY).setValue(numberFormat.format((0.00)));

                    totalMontoAnticipo += rsRecords.getDouble("MontoAnticipo");
                    totalSaldoAnticipo += rsRecords.getDouble("TOTALSALDO");

                } while (rsRecords.next());

            }
        } catch (Exception ex) {
            System.out.println("Error al listar grid anticipos factura : " + ex);
            ex.printStackTrace();
        }
    }

    public boolean exportToExcel() {
        if (this.facturasGrid.getHeightByRows() > 0) {
            TableHolder tableHolder = new DefaultTableHolder(facturasGrid);
            ExcelExport excelExport = new ExcelExport(tableHolder);
            excelExport.excludeCollapsedColumns();
            excelExport.setDisplayTotals(false);
            String fileexport;
            fileexport = ("Facturas y liquidaciones pendientes de pago".replaceAll(" ", "_").replaceAll(",", "_").replaceAll("[()]", "").replaceAll("[.]", "").replaceAll("ñ", "n").replaceAll("Ñ", "N").replaceAll("ó", "o").replaceAll("é", "") + ".xls").replaceAll(" ", "").replaceAll(",", "");
            excelExport.setExportFileName(fileexport);
            excelExport.export();                                  
        }
        return true;
    }       

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - Facturas y Liquidaciones pendientes");
    }
}
