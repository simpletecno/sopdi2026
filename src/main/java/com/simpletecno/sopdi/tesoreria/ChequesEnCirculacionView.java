package com.simpletecno.sopdi.tesoreria;

import com.simpletecno.sopdi.SopdiUI;
import com.simpletecno.sopdi.utilerias.Utileria;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.ValoTheme;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class ChequesEnCirculacionView extends VerticalLayout implements View {

    UI mainUI;
    ComboBox empresaCbx;
    String empresa;
    Utileria utileria;
    
    static final String NUMERO_CHEQUE_PROPERTY = "No. Cheque";
    static final String FECHA_EMITIDO_PROPERTY = "Fecha";
    static final String ID_PROVEEDOR_PROPERTY = "Proveedor";
    static final String NOMBRE_CHEQUE_PROPERTY = "Nombre";
    static final String MONEDA_CHEQUE = "Moneda";
    static final String MONTO_PROPERTY = "Monto";

    Grid containerGrid;
    IndexedContainer container;
    
    PreparedStatement stPreparedQuery = null;
    Statement stQuery;
    ResultSet rsRecords;
    String queryString;

    VerticalLayout reportLayout;

    public ChequesEnCirculacionView(){
        this.utileria = new Utileria();
        this.mainUI = UI.getCurrent();

        setWidth("100%");
        setMargin(false);
        setSpacing(true);
        setResponsive(true);

        Label titleLbl = new Label("Cheques en Circulación");
        if (mainUI.getPage().getBrowserWindowWidth() >= 736) {
            titleLbl.addStyleName(ValoTheme.LABEL_H2);
        }
        titleLbl.setSizeUndefined();
        titleLbl.addStyleName("h1_custom");

        empresaCbx = new ComboBox("Empresa:");
        if (mainUI.getPage().getBrowserWindowWidth() >= 736) {
            empresaCbx.setWidth("400px");
        }
        empresaCbx.setInvalidAllowed(false);
        empresaCbx.setNewItemsAllowed(false);
        empresaCbx.setTextInputAllowed(false);
        empresaCbx.setNullSelectionAllowed(false);
        if (mainUI.getPage().getBrowserWindowWidth() >= 736) {
            empresaCbx.addStyleName(ValoTheme.COMBOBOX_HUGE);
        }

        llenarComboEmpresa();

        empresa = String.valueOf(empresaCbx.getValue());

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setResponsive(true);
        titleLayout.setSpacing(true);
        titleLayout.setWidth("100%");
        titleLayout.setMargin(false);
        titleLayout.addComponents(empresaCbx, titleLbl);
        titleLayout.setComponentAlignment(empresaCbx, Alignment.MIDDLE_CENTER);
        titleLayout.setComponentAlignment(titleLbl, Alignment.MIDDLE_CENTER);
        titleLayout.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

        reportLayout = new VerticalLayout();
        reportLayout.addStyleName("rcorners3");
        reportLayout.setWidth("95%");
        reportLayout.setHeightUndefined();
        reportLayout.setResponsive(true);
        reportLayout.setSpacing(true);
        reportLayout.setMargin(false);

        addComponents(titleLayout, reportLayout);
        setComponentAlignment(titleLayout, Alignment.TOP_CENTER);
        setComponentAlignment(reportLayout, Alignment.TOP_CENTER);

        crearContainer();
        llenarContainer();
    }


    private void crearContainer(){
        container = new IndexedContainer();

        container.addContainerProperty(NUMERO_CHEQUE_PROPERTY, String.class, "");
        container.addContainerProperty(FECHA_EMITIDO_PROPERTY, String.class, "");
        container.addContainerProperty(ID_PROVEEDOR_PROPERTY, String.class, "");
        container.addContainerProperty(NOMBRE_CHEQUE_PROPERTY, String.class, "");
        container.addContainerProperty(MONEDA_CHEQUE, String.class, "");
        container.addContainerProperty(MONTO_PROPERTY, String.class, "");


        containerGrid = new Grid("", container);
        containerGrid.setWidth("95%");
        containerGrid.setImmediate(true);
        containerGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        containerGrid.setHeightMode(HeightMode.ROW);
        containerGrid.setHeightByRows(14);
        containerGrid.setResponsive(true);
        containerGrid.setResponsive(true);
        containerGrid.setEditorBuffered(false);
        containerGrid.setSizeFull();

        containerGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if (NUMERO_CHEQUE_PROPERTY.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else if (FECHA_EMITIDO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else if (ID_PROVEEDOR_PROPERTY.equals(cellReference.getPropertyId())) {
                return "rightalign";
            } else if (NOMBRE_CHEQUE_PROPERTY.equals(cellReference.getPropertyId())) {
                return "leftalign";
            } else if (MONEDA_CHEQUE.equals(cellReference.getPropertyId())) {
                return "centeralign";
            } else if (MONTO_PROPERTY.equals(cellReference.getPropertyId())) {
                return "leftalign";
            }else{
                return null;
            }
        });

        Grid.HeaderRow filterRow = containerGrid.appendHeaderRow();

        Utileria.addTextFilter(filterRow, NUMERO_CHEQUE_PROPERTY, container, 10);
        Utileria.addTextFilter(filterRow, FECHA_EMITIDO_PROPERTY, container, 10);
        Utileria.addTextFilter(filterRow, ID_PROVEEDOR_PROPERTY, container, 10);
        Utileria.addTextFilter(filterRow, NOMBRE_CHEQUE_PROPERTY, container, 30);
        Utileria.addTextFilter(filterRow, MONEDA_CHEQUE, container, 10);
        Utileria.addTextFilter(filterRow, MONTO_PROPERTY, container, 10);

        reportLayout.addComponent(containerGrid);
        reportLayout.setComponentAlignment(containerGrid, Alignment.BOTTOM_CENTER);
    }

    private void llenarComboEmpresa() {
        queryString = " SELECT * from contabilidad_empresa";
        queryString += " Where IdEmpresa = " + ((SopdiUI) UI.getCurrent()).sessionInformation.getStrAccountingCompanyId();

        empresaCbx.addContainerProperty("Nit", String.class, "");

        try {
            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsRecords = stQuery.executeQuery(queryString);

            while (rsRecords.next()) { //  encontrado
                empresaCbx.addItem(rsRecords.getString("IdEmpresa"));
                empresaCbx.setItemCaption(rsRecords.getString("IdEmpresa"), rsRecords.getString("Empresa"));
                empresaCbx.getContainerProperty(rsRecords.getString("IdEmpresa"), "Nit").setValue(rsRecords.getString("NIT"));
            }
            rsRecords.first();

            empresaCbx.select(rsRecords.getString("IdEmpresa"));

        } catch (Exception ex1) {
            System.out.println("Error al llenar Combo empresas: " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    private void llenarContainer(){
        queryString = "SELECT * ";
        queryString += "FROM contabilidad_partida ";
        queryString += "WHERE IdNomenclatura = 1 AND Fecha BETWEEN '2020-01-01' AND '" + Utileria.getFechaYYYYMMDD_1(Utileria.getUltimoDiaDelMes()) + "' ";
        queryString += "AND Estatus <> 'ANULADO' ";
        queryString += "AND TipoDocumento = 'CHEQUE' ";
        queryString += "AND TRIM(TipoDocumento) NOT IN ('PARTIDA CIERRE','PARTIDA APERTURA', 'TRANSACCION ESPECIAL', ";
        queryString +=                                 "'PARTIDA AJUSTE', 'PARTIDA AJUSTE1', 'PARTIDA AJUSTE TEMPORAL') ";
        queryString += "AND IdEmpresa = " + ((SopdiUI)mainUI).sessionInformation.getStrAccountingCompanyId() + " ";
        queryString += "AND IdConciliacion = 0 ";
        queryString += "AND Haber > 0 ";
        queryString += "ORDER BY Fecha";

        try {

            stQuery = ((SopdiUI) UI.getCurrent()).databaseProvider.getCurrentConnection().createStatement();
            rsRecords = stQuery.executeQuery(queryString);

            if(rsRecords.next()){
                do{
                    Object itemId = container.addItem();
                    container.getContainerProperty(itemId, NUMERO_CHEQUE_PROPERTY).setValue(rsRecords.getString("NumeroDocumento"));
                    container.getContainerProperty(itemId, FECHA_EMITIDO_PROPERTY).setValue(rsRecords.getString("Fecha"));
                    container.getContainerProperty(itemId, ID_PROVEEDOR_PROPERTY).setValue(rsRecords.getString("IdProveedor"));
                    container.getContainerProperty(itemId, NOMBRE_CHEQUE_PROPERTY).setValue(rsRecords.getString("NombreCheque"));
                    container.getContainerProperty(itemId, MONEDA_CHEQUE).setValue(rsRecords.getString("MonedaDocumento"));
                    if(rsRecords.getString("MonedaDocumento").equals("DOLARES")){
                        container.getContainerProperty(itemId, MONTO_PROPERTY).setValue("$\t " + Utileria.format(rsRecords.getDouble("Haber")));
                    }else {
                        container.getContainerProperty(itemId, MONTO_PROPERTY).setValue("Q\t" + Utileria.format(rsRecords.getDouble("Haber")));
                    }
                }while ((rsRecords.next()));
            }

        }catch (Exception ex1)
        {
            System.out.println("Error al listar Cheques en Circulacion " + ex1.getMessage());
            ex1.printStackTrace();
        }
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent viewChangeEvent) {
        
    }
}
