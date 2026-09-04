package com.simpletecno.sopdi.calendario;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.RecordatorioEventoService;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Ventana modal para crear, editar o eliminar un evento del calendario del
 * usuario (tabla usuario_evento).
 *
 * @author Jose Aguirre
 */
public class EventoUsuarioForm extends Window {

    private static final long serialVersionUID = 1L;

    private final Runnable onSaved;
    private final UsuarioEvento evento; // null => nuevo evento
    private final String idUsuarioDestino; // usuario dueño del evento (por defecto, el de sesión)

    private final TextField tituloTxt = new TextField("Título : ");
    private final TextArea descripcionTxt = new TextArea("Descripción : ");
    private final TextField lugarTxt = new TextField("Lugar : ");
    private final PopupDateField inicioDt = new PopupDateField("Inicio : ");
    private final PopupDateField finDt = new PopupDateField("Fin : ");
    private final CheckBox todoElDiaChk = new CheckBox("Todo el día");
    private final CheckBox realizadoChk = new CheckBox("Realizado");
    private final ComboBox colorCbx = new ComboBox("Color : ");

    /**
     * @param onSaved      callback que se ejecuta tras guardar/eliminar (refrescar calendario).
     * @param evento       evento existente a editar, o null para crear uno nuevo.
     * @param defaultStart fecha/hora de inicio por defecto (para creación).
     * @param defaultEnd   fecha/hora de fin por defecto (para creación).
     * @param idUsuarioDestino usuario dueño del evento; null/"" para usar el usuario en sesión.
     * @param nombreUsuarioDestino nombre del usuario destino (para el título, cuando no es el propio).
     */
    public EventoUsuarioForm(Runnable onSaved, UsuarioEvento evento, Date defaultStart, Date defaultEnd,
                             String idUsuarioDestino, String nombreUsuarioDestino) {
        this.onSaved = onSaved;
        this.evento = evento;
        String idSesion = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserId();
        this.idUsuarioDestino = (idUsuarioDestino != null && !idUsuarioDestino.isEmpty())
                ? idUsuarioDestino
                : idSesion;

        String caption = (evento == null ? "Nuevo evento" : "Editar evento");
        // Si el evento pertenece a otro usuario, indicarlo en el título.
        if (!this.idUsuarioDestino.equals(idSesion)
                && nombreUsuarioDestino != null && !nombreUsuarioDestino.trim().isEmpty()) {
            caption += " — Calendario de " + nombreUsuarioDestino;
        }
        setCaption(caption);
        setModal(true);
        setResizable(false);
        setWidth("420px");
        center();

        buildForm();
        cargarValores(defaultStart, defaultEnd);
    }

    private void buildForm() {

        tituloTxt.setWidth("100%");
        tituloTxt.setRequired(true);
        tituloTxt.setMaxLength(255);

        descripcionTxt.setWidth("100%");
        descripcionTxt.setRows(3);

        lugarTxt.setWidth("100%");
        lugarTxt.setMaxLength(255);

        inicioDt.setResolution(Resolution.MINUTE);
        inicioDt.setDateFormat("dd/MM/yyyy HH:mm");
        inicioDt.setWidth("100%");

        finDt.setResolution(Resolution.MINUTE);
        finDt.setDateFormat("dd/MM/yyyy HH:mm");
        finDt.setWidth("100%");

        // Al marcar "Todo el día" se oculta la hora (resolución por día).
        todoElDiaChk.addValueChangeListener(e -> {
            Resolution r = todoElDiaChk.getValue() ? Resolution.DAY : Resolution.MINUTE;
            String fmt = todoElDiaChk.getValue() ? "dd/MM/yyyy" : "dd/MM/yyyy HH:mm";
            inicioDt.setResolution(r);
            inicioDt.setDateFormat(fmt);
            finDt.setResolution(r);
            finDt.setDateFormat(fmt);
        });

        colorCbx.setWidth("100%");
        colorCbx.setNullSelectionAllowed(false);
        colorCbx.addItem("color1");
        colorCbx.setItemCaption("color1", "Azul");
        colorCbx.addItem("color2");
        colorCbx.setItemCaption("color2", "Verde");
        colorCbx.addItem("color3");
        colorCbx.setItemCaption("color3", "Rojo");
        colorCbx.addItem("color4");
        colorCbx.setItemCaption("color4", "Amarillo");
        colorCbx.select("color1");

        Button guardarBtn = new Button("Guardar", FontAwesome.CHECK);
        guardarBtn.addStyleName("primary");
        guardarBtn.setClickShortcut(KeyCode.ENTER);
        guardarBtn.addClickListener(e -> guardar());

        Button cancelarBtn = new Button("Cancelar", FontAwesome.CLOSE);
        cancelarBtn.setClickShortcut(KeyCode.ESCAPE);
        cancelarBtn.addClickListener(e -> close());

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);

        if (evento != null) {
            Button eliminarBtn = new Button("Eliminar", FontAwesome.TRASH);
            eliminarBtn.addStyleName("danger");
            eliminarBtn.addClickListener(e -> eliminar());
            buttons.addComponent(eliminarBtn);
        }

        buttons.addComponent(cancelarBtn);
        buttons.addComponent(guardarBtn);

        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(new MarginInfo(true, true, true, true));
        layout.setSpacing(true);
        layout.addComponents(tituloTxt, descripcionTxt, lugarTxt, todoElDiaChk, inicioDt, finDt, colorCbx);
        // El check "Realizado" solo aplica al editar un evento existente.
        if (evento != null) {
            realizadoChk.setDescription("Marque cuando el evento ya se atendió; deja de aparecer en los recordatorios.");
            layout.addComponent(realizadoChk);
        }
        layout.addComponent(buttons);
        layout.setComponentAlignment(buttons, Alignment.BOTTOM_RIGHT);

        setContent(layout);
    }

    private void cargarValores(Date defaultStart, Date defaultEnd) {
        if (evento == null) {
            inicioDt.setValue(defaultStart != null ? defaultStart : new Date());
            finDt.setValue(defaultEnd != null ? defaultEnd : defaultStart);
        } else {
            tituloTxt.setValue(evento.getCaption() == null ? "" : evento.getCaption());
            descripcionTxt.setValue(evento.getDescription() == null ? "" : evento.getDescription());
            lugarTxt.setValue(evento.getLugar() == null ? "" : evento.getLugar());
            inicioDt.setValue(evento.getStart());
            finDt.setValue(evento.getEnd());
            todoElDiaChk.setValue(evento.isAllDay());
            if (evento.getStyleName() != null && !evento.getStyleName().isEmpty()) {
                colorCbx.select(evento.getStyleName());
            }
            cargarRealizado();
        }
    }

    /**
     * Asegura la columna Realizado y carga su valor actual para el evento en edición.
     */
    private void cargarRealizado() {
        try {
            java.sql.Connection cnx = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection();
            new RecordatorioEventoService().asegurarColumnas(cnx);
            PreparedStatement ps = cnx.prepareStatement(
                    "SELECT Realizado FROM usuario_evento WHERE IdEvento = ?");
            ps.setInt(1, evento.getIdEvento());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                realizadoChk.setValue(rs.getInt("Realizado") == 1);
            }
        } catch (Exception ex) {
            Logger.getLogger(EventoUsuarioForm.class.getName()).log(Level.WARNING,
                    "No se pudo cargar el estado Realizado del evento: " + ex.getMessage());
        }
    }

    private boolean datosValidos() {
        if (tituloTxt.getValue() == null || tituloTxt.getValue().trim().isEmpty()) {
            Notification.show("Ingrese el título del evento.", Notification.Type.WARNING_MESSAGE);
            tituloTxt.focus();
            return false;
        }
        if (inicioDt.getValue() == null) {
            Notification.show("Ingrese la fecha de inicio.", Notification.Type.WARNING_MESSAGE);
            return false;
        }
        if (finDt.getValue() == null) {
            Notification.show("Ingrese la fecha de fin.", Notification.Type.WARNING_MESSAGE);
            return false;
        }
        if (finDt.getValue().before(inicioDt.getValue())) {
            Notification.show("La fecha de fin no puede ser anterior a la de inicio.", Notification.Type.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void guardar() {
        if (!datosValidos()) {
            return;
        }

        boolean esNuevo = (evento == null || evento.getIdEvento() == 0);

        try {
            PreparedStatement ps;

            if (esNuevo) {
                String sql = "INSERT INTO usuario_evento "
                        + " (IdUsuario, Titulo, Descripcion, Lugar, FechaInicio, FechaFin, TodoElDia, Color, Estatus) "
                        + " VALUES (?,?,?,?,?,?,?,?, 'ACTIVO')";
                ps = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection()
                        .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, Integer.parseInt(idUsuarioDestino));
                ps.setString(2, tituloTxt.getValue().trim());
                ps.setString(3, descripcionTxt.getValue());
                ps.setString(4, lugarTxt.getValue());
                ps.setTimestamp(5, new Timestamp(inicioDt.getValue().getTime()));
                ps.setTimestamp(6, new Timestamp(finDt.getValue().getTime()));
                ps.setInt(7, todoElDiaChk.getValue() ? 1 : 0);
                ps.setString(8, String.valueOf(colorCbx.getValue()));
                ps.executeUpdate();

                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) {
                    // id generado disponible si se necesitara
                    keys.getInt(1);
                }
            } else {
                String sql = "UPDATE usuario_evento SET "
                        + " Titulo = ?, Descripcion = ?, Lugar = ?, FechaInicio = ?, FechaFin = ?, TodoElDia = ?, Color = ?, Realizado = ? "
                        + " WHERE IdEvento = ? AND IdUsuario = ?";
                ps = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().prepareStatement(sql);
                ps.setString(1, tituloTxt.getValue().trim());
                ps.setString(2, descripcionTxt.getValue());
                ps.setString(3, lugarTxt.getValue());
                ps.setTimestamp(4, new Timestamp(inicioDt.getValue().getTime()));
                ps.setTimestamp(5, new Timestamp(finDt.getValue().getTime()));
                ps.setInt(6, todoElDiaChk.getValue() ? 1 : 0);
                ps.setString(7, String.valueOf(colorCbx.getValue()));
                ps.setInt(8, realizadoChk.getValue() ? 1 : 0);
                ps.setInt(9, evento.getIdEvento());
                ps.setInt(10, Integer.parseInt(idUsuarioDestino));
                ps.executeUpdate();
            }

            Notification.show("Evento guardado.", Notification.Type.HUMANIZED_MESSAGE);
            if (onSaved != null) {
                onSaved.run();
            }
            close();

        } catch (Exception ex) {
            Logger.getLogger(EventoUsuarioForm.class.getName()).log(Level.SEVERE, ex.getMessage());
            Notification.show("Error al guardar el evento: " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void eliminar() {
        if (evento == null || evento.getIdEvento() == 0) {
            close();
            return;
        }
        try {
            String sql = "UPDATE usuario_evento SET Estatus = 'ELIMINADO' "
                    + " WHERE IdEvento = ? AND IdUsuario = ?";
            PreparedStatement ps = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().prepareStatement(sql);
            ps.setInt(1, evento.getIdEvento());
            ps.setInt(2, Integer.parseInt(idUsuarioDestino));
            ps.executeUpdate();

            Notification.show("Evento eliminado.", Notification.Type.HUMANIZED_MESSAGE);
            if (onSaved != null) {
                onSaved.run();
            }
            close();

        } catch (Exception ex) {
            Logger.getLogger(EventoUsuarioForm.class.getName()).log(Level.SEVERE, ex.getMessage());
            Notification.show("Error al eliminar el evento: " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
