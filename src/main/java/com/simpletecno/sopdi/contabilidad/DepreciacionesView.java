package com.simpletecno.sopdi.contabilidad;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.renderers.DateRenderer;
import com.vaadin.ui.renderers.NumberRenderer;
import com.vaadin.ui.themes.ValoTheme;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Vista principal de DepreciacionesView (Vaadin 7).
 * Estructura:
 * - Toolbar con 3 botones: Crear Activo, Administrar Equivalencias, Generar Partidas Mes
 * - Grid de activos depreciables
 * - Sección inferior en 2 paneles: Detalle (izq) + Historial (der)
 */
public class DepreciacionesView extends VerticalLayout implements View {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private final VerticalLayout mainLayout = new VerticalLayout();

    // Componentes principales
    private final IndexedContainer activosContainer = new IndexedContainer();
    private final Grid activosGrid = new Grid("Activos en Depreciación");

    private final VerticalLayout detallePanel = new VerticalLayout();
    private final VerticalLayout historialPanel = new VerticalLayout();

    private Integer activoSeleccionadoId = null;

    public DepreciacionesView() {
        setMargin(true);
        setSpacing(true);
        setSizeFull();

        mainLayout.setSizeFull();

        crearToolbar();
        crearGridActivos();
        crearSeccionDetalle();

        addComponent(mainLayout);
        setExpandRatio(mainLayout, 1f);
    }

    /**
     * Barra de acciones con 3 botones
     */
    private void crearToolbar() {
        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setSpacing(true);

        Button btnCrearActivo = new Button("Crear Activo", FontAwesome.PLUS);
        btnCrearActivo.addClickListener(evt -> abrirFormCrearActivo());

        Button btnEquivalencias = new Button("Administrar equivalencias", FontAwesome.EXCHANGE);
        btnEquivalencias.addClickListener(evt -> abrirFormEquivalencias());

        Button btnPartidasMes = new Button("Generar Partidas Mes", FontAwesome.CALENDAR);
        btnPartidasMes.addClickListener(evt -> abrirFormPartidasMes());

        Button btnActualizar = new Button("Actualizar");
        btnActualizar.addClickListener(evt -> cargarActivosDespreciables());

        toolbar.addComponents(btnCrearActivo, btnEquivalencias, btnPartidasMes, btnActualizar);
        mainLayout.setWidth("100%");
        mainLayout.addComponent(toolbar);

    }

    /**
     * Grid principal de activos depreciables
     */
    private void crearGridActivos() {
        HorizontalLayout activosHLayout = new HorizontalLayout();
        activosHLayout.setMargin(true);
        activosHLayout.setMargin(new MarginInfo(true, true, false, true));
        activosHLayout.setWidth("100%");
        //activosHLayout.setSizeFull();

        activosContainer.addContainerProperty("Id", Long.class, null);
        activosContainer.addContainerProperty("CodigoActivo", String.class, null);
        activosContainer.addContainerProperty("Descripcion", String.class, null);
        activosContainer.addContainerProperty("MesInicio", Date.class, null);
        activosContainer.addContainerProperty("MesFin", Date.class, null);
        activosContainer.addContainerProperty("Valor", BigDecimal.class, null);
        activosContainer.addContainerProperty("ValorDepreciacion", BigDecimal.class, null);
        activosContainer.addContainerProperty("Responsable", String.class, "Responsable");
        activosContainer.addContainerProperty("Nomenclatura", String.class, null);

        activosGrid.setContainerDataSource(activosContainer);
        activosGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        activosGrid.setHeightMode(HeightMode.ROW);
        activosGrid.setHeightByRows(6);
        activosGrid.setWidth("100%");

        activosGrid.getColumn("MesInicio").setRenderer(new DateRenderer("%1$td/%1$tm/%1$tY"));
        activosGrid.getColumn("MesFin").setRenderer(new DateRenderer("%1$td/%1$tm/%1$tY"));
        activosGrid.getColumn("Valor").setRenderer(new NumberRenderer("Q. %1$.2f"));
        activosGrid.getColumn("ValorDepreciacion").setRenderer(new NumberRenderer("Q. %1$.2f"));

        activosGrid.getColumn("Responsable").setRenderer(new ButtonRenderer(e -> {
            Object itemId = e.getItemId();
            if (itemId != null) {
                activoSeleccionadoId = (Integer) itemId;

                // Si se hace clic en la columna Responsable, abrir ventana de selección
                if ("Responsable".equals(e.getPropertyId())) {
                    Long idActivo = (Long) activosContainer.getContainerProperty(itemId, "Id").getValue();
                    String codigoActivo = activosContainer.getContainerProperty(itemId, "CodigoActivo").getValue().toString();
                    String descripcion = activosContainer.getContainerProperty(itemId, "Descripcion").getValue().toString();
                    abrirGestionResponsables(Math.toIntExact(idActivo), codigoActivo, descripcion);
                }
            }
        }));

        activosGrid.addItemClickListener(evt -> {
            if (evt.getItemId() != null) {
                activoSeleccionadoId = (Integer) evt.getItemId();
                actualizarDetalleYHistorial(activoSeleccionadoId);
            }
        });

        // Cargar activos depreciables desde BD
        cargarActivosDespreciables();

        activosHLayout.addComponents(activosGrid);
        activosHLayout.setComponentAlignment(activosGrid, Alignment.MIDDLE_CENTER);
        activosHLayout.setHeightUndefined();

        mainLayout.addComponent(activosHLayout);
    }

    /**
     * Sección inferior con 2 paneles: Detalle + Historial
     */
    private void crearSeccionDetalle() {
        HorizontalLayout seccionDetalle = new HorizontalLayout();
        seccionDetalle.setSpacing(true);
        seccionDetalle.setMargin(true);
        seccionDetalle.setSizeFull();

        // Panel Detalle
        Panel panelDetalle = new Panel("Detalle del Activo");
        panelDetalle.setStyleName(ValoTheme.PANEL_BORDERLESS);
        panelDetalle.setSizeFull();

        detallePanel.setMargin(true);
        detallePanel.setSpacing(true);
        // IMPORTANTE: no usar setSizeFull si quieres que el Panel haga scroll por contenido
        detallePanel.setWidth("100%");
        panelDetalle.setContent(detallePanel);

        // Panel Historial
        Panel panelHistorial = new Panel("Historial de Depreciación");
        panelHistorial.setStyleName(ValoTheme.PANEL_BORDERLESS);
        panelHistorial.setSizeFull();

        historialPanel.setMargin(true);
        historialPanel.setSpacing(true);
        historialPanel.setWidth("100%");
        panelHistorial.setContent(historialPanel);

        seccionDetalle.addComponents(panelDetalle, panelHistorial);
        seccionDetalle.setExpandRatio(panelDetalle, 1f);
        seccionDetalle.setExpandRatio(panelHistorial, 1.5f);

        mainLayout.addComponent(seccionDetalle);
        mainLayout.setExpandRatio(seccionDetalle, 1f);
    }

    /**
     * Actualiza el panel de detalle y el historial cuando se selecciona un activo
     */
    private void actualizarDetalleYHistorial(Integer activoId) {
        detallePanel.removeAllComponents();
        historialPanel.removeAllComponents();

        Item item = activosContainer.getItem(activoId);
        if (item == null) return;

        // Información del detalle
        String responsable = (String) item.getItemProperty("Responsable").getValue();
        BigDecimal valorInicial = (BigDecimal) item.getItemProperty("Valor").getValue();
        Date mesInicio = (Date) item.getItemProperty("MesInicio").getValue();
        Date mesFin = (Date) item.getItemProperty("MesFin").getValue();
        BigDecimal valorDepreciacion = (BigDecimal) item.getItemProperty("ValorDepreciacion").getValue();
        String codigoActivo = (String) item.getItemProperty("CodigoActivo").getValue();
        String nomenclatura = (String) item.getItemProperty("Nomenclatura").getValue();

        // Crear layout de detalle
        FormLayout flDetalle = new FormLayout();
        flDetalle.setMargin(false);
        flDetalle.setSpacing(false);

        Label responsableLbl = new Label("<b>Responsable:</b> " + (responsable != null ? responsable : "N/A"));
        responsableLbl.setContentMode(ContentMode.HTML);
        flDetalle.addComponent(responsableLbl);

        Label ValorInicialLbl = new Label("<b>Valor Inicial:</b> Q. " + String.format("%.2f", valorInicial != null ? valorInicial : BigDecimal.ZERO));
        ValorInicialLbl.setContentMode(ContentMode.HTML);
        flDetalle.addComponent(ValorInicialLbl);

        Label mesInicioLbl = new Label("<b>Mes Inicio:</b> " + (mesInicio != null ? dateFormat.format(mesInicio) : "N/A"));
        mesInicioLbl.setContentMode(ContentMode.HTML);
        flDetalle.addComponent(mesInicioLbl);

        Label mesFinLbl = new Label("<b>Mes Fin:</b> " + (mesFin != null ? dateFormat.format(mesFin) : "N/A"));
        mesFinLbl.setContentMode(ContentMode.HTML);
        flDetalle.addComponent(mesFinLbl);

        Label valorDepreciacionLbl = new Label("<b>Depreciación Mensual:</b> Q. " + String.format("%.2f", valorDepreciacion != null ? valorDepreciacion : BigDecimal.ZERO));
        valorDepreciacionLbl.setContentMode(ContentMode.HTML);
        flDetalle.addComponent(valorDepreciacionLbl);

        Label codigoActivoLbl = new Label("<b>Código Activo:</b> " + (codigoActivo != null ? codigoActivo : "N/A"));
        codigoActivoLbl.setContentMode(ContentMode.HTML);
        flDetalle.addComponent(codigoActivoLbl);

        Label nomenclaturaLbl = new Label("<b>Nomenclatura:</b> " + (nomenclatura != null ? nomenclatura : "N/A"));
        nomenclaturaLbl.setContentMode(ContentMode.HTML);
        flDetalle.addComponent(nomenclaturaLbl);


        detallePanel.addComponent(flDetalle);

        // Grid de historial
        crearHistorialGrid(activoId);
    }

    /**
     * Crea el grid de historial mensual de depreciación
     */
    private void crearHistorialGrid(Integer activoId) {
        IndexedContainer historialContainer = new IndexedContainer();
        historialContainer.addContainerProperty("Id", Long.class, null);
        historialContainer.addContainerProperty("Mes/Año", String.class, null);
        historialContainer.addContainerProperty("FechaPartida", Date.class, null);
        historialContainer.addContainerProperty("Valor", BigDecimal.class, null);
        historialContainer.addContainerProperty("CodigoPartida", String.class, null);

        Grid historialGrid = new Grid(historialContainer);
        historialGrid.setSelectionMode(Grid.SelectionMode.NONE);
        historialGrid.setWidth("100%");
        historialGrid.setHeightMode(HeightMode.UNDEFINED);

        // Configurar renderers
        historialGrid.getColumn("FechaPartida").setRenderer(new DateRenderer("%1$td/%1$tm/%1$tY"));
        historialGrid.getColumn("Valor").setRenderer(new NumberRenderer("Q. %1$.2f"));

        // Ocultar columna Id
        historialGrid.getColumn("Id").setHidden(true);

        // Cargar datos del historial desde BD según el activo seleccionado
        cargarHistorialDesdeBaseDatos(activoId, historialContainer);

        historialPanel.removeAllComponents();
        historialPanel.addComponent(historialGrid);
    }

    /**
     * Carga el historial de depreciación desde la tabla activos_depreciacion
     */
    private void cargarHistorialDesdeBaseDatos(Integer activoId, IndexedContainer container) {
        container.removeAllItems();

        // Obtener el código del activo del contenedor
        Item item = activosContainer.getItem(activoId);
        if (item == null) return;

        String codigoActivo = (String) item.getItemProperty("CodigoActivo").getValue();
        if (codigoActivo == null || codigoActivo.isEmpty()) return;

        String query = "SELECT ad.Id, ad.mes, ad.año, ad.FechaPartida, ad.Valor, ad.CodigoPartida " +
                       "FROM activos_depreciacion ad " +
                       "WHERE ad.CodigoActivo = '" + codigoActivo + "' " +
                       "AND ad.IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId() + " " +
                       "ORDER BY ad.FechaCreado DESC";

        try {
            Statement stmt = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                Object itemId = container.addItem();
                String mesaño = String.format("%02d / %d", rs.getInt("mes"), rs.getInt("año"));
                container.getContainerProperty(itemId, "Id").setValue(rs.getLong("Id"));
                container.getContainerProperty(itemId, "Mes/Año").setValue(mesaño);
                container.getContainerProperty(itemId, "FechaPartida").setValue(rs.getDate("FechaPartida"));
                container.getContainerProperty(itemId, "Valor").setValue(rs.getBigDecimal("Valor"));
                container.getContainerProperty(itemId, "CodigoPartida").setValue(rs.getString("CodigoPartida"));
            }

            rs.close();
            stmt.close();

        } catch (Exception ex) {
            Notification.show("Error al cargar historial de depreciación: " + ex.getMessage(), 
                Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    /**
     * Carga los activos depreciables (MesFin >= mes actual)
     */
    private void cargarActivosDespreciables() {
        activosContainer.removeAllItems();

        String hoy = Utileria.getFechaYYYYMMDD_1(new Date());

        String query = "SELECT a.Id, a.CodigoActivo, a.Descripcion, a.MesInicio, a.MesFin, a.Valor, " +
                "a.ValorDepreciacion, a.ValorCorreccion, IFNULL(p.Nombre, 'Responsable') AS Responsable, cn.N5 AS Nomenclatura " +
                "FROM activos a " +
                "LEFT JOIN proveedor_empresa p ON a.IdProveedorResponsable = p.IdProveedor " +
                "AND p.IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId() + " " +
                "LEFT JOIN tipo_depreciacion td ON a.IdTipoDepreciacion = td.Id " +
                "AND td.IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId() + " " +
                "LEFT JOIN contabilidad_nomenclatura_empresa cn ON td.IdNomenclaturaActivo = cn.IdNomenclatura " +
                "AND cn.IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId() + " " +
                "WHERE a.MesFin >= DATE_FORMAT(CURDATE(), '%Y-%m-01') " +
                "AND a.IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId() + " " +
                "ORDER BY a.MesInicio DESC";

        try {
            Statement stmt = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                Object itemId = activosContainer.addItem();
                activosContainer.getContainerProperty(itemId, "Id").setValue(rs.getLong("Id"));
                activosContainer.getContainerProperty(itemId, "CodigoActivo").setValue(rs.getString("CodigoActivo"));
                activosContainer.getContainerProperty(itemId, "Descripcion").setValue(rs.getString("Descripcion"));
                activosContainer.getContainerProperty(itemId, "MesInicio").setValue(rs.getDate("MesInicio"));
                activosContainer.getContainerProperty(itemId, "MesFin").setValue(rs.getDate("MesFin"));
                activosContainer.getContainerProperty(itemId, "Valor").setValue(rs.getBigDecimal("Valor"));
                activosContainer.getContainerProperty(itemId, "ValorDepreciacion").setValue(rs.getBigDecimal("ValorDepreciacion"));
                activosContainer.getContainerProperty(itemId, "Responsable").setValue(rs.getString("Responsable"));
                activosContainer.getContainerProperty(itemId, "Nomenclatura").setValue(rs.getString("Nomenclatura"));
            }

            rs.close();
            stmt.close();

        } catch (Exception ex) {
            Notification.show("Error al cargar activos: " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    /**
     * Abre la ventana de formulario para crear activos
     */
    private void abrirFormCrearActivo() {
        try {
            DepreciacionForm depreciacionForm = new DepreciacionForm(null);
            UI.getCurrent().addWindow(depreciacionForm);

        } catch (Throwable ex) {
            ex.printStackTrace();

            String msg = ex.getClass().getName() + ": " + ex.getMessage();

            Throwable cause = ex.getCause();
            if (cause != null) {
                msg += "\nCaused by: " + cause.getClass().getName() + ": " + cause.getMessage();
                cause.printStackTrace();
            }

            Notification.show(
                    "Error al abrir formulario",
                    msg,
                    Notification.Type.ERROR_MESSAGE
            );
        }
    }

    /**
     * Abre la ventana de equivalencias
     */
    private void abrirFormEquivalencias() {
        Window form = new DepreciacionesEquivalenciasForm();
        UI.getCurrent().addWindow(form);
    }

    /**
     * Abre la ventana de partidas por mes
     */
    private void abrirFormPartidasMes() {
        Window form = new DepreciacionesGenerarPartidasMesForm();
        UI.getCurrent().addWindow(form);
    }

    /**
     * Abre una ventana modal para seleccionar el responsable de un activo
     */

    private void abrirGestionResponsables(Integer activoId, String codigoActivo, String descripcion) {
        if (activoId <= 0 || codigoActivo == null) {
            Notification.show("Debe guardar el activo primero", Notification.Type.WARNING_MESSAGE);
            return;
        }

        int idEmpresa = Integer.parseInt(((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId());
        ActivosResponsableForm formResponsables = new ActivosResponsableForm(
                activoId,
                codigoActivo,
                descripcion,
                idEmpresa
        );
        UI.getCurrent().addWindow(formResponsables);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        // Recargar activos cuando se entra a la vista
        cargarActivosDespreciables();
    }

    /**
     * Clase auxiliar para tipos de depreciación
     */
    public static class TipoDepreciacion {
        /** Nombre del tipo de depreciación */
        public final int id;

        /** Cantidad de meses de depreciación */
        public final int meses;

        /** ID de la nomenclatura contable del activo en la partida de Compra */
        public final int idNomenclaturaActivo;
        public final String numeroCuentaActivo;
        public final String nombreCuantaActivo;

        /** ID de la nomenclatura contable del debe (gasto de depreciación) */
        public final int idNomenclaturaDebe;
        public final String numeroCuentaDebe;
        public final String nombreCuantaDebe;

        /** ID de la nomenclatura contable del haber (depreciación acumulada) */
        public final int idNomenclaturaHaber;
        public final String numeroCuentaHaber;
        public final String nombreCuantaHaber;

        public TipoDepreciacion(int id, int meses, int idNomenclaturaActivo, int idNomenclaturaDebe, int idNomenclaturaHaber,
                                String numeroCuentaActivo, String numeroCuentaDebe, String numeroCuentaHaber,
                                String nombreCuantaActivo, String nombreCuantaDebe, String nombreCuantaHaber) {
            this.id = id;
            this.meses = meses;
            this.idNomenclaturaActivo = idNomenclaturaActivo;
            this.idNomenclaturaDebe = idNomenclaturaDebe;
            this.idNomenclaturaHaber = idNomenclaturaHaber;
            this.numeroCuentaActivo = numeroCuentaActivo;
            this.numeroCuentaDebe = numeroCuentaDebe;
            this.numeroCuentaHaber = numeroCuentaHaber;
            this.nombreCuantaHaber = nombreCuantaHaber;
            this.nombreCuantaActivo = nombreCuantaActivo;
            this.nombreCuantaDebe = nombreCuantaDebe;
        }

        @Override
        public String toString() {
            return id + " (" + meses + " meses)";
        }
    }
}

