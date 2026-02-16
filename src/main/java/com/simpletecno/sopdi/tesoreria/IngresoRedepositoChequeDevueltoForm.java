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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IngresoRedepositoChequeDevueltoForm extends Window {

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

    ComboBox cuentaContable1Cbx;
    ComboBox cuentaContable2Cbx;

    NumberField haber1Txt;
    NumberField haber2Txt;

    NumberField debe1Txt;
    NumberField debe2Txt;

    public IndexedContainer facturasContainer = new IndexedContainer();
    Grid facturasGrid;
    static final String CODIGO_PARTIDA_PROPERTY = "Codigo Partida";
    static final String CODIGO_CC_PROPERTY = "Codigo CC";
    static final String FECHA_DOCUMENTO_PROPERTY = "Fecha";
    static final String NUMERO_DOCUMENTO_PROPERTY = "# Documento";
    static final String MONEDA_DOCUMENTO_PROPERTY = "Moneda";
    static final String MONTO_DOCUMENTO_PROPERTY = "Monto";

    Statement stQuery, stQuery2, stQuery3;
    ResultSet rsRecords, rsRecords2, rsRecords3;
    String queryString, codigoPartida;
    String variableTemp = "";

    public IngresoRedepositoChequeDevueltoForm() {

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

        titleLbl = new Label("INGRESO A BANCOS REDEPOSITO CHEQUE DEVUELTO");
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

    public void llenarComboProveedor() {
        queryString = " SELECT prov.* ";
        queryString += " FROM proveedor prov";
        queryString += " WHERE prov.Inhabilitado = 0 ";
//        queryString += " AND prov.EsCliente = 1";
        queryString += " ORDER BY prov.Nombre";

        proveedorCbx.removeAllItems();

        try {
            stQuery2 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords2 = stQuery2.executeQuery(queryString);

            while (rsRecords2.next()) {  /// encontrado
                proveedorCbx.addItem(rsRecords2.getString("IDProveedor"));
                proveedorCbx.setItemCaption(rsRecords2.getString("IDProveedor"), rsRecords2.getString("Nombre"));
                proveedorCbx.getItem(rsRecords2.getString("IDProveedor")).getItemProperty(NIT_PROPERTY).setValue(rsRecords2.getString("NIT"));
                proveedorCbx.getItem(rsRecords2.getString("IDProveedor")).getItemProperty(NOMBRESINCODIGO_PROPERTY).setValue(rsRecords2.getString("Nombre") );
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar de clientes (tabla proveedores) " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void llenarComboCuentaContable() {

        queryString = " SELECT * FROM contabilidad_nomenclatura";
        queryString += " WHERE Estatus = 'HABILITADA'";
        queryString += " ORDER BY N5";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) {  // encontrado

                cuentaContable1Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable1Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                cuentaContable2Cbx.addItem(rsRecords.getString("IdNomenclatura"));
                cuentaContable2Cbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

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
        tipoIngresoCbx.addItem("REDEPOSITO CHEQUE DEVUELTO");
        tipoIngresoCbx.select("REDEPOSITO CHEQUE DEVUELTO");
        tipoIngresoCbx.setReadOnly(true);

        proveedorCbx = new ComboBox("Cliente");
        proveedorCbx.addContainerProperty(NIT_PROPERTY, String.class, "");
        proveedorCbx.addContainerProperty(NOMBRESINCODIGO_PROPERTY, String.class, "");
        proveedorCbx.setInvalidAllowed(false);
        proveedorCbx.setNewItemsAllowed(false);
        proveedorCbx.setNullSelectionAllowed(false);
        proveedorCbx.setWidth("100%");
        proveedorCbx.setFilteringMode(FilteringMode.CONTAINS);
        proveedorCbx.addValueChangeListener((event) -> {
            if (tipoIngresoCbx.getValue().equals("REDEPOSITO CHEQUE DEVUELTO")) {
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
        fechaDt.setValue(new Date());

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
                haber2Txt.setValue(montoTxt.getDoubleValueDoNotThrow());
                llenarTablaDocumentosPagar();
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
            llenarTablaDocumentosPagar();
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

        facturasContainer.addContainerProperty(CODIGO_PARTIDA_PROPERTY, String.class, "");
        facturasContainer.addContainerProperty(CODIGO_CC_PROPERTY, String.class, "");
        facturasContainer.addContainerProperty(NUMERO_DOCUMENTO_PROPERTY, String.class, "");
        facturasContainer.addContainerProperty(FECHA_DOCUMENTO_PROPERTY, String.class, "");
        facturasContainer.addContainerProperty(MONEDA_DOCUMENTO_PROPERTY, String.class, "");
        facturasContainer.addContainerProperty(MONTO_DOCUMENTO_PROPERTY, String.class, "");

        facturasGrid = new Grid("", facturasContainer);
        facturasGrid.setWidth("100%");
        facturasGrid.setImmediate(true);
        facturasGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        facturasGrid.setDescription("Seleccione un CHEQUE DEVUELTO.");
        facturasGrid.setHeightMode(HeightMode.ROW);
        facturasGrid.setHeightByRows(5);
        facturasGrid.setResponsive(true);
        facturasGrid.setEditorBuffered(false);
        facturasGrid.addSelectionListener(new SelectionEvent.SelectionListener() {
            @Override
            public void select(SelectionEvent event) {
                if (facturasGrid.getSelectedRow() != null) {

                    double montoDisponible = montoTxt.getDoubleValueDoNotThrow();

                    limpiarPartida();

                    if (monedaCbx.getValue() == "DOLARES") {
                        cuentaContable1Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaExtranjera());
                    } else {
                        cuentaContable1Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaLocal());
                    }

                    if (cuentaContable2Cbx.getValue() == null) {
                        if(montoDisponible <= 0) {
                            Notification.show("Monto del pago no cubre el saldo del documento seleccionado.", Notification.Type.WARNING_MESSAGE);
                            montoTxt.focus();
                            facturasGrid.deselectAll();
                            return;
                        }
                        cuentaContable2Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getChequesDevueltos());
                        if(   montoDisponible < Double.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(facturasGrid.getSelectedRow()).getItemProperty(MONTO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", ""))
                           || montoDisponible > Double.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(facturasGrid.getSelectedRow()).getItemProperty(MONTO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", ""))) {
                            Notification.show("Monto del pago diferente al saldo del documento seleccionado.", Notification.Type.WARNING_MESSAGE);
                            montoTxt.focus();
                            facturasGrid.deselectAll();
                        }
                    }

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

        llenarComboCuentaContable();
        llenarComboProveedor();
        //llenarProveedor(); // proveedores

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

        layoutHorizontal2.addComponent(cuentaContable2Cbx);
        layoutHorizontal2.addComponent(debe2Txt);
        layoutHorizontal2.addComponent(haber2Txt);

        layoutHorizontal3.addComponents(salirBtn, guardarBtn);
        layoutHorizontal3.setComponentAlignment(guardarBtn, Alignment.BOTTOM_CENTER);
        layoutHorizontal3.setComponentAlignment(salirBtn, Alignment.BOTTOM_CENTER);

        rightVerticalLayout.addComponent(facturasGrid);
        rightVerticalLayout.setComponentAlignment(facturasGrid, Alignment.TOP_CENTER);
        rightVerticalLayout.addComponent(layoutHorizontal1);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal1, Alignment.MIDDLE_CENTER);

        rightVerticalLayout.addComponent(layoutHorizontal2);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal2, Alignment.MIDDLE_CENTER);

        rightVerticalLayout.addComponent(layoutHorizontal3);
        rightVerticalLayout.setComponentAlignment(layoutHorizontal3, Alignment.MIDDLE_CENTER);

        horizontalLayout.addComponents(leftVerticalLayout, rightVerticalLayout);

        return horizontalLayout;

    }

    public void llenarTablaDocumentosPagar() {

        facturasContainer.removeAllItems();

        queryString = " SELECT * FROM contabilidad_partida ";
        queryString += " WHERE IdNomenclatura = " + ((SopdiUI) mainUI).cuentasContablesDefault.getChequesDevueltos();
        queryString += " AND TipoDocumento = 'NOTA DE DEBITO'";
        queryString += " AND IdProveedor = " + proveedorCbx.getValue();
        queryString += " AND IdEmpresa = " + empresaCbx.getValue();

        Logger.getLogger(this.getClass().getName()).log(Level.INFO,"QUERY BUSCAR CHEQUES DEVUELTOS DE CIENTE : " + queryString);

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
            System.out.println("Error al listar CHEQUES DEVUELTOS : " + ex);
            Notification.show("ERROR AL LEER CHEQUES DEVUELTOS: " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
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
                + debe2Txt.getDoubleValueDoNotThrow()
        ).setScale(2, BigDecimal.ROUND_HALF_UP);

        totalHaber = new BigDecimal(haber1Txt.getDoubleValueDoNotThrow()
                + haber2Txt.getDoubleValueDoNotThrow()
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

        while (iter.hasNext()) {

            Object gridItem = iter.next();

            codigoPartida = String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(CODIGO_PARTIDA_PROPERTY).getValue());

            queryString = " UPDATE  contabilidad_partida ";
            queryString += " SET Saldo = 0";
            queryString += " WHERE CodigoPartida = '" + codigoPartida + "'";
            queryString += " AND IdEmpresa = " + empresaCbx.getValue();
            queryString += " AND IdProveedor = " + proveedorCbx.getValue();

            try {
                stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                stQuery.executeUpdate(queryString);

            } catch (Exception ex1) {
                System.out.println("Error al actualizar el saldo de facturas seleccionadas : " + ex1.getMessage());
                ex1.printStackTrace();
            }
        }

        ingresarPagoDocumentoVenta();
    }

    public void ingresarPagoDocumentoVenta() {

        queryString = " SELECT * FROM contabilidad_partida";
        queryString += " WHERE NumeroDocumento = '" + numeroTxt.getValue().toUpperCase().trim() + "'";
        queryString += " AND IdProveedor     =  " + String.valueOf(proveedorCbx.getValue());
        queryString += " AND IdEmpresa = " + String.valueOf(empresaCbx.getValue());
        queryString += " AND TipoDocumento = '" + String.valueOf(medioCbx.getValue())  + "'";
        queryString += " AND MonedaDocumento = '" + monedaCbx.getValue() + "'";

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

        queryString = " SELECT codigoPartida FROM contabilidad_partida ";
        queryString += " WHERE codigoPartida LIKE '" + codigoPartida + "%'";
        queryString += " ORDER BY codigoPartida DESC ";

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
        queryString += ",'" + proveedorCbx.getContainerProperty(proveedorCbx.getValue(), NOMBRESINCODIGO_PROPERTY).getValue() + "'";
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

        queryString += ",(";
        queryString += String.valueOf(empresaCbx.getValue());
        queryString += ",'INGRESADO'";
        queryString += ",'" + codigoPartida + "'";
        queryString += ",'" + String.valueOf(facturasGrid.getContainerDataSource().getItem(facturasGrid.getSelectedRow()).getItemProperty(CODIGO_CC_PROPERTY).getValue()) + "'";   /// CODIGOCC
        queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
        queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
        queryString += "," + proveedorCbx.getValue();
        queryString += ",'" + proveedorCbx.getContainerProperty(proveedorCbx.getValue(), NOMBRESINCODIGO_PROPERTY).getValue() + "'";
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

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "RE-DEPOSITO CHEQUE DEVUELTO = " + queryString);

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

            Notification notif = new Notification("INGRESO REGISTRADO EXITOSAMENTE.",
                    Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.CHECK);
            notif.show(Page.getCurrent());

            montoTxt.setValue(0.00);

        } catch (Exception ex1) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE,"Error al insertar registro de ingreso: " + ex1.getMessage());
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
                Logger.getLogger(IngresoRedepositoChequeDevueltoForm.class.getName()).log(Level.SEVERE, null, ex);
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
//        haber2Txt.setValue(0.00);

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
