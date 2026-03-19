/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.contabilidad;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.FooterRow;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Calendar;

import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.ui.NumberField;

/**
 *
 * @author user
 */
public class CierrePeriodoView extends VerticalLayout implements View {

    UI mainUI;
    Statement stQuery;
    ResultSet rsRecords;

    ComboBox anioCbx;
    ComboBox cuentaContableCbx;

    Button cierreBtn;

    IndexedContainer balanceSaldosContainer;
    Grid balanceSaldosGrid;
    FooterRow footerRow;

    NumberField diferenciaSaldos;

    static final String ID_PROPERTY = "Id";
    static final String CUENTA_PROPERTY = "Cuenta";
    static final String DESCRIPCION_PROPERTY = "Descripción";
    static final String SALDO_ANTERIOR_PROPERTY = "Saldo Anterior";
    static final String DEBE_PROPERTY = "Debe";
    static final String HABER_PROPERTY = "Haber";
    static final String SALDO_FINAL_PROPERTY = "Saldo Final";
    static final String TIPO_CUENTA_PROPERTY = "Tipo cuenta";

    static DecimalFormat numberFormat = new DecimalFormat("###,###,##0.00");

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public CierrePeriodoView() {
        this.mainUI = UI.getCurrent();
        setWidth("100%");
        setSpacing(true);
        setMargin(true);
        setHeightUndefined();

        Label titleLbl = new Label(empresaId + " " + empresaNombre + " CIERRE DEL PERIODO CONTABLE");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();

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
        layoutGridBalanceSaldos.setSpacing(true);
        layoutGridBalanceSaldos.setHeightUndefined();
        layoutGridBalanceSaldos.addStyleName("rcorners3");

        anioCbx = new ComboBox("Año:");
        anioCbx.setWidth("100px");
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
                llenarGridBalanceSaldos();
            }
        });

        HorizontalLayout filterLayout = new HorizontalLayout();
        filterLayout.setSpacing(true);
        filterLayout.setMargin(new MarginInfo(false, true, false, true));

        filterLayout.addComponent(anioCbx);
        filterLayout.setComponentAlignment(anioCbx, Alignment.MIDDLE_LEFT);

        layoutGridBalanceSaldos.addComponent(filterLayout);
        layoutGridBalanceSaldos.setComponentAlignment(filterLayout, Alignment.MIDDLE_CENTER);

        balanceSaldosContainer = new IndexedContainer();
        balanceSaldosContainer.addContainerProperty(ID_PROPERTY, String.class, null);
        balanceSaldosContainer.addContainerProperty(CUENTA_PROPERTY, String.class, null);
        balanceSaldosContainer.addContainerProperty(DESCRIPCION_PROPERTY, String.class, null);
        balanceSaldosContainer.addContainerProperty(SALDO_ANTERIOR_PROPERTY, String.class, null);
        balanceSaldosContainer.addContainerProperty(DEBE_PROPERTY, String.class, null);
        balanceSaldosContainer.addContainerProperty(HABER_PROPERTY, String.class, null);
        balanceSaldosContainer.addContainerProperty(SALDO_FINAL_PROPERTY, String.class, null);
        balanceSaldosContainer.addContainerProperty(TIPO_CUENTA_PROPERTY, String.class, null);

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
            } else {
                return null;
            }

        });

        balanceSaldosGrid.getColumn(CUENTA_PROPERTY).setExpandRatio(1);
        balanceSaldosGrid.getColumn(DESCRIPCION_PROPERTY).setExpandRatio(3);
        balanceSaldosGrid.getColumn(DEBE_PROPERTY).setExpandRatio(1);
        balanceSaldosGrid.getColumn(HABER_PROPERTY).setExpandRatio(1);

        footerRow = balanceSaldosGrid.appendFooterRow();
        footerRow.getCell(DESCRIPCION_PROPERTY).setText("SUMAS IGUALES");
        footerRow.getCell(DEBE_PROPERTY).setText("0.00");
        footerRow.getCell(HABER_PROPERTY).setText("0.00");

        footerRow.getCell(DESCRIPCION_PROPERTY).setStyleName("rightalign");
        footerRow.getCell(DEBE_PROPERTY).setStyleName("rightalign");
        footerRow.getCell(HABER_PROPERTY).setStyleName("rightalign");
        footerRow.getCell(SALDO_ANTERIOR_PROPERTY).setStyleName("rightalign");
        footerRow.getCell(SALDO_FINAL_PROPERTY).setStyleName("rightalign");

        layoutGridBalanceSaldos.addComponent(balanceSaldosGrid);

        cuentaContableCbx = new ComboBox("Seleccione la cuenta de cierre");
        cuentaContableCbx.setWidth("30em");
        cuentaContableCbx.setDescription("La cuenta de perdidas o ganancias.");
        cuentaContableCbx.setFilteringMode(FilteringMode.CONTAINS);
        cuentaContableCbx.setInvalidAllowed(false);
        cuentaContableCbx.setNewItemsAllowed(false);
        llenarComboCuentaContable();

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
//        diferenciaSaldos.setReadOnly(true);

        cierreBtn = new Button("CERRAR PERIODO " + anioCbx.getValue());
        cierreBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        cierreBtn.setIcon(FontAwesome.LOCK);
        cierreBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {

                ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de hacer el cierre anual ?",
                        "SI", "NO", new ConfirmDialog.Listener() {

                            public void onClose(ConfirmDialog dialog) {
                                if (dialog.isConfirmed()) {
                                    insertarPartidaCierre();
                                }
                            }
                        }
                );
           }
        });

        HorizontalLayout layoutButtons = new HorizontalLayout();
        layoutButtons.setMargin(true);
        layoutButtons.setSpacing(true);

        layoutButtons.addComponents(cuentaContableCbx, diferenciaSaldos, cierreBtn);
        layoutButtons.addComponents(diferenciaSaldos, cierreBtn);
        layoutButtons.setComponentAlignment(cuentaContableCbx, Alignment.MIDDLE_CENTER);
        layoutButtons.setComponentAlignment(diferenciaSaldos, Alignment.MIDDLE_CENTER);
        layoutButtons.setComponentAlignment(cierreBtn, Alignment.MIDDLE_CENTER);

        layoutGridBalanceSaldos.addComponent(layoutButtons);

        addComponent(layoutGridBalanceSaldos);

    }

    public void llenarGridBalanceSaldos() {

        if (balanceSaldosContainer == null) {
            return;
        }

        cierreBtn.setCaption("CERRAR PERIODO " + anioCbx.getValue());

        balanceSaldosContainer.removeAllItems();
        balanceSaldosGrid.getContainerDataSource().removeAllItems();
        balanceSaldosContainer.removeAllContainerFilters();

        // verificar si el año ya esta cerrado
        String queryString = " SELECT * FROM contabilidad_partida";
        queryString += " WHERE TipoDocumento = 'PARTIDA CIERRE'";
        queryString += " AND IdEmpresa = " + empresaId;
        queryString += " AND EXTRACT(YEAR From Fecha) = " + anioCbx.getValue();

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                Notification.show("EL PERIODO " + anioCbx.getValue() + " YA ESTÁ CERRADO...", Notification.Type.ERROR_MESSAGE);
                return;
            }

            BigDecimal totalDebe = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
            BigDecimal totalHaber = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);
            BigDecimal diferenciaDeSaldos = new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP);

            queryString = " SELECT contabilidad_balance_saldo.*, contabilidad_nomenclatura_empresa.NoCuenta, ";
            queryString += " contabilidad_nomenclatura_empresa.N5,contabilidad_nomenclatura_empresa.Reporte  ";
            queryString += " FROM contabilidad_balance_saldo";
            queryString += " INNER JOIN contabilidad_nomenclatura_empresa ON contabilidad_nomenclatura_empresa.IdNomenclatura = contabilidad_balance_saldo.IdNomenclatura";
            queryString += " WHERE contabilidad_balance_saldo.IdEmpresa = " + empresaId;
            queryString += " AND contabilidad_balance_saldo.AnioMesCierre = '" + anioCbx.getValue() + "12'";
            queryString += " AND contabilidad_nomenclatura_empresa.IdEmpresa = " + empresaId;

            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (!rsRecords.next()) {
                Notification.show("DEBE CERRAR DICIEMBRE " + anioCbx.getValue() + " PARA CONTINUAR CON EL CIERRE DEL PERIODO...", Notification.Type.ERROR_MESSAGE);
                return;
            }

            Object itemId;

            itemId = balanceSaldosContainer.addItem();
            balanceSaldosContainer.getContainerProperty(itemId, ID_PROPERTY).setValue("-----");
            balanceSaldosContainer.getContainerProperty(itemId, CUENTA_PROPERTY).setValue("-----");
            balanceSaldosContainer.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue("***BALANCE SALDOS DICIEMBRE***");
            balanceSaldosContainer.getContainerProperty(itemId, SALDO_ANTERIOR_PROPERTY).setValue("-----");
            balanceSaldosContainer.getContainerProperty(itemId, DEBE_PROPERTY).setValue("-----");
            balanceSaldosContainer.getContainerProperty(itemId, HABER_PROPERTY).setValue("-----");
            balanceSaldosContainer.getContainerProperty(itemId, SALDO_FINAL_PROPERTY).setValue("-----");
            balanceSaldosContainer.getContainerProperty(itemId, TIPO_CUENTA_PROPERTY).setValue("-----");

            String cuenta;
            do {

                cuenta  = rsRecords.getString("NoCuenta");

                itemId = balanceSaldosContainer.addItem();
                balanceSaldosContainer.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords.getString("IdNomenclatura"));
                balanceSaldosContainer.getContainerProperty(itemId, CUENTA_PROPERTY).setValue(cuenta);
                balanceSaldosContainer.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(rsRecords.getString("N5"));

                balanceSaldosContainer.getContainerProperty(itemId, SALDO_ANTERIOR_PROPERTY).setValue(numberFormat.format(Double.parseDouble(String.valueOf(rsRecords.getDouble("SaldoAnterior")))));
                balanceSaldosContainer.getContainerProperty(itemId, DEBE_PROPERTY).setValue(numberFormat.format(Double.parseDouble(String.valueOf(rsRecords.getDouble("Debe")))));
                balanceSaldosContainer.getContainerProperty(itemId, HABER_PROPERTY).setValue(numberFormat.format(Double.parseDouble(String.valueOf(rsRecords.getDouble("Haber")))));
                balanceSaldosContainer.getContainerProperty(itemId, SALDO_FINAL_PROPERTY).setValue(numberFormat.format(Double.parseDouble(String.valueOf(rsRecords.getDouble("SaldoFinal")))));
                balanceSaldosContainer.getContainerProperty(itemId, TIPO_CUENTA_PROPERTY).setValue(rsRecords.getString("Reporte"));

                totalDebe = totalDebe.add(new BigDecimal(rsRecords.getDouble("Debe")).setScale(2, BigDecimal.ROUND_HALF_UP));
                totalHaber = totalHaber.add(new BigDecimal(rsRecords.getDouble("Haber")).setScale(2, BigDecimal.ROUND_HALF_UP));

                if (cuenta.startsWith("1") || cuenta.startsWith("5") || cuenta.startsWith("6")) { // se carga en el debe
                    diferenciaDeSaldos = diferenciaDeSaldos.add(BigDecimal.valueOf((rsRecords.getDouble("SaldoFinal"))).setScale(2, BigDecimal.ROUND_HALF_UP));
                }
                else {
                    diferenciaDeSaldos = diferenciaDeSaldos.subtract(BigDecimal.valueOf((rsRecords.getDouble("SaldoFinal"))).setScale(2, BigDecimal.ROUND_HALF_UP));
                }

            } while (rsRecords.next());

            footerRow.getCell(DEBE_PROPERTY).setText(numberFormat.format(totalDebe));
            footerRow.getCell(HABER_PROPERTY).setText(numberFormat.format(totalHaber));
            diferenciaSaldos.setReadOnly(false);

            diferenciaSaldos.setValue(Double.valueOf(String.valueOf(diferenciaDeSaldos)));
//            diferenciaSaldos.setReadOnly(true);

        } catch (Exception ex1) {
            System.out.println("Error al listar balance de saldos de Diciembre : " + ex1.getMessage());
            ex1.printStackTrace();
        }

    }

    public void insertarPartidaCierre() {
        
        ///// fucion provisional cuando se encuentre todo en orden se validara correctamente el cierre
//         if (((SopdiUI) UI.getCurrent()).esMesCerrado(String.valueOf(empresaCbx.getValue()), Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()))) {
//            Notification.show("La fecha del documento no puede ser de un mes ya cerrado contablemente, revise!", Notification.Type.WARNING_MESSAGE);
//            fechaDt.focus();
//            return;
//        }

        if (anioCbx.getValue() == null) {
            Notification.show("Por favor seleccione el año que desea cerrar..", Notification.Type.WARNING_MESSAGE);
            anioCbx.focus();
            return;
        }

        if (cuentaContableCbx.getValue() == null) {
            Notification.show("Por favor elija una cuenta para el cierre..", Notification.Type.WARNING_MESSAGE);
            return;
        }
        
        int anio = Integer.valueOf(String.valueOf(anioCbx.getValue()));
       
        Calendar fechaCierre = Calendar.getInstance();
        fechaCierre.set(anio, 11, 31);

        String codigoPartidaCierre = empresaId + String.valueOf(anio) + "31129999"; //= 21202012CIERR  21202101APERT

        try {

            String queryString;

            for (Object itemId : balanceSaldosContainer.getItemIds()) {

                Item item = balanceSaldosContainer.getItem(itemId);

                Object idNomenclatura = item.getItemProperty(ID_PROPERTY).getValue();
                String cuenta = String.valueOf(item.getItemProperty(CUENTA_PROPERTY).getValue());
                String nombre = String.valueOf(item.getItemProperty(DESCRIPCION_PROPERTY).getValue());
                String saldo = String.valueOf(item.getItemProperty(SALDO_FINAL_PROPERTY).getValue()).replaceAll(",", "");

                if(cuenta.contains("----")) {
                    continue;
                }

                queryString = "INSERT INTO contabilidad_partida (IdEmpresa, Estatus, CodigoPartida, CodigoCC, ";
                queryString += " Fecha, Descripcion, TipoDocumento, IdNomenclatura, MonedaDocumento, Debe, Haber,";
                queryString += " DebeQuetzales, HaberQuetzales, CreadoUsuario, CreadoFechaYHora, ArchivoNombre) ";
                queryString += " VALUES ";
                queryString += "(";
                queryString += empresaId;
                queryString += ",'REVISADO'";
                queryString += ",'" + codigoPartidaCierre + "'";
                queryString += ",'" + codigoPartidaCierre + "'"; //codigocc
                queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaCierre.getTime()) + "'";
                queryString += ",'PARTIDA CIERRE'";
                queryString += ",'PARTIDA CIERRE'";
                queryString += "," + idNomenclatura;
                queryString += ",'QUETZALES'";
                if (cuenta.startsWith("1") || cuenta.startsWith("5") || cuenta.startsWith("6")) { // se carga en el debe, para cierre se hace lo contrario
                    queryString += ",0.00";
                    queryString += "," + saldo;
                    queryString += ",0.00";
                    queryString += "," + saldo;
                }
                else { // cuentas del pasivo, se cargan en el haber,  para el cierre se hace lo contrario
                    queryString += "," + saldo;
                    queryString += ",0.00";
                    queryString += "," + saldo;
                    queryString += ",0.00";
                }
                queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                queryString += ",current_timestamp";
                queryString += ", '" + cuenta + " " + nombre + "'";
                queryString += ")";

                stQuery.executeUpdate(queryString);
            } // END FOR

            queryString = "INSERT INTO contabilidad_partida (IdEmpresa, Estatus, CodigoPartida,  CodigoCC,";
            queryString += " Fecha, Descripcion, TipoDocumento, IdNomenclatura, MonedaDocumento, Debe, Haber,";
            queryString += " DebeQuetzales, HaberQuetzales, CreadoUsuario, CreadoFechaYHora, ArchivoNombre) ";
            queryString += " VALUES ";
            queryString += "(";
            queryString += empresaId;
            queryString += ",'REVISADO'";
            queryString += ",'" + codigoPartidaCierre + "'";
            queryString += ",'" + codigoPartidaCierre + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaCierre.getTime()) + "'";
            queryString += ",'RESULTADO DEL EJERCISIO'";
            queryString += ",'PARTIDA CIERRE'";
            queryString += "," + cuentaContableCbx.getValue();
            queryString += ",'QUETZALES'";
            if (diferenciaSaldos.getDoubleValueDoNotThrow() > 0.00) {
                queryString += ",0.00";
                queryString += "," + diferenciaSaldos.getDoubleValueDoNotThrow();
                queryString += ",0.00";
                queryString += "," + diferenciaSaldos.getDoubleValueDoNotThrow();
            } else {
                //si la diferencia es negativa, entonces multiplicar por -1 para volverlo positivo
                if(diferenciaSaldos.getDoubleValueDoNotThrow() < 0.00) {
                    diferenciaSaldos.setValue(diferenciaSaldos.getDoubleValueDoNotThrow() * -1);
                }
                queryString += "," + diferenciaSaldos.getDoubleValueDoNotThrow();
                queryString += ",0.00";
                queryString += "," + diferenciaSaldos.getDoubleValueDoNotThrow();
                queryString += ",0.00";
            }
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ", 'RESULTADO DEL EJERCISIO'";
            queryString += ")";

            stQuery.executeUpdate(queryString);

            fechaCierre.set(anio+1, 0, 1);

            String codigoPartidaApertura = empresaId + String.valueOf(anio + 1) + "01010000"; //= 21202012CIERR  21202101APERT

            double saldoDebe = 0.00;
            double saldoHaber = 0.00;

            for (Object itemId : balanceSaldosContainer.getItemIds()) {

                Item item = balanceSaldosContainer.getItem(itemId);

                Object idNomenclatura = item.getItemProperty(ID_PROPERTY).getValue();
                String cuenta = String.valueOf(item.getItemProperty(CUENTA_PROPERTY).getValue());
                String nombre = String.valueOf(item.getItemProperty(DESCRIPCION_PROPERTY).getValue());
                String saldo = String.valueOf(item.getItemProperty(SALDO_FINAL_PROPERTY).getValue()).replaceAll(",", "");
                String tipo = String.valueOf(item.getItemProperty(TIPO_CUENTA_PROPERTY).getValue());

                if(cuenta.contains("----")) {
                    continue;
                }

                if(!tipo.equals("Balance General")) {
                    continue;
                }

                queryString = "INSERT INTO contabilidad_partida (IdEmpresa, Estatus, CodigoPartida, CodigoCC,";
                queryString += " Fecha, Descripcion, TipoDocumento, IdNomenclatura, MonedaDocumento, Debe, Haber,";
                queryString += " DebeQuetzales, HaberQuetzales, CreadoUsuario, CreadoFechaYHora, Archivo) ";
                queryString += " VALUES ";
                queryString += "(";
                queryString += empresaId;
                queryString += ",'REVISADO'";
                queryString += ",'" + codigoPartidaApertura + "'";
                queryString += ",'" + codigoPartidaCierre + "'";
                queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaCierre.getTime()) + "'";
                queryString += ",'PARTIDA APERTURA'";
                queryString += ",'PARTIDA APERTURA'";
                queryString += "," + idNomenclatura;
                queryString += ",'QUETZALES'";
                if (cuenta.startsWith("1") || cuenta.startsWith("5") || cuenta.startsWith("6")) { // se carga en el debe, para apertura se carga igual
                    queryString += "," + saldo;
                    queryString += ",0.00";
                    queryString += "," + saldo;
                    queryString += ",0.00";
                    saldoDebe+= Double.valueOf(saldo);
                }
                else { // cuentas del pasivo, se cargan en el haber,  para el apertura se carga igual
                    queryString += ",0.00";
                    queryString += "," + saldo;
                    queryString += ",0.00";
                    queryString += "," + saldo;
                    saldoHaber+= Double.valueOf(saldo);
                }
                queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                queryString += ",current_timestamp";
                queryString += ", '" + cuenta + " " + nombre + "'";
                queryString += ")";

                stQuery.executeUpdate(queryString);
            } // end for

            queryString = "INSERT INTO contabilidad_partida (IdEmpresa, Estatus, CodigoPartida, CodigoCc,";
            queryString += " Fecha, Descripcion, TipoDocumento, IdNomenclatura, MonedaDocumento, Debe, Haber,";
            queryString += " DebeQuetzales, HaberQuetzales, CreadoUsuario, CreadoFechaYHora, Referencia) ";
            queryString += " VALUES ";
            queryString += "(";
            queryString += empresaId;
            queryString += ",'REVISADO'";
            queryString += ",'" + codigoPartidaApertura + "'";
            queryString += ",'" + codigoPartidaCierre + "'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaCierre.getTime()) + "'";
            queryString += ",'PARTIDA APERTURA'";
            queryString += ",'PARTIDA APERTURA'";
//            queryString += "," + cuentaContableCbx.getValue();
            queryString += ", " +  ((SopdiUI) mainUI).cuentasContablesDefault.getPerdidasGananciasEjercicioAnterior();
            queryString += ",'QUETZALES'";

            if (diferenciaSaldos.getDoubleValueDoNotThrow() > 0.00) {
                queryString += ",0.00";
                queryString += "," + diferenciaSaldos.getDoubleValueDoNotThrow();
                queryString += ",0.00";
                queryString += "," + diferenciaSaldos.getDoubleValueDoNotThrow();
            } else {
                //si la diferencia es negativa, entonces multiplicar por -1 para volverlo positivo
                if(diferenciaSaldos.getDoubleValueDoNotThrow() < 0.00) {
                    diferenciaSaldos.setValue(diferenciaSaldos.getDoubleValueDoNotThrow() * -1);
                }
                queryString += "," + diferenciaSaldos.getDoubleValueDoNotThrow();
                queryString += ",0.00";
                queryString += "," + diferenciaSaldos.getDoubleValueDoNotThrow();
                queryString += ",0.00";
            }
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ", 'RESULTADO DEL EJERCISIO'";
            queryString += ")";

            stQuery.executeUpdate(queryString);

            queryString = "INSERT INTO contabilidad_empresa_cierre (IdEmpresa, Mes, Estatus)";
            queryString += " Values (";
            queryString += empresaId;
            queryString += ",'" + (anio + 1) + "01'";
            queryString += ",'ABIERTO'";
            queryString += ")";

            stQuery.executeUpdate(queryString);

            Notification.show("PARTIDAS DE CIERRE Y APERTURA agregadas con exito", Notification.Type.HUMANIZED_MESSAGE);

        } catch (Exception ex) {
            System.out.println("Error al insertar en CONTABILIDAD PARTIDA CIERRE, DIFERENCIA E INICIAL" + ex);

        }
    }

    public void llenarComboCuentaContable() {

        if(((SopdiUI) mainUI).cuentasContablesDefault.getPerdidasGananciasEjercicioAnterior()==null) {
            Notification.show("No se ha definido la cuenta contable para pérdidas y ganancias del ejercicio anterior en las cuentas contables por defecto, por favor verifique...", Notification.Type.ERROR_MESSAGE);
        }
        else {

            String queryString = "";
            queryString += " SELECT * FROM contabilidad_nomenclatura_empresa";
            queryString += " WHERE Estatus = 'HABILITADA'";
            queryString += " AND IdNomenclatura = " + ((SopdiUI)  UI.getCurrent()).cuentasContablesDefault.getPerdidasGananciasEjercicioAnterior();
            queryString += " AND IdEmpresa = " + empresaId;

            try {
                stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                rsRecords = stQuery.executeQuery(queryString);

                while (rsRecords.next()) { //  encontrado

                    cuentaContableCbx.addItem(rsRecords.getString("IdNomenclatura"));
                    cuentaContableCbx.setItemCaption(rsRecords.getString("IdNomenclatura"), rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5"));

                }
                cuentaContableCbx.select(((SopdiUI)  UI.getCurrent()).cuentasContablesDefault.getPerdidasGananciasEjercicioAnterior());
            } catch (Exception ex1) {
                System.out.println("Error al combo cuentas contables: " + ex1.getMessage());
                ex1.printStackTrace();
            }
        }
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - Cierre del PERIODO");
    }

}
