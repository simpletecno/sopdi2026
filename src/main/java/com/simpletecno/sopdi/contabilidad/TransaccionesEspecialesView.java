package com.simpletecno.sopdi.contabilidad;

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
 * @author user
 */
public class TransaccionesEspecialesView extends VerticalLayout implements View {

    UI mainUI;
    Statement stQuery;
    ResultSet rsRecords;
    Statement stQuery1;
    ResultSet rsRecords2;
    String queryString;
    Double totalHaber;
    Double totalDebe;

    DateField inicioDt;
    DateField finDt;
    Button consultarBtn;
    Button editarBtn;
    Button revisarBtn;

    Grid transaccionesEspecialesGrid;
    Grid.FooterRow transaccionesFooter;

    public IndexedContainer container = new IndexedContainer();
    static final String ID_PROPERTY = "Partida";
    static final String FECHA_PROPERTY = "Fecha";
    static final String ID_EMPRESA_PROPERTY = "Empresa";
    static final String DESCRIPCION_PROPERTY = "Descripción";
    static final String MONEDA_PROPERTY = "Moneda";
    static final String MONTO_PROPERTY = "Monto";
    static final String ESTATUS_PROPERTY = "Estatus";
    static final String TIPODOC_PROPERTY = "Tipo Documento";
    static final String MONTOSF_PROPERTY = "MSF";

    Grid partidaDocumentosGrid;
    public IndexedContainer containerPartida = new IndexedContainer();
            static final String ID_PARTIDA_PROPERTY = "Id";
    static final String CUENTA_PROPERTY = "Cuenta";
    static final String DESCRIPCION_PARTIDA_PROPERTY = "Descripción";
    static final String DEBE_PROPERTY = "Debe";
    static final String HABER_PROPERTY = "Haber";
    static final String TASACAMBIO_PROPERTY = "Tasa cambio";
    static final String DEBEQ_PROPERTY = "Debe Q.";
    static final String HABERQ_PROPERTY = "Haber Q.";
    Grid.FooterRow partidaFooter;

    VerticalLayout reportLayoutPartida = new VerticalLayout();
    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public TransaccionesEspecialesView() {
        this.mainUI = UI.getCurrent();
        setWidth("100%");
        setSpacing(true);
        //setMargin(true);
        reportLayoutPartida.setEnabled(false);

        Label titleLbl = new Label(empresaId + " " + empresaNombre + " TRANSACCIONES ESPECIALES");
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

        container.addContainerProperty(ID_PROPERTY, String.class, null);
        container.addContainerProperty(TIPODOC_PROPERTY, String.class, null);
        container.addContainerProperty(FECHA_PROPERTY, String.class, null);
        container.addContainerProperty(ID_EMPRESA_PROPERTY, String.class, null);
        container.addContainerProperty(DESCRIPCION_PROPERTY, String.class, null);
        container.addContainerProperty(MONEDA_PROPERTY, String.class, null);
        container.addContainerProperty(MONTO_PROPERTY, String.class, null);
        container.addContainerProperty(ESTATUS_PROPERTY, String.class, null);
        container.addContainerProperty(MONTOSF_PROPERTY, String.class, null);
        container.addContainerProperty(DESCRIPCION_PROPERTY, String.class, null);

        transaccionesEspecialesGrid = new Grid(container);
        transaccionesEspecialesGrid.setWidth("100%");
        transaccionesEspecialesGrid.setImmediate(true);
        transaccionesEspecialesGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        transaccionesEspecialesGrid.setDescription("Seleccione un registro.");
        transaccionesEspecialesGrid.setHeightMode(HeightMode.ROW);
        transaccionesEspecialesGrid.setHeightByRows(5);

        transaccionesEspecialesGrid.getColumn(MONTOSF_PROPERTY).setHidable(true).setHidden(true);
        transaccionesEspecialesGrid.getColumn(TIPODOC_PROPERTY).setHidable(true);

        transaccionesEspecialesGrid.setResponsive(true);
        transaccionesEspecialesGrid.setEditorBuffered(false);

        transaccionesFooter = transaccionesEspecialesGrid.appendFooterRow();
        transaccionesFooter.getCell(MONEDA_PROPERTY).setText("0 TRANSACCIONES");
        transaccionesFooter.getCell(MONTO_PROPERTY).setText("0.00");

        reportLayout.addComponent(filtrosLayout);
        reportLayout.setComponentAlignment(filtrosLayout, Alignment.MIDDLE_CENTER);
        reportLayout.addComponent(transaccionesEspecialesGrid);
        reportLayout.setComponentAlignment(transaccionesEspecialesGrid, Alignment.MIDDLE_CENTER);

        transaccionesEspecialesGrid.getColumn(DESCRIPCION_PROPERTY).setHidable(true).setHidden(true);
        transaccionesEspecialesGrid.getColumn(ID_EMPRESA_PROPERTY).setHidable(true).setHidden(true);

        transaccionesEspecialesGrid.setCellStyleGenerator(cellRef
                -> MONTO_PROPERTY.equals(cellRef.getPropertyId())
                ? "rightalign" : null);

        transaccionesEspecialesGrid.addSelectionListener(new SelectionListener() {
            @Override
            public void select(SelectionEvent event) {
                if (transaccionesEspecialesGrid.getSelectedRow() != null) {
                    llenarTablaPartida(String.valueOf(transaccionesEspecialesGrid.getContainerDataSource().getItem(transaccionesEspecialesGrid.getSelectedRow()).getItemProperty(ID_PROPERTY).getValue()));
                    System.out.println("Seleccionado" + String.valueOf(transaccionesEspecialesGrid.getContainerDataSource().getItem(transaccionesEspecialesGrid.getSelectedRow()).getItemProperty(ID_PROPERTY).getValue()));
                }
            }

        });

        Grid.HeaderRow filterRow = transaccionesEspecialesGrid.appendHeaderRow();

        Grid.HeaderCell cell = filterRow.getCell(DESCRIPCION_PROPERTY);

        TextField filterField = new TextField();
        filterField.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField.setInputPrompt("Filtrar");
        filterField.setColumns(12);

        filterField.addTextChangeListener(change -> {
            container.removeContainerFilters(DESCRIPCION_PROPERTY);

            // (Re)create the filter if necessary
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

            // (Re)create the filter if necessary
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

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(
                        new SimpleStringFilter(MONEDA_PROPERTY,
                                change.getText(), true, false));
            }
            setTotal();
        });

        cell3.setComponent(filterField3);

        Grid.HeaderCell cell4 = filterRow.getCell(ESTATUS_PROPERTY);

        TextField filterField4 = new TextField();
        filterField4.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField4.setInputPrompt("Filtrar");
        filterField4.setColumns(12);

        filterField4.addTextChangeListener(change -> {
            container.removeContainerFilters(ESTATUS_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(
                        new SimpleStringFilter(ESTATUS_PROPERTY,
                                change.getText(), true, false));
            }
            setTotal();
        });

        cell4.setComponent(filterField4);

        Button nuevoBtn = new Button("Nueva transacción");
        nuevoBtn.setIcon(FontAwesome.PLUS);
        nuevoBtn.setDescription("Agregar nueva transacción.");
        nuevoBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                TransaccionesEspecialesForm nuevaTransaccion =
                        new TransaccionesEspecialesForm(
                                empresaId,
                                "",
                                "TRANSACCION ESPECIAL",
                                1
                        );
                UI.getCurrent().addWindow(nuevaTransaccion);
                nuevaTransaccion.center();
                // nuevaTransaccion.llenarDatos();
            }
        });

        filtrosLayout.addComponents(inicioDt, finDt, consultarBtn, nuevoBtn);
        filtrosLayout.setComponentAlignment(inicioDt, Alignment.MIDDLE_CENTER);
        filtrosLayout.setComponentAlignment(finDt, Alignment.MIDDLE_CENTER);
        filtrosLayout.setComponentAlignment(consultarBtn, Alignment.BOTTOM_RIGHT);
        filtrosLayout.setComponentAlignment(nuevoBtn, Alignment.BOTTOM_RIGHT);

        addComponent(reportLayout);
        setComponentAlignment(reportLayout, Alignment.MIDDLE_CENTER);
    }

    public void llenarTablaFactura(String empresa) {
        container.removeAllItems();
        containerPartida.removeAllItems();

        setTotal();

        queryString = " SELECT *, SUM(contabilidad_partida.Debe) AS Total";
        queryString += " FROM contabilidad_partida";
        queryString += " WHERE contabilidad_partida.Fecha BETWEEN ";
        queryString += " '" + Utileria.getFechaYYYYMMDD_1(inicioDt.getValue()) + "'";
        queryString += " AND '" + Utileria.getFechaYYYYMMDD_1(finDt.getValue()) + "'";
        queryString += " AND (contabilidad_partida.TipoDocumento = 'TRANSACCION ESPECIAL' OR ";
        queryString += " contabilidad_partida.TipoDocumento = 'CONSTANCIA ISR VENTA')";
        queryString += " AND IdEmpresa = " + empresa;
        queryString += " GROUP BY CodigoPartida, Fecha, IdEmpresa";
        queryString += " ORDER BY CodigoPartida, Fecha, IdEmpresa";

        System.out.println("query " + queryString); //213107201860131
//                                           213107201860131

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado        
                do {
                    Object itemId = container.addItem();

                    container.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords.getString("CodigoPartida"));
                    container.getContainerProperty(itemId, FECHA_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha")));
                    container.getContainerProperty(itemId, ID_EMPRESA_PROPERTY).setValue(rsRecords.getString("IdEmpresa"));
                    container.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(rsRecords.getString("Descripcion"));
                    container.getContainerProperty(itemId, MONEDA_PROPERTY).setValue(rsRecords.getString("MonedaDocumento"));
                    if (rsRecords.getString("MonedaDocumento").equals("QUETZALES")) {
                        container.getContainerProperty(itemId, MONTO_PROPERTY).setValue("Q." + numberFormat.format(rsRecords.getDouble("Total")));
                    } else {
                        container.getContainerProperty(itemId, MONTO_PROPERTY).setValue("$." + numberFormat.format(rsRecords.getDouble("Total")));
                    }
                    container.getContainerProperty(itemId, ESTATUS_PROPERTY).setValue(rsRecords.getString("Estatus"));
                    container.getContainerProperty(itemId, TIPODOC_PROPERTY).setValue(rsRecords.getString("TipoDocumento"));
                    container.getContainerProperty(itemId, MONTOSF_PROPERTY).setValue(String.valueOf(rsRecords.getDouble("Total")));
                    container.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(rsRecords.getString("Descripcion"));
                } while (rsRecords.next());

                setTotal();
            }
        } catch (Exception ex) {
            System.out.println("Error al listar tabla transacciones especiales:" + ex);
            ex.printStackTrace();
        }
    }

    public void createTablaPartida() {
        reportLayoutPartida.setWidth("95%");
        reportLayoutPartida.addStyleName("rcorners3");

        HorizontalLayout camposPartidaLayout = new HorizontalLayout();
        camposPartidaLayout.setSpacing(true);
        camposPartidaLayout.setWidth("60%");

        containerPartida.addContainerProperty(ID_PARTIDA_PROPERTY, String.class, "");
        containerPartida.addContainerProperty(CUENTA_PROPERTY, String.class, "");
        containerPartida.addContainerProperty(DESCRIPCION_PARTIDA_PROPERTY, String.class, "");
        containerPartida.addContainerProperty(DEBE_PROPERTY, String.class, "");
        containerPartida.addContainerProperty(HABER_PROPERTY, String.class, "");
        containerPartida.addContainerProperty(TASACAMBIO_PROPERTY, String.class, "");
        containerPartida.addContainerProperty(DEBEQ_PROPERTY, String.class, "");
        containerPartida.addContainerProperty(HABERQ_PROPERTY, String.class, "");

        partidaDocumentosGrid = new Grid("Partida contable", containerPartida);

        partidaDocumentosGrid.setImmediate(true);
        partidaDocumentosGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        partidaDocumentosGrid.setDescription("Seleccione un registro.");
        partidaDocumentosGrid.setHeightMode(HeightMode.ROW);
        partidaDocumentosGrid.setHeightByRows(3);

        partidaDocumentosGrid.setWidth("100%");
        partidaDocumentosGrid.setResponsive(true);
        partidaDocumentosGrid.setEditorBuffered(false);

        partidaDocumentosGrid.getColumn(ID_PARTIDA_PROPERTY).setHidable(true).setHidden(true);
        partidaDocumentosGrid.getColumn(CUENTA_PROPERTY).setExpandRatio(1);
        partidaDocumentosGrid.getColumn(DESCRIPCION_PROPERTY).setExpandRatio(2);
        partidaDocumentosGrid.getColumn(DEBE_PROPERTY).setExpandRatio(1);
        partidaDocumentosGrid.getColumn(HABER_PROPERTY).setExpandRatio(1);
        partidaDocumentosGrid.getColumn(DEBEQ_PROPERTY).setExpandRatio(1);
        partidaDocumentosGrid.getColumn(HABERQ_PROPERTY).setExpandRatio(1);
        partidaDocumentosGrid.getColumn(TASACAMBIO_PROPERTY).setExpandRatio(1);

        partidaDocumentosGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {

//            System.out.println("celda=" + cellReference.getProperty());
            if (DEBE_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (HABER_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (DEBEQ_PROPERTY.equals(cellReference.getPropertyId())) {
                    return "rightalign";
            } else if (HABERQ_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (TASACAMBIO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else {
                return null;
            }

        });

        partidaFooter = partidaDocumentosGrid.appendFooterRow();
        partidaFooter.getCell(DESCRIPCION_PROPERTY).setText("SUMAS IGUALES");
        partidaFooter.getCell(DEBE_PROPERTY).setText("0.00");
        partidaFooter.getCell(HABER_PROPERTY).setText("0.00");
        partidaFooter.getCell(DESCRIPCION_PROPERTY).setStyleName("rightalign");
        partidaFooter.getCell(DEBE_PROPERTY).setStyleName("rightalign");
        partidaFooter.getCell(HABER_PROPERTY).setStyleName("rightalign");
        partidaFooter.getCell(DEBEQ_PROPERTY).setStyleName("rightalign");
        partidaFooter.getCell(HABERQ_PROPERTY).setStyleName("rightalign");

        Button editBtn = new Button("Editar");
        editBtn.setIcon(FontAwesome.EDIT);
        editBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        editBtn.setDescription("Actualizar datos");
        editBtn.addListener((Button.ClickListener) (Button.ClickEvent event) -> {

            if (transaccionesEspecialesGrid.getSelectedRow() == null) {
                Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
            } else {
                if (String.valueOf(container.getContainerProperty(transaccionesEspecialesGrid.getSelectedRow(), ESTATUS_PROPERTY).getValue()).equals("REVISADO")) {
                    Notification.show("No se puede editar un documento ya REVISADO .", Notification.Type.WARNING_MESSAGE);

                } else {
                    TransaccionesEspecialesForm editTransaccion
                            = new TransaccionesEspecialesForm(
                            String.valueOf(empresaId),
                            String.valueOf(container.getContainerProperty(transaccionesEspecialesGrid.getSelectedRow(), ID_PROPERTY).getValue()),
                            String.valueOf(container.getContainerProperty(transaccionesEspecialesGrid.getSelectedRow(), TIPODOC_PROPERTY).getValue()),
                            2
                    );
                    //editTransaccion.cuentaContable1Cbx.focus();
                    UI.getCurrent().addWindow(editTransaccion);
                    editTransaccion.center();
                }
            }
        });

        Button printBtn = new Button("Imprimir partida");
        printBtn.setIcon(FontAwesome.FILE_PDF_O);
        printBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        printBtn.setDescription("Imprimir partida");
        printBtn.addListener((Button.ClickListener) (Button.ClickEvent event) -> {

            if (transaccionesEspecialesGrid.getSelectedRow() == null) {
                Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
            } else {
                LibroDiarioView libroDiario = new LibroDiarioView();
                libroDiario.documentoTxt.setValue(String.valueOf(container.getContainerProperty(transaccionesEspecialesGrid.getSelectedRow(), ID_PROPERTY).getValue()));
                libroDiario.empresaId = empresaId;
                try {
                    libroDiario.inicioDt.setValue(
                            new SimpleDateFormat("dd/MM/yyyy")
                                    .parse(String.valueOf(container.getContainerProperty(transaccionesEspecialesGrid.getSelectedRow(), FECHA_PROPERTY).getValue())));
                    libroDiario.finDt.setValue(
                            new SimpleDateFormat("dd/MM/yyyy")
                                    .parse(String.valueOf(container.getContainerProperty(transaccionesEspecialesGrid.getSelectedRow(), FECHA_PROPERTY).getValue())));
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

            if (transaccionesEspecialesGrid.getSelectedRow() == null) {
                Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
            } else {

                if (String.valueOf(container.getContainerProperty(transaccionesEspecialesGrid.getSelectedRow(), ESTATUS_PROPERTY).getValue()).equals("REVISADO")) {
                    Notification.show("Documento ya REVISADO.", Notification.Type.WARNING_MESSAGE);
                } else {

                    try {

                        queryString = "UPDATE  contabilidad_partida";
                        queryString += " SET Estatus = 'REVISADO'";
                        queryString += " WHERE CodigoPartida = '" + String.valueOf(container.getContainerProperty(transaccionesEspecialesGrid.getSelectedRow(), ID_PROPERTY).getValue()) + "'";

                        stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                        stQuery.executeUpdate(queryString);

                        container.getContainerProperty(transaccionesEspecialesGrid.getSelectedRow(), ESTATUS_PROPERTY).setValue("REVISADO");

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

        reportLayoutPartida.addComponent(partidaDocumentosGrid);
        reportLayoutPartida.addComponent(camposPartidaLayout);
        reportLayoutPartida.setComponentAlignment(camposPartidaLayout, Alignment.BOTTOM_CENTER);
        addComponent(reportLayoutPartida);
        setComponentAlignment(reportLayoutPartida, Alignment.MIDDLE_CENTER);
    }

    public void llenarTablaPartida(String codigoPartida) {
        partidaFooter.getCell(DEBE_PROPERTY).setText("0.00");
        partidaFooter.getCell(HABER_PROPERTY).setText("0.00");
        containerPartida.removeAllItems();
        reportLayoutPartida.setEnabled(true);

        totalDebe = 0.00;
        totalHaber = 0.00;
        double totalDebeQ = 0.00;
        double totalHaberQ = 0.00;

        queryString = "";
        queryString = " SELECT contabilidad_partida.*,contabilidad_nomenclatura_emprewsa.N5, contabilidad_nomenclatura_emprewsa.NoCuenta";
        queryString += " FROM contabilidad_partida,contabilidad_nomenclatura_emprewsa";
        queryString += " WHERE contabilidad_partida.CodigoPartida = '" + codigoPartida + "'";
        queryString += " AND contabilidad_nomenclatura_emprewsa.IdNomenclatura = contabilidad_partida.IdNomenclatura";
        queryString += " AND contabilidad_nomenclatura_emprewsa.IdEmpresa = " + empresaId;

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);
            Object itemId = 0;
            String descripcion = "";
            if (rsRecords.next()) { //  encontrado                                                
                do {
                     itemId = containerPartida.addItem();

                    if (rsRecords.getDouble("Debe") > 0.00 || rsRecords.getDouble("DebeQuetzales") > 0.00) {
                        if (rsRecords.getString("MonedaDocumento").equals("QUETZALES")) {
                            containerPartida.getContainerProperty(itemId, DEBE_PROPERTY).setValue("Q." + numberFormat.format(rsRecords.getDouble("Debe")));
                        } else {
                            containerPartida.getContainerProperty(itemId, DEBE_PROPERTY).setValue("$." + numberFormat.format(rsRecords.getDouble("Debe")));
                        }
                    } else {
                        containerPartida.getContainerProperty(itemId, DEBE_PROPERTY).setValue("");
                    }
                    if (rsRecords.getDouble("Haber") > 0.00 || rsRecords.getDouble("HaberQuetzales") > 0.00) {
                        if (rsRecords.getString("MonedaDocumento").equals("QUETZALES")) {
                            containerPartida.getContainerProperty(itemId, HABER_PROPERTY).setValue("Q." + numberFormat.format(rsRecords.getDouble("Haber")));
                        } else {
                            containerPartida.getContainerProperty(itemId, HABER_PROPERTY).setValue("$." + numberFormat.format(rsRecords.getDouble("Haber")));
                        }

                    } else {
                        containerPartida.getContainerProperty(itemId, HABER_PROPERTY).setValue("");
                    }
                    containerPartida.getContainerProperty(itemId, TASACAMBIO_PROPERTY).setValue(rsRecords.getString("TipoCambio"));
                    containerPartida.getContainerProperty(itemId, DEBEQ_PROPERTY).setValue("Q." + numberFormat.format(rsRecords.getDouble("DebeQuetzales")));
                    containerPartida.getContainerProperty(itemId, HABERQ_PROPERTY).setValue("Q." + numberFormat.format(rsRecords.getDouble("HaberQuetzales")));
                    containerPartida.getContainerProperty(itemId, ID_PARTIDA_PROPERTY).setValue(rsRecords.getString("IdPartida"));
                    containerPartida.getContainerProperty(itemId, CUENTA_PROPERTY).setValue(rsRecords.getString("NoCuenta"));
                    containerPartida.getContainerProperty(itemId, DESCRIPCION_PARTIDA_PROPERTY).setValue(rsRecords.getString("N5"));

                    totalDebe = totalDebe + rsRecords.getDouble("Debe");
                    totalHaber = totalHaber + rsRecords.getDouble("Haber");

                    totalDebeQ = totalDebeQ   + rsRecords.getDouble("DebeQuetzales");
                    totalHaberQ = totalHaberQ + rsRecords.getDouble("HaberQuetzales");

                    descripcion = rsRecords.getString("Descripcion");

                } while (rsRecords.next());

                itemId = containerPartida.addItem();
                containerPartida.getContainerProperty(itemId, DESCRIPCION_PARTIDA_PROPERTY).setValue(descripcion);

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
        for (Object rid : transaccionesEspecialesGrid.getContainerDataSource()
                .getItemIds()) {
            total = total.add(new BigDecimal(
                    Double.parseDouble(
                            String.valueOf(container.getContainerProperty(rid, MONTOSF_PROPERTY).getValue())
                    )));
        }
        transaccionesFooter.getCell(MONEDA_PROPERTY).setText(String.valueOf(container.size()) + " TRANSACCIONES");
        transaccionesFooter.getCell(MONTO_PROPERTY).setText(numberFormat.format(total));
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - Transacciones especiales");
    }
}
