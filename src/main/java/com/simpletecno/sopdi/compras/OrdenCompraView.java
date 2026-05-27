package com.simpletecno.sopdi.compras;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.simpletecno.sopdi.contabilidad.MostrarPartidaContable;
import com.vaadin.addon.tableexport.DefaultTableHolder;
import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.addon.tableexport.TableHolder;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.dialogs.ConfirmDialog;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.logging.Logger;

/**
 * @author user
 */
public class OrdenCompraView extends VerticalLayout implements View {

    Grid ordenCompraGrid;
    public IndexedContainer ordenCompraContainer = new IndexedContainer();
    static final String ID_PROPERTY = "Id";
    static final String NOC_PROPERTY = "NOC";
//    static final String TIPO_PROPERTY = "Tipo";
    static final String EMPRESA_PROPERTY = "Empresa";
    static final String PROYECTO_PROPERTY = "Proyecto";
    static final String PROVEEDOR_PROPERTY = "Proveedor";
    static final String FECHA_PROPERTY = "Fecha";
    static final String MONEDA_PROPERTY = "Moneda";
    static final String TOTAL_PROPERTY = "Total";
    static final String RESPONSABLE_PROPERTY = "Responsable";
//    static final String DIRECCIONENTREGA_PROPERTY = "Direccion";
    static final String ANTICIPO_PROPERTY = "Anticipo";
    static final String RAZON_PROPERTY = "Razon";
//    static final String ESTADO_PROPERTY = "Estado";
    static final String CODIGOCC_ANTICIPO_PROPERTY = "CodigoCCAnticipo";
    static final String CODIGOCC_DOCUMENTO_PROPERTY = "CodigoCCDocumento";
    static final String CREADOFECHAHORA_PROPERTY = "Creada Fecha";
    static final String CREADOUSUARIO_PROPERTY = "Usuario";

    UI mainUI;
    Statement stQuery, stQuery2;
    ResultSet rsRecords, rsRecords2;
    String queryString;

    DateField inicioDt;
    DateField finDt;
    Button consultarBtn;

    static DecimalFormat numberFormat = new DecimalFormat("#,###,##0.00");

    Button newBtn;
    Button editBtn;
    Button delBtn;
    Button printBtn;
    Button partidaContableBtn;
    Button exportExcelBtn;
    Button refrescarBtn;

    Grid.FooterRow footerRow;

    OptionGroup ordenCompraOg = new OptionGroup();
//    CheckBox abiertasChbx = new CheckBox("Abiertas");
    CheckBox cerradasChbx = new CheckBox("Cerradas");

    String empresaId = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
    String empresaNombre = ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyName();

    public OrdenCompraView() {
        this.mainUI = UI.getCurrent();
        setSizeFull();
        setMargin(false);
        setSpacing(true);

        Label titleLbl = new Label(empresaId + " " + empresaNombre + " ORDENES DE COMPRA");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h2_custom");

        ordenCompraOg.setStyleName("horizontal");
        if(((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId().equals("10") || ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId().equals("11")) {
            ordenCompraOg.addItems("EVENTUAL", "RECURRENTE");
        } else {
            ordenCompraOg.addItems( "CONSTRUCCION", "EVENTUAL", "RECURRENTE");
        }

        ordenCompraOg.select("EVENTUAL");
        ordenCompraOg.addValueChangeListener( e -> {
            llenarTablaOrdenCompra();
        });

//        abiertasChbx.setValue(true);
//        abiertasChbx.addValueChangeListener(e -> {
//            llenarTablaOrdenCompra();
//        });

        cerradasChbx.addValueChangeListener(e -> {
            llenarTablaOrdenCompra();
        });
        cerradasChbx.addStyleName(ValoTheme.CHECKBOX_LARGE);
        cerradasChbx.setDescription("Por defecto solo se muestran las ABIERTAS, click aqui para mostrar solo las CERRADAS.");

        HorizontalLayout filterLayout = new HorizontalLayout();
        filterLayout.setSpacing(true);
        filterLayout.setMargin(new MarginInfo(false, true, false, false));
        filterLayout.setWidth("100%");
        filterLayout.addComponents(ordenCompraOg,cerradasChbx);
        filterLayout.setComponentAlignment(ordenCompraOg, Alignment.MIDDLE_CENTER);
//        filterLayout.setComponentAlignment(abiertasChbx, Alignment.MIDDLE_RIGHT);
        filterLayout.setComponentAlignment(cerradasChbx, Alignment.MIDDLE_CENTER);
        filterLayout.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setResponsive(true);
        titleLayout.setSpacing(true);
        titleLayout.setMargin(new MarginInfo(false, true, false, false));
        titleLayout.setWidth("100%");

        titleLayout.addComponents(titleLbl);
        titleLayout.setComponentAlignment(titleLbl, Alignment.BOTTOM_RIGHT);
        titleLayout.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

        addComponent(titleLayout);
        setComponentAlignment(titleLayout, Alignment.TOP_CENTER);
        setExpandRatio(titleLayout, 0);

        addComponent(filterLayout);
        setComponentAlignment(filterLayout, Alignment.TOP_CENTER);
        setExpandRatio(filterLayout, 0);

        crearTablaOrdenCompra();

        llenarTablaOrdenCompra();

    }

    public void crearTablaOrdenCompra() {

        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.addStyleName("rcorners3");
        contentLayout.setSizeFull();
        contentLayout.setMargin(new MarginInfo(false, true, false, true));
        contentLayout.setSpacing(true);

        HorizontalLayout filtrosLayout = new HorizontalLayout();
        filtrosLayout.setSpacing(true);
        filtrosLayout.setDefaultComponentAlignment(Alignment.BOTTOM_LEFT);

        inicioDt = new DateField("Desde:");
        inicioDt.setDateFormat("dd/MM/yyyy");
        Date primerDia = Utileria.getPrimerDiaDelMes();
        inicioDt.setValue(primerDia);
        inicioDt.setWidth("10em");

        finDt = new DateField("Hasta:");
        finDt.setDateFormat("dd/MM/yyyy");
        Date ultimoDia = Utileria.getUltimoDiaDelMes();
        finDt.setValue(ultimoDia);
        finDt.setWidth("10em");

        consultarBtn = new Button("Consultar");
        consultarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        consultarBtn.setIcon(FontAwesome.SEARCH);
        consultarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                llenarTablaOrdenCompra();
            }
        });

        refrescarBtn = new Button("Refrescar");
        refrescarBtn.setIcon(FontAwesome.REFRESH);
        refrescarBtn.setDescription("Recargar listado");
        refrescarBtn.addClickListener(e -> llenarTablaOrdenCompra());

        ordenCompraContainer.addContainerProperty(ID_PROPERTY, String.class, null);
        ordenCompraContainer.addContainerProperty(NOC_PROPERTY, String.class, null);
//        ordenCompraContainer.addContainerProperty(TIPO_PROPERTY, String.class, null);
        ordenCompraContainer.addContainerProperty(EMPRESA_PROPERTY, String.class, null);
        ordenCompraContainer.addContainerProperty(PROYECTO_PROPERTY, String.class, null);
        ordenCompraContainer.addContainerProperty(PROVEEDOR_PROPERTY, String.class, null);
        ordenCompraContainer.addContainerProperty(FECHA_PROPERTY, String.class, null);
        ordenCompraContainer.addContainerProperty(MONEDA_PROPERTY, String.class, null);
        ordenCompraContainer.addContainerProperty(TOTAL_PROPERTY, String.class, null);
//        ordenCompraContainer.addContainerProperty(DIRECCIONENTREGA_PROPERTY, String.class, null);
        ordenCompraContainer.addContainerProperty(ANTICIPO_PROPERTY, String.class, null);
        ordenCompraContainer.addContainerProperty(RAZON_PROPERTY, String.class, null);
//        ordenCompraContainer.addContainerProperty(ESTADO_PROPERTY, String.class, null);
        ordenCompraContainer.addContainerProperty(RESPONSABLE_PROPERTY, String.class, null);
        ordenCompraContainer.addContainerProperty(CODIGOCC_ANTICIPO_PROPERTY, String.class, null);
        ordenCompraContainer.addContainerProperty(CODIGOCC_DOCUMENTO_PROPERTY, String.class, null);
        ordenCompraContainer.addContainerProperty(CREADOFECHAHORA_PROPERTY, String.class, null);
        ordenCompraContainer.addContainerProperty(CREADOUSUARIO_PROPERTY, String.class, null);

        ordenCompraGrid = new Grid("Listado de ordenes de compra", ordenCompraContainer);
        ordenCompraGrid.setSizeFull();
        ordenCompraGrid.setImmediate(true);
        ordenCompraGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        ordenCompraGrid.setDescription("Seleccione un registro o doble click para editar.");
        ordenCompraGrid.setEditorBuffered(false);
        ordenCompraGrid.getColumn(ID_PROPERTY).setHidable(true).setHidden(true);
        ordenCompraGrid.getColumn(EMPRESA_PROPERTY).setHidable(true).setHidden(true);
        ordenCompraGrid.getColumn(PROYECTO_PROPERTY).setHidable(true).setHidden(true);
        ordenCompraGrid.getColumn(CREADOFECHAHORA_PROPERTY).setHidable(true).setHidden(true);
        ordenCompraGrid.getColumn(CREADOUSUARIO_PROPERTY).setHidable(true).setHidden(true);
        ordenCompraGrid.getColumn(CODIGOCC_ANTICIPO_PROPERTY).setHidable(true).setHidden(true);
        ordenCompraGrid.getColumn(CODIGOCC_DOCUMENTO_PROPERTY).setHidable(true).setHidden(true);

        ordenCompraGrid.getColumn(NOC_PROPERTY).setWidth(140);
        ordenCompraGrid.getColumn(FECHA_PROPERTY).setWidth(110);
        ordenCompraGrid.getColumn(MONEDA_PROPERTY).setWidth(100);
        ordenCompraGrid.getColumn(TOTAL_PROPERTY).setWidth(120);
        ordenCompraGrid.getColumn(ANTICIPO_PROPERTY).setWidth(110);
        ordenCompraGrid.getColumn(PROVEEDOR_PROPERTY).setExpandRatio(2);
        ordenCompraGrid.getColumn(RAZON_PROPERTY).setExpandRatio(2);
        ordenCompraGrid.getColumn(RESPONSABLE_PROPERTY).setExpandRatio(1);

        ordenCompraGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {

            if (TOTAL_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (ANTICIPO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (NOC_PROPERTY.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else if (FECHA_PROPERTY.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else if (MONEDA_PROPERTY.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else {
                return null;
            }
        });

        ordenCompraGrid.addItemClickListener(event -> {
            if (event.isDoubleClick()) {
                ordenCompraGrid.select(event.getItemId());
                editBtn.click();
            }
        });

        Grid.HeaderRow filterRow = ordenCompraGrid.appendHeaderRow();
        agregarFiltroColumna(filterRow, NOC_PROPERTY, 8);
        agregarFiltroColumna(filterRow, PROVEEDOR_PROPERTY, 15);
        agregarFiltroColumna(filterRow, RAZON_PROPERTY, 15);
        agregarFiltroColumna(filterRow, FECHA_PROPERTY, 8);
        agregarFiltroColumna(filterRow, MONEDA_PROPERTY, 6);
        agregarFiltroColumna(filterRow, TOTAL_PROPERTY, 8);
        agregarFiltroColumna(filterRow, RESPONSABLE_PROPERTY, 10);

        footerRow = ordenCompraGrid.appendFooterRow();
        footerRow.getCell(PROVEEDOR_PROPERTY).setText("0 órdenes");
        footerRow.getCell(PROVEEDOR_PROPERTY).setStyleName("rightalign");
        footerRow.getCell(TOTAL_PROPERTY).setText("0.00");
        footerRow.getCell(TOTAL_PROPERTY).setStyleName("rightalign");
        footerRow.getCell(ANTICIPO_PROPERTY).setText("0.00");
        footerRow.getCell(ANTICIPO_PROPERTY).setStyleName("rightalign");

        filtrosLayout.addComponent(inicioDt);
        filtrosLayout.addComponent(finDt);
        filtrosLayout.addComponent(consultarBtn);
        filtrosLayout.addComponent(refrescarBtn);

        contentLayout.addComponent(filtrosLayout);
        contentLayout.setComponentAlignment(filtrosLayout, Alignment.TOP_LEFT);
        contentLayout.setExpandRatio(filtrosLayout, 0);

        contentLayout.addComponent(ordenCompraGrid);
        contentLayout.setExpandRatio(ordenCompraGrid, 1);

        addComponent(contentLayout);
        setComponentAlignment(contentLayout, Alignment.MIDDLE_CENTER);
        setExpandRatio(contentLayout, 1);

        editBtn = new Button("Editar orden");
        editBtn.setIcon(FontAwesome.EDIT);
        editBtn.setDescription("Editar orden de compra.");
        editBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (ordenCompraGrid.getSelectedRow() != null) {
//                    if (String.valueOf(ordenCompraContainer.getContainerProperty(ordenCompraGrid.getSelectedRow(), ESTADO_PROPERTY).getValue()).equals("CERRADA")) {
                    if (cerradasChbx.getValue()) {
                        Notification notif = new Notification("Acción no permitida, Esta orden ya fue cerrada.", Notification.Type.WARNING_MESSAGE);
                        notif.setDelayMsec(1500);
                        notif.setPosition(Position.MIDDLE_CENTER);
                        notif.setIcon(FontAwesome.WARNING);
                        notif.show(Page.getCurrent());
                        return;
                    }
                    OrdenCompraForm ordenForm =
                            new OrdenCompraForm(
                                    String.valueOf(ordenCompraContainer.getContainerProperty(ordenCompraGrid.getSelectedRow(), ID_PROPERTY).getValue())
                            );
                    UI.getCurrent().addWindow(ordenForm);
                    ordenForm.center();
                } else {
                    Notification notif = new Notification("Por favor seleccione una orden de compra para poder editarla.", Notification.Type.WARNING_MESSAGE);
                    notif.setDelayMsec(1500);
                    notif.setPosition(Position.MIDDLE_CENTER);
                    notif.setIcon(FontAwesome.WARNING);
                    notif.show(Page.getCurrent());
                }
            }
        });

        newBtn = new Button("Nueva Orden");
        newBtn.setIcon(FontAwesome.PLUS);
        //newBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        newBtn.setDescription("Nueva Orden.");
        newBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                OrdenCompraForm ordenForm = new OrdenCompraForm("");
                UI.getCurrent().addWindow(ordenForm);
                ordenForm.center();
            }
        });

        delBtn = new Button("Eliminar orden");
        delBtn.setIcon(FontAwesome.TRASH);
        delBtn.addStyleName(ValoTheme.BUTTON_DANGER);
        delBtn.setDescription("Eliminar Orden.");
        delBtn.addClickListener((Button.ClickListener) event -> ConfirmDialog.show(UI.getCurrent(), "Confirme:", "Está seguro de Eliminar esta orden de compra?",
                "SI", "NO", new ConfirmDialog.Listener() {

                    public void onClose(ConfirmDialog dialog) {
                        if (dialog.isConfirmed()) {

                            if(!String.valueOf(ordenCompraContainer.getContainerProperty(ordenCompraGrid.getSelectedRow(), CODIGOCC_ANTICIPO_PROPERTY).getValue() ).trim().isEmpty()){
                                Notification.show("No se puede eliminar esta orden de compra, ya que tiene un anticipo asociado.", Notification.Type.WARNING_MESSAGE);
                                return;
                            }
                            if(!String.valueOf(ordenCompraContainer.getContainerProperty(ordenCompraGrid.getSelectedRow(), CODIGOCC_DOCUMENTO_PROPERTY).getValue() ).trim().isEmpty()){
                                Notification.show("No se puede eliminar esta orden de compra, ya que tiene un DOCUMENTO asociado.", Notification.Type.WARNING_MESSAGE);
                                return;
                            }

                            try {
                                queryString = "DELETE FROM orden_compra_detalle";
                                queryString += " WHERE IdOrdenCompra = " + ordenCompraContainer.getContainerProperty(ordenCompraGrid.getSelectedRow(), ID_PROPERTY).getValue();

                                stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                                stQuery.execute(queryString);

                                queryString = "DELETE FROM orden_compra";
                                queryString += " WHERE Id = " + ordenCompraContainer.getContainerProperty(ordenCompraGrid.getSelectedRow(), ID_PROPERTY).getValue();

                                stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                                stQuery.execute(queryString);

                                ordenCompraContainer.removeItem(ordenCompraGrid.getSelectedRow());

                                Notification notif = new Notification("Registro eliminado exitosamente!.", Notification.Type.TRAY_NOTIFICATION);
                                notif.setDelayMsec(1500);
                                notif.setPosition(Position.MIDDLE_CENTER);
                                notif.setIcon(FontAwesome.CHECK);
                                notif.show(Page.getCurrent());

                            } catch (Exception e) {
                                System.out.println("Error el intentar eliminar registro");
                                e.printStackTrace();
                            }
                        } else {
                            Notification.show("Operación cacelada!", Notification.Type.WARNING_MESSAGE);
                        }
                    }
                }));

        printBtn = new Button("Imprimir OC");
        printBtn.setIcon(FontAwesome.PRINT);
        printBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        printBtn.setDescription("Imprimir Orden de Compra.");
        printBtn.addClickListener((Button.ClickListener) event -> {
                queryString = " SELECT * ";
                queryString += " FROM orden_compra";
                queryString += " INNER JOIN tipo_orden_compra ON orden_compra.IdTipoOrdenCompra = tipo_orden_compra.Id";
                queryString += " INNER JOIN proveedor_empresa ON orden_compra.IdProveedor = proveedor_empresa.IdProveedor";
                queryString += " WHERE orden_compra.Id = " + ordenCompraContainer.getContainerProperty(ordenCompraGrid.getSelectedRow(), ID_PROPERTY).getValue();
                queryString += " AND proveedor_empresa.IdEmpresa = " + empresaId;

                try {
                    stQuery2 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                    rsRecords2 = stQuery2.executeQuery(queryString);

                    if (rsRecords2.next()) { //  encontrado
                        if(rsRecords2.getString("orden_compra.IdTipoOrdenCompra").equals("1") || rsRecords2.getString("orden_compra.IdTipoOrdenCompra").equals("2")) {
                            OrdenCompraEstimacionPDF pdfOrdenCompra =
                                    new OrdenCompraEstimacionPDF(
                                            rsRecords2.getString("orden_compra.Id"),
                                            rsRecords2.getString("orden_compra.IdTipoOrdenCompra"),
                                            rsRecords2.getString("tipo_orden_compra.Descripcion"),
                                            rsRecords2.getString("proveedor_empresa.IdProveedor") + " " + rsRecords2.getString("proveedor_empresa.Nombre"),
                                            rsRecords2.getDouble("orden_compra.Anticipo")
                                    );
                            UI.getCurrent().addWindow(pdfOrdenCompra);
                            pdfOrdenCompra.center();
                        } else { //eventual, recurrente
                            OrdenCompraPDF pdfOrdenCompra =
                                    new OrdenCompraPDF(
                                            rsRecords2.getString("Id"),
                                            rsRecords2.getString("IdTipoOrdenCompra"),
                                            rsRecords2.getString("tipo_orden_compra.Descripcion"),
                                            rsRecords2.getString("proveedor_empresa.IdProveedor") + " " + rsRecords2.getString("proveedor_empresa.Nombre"),
                                            rsRecords2.getDouble("orden_compra.Anticipo")
                                    );
                            UI.getCurrent().addWindow(pdfOrdenCompra);
                            pdfOrdenCompra.center();
                        }

                    }
                } catch (Exception ex) {
                    System.out.println("Error al listar tablea orden compra detalle:" + ex);
                    ex.printStackTrace();
                }
        });

        partidaContableBtn = new Button("Ver partida contable");
        partidaContableBtn.setIcon(FontAwesome.BOOK);
        partidaContableBtn.setDescription("Ver partida contable de Orden de Compra.");
        partidaContableBtn.addClickListener((Button.ClickListener) event -> {
            if(ordenCompraGrid.getSelectedRow() == null){
                Notification.show("Por favor seleccione una orden de compra para poder ver su partida contable.", Notification.Type.WARNING_MESSAGE);
                return;
            }
            if(String.valueOf(ordenCompraContainer.getContainerProperty(ordenCompraGrid.getSelectedRow(), CODIGOCC_DOCUMENTO_PROPERTY).getValue()).trim().isEmpty()){
                Notification.show("No se puede mostrar la partida contable, esta orden de compra no tiene un documento gasto o compra asociado.", Notification.Type.WARNING_MESSAGE);
                return;
            }

            queryString = " SELECT * ";
            queryString += " FROM contabilidad_partida";
            queryString += " WHERE CodigoCC = '" + ordenCompraContainer.getContainerProperty(ordenCompraGrid.getSelectedRow(), CODIGOCC_DOCUMENTO_PROPERTY).getValue() + "'";
            queryString += " AND IdEmpresa = " + ((SopdiUI) mainUI).sessionInformation.getStrAccountingCompanyId();

            try {
                stQuery2 = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                rsRecords2 = stQuery2.executeQuery(queryString);

                if (rsRecords2.next()) { //  encontrado
                    MostrarPartidaContable mostrarPartidaContable = new MostrarPartidaContable(
                            rsRecords2.getString("CodigoPartida"),
                            rsRecords2.getString("Descripcion"),
                            rsRecords2.getString("NombreProveedor"),
                            rsRecords2.getString("TipoDocumento") + " " + rsRecords2.getString("NumeroDocumento")
                    );
                    UI.getCurrent().addWindow(mostrarPartidaContable);
                    mostrarPartidaContable.center();
                }
            } catch (Exception ex) {
                System.out.println("Error al intentar mostrar partida contable:" + ex);
                ex.printStackTrace();
            }
        });

        exportExcelBtn = new Button("Exportar a Excel");
        exportExcelBtn.setIcon(FontAwesome.FILE_EXCEL_O);
        exportExcelBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
        exportExcelBtn.setDescription("Exportar listado a Excel");
        exportExcelBtn.addClickListener(e -> exportarExcel());

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setWidth("100%");
        buttonsLayout.setMargin(new MarginInfo(false, true, true, true));
        buttonsLayout.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);

        buttonsLayout.addComponents(newBtn, editBtn, partidaContableBtn, printBtn, exportExcelBtn, delBtn);
        buttonsLayout.setExpandRatio(exportExcelBtn, 1);
        buttonsLayout.setComponentAlignment(exportExcelBtn, Alignment.MIDDLE_RIGHT);
        buttonsLayout.setComponentAlignment(delBtn, Alignment.MIDDLE_RIGHT);

        addComponent(buttonsLayout);
        setComponentAlignment(buttonsLayout, Alignment.MIDDLE_CENTER);
        setExpandRatio(buttonsLayout, 0);

    }

    private void agregarFiltroColumna(Grid.HeaderRow filterRow, String propertyId, int cols) {
        Grid.HeaderCell cell = filterRow.getCell(propertyId);
        TextField filterField = new TextField();
        filterField.addStyleName(ValoTheme.TEXTFIELD_TINY);
        filterField.setInputPrompt("Filtrar");
        filterField.setColumns(cols);
        filterField.addTextChangeListener(change -> {
            ordenCompraContainer.removeContainerFilters(propertyId);
            if (!change.getText().isEmpty()) {
                ordenCompraContainer.addContainerFilter(
                        new SimpleStringFilter(propertyId, change.getText(), true, false));
            }
            actualizarFooter();
        });
        cell.setComponent(filterField);
    }

    private void actualizarFooter() {
        BigDecimal totalSum = new BigDecimal(0).setScale(2, RoundingMode.HALF_UP);
        BigDecimal anticipoSum = new BigDecimal(0).setScale(2, RoundingMode.HALF_UP);
        int count = 0;
        for (Object itemId : ordenCompraGrid.getContainerDataSource().getItemIds()) {
            count++;
            try {
                String totalStr = String.valueOf(ordenCompraContainer.getContainerProperty(itemId, TOTAL_PROPERTY).getValue()).replace(",", "");
                String anticipoStr = String.valueOf(ordenCompraContainer.getContainerProperty(itemId, ANTICIPO_PROPERTY).getValue()).replace(",", "");
                totalSum = totalSum.add(new BigDecimal(totalStr).setScale(2, RoundingMode.HALF_UP));
                anticipoSum = anticipoSum.add(new BigDecimal(anticipoStr).setScale(2, RoundingMode.HALF_UP));
            } catch (Exception ignored) {
            }
        }
        footerRow.getCell(PROVEEDOR_PROPERTY).setText(count + (count == 1 ? " orden" : " órdenes"));
        footerRow.getCell(TOTAL_PROPERTY).setText(numberFormat.format(totalSum));
        footerRow.getCell(ANTICIPO_PROPERTY).setText(numberFormat.format(anticipoSum));
    }

    private void exportarExcel() {
        if (ordenCompraContainer.size() == 0) {
            Notification notif = new Notification("No hay registros para exportar.", Notification.Type.WARNING_MESSAGE);
            notif.setDelayMsec(1500);
            notif.setPosition(Position.MIDDLE_CENTER);
            notif.setIcon(FontAwesome.WARNING);
            notif.show(Page.getCurrent());
            return;
        }
        TableHolder tableHolder = new DefaultTableHolder(ordenCompraGrid);
        ExcelExport excelExport = new ExcelExport(tableHolder);
        excelExport.excludeCollapsedColumns();
        excelExport.setDisplayTotals(false);
        String fileexport = empresaId + "_" + empresaNombre.replaceAll(" ", "_").replaceAll(",", "_")
                .replaceAll("[()]", "").replaceAll("[.]", "")
                .replaceAll("ñ", "n").replaceAll("Ñ", "N")
                .replaceAll("ó", "o").replaceAll("é", "") + "_ORDENES_COMPRA.xls";
        excelExport.setExportFileName(fileexport);
        excelExport.export();
    }

    public void llenarTablaOrdenCompra() {

        ordenCompraContainer.removeAllItems();

        try {

            if (inicioDt.getValue().before(finDt.getValue())) {

                queryString = " SELECT *, empresa.Nombre as EmpresaNombre, project.Etiqueta AS ProjectNom, ";
                queryString += " proveedor_empresa.Nombre AS ProveedorNombre, tipo_orden_compra.Descripcion As TipoOrdenCompra ";
                queryString += " FROM orden_compra";
                queryString += " LEFT JOIN empresa ON orden_compra.IdEmpresa = empresa.IdEmpresa";
                queryString += " LEFT JOIN project ON orden_compra.IdProyecto = project.Id";
                queryString += " LEFT JOIN proveedor_empresa ON orden_compra.IdProveedor = proveedor_empresa.IdProveedor";
                queryString += " LEFT JOIN tipo_orden_compra ON orden_compra.IdTipoOrdenCompra = tipo_orden_compra.Id";
                queryString += " WHERE orden_compra.Fecha between " + "'" + Utileria.getFechaYYYYMMDD_1(inicioDt.getValue()) + "'";
                queryString += " AND '" + Utileria.getFechaYYYYMMDD_1(finDt.getValue()) + "'";
                queryString += " AND orden_compra.IdEmpresa =" + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();
                //"Todas", "CONSTRUCCION", "EVENTUAL", "RECURRENTE");
                switch (String.valueOf(ordenCompraOg.getValue())) {
                    case "CONSTRUCCION":
                        queryString += " AND ( ";
                        queryString += "          orden_compra.IdTipoOrdenCompra = 1";
                        queryString += "       || orden_compra.IdTipoOrdenCompra = 2";
                        queryString += "       || orden_compra.IdTipoOrdenCompra = 3";
                        queryString += "     ) ";
                        break;
                    case "EVENTUAL":
                        queryString += " AND orden_compra.IdTipoOrdenCompra = 4";
                        break;
                    case "RECURRENTE":
                        queryString += " AND orden_compra.IdTipoOrdenCompra = 5";
                        break;
                }
                queryString += " And orden_compra.IdTipoOrdenCompra In (SELECT IdTipoOrdenCompra FROM usuario_permisos_tipo_orden_compra WHERE IdUsuario = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrUserId() + ")";
                if(cerradasChbx.getValue()) {
                    queryString += " And orden_compra.Estado = 'CERRADA'";
                }
                else {
                    queryString += " And orden_compra.Estado = 'ABIERTA'";
                }
                queryString += " AND proveedor_empresa.IdEmpresa = " + empresaId;

Logger.getLogger(this.getClass().getName()).info(queryString);

                stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement();
                rsRecords = stQuery.executeQuery(queryString);

                if (rsRecords.next()) { //  encontrado    
                    do {
                        Object itemId = ordenCompraContainer.addItem();
                        ordenCompraContainer.getContainerProperty(itemId, ID_PROPERTY).setValue(rsRecords.getString("Id"));
                        //01234567890123456
                        //NOC210_6761240001
                        //12345678901234567
                        ordenCompraContainer.getContainerProperty(itemId, NOC_PROPERTY).setValue(rsRecords.getString("NOC").substring(7));
//                        ordenCompraContainer.getContainerProperty(itemId, TIPO_PROPERTY).setValue(rsRecords.getString("TipoOrdenCompra"));
                        ordenCompraContainer.getContainerProperty(itemId, EMPRESA_PROPERTY).setValue(rsRecords.getString("EmpresaNombre"));
                        ordenCompraContainer.getContainerProperty(itemId, PROYECTO_PROPERTY).setValue(rsRecords.getString("ProjectNom"));
                        ordenCompraContainer.getContainerProperty(itemId, PROVEEDOR_PROPERTY).setValue(rsRecords.getString("ProveedorNombre"));
                        ordenCompraContainer.getContainerProperty(itemId, FECHA_PROPERTY).setValue(Utileria.getFechaDDMMYYYY(rsRecords.getDate("Fecha")));
                        ordenCompraContainer.getContainerProperty(itemId, MONEDA_PROPERTY).setValue(rsRecords.getString("Moneda"));
                        ordenCompraContainer.getContainerProperty(itemId, TOTAL_PROPERTY).setValue(numberFormat.format(rsRecords.getDouble("Total")));
                        ordenCompraContainer.getContainerProperty(itemId, RESPONSABLE_PROPERTY).setValue(rsRecords.getString("Responsable"));
  //                      ordenCompraContainer.getContainerProperty(itemId, DIRECCIONENTREGA_PROPERTY).setValue(rsRecords.getString("DireccionEntrega"));
                        ordenCompraContainer.getContainerProperty(itemId, ANTICIPO_PROPERTY).setValue(numberFormat.format(rsRecords.getDouble("Anticipo")));
                        ordenCompraContainer.getContainerProperty(itemId, RAZON_PROPERTY).setValue(rsRecords.getString("Razon"));
//                        ordenCompraContainer.getContainerProperty(itemId, ESTADO_PROPERTY).setValue(rsRecords.getString("Estado"));
                        ordenCompraContainer.getContainerProperty(itemId, CODIGOCC_ANTICIPO_PROPERTY).setValue(rsRecords.getString("CodigoCCAnticipo"));
                        ordenCompraContainer.getContainerProperty(itemId, CODIGOCC_DOCUMENTO_PROPERTY).setValue(rsRecords.getString("CodigoCCDocumento"));
                        ordenCompraContainer.getContainerProperty(itemId, CREADOFECHAHORA_PROPERTY).setValue(rsRecords.getString("CreadoFechaYHora"));
                        ordenCompraContainer.getContainerProperty(itemId, CREADOUSUARIO_PROPERTY).setValue(rsRecords.getString("CreadoUsuario"));

                    } while (rsRecords.next());

                    ordenCompraGrid.select(ordenCompraContainer.firstItemId());
                }

                actualizarFooter();

            } else {
                Notification.show("La fecha hasta no puede contener un valor menor a la fecha de inicio.", Notification.Type.WARNING_MESSAGE);
                inicioDt.focus();
            }

        } catch (Exception ex) {
            System.out.println("Error al listar tabla oden de compra " + ex);
            ex.printStackTrace();
        }
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - Orden de Compra");
    }
}
