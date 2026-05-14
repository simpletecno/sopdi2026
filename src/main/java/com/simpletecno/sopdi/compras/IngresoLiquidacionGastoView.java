package com.simpletecno.sopdi.compras;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.util.GeneratedPropertyContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.PropertyValueGenerator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.themes.ValoTheme;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.vaadin.dialogs.ConfirmDialog;

/**
 * @author user
 */
public class IngresoLiquidacionGastoView extends VerticalLayout implements View {

    Double totalMontoFactura;
    Double totalMonto;
    Double totalHaber = 0.00;
    Double totalDebe = 0.00;

    Grid liquidacionesGrid;
    public IndexedContainer containerLiquidacion = new IndexedContainer();
    static final String ID_PROPERTY = "Id";
    static final String ID_LIQUIDACION_PROPERTY = "Liquidación";
    static final String ID_LIQUIDADOR_PROPERTY = "Id Liquidador";
    static final String LIQUIDADOR_PROPERTY = "Liquidador";
    static final String MONTO_QUETZALES_PROPERTY = "Monto Q.";
    static final String ESTATUS_PROPERTY = "Estatus";
    static final String IMPRIMIR_PROPERTY = "Imprimir";
    Grid.FooterRow footerliquidaciones;

    Grid facturasGrid;
    public IndexedContainer containerFactura = new IndexedContainer();
    static final String ID_FACTURA_PROPERTY = "Id";
    static final String TIPODOCUMENTO_PROPERTY = "TIPODOC";
    static final String CODIGO_PARTIDA_PROPERTY = "Cod.Partida";
    static final String SERIE_PROPERTY = "SERIE";
    static final String NUMERO_PROPERTY = "Número";
    static final String NITPROVEEDOR_FACTURA_PROPERTY = "NIT";
    static final String PROVEEDOR_PROPERTY = "Proveedor";
    static final String FECHA_FACTURA_PROPERTY = "Fecha";
    static final String ID_LIQUIDADOR_FACTURA_PROPERTY = "Liquidador";
    static final String ID_LIQUIDACION_FACTURA_PROPERTY = "Liquidación";
    static final String DESCRIPCION_FACTURA_PROPERTY = "Descripcion";
    static final String CODIGOCC_PROPERTY = "CodigoCC";
    static final String SERIE_NUMERO_PROPERTY = "Serie/No.";
    Grid.FooterRow footerFactura;

    Grid partidasGrid;
    public IndexedContainer containerPartida = new IndexedContainer();
    static final String ID_PARTIDA_PROPERTY = "Id";
    static final String CUENTA_PARTIDA_PROPERTY = "Cuenta";
    static final String DESCRIPCION_PROPERTY = "Descripción";
    static final String DEBE_PROPERTY = "Debe";
    static final String HABER_PROPERTY = "Haber";
    Grid.FooterRow footerPartida;

    Button notaCreditoBtn;

    Statement stQuery;
    ResultSet rsRecords;
    Statement stQuery1, stQuery2;
    ResultSet rsRecords2;
    Statement stQuery3;
    ResultSet rsRecords3;
    String queryString;

    DateField inicioDt;
    DateField finDt;
    Button consultarBtn;

    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");

    Button continuarBtn;
    Button cerrarBtn;
    Button revisadoBtn;
    Button editBtn;

    UI mainUI = UI.getCurrent();
    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public IngresoLiquidacionGastoView() {
        setWidth("100%");
        setSpacing(true);
        setMargin(true);

        Label iconLbl = new Label(FontAwesome.MONEY.getHtml(), ContentMode.HTML);
        iconLbl.setSizeUndefined();

        Label titleLbl = new Label(empresaNombre);
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.addStyleName(ValoTheme.LABEL_BOLD);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h2_custom");

        Label subtitleLbl = new Label("— Liquidaciones de Caja Chica");
        subtitleLbl.addStyleName(ValoTheme.LABEL_H3);
        subtitleLbl.addStyleName(ValoTheme.LABEL_COLORED);
        subtitleLbl.setSizeUndefined();

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setSpacing(true);
        titleLayout.setWidth("100%");
        titleLayout.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
        titleLayout.addComponents(iconLbl, titleLbl, subtitleLbl);

        addComponent(titleLayout);
        setComponentAlignment(titleLayout, Alignment.TOP_LEFT);

        crearTablaLiquidaciones();
        createTablaFacturasYPartidas();

        if (partidasGrid != null) {
            llenarTablaLiquidacion(empresaId);
        }
    }

    public void crearTablaLiquidaciones() {

        Label seccionLbl = new Label(FontAwesome.LIST_ALT.getHtml() + "  Liquidaciones registradas", ContentMode.HTML);
        seccionLbl.addStyleName(ValoTheme.LABEL_H3);
        seccionLbl.addStyleName(ValoTheme.LABEL_COLORED);

        VerticalLayout liquidacionesLayout = new VerticalLayout();
        liquidacionesLayout.addStyleName("rcorners3");
        liquidacionesLayout.setWidth("100%");
        liquidacionesLayout.setResponsive(true);
        liquidacionesLayout.setSpacing(true);
        liquidacionesLayout.setMargin(true);

        HorizontalLayout filtrosLayout = new HorizontalLayout();
        filtrosLayout.setSpacing(true);
        filtrosLayout.setDefaultComponentAlignment(Alignment.BOTTOM_LEFT);

        inicioDt = new DateField("Desde:");
        inicioDt.setDateFormat("dd/MM/yyyy");
        inicioDt.setValue(Utileria.getPrimerDiaDelMes());
        inicioDt.setWidth("10em");

        finDt = new DateField("Hasta:");
        finDt.setDateFormat("dd/MM/yyyy");
        finDt.setValue(Utileria.getUltimoDiaDelMes());
        finDt.setWidth("10em");

        consultarBtn = new Button("Consultar");
        consultarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        consultarBtn.setIcon(FontAwesome.SEARCH);
        consultarBtn.addClickListener(event -> llenarTablaLiquidacion(empresaId));

        Button newBtn = new Button("Nueva liquidación");
        newBtn.setIcon(FontAwesome.PLUS_CIRCLE);
        newBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        newBtn.setDescription("Agregar nueva liquidación.");
        newBtn.addClickListener(event -> {
            try {
                containerFactura.removeAllItems();
                IngresoLiquidacionGastoForm newIngreso =
                        new IngresoLiquidacionGastoForm("", "", "", "", "");
                UI.getCurrent().addWindow(newIngreso);
                newIngreso.center();
            } catch (Exception ex) {
                System.out.println("Error en el boton nuevo" + ex);
                ex.printStackTrace();
            }
        });

        containerLiquidacion.addContainerProperty(ID_PROPERTY, String.class, null);
        containerLiquidacion.addContainerProperty(ID_LIQUIDACION_PROPERTY, String.class, null);
        containerLiquidacion.addContainerProperty(LIQUIDADOR_PROPERTY, String.class, null);
        containerLiquidacion.addContainerProperty(MONTO_QUETZALES_PROPERTY, String.class, null);
        containerLiquidacion.addContainerProperty(ESTATUS_PROPERTY, String.class, null);
        containerLiquidacion.addContainerProperty(ID_LIQUIDADOR_PROPERTY, String.class, null);
        containerLiquidacion.addContainerProperty(IMPRIMIR_PROPERTY, String.class, null);
        containerLiquidacion.addContainerProperty(CODIGOCC_PROPERTY, String.class, null);

        liquidacionesGrid = new Grid(containerLiquidacion);
        liquidacionesGrid.setWidth("100%");
        liquidacionesGrid.setImmediate(true);
        liquidacionesGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        liquidacionesGrid.setDescription("Seleccione una liquidación.");
        liquidacionesGrid.setHeightMode(HeightMode.ROW);
        liquidacionesGrid.setHeightByRows(4);
        liquidacionesGrid.setResponsive(true);
        liquidacionesGrid.setEditorBuffered(false);

        liquidacionesGrid.getColumn(IMPRIMIR_PROPERTY).setRenderer(new ButtonRenderer(e -> {
            ReporteLiquidacionPDF reporteLiquidacionPDF = new ReporteLiquidacionPDF(
                    empresaId, empresaNombre, getEmpresaNit(),
                    String.valueOf(containerLiquidacion.getContainerProperty(e.getItemId(), ID_LIQUIDACION_PROPERTY).getValue()),
                    String.valueOf(containerLiquidacion.getContainerProperty(e.getItemId(), LIQUIDADOR_PROPERTY).getValue())
            );
            mainUI.addWindow(reporteLiquidacionPDF);
            reporteLiquidacionPDF.center();
        }));

        liquidacionesGrid.getColumn(ID_PROPERTY).setHidable(true).setHidden(true);
        liquidacionesGrid.getColumn(ID_LIQUIDADOR_PROPERTY).setHidable(true).setHidden(true);
        liquidacionesGrid.getColumn(CODIGOCC_PROPERTY).setHidable(true).setHidden(true);

        liquidacionesGrid.getColumn(ID_LIQUIDACION_PROPERTY).setWidth(95);
        liquidacionesGrid.getColumn(MONTO_QUETZALES_PROPERTY).setWidth(130);
        liquidacionesGrid.getColumn(ESTATUS_PROPERTY).setWidth(115);
        liquidacionesGrid.getColumn(IMPRIMIR_PROPERTY).setWidth(100);
        liquidacionesGrid.getColumn(LIQUIDADOR_PROPERTY).setExpandRatio(1);

        liquidacionesGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if (ID_LIQUIDACION_PROPERTY.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else if (MONTO_QUETZALES_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            }
            return null;
        });

        liquidacionesGrid.addSelectionListener(event -> {
            if (liquidacionesGrid.getSelectedRow() != null) {
                continuarBtn.setEnabled(true);
                cerrarBtn.setEnabled(true);
                // Resetear botones dependientes de factura hasta nueva selección
                notaCreditoBtn.setEnabled(false);
                editBtn.setEnabled(false);
                revisadoBtn.setEnabled(false);

                llenarTablaFactura(
                        String.valueOf(liquidacionesGrid.getContainerDataSource()
                                .getItem(liquidacionesGrid.getSelectedRow())
                                .getItemProperty(ID_LIQUIDACION_PROPERTY).getValue())
                );
            } else {
                continuarBtn.setEnabled(false);
                cerrarBtn.setEnabled(false);
            }
        });

        footerliquidaciones = liquidacionesGrid.appendFooterRow();
        footerliquidaciones.getCell(LIQUIDADOR_PROPERTY).setText("Total");
        footerliquidaciones.getCell(MONTO_QUETZALES_PROPERTY).setText("0.00");
        footerliquidaciones.getCell(MONTO_QUETZALES_PROPERTY).setStyleName("rightalign");

        filtrosLayout.addComponents(inicioDt, finDt, consultarBtn, newBtn);

        liquidacionesLayout.addComponent(seccionLbl);
        liquidacionesLayout.addComponent(filtrosLayout);
        liquidacionesLayout.setComponentAlignment(filtrosLayout, Alignment.MIDDLE_LEFT);
        liquidacionesLayout.addComponent(liquidacionesGrid);

        addComponent(liquidacionesLayout);
        setComponentAlignment(liquidacionesLayout, Alignment.MIDDLE_CENTER);
    }

    public void createTablaFacturasYPartidas() {

        VerticalLayout detalleLayout = new VerticalLayout();
        detalleLayout.setWidth("100%");
        detalleLayout.addStyleName("rcorners2");
        detalleLayout.setSpacing(true);
        detalleLayout.setMargin(true);

        Label seccionDetalle = new Label(FontAwesome.FILE_TEXT_O.getHtml() + "  Detalle de la liquidación", ContentMode.HTML);
        seccionDetalle.addStyleName(ValoTheme.LABEL_H3);
        seccionDetalle.addStyleName(ValoTheme.LABEL_COLORED);

        HorizontalLayout facturasYPartidasLayout = new HorizontalLayout();
        facturasYPartidasLayout.setWidth("100%");
        facturasYPartidasLayout.setSpacing(true);

        HorizontalLayout botonesLayout = new HorizontalLayout();
        botonesLayout.setSpacing(true);
        botonesLayout.setWidth("100%");

        detalleLayout.addComponents(seccionDetalle, facturasYPartidasLayout, botonesLayout);

        // --- Container facturas ---
        containerFactura.addContainerProperty(ID_FACTURA_PROPERTY, String.class, null);
        containerFactura.addContainerProperty(CODIGO_PARTIDA_PROPERTY, String.class, null);
        containerFactura.addContainerProperty(TIPODOCUMENTO_PROPERTY, String.class, null);
        containerFactura.addContainerProperty(FECHA_FACTURA_PROPERTY, String.class, null);
        containerFactura.addContainerProperty(SERIE_PROPERTY, String.class, null);
        containerFactura.addContainerProperty(NUMERO_PROPERTY, String.class, null);
        containerFactura.addContainerProperty(PROVEEDOR_PROPERTY, String.class, null);
        containerFactura.addContainerProperty(NITPROVEEDOR_FACTURA_PROPERTY, String.class, null);
        containerFactura.addContainerProperty(MONTO_QUETZALES_PROPERTY, String.class, null);
        containerFactura.addContainerProperty(ID_LIQUIDACION_FACTURA_PROPERTY, String.class, null);
        containerFactura.addContainerProperty(ID_LIQUIDADOR_FACTURA_PROPERTY, String.class, null);
        containerFactura.addContainerProperty(DESCRIPCION_FACTURA_PROPERTY, String.class, null);
        containerFactura.addContainerProperty(ESTATUS_PROPERTY, String.class, null);

        // Columna generada que combina Serie + Número
        GeneratedPropertyContainer gpcFacturas = new GeneratedPropertyContainer(containerFactura);
        gpcFacturas.addGeneratedProperty(SERIE_NUMERO_PROPERTY, new PropertyValueGenerator<String>() {
            @Override
            public String getValue(com.vaadin.data.Item item, Object itemId, Object propertyId) {
                String serie = (String) item.getItemProperty(SERIE_PROPERTY).getValue();
                String numero = (String) item.getItemProperty(NUMERO_PROPERTY).getValue();
                return (serie != null ? serie : "") + " " + (numero != null ? numero : "");
            }
            @Override
            public Class<String> getType() { return String.class; }
        });

        facturasGrid = new Grid("Facturas de la liquidación", gpcFacturas);
        facturasGrid.setImmediate(true);
        facturasGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        facturasGrid.setDescription("Seleccione una factura.");
        facturasGrid.setHeightMode(HeightMode.ROW);
        facturasGrid.setHeightByRows(3);
        facturasGrid.setWidth("100%");
        facturasGrid.setResponsive(true);
        facturasGrid.setEditorBuffered(false);

        // Columnas ocultas (datos internos)
        facturasGrid.getColumn(ID_FACTURA_PROPERTY).setHidable(true).setHidden(true);
        facturasGrid.getColumn(ID_LIQUIDACION_FACTURA_PROPERTY).setHidable(true).setHidden(true);
        facturasGrid.getColumn(CODIGO_PARTIDA_PROPERTY).setHidable(true).setHidden(true);
        facturasGrid.getColumn(ID_LIQUIDADOR_FACTURA_PROPERTY).setHidable(true).setHidden(true);
        facturasGrid.getColumn(DESCRIPCION_FACTURA_PROPERTY).setHidable(true).setHidden(true);
        facturasGrid.getColumn(TIPODOCUMENTO_PROPERTY).setHidable(true).setHidden(true);
        facturasGrid.getColumn(SERIE_PROPERTY).setHidable(true).setHidden(true);
        facturasGrid.getColumn(NUMERO_PROPERTY).setHidable(true).setHidden(true);
        facturasGrid.getColumn(NITPROVEEDOR_FACTURA_PROPERTY).setHidable(true).setHidden(true);

        // Columnas visibles con anchos
        facturasGrid.getColumn(SERIE_NUMERO_PROPERTY).setWidth(130);
        facturasGrid.getColumn(FECHA_FACTURA_PROPERTY).setWidth(90);
        facturasGrid.getColumn(MONTO_QUETZALES_PROPERTY).setWidth(130);
        facturasGrid.getColumn(ESTATUS_PROPERTY).setWidth(100);
        facturasGrid.getColumn(PROVEEDOR_PROPERTY).setExpandRatio(1);

        facturasGrid.addSelectionListener(event -> {
            if (facturasGrid.getSelectedRow() != null) {
                String estatusFactura = String.valueOf(
                        containerFactura.getContainerProperty(facturasGrid.getSelectedRow(), ESTATUS_PROPERTY).getValue()
                );
                boolean esAuxiliar = ((SopdiUI) UI.getCurrent()).sessionInformation
                        .getStrUserProfileName().equals("AUXILIAR");

                revisadoBtn.setEnabled(false);
                editBtn.setEnabled(false);
                notaCreditoBtn.setEnabled(false);

                switch (estatusFactura) {
                    case "INGRESADO":
                        editBtn.setEnabled(true);
                        notaCreditoBtn.setEnabled(true);
                        revisadoBtn.setEnabled(!esAuxiliar);
                        break;
                    case "REVISADO":
                    case "CERRADO":
                        if (!esAuxiliar) {
                            editBtn.setEnabled(true);
                            notaCreditoBtn.setEnabled(true);
                        }
                        break;
                }

                llenarTablaPartida(
                        String.valueOf(facturasGrid.getContainerDataSource().getItem(facturasGrid.getSelectedRow()).getItemProperty(ID_LIQUIDACION_FACTURA_PROPERTY).getValue()),
                        String.valueOf(facturasGrid.getContainerDataSource().getItem(facturasGrid.getSelectedRow()).getItemProperty(CODIGO_PARTIDA_PROPERTY).getValue())
                );
            }
        });

        facturasGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if (MONTO_QUETZALES_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            }
            return null;
        });

        footerFactura = facturasGrid.appendFooterRow();
        footerFactura.getCell(FECHA_FACTURA_PROPERTY).setText("Total");
        footerFactura.getCell(FECHA_FACTURA_PROPERTY).setStyleName("rightalign");
        footerFactura.getCell(MONTO_QUETZALES_PROPERTY).setText("0.00");
        footerFactura.getCell(MONTO_QUETZALES_PROPERTY).setStyleName("rightalign");

        // --- Container partidas ---
        containerPartida.addContainerProperty(ID_PARTIDA_PROPERTY, String.class, null);
        containerPartida.addContainerProperty(CUENTA_PARTIDA_PROPERTY, String.class, null);
        containerPartida.addContainerProperty(DESCRIPCION_PROPERTY, String.class, null);
        containerPartida.addContainerProperty(DEBE_PROPERTY, String.class, null);
        containerPartida.addContainerProperty(HABER_PROPERTY, String.class, null);

        partidasGrid = new Grid("Partida contable", containerPartida);
        partidasGrid.setImmediate(true);
        partidasGrid.setSelectionMode(Grid.SelectionMode.NONE);
        partidasGrid.setHeightMode(HeightMode.ROW);
        partidasGrid.setHeightByRows(3);
        partidasGrid.setWidth("100%");
        partidasGrid.setResponsive(true);
        partidasGrid.setEditorBuffered(false);

        partidasGrid.getColumn(ID_PARTIDA_PROPERTY).setHidable(true).setHidden(true);

        partidasGrid.getColumn(CUENTA_PARTIDA_PROPERTY).setWidth(145);
        partidasGrid.getColumn(DEBE_PROPERTY).setWidth(130);
        partidasGrid.getColumn(HABER_PROPERTY).setWidth(130);
        partidasGrid.getColumn(DESCRIPCION_PROPERTY).setExpandRatio(1);

        partidasGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if (DEBE_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (HABER_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            }
            return null;
        });

        footerPartida = partidasGrid.appendFooterRow();
        footerPartida.getCell(DESCRIPCION_PROPERTY).setText("SUMAS IGUALES");
        footerPartida.getCell(DESCRIPCION_PROPERTY).setStyleName("rightalign");
        footerPartida.getCell(DEBE_PROPERTY).setText("0.00");
        footerPartida.getCell(HABER_PROPERTY).setText("0.00");
        footerPartida.getCell(DEBE_PROPERTY).setStyleName("rightalign");
        footerPartida.getCell(HABER_PROPERTY).setStyleName("rightalign");

        // Grids: facturas ocupa 60%, partidas 40%
        VerticalLayout facturasWrapper = new VerticalLayout();
        facturasWrapper.setWidth("100%");
        facturasWrapper.setSpacing(false);
        facturasWrapper.setMargin(false);
        facturasWrapper.addComponent(facturasGrid);

        VerticalLayout partidasWrapper = new VerticalLayout();
        partidasWrapper.setWidth("100%");
        partidasWrapper.setSpacing(false);
        partidasWrapper.setMargin(false);
        partidasWrapper.addComponent(partidasGrid);

        facturasYPartidasLayout.addComponent(facturasWrapper);
        facturasYPartidasLayout.addComponent(partidasWrapper);
        facturasYPartidasLayout.setExpandRatio(facturasWrapper, 6);
        facturasYPartidasLayout.setExpandRatio(partidasWrapper, 4);

        // --- Botones ---
        continuarBtn = new Button("Más facturas");
        continuarBtn.setIcon(FontAwesome.NEWSPAPER_O);
        continuarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        continuarBtn.setDescription("Continuar ingresando facturas de ésta liquidación");
        continuarBtn.setEnabled(false);
        continuarBtn.addClickListener(event -> {
            if (liquidacionesGrid.getSelectedRow() == null) {
                Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
            } else {
                String estatus = String.valueOf(containerLiquidacion.getContainerProperty(liquidacionesGrid.getSelectedRow(), ESTATUS_PROPERTY).getValue());
                if (estatus.equals("INGRESADO") || estatus.equals("REVISADO")) {
                    IngresoLiquidacionGastoForm newFacturasGasto = new IngresoLiquidacionGastoForm(
                            String.valueOf(containerLiquidacion.getContainerProperty(liquidacionesGrid.getSelectedRow(), ID_PROPERTY).getValue()),
                            String.valueOf(containerLiquidacion.getContainerProperty(liquidacionesGrid.getSelectedRow(), ID_LIQUIDACION_PROPERTY).getValue()),
                            String.valueOf(containerLiquidacion.getContainerProperty(liquidacionesGrid.getSelectedRow(), ID_LIQUIDADOR_PROPERTY).getValue()),
                            empresaId,
                            String.valueOf(containerLiquidacion.getContainerProperty(liquidacionesGrid.getSelectedRow(), CODIGOCC_PROPERTY).getValue())
                    );
                    UI.getCurrent().addWindow(newFacturasGasto);
                    newFacturasGasto.center();
                } else {
                    Notification.show("No se puede modificar una liquidación ya CERRADA / PAGADA.", Notification.Type.ERROR_MESSAGE);
                }
            }
        });

        notaCreditoBtn = new Button("Nota de crédito");
        notaCreditoBtn.setIcon(FontAwesome.CREDIT_CARD);
        notaCreditoBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        notaCreditoBtn.setDescription("Registrar nota de crédito para la factura seleccionada");
        notaCreditoBtn.setEnabled(false);
        notaCreditoBtn.addClickListener(event -> {
            if (facturasGrid.getSelectedRow() == null) {
                Notification notif = new Notification("Por favor, seleccione el registro correspondiente.",
                        Notification.Type.WARNING_MESSAGE);
                notif.setDelayMsec(1500);
                notif.setPosition(Position.MIDDLE_CENTER);
                notif.setIcon(FontAwesome.WARNING);
                notif.show(Page.getCurrent());
            } else {
                if (String.valueOf(containerFactura.getContainerProperty(facturasGrid.getSelectedRow(), TIPODOCUMENTO_PROPERTY).getValue()).equals("FACTURA")) {
                    NotaCreditoCompra nuevaNotaCredito = new NotaCreditoCompra(
                            empresaId, containerFactura, facturasGrid.getSelectedRow(),
                            String.valueOf(containerFactura.getContainerProperty(facturasGrid.getSelectedRow(), CODIGO_PARTIDA_PROPERTY).getValue()),
                            String.valueOf(containerFactura.getContainerProperty(facturasGrid.getSelectedRow(), SERIE_PROPERTY).getValue()),
                            String.valueOf(containerFactura.getContainerProperty(facturasGrid.getSelectedRow(), NUMERO_PROPERTY).getValue())
                    );
                    UI.getCurrent().addWindow(nuevaNotaCredito);
                    nuevaNotaCredito.center();
                    nuevaNotaCredito.getSerieTxt().focus();
                } else {
                    Notification notif = new Notification("SOLO SE PERMITEN NOTAS DE CRÉDITO PARA FACTURAS.",
                            Notification.Type.WARNING_MESSAGE);
                    notif.setDelayMsec(1500);
                    notif.setPosition(Position.MIDDLE_CENTER);
                    notif.setIcon(FontAwesome.WARNING);
                    notif.show(Page.getCurrent());
                }
            }
        });

        cerrarBtn = new Button("Cerrar liquidación");
        cerrarBtn.setIcon(FontAwesome.LOCK);
        cerrarBtn.addStyleName(ValoTheme.BUTTON_DANGER);
        cerrarBtn.setDescription("Cerrar ésta liquidación");
        cerrarBtn.setEnabled(false);
        cerrarBtn.addClickListener(event -> {
            if (liquidacionesGrid.getSelectedRow() == null) {
                Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
            } else {
                String estatus = String.valueOf(containerLiquidacion.getContainerProperty(liquidacionesGrid.getSelectedRow(), ESTATUS_PROPERTY).getValue());
                if (estatus.equals("CERRADO") || estatus.equals("PAGADO")) {
                    Notification.show("Liquidación ya CERRADA o PAGADA. Seleccione una ABIERTA.", Notification.Type.ERROR_MESSAGE);
                } else {
                    ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de CERRAR esta liquidación?\nYa no podrá agregar más facturas.",
                            "SI", "NO", new ConfirmDialog.Listener() {
                                public void onClose(ConfirmDialog dialog) {
                                    if (dialog.isConfirmed()) {
                                        cerrarLiquidacion();
                                    }
                                }
                            });
                }
            }
        });

        editBtn = new Button("Editar");
        editBtn.setIcon(FontAwesome.EDIT);
        editBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        editBtn.setDescription("Actualizar datos del documento y partida contable.");
        editBtn.setEnabled(false);
        editBtn.addClickListener(event -> {
            if (liquidacionesGrid.getSelectedRow() == null || facturasGrid.getSelectedRow() == null) {
                Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
            } else {
                queryString = "UPDATE contabilidad_partida";
                queryString += " SET Estatus = 'INGRESADO'";
                queryString += " WHERE IdLiquidador = " + containerLiquidacion.getContainerProperty(liquidacionesGrid.getSelectedRow(), ID_LIQUIDADOR_PROPERTY).getValue();
                queryString += " AND IdLiquidacion = " + containerLiquidacion.getContainerProperty(liquidacionesGrid.getSelectedRow(), ID_LIQUIDACION_PROPERTY).getValue();
                queryString += " AND IdEmpresa = " + empresaId;
                queryString += " AND CodigoPartida = '" + containerFactura.getContainerProperty(facturasGrid.getSelectedRow(), CODIGO_PARTIDA_PROPERTY).getValue() + "'";

                try {
                    stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                    stQuery.executeUpdate(queryString);
                } catch (SQLException ex) {
                    System.out.println("Error al intentar modificar estatus a INGRESADO" + ex);
                    Notification.show("ERROR AL INTENTAR CAMBIAR EL ESTATUS INGRESADO A PARTIDA CONTABLE", Notification.Type.ERROR_MESSAGE);
                    ex.printStackTrace();
                }

                EditarPartidaLiquidacion partidaLiquidacion = new EditarPartidaLiquidacion(
                        String.valueOf(containerFactura.getContainerProperty(facturasGrid.getSelectedRow(), CODIGO_PARTIDA_PROPERTY).getValue()),
                        String.valueOf(containerLiquidacion.getContainerProperty(liquidacionesGrid.getSelectedRow(), CODIGOCC_PROPERTY).getValue())
                );
                UI.getCurrent().addWindow(partidaLiquidacion);
                partidaLiquidacion.center();
            }
        });

        revisadoBtn = new Button("Revisado");
        revisadoBtn.setIcon(FontAwesome.CHECK_CIRCLE);
        revisadoBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        revisadoBtn.setDescription("Dar por revisado un documento / partida contable.");
        revisadoBtn.setEnabled(false);
        revisadoBtn.addClickListener(event -> {
            if (facturasGrid.getSelectedRow() == null) {
                Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
                return;
            }

            ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de dar por REVISADA esta partida contable?",
                    "SI", "NO", new ConfirmDialog.Listener() {
                        public void onClose(ConfirmDialog dialog) {
                            if (dialog.isConfirmed()) {
                                queryString = "UPDATE contabilidad_partida";
                                queryString += " SET Estatus = 'REVISADO'";
                                queryString += " WHERE IdLiquidador = " + containerLiquidacion.getContainerProperty(liquidacionesGrid.getSelectedRow(), ID_LIQUIDADOR_PROPERTY).getValue();
                                queryString += " AND IdLiquidacion = " + containerLiquidacion.getContainerProperty(liquidacionesGrid.getSelectedRow(), ID_LIQUIDACION_PROPERTY).getValue();
                                queryString += " AND IdEmpresa = " + empresaId;
                                queryString += " AND CodigoPartida = '" + containerFactura.getContainerProperty(facturasGrid.getSelectedRow(), CODIGO_PARTIDA_PROPERTY).getValue() + "'";

                                try {
                                    stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                                    stQuery.executeUpdate(queryString);
                                    llenarTablaLiquidacion(empresaId);
                                } catch (SQLException ex) {
                                    System.out.println("Error al intentar modificar estatus a REVISADO" + ex);
                                    Notification.show("ERROR AL INTENTAR CAMBIAR EL ESTATUS A REVISADO DE PARTIDA CONTABLE", Notification.Type.ERROR_MESSAGE);
                                    ex.printStackTrace();
                                }
                            }
                        }
                    });
        });

        // Grupo izquierdo: acciones sobre la liquidación/factura
        HorizontalLayout leftButtons = new HorizontalLayout();
        leftButtons.setSpacing(true);
        leftButtons.addComponents(continuarBtn, notaCreditoBtn);

        // Grupo derecho: cambios de estado
        HorizontalLayout rightButtons = new HorizontalLayout();
        rightButtons.setSpacing(true);
        rightButtons.addComponents(editBtn, revisadoBtn, cerrarBtn);

        Label spacer = new Label();
        botonesLayout.addComponents(leftButtons, spacer, rightButtons);
        botonesLayout.setExpandRatio(spacer, 1);
        botonesLayout.setComponentAlignment(leftButtons, Alignment.MIDDLE_LEFT);
        botonesLayout.setComponentAlignment(rightButtons, Alignment.MIDDLE_RIGHT);

        addComponent(detalleLayout);
        setComponentAlignment(detalleLayout, Alignment.MIDDLE_CENTER);
    }

    public void llenarTablaFactura(String idLiquidacion) {

        containerFactura.removeAllItems();
        containerPartida.removeAllItems();

        footerPartida.getCell(DEBE_PROPERTY).setText("0.00");
        footerPartida.getCell(HABER_PROPERTY).setText("0.00");
        footerFactura.getCell(MONTO_QUETZALES_PROPERTY).setText("0.00");

        this.facturasGrid.setCaption("Facturas de la liquidación : " + idLiquidacion);

        queryString = " SELECT IdPartida, CodigoPartida, TipoDocumento, IdLiquidacion, IdEmpresa, IdLiquidador,";
        queryString += " NumeroDocumento,SerieDocumento,NombreProveedor,NITProveedor, Fecha, Estatus, Haber,";
        queryString += " MonedaDocumento, DebeQuetzales, HaberQuetzales,  TipoCambio";
        queryString += " FROM contabilidad_partida";
        queryString += " WHERE IdEmpresa = " + empresaId;
        queryString += " AND IdLiquidacion = " + idLiquidacion;
        queryString += " AND IdNomenclatura = " + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getLiquidacionesCajaChicha();
        queryString += " GROUP BY NumeroDocumento,SerieDocumento,NombreProveedor,NITProveedor";

        try {
            stQuery3 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords3 = stQuery3.executeQuery(queryString);

            if (rsRecords3.next()) {
                totalMontoFactura = 0.00;
                do {
                    Object itemId = containerFactura.addItem();
                    containerFactura.getContainerProperty(itemId, ID_FACTURA_PROPERTY).setValue(rsRecords3.getString("IdPartida"));
                    containerFactura.getContainerProperty(itemId, CODIGO_PARTIDA_PROPERTY).setValue(rsRecords3.getString("CodigoPartida"));
                    containerFactura.getContainerProperty(itemId, TIPODOCUMENTO_PROPERTY).setValue(rsRecords3.getString("TipoDocumento"));
                    containerFactura.getContainerProperty(itemId, SERIE_PROPERTY).setValue(rsRecords3.getString("SerieDocumento"));
                    containerFactura.getContainerProperty(itemId, NUMERO_PROPERTY).setValue(rsRecords3.getString("NumeroDocumento"));
                    containerFactura.getContainerProperty(itemId, NITPROVEEDOR_FACTURA_PROPERTY).setValue(rsRecords3.getString("NITProveedor"));
                    containerFactura.getContainerProperty(itemId, PROVEEDOR_PROPERTY).setValue(rsRecords3.getString("NombreProveedor"));
                    containerFactura.getContainerProperty(itemId, FECHA_FACTURA_PROPERTY).setValue(rsRecords3.getString("Fecha"));
                    containerFactura.getContainerProperty(itemId, MONTO_QUETZALES_PROPERTY).setValue(numberFormat.format(rsRecords3.getDouble("HaberQuetzales")));
                    containerFactura.getContainerProperty(itemId, ID_LIQUIDACION_FACTURA_PROPERTY).setValue(rsRecords3.getString("IdLiquidacion"));
                    containerFactura.getContainerProperty(itemId, ID_LIQUIDADOR_FACTURA_PROPERTY).setValue(rsRecords3.getString("IdLiquidador"));
                    containerFactura.getContainerProperty(itemId, ESTATUS_PROPERTY).setValue(rsRecords3.getString("ESTATUS"));

                    totalMontoFactura = totalMontoFactura + rsRecords3.getDouble("Haber");
                } while (rsRecords3.next());

                facturasGrid.select(facturasGrid.getContainerDataSource().getIdByIndex(0));
                footerFactura.getCell(MONTO_QUETZALES_PROPERTY).setText(numberFormat.format(totalMontoFactura));
            }
        } catch (Exception ex) {
            System.out.println("Error al listar tabla partida facturas:" + ex);
            ex.printStackTrace();
        }
    }

    public void llenarTablaLiquidacion(String empresa) {
        containerLiquidacion.removeAllItems();
        containerFactura.removeAllItems();
        containerPartida.removeAllItems();

        footerliquidaciones.getCell(MONTO_QUETZALES_PROPERTY).setText("0.00");
        totalMonto = 0.00;

        try {
            if (inicioDt.getValue().before(finDt.getValue())) {

                queryString = " SELECT contabilidad_partida.IdPartida,";
                queryString += " contabilidad_partida.Fecha, ";
                queryString += " contabilidad_partida.MonedaDocumento, contabilidad_partida.TipoCambio,";
                queryString += " SUM(HaberQuetzales) as TotalQ,";
                queryString += " SUM(Haber) as Total, CodigoCC,";
                queryString += " contabilidad_partida.Estatus,";
                queryString += " contabilidad_partida.IdLiquidacion,";
                queryString += " contabilidad_partida.IdLiquidador,";
                queryString += " contabilidad_partida.IdEmpresa,";
                queryString += " proveedor_empresa.Nombre as NLiquidador";
                queryString += " FROM contabilidad_partida, proveedor_empresa ";
                queryString += " WHERE contabilidad_partida.Fecha BETWEEN ";
                queryString += "     '" + Utileria.getFechaYYYYMMDD_1(inicioDt.getValue()) + "'";
                queryString += " AND '" + Utileria.getFechaYYYYMMDD_1(finDt.getValue()) + "'";
                queryString += " AND contabilidad_partida.IdEmpresa =" + empresa;
                queryString += " AND contabilidad_partida.IdLiquidacion > 0 ";
                queryString += " AND proveedor_empresa.IdProveedor = contabilidad_partida.IdLiquidador";
                queryString += " AND contabilidad_partida.IdNomenclatura = " + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getLiquidacionesCajaChicha();
                queryString += " AND proveedor_empresa.IdEmpresa = " + empresa;
                queryString += " GROUP BY contabilidad_partida.IdLiquidacion";
                queryString += " ORDER BY contabilidad_partida.IdLiquidacion, contabilidad_partida.Estatus desc";

                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "QUERY LIQUIDACIONES : " + queryString);

                stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                stQuery2 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                rsRecords = stQuery.executeQuery(queryString);

                String estatus = "";

                if (rsRecords.next()) {
                    do {
                        queryString = " SELECT * FROM contabilidad_partida ";
                        queryString += " WHERE Fecha BETWEEN '" + Utileria.getFechaYYYYMMDD_1(inicioDt.getValue()) + "'";
                        queryString += " AND '" + Utileria.getFechaYYYYMMDD_1(finDt.getValue()) + "'";
                        queryString += " AND IdLiquidacion = " + rsRecords.getString("IdLiquidacion");
                        queryString += " AND IdEmpresa = " + empresaId;
                        queryString += " ORDER BY contabilidad_partida.IdLiquidacion, contabilidad_partida.Estatus desc";

                        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "QUERY PARTIDAS LIQUIDACION : " + queryString);
                        rsRecords2 = stQuery2.executeQuery(queryString);

                        if (rsRecords2.next()) {
                            do {
                                if (rsRecords.getString("Estatus").equals("REVISADO") &&
                                        rsRecords2.getString("Estatus").equals("REVISADO")) {
                                    estatus = "REVISADO";
                                } else if (rsRecords.getString("Estatus").equals("REVISADO") &&
                                        rsRecords2.getString("Estatus").equals("INGRESADO")) {
                                    estatus = "INGRESADO";
                                } else if (rsRecords.getString("Estatus").equals("INGRESADO") &&
                                        rsRecords2.getString("Estatus").equals("REVISADO")) {
                                    estatus = "INGRESADO";
                                } else if (rsRecords.getString("Estatus").equals("INGRESADO") &&
                                        rsRecords2.getString("Estatus").equals("INGRESADO")) {
                                    estatus = "INGRESADO";
                                } else if (rsRecords.getString("Estatus").equals("CERRADO") &&
                                        rsRecords2.getString("Estatus").equals("CERRADO")) {
                                    estatus = "CERRADO";
                                } else if (rsRecords.getString("Estatus").equals("PAGADO") &&
                                        rsRecords2.getString("Estatus").equals("PAGADO")) {
                                    estatus = "PAGADO";
                                }
                            } while (rsRecords2.next());

                            Object itemId = containerLiquidacion.addItem();
                            containerLiquidacion.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords.getString("IdPartida"));
                            containerLiquidacion.getContainerProperty(itemId, ID_LIQUIDACION_PROPERTY).setValue(rsRecords.getString("IdLiquidacion"));
                            containerLiquidacion.getContainerProperty(itemId, LIQUIDADOR_PROPERTY).setValue(rsRecords.getString("NLiquidador"));
                            containerLiquidacion.getContainerProperty(itemId, MONTO_QUETZALES_PROPERTY).setValue(numberFormat.format(rsRecords.getDouble("TotalQ")));
                            containerLiquidacion.getContainerProperty(itemId, ESTATUS_PROPERTY).setValue(estatus);
                            containerLiquidacion.getContainerProperty(itemId, ID_LIQUIDADOR_PROPERTY).setValue(rsRecords.getString("IdLiquidador"));
                            containerLiquidacion.getContainerProperty(itemId, IMPRIMIR_PROPERTY).setValue("Imprimir");
                            containerLiquidacion.getContainerProperty(itemId, CODIGOCC_PROPERTY).setValue(rsRecords.getString("CodigoCC"));
                        }

                        totalMonto = totalMonto + rsRecords.getDouble("Total");

                    } while (rsRecords.next());

                    liquidacionesGrid.select(liquidacionesGrid.getContainerDataSource().getIdByIndex(0));
                    footerliquidaciones.getCell(MONTO_QUETZALES_PROPERTY).setText(numberFormat.format(totalMonto));
                }
            } else {
                Notification.show("La fecha hasta no puede contener un valor menor a la fecha de inicio.", Notification.Type.WARNING_MESSAGE);
                inicioDt.focus();
            }
        } catch (Exception ex) {
            System.out.println("Error al listar tabla Facturas INGRESO LIQUIDACIONESGASTOVIEW" + ex);
            Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public void llenarTablaPartida(String idLiquidacion, String codigoPartida) {
        containerPartida.removeAllItems();

        totalDebe = 0.00;
        totalHaber = 0.00;

        String documento = String.valueOf(facturasGrid.getContainerDataSource().getItem(facturasGrid.getSelectedRow()).getItemProperty(SERIE_PROPERTY).getValue());
        documento += " " + facturasGrid.getContainerDataSource().getItem(facturasGrid.getSelectedRow()).getItemProperty(NUMERO_PROPERTY).getValue();
//        documento += " " + String.valueOf(facturasGrid.getContainerDataSource().getItem(facturasGrid.getSelectedRow()).getItemProperty(PROVEEDOR_PROPERTY).getValue());

        if (partidasGrid != null) {
            partidasGrid.setCaption("Partida : " + codigoPartida + " — " + documento);
        }

        queryString = " SELECT contabilidad_partida.IdPartida, contabilidad_partida.IdNomenclatura, ";
        queryString += " contabilidad_partida.Debe, contabilidad_partida.Haber, contabilidad_partida.MonedaDocumento, ";
        queryString += " contabilidad_nomenclatura_empresa.N5, contabilidad_nomenclatura_empresa.NoCuenta";
        queryString += " FROM contabilidad_partida, contabilidad_nomenclatura_empresa";
        queryString += " WHERE contabilidad_partida.CodigoPartida = '" + codigoPartida + "'";
        queryString += " AND contabilidad_partida.IdLiquidacion  = " + idLiquidacion;
        queryString += " AND contabilidad_nomenclatura_empresa.IdNomenclatura = contabilidad_partida.IdNomenclatura";
        queryString += " AND contabilidad_partida.IdEmpresa = " + empresaId;
        queryString += " AND contabilidad_nomenclatura_empresa.IdEmpresa = " + empresaId;

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                do {
                    Object itemId = containerPartida.addItem();
                    containerPartida.getContainerProperty(itemId, ID_PARTIDA_PROPERTY).setValue(rsRecords.getString("IdPartida"));
                    containerPartida.getContainerProperty(itemId, CUENTA_PARTIDA_PROPERTY).setValue(rsRecords.getString("NoCuenta"));
                    containerPartida.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(rsRecords.getString("N5"));
                    if (rsRecords.getDouble("Debe") > 0.00) {
                        if (rsRecords.getString("MonedaDocumento").equals("QUETZALES")) {
                            containerPartida.getContainerProperty(itemId, DEBE_PROPERTY).setValue("Q." + numberFormat.format(rsRecords.getDouble("Debe")));
                        } else {
                            containerPartida.getContainerProperty(itemId, DEBE_PROPERTY).setValue("$." + numberFormat.format(rsRecords.getDouble("Debe")));
                        }
                    } else {
                        containerPartida.getContainerProperty(itemId, DEBE_PROPERTY).setValue("");
                    }
                    if (rsRecords.getDouble("Haber") > 0.00) {
                        if (rsRecords.getString("MonedaDocumento").equals("QUETZALES")) {
                            containerPartida.getContainerProperty(itemId, HABER_PROPERTY).setValue("Q." + numberFormat.format(rsRecords.getDouble("Haber")));
                        } else {
                            containerPartida.getContainerProperty(itemId, HABER_PROPERTY).setValue("$." + numberFormat.format(rsRecords.getDouble("Haber")));
                        }
                    } else {
                        containerPartida.getContainerProperty(itemId, HABER_PROPERTY).setValue("");
                    }

                    totalDebe = totalDebe + rsRecords.getDouble("Debe");
                    totalHaber = totalHaber + rsRecords.getDouble("Haber");
                } while (rsRecords.next());

                footerPartida.getCell(DEBE_PROPERTY).setText(numberFormat.format(totalDebe));
                footerPartida.getCell(HABER_PROPERTY).setText(numberFormat.format(totalHaber));
            }
        } catch (Exception ex) {
            System.out.println("Error al listar tabla PARTIDA:" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void cerrarLiquidacion() {
        queryString = " UPDATE contabilidad_partida";
        queryString += " SET Estatus = 'CERRADO'";
        queryString += " WHERE IdLiquidador = " + containerLiquidacion.getContainerProperty(liquidacionesGrid.getSelectedRow(), ID_LIQUIDADOR_PROPERTY).getValue();
        queryString += " AND  IdLiquidacion = " + containerLiquidacion.getContainerProperty(liquidacionesGrid.getSelectedRow(), ID_LIQUIDACION_PROPERTY).getValue();
        queryString += " AND  IdEmpresa = " + empresaId;

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            Notification.show("Liquidación cerrada exitosamente.", Notification.Type.HUMANIZED_MESSAGE);

            containerLiquidacion.getContainerProperty(liquidacionesGrid.getSelectedRow(), ESTATUS_PROPERTY).setValue("CERRADO");

        } catch (Exception ex) {
            System.out.println("Error al intentar Modificar Estatus" + ex);
            Notification.show("Error al modificar estatus : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }

    public void abrirLiquidacion() {
        queryString = " UPDATE contabilidad_partida";
        queryString += " SET Estatus = 'INGRESADO'";
        queryString += " WHERE IdLiquidador = " + containerLiquidacion.getContainerProperty(liquidacionesGrid.getSelectedRow(), ID_LIQUIDADOR_PROPERTY).getValue();
        queryString += " AND  IdLiquidacion = " + containerLiquidacion.getContainerProperty(liquidacionesGrid.getSelectedRow(), ID_LIQUIDACION_PROPERTY).getValue();
        queryString += " AND  IdEmpresa = " + empresaId;

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            containerLiquidacion.getContainerProperty(liquidacionesGrid.getSelectedRow(), ESTATUS_PROPERTY).setValue("INGRESADO");

        } catch (Exception ex) {
            System.out.println("Error al intentar Modificar Estatus" + ex);
            Notification.show("Error al modificar estatus : " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }

    public String getEmpresaNit() {
        String strNit = "N/A";

        queryString = " SELECT Nit FROM contabilidad_empresa ";
        queryString += " WHERE IdEmpresa = " + empresaId;

        try {
            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords2 = stQuery1.executeQuery(queryString);

            if (rsRecords2.next()) {
                strNit = rsRecords2.getString("Nit");
            }

        } catch (Exception ex1) {
            System.out.println("Error al buscar NIT de empresa: " + ex1.getMessage());
            ex1.printStackTrace();
        }

        return strNit;
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - Liquidaciones caja chica");
    }
}