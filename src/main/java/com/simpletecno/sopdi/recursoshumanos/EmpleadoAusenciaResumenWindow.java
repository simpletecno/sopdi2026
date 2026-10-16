package com.simpletecno.sopdi.recursoshumanos;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class EmpleadoAusenciaResumenWindow extends Window {

    public EmpleadoAusenciaResumenWindow(UI mainUI,
                                        String idEmpleado,
                                        String nombreEmpleado,
                                        String cargo,
                                        String tipo,
                                        Date inicio,
                                        Date fin,
                                        double dias,
                                        String boleta,
                                        boolean medioInicio,
                                        boolean medioFin,
                                        boolean sabados,
                                        Runnable onSaveSuccess) {
        addCloseShortcut(com.vaadin.event.ShortcutAction.KeyCode.ESCAPE, null);

        setCaption("Resumen de Ausencia");
        setModal(true);
        setResizable(false);
        setWidth("520px");

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setMargin(true);
        content.setWidth("100%");

        VerticalLayout empleadoHeader = new VerticalLayout();
        empleadoHeader.setSpacing(false);
        empleadoHeader.setMargin(false);
        empleadoHeader.setWidth("100%");

        Label empleadoLbl = new Label(emptyToDefault(nombreEmpleado, "Empleado") + " - " + idEmpleado);
        empleadoLbl.addStyleName(ValoTheme.LABEL_H3);
        empleadoLbl.addStyleName(ValoTheme.LABEL_BOLD);
        empleadoLbl.addStyleName(ValoTheme.LABEL_NO_MARGIN);
        empleadoLbl.setWidth("100%");

        Label cargoLbl = new Label(emptyToDefault(cargo, ""));
        cargoLbl.addStyleName(ValoTheme.LABEL_NO_MARGIN);
        cargoLbl.setWidth("100%");

        empleadoHeader.addComponents(empleadoLbl, cargoLbl);
        empleadoHeader.setComponentAlignment(empleadoLbl, Alignment.MIDDLE_CENTER);
        empleadoHeader.setComponentAlignment(cargoLbl, Alignment.MIDDLE_CENTER);
        content.addComponent(empleadoHeader);

        Label tipoLbl = new Label("Tipo: " + emptyToDefault(tipo, ""));
        tipoLbl.addStyleName(ValoTheme.LABEL_BOLD);
        tipoLbl.setWidth("100%");
        content.addComponent(tipoLbl);

        HorizontalLayout datosLayout = new HorizontalLayout();
        datosLayout.setSpacing(true);
        datosLayout.setWidth("100%");

        Component boletaBlock = createInfoBlock("Boleta", emptyToDefault(boleta, ""));
        HorizontalLayout fechasLayout = new HorizontalLayout();
        fechasLayout.setSpacing(true);
        fechasLayout.setWidthUndefined();
        Component inicioBlock = createInfoBlock("Inicio", formatDate(inicio));
        Component finBlock = createInfoBlock("Fin", formatDate(fin));
        inicioBlock.setWidth("110px");
        finBlock.setWidth("110px");
        fechasLayout.addComponents(inicioBlock, finBlock);

        datosLayout.addComponents(boletaBlock, fechasLayout);
        datosLayout.setExpandRatio(boletaBlock, 1);
        datosLayout.setComponentAlignment(boletaBlock, Alignment.MIDDLE_LEFT);
        datosLayout.setComponentAlignment(fechasLayout, Alignment.MIDDLE_RIGHT);
        content.addComponent(datosLayout);

        Panel diasPanel = createDiasPanel(dias);
        content.addComponent(diasPanel);
        content.setComponentAlignment(diasPanel, Alignment.MIDDLE_CENTER);

        GridLayout booleanLayout = new GridLayout(3, 2);
        booleanLayout.setSpacing(true);
        booleanLayout.setWidth("100%");
        booleanLayout.setColumnExpandRatio(0, 1);
        booleanLayout.setColumnExpandRatio(1, 1);
        booleanLayout.setColumnExpandRatio(2, 1);
        Label medioInicioTitle = createBooleanTitle("Medio Dia Inicio");
        Label medioFinTitle = createBooleanTitle("Medio Dia Fin");
        Label sabadosTitle = createBooleanTitle("Sabados");
        Label medioInicioValue = createBooleanValue(medioInicio);
        Label medioFinValue = createBooleanValue(medioFin);
        Label sabadosValue = createBooleanValue(sabados);
        booleanLayout.addComponent(medioInicioTitle, 0, 0);
        booleanLayout.addComponent(medioFinTitle, 1, 0);
        booleanLayout.addComponent(sabadosTitle, 2, 0);
        booleanLayout.addComponent(medioInicioValue, 0, 1);
        booleanLayout.addComponent(medioFinValue, 1, 1);
        booleanLayout.addComponent(sabadosValue, 2, 1);
        booleanLayout.setComponentAlignment(medioInicioTitle, Alignment.MIDDLE_CENTER);
        booleanLayout.setComponentAlignment(medioFinTitle, Alignment.MIDDLE_CENTER);
        booleanLayout.setComponentAlignment(sabadosTitle, Alignment.MIDDLE_CENTER);
        booleanLayout.setComponentAlignment(medioInicioValue, Alignment.MIDDLE_CENTER);
        booleanLayout.setComponentAlignment(medioFinValue, Alignment.MIDDLE_CENTER);
        booleanLayout.setComponentAlignment(sabadosValue, Alignment.MIDDLE_CENTER);
        content.addComponent(booleanLayout);
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);

        Button saveBtn = new Button("Guardar");
        saveBtn.setIcon(FontAwesome.SAVE);
        saveBtn.addClickListener(event -> {
            boolean ok = saveNewAbsence(mainUI, idEmpleado, cargo, tipo, inicio, fin, dias, boleta, medioInicio, medioFin, sabados);
            if (ok) {
                Notification notif = new Notification("REGISTRO AGREGADO EXITOSAMENTE.", Notification.Type.HUMANIZED_MESSAGE);
                notif.setDelayMsec(1500);
                notif.setPosition(Position.MIDDLE_CENTER);
                notif.setIcon(FontAwesome.CHECK);
                notif.show(Page.getCurrent());

                if (onSaveSuccess != null) onSaveSuccess.run();
                close();
            }
        });

        Button closeBtn = new Button("Cerrar");
        closeBtn.addClickListener(event -> close());

        buttons.addComponents(saveBtn, closeBtn);
        content.addComponent(buttons);
        content.setComponentAlignment(buttons, Alignment.MIDDLE_CENTER);

        setContent(content);
    }

    private VerticalLayout createInfoBlock(String title, String value) {
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(false);
        layout.setMargin(false);
        layout.setWidth("100%");

        Label titleLbl = new Label(title);
        titleLbl.addStyleName(ValoTheme.LABEL_SMALL);
        titleLbl.addStyleName(ValoTheme.LABEL_BOLD);

        Label valueLbl = new Label(value == null ? "" : value);
        valueLbl.setWidth("100%");

        layout.addComponents(titleLbl, valueLbl);
        return layout;
    }

    private Panel createDiasPanel(double dias) {
        Panel panel = new Panel("Dias");
        panel.setWidth("100%");
        panel.setHeight("140px");

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(false);
        layout.setMargin(false);
        layout.setSizeFull();

        Label numeroLbl = new Label(String.valueOf(dias));
        numeroLbl.addStyleName(ValoTheme.LABEL_H1);
        numeroLbl.addStyleName(ValoTheme.LABEL_BOLD);
        numeroLbl.addStyleName(ValoTheme.LABEL_NO_MARGIN);
        numeroLbl.setSizeUndefined();


        layout.addComponents(numeroLbl);
        layout.setExpandRatio(numeroLbl, 1);
        layout.setComponentAlignment(numeroLbl, Alignment.MIDDLE_CENTER);

        panel.setContent(layout);
        return panel;
    }

    private Label createBooleanTitle(String title) {
        Label label = new Label(title);
        label.addStyleName(ValoTheme.LABEL_SMALL);
        label.addStyleName(ValoTheme.LABEL_BOLD);
        label.setSizeUndefined();
        return label;
    }

    private Label createBooleanValue(boolean value) {
        Label label = new Label(booleanText(value));
        label.addStyleName(ValoTheme.LABEL_BOLD);
        label.setSizeUndefined();
        return label;
    }

    private String formatDate(Date date) {
        return date != null ? Utileria.getFechaDDMMYYYY(date) : "";
    }

    private String booleanText(boolean value) {
        return value ? "Si" : "No";
    }

    private String emptyToDefault(String value, String defaultValue) {
        return value == null || value.trim().isEmpty() ? defaultValue : value.trim();
    }

    private boolean saveNewAbsence(UI mainUI,
                                   String idEmpleado,
                                   String cargo,
                                   String tipo,
                                   Date inicio,
                                   Date fin,
                                   double dias,
                                   String boleta,
                                   boolean medioInicio,
                                   boolean medioFin,
                                   boolean sabados) {

        if (dias <= 0) {
            Notification.show("Por favor asegúrese de que si tenga dias de ausencia.", Notification.Type.WARNING_MESSAGE);
            return false;
        }

        Connection connection = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection();
        boolean autoCommit = true;

        try {
            autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            insertEmpleadoAusencia(connection, mainUI, idEmpleado, tipo, inicio, fin, dias, boleta, medioInicio, medioFin, sabados);
            AusenciaConfig config = getAusenciaConfig(connection, tipo);
            saveOrUpdateEmpleadoAsistencia(connection, mainUI, idEmpleado, cargo, tipo, inicio, fin, medioInicio, medioFin, config);

            connection.commit();
        } catch (SQLException ex) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            Notification notif = new Notification("HA OCURRIDO UN ERROR DE BASE DE DATOS : " + ex.getMessage(), Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.WARNING);
            notif.show(Page.getCurrent());
            ex.printStackTrace();
            return false;
        } finally {
            try {
                connection.setAutoCommit(autoCommit);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        return true;
    }

    private void insertEmpleadoAusencia(Connection connection,
                                        UI mainUI,
                                        String idEmpleado,
                                        String tipo,
                                        Date inicio,
                                        Date fin,
                                        double dias,
                                        String boleta,
                                        boolean medioInicio,
                                        boolean medioFin,
                                        boolean sabados) throws SQLException {
        String sql = "INSERT INTO empleado_ausencia "
                + "(Tipo, IdEmpleado, FechaInicio, FechaFin, Dias, CreadoUsuario, "
                + "MedioDiaMañana, MedioDiaTarde, Sabados, Boleta) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, tipo);
            ps.setString(2, idEmpleado);
            ps.setString(3, Utileria.getFechaYYYYMMDD_1(inicio));
            ps.setString(4, Utileria.getFechaYYYYMMDD_1(fin));
            ps.setDouble(5, dias);
            ps.setString(6, ((SopdiUI) mainUI).sessionInformation.getStrUserId());
            ps.setInt(7, medioInicio ? 1 : 0);
            ps.setInt(8, medioFin ? 1 : 0);
            ps.setInt(9, sabados ? 1 : 0);
            ps.setString(10, boleta == null ? "" : boleta);
            ps.executeUpdate();
        }
    }

    private AusenciaConfig getAusenciaConfig(Connection connection, String tipo) throws SQLException {
        String sql = "SELECT EsDescuento, EsDescuentoEnPlanilla, Prioridad "
                + "FROM razon_ausencia WHERE Razon = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, tipo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new AusenciaConfig(
                            rs.getInt("EsDescuento"),
                            rs.getInt("EsDescuentoEnPlanilla"),
                            rs.getInt("Prioridad")
                    );
                }
            }
        }

        return new AusenciaConfig(0, 0, 0);
    }

    private void saveOrUpdateEmpleadoAsistencia(Connection connection,
                                                UI mainUI,
                                                String idEmpleado,
                                                String cargo,
                                                String tipo,
                                                Date inicio,
                                                Date fin,
                                                boolean medioInicio,
                                                boolean medioFin,
                                                AusenciaConfig config) throws SQLException {
        Date fechaTemp = inicio;
        while (!fechaTemp.after(fin)) {
            double tiempoDia = getTiempoDia(fechaTemp, inicio, fin, medioInicio, medioFin);
            if (tiempoDia > 0) {
                AsistenciaExistente existente = getAsistenciaExistente(connection, idEmpleado, fechaTemp);
                if (existente == null) {
                    insertEmpleadoAsistencia(connection, mainUI, idEmpleado, cargo, tipo, fechaTemp, tiempoDia, config);
                } else if (puedeActualizarAsistencia(existente, config)) {
                    updateEmpleadoAsistencia(connection, mainUI, existente.id, cargo, tipo, tiempoDia, config);
                }
            }
            fechaTemp = Utileria.getTomorrow(fechaTemp);
        }
    }

    private double getTiempoDia(Date fecha, Date inicio, Date fin, boolean medioInicio, boolean medioFin) {
        double tiempoDia = 1;
        if (fecha.equals(inicio) && medioInicio) tiempoDia -= 0.5;
        if (fecha.equals(fin) && medioFin) tiempoDia -= 0.5;
        return tiempoDia;
    }

    private AsistenciaExistente getAsistenciaExistente(Connection connection,
                                                       String idEmpleado,
                                                       Date fecha) throws SQLException {
        String sql = "SELECT ea.Id, ea.Estatus, ea.Razon, ea.EsDefinitiva, COALESCE(ra.Prioridad, 0) Prioridad "
                + "FROM empleado_asistencia ea "
                + "LEFT JOIN razon_ausencia ra ON ea.Razon = ra.Razon "
                + "WHERE ea.IdEmpleado = ? AND ea.Fecha = ? "
                + "ORDER BY ea.EsDefinitiva DESC, COALESCE(ra.Prioridad, 0) DESC, ea.Id DESC "
                + "LIMIT 1";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, idEmpleado);
            ps.setString(2, Utileria.getFechaYYYYMMDD_1(fecha));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new AsistenciaExistente(
                            rs.getInt("Id"),
                            rs.getString("Estatus"),
                            rs.getString("Razon"),
                            rs.getInt("EsDefinitiva") == 1,
                            rs.getInt("Prioridad")
                    );
                }
            }
        }

        return null;
    }

    private boolean puedeActualizarAsistencia(AsistenciaExistente existente, AusenciaConfig config) {
        if (existente.esDefinitiva) {
            return false;
        }

        String estatus = existente.estatus == null ? "" : existente.estatus.trim();
        String razon = existente.razon == null ? "" : existente.razon.trim();

        return razon.isEmpty()
                || "PRESENTE".equalsIgnoreCase(estatus)
                || existente.prioridad < config.prioridad;
    }

    private void insertEmpleadoAsistencia(Connection connection,
                                          UI mainUI,
                                          String idEmpleado,
                                          String cargo,
                                          String tipo,
                                          Date fecha,
                                          double tiempoDia,
                                          AusenciaConfig config) throws SQLException {
        String sql = "INSERT INTO empleado_asistencia "
                + "(IdEmpleado, Cargo, Fecha, HorasExtra, HorasExtraDoble, Estatus, Razon, DiasVacaciones, "
                + "EsDefinitiva, EsDescuento, EsAusenteSinGoceDeSueldo, CreadoFechaYHora, CreadoIdUsuario) "
                + "VALUES (?, ?, ?, 0, 0, 'AUSENTE', ?, ?, 0, ?, ?, current_timestamp, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, idEmpleado);
            ps.setString(2, cargo);
            ps.setString(3, Utileria.getFechaYYYYMMDD_1(fecha));
            ps.setString(4, tipo);
            ps.setDouble(5, "Vacaciones".equals(tipo) ? tiempoDia : 0);
            ps.setInt(6, config.esDescuento);
            ps.setInt(7, config.esDescuentoPlanilla);
            ps.setString(8, ((SopdiUI) mainUI).sessionInformation.getStrUserId());
            ps.executeUpdate();
        }
    }

    private void updateEmpleadoAsistencia(Connection connection,
                                          UI mainUI,
                                          int idAsistencia,
                                          String cargo,
                                          String tipo,
                                          double tiempoDia,
                                          AusenciaConfig config) throws SQLException {
        String sql = "UPDATE empleado_asistencia SET "
                + "Cargo = ?, HorasExtra = 0, HorasExtraDoble = 0, Estatus = 'AUSENTE', Razon = ?, "
                + "DiasVacaciones = ?, EsDescuento = ?, EsAusenteSinGoceDeSueldo = ?, CreadoFechaYHora = current_timestamp, "
                + "CreadoIdUsuario = ? "
                + "WHERE Id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, cargo);
            ps.setString(2, tipo);
            ps.setDouble(3, "Vacaciones".equals(tipo) ? tiempoDia : 0);
            ps.setInt(4, config.esDescuento);
            ps.setInt(5, config.esDescuentoPlanilla);
            ps.setString(6, ((SopdiUI) mainUI).sessionInformation.getStrUserId());
            ps.setInt(7, idAsistencia);
            ps.executeUpdate();
        }
    }

    private static class AusenciaConfig {
        final int esDescuento;
        final int esDescuentoPlanilla;
        final int prioridad;

        AusenciaConfig(int esDescuento, int esDescuentoPlanilla, int prioridad) {
            this.esDescuento = esDescuento;
            this.esDescuentoPlanilla = esDescuentoPlanilla;
            this.prioridad = prioridad;
        }
    }

    private static class AsistenciaExistente {
        final int id;
        final String estatus;
        final String razon;
        final boolean esDefinitiva;
        final int prioridad;

        AsistenciaExistente(int id, String estatus, String razon, boolean esDefinitiva, int prioridad) {
            this.id = id;
            this.estatus = estatus;
            this.razon = razon;
            this.esDefinitiva = esDefinitiva;
            this.prioridad = prioridad;
        }
    }
}
