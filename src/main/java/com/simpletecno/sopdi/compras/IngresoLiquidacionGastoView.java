package com.simpletecno.sopdi.compras;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.SelectionEvent;
import com.vaadin.event.SelectionEvent.SelectionListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
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
import java.util.Date;
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

        Label titleLbl = new Label(empresaId + " " + empresaNombre + " LIQUIDACIONES CAJA CHICA");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h2_custom");

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

        crearTablaLiquidaciones();
        createTablaFacturasYPartidas();

        if (partidasGrid != null) {
            llenarTablaLiquidacion(empresaId);
        }
    }

    public void crearTablaLiquidaciones() {

        VerticalLayout liquidacionesLayout = new VerticalLayout();
        liquidacionesLayout.addStyleName("rcorners3");
        liquidacionesLayout.setWidth("100%");
        liquidacionesLayout.setResponsive(true);
        liquidacionesLayout.setSpacing(true);

        HorizontalLayout filtrosLayout = new HorizontalLayout();
        filtrosLayout.setSpacing(true);

        inicioDt = new DateField("Facturas Desde:");
        inicioDt.setDateFormat("dd/MM/yyyy");
        Date primerDia = Utileria.getPrimerDiaDelMes();
        inicioDt.setValue(primerDia);
        inicioDt.setWidth("10em");

        finDt = new DateField("Facturas Hasta:");
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
                llenarTablaLiquidacion(empresaId);
            }
        });

        Button newBtn = new Button("Nueva liquidación");
        newBtn.setIcon(FontAwesome.PLUS);
        newBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        newBtn.setDescription("Agregar nueva liquidación.");
        newBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
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

        liquidacionesGrid = new Grid("Listado de liquidaciones", containerLiquidacion);
        liquidacionesGrid.setWidth("100%");
        liquidacionesGrid.setImmediate(true);
        liquidacionesGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        liquidacionesGrid.setDescription("Seleccione un registro.");
        liquidacionesGrid.setHeightMode(HeightMode.ROW);
        liquidacionesGrid.setHeightByRows(5);
        liquidacionesGrid.setResponsive(true);
        liquidacionesGrid.setEditorBuffered(false);

        liquidacionesGrid.getColumn(IMPRIMIR_PROPERTY).setRenderer(new ButtonRenderer(e
                -> {
            ReporteLiquidacionPDF reporteLiquidacionPDF
                    = new ReporteLiquidacionPDF(
                    empresaId,
                    empresaNombre,
                    getEmpresaNit(),
                    String.valueOf(containerLiquidacion.getContainerProperty(e.getItemId(), ID_LIQUIDACION_PROPERTY).getValue()),
                    String.valueOf(containerLiquidacion.getContainerProperty(e.getItemId(), LIQUIDADOR_PROPERTY).getValue())
            );
            mainUI.addWindow(reporteLiquidacionPDF);
            reporteLiquidacionPDF.center();
        }));

        liquidacionesGrid.getColumn(ID_PROPERTY).setHidable(true).setHidden(true);
        liquidacionesGrid.getColumn(ID_LIQUIDADOR_PROPERTY).setHidable(true).setHidden(true);
        liquidacionesGrid.getColumn(CODIGOCC_PROPERTY).setHidable(true).setHidden(true);
        liquidacionesGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {

            if (ID_LIQUIDACION_PROPERTY.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else if (MONTO_QUETZALES_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }

        });

        liquidacionesGrid.addSelectionListener(new SelectionListener() {
            @Override
            public void select(SelectionEvent event) {
                if (liquidacionesGrid.getSelectedRow() != null) {
                    llenarTablaFactura(
                            String.valueOf(liquidacionesGrid.getContainerDataSource().getItem(liquidacionesGrid.getSelectedRow()).getItemProperty(ID_LIQUIDACION_PROPERTY).getValue())
                    );
                }
            }
        });

        footerliquidaciones = liquidacionesGrid.appendFooterRow();
        footerliquidaciones.getCell(LIQUIDADOR_PROPERTY).setText("Total");
        footerliquidaciones.getCell(MONTO_QUETZALES_PROPERTY).setText("0.00");
        footerliquidaciones.getCell(MONTO_QUETZALES_PROPERTY).setStyleName("rightalign");

        filtrosLayout.addComponent(inicioDt);
        filtrosLayout.addComponent(finDt);
        filtrosLayout.addComponent(consultarBtn);
        filtrosLayout.setComponentAlignment(consultarBtn, Alignment.BOTTOM_CENTER);
        filtrosLayout.addComponent(newBtn);
        filtrosLayout.setComponentAlignment(newBtn, Alignment.BOTTOM_CENTER);

        liquidacionesLayout.addComponent(filtrosLayout);
        liquidacionesLayout.setComponentAlignment(filtrosLayout, Alignment.MIDDLE_CENTER);

        liquidacionesLayout.addComponent(liquidacionesGrid);
        liquidacionesLayout.setComponentAlignment(liquidacionesGrid, Alignment.MIDDLE_CENTER);

        addComponent(liquidacionesLayout);
        setComponentAlignment(liquidacionesLayout, Alignment.MIDDLE_CENTER);
    }

    public void createTablaFacturasYPartidas() {

        VerticalLayout detalleLayout = new VerticalLayout();
        detalleLayout.setWidth("100%");
        detalleLayout.addStyleName("rcorners2");
        detalleLayout.setSpacing(true);

        HorizontalLayout facturasYPartidasLayout = new HorizontalLayout();
        facturasYPartidasLayout.setWidth("100%");
        facturasYPartidasLayout.addStyleName("rcorners3");
        facturasYPartidasLayout.setSpacing(true);

        HorizontalLayout botonesLayout = new HorizontalLayout();
        botonesLayout.setSpacing(true);
        botonesLayout.setWidth("100%");

        detalleLayout.addComponents(facturasYPartidasLayout, botonesLayout);

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

        facturasGrid = new Grid(containerFactura);
        facturasGrid.setImmediate(true);
        facturasGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        facturasGrid.setDescription("Seleccione un registro.");
        facturasGrid.setHeightMode(HeightMode.ROW);
        facturasGrid.setHeightByRows(4);
        facturasGrid.setWidth("100%");
        facturasGrid.setResponsive(true);
        facturasGrid.setEditorBuffered(false);

        facturasGrid.getColumn(ID_FACTURA_PROPERTY).setHidable(true).setHidden(true);
        facturasGrid.getColumn(ID_LIQUIDACION_FACTURA_PROPERTY).setHidable(true).setHidden(true);
        facturasGrid.getColumn(CODIGO_PARTIDA_PROPERTY).setHidable(true).setHidden(true);
        facturasGrid.getColumn(ID_LIQUIDADOR_FACTURA_PROPERTY).setHidable(true).setHidden(true);
        facturasGrid.getColumn(DESCRIPCION_FACTURA_PROPERTY).setHidable(true).setHidden(true);

        facturasGrid.addSelectionListener(new SelectionListener() {
            @Override
            public void select(SelectionEvent event) {
                if (facturasGrid.getSelectedRow() != null) {
                    llenarTablaPartida(
                            String.valueOf(facturasGrid.getContainerDataSource().getItem(facturasGrid.getSelectedRow()).getItemProperty(ID_LIQUIDACION_FACTURA_PROPERTY).getValue()),
                            String.valueOf(facturasGrid.getContainerDataSource().getItem(facturasGrid.getSelectedRow()).getItemProperty(CODIGO_PARTIDA_PROPERTY).getValue())
                    );
                }
            }
        });

        facturasGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {

//            System.out.println("celda=" + cellReference.getProperty());
            if (MONTO_QUETZALES_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (HABER_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }

        });

        footerFactura = facturasGrid.appendFooterRow();
        footerFactura.getCell(FECHA_FACTURA_PROPERTY).setText("Total");
        footerFactura.getCell(FECHA_FACTURA_PROPERTY).setStyleName("rightalign");
        footerFactura.getCell(MONTO_QUETZALES_PROPERTY).setText("0.00");
        footerFactura.getCell(MONTO_QUETZALES_PROPERTY).setStyleName("rightalign");

        containerPartida.addContainerProperty(ID_PARTIDA_PROPERTY, String.class, null);
        containerPartida.addContainerProperty(CUENTA_PARTIDA_PROPERTY, String.class, null);
        containerPartida.addContainerProperty(DESCRIPCION_PROPERTY, String.class, null);
        containerPartida.addContainerProperty(DEBE_PROPERTY, String.class, null);
        containerPartida.addContainerProperty(HABER_PROPERTY, String.class, null);

        partidasGrid = new Grid("Partida contable", containerPartida);
        partidasGrid.setImmediate(true);
        partidasGrid.setSelectionMode(Grid.SelectionMode.NONE);
        partidasGrid.setHeightMode(HeightMode.ROW);
        partidasGrid.setHeightByRows(4);
        partidasGrid.setWidth("100%");
        partidasGrid.setResponsive(true);
        partidasGrid.setEditorBuffered(false);

        partidasGrid.getColumn(ID_FACTURA_PROPERTY).setHidable(true).setHidden(true);

        partidasGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {

//            System.out.println("celda=" + cellReference.getProperty());
            if (DEBE_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (HABER_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }

        });

        partidasGrid.addSelectionListener(new SelectionListener() {
            @Override
            public void select(SelectionEvent event) {

                revisadoBtn.setEnabled(false);
                editBtn.setEnabled(false);
                notaCreditoBtn.setEnabled(false);

                switch (String.valueOf(partidasGrid.getContainerDataSource().getItem(partidasGrid.getSelectedRow()).getItemProperty(ESTATUS_PROPERTY).getValue())) {
                    case "INGRESADO":
                        if (!((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserProfileName().equals("AUXILIAR")) {
                            revisadoBtn.setEnabled(true);
                        }
                        editBtn.setEnabled(true);
                        notaCreditoBtn.setEnabled(true);
                        break;
                    case "REVISADO":
                    case "CERRADO":
                        if (!((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserProfileName().equals("AUXILIAR")) {
                            editBtn.setEnabled(true);
                            notaCreditoBtn.setEnabled(true);
                        }
                        break;
                }
                if (((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserProfileName().equals("AUXILIAR")) {
                    revisadoBtn.setEnabled(false);
                }
            }
        });

        footerPartida = partidasGrid.appendFooterRow();
        footerPartida.getCell(DESCRIPCION_PROPERTY).setText("SUMAS IGUALES");
        footerPartida.getCell(DESCRIPCION_PROPERTY).setStyleName("rightalign");
        footerPartida.getCell(DEBE_PROPERTY).setText("0.00");
        footerPartida.getCell(HABER_PROPERTY).setText("0.00");
        footerPartida.getCell(DEBE_PROPERTY).setStyleName("rightalign");
        footerPartida.getCell(HABER_PROPERTY).setStyleName("rightalign");

        facturasYPartidasLayout.addComponent(facturasGrid);
        facturasYPartidasLayout.addComponent(partidasGrid);

        continuarBtn = new Button("Ingresar más facturas");
        continuarBtn.setIcon(FontAwesome.NEWSPAPER_O);
        continuarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        continuarBtn.setDescription("Continuar ingresando facturas de ésta liquidación");
        continuarBtn.addListener((Button.ClickListener) (Button.ClickEvent event) -> {

            if (liquidacionesGrid.getSelectedRow() == null) {
                Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
            } else {

                if (String.valueOf(containerLiquidacion.getContainerProperty(liquidacionesGrid.getSelectedRow(), ESTATUS_PROPERTY).getValue()).equals("INGRESADO")
                        || String.valueOf(containerLiquidacion.getContainerProperty(liquidacionesGrid.getSelectedRow(), ESTATUS_PROPERTY).getValue()).equals("REVISADO")) {
                    IngresoLiquidacionGastoForm newFacturasGasto
                            = new IngresoLiquidacionGastoForm(
                            String.valueOf(containerLiquidacion.getContainerProperty(liquidacionesGrid.getSelectedRow(), ID_PROPERTY).getValue()),
                            String.valueOf(containerLiquidacion.getContainerProperty(liquidacionesGrid.getSelectedRow(), ID_LIQUIDACION_PROPERTY).getValue()),
                            String.valueOf(containerLiquidacion.getContainerProperty(liquidacionesGrid.getSelectedRow(), ID_LIQUIDADOR_PROPERTY).getValue()),
                            empresaId,
                            String.valueOf(containerLiquidacion.getContainerProperty(liquidacionesGrid.getSelectedRow(), CODIGOCC_PROPERTY).getValue()));
                    UI.getCurrent().addWindow(newFacturasGasto);
                    newFacturasGasto.center();
                } else {
                    Notification.show("No se puede modificar una liquidación ya CERRADA / PAGADA.", Notification.Type.ERROR_MESSAGE);
                }
            }
        });

        notaCreditoBtn = new Button("NOTA DE CREDITO");
        notaCreditoBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        notaCreditoBtn.setDescription("NOTA DE CREDITO");
        notaCreditoBtn.addListener((Button.ClickListener) (Button.ClickEvent event) -> {

            if (facturasGrid.getSelectedRow() == null) {
                Notification notif = new Notification("Por favor, seleccione el registro correspondiente.",
                        Notification.Type.WARNING_MESSAGE);
                notif.setDelayMsec(1500);
                notif.setPosition(Position.MIDDLE_CENTER);
                notif.setIcon(FontAwesome.WARNING);
                notif.show(Page.getCurrent());
            } else {

                if (String.valueOf(containerFactura.getContainerProperty(facturasGrid.getSelectedRow(), TIPODOCUMENTO_PROPERTY).getValue()).equals("FACTURA")) {
                    NotaCreditoCompra nuevaNotaCredito
                            = new NotaCreditoCompra(
                            empresaId,
                            containerFactura,
                            facturasGrid.getSelectedRow(),
                            String.valueOf(containerFactura.getContainerProperty(facturasGrid.getSelectedRow(), CODIGO_PARTIDA_PROPERTY).getValue()),
                            String.valueOf(containerFactura.getContainerProperty(facturasGrid.getSelectedRow(), SERIE_PROPERTY).getValue()),
                            String.valueOf(containerFactura.getContainerProperty(facturasGrid.getSelectedRow(), NUMERO_PROPERTY).getValue())
                    );
                    UI.getCurrent().addWindow(nuevaNotaCredito);
                    nuevaNotaCredito.center();
                    nuevaNotaCredito.getSerieTxt().focus();
                }
                else {
                    Notification notif = new Notification("SOLO SE PERMITEN NOTAS DE CREDITO PARA FACTURAS.",
                            Notification.Type.WARNING_MESSAGE);
                    notif.setDelayMsec(1500);
                    notif.setPosition(Position.MIDDLE_CENTER);
                    notif.setIcon(FontAwesome.WARNING);
                    notif.show(Page.getCurrent());
                }
            }
        });
//        notaCreditoBtn.setVisible(false);

        cerrarBtn = new Button("Cerrar liquidación");
        cerrarBtn.setIcon(FontAwesome.CLOSE);
        cerrarBtn.addStyleName(ValoTheme.BUTTON_DANGER);
        cerrarBtn.setDescription("Cerrar ésta liquidación");
        cerrarBtn.addListener((Button.ClickListener) (Button.ClickEvent event) -> {

            if (liquidacionesGrid.getSelectedRow() == null) {
                Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
            } else {
                if (String.valueOf(containerLiquidacion.getContainerProperty(liquidacionesGrid.getSelectedRow(), ESTATUS_PROPERTY).getValue()).equals("CERRADO")
                        || String.valueOf(containerLiquidacion.getContainerProperty(liquidacionesGrid.getSelectedRow(), ESTATUS_PROPERTY).getValue()).equals("PAGADO")) {
                    Notification.show("Liquidación ya CERRADA o PAGADA. Seleccione una ABIERTA.", Notification.Type.ERROR_MESSAGE);
                } else {
                    ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de CERRAR esta liquidación ?\nTome en cuenta que ya no podrá agregar más facturas a esta liquidación.",
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
        editBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (facturasGrid.getSelectedRow() == null) {
                    Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
                } else {
                    queryString = "UPDATE  contabilidad_partida";
                    queryString += " SET Estatus = 'INGRESADO'";
                    queryString += " WHERE IdLiquidador = " + String.valueOf(containerLiquidacion.getContainerProperty(liquidacionesGrid.getSelectedRow(), ID_LIQUIDADOR_PROPERTY).getValue());
                    queryString += " AND IdLiquidacion = " + String.valueOf(containerLiquidacion.getContainerProperty(liquidacionesGrid.getSelectedRow(), ID_LIQUIDACION_PROPERTY).getValue());
                    queryString += " AND IdEmpresa = " + empresaId;
                    queryString += " AND CodigoPartida = '" + String.valueOf(containerFactura.getContainerProperty(facturasGrid.getSelectedRow(), CODIGO_PARTIDA_PROPERTY).getValue()) + "'";

                    try {
//                        System.out.println("query editar liquidacion" + queryString);
                        stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
                        stQuery.executeUpdate(queryString);
                    } catch (SQLException ex) {
                        System.out.println("Error al intentar modificar estatus a INGRESADO" + ex);
                        Notification.show("ERROR AL INTENTAR CAMBIAR EL ESTATUS INGRESADO A PARTIDA CONTABLE", Notification.Type.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }

                    EditarPartidaLiquidacion partidaLiquidacion
                            = new EditarPartidaLiquidacion(
                            String.valueOf(containerFactura.getContainerProperty(facturasGrid.getSelectedRow(), CODIGO_PARTIDA_PROPERTY).getValue()),
                            String.valueOf(containerLiquidacion.getContainerProperty(liquidacionesGrid.getSelectedRow(), CODIGOCC_PROPERTY).getValue()));
                    UI.getCurrent().addWindow(partidaLiquidacion);
                    partidaLiquidacion.center();
                }
            }
        });

        revisadoBtn = new Button("Revisado");
        revisadoBtn.setIcon(FontAwesome.CHECK);
        revisadoBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        revisadoBtn.setDescription("Dar por revisado un documento / partida contable.");
        revisadoBtn.addListener((Button.ClickListener) (Button.ClickEvent event) -> {

            if (facturasGrid.getSelectedRow() == null) {
                Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
            }

            ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de dar por REVISADA esta partida contable  ?",
                    "SI", "NO", new ConfirmDialog.Listener() {

                        public void onClose(ConfirmDialog dialog) {
                            if (dialog.isConfirmed()) {

                                queryString = "UPDATE  contabilidad_partida";
                                queryString += " SET Estatus = 'REVISADO'";
                                queryString += " WHERE IdLiquidador = " + String.valueOf(containerLiquidacion.getContainerProperty(liquidacionesGrid.getSelectedRow(), ID_LIQUIDADOR_PROPERTY).getValue());
                                queryString += " AND IdLiquidacion = " + String.valueOf(containerLiquidacion.getContainerProperty(liquidacionesGrid.getSelectedRow(), ID_LIQUIDACION_PROPERTY).getValue());
                                queryString += " AND IdEmpresa = " + empresaId;
                                queryString += " AND CodigoPartida = '" + String.valueOf(containerFactura.getContainerProperty(facturasGrid.getSelectedRow(), CODIGO_PARTIDA_PROPERTY).getValue()) + "'";

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

        botonesLayout.addComponents(continuarBtn, notaCreditoBtn, cerrarBtn);
        botonesLayout.setComponentAlignment(continuarBtn, Alignment.BOTTOM_LEFT);
        botonesLayout.setComponentAlignment(notaCreditoBtn, Alignment.BOTTOM_LEFT);
        botonesLayout.setComponentAlignment(cerrarBtn, Alignment.BOTTOM_RIGHT);
        botonesLayout.addComponent(editBtn);
        botonesLayout.setComponentAlignment(editBtn, Alignment.BOTTOM_RIGHT);
        botonesLayout.addComponent(revisadoBtn);
        botonesLayout.setComponentAlignment(revisadoBtn, Alignment.BOTTOM_RIGHT);

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
        queryString += " AND IdNomenclatura = " +  ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getLiquidacionesCajaChicha();
        queryString += " GROUP BY NumeroDocumento,SerieDocumento,NombreProveedor,NITProveedor";

        try {
            stQuery3 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords3 = stQuery3.executeQuery(queryString);

            if (rsRecords3.next()) { //  encontrado  
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

            if (inicioDt.getValue().before(finDt.getValue()) == true) {

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
                queryString += " AND contabilidad_partida.IdLiquidacion > 0 "; // PARA QUE MUESTRE SOLAMENTE LAS LIQUIDACIONES
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

                if (rsRecords.next()) { //  encontrado                                                
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
                                }else if (rsRecords.getString("Estatus").equals("PAGADO") &&
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
        documento += " " + String.valueOf(facturasGrid.getContainerDataSource().getItem(facturasGrid.getSelectedRow()).getItemProperty(NUMERO_PROPERTY).getValue());
        documento += " " + String.valueOf(facturasGrid.getContainerDataSource().getItem(facturasGrid.getSelectedRow()).getItemProperty(PROVEEDOR_PROPERTY).getValue());

        if (partidasGrid != null) {
            partidasGrid.setCaption("Partida contable  : " + codigoPartida + " Documento : " + documento);
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

//System.out.println("queryString Liqudacion partida = " + queryString);

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado                                                
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
        queryString += " WHERE IdLiquidador = " + String.valueOf(containerLiquidacion.getContainerProperty(liquidacionesGrid.getSelectedRow(), ID_LIQUIDADOR_PROPERTY).getValue());
        queryString += " AND  IdLiquidacion = " + String.valueOf(containerLiquidacion.getContainerProperty(liquidacionesGrid.getSelectedRow(), ID_LIQUIDACION_PROPERTY).getValue());
        queryString += " AND  IdEmpresa = " + empresaId;

//System.out.println("Query cerrrar liquidacion=" + queryString);

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
        queryString += " WHERE IdLiquidador = " + String.valueOf(containerLiquidacion.getContainerProperty(liquidacionesGrid.getSelectedRow(), ID_LIQUIDADOR_PROPERTY).getValue());
        queryString += " AND  IdLiquidacion = " + String.valueOf(containerLiquidacion.getContainerProperty(liquidacionesGrid.getSelectedRow(), ID_LIQUIDACION_PROPERTY).getValue());
        queryString += " AND  IdEmpresa = " + empresaId;

//System.out.println("Query cerrrar liquidacion=" + queryString);

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

//            Notification.show("Liquidación  exitosamente.", Notification.Type.HUMANIZED_MESSAGE);

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
