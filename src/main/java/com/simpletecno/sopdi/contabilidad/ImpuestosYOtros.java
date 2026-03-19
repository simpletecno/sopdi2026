/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.contabilidad;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.tesoreria.PagarView;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 *
 * @author joseaguirre
 */
public class ImpuestosYOtros extends VerticalLayout implements View {
    
    public static String cuentaIvaPorPagar = "21103003";
    public static String cuentaIvaPorCobrar = "11201002";
    public static String cuentaIsrPorPagar = "21103002";
    public static String cuentaRetencionesIsrPorPagar = "21103007";
    public static String cuentaIsoPorPagar = "21103004";
    public static String cuentaIusiPorPagar = "21103001";
    public static String cuentaCuotaPatronalIgss = "21103005";
    public static String cuentaCuotaLaboralIgss = "21103006";

    Statement stQuery;
    ResultSet rsRecords;
    String queryString;
    
    VerticalLayout mainLayout = new VerticalLayout();
    Table unitTable = new Table();

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public ImpuestosYOtros() {
        setWidth("70%");
        setHeight("95%");
        
        mainLayout.setWidth("100%");
        mainLayout.addStyleName("rcorners2");
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);
        
        addComponent(mainLayout);

        Label titleLbl = new Label(empresaId + " " + empresaNombre + " IMPUESTOS/IGSS");
        //   titleLbl.setWidth("10%");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        //  titleLbl.addStyleName("h2_custom");

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setResponsive(true);
        titleLayout.setSpacing(true);
        titleLayout.setWidth("100%");
        titleLayout.setMargin(false);
        titleLayout.addComponents(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.MIDDLE_CENTER);
        titleLayout.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

        mainLayout.addComponent(titleLayout);
        mainLayout.setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        createTable();
        fillData();
    }
    
    private void createTable() {
        VerticalLayout layout = new VerticalLayout();
        layout.setWidth("80%");
        layout.addStyleName("rcorners3");
        layout.setMargin(true);
        layout.setSpacing(true);
        
        unitTable.setPageLength(25);
        unitTable.setWidth("100%");
        unitTable.addStyleName(ValoTheme.TABLE_BORDERLESS);
//        unitTable.addStyleName(ValoTheme.TABLE_NO_HEADER);
        unitTable.addStyleName(ValoTheme.TABLE_NO_STRIPES);

        unitTable.addContainerProperty("CUENTA", Label.class, "");
        unitTable.addContainerProperty("DEBE",   String.class, "");
        unitTable.addContainerProperty("HABER",  String.class, "");

        unitTable.setColumnAlignments(new Table.Align[]{Table.Align.RIGHT, Table.Align.RIGHT, Table.Align.RIGHT});
        
        Panel panel = new Panel();
        panel.setContent(unitTable);

        layout.addComponent(panel);
        layout.setComponentAlignment(panel, Alignment.MIDDLE_CENTER);
        
        mainLayout.addComponent(layout);
        mainLayout.setComponentAlignment(layout, Alignment.BOTTOM_CENTER);
    }
    
    private void fillData() {
        
        unitTable.removeAllItems();
        
        queryString = " SELECT IfNull(contabilidad_nomenclatura_empresa.N5, 'IVA POR PAGAR') AS NombreCuenta, ";
        queryString += " IfNull(Sum(contabilidad_partida.DebeQuetzales), 0) TotalDebe, IfNull(Sum(contabilidad_partida.HaberQuetzales),0) TotalHaber ";
        queryString += " FROM contabilidad_partida, contabilidad_nomenclatura_empreesa ";
        queryString += " WHERE contabilidad_partida.IdNomenclatura = contabilidad_nomenclatura_empresa.IdNomenclatura ";
        queryString += " AND contabilidad_nomenclatura_empresa.NoCuenta = '" + cuentaIvaPorPagar + "'";
        queryString += " AND contabilidad_partida.IdEmpresa = " + empresaId;
        queryString += " AND contabilidad_nomenclatura_empresa.IdEmpresa = " + empresaId;
        
//System.out.println("queryString=" + queryString);

        try {

            rsRecords = stQuery.executeQuery(queryString);
            rsRecords.next();
            
            Label ivaPorPagarLbl = new Label("<strong>" + cuentaIvaPorPagar + " " + rsRecords.getString("NombreCuenta") + "</strong>");
            ivaPorPagarLbl.setWidth("100%");
            ivaPorPagarLbl.setContentMode(ContentMode.HTML);
            ivaPorPagarLbl.addStyleName("aladerecha");

            unitTable.addItem(new Object[]{ivaPorPagarLbl, PagarView.numberFormat.format(rsRecords.getDouble("TotalDebe")), PagarView.numberFormat.format(rsRecords.getDouble("TotalHaber"))}, 1);

            Label ivaPorPagarSaldoLbl = new Label("<strong>Saldo</strong>");
            ivaPorPagarSaldoLbl.setWidth("100%");
            ivaPorPagarSaldoLbl.setContentMode(ContentMode.HTML);
            ivaPorPagarSaldoLbl.addStyleName("aladerecha");

            unitTable.addItem(new Object[]{ivaPorPagarSaldoLbl, "", PagarView.numberFormat.format((rsRecords.getDouble("TotalHaber") - rsRecords.getDouble("TotalDebe")))}, 2);
            unitTable.addItem(new Object[]{new Label(), "", ""}, 3);

            queryString = " SELECT IfNull(contabilidad_nomenclatura.N5, 'Iva Por Cobrar') As NombreCuenta, ";
            queryString += " IfNull(Sum(contabilidad_partida.DebeQuetzales), 0) TotalDebe, IfNull(Sum(contabilidad_partida.HaberQuetzales),0) TotalHaber ";
            queryString += " FROM contabilidad_partida, contabilidad_nomenclatura_empresa ";
            queryString += " WHERE contabilidad_partida.IdNomenclatura = contabilidad_nomenclatura_empresa.IdNomenclatura ";
            queryString += " AND contabilidad_nomenclatura_empresa.NoCuenta = '" + cuentaIvaPorCobrar + "'";
            queryString += " AND contabilidad_partida.IdEmpresa = " + empresaId;
            queryString += " AND contabilidad_nomenclatura_empresa.IdEmpresa = " + empresaId;
            
//System.out.println("queryString=" + queryString);

            rsRecords = stQuery.executeQuery(queryString);
            rsRecords.next();

            Label ivaPorCobrarLbl = new Label("<strong>" + cuentaIvaPorPagar + " " + rsRecords.getString("NombreCuenta") + "</strong>");
            ivaPorCobrarLbl.setWidth("100%");
            ivaPorCobrarLbl.setContentMode(ContentMode.HTML);
            ivaPorCobrarLbl.addStyleName("aladerecha");

            unitTable.addItem(new Object[]{ivaPorCobrarLbl, PagarView.numberFormat.format(rsRecords.getDouble("TotalDebe")), PagarView.numberFormat.format(rsRecords.getDouble("TotalHaber"))}, 4);

            Label ivaPorCobrarSaldoLbl = new Label("<strong>Saldo</strong>");
            ivaPorCobrarSaldoLbl.setWidth("100%");
            ivaPorCobrarSaldoLbl.setContentMode(ContentMode.HTML);
            ivaPorCobrarSaldoLbl.addStyleName("aladerecha");

            unitTable.addItem(new Object[]{ivaPorCobrarSaldoLbl, PagarView.numberFormat.format((rsRecords.getDouble("TotalDebe") - rsRecords.getDouble("TotalHaber"))),""}, 5);
            unitTable.addItem(new Object[]{new Label(), "", ""}, 6);

            queryString = " SELECT IfNull(contabilidad_nomenclatura_empresa.N5, 'ISR Por Pagar') As NombreCuenta, ";
            queryString += " IfNull(Sum(contabilidad_partida.DebeQuetzales), 0) TotalDebe, IfNull(Sum(contabilidad_partida.HaberQuetzales),0) TotalHaber ";
            queryString += " FROM contabilidad_partida, contabilidad_nomenclatura_empresa ";
            queryString += " WHERE contabilidad_partida.IdNomenclatura = contabilidad_nomenclatura_empresa.IdNomenclatura ";
            queryString += " AND contabilidad_nomenclatura_empresa.NoCuenta = '" + cuentaIsrPorPagar + "'";
            queryString += " AND contabilidad_partida.IdEmpresa = " + empresaId;
            queryString += " AND contabilidad_nomenclatura_empresa.IdEmpresa = " + empresaId;
            
//System.out.println("queryString=" + queryString);

            rsRecords = stQuery.executeQuery(queryString);
            rsRecords.next();

            Label isrPorPagarLbl = new Label("<strong>" + cuentaIsrPorPagar + " " + rsRecords.getString("NombreCuenta") + "</strong>");
            isrPorPagarLbl.setWidth("100%");
            isrPorPagarLbl.setContentMode(ContentMode.HTML);
            isrPorPagarLbl.addStyleName("aladerecha");

            unitTable.addItem(new Object[]{isrPorPagarLbl, PagarView.numberFormat.format(rsRecords.getDouble("TotalDebe")), PagarView.numberFormat.format(rsRecords.getDouble("TotalHaber"))}, 7);

            Label isrPorPagarSaldoLbl = new Label("<strong>Saldo</strong>");
            isrPorPagarSaldoLbl.setWidth("100%");
            isrPorPagarSaldoLbl.setContentMode(ContentMode.HTML);
            isrPorPagarSaldoLbl.addStyleName("aladerecha");

            unitTable.addItem(new Object[]{isrPorPagarSaldoLbl, "", PagarView.numberFormat.format((rsRecords.getDouble("TotalDebe") - rsRecords.getDouble("TotalHaber")))}, 8);
            unitTable.addItem(new Object[]{new Label(), "", ""}, 9);

            queryString = " Select IfNull(contabilidad_nomenclatura_empresa.N5, 'Retenciones ISR por Pagar') As NombreCuenta, ";
            queryString += " IfNull(Sum(contabilidad_partida.DebeQuetzales), 0) TotalDebe, IfNull(Sum(contabilidad_partida.HaberQuetzales),0) TotalHaber ";
            queryString += " From contabilidad_partida, contabilidad_nomenclatura_empresa ";
            queryString += " Where contabilidad_partida.IdNomenclatura = contabilidad_nomenclatura_empresa.IdNomenclatura ";
            queryString += " And contabilidad_nomenclatura_empresa.NoCuenta = '" + cuentaRetencionesIsrPorPagar + "'";
            queryString += " And contabilidad_partida.IdEmpresa = " + empresaId;
            queryString += " And contabilidad_nomenclatura_empresa.IdEmpresa = " + empresaId;
            
//System.out.println("queryString=" + queryString);

            rsRecords = stQuery.executeQuery(queryString);
            rsRecords.next();

            Label retencionesIsrPorPagarLbl = new Label("<strong>" + cuentaRetencionesIsrPorPagar + " " + rsRecords.getString("NombreCuenta") + "</strong>");
            retencionesIsrPorPagarLbl.setWidth("100%");
            retencionesIsrPorPagarLbl.setContentMode(ContentMode.HTML);
            retencionesIsrPorPagarLbl.addStyleName("aladerecha");

            unitTable.addItem(new Object[]{retencionesIsrPorPagarLbl, PagarView.numberFormat.format(rsRecords.getDouble("TotalDebe")), PagarView.numberFormat.format(rsRecords.getDouble("TotalHaber"))}, 10);

            Label retencionesIsrPorPagarSaldoLbl = new Label("<strong>Saldo</strong>");
            retencionesIsrPorPagarSaldoLbl.setWidth("100%");
            retencionesIsrPorPagarSaldoLbl.setContentMode(ContentMode.HTML);
            retencionesIsrPorPagarSaldoLbl.addStyleName("aladerecha");

            unitTable.addItem(new Object[]{retencionesIsrPorPagarSaldoLbl, "", PagarView.numberFormat.format((rsRecords.getDouble("TotalDebe") - rsRecords.getDouble("TotalHaber")))}, 11);
            unitTable.addItem(new Object[]{new Label(), "", ""}, 12);

            queryString = " SELECT IfNull(contabilidad_nomenclatura_empresa.N5, 'Iso Por Pagar') As NombreCuenta, ";
            queryString += " IfNull(Sum(contabilidad_partida.DebeQuetzales), 0) TotalDebe, IfNull(Sum(contabilidad_partida.HaberQuetzales),0) TotalHaber ";
            queryString += " FROM contabilidad_partida, contabilidad_nomenclatura_empresa ";
            queryString += " WHERE contabilidad_partida.IdNomenclatura = contabilidad_nomenclatura_empresa.IdNomenclatura ";
            queryString += " AND contabilidad_nomenclatura_empresa.NoCuenta = '" + cuentaIsoPorPagar + "'";
            queryString += " AND contabilidad_partida.IdEmpresa = " + empresaId;
            queryString += " AND contabilidad_nomenclatura_empresa.IdEmpresa = " + empresaId;
            
//System.out.println("queryString=" + queryString);

            rsRecords = stQuery.executeQuery(queryString);
            rsRecords.next();

            Label isoPorPagarLbl = new Label("<strong>" + cuentaIsoPorPagar + " " + rsRecords.getString("NombreCuenta") + "</strong>");
            isoPorPagarLbl.setWidth("100%");
            isoPorPagarLbl.setContentMode(ContentMode.HTML);
            isoPorPagarLbl.addStyleName("aladerecha");

            unitTable.addItem(new Object[]{isoPorPagarLbl, PagarView.numberFormat.format(rsRecords.getDouble("TotalDebe")), PagarView.numberFormat.format(rsRecords.getDouble("TotalHaber"))}, 13);

            Label isoPorPagarSaldoLbl = new Label("<strong>Saldo</strong>");
            isoPorPagarSaldoLbl.setWidth("100%");
            isoPorPagarSaldoLbl.setContentMode(ContentMode.HTML);
            isoPorPagarSaldoLbl.addStyleName("aladerecha");

            unitTable.addItem(new Object[]{isoPorPagarSaldoLbl, "", PagarView.numberFormat.format((rsRecords.getDouble("TotalDebe") - rsRecords.getDouble("TotalHaber")))}, 14);
            unitTable.addItem(new Object[]{new Label(), "", ""}, 15);

            queryString = " SELECT IfNull(contabilidad_nomenclatura_empresa.N5, 'Iusi Por Pagar') As NombreCuenta, ";
            queryString += " IfNull(Sum(contabilidad_partida.DebeQuetzales), 0) TotalDebe, IfNull(Sum(contabilidad_partida.HaberQuetzales),0) TotalHaber ";
            queryString += " FROM contabilidad_partida, contabilidad_nomenclatura_empresa ";
            queryString += " WHERE contabilidad_partida.IdNomenclatura = contabilidad_nomenclatura_empresa.IdNomenclatura ";
            queryString += " AND contabilidad_nomenclatura_empresa.NoCuenta = '" + cuentaIusiPorPagar + "'";
            queryString += " AND contabilidad_partida.IdEmpresa = " + empresaId;
            queryString += " AND contabilidad_nomenclatura_empresa.IdEmpresa = " + empresaId;
            
//System.out.println("queryString=" + queryString);

            rsRecords = stQuery.executeQuery(queryString);
            rsRecords.next();

            Label iusiPorPagarLbl = new Label("<strong>" + cuentaIusiPorPagar + " " + rsRecords.getString("NombreCuenta") + "</strong>");
            iusiPorPagarLbl.setWidth("100%");
            iusiPorPagarLbl.setContentMode(ContentMode.HTML);
            iusiPorPagarLbl.addStyleName("aladerecha");

            unitTable.addItem(new Object[]{iusiPorPagarLbl, PagarView.numberFormat.format(rsRecords.getDouble("TotalDebe")), PagarView.numberFormat.format(rsRecords.getDouble("TotalHaber"))}, 16);
            
            Label iusiPorPagarSaldoLbl = new Label("<strong>Saldo</strong>");
            iusiPorPagarSaldoLbl.setWidth("100%");
            iusiPorPagarSaldoLbl.setContentMode(ContentMode.HTML);
            iusiPorPagarSaldoLbl.addStyleName("aladerecha");

            unitTable.addItem(new Object[]{iusiPorPagarSaldoLbl, "", PagarView.numberFormat.format((rsRecords.getDouble("TotalDebe") - rsRecords.getDouble("TotalHaber")))}, 17);
            unitTable.addItem(new Object[]{new Label(), "", ""}, 18);

            queryString = " SELECT IfNull(contabilidad_nomenclatura_empresa.N5, 'Cuota Patronal Igss') As NombreCuenta, ";
            queryString += " IfNull(Sum(contabilidad_partida.DebeQuetzales), 0) TotalDebe, IfNull(Sum(contabilidad_partida.HaberQuetzales),0) TotalHaber ";
            queryString += " FROM contabilidad_partida, contabilidad_nomenclatura_empresa ";
            queryString += " WHERE contabilidad_partida.IdNomenclatura = contabilidad_nomenclatura_empresa.IdNomenclatura ";
            queryString += " AND contabilidad_nomenclatura_empresa.NoCuenta = '" + cuentaCuotaPatronalIgss + "'";
            queryString += " AND contabilidad_partida.IdEmpresa = " + empresaId;
            queryString += " AND contabilidad_nomenclatura_empresa.IdEmpresa = " + empresaId;
            
//System.out.println("queryString=" + queryString);

            rsRecords = stQuery.executeQuery(queryString);
            rsRecords.next();

            Label cuotaPatronalIgssPorPagarLbl = new Label("<strong>" + cuentaIsoPorPagar + " " + rsRecords.getString("NombreCuenta") + "</strong>");
            cuotaPatronalIgssPorPagarLbl.setWidth("100%");
            cuotaPatronalIgssPorPagarLbl.setContentMode(ContentMode.HTML);
            cuotaPatronalIgssPorPagarLbl.addStyleName("aladerecha");

            unitTable.addItem(new Object[]{cuotaPatronalIgssPorPagarLbl, PagarView.numberFormat.format(rsRecords.getDouble("TotalDebe")), PagarView.numberFormat.format(rsRecords.getDouble("TotalHaber"))}, 19);
            
            Label cuotaPatronalIgssPorPagarSaldoLbl = new Label("<strong>Saldo</strong>");
            cuotaPatronalIgssPorPagarSaldoLbl.setWidth("100%");
            cuotaPatronalIgssPorPagarSaldoLbl.setContentMode(ContentMode.HTML);
            cuotaPatronalIgssPorPagarSaldoLbl.addStyleName("aladerecha");

            unitTable.addItem(new Object[]{cuotaPatronalIgssPorPagarSaldoLbl, "", PagarView.numberFormat.format((rsRecords.getDouble("TotalHaber") - rsRecords.getDouble("TotalDebe")))}, 20);
            unitTable.addItem(new Object[]{new Label(), "", ""}, 21);

            queryString = " Select IfNull(contabilidad_nomenclatura_empresa.N5, 'Cuota Laboral Igss') As NombreCuenta, ";
            queryString += " IfNull(Sum(contabilidad_partida.DebeQuetzales), 0) TotalDebe, IfNull(Sum(contabilidad_partida.HaberQuetzales),0) TotalHaber ";
            queryString += " From contabilidad_partida, contabilidad_nomenclatura_empresa ";
            queryString += " Where contabilidad_partida.IdNomenclatura = contabilidad_nomenclatura_empresa.IdNomenclatura ";
            queryString += " And contabilidad_nomenclatura_empresa.NoCuenta = '" + cuentaCuotaLaboralIgss + "'";
            queryString += " And contabilidad_partida.IdEmpresa = " + empresaId;
            queryString += " And contabilidad_nomenclatura_empresa.IdEmpresa = " + empresaId;
            
//System.out.println("queryString=" + queryString);

            rsRecords = stQuery.executeQuery(queryString);
            rsRecords.next();

            Label cuotaLaboralIgssPorPagarLbl = new Label("<strong>" + cuentaCuotaLaboralIgss + " " + rsRecords.getString("NombreCuenta") + "</strong>");
            cuotaLaboralIgssPorPagarLbl.setWidth("100%");
            cuotaLaboralIgssPorPagarLbl.setContentMode(ContentMode.HTML);
            cuotaLaboralIgssPorPagarLbl.addStyleName("aladerecha");

            unitTable.addItem(new Object[]{cuotaLaboralIgssPorPagarLbl, PagarView.numberFormat.format(rsRecords.getDouble("TotalDebe")), PagarView.numberFormat.format(rsRecords.getDouble("TotalHaber"))}, 22);
            
            Label cuotaLaboralIgssPorPagarSaldoLbl = new Label("<strong>Saldo</strong>");
            cuotaLaboralIgssPorPagarSaldoLbl.setWidth("100%");
            cuotaLaboralIgssPorPagarSaldoLbl.setContentMode(ContentMode.HTML);
            cuotaLaboralIgssPorPagarSaldoLbl.addStyleName("aladerecha");

            unitTable.addItem(new Object[]{cuotaLaboralIgssPorPagarSaldoLbl, "", PagarView.numberFormat.format((rsRecords.getDouble("TotalHaber") - rsRecords.getDouble("TotalDebe")))}, 23);
            unitTable.addItem(new Object[]{new Label(), "", ""}, 24);

          } catch (Exception ex1) {
            System.out.println("Error al listar impuetos/igss: " + ex1.getMessage());
            ex1.printStackTrace();
        }     
        
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - Impuestos");
    }

}
