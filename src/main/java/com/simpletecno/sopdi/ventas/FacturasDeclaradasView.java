package com.simpletecno.sopdi.ventas;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.SelectionEvent;
import com.vaadin.event.SelectionEvent.SelectionListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.HeaderCell;
import com.vaadin.ui.Grid.HeaderRow;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class FacturasDeclaradasView extends VerticalLayout implements View {

    static final String ID_PROPERTY = "Id";
    static final String FECHA_PROPERTY = "Fecha";
    static final String DIAHOY_PROPERTY = "Días";
    static final String TIPO_PROPERTY = "Tipo";
    static final String PROVEEDOR_PROPERTY = "Proveedor/Liquidador";
    static final String CODIGO_PROPERTY = "Código";
    static final String FACTURA_PROPERTY = "Docto./Liqui.";
    static final String MONEDA_PROPERTY = "Moneda";
    static final String VALOR_PROPERTY = "Monto";
    static final String TIPOCAMBIO_PROPERTY = "T.Cambio";
    static final String MONTO_QUETZALES_PROPERTY = "Monto Q.";
    IndexedContainer documentosContainer = new IndexedContainer();
    Grid documentosGrid;

    UI mainUI;
    Statement stQuery, stQuery1;
    ResultSet rsRecords, rsRecords1;
    String queryString;

    Label empresaLbl;
    String empresa;
    Button nextBtn;
    Button prevBtn;
    List<String> empresaLst;
    Grid.FooterRow documentosFooter;

    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");

    public FacturasDeclaradasView() {
        this.mainUI = UI.getCurrent();
        setWidth("100%");
        setSpacing(true);
        setMargin(false);

        empresaLbl = new Label("");
        empresaLbl.setWidth("340px");
        empresaLbl.addStyleName(ValoTheme.LABEL_H2);

        final int EPREV = 0;
        final int ENEXT = 1;

        empresaLst = new ArrayList<String>();

        llenarComboEmpresa();

        ListIterator<String> listIterator = empresaLst.listIterator();

        prevBtn = new Button("Anterior");
        prevBtn.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        prevBtn.addStyleName(ValoTheme.BUTTON_LARGE);
        prevBtn.addStyleName("flechas");
        prevBtn.setIcon(FontAwesome.ARROW_LEFT);
        prevBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (listIterator.hasPrevious()) {
                    prevBtn.setEnabled(true);
                    nextBtn.setEnabled(true);
                    empresaLbl.setValue(listIterator.previous());
                    empresa = empresaLbl.getValue().substring(1, 3);
                    llenarGridDocumentos();

                } else {
                    prevBtn.setEnabled(false);
                }
            }
        });

        nextBtn = new Button("Siguiente");
        nextBtn.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        nextBtn.addStyleName(ValoTheme.BUTTON_LARGE);
        nextBtn.addStyleName("flechas");
        nextBtn.setIcon(FontAwesome.ARROW_RIGHT);
        nextBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (listIterator.hasNext()) {
                    prevBtn.setEnabled(true);
                    nextBtn.setEnabled(true);

                    empresaLbl.setValue(listIterator.next());
                    empresa = empresaLbl.getValue().substring(1, 3);
                    llenarGridDocumentos();

                } else {
                    nextBtn.setEnabled(false);
                }
            }
        });

        Label titleLbl = new Label("AUTORIZAR PAGOS");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h1_custom");

        HorizontalLayout empresaLayout = new HorizontalLayout();
        empresaLayout.setSizeUndefined();
        empresaLayout.setResponsive(true);
        empresaLayout.setSpacing(true);
        empresaLayout.addStyleName("rcorners4");
        empresaLayout.addComponents(empresaLbl, prevBtn, nextBtn);
        empresaLayout.setComponentAlignment(prevBtn, Alignment.MIDDLE_LEFT);
        empresaLayout.setComponentAlignment(nextBtn, Alignment.MIDDLE_LEFT);

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setResponsive(true);
        titleLayout.setWidth("100%");
        titleLayout.setMargin(false);
        titleLayout.addComponents(empresaLayout, titleLbl);
        titleLayout.setComponentAlignment(empresaLayout, Alignment.MIDDLE_LEFT);
        titleLayout.setComponentAlignment(titleLbl, Alignment.TOP_RIGHT);

        addComponent(titleLayout);
        setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        crearGridDocumentos();


        String empresaString = String.valueOf(empresaLst.iterator().next());
        empresaLbl.setValue(empresaString);

        empresa = empresaString.substring(1, 3);

        llenarGridDocumentos();

    }

    public void crearGridDocumentos() {

        HorizontalLayout documentosLayout = new HorizontalLayout();
        documentosLayout.setWidth("100%");
        documentosLayout.addStyleName("rcorners3");
        documentosLayout.setResponsive(true);
        documentosLayout.setMargin(true);
        documentosLayout.setSpacing(true);

        documentosContainer.addContainerProperty(ID_PROPERTY, String.class, null);
        documentosContainer.addContainerProperty(FECHA_PROPERTY, String.class, null);
        documentosContainer.addContainerProperty(DIAHOY_PROPERTY, String.class, null);
        documentosContainer.addContainerProperty(TIPO_PROPERTY, String.class, null);
        documentosContainer.addContainerProperty(PROVEEDOR_PROPERTY, String.class, null);
        documentosContainer.addContainerProperty(CODIGO_PROPERTY, String.class, null);
        documentosContainer.addContainerProperty(FACTURA_PROPERTY, String.class, null);
//        documentosContainer.addContainerProperty(MONEDA_PROPERTY, String.class, null);
        documentosContainer.addContainerProperty(VALOR_PROPERTY, String.class, null);
        documentosContainer.addContainerProperty(TIPOCAMBIO_PROPERTY, String.class, null);
        documentosContainer.addContainerProperty(MONTO_QUETZALES_PROPERTY, String.class, null);

        documentosGrid = new Grid("Listado de documentos pendientes de pagar", documentosContainer);

        documentosGrid.setWidth("100%");
        documentosGrid.setImmediate(true);
        documentosGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        documentosGrid.setDescription("Seleccione un registro.");
        documentosGrid.setHeightMode(HeightMode.ROW);
        documentosGrid.setHeightByRows(10);
        documentosGrid.setResponsive(true);
        documentosGrid.setEditorBuffered(false);

        documentosGrid.setResponsive(true);
        documentosGrid.setEditorBuffered(false);

        documentosGrid.getColumn(ID_PROPERTY).setHidable(true).setHidden(true);
        documentosGrid.getColumn(FECHA_PROPERTY).setHidable(true).setHidden(true);
        documentosGrid.getColumn(DIAHOY_PROPERTY).setHidable(true).setHidden(true);
        documentosGrid.getColumn(TIPOCAMBIO_PROPERTY).setHidable(true).setHidden(true);
        documentosGrid.getColumn(TIPO_PROPERTY).setHidable(true).setHidden(true);
        documentosGrid.getColumn(CODIGO_PROPERTY).setHidable(true).setHidden(true);


        documentosGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {

            if (MONTO_QUETZALES_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (VALOR_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (DIAHOY_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightcenter";
            } else if (TIPOCAMBIO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else {
                return null;
            }

        });

        documentosGrid.getColumn(ID_PROPERTY).setExpandRatio(1);
        documentosGrid.getColumn(FECHA_PROPERTY).setExpandRatio(2).setWidth(111);
        documentosGrid.getColumn(DIAHOY_PROPERTY).setExpandRatio(1).setWidth(60);
        documentosGrid.getColumn(TIPO_PROPERTY).setExpandRatio(2).setWidth(80);
        documentosGrid.getColumn(PROVEEDOR_PROPERTY).setExpandRatio(4).setWidth(185);
        documentosGrid.getColumn(CODIGO_PROPERTY);
        documentosGrid.getColumn(FACTURA_PROPERTY).setExpandRatio(2).setWidth(115);
//        documentosGrid.getColumn(MONEDA_PROPERTY).setExpandRatio(2).setWidth(110);
        documentosGrid.getColumn(VALOR_PROPERTY).setExpandRatio(2).setWidth(118);
        documentosGrid.getColumn(TIPOCAMBIO_PROPERTY).setExpandRatio(1);
        documentosGrid.getColumn(MONTO_QUETZALES_PROPERTY).setExpandRatio(2).setWidth(118);

        documentosGrid.addSelectionListener(new SelectionListener() {
            @Override
            public void select(SelectionEvent event) {
                if (documentosGrid.getSelectedRow() != null) {
                    // mostrarAnticipos();
                }
            }
        });

        HeaderRow filterRow = documentosGrid.appendHeaderRow();

        HeaderCell cell = filterRow.getCell(TIPO_PROPERTY);

        TextField filterField = new TextField();
        filterField.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField.setInputPrompt("Filtrar");
        filterField.setColumns(10);

        filterField.addTextChangeListener(change -> {
            documentosContainer.removeContainerFilters(TIPO_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                documentosContainer.addContainerFilter(
                        new SimpleStringFilter(TIPO_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell.setComponent(filterField);

        HeaderCell cell0 = filterRow.getCell(FACTURA_PROPERTY);

        TextField filterField0 = new TextField();
        filterField0.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField0.setInputPrompt("Filtrar");
        filterField0.setColumns(8);

        filterField0.addTextChangeListener(change -> {
            documentosContainer.removeContainerFilters(FACTURA_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                documentosContainer.addContainerFilter(
                        new SimpleStringFilter(FACTURA_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell0.setComponent(filterField0);
        /**
         * HeaderCell cell1 = filterRow.getCell(MONEDA_PROPERTY);
         *
         * TextField filterField1 = new TextField();
         * filterField1.addStyleName(ValoTheme.TEXTFIELD_TINY);
         * filterField1.setInputPrompt("Filtrar"); filterField1.setColumns(6);
         *
         * filterField1.addTextChangeListener(change -> {
         * documentosContainer.removeContainerFilters(MONEDA_PROPERTY);
         *
         * // (Re)create the filter if necessary if
         * (!change.getText().isEmpty()) {
         * documentosContainer.addContainerFilter( new
         * SimpleStringFilter(MONEDA_PROPERTY, change.getText(), true, false));
         * } }); cell1.setComponent(filterField1);
         *
         */
        HeaderCell cell2 = filterRow.getCell(PROVEEDOR_PROPERTY);

        TextField filterField2 = new TextField();
        filterField2.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField2.setInputPrompt("Filtrar");
        filterField2.setColumns(15);

        filterField2.addTextChangeListener(change -> {
            documentosContainer.removeContainerFilters(PROVEEDOR_PROPERTY);

            // (Re)create the filter if necessary
            if (!change.getText().isEmpty()) {
                documentosContainer.addContainerFilter(
                        new SimpleStringFilter(PROVEEDOR_PROPERTY,
                                change.getText(), true, false));
            }
        });
        cell2.setComponent(filterField2);

        documentosFooter = documentosGrid.appendFooterRow();
        documentosFooter.getCell(FACTURA_PROPERTY).setText("Totales");
        documentosFooter.getCell(VALOR_PROPERTY).setText("0.00");
        documentosFooter.getCell(MONTO_QUETZALES_PROPERTY).setText("0.00");

        documentosFooter.getCell(VALOR_PROPERTY).setStyleName("rightalign");
        documentosFooter.getCell(MONTO_QUETZALES_PROPERTY).setStyleName("rightalign");

        documentosLayout.addComponent(documentosGrid);
        documentosLayout.setComponentAlignment(documentosGrid, Alignment.MIDDLE_CENTER);


        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setMargin(true);
        buttonsLayout.setSpacing(true);


        addComponent(documentosLayout);

        setComponentAlignment(documentosLayout, Alignment.MIDDLE_CENTER);

        addComponent(buttonsLayout);

        setComponentAlignment(buttonsLayout, Alignment.MIDDLE_CENTER);
    }

    public void llenarGridDocumentos() {

        documentosContainer.removeAllItems();
        documentosFooter.getCell(VALOR_PROPERTY).setText("0.00");
        documentosFooter.getCell(MONTO_QUETZALES_PROPERTY).setText("0.00");


        String queryString;
        queryString = " Select contabilidad_partida.IdPartida, contabilidad_partida.Fecha, ";
        queryString += " contabilidad_partida.IdProveedor, contabilidad_partida.NombreProveedor ,";
        queryString += " contabilidad_partida.SerieDocumento, contabilidad_partida.NumeroDocumento, ";
        queryString += " contabilidad_partida.MonedaDocumento, contabilidad_partida.TipoCambio, ";
        queryString += " contabilidad_partida.TipoDocumento,";
        queryString += " contabilidad_partida.MontoAutorizadoPagar, usuario.Nombre as uNombre,";
        queryString += " contabilidad_partida.Archivo, contabilidad_partida.Haber, contabilidad_partida.HaberQuetzales,";
        queryString += " contabilidad_partida.Saldo, contabilidad_partida.MontoAutorizadoPagar, contabilidad_partida.MontoAplicarAnticipo, ";
        queryString += " DATEDIFF(CURDATE(),contabilidad_partida.Fecha) as DiasHoy ";
        queryString += " From contabilidad_partida, usuario ";
        queryString += " Where contabilidad_partida.IdEmpresa = " + empresa;
        queryString += " And UPPER(contabilidad_partida.TipoDocumento) IN ('FACTURA')";
        queryString += " And contabilidad_partida.IdLiquidacion = 0 "; // SOLO FACTURAS PROVEEDORES COMPRA
        queryString += " And contabilidad_partida.SALDO > 0 ";
        queryString += " And usuario.IdUsuario = contabilidad_partida.CreadoUsuario ";

        try {

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { // si hay facturas compra por pagar

                String monedaSimbolo = "Q.";

                do {

                    Object itemId = documentosContainer.addItem();

                    documentosContainer.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords.getString("IdPartida"));
                    documentosContainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha")));
                    documentosContainer.getContainerProperty(itemId, DIAHOY_PROPERTY).setValue(rsRecords.getString("DiasHoy"));
                    documentosContainer.getContainerProperty(itemId, TIPO_PROPERTY).setValue(rsRecords.getString("TipoDocumento"));
                    documentosContainer.getContainerProperty(itemId, PROVEEDOR_PROPERTY).setValue(rsRecords.getString("NombreProveedor"));
                    documentosContainer.getContainerProperty(itemId, CODIGO_PROPERTY).setValue(rsRecords.getString("IDProveedor"));
                    documentosContainer.getContainerProperty(itemId, FACTURA_PROPERTY).setValue(rsRecords.getString("SerieDocumento") + " " + rsRecords.getString("NumeroDocumento"));
//                    documentosContainer.getContainerProperty(itemId, MONEDA_PROPERTY).setValue(rsRecords.getString("MonedaDocumento"));

                    if (rsRecords.getString("MonedaDocumento").equals("QUETZALES")) {
                        monedaSimbolo = "Q.";
                    } else {
                        monedaSimbolo = "$.";
                    }
                    documentosContainer.getContainerProperty(itemId, VALOR_PROPERTY).setValue(monedaSimbolo + numberFormat.format(rsRecords.getDouble("Haber")));
                    documentosContainer.getContainerProperty(itemId, TIPOCAMBIO_PROPERTY).setValue(rsRecords.getString("TipoCambio"));
                    documentosContainer.getContainerProperty(itemId, MONTO_QUETZALES_PROPERTY).setValue("Q." + numberFormat.format(rsRecords.getDouble("HaberQuetzales")));


                } while (rsRecords.next());

                documentosGrid.select(documentosContainer.firstItemId());

                // documentosFooter.getCell(VALOR_PROPERTY).setText(numberFormat.format(totalMonto));
                //  documentosFooter.getCell(MONTO_QUETZALES_PROPERTY).setText(numberFormat.format(totalQueztales));


            }

            queryString = " Select contabilidad_partida.IdLiquidacion, contabilidad_partida.IdLiquidador, contabilidad_partida.CreadoFechaYHora As Fecha, ";
            queryString += " contabilidad_partida.MonedaDocumento, contabilidad_partida.TipoCambio, ";
            queryString += " contabilidad_partida.MontoAutorizadoPagar, contabilidad_partida.MontoAplicarAnticipo,";
            queryString += " SUM(contabilidad_partida.Haber) as Total, sum(contabilidad_partida.HaberQuetzales) TotalQuetzales,";
            queryString += " liquidador_autorizado.Nombre as NLiquidador, contabilidad_partida.Archivo, usuario.Nombre as uNombre ";
            queryString += " From contabilidad_partida, usuario, liquidador_autorizado ";
            queryString += " Where contabilidad_partida.IdEmpresa = " + empresa;
            queryString += " And contabilidad_partida.TipoDocumento = 'FACTURA'";
            queryString += " And contabilidad_partida.IdLiquidacion > 0 "; // PARA QUE MUESTRE SOLAMENTE LAS LIQUIDACIONES
            queryString += " And contabilidad_partida.SALDO > 0 ";
            queryString += " And usuario.IdUsuario = contabilidad_partida.CreadoUsuario ";
            queryString += " And liquidador_autorizado.IdLiquidador = contabilidad_partida.IdLiquidador";
            queryString += " Group by contabilidad_partida.IdLiquidacion, contabilidad_partida.IdLiquidador";

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { // si hay liquidaciones por pagar

                String monedaSimbolo = "Q.";

                do {

                    Object itemId = documentosContainer.addItem();

                    documentosContainer.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords.getString("IdLiquidacion"));
                    documentosContainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha")));
                    documentosContainer.getContainerProperty(itemId, TIPO_PROPERTY).setValue("LIQUIDACION");
                    documentosContainer.getContainerProperty(itemId, PROVEEDOR_PROPERTY).setValue(rsRecords.getString("NLiquidador"));
                    documentosContainer.getContainerProperty(itemId, CODIGO_PROPERTY).setValue(rsRecords.getString("IdLiquidador"));
                    documentosContainer.getContainerProperty(itemId, FACTURA_PROPERTY).setValue(rsRecords.getString("IdLiquidacion"));
//                    documentosContainer.getContainerProperty(itemId, MONEDA_PROPERTY).setValue(rsRecords.getString("MonedaDocumento"));
                    if (rsRecords.getString("MonedaDocumento").equals("QUETZALES")) {
                        monedaSimbolo = "Q.";
                    } else {
                        monedaSimbolo = "$.";
                    }

                    documentosContainer.getContainerProperty(itemId, VALOR_PROPERTY).setValue(monedaSimbolo + numberFormat.format(rsRecords.getDouble("TOTAL")));
                    documentosContainer.getContainerProperty(itemId, TIPOCAMBIO_PROPERTY).setValue(rsRecords.getString("TipoCambio"));
                    documentosContainer.getContainerProperty(itemId, MONTO_QUETZALES_PROPERTY).setValue("Q." + numberFormat.format(rsRecords.getDouble("TOTALQuetzales")));

                } while (rsRecords.next());

                //   documentosFooter.getCell(VALOR_PROPERTY).setText(numberFormat.format(totlMonto));
                //   documentosFooter.getCell(MONTO_QUETZALES_PROPERTY).setText(numberFormat.format(totalQueztales));


            }

        } catch (Exception ex) {
            System.out.println("Error al listar tabla Facturas y Liquidaciones : " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void llenarComboEmpresa() {
        String queryString = " SELECT * from contabilidad_empresa";
        queryString += " Where IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();

        try {
            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords1 = stQuery1.executeQuery(queryString);

            while (rsRecords1.next()) { //  encontrado                
                empresaLst.add("(" + rsRecords1.getString("IdEmpresa") + ") " + rsRecords1.getString("Empresa"));

                if (empresaLst.size() == 1) {
                    empresaLbl.setValue("(" + rsRecords1.getString("IdEmpresa") + ") " + rsRecords1.getString("Empresa"));
                }
            }

        } catch (Exception ex1) {
            System.out.println("Error al listar empresas: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
