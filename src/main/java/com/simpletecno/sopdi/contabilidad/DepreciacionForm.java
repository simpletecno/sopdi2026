package com.simpletecno.sopdi.contabilidad;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.renderers.DateRenderer;
import com.vaadin.ui.renderers.NumberRenderer;
import com.vaadin.ui.themes.ValoTheme;
import org.atmosphere.interceptor.AtmosphereResourceStateRecovery;
import org.vaadin.ui.NumberField;
import org.vaadin.dialogs.ConfirmDialog;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Ventana separada que contiene únicamente el formulario de ingreso de activos.
 * No realiza la inserción automática — devuelve los valores mediante getters para
 * que el integrador construya la sentencia INSERT.
 */
public class DepreciacionForm extends Window {
    private final ComboBox cbCentroCosto = new ComboBox("Centro de Costo:");
    private final TextField tfNombre = new TextField("Nombre del activo:");
    private final ComboBox cbTipo = new ComboBox("Tipo de depreciación:");
    private final NumberField nfMeses = new NumberField("Mes:");
    private final TextField tfNumero = new TextField("Numero:");
    private final TextField tfSerie = new TextField("Serie:");
    private final NumberField nfValor = new NumberField("Valor inicial:");
    private final NumberField nfReduccion = new NumberField("Reducción:");
    private final NumberField nfValorResidual = new NumberField("Valor residual:");
    private final PopupDateField dfFechaInicio = new PopupDateField("Fecha Inicio:");
    private final PopupDateField dfFechaFin = new PopupDateField("Fecha Fin:");
    private final PopupDateField dfFechaInicioBusquedaPartidas = new PopupDateField("Del:");
    private final PopupDateField dfFechaFinBusquedaPartidas = new PopupDateField("Al:");

    private final Button btnGuardar = new Button("Guardar");
    private final Button btnCancelar = new Button("Cancelar");

    // Variables para almacenar el activo guardado

    private final IndexedContainer partidaContainer = new IndexedContainer();
    private final Grid partidasGrid = new Grid("Documentos:");

    // Constantes para los nombres de las columnas de la tabla de partidas
    private static final String IDPROVEEDOR = "ID Proveedor";
    private static final String PROVEEDOR = "Proveedor";
    private static final String CODIGO_PARTIDA = "Codigo Partida";
    private static final String DESCRIPCION = "Descripcion";
    private static final String SERIE_NUMERO = "Serie | Numero";
    private static final String MONTO = "Monto";
    private static final String MONTO_SIN_IVA = "Monto Sin IVA";
    private static final String ACTIVOS_SUM = "Depreciaciones";
    private static final String COUNT_AMORTIZACIONES = "Amortizaciones";
    
    private final IndexedContainer historialContainer = new IndexedContainer();
    private final Grid historialGrid = new Grid();

    // Columna para mostrar el historial de depreciación
    private static final String ID = "ID";
    private static final String DEPRECIACION = "Depre.";
    private static final String VALOR = "Val.";
    private static final String ACUMULADO = "";

    // Conbinado
    private static final String FECHA = "Fecha";

    // Variables para almacenar tipo de depreciación seleccionado
    private DepreciacionesView.TipoDepreciacion tipoDepreciacionSeleccionado = null; // Almacena el tipo de depreciación seleccionado
    private Label lblTipoSeleccionado = null; // Etiqueta para mostrar el tipo de depreciación seleccionado

    private final String empresaId;
    private final UI mainUI;

    Statement stQuery;
    ResultSet rsRecords;
    Statement stQuery1;
    ResultSet rsRecords1;
    String queryString;

    VerticalLayout formLayout;
    
    // Variables para manejar múltiples activos por factura
    private final List<ActivoDistribucion> activosACrear = new ArrayList<>();
    
    // Clase interna para representar cada activo a crear
    private static class ActivoDistribucion {
        String nombre;
        BigDecimal valor;
        String numero;
        String serie;
        
        ActivoDistribucion(String nombre, BigDecimal valor, String numero, String serie) {
            this.nombre = nombre;
            this.valor = valor;
            this.numero = numero;
            this.serie = serie;
        }
    }

    public DepreciacionForm(List<DepreciacionesView.TipoDepreciacion> tipos) {
        super("Depreciación - Formulario");

        this.mainUI = UI.getCurrent();

        if (!(this.mainUI instanceof SopdiUI)) {
            throw new IllegalStateException("UI actual no es instancia de SopdiUI");
        }

        SopdiUI sopdiUI = (SopdiUI) this.mainUI;

        if (sopdiUI.sessionInformation == null) {
            throw new IllegalStateException("sessionInformation no está inicializado");
        }

        this.empresaId = sopdiUI.sessionInformation.getStrAccountingCompanyId();

        center();
        setModal(true);
        setWidth("90%");
        setHeight("90%");

        llenarComboCentroCosto();


        MarginInfo marginInfo = new MarginInfo(false, true, false, true);

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setMargin(marginInfo);
        mainLayout.setResponsive(true);
        mainLayout.setWidth("100%");

        HorizontalLayout contentLayout = new HorizontalLayout();
        contentLayout.setWidth("100%");
        contentLayout.setMargin(false);
        contentLayout.setSpacing(true);

        crearFormulario(tipos);

        HorizontalLayout layoutTitle = new HorizontalLayout();
        layoutTitle.setSpacing(true);
        layoutTitle.setMargin(false);
        layoutTitle.setWidth("100%");

        VerticalLayout historialLayout = crearHistorial();
        contentLayout.addComponents(formLayout, historialLayout);
        contentLayout.setComponentAlignment(formLayout, Alignment.TOP_CENTER);
        contentLayout.setComponentAlignment(historialLayout, Alignment.MIDDLE_CENTER);
        contentLayout.setExpandRatio(formLayout, 2.0f);
        contentLayout.setExpandRatio(historialLayout, 1.5f);

        Label titleLbl = new Label("CREAR DEPRECIACIÓN");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setWidth("100%");
        titleLbl.addStyleName("h2_custom");


        layoutTitle.addComponent(titleLbl);
        layoutTitle.setComponentAlignment(titleLbl, Alignment.BOTTOM_RIGHT);

        mainLayout.addComponents(layoutTitle, contentLayout);

        cargarPartidasCompra();
        setContent(mainLayout);
    }

    private void crearFormulario(List<DepreciacionesView.TipoDepreciacion> tipos) {
        formLayout = new VerticalLayout();
        formLayout.setMargin(true);
        formLayout.setSpacing(true);
        formLayout.setWidth("99%");

        tfNombre.setWidth("100%");
        tfNombre.setRequired(true);

        cbCentroCosto.setWidth("100%");
        cbCentroCosto.setRequired(true);
        cbCentroCosto.setTextInputAllowed(false);

        tfNumero.setWidth("100%");
        tfNumero.setEnabled(false);

        tfSerie.setWidth("100%");
        tfSerie.setEnabled(false);

        nfValor.setWidth("100%");
        nfValor.addValueChangeListener(event -> {
            if (nfValor.getValue() != null && tipoDepreciacionSeleccionado != null) {
                BigDecimal valor = new BigDecimal(nfValor.getValue());
                int meses = tipoDepreciacionSeleccionado.meses;
                BigDecimal reduccion = valor.divide(new BigDecimal(meses), 2, RoundingMode.HALF_UP);
                BigDecimal sobrante = valor.subtract(reduccion.multiply(new BigDecimal(meses)));
                nfReduccion.setValue(reduccion.toString());
                nfValorResidual.setValue(sobrante.toString());
                cargarHistorialDepreciacion();
            } else {
                nfReduccion.setValue("");
                nfValorResidual.setValue("");
            }
        });

        nfReduccion.setWidth("100%");
        nfReduccion.addValueChangeListener(event -> {
            if (nfReduccion.getValue() != null && nfValor.getValue() != null && tipoDepreciacionSeleccionado != null) {
                BigDecimal valor = new BigDecimal(nfValor.getValue());
                BigDecimal reduccion = new BigDecimal(nfReduccion.getValue());
                int meses = tipoDepreciacionSeleccionado.meses;
                BigDecimal sobrante = valor.subtract(reduccion.multiply(new BigDecimal(meses)));
                nfValorResidual.setValue(sobrante.toString());
                cargarHistorialDepreciacion();
            }
        });

        nfValorResidual.setWidth("100%");
        nfValorResidual.addValueChangeListener(event -> {
            // Al modificar residual, solo actualizar tabla
            if (nfValorResidual.getValue() != null) {
                cargarHistorialDepreciacion();
            }
        });

        dfFechaInicio.setAssistiveText("Seleccione la fecha de alta del activo");
        dfFechaInicio.setDateFormat("dd/MM/yyyy");
        dfFechaInicio.addValueChangeListener(event -> {
            if (dfFechaInicio.getValue() != null && tipoDepreciacionSeleccionado != null) {
                // Usar directamente los meses del tipo seleccionado
                int meses = tipoDepreciacionSeleccionado.meses;
                long mesMillis = (long) meses * 30L * 24L * 60L * 60L * 1000L;
                Date fechaFin =  Utileria.getUltimoFechaDelMes(new Date(dfFechaInicio.getValue().getTime() + mesMillis));
                dfFechaFin.setValue(fechaFin);
            } else {
                dfFechaFin.setValue(null);
            }
        });

        dfFechaFin.setAssistiveText("Seleccione la fecha de alta del activo");
        dfFechaFin.setDateFormat("dd/MM/yyyy");
        dfFechaFin.setEnabled(false);

        if (tipos != null) {
            for (DepreciacionesView.TipoDepreciacion t : tipos) {
                cbTipo.addItem(t);
                cbTipo.setItemCaption(t, t.id + " (" + t.meses + " meses)");
            }
            if (!tipos.isEmpty()) cbTipo.setValue(tipos.get(0));
        }

        btnGuardar.setIcon(FontAwesome.SAVE);
        btnGuardar.setWidth(120, Sizeable.UNITS_PIXELS);
        btnGuardar.addClickListener(event -> {
            if (guardarActivo()) {
                // Limpiar formulario y cerrar ventana si se guardó exitosamente
                close();
            }
        });

        btnCancelar.setIcon(FontAwesome.TIMES);
        btnCancelar.setWidth(120, Sizeable.UNITS_PIXELS);
        btnCancelar.addClickListener(event -> close());

        HorizontalLayout hLayoutUpper = new HorizontalLayout();
        hLayoutUpper.setSizeFull();
        hLayoutUpper.setMargin(false);
        hLayoutUpper.setSpacing(true);
        hLayoutUpper.addStyleName("rcorners3");
        hLayoutUpper.setWidth("99%");
        hLayoutUpper.addComponents(tfNombre, cbCentroCosto);
        hLayoutUpper.setExpandRatio(tfNombre, 1.0f);
        hLayoutUpper.setExpandRatio(cbCentroCosto, 0.25f);

        HorizontalLayout hLayoutMiddle = new HorizontalLayout();
        hLayoutMiddle.setSizeFull();
        hLayoutMiddle.setMargin(false);
        hLayoutMiddle.setSpacing(true);
        hLayoutMiddle.setWidth("99%");
        hLayoutMiddle.addComponents(crearActivosGrid());

        HorizontalLayout hLayoutLower = new HorizontalLayout();
        hLayoutLower.setSizeFull();
        hLayoutLower.setMargin(false);
        hLayoutLower.setSpacing(true);
        hLayoutLower.setWidth("99%");

        VerticalLayout fechaLayout = new VerticalLayout();
        fechaLayout.setCaption("Fechas de depreciación:");
        fechaLayout.addStyleName("rcorners3");
        fechaLayout.setHeight("100%");
        fechaLayout.setMargin(false);
        fechaLayout.setSpacing(true);
        fechaLayout.addComponents(dfFechaInicio, dfFechaFin);
        fechaLayout.setComponentAlignment(dfFechaFin, Alignment.TOP_CENTER);
        fechaLayout.setComponentAlignment(dfFechaInicio, Alignment.BOTTOM_CENTER);


        HorizontalLayout subdivicionesValorLayout = new HorizontalLayout();
        subdivicionesValorLayout.setWidth("100%");
        subdivicionesValorLayout.setMargin(false);
        subdivicionesValorLayout.setSpacing(true);
        subdivicionesValorLayout.addComponents(nfReduccion, nfValorResidual);

        VerticalLayout valorLayout = new VerticalLayout();
        valorLayout.setCaption("Valores:");
        valorLayout.addStyleName("rcorners3");
        valorLayout.setSizeFull();
        valorLayout.setMargin(false);
        valorLayout.setSpacing(true);
        valorLayout.addComponents(nfValor, subdivicionesValorLayout);

        VerticalLayout documentoLayout = new VerticalLayout();
        documentoLayout.setCaption("Documento:");
        documentoLayout.addStyleName("rcorners3");
        documentoLayout.setSizeFull();
        documentoLayout.setMargin(false);
        documentoLayout.setSpacing(true);
        documentoLayout.addComponents(tfNumero, tfSerie);

        hLayoutLower.addComponents(documentoLayout, fechaLayout, valorLayout);
        hLayoutLower.setExpandRatio(documentoLayout, 3);
        hLayoutLower.setExpandRatio(fechaLayout, 2);
        hLayoutLower.setExpandRatio(valorLayout, 3);

        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSizeFull();
        buttonLayout.setMargin(false);
        buttonLayout.setSpacing(true);
        buttonLayout.setWidth("99%");
        buttonLayout.setHeight("50px");
        
        buttonLayout.addComponents(btnGuardar, btnCancelar);
        buttonLayout.setComponentAlignment(btnGuardar, Alignment.MIDDLE_CENTER);
        buttonLayout.setComponentAlignment(btnCancelar, Alignment.MIDDLE_CENTER);

        formLayout.addComponents(hLayoutUpper, hLayoutMiddle, hLayoutLower, buttonLayout);
        formLayout.setExpandRatio(hLayoutUpper, 1);
        formLayout.setExpandRatio(hLayoutMiddle, 2);
        formLayout.setExpandRatio(hLayoutLower, 1);
        formLayout.setComponentAlignment(hLayoutUpper, Alignment.TOP_CENTER);
        formLayout.setComponentAlignment(hLayoutMiddle, Alignment.TOP_CENTER);
        formLayout.setComponentAlignment(hLayoutLower, Alignment.TOP_CENTER);
        formLayout.setComponentAlignment(buttonLayout, Alignment.BOTTOM_CENTER);
    }

    private VerticalLayout crearActivosGrid() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setSpacing(true);

        dfFechaInicioBusquedaPartidas.setDateFormat("dd/MM/yyyy");
        Date primerDia = Utileria.getPrimerDiaDelMes();
        dfFechaInicioBusquedaPartidas.setValue(primerDia);
        dfFechaInicioBusquedaPartidas.addValueChangeListener(event -> cargarPartidasCompra());

        dfFechaFinBusquedaPartidas.setDateFormat("dd/MM/yyyy");
        Date ultimoDia = Utileria.getUltimoDiaDelMes();
        dfFechaFinBusquedaPartidas.setValue(ultimoDia);
        dfFechaFinBusquedaPartidas.addValueChangeListener(event -> cargarPartidasCompra());

        FormLayout fechaBusquedaInicioLayout = new FormLayout();
        fechaBusquedaInicioLayout.setMargin(false);
        fechaBusquedaInicioLayout.addComponent(dfFechaInicioBusquedaPartidas);

        FormLayout fechaBusquedaFinLayout = new FormLayout();
        fechaBusquedaFinLayout.setMargin(false);
        fechaBusquedaFinLayout.addComponent(dfFechaFinBusquedaPartidas);

        HorizontalLayout fechaBusquedaLayout = new HorizontalLayout();
        fechaBusquedaLayout.setMargin(false);
        fechaBusquedaLayout.setSpacing(true);
        fechaBusquedaLayout.addComponents(fechaBusquedaInicioLayout, fechaBusquedaFinLayout);
        fechaBusquedaLayout.setComponentAlignment(fechaBusquedaInicioLayout, Alignment.MIDDLE_LEFT);
        fechaBusquedaLayout.setComponentAlignment(fechaBusquedaFinLayout, Alignment.MIDDLE_LEFT);

        partidaContainer.addContainerProperty(CODIGO_PARTIDA, String.class, null);
        partidaContainer.addContainerProperty(IDPROVEEDOR, String.class, null);
        partidaContainer.addContainerProperty(PROVEEDOR, String.class, null);
        partidaContainer.addContainerProperty(SERIE_NUMERO, String.class, "|");
        partidaContainer.addContainerProperty(DESCRIPCION, String.class, null);
        partidaContainer.addContainerProperty(MONTO, BigDecimal.class, 0);
        partidaContainer.addContainerProperty(MONTO_SIN_IVA, BigDecimal.class, 0);
        partidaContainer.addContainerProperty(ACTIVOS_SUM, BigDecimal.class, new BigDecimal("0.00"));
        partidaContainer.addContainerProperty(COUNT_AMORTIZACIONES, Integer.class, 0);
        partidaContainer.addContainerProperty(FECHA, Date.class, new Date());

        partidasGrid.setContainerDataSource(partidaContainer);
        partidasGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        partidasGrid.setWidth("100%");
        partidasGrid.setHeightMode(HeightMode.ROW);
        partidasGrid.setHeightByRows(3);

        partidasGrid.getColumn(CODIGO_PARTIDA).setHidden(true);
        partidasGrid.getColumn(CODIGO_PARTIDA).setHidable(true);
        partidasGrid.getColumn(IDPROVEEDOR).setHidden(true);
        partidasGrid.getColumn(IDPROVEEDOR).setHidable(true);
        partidasGrid.getColumn(MONTO).setHidden(true);
        partidasGrid.getColumn(MONTO).setHidable(true);

        partidasGrid.getColumn(FECHA).setRenderer(new DateRenderer("%1$td/%1$tm/%1$tY"));
        partidasGrid.getColumn(MONTO).setRenderer(new NumberRenderer("Q. %1$.2f"));
        partidasGrid.getColumn(MONTO_SIN_IVA).setRenderer(new NumberRenderer("Q. %1$.2f"));
        partidasGrid.getColumn(ACTIVOS_SUM).setRenderer(new NumberRenderer("Q. %1$.2f"));
        partidasGrid.getColumn(COUNT_AMORTIZACIONES).setRenderer(new NumberRenderer("%1$d"));

        Grid.HeaderRow filterRow = partidasGrid.appendHeaderRow();

        Utileria.addTextFilter(filterRow, PROVEEDOR, partidaContainer, 10);
        Utileria.addTextFilter(filterRow, DESCRIPCION, partidaContainer, 10);
        Utileria.addTextFilter(filterRow, SERIE_NUMERO, partidaContainer, 10);
        Utileria.addTextFilter(filterRow, MONTO, partidaContainer, 10);
        Utileria.addTextFilter(filterRow, FECHA, partidaContainer, 10);

        partidasGrid.addItemClickListener(event -> {
            Object itemId = event.getItemId();
            if (itemId != null) {
                updateCombo(itemId);
                cargarHistorialDepreciacion();

            }
        });

        // Panel para seleccionar tipo de depreciación
        Panel tipoDepreciacionPanel = new Panel();
        tipoDepreciacionPanel.setCaption("Tipo de Depreciación:");
        tipoDepreciacionPanel.addStyleName("rcorners3");
        tipoDepreciacionPanel.setWidth("100%");

        HorizontalLayout tipoDepreciacionLayout = new HorizontalLayout();
        tipoDepreciacionLayout.setWidth("100%");
        tipoDepreciacionLayout.setSpacing(true);
        tipoDepreciacionLayout.setMargin(true);

        lblTipoSeleccionado = new Label("Seleccionar tipo de depreciación");
        lblTipoSeleccionado.setWidth("100%");

        nfMeses.setWidth("100%");
        nfMeses.setRequired(true);
        nfMeses.setEnabled(true);

        Button btnSeleccionarTipo = new Button("Elegir Tipo");
        btnSeleccionarTipo.setIcon(FontAwesome.EDIT);
        btnSeleccionarTipo.addClickListener(event -> abrirVentanaSeleccionarTipo());

        tipoDepreciacionLayout.addComponents(lblTipoSeleccionado, nfMeses, btnSeleccionarTipo);
        tipoDepreciacionLayout.setComponentAlignment(lblTipoSeleccionado, Alignment.MIDDLE_LEFT);
        tipoDepreciacionLayout.setComponentAlignment(nfMeses, Alignment.MIDDLE_RIGHT);
        tipoDepreciacionLayout.setComponentAlignment(btnSeleccionarTipo, Alignment.MIDDLE_RIGHT);
        tipoDepreciacionLayout.setExpandRatio(lblTipoSeleccionado, 1.0f);

        tipoDepreciacionPanel.setContent(tipoDepreciacionLayout);

        layout.addComponents(fechaBusquedaLayout, partidasGrid, tipoDepreciacionPanel);

        return layout;
    }
    
    private VerticalLayout crearHistorial() {

        MarginInfo marginInfo = new MarginInfo(false, true, true, false);

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setSpacing(true);
        layout.setMargin(marginInfo);
        layout.setHeight("91%");

        historialContainer.addContainerProperty(ID, Long.class, null);
        historialContainer.addContainerProperty(FECHA, Date.class, null);
        historialContainer.addContainerProperty(DEPRECIACION, BigDecimal.class, null);
        historialContainer.addContainerProperty(VALOR, BigDecimal.class, 0);
        historialContainer.addContainerProperty(ACUMULADO, BigDecimal.class, 0);

        historialGrid.setContainerDataSource(historialContainer);
        historialGrid.setHeight("100%");
        historialGrid.setWidth("100%");

        historialGrid.getColumn(FECHA).setRenderer(new DateRenderer("%1$td/%1$tm/%1$tY"));
        historialGrid.getColumn(VALOR).setRenderer(new NumberRenderer("Q. %1$.2f"));
        historialGrid.getColumn(ACUMULADO).setRenderer(new NumberRenderer("Q. %1$.2f"));
        historialGrid.getColumn(DEPRECIACION).setRenderer(new NumberRenderer("Q. %1$.2f"));

        historialGrid.getColumn(FECHA).setExpandRatio(1);
        historialGrid.getColumn(DEPRECIACION).setExpandRatio(1);
        historialGrid.getColumn(VALOR).setExpandRatio(2);
        historialGrid.getColumn(ACUMULADO).setExpandRatio(2);

        layout.addComponent(historialGrid);

        return layout;
    }


    public void llenarComboCentroCosto() {
        String queryString = "SELECT idCentroCosto, CodigoCentroCosto FROM centro_costo ";
        queryString += "WHERE IdEmpresa = " + empresaId + " ";
        queryString += "AND inhabilitado = 0 ";

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) {
                cbCentroCosto.addItem(rsRecords.getInt("idCentroCosto"));
                cbCentroCosto.setItemCaption(rsRecords.getInt("idCentroCosto"), rsRecords.getString("CodigoCentroCosto"));
            }

            // Seleccionar el primer elemento si hay
            if (cbCentroCosto.getItemIds().size() > 0) {
                cbCentroCosto.select(cbCentroCosto.getItemIds().iterator().next());
            }

        } catch (Exception ex) {
            System.out.println("Error al llenar combo centros de costo: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void cargarPartidasCompra() {
        // Limpiar el contenedor antes de cargar nuevos datos
        partidaContainer.removeAllItems();

        if(dfFechaInicioBusquedaPartidas.getValue().after(dfFechaFinBusquedaPartidas.getValue())){
            Notification notif = new Notification("Fecha de Inicio no puede mayor a Fecha Fin", Notification.Type.WARNING_MESSAGE);
            notif.setDelayMsec(2500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.WARNING);
            notif.show(Page.getCurrent());
        }

        queryString = "SELECT cp.CodigoPartida, cp.Descripcion, cp.MonedaDocumento, cp.SerieDocumento, ";
        queryString += "cp.NumeroDocumento, cp.MontoDocumento, cp.Fecha, cp.IdProveedor, cp.NombreProveedor ";
        queryString += "FROM contabilidad_partida AS cp ";
        queryString += "INNER JOIN contabilidad_nomenclatura_empresa AS cn ";
        queryString += "ON cn.IdNomenclatura = cp.IdNomenclatura ";
        queryString += "AND cn.IdEmpresa = cp.IdEmpresa ";
        queryString += "WHERE UPPER(cp.TipoDocumento) IN ('FACTURA') ";
        queryString += "AND cp.Fecha BETWEEN '" + Utileria.getFechaYYYYMMDD_1(dfFechaInicioBusquedaPartidas.getValue()) + "' ";
        queryString += "AND '" + Utileria.getFechaYYYYMMDD_1(dfFechaFinBusquedaPartidas.getValue()) + "' ";
        queryString += "AND cp.IdEmpresa = " + empresaId + " ";
        queryString += "AND cn.ID2 = 2 ";
        queryString += "GROUP BY cp.CodigoPartida ";
        queryString += "ORDER BY cp.Fecha, cp.IdNomenclatura DESC ";

        System.out.println("Query busqueda FACTURAS/DOCUMENTO COMPRA/GASTO : " + queryString);

        try {

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {
                do {
                    Object itemId = partidaContainer.addItem();
                    String codigoPartida = rsRecords.getString("CodigoPartida");
                    BigDecimal montoDocumento = rsRecords.getBigDecimal("MontoDocumento");
                    BigDecimal montoSinIVA = montoDocumento.divide(BigDecimal.valueOf(1.12), 2, RoundingMode.HALF_UP);
                    
                    partidaContainer.getContainerProperty(itemId, CODIGO_PARTIDA).setValue(codigoPartida);
                    partidaContainer.getContainerProperty(itemId, IDPROVEEDOR).setValue(rsRecords.getString("IdProveedor"));
                    partidaContainer.getContainerProperty(itemId, PROVEEDOR).setValue(rsRecords.getString("NombreProveedor"));
                    partidaContainer.getContainerProperty(itemId, DESCRIPCION).setValue(rsRecords.getString("Descripcion"));
                    partidaContainer.getContainerProperty(itemId, SERIE_NUMERO).setValue(rsRecords.getString("SerieDocumento") + " | " + rsRecords.getString("NumeroDocumento"));
                    partidaContainer.getContainerProperty(itemId, MONTO).setValue(montoDocumento);
                    partidaContainer.getContainerProperty(itemId, MONTO_SIN_IVA).setValue(montoSinIVA);
                    
                    // Obtener info de activos y depreciación
                    BigDecimal activosInfo = obtenerInfoActivosPartida(codigoPartida);
                    partidaContainer.getContainerProperty(itemId, ACTIVOS_SUM).setValue(activosInfo);
                    
                    partidaContainer.getContainerProperty(itemId, FECHA).setValue(rsRecords.getDate("Fecha"));

                } while (rsRecords.next());
            } else {
                System.out.println("No se encontraron partidas de compra para las fechas:");
                System.out.println("Fecha Inicio: " + Utileria.getFechaYYYYMMDD_1(dfFechaInicioBusquedaPartidas.getValue()));
                System.out.println("Fecha Fin: " + Utileria.getFechaYYYYMMDD_1(dfFechaFinBusquedaPartidas.getValue()));

                Notification notif = new Notification("No se encontraron Documentos entre la fecha indicadas ", Notification.Type.WARNING_MESSAGE);
                notif.setDelayMsec(2500);
                notif.setPosition(Position.MIDDLE_CENTER);
                notif.setIcon(FontAwesome.WARNING);
                notif.show(Page.getCurrent());
            }
        } catch (Exception ex) {
            System.out.println("Error al listar tabla Facturas IngresoDocumentosView:" + ex);
            ex.printStackTrace();
        }

    }

    private void cargarHistorialDepreciacion() {
        if (tipoDepreciacionSeleccionado == null) {
            return; // No hacer nada si no hay tipo seleccionado
        }

        historialContainer.removeAllItems();

        // Usar directamente los meses del tipo seleccionado
        int meses = tipoDepreciacionSeleccionado.meses + 1; // +1 para incluir el mes inicial

        // Generar historial de depreciación
        BigDecimal reduccionAcumulado = new BigDecimal(0);
        for(int i = 1; i < meses; i++){
            Object itemId = historialContainer.addItem();
            historialContainer.getContainerProperty(itemId, ID).setValue((long) i);

            Date mesFecha = Utileria.getInicioMesDate(new Date(dfFechaInicio.getValue().getTime() + (long) i * 30L * 24L * 60L * 60L * 1000L));

            historialContainer.getContainerProperty(itemId, FECHA).setValue(mesFecha);

            BigDecimal depreciacion = new BigDecimal(nfReduccion.getValue());
            if(i == meses - 1) depreciacion = depreciacion.add(new BigDecimal(nfValorResidual.getValue()));
            reduccionAcumulado = reduccionAcumulado.add(depreciacion);
            historialContainer.getContainerProperty(itemId, DEPRECIACION).setValue(depreciacion);

            BigDecimal valor = new BigDecimal(nfValor.getValue()).subtract(reduccionAcumulado);
            historialContainer.getContainerProperty(itemId, VALOR).setValue(valor);

            historialContainer.getContainerProperty(itemId, ACUMULADO).setValue(reduccionAcumulado);
        }
    }

    private void updateCombo(Object itemId) {
        // Obtener y mostrar serie y número del documento seleccionado
        String[] serieNumero = ((String) partidaContainer.getContainerProperty(itemId, SERIE_NUMERO).getValue()).split("\\|");
        tfNumero.setValue(serieNumero[1].trim());
        tfSerie.setValue(serieNumero[0].trim());
        
        // Llenar el valor inicial con el monto de la partida seleccionada
        BigDecimal montoSinIVA = (BigDecimal) partidaContainer.getContainerProperty(itemId, MONTO_SIN_IVA).getValue();
        BigDecimal depreciacionAcumulada = (BigDecimal) partidaContainer.getContainerProperty(itemId, ACTIVOS_SUM).getValue();
        BigDecimal monto = montoSinIVA.subtract(depreciacionAcumulada);
        nfValor.setValue(String.valueOf(monto));
        
        // Si hay un tipo de depreciación seleccionado, llenar el resto de datos
        if (tipoDepreciacionSeleccionado != null) {
            llenarDatosCompletos(itemId);
        }
    }

    private boolean guardarActivo() {
        try {
            if(tipoDepreciacionSeleccionado == null) {
                Notification.show("Por favor seleccione un tipo de depreciación", Notification.Type.WARNING_MESSAGE);
                return false;
            }
            if(partidasGrid.getSelectedRow() == null) {
                Notification.show("Por favor seleccione un documento de la lista", Notification.Type.WARNING_MESSAGE);
                return false;
            }
            Object selectedRow = partidasGrid.getSelectedRow();
            BigDecimal montoSinIVA = (BigDecimal) partidaContainer.getContainerProperty(selectedRow, MONTO_SIN_IVA).getValue();
            BigDecimal depreciacionAcumulada = (BigDecimal) partidaContainer.getContainerProperty(selectedRow, ACTIVOS_SUM).getValue();
            BigDecimal monto = montoSinIVA.subtract(depreciacionAcumulada).subtract(new BigDecimal(nfValor.getValue()));
            if (monto.compareTo(BigDecimal.ZERO) < 0) {
                Notification.show("Este activo supera el monto disponible de la factura seleccionada",
                        Notification.Type.WARNING_MESSAGE);
                return false;
            }

            // Validaciones básicas
            if (tfNombre.isEmpty() || tipoDepreciacionSeleccionado == null || dfFechaInicio.getValue() == null ||
                    nfValor.isEmpty() || nfReduccion.isEmpty() || empresaId == null) {
                Notification.show("Por favor complete todos los campos requeridos", Notification.Type.WARNING_MESSAGE);
                return false;
            }

            // Obtener datos del formulario
            String descripcion = tfNombre.getValue();
            int idEmpresa = Integer.parseInt(empresaId);
            int idTipoDepreciacion = tipoDepreciacionSeleccionado.id;
            String codigoPartida = (String) partidaContainer.getContainerProperty(
                    partidasGrid.getSelectedRow(), CODIGO_PARTIDA).getValue();
            String idproveedor = (String) partidaContainer.getContainerProperty(
                    partidasGrid.getSelectedRow(), IDPROVEEDOR).getValue();
            BigDecimal valor = new BigDecimal(nfValor.getValue());
            BigDecimal valorDepreciacion = new BigDecimal(nfReduccion.getValue());
            BigDecimal valorCorreccion = new BigDecimal(nfValorResidual.getValue());
            Date mesInicio = dfFechaInicio.getValue();
            Date mesFin = dfFechaFin.getValue();

            // Generar código único para el activo
            String codigoActivo = generarCodigoActivo();

            // CentroCosto
            int idCentroCosto = (int) cbCentroCosto.getValue();

            // Obtener datos del usuario actual
            String usuarioId = ((SopdiUI) mainUI).sessionInformation.getStrUserId();

            // Preparar sentencia INSERT para tabla activos
            StringBuilder insertActivos = new StringBuilder();
            insertActivos.append("INSERT INTO activos (")
                    .append("IdEmpresa, IdProveedor, CodigoPartida, Descripcion, MesInicio, MesFin, ")
                    .append("IdTipoDepreciacion, Valor, ValorDepreciacion, ValorCorreccion, ")
                    .append("CodigoActivo, IdCentroCosto, IdProveedorResponsable, ")
                    .append("CreadoUsuario, CreadoFechaYHora) VALUES (")
                    .append(idEmpresa).append(", ")
                    .append(idproveedor).append(", '")
                    .append(codigoPartida).append("', '")
                    .append(descripcion.replace("'", "''")).append("', '")
                    .append(Utileria.getFechaYYYYMMDD_1(mesInicio)).append("', '")
                    .append(Utileria.getFechaYYYYMMDD_1(mesFin)).append("', ")
                    .append(idTipoDepreciacion).append(", ")
                    .append(valor).append(", ")
                    .append(valorDepreciacion).append(", ")
                    .append(valorCorreccion).append(", '")
                    .append(codigoActivo).append("',")
                    .append(idCentroCosto).append(", 0, ")
                    .append(usuarioId).append(", NOW())");

            System.out.println("Query INSERT activos: " + insertActivos.toString());

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(insertActivos.toString());

            // Obtener el ID del activo insertado
            String selectLastId = "SELECT LAST_INSERT_ID() as IdActivo";
            ResultSet rsLastId = stQuery.executeQuery(selectLastId);
            long idActivo = -1;
            if (rsLastId.next()) {
                idActivo = rsLastId.getLong("IdActivo");
            }

            // Obtener los meses del tipo de depreciación para insertar en activos_depreciacion
            int mesesDepreciacion = tipoDepreciacionSeleccionado.meses;

            int cantidadDepreciaciones = obtenerCantidadDepreciaciones(codigoPartida);

            // Insertar registros en activos_depreciacion (uno por cada mes de depreciación)
            for (int i = 0; i < mesesDepreciacion; i++) {
                // Calcular la depreciación para este mes
                BigDecimal depreciacionMes = valorDepreciacion;
                if (i == 0) {
                    depreciacionMes = depreciacionMes.add(valorCorreccion);
                }

                String numeroDocumetoPartida = ((String) partidaContainer.getContainerProperty(
                        partidasGrid.getSelectedRow(), SERIE_NUMERO).getValue()
                ).split("\\|")[1].trim();

                // Calcular la fecha para este mes
                GregorianCalendar fechaMes = new GregorianCalendar();
                fechaMes.setTime(mesInicio);
                fechaMes.add(GregorianCalendar.MONTH, i);
                int mes = fechaMes.get(GregorianCalendar.MONTH) + 1; // +1 porque los meses en GregorianCalendar son 0-11
                int año = fechaMes.get(GregorianCalendar.YEAR);

                StringBuilder insertDepreciacion = new StringBuilder();
                insertDepreciacion.append("INSERT INTO activos_depreciacion (")
                        .append("IdEmpresa, CodigoActivo, CodigoDepreciacion, Valor, Mes, Año, FechaPartida, FechaCreado) VALUES (")
                        .append(idEmpresa).append(", '")
                        .append(codigoActivo).append("', '")
                        .append(numeroDocumetoPartida).append(String.format("%03d", i + cantidadDepreciaciones)).append("', ")
                        .append(depreciacionMes).append(", ")
                        .append(String.format("%02d", mes)).append(", ")
                        .append(año).append(", ")
                        .append("NULL, NOW())");

                System.out.println("Query INSERT activos_depreciacion: " + insertDepreciacion.toString());

                stQuery1 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                stQuery1.executeUpdate(insertDepreciacion.toString());
            }

            // Mostrar notificación de éxito
            Notification notif = new Notification("Activo registrado exitosamente con ID: " + idActivo,
                    Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(3000);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.show(Page.getCurrent());

            return true;

        } catch (Exception ex) {
            System.out.println("Error al guardar activo: " + ex.getMessage());
            ex.printStackTrace();
            Notification.show("Error al guardar el activo: " + ex.getMessage(),
                    Notification.Type.ERROR_MESSAGE);
            return false;
        }
    }

    private String generarCodigoActivo() {
        int cuenta = tipoDepreciacionSeleccionado.id;
        String idEmpresa = empresaId;
        String timestamp = (new Utileria()).getFechaSinFormato(dfFechaInicio.getValue());
        int count = 0;
        String fecha = Utileria.getFechaYYYYMMDD_1(dfFechaInicio.getValue());
        try {
            queryString = "SELECT COUNT(*) FROM activos " +
                    "WHERE idEmpresa = " + idEmpresa + " " +
                    "AND idTipoDepreciacion = " + cuenta + " " +
                    "AND MesInicio = '" + fecha + "'";
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);
            if (rsRecords.next()) {
                count = rsRecords.getInt(1);
            }
        }catch (Exception ex) {
            System.out.println("Error al contar activos para generar código: " + ex.getMessage());
            ex.printStackTrace();
        }

        String autonumerico = String.format("%04d", count + 1); // Autonumerico de 6 dígitos (4 + 2 dígitos extra)
        //      3     +     8     +     8     +       4   =     23 caracteres
        return idEmpresa + cuenta + timestamp + autonumerico;
    }
    

    /**
     * Abre una ventana modal para seleccionar el tipo de depreciación
     */
    private void abrirVentanaSeleccionarTipo() {
        Window ventanaSeleccion = new Window("Seleccionar Tipo de Depreciación");
        ventanaSeleccion.center();
        ventanaSeleccion.setModal(true);
        ventanaSeleccion.setWidth("900px");
        ventanaSeleccion.setHeight("600px");

        VerticalLayout layoutVentana = new VerticalLayout();
        layoutVentana.setMargin(true);
        layoutVentana.setSpacing(true);
        layoutVentana.setSizeFull();

        // Grid de tipos de depreciación
        Grid gridTipos = new Grid();
        gridTipos.setWidth("100%");
        gridTipos.setHeightMode(HeightMode.ROW);
        gridTipos.setHeightByRows(10);

        IndexedContainer containerTipos = new IndexedContainer();
        containerTipos.addContainerProperty("Id", Integer.class, null);
        containerTipos.addContainerProperty("Activo", String.class, null);
        containerTipos.addContainerProperty("Debe", String.class, null);
        containerTipos.addContainerProperty("Haber", String.class, null);
        containerTipos.addContainerProperty("Meses", Integer.class, null);
        containerTipos.addContainerProperty("TipoDepreciacion", DepreciacionesView.TipoDepreciacion.class, null);

        gridTipos.setContainerDataSource(containerTipos);

        // Cargar tipos de depreciación desde BD
        cargarTiposDepreciacionEnGrid(containerTipos);

        layoutVentana.addComponent(gridTipos);

        // Botones de acción
        HorizontalLayout botonesLayout = new HorizontalLayout();
        botonesLayout.setSpacing(true);
        botonesLayout.setMargin(false);

        Button btnSeleccionar = new Button("Confirmar Selección");
        btnSeleccionar.setStyleName(ValoTheme.BUTTON_PRIMARY);
        btnSeleccionar.addClickListener(event -> {
            if (gridTipos.getSelectedRow() != null) {
                tipoDepreciacionSeleccionado = (DepreciacionesView.TipoDepreciacion) containerTipos.getContainerProperty(
                        gridTipos.getSelectedRow(), "TipoDepreciacion").getValue();
                actualizarEtiquetaTipo();
                ventanaSeleccion.close();
            } else {
                Notification.show("Debe seleccionar un tipo de depreciación", Notification.Type.WARNING_MESSAGE);
            }
        });

        Button btnCrearNuevo = new Button("Crear Nuevo Tipo");
        btnCrearNuevo.setIcon(FontAwesome.PLUS);
        btnCrearNuevo.addClickListener(event -> {
            DepreciacionesEquivalenciasForm formEquivalencias = new DepreciacionesEquivalenciasForm();
            mainUI.addWindow(formEquivalencias);
        });

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.addClickListener(event -> ventanaSeleccion.close());

        botonesLayout.addComponents(btnSeleccionar, btnCrearNuevo, btnCancelar);
        layoutVentana.addComponent(botonesLayout);
        layoutVentana.setComponentAlignment(botonesLayout, Alignment.BOTTOM_RIGHT);
        layoutVentana.setExpandRatio(gridTipos, 1.0f);

        ventanaSeleccion.setContent(layoutVentana);
        mainUI.addWindow(ventanaSeleccion);
    }

    /**
     * Carga los tipos de depreciación disponibles en el grid
     */
    private void cargarTiposDepreciacionEnGrid(IndexedContainer container) {
        String query = "SELECT td.Id, td.IdNomenclaturaActivo, td.IdNomenclaturaDebe, td.IdNomenclaturaHaber, td.Meses, " +
                "na.NoCuenta AS numeroCuentaActivo, " +
                "nd.NoCuenta AS numeroCuentaDebe, " +
                "nh.NoCuenta AS numeroCuentaHaber, " +
                "na.N5 AS nombreCuentaActivo, " +
                "nd.N5 AS nombreCuentaDebe, " +
                "nh.N5 AS nombreCuentaHaber " +
                "FROM tipo_depreciacion td " +
                "LEFT JOIN contabilidad_nomenclatura_empresa na ON td.IdNomenclaturaActivo = na.IdNomenclatura " +
                "AND na.idempresa = " + empresaId + " " +
                "LEFT JOIN contabilidad_nomenclatura_empresa nd ON td.IdNomenclaturaDebe = nd.IdNomenclatura " +
                "AND nd.idempresa = " + empresaId + " " +
                "LEFT JOIN contabilidad_nomenclatura_empresa nh ON td.IdNomenclaturaHaber = nh.IdNomenclatura " +
                "AND nh.idempresa = " + empresaId + " " +
                "WHERE td.idempresa = " + empresaId + " " +
                "ORDER BY td.Id ASC";

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(query);

            while (rsRecords.next()) {
                int id = rsRecords.getInt("Id");
                String numeroCuentaActivoId = rsRecords.getString("numeroCuentaActivo");
                String numeroCuentaDebeId = rsRecords.getString("numeroCuentaDebe");
                String numeroCuentaHaberId = rsRecords.getString("numeroCuentaHaber");
                String nombreCuentaActivoId = rsRecords.getString("nombreCuentaActivo");
                String nombreCuentaDebeId = rsRecords.getString("nombreCuentaDebe");
                String nombreCuentaHaberId = rsRecords.getString("nombreCuentaHaber");
                int meses = rsRecords.getInt("Meses");
                int idNomenclaturaActivo = rsRecords.getInt("IdNomenclaturaActivo");
                int idNomenclaturaDebe = rsRecords.getInt("IdNomenclaturaDebe");
                int idNomenclaturaHaber = rsRecords.getInt("IdNomenclaturaHaber");

                // Crear el objeto TipoDepreciacion
                DepreciacionesView.TipoDepreciacion tipoDepreciacion = new DepreciacionesView.TipoDepreciacion(
                        id,
                        meses,
                        idNomenclaturaActivo,
                        idNomenclaturaDebe,
                        idNomenclaturaHaber,
                        String.valueOf(numeroCuentaActivoId),
                        String.valueOf(numeroCuentaDebeId),
                        String.valueOf(numeroCuentaHaberId),
                        String.valueOf(nombreCuentaActivoId),
                        String.valueOf(nombreCuentaDebeId),
                        String.valueOf(nombreCuentaHaberId)
                );

                // Agregar al contenedor del grid
                Object itemId = container.addItem();
                container.getContainerProperty(itemId, "Id").setValue(id);
                container.getContainerProperty(itemId, "Activo").setValue(numeroCuentaActivoId + " " + nombreCuentaActivoId);
                container.getContainerProperty(itemId, "Debe").setValue(numeroCuentaDebeId + " " + nombreCuentaDebeId);
                container.getContainerProperty(itemId, "Haber").setValue(numeroCuentaHaberId +  " " + nombreCuentaHaberId);
                container.getContainerProperty(itemId, "Meses").setValue(meses);
                container.getContainerProperty(itemId, "TipoDepreciacion").setValue(tipoDepreciacion);
            }

        } catch (Exception ex) {
            Notification.show("Error al cargar tipos de depreciación: " + ex.getMessage(),
                    Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    /**
     * Actualiza la etiqueta con el tipo de depreciación seleccionado y completa el formulario
     * si hay una partida seleccionada
     */
    private void actualizarEtiquetaTipo() {
        if (tipoDepreciacionSeleccionado != null) {
            lblTipoSeleccionado.setContentMode(ContentMode.HTML);

            // Actualizar la etiqueta con la información del objeto TipoDepreciacion
            String textoMuestra =
                    "<b>Activo:</b> "   + tipoDepreciacionSeleccionado.numeroCuentaActivo + " "
                                        + tipoDepreciacionSeleccionado.nombreCuantaActivo + " | " +
                    "<b>Debe:</b> "     + tipoDepreciacionSeleccionado.numeroCuentaDebe + " "
                                        + tipoDepreciacionSeleccionado.nombreCuantaDebe + " | " +
                    "<b>Haber:</b> "    + tipoDepreciacionSeleccionado.numeroCuentaHaber + " "
                                        + tipoDepreciacionSeleccionado.nombreCuantaHaber + " | " +
                    "<b>Meses:</b> "  + tipoDepreciacionSeleccionado.meses;
            lblTipoSeleccionado.setValue(textoMuestra);
            nfMeses.setValue((double) tipoDepreciacionSeleccionado.meses);
            
            // Verificar si hay una partida seleccionada y llenar el resto de datos
            Object selectedPartida = partidasGrid.getSelectedRow();
            if (selectedPartida != null) {
                llenarDatosCompletos(selectedPartida);
            }
        }
    }
    
    /**
     * Obtiene información de activos y depreciación total para una partida
     */
    private BigDecimal obtenerInfoActivosPartida(String codigoPartida) {
        try {
            String query = "SELECT IFNULL(SUM(a.Valor), 0) as depreciacionTotal " +
                    "FROM activos a " +
                    "WHERE a.CodigoPartida = '" + codigoPartida + "' " +
                    "AND a.IdEmpresa = " + empresaId;
            
            Statement stmt = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            if (rs.next()) {
                BigDecimal depreciacitotal = rs.getBigDecimal("depreciacionTotal");
                return depreciacitotal;
            }
            
            rs.close();
            stmt.close();
            
        } catch (Exception ex) {
            System.out.println("Error al obtener info de activos para partida: " + ex.getMessage());
            ex.printStackTrace();
        }
        
        return BigDecimal.ZERO;
    }

    /**
     * Obtiene cuantas depreciaciones/amortizacones estan asociadas a una factura/partida
     */
    private int obtenerCantidadDepreciaciones(String codigoPartida) {
        try {
            String query = "SELECT COUNT(*) as cantidad " +
                    "FROM activos a " +
                    "INNER JOIN activos_depreciacion ad ON a.CodigoActivo = ad.CodigoActivo " +
                    "WHERE a.CodigoPartida = '" + codigoPartida + "' " +
                    "AND a.IdEmpresa = " + empresaId;

            Statement stmt = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);

            if (rs.next()) {
                return rs.getInt("cantidad") + 1;
            }

            rs.close();
            stmt.close();

        } catch (Exception ex) {
            System.out.println("Error al obtener cantidad de depreciaciones para partida: " + ex.getMessage());
            ex.printStackTrace();
        }

        return 1;
    }
    /**
     * Llena todos los datos del formulario cuando documento y tipo de depreciación están seleccionados
     */
    private void llenarDatosCompletos(Object itemId) {
        if (tipoDepreciacionSeleccionado == null) {
            Notification.show("Debe seleccionar un tipo de depreciación primero", Notification.Type.WARNING_MESSAGE);
            return;
        }

        Date fechaInicio = Utileria.getInicioMesDate((Date) partidaContainer.getContainerProperty(itemId, FECHA).getValue());
        dfFechaInicio.setValue(fechaInicio);

        // Usar directamente los meses del tipo de depreciación seleccionado
        int meses = tipoDepreciacionSeleccionado.meses;

        long mesMillis = (long) meses * 30L * 24L * 60L * 60L * 1000L;
        Date fechaFin = Utileria.getUltimoFechaDelMes(new Date(dfFechaInicio.getValue().getTime() + mesMillis));
        dfFechaFin.setValue(fechaFin);

        BigDecimal montoSinIVA = (BigDecimal) partidaContainer.getContainerProperty(itemId, MONTO_SIN_IVA).getValue();
        BigDecimal depreciacionAcumulada = (BigDecimal) partidaContainer.getContainerProperty(itemId, ACTIVOS_SUM).getValue();
        BigDecimal monto = montoSinIVA.subtract(depreciacionAcumulada);
        nfValor.setValue(String.valueOf(monto));
        BigDecimal reduccion = monto.divide(BigDecimal.valueOf(meses), 2, RoundingMode.HALF_UP);
        BigDecimal sobrante = monto.subtract(reduccion.multiply(BigDecimal.valueOf(meses)));
        nfReduccion.setValue(String.valueOf(reduccion));
        nfValorResidual.setValue(String.valueOf(sobrante));

        cargarHistorialDepreciacion();
    }

    private boolean tieneDepreciacion(String codigoPartida){
        queryString = "SELECT COUNT(*) as hay " +
                "FROM contabilidad_partida cp " +
                "INNER JOIN activos a ON cp.CodigoPartida = a.CodigoPartida " +
                "WHERE cp.CodigoPartida = '" + codigoPartida + "'";
        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) {
                return (rsRecords.getInt("hay") > 0);
            }
            return false;
        } catch (Exception ex) {
            Notification.show("Error al buscar si existe activo asociado: " + ex.getMessage(),
                    Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
            return false;
        }
    }
}

