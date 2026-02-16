package com.simpletecno.sopdi.tesoreria;

import com.simpletecno.sopdi.utilerias.MyEmailMessanger;
import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.addon.tableexport.DefaultTableHolder;
import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.addon.tableexport.TableHolder;
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vaadin.ui.NumberField;

import javax.mail.MessagingException;

/**
 *
 * @author user
 */
public class PagoLiquidacionForm extends Window {

    VerticalLayout mainLayout;

    double totalMonto;
    double totalQueztales;

    static final String ID_PROPERTY = "Id";
    static final String FECHA_PROPERTY = "Fecha";
    static final String IDLIQUIDADOR_PROPERTY = "Id Liquidador";
    static final String LIQUIDACION_PROPERTY = "Liquidación";
    static final String LIQUIDADOR_PROPERTY = "Liquidador";
    static final String MONEDA_PROPERTY = "Moneda";
    static final String VALOR_PROPERTY = "Monto";
    static final String TIPOCAMBIO_PROPERTY = "Tasa";
    static final String MONTO_QUETZALES_PROPERTY = "Monto Q.";
    static final String USUARIO_PROPERTY = "Usuario";
    static final String CODIGOCC_PROPERTY = "CodigoCC";

    public IndexedContainer liquidacionesContainer = new IndexedContainer();
    Grid liquidacionesGrid;
    Grid.FooterRow footerLiquidaciones;

    ComboBox monedaCbx;
    ComboBox medioCbx;
    DateField fechaDt;
    NumberField montoTxt;
    NumberField tasaCambioTxt;
    TextField nombreChequeTxt;
    TextField descripcionTxt;
    TextField numeroTxt;

    TextField codigoCCLiquidacion1Txt;
    TextField codigoCCLiquidacion2Txt;
    TextField codigoCCLiquidacion3Txt;
    TextField codigoCCLiquidacion4Txt;
    TextField codigoCCLiquidacion5Txt;
    TextField codigoCCLiquidacion6Txt;
    TextField codigoCCLiquidacion7Txt;

    NumberField debe1Txt;
    NumberField debe2Txt;
    NumberField debe3Txt;
    NumberField debe4Txt;
    NumberField debe5Txt;
    NumberField debe6Txt;
    NumberField debe7Txt;
    NumberField haber1Txt;
    NumberField haber2Txt;
    NumberField haber3Txt;
    NumberField haber4Txt;
    NumberField haber5Txt;
    NumberField haber6Txt;
    NumberField haber7Txt;

    ComboBox cuentaContable1Cbx;
    ComboBox cuentaContable2Cbx;
    ComboBox cuentaContable3Cbx;
    ComboBox cuentaContable4Cbx;
    ComboBox cuentaContable5Cbx;
    ComboBox cuentaContable6Cbx;
    ComboBox cuentaContable7Cbx;

    HorizontalLayout chequeLayout = new HorizontalLayout();
    HorizontalLayout chequeLayout2 = new HorizontalLayout();
    VerticalLayout partidaLayout = new VerticalLayout();

    Button excelBtn;

    String facturasPagadas;

    String tipoDocumentoPagado;

    Button grabarPartidaBtn;
    ComboBox empresaCbx;

    UI mainUI;
    Statement stQuery;
    ResultSet rsRecords;
    Statement stQuery1;
    Statement stQuery2;
    ResultSet rsRecords2;

    String queryString;

    String proveedorNombre;
    String proveedorId;
    String liquidadorId;
    String idNomenclatura;
    String codigoPartidaNuevo;

    BigDecimal totalDebeBD;
    BigDecimal totalHaberBD;
    
    Date fechaPago;

    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");
    static DecimalFormat numberFormat3 = new DecimalFormat("######0.00");

    String IdProveedor;

    public PagoLiquidacionForm(String IdProveedor , Date fechaPago) {

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
                if (liquidacionesContainer.size() > 0) {
                    exportToExcel(liquidacionesGrid);
                } else {
                    Notification.show("La vista no contiene registros disponibles.. ", Notification.Type.WARNING_MESSAGE);
                }
            }
        });

        Label titleLbl = new Label("PAGO DE LIQUIDACION");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();

        empresaCbx = new ComboBox("Empresa:");
        empresaCbx.setWidth("400px");
        empresaCbx.addStyleName(ValoTheme.COMBOBOX_HUGE);
        empresaCbx.setInvalidAllowed(false);
        empresaCbx.setNewItemsAllowed(false);
        empresaCbx.setTextInputAllowed(false);
        empresaCbx.setNullSelectionAllowed(false);
        llenarComboEmpresa();

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

        createTablaLiquidacion();
        llenarTablaLiquidacion();
        crearLayoutCheque();
        crearPartidaLayout();
        limpiarPartida();

    }

    public void llenarComboEmpresa() {
        queryString = " SELECT * from contabilidad_empresa";
        queryString += " Where IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();

        try {
            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords2 = stQuery1.executeQuery(queryString);

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

    public void createTablaLiquidacion() {

        VerticalLayout facturasLayout = new VerticalLayout();
        facturasLayout.addStyleName("rcorners3");
        facturasLayout.setWidth("100%");
        facturasLayout.setResponsive(true);
        facturasLayout.setSpacing(true);
        facturasLayout.setMargin(false);

        liquidacionesContainer.addContainerProperty(ID_PROPERTY, String.class, null);
        liquidacionesContainer.addContainerProperty(FECHA_PROPERTY, String.class, null);
        liquidacionesContainer.addContainerProperty(IDLIQUIDADOR_PROPERTY, String.class, null);
        liquidacionesContainer.addContainerProperty(LIQUIDACION_PROPERTY, String.class, null);
        liquidacionesContainer.addContainerProperty(LIQUIDADOR_PROPERTY, String.class, null);
        liquidacionesContainer.addContainerProperty(MONEDA_PROPERTY, String.class, null);
        liquidacionesContainer.addContainerProperty(VALOR_PROPERTY, String.class, null);
        liquidacionesContainer.addContainerProperty(TIPOCAMBIO_PROPERTY, String.class, null);
        liquidacionesContainer.addContainerProperty(MONTO_QUETZALES_PROPERTY, String.class, null);
        liquidacionesContainer.addContainerProperty(USUARIO_PROPERTY, String.class, null);
        liquidacionesContainer.addContainerProperty(CODIGOCC_PROPERTY, String.class, null);

        liquidacionesGrid = new Grid("", liquidacionesContainer);
        liquidacionesGrid.setWidth("100%");
        liquidacionesGrid.setImmediate(true);
        liquidacionesGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        liquidacionesGrid.setDescription("Seleccione uno o varios registros.");
        liquidacionesGrid.setHeightMode(HeightMode.ROW);
        liquidacionesGrid.setHeightByRows(5);
        liquidacionesGrid.setResponsive(true);
        liquidacionesGrid.setEditorBuffered(false);

        liquidacionesGrid.getColumn(ID_PROPERTY).setHidable(true).setHidden(true);
        liquidacionesGrid.getColumn(CODIGOCC_PROPERTY).setHidable(true).setHidden(true);
        liquidacionesGrid.getColumn(USUARIO_PROPERTY).setHidable(true).setHidden(true);
        liquidacionesGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {

            if (MONTO_QUETZALES_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (VALOR_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (TIPOCAMBIO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else {
                return null;
            }

        });

        liquidacionesGrid.addSelectionListener(new SelectionListener() {
            @Override
            public void select(SelectionEvent event) {
                if (liquidacionesGrid.getSelectedRows() != null) {

                    Double montoPagar = 0.00;
                    String moneda;
                    Object gridItem;

                    facturasPagadas = "";
                    proveedorNombre = "";
                    proveedorId = "0";
                    liquidadorId = "0";
                    tipoDocumentoPagado = "";
                    idNomenclatura = ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getLiquidacionesCajaChicha();

                    Iterator iter = event.getSelected().iterator();

                    if (iter == null) {

                        limpiarPartida();
//                        llenarComboProveedor();

                        nombreChequeTxt.setReadOnly(false);
                        nombreChequeTxt.setValue("");

                        montoTxt.setReadOnly(false);
                        montoTxt.setValue(montoPagar);
                        montoTxt.setReadOnly(true);
                        return;
                    }
                    if (!iter.hasNext()) {

                        limpiarPartida();
//                        llenarComboProveedor();

                        nombreChequeTxt.setReadOnly(false);
                        nombreChequeTxt.setValue("");

                        montoTxt.setReadOnly(false);
                        montoTxt.setValue(montoPagar);
                        montoTxt.setReadOnly(true);
                        return;
                    }

                    gridItem = iter.next();

                    proveedorId = String.valueOf(liquidacionesGrid.getContainerDataSource().getItem(gridItem).getItemProperty(IDLIQUIDADOR_PROPERTY).getValue());
                    liquidadorId = proveedorId;
                    proveedorNombre = String.valueOf(liquidacionesGrid.getContainerDataSource().getItem(gridItem).getItemProperty(LIQUIDADOR_PROPERTY).getValue());
                    moneda = String.valueOf(liquidacionesGrid.getContainerDataSource().getItem(gridItem).getItemProperty(MONEDA_PROPERTY).getValue());
                    montoPagar = Double.valueOf(String.valueOf(liquidacionesGrid.getContainerDataSource().getItem(gridItem).getItemProperty(VALOR_PROPERTY).getValue()).replaceAll(",", ""));
                    facturasPagadas = String.valueOf(liquidacionesGrid.getContainerDataSource().getItem(gridItem).getItemProperty(LIQUIDACION_PROPERTY).getValue()) + ",";
                    tipoDocumentoPagado = "LIQUIDACION";

                    monedaCbx.setReadOnly(false);
                    monedaCbx.select(moneda);
                    monedaCbx.setReadOnly(true);

                    while (iter.hasNext()) {
                        gridItem = iter.next();

                        //VALIDAR QUE SEA EL MISMO PROVEEDOR
                        if (!proveedorId.equals(String.valueOf(liquidacionesGrid.getContainerDataSource().getItem(gridItem).getItemProperty(IDLIQUIDADOR_PROPERTY).getValue()))) {
                            Notification.show("SOLO SE PUEDEN PAGAR VARIAS LIQUIDACIONES DEL MISMO LIQUIDAOR, REVISE!", Notification.Type.WARNING_MESSAGE);
                            liquidacionesGrid.deselect(gridItem);
                            return;
                        }
                        //VALIDAR QUE SEA LA MISMA MONEDA
                        if (!moneda.equals(String.valueOf(liquidacionesGrid.getContainerDataSource().getItem(gridItem).getItemProperty(MONEDA_PROPERTY).getValue()))) {
                            Notification.show("SOLO SE PUEDEN PAGAR VARIAS LIQUIDACIONES DEL MISMO LIQUIDADOR Y DE LA MISMA MONEDA, REVISE!", Notification.Type.WARNING_MESSAGE);
                            liquidacionesGrid.deselect(gridItem);
                            return;
                        }
                        montoPagar += Double.valueOf(String.valueOf(liquidacionesGrid.getContainerDataSource().getItem(gridItem).getItemProperty(VALOR_PROPERTY).getValue()).replaceAll(",", ""));
                        facturasPagadas += String.valueOf(liquidacionesGrid.getContainerDataSource().getItem(gridItem).getItemProperty(LIQUIDACION_PROPERTY).getValue()) + ",";
                    }

                    limpiarPartida();

                    nombreChequeTxt.setValue(proveedorNombre);

                    descripcionTxt.setValue("PAGO DE LIQUIDACION : [" + facturasPagadas + "] LIQUIDADOR : [" + proveedorNombre + "]");

                    montoTxt.setReadOnly(false);
                    montoTxt.setValue(montoPagar);
                    montoTxt.setReadOnly(true);

                    Iterator iter2 = liquidacionesGrid.getSelectedRows().iterator();

                    String codigoCCSelect = "";
                    double valorMonto = 0.00;

                    while (iter2.hasNext()) {

                        Object gridItem2 = iter2.next();

                        codigoCCSelect = String.valueOf(liquidacionesGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(CODIGOCC_PROPERTY).getValue()).replaceAll(",", "");
                        valorMonto = Double.valueOf(String.valueOf(liquidacionesGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(VALOR_PROPERTY).getValue()).replaceAll(",", ""));

                        if (cuentaContable1Cbx.getValue() == null) {
                            codigoCCLiquidacion1Txt.setValue(codigoCCSelect);
                            cuentaContable1Cbx.setValue(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getLiquidacionesCajaChicha());
                            debe1Txt.setValue(valorMonto);
                        } else if (cuentaContable2Cbx.getValue() == null) {
                            codigoCCLiquidacion2Txt.setValue(codigoCCSelect);
                            cuentaContable2Cbx.setValue(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getLiquidacionesCajaChicha());
                            debe2Txt.setValue(valorMonto);
                        } else if (cuentaContable3Cbx.getValue() == null) {
                            codigoCCLiquidacion3Txt.setValue(codigoCCSelect);
                            cuentaContable3Cbx.setValue(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getLiquidacionesCajaChicha());
                            debe3Txt.setValue(valorMonto);
                        } else if (cuentaContable4Cbx.getValue() == null) {
                            codigoCCLiquidacion4Txt.setValue(codigoCCSelect);
                            cuentaContable4Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getLiquidacionesCajaChicha());
                            debe4Txt.setValue(valorMonto);
                        } else if (cuentaContable5Cbx.getValue() == null) {
                            codigoCCLiquidacion5Txt.setValue(codigoCCSelect);
                            cuentaContable5Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getLiquidacionesCajaChicha());
                            debe5Txt.setValue(valorMonto);
                        } else if (cuentaContable6Cbx.getValue() == null) {
                            codigoCCLiquidacion6Txt.setValue(codigoCCSelect);
                            cuentaContable6Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getLiquidacionesCajaChicha());
                            debe6Txt.setValue(valorMonto);
                        }

                    }
                    if (cuentaContable2Cbx.getValue() == null) {
                        cuentaContable2Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaLocal());
                        haber2Txt.setValue(montoPagar);
                    } else if (cuentaContable3Cbx.getValue() == null) {
                        cuentaContable3Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaLocal());
                        haber3Txt.setValue(montoPagar);
                    } else if (cuentaContable4Cbx.getValue() == null) {
                        cuentaContable4Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaLocal());
                        haber4Txt.setValue(montoPagar);
                    } else if (cuentaContable5Cbx.getValue() == null) {
                        cuentaContable5Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaLocal());
                        haber5Txt.setValue(montoPagar);
                    } else if (cuentaContable6Cbx.getValue() == null) {
                        cuentaContable6Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaLocal());
                        haber6Txt.setValue(montoPagar);
                    } else if (cuentaContable7Cbx.getValue() == null) {
                        cuentaContable7Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaLocal());
                        haber7Txt.setValue(montoPagar);
                    }

                }
            }
        });

        HeaderRow filterRow = liquidacionesGrid.appendHeaderRow();

        HeaderCell cell = filterRow.getCell(LIQUIDACION_PROPERTY);

        TextField filterField = new TextField();
        filterField.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField.setInputPrompt("Filtrar");
        filterField.setColumns(5);

        filterField.addTextChangeListener(change -> {
            liquidacionesContainer.removeContainerFilters(LIQUIDACION_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                liquidacionesContainer.addContainerFilter(
                        new SimpleStringFilter(LIQUIDACION_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell.setComponent(filterField);

        HeaderCell cell1 = filterRow.getCell(MONEDA_PROPERTY);

        TextField filterField1 = new TextField();
        filterField1.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField1.setInputPrompt("Filtrar");
        filterField1.setColumns(8);

        filterField1.addTextChangeListener(change -> {
            liquidacionesContainer.removeContainerFilters(MONEDA_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                liquidacionesContainer.addContainerFilter(
                        new SimpleStringFilter(MONEDA_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell1.setComponent(filterField1);

        HeaderCell cell2 = filterRow.getCell(LIQUIDADOR_PROPERTY);

        TextField filterField2 = new TextField();
        filterField2.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField2.setInputPrompt("Filtrar");
        filterField2.setColumns(20);

        filterField2.addTextChangeListener(change -> {
            liquidacionesContainer.removeContainerFilters(LIQUIDADOR_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                liquidacionesContainer.addContainerFilter(
                        new SimpleStringFilter(LIQUIDADOR_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell2.setComponent(filterField2);

        footerLiquidaciones = liquidacionesGrid.appendFooterRow();
        footerLiquidaciones.getCell(MONEDA_PROPERTY).setText("Totales");
        footerLiquidaciones.getCell(VALOR_PROPERTY).setText("0.00");
        footerLiquidaciones.getCell(MONTO_QUETZALES_PROPERTY).setText("0.00");
        footerLiquidaciones.getCell(VALOR_PROPERTY).setStyleName("rightalign");
        footerLiquidaciones.getCell(MONTO_QUETZALES_PROPERTY).setStyleName("rightalign");

        facturasLayout.addComponent(liquidacionesGrid);
        facturasLayout.setComponentAlignment(liquidacionesGrid, Alignment.MIDDLE_CENTER);

        mainLayout.addComponent(facturasLayout);
        mainLayout.setComponentAlignment(facturasLayout, Alignment.MIDDLE_CENTER);
    }

    public void llenarTablaLiquidacion() {

        footerLiquidaciones.getCell(VALOR_PROPERTY).setText("0.00");
        footerLiquidaciones.getCell(MONTO_QUETZALES_PROPERTY).setText("0.00");

        liquidacionesContainer.removeAllItems();

        totalMonto = 0.00;
        totalQueztales = 0.00;

        queryString = " Select contabilidad_partida.IdPartida, contabilidad_partida.Fecha, contabilidad_partida.CodigoCC,";
        queryString += " contabilidad_partida.MonedaDocumento, contabilidad_partida.TipoCambio,  ";
        queryString += " contabilidad_partida.IdProveedor, contabilidad_partida.NombreProveedor, ";
        queryString += " contabilidad_partida.IdLiquidacion, contabilidad_partida.IdLiquidador,  ";
        queryString += " SUM(Haber) as Total,usuario.Nombre as uNombre, proveedor.Nombre as NLiquidador   ";
        queryString += " From contabilidad_partida,usuario, proveedor ";
        queryString += " Where contabilidad_partida.IdEmpresa = " + empresaCbx.getValue();
        queryString += " And UPPER(contabilidad_partida.TipoDocumento) IN ('FACTURA', 'RECIBO','RECIBO CONTABLE', 'RECIBO CORRIENTE', 'FORMULARIO','NOTA DE CREDITO')";
        queryString += " And contabilidad_partida.IdLiquidador = " + IdProveedor;
        queryString += " And contabilidad_partida.IdLiquidacion > 0   ";
        queryString += " And contabilidad_partida.MontoAutorizadoPagar > 0   ";
        queryString += " And usuario.IdUsuario = contabilidad_partida.CreadoUsuario   ";
        queryString += " And proveedor.IdProveedor = contabilidad_partida.IdLiquidador  ";
        queryString += " AND contabilidad_partida.IdNomenclatura = " + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getLiquidacionesCajaChicha();
        queryString += " Group by contabilidad_partida.IdLiquidacion, contabilidad_partida.IdLiquidador ";

System.out.println("CONSULTA DE LIQUIDACIONES POR PAGAR=" + queryString);

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado
                do {

                    Object itemId = liquidacionesContainer.addItem();

                    liquidacionesContainer.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords.getString("IdPartida"));
                    liquidacionesContainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha")));
                    liquidacionesContainer.getContainerProperty(itemId, LIQUIDACION_PROPERTY).setValue(rsRecords.getString("Idliquidacion"));
                    liquidacionesContainer.getContainerProperty(itemId, IDLIQUIDADOR_PROPERTY).setValue(rsRecords.getString("Idliquidador"));
                    liquidacionesContainer.getContainerProperty(itemId, LIQUIDADOR_PROPERTY).setValue(rsRecords.getString("NLiquidador"));
                    liquidacionesContainer.getContainerProperty(itemId, MONEDA_PROPERTY).setValue(rsRecords.getString("MonedaDocumento"));
                    liquidacionesContainer.getContainerProperty(itemId, VALOR_PROPERTY).setValue(numberFormat.format(rsRecords.getDouble("Total")));
                    liquidacionesContainer.getContainerProperty(itemId, TIPOCAMBIO_PROPERTY).setValue(rsRecords.getString("TipoCambio"));
                    liquidacionesContainer.getContainerProperty(itemId, MONTO_QUETZALES_PROPERTY).setValue(numberFormat.format(rsRecords.getDouble("Total") * rsRecords.getDouble("TipoCambio")));
                    liquidacionesContainer.getContainerProperty(itemId, USUARIO_PROPERTY).setValue(rsRecords.getString("UNombre"));
                    liquidacionesContainer.getContainerProperty(itemId, CODIGOCC_PROPERTY).setValue(rsRecords.getString("CodigoCC"));

                    totalMonto = totalMonto + rsRecords.getDouble("Total");
                    totalQueztales = totalQueztales + (rsRecords.getDouble("Total") * rsRecords.getDouble("TipoCambio"));

                } while (rsRecords.next());
            }

        } catch (Exception ex) {
            System.out.println("Error al listar tabla de Liquidaciones : " + ex);
            ex.printStackTrace();
        }

        footerLiquidaciones.getCell(VALOR_PROPERTY).setText(numberFormat.format(totalMonto));
        footerLiquidaciones.getCell(MONTO_QUETZALES_PROPERTY).setText(numberFormat.format(totalQueztales));

    }

    private void crearLayoutCheque() {

//        chequeLayout.setHeight("60%");
        chequeLayout.setSpacing(true);
        chequeLayout.setMargin(false);
        chequeLayout.setSizeUndefined();
//        chequeLayout.addStyleName(ValoTheme.LAYOUT_HORIZONTAL_WRAPPING);

        chequeLayout2.setSpacing(true);
        chequeLayout2.setMargin(false);
        chequeLayout2.setSizeUndefined();

        numeroTxt = new TextField("# Documento : ");
        numeroTxt.setWidth("8em");

        medioCbx = new ComboBox("Medio : ");
        medioCbx.setWidth("10em");
        medioCbx.addItem("CHEQUE");
        medioCbx.addItem("NOTA DE DEBITO");
        medioCbx.select("CHEQUE");

        monedaCbx = new ComboBox("Moneda : ");
        monedaCbx.setWidth("10em");
        monedaCbx.addItem("QUETZALES");
        monedaCbx.addItem("DOLARES");
        monedaCbx.select("QUETZALES");
        monedaCbx.addValueChangeListener((event) -> {
            if (cuentaContable2Cbx.getValue() != null) {

                if (monedaCbx.getValue() == "DOLARES") {

                    tasaCambioTxt.setValue((Float.toString(((SopdiUI) UI.getCurrent()).sessionInformation.getFltExchangeRate())));

                    if (String.valueOf(cuentaContable2Cbx.getValue()).equals(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getLiquidacionesCajaChicha())) {
                        cuentaContable3Cbx.setReadOnly(false);
                        cuentaContable3Cbx.setValue(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaExtranjera());
                        cuentaContable3Cbx.setReadOnly(true);
                    } else {
                        cuentaContable2Cbx.setReadOnly(false);
                        cuentaContable2Cbx.setValue(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaExtranjera());
                        cuentaContable2Cbx.setReadOnly(true);

                    }
                } else {
                    tasaCambioTxt.setValue(1.00);
                    if (String.valueOf(cuentaContable2Cbx.getValue()).equals(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getLiquidacionesCajaChicha())) {
                        cuentaContable3Cbx.setReadOnly(false);
                        cuentaContable3Cbx.setValue(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaLocal());
                        cuentaContable3Cbx.setReadOnly(true);

                    } else {
                        cuentaContable2Cbx.setReadOnly(false);
                        cuentaContable2Cbx.setValue(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaLocal());
                        cuentaContable2Cbx.setReadOnly(true);
                    }
                }

            }
        });

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

        nombreChequeTxt = new TextField("Nombre cheque/transf. : ");
        nombreChequeTxt.setWidth("20em");
        nombreChequeTxt.setValue("");
        nombreChequeTxt.setReadOnly(((SopdiUI) mainUI).sessionInformation.getStrUserProfileName().equals("CONTADOR"));
        nombreChequeTxt.setReadOnly(((SopdiUI) mainUI).sessionInformation.getStrUserProfileName().equals("AUXILIAR"));

        descripcionTxt = new TextField("Observación extra : ");
        descripcionTxt.setWidth("35em");
        descripcionTxt.setVisible(false);

        chequeLayout.addComponent(medioCbx);
        chequeLayout.setComponentAlignment(medioCbx, Alignment.MIDDLE_CENTER);
        chequeLayout.addComponent(numeroTxt);
        chequeLayout.setComponentAlignment(numeroTxt, Alignment.MIDDLE_CENTER);
        chequeLayout.addComponent(fechaDt);
        chequeLayout.setComponentAlignment(fechaDt, Alignment.MIDDLE_CENTER);
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
        partidaLayout.setWidth("70%");
        partidaLayout.setResponsive(true);
        partidaLayout.setSpacing(false);
        partidaLayout.setMargin(false);

        HorizontalLayout layoutHorizontal1 = new HorizontalLayout();
        layoutHorizontal1.setSpacing(true);
        layoutHorizontal1.setMargin(false);

        HorizontalLayout layoutHorizontal2 = new HorizontalLayout();
        layoutHorizontal2.setSpacing(true);
        layoutHorizontal2.setMargin(false);

        HorizontalLayout layoutHorizontal3 = new HorizontalLayout();
        layoutHorizontal3.setSpacing(true);
        layoutHorizontal3.setMargin(false);

        HorizontalLayout layoutHorizontal4 = new HorizontalLayout();
        layoutHorizontal4.setSpacing(true);
        layoutHorizontal4.setMargin(false);

        HorizontalLayout layoutHorizontal5 = new HorizontalLayout();
        layoutHorizontal5.setSpacing(true);
        layoutHorizontal5.setMargin(false);

        HorizontalLayout layoutHorizontal6 = new HorizontalLayout();
        layoutHorizontal6.setSpacing(true);
        layoutHorizontal6.setMargin(false);

        HorizontalLayout layoutHorizontal7 = new HorizontalLayout();
        layoutHorizontal7.setSpacing(true);
        layoutHorizontal7.setMargin(false);

        HorizontalLayout layoutHorizontal8 = new HorizontalLayout();
        layoutHorizontal8.setSpacing(true);
//        layoutHorizontal6.setMargin(new MarginInfo(true,false,false,false));
        layoutHorizontal8.setMargin(false);
        layoutHorizontal8.setWidth("90%");

        codigoCCLiquidacion1Txt = new TextField("");
        codigoCCLiquidacion1Txt.setWidth("8em");
        codigoCCLiquidacion1Txt.setVisible(false);

        codigoCCLiquidacion2Txt = new TextField("");
        codigoCCLiquidacion2Txt.setWidth("8em");
        codigoCCLiquidacion2Txt.setVisible(false);

        codigoCCLiquidacion3Txt = new TextField("");
        codigoCCLiquidacion3Txt.setWidth("8em");
        codigoCCLiquidacion3Txt.setVisible(false);

        codigoCCLiquidacion4Txt = new TextField("");
        codigoCCLiquidacion4Txt.setWidth("8em");
        codigoCCLiquidacion4Txt.setVisible(false);

        codigoCCLiquidacion5Txt = new TextField("");
        codigoCCLiquidacion5Txt.setWidth("8em");
        codigoCCLiquidacion5Txt.setVisible(false);

        codigoCCLiquidacion6Txt = new TextField("");
        codigoCCLiquidacion6Txt.setWidth("8em");
        codigoCCLiquidacion6Txt.setVisible(false);

        codigoCCLiquidacion7Txt = new TextField("");
        codigoCCLiquidacion7Txt.setWidth("8em");
        codigoCCLiquidacion7Txt.setVisible(false);

        cuentaContable1Cbx = new ComboBox("Cuenta contable :");
        cuentaContable1Cbx.setWidth("30em");
        cuentaContable1Cbx.setFilteringMode(FilteringMode.CONTAINS);

        cuentaContable2Cbx = new ComboBox();
        cuentaContable2Cbx.setWidth("30em");
        cuentaContable2Cbx.setFilteringMode(FilteringMode.CONTAINS);

        cuentaContable3Cbx = new ComboBox();
        cuentaContable3Cbx.setWidth("30em");
        cuentaContable3Cbx.setFilteringMode(FilteringMode.CONTAINS);

        cuentaContable4Cbx = new ComboBox();
        cuentaContable4Cbx.setWidth("30em");
        cuentaContable4Cbx.setFilteringMode(FilteringMode.CONTAINS);

        cuentaContable5Cbx = new ComboBox();
        cuentaContable5Cbx.setWidth("30em");
        cuentaContable5Cbx.setFilteringMode(FilteringMode.CONTAINS);

        cuentaContable6Cbx = new ComboBox();
        cuentaContable6Cbx.setWidth("30em");
        cuentaContable6Cbx.setFilteringMode(FilteringMode.CONTAINS);

        cuentaContable7Cbx = new ComboBox();
        cuentaContable7Cbx.setWidth("30em");
        cuentaContable7Cbx.setFilteringMode(FilteringMode.CONTAINS);

        llenarComboCuentaContable();

        debe1Txt = new NumberField("Debe :");
        debe1Txt.setValidationVisible(false);
        debe1Txt.setDecimalAllowed(true);
        debe1Txt.setDecimalPrecision(2);
        debe1Txt.setMinimumFractionDigits(2);
        debe1Txt.setDecimalSeparator('.');
        debe1Txt.setDecimalSeparatorAlwaysShown(true);
        debe1Txt.setValue(0d);
        debe1Txt.setGroupingUsed(true);
        debe1Txt.setGroupingSeparator(',');
        debe1Txt.setGroupingSize(3);
        debe1Txt.setImmediate(true);
        debe1Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        debe1Txt.setWidth("8em");
        debe1Txt.setValue(0.00);

        debe2Txt = new NumberField();
        debe2Txt.setValidationVisible(false);
        debe2Txt.setDecimalAllowed(true);
        debe2Txt.setDecimalPrecision(2);
        debe2Txt.setMinimumFractionDigits(2);
        debe2Txt.setDecimalSeparator('.');
        debe2Txt.setDecimalSeparatorAlwaysShown(true);
        debe2Txt.setValue(0d);
        debe2Txt.setGroupingUsed(true);
        debe2Txt.setGroupingSeparator(',');
        debe2Txt.setGroupingSize(3);
        debe2Txt.setImmediate(true);
        debe2Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        debe2Txt.setWidth("8em");
        debe2Txt.setValue(0.00);

        debe3Txt = new NumberField();
        debe3Txt.setValidationVisible(false);
        debe3Txt.setDecimalAllowed(true);
        debe3Txt.setDecimalPrecision(2);
        debe3Txt.setMinimumFractionDigits(2);
        debe3Txt.setDecimalSeparator('.');
        debe3Txt.setDecimalSeparatorAlwaysShown(true);
        debe3Txt.setValue(0d);
        debe3Txt.setGroupingUsed(true);
        debe3Txt.setGroupingSeparator(',');
        debe3Txt.setGroupingSize(3);
        debe3Txt.setImmediate(true);
        debe3Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        debe3Txt.setWidth("8em");
        debe3Txt.setValue(0.00);

        debe4Txt = new NumberField();
        debe4Txt.setValidationVisible(false);
        debe4Txt.setDecimalAllowed(true);
        debe4Txt.setDecimalPrecision(2);
        debe4Txt.setMinimumFractionDigits(2);
        debe4Txt.setDecimalSeparator('.');
        debe4Txt.setDecimalSeparatorAlwaysShown(true);
        debe4Txt.setValue(0d);
        debe4Txt.setGroupingUsed(true);
        debe4Txt.setGroupingSeparator(',');
        debe4Txt.setGroupingSize(3);
        debe4Txt.setImmediate(true);
        debe4Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        debe4Txt.setWidth("8em");
        debe4Txt.setValue(0.00);

        debe5Txt = new NumberField();
        debe5Txt.setValidationVisible(false);
        debe5Txt.setDecimalAllowed(true);
        debe5Txt.setDecimalPrecision(2);
        debe5Txt.setMinimumFractionDigits(2);
        debe5Txt.setDecimalSeparator('.');
        debe5Txt.setDecimalSeparatorAlwaysShown(true);
        debe5Txt.setValue(0d);
        debe5Txt.setGroupingUsed(true);
        debe5Txt.setGroupingSeparator(',');
        debe5Txt.setGroupingSize(3);
        debe5Txt.setImmediate(true);
        debe5Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        debe5Txt.setWidth("8em");
        debe5Txt.setValue(0.00);

        debe6Txt = new NumberField();
        debe6Txt.setValidationVisible(false);
        debe6Txt.setDecimalAllowed(true);
        debe6Txt.setDecimalPrecision(2);
        debe6Txt.setMinimumFractionDigits(2);
        debe6Txt.setDecimalSeparator('.');
        debe6Txt.setDecimalSeparatorAlwaysShown(true);
        debe6Txt.setValue(0d);
        debe6Txt.setGroupingUsed(true);
        debe6Txt.setGroupingSeparator(',');
        debe6Txt.setGroupingSize(3);
        debe6Txt.setImmediate(true);
        debe6Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        debe6Txt.setWidth("8em");
        debe6Txt.setValue(0.00);

        debe7Txt = new NumberField();
        debe7Txt.setValidationVisible(false);
        debe7Txt.setDecimalAllowed(true);
        debe7Txt.setDecimalPrecision(2);
        debe7Txt.setMinimumFractionDigits(2);
        debe7Txt.setDecimalSeparator('.');
        debe7Txt.setDecimalSeparatorAlwaysShown(true);
        debe7Txt.setValue(0d);
        debe7Txt.setGroupingUsed(true);
        debe7Txt.setGroupingSeparator(',');
        debe7Txt.setGroupingSize(3);
        debe7Txt.setImmediate(true);
        debe7Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        debe7Txt.setWidth("8em");
        debe7Txt.setValue(0.00);

        haber1Txt = new NumberField("Haber:");
        haber1Txt.setValidationVisible(false);
        haber1Txt.setDecimalAllowed(true);
        haber1Txt.setDecimalPrecision(2);
        haber1Txt.setMinimumFractionDigits(2);
        haber1Txt.setDecimalSeparator('.');
        haber1Txt.setDecimalSeparatorAlwaysShown(true);
        haber1Txt.setValue(0d);
        haber1Txt.setGroupingUsed(true);
        haber1Txt.setGroupingSeparator(',');
        haber1Txt.setGroupingSize(3);
        haber1Txt.setImmediate(true);
        haber1Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        haber1Txt.setWidth("8em");
        haber1Txt.setValue(0.00);

        haber2Txt = new NumberField();
        haber2Txt.setValidationVisible(false);
        haber2Txt.setDecimalAllowed(true);
        haber2Txt.setDecimalPrecision(2);
        haber2Txt.setMinimumFractionDigits(2);
        haber2Txt.setDecimalSeparator('.');
        haber2Txt.setDecimalSeparatorAlwaysShown(true);
        haber2Txt.setValue(0d);
        haber2Txt.setGroupingUsed(true);
        haber2Txt.setGroupingSeparator(',');
        haber2Txt.setGroupingSize(3);
        haber2Txt.setImmediate(true);
        haber2Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        haber2Txt.setWidth("8em");
        haber2Txt.setValue(0.00);

        haber3Txt = new NumberField();
        haber3Txt.setValidationVisible(false);
        haber3Txt.setDecimalAllowed(true);
        haber3Txt.setDecimalPrecision(2);
        haber3Txt.setMinimumFractionDigits(2);
        haber3Txt.setDecimalSeparator('.');
        haber3Txt.setDecimalSeparatorAlwaysShown(true);
        haber3Txt.setValue(0d);
        haber3Txt.setGroupingUsed(true);
        haber3Txt.setGroupingSeparator(',');
        haber3Txt.setGroupingSize(3);
        haber3Txt.setImmediate(true);
        haber3Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        haber3Txt.setWidth("8em");
        haber3Txt.setValue(0.00);

        haber4Txt = new NumberField();
        haber4Txt.setValidationVisible(false);
        haber4Txt.setDecimalAllowed(true);
        haber4Txt.setDecimalPrecision(2);
        haber4Txt.setMinimumFractionDigits(2);
        haber4Txt.setDecimalSeparator('.');
        haber4Txt.setDecimalSeparatorAlwaysShown(true);
        haber4Txt.setValue(0d);
        haber4Txt.setGroupingUsed(true);
        haber4Txt.setGroupingSeparator(',');
        haber4Txt.setGroupingSize(3);
        haber4Txt.setImmediate(true);
        haber4Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        haber4Txt.setWidth("8em");
        haber4Txt.setValue(0.00);

        haber5Txt = new NumberField();
        haber5Txt.setValidationVisible(false);
        haber5Txt.setDecimalAllowed(true);
        haber5Txt.setDecimalPrecision(2);
        haber5Txt.setMinimumFractionDigits(2);
        haber5Txt.setDecimalSeparator('.');
        haber5Txt.setDecimalSeparatorAlwaysShown(true);
        haber5Txt.setValue(0d);
        haber5Txt.setGroupingUsed(true);
        haber5Txt.setGroupingSeparator(',');
        haber5Txt.setGroupingSize(3);
        haber5Txt.setImmediate(true);
        haber5Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        haber5Txt.setWidth("8em");
        haber5Txt.setValue(0.00);

        haber6Txt = new NumberField();
        haber6Txt.setValidationVisible(false);
        haber6Txt.setDecimalAllowed(true);
        haber6Txt.setDecimalPrecision(2);
        haber6Txt.setMinimumFractionDigits(2);
        haber6Txt.setDecimalSeparator('.');
        haber6Txt.setDecimalSeparatorAlwaysShown(true);
        haber6Txt.setValue(0d);
        haber6Txt.setGroupingUsed(true);
        haber6Txt.setGroupingSeparator(',');
        haber6Txt.setGroupingSize(3);
        haber6Txt.setImmediate(true);
        haber6Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        haber6Txt.setWidth("8em");
        haber6Txt.setValue(0.00);

        haber7Txt = new NumberField();
        haber7Txt.setValidationVisible(false);
        haber7Txt.setDecimalAllowed(true);
        haber7Txt.setDecimalPrecision(2);
        haber7Txt.setMinimumFractionDigits(2);
        haber7Txt.setDecimalSeparator('.');
        haber7Txt.setDecimalSeparatorAlwaysShown(true);
        haber7Txt.setValue(0d);
        haber7Txt.setGroupingUsed(true);
        haber7Txt.setGroupingSeparator(',');
        haber7Txt.setGroupingSize(3);
        haber7Txt.setImmediate(true);
        haber7Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        haber7Txt.setWidth("8em");
        haber7Txt.setValue(0.00);

        grabarPartidaBtn = new Button("Grabar");
        grabarPartidaBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        grabarPartidaBtn.setIcon(FontAwesome.SAVE);
        grabarPartidaBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                //     if (cuentaRepetida()) {
                //         return;
                //     }
                insertarPartidaCompuesta();
            }
        });

        Button cancelarBtn = new Button("Cancelar");
        cancelarBtn.addStyleName(ValoTheme.BUTTON_DANGER);
        cancelarBtn.setIcon(FontAwesome.TRASH);
        cancelarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                limpiarPartida();
            }
        });

        layoutHorizontal1.addComponent(cuentaContable1Cbx);
        layoutHorizontal1.addComponent(debe1Txt);
        layoutHorizontal1.addComponent(haber1Txt);

        layoutHorizontal2.addComponent(cuentaContable2Cbx);
        layoutHorizontal2.addComponent(debe2Txt);
        layoutHorizontal2.addComponent(haber2Txt);

        layoutHorizontal3.addComponent(cuentaContable3Cbx);
        layoutHorizontal3.addComponent(debe3Txt);
        layoutHorizontal3.addComponent(haber3Txt);

        layoutHorizontal4.addComponent(cuentaContable4Cbx);
        layoutHorizontal4.addComponent(debe4Txt);
        layoutHorizontal4.addComponent(haber4Txt);

        layoutHorizontal5.addComponent(cuentaContable5Cbx);
        layoutHorizontal5.addComponent(debe5Txt);
        layoutHorizontal5.addComponent(haber5Txt);

        layoutHorizontal6.addComponent(cuentaContable6Cbx);
        layoutHorizontal6.addComponent(debe6Txt);
        layoutHorizontal6.addComponent(haber6Txt);

        layoutHorizontal7.addComponent(cuentaContable7Cbx);
        layoutHorizontal7.addComponent(debe7Txt);
        layoutHorizontal7.addComponent(haber7Txt);

        layoutHorizontal8.addComponent(cancelarBtn);
        layoutHorizontal8.setComponentAlignment(cancelarBtn, Alignment.BOTTOM_LEFT);

        layoutHorizontal8.addComponent(grabarPartidaBtn);
        layoutHorizontal8.setComponentAlignment(grabarPartidaBtn, Alignment.BOTTOM_RIGHT);

        partidaLayout.addComponent(layoutHorizontal1);
        partidaLayout.setComponentAlignment(layoutHorizontal1, Alignment.MIDDLE_CENTER);

        partidaLayout.addComponent(layoutHorizontal2);
        partidaLayout.setComponentAlignment(layoutHorizontal2, Alignment.MIDDLE_CENTER);

        partidaLayout.addComponent(layoutHorizontal3);
        partidaLayout.setComponentAlignment(layoutHorizontal3, Alignment.MIDDLE_CENTER);

        partidaLayout.addComponent(layoutHorizontal4);
        partidaLayout.setComponentAlignment(layoutHorizontal4, Alignment.MIDDLE_CENTER);

        partidaLayout.addComponent(layoutHorizontal5);
        partidaLayout.setComponentAlignment(layoutHorizontal5, Alignment.MIDDLE_CENTER);

        partidaLayout.addComponent(layoutHorizontal6);
        partidaLayout.setComponentAlignment(layoutHorizontal6, Alignment.MIDDLE_CENTER);

        partidaLayout.addComponent(layoutHorizontal7);
        partidaLayout.setComponentAlignment(layoutHorizontal7, Alignment.MIDDLE_CENTER);

        partidaLayout.addComponent(layoutHorizontal8);
        partidaLayout.setComponentAlignment(layoutHorizontal8, Alignment.MIDDLE_CENTER);

        mainLayout.addComponent(partidaLayout);
        mainLayout.setComponentAlignment(partidaLayout, Alignment.MIDDLE_CENTER);

    }

    public void llenarComboCuentaContable() {
        String queryString = " SELECT * from contabilidad_nomenclatura ";
//      queryString += " where FiltrarFormularioLiquidacion = ?";
        queryString += " where Estatus='HABILITADA'";
        queryString += " Order By N5";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) { //  encontrado
                cuentaContable1Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable1Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable2Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable2Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable3Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable3Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable4Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable4Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable5Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable5Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable6Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable6Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable7Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable7Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));
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
        if (cuentaContable1Cbx.getValue() == null || cuentaContable2Cbx.getValue() == null) {
            Notification.show("Por favor elija la cuenta contable que corresponda. ", Notification.Type.ERROR_MESSAGE);
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

        totalDebeBD = new BigDecimal(debe1Txt.getDoubleValueDoNotThrow()
                + debe2Txt.getDoubleValueDoNotThrow() + debe3Txt.getDoubleValueDoNotThrow()
                + debe4Txt.getDoubleValueDoNotThrow() + debe5Txt.getDoubleValueDoNotThrow()
                + debe6Txt.getDoubleValueDoNotThrow() + debe7Txt.getDoubleValueDoNotThrow()).setScale(2, BigDecimal.ROUND_HALF_UP);

        totalHaberBD = new BigDecimal(haber1Txt.getDoubleValueDoNotThrow()
                + haber2Txt.getDoubleValueDoNotThrow() + haber3Txt.getDoubleValueDoNotThrow()
                + haber4Txt.getDoubleValueDoNotThrow() + haber5Txt.getDoubleValueDoNotThrow()
                + haber6Txt.getDoubleValueDoNotThrow() + haber7Txt.getDoubleValueDoNotThrow()).setScale(2, BigDecimal.ROUND_HALF_UP);

        totalDebeBD.setScale(2, BigDecimal.ROUND_HALF_UP);
        totalHaberBD.setScale(2, BigDecimal.ROUND_HALF_UP);

 System.out.println("Monto del total Debe Normal " +  totalDebeBD + " monto Total Debe getDouble " + totalDebeBD.doubleValue());

 System.out.println("Monto del total haber Normal " +  totalHaberBD + " monto Total haber getDouble " + totalHaberBD.doubleValue());

        if (totalDebeBD.doubleValue() != totalHaberBD.doubleValue()) {
            Notification notif = new Notification("EL MONTO DEL DEBE Y EL HABER NO COINCIDEN!. MONTO DEL DEBE : " + totalDebeBD.doubleValue() + " MONTO DEL HABER : " + totalHaberBD.doubleValue(),
                    Notification.Type.ERROR_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.WARNING);
            notif.show(Page.getCurrent());
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

        String descripcion;// = "PAGO DE " + tipoDocumentoPagado;
//        descripcion += " " + facturasPagadas;
//        descripcion += " " + proveedorNombre;
        //ojo aqui
        descripcion = descripcionTxt.getValue().trim();

        queryString = " Insert Into contabilidad_partida (IdEmpresa, Estatus, CodigoPartida, CodigoCC,";
        queryString += " TipoDocumento, NoDOCA, TipoDOCA, Fecha, IdProveedor, NITProveedor, ";
        queryString += " NombreProveedor, NombreCheque, MontoDocumento, SerieDocumento, NumeroDocumento, ";
        queryString += " IdNomenclatura, MonedaDocumento, Debe, Haber,";
        queryString += " DebeQuetzales, HaberQuetzales, TipoCambio, IdLiquidador,";
        queryString += " Descripcion, CreadoUsuario, CreadoFechaYHora)";
        queryString += " Values ";
        queryString += " (";
        queryString += String.valueOf(empresaCbx.getValue());
        queryString += ",'INGRESADO'";
        queryString += ",'" + codigoPartida + "'";
        queryString += ",'" + codigoCCLiquidacion1Txt.getValue() + "'";
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
        queryString += "," + cuentaContable1Cbx.getValue();
        queryString += ",'" + monedaCbx.getValue() + "'";
        queryString += "," + String.valueOf(debe1Txt.getDoubleValueDoNotThrow()); //DEBe
        queryString += "," + String.valueOf(haber1Txt.getDoubleValueDoNotThrow()); //DEBe
        queryString += "," + String.valueOf(debe1Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow()); //DEBE Q
        queryString += "," + String.valueOf(haber1Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow()); //HABER Q
        queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
        queryString += "," + liquidadorId;
        queryString += ",'" + descripcion + "'";
        queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
        queryString += ",current_timestamp";
        queryString += ")";

        System.out.println("primer insert pago factura = " + queryString);

        if (cuentaContable2Cbx.getValue() != null && (debe2Txt.getDoubleValueDoNotThrow() > 0 || haber2Txt.getDoubleValueDoNotThrow() > 0)) {
            queryString += ",(";
            queryString += String.valueOf(empresaCbx.getValue());
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigoCCLiquidacion2Txt.getValue() + "'";
            queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
            queryString += ",'" + facturasPagadas + "'";//NODOCA
            queryString += ",'" + tipoDocumentoPagado + "'";//TIPODOCA
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorId;
            queryString += ",''"; // nit
            queryString += ",'" + proveedorNombre + "'";
            queryString += ",'" + nombreChequeTxt.getValue() + "'";
            queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow());
            queryString += ",''"; // serie documento
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += "," + cuentaContable2Cbx.getValue(); //idcuentacontable
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(debe2Txt.getDoubleValueDoNotThrow()); // DEBE
            queryString += "," + String.valueOf(haber2Txt.getDoubleValueDoNotThrow()); // HABER
            queryString += "," + String.valueOf(debe2Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow()); //DEBE Q
            queryString += "," + String.valueOf(haber2Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow()); //HABER Q
            queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + liquidadorId;
            queryString += ",'" + descripcion + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";

            System.out.println("segundo query pago factura = " + queryString);

        }

        if (cuentaContable3Cbx.getValue() != null && (debe3Txt.getDoubleValueDoNotThrow() > 0 || haber3Txt.getDoubleValueDoNotThrow() > 0)) {
            queryString += ",(";
            queryString += String.valueOf(empresaCbx.getValue());
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigoCCLiquidacion3Txt.getValue() + "'";
            queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
            queryString += ",'" + facturasPagadas + "'";//NODOCA
            queryString += ",'" + tipoDocumentoPagado + "'";//TIPODOCA
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorId;
            queryString += ",''"; // nit
            queryString += ",'" + proveedorNombre + "'";
            queryString += ",'" + nombreChequeTxt.getValue() + "'";
            queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow());
            queryString += ",''";
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += "," + cuentaContable3Cbx.getValue(); //idcuentacontable
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(debe3Txt.getDoubleValueDoNotThrow()); // DEBE
            queryString += "," + String.valueOf(haber3Txt.getDoubleValueDoNotThrow()); // HABER
            queryString += "," + String.valueOf(debe3Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow()); //DEBE Q
            queryString += "," + String.valueOf(haber3Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow()); //HABER Q
            queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + liquidadorId;
            queryString += ",'" + descripcion + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";

            System.out.println("tercer query pago factura = " + queryString);

        }

        if (cuentaContable4Cbx.getValue() != null && (debe4Txt.getDoubleValueDoNotThrow() > 0 || haber4Txt.getDoubleValueDoNotThrow() > 0)) {
            queryString += ",(";
            queryString += String.valueOf(empresaCbx.getValue());
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigoCCLiquidacion4Txt.getValue() + "'";
            queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
            queryString += ",'" + facturasPagadas + "'";//NODOCA
            queryString += ",'" + tipoDocumentoPagado + "'";//TIPODOCA
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorId;
            queryString += ",''"; // nit
            queryString += ",'" + proveedorNombre + "'";
            queryString += ",'" + nombreChequeTxt.getValue() + "'";
            queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow());
            queryString += ",''";
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += "," + cuentaContable4Cbx.getValue(); //idcuentacontable
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(debe4Txt.getDoubleValueDoNotThrow()); // DEBE
            queryString += "," + String.valueOf(haber4Txt.getDoubleValueDoNotThrow()); // HABER
            queryString += "," + String.valueOf(debe4Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow()); //DEBE Q
            queryString += "," + String.valueOf(haber4Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow()); //HABER Q
            queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + liquidadorId;
            queryString += ",'" + descripcion + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";

            System.out.println("cuarto query pago factura = " + queryString);

        }

        if (cuentaContable5Cbx.getValue() != null && (debe5Txt.getDoubleValueDoNotThrow() > 0 || haber5Txt.getDoubleValueDoNotThrow() > 0)) {
            queryString += ",(";
            queryString += String.valueOf(empresaCbx.getValue());
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigoCCLiquidacion5Txt.getValue() + "'";
            queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
            queryString += ",'" + facturasPagadas + "'";//NODOCA
            queryString += ",'" + tipoDocumentoPagado + "'";//TIPODOCA
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorId;
            queryString += ",''"; // nit
            queryString += ",'" + proveedorNombre + "'";
            queryString += ",'" + nombreChequeTxt.getValue() + "'";
            queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow());
            queryString += ",''";
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += "," + cuentaContable5Cbx.getValue(); //idcuentacontable
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(debe5Txt.getDoubleValueDoNotThrow()); // DEBE
            queryString += "," + String.valueOf(haber5Txt.getDoubleValueDoNotThrow()); // HABER
            queryString += "," + String.valueOf(debe5Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow()); //DEBE Q
            queryString += "," + String.valueOf(haber5Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow()); //HABER Q
            queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + liquidadorId;
            queryString += ",'" + descripcion + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";

            System.out.println("quinto query pago factura = " + queryString);

        }

        if (cuentaContable6Cbx.getValue() != null && (debe6Txt.getDoubleValueDoNotThrow() > 0 || haber6Txt.getDoubleValueDoNotThrow() > 0)) {
            queryString += ",(";
            queryString += String.valueOf(empresaCbx.getValue());
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigoCCLiquidacion6Txt.getValue() + "'";
            queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
            queryString += ",'" + facturasPagadas + "'";//NODOCA
            queryString += ",'" + tipoDocumentoPagado + "'";//TIPODOCA
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorId;
            queryString += ",''"; // nit
            queryString += ",'" + proveedorNombre + "'";
            queryString += ",'" + nombreChequeTxt.getValue() + "'";
            queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow());
            queryString += ",''";
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += "," + cuentaContable6Cbx.getValue(); //idcuentacontable
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(debe6Txt.getDoubleValueDoNotThrow()); // DEBE
            queryString += "," + String.valueOf(haber6Txt.getDoubleValueDoNotThrow()); // HABER
            queryString += "," + String.valueOf(debe6Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow()); //DEBE Q
            queryString += "," + String.valueOf(haber6Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow()); //HABER Q
            queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + liquidadorId;
            queryString += ",'" + descripcion + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";

            System.out.println("sexto query pago factura = " + queryString);

        }

        if (cuentaContable7Cbx.getValue() != null && (debe7Txt.getDoubleValueDoNotThrow() > 0 || haber7Txt.getDoubleValueDoNotThrow() > 0)) {
            queryString += ",(";
            queryString += String.valueOf(empresaCbx.getValue());
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigoCCLiquidacion7Txt.getValue() + "'";
            queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
            queryString += ",'" + facturasPagadas + "'";//NODOCA
            queryString += ",'" + tipoDocumentoPagado + "'";//TIPODOCA
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorId;
            queryString += ",''"; // nit
            queryString += ",'" + proveedorNombre + "'";
            queryString += ",'" + nombreChequeTxt.getValue() + "'";
            queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow());
            queryString += ",''";
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += "," + cuentaContable7Cbx.getValue(); //idcuentacontable
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(debe7Txt.getDoubleValueDoNotThrow()); // DEBE
            queryString += "," + String.valueOf(haber7Txt.getDoubleValueDoNotThrow()); // HABER
            queryString += "," + String.valueOf(debe7Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow()); //DEBE Q
            queryString += "," + String.valueOf(haber7Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow()); //HABER Q
            queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + liquidadorId;
            queryString += ",'" + descripcion + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";

            System.out.println("Septimo query pago factura = " + queryString);

        }

        try {

            ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().setAutoCommit(false);

            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            Iterator iter;

            iter = liquidacionesGrid.getSelectedRows().iterator();

            String serieDocumento = "";
            String numeroDocumento = "";
            Double montoPagar = 0.00;
            Double montoAnticipo = 0.00;
            Double saldo = 0.00;
            String idLiquidacion = "";
            String codigoPartidaDoca;
            String codigoCC;
            String queryString3 = "";

            while (iter.hasNext()) {

                Object gridItem = iter.next();

                //liquidaciones por pagar
                idLiquidacion = String.valueOf(liquidacionesGrid.getContainerDataSource().getItem(gridItem).getItemProperty(LIQUIDACION_PROPERTY).getValue());
                codigoPartidaDoca = String.valueOf(liquidacionesGrid.getContainerDataSource().getItem(gridItem).getItemProperty(ID_PROPERTY).getValue());
                montoPagar = Double.valueOf(String.valueOf(liquidacionesGrid.getContainerDataSource().getItem(gridItem).getItemProperty(VALOR_PROPERTY).getValue()).replaceAll(",", ""));
                codigoCC = String.valueOf(liquidacionesGrid.getContainerDataSource().getItem(gridItem).getItemProperty(CODIGOCC_PROPERTY).getValue());

                //liquidaciones por pagar
                queryString = " Update contabilidad_partida Set ";
                queryString += " MontoAutorizadoPagar = 0.00";
                queryString += " ,MontoAplicarAnticipo = 0.00";
                queryString += " ,Estatus  = 'PAGADO', Saldo = 0.00 ";
                queryString += " ,Referencia = '" + codigoPartida + "'";
                queryString += " ,TipoDoca = '" + medioCbx.getValue() + "'";
                queryString += " ,NoDoca = '" + numeroTxt.getValue() + "'"; //del cheque
                queryString += " Where IdEmpresa   = " + String.valueOf(empresaCbx.getValue());
                queryString += " And IdLiquidacion = " + idLiquidacion;

Logger.getLogger(this.getClass().getName()).info("QueryUpdateLiquidacion=" + queryString);

                stQuery.executeUpdate(queryString);

                queryString = " DELETE FROM autorizacion_pago";
                queryString += " WHERE CodigoCC = '" + codigoCC + "'";

                stQuery.executeUpdate(queryString);

                ((PagarView) (mainUI.getNavigator().getCurrentView())).documentosContainer.removeItem(((PagarView) (mainUI.getNavigator().getCurrentView())).documentosGrid.getSelectedRow());

//                for (Iterator iTerator = ((PagarView) (mainUI.getNavigator().getCurrentView())).documentosContainer.getItemIds().iterator(); iTerator.hasNext();) {
//                    // Get the current item identifier, which is an integer.
//                    Object itemId = iTerator.next();
//
//                    if(idLiquidacion.equals(String.valueOf(((PagarView) (mainUI.getNavigator().getCurrentView())).documentosContainer.getContainerProperty(itemId, PagarView.ID_AUTO_PROPERTY).getValue()))) {
//                        ((PagarView) (mainUI.getNavigator().getCurrentView())).documentosContainer.getContainerProperty(itemId, PagarView.ESTATUS_PROPERTY).setValue("PAGADO");
//                    }
//                }

            }//end while

            ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().commit();
            ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().setAutoCommit(true);

            Notification notif = new Notification("REGISTRO AGREGADO EXITOSAMENTE.",
                    Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.CHECK);
            notif.show(Page.getCurrent());

            iter = liquidacionesGrid.getSelectedRows().iterator();

            while (iter.hasNext()) {

                Object gridItem = iter.next();

                liquidacionesGrid.getContainerDataSource().removeItem(gridItem);

            }

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

//            if (tabId.equals("2")) { //liquidaciones por pagar
//                llenarGridLiquidaciones(empresa);
//            }
//            else {
//                llenarGridFacturas(empresa);
//            }
            liquidacionesGrid.getSelectedRows().clear();
            liquidacionesGrid.getSelectionModel().reset();

            limpiarPartida();

            ((PagarView) (mainUI.getNavigator().getCurrentView())).documentosContainer.removeItem(((PagarView) (mainUI.getNavigator().getCurrentView())).documentosGrid.getSelectedRow());

//            ((PagarView) (mainUI.getNavigator().getCurrentView())).documentosContainer.getContainerProperty(((PagarView) (mainUI.getNavigator().getCurrentView())).documentosGrid.getSelectedRow(), PagarView.ESTATUS_PROPERTY).setValue("PAGADO");

        } catch (Exception ex1) {

            Notification notif = new Notification("HA OCURRIDO UN ERROR DE BASE DE DATOS : " + ex1.getMessage(),
                    Notification.Type.ERROR_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.WARNING);
            notif.show(Page.getCurrent());


            try {
                ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().rollback();
                ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().setAutoCommit(true);
            } catch (SQLException ex) {
                Logger.getLogger(PagoLiquidacionForm.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                String emailsTo[] = {"alerta@simpletecno.com"};
                MyEmailMessanger eMail = new MyEmailMessanger();

                eMail.postMail(emailsTo, "Error en SOPDI", "Error en base de datos :  " + this.getClass().getName() + " -->" + ex1.getMessage());
            } catch (MessagingException ex2) {
                Logger.getLogger(PagoLiquidacionForm.class.getName()).log(Level.SEVERE, null, ex2);
            }

            System.out.println("Error al insertar transacción  : " + ex1.getMessage());
            ex1.printStackTrace();
        }

    }

    public void limpiarPartida() {

//        proveedorCbx.setReadOnly(false);
//        proveedorCbx.clear();
        numeroTxt.setReadOnly(false);
        numeroTxt.setValue("");
        nombreChequeTxt.setReadOnly(false);
        nombreChequeTxt.setValue("");
        nombreChequeTxt.setReadOnly(((SopdiUI) mainUI).sessionInformation.getStrUserProfileName().equals("CONTADOR"));
        nombreChequeTxt.setReadOnly(((SopdiUI) mainUI).sessionInformation.getStrUserProfileName().equals("AUXILIAR"));
        montoTxt.setReadOnly(false);
        montoTxt.setValue(0.00);

        codigoCCLiquidacion1Txt.setValue("");
        cuentaContable1Cbx.setReadOnly(false);
        cuentaContable1Cbx.clear();
        haber1Txt.setReadOnly(false);
        debe1Txt.setReadOnly(false);
        haber1Txt.setValue(0.00);
        debe1Txt.setValue(0.00);

        codigoCCLiquidacion2Txt.setValue("");
        cuentaContable2Cbx.setReadOnly(false);
        cuentaContable2Cbx.clear();
        debe2Txt.setReadOnly(false);
        debe2Txt.setValue(0.00);
        haber2Txt.setReadOnly(false);
        haber2Txt.setValue(0.00);

        codigoCCLiquidacion3Txt.setValue("");
        cuentaContable3Cbx.setReadOnly(false);
        cuentaContable3Cbx.clear();
        debe3Txt.setReadOnly(false);
        debe3Txt.setValue(0.00);
        haber3Txt.setReadOnly(false);
        haber3Txt.setValue(0.00);

        codigoCCLiquidacion4Txt.setValue("");
        cuentaContable4Cbx.setReadOnly(false);
        cuentaContable4Cbx.clear();
        debe4Txt.setReadOnly(false);
        haber4Txt.setReadOnly(false);
        debe4Txt.setValue(0.00);
        haber4Txt.setValue(0.00);

        codigoCCLiquidacion5Txt.setValue("");
        cuentaContable5Cbx.setReadOnly(false);
        cuentaContable5Cbx.clear();
        debe5Txt.setReadOnly(false);
        haber5Txt.setReadOnly(false);
        debe5Txt.setValue(0.00);
        haber5Txt.setValue(0.00);

        codigoCCLiquidacion6Txt.setValue("");
        cuentaContable6Cbx.setReadOnly(false);
        cuentaContable6Cbx.clear();
        debe6Txt.setReadOnly(false);
        haber6Txt.setReadOnly(false);
        debe6Txt.setValue(0.00);
        haber6Txt.setValue(0.00);

        codigoCCLiquidacion7Txt.setValue("");
        cuentaContable7Cbx.setReadOnly(false);
        cuentaContable7Cbx.clear();
        debe7Txt.setReadOnly(false);
        haber7Txt.setReadOnly(false);
        debe7Txt.setValue(0.00);
        haber7Txt.setValue(0.00);

        descripcionTxt.setValue("");
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

    /*private boolean cuentaRepetida() {
        if (cuentaContable1Cbx.getValue() != null) { // hay una cuenta seleccioada
            if (cuentaContable2Cbx.getValue() != null) { // hay una cuenta seleccioada
                if (cuentaContable2Cbx.getValue().equals(cuentaContable1Cbx.getValue())) {
                    Notification.show("No puede utilizar la misma cuenta para dos registros de la partida.", Notification.Type.ERROR_MESSAGE);
                    return true;
                }
                if (cuentaContable3Cbx.getValue() != null) {
                    if (cuentaContable1Cbx.getValue().equals(cuentaContable3Cbx.getValue())
                            || cuentaContable2Cbx.getValue().equals(cuentaContable3Cbx.getValue())) {
                        Notification.show("No puede utilizar la misma cuenta para dos registros de la partida.", Notification.Type.ERROR_MESSAGE);
                        return true;
                    }
                    if (cuentaContable4Cbx.getValue() != null) {
                        if (cuentaContable4Cbx.getValue().equals(cuentaContable1Cbx.getValue())
                                || cuentaContable4Cbx.getValue().equals(cuentaContable3Cbx.getValue())
                                || cuentaContable4Cbx.getValue().equals(cuentaContable2Cbx.getValue())) {
                            return true;
                        }
                    }
                }
                if (cuentaContable4Cbx.getValue() != null) {
                    if (cuentaContable4Cbx.getValue().equals(cuentaContable1Cbx.getValue())
                            || cuentaContable4Cbx.getValue().equals(cuentaContable3Cbx.getValue())
                            || cuentaContable4Cbx.getValue().equals(cuentaContable2Cbx.getValue())) {
                        return true;
                    }
                }
            }
        }
        if (cuentaContable2Cbx.getValue() != null) { // hay una cuenta seleccioada
            if (cuentaContable1Cbx.getValue() != null) { // hay una cuenta seleccioada
                if (cuentaContable2Cbx.getValue().equals(cuentaContable1Cbx.getValue())) {
                    Notification.show("No puede utilizar la misma cuenta para dos registros de la partida.", Notification.Type.ERROR_MESSAGE);
                    return true;
                }
            }
            if (cuentaContable3Cbx.getValue() != null) { // hay una cuenta seleccioada
                if (cuentaContable2Cbx.getValue().equals(cuentaContable3Cbx.getValue())) {
                    Notification.show("No puede utilizar la misma cuenta para dos registros de la partida.", Notification.Type.ERROR_MESSAGE);
                    return true;
                }
            }
            if (cuentaContable4Cbx.getValue() != null) { // hay una cuenta seleccioada
                if (cuentaContable2Cbx.getValue().equals(cuentaContable4Cbx.getValue())) {
                    Notification.show("No puede utilizar la misma cuenta para dos registros de la partida.", Notification.Type.ERROR_MESSAGE);
                    return true;
                }
            }
        }
        if (cuentaContable3Cbx.getValue() != null) {
            if (cuentaContable1Cbx.getValue() != null) {
                if (cuentaContable3Cbx.getValue().equals(cuentaContable1Cbx.getValue())) {
                    Notification.show("No puede utilizar la misma cuenta para dos registros de la partida.", Notification.Type.ERROR_MESSAGE);
                    return true;
                }
            }
            if (cuentaContable2Cbx.getValue() != null) {
                if (cuentaContable2Cbx.getValue().equals(cuentaContable3Cbx.getValue())) {
                    Notification.show("No puede utilizar la misma cuenta para dos registros de la partida.", Notification.Type.ERROR_MESSAGE);
                    return true;
                }
            }
            if (cuentaContable4Cbx.getValue() != null) {
                if (cuentaContable4Cbx.getValue().equals(cuentaContable3Cbx.getValue())) {
                    Notification.show("No puede utilizar la misma cuenta para dos registros de la partida.", Notification.Type.ERROR_MESSAGE);
                    return true;
                }
            }
        }
        if (cuentaContable4Cbx.getValue() != null) {
            if (cuentaContable1Cbx.getValue() != null) {
                if (cuentaContable1Cbx.getValue().equals(cuentaContable4Cbx.getValue())) {
                    Notification.show("No puede utilizar la misma cuenta para dos registros de la partida.", Notification.Type.ERROR_MESSAGE);
                    return true;
                }
            }
            if (cuentaContable2Cbx.getValue() != null) { // hay una cuenta seleccioada
                if (cuentaContable2Cbx.getValue().equals(cuentaContable4Cbx.getValue())) {
                    Notification.show("No puede utilizar la misma cuenta para dos registros de la partida.", Notification.Type.ERROR_MESSAGE);
                    return true;
                }
            }
            if (cuentaContable3Cbx.getValue() != null) { // hay una cuenta seleccioada
                if (cuentaContable3Cbx.getValue().equals(cuentaContable4Cbx.getValue())) {
                    Notification.show("No puede utilizar la misma cuenta para dos registros de la partida.", Notification.Type.ERROR_MESSAGE);
                    return true;
                }
            }
        }
        return false;
    }*/
}