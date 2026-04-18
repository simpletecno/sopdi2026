package com.simpletecno.sopdi.tesoreria;

import com.simpletecno.sopdi.contabilidad.MostrarPartidaContable;
import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.addon.tableexport.DefaultTableHolder;
import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.addon.tableexport.TableHolder;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.HeaderCell;
import com.vaadin.ui.Grid.HeaderRow;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.themes.ValoTheme;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author user
 */
public class HistorialPagoView extends VerticalLayout implements View {

    double totalMonto;
    double totalQueztales;

    static final String ID_PROPERTY = "Id";
    static final String FECHA_PROPERTY = "Fecha";
    static final String TIPO_PROPERTY = "Tipo";
    static final String CUENTA_PROPERTY = "Cuenta";
    static final String DESCRIPCION_PROPERTY = "Descripcion";
    static final String DOCUMENTO_PROPERTY = "Documento";
    static final String PROVEEDOR_PROPERTY = "Proveedor";
    static final String NOMBRECHEQUE_PROPERTY = "Nombre cheque";
    static final String DOCA_PROPERTY = "DOCA";
    static final String MONEDA_PROPERTY = "Moneda";
    static final String VALOR_PROPERTY = "Monto";
    static final String TIPOCAMBIO_PROPERTY = "Tasa";
    static final String MONTO_QUETZALES_PROPERTY = "Monto Q.";
    static final String USUARIO_PROPERTY = "Usuario";

    static final String ESTATUS_PROPERTY = "Estatus";
    static final String CODIGOPARTIDA_PROPERTY = "Partida";

    static final String ID_NOMENCLATURA_PROPERTY = "Id Nomenclatura";
    static final String ID_PROVEEDOR_PROPERTY = "Id Proveedor";

    Grid historialPagosGrid;
    Grid.FooterRow footerHistorial;

    public IndexedContainer historialPagosContainer = new IndexedContainer();

    DateField inicioDt;
    DateField finDt;

    Button excelBtn;

    UI mainUI;
    Statement stQuery;
    ResultSet rsRecords;
    Statement stQuery1;
    ResultSet rsRecords2;

    Statement stQueryHistorial;
    ResultSet rsRecordsHistorial;

    String queryString;

    String filePath = "";
    FileDownloader fileDownloader = null;

    Button descargarTxt;

    int contador = 0;

    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public HistorialPagoView() {

        this.mainUI = UI.getCurrent();

        setWidth("100%");
        setMargin(false);
        setSpacing(true);

        excelBtn = new Button("Excel");
        excelBtn.setIcon(FontAwesome.FILE_EXCEL_O);
        excelBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        excelBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (historialPagosContainer.size() > 0) {
                    exportToExcel();
                } else {
                    Notification.show("La vista no contiene registros disponibles.. ", Notification.Type.WARNING_MESSAGE);
                }
            }
        });

        Label titleLbl = new Label(empresaId + " " + empresaNombre + " HISTORIAL DE PAGOS");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setResponsive(true);
        titleLayout.setSpacing(true);
        titleLayout.setWidth("100%");
        titleLayout.setMargin(false);
        titleLayout.addComponents(titleLbl, excelBtn);
        titleLayout.setComponentAlignment(titleLbl, Alignment.MIDDLE_CENTER);
        titleLayout.setComponentAlignment(excelBtn, Alignment.MIDDLE_CENTER);
        titleLayout.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

        addComponent(titleLayout);
        setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        agregarHistorialPagos();
        llenarGridHistorialPagos();
    }

    public void agregarHistorialPagos() {

        VerticalLayout historialPagosLayout = new VerticalLayout();
        historialPagosLayout.addStyleName("rcorners3");
        historialPagosLayout.setWidth("100%");
        historialPagosLayout.setResponsive(true);
        historialPagosLayout.setSpacing(true);
        historialPagosLayout.setMargin(false);

        HorizontalLayout filtrosLayout = new HorizontalLayout();
        filtrosLayout.setSpacing(true);
        filtrosLayout.setMargin(false);

        inicioDt = new DateField("Desde:");
        inicioDt.setDateFormat("dd/MM/yyyy");
        Date primerDia = Utileria.getPrimerDiaDelMes();
        inicioDt.setValue(primerDia);
        inicioDt.setWidth("9em");

        finDt = new DateField("Hasta:");
        finDt.setDateFormat("dd/MM/yyyy");
        Date ultimoDia = Utileria.getUltimoDiaDelMes();
        finDt.setValue(ultimoDia);
        finDt.setWidth("9em");

        Button consultarBtn;
        consultarBtn = new Button("Consultar");
        consultarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                llenarGridHistorialPagos();
            }
        });

        descargarTxt = new Button("Click para descargar txt.");
        descargarTxt.setIcon(FontAwesome.DOWNLOAD);
        descargarTxt.addClickListener(listener -> {
            contador = contador + 1;
            
            if (!filePath.isEmpty()) {
                File file = new File(filePath);
                if (file.exists()) {
                    file.delete();
                }
            }
            llenarDatosArhivo();

            if (fileDownloader == null) {
                fileDownloader = new FileDownloader(getStream(new File(filePath)));
            }

            fileDownloader.extend(descargarTxt);

            if (contador == 1) {
                Notification notif = new Notification("ARCHIVO GENERADO, POR FAVOR VUELVA A HACER CLICK PARA DESCARGARLO..", Notification.Type.HUMANIZED_MESSAGE);
                notif.setDelayMsec(1500);
                notif.setPosition(Position.MIDDLE_CENTER);               
                notif.show(Page.getCurrent());
            }
        });

        filtrosLayout.addComponents(inicioDt, finDt, consultarBtn, descargarTxt);
        filtrosLayout.setComponentAlignment(inicioDt, Alignment.BOTTOM_CENTER);
        filtrosLayout.setComponentAlignment(finDt, Alignment.BOTTOM_CENTER);
        filtrosLayout.setComponentAlignment(consultarBtn, Alignment.BOTTOM_CENTER);
        filtrosLayout.setComponentAlignment(descargarTxt, Alignment.BOTTOM_CENTER);

        historialPagosLayout.addComponent(filtrosLayout);
        historialPagosLayout.setComponentAlignment(filtrosLayout, Alignment.TOP_CENTER);

        historialPagosContainer.addContainerProperty(ID_PROPERTY, String.class, null);
        historialPagosContainer.addContainerProperty(CODIGOPARTIDA_PROPERTY, String.class, null);
        historialPagosContainer.addContainerProperty(FECHA_PROPERTY, String.class, null);
        historialPagosContainer.addContainerProperty(DESCRIPCION_PROPERTY, String.class, null);
        historialPagosContainer.addContainerProperty(TIPO_PROPERTY, String.class, null);
        historialPagosContainer.addContainerProperty(DOCUMENTO_PROPERTY, String.class, null);
        historialPagosContainer.addContainerProperty(MONEDA_PROPERTY, String.class, null);
        historialPagosContainer.addContainerProperty(VALOR_PROPERTY, String.class, null);
        historialPagosContainer.addContainerProperty(TIPOCAMBIO_PROPERTY, String.class, null);
        historialPagosContainer.addContainerProperty(MONTO_QUETZALES_PROPERTY, String.class, null);
        historialPagosContainer.addContainerProperty(ESTATUS_PROPERTY, String.class, null);
        historialPagosContainer.addContainerProperty(CUENTA_PROPERTY, String.class, null);
        historialPagosContainer.addContainerProperty(PROVEEDOR_PROPERTY, String.class, null);
        historialPagosContainer.addContainerProperty(DOCA_PROPERTY, String.class, null);
        historialPagosContainer.addContainerProperty(USUARIO_PROPERTY, String.class, null);
        historialPagosContainer.addContainerProperty(NOMBRECHEQUE_PROPERTY, String.class, null);
        historialPagosContainer.addContainerProperty(ID_NOMENCLATURA_PROPERTY, String.class, null);
        historialPagosContainer.addContainerProperty(ID_PROVEEDOR_PROPERTY, String.class, null);

        historialPagosGrid = new Grid(historialPagosContainer);
        historialPagosGrid.setWidth("100%");
        historialPagosGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        historialPagosGrid.setHeightMode(HeightMode.ROW);
        historialPagosGrid.setHeight("100%");
        historialPagosGrid.setResponsive(true);

        historialPagosGrid.getColumn(DESCRIPCION_PROPERTY).setHidable(true).setHidden(true);
        historialPagosGrid.getColumn(ID_PROPERTY).setHidable(true).setHidden(true);
        historialPagosGrid.getColumn(CUENTA_PROPERTY).setHidable(true);
        historialPagosGrid.getColumn(USUARIO_PROPERTY).setHidable(true).setHidden(true);
        historialPagosGrid.getColumn(ID_NOMENCLATURA_PROPERTY).setHidable(true).setHidden(true);
        historialPagosGrid.getColumn(ID_PROVEEDOR_PROPERTY).setHidable(true).setHidden(true);
        historialPagosGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {

            if (MONTO_QUETZALES_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (VALOR_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (TIPOCAMBIO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else {
                return null;
            }

        });

        historialPagosGrid.getColumn(ID_PROPERTY).setExpandRatio(1).setHidden(true);
        historialPagosGrid.getColumn(CODIGOPARTIDA_PROPERTY).setExpandRatio(3);
        historialPagosGrid.getColumn(FECHA_PROPERTY).setExpandRatio(2);
        historialPagosGrid.getColumn(DESCRIPCION_PROPERTY).setExpandRatio(2);
        historialPagosGrid.getColumn(TIPO_PROPERTY).setExpandRatio(1);
        historialPagosGrid.getColumn(DOCUMENTO_PROPERTY).setExpandRatio(1);
        historialPagosGrid.getColumn(CUENTA_PROPERTY).setExpandRatio(1).setHidden(true);
        historialPagosGrid.getColumn(PROVEEDOR_PROPERTY).setExpandRatio(1);
        historialPagosGrid.getColumn(MONEDA_PROPERTY).setExpandRatio(2);
        historialPagosGrid.getColumn(VALOR_PROPERTY).setExpandRatio(2);
        historialPagosGrid.getColumn(TIPOCAMBIO_PROPERTY).setExpandRatio(1);
        historialPagosGrid.getColumn(MONTO_QUETZALES_PROPERTY).setExpandRatio(2);
        historialPagosGrid.getColumn(ESTATUS_PROPERTY).setExpandRatio(2);
        historialPagosGrid.getColumn(DOCA_PROPERTY).setExpandRatio(2);
        historialPagosGrid.getColumn(USUARIO_PROPERTY).setExpandRatio(1);
        historialPagosGrid.getColumn(NOMBRECHEQUE_PROPERTY).setExpandRatio(1);

        historialPagosGrid.getColumn(CODIGOPARTIDA_PROPERTY).setRenderer(new ButtonRenderer(e -> {
            String codigoPartida = String.valueOf(historialPagosContainer.getContainerProperty(e.getItemId(), CODIGOPARTIDA_PROPERTY).getValue());
            String descripcion = String.valueOf(historialPagosContainer.getContainerProperty(e.getItemId(), DESCRIPCION_PROPERTY).getValue());
            String nombre = String.valueOf(historialPagosContainer.getContainerProperty(e.getItemId(), PROVEEDOR_PROPERTY).getValue());
            String tipo = String.valueOf(historialPagosContainer.getContainerProperty(e.getItemId(), TIPO_PROPERTY).getValue());
            String documento = String.valueOf(historialPagosContainer.getContainerProperty(e.getItemId(), DOCUMENTO_PROPERTY).getValue());

            MostrarPartidaContable mostrarPartidaContable
                    = new MostrarPartidaContable(
                            codigoPartida,
                            descripcion,
                            nombre,
                            tipo + " " + documento
                    );
            mainUI.addWindow(mostrarPartidaContable);
            mostrarPartidaContable.center();
        }));

        historialPagosGrid.getColumn(DOCUMENTO_PROPERTY).setRenderer(new ButtonRenderer(e -> {
            if (String.valueOf(historialPagosContainer.getContainerProperty(e.getItemId(), TIPO_PROPERTY).getValue()).toUpperCase().equals("CHEQUE") || String.valueOf(historialPagosContainer.getContainerProperty(e.getItemId(), TIPO_PROPERTY).getValue()).toUpperCase().equals("NOTA DE DEBITO")) {
                String codigoPartida = String.valueOf(historialPagosContainer.getContainerProperty(e.getItemId(), CODIGOPARTIDA_PROPERTY).getValue());
                String descripcion = String.valueOf(historialPagosContainer.getContainerProperty(e.getItemId(), DESCRIPCION_PROPERTY).getValue());
                String nombre = String.valueOf(historialPagosContainer.getContainerProperty(e.getItemId(), NOMBRECHEQUE_PROPERTY).getValue());
                String documento = String.valueOf(historialPagosContainer.getContainerProperty(e.getItemId(), DOCUMENTO_PROPERTY).getValue());
                String montoDocumento = String.valueOf(historialPagosContainer.getContainerProperty(e.getItemId(), VALOR_PROPERTY).getValue());

                PagoChequesPDF Pagocheques
                        = new PagoChequesPDF(
                                empresaId,
                                empresaNombre,
                                codigoPartida,
                                "0",
                                nombre,
                                documento,
                                descripcion,
                                montoDocumento.replace(",", "").replace("Q.", "").replace("$.", "")
                        );
                mainUI.addWindow(Pagocheques);
                Pagocheques.center();
            } else {
                Notification.show("SOLAMENTE LOS CHEQUES SE PUEDEN REIMPRIMIR", Notification.Type.HUMANIZED_MESSAGE);
            }

        }));

        historialPagosGrid.getColumn(ESTATUS_PROPERTY).setRenderer(new ButtonRenderer(e -> {

            String estatus = String.valueOf(historialPagosContainer.getContainerProperty(e.getItemId(), ESTATUS_PROPERTY).getValue());

            if (estatus.equals("ANULADO")) {
                Notification.show("CHEQUE ANULADO, NO SE PERMITEN CAMBIOS...", Notification.Type.HUMANIZED_MESSAGE);
                return;
            }
            String codigoPartida = String.valueOf(historialPagosContainer.getContainerProperty(e.getItemId(), CODIGOPARTIDA_PROPERTY).getValue());
            String monto = String.valueOf(historialPagosContainer.getContainerProperty(e.getItemId(), VALOR_PROPERTY).getValue()).replaceAll(",", "");
            String descripcion = String.valueOf(historialPagosContainer.getContainerProperty(e.getItemId(), DESCRIPCION_PROPERTY).getValue()).replaceAll(",", "");
            CambiarEstatusPago cambiarEstatusPago
                    = new CambiarEstatusPago(
                            historialPagosContainer,
                            e.getItemId(),
                            codigoPartida,
                            monto
                    );
            UI.getCurrent().addWindow(cambiarEstatusPago);
            cambiarEstatusPago.center();

        }));

        HeaderRow filterRow = historialPagosGrid.appendHeaderRow();

        HeaderCell cellF = filterRow.getCell(FECHA_PROPERTY);

        TextField filterFieldF = new TextField();
        filterFieldF.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterFieldF.setInputPrompt("Filtrar");
        filterFieldF.setColumns(10);

        filterFieldF.addTextChangeListener(change -> {
            historialPagosContainer.removeContainerFilters(DOCUMENTO_PROPERTY);
            if (!change.getText().isEmpty()) {
                historialPagosContainer.addContainerFilter(
                        new SimpleStringFilter(FECHA_PROPERTY,
                                change.getText(), true, false));
            }
            setTotal();
        });
        cellF.setComponent(filterFieldF);

        HeaderCell cellB0 = filterRow.getCell(TIPO_PROPERTY);

        TextField filterFieldB0 = new TextField();
        filterFieldB0.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterFieldB0.setInputPrompt("Filtrar");
        filterFieldB0.setColumns(8);

        filterFieldB0.addTextChangeListener(change -> {
            historialPagosContainer.removeContainerFilters(TIPO_PROPERTY);
            if (!change.getText().isEmpty()) {
                historialPagosContainer.addContainerFilter(
                        new SimpleStringFilter(TIPO_PROPERTY,
                                change.getText(), true, false));
            }
            setTotal();
        });
        cellB0.setComponent(filterFieldB0);

        HeaderCell cellB = filterRow.getCell(DOCUMENTO_PROPERTY);

        TextField filterFieldB = new TextField();
        filterFieldB.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterFieldB.setInputPrompt("Filtrar");
        filterFieldB.setColumns(8);

        filterFieldB.addTextChangeListener(change -> {
            historialPagosContainer.removeContainerFilters(DOCUMENTO_PROPERTY);
            if (!change.getText().isEmpty()) {
                historialPagosContainer.addContainerFilter(
                        new SimpleStringFilter(DOCUMENTO_PROPERTY,
                                change.getText(), true, false));
            }
            setTotal();
        });
        cellB.setComponent(filterFieldB);

        HeaderCell cellD = filterRow.getCell(DESCRIPCION_PROPERTY);

        TextField filterFieldD = new TextField();
        filterFieldD.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterFieldD.setInputPrompt("Filtrar");
        filterFieldD.setColumns(20);

        filterFieldD.addTextChangeListener(change -> {
            historialPagosContainer.removeContainerFilters(DESCRIPCION_PROPERTY);
            if (!change.getText().isEmpty()) {
                historialPagosContainer.addContainerFilter(
                        new SimpleStringFilter(DESCRIPCION_PROPERTY,
                                change.getText(), true, false));
            }
            setTotal();
        });
        cellD.setComponent(filterFieldD);

        HeaderCell cell1 = filterRow.getCell(MONEDA_PROPERTY);

        TextField filterField1 = new TextField();
        filterField1.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField1.setInputPrompt("Filtrar");
        filterField1.setColumns(8);

        filterField1.addTextChangeListener(change -> {
            historialPagosContainer.removeContainerFilters(MONEDA_PROPERTY);
            if (!change.getText().isEmpty()) {
                historialPagosContainer.addContainerFilter(
                        new SimpleStringFilter(MONEDA_PROPERTY,
                                change.getText(), true, false));
            }
            setTotal();
        });
        cell1.setComponent(filterField1);

        HeaderCell cell2 = filterRow.getCell(PROVEEDOR_PROPERTY);

        TextField filterField2 = new TextField();
        filterField2.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField2.setInputPrompt("Filtrar");
        filterField2.setColumns(15);

        filterField2.addTextChangeListener(change -> {
            historialPagosContainer.removeContainerFilters(PROVEEDOR_PROPERTY);
            if (!change.getText().isEmpty()) {
                historialPagosContainer.addContainerFilter(
                        new SimpleStringFilter(PROVEEDOR_PROPERTY,
                                change.getText(), true, false));
            }
            setTotal();
        });
        cell2.setComponent(filterField2);

        HeaderCell cell3 = filterRow.getCell(DOCA_PROPERTY);

        TextField filterField3 = new TextField();
        filterField3.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField3.setInputPrompt("Filtrar");
        filterField3.setColumns(20);

        filterField3.addTextChangeListener(change -> {
            historialPagosContainer.removeContainerFilters(DOCA_PROPERTY);
            if (!change.getText().isEmpty()) {
                historialPagosContainer.addContainerFilter(
                        new SimpleStringFilter(DOCA_PROPERTY,
                                change.getText(), true, false));
            }
            setTotal();
        });
        cell3.setComponent(filterField3);

        footerHistorial = historialPagosGrid.appendFooterRow();
        footerHistorial.getCell(MONEDA_PROPERTY).setText("Totales");
        footerHistorial.getCell(VALOR_PROPERTY).setText("0.00");
        footerHistorial.getCell(MONTO_QUETZALES_PROPERTY).setText("0.00");
        footerHistorial.getCell(VALOR_PROPERTY).setStyleName("rightalign");
        footerHistorial.getCell(MONTO_QUETZALES_PROPERTY).setStyleName("rightalign");

        historialPagosLayout.addComponent(historialPagosGrid);
        historialPagosLayout.setComponentAlignment(historialPagosGrid, Alignment.TOP_CENTER);

        addComponent(historialPagosLayout);
    }

    public void llenarGridHistorialPagos() {
        footerHistorial.getCell(VALOR_PROPERTY).setText("0.00");
        footerHistorial.getCell(MONTO_QUETZALES_PROPERTY).setText("0.00");

        historialPagosContainer.removeAllItems();

        totalMonto = 0.00;
        totalQueztales = 0.00;

        queryString = "SELECT DISTINCT contabilidad_partida.IdPartida, contabilidad_partida.CodigoPartida, contabilidad_partida.Fecha, ";
        queryString += " contabilidad_partida.TipoDocumento, contabilidad_partida.NumeroDocumento, contabilidad_partida.IdNomenclatura,";
        queryString += " contabilidad_partida.TipoDOCA, contabilidad_partida.NoDOCA, contabilidad_partida.Estatus,";
        queryString += " contabilidad_partida.MonedaDocumento, contabilidad_partida.TipoCambio,  ";
        queryString += " contabilidad_partida.IdProveedor, contabilidad_partida.NombreProveedor, contabilidad_partida.NombreCheque, ";
        queryString += " Debe AS Total, usuario.Nombre AS uNombre, contabilidad_nomenclatura.N5, contabilidad_partida.Descripcion,  ";
        queryString += " contabilidad_partida.MontoDocumento";
        queryString += " FROM contabilidad_partida,usuario, contabilidad_nomenclatura  ";
        queryString += " WHERE contabilidad_partida.IdEmpresa = " + empresaId;
        queryString += " AND contabilidad_partida.TipoDocumento In ('CHEQUE', 'TRANSFERENCIA', 'NOTA DE DEBITO', 'PAGO DOCUMENTO VENTA')";
        queryString += " AND contabilidad_partida.IdNomenclatura In (";
        if(((SopdiUI) mainUI).cuentasContablesDefault.getBancosMonedaLocal() != null){
            queryString += ((SopdiUI) mainUI).cuentasContablesDefault.getBancosMonedaLocal();
        }
        if(((SopdiUI) mainUI).cuentasContablesDefault.getBancosMonedaExtranjera() != null){
            queryString += " ," + ((SopdiUI) mainUI).cuentasContablesDefault.getBancosMonedaExtranjera();
        }
        queryString += ")";
        queryString += " AND contabilidad_nomenclatura.IdNomenclatura = contabilidad_partida.IdNomenclatura ";
        queryString += " AND usuario.IdUsuario = contabilidad_partida.CreadoUsuario   ";
        queryString += " AND (contabilidad_partida.Fecha BETWEEN ";
        queryString += "     '" + Utileria.getFechaYYYYMMDD_1(inicioDt.getValue()) + "'";
        queryString += " AND '" + Utileria.getFechaYYYYMMDD_1(finDt.getValue()) + "')";

//        System.out.println("query de busqueda en historial pago " + queryString);

        try {
            stQueryHistorial = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecordsHistorial = stQueryHistorial.executeQuery(queryString);

            if (rsRecordsHistorial.next()) { //  encontrado                                                                
                do {

                    Object itemId = historialPagosContainer.addItem();

                    if (itemId == null) {
                        break;
                    }
                    historialPagosContainer.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecordsHistorial.getString("IdPartida"));
                    historialPagosContainer.getContainerProperty(itemId, CODIGOPARTIDA_PROPERTY).setValue(rsRecordsHistorial.getString("CodigoPartida"));
                    historialPagosContainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecordsHistorial.getDate("Fecha")));
                    historialPagosContainer.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(rsRecordsHistorial.getString("Descripcion"));
                    historialPagosContainer.getContainerProperty(itemId, CUENTA_PROPERTY).setValue(rsRecordsHistorial.getString("N5"));
                    historialPagosContainer.getContainerProperty(itemId, TIPO_PROPERTY).setValue(rsRecordsHistorial.getString("TipoDocumento"));
                    historialPagosContainer.getContainerProperty(itemId, DOCUMENTO_PROPERTY).setValue(rsRecordsHistorial.getString("NumeroDocumento"));
                    historialPagosContainer.getContainerProperty(itemId, PROVEEDOR_PROPERTY).setValue(rsRecordsHistorial.getString("NombreProveedor"));
                    historialPagosContainer.getContainerProperty(itemId, MONEDA_PROPERTY).setValue(rsRecordsHistorial.getString("MonedaDocumento"));
                    historialPagosContainer.getContainerProperty(itemId, VALOR_PROPERTY).setValue(numberFormat.format(rsRecordsHistorial.getDouble("MontoDocumento")));
                    historialPagosContainer.getContainerProperty(itemId, TIPOCAMBIO_PROPERTY).setValue(rsRecordsHistorial.getString("TipoCambio"));
                    historialPagosContainer.getContainerProperty(itemId, MONTO_QUETZALES_PROPERTY).setValue(numberFormat.format(rsRecordsHistorial.getDouble("MontoDocumento") * rsRecordsHistorial.getDouble("TipoCambio")));
                    historialPagosContainer.getContainerProperty(itemId, ESTATUS_PROPERTY).setValue(rsRecordsHistorial.getString("Estatus"));
                    historialPagosContainer.getContainerProperty(itemId, DOCA_PROPERTY).setValue(rsRecordsHistorial.getString("TipoDOCA") + " " + rsRecordsHistorial.getString("NoDOCA"));
                    historialPagosContainer.getContainerProperty(itemId, USUARIO_PROPERTY).setValue(rsRecordsHistorial.getString("UNombre"));
                    historialPagosContainer.getContainerProperty(itemId, NOMBRECHEQUE_PROPERTY).setValue(rsRecordsHistorial.getString("NombreCheque"));
                    historialPagosContainer.getContainerProperty(itemId, ID_NOMENCLATURA_PROPERTY).setValue(rsRecordsHistorial.getString("IdNomenclatura"));
                    historialPagosContainer.getContainerProperty(itemId, ID_PROVEEDOR_PROPERTY).setValue(rsRecordsHistorial.getString("IdProveedor"));

                    totalMonto = totalMonto + rsRecordsHistorial.getDouble("MontoDocumento");
                    totalQueztales = totalQueztales + (rsRecordsHistorial.getDouble("MontoDocumento") * rsRecordsHistorial.getDouble("TipoCambio"));

                } while (rsRecordsHistorial.next());
            }

        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error al listar tabla de HISTORIAL DE PAGOS : " + ex);
            ex.printStackTrace();
        }

        setTotal();

    }

    private void setTotal() {
        BigDecimal total = new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal totalQ = new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP);

        for (Object rid : historialPagosGrid.getContainerDataSource()
                .getItemIds()) {
            if (rid == null) {
                return;
            }

            if (historialPagosContainer.getContainerProperty(rid, VALOR_PROPERTY).getValue() == null) {
                return;
            }
            total = total.add(new BigDecimal(
                    Double.parseDouble(
                            String.valueOf(historialPagosContainer.getContainerProperty(rid, VALOR_PROPERTY).getValue()).replaceAll(",", "")
                    )));
            totalQ = totalQ.add(new BigDecimal(
                    Double.parseDouble(
                            String.valueOf(historialPagosContainer.getContainerProperty(rid, MONTO_QUETZALES_PROPERTY).getValue()).replaceAll(",", "")
                    )));
        }
        footerHistorial.getCell(VALOR_PROPERTY).setText(numberFormat.format(total));
        footerHistorial.getCell(MONTO_QUETZALES_PROPERTY).setText(numberFormat.format(totalQ));
    }

    private boolean exportToExcel() {
        if (this.historialPagosGrid.getHeightByRows() > 0) {
            TableHolder tableHolder = new DefaultTableHolder(historialPagosGrid);
            ExcelExport excelExport = new ExcelExport(tableHolder);
            excelExport.excludeCollapsedColumns();
            excelExport.setDisplayTotals(false);
            String fileexport;
            fileexport = (empresaId + "_" + empresaNombre.replaceAll(" ", "_").replaceAll(",", "_").replaceAll("[()]", "").replaceAll("[.]", "").replaceAll("Ã±", "n").replaceAll("Ã‘", "N").replaceAll("Ã³", "o").replaceAll("Ã©", "") + "_DOCUMENTOS.xls").replaceAll(" ", "").replaceAll(",", "");
            excelExport.setExportFileName(fileexport);
            excelExport.export();
        }
        return true;
    }

    public void llenarDatosArhivo() {

        ControladorPojoChequesBiBanking controlador = new ControladorPojoChequesBiBanking();
        ArrayList<PojoChequesBiBanking> listaCheques = new ArrayList<PojoChequesBiBanking>();

        for (Object itemId : historialPagosContainer.getItemIds()) {

            try {

                Item item = historialPagosContainer.getItem(itemId);

                if (String.valueOf(item.getItemProperty(TIPO_PROPERTY).getValue()).equals("CHEQUE")) {

                    queryString = "SELECT * FROM proveedor_empresa";
                    queryString += " WHERE IdProveedor = " + String.valueOf(item.getItemProperty(ID_PROVEEDOR_PROPERTY).getValue());
                    queryString += " AND IdEmpresa = " + empresaId;
                    //queryString += " And EsPlanilla = 1 ";

                    stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                    rsRecords = stQuery.executeQuery(queryString);

                    if (rsRecords.next()) {

                        queryString = "SELECT * FROM contabilidad_cuentas_bancos";
                        queryString += " WHERE IdEmpresa = " +empresaId;
                        queryString += " AND IdNomenclatura = " + String.valueOf(item.getItemProperty(ID_NOMENCLATURA_PROPERTY).getValue());
                        queryString += " AND Moneda = '" + String.valueOf(item.getItemProperty(MONEDA_PROPERTY).getValue() + "'");

                        stQuery1 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                        rsRecords2 = stQuery1.executeQuery(queryString);

                        if (rsRecords2.next()) {

                            PojoChequesBiBanking cheque = new PojoChequesBiBanking();
                            cheque.setNumeroDocumento(String.valueOf(item.getItemProperty(DOCUMENTO_PROPERTY).getValue()));
                            cheque.setFecha(String.valueOf(item.getItemProperty(FECHA_PROPERTY).getValue()));
                            cheque.setHaber(String.valueOf(item.getItemProperty(VALOR_PROPERTY).getValue()).replaceAll(",", ""));
                            cheque.setNoCuenta(rsRecords2.getString("NoCuenta"));
                            cheque.setNombreCheque(String.valueOf(item.getItemProperty(NOMBRECHEQUE_PROPERTY).getValue()));

                            listaCheques.add(cheque);
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Error al intentar buscar en contabilidad_cuentas_bancos" + e);
                e.printStackTrace();
            }
        }

        filePath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath() + "/pdfreceipts/" + empresaId+"_CHEQUES"+ ".txt";
//DESARROLLO filePath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath() + "\\" +empresaCbx.getValue() + "_CHEQUES" + ".txt";

        controlador.crearArchivo(filePath, listaCheques);
    }

    private StreamResource getStream(File inputfile) {

        StreamResource.StreamSource source = new StreamResource.StreamSource() {

            public InputStream getStream() {

                InputStream input = null;
                try {
                    input = new FileInputStream(inputfile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                return input;

            }
        };
        StreamResource resource = new StreamResource(source, inputfile.getName());
        return resource;
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - Historial Pagos");
    }
}
