package com.simpletecno.sopdi.contabilidad;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.SelectionEvent;
import com.vaadin.event.SelectionEvent.SelectionListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.HeaderCell;
import com.vaadin.ui.Grid.HeaderRow;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;

/**
 *
 * @author user
 */
public class CuentaCorrienteDocumentosView extends Window {

    VerticalLayout mainLayout;

    static final String ID_PROPERTY = "Id";
    static final String CODIGO_PARTIDA_PROPERTY = "Codigo partida";
    static final String CODIGO_CC_PROPERTY = "CodigoCC";
    static final String ID_PROVEEDOR_PROPERTY = "Id Proveedor";
    static final String PROVEEDOR_PROPERTY = "Proveedor";
    static final String FECHA_PROPERTY = "Fecha";
    static final String FACTURA_PROPERTY = "Factura";
    static final String DESCRIPCION_PROPERTY = "Descripciòn";
    static final String MONEDA_PROPERTY = "Moneda";
    static final String MONTO_PROPERTY = "Monto";

    static final String ID2_PROPERTY = "Id";
    static final String CODIGO_PARTIDA2_PROPERTY = "Codigo partida";
    static final String FECHA2__PROPERTY = "Fecha";
    static final String DOCUMENTO_PROPERTY = "Cheque/ Transf";
    static final String MONEDA2_PROPERTY = "Moneda";
    static final String DEBE_PROPERTY = "Monto";
    static final String HABER_PROPERTY = "Haber";
    static final String CODIGO_CC2_PROPERTY = "CodigoCC";

    IndexedContainer facturasContainer = new IndexedContainer();
    Grid facturasGrid;
    Grid.FooterRow facturasFooter;

    IndexedContainer documentoPagoContainer = new IndexedContainer();
    Grid documentoPagoGrid;

    Button salirBtn;
    Button autorizarBtn;

    UI mainUI;
    Statement stQuery, stQuery1;
    ResultSet rsRecords, rsRecords1;
    String queryString;

    String codigoPartidaFactura = "";
    String codigoProveedor = "";

    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");
    static DecimalFormat numberFormat2 = new DecimalFormat("#,###,##0");

    static PreparedStatement stPreparedQuery;

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public CuentaCorrienteDocumentosView() {
        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setResponsive(true);

        HorizontalLayout layoutTitle;
        layoutTitle = new HorizontalLayout();
        layoutTitle.setSpacing(true);
        layoutTitle.setMargin(true);
        layoutTitle.setWidth("100%");

        this.mainUI = UI.getCurrent();
        setResponsive(true);
        setWidth("90%");
        setHeight("90%");


        Label titleLbl = new Label(empresaId + " " + empresaNombre + " Consulta de documentos de pago");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h2_custom");

        layoutTitle.addComponent(titleLbl);
        layoutTitle.setComponentAlignment(titleLbl, Alignment.BOTTOM_RIGHT);

        mainLayout.addComponent(layoutTitle);

        setContent(mainLayout);
        crearGridFactura();
        llenarGridFactura();

        crearGridAnticipos();

        crearComponentes();

    }

    public void crearGridFactura() {

        HorizontalLayout documentosLayout = new HorizontalLayout();
        documentosLayout.setWidth("100%");
        documentosLayout.addStyleName("rcorners3");
        documentosLayout.setResponsive(true);
        documentosLayout.setMargin(true);
        documentosLayout.setSpacing(true);

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setMargin(new MarginInfo(false, true, false, false));
        buttonsLayout.setSpacing(true);

        facturasContainer.addContainerProperty(ID_PROPERTY, String.class, null);
        facturasContainer.addContainerProperty(CODIGO_PARTIDA_PROPERTY, String.class, null);
        facturasContainer.addContainerProperty(CODIGO_CC_PROPERTY, String.class, null);
        facturasContainer.addContainerProperty(ID_PROVEEDOR_PROPERTY, String.class, null);
        facturasContainer.addContainerProperty(PROVEEDOR_PROPERTY, String.class, null);
        facturasContainer.addContainerProperty(FECHA_PROPERTY, String.class, null);
        facturasContainer.addContainerProperty(FACTURA_PROPERTY, String.class, null);
        facturasContainer.addContainerProperty(DESCRIPCION_PROPERTY, String.class, null);
        facturasContainer.addContainerProperty(MONEDA_PROPERTY, String.class, null);
        facturasContainer.addContainerProperty(MONTO_PROPERTY, String.class, null);

        facturasGrid = new Grid("Factura", facturasContainer);

        facturasGrid.setWidth("100%");
        facturasGrid.setImmediate(true);
        facturasGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        facturasGrid.setDescription("Seleccione un registro.");
        facturasGrid.setHeightMode(HeightMode.ROW);
        facturasGrid.setHeightByRows(2);
        facturasGrid.setResponsive(true);
        facturasGrid.setEditorBuffered(false);
        facturasGrid.setResponsive(true);
        facturasGrid.setEditorBuffered(false);

        facturasGrid.getColumn(ID_PROPERTY).setHidable(true).setHidden(true);

        facturasGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {

            if (MONTO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (MONTO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }

        });

        facturasGrid.addSelectionListener(new SelectionListener() {
            @Override
            public void select(SelectionEvent event) {

                if (facturasGrid.getSelectedRow() != null) {
                    codigoProveedor = String.valueOf(facturasContainer.getContainerProperty(facturasGrid.getSelectedRow(), ID_PROVEEDOR_PROPERTY).getValue());
                    codigoPartidaFactura = String.valueOf(facturasContainer.getContainerProperty(facturasGrid.getSelectedRow(), CODIGO_PARTIDA_PROPERTY).getValue());
                    llenarTablaDocumentos();
                }
            }
        });

        HeaderRow filterRow = facturasGrid.appendHeaderRow();

        HeaderCell cell = filterRow.getCell(PROVEEDOR_PROPERTY);

        TextField filterField = new TextField();
        filterField.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField.setInputPrompt("Filtrar");
        filterField.setColumns(10);

        filterField.addTextChangeListener(change -> {
            facturasContainer.removeContainerFilters(PROVEEDOR_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                facturasContainer.addContainerFilter(
                        new SimpleStringFilter(PROVEEDOR_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell.setComponent(filterField);

        HeaderCell cell0 = filterRow.getCell(FACTURA_PROPERTY);

        TextField filterField0 = new TextField();
        filterField0.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField0.setInputPrompt("Filtrar");
        filterField0.setColumns(8);

        filterField0.addTextChangeListener(change -> {
            facturasContainer.removeContainerFilters(FACTURA_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                facturasContainer.addContainerFilter(
                        new SimpleStringFilter(FACTURA_PROPERTY,
                                change.getText(), true, false));
            }
        });

        cell0.setComponent(filterField0);

        HeaderCell cell1 = filterRow.getCell(MONEDA_PROPERTY);

        TextField filterField1 = new TextField();
        filterField1.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField1.setInputPrompt("Filtrar");
        filterField1.setColumns(6);

        filterField1.addTextChangeListener(change -> {
            facturasContainer.removeContainerFilters(MONEDA_PROPERTY);
            if (!change.getText().isEmpty()) {
                facturasContainer.addContainerFilter(new SimpleStringFilter(MONEDA_PROPERTY, change.getText(), true, false));
            }
        });
        cell1.setComponent(filterField1);

        facturasFooter = facturasGrid.appendFooterRow();
        facturasFooter.getCell(DESCRIPCION_PROPERTY).setText("TOTALES");
        facturasFooter.getCell(MONEDA_PROPERTY).setText("QUETZALES");
        facturasFooter.getCell(MONTO_PROPERTY).setText("0.00");

        facturasFooter.getCell(DESCRIPCION_PROPERTY).setStyleName("rightalign");
        facturasFooter.getCell(MONTO_PROPERTY).setStyleName("rightalign");

        documentosLayout.addComponent(facturasGrid);
        documentosLayout.setComponentAlignment(facturasGrid, Alignment.MIDDLE_CENTER);

        mainLayout.addComponent(documentosLayout);
        mainLayout.setComponentAlignment(documentosLayout, Alignment.MIDDLE_CENTER);

        mainLayout.addComponent(buttonsLayout);
        mainLayout.setComponentAlignment(buttonsLayout, Alignment.TOP_RIGHT);
    }

    public void llenarGridFactura() {
        documentoPagoContainer.removeAllItems();
        facturasContainer.removeAllItems();
        facturasContainer.removeAllContainerFilters();

        facturasFooter.getCell(MONTO_PROPERTY).setText("0.00");

        queryString = " SELECT contabilidad_partida.CodigoPartida, contabilidad_partida.IdPartida, contabilidad_partida.Fecha, ";
        queryString += " contabilidad_partida.IdProveedor, contabilidad_partida.NombreProveedor ,";
        queryString += " contabilidad_partida.SerieDocumento, contabilidad_partida.NumeroDocumento, ";
        queryString += " contabilidad_partida.MonedaDocumento, contabilidad_partida.TipoCambio, ";
        queryString += " contabilidad_partida.TipoDocumento,contabilidad_partida.ArchivoNombre, contabilidad_partida.ArchivoTipo, ";
        queryString += " contabilidad_partida.MontoAutorizadoPagar, usuario.Nombre as uNombre,";
        queryString += " Sum(contabilidad_partida.Haber) Total, Sum(contabilidad_partida.HaberQuetzales) TotalQ,";
        queryString += " contabilidad_partida.Saldo, contabilidad_partida.Descripcion, contabilidad_partida.MontoAutorizadoPagar, contabilidad_partida.MontoAplicarAnticipo, ";
        queryString += " DATEDIFF(CURDATE(),contabilidad_partida.Fecha) as DiasHoy ";
        queryString += " FROM contabilidad_partida, usuario ";
        queryString += " WHERE contabilidad_partida.IdEmpresa = " + empresaId;
        queryString += " AND contabilidad_partida.Estatus = 'REVISADO'";
        queryString += " AND UPPER(contabilidad_partida.TipoDocumento) IN ('FACTURA', 'RECIBO','RECIBO CONTABLE', 'FORMULARIO','NOTA DE CREDITO')";
        queryString += " AND contabilidad_partida.IdLiquidacion = 0 "; // SOLO FACTURAS PROVEEDORES COMPRA
        queryString += " AND contabilidad_partida.SALDO > 0 ";
        queryString += " AND usuario.IdUsuario = contabilidad_partida.CreadoUsuario ";
        queryString += " GROUP BY contabilidad_partida.CodigoPartida";
        queryString += " ORDER BY contabilidad_partida.NombreProveedor";

//        System.out.println("query autorizar pagos documentos = " + queryString);

        try {

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {

                String monedaSimbolo;

                do {

                    Object itemId = facturasContainer.addItem();

                    try {
                        facturasContainer.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords.getString("IdPartida"));
                    } catch (java.lang.NullPointerException npe) {
//                        Notification.show("OCURRIO UN ERROR INESPERADO DEL SISTEMA, POR FAVOR VUELVA A INTENTAR LA ACCION", Notification.Type.ERROR_MESSAGE);
                        return;
                    }

                    facturasContainer.getContainerProperty(itemId, CODIGO_PARTIDA_PROPERTY).setValue(rsRecords.getString("CodigoPartida"));
                    facturasContainer.getContainerProperty(itemId, ID_PROVEEDOR_PROPERTY).setValue(rsRecords.getString("IdProveedor"));
                    facturasContainer.getContainerProperty(itemId, PROVEEDOR_PROPERTY).setValue(rsRecords.getString("NombreProveedor"));
                    facturasContainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha")));
                    facturasContainer.getContainerProperty(itemId, FACTURA_PROPERTY).setValue(rsRecords.getString("SerieDocumento") + " " + rsRecords.getString("NumeroDocumento"));
                    facturasContainer.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(rsRecords.getString("Descripcion"));
                    facturasContainer.getContainerProperty(itemId, MONEDA_PROPERTY).setValue(rsRecords.getString("MonedaDocumento"));
                    facturasContainer.getContainerProperty(itemId, MONTO_PROPERTY).setValue(numberFormat.format(rsRecords.getDouble("Total")));

                } while (rsRecords.next());

            }

        } catch (Exception ex) {
            System.out.println("Error al listar tabla Facturas cuenta corriente Documentos: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void crearGridAnticipos() {

        HorizontalLayout reportLayout = new HorizontalLayout();
        reportLayout.setWidth("100%");
        reportLayout.addStyleName("rcorners3");
        reportLayout.setResponsive(true);
        reportLayout.setMargin(true);

        documentoPagoContainer.addContainerProperty(ID2_PROPERTY, String.class, null);
        documentoPagoContainer.addContainerProperty(CODIGO_PARTIDA2_PROPERTY, String.class, null);
        documentoPagoContainer.addContainerProperty(CODIGO_CC2_PROPERTY, String.class, null);
        documentoPagoContainer.addContainerProperty(FECHA2__PROPERTY, String.class, null);
        documentoPagoContainer.addContainerProperty(DOCUMENTO_PROPERTY, String.class, null);
        documentoPagoContainer.addContainerProperty(DOCUMENTO_PROPERTY, String.class, null);
        documentoPagoContainer.addContainerProperty(MONEDA2_PROPERTY, String.class, null);
        documentoPagoContainer.addContainerProperty(DEBE_PROPERTY, String.class, null);
        documentoPagoContainer.addContainerProperty(HABER_PROPERTY, String.class, null);

        documentoPagoGrid = new Grid("Partida pago", documentoPagoContainer);

        documentoPagoGrid.setImmediate(true);
        documentoPagoGrid.setSelectionMode(Grid.SelectionMode.NONE);
        documentoPagoGrid.setDescription("Seleccione un registro para ingresar o editar.");
        documentoPagoGrid.setHeightMode(HeightMode.ROW);
        documentoPagoGrid.setHeightByRows(5);
        documentoPagoGrid.setWidth("100%");
        documentoPagoGrid.setResponsive(true);
        documentoPagoGrid.setEditorBuffered(false);
        documentoPagoGrid.setSizeFull();
        documentoPagoGrid.setEditorEnabled(true);
           
        reportLayout.addComponent(documentoPagoGrid);
        reportLayout.setComponentAlignment(documentoPagoGrid, Alignment.TOP_CENTER);
      
        mainLayout.addComponent(reportLayout);
        mainLayout.setComponentAlignment(reportLayout, Alignment.TOP_CENTER);

    }

   
    public void llenarTablaDocumentos() {

        documentoPagoContainer.removeAllItems();
        documentoPagoContainer.removeAllContainerFilters();

        double saldoTotalLiquidar = 0.00;
        double montoAnticipos = 0.00;

        String proveedorSeleccionado = String.valueOf(facturasContainer.getContainerProperty(facturasGrid.getSelectedRow(), ID_PROVEEDOR_PROPERTY).getValue());
        String tipoMonedaSeleccionado = String.valueOf(facturasContainer.getContainerProperty(facturasGrid.getSelectedRow(), MONEDA_PROPERTY).getValue());

        queryString = " SELECT contabilidad_partida.IdPartida, contabilidad_partida.CodigoPartida, contabilidad_partida.Fecha,";
        queryString += " contabilidad_partida.TipoDocumento,contabilidad_partida.NumeroDocumento, ";
        queryString += " contabilidad_partida.Descripcion,contabilidad_partida.MonedaDocumento,";
        queryString += " contabilidad_partida.Debe, contabilidad_partida.Haber,contabilidad_partida.TipoCambio,";
        queryString += " contabilidad_partida.DebeQuetzales, contabilidad_partida.HaberQuetzales, contabilidad_partida.Saldo";
        queryString += " FROM contabilidad_partida";
        queryString += " WHERE contabilidad_partida.IdProveedor = " + proveedorSeleccionado;
        queryString += " AND contabilidad_partida.IdEmpresa = " + empresaId;
        queryString += " AND contabilidad_partida.IdNomenclatura = " + ((SopdiUI) mainUI).cuentasContablesDefault.getProveedores();
        queryString += " and contabilidad_partida.TipoDocumento In ('CHEQUE', 'TRANSFERENCIA', 'NOTA DE CREDITO')";
        queryString += " AND contabilidad_partida.MonedaDocumento = '" + tipoMonedaSeleccionado + "'";
        queryString += " and contabilidad_partida.Saldo > 0.00";
////colcar condicion por tipo de moneda

        try {
            stQuery1 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords1 = stQuery1.executeQuery(queryString);
            if (rsRecords1.next()) { //  encontrado      

                do {

                    Object itemId = documentoPagoContainer.addItem();
                    documentoPagoContainer.getContainerProperty(itemId, ID2_PROPERTY).setValue(rsRecords1.getString("IdPartida"));
                    documentoPagoContainer.getContainerProperty(itemId, CODIGO_PARTIDA2_PROPERTY).setValue(rsRecords1.getString("CodigoPartida"));
                    documentoPagoContainer.getContainerProperty(itemId, FECHA2__PROPERTY).setValue(rsRecords1.getString("Fecha"));
                    documentoPagoContainer.getContainerProperty(itemId, DOCUMENTO_PROPERTY).setValue(rsRecords1.getString("TipoDocumento"));
                    documentoPagoContainer.getContainerProperty(itemId, MONEDA2_PROPERTY).setValue(rsRecords1.getString("MonedaDocumento"));
                    documentoPagoContainer.getContainerProperty(itemId, DEBE_PROPERTY).setValue(rsRecords1.getString("Debe"));
                    documentoPagoContainer.getContainerProperty(itemId, HABER_PROPERTY).setValue(numberFormat.format((rsRecords1.getDouble("haber"))));                    
                    documentoPagoContainer.getContainerProperty(itemId, CODIGO_CC2_PROPERTY).setValue(rsRecords1.getString("CodigoCC"));

                    montoAnticipos += rsRecords1.getDouble("Saldo");
                    saldoTotalLiquidar += rsRecords1.getDouble("Saldo");
                } while (rsRecords1.next());
             
            }
        } catch (Exception ex) {
            System.out.println("Error al listar grid anticipos factura:" + ex);
            ex.printStackTrace();
        }
    }

    public void crearComponentes() {

        HorizontalLayout camposDocumento = new HorizontalLayout();
        camposDocumento.setWidth("96%");
        autorizarBtn = new Button("Autorizar");
        autorizarBtn.setIcon(FontAwesome.SAVE);
        autorizarBtn.setStyleName(ValoTheme.BUTTON_PRIMARY);
        autorizarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                ///
            }
        });

        salirBtn = new Button("Salir");
        salirBtn.setWidth("7em");
        salirBtn.setStyleName(ValoTheme.BUTTON_DANGER);
        salirBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();
            }
        });

        camposDocumento.addComponents(salirBtn, autorizarBtn);
        camposDocumento.setComponentAlignment(salirBtn, Alignment.MIDDLE_LEFT);
        camposDocumento.setComponentAlignment(autorizarBtn, Alignment.MIDDLE_RIGHT);

        mainLayout.addComponent(camposDocumento);
        mainLayout.setComponentAlignment(camposDocumento, Alignment.MIDDLE_CENTER);
    }
}
