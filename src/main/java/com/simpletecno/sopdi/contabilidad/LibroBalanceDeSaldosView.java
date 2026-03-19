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
import com.vaadin.data.Property;
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
import org.vaadin.ui.NumberField;

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
public class LibroBalanceDeSaldosView extends VerticalLayout implements View {

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
    ComboBox mesCbx;
    Button consultarBtn;
    CheckBox omitirSinMovimientoChk;
    Button alDiaBtn;

    IndexedContainer balanceSaldosContainer;
    Grid balanceSaldosGrid;
    FooterRow footerRow;

    NumberField folioTxt;
    Button exportExcelBtn;

    static final String ID_PROPERTY = "Id";
    static final String CUENTA_PROPERTY = "Cuenta";
    static final String DESCRIPCION_PROPERTY = "Descripción";
    static final String SALDO_ANTERIOR_PROPERTY = "S.Anterior";
    static final String DEBE_PROPERTY = "Debe";
    static final String HABER_PROPERTY = "Haber";
    static final String SALDO_FINAL_PROPERTY = "S.Final";

    static DecimalFormat numberFormat = new DecimalFormat("###,###,##0.00");

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public LibroBalanceDeSaldosView() {
        this.mainUI = UI.getCurrent();
        setWidth("100%");
        setSpacing(true);
        setMargin(true);
        setHeightUndefined();

        Label titleLbl = new Label(empresaId + " " + empresaNombre + " BALANCE SALDOS");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
//        titleLbl.addStyleName("h2_custom");

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setResponsive(true);
        titleLayout.setSpacing(true);
        titleLayout.setWidth("100%");
        titleLayout.setMargin(false);
        titleLayout.addComponents(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.MIDDLE_CENTER);
        titleLayout.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

        addComponent(titleLayout);
        setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        crearGridBalanceSaldos();
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
        anioCbx.setInvalidAllowed(false);
        anioCbx.setNewItemsAllowed(false);
        anioCbx.setNullSelectionAllowed(false);

        Calendar todayCal = Calendar.getInstance();
        anioCbx.addItem(String.valueOf(todayCal.get(Calendar.YEAR) - 5));
        anioCbx.addItem(String.valueOf(todayCal.get(Calendar.YEAR) - 4));
        anioCbx.addItem(String.valueOf(todayCal.get(Calendar.YEAR) - 3));
        anioCbx.addItem(String.valueOf(todayCal.get(Calendar.YEAR) - 2));
        anioCbx.addItem(String.valueOf(todayCal.get(Calendar.YEAR) - 1));
        anioCbx.addItem(String.valueOf(todayCal.get(Calendar.YEAR)));
        anioCbx.select(String.valueOf(todayCal.get(Calendar.YEAR)));
        anioCbx.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                llenarComboMes();
            }
        });

        mesCbx = new ComboBox("Mes cerrado:");
        mesCbx.setInvalidAllowed(false);
        mesCbx.setNewItemsAllowed(false);
        mesCbx.setNullSelectionAllowed(false);
        llenarComboMes();

        omitirSinMovimientoChk = new CheckBox("Omitir sin movimientos");
        omitirSinMovimientoChk.setValue(false);

        consultarBtn = new Button("Consultar");
        consultarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        consultarBtn.setIcon(FontAwesome.SEARCH);
        consultarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                llenarGridBalanceSaldos();
            }
        });

        alDiaBtn = new Button("Saldos al Dia");
        alDiaBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        alDiaBtn.setIcon(FontAwesome.SEARCH);
        alDiaBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                BalanceGeneralAlDia balanceGeneralAlDia = new BalanceGeneralAlDia();
                mainUI.addWindow(balanceGeneralAlDia);
                balanceGeneralAlDia.center();

            }
        });

        balanceSaldosContainer = new IndexedContainer();
        balanceSaldosContainer.addContainerProperty(ID_PROPERTY, String.class, null);
        balanceSaldosContainer.addContainerProperty(CUENTA_PROPERTY, String.class, null);
        balanceSaldosContainer.addContainerProperty(DESCRIPCION_PROPERTY, String.class, null);
        balanceSaldosContainer.addContainerProperty(SALDO_ANTERIOR_PROPERTY, String.class, null);
        balanceSaldosContainer.addContainerProperty(DEBE_PROPERTY, String.class, null);
        balanceSaldosContainer.addContainerProperty(HABER_PROPERTY, String.class, null);
        balanceSaldosContainer.addContainerProperty(SALDO_FINAL_PROPERTY, String.class, null);

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
        footerRow.getCell(SALDO_ANTERIOR_PROPERTY).setText("0.00");
        footerRow.getCell(DESCRIPCION_PROPERTY).setText("SUMAS IGUALES");
        footerRow.getCell(DEBE_PROPERTY).setText("0.00");
        footerRow.getCell(HABER_PROPERTY).setText("0.00");
        footerRow.getCell(SALDO_FINAL_PROPERTY).setText("0.00");

        footerRow.getCell(DESCRIPCION_PROPERTY).setStyleName("rightalign");
        footerRow.getCell(SALDO_ANTERIOR_PROPERTY).setStyleName("rightalign");
        footerRow.getCell(DEBE_PROPERTY).setStyleName("rightalign");
        footerRow.getCell(HABER_PROPERTY).setStyleName("rightalign");
        footerRow.getCell(SALDO_FINAL_PROPERTY).setStyleName("rightalign");

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

        folioTxt = new NumberField("Folio Inicial :");
        folioTxt.setDecimalAllowed(false);
        folioTxt.setDecimalPrecision(0);
        folioTxt.setMinimumFractionDigits(0);
        folioTxt.setDecimalSeparatorAlwaysShown(false);
        folioTxt.setValue(1d);
        folioTxt.setGroupingUsed(true);
        folioTxt.setGroupingSize(0);
        folioTxt.setImmediate(true);
        folioTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);
        folioTxt.setWidth("8em");
        folioTxt.setValidationVisible(false);

        Button generarPDF = new Button("Generar PDF");
        generarPDF.setIcon(FontAwesome.PAPER_PLANE);
        generarPDF.addStyleName(ValoTheme.BUTTON_PRIMARY);

        generarPDF.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (balanceSaldosContainer.size() > 0) {
//                    Collection collMes = (Collection)mesCbx.getValue();
//                    Iterator iterMes = collMes.iterator();
//                    String meses = "";
//                    while(iterMes.hasNext()) {
//                        if(iterMes.hasNext()) {
//                            meses += iterMes.next();
//                        }
//                        if(iterMes.hasNext()) {
//                            meses += ",";
//                        }
//                    }
                    LibroBalanceDeSaldosPDF libroBalancePdf
                            = new LibroBalanceDeSaldosPDF(
                                    empresaId,
                                    empresaNombre,
                                    ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyTaxId(),
                                    balanceSaldosContainer,
                                    String.valueOf(anioCbx.getValue()),
                                    String.valueOf(mesCbx.getValue()),
                                    folioTxt.getValue()
                            );
                    mainUI.addWindow(libroBalancePdf);
                    libroBalancePdf.center();

                } else {
                    Notification.show("La vista no contiene registros disponibles.. ", Notification.Type.WARNING_MESSAGE);
                }
            }
        });
         
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
                    fileexport = "BalanceSaldos_" + empresaNombre.replaceAll(" ", "_").replaceAll(",", "_").replaceAll("[()]", "").replaceAll("[.]", "").replaceAll("ñ", "n").replaceAll("Ñ", "N").replaceAll("ó", "o").replaceAll("é", "") + ".xls";
                    excelExport.setExportFileName(fileexport);
                    excelExport.export();
                }
            }
        });

        filterLayout.addComponents(anioCbx, mesCbx, omitirSinMovimientoChk, consultarBtn, alDiaBtn);
        filterLayout.setComponentAlignment(anioCbx, Alignment.MIDDLE_LEFT);
        filterLayout.setComponentAlignment(mesCbx, Alignment.BOTTOM_LEFT);
        filterLayout.setComponentAlignment(omitirSinMovimientoChk, Alignment.MIDDLE_LEFT);
        filterLayout.setComponentAlignment(consultarBtn, Alignment.MIDDLE_CENTER);
        filterLayout.setComponentAlignment(alDiaBtn, Alignment.MIDDLE_RIGHT);

        layoutButtons.addComponent(folioTxt);
        layoutButtons.setComponentAlignment(folioTxt, Alignment.MIDDLE_CENTER);
        layoutButtons.addComponent(generarPDF);
        layoutButtons.setComponentAlignment(generarPDF, Alignment.BOTTOM_CENTER);
        layoutButtons.addComponent(exportExcelBtn);
        layoutButtons.setComponentAlignment(exportExcelBtn, Alignment.BOTTOM_CENTER);

        layoutGridBalanceSaldos.addComponent(filterLayout);
        layoutGridBalanceSaldos.setComponentAlignment(filterLayout, Alignment.MIDDLE_CENTER);

        layoutGridBalanceSaldos.addComponent(balanceSaldosGrid);
        layoutGridBalanceSaldos.addComponent(layoutButtons);
        layoutGridBalanceSaldos.setComponentAlignment(layoutButtons, Alignment.MIDDLE_CENTER);

        addComponent(layoutGridBalanceSaldos);
        setComponentAlignment(layoutGridBalanceSaldos, Alignment.MIDDLE_CENTER);

    }

    private void llenarComboMes() {

        mesCbx.removeAllItems();

        mesCbx.addItem("01");
        mesCbx.setItemCaption("01", NOMBREMES.get("01"));
        mesCbx.addItem("02");
        mesCbx.setItemCaption("02", NOMBREMES.get("02"));
        mesCbx.addItem("03");
        mesCbx.setItemCaption("03", NOMBREMES.get("03"));
        mesCbx.addItem("04");
        mesCbx.setItemCaption("04", NOMBREMES.get("04"));
        mesCbx.addItem("05");
        mesCbx.setItemCaption("05", NOMBREMES.get("05"));
        mesCbx.addItem("06");
        mesCbx.setItemCaption("06", NOMBREMES.get("06"));
        mesCbx.addItem("07");
        mesCbx.setItemCaption("07", NOMBREMES.get("07"));
        mesCbx.addItem("08");
        mesCbx.setItemCaption("08", NOMBREMES.get("08"));
        mesCbx.addItem("09");
        mesCbx.setItemCaption("09", NOMBREMES.get("09"));
        mesCbx.addItem("10");
        mesCbx.setItemCaption("10", NOMBREMES.get("10"));
        mesCbx.addItem("11");
        mesCbx.setItemCaption("11", NOMBREMES.get("11"));
        mesCbx.addItem("12");
        mesCbx.setItemCaption("12", NOMBREMES.get("12"));
    }

    public void llenarGridBalanceSaldos() {

        if(mesCbx.size() == 0) {
            Notification.show("Año " + anioCbx.getValue() + " NO TIENE NINGUN MES CERRADO.", Notification.Type.WARNING_MESSAGE);
            anioCbx.focus();
            return;
        }

        BigDecimal totalSaldoAnterior = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal totalDebe = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal totalHaber = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal totalSaldoFinal = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);

        if (balanceSaldosContainer == null) {
            return;
        }

        balanceSaldosContainer.removeAllItems();
        balanceSaldosGrid.getContainerDataSource().removeAllItems();
        balanceSaldosContainer.removeAllContainerFilters();

        Object itemId;

        String queryString = " SELECT * FROM contabilidad_nomenclatura";
        queryString += " WHERE Estatus = 'HABILITADA'";
        queryString += " ORDER BY Cast(NoCuenta AS UNSIGNED)";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado     

                do {

                    queryString = " SELECT *";
                    queryString += " FROM contabilidad_balance_saldo";
                    queryString += " WHERE IdEmpresa = " + empresaId;
                    queryString += " AND IdNomenclatura = " + rsRecords.getString("IdNomenclatura");
                    queryString += " AND AnioMesCierre = '" + anioCbx.getValue() + mesCbx.getValue() + "'";

//System.out.println(queryString);

                    rsRecords1 = stQuery1.executeQuery(queryString);

                    if(omitirSinMovimientoChk.getValue()) {
                        if(rsRecords1.getDouble("SaldoAnterior") == 0.00 && rsRecords1.getDouble("SaldoFinal") == 0.00) {
                            continue;
                        }
                    }

                    itemId = balanceSaldosContainer.addItem();
                    balanceSaldosContainer.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords.getString("IdNomenclatura"));
                    balanceSaldosContainer.getContainerProperty(itemId, CUENTA_PROPERTY).setValue(rsRecords.getString("NoCuenta"));
                    balanceSaldosContainer.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(rsRecords.getString("N5"));

                    if(rsRecords1.next()) {
                        balanceSaldosContainer.getContainerProperty(itemId, SALDO_ANTERIOR_PROPERTY).setValue(numberFormat.format(rsRecords1.getDouble("SaldoAnterior")));
                        balanceSaldosContainer.getContainerProperty(itemId, DEBE_PROPERTY).setValue(numberFormat.format(rsRecords1.getDouble("Debe")));
                        balanceSaldosContainer.getContainerProperty(itemId, HABER_PROPERTY).setValue(numberFormat.format(rsRecords1.getDouble("Haber")));
                        balanceSaldosContainer.getContainerProperty(itemId, SALDO_FINAL_PROPERTY).setValue(numberFormat.format(rsRecords1.getDouble("SaldoFinal")));
                        totalSaldoAnterior = totalSaldoAnterior.add(new BigDecimal(rsRecords1.getDouble("SaldoAnterior"))).setScale(2, BigDecimal.ROUND_HALF_UP);
                        totalSaldoFinal = totalSaldoFinal.add(new BigDecimal(rsRecords1.getDouble("SaldoFinal"))).setScale(2, BigDecimal.ROUND_HALF_UP);
                        totalDebe = totalDebe.add(new BigDecimal(rsRecords1.getDouble("Debe")).setScale(2, BigDecimal.ROUND_HALF_UP));
                        totalHaber = totalHaber.add(new BigDecimal(rsRecords1.getDouble("Haber")).setScale(2, BigDecimal.ROUND_HALF_UP));
                    }
                    else {
                        balanceSaldosContainer.getContainerProperty(itemId, SALDO_ANTERIOR_PROPERTY).setValue("0.00");
                        balanceSaldosContainer.getContainerProperty(itemId, DEBE_PROPERTY).setValue("0.00");
                        balanceSaldosContainer.getContainerProperty(itemId, HABER_PROPERTY).setValue("0.00");
                        balanceSaldosContainer.getContainerProperty(itemId, SALDO_FINAL_PROPERTY).setValue("0.00");
                    }

                } while (rsRecords.next());

                footerRow.getCell(SALDO_ANTERIOR_PROPERTY).setText(numberFormat.format(totalSaldoAnterior));
                footerRow.getCell(DEBE_PROPERTY).setText(numberFormat.format(totalDebe));
                footerRow.getCell(HABER_PROPERTY).setText(numberFormat.format(totalHaber));
                footerRow.getCell(SALDO_FINAL_PROPERTY).setText(numberFormat.format(totalSaldoFinal));
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar empresas : " + ex1.getMessage());
            ex1.printStackTrace();
        }

    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - Libro balance de saldos");
    }

}
