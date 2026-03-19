package com.simpletecno.sopdi.tesoreria;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.SelectionEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Iterator;

import org.vaadin.ui.NumberField;

/**
 * @author user
 */
public class AutorizarPagoDevolucionClienteForm extends Window {

    VerticalLayout mainLayout;

    Button salirBtn;
    Button autorizarBtn;

    UI mainUI;
    Statement stQuery, stQuery1;
    ResultSet rsRecords, rsRecords1;
    String queryString;

    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");

    static final String TIPO = "Tipo";
    static final String ID_PROVEEDOR = "Proveedor";
    static final String PROVEEDOR = "Nombre";
    static final String CODIGO_CC = "CodigoCC";
    static final String MONEDA = "Moneda";
    static final String DEBE_MONEDA = "Debe";
    static final String HABER_MONEDA = "Haber";
    static final String SALDO_MONEDA = "Saldo";
    static final String DEBE_QUETZALES = "Debe Q.";
    static final String HABER_QUETZALES = "Haber Q.";
    static final String SALDO_Q = "Saldo Q.";
    static final String IDNOMENCLATURA = "IdNomenclatura";
    static final String MONTO_DEVOLVER = "Devolver";

    public IndexedContainer container = new IndexedContainer();
    Grid clienteGrid;
    Grid.FooterRow clienteFooter;

    Label totalDevolver = new Label("Total a devolver : 0.00");

    double totalDebeQuetzales = 0.00, totalHaberQueztales = 0.00;
    double saldo = 0.00;

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public AutorizarPagoDevolucionClienteForm() {
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

        Label titleLbl = new Label("");
        titleLbl.setValue(empresaId + " " + empresaNombre + " " + AutorizacionesPagoView.DEVOLUCION_CLIENTE);
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h2_custom");

        layoutTitle.addComponents(titleLbl);
        layoutTitle.setComponentAlignment(titleLbl, Alignment.MIDDLE_RIGHT);

        mainLayout.addComponent(layoutTitle);

        setContent(mainLayout);
        createTablaDevolucionesCliente();
        llenarTablaDevolucionesCliente();
        crearComponentes();
    }

    public void createTablaDevolucionesCliente() {

        VerticalLayout reportLayout = new VerticalLayout();
        reportLayout.setWidth("100%");
        reportLayout.addStyleName("rcorners3");
        reportLayout.setResponsive(true);
        reportLayout.setMargin(new MarginInfo(false, true, false, true));
        reportLayout.setSpacing(true);

        container.addContainerProperty(TIPO, String.class, "");
        container.addContainerProperty(ID_PROVEEDOR, String.class, "");
        container.addContainerProperty(PROVEEDOR, String.class, "");
        container.addContainerProperty(CODIGO_CC, String.class, "");
        container.addContainerProperty(MONEDA, String.class, "");
        container.addContainerProperty(DEBE_MONEDA, String.class, "");
        container.addContainerProperty(HABER_MONEDA, String.class, "");
        container.addContainerProperty(SALDO_MONEDA, String.class, "");
        container.addContainerProperty(DEBE_QUETZALES, String.class, "");
        container.addContainerProperty(HABER_QUETZALES, String.class, "");
        container.addContainerProperty(SALDO_Q, String.class, "");
        container.addContainerProperty(IDNOMENCLATURA, String.class, "");
        container.addContainerProperty(MONTO_DEVOLVER, String.class, "");

        clienteGrid = new Grid("Enganches pendientes de liquidar.", container);
        clienteGrid.setWidth("100%");
        clienteGrid.setImmediate(true);
        clienteGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        clienteGrid.setDescription("Seleccione uno o varios registros del mismo cliente.");
        clienteGrid.setHeightMode(HeightMode.ROW);
        clienteGrid.setHeightByRows(8);
        clienteGrid.setResponsive(true);
        clienteGrid.setEditorBuffered(false);
        clienteGrid.setEditorEnabled(true);

        clienteGrid.getColumn(MONTO_DEVOLVER).setEditorField(getAmmountField(MONTO_DEVOLVER));
        clienteGrid.addItemClickListener((event) -> {
            if (event != null) {
                clienteGrid.editItem(event.getItemId());
            }
        });

        clienteGrid.addSelectionListener(
                (SelectionEvent.SelectionListener) event -> {
                    if (clienteGrid.getSelectedRows() != null) {
                        double granTotal = 0.00;
                        for(Object itemId : clienteGrid.getSelectedRows()) {
                            granTotal+= Double.valueOf(String.valueOf(container.getContainerProperty(itemId, MONTO_DEVOLVER).getValue()));
                        }
                        totalDevolver.setValue("Total a devolver : " + String.valueOf(granTotal));
                    }
                }
        );

        clienteGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if (DEBE_MONEDA.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (HABER_MONEDA.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (SALDO_MONEDA.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (DEBE_QUETZALES.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (HABER_QUETZALES.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (SALDO_Q.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (MONTO_DEVOLVER.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }
        });

        clienteGrid.getColumn(TIPO).setExpandRatio(1).setHidable(true).setHidden(true);
        clienteGrid.getColumn(ID_PROVEEDOR).setExpandRatio(1).setHidable(true).setHidden(true);
        clienteGrid.getColumn(CODIGO_CC).setExpandRatio(1).setHidable(true).setHidden(true);
        clienteGrid.getColumn(IDNOMENCLATURA).setExpandRatio(1).setHidable(true).setHidden(true);
        clienteGrid.getColumn(PROVEEDOR).setExpandRatio(3);
        clienteGrid.getColumn(MONEDA).setExpandRatio(2);
        clienteGrid.getColumn(DEBE_MONEDA).setExpandRatio(2);
        clienteGrid.getColumn(HABER_MONEDA).setExpandRatio(2);
        clienteGrid.getColumn(SALDO_MONEDA).setExpandRatio(2);
        clienteGrid.getColumn(DEBE_QUETZALES).setExpandRatio(2);
        clienteGrid.getColumn(HABER_QUETZALES).setExpandRatio(2);
        clienteGrid.getColumn(SALDO_Q).setExpandRatio(2);
        clienteGrid.getColumn(SALDO_Q).setExpandRatio(2);
        clienteGrid.getColumn(MONTO_DEVOLVER).setExpandRatio(2);

        Grid.HeaderRow filterRow = clienteGrid.appendHeaderRow();

        Grid.HeaderCell cell = filterRow.getCell(PROVEEDOR);

        TextField filterField = new TextField();
        filterField.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField.setInputPrompt("Filtrar");
        filterField.setColumns(10);

        filterField.addTextChangeListener(change -> {
            container.removeContainerFilters(PROVEEDOR);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(
                        new SimpleStringFilter(PROVEEDOR,
                                change.getText(), true, false));
            }
        });
        cell.setComponent(filterField);

        Grid.HeaderCell cell0 = filterRow.getCell(TIPO);

        TextField filterField0 = new TextField();
        filterField0.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField0.setInputPrompt("Filtrar");
        filterField0.setColumns(8);

        filterField0.addTextChangeListener(change -> {
            container.removeContainerFilters(TIPO);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(
                        new SimpleStringFilter(TIPO,
                                change.getText(), true, false));
            }
        });

        cell0.setComponent(filterField0);

        clienteFooter = clienteGrid.appendFooterRow();
        clienteFooter.getCell(CODIGO_CC).setText("TOTAL");
        clienteFooter.getCell(DEBE_MONEDA).setText("0.00");
        clienteFooter.getCell(HABER_MONEDA).setText("0.00");
        clienteFooter.getCell(SALDO_MONEDA).setText("0.00");
        clienteFooter.getCell(DEBE_QUETZALES).setText("0.00");
        clienteFooter.getCell(HABER_QUETZALES).setText("0.00");
        clienteFooter.getCell(SALDO_Q).setText("0.00");
        clienteFooter.getCell(MONTO_DEVOLVER).setText("0.00");

        clienteFooter.getCell(DEBE_MONEDA).setStyleName("rightalign");
        clienteFooter.getCell(HABER_MONEDA).setStyleName("rightalign");
        clienteFooter.getCell(SALDO_MONEDA).setStyleName("rightalign");
        clienteFooter.getCell(DEBE_QUETZALES).setStyleName("rightalign");
        clienteFooter.getCell(HABER_QUETZALES).setStyleName("rightalign");
        clienteFooter.getCell(SALDO_Q).setStyleName("rightalign");
        clienteFooter.getCell(MONTO_DEVOLVER).setStyleName("rightalign");

        reportLayout.addComponent(clienteGrid);
        reportLayout.setComponentAlignment(clienteGrid, Alignment.MIDDLE_CENTER);

        HorizontalLayout totalLayout = new HorizontalLayout();
        totalLayout.setWidth("100%");
        totalLayout.setSpacing(true);
        totalLayout.setMargin(new MarginInfo(true, true, false, true));

        totalDevolver.setSizeUndefined();

        totalLayout.addComponent(totalDevolver);
        totalLayout.setComponentAlignment(totalDevolver, Alignment.MIDDLE_CENTER);

        reportLayout.addComponent(totalLayout);
        reportLayout.setComponentAlignment(totalLayout, Alignment.BOTTOM_CENTER);

        mainLayout.addComponent(reportLayout);
        mainLayout.setComponentAlignment(reportLayout, Alignment.TOP_CENTER);

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
//        valueTxt.setValue(Double.valueOf(String.valueOf(container.getContainerProperty(MONTO_DEVOLVER,clienteGrid.getEditedItemId()).getValue()).replaceAll(",", "")));
        valueTxt.selectAll();
        valueTxt.setDescription("Doble click para selecionar todo el monto...");
        valueTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        valueTxt.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (container.size() > 0) {
                    if (event.getProperty().getValue() != null) {
                        if (!String.valueOf(event.getProperty().getValue()).trim().isEmpty()) {
                            Item item = container.getItem(clienteGrid.getEditedItemId());
                            Object propertyValue = item.getItemProperty(propertyId).getValue(); ///MONTO_DEVOLVER
                            Object propertyValue2 = item.getItemProperty(SALDO_MONEDA).getValue();

                            if (Double.valueOf(String.valueOf(propertyValue).replaceAll(",", "")) > Double.valueOf(String.valueOf(propertyValue2).replaceAll(",", ""))) {
                                Notification.show("El Monto a devolver no puede ser mayor al saldo del ENGANCHE", Notification.Type.ERROR_MESSAGE);
//                                    valueTxt.setValue(0.00);
                                event.getProperty().setValue(String.valueOf(propertyValue2).replaceAll(",", ""));
                            }

                            double granTotal = 0.00;
                            for(Object itemId : clienteGrid.getSelectedRows()) {
                                granTotal+= Double.valueOf(String.valueOf(container.getContainerProperty(itemId, MONTO_DEVOLVER).getValue()));
                            }
                            totalDevolver.setValue("Total a devolver : " + String.valueOf(granTotal));
                        }
                    }
                }
            }
        });

        return valueTxt;
    }

    public void llenarTablaDevolucionesCliente() {

        container.removeAllItems();

        clienteFooter.getCell(DEBE_MONEDA).setText("0.00");
        clienteFooter.getCell(HABER_MONEDA).setText("0.00");
        clienteFooter.getCell(SALDO_MONEDA).setText("0.00");
        clienteFooter.getCell(DEBE_QUETZALES).setText("0.00");
        clienteFooter.getCell(HABER_QUETZALES).setText("0.00");
        clienteFooter.getCell(SALDO_Q).setText("0.00");

        totalDebeQuetzales = 0.00;
        totalHaberQueztales = 0.00;
        saldo = 0.00;

        queryString = "  SELECT contabilidad_partida.MonedaDocumento, contabilidad_nomenclatura_empresa.N5, contabilidad_partida.IdNomenclatura, ";
        queryString += " contabilidad_partida.CodigoCC, proveedor_empresa.IdProveedor, proveedor_empresa.Nombre, ";
        queryString += " SUM(contabilidad_partida.Debe) SUMDEBE, SUM(contabilidad_partida.Haber) SUMHABER,";
        queryString += " SUM(contabilidad_partida.DebeQuetzales) SUMDEBEQ, SUM(contabilidad_partida.HaberQuetzales) SUMHABERQ ";
        queryString += " FROM contabilidad_partida";
        queryString += " INNER JOIN proveedor_empresa on contabilidad_partida.IdProveedor = proveedor_empresa.IDProveedor ";
        queryString += " INNER JOIN contabilidad_nomenclatura_empresa on contabilidad_nomenclatura_empresa.IdNomenclatura = contabilidad_partida.IdNomenclatura";
        queryString += " WHERE contabilidad_partida.IdEmpresa =" + empresaId;
        queryString += " AND contabilidad_partida.Fecha >= '2019-01-01'";
        queryString += " AND contabilidad_partida.CodigoCC NOT IN (SELECT autorizacion_pago.CodigoCC FROM autorizacion_pago)";
        queryString += " AND contabilidad_partida.Estatus <> 'ANULADO'";
        queryString += " AND contabilidad_partida.IdNomenclatura In (" + ((SopdiUI) mainUI).cuentasContablesDefault.getEnganches() + "," + ((SopdiUI) mainUI).cuentasContablesDefault.getAnticiposClientes() + ")";
        queryString += " AND contabildiad_nomenclatura_empresa.IdEmpresa = " + empresaId;
        queryString += " ANd proveedor_empresa.IdEmpresa = " + empresaId;
        queryString += " GROUP BY contabilidad_partida.MonedaDocumento, contabilidad_partida.IdNomenclatura, contabilidad_partida.CodigoCC, proveedor.IdProveedor ";
        queryString += " ORDER BY contabilidad_partida.Fecha desc";

        try {

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);
            stQuery1 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            if (rsRecords.next()) {

                do {

                    queryString = " SELECT ";
                    queryString += " SUM(HABER - DEBE) TOTALSALDO, SUM(HaberQuetzales - DebeQuetzales) TOTALSALDOQ ";
                    queryString += " FROM contabilidad_partida";
                    queryString += " WHERE IdEmpresa = " + empresaId;
                    queryString += " AND CodigoCC = '" + rsRecords.getString("CodigoCC") + "'";
                    queryString += " AND Estatus <> 'ANULADO'";
                    queryString += " and IdNomenclatura = " + rsRecords.getString("IdNomenclatura");

                    rsRecords1 = stQuery1.executeQuery(queryString);

                    if (rsRecords1.next()) {

                        if (rsRecords1.getDouble("TOTALSALDO") > 0.00) {

                            Object itemId = container.addItem();
                            container.getContainerProperty(itemId, TIPO).setValue(rsRecords.getString("N5"));
                            container.getContainerProperty(itemId, CODIGO_CC).setValue(rsRecords.getString("CodigoCC"));
//                            container.getContainerProperty(itemId, FECHA).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha")));
                            container.getContainerProperty(itemId, ID_PROVEEDOR).setValue(rsRecords.getString("proveedor.IdProveedor"));
                            container.getContainerProperty(itemId, PROVEEDOR).setValue(rsRecords.getString("proveedor.Nombre"));
                            container.getContainerProperty(itemId, MONEDA).setValue(rsRecords.getString("MonedaDocumento"));
                            container.getContainerProperty(itemId, DEBE_MONEDA).setValue(numberFormat.format((rsRecords.getDouble("SUMDEBE"))));
                            container.getContainerProperty(itemId, HABER_MONEDA).setValue(numberFormat.format((rsRecords.getDouble("SUMHABER"))));
                            container.getContainerProperty(itemId, SALDO_MONEDA).setValue(numberFormat.format((rsRecords1.getDouble("TOTALSALDO"))));
                            container.getContainerProperty(itemId, DEBE_QUETZALES).setValue(numberFormat.format((rsRecords.getDouble("SUMDEBEQ"))));
                            container.getContainerProperty(itemId, HABER_QUETZALES).setValue(numberFormat.format((rsRecords.getDouble("SUMHABERQ"))));
                            container.getContainerProperty(itemId, SALDO_Q).setValue(numberFormat.format((rsRecords1.getDouble("TOTALSALDOQ"))));
                            container.getContainerProperty(itemId, IDNOMENCLATURA).setValue(rsRecords.getString("IdNomenclatura"));
                            container.getContainerProperty(itemId, MONTO_DEVOLVER).setValue(rsRecords1.getString("TOTALSALDO"));

                            totalDebeQuetzales = totalDebeQuetzales + rsRecords.getDouble("SUMDEBEQ");
                            totalHaberQueztales = totalHaberQueztales + rsRecords.getDouble("SUMHABERQ");
                            saldo = saldo + rsRecords1.getDouble("TOTALSALDOQ");
                        }
                    }
                } while (rsRecords.next());

                clienteFooter.getCell(DEBE_QUETZALES).setText(numberFormat.format(totalDebeQuetzales));
                clienteFooter.getCell(HABER_QUETZALES).setText(numberFormat.format(totalHaberQueztales));
                clienteFooter.getCell(SALDO_Q).setText(numberFormat.format(saldo));
            }
        } catch (Exception ex) {
            System.out.println("Error al listar historial devoluciones a clientes " + ex);
            ex.printStackTrace();
        }
    }

    public void crearComponentes() {

        HorizontalLayout camposDocumento = new HorizontalLayout();
        camposDocumento.setWidth("96%");
        camposDocumento.setSpacing(true);
        camposDocumento.setMargin(new MarginInfo(false, true, false, true));

        autorizarBtn = new Button("Autorizar");
        autorizarBtn.setIcon(FontAwesome.SAVE);
        //autorizarBtn.setWidth("60%");
        //autorizarBtn.setHeight("80%");
        autorizarBtn.setStyleName(ValoTheme.BUTTON_PRIMARY);
        autorizarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                insertTablaDevoluciones();
            }
        });

        salirBtn = new Button("Salir");
        salirBtn.setStyleName(ValoTheme.BUTTON_DANGER);
        salirBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();
            }
        });

        camposDocumento.addComponents(salirBtn);
        camposDocumento.setComponentAlignment(salirBtn, Alignment.TOP_LEFT);
        camposDocumento.addComponents(autorizarBtn);
        camposDocumento.setComponentAlignment(autorizarBtn, Alignment.TOP_RIGHT);

        mainLayout.addComponent(camposDocumento);
        mainLayout.setComponentAlignment(camposDocumento, Alignment.TOP_LEFT);
    }

    public void insertTablaDevoluciones() {

        Iterator iter;
        iter = clienteGrid.getSelectedRows().iterator();

        String proveedorId = "";
        String codigoCC = "";
        Double montoDevolver = 0.00;

        while (iter.hasNext()) {
            Object gridItem = iter.next();

            montoDevolver = Double.valueOf(String.valueOf(clienteGrid.getContainerDataSource().getItem(gridItem).getItemProperty(MONTO_DEVOLVER).getValue()).replaceAll(",", ""));

            if(montoDevolver == 0.00) {
                Notification.show("Por favor ingrese el monto a devolver, del enganche o enganches seleccionados...!", Notification.Type.HUMANIZED_MESSAGE);
                return;
            }

            codigoCC = String.valueOf(clienteGrid.getContainerDataSource().getItem(gridItem).getItemProperty(CODIGO_CC).getValue());
            proveedorId = String.valueOf(clienteGrid.getContainerDataSource().getItem(gridItem).getItemProperty(ID_PROVEEDOR).getValue());

            queryString = "  Insert Into autorizacion_pago (TipoAutorizacion, IdEmpresa, IdProveedor, ";
            queryString += " Fecha, Moneda, Monto, CodigoCC, CuentaContableLiquidar, ";
            queryString += " Objetivo, CreadoUsuario, CreadoFechaYHora)";
            queryString += " Values ";
            queryString += "(";
            queryString += "'" + AutorizacionesPagoView.DEVOLUCION_CLIENTE + "'";
            queryString += "," + empresaId;
            queryString += "," + proveedorId;
            queryString += ",current_date";
            queryString += ",'" + clienteGrid.getContainerDataSource().getItem(gridItem).getItemProperty(MONEDA).getValue() + "'";
            queryString += "," + montoDevolver;
            queryString += ",'" + codigoCC + "'"; // codigoCC
            queryString += ",'" + clienteGrid.getContainerDataSource().getItem(gridItem).getItemProperty(IDNOMENCLATURA).getValue() + "'"; // cuentacontableliquidar
            queryString += ",'" + clienteGrid.getContainerDataSource().getItem(gridItem).getItemProperty(TIPO).getValue() + "'";
            queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
            queryString += ",current_timestamp";
            queryString += ")";

            try {

                stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                stQuery.executeUpdate(queryString);

            } catch (Exception ex1) {
                System.out.println("Error al insertar registro en autorizacion_pago :  " + ex1.getMessage());
                ex1.printStackTrace();
            }

        }

        Notification notif = new Notification("AUTORIZACION EXITOSA", Notification.Type.HUMANIZED_MESSAGE);
        notif.setDelayMsec(1500);
        notif.setPosition(Position.MIDDLE_CENTER);
        notif.setIcon(FontAwesome.CHECK);
        notif.show(Page.getCurrent());
        close();
    }
}