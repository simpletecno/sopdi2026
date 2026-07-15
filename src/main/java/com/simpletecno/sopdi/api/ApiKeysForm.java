package com.simpletecno.sopdi.api;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Runo;
import com.vaadin.ui.themes.ValoTheme;

import java.security.SecureRandom;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Ventana modal para administrar las API Keys de una empresa (tabla api_key).
 *
 * Las claves son POR EMPRESA (columna IdEmpresa). Permite crear, editar,
 * activar/desactivar y eliminar claves. Cada clave puede tener una fecha de
 * vencimiento o ser permanente (FechaVencimiento NULL).
 *
 * El acceso está restringido a usuarios con perfil ADMINISTRADOR; el punto de
 * entrada es un botón en {@code EmpresasContablesForm}.
 *
 * La validación en tiempo de request la realiza {@link ApiKeyFilter}, que exige
 * Activo = 1 y que la clave no esté vencida.
 *
 * @author Jose Aguirre
 */
public class ApiKeysForm extends Window {

    static final String ID_PROPERTY          = "Id";
    static final String NOMBRE_PROPERTY      = "Nombre";
    static final String KEY_PROPERTY         = "API Key";
    static final String ACTIVO_PROPERTY      = "Activo";
    static final String VENCIMIENTO_PROPERTY = "Vencimiento";
    static final String CREACION_PROPERTY    = "Creada";

    private final String idEmpresa;
    private final String nombreEmpresa;

    UI mainUI;
    Statement stQuery;
    ResultSet rsRecords;
    String queryString;

    IndexedContainer container = new IndexedContainer();
    Grid keysGrid;

    // Panel inline de alta/edición
    VerticalLayout formPanel;
    TextField nombreTxt;
    CheckBox permanenteChk;
    PopupDateField vencimientoDt;
    String idKeyEdit = "";
    boolean editando = false;

    public ApiKeysForm(String idEmpresa, String nombreEmpresa) {
        this.mainUI = UI.getCurrent();
        this.idEmpresa = idEmpresa;
        this.nombreEmpresa = nombreEmpresa;

        setCaption("Administración de API Keys");
        setModal(true);
        setResponsive(true);
        setWidth("820px");
        setHeight("90%");
        center();

        // Guardia defensiva: solo ADMINISTRADOR.
        if (!esAdministrador()) {
            Notification.show("Acceso restringido a administradores.", Notification.Type.WARNING_MESSAGE);
            VerticalLayout vacio = new VerticalLayout();
            vacio.setMargin(true);
            vacio.addComponent(new Label("No tiene permisos para administrar API Keys."));
            setContent(vacio);
            return;
        }

        asegurarTabla();
        setContent(construirContenido());
        llenarTablaKeys();
    }

    private boolean esAdministrador() {
        try {
            return "ADMINISTRADOR".equals(
                    ((SopdiUI) mainUI).sessionInformation.getStrUserProfile());
        } catch (Exception ex) {
            return false;
        }
    }

    // =========================================================================
    //  UI
    // =========================================================================

    private VerticalLayout construirContenido() {
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setMargin(true);
        content.setSpacing(true);

        Label titleLbl = new Label(idEmpresa + "  " + nombreEmpresa + "  –  API KEYS");
        titleLbl.addStyleName(Runo.LABEL_H2);
        titleLbl.setSizeUndefined();
        content.addComponent(titleLbl);
        content.setComponentAlignment(titleLbl, Alignment.TOP_CENTER);

        content.addComponent(construirFormPanel());

        // ── Grid ─────────────────────────────────────────────────────────────
        container.addContainerProperty(ID_PROPERTY,          String.class, null);
        container.addContainerProperty(NOMBRE_PROPERTY,      String.class, null);
        container.addContainerProperty(KEY_PROPERTY,         String.class, null);
        container.addContainerProperty(ACTIVO_PROPERTY,      String.class, null);
        container.addContainerProperty(VENCIMIENTO_PROPERTY, String.class, null);
        container.addContainerProperty(CREACION_PROPERTY,    String.class, null);

        keysGrid = new Grid("API Keys de la empresa", container);
        keysGrid.setImmediate(true);
        keysGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        keysGrid.setHeightMode(HeightMode.ROW);
        keysGrid.setHeightByRows(8);
        keysGrid.setWidth("100%");
        keysGrid.setResponsive(true);
        keysGrid.setEditorEnabled(false);

        keysGrid.getColumn(ID_PROPERTY).setHidable(true).setHidden(true);
        keysGrid.getColumn(NOMBRE_PROPERTY).setExpandRatio(3);
        keysGrid.getColumn(KEY_PROPERTY).setExpandRatio(5);
        keysGrid.getColumn(ACTIVO_PROPERTY).setExpandRatio(1);
        keysGrid.getColumn(VENCIMIENTO_PROPERTY).setExpandRatio(2);
        keysGrid.getColumn(CREACION_PROPERTY).setExpandRatio(2);

        Panel gridScroll = new Panel(keysGrid);
        gridScroll.setWidth("100%");
        content.addComponent(gridScroll);
        content.setExpandRatio(gridScroll, 1);

        content.addComponent(construirBarraBotones());

        return content;
    }

    private VerticalLayout construirFormPanel() {
        formPanel = new VerticalLayout();
        formPanel.setVisible(false);
        formPanel.setWidth("100%");
        formPanel.setSpacing(true);
        formPanel.setMargin(new MarginInfo(true));
        formPanel.addStyleName(ValoTheme.LAYOUT_CARD);

        Label formTitle = new Label("Datos de la clave");
        formTitle.addStyleName(ValoTheme.LABEL_H3);
        formPanel.addComponent(formTitle);

        nombreTxt = new TextField("Nombre / descripción");
        nombreTxt.setIcon(FontAwesome.TAG);
        nombreTxt.setWidth("100%");
        nombreTxt.setRequired(true);
        formPanel.addComponent(nombreTxt);

        permanenteChk = new CheckBox("Permanente (sin fecha de vencimiento)");
        permanenteChk.setValue(true);
        formPanel.addComponent(permanenteChk);

        vencimientoDt = new PopupDateField("Fecha de vencimiento");
        vencimientoDt.setIcon(FontAwesome.CALENDAR);
        vencimientoDt.setResolution(Resolution.DAY);
        vencimientoDt.setDateFormat("yyyy-MM-dd");
        vencimientoDt.setEnabled(false);
        formPanel.addComponent(vencimientoDt);

        permanenteChk.addValueChangeListener(e -> {
            boolean permanente = permanenteChk.getValue();
            vencimientoDt.setEnabled(!permanente);
            if (permanente) {
                vencimientoDt.setValue(null);
            }
        });

        Button guardarBtn = new Button("Guardar", FontAwesome.SAVE);
        guardarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        guardarBtn.addClickListener(e -> guardarKey());

        Button cancelarBtn = new Button("Cancelar", FontAwesome.TIMES);
        cancelarBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        cancelarBtn.addClickListener(e -> {
            formPanel.setVisible(false);
            limpiarForm();
        });

        HorizontalLayout formButtons = new HorizontalLayout(cancelarBtn, guardarBtn);
        formButtons.setSpacing(true);
        formPanel.addComponent(formButtons);

        return formPanel;
    }

    private HorizontalLayout construirBarraBotones() {
        Button nuevaBtn = new Button("Nueva", FontAwesome.PLUS);
        nuevaBtn.setDescription("Generar una nueva API Key.");
        nuevaBtn.addClickListener(e -> {
            editando = false;
            idKeyEdit = "";
            limpiarForm();
            formPanel.setVisible(true);
        });

        Button editarBtn = new Button("Editar", FontAwesome.EDIT);
        editarBtn.setDescription("Editar nombre o vencimiento.");
        editarBtn.addClickListener(e -> cargarParaEditar());

        Button toggleBtn = new Button("Activar/Desactivar", FontAwesome.TOGGLE_ON);
        toggleBtn.setDescription("Cambia el estado activo de la clave seleccionada.");
        toggleBtn.addClickListener(e -> toggleActivo());

        Button copiarBtn = new Button("Copiar clave", FontAwesome.COPY);
        copiarBtn.setDescription("Muestra la clave completa para copiarla.");
        copiarBtn.addClickListener(e -> mostrarClave());

        Button eliminarBtn = new Button("Eliminar", FontAwesome.TRASH);
        eliminarBtn.addStyleName(ValoTheme.BUTTON_DANGER);
        eliminarBtn.addClickListener(e -> eliminarKey());

        Button salirBtn = new Button("Salir", FontAwesome.SIGN_OUT);
        salirBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        salirBtn.addClickListener(e -> close());

        HorizontalLayout bar = new HorizontalLayout(
                nuevaBtn, editarBtn, toggleBtn, copiarBtn, eliminarBtn, salirBtn);
        bar.setSpacing(true);
        return bar;
    }

    // =========================================================================
    //  Esquema
    // =========================================================================

    /**
     * Garantiza que la tabla api_key exista con todas las columnas necesarias,
     * incluyendo IdEmpresa para el alcance por empresa. La tabla original solo
     * contenía KeyValue y Activo; aquí se agregan las columnas faltantes.
     */
    private void asegurarTabla() {
        String ddl = "CREATE TABLE IF NOT EXISTS api_key ("
                + " IdApiKey INT NOT NULL AUTO_INCREMENT,"
                + " IdEmpresa INT NULL,"
                + " Nombre VARCHAR(255) NULL,"
                + " KeyValue VARCHAR(255) NOT NULL,"
                + " Activo TINYINT(1) NOT NULL DEFAULT 1,"
                + " FechaCreacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + " FechaVencimiento DATE NULL,"
                + " PRIMARY KEY (IdApiKey),"
                + " UNIQUE KEY uk_api_key_value (KeyValue),"
                + " KEY idx_api_key_empresa (IdEmpresa)"
                + " )";
        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(ddl);
        } catch (Exception ex) {
            Logger.getLogger(ApiKeysForm.class.getName()).log(Level.WARNING,
                    "No se pudo asegurar la tabla api_key: {0}", ex.getMessage());
        }

        // Para instalaciones donde la tabla ya existía con el esquema mínimo.
        asegurarColumna("IdApiKey",
                "ALTER TABLE api_key ADD COLUMN IdApiKey INT NOT NULL AUTO_INCREMENT PRIMARY KEY FIRST");
        asegurarColumna("IdEmpresa",
                "ALTER TABLE api_key ADD COLUMN IdEmpresa INT NULL");
        asegurarColumna("Nombre",
                "ALTER TABLE api_key ADD COLUMN Nombre VARCHAR(255) NULL");
        asegurarColumna("FechaCreacion",
                "ALTER TABLE api_key ADD COLUMN FechaCreacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP");
        asegurarColumna("FechaVencimiento",
                "ALTER TABLE api_key ADD COLUMN FechaVencimiento DATE NULL");
    }

    private void asegurarColumna(String columna, String ddl) {
        String existeSql = "SELECT COUNT(*) FROM information_schema.COLUMNS "
                + " WHERE TABLE_SCHEMA = DATABASE() "
                + " AND TABLE_NAME = 'api_key' "
                + " AND COLUMN_NAME = '" + columna + "'";
        try {
            Statement st = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            ResultSet rs = st.executeQuery(existeSql);
            boolean existe = rs.next() && rs.getInt(1) > 0;
            rs.close();
            if (!existe) {
                st.executeUpdate(ddl);
            }
        } catch (Exception ex) {
            Logger.getLogger(ApiKeysForm.class.getName()).log(Level.WARNING,
                    "No se pudo asegurar la columna api_key." + columna + ": {0}", ex.getMessage());
        }
    }

    // =========================================================================
    //  Datos
    // =========================================================================

    public void llenarTablaKeys() {
        container.removeAllItems();

        queryString = " SELECT IdApiKey, Nombre, KeyValue, Activo,"
                + " DATE_FORMAT(FechaVencimiento, '%Y-%m-%d') AS Vence,"
                + " DATE_FORMAT(FechaCreacion, '%Y-%m-%d') AS Creada,"
                + " (FechaVencimiento IS NOT NULL AND FechaVencimiento < CURDATE()) AS Vencida"
                + " FROM api_key"
                + " WHERE IdEmpresa = " + idEmpresa
                + " ORDER BY IdApiKey DESC";

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) {
                Object itemId = container.addItem();

                String vence   = rsRecords.getString("Vence");
                boolean vencida = rsRecords.getInt("Vencida") == 1;
                String vencimiento = (vence == null || vence.isEmpty())
                        ? "Permanente"
                        : (vencida ? vence + " (VENCIDA)" : vence);

                container.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords.getString("IdApiKey"));
                container.getContainerProperty(itemId, NOMBRE_PROPERTY).setValue(nvl(rsRecords.getString("Nombre")));
                container.getContainerProperty(itemId, KEY_PROPERTY).setValue(enmascarar(rsRecords.getString("KeyValue")));
                container.getContainerProperty(itemId, ACTIVO_PROPERTY).setValue(rsRecords.getInt("Activo") == 1 ? "Sí" : "No");
                container.getContainerProperty(itemId, VENCIMIENTO_PROPERTY).setValue(vencimiento);
                container.getContainerProperty(itemId, CREACION_PROPERTY).setValue(nvl(rsRecords.getString("Creada")));
            }
        } catch (Exception ex) {
            Logger.getLogger(ApiKeysForm.class.getName()).log(Level.SEVERE,
                    "Error al listar api_key: {0}", ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void guardarKey() {
        String nombre = nombreTxt.getValue() == null ? "" : nombreTxt.getValue().trim();
        if (nombre.isEmpty()) {
            Notification.show("Ingrese un nombre o descripción para la clave.", Notification.Type.WARNING_MESSAGE);
            return;
        }

        boolean permanente = permanenteChk.getValue();
        String vencimientoSql;
        if (permanente) {
            vencimientoSql = "NULL";
        } else {
            Date fecha = vencimientoDt.getValue();
            if (fecha == null) {
                Notification.show("Seleccione una fecha de vencimiento o marque la clave como permanente.",
                        Notification.Type.WARNING_MESSAGE);
                return;
            }
            vencimientoSql = "'" + new SimpleDateFormat("yyyy-MM-dd").format(fecha) + "'";
        }

        try {
            Statement st = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            if (editando && !idKeyEdit.isEmpty()) {
                queryString = "UPDATE api_key SET "
                        + "  Nombre = '" + nombre.replace("'", "''") + "'"
                        + ", FechaVencimiento = " + vencimientoSql
                        + " WHERE IdApiKey = " + idKeyEdit
                        + " AND IdEmpresa = " + idEmpresa;
                st.executeUpdate(queryString);

                llenarTablaKeys();
                formPanel.setVisible(false);
                limpiarForm();
                notificar("Clave actualizada.", FontAwesome.CHECK);

            } else {
                String nuevaClave = generarClave();
                queryString = "INSERT INTO api_key (IdEmpresa, Nombre, KeyValue, Activo, FechaVencimiento)"
                        + " VALUES (" + idEmpresa
                        + ", '" + nombre.replace("'", "''") + "'"
                        + ", '" + nuevaClave + "'"
                        + ", 1"
                        + ", " + vencimientoSql + ")";
                st.executeUpdate(queryString);

                llenarTablaKeys();
                formPanel.setVisible(false);
                limpiarForm();
                mostrarClaveGenerada(nuevaClave);
            }
        } catch (SQLException ex) {
            Notification.show("Error al guardar la clave: " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void cargarParaEditar() {
        String id = idSeleccionado();
        if (id == null) {
            return;
        }
        queryString = "SELECT Nombre, DATE_FORMAT(FechaVencimiento, '%Y-%m-%d') AS Vence"
                + " FROM api_key WHERE IdApiKey = " + id + " AND IdEmpresa = " + idEmpresa;
        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);
            if (rsRecords.next()) {
                editando = true;
                idKeyEdit = id;
                nombreTxt.setValue(nvl(rsRecords.getString("Nombre")));
                String vence = rsRecords.getString("Vence");
                if (vence == null || vence.isEmpty()) {
                    permanenteChk.setValue(true);
                    vencimientoDt.setValue(null);
                    vencimientoDt.setEnabled(false);
                } else {
                    permanenteChk.setValue(false);
                    vencimientoDt.setEnabled(true);
                    vencimientoDt.setValue(new SimpleDateFormat("yyyy-MM-dd").parse(vence));
                }
                formPanel.setVisible(true);
            }
        } catch (Exception ex) {
            Notification.show("Error al cargar la clave: " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void toggleActivo() {
        String id = idSeleccionado();
        if (id == null) {
            return;
        }
        try {
            queryString = "UPDATE api_key SET Activo = CASE WHEN Activo = 1 THEN 0 ELSE 1 END"
                    + " WHERE IdApiKey = " + id + " AND IdEmpresa = " + idEmpresa;
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);
            llenarTablaKeys();
            keysGrid.select(null);
            Notification.show("Estado de la clave actualizado.", Notification.Type.HUMANIZED_MESSAGE);
        } catch (SQLException ex) {
            Notification.show("Error al actualizar el estado: " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void eliminarKey() {
        String id = idSeleccionado();
        if (id == null) {
            return;
        }
        try {
            queryString = "DELETE FROM api_key WHERE IdApiKey = " + id + " AND IdEmpresa = " + idEmpresa;
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);
            llenarTablaKeys();
            keysGrid.select(null);
            Notification.show("API Key eliminada.", Notification.Type.HUMANIZED_MESSAGE);
        } catch (SQLException ex) {
            Notification.show("Error al eliminar la clave: " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    /**
     * Muestra la clave completa (sin enmascarar) del registro seleccionado para
     * poder copiarla. Se lee de la BD porque la grilla solo guarda la versión
     * enmascarada.
     */
    private void mostrarClave() {
        String id = idSeleccionado();
        if (id == null) {
            return;
        }
        try (PreparedStatement ps = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection()
                .prepareStatement("SELECT KeyValue FROM api_key WHERE IdApiKey = ? AND IdEmpresa = ?")) {
            ps.setString(1, id);
            ps.setString(2, idEmpresa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    mostrarClaveGenerada(rs.getString("KeyValue"));
                }
            }
        } catch (SQLException ex) {
            Notification.show("Error al leer la clave: " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    /** Ventana secundaria con la clave en un campo de solo lectura para copiar. */
    private void mostrarClaveGenerada(String clave) {
        Window w = new Window("API Key");
        w.setModal(true);
        w.setResizable(false);
        w.setWidth("560px");
        w.center();

        VerticalLayout l = new VerticalLayout();
        l.setMargin(true);
        l.setSpacing(true);

        Label info = new Label("Copie y guarde esta clave en un lugar seguro. "
                + "Se envía en el header <b>X-API-Key</b> al consumir el API REST.",
                com.vaadin.shared.ui.label.ContentMode.HTML);
        l.addComponent(info);

        TextField claveTxt = new TextField();
        claveTxt.setValue(clave);
        claveTxt.setReadOnly(true);
        claveTxt.setWidth("100%");
        claveTxt.addStyleName(ValoTheme.TEXTFIELD_LARGE);
        l.addComponent(claveTxt);

        Button cerrar = new Button("Cerrar", ev -> w.close());
        cerrar.addStyleName(ValoTheme.BUTTON_PRIMARY);
        l.addComponent(cerrar);
        l.setComponentAlignment(cerrar, Alignment.BOTTOM_RIGHT);

        w.setContent(l);
        UI.getCurrent().addWindow(w);
    }

    // =========================================================================
    //  Utilidades
    // =========================================================================

    /** Genera una clave aleatoria criptográficamente fuerte (Base64 URL, prefijo sk_). */
    private String generarClave() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return "sk_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** Enmascara la clave para el listado: muestra el prefijo y los últimos 4. */
    private String enmascarar(String clave) {
        if (clave == null) {
            return "";
        }
        if (clave.length() <= 12) {
            return "••••";
        }
        return clave.substring(0, 7) + "…" + clave.substring(clave.length() - 4);
    }

    private String idSeleccionado() {
        if (keysGrid.getSelectedRow() == null) {
            Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
            return null;
        }
        return String.valueOf(container.getContainerProperty(keysGrid.getSelectedRow(), ID_PROPERTY).getValue());
    }

    private void limpiarForm() {
        nombreTxt.setValue("");
        permanenteChk.setValue(true);
        vencimientoDt.setValue(null);
        vencimientoDt.setEnabled(false);
        idKeyEdit = "";
        editando = false;
    }

    private void notificar(String msg, FontAwesome icon) {
        Notification n = new Notification(msg, Notification.Type.HUMANIZED_MESSAGE);
        n.setDelayMsec(1500);
        n.setPosition(Position.MIDDLE_CENTER);
        n.setIcon(icon);
        n.show(Page.getCurrent());
    }

    private String nvl(String value) {
        return value == null ? "" : value;
    }
}
