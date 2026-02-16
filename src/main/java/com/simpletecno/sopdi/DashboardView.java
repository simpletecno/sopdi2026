package com.simpletecno.sopdi;

import com.simpletecno.sopdi.utilerias.EChartsComponent;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.icons.VaadinIcons;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DashboardView extends VerticalLayout implements View {

    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");
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

        setSizeUndefined();
        setSpacing(true);
        setDefaultComponentAlignment(Alignment.TOP_CENTER);

        Label titleLbl = new Label("Dashboard");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h2_custom");

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setMargin(new MarginInfo(false, true, false, true));
        titleLayout.setSpacing(true);
        titleLayout.setWidth("100%");

        titleLayout.addComponents(titleLbl, refrescarBtn);
        titleLayout.setComponentAlignment(titleLbl, Alignment.TOP_LEFT);
        titleLayout.setComponentAlignment(refrescarBtn, Alignment.BOTTOM_CENTER);

        addComponent(titleLayout);

        topLayout.setSpacing(true);
        topLayout.setMargin(true);
        topLayout.setDefaultComponentAlignment(Alignment.TOP_CENTER);
        topLayout.addStyleName(ValoTheme.LAYOUT_HORIZONTAL_WRAPPING);
        topLayout.addStyleName(ValoTheme.LAYOUT_CARD);
        topLayout.setWidth("100%");

        bottomLayout.setSpacing(true);
        bottomLayout.setMargin(true);
        bottomLayout.setDefaultComponentAlignment(Alignment.TOP_CENTER);
        bottomLayout.addStyleName(ValoTheme.LAYOUT_HORIZONTAL_WRAPPING);

        addComponents(topLayout, bottomLayout);

        createTopData();
        createBottomGraphs();

        refrescarBtn.setIcon(FontAwesome.REFRESH);
        refrescarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        refrescarBtn.addClickListener(e -> {
            topLayout.removeAllComponents();
            createTopData();
        });
    }

    private void createTopData() {
        topLayout.addComponents(
                createBadget("Facturas Por Pagar", numberFormat.format(getTotalFacturasPorPagar())),
                createBadget("Formularios por Pagar", numberFormat.format(getTotalFormulariosPorPagar())),
                createBadget("Caja Chica por Liquidar", numberFormat.format(getTotalCajaChicaPorLiquidar())),
                createBadget("Facturas por Cobrar", numberFormat.format(getTotalFacturasPorCobrarr()))
        );
    }

    private VerticalLayout createBadget(String titulo, String valor) {
        Label tituloLbl = new Label(titulo);
        tituloLbl.addStyleName(ValoTheme.LABEL_H4);
        tituloLbl.setSizeUndefined();
        tituloLbl.addStyleName(ValoTheme.LABEL_COLORED);
        tituloLbl.setIcon(FontAwesome.MONEY);

        Label valorLbl = new Label();
        valorLbl.addStyleName(ValoTheme.LABEL_H3);
        valorLbl.setSizeUndefined();
        valorLbl.setValue(valor);
//        valorLbl.addStyleName("mi-label-personalizado");
        valorLbl.addStyleName("label-kpi");

        VerticalLayout card = new VerticalLayout(tituloLbl, valorLbl);
        card.setSizeFull();
        card.setSpacing(true);
        card.setMargin(true);
//        card.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);
        card.addStyleName("rcorners3");

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
//        pie.setCaption("Top 10 saldo proveedores ");

//        EChartsComponent bar = new EChartsComponent();
//        bar.setCaption("Bar chart");

        pie.setOptionJson(pieOption());
//        bar.setOptionJson(barOption());

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

System.out.println("sb=" + sb.toString());

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