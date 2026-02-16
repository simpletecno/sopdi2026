package com.simpletecno.sopdi.contabilidad;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.addon.tableexport.DefaultTableHolder;
import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.addon.tableexport.TableHolder;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Date;

/**
 *
 * @author user>
 */
public class TopProveedoresView extends VerticalLayout implements View {

    public IndexedContainer proveedorContainer = new IndexedContainer();
    static final String IDPROVEEDOR_PROPERTY = "Codigo del proveedor";
    static final String NIT_PROPERTY = "NIT del proveedor";
    static final String NOMBRE_PROPERTY = "Nombre del proveedor";
    static final String CANTIDAD_PROPERTY = "Cantidad de Facturas";
    static final String TOTAL_PROPERTY = "Total BASE";

    Grid proveedorGrid;

    UI mainUI;
    Statement stQuery;
    ResultSet rsRecords;
    Statement stQuery1;
    ResultSet rsRecords1;

    OptionGroup sumarPorOG;
    Button consultarBtn;
    Button exportExcelBtn;
    ComboBox empresaCbx;
    public String empresa;
    DateField inicioDt;
    DateField finDt;

    static DecimalFormat numberFormat = new DecimalFormat("###,###,##0.00");
    static DecimalFormat quantityFormat = new DecimalFormat("###,###");

    public TopProveedoresView() {

        this.mainUI = UI.getCurrent();
        setWidth("100%");
        setSpacing(true);
        setMargin(false);
        setHeightUndefined();

        Label titleLbl = new Label("TOP 10 PROVEEDORES");
        titleLbl.addStyleName(ValoTheme.LABEL_H2);
        titleLbl.setSizeUndefined();

        empresaCbx = new ComboBox("Empresa:");
        empresaCbx.setWidth("400px");
        empresaCbx.addStyleName(ValoTheme.COMBOBOX_HUGE);
        empresaCbx.setInvalidAllowed(false);
        empresaCbx.setNewItemsAllowed(false);
        empresaCbx.setTextInputAllowed(false);

        llenarComboEmpresa();

        empresaCbx.addValueChangeListener(event -> {
            empresa = String.valueOf(event.getProperty().getValue());
        });

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setResponsive(true);
        titleLayout.setSpacing(true);
        titleLayout.setWidth("100%");
        titleLayout.setMargin(false);
        titleLayout.addComponents(empresaCbx, titleLbl);
        titleLayout.setComponentAlignment(empresaCbx, Alignment.MIDDLE_CENTER);
        titleLayout.setComponentAlignment(titleLbl, Alignment.MIDDLE_CENTER);
        titleLayout.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

        addComponent(titleLayout);
        setComponentAlignment(titleLayout, Alignment.TOP_CENTER);

        crearTablaTop10();

        empresa = String.valueOf(empresaCbx.getValue());

    }

    public void crearTablaTop10() {
        VerticalLayout layoutTablaLibroDiario = new VerticalLayout();
        layoutTablaLibroDiario.setWidth("100%");
        layoutTablaLibroDiario.setSpacing(true);
        layoutTablaLibroDiario.addStyleName("rcorners3");

        HorizontalLayout filtrosLayout = new HorizontalLayout();
        filtrosLayout.setSpacing(true);

        HorizontalLayout layoutButtons = new HorizontalLayout();
        layoutButtons.setSpacing(true);

        proveedorContainer.addContainerProperty(IDPROVEEDOR_PROPERTY, String.class, null);
        proveedorContainer.addContainerProperty(NIT_PROPERTY, String.class, null);
        proveedorContainer.addContainerProperty(NOMBRE_PROPERTY, String.class, null);
        proveedorContainer.addContainerProperty(CANTIDAD_PROPERTY, String.class, null);
        proveedorContainer.addContainerProperty(TOTAL_PROPERTY, String.class, null);

        proveedorGrid = new Grid(proveedorContainer);
        proveedorGrid.setImmediate(true);
        proveedorGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        proveedorGrid.setHeightMode(HeightMode.ROW);
        proveedorGrid.setHeightByRows(10);
        proveedorGrid.setWidth("100%");
        proveedorGrid.setResponsive(true);
        proveedorGrid.setEditorBuffered(false);

        proveedorGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {

            if (CANTIDAD_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (TOTAL_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else {
                return null;
            }

        });

        inicioDt = new DateField("DEL:");
        inicioDt.setDateFormat("dd/MM/yyyy");
        Date primerDia = Utileria.getPrimerDiaDelMes();
        inicioDt.setValue(primerDia);
        inicioDt.setWidth("8em");

        finDt = new DateField("AL:");
        finDt.setDateFormat("dd/MM/yyyy");
        Date ultimoDia = Utileria.getUltimoDiaDelMes();
        finDt.setValue(ultimoDia);
        finDt.setWidth("8em");

        sumarPorOG = new OptionGroup("Sumar por");
        sumarPorOG.addItem("Monto base");
        sumarPorOG.addItem("Cantidad");
        sumarPorOG.select("Monto base");
        sumarPorOG.addValueChangeListener(event -> {
            llenarGridTopTen();
        });

        consultarBtn = new Button("Consultar");
        consultarBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        consultarBtn.setIcon(FontAwesome.SEARCH);
        consultarBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                    llenarGridTopTen();
            }
        });

        exportExcelBtn = new Button("Excel");
        exportExcelBtn.setIcon(FontAwesome.FILE_EXCEL_O);
        exportExcelBtn.setWidth(120, Sizeable.UNITS_PIXELS);
        exportExcelBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {

                if (proveedorGrid.getHeightByRows() > 0) {
                    TableHolder tableHolder = new DefaultTableHolder(proveedorGrid);
                    ExcelExport excelExport = new ExcelExport(tableHolder);
                    excelExport.excludeCollapsedColumns();
                    excelExport.setDisplayTotals(false);
                    String fileexport;
                    fileexport = "topProveedores_" + empresaCbx.getItemCaption(empresaCbx.getValue()).replaceAll(" ", "_").replaceAll(",", "_").replaceAll("[()]", "").replaceAll("[.]", "").replaceAll("ñ", "n").replaceAll("Ñ", "N").replaceAll("ó", "o").replaceAll("é", "") + ".xls";
                    excelExport.setExportFileName(fileexport);
                    excelExport.export();
                }
            }
        });

        filtrosLayout.addComponents(inicioDt, finDt, sumarPorOG, consultarBtn);
        filtrosLayout.setComponentAlignment(consultarBtn, Alignment.BOTTOM_RIGHT);
        filtrosLayout.setComponentAlignment(sumarPorOG, Alignment.BOTTOM_RIGHT);
        filtrosLayout.setComponentAlignment(inicioDt, Alignment.MIDDLE_RIGHT);
        filtrosLayout.setComponentAlignment(finDt, Alignment.MIDDLE_RIGHT);

        layoutButtons.addComponent(exportExcelBtn);
        layoutButtons.setComponentAlignment(exportExcelBtn, Alignment.MIDDLE_CENTER);

        layoutTablaLibroDiario.addComponent(filtrosLayout);
        layoutTablaLibroDiario.addComponent(proveedorGrid);
        layoutTablaLibroDiario.addComponent(layoutButtons);
        layoutTablaLibroDiario.setComponentAlignment(layoutButtons, Alignment.MIDDLE_CENTER);
        layoutTablaLibroDiario.setComponentAlignment(filtrosLayout, Alignment.MIDDLE_CENTER);

        addComponent(layoutTablaLibroDiario);
        setComponentAlignment(layoutTablaLibroDiario, Alignment.MIDDLE_CENTER);
    }

    public void llenarGridTopTen() {

        if (proveedorGrid == null) {
            return;
        }

        proveedorContainer.removeAllItems();

        try {

            String queryString;
            queryString = " SELECT IdProveedor, NitProveedor, NombreProveedor, ";
            queryString += " COUNT(*) AS CantidadFacturas, SUM(HaberQuetzales / 1.12) AS Total ";
            queryString += " From contabilidad_partida";
            queryString += " WHERE IdEmpresa = " + empresaCbx.getValue();
            queryString += " AND TipoDocumento = 'FACTURA'";
            queryString += " AND IdProveedor > 0";
            queryString += " AND contabilidad_partida.Fecha BETWEEN ";
            queryString += "     '" + Utileria.getFechaYYYYMMDD_1(inicioDt.getValue()) + "'";
            queryString += " AND '" + Utileria.getFechaYYYYMMDD_1(finDt.getValue()) + "'";
            queryString += " AND IdNomenclatura IN (" + ((SopdiUI) mainUI).cuentasContablesDefault.getProveedores() + "," + ((SopdiUI) mainUI).cuentasContablesDefault.getInstituciones() + ")";
            queryString += " GROUP BY IdProveedor";
            if (sumarPorOG.getValue().toString().equals("Cantidad")) {
                queryString += " ORDER BY CantidadFacturas DESC limit 10";
            }
            else {
                queryString += " ORDER BY Total DESC limit 10";
            }

System.out.println("QUERY TOP PROVEEDORES = " + queryString);

            stQuery = ((SopdiUI) mainUI).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords = stQuery.executeQuery(queryString);

            if (rsRecords.next()) { //  encontrado                                                

                do {
                    Object itemId = proveedorContainer.addItem();

                    proveedorContainer.getContainerProperty(itemId, IDPROVEEDOR_PROPERTY).setValue(rsRecords.getString("IdProveedor"));
                    proveedorContainer.getContainerProperty(itemId, NIT_PROPERTY).setValue(rsRecords.getString("NitProveedor"));
                    proveedorContainer.getContainerProperty(itemId, NOMBRE_PROPERTY).setValue(rsRecords.getString("NombreProveedor"));
                    proveedorContainer.getContainerProperty(itemId, CANTIDAD_PROPERTY).setValue(quantityFormat.format(rsRecords.getInt("CantidadFacturas")));
                    proveedorContainer.getContainerProperty(itemId, TOTAL_PROPERTY).setValue(numberFormat.format(rsRecords.getDouble("Total")));

                } while (rsRecords.next());

            }

        } catch (Exception ex) {
            System.out.println("Error al listar tabla Facturas en TopProveedoresView:" + ex);
            ex.printStackTrace();
        }
    }

    public void llenarComboEmpresa() {
        String queryString = " SELECT * from contabilidad_empresa";
        queryString += " Where IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();

        try {
            stQuery1 = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords1 = stQuery1.executeQuery(queryString);

            while (rsRecords1.next()) { //  encontrado                
                empresaCbx.addItem(rsRecords1.getString("IdEmpresa"));
                empresaCbx.setItemCaption(rsRecords1.getString("IdEmpresa"), rsRecords1.getString("Empresa"));
            }
            rsRecords1.first();

            empresaCbx.select(rsRecords1.getString("IdEmpresa"));

        } catch (Exception ex1) {
            System.out.println("Error al listar empresas: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle("Sopdi - Top 10 PROVEEDORES");
    }
}
