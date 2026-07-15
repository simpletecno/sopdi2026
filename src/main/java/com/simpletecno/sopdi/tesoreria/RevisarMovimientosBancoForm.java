package com.simpletecno.sopdi.tesoreria;

import com.simpletecno.sopdi.contabilidad.MostrarPartidaContable;
import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.themes.ValoTheme;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Date;

/**
 * Formulario de revisión de movimientos de banco.
 *
 * Copiado de {@link IngresoBancosView}, pero:
 *   - Sin el botón "Nuevo movimiento" (esta ventana solo consulta / revisa).
 *   - Sin el grid ni el layout de partida ({@code partidaDocumentosGrid}); no es
 *     necesario porque al hacer clic en el Código de partida ya se lanza
 *     {@link MostrarPartidaContable} con la partida contable.
 */
public class RevisarMovimientosBancoForm extends Window {

    // ── Filtro de tipo de movimiento ─────────────────────────────────────────
    public static final String FILTRO_EGRESOS  = "EGRESOS";
    public static final String FILTRO_INGRESOS = "INGRESOS";
    public static final String FILTRO_TODO     = "TODO MOVIMIENTO";

    /** TipoDocumento considerados INGRESOS (créditos al banco). */
    private static final String TIPOS_INGRESO =
            "'PRESTAMOS','ENGANCHES','DEPOSITO POR COMPRA DE MONEDA','DEPOSITO','TRANSFERENCIA'," +
            "'NOTA DE CREDITO','INTERESES DEVENGADOS','REEMBOLSO DE ANTICIPOS','PAGOS DE FACTURA VENTA'";

    /** TipoDocumento considerados EGRESOS (débitos automáticos: servicio bancario, intereses préstamo, ...). */
    private static final String TIPOS_EGRESO = "'NOTA DE DEBITO','CHEQUE'";

    UI mainUI;
    Statement stQuery;
    ResultSet rsRecords;
    String queryString;

    DateField inicioDt;
    DateField finDt;
    ComboBox tipoFiltroCbx;
    Button consultarBtn;

    Grid ingresoBancosGrid;
    Grid.FooterRow ingresosFooter;

    public IndexedContainer container = new IndexedContainer();
    static final String ID_PROPERTY = "Id";
    static final String FECHA_PROPERTY = "Fecha";
    static final String ID_EMPRESA_PROPERTY = "Empresa";
    static final String MEDIO_PROPERTY = "Medio";
    static final String DOCUMENTO_PROPERTY = "Documento";
    static final String DESCRIPCION_PROPERTY = "Descripción";
    static final String TIPO_PROPERTY = "TIPO";
    static final String CLIENTE_PROPERTY = "Proveedor/Cliente";
    static final String MONEDA_PROPERTY = "Moneda";
    static final String MONTO_PROPERTY = "Monto";
    static final String ESTATUS_PROPERTY = "Estatus";
    static final String MONTOSF_PROPERTY = "MSF";

    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    /** Por defecto abre el formulario mostrando EGRESOS. */
    public RevisarMovimientosBancoForm() {
        this(FILTRO_EGRESOS);
    }

    /**
     * @param filtroInicial uno de {@link #FILTRO_EGRESOS}, {@link #FILTRO_INGRESOS}
     *                      o {@link #FILTRO_TODO}. Si es nulo/desconocido se usa EGRESOS.
     */
    public RevisarMovimientosBancoForm(String filtroInicial) {
        this.mainUI = UI.getCurrent();
        setCaption(empresaId + " " + empresaNombre + " REVISAR MOVIMIENTOS DE BANCO");
        setModal(true);
        setResponsive(true);
        setWidth("90%");
        // Sin alto fijo: la ventana se ajusta al contenido (grid con HeightMode.ROW),
        // evitando el espacio vacío vertical.

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);
        mainLayout.setWidth("100%");
        setContent(mainLayout);

        createTablaTransacciones(mainLayout);

        String filtro = FILTRO_INGRESOS.equals(filtroInicial) || FILTRO_TODO.equals(filtroInicial)
                ? filtroInicial : FILTRO_EGRESOS;
        tipoFiltroCbx.select(filtro);

        llenarTablaFactura(empresaId);
    }

    public void createTablaTransacciones(VerticalLayout parentLayout) {
        VerticalLayout reportLayout = new VerticalLayout();
        reportLayout.addStyleName("rcorners3");
        reportLayout.setWidth("100%");
        reportLayout.setMargin(true);
        reportLayout.setResponsive(true);
        reportLayout.setSpacing(true);

        HorizontalLayout filtrosLayout = new HorizontalLayout();
        filtrosLayout.setSpacing(true);
        filtrosLayout.setMargin(false);

        inicioDt = new DateField("DEL:");
        inicioDt.setDateFormat("dd/MM/yyyy");
        Date primerDia = Utileria.getPrimerDiaDelMes();
        inicioDt.setValue(primerDia);
        inicioDt.setWidth("10em");

        finDt = new DateField("AL:");
        finDt.setDateFormat("dd/MM/yyyy");
        Date ultimoDia = Utileria.getUltimoDiaDelMes();
        finDt.setValue(ultimoDia);
        finDt.setWidth("10em");

        tipoFiltroCbx = new ComboBox("Movimiento:");
        tipoFiltroCbx.setWidth("14em");
        tipoFiltroCbx.setNullSelectionAllowed(false);
        tipoFiltroCbx.setNewItemsAllowed(false);
        tipoFiltroCbx.setTextInputAllowed(false);
        tipoFiltroCbx.setImmediate(true);
        tipoFiltroCbx.addItem(FILTRO_EGRESOS);
        tipoFiltroCbx.addItem(FILTRO_INGRESOS);
        tipoFiltroCbx.addItem(FILTRO_TODO);
        tipoFiltroCbx.select(FILTRO_EGRESOS);
        tipoFiltroCbx.addValueChangeListener(e -> llenarTablaFactura(empresaId));

        consultarBtn = new Button("Consultar");
        consultarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        consultarBtn.setIcon(FontAwesome.SEARCH);
        consultarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                llenarTablaFactura(empresaId);
            }
        });

        filtrosLayout.addComponents(inicioDt, finDt, tipoFiltroCbx, consultarBtn);
        filtrosLayout.setComponentAlignment(inicioDt, Alignment.MIDDLE_CENTER);
        filtrosLayout.setComponentAlignment(finDt, Alignment.MIDDLE_CENTER);
        filtrosLayout.setComponentAlignment(tipoFiltroCbx, Alignment.MIDDLE_CENTER);
        filtrosLayout.setComponentAlignment(consultarBtn, Alignment.BOTTOM_RIGHT);

        reportLayout.addComponent(filtrosLayout);
        reportLayout.setComponentAlignment(filtrosLayout, Alignment.MIDDLE_CENTER);

        container.addContainerProperty(ID_PROPERTY, String.class, null);
        container.addContainerProperty(FECHA_PROPERTY, String.class, null);
        container.addContainerProperty(MEDIO_PROPERTY, String.class, null);
        container.addContainerProperty(DOCUMENTO_PROPERTY, String.class, null);
        container.addContainerProperty(ID_EMPRESA_PROPERTY, String.class, null);
        container.addContainerProperty(DESCRIPCION_PROPERTY, String.class, null);
        container.addContainerProperty(TIPO_PROPERTY, String.class, null);
        container.addContainerProperty(CLIENTE_PROPERTY, String.class, null);
        container.addContainerProperty(MONEDA_PROPERTY, String.class, null);
        container.addContainerProperty(MONTO_PROPERTY, String.class, null);
        container.addContainerProperty(ESTATUS_PROPERTY, String.class, null);
        container.addContainerProperty(MONTOSF_PROPERTY, String.class, null);

        ingresoBancosGrid = new Grid(container);
        ingresoBancosGrid.setWidth("100%");
        ingresoBancosGrid.setImmediate(true);
        ingresoBancosGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        ingresoBancosGrid.setDescription("Seleccione un registro.");
        ingresoBancosGrid.setHeightMode(HeightMode.ROW);
        ingresoBancosGrid.setHeightByRows(12);

        ingresoBancosGrid.setResponsive(true);
        ingresoBancosGrid.setEditorBuffered(false);

        ingresosFooter = ingresoBancosGrid.appendFooterRow();
        ingresosFooter.getCell(MONEDA_PROPERTY).setText("0 TRANSACCIONES");
        ingresosFooter.getCell(MONTO_PROPERTY).setText("0.00");

        reportLayout.addComponent(ingresoBancosGrid);
        reportLayout.setComponentAlignment(ingresoBancosGrid, Alignment.MIDDLE_CENTER);

        ingresoBancosGrid.getColumn(ID_PROPERTY).setHidable(true);
        ingresoBancosGrid.getColumn(ID_EMPRESA_PROPERTY).setHidable(true).setHidden(true);
        ingresoBancosGrid.getColumn(MONTOSF_PROPERTY).setHidable(true).setHidden(true);
        ingresoBancosGrid.getColumn(MEDIO_PROPERTY).setHidable(true).setHidden(true);
        ingresoBancosGrid.getColumn(FECHA_PROPERTY).setExpandRatio(2);
        ingresoBancosGrid.getColumn(MONTO_PROPERTY).setExpandRatio(2);

        ingresoBancosGrid.setCellStyleGenerator(cellRef
                -> MONTO_PROPERTY.equals(cellRef.getPropertyId())
                ? "rightalign" : null);

        ingresoBancosGrid.getColumn(ID_PROPERTY).setRenderer(new ButtonRenderer(e -> {
            String codigoPartida = String.valueOf(container.getContainerProperty(e.getItemId(), ID_PROPERTY).getValue());
            String descripcion = String.valueOf(container.getContainerProperty(e.getItemId(), DESCRIPCION_PROPERTY).getValue());
            String documento = String.valueOf(container.getContainerProperty(e.getItemId(), DOCUMENTO_PROPERTY).getValue());

            MostrarPartidaContable mostrarPartidaContable
                    = new MostrarPartidaContable(
                            codigoPartida,
                            descripcion,
                            "",
                            documento
                    );
            mainUI.addWindow(mostrarPartidaContable);
            mostrarPartidaContable.center();
        }));

        ingresoBancosGrid.getColumn(ESTATUS_PROPERTY).setRenderer(new ButtonRenderer(e -> {
            String codigoPartida = String.valueOf(container.getContainerProperty(e.getItemId(), ID_PROPERTY).getValue());
            String estatus = String.valueOf(container.getContainerProperty(e.getItemId(), ESTATUS_PROPERTY).getValue());
            CambiarEstatusPago cambiarEstatusPago
                    = new CambiarEstatusPago(
                            container,
                            e.getItemId(),
                            codigoPartida,
                            estatus
                    );
            UI.getCurrent().addWindow(cambiarEstatusPago);
            cambiarEstatusPago.center();

        }));

        Grid.HeaderRow filterRow = ingresoBancosGrid.appendHeaderRow();

        Grid.HeaderCell cell0 = filterRow.getCell(MEDIO_PROPERTY);

        TextField filterField0 = new TextField();
        filterField0.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField0.setInputPrompt("Filtrar");
        filterField0.setColumns(8);

        filterField0.addTextChangeListener(change -> {
            container.removeContainerFilters(MEDIO_PROPERTY);
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(
                        new SimpleStringFilter(MEDIO_PROPERTY,
                                change.getText(), true, false));
            }
            setTotal();
        });

        cell0.setComponent(filterField0);

        Grid.HeaderCell cell00 = filterRow.getCell(DOCUMENTO_PROPERTY);

        TextField filterField00 = new TextField();
        filterField00.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField00.setInputPrompt("Filtrar");
        filterField00.setColumns(12);

        filterField00.addTextChangeListener(change -> {
            container.removeContainerFilters(DOCUMENTO_PROPERTY);
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(
                        new SimpleStringFilter(DOCUMENTO_PROPERTY,
                                change.getText(), true, false));
            }
            setTotal();
        });

        cell00.setComponent(filterField00);

        Grid.HeaderCell cell = filterRow.getCell(DESCRIPCION_PROPERTY);

        TextField filterField = new TextField();
        filterField.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField.setInputPrompt("Filtrar");
        filterField.setColumns(12);

        filterField.addTextChangeListener(change -> {
            container.removeContainerFilters(DESCRIPCION_PROPERTY);
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(
                        new SimpleStringFilter(DESCRIPCION_PROPERTY,
                                change.getText(), true, false));
            }
            setTotal();
        });

        cell.setComponent(filterField);

        Grid.HeaderCell cell2 = filterRow.getCell(ID_PROPERTY);

        TextField filterField2 = new TextField();
        filterField2.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField2.setInputPrompt("Filtrar");
        filterField2.setColumns(12);

        filterField2.addTextChangeListener(change -> {
            container.removeContainerFilters(ID_PROPERTY);
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(
                        new SimpleStringFilter(ID_PROPERTY,
                                change.getText(), true, false));
            }
            setTotal();
        });

        cell2.setComponent(filterField2);

        Grid.HeaderCell cell3 = filterRow.getCell(MONEDA_PROPERTY);

        TextField filterField3 = new TextField();
        filterField3.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField3.setInputPrompt("Filtrar");
        filterField3.setColumns(12);

        filterField3.addTextChangeListener(change -> {
            container.removeContainerFilters(MONEDA_PROPERTY);
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(
                        new SimpleStringFilter(MONEDA_PROPERTY,
                                change.getText(), true, false));
            }
            setTotal();
        });

        cell3.setComponent(filterField3);

        Grid.HeaderCell cell4 = filterRow.getCell(CLIENTE_PROPERTY);

        TextField filterField4 = new TextField();
        filterField4.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField4.setInputPrompt("Filtrar");
        filterField4.setColumns(12);

        filterField4.addTextChangeListener(change -> {
            container.removeContainerFilters(CLIENTE_PROPERTY);
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(
                        new SimpleStringFilter(CLIENTE_PROPERTY,
                                change.getText(), true, false));
            }
            setTotal();
        });

        cell4.setComponent(filterField4);

        Grid.HeaderCell cell5 = filterRow.getCell(TIPO_PROPERTY);

        TextField filterField5 = new TextField();
        filterField5.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField5.setInputPrompt("Filtrar");
        filterField5.setColumns(12);

        filterField5.addTextChangeListener(change -> {
            container.removeContainerFilters(TIPO_PROPERTY);
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(
                        new SimpleStringFilter(TIPO_PROPERTY,
                                change.getText(), true, false));
            }
            setTotal();
        });

        cell5.setComponent(filterField5);

        Grid.HeaderCell cell6 = filterRow.getCell(ESTATUS_PROPERTY);

        TextField filterField6 = new TextField();
        filterField6.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField6.setInputPrompt("Filtrar");
        filterField6.setColumns(12);

        filterField6.addTextChangeListener(change -> {
            container.removeContainerFilters(ESTATUS_PROPERTY);
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(
                        new SimpleStringFilter(ESTATUS_PROPERTY,
                                change.getText(), true, false));
            }
            setTotal();
        });

        cell6.setComponent(filterField6);

        parentLayout.addComponent(reportLayout);
        parentLayout.setComponentAlignment(reportLayout, Alignment.MIDDLE_CENTER);
    }

    public void llenarTablaFactura(String empresa) {
        container.removeAllItems();

        setTotal();

        queryString = " SELECT * FROM contabilidad_partida";
        queryString += " WHERE contabilidad_partida.TipoDocumento In (" + tiposDocumentoFiltro() + ")";
        queryString += " AND IdEmpresa = " + empresa;
        queryString += " AND contabilidad_partida.Fecha BETWEEN ";
        queryString += " '" + Utileria.getFechaYYYYMMDD_1(inicioDt.getValue()) + "'";
        queryString += " AND '" + Utileria.getFechaYYYYMMDD_1(finDt.getValue()) + "'";
        queryString += " GROUP BY CodigoPartida, Fecha";
        queryString += " ORDER BY CodigoPartida, Fecha ASC";

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado
                do {
                    Object itemId = container.addItem();
                    container.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords.getString("CodigoPartida"));
                    container.getContainerProperty(itemId, FECHA_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha")));
                    container.getContainerProperty(itemId, TIPO_PROPERTY).setValue(rsRecords.getString("TipoDocumento"));
                    container.getContainerProperty(itemId, MEDIO_PROPERTY).setValue(rsRecords.getString("SerieDocumento"));
                    container.getContainerProperty(itemId, DOCUMENTO_PROPERTY).setValue(rsRecords.getString("NumeroDocumento"));
                    container.getContainerProperty(itemId, ID_EMPRESA_PROPERTY).setValue(rsRecords.getString("IdEmpresa"));
                    container.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(rsRecords.getString("Descripcion"));
                    container.getContainerProperty(itemId, CLIENTE_PROPERTY).setValue(rsRecords.getString("IdProveedor") + " " + rsRecords.getString("NombreProveedor"));
                    container.getContainerProperty(itemId, MONEDA_PROPERTY).setValue(rsRecords.getString("MonedaDocumento"));
                    container.getContainerProperty(itemId, MONTO_PROPERTY).setValue(numberFormat.format(rsRecords.getDouble("MontoDocumento")));
                    container.getContainerProperty(itemId, ESTATUS_PROPERTY).setValue(rsRecords.getString("Estatus"));
                    container.getContainerProperty(itemId, MONTOSF_PROPERTY).setValue(String.valueOf(rsRecords.getDouble("MontoDocumento")));
                } while (rsRecords.next());
                setTotal();
            }
        } catch (Exception ex) {
            System.out.println("Error al listar tabla de movimientos:" + ex);
            ex.printStackTrace();
        }
    }

    /** Devuelve la lista de TipoDocumento para el IN(...) según el filtro seleccionado. */
    private String tiposDocumentoFiltro() {
        Object filtro = tipoFiltroCbx == null ? FILTRO_EGRESOS : tipoFiltroCbx.getValue();
        if (FILTRO_INGRESOS.equals(filtro)) {
            return TIPOS_INGRESO;
        } else if (FILTRO_TODO.equals(filtro)) {
            return TIPOS_INGRESO + "," + TIPOS_EGRESO;
        } else {
            return TIPOS_EGRESO;
        }
    }

    private void setTotal() {
        BigDecimal total = new BigDecimal(0).setScale(2, RoundingMode.HALF_UP);
        for (Object rid : ingresoBancosGrid.getContainerDataSource()
                .getItemIds()) {
            total = total.add(new BigDecimal(
                    Double.parseDouble(
                            String.valueOf(container.getContainerProperty(rid, MONTOSF_PROPERTY).getValue())
                    )));
        }
        ingresosFooter.getCell(MONEDA_PROPERTY).setText(container.size() + " TRANSACCIONES");
        ingresosFooter.getCell(MONTO_PROPERTY).setText(numberFormat.format(total));
    }
}
