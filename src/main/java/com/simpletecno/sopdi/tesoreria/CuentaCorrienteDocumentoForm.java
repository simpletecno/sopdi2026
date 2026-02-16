package com.simpletecno.sopdi.tesoreria;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;

/**
 * @author user
 */
public class CuentaCorrienteDocumentoForm extends Window {

    VerticalLayout mainLayout;

    static final String FECHA_PROPERTY = "Fecha";
    static final String DESCRIPCION_PROPERTY = "Descripción";
    static final String MONEDA_PROPERTY = "Moneda";
    static final String DEBE_PROPERTY = "DEBE";
    static final String HABER_PROPERTY = "HABER";
    static final String CODIGO_PARTIDA_PROPERTY = "Codigo partida";
    static final String CODIGO_CC_PROPERTY = "CodigoCC";

    IndexedContainer ccContainer = new IndexedContainer();
    Grid ccGrid;

    Button salirBtn;

    Label saldoFacturaLbl = new Label("Saldo de la factura seleccionada : ");

    double saldoTotalLiquidar = 0.00;

    UI mainUI;
    Statement stQuery;
    ResultSet rsRecords;
    String queryString;

    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");

    public CuentaCorrienteDocumentoForm(String codigoCC) {
        this.mainUI = UI.getCurrent();
        setResponsive(true);
        setWidth("85%");
//        setHeight("60%");

        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setResponsive(true);

        HorizontalLayout layoutTitle = new HorizontalLayout();
        layoutTitle.setSpacing(true);
        layoutTitle.setMargin(new MarginInfo(false, true, false, true));
        layoutTitle.setWidth("100%");

        Label titleLbl = new Label("Cuenta Corriente de Documento");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h2_custom");

        layoutTitle.addComponent(titleLbl);
        layoutTitle.setComponentAlignment(titleLbl, Alignment.TOP_CENTER);

        mainLayout.addComponent(layoutTitle);

        setContent(mainLayout);
        crearGridFactura();
        llenarGridFactura(codigoCC);

    }

    public void crearGridFactura() {

        HorizontalLayout documentosLayout = new HorizontalLayout();
        documentosLayout.setWidth("100%");
        documentosLayout.addStyleName("rcorners3");
        documentosLayout.setMargin(new MarginInfo(false, true, false, true));
        documentosLayout.setResponsive(true);

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setMargin(new MarginInfo(false, true, false, true));

        ccContainer.addContainerProperty(FECHA_PROPERTY, String.class, null);
        ccContainer.addContainerProperty(DESCRIPCION_PROPERTY, String.class, null);
        ccContainer.addContainerProperty(MONEDA_PROPERTY, String.class, null);
        ccContainer.addContainerProperty(DEBE_PROPERTY, String.class, null);
        ccContainer.addContainerProperty(HABER_PROPERTY, String.class, null);
        ccContainer.addContainerProperty(CODIGO_PARTIDA_PROPERTY, String.class, null);
        ccContainer.addContainerProperty(CODIGO_CC_PROPERTY, String.class, null);

        ccGrid = new Grid("CUENTA CORRIENTE ", ccContainer);

        ccGrid.setWidth("100%");
        ccGrid.setImmediate(true);
        ccGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        ccGrid.setHeightMode(HeightMode.ROW);
        ccGrid.setHeightByRows(10);
        ccGrid.setResponsive(true);
        ccGrid.setEditorBuffered(false);
        ccGrid.setResponsive(true);
        ccGrid.setEditorBuffered(false);

//        ccGrid.getColumn(CODIGO_PARTIDA_PROPERTY).setHidable(true).setHidden(true);
//        ccGrid.getColumn(CODIGO_CC_PROPERTY).setHidable(true).setHidden(true);
//        ccGrid.getColumn(DESCRIPCION_PROPERTY).setHidable(true).setHidden(true);
//        ccGrid.getColumn(FECHA_PROPERTY).setHidable(true).setHidden(true);

        ccGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {

            if (DEBE_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (HABER_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }

        });

        documentosLayout.addComponent(ccGrid);
        documentosLayout.setComponentAlignment(ccGrid, Alignment.TOP_CENTER);

        buttonsLayout.addComponents(saldoFacturaLbl);
        buttonsLayout.setComponentAlignment(saldoFacturaLbl, Alignment.BOTTOM_RIGHT);

        mainLayout.addComponent(documentosLayout);
        mainLayout.setComponentAlignment(documentosLayout, Alignment.TOP_CENTER);

        mainLayout.addComponent(buttonsLayout);
        mainLayout.setComponentAlignment(buttonsLayout, Alignment.TOP_RIGHT);
    }

    public void llenarGridFactura(String codigoCC) {
        ccContainer.removeAllItems();

        queryString = " SELECT * ";
        queryString += " FROM contabilidad_partida";
        queryString += " WHERE CODIGOCC = '" + codigoCC + "'";
        queryString += " AND   IdNomenclatura IN (" + ((SopdiUI) mainUI).cuentasContablesDefault.getProveedores() + "," + ((SopdiUI) mainUI).cuentasContablesDefault.getInstituciones() + ")";
        queryString += " ORDER by contabilidad_partida.CodigoPartida";

        double totalSaldo = 0.00;

        try {

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {

                String monedaSimbolo;

                do {

                    Object itemId = ccContainer.addItem();

                    ccContainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha")));
                    ccContainer.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(rsRecords.getString("Descripcion"));
                    ccContainer.getContainerProperty(itemId, MONEDA_PROPERTY).setValue(rsRecords.getString("MonedaDocumento"));

                    if (rsRecords.getString("MonedaDocumento").equals("QUETZALES")) {
                        monedaSimbolo = "Q.";
                    } else {
                        monedaSimbolo = "$.";
                    }
                    ccContainer.getContainerProperty(itemId, DEBE_PROPERTY).setValue(monedaSimbolo + numberFormat.format(rsRecords.getDouble("DEBE")));
                    ccContainer.getContainerProperty(itemId, HABER_PROPERTY).setValue(monedaSimbolo + numberFormat.format(rsRecords.getDouble("HABER")));
                    ccContainer.getContainerProperty(itemId, CODIGO_CC_PROPERTY).setValue(rsRecords.getString("CodigoCC"));
                    ccContainer.getContainerProperty(itemId, CODIGO_PARTIDA_PROPERTY).setValue(rsRecords.getString("CodigoPartida"));

                    totalSaldo += (rsRecords.getDouble("HABER") - rsRecords.getDouble("DEBE"));

                } while (rsRecords.next());

                saldoFacturaLbl.setValue("Saldo  = " + numberFormat.format(totalSaldo));

            }

        } catch (Exception ex) {
            System.out.println("Error al listar tabla Facturas en CUENTACORRIENTE: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
