package com.simpletecno.sopdi.contabilidad;

import com.simpletecno.sopdi.ventas.FacturaVentaView;
import com.simpletecno.sopdi.compras.IngresoDocumentosView;
import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Iterator;

import org.vaadin.ui.NumberField;

/**
 * @author user
 */
public class TransaccionesEspecialesForm extends Window {

    VerticalLayout mainLayout = new VerticalLayout();

    public IndexedContainer container = new IndexedContainer();
    Grid partidaGrid;
    Grid.FooterRow footer;

    NumberField montoTxt;
    NumberField tasaCambioTxt;

    DateField fechaDt;

    TextField serieTxt;
    TextField numeroTxt;
    TextField nitProveedotTxt;
    TextField descripcionTxt;
    TextField documentoAfectaTxt;

    CheckBox huboRetencionChb;
    CheckBox regularizarChb;

    ComboBox proveedorCbx;
    ComboBox monedaCbx;
    ComboBox tipoDocumentoCbx;
    ComboBox empresaCbx;

    Statement stQuery, stQuery2, stQuery3;
    ResultSet rsRecords, rsRecords2, rsRecords3;
    String queryString;

    Button guardarBtn;
    Button salirBtn;
    UI mainUI;
    BigDecimal totalDebe;
    BigDecimal totalHaber;

    String empresa;
    String codigoPartida;
    String codigoCCFactura;
    String backupCodigoEditar;
    String tipoDocumento;
    int editar;

    static final String NIT_PROPERTY = "NIT";

    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");

    public TransaccionesEspecialesForm(
            String empresa,
            String codigoPartida,
            String tipoDocumento,
            int editar) { // 1 es nuevo y 2 es editar

        this.empresa = empresa;
        this.codigoPartida = codigoPartida;
        this.tipoDocumento = tipoDocumento;
        this.editar = editar;
        this.mainUI = UI.getCurrent();

        setResponsive(true);
        setModal(true);
        setResponsive(true);
        setWidth("90%");
        setHeight("98%");

        mainLayout.addStyleName("rcorners3");
        mainLayout.setSpacing(true);
        mainLayout.setWidth("100%");

        HorizontalLayout layoutTitle;
        layoutTitle = new HorizontalLayout();
        layoutTitle.setSpacing(true);
        layoutTitle.setMargin(true);
        layoutTitle.setWidth("100%");

        Label titleLbl = new Label(tipoDocumento);
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

        setContent(mainLayout);
        crearEncabezado();
        crearPartidaContable();

        if ((tipoDocumento.equals("CONSTANCIA ISR COMPRA") || tipoDocumento.equals("CONSTANCIA ISR VENTA")) && editar == 1) { // Nueva constancia
            generarTable(codigoPartida);
            codigoCCFactura = codigoPartida;
        } else if ((tipoDocumento.equals("CONSTANCIA ISR COMPRA") || tipoDocumento.equals("CONSTANCIA ISR VENTA")) && editar == 2) { /// editar constancia
            llenarDatos();
        } else if (tipoDocumento.equals("TRANSACCION ESPECIAL") && editar == 1) {
            generarTable("");
        } else if (tipoDocumento.equals("TRANSACCION ESPECIAL") && editar == 2) {
            llenarDatos();
        } else if (editar == 2) {
            llenarDatos();
        }

    }

    public void crearEncabezado() {

        VerticalLayout todoEncabezado = new VerticalLayout();
        todoEncabezado.addStyleName("rcorners3");
        todoEncabezado.setSpacing(true);

        tipoDocumentoCbx = new ComboBox("T. Documento");
        tipoDocumentoCbx.setWidth("16em");
        tipoDocumentoCbx.addItem("FACTURA VENTA");
        tipoDocumentoCbx.addItem("FACTURA COMPRA");
        tipoDocumentoCbx.addItem("CONSTANCIA ISR COMPRA");
        tipoDocumentoCbx.addItem("CONSTANCIA ISR VENTA");
        tipoDocumentoCbx.addItem("TRANSACCION ESPECIAL");
        tipoDocumentoCbx.select(tipoDocumento);
        tipoDocumentoCbx.setEnabled(false);
        tipoDocumentoCbx.setVisible(false);

        documentoAfectaTxt = new TextField("Documento Afecta :");
        documentoAfectaTxt.setWidth("14em");
        documentoAfectaTxt.setVisible(false);

        if (!tipoDocumento.equals("TRANSACCION ESPECIAL") || !tipoDocumento.equals("CONSTANCIA ISR COMPRA") || !tipoDocumento.equals("CONSTANCIA ISR VENTA") || !tipoDocumento.equals("NOTA DE CREDITO ")) {
            tipoDocumentoCbx.setVisible(true);
            documentoAfectaTxt.setVisible(true);
        }

        regularizarChb = new CheckBox("Regularización dólares?");
        regularizarChb.addValueChangeListener(e -> {
            if(monedaCbx != null && monedaCbx.getValue() != null) {
                if(partidaGrid!=null) {
                    partidaGrid.getColumn("DEBE").setHeaderCaption("DEBE");
                    partidaGrid.getColumn("HABER").setHeaderCaption("HABER");
                    partidaGrid.setDescription("Seleccione un registro para ingresar o editar. Montos en la moneda seleccionada");
                }
                if (regularizarChb.getValue()) {
                    if (!monedaCbx.getValue().equals("DOLARES")) {
                        Notification.show("Si desea regularizar dólares, debe seleccionar la moneda DOLARES", Notification.Type.WARNING_MESSAGE);
                        monedaCbx.focus();
                        regularizarChb.setEnabled(false);
                    }
                    else {
                        if(partidaGrid!=null) {
                            partidaGrid.getColumn("DEBE").setHeaderCaption("DEBE Q.");
                            partidaGrid.getColumn("HABER").setHeaderCaption("HABER Q.");
                            partidaGrid.setDescription("Seleccione un registro para ingresar o editar. Montos en Quetzales");
                        }
                    }
                }
            }
        });
        regularizarChb.setEnabled(false);

        huboRetencionChb = new CheckBox("Hubo retención?");
        huboRetencionChb.setValue(true);
        huboRetencionChb.addValueChangeListener(e -> {
            generarTable(codigoPartida);
        });

        huboRetencionChb.setEnabled(false);
        if (tipoDocumento.equals("CONSTANCIA ISR VENTA")) {
            huboRetencionChb.setEnabled(true);
        }

        fechaDt = new DateField("Fecha : ");
        fechaDt.setDateFormat("dd/MM/yyyy");
        fechaDt.setWidth("8em");
        fechaDt.addValueChangeListener(event -> {
            if (fechaDt.getValue() != null) {
//                if (((SopdiUI) UI.getCurrent()).esMesCerrado(String.valueOf(empresaCbx.getValue()), Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()))) {
//                    Notification.show("La fecha del documento no puede ser de un mes ya cerrado contablemente, revise!", Notification.Type.WARNING_MESSAGE);
//                    fechaDt.focus();
//                    return;
//                }
//                if (!((SopdiUI) UI.getCurrent()).esPrimerMesAbierto(String.valueOf(empresaCbx.getValue()), Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()))) {
//                    Notification.show("El mes abierto a operaciones es : " + ((SopdiUI) UI.getCurrent()).primerMesAbierto(String.valueOf(empresaCbx.getValue())), Notification.Type.WARNING_MESSAGE);
//                    fechaDt.focus();
//                    return;
//                }
                if(monedaCbx != null && monedaCbx.getValue() != null) {
                    if (monedaCbx.getValue().equals("DOLARES")) {
                        if (tasaCambioTxt != null && tasaCambioTxt.getValue() != null && !tasaCambioTxt.getValue().equals("")) {
                            tasaCambioTxt.setValue(((SopdiUI) UI.getCurrent()).getTasaCambioDelDia(Utileria.getFechaYYYYMMDD_1(fechaDt.getValue())));
                        }
                    }
                }
            }
        });
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

        descripcionTxt = new TextField("Descripcón : ");
        descripcionTxt.setWidth("35em");
        descripcionTxt.setVisible(true);

        monedaCbx = new ComboBox("Moneda :");
        monedaCbx.addItem("QUETZALES");
        monedaCbx.addItem("DOLARES");
        monedaCbx.select("QUETZALES");
        monedaCbx.setWidth("11em");
        monedaCbx.addValueChangeListener(evet -> {
            regularizarChb.setValue(false);
            regularizarChb.setEnabled(false);
            if (monedaCbx.getValue() != null) {
                if (monedaCbx.getValue().equals("DOLARES")) {
                    if (tasaCambioTxt != null && tasaCambioTxt.getValue() != null && !tasaCambioTxt.getValue().equals("")) {
                        tasaCambioTxt.setValue(((SopdiUI) UI.getCurrent()).getTasaCambioDelDia(Utileria.getFechaYYYYMMDD_1(fechaDt.getValue())));
                        regularizarChb.setEnabled(true);
                    }
                } else {
                    tasaCambioTxt.setValue(1.00);
                }
            } else {
                tasaCambioTxt.setValue(1.00);
            }
        });

        montoTxt = new NumberField("Monto :");
        montoTxt.setWidth("8em");
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
                generarTable(codigoPartida);
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

        HorizontalLayout layoutEncabezado = new HorizontalLayout();
        layoutEncabezado.setSpacing(true);
        layoutEncabezado.setMargin(false);

        HorizontalLayout layoutEncabezado2 = new HorizontalLayout();
        layoutEncabezado2.setSpacing(true);
        layoutEncabezado2.setMargin(false);

        layoutEncabezado.addComponent(tipoDocumentoCbx);
        layoutEncabezado.addComponent(fechaDt);
        layoutEncabezado.addComponent(serieTxt);
        layoutEncabezado.addComponent(numeroTxt);
        layoutEncabezado.addComponent(nitProveedotTxt);
        layoutEncabezado.addComponent(proveedorCbx);
        layoutEncabezado.addComponent(documentoAfectaTxt);

        layoutEncabezado2.addComponent(monedaCbx);
        layoutEncabezado2.addComponent(montoTxt);
        layoutEncabezado2.addComponent(tasaCambioTxt);

        layoutEncabezado2.addComponent(huboRetencionChb);
        layoutEncabezado2.addComponent(regularizarChb);
        layoutEncabezado2.addComponent(descripcionTxt);

        layoutEncabezado2.setComponentAlignment(regularizarChb, Alignment.BOTTOM_CENTER);
        layoutEncabezado2.setComponentAlignment(huboRetencionChb, Alignment.BOTTOM_CENTER);

        todoEncabezado.addComponent(layoutEncabezado);
        todoEncabezado.setComponentAlignment(layoutEncabezado, Alignment.MIDDLE_CENTER);
        todoEncabezado.addComponent(layoutEncabezado2);
        todoEncabezado.setComponentAlignment(layoutEncabezado2, Alignment.MIDDLE_CENTER);

        mainLayout.addComponent(todoEncabezado);
        mainLayout.setComponentAlignment(todoEncabezado, Alignment.MIDDLE_CENTER);

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

    public void llenarComboProveedor() {
        queryString = " SELECT * from proveedor ";
        queryString += " WHERE Inhabilitado = 0 ";
        if (tipoDocumento.equals("CONSTANCIA ISR VENTA")) {
            queryString += " AND EsCliente = 1";
        }
        else {
            queryString += " AND EsProveedor = 1";
        }

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

    public void crearPartidaContable() {

        HorizontalLayout reportLayout = new HorizontalLayout();
        reportLayout.setWidth("75%");
        reportLayout.addStyleName("rcorners3");
        reportLayout.setResponsive(true);
        reportLayout.setMargin(new MarginInfo(true, false, true, false));

        container.addContainerProperty("CUENTA", String.class, null);
        container.addContainerProperty("DEBE", String.class, null);
        container.addContainerProperty("HABER", String.class, null);

        partidaGrid = new Grid("", container);

        partidaGrid.setImmediate(true);
        partidaGrid.setSelectionMode(Grid.SelectionMode.NONE);
        partidaGrid.setDescription("Seleccione un registro para ingresar o editar.");
//        partidaGrid.setHeightMode(HeightMode.ROW);
        //partidaGrid.setHeightByRows(25);
        partidaGrid.setWidth("100%");
        partidaGrid.setResponsive(true);
        partidaGrid.setEditorBuffered(false);
        partidaGrid.setSizeFull();
        partidaGrid.setEditorEnabled(true);
        partidaGrid.getColumn("CUENTA").setEditorField(getComboCuentas());
        partidaGrid.getColumn("DEBE").setEditorField(getAmmountField("DEBE"));
        partidaGrid.getColumn("HABER").setEditorField(getAmmountField("HABER"));
        partidaGrid.addItemClickListener((event) -> {
            if (event != null) {
                partidaGrid.editItem(event.getItemId());
            }
        });

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

        footer = partidaGrid.appendFooterRow();
        footer.getCell("CUENTA").setText("SUMAS IGUALES");
        footer.getCell("DEBE").setText("0.00");
        footer.getCell("HABER").setText("0.00");
        footer.getCell("CUENTA").setStyleName("rightalign");
        footer.getCell("DEBE").setStyleName("rightalign");
        footer.getCell("HABER").setStyleName("rightalign");

        partidaGrid.setFooterVisible(true);

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
                updatePartidaTransaccion();
            }
        });

        mainLayout.addComponent(reportLayout);
        mainLayout.setComponentAlignment(reportLayout, Alignment.TOP_CENTER);

        HorizontalLayout layoutButtons = new HorizontalLayout();
        layoutButtons.setSpacing(true);

        layoutButtons.addComponent(salirBtn);
        layoutButtons.setComponentAlignment(salirBtn, Alignment.MIDDLE_LEFT);
        layoutButtons.addComponent(guardarBtn);
        layoutButtons.setComponentAlignment(guardarBtn, Alignment.MIDDLE_CENTER);

        mainLayout.addComponent(layoutButtons);
        mainLayout.setComponentAlignment(layoutButtons, Alignment.MIDDLE_CENTER);

        llenarComboProveedor();
    }

    public void generarTable(String codigoCC) {

        container.removeAllItems();

        footer.getCell("DEBE").setText("0.00");
        footer.getCell("HABER").setText("0.00");

        if (tipoDocumento.equals("TRANSACCION ESPECIAL")) {
            Object itemId;
            for (int idx = 1; idx < 25; idx++) {
                itemId = container.addItem();
                container.getContainerProperty(itemId, "CUENTA").setValue("");
                container.getContainerProperty(itemId, "DEBE").setValue("0.00");
                container.getContainerProperty(itemId, "HABER").setValue("0.00");
            }
        } else {

            queryString = "SELECT *, contabilidad_nomenclatura.N5, contabilidad_nomenclatura.NoCuenta ";
            queryString += "FROM contabilidad_partida,contabilidad_nomenclatura ";
            queryString += "WHERE contabilidad_partida.CodigoPartida = '" + codigoCC + "' ";
            queryString += "AND contabilidad_nomenclatura.IdNomenclatura = contabilidad_partida.IdNomenclatura ";
            if (tipoDocumento.equals("CONSTANCIA ISR COMPRA")) {
                queryString += "AND contabilidad_nomenclatura.IdNomenclatura IN ( " + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getProveedores() + ", " + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getAnticiposProveedor() + ") ";
            } else if (tipoDocumento.equals("CONSTANCIA ISR VENTA")) {
                queryString += "AND contabilidad_nomenclatura.IdNomenclatura = " +  ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getClientes() + " ";
            }
            queryString += "GROUP by contabilidad_partida.CodigoPartida";

            try {
                double debe = 0.00;
                double haber = 0.00;

                stQuery3 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                rsRecords3 = stQuery3.executeQuery(queryString);

                if (rsRecords3.next()) {

                    do {

                        if (tipoDocumento.equals("CONSTANCIA ISR COMPRA") || tipoDocumento.equals("CONSTANCIA ISR VENTA")) {

                            proveedorCbx.setValue(rsRecords3.getString("IdProveedor"));
                            proveedorCbx.select(rsRecords3.getString("IdProveedor"));
                            nitProveedotTxt.setValue(String.valueOf(proveedorCbx.getContainerProperty(proveedorCbx.getValue(), "NIT").getValue()));

                            if (tipoDocumento.equals("CONSTANCIA ISR VENTA")) {
                                tipoDocumentoCbx.select("CONSTANCIA ISR VENTA");
                                tipoDocumentoCbx.setReadOnly(true);
                                proveedorCbx.setCaption("CLIENTE");
                            } else {
                                tipoDocumentoCbx.select("CONSTANCIA ISR COMPRA");
                                tipoDocumentoCbx.setReadOnly(true);
                            }

                            monedaCbx.select(rsRecords3.getString("MonedaDocumento"));
                            monedaCbx.setReadOnly(true);

                            documentoAfectaTxt.setValue(rsRecords3.getString("SerieDocumento") + " " + rsRecords3.getString("NumeroDocumento"));
                            documentoAfectaTxt.setReadOnly(true);

                            descripcionTxt.setValue("RETENCION ISR FACTURA (N# " + documentoAfectaTxt.getValue()
                                    + " " + rsRecords3.getString("NombreProveedor") + " ) ");

                            Object itemId = container.addItem();
                            Object itemId2 = container.addItem();

                            if (tipoDocumento.equals("CONSTANCIA ISR COMPRA")) {
                                container.getContainerProperty(itemId, "CUENTA").setValue(getDescripcionCuentas( ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getProveedores() ));
                                container.getContainerProperty(itemId2, "CUENTA").setValue( getDescripcionCuentas(((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getIsrRetenidoPorPagar()));
                            } else if (tipoDocumento.equals("CONSTANCIA ISR VENTA")) {
                                container.getContainerProperty(itemId, "CUENTA").setValue(getDescripcionCuentas( ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getIsrGasto() ));
                                container.getContainerProperty(itemId2, "CUENTA").setValue(getDescripcionCuentas( ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getClientes() ));
                            }
                            else {
                                container.getContainerProperty(itemId2, "CUENTA").setValue(rsRecords3.getString("IdNomenclatura") + " " + rsRecords3.getString("NoCuenta") + " " + rsRecords3.getString("N5"));
                            }
                            container.getContainerProperty(itemId, "DEBE").setValue(montoTxt.getValue());
                            container.getContainerProperty(itemId, "HABER").setValue("0.00");
                            container.getContainerProperty(itemId2, "DEBE").setValue("0.00");
                            container.getContainerProperty(itemId2, "HABER").setValue(montoTxt.getValue());
                            debe = montoTxt.getDoubleValueDoNotThrow();
                            haber = montoTxt.getDoubleValueDoNotThrow();
                        }
                        else {
                            debe += rsRecords3.getDouble("Debe");
                            haber += rsRecords3.getDouble("Haber");
                        }
                    } while (rsRecords3.next());
                }

                footer.getCell("DEBE").setText(numberFormat.format(debe));
                footer.getCell("HABER").setText(numberFormat.format(haber));

            } catch (Exception ex1) {
                System.out.println("Error al listar cuentas contables: " + ex1.getMessage());
                ex1.printStackTrace();
            }
        }
    }

    public void llenarDatos() {

        container.removeAllItems();

        footer.getCell("DEBE").setText("0.00");
        footer.getCell("HABER").setText("0.00");

        queryString = " select contabilidad_partida.*,contabilidad_nomenclatura.N5, contabilidad_nomenclatura.NoCuenta";
        queryString += " from contabilidad_partida,contabilidad_nomenclatura";
        queryString += " WHERE contabilidad_partida.CodigoPartida = '" + codigoPartida + "'";
        queryString += " and contabilidad_nomenclatura.IdNomenclatura = contabilidad_partida.IdNomenclatura";

        try {

            double debe = 0.00;
            double haber = 0.00;

            stQuery3 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords3 = stQuery3.executeQuery(queryString);

            int contador = 0;

            if (rsRecords3.next()) {

                do {

                    if (contador == 0) {
                        empresaCbx.select(rsRecords3.getString("IdEmpresa"));
                        serieTxt.setValue(rsRecords3.getString("SerieDocumento"));
                        numeroTxt.setValue(rsRecords3.getString("NumeroDocumento"));
                        nitProveedotTxt.setValue(rsRecords3.getString("NITProveedor"));
                        proveedorCbx.select(rsRecords3.getString("IdProveedor"));
                        fechaDt.setValue(rsRecords3.getDate("Fecha"));
                        monedaCbx.setValue(rsRecords3.getString("MonedaDocumento"));
                        tasaCambioTxt.setValue(rsRecords3.getDouble("TipoCambio"));
                        descripcionTxt.setValue(rsRecords3.getString("Descripcion"));
                        tipoDocumentoCbx.select(rsRecords3.getString("TipoDocumento"));
                        montoTxt.setValue(rsRecords3.getDouble("MontoDocumento"));
                        documentoAfectaTxt.setValue(rsRecords3.getString("NoDoca"));

                        if (tipoDocumento.equals("CONSTANCIA ISR VENTA") || tipoDocumento.equals("CONSTANCIA ISR COMPRA") || tipoDocumento.equals("NOTA DE CREDITO")) {
                            codigoCCFactura = rsRecords3.getString("CodigoCC");
                        }
                    }

                    Object itemId = container.addItem();

                    container.getContainerProperty(itemId, "CUENTA").setValue(rsRecords3.getString("IdNomenclatura") + " " + rsRecords3.getString("NoCuenta") + " " + rsRecords3.getString("N5"));
                    container.getContainerProperty(itemId, "DEBE").setValue(rsRecords3.getString("Debe"));
                    container.getContainerProperty(itemId, "HABER").setValue(rsRecords3.getString("Haber"));

                    contador = contador + 1;
                    debe += rsRecords3.getDouble("Debe");
                    haber += rsRecords3.getDouble("Haber");
                } while (rsRecords3.next());
            }
            if (tipoDocumento.equals("TRANSACCION ESPECIAL")) {
                for (int idx = contador; idx < 25; idx++) {
                    Object itemId = container.addItem();
                    container.getContainerProperty(itemId, "CUENTA").setValue("");
                    container.getContainerProperty(itemId, "DEBE").setValue("0.00");
                    container.getContainerProperty(itemId, "HABER").setValue("0.00");
                }
            }

            footer.getCell("DEBE").setText(numberFormat.format(debe));
            footer.getCell("HABER").setText(numberFormat.format(haber));

        } catch (Exception ex1) {
            System.out.println("Error al listar cuentas contables: " + ex1.getMessage());
            ex1.printStackTrace();
            Notification.show("ERROR EN BASE DE DATOS. " + ex1.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
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

    public void updatePartidaTransaccion() {

        if(tasaCambioTxt.getDoubleValueDoNotThrow() == 0 && monedaCbx.getValue().equals("DOLARES")){
            Notification.show("Tasa de Cambio igual a 0.0", Notification.Type.WARNING_MESSAGE);
            return;
        }

        totalDebe = new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP);
        totalHaber = new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP);

        for (Object itemId : container.getItemIds()) {
            Item item = container.getItem(itemId);
            Object debeValue = item.getItemProperty("DEBE").getValue();
            Object haberValue = item.getItemProperty("HABER").getValue();
            totalDebe = totalDebe.add(new BigDecimal(Double.valueOf(String.valueOf(debeValue))).setScale(2, BigDecimal.ROUND_HALF_UP));
            totalHaber = totalHaber.add(new BigDecimal(Double.valueOf(String.valueOf(haberValue))).setScale(2, BigDecimal.ROUND_HALF_UP));

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

        if (editar == 2) { /// editar partida

            backupCodigoEditar = codigoPartida;

            queryString = " DELETE from contabilidad_partida ";
            queryString += " where CodigoPartida = '" + codigoPartida + "'";

            try {
                stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                stQuery.executeUpdate(queryString);

            } catch (SQLException ex) {
                System.out.println("Error al intentar eliminar registros " + ex);
                ex.printStackTrace();
            }

        } else {  /// nueva partida

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
        }

        try {
            queryString = " Insert Into contabilidad_partida (IdEmpresa,CodigoPartida, CodigoCC, ";
            queryString += " Fecha, Descripcion, TipoDocumento, IdNomenclatura, ";
            queryString += " IdProveedor, NITProveedor, NombreProveedor,";
            queryString += " SerieDocumento, NumeroDocumento, TipoDOCA, NoDoca, ";
            queryString += " MonedaDocumento, MontoDocumento, Debe, Haber,";
            queryString += " DebeQuetzales, HaberQuetzales, TipoCambio,Estatus ,CreadoUsuario, CreadoFechaYHora)";
            queryString += " Values ";

            int contador = 0;

            for (Object itemId : container.getItemIds()) {
                Item item = container.getItem(itemId);
                if (Double.valueOf(String.valueOf(item.getItemProperty("DEBE").getValue())) == 0
                        && Double.valueOf(String.valueOf(item.getItemProperty("HABER").getValue())) == 0) {
                    continue;
                }

                queryString += "(";
                queryString += empresaCbx.getValue();

                if (editar == 1) { // NUEVA PARTIDA
                    queryString += ",'" + codigoPartida + "'";
                } else { // EDITAR PARTIDA
                    queryString += ",'" + backupCodigoEditar + "'";
                }

                if ((tipoDocumento.equals("CONSTANCIA ISR COMPRA") || tipoDocumento.equals("CONSTANCIA ISR VENTA"))) {
                    queryString += ",'" + codigoCCFactura + "'";
                } else {
                    if(editar == 1){
                        queryString += ",'" + codigoPartida + "'";
                    }else{
                        queryString += ",'" + backupCodigoEditar + "'";
                    }                    
                }

                queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
                queryString += ",'" + descripcionTxt.getValue() + "'";
                queryString += ",'" + tipoDocumento + "'";
                queryString += "," + String.valueOf(item.getItemProperty("CUENTA").getValue()).split(" ")[0];
                queryString += "," + proveedorCbx.getValue();
                queryString += ",'" + nitProveedotTxt.getValue() + "'";
                queryString += ",'" + proveedorCbx.getItemCaption(proveedorCbx.getValue()) + "'";
                queryString += ",'" + serieTxt.getValue().toUpperCase().trim() + "'";
                queryString += ",'" + numeroTxt.getValue().trim() + "'";

                if (!tipoDocumento.equals("TRANSACCION ESPECIAL")) {
                    queryString += ",'" + tipoDocumentoCbx.getValue() + "'";
                    queryString += ",'" + documentoAfectaTxt.getValue() + "'";
                } else {
                    queryString += ",''";
                    queryString += ",''";
                }
                queryString += ",'" + String.valueOf(monedaCbx.getValue()) + "'";
                queryString += "," + String.valueOf(totalHaber.doubleValue());
                if( monedaCbx.getValue().equals("DOLARES") && regularizarChb.getValue()) {
                    queryString += ", 0"; //debe
                    queryString += ", 0"; //haber
                    queryString += "," + String.valueOf(item.getItemProperty("DEBE").getValue()); //DEBE QUETZALES
                    queryString += "," + String.valueOf(item.getItemProperty("HABER").getValue()); //HABER QUETZALES
                } else {
                    queryString += "," + String.valueOf(item.getItemProperty("DEBE").getValue()); //DEBE
                    queryString += "," + String.valueOf(item.getItemProperty("HABER").getValue()); //HABER
                    queryString += "," + String.valueOf(Double.valueOf(String.valueOf(item.getItemProperty("DEBE").getValue())) * tasaCambioTxt.getDoubleValueDoNotThrow());
                    queryString += "," + String.valueOf(Double.valueOf(String.valueOf(item.getItemProperty("HABER").getValue())) * tasaCambioTxt.getDoubleValueDoNotThrow());
                }
                queryString += "," + String.valueOf(tasaCambioTxt.getDoubleValueDoNotThrow());
                queryString += ",'INGRESADO'";
                queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                queryString += ",current_timestamp";
                queryString += "),";

                contador = contador + 1;
            }

            queryString = queryString.substring(0, queryString.length() - 1);  //quitar la ultima coma

            System.out.println("\nquery insert Transacción Especial = " + queryString);

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

            if (editar == 1 && (tipoDocumento.equals("CONSTANCIA ISR COMPRA") || tipoDocumento.equals("CONSTANCIA ISR VENTA"))) {

                queryString = " Update contabilidad_partida Set ";
                queryString += " Referencia = 'NO'";
                queryString += " Where CodigoPartida = '" + codigoCCFactura + "'";
                queryString += " and IdEmpresa = " + String.valueOf(empresaCbx.getValue());

                stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                stQuery.executeUpdate(queryString);
            }

            close();

        } catch (Exception ex1) {
            System.out.println("Error al insertar TRANSACCION ESPECIAL  : " + ex1.getMessage());
            Notification.show("Error al insertar la TRANSACCION ESPECIAL : " + ex1.getMessage(), Notification.Type.ERROR_MESSAGE);
            ex1.printStackTrace();
        }
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

    private String getDescripcionCuentas(String idNomenclatura) {

        queryString = " SELECT * from contabilidad_nomenclatura";
        queryString += " where IdNomenclatura = " + idNomenclatura;

        try {
            stQuery2 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords2 = stQuery2.executeQuery(queryString);

            queryString = "<<cuenta contable no encontrada, revise cuentas por defaut...>>";

            if(rsRecords2.next()) { //  encontrado
                queryString  = rsRecords2.getString("IdNomenclatura") + " " + rsRecords2.getString("NoCuenta") + " " + rsRecords2.getString("N5");
            }

        } catch (Exception ex1) {
            System.out.println("Error al combo cuentas contables: " + ex1.getMessage());
            ex1.printStackTrace();
        }

        return queryString;
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
        for (Object itemId : container.getItemIds()) {
            Item item = container.getItem(itemId);
            Object propertyValue = item.getItemProperty(propertyId).getValue();
            if(propertyValue != null)
                total += Double.valueOf(String.valueOf(propertyValue)).doubleValue();
        }
        if (footer != null) {
            footer.getCell(propertyId).setText(numberFormat.format(total));
        }
    }

}
