package com.simpletecno.sopdi.ventas;

import com.simpletecno.sopdi.utilerias.MyEmailMessanger;
import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.simpletecno.sopdi.compras.IngresoDocumentosView;
import com.simpletecno.sopdi.utilerias.ValidarTokenForm;
import com.simpletecno.sopdi.contabilidad.TransaccionesEspecialesView;
import com.simpletecno.sopdi.extras.custom.SegmentedField;
import com.simpletecno.sopdi.extras.custom.ToggleSwitch;
import com.simpletecno.sopdi.extras.infile.InfileClient;
import com.simpletecno.sopdi.extras.infile.Producto;
import com.simpletecno.sopdi.extras.infile.Receptor;
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
import org.vaadin.ui.NumberField;

import javax.mail.MessagingException;
import java.io.File;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExencionIvaInfileForm extends Window {

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
    InfileClient infileClient;

    ComboBox empresaCbx;

    Statement stQuery, stQuery2, stQuery3;
    ResultSet rsRecords, rsRecords2, rsRecords3;
    String queryString;

    String xmlRequest;
    String xmlResponse;
    String fechaYHoraCertificacion = "";
    String idProveedor = "";
    String direccion = "";
    String correo = "";
    Date fechaDependienteEmision;
    File pdfFile = null;

    NumberField montoTxt;
    NumberField tasaCambioTxt;
    DateField fechaDt;

    SegmentedField uuidField;

    TextField serieTxt;
    TextField numeroTxt;
    TextField nitProveedotTxt;
    TextField documentoAfectaTxt;

    TextField proveedorTxt;
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

    double debe = 0.00;
    double haber = 0.00;

    public ExencionIvaInfileForm(String empresa, String codigoPartida, String tipoDocumento) {

        this.empresa = empresa;
        this.codigoPartida = this.codigoCCFactura = codigoPartida;
        this.tipoDocumento = tipoDocumento;
        this.mainUI = UI.getCurrent();
        this.infileClient = new InfileClient(((SopdiUI)mainUI).sessionInformation.getInfileEmisor());

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
        fechaDt.setValue(new Date());

        serieTxt = new TextField("Serie : ");
        serieTxt.setWidth("6em");
        serieTxt.addStyleName("mayusculas");

        numeroTxt = new TextField("Número : ");
        numeroTxt.setWidth("8em");
        numeroTxt.setEnabled(true);

        proveedorTxt = new TextField("Proveedores");
        proveedorTxt.setWidth("28em");
        proveedorTxt.setInvalidAllowed(false);
        proveedorTxt.setEnabled(false);

        nitProveedotTxt = new TextField("Nit : ");
        nitProveedotTxt.setWidth("10em");
        nitProveedotTxt.setEnabled(false);

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

        layoutEncabezado.addComponents(tipoDocumentoCbx, fechaDt, serieTxt, numeroTxt, proveedorTxt, nitProveedotTxt);

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

    public void generarTablaPartida(String codigoCC) {

        containerPartida.removeAllItems();

        footerPartida.getCell("DEBE").setText("0.00");
        footerPartida.getCell("HABER").setText("0.00");

        queryString = "SELECT *, cn.N5, cn.NoCuenta, dfs.NumeroAutorizacion ";
        queryString += "FROM contabilidad_partida cp ";
        queryString += "INNER JOIN contabilidad_nomenclatura cn ON cp.IdNomenclatura = cn.IdNomenclatura ";
        queryString += "LEFT JOIN documentos_fel_sat dfs ON cp.NumeroDocumento = dfs.Numero ";
        queryString += "WHERE cp.CodigoPartida = '" + codigoCC + "'";

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

                    fechaDependienteEmision = rsRecords3.getDate("Fecha");

                    queryString = "SELECT * from proveedor ";
                    queryString += "WHERE Inhabilitado = 0 ";
                    queryString += "AND EsProveedor = 1 ";
                    queryString += "AND IDProveedor = " + rsRecords3.getString("IdProveedor") + " ";
                    queryString += "Order By Nombre ";

                    try {
                        stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                        rsRecords = stQuery.executeQuery(queryString);
                        if(rsRecords.next()) {
                            proveedorTxt.setValue(rsRecords.getString("Nombre"));
                            idProveedor = rsRecords.getString("IDProveedor");
                            nitProveedotTxt.setValue(rsRecords.getString("NIT"));
                            direccion = rsRecords.getString("Direccion");
                            correo = rsRecords.getString("Email");
                        }
                    } catch (Exception ex1) {
                        System.out.println("Error al listar Proveedores error : " + ex1.getMessage());
                        ex1.printStackTrace();
                    }

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
            debe = 0.00;
            haber = 0.00;

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
        if (proveedorTxt.getValue() == null || proveedorTxt.getValue().equals("0")) {
            Notification.show("Por favor ingrese el cliente.", Notification.Type.WARNING_MESSAGE);
            proveedorTxt.focus();
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
                    Notification.show("Este documento ya fué ingresado, revise.!. Empresa = " + rsRecords.getString("IdEmpresa"),
                            "Fecha : " + rsRecords.getString("Fecha") +
                                    "Serie : " + serieTxt.getValue().toUpperCase().trim() +
                                    "Numero : " + numeroTxt.getValue().toUpperCase().trim(),
                            Notification.Type.WARNING_MESSAGE);
                    numeroTxt.focus();
                    System.out.println("Este documento ya fué ingresado, revise.!. Empresa = " + rsRecords.getString("IdEmpresa") +
                            "Fecha : " + rsRecords.getString("Fecha") +
                            "Serie : " + serieTxt.getValue().toUpperCase().trim() +
                            "Numero : " + numeroTxt.getValue().toUpperCase().trim());
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
        try {
            queryString = "Insert Into contabilidad_partida (IdEmpresa, CodigoPartida, CodigoCC, ";
            queryString += "Fecha, Descripcion, TipoDocumento, IdNomenclatura, IdProveedor, NITProveedor, NombreProveedor, ";
            queryString += "SerieDocumento, NumeroDocumento, TipoDOCA, NoDoca, UUIDDoca, MonedaDocumento, TipoCambio, ";
            queryString += "MontoDocumento, Debe, Haber, DebeQuetzales, HaberQuetzales, Estatus ,CreadoUsuario, CreadoFechaYHora, ";
            queryString += "Archivo, ArchivoTipo, ArchivoPeso, ArchivoNombre, ";
            queryString += "UUID, FechaYHoraCertificacion,  XmlRequest, XmlResponse, IdProducto) ";
            queryString += "Values ";

            int contador = 0;

            for (Object itemId : containerExencion.getItemIds()) {
                Item item = containerExencion.getItem(itemId);
                if (montoTxt.getDoubleValueDoNotThrow() == 0 && montoTxt.getDoubleValueDoNotThrow() == 0) {
                    continue;
                }

                queryString += "(";
                queryString += empresaCbx.getValue();                                               //IdEmpresa
                queryString += ",'" + codigoPartida + "'";                                          //CodigoPartida
                queryString += ",'" + codigoCCFactura + "'";                                        //CodigoCC
                queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";        //Fecha
                                                                                            //Descripcion
                queryString += ",'" + "EXENCiÓN IVA (N# " + documentoAfectaTxt.getValue() + " " + proveedorTxt.getValue() + " )' ";
                queryString += ",'" + tipoDocumentoCbx.getValue() + "'";                            //TipoDocumento
                                                                                            //IdNomenclatura
                queryString += "," + String.valueOf(item.getItemProperty("CUENTA").getValue()).split(" ")[0];
                queryString += "," + idProveedor;                                       //IdProveedor
                queryString += ",'" + nitProveedotTxt.getValue() + "'";                             //NitProveedor
                queryString += ",'" + proveedorTxt.getValue() + "'";   //NombreProveedor
                queryString += ",'" + serieTxt.getValue().toUpperCase().trim() + "'";                             //SerieDocumento
                queryString += ",'" + numeroTxt.getValue().trim() + "'";                                          //NumeroDocumento
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
                queryString += "," + Utileria.round(debe * tasaCambioTxt.getDoubleValueDoNotThrow());
                                                                                            //HaberQuetzales
                queryString += "," + Utileria.round(haber * tasaCambioTxt.getDoubleValueDoNotThrow());

                queryString += ",'INGRESADO'";                                                      //Estatus
                queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();          //CreadoUsuario
                queryString += ",current_timestamp";                                                //CreadoFechaHora
                queryString += ",null"; //archivo
                queryString += ",'application/pdf'"; //archivo tipo
                queryString += "," + (pdfFile != null ? pdfFile.length() : 0);    //archvio size
                queryString += ",'" + (pdfFile != null ? pdfFile.getAbsolutePath() : "").replace("\\", "/") + "'";
                queryString += ",'" + infileClient.getUUID() + "'";                                                   //UUID
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

            Notification notif = new Notification("Registro ingresado exitosamente",
                    Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.CHECK);
            notif.show(Page.getCurrent());

            insertarDocumentoElectronico(codigoPartida);

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

    private boolean documentoCeritficaroInfile(){
        Receptor receptor = new Receptor(
                nitProveedotTxt.getValue().replaceAll("-", ""),
                proveedorTxt.getValue().toString(),
                correo,
                direccion
        );

        List<Producto> productoList = new ArrayList<>();
        productoList.add(new Producto(
                "Exencion Iva Generada por constancia total",
                montoDocumento,
                1,
                "",
                "S"
        ));



        infileClient = new InfileClient(((SopdiUI)mainUI).sessionInformation.getInfileEmisor());
        return infileClient.generarDocumentoCIVA(
                receptor,
                codigoPartida,
                productoList,
                BigDecimal.valueOf(montoTxt.getDoubleValueDoNotThrow()),
                "CIVA",
                "",
                uuidField.getValue().toUpperCase().trim(),
                serieTxt.getValue().toUpperCase().trim(),
                numeroTxt.getValue().toUpperCase().trim(),
                fechaDt.getValue(),
                fechaDependienteEmision,
                monedaDocumento.trim().equals("QUETZALES")?"GTQ":"USD",
                tasaCambioTxt.getDoubleValueDoNotThrow(),
                ""
        );
    }

    public void insertTablaFactura() {
        if (datosValidos()) {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "DATOS VALIDOS OK!");

            codigoPartida = Utileria.nextCodigoPartida(
                    ((SopdiUI) mainUI).databaseProvider.getCurrentConnection(),
                    ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId(),
                    fechaDt.getValue(),
                    0
            );

            if (!((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyFelUser().trim().isEmpty() && toggleSwitch.getValue()) {
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "ES FEL!");
                if (documentoCeritficaroInfile() && toggleSwitch.getValue()) {
                    String serie = infileClient.getSerie();
                    String numero = String.valueOf(infileClient.getNumero());
                    numeroTxt.setValue(numero);
                    serieTxt.setValue(serie);

                    pdfFile = infileClient.obtenerDTEPdf(((SopdiUI) UI.getCurrent()).enviromentsVars.getDtePath() + serie + "_" + numero + ".pdf");

                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, "DOCUMENTO CERTIFICDADO FEL OK!");
                    Notification.show("DOCUMENTO CERTIFICADO FEL OK!", Notification.Type.HUMANIZED_MESSAGE);
                } else {
                    Notification.show("ERROR AL CERTIFICAR DOCUMENTO CON FEL, NOTIFIQUE!", Notification.Type.ERROR_MESSAGE);
                    Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "ERROR AL CERTIFICAR DOCUMENTO CON FEL, VERIFIQUE!" + infileClient.getDescripcionErrores().toString());

                    return;
                }
            }

            updatePartidaTransaccion();
        }
    }


    private void insertarDocumentoElectronico(String codigoPartida){

        queryString =   "INSERT INTO certificado_fel_infile (";
        queryString +=  "Fecha, Origen, Descripcion, Saldo, Creditos, AlertasInfile, AlertasSat, ";
        queryString +=  "InformacionAdicional, UUID, Serie, Numero, JsonResponse, CodigoPartida, IdEmpresa, Estado) ";
        queryString +=  "VALUES (";
        queryString +=  "'" +Utileria.getFechaYYYYMMDDHHMMSS(infileClient.getFechaHoraCertificacion()) + "'";
        queryString +=  ", '" + infileClient.getOrigen() + "'";
        queryString +=  ", '" + infileClient.getDescripcion() + "'";
        queryString +=  ", '" + infileClient.getSaldo() + "'";
        queryString +=  ", '" + infileClient.getCreditos() + "'";
        queryString +=  ", " + infileClient.getAlertasInfile();
        queryString +=  ", " + infileClient.getAlertasSAT();
        queryString +=  ", '" + infileClient.getInformacionAdicional() + "'";
        queryString +=  ", '" + infileClient.getUUID() + "'";
        queryString +=  ", '" + infileClient.getSerie() + "'";
        queryString +=  ", '" + infileClient.getNumero() + "'";
        queryString +=  ", '" + infileClient.getRespuesta() + "'";
        queryString +=  ", '" + codigoPartida + "'";
        queryString +=  ", '" + ((SopdiUI)mainUI).sessionInformation.getStrAccountingCompanyId() + "'";
        queryString +=  ", 'INGRESADO')";


        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "QUERY DOCUMENTO ELECTROCNICO : " + queryString);

        try {

            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            ((IngresoDocumentosView)(mainUI.getNavigator().getCurrentView())).llenarTablaFactura(String.valueOf(empresaCbx.getValue()), 0);

            close();

        } catch (Exception ex1) {
            System.out.println("Error al insertar Documetno Electronico  : " + ex1.getMessage());
            ex1.printStackTrace();
            Notification notif = new Notification("HA OCURRIDO UN ERROR DE BASE DE DATOS : " + ex1.getMessage(),
                    Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.WARNING);
            notif.show(Page.getCurrent());

            try {
                String emailsTo[] = {"alerta@simpletecno.com"};
                MyEmailMessanger eMail = new MyEmailMessanger();

                eMail.postMail(emailsTo, "Error en SOPDI", "Error en base de datos :  " + this.getClass().getName() + " -->" + ex1.getMessage());
            } catch (MessagingException ex2) {
                Logger.getLogger(SopdiUI.class.getName()).log(Level.SEVERE, null, ex2);
            }
        }
    }
}
