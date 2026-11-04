package com.simpletecno.sopdi.tesoreria;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.Runo;
import com.vaadin.ui.themes.ValoTheme;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vaadin.dialogs.ConfirmDialog;

public class CambiarEstatusPago extends Window {
    private final UI mainUI;
    private final IndexedContainer container;
    private final Object itemId;
    private final String codigoPartida;
    private final String idNomenclatura;
    private final String empresaId;
    private final boolean esCheque;
    private final ComboBox estatusCbx = new ComboBox("Estatus :");
    private final TextField numeroTxt = new TextField("# Documento :");
    private final Button guardarBtn = new Button("Cambiar datos");

    public CambiarEstatusPago(IndexedContainer container, Object itemId, String codigoPartida,
            String monto, String idNomenclatura) {
        this.container = container;
        this.itemId = itemId;
        this.codigoPartida = codigoPartida;
        this.idNomenclatura = idNomenclatura;
        this.mainUI = UI.getCurrent();
        this.empresaId = ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();
        this.esCheque = "CHEQUE".equalsIgnoreCase(String.valueOf(
                container.getContainerProperty(itemId, HistorialPagoView.TIPO_PROPERTY).getValue()));
        setModal(true);
        setResponsive(true);
        addCloseShortcut(com.vaadin.event.ShortcutAction.KeyCode.ESCAPE, null);

        numeroTxt.setValue(String.valueOf(container.getContainerProperty(
                itemId, HistorialPagoView.DOCUMENTO_PROPERTY).getValue()));
        numeroTxt.setWidth("8em");
        numeroTxt.setReadOnly(esCheque);
        if (esCheque) numeroTxt.setDescription("Para cambiar el numero, anule y traslade el cheque.");
        estatusCbx.addStyleName(ValoTheme.COMBOBOX_HUGE);
        estatusCbx.setInvalidAllowed(false);
        estatusCbx.setNewItemsAllowed(false);
        estatusCbx.setWidth("14em");
        estatusCbx.addItems("CONCILIADO", "INGRESADO", "ANULADO");
        estatusCbx.select(String.valueOf(container.getContainerProperty(
                itemId, HistorialPagoView.ESTATUS_PROPERTY).getValue()));
        guardarBtn.setIcon(FontAwesome.SAVE);
        guardarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        guardarBtn.addClickListener(e -> ConfirmDialog.show(mainUI, "Confirme:",
                "Esta seguro de cambiar los datos de este documento?", "SI", "NO", dialog -> {
                    if (dialog.isConfirmed()) cambiarEstatus();
                }));
        Button salir = new Button("Salir", FontAwesome.ARROW_RIGHT);
        salir.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        salir.addClickListener(e -> close());
        HorizontalLayout botones = new HorizontalLayout(salir, guardarBtn);
        botones.setSpacing(true);
        FormLayout formulario = new FormLayout(estatusCbx, numeroTxt, botones);
        formulario.setMargin(new MarginInfo(false, true, true, true));
        Label titulo = new Label("Cambio de estatus/documento");
        titulo.addStyleName(Runo.LABEL_H2);
        VerticalLayout contenido = new VerticalLayout(titulo, formulario);
        contenido.setSizeUndefined();
        setContent(contenido);
    }

    private Connection conexion() {
        return ((SopdiUI) mainUI).databaseProvider.getCurrentConnection();
    }

    private void cambiarEstatus() {
        if (estatusCbx.getValue() == null) {
            Notification.show("Seleccione un estatus.", Notification.Type.WARNING_MESSAGE);
            return;
        }
        if (esCheque && "ANULADO".equals(estatusCbx.getValue())) {
            ConfirmDialog.show(mainUI, "Trasladar cheque:",
                    "Desea trasladar la partida al siguiente cheque disponible en la misma cuenta?",
                    "SI — Trasladar", "NO — Solo anular", dialog -> {
                        if (dialog.isConfirmed()) pedirFechaReemision();
                        else actualizarPartida();
                    });
        } else actualizarPartida();
    }

    private void actualizarPartida() {
        String estatus = String.valueOf(estatusCbx.getValue());
        String sql = "UPDATE contabilidad_partida SET Estatus=?"
                + (esCheque ? "" : ",NumeroDocumento=?")
                + " WHERE IdEmpresa=? AND CodigoPartida=? AND Estatus <> 'ANULADO'";
        try (PreparedStatement ps = conexion().prepareStatement(sql)) {
            int parametro = 1;
            ps.setString(parametro++, estatus);
            if (!esCheque) ps.setString(parametro++, numeroTxt.getValue());
            ps.setString(parametro++, empresaId);
            ps.setString(parametro, codigoPartida);
            if (ps.executeUpdate() == 0) throw new SQLException("La partida ya fue anulada o no existe en esta empresa.");
            container.getContainerProperty(itemId, HistorialPagoView.ESTATUS_PROPERTY).setValue(estatus);
            if (!esCheque) container.getContainerProperty(itemId, HistorialPagoView.DOCUMENTO_PROPERTY).setValue(numeroTxt.getValue());
            if ("ANULADO".equals(estatus)) Notification.show("Cheque anulado. Su numero permanece consumido.");
            close();
        } catch (SQLException ex) {
            mostrarError("actualizar", ex);
        }
    }

    private void pedirFechaReemision() {
        Window popup = new Window("Fecha del cheque de reemplazo");
        popup.setModal(true);
        popup.setResizable(false);
        DateField fecha = new DateField("Fecha de cheque");
        fecha.setDateFormat("dd/MM/yyyy");
        fecha.setTimeZone(TimeZone.getTimeZone("America/Guatemala"));
        fecha.setValue(new Date());
        Button aceptar = new Button("Aceptar", FontAwesome.CHECK);
        aceptar.addStyleName(ValoTheme.BUTTON_PRIMARY);
        aceptar.addClickListener(e -> {
            if (fecha.getValue() == null) {
                Notification.show("Seleccione una fecha.", Notification.Type.WARNING_MESSAGE);
                return;
            }
            popup.close();
            anularYTrasladar(fecha.getValue());
        });
        Button cancelar = new Button("Cancelar", FontAwesome.TIMES);
        cancelar.addClickListener(e -> popup.close());
        HorizontalLayout botones = new HorizontalLayout(cancelar, aceptar);
        botones.setSpacing(true);
        VerticalLayout contenido = new VerticalLayout(fecha, botones);
        contenido.setMargin(true);
        contenido.setSpacing(true);
        popup.setContent(contenido);
        mainUI.addWindow(popup);
        popup.center();
    }

    private void anularYTrasladar(Date fecha) {
        guardarBtn.setEnabled(false);
        try {
            ReemisionChequeService.Resultado resultado = ReemisionChequeService.trasladar(
                    conexion(), empresaId, idNomenclatura, codigoPartida, fecha,
                    ((SopdiUI) mainUI).sessionInformation.getStrUserId());
            container.getContainerProperty(itemId, HistorialPagoView.ESTATUS_PROPERTY).setValue("ANULADO");
            Notification.show("Cheque anulado. Partida trasladada al cheque # " + resultado.numeroCheque
                    + " (Partida: " + resultado.codigoPartida + ").");
            close();
        } catch (Exception ex) {
            mostrarError("trasladar cheque", ex);
        } finally {
            guardarBtn.setEnabled(true);
        }
    }

    private void mostrarError(String operacion, Exception ex) {
        Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Error al " + operacion, ex);
        Notification.show("Error al " + operacion + ": " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
    }
}
