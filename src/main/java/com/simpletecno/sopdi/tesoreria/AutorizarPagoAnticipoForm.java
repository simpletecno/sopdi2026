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
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.renderers.ClickableRenderer;
import com.vaadin.ui.themes.ValoTheme;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import org.vaadin.ui.NumberField;

/**
 *
 * @author user
 */
public class AutorizarPagoAnticipoForm extends Window {

    VerticalLayout mainLayout;

    NumberField montoAutorizarTxt;

    ComboBox empresaCbx;
    ComboBox proveedorCbx;
    ComboBox monedaCbx;

    Button salirBtn;
    Button autorizarBtn;

    UI mainUI;
    Statement stQuery, stQuery1;
    ResultSet rsRecords, rsRecords1;

    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");

    static final String CODIGO_PARTIDA = "Codigo";
    static final String CODIGO_CC = "CodigoCC";
    static final String FECHA = "Fecha";
    static final String MONEDA_DOCUMENTO = "Moneda";
    static final String DEBE = "Debe";
    static final String HABER = "Haber";
    static final String TIPO_CAMBIO = "Tasa";
    static final String DEBE_QUETZALES = "Debe Q.";
    static final String HABER_QUETZALES = "Haber Q.";
    static final String SALDO = "Saldo";
    static final String ACCION = "Autorizar";

    public IndexedContainer container = new IndexedContainer();
    Grid anticiposGrind;
    Grid.FooterRow anticiposFooter;

    double totalDebeQuetzales = 0.00, totalHaberQueztales = 0.00;
    double totalDebe = 0.00, totalHaber = 0.00;
    double saldo = 0.00;

    String tipo;
    String queryString;

    public AutorizarPagoAnticipoForm(String tipo) {
        this.tipo = tipo;

        this.mainUI = UI.getCurrent();
        setResponsive(true);
        if (mainUI.getPage().getBrowserWindowWidth() >= 736) {
            setWidth("85%");
            setHeight("70%");
        }
        else {
            setWidth("95%");
            setHeight("95%");
        }

        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setMargin(new MarginInfo(false, true, false, true));
        mainLayout.setResponsive(true);

        HorizontalLayout layoutTitle = new HorizontalLayout();
        layoutTitle.setWidth("100%");
        layoutTitle.setSpacing(true);
        layoutTitle.setMargin(new MarginInfo(true, true, false, true));
        layoutTitle.setSizeUndefined();
        layoutTitle.addStyleName(ValoTheme.LAYOUT_HORIZONTAL_WRAPPING);

        empresaCbx = new ComboBox("EMPRESA :");
        empresaCbx.setStyleName(ValoTheme.COMBOBOX_HUGE);
        empresaCbx.setWidth("90%");
        empresaCbx.setInvalidAllowed(false);
        empresaCbx.setNewItemsAllowed(false);
        empresaCbx.setTextInputAllowed(false);
        empresaCbx.setNullSelectionAllowed(false);
        llenarComboEmpresa();

        Label titleLbl = new Label(tipo);
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h2_custom");

        HorizontalLayout filtrosLayouth = new HorizontalLayout();
        filtrosLayouth.setMargin(new MarginInfo(false, true, false, true));
        filtrosLayouth.setSpacing(true);
        filtrosLayouth.setResponsive(true);
        filtrosLayouth.setSizeUndefined();
        filtrosLayouth.addStyleName(ValoTheme.LAYOUT_HORIZONTAL_WRAPPING);

        VerticalLayout filtrosLayoutv = new VerticalLayout();
        filtrosLayoutv.setMargin(new MarginInfo(false, true, false, true));
        filtrosLayoutv.setSpacing(true);
        filtrosLayoutv.setResponsive(true);
        filtrosLayoutv.setSizeUndefined();
        filtrosLayoutv.addStyleName(ValoTheme.LAYOUT_HORIZONTAL_WRAPPING);

        proveedorCbx = new ComboBox("Proveedores");
        if (mainUI.getPage().getBrowserWindowWidth() >= 736) {
            proveedorCbx.setWidth("30em");
        }
        else {
            String w = (mainUI.getPage().getBrowserWindowWidth()/30) + "em";
            proveedorCbx.setWidth(w);
        }
        proveedorCbx.setFilteringMode(FilteringMode.CONTAINS);
        proveedorCbx.setInvalidAllowed(false);
        proveedorCbx.setNewItemsAllowed(false);
        proveedorCbx.setNullSelectionAllowed(false);
        proveedorCbx.addValueChangeListener(event -> {
            if (event.getProperty() == null) {
                return;
            }
            if(!proveedorCbx.getValue().equals("0")){
                montoAutorizarTxt.setValue("0.00");
                llenarTablaAnticipos();
            }

        });

        llenarComboProveedor();

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
        montoAutorizarTxt.setWidth("10em");

        monedaCbx = new ComboBox("Moneda :");
        monedaCbx.setWidth("10em");
        monedaCbx.addItem("QUETZALES");
        monedaCbx.addItem("DOLARES");
        monedaCbx.select("QUETZALES");
        monedaCbx.setInvalidAllowed(false);
        monedaCbx.setNewItemsAllowed(false);

        autorizarBtn = new Button("Autorizar");
        autorizarBtn.setIcon(FontAwesome.SAVE);
        autorizarBtn.setWidth("60%");
        autorizarBtn.setHeight("80%");
        autorizarBtn.setStyleName(ValoTheme.BUTTON_PRIMARY);
        autorizarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                insertTablaAnticipo();
            }
        });

        layoutTitle.addComponent(empresaCbx);
        layoutTitle.setComponentAlignment(empresaCbx, Alignment.MIDDLE_LEFT);

        layoutTitle.addComponent(titleLbl);
        layoutTitle.setComponentAlignment(titleLbl, Alignment.BOTTOM_RIGHT);

        if (mainUI.getPage().getBrowserWindowWidth() >= 736) {
            filtrosLayouth.addComponents(proveedorCbx, monedaCbx, montoAutorizarTxt, autorizarBtn);
            filtrosLayouth.setComponentAlignment(autorizarBtn, Alignment.BOTTOM_LEFT);

            mainLayout.addComponent(layoutTitle);
            mainLayout.addComponent(filtrosLayouth);
            mainLayout.setComponentAlignment(filtrosLayouth, Alignment.MIDDLE_LEFT);
        }
        else {
            filtrosLayoutv.addComponents(proveedorCbx, monedaCbx, montoAutorizarTxt, autorizarBtn);
            filtrosLayoutv.setComponentAlignment(autorizarBtn, Alignment.BOTTOM_LEFT);

            mainLayout.addComponent(filtrosLayoutv);
            mainLayout.addComponent(filtrosLayoutv);
            mainLayout.setComponentAlignment(filtrosLayoutv, Alignment.MIDDLE_LEFT);
        }

        setContent(mainLayout);
        createTablaAnticipos();
        crearComponentes();

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
            System.out.println("Error al listar empresas: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void llenarComboProveedor() {
        queryString = " SELECT * FROM proveedor ";
        queryString += " WHERE Inhabilitado = 0";
        if (tipo.equals(AutorizacionesPagoView.ANTICIPO_PROVEEDOR)) {
            queryString += " And EsProveedor = 1";
// 2021-04-22 por convenio con Pedro       } else if (tipo.equals(AutorizacionesPagoView.ANTICIPO_HONORARIOS)) {
//            queryString += " And EsPlanilla = 1";
        //} else if (tipo.equals(AutorizacionesPagoView.ANTICIPO_SUELDOS)) {
          //  queryString += " And EsPlanilla = 1";
        }
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

    public void createTablaAnticipos() {

        HorizontalLayout reportLayout = new HorizontalLayout();
        reportLayout.setWidth("96%");
        reportLayout.addStyleName("rcorners3");
        reportLayout.setResponsive(true);
        reportLayout.setMargin(new MarginInfo(false, true, false, true));
        reportLayout.setSpacing(true);

        container.addContainerProperty(CODIGO_PARTIDA, String.class, null);
        container.addContainerProperty(CODIGO_CC, String.class, null);
        container.addContainerProperty(FECHA, String.class, null);
        container.addContainerProperty(MONEDA_DOCUMENTO, String.class, null);
        container.addContainerProperty(DEBE, String.class, null);
        container.addContainerProperty(HABER, String.class, null);
        container.addContainerProperty(TIPO_CAMBIO, String.class, null);
        container.addContainerProperty(DEBE_QUETZALES, String.class, null);
        container.addContainerProperty(HABER_QUETZALES, String.class, null);
        container.addContainerProperty(SALDO, String.class, null);
        container.addContainerProperty(ACCION, String.class, null);

        anticiposGrind = new Grid("Historial de anticipos ACTIVOS del proveedor.", container);
        anticiposGrind.setImmediate(true);
        anticiposGrind.setSelectionMode(Grid.SelectionMode.SINGLE);
        anticiposGrind.setDescription("Seleccione un registro.");
        anticiposGrind.setHeightMode(HeightMode.ROW);
        anticiposGrind.setHeightByRows(7);
        anticiposGrind.setWidth("100%");
        anticiposGrind.setResponsive(true);
        anticiposGrind.setEditorBuffered(false);
        anticiposGrind.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if (DEBE.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (HABER.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (DEBE_QUETZALES.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (HABER_QUETZALES.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (SALDO.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }
        });
        anticiposGrind.getColumn(ACCION).setRenderer(new ButtonRenderer(e
                -> updateAnticipo(e)
        ));

        anticiposFooter = anticiposGrind.appendFooterRow();
        anticiposFooter.getCell(MONEDA_DOCUMENTO).setText("TOTALES");
        anticiposFooter.getCell(DEBE).setText("0.00");
        anticiposFooter.getCell(HABER).setText("0.00");
        anticiposFooter.getCell(TIPO_CAMBIO).setText("TOTALES");
        anticiposFooter.getCell(DEBE_QUETZALES).setText("0.00");
        anticiposFooter.getCell(HABER_QUETZALES).setText("0.00");
        anticiposFooter.getCell(SALDO).setText("0.00");

        anticiposFooter.getCell(DEBE).setStyleName("rightalign");
        anticiposFooter.getCell(HABER).setStyleName("rightalign");
        anticiposFooter.getCell(DEBE_QUETZALES).setStyleName("rightalign");
        anticiposFooter.getCell(HABER_QUETZALES).setStyleName("rightalign");
        anticiposFooter.getCell(SALDO).setStyleName("rightalign");

        reportLayout.addComponent(anticiposGrind);
        reportLayout.setComponentAlignment(anticiposGrind, Alignment.MIDDLE_CENTER);

        mainLayout.addComponent(reportLayout);
        mainLayout.setComponentAlignment(reportLayout, Alignment.TOP_CENTER);
    }

    private void updateAnticipo(ClickableRenderer.RendererClickEvent e) {

        String idAnticipo = String.valueOf(container.getContainerProperty(e.getItemId(), CODIGO_PARTIDA).getValue());

        queryString = "  UPDATE autorizacion_pago SET  ";
        queryString += " CodigoCC = '" + "TEMP_" + new java.util.Date().getTime() + "'";
        queryString += " WHERE IdAutorizacion = " + idAnticipo;

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            Notification notif = new Notification("AUTORIZACION EXITOSA",
                    Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.CHECK);
            notif.show(Page.getCurrent());

            montoAutorizarTxt.setValue("0.00");

        } catch (Exception ex1) {
            System.out.println("Error al insertar registro en autorizacion_pago :  " + ex1.getMessage());
            ex1.printStackTrace();
        }

    }

    public void llenarTablaAnticipos() {

        container.removeAllItems();
        anticiposFooter.getCell(DEBE).setText("0.00");
        anticiposFooter.getCell(HABER).setText("0.00");
        anticiposFooter.getCell(DEBE_QUETZALES).setText("0.00");
        anticiposFooter.getCell(HABER_QUETZALES).setText("0.00");

        totalDebeQuetzales = 0.00;
        totalHaberQueztales = 0.00;
        totalDebe = 0.00;
        totalHaber = 0.00;
        saldo = 0.00;

        queryString = " SELECT CodigoPartida, CodigoCC, Fecha, MonedaDocumento, Debe, Haber,  ";
        queryString += " TipoCambio, DebeQuetzales, HaberQuetzales, IdNomenclatura ";
        queryString += " FROM contabilidad_partida";
        queryString += " WHERE IdEmpresa = " + empresaCbx.getValue();
        queryString += " AND   Fecha >= '2020-01-01'";
        queryString += " AND   Upper(TipoDocumento) IN ('CHEQUE','TRANSFERENCIA', 'NOTA DE DEBITO')";
        queryString += " AND   IdProveedor = " + proveedorCbx.getValue();
        if (tipo.equals(AutorizacionesPagoView.ANTICIPO_PROVEEDOR)) {
            queryString += " and IdNomenclatura = " + ((SopdiUI) mainUI).cuentasContablesDefault.getAnticiposProveedor();
//        } else if (tipo.equals(AutorizacionesPagoView.ANTICIPO_HONORARIOS)) {
//            queryString += " and IdNomenclatura = " + ((SopdiUI) mainUI).cuentasContablesDefault.getAnticiposHonorarios();
        } else {
            queryString += " and IdNomenclatura = " + ((SopdiUI) mainUI).cuentasContablesDefault.getAnticiposSueldos();
        }
        queryString += " GROUP by contabilidad_partida.CodigoPartida ";
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
                    queryString += " WHERE IdEmpresa = " + empresaCbx.getValue();
                    queryString += " AND CodigoCC = '" + rsRecords.getString("CodigoCC") + "'";
                    queryString += " and IdNomenclatura = " + rsRecords.getString("IdNomenclatura");

                    rsRecords1 = stQuery1.executeQuery(queryString);

                    if (rsRecords1.next()) {

                        if (rsRecords1.getDouble("TOTALSALDO") > 0.00) {

                            Object itemId = container.addItem();
                            container.getContainerProperty(itemId, CODIGO_PARTIDA).setValue(rsRecords.getString("CodigoPartida"));
                            container.getContainerProperty(itemId, CODIGO_CC).setValue(rsRecords.getString("CodigoCC"));
                            container.getContainerProperty(itemId, FECHA).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha")));
                            container.getContainerProperty(itemId, MONEDA_DOCUMENTO).setValue(rsRecords.getString("MonedaDocumento"));
                            container.getContainerProperty(itemId, DEBE).setValue(numberFormat.format(rsRecords.getDouble("Debe")));
                            container.getContainerProperty(itemId, HABER).setValue(numberFormat.format(rsRecords.getDouble("Haber")));
                            container.getContainerProperty(itemId, TIPO_CAMBIO).setValue(numberFormat.format((rsRecords.getDouble("TipoCambio"))));
                            container.getContainerProperty(itemId, DEBE_QUETZALES).setValue(numberFormat.format((rsRecords.getDouble("DebeQuetzales"))));
                            container.getContainerProperty(itemId, HABER_QUETZALES).setValue(numberFormat.format((rsRecords.getDouble("HaberQuetzales"))));
                            container.getContainerProperty(itemId, SALDO).setValue(numberFormat.format((rsRecords1.getDouble("TOTALSALDO"))));

                            totalDebe = totalDebe + rsRecords.getDouble("Debe");
                            totalHaber = totalHaber + rsRecords.getDouble("Haber");
                            totalDebeQuetzales = totalDebeQuetzales + rsRecords.getDouble("DebeQuetzales");
                            totalHaberQueztales = totalHaberQueztales + rsRecords.getDouble("HaberQuetzales");
                            saldo = saldo + rsRecords1.getDouble("TOTALSALDO");
                        }
                    }

                } while (rsRecords.next());

                anticiposFooter.getCell(DEBE).setText(numberFormat.format(totalDebe));
                anticiposFooter.getCell(HABER).setText(numberFormat.format(totalHaber));
                anticiposFooter.getCell(DEBE_QUETZALES).setText(numberFormat.format(totalDebeQuetzales));
                anticiposFooter.getCell(HABER_QUETZALES).setText(numberFormat.format(totalHaberQueztales));
                anticiposFooter.getCell(SALDO).setText(numberFormat.format(saldo));
            }
        } catch (Exception ex) {
            System.out.println("Error al listar anicipos VIVOS." + ex);
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

    public void insertTablaAnticipo() {

        if (proveedorCbx.getValue() == "0"){
            Notification.show("Por favor, seleccione un proveedor..", Notification.Type.ERROR_MESSAGE);
            proveedorCbx.focus();
            return;
        }

        if (proveedorCbx.getValue() == null) {
            Notification.show("Por favor, seleccione un proveedor..", Notification.Type.ERROR_MESSAGE);
            proveedorCbx.focus();
            return;
        }
        if (montoAutorizarTxt.getDoubleValueDoNotThrow() == 0.00) {
            Notification.show("Por favor, Ingrese un monto..", Notification.Type.ERROR_MESSAGE);
            montoAutorizarTxt.focus();
            return;
        }

        queryString = "  Insert Into autorizacion_pago (TipoAutorizacion, IdEmpresa, IdProveedor, ";
        queryString += " Fecha, Moneda, Monto, CodigoCC, CuentaContableLiquidar, ";
        queryString += " Objetivo, CreadoUsuario, CreadoFechaYHora)";
        queryString += " Values ";
        queryString += "(";
        queryString += "'" + tipo + "'";
        queryString += "," + String.valueOf(empresaCbx.getValue());
        queryString += "," + proveedorCbx.getValue();
        queryString += ",current_date";
        queryString += ",'" + monedaCbx.getValue() + "'";
        queryString += "," + montoAutorizarTxt.getValue();
        queryString += ",'" + "TEMP_" + new java.util.Date().getTime() + "'";
        queryString += ",''"; // cuentacontableliquidar
        queryString += ",'" + tipo + "'";
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

            montoAutorizarTxt.setValue("0.00");

        } catch (Exception ex1) {
            System.out.println("Error al insertar registro en autorizacion_pago :  " + ex1.getMessage());
            ex1.printStackTrace();
        }

    }
}
