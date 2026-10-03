package com.simpletecno.sopdi.compras;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;

/**
 * Ventana para seleccionar la factura compra vigente que será abonada por una
 * NOTA DE CREDITO COMPRA.
 *
 * El saldo vigente se determina por SUM(Haber-Debe) > 0 para el par
 * CodigoCC + IdNomenclatura de la línea HABER del proveedor.
 *
 * Expone:
 *  - CodigoCC      : CodigoCC de la línea HABER proveedor → para el DEBE proveedor de la NC
 *  - CodigoPartida : CodigoPartida compartida por todas las líneas → para localizar IVA y costo
 */
public class SeleccionarFacturaNotaCreditoWindow extends Window {

    private static final DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");

    static final String FECHA_P          = "Fecha";
    static final String SERIE_P          = "Serie";
    static final String NUMERO_P         = "Número";
    static final String MONTO_P          = "Monto";
    static final String NOC_P            = "OC";
    static final String CENTRO_COSTO_P   = "C.Costo";
    static final String CODIGO_CC_P      = "_CodigoCC";       // CodigoCC línea HABER proveedor
    static final String CODIGO_PARTIDA_P = "_CodigoPartida";  // CodigoPartida (igual en todas las líneas)

    private final IndexedContainer container = new IndexedContainer();
    private final Grid grid = new Grid("Facturas del proveedor con saldo pendiente", container);

    private Button aplicarBtn;

    public SeleccionarFacturaNotaCreditoWindow(UI mainUI, String empresaId,
                                               String idProveedor, String moneda) {
        super("Seleccione la factura que será abonada por la nota de crédito");
        addCloseShortcut(com.vaadin.event.ShortcutAction.KeyCode.ESCAPE, null);
        setModal(true);
        setResizable(true);
        setWidth("72%");
        setHeight("60%");

        buildContainer();
        buildGrid();
        llenarGrid(mainUI, empresaId, idProveedor, moneda);
        buildLayout();
    }

    private void buildContainer() {
        container.addContainerProperty(FECHA_P,          String.class, "");
        container.addContainerProperty(SERIE_P,          String.class, "");
        container.addContainerProperty(NUMERO_P,         String.class, "");
        container.addContainerProperty(MONTO_P,          String.class, "0.00");
        container.addContainerProperty(NOC_P,            String.class, "");
        container.addContainerProperty(CENTRO_COSTO_P,   String.class, "");
        container.addContainerProperty(CODIGO_CC_P,      String.class, "");
        container.addContainerProperty(CODIGO_PARTIDA_P, String.class, "");
    }

    private void buildGrid() {
        grid.setWidth("100%");
        grid.setImmediate(true);
        grid.setHeightMode(HeightMode.ROW);
        grid.setHeightByRows(7);
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);

        grid.getColumn(CODIGO_CC_P).setHidden(true);
        grid.getColumn(CODIGO_PARTIDA_P).setHidden(true);

        grid.getColumn(FECHA_P).setWidth(105);
        grid.getColumn(SERIE_P).setWidth(90);
        grid.getColumn(NUMERO_P).setWidth(90);
        grid.getColumn(MONTO_P).setWidth(120);
        grid.getColumn(NOC_P).setWidth(80);
        grid.getColumn(CENTRO_COSTO_P).setExpandRatio(1);

        grid.setCellStyleGenerator((Grid.CellReference cr) ->
            MONTO_P.equals(cr.getPropertyId()) ? "rightalign" : null);
    }

    private void llenarGrid(UI mainUI, String empresaId, String idProveedor, String moneda) {
        // La línea HABER (proveedor) puede tener CodigoCC distinto al CodigoPartida.
        // El CodigoCentroCosto visible se toma de la línea de costo (DEBE, última por IdPartida).
        String q  = " SELECT cp.Fecha, cp.SerieDocumento, cp.NumeroDocumento, cp.MontoDocumento,";
               q += " cp.CodigoCC, cp.CodigoPartida, IFNULL(oc.NOC,'') AS NOC,";
               // Centro de costo de la línea de costo (DEBE), que es la de mayor IdPartida
               q += " (SELECT cp3.CodigoCentroCosto FROM contabilidad_partida cp3";
               q += "  WHERE cp3.CodigoPartida = cp.CodigoPartida AND cp3.IdEmpresa = cp.IdEmpresa";
               q += "  AND cp3.Debe > 0 AND cp3.Estatus NOT IN ('ANULADO','ANULADA')";
               q += "  ORDER BY cp3.IdPartida DESC LIMIT 1) AS CodigoCentroCostoGasto";
               q += " FROM contabilidad_partida cp";
               q += " LEFT JOIN orden_compra oc ON oc.Id = cp.IdOrdenCompra AND oc.IdEmpresa = cp.IdEmpresa";
               q += " WHERE cp.IdEmpresa = " + empresaId;
               q += " AND cp.IdProveedor = " + idProveedor;
               q += " AND cp.TipoDocumento = 'FACTURA'";
               q += " AND cp.MonedaDocumento = '" + moneda + "'";
               q += " AND cp.Haber > 0";
               q += " AND cp.Estatus NOT IN ('ANULADO','ANULADA')";
               q += " AND (SELECT SUM(cp2.Haber - cp2.Debe)";
               q += "      FROM contabilidad_partida cp2";
               q += "      WHERE cp2.IdEmpresa = cp.IdEmpresa";
               q += "      AND cp2.CodigoCC = cp.CodigoCC";
               q += "      AND cp2.IdNomenclatura = cp.IdNomenclatura";
               q += "      AND cp2.Estatus NOT IN ('ANULADO','ANULADA')) > 0";
               q += " ORDER BY cp.Fecha DESC";

        try {
            Statement st = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            ResultSet rs = st.executeQuery(q);
            while (rs.next()) {
                Object id = container.addItem();
                container.getContainerProperty(id, FECHA_P).setValue(rs.getString("Fecha"));
                container.getContainerProperty(id, SERIE_P).setValue(rs.getString("SerieDocumento"));
                container.getContainerProperty(id, NUMERO_P).setValue(rs.getString("NumeroDocumento"));
                container.getContainerProperty(id, MONTO_P).setValue(numberFormat.format(rs.getDouble("MontoDocumento")));
                container.getContainerProperty(id, NOC_P).setValue(rs.getString("NOC"));
                String cc = rs.getString("CodigoCentroCostoGasto");
                container.getContainerProperty(id, CENTRO_COSTO_P).setValue(cc != null ? cc : "");
                container.getContainerProperty(id, CODIGO_CC_P).setValue(rs.getString("CodigoCC"));
                container.getContainerProperty(id, CODIGO_PARTIDA_P).setValue(rs.getString("CodigoPartida"));
            }
            if (container.size() == 0) {
                Notification.show("No se encontraron facturas vigentes de este proveedor en " + moneda + ".",
                        Notification.Type.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            Notification.show("Error al cargar facturas vigentes: " + ex.getMessage(),
                    Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void buildLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setSpacing(true);
        layout.setMargin(true);

        Label info = new Label("Seleccione la factura de compra que será abonada con esta nota de crédito. "
                + "La partida contable usará el CodigoCC y Centro de Costo de esa factura.");
        layout.addComponent(info);
        layout.addComponent(grid);
        layout.setExpandRatio(grid, 1f);

        aplicarBtn = new Button("Aplicar nota de crédito");
        aplicarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        aplicarBtn.setIcon(FontAwesome.CHECK);
        aplicarBtn.setEnabled(false);

        Button cancelarBtn = new Button("Cancelar");
        cancelarBtn.setIcon(FontAwesome.TIMES);
        cancelarBtn.addClickListener(e -> close());

        grid.addSelectionListener(event -> aplicarBtn.setEnabled(grid.getSelectedRow() != null));

        HorizontalLayout buttons = new HorizontalLayout(cancelarBtn, aplicarBtn);
        buttons.setSpacing(true);
        layout.addComponent(buttons);
        layout.setComponentAlignment(buttons, Alignment.BOTTOM_RIGHT);

        setContent(layout);
    }

    public Button getAplicarBtn() { return aplicarBtn; }

    /** CodigoCC de la línea HABER proveedor — para el DEBE proveedor de la NC */
    public String getSelectedCodigoCC() {
        Object row = grid.getSelectedRow();
        return row == null ? "" : String.valueOf(container.getContainerProperty(row, CODIGO_CC_P).getValue());
    }

    /** CodigoPartida compartida por todas las líneas — para localizar IVA y costo */
    public String getSelectedCodigoPartida() {
        Object row = grid.getSelectedRow();
        return row == null ? "" : String.valueOf(container.getContainerProperty(row, CODIGO_PARTIDA_P).getValue());
    }
}
