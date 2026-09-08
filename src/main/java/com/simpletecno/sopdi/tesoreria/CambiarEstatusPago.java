package com.simpletecno.sopdi.tesoreria;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Runo;
import com.vaadin.ui.themes.ValoTheme;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.vaadin.dialogs.ConfirmDialog;

public class CambiarEstatusPago extends Window {

    String queryString;
    FormLayout mainForm;
    ComboBox estatusCbx;
    TextField numeroTxt;

    Button guardarBtn;
    Statement stQuery;
    Statement stQuery2;

    UI mainUI;

    IndexedContainer container;
    Object itemId;
    String codigoPartida;
    String monto;
    String idNomenclatura;

    public CambiarEstatusPago(
            IndexedContainer container,
            Object itemId,
            String codigoPartida,
            String monto,
            String idNomenclatura) {

        this.container       = container;
        this.itemId          = itemId;
        this.codigoPartida   = codigoPartida;
        this.idNomenclatura  = idNomenclatura;
        this.mainUI          = UI.getCurrent();
        setResponsive(true);

        MarginInfo marginInfo = new MarginInfo(false, true, true, true);

        mainForm = new FormLayout();
        mainForm.setMargin(marginInfo);
        mainForm.setSpacing(true);

        numeroTxt = new TextField("# Documento : ");
        numeroTxt.setValue(String.valueOf(container.getItem(itemId).getItemProperty(HistorialPagoView.DOCUMENTO_PROPERTY).getValue()));
        numeroTxt.setWidth("8em");

        estatusCbx = new ComboBox("Estatus :");
        estatusCbx.addStyleName(ValoTheme.COMBOBOX_HUGE);
        estatusCbx.setInvalidAllowed(false);
        estatusCbx.setNewItemsAllowed(false);
        estatusCbx.setWidth("14em");
        estatusCbx.addItem("CONCILIADO");
        estatusCbx.addItem("INGRESADO");
        estatusCbx.addItem("ANULADO");
        estatusCbx.select(String.valueOf(container.getItem(itemId).getItemProperty(HistorialPagoView.ESTATUS_PROPERTY).getValue()));

        guardarBtn = new Button("Cambiar datos");
        guardarBtn.setIcon(FontAwesome.SAVE);
        guardarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        guardarBtn.addClickListener(event ->
            ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de CAMBIAR los datos de este cheque?",
                "SI", "NO", dialog -> {
                    if (dialog.isConfirmed()) {
                        cambiarEstatus();
                    }
                })
        );

        Button salirBtn = new Button("Salir");
        salirBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        salirBtn.setIcon(FontAwesome.ARROW_RIGHT);
        salirBtn.addClickListener(event -> close());

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.addComponent(salirBtn);
        buttonsLayout.setComponentAlignment(salirBtn, Alignment.BOTTOM_LEFT);
        buttonsLayout.addComponent(guardarBtn);
        buttonsLayout.setComponentAlignment(guardarBtn, Alignment.BOTTOM_CENTER);

        mainForm.addComponent(estatusCbx);
        mainForm.addComponent(numeroTxt);
        mainForm.addComponent(buttonsLayout);

        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setSizeUndefined();

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setWidth("100%");
        titleLayout.setMargin(true);

        Label titleLbl = new Label("Cambio de estatus/documento");
        titleLbl.addStyleName(Runo.LABEL_H2);
        titleLbl.setSizeUndefined();

        titleLayout.addComponent(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.TOP_CENTER);

        contentLayout.addComponent(titleLayout);
        contentLayout.setComponentAlignment(titleLayout, Alignment.TOP_CENTER);
        contentLayout.addComponent(mainForm);
        contentLayout.setComponentAlignment(mainForm, Alignment.TOP_CENTER);

        setContent(contentLayout);
    }

    private void cambiarEstatus() {
        if ("ANULADO".equals(String.valueOf(estatusCbx.getValue()))) {
            ConfirmDialog.show(UI.getCurrent(),
                "Trasladar cheque:",
                "¿Desea trasladar la partida al siguiente # de cheque disponible en la misma cuenta bancaria?",
                "SI — Trasladar", "NO — Solo anular",
                dialog -> {
                    if (dialog.isConfirmed()) {
                        anularYTrasladar();
                    } else {
                        soloAnular();
                    }
                });
        } else {
            actualizarPartida();
        }
    }

    // Cambia estatus/número sin clonar
    private void actualizarPartida() {
        try {
            queryString  = " UPDATE contabilidad_partida SET ";
            queryString += " Estatus = '" + estatusCbx.getValue() + "'";
            queryString += ",NumeroDocumento = '" + numeroTxt.getValue() + "'";
            queryString += " WHERE CodigoPartida = '" + codigoPartida + "'";

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            container.getItem(itemId).getItemProperty(HistorialPagoView.ESTATUS_PROPERTY).setValue(String.valueOf(estatusCbx.getValue()));
            container.getItem(itemId).getItemProperty(HistorialPagoView.DOCUMENTO_PROPERTY).setValue(numeroTxt.getValue());
            close();
        } catch (SQLException ex) {
            Notification.show("ERROR al actualizar: " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    // Anula sin trasladar
    private void soloAnular() {
        try {
            queryString  = " UPDATE contabilidad_partida SET Estatus = 'ANULADO'";
            queryString += " WHERE CodigoPartida = '" + codigoPartida + "'";

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(queryString);

            container.getItem(itemId).getItemProperty(HistorialPagoView.ESTATUS_PROPERTY).setValue("ANULADO");
            Notification.show("Cheque anulado.", Notification.Type.HUMANIZED_MESSAGE);
            close();
        } catch (SQLException ex) {
            Notification.show("ERROR al anular: " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    // Anula y clona la partida con el siguiente # de cheque de la chequera
    private void anularYTrasladar() {
        try {
            String empresaId = ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();

            // 1. Buscar la chequera activa para esta cuenta bancaria
            queryString  = " SELECT ch.IdChequera, ch.UltimoUtilizado, ch.Al";
            queryString += " FROM contabilidad_cuentas_bancos_chequera ch";
            queryString += " INNER JOIN contabilidad_cuentas_bancos ccb ON ccb.IdCuentaBanco = ch.IdCuentaBanco";
            queryString += " WHERE ccb.IdNomenclatura = " + idNomenclatura;
            queryString += " AND ccb.IdEmpresa = " + empresaId;
            queryString += " AND ch.UltimoUtilizado < ch.Al";
            queryString += " ORDER BY ch.IdChequera ASC LIMIT 1";

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            ResultSet rs = stQuery.executeQuery(queryString);

            if (!rs.next()) {
                Notification.show("No hay chequera disponible para esta cuenta. Se anulará sin trasladar.",
                        Notification.Type.WARNING_MESSAGE);
                soloAnular();
                return;
            }

            int idChequera       = rs.getInt("IdChequera");
            int ultimoUtilizado  = rs.getInt("UltimoUtilizado");
            int nuevoNumeroCheque = ultimoUtilizado + 1;

            // 2. Generar nuevo CodigoPartida (mismo esquema: empresaId+YYYYMMDD+1+NNN)
            String nuevoCodigo = generarNuevoCodigoPartida(empresaId);

            // 3. Clonar todas las filas de la partida con el nuevo código y nuevo # de cheque
            queryString  = " INSERT INTO contabilidad_partida";
            queryString += " (IdEmpresa, Estatus, CodigoPartida, CodigoCC, Fecha, Descripcion,";
            queryString += "  IdProveedor, NITProveedor, NombreProveedor, NombreCheque, NumeroDocumento,";
            queryString += "  SerieDocumento, TipoDocumento, IdNomenclatura, MonedaDocumento, MontoDocumento,";
            queryString += "  Debe, Haber, TipoCambio, DebeQuetzales, HaberQuetzales, Saldo,";
            queryString += "  MontoAutorizadoPagar, MontoAplicarAnticipo, IdLiquidacion, IdLiquidador,";
            queryString += "  TipoEnganche, TipoVenta, TipoDOCA, NoDOCA, Referencia,";
            queryString += "  CreadoFechaYHora, CreadoUsuario, Año, PagadoIva, IdOrdenCompra,";
            queryString += "  IdConciliacion, IdCentroCosto, CodigoCentroCosto, IdProducto, EsCuota,";
            queryString += "  SueldoOrdinario, FechaYHoraCreado, CreadoPor)";
            queryString += " SELECT";
            queryString += "  IdEmpresa, 'INGRESADO', '" + nuevoCodigo + "',";
            // La línea de bancos tiene CodigoCC = CodigoPartida; las de gasto tienen su propio CodigoCC → respetar
            queryString += "  CASE WHEN CodigoCC = '" + codigoPartida + "' THEN '" + nuevoCodigo + "' ELSE CodigoCC END,";
            queryString += "  Fecha, Descripcion,";
            queryString += "  IdProveedor, NITProveedor, NombreProveedor, NombreCheque, '" + nuevoNumeroCheque + "',";
            queryString += "  SerieDocumento, TipoDocumento, IdNomenclatura, MonedaDocumento, MontoDocumento,";
            queryString += "  Debe, Haber, TipoCambio, DebeQuetzales, HaberQuetzales, Saldo,";
            queryString += "  MontoAutorizadoPagar, MontoAplicarAnticipo, IdLiquidacion, IdLiquidador,";
            queryString += "  TipoEnganche, TipoVenta, TipoDOCA, NoDOCA, Referencia,";
            queryString += "  NOW(), CreadoUsuario, Año, PagadoIva, IdOrdenCompra,";
            queryString += "  IdConciliacion, IdCentroCosto, CodigoCentroCosto, IdProducto, EsCuota,";
            queryString += "  SueldoOrdinario, NOW(), CreadoPor";
            queryString += " FROM contabilidad_partida";
            queryString += " WHERE CodigoPartida = '" + codigoPartida + "'";

            stQuery2 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            stQuery2.executeUpdate(queryString);

            // 4. Actualizar UltimoUtilizado en la chequera
            queryString  = " UPDATE contabilidad_cuentas_bancos_chequera";
            queryString += " SET UltimoUtilizado = " + nuevoNumeroCheque;
            queryString += " WHERE IdChequera = " + idChequera;
            stQuery.executeUpdate(queryString);

            // 5. Anular la partida original
            queryString  = " UPDATE contabilidad_partida SET Estatus = 'ANULADO'";
            queryString += " WHERE CodigoPartida = '" + codigoPartida + "'";
            stQuery.executeUpdate(queryString);

            container.getItem(itemId).getItemProperty(HistorialPagoView.ESTATUS_PROPERTY).setValue("ANULADO");

            Notification.show("Cheque anulado. Partida trasladada al cheque # " + nuevoNumeroCheque
                    + "  (Partida: " + nuevoCodigo + ")", Notification.Type.HUMANIZED_MESSAGE);
            close();

        } catch (Exception ex) {
            Notification.show("ERROR al trasladar cheque: " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private String generarNuevoCodigoPartida(String empresaId) throws SQLException {
        String fecha  = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String prefijo = empresaId + fecha + "1";

        queryString  = " SELECT CodigoPartida FROM contabilidad_partida";
        queryString += " WHERE CodigoPartida LIKE '" + prefijo + "%'";
        queryString += " ORDER BY CodigoPartida DESC LIMIT 1";

        stQuery2 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
        ResultSet rs = stQuery2.executeQuery(queryString);

        if (rs.next()) {
            String ultimo = rs.getString("CodigoPartida");
            int secuencia = Integer.parseInt(ultimo.substring(12, 15)) + 1;
            return prefijo + String.format("%03d", secuencia);
        }
        return prefijo + "001";
    }
}