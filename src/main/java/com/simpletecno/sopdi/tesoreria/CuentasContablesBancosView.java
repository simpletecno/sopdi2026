package com.simpletecno.sopdi.tesoreria;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Responsive;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import java.text.DecimalFormat;

public class CuentasContablesBancosView extends VerticalLayout implements View {

    static final String ID_CUENTABANCO_PROPERTY    = "Id";
    static final String ID_EMPRESA_PROPERTY        = "Id Empresa";
    static final String EMPRESA_PROPERTY           = "Empresa";
    static final String ID_NOMENCLATURA_PROPERTY   = "Id Nomenclatura";
    static final String N5_PROPERTY                = "N5";
    static final String PROVEEDOR_PROPERTY         = "Banco";
    static final String NOCUENTA_PROPERTY          = "No Cuenta";
    static final String MONEDA_PROPERTY            = "Moneda";
    static final String PRINCIPAL_PROPERTY         = "Principal";
    static final String PLANILLA_PROPERTY          = "Planilla";
    static final String SALDO_CONTABLE_PROPERTY    = "Saldo Contable";
    static final String ULT_CONCILIACION_PROPERTY  = "Conciliado hasta";

    static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("#,###,##0.00");

    public IndexedContainer container = new IndexedContainer();
    Grid cuentasGrid;

    UI mainUI;
    Statement stQuery;
    ResultSet rsRecords;
    String queryString;

    String idEmpresa;
    String idCuentaBanco;

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public CuentasContablesBancosView() {
        this.mainUI = UI.getCurrent();

        Responsive.makeResponsive(this);
        setMargin(true);
        setSpacing(true);

        createTablaCuentasContables();
        asegurarColumnasCuentasBancos();
        llenarTablaCuentas();
        createButtons();

    }

    public void createTablaCuentasContables() {

        HorizontalLayout reportLayout = new HorizontalLayout();
        reportLayout.setWidth("100%");
        reportLayout.addStyleName("rcorners3");
        reportLayout.setResponsive(true);
        reportLayout.setMargin(true);

        container.addContainerProperty(ID_CUENTABANCO_PROPERTY, String.class, null);
        container.addContainerProperty(PROVEEDOR_PROPERTY, String.class, null);
        container.addContainerProperty(ID_NOMENCLATURA_PROPERTY, String.class, null);
        container.addContainerProperty(N5_PROPERTY, String.class, null);
        container.addContainerProperty(NOCUENTA_PROPERTY, String.class, null);
        container.addContainerProperty(MONEDA_PROPERTY, String.class, null);
        container.addContainerProperty(ID_EMPRESA_PROPERTY, String.class, null);
        container.addContainerProperty(EMPRESA_PROPERTY, String.class, null);
        container.addContainerProperty(PRINCIPAL_PROPERTY, String.class, "No");
        container.addContainerProperty(PLANILLA_PROPERTY, String.class, "No");
        container.addContainerProperty(SALDO_CONTABLE_PROPERTY, String.class, "0.00");
        container.addContainerProperty(ULT_CONCILIACION_PROPERTY, String.class, "—");

        cuentasGrid = new Grid("Listado de cuentas", container);
        cuentasGrid.setImmediate(true);
        cuentasGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        cuentasGrid.setDescription("Seleccione un registro.");
        cuentasGrid.setHeightMode(HeightMode.ROW);
        cuentasGrid.setHeightByRows(10);
        cuentasGrid.setWidth("100%");
        cuentasGrid.setResponsive(true);
        cuentasGrid.setEditorBuffered(false);

        cuentasGrid.getColumn(ID_CUENTABANCO_PROPERTY).setHidable(true).setHidden(true);
        cuentasGrid.getColumn(ID_EMPRESA_PROPERTY).setHidable(true).setHidden(true);
        cuentasGrid.getColumn(ID_NOMENCLATURA_PROPERTY).setHidable(true).setHidden(true);

        cuentasGrid.getColumn(N5_PROPERTY).setExpandRatio(3);
        cuentasGrid.getColumn(PROVEEDOR_PROPERTY).setWidth(180);
        cuentasGrid.getColumn(NOCUENTA_PROPERTY).setWidth(150);
        cuentasGrid.getColumn(MONEDA_PROPERTY).setWidth(100);
        cuentasGrid.getColumn(EMPRESA_PROPERTY).setExpandRatio(2);
        cuentasGrid.getColumn(PRINCIPAL_PROPERTY).setWidth(80);
        cuentasGrid.getColumn(PLANILLA_PROPERTY).setWidth(80);
        cuentasGrid.getColumn(SALDO_CONTABLE_PROPERTY).setWidth(140);
        cuentasGrid.getColumn(ULT_CONCILIACION_PROPERTY).setWidth(140);

        cuentasGrid.setCellStyleGenerator(cell -> {
            if (SALDO_CONTABLE_PROPERTY.equals(cell.getPropertyId())) return "rightalign";
            return null;
        });

        reportLayout.addComponent(cuentasGrid);
        reportLayout.setComponentAlignment(cuentasGrid, Alignment.MIDDLE_CENTER);

        addComponent(reportLayout);
        setComponentAlignment(reportLayout, Alignment.TOP_CENTER);
    }

    private void createButtons() {

        Button editBtn = new Button("Editar");
        editBtn.setIcon(FontAwesome.EDIT);
        editBtn.setWidth(140, Sizeable.UNITS_PIXELS);
        editBtn.setDescription("Actualizar datos");
        editBtn.addListener((Button.ClickListener) (Button.ClickEvent event) -> {
            try {
                if (cuentasGrid.getSelectedRow() == null) {
                    Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
                } else {

                    idEmpresa = String.valueOf(container.getContainerProperty(cuentasGrid.getSelectedRow(), ID_EMPRESA_PROPERTY).getValue());
                    idCuentaBanco = String.valueOf(container.getContainerProperty(cuentasGrid.getSelectedRow(), ID_CUENTABANCO_PROPERTY).getValue());

                    if (!idEmpresa.equals(((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId())) {
                        Notification.show("Debe seleccionar una cuenta que pertenezca a " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName(), Notification.Type.WARNING_MESSAGE);
                        return;
                    }

                    CuentasBancosForm cuentasBancosForm = new CuentasBancosForm("1", idCuentaBanco); // 1 SIGNIFICA EDITAR
                    UI.getCurrent().addWindow(cuentasBancosForm);
                    cuentasBancosForm.center();
                }

            } catch (Exception ex) {
                System.out.println("Error en el boton editar cuenta" + ex);
                ex.printStackTrace();
            }
        });

        Button newBtn = new Button("Nueva");
        newBtn.setIcon(FontAwesome.PLUS);
        newBtn.setWidth(140, Sizeable.UNITS_PIXELS);
        newBtn.setDescription("Agregar nueva empresa.");
        newBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                CuentasBancosForm cuentasBancosForm = new CuentasBancosForm("0", ""); // 0 SIGNIFICA NUEVA CUENTA
                UI.getCurrent().addWindow(cuentasBancosForm);
                cuentasBancosForm.center();
            }
        });

        Button eliminarBtn = new Button("Eliminar");
        eliminarBtn.setIcon(FontAwesome.TRASH);
        eliminarBtn.setDescription("Eliminar cuenta.");
        eliminarBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (cuentasGrid.getSelectedRow() == null) {

                    Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
                } else {
                    if (cuentasGrid.getSelectedRow() == null) {
                        Notification.show("Por favor, seleccione el registro correspondiente.", Notification.Type.WARNING_MESSAGE);
                        return;
                    }

                    idEmpresa = String.valueOf(container.getContainerProperty(cuentasGrid.getSelectedRow(), ID_EMPRESA_PROPERTY).getValue());
                    idCuentaBanco = String.valueOf(container.getContainerProperty(cuentasGrid.getSelectedRow(), ID_CUENTABANCO_PROPERTY).getValue());

                    if (!idEmpresa.equals(((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId())) {
                        Notification.show("Debe seleccionar una cuenta que pertenezca a " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName(), Notification.Type.WARNING_MESSAGE);
                        return;
                    }

                    try {

                        queryString = "DELETE FROM contabilidad_cuentas_bancos";
                        queryString += " WHERE IdCuentaBanco = " + idCuentaBanco;

                        stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                        stQuery.executeUpdate(queryString);

                        Notification.show("Cuenta eliminada con exito!", Notification.Type.HUMANIZED_MESSAGE);

                        llenarTablaCuentas();

                    } catch (SQLException ex) {
                        System.out.println("Error al buscar registros en contabilidad_cuentas_bancos" + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            }
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setMargin(false);
        buttonsLayout.addComponent(editBtn);
        buttonsLayout.addComponent(newBtn);
        buttonsLayout.addComponent(eliminarBtn);

        addComponent(buttonsLayout);
        setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);
    }

    public void llenarTablaCuentas() {

        container.removeAllItems();        

        queryString = "  SELECT *, cuen.N5, emp.Empresa, prov.Nombre ";
        queryString += " FROM contabilidad_cuentas_bancos AS ban";
        queryString += " INNER JOIN contabilidad_nomenclatura_empresa AS cuen";
        queryString += " ON ban.IdNomenclatura = cuen.IdNomenclatura";
        queryString += " INNER JOIN contabilidad_empresa AS emp ON ban.IdEmpresa = emp.IdEmpresa";
        queryString += " INNER JOIN proveedor_empresa AS prov ON ban.IdProveedor = prov.IdProveedor";
        queryString += " WHERE ban.IdEmpresa = " + empresaId;
        queryString += " AND cuen.IdEmpresa = " + empresaId;
        queryString += " AND prov.IdEmpresa = " + empresaId;
        queryString += " ORDER BY ban.IdEmpresa, ban.IdNomenclatura";

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);
            
            if (rsRecords.next()) { //  encontrado                                                
                do {
                    Object itemId = container.addItem();

                    container.getContainerProperty(itemId, ID_CUENTABANCO_PROPERTY).setValue(rsRecords.getString("IdCuentaBanco"));
                    container.getContainerProperty(itemId, PROVEEDOR_PROPERTY).setValue(rsRecords.getString("prov.Nombre"));
                    container.getContainerProperty(itemId, ID_EMPRESA_PROPERTY).setValue(rsRecords.getString("IdEmpresa"));
                    container.getContainerProperty(itemId, EMPRESA_PROPERTY).setValue(rsRecords.getString("Empresa"));
                    container.getContainerProperty(itemId, ID_NOMENCLATURA_PROPERTY).setValue(rsRecords.getString("IdNomenclatura"));
                    container.getContainerProperty(itemId, N5_PROPERTY).setValue(rsRecords.getString("N5"));
                    container.getContainerProperty(itemId, NOCUENTA_PROPERTY).setValue(rsRecords.getString("NoCuenta"));
                    container.getContainerProperty(itemId, MONEDA_PROPERTY).setValue(rsRecords.getString("Moneda"));
                    container.getContainerProperty(itemId, PRINCIPAL_PROPERTY).setValue("1".equals(rsRecords.getString("EsPrincipal")) ? "Sí" : "No");
                    container.getContainerProperty(itemId, PLANILLA_PROPERTY).setValue("1".equals(rsRecords.getString("EsPlanilla")) ? "Sí" : "No");

                    String idCuenta = rsRecords.getString("IdCuentaBanco");
                    String idNom    = rsRecords.getString("IdNomenclatura");
                    String moneda   = rsRecords.getString("Moneda");
                    String simb     = (moneda != null && moneda.startsWith("Q")) ? "Q." : "$.";
                    double[] saldoYFecha = calcularSaldoYUltimaConciliacion(idCuenta, idNom);
                    container.getContainerProperty(itemId, SALDO_CONTABLE_PROPERTY).setValue(simb + NUMBER_FORMAT.format(saldoYFecha[0]));
                    container.getContainerProperty(itemId, ULT_CONCILIACION_PROPERTY).setValue(formatearAnioMes((long) saldoYFecha[1]));

                } while (rsRecords.next());
            }
        } catch (Exception ex) {
            System.out.println("Error al listar tabla empresas contables :" + ex);
            ex.printStackTrace();
        }
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        ((SopdiUI) UI.getCurrent()).lblEmpresaYFormulario.setValue(empresaId + " " + empresaNombre + " Cuentas contables de Bancos");
        Page.getCurrent().setTitle("Sopdi - Cuentas Banco");
        llenarTablaCuentas();
    }

    /**
     * Devuelve [saldoContable, anioMes] para una cuenta bancaria.
     * saldoContable = SaldoFinalContable última conciliación + movimientos posteriores.
     * anioMes = valor numérico YYYYMM (0 si no hay conciliación).
     */
    private double[] calcularSaldoYUltimaConciliacion(String idCuentaBanco, String idNomenclatura) {
        double saldo = 0;
        long anioMes = 0;
        try {
            String sqlConc = "SELECT SaldoFinalContable, AnioMes"
                    + " FROM contabilidad_conciliacion_bancaria"
                    + " WHERE IdCuentaBanco = " + idCuentaBanco
                    + "   AND IdEmpresa = " + empresaId
                    + "   AND Estatus = 'FINALIZADA'"
                    + " ORDER BY AnioMes DESC LIMIT 1";
            double saldoConc = 0;
            String anioMesConcStr = null;
            try (Statement st = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                 ResultSet rs = st.executeQuery(sqlConc)) {
                if (rs.next()) {
                    saldoConc = rs.getDouble("SaldoFinalContable");
                    anioMesConcStr = rs.getString("AnioMes");
                    anioMes = Long.parseLong(anioMesConcStr);
                }
            }
            String filtroFecha = (anioMesConcStr != null)
                    ? " AND cp.Fecha > LAST_DAY(STR_TO_DATE(CONCAT('" + anioMesConcStr + "','01'),'%Y%m%d'))"
                    : "";
            String sqlMov = "SELECT COALESCE(SUM(cp.Debe - cp.Haber), 0) AS Movimiento"
                    + " FROM contabilidad_partida cp"
                    + " WHERE cp.IdNomenclatura = " + idNomenclatura
                    + "   AND cp.IdEmpresa = " + empresaId
                    + "   AND cp.Estatus <> 'ANULADO'"
                    + filtroFecha;
            try (Statement st = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                 ResultSet rs = st.executeQuery(sqlMov)) {
                if (rs.next()) saldo = saldoConc + rs.getDouble("Movimiento");
            }
        } catch (Exception ex) {
            System.out.println("Error calcularSaldoYUltimaConciliacion: " + ex.getMessage());
        }
        return new double[]{saldo, anioMes};
    }

    /** Convierte YYYYMM a formato MM/YYYY. Devuelve "—" si anioMes es 0. */
    private String formatearAnioMes(long anioMes) {
        if (anioMes == 0) return "—";
        String s = String.valueOf(anioMes);
        if (s.length() == 6) return s.substring(4, 6) + "/" + s.substring(0, 4);
        return s;
    }

    private void asegurarColumnasCuentasBancos() {
        String[][] columnas = {
                {"EsPrincipal", "TINYINT(1) NOT NULL DEFAULT 0"},
                {"EsPlanilla",  "TINYINT(1) NOT NULL DEFAULT 0"},
        };
        for (String[] col : columnas) {
            try {
                Statement st = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                ResultSet rs = st.executeQuery(
                        "SELECT COUNT(*) FROM information_schema.COLUMNS"
                        + " WHERE TABLE_SCHEMA = DATABASE()"
                        + " AND TABLE_NAME = 'contabilidad_cuentas_bancos'"
                        + " AND COLUMN_NAME = '" + col[0] + "'");
                boolean existe = rs.next() && rs.getInt(1) > 0;
                rs.close();
                if (!existe) {
                    st.executeUpdate("ALTER TABLE contabilidad_cuentas_bancos ADD COLUMN " + col[0] + " " + col[1]);
                }
            } catch (Exception ex) {
                System.out.println("No se pudo asegurar columna contabilidad_cuentas_bancos." + col[0] + ": " + ex.getMessage());
            }
        }
    }
}
