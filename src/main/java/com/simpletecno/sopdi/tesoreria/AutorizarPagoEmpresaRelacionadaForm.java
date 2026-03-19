package com.simpletecno.sopdi.tesoreria;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import org.vaadin.ui.NumberField;

/**
 *
 * @author user
 */
public class AutorizarPagoEmpresaRelacionadaForm extends Window {

    VerticalLayout mainLayout;

    NumberField montoAutorizarTxt;

    ComboBox empresaRelacionadaCbx;
    ComboBox monedaCbx;

    Button salirBtn;
    Button autorizarBtn;
    
    UI mainUI;
    Statement stQuery;
    ResultSet rsRecords;
    String queryString;

    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");

    Label anticiposPorLiquidarDolaresLbl = new Label("0.00");
    Label anticiposPorLiquidarDolaresQLbl =  new Label("0.00");

    Label anticiposPorLiquidarQuetzalesLbl = new Label("0.00");
    Label anticiposPorLiquidarQuetzalesQLbl = new Label("0.00");

    Label acreedoresPorLiquidarDolaresLbl = new Label("0.00");
    Label acreedoresPorLiquidarDolaresQLbl =  new Label("0.00");

    Label acreedoresPorLiquidarQuetzalesLbl = new Label("0.00");
    Label acreedoresPorLiquidarQuetzalesQLbl =  new Label("0.00");

    OptionGroup tipodeAnticipoOg;

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public AutorizarPagoEmpresaRelacionadaForm() {

        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setResponsive(true);

        HorizontalLayout layoutTitle;
        layoutTitle = new HorizontalLayout();
        layoutTitle.setSpacing(true);
        layoutTitle.setMargin(true);
        layoutTitle.setWidth("100%");

        HorizontalLayout layoutTitle2;
        layoutTitle2 = new HorizontalLayout();
        layoutTitle2.setSpacing(true);
        layoutTitle2.setMargin(true);
        layoutTitle2.setWidth("100%");

        this.mainUI = UI.getCurrent();
        setResponsive(true);
        setWidth("90%");
        setHeight("60%");

        Label titleLbl = new Label("");
        titleLbl.setValue(empresaId + " " + empresaNombre + " " + AutorizacionesPagoView.TRASLADO_EMP_REL);
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h2_custom");

        layoutTitle.addComponent(titleLbl);
        layoutTitle.setComponentAlignment(titleLbl, Alignment.BOTTOM_RIGHT);

        mainLayout.addComponent(layoutTitle);

        crearLayoutSaldos();

        setContent(mainLayout);

        crearComponentes();

    }

    public void crearLayoutSaldos() {
        HorizontalLayout saldosLayout = new HorizontalLayout();
        saldosLayout.setMargin(true);
        saldosLayout.setSpacing(true);
        saldosLayout.setWidth("100%");

        empresaRelacionadaCbx = new ComboBox("EMPRESA RELACIONADA");
        empresaRelacionadaCbx.setWidth("90%");
        empresaRelacionadaCbx.setFilteringMode(FilteringMode.CONTAINS);
        empresaRelacionadaCbx.setInvalidAllowed(false);
        empresaRelacionadaCbx.setNewItemsAllowed(false);
        empresaRelacionadaCbx.setNullSelectionAllowed(false);
        empresaRelacionadaCbx.addContainerProperty("cuentaAnticiposLiquidar", String.class, "");
        empresaRelacionadaCbx.addContainerProperty("cuentaAcreedores", String.class, "");
        empresaRelacionadaCbx.addValueChangeListener(event -> {
            if (event.getProperty() == null) {
                return;
            }
            montoAutorizarTxt.clear();
            cuentasPorLiquidar();
        });

        llenarComboEmpresaRelacionda();

        saldosLayout.addComponents(empresaRelacionadaCbx);
        saldosLayout.setComponentAlignment(empresaRelacionadaCbx, Alignment.MIDDLE_LEFT);

        GridLayout gridLayout = new GridLayout(5, 3);
        gridLayout.setSpacing(true);
        gridLayout.setMargin(true);
        gridLayout.setWidth("100%");
        gridLayout.addStyleName("rcorners2");

        Label anticiposLbl = new Label("Anticipos por liquidar");

        gridLayout.addComponent(anticiposLbl, 0, 0, 1,0);
        gridLayout.setComponentAlignment(anticiposLbl, Alignment.TOP_CENTER);

        Label acreedoresLbl = new Label("Acreedores por liquidar");

        gridLayout.addComponent(acreedoresLbl, 3, 0,4,0);
        gridLayout.setComponentAlignment(acreedoresLbl, Alignment.TOP_CENTER);

        gridLayout.addComponent(anticiposPorLiquidarDolaresLbl,   0,1);
        gridLayout.addComponent(anticiposPorLiquidarDolaresQLbl,  1,1);
        gridLayout.addComponent(anticiposPorLiquidarQuetzalesLbl, 0,2);
        gridLayout.addComponent(anticiposPorLiquidarQuetzalesQLbl,1,2);
        gridLayout.setComponentAlignment(anticiposPorLiquidarDolaresLbl, Alignment.MIDDLE_RIGHT);
        gridLayout.setComponentAlignment(anticiposPorLiquidarDolaresQLbl, Alignment.MIDDLE_RIGHT);
        gridLayout.setComponentAlignment(anticiposPorLiquidarQuetzalesLbl, Alignment.MIDDLE_RIGHT);
        gridLayout.setComponentAlignment(anticiposPorLiquidarQuetzalesQLbl, Alignment.MIDDLE_RIGHT);

        anticiposPorLiquidarDolaresLbl.setStyleName("aladerecha");
        anticiposPorLiquidarDolaresQLbl.setStyleName("aladerecha");
        anticiposPorLiquidarQuetzalesLbl.setStyleName("aladerecha");
        anticiposPorLiquidarQuetzalesQLbl.setStyleName("aladerecha");

        gridLayout.addComponent(acreedoresPorLiquidarDolaresLbl,   3,1);
        gridLayout.addComponent(acreedoresPorLiquidarDolaresQLbl,  4,1);
        gridLayout.addComponent(acreedoresPorLiquidarQuetzalesLbl, 3,2);
        gridLayout.addComponent(acreedoresPorLiquidarQuetzalesQLbl,4,2);
        gridLayout.setComponentAlignment(acreedoresPorLiquidarDolaresLbl, Alignment.MIDDLE_RIGHT);
        gridLayout.setComponentAlignment(acreedoresPorLiquidarDolaresQLbl, Alignment.MIDDLE_RIGHT);
        gridLayout.setComponentAlignment(acreedoresPorLiquidarQuetzalesLbl, Alignment.MIDDLE_RIGHT);
        gridLayout.setComponentAlignment(acreedoresPorLiquidarQuetzalesQLbl, Alignment.MIDDLE_RIGHT);

        acreedoresPorLiquidarDolaresLbl.setStyleName("aladerecha");
        acreedoresPorLiquidarDolaresQLbl.setStyleName("aladerecha");
        acreedoresPorLiquidarQuetzalesLbl.setStyleName("aladerecha");
        acreedoresPorLiquidarQuetzalesQLbl.setStyleName("aladerecha");

        gridLayout.addComponent(new Label(" "), 2, 0);
        gridLayout.addComponent(new Label(" "), 2, 1);
        gridLayout.addComponent(new Label(" "), 2, 2);

        saldosLayout.addComponent(gridLayout);
        saldosLayout.setComponentAlignment(gridLayout, Alignment.MIDDLE_RIGHT);

        mainLayout.addComponent(saldosLayout);
        mainLayout.setComponentAlignment(saldosLayout, Alignment.MIDDLE_CENTER);
    }

    public void llenarComboEmpresaRelacionda() {

        queryString = " SELECT IdProveedor, Nombre, CuentaAnticiposLiquidar, CuentaAcreedores";
        queryString += " FROM proveedor_empresa ";
        queryString += " WHERE EsRelacionada = 1";
        queryString += " AND IdEmpresa <> " + empresaId;
        queryString += " ORDER BY Nombre";
//        queryString += " INNER JOIN contabilidad_empresa ON contabilidad_empresa.IdEmpresa = proveedor.IdEmpresa";

System.out.println("query AutorizarEmpresa Realacionada " + queryString);
        empresaRelacionadaCbx.removeAllItems();

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) { //  encontrado                
                empresaRelacionadaCbx.addItem(rsRecords.getString("IDProveedor"));
                empresaRelacionadaCbx.setItemCaption(rsRecords.getString("IDProveedor"), rsRecords.getString("Nombre"));
                empresaRelacionadaCbx.getContainerProperty(rsRecords.getString("IDProveedor"), "cuentaAnticiposLiquidar").setValue(rsRecords.getString("CuentaAnticiposLiquidar"));
                empresaRelacionadaCbx.getContainerProperty(rsRecords.getString("IDProveedor"), "cuentaAcreedores").setValue(rsRecords.getString("CuentaAcreedores"));
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar Proveedores " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void cuentasPorLiquidar() {

        Object selectedItem = empresaRelacionadaCbx.getValue();

        queryString = " SELECT contabilidad_partida.CodigoPartida, contabilidad_partida.CodigoCc,";
        queryString += " contabilidad_nomenclatura.NoCuenta, contabilidad_partida.MonedaDocumento,";
        queryString += " SUM(contabilidad_partida.Debe) TOTALDEBE, SUM(contabilidad_partida.Haber) TOTALHABER,";
        queryString += " SUM(contabilidad_partida.DebeQuetzales) TOTALDEBEQ, SUM(contabilidad_partida.HaberQuetzales) TOTALHABERQ,";
        queryString += " SUM(DEBE - HABER) TOTALSALDO, SUM(DebeQuetzales - HaberQuetzales) TOTALSALDOQ, contabilidad_partida.Fecha";
        queryString += " FROM contabilidad_partida";
        queryString += " INNER JOIN contabilidad_nomenclatura ON contabilidad_nomenclatura.IdNomenclatura = contabilidad_partida.IdNomenclatura";
        queryString += " WHERE trim(contabilidad_partida.CodigoCC) <> ''";
        queryString += " AND contabilidad_partida.CodigoCC <> '0'";
        queryString += " AND contabilidad_partida.IdProveedor = " + String.valueOf(selectedItem);
        queryString += " AND contabilidad_partida.IdEmpresa = " + empresaId;
        queryString += " AND contabilidad_nomenclatura.NoCuenta In ('" + String.valueOf(empresaRelacionadaCbx.getContainerProperty(selectedItem, "cuentaAnticiposLiquidar").getValue()) + "','" + String.valueOf(empresaRelacionadaCbx.getContainerProperty(selectedItem, "cuentaAcreedores").getValue()) + "')";
        queryString += " HAVING TOTALSALDO > 0";

        anticiposPorLiquidarDolaresLbl.setValue("$.0.00");
        anticiposPorLiquidarDolaresQLbl.setValue("Q.0.00");
        anticiposPorLiquidarQuetzalesLbl.setValue("Q.0.00");
        anticiposPorLiquidarQuetzalesQLbl.setValue("Q.0.00");
        acreedoresPorLiquidarDolaresLbl.setValue("$.0.00");
        acreedoresPorLiquidarDolaresQLbl.setValue("Q.0.00");
        acreedoresPorLiquidarQuetzalesLbl.setValue("Q.0.00");
        acreedoresPorLiquidarQuetzalesQLbl.setValue("Q.0.00");

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);
            if (rsRecords.next()) {
                do {
                    if(rsRecords.getString("NoCuenta").toUpperCase().equals(String.valueOf(empresaRelacionadaCbx.getContainerProperty(selectedItem, "cuentaAnticiposLiquidar").getValue()) )) {
                        if(rsRecords.getString("MonedaDocumento").toUpperCase().equals("DOLARES")) {
                            anticiposPorLiquidarDolaresLbl.setValue("$." + numberFormat.format(rsRecords.getDouble("TOTALSALDO")));
                            anticiposPorLiquidarDolaresQLbl.setValue("Q." + numberFormat.format(rsRecords.getDouble("TOTALSALDOQ")));
                        }
                        else {
                            anticiposPorLiquidarQuetzalesLbl.setValue("Q." + numberFormat.format(rsRecords.getDouble("TOTALSALDO")));
                            anticiposPorLiquidarQuetzalesQLbl.setValue("Q." + numberFormat.format(rsRecords.getDouble("TOTALSALDOQ")));
                        }
                    }
                    else {
                        if(rsRecords.getString("MonedaDocumento").toUpperCase().equals("DOLARES")) {
                            acreedoresPorLiquidarDolaresLbl.setValue("$." + numberFormat.format(rsRecords.getDouble("TOTALSALDO")));
                            acreedoresPorLiquidarDolaresQLbl.setValue("Q." + numberFormat.format(rsRecords.getDouble("TOTALSALDOQ")));
                        }
                        else {
                            acreedoresPorLiquidarQuetzalesLbl.setValue("Q." + numberFormat.format(rsRecords.getDouble("TOTALSALDO")));
                            acreedoresPorLiquidarQuetzalesQLbl.setValue("Q." + numberFormat.format(rsRecords.getDouble("TOTALSALDOQ")));
                        }
                    }
                } while(rsRecords.next());
            }
        } catch (Exception ex) {
            System.out.println("Error al calcular anticipos/acrredores relacionados a empresa : " + ex.getMessage());
            Notification.show("Error al calcular anticipos/acreedores relacionados a empresa. " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public void crearComponentes() {

        HorizontalLayout camposDocumento = new HorizontalLayout();
        camposDocumento.setWidth("96%");

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
        montoAutorizarTxt.setWidth("12em");

        monedaCbx = new ComboBox("Moneda :");
        monedaCbx.setWidth("13em");
        monedaCbx.addItem("QUETZALES");
        monedaCbx.addItem("DOLARES");
        monedaCbx.select("QUETZALES");
        monedaCbx.setInvalidAllowed(false);
        monedaCbx.setNewItemsAllowed(false);

        tipodeAnticipoOg = new OptionGroup("Elija una opción");
        tipodeAnticipoOg.addItems("ANTICIPO", "REBAJAR DEUDA");

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

        salirBtn = new Button("Salir");
        salirBtn.setWidth("50%");
        salirBtn.setHeight("80%");
        salirBtn.setStyleName(ValoTheme.BUTTON_DANGER);
        salirBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();
            }
        });

        camposDocumento.addComponents(salirBtn, monedaCbx, montoAutorizarTxt,tipodeAnticipoOg, autorizarBtn);

        camposDocumento.setComponentAlignment(salirBtn, Alignment.MIDDLE_LEFT);
        camposDocumento.setComponentAlignment(monedaCbx, Alignment.MIDDLE_RIGHT);
        camposDocumento.setComponentAlignment(montoAutorizarTxt, Alignment.MIDDLE_RIGHT);
        camposDocumento.setComponentAlignment(tipodeAnticipoOg, Alignment.MIDDLE_RIGHT);
        camposDocumento.setComponentAlignment(autorizarBtn, Alignment.MIDDLE_RIGHT);

        mainLayout.addComponent(camposDocumento);
        mainLayout.setComponentAlignment(camposDocumento, Alignment.MIDDLE_CENTER);
    }

    public void insertTablaAnticipo() {

        if (empresaRelacionadaCbx.getValue() == null) {
            Notification.show("Por favor, seleccione un proveedor..", Notification.Type.ERROR_MESSAGE);
            empresaRelacionadaCbx.focus();
            return;
        }
        if (montoAutorizarTxt.getDoubleValueDoNotThrow() == 0.00) {
            Notification.show("Por favor, Ingrese un monto..", Notification.Type.ERROR_MESSAGE);
            montoAutorizarTxt.focus();
            return;
        }
        if (tipodeAnticipoOg.getValue() == null) {
            Notification.show("Por favor, elija un tipo de pago o anticipo..", Notification.Type.ERROR_MESSAGE);
            tipodeAnticipoOg.focus();
            return;
        }

        Object selectedItem = empresaRelacionadaCbx.getValue();

        queryString = "  INSERT INTO autorizacion_pago (TipoAutorizacion, IdEmpresa, IdProveedor, ";
        queryString += " Fecha, Moneda, Monto, CodigoCC, CuentaContableLiquidar, ";
        queryString += " Objetivo, CreadoUsuario, CreadoFechaYHora)";
        queryString += " VALUES ";
        queryString += "(";
        queryString += "'" + AutorizacionesPagoView.TRASLADO_EMP_REL + "'";
        queryString += "," + empresaId;
        queryString += "," + empresaRelacionadaCbx.getValue();
        queryString += ",current_date";
        queryString += ",'" + monedaCbx.getValue() + "'";
        queryString += "," + montoAutorizarTxt.getValue();
//        queryString += ",'" + "TEMP_" + montoAutorizarTxt.getValue() + "'";
        queryString += ",'" + "TEMP_" + new java.util.Date().getTime() + "'";
        if(tipodeAnticipoOg.getValue().equals("ANTICIPO")) {
            queryString += ",'" + String.valueOf(empresaRelacionadaCbx.getContainerProperty(selectedItem, "cuentaAnticiposLiquidar").getValue()) + "'";
        }
        else {
            queryString += ",'" + String.valueOf(empresaRelacionadaCbx.getContainerProperty(selectedItem, "cuentaAcreedores").getValue()) + "'";
        }
        queryString += ",'" + tipodeAnticipoOg.getValue() + "'";
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
            System.out.println("Error al insertar en la tabla anticipo/pago_empresa relacionada " + ex1.getMessage());
            ex1.printStackTrace();
        }

    }
}
