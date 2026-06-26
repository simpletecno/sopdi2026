package com.simpletecno.sopdi.tesoreria;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Page.Styles;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.themes.ValoTheme;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Vista de Cheques en Tesorería.
 *
 * <p>
 * Un cheque emitido a un proveedor o cliente tiene vigencia de cobro de 6 meses.
 * Los cheques que no han sido entregados/recogidos por el librado deben
 * registrarse como "Cheques en Tesorería": se hace una partida reversando
 * Bancos (DEBE) y abonando la cuenta Cheques en Tesorería (HABER).
 * </p>
 *
 * <p>
 * El primer grid muestra los cheques YA registrados en tesorería (lee
 * contabilidad_partida filtrando por la cuenta default "Cheques en Tesorería").
 * El segundo grid muestra los cheques candidatos (TipoDocumento='CHEQUE' sobre
 * las cuentas default de Bancos), con una columna inline de acción que genera la
 * partida contable. Toma como referencia el insert de {@code ChequeTesoreriaForm}.
 * </p>
 *
 * @author Sopdi
 */
public class ChequesEnTesoreriaView extends VerticalLayout implements View {

    UI mainUI;
    Statement stQuery;
    ResultSet rsRecords;
    String queryString;

    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    // Cuentas contables default (IdNomenclatura) ya cargadas en la sesión.
    String idChequesTesoreria = ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getChequesTesoreria();
    String idBancosLocal = ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaLocal();
    String idBancosExtranjera = ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getBancosMonedaExtranjera();

    // ---- Grid 1: cheques ya registrados en tesorería ----
    Grid registradosGrid;
    Grid.FooterRow registradosFooter;
    public IndexedContainer registradosContainer = new IndexedContainer();

    // ---- Grid 2: cheques candidatos a registrar ----
    Grid pendientesGrid;
    Grid.FooterRow pendientesFooter;
    public IndexedContainer pendientesContainer = new IndexedContainer();

    // Propiedades visibles (compartidas por ambos grids).
    static final String CHEQUE_PROPERTY = "No. Cheque";
    static final String ENTIDAD_PROPERTY = "Entidad (Proveedor/Cliente)";
    static final String MONTO_PROPERTY = "Monto";
    static final String MONEDA_PROPERTY = "Moneda";
    static final String FECHA_PROPERTY = "Fecha";
    static final String ANTIGUEDAD_PROPERTY = "Antigüedad";
    static final String ACCION_PROPERTY = "Acción";

    // Propiedades ocultas, necesarias para construir la partida de reverso.
    static final String IDPROVEEDOR_PROPERTY = "IdProveedor";
    static final String NOMBREPROV_PROPERTY = "NombreProveedor";
    static final String IDNOMENCLATURA_PROPERTY = "IdNomenclatura";
    static final String CODIGOCC_PROPERTY = "CodigoCC";
    static final String MONTOSF_PROPERTY = "MontoSinFormato";
    static final String MONTOQ_PROPERTY = "MontoQuetzales";
    static final String TIPOCAMBIO_PROPERTY = "TipoCambio";

    public ChequesEnTesoreriaView() {
        this.mainUI = UI.getCurrent();
        setWidth("100%");
        setSpacing(true);
        setMargin(true);

        // Estilo para resaltar cheques con más de 1 año de antigüedad.
        Styles styles = Page.getCurrent().getStyles();
        styles.add(".v-grid-row.vencido td { background-color: #ffe0e0; }");

        crearGridRegistrados();
        crearGridPendientes();

        llenarGridRegistrados();
        llenarGridPendientes();
    }

    // =====================================================================
    //  GRID 1 - Cheques en Tesorería ya registrados
    // =====================================================================
    private void crearGridRegistrados() {
        VerticalLayout layout = new VerticalLayout();
        layout.addStyleName("rcorners3");
        layout.setWidth("100%");
        layout.setSpacing(true);
        layout.setMargin(true);

        Label titulo = new Label("Cheques registrados en Tesorería");
        titulo.addStyleName(ValoTheme.LABEL_H3);

        registradosContainer.addContainerProperty(CHEQUE_PROPERTY, String.class, null);
        registradosContainer.addContainerProperty(ENTIDAD_PROPERTY, String.class, null);
        registradosContainer.addContainerProperty(MONTO_PROPERTY, String.class, null);
        registradosContainer.addContainerProperty(MONEDA_PROPERTY, String.class, null);
        registradosContainer.addContainerProperty(FECHA_PROPERTY, String.class, null);
        registradosContainer.addContainerProperty(ANTIGUEDAD_PROPERTY, String.class, null);
        registradosContainer.addContainerProperty(MONTOSF_PROPERTY, String.class, null);

        registradosGrid = new Grid(registradosContainer);
        registradosGrid.setWidth("100%");
        registradosGrid.setImmediate(true);
        registradosGrid.setSelectionMode(Grid.SelectionMode.NONE);
        registradosGrid.setHeightMode(HeightMode.ROW);
        registradosGrid.setHeightByRows(5);
        registradosGrid.setResponsive(true);

        registradosGrid.getColumn(MONTOSF_PROPERTY).setHidable(true).setHidden(true);
        registradosGrid.getColumn(ENTIDAD_PROPERTY).setExpandRatio(3);
        registradosGrid.getColumn(MONTO_PROPERTY).setExpandRatio(1);
        registradosGrid.setCellStyleGenerator(cellRef
                -> MONTO_PROPERTY.equals(cellRef.getPropertyId()) ? "rightalign" : null);

        registradosFooter = registradosGrid.appendFooterRow();
        registradosFooter.getCell(MONEDA_PROPERTY).setText("0 CHEQUES");
        registradosFooter.getCell(MONTO_PROPERTY).setText("0.00");
        registradosFooter.getCell(MONTO_PROPERTY).setStyleName("rightalign");

        agregarFiltro(registradosGrid, registradosContainer, CHEQUE_PROPERTY);
        agregarFiltro(registradosGrid, registradosContainer, ENTIDAD_PROPERTY);

        layout.addComponents(titulo, registradosGrid);
        addComponent(layout);
    }

    private void llenarGridRegistrados() {
        registradosContainer.removeAllItems();

        if (idChequesTesoreria == null || idChequesTesoreria.trim().isEmpty()) {
            Notification.show("No está configurada la cuenta default 'Cheques en Tesorería'.",
                    Notification.Type.WARNING_MESSAGE);
            return;
        }

        queryString = " SELECT * FROM contabilidad_partida";
        queryString += " WHERE IdEmpresa = " + empresaId;
        queryString += " AND IdNomenclatura = " + idChequesTesoreria;
        queryString += " AND Estatus <> 'ANULADO'";
        queryString += " AND Haber > 0";
        queryString += " ORDER BY Fecha DESC";

        double total = 0.00;
        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) {
                Object itemId = registradosContainer.addItem();
                double monto = rsRecords.getDouble("Haber");

                registradosContainer.getContainerProperty(itemId, CHEQUE_PROPERTY).setValue(rsRecords.getString("NumeroDocumento"));
                registradosContainer.getContainerProperty(itemId, ENTIDAD_PROPERTY).setValue(rsRecords.getString("IdProveedor") + " " + rsRecords.getString("NombreProveedor"));
                registradosContainer.getContainerProperty(itemId, MONTO_PROPERTY).setValue(numberFormat.format(monto));
                registradosContainer.getContainerProperty(itemId, MONEDA_PROPERTY).setValue(rsRecords.getString("MonedaDocumento"));
                registradosContainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha")));
                registradosContainer.getContainerProperty(itemId, ANTIGUEDAD_PROPERTY).setValue(calcularAntiguedad(rsRecords.getDate("Fecha")));
                registradosContainer.getContainerProperty(itemId, MONTOSF_PROPERTY).setValue(String.valueOf(monto));

                total += monto;
            }
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Error al listar cheques en tesorería: " + ex.getMessage());
            ex.printStackTrace();
        }

        registradosFooter.getCell(MONEDA_PROPERTY).setText(registradosContainer.size() + " CHEQUES");
        registradosFooter.getCell(MONTO_PROPERTY).setText(numberFormat.format(total));
    }

    // =====================================================================
    //  GRID 2 - Cheques candidatos a registrar en Tesorería
    // =====================================================================
    private void crearGridPendientes() {
        VerticalLayout layout = new VerticalLayout();
        layout.addStyleName("rcorners3");
        layout.setWidth("100%");
        layout.setSpacing(true);
        layout.setMargin(true);

        HorizontalLayout tituloLayout = new HorizontalLayout();
        tituloLayout.setWidth("100%");
        tituloLayout.setSpacing(true);

        Label titulo = new Label("Cheques pendientes de registrar (no cobrados con fecha > 6 meses)");
        titulo.addStyleName(ValoTheme.LABEL_H3);

        Button refrescarBtn = new Button("Refrescar");
        refrescarBtn.setIcon(FontAwesome.REFRESH);
        refrescarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        refrescarBtn.addClickListener(e -> {
            llenarGridRegistrados();
            llenarGridPendientes();
        });

        Button pdfBtn = new Button("Generar PDF");
        pdfBtn.setIcon(FontAwesome.FILE_PDF_O);
        pdfBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        pdfBtn.setDescription("Genera el reporte PDF con ambos detalles.");
        pdfBtn.addClickListener(e -> {
            if (registradosContainer.size() == 0 && pendientesContainer.size() == 0) {
                Notification.show("No hay registros para imprimir.", Notification.Type.WARNING_MESSAGE);
            } else {
                printPdf();
            }
        });

        tituloLayout.addComponents(titulo, refrescarBtn, pdfBtn);
        tituloLayout.setComponentAlignment(titulo, Alignment.MIDDLE_LEFT);
        tituloLayout.setComponentAlignment(refrescarBtn, Alignment.MIDDLE_RIGHT);
        tituloLayout.setComponentAlignment(pdfBtn, Alignment.MIDDLE_RIGHT);
        tituloLayout.setExpandRatio(titulo, 1);

        pendientesContainer.addContainerProperty(CHEQUE_PROPERTY, String.class, null);
        pendientesContainer.addContainerProperty(ENTIDAD_PROPERTY, String.class, null);
        pendientesContainer.addContainerProperty(MONTO_PROPERTY, String.class, null);
        pendientesContainer.addContainerProperty(MONEDA_PROPERTY, String.class, null);
        pendientesContainer.addContainerProperty(FECHA_PROPERTY, String.class, null);
        pendientesContainer.addContainerProperty(ANTIGUEDAD_PROPERTY, String.class, null);
        pendientesContainer.addContainerProperty(ACCION_PROPERTY, String.class, null);
        // ocultas
        pendientesContainer.addContainerProperty(IDPROVEEDOR_PROPERTY, String.class, null);
        pendientesContainer.addContainerProperty(NOMBREPROV_PROPERTY, String.class, null);
        pendientesContainer.addContainerProperty(IDNOMENCLATURA_PROPERTY, String.class, null);
        pendientesContainer.addContainerProperty(CODIGOCC_PROPERTY, String.class, null);
        pendientesContainer.addContainerProperty(MONTOSF_PROPERTY, String.class, null);
        pendientesContainer.addContainerProperty(MONTOQ_PROPERTY, String.class, null);
        pendientesContainer.addContainerProperty(TIPOCAMBIO_PROPERTY, String.class, null);

        pendientesGrid = new Grid(pendientesContainer);
        pendientesGrid.setWidth("100%");
        pendientesGrid.setImmediate(true);
        pendientesGrid.setSelectionMode(Grid.SelectionMode.NONE);
        pendientesGrid.setHeightMode(HeightMode.ROW);
        pendientesGrid.setHeightByRows(5);
        pendientesGrid.setResponsive(true);

        pendientesGrid.getColumn(IDPROVEEDOR_PROPERTY).setHidable(true).setHidden(true);
        pendientesGrid.getColumn(NOMBREPROV_PROPERTY).setHidable(true).setHidden(true);
        pendientesGrid.getColumn(IDNOMENCLATURA_PROPERTY).setHidable(true).setHidden(true);
        pendientesGrid.getColumn(CODIGOCC_PROPERTY).setHidable(true).setHidden(true);
        pendientesGrid.getColumn(MONTOSF_PROPERTY).setHidable(true).setHidden(true);
        pendientesGrid.getColumn(MONTOQ_PROPERTY).setHidable(true).setHidden(true);
        pendientesGrid.getColumn(TIPOCAMBIO_PROPERTY).setHidable(true).setHidden(true);

        pendientesGrid.getColumn(ENTIDAD_PROPERTY).setExpandRatio(3);
        pendientesGrid.getColumn(MONTO_PROPERTY).setExpandRatio(1);

        pendientesGrid.setCellStyleGenerator(cellRef
                -> MONTO_PROPERTY.equals(cellRef.getPropertyId()) ? "rightalign" : null);

        // Resaltar filas vencidas (> 6 meses).
        pendientesGrid.setRowStyleGenerator(rowRef -> {
            Object fechaStr = rowRef.getItem().getItemProperty(FECHA_PROPERTY).getValue();
            return esVencido(String.valueOf(fechaStr)) ? "vencido" : null;
        });

        // Columna inline de acción: registra la partida de reverso a Tesorería.
        pendientesGrid.getColumn(ACCION_PROPERTY).setRenderer(new ButtonRenderer(e -> {
            Object itemId = e.getItemId();
            registrarChequeEnTesoreria(
                    String.valueOf(pendientesContainer.getContainerProperty(itemId, CHEQUE_PROPERTY).getValue()),
                    String.valueOf(pendientesContainer.getContainerProperty(itemId, IDPROVEEDOR_PROPERTY).getValue()),
                    String.valueOf(pendientesContainer.getContainerProperty(itemId, NOMBREPROV_PROPERTY).getValue()),
                    String.valueOf(pendientesContainer.getContainerProperty(itemId, IDNOMENCLATURA_PROPERTY).getValue()),
                    String.valueOf(pendientesContainer.getContainerProperty(itemId, CODIGOCC_PROPERTY).getValue()),
                    String.valueOf(pendientesContainer.getContainerProperty(itemId, MONEDA_PROPERTY).getValue()),
                    Double.parseDouble(String.valueOf(pendientesContainer.getContainerProperty(itemId, MONTOSF_PROPERTY).getValue())),
                    Double.parseDouble(String.valueOf(pendientesContainer.getContainerProperty(itemId, MONTOQ_PROPERTY).getValue())),
                    Double.parseDouble(String.valueOf(pendientesContainer.getContainerProperty(itemId, TIPOCAMBIO_PROPERTY).getValue()))
            );
        }));

        pendientesFooter = pendientesGrid.appendFooterRow();
        pendientesFooter.getCell(MONEDA_PROPERTY).setText("0 CHEQUES");
        pendientesFooter.getCell(MONTO_PROPERTY).setText("0.00");
        pendientesFooter.getCell(MONTO_PROPERTY).setStyleName("rightalign");

        agregarFiltro(pendientesGrid, pendientesContainer, CHEQUE_PROPERTY);
        agregarFiltro(pendientesGrid, pendientesContainer, ENTIDAD_PROPERTY);

        layout.addComponents(tituloLayout, pendientesGrid);
        addComponent(layout);
    }

    private void llenarGridPendientes() {
        pendientesContainer.removeAllItems();

        if (idBancosLocal == null || idBancosExtranjera == null) {
            Notification.show("No están configuradas las cuentas default de Bancos.",
                    Notification.Type.WARNING_MESSAGE);
            return;
        }

        // Cheques emitidos (salida de bancos) que aún no han sido conciliados/cobrados
        // y que todavía no han sido trasladados a la cuenta de Cheques en Tesorería.
        queryString = " SELECT cp.* FROM contabilidad_partida cp";
        queryString += " WHERE cp.IdEmpresa = " + empresaId;
        queryString += " AND cp.TipoDocumento = 'CHEQUE'";
        queryString += " AND cp.IdNomenclatura IN (" + idBancosLocal + ", " + idBancosExtranjera + ")";
        queryString += " AND cp.Estatus NOT IN ('ANULADO', 'COBRADO', 'PAGADO')";
        queryString += " AND cp.IdConciliacion = 0";
        queryString += " AND cp.Haber > 0";
        queryString += " AND Extract(YEAR FROM cp.Fecha) > 2020";
        // Solo cheques que ya superaron la vigencia de cobro (hoy - Fecha >= 6 meses).
        queryString += " AND cp.Fecha <= DATE_SUB(CURDATE(), INTERVAL 6 MONTH)";
        queryString += " AND NOT EXISTS (";
        queryString += "    SELECT 1 FROM contabilidad_partida t";
        queryString += "    WHERE t.IdEmpresa = cp.IdEmpresa";
        queryString += "    AND t.IdNomenclatura = " + idChequesTesoreria;
        queryString += "    AND t.NumeroDocumento = cp.NumeroDocumento";
        queryString += "    AND t.IdProveedor = cp.IdProveedor";
        queryString += "    AND t.Estatus <> 'ANULADO'";
        queryString += " )";
        queryString += " ORDER BY cp.Fecha ASC";

        double total = 0.00;
        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) {
                Object itemId = pendientesContainer.addItem();
                double monto = rsRecords.getDouble("Haber");
                double montoQ = rsRecords.getDouble("HaberQuetzales");
                double tipoCambio = rsRecords.getDouble("TipoCambio");
                if (tipoCambio == 0.00) {
                    tipoCambio = 1.00;
                }

                pendientesContainer.getContainerProperty(itemId, CHEQUE_PROPERTY).setValue(rsRecords.getString("NumeroDocumento"));
                pendientesContainer.getContainerProperty(itemId, ENTIDAD_PROPERTY).setValue(rsRecords.getString("IdProveedor") + " " + rsRecords.getString("NombreProveedor"));
                pendientesContainer.getContainerProperty(itemId, MONTO_PROPERTY).setValue(numberFormat.format(monto));
                pendientesContainer.getContainerProperty(itemId, MONEDA_PROPERTY).setValue(rsRecords.getString("MonedaDocumento"));
                pendientesContainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha")));
                pendientesContainer.getContainerProperty(itemId, ANTIGUEDAD_PROPERTY).setValue(calcularAntiguedad(rsRecords.getDate("Fecha")));
                pendientesContainer.getContainerProperty(itemId, ACCION_PROPERTY).setValue("Registrar en Tesorería");

                pendientesContainer.getContainerProperty(itemId, IDPROVEEDOR_PROPERTY).setValue(rsRecords.getString("IdProveedor"));
                pendientesContainer.getContainerProperty(itemId, NOMBREPROV_PROPERTY).setValue(rsRecords.getString("NombreProveedor"));
                pendientesContainer.getContainerProperty(itemId, IDNOMENCLATURA_PROPERTY).setValue(rsRecords.getString("IdNomenclatura"));
                pendientesContainer.getContainerProperty(itemId, CODIGOCC_PROPERTY).setValue(rsRecords.getString("CodigoCC") == null ? "" : rsRecords.getString("CodigoCC"));
                pendientesContainer.getContainerProperty(itemId, MONTOSF_PROPERTY).setValue(String.valueOf(monto));
                pendientesContainer.getContainerProperty(itemId, MONTOQ_PROPERTY).setValue(String.valueOf(montoQ));
                pendientesContainer.getContainerProperty(itemId, TIPOCAMBIO_PROPERTY).setValue(String.valueOf(tipoCambio));

                total += monto;
            }
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Error al listar cheques pendientes: " + ex.getMessage());
            ex.printStackTrace();
        }

        pendientesFooter.getCell(MONEDA_PROPERTY).setText(pendientesContainer.size() + " CHEQUES");
        pendientesFooter.getCell(MONTO_PROPERTY).setText(numberFormat.format(total));
    }

    // =====================================================================
    //  Acción: genera la partida contable que reversa Bancos y abona
    //          la cuenta de Cheques en Tesorería.
    // =====================================================================
    private void registrarChequeEnTesoreria(String numeroCheque, String idProveedor, String nombreProveedor,
            String idBancos, String codigoCC, String moneda, double monto, double montoQ, double tipoCambio) {

        if (idChequesTesoreria == null || idChequesTesoreria.trim().isEmpty()) {
            Notification.show("No está configurada la cuenta default 'Cheques en Tesorería'.",
                    Notification.Type.WARNING_MESSAGE);
            return;
        }

        String fecha = Utileria.getFechaYYYYMMDD_1(new Date());
        String anio = fecha.substring(0, 4);
        String mes = fecha.substring(5, 7);
        String dia = fecha.substring(8, 10);

        // CodigoPartida: IdEmpresa + YYYY + MM + DD + "5" + secuencia (mismo
        // patrón que ChequeTesoreriaForm).
        String prefijo = empresaId + anio + mes + dia + "5";
        String codigoPartida = prefijo + "001";

        try {
            queryString = " SELECT CodigoPartida FROM contabilidad_partida";
            queryString += " WHERE CodigoPartida LIKE '" + prefijo + "%'";
            queryString += " ORDER BY CodigoPartida DESC";

            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);
            if (rsRecords.next()) {
                String ultimo = rsRecords.getString("CodigoPartida");
                String seq = ultimo.substring(prefijo.length());
                codigoPartida = prefijo + String.format("%03d", Integer.valueOf(seq) + 1);
            }
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Error al generar CodigoPartida: " + ex.getMessage());
            ex.printStackTrace();
        }

        String descripcion = "Cheque en tesoreria No. " + numeroCheque + " - " + nombreProveedor;
        String usuario = ((SopdiUI) mainUI).sessionInformation.getStrUserId();

        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO contabilidad_partida (IdEmpresa, Estatus, CodigoPartida, CodigoCC,");
        sql.append(" TipoDocumento, Fecha, IdProveedor, NombreProveedor, NumeroDocumento, IdNomenclatura,");
        sql.append(" MonedaDocumento, MontoDocumento, Debe, Haber, DebeQuetzales, HaberQuetzales, TipoCambio, Saldo,");
        sql.append(" Descripcion, CreadoUsuario, CreadoFechaYHora)");
        sql.append(" VALUES ");

        // Línea 1: BANCOS - DEBE (reversa la salida original de bancos).
        sql.append("(");
        sql.append(empresaId);
        sql.append(",'INGRESADO'");
        sql.append(",'").append(codigoPartida).append("'");
        sql.append(",'").append(codigoCC).append("'");
        sql.append(",'CHEQUE TESORERIA'");
        sql.append(",'").append(fecha).append("'");
        sql.append(",").append(idProveedor);
        sql.append(",'").append(nombreProveedor.replace("'", "")).append("'");
        sql.append(",'").append(numeroCheque).append("'");
        sql.append(",").append(idBancos);
        sql.append(",'").append(moneda).append("'");
        sql.append(",").append(monto);
        sql.append(",").append(monto);   // Debe
        sql.append(",0.00");             // Haber
        sql.append(",").append(montoQ);  // DebeQuetzales
        sql.append(",0.00");             // HaberQuetzales
        sql.append(",").append(tipoCambio);
        sql.append(",0.00");
        sql.append(",'").append(descripcion).append("'");
        sql.append(",").append(usuario);
        sql.append(",current_timestamp");
        sql.append(")");

        // Línea 2: CHEQUES EN TESORERÍA - HABER (abono).
        sql.append(",(");
        sql.append(empresaId);
        sql.append(",'INGRESADO'");
        sql.append(",'").append(codigoPartida).append("'");
        sql.append(",'").append(codigoCC).append("'");
        sql.append(",'CHEQUE TESORERIA'");
        sql.append(",'").append(fecha).append("'");
        sql.append(",").append(idProveedor);
        sql.append(",'").append(nombreProveedor.replace("'", "")).append("'");
        sql.append(",'").append(numeroCheque).append("'");
        sql.append(",").append(idChequesTesoreria);
        sql.append(",'").append(moneda).append("'");
        sql.append(",").append(monto);
        sql.append(",0.00");             // Debe
        sql.append(",").append(monto);   // Haber
        sql.append(",0.00");             // DebeQuetzales
        sql.append(",").append(montoQ);  // HaberQuetzales
        sql.append(",").append(tipoCambio);
        sql.append(",0.00");
        sql.append(",'").append(descripcion).append("'");
        sql.append(",").append(usuario);
        sql.append(",current_timestamp");
        sql.append(")");

        try {
            ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().setAutoCommit(false);

            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            stQuery.executeUpdate(sql.toString());

            ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().commit();
            ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().setAutoCommit(true);

            Notification notif = new Notification("CHEQUE REGISTRADO EN TESORERÍA EXITOSAMENTE.",
                    Notification.Type.HUMANIZED_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.CHECK);
            notif.show(Page.getCurrent());

            llenarGridRegistrados();
            llenarGridPendientes();

        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Error al registrar cheque en tesorería: " + ex.getMessage());
            ex.printStackTrace();

            Notification notif = new Notification("HA OCURRIDO UN ERROR DE BASE DE DATOS : " + ex.getMessage(),
                    Notification.Type.ERROR_MESSAGE);
            notif.setDelayMsec(2000);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.WARNING);
            notif.show(Page.getCurrent());

            try {
                ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().rollback();
                ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().setAutoCommit(true);
            } catch (SQLException ex2) {
                Logger.getLogger(ChequesEnTesoreriaView.class.getName()).log(Level.SEVERE, null, ex2);
            }
        }
    }

    // =====================================================================
    //  Utilitarios
    // =====================================================================
    /**
     * Calcula la antigüedad de un cheque en años/meses/días respecto de hoy.
     */
    private String calcularAntiguedad(Date fecha) {
        if (fecha == null) {
            return "";
        }
        Calendar desde = Calendar.getInstance();
        desde.setTime(fecha);
        Calendar hoy = Calendar.getInstance();

        int meses = (hoy.get(Calendar.YEAR) - desde.get(Calendar.YEAR)) * 12
                + (hoy.get(Calendar.MONTH) - desde.get(Calendar.MONTH));
        if (hoy.get(Calendar.DAY_OF_MONTH) < desde.get(Calendar.DAY_OF_MONTH)) {
            meses--;
        }
        if (meses < 0) {
            meses = 0;
        }

        int anios = meses / 12;
        int mesesRestantes = meses % 12;

        StringBuilder sb = new StringBuilder();
        if (anios > 0) {
            sb.append(anios).append(anios == 1 ? " año " : " años ");
        }
        sb.append(mesesRestantes).append(mesesRestantes == 1 ? " mes" : " meses");
        if (meses >= 6) {
            sb.append(" (VENCIDO)");
        }
        return sb.toString();
    }

    /**
     * Determina si una fecha (dd/MM/yyyy) tiene 1 o más años de antigüedad.
     */
    private boolean esVencido(String fechaDDMMYYYY) {
        try {
            String[] partes = fechaDDMMYYYY.split("/");
            Calendar desde = Calendar.getInstance();
            desde.set(Integer.parseInt(partes[2]), Integer.parseInt(partes[1]) - 1, Integer.parseInt(partes[0]));
            Calendar limite = Calendar.getInstance();
            limite.add(Calendar.YEAR, -1);
            return !desde.after(limite);
        } catch (Exception ex) {
            return false;
        }
    }

    private void agregarFiltro(Grid grid, IndexedContainer container, String propertyId) {
        Grid.HeaderRow filterRow;
        if (grid.getHeaderRowCount() > 1) {
            filterRow = grid.getHeaderRow(1);
        } else {
            filterRow = grid.appendHeaderRow();
        }

        Grid.HeaderCell cell = filterRow.getCell(propertyId);
        TextField filtro = new TextField();
        filtro.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filtro.setInputPrompt("Filtrar");
        filtro.setColumns(10);
        filtro.addTextChangeListener(change -> {
            container.removeContainerFilters(propertyId);
            if (!change.getText().isEmpty()) {
                container.addContainerFilter(new SimpleStringFilter(propertyId, change.getText(), true, false));
            }
        });
        cell.setComponent(filtro);
    }

    /**
     * Abre el reporte PDF con el detalle de ambos grids y el encabezado de la
     * empresa.
     */
    private void printPdf() {
        ChequesEnTesoreriaPDF reporte = new ChequesEnTesoreriaPDF(
                empresaId,
                empresaNombre,
                ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyTaxId(),
                Utileria.getFechaDDMMYYYY(new Date()),
                registradosContainer,
                pendientesContainer
        );
        mainUI.addWindow(reporte);
        reporte.center();
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        ((SopdiUI) UI.getCurrent()).lblEmpresaYFormulario.setValue(empresaId + " " + empresaNombre + " CHEQUES EN TESORERÍA");
        Page.getCurrent().setTitle("Sopdi - Cheques en Tesorería");
    }
}
