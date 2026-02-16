package com.simpletecno.sopdi.compras;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.SelectionEvent;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
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
public class DocumentosPorLiquidarView extends VerticalLayout implements View {

    static final String ID_PROVEEDOR_PROPERTY = "Id Proveedor";
    static final String PROVEEDOR_PROPERTY = "Proveedor";
    static final String FECHA_PROPERTY = "Fecha";
    static final String TIPO_DOCUMENTO_PROPERTY = "T. Documento";
    static final String DOCUMENTO_PROPERTY = "Documento";
    static final String DESCRIPCION_PROPERTY = "Descripción";
    static final String MONEDA_PROPERTY = "Moneda";
    static final String MONTO_PROPERTY = "Monto";
    static final String SALDO_PROPERTY = "Saldo";
    static final String CODIGO_PARTIDA_PROPERTY = "Codigo partida";
    static final String CODIGO_CC_PROPERTY = "CodigoCC";
    static final String DEBE_PROPERTY = "DEBE";
    static final String HABER_PROPERTY = "HABER";

    ComboBox empresaCbx;

    OptionGroup tipoDocumentoOg;

    IndexedContainer documentosContainer = new IndexedContainer();
    Grid documentosGrid;

    IndexedContainer partidasContainer = new IndexedContainer();
    Grid partidasGrid;
    
    UI mainUI;
    Statement stQuery, stQuery1;
    ResultSet rsRecords, rsRecords1;
    String queryString;
    
    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");

    public DocumentosPorLiquidarView() {
        this.mainUI = UI.getCurrent();
        setResponsive(true);
        setSpacing(true);

        HorizontalLayout layoutTitle = new HorizontalLayout();
        layoutTitle.setSpacing(true);
        layoutTitle.setMargin(new MarginInfo(false, true, false, true));
        layoutTitle.setWidth("100%");

        empresaCbx = new ComboBox("EMPRESA :");
        empresaCbx.setStyleName(ValoTheme.COMBOBOX_HUGE);
        empresaCbx.setWidth("90%");
        empresaCbx.setInvalidAllowed(false);
        empresaCbx.setNewItemsAllowed(false);
        empresaCbx.setTextInputAllowed(false);
        empresaCbx.setNullSelectionAllowed(false);
        llenarComboEmpresa();

        tipoDocumentoOg = new OptionGroup("Elija una opción");
        tipoDocumentoOg.addItems("FACTURAS", "ANTICIPOS");
        tipoDocumentoOg.select("FACTURAS");
        tipoDocumentoOg.addValueChangeListener(event -> {
            if (event.getProperty() == null) {
                return;
            }
            llenarGridDocumentos();
        });

        Label titleLbl = new Label("DOCUMENTOS POR LIQUIDAR");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h2_custom");

        layoutTitle.addComponent(tipoDocumentoOg);
        layoutTitle.setComponentAlignment(tipoDocumentoOg, Alignment.MIDDLE_LEFT);

        layoutTitle.addComponent(empresaCbx);
        layoutTitle.setComponentAlignment(empresaCbx, Alignment.MIDDLE_LEFT);

        layoutTitle.addComponent(titleLbl);
        layoutTitle.setComponentAlignment(titleLbl, Alignment.BOTTOM_RIGHT);

        addComponent(layoutTitle);

        crearGridDocumentos();
        llenarGridDocumentos();
        crearGridPartidas();

    }

    public void llenarComboEmpresa() {

        queryString = " SELECT * from contabilidad_empresa";
        queryString += " Where IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();

        try {

            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) { //  encontrado

                empresaCbx.addItem(rsRecords.getString("IdEmpresa"));
                empresaCbx.setItemCaption(rsRecords.getString("IdEmpresa"), rsRecords.getString("Empresa"));
            }
            rsRecords.first();

            empresaCbx.select(rsRecords.getString("IdEmpresa"));

        } catch (Exception ex1) {
            System.out.println("Error al listar empresas: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }
    
    public void crearGridDocumentos() {

        HorizontalLayout reportLayout = new HorizontalLayout();
        reportLayout.setWidth("100%");
        reportLayout.addStyleName("rcorners3");
        reportLayout.setResponsive(true);
        reportLayout.setMargin(new MarginInfo(false, true, false, true));

        documentosContainer.addContainerProperty(TIPO_DOCUMENTO_PROPERTY, String.class, null);
        documentosContainer.addContainerProperty(DOCUMENTO_PROPERTY, String.class, null);
        documentosContainer.addContainerProperty(ID_PROVEEDOR_PROPERTY, String.class, null);
        documentosContainer.addContainerProperty(PROVEEDOR_PROPERTY, String.class, null);
        documentosContainer.addContainerProperty(FECHA_PROPERTY, String.class, null);
        documentosContainer.addContainerProperty(DESCRIPCION_PROPERTY, String.class, null);
        documentosContainer.addContainerProperty(MONEDA_PROPERTY, String.class, null);
        documentosContainer.addContainerProperty(MONTO_PROPERTY, String.class, null);
        documentosContainer.addContainerProperty(SALDO_PROPERTY, String.class, null);
        documentosContainer.addContainerProperty(CODIGO_PARTIDA_PROPERTY, String.class, null);
        documentosContainer.addContainerProperty(CODIGO_CC_PROPERTY, String.class, null);

        documentosGrid = new Grid("Documentos pendientes de liquidar.", documentosContainer);

        documentosGrid.setImmediate(true);
        documentosGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        documentosGrid.setDescription("Seleccione un registro.");
        documentosGrid.setHeightMode(HeightMode.ROW);
        documentosGrid.setHeightByRows(10);
        documentosGrid.setWidth("100%");
        documentosGrid.setResponsive(true);
        documentosGrid.setEditorBuffered(false);
        documentosGrid.setSizeFull();
        documentosGrid.setEditorEnabled(true);
        documentosGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if (MONTO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (SALDO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }
        });
        documentosGrid.getColumn(CODIGO_PARTIDA_PROPERTY).setHidable(true).setHidden(true);
        documentosGrid.getColumn(CODIGO_CC_PROPERTY).setHidable(true).setHidden(true);

        documentosGrid.addSelectionListener(new SelectionEvent.SelectionListener() {
            @Override
            public void select(SelectionEvent event) {

                if (documentosGrid.getSelectedRow() != null) {
                    llenarTablaCC();
                }
            }
        });

        reportLayout.addComponent(documentosGrid);
        reportLayout.setComponentAlignment(documentosGrid, Alignment.TOP_CENTER);

        addComponent(reportLayout);
        setComponentAlignment(reportLayout, Alignment.TOP_CENTER);

    }

    private void setFooterTotal(String propertyId) {

        double total = 0.00;
        double montoCheque = 0.00;
        double utilizarAnticipos = 0.00;

        for (Object itemId : documentosContainer.getItemIds()) {
            Item item = documentosContainer.getItem(itemId);
            Object propertyValue = item.getItemProperty(propertyId).getValue();

            total += Double.valueOf(String.valueOf(propertyValue));
        }
    }

    public void crearGridPartidas() {

        HorizontalLayout reportLayout = new HorizontalLayout();
        reportLayout.setWidth("100%");
        reportLayout.addStyleName("rcorners3");
        reportLayout.setResponsive(true);
        reportLayout.setMargin(new MarginInfo(false, true, false, true));

        partidasContainer.addContainerProperty(TIPO_DOCUMENTO_PROPERTY, String.class, null);
        partidasContainer.addContainerProperty(DOCUMENTO_PROPERTY, String.class, null);
        partidasContainer.addContainerProperty(FECHA_PROPERTY, String.class, null);
        partidasContainer.addContainerProperty(DESCRIPCION_PROPERTY, String.class, null);
        partidasContainer.addContainerProperty(MONEDA_PROPERTY, String.class, null);
        partidasContainer.addContainerProperty(DEBE_PROPERTY, String.class, null);
        partidasContainer.addContainerProperty(HABER_PROPERTY, String.class, null);
        partidasContainer.addContainerProperty(CODIGO_PARTIDA_PROPERTY, String.class, null);
        partidasContainer.addContainerProperty(CODIGO_CC_PROPERTY, String.class, null);

        partidasGrid = new Grid("Cuenta Corriente.", partidasContainer);

        partidasGrid.setImmediate(true);
        partidasGrid.setSelectionMode(Grid.SelectionMode.NONE);
        partidasGrid.setDescription("Seleccione un registro.");
        partidasGrid.setHeightMode(HeightMode.ROW);
        partidasGrid.setHeightByRows(10);
        partidasGrid.setWidth("100%");
        partidasGrid.setResponsive(true);
        partidasGrid.setEditorBuffered(false);
        partidasGrid.setSizeFull();
        partidasGrid.setEditorEnabled(true);
        partidasGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if (DEBE_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (HABER_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }
        });
        partidasGrid.getColumn(CODIGO_PARTIDA_PROPERTY).setHidable(true).setHidden(true);
        partidasGrid.getColumn(CODIGO_CC_PROPERTY).setHidable(true).setHidden(true);

        reportLayout.addComponent(partidasGrid);
        reportLayout.setComponentAlignment(partidasGrid, Alignment.TOP_CENTER);

        addComponent(reportLayout);
        setComponentAlignment(reportLayout, Alignment.TOP_CENTER);

    }

    public void llenarGridDocumentos() {

        if(documentosGrid == null) {
            return;
        }

        partidasContainer.removeAllItems();
        documentosContainer.removeAllItems();

        queryString = " SELECT * ";
        queryString += " FROM contabilidad_partida";
        queryString += " WHERE IdEmpresa = " + empresaCbx.getValue();
        queryString += " AND   Extract(YEAR From Fecha) >= 2019";
        if(tipoDocumentoOg.getValue().equals("FACTURAS")) {
            queryString += " AND   Upper(TipoDocumento) IN ('FACTURA','RECIBO','RECIBO CONTABLE','RECIBO CORRIENTE','FORMULARIO IVA',";
            queryString += " 'FORMULARIO ISR', 'FORMULARIO ISR RETENIDO', 'FORMULARIO ISO')";
            queryString += " AND   IdNomenclatura IN (" + ((SopdiUI) mainUI).cuentasContablesDefault.getProveedores() + "," + ((SopdiUI) mainUI).cuentasContablesDefault.getInstituciones() + ")";
        }
        else {
            queryString += " AND   Upper(TipoDocumento) IN ('CHEQUE','TRANSFERENCIA') ";
            queryString += " AND   IdNomenclatura = " + ((SopdiUI) mainUI).cuentasContablesDefault.getAnticiposProveedor();
        }
        queryString += " ORDER by contabilidad_partida.CodigoPartida";

System.out.println("query para mostrar ciuenta corriente : " + queryString);

        try {

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);
            stQuery1 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();

            if (rsRecords.next()) {

                String monedaSimbolo;

                do {

                    queryString = " SELECT ";
                    queryString += " SUM(HABER - DEBE) TOTALSALDO, SUM(HaberQuetzales - DebeQuetzales) TOTALSALDOQ ";
                    queryString += " FROM contabilidad_partida";
                    queryString += " WHERE IdEmpresa = " + empresaCbx.getValue();
                    queryString += " AND CodigoCC = '" + rsRecords.getString("CodigoCC") + "'";
                    queryString += " AND contabilidad_partida.IdNomenclatura = " + rsRecords.getString("IdNomenclatura");

                    rsRecords1 = stQuery1.executeQuery(queryString);

                    if (rsRecords1.next()) {

                        if (rsRecords1.getDouble("TOTALSALDO") > 0.00) {

                            Object itemId = documentosContainer.addItem();

                            documentosContainer.getContainerProperty(itemId, TIPO_DOCUMENTO_PROPERTY).setValue(rsRecords.getString("TipoDocumento"));
                            documentosContainer.getContainerProperty(itemId, ID_PROVEEDOR_PROPERTY).setValue(rsRecords.getString("IdProveedor"));
                            documentosContainer.getContainerProperty(itemId, PROVEEDOR_PROPERTY).setValue(rsRecords.getString("NombreProveedor"));
                            documentosContainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha")));
                            documentosContainer.getContainerProperty(itemId, DOCUMENTO_PROPERTY).setValue(rsRecords.getString("SerieDocumento") + " " + rsRecords.getString("NumeroDocumento"));
                            documentosContainer.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(rsRecords.getString("Descripcion"));
                            documentosContainer.getContainerProperty(itemId, MONEDA_PROPERTY).setValue(rsRecords.getString("MonedaDocumento"));

                            if (rsRecords.getString("MonedaDocumento").equals("QUETZALES")) {
                                monedaSimbolo = "Q.";
                            } else {
                                monedaSimbolo = "$.";
                            }
                            documentosContainer.getContainerProperty(itemId, MONTO_PROPERTY).setValue(monedaSimbolo + numberFormat.format(rsRecords.getDouble("MontoDocumento")));
                            documentosContainer.getContainerProperty(itemId, SALDO_PROPERTY).setValue(monedaSimbolo + numberFormat.format(rsRecords1.getDouble("TOTALSALDO")));
                            documentosContainer.getContainerProperty(itemId, CODIGO_CC_PROPERTY).setValue(rsRecords.getString("CodigoCC"));
                            documentosContainer.getContainerProperty(itemId, CODIGO_PARTIDA_PROPERTY).setValue(rsRecords.getString("CodigoPartida"));
                        } // saldo = 0.00
                    }//no tiene cuenta corriente

                } while (rsRecords.next());
            }// NO HAY DOCUMENTOS

        } catch (Exception ex) {
            System.out.println("Error al listar grid anticipos factura : " + ex);
            ex.printStackTrace();
        }
    }

    public void llenarTablaCC() {

        if(partidasGrid == null) {
            return;
        }

        partidasContainer.removeAllItems();
        partidasContainer.removeAllContainerFilters();

        queryString = " SELECT * ";
        queryString += " FROM contabilidad_partida";
        queryString += " WHERE IdEmpresa = " + empresaCbx.getValue();
        queryString += " AND   CodigoCC = '" + documentosContainer.getContainerProperty(documentosGrid.getSelectedRow(), CODIGO_CC_PROPERTY).getValue() + "'";
        if(tipoDocumentoOg.getValue().equals("FACTURAS")) {
            queryString += " AND   IdNomenclatura IN (" + ((SopdiUI) mainUI).cuentasContablesDefault.getProveedores() + "," + ((SopdiUI) mainUI).cuentasContablesDefault.getInstituciones() + ")";
        }
        else {
            queryString += " AND   IdNomenclatura = " + ((SopdiUI) mainUI).cuentasContablesDefault.getAnticiposProveedor();
        }

System.out.println("query para mostrar ciuenta corriente : " + queryString);

        try {
            stQuery1 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords1 = stQuery1.executeQuery(queryString);

            if (rsRecords1.next()) {

                String monedaSimbolo = "Q.";
                do {

                    Object itemId = partidasContainer.addItem();

                    partidasContainer.getContainerProperty(itemId, TIPO_DOCUMENTO_PROPERTY).setValue(rsRecords1.getString("TipoDocumento"));
                    partidasContainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords1.getDate("Fecha")));
                    partidasContainer.getContainerProperty(itemId, DOCUMENTO_PROPERTY).setValue(rsRecords1.getString("SerieDocumento") + " " + rsRecords1.getString("NumeroDocumento"));
                    partidasContainer.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(rsRecords1.getString("Descripcion"));
                    partidasContainer.getContainerProperty(itemId, MONEDA_PROPERTY).setValue(rsRecords1.getString("MonedaDocumento"));

                    if (rsRecords1.getString("MonedaDocumento").equals("QUETZALES")) {
                        monedaSimbolo = "Q.";
                    } else {
                        monedaSimbolo = "$.";
                    }
                    partidasContainer.getContainerProperty(itemId, DEBE_PROPERTY).setValue(monedaSimbolo + numberFormat.format(rsRecords1.getDouble("Debe")));
                    partidasContainer.getContainerProperty(itemId, HABER_PROPERTY).setValue(monedaSimbolo + numberFormat.format(rsRecords1.getDouble("Haber")));
                    partidasContainer.getContainerProperty(itemId, CODIGO_CC_PROPERTY).setValue(rsRecords1.getString("CodigoCC"));
                    partidasContainer.getContainerProperty(itemId, CODIGO_PARTIDA_PROPERTY).setValue(rsRecords1.getString("CodigoPartida"));

                } while (rsRecords1.next());
            }
        } catch (Exception ex) {
            System.out.println("Error al listar grid cuenta corriente : " + ex);
            ex.printStackTrace();
        }
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - Documentos Por Liquidar");
    }

}
