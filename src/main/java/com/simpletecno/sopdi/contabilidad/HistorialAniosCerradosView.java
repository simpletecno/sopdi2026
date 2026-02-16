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
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.FooterRow;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import org.vaadin.ui.NumberField;

/**
 *
 * @author user
 */
public class HistorialAniosCerradosView extends VerticalLayout implements View {

    UI mainUI;
    Statement stQuery, stQuery1, stQuery2;
    ResultSet rsRecords, rsRecords1, rsRecords2;

    ComboBox anioCbx;
    
    ComboBox empresaCbx;
    String empresa;

    IndexedContainer balanceSaldosContainer;
    Grid balanceSaldosGrid;
    FooterRow footerRow;

    Button exportExcelBtn;
    PopupDateField monthDt;
    NumberField diferenciaSaldos;

    static final String ID_PROPERTY = "Id";
    static final String CUENTA_PROPERTY = "Cuenta";
    static final String DESCRIPCION_PROPERTY = "Descripción";
    static final String DEBE_PROPERTY = "Debe";
    static final String HABER_PROPERTY = "Haber";
    static final String DEBE_INVERSO_PROPERTY = "Saldo Debe Inverso";
    static final String HABER_INVERSO_PROPERTY = "Saldo Haber Inverso";

    static DecimalFormat numberFormat = new DecimalFormat("###,###,##0.00");

    static final Utileria UTILERIA = new Utileria();

    public HistorialAniosCerradosView() {
        this.mainUI = UI.getCurrent();
        setWidth("100%");
        setSpacing(true);
        setMargin(true);
        setHeightUndefined();

        Label titleLbl = new Label("HISTORIAL AÑOS CERRAADOS");
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
        layoutGridBalanceSaldos.setSpacing(true);
        layoutGridBalanceSaldos.setHeightUndefined();
        layoutGridBalanceSaldos.addStyleName("rcorners3");

        HorizontalLayout layoutButtons = new HorizontalLayout();
        layoutButtons.setMargin(true);
        layoutButtons.setSpacing(true);

        HorizontalLayout filterLayout = new HorizontalLayout();
        filterLayout.setSpacing(true);
        filterLayout.setMargin(new MarginInfo(false, true, false, true));

        anioCbx = new ComboBox("Año:");
        anioCbx.setWidth("80px");
        llenarAnios();
        anioCbx.addValueChangeListener((event) -> {
            llenarGridBalanceSaldos();
        });        

        diferenciaSaldos = new NumberField("Diferencia Acumulada : ");
        diferenciaSaldos.setDecimalAllowed(true);
        diferenciaSaldos.setDecimalPrecision(4);
        diferenciaSaldos.setMinimumFractionDigits(2);
        diferenciaSaldos.setDecimalSeparator('.');
        diferenciaSaldos.setDecimalSeparatorAlwaysShown(true);
        diferenciaSaldos.setValue(0d);
        diferenciaSaldos.setGroupingUsed(true);
        diferenciaSaldos.setGroupingSeparator(',');
        diferenciaSaldos.setGroupingSize(3);
        diferenciaSaldos.setImmediate(true);
        diferenciaSaldos.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        diferenciaSaldos.setWidth("11em");
        diferenciaSaldos.setReadOnly(true);     

        balanceSaldosContainer = new IndexedContainer();
        balanceSaldosContainer.addContainerProperty(ID_PROPERTY, String.class, null);
        balanceSaldosContainer.addContainerProperty(CUENTA_PROPERTY, String.class, null);
        balanceSaldosContainer.addContainerProperty(DESCRIPCION_PROPERTY, String.class, null);
        balanceSaldosContainer.addContainerProperty(DEBE_PROPERTY, String.class, null);
        balanceSaldosContainer.addContainerProperty(HABER_PROPERTY, String.class, null);
        balanceSaldosContainer.addContainerProperty(DEBE_INVERSO_PROPERTY, String.class, null);
        balanceSaldosContainer.addContainerProperty(HABER_INVERSO_PROPERTY, String.class, null);

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
            } else if (DEBE_INVERSO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (HABER_INVERSO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }

        });

        balanceSaldosGrid.getColumn(CUENTA_PROPERTY).setExpandRatio(1);
        balanceSaldosGrid.getColumn(DESCRIPCION_PROPERTY).setExpandRatio(3);
        balanceSaldosGrid.getColumn(DEBE_PROPERTY).setExpandRatio(1);
        balanceSaldosGrid.getColumn(HABER_PROPERTY).setExpandRatio(1);
        balanceSaldosGrid.getColumn(DEBE_INVERSO_PROPERTY).setExpandRatio(1);
        balanceSaldosGrid.getColumn(HABER_INVERSO_PROPERTY).setExpandRatio(1);

        footerRow = balanceSaldosGrid.appendFooterRow();
        footerRow.getCell(DESCRIPCION_PROPERTY).setText("SUMAS IGUALES");
        footerRow.getCell(DEBE_PROPERTY).setText("0.00");
        footerRow.getCell(HABER_PROPERTY).setText("0.00");
        footerRow.getCell(DEBE_INVERSO_PROPERTY).setText("0.00");
        footerRow.getCell(HABER_INVERSO_PROPERTY).setText("0.00");

        footerRow.getCell(DESCRIPCION_PROPERTY).setStyleName("rightalign");
        footerRow.getCell(DEBE_PROPERTY).setStyleName("rightalign");
        footerRow.getCell(HABER_PROPERTY).setStyleName("rightalign");
        footerRow.getCell(DEBE_INVERSO_PROPERTY).setStyleName("rightalign");
        footerRow.getCell(HABER_INVERSO_PROPERTY).setStyleName("rightalign");

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

        filterLayout.addComponents(anioCbx, diferenciaSaldos);
        filterLayout.setComponentAlignment(anioCbx, Alignment.MIDDLE_LEFT);

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

    public void llenarAnios() {

        ArrayList<String> anios = new ArrayList<String>();
        Calendar todayCal = Calendar.getInstance();

        anios.add(String.valueOf(todayCal.get(Calendar.YEAR) + 1));
        anios.add(String.valueOf(todayCal.get(Calendar.YEAR)));
        anios.add(String.valueOf(todayCal.get(Calendar.YEAR) - 1));
        anios.add(String.valueOf(todayCal.get(Calendar.YEAR) - 2));
        anios.add(String.valueOf(todayCal.get(Calendar.YEAR) - 3));

        try {

            for (int i = 0; i < anios.size(); i++) { // Recorrer array con años

                String queryString = " SELECT * from contabilidad_partida"; // buscar si el año tiene partida inicial
                queryString += " where Descripcion like 'PARTIDA INICIAL %'";
                queryString += " And Extract(YEAR From Fecha) =" + anios.get(i);

                stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                rsRecords = stQuery.executeQuery(queryString);

                if (rsRecords.next()) { // El año tiene partida inicial

                    // verificar si el año ya esta cerrado
                    queryString = " SELECT * from contabilidad_partida";
                    queryString += " where Descripcion like 'PARTIDA CIERRE %'";
                    queryString += " And Extract(YEAR From Fecha) =" + anios.get(i);

                    stQuery2 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                    rsRecords2 = stQuery2.executeQuery(queryString);

                    if (rsRecords2.next()) {
                        anioCbx.addItem(anios.get(i));
                    }
                }

            }
        } catch (Exception ex) {
            System.out.println("error " + ex);
        }

    }

    public void llenarGridBalanceSaldos() {

        BigDecimal totalDebe = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal diferenciaDeSaldos = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal totalHaber = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal totalDebeInverso = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal totalHaberInverso = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);

        double diferencia;

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

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado     

                do {

                    queryString = " Select IfNull(SUM(contabilidad_partida.DebeQuetzales), 0.00) as DEBEQ,";
                    queryString += " IfNull(SUM(contabilidad_partida.HaberQuetzales), 0.00) as HABERQ ";
                    queryString += " From contabilidad_partida";
                    queryString += " Where contabilidad_partida.IdEmpresa = " + empresa;
                    queryString += " And contabilidad_partida.IdNomenclatura = " + rsRecords.getString("IdNomenclatura");
                    queryString += " And contabilidad_partida.Estatus <> 'ANULADO'";
                    queryString += " And Extract(YEAR From contabilidad_partida.Fecha) = " + anioCbx.getValue();

                    rsRecords1 = stQuery1.executeQuery(queryString);
                    rsRecords1.next();

                    itemId = balanceSaldosContainer.addItem();
                    balanceSaldosContainer.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords.getString("IdNomenclatura"));
                    balanceSaldosContainer.getContainerProperty(itemId, CUENTA_PROPERTY).setValue(rsRecords.getString("NoCuenta"));
                    balanceSaldosContainer.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(rsRecords.getString("N5"));

                    if (rsRecords.getString("NoCuenta").startsWith("1")
                            || rsRecords.getString("NoCuenta").startsWith("2")) {
                        balanceSaldosContainer.getContainerProperty(itemId, DEBE_PROPERTY).setValue(numberFormat.format(Double.parseDouble(String.valueOf(rsRecords1.getDouble("DebeQ")))));
                        balanceSaldosContainer.getContainerProperty(itemId, DEBE_INVERSO_PROPERTY).setValue(numberFormat.format(Double.parseDouble(String.valueOf(rsRecords1.getDouble("DebeQ") - rsRecords1.getDouble("HaberQ")))));
                        balanceSaldosContainer.getContainerProperty(itemId, HABER_PROPERTY).setValue(numberFormat.format(Double.parseDouble(String.valueOf(rsRecords1.getDouble("HaberQ")))));
                        balanceSaldosContainer.getContainerProperty(itemId, HABER_INVERSO_PROPERTY).setValue("0.00");

                        totalDebe = totalDebe.add(new BigDecimal(rsRecords1.getDouble("DebeQ")).setScale(2, BigDecimal.ROUND_HALF_UP));
                        totalDebeInverso = totalDebeInverso.add(new BigDecimal(rsRecords1.getDouble("DebeQ") - rsRecords1.getDouble("HaberQ")).setScale(2, BigDecimal.ROUND_HALF_UP));

                    } else {
                        balanceSaldosContainer.getContainerProperty(itemId, DEBE_PROPERTY).setValue(numberFormat.format(Double.parseDouble(String.valueOf(rsRecords1.getDouble("DebeQ")))));
                        balanceSaldosContainer.getContainerProperty(itemId, DEBE_INVERSO_PROPERTY).setValue("0.00");
                        balanceSaldosContainer.getContainerProperty(itemId, HABER_PROPERTY).setValue(numberFormat.format(Double.parseDouble(String.valueOf(rsRecords1.getDouble("HaberQ")))));
                        balanceSaldosContainer.getContainerProperty(itemId, HABER_INVERSO_PROPERTY).setValue(numberFormat.format(Double.parseDouble(String.valueOf(rsRecords1.getDouble("HaberQ") - rsRecords1.getDouble("DebeQ")))));

                        totalHaber = totalHaber.add(new BigDecimal(rsRecords1.getDouble("HaberQ")).setScale(2, BigDecimal.ROUND_HALF_UP));
                        totalHaberInverso = totalHaberInverso.add(new BigDecimal(rsRecords1.getDouble("HaberQ") - rsRecords1.getDouble("DebeQ")).setScale(2, BigDecimal.ROUND_HALF_UP));
                    }

                } while (rsRecords.next());

                footerRow.getCell(DEBE_PROPERTY).setText(numberFormat.format(totalDebe));
                footerRow.getCell(HABER_PROPERTY).setText(numberFormat.format(totalHaber));
                footerRow.getCell(DEBE_INVERSO_PROPERTY).setText(numberFormat.format(totalDebeInverso));
                footerRow.getCell(HABER_INVERSO_PROPERTY).setText(numberFormat.format(totalHaberInverso));
                diferenciaSaldos.setReadOnly(false);

                diferenciaDeSaldos = diferenciaDeSaldos.add(new BigDecimal(Double.parseDouble(String.valueOf(totalDebeInverso)) - Double.valueOf(String.valueOf(totalHaberInverso))).setScale(2, BigDecimal.ROUND_HALF_UP));

                diferenciaSaldos.setValue(Double.valueOf(String.valueOf(diferenciaDeSaldos)));
                diferenciaSaldos.setReadOnly(true);

            }

        } catch (Exception ex1) {
            System.out.println("Error al listar cuentas : " + ex1.getMessage());
            ex1.printStackTrace();
        }

    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - Cierre Anual");
    }

}
