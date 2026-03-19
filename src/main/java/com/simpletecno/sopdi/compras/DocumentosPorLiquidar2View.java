package com.simpletecno.sopdi.compras;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
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
public class DocumentosPorLiquidar2View extends VerticalLayout implements View {

    static final String TIPO_DOCUMENTO_PROPERTY = "TIPO";
    static final String FECHA_PROPERTY = "Fecha";
    static final String ID_PROVEEDOR_PROPERTY = "Id Proveeodr";
    static final String PROVEEDOR_PROPERTY = "Proveedor/Cliente";
    static final String DOCUMENTO_PROPERTY = "Documento";
    static final String CODIGO_CC_PROPERTY = "CodigoCC";
    static final String IDNOMENCLATURA_PROPERTY = "IDNomenclatura";
    static final String DESCRIPCION_PROPERTY = "Descripción";
    static final String MONEDA_PROPERTY = "Moneda";
    static final String DEBE_PROPERTY = "DEBE";
    static final String HABER_PROPERTY = "HABER";
    static final String DEBEQ_PROPERTY = "DEBE Q";
    static final String HABERQ_PROPERTY = "HABER Q";
    static final String SALDO_PROPERTY = "Saldo";
    static final String SALDOQ_PROPERTY = "Saldo Q";

    static final String CODIGO_PARTIDA_PROPERTY = "Partida";

    ComboBox tipoConsultaCbx;

    IndexedContainer documentosContainer = new IndexedContainer();
    Grid documentosGrid;
    Grid.HeaderRow documentosGridFilterRow;

    IndexedContainer partidasContainer = new IndexedContainer();
    Grid partidasGrid;

    UI mainUI;
    Statement stQuery, stQuery1;
    ResultSet rsRecords, rsRecords1;
    String queryString;

    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public DocumentosPorLiquidar2View() {
        this.mainUI = UI.getCurrent();
        setResponsive(true);
        setSpacing(true);

        HorizontalLayout layoutTitle = new HorizontalLayout();
        layoutTitle.setSpacing(true);
        layoutTitle.setMargin(new MarginInfo(false, true, false, true));
        layoutTitle.setWidth("100%");

        tipoConsultaCbx = new ComboBox("TIPO DE CUENTA : ");
        tipoConsultaCbx.setNewItemsAllowed(false);
        tipoConsultaCbx.setNullSelectionAllowed(false);
        tipoConsultaCbx.setInvalidAllowed(false);
        tipoConsultaCbx.setTextInputAllowed(false);
        tipoConsultaCbx.select("");
        tipoConsultaCbx.setWidth("15em");
        tipoConsultaCbx.addValueChangeListener(event -> {
            if (event.getProperty() == null) {
                return;
            }
            llenarGridDocumentos();
        });

        llenarComboTiposConsulta();

        Label titleLbl = new Label(empresaId + " " + empresaNombre + " DOCUMENTOS POR LIQUIDAR");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h2_custom");

        layoutTitle.addComponent(tipoConsultaCbx);
        layoutTitle.setComponentAlignment(tipoConsultaCbx, Alignment.BOTTOM_RIGHT);

        layoutTitle.addComponent(titleLbl);
        layoutTitle.setComponentAlignment(titleLbl, Alignment.BOTTOM_RIGHT);

        addComponent(layoutTitle);

        crearGridDocumentos();
        crearGridPartidas();
        llenarGridDocumentos();

    }

    public void crearGridDocumentos() {

        HorizontalLayout reportLayout = new HorizontalLayout();
        reportLayout.setWidth("100%");
        reportLayout.addStyleName("rcorners3");
        reportLayout.setResponsive(true);
        reportLayout.setMargin(new MarginInfo(false, true, false, true));

        documentosContainer.addContainerProperty(ID_PROVEEDOR_PROPERTY, String.class, null);
        documentosContainer.addContainerProperty(PROVEEDOR_PROPERTY, String.class, null);
        documentosContainer.addContainerProperty(CODIGO_CC_PROPERTY, String.class, null);
        documentosContainer.addContainerProperty(IDNOMENCLATURA_PROPERTY, String.class, null);
        documentosContainer.addContainerProperty(DESCRIPCION_PROPERTY, String.class, null);
        documentosContainer.addContainerProperty(MONEDA_PROPERTY, String.class, null);
        documentosContainer.addContainerProperty(DEBE_PROPERTY, String.class, null);
        documentosContainer.addContainerProperty(HABER_PROPERTY, String.class, null);
        documentosContainer.addContainerProperty(DEBEQ_PROPERTY, String.class, null);
        documentosContainer.addContainerProperty(HABERQ_PROPERTY, String.class, null);
        documentosContainer.addContainerProperty(SALDO_PROPERTY, String.class, null);
        documentosContainer.addContainerProperty(SALDOQ_PROPERTY, String.class, null);

        documentosGrid = new Grid("Documentos pendientes de liquidar.", documentosContainer);

        documentosGrid.setImmediate(true);
        documentosGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
//        documentosGrid.setDescription("Seleccione un registro.");
        documentosGrid.setHeightMode(HeightMode.ROW);
        documentosGrid.setHeightByRows(10);
        documentosGrid.setWidth("100%");
        documentosGrid.setResponsive(true);
        documentosGrid.setSizeFull();
        documentosGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if (DEBE_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (HABER_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (DEBEQ_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (HABERQ_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (SALDO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (SALDOQ_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }
        });
        documentosGrid.getColumn(CODIGO_CC_PROPERTY).setHidable(true);
        documentosGrid.getColumn(DESCRIPCION_PROPERTY).setHidable(true);

        documentosGrid.addSelectionListener(new SelectionEvent.SelectionListener() {
            @Override
            public void select(SelectionEvent event) {

                if (documentosGrid.getSelectedRow() != null) {
                    llenarGridPartidas();
                }
            }
        });

        documentosGridFilterRow = documentosGrid.appendHeaderRow();

        Grid.HeaderCell cell1 = documentosGridFilterRow.getCell(PROVEEDOR_PROPERTY);

        TextField filterField1 = new TextField();
        filterField1.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField1.setInputPrompt("Filtrar");
        filterField1.setColumns(15);

        filterField1.addTextChangeListener(change -> {
            documentosContainer.removeContainerFilters(PROVEEDOR_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                documentosContainer.addContainerFilter(
                        new SimpleStringFilter(PROVEEDOR_PROPERTY,
                                change.getText(), true, false));
            }
//            setTotal();
        });
        cell1.setComponent(filterField1);

        Grid.HeaderCell cell2 = documentosGridFilterRow.getCell(MONEDA_PROPERTY);

        TextField filterField2 = new TextField();
        filterField2.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField2.setInputPrompt("Filtrar");
        filterField2.setColumns(15);

        filterField2.addTextChangeListener(change -> {
            documentosContainer.removeContainerFilters(MONEDA_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                documentosContainer.addContainerFilter(
                        new SimpleStringFilter(MONEDA_PROPERTY,
                                change.getText(), true, false));
            }
//            setTotal();
        });
        cell2.setComponent(filterField2);

        Grid.HeaderCell cell4 = documentosGridFilterRow.getCell(DESCRIPCION_PROPERTY);

        TextField filterField4 = new TextField();
        filterField4.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField4.setInputPrompt("Filtrar");
        filterField4.setColumns(15);

        filterField4.addTextChangeListener(change -> {
            documentosContainer.removeContainerFilters(DESCRIPCION_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                documentosContainer.addContainerFilter(
                        new SimpleStringFilter(DESCRIPCION_PROPERTY,
                                change.getText(), true, false));
            }
//            setTotal();
        });
        cell4.setComponent(filterField4);

        Grid.HeaderCell cell5 = documentosGridFilterRow.getCell(IDNOMENCLATURA_PROPERTY);

        TextField filterField5 = new TextField();
        filterField5.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField5.setInputPrompt("Filtrar");
        filterField5.setColumns(10);

        filterField5.addTextChangeListener(change -> {
            documentosContainer.removeContainerFilters(IDNOMENCLATURA_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                documentosContainer.addContainerFilter(
                        new SimpleStringFilter(IDNOMENCLATURA_PROPERTY,
                                change.getText(), true, false));
            }
//            setTotal();
        });
        cell5.setComponent(filterField5);

        reportLayout.addComponent(documentosGrid);
        reportLayout.setComponentAlignment(documentosGrid, Alignment.TOP_CENTER);

        addComponent(reportLayout);
        setComponentAlignment(reportLayout, Alignment.TOP_CENTER);

    }

    private void setFooterTotal(String propertyId) {

        double total = 0.00;

        for (Object itemId : documentosContainer.getItemIds()) {
            Item item = documentosContainer.getItem(itemId);
            Object propertyValue = item.getItemProperty(propertyId).getValue();

            total += Double.valueOf(String.valueOf(propertyValue));
        }
    }

    private void llenarComboTiposConsulta() {
        if(tipoConsultaCbx == null) {
            return;
        }

        tipoConsultaCbx.clear();

        String queryString = " SELECT N3 ";
        queryString += " FROM contabilidad_nomenclatura_empresa ";
        queryString += " WHERE Estatus='HABILITADA'";
        queryString += " AND PorLiquidar = 1";
        queryString += " AND IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
        queryString += " GROUP BY N3";
        queryString += " ORDER BY N3";

        tipoConsultaCbx.addItem("<<ELIJA>>");
        tipoConsultaCbx.select("<<ELIJA>>");

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) { //  encontrado
                tipoConsultaCbx.addItem(rsRecords.getString("N3"));
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar cuentas contables: " + ex1.getMessage());
            ex1.printStackTrace();
        }

    }
    public void llenarGridDocumentos() {

        if(documentosGrid == null) {
            return;
        }

        partidasContainer.removeAllItems();

        documentosContainer.removeAllContainerFilters();
        documentosContainer.removeAllItems();

        queryString = " SELECT * ";
        queryString += " FROM contabilidad_cuentas_por_liquidar";
        queryString += " WHERE IdEmpresa = " + empresaId;
        queryString += " AND IdNomenclatura IN (SELECT IdNomenclatura FROM contabilidad_nomenclatura_empresa WHERE N3 = '" + tipoConsultaCbx.getValue() + "' AND IDEMPRESA = " + empresaId + ")";
        queryString += " ORDER BY IdNomenclatura, IdProveedor ";

System.out.println("--> DOCUMENTOS POR LIQUIDAR = " + queryString);

        try {

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) {

                String monedaSimbolo;

                do {
                    Object itemId = documentosContainer.addItem();

                    documentosContainer.getContainerProperty(itemId, ID_PROVEEDOR_PROPERTY).setValue(rsRecords.getString("IdProveedor"));
                    documentosContainer.getContainerProperty(itemId, PROVEEDOR_PROPERTY).setValue(rsRecords.getString("NombreProveedor"));
                    documentosContainer.getContainerProperty(itemId, CODIGO_CC_PROPERTY).setValue(rsRecords.getString("CodigoCC"));
                    documentosContainer.getContainerProperty(itemId, IDNOMENCLATURA_PROPERTY).setValue(rsRecords.getString("IdNomenclatura"));
                    documentosContainer.getContainerProperty(itemId, DESCRIPCION_PROPERTY).setValue(rsRecords.getString("N5"));
                    documentosContainer.getContainerProperty(itemId, MONEDA_PROPERTY).setValue(rsRecords.getString("MonedaDocumento"));

                    if (rsRecords.getString("MonedaDocumento").toUpperCase().equals("QUETZALES")) {
                        monedaSimbolo = "Q.";
                    } else {
                        monedaSimbolo = "$.";
                    }
                    documentosContainer.getContainerProperty(itemId, DEBE_PROPERTY).setValue(monedaSimbolo + numberFormat.format(rsRecords.getDouble("TotalDebe")));
                    documentosContainer.getContainerProperty(itemId, HABER_PROPERTY).setValue(monedaSimbolo + numberFormat.format(rsRecords.getDouble("TotalHaber")));
                    documentosContainer.getContainerProperty(itemId, DEBEQ_PROPERTY).setValue("Q." + numberFormat.format(rsRecords.getDouble("TotalDebeQuetzales")));
                    documentosContainer.getContainerProperty(itemId, HABERQ_PROPERTY).setValue("Q." + numberFormat.format(rsRecords.getDouble("TotalHaberQuetzales")));
                    documentosContainer.getContainerProperty(itemId, SALDO_PROPERTY).setValue(monedaSimbolo + numberFormat.format(rsRecords.getDouble("SALDO")));
                    documentosContainer.getContainerProperty(itemId, SALDOQ_PROPERTY).setValue("Q." + numberFormat.format(rsRecords.getDouble("SALDOQuetzales")));

                } while (rsRecords.next());
            }// NO HAY DOCUMENTOS

        } catch (Exception ex) {
            System.out.println("Error al listar grid documentos pendientes de liquidar : " + ex);
            ex.printStackTrace();
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
        partidasContainer.addContainerProperty(DEBEQ_PROPERTY, String.class, null);
        partidasContainer.addContainerProperty(HABERQ_PROPERTY, String.class, null);
        partidasContainer.addContainerProperty(CODIGO_PARTIDA_PROPERTY, String.class, null);
        partidasContainer.addContainerProperty(CODIGO_CC_PROPERTY, String.class, null);

        partidasGrid = new Grid("Cuenta Corriente.", partidasContainer);

        partidasGrid.setImmediate(true);
        partidasGrid.setSelectionMode(Grid.SelectionMode.NONE);
        partidasGrid.setHeightMode(HeightMode.ROW);
        partidasGrid.setHeightByRows(10);
        partidasGrid.setWidth("100%");
        partidasGrid.setResponsive(true);
        partidasGrid.setSizeFull();
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
        partidasGrid.getColumn(CODIGO_CC_PROPERTY).setHidable(true);

        reportLayout.addComponent(partidasGrid);
        reportLayout.setComponentAlignment(partidasGrid, Alignment.TOP_CENTER);

        addComponent(reportLayout);
        setComponentAlignment(reportLayout, Alignment.TOP_CENTER);

    }

    public void llenarGridPartidas() {

        if(partidasGrid == null) {
            return;
        }

        partidasContainer.removeAllItems();
        partidasContainer.removeAllContainerFilters();

        queryString = " SELECT * ";
        queryString += " FROM contabilidad_partida";
        queryString += " WHERE IdEmpresa = " + empresaId;
        queryString += " AND   CodigoCC = '" + documentosContainer.getContainerProperty(documentosGrid.getSelectedRow(), CODIGO_CC_PROPERTY).getValue() + "'";
        queryString += " AND   IdNomenclatura = " + documentosContainer.getContainerProperty(documentosGrid.getSelectedRow(), IDNOMENCLATURA_PROPERTY).getValue();

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

                    if (rsRecords1.getString("MonedaDocumento").toUpperCase().equals("QUETZALES")) {
                        monedaSimbolo = "Q.";
                    } else {
                        monedaSimbolo = "$.";
                    }

                    partidasContainer.getContainerProperty(itemId, DEBE_PROPERTY).setValue(monedaSimbolo + numberFormat.format(rsRecords1.getDouble("Debe")));
                    partidasContainer.getContainerProperty(itemId, HABER_PROPERTY).setValue(monedaSimbolo + numberFormat.format(rsRecords1.getDouble("Haber")));
                    partidasContainer.getContainerProperty(itemId, DEBEQ_PROPERTY).setValue("Q." + numberFormat.format(rsRecords1.getDouble("DebeQuetzales")));
                    partidasContainer.getContainerProperty(itemId, HABERQ_PROPERTY).setValue("Q." + numberFormat.format(rsRecords1.getDouble("HaberQuetzales")));
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
