package com.simpletecno.sopdi.tesoreria;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.SelectionEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.vaadin.ui.NumberField;

/**
 * Formulario de selección de anticipos otorgados a clientes para devolución.
 *
 * Basado en {@link AutorizarPagoDevolucionClienteForm}, pero en lugar de insertar
 * autorizaciones de pago, devuelve a quien lo invoca (vía {@link SeleccionListener})
 * la lista de anticipos seleccionados con el monto a devolver escrito en cada uno.
 *
 * Reglas:
 *  - Se listan los anticipos/enganches de clientes con saldo &gt; 0.
 *  - El usuario puede seleccionar varios registros, pero todos del MISMO cliente y
 *    de la MISMA moneda (un solo cheque = un cliente y una moneda).
 *  - El monto a devolver de cada anticipo no puede exceder su saldo pendiente.
 *
 * @author user
 */
public class SeleccionAnticiposDevolucionForm extends Window {

    /** Un anticipo seleccionado con su monto a devolver. */
    public static class DevolucionItem {
        public final String idProveedor;
        public final String nombreProveedor;
        public final String codigoCC;
        public final String idNomenclatura;
        public final String moneda;
        public final double montoDevolver;
        public final String tipo;

        public DevolucionItem(String idProveedor, String nombreProveedor, String codigoCC,
                              String idNomenclatura, String moneda, double montoDevolver, String tipo) {
            this.idProveedor = idProveedor;
            this.nombreProveedor = nombreProveedor;
            this.codigoCC = codigoCC;
            this.idNomenclatura = idNomenclatura;
            this.moneda = moneda;
            this.montoDevolver = montoDevolver;
            this.tipo = tipo;
        }
    }

    /** Callback invocado al aceptar la selección. */
    public interface SeleccionListener {
        void onSeleccion(List<DevolucionItem> items, double total);
    }

    private final SeleccionListener seleccionListener;
    /** Si no está vacío, filtra los anticipos por este IdProveedor (cliente). */
    private final String idProveedorFiltro;

    VerticalLayout mainLayout;

    Button salirBtn;
    Button aceptarBtn;

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

    public SeleccionAnticiposDevolucionForm(SeleccionListener seleccionListener) {
        this(seleccionListener, null);
    }

    public SeleccionAnticiposDevolucionForm(SeleccionListener seleccionListener, String idProveedorFiltro) {
        this.seleccionListener = seleccionListener;
        this.idProveedorFiltro = idProveedorFiltro;
        this.mainUI = UI.getCurrent();
        setResponsive(true);
        setWidth("85%");
        setHeight("95%");
        setModal(true);

        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setMargin(new MarginInfo(false, true, false, true));
        mainLayout.setResponsive(true);

        HorizontalLayout layoutTitle = new HorizontalLayout();
        layoutTitle.setWidth("100%");
        layoutTitle.setSpacing(true);
        layoutTitle.setMargin(new MarginInfo(true, true, false, true));

        Label titleLbl = new Label("");
        titleLbl.setValue(empresaId + " " + empresaNombre + " DEVOLUCION ANTICIPO CLIENTE");
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

        clienteGrid = new Grid("Anticipos de clientes pendientes de devolver.", container);
        clienteGrid.setWidth("100%");
        clienteGrid.setImmediate(true);
        clienteGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        clienteGrid.setDescription("Seleccione uno o varios registros del mismo cliente y moneda.");
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
                (SelectionEvent.SelectionListener) event -> actualizarTotal()
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
        clienteGrid.getColumn(MONTO_DEVOLVER).setExpandRatio(2);

        Grid.HeaderRow filterRow = clienteGrid.appendHeaderRow();

        Grid.HeaderCell cell = filterRow.getCell(PROVEEDOR);

        TextField filterField = new TextField();
        filterField.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField.setInputPrompt("Filtrar");
        filterField.setColumns(10);

        filterField.addTextChangeListener(change -> {
            container.removeContainerFilters(PROVEEDOR);
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(
                        new SimpleStringFilter(PROVEEDOR,
                                change.getText(), true, false));
            }
        });
        cell.setComponent(filterField);

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

    /** Suma el monto a devolver de las filas seleccionadas y lo muestra. */
    private void actualizarTotal() {
        if (clienteGrid.getSelectedRows() == null) return;
        double granTotal = 0.00;
        for (Object itemId : clienteGrid.getSelectedRows()) {
            granTotal += parseMonto(container.getContainerProperty(itemId, MONTO_DEVOLVER).getValue());
        }
        totalDevolver.setValue("Total a devolver : " + numberFormat.format(granTotal));
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
        valueTxt.selectAll();
        valueTxt.setDescription("Doble click para selecionar todo el monto...");
        valueTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        valueTxt.addValueChangeListener((Property.ValueChangeListener) event -> {
            if (container.size() > 0) {
                if (event.getProperty().getValue() != null) {
                    if (!String.valueOf(event.getProperty().getValue()).trim().isEmpty()) {
                        Item item = container.getItem(clienteGrid.getEditedItemId());
                        if (item == null) return;
                        Object propertyValue = item.getItemProperty(propertyId).getValue(); //MONTO_DEVOLVER
                        Object propertyValue2 = item.getItemProperty(SALDO_MONEDA).getValue();

                        if (parseMonto(propertyValue) > parseMonto(propertyValue2)) {
                            Notification.show("El Monto a devolver no puede ser mayor al saldo del ANTICIPO", Notification.Type.ERROR_MESSAGE);
                            event.getProperty().setValue(String.valueOf(propertyValue2).replaceAll(",", ""));
                        }
                        actualizarTotal();
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

        queryString =  " SELECT cp.MonedaDocumento, cne.N5, cp.IdNomenclatura, cp.CodigoCC, pe.IdProveedor,";
        queryString += " pe.Nombre, SUM(cp.Debe) AS SUMDEBE, SUM(cp.Haber) AS SUMHABER,";
        queryString += " SUM(cp.DebeQuetzales) AS SUMDEBEQ, SUM(cp.HaberQuetzales) AS SUMHABERQ";
        queryString += " FROM contabilidad_partida cp";
        queryString += " INNER JOIN proveedor_empresa pe ON cp.IdProveedor = pe.IdProveedor";
        queryString += " INNER JOIN contabilidad_nomenclatura_empresa cne ON cne.IdNomenclatura = cp.IdNomenclatura";
        queryString += " WHERE cp.IdEmpresa = " + empresaId;
        queryString += " AND cp.Fecha >= '2019-01-01'";
        queryString += " AND cp.CodigoCC NOT IN (SELECT ap.CodigoCC FROM autorizacion_pago ap)";
        queryString += " AND cp.Estatus <> 'ANULADO'";
        queryString += " AND cp.IdNomenclatura IN (";
        queryString += ((SopdiUI) mainUI).cuentasContablesDefault.getEnganches();
        queryString += ",";
        queryString += ((SopdiUI) mainUI).cuentasContablesDefault.getAnticiposClientes();
        queryString += ")";
        queryString += " AND cne.IdEmpresa = " + empresaId;
        queryString += " AND pe.IdEmpresa = " + empresaId;
        if (idProveedorFiltro != null && !idProveedorFiltro.trim().isEmpty()) {
            queryString += " AND pe.IdProveedor = " + idProveedorFiltro.trim();
        }
        queryString += " GROUP BY cp.MonedaDocumento, cne.N5, cp.IdNomenclatura, cp.CodigoCC, pe.IdProveedor, pe.Nombre";
        queryString += " ORDER BY cp.Fecha DESC";

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
                    queryString += " AND IdNomenclatura = " + rsRecords.getString("IdNomenclatura");

                    rsRecords1 = stQuery1.executeQuery(queryString);

                    if (rsRecords1.next()) {

                        if (rsRecords1.getDouble("TOTALSALDO") > 0.00) {

                            Object itemId = container.addItem();
                            container.getContainerProperty(itemId, TIPO).setValue(rsRecords.getString("N5"));
                            container.getContainerProperty(itemId, CODIGO_CC).setValue(rsRecords.getString("CodigoCC"));
                            container.getContainerProperty(itemId, ID_PROVEEDOR).setValue(rsRecords.getString("IdProveedor"));
                            container.getContainerProperty(itemId, PROVEEDOR).setValue(rsRecords.getString("Nombre"));
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
            System.out.println("Error al listar anticipos de clientes a devolver " + ex);
            ex.printStackTrace();
        }
    }

    public void crearComponentes() {

        HorizontalLayout camposDocumento = new HorizontalLayout();
        camposDocumento.setWidth("96%");
        camposDocumento.setSpacing(true);
        camposDocumento.setMargin(new MarginInfo(false, true, false, true));

        aceptarBtn = new Button("Aceptar");
        aceptarBtn.setIcon(FontAwesome.CHECK);
        aceptarBtn.setStyleName(ValoTheme.BUTTON_PRIMARY);
        aceptarBtn.addClickListener((Button.ClickListener) event -> aceptarSeleccion());

        salirBtn = new Button("Salir");
        salirBtn.setStyleName(ValoTheme.BUTTON_DANGER);
        salirBtn.addClickListener((Button.ClickListener) event -> close());

        camposDocumento.addComponents(salirBtn);
        camposDocumento.setComponentAlignment(salirBtn, Alignment.TOP_LEFT);
        camposDocumento.addComponents(aceptarBtn);
        camposDocumento.setComponentAlignment(aceptarBtn, Alignment.TOP_RIGHT);

        mainLayout.addComponent(camposDocumento);
        mainLayout.setComponentAlignment(camposDocumento, Alignment.TOP_LEFT);
    }

    /**
     * Valida la selección (mismo cliente, misma moneda, montos &gt; 0 y &lt;= saldo),
     * la entrega al listener y cierra el formulario.
     */
    private void aceptarSeleccion() {

        if (clienteGrid.getSelectedRows() == null || clienteGrid.getSelectedRows().isEmpty()) {
            Notification.show("Seleccione al menos un anticipo a devolver.", Notification.Type.HUMANIZED_MESSAGE);
            return;
        }

        List<DevolucionItem> items = new ArrayList<>();
        double total = 0.00;
        String idProveedorComun = null;
        String monedaComun = null;

        for (Object gridItem : clienteGrid.getSelectedRows()) {

            Item item = clienteGrid.getContainerDataSource().getItem(gridItem);

            String idProveedor = String.valueOf(item.getItemProperty(ID_PROVEEDOR).getValue());
            String moneda = String.valueOf(item.getItemProperty(MONEDA).getValue());
            double montoDevolver = parseMonto(item.getItemProperty(MONTO_DEVOLVER).getValue());
            double saldoAnticipo = parseMonto(item.getItemProperty(SALDO_MONEDA).getValue());

            if (montoDevolver <= 0.00) {
                Notification.show("Ingrese el monto a devolver del anticipo o anticipos seleccionados.", Notification.Type.HUMANIZED_MESSAGE);
                return;
            }
            if (montoDevolver > saldoAnticipo) {
                Notification.show("El monto a devolver no puede ser mayor al saldo del anticipo.", Notification.Type.ERROR_MESSAGE);
                return;
            }
            if (idProveedorComun == null) {
                idProveedorComun = idProveedor;
            } else if (!idProveedorComun.equals(idProveedor)) {
                Notification.show("Todos los anticipos seleccionados deben ser del mismo cliente.", Notification.Type.WARNING_MESSAGE);
                return;
            }
            if (monedaComun == null) {
                monedaComun = moneda;
            } else if (!monedaComun.equals(moneda)) {
                Notification.show("Todos los anticipos seleccionados deben ser de la misma moneda.", Notification.Type.WARNING_MESSAGE);
                return;
            }

            items.add(new DevolucionItem(
                    idProveedor,
                    String.valueOf(item.getItemProperty(PROVEEDOR).getValue()),
                    String.valueOf(item.getItemProperty(CODIGO_CC).getValue()),
                    String.valueOf(item.getItemProperty(IDNOMENCLATURA).getValue()),
                    moneda,
                    montoDevolver,
                    String.valueOf(item.getItemProperty(TIPO).getValue())
            ));
            total += montoDevolver;
        }

        if (seleccionListener != null) {
            seleccionListener.onSeleccion(items, total);
        }
        close();
    }

    /** Convierte un valor del container a double, tolerando comas y símbolos. */
    private double parseMonto(Object value) {
        try {
            if (value == null) return 0.00;
            String s = String.valueOf(value).replaceAll("[^0-9.]", "");
            return s.isEmpty() ? 0.00 : Double.parseDouble(s);
        } catch (NumberFormatException ex) {
            return 0.00;
        }
    }
}
