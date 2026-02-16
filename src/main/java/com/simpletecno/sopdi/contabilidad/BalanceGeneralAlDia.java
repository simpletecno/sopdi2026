/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.contabilidad;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author user
 */
public final class BalanceGeneralAlDia extends Window {

    static final String ID_PROPERTY = "Id";
    static final String CUENTA_PROPERTY = "Cuenta";
    static final String DESCRIPCION_PROPERTY = "Descripción";
    static final String SALDO_ANTERIOR_PROPERTY = "S.Anterior";
    static final String DEBE_PROPERTY = "Debe";
    static final String HABER_PROPERTY = "Haber";
    static final String SALDO_FINAL_PROPERTY = "S.Final";
    static final String SALDOQ_ANTERIOR_PROPERTY = "S.Anterior Q.";
    static final String DEBEQ_PROPERTY = "Debe Q.";
    static final String HABERQ_PROPERTY = "Haber Q.";
    static final String SALDOQ_FINAL_PROPERTY = "S.Final Q.";

    static DecimalFormat numberFormat = new DecimalFormat("###,###,##0.00");

    UI mainUI;
    Statement stQuery, stQuery1, stQuery2;
    ResultSet rsRecords, rsRecords1, rsRecords2;
    ComboBox empresaCbx;
    DateField finDt;
    CheckBox omitirSinMovimientoChk;
    Button consultarBtn;
    Label waitLbl;

    IndexedContainer balanceSaldosContainer;
    Grid balanceSaldosGrid;
    Grid.FooterRow footerRow;

    public BalanceGeneralAlDia() {
        this.mainUI = UI.getCurrent();
        setResponsive(true);
        setWidth("70%");
        setHeight("80%");

        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setSpacing(true);
        contentLayout.setMargin(true);

        setContent(contentLayout);

        Label titleLbl = new Label("BALANCE GENERAL DE SALDOS AL DIA");
        titleLbl.addStyleName(ValoTheme.LABEL_H1);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h1_custom");

        contentLayout.addComponents(titleLbl);
        contentLayout.setComponentAlignment(titleLbl, Alignment.TOP_CENTER);

        empresaCbx = new ComboBox("Empresa :");
        empresaCbx.addStyleName(ValoTheme.COMBOBOX_LARGE);
        empresaCbx.setWidth("30em");
        empresaCbx.setInvalidAllowed(false);
        empresaCbx.setNewItemsAllowed(false);
        empresaCbx.setTextInputAllowed(false);
        empresaCbx.setNullSelectionAllowed(false);        
        empresaCbx.setFilteringMode(FilteringMode.CONTAINS);

        llenarComboEmpresa();

        finDt = new DateField("AL:");
        finDt.setDateFormat("dd/MM/yyyy");
        Date ultimoDia = Utileria.getUltimoDiaDelMes();
        finDt.setValue(ultimoDia);
        finDt.setWidth("8em");

        omitirSinMovimientoChk = new CheckBox("Omitir sin movimientos");
        omitirSinMovimientoChk.setValue(false);

        consultarBtn = new Button("Consultar");
        consultarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        consultarBtn.setIcon(FontAwesome.SEARCH);
        consultarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                fillGridBalanceSaldos();
            }
        });

        HorizontalLayout layoutCerrar = new HorizontalLayout();
        layoutCerrar.setMargin(true);
        layoutCerrar.setSpacing(true);
        layoutCerrar.addComponents(empresaCbx,finDt, omitirSinMovimientoChk, consultarBtn);
        layoutCerrar.setComponentAlignment(empresaCbx, Alignment.BOTTOM_LEFT);
        layoutCerrar.setComponentAlignment(finDt, Alignment.BOTTOM_RIGHT);
        layoutCerrar.setComponentAlignment(consultarBtn, Alignment.BOTTOM_RIGHT);

        waitLbl = new Label("Espere...");
        waitLbl.setVisible(false);
        waitLbl.addStyleName(ValoTheme.LABEL_SUCCESS);

        contentLayout.addComponents(layoutCerrar, crearGridBalanceSaldos(), waitLbl);

    }

    public void llenarComboEmpresa() {
        String queryString = " SELECT * from contabilidad_empresa";
        queryString += " Where IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();

        try {
            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords1 = stQuery1.executeQuery(queryString);

            while (rsRecords1.next()) { //  encontrado        

                empresaCbx.addItem(rsRecords1.getString("IdEmpresa"));
                empresaCbx.setItemCaption(rsRecords1.getString("IdEmpresa"), rsRecords1.getString("Empresa"));

            }
            empresaCbx.select(empresaCbx.getItemIds().iterator().next());

        } catch (Exception ex1) {
            System.out.println("Error al listar empresas: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public VerticalLayout crearGridBalanceSaldos() {
        VerticalLayout layoutGridBalanceSaldos = new VerticalLayout();
        layoutGridBalanceSaldos.setWidth("100%");
        layoutGridBalanceSaldos.addStyleName("rcorners3");

        balanceSaldosContainer = new IndexedContainer();
        balanceSaldosContainer.addContainerProperty(ID_PROPERTY, String.class, null);
        balanceSaldosContainer.addContainerProperty(CUENTA_PROPERTY, String.class, null);
        balanceSaldosContainer.addContainerProperty(DESCRIPCION_PROPERTY, String.class, null);
        balanceSaldosContainer.addContainerProperty(SALDO_ANTERIOR_PROPERTY, String.class, null);
        balanceSaldosContainer.addContainerProperty(DEBE_PROPERTY, String.class, null);
        balanceSaldosContainer.addContainerProperty(HABER_PROPERTY, String.class, null);
        balanceSaldosContainer.addContainerProperty(SALDO_FINAL_PROPERTY, String.class, null);
//        balanceSaldosContainer.addContainerProperty(SALDOQ_ANTERIOR_PROPERTY, String.class, null);
//        balanceSaldosContainer.addContainerProperty(DEBEQ_PROPERTY, String.class, null);
//        balanceSaldosContainer.addContainerProperty(HABERQ_PROPERTY, String.class, null);
//        balanceSaldosContainer.addContainerProperty(SALDOQ_FINAL_PROPERTY, String.class, null);

        balanceSaldosGrid = new Grid(balanceSaldosContainer);
        balanceSaldosGrid.setImmediate(true);
        balanceSaldosGrid.setSelectionMode(Grid.SelectionMode.NONE);
        balanceSaldosGrid.setHeightMode(HeightMode.ROW);
        balanceSaldosGrid.setHeightByRows(15);
        balanceSaldosGrid.setWidth("100%");
        balanceSaldosGrid.setResponsive(true);
        balanceSaldosGrid.setEditorBuffered(false);

        balanceSaldosGrid.getColumn(ID_PROPERTY).setHidable(true).setHidden(true);

        balanceSaldosGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {

            if (DEBE_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (HABER_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (SALDO_ANTERIOR_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (SALDO_FINAL_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }

        });

        balanceSaldosGrid.getColumn(CUENTA_PROPERTY).setExpandRatio(1);
        balanceSaldosGrid.getColumn(DESCRIPCION_PROPERTY).setExpandRatio(3);
        balanceSaldosGrid.getColumn(SALDO_ANTERIOR_PROPERTY).setExpandRatio(1);
        balanceSaldosGrid.getColumn(DEBE_PROPERTY).setExpandRatio(1);
        balanceSaldosGrid.getColumn(HABER_PROPERTY).setExpandRatio(1);
        balanceSaldosGrid.getColumn(SALDO_FINAL_PROPERTY).setExpandRatio(1);

        footerRow = balanceSaldosGrid.appendFooterRow();
        footerRow.getCell(DESCRIPCION_PROPERTY).setText("SUMAS IGUALES");
        footerRow.getCell(SALDO_ANTERIOR_PROPERTY).setText("0.00");
        footerRow.getCell(DEBE_PROPERTY).setText("0.00");
        footerRow.getCell(HABER_PROPERTY).setText("0.00");
        footerRow.getCell(SALDO_FINAL_PROPERTY).setText("0.00");
//        footerRow.getCell(SALDOQ_ANTERIOR_PROPERTY).setText("0.00");
//        footerRow.getCell(DEBEQ_PROPERTY).setText("0.00");
//        footerRow.getCell(HABERQ_PROPERTY).setText("0.00");
//        footerRow.getCell(SALDOQ_FINAL_PROPERTY).setText("0.00");

        footerRow.getCell(DESCRIPCION_PROPERTY).setStyleName("rightalign");
        footerRow.getCell(SALDO_ANTERIOR_PROPERTY).setStyleName("rightalign");
        footerRow.getCell(DEBE_PROPERTY).setStyleName("rightalign");
        footerRow.getCell(HABER_PROPERTY).setStyleName("rightalign");
        footerRow.getCell(SALDO_FINAL_PROPERTY).setStyleName("rightalign");
//        footerRow.getCell(SALDOQ_ANTERIOR_PROPERTY).setStyleName("rightalign");
//        footerRow.getCell(DEBEQ_PROPERTY).setStyleName("rightalign");
//        footerRow.getCell(HABERQ_PROPERTY).setStyleName("rightalign");
//        footerRow.getCell(SALDOQ_FINAL_PROPERTY).setStyleName("rightalign");

        Grid.HeaderRow filterRow = balanceSaldosGrid.appendHeaderRow();

        Grid.HeaderCell cell0 = filterRow.getCell(SALDO_FINAL_PROPERTY);

        TextField filterField0 = new TextField();
        filterField0.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField0.setInputPrompt("Filtrar");
        filterField0.setColumns(8);

        filterField0.addTextChangeListener(change -> {
            balanceSaldosContainer.removeContainerFilters(SALDO_FINAL_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                balanceSaldosContainer.addContainerFilter(
                        new SimpleStringFilter(SALDO_FINAL_PROPERTY,
                                change.getText(), true, false));
            }
        });

        cell0.setComponent(filterField0);

        Grid.HeaderCell cell00 = filterRow.getCell(DESCRIPCION_PROPERTY);

        TextField filterField00 = new TextField();
        filterField00.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField00.setInputPrompt("Filtrar");
        filterField00.setColumns(12);

        filterField00.addTextChangeListener(change -> {
            balanceSaldosContainer.removeContainerFilters(DESCRIPCION_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                balanceSaldosContainer.addContainerFilter(
                        new SimpleStringFilter(DESCRIPCION_PROPERTY,
                                change.getText(), true, false));
            }
        });

        cell00.setComponent(filterField00);

        Grid.HeaderCell cell = filterRow.getCell(SALDO_ANTERIOR_PROPERTY);

        TextField filterField = new TextField();
        filterField.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField.setInputPrompt("Filtrar");
        filterField.setColumns(12);

        filterField.addTextChangeListener(change -> {
            balanceSaldosContainer.removeContainerFilters(SALDO_ANTERIOR_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                balanceSaldosContainer.addContainerFilter(
                        new SimpleStringFilter(SALDO_ANTERIOR_PROPERTY,
                                change.getText(), true, false));
            }
        });

        cell.setComponent(filterField);

        Grid.HeaderCell cell2 = filterRow.getCell(DEBE_PROPERTY);

        TextField filterField2 = new TextField();
        filterField2.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField2.setInputPrompt("Filtrar");
        filterField2.setColumns(12);

        filterField2.addTextChangeListener(change -> {
            balanceSaldosContainer.removeContainerFilters(DEBE_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                balanceSaldosContainer.addContainerFilter(
                        new SimpleStringFilter(DEBE_PROPERTY,
                                change.getText(), true, false));
            }
        });

        cell2.setComponent(filterField2);

        Grid.HeaderCell cell3 = filterRow.getCell(HABER_PROPERTY);

        TextField filterField3 = new TextField();
        filterField3.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField3.setInputPrompt("Filtrar");
        filterField3.setColumns(12);

        filterField3.addTextChangeListener(change -> {
            balanceSaldosContainer.removeContainerFilters(HABER_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                balanceSaldosContainer.addContainerFilter(
                        new SimpleStringFilter(HABER_PROPERTY,
                                change.getText(), true, false));
            }
        });

        cell3.setComponent(filterField3);

        layoutGridBalanceSaldos.addComponent(balanceSaldosGrid);

        return(layoutGridBalanceSaldos);

    }

    public void fillGridBalanceSaldos() {

        BigDecimal totalSaldoAnterior = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal totalDebe = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal totalHaber = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal totalSaldoFinal = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
//        BigDecimal totalSaldoAnteriorQ = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
//        BigDecimal totalDebeQ = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
//        BigDecimal totalHaberQ = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
//        BigDecimal totalSaldoFinalQ = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);

        if (balanceSaldosContainer == null) {
            return;
        }

        balanceSaldosContainer.removeAllItems();
        balanceSaldosGrid.getContainerDataSource().removeAllItems();
        balanceSaldosContainer.removeAllContainerFilters();

        Object itemId;

        String queryString = " SELECT * from contabilidad_nomenclatura";
        queryString += " where Estatus = 'HABILITADA'";
        queryString += " Order By Cast(NoCuenta AS UNSIGNED)";

//System.out.println(queryString);

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado

                BigDecimal saldoAnterior;
                BigDecimal debe;
                BigDecimal haber;
                BigDecimal saldoFinal;
                BigDecimal saldoAnteriorQ;
                BigDecimal debeQ;
                BigDecimal haberQ;
                BigDecimal saldoFinalQ;

                do {

                    debe  = new BigDecimal(getDebeHaberCuenta(false, rsRecords.getString("IdNomenclatura"), "DebeQuetzales")).setScale(2, BigDecimal.ROUND_HALF_UP);
                    haber = new BigDecimal(getDebeHaberCuenta(false, rsRecords.getString("IdNomenclatura"), "HaberQuetzales")).setScale(2, BigDecimal.ROUND_HALF_UP);
//                    debeQ  = new BigDecimal(getDebeHaberCuenta(true,rsRecords.getString("IdNomenclatura"), "DebeQuetzales")).setScale(2, BigDecimal.ROUND_HALF_UP);
//                    haberQ = new BigDecimal(getDebeHaberCuenta(true,rsRecords.getString("IdNomenclatura"), "HaberQuetzales")).setScale(2, BigDecimal.ROUND_HALF_UP);

                    saldoAnterior = new BigDecimal(getSaldoCuentaMesAnterior(false,rsRecords.getString("IdNomenclatura")).doubleValue()).setScale(2, BigDecimal.ROUND_HALF_UP);
                    saldoFinal = new BigDecimal(saldoAnterior.doubleValue()).setScale(2, BigDecimal.ROUND_HALF_UP);
//                  saldoAnteriorQ = new BigDecimal(getSaldoCuentaMesAnterior(true,rsRecords.getString("IdNomenclatura")).doubleValue()).setScale(2, BigDecimal.ROUND_HALF_UP);
//                    saldoFinalQ = new BigDecimal(saldoAnteriorQ.doubleValue()).setScale(2, BigDecimal.ROUND_HALF_UP);

                    if(   rsRecords.getString("NoCuenta").startsWith("1")
                            || rsRecords.getString("NoCuenta").startsWith("5")
                            || rsRecords.getString("NoCuenta").startsWith("6")) {
                        saldoFinal = saldoFinal.add(new BigDecimal(debe.doubleValue() - haber.doubleValue())).setScale(2, BigDecimal.ROUND_HALF_UP);
//                        saldoFinalQ = saldoFinalQ.add(new BigDecimal(debeQ.doubleValue() - haberQ.doubleValue())).setScale(2, BigDecimal.ROUND_HALF_UP);
                    }
                    else {
                        saldoFinal = saldoFinal.subtract(new BigDecimal(debe.doubleValue() - haber.doubleValue())).setScale(2, BigDecimal.ROUND_HALF_UP);
//                        saldoFinalQ = saldoFinalQ.subtract(new BigDecimal(debeQ.doubleValue() - haberQ.doubleValue())).setScale(2, BigDecimal.ROUND_HALF_UP);
                    }

//System.out.println(queryString);

                    if(omitirSinMovimientoChk.getValue()) {
                        if(saldoAnterior.doubleValue() == 0.00 && saldoFinal.doubleValue() == 0.00) {
                            continue;
                        }
                    }

                    itemId = balanceSaldosContainer.addItem();
                    balanceSaldosContainer.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords.getString("IdNomenclatura"));
                    balanceSaldosContainer.getContainerProperty(itemId, CUENTA_PROPERTY).setValue(rsRecords.getString("NoCuenta"));
                    balanceSaldosContainer.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(rsRecords.getString("N5"));

                    balanceSaldosContainer.getContainerProperty(itemId, SALDO_ANTERIOR_PROPERTY).setValue(numberFormat.format(saldoAnterior));
                    balanceSaldosContainer.getContainerProperty(itemId, DEBE_PROPERTY).setValue(numberFormat.format(debe));
                    balanceSaldosContainer.getContainerProperty(itemId, HABER_PROPERTY).setValue(numberFormat.format(haber));
                    balanceSaldosContainer.getContainerProperty(itemId, SALDO_FINAL_PROPERTY).setValue(numberFormat.format(saldoFinal));
//                    balanceSaldosContainer.getContainerProperty(itemId, SALDOQ_ANTERIOR_PROPERTY).setValue(numberFormat.format(saldoAnteriorQ));
//                    balanceSaldosContainer.getContainerProperty(itemId, DEBEQ_PROPERTY).setValue(numberFormat.format(debeQ));
//                    balanceSaldosContainer.getContainerProperty(itemId, HABERQ_PROPERTY).setValue(numberFormat.format(haberQ));
//                    balanceSaldosContainer.getContainerProperty(itemId, SALDOQ_FINAL_PROPERTY).setValue(numberFormat.format(saldoFinalQ));

                    totalSaldoAnterior = totalSaldoAnterior.add(saldoAnterior).setScale(2, BigDecimal.ROUND_HALF_UP);
                    totalSaldoFinal = totalSaldoFinal.add(saldoFinal).setScale(2, BigDecimal.ROUND_HALF_UP);
                    totalDebe = totalDebe.add(debe).setScale(2, BigDecimal.ROUND_HALF_UP);
                    totalHaber = totalHaber.add(haber).setScale(2, BigDecimal.ROUND_HALF_UP);

//                    totalSaldoAnteriorQ = totalSaldoAnterior.add(saldoAnteriorQ).setScale(2, BigDecimal.ROUND_HALF_UP);
//                    totalSaldoFinalQ = totalSaldoFinalQ.add(saldoFinal).setScale(2, BigDecimal.ROUND_HALF_UP);
//                    totalDebeQ = totalDebeQ.add(debeQ).setScale(2, BigDecimal.ROUND_HALF_UP);
//                    totalHaberQ = totalHaberQ.add(haberQ).setScale(2, BigDecimal.ROUND_HALF_UP);
                } while (rsRecords.next());

                footerRow.getCell(SALDO_ANTERIOR_PROPERTY).setText(numberFormat.format(totalSaldoAnterior));
                footerRow.getCell(DEBE_PROPERTY).setText(numberFormat.format(totalDebe));
                footerRow.getCell(HABER_PROPERTY).setText(numberFormat.format(totalHaber));
                footerRow.getCell(SALDO_FINAL_PROPERTY).setText(numberFormat.format(totalSaldoFinal));
//                footerRow.getCell(SALDOQ_ANTERIOR_PROPERTY).setText(numberFormat.format(totalSaldoAnteriorQ));
//                footerRow.getCell(DEBEQ_PROPERTY).setText(numberFormat.format(totalDebeQ));
//                footerRow.getCell(HABERQ_PROPERTY).setText(numberFormat.format(totalHaberQ));
//                footerRow.getCell(SALDOQ_FINAL_PROPERTY).setText(numberFormat.format(totalSaldoFinalQ));
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar empresas : " + ex1.getMessage());
            ex1.printStackTrace();
        }

    }

    public double getDebeHaberCuenta(boolean enQuetzales, String idNomenclatura, String rubro) throws SQLException {
        double saldo = 0.00;

        Calendar c = Calendar.getInstance();

        String queryString  = " Select IfNull(SUM(" + rubro + "), 0.00) As Saldo";
        queryString += " From contabilidad_partida";
        queryString += " Where IdNomenclatura = " + idNomenclatura;
        queryString += " And   IdEmpresa = " + String.valueOf(empresaCbx.getValue());
        queryString += " And   Extract(YEAR From contabilidad_partida.Fecha) = " + c.get(Calendar.YEAR);
        queryString += " And   Fecha <= '" + Utileria.getFechaYYYYMMDD_1(finDt.getValue()) + "'";
        queryString += " And   Estatus <> 'ANULADO'";
//        queryString += " And   TipoDocumento NOT IN ('PARTIDA CIERRE', 'PARTIDA APERTURA', 'PARTIDA INICIAL')";

        stQuery2 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
        rsRecords2 = stQuery2.executeQuery(queryString);

        if(rsRecords2.next()) {
            saldo = rsRecords2.getBigDecimal("Saldo").setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        }
        
        return saldo;
    }

    public BigDecimal getSaldoCuentaMesAnterior(boolean enQuetzales, String idNomenclatura) throws SQLException {
        BigDecimal saldo = new BigDecimal(0.00);

        Calendar c = Calendar.getInstance();
        c.set(Calendar.MONTH, 11);
        c.set(Calendar.DATE, 31);
        c.add(Calendar.YEAR, -1);
/**  la unica forma de consultar el mes anterior  cuando no se han cerrado los meses...
        Calendar c = Calendar.getInstance();
        c.set(Integer.valueOf(mesCierre.substring(0, 4)), Integer.valueOf(mesCierre.substring(4, 6)) - 1, 1);
        c.add(Calendar.MONTH, -1);
**/

        String queryString = "";
        queryString = " Select IfNull(SaldoFinal, 0) SaldoMesAnterior ";
        queryString += " From contabilidad_balance_saldo";
        queryString += " Where IdNomenclatura = " + idNomenclatura;
        queryString += " And  IdEmpresa = " + empresaCbx.getValue();
        queryString += " And  AnioMesCierre = " + c.get(Calendar.YEAR) + "12";
//        queryString += " And  Estatus <> 'ANULADO'";

//System.out.println("query saldo mes anterior = " + queryString);

        stQuery2 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
        rsRecords2 = stQuery2.executeQuery(queryString);

        if(rsRecords2.next()) { //  encontrado        
            saldo = rsRecords2.getBigDecimal("SaldoMesAnterior").setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        
        return saldo;
    }
}
