/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.tesoreria;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.SelectionEvent;
import com.vaadin.event.SelectionEvent.SelectionListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.HeaderCell;
import com.vaadin.ui.Grid.HeaderRow;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.ui.NumberField;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Administrador
 */
public class AutorizarPagoLiquidacionForm extends Window {

    VerticalLayout mainLayout;

    static final String ID_PROPERTY = "Id";
    static final String CODIGO_CC_PROPERTY = "CODIGOCC";
    static final String ID_LIQUIDADOR_PROPERTY = "Id Liquidador";
    static final String LIQUIDADOR_PROPERTY = "Liquidador";
    static final String FECHA_PROPERTY = "Fecha";
    static final String ID_LIQUIDACION_PROPERTY = "Liquidación";
    static final String MONTO_QUETZALES_PROPERTY = "Monto Q.";
    static final String VALORSF_PROPERTY = "MSF";
    static final String MONTOQSF_PROPERTY = "MQSF";

    IndexedContainer liquidacionContainer = new IndexedContainer();
    Grid liquidacionGrid;
    Grid.FooterRow liquidacionFooter;

    UI mainUI;
    Statement stQuery, stQuery1;
    ResultSet rsRecords, rsRecords1;

    String queryString;

    NumberField montoLiquidacionSeleccionadaTxt;
    NumberField montoPendienteChequeTxt;

    Button salirBtn;
    Button autorizarBtn;

    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public AutorizarPagoLiquidacionForm() {
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

        Label titleLbl = new Label(empresaId + " " + empresaNombre + " " + AutorizacionesPagoView.PAGO_LIQUIDACION);
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h2_custom");

        layoutTitle.addComponent(titleLbl);
        layoutTitle.setComponentAlignment(titleLbl, Alignment.BOTTOM_RIGHT);

        mainLayout.addComponent(layoutTitle);

        setContent(mainLayout);

        crearGridLiquidaciones();
        llenarGridLiquidaciones();
    }

    public void crearGridLiquidaciones() {

        HorizontalLayout reportLayout = new HorizontalLayout();
        reportLayout.setWidth("100%");
        reportLayout.addStyleName("rcorners3");
        reportLayout.setResponsive(true);
        reportLayout.setMargin(new MarginInfo(false, true, false, true));

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setMargin(new MarginInfo(false, true, false, true));
        buttonsLayout.setSpacing(false);
        buttonsLayout.setWidth("100%");

        HorizontalLayout leftLayout = new HorizontalLayout();
        leftLayout.setMargin(new MarginInfo(false, false, false, false));
        leftLayout.setSpacing(true);

        HorizontalLayout rightLayout = new HorizontalLayout();
        rightLayout.setMargin(new MarginInfo(false, false, false, false));
        rightLayout.setSpacing(true);

        liquidacionContainer.addContainerProperty(ID_PROPERTY, String.class, null);
        liquidacionContainer.addContainerProperty(CODIGO_CC_PROPERTY, String.class, null);
        liquidacionContainer.addContainerProperty(ID_LIQUIDADOR_PROPERTY, String.class, null);
        liquidacionContainer.addContainerProperty(LIQUIDADOR_PROPERTY, String.class, null);
        liquidacionContainer.addContainerProperty(ID_LIQUIDACION_PROPERTY, String.class, null);
        liquidacionContainer.addContainerProperty(MONTO_QUETZALES_PROPERTY, String.class, null);
        liquidacionContainer.addContainerProperty(VALORSF_PROPERTY, String.class, null);
        liquidacionContainer.addContainerProperty(MONTOQSF_PROPERTY, String.class, null);

        liquidacionGrid = new Grid("Liquidaciones pendientes de autorizar/pagar", liquidacionContainer);
        liquidacionGrid.setImmediate(true);
        liquidacionGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        liquidacionGrid.setDescription("Seleccione un registro para autorizar.");
        liquidacionGrid.setHeightMode(HeightMode.ROW);
        liquidacionGrid.setHeightByRows(7);
        liquidacionGrid.setWidth("100%");
        liquidacionGrid.setResponsive(true);
        liquidacionGrid.setEditorBuffered(false);
        liquidacionGrid.setSizeFull();
        liquidacionGrid.getColumn(ID_PROPERTY).setHidable(true).setHidden(true);
        liquidacionGrid.getColumn(ID_LIQUIDADOR_PROPERTY).setHidable(true).setHidden(true);
        liquidacionGrid.getColumn(VALORSF_PROPERTY).setHidable(true).setHidden(true);
        liquidacionGrid.getColumn(MONTOQSF_PROPERTY).setHidable(true).setHidden(true);
        liquidacionGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if (MONTO_QUETZALES_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }
        });
        liquidacionGrid.addSelectionListener(new SelectionListener() {
            @Override
            public void select(SelectionEvent event) {
                if (liquidacionGrid.getSelectedRow() != null) {
                    montoLiquidacionSeleccionadaTxt.setReadOnly(false);
                    montoPendienteChequeTxt.setReadOnly(false);
                    montoLiquidacionSeleccionadaTxt.setValue(String.valueOf(liquidacionContainer.getContainerProperty(liquidacionGrid.getSelectedRow(), MONTO_QUETZALES_PROPERTY).getValue()).replaceAll(",", ""));
                    montoPendienteChequeTxt.setValue(String.valueOf(liquidacionContainer.getContainerProperty(liquidacionGrid.getSelectedRow(), MONTO_QUETZALES_PROPERTY).getValue()).replaceAll(",", ""));
                    montoPendienteChequeTxt.setReadOnly(true);
                    montoLiquidacionSeleccionadaTxt.setReadOnly(true);
                }
            }
        });

        HeaderRow filterRow = liquidacionGrid.appendHeaderRow();

        HeaderCell cell = filterRow.getCell(ID_LIQUIDACION_PROPERTY);

        TextField filterField = new TextField();
        filterField.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField.setInputPrompt("Filtrar");
        filterField.setColumns(5);

        filterField.addTextChangeListener(change -> {
            liquidacionContainer.removeContainerFilters(ID_LIQUIDACION_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                liquidacionContainer.addContainerFilter(
                        new SimpleStringFilter(ID_LIQUIDACION_PROPERTY,
                                change.getText(), true, false));
            }
            setTotal();

        });
        cell.setComponent(filterField);

        HeaderCell cell2 = filterRow.getCell(LIQUIDADOR_PROPERTY);

        TextField filterField2 = new TextField();
        filterField2.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField2.setInputPrompt("Filtrar");
        filterField2.setColumns(20);

        filterField2.addTextChangeListener(change -> {
            liquidacionContainer.removeContainerFilters(LIQUIDADOR_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                liquidacionContainer.addContainerFilter(
                        new SimpleStringFilter(LIQUIDADOR_PROPERTY,
                                change.getText(), true, false));
            }
            setTotal();

        });
        cell2.setComponent(filterField2);

        liquidacionFooter = liquidacionGrid.appendFooterRow();
        liquidacionFooter.getCell(LIQUIDADOR_PROPERTY).setText("TOTALES");
        liquidacionFooter.getCell(MONTO_QUETZALES_PROPERTY).setText("0.00");

        liquidacionFooter.getCell(LIQUIDADOR_PROPERTY).setStyleName("leftalign");
        liquidacionFooter.getCell(MONTO_QUETZALES_PROPERTY).setStyleName("rightalign");

        montoLiquidacionSeleccionadaTxt = new NumberField("Monto de la liquidación:");
        montoLiquidacionSeleccionadaTxt.setDecimalAllowed(true);
        montoLiquidacionSeleccionadaTxt.setDecimalPrecision(2);
        montoLiquidacionSeleccionadaTxt.setMinimumFractionDigits(2);
        montoLiquidacionSeleccionadaTxt.setDecimalSeparator('.');
        montoLiquidacionSeleccionadaTxt.setDecimalSeparatorAlwaysShown(true);
        montoLiquidacionSeleccionadaTxt.setValue(0d);
        montoLiquidacionSeleccionadaTxt.setGroupingUsed(true);
        montoLiquidacionSeleccionadaTxt.setGroupingSeparator(',');
        montoLiquidacionSeleccionadaTxt.setGroupingSize(3);
        montoLiquidacionSeleccionadaTxt.setImmediate(true);
        montoLiquidacionSeleccionadaTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        montoLiquidacionSeleccionadaTxt.setWidth("8em");

        montoPendienteChequeTxt = new NumberField("Monto para cheque:");
        montoPendienteChequeTxt.setDecimalAllowed(true);
        montoPendienteChequeTxt.setDecimalPrecision(2);
        montoPendienteChequeTxt.setMinimumFractionDigits(2);
        montoPendienteChequeTxt.setDecimalSeparator('.');
        montoPendienteChequeTxt.setDecimalSeparatorAlwaysShown(true);
        montoPendienteChequeTxt.setValue(0d);
        montoPendienteChequeTxt.setGroupingUsed(true);
        montoPendienteChequeTxt.setGroupingSeparator(',');
        montoPendienteChequeTxt.setGroupingSize(3);
        montoPendienteChequeTxt.setImmediate(true);
        montoPendienteChequeTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        montoPendienteChequeTxt.setWidth("8em");

        autorizarBtn = new Button("Autorizar");
        autorizarBtn.setIcon(FontAwesome.SAVE);
        autorizarBtn.setWidth("60%");
        autorizarBtn.setHeight("80%");
        autorizarBtn.setStyleName(ValoTheme.BUTTON_PRIMARY);
        autorizarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                ActualizarMonto();
            }
        });

        salirBtn = new Button("Salir");
        salirBtn.setWidth("7em");
        salirBtn.setStyleName(ValoTheme.BUTTON_DANGER);
        salirBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();
            }
        });

        reportLayout.addComponent(liquidacionGrid);
        reportLayout.setComponentAlignment(liquidacionGrid, Alignment.TOP_CENTER);

        leftLayout.addComponent(salirBtn);
        leftLayout.setComponentAlignment(salirBtn, Alignment.BOTTOM_LEFT);

        rightLayout.addComponents(montoLiquidacionSeleccionadaTxt, montoPendienteChequeTxt, autorizarBtn);
        rightLayout.setComponentAlignment(montoLiquidacionSeleccionadaTxt, Alignment.TOP_RIGHT);
        rightLayout.setComponentAlignment(montoPendienteChequeTxt, Alignment.TOP_RIGHT);
        rightLayout.setComponentAlignment(autorizarBtn, Alignment.BOTTOM_RIGHT);

        buttonsLayout.addComponents(leftLayout, rightLayout);
        buttonsLayout.setComponentAlignment(leftLayout, Alignment.BOTTOM_LEFT);
        buttonsLayout.setComponentAlignment(rightLayout, Alignment.TOP_RIGHT);

        mainLayout.addComponent(reportLayout);
        mainLayout.setComponentAlignment(reportLayout, Alignment.TOP_CENTER);

        mainLayout.addComponent(buttonsLayout);

    }

    public void llenarGridLiquidaciones() {
        try {

            liquidacionContainer.removeAllItems();
            liquidacionContainer.removeAllContainerFilters();

            queryString = " SELECT contabilidad_partida.IdLiquidacion, contabilidad_partida.CodigoCC, contabilidad_partida.IdNomenclatura, ";
            queryString += " contabilidad_partida.IdLiquidador, proveedor_empresa.Nombre as NLiquidador";
            queryString += " FROM contabilidad_partida";
            queryString += " INNER JOIN proveedor_empresa ON proveedor_empresa.IDProveedor = contabilidad_partida.IdLiquidador";
            queryString += " WHERE contabilidad_partida.IdEmpresa = " + empresaId;
            queryString += " AND contabilidad_partida.Fecha >= '2021-01-01' ";
            queryString += " AND contabilidad_partida.IdNomenclatura = " + ((SopdiUI) mainUI).cuentasContablesDefault.getLiquidacionesCajaChicha();
//            queryString += " AND contabilidad_partida.TipoDocumento in ('FACTURA', 'RECIBO CONTABLE', 'FORMULARIO', 'RECIBO CORRIENTE')";
            queryString += " AND contabilidad_partida.IdLiquidacion > 0 ";
            queryString += " AND contabilidad_partida.MontoAutorizadoPagar = 0";
            queryString += " And proveedor.IdProveedor = contabilidad_partida.IdLiquidador";
            queryString += " AND contabilidad_partida.Estatus = 'CERRADO'";
            queryString += " AND proveedor_empresa.IdEmpresa = " + empresaId;
            queryString += " GROUP BY contabilidad_partida.IdLiquidacion";
            queryString += " ORDER BY contabilidad_partida.IdLiquidacion";

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);
            stQuery1 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            if (rsRecords.next()) {

                do {

                    queryString = " SELECT SUM(HABER - DEBE) TOTALSALDO, ";
                    queryString += " SUM(HaberQuetzales - DebeQuetzales) TOTALSALDOQ ";
                    queryString += " FROM contabilidad_partida";
                    queryString += " WHERE IdEmpresa = " + empresaId;
                    queryString += " AND CodigoCC = '" + rsRecords.getString("CodigoCC") + "'";
                    queryString += " AND contabilidad_partida.IdNomenclatura = " + ((SopdiUI) mainUI).cuentasContablesDefault.getLiquidacionesCajaChicha();
//                    queryString += " AND contabilidad_partida.IdLiquidacion = " + rsRecords.getString("IdLiquidacion");
                    queryString += " AND contabilidad_partida.Estatus = 'CERRADO'";

                    rsRecords1 = stQuery1.executeQuery(queryString);

                    if (rsRecords1.next()) {

                        if (rsRecords1.getDouble("TOTALSALDO") > 0.00) {
                            Object itemId = liquidacionContainer.addItem();

                            liquidacionContainer.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords.getString("IdLiquidacion"));
                            liquidacionContainer.getContainerProperty(itemId, CODIGO_CC_PROPERTY).setValue(rsRecords.getString("CodigoCC"));
                            liquidacionContainer.getContainerProperty(itemId, ID_LIQUIDADOR_PROPERTY).setValue(rsRecords.getString("IdLiquidador"));
                            liquidacionContainer.getContainerProperty(itemId, LIQUIDADOR_PROPERTY).setValue(rsRecords.getString("NLiquidador"));
                            liquidacionContainer.getContainerProperty(itemId, ID_LIQUIDACION_PROPERTY).setValue(rsRecords.getString("IdLiquidacion"));
                            liquidacionContainer.getContainerProperty(itemId, MONTO_QUETZALES_PROPERTY).setValue(numberFormat.format(rsRecords1.getDouble("TOTALSALDO")));
                            liquidacionContainer.getContainerProperty(itemId, VALORSF_PROPERTY).setValue(rsRecords1.getString("TOTALSALDO"));
                            liquidacionContainer.getContainerProperty(itemId, MONTOQSF_PROPERTY).setValue(rsRecords1.getString("TOTALSALDO"));

                        }
                    }

                } while (rsRecords.next());
            }
            setTotal();

        } catch (SQLException ex) {
            System.out.println("Error en listar liquidaciones " + ex);
            Logger.getLogger(AutorizarPagoLiquidacionForm.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void setTotal() {

        BigDecimal total = new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal totalQ = new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP);

        for (Object rid : liquidacionGrid.getContainerDataSource().getItemIds()) {
            if (rid == null) {
                return;
            }
            if (liquidacionContainer.getContainerProperty(rid, VALORSF_PROPERTY).getValue() == null) {
                return;
            }
            total = total.add(new BigDecimal(
                    Double.parseDouble(
                            String.valueOf(liquidacionContainer.getContainerProperty(rid, VALORSF_PROPERTY).getValue())
                    )));
            totalQ = totalQ.add(new BigDecimal(
                    Double.parseDouble(
                            String.valueOf(liquidacionContainer.getContainerProperty(rid, MONTOQSF_PROPERTY).getValue())
                    )));
        }

        liquidacionFooter.getCell(MONTO_QUETZALES_PROPERTY).setText(numberFormat.format(totalQ));
    }

    private void ActualizarMonto() {
        try {
            if (liquidacionContainer.size() == 0) {
                Notification.show("No hay registros pendientes", Notification.Type.ERROR_MESSAGE);
                return;
            }
            if (liquidacionGrid.getSelectedRow() == null) {
                Notification.show("Por favor seleccione un registro", Notification.Type.ERROR_MESSAGE);
                return;
            }

            queryString = "UPDATE contabilidad_partida SET ";
            queryString += " MontoAutorizadoPagar = " + montoPendienteChequeTxt.getDoubleValueDoNotThrow();
            queryString += " WHERE IdLiquidacion = " + String.valueOf(liquidacionContainer.getContainerProperty(liquidacionGrid.getSelectedRow(), ID_LIQUIDACION_PROPERTY).getValue());
            queryString += " AND IdNomenclatura = " + ((SopdiUI) mainUI).cuentasContablesDefault.getLiquidacionesCajaChicha();;
            queryString += " And IdEmpresa = " + empresaId;

            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            queryString = "  INSERT INTO autorizacion_pago (TipoAutorizacion, IdEmpresa, IdProveedor, ";
            queryString += " Fecha, Moneda, Monto, CodigoCC, CodigoCCRelacionado, CuentaContableLiquidar, ";
            queryString += " Objetivo, CreadoUsuario, CreadoFechaYHora)";
            queryString += " VALUES ";
            queryString += "(";
            queryString += "'" + AutorizacionesPagoView.PAGO_LIQUIDACION + "'";
            queryString += "," + empresaId;
            queryString += "," + String.valueOf(liquidacionContainer.getContainerProperty(liquidacionGrid.getSelectedRow(), ID_LIQUIDADOR_PROPERTY).getValue());
            queryString += ",current_date";
            queryString += ",'QUETZALES'";
            queryString += "," + montoPendienteChequeTxt.getDoubleValueDoNotThrow();
            queryString += ",'" +  String.valueOf(liquidacionContainer.getContainerProperty(liquidacionGrid.getSelectedRow(), CODIGO_CC_PROPERTY).getValue()) + "'";
            queryString += ",''"; // codigoccrelacionado
            queryString += ",''"; // cuentacontableliquidar
            queryString += ",'" + AutorizacionesPagoView.PAGO_LIQUIDACION + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";

            stQuery.executeUpdate(queryString);

            Notification notif = new Notification("Autorización exitosa",
                    Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.CHECK);
            notif.show(Page.getCurrent());

            llenarGridLiquidaciones();

        } catch (Exception ex) {
            Notification.show("Error al momento de actualizar el registro del documento Monto a pagar.", Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
