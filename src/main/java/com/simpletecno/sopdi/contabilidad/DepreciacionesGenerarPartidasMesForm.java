package com.simpletecno.sopdi.contabilidad;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.renderers.NumberRenderer;
import com.vaadin.ui.themes.ValoTheme;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Ventana para generar partidas por mes (Vaadin 7).
 * Vista previa informativa sin lógica de generación.
 */
public class DepreciacionesGenerarPartidasMesForm extends Window {

    private static final String IDDEPRECIACION = "IdDepreciacion";
    private static final String CUENTA = "NoCuenta";
    private static final String N5 = "Cuenta";
    private static final String IDNOMENCLATURA = "IdNomenclatura";
    private static final String DESCRIPCION = "Descripcion";
    private static final String CODIGODEPRECIACION = "CodigoDepreciacion";
    private static final String DEBE = "Debe";
    private static final String HABER = "Haber";
    private static final String ACTIVO = "Activo";
    private static final String IDPROVEEDOR = "IdProveedor";
    private static final String PROVEEDORNOMBRE = "Nombre";
    private static final String NITPROVEDOR = "NitProveedor";
    private static final String CENTROCOSTO = "CentroCosto";

    private static final Utileria utileria = new Utileria();

    private final IndexedContainer partidasContainer = new IndexedContainer();
    private final Grid partidasGrid = new Grid(partidasContainer);
    private Label lblTotalDebe;
    private Label lblTotalHaber;
    private final PopupDateField dfMes;
    private final Button btnGenerar;
    private int numDepreciaciones = 0;

    private final Map<String, String> idsDepreciacionesGeneradas = new HashMap<>();
    private String[] codigoPartidas = new String[0];
    private String idEmpresa;

    public DepreciacionesGenerarPartidasMesForm() {

        center();
        setModal(true);
        setWidth("60%");
        setHeight("70%");

        idEmpresa= ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setMargin(new MarginInfo(true, true, true, true));
        mainLayout.setSpacing(true);
        mainLayout.setSizeFull();

        // Encabezado
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidth("100%");
        headerLayout.setSpacing(true);
        headerLayout.setMargin(false);

        dfMes = new PopupDateField("Mes a generar:");
        dfMes.setDateFormat("MM/yyyy");
        dfMes.setValue(new Date());
        dfMes.setWidth("200px");
        dfMes.addValueChangeListener(event -> cargarDatos());

        Label lblInfo = new Label("Las siguientes partidas se generarán para este mes:");
        lblInfo.addStyleName(ValoTheme.LABEL_SMALL);

        headerLayout.addComponents(dfMes, lblInfo);
        headerLayout.setExpandRatio(dfMes, 1f);
        headerLayout.setExpandRatio(lblInfo, 2f);

        mainLayout.addComponent(headerLayout);

        // Grid de partidas
        crearGridPartidas();
        mainLayout.addComponent(partidasGrid);
        mainLayout.setExpandRatio(partidasGrid, 1f);

        // Panel de totales
        VerticalLayout panelTotales = crearPanelTotales();
        mainLayout.addComponent(panelTotales);

        // Botones de acción
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);
        buttonLayout.setMargin(new MarginInfo(true, false, false, false));
        buttonLayout.setWidth("100%");

        btnGenerar = new Button("Generar Partidas", FontAwesome.SAVE);
        btnGenerar.setStyleName(ValoTheme.BUTTON_PRIMARY);
        btnGenerar.addClickListener(event -> {
            if(generarPartidas())
                if(actualizarActivosDepreciados())
                    close();
        });

        Button btnCerrar = new Button("Cerrar", FontAwesome.TIMES);
        btnCerrar.addClickListener(event -> close());

        buttonLayout.addComponents(btnGenerar, btnCerrar);
        buttonLayout.setComponentAlignment(btnGenerar, Alignment.MIDDLE_CENTER);
        buttonLayout.setComponentAlignment(btnCerrar, Alignment.MIDDLE_RIGHT);

        mainLayout.addComponent(buttonLayout);

        setContent(mainLayout);

        // Cargar datos de ejemplo
        cargarDatos();
    }

    /**
     * Crea el grid con las partidas contables
     */
    private void crearGridPartidas() {

        partidasContainer.addContainerProperty(IDDEPRECIACION, String.class, null);
        partidasContainer.addContainerProperty(CUENTA, String.class, null);
        partidasContainer.addContainerProperty(N5, String.class, null);
        partidasContainer.addContainerProperty(DESCRIPCION, String.class, null);
        partidasContainer.addContainerProperty(IDNOMENCLATURA, String.class, null);
        partidasContainer.addContainerProperty(CODIGODEPRECIACION, String.class, null);
        partidasContainer.addContainerProperty(DEBE, BigDecimal.class, null);
        partidasContainer.addContainerProperty(HABER, BigDecimal.class, null);
        partidasContainer.addContainerProperty(ACTIVO, String.class, null);
        partidasContainer.addContainerProperty(IDPROVEEDOR, String.class, null);
        partidasContainer.addContainerProperty(PROVEEDORNOMBRE, String.class, null);
        partidasContainer.addContainerProperty(NITPROVEDOR, String.class, null);
        partidasContainer.addContainerProperty(CENTROCOSTO, String.class, null);

        partidasGrid.setContainerDataSource(partidasContainer);
        partidasGrid.setSelectionMode(Grid.SelectionMode.NONE);
        partidasGrid.setWidth("100%");
        partidasGrid.setHeightMode(HeightMode.ROW);
        partidasGrid.setHeightByRows(6);

        partidasGrid.getColumn(IDDEPRECIACION).setHidden(true);
        partidasGrid.getColumn(IDDEPRECIACION).setHidable(true);
        partidasGrid.getColumn(CUENTA).setHidden(true);
        partidasGrid.getColumn(CUENTA). setHidable(true);
        partidasGrid.getColumn(IDNOMENCLATURA).setHidden(true);
        partidasGrid.getColumn(IDNOMENCLATURA).setHidable(true);
        partidasGrid.getColumn(ACTIVO).setHidden(true);
        partidasGrid.getColumn(ACTIVO).setHidable(true);
        partidasGrid.getColumn(IDPROVEEDOR).setHidden(true);
        partidasGrid.getColumn(IDPROVEEDOR).setHidable(true);
        partidasGrid.getColumn(PROVEEDORNOMBRE).setHidden(true);
        partidasGrid.getColumn(PROVEEDORNOMBRE).setHidable(true);
        partidasGrid.getColumn(NITPROVEDOR).setHidden(true);
        partidasGrid.getColumn(NITPROVEDOR).setHidable(true);
        partidasGrid.getColumn(CENTROCOSTO).setHidden(true);
        partidasGrid.getColumn(CENTROCOSTO).setHidable(true);


        partidasGrid.getColumn(DEBE).setRenderer(new NumberRenderer("Q. %1$.2f"));
        partidasGrid.getColumn(HABER).setRenderer(new NumberRenderer("Q. %1$.2f"));

        // Hacer columnas redimensionables
        partidasGrid.getColumn(CUENTA).setExpandRatio(1);
        partidasGrid.getColumn(N5).setExpandRatio(1);
        partidasGrid.getColumn(DESCRIPCION).setExpandRatio(2);
        partidasGrid.getColumn(DEBE).setExpandRatio(1);
        partidasGrid.getColumn(HABER).setExpandRatio(1);
        partidasGrid.getColumn(ACTIVO).setExpandRatio(2);
    }

    /**
     * Crea panel con totales de debe y haber
     */
    private VerticalLayout crearPanelTotales() {
        VerticalLayout layout = new VerticalLayout();
        layout.addStyleName("rcorners3");
        layout.setMargin(true);
        layout.setSpacing(true);
        layout.setWidth("100%");
        layout.setHeight("80px");

        HorizontalLayout totalesLayout = new HorizontalLayout();
        totalesLayout.setSpacing(true);
        totalesLayout.setWidth("100%");
        totalesLayout.setMargin(false);

        lblTotalDebe = new Label("<b>Total Debe:</b> Q. 0.00");
        lblTotalDebe.setContentMode(ContentMode.HTML);
        lblTotalDebe.addStyleName(ValoTheme.LABEL_BOLD);

        lblTotalHaber = new Label("<b>Total Haber:</b> Q. 0.00");
        lblTotalHaber.setContentMode(ContentMode.HTML);
        lblTotalHaber.addStyleName(ValoTheme.LABEL_BOLD);

        totalesLayout.addComponents(lblTotalDebe, lblTotalHaber);
        totalesLayout.setComponentAlignment(lblTotalDebe, Alignment.MIDDLE_CENTER);
        totalesLayout.setComponentAlignment(lblTotalHaber, Alignment.MIDDLE_CENTER);
        totalesLayout.setExpandRatio(lblTotalDebe, 1f);
        totalesLayout.setExpandRatio(lblTotalHaber, 1f);

        layout.addComponent(totalesLayout);

        return layout;
    }

    /**
     * Carga los datos de depreciación del mes seleccionado desde la tabla activos_depreciacion
     */
    private void cargarDatos() {
        numDepreciaciones = 0;
        partidasContainer.removeAllItems();
        GregorianCalendar fecha = new GregorianCalendar();
        fecha.setTime(dfMes.getValue());
        int mes = fecha.get(GregorianCalendar.MONTH) + 1;
        int año = fecha.get(GregorianCalendar.YEAR);
        String errores = "";

        Logger.getLogger(DepreciacionesGenerarPartidasMesForm.class.getName()).log(Level.INFO, "Cargando datos de depreciación para " + mes + " / " + año);

        String query =
                "SELECT " +
                "ad.Id, ad.CodigoActivo, ad.CodigoDepreciacion, ad.Valor, ad.FechaCreado, ad.CodigoPartida, " +
                "p.IdProveedor, p.Nombre, p.NIT, a.Descripcion AS DescripcionActivo, cc.CodigoCentroCosto, " +
                "td.IdNomenclaturaDebe, td.IdNomenclaturaHaber, " +
                "cn_debe.N5 AS N5_Debe, cn_debe.NoCuenta AS Cuenta_Debe, cn_debe.IdNomenclatura AS IdNomenclatura_Debe, " +
                "cn_haber.N5 AS N5_Haber, cn_haber.NoCuenta AS Cuenta_Haber, cn_haber.IdNomenclatura AS IdNomenclatura_Haber " +
                "FROM activos_depreciacion ad " +
                "LEFT JOIN activos a ON ad.CodigoActivo = a.CodigoActivo AND a.IdEmpresa = ad.IdEmpresa " +
                "LEFT JOIN tipo_depreciacion td ON a.IdTipoDepreciacion = td.Id AND td.IdEmpresa = ad.IdEmpresa " +
                "LEFT JOIN contabilidad_nomenclatura_empresa cn_debe ON td.IdNomenclaturaDebe = cn_debe.IdNomenclatura " +
                "AND cn_debe.IdEmpresa = ad.IdEmpresa " +
                "LEFT JOIN contabilidad_nomenclatura_empresa cn_haber ON td.IdNomenclaturaHaber = cn_haber.IdNomenclatura " +
                "AND cn_haber.IdEmpresa = ad.IdEmpresa " +
                "LEFT JOIN proveedor_empresa p ON a.IdProveedor = p.IdProveedor " +
                "AND p.IdEmpresa = ad.IdEmpresa " +
                "LEFT JOIN centro_costo cc ON a.IdCentroCosto = cc.IdCentroCosto " +
                "AND cc.IdEmpresa = ad.IdEmpresa " +
                "WHERE ad.Mes = " + mes + " " +
                "AND ad.`Año` = " + año + " " +
                "AND ad.IdEmpresa = " + idEmpresa + " " +
                "AND ad.CodigoPartida IS NULL " +
                "ORDER BY ad.FechaCreado, ad.CodigoActivo";

        try {
            Statement stmt = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query);

            BigDecimal totalDebe = BigDecimal.ZERO;
            BigDecimal totalHaber = BigDecimal.ZERO;

            while (rs.next()) {
                numDepreciaciones++;

                idsDepreciacionesGeneradas.put(rs.getString("Id"), ""); // Guardamos los IDs de depreciaciones para usarlos en la generación de partidas

                String idDepreciacion = rs.getString("Id");
                String codigoActivo = rs.getString("CodigoActivo");
                BigDecimal valor = rs.getBigDecimal("Valor");
                String descripcionActivo = rs.getString("DescripcionActivo");
                String codigoDepreciacion = rs.getString("CodigoDepreciacion");
                String proveedorId = rs.getString("IdProveedor");
                String proveedorNombre = rs.getString("Nombre");
                String nitProveedor = rs.getString("NIT");
                String centroCosto = rs.getString("CodigoCentroCosto");

                // Partida de DEBE (Gasto de Depreciación)
                String cuentaDebe = rs.getString("Cuenta_Debe");
                String n5Debe = rs.getString("N5_Debe");
                String idNomenclaturaDebe = rs.getString("IdNomenclatura_Debe");

                if (cuentaDebe != null) {
                    agregarPartida(
                            idDepreciacion,
                            cuentaDebe != null ? cuentaDebe : "N/A",
                            n5Debe != null ? n5Debe : "N/A",
                            idNomenclaturaDebe,
                            "Depreciación - " + descripcionActivo + " " + codigoActivo,
                            codigoDepreciacion,
                            valor,
                            BigDecimal.ZERO,
                            codigoActivo,
                            proveedorId,
                            proveedorNombre,
                            nitProveedor,
                            centroCosto
                    );
                    totalDebe = totalDebe.add(valor);
                }else {
                    // Error: No se encontró cuenta de DEBE para este activo
                    errores += "- No se encontró cuenta de DEBE para el activo " + codigoActivo + "\n";
                    Logger.getLogger(DepreciacionesGenerarPartidasMesForm.class.getName()).log(Level.SEVERE, "No se encontró cuenta de DEBE para el activo " + codigoActivo);
                }

                // Partida de HABER (Depreciación Acumulada)
                String cuentaHaber = rs.getString("Cuenta_Haber");
                String n5Haber = rs.getString("N5_Haber");
                String idNomenclaturaHaber = rs.getString("IdNomenclatura_Haber");
                if (cuentaHaber != null) {
                    agregarPartida(
                            idDepreciacion,
                            cuentaHaber != null ? cuentaHaber : "N/A",
                            n5Haber != null ? n5Haber : "N/A",
                            idNomenclaturaHaber,
                            "Depreciación - " + descripcionActivo + " " + codigoActivo,
                            codigoDepreciacion,
                            BigDecimal.ZERO,
                            valor,
                            codigoActivo,
                            proveedorId,
                            proveedorNombre,
                            nitProveedor,
                            centroCosto
                    );
                    totalHaber = totalHaber.add(valor);
                }else{
                    // Error: No se encontró cuenta de HABER para este activo
                    errores += "- No se encontró cuenta de HABER para el activo " + codigoActivo + "\n";
                    Logger.getLogger(DepreciacionesGenerarPartidasMesForm.class.getName()).log(Level.SEVERE, "No se encontró cuenta de HABER para el activo " + codigoActivo);
                }
            }

            rs.close();
            stmt.close();

            if(!errores.isEmpty()){
                btnGenerar.setEnabled(false);
                Notification.show("Se encontraron los siguientes errores al cargar las partidas:\n" + errores,
                        Notification.Type.ERROR_MESSAGE);
            }

            // Actualizar totales
            actualizarTotales(totalDebe, totalHaber);

        } catch (Exception ex) {
            Notification.show("Error al cargar partidas de depreciación: " + ex.getMessage(),
                    Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    /**
     * Actualiza los labels de totales en el panel
     */
    private void actualizarTotales(BigDecimal debe, BigDecimal haber) {
        BigDecimal balance = debe.subtract(haber);

        String colorBalance = balance.compareTo(BigDecimal.ZERO) == 0 ? "green" : "red";

        lblTotalDebe.setValue("<b>Total Debe:</b> Q. " + String.format("%.2f", debe));
        lblTotalHaber.setValue("<b>Total Haber:</b> Q. " + String.format("%.2f", haber));
    }

    /**
     * Agrega una partida al grid
     */
    private void agregarPartida(String idDepreciacion, String cuenta, String n5, String idNomenclatura, String descripcion,
                                String codigoDepreciacion, BigDecimal debe, BigDecimal haber,
                                String activo, String idProveedor, String nombreProveedor, String nitProveedor, String centroCosto) {
        Object itemId = partidasContainer.addItem();
        partidasContainer.getContainerProperty(itemId, IDDEPRECIACION).setValue(idDepreciacion);
        partidasContainer.getContainerProperty(itemId, CUENTA).setValue(cuenta);
        partidasContainer.getContainerProperty(itemId, N5).setValue(n5);
        partidasContainer.getContainerProperty(itemId, IDNOMENCLATURA).setValue(idNomenclatura); //usamos n5 como id nomenclatura para simplificar
        partidasContainer.getContainerProperty(itemId, DESCRIPCION).setValue(descripcion);
        partidasContainer.getContainerProperty(itemId, CODIGODEPRECIACION).setValue(codigoDepreciacion);
        partidasContainer.getContainerProperty(itemId, DEBE).setValue(debe);
        partidasContainer.getContainerProperty(itemId, HABER).setValue(haber);
        partidasContainer.getContainerProperty(itemId, ACTIVO).setValue(activo);
        partidasContainer.getContainerProperty(itemId, IDPROVEEDOR).setValue(idProveedor);
        partidasContainer.getContainerProperty(itemId, PROVEEDORNOMBRE).setValue(nombreProveedor);
        partidasContainer.getContainerProperty(itemId, NITPROVEDOR).setValue(nitProveedor);
        partidasContainer.getContainerProperty(itemId, CENTROCOSTO).setValue(centroCosto); //centro de costo no se muestra pero se guarda para usar en generación de partidas
    }

    /**
     * Genera las partidas (stub: el integrador implementará la lógica real)
     */
    private boolean generarPartidas() {
        String queryString;
        String empresa = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
        Connection conn = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection();

        Date fecha = Utileria.getUltimoFechaDelMes(dfMes.getValue());
        codigoPartidas = new String[numDepreciaciones];

        codigoPartidas = Utileria.nextCodigosPartida(conn, empresa, fecha, 6, numDepreciaciones);

        int indexPartida = -1;
        String codigoActiuvoActual = "";

        queryString = " Insert Into contabilidad_partida (IdEmpresa, Estatus, CodigoPartida, CodigoCC,";
        queryString += " TipoDocumento, Fecha, IdProveedor, NITProveedor, ";
        queryString += " NombreProveedor, MontoDocumento, SerieDocumento, NumeroDocumento, ";
        queryString += " IdNomenclatura, MonedaDocumento, Debe, Haber,";
        queryString += " DebeQuetzales, HaberQuetzales, TipoCambio, CodigoCentroCosto, ";
        queryString += " Descripcion, CreadoUsuario, CreadoFechaYHora)";
        queryString += " Values \n";

        for (Object itemId : partidasContainer.getItemIds()) {
            if(!codigoActiuvoActual.equals(partidasContainer.getContainerProperty(itemId, ACTIVO).getValue().toString())) {
                codigoActiuvoActual = partidasContainer.getContainerProperty(itemId, ACTIVO).getValue().toString();
                indexPartida++;
            }
            idsDepreciacionesGeneradas.put(  //marcamos esta depreciacion como usada para generar partida
                    (partidasContainer.getContainerProperty(itemId, IDDEPRECIACION).getValue().toString()),
                    (codigoPartidas[indexPartida]));

            BigDecimal debe = (BigDecimal) partidasContainer.getContainerProperty(itemId, DEBE).getValue();
            BigDecimal haber = (BigDecimal) partidasContainer.getContainerProperty(itemId, HABER).getValue();
            BigDecimal monto = debe.compareTo(BigDecimal.ZERO) > 0 ? debe : haber;

            queryString += "(";
            queryString += String.valueOf(empresa);
            queryString += ",'INGRESADO'";
            queryString += ",'" + codigoPartidas[indexPartida] + "'";
            queryString += ",'" + codigoPartidas[indexPartida] + "'";
            queryString += ",'TRANSACCION ESPECIAL'";
            queryString += ",'" + Utileria.getFechaYYYYMMDD_1(fecha) + "'";
            queryString += "," + String.valueOf(partidasContainer.getContainerProperty(itemId, IDPROVEEDOR).getValue());
            queryString += ",'" + String.valueOf(partidasContainer.getContainerProperty(itemId, NITPROVEDOR).getValue()) +"'"; //nit proveedor
            queryString += ",'" + String.valueOf(partidasContainer.getContainerProperty(itemId, PROVEEDORNOMBRE).getValue()) + "'"; //nombre proveedor
            queryString += "," + Utileria.round(monto.doubleValue(), 2); //monto documento
            queryString += ",''"; //serie documento
            queryString += ",'" + String.valueOf(partidasContainer.getContainerProperty(itemId, CODIGODEPRECIACION).getValue()) + "'"; //numero documento (codigo depreciacion)
            queryString += "," + String.valueOf(partidasContainer.getContainerProperty(itemId, IDNOMENCLATURA).getValue()); //id nomenclatura
            queryString += ",'QUETZALES'"; //moneda documento
            queryString += "," + debe; // debe
            queryString += "," + haber; // haber
            queryString += "," + debe; // debe en quetzales
            queryString += "," + haber; // haber en quetzales
            queryString += "," + 1; //tipo cambio
            queryString += ",'" + String.valueOf(partidasContainer.getContainerProperty(itemId, CENTROCOSTO).getValue()) + "'"; //centro de costo
            queryString += ",'" + partidasContainer.getContainerProperty(itemId, DESCRIPCION).getValue() + "'"; //descripcion
            queryString += "," + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserId(); //usuario
            queryString += ",current_timestamp";
            queryString += ")";

            if(itemId != partidasContainer.lastItemId()){
                queryString += ",\n";
            }
        }

        System.out.println("segundo query pago factura = " + queryString);

        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(queryString);
            stmt.close();

            Notification.show("Partidas generadas exitosamente para el mes seleccionado.", Notification.Type.HUMANIZED_MESSAGE);

            return true;

        } catch (Exception ex) {
            Notification.show("Error al generar partidas: " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
            return false;
        }

    }

    private boolean actualizarActivosDepreciados(){
        String queryString;
        Connection conn = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection();
        Statement stmt = null;

        try {
            // Iniciar transacción
            conn.setAutoCommit(false);

            for(String activoId : idsDepreciacionesGeneradas.keySet()){
                queryString = "UPDATE activos_depreciacion SET CodigoPartida = '" + idsDepreciacionesGeneradas.get(activoId) + "', FechaPartida = current_timestamp WHERE Id = " + activoId;
                stmt = conn.createStatement();
                stmt.executeUpdate(queryString);
                stmt.close();
            }

            // Si todos los updates fueron exitosos, hacer commit
            conn.commit();
            conn.setAutoCommit(true);
            Notification.show("Activos depreciados actualizados exitosamente.", Notification.Type.HUMANIZED_MESSAGE);
            return true;

        } catch (Exception ex) {
            try {
                // Si hay error, hacer rollback de todos los cambios
                conn.rollback();
                conn.setAutoCommit(true);
            } catch (Exception rollbackEx) {
                Notification.show("Error al hacer rollback: " + rollbackEx.getMessage(), Notification.Type.ERROR_MESSAGE);
                rollbackEx.printStackTrace();
            }

            Notification.show("Error al actualizar activos depreciados. Se han revertido todos los cambios: " + ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            ex.printStackTrace();
            return false;

        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (Exception closeEx) {
                closeEx.printStackTrace();
            }
        }
    }
}
