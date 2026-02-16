package com.simpletecno.sopdi.tesoreria;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.simpletecno.sopdi.utilerias.ValidarTokenForm;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.SelectionEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.ui.NumberField;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PagoDocumentoVentaForm extends Window {

    static final String NIT_PROPERTY = "NIT";
    static final String NOMBRESINCODIGO_PROPERTY = "NSC";

    VerticalLayout mainLayout;
    UI mainUI;

    HorizontalLayout layoutTitle;
    ComboBox empresaCbx;
    Label titleLbl;

    BigDecimal totalDebe;
    BigDecimal totalHaber;

    Button guardarBtn;
    Button salirBtn;

    DateField fechaDt;
    TextField numeroTxt;
    TextField descripcionTxt;
    ComboBox tipoIngresoCbx;
    ComboBox proveedorCbx;
    ComboBox medioCbx;
    ComboBox monedaCbx;
    NumberField montoTxt;
    NumberField tipoCambioTxt;

    ArrayList<String> codigoEnganches = new ArrayList<String>();
    TextField codigo1Txt;
    TextField codigo2Txt;
    TextField codigo3Txt;
    TextField codigo4Txt;
    TextField codigo5Txt;
    TextField codigo6Txt;
    TextField codigo7Txt;
    TextField codigo8Txt;
    TextField codigo9Txt;
    TextField codigo10Txt;

    ComboBox cuentaContable1Cbx;
    ComboBox cuentaContable2Cbx;
    ComboBox cuentaContable3Cbx;
    ComboBox cuentaContable4Cbx;
    ComboBox cuentaContable5Cbx;
    ComboBox cuentaContable6Cbx;
    ComboBox cuentaContable7Cbx;
    ComboBox cuentaContable8Cbx;
    ComboBox cuentaContable9Cbx;
    ComboBox cuentaContable10Cbx;

    NumberField haber1Txt;
    NumberField haber2Txt;
    NumberField haber3Txt;
    NumberField haber4Txt;
    NumberField haber5Txt;
    NumberField haber6Txt;
    NumberField haber7Txt;
    NumberField haber8Txt;
    NumberField haber9Txt;
    NumberField haber10Txt;

    NumberField debe1Txt;
    NumberField debe2Txt;
    NumberField debe3Txt;
    NumberField debe4Txt;
    NumberField debe5Txt;
    NumberField debe6Txt;
    NumberField debe7Txt;
    NumberField debe8Txt;
    NumberField debe9Txt;
    NumberField debe10Txt;


    public IndexedContainer facturasContainer = new IndexedContainer();
    Grid facturasGrid;
    static final String CODIGO_PARTIDA_PROPERTY = "Codigo Partida";
    static final String CODIGO_CC_PROPERTY = "Codigo CC";
    static final String FECHA_DOCUMENTO_PROPERTY = "Fecha";
    static final String NUMERO_DOCUMENTO_PROPERTY = "# Documento";
    static final String MONEDA_DOCUMENTO_PROPERTY = "Moneda";
    static final String MONTO_DOCUMENTO_PROPERTY = "Monto";
    static final String MONTO_PAGADO_PROPERTY = "Monto Pagado";

    Statement stQuery, stQuery2, stQuery3;
    ResultSet rsRecords, rsRecords2, rsRecords3;
    String queryString, codigoPartida;
    String variableTemp = "";

    public PagoDocumentoVentaForm() {

        this.mainUI = UI.getCurrent();
        setResponsive(true);
        setWidth("87%");
        setHeight("85%");
        setModal(true);

        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setResponsive(true);

        layoutTitle = new HorizontalLayout();
        layoutTitle.setSpacing(true);
        layoutTitle.setMargin(true);
        layoutTitle.setWidth("100%");

        empresaCbx = new ComboBox("EMPRESA :");
        empresaCbx.setStyleName(ValoTheme.COMBOBOX_HUGE);
        empresaCbx.setWidth("90%");
        empresaCbx.setInvalidAllowed(false);
        empresaCbx.setNewItemsAllowed(false);
        empresaCbx.setTextInputAllowed(false);
        empresaCbx.setNullSelectionAllowed(false);
        llenarComboEmpresa();

        titleLbl = new Label("INGRESO A BANCOS");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h2_custom");

        layoutTitle.addComponent(empresaCbx);
        layoutTitle.setComponentAlignment(empresaCbx, Alignment.MIDDLE_LEFT);

        layoutTitle.addComponent(titleLbl);
        layoutTitle.setComponentAlignment(titleLbl, Alignment.BOTTOM_RIGHT);

        mainLayout.addComponent(layoutTitle);

        setContent(mainLayout);

        mainLayout.addComponent(crearComponentes());

    }

    public void llenarComboEmpresa() {

        queryString = " SELECT * from contabilidad_empresa";
        queryString += " Where IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();

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
            System.out.println("Error al llenar combo empresas: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    /*
    public void llenarProveedor() {
        String prov = "";
        try {

            queryString = " SELECT prov.* ";
            queryString += " FROM proveedor prov";
            queryString += " WHERE prov.Inhabilitado = 0 ";
            queryString += " AND prov.EsCliente = 1";
            queryString += " Order By prov.Nombre";

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery2 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery3 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                do {

                    queryString = " SELECT * from contabilidad_partida ";
                    queryString += " WHERE IdNomenclatura = 6";
                    queryString += " And TipoDocumento = 'FACTURA VENTA' ";
                    queryString += " And IdProveedor = " + rsRecords.getString("IdProveedor");
                    queryString += " And IdEmpresa = " + empresaCbx.getValue();
                    
                    rsRecords2 = stQuery2.executeQuery(queryString);

                    if (rsRecords2.next()) {
                        do {

                            queryString = " SELECT  ";
                            queryString += " SUM(DEBE - HABER) as TOTALSALDO, SUM(DebeQuetzales - HaberQuetzales) as TOTALSALDOQ ";
                            queryString += " FROM contabilidad_partida";
                            queryString += " WHERE CodigoCC = '" + rsRecords2.getString("CodigoCC") + "'";
                            queryString += " AND IdNomenclatura = 6";
                            queryString += " GROUP BY CodigoCC, IdNomenclatura";
                            queryString += " HAVING TOTALSALDO > 0";
                            
                            rsRecords3 = stQuery3.executeQuery(queryString);

                            if (rsRecords3.next()) {
                                if (!prov.equals(rsRecords.getString("IdProveedor"))) {
                                    proveedorCbx.addItem(rsRecords.getString("IDProveedor"));
                                    proveedorCbx.setItemCaption(rsRecords.getString("IDProveedor"), rsRecords.getString("Nombre"));
                                    proveedorCbx.getItem(rsRecords.getString("IDProveedor")).getItemProperty(NIT_PROPERTY).setValue(rsRecords.getString("NIT"));
                                }
                            }
                            prov = rsRecords.getString("IdProveedor");
                        } while (rsRecords2.next());
                    }

                } while (rsRecords.next());

            }
        } catch (Exception e) {
            System.out.println("Error en busqueda proveedores con saldo " + e);
            e.printStackTrace();
        }
    }
*/
    public void llenarComboProveedor() {
        queryString = " SELECT prov.* ";
        queryString += " FROM proveedor prov";
        queryString += " WHERE prov.Inhabilitado = 0 ";
        queryString += " AND N0 in (1, 4, 7, 9)";
        queryString += " AND prov.EsCliente = 1";
        queryString += " Order By prov.Nombre";

        proveedorCbx.removeAllItems();

        try {
            stQuery2 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords2 = stQuery2.executeQuery(queryString);

            while (rsRecords2.next()) {  /// encontrado
                proveedorCbx.addItem(rsRecords2.getString("IDProveedor"));
                proveedorCbx.setItemCaption(rsRecords2.getString("IDProveedor"), rsRecords2.getString("Nombre"));
                proveedorCbx.getItem(rsRecords2.getString("IDProveedor")).getItemProperty(NIT_PROPERTY).setValue(rsRecords2.getString("NIT"));
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar de clientes (tabla proveedores) " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void llenarComboCuentaContable() {

        queryString = " SELECT * from contabilidad_nomenclatura";
        queryString += " where Estatus = 'HABILITADA'";
        queryString += " Order By N5";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) {  // encontrado

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

                cuentaContable8Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable8Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable9Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable9Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable10Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable10Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));
            }

        } catch (Exception ex1) {
            System.out.println("Error al combo cuentas contables: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public HorizontalLayout crearComponentes() {

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.addStyleName("rcorners3");
        horizontalLayout.setSpacing(true);
        horizontalLayout.setMargin(true);
        horizontalLayout.setWidth("100%");

        VerticalLayout leftVerticalLayout = new VerticalLayout();
        leftVerticalLayout.setWidth("100%");
        leftVerticalLayout.setSpacing(true);
        leftVerticalLayout.setMargin(true);

        VerticalLayout rightVerticalLayout = new VerticalLayout();
        rightVerticalLayout.setWidth("100%");
        rightVerticalLayout.setSpacing(true);
        rightVerticalLayout.setMargin(true);

        horizontalLayout.addComponents(leftVerticalLayout, rightVerticalLayout);
        horizontalLayout.setComponentAlignment(leftVerticalLayout, Alignment.MIDDLE_LEFT);
        horizontalLayout.setComponentAlignment(leftVerticalLayout, Alignment.MIDDLE_RIGHT);

        tipoIngresoCbx = new ComboBox("Tipo de Ingreso :");
        tipoIngresoCbx.setWidth("100%");
        tipoIngresoCbx.setFilteringMode(FilteringMode.CONTAINS);
        tipoIngresoCbx.setInvalidAllowed(false);
        tipoIngresoCbx.setNewItemsAllowed(false);
        tipoIngresoCbx.setNullSelectionAllowed(false);
        tipoIngresoCbx.addItem("PAGO DE DOCUMENTO VENTA");
        tipoIngresoCbx.select("PAGO DE DOCUMENTO VENTA");
        tipoIngresoCbx.setReadOnly(true);

        proveedorCbx = new ComboBox("Cliente");
        proveedorCbx.addContainerProperty(NIT_PROPERTY, String.class, "");
        proveedorCbx.setInvalidAllowed(false);
        proveedorCbx.setNewItemsAllowed(false);
        proveedorCbx.setNullSelectionAllowed(false);
        proveedorCbx.setWidth("100%");
        proveedorCbx.setFilteringMode(FilteringMode.CONTAINS);
        proveedorCbx.addValueChangeListener((event) -> {
            if (tipoIngresoCbx.getValue().equals("PAGO DE DOCUMENTO VENTA")) {
                llenarTablaDocumentosPagar();
            }
        });

        medioCbx = new ComboBox("Medio : ");
        medioCbx.setWidth("100%");
        medioCbx.addItem("DEPOSITO");
        medioCbx.addItem("NOTA DE CREDITO");
        medioCbx.setInvalidAllowed(false);
        medioCbx.setNewItemsAllowed(false);
        medioCbx.setNullSelectionAllowed(false);
        medioCbx.select("DEPOSITO");

        fechaDt = new DateField("Fecha : ");
        fechaDt.setDateFormat("dd/MM/yyyy");
        fechaDt.setWidth("100%");
        fechaDt.setValue(new java.util.Date());


        numeroTxt = new TextField("# Depósito/Nota de crédito:");
        numeroTxt.setWidth("100%");

        montoTxt = new NumberField("Monto : ");
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
        montoTxt.setWidth("100%");
        montoTxt.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(Property.ValueChangeEvent event) {
                debe1Txt.setValue(montoTxt.getDoubleValueDoNotThrow());
            }
        });

        monedaCbx = new ComboBox("Moneda :");
        monedaCbx.setWidth("100%");
        monedaCbx.addItem("QUETZALES");
        monedaCbx.addItem("DOLARES");
        monedaCbx.select("QUETZALES");
        monedaCbx.setInvalidAllowed(false);
        monedaCbx.setNewItemsAllowed(false);
        monedaCbx.setNullSelectionAllowed(false);
        monedaCbx.setImmediate(true);
        monedaCbx.addValueChangeListener((event) -> {
//System.out.println("cambio a :" + monedaCbx.getValue() + " " +  Float.toString(((SopdiUI) UI.getCurrent()).sessionInformation.getFltExchangeRate()) + " cuentaconbble1=" + cuentaContable1Cbx.getValue());
            if (cuentaContable1Cbx != null) {
                if (monedaCbx.getValue().equals("DOLARES")) {
                    tipoCambioTxt.setReadOnly(false);
                    tipoCambioTxt.setValue(Float.toString(((SopdiUI) UI.getCurrent()).sessionInformation.getFltExchangeRate()));
                    cuentaContable1Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaExtranjera());
                } else {
                    cuentaContable1Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaLocal());
                    tipoCambioTxt.setReadOnly(false);
                    tipoCambioTxt.setValue(1.00);
                    tipoCambioTxt.setReadOnly(true);
                }
            }
        });

        tipoCambioTxt = new NumberField("Tipo de Cambio :");
        tipoCambioTxt.setDecimalAllowed(true);
        tipoCambioTxt.setDecimalPrecision(5);
        tipoCambioTxt.setMinimumFractionDigits(5);
        tipoCambioTxt.setDecimalSeparator('.');
        tipoCambioTxt.setDecimalSeparatorAlwaysShown(true);
        tipoCambioTxt.setImmediate(true);
        tipoCambioTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        tipoCambioTxt.setWidth("100%");
        tipoCambioTxt.setValue(1.00);
        tipoCambioTxt.setReadOnly(true);

        descripcionTxt = new TextField("Descripción :");
        descripcionTxt.setWidth("100%");
        descripcionTxt.setVisible(false);

        leftVerticalLayout.addComponent(tipoIngresoCbx);
        leftVerticalLayout.addComponent(proveedorCbx);
        leftVerticalLayout.addComponent(medioCbx);
        leftVerticalLayout.addComponent(fechaDt);
        leftVerticalLayout.addComponent(numeroTxt);
        leftVerticalLayout.addComponent(descripcionTxt);
        leftVerticalLayout.addComponent(montoTxt);
        leftVerticalLayout.addComponent(monedaCbx);
        leftVerticalLayout.addComponent(tipoCambioTxt);

        HorizontalLayout layoutHorizontal1 = new HorizontalLayout();
        layoutHorizontal1.setResponsive(true);
        layoutHorizontal1.setSpacing(true);

        HorizontalLayout layoutHorizontal2 = new HorizontalLayout();
        layoutHorizontal2.setResponsive(true);
        layoutHorizontal2.setSpacing(true);

        HorizontalLayout layoutHorizontal3 = new HorizontalLayout();
        layoutHorizontal3.setResponsive(true);
        layoutHorizontal3.setSpacing(true);

        HorizontalLayout layoutHorizontal4 = new HorizontalLayout();
        layoutHorizontal4.setResponsive(true);
        layoutHorizontal4.setSpacing(true);

        HorizontalLayout layoutHorizontal5 = new HorizontalLayout();
        layoutHorizontal5.setResponsive(true);
        layoutHorizontal5.setSpacing(true);

        HorizontalLayout layoutHorizontal6 = new HorizontalLayout();
        layoutHorizontal6.setResponsive(true);
        layoutHorizontal6.setSpacing(true);

        HorizontalLayout layoutHorizontal7 = new HorizontalLayout();
        layoutHorizontal7.setResponsive(true);
        layoutHorizontal7.setSpacing(true);

        HorizontalLayout layoutHorizontal8 = new HorizontalLayout();
        layoutHorizontal8.setResponsive(true);
        layoutHorizontal8.setSpacing(true);

        HorizontalLayout layoutHorizontal9 = new HorizontalLayout();
        layoutHorizontal9.setResponsive(true);
        layoutHorizontal9.setSpacing(true);

        HorizontalLayout layoutHorizontal10 = new HorizontalLayout();
        layoutHorizontal10.setResponsive(true);
        layoutHorizontal10.setSpacing(true);

        HorizontalLayout layoutHorizontal11 = new HorizontalLayout();
        layoutHorizontal11.setResponsive(true);
        layoutHorizontal11.setSpacing(true);

        facturasContainer.addContainerProperty(CODIGO_PARTIDA_PROPERTY, String.class, "");
        facturasContainer.addContainerProperty(CODIGO_CC_PROPERTY, String.class, "");
        facturasContainer.addContainerProperty(NUMERO_DOCUMENTO_PROPERTY, String.class, "");
        facturasContainer.addContainerProperty(FECHA_DOCUMENTO_PROPERTY, String.class, "");
        facturasContainer.addContainerProperty(MONEDA_DOCUMENTO_PROPERTY, String.class, "");
        facturasContainer.addContainerProperty(MONTO_DOCUMENTO_PROPERTY, String.class, "");
        facturasContainer.addContainerProperty(MONTO_PAGADO_PROPERTY, String.class, "");

        facturasGrid = new Grid("", facturasContainer);
        facturasGrid.setWidth("100%");
        facturasGrid.setImmediate(true);
        facturasGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        facturasGrid.setDescription("Seleccione uno o varios registros del mismo proveedor.");
        facturasGrid.setHeightMode(HeightMode.ROW);
        facturasGrid.setHeightByRows(5);
        facturasGrid.setResponsive(true);
        facturasGrid.setEditorBuffered(false);
        facturasGrid.addSelectionListener(new SelectionEvent.SelectionListener() {
            @Override
            public void select(SelectionEvent event) {
                if (facturasGrid.getSelectedRows() != null) {

                    Object gridItem;
                    String moneda = "";
                    double montoDisponible = montoTxt.getDoubleValueDoNotThrow();
                    codigoEnganches.clear();

                    Iterator iter = event.getSelected().iterator();

                    if (iter == null) {
                        limpiarPartida();
                        return;

                    }
                    if (!iter.hasNext()) {
                        limpiarPartida();
                        return;
                    }

                    gridItem = iter.next();
                    moneda = String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(MONEDA_DOCUMENTO_PROPERTY).getValue());

                    limpiarPartida();

                    while (iter.hasNext()) {

                        if (!moneda.equals(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(MONEDA_DOCUMENTO_PROPERTY).getValue()))) {
                            Notification.show("SOLO SE PUEDEN PAGAR VARIAS FACTURAS DEL MISMO PROVEEDOR Y DE LA MISMA MONEDA, REVISE!", Notification.Type.WARNING_MESSAGE);
                            facturasGrid.deselect(gridItem);
                            return;
                        }
                        gridItem = iter.next();

                    }
                    limpiarPartida();

                    if (monedaCbx.getValue() == "DOLARES") {
                        cuentaContable1Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaExtranjera());
                    } else {
                        cuentaContable1Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaLocal());
                    }

                    Iterator iter2 = facturasGrid.getSelectedRows().iterator();

                    while (iter2.hasNext()) {

                        Object gridItem2 = iter2.next();

                        if (cuentaContable2Cbx.getValue() == null) {
                            if(montoDisponible <= 0) {
                                Notification.show("Monto del pago no cubre el (los) saldo(s) del (los) documento(s) seleccionado(s)!!", Notification.Type.WARNING_MESSAGE);
                                return;
                            }
                            cuentaContable2Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getClientes());
                            if(montoDisponible <= Double.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", ""))) {
                                haber2Txt.setValue(montoDisponible);
                                facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_PAGADO_PROPERTY).setValue(String.valueOf(montoDisponible));
                            }
                            else {
                                haber2Txt.setValue(Double.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", "")));
                                facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_PAGADO_PROPERTY).setValue(String.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", "")));
                            }
                            montoDisponible -= Double.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", ""));
                            codigo2Txt.setValue(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(CODIGO_CC_PROPERTY).getValue()));
                        } else if (cuentaContable3Cbx.getValue() == null) {
                            if(montoDisponible <= 0) {
                                Notification.show("Monto del pago no cubre el (los) saldo(s) del (los) documento(s) seleccionado(s)!!", Notification.Type.WARNING_MESSAGE);
                                return;
                            }
                            cuentaContable3Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getClientes());
                            if(montoDisponible <= Double.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", ""))) {
                                haber3Txt.setValue(montoDisponible);
                                facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_PAGADO_PROPERTY).setValue(String.valueOf(montoDisponible));
                            }
                            else {
                                haber3Txt.setValue(Double.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", "")));
                                facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_PAGADO_PROPERTY).setValue(String.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", "")));
                            }
                            montoDisponible -= Double.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", ""));
                            codigo3Txt.setValue(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(CODIGO_CC_PROPERTY).getValue()));
                        } else if (cuentaContable4Cbx.getValue() == null) {
                            if(montoDisponible <= 0) {
                                Notification.show("Monto del pago no cubre el (los) saldo(s) del (los) documento(s) seleccionado(s)!!", Notification.Type.WARNING_MESSAGE);
                                return;
                            }
                            cuentaContable4Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getClientes());
                            if(montoDisponible <= Double.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", ""))) {
                                haber4Txt.setValue(montoDisponible);
                                facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_PAGADO_PROPERTY).setValue(String.valueOf(montoDisponible));
                            }
                            else {
                                haber4Txt.setValue(Double.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", "")));
                                facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_PAGADO_PROPERTY).setValue(String.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", "")));
                            }
                            montoDisponible -= Double.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", ""));
                            codigo4Txt.setValue(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(CODIGO_CC_PROPERTY).getValue()));
                        } else if (cuentaContable5Cbx.getValue() == null) {
                            if(montoDisponible <= 0) {
                                Notification.show("Monto del pago no cubre el (los) saldo(s) del (los) documento(s) seleccionado(s)!!", Notification.Type.WARNING_MESSAGE);
                                return;
                            }
                            cuentaContable5Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getClientes());
                            if(montoDisponible <= Double.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", ""))) {
                                haber4Txt.setValue(montoDisponible);
                                facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_PAGADO_PROPERTY).setValue(String.valueOf(montoDisponible));
                            }
                            else {
                                haber4Txt.setValue(Double.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", "")));
                                facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_PAGADO_PROPERTY).setValue(String.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", "")));
                            }
                            montoDisponible -= Double.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", ""));
                            codigo5Txt.setValue(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(CODIGO_CC_PROPERTY).getValue()));
                        } else if (cuentaContable6Cbx.getValue() == null) {
                            if(montoDisponible <= 0) {
                                Notification.show("Monto del pago no cubre el (los) saldo(s) del (los) documento(s) seleccionado(s)!!", Notification.Type.WARNING_MESSAGE);
                                return;
                            }
                            cuentaContable6Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getClientes());
//                            haber6Txt.setValue(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", ""));
                            if(montoDisponible <= Double.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", ""))) {
                                haber6Txt.setValue(String.valueOf(montoDisponible));
                                facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_PAGADO_PROPERTY).setValue(String.valueOf(montoDisponible));
                            }
                            else {
                                haber6Txt.setValue(Double.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", "")));
                                facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_PAGADO_PROPERTY).setValue(String.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", "")));
                            }
                            montoDisponible -= Double.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", ""));
                            codigo6Txt.setValue(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(CODIGO_CC_PROPERTY).getValue()));
                        } else if (cuentaContable7Cbx.getValue() == null) {
                            if(montoDisponible <= 0) {
                                Notification.show("Monto del pago no cubre el (los) saldo(s) del (los) documento(s) seleccionado(s)!!", Notification.Type.WARNING_MESSAGE);
                                return;
                            }
                            cuentaContable7Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getClientes());
//                            haber7Txt.setValue(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", ""));
                            if(montoDisponible <= Double.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", ""))) {
                                haber7Txt.setValue(montoDisponible);
                                facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_PAGADO_PROPERTY).setValue(String.valueOf(montoDisponible));
                            }
                            else {
                                haber7Txt.setValue(Double.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", "")));
                                facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_PAGADO_PROPERTY).setValue(String.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", "")));
                            }
                            montoDisponible -= Double.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", ""));
                            codigo7Txt.setValue(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(CODIGO_CC_PROPERTY).getValue()));
                        } else if (cuentaContable8Cbx.getValue() == null) {
                            if(montoDisponible <= 0) {
                                Notification.show("Monto del pago no cubre el (los) saldo(s) del (los) documento(s) seleccionado(s)!!", Notification.Type.WARNING_MESSAGE);
                                return;
                            }
                            cuentaContable8Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getClientes());
//                            haber8Txt.setValue(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", ""));
                            if(montoDisponible <= Double.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", ""))) {
                                haber8Txt.setValue(montoDisponible);
                                facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_PAGADO_PROPERTY).setValue(String.valueOf(montoDisponible));
                            }
                            else {
                                haber8Txt.setValue(Double.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", "")));
                                facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_PAGADO_PROPERTY).setValue(String.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", "")));
                            }
                            montoDisponible -= Double.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", ""));
                            codigo8Txt.setValue(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(CODIGO_CC_PROPERTY).getValue()));
                        } else if (cuentaContable9Cbx.getValue() == null) {
                            if(montoDisponible <= 0) {
                                Notification.show("Monto del pago no cubre el (los) saldo(s) del (los) documento(s) seleccionado(s)!!", Notification.Type.WARNING_MESSAGE);
                                return;
                            }
                            cuentaContable9Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getClientes());
//                            haber9Txt.setValue(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", ""));
                            if(montoDisponible <= Double.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", ""))) {
                                haber9Txt.setValue(montoDisponible);
                                facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_PAGADO_PROPERTY).setValue(String.valueOf(montoDisponible));
                            }
                            else {
                                haber9Txt.setValue(Double.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", "")));
                                facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_PAGADO_PROPERTY).setValue(String.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", "")));
                            }
                            montoDisponible -= Double.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", ""));
                            codigo9Txt.setValue(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(CODIGO_CC_PROPERTY).getValue()));
                        } else if (cuentaContable10Cbx.getValue() == null) {
                            if(montoDisponible <= 0) {
                                Notification.show("Monto del pago no cubre el (los) saldo(s) del (los) documento(s) seleccionado(s)!!", Notification.Type.WARNING_MESSAGE);
                                return;
                            }
                            cuentaContable10Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getClientes());
//                            haber10Txt.setValue(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", ""));
                            if(montoDisponible <= Double.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", ""))) {
                                haber10Txt.setValue(montoDisponible);
                                facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_PAGADO_PROPERTY).setValue(String.valueOf(montoDisponible));
                            }
                            else {
                                haber10Txt.setValue(Double.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", "")));
                                facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_PAGADO_PROPERTY).setValue(String.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", "")));
                            }
                            montoDisponible -= Double.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", ""));
                            codigo10Txt.setValue(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(CODIGO_CC_PROPERTY).getValue()));
                        }

                    }

//                    queryString = " select * from contabilidad_partida ";
//                    queryString += " where IdProveedor = " + proveedorCbx.getValue();
//                    queryString += " and IdNomenclatura = " + ((SopdiUI) mainUI).cuentasContablesDefault.getEnganches();
//                    queryString += " and IdEmpresa = " + empresaCbx.getValue();
//                    queryString += " and Saldo > 0.00 ";
//
//                    try {
//                        stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
//                        rsRecords = stQuery.executeQuery(queryString);
//
//                        if (rsRecords.next()) {
//                            do {
//                                if (cuentaContable3Cbx.getValue() == null) {
//                                    cuentaContable3Cbx.select(rsRecords.getString("IdNomenclatura"));
//                                    haber3Txt.setValue(rsRecords.getDouble("Debe"));
//                                    debe3Txt.setValue(rsRecords.getDouble("Haber"));
//                                    codigo3Txt.setValue(rsRecords.getString("CodigoCC"));
//                                    codigoEnganches.add(rsRecords.getString("CodigoCC"));
//                                } else if (cuentaContable4Cbx.getValue() == null) {
//                                    cuentaContable4Cbx.select(rsRecords.getString("IdNomenclatura"));
//                                    haber4Txt.setValue(rsRecords.getDouble("Debe"));
//                                    debe4Txt.setValue(rsRecords.getDouble("Haber"));
//                                    codigo4Txt.setValue(rsRecords.getString("CodigoCC"));
//                                    codigoEnganches.add(rsRecords.getString("CodigoCC"));
//                                } else if (cuentaContable5Cbx.getValue() == null) {
//                                    cuentaContable5Cbx.select(rsRecords.getString("IdNomenclatura"));
//                                    haber5Txt.setValue(rsRecords.getDouble("Debe"));
//                                    debe5Txt.setValue(rsRecords.getDouble("Haber"));
//                                    codigo5Txt.setValue(rsRecords.getString("CodigoCC"));
//                                    codigoEnganches.add(rsRecords.getString("CodigoCC"));
//                                } else if (cuentaContable6Cbx.getValue() == null) {
//                                    cuentaContable6Cbx.select(rsRecords.getString("IdNomenclatura"));
//                                    haber6Txt.setValue(rsRecords.getDouble("Debe"));
//                                    debe6Txt.setValue(rsRecords.getDouble("Haber"));
//                                    codigo6Txt.setValue(rsRecords.getString("CodigoCC"));
//                                    codigoEnganches.add(rsRecords.getString("CodigoCC"));
//                                } else if (cuentaContable7Cbx.getValue() == null) {
//                                    cuentaContable7Cbx.select(rsRecords.getString("IdNomenclatura"));
//                                    haber7Txt.setValue(rsRecords.getDouble("Debe"));
//                                    debe7Txt.setValue(rsRecords.getDouble("Haber"));
//                                    codigo7Txt.setValue(rsRecords.getString("CodigoCC"));
//                                    codigoEnganches.add(rsRecords.getString("CodigoCC"));
//                                } else if (cuentaContable8Cbx.getValue() == null) {
//                                    cuentaContable8Cbx.select(rsRecords.getString("IdNomenclatura"));
//                                    haber8Txt.setValue(rsRecords.getDouble("Debe"));
//                                    debe8Txt.setValue(rsRecords.getDouble("Haber"));
//                                    codigo8Txt.setValue(rsRecords.getString("CodigoCC"));
//                                    codigoEnganches.add(rsRecords.getString("CodigoCC"));
//                                } else if (cuentaContable9Cbx.getValue() == null) {
//                                    cuentaContable9Cbx.select(rsRecords.getString("IdNomenclatura"));
//                                    haber9Txt.setValue(rsRecords.getDouble("Debe"));
//                                    debe9Txt.setValue(rsRecords.getDouble("Haber"));
//                                    codigo9Txt.setValue(rsRecords.getString("CodigoCC"));
//                                    codigoEnganches.add(rsRecords.getString("CodigoCC"));
//                                } else if (cuentaContable10Cbx.getValue() == null) {
//                                    cuentaContable10Cbx.select(rsRecords.getString("IdNomenclatura"));
//                                    haber10Txt.setValue(rsRecords.getDouble("Debe"));
//                                    debe10Txt.setValue(rsRecords.getDouble("Haber"));
//                                    codigo10Txt.setValue(rsRecords.getString("CodigoCC"));
//                                    codigoEnganches.add(rsRecords.getString("CodigoCC"));
//                                }
//
//                            } while (rsRecords.next());
//                        }
//
//                    } catch (Exception ex1) {
//                        System.out.println("Error al listar engaches: " + ex1.getMessage());
//                        ex1.printStackTrace();
//                    }
                }
            }
        });

        facturasGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if (MONTO_DOCUMENTO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }
        });

        facturasGrid.getColumn(CODIGO_PARTIDA_PROPERTY).setHidable(true).setHidden(true);
        facturasGrid.getColumn(CODIGO_CC_PROPERTY).setHidable(true).setHidden(true);

        cuentaContable1Cbx = new ComboBox("Cuentas contables :");
        cuentaContable1Cbx.setWidth("24em");
        cuentaContable1Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable1Cbx.setInvalidAllowed(false);
        cuentaContable1Cbx.setNewItemsAllowed(false);
        cuentaContable1Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaLocal());

        cuentaContable2Cbx = new ComboBox();
        cuentaContable2Cbx.setWidth("24em");
        cuentaContable2Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable2Cbx.setInvalidAllowed(false);
        cuentaContable2Cbx.setNewItemsAllowed(false);

        cuentaContable3Cbx = new ComboBox();
        cuentaContable3Cbx.setWidth("24em");
        cuentaContable3Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable3Cbx.setInvalidAllowed(false);
        cuentaContable3Cbx.setNewItemsAllowed(false);

        cuentaContable4Cbx = new ComboBox();
        cuentaContable4Cbx.setWidth("24em");
        cuentaContable4Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable4Cbx.setInvalidAllowed(false);
        cuentaContable4Cbx.setNewItemsAllowed(false);

        cuentaContable5Cbx = new ComboBox();
        cuentaContable5Cbx.setWidth("24em");
        cuentaContable5Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable5Cbx.setInvalidAllowed(false);
        cuentaContable5Cbx.setNewItemsAllowed(false);

        cuentaContable6Cbx = new ComboBox();
        cuentaContable6Cbx.setWidth("24em");
        cuentaContable6Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable6Cbx.setInvalidAllowed(false);
        cuentaContable6Cbx.setNewItemsAllowed(false);

        cuentaContable7Cbx = new ComboBox();
        cuentaContable7Cbx.setWidth("24em");
        cuentaContable7Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable7Cbx.setInvalidAllowed(false);
        cuentaContable7Cbx.setNewItemsAllowed(false);

        cuentaContable8Cbx = new ComboBox();
        cuentaContable8Cbx.setWidth("24em");
        cuentaContable8Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable8Cbx.setInvalidAllowed(false);
        cuentaContable8Cbx.setNewItemsAllowed(false);

        cuentaContable9Cbx = new ComboBox();
        cuentaContable9Cbx.setWidth("24em");
        cuentaContable9Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable9Cbx.setInvalidAllowed(false);
        cuentaContable9Cbx.setNewItemsAllowed(false);

        cuentaContable10Cbx = new ComboBox();
        cuentaContable10Cbx.setWidth("24em");
        cuentaContable10Cbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContable10Cbx.setInvalidAllowed(false);
        cuentaContable10Cbx.setNewItemsAllowed(false);

        llenarComboCuentaContable();
        llenarComboProveedor();
        //llenarProveedor(); // proveedores

        codigo1Txt = new TextField();
        codigo1Txt.setVisible(false);

        codigo2Txt = new TextField();
        codigo2Txt.setVisible(false);

        codigo3Txt = new TextField();
        codigo3Txt.setVisible(false);

        codigo4Txt = new TextField();
        codigo4Txt.setVisible(false);

        codigo5Txt = new TextField();
        codigo5Txt.setVisible(false);

        codigo6Txt = new TextField();
        codigo6Txt.setVisible(false);

        codigo7Txt = new TextField();
        codigo7Txt.setVisible(false);

        codigo8Txt = new TextField();
        codigo8Txt.setVisible(false);

        codigo9Txt = new TextField();
        codigo9Txt.setVisible(false);

        codigo10Txt = new TextField();
        codigo10Txt.setVisible(false);

        haber1Txt = new NumberField("HABER : ");
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
        haber1Txt.setWidth("7em");
        haber1Txt.setValue(0.00);
        haber1Txt.setEnabled(false);

        haber2Txt = new NumberField();
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
        haber2Txt.setWidth("7em");
        haber2Txt.setValue(0.00);

        haber3Txt = new NumberField();
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
        haber3Txt.setWidth("7em");
        haber3Txt.setValue(0.00);

        haber4Txt = new NumberField();
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
        haber4Txt.setWidth("7em");
        haber4Txt.setValue(0.00);

        haber5Txt = new NumberField();
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
        haber5Txt.setWidth("7em");
        haber5Txt.setValue(0.00);

        haber6Txt = new NumberField();
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
        haber6Txt.setWidth("7em");
        haber6Txt.setValue(0.00);

        haber7Txt = new NumberField();
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
        haber7Txt.setWidth("7em");
        haber7Txt.setValue(0.00);

        haber8Txt = new NumberField();
        haber8Txt.setDecimalAllowed(true);
        haber8Txt.setDecimalPrecision(2);
        haber8Txt.setMinimumFractionDigits(2);
        haber8Txt.setDecimalSeparator('.');
        haber8Txt.setDecimalSeparatorAlwaysShown(true);
        haber8Txt.setValue(0d);
        haber8Txt.setGroupingUsed(true);
        haber8Txt.setGroupingSeparator(',');
        haber8Txt.setGroupingSize(3);
        haber8Txt.setImmediate(true);
        haber8Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        haber8Txt.setWidth("7em");
        haber8Txt.setValue(0.00);

        haber9Txt = new NumberField();
        haber9Txt.setDecimalAllowed(true);
        haber9Txt.setDecimalPrecision(2);
        haber9Txt.setMinimumFractionDigits(2);
        haber9Txt.setDecimalSeparator('.');
        haber9Txt.setDecimalSeparatorAlwaysShown(true);
        haber9Txt.setValue(0d);
        haber9Txt.setGroupingUsed(true);
        haber9Txt.setGroupingSeparator(',');
        haber9Txt.setGroupingSize(3);
        haber9Txt.setImmediate(true);
        haber9Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        haber9Txt.setWidth("7em");
        haber9Txt.setValue(0.00);

        haber10Txt = new NumberField();
        haber10Txt.setDecimalAllowed(true);
        haber10Txt.setDecimalPrecision(2);
        haber10Txt.setMinimumFractionDigits(2);
        haber10Txt.setDecimalSeparator('.');
        haber10Txt.setDecimalSeparatorAlwaysShown(true);
        haber10Txt.setValue(0d);
        haber10Txt.setGroupingUsed(true);
        haber10Txt.setGroupingSeparator(',');
        haber10Txt.setGroupingSize(3);
        haber10Txt.setImmediate(true);
        haber10Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        haber10Txt.setWidth("7em");
        haber10Txt.setValue(0.00);

        debe1Txt = new NumberField("DEBE : ");
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
        debe1Txt.setWidth("7em");
        debe1Txt.setValue(0.00);

        debe2Txt = new NumberField();
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
        debe2Txt.setWidth("7em");
        debe2Txt.setValue(0.00);
        debe2Txt.setEnabled(false);

        debe3Txt = new NumberField();
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
        debe3Txt.setWidth("7em");
        debe3Txt.setValue(0.00);
        debe3Txt.setEnabled(false);

        debe4Txt = new NumberField();
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
        debe4Txt.setWidth("7em");
        debe4Txt.setValue(0.00);
        debe4Txt.setEnabled(false);

        debe5Txt = new NumberField();
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
        debe5Txt.setWidth("7em");
        debe5Txt.setValue(0.00);
        debe5Txt.setEnabled(false);

        debe6Txt = new NumberField();
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
        debe6Txt.setWidth("7em");
        debe6Txt.setValue(0.00);
        debe6Txt.setEnabled(false);

        debe7Txt = new NumberField();
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
        debe7Txt.setWidth("7em");
        debe7Txt.setValue(0.00);
        debe7Txt.setEnabled(false);

        debe8Txt = new NumberField();
        debe8Txt.setDecimalAllowed(true);
        debe8Txt.setDecimalPrecision(2);
        debe8Txt.setMinimumFractionDigits(2);
        debe8Txt.setDecimalSeparator('.');
        debe8Txt.setDecimalSeparatorAlwaysShown(true);
        debe8Txt.setValue(0d);
        debe8Txt.setGroupingUsed(true);
        debe8Txt.setGroupingSeparator(',');
        debe8Txt.setGroupingSize(3);
        debe8Txt.setImmediate(true);
        debe8Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        debe8Txt.setWidth("7em");
        debe8Txt.setValue(0.00);
        debe8Txt.setEnabled(false);

        debe9Txt = new NumberField();
        debe9Txt.setDecimalAllowed(true);
        debe9Txt.setDecimalPrecision(2);
        debe9Txt.setMinimumFractionDigits(2);
        debe9Txt.setDecimalSeparator('.');
        debe9Txt.setDecimalSeparatorAlwaysShown(true);
        debe9Txt.setValue(0d);
        debe9Txt.setGroupingUsed(true);
        debe9Txt.setGroupingSeparator(',');
        debe9Txt.setGroupingSize(3);
        debe9Txt.setImmediate(true);
        debe9Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        debe9Txt.setWidth("7em");
        debe9Txt.setValue(0.00);
        debe9Txt.setEnabled(false);

        debe10Txt = new NumberField();
        debe10Txt.setDecimalAllowed(true);
        debe10Txt.setDecimalPrecision(2);
        debe10Txt.setMinimumFractionDigits(2);
        debe10Txt.setDecimalSeparator('.');
        debe10Txt.setDecimalSeparatorAlwaysShown(true);
        debe10Txt.setValue(0d);
        debe10Txt.setGroupingUsed(true);
        debe10Txt.setGroupingSeparator(',');
        debe10Txt.setGroupingSize(3);
        debe10Txt.setImmediate(true);
        debe10Txt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        debe10Txt.setWidth("7em");
        debe10Txt.setValue(0.00);
        debe10Txt.setEnabled(false);

        guardarBtn = new Button("Grabar");
        guardarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        guardarBtn.setIcon(FontAwesome.SAVE);
        guardarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (validarCamposParaIngresarPagoDocumentos() == false) {
                    actualizarSaldosFacturas();
                }
            }
        });

        salirBtn = new Button("Salir");
        salirBtn.addStyleName(ValoTheme.BUTTON_DANGER);
        salirBtn.setWidth("7em");
        salirBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();
            }
        });

        layoutHorizontal1.addComponent(cuentaContable1Cbx);
        layoutHorizontal1.addComponent(debe1Txt);
        layoutHorizontal1.addComponent(haber1Txt);
        layoutHorizontal1.addComponent(codigo1Txt);

        layoutHorizontal2.addComponent(cuentaContable2Cbx);
        layoutHorizontal2.addComponent(debe2Txt);
        layoutHorizontal2.addComponent(haber2Txt);
        layoutHorizontal2.addComponent(codigo2Txt);

        layoutHorizontal3.addComponent(cuentaContable3Cbx);
        layoutHorizontal3.addComponent(debe3Txt);
        layoutHorizontal3.addComponent(haber3Txt);
        layoutHorizontal3.addComponent(codigo3Txt);

        layoutHorizontal4.addComponent(cuentaContable4Cbx);
        layoutHorizontal4.addComponent(debe4Txt);
        layoutHorizontal4.addComponent(haber4Txt);
        layoutHorizontal4.addComponent(codigo4Txt);

        layoutHorizontal5.addComponent(cuentaContable5Cbx);
        layoutHorizontal5.addComponent(debe5Txt);
        layoutHorizontal5.addComponent(haber5Txt);
        layoutHorizontal5.addComponent(codigo5Txt);

        layoutHorizontal6.addComponent(cuentaContable6Cbx);
        layoutHorizontal6.addComponent(debe6Txt);
        layoutHorizontal6.addComponent(haber6Txt);
        layoutHorizontal6.addComponent(codigo6Txt);

        layoutHorizontal7.addComponent(cuentaContable7Cbx);
        layoutHorizontal7.addComponent(debe7Txt);
        layoutHorizontal7.addComponent(haber7Txt);
        layoutHorizontal7.addComponent(codigo7Txt);

        layoutHorizontal8.addComponent(cuentaContable8Cbx);
        layoutHorizontal8.addComponent(debe8Txt);
        layoutHorizontal8.addComponent(haber8Txt);
        layoutHorizontal8.addComponent(codigo8Txt);

        layoutHorizontal9.addComponent(cuentaContable9Cbx);
        layoutHorizontal9.addComponent(debe9Txt);
        layoutHorizontal9.addComponent(haber9Txt);
        layoutHorizontal9.addComponent(codigo9Txt);

        layoutHorizontal10.addComponent(cuentaContable10Cbx);
        layoutHorizontal10.addComponent(debe10Txt);
        layoutHorizontal10.addComponent(haber10Txt);
        layoutHorizontal10.addComponent(codigo10Txt);

        layoutHorizontal11.addComponents(salirBtn, guardarBtn);
        layoutHorizontal11.setComponentAlignment(guardarBtn, Alignment.BOTTOM_CENTER);
        layoutHorizontal11.setComponentAlignment(salirBtn, Alignment.BOTTOM_CENTER);

        rightVerticalLayout.addComponent(facturasGrid);
        rightVerticalLayout.setComponentAlignment(facturasGrid, Alignment.TOP_CENTER);
        rightVerticalLayout.addComponent(layoutHorizontal1);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal1, Alignment.MIDDLE_CENTER);

        rightVerticalLayout.addComponent(layoutHorizontal2);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal2, Alignment.MIDDLE_CENTER);

        rightVerticalLayout.addComponent(layoutHorizontal3);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal3, Alignment.MIDDLE_CENTER);

        rightVerticalLayout.addComponent(layoutHorizontal4);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal4, Alignment.MIDDLE_CENTER);

        rightVerticalLayout.addComponent(layoutHorizontal5);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal5, Alignment.MIDDLE_CENTER);

        rightVerticalLayout.addComponent(layoutHorizontal6);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal6, Alignment.MIDDLE_CENTER);

        rightVerticalLayout.addComponent(layoutHorizontal7);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal7, Alignment.MIDDLE_CENTER);

        rightVerticalLayout.addComponent(layoutHorizontal8);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal8, Alignment.MIDDLE_CENTER);

        rightVerticalLayout.addComponent(layoutHorizontal9);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal9, Alignment.MIDDLE_CENTER);

        rightVerticalLayout.addComponent(layoutHorizontal10);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal10, Alignment.MIDDLE_CENTER);

        rightVerticalLayout.addComponent(layoutHorizontal11);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal11, Alignment.MIDDLE_CENTER);

        horizontalLayout.addComponents(leftVerticalLayout, rightVerticalLayout);

        return horizontalLayout;

    }

    public void llenarTablaDocumentosPagar() {

        facturasContainer.removeAllItems();

        queryString = " SELECT * from contabilidad_partida ";
        queryString += " WHERE IdNomenclatura = " + ((SopdiUI) mainUI).cuentasContablesDefault.getClientes();
        queryString += " And TipoDocumento IN ('FACTURA VENTA', 'RECIBO CONTABLE',  'RECIBO CONTABLE VENTA')";
        queryString += " And IdProveedor = " + proveedorCbx.getValue();
        queryString += " And IdEmpresa = " + empresaCbx.getValue();
        queryString += " And Estatus <> 'ANULADO'";

        Logger.getLogger(this.getClass().getName()).log(Level.INFO,"QUERY BUSCAR FACTURAS VENTA DE UN PROVEEDOR (CIENTE) : " + queryString);

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                do {

                    // se busca el saldo real...
                    queryString = " SELECT  ";
                    queryString += " SUM(DEBE - HABER) as TOTALSALDO, SUM(DebeQuetzales - HaberQuetzales) as TOTALSALDOQ ";
                    queryString += " FROM contabilidad_partida";
                    queryString += " WHERE CodigoCC = '" + rsRecords.getString("CodigoCC") + "'";
                    queryString += " AND IdNomenclatura = " + rsRecords.getString("IdNomenclatura");
                    queryString += " GROUP BY CodigoCC, IdNomenclatura";
                    queryString += " HAVING TOTALSALDO > 0";
// no es necesario                   queryString += " Order by contabilidad_partida.NombreProveedor";

                    stQuery2 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                    rsRecords2 = stQuery2.executeQuery(queryString);

                    if (rsRecords2.next()) {
                        Object itemId = facturasContainer.addItem();
                        facturasContainer.getContainerProperty(itemId, CODIGO_PARTIDA_PROPERTY).setValue(rsRecords.getString("CodigoPartida"));
                        facturasContainer.getContainerProperty(itemId, CODIGO_CC_PROPERTY).setValue(rsRecords.getString("CodigoCC"));
                        facturasContainer.getContainerProperty(itemId, FECHA_DOCUMENTO_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha")));
                        facturasContainer.getContainerProperty(itemId, NUMERO_DOCUMENTO_PROPERTY).setValue(rsRecords.getString("NumeroDocumento") + " " + rsRecords.getString("SerieDocumento"));
                        facturasContainer.getContainerProperty(itemId, MONEDA_DOCUMENTO_PROPERTY).setValue(rsRecords.getString("MonedaDocumento"));
                        facturasContainer.getContainerProperty(itemId, MONTO_DOCUMENTO_PROPERTY).setValue(rsRecords2.getString("TOTALSALDO"));
                    }

                } while (rsRecords.next());
            }
            if (facturasContainer.size() == 0) {
                guardarBtn.setEnabled(false);
                Notification.show("Este cliente no tiene facturas/recibos contables pendientes de pago.", Notification.Type.ASSISTIVE_NOTIFICATION);
            } else {
                guardarBtn.setEnabled(true);
            }
        } catch (Exception ex) {
            System.out.println("Error al listar tabla de DOCUMENTOS : " + ex);
            Notification.show("ERROR AL LEER DOCUMENTOS VENTA PARA REGISTRAR PAGOS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
        /*
        queryString = " SELECT *, ";
        queryString += " SUM(contabilidad_partida.Debe) TOTALDEBE, SUM(contabilidad_partida.Haber) TOTALHABER,";
        queryString += " SUM(contabilidad_partida.DebeQuetzales) TOTALDEBEQ, SUM(contabilidad_partida.HaberQuetzales) TOTALHABERQ, ";
        queryString += " SUM(DEBE - HABER) TOTALSALDO, SUM(DebeQuetzales - HaberQuetzales) TOTALSALDOQ ";
        queryString += " FROM contabilidad_partida";
        queryString += " INNER JOIN contabilidad_nomenclatura ON contabilidad_nomenclatura.IdNomenclatura = contabilidad_partida.IdNomenclatura";
        queryString += " WHERE Fecha >= '2015-01-01' AND contabilidad_partida.IdLiquidacion = 0";
        queryString += " AND (trim(contabilidad_partida.CodigoCC) <> '' AND contabilidad_partida.CodigoCC <> '0')";
        queryString += " AND contabilidad_partida.IdEmpresa = " + empresaCbx.getValue();
        queryString += " And contabilidad_partida.IdProveedor = " + proveedorCbx.getValue();
        queryString += " AND contabilidad_nomenclatura.IdNomenclatura = " + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getCientes();
        queryString += " AND contabilidad_partida.MontoAutorizadoPagar = 0 ";
        queryString += " AND contabilidad_partida.MontoAplicarAnticipo = 0 ";
        queryString += " GROUP BY contabilidad_partida.CodigoCC, contabilidad_partida.IdNomenclatura";
        queryString += " HAVING TOTALSALDO > 0";
        queryString += " Order by contabilidad_partida.NombreProveedor";

        System.out.println("Query para listar tabla documentos pagar" + queryString);

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado
                do {
                    Object itemId = facturasContainer.addItem();

                    facturasContainer.getContainerProperty(itemId, CODIGO_PARTIDA_PROPERTY).setValue(rsRecords.getString("CodigoPartida"));
                    facturasContainer.getContainerProperty(itemId, CODIGO_CC_PROPERTY).setValue(rsRecords.getString("CodigoCC"));
                    facturasContainer.getContainerProperty(itemId, NUMERO_DOCUMENTO_PROPERTY).setValue(rsRecords.getString("NumeroDocumento") + " " + rsRecords.getString("SerieDocumento"));
                    facturasContainer.getContainerProperty(itemId, MONEDA_DOCUMENTO_PROPERTY).setValue(rsRecords.getString("MonedaDocumento"));
                    facturasContainer.getContainerProperty(itemId, MONTO_DOCUMENTO_PROPERTY).setValue(rsRecords.getString("TOTALSALDO"));

                } while (rsRecords.next());
            }

        } catch (Exception ex) {
            System.out.println("Error al listar tabla de DOCUMENTOS : " + ex);
            Notification.show("ERROR AL LEER DOCUMENTOS AUTORIZADOS PARA PAGOS : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
         */
    }

    public boolean validarCamposParaIngresarPagoDocumentos() {

        boolean error = false;

        totalDebe = new BigDecimal(debe1Txt.getDoubleValueDoNotThrow()
                + debe2Txt.getDoubleValueDoNotThrow() + debe3Txt.getDoubleValueDoNotThrow()
                + debe4Txt.getDoubleValueDoNotThrow() + debe5Txt.getDoubleValueDoNotThrow()
                + debe6Txt.getDoubleValueDoNotThrow() + debe7Txt.getDoubleValueDoNotThrow()
                + debe8Txt.getDoubleValueDoNotThrow() + debe9Txt.getDoubleValueDoNotThrow()
                + debe10Txt.getDoubleValueDoNotThrow()
        ).setScale(2, BigDecimal.ROUND_HALF_UP);

        totalHaber = new BigDecimal(haber1Txt.getDoubleValueDoNotThrow()
                + haber2Txt.getDoubleValueDoNotThrow() + haber3Txt.getDoubleValueDoNotThrow()
                + haber4Txt.getDoubleValueDoNotThrow() + haber5Txt.getDoubleValueDoNotThrow()
                + haber6Txt.getDoubleValueDoNotThrow() + haber7Txt.getDoubleValueDoNotThrow()
                + haber8Txt.getDoubleValueDoNotThrow() + haber9Txt.getDoubleValueDoNotThrow()
                + haber10Txt.getDoubleValueDoNotThrow()
        ).setScale(2, BigDecimal.ROUND_HALF_UP);

        totalDebe.setScale(2, BigDecimal.ROUND_HALF_UP);
        totalHaber.setScale(2, BigDecimal.ROUND_HALF_UP);

        if (totalDebe.doubleValue() != totalHaber.doubleValue()) {
            System.out.println("Debe =" + totalDebe.doubleValue() + "  haber=" + totalHaber);
            Notification.show("La partida está descuadrada, por favor revisar"
                    + " Debe = " + totalDebe.doubleValue() + "  Haber = " + totalHaber, Notification.Type.WARNING_MESSAGE);
            error = true;
        }

        if (proveedorCbx.getValue() == null) {
            Notification.show("Por favor, seleccione un cliente..", Notification.Type.ERROR_MESSAGE);
            proveedorCbx.focus();
            error = true;
        }

        if (montoTxt.getDoubleValueDoNotThrow() == 0.00) {
            Notification.show("Por favor, Ingrese un monto..", Notification.Type.ERROR_MESSAGE);
            montoTxt.focus();
            error = true;
        }
        /*
        if (descripcionTxt.getValue().isEmpty()) {
            Notification.show("Por favor, Ingrese una descripción..", Notification.Type.ERROR_MESSAGE);
            descripcionTxt.focus();
            error = true;
        }
         */
        if (monedaCbx.getValue().equals("DOLARES") && tipoCambioTxt.getDoubleValueDoNotThrow() <= 1.00) {
            Notification.show("Si la transacción es en DOLARES, debe llebar tipo de cambio. Por favor, revise la moneda.", Notification.Type.ERROR_MESSAGE);
            monedaCbx.focus();
            error = true;
        }
        if (monedaCbx.getValue().equals("QUETZALES") && tipoCambioTxt.getDoubleValueDoNotThrow() < 1.00) {
            Notification.show("Si la transacción es en QUETZALES, debe llebar 1.00 de tipo de cambio. Por favor revise.", Notification.Type.ERROR_MESSAGE);
            monedaCbx.focus();
            error = true;
        }
        if (monedaCbx.getValue().equals("DOLARES") && !String.valueOf(cuentaContable1Cbx.getValue()).equals(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaExtranjera())) {
            Notification.show("Si la transacción es en DOLARES, debe llebar cuenta contable DOLARES. Por favor evise la CUENTA CONTABLE.", Notification.Type.ERROR_MESSAGE);
            cuentaContable1Cbx.focus();
            error = true;
        }
        if (medioCbx.getValue() == null) {
            Notification.show("Por favor seleccionar un Medio.", Notification.Type.ERROR_MESSAGE);
            medioCbx.focus();
            error = true;
        }

        if (numeroTxt.getValue().isEmpty()) {
            Notification.show("Por favor ingrese un # de DEPOSITO o NOTA DE CREDITO.", Notification.Type.ERROR_MESSAGE);
            numeroTxt.focus();
            error = true;
        }

        if(facturasContainer.size() > 0) {
            if(facturasGrid.getSelectedRows().size() == 0) {
                Notification.show("Por favor relacione uno o mas facturas o recibos contables para este DEPOSITO o NOTA DE CREDITO.", Notification.Type.ERROR_MESSAGE);
                error = true;
            }
        }
        return error;
    }

    public void actualizarSaldosFacturas() {

        try {

            Utileria utileria = new Utileria();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

            Date fechaInicial = dateFormat.parse(utileria.getFecha());
            Date fechaFinal = dateFormat.parse(utileria.getFechaYYYYMMDD(fechaDt.getValue()));

            int dias = (int) ((fechaInicial.getTime() - fechaFinal.getTime()) / 86400000);

            System.out.println("DIAS DE DIFERENCIA ENTRE FACTURA Y FECHA ACTUAL : " + dias);

            if (dias > 30) {

                if (((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserToken() == null) {
                    ValidarTokenForm validarTokenForm = new ValidarTokenForm(false);
                    UI.getCurrent().addWindow(validarTokenForm);
                    validarTokenForm.center();
                } else {
                    variableTemp = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserToken();
                    ((SopdiUI) UI.getCurrent()).sessionInformation.setStrUserToken(null);
                }
            }

        } catch (Exception e) {
            System.out.println("Error validar fechas " + e);
            e.printStackTrace();

        }

        //// PRIMERO  POR CADA FACTURA QUE ESTAMOS SELECCINANDO ACTUALIZAR SU SALDO
        String codigoPartida = "";
        Iterator iter = facturasGrid.getSelectedRows().iterator();
        Object gridItem;

        while (iter.hasNext()) {

            gridItem = iter.next();

            codigoPartida = String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(CODIGO_PARTIDA_PROPERTY).getValue());

            queryString = " Update  contabilidad_partida ";
            queryString += " Set Saldo = Saldo - " + String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(MONTO_PAGADO_PROPERTY).getValue()).replaceAll(",", "");
            queryString += " Where CodigoPartida = '" + codigoPartida + "'";
            queryString += " And IdEmpresa = " + empresaCbx.getValue();
            queryString += " And IdProveedor = " + proveedorCbx.getValue();

            System.out.println("Query actualizar saldo de facturas :" + queryString);

            try {
                stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                stQuery.executeUpdate(queryString);

            } catch (Exception ex1) {
                System.out.println("Error al actualizar el saldo de facturas seleccionadas : " + ex1.getMessage());
                ex1.printStackTrace();
            }
        }
        /// SI HAY ENGANCHES ACTUALIZAR SALDO

        if (codigoEnganches.size() > 0) {
            for (int i = 0; i < codigoEnganches.size(); i++) {

                queryString = " Update  contabilidad_partida ";
                queryString += " Set Saldo = 0";
                queryString += " Where CodigoPartida = '" + codigoEnganches.get(i) + "'";
                queryString += " And IdEmpresa = " + empresaCbx.getValue();
                queryString += " And IdProveedor = " + proveedorCbx.getValue();

                System.out.println("Query actualizar saldo de enganches :" + queryString);
                try {
                    stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                    stQuery.executeUpdate(queryString);
                } catch (Exception ex1) {
                    System.out.println("Error al actualizar el saldo de los enganches" + ex1.getMessage());
                    ex1.printStackTrace();
                }
            }
        }

        ingresarPagoDocumentoVenta();
    }

    public void ingresarPagoDocumentoVenta() {

        queryString = " Select * from contabilidad_partida";
        queryString += " Where NumeroDocumento = '" + numeroTxt.getValue().toUpperCase().trim() + "'";
        queryString += " And IdProveedor     =  " + String.valueOf(proveedorCbx.getValue());
        queryString += " And IdEmpresa = " + String.valueOf(empresaCbx.getValue());
        queryString += " And TipoDocumento = '" + String.valueOf(medioCbx.getValue())  + "'";
        queryString += " And MonedaDocumento = '" + monedaCbx.getValue() + "'";

        System.out.println("\n\nQuery=" + queryString + "\n\n");

        try {
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                Notification.show("Este documento ya fué ingresado, revise.!.", Notification.Type.WARNING_MESSAGE);
                numeroTxt.focus();
                return;
            }
        } catch (Exception ex1) {
            System.out.println("Error al buscar documento : " + ex1.getMessage());
            ex1.printStackTrace();
        }

        String fecha = Utileria.getFechaYYYYMMDD_1(fechaDt.getValue());
        String ultimoEncontado;
        String dia = fecha.substring(8, 10);
        String mes = fecha.substring(5, 7);
        String año = fecha.substring(0, 4);

        codigoPartida = String.valueOf(empresaCbx.getValue()) + año + mes + dia + "5";

        queryString = " select codigoPartida from contabilidad_partida ";
        queryString += " where codigoPartida like '" + codigoPartida + "%'";
        queryString += " order by codigoPartida desc ";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {   //encontrado

                ultimoEncontado = rsRecords.getString("codigoPartida").substring(12, 15);

                System.out.println("ultimo encontrado " + ultimoEncontado);

                codigoPartida += String.format("%03d", (Integer.valueOf(ultimoEncontado) + 1));

            } else {
                codigoPartida += "001";
            }

        } catch (Exception ex1) {
            System.out.println("Error al buscar el ultimo codigoPartida" + ex1.getMessage());
            ex1.printStackTrace();
        }

        queryString = " Insert Into contabilidad_partida (IdEmpresa, Estatus, CodigoPartida, CodigoCC, ";
        queryString += " TipoDocumento, Fecha, IdProveedor, NombreProveedor, NumeroDocumento, IdNomenclatura,";
        queryString += " MonedaDocumento, MontoDocumento, Debe, Haber, DebeQuetzales, HaberQuetzales, TipoCambio, Saldo,";
        queryString += " Descripcion, CreadoUsuario, CreadoFechaYHora)";
        queryString += " Values (";
        queryString += String.valueOf(empresaCbx.getValue());
        queryString += ",'INGRESADO'";
        queryString += ",'" + codigoPartida + "'";
        queryString += ",''";
        queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
        queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
        queryString += "," + proveedorCbx.getValue();
        queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
        queryString += ",'" + numeroTxt.getValue() + "'";
        queryString += "," + cuentaContable1Cbx.getValue();
        queryString += ",'" + monedaCbx.getValue() + "'";
        queryString += ","  + montoTxt.getDoubleValueDoNotThrow();
        queryString += "," + String.valueOf(debe1Txt.getDoubleValueDoNotThrow()); // DEBE
        queryString += ",0.00"; //HABER
        queryString += "," + String.valueOf(debe1Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());
        queryString += ",0.00"; //HABER Q.
        queryString += "," + String.valueOf(tipoCambioTxt.getDoubleValueDoNotThrow());
        queryString += ",0.00";
        queryString += ",'Ingreso por " + String.valueOf(tipoIngresoCbx.getValue()) + " " + descripcionTxt.getValue() + "'";
        queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
        queryString += ",current_timestamp";
        queryString += ")";

        if (cuentaContable2Cbx.getValue() != null && (debe2Txt.getDoubleValueDoNotThrow() != 0.00
                || haber2Txt.getDoubleValueDoNotThrow() != 0.00)) {

            queryString += ",(";
            queryString += String.valueOf(empresaCbx.getValue());
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigo2Txt.getValue() + "'";   /// CODIGOCC
            queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += "," + cuentaContable2Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += ","  + montoTxt.getDoubleValueDoNotThrow();
            queryString += "," + String.valueOf(debe2Txt.getDoubleValueDoNotThrow());  //debe
            queryString += "," + String.valueOf(haber2Txt.getDoubleValueDoNotThrow());  //HABER
            queryString += "," + String.valueOf(debe2Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());  // DEBE Q.
            queryString += "," + String.valueOf(haber2Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00";
            queryString += ",'Ingreso por " + String.valueOf(tipoIngresoCbx.getValue()) + " " + descripcionTxt.getValue() + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";
        }

        if (cuentaContable3Cbx.getValue() != null && (debe3Txt.getDoubleValueDoNotThrow() != 0.00
                || haber3Txt.getDoubleValueDoNotThrow() != 0.00)) {

            queryString += ",(";
            queryString += String.valueOf(empresaCbx.getValue());
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigo3Txt.getValue() + "'";   /// CODIGOCC
            queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += "," + cuentaContable3Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += ","  + montoTxt.getDoubleValueDoNotThrow();
            queryString += "," + String.valueOf(debe3Txt.getDoubleValueDoNotThrow());  //debe
            queryString += "," + String.valueOf(haber3Txt.getDoubleValueDoNotThrow());  //HABER
            queryString += "," + String.valueOf(debe3Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());  // DEBE Q.
            queryString += "," + String.valueOf(haber3Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00";
            queryString += ",'Ingreso por " + String.valueOf(tipoIngresoCbx.getValue()) + " " + descripcionTxt.getValue() + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";

        }

        if (cuentaContable4Cbx.getValue() != null && (debe4Txt.getDoubleValueDoNotThrow() != 0.00
                || haber4Txt.getDoubleValueDoNotThrow() != 0.00)) {

            queryString += ",(";
            queryString += String.valueOf(empresaCbx.getValue());
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigo4Txt.getValue() + "'";   /// CODIGOCC
            queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += "," + cuentaContable4Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += ","  + montoTxt.getDoubleValueDoNotThrow();
            queryString += "," + String.valueOf(debe4Txt.getDoubleValueDoNotThrow());  //debe
            queryString += "," + String.valueOf(haber4Txt.getDoubleValueDoNotThrow());  //HABER
            queryString += "," + String.valueOf(debe4Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());  // DEBE Q.
            queryString += "," + String.valueOf(haber4Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00";
            queryString += ",'Ingreso por " + String.valueOf(tipoIngresoCbx.getValue()) + " " + descripcionTxt.getValue() + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";
        }

        if (cuentaContable5Cbx.getValue() != null && (debe5Txt.getDoubleValueDoNotThrow() != 0.00
                || haber5Txt.getDoubleValueDoNotThrow() != 0.00)) {

            queryString += ",(";
            queryString += String.valueOf(empresaCbx.getValue());
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigo5Txt.getValue() + "'";   /// CODIGOCC
            queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += "," + cuentaContable5Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += ","  + montoTxt.getDoubleValueDoNotThrow();
            queryString += "," + String.valueOf(debe5Txt.getDoubleValueDoNotThrow());  //debe
            queryString += "," + String.valueOf(haber5Txt.getDoubleValueDoNotThrow());  //HABER
            queryString += "," + String.valueOf(debe5Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());  // DEBE Q.
            queryString += "," + String.valueOf(haber5Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00";
            queryString += ",'Ingreso por " + String.valueOf(tipoIngresoCbx.getValue()) + " " + descripcionTxt.getValue() + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";
        }

        if (cuentaContable6Cbx.getValue() != null && (debe6Txt.getDoubleValueDoNotThrow() != 0.00
                || haber6Txt.getDoubleValueDoNotThrow() != 0.00)) {

            queryString += ",(";
            queryString += String.valueOf(empresaCbx.getValue());
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigo6Txt.getValue() + "'";   /// CODIGOCC
            queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += "," + cuentaContable6Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += ","  + montoTxt.getDoubleValueDoNotThrow();
            queryString += "," + String.valueOf(debe6Txt.getDoubleValueDoNotThrow());  //debe
            queryString += "," + String.valueOf(haber6Txt.getDoubleValueDoNotThrow());  //HABER
            queryString += "," + String.valueOf(debe6Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());  // DEBE Q.
            queryString += "," + String.valueOf(haber6Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00";
            queryString += ",'Ingreso por " + String.valueOf(tipoIngresoCbx.getValue()) + " " + descripcionTxt.getValue() + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";
        }
        if (cuentaContable7Cbx.getValue() != null && (debe7Txt.getDoubleValueDoNotThrow() != 0.00
                || haber7Txt.getDoubleValueDoNotThrow() != 0.00)) {

            queryString += ",(";
            queryString += String.valueOf(empresaCbx.getValue());
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigo7Txt.getValue() + "'";   /// CODIGOCC
            queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += "," + cuentaContable7Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += ","  + montoTxt.getDoubleValueDoNotThrow();
            queryString += "," + String.valueOf(debe7Txt.getDoubleValueDoNotThrow());  //debe
            queryString += "," + String.valueOf(haber7Txt.getDoubleValueDoNotThrow());  //HABER
            queryString += "," + String.valueOf(debe7Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());  // DEBE Q.
            queryString += "," + String.valueOf(haber7Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00";
            queryString += ",'Ingreso por " + String.valueOf(tipoIngresoCbx.getValue()) + " " + descripcionTxt.getValue() + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";
        }

        if (cuentaContable8Cbx.getValue() != null && (debe8Txt.getDoubleValueDoNotThrow() != 0.00
                || haber8Txt.getDoubleValueDoNotThrow() != 0.00)) {

            queryString += ",(";
            queryString += String.valueOf(empresaCbx.getValue());
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigo8Txt.getValue() + "'";   /// CODIGOCC
            queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += "," + cuentaContable8Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += ","  + montoTxt.getDoubleValueDoNotThrow();
            queryString += "," + String.valueOf(debe8Txt.getDoubleValueDoNotThrow());  //debe
            queryString += "," + String.valueOf(haber8Txt.getDoubleValueDoNotThrow());  //HABER
            queryString += "," + String.valueOf(debe8Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());  // DEBE Q.
            queryString += "," + String.valueOf(haber8Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00";
            queryString += ",'Ingreso por " + String.valueOf(tipoIngresoCbx.getValue()) + " " + descripcionTxt.getValue() + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";
        }

        if (cuentaContable9Cbx.getValue() != null && (debe9Txt.getDoubleValueDoNotThrow() != 0.00
                || haber9Txt.getDoubleValueDoNotThrow() != 0.00)) {

            queryString += ",(";
            queryString += String.valueOf(empresaCbx.getValue());
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigo9Txt.getValue() + "'";   /// CODIGOCC
            queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += "," + cuentaContable9Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += ","  + montoTxt.getDoubleValueDoNotThrow();
            queryString += "," + String.valueOf(debe9Txt.getDoubleValueDoNotThrow());  //debe
            queryString += "," + String.valueOf(haber9Txt.getDoubleValueDoNotThrow());  //HABER
            queryString += "," + String.valueOf(debe9Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());  // DEBE Q.
            queryString += "," + String.valueOf(haber9Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00";
            queryString += ",'Ingreso por " + String.valueOf(tipoIngresoCbx.getValue()) + " " + descripcionTxt.getValue() + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";
        }

        if (cuentaContable10Cbx.getValue() != null && (debe10Txt.getDoubleValueDoNotThrow() != 0.00
                || haber10Txt.getDoubleValueDoNotThrow() != 0.00)) {

            queryString += ",(";
            queryString += String.valueOf(empresaCbx.getValue());
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigo10Txt.getValue() + "'";   /// CODIGOCC
            queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorCbx.getValue();
            queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += "," + cuentaContable10Cbx.getValue();
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += ","  + montoTxt.getDoubleValueDoNotThrow();
            queryString += "," + String.valueOf(debe10Txt.getDoubleValueDoNotThrow());  //debe
            queryString += "," + String.valueOf(haber10Txt.getDoubleValueDoNotThrow());  //HABER
            queryString += "," + String.valueOf(debe10Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());  // DEBE Q.
            queryString += "," + String.valueOf(haber10Txt.getDoubleValueDoNotThrow() * tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(tipoCambioTxt.getDoubleValueDoNotThrow());
            queryString += ",0.00";
            queryString += ",'Ingreso por " + String.valueOf(tipoIngresoCbx.getValue()) + " " + descripcionTxt.getValue() + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";
        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Ingreso de pago de documento venta = " + queryString);

        try {

            ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().setAutoCommit(false);

            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            if (!variableTemp.isEmpty()) {
                cambiarEstatusToken(codigoPartida);
            }

            ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().commit();
            ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().setAutoCommit(true);

            facturasGrid.getSelectedRows().clear();
            facturasGrid.getSelectionModel().reset();
            facturasContainer.removeAllItems();

            ((IngresoBancosView) (mainUI.getNavigator().getCurrentView())).llenarTablaFactura(String.valueOf(empresaCbx.getValue()));

            Notification notif = new Notification("PAGO REGISTRADO EXITOSAMENTE.",
                    Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.CHECK);
            notif.show(Page.getCurrent());

            montoTxt.setValue(0.00);

        } catch (Exception ex1) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE,"Error al insertar pago documento venta: " + ex1.getMessage());
            ex1.printStackTrace();

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
                Logger.getLogger(PagoDocumentoVentaForm.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    public void limpiarPartida() {
        //montoTxt.setValue(0.00);

        cuentaContable1Cbx.setReadOnly(false);
        cuentaContable1Cbx.clear();
        haber1Txt.setReadOnly(false);
        debe1Txt.setReadOnly(false);
        haber1Txt.setValue(0.00);
        //debe1Txt.setValue(0.00);

        cuentaContable2Cbx.setReadOnly(false);
        cuentaContable2Cbx.clear();
        debe2Txt.setReadOnly(false);
        debe2Txt.setValue(0.00);
        haber2Txt.setReadOnly(false);
        haber2Txt.setValue(0.00);

        cuentaContable3Cbx.setReadOnly(false);
        cuentaContable3Cbx.clear();
        debe3Txt.setReadOnly(false);
        debe3Txt.setValue(0.00);
        haber3Txt.setReadOnly(false);
        haber3Txt.setValue(0.00);

        cuentaContable4Cbx.setReadOnly(false);
        cuentaContable4Cbx.clear();
        debe4Txt.setReadOnly(false);
        haber4Txt.setReadOnly(false);
        debe4Txt.setValue(0.00);
        haber4Txt.setValue(0.00);

        cuentaContable5Cbx.setReadOnly(false);
        cuentaContable5Cbx.clear();
        debe5Txt.setReadOnly(false);
        haber5Txt.setReadOnly(false);
        debe5Txt.setValue(0.00);
        haber5Txt.setValue(0.00);

        cuentaContable6Cbx.setReadOnly(false);
        cuentaContable6Cbx.clear();
        debe6Txt.setReadOnly(false);
        haber6Txt.setReadOnly(false);
        debe6Txt.setValue(0.00);
        haber6Txt.setValue(0.00);

        cuentaContable7Cbx.setReadOnly(false);
        cuentaContable7Cbx.clear();
        debe7Txt.setReadOnly(false);
        haber7Txt.setReadOnly(false);
        debe7Txt.setValue(0.00);
        haber7Txt.setValue(0.00);

        cuentaContable8Cbx.setReadOnly(false);
        cuentaContable8Cbx.clear();
        debe8Txt.setReadOnly(false);
        haber8Txt.setReadOnly(false);
        debe8Txt.setValue(0.00);
        haber8Txt.setValue(0.00);

        cuentaContable9Cbx.setReadOnly(false);
        cuentaContable9Cbx.clear();
        debe9Txt.setReadOnly(false);
        haber9Txt.setReadOnly(false);
        debe9Txt.setValue(0.00);
        haber9Txt.setValue(0.00);

        cuentaContable10Cbx.setReadOnly(false);
        cuentaContable10Cbx.clear();
        debe10Txt.setReadOnly(false);
        haber10Txt.setReadOnly(false);
        debe10Txt.setValue(0.00);
        haber10Txt.setValue(0.00);

    }

    public void cambiarEstatusToken(String codigoPartida) {

        try {
            queryString = "UPDATE token SET ";
            queryString += " IdUsuario = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserId();
            queryString += ", Usuario = '" + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserFullName() + "'";
            queryString += ", FechaUsado = current_timestamp";
            queryString += ", CodigoPartida = '" + codigoPartida + "'";
            queryString += ", Estatus = 'UTILIZADO'";
            queryString += " Where Codigo = '" + variableTemp + "'";

            stQuery.executeUpdate(queryString);

            variableTemp = "";

        } catch (Exception e) {
            System.out.println("Error al intentar cambiar estatus token : " + e);
            e.printStackTrace();
        }

    }
}
