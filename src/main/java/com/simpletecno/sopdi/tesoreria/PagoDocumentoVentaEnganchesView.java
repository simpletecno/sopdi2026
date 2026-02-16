package com.simpletecno.sopdi.tesoreria;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.simpletecno.sopdi.utilerias.ValidarTokenForm;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PagoDocumentoVentaEnganchesView extends VerticalLayout implements View {

    static final String NIT_PROPERTY = "NIT";
    static final String NOMBRESINCODIGO_PROPERTY = "NSC";

    VerticalLayout mainLayout;
    UI mainUI;

    HorizontalLayout layoutTitle;
    ComboBox empresaCbx;
    Label titleLbl;

    Button generarBtn;
    Button guardarBtn;

    ComboBox proveedorCbx;
    DateField fechaDt;
    TextField numeroTxt;
    TextField descripcionTxt;

//    ArrayList<String> codigoEnganches = new ArrayList<String>();

    public IndexedContainer facturasContainer = new IndexedContainer();
    Grid facturasGrid;
    public IndexedContainer enganchesContainer = new IndexedContainer();
    Grid enganchesGrid;

    IndexedContainer partidaContainer = new IndexedContainer();
    Grid partidaGrid;

    HashMap<String, String> cuentasContables = new HashMap<String, String>();

    static final String CODIGO_PARTIDA_PROPERTY = "Codigo Partida";
    static final String CODIGOCC_PROPERTY = "CodigoCC";
    static final String FECHA_DOCUMENTO_PROPERTY = "Fecha";
    static final String NUMERO_DOCUMENTO_PROPERTY = "# Documento";
    static final String MONEDA_DOCUMENTO_PROPERTY = "Moneda";
    static final String MONTO_DOCUMENTO_PROPERTY = "Monto";
    static final String SALDO_DOCUMENTO_PROPERTY = "Saldo";
    static final String TIPO_CAMBIO_PROPERTY = "T.Cambio";

    static final String CUENTA_PROPERTY = "IdNomenclatura";
    static final String DESCRIPCION_PROPERTY = "Descripción";
    static final String HABER_PROPERTY = "Haber";
    static final String HABER_Q_PROPERTY = "Haber Q";
    static final String DEBE_PROPERTY = "Debe";
    static final String DEBE_Q_PROPERTY = "Debe Q";

    Statement stQuery, stQuery2;
    ResultSet rsRecords, rsRecords2;
    String queryString, codigoPartida;
    String variableTemp = "";

    public PagoDocumentoVentaEnganchesView() {

        this.mainUI = UI.getCurrent();
        setResponsive(true);

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

        titleLbl = new Label("PAGO DOCUMENTO VENTAS CON ENGANCHES");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h2_custom");

        empresaCbx.addItem(((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId());
        empresaCbx.setItemCaption(((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId(), "(" + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId() +") " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName() );
        empresaCbx.select(((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId());

        layoutTitle.addComponent(empresaCbx);
        layoutTitle.setComponentAlignment(empresaCbx, Alignment.MIDDLE_LEFT);

        layoutTitle.addComponent(titleLbl);
        layoutTitle.setComponentAlignment(titleLbl, Alignment.BOTTOM_RIGHT);

        mainLayout.addComponent(layoutTitle);

        addComponent(mainLayout);

        createComponents();

        llenarComboCuentaContable();

    }

    private void llenarComboCuentaContable() {
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

    private void llenarComboProveedor() {
        queryString = " SELECT prov.* ";
        queryString += " FROM proveedor prov";
        queryString += " WHERE prov.Inhabilitado = 0 ";
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

    private void createComponents() {

        createDocHeader();
        createDetail();
        createPartida();
    }

    private void createDocHeader() {
        HorizontalLayout docHeaderlLayout = new HorizontalLayout();
        docHeaderlLayout.addStyleName("rcorners2");
        docHeaderlLayout.setSpacing(true);
        docHeaderlLayout.setMargin(true);
        docHeaderlLayout.setWidth("100%");

        proveedorCbx = new ComboBox("Cliente");
        proveedorCbx.addContainerProperty(NIT_PROPERTY, String.class, "");
        proveedorCbx.setInvalidAllowed(false);
        proveedorCbx.setNewItemsAllowed(false);
        proveedorCbx.setNullSelectionAllowed(false);
        proveedorCbx.setWidth("100%");
        proveedorCbx.setFilteringMode(FilteringMode.CONTAINS);
        proveedorCbx.addValueChangeListener((event) -> {
            llenarGrids();
        });
        llenarComboProveedor();

        fechaDt = new DateField("Fecha : ");
        fechaDt.setDateFormat("dd/MM/yyyy");
        fechaDt.setWidth("100%");
        fechaDt.setValue(new Date());

        numeroTxt = new TextField("# Documento interno:");
        numeroTxt.setWidth("100%");

        descripcionTxt = new TextField("Descripción :");
        descripcionTxt.setWidth("100%");
        descripcionTxt.setVisible(false);

        docHeaderlLayout.addComponents(proveedorCbx, fechaDt, numeroTxt, descripcionTxt);

        mainLayout.addComponent(docHeaderlLayout);
    }

    private void createDetail() {
        HorizontalLayout detailLayout = new HorizontalLayout();
        detailLayout.addStyleName("rcorners3");
        detailLayout.setSpacing(true);
        detailLayout.setMargin(true);
        detailLayout.setWidth("100%");

        VerticalLayout facturasLayout = new VerticalLayout();
        facturasLayout.addStyleName("rcorners4");
        facturasLayout.setSpacing(true);
        facturasLayout.setMargin(true);
        facturasLayout.setWidth("100%");

        VerticalLayout enganchesLayout = new VerticalLayout();
        enganchesLayout.addStyleName("rcorners4");
        enganchesLayout.setSpacing(true);
        enganchesLayout.setMargin(true);
        enganchesLayout.setWidth("100%");

        detailLayout.addComponents(facturasLayout, enganchesLayout);
        detailLayout.setComponentAlignment(facturasLayout, Alignment.MIDDLE_LEFT);
        detailLayout.setComponentAlignment(enganchesLayout, Alignment.MIDDLE_RIGHT);

        facturasContainer.addContainerProperty(CODIGO_PARTIDA_PROPERTY, String.class, null);
        facturasContainer.addContainerProperty(CODIGOCC_PROPERTY, String.class, null);
        facturasContainer.addContainerProperty(NUMERO_DOCUMENTO_PROPERTY, String.class, null);
        facturasContainer.addContainerProperty(FECHA_DOCUMENTO_PROPERTY, String.class, null);
        facturasContainer.addContainerProperty(MONEDA_DOCUMENTO_PROPERTY, String.class, null);
        facturasContainer.addContainerProperty(MONTO_DOCUMENTO_PROPERTY, String.class, null);
        facturasContainer.addContainerProperty(SALDO_DOCUMENTO_PROPERTY, String.class, null);
        facturasContainer.addContainerProperty(TIPO_CAMBIO_PROPERTY, String.class, null);

        facturasGrid = new Grid("FACTURAS VENTA", facturasContainer);
        facturasGrid.setWidth("100%");
        facturasGrid.setImmediate(true);
        facturasGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        facturasGrid.setDescription("Seleccione una factura.");
        facturasGrid.setHeightMode(HeightMode.ROW);
        facturasGrid.setHeightByRows(5);
        facturasGrid.setResponsive(true);
        facturasGrid.setEditorBuffered(false);

        facturasGrid.getColumn(CODIGO_PARTIDA_PROPERTY).setHidable(true).setHidden(true);
        facturasGrid.getColumn(CODIGOCC_PROPERTY).setHidable(true).setHidden(true);

        facturasLayout.addComponent(facturasGrid);

        enganchesContainer.addContainerProperty(CODIGO_PARTIDA_PROPERTY, String.class, null);
        enganchesContainer.addContainerProperty(CODIGOCC_PROPERTY, String.class, null);
        enganchesContainer.addContainerProperty(NUMERO_DOCUMENTO_PROPERTY, String.class, null);
        enganchesContainer.addContainerProperty(FECHA_DOCUMENTO_PROPERTY, String.class, null);
        enganchesContainer.addContainerProperty(MONEDA_DOCUMENTO_PROPERTY, String.class, null);
        enganchesContainer.addContainerProperty(MONTO_DOCUMENTO_PROPERTY, String.class, null);
        enganchesContainer.addContainerProperty(SALDO_DOCUMENTO_PROPERTY, String.class, null);
        enganchesContainer.addContainerProperty(TIPO_CAMBIO_PROPERTY, String.class, null);

        enganchesGrid = new Grid("ENGANCHES DE CLIENTE", enganchesContainer);
        enganchesGrid.setWidth("100%");
        enganchesGrid.setImmediate(true);
        enganchesGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        enganchesGrid.setDescription("Seleccione uno o varios enganches.");
        enganchesGrid.setHeightMode(HeightMode.ROW);
        enganchesGrid.setHeightByRows(7);
        enganchesGrid.setResponsive(true);
        enganchesGrid.setEditorBuffered(false);

        enganchesGrid.getColumn(CODIGO_PARTIDA_PROPERTY).setHidable(true).setHidden(true);
        enganchesGrid.getColumn(CODIGOCC_PROPERTY).setHidable(true).setHidden(true);

        enganchesLayout.addComponent(enganchesGrid);

        mainLayout.addComponent(detailLayout);
    }

    private void createPartida() {
        HorizontalLayout partidaLayout = new HorizontalLayout();
        partidaLayout.setWidth("100%");
        partidaLayout.setSpacing(true);
        partidaLayout.setMargin(true);

        partidaLayout.addStyleName("rcorners3");
        partidaLayout.setWidth("90%");
        partidaLayout.setResponsive(true);
        partidaLayout.setSpacing(false);
        partidaLayout.setMargin(false);

        partidaContainer.addContainerProperty(CUENTA_PROPERTY, String.class, "0");
        partidaContainer.addContainerProperty(DESCRIPCION_PROPERTY, String.class, "");
        partidaContainer.addContainerProperty(DEBE_PROPERTY, String.class, "0");
        partidaContainer.addContainerProperty(HABER_PROPERTY, String.class, "0");
        partidaContainer.addContainerProperty(DEBE_Q_PROPERTY, String.class, "0");
        partidaContainer.addContainerProperty(HABER_Q_PROPERTY, String.class, "0");
        partidaContainer.addContainerProperty(CODIGOCC_PROPERTY, String.class, "0");
        partidaContainer.addContainerProperty(TIPO_CAMBIO_PROPERTY, String.class, "1.0");

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

        partidaLayout.addComponent(partidaGrid);
        partidaLayout.setComponentAlignment(partidaGrid, Alignment.MIDDLE_CENTER);

        mainLayout.addComponent(partidaLayout);
        mainLayout.setComponentAlignment(partidaLayout, Alignment.MIDDLE_CENTER);

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

        generarBtn = new Button("Generar partida");
        generarBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        generarBtn.setIcon(FontAwesome.GEARS);
        generarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                generarPartida();
            }
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.addComponents(generarBtn, guardarBtn);
        buttonsLayout.setComponentAlignment(generarBtn, Alignment.BOTTOM_CENTER);
        buttonsLayout.setComponentAlignment(guardarBtn, Alignment.BOTTOM_CENTER);

        mainLayout.addComponent(buttonsLayout);
        mainLayout.setComponentAlignment(buttonsLayout, Alignment.MIDDLE_CENTER);

    }

    private void llenarGrids() {

        facturasContainer.removeAllItems();
        enganchesContainer.removeAllItems();
        partidaContainer.removeAllItems();

        llenarFacturas();
        llenarEnganches();
    }

    private void llenarFacturas() {
        queryString = " SELECT * From contabilidad_partida ";
        queryString += " WHERE IdNomenclatura = " + ((SopdiUI) mainUI).cuentasContablesDefault.getClientes();
        queryString += " And TipoDocumento IN ('FACTURA VENTA', 'RECIBO CONTABLE')";
        queryString += " And IdProveedor = " + proveedorCbx.getValue();
        queryString += " And IdEmpresa = " + empresaCbx.getValue();

        Logger.getLogger(this.getClass().getName()).log(Level.INFO,"QUERY BUSCAR FACTURAS VENTA DE UN PROVEEDOR (CIENTE) : " + queryString);

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                do {

                    // se busca el saldo real...
                    queryString = " SELECT  ";
                    queryString += " SUM(DEBE - HABER) as TOTALSALDO ";
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
                        facturasContainer.getContainerProperty(itemId, CODIGOCC_PROPERTY).setValue(rsRecords.getString("CodigoCC"));
                        facturasContainer.getContainerProperty(itemId, FECHA_DOCUMENTO_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha")));
                        facturasContainer.getContainerProperty(itemId, NUMERO_DOCUMENTO_PROPERTY).setValue(rsRecords.getString("NumeroDocumento") + " " + rsRecords.getString("SerieDocumento"));
                        facturasContainer.getContainerProperty(itemId, MONEDA_DOCUMENTO_PROPERTY).setValue(rsRecords.getString("MonedaDocumento"));
                        facturasContainer.getContainerProperty(itemId, MONTO_DOCUMENTO_PROPERTY).setValue(rsRecords.getString("DEBE"));
                        facturasContainer.getContainerProperty(itemId, SALDO_DOCUMENTO_PROPERTY).setValue(Utileria.numberFormatMoney.format(rsRecords2.getDouble("TOTALSALDO")));
                        facturasContainer.getContainerProperty(itemId, TIPO_CAMBIO_PROPERTY).setValue(rsRecords.getString("TipoCambio"));
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
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error al buscar facturas : " + ex);
            Notification.show("Error al buscar facturas.", Notification.Type.ERROR_MESSAGE);
        }
    }

    private void llenarEnganches() {
        queryString = "  select contabilidad_partida.IdNomenclatura, contabilidad_partida.CodigoPartida, contabilidad_partida.CodigoCC, ";
        queryString += " contabilidad_partida.MonedaDocumento, contabilidad_partida.Fecha, contabilidad_partida.Debe, contabilidad_partida.Haber, ";
        queryString += "contabilidad_partida.NumeroDocumento, proveedor.IdProveedor, proveedor.Nombre, TipoCambio ";
        queryString += " from contabilidad_partida";
        queryString += " inner join proveedor on contabilidad_partida.IdProveedor = proveedor.IDProveedor ";
        queryString += " where contabilidad_partida.IdEmpresa =" + empresaCbx.getValue();
        queryString += " and contabilidad_partida.Fecha >= '2019-01-01'";
        queryString += " and contabilidad_partida.IdProveedor = " + proveedorCbx.getValue();
        queryString += " and contabilidad_partida.Estatus <> 'ANULADO'";
//        queryString += " and contabilidad_partida.IdNomenclatura IN (" + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getEnganches() + "," + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getAnticiposClientes() + ")";
        queryString += " and contabilidad_partida.IdNomenclatura = " + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getEnganches();
        queryString += " Order by contabilidad_partida.Fecha desc";

        Logger.getLogger(this.getClass().getName()).log(Level.INFO,"QUERY BUSCAR ENGANCHES DE UN CLIENTE : " + queryString);

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);
            stQuery2 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            if (rsRecords.next()) { //  encontrado
                do {

                    queryString = " SELECT ";
                    queryString += " SUM(HABER - DEBE) TOTALSALDO";
                    queryString += " FROM contabilidad_partida";
                    queryString += " WHERE IdEmpresa = " + empresaCbx.getValue();
                    queryString += " AND CodigoCC = '" + rsRecords.getString("CodigoCC") + "'";
                    queryString += " and IdNomenclatura = " + rsRecords.getString("IdNomenclatura");

                    rsRecords2 = stQuery2.executeQuery(queryString);

                    if (rsRecords2.next()) {

                        Object itemId;

                        if (rsRecords2.getDouble("TOTALSALDO") > 0.00) {

                            itemId = enganchesContainer.addItem();

                            enganchesContainer.getContainerProperty(itemId, CODIGO_PARTIDA_PROPERTY).setValue(rsRecords.getString("CodigoPartida"));
                            enganchesContainer.getContainerProperty(itemId, CODIGOCC_PROPERTY).setValue(rsRecords.getString("CodigoCC"));
                            enganchesContainer.getContainerProperty(itemId, NUMERO_DOCUMENTO_PROPERTY).setValue(rsRecords.getString("NumeroDocumento"));
                            enganchesContainer.getContainerProperty(itemId, FECHA_DOCUMENTO_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha")));
                            enganchesContainer.getContainerProperty(itemId, MONEDA_DOCUMENTO_PROPERTY).setValue(rsRecords.getString("MonedaDocumento"));
                            enganchesContainer.getContainerProperty(itemId, MONTO_DOCUMENTO_PROPERTY).setValue(Utileria.numberFormatMoney.format(rsRecords.getDouble("Haber")));
                            enganchesContainer.getContainerProperty(itemId, SALDO_DOCUMENTO_PROPERTY).setValue(Utileria.numberFormatMoney.format(rsRecords2.getDouble("TOTALSALDO")));
                            enganchesContainer.getContainerProperty(itemId, TIPO_CAMBIO_PROPERTY).setValue(rsRecords.getString("TipoCambio"));
                        }
                    }
                } while (rsRecords.next());

            }

        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error al buscar ENGANCHES : " + ex);
            Notification.show("Error al buscar ENGANCHES.", Notification.Type.ERROR_MESSAGE);
        }
    }

    private void generarPartida() {
        partidaContainer.removeAllItems();

        Double tipoCambio = 0.00;
        Double saldoFactura = 0.00;
        Double saldoFacturaQ = 0.00;
        Double montoEnganche = 0.00;
        Double montoEngancheQ = 0.00;
        Double totalEnganches = 0.00;
        Object gridItem;
        Double totalDebe = 0.00;
        Double totalHaber = 0.00;;
        Double totalDebeQ = 0.00;;
        Double totalHaberQ = 0.00;;

//        codigoEnganches.clear();

        if(facturasGrid.getSelectedRow() == null) {
            return;
        }

        gridItem = facturasGrid.getSelectedRow();

        saldoFactura = Double.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(SALDO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", ""));
        tipoCambio = Double.valueOf(String.valueOf(facturasGrid.getContainerDataSource().getItem(gridItem).getItemProperty(TIPO_CAMBIO_PROPERTY).getValue()).replaceAll(",", ""));
        saldoFacturaQ = saldoFactura * tipoCambio;

        Iterator enganchesIter = enganchesGrid.getSelectedRows().iterator();

        if (enganchesIter == null || !enganchesIter.hasNext()) {
            return;
        }

        while (enganchesIter.hasNext()) {   // Si hay mas de un registro seleccionado
            gridItem = enganchesIter.next();

            totalEnganches += Double.valueOf(String.valueOf(enganchesGrid.getContainerDataSource().getItem(gridItem).getItemProperty(SALDO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", ""));
//            codigoEnganches.add(String.valueOf(enganchesGrid.getContainerDataSource().getItem(gridItem).getItemProperty(CODIGOCC_PROPERTY).getValue()));
        }

        if(totalEnganches < saldoFactura) {
            Notification.show("EL MONTO DE LOS ENGANCHES NO ES SUFICIENTE PARA FINIQUITAR LA FACTURA. MONTO FACTURA = " + saldoFactura + "  MONTO ENGANCHES = " + totalEnganches);
            return;
        }

        descripcionTxt.setValue("PAGO DOCUMENTO VENTA CON ENGANCHES " + String.valueOf(facturasGrid.getContainerDataSource().getItem(facturasGrid.getSelectedRow()).getItemProperty(NUMERO_DOCUMENTO_PROPERTY).getValue())
            + " CLIENTE [" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "]");

        Object partidaObject = partidaContainer.addItem();
        //LINEA DE LA FACTURA CLIENTE
        partidaContainer.getContainerProperty(partidaObject, CUENTA_PROPERTY).setValue(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getClientes());
        partidaContainer.getContainerProperty(partidaObject, DESCRIPCION_PROPERTY).setValue(cuentasContables.get(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getClientes()));
        partidaContainer.getContainerProperty(partidaObject, DEBE_PROPERTY).setValue("0.00");
        partidaContainer.getContainerProperty(partidaObject, HABER_PROPERTY).setValue(String.valueOf(saldoFactura));
        partidaContainer.getContainerProperty(partidaObject, DEBE_Q_PROPERTY).setValue("0.00");
        partidaContainer.getContainerProperty(partidaObject, HABER_Q_PROPERTY).setValue(String.valueOf(saldoFacturaQ));
        partidaContainer.getContainerProperty(partidaObject, TIPO_CAMBIO_PROPERTY).setValue(String.valueOf(facturasGrid.getContainerDataSource().getItem(facturasGrid.getSelectedRow()).getItemProperty(TIPO_CAMBIO_PROPERTY).getValue()));
        partidaContainer.getContainerProperty(partidaObject, CODIGOCC_PROPERTY).setValue(String.valueOf(facturasGrid.getContainerDataSource().getItem(facturasGrid.getSelectedRow()).getItemProperty(CODIGOCC_PROPERTY).getValue()));

        totalHaber = saldoFactura;
        totalHaberQ = saldoFacturaQ;

        enganchesIter = enganchesGrid.getSelectedRows().iterator();

        // POR CADA ENGANCHE
        while (enganchesIter.hasNext() && saldoFactura > 0) {

            gridItem = enganchesIter.next();

            partidaObject = partidaContainer.addItem();

            montoEnganche = Double.valueOf(String.valueOf(enganchesGrid.getContainerDataSource().getItem(gridItem).getItemProperty(SALDO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", ""));
            tipoCambio = Double.valueOf(String.valueOf(enganchesGrid.getContainerDataSource().getItem(gridItem).getItemProperty(TIPO_CAMBIO_PROPERTY).getValue()).replaceAll(",", ""));

            saldoFactura = saldoFactura - montoEnganche;

            if(saldoFactura < 0) {
                montoEnganche = saldoFactura;
            }

            montoEngancheQ = montoEnganche * tipoCambio;

            partidaContainer.getContainerProperty(partidaObject, CUENTA_PROPERTY).setValue(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getEnganches());
            partidaContainer.getContainerProperty(partidaObject, DESCRIPCION_PROPERTY).setValue(cuentasContables.get(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getEnganches()));
            partidaContainer.getContainerProperty(partidaObject, DEBE_PROPERTY).setValue(String.valueOf(montoEnganche));
            partidaContainer.getContainerProperty(partidaObject, HABER_PROPERTY).setValue("0.00");
            partidaContainer.getContainerProperty(partidaObject, DEBE_Q_PROPERTY).setValue(String.valueOf(montoEngancheQ));
            partidaContainer.getContainerProperty(partidaObject, HABER_Q_PROPERTY).setValue("0.00");
            partidaContainer.getContainerProperty(partidaObject, TIPO_CAMBIO_PROPERTY).setValue(String.valueOf(enganchesGrid.getContainerDataSource().getItem(gridItem).getItemProperty(TIPO_CAMBIO_PROPERTY).getValue()));
            partidaContainer.getContainerProperty(partidaObject, CODIGOCC_PROPERTY).setValue(String.valueOf(enganchesGrid.getContainerDataSource().getItem(gridItem).getItemProperty(CODIGOCC_PROPERTY).getValue()));

            totalDebe += montoEnganche;
            totalDebeQ += montoEngancheQ;

        }// end while iterator enganches seleccionados

        //diferencial cambiario

//        if( (totalDebeQ.doubleValue() != totalHaberQ.doubleValue())) { // si hay diferencial cambiario
//
//            partidaObject = partidaContainer.addItem();
//
//            partidaContainer.getContainerProperty(partidaObject, CUENTA_PROPERTY).setValue(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getDiferencialCambiario());
//            partidaContainer.getContainerProperty(partidaObject, DESCRIPCION_PROPERTY).setValue("DIFERENCIAL CAMBIARIO");
//
//            partidaContainer.getContainerProperty(partidaObject, DEBE_PROPERTY).setValue("0");
//            partidaContainer.getContainerProperty(partidaObject, HABER_PROPERTY).setValue("0");
//
//            if((totalDebeQ.doubleValue() - totalHaberQ.doubleValue()) > 0 ) {
//                partidaContainer.getContainerProperty(partidaObject, DEBE_Q_PROPERTY).setValue(String.valueOf(Utileria.numberFormatEntero.format(totalDebeQ.doubleValue() - totalHaberQ.doubleValue())));
//                totalDebeQ = totalDebeQ.add(new BigDecimal(Utileria.numberFormatEntero.format(totalDebeQ.doubleValue() - totalHaberQ.doubleValue())));
//            }
//            else {
//                partidaContainer.getContainerProperty(partidaObject, DEBE_Q_PROPERTY).setValue(String.valueOf(Utileria.numberFormatEntero.format((totalDebeQ.doubleValue() - totalHaberQ.doubleValue()) * -1)));
//                totalDebeQ = totalDebeQ.add(new BigDecimal(Utileria.numberFormatEntero.format((totalDebeQ.doubleValue() - totalHaberQ.doubleValue()) * -1)));
//            }
//
//            partidaContainer.getContainerProperty(partidaObject, HABER_Q_PROPERTY).setValue("0");
//            partidaContainer.getContainerProperty(partidaObject, CODIGOCC_PROPERTY).setValue("");
//
//        }

        partidaObject = partidaContainer.addItem();

        partidaContainer.getContainerProperty(partidaObject, CUENTA_PROPERTY).setValue("__________");
        partidaContainer.getContainerProperty(partidaObject, DESCRIPCION_PROPERTY).setValue("__________");
        partidaContainer.getContainerProperty(partidaObject, DEBE_PROPERTY).setValue("____________");
        partidaContainer.getContainerProperty(partidaObject, HABER_PROPERTY).setValue("___________");
        partidaContainer.getContainerProperty(partidaObject, DEBE_Q_PROPERTY).setValue("___________");
        partidaContainer.getContainerProperty(partidaObject, HABER_Q_PROPERTY).setValue("___________");
        partidaContainer.getContainerProperty(partidaObject, CODIGOCC_PROPERTY).setValue("___________");

        partidaObject = partidaContainer.addItem();

        partidaContainer.getContainerProperty(partidaObject, CUENTA_PROPERTY).setValue("");
        partidaContainer.getContainerProperty(partidaObject, DESCRIPCION_PROPERTY).setValue("--------> SUMAS IGUALES");
        partidaContainer.getContainerProperty(partidaObject, DEBE_PROPERTY).setValue(Utileria.numberFormatMoney.format(totalDebe));
        partidaContainer.getContainerProperty(partidaObject, HABER_PROPERTY).setValue(Utileria.numberFormatMoney.format(totalHaber));
        partidaContainer.getContainerProperty(partidaObject, DEBE_Q_PROPERTY).setValue(Utileria.numberFormatMoney.format(totalDebeQ));
        partidaContainer.getContainerProperty(partidaObject, HABER_Q_PROPERTY).setValue(Utileria.numberFormatMoney.format(totalHaberQ));
        partidaContainer.getContainerProperty(partidaObject, CODIGOCC_PROPERTY).setValue("___________");

        partidaObject = partidaContainer.addItem();

        partidaContainer.getContainerProperty(partidaObject, CUENTA_PROPERTY).setValue("__________");
        partidaContainer.getContainerProperty(partidaObject, DESCRIPCION_PROPERTY).setValue("__________");
        partidaContainer.getContainerProperty(partidaObject, DEBE_PROPERTY).setValue("____________");
        partidaContainer.getContainerProperty(partidaObject, HABER_PROPERTY).setValue("___________");
        partidaContainer.getContainerProperty(partidaObject, DEBE_Q_PROPERTY).setValue("___________");
        partidaContainer.getContainerProperty(partidaObject, HABER_Q_PROPERTY).setValue("___________");
        partidaContainer.getContainerProperty(partidaObject, CODIGOCC_PROPERTY).setValue("___________");
    }

    public boolean validarCamposParaIngresarPagoDocumentos() {

        boolean error = false;

        if (proveedorCbx.getValue() == null) {
            Notification.show("Por favor, seleccione un cliente..", Notification.Type.ERROR_MESSAGE);
            proveedorCbx.focus();
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

//            System.out.println("DIAS DE DIFERENCIA ENTRE FACTURA Y FECHA ACTUAL : " + dias);

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

        codigoPartida = String.valueOf(facturasGrid.getContainerDataSource().getItem(facturasGrid.getSelectedRow()).getItemProperty(CODIGO_PARTIDA_PROPERTY).getValue());

        queryString = " Update  contabilidad_partida ";
        queryString += " Set Saldo = 0";
        queryString += " Where CodigoPartida = '" + codigoPartida + "'";
        queryString += " And IdEmpresa = " + empresaCbx.getValue();
        queryString += " And IdProveedor = " + proveedorCbx.getValue();

//        System.out.println("Query actualizar saldo de facturas :" + queryString);

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

        } catch (Exception ex1) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error al actualizar el saldo de facturas seleccionadas : " + ex1.getMessage());
            Notification.show("Error al actualizar saldo de factura venta.", Notification.Type.ERROR_MESSAGE);
            return;
        }

        /// SI HAY ENGANCHES ACTUALIZAR SALDO

//        if (codigoEnganches.size() > 0) {
//            for (int i = 0; i < codigoEnganches.size(); i++) {
//
//                queryString = " Update  contabilidad_partida ";
//                queryString += " Set Saldo = 0";
//                queryString += " Where CodigoPartida = '" + codigoEnganches.get(i) + "'";
//                queryString += " And IdEmpresa = " + empresaCbx.getValue();
//                queryString += " And IdProveedor = " + proveedorCbx.getValue();
//
//                System.out.println("Query actualizar saldo de enganches :" + queryString);
//                try {
//                    stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
//                    stQuery.executeUpdate(queryString);
//                } catch (Exception ex1) {
//                    System.out.println("Error al actualizar el saldo de los enganches" + ex1.getMessage());
//                    ex1.printStackTrace();
//                }
//            }
//        }

        ingresarPagoDocumentoVenta();
    }

    public void ingresarPagoDocumentoVenta() {

        queryString = " Select * from contabilidad_partida";
        queryString += " Where NumeroDocumento = '" + numeroTxt.getValue().toUpperCase().trim() + "'";
        queryString += " And IdProveedor     =  " + String.valueOf(proveedorCbx.getValue());
        queryString += " And IdEmpresa = " + String.valueOf(empresaCbx.getValue());
        queryString += " And TipoDocumento = 'PAGO DOCUMENTO VENTA'";
        queryString += " And MonedaDocumento = 'DOLARES'";

//        System.out.println("\n\nQuery=" + queryString + "\n\n");

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
                queryString += ",'PAGO DOCUMENTO VENTA'";
                queryString += ",'" + String.valueOf(facturasGrid.getContainerDataSource().getItem(facturasGrid.getSelectedRow()).getItemProperty(NUMERO_DOCUMENTO_PROPERTY).getValue()) + "'";//NODOCA
                queryString += ",'FACTURA VENTA'";//TIPODOCA
                queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
                queryString += "," + proveedorCbx.getValue();
                queryString += ",''"; //nit proveedor
                queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
                queryString += ",''";
                queryString += "," + String.valueOf(facturasGrid.getContainerDataSource().getItem(facturasGrid.getSelectedRow()).getItemProperty(SALDO_DOCUMENTO_PROPERTY).getValue()).replaceAll(",", "");
                queryString += ",''"; //serie documento
                queryString += ",'" + numeroTxt.getValue() + "'";
                queryString += "," + String.valueOf(item.getItemProperty(CUENTA_PROPERTY).getValue());
                queryString += ",'DOLARES'";
                queryString += "," + String.valueOf(item.getItemProperty(DEBE_PROPERTY).getValue());  //Debe
                queryString += "," + String.valueOf(item.getItemProperty(HABER_PROPERTY).getValue()); //Haber
                queryString += "," + String.valueOf(item.getItemProperty(DEBE_Q_PROPERTY).getValue()); //DEBE Q
                queryString += "," + String.valueOf(item.getItemProperty(HABER_Q_PROPERTY).getValue()); //HABER Q
                queryString += "," + String.valueOf(item.getItemProperty(TIPO_CAMBIO_PROPERTY).getValue());
                queryString += ",'" + descripcionTxt.getValue() + "'";
                queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                queryString += ",current_timestamp";
                queryString += "),";
            }
        }
        queryString = queryString.substring(0, queryString.length()-1);

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

            llenarGrids();

        } catch (Exception ex1) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error al insertar pago documento venta: " + ex1.getMessage() + " query="+queryString);

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
                Logger.getLogger(PagoDocumentoVentaEnganchesView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
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

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - Pago doc venta anticipos");
    }
}
