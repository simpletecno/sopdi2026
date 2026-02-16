
package com.simpletecno.sopdi.tesoreria;

import com.simpletecno.sopdi.*;
import com.simpletecno.sopdi.contabilidad.MostrarPartidaContable;
import com.simpletecno.sopdi.utilerias.MyEmailMessanger;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.addon.tableexport.DefaultTableHolder;
import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.addon.tableexport.TableHolder;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.SelectionEvent;
import com.vaadin.event.SelectionEvent.SelectionListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.HeaderCell;
import com.vaadin.ui.Grid.HeaderRow;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.ui.NumberField;

import javax.mail.MessagingException;

/**
 * @author user
 */
public class PagoFacturaProveedorForm extends Window {

    static final String ID_PROPERTY = "Id";
    static final String TIPO_PROPERTY = "Tipo";
    static final String FECHA_PROPERTY = "Fecha";
    static final String PROVEEDOR_PROPERTY = "Proveedor";
    static final String CODIGO_PROPERTY = "Id Proveedor";
    static final String FACTURA_PROPERTY = "Documento";
    static final String MONEDA_PROPERTY = "Moneda";
    static final String VALOR_PROPERTY = "Monto";
    static final String TIPOCAMBIO_PROPERTY = "Tasa";
    static final String MONTO_AUTORIZADO_PROPERTY = "Monto";
    static final String ANTICIPO_PROPERTY = "A.Anticipo";
    static final String CUENTA_PROPERTY = "IdNomenclatura";
    static final String HABER_PROPERTY = "Haber";
    static final String HABER_Q_PROPERTY = "Haber Q";
    static final String DEBE_PROPERTY = "Debe";
    static final String DEBE_Q_PROPERTY = "Debe Q";
    static final String CODIGOCC_PROPERTY = "CodigoCC";
    static final String DOCUMENTO_PROPERTY = "Documento";

    static final String DESCRIPCION_PROPERTY = "Descripción";

    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");
    static DecimalFormat numberFormat3 = new DecimalFormat("######0.00");

    Statement stQuery;
    ResultSet rsRecords;
    Statement stQueryFacturas;
    ResultSet rsRecordsFacturas;
    Statement stQuery2;
    ResultSet rsRecords2;

    VerticalLayout mainLayout;

    public IndexedContainer facturasContainer = new IndexedContainer();
    IndexedContainer partidaContainer = new IndexedContainer();
    Grid facturasGrid;
    Grid.FooterRow footerFacturas;
    Grid partidaGrid;

    HashMap<String, String> cuentasContables = new HashMap<String, String>();

    ComboBox proveedorCbx;
    ComboBox monedaCbx;
    ComboBox medioCbx;
    DateField fechaDt;
    NumberField montoTxt;
    NumberField tasaCambioTxt;
    TextField nombreChequeTxt;
    TextField descripcionTxt;
    TextField numeroTxt;

    HorizontalLayout chequeLayout = new HorizontalLayout();
    HorizontalLayout chequeLayout2 = new HorizontalLayout();
    VerticalLayout partidaLayout = new VerticalLayout();

    double totalMonto;
    double totalQueztales;
    double totalAnticipo;

    Button excelBtn;

    String tipoDocumentoPagado;

    Button grabarPartidaBtn;
    Button desAutorizarBtn;
    ComboBox empresaCbx;

    String proveedorNombre;
    String proveedorId;
    String idNomenclatura;
    String codigoPartida;
    String codigoPartidaNuevo;
    String codigoCC;

    String partidasPagadas;
    String facturasPagadas;
    ArrayList<String> codigoAnticipoList = new ArrayList<String>();

    BigDecimal totalDebe;
    BigDecimal totalHaber;
    BigDecimal totalDebeQ;
    BigDecimal totalHaberQ;

    String IdProveedor = "";

    String codigo;

    String queryString;
    
    Date fechaPago;

    boolean calcular = true;

    UI mainUI;

    public PagoFacturaProveedorForm(String IdProveedor, Date fechaPago) {

        this.mainUI = UI.getCurrent();
        this.IdProveedor = IdProveedor;
        this.fechaPago = fechaPago;

        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setResponsive(true);

        setContent(mainLayout);

        setResponsive(true);
        setWidth("90%");
        setHeight("90%");

        excelBtn = new Button("Excel");
        excelBtn.setIcon(FontAwesome.FILE_EXCEL_O);
        excelBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);

        excelBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (facturasContainer.size() > 0) {
                    exportToExcel(facturasGrid);
                } else {
                    Notification.show("La vista no contiene registros disponibles.. ", Notification.Type.WARNING_MESSAGE);
                }
            }
        });

        Label titleLbl = new Label("PAGO DE DOCUMENTOS");
        if (mainUI.getPage().getBrowserWindowWidth() >= 736) {
            titleLbl.addStyleName(ValoTheme.LABEL_H2);
        }
        else {
            titleLbl.addStyleName(ValoTheme.LABEL_H4);
        }
        titleLbl.setSizeUndefined();

        empresaCbx = new ComboBox("Empresa:");
        empresaCbx.setWidth("400px");
        empresaCbx.addStyleName(ValoTheme.COMBOBOX_HUGE);
        empresaCbx.setInvalidAllowed(false);
        empresaCbx.setNewItemsAllowed(false);
        empresaCbx.setTextInputAllowed(false);
        empresaCbx.setNullSelectionAllowed(false);
        llenarComboEmpresa();

        proveedorCbx = new ComboBox("Proveedor : ");
        proveedorCbx.setWidth("35em");
        proveedorCbx.setVisible(false);
        proveedorCbx.setFilteringMode(FilteringMode.CONTAINS);
        proveedorCbx.addValueChangeListener(event -> {
            //llenarTablaLiquidacion();
            nombreChequeTxt.setReadOnly(false);
            nombreChequeTxt.setValue(proveedorCbx.getItemCaption(proveedorCbx.getValue()));
            nombreChequeTxt.setReadOnly(true);

        });

        llenarComboProveedor();

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setResponsive(true);
        titleLayout.setSpacing(true);
        titleLayout.setWidth("100%");
        titleLayout.setMargin(false);
        titleLayout.addComponents(empresaCbx, titleLbl, excelBtn);
        titleLayout.setComponentAlignment(empresaCbx, Alignment.MIDDLE_CENTER);
        titleLayout.setComponentAlignment(titleLbl, Alignment.MIDDLE_CENTER);
        titleLayout.setComponentAlignment(excelBtn, Alignment.MIDDLE_CENTER);
        titleLayout.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

        mainLayout.addComponent(titleLayout);
        mainLayout.setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        mainLayout.addComponent(proveedorCbx);
        mainLayout.setComponentAlignment(proveedorCbx, Alignment.TOP_CENTER);

        crearGridFacturas();
        llenarGridFacturas();
        crearLayoutCheque();
        crearPartidaLayout();
        limpiarPartida();

    }

    public void llenarComboEmpresa() {
        queryString = " SELECT * from contabilidad_empresa";
        queryString += " Where IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();

        try {
            stQuery2 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords2 = stQuery2.executeQuery(queryString);

            while (rsRecords2.next()) { //  encontrado
                empresaCbx.addItem(rsRecords2.getString("IdEmpresa"));
                empresaCbx.setItemCaption(rsRecords2.getString("IdEmpresa"), rsRecords2.getString("Empresa"));
            }
            rsRecords2.first();

            empresaCbx.select(rsRecords2.getString("IdEmpresa"));

        } catch (Exception ex1) {
            System.out.println("Error al listar empresas: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void crearGridFacturas() {

        VerticalLayout facturasLayout = new VerticalLayout();
        facturasLayout.addStyleName("rcorners3");
        facturasLayout.setWidth("100%");
        facturasLayout.setResponsive(true);
        facturasLayout.setSpacing(true);
        facturasLayout.setMargin(false);

        partidaContainer.addContainerProperty(CUENTA_PROPERTY, String.class, null);
        partidaContainer.addContainerProperty(DESCRIPCION_PROPERTY, String.class, null);
        partidaContainer.addContainerProperty(DEBE_PROPERTY, String.class, null);
        partidaContainer.addContainerProperty(HABER_PROPERTY, String.class, null);
        partidaContainer.addContainerProperty(DEBE_Q_PROPERTY, String.class, null);
        partidaContainer.addContainerProperty(HABER_Q_PROPERTY, String.class, null);
        partidaContainer.addContainerProperty(CODIGOCC_PROPERTY, String.class, null);

        facturasContainer.addContainerProperty(ID_PROPERTY, String.class, null);
        facturasContainer.addContainerProperty(TIPO_PROPERTY, String.class, null);
        facturasContainer.addContainerProperty(FECHA_PROPERTY, String.class, null);
        facturasContainer.addContainerProperty(PROVEEDOR_PROPERTY, String.class, null);
        facturasContainer.addContainerProperty(CODIGO_PROPERTY, String.class, null);
        facturasContainer.addContainerProperty(FACTURA_PROPERTY, String.class, null);
        facturasContainer.addContainerProperty(MONEDA_PROPERTY, String.class, null);
        facturasContainer.addContainerProperty(VALOR_PROPERTY, String.class, null);
        facturasContainer.addContainerProperty(MONTO_AUTORIZADO_PROPERTY, String.class, null);
        facturasContainer.addContainerProperty(ANTICIPO_PROPERTY, String.class, null);
        facturasContainer.addContainerProperty(CUENTA_PROPERTY, String.class, null);
        facturasContainer.addContainerProperty(HABER_PROPERTY, String.class, null);
        facturasContainer.addContainerProperty(HABER_Q_PROPERTY, String.class, null);
        facturasContainer.addContainerProperty(CODIGOCC_PROPERTY, String.class, null);

        facturasGrid = new Grid("", facturasContainer);
        facturasGrid.setWidth("100%");
        facturasGrid.setImmediate(true);
        facturasGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        facturasGrid.setDescription("Seleccione uno o varios registros del mismo proveedor.");
        facturasGrid.setHeightMode(HeightMode.ROW);
        facturasGrid.setHeightByRows(5);
        facturasGrid.setResponsive(true);
        facturasGrid.setEditorBuffered(false);

        facturasGrid.addSelectionListener(
            new SelectionListener() {
                @Override
                public void select(SelectionEvent event) {
                    if (facturasGrid.getSelectedRows() != null) {
                        calcularPartida();
                    }
                }
            }
        );

        facturasGrid.getColumn(ID_PROPERTY).setHidable(true).setHidden(true);
        facturasGrid.getColumn(TIPO_PROPERTY).setHidable(true).setHidden(true);
        facturasGrid.getColumn(CODIGO_PROPERTY).setHidable(true).setHidden(true);
        facturasGrid.getColumn(FECHA_PROPERTY).setHidable(true).setHidden(true);
        facturasGrid.getColumn(CUENTA_PROPERTY).setHidable(true).setHidden(true);
        facturasGrid.getColumn(HABER_PROPERTY).setHidable(true).setHidden(true);
        facturasGrid.getColumn(CODIGOCC_PROPERTY).setHidable(true).setHidden(true);

        facturasGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {

                    if (MONTO_AUTORIZADO_PROPERTY.equals(cellReference.getPropertyId())) {
                        return "rightalign";
                    } else if (VALOR_PROPERTY.equals(cellReference.getPropertyId())) {
                        return "rightalign";
                    } else if (ANTICIPO_PROPERTY.equals(cellReference.getPropertyId())) {
                        return "rightalign";
                    } else if (TIPOCAMBIO_PROPERTY.equals(cellReference.getPropertyId())) {
                        return "centeralign";
                    } else {
                        return null;
                    }
                }
        );

        HeaderRow filterRow = facturasGrid.appendHeaderRow();

        HeaderCell cell = filterRow.getCell(FACTURA_PROPERTY);

        TextField filterField = new TextField();
        filterField.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField.setInputPrompt("Filtrar");
        filterField.setColumns(8);

        filterField.addTextChangeListener(change
                -> {
            facturasContainer.removeContainerFilters(FACTURA_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                facturasContainer.addContainerFilter(
                        new SimpleStringFilter(FACTURA_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell.setComponent(filterField);

        HeaderCell cell1 = filterRow.getCell(MONEDA_PROPERTY);

        TextField filterField1 = new TextField();
        filterField1.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField1.setInputPrompt("Filtrar");
        filterField1.setColumns(8);
        filterField1.addTextChangeListener(change
                -> {
            facturasContainer.removeContainerFilters(MONEDA_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                facturasContainer.addContainerFilter(
                        new SimpleStringFilter(MONEDA_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell1.setComponent(filterField1);

        HeaderCell cell2 = filterRow.getCell(PROVEEDOR_PROPERTY);

        TextField filterField2 = new TextField();
        filterField2.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField2.setInputPrompt("Filtrar");
        filterField2.setColumns(20);

        filterField2.addTextChangeListener(change
                -> {
            facturasContainer.removeContainerFilters(PROVEEDOR_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                facturasContainer.addContainerFilter(
                        new SimpleStringFilter(PROVEEDOR_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell2.setComponent(filterField2);

        footerFacturas = facturasGrid.appendFooterRow();

        footerFacturas.getCell(MONEDA_PROPERTY).setText("Totales");
        footerFacturas.getCell(VALOR_PROPERTY).setText("0.00");
        footerFacturas.getCell(MONTO_AUTORIZADO_PROPERTY).setText("0.00");
        footerFacturas.getCell(ANTICIPO_PROPERTY).setText("0.00");
        footerFacturas.getCell(VALOR_PROPERTY).setStyleName("rightalign");
        footerFacturas.getCell(MONTO_AUTORIZADO_PROPERTY).setStyleName("rightalign");
        footerFacturas.getCell(ANTICIPO_PROPERTY).setStyleName("rightalign");

        facturasLayout.addComponent(facturasGrid);
        facturasLayout.setComponentAlignment(facturasGrid, Alignment.MIDDLE_CENTER);

        mainLayout.addComponent(facturasLayout);
        mainLayout.setComponentAlignment(facturasLayout, Alignment.MIDDLE_CENTER);

    }

    private void calcularPartida() {
        if(!calcular) {
            return;
        }
        partidaContainer.removeAllItems();

        Double montoPagar = 0.00;
        Double montoAnticipo = 0.00;
        Double porcentajeProporcional = 0.00;
        Double montoQuetzales = 0.00;
        Double montoMoneda = 0.00;
        String moneda = "";
        Object gridItem;

        totalDebe = new BigDecimal(montoPagar);
        totalDebeQ = new BigDecimal((montoPagar * tasaCambioTxt.getDoubleValueDoNotThrow()));
        totalHaber = new BigDecimal(montoPagar);
        totalHaberQ = new BigDecimal((montoPagar * tasaCambioTxt.getDoubleValueDoNotThrow()));

        facturasPagadas = "";
        partidasPagadas = "";
        proveedorNombre = "";
        proveedorId = "0";
        tipoDocumentoPagado = "";
        idNomenclatura = ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getProveedores();

        codigoAnticipoList.clear();

        Iterator facturasGridIter = facturasGrid.getSelectedRows().iterator();

        if (facturasGridIter == null || !facturasGridIter.hasNext()) {
            proveedorCbx.setReadOnly(false);

            limpiarPartida();
            codigoAnticipoList.clear();

            proveedorCbx.setReadOnly(false);
            proveedorCbx.select(proveedorId);
            proveedorCbx.setReadOnly(true);

            return;
        }

        gridItem = facturasGridIter.next();
        proveedorId = String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(CODIGO_PROPERTY).getValue());
        proveedorNombre = String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(PROVEEDOR_PROPERTY).getValue());
        moneda = String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(MONEDA_PROPERTY).getValue());
        montoPagar = Double.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(MONTO_AUTORIZADO_PROPERTY).getValue()).replaceAll(",", ""));
        montoAnticipo = Double.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(ANTICIPO_PROPERTY).getValue()).replaceAll(",", ""));

        facturasPagadas = String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(DOCUMENTO_PROPERTY).getValue()) + ",";
        partidasPagadas = String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(ID_PROPERTY).getValue()) + ",";
        tipoDocumentoPagado = String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(TIPO_PROPERTY).getValue());
        idNomenclatura = String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(CUENTA_PROPERTY).getValue());
        codigoAnticipoList.add(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(ID_PROPERTY).getValue()));

        codigoCC = String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(CODIGOCC_PROPERTY).getValue());

        codigo = String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(CODIGO_PROPERTY).getValue());

        monedaCbx.setReadOnly(false);
        monedaCbx.select(moneda);
        monedaCbx.setReadOnly(true);

        calcular = false;
        if(moneda.equals("DOLARES")) {
            if(tasaCambioTxt.getDoubleValueDoNotThrow() == 1.0) {
                tasaCambioTxt.setValue((Float.toString(((SopdiUI) UI.getCurrent()).sessionInformation.getFltExchangeRate())));
            }
        }
        else {
            tasaCambioTxt.setValue(1.00);
        }
        calcular = true;

        proveedorCbx.setReadOnly(false);
        proveedorCbx.select(proveedorId);
        proveedorCbx.setReadOnly(true);

        montoTxt.setReadOnly(false);
        montoTxt.setValue(montoPagar);

        while (facturasGridIter.hasNext()) {   //// Si hay mas de un registro seleccionado
            gridItem = facturasGridIter.next();
            //VALIDAR QUE SEA LA MISMA MONEDA
            if (!moneda.equals(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(MONEDA_PROPERTY).getValue()))) {
                Notification.show("SOLO SE PUEDEN PAGAR VARIAS FACTURAS DEL MISMO PROVEEDOR Y DE LA MISMA MONEDA, REVISE!", Notification.Type.WARNING_MESSAGE);
                facturasGrid.deselect(gridItem);
                return;
            }
            if (!idNomenclatura.equals(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(CUENTA_PROPERTY).getValue()))) {
                Notification.show("SOLO SE PUEDEN PAGAR VARIAS FACTURAS DE LA MISMA CUENTA CONTABLE, REVISE!", Notification.Type.WARNING_MESSAGE);
                facturasGrid.deselect(gridItem);
                return;
            }
            if (!tipoDocumentoPagado.equals(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(TIPO_PROPERTY).getValue()))) {
                Notification.show("SOLO SE PUEDEN PAGAR DOCUMENTOS DEL MISMO TIPO, REVISE!", Notification.Type.WARNING_MESSAGE);
                facturasGrid.deselect(gridItem);
                return;
            }

            montoPagar += Double.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(MONTO_AUTORIZADO_PROPERTY).getValue()).replaceAll(",", ""));
            montoAnticipo += Double.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(ANTICIPO_PROPERTY).getValue()).replaceAll(",", ""));

            facturasPagadas += String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(DOCUMENTO_PROPERTY).getValue()) + ",";
            partidasPagadas += String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(ID_PROPERTY).getValue()) + ",";
            codigoAnticipoList.add(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(ID_PROPERTY).getValue()));
        }
//                          limpiarPartida();

        descripcionTxt.setValue("PAGO DE " + String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(TIPO_PROPERTY).getValue())
                + " : [" + facturasPagadas + "] PROVEEDOR/INSTITUCION : [" + proveedorNombre + "]");

        montoTxt.setValue(montoPagar);
        montoTxt.setReadOnly(true);

        numeroTxt.focus();

        nombreChequeTxt.setReadOnly(false);
        nombreChequeTxt.setValue(proveedorNombre);

        Object partidaObject = partidaContainer.addItem();
        //LINEA DEL BANCO
        if (moneda.equals("DOLARES")) {
            partidaContainer.getContainerProperty(partidaObject, CUENTA_PROPERTY).setValue(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaExtranjera());
            partidaContainer.getContainerProperty(partidaObject, DESCRIPCION_PROPERTY).setValue(cuentasContables.get(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaExtranjera()));
        }
        else {
            partidaContainer.getContainerProperty(partidaObject, CUENTA_PROPERTY).setValue(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaLocal());
            partidaContainer.getContainerProperty(partidaObject, DESCRIPCION_PROPERTY).setValue(cuentasContables.get(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaLocal()));
        }

        partidaContainer.getContainerProperty(partidaObject, DEBE_PROPERTY).setValue("0");
        partidaContainer.getContainerProperty(partidaObject, HABER_PROPERTY).setValue(String.valueOf(montoPagar));
        partidaContainer.getContainerProperty(partidaObject, DEBE_Q_PROPERTY).setValue("0");
        partidaContainer.getContainerProperty(partidaObject, HABER_Q_PROPERTY).setValue(String.valueOf(Utileria.numberFormatEntero.format(montoPagar * tasaCambioTxt.getDoubleValueDoNotThrow())));
        partidaContainer.getContainerProperty(partidaObject, CODIGOCC_PROPERTY).setValue("");

        totalHaber = totalHaber.add(new BigDecimal(montoPagar));
        totalHaberQ = totalHaberQ.add(new BigDecimal((montoPagar * tasaCambioTxt.getDoubleValueDoNotThrow())));

        facturasGridIter = facturasGrid.getSelectedRows().iterator();

        double montoProveedores = 0.00;

        // POR CADA FACTURA QUE ESTAMOS SELECCINANDO, BUSCAR ANTICIPOS RELACIONADOS
        while (facturasGridIter.hasNext()) {

            Object gridItem2 = facturasGridIter.next();

            codigoPartida = String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(ID_PROPERTY).getValue()).replaceAll(",", "");
            codigoCC = String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(CODIGOCC_PROPERTY).getValue()).replaceAll(",", "");
            montoProveedores = Double.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_AUTORIZADO_PROPERTY).getValue()).replaceAll(",", ""))
                    + Double.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(ANTICIPO_PROPERTY).getValue()).replaceAll(",", ""));

            //para los anticipos relacionados o utlizados para pagar la factura
            queryString = " SELECT autorizacion_pago.*, contabilidad_partida.Debe,contabilidad_partida.DebeQuetzales ";
            queryString += " FROM autorizacion_pago";
            queryString += " INNER JOIN contabilidad_partida On contabilidad_partida.CodigoCC = autorizacion_pago.CodigoCCRelacionado AND contabilidad_partida.IdNomenclatura = " + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getAnticiposProveedor();
            queryString += " WHERE autorizacion_pago.CodigoCC = '" + codigoCC + "'";
            queryString += " AND autorizacion_pago.CodigoCCRelacionado <> ''";
            queryString += " AND contabilidad_partida.IdNomenclatura = " + ((SopdiUI) mainUI).cuentasContablesDefault.getAnticiposProveedor();
            queryString += " AND contabilidad_partida.DEBE > 0";

//            System.out.println("query anticipos = " + queryString);

            try {
                stQueryFacturas = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                rsRecordsFacturas = stQueryFacturas.executeQuery(queryString);

                if (rsRecordsFacturas.next()) {
                    do {
                        //// ANTICIPOS
                        partidaObject = partidaContainer.addItem();

                        if(moneda.equals("DOLARES")) {
                            porcentajeProporcional = (rsRecordsFacturas.getDouble("Monto") / rsRecordsFacturas.getDouble("Debe"));
                            montoQuetzales = rsRecordsFacturas.getDouble("DebeQuetzales") * porcentajeProporcional.doubleValue();
                        }
                        else {
                            montoQuetzales = rsRecordsFacturas.getDouble("Monto");
                        }

                        partidaContainer.getContainerProperty(partidaObject, CUENTA_PROPERTY).setValue(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getAnticiposProveedor());
                        partidaContainer.getContainerProperty(partidaObject, DESCRIPCION_PROPERTY).setValue(cuentasContables.get(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getAnticiposProveedor()));
                        partidaContainer.getContainerProperty(partidaObject, DEBE_PROPERTY).setValue("0");
                        partidaContainer.getContainerProperty(partidaObject, HABER_PROPERTY).setValue(rsRecordsFacturas.getString("Monto"));
                        partidaContainer.getContainerProperty(partidaObject, DEBE_Q_PROPERTY).setValue("0");
                        partidaContainer.getContainerProperty(partidaObject, HABER_Q_PROPERTY).setValue(String.valueOf(montoQuetzales));
                        partidaContainer.getContainerProperty(partidaObject, CODIGOCC_PROPERTY).setValue(rsRecordsFacturas.getString("CodigoCCRelacionado")); // del anticipo

                        totalHaber = totalHaber.add(new BigDecimal(rsRecordsFacturas.getString("Monto")));
                        totalHaberQ = totalHaberQ.add(new BigDecimal(montoQuetzales));

                    } while (rsRecordsFacturas.next());
                } // end while anticipos relacionados

                idNomenclatura = String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(CUENTA_PROPERTY).getValue());

                /// DOCUMENTOS
                partidaObject = partidaContainer.addItem();

                if(moneda.equals("DOLARES")) {
                    montoMoneda = Double.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(HABER_PROPERTY).getValue()).replaceAll(",", ""));
                    montoQuetzales = Double.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(HABER_Q_PROPERTY).getValue()).replaceAll(",", ""));
//                    porcentajeProporcional = (montoProveedores / montoMoneda);
//                    montoQuetzales = montoQuetzales * porcentajeProporcional.doubleValue();
                    //JAGUIRRE 2025-08-07
//                    porcentajeProporcional = (montoQuetzales / montoMoneda);
//System.out.println("montoMoneda=" + montoMoneda + " montoQuetzales=" + montoQuetzales + " porcentajeProporcional=" + porcentajeProporcional);
//                    montoQuetzales = porcentajeProporcional * montoProveedores;
//System.out.println("montoQuetzales=" + montoQuetzales);
                }
                else {
                    montoQuetzales = montoProveedores;
                }

                partidaContainer.getContainerProperty(partidaObject, CUENTA_PROPERTY).setValue(idNomenclatura);
                partidaContainer.getContainerProperty(partidaObject, DESCRIPCION_PROPERTY).setValue(cuentasContables.get(idNomenclatura));
                partidaContainer.getContainerProperty(partidaObject, DEBE_PROPERTY).setValue(String.valueOf(Utileria.numberFormatEntero.format(montoProveedores)));
                partidaContainer.getContainerProperty(partidaObject, HABER_PROPERTY).setValue("0");
                partidaContainer.getContainerProperty(partidaObject, DEBE_Q_PROPERTY).setValue(String.valueOf(Utileria.numberFormatEntero.format(montoQuetzales)));
                partidaContainer.getContainerProperty(partidaObject, HABER_Q_PROPERTY).setValue("0");
                partidaContainer.getContainerProperty(partidaObject, CODIGOCC_PROPERTY).setValue(codigoCC); // del documento

                totalDebe = totalDebe.add(new BigDecimal(montoProveedores));
                totalDebeQ = totalDebeQ.add(new BigDecimal(montoQuetzales));

            } catch (Exception ex) {
                Notification.show("Error al calcular partida : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Error " + ex);
            }
        }// end while iterator documentos seleccionados

        //diferencial cambiario

        if(monedaCbx.getValue().equals("DOLARES") && (totalDebeQ.doubleValue() != totalHaberQ.doubleValue())) { // si hay diferencial cambiario

            partidaObject = partidaContainer.addItem();

            partidaContainer.getContainerProperty(partidaObject, CUENTA_PROPERTY).setValue(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getDiferencialCambiario());
            partidaContainer.getContainerProperty(partidaObject, DESCRIPCION_PROPERTY).setValue("DIFERENCIAL CAMBIARIO");

            partidaContainer.getContainerProperty(partidaObject, DEBE_PROPERTY).setValue("0");
            partidaContainer.getContainerProperty(partidaObject, HABER_PROPERTY).setValue("0");

            if((totalDebeQ.doubleValue() - totalHaberQ.doubleValue()) > 0 ) {
                partidaContainer.getContainerProperty(partidaObject, HABER_Q_PROPERTY).setValue(String.valueOf(Utileria.numberFormatEntero.format(totalDebeQ.doubleValue() - totalHaberQ.doubleValue())));
                totalHaberQ = totalHaberQ.add(new BigDecimal(Utileria.numberFormatEntero.format(totalDebeQ.doubleValue() - totalHaberQ.doubleValue())));
                partidaContainer.getContainerProperty(partidaObject, DEBE_Q_PROPERTY).setValue("0");
            }
            else {
                partidaContainer.getContainerProperty(partidaObject, DEBE_Q_PROPERTY).setValue(String.valueOf(Utileria.numberFormatEntero.format((totalDebeQ.doubleValue() - totalHaberQ.doubleValue()) * -1)));
                totalDebeQ = totalDebeQ.add(new BigDecimal(Utileria.numberFormatEntero.format((totalDebeQ.doubleValue() - totalHaberQ.doubleValue()) * -1)));
                partidaContainer.getContainerProperty(partidaObject, HABER_Q_PROPERTY).setValue("0");
            }


            partidaContainer.getContainerProperty(partidaObject, CODIGOCC_PROPERTY).setValue("");

        }

        partidaObject = partidaContainer.addItem();

        partidaContainer.getContainerProperty(partidaObject, CUENTA_PROPERTY).setValue("__________");
        partidaContainer.getContainerProperty(partidaObject, DESCRIPCION_PROPERTY).setValue("__________");
        partidaContainer.getContainerProperty(partidaObject, DEBE_PROPERTY).setValue("___________");
        partidaContainer.getContainerProperty(partidaObject, HABER_PROPERTY).setValue("___________");
        partidaContainer.getContainerProperty(partidaObject, DEBE_Q_PROPERTY).setValue("___________");
        partidaContainer.getContainerProperty(partidaObject, HABER_Q_PROPERTY).setValue("___________");
        partidaContainer.getContainerProperty(partidaObject, CODIGOCC_PROPERTY).setValue("___________");

        partidaObject = partidaContainer.addItem();

        partidaContainer.getContainerProperty(partidaObject, CUENTA_PROPERTY).setValue("");
        partidaContainer.getContainerProperty(partidaObject, DESCRIPCION_PROPERTY).setValue("--------> SUMAS IGUALES");
        partidaContainer.getContainerProperty(partidaObject, DEBE_PROPERTY).setValue(numberFormat.format(totalDebe.doubleValue()));
        partidaContainer.getContainerProperty(partidaObject, HABER_PROPERTY).setValue(numberFormat.format(totalHaber.doubleValue()));
        partidaContainer.getContainerProperty(partidaObject, DEBE_Q_PROPERTY).setValue(numberFormat.format(totalDebeQ.doubleValue()));
        partidaContainer.getContainerProperty(partidaObject, HABER_Q_PROPERTY).setValue(numberFormat.format(totalHaberQ.doubleValue()));
        partidaContainer.getContainerProperty(partidaObject, CODIGOCC_PROPERTY).setValue("___________");

        partidaObject = partidaContainer.addItem();

        partidaContainer.getContainerProperty(partidaObject, CUENTA_PROPERTY).setValue("__________");
        partidaContainer.getContainerProperty(partidaObject, DESCRIPCION_PROPERTY).setValue("__________");
        partidaContainer.getContainerProperty(partidaObject, DEBE_PROPERTY).setValue("___________");
        partidaContainer.getContainerProperty(partidaObject, HABER_PROPERTY).setValue("___________");
        partidaContainer.getContainerProperty(partidaObject, DEBE_Q_PROPERTY).setValue("___________");
        partidaContainer.getContainerProperty(partidaObject, HABER_Q_PROPERTY).setValue("___________");
        partidaContainer.getContainerProperty(partidaObject, CODIGOCC_PROPERTY).setValue("___________");
    }

    public void llenarGridFacturas() {

        totalDebe = new BigDecimal(0);
        totalHaber = new BigDecimal(0);
        totalDebe.setScale(2, RoundingMode.HALF_UP);
        totalHaber.setScale(2, RoundingMode.HALF_UP);
        totalDebeQ = new BigDecimal(0);
        totalHaberQ = new BigDecimal(0);

        footerFacturas.getCell(VALOR_PROPERTY).setText("0.00");
        footerFacturas.getCell(MONTO_AUTORIZADO_PROPERTY).setText("0.00");
        footerFacturas.getCell(ANTICIPO_PROPERTY).setText("0.00");

        facturasContainer.removeAllItems();

        totalMonto = 0.00;
        totalQueztales = 0.00;
        totalAnticipo = 0.00;

        queryString = " Select contabilidad_partida.CodigoPartida, contabilidad_partida.CodigoCC, ";
        queryString += " contabilidad_partida.Fecha, contabilidad_partida.NombreProveedor ,";
        queryString += " contabilidad_partida.IdNomenclatura, contabilidad_partida.TipoDocumento, ";
        queryString += " contabilidad_partida.SerieDocumento, contabilidad_partida.NumeroDocumento, ";
        queryString += " contabilidad_partida.MonedaDocumento, ";
        queryString += " contabilidad_partida.MontoAutorizadoPagar, contabilidad_partida.MontoAplicarAnticipo,";
        queryString += " contabilidad_partida.Haber, contabilidad_partida.HaberQuetzales, ";
//JAGUIRRE        queryString += " (((contabilidad_partida.MontoAutorizadoPagar + contabilidad_partida.MontoAplicarAnticipo) / contabilidad_partida.Haber) * contabilidad_partida.HaberQuetzales) ProporcionHaberQ";
        queryString += " ( (contabilidad_partida.HaberQuetzales / contabilidad_partida.Haber) * (contabilidad_partida.MontoAutorizadoPagar + contabilidad_partida.MontoAplicarAnticipo)) ProporcionHaberQ";
        queryString += " From contabilidad_partida ";
        queryString += " Inner Join autorizacion_pago On autorizacion_pago.CodigoCC = contabilidad_partida.CodigoCC ";
        queryString += " Where contabilidad_partida.IdEmpresa =" + String.valueOf(empresaCbx.getValue());
        queryString += " And contabilidad_partida.IdProveedor = " + IdProveedor;
        queryString += " And contabilidad_partida.IdNomenclatura = " + ((SopdiUI) mainUI).cuentasContablesDefault.getProveedores();
        queryString += " And UPPER(contabilidad_partida.TipoDocumento) IN ('FACTURA', 'RECIBO', ";
        queryString += " 'RECIBO CONTABLE', 'RECIBO CORRIENTE', 'FORMULARIO RECTIFICACION')";
//        queryString += " And contabilidad_partida.MontoAutorizadoPagar > 0 ";  // este es el monto para el cheque
//        queryString += " OR contabilidad_partida.MontoAplicarAnticipo > 0 ";
        queryString += " Group by CodigoPartida";

System.out.println("query mostrar documentos a pagar FACTURAS : " + queryString);

        try {
            stQueryFacturas = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecordsFacturas = stQuery.executeQuery(queryString);

            if (rsRecordsFacturas.next()) { //  encontrado
                do {

                    Object itemId = facturasContainer.addItem();

                    try {
                        facturasContainer.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecordsFacturas.getString("CodigoPartida"));
                        facturasContainer.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecordsFacturas.getString("CodigoPartida"));
                    } catch (Exception ex11) {
                        ex11.printStackTrace();
                        return;
                    }

                    facturasContainer.getContainerProperty(itemId, TIPO_PROPERTY).setValue(rsRecordsFacturas.getString("TipoDocumento"));
                    facturasContainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecordsFacturas.getDate("Fecha")));
                    facturasContainer.getContainerProperty(itemId, PROVEEDOR_PROPERTY).setValue(rsRecordsFacturas.getString("NombreProveedor"));
                    facturasContainer.getContainerProperty(itemId, CODIGO_PROPERTY).setValue(IdProveedor);
                    facturasContainer.getContainerProperty(itemId, FACTURA_PROPERTY).setValue(rsRecordsFacturas.getString("SerieDocumento") + " " + rsRecordsFacturas.getString("NumeroDocumento"));
                    facturasContainer.getContainerProperty(itemId, MONEDA_PROPERTY).setValue(rsRecordsFacturas.getString("MonedaDocumento"));
                    facturasContainer.getContainerProperty(itemId, VALOR_PROPERTY).setValue(numberFormat.format(rsRecordsFacturas.getDouble("MontoAutorizadoPagar")));
                    facturasContainer.getContainerProperty(itemId, MONTO_AUTORIZADO_PROPERTY).setValue(numberFormat.format(rsRecordsFacturas.getDouble("MontoAutorizadoPagar")));
                    facturasContainer.getContainerProperty(itemId, ANTICIPO_PROPERTY).setValue(numberFormat.format(rsRecordsFacturas.getDouble("MontoAplicarAnticipo")));
                    facturasContainer.getContainerProperty(itemId, CUENTA_PROPERTY).setValue(rsRecordsFacturas.getString("IdNomenclatura"));
                    facturasContainer.getContainerProperty(itemId, HABER_PROPERTY).setValue(rsRecordsFacturas.getString("Haber"));
                    facturasContainer.getContainerProperty(itemId, HABER_Q_PROPERTY).setValue(rsRecordsFacturas.getString("ProporcionHaberQ"));
                    facturasContainer.getContainerProperty(itemId, CODIGOCC_PROPERTY).setValue(rsRecordsFacturas.getString("CodigoCC"));

                    totalMonto += rsRecordsFacturas.getDouble("MontoAutorizadoPagar");
                    totalQueztales += rsRecordsFacturas.getDouble("ProporcionHaberQ");
                    totalAnticipo += rsRecordsFacturas.getDouble("MontoAplicarAnticipo");

                } while (rsRecordsFacturas.next());

            }

            //INSTITUCIONES
            queryString = " Select contabilidad_partida.CodigoPartida, contabilidad_partida.CodigoCC, ";
            queryString += " contabilidad_partida.Fecha, contabilidad_partida.NombreProveedor,";
            queryString += " contabilidad_partida.IdNomenclatura, contabilidad_partida.TipoDocumento, ";
            queryString += " contabilidad_partida.SerieDocumento, contabilidad_partida.NumeroDocumento, ";
            queryString += " contabilidad_partida.MonedaDocumento, ";
            queryString += " contabilidad_partida.MontoAutorizadoPagar, contabilidad_partida.MontoAplicarAnticipo, ";
            queryString += " contabilidad_partida.Haber, contabilidad_partida.HaberQuetzales ";
            queryString += " From contabilidad_partida ";
            queryString += " Inner Join autorizacion_pago On autorizacion_pago.CodigoCC = contabilidad_partida.CodigoCC ";
            queryString += " Where contabilidad_partida.IdEmpresa = " + String.valueOf(empresaCbx.getValue());
            queryString += " And contabilidad_partida.IdProveedor = " + IdProveedor;
            queryString += " And contabilidad_partida.IdNomenclatura = " + ((SopdiUI) mainUI).cuentasContablesDefault.getInstituciones();
            queryString += " And UPPER(contabilidad_partida.TipoDocumento) IN ('FORMULARIO IVA', ";
            queryString += " 'FORMULARIO ISR', 'FORMULARIO ISR RETENIDO','FORMULARIO ISR OPCIONAL MENSUAL', 'FORMULARIO ISO', 'RECIBO CONTABLE', ";
            queryString += " 'FORMULARIO', 'FORMULARIO RECTIFICACION')";
//        queryString += " And contabilidad_partida.MontoAutorizadoPagar > 0 ";  // este es el monto para el cheque
//        queryString += " OR contabilidad_partida.MontoAplicarAnticipo > 0 ";
            queryString += " Group by CodigoPartida";

System.out.println("query mostrar documentos a pagar INSTITUCIONES : " + queryString);

            stQueryFacturas = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecordsFacturas = stQuery.executeQuery(queryString);

            if (rsRecordsFacturas.next()) { //  encontrado
                do {

                    Object itemId = facturasContainer.addItem();

                    try {
                        facturasContainer.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecordsFacturas.getString("CodigoPartida"));
                        facturasContainer.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecordsFacturas.getString("CodigoPartida"));
                    } catch (Exception ex11) {
                        ex11.printStackTrace();
                        return;
                    }

                    facturasContainer.getContainerProperty(itemId, TIPO_PROPERTY).setValue(rsRecordsFacturas.getString("TipoDocumento"));
                    facturasContainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecordsFacturas.getDate("Fecha")));
                    facturasContainer.getContainerProperty(itemId, PROVEEDOR_PROPERTY).setValue(rsRecordsFacturas.getString("NombreProveedor"));
                    facturasContainer.getContainerProperty(itemId, CODIGO_PROPERTY).setValue(IdProveedor);
                    facturasContainer.getContainerProperty(itemId, FACTURA_PROPERTY).setValue(rsRecordsFacturas.getString("SerieDocumento") + " " + rsRecordsFacturas.getString("NumeroDocumento"));
                    facturasContainer.getContainerProperty(itemId, MONEDA_PROPERTY).setValue(rsRecordsFacturas.getString("MonedaDocumento"));
                    facturasContainer.getContainerProperty(itemId, VALOR_PROPERTY).setValue(numberFormat.format(rsRecordsFacturas.getDouble("MontoAutorizadoPagar")));
                    facturasContainer.getContainerProperty(itemId, MONTO_AUTORIZADO_PROPERTY).setValue(numberFormat.format(rsRecordsFacturas.getDouble("MontoAutorizadoPagar")));
                    facturasContainer.getContainerProperty(itemId, ANTICIPO_PROPERTY).setValue(numberFormat.format(rsRecordsFacturas.getDouble("MontoAplicarAnticipo")));
                    facturasContainer.getContainerProperty(itemId, CUENTA_PROPERTY).setValue(rsRecordsFacturas.getString("IDNomenclatura"));
                    facturasContainer.getContainerProperty(itemId, HABER_PROPERTY).setValue(rsRecordsFacturas.getString("Haber"));
                    facturasContainer.getContainerProperty(itemId, HABER_Q_PROPERTY).setValue(rsRecordsFacturas.getString("HaberQuetzales"));
                    facturasContainer.getContainerProperty(itemId, CODIGOCC_PROPERTY).setValue(rsRecordsFacturas.getString("CodigoCC"));

                    totalMonto += rsRecordsFacturas.getDouble("MontoAutorizadoPagar");
                    totalQueztales += rsRecordsFacturas.getDouble("MontoAutorizadoPagar");
                    totalAnticipo += rsRecordsFacturas.getDouble("MontoAplicarAnticipo");

                } while (rsRecordsFacturas.next());

            }

        } catch (Exception ex) {
            System.out.println("Error al listar tabla de DOCUMENTOS : " + ex);
            ex.printStackTrace();

            Notification notif = new Notification("HA OCURRIDO UN ERROR DE BASE DE DATOS : " + ex.getMessage(),
                    Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.WARNING);
            notif.show(Page.getCurrent());
        }

        footerFacturas.getCell(VALOR_PROPERTY).setText(numberFormat.format(totalMonto));
        footerFacturas.getCell(MONTO_AUTORIZADO_PROPERTY).setText(numberFormat.format(totalQueztales));
        footerFacturas.getCell(ANTICIPO_PROPERTY).setText(numberFormat.format(totalAnticipo));
    }

    private void crearLayoutCheque() {

        chequeLayout.setSpacing(true);
        chequeLayout.setMargin(false);
        chequeLayout.setSizeUndefined();

        chequeLayout2.setSpacing(true);
        chequeLayout2.setMargin(false);
        chequeLayout2.setSizeUndefined();

        proveedorCbx = new ComboBox("Proveedor : ");
        proveedorCbx.setWidth("15em");
        proveedorCbx.setFilteringMode(FilteringMode.CONTAINS);
        llenarComboProveedor();
        proveedorCbx.addValueChangeListener(event -> {
            if (nombreChequeTxt == null) {
                return;
            }
            if (proveedorCbx.getValue() == null) {
                nombreChequeTxt.setReadOnly(false);
                nombreChequeTxt.setValue("");
            } else {
                nombreChequeTxt.setReadOnly(false);
                nombreChequeTxt.setValue(proveedorCbx.getItemCaption(proveedorCbx.getValue()));
            }
        });

        proveedorCbx.setVisible(false);

        numeroTxt = new TextField("# Documento : ");
        numeroTxt.setWidth("8em");

        medioCbx = new ComboBox("Medio : ");
        medioCbx.setWidth("12em");
        medioCbx.addItem("CHEQUE");
        medioCbx.addItem("NOTA DE DEBITO");
        medioCbx.select("CHEQUE");

        monedaCbx = new ComboBox("Moneda : ");
        monedaCbx.setWidth("10em");
        monedaCbx.addItem("QUETZALES");
        monedaCbx.addItem("DOLARES");
        monedaCbx.select("QUETZALES");

        tasaCambioTxt = new NumberField("T.Cambio : ");
        tasaCambioTxt.setDecimalAllowed(true);
        tasaCambioTxt.setDecimalPrecision(5);
        tasaCambioTxt.setMinimumFractionDigits(5);
        tasaCambioTxt.setDecimalSeparator('.');
        tasaCambioTxt.setDecimalSeparatorAlwaysShown(true);
        tasaCambioTxt.setGroupingUsed(true);
        tasaCambioTxt.setGroupingSeparator(',');
        tasaCambioTxt.setGroupingSize(3);
        tasaCambioTxt.setImmediate(true);
        tasaCambioTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        tasaCambioTxt.setWidth("5em");
        tasaCambioTxt.setValue(1.00);
        tasaCambioTxt.addValueChangeListener( event -> {
            calcularPartida();
        });
//        tasaCambioTxt.addFocusListener(new FieldEvents.FocusListener() {
//
//            @Override
//            public void focus(FieldEvents.FocusEvent event) {
//                calcularPartida();
//            }
//        });

        montoTxt = new NumberField("Monto : ");
        montoTxt.setValidationVisible(false);
        montoTxt.setDecimalAllowed(true);
        montoTxt.setDecimalPrecision(2);
        montoTxt.setMinimumFractionDigits(2);
        montoTxt.setDecimalSeparator('.');
        montoTxt.setDecimalSeparatorAlwaysShown(true);
        montoTxt.setValue(0d);
        montoTxt.setGroupingUsed(true);
        montoTxt.setGroupingSeparator(',');
        montoTxt.setGroupingSize(3);
        montoTxt.setImmediate(true);
        montoTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        montoTxt.setWidth("7em");

        fechaDt = new DateField("Fecha : ");
        fechaDt.setDateFormat("dd/MM/yyyy");
        fechaDt.setWidth("9em");
        fechaDt.setValue(fechaPago);
        fechaDt.setReadOnly(true);

        nombreChequeTxt = new TextField("Nombre cheque/nota : ");
        nombreChequeTxt.setWidth("25em");
        nombreChequeTxt.setValue(proveedorCbx.getItemCaption(proveedorCbx.getValue()));
        nombreChequeTxt.setReadOnly(((SopdiUI) mainUI).sessionInformation.getStrUserProfileName().equals("CONTADOR"));
        nombreChequeTxt.setReadOnly(((SopdiUI) mainUI).sessionInformation.getStrUserProfileName().equals("AUXILIAR"));

        descripcionTxt = new TextField("Descripción : ");
        descripcionTxt.setWidth("45em");
        descripcionTxt.setVisible(false);

        chequeLayout.addComponent(medioCbx);
        chequeLayout.setComponentAlignment(medioCbx, Alignment.MIDDLE_CENTER);
        chequeLayout.addComponent(numeroTxt);
        chequeLayout.setComponentAlignment(numeroTxt, Alignment.MIDDLE_CENTER);
        chequeLayout.addComponent(fechaDt);
        chequeLayout.setComponentAlignment(fechaDt, Alignment.MIDDLE_CENTER);
        chequeLayout.addComponent(proveedorCbx);
        chequeLayout.setComponentAlignment(proveedorCbx, Alignment.MIDDLE_CENTER);
        chequeLayout.addComponent(montoTxt);
        chequeLayout.setComponentAlignment(montoTxt, Alignment.MIDDLE_CENTER);
        chequeLayout.addComponent(monedaCbx);
        chequeLayout.setComponentAlignment(monedaCbx, Alignment.MIDDLE_CENTER);
        chequeLayout.addComponent(tasaCambioTxt);
        chequeLayout.setComponentAlignment(tasaCambioTxt, Alignment.MIDDLE_CENTER);

        mainLayout.addComponent(chequeLayout);
        mainLayout.setComponentAlignment(chequeLayout, Alignment.MIDDLE_CENTER);

        chequeLayout2.addComponent(nombreChequeTxt);
        chequeLayout2.setComponentAlignment(nombreChequeTxt, Alignment.MIDDLE_CENTER);
        chequeLayout2.addComponent(descripcionTxt);
        chequeLayout2.setComponentAlignment(descripcionTxt, Alignment.MIDDLE_CENTER);

        mainLayout.addComponent(chequeLayout2);
        mainLayout.setComponentAlignment(chequeLayout2, Alignment.MIDDLE_CENTER);
    }

    public void crearPartidaLayout() {

        partidaLayout.addStyleName("rcorners3");
        partidaLayout.setWidth("90%");
        partidaLayout.setResponsive(true);
        partidaLayout.setSpacing(false);
        partidaLayout.setMargin(false);

        llenarComboCuentaContable();

        partidaGrid = new Grid(partidaContainer);
        partidaGrid.setImmediate(true);
        partidaGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        partidaGrid.setHeightMode(HeightMode.ROW);
        partidaGrid.setHeightByRows(10);
        partidaGrid.setWidth("100%");
        partidaGrid.setResponsive(true);
        partidaGrid.setEditorBuffered(false);
        partidaGrid.setColumnReorderingAllowed(false);

        partidaGrid.getColumn(CUENTA_PROPERTY).setExpandRatio(1);
        partidaGrid.getColumn(DESCRIPCION_PROPERTY).setExpandRatio(5);
        partidaGrid.getColumn(DEBE_PROPERTY).setExpandRatio(2);
        partidaGrid.getColumn(HABER_PROPERTY).setExpandRatio(2);
        partidaGrid.getColumn(DEBE_Q_PROPERTY).setExpandRatio(2);
        partidaGrid.getColumn(HABER_Q_PROPERTY).setExpandRatio(2);
        partidaGrid.getColumn(CODIGOCC_PROPERTY).setExpandRatio(3);

        partidaGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {

                    if (DEBE_PROPERTY.equals(cellReference.getPropertyId())) {
                        return "rightalign";
                    } else if (HABER_PROPERTY.equals(cellReference.getPropertyId())) {
                        return "rightalign";
                    } else if (DEBE_Q_PROPERTY.equals(cellReference.getPropertyId())) {
                        return "rightalign";
                    } else if (HABER_Q_PROPERTY.equals(cellReference.getPropertyId())) {
                        return "rightalign";
                    } else {
                        return null;
                    }
                }
        );

        grabarPartidaBtn = new Button("Grabar");
        grabarPartidaBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        grabarPartidaBtn.setIcon(FontAwesome.SAVE);
        grabarPartidaBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                insertarPartidaCompuesta();
            }
        });

        desAutorizarBtn = new Button("Des autorizar pago");
        desAutorizarBtn.addStyleName(ValoTheme.BUTTON_DANGER);
        desAutorizarBtn.setIcon(FontAwesome.TRASH);
        desAutorizarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {

                if (facturasGrid.getSelectedRows() != null) {
                    desAutorizarFactura();
                }
            }
        });

        Button cancelarBtn = new Button("Cancelar");
        cancelarBtn.addStyleName(ValoTheme.BUTTON_DANGER);
        cancelarBtn.setIcon(FontAwesome.BAN);
        cancelarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                proveedorCbx.setReadOnly(false);
                limpiarPartida();
                proveedorCbx.setReadOnly(true);
                proveedorCbx.focus();
                facturasGrid.deselectAll();
            }
        });

        HorizontalLayout layoutHorizontal6 = new HorizontalLayout();
        layoutHorizontal6.setSpacing(true);
        layoutHorizontal6.addComponents(cancelarBtn, desAutorizarBtn, grabarPartidaBtn);
        layoutHorizontal6.setComponentAlignment(cancelarBtn, Alignment.BOTTOM_LEFT);
        layoutHorizontal6.setComponentAlignment(desAutorizarBtn, Alignment.BOTTOM_CENTER);
        layoutHorizontal6.setComponentAlignment(grabarPartidaBtn, Alignment.BOTTOM_RIGHT);

        partidaLayout.addComponent(partidaGrid);
        partidaLayout.setComponentAlignment(partidaGrid, Alignment.TOP_CENTER);
        partidaLayout.addComponent(layoutHorizontal6);
        partidaLayout.setComponentAlignment(layoutHorizontal6, Alignment.MIDDLE_CENTER);

        mainLayout.addComponent(partidaLayout);
        mainLayout.setComponentAlignment(partidaLayout, Alignment.MIDDLE_CENTER);

    }

    public void desAutorizarFactura() {

        Iterator iter = facturasGrid.getSelectedRows().iterator();

        ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Desea eliminar la siguiente autorizacion de estas facturas ?",
                "SI", "NO", new ConfirmDialog.Listener() {

                    public void onClose(ConfirmDialog dialog) {
                        if (dialog.isConfirmed()) {
                            try {

                                ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().setAutoCommit(false);

                                stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

                                while (iter.hasNext()) {

                                    Object gridItem = iter.next();

                                    String codigoPartida = String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(ID_PROPERTY).getValue());
                                    String codigoProveedor = String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(CODIGO_PROPERTY).getValue());

                                    queryString = " UPDATE contabilidad_partida SET";
                                    queryString += " MontoAutorizadoPagar = 0 ";
                                    queryString += ",MontoAplicarAnticipo = 0 ";
                                    queryString += " WHERE CodigoPartida = '" + codigoPartida + "'";

                                    stQuery.executeUpdate(queryString);

                                    queryString = " DELETE FROM autorizacion_pago";
                                    queryString += " WHERE CodigoCC = '" + codigoPartida + "'";

                                    stQuery.executeUpdate(queryString);

//                                    for (Iterator iTerator = ((PagarView) (mainUI.getNavigator().getCurrentView())).documentosContainer.getItemIds().iterator(); iTerator.hasNext();) {
//                                        // Get the current item identifier, which is an integer.
//                                        Object itemId = iTerator.next();
//
//                                        if(codigoPartida.equals(String.valueOf(((PagarView) (mainUI.getNavigator().getCurrentView())).documentosContainer.getContainerProperty(itemId, PagarView.ID_AUTO_PROPERTY).getValue()))) {
//                                            ((PagarView) (mainUI.getNavigator().getCurrentView())).documentosContainer.getContainerProperty(itemId, PagarView.ESTATUS_PROPERTY).setValue("NO AUTORIZADO");
//                                        }
//                                    }

                                } // end while

                                ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().commit();
                                ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().setAutoCommit(true);

                                Notification.show("DOCUMENTOS DES-AUTORIZADOS CON EXITO ", Notification.Type.HUMANIZED_MESSAGE);

                                close();

                            } catch (Exception ex) {
                                System.out.println("Error al momento de des autorizar facturas : " + ex);

                                Notification notif = new Notification("HA OCURRIDO UN ERROR DE BASE DE DATOS : " + ex.getMessage() + "  TRANSACCION ABORTADA!!!",
                                        Notification.Type.ERROR_MESSAGE);
                                notif.setDelayMsec(1500);
                                notif.setPosition(Position.MIDDLE_CENTER);
                                notif.setIcon(FontAwesome.WARNING);
                                notif.show(Page.getCurrent());

                                try {
                                    ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().rollback();
                                    ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().setAutoCommit(true);
                                } catch (SQLException ex2) {
                                    Logger.getLogger(PagoFacturaProveedorForm.class
                                            .getName()).log(Level.SEVERE, null, ex2);
                                }

                                try {
                                    String emailsTo[] = {"alerta@simpletecno.com"};
                                    MyEmailMessanger eMail = new MyEmailMessanger();

                                    eMail.postMail(emailsTo, "Error en SOPDI", "Error en base de datos :  " + this.getClass().getName() + " -->" + ex.getMessage());
                                } catch (MessagingException ex2) {
                                    Logger.getLogger(PagoFacturaProveedorForm.class.getName()).log(Level.SEVERE, null, ex2);
                                }

                            }
                        }
                    }
                }
        );

    }

    public void llenarComboCuentaContable() {
        String queryString = " SELECT * from contabilidad_nomenclatura ";
        queryString += " where Estatus='HABILITADA'";
        queryString += " Order By N5";

        cuentasContables.clear();

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) { //  encontrado
                cuentasContables.put(rsRecords.getString("IdNomenclatura"), rsRecords.getString("N5"));
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar cuentas contables: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void insertarPartidaCompuesta() {

        if (nombreChequeTxt.getValue().trim().isEmpty()) {
            Notification.show("Por favor, escriba el nombre del cheque o transferencia. ", Notification.Type.ERROR_MESSAGE);
            nombreChequeTxt.focus();
            return;
        }
        if (numeroTxt.getValue().trim().isEmpty()) {
            Notification.show("Por favor, escriba el cheque o transferencia. ", Notification.Type.ERROR_MESSAGE);
            numeroTxt.focus();
            return;
        }
        if (montoTxt.getDoubleValueDoNotThrow() == 0.00) {
            Notification.show("Por favor primero elija un documento o escriba el monto a pagar.", Notification.Type.ERROR_MESSAGE);
            return;
        }
        if (((SopdiUI) UI.getCurrent()).esMesCerrado(String.valueOf(empresaCbx.getValue()), Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()))) {
            Notification.show("La fecha del documento no puede ser de un mes ya cerrado contablemente, revise!", Notification.Type.WARNING_MESSAGE);
            fechaDt.focus();
            return;
        }
        if (!((SopdiUI) UI.getCurrent()).esPrimerMesAbierto(String.valueOf(empresaCbx.getValue()), Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()))) {
            Notification.show("El mes abierto a operaciones es : " + ((SopdiUI) UI.getCurrent()).primerMesAbierto(String.valueOf(empresaCbx.getValue())), Notification.Type.WARNING_MESSAGE);
            fechaDt.focus();
            return;
        }

        if (!numberFormat.format(totalDebe.doubleValue()).equals(numberFormat.format(totalHaber.doubleValue()))) {
            System.out.println("Debe =" + totalDebe.doubleValue() + "  haber=" + totalHaber.doubleValue());
            Notification.show("La partida es descuadrada, por favor revisar"
                    + " Debe = " + totalDebe.doubleValue() + "  Haber = " + totalHaber.doubleValue(), Notification.Type.WARNING_MESSAGE);
            return;
        }

        queryString = " Select CodigoPartida from contabilidad_partida ";
        queryString += " Where NumeroDocumento = '" + numeroTxt.getValue() + "'";
        queryString += " And IdEmpresa = " + String.valueOf(empresaCbx.getValue());
        queryString += " And TipoDocumento = '" + String.valueOf(medioCbx.getValue()) + "'";
        queryString += " And MonedaDocumento = '" + monedaCbx.getValue() + "'";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado
                Notification.show("Documento ya registrado en pago, codigo de partida = " + rsRecords.getString("CodigoPartida"), Notification.Type.ERROR_MESSAGE);
                numeroTxt.focus();
                return;
            }

        } catch (Exception ex1) {
            System.out.println("Error al validar el documento ingresado. " + ex1.getMessage());
            ex1.printStackTrace();
        }

        String fecha = Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()); //yyy/mm/yyyy
        String ultimoEncontado;
        String dia = fecha.substring(8, 10);
        String mes = fecha.substring(5, 7);
        String año = fecha.substring(0, 4);

        String codigoPartida = String.valueOf(empresaCbx.getValue()) + año + mes + dia + "3";

        queryString = " select codigoPartida from contabilidad_partida ";
        queryString += " where codigoPartida like '" + codigoPartida + "%'";
        queryString += " order by codigoPartida desc ";

        try {

            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado

                ultimoEncontado = rsRecords.getString("codigoPartida").substring(12, 15);

//System.out.println("ultimo encontrado " + ultimoEncontado);
                codigoPartida += String.format("%03d", (Integer.valueOf(ultimoEncontado) + 1));

            } else {
                codigoPartida += "001";
            }

        } catch (Exception ex1) {
            System.out.println("Error al buscar el ultimo codigoPartida" + ex1.getMessage());
            ex1.printStackTrace();
        }
        codigoPartidaNuevo = codigoPartida;

        String descripcion =  descripcionTxt.getValue().trim();

        queryString = " Insert Into contabilidad_partida (IdEmpresa, Estatus, CodigoPartida, CodigoCC,";
        queryString += " TipoDocumento, NoDOCA, TipoDOCA, Fecha, IdProveedor, NITProveedor, ";
        queryString += " NombreProveedor, NombreCheque, MontoDocumento, SerieDocumento, NumeroDocumento, ";
        queryString += " IdNomenclatura, MonedaDocumento, Debe, Haber,";
        queryString += " DebeQuetzales, HaberQuetzales, TipoCambio,";
        queryString += " Descripcion, CreadoUsuario, CreadoFechaYHora)";
        queryString += " Values ";

        for (Object itemId: partidaContainer.getItemIds()) {
            Item item = partidaContainer.getItem(itemId);
            if(!String.valueOf(item.getItemProperty(CODIGOCC_PROPERTY).getValue()).equals("___________")) {
                queryString += " (";
                queryString += String.valueOf(empresaCbx.getValue());
                queryString += ",'INGRESADO'";
                queryString += ",'" + codigoPartida + "'";
                queryString += ",'" + String.valueOf(item.getItemProperty(CODIGOCC_PROPERTY).getValue()) + "'";
                queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
                queryString += ",'" + facturasPagadas + "'";//NODOCA
                queryString += ",'" + tipoDocumentoPagado + "'";//TIPODOCA
                queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
                queryString += "," + proveedorId;
                queryString += ",''"; //nit proveedor
                queryString += ",'" + proveedorNombre + "'";
                queryString += ",'" + nombreChequeTxt.getValue() + "'";
                queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow());
                queryString += ",''"; //serie documento
                queryString += ",'" + numeroTxt.getValue() + "'";
                queryString += "," + String.valueOf(item.getItemProperty(CUENTA_PROPERTY).getValue());
                queryString += ",'" + monedaCbx.getValue() + "'";
                queryString += "," + String.valueOf(item.getItemProperty(DEBE_PROPERTY).getValue());  //Debe
                queryString += "," + String.valueOf(item.getItemProperty(HABER_PROPERTY).getValue()); //Haber
                queryString += "," + String.valueOf(item.getItemProperty(DEBE_Q_PROPERTY).getValue()); //DEBE Q
                queryString += "," + String.valueOf(item.getItemProperty(HABER_Q_PROPERTY).getValue()); //HABER Q
                queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
                queryString += ",'" + descripcion + "'";
                queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                queryString += ",current_timestamp";
                queryString += "),";
            }
        }

//        //diferencial cambiario
//        if(monedaCbx.getValue().equals("DOLARES") && (totalDebeQ.doubleValue() != totalHaberQ.doubleValue())) { // si hay diferencial cambiario
//            queryString += " (";
//            queryString += String.valueOf(empresaCbx.getValue());
//            queryString += ",'INGRESADO'";
//            queryString += ",'" + codigoPartida + "'";
//            queryString += ",'" + codigoPartida + "'";
//            queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
//            queryString += ",'" + facturasPagadas + "'";//NODOCA
//            queryString += ",'" + tipoDocumentoPagado + "'";//TIPODOCA
//            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
//            queryString += "," + proveedorId;
//            queryString += ",''"; //nit proveedor
//            queryString += ",'" + proveedorNombre + "'";
//            queryString += ",'" + nombreChequeTxt.getValue() + "'";
//            queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow());
//            queryString += ",''"; //serie documento
//            queryString += ",'" + numeroTxt.getValue() + "'";
//            queryString += ","  + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getDiferencialCambiario();
//            queryString += ",'" + monedaCbx.getValue() + "'";
//            queryString += ",0.00"; //Debe moneda
//            queryString += ",0.00"; //Haber moneda
//            if((totalDebeQ.doubleValue() - totalHaberQ.doubleValue()) > 0 ) {
//                queryString += "," + String.valueOf(totalDebeQ.doubleValue() - totalHaberQ.doubleValue()); //DEBE Q
//            }
//            else {
//                queryString += "," + String.valueOf((totalDebeQ.doubleValue() - totalHaberQ.doubleValue()) * -1); //DEBE Q
//            }
//            queryString += ",0.0";
//            queryString += ",0.0";
//            queryString += ",'" + descripcion + "'";
//            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
//            queryString += ",current_timestamp";
//            queryString += "),";
//        }

        queryString = queryString.substring(0, queryString.length()-1);

System.out.println("PARTIDA PAGO DOCUMENTO = " + queryString);

        try {

            ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().setAutoCommit(false);

            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            Iterator iter;

            iter = facturasGrid.getSelectedRows().iterator();

            Double montoPagar = 0.00;
            Double montoAnticipo = 0.00;
            Double saldo = 0.00;
            String tipo = "";
            String fechaSelect = "";

            String codigoPartidaDoca;

            while (iter.hasNext()) {  // POR CADA FACTURA QUE ESTAMOS PAGANDO CON ESTE CHEQUE

                Object gridItem = iter.next();

                codigoCC = String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(CODIGOCC_PROPERTY).getValue());
                codigoPartidaDoca = String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(ID_PROPERTY).getValue());
                montoPagar = Double.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(VALOR_PROPERTY).getValue()).replaceAll(",", ""));
                montoAnticipo = Double.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(ANTICIPO_PROPERTY).getValue()).replaceAll(",", ""));

                tipo = String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(TIPO_PROPERTY).getValue());
                fechaSelect = String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(FECHA_PROPERTY).getValue());

                queryString = " Update contabilidad_partida Set ";
                queryString += " MontoAutorizadoPagar = 0.00";
                queryString += ", MontoAplicarAnticipo = 0.00";
                queryString += ", Estatus = 'PAGADO'";
                queryString += ", Referencia = '" + codigoPartida + "'";//codigo de la partida del CHEQUE
                queryString += ", TipoDoca = '" + medioCbx.getValue() + "'";
                queryString += ", NoDoca = '" + numeroTxt.getValue() + "'";
                queryString += " WHERE CodigoPartida = '" + codigoPartidaDoca + "'";  //CODIGO DE LA PARTIDA DEL DOCUMENTO

                stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                stQuery.executeUpdate(queryString);

                queryString = " Update contabilidad_partida Set ";
                queryString += " Estatus = 'PAGADO'";
                queryString += ", Referencia = '" + codigoPartida + "'";//codigo de la partida del CHEQUE
                queryString += ", TipoDoca = '" + medioCbx.getValue() + "'";
                queryString += ", NoDoca = '" + numeroTxt.getValue() + "'";
                queryString += " WHERE CodigoCC = '" + codigoCC + "'";  //CODIGOCC DE LA PARTIDA DEL DOCUMENTO
                queryString += " AND   TipoDocumento = 'NOTA DE CREDITO COMPRA'";

                stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                stQuery.executeUpdate(queryString);

                queryString = " DELETE FROM autorizacion_pago";
                queryString += " WHERE CodigoCC = '" + codigoPartidaDoca + "'";

                stQuery.executeUpdate(queryString);

                ((PagarView) (mainUI.getNavigator().getCurrentView())).llenarTablaAutorizaciones();

//                for (Iterator iTerator = ((PagarView) (mainUI.getNavigator().getCurrentView())).documentosContainer.getItemIds().iterator(); iTerator.hasNext();) {
//                    Object itemId = iTerator.next();
//
//                    if(codigoPartidaDoca.equals(String.valueOf(((PagarView) (mainUI.getNavigator().getCurrentView())).documentosContainer.getContainerProperty(itemId, PagarView.CODIGOCC_PROPERTY).getValue()))) {
//                        ((PagarView) (mainUI.getNavigator().getCurrentView())).documentosContainer.getContainerProperty(itemId, PagarView.ESTATUS_PROPERTY).setValue("PAGADO");
//                    }
//                }

                facturasGrid.getContainerDataSource().removeItem(gridItem);

            }//end while

            ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().commit();
            ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().setAutoCommit(true);

            Notification notif = new Notification("PAGAO REALIZADO EXITOSAMENTE.",
                    Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.CHECK);
            notif.show(Page.getCurrent());

            PagoChequesPDF Pagocheques
                    = new PagoChequesPDF(
                    String.valueOf(empresaCbx.getValue()),
                    empresaCbx.getItemCaption(empresaCbx.getValue()),
                    codigoPartidaNuevo,
                    "0",
                    nombreChequeTxt.getValue(),
                    numeroTxt.getValue(),
                    descripcion,
                    numberFormat3.format(montoTxt.getDoubleValueDoNotThrow())
            );

            mainUI.addWindow(Pagocheques);

            Pagocheques.center();

            facturasGrid.getSelectedRows().clear();
            facturasGrid.getSelectionModel().reset();

            proveedorCbx.setReadOnly(false);

            limpiarPartida();

            proveedorCbx.setReadOnly(true);

            MostrarPartidaContable mostrarPartidaContable = new MostrarPartidaContable(codigoPartida, "", descripcion, numeroTxt.getValue());
            mainUI.addWindow(mostrarPartidaContable);
            mostrarPartidaContable.center();

        } catch (Exception ex1) {

            System.out.println("Error al insertar transacción  : " + ex1.getMessage());
            ex1.printStackTrace();

            Notification notif = new Notification("HA OCURRIDO UN ERROR DE BASE DE DATOS : " + ex1.getMessage() + "  TRANSACCION ABORTADA!!!",
                    Notification.Type.ERROR_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.WARNING);
            notif.show(Page.getCurrent());

            try {
                ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().rollback();
                ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().setAutoCommit(true);
            } catch (SQLException ex) {
                Logger.getLogger(PagoFacturaProveedorForm.class
                        .getName()).log(Level.SEVERE, null, ex);
            }

            try {
                String emailsTo[] = {"alerta@simpletecno.com"};
                MyEmailMessanger eMail = new MyEmailMessanger();

                eMail.postMail(emailsTo, "Error en SOPDI", "Error en base de datos :  " + this.getClass().getName() + " -->" + ex1.getMessage());
            } catch (MessagingException ex2) {
                Logger.getLogger(PagoFacturaProveedorForm.class.getName()).log(Level.SEVERE, null, ex2);
            }

        }

    }

    public void limpiarPartida() {
        
        numeroTxt.setReadOnly(false);
        numeroTxt.setValue("");
        nombreChequeTxt.setReadOnly(false);
        nombreChequeTxt.setValue("");
        nombreChequeTxt.setReadOnly(((SopdiUI) mainUI).sessionInformation.getStrUserProfileName().equals("CONTADOR"));
        nombreChequeTxt.setReadOnly(((SopdiUI) mainUI).sessionInformation.getStrUserProfileName().equals("AUXILIAR"));
        montoTxt.setReadOnly(false);
        montoTxt.setValue(0.00);

        partidaContainer.removeAllItems();

        descripcionTxt.setValue("");
    }

    public void llenarComboProveedor() {
        String queryString = " SELECT * from proveedor ";
        queryString += " WHERE Inhabilitado = 0 ";
        queryString += " AND EsProveedor = 1 ";
        queryString += " Order By Nombre ";

        proveedorCbx.removeAllItems();

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) { //  encontrado
                proveedorCbx.addItem(rsRecords.getString("IDProveedor"));
                proveedorCbx.setItemCaption(rsRecords.getString("IDProveedor"), rsRecords.getString("Nombre"));
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar Proveedores " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public boolean exportToExcel(Grid theGrid) {
        if (theGrid.getHeightByRows() > 0) {
            TableHolder tableHolder = new DefaultTableHolder(theGrid);
            ExcelExport excelExport = new ExcelExport(tableHolder);
            excelExport.excludeCollapsedColumns();
            excelExport.setDisplayTotals(false);
            String fileexport;
// produccion            fileexport = (empresa + "_" + empresaLbl.getValue().substring(5, empresaLbl.getValue().length()).replaceAll(" ", "_").replaceAll(",", "_").replaceAll("[()]", "").replaceAll("[.]", "").replaceAll("ñ", "n").replaceAll("Ñ", "N").replaceAll("ó", "o").replaceAll("é","") + "_INTEGRACION_CAMBIOS.xlsx").replaceAll(" ", "").replaceAll(",", "");
            fileexport = (empresaCbx.getValue() + "_" + empresaCbx.getItemCaption(empresaCbx.getValue()).replaceAll(" ", "_").replaceAll(",", "_").replaceAll("[()]", "").replaceAll("[.]", "").replaceAll("ñ", "n").replaceAll("Ñ", "N").replaceAll("ó", "o").replaceAll("é", "") + "_DOCUMENTOS.xls").replaceAll(" ", "").replaceAll(",", "");
            excelExport.setExportFileName(fileexport);
            excelExport.export();
        }
        return true;
    }
}