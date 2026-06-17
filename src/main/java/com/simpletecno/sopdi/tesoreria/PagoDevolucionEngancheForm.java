package com.simpletecno.sopdi.tesoreria;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.SelectionEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.ui.NumberField;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author user
 */
public class PagoDevolucionEngancheForm extends Window {

    VerticalLayout mainLayout;

    public IndexedContainer container = new IndexedContainer();
    Grid enganchesGrid;
    static final String TIPO = "Tipo";
    static final String NO_PROPERTY = "No.";
    static final String ID_PROVEEDOR_PROPERTY = "Cod Cliente";
    static final String PROVEEDOR_PROPERTY = "Cliente";
    static final String FECHA_PROPERTY = "Fecha";
    static final String CODIGO_CC = "Codigo CC";
    static final String MONEDA_PROPERTY = "Moneda";
    static final String MONTO_PROPERTY = "Monto";
    static final String MONTO_Q_PROPERTY = "Monto Q.";
    static final String IDNOMENCLATURA = "IdNomenclatura";

    DateField fechaDt;

    TextField nombreChequeTxt;
    TextField descripcionTxt;
    TextField numeroTxt;

    NumberField montoTxt;
    NumberField tasaCambioTxt;

    // Numero maximo de lineas (cuentas contables) que puede tener una partida.
    static final int FILAS = 20;

    TextField[] codigoTxt = new TextField[FILAS];
    NumberField[] debeTxt = new NumberField[FILAS];
    NumberField[] debeQTxt = new NumberField[FILAS];
    NumberField[] haberTxt = new NumberField[FILAS];
    NumberField[] haberQTxt = new NumberField[FILAS];

    ComboBox proveedorCbx;
    ComboBox monedaCbx;
    ComboBox medioCbx;
    ComboBox[] cuentaContableCbx = new ComboBox[FILAS];

    HorizontalLayout chequeLayout = new HorizontalLayout();
    HorizontalLayout chequeLayout2 = new HorizontalLayout();
    VerticalLayout partidaLayout = new VerticalLayout();

    Button grabarPartidaBtn;

    Label titleLbl;

    UI mainUI;
    Statement stQuery, stQuery2;
    ResultSet rsRecords;
    String queryString;

    String codigoPartidaNuevo;
    String proveedorId = "";
    String nombreProveedor = "";

    BigDecimal totalDebe;
    BigDecimal totalHaber;

    String idProveedor;
    
    Date fechaPago;

    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");
    static DecimalFormat numberFormat3 = new DecimalFormat("######0.00");

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public PagoDevolucionEngancheForm(String idProveedor, Date fechaPago) {

        this.mainUI = UI.getCurrent();
        this.idProveedor= idProveedor;
        this.fechaPago = fechaPago;

        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setResponsive(true);

        setContent(mainLayout);

        setResponsive(true);
        setWidth("95%");
        setHeight("100%");

        titleLbl = new Label("");
        titleLbl.setValue(empresaId + " " + empresaNombre + " DEVOLUCION A CLIENTE");
        titleLbl.addStyleName(ValoTheme.LABEL_H3);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h3_custom");

        proveedorCbx = new ComboBox("Cliente : ");
        proveedorCbx.setWidth("35em");
        proveedorCbx.setVisible(false);
        proveedorCbx.setFilteringMode(FilteringMode.CONTAINS);
        proveedorCbx.addValueChangeListener(event -> {
            llenarTablaEnganches();
            nombreChequeTxt.setReadOnly(false);
            nombreChequeTxt.setValue(proveedorCbx.getItemCaption(proveedorCbx.getValue()));

        });

        llenarComboProveedor("ESCLIENTE = 1");

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setSpacing(true);
        titleLayout.setWidth("100%");
        titleLayout.setMargin(false);
        titleLayout.addComponents(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.MIDDLE_CENTER);
        titleLayout.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

        mainLayout.addComponent(titleLayout);
        mainLayout.setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        mainLayout.addComponent(proveedorCbx);
        mainLayout.setComponentAlignment(proveedorCbx, Alignment.TOP_CENTER);

        createTablaAnticipos();
        llenarTablaEnganches();
        crearLayoutCheque();
        crearPartidaLayout();
    }

    public void llenarComboProveedor(String codigos) {
        String queryString = " SELECT * FROM proveedor_empresa ";
        queryString += " WHERE " + codigos;
        queryString += " AND Inhabilitado = 0 ";
        queryString += " ORDER BY Nombre ";

        proveedorCbx.removeAllItems();

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) { //  encontrado
                proveedorCbx.addItem(rsRecords.getString("IDProveedor"));
                proveedorCbx.setItemCaption(rsRecords.getString("IDProveedor"), rsRecords.getString("Nombre"));
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar Proveedores " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void createTablaAnticipos() {

        HorizontalLayout reportLayout = new HorizontalLayout();
        reportLayout.setWidth("96%");
        reportLayout.addStyleName("rcorners3");
        reportLayout.setResponsive(true);
        reportLayout.setMargin(true);

        container.addContainerProperty(TIPO, String.class, null);
        container.addContainerProperty(NO_PROPERTY, String.class, null);
        container.addContainerProperty(CODIGO_CC, String.class, null);
        container.addContainerProperty(ID_PROVEEDOR_PROPERTY, String.class, null);
        container.addContainerProperty(PROVEEDOR_PROPERTY, String.class, null);
        container.addContainerProperty(FECHA_PROPERTY, String.class, null);
        container.addContainerProperty(MONEDA_PROPERTY, String.class, null);
        container.addContainerProperty(MONTO_PROPERTY, String.class, null);
        container.addContainerProperty(MONTO_Q_PROPERTY, String.class, null);
        container.addContainerProperty(IDNOMENCLATURA, String.class, null);

        enganchesGrid = new Grid("Devoluciones autorizadas", container);
        enganchesGrid.setWidth("100%");
        enganchesGrid.setImmediate(true);
        enganchesGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        enganchesGrid.setDescription("Seleccione uno o varios registros del mismo proveedor.");
        enganchesGrid.setHeightMode(HeightMode.ROW);
        enganchesGrid.setHeightByRows(5);
        enganchesGrid.setResponsive(true);
        enganchesGrid.setEditorBuffered(false);
        enganchesGrid.addSelectionListener(
                new SelectionEvent.SelectionListener() {
                    @Override
                    public void select(SelectionEvent event) {
                        if (enganchesGrid.getSelectedRows() != null) {
                            calcularPartida();
                        }
                    }
                }
        );

        enganchesGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if (MONTO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }
        });

        reportLayout.addComponent(enganchesGrid);
        reportLayout.setComponentAlignment(enganchesGrid, Alignment.MIDDLE_CENTER);

        mainLayout.addComponent(reportLayout);
        mainLayout.setComponentAlignment(reportLayout, Alignment.TOP_CENTER);
    }

    public void llenarTablaEnganches() {

        container.removeAllItems();
        enganchesGrid.getSelectedRows().clear();
        enganchesGrid.getSelectionModel().reset();

        queryString = "SELECT contabilidad_partida.CodigoPartida, contabilidad_partida.CodigoCC, ";
        queryString += " contabilidad_partida.NombreProveedor ,";
        queryString += " contabilidad_partida.IdNomenclatura, contabilidad_partida.TipoDocumento, ";
        queryString += " contabilidad_partida.SerieDocumento, contabilidad_partida.NumeroDocumento, ";
        queryString += " contabilidad_partida.MontoAutorizadoPagar, contabilidad_partida.MontoAplicarAnticipo,";
        queryString += " contabilidad_partida.Haber, contabilidad_partida.HaberQuetzales, ";
        queryString += " ((autorizacion_pago.Monto / contabilidad_partida.Haber) * contabilidad_partida.HaberQuetzales) ProporcionHaberQ,";
        queryString += " autorizacion_pago.Objetivo, autorizacion_pago.IdAutorizacion, autorizacion_pago.CuentaContableLiquidar, ";
        queryString += " autorizacion_pago.Moneda, autorizacion_pago.Fecha, autorizacion_pago.Monto, autorizacion_pago.IdProveedor ";
        queryString += " FROM contabilidad_partida ";
        queryString += " INNER JOIN autorizacion_pago ON autorizacion_pago.CodigoCC = contabilidad_partida.CodigoCC ";
        queryString += " WHERE contabilidad_partida.IdEmpresa =" + empresaId;
        queryString += " AND contabilidad_partida.IdProveedor = " + idProveedor;
        queryString += " AND contabilidad_partida.IdNomenclatura In (" + ((SopdiUI) mainUI).cuentasContablesDefault.getEnganches() + "," + ((SopdiUI) mainUI).cuentasContablesDefault.getAnticiposClientes() + ", " + ((SopdiUI) mainUI).cuentasContablesDefault.getAcreedoresCortoPlazo() + ")";
        queryString += " AND autorizacion_pago.TipoAutorizacion = 'DEVOLUCION A CLIENTE'";
        queryString += " GROUP BY CodigoPartida";

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);
            if (rsRecords.next()) { //  encontrado
                do {

                    Object itemId = container.addItem();

                    container.getContainerProperty(itemId, TIPO).setValue(rsRecords.getString("Objetivo"));
                    container.getContainerProperty(itemId, NO_PROPERTY).setValue(rsRecords.getString("IdAutorizacion"));
                    container.getContainerProperty(itemId, CODIGO_CC).setValue(rsRecords.getString("CodigoCC"));
                    container.getContainerProperty(itemId, ID_PROVEEDOR_PROPERTY).setValue(rsRecords.getString("IdProveedor"));
                    container.getContainerProperty(itemId, PROVEEDOR_PROPERTY).setValue(rsRecords.getString("NombreProveedor"));
                    container.getContainerProperty(itemId, FECHA_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha")));
                    container.getContainerProperty(itemId, MONEDA_PROPERTY).setValue(rsRecords.getString("Moneda"));
                    container.getContainerProperty(itemId, MONTO_PROPERTY).setValue(numberFormat.format((rsRecords.getDouble("Monto"))));
                    container.getContainerProperty(itemId, MONTO_Q_PROPERTY).setValue(numberFormat.format((rsRecords.getDouble("ProporcionHaberQ"))));
                    container.getContainerProperty(itemId, IDNOMENCLATURA).setValue(rsRecords.getString("CuentaContableLiquidar"));

                } while (rsRecords.next());
            }

        } catch (Exception ex) {
            System.out.println("Error al listar tabla nomenclatura contable:" + ex);
            ex.printStackTrace();
        }
    }

    private void crearLayoutCheque() {

        chequeLayout.setSpacing(true);
        chequeLayout.setMargin(false);
        chequeLayout.setSizeUndefined();

        chequeLayout2.setSpacing(true);
        chequeLayout2.setMargin(false);
        chequeLayout2.setSizeUndefined();

        numeroTxt = new TextField("# Documento : ");
        numeroTxt.setWidth("8em");

        medioCbx = new ComboBox("Medio : ");
        medioCbx.setWidth("10em");
        medioCbx.addItem("CHEQUE");
        medioCbx.addItem("NOTA DE DEBITO");
        medioCbx.select("CHEQUE");

        monedaCbx = new ComboBox("Moneda : ");
        monedaCbx.setWidth("10em");
        monedaCbx.addItem("QUETZALES");
        monedaCbx.addItem("DOLARES");
        monedaCbx.select("QUETZALES");

        tasaCambioTxt = new NumberField("T.Cambio : ");
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
        tasaCambioTxt.setWidth("5em");
        tasaCambioTxt.setValue(1.00);
        tasaCambioTxt.addValueChangeListener( event -> {
            calcularPartida();
        });

        montoTxt = new NumberField("Monto : ");
        montoTxt.setValidationVisible(false);
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
        montoTxt.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        montoTxt.setWidth("7em");

        fechaDt = new DateField("Fecha : ");
        fechaDt.setDateFormat("dd/MM/yyyy");
        fechaDt.setWidth("9em");
        fechaDt.setValue(fechaPago);
        fechaDt.setReadOnly(true);

        nombreChequeTxt = new TextField("Nombre cheque/transf. : ");
        nombreChequeTxt.setWidth("30em");
        nombreChequeTxt.setReadOnly(((SopdiUI) mainUI).sessionInformation.getStrUserProfileName().equals("CONTADOR"));
        nombreChequeTxt.setReadOnly(((SopdiUI) mainUI).sessionInformation.getStrUserProfileName().equals("AUXILIAR"));

        descripcionTxt = new TextField("Observación extra : ");
        descripcionTxt.setWidth("35em");
        descripcionTxt.setVisible(false);

        chequeLayout.addComponent(medioCbx);
        chequeLayout.setComponentAlignment(medioCbx, Alignment.MIDDLE_CENTER);
        chequeLayout.addComponent(numeroTxt);
        chequeLayout.setComponentAlignment(numeroTxt, Alignment.MIDDLE_CENTER);
        chequeLayout.addComponent(fechaDt);
        chequeLayout.setComponentAlignment(fechaDt, Alignment.MIDDLE_CENTER);
        chequeLayout.addComponent(montoTxt);
        chequeLayout.setComponentAlignment(montoTxt, Alignment.MIDDLE_CENTER);
        chequeLayout.addComponent(monedaCbx);
        chequeLayout.setComponentAlignment(monedaCbx, Alignment.MIDDLE_CENTER);
        chequeLayout.addComponent(tasaCambioTxt);
        chequeLayout.setComponentAlignment(tasaCambioTxt, Alignment.MIDDLE_CENTER);
        chequeLayout.addComponent(nombreChequeTxt);
        chequeLayout.setComponentAlignment(nombreChequeTxt, Alignment.MIDDLE_CENTER);

        mainLayout.addComponent(chequeLayout);
        mainLayout.setComponentAlignment(chequeLayout, Alignment.MIDDLE_CENTER);

//        chequeLayout2.addComponent(nombreChequeTxt);
//        chequeLayout2.setComponentAlignment(nombreChequeTxt, Alignment.MIDDLE_CENTER);
        chequeLayout2.addComponent(descripcionTxt);
        chequeLayout2.setComponentAlignment(descripcionTxt, Alignment.MIDDLE_CENTER);

        mainLayout.addComponent(chequeLayout2);
        mainLayout.setComponentAlignment(chequeLayout2, Alignment.MIDDLE_CENTER);
    }

    public void crearPartidaLayout() {

        partidaLayout.addStyleName("rcorners3");
        partidaLayout.setWidthUndefined();
        partidaLayout.setResponsive(true);
        partidaLayout.setSpacing(false);
        partidaLayout.setMargin(true);

        // Las filas de la partida (hasta FILAS lineas) se crean dinamicamente mas abajo.

        HorizontalLayout layoutHorizontalBotones = new HorizontalLayout();
        layoutHorizontalBotones.setSpacing(true);
//        layoutHorizontalBotones.setMargin(new MarginInfo(true,false,false,false));
        layoutHorizontalBotones.setMargin(true);
        layoutHorizontalBotones.setWidth("90%");
        layoutHorizontalBotones.setSpacing(true);

        for (int i = 0; i < FILAS; i++) {
            codigoTxt[i] = new TextField();
            codigoTxt[i].setWidth("2em");
            codigoTxt[i].setVisible(false);
            codigoTxt[i].setValue("");

            cuentaContableCbx[i] = new ComboBox();
            cuentaContableCbx[i].setWidth("30em");
            cuentaContableCbx[i].setFilteringMode(FilteringMode.CONTAINS);

            debeTxt[i] = crearNumberField();
            haberTxt[i] = crearNumberField();
            debeQTxt[i] = crearNumberField();
            haberQTxt[i] = crearNumberField();
        }

        llenarComboCuentaContable();

        Button desAutorizarBtn = new Button("Eliminar Autorización");
        desAutorizarBtn.addStyleName(ValoTheme.BUTTON_DANGER);
        desAutorizarBtn.setIcon(FontAwesome.TRASH);
        desAutorizarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (enganchesGrid.getSelectedRows() != null) {

                    Iterator iter;
                    iter = enganchesGrid.getSelectedRows().iterator();

                    while (iter.hasNext()) {

                        Object gridItem = iter.next();
                        String idAutorizacion = String.valueOf(enganchesGrid.getContainerDataSource().getItem(gridItem).getItemProperty(NO_PROPERTY).getValue());
                        String codigoCC = String.valueOf(enganchesGrid.getContainerDataSource().getItem(gridItem).getItemProperty(CODIGO_CC).getValue());

                        ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Desea eliminar la autorizacion de devolución de enganches ?", "SI", "NO", new ConfirmDialog.Listener() {
                            public void onClose(ConfirmDialog dialog) {
                                if (dialog.isConfirmed()) {

                                    queryString = "DELETE FROM autorizacion_pago";
                                    queryString += " WHERE IdAutorizacion = " + idAutorizacion;

                                    try {

                                        stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                                        stQuery.executeUpdate(queryString);

                                        Notification.show("Autorizacion eliminada con exito! ", Notification.Type.HUMANIZED_MESSAGE);

                                        ((PagarView) (mainUI.getNavigator().getCurrentView())).documentosContainer.removeItem(((PagarView) (mainUI.getNavigator().getCurrentView())).documentosGrid.getSelectedRow());
 /**
                                        for (Iterator iTerator = ((PagarView) (mainUI.getNavigator().getCurrentView())).documentosContainer.getItemIds().iterator(); iTerator.hasNext();) {
                                            Object itemId = iTerator.next();

                                            if(idAutorizacion.equals(String.valueOf(((PagarView) (mainUI.getNavigator().getCurrentView())).documentosContainer.getContainerProperty(itemId, PagarView.ID_AUTO_PROPERTY).getValue()))) {
                                                ((PagarView) (mainUI.getNavigator().getCurrentView())).documentosContainer.getContainerProperty(itemId, PagarView.ESTATUS_PROPERTY).setValue("NO AUTORIZADO");
                                            }
                                        }
**/
                                    } catch (SQLException throwables) {
                                        throwables.printStackTrace();
                                    }
                                }
                            }
                        });
                    }
                    llenarTablaEnganches();
                    close();

                } else {
                    Notification.show("Por favor seleccione un registro..", Notification.Type.ASSISTIVE_NOTIFICATION);
                }
            }
        });

        grabarPartidaBtn = new Button("Registrar pago");
        grabarPartidaBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        grabarPartidaBtn.setIcon(FontAwesome.SAVE);
        grabarPartidaBtn.setWidth(String.valueOf(desAutorizarBtn.getWidth()));
        grabarPartidaBtn.addClickListener((event) -> {
            insertarPartidaSimple();
        });

        Button cancelarBtn = new Button("Cancelar");
        cancelarBtn.addStyleName(ValoTheme.BUTTON_DANGER);
        cancelarBtn.setIcon(FontAwesome.BAN);
        cancelarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                proveedorCbx.setReadOnly(false);
                limpiarPartida();
                proveedorCbx.setReadOnly(true);
                proveedorCbx.focus();
            }
        });

        // Encabezado de columnas (se muestra una sola vez, sobre el area con scroll)
        HorizontalLayout headerFila = new HorizontalLayout();
        headerFila.setSpacing(true);
        headerFila.setMargin(false);
        headerFila.addComponent(crearEncabezado("#", "2em"));
        headerFila.addComponent(crearEncabezado("Cuenta contable", "30em"));
        headerFila.addComponent(crearEncabezado("Debe", "8em"));
        headerFila.addComponent(crearEncabezado("Haber", "8em"));
        headerFila.addComponent(crearEncabezado("Debe Q.", "8em"));
        headerFila.addComponent(crearEncabezado("Haber Q.", "8em"));

        // Filas de la partida dentro de un Panel con scroll vertical, para poder
        // manejar hasta FILAS lineas sin que el formulario crezca demasiado.
        VerticalLayout filasLayout = new VerticalLayout();
        filasLayout.setSpacing(false);
        filasLayout.setMargin(false);
        filasLayout.setWidthUndefined();

        for (int i = 0; i < FILAS; i++) {
            HorizontalLayout fila = new HorizontalLayout();
            fila.setSpacing(true);
            fila.setMargin(false);

            Label numLbl = new Label(String.valueOf(i + 1));
            numLbl.setWidth("2em");

            fila.addComponent(numLbl);
            fila.setComponentAlignment(numLbl, Alignment.MIDDLE_CENTER);
            fila.addComponent(cuentaContableCbx[i]);
            fila.addComponent(debeTxt[i]);
            fila.addComponent(haberTxt[i]);
            fila.addComponent(debeQTxt[i]);
            fila.addComponent(haberQTxt[i]);

            filasLayout.addComponent(fila);
        }

        Panel filasPanel = new Panel();
        filasPanel.setWidthUndefined();
        filasPanel.setHeight("280px");
        filasPanel.addStyleName(ValoTheme.PANEL_WELL);
        filasPanel.setContent(filasLayout);

//        layoutHorizontalBotones.addComponent(cancelarBtn);
//        layoutHorizontalBotones.setComponentAlignment(cancelarBtn, Alignment.BOTTOM_LEFT);
        layoutHorizontalBotones.addComponent(desAutorizarBtn);
        layoutHorizontalBotones.setComponentAlignment(desAutorizarBtn, Alignment.BOTTOM_CENTER);
        layoutHorizontalBotones.addComponent(grabarPartidaBtn);
        layoutHorizontalBotones.setComponentAlignment(grabarPartidaBtn, Alignment.BOTTOM_RIGHT);

        partidaLayout.addComponent(headerFila);
        partidaLayout.setComponentAlignment(headerFila, Alignment.MIDDLE_CENTER);

        partidaLayout.addComponent(filasPanel);
        partidaLayout.setComponentAlignment(filasPanel, Alignment.MIDDLE_CENTER);

        partidaLayout.addComponent(layoutHorizontalBotones);
        partidaLayout.setComponentAlignment(layoutHorizontalBotones, Alignment.MIDDLE_CENTER);

        mainLayout.addComponent(partidaLayout);
        mainLayout.setComponentAlignment(partidaLayout, Alignment.MIDDLE_CENTER);

    }

    /**
     * Crea un NumberField con el formato estandar usado en las lineas de la partida.
     */
    private NumberField crearNumberField() {
        NumberField nf = new NumberField();
        nf.setValidationVisible(false);
        nf.setDecimalAllowed(true);
        nf.setDecimalPrecision(2);
        nf.setMinimumFractionDigits(2);
        nf.setDecimalSeparator('.');
        nf.setDecimalSeparatorAlwaysShown(true);
        nf.setValue(0d);
        nf.setGroupingUsed(true);
        nf.setGroupingSeparator(',');
        nf.setGroupingSize(3);
        nf.setImmediate(true);
        nf.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
        nf.setWidth("8em");
        nf.setValue(0.00);
        return nf;
    }

    /**
     * Crea una etiqueta de encabezado de columna con el ancho indicado.
     */
    private Label crearEncabezado(String texto, String ancho) {
        Label lbl = new Label(texto);
        lbl.setWidth(ancho);
        lbl.addStyleName(ValoTheme.LABEL_BOLD);
        return lbl;
    }

    public void calcularPartida() {

        Object gridItem;
        proveedorId = "";
        nombreProveedor = "";
        double montoTotalSeleccionado = 0.00;

        Iterator iter = enganchesGrid.getSelectedRows().iterator();

        if (iter == null) {
            limpiarPartida();

            nombreChequeTxt.setReadOnly(false);
            nombreChequeTxt.setValue("");

            montoTxt.setReadOnly(false);
            montoTxt.setValue(montoTotalSeleccionado);
            return;
        }
        if (!iter.hasNext()) {
            limpiarPartida();

            nombreChequeTxt.setReadOnly(false);
            nombreChequeTxt.setValue("");

            montoTxt.setReadOnly(false);
            montoTxt.setValue(montoTotalSeleccionado);
            return;
        }

        gridItem = iter.next();
        proveedorId = String.valueOf(enganchesGrid.getContainerDataSource().getItem(gridItem).getItemProperty(ID_PROVEEDOR_PROPERTY).getValue());
        nombreProveedor = String.valueOf(enganchesGrid.getContainerDataSource().getItem(gridItem).getItemProperty(PROVEEDOR_PROPERTY).getValue());
        montoTotalSeleccionado = Double.valueOf(String.valueOf(enganchesGrid.getContainerDataSource().getItem(gridItem).getItemProperty(MONTO_PROPERTY).getValue()).replaceAll(",", ""));

        while (iter.hasNext()) { //// Si hay mas de un registro seleccionado
            gridItem = iter.next();
            montoTotalSeleccionado += Double.valueOf(String.valueOf(enganchesGrid.getContainerDataSource().getItem(gridItem).getItemProperty(MONTO_PROPERTY).getValue()).replaceAll(",", ""));

        }
        limpiarPartida();

        Iterator iter2 = enganchesGrid.getSelectedRows().iterator();

        double montoEnganche = 0.00, montoProporcialQ = 0.00;
        String codigoCC = "";

        while (iter2.hasNext()) {  // POR CADA ENGANCHE QUE ESTAMOS SELECCINANDO

            Object gridItem2 = iter2.next();
            montoEnganche = Double.valueOf(String.valueOf(enganchesGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_PROPERTY).getValue()).replaceAll(",", ""));
            montoProporcialQ = Double.valueOf(String.valueOf(enganchesGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONTO_Q_PROPERTY).getValue()).replaceAll(",", ""));
            codigoCC = String.valueOf(enganchesGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(CODIGO_CC).getValue());

            montoTxt.setReadOnly(false);
            montoTxt.setValue(montoTotalSeleccionado);
            nombreChequeTxt.setReadOnly(false);
            nombreChequeTxt.setValue(nombreProveedor);
//                                nombreChequeTxt.setReadOnly(true);

            monedaCbx.setReadOnly(false);
            monedaCbx.select(String.valueOf(enganchesGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(MONEDA_PROPERTY).getValue()));

            try {
                //// ENGANCHES SELECCIONADOS: se coloca en la primera linea disponible
                for (int i = 0; i < FILAS; i++) {
                    if (cuentaContableCbx[i].getValue() == null) {
                        cuentaContableCbx[i].setValue(enganchesGrid.getContainerDataSource().getItem(gridItem2).getItemProperty(IDNOMENCLATURA).getValue());
                        debeTxt[i].setValue(montoEnganche);
                        debeQTxt[i].setValue(montoProporcialQ);
                        codigoTxt[i].setValue(codigoCC);
                        break;
                    }
                }
            } catch (Exception ex) {
                System.out.println("Error " + ex);
            }
        }

        //// CUENTA DE BANCOS (contrapartida del pago): primera linea disponible desde la 2da
        String cuentaBancos;
        double haberQBancos;
        if (monedaCbx.getValue().toString().equals("QUETZALES")) {
            tasaCambioTxt.setValue(1.00);
            cuentaBancos = ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaLocal();
            haberQBancos = montoTotalSeleccionado * tasaCambioTxt.getDoubleValueDoNotThrow();
        } else {
            cuentaBancos = ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaExtranjera();
            haberQBancos = Double.valueOf(numberFormat3.format(montoTotalSeleccionado * tasaCambioTxt.getDoubleValueDoNotThrow()));
        }
        for (int i = 1; i < FILAS; i++) {
            if (cuentaContableCbx[i].getValue() == null) {
                cuentaContableCbx[i].select(cuentaBancos);
                haberTxt[i].setValue(montoTotalSeleccionado);
                haberQTxt[i].setReadOnly(false);
                haberQTxt[i].setValue(haberQBancos);
                break;
            }
        }

        totalDebe = new BigDecimal(0);
        totalHaber = new BigDecimal(0);
        for (int i = 0; i < FILAS; i++) {
            totalDebe = totalDebe.add(new BigDecimal(debeQTxt[i].getDoubleValueDoNotThrow()));
            totalHaber = totalHaber.add(new BigDecimal(haberQTxt[i].getDoubleValueDoNotThrow()));
        }
        totalDebe = totalDebe.setScale(2, RoundingMode.HALF_UP);
        totalHaber = totalHaber.setScale(2, RoundingMode.HALF_UP);

        double diferencia = Double.valueOf(numberFormat3.format((totalHaber.doubleValue() - totalDebe.doubleValue())));

        //para diferencial cambiario, si es que lo hay ... primera linea libre desde la 3ra
        String diferencial = ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getDiferencialCambiario();
        if (diferencia < 0) {
            diferencia = diferencia * -1;
            for (int i = 2; i < FILAS; i++) {
                if (cuentaContableCbx[i].getValue() == null
                        && (i == 2 || !diferencial.equals(String.valueOf(cuentaContableCbx[i - 1].getValue())))) {
                    cuentaContableCbx[i].select(diferencial);
                    haberQTxt[i].setValue(diferencia);
                    break;
                }
            }
        } else {
            for (int i = 2; i < FILAS; i++) {
                if (cuentaContableCbx[i].getValue() == null
                        && (i == 2 || !diferencial.equals(String.valueOf(cuentaContableCbx[i - 1].getValue())))) {
                    cuentaContableCbx[i].select(diferencial);
                    debeQTxt[i].setValue(diferencia);
                    break;
                }
            }
        }
    }
    
    public void llenarComboCuentaContable() {
        String queryString = " SELECT * FROM contabilidad_nomenclatura_empresa";
        queryString += " WHERE Estatus='HABILITADA'";
        queryString += " ANd IdEmpresa = " + empresaId;
        queryString += " ORDER BY N5";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) { //  encontrado
                String idNomenclatura = rsRecords.getString("IdNomenclatura");
                String caption = rsRecords.getString("NoCuenta") + " " + rsRecords.getString("N5");
                for (int i = 0; i < FILAS; i++) {
                    cuentaContableCbx[i].addItem(idNomenclatura);
                    cuentaContableCbx[i].setItemCaption(idNomenclatura, caption);
                }
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar cuentas contables: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    public void insertarPartidaSimple() {

        if (enganchesGrid.getSelectedRows() == null) {
            Notification.show("Por favor seleccione un registro!", Notification.Type.WARNING_MESSAGE);
            return;
        }

        if (((SopdiUI) UI.getCurrent()).esMesCerrado(empresaId, Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()))) {
            Notification.show("La fecha del documento no puede ser de un mes ya cerrado contablemente, revise!", Notification.Type.WARNING_MESSAGE);
            fechaDt.focus();
            return;
        }
        if (!((SopdiUI) UI.getCurrent()).esPrimerMesAbierto(empresaId, Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()))) {
            Notification.show("El mes abierto a operaciones es : " + ((SopdiUI) UI.getCurrent()).primerMesAbierto(empresaId), Notification.Type.WARNING_MESSAGE);
            fechaDt.focus();
            return;
        }

        totalDebe = new BigDecimal(0);
        totalHaber = new BigDecimal(0);
        for (int i = 0; i < FILAS; i++) {
            totalDebe = totalDebe.add(new BigDecimal(debeTxt[i].getDoubleValueDoNotThrow()));
            totalHaber = totalHaber.add(new BigDecimal(haberTxt[i].getDoubleValueDoNotThrow()));
        }
        totalDebe = totalDebe.setScale(2, RoundingMode.HALF_UP);
        totalHaber = totalHaber.setScale(2, RoundingMode.HALF_UP);

        if (totalDebe.doubleValue() != totalHaber.doubleValue()) {
            Notification.show("La partida es descuadrada, por favor revisar"
                    + " Debe = " + totalDebe.doubleValue() + "  Haber = " + totalHaber, Notification.Type.WARNING_MESSAGE);
            return;
        }

        if (numeroTxt.getValue().trim().isEmpty()) {
            Notification.show("Por favor, escriba el cheque o transferencia. ", Notification.Type.ERROR_MESSAGE);
            numeroTxt.focus();
            return;
        }
        if (nombreChequeTxt.getValue().trim().isEmpty()) {
            Notification.show("Por favor, escriba el nombre del cheque o transferencia. ", Notification.Type.ERROR_MESSAGE);
            nombreChequeTxt.focus();
            return;
        }
        if (montoTxt.getDoubleValueDoNotThrow() == 0.00) {
            Notification.show("Por favor primero cree la partida, con el monto respectivo. ", Notification.Type.ERROR_MESSAGE);
            montoTxt.focus();
            return;
        }
        if (cuentaContableCbx[0].getValue() == null || cuentaContableCbx[1].getValue() == null) {
            Notification.show("Por favor elija la cuenta contable que corresponda. ", Notification.Type.ERROR_MESSAGE);
            cuentaContableCbx[0].focus();
            return;
        }

        queryString = "SELECT CodigoPartida FROM contabilidad_partida ";
        queryString += " WHERE NumeroDocumento = '" + numeroTxt.getValue() + "'";
        queryString += " AND IdEmpresa = " + empresaId;
        queryString += " AND TipoDocumento = '" + medioCbx.getValue() + "'";
        queryString += " AND MonedaDocumento = '" + monedaCbx.getValue() + "'";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado
                Notification.show("Documento ya registrado en pago, codigo de partida = " + rsRecords.getString("CodigoPartida"), Notification.Type.ERROR_MESSAGE);
                numeroTxt.focus();
                return;
            }

        } catch (Exception ex1) {
            System.out.println("Error al validar el documento ingresado. " + ex1.getMessage());
            ex1.printStackTrace();
        }

        String fecha = Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()); //yyy/mm/yyyy
        String ultimoEncontado;
        String dia = fecha.substring(8, 10);
        String mes = fecha.substring(5, 7);
        String año = fecha.substring(0, 4);

        String codigoPartida = empresaId + año + mes + dia + "3";

        queryString = "SELECT codigoPartida FROM contabilidad_partida ";
        queryString += " WHERE CodigoPartida LIKE '" + codigoPartida + "%'";
        queryString += " ORDER BY CodigoPartida DESC ";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado

                ultimoEncontado = rsRecords.getString("codigoPartida").substring(12, 15);

                System.out.println("ultimo encontrado " + ultimoEncontado);

                codigoPartida += String.format("%03d", (Integer.valueOf(ultimoEncontado) + 1));

            } else {
                codigoPartida += "001";
            }

        } catch (Exception ex1) {
            System.out.println("Error al buscar siguiente correlativo de partida" + ex1.getMessage());
            ex1.printStackTrace();
        }

        codigoPartidaNuevo = codigoPartida;

        queryString = "INSERT INTO contabilidad_partida (IdEmpresa, Estatus, CodigoPartida, CodigoCC,";
        queryString += " TipoDocumento, Fecha, IdProveedor, NITProveedor, NombreProveedor, NombreCheque,";
        queryString += " MontoDocumento, SerieDocumento, NumeroDocumento, TipoDOCA, NoDOCA, IdNomenclatura, MonedaDocumento, Debe, Haber,";
        queryString += " DebeQuetzales, HaberQuetzales, TipoCambio, Saldo,";
        queryString += " Descripcion, CreadoUsuario, CreadoFechaYHora)";
        queryString += " VALUES ";
        queryString += " (";
        queryString += empresaId;
        queryString += ",'INGRESADO'";
        queryString += ",'" + codigoPartida + "'";
        queryString += ",'" + codigoTxt[0].getValue() + "'";
        queryString += ",'" + medioCbx.getValue() + "'";
        queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
        queryString += "," + proveedorId;
        queryString += ",''";//nitproveedor
        queryString += ",'" + nombreProveedor + "'";
        queryString += ",'" + nombreChequeTxt.getValue() + "'";
        queryString += "," + montoTxt.getDoubleValueDoNotThrow();
        queryString += ",''";  //serie documento
        queryString += ",'" + numeroTxt.getValue() + "'";
        queryString += ",''"; //tipodoca
        queryString += ",''"; //doca
        queryString += "," + cuentaContableCbx[0].getValue(); //idcuentacontable
        queryString += ",'" + monedaCbx.getValue() + "'";
        queryString += "," + debeTxt[0].getDoubleValueDoNotThrow(); //DEBE
        queryString += ",0.00"; //HABER
        queryString += "," + debeQTxt[0].getDoubleValueDoNotThrow(); //DEBE Q
        queryString += ",0.00"; //HABER Q.
        queryString += "," + tasaCambioTxt.getDoubleValueDoNotThrow();
        queryString += "," + montoTxt.getDoubleValueDoNotThrow();
        queryString += ",'" + descripcionTxt.getValue() + "'";
        queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
        queryString += ",current_timestamp";
        queryString += ")";

        // Lineas adicionales de la partida (de la 2da en adelante): se agregan las que
        // tengan cuenta contable seleccionada y algun monto en debe o haber.
        for (int i = 1; i < FILAS; i++) {
            if (cuentaContableCbx[i].getValue() != null
                    && (debeTxt[i].getDoubleValueDoNotThrow() > 0 || haberTxt[i].getDoubleValueDoNotThrow() > 0)) {
                queryString += ",(";
                queryString += empresaId;
                queryString += ",'INGRESADO'";
                queryString += ",'" + codigoPartida + "'";
                queryString += ",'" + codigoTxt[i].getValue() + "'";
                queryString += ",'" + medioCbx.getValue() + "'";
                queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fechaDt.getValue()) + "'";
                queryString += "," + proveedorId;
                queryString += ",''"; //nitproveedor
                queryString += ",'" + nombreProveedor + "'";
                queryString += ",'" + nombreChequeTxt.getValue() + "'";
                queryString += "," + montoTxt.getDoubleValueDoNotThrow();
                queryString += ",''"; //serie documento
                queryString += ",'" + numeroTxt.getValue() + "'";
                queryString += ",''"; //tipodoca
                queryString += ",''"; //doca
                queryString += "," + cuentaContableCbx[i].getValue(); //idcuentacontable
                queryString += ",'" + monedaCbx.getValue() + "'";
                queryString += "," + debeTxt[i].getDoubleValueDoNotThrow(); // DEBE
                queryString += "," + haberTxt[i].getDoubleValueDoNotThrow(); // HABER
                queryString += "," + debeQTxt[i].getDoubleValueDoNotThrow(); //DEBE Q.
                queryString += "," + haberQTxt[i].getDoubleValueDoNotThrow(); //HABER Q
                queryString += "," + tasaCambioTxt.getDoubleValueDoNotThrow();
                queryString += "," + montoTxt.getDoubleValueDoNotThrow();
                queryString += ",'" + descripcionTxt.getValue() + "'";
                queryString += "," + ((SopdiUI) mainUI).sessionInformation.getStrUserId();
                queryString += ",current_timestamp";
                queryString += ")";
            }
        }

        try {

            ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().setAutoCommit(false);

            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();

            stQuery.executeUpdate(queryString);

            PagoChequesPDF Pagocheques
                    = new PagoChequesPDF(
                    empresaId,
                    empresaNombre,
                    codigoPartidaNuevo,
                    "0",
                    nombreChequeTxt.getValue(),
                    numeroTxt.getValue(),
                    descripcionTxt.getValue(),
                    numberFormat3.format(montoTxt.getDoubleValueDoNotThrow())
            );
            mainUI.addWindow(Pagocheques);
            Pagocheques.center();

            actualizarAnticiposAutorizados();

            ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().commit();
            ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().setAutoCommit(true);

            Notification notif = new Notification("PAGO REALIZADO  EXITOSAMENTE.",
                    Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.CHECK);
            notif.show(Page.getCurrent());

//            ((PagarView) (mainUI.getNavigator().getCurrentView())).documentosContainer.getContainerProperty(((PagarView) (mainUI.getNavigator().getCurrentView())).documentosGrid.getSelectedRow(), PagarView.ESTATUS_PROPERTY).setValue("PAGADO");
            ((PagarView) (mainUI.getNavigator().getCurrentView())).documentosContainer.removeItem((((PagarView) (mainUI.getNavigator().getCurrentView())).documentosGrid.getSelectedRow()));

        } catch (Exception ex1) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error al insertar transacción  : ", ex1);

            Notification notif = new Notification("HA OCURRIDO UN ERROR DE BASE DE DATOS : " + ex1.getMessage(),
                    Notification.Type.ERROR_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.WARNING);
            notif.show(Page.getCurrent());

            try {
                ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().rollback();
                ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().setAutoCommit(true);
            } catch (SQLException ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public void actualizarAnticiposAutorizados() {

        if (enganchesGrid.getSelectedRows() == null) {
            Notification.show("Por favor seleccione un registro de la tabla. ", Notification.Type.ERROR_MESSAGE);
            return;
        }

        try {
            Iterator iter;

            iter = enganchesGrid.getSelectedRows().iterator();
            String idAutorizacion = "";

            stQuery2 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            Object gridItem;

            while (iter.hasNext()) {

                gridItem = iter.next();

                idAutorizacion = String.valueOf(enganchesGrid.getContainerDataSource().getItem(gridItem).getItemProperty(NO_PROPERTY).getValue());

                queryString = "DELETE FROM autorizacion_pago";
                queryString += " WHERE IdAutorizacion = " + idAutorizacion;

                stQuery2.executeUpdate(queryString);

            }

            ((PagarView) (mainUI.getNavigator().getCurrentView())).documentosContainer.removeItem(((PagarView) (mainUI.getNavigator().getCurrentView())).documentosGrid.getSelectedRow());

            llenarTablaEnganches();
            limpiarPartida();

        } catch (SQLException ex) {
            ex.printStackTrace();
            Logger.getLogger(PagoDevolucionEngancheForm.class.getName()).log(Level.SEVERE, null, ex);

            Notification notif = new Notification("HA OCURRIDO UN ERROR DE BASE DE DATOS : " + ex.getMessage(),
                    Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.WARNING);
            notif.show(Page.getCurrent());

            try {
                ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().rollback();
                ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().setAutoCommit(true);
            } catch (SQLException ex1) {
                Logger.getLogger(PagoDevolucionEngancheForm.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
    }

    public void limpiarPartida() {
        montoTxt.setReadOnly(false);
        numeroTxt.setValue("");
        montoTxt.setValue(0.00);
        nombreChequeTxt.setReadOnly(false);
        nombreChequeTxt.setValue("");
        descripcionTxt.setValue("");

        nombreChequeTxt.setReadOnly(((SopdiUI) mainUI).sessionInformation.getStrUserProfileName().equals("CONTADOR"));
        nombreChequeTxt.setReadOnly(((SopdiUI) mainUI).sessionInformation.getStrUserProfileName().equals("AUXILIAR"));

        for (int i = 0; i < FILAS; i++) {
            cuentaContableCbx[i].setReadOnly(false);
            cuentaContableCbx[i].clear();
            debeTxt[i].setReadOnly(false);
            haberTxt[i].setReadOnly(false);
            debeTxt[i].setValue(0.00);
            haberTxt[i].setValue(0.00);
            debeQTxt[i].setReadOnly(false);
            haberQTxt[i].setReadOnly(false);
            debeQTxt[i].setValue(0.00);
            haberQTxt[i].setValue(0.00);
            codigoTxt[i].setValue("");
        }
    }
}