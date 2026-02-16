package com.simpletecno.sopdi.contabilidad;

import com.vaadin.addon.tableexport.DefaultTableHolder;
import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.addon.tableexport.TableHolder;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.configuracion.EnvironmentVars;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Sizeable;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.renderers.ClickableRenderer;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.ui.NumberField;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author user
 */
public class LibroDiarioView extends VerticalLayout implements View {

    static final String ID_PROPERTY = "Id";
    static final String FECHA_PROPERTY = "Fecha";
    static final String CODIGO_PARTIDA_PROPERTY = "Partida";
    static final String CUENTA_PROPERTY = "Cuenta";
    static final String DESCRIPCION_PROPERTY = "Descripción";
    static final String DEBE_PROPERTY = "Debe";
    static final String HABER_PROPERTY = "Haber";
    static final String DIFERENCIA_PROPERTU = "Diferencia";
    static final String LIQUIDACION_PROPERTY = "Liquidacion";
    static final String DOCUMENTO_PROPERTY = "Documento";
    static final String TIPODOC_PROPERTY = "TipoDoc";
    static final String CODIGOCC_PROPERTY = "Codigo CC";
    static final String ARCHIVO_PROPERTY = "Archivo";
    static final String ARCHIVO_TIPO_PROPERTY = "Tipo";
    static final String IDENTIFICADOR_PROPERTY = "I";

    Grid libroDiarioGrid;

    public IndexedContainer libroDiariocontainer = new IndexedContainer();

    UI mainUI;
    Statement stQuery1;
    ResultSet rsRecords1;

    Statement stQuery2;
    ResultSet rsRecords2;

    public Button consultarBtn;
    Button exportExcelBtn;
    ComboBox empresaCbx;
    public String empresa;
    public DateField inicioDt;
    public DateField finDt;
    public TextField documentoTxt;

    ComboBox tipoTransaccionCbx;
    EnvironmentVars enviroments;
    CheckBox verDescuadresCbx;
    NumberField folioTxt;

    double debe = 0.00;
    double haber = 0.00;
    double totalDebe = 0.00;
    double totalHaber = 0.00;

    String queryString;

    static DecimalFormat numberFormat = new DecimalFormat("##,###,##0.00");

    public LibroDiarioView() {

        this.mainUI = UI.getCurrent();
        setWidth("100%");
        setSpacing(true);
        setMargin(false);
        setHeightUndefined();

        enviroments = new EnvironmentVars();

        tipoTransaccionCbx = new ComboBox("Tipo transacción");
        tipoTransaccionCbx.setNewItemsAllowed(true);
        tipoTransaccionCbx.setWidth("15em");
        tipoTransaccionCbx.addItem("<<TODAS>>");
        tipoTransaccionCbx.addItem("FACTURA");
        tipoTransaccionCbx.addItem("FACTURA VENTA");
        tipoTransaccionCbx.addItem("RECIBO");
        tipoTransaccionCbx.addItem("RECIBO CORRIENTE");
        tipoTransaccionCbx.addItem("RECIBO CONTABLE");
        tipoTransaccionCbx.addItem("FORMULARIO");
        tipoTransaccionCbx.addItem("CHEQUE");
        tipoTransaccionCbx.addItem("TRANSFERENCIA ");
        tipoTransaccionCbx.addItem("NOTA DE CREDITO FACTURA");
        tipoTransaccionCbx.addItem("NOTA DE DEBITO FACTURA");
        tipoTransaccionCbx.addItem("NOTA DE CREDITO");
        tipoTransaccionCbx.addItem("NOTA DE DEBITO");
        tipoTransaccionCbx.addItem("PLANILLA");
        tipoTransaccionCbx.addItem("PRESTAMOS");
        tipoTransaccionCbx.addItem("ENGANCHES");
        tipoTransaccionCbx.addItem("DEPOSITO POR COMPRA DE MONEDA");
        tipoTransaccionCbx.addItem("INTERESES DEVENGADOS");
        tipoTransaccionCbx.addItem("REEMBOLSO DE ANTICIPOS");
        tipoTransaccionCbx.addItem("TRANSACCION ESPECIAL");
        tipoTransaccionCbx.addItem("PAGO DOCUMENTO");
        tipoTransaccionCbx.select("<<TODAS>>");
        tipoTransaccionCbx.addValueChangeListener(event -> {
            llenarGridLibroDiario(empresa);
        });

        documentoTxt = new TextField("Docto./Liqui./Partida");
        documentoTxt.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
        documentoTxt.setWidth("10em");
        documentoTxt.setIcon(FontAwesome.SEARCH);
        documentoTxt.setInputPrompt("Buscar");
        documentoTxt.setDescription("Escriba la factura, liquidación o partida a buscar.");
        documentoTxt.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {

                if (libroDiarioGrid != null) {
                    llenarGridLibroDiario(empresa);
                }
            }
        });

        Label titleLbl = new Label("LIBRO DIARIO");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();

        empresaCbx = new ComboBox("Empresa:");
        empresaCbx.setWidth("400px");
        empresaCbx.addStyleName(ValoTheme.COMBOBOX_HUGE);
        empresaCbx.setInvalidAllowed(false);
        empresaCbx.setNewItemsAllowed(false);
        empresaCbx.setTextInputAllowed(false);

        llenarComboEmpresa();

        empresaCbx.addValueChangeListener(event -> {
            empresa = String.valueOf(event.getProperty().getValue());
            llenarGridLibroDiario(empresa);
        });

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setResponsive(true);
        titleLayout.setSpacing(true);
        titleLayout.setWidth("100%");
        titleLayout.setMargin(false);
        titleLayout.addComponents(empresaCbx, titleLbl);
        titleLayout.setComponentAlignment(empresaCbx, Alignment.MIDDLE_CENTER);
        titleLayout.setComponentAlignment(titleLbl, Alignment.MIDDLE_CENTER);
        titleLayout.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

        addComponent(titleLayout);
        setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        crearTablaLibroDiario();

        empresa = String.valueOf(empresaCbx.getValue());

        //  llenarGridLibroDiario(empresa);
    }

    public void crearTablaLibroDiario() {
        VerticalLayout layoutTablaLibroDiario = new VerticalLayout();
        layoutTablaLibroDiario.setWidth("100%");
        layoutTablaLibroDiario.setSpacing(true);
        layoutTablaLibroDiario.addStyleName("rcorners3");

        HorizontalLayout filtrosLayout = new HorizontalLayout();
        filtrosLayout.setSpacing(true);

        HorizontalLayout layoutButtons = new HorizontalLayout();
        layoutButtons.setSpacing(true);

        libroDiariocontainer.addContainerProperty(ID_PROPERTY, String.class, null);
        libroDiariocontainer.addContainerProperty(FECHA_PROPERTY, String.class, null);
        libroDiariocontainer.addContainerProperty(CODIGO_PARTIDA_PROPERTY, String.class, null);
        libroDiariocontainer.addContainerProperty(CUENTA_PROPERTY, String.class, null);
        libroDiariocontainer.addContainerProperty(DESCRIPCION_PROPERTY, String.class, null);
        libroDiariocontainer.addContainerProperty(DEBE_PROPERTY, String.class, null);
        libroDiariocontainer.addContainerProperty(HABER_PROPERTY, String.class, null);
        libroDiariocontainer.addContainerProperty(DIFERENCIA_PROPERTU, String.class, null);
        libroDiariocontainer.addContainerProperty(LIQUIDACION_PROPERTY, String.class, null);
        libroDiariocontainer.addContainerProperty(TIPODOC_PROPERTY, String.class, null);
        libroDiariocontainer.addContainerProperty(DOCUMENTO_PROPERTY, String.class, null);
        libroDiariocontainer.addContainerProperty(CODIGOCC_PROPERTY, String.class, null);
        libroDiariocontainer.addContainerProperty(ARCHIVO_PROPERTY, String.class, null);
        libroDiariocontainer.addContainerProperty(ARCHIVO_TIPO_PROPERTY, String.class, null);
        libroDiariocontainer.addContainerProperty(IDENTIFICADOR_PROPERTY, String.class, null);

        libroDiarioGrid = new Grid(libroDiariocontainer);
        libroDiarioGrid.setImmediate(true);
        libroDiarioGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        libroDiarioGrid.setHeightMode(HeightMode.ROW);
        libroDiarioGrid.setHeightByRows(15);
        libroDiarioGrid.setWidth("100%");
        libroDiarioGrid.setResponsive(true);
        libroDiarioGrid.setEditorBuffered(false);
        libroDiarioGrid.setColumnReorderingAllowed(false);

        libroDiarioGrid.getColumn(CODIGO_PARTIDA_PROPERTY).setRenderer(new ButtonRenderer(e
                -> VisualizarImagen(e), ""));
/**** falta validar el estatus de la partida, no se permite editar partidas en estatus <> "INGRESADO
        libroDiarioGrid.getColumn(DIFERENCIA_PROPERTU).setRenderer(new ButtonRenderer(e -> {

            if (String.valueOf(libroDiariocontainer.getContainerProperty(e.getItemId(), TIPODOC_PROPERTY).getValue()).equals("FACTURA VENTA")) {
                EditarPartidaFacturaVenta editFacturasGasto
                        = new EditarPartidaFacturaVenta(
                                String.valueOf(libroDiariocontainer.getContainerProperty(e.getItemId(), CODIGO_PARTIDA_PROPERTY).getValue()),
                                String.valueOf(libroDiariocontainer.getContainerProperty(e.getItemId(), DESCRIPCION_PROPERTY).getValue()));
                editFacturasGasto.llenarComboProveedor();
                editFacturasGasto.cuentaContable1Cbx.focus();
                editFacturasGasto.llenarCampos();
                UI.getCurrent().addWindow(editFacturasGasto);
                editFacturasGasto.center();

            } else if (String.valueOf(libroDiariocontainer.getContainerProperty(e.getItemId(), TIPODOC_PROPERTY).getValue()).equals("NOTA DE CREDITO")) {

                TransaccionesEspecialesISRForm nuevaTransaccion
                        = new TransaccionesEspecialesISRForm(
                                empresa,
                                String.valueOf(libroDiariocontainer.getContainerProperty(e.getItemId(), CODIGO_PARTIDA_PROPERTY).getValue()),
                                "NOTA DE CREDITO"
                        );

                UI.getCurrent().addWindow(nuevaTransaccion);
                nuevaTransaccion.center();

            } else if (Integer.parseInt(String.valueOf(libroDiariocontainer.getContainerProperty(e.getItemId(), LIQUIDACION_PROPERTY).getValue())) > 0) {
                System.out.println("Entro a modificar la liquidacion");
                queryString = "UPDATE  contabilidad_partida";
                queryString += " set Estatus = 'INGRESADO'";
                queryString += " Where IdEmpresa = " + empresa;
                queryString += " and CodigoPartida = '" + String.valueOf(libroDiariocontainer.getContainerProperty(e.getItemId(), CODIGO_PARTIDA_PROPERTY).getValue()) + "'";
                try {
                    stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                    stQuery.executeUpdate(queryString);
                } catch (Exception ex) {

                    System.out.println("Error al intentar modificar estatus a INGRESADO" + ex);
                    Notification.show("ERROR AL INTENTAR CAMBIAR EL ESTATUS INGRESADO A PARTIDA CONTABLE", Notification.Type.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
                EditarPartidaLiquidacion partidaLiquidacion
                        = new EditarPartidaLiquidacion(
                                String.valueOf(libroDiariocontainer.getContainerProperty(e.getItemId(), CODIGO_PARTIDA_PROPERTY).getValue()),
                                String.valueOf(libroDiariocontainer.getContainerProperty(e.getItemId(), CODIGOCC_PROPERTY).getValue()));///////PENDIENTE DE ASIGNAR EL CODIGO CC
                UI.getCurrent().addWindow(partidaLiquidacion);
                partidaLiquidacion.center();
            } else if (String.valueOf(libroDiariocontainer.getContainerProperty(e.getItemId(), TIPODOC_PROPERTY).getValue()).equals("FACTURA")
                    || String.valueOf(libroDiariocontainer.getContainerProperty(e.getItemId(), TIPODOC_PROPERTY).getValue()).equals("RECIBO CONTABLE")
                    || String.valueOf(libroDiariocontainer.getContainerProperty(e.getItemId(), TIPODOC_PROPERTY).getValue()).equals("RECIBO")
                    || String.valueOf(libroDiariocontainer.getContainerProperty(e.getItemId(), TIPODOC_PROPERTY).getValue()).equals("FORMULARIO")
                    || String.valueOf(libroDiariocontainer.getContainerProperty(e.getItemId(), TIPODOC_PROPERTY).getValue()).equals("CONSTANCIA ISR")
                    && String.valueOf(libroDiariocontainer.getContainerProperty(e.getItemId(), LIQUIDACION_PROPERTY).getValue()).equals("0")) {
                EditarIngresoDocumentos editFacturasGasto
                        = new EditarIngresoDocumentos(
                                String.valueOf(libroDiariocontainer.getContainerProperty(e.getItemId(), CODIGO_PARTIDA_PROPERTY).getValue()),
                                String.valueOf(libroDiariocontainer.getContainerProperty(e.getItemId(), DESCRIPCION_PROPERTY).getValue()));
                editFacturasGasto.cuentaContable1Cbx.focus();
                editFacturasGasto.llenarComboOrdenCompra();
                UI.getCurrent().addWindow(editFacturasGasto);
                editFacturasGasto.center();
            }

        }));
***/
        libroDiarioGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {

            if (DEBE_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (HABER_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }

        });

        libroDiarioGrid.getColumn(FECHA_PROPERTY).setExpandRatio(1);
        libroDiarioGrid.getColumn(CODIGO_PARTIDA_PROPERTY).setExpandRatio(1).setMaximumWidth(50);
        libroDiarioGrid.getColumn(CUENTA_PROPERTY).setExpandRatio(1);
        libroDiarioGrid.getColumn(DESCRIPCION_PROPERTY).setExpandRatio(3);
        libroDiarioGrid.getColumn(DEBE_PROPERTY).setExpandRatio(1);
        libroDiarioGrid.getColumn(HABER_PROPERTY).setExpandRatio(1);
        libroDiarioGrid.getColumn(DIFERENCIA_PROPERTU).setExpandRatio(1);
        libroDiarioGrid.getColumn(TIPODOC_PROPERTY).setExpandRatio(1);
        libroDiarioGrid.getColumn(DOCUMENTO_PROPERTY).setExpandRatio(1);
        libroDiarioGrid.getColumn(CODIGOCC_PROPERTY).setExpandRatio(1);

        libroDiarioGrid.getColumn(ID_PROPERTY).setHidable(true).setHidden(true);
        libroDiarioGrid.getColumn(LIQUIDACION_PROPERTY).setHidable(true).setHidden(true);
        libroDiarioGrid.getColumn(DIFERENCIA_PROPERTU).setHidable(true).setHidden(true);
        libroDiarioGrid.getColumn(TIPODOC_PROPERTY).setHidable(true).setHidden(true);
        libroDiarioGrid.getColumn(DOCUMENTO_PROPERTY).setHidable(true).setHidden(true);
        libroDiarioGrid.getColumn(CODIGOCC_PROPERTY).setHidable(true).setHidden(true);
        libroDiarioGrid.getColumn(ARCHIVO_PROPERTY).setHidable(true).setHidden(true);
        libroDiarioGrid.getColumn(ARCHIVO_TIPO_PROPERTY).setHidable(true).setHidden(true);
        libroDiarioGrid.getColumn(IDENTIFICADOR_PROPERTY).setHidden(true);

        inicioDt = new DateField("DEL:");
        inicioDt.setDateFormat("dd/MM/yyyy");
        Date primerDia = Utileria.getPrimerDiaDelMes();
        inicioDt.setValue(primerDia);
        inicioDt.setWidth("8em");

        finDt = new DateField("AL:");
        finDt.setDateFormat("dd/MM/yyyy");
        Date ultimoDia = Utileria.getUltimoDiaDelMes();
        finDt.setValue(ultimoDia);
        finDt.setWidth("8em");

        verDescuadresCbx = new CheckBox("Ver descuadres");
        verDescuadresCbx.setValue(false);
        verDescuadresCbx.addValueChangeListener((event) -> {
            llenarGridLibroDiario(empresa);
        });

        consultarBtn = new Button("Consultar");
        consultarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        consultarBtn.setIcon(FontAwesome.SEARCH);
        consultarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                llenarGridLibroDiario(empresa);
            }
        });

        folioTxt = new NumberField("Folio Inicial :");
        folioTxt.setDecimalAllowed(false);
        folioTxt.setDecimalPrecision(0);
        folioTxt.setMinimumFractionDigits(0);
        folioTxt.setDecimalSeparatorAlwaysShown(false);
        folioTxt.setValue(1d);
        folioTxt.setGroupingUsed(true);
        folioTxt.setGroupingSize(0);
        folioTxt.setImmediate(true);
        folioTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);
        folioTxt.setWidth("8em");
        folioTxt.setValidationVisible(false);

        Button generarPDF = new Button("Generar PDF");
        generarPDF.setIcon(FontAwesome.PAPER_PLANE);
        generarPDF.addStyleName(ValoTheme.BUTTON_PRIMARY);

        generarPDF.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (libroDiariocontainer.size() > 0) {
                    printPdf();
                } else {
                    Notification.show("La vista no contiene registros disponibles.. ", Notification.Type.WARNING_MESSAGE);
                }
            }
        });

        exportExcelBtn = new Button("Excel");
        exportExcelBtn.setIcon(FontAwesome.FILE_EXCEL_O);
        exportExcelBtn.setWidth(120, Sizeable.UNITS_PIXELS);
        exportExcelBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {

                if (libroDiarioGrid.getHeightByRows() > 0) {
                    TableHolder tableHolder = new DefaultTableHolder(libroDiarioGrid);
                    ExcelExport excelExport = new ExcelExport(tableHolder);
                    excelExport.excludeCollapsedColumns();
                    excelExport.setDisplayTotals(false);
                    String fileexport;
                    fileexport = "LibroDiario_" + empresaCbx.getItemCaption(empresaCbx.getValue()).replaceAll(" ", "_").replaceAll(",", "_").replaceAll("[()]", "").replaceAll("[.]", "").replaceAll("ñ", "n").replaceAll("Ñ", "N").replaceAll("ó", "o").replaceAll("é", "") + ".xls";
                    excelExport.setExportFileName(fileexport);
                    excelExport.export();
                }
            }
        });

        filtrosLayout.addComponents(inicioDt, finDt, documentoTxt, tipoTransaccionCbx, consultarBtn);
        filtrosLayout.setComponentAlignment(consultarBtn, Alignment.BOTTOM_RIGHT);

        layoutButtons.addComponent(verDescuadresCbx);
        layoutButtons.setComponentAlignment(verDescuadresCbx, Alignment.MIDDLE_LEFT);
        layoutButtons.addComponent(folioTxt);
        layoutButtons.setComponentAlignment(folioTxt, Alignment.BOTTOM_LEFT);
        layoutButtons.addComponent(generarPDF);
        layoutButtons.setComponentAlignment(generarPDF, Alignment.BOTTOM_CENTER);
        layoutButtons.addComponent(exportExcelBtn);
        layoutButtons.setComponentAlignment(exportExcelBtn, Alignment.BOTTOM_CENTER);

        layoutTablaLibroDiario.addComponent(filtrosLayout);
        layoutTablaLibroDiario.addComponent(libroDiarioGrid);
        layoutTablaLibroDiario.addComponent(layoutButtons);
        layoutTablaLibroDiario.setComponentAlignment(layoutButtons, Alignment.MIDDLE_CENTER);
        layoutTablaLibroDiario.setComponentAlignment(filtrosLayout, Alignment.MIDDLE_CENTER);

        addComponent(layoutTablaLibroDiario);
        setComponentAlignment(layoutTablaLibroDiario, Alignment.MIDDLE_CENTER);

    }

    public void llenarGridLibroDiario(String empresa) {

        libroDiarioGrid.getColumn(CUENTA_PROPERTY).setHidable(false).setHidden(false);
        libroDiarioGrid.getColumn(DESCRIPCION_PROPERTY).setHidable(false).setHidden(false);
        libroDiarioGrid.getColumn(DIFERENCIA_PROPERTU).setHidable(true).setHidden(true);

        if (libroDiariocontainer == null) {
            return;
        }

        libroDiariocontainer.removeAllItems();

        if (libroDiariocontainer.getContainerPropertyIds().size() < 7) {
            return;
        }

        totalDebe = 0.00;
        totalHaber = 0.00;
        debe = 0.00;
        haber = 0.00;

        try {

            String queryString;
            queryString = " select contabilidad_partida.IdPartida, contabilidad_partida.CodigoPartida,  ";
            queryString += " contabilidad_nomenclatura.NoCuenta, Sum(contabilidad_partida.DebeQuetzales) AS TotalDebe, ";
            queryString += " Sum(contabilidad_partida.HaberQuetzales) AS TotalHaber, contabilidad_nomenclatura.N5,";
            queryString += " contabilidad_partida.Fecha, contabilidad_partida.Descripcion,";
            queryString += " contabilidad_partida.ArchivoNombre, contabilidad_partida.ArchivoTipo, contabilidad_partida.CodigoCC,";
            queryString += " contabilidad_partida.SerieDocumento, contabilidad_partida.NumeroDocumento, contabilidad_partida.TipoDocumento";
            queryString += " from contabilidad_partida,contabilidad_nomenclatura";
            queryString += " where contabilidad_partida.IdEmpresa  = " + empresa;
            queryString += " and contabilidad_nomenclatura.IdNomenclatura = contabilidad_partida.IdNomenclatura";
            queryString += " AND contabilidad_partida.Fecha BETWEEN ";
            queryString += "     '" + Utileria.getFechaYYYYMMDD_1(inicioDt.getValue()) + "'";
            queryString += " AND '" + Utileria.getFechaYYYYMMDD_1(finDt.getValue()) + "'";
            if (!documentoTxt.getValue().trim().isEmpty()) {
                String documentoSerie[] = documentoTxt.getValue().split(" ");

                if (documentoSerie.length > 1) {
                    queryString += " and contabilidad_partida.SerieDocumento = '" + documentoSerie[0] + "'";
                    queryString += " and contabilidad_partida.NumeroDocumento = '" + documentoSerie[1] + "'";
                    queryString += " OR contabilidad_partida.CodigoPartida LIKE '" + documentoTxt.getValue().trim() + "%'";
                } else {
                    queryString += " AND (contabilidad_partida.NumeroDocumento = '" + documentoSerie[0] + "' Or contabilidad_partida.IdLiquidacion = " + documentoSerie[0];
                    queryString += "  OR contabilidad_partida.CodigoPartida LIKE '" + documentoTxt.getValue().trim() + "%')";
                }
            }
            if (!String.valueOf(tipoTransaccionCbx.getValue()).equals("<<TODAS>>")) {
                queryString += " And contabilidad_partida.TipoDocumento LIKE '" + String.valueOf(tipoTransaccionCbx.getValue()) + "%'";
            }
            queryString += " And UPPER(contabilidad_partida.Estatus) NOT IN('ANULADO', 'ANULADA')";
            queryString += " group  by contabilidad_partida.CodigoPartida, contabilidad_partida.IdNomenclatura";
            queryString += " Order By contabilidad_partida.CodigoPartida, contabilidad_partida.Debe Desc";

//            System.out.println("QUERY LIBRO DIARIO = " + queryString);

            stQuery2 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords2 = stQuery2.executeQuery(queryString);

            if (rsRecords2.next()) { //  encontrado                                                

                String codigoPartida = rsRecords2.getString("CodigoPartida");

                Object itemId = null;

                do {

                    itemId = libroDiariocontainer.addItem();

                    if (codigoPartida.equals(rsRecords2.getString("CodigoPartida"))) {

                        libroDiariocontainer.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords2.getString("IdPartida"));
                        libroDiariocontainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue((debe == 0.00 && haber == 0.00 ? Utileria.getFechaDDMMYYYY(rsRecords2.getDate("Fecha")) : ""));
                        libroDiariocontainer.getContainerProperty(itemId, CODIGO_PARTIDA_PROPERTY).setValue((debe == 0.00 && haber == 0.00 ? rsRecords2.getString("CodigoPartida") : ""));
                        libroDiariocontainer.getContainerProperty(itemId, CUENTA_PROPERTY).setValue(rsRecords2.getString("NoCuenta"));
                        libroDiariocontainer.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(rsRecords2.getString("N5"));
                        libroDiariocontainer.getContainerProperty(itemId, DEBE_PROPERTY).setValue(numberFormat.format(rsRecords2.getDouble("TotalDebe")));
                        libroDiariocontainer.getContainerProperty(itemId, HABER_PROPERTY).setValue(numberFormat.format(rsRecords2.getDouble("TotalHaber")));
                        libroDiariocontainer.getContainerProperty(itemId, ARCHIVO_PROPERTY).setValue((debe == 0.00 && haber == 0.00 ? rsRecords2.getString("ArchivoNombre") : ""));
                        libroDiariocontainer.getContainerProperty(itemId, ARCHIVO_TIPO_PROPERTY).setValue((debe == 0.00 && haber == 0.00 ? rsRecords2.getString("ArchivoTipo") : ""));
                        libroDiariocontainer.getContainerProperty(itemId,IDENTIFICADOR_PROPERTY).setValue(codigoPartida);
                        libroDiariocontainer.getContainerProperty(itemId,TIPODOC_PROPERTY).setValue(rsRecords2.getString("TipoDocumento"));
                        libroDiariocontainer.getContainerProperty(itemId,DOCUMENTO_PROPERTY).setValue(rsRecords2.getString("SerieDocUmento") + " " + rsRecords2.getString("NumeroDocumento"));
                        libroDiariocontainer.getContainerProperty(itemId,CODIGOCC_PROPERTY).setValue(rsRecords2.getString("CodigoCC"));

                    } else { // cambio de partida

                        libroDiariocontainer.getContainerProperty(itemId, ID_PROPERTY).setValue("");
                        libroDiariocontainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue("___________");
                        libroDiariocontainer.getContainerProperty(itemId, CODIGO_PARTIDA_PROPERTY).setValue("__________");
                        libroDiariocontainer.getContainerProperty(itemId, CUENTA_PROPERTY).setValue("__________");
                        libroDiariocontainer.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue("__________");
                        libroDiariocontainer.getContainerProperty(itemId, DEBE_PROPERTY).setValue("____________");
                        libroDiariocontainer.getContainerProperty(itemId, HABER_PROPERTY).setValue("___________");
                        libroDiariocontainer.getContainerProperty(itemId, ARCHIVO_PROPERTY).setValue("___________");
                        libroDiariocontainer.getContainerProperty(itemId, ARCHIVO_TIPO_PROPERTY).setValue("___________");
                        libroDiariocontainer.getContainerProperty(itemId, TIPODOC_PROPERTY).setValue("___________");
                        libroDiariocontainer.getContainerProperty(itemId, DOCUMENTO_PROPERTY).setValue("___________");
                        libroDiariocontainer.getContainerProperty(itemId, CODIGOCC_PROPERTY).setValue("___________");
                        libroDiariocontainer.getContainerProperty(itemId, IDENTIFICADOR_PROPERTY).setValue(codigoPartida);

                        itemId = libroDiariocontainer.addItem();

                        rsRecords2.previous();
                        libroDiariocontainer.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords2.getString("IdPartida"));
                        libroDiariocontainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue("");
                        libroDiariocontainer.getContainerProperty(itemId, CODIGO_PARTIDA_PROPERTY).setValue(rsRecords2.getString("Descripcion"));
                        rsRecords2.next();
                        libroDiariocontainer.getContainerProperty(itemId, CUENTA_PROPERTY).setValue("");
                        libroDiariocontainer.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue("----------> SUMAS IGUALES");
                        libroDiariocontainer.getContainerProperty(itemId, DEBE_PROPERTY).setValue(numberFormat.format(debe));
                        libroDiariocontainer.getContainerProperty(itemId, HABER_PROPERTY).setValue(numberFormat.format(haber));
                        libroDiariocontainer.getContainerProperty(itemId,IDENTIFICADOR_PROPERTY).setValue(codigoPartida);

                        itemId = libroDiariocontainer.addItem();

                        libroDiariocontainer.getContainerProperty(itemId, ID_PROPERTY).setValue("");
                        libroDiariocontainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue("");
                        libroDiariocontainer.getContainerProperty(itemId, CODIGO_PARTIDA_PROPERTY).setValue("");
                        libroDiariocontainer.getContainerProperty(itemId, CUENTA_PROPERTY).setValue("");
                        libroDiariocontainer.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue("");
                        libroDiariocontainer.getContainerProperty(itemId, DEBE_PROPERTY).setValue("");
                        libroDiariocontainer.getContainerProperty(itemId, HABER_PROPERTY).setValue("");
                        libroDiariocontainer.getContainerProperty(itemId, ARCHIVO_PROPERTY).setValue("");
                        libroDiariocontainer.getContainerProperty(itemId, ARCHIVO_TIPO_PROPERTY).setValue("");
                        libroDiariocontainer.getContainerProperty(itemId, TIPODOC_PROPERTY).setValue("___________");
                        libroDiariocontainer.getContainerProperty(itemId, DOCUMENTO_PROPERTY).setValue("___________");
                        libroDiariocontainer.getContainerProperty(itemId, CODIGOCC_PROPERTY).setValue("___________");
                        libroDiariocontainer.getContainerProperty(itemId,IDENTIFICADOR_PROPERTY).setValue(codigoPartida);

                        if (verDescuadresCbx.getValue()) {  // ver partidas descuadradas solamente
                            if (new BigDecimal(debe).setScale(2, BigDecimal.ROUND_HALF_UP).equals(new BigDecimal(haber).setScale(2, BigDecimal.ROUND_HALF_UP))) { // esta cuadrada, hay que marcarla para eliminarla del listado
                                for (Object lineId : libroDiariocontainer.getItemIds()) { // marcar los itemid de la partida
                                    if(codigoPartida.equals(libroDiariocontainer.getContainerProperty(lineId, IDENTIFICADOR_PROPERTY).getValue())) {
                                        libroDiariocontainer.getContainerProperty(lineId,IDENTIFICADOR_PROPERTY).setValue("CUADRADA");
                                    }
                                }
                            }
                        }

                        itemId = libroDiariocontainer.addItem();

                        codigoPartida = rsRecords2.getString("CodigoPartida");
                        debe = 0.00;
                        haber = 0.00;

                        //primera linea de la siguiente partida
                        libroDiariocontainer.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords2.getString("IdPartida"));
                        libroDiariocontainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords2.getDate("Fecha")));
                        libroDiariocontainer.getContainerProperty(itemId, CODIGO_PARTIDA_PROPERTY).setValue(rsRecords2.getString("CodigoPartida"));
                        libroDiariocontainer.getContainerProperty(itemId, CUENTA_PROPERTY).setValue(rsRecords2.getString("NoCuenta"));
                        libroDiariocontainer.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(rsRecords2.getString("N5"));
                        libroDiariocontainer.getContainerProperty(itemId, DEBE_PROPERTY).setValue(numberFormat.format(rsRecords2.getDouble("TotalDebe")));
                        libroDiariocontainer.getContainerProperty(itemId, HABER_PROPERTY).setValue(numberFormat.format(rsRecords2.getDouble("TotalHaber")));
                        libroDiariocontainer.getContainerProperty(itemId, ARCHIVO_PROPERTY).setValue(rsRecords2.getString("ArchivoNombre"));
                        libroDiariocontainer.getContainerProperty(itemId, ARCHIVO_TIPO_PROPERTY).setValue(rsRecords2.getString("ArchivoTipo"));
                        libroDiariocontainer.getContainerProperty(itemId, TIPODOC_PROPERTY).setValue("___________");
                        libroDiariocontainer.getContainerProperty(itemId, DOCUMENTO_PROPERTY).setValue("___________");
                        libroDiariocontainer.getContainerProperty(itemId, CODIGOCC_PROPERTY).setValue("___________");
                        libroDiariocontainer.getContainerProperty(itemId, IDENTIFICADOR_PROPERTY).setValue(codigoPartida);
                    }

                    debe += Utileria.round(rsRecords2.getDouble("TotalDebe"));
                    haber += Utileria.round(rsRecords2.getDouble("TotalHaber"));
                    totalDebe += Utileria.round(rsRecords2.getDouble("TotalDebe"));
                    totalHaber += Utileria.round(rsRecords2.getDouble("TotalHaber"));

                } while (rsRecords2.next());

                rsRecords2.previous();

                if(libroDiariocontainer.size() > 0) {

                    itemId = libroDiariocontainer.addItem();

                    libroDiariocontainer.getContainerProperty(itemId, ID_PROPERTY).setValue("___________");
                    libroDiariocontainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue("____________");
                    libroDiariocontainer.getContainerProperty(itemId, CODIGO_PARTIDA_PROPERTY).setValue("___________");
                    libroDiariocontainer.getContainerProperty(itemId, CUENTA_PROPERTY).setValue("___________");
                    libroDiariocontainer.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue("___________");
                    libroDiariocontainer.getContainerProperty(itemId, DEBE_PROPERTY).setValue("______________");
                    libroDiariocontainer.getContainerProperty(itemId, HABER_PROPERTY).setValue("____________");
                    libroDiariocontainer.getContainerProperty(itemId, ARCHIVO_PROPERTY).setValue("____________");
                    libroDiariocontainer.getContainerProperty(itemId, ARCHIVO_TIPO_PROPERTY).setValue("____________");
                    libroDiariocontainer.getContainerProperty(itemId, TIPODOC_PROPERTY).setValue("___________");
                    libroDiariocontainer.getContainerProperty(itemId, DOCUMENTO_PROPERTY).setValue("___________");
                    libroDiariocontainer.getContainerProperty(itemId, CODIGOCC_PROPERTY).setValue("___________");
                    libroDiariocontainer.getContainerProperty(itemId, IDENTIFICADOR_PROPERTY).setValue(codigoPartida);

                    itemId = libroDiariocontainer.addItem();

                    libroDiariocontainer.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords2.getString("IdPartida"));
                    libroDiariocontainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue("");
                    libroDiariocontainer.getContainerProperty(itemId, CODIGO_PARTIDA_PROPERTY).setValue(rsRecords2.getString("Descripcion"));
                    libroDiariocontainer.getContainerProperty(itemId, CUENTA_PROPERTY).setValue("");
                    libroDiariocontainer.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue("----------> SUMAS IGUALES");
                    libroDiariocontainer.getContainerProperty(itemId, DEBE_PROPERTY).setValue(numberFormat.format(debe));
                    libroDiariocontainer.getContainerProperty(itemId, HABER_PROPERTY).setValue(numberFormat.format(haber));
                    libroDiariocontainer.getContainerProperty(itemId, ARCHIVO_PROPERTY).setValue("");
                    libroDiariocontainer.getContainerProperty(itemId, ARCHIVO_TIPO_PROPERTY).setValue("");
                    libroDiariocontainer.getContainerProperty(itemId, TIPODOC_PROPERTY).setValue("___________");
                    libroDiariocontainer.getContainerProperty(itemId, DOCUMENTO_PROPERTY).setValue("___________");
                    libroDiariocontainer.getContainerProperty(itemId, CODIGOCC_PROPERTY).setValue("___________");
                    libroDiariocontainer.getContainerProperty(itemId, IDENTIFICADOR_PROPERTY).setValue(codigoPartida);

                    if (verDescuadresCbx.getValue()) {  // ver partidas descuadradas solamente

                        if (debe == haber) { // esta cuadrada, hay que marcarla para eliminarla del listado
                            for (Object lineId : libroDiariocontainer.getItemIds()) { // eliminar los itemid de la partida
                                if(codigoPartida.equals(libroDiariocontainer.getContainerProperty(lineId, IDENTIFICADOR_PROPERTY).getValue())) {
                                    libroDiariocontainer.getContainerProperty(lineId,IDENTIFICADOR_PROPERTY).setValue("CUADRADA");
                                }
                            }
                        }

                        Map<Integer,Object> objectsIds = new HashMap<Integer,Object>();

                        int icounter = 0;

                        for (Object lineId : libroDiariocontainer.getItemIds()) { // eliminar la partida

                            if("CUADRADA".equals(String.valueOf(libroDiariocontainer.getContainerProperty(lineId, IDENTIFICADOR_PROPERTY).getValue()))) {
                                objectsIds.put(icounter++, lineId);
                            }
                        }
                        for (Map.Entry<Integer,Object> entry : objectsIds.entrySet()) {
                            if(   !String.valueOf(libroDiariocontainer.getContainerProperty(entry.getValue(), DEBE_PROPERTY).getValue()).contains("___________")
                               && !String.valueOf(libroDiariocontainer.getContainerProperty(entry.getValue(), DEBE_PROPERTY).getValue()).trim().isEmpty()
                               && !String.valueOf(libroDiariocontainer.getContainerProperty(entry.getValue(), DESCRIPCION_PROPERTY).getValue()).contains("----------> SUMAS IGUALES")) {
                                totalDebe  -= Double.valueOf(String.valueOf(libroDiariocontainer.getContainerProperty(entry.getValue(), DEBE_PROPERTY).getValue()).replaceAll(",", ""));
                                totalDebe = Utileria.round(totalDebe);
                                totalHaber -= Double.valueOf(String.valueOf(libroDiariocontainer.getContainerProperty(entry.getValue(), HABER_PROPERTY).getValue()).replaceAll(",", ""));
                                totalHaber = Utileria.round(totalHaber);
                            }
                            libroDiariocontainer.removeItem(entry.getValue());
                        }

                    }
                    itemId = libroDiariocontainer.addItem();

                    libroDiariocontainer.getContainerProperty(itemId, ID_PROPERTY).setValue("___________");
                    libroDiariocontainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue("____________");
                    libroDiariocontainer.getContainerProperty(itemId, CODIGO_PARTIDA_PROPERTY).setValue("___________");
                    libroDiariocontainer.getContainerProperty(itemId, CUENTA_PROPERTY).setValue("___________");
                    libroDiariocontainer.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue("___________");
                    libroDiariocontainer.getContainerProperty(itemId, DEBE_PROPERTY).setValue("______________");
                    libroDiariocontainer.getContainerProperty(itemId, HABER_PROPERTY).setValue("____________");
                    libroDiariocontainer.getContainerProperty(itemId, ARCHIVO_PROPERTY).setValue("____________");
                    libroDiariocontainer.getContainerProperty(itemId, ARCHIVO_TIPO_PROPERTY).setValue("____________");
                    libroDiariocontainer.getContainerProperty(itemId, TIPODOC_PROPERTY).setValue("___________");
                    libroDiariocontainer.getContainerProperty(itemId, DOCUMENTO_PROPERTY).setValue("___________");
                    libroDiariocontainer.getContainerProperty(itemId, CODIGOCC_PROPERTY).setValue("___________");
                    libroDiariocontainer.getContainerProperty(itemId, IDENTIFICADOR_PROPERTY).setValue(codigoPartida);

                    itemId = libroDiariocontainer.addItem();

                    libroDiariocontainer.getContainerProperty(itemId, ID_PROPERTY).setValue("");
                    libroDiariocontainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue("");
                    libroDiariocontainer.getContainerProperty(itemId, CODIGO_PARTIDA_PROPERTY).setValue("");
                    libroDiariocontainer.getContainerProperty(itemId, CUENTA_PROPERTY).setValue("");
                    libroDiariocontainer.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue("----------> GRAN TOTAL");
                    libroDiariocontainer.getContainerProperty(itemId, DEBE_PROPERTY).setValue(numberFormat.format(totalDebe));
                    libroDiariocontainer.getContainerProperty(itemId, HABER_PROPERTY).setValue(numberFormat.format(totalHaber));
                    libroDiariocontainer.getContainerProperty(itemId, ARCHIVO_PROPERTY).setValue("");
                    libroDiariocontainer.getContainerProperty(itemId, ARCHIVO_TIPO_PROPERTY).setValue("");
                    libroDiariocontainer.getContainerProperty(itemId, TIPODOC_PROPERTY).setValue("___________");
                    libroDiariocontainer.getContainerProperty(itemId, DOCUMENTO_PROPERTY).setValue("___________");
                    libroDiariocontainer.getContainerProperty(itemId, CODIGOCC_PROPERTY).setValue("___________");
                    libroDiariocontainer.getContainerProperty(itemId, IDENTIFICADOR_PROPERTY).setValue(codigoPartida);

                    itemId = libroDiariocontainer.addItem();

                    libroDiariocontainer.getContainerProperty(itemId, ID_PROPERTY).setValue("___________");
                    libroDiariocontainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue("____________");
                    libroDiariocontainer.getContainerProperty(itemId, CODIGO_PARTIDA_PROPERTY).setValue("___________");
                    libroDiariocontainer.getContainerProperty(itemId, CUENTA_PROPERTY).setValue("___________");
                    libroDiariocontainer.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue("___________");
                    libroDiariocontainer.getContainerProperty(itemId, DEBE_PROPERTY).setValue("______________");
                    libroDiariocontainer.getContainerProperty(itemId, HABER_PROPERTY).setValue("____________");
                    libroDiariocontainer.getContainerProperty(itemId, ARCHIVO_PROPERTY).setValue("____________");
                    libroDiariocontainer.getContainerProperty(itemId, ARCHIVO_TIPO_PROPERTY).setValue("____________");
                    libroDiariocontainer.getContainerProperty(itemId, TIPODOC_PROPERTY).setValue("___________");
                    libroDiariocontainer.getContainerProperty(itemId, DOCUMENTO_PROPERTY).setValue("___________");
                    libroDiariocontainer.getContainerProperty(itemId, CODIGOCC_PROPERTY).setValue("___________");
                    libroDiariocontainer.getContainerProperty(itemId, IDENTIFICADOR_PROPERTY).setValue(codigoPartida);

                }

                debe = 0.00;
                haber = 0.00;
            }

        } catch (Exception ex) {
            System.out.println("Error al listar tabla Facturas en LibroDiarioView: " + ex);
            ex.printStackTrace();
        }
    }

    public void llenarComboEmpresa() {
        String queryString = " SELECT * from contabilidad_empresa";
        queryString += " Where IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();

        try {
            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords1 = stQuery1.executeQuery(queryString);

            while (rsRecords1.next()) { //  encontrado                
                empresaCbx.addItem(rsRecords1.getString("IdEmpresa"));
                empresaCbx.setItemCaption(rsRecords1.getString("IdEmpresa"), rsRecords1.getString("Empresa"));
            }
            rsRecords1.first();

            empresaCbx.select(rsRecords1.getString("IdEmpresa"));

        } catch (Exception ex1) {
            System.out.println("Error al listar empresas: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void VisualizarImagen(ClickableRenderer.RendererClickEvent e) {

        String codigoPartida = String.valueOf(libroDiariocontainer.getContainerProperty(e.getItemId(), CODIGO_PARTIDA_PROPERTY).getValue());
        String archivoNombre = String.valueOf(libroDiariocontainer.getContainerProperty(e.getItemId(), ARCHIVO_PROPERTY).getValue());
        String archivoTipo = String.valueOf(libroDiariocontainer.getContainerProperty(e.getItemId(), ARCHIVO_TIPO_PROPERTY).getValue());

        libroDiarioGrid.select(e.getItemId());
        String queryString;

        try {

            if (archivoNombre == null) {
                Notification.show("Documento scan no disponible para visualizar!", Notification.Type.WARNING_MESSAGE);
                return;
            }

            final byte docBytes[] = Files.readAllBytes(new File(archivoNombre).toPath());
            final String fileName = archivoNombre;

            if (docBytes == null) {
                Notification.show("Documento scan no disponible para visualizar!", Notification.Type.WARNING_MESSAGE);
                return;
            }

            Window window = new Window();
            window.setResizable(true);
            window.setWidth("95%");
            window.setHeight("95%");

            StreamResource documentStreamResource = null;

            if (docBytes != null) {
                documentStreamResource = new StreamResource(
                        new StreamResource.StreamSource() {
                    public InputStream getStream() {
                        return new ByteArrayInputStream(docBytes);
                    }
                }, fileName
                );
            }
            documentStreamResource.setMIMEType(archivoTipo);
            documentStreamResource.setFilename(archivoNombre);
            documentStreamResource.getStream().setParameter("Content-Disposition", "attachment; filename=" + fileName);

            if (archivoTipo.contains("pdf")) {

                BrowserFrame browserFrame = new BrowserFrame();
                browserFrame.setSizeFull();

                browserFrame.setSource(documentStreamResource);
                /*
                if(rsRecords.getString("Tipo").contains("image")) {
                    browserFrame.setHeight("600px");
                    browserFrame.setWidth("600px");
                }
                 */
                window.setContent(browserFrame);

            } else {
                window.setWidth("90%");
                window.setHeight("90%");
                VerticalLayout imageLayout = new VerticalLayout();
                imageLayout.setSizeFull();

                Image imageViewComponent = new Image(null, documentStreamResource);
                imageViewComponent.setImmediate(true);
                imageViewComponent.setSizeFull();
//                imageViewComponent.setWidth("600px");
//                imageViewComponent.setHeight("600px");
                imageViewComponent.addStyleName("my-img-button");
                imageViewComponent.setData(codigoPartida);

                imageLayout.addComponent(imageViewComponent);
                imageLayout.setComponentAlignment(imageViewComponent, Alignment.MIDDLE_CENTER);

                Panel imagePanel = new Panel();
                imagePanel.setResponsive(true);
                imagePanel.setContent(imageLayout);

                window.setContent(imagePanel);
            }

            UI.getCurrent().addWindow(window);
            window.center();

        } catch (Exception allEx) {
            Notification.show("Archivo (SCAN) no disponible..", Notification.Type.WARNING_MESSAGE);
            allEx.printStackTrace();
        }
    }

    public String getEmpresaNit() {
        String strNit = "N/A";

        String queryString = " SELECT Nit from contabilidad_empresa ";
        queryString += " Where IdEmpresa = " + empresa;

        try {
            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords1 = stQuery1.executeQuery(queryString);

            if (rsRecords1.next()) {
                strNit = rsRecords1.getString("Nit");
            }

        } catch (Exception ex1) {
            System.out.println("Error al buscar NIT de empresa: " + ex1.getMessage());
            ex1.printStackTrace();
        }

        return strNit;
    }

    public void printPdf() {
        LibroDiarioPDF libroDiarioPdf
                = new LibroDiarioPDF(
                        empresa,
                        empresaCbx.getItemCaption(empresaCbx.getValue()),
                        getEmpresaNit(),
                        libroDiariocontainer,
                        Utileria.getFechaDDMMYYYY(inicioDt.getValue()),
                        Utileria.getFechaDDMMYYYY(finDt.getValue()),
                        folioTxt.getValue()
                );
        mainUI.addWindow(libroDiarioPdf);
        libroDiarioPdf.center();
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - Libro diario");
    }
}
