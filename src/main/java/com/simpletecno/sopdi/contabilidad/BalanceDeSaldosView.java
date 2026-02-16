/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.contabilidad;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.addon.tableexport.DefaultTableHolder;
import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.addon.tableexport.TableHolder;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.Grid.FooterRow;
import com.vaadin.ui.themes.ValoTheme;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.LinkedHashMap;

/**
 *
 * @author user
 */
public class BalanceDeSaldosView extends VerticalLayout implements View {

    private static final LinkedHashMap<String, String> NOMBREMES = new LinkedHashMap<String, String>();
    static {
        NOMBREMES.put("01", "ENERO");
        NOMBREMES.put("02", "FEBRERO");
        NOMBREMES.put("03", "MARZO");
        NOMBREMES.put("04", "ABRIL");
        NOMBREMES.put("05", "MAYO");
        NOMBREMES.put("06", "JUNIO");
        NOMBREMES.put("07", "JULIO");
        NOMBREMES.put("08", "AGOSTO");
        NOMBREMES.put("09", "SEPTIEMBRE");
        NOMBREMES.put("10", "OCTUBRE");
        NOMBREMES.put("11", "NOVIEMBRE");
        NOMBREMES.put("12", "DICIEMBRE");
    }

    UI mainUI;
    Statement stQuery, stQuery1, stQuery2;
    ResultSet rsRecords, rsRecords1;

    ComboBox anioCbx;
    Button consultarBtn;

    ComboBox empresaCbx;
    String empresa;

    IndexedContainer balanceSaldosContainer;
    Grid balanceSaldosGrid;
    FooterRow footerRow;

    Button exportExcelBtn;

    static final String ID_PROPERTY = "Id";
    static final String CUENTA_PROPERTY = "Cuenta";
    static final String DESCRIPCION_PROPERTY = "Descripción";
    static final String SALDO_ANTERIOR_PROPERTY = "S.Anterior";
    static final String DEBE_PROPERTY = "Debe";
    static final String HABER_PROPERTY = "Haber";
    static final String SALDO_FINAL_PROPERTY = "S.Final";

    static DecimalFormat numberFormat = new DecimalFormat("###,###,##0.00");

    static final Utileria UTILERIA = new Utileria();

    public BalanceDeSaldosView() {
        this.mainUI = UI.getCurrent();
        setWidth("100%");
        setSpacing(true);
        setMargin(true);
        setHeightUndefined();

        Label titleLbl = new Label("BALANCE SALDOS");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
//        titleLbl.addStyleName("h2_custom");

        empresaCbx = new ComboBox("Empresa:");
        empresaCbx.setWidth("400px");
        empresaCbx.addStyleName(ValoTheme.COMBOBOX_HUGE);
        empresaCbx.setInvalidAllowed(false);
        empresaCbx.setNewItemsAllowed(false);
        empresaCbx.setTextInputAllowed(false);
        empresaCbx.setNullSelectionAllowed(false);

        llenarComboEmpresa();

        empresaCbx.addValueChangeListener(event -> {
            empresa = String.valueOf(event.getProperty().getValue());
            llenarGridBalanceSaldos();
        });

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setResponsive(true);
        titleLayout.setSpacing(true);
        titleLayout.setWidth("100%");
        titleLayout.setMargin(false);
        titleLayout.addComponents(empresaCbx, titleLbl);
        titleLayout.setComponentAlignment(empresaCbx, Alignment.MIDDLE_CENTER);
        titleLayout.setComponentAlignment(titleLbl, Alignment.MIDDLE_CENTER);
        titleLayout.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

        addComponent(titleLayout);
        setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        crearGridBalanceSaldos();

        empresa = String.valueOf(empresaCbx.getValue());

    }

    public void llenarComboEmpresa() {
        String queryString = " SELECT * from contabilidad_empresa";
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

    public void crearGridBalanceSaldos() {
        VerticalLayout layoutGridBalanceSaldos = new VerticalLayout();
        layoutGridBalanceSaldos.setWidth("100%");
//        layoutGridBalanceSaldos.setHeightUndefined();
        layoutGridBalanceSaldos.addStyleName("rcorners3");

        HorizontalLayout layoutButtons = new HorizontalLayout();
        layoutButtons.setMargin(true);
        layoutButtons.setSpacing(true);

        HorizontalLayout filterLayout = new HorizontalLayout();
        filterLayout.setSpacing(true);
        filterLayout.setMargin(new MarginInfo(false, true, false, true));

        anioCbx = new ComboBox("Año:");
        anioCbx.setWidth("80px");
        Calendar todayCal = Calendar.getInstance();
        anioCbx.addItem(String.valueOf(todayCal.get(Calendar.YEAR) - 5));
        anioCbx.addItem(String.valueOf(todayCal.get(Calendar.YEAR) - 4));
        anioCbx.addItem(String.valueOf(todayCal.get(Calendar.YEAR) - 3));
        anioCbx.addItem(String.valueOf(todayCal.get(Calendar.YEAR) - 2));
        anioCbx.addItem(String.valueOf(todayCal.get(Calendar.YEAR) - 1));
        anioCbx.addItem(String.valueOf(todayCal.get(Calendar.YEAR)));
        anioCbx.select(String.valueOf(todayCal.get(Calendar.YEAR)));

        consultarBtn = new Button("Consultar");
        consultarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        consultarBtn.setIcon(FontAwesome.SEARCH);
        consultarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                llenarGridBalanceSaldos();
            }
        });

        balanceSaldosContainer = new IndexedContainer();
        balanceSaldosContainer.addContainerProperty(ID_PROPERTY, String.class, "");
        balanceSaldosContainer.addContainerProperty(CUENTA_PROPERTY, String.class, "");
        balanceSaldosContainer.addContainerProperty(DESCRIPCION_PROPERTY, String.class, "");

        for(String mes : NOMBREMES.values()) {
            balanceSaldosContainer.addContainerProperty(SALDO_ANTERIOR_PROPERTY + "_" + mes, String.class, "0.00");
            balanceSaldosContainer.addContainerProperty(DEBE_PROPERTY + "_" + mes, String.class, "0.00");
            balanceSaldosContainer.addContainerProperty(HABER_PROPERTY + "_" + mes, String.class, "0.00");
            balanceSaldosContainer.addContainerProperty(SALDO_FINAL_PROPERTY + "_" + mes, String.class, "0.00");

        }

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
            if (cellReference.getPropertyId().toString().contains(DEBE_PROPERTY)) {
                return "rightalign";
            } else if (cellReference.getPropertyId().toString().contains(HABER_PROPERTY)) {
                return "rightalign";
            } else if (cellReference.getPropertyId().toString().contains(SALDO_ANTERIOR_PROPERTY)) {
                return "rightalign";
            } else if (cellReference.getPropertyId().toString().contains(SALDO_FINAL_PROPERTY)) {
                return "rightalign";
            } else {
                return null;
            }

        });

        footerRow = balanceSaldosGrid.appendFooterRow();
        footerRow.getCell(DESCRIPCION_PROPERTY ).setText("SUMAS IGUALES");
        footerRow.getCell(DESCRIPCION_PROPERTY).setStyleName("rightalign");

        Grid.HeaderRow groupingHeader = balanceSaldosGrid.prependHeaderRow();

        for(String mes : NOMBREMES.values()) {
            Grid.HeaderCell joinedCell = groupingHeader.join(
                    groupingHeader.getCell(SALDO_ANTERIOR_PROPERTY + "_" + mes),
                    groupingHeader.getCell(DEBE_PROPERTY + "_" + mes),
                    groupingHeader.getCell(HABER_PROPERTY + "_" + mes),
                    groupingHeader.getCell(SALDO_FINAL_PROPERTY + "_" + mes));
            joinedCell.setText(mes);
            joinedCell.setStyleName("centeralign");

            footerRow.getCell(SALDO_ANTERIOR_PROPERTY + "_" + mes).setText("0.00");
            footerRow.getCell(DEBE_PROPERTY + "_" + mes).setText("0.00");
            footerRow.getCell(HABER_PROPERTY + "_" + mes).setText("0.00");
            footerRow.getCell(SALDO_FINAL_PROPERTY + "_" + mes).setText("0.00");

            footerRow.getCell(SALDO_ANTERIOR_PROPERTY + "_" + mes).setStyleName("rightalign");
            footerRow.getCell(DEBE_PROPERTY + "_" + mes).setStyleName("rightalign");
            footerRow.getCell(HABER_PROPERTY + "_" + mes).setStyleName("rightalign");
            footerRow.getCell(SALDO_FINAL_PROPERTY + "_" + mes).setStyleName("rightalign");
        }

        balanceSaldosGrid.getColumn(CUENTA_PROPERTY).setExpandRatio(1);
        balanceSaldosGrid.getColumn(DESCRIPCION_PROPERTY).setExpandRatio(3);
//        balanceSaldosGrid.getColumn(SALDO_ANTERIOR_PROPERTY).setExpandRatio(1);
//        balanceSaldosGrid.getColumn(DEBE_PROPERTY).setExpandRatio(1);
//        balanceSaldosGrid.getColumn(HABER_PROPERTY).setExpandRatio(1);
//        balanceSaldosGrid.getColumn(SALDO_FINAL_PROPERTY).setExpandRatio(1);


        Grid.HeaderRow filterRow = balanceSaldosGrid.appendHeaderRow();

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

        layoutGridBalanceSaldos.addComponent(balanceSaldosGrid);

        exportExcelBtn = new Button("Excel");
        exportExcelBtn.setIcon(FontAwesome.FILE_EXCEL_O);
        exportExcelBtn.setWidth(120, Sizeable.UNITS_PIXELS);
        exportExcelBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (balanceSaldosGrid.getHeightByRows() > 0) {
                    TableHolder tableHolder = new DefaultTableHolder(balanceSaldosGrid);
                    ExcelExport excelExport = new ExcelExport(tableHolder);
                    excelExport.excludeCollapsedColumns();
                    excelExport.setDisplayTotals(false);
                    String fileexport;
                    fileexport = "BalanceSaldos_" + empresaCbx.getItemCaption(empresaCbx.getValue()).replaceAll(" ", "_").replaceAll(",", "_").replaceAll("[()]", "").replaceAll("[.]", "").replaceAll("ñ", "n").replaceAll("Ñ", "N").replaceAll("ó", "o").replaceAll("é", "") + ".xls";
                    excelExport.setExportFileName(fileexport);
                    excelExport.export();
                }
            }
        });

        filterLayout.addComponents(anioCbx,  consultarBtn);
        filterLayout.setComponentAlignment(anioCbx, Alignment.MIDDLE_LEFT);
        filterLayout.setComponentAlignment(consultarBtn, Alignment.MIDDLE_CENTER);

        layoutButtons.addComponent(exportExcelBtn);
        layoutButtons.setComponentAlignment(exportExcelBtn, Alignment.MIDDLE_CENTER);

        layoutGridBalanceSaldos.addComponent(filterLayout);
        layoutGridBalanceSaldos.setComponentAlignment(filterLayout, Alignment.MIDDLE_CENTER);

        layoutGridBalanceSaldos.addComponent(balanceSaldosGrid);
        layoutGridBalanceSaldos.addComponent(layoutButtons);
        layoutGridBalanceSaldos.setComponentAlignment(layoutButtons, Alignment.MIDDLE_CENTER);

        addComponent(layoutGridBalanceSaldos);
        setComponentAlignment(layoutGridBalanceSaldos, Alignment.MIDDLE_CENTER);

    }

    public void llenarGridBalanceSaldos() {

        if (balanceSaldosContainer == null) {
            return;
        }

        balanceSaldosContainer.removeAllItems();
        balanceSaldosGrid.getContainerDataSource().removeAllItems();
        balanceSaldosContainer.removeAllContainerFilters();

        Object itemId;
        String stringMes;
        String queryString;

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();

            queryString = " SELECT * from contabilidad_nomenclatura";
            queryString += " where Estatus = 'HABILITADA'";
            queryString += " Order By Cast(NoCuenta AS UNSIGNED)";

            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {

                do {

                    itemId = balanceSaldosContainer.addItem();
                    balanceSaldosContainer.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords.getString("IdNomenclatura"));
                    balanceSaldosContainer.getContainerProperty(itemId, CUENTA_PROPERTY).setValue(rsRecords.getString("NoCuenta"));
                    balanceSaldosContainer.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(rsRecords.getString("N5"));

                    for(int numeroMes = 1; numeroMes <= 12; numeroMes++) {

                        if (numeroMes < 10) {
                            stringMes = "0" + String.valueOf(numeroMes);
                        }
                        else {
                            stringMes = String.valueOf(numeroMes);
                        }

                        queryString = " Select *";
                        queryString += " From contabilidad_balance_saldo";
                        queryString += " Where IdEmpresa = " + empresa;
                        queryString += " And IdNomenclatura = " + rsRecords.getString("IdNomenclatura");
                        queryString += " And AnioMesCierre = '" + anioCbx.getValue() + stringMes + "'";

//System.out.println(queryString);

                        rsRecords1 = stQuery1.executeQuery(queryString);

                        if(rsRecords1.next()) {

                            balanceSaldosContainer.getContainerProperty(itemId, SALDO_ANTERIOR_PROPERTY + "_" + NOMBREMES.get(stringMes)).setValue(numberFormat.format(rsRecords1.getDouble("SaldoAnterior")));
                            balanceSaldosContainer.getContainerProperty(itemId, DEBE_PROPERTY + "_" + NOMBREMES.get(stringMes)).setValue(numberFormat.format(rsRecords1.getDouble("Debe")));
                            balanceSaldosContainer.getContainerProperty(itemId, HABER_PROPERTY + "_" + NOMBREMES.get(stringMes)).setValue(numberFormat.format(rsRecords1.getDouble("Haber")));
                            balanceSaldosContainer.getContainerProperty(itemId, SALDO_FINAL_PROPERTY + "_" + NOMBREMES.get(stringMes)).setValue(numberFormat.format(rsRecords1.getDouble("SaldoFinal")));

                        }
                    } // end for meses

                } while (rsRecords.next());//siguiente cuenta

            } // end if nomenclatura

            BigDecimal totalSaldoAnterior = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
            BigDecimal totalDebe = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
            BigDecimal totalHaber = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
            BigDecimal totalSaldoFinal = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);

            for(String mes : NOMBREMES.values()) {
                for(Object item : balanceSaldosContainer.getItemIds()) {
                    totalSaldoAnterior = totalSaldoAnterior.add(new BigDecimal(Double.valueOf(String.valueOf(balanceSaldosContainer.getContainerProperty(item, SALDO_ANTERIOR_PROPERTY + "_" + mes).getValue()).replaceAll(",", ""))));
                    totalDebe = totalDebe.add(new BigDecimal(Double.valueOf(String.valueOf(balanceSaldosContainer.getContainerProperty(item, DEBE_PROPERTY + "_" + mes).getValue()).replaceAll(",", ""))));
                    totalHaber = totalHaber.add(new BigDecimal(Double.valueOf(String.valueOf(balanceSaldosContainer.getContainerProperty(item, HABER_PROPERTY + "_" + mes).getValue()).replaceAll(",", ""))));
                    totalSaldoFinal = totalSaldoFinal.add(new BigDecimal(Double.valueOf(String.valueOf(balanceSaldosContainer.getContainerProperty(item, SALDO_FINAL_PROPERTY + "_" + mes).getValue()).replaceAll(",", ""))));
                }

                footerRow.getCell(SALDO_ANTERIOR_PROPERTY + "_" + mes).setText(numberFormat.format(totalSaldoAnterior));
                footerRow.getCell(DEBE_PROPERTY + "_" + mes).setText(numberFormat.format(totalDebe));
                footerRow.getCell(HABER_PROPERTY + "_" + mes).setText(numberFormat.format(totalHaber));
                footerRow.getCell(SALDO_FINAL_PROPERTY + "_" + mes).setText(numberFormat.format(totalSaldoFinal));

                totalSaldoAnterior = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
                totalDebe = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
                totalHaber = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
                totalSaldoFinal = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar empresas : " + ex1.getMessage());
            ex1.printStackTrace();
        }

    }

    public String getEmpresaNit() {
        String strNit = "N/A";

        String queryString = " SELECT Nit from contabilidad_empresa ";
        queryString += " Where IdEmpresa = " + empresa;

        try {
            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords1 = stQuery1.executeQuery(queryString);

            if (rsRecords1.next()) {
                strNit = rsRecords1.getString("Nit");
            }

        } catch (Exception ex1) {
            System.out.println("Error al buscar NIT de empresa: " + ex1.getMessage());
            ex1.printStackTrace();
        }

        return strNit;
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - Libro balance de saldos");
    }

}
