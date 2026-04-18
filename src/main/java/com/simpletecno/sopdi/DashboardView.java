package com.simpletecno.sopdi;

import com.simpletecno.sopdi.utilerias.EChartsComponent;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class DashboardView extends VerticalLayout implements View {

    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");
    static DecimalFormat numberFormat2 = new DecimalFormat("#.#####");
    public Statement stQuery = null;
    public Statement stQuery1 = null;
    public ResultSet rsRecords = null;
    public ResultSet rsRecords1 = null;

    private HorizontalLayout topLayout = new HorizontalLayout();
    private HorizontalLayout bottomLayout = new HorizontalLayout();

    private Button refrescarBtn = new Button("Refrescar");

    String queryString = "";

    Map<String, Double> proveedorSaldo = new HashMap<>();

    public DashboardView() {

        // setWidth en lugar de setSizeUndefined: la vista debe ocupar el ancho
        // completo del navegador para que los layouts internos se anclen correctamente.
        setWidth("100%");
        setSpacing(true);
        setMargin(new MarginInfo(false, false, false, false));
        setDefaultComponentAlignment(Alignment.TOP_LEFT);

        Label titleLbl = new Label("Dashboard");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h2_custom");

        refrescarBtn.setIcon(FontAwesome.REFRESH);
        refrescarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setMargin(new MarginInfo(true, true, false, true));
        titleLayout.setSpacing(true);
        titleLayout.setWidth("100%");
        titleLayout.addComponents(titleLbl, refrescarBtn);
        titleLayout.setComponentAlignment(titleLbl, Alignment.MIDDLE_LEFT);
        titleLayout.setComponentAlignment(refrescarBtn, Alignment.MIDDLE_RIGHT);

        addComponent(titleLayout);

        // topLayout: contenedor de tarjetas KPI.
        // Se elimina LAYOUT_CARD del contenedor (era incorrecto aplicarlo aquí;
        // el estilo de tarjeta va en cada componente hijo, no en el wrapper).
        topLayout.setSpacing(true);
        topLayout.setMargin(new MarginInfo(false, true, false, true));
        topLayout.setDefaultComponentAlignment(Alignment.TOP_LEFT);
        topLayout.addStyleName(ValoTheme.LAYOUT_HORIZONTAL_WRAPPING);
        topLayout.setWidth("100%");

        // bottomLayout: gráficas. Necesita ancho 100% para anclar sus hijos.
        bottomLayout.setSpacing(true);
        bottomLayout.setMargin(new MarginInfo(false, true, true, true));
        bottomLayout.setDefaultComponentAlignment(Alignment.TOP_LEFT);
        bottomLayout.addStyleName(ValoTheme.LAYOUT_HORIZONTAL_WRAPPING);
        bottomLayout.setWidth("100%");

        addComponents(topLayout, bottomLayout);

        createTopData();
        createBottomGraphs();

        refrescarBtn.addClickListener(e -> {
            topLayout.removeAllComponents();
            proveedorSaldo.clear();
            createTopData();
        });
    }

    private void createTopData() {
        topLayout.addComponents(
                createBadget("Facturas Por Pagar", numberFormat.format(getTotalFacturasPorPagar())),
                createBadget("Formularios por Pagar", numberFormat.format(getTotalFormulariosPorPagar())),
                createBadget("Caja Chica por Liquidar", numberFormat.format(getTotalCajaChicaPorLiquidar())),
                createBadget("Tipo de cambio hoy", numberFormat2.format(((SopdiUI) UI.getCurrent()).sessionInformation.getFltExchangeRate()))
        );
    }

    private VerticalLayout createBadget(String titulo, String valor) {
        Label tituloLbl = new Label(titulo);
        tituloLbl.addStyleName(ValoTheme.LABEL_SMALL);
        tituloLbl.addStyleName(ValoTheme.LABEL_COLORED);
        tituloLbl.addStyleName(ValoTheme.LABEL_BOLD);
        tituloLbl.setIcon(FontAwesome.MONEY);
        tituloLbl.setSizeUndefined();

        Label valorLbl = new Label(valor);
        valorLbl.addStyleName(ValoTheme.LABEL_H4);
        valorLbl.addStyleName("label-kpi");
        valorLbl.setSizeUndefined();

        VerticalLayout card = new VerticalLayout(tituloLbl, valorLbl);
        card.setWidth("200px");
        card.setHeightUndefined();
        card.setSpacing(false);
        card.setMargin(false);   // el padding lo controla .kpi-card en CSS
        card.addStyleName("rcorners3");
        card.addStyleName("kpi-card");

        return card;
    }

    private double getTotalFacturasPorPagar() {

        double total = 0;

        queryString = " SELECT * ";
        queryString += " FROM contabilidad_partida";
        queryString += " WHERE IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
        queryString += " AND   Upper(TipoDocumento) IN ('FACTURA')";
        queryString += " AND   IdNomenclatura = " + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getProveedores();
//        queryString += " AND   MontoAutorizadoPagar = 0 ";
//        queryString += " AND   MontoAplicarAnticipo = 0 ";
        queryString += " AND   Estatus <> 'ANULADO'";
        queryString += " AND   IdLiquidacion = 0";

        try {

            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);
            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();

            if (rsRecords.next()) {

                do {

                    queryString = " SELECT ";
                    queryString += " SUM(HABER - DEBE) TOTALSALDO, SUM(HaberQuetzales - DebeQuetzales) TOTALSALDOQ ";
                    queryString += " FROM contabilidad_partida";
                    queryString += " WHERE IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
                    queryString += " AND CodigoCC = '" + rsRecords.getString("CodigoCC") + "'";
                    queryString += " AND contabilidad_partida.IdNomenclatura = " + rsRecords.getString("IdNomenclatura");
                    queryString += " AND contabilidad_partida.Estatus <> 'ANULADO'";

//                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, queryString);

                    rsRecords1 = stQuery1.executeQuery(queryString);

                    if (rsRecords1.next()) {
                        if (rsRecords1.getDouble("TOTALSALDO") > 0.00) {
                            total += rsRecords1.getDouble("TOTALSALDO");
                            if(proveedorSaldo.get(rsRecords.getString("NombreProveedor")) != null) {
                                proveedorSaldo.replace(rsRecords.getString("NombreProveedor"), (proveedorSaldo.get(rsRecords.getString("NombreProveedor")) + rsRecords1.getDouble("TOTALSALDOQ")));
                            }
                            else {
                                proveedorSaldo.put(rsRecords.getString("NombreProveedor"), rsRecords1.getDouble("TOTALSALDOQ"));
                            }
                        } // saldo = 0.00
                    }//no tiene cuenta corriente

                } while (rsRecords.next());

            }

        } catch (Exception ex) {
            System.out.println("Error al buscar información para dashboard: " + ex.getMessage());
            ex.printStackTrace();
        }
        return total;
    }

    private double getTotalFormulariosPorPagar() {
        double total = 0;

        queryString = " SELECT * ";
        queryString += " FROM contabilidad_partida";
        queryString += " WHERE IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
        queryString += " AND   Upper(TipoDocumento) IN ('RECIBO','RECIBO CONTABLE','RECIBO CORRIENTE','FORMULARIO IVA',";
        queryString += " 'FORMULARIO ISR', 'FORMULARIO ISR RETENIDO', 'FORMULARIO ISO', 'FORMULARIO RECTIFICACION', 'FORMULARIO ISR OPCIONAL MENSUAL')";
        queryString += " AND   IdNomenclatura = " + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getLiquidacionesCajaChicha();
//        queryString += " AND   MontoAutorizadoPagar = 0 ";
//        queryString += " AND   MontoAplicarAnticipo = 0 ";
        queryString += " AND   Estatus <> 'ANULADO'";

        try {

            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);
            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();

            if (rsRecords.next()) {

                do {

                    queryString = " SELECT ";
                    queryString += " SUM(HABER - DEBE) TOTALSALDO, SUM(HaberQuetzales - DebeQuetzales) TOTALSALDOQ ";
                    queryString += " FROM contabilidad_partida";
                    queryString += " WHERE IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
                    queryString += " AND CodigoCC = '" + rsRecords.getString("CodigoCC") + "'";
                    queryString += " AND contabilidad_partida.IdNomenclatura = " + rsRecords.getString("IdNomenclatura");
                    queryString += " AND contabilidad_partida.Estatus <> 'ANULADO'";

//                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, queryString);

                    rsRecords1 = stQuery1.executeQuery(queryString);

                    if (rsRecords1.next()) {
                        if (rsRecords1.getDouble("TOTALSALDO") > 0.00) {
                            total += rsRecords1.getDouble("TOTALSALDO");
                        } // saldo = 0.00
                    }//no tiene cuenta corriente

                } while (rsRecords.next());

            }

        } catch (Exception ex) {
            System.out.println("Error al buscar información para dashboard: " + ex.getMessage());
            ex.printStackTrace();
        }
        return total;
    }

    private double getTotalCajaChicaPorLiquidar() {

        double total = 0;

        queryString = " SELECT * ";
        queryString += " FROM contabilidad_partida";
        queryString += " WHERE IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
        queryString += " AND   Upper(TipoDocumento) IN ('FACTURA')";
        queryString += " AND   IdNomenclatura = " + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getLiquidacionesCajaChicha();
//        queryString += " AND   MontoAutorizadoPagar = 0 ";
//        queryString += " AND   MontoAplicarAnticipo = 0 ";
        queryString += " AND   Estatus <> 'ANULADO'";
        queryString += " AND   IdLiquidacion > 0";

        try {

            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);
            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();

            if (rsRecords.next()) {

                do {

                    queryString = " SELECT ";
                    queryString += " SUM(HABER - DEBE) TOTALSALDO, SUM(HaberQuetzales - DebeQuetzales) TOTALSALDOQ ";
                    queryString += " FROM contabilidad_partida";
                    queryString += " WHERE IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
                    queryString += " AND CodigoCC = '" + rsRecords.getString("CodigoCC") + "'";
                    queryString += " AND contabilidad_partida.IdNomenclatura = " + rsRecords.getString("IdNomenclatura");
                    queryString += " AND contabilidad_partida.Estatus <> 'ANULADO'";

//                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, queryString);

                    rsRecords1 = stQuery1.executeQuery(queryString);

                    if (rsRecords1.next()) {
                        if (rsRecords1.getDouble("TOTALSALDO") > 0.00) {
                            total += rsRecords1.getDouble("TOTALSALDO");
                        } // saldo = 0.00
                    }//no tiene cuenta corriente

                } while (rsRecords.next());

            }

        } catch (Exception ex) {
            System.out.println("Error al buscar información para dashboard: " + ex.getMessage());
            ex.printStackTrace();
        }
        return total;
    }

    private double getTotalFacturasPorCobrarr() {
        double total = 0;

        queryString = " SELECT * ";
        queryString += " FROM contabilidad_partida";
        queryString += " WHERE IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
        queryString += " AND   Upper(TipoDocumento) IN ('FACTURA VENTA')";
        queryString += " AND   IdNomenclatura = " + ((SopdiUI) UI.getCurrent()).cuentasContablesDefault.getClientes();
        queryString += " AND   Estatus <> 'ANULADO'";

        try {

            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);
            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();

            if (rsRecords.next()) {

                do {

                    queryString = " SELECT ";
                    queryString += " SUM(DEBE - HABER) TOTALSALDO, SUM(DebeQuetzales - HaberQuetzales) TOTALSALDOQ ";
                    queryString += " FROM contabilidad_partida";
                    queryString += " WHERE IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
                    queryString += " AND CodigoCC = '" + rsRecords.getString("CodigoCC") + "'";
                    queryString += " AND contabilidad_partida.IdNomenclatura = " + rsRecords.getString("IdNomenclatura");
                    queryString += " AND contabilidad_partida.Estatus <> 'ANULADO'";

//                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, queryString);

                    rsRecords1 = stQuery1.executeQuery(queryString);

                    if (rsRecords1.next()) {
                        if (rsRecords1.getDouble("TOTALSALDO") > 0.00) {
                            total += rsRecords1.getDouble("TOTALSALDO");
                        } // saldo = 0.00
                    }//no tiene cuenta corriente

                } while (rsRecords.next());

            }

        } catch (Exception ex) {
            System.out.println("Error al buscar información para dashboard: " + ex.getMessage());
            ex.printStackTrace();
        }
        return total;
    }

    private void createBottomGraphs() {

        EChartsComponent pie = new EChartsComponent();
        EChartsComponent line = new EChartsComponent();

        // Ancho fijo razonable para que ambas quepan lado a lado y el wrapping
        // las apile verticalmente en pantallas pequeñas. La altura se mantiene fija
        // porque ECharts la necesita para renderizar el canvas.
        pie.setWidth("560px");
        pie.setHeight("380px");

        line.setWidth("560px");
        line.setHeight("380px");

        pie.setOptionJson(pieOption());
        line.setOptionJson(lineChartOption());

        bottomLayout.addComponents(pie, line);
    }

    private String pieOption() {

        StringBuilder sb = new StringBuilder();
        sb.append("[");

        Map<String, Double> top10 =
                proveedorSaldo.entrySet()
                        .stream()
                        .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                        .limit(5)
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (e1, e2) -> e1,
                                LinkedHashMap::new   // mantiene el orden!
                        ));

        boolean first = true;
        for (Map.Entry<String, Double> e : top10.entrySet()) {
            if (!first) sb.append(",");
            first = false;

            sb.append("{")
                    .append("\"name\":\"").append(e.getKey()).append("\",")
                    .append("\"value\":").append(e.getValue())
                    .append("}");
        }

        sb.append("]");

//System.out.println("sb=" + sb.toString());

        return "{"
                + "\"title\":{\"text\":\"Distribución Facturas Por Pagar\",\"left\":\"center\"},"
                + "\"tooltip\":{\"trigger\":\"item\"},"
                + "\"series\":[{"
                + "  \"type\":\"pie\","
                + "  \"radius\":\"50%\","
                + "  \"data\":" + sb.toString()
                + "}]"
                + "}";
    }

    private String barOption() {
        return "{"
                + "\"title\":{\"text\":\"Ventas Mensuales\"},"
                + "\"tooltip\":{\"trigger\":\"item\"},"
                + "\"xAxis\":{\"type\":\"category\",\"data\":[\"Ene\",\"Feb\",\"Mar\",\"Abr\",\"May\"]},"
                + "\"yAxis\":{\"type\":\"value\"},"
                + "\"series\":[{"
                + "  \"type\":\"bar\","
                + "  \"data\":[120,200,150,80,70]"
                + "}]"
                + "}";
    }

    private String lineChartOption() {
        return "{"
                + "\"title\":{\"text\":\"Ordenes de Compra\"},"
                + "\"xAxis\":{\"type\":\"category\",\"data\":[\"Ene\",\"Feb\",\"Mar\",\"Abr\",\"May\",\"Jun\",\"Ago\",\"Sep\",\"Oct\",\"Nov\",\"Dic\"]},"
                + "\"yAxis\":{\"type\":\"value\"},"
                + "\"tooltip\":{\"trigger\":\"item\"},"
                + "\"legend\":{\"data\":[\"2025\", \"2026\"]"
                + "},"
                + "\"series\":[{"
                + "  \"type\":\"line\","
                + "  \"data\":[10000,15000,15000,20000,40000,50000,20000,30000,30000,40000,40000,60000]"
                + "  },"
                + "  {"
                + "  \"type\":\"line\","
                + "  \"data\":[15000,25000,15000,20000,40000,60000,20000,20000,20000,40000,20000,60000]"
                + "  }]"
                + "}";
    }
/*
                + "\"tooltip\":{"
                + "  \"trigger:\" 'axis'"
                + "},"

                + "\"legend:\" {"
                + "  \"data:\" ['2025', '2026']"
                + "},"

 */
    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - Dashboard");
    }

}