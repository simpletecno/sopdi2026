package com.simpletecno.sopdi.calendario;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Responsive;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Calendar;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.components.calendar.CalendarComponentEvents.EventClick;
import com.vaadin.ui.components.calendar.CalendarComponentEvents.EventClickHandler;
import com.vaadin.ui.components.calendar.CalendarComponentEvents.RangeSelectEvent;
import com.vaadin.ui.components.calendar.CalendarComponentEvents.RangeSelectHandler;
import com.vaadin.ui.components.calendar.event.BasicEventProvider;
import com.vaadin.ui.components.calendar.event.CalendarEvent;
import com.vaadin.ui.themes.ValoTheme;

import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Calendario de eventos del usuario. Permite crear, visualizar y editar los
 * eventos almacenados en la tabla usuario_evento (uno por usuario).
 *
 * @author Jose Aguirre
 */
public class CalendarView extends VerticalLayout implements View {

    private static final long serialVersionUID = 1L;

    private static final String MES = "MES";
    private static final String SEMANA = "SEMANA";
    private static final String DIA = "DIA";

    private final Locale localeEs = new Locale("es", "GT");

    private Calendar calendar;
    private Label rangoLbl;
    private ComboBox vistaCbx;

    // Proveedor de eventos persistente: agregar/quitar eventos en él dispara
    // EventSetChange y el Calendar se repinta automáticamente.
    private final BasicEventProvider eventProvider = new BasicEventProvider();
    private final List<UsuarioEvento> eventosCargados = new ArrayList<UsuarioEvento>();

    // Cursor de navegación (mes/semana/día actualmente mostrado).
    private final java.util.Calendar cursor = java.util.Calendar.getInstance();

    public CalendarView() {

        Responsive.makeResponsive(this);

        setSizeFull();
        setMargin(true);
        setSpacing(true);
        addStyleName("rcorners3");

        crearTablaSiNoExiste();

        construirEncabezado();
        construirBarraHerramientas();
        construirCalendario();

        aplicarRango();
        cargarEventos();
    }

    private void construirEncabezado() {
        Label titleLbl = new Label("Mi Calendario de Eventos");
        titleLbl.addStyleName(ValoTheme.LABEL_H1);
        titleLbl.addStyleName("h1_custom");
        titleLbl.setSizeUndefined();
        addComponent(titleLbl);
    }

    private void construirBarraHerramientas() {

        Button hoyBtn = new Button("Hoy", FontAwesome.CALENDAR);
        hoyBtn.addClickListener(e -> {
            cursor.setTime(new Date());
            aplicarRango();
        });

        Button anteriorBtn = new Button(FontAwesome.CHEVRON_LEFT);
        anteriorBtn.setDescription("Anterior");
        anteriorBtn.addClickListener(e -> desplazar(-1));

        Button siguienteBtn = new Button(FontAwesome.CHEVRON_RIGHT);
        siguienteBtn.setDescription("Siguiente");
        siguienteBtn.addClickListener(e -> desplazar(1));

        rangoLbl = new Label();
        rangoLbl.addStyleName(ValoTheme.LABEL_H3);
        rangoLbl.setSizeUndefined();

        vistaCbx = new ComboBox();
        vistaCbx.setNullSelectionAllowed(false);
        vistaCbx.setTextInputAllowed(false);
        vistaCbx.setWidth("120px");
        vistaCbx.addItem(MES);
        vistaCbx.setItemCaption(MES, "Mes");
        vistaCbx.addItem(SEMANA);
        vistaCbx.setItemCaption(SEMANA, "Semana");
        vistaCbx.addItem(DIA);
        vistaCbx.setItemCaption(DIA, "Día");
        vistaCbx.select(MES);
        vistaCbx.addValueChangeListener(e -> aplicarRango());

        Button nuevoBtn = new Button("Nuevo evento", FontAwesome.PLUS);
        nuevoBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        nuevoBtn.addClickListener(e -> abrirFormularioNuevo());

        HorizontalLayout izquierda = new HorizontalLayout(hoyBtn, anteriorBtn, siguienteBtn, rangoLbl);
        izquierda.setSpacing(true);
        izquierda.setComponentAlignment(rangoLbl, Alignment.MIDDLE_LEFT);

        HorizontalLayout derecha = new HorizontalLayout(vistaCbx, nuevoBtn);
        derecha.setSpacing(true);

        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setWidth("100%");
        toolbar.setSpacing(true);
        toolbar.addComponents(izquierda, derecha);
        toolbar.setComponentAlignment(izquierda, Alignment.MIDDLE_LEFT);
        toolbar.setComponentAlignment(derecha, Alignment.MIDDLE_RIGHT);
        toolbar.setExpandRatio(izquierda, 1);

        addComponent(toolbar);
    }

    private void construirCalendario() {
        calendar = new Calendar();
        calendar.setEventProvider(eventProvider);
        calendar.setLocale(localeEs);
        calendar.setSizeFull();
        calendar.setFirstVisibleHourOfDay(6);
        calendar.setLastVisibleHourOfDay(22);

        // Clic sobre un evento existente -> editar / eliminar.
        calendar.setHandler(new EventClickHandler() {
            @Override
            public void eventClick(EventClick event) {
                CalendarEvent ce = event.getCalendarEvent();
                if (ce instanceof UsuarioEvento) {
                    UsuarioEvento ev = (UsuarioEvento) ce;
                    UI.getCurrent().addWindow(
                            new EventoUsuarioForm(CalendarView.this::cargarEventos, ev, null, null));
                }
            }
        });

        // Selección de un rango (arrastrar) -> crear evento con esas fechas.
        calendar.setHandler(new RangeSelectHandler() {
            @Override
            public void rangeSelect(RangeSelectEvent event) {
                UI.getCurrent().addWindow(
                        new EventoUsuarioForm(CalendarView.this::cargarEventos, null,
                                event.getStart(), event.getEnd()));
            }
        });

        addComponent(calendar);
        setExpandRatio(calendar, 1);
    }

    /** Desplaza el cursor hacia adelante/atrás según la vista activa. */
    private void desplazar(int direccion) {
        String modo = String.valueOf(vistaCbx.getValue());
        if (SEMANA.equals(modo)) {
            cursor.add(java.util.Calendar.DAY_OF_MONTH, 7 * direccion);
        } else if (DIA.equals(modo)) {
            cursor.add(java.util.Calendar.DAY_OF_MONTH, direccion);
        } else {
            cursor.add(java.util.Calendar.MONTH, direccion);
        }
        aplicarRango();
    }

    /** Calcula el rango de fechas a mostrar y actualiza el calendario y la etiqueta. */
    private void aplicarRango() {
        String modo = String.valueOf(vistaCbx.getValue());

        java.util.Calendar ini = (java.util.Calendar) cursor.clone();
        java.util.Calendar fin = (java.util.Calendar) cursor.clone();

        if (SEMANA.equals(modo)) {
            ini.set(java.util.Calendar.DAY_OF_WEEK, ini.getFirstDayOfWeek());
            fin.setTime(ini.getTime());
            fin.add(java.util.Calendar.DAY_OF_MONTH, 6);
        } else if (DIA.equals(modo)) {
            // ini y fin en el mismo día
        } else { // MES
            ini.set(java.util.Calendar.DAY_OF_MONTH, 1);
            fin.set(java.util.Calendar.DAY_OF_MONTH, fin.getActualMaximum(java.util.Calendar.DAY_OF_MONTH));
        }

        ponerInicioDia(ini);
        ponerFinDia(fin);

        calendar.setStartDate(ini.getTime());
        calendar.setEndDate(fin.getTime());

        actualizarEtiquetaRango(modo);
    }

    private void actualizarEtiquetaRango(String modo) {
        if (DIA.equals(modo)) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd 'de' MMMM 'de' yyyy", localeEs);
            rangoLbl.setValue(capitalizar(sdf.format(cursor.getTime())));
        } else if (SEMANA.equals(modo)) {
            java.util.Calendar ini = (java.util.Calendar) cursor.clone();
            ini.set(java.util.Calendar.DAY_OF_WEEK, ini.getFirstDayOfWeek());
            java.util.Calendar fin = (java.util.Calendar) ini.clone();
            fin.add(java.util.Calendar.DAY_OF_MONTH, 6);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", localeEs);
            rangoLbl.setValue("Semana: " + sdf.format(ini.getTime()) + " - " + sdf.format(fin.getTime()));
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM 'de' yyyy", localeEs);
            rangoLbl.setValue(capitalizar(sdf.format(cursor.getTime())));
        }
    }

    private String capitalizar(String texto) {
        if (texto == null || texto.isEmpty()) {
            return texto;
        }
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }

    private void ponerInicioDia(java.util.Calendar c) {
        c.set(java.util.Calendar.HOUR_OF_DAY, 0);
        c.set(java.util.Calendar.MINUTE, 0);
        c.set(java.util.Calendar.SECOND, 0);
        c.set(java.util.Calendar.MILLISECOND, 0);
    }

    private void ponerFinDia(java.util.Calendar c) {
        c.set(java.util.Calendar.HOUR_OF_DAY, 23);
        c.set(java.util.Calendar.MINUTE, 59);
        c.set(java.util.Calendar.SECOND, 59);
        c.set(java.util.Calendar.MILLISECOND, 0);
    }

    private void abrirFormularioNuevo() {
        Date inicio = new Date();
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.setTime(inicio);
        c.add(java.util.Calendar.HOUR_OF_DAY, 1);
        UI.getCurrent().addWindow(
                new EventoUsuarioForm(this::cargarEventos, null, inicio, c.getTime()));
    }

    /** Carga (o recarga) los eventos ACTIVOS del usuario en el calendario. */
    private void cargarEventos() {

        // Quita los eventos previos del proveedor (dispara el repintado).
        for (UsuarioEvento previo : eventosCargados) {
            eventProvider.removeEvent(previo);
        }
        eventosCargados.clear();

        String queryString = " SELECT * FROM usuario_evento ";
        queryString += " WHERE IdUsuario = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserId();
        queryString += " AND Estatus = 'ACTIVO'";
        queryString += " ORDER BY FechaInicio";

        Statement st = null;
        ResultSet rs = null;
        try {
            st = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rs = st.executeQuery(queryString);

            while (rs.next()) {
                UsuarioEvento ev = new UsuarioEvento();
                ev.setIdEvento(rs.getInt("IdEvento"));
                ev.setCaption(rs.getString("Titulo"));
                ev.setDescription(rs.getString("Descripcion"));
                ev.setLugar(rs.getString("Lugar"));
                ev.setStart(new Date(rs.getTimestamp("FechaInicio").getTime()));
                ev.setEnd(new Date(rs.getTimestamp("FechaFin").getTime()));
                ev.setAllDay(rs.getInt("TodoElDia") == 1);
                String color = rs.getString("Color");
                if (color != null && !color.isEmpty()) {
                    ev.setStyleName(color);
                }
                eventProvider.addEvent(ev);
                eventosCargados.add(ev);
            }
        } catch (Exception ex) {
            Logger.getLogger(CalendarView.class.getName()).log(Level.SEVERE, ex.getMessage());
            Notification.show("Error al cargar los eventos del calendario: " + ex.getMessage(),
                    Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) { }
            try { if (st != null) st.close(); } catch (Exception ignored) { }
        }
    }

    /** Crea la tabla usuario_evento si aún no existe. */
    private void crearTablaSiNoExiste() {
        String ddl = "CREATE TABLE IF NOT EXISTS usuario_evento ("
                + " IdEvento INT NOT NULL AUTO_INCREMENT,"
                + " IdUsuario INT NOT NULL,"
                + " Titulo VARCHAR(255) NOT NULL,"
                + " Descripcion TEXT NULL,"
                + " Lugar VARCHAR(255) NULL,"
                + " FechaInicio DATETIME NOT NULL,"
                + " FechaFin DATETIME NOT NULL,"
                + " TodoElDia TINYINT(1) NOT NULL DEFAULT 0,"
                + " Color VARCHAR(20) NULL,"
                + " Estatus VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',"
                + " FechaCreacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + " PRIMARY KEY (IdEvento),"
                + " KEY idx_usuario_evento_usuario (IdUsuario)"
                + " )";
        Statement st = null;
        try {
            st = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            st.executeUpdate(ddl);
        } catch (Exception ex) {
            Logger.getLogger(CalendarView.class.getName()).log(Level.SEVERE,
                    "Error al crear la tabla usuario_evento: " + ex.getMessage());
        } finally {
            try { if (st != null) st.close(); } catch (Exception ignored) { }
        }
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - Mi Calendario");
        cargarEventos();
    }
}
