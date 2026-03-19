package com.simpletecno.sopdi.tesoreria;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import org.vaadin.ui.NumberField;

/**
 *
 * @author user
 */
public class AutorizarPagoPrestamoForm extends Window {

    VerticalLayout mainLayout;

    NumberField montoAutorizarTxt;

    ComboBox monedaCbx;
    ComboBox proveedorCbx;
    ComboBox tipoCbx;

    Button salirBtn;
    Button autorizarBtn;

    UI mainUI;
    Statement stQuery, stQuery1;
    ResultSet rsRecords, rsRecords1;
    String queryString;

    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");

    static final String CUENTA = "Cuenta";
    static final String CODIGO_PARTIDA = "Codigo";
    static final String FECHA = "Fecha";
    static final String TIPO_DOCUMENTO = "Cheque/Transfer.";
    static final String DOCUMENTO = "# Documento";
    static final String DESCRIPCION = "Descripción";
    static final String MONEDA_DOCUMENTO = "Moneda";
    static final String DEBE = "Debe";
    static final String HABER = "Haber";
    static final String TIPO_CAMBIO = "Tasa";
    static final String DEBE_QUETZALES = "Debe Q.";
    static final String HABER_QUETZALES = "Haber Q.";

    public IndexedContainer prestamoContainer = new IndexedContainer();
    Grid prestamoGrid;
    Grid.FooterRow prestamoFooter;

    double totalDebeQuetzales = 0.00, totalHaberQueztales = 0.00;
    double totalDebe = 0.00, totalHaber = 0.00;

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public AutorizarPagoPrestamoForm() {
        this.mainUI = UI.getCurrent();
        setResponsive(true);
        setWidth("85%");
        setHeight("80%");

        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setMargin(new MarginInfo(false, true, false, true));
        mainLayout.setResponsive(true);

        HorizontalLayout layoutTitle = new HorizontalLayout();
        layoutTitle.setWidth("100%");
        layoutTitle.setSpacing(true);
        layoutTitle.setMargin(new MarginInfo(true, true, false, true));

        Label titleLbl = new Label(empresaId + " " + empresaNombre + " " + AutorizacionesPagoView.PAGO_PRESTAMO);
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h2_custom");

        HorizontalLayout filtrosLayout = new HorizontalLayout();
        filtrosLayout.setMargin(new MarginInfo(false, true, false, true));
        filtrosLayout.setSpacing(true);

        proveedorCbx = new ComboBox("Proveedor : ");
        proveedorCbx.setWidth("40em");
        proveedorCbx.setVisible(true);
        proveedorCbx.setFilteringMode(FilteringMode.CONTAINS);
        proveedorCbx.addValueChangeListener(event -> {
            if (event.getProperty() == null) {
                return;
            }
            if(!proveedorCbx.getValue().equals("0")){
                llenarGridPrestamo();
            }
        });
        llenarComboProveedor();

        /**** AQUI ESTA QUEMADO LA CUENTA CONTABLE  ***/
        tipoCbx = new ComboBox("Tipo :");
        tipoCbx.setInvalidAllowed(false);
        tipoCbx.setNullSelectionAllowed(false);
        tipoCbx.setNewItemsAllowed(false);
        tipoCbx.addContainerProperty("CUENTA", String.class, "");
        tipoCbx.addItem(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getPrestamos());
        tipoCbx.addItem(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getAbastos());
        tipoCbx.setItemCaption(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getPrestamos(), "PRESTAMO");
        tipoCbx.setItemCaption(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getAbastos(), "ABASTO");
        tipoCbx.select(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getPrestamos());
        tipoCbx.addValueChangeListener(event -> {
            if (event.getProperty() == null) {
                return;
            }
            if(proveedorCbx.getValue() != null){
                llenarGridPrestamo();
            }
        });

        monedaCbx = new ComboBox("Moneda :");
        monedaCbx.setWidth("12em");
        monedaCbx.addItem("QUETZALES");
        monedaCbx.addItem("DOLARES");
        monedaCbx.select("QUETZALES");
        monedaCbx.setInvalidAllowed(false);
        monedaCbx.setNewItemsAllowed(false);

        montoAutorizarTxt = new NumberField("Monto : ");
        montoAutorizarTxt.setDecimalAllowed(true);
        montoAutorizarTxt.setDecimalPrecision(2);
        montoAutorizarTxt.setMinimumFractionDigits(2);
        montoAutorizarTxt.setDecimalSeparator('.');
        montoAutorizarTxt.setDecimalSeparatorAlwaysShown(true);
        montoAutorizarTxt.setValue(0d);
        montoAutorizarTxt.setGroupingUsed(true);
        montoAutorizarTxt.setGroupingSeparator(',');
        montoAutorizarTxt.setGroupingSize(3);
        montoAutorizarTxt.setImmediate(true);
        montoAutorizarTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        montoAutorizarTxt.setWidth("11em");

        autorizarBtn = new Button("Autorizar");
        autorizarBtn.setIcon(FontAwesome.SAVE);
        autorizarBtn.setWidth("60%");
        autorizarBtn.setHeight("80%");
        autorizarBtn.setStyleName(ValoTheme.BUTTON_PRIMARY);
        autorizarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                insertTablaPrestamoAutorizado();
            }
        });

        layoutTitle.addComponent(titleLbl);
        layoutTitle.setComponentAlignment(titleLbl, Alignment.BOTTOM_RIGHT);

        filtrosLayout.addComponents(proveedorCbx, tipoCbx, monedaCbx, montoAutorizarTxt, autorizarBtn);
        filtrosLayout.setComponentAlignment(autorizarBtn, Alignment.BOTTOM_LEFT);

        mainLayout.addComponents(layoutTitle,filtrosLayout);
        mainLayout.setComponentAlignment(filtrosLayout, Alignment.TOP_LEFT);

        HorizontalLayout spaceLayout = new HorizontalLayout();
        spaceLayout.setMargin(new MarginInfo(true, true, true, true));
        spaceLayout.setSpacing(true);
        spaceLayout.addComponents(new Label(""));

        setContent(mainLayout);

        crearGridPrestamo();
        crearComponentes();
    }

    public void crearGridPrestamo() {

        HorizontalLayout reportLayout = new HorizontalLayout();
        reportLayout.setWidth("96%");
        reportLayout.addStyleName("rcorners3");
        reportLayout.setResponsive(true);
        reportLayout.setMargin(new MarginInfo(false, true, false, true));
        reportLayout.setSpacing(true);

        prestamoContainer.addContainerProperty(CUENTA, String.class, null);
        prestamoContainer.addContainerProperty(CODIGO_PARTIDA, String.class, null);
        prestamoContainer.addContainerProperty(FECHA, String.class, null);
        prestamoContainer.addContainerProperty(TIPO_DOCUMENTO, String.class, null);
        prestamoContainer.addContainerProperty(DOCUMENTO, String.class, null);
        prestamoContainer.addContainerProperty(DESCRIPCION, String.class, null);
        prestamoContainer.addContainerProperty(MONEDA_DOCUMENTO, String.class, null);
        prestamoContainer.addContainerProperty(DEBE, String.class, null);
        prestamoContainer.addContainerProperty(HABER, String.class, null);
        prestamoContainer.addContainerProperty(TIPO_CAMBIO, String.class, null);
        prestamoContainer.addContainerProperty(DEBE_QUETZALES, String.class, null);
        prestamoContainer.addContainerProperty(HABER_QUETZALES, String.class, null);

        prestamoGrid = new Grid("Historial de prestamos", prestamoContainer);

        prestamoGrid.setImmediate(true);
        prestamoGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        prestamoGrid.setDescription("Seleccione un registro.");
        prestamoGrid.setHeightMode(HeightMode.ROW);
        prestamoGrid.setHeightByRows(7);
        prestamoGrid.setWidth("100%");
        prestamoGrid.setResponsive(true);
        prestamoGrid.setEditorBuffered(false);
        prestamoGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if (DEBE.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (HABER.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (DEBE_QUETZALES.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (HABER_QUETZALES.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }
        });

        prestamoGrid.getColumn(DESCRIPCION).setHidable(true).setHidden(true);

        prestamoFooter = prestamoGrid.appendFooterRow();
        prestamoFooter.getCell(MONEDA_DOCUMENTO).setText("TOTALES");
        prestamoFooter.getCell(DEBE).setText("0.00");
        prestamoFooter.getCell(HABER).setText("0.00");
        prestamoFooter.getCell(TIPO_CAMBIO).setText("TOTALES");
        prestamoFooter.getCell(DEBE_QUETZALES).setText("0.00");
        prestamoFooter.getCell(HABER_QUETZALES).setText("0.00");

        prestamoFooter.getCell(DEBE).setStyleName("rightalign");
        prestamoFooter.getCell(HABER).setStyleName("rightalign");
        prestamoFooter.getCell(DEBE_QUETZALES).setStyleName("rightalign");
        prestamoFooter.getCell(HABER_QUETZALES).setStyleName("rightalign");

        reportLayout.addComponent(prestamoGrid);
        reportLayout.setComponentAlignment(prestamoGrid, Alignment.MIDDLE_CENTER);

        mainLayout.addComponent(reportLayout);
        mainLayout.setComponentAlignment(reportLayout, Alignment.TOP_CENTER);
    }

    public void llenarGridPrestamo() {

        prestamoContainer.removeAllItems();
        prestamoFooter.getCell(DEBE).setText("0.00");
        prestamoFooter.getCell(HABER).setText("0.00");
        prestamoFooter.getCell(DEBE_QUETZALES).setText("0.00");
        prestamoFooter.getCell(HABER_QUETZALES).setText("0.00");

        totalDebeQuetzales = 0.00;
        totalHaberQueztales = 0.00;
        totalDebe = 0.00;
        totalHaber = 0.00;

        queryString = " SELECT contabilidad_partida.CodigoPartida, contabilidad_partida.Fecha, contabilidad_partida.TipoDocumento,";
        queryString += " contabilidad_partida.NumeroDocumento, contabilidad_partida.Descripcion, contabilidad_partida.MonedaDocumento,";
        queryString += " contabilidad_partida.Debe, contabilidad_partida.Haber, contabilidad_partida.TipoCambio, contabilidad_partida.CodigoCC,";
        queryString += " contabilidad_partida.DebeQuetzales, contabilidad_partida.HaberQuetzales,contabilidad_partida.IdNomenclatura ";
        queryString += " FROM contabilidad_partida";
        queryString += " WHERE contabilidad_partida.IdEmpresa =" + empresaId;
//        queryString += " and contabilidad_partida.IdProveedor =" + proveedorCbx.getValue();
        queryString += " AND contabilidad_partida.IdNomenclatura = " + tipoCbx.getValue();
//        queryString += " and contabilidad_partida.TipoDocumento In ('CHEQUE', 'TRANSFERENCIA')";
        queryString += " ORDER BY contabilidad_partida.Fecha DESC";

        try {

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);
            stQuery1 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            if (rsRecords.next()) {

                String monedaSimbolo;

                do {

                    queryString = " SELECT ";
                    queryString += " SUM(HABER - DEBE) TOTALSALDO, SUM(DebeQuetzales - HaberQuetzales) TOTALSALDOQ ";
                    queryString += " FROM contabilidad_partida";
                    queryString += " WHERE IdEmpresa = " + empresaId;
                    queryString += " AND CodigoCC = '" + rsRecords.getString("CodigoCC") + "'";
                    queryString += " AND contabilidad_partida.IdNomenclatura = " + rsRecords.getString("IdNomenclatura");
                    queryString += " AND contabilidad_partida.Estatus <> 'ANULADO'";

                    rsRecords1 = stQuery1.executeQuery(queryString);

                    if (rsRecords1.next()) {

                        if (rsRecords1.getDouble("TOTALSALDO") > 0.00) {

                            Object itemId = prestamoContainer.addItem();
                            prestamoContainer.getContainerProperty(itemId, CUENTA).setValue(tipoCbx.getItemCaption(tipoCbx.getValue()));
                            prestamoContainer.getContainerProperty(itemId, CODIGO_PARTIDA).setValue(rsRecords.getString("CodigoPartida"));
                            prestamoContainer.getContainerProperty(itemId, FECHA).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha")));
                            prestamoContainer.getContainerProperty(itemId, TIPO_DOCUMENTO).setValue(rsRecords.getString("TipoDocumento"));
                            prestamoContainer.getContainerProperty(itemId, DOCUMENTO).setValue(rsRecords.getString("NumeroDocumento"));
                            prestamoContainer.getContainerProperty(itemId, DESCRIPCION).setValue(rsRecords.getString("Descripcion"));
                            prestamoContainer.getContainerProperty(itemId, MONEDA_DOCUMENTO).setValue(rsRecords.getString("MonedaDocumento"));
                            prestamoContainer.getContainerProperty(itemId, DEBE).setValue(numberFormat.format(rsRecords.getDouble("Debe")));
                            prestamoContainer.getContainerProperty(itemId, HABER).setValue(numberFormat.format(rsRecords.getDouble("Haber")));
                            prestamoContainer.getContainerProperty(itemId, TIPO_CAMBIO).setValue(numberFormat.format((rsRecords.getDouble("TipoCambio"))));
                            prestamoContainer.getContainerProperty(itemId, DEBE_QUETZALES).setValue(numberFormat.format((rsRecords.getDouble("DebeQuetzales"))));
                            prestamoContainer.getContainerProperty(itemId, HABER_QUETZALES).setValue(numberFormat.format((rsRecords.getDouble("HaberQuetzales"))));

                            totalDebe = totalDebe + rsRecords.getDouble("Debe");
                            totalHaber = totalHaber + rsRecords.getDouble("Haber");
                            totalDebeQuetzales = totalDebeQuetzales + rsRecords.getDouble("DebeQuetzales");
                            totalHaberQueztales = totalHaberQueztales + rsRecords.getDouble("HaberQuetzales");
                        }
                    }

                } while (rsRecords.next());

                prestamoFooter.getCell(DEBE).setText(numberFormat.format(totalDebe));
                prestamoFooter.getCell(HABER).setText(numberFormat.format(totalHaber));
                prestamoFooter.getCell(DEBE_QUETZALES).setText(numberFormat.format(totalDebeQuetzales));
                prestamoFooter.getCell(HABER_QUETZALES).setText(numberFormat.format(totalHaberQueztales));
            }
        } catch (Exception ex) {
            System.out.println("Error al listar historial de prestamos" + ex);
            ex.printStackTrace();
        }
    }

    public void crearComponentes() {

        HorizontalLayout camposDocumento = new HorizontalLayout();
        camposDocumento.setWidth("96%");
        camposDocumento.setSpacing(true);
        camposDocumento.setMargin(new MarginInfo(false, true, false, true));

        salirBtn = new Button("Salir");
        salirBtn.setWidth("10%");
        salirBtn.setHeight("80%");
        salirBtn.setStyleName(ValoTheme.BUTTON_DANGER);
        salirBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();
            }
        });

        camposDocumento.addComponents(salirBtn);
        camposDocumento.setComponentAlignment(salirBtn, Alignment.TOP_LEFT);

        mainLayout.addComponent(camposDocumento);
        mainLayout.setComponentAlignment(camposDocumento, Alignment.TOP_LEFT);
    }

    public void llenarComboProveedor() {
        queryString = " SELECT * FROM proveedor_empresa ";
        queryString += " WHERE Inhabilitado = 0";
        queryString += " AND EsProveedor = 1";
        queryString += " AND IdEmpresa = " + empresaId;
        queryString += " ORDER BY Nombre ";

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

    public void insertTablaPrestamoAutorizado() {

        if (proveedorCbx.getValue() == "0") {
            Notification.show("Por favor, Seleccione un proveedor..", Notification.Type.ERROR_MESSAGE);
            proveedorCbx.focus();
            return;
        }

        if (proveedorCbx.getValue() == null) {
            Notification.show("Por favor, Seleccione un proveedor..", Notification.Type.ERROR_MESSAGE);
            proveedorCbx.focus();
            return;
        }

        if (monedaCbx.getValue() == null) {
            Notification.show("Por favor, Seleccione una moneda..", Notification.Type.ERROR_MESSAGE);
            monedaCbx.focus();
            return;
        }

        if (montoAutorizarTxt.getDoubleValueDoNotThrow() == 0.00) {
            Notification.show("Por favor, Ingrese un monto..", Notification.Type.ERROR_MESSAGE);
            montoAutorizarTxt.focus();
            return;
        }

        queryString = "INSERT INTO autorizacion_pago (TipoAutorizacion, IdEmpresa, IdProveedor, ";
        queryString += " Fecha, Moneda, Monto, CodigoCC, CuentaContableLiquidar, ";
        queryString += " Objetivo, CreadoUsuario, CreadoFechaYHora)";
        queryString += " VALUES ";
        queryString += "(";
        queryString += "'" + AutorizacionesPagoView.PAGO_PRESTAMO + "'";
        queryString += "," + empresaId;
        queryString += "," + proveedorCbx.getValue();
        queryString += ",current_date";
        queryString += ",'" + monedaCbx.getValue() + "'";
        queryString += "," + montoAutorizarTxt.getValue();
        queryString += ",'" + "TEMP_" + new java.util.Date().getTime() + "'";
//        queryString += ",'" + tipoCbx.getContainerProperty(tipoCbx.getValue(), "CUENTA").getValue() + "'"; // cuentacontableliquidar
        queryString += ",'" + tipoCbx.getValue() + "'"; // cuentacontableliquidar
        queryString += ",'" + AutorizacionesPagoView.PAGO_PRESTAMO + "'";
        queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
        queryString += ",current_timestamp";
        queryString += ")";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            Notification notif = new Notification("AUTORIZACION EXITOSA",
                    Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.CHECK);
            notif.show(Page.getCurrent());

            close();
        } catch (Exception ex1) {
            System.out.println("Error al insertar en la tabla pago pressamo autorizado" + ex1.getMessage());
            ex1.printStackTrace();
        }

    }
}
