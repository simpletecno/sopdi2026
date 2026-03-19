package com.simpletecno.sopdi.tesoreria;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
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
import com.vaadin.ui.Field;
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import org.vaadin.ui.NumberField;

/**
 *
 * @author user
 */
public class AutorizarPagoPlanillaForm extends Window {

    VerticalLayout mainLayout;

    static final String ID_PROPERTY = "Id";
    static final String CODIGO_PARTIDA_PROPERTY = "Codigo Partida";
    static final String FECHA_PROPERTY = "Fecha";
    static final String DOCUMENTO_PROPERTY = "Documento";
    static final String PLANILLA_PROPERTY = "Planilla";
    static final String CODIGO_PROPERTY = "Id Proveedor";
    static final String EMPLEADO_PROPERTY = "Empleado";
    static final String MONEDA_PROPERTY = "Moneda";
    static final String VALOR_PROPERTY = "Monto";
    static final String SALDO_PROPERTY = "Saldo";
    static final String AUTORIZADO_PROPERTY = "Autorizado";
    static final String ANTICIPO_PROPERTY = "Anticipo";
    static final String CODIGOCC_PROPERTY = "Codigo CC";

    static final String ID2_PROPERTY = "Proveedor";
    static final String CODIGOPARTIDA_PROPERTY = "Codigo partida";
    static final String FECHA2 = "Fecha";
    static final String DOCUMENTO1_PROPERTY = "Cheque/ Transf";
    static final String MONEDA2_PROPERTY = "Moneda";
    static final String DEBE = "Monto";
    static final String HABER = "Haber";
    static final String DEBE_QUETZALES = "Debe Q.";
    static final String HABER_QUETZALES = "Haber Q.";
    static final String SALDO2 = "Saldo";
    static final String UTILIZAR_PROPERTY = "Utilizar";

    IndexedContainer planillaContainer = new IndexedContainer();
    Grid planillaGrid;
    Grid.FooterRow planillaFooter;

    IndexedContainer anticiposContainer = new IndexedContainer();
    Grid anticiposGrid;
    Grid.FooterRow anticiposFooter;

    Button salirBtn;
    Button autorizarBtn;

    NumberField totalUtilizarAnticiposTxt;
    NumberField montoPendienteChequeTxt;

    double saldoPlanillaSeleccionada = 0.00;

    UI mainUI;
    Statement stQuery, stQuery1, stQueryPlanillas;
    ResultSet  rsRecords1, rsRecordsPlanillas;

    String queryString;
    String codigoPartidaPlanilla = "";

    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public AutorizarPagoPlanillaForm() {
        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setResponsive(true);
        mainLayout.setMargin(true);

        HorizontalLayout layoutTitle;
        layoutTitle = new HorizontalLayout();
        layoutTitle.setSpacing(true);
        layoutTitle.setMargin(true);
        layoutTitle.setWidth("100%");

        this.mainUI = UI.getCurrent();
        setResponsive(true);
        setWidth("90%");
        setHeight("90%");

        Label titleLbl = new Label(empresaId + " " + empresaNombre + " AUTORIZAR PAGO DE PLANILLA");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h2_custom");

        layoutTitle.addComponent(titleLbl);
        layoutTitle.setComponentAlignment(titleLbl, Alignment.BOTTOM_RIGHT);

        mainLayout.addComponent(layoutTitle);
        setContent(mainLayout);

        crearGridPlanilla();
        llenarGridPlanilla();

        crearGridAnticipos();

    }

    public void crearGridPlanilla() {

        HorizontalLayout documentosLayout = new HorizontalLayout();
        documentosLayout.setMargin(true);
        documentosLayout.setSpacing(true);
        documentosLayout.setResponsive(true);
        documentosLayout.setWidth("100%");
        documentosLayout.addStyleName("rcorners3");

        planillaContainer.addContainerProperty(ID_PROPERTY, String.class, null);
        planillaContainer.addContainerProperty(CODIGO_PARTIDA_PROPERTY, String.class, null);
        planillaContainer.addContainerProperty(FECHA_PROPERTY, String.class, null);
        planillaContainer.addContainerProperty(DOCUMENTO_PROPERTY, String.class, null);
        planillaContainer.addContainerProperty(PLANILLA_PROPERTY, String.class, null);
        planillaContainer.addContainerProperty(CODIGO_PROPERTY, String.class, null);
        planillaContainer.addContainerProperty(EMPLEADO_PROPERTY, String.class, null);
        planillaContainer.addContainerProperty(MONEDA_PROPERTY, String.class, null);
        planillaContainer.addContainerProperty(VALOR_PROPERTY, String.class, null);
        planillaContainer.addContainerProperty(SALDO_PROPERTY, String.class, null);
        planillaContainer.addContainerProperty(AUTORIZADO_PROPERTY, String.class, null);
        planillaContainer.addContainerProperty(ANTICIPO_PROPERTY, String.class, null);
        planillaContainer.addContainerProperty(CODIGOCC_PROPERTY, String.class, null);

        planillaGrid = new Grid("Listado de empleados", planillaContainer);

        planillaGrid.setWidth("100%");
        planillaGrid.setImmediate(true);
        planillaGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        planillaGrid.setDescription("Seleccione un registro.");
        planillaGrid.setHeightMode(HeightMode.ROW);
        planillaGrid.setHeightByRows(4);
        planillaGrid.setResponsive(true);
        planillaGrid.setEditorBuffered(false);
        planillaGrid.setResponsive(true);
        planillaGrid.setEditorBuffered(false);

        planillaGrid.getColumn(ID_PROPERTY).setHidable(true).setHidden(true);
        planillaGrid.getColumn(CODIGO_PARTIDA_PROPERTY).setHidable(true).setHidden(true);
        planillaGrid.getColumn(CODIGOCC_PROPERTY).setHidable(true).setHidden(true);
        planillaGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {

            if (VALOR_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (PLANILLA_PROPERTY.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else if (DOCUMENTO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else {
                return null;
            }

        });

        planillaGrid.addSelectionListener(new SelectionListener() {
            @Override
            public void select(SelectionEvent event) {

                if (planillaGrid.getSelectedRow() != null) {
                    String saldo = String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), SALDO_PROPERTY).getValue());
                    saldoPlanillaSeleccionada = Double.valueOf(saldo.replaceAll(",", "").replaceAll("Q.", "").replaceAll("\\$.", ""));
                    codigoPartidaPlanilla = String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), ID_PROPERTY).getValue());
                    montoPendienteChequeTxt.setValue(saldoPlanillaSeleccionada);

                    llenarTablaAnticipos();
                }
            }
        });

        HeaderRow filterRow = planillaGrid.appendHeaderRow();

        HeaderCell cell = filterRow.getCell(PLANILLA_PROPERTY);

        TextField filterField = new TextField();
        filterField.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField.setInputPrompt("Filtrar");
        filterField.setColumns(5);

        filterField.addTextChangeListener(change -> {
            planillaContainer.removeContainerFilters(PLANILLA_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                planillaContainer.addContainerFilter(
                        new SimpleStringFilter(PLANILLA_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell.setComponent(filterField);

        HeaderCell cell2 = filterRow.getCell(EMPLEADO_PROPERTY);

        TextField filterField2 = new TextField();
        filterField2.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField2.setInputPrompt("Filtrar");
        filterField2.setColumns(20);

        filterField2.addTextChangeListener(change -> {
            planillaContainer.removeContainerFilters(EMPLEADO_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                planillaContainer.addContainerFilter(
                        new SimpleStringFilter(EMPLEADO_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell2.setComponent(filterField2);

        planillaFooter = planillaGrid.appendFooterRow();
        planillaFooter.getCell(EMPLEADO_PROPERTY).setText("Totales");
        planillaFooter.getCell(VALOR_PROPERTY).setText("0.00");
        planillaFooter.getCell(VALOR_PROPERTY).setStyleName("rightalign");

        documentosLayout.addComponent(planillaGrid);
        documentosLayout.setComponentAlignment(planillaGrid, Alignment.TOP_CENTER);

        mainLayout.addComponent(documentosLayout);
        mainLayout.setComponentAlignment(documentosLayout, Alignment.TOP_CENTER);

    }

    public void llenarGridPlanilla() {
        planillaFooter.getCell(VALOR_PROPERTY).setText("0.00");

        planillaContainer.removeAllItems();
        anticiposContainer.removeAllItems();
        planillaContainer.removeAllContainerFilters();

        double totalMonto = 0.00;

        queryString = " SELECT CodigoPartida, IdPartida, Fecha, SerieDocumento, NumeroDocumento, IdProveedor, ";
        queryString += " IdNomenclatura, NombreProveedor, MonedaDocumento ,Sum(Haber) Pagar, CodigoPartida, CodigoCC, ";
        queryString += " Saldo, MontoAutorizadoPagar, MontoAplicarAnticipo";
        queryString += " FROM contabilidad_partida ";
        queryString += " WHERE UPPER(contabilidad_partida.TipoDocumento) = 'PLANILLA' ";
        queryString += " AND IdProveedor > 0 ";
        queryString += " AND Estatus <> 'ANULADO' ";
        queryString += " AND IdEmpresa = " + empresaId;
        queryString += " AND IdNomenclatura = " + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getSueldosPorPagar();
        queryString += " AND Saldo > 0";
//        queryString += " And EXTRACT(YEAR FROM Fecha) >= EXTRACT(YEAR FROM current_date)";
        queryString += " AND CodigoCC NOT IN (SELECT codigoCc FROM autorizacion_pago)";
        queryString += " GROUP BY IdProveedor, SerieDocumento, NumeroDocumento ";

        try {
            stQueryPlanillas = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecordsPlanillas = stQueryPlanillas.executeQuery(queryString);
            stQuery1 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            if (rsRecordsPlanillas.next()) { //  encontrado

                do {
                    queryString = " SELECT SUM(HABER - DEBE) TOTALSALDO, ";
                    queryString += " SUM(HaberQuetzales - DebeQuetzales) TOTALSALDOQ ";
                    queryString += " FROM contabilidad_partida";
                    queryString += " WHERE IdEmpresa = " + empresaId;
                    queryString += " AND CodigoCC = '" + rsRecordsPlanillas.getString("CodigoCC") + "'";
                    queryString += " AND contabilidad_partida.IdNomenclatura = " + rsRecordsPlanillas.getString("IdNomenclatura");
                    queryString += " AND contabilidad_partida.Estatus <> 'ANULADO'";

                    rsRecords1 = stQuery1.executeQuery(queryString);

                    if (rsRecords1.next()) {

                        if (rsRecords1.getDouble("TOTALSALDO") > 0.00) {
                            Object itemId = planillaContainer.addItem();

                            planillaContainer.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecordsPlanillas.getString("IdPartida"));
                            planillaContainer.getContainerProperty(itemId, CODIGO_PARTIDA_PROPERTY).setValue(rsRecordsPlanillas.getString("CodigoPartida"));
                            planillaContainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecordsPlanillas.getDate("Fecha")));
                            planillaContainer.getContainerProperty(itemId, DOCUMENTO_PROPERTY).setValue(rsRecordsPlanillas.getString("NumeroDocumento"));
                            planillaContainer.getContainerProperty(itemId, PLANILLA_PROPERTY).setValue(rsRecordsPlanillas.getString("SerieDocumento"));
                            planillaContainer.getContainerProperty(itemId, CODIGO_PROPERTY).setValue(rsRecordsPlanillas.getString("IdProveedor"));
                            planillaContainer.getContainerProperty(itemId, EMPLEADO_PROPERTY).setValue(rsRecordsPlanillas.getString("NombreProveedor"));
                            planillaContainer.getContainerProperty(itemId, MONEDA_PROPERTY).setValue(rsRecordsPlanillas.getString("MonedaDocumento"));
                            planillaContainer.getContainerProperty(itemId, VALOR_PROPERTY).setValue(numberFormat.format(rsRecordsPlanillas.getDouble("PAGAR")));
                            planillaContainer.getContainerProperty(itemId, SALDO_PROPERTY).setValue(numberFormat.format(rsRecordsPlanillas.getDouble("Saldo")));
                            planillaContainer.getContainerProperty(itemId, AUTORIZADO_PROPERTY).setValue(numberFormat.format(rsRecordsPlanillas.getDouble("MontoAutorizadoPagar")));
                            planillaContainer.getContainerProperty(itemId, ANTICIPO_PROPERTY).setValue(numberFormat.format(rsRecordsPlanillas.getDouble("MontoAplicarAnticipo")));
                            planillaContainer.getContainerProperty(itemId, CODIGOCC_PROPERTY).setValue(rsRecordsPlanillas.getString("CodigoCC"));

                            totalMonto += rsRecordsPlanillas.getDouble("Pagar");
                        }
                    }

                } while (rsRecordsPlanillas.next());
            }

        } catch (Exception ex) {
            System.out.println("Error al listar tabla de Planilla laboral por pagar : " + ex);
            ex.printStackTrace();
        }

        planillaFooter.getCell(VALOR_PROPERTY).setText(numberFormat.format(totalMonto));
    }

    public void crearGridAnticipos() {

        HorizontalLayout reportLayout = new HorizontalLayout();
        reportLayout.setWidth("100%");
        reportLayout.addStyleName("rcorners3");
        reportLayout.setResponsive(true);
        reportLayout.setMargin(true);
        reportLayout.setSpacing(true);

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

        anticiposContainer.addContainerProperty(ID2_PROPERTY, String.class, null);
        anticiposContainer.addContainerProperty(CODIGOPARTIDA_PROPERTY, String.class, null);
        anticiposContainer.addContainerProperty(FECHA2, String.class, null);
        anticiposContainer.addContainerProperty(DOCUMENTO_PROPERTY, String.class, null);
        anticiposContainer.addContainerProperty(MONEDA2_PROPERTY, String.class, null);
        anticiposContainer.addContainerProperty(DEBE, String.class, null);
        anticiposContainer.addContainerProperty(HABER, String.class, null);
        anticiposContainer.addContainerProperty(DEBE_QUETZALES, String.class, null);
        anticiposContainer.addContainerProperty(HABER_QUETZALES, String.class, null);
        anticiposContainer.addContainerProperty(SALDO2, String.class, null);
        anticiposContainer.addContainerProperty(UTILIZAR_PROPERTY, String.class, null);

        anticiposGrid = new Grid("Historial de anticipos otorgados al empleado", anticiposContainer);
        anticiposGrid.setImmediate(true);
        anticiposGrid.setSelectionMode(Grid.SelectionMode.NONE);
        anticiposGrid.setDescription("Seleccione un registro para ingresar o editar.");
        anticiposGrid.setHeightMode(HeightMode.ROW);
        anticiposGrid.setHeightByRows(3);
        anticiposGrid.setWidth("100%");
        anticiposGrid.setResponsive(true);
        anticiposGrid.setEditorBuffered(false);
        anticiposGrid.setSizeFull();
        anticiposGrid.setEditorEnabled(true);
        anticiposGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if (DEBE.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (HABER.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (DEBE_QUETZALES.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (HABER_QUETZALES.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (SALDO2.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }
        });

        anticiposGrid.getColumn(UTILIZAR_PROPERTY).setEditorField(getAmmountField(UTILIZAR_PROPERTY));
        anticiposGrid.addItemClickListener((event) -> {
            if (event != null) {
                montoPendienteChequeTxt.setValue(0.00);
                totalUtilizarAnticiposTxt.setValue(0.00);
                anticiposFooter.getCell(UTILIZAR_PROPERTY).setText("0.00");
                anticiposGrid.editItem(event.getItemId());
            }
        });

        anticiposGrid.getColumn(ID2_PROPERTY).setHidable(true).setHidden(true);
        anticiposGrid.getColumn(HABER).setHidable(true).setHidden(true);
        anticiposGrid.getColumn(HABER_QUETZALES).setHidable(true).setHidden(true);
        anticiposGrid.getColumn(DEBE_QUETZALES).setHidable(true).setHidden(true);

        anticiposFooter = anticiposGrid.appendFooterRow();
        anticiposFooter.getCell(MONEDA2_PROPERTY).setText("TOTALES");
        anticiposFooter.getCell(DEBE).setText("0.00");
        anticiposFooter.getCell(SALDO2).setText("0.00");
        anticiposFooter.getCell(UTILIZAR_PROPERTY).setText("0.00");

        anticiposFooter.getCell(DEBE).setStyleName("rightalign");
        anticiposFooter.getCell(SALDO2).setStyleName("rightalign");
        anticiposFooter.getCell(UTILIZAR_PROPERTY).setStyleName("rightalign");

        totalUtilizarAnticiposTxt = new NumberField("Total a utilizar en anticipos:");
        totalUtilizarAnticiposTxt.setDecimalAllowed(true);
        totalUtilizarAnticiposTxt.setDecimalPrecision(2);
        totalUtilizarAnticiposTxt.setMinimumFractionDigits(2);
        totalUtilizarAnticiposTxt.setDecimalSeparator('.');
        totalUtilizarAnticiposTxt.setDecimalSeparatorAlwaysShown(true);
        totalUtilizarAnticiposTxt.setValue(0d);
        totalUtilizarAnticiposTxt.setGroupingUsed(true);
        totalUtilizarAnticiposTxt.setGroupingSeparator(',');
        totalUtilizarAnticiposTxt.setGroupingSize(3);
        totalUtilizarAnticiposTxt.setImmediate(true);
        totalUtilizarAnticiposTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        totalUtilizarAnticiposTxt.setWidth("8em");

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

                if (planillaGrid.getSelectedRow() != null) {
                    actualizarPlanilla();

                    Notification notif = new Notification("AUTORIZACION EXITOSA",
                            Notification.Type.HUMANIZED_MESSAGE);
                    notif.setDelayMsec(1500);
                    notif.setPosition(Position.MIDDLE_CENTER);
                    notif.setIcon(FontAwesome.CHECK);
                    notif.show(Page.getCurrent());

                    montoPendienteChequeTxt.setValue(0.00);
                    totalUtilizarAnticiposTxt.setValue(0.00);

                    llenarGridPlanilla();
                }
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
        
        reportLayout.addComponent(anticiposGrid);
        reportLayout.setComponentAlignment(anticiposGrid, Alignment.BOTTOM_CENTER);
        
        leftLayout.addComponent(salirBtn);
        leftLayout.setComponentAlignment(salirBtn, Alignment.BOTTOM_LEFT);

        rightLayout.addComponents(montoPendienteChequeTxt,totalUtilizarAnticiposTxt,autorizarBtn);
        rightLayout.setComponentAlignment(montoPendienteChequeTxt, Alignment.TOP_RIGHT);
        rightLayout.setComponentAlignment(totalUtilizarAnticiposTxt, Alignment.TOP_RIGHT);
        rightLayout.setComponentAlignment(autorizarBtn, Alignment.BOTTOM_RIGHT);

        buttonsLayout.addComponents(leftLayout, rightLayout);
        buttonsLayout.setComponentAlignment(leftLayout, Alignment.BOTTOM_LEFT);
        buttonsLayout.setComponentAlignment(rightLayout, Alignment.TOP_RIGHT);

        mainLayout.addComponent(reportLayout);
        mainLayout.setComponentAlignment(reportLayout, Alignment.TOP_CENTER);

        mainLayout.addComponent(buttonsLayout);    

    }

    private Field<?> getAmmountField(String propertyId) {

        NumberField valueTxt = new NumberField("Monto :");
        valueTxt.setWidth("10em");
        valueTxt.setDecimalAllowed(true);
        valueTxt.setDecimalPrecision(2);
        valueTxt.setMinimumFractionDigits(2);
        valueTxt.setDecimalSeparator('.');
        valueTxt.setDecimalSeparatorAlwaysShown(true);
        valueTxt.setValue(0d);
        valueTxt.setGroupingUsed(true);
        valueTxt.setGroupingSeparator(',');
        valueTxt.setGroupingSize(3);
        valueTxt.setImmediate(true);
        valueTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        valueTxt.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (event.getProperty().getValue() != null) {
                    if (!String.valueOf(event.getProperty().getValue()).trim().isEmpty()) {
                        if (anticiposContainer.size() > 0) {

                            for (Object itemId : anticiposContainer.getItemIds()) {

                                Item item = anticiposContainer.getItem(itemId);
                                Object propertyValue = item.getItemProperty(propertyId).getValue(); ///Utilizar"UTILIZAR"
                                Object propertyValue2 = item.getItemProperty(SALDO2).getValue();

                                if (Double.valueOf(String.valueOf(propertyValue).replaceAll(",", "")) > Double.valueOf(String.valueOf(propertyValue2).replaceAll(",", ""))) {
                                    Notification.show("El Monto utilizar no puede ser mayor al monto del anticipo", Notification.Type.ERROR_MESSAGE);
                                    valueTxt.setValue(0.00);
                                    return;
                                }

                            }
                            setFooterTotal(propertyId);
                        }

                    }
                }
            }
        });

        return valueTxt;
    }

    private void setFooterTotal(String propertyId) {

        double total = 0.00;
        double montoCheque = 0.00;
        double utilizarAnticipos = 0.00;

        for (Object itemId : anticiposContainer.getItemIds()) {
            Item item = anticiposContainer.getItem(itemId);
            Object propertyValue = item.getItemProperty(propertyId).getValue();

            total += Double.valueOf(String.valueOf(propertyValue));
        }
        if (anticiposFooter != null) {

            anticiposFooter.getCell(propertyId).setText(String.valueOf(total));

            utilizarAnticipos = Double.parseDouble(String.valueOf(anticiposFooter.getCell(UTILIZAR_PROPERTY).getText().replaceAll(",", "")));
            totalUtilizarAnticiposTxt.setValue(utilizarAnticipos);

            montoCheque = (saldoPlanillaSeleccionada - Double.parseDouble(String.valueOf(totalUtilizarAnticiposTxt.getValue())));
            montoPendienteChequeTxt.setValue(montoCheque);

        }

    }

    public void llenarTablaAnticipos() {

        anticiposContainer.removeAllItems();

        double saldoTotalLiquidar = 0.00;
        double montoAnticipos = 0.00;

        String proveedorSeleccionado = String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), CODIGO_PROPERTY).getValue());
        String tipoMonedaSeleccionado = String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), MONEDA_PROPERTY).getValue());

        queryString = " SELECT contabilidad_partida.IdPartida, contabilidad_partida.CodigoPartida, contabilidad_partida.Fecha,";
        queryString += " contabilidad_partida.TipoDocumento,contabilidad_partida.NumeroDocumento, ";
        queryString += " contabilidad_partida.Descripcion,contabilidad_partida.MonedaDocumento,";
        queryString += " contabilidad_partida.Debe, contabilidad_partida.Haber,contabilidad_partida.TipoCambio,";
        queryString += " contabilidad_partida.DebeQuetzales, contabilidad_partida.HaberQuetzales, contabilidad_partida.Saldo";
        queryString += " FROM contabilidad_partida";
        queryString += " WHERE contabilidad_partida.IdProveedor = " + proveedorSeleccionado;
        queryString += " AND contabilidad_partida.IdEmpresa = " + empresaId;
        queryString += " AND contabilidad_partida.IdNomenclatura = " + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getAnticiposSueldos();
        queryString += " AND contabilidad_partida.TipoDocumento In ('CHEQUE', 'TRANSFERENCIA', 'NOTA DE CREDITO')";
        queryString += " AND contabilidad_partida.MonedaDocumento = '" + tipoMonedaSeleccionado + "'";
        queryString += " AND contabilidad_partida.Saldo > 0.00";

        try {
            stQuery1 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords1 = stQuery1.executeQuery(queryString);
            if (rsRecords1.next()) { //  encontrado      

                do {

                    Object itemId = anticiposContainer.addItem();
                    anticiposContainer.getContainerProperty(itemId, ID2_PROPERTY).setValue(rsRecords1.getString("IdPartida"));
                    anticiposContainer.getContainerProperty(itemId, CODIGOPARTIDA_PROPERTY).setValue(rsRecords1.getString("CodigoPartida"));
                    anticiposContainer.getContainerProperty(itemId, FECHA2).setValue(rsRecords1.getString("Fecha"));
                    anticiposContainer.getContainerProperty(itemId, DOCUMENTO_PROPERTY).setValue(rsRecords1.getString("TipoDocumento"));
                    anticiposContainer.getContainerProperty(itemId, MONEDA2_PROPERTY).setValue(rsRecords1.getString("MonedaDocumento"));
                    anticiposContainer.getContainerProperty(itemId, DEBE).setValue(rsRecords1.getString("Debe"));
                    anticiposContainer.getContainerProperty(itemId, HABER).setValue(numberFormat.format((rsRecords1.getDouble("haber"))));
                    anticiposContainer.getContainerProperty(itemId, DEBE_QUETZALES).setValue(numberFormat.format((rsRecords1.getDouble("DebeQuetzales"))));
                    anticiposContainer.getContainerProperty(itemId, HABER_QUETZALES).setValue(numberFormat.format((rsRecords1.getDouble("HaberQuetzales"))));
                    anticiposContainer.getContainerProperty(itemId, SALDO2).setValue(numberFormat.format((rsRecords1.getDouble("Saldo"))));
                    anticiposContainer.getContainerProperty(itemId, UTILIZAR_PROPERTY).setValue("0.00");

                    montoAnticipos += rsRecords1.getDouble("Saldo");
                    saldoTotalLiquidar += rsRecords1.getDouble("Saldo");
                } while (rsRecords1.next());
                anticiposFooter.getCell(DEBE).setText(numberFormat.format(montoAnticipos));
                anticiposFooter.getCell(SALDO2).setText(numberFormat.format(saldoTotalLiquidar));
            }
        } catch (Exception ex) {
            System.out.println("Error al listar tabla nomenclatura contable en AutorizarPagoPlanillaFomr:" + ex);
            ex.printStackTrace();
        }
    }

    private void actualizarPlanilla() {

        try {

            double montoAuotizadoPagar = 0.00;
            double montoAplicarAnticipo = 0.00;

            montoAuotizadoPagar = montoPendienteChequeTxt.getDoubleValueDoNotThrow()
                    + Double.valueOf(String.valueOf(
                    planillaContainer.getContainerProperty(
                            planillaGrid.getSelectedRow(), AUTORIZADO_PROPERTY).getValue()).
                    replaceAll("Q.", "").
                    replaceAll("\\$.", "").
                    replaceAll(",", ""));

            montoAplicarAnticipo = totalUtilizarAnticiposTxt.getDoubleValueDoNotThrow() + Double.valueOf(String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), ANTICIPO_PROPERTY).getValue()).replaceAll("Q.", "").replaceAll("\\$.", "").replaceAll(",", ""));

            queryString = "UPDATE contabilidad_partida SET ";
            queryString += " MontoAutorizadoPagar = " + montoAuotizadoPagar;
            queryString += ", MontoAplicarAnticipo = " + montoAplicarAnticipo;
            //           queryString += ", Saldo = " + nuevoSaldo;
            queryString += " WHERE CodigoPartida = '" + String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), CODIGO_PARTIDA_PROPERTY).getValue()) + "'";
            queryString += " AND IdEmpresa = " + empresaId;

            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();stQuery.executeUpdate(queryString);

            queryString = "  INSERT INTO autorizacion_pago (TipoAutorizacion, IdEmpresa, IdProveedor, ";
            queryString += " Fecha, Moneda, Monto, CodigoCC, CodigoCCRelacionado, CuentaContableLiquidar, ";
            queryString += " Objetivo, CreadoUsuario, CreadoFechaYHora)";
            queryString += " VALUES ";
            queryString += "(";
            queryString += "'" + AutorizacionesPagoView.PAGO_PLANILLA + "'";
            queryString += "," + empresaId;
            queryString += "," + String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), CODIGO_PROPERTY).getValue());
            queryString += ",current_date";
            queryString += ",'QUETZALES'";
            queryString += "," + montoPendienteChequeTxt.getDoubleValueDoNotThrow();
            queryString += ",'" +  String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), CODIGOCC_PROPERTY).getValue()) + "'";
            queryString += ",''"; // codigoccrelacionado
            queryString += ",''"; // cuentacontableliquidar
            queryString += ",'" + AutorizacionesPagoView.PAGO_PLANILLA + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";

            stQuery.executeUpdate(queryString);

            insertAnticiposAutorizadosEMPLEADO();

            Notification notif = new Notification("Autorización exitosa",
                    Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.CHECK);
            notif.show(Page.getCurrent());

        } catch (Exception ex) {
            Notification.show("Error al momento de actualizar el registro ", Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void insertAnticiposAutorizadosEMPLEADO() {

        String proveedorSeleccionado = String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(), CODIGO_PROPERTY).getValue());
        String codigoCC = String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(),  CODIGOCC_PROPERTY).getValue());
        String moneda = String.valueOf(planillaContainer.getContainerProperty(planillaGrid.getSelectedRow(),  MONEDA_PROPERTY).getValue());

        if (anticiposContainer.size() > 0) {

            for (Object itemId : anticiposContainer.getItemIds()) {

                Item item = anticiposContainer.getItem(itemId);

                Object codigoCCAnticipo = item.getItemProperty(CODIGOCC_PROPERTY).getValue();
                Object montoUtilziar = item.getItemProperty(UTILIZAR_PROPERTY).getValue();

                double montoUtilizarVariable = Double.parseDouble(String.valueOf(montoUtilziar).replaceAll(",", ""));

                if (montoUtilizarVariable > 0) {

                    try {
                        queryString = " DELETE FROM autorizacion_pago ";
                        queryString += " WHERE CodigoCCRelacionado = '" + codigoCCAnticipo + "'";
                        queryString += " AND   CodigoCC = '" + codigoCC + "'";

                        stQuery.executeUpdate(queryString);

                        queryString = "  INSERT INTO autorizacion_pago (TipoAutorizacion, IdEmpresa, IdProveedor, ";
                        queryString += " Fecha, Moneda, Monto, CodigoCC, CodigoCCRelacionado, CuentaContableLiquidar, ";
                        queryString += " Objetivo, CreadoUsuario, CreadoFechaYHora)";
                        queryString += " VALUES ";
                        queryString += "(";
                        queryString += "'" + AutorizacionesPagoView.PAGO_DOCUMENTO + "'";
                        queryString += "," + empresaId;
                        queryString += "," + proveedorSeleccionado;
                        queryString += ",current_date";
                        queryString += ",'" + moneda + "'";
                        queryString += "," + montoUtilizarVariable;
                        queryString += ",'" + codigoCC + "'";
                        queryString += ",'" + codigoCCAnticipo + "'";
                        queryString += ",''"; // cuentacontableliquidar
                        queryString += ",'" + AutorizacionesPagoView.PAGO_DOCUMENTO + "'";
                        queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                        queryString += ",current_timestamp";
                        queryString += ")";

                        stQuery.executeUpdate(queryString);

                    } catch (SQLException ex) {
                        Notification.show("Error al momento  INSERTAR REGISTROS ANTICIPOS (EMPLEADOS) en tablaautorizacion_pago. ", Notification.Type.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                }
            }
        }

        montoPendienteChequeTxt.setValue(0.00);
        totalUtilizarAnticiposTxt.setValue(0.00);

    }
}
