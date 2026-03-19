/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simpletecno.sopdi.contabilidad;

import com.simpletecno.sopdi.SopdiUI;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import java.sql.ResultSet;
import java.sql.Statement;
import org.vaadin.ui.NumberField;

/**
 *
 * @author user
 */
public class SaldosCuentasForm extends Window {

    UI mainUI;
    Statement stQuery;
    ResultSet rsRecords;
    String idNomenclatura;
    VerticalLayout contentLayout;

    NumberField debeTxt;
    NumberField haberTxt;

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public SaldosCuentasForm(String idNomenclatura) {
        this.idNomenclatura = idNomenclatura;
        this.mainUI = UI.getCurrent();

        setResponsive(true);
        setWidth("50%");
        setHeight("30%");

        contentLayout = new VerticalLayout();
        contentLayout.setSpacing(true);
        contentLayout.setMargin(true);

        Label titleLbl = new Label(empresaId + " " + empresaNombre +  " SALDOS DE CUENTAS");
        titleLbl.addStyleName(ValoTheme.LABEL_H1);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h1_custom");

        contentLayout.addComponents(titleLbl);

        contentLayout.setComponentAlignment(titleLbl, Alignment.TOP_CENTER);
        crearCampos();
        buscarSaldos();
        setContent(contentLayout);
    }

    public void buscarSaldos() {
        String queryString = "";
        queryString = "  SELECT SUM(DebeQuetzales) AS DEBEQ, SUM(HaberQuetzales) AS HABERQ";
        queryString += " FROM contabilidad_partida";
        queryString += " WHERE IdNomenclatura = " + idNomenclatura;

        try {
            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);
            while (rsRecords.next()) {
                if (rsRecords.getDouble("DEBEQ") == 0.00 && rsRecords.getDouble("HABERQ") == 0.00) {
                    debeTxt.setValue(0.00);
                    haberTxt.setValue(0.00);
                } else {
                    debeTxt.setValue(rsRecords.getString("DEBEQ"));
                    haberTxt.setValue(rsRecords.getString("HABERQ"));
                }

            }

        } catch (Exception ex) {
            System.out.println("Error al buscar saldos de cuentas" + ex);
        }
    }

    public void crearCampos() {

        HorizontalLayout saldosLayout = new HorizontalLayout();
        saldosLayout.setSpacing(true);

        debeTxt = new NumberField("DEBE : ");
        debeTxt.setDecimalAllowed(true);
        debeTxt.setDecimalPrecision(2);
        debeTxt.setMinimumFractionDigits(2);
        debeTxt.setDecimalSeparator('.');
        debeTxt.setDecimalSeparatorAlwaysShown(true);
        debeTxt.setValue(0d);
        debeTxt.setGroupingUsed(true);
        debeTxt.setGroupingSeparator(',');
        debeTxt.setGroupingSize(3);
        debeTxt.setImmediate(true);
        debeTxt.addStyleName(ValoTheme.TEXTAREA_HUGE);
        debeTxt.setWidth("7em");

        haberTxt = new NumberField("HABER : ");
        haberTxt.setDecimalAllowed(true);
        haberTxt.setDecimalPrecision(2);
        haberTxt.setMinimumFractionDigits(2);
        haberTxt.setDecimalSeparator('.');
        haberTxt.setDecimalSeparatorAlwaysShown(true);
        haberTxt.setValue(0d);
        haberTxt.setGroupingUsed(true);
        haberTxt.setGroupingSeparator(',');
        haberTxt.setGroupingSize(3);
        haberTxt.setImmediate(true);
        haberTxt.addStyleName(ValoTheme.TEXTAREA_HUGE);
        haberTxt.setWidth("7em");

        saldosLayout.addComponent(debeTxt);
        saldosLayout.setComponentAlignment(debeTxt, Alignment.TOP_CENTER);
        saldosLayout.addComponent(haberTxt);
        saldosLayout.setComponentAlignment(haberTxt, Alignment.TOP_CENTER);

        contentLayout.addComponent(saldosLayout);
        contentLayout.setComponentAlignment(saldosLayout, Alignment.TOP_CENTER);

    }
}
