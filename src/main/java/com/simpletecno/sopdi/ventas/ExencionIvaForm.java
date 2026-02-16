package com.simpletecno.sopdi.ventas;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.simpletecno.sopdi.compras.IngresoDocumentosView;
import com.simpletecno.sopdi.utilerias.ValidarTokenForm;
import com.simpletecno.sopdi.contabilidad.TransaccionesEspecialesView;
import com.simpletecno.sopdi.extras.custom.SegmentedField;
import com.simpletecno.sopdi.extras.custom.ToggleSwitch;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import guatefac.Guatefac;
import guatefac.SimpleGuatefacService;
import org.vaadin.ui.NumberField;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.ConnectException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExencionIvaForm extends Window {

    static final String NIT_PROPERTY = "NIT";

    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");

    VerticalLayout mainLayout = new VerticalLayout();

    public IndexedContainer containerPartida = new IndexedContainer();
    Grid partidaGrid;
    Grid.FooterRow footerPartida;

    public IndexedContainer containerExencion = new IndexedContainer();
    Grid exencionGrid;
    Grid.FooterRow footerExencion;

    UI mainUI;

    ComboBox empresaCbx;

    Statement stQuery, stQuery2, stQuery3;
    ResultSet rsRecords, rsRecords2, rsRecords3;
    String queryString;
    
    String xmlRequest;
    String xmlResponse;
    String serie;
    String numero;
    String uuid = ""; // Numero Autorizacion
    String fechaYHoraCertificacion = "";
    String nombre = "";
    String direccion = "";
    String telefono = "";
    File pdfFile = null;

    NumberField montoTxt;
    NumberField tasaCambioTxt;
    DateField fechaDt;

    SegmentedField uuidField;

    TextField serieTxt;
    TextField numeroTxt;
    TextField nitProveedotTxt;
    TextField documentoAfectaTxt;

    ComboBox proveedorCbx;
    ComboBox monedaCbx;
    ComboBox tipoDocumentoCbx;
    ComboBox tipoIdentificacionCbx;

    Button guardarBtn;
    Button salirBtn;

    ToggleSwitch toggleSwitch;

    String empresa;
    String codigoPartida;
    String tipoDocumento;
    String codigoCCFactura;

    BigDecimal totalDebe;
    BigDecimal totalHaber;

    String idNomenclaturaDebe = "";
    String idNomenclaturaHaber = "";

    BigDecimal montoDocumento;
    String monedaDocumento;
    String serieDocumento;
    String numeroDocumento;

    int idProductoExencion;
    String tipoProductoExencion;

    String variableTemp = "";

    public ExencionIvaForm(String empresa, String codigoPartida, String tipoDocumento) {

        this.empresa = empresa;
        this.codigoPartida = this.codigoCCFactura = codigoPartida;
        this.tipoDocumento = tipoDocumento;
        this.mainUI = UI.getCurrent();

        setResponsive(true);
        setModal(true);
        setWidth("90%");
        setHeight("90%");

        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);
        mainLayout.setWidth("95%");
        setContent(mainLayout);

        HorizontalLayout layoutTitle;
        layoutTitle = new HorizontalLayout();
        layoutTitle.addStyleName("rcorners3");
        layoutTitle.setSpacing(true);
        layoutTitle.setMargin(true);
        layoutTitle.setWidth("100%");

        Label titleLbl = new Label("EXENCIÓN IVA");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h2_custom");

        empresaCbx = new ComboBox("EMPRESA :");
        empresaCbx.setStyleName(ValoTheme.COMBOBOX_HUGE);
        empresaCbx.setWidth("90%");
        empresaCbx.setInvalidAllowed(false);
        empresaCbx.setNewItemsAllowed(false);
        empresaCbx.setTextInputAllowed(false);
        empresaCbx.setNullSelectionAllowed(false);
        llenarComboEmpresa();
        empresaCbx.select(empresa);
        empresaCbx.setReadOnly(true);

        layoutTitle.addComponent(empresaCbx);
        layoutTitle.setComponentAlignment(empresaCbx, Alignment.MIDDLE_LEFT);

        layoutTitle.addComponent(titleLbl);
        layoutTitle.setComponentAlignment(titleLbl, Alignment.BOTTOM_RIGHT);

        mainLayout.addComponent(layoutTitle);
        mainLayout.setComponentAlignment(layoutTitle, Alignment.MIDDLE_CENTER);

        crearEncabezado();
        llenarComboProveedor();
        crearTablaPartida();
        crearTablaExencion();
        crearPieDePagina();

        generarTablaPartida(codigoPartida);
    }

    public void crearEncabezado() {

        VerticalLayout todoEncabezado = new VerticalLayout();
        todoEncabezado.addStyleName("rcorners3");
        todoEncabezado.setSpacing(true);

        tipoDocumentoCbx = new ComboBox("T. Documento");
        tipoDocumentoCbx.setWidth("16em");
        tipoDocumentoCbx.addItem("EXENCIÓN IVA");
        tipoDocumentoCbx.select("EXENCIÓN IVA");
        tipoDocumentoCbx.setEnabled(false);

        documentoAfectaTxt = new TextField("Documento Afecta :");
        documentoAfectaTxt.setWidth("14em");

        fechaDt = new DateField("Fecha : ");
        fechaDt.setDateFormat("dd/MM/yyyy");
        fechaDt.setWidth("8em");
        fechaDt.setValue(new java.util.Date());

        serieTxt = new TextField("Serie : ");
        serieTxt.setWidth("6em");
        serieTxt.addStyleName("mayusculas");

        numeroTxt = new TextField("Número : ");
        numeroTxt.setWidth("8em");

        proveedorCbx = new ComboBox("Proveedores");
        proveedorCbx.setWidth("28em");
        proveedorCbx.addContainerProperty(NIT_PROPERTY, String.class, "");
        proveedorCbx.setInvalidAllowed(false);
        proveedorCbx.setNewItemsAllowed(false);
        proveedorCbx.setFilteringMode(FilteringMode.CONTAINS);
        proveedorCbx.setInvalidAllowed(false);
        proveedorCbx.setNewItemsAllowed(false);
        proveedorCbx.setNullSelectionAllowed(false);
        proveedorCbx.addValueChangeListener(event -> {
            if (event.getProperty() == null) {
                return;
            }
            verificarProveedor();
        });
        nitProveedotTxt = new TextField("Nit : ");
        nitProveedotTxt.setWidth("10em");
        nitProveedotTxt.addValueChangeListener(event
                -> buscarProveedorPorNit()
        );

        monedaCbx = new ComboBox("Moneda :");
        monedaCbx.addItem("QUETZALES");
        monedaCbx.addItem("DOLARES");
        monedaCbx.select("QUETZALES");
        monedaCbx.setWidth("11em");
        monedaCbx.addValueChangeListener(evet -> {
            if (monedaCbx.getValue() != null) {
                if (monedaCbx.getValue().equals("DOLARES")) {
                    tasaCambioTxt.setValue((Float.toString(((SopdiUI) UI.getCurrent()).sessionInformation.getFltExchangeRate())));
                } else {
                    tasaCambioTxt.setValue(1.00);
                }
            }
        });
        monedaCbx.select(monedaDocumento);

        montoTxt = new NumberField("Monto :");
        montoTxt.setWidth("9em");
        montoTxt.setDecimalAllowed(true);
        montoTxt.setDecimalPrecision(2);
        montoTxt.setMinimumFractionDigits(2);
        montoTxt.setDecimalSeparator('.');
        montoTxt.setDecimalSeparatorAlwaysShown(true);
        montoTxt.setValue(0d);
        montoTxt.setGroupingUsed(true);
        montoTxt.setGroupingSeparator(',');
        montoTxt.setGroupingSize(3);
        montoTxt.setImmediate(true);
        montoTxt.setSelectionRange(0, 4);
        montoTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        montoTxt.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                generarTablaExencion();
            }
        });

        tasaCambioTxt = new NumberField("Tipo Cambio : ");
        tasaCambioTxt.setDecimalAllowed(true);
        tasaCambioTxt.setDecimalPrecision(5);
        tasaCambioTxt.setMinimumFractionDigits(5);
        tasaCambioTxt.setDecimalSeparator('.');
        tasaCambioTxt.setDecimalSeparatorAlwaysShown(true);
        tasaCambioTxt.setGroupingUsed(true);
        tasaCambioTxt.setGroupingSeparator(',');
        tasaCambioTxt.setGroupingSize(3);
        tasaCambioTxt.setImmediate(true);
        tasaCambioTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        tasaCambioTxt.setWidth("6em");
        tasaCambioTxt.setValue(1.00);

        uuidField = new SegmentedField(new int[]{8, 4, 4, 4, 12});
        uuidField.setCaption("UUID");
        /*layout.addComponent(uuidField);

        Button b = new Button("Ver UUID", e -> {
            String valor = uuidField.getValue();
            Notification.show("UUID capturado: " + valor);
        });
        layout.addComponent(b);*/


        toggleSwitch = new ToggleSwitch("Agregar", "Crear", event -> {
            boolean value = (boolean) event.getProperty().getValue();
            numeroTxt.setEnabled(!value);
            serieTxt.setEnabled(!value);
        });

        toggleSwitch.setValue(true);

        HorizontalLayout layoutEncabezado = new HorizontalLayout();
        layoutEncabezado.setSpacing(true);

        HorizontalLayout layoutEncabezado2 = new HorizontalLayout();
        layoutEncabezado2.setSpacing(true);

        layoutEncabezado.addComponents(tipoDocumentoCbx, fechaDt, serieTxt, numeroTxt, nitProveedotTxt, proveedorCbx);

        layoutEncabezado2.addComponents(monedaCbx, montoTxt, tasaCambioTxt, documentoAfectaTxt, uuidField, toggleSwitch);
        layoutEncabezado2.setComponentAlignment(toggleSwitch, Alignment.BOTTOM_CENTER);

        todoEncabezado.addComponent(layoutEncabezado);
        todoEncabezado.setComponentAlignment(layoutEncabezado, Alignment.MIDDLE_CENTER);
        todoEncabezado.addComponent(layoutEncabezado2);
        todoEncabezado.setComponentAlignment(layoutEncabezado2, Alignment.MIDDLE_CENTER);

        mainLayout.addComponent(todoEncabezado);
        mainLayout.setComponentAlignment(todoEncabezado, Alignment.MIDDLE_CENTER);

    }

    public void llenarComboEmpresa() {
        queryString = " SELECT * FROM contabilidad_empresa";
        queryString += " Where IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords2 = stQuery.executeQuery(queryString);

            while (rsRecords2.next()) { //  encontrado
                empresaCbx.addItem(rsRecords2.getString("IdEmpresa"));
                empresaCbx.setItemCaption(rsRecords2.getString("IdEmpresa"), rsRecords2.getString("Empresa"));
            }

            rsRecords2.first();
            empresaCbx.select(rsRecords2.getString("IdEmpresa"));

        } catch (Exception ex1) {
            System.out.println("Error al listar cuentas a cargar: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void verificarProveedor() {
        if (nitProveedotTxt == null) {
            return;
        }
        if (proveedorCbx == null) {
            return;
        }
        if (proveedorCbx.getValue() == null) {
            return;
        }

        try {
            Integer.valueOf(String.valueOf(proveedorCbx.getValue()));
        } catch (Exception strE) {
            return;
        }

        nitProveedotTxt.setValue("");

        nitProveedotTxt.setValue(String.valueOf(proveedorCbx.getItem(proveedorCbx.getValue()).getItemProperty(NIT_PROPERTY).getValue()));

    }

    private void buscarProveedorPorNit() {
        for (Iterator<?> i = proveedorCbx.getItemIds().iterator(); i.hasNext();) {
            String id = (String) i.next();
            Item item = proveedorCbx.getItem(id);

            if (nitProveedotTxt.getValue().equals(String.valueOf(item.getItemProperty("NIT").getValue()).trim())) {
                proveedorCbx.select(id);
                break;
            }
        }
    }

    public void crearTablaPartida() {

        VerticalLayout reportLayout = new VerticalLayout();
        reportLayout.setWidth("75%");
        reportLayout.addStyleName("rcorners3");
        reportLayout.setResponsive(true);
        reportLayout.setMargin(new MarginInfo(false, false, false, false));


        containerPartida.addContainerProperty("CUENTA", String.class, null);
        containerPartida.addContainerProperty("DEBE", String.class, null);
        containerPartida.addContainerProperty("HABER", String.class, null);

        partidaGrid = new Grid("Partida Documento : " + documentoAfectaTxt.getValue(), containerPartida);

        partidaGrid.setImmediate(true);
        partidaGrid.setSelectionMode(Grid.SelectionMode.NONE);
        partidaGrid.setDescription("Seleccione un registro para ingresar o editar.");
        partidaGrid.setHeightMode(HeightMode.ROW);
        partidaGrid.setHeightByRows(3);
        partidaGrid.setWidth("98%");
        partidaGrid.getColumn("CUENTA").setEditorField(getComboCuentas());
        partidaGrid.getColumn("DEBE").setEditorField(getAmmountField("DEBE"));
        partidaGrid.getColumn("HABER").setEditorField(getAmmountField("HABER"));

        partidaGrid.getColumn("CUENTA").setEditable(true).setExpandRatio(2);
        partidaGrid.getColumn("DEBE").setEditable(true).setExpandRatio(1);
        partidaGrid.getColumn("HABER").setEditable(true).setExpandRatio(1);

        reportLayout.addComponent(partidaGrid);
        reportLayout.setComponentAlignment(partidaGrid, Alignment.MIDDLE_CENTER);

        partidaGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if ("DEBE".equals(cellReference.getPropertyId()) || "HABER".equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }
        });

        footerPartida = partidaGrid.appendFooterRow();
        footerPartida.getCell("CUENTA").setText("SUMAS IGUALES");
        footerPartida.getCell("DEBE").setText("0.00");
        footerPartida.getCell("HABER").setText("0.00");
        footerPartida.getCell("CUENTA").setStyleName("rightalign");
        footerPartida.getCell("DEBE").setStyleName("rightalign");
        footerPartida.getCell("HABER").setStyleName("rightalign");

        partidaGrid.setFooterVisible(true);

        mainLayout.addComponent(reportLayout);
        mainLayout.setComponentAlignment(reportLayout, Alignment.TOP_CENTER);

    }

    public void crearTablaExencion() {

        VerticalLayout reportLayout = new VerticalLayout();
        reportLayout.setWidth("75%");
        reportLayout.addStyleName("rcorners3");
        reportLayout.setResponsive(true);
        reportLayout.setMargin(new MarginInfo(false, false, false, false));


        containerExencion.addContainerProperty("CUENTA", String.class, null);
        containerExencion.addContainerProperty("DEBE", String.class, null);
        containerExencion.addContainerProperty("HABER", String.class, null);

        exencionGrid = new Grid("Partida Exención :", containerExencion);

        exencionGrid.setImmediate(true);
        exencionGrid.setSelectionMode(Grid.SelectionMode.NONE);
        exencionGrid.setDescription("Seleccione un registro para ingresar o editar.");
        exencionGrid.setHeightMode(HeightMode.ROW);
        exencionGrid.setHeightByRows(2);
        exencionGrid.setWidth("98%");
        exencionGrid.setResponsive(true);
        exencionGrid.setEditorBuffered(false);
        exencionGrid.setEditorEnabled(true);
        exencionGrid.getColumn("CUENTA").setEditorField(getComboCuentas());
        exencionGrid.getColumn("DEBE").setEditorField(getAmmountField("DEBE"));
        exencionGrid.getColumn("HABER").setEditorField(getAmmountField("HABER"));
        exencionGrid.addItemClickListener((event) -> {
            if (event != null) {
                exencionGrid.editItem(event.getItemId());
            }
        });

        exencionGrid.getColumn("CUENTA").setEditable(true).setExpandRatio(2);
        exencionGrid.getColumn("DEBE").setEditable(true).setExpandRatio(1);
        exencionGrid.getColumn("HABER").setEditable(true).setExpandRatio(1);

        reportLayout.addComponent(exencionGrid);
        reportLayout.setComponentAlignment(exencionGrid, Alignment.MIDDLE_CENTER);

        exencionGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if ("DEBE".equals(cellReference.getPropertyId()) || "HABER".equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }
        });

        footerExencion = exencionGrid.appendFooterRow();
        footerExencion.getCell("CUENTA").setText("SUMAS IGUALES");
        footerExencion.getCell("DEBE").setText("0.00");
        footerExencion.getCell("HABER").setText("0.00");
        footerExencion.getCell("CUENTA").setStyleName("rightalign");
        footerExencion.getCell("DEBE").setStyleName("rightalign");
        footerExencion.getCell("HABER").setStyleName("rightalign");

        exencionGrid.setFooterVisible(true);

        mainLayout.addComponent(reportLayout);
        mainLayout.setComponentAlignment(reportLayout, Alignment.TOP_CENTER);

    }

    private Field<?> getComboCuentas() {

        ComboBox comboBox = new ComboBox();
        comboBox.setImmediate(true);
        comboBox.setNullSelectionAllowed(false);
        comboBox.setWidth("15em");
        comboBox.setNewItemsAllowed(false);
        comboBox.setFilteringMode(FilteringMode.CONTAINS);

        queryString = " SELECT * from contabilidad_nomenclatura";
        queryString += " where Estatus = 'HABILITADA'";
        queryString += " Order By N5";

        try {
            stQuery2 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords2 = stQuery2.executeQuery(queryString);

            while (rsRecords2.next()) { //  encontrado
                comboBox.addItem(rsRecords2.getString("IdNomenclatura") + " " + rsRecords2.getString("NoCuenta") + " " + rsRecords2.getString("N5"));
            }

        } catch (Exception ex1) {
            System.out.println("Error al combo cuentas contables: " + ex1.getMessage());
            ex1.printStackTrace();
        }

        return comboBox;
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
                        setFooterTotal(propertyId);
                    }
                }
            }
        });
        return valueTxt;
    }

    private void setFooterTotal(String propertyId) {
        double total;
        total = 0.00;
        for (Object itemId : containerExencion.getItemIds()) {
            Item item = containerExencion.getItem(itemId);
            Object propertyValue = item.getItemProperty(propertyId).getValue();
            if(propertyValue != null)
                total += Double.valueOf(String.valueOf(propertyValue)).doubleValue();
        }
        if (footerExencion != null) {
            footerExencion.getCell(propertyId).setText(numberFormat.format(total));
        }
    }

    public void llenarComboProveedor() {
        queryString = " SELECT * from proveedor ";
        queryString += " WHERE Inhabilitado = 0 ";
        queryString += " AND EsProveedor = 1";
        queryString += " Order By Nombre ";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            proveedorCbx.addItem("0");
            proveedorCbx.setItemCaption("0", "");
            proveedorCbx.getItem("0").getItemProperty("NIT").setValue("");

            while (rsRecords.next()) {
                proveedorCbx.addItem(rsRecords.getString("IDProveedor"));
                proveedorCbx.setItemCaption(rsRecords.getString("IDProveedor"), rsRecords.getString("Nombre"));
                proveedorCbx.getItem(rsRecords.getString("IDProveedor")).getItemProperty(NIT_PROPERTY).setValue(rsRecords.getString("NIT"));
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar Proveedores error : " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void generarTablaPartida(String codigoCC) {

        containerPartida.removeAllItems();

        footerPartida.getCell("DEBE").setText("0.00");
        footerPartida.getCell("HABER").setText("0.00");

        queryString = "SELECT *, cn.N5, cn.NoCuenta, dfs.NumeroAutorizacion ";
        queryString += "FROM contabilidad_partida cp ";
        queryString += "INNER JOIN contabilidad_nomenclatura cn ON cp.IdNomenclatura = cn.IdNomenclatura ";
        queryString += "LEFT JOIN documentos_fel_sat dfs ON cp.NumeroDocumento = dfs.Numero ";
        queryString += "WHERE cp.CodigoPartida = '" + codigoCC + "'";
        //queryString += "AND contabilidad_nomenclatura.IdNomenclatura IN ( " + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getProveedores() + ", " + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getAnticiposProveedor() + ") ";

        try {
            double debe = 0.00;
            double haber = 0.00;
            boolean haberStop = false;
            boolean debeStop = false;

            stQuery3 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords3 = stQuery3.executeQuery(queryString);

            boolean temp = true;
            if (rsRecords3.next()) {
                do {
                    if( rsRecords3.getDouble("Debe") > 0 && !debeStop) {
                        idNomenclaturaDebe = rsRecords3.getString("IdNomenclatura");
                        debeStop = true;
                    }
                    if( rsRecords3.getDouble("Haber") > 0 && !haberStop) {
                        idNomenclaturaHaber = rsRecords3.getString("IdNomenclatura");
                        haberStop = true;
                    }
                    if(rsRecords3.getString("IdNomenclatura").equals(((SopdiUI)mainUI).cuentasContablesDefault.getIvaPorCobrar())){
                        idNomenclaturaDebe = rsRecords3.getString("IdNomenclatura");
                        debeStop = true;
                    }
                    proveedorCbx.setValue(rsRecords3.getString("IdProveedor"));
                    proveedorCbx.select(rsRecords3.getString("IdProveedor"));
                    nitProveedotTxt.setValue(String.valueOf(proveedorCbx.getContainerProperty(proveedorCbx.getValue(), "NIT").getValue()));

                    monedaCbx.select(rsRecords3.getString("MonedaDocumento"));
                    monedaCbx.setReadOnly(true);

                    documentoAfectaTxt.setValue(rsRecords3.getString("SerieDocumento") + " " + rsRecords3.getString("NumeroDocumento"));
                    documentoAfectaTxt.setReadOnly(true);

                    uuidField.setValue(rsRecords3.getString("NumeroAutorizacion"));

                    Object itemId = containerPartida.addItem();

                    containerPartida.getContainerProperty(itemId, "CUENTA").setValue(getDescripcionCuentas(rsRecords3.getString("IdNomenclatura")));

                    containerPartida.getContainerProperty(itemId, "DEBE").setValue(numberFormat.format(rsRecords3.getDouble("Debe")));
                    containerPartida.getContainerProperty(itemId, "HABER").setValue(numberFormat.format(rsRecords3.getDouble("Haber")));
                    debe += rsRecords3.getDouble("Debe");
                    haber += rsRecords3.getDouble("Haber");

                    if(temp && !idNomenclaturaHaber.isEmpty() && !idNomenclaturaDebe.isEmpty()) {
                        monedaDocumento = rsRecords3.getString("MonedaDocumento");
                        montoDocumento = rsRecords3.getBigDecimal("MontoDocumento");
                        montoTxt.setValue(Utileria.format((rsRecords3.getDouble("MontoDocumento") / 1.12) * .12));
                        serieDocumento = rsRecords3.getString("SerieDocumento");
                        numeroDocumento = rsRecords3.getString("NumeroDocumento");
                        temp = false;
                    }

                } while (rsRecords3.next());
            }

            footerPartida.getCell("DEBE").setText(numberFormat.format(debe));
            footerPartida.getCell("HABER").setText(numberFormat.format(haber));

        } catch (Exception ex1) {
            System.out.println("Error al listar cuentas contables: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void generarTablaExencion() {

        containerExencion.removeAllItems();

        footerExencion.getCell("DEBE").setText("0.00");
        footerExencion.getCell("HABER").setText("0.00");

        try {
            double debe = 0.00;
            double haber = 0.00;

            Object itemId = containerExencion.addItem();

            containerExencion.getContainerProperty(itemId, "CUENTA").setValue(getDescripcionCuentas(idNomenclaturaHaber));

            containerExencion.getContainerProperty(itemId, "DEBE").setValue(numberFormat.format(Double.parseDouble(montoTxt.getValue())));
            containerExencion.getContainerProperty(itemId, "HABER").setValue("0.00");
            debe = Double.parseDouble(montoTxt.getValue());

            itemId = containerExencion.addItem();

            containerExencion.getContainerProperty(itemId, "CUENTA").setValue(getDescripcionCuentas(idNomenclaturaDebe));

            containerExencion.getContainerProperty(itemId, "DEBE").setValue("0.00");
            containerExencion.getContainerProperty(itemId, "HABER").setValue(numberFormat.format(Double.parseDouble(montoTxt.getValue())));
            haber = Double.parseDouble(montoTxt.getValue());

            footerExencion.getCell("DEBE").setText(numberFormat.format(debe));
            footerExencion.getCell("HABER").setText(numberFormat.format(haber));

        } catch (Exception ex1) {
            System.out.println("Error al listar cuentas contables: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void crearPieDePagina(){
        salirBtn = new Button("Salir");
        salirBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        salirBtn.setIcon(FontAwesome.ARROW_RIGHT);
        salirBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();
            }
        });

        guardarBtn = new Button("Guardar");
        guardarBtn.setIcon(FontAwesome.SAVE);
        guardarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                insertTablaFactura();
            }
        });

        HorizontalLayout layoutButtons = new HorizontalLayout();
        layoutButtons.setSpacing(true);

        layoutButtons.addComponent(salirBtn);
        layoutButtons.setComponentAlignment(salirBtn, Alignment.MIDDLE_LEFT);
        layoutButtons.addComponent(guardarBtn);
        layoutButtons.setComponentAlignment(guardarBtn, Alignment.MIDDLE_CENTER);

        mainLayout.addComponent(layoutButtons);
        mainLayout.setComponentAlignment(layoutButtons, Alignment.MIDDLE_CENTER);
    }

    private String getDescripcionCuentas(String idNomenclatura) {

        queryString = " SELECT * from contabilidad_nomenclatura";
        queryString += " where IdNomenclatura = " + idNomenclatura;

        try {
            stQuery2 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords2 = stQuery2.executeQuery(queryString);

            queryString = "<<cuenta contable no encontrada, revise cuentas por default...>>";

            if(rsRecords2.next()) { //  encontrado
                queryString  = rsRecords2.getString("IdNomenclatura") + " " + rsRecords2.getString("NoCuenta") + " " + rsRecords2.getString("N5");
            }

        } catch (Exception ex1) {
            System.out.println("Error al combo cuentas contables: " + ex1.getMessage());
            ex1.printStackTrace();
        }

        return queryString;
    }

    private boolean datosValidos() {
        try {
            Utileria utileria = new Utileria();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

            Date fechaInicial = dateFormat.parse(utileria.getFecha());
            Date fechaFinal = dateFormat.parse(utileria.getFechaYYYYMMDD(fechaDt.getValue()));

            int dias = (int) ((fechaInicial.getTime() - fechaFinal.getTime()) / 86400000);

//            System.out.println("Hay " + dias + " dias de diferencia");

            if (dias > 30) {

                if (((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserToken().isEmpty()) {
                    ValidarTokenForm validarTokenForm = new ValidarTokenForm(false);
                    UI.getCurrent().addWindow(validarTokenForm);
                    validarTokenForm.center();
                    return false;
                } else {
                    variableTemp = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserToken();
                    ((SopdiUI) UI.getCurrent()).sessionInformation.setStrUserToken("");
                }
            }

        } catch (Exception e) {
            System.out.println("Error validar fechas " + e);
            e.printStackTrace();
            return false;
        }

        if (((SopdiUI) UI.getCurrent()).esMesCerrado(String.valueOf(empresaCbx.getValue()), Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()))) {
            Notification.show("La fecha del documento no puede ser de un mes ya cerrado contablemente, revise!", Notification.Type.WARNING_MESSAGE);
            fechaDt.focus();
            return false;
        }
        if (!((SopdiUI) UI.getCurrent()).esPrimerMesAbierto(String.valueOf(empresaCbx.getValue()), Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()))) {
            Notification.show("El mes abierto a operaciones es : " + ((SopdiUI) UI.getCurrent()).primerMesAbierto(String.valueOf(empresaCbx.getValue())), Notification.Type.WARNING_MESSAGE);
            fechaDt.focus();
            return false;
        }
        if(toggleSwitch.getValue()) {
            if (((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyFelUser().trim().isEmpty()) {
                if (this.serieTxt.getValue().trim().isEmpty()) {
                    Notification.show("Por favor ingrese la serie del documento.", Notification.Type.WARNING_MESSAGE);
                    serieTxt.focus();
                    return false;
                }
                if (this.numeroTxt.getValue().trim().isEmpty()) {
                    Notification.show("Por favor ingrese el número de documento.", Notification.Type.WARNING_MESSAGE);
                    numeroTxt.focus();
                    return false;
                }
            }
        }
        if (proveedorCbx.getValue() == null || proveedorCbx.getValue().equals("0")) {
            Notification.show("Por favor ingrese el cliente.", Notification.Type.WARNING_MESSAGE);
            proveedorCbx.focus();
            return false;
        }
        if (nitProveedotTxt.isEmpty()) {
            Notification.show("Por favor ingrese el Nit del cliente.", Notification.Type.WARNING_MESSAGE);
            nitProveedotTxt.focus();
            return false;
        }
        if (monedaCbx.getValue() == null) {
            Notification.show("Por favor ingrese el tipo de moneda.", Notification.Type.WARNING_MESSAGE);
            monedaCbx.focus();
            return false;
        }

        if (this.montoTxt.getDoubleValueDoNotThrow() == 0) {
            Notification.show("Por favor ingrese el monto de la Exención.", Notification.Type.WARNING_MESSAGE);
            montoTxt.focus();
            return false;
        }

        if (!uuidField.isValid()) {
            Notification.show("Por favor ingrese el UUID del documento.", Notification.Type.WARNING_MESSAGE);
            uuidField.focus();
            return false;
        }

        if (!toggleSwitch.getValue() && (serieTxt.getValue().isEmpty() || serieTxt.getValue() == null)) {
            Notification.show("Por favor ingrese la Serie de la Exención.", Notification.Type.WARNING_MESSAGE);
            serieTxt.focus();
            return false;
        }

        if (!toggleSwitch.getValue() && (numeroTxt.getValue().isEmpty() || numeroTxt.getValue() == null)) {
            Notification.show("Por favor ingrese la Numero de la Exención.", Notification.Type.WARNING_MESSAGE);
            numeroTxt.focus();
            return false;
        }

        if(!toggleSwitch.getValue()) {
            queryString = " Select * from contabilidad_partida";
            queryString += " Where SerieDocumento  = '" + serieTxt.getValue().toUpperCase().trim() + "'";
            queryString += " And   NumeroDocumento = '" + numeroTxt.getValue().toUpperCase().trim() + "'";
//        queryString += " And   IdProveedor     =  " + String.valueOf(clienteCbx.getValue());

            queryString += " And   TipoDocumento = 'EXENCION IVA'";
            queryString += " And   IdEmpresa = " + empresaCbx.getValue();

            try {
                rsRecords = stQuery.executeQuery(queryString);

                if (rsRecords.next()) {
                    Notification.show("Este documento ya fué ingresado, revise.!. Empresa = " + rsRecords.getString("IdEmpresa") + " Fecha : " + rsRecords.getString("Fecha"), Notification.Type.WARNING_MESSAGE);
                    numeroTxt.focus();
                    return false;
                }
            } catch (Exception ex1) {
                System.out.println("Error al buscar documento : " + ex1.getMessage());
                ex1.printStackTrace();
                return false;
            }
        }

        queryString = " Select * from contabilidad_partida";
        queryString += " Where UUIDDoca  = '" + uuidField.getValue().toUpperCase().trim() + "'";
        queryString += " And   TipoDocumento = 'EXENCION IVA'";
        queryString += " And   IdEmpresa = " + empresaCbx.getValue();

        try {
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                Notification.show("Este documento ya fué ingresado, revise.!. Empresa = " + rsRecords.getString("IdEmpresa") + " Fecha : " + rsRecords.getString("Fecha"), Notification.Type.WARNING_MESSAGE);
                numeroTxt.focus();
                return false;
            }
        } catch (Exception ex1) {
            System.out.println("Error al buscar documento : " + ex1.getMessage());
            ex1.printStackTrace();
            return false;
        }


        return true;
    }

    public void updatePartidaTransaccion() {

        if(tasaCambioTxt.getDoubleValueDoNotThrow() == 0 && monedaCbx.getValue().equals("DOLARES")){
            Notification.show("Tasa de Cambio igual a 0.0", Notification.Type.WARNING_MESSAGE);
            return;
        }

        totalDebe = new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP);
        totalHaber = new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP);

        for (Object itemId : containerExencion.getItemIds()) {
            Item item = containerExencion.getItem(itemId);
            String debeValue = item.getItemProperty("DEBE").getValue().toString().replaceAll(",","");
            String haberValue = item.getItemProperty("HABER").getValue().toString().replaceAll(",","");
            totalDebe = totalDebe.add(new BigDecimal(Double.valueOf(debeValue)).setScale(2, BigDecimal.ROUND_HALF_UP));
            totalHaber = totalHaber.add(new BigDecimal(Double.valueOf(haberValue)).setScale(2, BigDecimal.ROUND_HALF_UP));

        }

        //       if (((SopdiUI) UI.getCurrent()).esMesCerrado(String.valueOf(empresaCbx.getValue()), Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()))) {
        //           Notification.show("La fecha del documento no puede ser de un mes ya cerrado contablemente, revise!", Notification.Type.WARNING_MESSAGE);
        //           fechaDt.focus();
        //           return;
        //       }
        //       if (!((SopdiUI) UI.getCurrent()).esPrimerMesAbierto(String.valueOf(empresaCbx.getValue()), Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()))) {
        //           Notification.show("El mes abierto a operaciones es : " + ((SopdiUI) UI.getCurrent()).primerMesAbierto(String.valueOf(empresaCbx.getValue())), Notification.Type.WARNING_MESSAGE);
        //           fechaDt.focus();
        //           return;
//        }
        if (totalDebe.doubleValue() != totalHaber.doubleValue()) {
            Notification.show("Partida está descuadrada, por favor revisar."
                    + " Debe=" + totalDebe.doubleValue() + " Haber=" + totalHaber.doubleValue(), Notification.Type.ERROR_MESSAGE);
            return;
        }
        if (totalDebe.doubleValue() == 0.00 && totalHaber.doubleValue() == 0.00) {
            Notification.show("Partida NO está completa DEBE y HABER no tienen valores validos, por favor revisar.", Notification.Type.ERROR_MESSAGE);
            System.out.println("totalDebe=" + totalDebe.doubleValue() + " totalHaber=" + totalHaber.doubleValue());
            return;
        }

        /// nueva partida

        String fecha = Utileria.getFechaYYYYMMDD_1(fechaDt.getValue());
        String ultimoEncontado;
        String dia = fecha.substring(8, 10);
        String mes = fecha.substring(5, 7);
        String año = fecha.substring(0, 4);

        codigoPartida = String.valueOf(empresaCbx.getValue()) + año + mes + dia + "6";

        queryString = " select codigoPartida from contabilidad_partida ";
        queryString += " where codigoPartida like '" + codigoPartida + "%'";
        queryString += " order by codigoPartida desc ";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado

                ultimoEncontado = rsRecords.getString("codigoPartida").substring(12, 15);

                codigoPartida += String.format("%03d", (Integer.valueOf(ultimoEncontado) + 1));

            } else {
                codigoPartida += "001";
            }

        } catch (Exception ex1) {
            System.out.println("Error al buscar el ultimo codigoPartida" + ex1.getMessage());
            ex1.printStackTrace();
        }

        try {
            queryString = "Insert Into contabilidad_partida (IdEmpresa, CodigoPartida, CodigoCC, ";
            queryString += "Fecha, Descripcion, TipoDocumento, IdNomenclatura, IdProveedor, NITProveedor, NombreProveedor, ";
            queryString += "SerieDocumento, NumeroDocumento, TipoDOCA, NoDoca, UUIDDoca, MonedaDocumento, TipoCambio, ";
            queryString += "MontoDocumento, Debe, Haber, DebeQuetzales, HaberQuetzales, Estatus ,CreadoUsuario, CreadoFechaYHora, ";
            queryString += "UUID, FechaYHoraCertificacion,  XmlRequest, XmlResponse, IdProducto) ";
            queryString += "Values ";

            int contador = 0;

            for (Object itemId : containerExencion.getItemIds()) {
                Item item = containerExencion.getItem(itemId);
                if (Double.valueOf(String.valueOf(item.getItemProperty("DEBE").getValue())) == 0
                        && Double.valueOf(String.valueOf(item.getItemProperty("HABER").getValue())) == 0) {
                    continue;
                }

                queryString += "(";
                queryString += empresaCbx.getValue();                                               //IdEmpresa
                queryString += ",'" + codigoPartida + "'";                                          //CodigoPartida
                queryString += ",'" + codigoCCFactura + "'";                                        //CodigoCC
                queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";        //Fecha
                                                                                            //Descripcion
                queryString += ",'" + "EXENCiÓN IVA (N# " + documentoAfectaTxt.getValue() + " " + proveedorCbx.getValue() + " )' ";
                queryString += ",'" + tipoDocumentoCbx.getValue() + "'";                            //TipoDocumento
                                                                                            //IdNomenclatura
                queryString += "," + String.valueOf(item.getItemProperty("CUENTA").getValue()).split(" ")[0];
                queryString += "," + proveedorCbx.getValue();                                       //IdProveedor
                queryString += ",'" + nitProveedotTxt.getValue() + "'";                             //NitProveedor
                queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";   //NombreProveedor
                queryString += ",'" + serie.toUpperCase().trim() + "'";                             //SerieDocumento
                queryString += ",'" + numero.trim() + "'";                                          //NumeroDocumento
                queryString += ",'" + tipoDocumento + "'";                                          //TipoDoca
                queryString += ",'" + documentoAfectaTxt.getValue() + "'";                          //NoDoca
                queryString += ",'" + uuidField.getValue().toUpperCase().trim() + "'";              //UUIDDoca
                queryString += ",'" + monedaCbx.getValue() + "'";                                   //MonedaDocumento
                queryString += "," + tasaCambioTxt.getDoubleValueDoNotThrow();                      //TipoCambio
                queryString += "," + montoTxt.getDoubleValueDoNotThrow();                                      //MontoDocumento
                                                                                            //Debe
                queryString += ", " + item.getItemProperty("DEBE").getValue() + "";
                                                                                            //Haber
                queryString += ", " + item.getItemProperty("HABER").getValue() + "";
                                                                                            //DebeQuetzales
                queryString += "," + Double.valueOf(String.valueOf(item.getItemProperty("DEBE").getValue())) * tasaCambioTxt.getDoubleValueDoNotThrow();
                                                                                            //HaberQuetzales
                queryString += "," + Double.valueOf(String.valueOf(item.getItemProperty("HABER").getValue())) * tasaCambioTxt.getDoubleValueDoNotThrow();

                queryString += ",'INGRESADO'";                                                      //Estatus
                queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();          //CreadoUsuario
                queryString += ",current_timestamp";                                                //CreadoFechaHora
                queryString += ",'" + uuid + "'";                                                   //UUID
                queryString += ",'" + fechaYHoraCertificacion + "'";                                //FechaYHoraCertificacion
                queryString += ",'" + xmlRequest + "'";                                             //XmlRequest
                queryString += ",'" + xmlResponse + "'";                                            //XmlResponse
                queryString += ", " + idProductoExencion + "\n";                                  //IdProducto
                queryString += "),";

                contador = contador + 1;
            }

            queryString = queryString.substring(0, queryString.length() - 1);  //quitar la ultima coma

            System.out.println("\nquery insert exención iva = " + queryString);

            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            Notification notif = new Notification("Registro ingresado exitosamente, por favor espere!!!",
                    Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.WARNING);
            notif.show(Page.getCurrent());

            if (mainUI.getNavigator().getCurrentView().getClass().getSimpleName().equals("TransaccionesEspecialesView")) {
                ((TransaccionesEspecialesView) (mainUI.getNavigator().getCurrentView())).llenarTablaFactura(String.valueOf(empresaCbx.getValue()));
            } else if (mainUI.getNavigator().getCurrentView().getClass().getSimpleName().equals("IngresoDocumentosView")) {
                ((IngresoDocumentosView) (mainUI.getNavigator().getCurrentView())).llenarTablaFactura(String.valueOf(empresaCbx.getValue()), 0);
            } else if (mainUI.getNavigator().getCurrentView().getClass().getSimpleName().equals("FacturaVentaView")) {
                ((FacturaVentaView) (mainUI.getNavigator().getCurrentView())).llenarTablaFacturaVenta(String.valueOf(empresaCbx.getValue()));
            }

            queryString = " Update contabilidad_partida Set ";
            queryString += " Referencia = 'NO'";
            queryString += " Where CodigoPartida = '" + codigoCCFactura + "'";
            queryString += " and IdEmpresa = " + String.valueOf(empresaCbx.getValue());

            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            close();

        } catch (Exception ex1) {
            System.out.println("Error al insertar EXENCIÓN IVA : " + ex1.getMessage());
            Notification.show("Error al insertar la EXENCIÓN IVA : " + ex1.getMessage(), Notification.Type.ERROR_MESSAGE);
            ex1.printStackTrace();
        }
    }

    public boolean documentoCertificado() {

        queryString = "SELECT * from producto_venta_empresa ";
        queryString += "WHERE IdEmpresa = " + empresaCbx.getValue() + " ";
        queryString += "AND Especial = 1 ";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if(rsRecords.next()){
                idProductoExencion = rsRecords.getInt("IdProducto");
                tipoProductoExencion = String.valueOf(rsRecords.getString("Tipo").charAt(0));
            }

        } catch (Exception ex1) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error al combo cuentas contables: ", ex1);
            ex1.printStackTrace();
        }

        //cerGuatefac_Sertificacion FEL
        SimpleGuatefacService guatefacInterface = new SimpleGuatefacService();

        Guatefac guateFacPort = null;
        try {
            guateFacPort = guatefacInterface.createService("usr_guatefac", "usrguatefac");
        } catch (Exception e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "ERROR AL CERTIFICAR LA FACTURA : " + e);
            Notification.show("ERROR AL CONECTAR CON GUATEFACTURA : " + e);
            return false;
        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "URL:" + guatefacInterface.getUrl());
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "NIT:" + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyTaxId());
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Usuario:" + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyFelUser());
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Passwd:" + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyFelPass());

        xmlRequest = "<![CDATA[<DocElectronico>\n" +
                "<Encabezado>\n" +
                    "<Receptor>\n" +
                        "<NITReceptor>" + proveedorCbx.getContainerProperty(proveedorCbx.getValue(), NIT_PROPERTY).getValue().toString().replaceAll("-", "") + "</NITReceptor>\n" +
                        "<Nombre>" + proveedorCbx.getValue() + "</Nombre>\n" +
                        "<Direccion>" + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyBillingDirection() + "</Direccion>\n";


        xmlRequest +=
                    "</Receptor>\n" +
                    "<InfoDoc>\n" +
                        "<TipoVenta>S</TipoVenta>\n" +
                        "<DestinoVenta>1</DestinoVenta>\n" +
                        "<Fecha>" + Utileria.getFechaDDMMYYYY(fechaDt.getValue()) + "</Fecha>\n" +
                        "<Moneda>" + (monedaCbx.getValue().equals("DOLARES")?2:1) + "</Moneda>\n" +
                        "<Tasa>" + (monedaCbx.getValue().equals("DOLARES")?tasaCambioTxt.getDoubleValueDoNotThrow():1)+ "</Tasa>\n" +
                        "<Referencia>" + new Utileria().getReferencia() + "</Referencia>\n" +
                    "</InfoDoc>\n";

        xmlRequest +=
                    "<Totales>\n" +
                        "<Bruto>" + Utileria.format(montoDocumento) + "</Bruto>\n" +
                        "<Descuento>0.00</Descuento>\n" +
                        "<Exento>0.00</Exento>\n" +
                        "<Otros>0.00</Otros>\n" +
                        "<Neto>" + Utileria.format(montoDocumento.subtract(BigDecimal.valueOf(montoTxt.getDoubleValueDoNotThrow()))) + "</Neto>\n" +
                        "<Isr>0.00</Isr>\n" +
                        "<Iva>" + Utileria.format(montoTxt.getDoubleValueDoNotThrow()) + "</Iva>\n" +
                        "<Total>" + Utileria.format(montoDocumento) + "</Total>\n" +
                    "</Totales>\n";

        xmlRequest +=
                    "<DatosAdicionales>\n" +
                        "<RegimenAntiguo>N</RegimenAntiguo>\n" +
                        "<NumAutDocOrigen>" + uuidField.getValue() + "</NumAutDocOrigen>\n" +
                        "<FecEmiDocOrigen>" + Utileria.getFechaDDMMYYYY(fechaDt.getValue()) + "</FecEmiDocOrigen>\n" +
                        "<SerieDocOrigen></SerieDocOrigen>\n" +
                        "<NumeroDocOrigen></NumeroDocOrigen>\n" +
                        "<MontoIVAExento>" + Utileria.format(montoTxt.getDoubleValueDoNotThrow()) + "</MontoIVAExento>\n" +
                        "<MonTotalDocOrigen>" + Utileria.format(montoDocumento) + "</MonTotalDocOrigen>\n" +
                        "<IVATotalDocOrigen>" + Utileria.format(montoDocumento.divide(BigDecimal.valueOf(1.12), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(.12))) + "</IVATotalDocOrigen>\n" +
                    "</DatosAdicionales>\n";

        xmlRequest +=
            "</Encabezado>\n" +
            "<Detalles>\n" +
                "<Productos>\n" +
                    "<ProductoNota>" + idProductoExencion + "</ProductoNota>\n" +
                    "<Descripcion> Exencion Iva Generada por constancia total | " + Utileria.getFechaDDMMYYYY(fechaDt.getValue()) + "</Descripcion>\n" +
                    "<Medida>1</Medida>\n" +
                    "<Cantidad>" + 1 + "</Cantidad>\n" +
                    "<Precio>" + Utileria.format(montoDocumento) + "</Precio>\n" +
                    "<PorcDesc>0.00</PorcDesc>\n" +
                    "<ImpBruto>" + Utileria.format(montoDocumento) + "</ImpBruto>\n" +
                    "<ImpDescuento>0.00</ImpDescuento>\n" +
                    "<ImpExento>0.00</ImpExento>\n" +
                    "<ImpNeto>" + Utileria.format(montoDocumento.subtract(BigDecimal.valueOf(montoTxt.getDoubleValueDoNotThrow()))) + "</ImpNeto>\n"+
                    "<ImpOtros>0.00</ImpOtros>\n" +
                    "<ImpIsr>0.00</ImpIsr>\n" +
                    "<ImpIva>" + Utileria.format(montoTxt.getDoubleValueDoNotThrow()) + "</ImpIva>\n" +
                    "<ImpTotal>" + Utileria.format(montoDocumento) + "</ImpTotal>\n" +
                    "<TipoVentaDet>" + tipoProductoExencion + "</TipoVentaDet>\n" +
                "</Productos>\n" +
            "</Detalles>\n" +
            "</DocElectronico>]]>";

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "xmlRequest:" + xmlRequest);

        // 1 = factura
        // 7 = recibo por donación
        // 8 = recibo
        String tipoDocumentoFel = "17";

        xmlResponse = "<?xml version=\"1.0\"?>" + guateFacPort.generaDocumento(
                ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyFelUser(),
                ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyFelPass(),
                ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyTaxId(),
                new BigDecimal(1), // establecimiento
                new BigDecimal(tipoDocumentoFel), // tipodocumento
                "1",
                "R",
                xmlRequest
        );
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "xmlResponse=" + xmlResponse);

        //Parser that produces DOM object trees from XML content
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setIgnoringElementContentWhitespace(true);
        factory.setNamespaceAware(true);

        //API to obtain DOM Document instance
        DocumentBuilder builder = null;
        try {
            //Create DocumentBuilder with default configuration
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            builder = factory.newDocumentBuilder();

            //Parse the content to Document object
            Document doc = builder.parse(new InputSource(new StringReader(xmlResponse)));
            doc.getDocumentElement().normalize();

            // Respuesta de Guatefactyras
            Node resultado = doc.getFirstChild();
            if (resultado.getChildNodes().getLength() > 2) {
                serie = (resultado.getChildNodes().item(0).getTextContent());          // Serie                | Se Usa en Partida
                numero = (resultado.getChildNodes().item(1).getTextContent());         // Preimpreso           | Se Usa en Partida
                nombre = resultado.getChildNodes().item(2).getTextContent();                    // Nombre
                direccion = resultado.getChildNodes().item(3).getTextContent();                 // Direccion
                telefono = resultado.getChildNodes().item(4).getTextContent();                  // Telefono
                uuid = resultado.getChildNodes().item(5).getTextContent();                      // Numero Autorizacion  | Se Usa en Partida
                fechaYHoraCertificacion = resultado.getChildNodes().item(6).getTextContent();   // Referencia           | Se Usa en Partida

                obtenerFacturaPdf(serie, numero);

            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "ERROR AL CERTIFICAR LA FACTURA : " + resultado.getTextContent());
                Notification.show("ERROR AL CERTIFICAR LA FACTURA  : " + resultado.getTextContent(), Notification.Type.ERROR_MESSAGE);
                return false;
            }

        } catch (Exception e) {
            Notification.show("ERROR AL CERTIFICAR LA FACTURA  : " + e.getMessage());
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "ERROR AL CERTIFICAR LA FACTURA");
            e.printStackTrace();
            return false;
        }

        return true;
//        return false;
    }

    private void obtenerFacturaPdf(String serie, String numero) {

        Utileria utileria = new Utileria();

        long fileSize = 0;
        byte[] ba1 = new byte[1024];
        int baLength;

        try {

            Thread.sleep(1000);

//                URL pdfFileUrl = new URL("https://dte.guatefacturas.com/reports/rwservlet?repfel&report=r65_2170&destype=cache&desformat=pdf&paramform=no&P_NIT_EMISOR=" + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyTaxId() + "&P_UUID=" + referencia);
            String credemciales = "P_NIT_EMISOR=" + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyTaxId() + "&P_SERIE=" + serie + "&P_NUMERO=" + numero + "&P_FECHA=" + utileria.getFechaSinFormato_v2(fechaDt.getValue());
            URL pdfFileUrl = new URL("https://dte.guatefacturas.com/reports/rwservlet?reportesfel&report=r65_0014&destype=cache&desformat=pdf&paramform=no&" + credemciales);
            pdfFile = new File(((SopdiUI) UI.getCurrent()).enviromentsVars.getDtePath() + serie + "_" + numero + ".pdf");
            FileOutputStream fos1 = new FileOutputStream(pdfFile);

            // Contacting the URL
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Connecting to " + pdfFileUrl.toString() + " ... ");
            URLConnection urlConn = pdfFileUrl.openConnection();

            // Checking whether the URL contains a PDF
            if (!urlConn.getContentType().equalsIgnoreCase("application/pdf")) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "ERROR AL OBTENER EL ARCHIVO PDF DE LA FACTUR.");
                Notification.show("ERROR AL OBTENER EL ARCHIVO PDF DE LA FACTURA...", Notification.Type.ERROR_MESSAGE);
            } else {
                try {
                    // Read the PDF from the URL and save to a local file
                    InputStream is1 = pdfFileUrl.openStream();
                    while ((baLength = is1.read(ba1)) != -1) {
                        fos1.write(ba1, 0, baLength);
                    }
                    fos1.flush();
                    fos1.close();
                    is1.close();

                    // Load the PDF document and display its page count
                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, "DONE.Processing the PDF ... ");

                } catch (ConnectException ce) {
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "ERROR AL OBTENER EL ARCHIVO PDF DE LA FACTURA.\n[" + ce.getMessage() + "]\n");
                    Notification.show("ERROR AL OBTENER EL ARCHIVO PDF DE LA FACTURA...", Notification.Type.ERROR_MESSAGE);
                    ce.printStackTrace();
                }
            }

        } catch (Exception exep) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "FAILED.\n[" + exep.getMessage() + "]\n");
            Notification.show("ERROR AL OBTENER EL ARCHIVO PDF DE LA FACTURA...", Notification.Type.ERROR_MESSAGE);
        }
    }

    public void insertTablaFactura() {
        if (datosValidos()) {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "DATOS VALIDOS OK!");
            if (!((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyFelUser().trim().isEmpty() && toggleSwitch.getValue()) {
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "ES FEL!");
                if (documentoCertificado() && toggleSwitch.getValue()) {
                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, "DOCUMENTO CERTIFICDADO FEL OK!");
                    Notification.show("DOCUMENTO CERTIFICADO FEL OK!", Notification.Type.HUMANIZED_MESSAGE);
                } else {
                    return;
                }
            }

            if(!toggleSwitch.getValue()){
                serie = serieTxt.getValue();
                numero = numeroTxt.getValue();
            }

            updatePartidaTransaccion();
        }
    }
}
