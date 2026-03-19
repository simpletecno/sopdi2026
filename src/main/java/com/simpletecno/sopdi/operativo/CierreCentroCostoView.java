package com.simpletecno.sopdi.operativo;

import com.simpletecno.sopdi.configuracion.EnvironmentVars;
import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.SelectionEvent;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.dialogs.ConfirmDialog;

import java.io.*;
import java.nio.file.Files;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.simpletecno.sopdi.compras.IngresoDocumentosPDF.stPreparedQuery;

public class CierreCentroCostoView extends VerticalLayout implements View {

    public File file;

    Grid facturasVentaGrid;
    public IndexedContainer container = new IndexedContainer();

    public IndexedContainer ocIdexContainer = new IndexedContainer();
    Grid ocIdexGrid;

    static final String ID_PROPERTY = "Id";
    static final String CENTROCOSTO_PROPERTY = "Centro Costo";
    static final String FACTURA_PROPERTY = "Factura";
    static final String FECHA_PROPERTY = "Fecha";
    static final String IDCLIENTE_PROPERTY = "IdCliente";
    static final String CLIENTE_PROPERTY = "Cliente";
    static final String VALOR_PROPERTY = "Monto";
    static final String SALDO_PROPERTY = "Saldo";
    static final String TIPOCAMBIO_PROPERTY = "T_Cambio";
    static final String MONTO_QUETZALES_PROPERTY = "Monto Q.";
    static final String ESTATUS_PROPERTY = "Estatus";
    static final String DESCRIPCION_PROPERTY = "Descripción";
    static final String IMAGEN_PROPERTY = "Imagen";
    static final String ARCHIVO_PROPERTY = "Archivo";
    static final String ARCHIVO_TIPO_PROPERTY = "Tipo";
    static final String PARTIDA_PROPERTY = "Partida";

    static final String PROJECT_PROPERTY = "Project";
    static final String LOTE_PROPERTY = "Lote";
    static final String IDEX_PROPERTY = "IDEX";
    static final String CUENTA_PROPERTY = "Cuenta";
    static final String IDAREA_PROPERTY = "Id Area";
    static final String AREA_PROPERTY = "Area";
    static final String UNIDAD_PROPERTY = "Unidad";
    static final String EMPRESA_PROPERTY = "Empresa";
    static final String PROVEEDOR_PROPERTY = "Proveedor";
    static final String MONEDA_PROPERTY = "Moneda";
    static final String CANTIDAD_PROPERTY = "Cantidad";
    static final String PRECIO_PROPERTY = "Pre_Prom.";
    static final String TOTAL_PROPERTY = "Total";
    static final String ULT_PRECIO_PROPERTY = "Ult_Precio";

    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");
    static DecimalFormat numberFormat3 = new DecimalFormat("######0.00");

    UI mainUI;
    Statement stQuery, stQuery2;
    ResultSet rsRecords, rsRecords2;
    String queryString;

    ComboBox empresaCbx;
    String empresa;

    double cantidadOC;
    double totalOC;
    double precioOC;

    EnvironmentVars enviromentsVars;

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public CierreCentroCostoView() {
        this.mainUI = UI.getCurrent();
        setWidth("100%");
        setMargin(false);
        setSpacing(true);

        enviromentsVars = new EnvironmentVars();

        Label titleLbl = new Label(empresaId + " " + empresaNombre + " Cierre Centro Costo");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h1_custom");

        empresaCbx = new ComboBox("Empresa:");
        empresaCbx.setWidth("95%");
        empresaCbx.addStyleName(ValoTheme.COMBOBOX_HUGE);
        empresaCbx.setInvalidAllowed(false);
        empresaCbx.setNewItemsAllowed(false);
        empresaCbx.setTextInputAllowed(false);
        empresaCbx.setNullSelectionAllowed(false);

        llenarComboEmpresa();

        empresaCbx.addValueChangeListener(event -> {
            empresa = String.valueOf(event.getProperty().getValue());
            llenarTablaFacturaVenta();
        });

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setResponsive(true);
        titleLayout.setSpacing(true);
        titleLayout.setWidth("100%");
        titleLayout.setMargin(false);
        titleLayout.addComponents(empresaCbx, titleLbl);
        titleLayout.setComponentAlignment(empresaCbx, Alignment.MIDDLE_CENTER);
        titleLayout.setComponentAlignment(titleLbl, Alignment.MIDDLE_CENTER);
        titleLayout.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

        addComponent(titleLayout);
        setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        crearTablaFacturasVenta();

        crearTablaIdexPendientes();

        empresa = String.valueOf(empresaCbx.getValue());

        llenarTablaFacturaVenta();

    }

    public void llenarComboEmpresa() {
        queryString = " SELECT * FROM contabilidad_empresa";
        queryString += " WHERE IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) { //  encontrado
                empresaCbx.addItem(rsRecords.getString("IdEmpresa"));
                empresaCbx.setItemCaption(rsRecords.getString("IdEmpresa"), rsRecords.getString("Empresa"));
            }
            rsRecords.first();

            empresaCbx.select(rsRecords.getString("IdEmpresa"));

        } catch (Exception ex1) {
            System.out.println("Error al llenar Combo empresas: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void crearTablaFacturasVenta() {
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

        container.addContainerProperty(ID_PROPERTY, String.class, null);
        container.addContainerProperty(CENTROCOSTO_PROPERTY, String.class, null);
        container.addContainerProperty(FACTURA_PROPERTY, String.class, null);
        container.addContainerProperty(FECHA_PROPERTY, String.class, null);
        container.addContainerProperty(IDCLIENTE_PROPERTY, String.class, null);
        container.addContainerProperty(CLIENTE_PROPERTY, String.class, null);
        container.addContainerProperty(VALOR_PROPERTY, String.class, null);
        container.addContainerProperty(MONTO_QUETZALES_PROPERTY, String.class, null);
        container.addContainerProperty(SALDO_PROPERTY, String.class, null);
        container.addContainerProperty(TIPOCAMBIO_PROPERTY, String.class, null);
        container.addContainerProperty(ESTATUS_PROPERTY, String.class, null);
        container.addContainerProperty(DESCRIPCION_PROPERTY, String.class, null);
        container.addContainerProperty(IMAGEN_PROPERTY, String.class, null);
        container.addContainerProperty(ARCHIVO_PROPERTY, String.class, null);
        container.addContainerProperty(ARCHIVO_TIPO_PROPERTY, String.class, null);
        container.addContainerProperty(PARTIDA_PROPERTY, String.class, null);

        facturasVentaGrid = new Grid("", container);
        facturasVentaGrid.setWidth("100%");
        facturasVentaGrid.setImmediate(true);
        facturasVentaGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        facturasVentaGrid.setDescription("Seleccione un registro.");
        facturasVentaGrid.setHeightMode(HeightMode.ROW);
        facturasVentaGrid.setHeightByRows(10);
        facturasVentaGrid.setResponsive(true);
        facturasVentaGrid.setEditorBuffered(false);

        facturasVentaGrid.getColumn(IMAGEN_PROPERTY).setRenderer(new ButtonRenderer(e
                -> {

            if (!String.valueOf(container.getContainerProperty(e.getItemId(), ARCHIVO_PROPERTY).getValue()).equals("NO DISPONIBLE")) {
                verImagenPdf(String.valueOf(container.getContainerProperty(e.getItemId(), ARCHIVO_PROPERTY).getValue()));
            }

        }));

  //      facturasVentaGrid.getColumn(ID_PROPERTY).setHidable(true).setHidden(true);
        facturasVentaGrid.getColumn(TIPOCAMBIO_PROPERTY).setHidable(true).setHidden(true);
        facturasVentaGrid.getColumn(DESCRIPCION_PROPERTY).setHidable(true).setHidden(true);
        facturasVentaGrid.getColumn(ARCHIVO_PROPERTY).setHidable(true).setHidden(true);
        facturasVentaGrid.getColumn(ARCHIVO_TIPO_PROPERTY).setHidable(true).setHidden(true);
        facturasVentaGrid.getColumn(PARTIDA_PROPERTY).setHidable(true).setHidden(true);

//        facturasVentaGrid.getColumn(FECHA_PROPERTY).setWidth(113);
//        facturasVentaGrid.getColumn(VALOR_PROPERTY).setWidth(118);
//        facturasVentaGrid.getColumn(SALDO_PROPERTY).setWidth(118);
//        facturasVentaGrid.getColumn(MONTO_QUETZALES_PROPERTY).setWidth(118);
//        facturasVentaGrid.getColumn(ESTATUS_PROPERTY).setWidth(118);
//        facturasVentaGrid.getColumn(IMAGEN_PROPERTY).setWidth(100);

        facturasVentaGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if (MONTO_QUETZALES_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (VALOR_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (SALDO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (TIPOCAMBIO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else {
                return null;
            }
        });

        facturasVentaGrid.addSelectionListener(new SelectionEvent.SelectionListener() {
            @Override
            public void select(SelectionEvent event) {
                if (facturasVentaGrid.getSelectedRow() != null) {
                    fillIdexPendientesGrid(String.valueOf(container.getContainerProperty(facturasVentaGrid.getSelectedRow(), CENTROCOSTO_PROPERTY).getValue()));
                }
            }
        });

        layoutGrid.addComponent(facturasVentaGrid);
        layoutGrid.setComponentAlignment(facturasVentaGrid, Alignment.TOP_CENTER);

        layoutGrid.addComponent(facturasVentaGrid);

        reportLayout.addComponent(layoutGrid);

        addComponent(reportLayout);

        setComponentAlignment(reportLayout, Alignment.MIDDLE_CENTER);
    }

    public void llenarTablaFacturaVenta() {

        container.removeAllItems();
        ocIdexContainer.removeAllItems();

        queryString = " SELECT * ";
        queryString += " FROM contabilidad_partida ";
        queryString += " WHERE contabilidad_partida.TipoDocumento = 'FACTURA VENTA'";
        queryString += " AND contabilidad_partida.IdEmpresa = " + empresaCbx.getValue();
        queryString += " AND contabilidad_partida.IdNomenclatura = " +  ((SopdiUI) mainUI).cuentasContablesDefault.getClientes();
        queryString += " AND contabilidad_partida.IdCentroCosto <> '0'";
        String cuentasAccion = ((SopdiUI) mainUI).cuentasContablesDefault.getTituloAccion().trim().isEmpty()==false ? ((SopdiUI) mainUI).cuentasContablesDefault.getTituloAccion() : "0";
        cuentasAccion += ",";
        cuentasAccion += ((SopdiUI) mainUI).cuentasContablesDefault.getTituloAccion().trim().isEmpty()==false ? ((SopdiUI) mainUI).cuentasContablesDefault.getTituloAccion2() : "0";
        queryString += " AND contabilidad_partida.CodigoPartida NOT IN (SELECT CP.CodigoPartida FROM contabilidad_partida CP WHERE CP.CodigoPartida = contabilidad_partida.CodigoPartida AND CP.IdNomenclatura IN (" + cuentasAccion + ")";
        queryString += " AND contabilidad_partida.Estatus <> 'ANULADA'";
        queryString += " GROUP BY contabilidad_partida.CodigoPartida ";

Logger.getLogger(this.getClass().getName()).log(Level.INFO, "QUERY FACTURAS VENTA : " + queryString);

        try {

            String monedaSimbolo = "$";
            double saldo = 0.00;

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                do {

                    saldo = rsRecords.getDouble("MontoDocumento");

                    Object itemId = container.addItem();

                    container.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords.getString("IdPartida"));
                    container.getContainerProperty(itemId, CENTROCOSTO_PROPERTY).setValue(rsRecords.getString("IdCentroCosto"));
                    container.getContainerProperty(itemId, FACTURA_PROPERTY).setValue(rsRecords.getString("SerieDocumento") + " " + rsRecords.getString("NumeroDocumento"));
                    container.getContainerProperty(itemId, FECHA_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha")));
                    container.getContainerProperty(itemId, IDCLIENTE_PROPERTY).setValue(rsRecords.getString("IdProveedor"));
                    container.getContainerProperty(itemId, CLIENTE_PROPERTY).setValue(rsRecords.getString("NombreProveedor"));
                    if (rsRecords.getString("MonedaDocumento").equals("QUETZALES")) {
                        monedaSimbolo = "Q.";
                    } else {
                        monedaSimbolo = "$.";
                    }
                    container.getContainerProperty(itemId, VALOR_PROPERTY).setValue(monedaSimbolo + numberFormat.format(rsRecords.getDouble("Debe")));
                    container.getContainerProperty(itemId, SALDO_PROPERTY).setValue(monedaSimbolo + numberFormat.format(saldo));
                    container.getContainerProperty(itemId, TIPOCAMBIO_PROPERTY).setValue(rsRecords.getString("TipoCambio"));
                    container.getContainerProperty(itemId, MONTO_QUETZALES_PROPERTY).setValue("Q." + numberFormat.format(rsRecords.getDouble("DebeQuetzales")));
                    container.getContainerProperty(itemId, ESTATUS_PROPERTY).setValue(rsRecords.getString("Estatus"));
                    container.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(rsRecords.getString("Descripcion"));
                    if (rsRecords.getObject("ArchivoNombre") == null || rsRecords.getString("ArchivoNombre").trim().isEmpty() ) {
                        container.getContainerProperty(itemId, IMAGEN_PROPERTY).setValue("Cargar archivo");
                    } else {
                        container.getContainerProperty(itemId, IMAGEN_PROPERTY).setValue("Visualizar");
                    }
                    container.getContainerProperty(itemId, ARCHIVO_PROPERTY).setValue(rsRecords.getString("ArchivoNombre"));
                    container.getContainerProperty(itemId, ARCHIVO_TIPO_PROPERTY).setValue(rsRecords.getString("ArchivoTipo"));
                    container.getContainerProperty(itemId, PARTIDA_PROPERTY).setValue(rsRecords.getString("CodigoPartida"));

                    if(getSaldoSegunDIC(rsRecords.getString("IdCentroCosto")) <= 0.00) {
                        container.removeItem(itemId);
                    }
                    if(facturaTieneVisitaTarea(rsRecords.getString("SerieDocumento") + " " + rsRecords.getString("NumeroDocumento"))) {
                        container.removeItem(itemId);
                    }
                } while (rsRecords.next());

            }
        } catch (Exception ex) {
            System.out.println("Error al buscar registros FACTURAS VENTA : " + ex);
            ex.printStackTrace();
        }
    }

    private void crearTablaIdexPendientes() {

        VerticalLayout reportLayout = new VerticalLayout();
        reportLayout.addStyleName("rcorners3");
        reportLayout.setWidth("100%");
        reportLayout.setResponsive(true);
        reportLayout.setSpacing(true);
        reportLayout.setMargin(true);

        HorizontalLayout layoutGrid = new HorizontalLayout();
        layoutGrid.setWidth("100%");
        layoutGrid.setSpacing(true);
        layoutGrid.setMargin(true);

//        ocIdexContainer.addContainerProperty(ID_PROPERTY, String.class, "");
        ocIdexContainer.addContainerProperty(PROJECT_PROPERTY, String.class, "");
        ocIdexContainer.addContainerProperty(LOTE_PROPERTY, String.class, "");
        ocIdexContainer.addContainerProperty(IDEX_PROPERTY, String.class, "");
        ocIdexContainer.addContainerProperty(CUENTA_PROPERTY, String.class, "");
        ocIdexContainer.addContainerProperty(DESCRIPCION_PROPERTY, String.class, "");
        ocIdexContainer.addContainerProperty(IDAREA_PROPERTY, String.class, "");
        ocIdexContainer.addContainerProperty(AREA_PROPERTY, String.class, "");
        ocIdexContainer.addContainerProperty(UNIDAD_PROPERTY, String.class, "");
        ocIdexContainer.addContainerProperty(EMPRESA_PROPERTY, String.class, "");
        ocIdexContainer.addContainerProperty(PROVEEDOR_PROPERTY, String.class, "");
        ocIdexContainer.addContainerProperty(MONEDA_PROPERTY, String.class, "");
        ocIdexContainer.addContainerProperty(CANTIDAD_PROPERTY, String.class, "");
        ocIdexContainer.addContainerProperty(PRECIO_PROPERTY, String.class, "");
        ocIdexContainer.addContainerProperty(TOTAL_PROPERTY, String.class, "");
        ocIdexContainer.addContainerProperty(ULT_PRECIO_PROPERTY, String.class, "");

        ocIdexGrid = new Grid("IDEX DEL CENTRO DE COSTO Y PROJECT ", ocIdexContainer);
        ocIdexGrid.setWidth("100%");
        ocIdexGrid.setImmediate(true);
        ocIdexGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        ocIdexGrid.setDescription("Seleccione una o varias lineas DIC.");
        ocIdexGrid.setHeightMode(HeightMode.ROW);
        ocIdexGrid.setHeightByRows(10);
        ocIdexGrid.setResponsive(true);
        ocIdexGrid.setSizeFull();

        ocIdexGrid.getColumn(PROJECT_PROPERTY).setExpandRatio(1);
        ocIdexGrid.getColumn(IDEX_PROPERTY).setExpandRatio(1);
        ocIdexGrid.getColumn(CUENTA_PROPERTY).setExpandRatio(1);
        ocIdexGrid.getColumn(DESCRIPCION_PROPERTY).setExpandRatio(2);
        ocIdexGrid.getColumn(AREA_PROPERTY).setExpandRatio(1);
        ocIdexGrid.getColumn(UNIDAD_PROPERTY).setExpandRatio(1);
        ocIdexGrid.getColumn(EMPRESA_PROPERTY).setExpandRatio(1);
        ocIdexGrid.getColumn(PROVEEDOR_PROPERTY).setExpandRatio(1);
        ocIdexGrid.getColumn(MONEDA_PROPERTY).setExpandRatio(1);
        ocIdexGrid.getColumn(CANTIDAD_PROPERTY).setExpandRatio(1);
        ocIdexGrid.getColumn(PRECIO_PROPERTY).setExpandRatio(1);
        ocIdexGrid.getColumn(TOTAL_PROPERTY).setExpandRatio(1);
        ocIdexGrid.getColumn(ULT_PRECIO_PROPERTY).setExpandRatio(1);

        ocIdexGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if (CANTIDAD_PROPERTY.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else if (PRECIO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (TOTAL_PROPERTY.equals(cellReference.getPropertyId())) {
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

        layoutGrid.addComponent(ocIdexGrid);
        layoutGrid.setComponentAlignment(ocIdexGrid, Alignment.TOP_CENTER);
        reportLayout.addComponent(layoutGrid);

        Button liquidarDICBtn = new Button("LIQUIDAR DIC SELECIONADOS");
        liquidarDICBtn.addStyleName(ValoTheme.BUTTON_DANGER);
        liquidarDICBtn.setIcon(FontAwesome.CHECK_CIRCLE);
        liquidarDICBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if(facturasVentaGrid.getSelectedRow() != null) {
                    ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de FINALIZAR la orden de cambio ?",
                            "SI", "NO", new ConfirmDialog.Listener() {
                                public void onClose(ConfirmDialog dialog) {
                                    if (dialog.isConfirmed()) {
                                        liquidarDIC();
                                    }
                                }
                            } //end confirmdialog
                    ); //end dialog
                } //if selected row
            } // button
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setWidth("100%");
        buttonsLayout.addComponent(liquidarDICBtn);
        buttonsLayout.setComponentAlignment(liquidarDICBtn, Alignment.BOTTOM_CENTER);

        reportLayout.addComponent(buttonsLayout);
        reportLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);

        addComponent(reportLayout);
        setComponentAlignment(reportLayout, Alignment.MIDDLE_CENTER);
    }

    private double getSaldoSegunDIC(String CENTROCOSTO) {

        String queryString =  "SELECT SUM(Total) TotalTotal ";
        queryString += " FROM  DetalleItemsCostos ";
        queryString += " WHERE IDCC      = '" + CENTROCOSTO + "'";
        queryString += " AND IdEmpresa = " + empresa;
//System.out.println(queryString);

        double saldo = 0.00;

        try {

            stQuery2 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords2 = stQuery2.executeQuery (queryString);

            if(rsRecords2.next()) { //  encontrado
                saldo = rsRecords2.getDouble("TotalTotal");
            }
        }
        catch (Exception ex) {
            Logger.getLogger(IntegracionActual.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de detalle de integracion DIC : " + ex.getMessage());
            Notification.show("Error al intentar leer detalle de integracion DIC..! ", Notification.Type.ERROR_MESSAGE);
        }

        return saldo;
    }

    public boolean facturaTieneVisitaTarea(String factura) {

        queryString = " SELECT * ";
        queryString += " FROM visita_inspeccion ";
        queryString += " WHERE Referencia = '" + factura + "'";

        try {
            rsRecords2 = stQuery2.executeQuery(queryString);

            return rsRecords2.next();
        } catch (Exception ex) {
            System.out.println("Error al buscar registros FACTURAS VENTA en visita inspeccion : " + ex);
            ex.printStackTrace();
        }

        return false;
    }

    public void fillIdexPendientesGrid(String CENTROCOSTO) {

        if(ocIdexGrid == null) {
            return;
        }
        ocIdexContainer.removeAllItems();
//        ocIdexGrid.select(null);

        try {

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            String queryString =  "SELECT DIC.IdProject, DIC.NoCuenta, CCC.Descripcion CuentaDescripcion, ";
            queryString += " DIC.IdEmpresa, DIC.IdProveedor, Prov.Nombre NombreProveedor, ";
            queryString += " DIC.Lote, DIC.Idex, DIC.IdArea, area.Descripcion Area, DIC.Unidad, DIC.Moneda ,";
            queryString += " SUM(DIC.Total / DIC.Cantidad) PrecioTotal, SUM(DIC.Cantidad) CantidadTotal, SUM(DIC.Total) TotalTotal";
            queryString += " FROM  DetalleItemsCostos DIC";
            queryString += " INNER JOIN proveedor Prov ON Prov.IdProveedor = DIC.IdProveedor";
            queryString += " INNER JOIN centro_costo_cuenta CCC ON CCC.CodigoCuentaCentroCosto = DIC.NoCuenta";
            queryString += " INNER JOIN area ON area.IdArea = DIC.IdArea";
            queryString += " WHERE DIC.IdCC = '" + CENTROCOSTO + "'";
            //            queryString += " And DIC.IdProject = " + project;
            queryString += " AND DIC.Tipo In ('INTINI', 'DOCA')";
            queryString += " AND DIC.IdEmpresa = " + empresa;
            queryString += " GROUP BY DIC.IdProject, DIC.NoCuenta, DIC.IdEmpresa, DIC.IdProveedor, DIC.Idex, DIC.IdArea";
            queryString += " GROUP BY DIC.IdProject, DIC.Idex, DIC.NoCuenta ";

Logger.getLogger(this.getClass().getName()).log(Level.INFO, "QUERY IDEX PENDIENTES DE CERRAR : " + queryString);

            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado

                double montoTotal = 0.00;
                Object itemId = 0;

                do {

//                    getSaldoSegunOC(
//                            CENTROCOSTO,
//                            rsRecords.getString("IdProject"),
//                            rsRecords.getString("Idex"),
//                            rsRecords.getString("NoCuenta"),
//                            rsRecords.getString("IdArea"),
//                            rsRecords.getString("IdEmpresa"),
//                            rsRecords.getString("IdProveedor")
//                    );
//                    cantidadTotal = rsRecords.getDouble("Cantidad") + cantidadOC;
//                    montoTotal = rsRecords.getDouble("Total") + totalOC;
//                    precioPromedio = rsRecords.getDouble("Precio") + (precioOC / cantidadTotal);
//
//                    if(montoTotal > 0.00) {

                    montoTotal = (rsRecords.getDouble("TotalTotal") - getSaldoDocumentosContablesAplicados(
                            CENTROCOSTO,
                            rsRecords.getString("IdProject"),
                            rsRecords.getString("Idex"),
                            rsRecords.getString("NoCuenta"),
                            rsRecords.getString("IdArea"),
                            rsRecords.getString("IdEmpresa"),
                            rsRecords.getString("IdProveedor")
                    ));

                    if(montoTotal > 0) {

                        itemId = ocIdexContainer.addItem();

//                        ocIdexContainer.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords.getString("Id"));
                        ocIdexContainer.getContainerProperty(itemId, PROJECT_PROPERTY).setValue(rsRecords.getString("IdProject"));
                        ocIdexContainer.getContainerProperty(itemId, LOTE_PROPERTY).setValue(rsRecords.getString("Lote"));
                        ocIdexContainer.getContainerProperty(itemId, IDEX_PROPERTY).setValue(rsRecords.getString("Idex"));
                        ocIdexContainer.getContainerProperty(itemId, CUENTA_PROPERTY).setValue(rsRecords.getString("NoCuenta"));
                        ocIdexContainer.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(rsRecords.getString("CuentaDescripcion"));
                        ocIdexContainer.getContainerProperty(itemId, IDAREA_PROPERTY).setValue(rsRecords.getString("IdArea"));
                        ocIdexContainer.getContainerProperty(itemId, AREA_PROPERTY).setValue(rsRecords.getString("Area"));
                        ocIdexContainer.getContainerProperty(itemId, UNIDAD_PROPERTY).setValue(rsRecords.getString("Unidad"));
                        ocIdexContainer.getContainerProperty(itemId, EMPRESA_PROPERTY).setValue(rsRecords.getString("IdEmpresa"));
                        ocIdexContainer.getContainerProperty(itemId, PROVEEDOR_PROPERTY).setValue(rsRecords.getString("IdProveedor") + " " + rsRecords.getString("NombreProveedor"));
                        ocIdexContainer.getContainerProperty(itemId, MONEDA_PROPERTY).setValue(rsRecords.getString("Moneda"));
                        ocIdexContainer.getContainerProperty(itemId, CANTIDAD_PROPERTY).setValue(numberFormat3.format(rsRecords.getDouble("CantidadTotal")));
                        ocIdexContainer.getContainerProperty(itemId, PRECIO_PROPERTY).setValue(numberFormat.format(rsRecords.getDouble("PrecioTotal")));
                        ocIdexContainer.getContainerProperty(itemId, TOTAL_PROPERTY).setValue(numberFormat.format(rsRecords.getDouble("TotalTotal")));
                        ocIdexContainer.getContainerProperty(itemId, ULT_PRECIO_PROPERTY).setValue(
                                UltimoPrecio(
                                        rsRecords.getString("IdProject"),
                                        CENTROCOSTO,
                                        rsRecords.getString("Idex"),
                                        rsRecords.getString("NoCuenta"),
                                        rsRecords.getString("IdArea"),
                                        rsRecords.getString("IdEmpresa"),
                                        rsRecords.getString("IdProveedor")
                                )
                        );
                    }
                } while (rsRecords.next());
            }
        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error al listar tabla IDEX de project para OC.",  ex);
            ex.printStackTrace();
            Notification.show("ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }

    private String UltimoPrecio(
            String idProject,
            String idcc,
            String idex,
            String noCuenta,
            String idArea,
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
            queryString += " AND IdArea = " + idArea;
            queryString += " AND IdEmpresa = " + idEmpresa;
            queryString += " AND IdProveedor = " + idProveedor;
            queryString += " ORDER BY ID DESC Limit 1";

            rsRecords2 = stQuery2.executeQuery(queryString);

            if (rsRecords2.next()) { //  encontrado
                ultimoPrecio = rsRecords2.getString("Precio");
            }
        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error al obtener ULTIMO PRECIO DE IDEX.",  ex);
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
            String IDAREA,
            String EMPRESA,
            String PROVEEDOR
    ) {

        totalOC = 0.00;
        cantidadOC = 0.00;
        precioOC = 0.00;

        String
                queryString =  "SELECT SUM(Cantidad) TotalCantidad, SUM(Precio) TotalPrecio, SUM(Total) TotalTotal ";
        queryString += " FROM  DetalleItemsCostos ";
        queryString += " WHERE IDCC      = '" + CENTROCOSTO + "'";
        queryString += " AND IdProject   = " + PROJECT;
        queryString += " AND Idex        = '" + IDEX + "'";
        queryString += " AND NoCuenta    = '" + CUENTA + "'";
        queryString += " AND IdArea      = '" + IDAREA + "'";
        queryString += " AND IdEmpresa   = " + EMPRESA;
        queryString += " AND IdProveedor = " + PROVEEDOR;
        queryString += " AND Tipo = 'DOCA'";

//        System.out.println(queryString);


        try {

            stQuery2 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords2 = stQuery2.executeQuery (queryString);

            if(rsRecords2.next()) { //  encontrado
                cantidadOC = rsRecords2.getDouble("TotalCantidad");
                precioOC = rsRecords2.getDouble("TotalPrecio");
                totalOC = rsRecords2.getDouble("TotalTotal");
            }
        }
        catch (Exception ex) {
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
            String IDAREA,
            String EMPRESA,
            String PROVEEDOR
    ) {

        double saldoDoca = 0.00;

        String queryString =  "SELECT SUM(Total) TotalTotal ";
        queryString += " FROM  DocumentosContablesAplicados ";
        queryString += " WHERE IDCC      = '" + CENTROCOSTO + "'";
        queryString += " AND IdProject   = " + PROJECT;
        queryString += " AND Idex        = '" + IDEX + "'";
        queryString += " AND NoCuenta    = '" + CUENTA + "'";
//        queryString += " AND IdArea      = '" + IDAREA + "'";
        queryString += " AND IdEmpresa   = " + EMPRESA;
        queryString += " AND IdProveedor = " + PROVEEDOR;

//        System.out.println(queryString);

        try {

            stQuery2 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords2 = stQuery2.executeQuery (queryString);

            if(rsRecords2.next()) { //  encontrado
                saldoDoca = rsRecords2.getDouble("TotalTotal");
            }
        }
        catch (Exception ex) {
            Logger.getLogger(IntegracionActual.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error al intentar leer registros de detalle de integracion DCA : " + ex.getMessage());
            Notification.show("Error al intentar leer detalle de integracion DCA..! ", Notification.Type.ERROR_MESSAGE);
        }

        return saldoDoca;
    }

    public void verImagenPdf(String fileName) {

        try {

            String filePath = enviromentsVars.getDtePath();

            fileName = filePath + fileName;
            com.sun.istack.logging.Logger.getLogger(this.getClass()).log(Level.INFO, fileName);
            final byte docBytes[] = Files.readAllBytes(new File(fileName).toPath());

            if (docBytes == null) {
                Notification.show("Documento PDF no disponible para visualizar!");
                return;
            }
            Window window = new Window();
            window.setResizable(true);
            window.setWidth("50%");
            window.setHeight("50%");
            window.center();

            StreamResource documentStreamResource = null;

            if (docBytes != null) {
                documentStreamResource = new StreamResource(
                        new StreamResource.StreamSource() {
                            public InputStream getStream() {
                                return new ByteArrayInputStream(docBytes);
                            }
                        }, fileName
                );
            }
            documentStreamResource.setMIMEType("PDF");
            documentStreamResource.setFilename(fileName);
            documentStreamResource.getStream().setParameter("Content-Disposition", "attachment; filename=" + fileName);

//            System.out.println("Tipo de Archivo : " + rsRecords.getString("ArchivoTipo"));
//            System.out.println("Archivo Nombre :" + rsRecords.getString("ArchivoNombre"));
//            System.out.println("Tamanio :" + rsRecords.getString("ArchivoPeso"));
            window.setWidth("98%");
            window.setHeight("98%");

            VerticalLayout pdfLayout = new VerticalLayout();
            pdfLayout.setSizeFull();
            pdfLayout.setSpacing(true);

            BrowserFrame browserFrame = new BrowserFrame();
            browserFrame.setSizeFull();
            browserFrame.setSource(documentStreamResource);

            pdfLayout.addComponent(browserFrame);

            window.setContent(pdfLayout);

            pdfLayout.setExpandRatio(browserFrame, 2);

            UI.getCurrent().addWindow(window);
            window.center();

        } catch (Exception allEx) {
            Notification.show("No existe archivo PDF,  o no se puede leer el archivo.", Notification.Type.ERROR_MESSAGE);
            allEx.printStackTrace();
        }

    }

    void creaVisita() {
        String codigoVisita = String.format("%02d", Integer.valueOf(((SopdiUI) mainUI).sessionInformation.getStrProjectId()));
        codigoVisita += "01"; //reiunion
        codigoVisita += "10"; //cierre centro costo
        SimpleDateFormat df = new SimpleDateFormat("ddMMyy");
        codigoVisita += df.format(new java.util.Date());

        queryString = "SELECT CodigoVisita";
        queryString += " FROM  visita_inspeccion ";
        queryString += " WHERE IdProyecto = " + ((SopdiUI) mainUI).sessionInformation.getStrProjectId();
        queryString += " AND   CodigoVisita Like '" + codigoVisita + "%'";
        queryString += " ORDER BY CodigoVisita DESC";
        queryString += " LIMIT 1";

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado
                codigoVisita = rsRecords.getString("CodigoVisita");

                codigoVisita = codigoVisita.substring(0, 12) + String.format("%02d", Integer.valueOf(codigoVisita.substring(12, 14)) + 1);
            } else {
                codigoVisita += "01";
            }

            queryString = "INSERT INTO visita_inspeccion ";
            queryString += "(IdProyecto, CodigoVisita, FechaYHoraInicio, FechaYHoraFin, ";
            queryString += " Medio, Motivo, Visitas, IdCliente, IdCentroCosto, Referencia, ";
            queryString += " Participante1,Participante1Empresa,Participante1Email,";
            queryString += " PuntoAgenda1, CreadoUsuario, CreadoFechaYHora,Lugar,Observaciones) ";
            queryString += " VALUES (";
            queryString += "  " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrProjectId();
            queryString += ",'" + codigoVisita + "'";
            queryString += ",current_date";
            queryString += ",current_date";
            queryString += ",'Reunión'";
            queryString += ",'Cierre Centro Costo'";
            queryString += ",'Reunión Administrativa'"; //visitas
            queryString += ",'" + container.getContainerProperty(facturasVentaGrid.getSelectedRow(), IDCLIENTE_PROPERTY).getValue() + "'";
            queryString += ",'" + container.getContainerProperty(facturasVentaGrid.getSelectedRow(), CENTROCOSTO_PROPERTY).getValue() + "'";
            queryString += ",'" + container.getContainerProperty(facturasVentaGrid.getSelectedRow(), FACTURA_PROPERTY).getValue() + "'"; //REFERENCIA
            queryString += ",'GABRIEL ARRIAZA'";
            queryString += ",'NISA'";
            queryString += ",'gabrielarriaza@nisa.com.gt'";
            queryString += ",'CIERRE DE CENTRO DE COSTO'";
            queryString += ", " + ((SopdiUI) mainUI).sessionInformation.getStrUserId(); //creado usuario
            queryString += ",current_timestamp"; // creado fechayhora
            queryString += ",'PROYECTO'";
            queryString += ",'ORIGEN FACTURAS PENDIENTE CIERRE CENTRO DE COSTO'";
            queryString += ")";

            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "queryString VISITA = " + queryString);

            stPreparedQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().prepareStatement(queryString, Statement.RETURN_GENERATED_KEYS);
            stPreparedQuery.executeUpdate();
            rsRecords = stPreparedQuery.getGeneratedKeys();

            rsRecords.next();

            String idVisita = rsRecords.getString(1);

            queryString = "INSERT INTO visita_inspeccion_tarea (IdVisitaInspeccion, CodigoTarea, Rubro, ";
            queryString += " Descripcion, Instruccion, Responsable, Ejecutor, ";
            queryString += " EsTarea, Garantia, Presupuesto, AutorizadoTipo, IdCentroCosto, EquipoDibujo, ";
            queryString += " Estatus, FechaUltimoEstatus) ";
            queryString += " VALUES (";
            queryString += "  " + idVisita;
            queryString += ",'" + codigoVisita + "01'";
            queryString += ",'CIERRE DE CENTRO DE COSTO'";
            queryString += ",'ORDEN CAMBIO'";
            queryString += ",'HACER ORDEN DE CAMBIO PARA CERRAR CENTRO DE COSTO'";
            queryString += ",'GABRIEL ARRIAZA'";
            queryString += ",'GABRIEL ARRIAZA'";
            queryString += ",'SI'";
            queryString += ",'NO'";
            queryString += ",'SI'";
            queryString += ",'" + container.getContainerProperty(facturasVentaGrid.getSelectedRow(), CENTROCOSTO_PROPERTY).getValue() + "'";
            queryString += ",'GERENCIA'";
            queryString += ",'NO'";
            queryString += ",'PENDIENTE'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(new java.util.Date()) + "'";
            queryString += ")";

            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "queryString TAREA = " + queryString);

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            Notification.show("OPERACION EXITOSA!", Notification.Type.HUMANIZED_MESSAGE);

            llenarTablaFacturaVenta();

        }
        catch(Exception ex1) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE,"", ex1);
            Notification.show("ERROR AL INSERTAR VISITA PARA ORDEN DE CAMBIO", Notification.Type.ERROR_MESSAGE);
        }

    }

    private void liquidarDIC() {

        if(ocIdexGrid.getSelectedRows().size() == 0) {
            Notification.show("POR FAVOR SELECCIONE UNO O VARIAS LINEAS DIC.!!", Notification.Type.WARNING_MESSAGE);
            return;
        }

        try {

            ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().setAutoCommit(false);

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            queryString = "INSERT INTO DetalleItemsCostos ";
            queryString += " (NoCuenta, Descripcion, Precio, Cantidad, Total, IdProject, IDProveedor, ";
            queryString += " IdCC, IdArea, Lote, Moneda, IdEmpresa, Empresa, IDTarea, RazonOC, Unidad, ";
            queryString += " Tipo, FechaIngreso, NOC, OCInicial, IDEXAN, IDEX, CodItemPro, IDVisita) ";
            queryString += " VALUES ";

            for(Object itemObject : ocIdexContainer.getItemIds()) {

                if(ocIdexGrid.isSelected(itemObject)) {
                    queryString += "(";
                    queryString += "'" + ocIdexContainer.getContainerProperty(itemObject, CUENTA_PROPERTY).getValue() + "'";
                    queryString += ",'" + String.valueOf(ocIdexContainer.getContainerProperty(itemObject, DESCRIPCION_PROPERTY).getValue()).replaceAll("'", "pies").replaceAll("Ø", " ").replaceAll("ø", " ") + "'";
                    queryString += ", " + String.valueOf(ocIdexContainer.getContainerProperty(itemObject, PRECIO_PROPERTY).getValue()).replaceAll(",", "");
                    queryString += ", -" + String.valueOf(ocIdexContainer.getContainerProperty(itemObject, CANTIDAD_PROPERTY).getValue()).replaceAll(",", "");  //en negativo
                    queryString += ", -" + String.valueOf(ocIdexContainer.getContainerProperty(itemObject, TOTAL_PROPERTY).getValue()).replaceAll(",", "");
                    queryString += ","  + ocIdexContainer.getContainerProperty(itemObject, PROJECT_PROPERTY).getValue();
                    queryString += ", " + String.valueOf(ocIdexContainer.getContainerProperty(itemObject, PROVEEDOR_PROPERTY).getValue()).substring(0,5); //7
                    queryString += ",'" + container.getContainerProperty(facturasVentaGrid.getSelectedRow(), CENTROCOSTO_PROPERTY).getValue() + "'";
                    queryString += ", " + ocIdexContainer.getContainerProperty(itemObject, IDAREA_PROPERTY).getValue();
                    queryString += ",'" + ocIdexContainer.getContainerProperty(itemObject, LOTE_PROPERTY).getValue() + "'";
                    queryString += ",'" + ocIdexContainer.getContainerProperty(itemObject, MONEDA_PROPERTY).getValue() + "'";
                    queryString += ", " + empresa;
                    queryString += ",'" + empresaCbx.getItemCaption(empresaCbx.getValue()) + "'";
                    queryString += ",'0'"; //tarea
                    queryString += ",''"; //descripcion tarea
                    queryString += ",'" + ocIdexContainer.getContainerProperty(itemObject, UNIDAD_PROPERTY).getValue() + "'"; //16
                    queryString += ",'DOCA'";
                    queryString += ",'" + Utileria.getFechaYYYYMMDD_1(new java.util.Date()) + "'";
                    queryString += ",0"; //noc
                    queryString += ",0"; //OCInicial
                    queryString += "," + ocIdexContainer.getContainerProperty(itemObject, IDEX_PROPERTY).getValue(); //idexan
                    queryString += ",'" + ocIdexContainer.getContainerProperty(itemObject, IDEX_PROPERTY).getValue() + "'";
                    queryString += ",''"; //coditempro
                    queryString += ",'0'";
                    queryString += "),";
                }
            }

            queryString = queryString.substring(0, queryString.length()-1).replaceAll("\"", "plgds");

            Logger.getLogger(this.getClass().getName()).log(Level.INFO, queryString);

            stQuery.executeUpdate(queryString);

            creaVisita();

            ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().commit();
            ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().setAutoCommit(true);

            llenarTablaFacturaVenta();

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

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event
    ) {
        Page.getCurrent().setTitle("Sopdi - Factura venta CC DIC");
    }
}
