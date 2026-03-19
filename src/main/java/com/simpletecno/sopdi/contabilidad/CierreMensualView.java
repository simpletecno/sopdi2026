/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.contabilidad;

import com.simpletecno.sopdi.utilerias.MyEmailMessanger;
import com.simpletecno.sopdi.SopdiUI;
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
import com.vaadin.shared.Position;
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
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vaadin.dialogs.ConfirmDialog;

import javax.mail.MessagingException;

/**
 *
 * @author user
 */
public final class CierreMensualView extends VerticalLayout implements View {

    static final String ID_PROPERTY = "Id";
    static final String CUENTA_PROPERTY = "Cuenta";
    static final String DESCRIPCION_PROPERTY = "Descripción";
    static final String SALDO_ANTERIOR_PROPERTY = "S.Anterior";
    static final String DEBE_PROPERTY = "Debe";
    static final String HABER_PROPERTY = "Haber";
    static final String SALDO_FINAL_PROPERTY = "S.Final";

    static DecimalFormat numberFormat = new DecimalFormat("###,###,##0.00");
    static DecimalFormat numberFormat2 = new DecimalFormat("00");

    UI mainUI;
    Statement stQuery, stQuery1, stQuery2;
    ResultSet rsRecords, rsRecords1, rsRecords2;
    ComboBox mesCbx;
    CheckBox omitirSinMovimientoChk;
    Button consultarBtn;
    Button cerrarBtn;
    Button exportExcelBtn;

    Label waitLbl;

    IndexedContainer balanceSaldosContainer;
    Grid balanceSaldosGrid;
    Grid.FooterRow footerRow;

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

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

    public CierreMensualView() {
        this.mainUI = UI.getCurrent();
        setResponsive(true);
        setWidth("80%");
        setHeight("80%");

        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setSpacing(true);
        contentLayout.setMargin(true);

        addComponent(contentLayout);

        Label titleLbl = new Label(empresaId + " " + empresaNombre + " CIERRE CONTABLE MENSUAL");
        titleLbl.addStyleName(ValoTheme.LABEL_H1);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h1_custom");

        contentLayout.addComponents(titleLbl);
        contentLayout.setComponentAlignment(titleLbl, Alignment.TOP_CENTER);

        mesCbx = new ComboBox("Mes a cerrar :");
        mesCbx.setWidth("15em");
        llenarComboMes();
        mesCbx.setFilteringMode(FilteringMode.CONTAINS);
        mesCbx.setNewItemsAllowed(false);
        mesCbx.setInvalidAllowed(false);
        mesCbx.setNewItemsAllowed(false);
        if (mesCbx.size() > 1) {
            mesCbx.select(mesCbx.getItemIds().iterator().next());
        }

        omitirSinMovimientoChk = new CheckBox("Omitir sin movimientos");
        omitirSinMovimientoChk.setValue(false);

        consultarBtn = new Button("Consultar");
        consultarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        consultarBtn.setIcon(FontAwesome.SEARCH);
        consultarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (mesCbx.getValue() != null) {
                    fillGridBalanceSaldos();
                }
            }
        });

        HorizontalLayout layoutCerrar = new HorizontalLayout();
        layoutCerrar.setMargin(true);
        layoutCerrar.setSpacing(true);
        layoutCerrar.addComponents(mesCbx, omitirSinMovimientoChk, consultarBtn);
        layoutCerrar.setComponentAlignment(mesCbx, Alignment.BOTTOM_RIGHT);
        layoutCerrar.setComponentAlignment(consultarBtn, Alignment.BOTTOM_RIGHT);

        cerrarBtn = new Button("ACEPTAR CERRAR MES");
        cerrarBtn.setIcon(FontAwesome.CHECK);
        cerrarBtn.setStyleName(ValoTheme.BUTTON_FRIENDLY);
        cerrarBtn.setDescription("Click para CERRAR el mes");
        cerrarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (String.valueOf(mesCbx.getValue()).equals("<<SELECCIONE EL MES A CERRAR>>")) {
                    Notification.show("Por favor seleccione un mes a cerrar...", Notification.Type.WARNING_MESSAGE);
                    mesCbx.focus();
                    return;
                }

                ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de CERRAR este mes : " + mesCbx.getItemCaption(mesCbx.getValue()) + " ?",
                        "SI", "NO", new ConfirmDialog.Listener() {

                    public void onClose(ConfirmDialog dialog) {
                        if (dialog.isConfirmed()) {
                            dialog.getOkButton().setDisableOnClick(true);
                            cerrarBtn.setEnabled(false);
                            cierreMensual();
                        }
                    }
                }).getOkButton().setDisableOnClick(true);
            }
        });

        exportExcelBtn = new Button("Exportar a Excel");
        exportExcelBtn.setIcon(FontAwesome.FILE_EXCEL_O);
//        exportExcelBtn.setWidth(130,Sizeable.UNITS_PIXELS);
        exportExcelBtn.addStyleName(ValoTheme.BUTTON_LINK);
        exportExcelBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (balanceSaldosContainer.size() > 0) {
                    exportToExcel();
                }
            }
        });

        waitLbl = new Label("Espere...");
        waitLbl.setVisible(false);
        waitLbl.addStyleName(ValoTheme.LABEL_SUCCESS);

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setWidth("100%");
        buttonsLayout.setSpacing(true);

        buttonsLayout.addComponents(cerrarBtn, exportExcelBtn, waitLbl);
        buttonsLayout.setComponentAlignment(waitLbl, Alignment.BOTTOM_RIGHT);

        contentLayout.addComponents(layoutCerrar, crearGridBalanceSaldos(), buttonsLayout);
        contentLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);

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

        return (layoutGridBalanceSaldos);

    }

    public void llenarComboMes() {

        if (mesCbx == null) {
            return;
        }

        mesCbx.removeAllItems();

        String queryString = "";
        queryString += " SELECT * FROM contabilidad_empresa_cierre";
        queryString += " WHERE Estatus = 'ABIERTO'";
        queryString += " AND IdEmpresa = " + empresaId;

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                mesCbx.addItem(rsRecords.getString("Mes"));
                mesCbx.setItemCaption(rsRecords.getString("Mes"), NOMBREMES.get(rsRecords.getString("Mes").substring(4, 6)) + " " + rsRecords.getString("Mes").substring(0, 4));

                mesCbx.select(rsRecords.getString("Mes"));
            }
        } catch (Exception ex1) {
            System.out.println("Error al combo de mes a cerrar: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void fillGridBalanceSaldos() {

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

        String queryString = " SELECT * FROM contabilidad_nomenclatura_empresa";
        queryString += " WHERE Estatus = 'HABILITADA'";
        queryString += " AND IdEmpresa = " + empresaId;
        queryString += " ORDER BY Cast(NoCuenta AS UNSIGNED)";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado

                BigDecimal debe;
                BigDecimal haber;
                BigDecimal saldoAnterior;
                BigDecimal saldoFinal;

                do {

                    debe = new BigDecimal(getDebeHaberCuenta(rsRecords.getString("IdNomenclatura"), "DebeQuetzales")).setScale(2, BigDecimal.ROUND_HALF_UP);
                    haber = new BigDecimal(getDebeHaberCuenta(rsRecords.getString("IdNomenclatura"), "HaberQuetzales")).setScale(2, BigDecimal.ROUND_HALF_UP);

                    saldoAnterior = new BigDecimal(getSaldoCuentaMesAnterior(rsRecords.getString("IdNomenclatura")).doubleValue()).setScale(2, BigDecimal.ROUND_HALF_UP);
                    saldoFinal = new BigDecimal(saldoAnterior.doubleValue()).setScale(2, BigDecimal.ROUND_HALF_UP);

                    if (rsRecords.getString("NoCuenta").startsWith("1")
                            || rsRecords.getString("NoCuenta").startsWith("5")
                            || rsRecords.getString("NoCuenta").startsWith("6")) {
                        saldoFinal = saldoFinal.add(new BigDecimal(debe.doubleValue() - haber.doubleValue())).setScale(2, BigDecimal.ROUND_HALF_UP);
                    } else {
                        saldoFinal = saldoFinal.subtract(new BigDecimal(debe.doubleValue() - haber.doubleValue())).setScale(2, BigDecimal.ROUND_HALF_UP);
                    }

//System.out.println(queryString);
                    if (omitirSinMovimientoChk.getValue()) {
                        if (debe.doubleValue() == 0.00 && haber.doubleValue() == 0.00) {
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

                    totalSaldoAnterior = totalSaldoAnterior.add(saldoAnterior).setScale(2, BigDecimal.ROUND_HALF_UP);
                    totalSaldoFinal = totalSaldoFinal.add(saldoFinal).setScale(2, BigDecimal.ROUND_HALF_UP);
                    totalDebe = totalDebe.add(debe).setScale(2, BigDecimal.ROUND_HALF_UP);
                    totalHaber = totalHaber.add(haber).setScale(2, BigDecimal.ROUND_HALF_UP);

                } while (rsRecords.next());

                footerRow.getCell(SALDO_ANTERIOR_PROPERTY).setText(numberFormat.format(totalSaldoAnterior));
                footerRow.getCell(DEBE_PROPERTY).setText(numberFormat.format(totalDebe));
                footerRow.getCell(HABER_PROPERTY).setText(numberFormat.format(totalHaber));
                footerRow.getCell(SALDO_FINAL_PROPERTY).setText(numberFormat.format(totalSaldoFinal));

                cerrarBtn.setEnabled(true);

            }

        } catch (Exception ex1) {
            System.out.println("Error al listar empresas : " + ex1.getMessage());
            ex1.printStackTrace();
        }

    }

    public void cierreMensual() {
        String queryString = " UPDATE contabilidad_empresa_cierre SET ";
        queryString += " Estatus = 'CERRADO'";
        queryString += ",FechaYHoraCierre = current_timestamp";
        queryString += ",UsuarioCerro = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserId();
        queryString += " WHERE IdEmpresa = " + empresaId;
        queryString += " AND Mes = '" + mesCbx.getValue() + "'";

        waitLbl.setVisible(true);

        try {

            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery1.executeUpdate(queryString);

            queryString = " SELECT * FROM contabilidad_nomenclatura";
            queryString += " WHERE Estatus = 'HABILITADA'";
            queryString += " ORDER BY ID1";

            rsRecords1 = stQuery1.executeQuery(queryString);

            BigDecimal debe;
            BigDecimal haber;
            BigDecimal saldoAnterior;
            BigDecimal saldoFinal;

            while (rsRecords1.next()) {

                debe = new BigDecimal(getDebeHaberCuenta(rsRecords1.getString("IdNomenclatura"), "DebeQuetzales")).setScale(2, BigDecimal.ROUND_HALF_UP);
                haber = new BigDecimal(getDebeHaberCuenta(rsRecords1.getString("IdNomenclatura"), "HaberQuetzales")).setScale(2, BigDecimal.ROUND_HALF_UP);

                saldoAnterior = new BigDecimal(getSaldoCuentaMesAnterior(rsRecords1.getString("IdNomenclatura")).doubleValue()).setScale(2, BigDecimal.ROUND_HALF_UP);
                saldoFinal = new BigDecimal(saldoAnterior.doubleValue()).setScale(2, BigDecimal.ROUND_HALF_UP);

                if (rsRecords1.getString("NoCuenta").startsWith("1")
                        || rsRecords1.getString("NoCuenta").startsWith("5")
                        || rsRecords1.getString("NoCuenta").startsWith("6")) {
                    saldoFinal = saldoFinal.add(new BigDecimal(debe.doubleValue() - haber.doubleValue())).setScale(2, BigDecimal.ROUND_HALF_UP);
                } else {
                    saldoFinal = saldoFinal.subtract(new BigDecimal(debe.doubleValue() - haber.doubleValue())).setScale(2, BigDecimal.ROUND_HALF_UP);
                }

                queryString = " Insert Into contabilidad_balance_saldo (IdEmpresa, IdNomenclatura, AnioMesCierre,";
                queryString += " SaldoAnterior, Debe, Haber, SaldoFinal ) Values (";
                queryString += " " + empresaId;
                queryString += "," + rsRecords1.getString("IdNomenclatura");
                queryString += ",'" + String.valueOf(mesCbx.getValue()) + "'";
                queryString += "," + String.valueOf(saldoAnterior.doubleValue());
                queryString += "," + String.valueOf(debe.doubleValue());
                queryString += "," + String.valueOf(haber.doubleValue());
                queryString += "," + String.valueOf(saldoFinal.doubleValue());
                queryString += ")";

                stQuery2.executeUpdate(queryString);

            }

            if (!String.valueOf(mesCbx.getValue()).endsWith("12")) {

                String proxAnioMesCierre = String.valueOf(mesCbx.getValue()).substring(0, 4) + numberFormat2.format((Integer.valueOf(String.valueOf(mesCbx.getValue()).substring(4, 6)) + 1));

                queryString = " SELECT * from contabilidad_empresa_cierre";
                queryString += " where Estatus = 'ABIERTO'";
                queryString += " And IdEmpresa = " + empresaId;
                queryString += " And Mes = '" + proxAnioMesCierre + "'";
//System.out.println("cierreMensual crea proximo mes de cierre : " + queryString);
                try {
                    stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                    rsRecords = stQuery.executeQuery(queryString);

                    if (!rsRecords.next()) {
                        queryString = "INSERT INTO contabilidad_empresa_cierre (IdEmpresa, Mes, Estatus)";
                        queryString += " VALUES (";
                        queryString += empresaId;
                        queryString += ",'" + proxAnioMesCierre + "'";
                        queryString += ",'ABIERTO'";
                        queryString += ")";

//System.out.println(queryString);
                        stQuery2.executeUpdate(queryString);
                    }
                } catch (Exception ex1) {
                    System.out.println("Error al combo de mes a cerrar: " + ex1.getMessage());
                    Notification.show("Error al incrementar mes de cierre , siguiente mes = " + proxAnioMesCierre, Notification.Type.ERROR_MESSAGE);
                    ex1.printStackTrace();
                }
            }

            Notification.show("Cierre mensual efectuado exitosamente!", Notification.Type.WARNING_MESSAGE);

        } catch (Exception ex1) {
            waitLbl.setVisible(false);
            System.out.println("Error al insertar transacción  : " + ex1.getMessage());
            ex1.printStackTrace();

            Notification notif = new Notification("HA OCURRIDO UN ERROR DE BASE DE DATOS : " + ex1.getMessage() + "  TRANSACCION ABORTADA!!!",
                    Notification.Type.ERROR_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.WARNING);
            notif.show(Page.getCurrent());

            try {
                String emailsTo[] = {"alerta@simpletecno.com"};
                MyEmailMessanger eMail = new MyEmailMessanger();

                eMail.postMail(emailsTo, "Error en SOPDI", "Error en base de datos :  " + this.getClass().getName() + " -->" + ex1.getMessage());
            } catch (MessagingException ex2) {
                Logger.getLogger(CierreMensualView.class.getName()).log(Level.SEVERE, null, ex2);
            }
        }
    }

    public double getDebeHaberCuenta(String idNomenclatura, String rubro) throws SQLException {
        double saldo = 0.00;

        String queryString = " SELECT IfNull(SUM(" + rubro + "), 0.00) As Saldo";
        queryString += " FROM contabilidad_partida";
        queryString += " WHERE IdNomenclatura = " + idNomenclatura;
        queryString += " AND   IdEmpresa = " + empresaId;
        queryString += " AND   Extract(YEAR_MONTH From contabilidad_partida.Fecha) = " + String.valueOf(mesCbx.getValue());
        queryString += " AND   Estatus <> 'ANULADO'";
//        queryString += " And   TipoDocumento NOT IN ('PARTIDA CIERRE', 'PARTIDA APERTURA', 'PARTIDA INICIAL')";
        queryString += " AND   TipoDocumento <> 'PARTIDA CIERRE'";

        stQuery2 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
        rsRecords2 = stQuery2.executeQuery(queryString);

        if (rsRecords2.next()) {
            saldo = rsRecords2.getBigDecimal("Saldo").setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        }

        return saldo;
    }

    public BigDecimal getSaldoCuentaMesAnterior(String idNomenclatura) throws SQLException {
        BigDecimal saldo = new BigDecimal(0.00);
        String mesCierre = String.valueOf(mesCbx.getValue());

        if (Integer.valueOf(mesCierre.substring(4, 6)) == 1) {
            return saldo;
        }

        Calendar c = Calendar.getInstance();
        c.set(Integer.valueOf(mesCierre.substring(0, 4)), Integer.valueOf(mesCierre.substring(4, 6)) - 1, 1);
        c.add(Calendar.MONTH, -1);

        String queryString = " SELECT IfNull(SaldoFinal, 0) SaldoMesAnterior ";
        queryString += " FROM contabilidad_balance_saldo";
        queryString += " WHERE IdNomenclatura = " + idNomenclatura;
        queryString += " AND  IdEmpresa = " + empresaId;
        queryString += " AND  AnioMesCierre = " + String.valueOf(c.get(Calendar.YEAR)) + String.format("%02d", c.get(Calendar.MONTH) + 1);

        stQuery2 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
        rsRecords2 = stQuery2.executeQuery(queryString);

        if (rsRecords2.next()) { //  encontrado        
            saldo = rsRecords2.getBigDecimal("SaldoMesAnterior").setScale(2, BigDecimal.ROUND_HALF_UP);
        }

        return saldo;
    }

    public boolean exportToExcel() {
        if (balanceSaldosGrid.getHeightByRows() > 0) {
            TableHolder tableHolder = new DefaultTableHolder(balanceSaldosGrid);
            ExcelExport excelExport = new ExcelExport(tableHolder);
            excelExport.excludeCollapsedColumns();
            excelExport.setDisplayTotals(false);
            String fileexport;
            fileexport = ((empresaId+ "_" + empresaNombre).replaceAll(" ", "_").replaceAll(",", "_").replaceAll("[()]", "").replaceAll("[.]", "").replaceAll("ñ", "n").replaceAll("Ñ", "N").replaceAll("ó", "o").replaceAll("é", "") + "_" + mesCbx.getValue() + ".xls").replaceAll(" ", "").replaceAll(",", "");
            excelExport.setExportFileName(fileexport);
            excelExport.export();
        }
        return true;
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - Cierre mensual");
    }
}
