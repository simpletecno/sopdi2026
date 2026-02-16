package com.simpletecno.sopdi.tesoreria;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.simpletecno.sopdi.ventas.FacturaVentaView;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.SelectionEvent;
import com.vaadin.event.SelectionEvent.SelectionListener;
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
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author user
 */
public class PagoFacturaVentaForm extends Window {

    VerticalLayout mainLayout;

    public IndexedContainer container = new IndexedContainer();
    Grid anticiposGrind;
    Grid.FooterRow anticiposFooter;

    double totalDebeQuetzales = 0.00, totalHaberQueztales = 0.00;
    double saldo = 0.00;

    static final String ID_PROVEEDOR = "Proveedor";
    static final String PROVEEDOR = "Nombre";
    static final String FECHA = "Fecha";
    static final String CODIGO_PARTIDA = "Codigo";
    static final String CODIGO_CC = "CodigoCC";
    static final String DEBE_QUETZALES = "Debe Q.";
    static final String HABER_QUETZALES = "Haber Q.";
    static final String SALDO_Q = "Saldo Q.";

    DateField fechaDt;

    TextField nombreChequeTxt;
    TextField descripcionTxt;
    TextField numeroTxt;

    NumberField montoTxt;
    NumberField tasaCambioTxt;

    TextField codigo1Txt;
    TextField codigo2Txt;
    TextField codigo3Txt;
    TextField codigo4Txt;
    TextField codigo5Txt;

    NumberField debe1Txt;
    NumberField debe2Txt;
    NumberField debe3Txt;
    NumberField debe4Txt;
    NumberField debe5Txt;

    NumberField haber1Txt;
    NumberField haber2Txt;
    NumberField haber3Txt;
    NumberField haber4Txt;
    NumberField haber5Txt;

    ComboBox empresaCbx;
    ComboBox monedaCbx;
    ComboBox medioCbx;
    ComboBox cuentaContable1Cbx;
    ComboBox cuentaContable2Cbx;
    ComboBox cuentaContable3Cbx;
    ComboBox cuentaContable4Cbx;
    ComboBox cuentaContable5Cbx;

    HorizontalLayout chequeLayout = new HorizontalLayout();
    HorizontalLayout chequeLayout2 = new HorizontalLayout();
    VerticalLayout partidaLayout = new VerticalLayout();

    Button grabarPartidaBtn;

    Label titleLbl;
    Label title2Lbl;

    UI mainUI;
    Statement stQuery, stQuery1;
    ResultSet rsRecords, rsRecords1;
    String queryString;

    String codigoPartidaNuevo;
    String proveedorId = "";
    String nombreProveedor = "";

    BigDecimal totalDebe;
    BigDecimal totalHaber;


    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");
    static DecimalFormat numberFormat3 = new DecimalFormat("######0.00");

    String codigoCCFactura,factura, idproveedor,nombre;

    double saldo_quetzales;

    public PagoFacturaVentaForm(String codigoCCFactura, String factura, String idproveedor, String nombre, double saldo_quetzales) {

        this.mainUI = UI.getCurrent();
        this.codigoCCFactura = codigoCCFactura;
        this.factura = factura;
        this.idproveedor= idproveedor;
        this.nombre= nombre;
        this.saldo_quetzales = saldo_quetzales;

        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setResponsive(true);

        setContent(mainLayout);

        setResponsive(true);
        setWidth("90%");
        setHeight("90%");

        titleLbl = new Label("");
        titleLbl.setValue("PAGO DE FACTURA N# " + factura + " DEL CLIENTE");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h2_custom");

        title2Lbl = new Label("");
        title2Lbl.setValue(""+nombre + " POR Q." +  saldo_quetzales);
        title2Lbl.addStyleName(ValoTheme.LABEL_H2);
        title2Lbl.setSizeUndefined();
        title2Lbl.addStyleName("h2_custom");

        empresaCbx = new ComboBox("Empresa:");
        empresaCbx.setWidth("400px");
        empresaCbx.addStyleName(ValoTheme.COMBOBOX_HUGE);
        empresaCbx.setInvalidAllowed(false);
        empresaCbx.setNewItemsAllowed(false);
        empresaCbx.setTextInputAllowed(false);
        empresaCbx.setNullSelectionAllowed(false);
        llenarComboEmpresa();

        VerticalLayout titleLayout2 = new VerticalLayout();
        titleLayout2.setResponsive(true);
        titleLayout2.setSpacing(true);
        titleLayout2.setWidth("100%");
        titleLayout2.setMargin(false);
        titleLayout2.addComponents(titleLbl, title2Lbl);
        titleLayout2.setComponentAlignment(titleLbl, Alignment.MIDDLE_CENTER);
        titleLayout2.setComponentAlignment(title2Lbl, Alignment.MIDDLE_CENTER);
        titleLayout2.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setResponsive(true);
        titleLayout.setSpacing(true);
        titleLayout.setWidth("100%");
        titleLayout.setMargin(false);
        titleLayout.addComponents(empresaCbx, titleLayout2);
        titleLayout.setComponentAlignment(empresaCbx, Alignment.MIDDLE_CENTER);
        titleLayout.setComponentAlignment(titleLayout2, Alignment.MIDDLE_CENTER);
        titleLayout.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

        mainLayout.addComponent(titleLayout);
        mainLayout.setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        createTablaAnticipos();
        llenarTablaAnticipos();
        crearLayoutCheque();
        crearPartidaLayout();

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

    public void createTablaAnticipos() {

        HorizontalLayout reportLayout = new HorizontalLayout();
        reportLayout.setWidth("96%");
        reportLayout.addStyleName("rcorners3");
        reportLayout.setResponsive(true);
        reportLayout.setMargin(true);

        container.addContainerProperty(ID_PROVEEDOR, String.class, null);
        container.addContainerProperty(PROVEEDOR, String.class, null);
        container.addContainerProperty(FECHA, String.class, null);
        container.addContainerProperty(CODIGO_PARTIDA, String.class, null);
        container.addContainerProperty(CODIGO_CC, String.class, null);
        container.addContainerProperty(DEBE_QUETZALES, String.class, null);
        container.addContainerProperty(HABER_QUETZALES, String.class, null);
        container.addContainerProperty(SALDO_Q, String.class, null);

        anticiposGrind = new Grid("Anticipos del cliente", container);
        anticiposGrind.setWidth("100%");
        anticiposGrind.setImmediate(true);
        anticiposGrind.setSelectionMode(Grid.SelectionMode.MULTI);
        anticiposGrind.setDescription("Seleccione uno o varios registros del cliente.");
        anticiposGrind.setHeightMode(HeightMode.ROW);
        anticiposGrind.setHeightByRows(5);
        anticiposGrind.setResponsive(true);
        anticiposGrind.setEditorBuffered(false);
        anticiposGrind.addSelectionListener(
                new SelectionListener() {
                    @Override
                    public void select(SelectionEvent event) {
                        if (anticiposGrind.getSelectedRows() != null) {

                            Object gridItem;
                            proveedorId = "";
                            nombreProveedor = "";
                            double montoTotalSeleccionado = 0.00;

                            Iterator iter = event.getSelected().iterator();

                            if (iter == null) {
                                limpiarPartida();

                                nombreChequeTxt.setReadOnly(false);
                                nombreChequeTxt.setValue("");

                                montoTxt.setReadOnly(false);
                                montoTxt.setValue(montoTotalSeleccionado);
                                montoTxt.setReadOnly(true);
                                return;
                            }
                            if (!iter.hasNext()) {
                                limpiarPartida();

                                nombreChequeTxt.setReadOnly(false);
                                nombreChequeTxt.setValue("");

                                montoTxt.setReadOnly(false);
                                montoTxt.setValue(montoTotalSeleccionado);
                                montoTxt.setReadOnly(true);
                                return;
                            }

                            gridItem = iter.next();
                            proveedorId = String.valueOf(anticiposGrind.getContainerDataSource().getItem(gridItem).getItemProperty(ID_PROVEEDOR).getValue());
                            nombreProveedor = String.valueOf(anticiposGrind.getContainerDataSource().getItem(gridItem).getItemProperty(PROVEEDOR).getValue());
                            montoTotalSeleccionado = Double.valueOf(String.valueOf(anticiposGrind.getContainerDataSource().getItem(gridItem).getItemProperty(SALDO_Q).getValue()).replaceAll(",", ""));

                            if (montoTotalSeleccionado> saldo_quetzales ){
                                Notification.show("EL MONTO DE LOS ANTICIPOS NO PUEDE SER MAYOR Al SALDO DE LA FACTURA", Notification.Type.WARNING_MESSAGE);
                                anticiposGrind.deselect(gridItem);
                                return;
                            }

                            while (iter.hasNext()) { //// Si hay mas de un registro seleccionado
                                gridItem = iter.next();
                                montoTotalSeleccionado += Double.valueOf(String.valueOf(anticiposGrind.getContainerDataSource().getItem(gridItem).getItemProperty(SALDO_Q).getValue()).replaceAll(",", ""));

                                if (montoTotalSeleccionado> saldo_quetzales ){
                                    Notification.show("EL MONTO DE LOS ANTICIPOS NO PUEDE SER MAYOR AL SALDO DE LA FACTURA", Notification.Type.WARNING_MESSAGE);
                                    anticiposGrind.deselect(gridItem);
                                    return;
                                }
                            }
                            limpiarPartida();

                            Iterator iter2 = anticiposGrind.getSelectedRows().iterator();

                            double montoEnganche = 0.00;
                            String codigoCC = "";

                            while (iter2.hasNext()) {  // POR CADA FACTURA QUE ESTAMOS SELECCINANDO

                                Object gridItem2 = iter2.next();
                                montoEnganche = Double.valueOf(String.valueOf(anticiposGrind.getContainerDataSource().getItem(gridItem2).getItemProperty(SALDO_Q).getValue()).replaceAll(",", ""));
                                codigoCC = String.valueOf(anticiposGrind.getContainerDataSource().getItem(gridItem2).getItemProperty(CODIGO_CC).getValue());

                                montoTxt.setReadOnly(false);
                                montoTxt.setValue(montoTotalSeleccionado);
                                montoTxt.setReadOnly(true);
                                nombreChequeTxt.setReadOnly(false);
                                nombreChequeTxt.setValue(nombreProveedor);
                                nombreChequeTxt.setReadOnly(true);

                                monedaCbx.setReadOnly(true);

                                try {
                                    //// Anticipos Seleccionados
                                    if (cuentaContable1Cbx.getValue() == null) {
                                        cuentaContable1Cbx.setValue(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getAnticiposClientes());
                                        debe1Txt.setValue(montoEnganche);
                                        codigo1Txt.setValue(codigoCC);
                                    } else if (cuentaContable2Cbx.getValue() == null) {
                                        cuentaContable2Cbx.setValue(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getAnticiposClientes());
                                        debe2Txt.setValue(montoEnganche);
                                        codigo2Txt.setValue(codigoCC);
                                    } else if (cuentaContable3Cbx.getValue() == null) {
                                        cuentaContable3Cbx.setValue(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getAnticiposClientes());
                                        debe3Txt.setValue(montoEnganche);
                                        codigo3Txt.setValue(codigoCC);
                                    } else if (cuentaContable4Cbx.getValue() == null) {
                                        cuentaContable4Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getAnticiposClientes());
                                        debe4Txt.setValue(montoEnganche);
                                        codigo4Txt.setValue(codigoCC);
                                    }

                                } catch (Exception ex) {
                                    System.out.println("Error " + ex);
                                }
                            }

                            if (cuentaContable2Cbx.getValue() == null) {
                                cuentaContable2Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getClientes());
                                haber2Txt.setValue(montoTotalSeleccionado);
                                codigo2Txt.setValue(codigoCCFactura);
                            } else if (cuentaContable3Cbx.getValue() == null) {
                                cuentaContable3Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getClientes());
                                haber3Txt.setValue(montoTotalSeleccionado);
                                codigo3Txt.setValue(codigoCCFactura);
                            } else if (cuentaContable4Cbx.getValue() == null) {
                                cuentaContable4Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getClientes());
                                haber4Txt.setValue(montoTotalSeleccionado);
                                codigo4Txt.setValue(codigoCCFactura);
                            } else if (cuentaContable5Cbx.getValue() == null) {
                                cuentaContable5Cbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getClientes());
                                haber5Txt.setValue(montoTotalSeleccionado);
                                codigo5Txt.setValue(codigoCCFactura);
                            }
                        }
                    }
                }
        );

        anticiposFooter = anticiposGrind.appendFooterRow();
        anticiposFooter.getCell(CODIGO_CC).setText("TOTAL");
        anticiposFooter.getCell(DEBE_QUETZALES).setText("0.00");
        anticiposFooter.getCell(HABER_QUETZALES).setText("0.00");
        anticiposFooter.getCell(SALDO_Q).setText("0.00");

        anticiposFooter.getCell(DEBE_QUETZALES).setStyleName("rightalign");
        anticiposFooter.getCell(HABER_QUETZALES).setStyleName("rightalign");
        anticiposFooter.getCell(SALDO_Q).setStyleName("rightalign");

        anticiposGrind.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if (DEBE_QUETZALES.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (HABER_QUETZALES.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (SALDO_Q.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }
        });

        reportLayout.addComponent(anticiposGrind);
        reportLayout.setComponentAlignment(anticiposGrind, Alignment.MIDDLE_CENTER);

        mainLayout.addComponent(reportLayout);
        mainLayout.setComponentAlignment(reportLayout, Alignment.TOP_CENTER);
    }

    public void llenarTablaAnticipos() {

        container.removeAllItems();
        anticiposGrind.getSelectedRows().clear();
        anticiposGrind.getSelectionModel().reset();

        queryString = "  select contabilidad_partida.CodigoPartida, contabilidad_partida.CodigoCC, contabilidad_partida.Fecha,";
        queryString += " contabilidad_partida.DebeQuetzales, contabilidad_partida.HaberQuetzales, proveedor.IdProveedor, proveedor.Nombre ";
        queryString += " from contabilidad_partida";
        queryString += " inner join proveedor on contabilidad_partida.IdProveedor = proveedor.IDProveedor ";
        queryString += " where contabilidad_partida.IdEmpresa =" + empresaCbx.getValue();
        queryString += " and contabilidad_partida.Fecha >= '2019-01-01'";
        queryString += " and contabilidad_partida.IdProveedor = " + idproveedor;
        queryString += " and contabilidad_partida.Estatus = 'REVISADO'";
        queryString += " and contabilidad_partida.IdNomenclatura = " + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getAnticiposClientes();
        queryString += " Order by contabilidad_partida.Fecha desc";

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);
            stQuery1 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            if (rsRecords.next()) { //  encontrado
                do {

                    queryString = " SELECT ";
                    queryString += " SUM(HABER - DEBE) TOTALSALDO, SUM(HaberQuetzales - DebeQuetzales) TOTALSALDOQ ";
                    queryString += " FROM contabilidad_partida";
                    queryString += " WHERE IdEmpresa = " + empresaCbx.getValue();
                    queryString += " AND CodigoCC = '" + rsRecords.getString("CodigoCC") + "'";
                    queryString += " and IdNomenclatura = " + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getAnticiposClientes();

                    rsRecords1 = stQuery1.executeQuery(queryString);

                    if (rsRecords1.next()) {

                        if (rsRecords1.getDouble("TOTALSALDO") > 0.00) {

                            Object itemId = container.addItem();

                            container.getContainerProperty(itemId, CODIGO_PARTIDA).setValue(rsRecords.getString("CodigoPartida"));
                            container.getContainerProperty(itemId, CODIGO_CC).setValue(rsRecords.getString("CodigoCC"));
                            container.getContainerProperty(itemId, FECHA).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha")));
                            container.getContainerProperty(itemId, ID_PROVEEDOR).setValue(rsRecords.getString("proveedor.IdProveedor"));
                            container.getContainerProperty(itemId, PROVEEDOR).setValue(rsRecords.getString("proveedor.Nombre"));
                            container.getContainerProperty(itemId, DEBE_QUETZALES).setValue(numberFormat.format((rsRecords.getDouble("DebeQuetzales"))));
                            container.getContainerProperty(itemId, HABER_QUETZALES).setValue(numberFormat.format((rsRecords.getDouble("HaberQuetzales"))));
                            container.getContainerProperty(itemId, SALDO_Q).setValue(numberFormat.format((rsRecords1.getDouble("TOTALSALDOQ"))));

                            totalDebeQuetzales = totalDebeQuetzales + rsRecords.getDouble("DebeQuetzales");
                            totalHaberQueztales = totalHaberQueztales + rsRecords.getDouble("HaberQuetzales");
                            saldo = saldo + rsRecords1.getDouble("TOTALSALDOQ");
                        }
                    }
                } while (rsRecords.next());

                anticiposFooter.getCell(DEBE_QUETZALES).setText(numberFormat.format(totalDebeQuetzales));
                anticiposFooter.getCell(HABER_QUETZALES).setText(numberFormat.format(totalHaberQueztales));
                anticiposFooter.getCell(SALDO_Q).setText(numberFormat.format(saldo));
            }

        } catch (Exception ex) {
            System.out.println("Error al listar tabla nomenclatura contable:" + ex);
            ex.printStackTrace();
        }
    }

    private void crearLayoutCheque() {

        chequeLayout.setSpacing(true);
        chequeLayout.setMargin(false);
        chequeLayout.setSizeUndefined();

        chequeLayout2.setSpacing(true);
        chequeLayout2.setMargin(false);
        chequeLayout2.setSizeUndefined();

        numeroTxt = new TextField("# Documento : ");
        numeroTxt.setWidth("8em");

        medioCbx = new ComboBox("Medio : ");
        medioCbx.setWidth("10em");
        medioCbx.addItem("DEPOSITO");
        medioCbx.addItem("NOTA DE CREDITO");
        medioCbx.select("DEPOSITO");

        monedaCbx = new ComboBox("Moneda : ");
        monedaCbx.setWidth("10em");
        monedaCbx.addItem("QUETZALES");
        monedaCbx.addItem("DOLARES");
        monedaCbx.select("QUETZALES");
        monedaCbx.addValueChangeListener((event) -> {
            if (monedaCbx.getValue().equals("DOLARES")) {
                tasaCambioTxt.setValue((Float.toString(((SopdiUI) UI.getCurrent()).sessionInformation.getFltExchangeRate())));
            }else{
                tasaCambioTxt.setValue(1.00);
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
        fechaDt.setValue(new java.util.Date());

        nombreChequeTxt = new TextField("Nombre cheque/transf. : ");
        nombreChequeTxt.setWidth("20em");
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
//        layoutHorizontal6.setMargin(new MarginInfo(true,false,false,false));
        layoutHorizontal6.setMargin(false);
        layoutHorizontal6.setWidth("90%");

        codigo1Txt = new TextField("");
        codigo1Txt.setWidth("2em");
        codigo1Txt.setVisible(false);
        codigo1Txt.setValue("");

        codigo2Txt = new TextField("");
        codigo2Txt.setWidth("2em");
        codigo2Txt.setVisible(false);
        codigo2Txt.setValue("");

        codigo3Txt = new TextField("");
        codigo3Txt.setWidth("2em");
        codigo3Txt.setVisible(false);
        codigo3Txt.setValue("");

        codigo4Txt = new TextField("");
        codigo4Txt.setWidth("2em");
        codigo4Txt.setVisible(false);
        codigo4Txt.setValue("");

        codigo5Txt = new TextField("");
        codigo5Txt.setWidth("2em");
        codigo5Txt.setVisible(false);
        codigo5Txt.setValue("");

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

        grabarPartidaBtn = new Button("Grabar");
        grabarPartidaBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        grabarPartidaBtn.setIcon(FontAwesome.SAVE);
        grabarPartidaBtn.addClickListener((event) -> {
            insertarPartidaSimple();
        });

        Button cancelarBtn = new Button("Cancelar");
        cancelarBtn.addStyleName(ValoTheme.BUTTON_DANGER);
        cancelarBtn.setIcon(FontAwesome.BAN);
        cancelarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                limpiarPartida();
            }
        });

        layoutHorizontal1.addComponent(codigo1Txt);
        layoutHorizontal1.addComponent(cuentaContable1Cbx);
        layoutHorizontal1.addComponent(debe1Txt);
        layoutHorizontal1.addComponent(haber1Txt);

        layoutHorizontal2.addComponent(codigo2Txt);
        layoutHorizontal2.addComponent(cuentaContable2Cbx);
        layoutHorizontal2.addComponent(debe2Txt);
        layoutHorizontal2.addComponent(haber2Txt);

        layoutHorizontal3.addComponent(codigo3Txt);
        layoutHorizontal3.addComponent(cuentaContable3Cbx);
        layoutHorizontal3.addComponent(debe3Txt);
        layoutHorizontal3.addComponent(haber3Txt);

        layoutHorizontal4.addComponent(codigo4Txt);
        layoutHorizontal4.addComponent(cuentaContable4Cbx);
        layoutHorizontal4.addComponent(debe4Txt);
        layoutHorizontal4.addComponent(haber4Txt);

        layoutHorizontal5.addComponent(codigo5Txt);
        layoutHorizontal5.addComponent(cuentaContable5Cbx);
        layoutHorizontal5.addComponent(debe5Txt);
        layoutHorizontal5.addComponent(haber5Txt);

        layoutHorizontal6.addComponent(cancelarBtn);
        layoutHorizontal6.setComponentAlignment(cancelarBtn, Alignment.BOTTOM_LEFT);
        layoutHorizontal6.addComponent(grabarPartidaBtn);
        layoutHorizontal6.setComponentAlignment(grabarPartidaBtn, Alignment.BOTTOM_RIGHT);

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

        mainLayout.addComponent(partidaLayout);
        mainLayout.setComponentAlignment(partidaLayout, Alignment.MIDDLE_CENTER);

    }

    public void llenarComboCuentaContable() {
        String queryString = " SELECT * from contabilidad_nomenclatura ";
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
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar cuentas contables: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void insertarPartidaSimple() {

        if (anticiposGrind.getSelectedRows() == null) {
            Notification.show("Por favor seleccione un registro!", Notification.Type.WARNING_MESSAGE);
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

        totalDebe = new BigDecimal(debe1Txt.getDoubleValueDoNotThrow()
                + debe2Txt.getDoubleValueDoNotThrow() + debe3Txt.getDoubleValueDoNotThrow()
                + debe4Txt.getDoubleValueDoNotThrow() + debe5Txt.getDoubleValueDoNotThrow()
        ).setScale(2, BigDecimal.ROUND_HALF_UP);

        totalHaber = new BigDecimal(haber1Txt.getDoubleValueDoNotThrow()
                + haber2Txt.getDoubleValueDoNotThrow() + haber3Txt.getDoubleValueDoNotThrow()
                + haber4Txt.getDoubleValueDoNotThrow() + haber5Txt.getDoubleValueDoNotThrow()
        ).setScale(2, BigDecimal.ROUND_HALF_UP);

        totalDebe.setScale(2, BigDecimal.ROUND_HALF_UP);
        totalHaber.setScale(2, BigDecimal.ROUND_HALF_UP);

        if (totalDebe.doubleValue() != totalHaber.doubleValue()) {
            Notification.show("La partida es descuadrada, por favor revisar"
                    + " Debe = " + totalDebe.doubleValue() + "  Haber = " + totalHaber, Notification.Type.WARNING_MESSAGE);
            return;
        }

        if (numeroTxt.getValue().trim().isEmpty()) {
            Notification.show("Por favor, escriba el cheque o transferencia. ", Notification.Type.ERROR_MESSAGE);
            numeroTxt.focus();
            return;
        }
        if (nombreChequeTxt.getValue().trim().isEmpty()) {
            Notification.show("Por favor, escriba el nombre del cheque o transferencia. ", Notification.Type.ERROR_MESSAGE);
            nombreChequeTxt.focus();
            return;
        }
        if (montoTxt.getDoubleValueDoNotThrow() == 0.00) {
            Notification.show("Por favor primero cree la partida, con el monto respectivo. ", Notification.Type.ERROR_MESSAGE);
            montoTxt.focus();
            return;
        }
        if (cuentaContable1Cbx.getValue() == null || cuentaContable2Cbx.getValue() == null) {
            Notification.show("Por favor elija la cuenta contable que corresponda. ", Notification.Type.ERROR_MESSAGE);
            cuentaContable1Cbx.focus();
            return;
        }
        if (monedaCbx.getValue().equals("DOLARES") && tasaCambioTxt.getDoubleValueDoNotThrow() <= 1.00) {
            Notification.show("Si la transacción es en DOLARES, debe llebar tipo de cambio. Por favor, revise la moneda.", Notification.Type.ERROR_MESSAGE);
            monedaCbx.focus();
            return;
        }
        if (medioCbx.getValue() == null) {
            Notification.show("Por favor seleccionar un Medio.", Notification.Type.ERROR_MESSAGE);
            medioCbx.focus();
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

        queryString = " Select codigoPartida from contabilidad_partida ";
        queryString += " Where CodigoPartida like '" + codigoPartida + "%'";
        queryString += " Order by CodigoPartida desc ";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado

                ultimoEncontado = rsRecords.getString("codigoPartida").substring(12, 15);

                System.out.println("ultimo encontrado " + ultimoEncontado);

                codigoPartida += String.format("%03d", (Integer.valueOf(ultimoEncontado) + 1));

            } else {
                codigoPartida += "001";
            }

        } catch (Exception ex1) {
            System.out.println("Error al buscar siguiente correlativo de partida" + ex1.getMessage());
            ex1.printStackTrace();
        }

        codigoPartidaNuevo = codigoPartida;
        
        String descripcion  = titleLbl.getValue() + " " + title2Lbl;

        queryString = " Insert Into contabilidad_partida (IdEmpresa, Estatus, CodigoPartida, CodigoCC,";
        queryString += " TipoDocumento, Fecha, IdProveedor, NITProveedor, NombreProveedor, NombreCheque,";
        queryString += " MontoDocumento, SerieDocumento, NumeroDocumento, TipoDOCA, NoDOCA, IdNomenclatura, MonedaDocumento, Debe, Haber,";
        queryString += " DebeQuetzales, HaberQuetzales, TipoCambio, Saldo,";
        queryString += " Descripcion, CreadoUsuario, CreadoFechaYHora)";
        queryString += " Values ";
        queryString += " (";
        queryString += String.valueOf(empresaCbx.getValue());
        queryString += ",'INGRESADO'";
        queryString += ",'" + codigoPartida + "'";
        queryString += ",'" + codigo1Txt.getValue() + "'";
        queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
        queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
        queryString += "," + proveedorId;
        queryString += ",''";//nitproveedor
        queryString += ",'" + nombreProveedor + "'";
        queryString += ",'" + nombreChequeTxt.getValue() + "'";
        queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow());
        queryString += ",''";  //serie documento
        queryString += ",'" + numeroTxt.getValue() + "'";
        queryString += ",''"; //tipodoca
        queryString += ",''"; //doca
        queryString += "," + String.valueOf(cuentaContable1Cbx.getValue()); //idcuentacontable
        queryString += ",'" + monedaCbx.getValue() + "'";
        queryString += "," + String.valueOf(debe1Txt.getDoubleValueDoNotThrow()); //DEBE
        queryString += ",0.00"; //HABER
        queryString += "," + String.valueOf(debe1Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow()); //DEBE Q
        queryString += ",0.00"; //HABER Q.
        queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
        queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow());
        queryString += ",'" + descripcion+ "'";
        queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
        queryString += ",current_timestamp";
        queryString += ")";

        System.out.println("primer insert" + queryString);

        if (cuentaContable2Cbx.getValue() != null && (debe2Txt.getDoubleValueDoNotThrow() > 0 || haber2Txt.getDoubleValueDoNotThrow() > 0)) {
            queryString += ",(";
            queryString += String.valueOf(empresaCbx.getValue());
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigo2Txt.getValue() + "'";
            queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorId;
            queryString += ",''"; //nitproveedor
            queryString += ",'" + nombreProveedor + "'";
            queryString += ",'" + nombreChequeTxt.getValue() + "'";
            queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow());
            queryString += ",''"; //serie documento
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += ",''"; //tipodoca
            queryString += ",''"; //doca
            queryString += "," + cuentaContable2Cbx.getValue(); //idcuentacontable
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(debe2Txt.getDoubleValueDoNotThrow()); // DEBE
            queryString += "," + String.valueOf(haber2Txt.getDoubleValueDoNotThrow()); // HABER
            queryString += "," + String.valueOf(debe2Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow()); //DEBE Q.
            queryString += "," + String.valueOf(haber2Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow()); //HABER Q
            queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow());
            queryString += ",'" + descripcion + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";

            System.out.println("segundo query" + queryString);

        }

        if (cuentaContable3Cbx.getValue() != null && (debe3Txt.getDoubleValueDoNotThrow() > 0 || haber3Txt.getDoubleValueDoNotThrow() > 0)) {
            queryString += ",(";
            queryString += String.valueOf(empresaCbx.getValue());
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigo3Txt.getValue() + "'";
            queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorId;
            queryString += ",''";
            queryString += ",'" + nombreProveedor + "'";
            queryString += ",'" + nombreChequeTxt.getValue() + "'";
            queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow());
            queryString += ",''"; //serie documento
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += ",''"; //tipodoca
            queryString += ",''"; //doca
            queryString += "," + cuentaContable3Cbx.getValue(); //idcuentacontable
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(debe3Txt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(haber3Txt.getDoubleValueDoNotThrow()); // HABER
            queryString += "," + String.valueOf(debe3Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow()); //DEBE Q.
            queryString += "," + String.valueOf(haber3Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow()); //HABER Q
            queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow());
            queryString += ",'" + descripcion + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";

            System.out.println("tercer query" + queryString);

        }
        if (cuentaContable4Cbx.getValue() != null && (debe4Txt.getDoubleValueDoNotThrow() > 0 || haber4Txt.getDoubleValueDoNotThrow() > 0)) {
            queryString += ",(";
            queryString += String.valueOf(empresaCbx.getValue());
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigo4Txt.getValue() + "'";
            queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorId;
            queryString += ",''";
            queryString += ",'" + nombreProveedor + "'";
            queryString += ",'" + nombreChequeTxt.getValue() + "'";
            queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow());
            queryString += ",''"; //serie documento
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += ",''"; //tipodoca
            queryString += ",''"; //doca
            queryString += "," + cuentaContable4Cbx.getValue(); //idcuentacontable
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(debe4Txt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(haber4Txt.getDoubleValueDoNotThrow()); // HABER
            queryString += "," + String.valueOf(debe4Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow()); //DEBE Q.
            queryString += "," + String.valueOf(haber4Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow()); //HABER Q
            queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow());
            queryString += ",'" + descripcion + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";

            System.out.println("cuarto query" + queryString);

        }
        if (cuentaContable5Cbx.getValue() != null && (debe5Txt.getDoubleValueDoNotThrow() > 0 || haber5Txt.getDoubleValueDoNotThrow() > 0)) {
            queryString += ",(";
            queryString += String.valueOf(empresaCbx.getValue());
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartida + "'";
            queryString += ",'" + codigo5Txt.getValue() + "'";
            queryString += ",'" + String.valueOf(medioCbx.getValue()) + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
            queryString += "," + proveedorId;
            queryString += ",''";
            queryString += ",'" + nombreProveedor + "'";
            queryString += ",'" + nombreChequeTxt.getValue() + "'";
            queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow());
            queryString += ",''"; //serie documento
            queryString += ",'" + numeroTxt.getValue() + "'";
            queryString += ",''"; //tipodoca
            queryString += ",''"; //doca
            queryString += "," + cuentaContable5Cbx.getValue(); //idcuentacontable
            queryString += ",'" + monedaCbx.getValue() + "'";
            queryString += "," + String.valueOf(debe5Txt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(haber5Txt.getDoubleValueDoNotThrow()); // HABER
            queryString += "," + String.valueOf(debe5Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow()); //DEBE Q.
            queryString += "," + String.valueOf(haber5Txt.getDoubleValueDoNotThrow() * tasaCambioTxt.getDoubleValueDoNotThrow()); //HABER Q
            queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
            queryString += "," + String.valueOf(montoTxt.getDoubleValueDoNotThrow());
            queryString += ",'" + descripcion + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";

            System.out.println("quinto query" + queryString);

        }

        try {

            ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().setAutoCommit(false);

            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();

            stQuery.executeUpdate(queryString);

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

            ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().commit();
            ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().setAutoCommit(true);

            ((FacturaVentaView) (mainUI.getNavigator().getCurrentView())).llenarTablaFacturaVenta(String.valueOf(empresaCbx.getValue()));

            Notification notif = new Notification("PAGAO REALIZADO EXITOSAMENTE.",
                    Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.CHECK);
            notif.show(Page.getCurrent());

            close();


        } catch (Exception ex1) {
            System.out.println("Error al insertar transacción  : " + ex1.getMessage());
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
                Logger.getLogger(PagoFacturaVentaForm.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public void limpiarPartida() {
        montoTxt.setReadOnly(false);
        numeroTxt.setValue("");
        montoTxt.setValue(0.00);
        nombreChequeTxt.setReadOnly(false);
        nombreChequeTxt.setValue("");
        descripcionTxt.setValue("");

        nombreChequeTxt.setReadOnly(((SopdiUI) mainUI).sessionInformation.getStrUserProfileName().equals("CONTADOR"));
        nombreChequeTxt.setReadOnly(((SopdiUI) mainUI).sessionInformation.getStrUserProfileName().equals("AUXILIAR"));

        cuentaContable1Cbx.setReadOnly(false);
        cuentaContable1Cbx.clear();
        debe1Txt.setReadOnly(false);
        haber1Txt.setReadOnly(false);
        debe1Txt.setValue(0.00);
        haber1Txt.setValue(0.00);

        cuentaContable2Cbx.setReadOnly(false);
        cuentaContable2Cbx.clear();
        debe2Txt.setReadOnly(false);
        haber2Txt.setReadOnly(false);
        debe2Txt.setValue(0.00);
        haber2Txt.setValue(0.00);

        cuentaContable3Cbx.setReadOnly(false);
        cuentaContable3Cbx.clear();
        debe3Txt.setReadOnly(false);
        haber3Txt.setReadOnly(false);
        debe3Txt.setValue(0.00);
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

        codigo1Txt.setValue("");
        codigo2Txt.setValue("");
        codigo3Txt.setValue("");
        codigo4Txt.setValue("");
        codigo5Txt.setValue("");
    }

}