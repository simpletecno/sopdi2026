package com.simpletecno.sopdi.tesoreria;

import com.simpletecno.sopdi.contabilidad.MostrarPartidaContable;
import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.SelectionEvent;
import com.vaadin.event.SelectionEvent.SelectionListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.themes.ValoTheme;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author user
 */
public class IngresoBancosView extends VerticalLayout implements View {

    UI mainUI;
    Statement stQuery;
    ResultSet rsRecords;
    String queryString;
    Double totalHaber;
    Double totalDebe;
    Double totalHaberQ;
    Double totalDebeQ;

    DateField inicioDt;
    DateField finDt;
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

    Grid partidaDocumentosGrid;
    public IndexedContainer containerPartida = new IndexedContainer();
    static final String ID_PARTIDA_PROPERTY = "Id";
    static final String CUENTA_PROPERTY = "Cuenta";
    static final String DESCRIPCION_PARTIDA_PROPERTY = "Descripción";
    static final String DEBE_PROPERTY = "Debe";
    static final String HABER_PROPERTY = "Haber";
    static final String DEBEQ_PROPERTY = "Debe Q.";
    static final String HABERQ_PROPERTY = "Haber Q.";
    Grid.FooterRow partidaFooter;

    VerticalLayout reportLayoutPartida = new VerticalLayout();
    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public IngresoBancosView() {
        this.mainUI = UI.getCurrent();
        setWidth("100%");
        setSpacing(true);
        //setMargin(true);
        reportLayoutPartida.setEnabled(false);

        Label titleLbl = new Label(empresaId + " " + empresaNombre + " MOVIMIENTOS DE BANCOS");
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

        createTablaTransacciones();
        createTablaPartida();

        llenarTablaFactura(empresaId);
    }

    public void createTablaTransacciones() {
        VerticalLayout reportLayout = new VerticalLayout();
        reportLayout.addStyleName("rcorners3");
        reportLayout.setWidth("100%");
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

        consultarBtn = new Button("Consultar");
        consultarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        consultarBtn.setIcon(FontAwesome.SEARCH);
        consultarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                llenarTablaFactura(empresaId);
            }
        });

        Button nuevoBtn = new Button("Nuevo movimiento");
        nuevoBtn.setIcon(FontAwesome.PLUS);
        nuevoBtn.setDescription("Agregar nuevo movimiento a bancos.");
        nuevoBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                IngresosVariadosForm nuevosIngresos = new IngresosVariadosForm(empresaId);
                UI.getCurrent().addWindow(nuevosIngresos);
                nuevosIngresos.center();
            }
        });

        filtrosLayout.addComponents(inicioDt, finDt, consultarBtn, nuevoBtn);
        filtrosLayout.setComponentAlignment(inicioDt, Alignment.MIDDLE_CENTER);
        filtrosLayout.setComponentAlignment(finDt, Alignment.MIDDLE_CENTER);
        filtrosLayout.setComponentAlignment(consultarBtn, Alignment.BOTTOM_RIGHT);
        filtrosLayout.setComponentAlignment(nuevoBtn, Alignment.BOTTOM_RIGHT);

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
        ingresoBancosGrid.setHeightByRows(5);

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

        ingresoBancosGrid.addSelectionListener(new SelectionListener() {

            @Override
            public void select(SelectionEvent event) {
                if (ingresoBancosGrid.getSelectedRow() != null) {
                    llenarTablaPartida(String.valueOf(ingresoBancosGrid.getContainerDataSource().getItem(ingresoBancosGrid.getSelectedRow()).getItemProperty(ID_PROPERTY).getValue()));
                }
            }

        });

        ingresoBancosGrid.getColumn(ID_PROPERTY).setRenderer(new ButtonRenderer(e -> {
            String codigoPartida = String.valueOf(container.getContainerProperty(e.getItemId(), ID_PROPERTY).getValue());
            String descripcion = String.valueOf(container.getContainerProperty(e.getItemId(), DESCRIPCION_PROPERTY).getValue());
            String tipo = String.valueOf(container.getContainerProperty(e.getItemId(), TIPO_PROPERTY).getValue());
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

        addComponent(reportLayout);
        setComponentAlignment(reportLayout, Alignment.MIDDLE_CENTER);
    }

    public void llenarTablaFactura(String empresa) {
        container.removeAllItems();
        containerPartida.removeAllItems();

        setTotal();

        queryString = " SELECT * FROM contabilidad_partida";
        queryString += " WHERE contabilidad_partida.TipoDocumento In ('PRESTAMOS', ";
        queryString += " 'ENGANCHES', 'DEPOSITO POR COMPRA DE MONEDA','DEPOSITO','TRANSFERENCIA', 'NOTA DE CREDITO',";
        queryString += " 'NOTA DE DEBITO', 'INTERESES DEVENGADOS', 'REEMBOLSO DE ANTICIPOS', 'PAGOS DE FACTURA VENTA')";
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
                double total = 0.00;
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
            System.out.println("Error al listar tabla de ingresos:" + ex);
            ex.printStackTrace();
        }
    }

    public void createTablaPartida() {
        reportLayoutPartida.setWidth("75%");
        reportLayoutPartida.addStyleName("rcorners3");

        HorizontalLayout camposPartidaLayout = new HorizontalLayout();
        camposPartidaLayout.setSpacing(true);
        camposPartidaLayout.setWidth("100%");

        containerPartida.addContainerProperty(ID_PARTIDA_PROPERTY, String.class, null);
        containerPartida.addContainerProperty(CUENTA_PROPERTY, String.class, null);
        containerPartida.addContainerProperty(DESCRIPCION_PARTIDA_PROPERTY, String.class, null);
        containerPartida.addContainerProperty(DEBE_PROPERTY, String.class, null);
        containerPartida.addContainerProperty(HABER_PROPERTY, String.class, null);
        containerPartida.addContainerProperty(DEBEQ_PROPERTY, String.class, null);
        containerPartida.addContainerProperty(HABERQ_PROPERTY, String.class, null);

        partidaDocumentosGrid = new Grid("Partida contable", containerPartida);

        partidaDocumentosGrid.setImmediate(true);
        partidaDocumentosGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        partidaDocumentosGrid.setDescription("Seleccione un registro.");
        partidaDocumentosGrid.setHeightMode(HeightMode.ROW);
        partidaDocumentosGrid.setHeightByRows(3);

        partidaDocumentosGrid.setWidth("100%");
        partidaDocumentosGrid.setResponsive(true);
        partidaDocumentosGrid.setEditorBuffered(false);

        partidaDocumentosGrid.getColumn(ID_PROPERTY).setHidable(true).setHidden(true);
        partidaDocumentosGrid.getColumn(CUENTA_PROPERTY).setExpandRatio(1);
        partidaDocumentosGrid.getColumn(DESCRIPCION_PROPERTY).setExpandRatio(3);
        partidaDocumentosGrid.getColumn(DEBE_PROPERTY).setExpandRatio(1);
        partidaDocumentosGrid.getColumn(HABER_PROPERTY).setExpandRatio(1);
        partidaDocumentosGrid.getColumn(DEBEQ_PROPERTY).setExpandRatio(1);
        partidaDocumentosGrid.getColumn(HABERQ_PROPERTY).setExpandRatio(1);


        partidaDocumentosGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if (DEBE_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (HABER_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (DEBEQ_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (HABERQ_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }

        });

        partidaFooter = partidaDocumentosGrid.appendFooterRow();
        partidaFooter.getCell(DEBE_PROPERTY).setText("0.00");
        partidaFooter.getCell(HABER_PROPERTY).setText("0.00");
        partidaFooter.getCell(DEBE_PROPERTY).setStyleName("rightalign");
        partidaFooter.getCell(HABER_PROPERTY).setStyleName("rightalign");
        partidaFooter.getCell(DEBEQ_PROPERTY).setText("0.00");
        partidaFooter.getCell(HABERQ_PROPERTY).setText("0.00");
        partidaFooter.getCell(DEBEQ_PROPERTY).setStyleName("rightalign");
        partidaFooter.getCell(HABERQ_PROPERTY).setStyleName("rightalign");

        reportLayoutPartida.addComponent(partidaDocumentosGrid);

        Button editBtn = new Button("Editar");
        editBtn.setIcon(FontAwesome.EDIT);
        editBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        editBtn.setDescription("Actualizar datos");
        editBtn.addListener((Button.ClickListener) (Button.ClickEvent event) -> {

            if (String.valueOf(container.getContainerProperty(ingresoBancosGrid.getSelectedRow(), DESCRIPCION_PROPERTY).getValue()).equals("Ingreso por DEPOSITO POR VENTA DE MONEDA")) {
                IngresoDepositoVentaMonedaForm depositoMonedaForm = new IngresoDepositoVentaMonedaForm(String.valueOf(container.getContainerProperty(ingresoBancosGrid, ID_PROPERTY).getValue()));
                mainUI.addWindow(depositoMonedaForm);
                depositoMonedaForm.center();

            } else {
                Notification.show("Opción temporalmente deshabilitada.", Notification.Type.HUMANIZED_MESSAGE);
            }
        });

        Button printBtn = new Button("Imprimir partida");
        printBtn.setIcon(FontAwesome.FILE_PDF_O);
        printBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        printBtn.setDescription("Imprimir partida");
        printBtn.addListener((Button.ClickListener) (Button.ClickEvent event) -> {

            if (ingresoBancosGrid.getSelectedRow() == null) {
                Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
            } else {
                com.simpletecno.sopdi.contabilidad.LibroDiarioView libroDiario = new com.simpletecno.sopdi.contabilidad.LibroDiarioView();
                libroDiario.documentoTxt.setValue(String.valueOf(container.getContainerProperty(ingresoBancosGrid.getSelectedRow(), ID_PROPERTY).getValue()));

                try {
                    libroDiario.inicioDt.setValue(
                            new SimpleDateFormat("dd/MM/yyyy")
                                    .parse(String.valueOf(container.getContainerProperty(ingresoBancosGrid.getSelectedRow(), FECHA_PROPERTY).getValue())));
                    libroDiario.finDt.setValue(
                            new SimpleDateFormat("dd/MM/yyyy")
                                    .parse(String.valueOf(container.getContainerProperty(ingresoBancosGrid.getSelectedRow(), FECHA_PROPERTY).getValue())));
                } catch (Property.ReadOnlyException | Converter.ConversionException | IllegalStateException | ParseException ex) {
                    ex.printStackTrace();
                }
                libroDiario.consultarBtn.click();
                libroDiario.printPdf();
            }
        });

        Button revisadoBtn = new Button("Revisado");
        revisadoBtn.setIcon(FontAwesome.CHECK);
        revisadoBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        revisadoBtn.setDescription("Actualizar estatus");
        revisadoBtn.addListener((Button.ClickListener) (Button.ClickEvent event) -> {

            if (ingresoBancosGrid.getSelectedRow() == null) {
                Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
            } else {

                if (String.valueOf(container.getContainerProperty(ingresoBancosGrid.getSelectedRow(), ESTATUS_PROPERTY).getValue()).equals("REVISADO")) {
                    Notification.show("Documento ya REVISADO.", Notification.Type.WARNING_MESSAGE);
                } else {

                    try {

                        queryString = "UPDATE  contabilidad_partida";
                        queryString += " SET Estatus = 'REVISADO'";
                        queryString += " WHERE CodigoPartida = '" + String.valueOf(container.getContainerProperty(ingresoBancosGrid.getSelectedRow(), ID_PROPERTY).getValue()) + "'";

                        stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                        stQuery.executeUpdate(queryString);

                        container.getContainerProperty(ingresoBancosGrid.getSelectedRow(), ESTATUS_PROPERTY).setValue("REVISADO");

                    } catch (SQLException ex) {
                        System.out.println("Error al intentar modificar estatus a revisado" + ex);
                        ex.printStackTrace();
                    }
                }
            }
        });

        camposPartidaLayout.addComponents(editBtn, printBtn, revisadoBtn);

        camposPartidaLayout.setComponentAlignment(editBtn, Alignment.BOTTOM_LEFT);
        camposPartidaLayout.setComponentAlignment(printBtn, Alignment.BOTTOM_CENTER);
        camposPartidaLayout.setComponentAlignment(revisadoBtn, Alignment.BOTTOM_RIGHT);

        reportLayoutPartida.addComponent(camposPartidaLayout);

        addComponent(reportLayoutPartida);
        setComponentAlignment(reportLayoutPartida, Alignment.MIDDLE_CENTER);

    }

    public void llenarTablaPartida(String codigoPartida) {
        partidaFooter.getCell(DEBE_PROPERTY).setText("0.00");
        partidaFooter.getCell(HABER_PROPERTY).setText("0.00");
        partidaFooter.getCell(DEBEQ_PROPERTY).setText("0.00");
        partidaFooter.getCell(HABERQ_PROPERTY).setText("0.00");
        containerPartida.removeAllItems();
        reportLayoutPartida.setEnabled(true);

        totalDebe = 0.00;
        totalHaber = 0.00;
        totalDebeQ = 0.00;
        totalHaberQ = 0.00;

        String queryString;
        queryString = " SELECT contabilidad_partida.*,contabilidad_nomenclatura_empresa.N5, contabilidad_nomenclatura_empresa.NoCuenta";
        queryString += " FROM contabilidad_partida, contabilidad_nomenclatura_empresa";
        queryString += " WHERE contabilidad_partida.CodigoPartida = '" + codigoPartida + "'";
        queryString += " AND contabilidad_nomenclatura_empresa.IdNomenclatura = contabilidad_partida.IdNomenclatura";
        queryString += " AND contabilidad_nomenclatura_empresa.IdEmpresa = " + empresaId;

       // System.out.println("Query partida ingreso=" + queryString);

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado                                                
                do {
                    Object itemId = containerPartida.addItem();

                    containerPartida.getContainerProperty(itemId, ID_PARTIDA_PROPERTY).setValue(rsRecords.getString("IdPartida"));
                    containerPartida.getContainerProperty(itemId, CUENTA_PROPERTY).setValue(rsRecords.getString("NoCuenta"));
                    containerPartida.getContainerProperty(itemId, DESCRIPCION_PARTIDA_PROPERTY).setValue(rsRecords.getString("N5"));
                    containerPartida.getContainerProperty(itemId, DEBE_PROPERTY).setValue(numberFormat.format(rsRecords.getDouble("Debe")));
                    containerPartida.getContainerProperty(itemId, HABER_PROPERTY).setValue(numberFormat.format(rsRecords.getDouble("Haber")));
                    containerPartida.getContainerProperty(itemId, DEBEQ_PROPERTY).setValue(numberFormat.format(rsRecords.getDouble("DebeQuetzales")));
                    containerPartida.getContainerProperty(itemId, HABERQ_PROPERTY).setValue(numberFormat.format(rsRecords.getDouble("HaberQuetzales")));

                    totalDebe = totalDebe + rsRecords.getDouble("Debe");
                    totalHaber = totalHaber + rsRecords.getDouble("Haber");
                totalDebeQ = totalDebeQ + rsRecords.getDouble("DebeQuetzales");
                    totalHaberQ = totalHaberQ + rsRecords.getDouble("HaberQuetzales");

                } while (rsRecords.next());

                partidaFooter.getCell(DEBE_PROPERTY).setText(numberFormat.format(totalDebe));
                partidaFooter.getCell(HABER_PROPERTY).setText(numberFormat.format(totalHaber));
                partidaFooter.getCell(DEBEQ_PROPERTY).setText(numberFormat.format(totalDebeQ));
                partidaFooter.getCell(HABERQ_PROPERTY).setText(numberFormat.format(totalHaberQ));

            }
        } catch (Exception ex) {
            System.out.println("Error al listar tabla PARTIDA:" + ex);
            ex.printStackTrace();
        }
    }

    private void setTotal() {
        BigDecimal total = new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP);
        for (Object rid : ingresoBancosGrid.getContainerDataSource()
                .getItemIds()) {
            total = total.add(new BigDecimal(
                    Double.parseDouble(
                            String.valueOf(container.getContainerProperty(rid, MONTOSF_PROPERTY).getValue())
                    )));
        }
        ingresosFooter.getCell(MONEDA_PROPERTY).setText(String.valueOf(container.size()) + " TRANSACCIONES");
        ingresosFooter.getCell(MONTO_PROPERTY).setText(numberFormat.format(total));
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - MOVIMIENTO BANCOS");
    }
}
